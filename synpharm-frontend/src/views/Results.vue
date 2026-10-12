<template>
  <div class="rs">
    <Sidebar />
    <main class="rs__main">
      <!-- ==================== 页头 ==================== -->
      <header class="rs__header">
        <div class="rs__header-main">
          <h1 class="rs__title">预测结果</h1>
          <p class="rs__subtitle">查看和管理全部预测结果</p>
        </div>
        <span class="rs__count">
          <template v-if="!hasFilter">共 {{ total }} 条</template>
          <template v-else>筛选 {{ filteredResults.length }} / 共 {{ total }} 条</template>
        </span>
      </header>

      <!-- ==================== 筛选区 ==================== -->
      <section class="rs__filters">
        <div class="rs__search">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <circle cx="11" cy="11" r="7" />
            <path d="m21 21-4.3-4.3" />
          </svg>
          <input v-model="filters.search" type="text" placeholder="搜索靶点名称、ID 或 SMILES" />
        </div>

        <select v-model="filters.type" class="rs__select">
          <option value="all">全部类型</option>
          <option value="dti">药物-靶点 (DTI)</option>
          <option value="ppi">蛋白-蛋白 (PPI)</option>
          <option value="ddi">药物-药物 (DDI)</option>
        </select>

        <select v-model="filters.confidence" class="rs__select">
          <option value="all">全部置信度</option>
          <option value="high">高</option>
          <option value="medium">中</option>
          <option value="low">低</option>
        </select>

        <select v-model="filters.sort" class="rs__select">
          <option value="date">按时间</option>
          <option value="affinity">按亲和力</option>
          <option value="confidence">按置信度</option>
        </select>
      </section>

      <!-- 后端未提供筛选参数，客户端仅拉取 pageSize=100 条；超出时明确提示范围 -->
      <div v-if="isPartial" class="rs__scope-hint">
        已加载前 {{ results.length }} 条（共 {{ total }} 条）；筛选与分页仅作用于已加载数据，更早结果需后端分页接口进一步支持。
      </div>

      <!-- ==================== 结果表格 ==================== -->
      <section class="rs__card">
        <div v-if="loading" class="rs__state">
          <span class="u-spinner" />
          <span>加载中…</span>
        </div>

        <div v-else-if="loadError" class="rs__state rs__state--error">
          <span>{{ loadError }}</span>
          <button class="rs__retry" @click="loadResults">重试</button>
        </div>

        <template v-else-if="filteredResults.length">
          <div class="rs__table-wrap">
            <table class="rs__table">
              <thead>
                <tr>
                  <th class="rs__th rs__th--target">靶点</th>
                  <th class="rs__th rs__th--type">类型</th>
                  <th class="rs__th rs__th--num">亲和力</th>
                  <th class="rs__th rs__th--conf">置信度</th>
                  <th class="rs__th rs__th--smiles">配体 SMILES</th>
                  <th class="rs__th rs__th--time">时间</th>
                  <th class="rs__th rs__th--ops">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="result in pagedResults" :key="String(result.id)" class="rs__row">
                  <td class="rs__td rs__td--target">
                    <span class="rs__target-name">{{ result.targetName || '—' }}</span>
                    <span class="rs__target-id">{{ result.targetId || '' }}</span>
                  </td>
                  <td class="rs__td rs__td--type">
                    <span class="rs__type" :class="`rs__type--${typeKey(result.algoType)}`">{{ typeLabel(result.algoType) }}</span>
                  </td>
                  <td class="rs__td rs__td--num">{{ affinityText(result) }}</td>
                  <td class="rs__td rs__td--conf">
                    <span class="rs__conf-num">{{ confidenceText(result) }}</span>
                    <StatusTag kind="confidence" :value="result.confidenceLevel" :score="result.confidenceScore" />
                  </td>
                  <td class="rs__td rs__td--smiles">
                    <span class="rs__smiles" :title="result.ligandSmiles || ''">{{ result.ligandSmiles || '—' }}</span>
                  </td>
                  <td class="rs__td rs__td--time">{{ formatDateTime(result.createdAt) }}</td>
                  <td class="rs__td rs__td--ops">
                    <button class="rs__op" @click="handleResultDetail(result)">详情</button>
                    <button class="rs__op rs__op--accent" @click="handleResult3D(result)">3D</button>
                    <button class="rs__op rs__op--danger" @click="handleResultDelete(result)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页 -->
          <div class="rs__pager">
            <span class="rs__pager-info">共 {{ filteredResults.length }} 条 · 每页</span>
            <select v-model="pageSize" class="rs__page-size">
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
            </select>
            <div class="rs__pager-nav">
              <button class="rs__page-btn" :disabled="page <= 1" @click="page--">上一页</button>
              <span class="rs__page-cur">{{ page }} / {{ totalPages }}</span>
              <button class="rs__page-btn" :disabled="page >= totalPages" @click="page++">下一页</button>
            </div>
          </div>
        </template>

        <EmptyState
          v-else
          icon="📊"
          :title="results.length ? '没有匹配的结果' : '暂无预测结果'"
          :description="results.length
            ? '当前筛选条件下没有结果，可调整筛选条件或清空筛选。'
            : '完成一次预测后，结果会展示在这里，并可进入 3D 可视化查看结构。'"
        >
          <button v-if="results.length" class="rs__retry" @click="clearFilters">清空筛选</button>
        </EmptyState>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { resultApi } from '@/api/predict'
