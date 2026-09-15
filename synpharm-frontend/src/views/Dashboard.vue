<template>
  <div class="db">
    <Sidebar />
    <main class="db__main">
      <PageHeader
        icon="📊"
        title="仪表盘"
        :subtitle="`欢迎回来，${authStore.userNickname}`"
      >
        <template #actions>
          <span class="db__date">{{ currentDate }}</span>
        </template>
      </PageHeader>

      <TabBar v-model="activeTab" :tabs="tabs" />

      <div v-if="loadError" class="db__error">{{ loadError }}</div>

      <!-- 页签一：最近任务 -->
      <AppCard v-if="activeTab === 'tasks'" padding="none">
        <template #actions>
          <router-link to="/tasks" class="db__link">查看全部 →</router-link>
        </template>

        <div v-if="loading" class="db__tasks">
          <span v-for="n in 3" :key="n" class="u-skeleton db__row-skeleton" />
        </div>

        <div v-else-if="recentTasks.length" class="db__tasks">
          <div v-for="task in recentTasks" :key="task.id" class="db__task">
            <div class="db__task-info">
              <span class="db__task-name">{{ task.name || task.taskNo || task.id }}</span>
              <span class="db__task-type">{{ task.predictType || task.type || '—' }}</span>
            </div>
            <StatusTag kind="status" :value="task.status" />
          </div>
        </div>

        <EmptyState
          v-else
          icon="📋"
          title="还没有预测任务"
          description="到预测中心发起一次单条或批量预测，任务会出现在这里。"
        />
      </AppCard>

      <!-- 页签二：最近结果 -->
      <AppCard v-else-if="activeTab === 'results'" padding="none">
        <template #actions>
          <router-link to="/results" class="db__link">查看全部 →</router-link>
        </template>

        <div v-if="loading" class="db__results">
          <span v-for="n in 2" :key="n" class="u-skeleton db__row-skeleton" />
        </div>

        <div v-else-if="recentResults.length" class="db__results">
          <ResultCard
            v-for="result in recentResults"
            :key="result.id"
            :result="result"
            @detail="handleResultDetail"
            @3d="handleResult3D"
          />
        </div>

        <EmptyState
          v-else
          icon="📈"
          title="还没有预测结果"
          description="完成一次预测后，结果会展示在这里，并可进入 3D 可视化查看结构。"
        />
      </AppCard>

      <!-- 页签三：统计指标 -->
      <AppCard v-else padding="lg">
        <div class="db__stats">
          <template v-if="loading">
            <span v-for="n in 4" :key="n" class="u-skeleton db__skeleton" />
          </template>
          <template v-else>
            <StatCard icon="📊" :value="stats.totalTasks" label="总任务数" tone="brand" />
            <StatCard icon="✅" :value="stats.completedTasks" label="已完成" tone="success" />
            <StatCard icon="⏳" :value="stats.runningTasks" label="运行中" tone="warning" />
            <StatCard icon="🎯" :value="`${stats.averageConfidence}%`" label="平均置信度" tone="info" />
          </template>
        </div>
      </AppCard>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { taskApi, resultApi } from '@/api/predict'
import Sidebar from '@/components/Sidebar.vue'
import ResultCard from '@/components/ResultCard.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import AppCard from '@/components/ui/AppCard.vue'
import StatCard from '@/components/ui/StatCard.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import TabBar from '@/components/ui/TabBar.vue'
import type { PredictionResult, Task } from '@/types'

const authStore = useAuthStore()
const router = useRouter()

const tasks = ref<Task[]>([])
const results = ref<PredictionResult[]>([])
const loading = ref(false)
const loadError = ref('')

/** 二级页签：最近任务 / 最近结果 / 统计指标 */
type TabKey = 'tasks' | 'results' | 'stats'
const activeTab = ref<TabKey>('tasks')

const tabs = computed(() => [
  { key: 'tasks', label: '最近任务', icon: '📋', count: tasks.value.length },
  { key: 'results', label: '最近结果', icon: '📈', count: results.value.length },
  { key: 'stats', label: '统计指标', icon: '📊' }
])

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
  } catch (err) {
    loadError.value = err instanceof Error ? err.message : '加载仪表盘数据失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)

const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
})

const stats = computed(() => {
  const total = tasks.value.length
  const completed = tasks.value.filter((t: Task) => t.status === 'completed').length
  const running = tasks.value.filter((t: Task) => t.status === 'running').length
  const avgConfidence = results.value.length > 0
    ? Math.round(results.value.reduce((sum: number, r: PredictionResult) => sum + (r.confidenceScore ?? 0), 0) / results.value.length * 100)
    : 0
  
  return {
    totalTasks: total,
    completedTasks: completed,
    runningTasks: running,
    averageConfidence: avgConfidence
  }
})

const recentTasks = computed(() => {
  return [...tasks.value].sort((a, b) =>
    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  ).slice(0, 5)
})

const recentResults = computed(() => {
  return [...results.value].sort((a, b) =>
    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  ).slice(0, 2)
})

const handleResultDetail = (result: PredictionResult) => {
  router.push({
    path: '/result/' + String(result.id),
  })
}

const handleResult3D = (result: PredictionResult) => {
  router.push({
    path: '/visualization',
    query: {
      id: String(result.id),
      targetName: result.targetName || '',
      targetId: result.targetId || '',
    },
  })
}
</script>

<style lang="scss" scoped>
/* ===================== 仪表盘 ===================== */
.db {
  display: flex;
  min-height: 100vh;
  background: $bg-secondary;
  padding-top: $header-height;
}

// 宽度与居中由 styles/base.scss 的全局规则统一提供，此处不再重复声明
.db__main {
  flex: 1;
  padding: $spacing-lg $spacing-xl $spacing-2xl;
}

.db__date {
  font-size: $font-size-sm;
  color: $text-secondary;
  background: $bg-tertiary;
  padding: $spacing-xs $spacing-md;
  border-radius: 999px;
}

.db__error {
  margin-bottom: $spacing-lg;
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  text-align: center;
  background: rgba(239, 68, 68, 0.08);
  color: $error-color;
}

.db__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: $spacing-md;
}

.db__skeleton {
  height: 78px;
  border-radius: $radius-card;
}

// 列表加载态的骨架条
.db__row-skeleton {
  height: 52px;
  border-radius: $border-radius-md;
}

// 窄屏下指标卡改两列，避免数值被挤压换行
@media (max-width: 1100px) {
  .db__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.db__link {
  font-size: $font-size-sm;
  color: $accent-color;
  text-decoration: none;
  &:hover { text-decoration: underline; }
}

.db__tasks {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-md $spacing-lg $spacing-lg;
}

.db__task {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-sm $spacing-md;
  background: $bg-secondary;
  border-radius: $border-radius-md;
}

.db__task-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.db__task-name {
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-primary;
}

.db__task-type {
  font-size: $font-size-xs;
  color: $text-muted;
}

// 结果卡片自带内边距，这里只负责列表布局
.db__results {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  padding: $spacing-lg;
}
</style>