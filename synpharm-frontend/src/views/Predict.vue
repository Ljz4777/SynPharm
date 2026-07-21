<template>
  <div class="predict">
    <Sidebar />
    <main class="predict__content">
      <header class="predict__header">
        <h1 class="predict__title">预测中心</h1>
        <p class="predict__subtitle">选择预测类型并输入数据开始预测</p>
      </header>
      
      <section class="predict__type-selector">
        <div 
          v-for="type in predictionTypes" 
          :key="type.value"
          @click="selectedType = type.value"
          class="predict__type-card"
          :class="{ 'predict__type-card--active': selectedType === type.value }"
        >
          <span class="predict__type-icon">{{ type.icon }}</span>
          <span class="predict__type-name">{{ type.label }}</span>
          <span class="predict__type-desc">{{ type.description }}</span>
        </div>
      </section>
      
      <section class="predict__form">
        <div class="predict__form-card">
          <div class="predict__form-header">
            <h2 class="predict__form-title">输入数据</h2>
          </div>
          
          <div class="predict__input-section">
            <label class="predict__label">输入类型</label>
            <div class="predict__input-type-group">
              <button 
                v-for="inputType in inputTypes" 
                :key="inputType.value"
                @click="selectedInputType = inputType.value"
                class="predict__input-type-btn"
                :class="{ 'predict__input-type-btn--active': selectedInputType === inputType.value }"
              >
                {{ inputType.label }}
              </button>
            </div>
          </div>
          
          <div class="predict__input-section">
            <label class="predict__label">
              {{ selectedInputType === 'csv' ? '上传CSV文件' : '输入值' }}
            </label>
            <input 
              v-if="selectedInputType !== 'csv'"
              v-model="inputValue"
              type="text"
              class="predict__input"
              :placeholder="getPlaceholder()"
            />
            <div v-else class="predict__file-upload">
              <input 
                type="file" 
                accept=".csv" 
                class="predict__file-input"
                @change="handleFileUpload"
              />
              <div class="predict__file-upload-area">
                <span class="predict__file-icon">📁</span>
                <span class="predict__file-text">点击或拖拽上传CSV文件</span>
              </div>
            </div>
          </div>
          
          <div v-if="selectedInputType === 'csv' && uploadedFileName" class="predict__uploaded-file">
            <span class="predict__uploaded-icon">✓</span>
            <span class="predict__uploaded-name">{{ uploadedFileName }}</span>
            <button class="predict__uploaded-remove" @click="clearFile">移除</button>
          </div>
          
          <div class="predict__options">
            <button 
              @click="showAdvancedOptions = !showAdvancedOptions"
              class="predict__options-toggle"
            >
              {{ showAdvancedOptions ? '收起' : '展开' }}高级选项
            </button>
            
            <div v-if="showAdvancedOptions" class="predict__advanced-options">
              <div class="predict__advanced-option">
                <label class="predict__label">置信度阈值</label>
                <input 
                  v-model="confidenceThreshold"
                  type="range"
                  min="0"
                  max="100"
                  class="predict__range-input"
                />
                <span class="predict__range-value">{{ confidenceThreshold }}%</span>
              </div>
              <div class="predict__advanced-option">
                <label class="predict__label">输出详细结果</label>
                <input 
                  v-model="detailedOutput"
                  type="checkbox"
                  class="predict__checkbox"
                />
              </div>
            </div>
          </div>
          
          <div class="predict__actions">
            <button 
              @click="handleDemoPredict" 
              class="predict__btn predict__btn--outline"
            >
              使用演示数据
            </button>
            <button 
              @click="handlePredict" 
              class="predict__btn predict__btn--primary"
              :disabled="!isValidInput"
            >
              {{ isLoading ? '预测中...' : '开始预测' }}
            </button>
          </div>
        </div>
      </section>
      
      <section v-if="demoResult" class="predict__result">
        <div class="predict__result-card">
          <div class="predict__result-header">
            <h3 class="predict__result-title">预测结果</h3>
            <span class="predict__result-badge">演示数据</span>
          </div>
          
          <div class="predict__result-main">
            <div class="predict__result-item">
              <span class="predict__result-label">靶点名称</span>
              <span class="predict__result-value">{{ demoResult.targetName }}</span>
            </div>
            <div class="predict__result-item">
              <span class="predict__result-label">靶点ID</span>
              <span class="predict__result-value">{{ demoResult.targetId }}</span>
            </div>
            <div class="predict__result-item">
              <span class="predict__result-label">结合亲和力</span>
              <span class="predict__result-value highlight">{{ demoResult.bindingAffinity.toFixed(2) }} kcal/mol</span>
            </div>
            <div class="predict__result-item">
              <span class="predict__result-label">置信度</span>
              <span class="predict__result-value" :style="{ color: getConfidenceColor(demoResult.confidenceScore) }">
                {{ Math.round(demoResult.confidenceScore * 100) }}%
              </span>
            </div>
            <div class="predict__result-item">
              <span class="predict__result-label">置信等级</span>
              <span 
                class="predict__result-badge"
                :class="`predict__result-badge--${demoResult.confidenceLevel}`"
              >
                {{ getConfidenceText(demoResult.confidenceLevel) }}
              </span>
            </div>
          </div>
          
          <div class="predict__result-interactions">
            <span class="predict__result-label">相互作用类型</span>
            <div class="predict__interaction-list">
              <div 
                v-for="(interaction, index) in demoResult.interactions" 
                :key="index"
                class="predict__interaction-item"
              >
                <span class="predict__interaction-type" :style="{ background: getInteractionColor(interaction.type) }">
                  {{ getInteractionTypeName(interaction.type) }}
                </span>
                <span class="predict__interaction-residue">{{ interaction.residueName }} {{ interaction.residueNumber }}</span>
                <span class="predict__interaction-distance">{{ interaction.distance }} Å</span>
              </div>
            </div>
          </div>
          
          <div class="predict__result-actions">
            <button class="predict__btn predict__btn--secondary">保存结果</button>
            <button class="predict__btn predict__btn--primary">3D可视化</button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { mockResults } from '@/data/mockResults'
