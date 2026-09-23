<template>
  <div class="ide">
    <Sidebar />

    <div class="ide__body">
      <!-- ==================== 左栏：项目 / 靶点 / 约束 / 轮次 ==================== -->
      <aside class="ide__left">
        <div v-if="store.loading.initiating" class="ide__pane-loading">加载项目数据…</div>

        <template v-else>
          <div class="ide__pane-title">项目</div>
          <div class="ide__project">
            <span class="ide__project-icon">🧬</span>
            <div class="ide__project-info">
              <span class="ide__project-name">{{ store.project?.name ?? '—' }}</span>
              <span class="ide__project-meta">
                {{ store.project?.projectNo }} · {{ projectStatusLabel }}
              </span>
            </div>
          </div>

          <div class="ide__pane-title">靶点（{{ store.targets.length }}）</div>
          <div class="ide__list">
            <div v-for="target in store.targets" :key="target.id" class="ide__target">
              <span class="ide__role" :class="`ide__role--${target.role.toLowerCase()}`">
                {{ roleLabel(target.role) }}
              </span>
              <div class="ide__target-info">
                <span class="ide__target-name">{{ target.chineseName || target.name }}</span>
                <span class="ide__target-meta">
                  {{ target.geneName }} · {{ target.uniprotId }} · {{ target.sequenceLength }} aa
                </span>
              </div>
            </div>
          </div>

          <div class="ide__pane-title">约束集</div>
          <div class="ide__constraint">
            <div class="ide__constraint-head">
              <span class="ide__constraint-name">{{ store.constraintSet?.name ?? '—' }}</span>
              <span class="ide__constraint-ver">v{{ store.constraintSet?.version ?? 0 }}</span>
            </div>
            <div class="ide__kv">
              <span>硬规则</span><span class="ide__kv-num">{{ hardRuleCount }}</span>
              <span>软规则</span><span class="ide__kv-num">{{ softRuleCount }}</span>
              <span>结合口袋</span><span class="ide__kv-num">{{ store.pockets.length }}</span>
            </div>
          </div>

          <div class="ide__pane-title">轮次</div>
          <div class="ide__rounds">
            <button
              v-for="round in store.rounds"
              :key="round.id"
              type="button"
              class="ide__round"
              :class="{ 'ide__round--active': round.id === store.query.roundId }"
              @click="store.setRound(round.id)"
            >
              <span class="ide__round-dot" :class="`ide__round-dot--${round.status.toLowerCase()}`" />
              <span class="ide__round-name">{{ round.name }}</span>
              <span class="ide__round-count">{{ round.candidateCount }}</span>
            </button>
          </div>

          <div class="ide__pane-title">生成任务</div>
          <div class="ide__list">
            <div v-for="run in store.runs" :key="run.id" class="ide__run">
              <span class="ide__run-name">{{ run.strategy }} {{ run.strategyLabel }}</span>
              <span class="ide__run-state">{{ runStateLabel(run.status, run.progress) }}</span>
            </div>
          </div>
        </template>
      </aside>

      <!-- ==================== 中栏：主工作面 ==================== -->
      <main class="ide__main">
        <div class="ide__tabs">
          <button
            v-for="tab in TABS"
            :key="tab.key"
            type="button"
            class="ide__tab"
            :class="{ 'ide__tab--active': store.activeTab === tab.key }"
            @click="store.setTab(tab.key)"
          >
            <span class="ide__tab-icon">{{ tab.icon }}</span>
            <span>{{ tab.label }}</span>
          </button>

          <div class="ide__tabs-spacer" />

          <template v-if="store.activeTab === 'canvas'">
            <button
              type="button"
              class="ide__action"
              :disabled="!editorReady || !store.selectedCandidate"
              @click="loadSelectedToCanvas"
            >
              载入选中候选
            </button>
            <button
              type="button"
              class="ide__action ide__action--primary"
              :disabled="!editorReady || saving"
              @click="saveAsCandidate"
            >
              {{ saving ? '保存中…' : '保存为新候选' }}
            </button>
          </template>
        </div>

        <div class="ide__stage">
          <!-- 画布 -->
          <div v-show="store.activeTab === 'canvas'" class="ide__stage-slot">
            <MoleculeEditor ref="editorRef" @ready="onEditorReady" @error="onEditorError" />
          </div>

          <!-- 候选 -->
          <div v-if="store.activeTab === 'candidates'" class="ide__stage-slot">
            <CandidateGrid
              :rows="store.candidates"
              :total="store.candidateTotal"
              :page="store.query.page"
              :page-size="store.query.pageSize"
              :total-pages="store.totalPages"
              :loading="store.loading.candidates"
              :selected-id="store.selectedMolId"
              :sort-by="store.query.sortBy ?? 'compositeScore'"
              :sort-order="store.query.sortOrder ?? 'desc'"
              :keyword="store.query.keyword ?? ''"
              @select="store.selectCandidate"
              @query="store.setQuery"
            />
          </div>

          <!-- 对比 / 口袋：待实现 -->
          <div v-if="store.activeTab !== 'canvas' && store.activeTab !== 'candidates'" class="ide__placeholder">
            <span class="ide__placeholder-icon">{{ activeTabMeta.icon }}</span>
            <span class="ide__placeholder-title">{{ activeTabMeta.label }}</span>
            <span class="ide__placeholder-desc">{{ activeTabMeta.desc }}</span>
          </div>
        </div>

        <div class="ide__statusbar">
          <span class="ide__status-item">
            <span class="ide__status-dot" :class="editorReady ? 'ide__status-dot--ok' : 'ide__status-dot--idle'" />
            {{ editorStateText }}
          </span>
          <span class="ide__status-item">
            当前轮次 <b>{{ store.currentRound?.name ?? '—' }}</b>
            · {{ store.currentRoundCandidateCount }} 个候选
          </span>
          <span class="ide__status-item">
            选中 <b>{{ store.selectedCandidate?.displayName ?? '—' }}</b>
          </span>
          <span class="ide__status-spacer" />
          <span v-if="store.degraded" class="ide__status-item ide__status-item--warn">
            ⚠ 部分计算引擎未启用，相关指标已跳过
          </span>
          <span class="ide__status-item">{{ dataSourceLabel }}</span>
        </div>
      </main>

      <!-- ==================== 右栏：约束与指标 ==================== -->
      <aside class="ide__right">
        <div class="ide__pane-title">
          设计约束
          <span v-if="store.violations.length" class="ide__pane-badge">
            {{ store.hardViolationCount }} 硬 / {{ store.softViolationCount }} 软 违规
          </span>
        </div>

        <div class="ide__list">
          <div v-for="row in store.violations" :key="row.ruleCode" class="ide__check">
            <span
              class="ide__check-dot"
              :class="row.passed ? 'ide__check-dot--pass' : `ide__check-dot--${row.severity.toLowerCase()}`"
            />
            <span class="ide__check-label" :title="`${row.ruleCode}｜期望 ${row.expected}`">
              {{ row.ruleName }}
            </span>
            <span class="ide__check-value">{{ row.actual }}</span>
          </div>

          <p v-if="!store.violations.length" class="ide__hint ide__hint--inline">
            选中一个候选后显示逐条约束判定结果
          </p>
        </div>

        <div class="ide__pane-title">
          评估指标
          <span v-if="store.metrics" class="ide__pane-badge ide__pane-badge--score">
            综合 {{ store.metrics.compositeScore.toFixed(1) }}
          </span>
        </div>

        <div class="ide__list">
          <div
            v-for="row in store.metricRows"
            :key="row.definition.metricCode"
            class="ide__metric"
            :class="{ 'ide__metric--off': !row.definition.enabled }"
          >
            <span class="ide__check-label" :title="row.definition.metricCode">
              {{ row.definition.metricName }}
            </span>
            <span class="ide__metric-value" :class="metricToneClass(row.grade)">
              {{ formatMetricValue(row) }}
            </span>
          </div>
        </div>

        <p class="ide__hint">
          数据经 <code>designApi</code> 获取；后端就绪后置
          <code>VITE_DESIGN_MOCK=false</code> 即切换到真实接口。
        </p>
      </aside>
    </div>

    <!-- ==================== 底部：时间线 ==================== -->
    <footer class="ide__timeline">
      <span class="ide__timeline-title">时间线</span>
      <div class="ide__timeline-track">
        <button
          v-for="round in store.rounds"
          :key="round.id"
          type="button"
          class="ide__timeline-node"
          :class="{
            'ide__timeline-node--active': round.id === store.query.roundId,
            'ide__timeline-node--closed': round.status === 'CLOSED'
          }"
          :title="round.name"
          @click="store.setRound(round.id)"
        >
          R{{ round.roundNo }}
        </button>
      </div>
      <span class="ide__timeline-gap" />
      <span v-if="store.currentRound?.metricSnapshot" class="ide__timeline-stats">
        通过率 {{ store.currentRound.metricSnapshot.passRate }}% ·
        均值：综合 {{ store.currentRound.metricSnapshot.compositeScore }} ·
        QED {{ store.currentRound.metricSnapshot.qed }} ·
        SA {{ store.currentRound.metricSnapshot.saScore }}
      </span>
      <span v-else class="ide__timeline-note">谱系树 / 操作日志待实现</span>
    </footer>

    <!-- 全局错误条 -->
    <div v-if="store.error" class="ide__error" @click="store.error = ''">
      {{ store.error }} <span class="ide__error-close">×</span>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 干实验设计工作台（IDE）。
 *
 * 数据全部来自 `useDesignStore`（其背后是 `designApi`，可在 mock 与真实后端间切换），
 * 本文件不包含任何写死的业务数据。三栏分工：
 *   左栏 项目/靶点/约束/轮次  —  store.tree
 *   中栏 画布 / 候选 / 对比 / 口袋 —  store.candidates + 编辑器
 *   右栏 约束判定 + 指标      —  store.violations + store.metricRows
 */
