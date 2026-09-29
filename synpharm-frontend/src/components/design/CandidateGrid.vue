<template>
  <div class="cg">
    <!-- 工具条：搜索 / 筛选 / 排序 -->
    <div class="cg__toolbar">
      <input
        v-model="keyword"
        class="cg__search"
        type="search"
        placeholder="搜索 SMILES / InChIKey / 编号"
        @keyup.enter="applyKeyword"
      />

      <select v-model="originType" class="cg__select" @change="applyFilters">
        <option value="">全部来源</option>
        <option value="GENERATED">生成</option>
        <option value="EDITED">编辑衍生</option>
        <option value="IMPORTED">导入</option>
      </select>

      <select v-model="reviewStatus" class="cg__select" @change="applyFilters">
        <option value="">全部状态</option>
        <option value="TOP">优选</option>
        <option value="QUALIFIED">合格</option>
        <option value="NEW">待评</option>
        <option value="REJECTED">已淘汰</option>
      </select>

      <span class="cg__spacer" />

      <span class="cg__count">
        共 {{ total }} 条<template v-if="selectedCount"> · 已选 {{ selectedCount }}</template>
      </span>
    </div>

    <!-- 表格 -->
    <div class="cg__body">
      <div v-if="loading" class="cg__state">
        <span v-for="n in 6" :key="n" class="cg__skeleton" />
      </div>

      <div v-else-if="!rows.length" class="cg__state cg__state--empty">
        <span class="cg__state-icon">🧫</span>
        <span class="cg__state-title">当前条件下没有候选分子</span>
        <span class="cg__state-desc">换一个轮次或放宽筛选条件，也可以先生成一批候选。</span>
      </div>

      <table v-else class="cg__table">
        <thead>
          <tr>
            <th class="cg__th cg__th--idx">#</th>
            <th class="cg__th cg__th--name">编号</th>
            <th class="cg__th cg__th--smiles">SMILES</th>
            <th
              v-for="col in columns"
              :key="col.key"
              class="cg__th cg__th--num"
              :class="{ 'cg__th--sorted': sortBy === col.key }"
              :title="col.tip"
              @click="toggleSort(col.key)"
            >
              {{ col.label }}
              <span v-if="sortBy === col.key" class="cg__sort-mark">
                {{ sortOrder === 'desc' ? '▼' : '▲' }}
              </span>
            </th>
            <th class="cg__th cg__th--status">状态</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(row, index) in rows"
            :key="row.id"
            class="cg__row"
            :class="{ 'cg__row--active': row.id === selectedId }"
            @click="emit('select', row.id)"
          >
            <td class="cg__td cg__td--idx">{{ (page - 1) * pageSize + index + 1 }}</td>
            <td class="cg__td cg__td--name">
              {{ row.displayName || '—' }}
              <span v-if="row.originType === 'EDITED'" class="cg__tag">衍生</span>
            </td>
            <td class="cg__td cg__td--smiles" :title="row.smiles">{{ row.smiles }}</td>
            <td class="cg__td cg__td--num">{{ row.mw.toFixed(1) }}</td>
            <td class="cg__td cg__td--num">{{ row.logp.toFixed(2) }}</td>
            <td class="cg__td cg__td--num">{{ row.qed.toFixed(3) }}</td>
            <td class="cg__td cg__td--num">{{ row.saScore.toFixed(2) }}</td>
            <td class="cg__td cg__td--num cg__td--score">{{ row.compositeScore.toFixed(1) }}</td>
            <td class="cg__td cg__td--status">
              <span class="cg__badge" :class="`cg__badge--${statusTone(row.reviewStatus)}`">
                {{ statusLabel(row.reviewStatus) }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="cg__footer">
      <span class="cg__page-size">每页
        <select v-model.number="pageSize" class="cg__select cg__select--sm" @change="changePageSize">
          <option :value="20">20</option>
          <option :value="50">50</option>
          <option :value="100">100</option>
        </select>
        条
      </span>
      <span class="cg__spacer" />
      <button class="cg__page-btn" :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
      <span class="cg__page-info">{{ page }} / {{ totalPages }}</span>
      <button class="cg__page-btn" :disabled="page >= totalPages" @click="goPage(page + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 候选分子列表（中栏「候选」页签）。
 *
 * 只负责展示与交互，不直接取数：筛选/排序/分页通过 props 回传由 store 统一查询，
 * 因此换成真实后端时本组件无需改动。
 */
import { computed, ref, watch } from 'vue'
import type { CandidateQuery, DesignCandidate, MoleculeOriginType, ReviewStatus } from '@/types/design'

const props = defineProps<{
  rows: DesignCandidate[]
  total: number
  page: number
  pageSize: number
  totalPages: number
  loading: boolean
  selectedId: number | null
  sortBy: string
  sortOrder: 'asc' | 'desc'
  keyword: string
  originType?: MoleculeOriginType
  reviewStatus?: ReviewStatus
}>()

const emit = defineEmits<{
  (e: 'select', moleculeId: number): void
  (e: 'query', patch: Partial<CandidateQuery>): void
}>()

/** 可排序的数值列（与后端冗余列对应，避免每次关联查询） */
const columns = [
  { key: 'mw', label: 'MW', tip: '分子量' },
  { key: 'logp', label: 'LogP', tip: '脂水分配系数' },
  { key: 'qed', label: 'QED', tip: '类药性' },
  { key: 'saScore', label: 'SA', tip: '合成可及性，越小越好' },
  { key: 'compositeScore', label: '综合得分', tip: '按权重快照加权后再扣分' }
] as const

const keyword = ref(props.keyword)
const originType = ref<string>(props.originType ?? '')
const reviewStatus = ref<string>(props.reviewStatus ?? '')
const pageSize = ref(props.pageSize)

// props → 本地控件回填（例如从服务端恢复工作台状态时）
watch(
  () => props.keyword,
  (value) => {
    keyword.value = value
  }
)

const selectedCount = computed(() => props.rows.filter((r) => r.reviewStatus === 'SELECTED').length)

const STATUS_LABEL: Record<ReviewStatus, string> = {
  TOP: '优选',
  QUALIFIED: '合格',
  NEW: '待评',
  REJECTED: '已淘汰',
  SELECTED: '已选'
}

const STATUS_TONE: Record<ReviewStatus, string> = {
  TOP: 'top',
  QUALIFIED: 'ok',
  NEW: 'idle',
  REJECTED: 'bad',
  SELECTED: 'top'
}

const statusLabel = (status: ReviewStatus): string => STATUS_LABEL[status]
const statusTone = (status: ReviewStatus): string => STATUS_TONE[status]

function toggleSort(key: string): void {
  const nextOrder: 'asc' | 'desc' =
    props.sortBy === key && props.sortOrder === 'desc' ? 'asc' : 'desc'
  emit('query', { sortBy: key, sortOrder: nextOrder })
}

function applyKeyword(): void {
  emit('query', { keyword: keyword.value.trim() })
}

function applyFilters(): void {
  emit('query', {
    originType: (originType.value || undefined) as MoleculeOriginType | undefined,
    reviewStatus: (reviewStatus.value || undefined) as ReviewStatus | undefined
  })
}

function goPage(page: number): void {
  emit('query', { page })
}

function changePageSize(): void {
  emit('query', { pageSize: pageSize.value, page: 1 })
}
</script>

<style lang="scss" scoped>
.cg {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: $color-surface;
}

/* ---------- 工具条 ---------- */
.cg__toolbar {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  border-bottom: 1px solid $color-border;
}

.cg__search {
  width: 240px;
  padding: 5px $spacing-sm;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  font-size: $font-size-sm;
  color: $color-text;

  &:focus {
    outline: none;
    border-color: $color-brand;
  }
}

.cg__select {
  padding: 5px $spacing-sm;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  font-size: $font-size-sm;
  color: $color-text-soft;

  &--sm {
    padding: 2px 4px;
    font-size: $font-size-xs;
  }
}

.cg__spacer {
  flex: 1;
}

.cg__count {
  font-size: $font-size-xs;
  color: $color-text-faint;
}

/* ---------- 表格 ---------- */
.cg__body {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.cg__state {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-lg;
}

.cg__state--empty {
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: $spacing-2xl $spacing-lg;
}

.cg__state-icon {
  font-size: 30px;
  opacity: 0.5;
}

.cg__state-title {
  font-size: $font-size-base;
  color: $color-text-soft;
}

.cg__state-desc {
  font-size: $font-size-sm;
  color: $color-text-faint;
}

.cg__skeleton {
  height: 16px;
  border-radius: 4px;
  background: $color-surface-sunken;
}

.cg__table {
  width: 100%;
  border-collapse: collapse;
  font-size: $font-size-sm;
}

.cg__th {
  position: sticky;
  top: 0;
  z-index: 1;
  padding: 6px $spacing-sm;
  background: $color-surface-alt;
  border-bottom: 1px solid $color-border;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $color-text-soft;
  text-align: left;
  white-space: nowrap;

  &--num {
    text-align: right;
    cursor: pointer;
    user-select: none;

    &:hover {
      color: $color-brand-strong;
    }
  }

  &--sorted {
    color: $color-brand-strong;
  }

  &--idx {
    width: 46px;
    text-align: right;
  }

  &--status {
    width: 72px;
  }
}

.cg__sort-mark {
  font-size: 9px;
}

.cg__row {
  cursor: pointer;
  transition: background $transition-fast;

  &:hover {
    background: $color-surface-alt;
  }

  &--active {
    background: $color-brand-soft;
  }
}

.cg__td {
  padding: 5px $spacing-sm;
  border-bottom: 1px solid $color-border-soft;
  color: $color-text-soft;

  &--idx {
    text-align: right;
    color: $color-text-faint;
    font-size: $font-size-xs;
  }

  &--name {
    white-space: nowrap;
    color: $color-text;
  }

  &--smiles {
    max-width: 320px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
    font-size: $font-size-xs;
  }

  &--num {
    text-align: right;
    font-variant-numeric: tabular-nums;
  }

  &--score {
    font-weight: $font-weight-semibold;
    color: $color-text;
  }
}

.cg__tag {
  margin-left: 6px;
  padding: 0 4px;
  border-radius: 3px;
  background: $color-brand-soft;
  color: $color-brand-strong;
  font-size: 10px;
}

.cg__badge {
  display: inline-block;
  padding: 1px 6px;
  border-radius: $radius-pill;
  font-size: 11px;

  &--top {
    background: rgba($success-color, 0.14);
    color: $success-color;
  }

  &--ok {
    background: rgba($accent-color, 0.14);
    color: $color-brand-strong;
  }

  &--idle {
    background: $color-surface-sunken;
    color: $color-text-faint;
  }

  &--bad {
    background: rgba($error-color, 0.12);
    color: $error-color;
  }
}

/* ---------- 分页 ---------- */
.cg__footer {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: 6px $spacing-md;
  border-top: 1px solid $color-border;
}

.cg__page-size {
  font-size: $font-size-xs;
  color: $color-text-faint;
}

.cg__page-btn {
  padding: 3px $spacing-sm;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  font-size: $font-size-xs;
  color: $color-text-soft;
  cursor: pointer;

  &:hover:not(:disabled) {
    border-color: $color-brand;
    color: $color-brand-strong;
  }

  &:disabled {
    opacity: 0.45;
    cursor: not-allowed;
  }
}

.cg__page-info {
  font-size: $font-size-xs;
  color: $color-text-soft;
  font-variant-numeric: tabular-nums;
}
</style>
