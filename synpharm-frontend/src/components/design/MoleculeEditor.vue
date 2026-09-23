<template>
  <div class="med">
    <iframe
      ref="frameRef"
      class="med__frame"
      :src="frameSrc"
      title="分子编辑器"
      allow="clipboard-write"
    />

    <div v-if="status !== 'ready'" class="med__overlay">
      <span class="med__overlay-icon">{{ overlayIcon }}</span>
      <span class="med__overlay-title">{{ overlayTitle }}</span>
      <span v-if="overlayDetail" class="med__overlay-detail">{{ overlayDetail }}</span>
      <button v-if="status === 'error'" type="button" class="med__overlay-btn" @click="reload">
        重新加载
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 2D 分子编辑器宿主（iframe 方式）。
 *
 * 设计意图：Ketcher 走独立子应用（ketcher-app/），本组件只做两件事 ——
 *   1) 把子应用放进 iframe，使其 Node polyfill 需求与 ~29 MB 化学引擎
 *      完全隔离在这个文档内，不进入主应用依赖图、不修改主构建配置；
 *   2) 用 postMessage 暴露一个窄接口（读/写分子），对上层仍是普通组件。
 *
 * 协议定义与 ketcher-app/main.ts 成对维护，修改任一侧需同步另一侧。
 * 子应用未构建时（public/ketcher 不存在）会在此给出明确指引，而不是白屏。
 */
import { computed, onBeforeUnmount, onMounted, ref, shallowRef } from 'vue'

/** 子应用入口；base 变化时自动跟随 */
const frameSrc = `${import.meta.env.BASE_URL}ketcher/index.html`

/** 子应用未启动时的提示：既可能是产物缺失，也可能是子应用内部报错 */
const BUILD_HINT =
  '编辑器子应用未能启动。请确认已执行 npm run build:ketcher 生成 public/ketcher，并检查浏览器控制台有无报错。'
/** 子应用脚本启动握手超时（需先下载并解析约 28 MB 的引擎分块，偏保守取值） */
const MOUNT_TIMEOUT_MS = 20000
/** 化学引擎（约 29 MB）加载超时，需给较宽裕的时间 */
const ENGINE_TIMEOUT_MS = 120000
/** 单次读写请求超时 */
const REQUEST_TIMEOUT_MS = 15000

type Status = 'loading' | 'engine' | 'ready' | 'error'

interface ChildMessage {
  type: 'mounted' | 'ready' | 'response' | 'error'
  requestId?: string
  ok?: boolean
  data?: string
  error?: string
  message?: string
}

interface Pending {
  resolve: (value: string) => void
  reject: (err: Error) => void
  timer: number
}

const emit = defineEmits<{
  (e: 'ready'): void
  (e: 'error', message: string): void
}>()

const frameRef = shallowRef<HTMLIFrameElement | null>(null)
const status = ref<Status>('loading')
const errorText = ref('')

let mountTimer = 0
let engineTimer = 0
let requestSeq = 0
const pending = new Map<string, Pending>()

const overlayIcon = computed(() => (status.value === 'error' ? '⚠️' : '⏳'))

const overlayTitle = computed(() => {
  switch (status.value) {
    case 'loading':
      return '正在启动分子编辑器…'
    case 'engine':
      return '正在下载化学引擎（约 29 MB，仅首次）…'
    case 'error':
      return '分子编辑器不可用'
    default:
      return ''
  }
})

const overlayDetail = computed(() => (status.value === 'error' ? errorText.value : ''))

/* ------------------------------------------------------------------ */
/* 与子应用通信                                                        */
/* ------------------------------------------------------------------ */

function post(message: Record<string, unknown>): void {
  frameRef.value?.contentWindow?.postMessage(message, window.location.origin)
}

function fail(message: string): void {
  status.value = 'error'
  errorText.value = message
  emit('error', message)
}

function settleAll(reason: string): void {
  pending.forEach((item) => {
    window.clearTimeout(item.timer)
    item.reject(new Error(reason))
  })
  pending.clear()
}