import Sidebar from '@/components/Sidebar.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { formatDateTime } from '@/utils/display'
import type { PredictionResult } from '@/types'

const router = useRouter()

const results = ref<PredictionResult[]>([])
const total = ref(0)
const loading = ref(false)
const loadError = ref('')

/* ------------------------------- 数据装载 ------------------------------- */

async function loadResults(): Promise<void> {
  loading.value = true
  loadError.value = ''
  try {
    const page = await resultApi.getResultList(1, 100)
    results.value = page.list
    total.value = page.total
  } catch (error: unknown) {
    loadError.value = error instanceof Error ? error.message : '加载预测结果失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadResults)

/* ------------------------------- 筛选 / 排序 ------------------------------- */

const TYPE_LABELS: Record<string, string> = {
  dti: '药物-靶点',
  ppi: '蛋白-蛋白',
  ddi: '药物-药物'
}

const typeKey = (t?: string): string => (t ? t.toLowerCase() : 'none')
const typeLabel = (t?: string): string => (t ? TYPE_LABELS[t.toLowerCase()] || t : '—')

const filters = reactive({
  search: '',
  type: 'all',
  confidence: 'all',
  sort: 'date'
})

const hasFilter = computed(() =>
  filters.search.trim() !== '' || filters.type !== 'all' || filters.confidence !== 'all'
)

/** 后端 total 超过本次拉取条数时，明确提示客户端筛选范围受限 */
const isPartial = computed(() => total.value > results.value.length)

const filteredResults = computed(() => {
  let list = [...results.value]

  if (filters.type !== 'all') {
    list = list.filter((r) => (r.algoType || '').toLowerCase() === filters.type)
  }

  if (filters.confidence !== 'all') {
    list = list.filter((r) => r.confidenceLevel === filters.confidence)
  }

  if (filters.search) {
    const q = filters.search.toLowerCase()
    list = list.filter((r) =>
      (r.targetName || '').toLowerCase().includes(q) ||
      (r.targetId || '').toLowerCase().includes(q) ||
      (r.ligandSmiles || '').toLowerCase().includes(q)
    )
  }

  switch (filters.sort) {
    case 'date':
      list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      break
    case 'affinity':
      list.sort((a, b) => {
        if (a.bindingAffinity == null && b.bindingAffinity == null) return 0
        if (a.bindingAffinity == null) return 1
        if (b.bindingAffinity == null) return -1
        return a.bindingAffinity - b.bindingAffinity
      })
      break
    case 'confidence':
      list.sort((a, b) => (b.confidenceScore ?? 0) - (a.confidenceScore ?? 0))
      break
  }

  return list
})

/* ------------------------------- 分页（客户端） ------------------------------- */

const page = ref(1)
const pageSize = ref(10)

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filteredResults.value.length / pageSize.value))
)

const pagedResults = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredResults.value.slice(start, start + pageSize.value)
})

watch(
  [() => filters.search, () => filters.type, () => filters.confidence, () => filters.sort, pageSize],
  () => { page.value = 1 }
)

watch(totalPages, (tp) => {
  if (page.value > tp) page.value = tp
})

/* ------------------------------- 展示辅助 ------------------------------- */

/** 结合亲和力仅对 DTI 有意义（PPI/DDI 为空），其余显示占位符 */
function affinityText(r: PredictionResult): string {
  const v = r.bindingAffinity
  if (v == null || !Number.isFinite(v)) return '—'
  return `${v.toFixed(2)} kcal/mol`
}