import Sidebar from '@/components/Sidebar.vue'
import type { PredictionResult } from '@/types'

const selectedType = ref<'ppi' | 'dti' | 'ddi'>('dti')
const selectedInputType = ref<'pdb' | 'uniprot' | 'smiles' | 'csv'>('pdb')
const inputValue = ref('')
const uploadedFileName = ref('')
const confidenceThreshold = ref(70)
const detailedOutput = ref(true)
const showAdvancedOptions = ref(false)
const isLoading = ref(false)
const demoResult = ref<PredictionResult | null>(null)

const predictionTypes: Array<{ value: 'ppi' | 'dti' | 'ddi', label: string, icon: string, description: string }> = [
  { value: 'ppi', label: 'PPI预测', icon: '🔬', description: '蛋白质-蛋白质相互作用' },
  { value: 'dti', label: 'DTI预测', icon: '💊', description: '药物-靶点相互作用' },
  { value: 'ddi', label: 'DDI预测', icon: '⚗️', description: '药物-药物相互作用' }
]

const inputTypes: Array<{ value: 'pdb' | 'uniprot' | 'smiles' | 'csv', label: string }> = [
  { value: 'pdb', label: 'PDB ID' },
  { value: 'uniprot', label: 'UniProt ID' },
  { value: 'smiles', label: 'SMILES' },
  { value: 'csv', label: 'CSV批量' }
]

const isValidInput = computed(() => {
  if (selectedInputType.value === 'csv') {
    return uploadedFileName.value !== ''
  }
  return inputValue.value.trim() !== ''
})

const getPlaceholder = () => {
  const placeholders: Record<string, string> = {
    pdb: '输入PDB ID，如: 6M0J',
    uniprot: '输入UniProt ID，如: P0DTC2',
    smiles: '输入SMILES表达式'
  }
  return placeholders[selectedInputType.value] || ''
}

const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    uploadedFileName.value = target.files[0].name
  }
}

const clearFile = () => {
  uploadedFileName.value = ''
}

const getConfidenceColor = (score: number): string => {
  if (score >= 0.8) return '#4CAF50'
  if (score >= 0.6) return '#FF9800'
  return '#F44336'
}

const getConfidenceText = (level: string): string => {
  const texts: Record<string, string> = {
    high: '高',
    medium: '中',
    low: '低'
  }
  return texts[level] || level
}

const getInteractionColor = (type: string): string => {
  const colors: Record<string, string> = {
    hydrogen_bond: 'rgba(76, 175, 80, 0.15)',
    hydrophobic: 'rgba(255, 152, 0, 0.15)',
    ionic: 'rgba(244, 67, 54, 0.15)',
    pi_pi: 'rgba(156, 39, 176, 0.15)',
    metal: 'rgba(33, 150, 243, 0.15)'
  }
  return colors[type] || 'rgba(138, 138, 154, 0.15)'
}

