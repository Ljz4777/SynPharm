<template>
  <div class="mc">
    <!-- 选择对比对象 -->
    <div class="mc__pickers">
      <div class="mc__pick">
        <span class="mc__pick-label">A</span>
        <select v-model.number="aId" class="mc__select">
          <option :value="0" disabled>选择分子…</option>
          <option v-for="c in candidates" :key="c.id" :value="c.id">
            {{ c.displayName || c.id }} · {{ c.compositeScore.toFixed(1) }}
          </option>
        </select>
      </div>

      <button
        type="button"
        class="mc__swap"
        title="交换 A / B"
        :disabled="!aId || !bId"
        @click="swap"
      >
        ⇄
      </button>

      <div class="mc__pick">
        <span class="mc__pick-label">B</span>
        <select v-model.number="bId" class="mc__select">
          <option :value="0" disabled>选择分子…</option>
          <option v-for="c in candidates" :key="c.id" :value="c.id">
            {{ c.displayName || c.id }} · {{ c.compositeScore.toFixed(1) }}
          </option>
        </select>
      </div>

      <span class="mc__spacer" />
      <span class="mc__summary">{{ deltaSummary }}</span>
    </div>

    <!-- 并排结构式（由编辑器子应用经 Indigo render 生成） -->
    <div class="mc__panels">
      <div v-for="side in ['a', 'b'] as const" :key="side" class="mc__panel">
        <div class="mc__panel-head">
          <span class="mc__panel-tag">{{ side.toUpperCase() }}</span>
          <span class="mc__panel-name">{{ sideData[side].candidate?.displayName ?? '—' }}</span>
          <span
            v-if="sideData[side].candidate?.originType === 'EDITED'"
            class="mc__panel-origin"
          >
            编辑衍生
          </span>
        </div>

        <div class="mc__structure">
          <img
            v-if="sideData[side].image"
            class="mc__structure-img"
            :src="sideData[side].image"
            :alt="`${side.toUpperCase()} 结构式`"
          />
          <span v-else class="mc__structure-hint" :class="{ 'mc__structure-hint--err': sideData[side].error }">
            {{
              sideData[side].rendering
                ? '结构式渲染中…'
                : sideData[side].error || '未选择分子'
            }}
          </span>
        </div>

        <div class="mc__panel-foot">
          <span class="mc__smiles" :title="sideData[side].candidate?.smiles">
            {{ sideData[side].candidate?.smiles ?? '—' }}
          </span>
          <span class="mc__inchikey">{{ sideData[side].candidate?.inchikey ?? '' }}</span>
        </div>
      </div>
    </div>

    <!-- 指标差值 -->
    <div class="mc__deltas">
      <table class="mc__table">
        <thead>
          <tr>
            <th class="mc__th">指标</th>
            <th class="mc__th mc__th--num">A</th>
            <th class="mc__th mc__th--num">B</th>
            <th class="mc__th mc__th--num">差值 (B−A)</th>
            <th class="mc__th mc__th--verdict">评价</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!deltas.length">
            <td class="mc__td mc__td--empty" colspan="5">选择两个分子后显示逐项差值</td>
          </tr>
          <tr v-for="row in deltas" :key="row.code" class="mc__row">
            <td class="mc__td">
              {{ row.name }}
              <span v-if="row.weight === 0" class="mc__td-note">仅展示</span>
            </td>
            <td class="mc__td mc__td--num">{{ row.a }}</td>
            <td class="mc__td mc__td--num">{{ row.b }}</td>
            <td class="mc__td mc__td--num mc__td--delta" :class="row.toneClass">
              {{ row.deltaText }}
            </td>
            <td class="mc__td mc__td--verdict" :class="row.toneClass">{{ row.verdict }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 分子对比。
 *
 * 结构式由编辑器子应用通过 Indigo 的 render 能力生成 SVG，
 * 指标与约束明细走 `designApi`，因此本组件同样不区分 mock 与真实后端。
 *
 * 说明：真正的结构差异高亮（MCS 共有骨架 / 差异原子着色）需要 RDKit 的
 * 最大公共子结构计算，属于后端能力，待 `/v1/evaluate` 提供后接入；
 * 当前先给出「并排结构式 + 逐项指标差值」，这两项已能支撑绝大多数判断。
 */
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { designApi } from '@/api/design'
import { useDesignStore } from '@/stores/design'
import type { DesignCandidate, MetricDefinition, MoleculeMetrics } from '@/types/design'

const props = defineProps<{
  /** 由工作台注入：调用编辑器子应用渲染结构式，返回 data URL */
  renderStructure: (smiles: string) => Promise<string>
  /** 初始对比对象（通常为当前选中候选及其父代） */
  initialA?: number
  initialB?: number
}>()

const store = useDesignStore()

const candidates = ref<DesignCandidate[]>([])
// 初始置 0（未选择），由 onMounted 赋实际值 —— 使 watch 必定触发一次
const aId = ref(0)
const bId = ref(0)

interface SideState {
  candidate: DesignCandidate | null
  metrics: MoleculeMetrics | null
  image: string
  rendering: boolean
  /** 渲染失败原因（不静默留空，便于定位与展示） */
  error: string
}

const sideData = reactive<Record<'a' | 'b', SideState>>({
  a: { candidate: null, metrics: null, image: '', rendering: false, error: '' },
  b: { candidate: null, metrics: null, image: '', rendering: false, error: '' }
})

/* ------------------------------ 指标差值 ------------------------------ */

interface DeltaRow {
  code: string
  name: string
  weight: number
  a: string
  b: string
  deltaText: string
  verdict: string
  toneClass: string
}

const decimalsOf = (definition: MetricDefinition): number =>
  definition.unit === 'Da' ? 1 : definition.metricCode === 'QED' ? 3 : 2

function formatValue(definition: MetricDefinition, value: number | undefined): string {
  if (value === undefined) return '—'
  const unit = definition.unit ? ` ${definition.unit}` : ''
  return `${value.toFixed(decimalsOf(definition))}${unit}`
}

const deltas = computed<DeltaRow[]>(() => {
  const aValues = new Map((sideData.a.metrics?.values ?? []).map((v) => [v.metricCode, v]))
  const bValues = new Map((sideData.b.metrics?.values ?? []).map((v) => [v.metricCode, v]))
  if (!aValues.size || !bValues.size) return []

  const rows: DeltaRow[] = []

  store.metricDefinitions
    .filter((d) => d.valueType === 'NUMERIC' && d.enabled)
    .forEach((definition) => {
      const av = aValues.get(definition.metricCode)?.valueNum
      const bv = bValues.get(definition.metricCode)?.valueNum
      if (av === undefined || bv === undefined) return

      const delta = bv - av
      const decimals = decimalsOf(definition)
      const sign = delta > 0 ? '+' : ''
      const deltaText = Math.abs(delta) < 10 ** -decimals ? '0' : `${sign}${delta.toFixed(decimals)}`

      // 按指标方向判定改善 / 变差（better=NONE 表示区间型，不做好坏判断）
      let verdict = '—'
      let toneClass = ''
      if (definition.better !== 'NONE' && Math.abs(delta) >= 10 ** -decimals) {
        const improved = definition.better === 'HIGHER' ? delta > 0 : delta < 0
        verdict = improved ? '改善' : '变差'
        toneClass = improved ? 'mc__delta--good' : 'mc__delta--bad'
      }

      rows.push({
        code: definition.metricCode,
        name: definition.metricName,
        weight: definition.weight,
        a: formatValue(definition, av),
        b: formatValue(definition, bv),
        deltaText,
        verdict,
        toneClass
      })
    })

  return rows
})

const deltaSummary = computed(() => {
  const aScore = sideData.a.metrics?.compositeScore
  const bScore = sideData.b.metrics?.compositeScore
  if (aScore === undefined || bScore === undefined) return '选择两个分子以对比'

  const delta = bScore - aScore
  if (Math.abs(delta) < 0.05) return '两者综合得分相同'
  return delta > 0
    ? `B 综合得分高 ${delta.toFixed(1)}`
    : `B 综合得分低 ${Math.abs(delta).toFixed(1)}`
})

/* ------------------------------ 数据装载 ------------------------------ */

async function loadSide(side: 'a' | 'b', id: number): Promise<void> {
  const state = sideData[side]
  if (!id) {
    state.candidate = null
    state.metrics = null
    state.image = ''
    return
  }

  state.candidate = candidates.value.find((c) => c.id === id) ?? null
  if (!state.candidate) return

  // 结构式渲染与指标拉取互不依赖，并行执行
  state.rendering = true
  state.error = ''
  const smiles = state.candidate.smiles

  void props
    .renderStructure(smiles)
    .then((image) => {
      // 期间用户可能已切换分子，丢弃过期结果
      if (sideData[side].candidate?.id !== id) return
      state.image = image
      if (!image) state.error = '渲染服务返回空结果'
    })
    .catch((err: unknown) => {
      if (sideData[side].candidate?.id !== id) return
      state.image = ''
      state.error = err instanceof Error ? err.message : '结构式渲染失败'
    })
    .finally(() => {
      if (sideData[side].candidate?.id === id) state.rendering = false
    })

  try {
    state.metrics = await designApi.fetchMoleculeMetrics(id)
  } catch {
    state.metrics = null
  }
}

function swap(): void {
  const a = aId.value
  aId.value = bId.value
  bId.value = a
}

watch(aId, (id) => void loadSide('a', id))
watch(bId, (id) => void loadSide('b', id))

onMounted(async () => {
  try {
    // 对比对象取自当前项目的候选（按综合得分排序），数量取够即可
    const page = await designApi.fetchCandidates({
      projectId: store.projectId,
      page: 1,
      pageSize: 200,
      sortBy: 'compositeScore',
      sortOrder: 'desc'
    })
    candidates.value = page.list
  } catch {
    candidates.value = []
  }

  // 用 || 而非 ??：调用方可能把「未选中」传成 0，而分子 id 永不为 0
  const selected = props.initialA || store.selectedMolId || candidates.value[0]?.id || 0
  const selectedCandidate = candidates.value.find((c) => c.id === selected)

  // 默认 A 优先取父代 —— 直接呈现「这次编辑改了什么」；
  // 若是生成而非编辑产物（无父代），则取本轮最优的另一个分子，避免两边是同一个。
  const fallbackA = candidates.value.find((c) => c.id !== selected)?.id
  aId.value = selectedCandidate?.parentMolId || fallbackA || selected
  bId.value = props.initialB || selected
})

/** 供父组件在选中候选变化时同步对比对象 */
defineExpose({
  setPair(a: number, b: number): void {
    aId.value = a
    bId.value = b
  }
})
</script>

<style lang="scss" scoped>
.mc {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: $color-surface;
}

/* ---------------- 选择区 ---------------- */
.mc__pickers {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  border-bottom: 1px solid $color-border;
}

.mc__pick {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.mc__pick-label {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 4px;
  background: $color-brand-soft;
  color: $color-brand-strong;
  font-size: 11px;
  font-weight: $font-weight-semibold;
}

.mc__select {
  width: 220px;
  padding: 4px $spacing-sm;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  font-size: $font-size-sm;
  color: $color-text;
}

.mc__swap {
  padding: 3px 8px;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  font-size: $font-size-sm;
  color: $color-text-soft;
  cursor: pointer;

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}

.mc__spacer {
  flex: 1;
}

.mc__summary {
  font-size: $font-size-xs;
  color: $color-text-soft;
}

/* ---------------- 结构式并排 ---------------- */
.mc__panels {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1px;
  background: $color-border;
  border-bottom: 1px solid $color-border;
  flex: 0 0 auto;
  height: 260px;
}

.mc__panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: $color-surface;
}

.mc__panel-head {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px $spacing-md;
  border-bottom: 1px solid $color-border-soft;
}

.mc__panel-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 4px;
  background: $color-brand;
  color: #fff;
  font-size: 11px;
  font-weight: $font-weight-semibold;
}