/** 置信度百分比：缺失/非法时降级为占位符，避免显示 0% 或 NaN */
function confidenceText(r: PredictionResult): string {
  const s = r.confidenceScore
  if (s == null || !Number.isFinite(s)) return '—'
  return `${Math.round(s * 100)}%`
}

function clearFilters() {
  filters.search = ''
  filters.type = 'all'
  filters.confidence = 'all'
  filters.sort = 'date'
}

/* ------------------------------- 操作 ------------------------------- */

function handleResultDetail(result: PredictionResult) {
  router.push({ path: '/result/' + String(result.id) })
}

function handleResult3D(result: PredictionResult) {
  router.push({
    path: '/visualization',
    query: {
      id: String(result.id),
      targetName: result.targetName || '',
      targetId: result.targetId || ''
    }
  })
}

async function handleResultDelete(result: PredictionResult) {
  try {
    await ElMessageBox.confirm('确定删除该预测结果吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await resultApi.deleteResult(result.id)
    ElMessage.success('删除成功')
    await loadResults()
  } catch (error: unknown) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败')
  }
}
</script>

<style lang="scss" scoped>
/* ===================== 预测结果（数据工作台） ===================== */
.rs {
  display: flex;
  min-height: 100vh;
  background: $color-canvas;
  padding-top: $header-height;
}

// 宽度与居中由 styles/base.scss 的全局规则统一提供（1200px 窄幅）
.rs__main {
  flex: 1;
  padding: $spacing-lg $spacing-xl $spacing-2xl;
}

/* ---------------- 页头 ---------------- */
.rs__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: $spacing-lg;
  padding: $spacing-md 0 $spacing-xl;
}

.rs__header-main {
  position: relative;
  padding-left: $spacing-md;
  min-width: 0;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 5px;
    bottom: 5px;
    width: 3px;
    border-radius: $radius-pill;
    background: linear-gradient(180deg, $app-accent, $app-brand);
  }
}

.rs__title {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  line-height: 1.3;
  color: $color-text;
}

.rs__subtitle {
  margin-top: 4px;
  font-size: $font-size-sm;
  color: $color-text-faint;
}

.rs__count {
  flex-shrink: 0;
  font-size: $font-size-sm;
  color: $color-text-soft;
  background: $color-surface-sunken;
  padding: $spacing-xs $spacing-md;
  border-radius: $radius-pill;
}

/* ---------------- 筛选区 ---------------- */
.rs__filters {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  flex-wrap: wrap;
  margin-bottom: $spacing-lg;
}

.rs__search {
  position: relative;
  flex: 1;
  min-width: 220px;

  svg {
    position: absolute;
    left: $spacing-md;
    top: 50%;
    transform: translateY(-50%);
    width: 16px;
    height: 16px;
    color: $color-text-faint;
    pointer-events: none;
  }

  input {
    width: 100%;
    padding: $spacing-sm $spacing-md $spacing-sm 38px;
    border: 1px solid $color-border;
    border-radius: $radius-control;
    font-size: $font-size-base;
    color: $color-text;
    background: $color-surface;
    font-family: inherit;

    &::placeholder { color: $text-light; }

    &:focus {
      outline: none;
      border-color: $app-accent;
      box-shadow: 0 0 0 3px rgba(8, 145, 178, 0.12);
    }
  }
}

.rs__select {
  padding: $spacing-sm $spacing-md;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  font-size: $font-size-base;
  color: $color-text;
  background: $color-surface;
  font-family: inherit;
  cursor: pointer;

  &:focus { outline: none; border-color: $app-accent; }
}

.rs__scope-hint {
  margin-bottom: $spacing-lg;
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-control;
  font-size: $font-size-xs;
  line-height: 1.5;
  color: $app-accent-strong;
  background: $app-accent-soft;
}

/* ---------------- 卡片 / 表格 ---------------- */
.rs__card {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  overflow: hidden;
}

.rs__table-wrap {
  overflow-x: auto;
}

.rs__table {
  width: 100%;
  border-collapse: collapse;
  font-size: $font-size-sm;
}

.rs__th {
  padding: $spacing-sm $spacing-md;
  text-align: left;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $color-text-faint;
  letter-spacing: 0.03em;
  white-space: nowrap;
  background: $color-surface-alt;
  border-bottom: 1px solid $color-border;
}