import { computed, onMounted, ref, watch } from 'vue'
import Sidebar from '@/components/Sidebar.vue'
import MoleculeEditor from '@/components/design/MoleculeEditor.vue'
import CandidateGrid from '@/components/design/CandidateGrid.vue'
import { useDesignStore } from '@/stores/design'
import { isDesignMockEnabled } from '@/api/design'
import type { DesignProjectStatus, GenerationRunStatus, ProjectTargetRole, WorkbenchTab } from '@/types/design'

const store = useDesignStore()

const editorRef = ref<InstanceType<typeof MoleculeEditor> | null>(null)
const editorReady = ref(false)
const editorErrorText = ref('')
const saving = ref(false)

const TABS: Array<{ key: WorkbenchTab; icon: string; label: string }> = [
  { key: 'canvas', icon: '✏️', label: '画布' },
  { key: 'candidates', icon: '🧫', label: '候选' },
  { key: 'diff', icon: '🔀', label: '对比' },
  { key: 'pocket', icon: '🧊', label: '口袋' }
]

const activeTabMeta = computed(() => {
  const found = TABS.find((t) => t.key === store.activeTab)
  return {
    icon: found?.icon ?? '',
    label: found?.label ?? '',
    desc: `${found?.label ?? ''}视图将在后续步骤实现（见 docs/modules/design/07-工作台与交互设计.md）`
  }
})

