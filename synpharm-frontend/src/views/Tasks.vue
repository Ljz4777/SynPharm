<template>
  <div class="tk">
    <Sidebar />
    <main class="tk__main">
      <!-- ==================== 页头 ==================== -->
      <header class="tk__header">
        <div class="tk__header-main">
          <h1 class="tk__title">任务管理</h1>
          <p class="tk__subtitle">查看和管理全部预测任务</p>
        </div>
        <button class="tk__create" @click="goCreate">创建任务</button>
      </header>

      <!-- ==================== 状态概览 / 筛选 ==================== -->
      <div class="tk__tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          class="tk__tab"
          :class="{ 'tk__tab--active': activeTab === tab.value }"
          @click="activeTab = tab.value"
        >
          {{ tab.label }}
          <span v-if="tab.count > 0" class="tk__tab-count">{{ tab.count }}</span>
        </button>
      </div>

      <section class="tk__filters">
        <div class="tk__search">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <circle cx="11" cy="11" r="7" />
            <path d="m21 21-4.3-4.3" />
          </svg>
          <input v-model="filters.search" type="text" placeholder="搜索任务编号或名称" />
        </div>

        <select v-model="filters.type" class="tk__select">
          <option value="all">全部类型</option>
          <option value="dti">药物-靶点 (DTI)</option>
          <option value="ppi">蛋白-蛋白 (PPI)</option>
          <option value="ddi">药物-药物 (DDI)</option>
        </select>
      </section>

      <!-- ==================== 任务表格 ==================== -->
      <section class="tk__card">
        <div v-if="loading" class="tk__state">
          <span class="u-spinner" />
          <span>加载中…</span>
        </div>

        <div v-else-if="loadError" class="tk__state tk__state--error">
          <span>{{ loadError }}</span>
          <button class="tk__retry" @click="loadTasks">重试</button>
        </div>

        <template v-else-if="filteredTasks.length">
          <div class="tk__table-wrap">
            <table class="tk__table">
              <thead>
                <tr>
                  <th class="tk__th tk__th--task">任务</th>
                  <th class="tk__th tk__th--type">类型</th>
                  <th class="tk__th tk__th--status">状态</th>
                  <th class="tk__th tk__th--progress">进度</th>
                  <th class="tk__th tk__th--time">创建时间</th>
                  <th class="tk__th tk__th--ops">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="task in pagedTasks" :key="task.id" class="tk__row">
                  <td class="tk__td tk__td--task">
                    <span class="tk__task-name">{{ task.taskNo || task.name || task.id }}</span>
                  </td>
                  <td class="tk__td tk__td--type">
                    <span class="tk__type" :class="`tk__type--${taskTypeKey(task)}`">{{ typeLabel(taskType(task)) }}</span>
                  </td>
                  <td class="tk__td tk__td--status">
                    <StatusTag kind="status" :value="task.status" />
                  </td>
                  <td class="tk__td tk__td--progress">
                    <template v-if="task.status === 'running'">
                      <div class="tk__progress">
                        <div class="tk__progress-fill" :style="{ width: (task.progress ?? 0) + '%' }" />
                      </div>
                      <span class="tk__progress-text">{{ task.progress ?? 0 }}%</span>
                    </template>
                    <span v-else class="tk__progress-text">{{ task.status === 'completed' ? '100%' : '—' }}</span>
                  </td>
                  <td class="tk__td tk__td--time">{{ formatDateTime(task.createdAt) }}</td>
                  <td class="tk__td tk__td--ops">
                    <button class="tk__op" @click="handleView(task)">查看</button>
                    <button
                      v-if="task.status === 'running' || task.status === 'pending'"
                      class="tk__op tk__op--danger"
                      @click="handleCancel(task)"
                    >取消</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页 -->
          <div class="tk__pager">
            <span class="tk__pager-info">共 {{ filteredTasks.length }} 条 · 每页</span>
            <select v-model="pageSize" class="tk__page-size">
              <option :value="10">10</option>
              <option :value="20">20</option>
              <option :value="50">50</option>
            </select>
            <div class="tk__pager-nav">
              <button class="tk__page-btn" :disabled="page <= 1" @click="page--">上一页</button>
              <span class="tk__page-cur">{{ page }} / {{ totalPages }}</span>
              <button class="tk__page-btn" :disabled="page >= totalPages" @click="page++">下一页</button>
            </div>
          </div>
        </template>

        <EmptyState
          v-else
          icon="📋"
          :title="tasks.length ? '没有匹配的任务' : '暂无预测任务'"
          :description="tasks.length
            ? '当前筛选条件下没有任务，可调整筛选条件或清空筛选。'
            : '到预测中心发起一次预测，任务会出现在这里。'"
        >
          <button v-if="tasks.length" class="tk__retry" @click="clearFilters">清空筛选</button>
        </EmptyState>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTasks, cancelTask } from '@/api/task'