.mc__panel-name {
  font-size: $font-size-sm;
  color: $color-text;
}

.mc__panel-origin {
  padding: 0 5px;
  border-radius: 3px;
  background: $color-brand-soft;
  color: $color-brand-strong;
  font-size: 10px;
}

.mc__structure {
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: $spacing-sm;
}

.mc__structure-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.mc__structure-hint {
  font-size: $font-size-sm;
  color: $color-text-faint;

  &--err {
    max-width: 260px;
    text-align: center;
    color: $error-color;
    word-break: break-word;
  }
}

.mc__panel-foot {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: 4px $spacing-md;
  border-top: 1px solid $color-border-soft;
  font-size: 10px;
  color: $color-text-faint;
}

.mc__smiles {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

.mc__inchikey {
  flex-shrink: 0;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

/* ---------------- 差值表 ---------------- */
.mc__deltas {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.mc__table {
  width: 100%;
  border-collapse: collapse;
  font-size: $font-size-sm;
}

.mc__th {
  position: sticky;
  top: 0;
  z-index: 1;
  padding: 5px $spacing-md;
  background: $color-surface-alt;
  border-bottom: 1px solid $color-border;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $color-text-soft;
  text-align: left;

  &--num {
    text-align: right;
    width: 110px;
  }

  &--verdict {
    width: 64px;
  }
}

.mc__td {
  padding: 4px $spacing-md;
  border-bottom: 1px solid $color-border-soft;
  color: $color-text-soft;

  &--num {
    text-align: right;
    font-variant-numeric: tabular-nums;
  }

  &--delta {
    font-weight: $font-weight-medium;
  }

  &--verdict {
    font-size: $font-size-xs;
  }

  &--empty {
    padding: $spacing-lg;
    text-align: center;
    color: $color-text-faint;
  }
}

.mc__td-note {
  margin-left: 4px;
  font-size: 10px;
  color: $color-text-faint;
}

.mc__delta--good {
  color: $success-color;
}

.mc__delta--bad {
  color: $error-color;
}
</style>