const getInteractionTypeName = (type: string): string => {
  const types: Record<string, string> = {
    hydrogen_bond: '氢键',
    hydrophobic: '疏水',
    ionic: '离子',
    pi_pi: 'π-π',
    metal: '金属'
  }
  return types[type] || type
}

const handleDemoPredict = async () => {
  isLoading.value = true
  
  await new Promise(resolve => setTimeout(resolve, 1500))
  
  demoResult.value = {
    id: 'demo_result',
    targetId: 'P0DTC2',
    targetName: 'ACE2',
    ligandSmiles: 'C(=O)(C(=O)O)NC(CCC(=O)O)C(=O)O',
    bindingAffinity: -8.5 + Math.random() * (-2),
    confidenceScore: 0.85 + Math.random() * 0.1,
    confidenceLevel: 'high',
    interactions: [
      { type: 'hydrogen_bond', residueName: 'ASP', residueNumber: 30, distance: 2.8 },
      { type: 'hydrogen_bond', residueName: 'GLN', residueNumber: 24, distance: 3.1 },
      { type: 'hydrophobic', residueName: 'PHE', residueNumber: 45, distance: 4.2 },
      { type: 'ionic', residueName: 'LYS', residueNumber: 19, distance: 3.5 }
    ],
    createdAt: new Date().toISOString(),
    datasetInfo: {
      name: '演示数据集',
      size: 10000,
      description: '演示数据',
      source: 'internal'
    }
  }
  
  isLoading.value = false
}

const handlePredict = async () => {
  if (!isValidInput.value) return
  
  isLoading.value = true
  
  await new Promise(resolve => setTimeout(resolve, 2000))
  
  demoResult.value = mockResults[0]
  
  isLoading.value = false
}
</script>

<style lang="scss" scoped>
.predict {
  display: flex;
  min-height: 100vh;
  background: $bg-secondary;
}

.predict__content {
  flex: 1;
  margin-left: $sidebar-width;
  padding: $spacing-lg;
}

.predict__header {
  margin-bottom: $spacing-xl;
}

.predict__title {
  font-size: 24px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.predict__subtitle {
  font-size: $font-size-sm;
  color: $text-muted;
}

.predict__type-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-lg;
  margin-bottom: $spacing-xl;
}

.predict__type-card {
  background: $bg-primary;
  padding: $spacing-xl;
  border-radius: $border-radius-lg;
  text-align: center;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all $transition-normal;
  
  &:hover {
    border-color: $border-color;
    box-shadow: $shadow-sm;
  }
  
  &--active {
    border-color: $primary-color;
    background: rgba($primary-color, 0.02);
  }
}

.predict__type-icon {
  font-size: 36px;
  margin-bottom: $spacing-md;
}

