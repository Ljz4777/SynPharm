/**
 * 干实验设计工作台 · 2D 分子编辑器子应用
 * ============================================================================
 * 为什么是独立子应用 + iframe：
 *   Ketcher 的 npm 包对主应用来说代价很高 ——
 *     1) 依赖链引用 Node 内置模块（events / util / assert / process），
 *        需要 polyfill，会污染主构建配置；
 *     2) 自带的 Indigo 化学引擎为 13~21 MB，且 binaryWasm 变体会把 WASM
 *        以字符串字面量内联进 JS（单行可达 21 MB），无法拆成独立静态文件。
 *   放进独立子应用后，上述代价全部被限制在这一个 iframe 文档里，
 *   主应用不引入任何 Ketcher 代码、不修改 vite 配置、也不受影响其体积。
 *
 * 通信协议（与 src/components/design/MoleculeEditor.vue 成对维护）：
 *   父 → 子  setMolecule{smiles} | getSmiles{requestId} | getMolfile{requestId} | layout
 *            | renderImage{smiles, format, requestId}
 *   子 → 父  mounted | ready | response{requestId,ok,data|error} | error{message}
 */
import { createElement } from 'react'
import { createRoot } from 'react-dom/client'
// 必须排在 ketcher-react 之前：Ketcher 求值时会读取本模块注册的全局 Raphael
import './raphael-bridge'
import { Editor } from 'ketcher-react'
import { StandaloneStructServiceProvider } from 'ketcher-standalone'
import 'ketcher-react/dist/index.css'

/** Ketcher 实例上本项目用到的 API 子集 */
interface KetcherApi {
  getSmiles: () => Promise<string>
  getMolfile: () => Promise<string>
  setMolecule: (structure: string) => Promise<void>
  layout: () => Promise<void>
  /** 把任意结构式渲染为图片（Ketcher 的公开 API，走 Indigo render） */
  generateImage: (
    data: string,
    options?: { outputFormat: 'png' | 'svg'; backgroundColor?: string }
  ) => Promise<Blob>
}

type ParentMessage =
  | { type: 'setMolecule'; smiles: string }
  | { type: 'getSmiles'; requestId: string }
  | { type: 'getMolfile'; requestId: string }
  | { type: 'layout' }
  | { type: 'renderImage'; smiles: string; format: 'png' | 'svg'; requestId: string }

/** 仅接受同源父窗口的消息，避免被第三方页面驱动 */
const PARENT_ORIGIN = window.location.origin

let ketcher: KetcherApi | null = null

function toParent(message: Record<string, unknown>): void {
  window.parent.postMessage(message, PARENT_ORIGIN)
}

function hideBoot(): void {
  document.getElementById('boot')?.remove()
}

function showBootError(message: string): void {
  const boot = document.getElementById('boot')
  if (!boot) return
  boot.innerHTML = ''
  const icon = document.createElement('span')
  icon.className = 'boot__icon'
  icon.textContent = '⚠️'
  const text = document.createElement('span')
  text.textContent = `编辑器加载失败：${message}`
  boot.append(icon, text)
  toParent({ type: 'error', message })
}

/**
 * 诊断钩子：Ketcher 初始化失败时往往只留下一个未处理的 Promise 拒绝，
 * 界面上什么也不显示（本页表现为一直停在「加载中」）。
 * 这里把未捕获错误与看门狗超时都暴露到启动态上，避免静默失败。
 */
window.addEventListener('error', (event) => {
  showBootError(`uncaught: ${event.message}`)
})

window.addEventListener('unhandledrejection', (event) => {
  const reason = event.reason
  showBootError(`unhandled rejection: ${reason instanceof Error ? reason.message : String(reason)}`)
})

window.setTimeout(() => {
  if (!ketcher) showBootError('化学引擎初始化超时（90s）未完成')
}, 90000)

/** 单例即可：内部持有 Indigo worker，重复构造会重复拉取引擎 */
const structServiceProvider = new StandaloneStructServiceProvider()

