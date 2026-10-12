<template>
  <div class="db">
    <Sidebar />
    <main class="db__main">
      <!-- ==================== 页头 ==================== -->
      <header class="db__header">
        <div class="db__header-main">
          <h1 class="db__title">仪表盘</h1>
          <p class="db__subtitle">欢迎回来，{{ authStore.userNickname }}</p>
        </div>
        <span class="db__date">{{ currentDate }}</span>
      </header>

      <div v-if="loadError" class="db__error">{{ loadError }}</div>

      <!-- ==================== 统计指标（真实数据派生） ==================== -->
      <section class="db__stats">
        <template v-if="loading">
          <span v-for="n in 4" :key="n" class="u-skeleton db__stat-skeleton" />
        </template>
        <template v-else>
          <div class="db__stat">
            <span class="db__stat-icon db__stat-icon--brand" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" />
                <rect x="9" y="3" width="6" height="4" rx="1" />
                <path d="M9 12h6M9 16h4" />
              </svg>
            </span>
            <div class="db__stat-text">
              <span class="db__stat-value">{{ stats.totalTasks }}</span>
              <span class="db__stat-label">总任务数</span>
            </div>
          </div>

          <div class="db__stat">
            <span class="db__stat-icon db__stat-icon--info" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="9" />
                <path d="M12 8v4l3 3" />
              </svg>
            </span>
            <div class="db__stat-text">
              <span class="db__stat-value">{{ stats.running }}</span>
              <span class="db__stat-label">进行中</span>
            </div>
          </div>

          <div class="db__stat">
            <span class="db__stat-icon db__stat-icon--success" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="9" />
                <path d="m9 12 2 2 4-4" />
              </svg>
            </span>
            <div class="db__stat-text">
              <span class="db__stat-value">{{ stats.completed }}</span>
              <span class="db__stat-label">已完成</span>
            </div>
          </div>

          <div class="db__stat">
            <span class="db__stat-icon db__stat-icon--deep" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                <path d="M5 20V10" />
                <path d="M12 20V4" />
                <path d="M19 20v-6" />
              </svg>
            </span>
            <div class="db__stat-text">
              <span class="db__stat-value">{{ stats.results }}</span>
              <span class="db__stat-label">预测结果</span>
            </div>
          </div>
        </template>
      </section>

      <!-- ==================== 最近任务 + 任务趋势 ==================== -->
      <section class="db__grid">
        <article class="db__card">
          <header class="db__card-head">
            <h2 class="db__card-title">最近任务</h2>
            <router-link to="/tasks" class="db__link">查看全部 →</router-link>
          </header>

          <div v-if="loading" class="db__skeletons">
            <span v-for="n in 4" :key="n" class="u-skeleton db__row-skeleton" />
          </div>
          <div v-else-if="recentTasks.length" class="db__task-list">
            <router-link
              v-for="task in recentTasks"
              :key="task.id"
              :to="`/tasks/${task.id}`"
              class="db__task"
            >
              <div class="db__task-info">
                <span class="db__task-name">{{ task.taskNo || task.name || task.id }}</span>
                <span class="db__task-type">{{ getTypeText(task.predictType || task.type || '') }}</span>
              </div>
              <div class="db__task-right">
                <span class="db__task-time">{{ formatDateTime(task.createdAt) }}</span>
                <StatusTag kind="status" :value="task.status" />
              </div>
            </router-link>
          </div>
          <EmptyState
            v-else
            icon="📋"
            title="还没有预测任务"
            description="到预测中心发起一次预测，任务会出现在这里。"
          />
        </article>

        <article class="db__card">
          <header class="db__card-head">
            <h2 class="db__card-title">近 7 日任务趋势</h2>
          </header>

          <div v-if="loading" class="db__chart db__chart--loading">
            <span class="u-skeleton db__chart-skeleton" />
          </div>
          <div v-else-if="hasTrendData" ref="chartRef" class="db__chart" />
          <EmptyState
            v-else
            icon="📈"
            title="暂无任务数据"
            description="发起预测后，这里会展示近 7 天的任务量变化。"
          />
        </article>
      </section>

      <!-- ==================== 最近结果 ==================== -->
      <section class="db__card db__card--results">
        <header class="db__card-head">
          <h2 class="db__card-title">
            最近结果
            <span v-if="!loading && recentResults.length" class="db__card-hint">
              平均置信度 {{ avgConfidence }}
            </span>
          </h2>
          <router-link to="/results" class="db__link">查看全部 →</router-link>
        </header>

        <div v-if="loading" class="db__skeletons db__skeletons--grid">
          <span v-for="n in 3" :key="n" class="u-skeleton db__result-skeleton" />
        </div>
        <div v-else-if="recentResults.length" class="db__results-grid">
          <ResultCard
            v-for="result in recentResults"
            :key="String(result.id)"
            :result="result"
            @detail="handleResultDetail"
            @3d="handleResult3D"
            @delete="handleResultDelete"
          />
        </div>
        <EmptyState
          v-else
          icon="📊"
          title="还没有预测结果"
          description="完成一次预测后，结果会展示在这里，并可进入 3D 可视化查看结构。"
        />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