/* ------------------------------- 文案映射 ------------------------------- */

const PROJECT_STATUS: Record<DesignProjectStatus, string> = {
  DRAFT: '草稿',
  READY: '就绪',
  ACTIVE: '进行中',
  COMPLETED: '已结项',
  CANCELLED: '已取消',
  ARCHIVED: '已归档'
}

const ROLE_LABEL: Record<ProjectTargetRole, string> = {
  WILD_TYPE: '野生型',
  MUTANT: '突变体',
  ANTI_TARGET: '反靶点',
  OFF_TARGET: '脱靶'
}

const projectStatusLabel = computed(
  () => (store.project ? PROJECT_STATUS[store.project.status] : '—')
)

const roleLabel = (role: ProjectTargetRole): string => ROLE_LABEL[role]

const runStateLabel = (status: GenerationRunStatus, progress: number): string => {
  if (status === 'RUNNING') return `${progress}%`
  const map: Record<GenerationRunStatus, string> = {
    PENDING: '排队中',
    RUNNING: '运行中',
    SUCCESS: '已完成',
    PARTIAL: '部分成功',
    FAILED: '失败',
    CANCELLED: '已取消'
  }
  return map[status]
}

/* ------------------------------- 派生统计 ------------------------------- */

const hardRuleCount = computed(() => store.constraintRules.filter((r) => r.severity === 'HARD').length)
const softRuleCount = computed(() => store.constraintRules.filter((r) => r.severity === 'SOFT').length)