/**
 * Blob → data URL。
 * Ketcher 的 generateImage 返回 Blob，而跨 iframe 传 Blob 不如传字符串方便，
 * 这里直接转成 data URL，父页面可以拿去当中 <img> 的 src。
 */
function blobToDataUrl(blob: Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result ?? ''))
    reader.onerror = () => reject(new Error('结构式图片读取失败'))
    reader.readAsDataURL(blob)
  })
}

/**
 * 结构式渲染串行化。
 * 同一个 Ketcher 实例内部共用一个 Indigo 实例，并发调用 generateImage 会互相干扰
 * （实测并发两次时只有一次成功）。对比页签会同时渲染 A / B 两个结构式，
 * 因此在这里排队，调用方无需关心。
 */
let renderChain: Promise<unknown> = Promise.resolve()

function enqueueRender<T>(task: () => Promise<T>): Promise<T> {
  const result = renderChain.then(task, task)
  // 无论成功失败都让队列继续，避免一次失败卡死后续请求
  renderChain = result.catch(() => undefined)
  return result
}

const host = document.getElementById('root')

if (host) {
  createRoot(host).render(
    createElement(Editor, {
      staticResourcesUrl: '',
      structServiceProvider,
      /**
       * 只保留小分子编辑器。
       * Ketcher 3.x 另有一个面向肽/RNA 的「大分子编辑器」，其渲染器依赖 Raphael，
       * 在经 Rollup 打包后 `require('raphael')` 的 CommonJS 互操作拿不到构造函数，
       * 会在初始化时抛 `TypeError: po is not a constructor` 并导致整个编辑器起不来。
       * 干实验设计只处理小分子，因此直接关闭它，既规避该问题也加快初始化。
       */
      disableMacromoleculesEditor: true,
      errorHandler: (message: string) => showBootError(message),
      onInit: (instance: unknown) => {
        ketcher = instance as KetcherApi
        hideBoot()
        toParent({ type: 'ready' })
      }
    })
  )
} else {
  showBootError('未找到挂载节点 #root')
}

window.addEventListener('message', (event: MessageEvent<ParentMessage>) => {
  if (event.origin !== PARENT_ORIGIN) return

  const data = event.data
  if (!data || typeof data !== 'object' || typeof data.type !== 'string') {
    return
  }

  void handleMessage(data)
})

async function handleMessage(data: ParentMessage): Promise<void> {
  try {
    switch (data.type) {
      case 'setMolecule':
        await ketcher?.setMolecule(data.smiles)
        break

      case 'getSmiles': {
        const smiles = (await ketcher?.getSmiles()) ?? ''
        toParent({ type: 'response', requestId: data.requestId, ok: true, data: smiles })
        break
      }

      case 'getMolfile': {
        const molfile = (await ketcher?.getMolfile()) ?? ''
        toParent({ type: 'response', requestId: data.requestId, ok: true, data: molfile })
        break
      }

      case 'layout':
        await ketcher?.layout()
        break

      case 'renderImage': {
        const instance = ketcher
        if (!instance) throw new Error('编辑器尚未就绪')
        // 用 Ketcher 实例自带的方法：它内部已登记好 ketcherId，无需另建服务
        const dataUrl = await enqueueRender(async () => {
          const blob = await instance.generateImage(data.smiles, { outputFormat: data.format })
          return blobToDataUrl(blob)
        })
        toParent({ type: 'response', requestId: data.requestId, ok: true, data: dataUrl })
        break
      }

      default:
        break
    }
  } catch (err) {
    const message = err instanceof Error ? err.message : String(err)
    const requestId = 'requestId' in data ? data.requestId : undefined

    if (requestId) {
      toParent({ type: 'response', requestId, ok: false, error: message })
    } else {
      toParent({ type: 'error', message })
    }
  }
}

// 通知父窗口：脚本已执行（此时引擎可能仍在加载）
toParent({ type: 'mounted' })