/**
 * 仪表盘（SynPharm Dashboard — v1）。
 *
 * 单页滚动式：页头 → 四项统计指标 → 最近任务 + 近 7 日任务趋势 → 最近结果。
 * 数据全部来自后端真实接口，无任何写死的业务数据：
 *   - 任务列表 / 统计 / 趋势 → taskApi.getTaskList()    → GET /api/tasks
 *   - 结果列表 / 统计        → resultApi.getResultList() → GET /api/results（含 total 总数）
 *
 * 视觉沿用应用品牌 v2（深青 $app-brand / 青 $app-accent，见 styles/variables.scss），
 * 与顶栏 Sidebar、设计工作台、首页保持一致；趋势图按 dataviz 规范用单色顺序渐变。
 */
import { computed, nextTick, onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { useAuthStore } from '@/stores/auth'
import { taskApi, resultApi } from '@/api/predict'
import Sidebar from '@/components/Sidebar.vue'
import ResultCard from '@/components/ResultCard.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { formatDateTime } from '@/utils/display'
import type { PredictionResult, Task } from '@/types'

const authStore = useAuthStore()
const router = useRouter()

const tasks = ref<Task[]>([])
const results = ref<PredictionResult[]>([])
const resultTotal = ref(0)
const loading = ref(false)
const loadError = ref('')

/* ------------------------------- 数据装载 ------------------------------- */

async function loadDashboard(): Promise<void> {
  loading.value = true
  loadError.value = ''

  try {
    const [taskList, resultPage] = await Promise.all([
      taskApi.getTaskList(),
      resultApi.getResultList(1, 100),
    ])
    tasks.value = taskList
    results.value = resultPage.list
    resultTotal.value = resultPage.total
  } catch (err) {
    loadError.value = err instanceof Error ? err.message : '加载仪表盘数据失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)

/* ------------------------------- 派生统计 ------------------------------- */

const statusCounts = computed(() => {
  const counts: Record<string, number> = { completed: 0, running: 0, pending: 0, failed: 0, cancelled: 0 }
  for (const t of tasks.value) {
    const k = t.status
    if (k in counts) counts[k] += 1
  }
  return counts
})

const stats = computed(() => ({
  totalTasks: tasks.value.length,
  running: statusCounts.value.running + statusCounts.value.pending,
  completed: statusCounts.value.completed,
  results: resultTotal.value
}))

const recentTasks = computed(() =>
  [...tasks.value]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 6)
)

const recentResults = computed(() =>
  [...results.value]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 6)
)

const avgConfidence = computed(() => {
  if (!results.value.length) return '—'
  const sum = results.value.reduce((s, r) => s + (r.confidenceScore ?? 0), 0)
  return `${Math.round((sum / results.value.length) * 100)}%`
})

const currentDate = computed(() =>
  new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
)

/** 任务类型（dti/ppi/ddi）→ 中文，与任务管理页保持一致 */
function getTypeText(type: string): string {
  const texts: Record<string, string> = {
    dti: '药物-靶点',
    ppi: '蛋白-蛋白',
    ddi: '药物-药物'
  }
  return texts[type] || type
}

/* ------------------------------- 任务趋势图 ------------------------------- */

/** 最近 n 天的日期列表（本地时区，YYYY-MM-DD，含今天） */
function lastNDays(n: number): string[] {
  const days: string[] = []
  const now = new Date()
  for (let i = n - 1; i >= 0; i--) {
    const d = new Date(now)
    d.setDate(now.getDate() - i)
    days.push(d.toISOString().slice(0, 10))
  }
  return days
}

/** 近 7 日每日新增任务数（按 createdAt 分桶，真实数据） */
const taskTrend = computed(() => {
  const days = lastNDays(7)
  const counts = new Map<string, number>(days.map((d) => [d, 0]))
  for (const t of tasks.value) {
    const key = t.createdAt ? String(t.createdAt).slice(0, 10) : ''
    if (counts.has(key)) counts.set(key, (counts.get(key) ?? 0) + 1)
  }
  return days.map((d) => ({
    date: d,
    label: `${Number(d.slice(5, 7))}/${Number(d.slice(8, 10))}`,
    value: counts.get(d) ?? 0
  }))
})

const hasTrendData = computed(() => tasks.value.length > 0)

const chartRef = ref<HTMLElement | null>(null)
const chart = shallowRef<ReturnType<typeof echarts.init> | null>(null)
let observer: ResizeObserver | null = null

/** 品牌青（来自参考稿 #0890b0，与 $app-accent 一致） */
const TREND_COLOR = '#0890b0'

function buildOption(): echarts.EChartsOption {
  return {
    grid: { left: 40, right: 16, top: 20, bottom: 28 },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'line' },
      formatter: (params: unknown) => {
        const arr = params as Array<{ name: string; value: number }>
        const p = arr[0]
        return `${p.name}<br/>任务数：${p.value}`
      }
    },
    xAxis: {
      type: 'category',
      data: taskTrend.value.map((d) => d.label),
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisTick: { show: false },
      axisLabel: { fontSize: 11, color: '#94a3b8' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { fontSize: 11, color: '#94a3b8' },
      splitLine: { lineStyle: { color: '#f1f5f9' } }
    },
    series: [
      {
        name: '任务数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: taskTrend.value.map((d) => d.value),
        lineStyle: { width: 2, color: TREND_COLOR },
        itemStyle: { color: TREND_COLOR, borderColor: '#ffffff', borderWidth: 1 },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(8, 144, 176, 0.28)' },
              { offset: 1, color: 'rgba(8, 144, 176, 0.02)' }
            ]
          }
        }
      }
    ]
  }
}

