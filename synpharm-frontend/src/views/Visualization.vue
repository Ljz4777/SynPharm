<template>
  <div class="visualization">
    <Sidebar />
    <main class="visualization__content">
      <header class="visualization__header">
        <div>
          <h1 class="visualization__title">3D可视化</h1>
          <p class="visualization__subtitle">交互式分子结构展示</p>
        </div>
      </header>
      
      <section class="visualization__main">
        <div class="visualization__canvas">
          <div class="visualization__canvas-placeholder">
            <span class="visualization__canvas-icon">🧬</span>
            <span class="visualization__canvas-text">3D分子结构展示区域</span>
            <span class="visualization__canvas-hint">加载中...</span>
          </div>
        </div>
        
        <div class="visualization__controls">
          <div class="visualization__control-section">
            <h3 class="visualization__control-title">显示模式</h3>
            <div class="visualization__mode-buttons">
              <button 
                v-for="mode in displayModes" 
                :key="mode.value"
                @click="displayMode = mode.value"
                class="visualization__mode-btn"
                :class="{ 'visualization__mode-btn--active': displayMode === mode.value }"
              >
                {{ mode.label }}
              </button>
            </div>
          </div>
          
          <div class="visualization__control-section">
            <h3 class="visualization__control-title">颜色方案</h3>
            <div class="visualization__color-options">
              <button 
                v-for="color in colorSchemes" 
                :key="color.value"
                @click="colorScheme = color.value"
                class="visualization__color-btn"
                :class="{ 'visualization__color-btn--active': colorScheme === color.value }"
                :style="{ background: color.preview }"
              ></button>
            </div>
          </div>
          
          <div class="visualization__control-section">
            <h3 class="visualization__control-title">设置</h3>
            <div class="visualization__setting-item">
              <label class="visualization__setting-label">显示网格</label>
              <input 
                v-model="showGrid" 
                type="checkbox" 
                class="visualization__setting-checkbox"
              />
            </div>
            <div class="visualization__setting-item">
              <label class="visualization__setting-label">自动旋转</label>
              <input 
                v-model="autoRotate" 
                type="checkbox" 
                class="visualization__setting-checkbox"
              />
            </div>
            <div class="visualization__setting-item">
              <label class="visualization__setting-label">显示标签</label>
              <input 
                v-model="showLabels" 
                type="checkbox" 
                class="visualization__setting-checkbox"
              />
            </div>
          </div>
          
          <div class="visualization__control-section">
            <h3 class="visualization__control-title">操作</h3>
            <div class="visualization__action-buttons">
              <button class="visualization__action-btn" @click="resetView">重置视角</button>
              <button class="visualization__action-btn" @click="exportImage">导出图片</button>
            </div>
          </div>
        </div>
      </section>
      
      <section class="visualization__info">
        <div class="visualization__info-card">
          <h3 class="visualization__info-title">结构信息</h3>
          <div class="visualization__info-content">
            <div class="visualization__info-row">
              <span class="visualization__info-label">PDB ID</span>
              <span class="visualization__info-value">6M0J</span>
            </div>
            <div class="visualization__info-row">
              <span class="visualization__info-label">靶点名称</span>
              <span class="visualization__info-value">ACE2</span>
            </div>
            <div class="visualization__info-row">
              <span class="visualization__info-label">链数</span>
              <span class="visualization__info-value">4</span>
            </div>
            <div class="visualization__info-row">
              <span class="visualization__info-label">残基数</span>
              <span class="visualization__info-value">805</span>
            </div>
            <div class="visualization__info-row">
              <span class="visualization__info-label">配体</span>
              <span class="visualization__info-value">N/A</span>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from '@/components/Sidebar.vue'

const displayMode = ref('cartoon')
const colorScheme = ref('chain')
const showGrid = ref(true)
const autoRotate = ref(false)
const showLabels = ref(false)

const displayModes = [
  { value: 'cartoon', label: '卡通' },
  { value: 'sphere', label: '球体' },
  { value: 'stick', label: '棍状' },
  { value: 'surface', label: '表面' }
]

const colorSchemes = [
  { value: 'chain', label: '链颜色', preview: 'linear-gradient(to right, #1a1a2e, #0f3460)' },
  { value: 'element', label: '元素', preview: 'linear-gradient(to right, #4CAF50, #FF9800, #2196F3)' },
  { value: 'secondary', label: '二级结构', preview: 'linear-gradient(to right, #E91E63, #2196F3)' },
  { value: 'uniform', label: '单色', preview: '#1a1a2e' }
]

const resetView = () => {
  console.log('重置视角')
}

const exportImage = () => {
  console.log('导出图片')
}
</script>

<style lang="scss" scoped>
.visualization {
  display: flex;
  min-height: 100vh;
  background: $bg-secondary;
}

.visualization__content {
  flex: 1;
  margin-left: $sidebar-width;
  padding: $spacing-lg;
}

.visualization__header {
  margin-bottom: $spacing-xl;
}

.visualization__title {
  font-size: 24px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.visualization__subtitle {
  font-size: $font-size-sm;
  color: $text-muted;
}

.visualization__main {
  display: grid;
  grid-template-columns: 3fr 1fr;
  gap: $spacing-xl;
  margin-bottom: $spacing-xl;
}

.visualization__canvas {
  background: $bg-primary;
  border-radius: $border-radius-lg;
  border: 1px solid $border-light;
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.visualization__canvas-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;
}

.visualization__canvas-icon {
  font-size: 64px;
}

.visualization__canvas-text {
  font-size: $font-size-lg;
  font-weight: 500;
  color: $text-primary;
}

.visualization__canvas-hint {
  font-size: $font-size-xs;
  color: $text-muted;
}

.visualization__controls {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.visualization__control-section {
  background: $bg-primary;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  border: 1px solid $border-light;
}

.visualization__control-title {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.visualization__mode-buttons {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.visualization__mode-btn {
  padding: $spacing-sm;
  background: $bg-secondary;
  border: 1px solid $border-light;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  color: $text-secondary;
  cursor: pointer;
  text-align: left;
  transition: all $transition-fast;
  
  &:hover {
    background: $bg-tertiary;
  }
  
  &--active {
    background: $primary-color;
    color: #ffffff;
    border-color: $primary-color;
  }
}

.visualization__color-options {
  display: flex;
  gap: $spacing-sm;
}

.visualization__color-btn {
  width: 40px;
  height: 40px;
  border-radius: $border-radius-md;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all $transition-fast;
  
  &:hover {
    border-color: $border-color;
  }
  
  &--active {
    border-color: $primary-color;
  }
}

.visualization__setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.visualization__setting-label {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.visualization__setting-checkbox {
  width: 18px;
  height: 18px;
  border-radius: $border-radius-sm;
  cursor: pointer;
}

.visualization__action-buttons {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.visualization__action-btn {
  padding: $spacing-sm;
  background: $bg-secondary;
  border: 1px solid $border-light;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  color: $text-secondary;
  cursor: pointer;
  transition: all $transition-fast;
  
  &:hover {
    background: $bg-tertiary;
  }
}

.visualization__info {
  max-width: 800px;
}

.visualization__info-card {
  background: $bg-primary;
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
  border: 1px solid $border-light;
}

.visualization__info-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-lg;
}

.visualization__info-content {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: $spacing-md;
}

.visualization__info-row {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.visualization__info-label {
  font-size: $font-size-xs;
  color: $text-muted;
}

.visualization__info-value {
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-primary;
}
</style>