import Sidebar from '@/components/Sidebar.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { formatDateTime } from '@/utils/display'
import type { Task } from '@/types'

const router = useRouter()

const tasks = ref<Task[]>([])
const loading = ref(false)
const loadError = ref('')

/* ------------------------------- 数据装载 ------------------------------- */

async function loadTasks(): Promise<void> {
  loading.value = true
  loadError.value = ''
  try {
    tasks.value = await getTasks()
  } catch (error: unknown) {
    loadError.value = error instanceof Error ? error.message : '加载任务失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadTasks)

/* ------------------------------- 概览 / 筛选 ------------------------------- */

const activeTab = ref('all')

const filters = reactive({
  search: '',
  type: 'all'
})

const TYPE_LABELS: Record<string, string> = {
  dti: '药物-靶点',
  ppi: '蛋白-蛋白',
  ddi: '药物-药物'
}

const taskType = (t: Task): string => (t.predictType || t.type || '').toLowerCase()
const taskTypeKey = (t: Task): string => taskType(t) || 'none'
const typeLabel = (t?: string): string => (t ? TYPE_LABELS[t.toLowerCase()] || t : '—')

const counts = computed(() => ({
  all: tasks.value.length,
  running: tasks.value.filter((t) => t.status === 'running').length,
  pending: tasks.value.filter((t) => t.status === 'pending').length,
  completed: tasks.value.filter((t) => t.status === 'completed').length,
  failed: tasks.value.filter((t) => t.status === 'failed').length
}))

const tabs = computed(() => [
  { value: 'all', label: '全部', count: counts.value.all },
  { value: 'running', label: '运行中', count: counts.value.running },
  { value: 'pending', label: '待处理', count: counts.value.pending },
  { value: 'completed', label: '已完成', count: counts.value.completed },
  { value: 'failed', label: '失败', count: counts.value.failed }
])

const filteredTasks = computed(() => {
  let list = [...tasks.value]

  if (activeTab.value !== 'all') {
    list = list.filter((t) => t.status === activeTab.value)
  }
  if (filters.type !== 'all') {
    list = list.filter((t) => taskType(t) === filters.type)
  }
  if (filters.search) {
    const q = filters.search.toLowerCase()
    list = list.filter((t) =>
      (t.taskNo || '').toLowerCase().includes(q) ||
      (t.name || '').toLowerCase().includes(q) ||
      String(t.id).toLowerCase().includes(q)
    )
  }

  list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
  return list
})

/* ------------------------------- 分页（客户端） ------------------------------- */

const page = ref(1)
const pageSize = ref(10)

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filteredTasks.value.length / pageSize.value))
)

const pagedTasks = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredTasks.value.slice(start, start + pageSize.value)
})

watch([activeTab, () => filters.type, () => filters.search, pageSize], () => {
  page.value = 1
})

watch(totalPages, (tp) => {
  if (page.value > tp) page.value = tp
})

/* ------------------------------- 操作 ------------------------------- */

function clearFilters() {
  activeTab.value = 'all'
  filters.search = ''
  filters.type = 'all'
}

function goCreate() {
  router.push('/predict')
}

function handleView(task: Task) {
  router.push(`/tasks/${task.id}`)
}

async function handleCancel(task: Task) {
  try {
    await ElMessageBox.confirm('确定取消该预测任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await cancelTask(task.id)
    ElMessage.success('任务取消成功')
    await loadTasks()
  } catch (error: unknown) {
    ElMessage.error(error instanceof Error ? error.message : '取消任务失败')
  }
}
</script>

<style lang="scss" scoped>
/* ===================== 任务管理（数据工作台） ===================== */
.tk {
  display: flex;
  min-height: 100vh;
  background: $color-canvas;
  padding-top: $header-height;
}

// 宽度与居中由 styles/base.scss 的全局规则统一提供（1200px 窄幅）
.tk__main {
  flex: 1;
  padding: $spacing-lg $spacing-xl $spacing-2xl;
}

/* ---------------- 页头 ---------------- */
.tk__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: $spacing-lg;
  padding: $spacing-md 0 $spacing-xl;
}