function renderChart(): void {
  if (!chartRef.value) return
  if (!chart.value) {
    chart.value = echarts.init(chartRef.value)
    if (typeof ResizeObserver !== 'undefined') {
      observer = new ResizeObserver(() => chart.value?.resize())
      observer.observe(chartRef.value)
    }
  }
  chart.value.setOption(buildOption(), true)
}

// 数据就绪后（v-if 渲染出容器）再初始化图表
watch(taskTrend, () => void nextTick(renderChart), { deep: true })

onBeforeUnmount(() => {
  observer?.disconnect()
  observer = null
  chart.value?.dispose()
  chart.value = null
})

/* ------------------------------- 跳转 / 操作 ------------------------------- */

const handleResultDetail = (result: PredictionResult) => {
  router.push({ path: '/result/' + String(result.id) })
}

const handleResult3D = (result: PredictionResult) => {
  router.push({
    path: '/visualization',
    query: {
      id: String(result.id),
      targetName: result.targetName || '',
      targetId: result.targetId || ''
    }
  })
}

/** 最近结果卡片的删除（绑定 ResultCard 的 delete 事件，避免无响应按钮） */
const handleResultDelete = async (result: PredictionResult) => {
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
    await loadDashboard()
  } catch (err) {
    ElMessage.error(err instanceof Error ? err.message : '删除失败')
  }
}
</script>

<style lang="scss" scoped>
/* ===================== 仪表盘（SynPharm Dashboard — v1） ===================== */
.db {
  display: flex;
  min-height: 100vh;
  background: $color-canvas;
  padding-top: $header-height;
}

// 宽度与居中由 styles/base.scss 的全局规则统一提供（1200px 窄幅）
.db__main {
  flex: 1;
  padding: $spacing-lg $spacing-xl $spacing-2xl;
}

/* ---------------- 页头 ---------------- */
.db__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: $spacing-lg;
  padding: $spacing-md 0 $spacing-xl;
}

