<template>
  <div class="tasks">
    <Sidebar />
    <main class="tasks__content">
      <header class="tasks__header">
        <div>
          <h1 class="tasks__title">任务管理</h1>
          <p class="tasks__subtitle">查看和管理所有预测任务</p>
        </div>
        <button class="tasks__btn tasks__btn--primary">
          创建任务
        </button>
      </header>
      
      <section class="tasks__tabs">
        <button 
          v-for="tab in tabs" 
          :key="tab.value"
          @click="activeTab = tab.value"
          class="tasks__tab"
          :class="{ 'tasks__tab--active': activeTab === tab.value }"
        >
          {{ tab.label }}
          <span v-if="tab.count > 0" class="tasks__tab-count">{{ tab.count }}</span>
        </button>
      </section>
      
      <section class="tasks__list">
        <div 
          v-for="task in filteredTasks" 
          :key="task.id"
          class="tasks__card"
        >
          <div class="tasks__card-header">
            <div class="tasks__card-info">
              <span class="tasks__card-name">{{ task.name || task.id }}</span>
              <span class="tasks__card-type">{{ task.type }}</span>
            </div>
            <span 
              class="tasks__status"
              :class="`tasks__status--${task.status}`"
            >
              {{ getStatusText(task.status) }}
            </span>
          </div>
          
          <div v-if="task.status === 'running'" class="tasks__progress">
            <div class="tasks__progress-track">
              <div 
                class="tasks__progress-fill"
                :style="{ width: task.progress + '%' }"
              ></div>
            </div>
            <span class="tasks__progress-text">{{ task.progress }}%</span>
          </div>
          
          <div class="tasks__card-footer">
            <span class="tasks__card-date">{{ formatDate(task.createdAt) }}</span>
            <div class="tasks__card-actions">
              <button 
                v-if="task.status === 'running'" 
                class="tasks__action-btn"
                @click="handlePause(task)"
              >
                暂停
              </button>
              <button 
                v-if="task.status === 'completed'" 
                class="tasks__action-btn"
                @click="handleView(task)"
              >
                查看结果
              </button>
              <button 
                v-if="task.status !== 'running'" 
                class="tasks__action-btn"
                @click="handleDelete(task)"
              >
                删除
              </button>
            </div>
          </div>
        </div>
        
        <div v-if="filteredTasks.length === 0" class="tasks__empty">
          <span class="tasks__empty-icon">📋</span>
          <span class="tasks__empty-text">暂无{{ getTabLabel(activeTab) }}任务</span>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { mockTasks } from '@/data/mockResults'
import Sidebar from '@/components/Sidebar.vue'
import type { Task } from '@/types'

const activeTab = ref('all')

const tabs = computed(() => [
  { value: 'all', label: '全部', count: mockTasks.length },
  { value: 'running', label: '运行中', count: mockTasks.filter((t: Task) => t.status === 'running').length },
  { value: 'pending', label: '待处理', count: mockTasks.filter((t: Task) => t.status === 'pending').length },
  { value: 'completed', label: '已完成', count: mockTasks.filter((t: Task) => t.status === 'completed').length },
  { value: 'failed', label: '失败', count: mockTasks.filter((t: Task) => t.status === 'failed').length }
])

const filteredTasks = computed(() => {
  if (activeTab.value === 'all') {
    return mockTasks
  }
  return mockTasks.filter((t: Task) => t.status === activeTab.value)
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

const getTabLabel = (tab: string): string => {
  const labels: Record<string, string> = {
    all: '',
    running: '运行中',
    pending: '待处理',
    completed: '已完成',
    failed: '失败'
  }
  return labels[tab] || ''
}

const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handlePause = (task: Task) => {
  console.log('暂停任务:', task.id)
}

const handleView = (task: Task) => {
  console.log('查看结果:', task.id)
}

const handleDelete = (task: Task) => {
  console.log('删除任务:', task.id)
}
</script>

<style lang="scss" scoped>
.tasks {
  display: flex;
  min-height: 100vh;
  background: $bg-secondary;
}

.tasks__content {
  flex: 1;
  margin-left: $sidebar-width;
  padding: $spacing-lg;
}

.tasks__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-xl;
}

.tasks__title {
  font-size: 24px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.tasks__subtitle {
  font-size: $font-size-sm;
  color: $text-muted;
}

.tasks__btn {
  padding: $spacing-sm $spacing-lg;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all $transition-fast;
  
  &--primary {
    background: $primary-color;
    color: #ffffff;
    
    &:hover {
      background: $primary-dark;
    }
  }
}

.tasks__tabs {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-xl;
}

.tasks__tab {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-lg;
  background: $bg-primary;
  border: 1px solid $border-light;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  color: $text-secondary;
  cursor: pointer;
  transition: all $transition-fast;
  
  &:hover {
    border-color: $border-color;
  }
  
  &--active {
    background: $primary-color;
    color: #ffffff;
    border-color: $primary-color;
  }
}

.tasks__tab-count {
  font-size: $font-size-xs;
  padding: 1px 6px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 100px;
}

.tasks__list {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.tasks__card {
  background: $bg-primary;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  border: 1px solid $border-light;
}

.tasks__card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-md;
}

.tasks__card-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.tasks__card-name {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.tasks__card-type {
  font-size: $font-size-xs;
  color: $text-muted;
}

.tasks__status {
  font-size: $font-size-xs;
  font-weight: 500;
  padding: 3px 10px;
  border-radius: 100px;
  
  &--completed {
    background: rgba($success-color, 0.1);
    color: $success-color;
  }
  
  &--running {
    background: rgba($info-color, 0.1);
    color: $info-color;
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

.tasks__progress {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.tasks__progress-track {
  flex: 1;
  height: 6px;
  background: $bg-secondary;
  border-radius: 3px;
  overflow: hidden;
}

.tasks__progress-fill {
  height: 100%;
  background: $primary-color;
  border-radius: 3px;
  transition: width 0.5s ease;
}

.tasks__progress-text {
  font-size: $font-size-xs;
  color: $text-muted;
  width: 40px;
  text-align: right;
}

.tasks__card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: $spacing-md;
  border-top: 1px solid $border-light;
}

.tasks__card-date {
  font-size: $font-size-xs;
  color: $text-muted;
}

.tasks__card-actions {
  display: flex;
  gap: $spacing-sm;
}

.tasks__action-btn {
  padding: $spacing-xs $spacing-md;
  background: transparent;
  border: 1px solid $border-color;
  border-radius: $border-radius-sm;
  font-size: $font-size-xs;
  color: $text-secondary;
  cursor: pointer;
  transition: all $transition-fast;
  
  &:hover {
    background: $bg-secondary;
  }
}

.tasks__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-2xl;
}

.tasks__empty-icon {
  font-size: 48px;
  margin-bottom: $spacing-md;
}

.tasks__empty-text {
  font-size: $font-size-base;
  color: $text-muted;
}
</style>