const editorStateText = computed(() => {
  if (editorReady.value) return '编辑器就绪'
  if (editorErrorText.value) return '编辑器不可用'
  return '编辑器加载中…'
})

const dataSourceLabel = computed(() =>
  isDesignMockEnabled ? '数据来源：内置演示数据' : '数据来源：后端接口'
)

/** 指标展示格式：数值型按单位决定小数位，布尔/文本型直接展示文本 */
function formatMetricValue(row: {
  definition: { metricCode: string; unit?: string; valueType: string }
  value?: number
  text?: string
}): string {
  if (row.text) return row.text
  if (row.value === undefined) return '—'
  const decimals = row.definition.valueType === 'NUMERIC' && row.definition.unit === 'Da' ? 1 : 2
  const unit = row.definition.unit ? ` ${row.definition.unit}` : ''
  return `${row.value.toFixed(decimals)}${unit}`
}

function metricToneClass(grade?: string): string {
  if (!grade) return ''
  const map: Record<string, string> = {
    EXCELLENT: 'ide__metric-value--good',
    GOOD: 'ide__metric-value--ok',
    FAIR: 'ide__metric-value--fair',
    POOR: 'ide__metric-value--poor'
  }
  return map[grade] ?? ''
}

/* ------------------------------- 编辑器联动 ------------------------------- */

function onEditorReady(): void {
  editorReady.value = true
  editorErrorText.value = ''
  loadSelectedToCanvas()
}

function onEditorError(message: string): void {
  editorErrorText.value = message
}

function loadSelectedToCanvas(): void {
  const smiles = store.selectedCandidate?.smiles
  if (smiles) editorRef.value?.setMolecule(smiles)
}

/** 选中候选后自动载入画布，形成"左侧点选 → 中间查看 → 右侧指标"的联动 */
watch(
  () => store.selectedCandidate?.smiles,
  (smiles) => {
    if (smiles && editorReady.value) editorRef.value?.setMolecule(smiles)
  }
)

async function saveAsCandidate(): Promise<void> {
  if (!editorRef.value) return
  saving.value = true
  try {
    const smiles = await editorRef.value.getSmiles()
    if (smiles) await store.saveEdited(smiles)
  } catch (err) {
    store.error = err instanceof Error ? err.message : '保存失败'
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  void store.init()
})
</script>

<style lang="scss" scoped>
/* 顶栏（Sidebar）为 fixed 定位，外层需留出等高上边距 */
.ide {
  height: 100vh;
  padding-top: $header-height;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: $color-canvas;
}

.ide__body {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 244px minmax(0, 1fr) 276px;
}

/* ---------------- 通用面板 ---------------- */
.ide__left,
.ide__right {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-md;
  overflow-y: auto;
  background: $color-surface;
}

.ide__left {
  border-right: 1px solid $color-border;
}

.ide__right {
  border-left: 1px solid $color-border;
}

.ide__pane-loading {
  padding: $spacing-lg 0;
  font-size: $font-size-sm;
  color: $color-text-faint;
  text-align: center;
}

.ide__pane-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: $spacing-xs;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: $color-text-faint;
}