.rs__row {
  border-bottom: 1px solid $color-border-soft;
  transition: background $transition-fast;

  &:hover {
    background: $app-accent-softer;
  }

  &:last-child { border-bottom: none; }
}

.rs__td {
  padding: $spacing-sm $spacing-md;
  vertical-align: middle;
  color: $color-text;
}

/* 靶点列 */
.rs__td--target {
  min-width: 180px;

  .rs__target-name {
    display: block;
    font-weight: $font-weight-medium;
    color: $color-text;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .rs__target-id {
    display: block;
    font-size: $font-size-xs;
    color: $color-text-faint;
    font-family: monospace;
  }
}

/* 类型徽标 */
.rs__type {
  display: inline-block;
  padding: 2px 10px;
  border-radius: $radius-pill;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
  white-space: nowrap;
  background: $app-accent-soft;
  color: $app-accent-strong;

  &--ppi { background: rgba(16, 185, 129, 0.12); color: #0e9f6e; }
  &--ddi { background: rgba(139, 92, 246, 0.12); color: #7c3aed; }
  &--none { background: $color-surface-sunken; color: $color-text-faint; }
}

/* 数值列（右对齐 + 等宽数字） */
.rs__th--num { text-align: right; }
.rs__td--num {
  text-align: right;
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
  color: $color-text-soft;
}

/* 置信度列 */
.rs__conf-num {
  display: inline-block;
  min-width: 40px;
  font-variant-numeric: tabular-nums;
  color: $color-text;
  margin-right: $spacing-sm;
}

/* SMILES 列 */
.rs__td--smiles {
  max-width: 260px;

  .rs__smiles {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-family: monospace;
    font-size: $font-size-xs;
    color: $color-text-soft;
  }
}

/* 时间列 */
.rs__td--time {
  white-space: nowrap;
  color: $color-text-faint;
  font-variant-numeric: tabular-nums;
}

/* 操作列 */
.rs__td--ops {
  white-space: nowrap;
  text-align: right;
}

.rs__op {
  padding: 4px $spacing-sm;
  border: 1px solid $color-border;
  border-radius: $border-radius-sm;
  background: transparent;
  color: $color-text-soft;
  font-size: $font-size-xs;
  cursor: pointer;
  transition: all $transition-fast;

  & + & { margin-left: $spacing-xs; }

  &:hover {
    border-color: $app-accent;
    color: $app-accent-strong;
  }

  &--accent {
    border-color: $app-accent;
    color: $app-accent-strong;
    &:hover { background: $app-accent-soft; }
  }

  &--danger {
    border-color: transparent;
    color: $color-text-faint;
    &:hover { color: $error-color; border-color: rgba(239, 68, 68, 0.3); }
  }
}

/* ---------------- 分页 ---------------- */
.rs__pager {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md $spacing-lg;
  border-top: 1px solid $color-border-soft;
  font-size: $font-size-sm;
  color: $color-text-faint;
}

.rs__pager-info {
  white-space: nowrap;
}

.rs__page-size {
  padding: 4px $spacing-sm;
  border: 1px solid $color-border;
  border-radius: $border-radius-sm;
  background: $color-surface;
  color: $color-text;
  font-size: $font-size-sm;
  cursor: pointer;
}

.rs__pager-nav {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.rs__page-cur {
  font-variant-numeric: tabular-nums;
  color: $color-text-soft;
}

.rs__page-btn {
  padding: 4px $spacing-md;
  border: 1px solid $color-border;
  border-radius: $border-radius-sm;
  background: $color-surface;
  color: $color-text-soft;
  font-size: $font-size-sm;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover:not(:disabled) {
    border-color: $app-accent;
    color: $app-accent-strong;
  }

  &:disabled {
    opacity: 0.45;
    cursor: not-allowed;
  }
}

/* ---------------- 状态 ---------------- */
.rs__state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  padding: $spacing-2xl;
  color: $color-text-faint;
  font-size: $font-size-base;

  &--error {
    flex-direction: column;
    color: $error-color;
  }
}

.rs__retry {
  padding: $spacing-xs $spacing-lg;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  color: $color-text-soft;
  font-size: $font-size-sm;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover { border-color: $app-accent; color: $app-accent-strong; }
}

/* ---------------- 响应式 ---------------- */
@media (max-width: 900px) {
  .rs__filters {
    flex-direction: column;
    align-items: stretch;

    .rs__search { min-width: 0; }
    .rs__select { width: 100%; }
  }

  .rs__header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
