<template>
  <div class="targets">
    <Sidebar />
    <main class="targets__content">
      <header class="targets__header">
        <div>
          <h1 class="targets__title">靶点库</h1>
          <p class="targets__subtitle">浏览和搜索支持的靶点</p>
        </div>
      </header>
      
      <section class="targets__search">
        <input 
          v-model="searchQuery" 
          type="text" 
          class="targets__search-input"
          placeholder="搜索靶点名称或ID..."
        />
        <select v-model="filterOrganism" class="targets__organism-select">
          <option value="all">所有物种</option>
          <option value="human">人类</option>
          <option value="mouse">小鼠</option>
          <option value="rat">大鼠</option>
        </select>
      </section>
      
      <section class="targets__main">
        <div class="targets__list">
          <div 
            v-for="target in filteredTargets" 
            :key="target.id"
            @click="selectTarget(target)"
            class="targets__item"
            :class="{ 'targets__item--selected': selectedTarget?.id === target.id }"
          >
            <div class="targets__item-info">
              <span class="targets__item-name">{{ target.name }}</span>
              <span class="targets__item-id">{{ target.uniprotId }}</span>
            </div>
            <div class="targets__item-organism">
              {{ getOrganismText(target.organism) }}
            </div>
          </div>
          
          <div v-if="filteredTargets.length === 0" class="targets__empty">
            <span class="targets__empty-icon">🧪</span>
            <span class="targets__empty-text">未找到匹配的靶点</span>
          </div>
        </div>
        
        <div v-if="selectedTarget" class="targets__detail">
          <div class="targets__detail-header">
            <h3 class="targets__detail-title">{{ selectedTarget.name }}</h3>
            <button class="targets__use-btn" @click.stop="useTarget(selectedTarget)">
              使用此靶点
            </button>
          </div>
          
          <div class="targets__detail-content">
            <div class="targets__detail-row">
              <span class="targets__detail-label">UniProt ID</span>
              <span class="targets__detail-value">{{ selectedTarget.uniprotId }}</span>
            </div>
            <div class="targets__detail-row">
              <span class="targets__detail-label">基因名称</span>
              <span class="targets__detail-value">{{ selectedTarget.geneName || '-' }}</span>
            </div>
            <div class="targets__detail-row">
              <span class="targets__detail-label">物种</span>
              <span class="targets__detail-value">{{ getOrganismText(selectedTarget.organism) }}</span>
            </div>
            <div class="targets__detail-row">
              <span class="targets__detail-label">功能描述</span>
              <span class="targets__detail-value">{{ selectedTarget.description }}</span>
            </div>
            <div class="targets__detail-row">
              <span class="targets__detail-label">结构信息</span>
              <span class="targets__detail-value">{{ selectedTarget.pdbIds?.join(', ') || selectedTarget.pdbId || '-' }}</span>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { mockTargets } from '@/data/mockResults'
import Sidebar from '@/components/Sidebar.vue'
import type { Target } from '@/types'

const searchQuery = ref('')
const filterOrganism = ref('all')
const selectedTarget = ref<Target | null>(null)

const filteredTargets = computed(() => {
  let targets = [...mockTargets]
  
  if (filterOrganism.value !== 'all') {
    targets = targets.filter(t => t.organism === filterOrganism.value)
  }
  
  if (searchQuery.value) {
    const search = searchQuery.value.toLowerCase()
    targets = targets.filter(t => 
      t.name.toLowerCase().includes(search) ||
      t.uniprotId.toLowerCase().includes(search) ||
      (t.geneName && t.geneName.toLowerCase().includes(search))
    )
  }
  
  return targets
})

const getOrganismText = (organism?: string): string => {
  if (!organism) return '-'
  const texts: Record<string, string> = {
    human: '人类',
    mouse: '小鼠',
    rat: '大鼠'
  }
  return texts[organism] || organism
}

const selectTarget = (target: Target) => {
  selectedTarget.value = target
}

const useTarget = (_target: Target) => {
  window.open('/predict', '_self')
}
</script>

<style lang="scss" scoped>
.targets {
  display: flex;
  min-height: 100vh;
  background: $bg-secondary;
}

.targets__content {
  flex: 1;
  margin-left: $sidebar-width;
  padding: $spacing-lg;
}

.targets__header {
  margin-bottom: $spacing-xl;
}

.targets__title {
  font-size: 24px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.targets__subtitle {
  font-size: $font-size-sm;
  color: $text-muted;
}

.targets__search {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-xl;
}

.targets__search-input {
  flex: 1;
  padding: $spacing-sm $spacing-md;
  border: 1px solid $border-color;
  border-radius: $border-radius-md;
  font-size: $font-size-base;
  background: $bg-primary;
  
  &:focus {
    outline: none;
    border-color: $primary-color;
  }
}

.targets__organism-select {
  padding: $spacing-sm $spacing-md;
  border: 1px solid $border-color;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  background: $bg-primary;
  cursor: pointer;
  
  &:focus {
    outline: none;
    border-color: $primary-color;
  }
}

.targets__main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-xl;
}

.targets__list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.targets__item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md;
  background: $bg-primary;
  border-radius: $border-radius-md;
  border: 1px solid $border-light;
  cursor: pointer;
  transition: all $transition-fast;
  
  &:hover {
    border-color: $border-color;
  }
  
  &--selected {
    border-color: $primary-color;
    background: rgba($primary-color, 0.02);
  }
}

.targets__item-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.targets__item-name {
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-primary;
}

.targets__item-id {
  font-size: $font-size-xs;
  color: $text-muted;
  font-family: monospace;
}

.targets__item-organism {
  font-size: $font-size-xs;
  color: $text-muted;
  padding: 2px 8px;
  background: $bg-secondary;
  border-radius: $border-radius-sm;
}

.targets__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-2xl;
}

.targets__empty-icon {
  font-size: 48px;
  margin-bottom: $spacing-md;
}

.targets__empty-text {
  font-size: $font-size-base;
  color: $text-muted;
}

.targets__detail {
  background: $bg-primary;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  border: 1px solid $border-light;
  height: fit-content;
}

.targets__detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.targets__detail-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
}

.targets__use-btn {
  padding: $spacing-sm $spacing-lg;
  background: $primary-color;
  color: #ffffff;
  border: none;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  font-weight: 500;
  cursor: pointer;
  transition: all $transition-fast;
  
  &:hover {
    background: $primary-dark;
  }
}

.targets__detail-content {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.targets__detail-row {
  display: flex;
  gap: $spacing-md;
}

.targets__detail-label {
  font-size: $font-size-xs;
  color: $text-muted;
  width: 100px;
  flex-shrink: 0;
}

.targets__detail-value {
  font-size: $font-size-sm;
  color: $text-primary;
  flex: 1;
}
</style>