function onMessage(event: MessageEvent<ChildMessage>): void {
  // 仅接受同源、且确实来自本组件 iframe 的消息
  if (event.origin !== window.location.origin) return
  if (event.source !== frameRef.value?.contentWindow) return

  const data = event.data
  if (!data || typeof data.type !== 'string') return

  switch (data.type) {
    case 'mounted':
      window.clearTimeout(mountTimer)
      if (status.value === 'loading') {
        status.value = 'engine'
        engineTimer = window.setTimeout(() => {
          if (status.value !== 'ready') {
            fail('化学引擎加载超时，请检查 public/ketcher 是否完整，或网络是否受限。')
          }
        }, ENGINE_TIMEOUT_MS)
      }
      break

    case 'ready':
      window.clearTimeout(mountTimer)
      window.clearTimeout(engineTimer)
      status.value = 'ready'
      errorText.value = ''
      emit('ready')
      break

    case 'response': {
      const requestId = data.requestId
      if (!requestId) break
      const item = pending.get(requestId)
      if (!item) break
      pending.delete(requestId)
      window.clearTimeout(item.timer)
      if (data.ok) {
        item.resolve(data.data ?? '')
      } else {
        item.reject(new Error(data.error ?? '编辑器返回失败'))
      }
      break
    }

    case 'error':
      fail(data.message ?? '分子编辑器内部错误')
      break

    default:
      break
  }
}

function request(kind: 'getSmiles' | 'getMolfile'): Promise<string> {
  if (status.value !== 'ready') {
    return Promise.reject(new Error('编辑器尚未就绪'))
  }

  const requestId = `r${++requestSeq}`

  return new Promise<string>((resolve, reject) => {
    const timer = window.setTimeout(() => {
      pending.delete(requestId)
      reject(new Error('编辑器响应超时'))
    }, REQUEST_TIMEOUT_MS)

    pending.set(requestId, { resolve, reject, timer })
    post({ type: kind, requestId })
  })
}

/* ------------------------------------------------------------------ */
/* 生命周期                                                            */
/* ------------------------------------------------------------------ */

onMounted(() => {
  window.addEventListener('message', onMessage)

  // 子应用未构建时 iframe 会 404，且 onerror 不触发，只能靠握手超时兜底
  mountTimer = window.setTimeout(() => {
    if (status.value === 'loading') fail(BUILD_HINT)
  }, MOUNT_TIMEOUT_MS)
})

onBeforeUnmount(() => {
  window.removeEventListener('message', onMessage)
  window.clearTimeout(mountTimer)
  window.clearTimeout(engineTimer)
  settleAll('编辑器已卸载')
})

function reload(): void {
  settleAll('编辑器已重新加载')
  status.value = 'loading'
  errorText.value = ''
  const frame = frameRef.value
  if (frame) frame.src = frameSrc

  mountTimer = window.setTimeout(() => {
    if (status.value === 'loading') fail(BUILD_HINT)
  }, MOUNT_TIMEOUT_MS)
}

defineExpose({
  isReady: (): boolean => status.value === 'ready',
  getSmiles: (): Promise<string> => request('getSmiles'),
  getMolfile: (): Promise<string> => request('getMolfile'),
  setMolecule: (smiles: string): void => {
    post({ type: 'setMolecule', smiles })
  },
  layout: (): void => {
    post({ type: 'layout' })
  }
})
</script>

<style lang="scss" scoped>
.med {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 360px;
  background: $color-surface;
}

.med__frame {
  display: block;
  width: 100%;
  height: 100%;
  border: 0;
}

.med__overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  padding: $spacing-lg;
  background: $color-surface;
  text-align: center;
}

.med__overlay-icon {
  font-size: 28px;
}

.med__overlay-title {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $color-text;
}

.med__overlay-detail {
  max-width: 460px;
  font-size: $font-size-sm;
  line-height: 1.7;
  color: $color-text-faint;
  word-break: break-word;
}

.med__overlay-btn {
  margin-top: $spacing-xs;
  padding: 5px $spacing-md;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  font-size: $font-size-sm;
  color: $color-text-soft;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    border-color: $color-brand;
    color: $color-brand-strong;
  }
}
</style>