.ide__pane-badge {
  margin-left: auto;
  padding: 0 5px;
  border-radius: $radius-pill;
  background: $color-surface-sunken;
  font-size: 10px;
  font-weight: $font-weight-normal;
  letter-spacing: 0;
  text-transform: none;
  color: $color-text-soft;

  &--score {
    background: $color-brand-soft;
    color: $color-brand-strong;
  }
}

.ide__list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ide__hint {
  margin-top: auto;
  padding-top: $spacing-md;
  font-size: 11px;
  line-height: 1.6;
  color: $color-text-faint;

  code {
    padding: 1px 4px;
    border-radius: 4px;
    background: $color-surface-sunken;
    font-size: 10px;
  }

  &--inline {
    margin: 0;
    padding: $spacing-sm 0;
  }
}

/* ---------------- 左栏 ---------------- */
.ide__project {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm;
  border-radius: $radius-control;
  background: $color-surface-alt;
}

.ide__project-icon {
  font-size: 18px;
}

.ide__project-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.ide__project-name {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ide__project-meta {
  font-size: 11px;
  color: $color-text-faint;
}

.ide__target {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: 4px $spacing-sm;
}

.ide__role {
  flex-shrink: 0;
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: $font-weight-medium;

  &--wild_type {
    background: rgba($accent-color, 0.14);
    color: $color-brand-strong;
  }

  &--mutant {
    background: rgba($warning-color, 0.16);
    color: $warning-color;
  }

  &--anti_target,
  &--off_target {
    background: rgba($error-color, 0.12);
    color: $error-color;
  }
}

.ide__target-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.ide__target-name {
  font-size: $font-size-sm;
  color: $color-text-soft;
}

.ide__target-meta {
  font-size: 10px;
  color: $color-text-faint;
}

.ide__constraint {
  padding: $spacing-sm;
  border-radius: $radius-control;
  background: $color-surface-alt;
}

.ide__constraint-head {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 6px;
}

.ide__constraint-name {
  flex: 1;
  min-width: 0;
  font-size: $font-size-sm;
  color: $color-text-soft;

  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ide__constraint-ver {
  font-size: 10px;
  color: $color-text-faint;
}

.ide__kv {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 2px 8px;
  font-size: 11px;
  color: $color-text-faint;
}

.ide__kv-num {
  font-variant-numeric: tabular-nums;
  color: $color-text-soft;
}

.ide__rounds {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ide__round {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: 5px $spacing-sm;
  border: none;
  border-radius: $radius-control;
  background: transparent;
  font-size: $font-size-sm;
  color: $color-text-soft;
  text-align: left;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover {
    background: $color-surface-alt;
  }

  &--active {
    background: $color-brand-soft;
    color: $color-brand-strong;
    font-weight: $font-weight-medium;
  }
}

.ide__round-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: $text-light;

  &--closed {
    background: $success-color;
  }

  &--editing,
  &--generating,
  &--evaluating {
    background: $accent-color;
  }

  &--reviewing {
    background: $warning-color;
  }
}

.ide__round-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ide__round-count {
  font-size: 11px;
  font-variant-numeric: tabular-nums;
  color: $color-text-faint;
}

.ide__run {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: 4px $spacing-sm;
  font-size: 11px;
}

.ide__run-name {
  flex: 1;
  min-width: 0;
  color: $color-text-soft;
}

.ide__run-state {
  color: $color-text-faint;
  font-variant-numeric: tabular-nums;
}

/* ---------------- 中栏 ---------------- */
.ide__main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
}

.ide__tabs {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: $spacing-sm $spacing-md;
  border-bottom: 1px solid $color-border;
  background: $color-surface;
}

.ide__tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px $spacing-sm;
  border: none;
  border-radius: $radius-control;
  background: transparent;
  font-size: $font-size-sm;
  color: $color-text-soft;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover {
    background: $color-surface-alt;
  }

  &--active {
    background: $color-brand-soft;
    color: $color-brand-strong;
    font-weight: $font-weight-medium;
  }
}

.ide__tab-icon {
  font-size: 12px;
}

.ide__tabs-spacer {
  flex: 1;
}

