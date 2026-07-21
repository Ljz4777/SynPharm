<template>
  <div class="dashboard">
    <Sidebar />
    <main class="dashboard__content">
      <header class="dashboard__header">
        <div>
          <h1 class="dashboard__title">仪表盘</h1>
          <p class="dashboard__subtitle">欢迎回来，{{ authStore.userNickname }}</p>
        </div>
        <div class="dashboard__header-right">
          <span class="dashboard__date">{{ currentDate }}</span>
        </div>
      </header>
      
      <section class="dashboard__stats">
        <div class="dashboard__stat-card">
          <div class="dashboard__stat-icon">📊</div>
          <div class="dashboard__stat-info">
            <span class="dashboard__stat-value">{{ stats.totalTasks }}</span>
            <span class="dashboard__stat-label">总任务数</span>
          </div>
        </div>
        <div class="dashboard__stat-card">
          <div class="dashboard__stat-icon">✅</div>
          <div class="dashboard__stat-info">
            <span class="dashboard__stat-value">{{ stats.completedTasks }}</span>
            <span class="dashboard__stat-label">已完成</span>
          </div>
        </div>
        <div class="dashboard__stat-card">
          <div class="dashboard__stat-icon">⏳</div>
          <div class="dashboard__stat-info">
            <span class="dashboard__stat-value">{{ stats.runningTasks }}</span>
            <span class="dashboard__stat-label">运行中</span>
          </div>
        </div>
        <div class="dashboard__stat-card">
          <div class="dashboard__stat-icon">🎯</div>
          <div class="dashboard__stat-info">
            <span class="dashboard__stat-value">{{ stats.averageConfidence }}%</span>
            <span class="dashboard__stat-label">平均置信度</span>
          </div>
        </div>
      </section>
      
      <section class="dashboard__main">
        <div class="dashboard__section">
          <div class="dashboard__section-header">
            <h2 class="dashboard__section-title">最近任务</h2>
            <router-link to="/tasks" class="dashboard__section-link">查看全部</router-link>
          </div>
          <div class="dashboard__tasks">
            <div 
              v-for="task in recentTasks" 
              :key="task.id"
              class="dashboard__task-item"
            >
              <div class="dashboard__task-info">
                <span class="dashboard__task-name">{{ task.name || task.id }}</span>
                <span class="dashboard__task-type">{{ task.type }}</span>
              </div>
              <div class="dashboard__task-status">
                <span 
                  class="dashboard__status-badge"
                  :class="`dashboard__status-badge--${task.status}`"
                >
                  {{ getStatusText(task.status) }}
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="dashboard__section">
          <div class="dashboard__section-header">
            <h2 class="dashboard__section-title">最近结果</h2>
            <router-link to="/results" class="dashboard__section-link">查看全部</router-link>
          </div>
          <div class="dashboard__results">
            <ResultCard 
              v-for="result in recentResults" 
              :key="result.id"
              :result="result"
              @detail="handleResultDetail"
              @3d="handleResult3D"
            />
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { mockTasks, mockResults } from '@/data/mockResults'
import Sidebar from '@/components/Sidebar.vue'
import ResultCard from '@/components/ResultCard.vue'
import type { PredictionResult, Task } from '@/types'

const authStore = useAuthStore()

const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
})

const stats = computed(() => {
  const total = mockTasks.length
  const completed = mockTasks.filter((t: Task) => t.status === 'completed').length
  const running = mockTasks.filter((t: Task) => t.status === 'running').length
  const avgConfidence = mockResults.length > 0 
    ? Math.round(mockResults.reduce((sum: number, r: PredictionResult) => sum + r.confidenceScore, 0) / mockResults.length * 100)
    : 0
  
  return {
    totalTasks: total,
    completedTasks: completed,
    runningTasks: running,
    averageConfidence: avgConfidence
  }
})

const recentTasks = computed(() => {
  return [...mockTasks].sort((a, b) => 
    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  ).slice(0, 5)
})

const recentResults = computed(() => {
  return [...mockResults].sort((a, b) => 
    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  ).slice(0, 2)
})

const getStatusText = (status: string): string => {
  const texts: Record<string, string> = {
    completed: '已完成',
    running: '运行中',
    pending: '待处理',
    failed: '失败'
  }
  return texts[status] || status
}

const handleResultDetail = (result: PredictionResult) => {
  console.log('查看详情:', result)
}

const handleResult3D = (result: PredictionResult) => {
  console.log('查看3D:', result)
}
</script>

<style lang="scss" scoped>
.dashboard {
  display: flex;
  min-height: 100vh;
  background: $bg-secondary;
}

.dashboard__content {
  flex: 1;
  margin-left: $sidebar-width;
  padding: $spacing-lg $spacing-xl;
}

.dashboard__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-xl;
}

.dashboard__title {
  font-size: 28px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
  letter-spacing: -0.3px;
}

.dashboard__subtitle {
  font-size: $font-size-sm;
  color: $text-muted;
}

.dashboard__date {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.dashboard__stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-lg;
  margin-bottom: $spacing-xl;
}

.dashboard__stat-card {
  background: $bg-primary;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  border: 1px solid $border-light;
  transition: all $transition-fast;
  
  &:hover {
    box-shadow: $shadow-sm;
  }
}

.dashboard__stat-icon {
  font-size: 36px;
}

.dashboard__stat-info {
  display: flex;
  flex-direction: column;
}

.dashboard__stat-value {
  font-size: 28px;
  font-weight: 700;
  color: $primary-color;
  letter-spacing: -0.5px;
}

.dashboard__stat-label {
  font-size: $font-size-xs;
  color: $text-muted;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  margin-top: 2px;
}

.dashboard__main {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: $spacing-xl;
}

.dashboard__section {
  background: $bg-primary;
  border-radius: $border-radius-lg;
  padding: $spacing-xl;
  border: 1px solid $border-light;
}

.dashboard__section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.dashboard__section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.dashboard__section-link {
  font-size: $font-size-xs;
  color: $accent-color;
  text-decoration: none;
  font-weight: 500;
  
  &:hover {
    text-decoration: underline;
  }
}

.dashboard__tasks {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.dashboard__task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md;
  background: $bg-secondary;
  border-radius: $border-radius-md;
  transition: all $transition-fast;
  
  &:hover {
    background: $bg-tertiary;
  }
}

.dashboard__task-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dashboard__task-name {
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-primary;
}

.dashboard__task-type {
  font-size: $font-size-xs;
  color: $text-muted;
}

.dashboard__task-status {
  display: flex;
  align-items: center;
}

.dashboard__status-badge {
  font-size: $font-size-xs;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 100px;
  
  &--completed {
    background: rgba($success-color, 0.1);
    color: $success-color;
  }
  
  &--running {
    background: rgba($accent-color, 0.1);
    color: $accent-color;
  }
  
  &--pending {
    background: rgba($warning-color, 0.1);
    color: $warning-color;
  }
  
  &--failed {
    background: rgba($error-color, 0.1);
    color: $error-color;
  }
}

.dashboard__results {
  display: grid;
  grid-template-columns: 1fr;
  gap: $spacing-lg;
}
</style>