.tk__header-main {
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

.tk__title {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  line-height: 1.3;
  color: $color-text;
}

.tk__subtitle {
  margin-top: 4px;
  font-size: $font-size-sm;
  color: $color-text-faint;
}

.tk__create {
  flex-shrink: 0;
  padding: $spacing-sm $spacing-lg;
  border: none;
  border-radius: $radius-control;
  background: $app-accent;
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover { background: $app-accent-strong; }
}

/* ---------------- 状态概览 Tabs ---------------- */
.tk__tabs {
  display: inline-flex;
  background: $color-surface-sunken;
  border-radius: $radius-control;
  padding: 4px;
  gap: 4px;
  margin-bottom: $spacing-md;
}

.tk__tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: $spacing-sm $spacing-lg;
  border: none;
  background: transparent;
  border-radius: $border-radius-sm;
  font-size: $font-size-base;
  color: $color-text-soft;
  cursor: pointer;
  transition: $transition-fast;

  &:hover { color: $color-text; }

  &--active {
    background: $color-surface;
    color: $app-brand;
    font-weight: $font-weight-medium;
    box-shadow: $shadow-sm;
  }
}

.tk__tab-count {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: $radius-pill;
  background: $color-surface-sunken;
  color: $color-text-faint;
  font-size: $font-size-xs;
  line-height: 18px;
  text-align: center;
}

.tk__tab--active .tk__tab-count {
  background: $app-accent-soft;
  color: $app-accent-strong;
}

/* ---------------- 筛选区 ---------------- */
.tk__filters {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  flex-wrap: wrap;
  margin-bottom: $spacing-lg;
}

.tk__search {
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

.tk__select {
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

/* ---------------- 卡片 / 表格 ---------------- */
.tk__card {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  overflow: hidden;
}

.tk__table-wrap {
  overflow-x: auto;
}

.tk__table {
  width: 100%;
  border-collapse: collapse;
  font-size: $font-size-sm;
}

.tk__th {
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

.tk__row {
  border-bottom: 1px solid $color-border-soft;
  transition: background $transition-fast;

  &:hover { background: $app-accent-softer; }
  &:last-child { border-bottom: none; }
}

.tk__td {
  padding: $spacing-sm $spacing-md;
  vertical-align: middle;
  color: $color-text;
}

/* 任务列 */
.tk__td--task {
  min-width: 160px;

  .tk__task-name {
    font-weight: $font-weight-medium;
    color: $color-text;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-family: monospace;
    font-size: $font-size-sm;
  }
}

/* 类型徽标 */
.tk__type {
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

/* 进度列 */
.tk__td--progress {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  min-width: 140px;
}

.tk__progress {
  flex: 1;
  height: 6px;
  background: $color-border;
  border-radius: $radius-pill;
  overflow: hidden;
  min-width: 60px;
}

.tk__progress-fill {
  height: 100%;
  background: linear-gradient(90deg, $app-accent, $app-brand);
  border-radius: $radius-pill;
  transition: width 0.3s ease;
}

.tk__progress-text {
  font-size: $font-size-xs;
  color: $color-text-faint;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

/* 时间列 */
.tk__td--time {
  white-space: nowrap;
  color: $color-text-faint;
  font-variant-numeric: tabular-nums;
}

/* 操作列 */
.tk__td--ops {
  white-space: nowrap;
  text-align: right;
}

.tk__op {
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

  &--danger {
    border-color: transparent;
    color: $color-text-faint;
    &:hover { color: $error-color; border-color: rgba(239, 68, 68, 0.3); }
  }
}

/* ---------------- 分页 ---------------- */
.tk__pager {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md $spacing-lg;
  border-top: 1px solid $color-border-soft;
  font-size: $font-size-sm;
  color: $color-text-faint;
}

.tk__pager-info {
  white-space: nowrap;
}

.tk__page-size {
  padding: 4px $spacing-sm;
  border: 1px solid $color-border;
  border-radius: $border-radius-sm;
  background: $color-surface;
  color: $color-text;
  font-size: $font-size-sm;
  cursor: pointer;
}

.tk__pager-nav {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.tk__page-cur {
  font-variant-numeric: tabular-nums;
  color: $color-text-soft;
}

.tk__page-btn {
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
.tk__state {
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

.tk__retry {
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
  .tk__filters {
    flex-direction: column;
    align-items: stretch;

    .tk__search { min-width: 0; }
    .tk__select { width: 100%; }
  }

  .tk__header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
