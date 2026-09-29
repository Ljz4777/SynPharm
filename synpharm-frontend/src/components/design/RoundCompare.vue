<template>
  <div class="rc">
    <!-- 轮次选择 -->
    <div class="rc__toolbar">
      <div class="rc__pick">
        <span class="rc__pick-label">基准轮次</span>
        <select v-model.number="baseRoundId" class="rc__select">
          <option v-for="r in rounds" :key="r.id" :value="r.id">{{ r.name }}</option>
        </select>
      </div>
      <span class="rc__arrow">→</span>
      <div class="rc__pick">
        <span class="rc__pick-label">对比轮次</span>
        <select v-model.number="targetRoundId" class="rc__select">
          <option v-for="r in rounds" :key="r.id" :value="r.id">{{ r.name }}</option>
        </select>
      </div>
      <span class="rc__spacer" />
      <span class="rc__hint">{{ loading ? '统计中…' : `共 ${rounds.length} 个轮次` }}</span>
    </div>

    <!-- 指标趋势 -->
    <section class="rc__section">
      <h4 class="rc__title">指标趋势</h4>
      <div ref="chartRef" class="rc__chart" />
    </section>

    <!-- 对比统计 -->
    <section class="rc__section">
      <h4 class="rc__title">轮次统计对比</h4>
      <table class="rc__table">
        <thead>
          <tr>
            <th class="rc__th">统计项</th>
            <th class="rc__th rc__th--num">{{ baseRound?.name ?? '基准' }}</th>
            <th class="rc__th rc__th--num">{{ targetRound?.name ?? '对比' }}</th>
            <th class="rc__th rc__th--num">变化</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in statRows" :key="row.label" class="rc__row">
            <td class="rc__td">{{ row.label }}</td>
            <td class="rc__td rc__td--num">{{ row.base }}</td>
            <td class="rc__td rc__td--num">{{ row.target }}</td>
            <td class="rc__td rc__td--num" :class="row.toneClass">{{ row.delta }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- 候选流向 -->
    <section class="rc__section">
      <h4 class="rc__title">候选流向</h4>
      <div class="rc__flow">
        <div class="rc__flow-node">
          <span class="rc__flow-num">{{ flow.baseTotal }}</span>
          <span class="rc__flow-label">{{ baseRound?.name }} 候选</span>
        </div>
        <div class="rc__flow-arrow">
          <span class="rc__flow-through">通过硬约束 {{ flow.basePassed }}</span>
          <span class="rc__flow-line">──▶</span>
        </div>
        <div class="rc__flow-node">
          <span class="rc__flow-num">{{ flow.derived }}</span>
          <span class="rc__flow-label">衍生到 {{ targetRound?.name }}</span>
        </div>
        <div class="rc__flow-gap" />
        <div class="rc__flow-node">
          <span class="rc__flow-num rc__flow-num--dim">{{ flow.targetRejected }}</span>
          <span class="rc__flow-label">本轮被淘汰</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
/**
 * 轮次对比。
 *
 * 回答"这一轮比上一轮好在哪里"：
 *   - 指标趋势：各轮 design_round.metricSnapshot 的走势（真实数据，非推断）
 *   - 统计对比：候选数、通过率、得分均值/最优
 *   - 候选流向：上一轮有多少候选通过硬约束、有多少被衍生到本轮
 *
 * 数据全部来自 `designApi`，与 mock / 真实后端无关。
 */
import { computed, onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import * as echarts from 'echarts'
import { designApi } from '@/api/design'
import { useDesignStore } from '@/stores/design'
import type { DesignCandidate, DesignRound } from '@/types/design'

const store = useDesignStore()

const rounds = computed<DesignRound[]>(() => store.rounds)
const baseRoundId = ref(0)
const targetRoundId = ref(0)
const loading = ref(false)

/** 两个轮次的候选（用于统计与流向） */
const baseCandidates = ref<DesignCandidate[]>([])
const targetCandidates = ref<DesignCandidate[]>([])

const chartRef = ref<HTMLElement | null>(null)
const chart = shallowRef<ReturnType<typeof echarts.init> | null>(null)
let observer: ResizeObserver | null = null

const baseRound = computed(() => rounds.value.find((r) => r.id === baseRoundId.value) ?? null)
const targetRound = computed(() => rounds.value.find((r) => r.id === targetRoundId.value) ?? null)

/* ------------------------------ 统计 ------------------------------ */

const passCount = (list: DesignCandidate[]): number =>
  list.filter((c) => c.reviewStatus !== 'REJECTED').length

const avgScore = (list: DesignCandidate[]): number => {
  const passed = list.filter((c) => c.reviewStatus !== 'REJECTED')
  if (!passed.length) return 0
  return passed.reduce((s, c) => s + c.compositeScore, 0) / passed.length
}

const bestScore = (list: DesignCandidate[]): number =>
  list.reduce((max, c) => Math.max(max, c.compositeScore), 0)

interface StatRow {
  label: string
  base: string
  target: string
  delta: string
  toneClass: string
}

/** 数值型统计的统一呈现：给出变化量与好坏方向 */
function numericRow(
  label: string,
  baseValue: number,
  targetValue: number,
  higherIsBetter: boolean,
  digits = 1,
  suffix = ''
): StatRow {
  const delta = targetValue - baseValue
  const unchanged = Math.abs(delta) < 10 ** -digits
  const better = higherIsBetter ? delta > 0 : delta < 0
  return {
    label,
    base: `${baseValue.toFixed(digits)}${suffix}`,
    target: `${targetValue.toFixed(digits)}${suffix}`,
    delta: unchanged ? '—' : `${delta > 0 ? '+' : ''}${delta.toFixed(digits)}${suffix}`,
    toneClass: unchanged ? '' : better ? 'rc__delta--good' : 'rc__delta--bad'
  }
}

const statRows = computed<StatRow[]>(() => {
  const base = baseCandidates.value
  const target = targetCandidates.value
  if (!base.length && !target.length) return []

  const basePassed = passCount(base)
  const targetPassed = passCount(target)

  return [
    numericRow('候选总数', base.length, target.length, true, 0),
    numericRow('通过硬约束', basePassed, targetPassed, true, 0),
    numericRow(
      '通过率',
      base.length ? (basePassed / base.length) * 100 : 0,
      target.length ? (targetPassed / target.length) * 100 : 0,
      true,
      1,
      '%'
    ),
    numericRow('综合得分均值', avgScore(base), avgScore(target), true, 2),
    numericRow('综合得分最优', bestScore(base), bestScore(target), true, 2),
    numericRow(
      '编辑衍生数',
      base.filter((c) => c.originType === 'EDITED').length,
      target.filter((c) => c.originType === 'EDITED').length,
      false,
      0
    )
  ]
})

/** 流向：基准轮通过硬约束的候选 → 本轮由它们衍生出的候选数 */
const flow = computed(() => {
  const baseIds = new Set(baseCandidates.value.map((c) => c.id))
  const derived = targetCandidates.value.filter(
    (c) => c.parentMolId !== undefined && baseIds.has(c.parentMolId)
  ).length

  return {
    baseTotal: baseCandidates.value.length,
    basePassed: passCount(baseCandidates.value),
    derived,
    targetRejected: targetCandidates.value.filter((c) => c.reviewStatus === 'REJECTED').length
  }
})

/* ------------------------------ 图表 ------------------------------ */

function buildOption(): echarts.EChartsOption {
  const labels = rounds.value.map((r) => r.name)
  const seriesOf = (key: string, name: string, color: string): echarts.SeriesOption => ({
    name,
    type: 'line',
    smooth: true,
    symbolSize: 7,
    data: rounds.value.map((r) => r.metricSnapshot?.[key] ?? null),
    itemStyle: { color },
    lineStyle: { width: 2 }
  })

  return {
    grid: { left: 46, right: 56, top: 34, bottom: 30 },
    tooltip: { trigger: 'axis' },
    legend: {
      top: 4,
      right: 8,
      itemWidth: 12,
      itemHeight: 8,
      textStyle: { fontSize: 11, color: '#475569' }
    },
    xAxis: {
      type: 'category',
      data: labels,
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { fontSize: 11, color: '#64748b' }
    },
    yAxis: [
      {
        type: 'value',
        name: '得分 / 通过率',
        nameTextStyle: { fontSize: 10, color: '#94a3b8' },
        axisLabel: { fontSize: 11, color: '#64748b' },
        splitLine: { lineStyle: { color: '#f1f5f9' } }
      },
      {
        type: 'value',
        name: 'QED / SA',
        nameTextStyle: { fontSize: 10, color: '#94a3b8' },
        axisLabel: { fontSize: 11, color: '#64748b' },
        splitLine: { show: false }
      }
    ],
    series: [
      { ...seriesOf('compositeScore', '综合得分', '#3b82f6'), yAxisIndex: 0 },
      { ...seriesOf('passRate', '通过率 %', '#10b981'), yAxisIndex: 0 },
      { ...seriesOf('qed', 'QED', '#f59e0b'), yAxisIndex: 1 },
      { ...seriesOf('saScore', 'SA Score', '#ef4444'), yAxisIndex: 1 }
    ]
  }
}

function renderChart(): void {
  if (!chartRef.value) return
  if (!chart.value) {
    chart.value = echarts.init(chartRef.value)
  }
  chart.value.setOption(buildOption(), true)
}

/* ------------------------------ 装载 ------------------------------ */

async function fetchRoundCandidates(roundId: number): Promise<DesignCandidate[]> {
  if (!roundId) return []
  try {
    const page = await designApi.fetchCandidates({
      projectId: store.projectId,
      roundId,
      page: 1,
      pageSize: 500
    })
    return page.list
  } catch {
    return []
  }
}

async function refresh(): Promise<void> {
  if (!baseRoundId.value || !targetRoundId.value) return
  loading.value = true
  try {
    const [base, target] = await Promise.all([
      fetchRoundCandidates(baseRoundId.value),
      fetchRoundCandidates(targetRoundId.value)
    ])
    baseCandidates.value = base
    targetCandidates.value = target
  } finally {
    loading.value = false
  }
}

watch([baseRoundId, targetRoundId], () => void refresh())

onMounted(() => {
  // 默认对比最近两个轮次
  const list = rounds.value
  const target = list[list.length - 1]
  const base = list[list.length - 2] ?? target
  baseRoundId.value = base?.id ?? 0
  targetRoundId.value = target?.id ?? 0

  renderChart()
  void refresh()

  // 趋势只依赖轮次快照，不依赖上面的候选拉取
  watch(rounds, () => renderChart())

  if (chartRef.value && typeof ResizeObserver !== 'undefined') {
    observer = new ResizeObserver(() => chart.value?.resize())
    observer.observe(chartRef.value)
  }
})

onBeforeUnmount(() => {
  observer?.disconnect()
  observer = null
  chart.value?.dispose()
  chart.value = null
})
</script>

<style lang="scss" scoped>
.rc {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  height: 100%;
  min-height: 0;
  padding: $spacing-md;
  overflow-y: auto;
  background: $color-canvas;
}

/* ---------------- 工具条 ---------------- */
.rc__toolbar {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-control;
  background: $color-surface;
}

.rc__pick {
  display: flex;
  align-items: center;
  gap: 6px;
}

.rc__pick-label {
  font-size: $font-size-xs;
  color: $color-text-faint;
}

.rc__select {
  padding: 4px $spacing-sm;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  font-size: $font-size-sm;
  color: $color-text;
}

.rc__arrow {
  color: $color-text-faint;
}

.rc__spacer {
  flex: 1;
}

.rc__hint {
  font-size: $font-size-xs;
  color: $color-text-faint;
}

/* ---------------- 分区 ---------------- */
.rc__section {
  padding: $spacing-md;
  border-radius: $radius-card;
  background: $color-surface;
}

.rc__title {
  margin: 0 0 $spacing-sm;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: $color-text-soft;
}

.rc__chart {
  width: 100%;
  height: 260px;
}

/* ---------------- 统计表 ---------------- */
.rc__table {
  width: 100%;
  border-collapse: collapse;
  font-size: $font-size-sm;
}

.rc__th {
  padding: 5px $spacing-sm;
  border-bottom: 1px solid $color-border;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $color-text-soft;
  text-align: left;

  &--num {
    text-align: right;
    width: 26%;
  }
}

.rc__td {
  padding: 4px $spacing-sm;
  border-bottom: 1px solid $color-border-soft;
  color: $color-text-soft;

  &--num {
    text-align: right;
    font-variant-numeric: tabular-nums;
  }
}

.rc__delta--good {
  color: $success-color;
}

.rc__delta--bad {
  color: $error-color;
}

/* ---------------- 流向 ---------------- */
.rc__flow {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  flex-wrap: wrap;
}

.rc__flow-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  min-width: 96px;
  padding: $spacing-sm;
  border-radius: $radius-control;
  background: $color-surface-alt;
}

.rc__flow-num {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $color-brand-strong;
  font-variant-numeric: tabular-nums;

  &--dim {
    color: $color-text-faint;
  }
}

.rc__flow-label {
  font-size: 11px;
  color: $color-text-faint;
}

.rc__flow-arrow {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.rc__flow-through {
  font-size: 10px;
  color: $color-text-faint;
}

.rc__flow-line {
  color: $color-brand;
  letter-spacing: -2px;
}

.rc__flow-gap {
  flex: 1;
}
</style>