.ide__action {
  padding: 5px $spacing-sm;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  font-size: $font-size-xs;
  color: $color-text-soft;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover:not(:disabled) {
    border-color: $color-brand;
    color: $color-brand-strong;
  }

  &:disabled {
    opacity: 0.45;
    cursor: not-allowed;
  }

  & + & {
    margin-left: $spacing-sm;
  }

  &--primary:not(:disabled) {
    border-color: $color-brand;
    background: $color-brand-soft;
    color: $color-brand-strong;
  }
}

.ide__stage {
  position: relative;
  flex: 1;
  min-height: 0;
  background: $color-canvas;
}

.ide__stage-slot {
  position: absolute;
  inset: 0;
}

.ide__placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
}

.ide__placeholder-icon {
  font-size: 32px;
  opacity: 0.5;
}

.ide__placeholder-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-medium;
  color: $color-text-soft;
}

.ide__placeholder-desc {
  max-width: 460px;
  font-size: $font-size-sm;
  color: $color-text-faint;
  text-align: center;
  line-height: 1.7;
}

.ide__statusbar {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: 5px $spacing-md;
  border-top: 1px solid $color-border;
  background: $color-surface;
  font-size: 11px;
  color: $color-text-soft;
}

.ide__status-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  b {
    font-weight: $font-weight-medium;
    color: $color-text;
  }

  &--warn {
    color: $warning-color;
  }
}

.ide__status-spacer {
  flex: 1;
}

.ide__status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;

  &--ok {
    background: $success-color;
  }

  &--idle {
    background: $text-light;
  }
}

/* ---------------- 右栏 ---------------- */
.ide__check {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: 3px $spacing-sm;
  font-size: 12px;
  color: $color-text-soft;
}

.ide__check-dot {
  flex-shrink: 0;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: $text-light;

  &--pass {
    background: $success-color;
  }

  &--hard {
    background: $error-color;
  }

  &--soft {
    background: $warning-color;
  }
}

.ide__check-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ide__check-value {
  font-size: 11px;
  font-variant-numeric: tabular-nums;
  color: $color-text-faint;
}

.ide__metric {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: 3px $spacing-sm;
  font-size: 12px;
  color: $color-text-soft;

  &--off {
    opacity: 0.5;
  }
}

.ide__metric-value {
  font-size: 11px;
  font-variant-numeric: tabular-nums;
  color: $color-text-faint;

  &--good {
    color: $success-color;
  }

  &--ok {
    color: $color-brand-strong;
  }

  &--fair {
    color: $warning-color;
  }

  &--poor {
    color: $error-color;
  }
}

/* ---------------- 底部时间线 ---------------- */
.ide__timeline {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  height: 42px;
  padding: 0 $spacing-md;
  border-top: 1px solid $color-border;
  background: $color-surface;
}

.ide__timeline-title {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: $color-text-faint;
}

.ide__timeline-track {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.ide__timeline-node {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border: none;
  border-radius: 50%;
  background: $color-surface-sunken;
  font-size: 11px;
  font-weight: $font-weight-medium;
  color: $color-text-faint;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    background: $color-brand-soft;
    color: $color-brand-strong;
  }

  &--closed {
    background: rgba($success-color, 0.16);
    color: $success-color;
  }

  &--active {
    background: $color-brand;
    color: #fff;
  }
}

.ide__timeline-gap {
  flex: 1;
}

.ide__timeline-stats,
.ide__timeline-note {
  font-size: 11px;
  color: $color-text-faint;
  font-variant-numeric: tabular-nums;
}

/* ---------------- 错误条 ---------------- */
.ide__error {
  position: fixed;
  right: $spacing-lg;
  bottom: 56px;
  z-index: $z-toast;
  max-width: 420px;
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-control;
  background: rgba($error-color, 0.95);
  color: #fff;
  font-size: $font-size-sm;
  cursor: pointer;
  box-shadow: $shadow-lg;
}

.ide__error-close {
  margin-left: $spacing-sm;
  opacity: 0.8;
}
</style>