.db__header-main {
  position: relative;
  padding-left: $spacing-md;
  min-width: 0;

  // 品牌色竖条：与设计工作台的视觉锚点一致
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

.db__title {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  line-height: 1.3;
  color: $color-text;
}

.db__subtitle {
  margin-top: 4px;
  font-size: $font-size-sm;
  color: $color-text-faint;
}

.db__date {
  flex-shrink: 0;
  font-size: $font-size-sm;
  color: $color-text-soft;
  background: $color-surface-sunken;
  padding: $spacing-xs $spacing-md;
  border-radius: $radius-pill;
}

.db__error {
  margin-bottom: $spacing-lg;
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-control;
  font-size: $font-size-sm;
  text-align: center;
  background: rgba(239, 68, 68, 0.08);
  color: $error-color;
}

/* ---------------- 统计指标 ---------------- */
.db__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.db__stat-skeleton {
  height: 92px;
  border-radius: $radius-card;
}

.db__stat {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-lg;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  transition: box-shadow 0.2s $ease-out, transform 0.2s $ease-out;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-card-hover;
  }
}

.db__stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: $radius-control;

  svg {
    width: 22px;
    height: 22px;
  }

  // 对齐参考稿的青 / 蓝 / 绿 / 深青四色
  &--brand   { color: #0890b0; background: rgba(8, 144, 176, 0.12); }
  &--info    { color: #3880f0; background: rgba(56, 128, 240, 0.12); }
  &--success { color: #10b880; background: rgba(16, 184, 128, 0.12); }
  &--deep    { color: #084868; background: rgba(8, 72, 104, 0.10); }
}

.db__stat-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.db__stat-value {
  font-size: $font-size-2xl;
  font-weight: $font-weight-semibold;
  line-height: 1.15;
  color: $color-text;
  font-variant-numeric: tabular-nums;
}

.db__stat-label {
  font-size: $font-size-sm;
  color: $color-text-faint;
}

/* ---------------- 卡片与栅格 ---------------- */
.db__grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: $spacing-lg;
  margin-bottom: $spacing-lg;
}

.db__card {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
}

.db__card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-md;
  padding: $spacing-md $spacing-lg;
  border-bottom: 1px solid $color-border-soft;
}

.db__card-title {
  display: flex;
  align-items: baseline;
  gap: $spacing-sm;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $color-text;
}

.db__card-hint {
  font-size: $font-size-xs;
  font-weight: $font-weight-normal;
  color: $color-text-faint;
}

.db__link {
  font-size: $font-size-sm;
  color: $app-accent-strong;
  text-decoration: none;
  white-space: nowrap;

  &:hover { text-decoration: underline; }
}

.db__skeletons {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-md $spacing-lg $spacing-lg;

  &--grid {
    flex-direction: row;
  }
}

.db__row-skeleton {
  height: 52px;
  border-radius: $radius-control;
}

.db__result-skeleton {
  flex: 1;
  min-width: 280px;
  height: 150px;
  border-radius: $radius-card;
}

/* ---------------- 最近任务 ---------------- */
.db__task-list {
  display: flex;
  flex-direction: column;
  padding: $spacing-xs $spacing-sm $spacing-sm;
}

.db__task {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-md;
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-control;
  text-decoration: none;
  transition: background $transition-fast;

  &:hover {
    background: $color-surface-alt;
  }
}

.db__task-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.db__task-name {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.db__task-type {
  font-size: $font-size-xs;
  color: $color-text-faint;
}

.db__task-right {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  flex-shrink: 0;
}

.db__task-time {
  font-size: $font-size-xs;
  color: $color-text-faint;
  font-variant-numeric: tabular-nums;
}

/* ---------------- 任务趋势图 ---------------- */
.db__chart {
  height: 260px;
  padding: $spacing-sm;

  &--loading {
    padding: $spacing-md;
  }
}

.db__chart-skeleton {
  width: 100%;
  height: 220px;
  border-radius: $radius-control;
}

/* ---------------- 最近结果 ---------------- */
.db__results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: $spacing-md;
  padding: $spacing-lg;
}

/* ---------------- 响应式 ---------------- */
@media (max-width: 1100px) {
  .db__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .db__grid {
    grid-template-columns: 1fr;
  }
}
</style>