.predict__type-name {
  display: block;
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.predict__type-desc {
  display: block;
  font-size: $font-size-xs;
  color: $text-muted;
}

.predict__form {
  max-width: 600px;
  margin: 0 auto $spacing-xl;
}

.predict__form-card {
  background: $bg-primary;
  border-radius: $border-radius-lg;
  padding: $spacing-xl;
  border: 1px solid $border-light;
}

.predict__form-header {
  margin-bottom: $spacing-xl;
}

.predict__form-title {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
}

.predict__input-section {
  margin-bottom: $spacing-lg;
}

.predict__label {
  display: block;
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.predict__input-type-group {
  display: flex;
  gap: $spacing-sm;
}

.predict__input-type-btn {
  flex: 1;
  padding: $spacing-sm $spacing-md;
  border: 1px solid $border-color;
  border-radius: $border-radius-md;
  background: $bg-secondary;
  color: $text-secondary;
  font-size: $font-size-sm;
  cursor: pointer;
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

.predict__input {
  width: 100%;
  padding: $spacing-md;
  border: 1px solid $border-color;
  border-radius: $border-radius-md;
  font-size: $font-size-base;
  background: $bg-secondary;
  transition: border-color $transition-fast;
  
  &:focus {
    outline: none;
    border-color: $primary-color;
  }
}

.predict__file-upload {
  border: 2px dashed $border-color;
  border-radius: $border-radius-lg;
  overflow: hidden;
}

.predict__file-input {
  display: none;
}

.predict__file-upload-area {
  padding: $spacing-2xl;
  text-align: center;
  cursor: pointer;
  transition: all $transition-fast;
  
  &:hover {
    background: $bg-secondary;
  }
}

.predict__file-icon {
  display: block;
  font-size: 32px;
  margin-bottom: $spacing-sm;
}

.predict__file-text {
  font-size: $font-size-sm;
  color: $text-muted;
}

.predict__uploaded-file {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: rgba($success-color, 0.1);
  border-radius: $border-radius-md;
  margin-top: $spacing-md;
}

.predict__uploaded-icon {
  color: $success-color;
}

.predict__uploaded-name {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-primary;
}

.predict__uploaded-remove {
  background: transparent;
  border: none;
  color: $error-color;
  font-size: $font-size-xs;
  cursor: pointer;
}

.predict__options {
  margin-bottom: $spacing-xl;
}

.predict__options-toggle {
  background: transparent;
  border: none;
  color: $primary-color;
  font-size: $font-size-sm;
  cursor: pointer;
  padding: $spacing-xs 0;
}

.predict__advanced-options {
  margin-top: $spacing-md;
  padding: $spacing-lg;
  background: $bg-secondary;
  border-radius: $border-radius-md;
}

.predict__advanced-option {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.predict__range-input {
  flex: 1;
  height: 6px;
  border-radius: 3px;
  background: $bg-tertiary;
  appearance: none;
  
  &::-webkit-slider-thumb {
    appearance: none;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: $primary-color;
    cursor: pointer;
  }
}

.predict__range-value {
  font-size: $font-size-sm;
  color: $text-primary;
  font-weight: 500;
  width: 40px;
  text-align: right;
}

.predict__checkbox {
  width: 18px;
  height: 18px;
  border-radius: $border-radius-sm;
  cursor: pointer;
}

.predict__actions {
  display: flex;
  gap: $spacing-md;
}

.predict__btn {
  flex: 1;
  padding: $spacing-md;
  border-radius: $border-radius-md;
  font-size: $font-size-base;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all $transition-fast;
  
  &--primary {
    background: $primary-color;
    color: #ffffff;
    
    &:hover:not(:disabled) {
      background: $primary-dark;
    }
    
    &:disabled {
      background: $border-color;
      cursor: not-allowed;
    }
  }
  
  &--outline {
    background: transparent;
    color: $primary-color;
    border: 1px solid $border-color;
    
    &:hover {
      background: rgba($primary-color, 0.05);
    }
  }
  
  &--secondary {
    background: $bg-secondary;
    color: $text-primary;
    
    &:hover {
      background: $bg-tertiary;
    }
  }
}

.predict__result {
  max-width: 800px;
  margin: 0 auto;
}

.predict__result-card {
  background: $bg-primary;
  border-radius: $border-radius-lg;
  padding: $spacing-xl;
  border: 1px solid $border-light;
}

.predict__result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-xl;
}

.predict__result-title {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
}

.predict__result-badge {
  font-size: $font-size-xs;
  font-weight: 500;
  padding: 3px 10px;
  border-radius: 100px;
  background: rgba($info-color, 0.1);
  color: $info-color;
  
  &--high {
    background: rgba($success-color, 0.1);
    color: $success-color;
  }
  
  &--medium {
    background: rgba($warning-color, 0.1);
    color: $warning-color;
  }
  
  &--low {
    background: rgba($error-color, 0.1);
    color: $error-color;
  }
}

.predict__result-main {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-lg;
  margin-bottom: $spacing-xl;
  padding: $spacing-lg;
  background: $bg-secondary;
  border-radius: $border-radius-md;
}

.predict__result-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.predict__result-label {
  font-size: $font-size-xs;
  color: $text-muted;
}

.predict__result-value {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  
  &.highlight {
    color: $primary-color;
    font-size: 24px;
  }
}

.predict__result-interactions {
  margin-bottom: $spacing-xl;
}

.predict__interaction-list {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.predict__interaction-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  background: $bg-secondary;
  border-radius: $border-radius-md;
}

.predict__interaction-type {
  font-size: $font-size-xs;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: $border-radius-sm;
  color: $text-secondary;
}

.predict__interaction-residue {
  font-size: $font-size-xs;
  color: $text-primary;
}

.predict__interaction-distance {
  font-size: $font-size-xs;
  color: $text-muted;
}

.predict__result-actions {
  display: flex;
  gap: $spacing-md;
}
</style>