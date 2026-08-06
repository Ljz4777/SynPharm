<template>
  <div class="results">
    <Sidebar />
    <main class="results__content">
      <header class="results__header">
        <div>
          <h1 class="results__title">预测结果</h1>
          <p class="results__subtitle">管理和查看所有预测结果</p>
        </div>
      </header>
      
      <section class="results__filters">
        <div class="results__search">
          <input 
            v-model="filters.search" 
            type="text" 
            class="results__search-input"
            placeholder="搜索靶点名称、ID..."
          />
        </div>
        <div class="results__filter-group">
          <select v-model="filters.confidence" class="results__select">
            <option value="all">所有置信度</option>
            <option value="high">高</option>
            <option value="medium">中</option>
            <option value="low">低</option>
          </select>
          <select v-model="filters.sortBy" class="results__select">
            <option value="date">按日期</option>
            <option value="affinity">按亲和力</option>
            <option value="confidence">按置信度</option>
          </select>
        </div>
      </section>
      
      <section class="results__list">
        <div v-if="loading" class="results__loading">加载中...</div>
        <div v-else-if="loadError" class="results__error">{{ loadError }}</div>
        <template v-else>
          <div class="results__grid">
            <ResultCard 
              v-for="result in filteredResults" 
              :key="String(result.id)"
              :result="result"
              @detail="handleResultDetail"
              @3d="handleResult3D"
            />
          </div>
          
          <div v-if="filteredResults.length === 0" class="results__empty">
            <span class="results__empty-icon">📭</span>
            <span class="results__empty-text">暂无预测结果</span>
            <router-link to="/predict" class="results__empty-link">开始预测</router-link>
          </div>
        </template>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, ref, onMounted } from 'vue'
import { resultApi } from '@/api/predict'
import Sidebar from '@/components/Sidebar.vue'
import ResultCard from '@/components/ResultCard.vue'
import type { PredictionResult } from '@/types'

const filters = reactive({
  confidence: 'all',
  search: '',
  sortBy: 'date'
})

const results = ref<PredictionResult[]>([])
const loading = ref(false)
const loadError = ref('')

const loadResults = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const page = await resultApi.getResultList(1, 100)
    results.value = page.list
  } catch (error: unknown) {
    loadError.value = error instanceof Error ? error.message : '加载预测结果失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadResults)

const filteredResults = computed(() => {
  let list = [...results.value]
  
  if (filters.confidence !== 'all') {
    list = list.filter(r => r.confidenceLevel === filters.confidence)
  }
  
  if (filters.search) {
    const search = filters.search.toLowerCase()
    list = list.filter(r => 
      (r.targetName || '').toLowerCase().includes(search) ||
      (r.targetId || '').toLowerCase().includes(search) ||
      String(r.id).toLowerCase().includes(search)
    )
  }
  
  switch (filters.sortBy) {
    case 'date':
      list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      break
    case 'affinity':
      list.sort((a, b) => (a.bindingAffinity ?? 0) - (b.bindingAffinity ?? 0))
      break
    case 'confidence':
      list.sort((a, b) => (b.confidenceScore ?? 0) - (a.confidenceScore ?? 0))
      break
  }
  
  return list
})

const handleResultDetail = (result: PredictionResult) => {
  console.log('查看详情:', result)
}

const handleResult3D = (result: PredictionResult) => {
  console.log('查看3D:', result)
}
</script>

<style lang="scss" scoped>
.results {
  display: flex;
  min-height: 100vh;
  background: $bg-secondary;
}

.results__content {
  flex: 1;
  margin-left: $sidebar-width;
  padding: $spacing-lg;
}

.results__header {
  margin-bottom: $spacing-xl;
}

.results__title {
  font-size: 24px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.results__subtitle {
  font-size: $font-size-sm;
  color: $text-muted;
}

.results__filters {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-xl;
  padding: $spacing-md;
  background: $bg-primary;
  border-radius: $border-radius-lg;
  border: 1px solid $border-light;
}

.results__search {
  flex: 1;
  max-width: 400px;
}

.results__search-input {
  width: 100%;
  padding: $spacing-sm $spacing-md;
  border: 1px solid $border-color;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  background: $bg-secondary;
  
  &:focus {
    outline: none;
    border-color: $primary-color;
  }
}

.results__filter-group {
  display: flex;
  gap: $spacing-md;
}

.results__select {
  padding: $spacing-sm $spacing-md;
  border: 1px solid $border-color;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  background: $bg-secondary;
  cursor: pointer;
  
  &:focus {
    outline: none;
    border-color: $primary-color;
  }
}

.results__list {
  min-height: 400px;
}

.results__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: $spacing-lg;
}

.results__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-2xl;
}

.results__empty-icon {
  font-size: 48px;
  margin-bottom: $spacing-md;
}

.results__empty-text {
  font-size: $font-size-base;
  color: $text-muted;
  margin-bottom: $spacing-md;
}

.results__empty-link {
  padding: $spacing-sm $spacing-lg;
  background: $primary-color;
  color: #ffffff;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  font-weight: 500;
  text-decoration: none;
  
  &:hover {
    background: $primary-dark;
  }
}
</style>