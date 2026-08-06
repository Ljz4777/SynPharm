<template>
  <div class="predict">
    <Sidebar />
    <main class="predict__content">
      <header class="predict__header">
        <div class="predict__header-left">
          <h1 class="predict__title">AI 预测中心</h1>
          <p class="predict__subtitle">药物相互作用智能预测平台</p>
        </div>
        <div class="predict__header-stats">
          <div class="predict__stat-item">
            <span class="predict__stat-value">2,500+</span>
            <span class="predict__stat-label">预测任务</span>
          </div>
          <div class="predict__stat-item">
            <span class="predict__stat-value">98%</span>
            <span class="predict__stat-label">准确率</span>
          </div>
        </div>
      </header>
      
      <section class="predict__type-selector">
        <div 
          v-for="type in predictionTypes" 
          :key="type.value"
          @click="selectedType = type.value"
          class="predict__type-card"
          :class="{ 'predict__type-card--active': selectedType === type.value }"
        >
          <div class="predict__type-icon-wrapper">
            <span class="predict__type-icon">{{ type.icon }}</span>
          </div>
          <div class="predict__type-info">
            <span class="predict__type-name">{{ type.label }}</span>
            <span class="predict__type-desc">{{ type.description }}</span>
          </div>
          <div class="predict__type-arrow">
            <svg v-if="selectedType === type.value" class="predict__check-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
          </div>
        </div>
      </section>
      
      <section class="predict__form">
        <div class="predict__form-card">
          <div class="predict__form-header">
            <div class="predict__form-header-left">
              <h2 class="predict__form-title">输入数据</h2>
              <span class="predict__form-subtitle">{{ getFormSubtitle() }}</span>
            </div>
            <button 
              @click="handleDemoPredict" 
              class="predict__demo-btn"
            >
              <svg class="predict__demo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <path d="M15 9l-6 6"></path>
                <path d="M9 9l6 6"></path>
              </svg>
              使用演示数据
            </button>
          </div>
          
          <div class="predict__input-wrapper">
            <div class="predict__input-column">
              <div class="predict__input-group">
                <div class="predict__input-group-header">
                  <label class="predict__label">{{ getFirstInputLabel() }}</label>
                  <button 
                    @click="clearFirstInput" 
                    v-if="firstInputValue"
                    class="predict__input-clear"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="18" y1="6" x2="6" y2="18"></line>
                      <line x1="6" y1="6" x2="18" y2="18"></line>
                    </svg>
                  </button>
                </div>
                <input 
                  v-model="firstInputValue"
                  type="text"
                  class="predict__input"
                  :class="{ 'predict__input--error': firstInputError }"
                  :placeholder="getFirstInputPlaceholder()"
                  @input="validateFirstInput"
                />
                <span v-if="firstInputError" class="predict__input-error">{{ firstInputError }}</span>
                <span v-if="!firstInputError && firstInputValue" class="predict__input-valid">✓ 格式有效</span>
              </div>
              
              <div class="predict__input-divider">
                <span class="predict__divider-text">VS</span>
              </div>
              
              <div class="predict__input-group">
                <div class="predict__input-group-header">
                  <label class="predict__label">{{ getSecondInputLabel() }}</label>
                  <button 
                    @click="clearSecondInput" 
                    v-if="secondInputValue"
                    class="predict__input-clear"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="18" y1="6" x2="6" y2="18"></line>
                      <line x1="6" y1="6" x2="18" y2="18"></line>
                    </svg>
                  </button>
                </div>
                <input 
                  v-model="secondInputValue"
                  type="text"
                  class="predict__input"
                  :class="{ 'predict__input--error': secondInputError }"
                  :placeholder="getSecondInputPlaceholder()"
                  @input="validateSecondInput"
                />
                <span v-if="secondInputError" class="predict__input-error">{{ secondInputError }}</span>
                <span v-if="!secondInputError && secondInputValue" class="predict__input-valid">✓ 格式有效</span>
              </div>
            </div>
          </div>
          
          <div class="predict__options">
            <button 
              @click="showAdvancedOptions = !showAdvancedOptions"
              class="predict__options-toggle"
            >
              <svg class="predict__toggle-icon" :class="{ 'predict__toggle-icon--rotated': showAdvancedOptions }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="6 9 12 15 18 9"></polyline>
              </svg>
              {{ showAdvancedOptions ? '收起' : '展开' }}高级选项
            </button>
            
            <div v-if="showAdvancedOptions" class="predict__advanced-options">
              <div class="predict__advanced-row">
                <div class="predict__advanced-option">
                  <div class="predict__advanced-option-header">
                    <label class="predict__label">置信度阈值</label>
                    <span class="predict__range-value">{{ confidenceThreshold }}%</span>
                  </div>
                  <input 
                    v-model="confidenceThreshold"
                    type="range"
                    min="0"
                    max="100"
                    class="predict__range-input"
                  />
                  <div class="predict__range-labels">
                    <span>0%</span>
                    <span>50%</span>
                    <span>100%</span>
                  </div>
                </div>
                <div class="predict__advanced-option">
                  <div class="predict__advanced-option-header">
                    <label class="predict__label">输出详细结果</label>
                    <label class="predict__checkbox-wrapper">
                      <input 
                        v-model="detailedOutput"
                        type="checkbox"
                        class="predict__checkbox"
                      />
                      <span class="predict__checkbox-custom"></span>
                    </label>
                  </div>
                  <span class="predict__advanced-option-desc">包含完整的相互作用分析和可视化数据</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="predict__actions">
            <button 
              @click="handlePredict" 
              class="predict__btn predict__btn--primary"
              :disabled="!isValidInput || isLoading"
            >
              <svg v-if="isLoading" class="predict__btn-icon predict__btn-icon--loading" viewBox="0 0 24 24">
                <circle class="predict__loading-spinner" cx="12" cy="12" r="10" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
              </svg>
              {{ isLoading ? '预测中...' : '开始预测' }}
            </button>
          </div>
          <div v-if="predictError" class="predict__error">{{ predictError }}</div>
        </div>
      </section>
      
      <section v-if="predictionResult" class="predict__result">
        <div class="predict__result-card">
          <div class="predict__result-header">
            <h3 class="predict__result-title">预测结果</h3>
            <div class="predict__result-header-right">
              <span class="predict__result-badge">
                {{ predictionResult.datasetInfo.name }}
              </span>
              <span class="predict__result-time">{{ formatTime(predictionResult.createdAt) }}</span>
            </div>
          </div>
          
          <div class="predict__result-summary">
            <div class="predict__result-main">
              <div class="predict__result-score-card">
                <div class="predict__score-label">结合概率</div>
                <div class="predict__score-ring">
                  <svg class="predict__score-ring-svg" viewBox="0 0 120 120">
                    <circle class="predict__score-ring-bg" cx="60" cy="60" r="50" fill="none" stroke="#e2e8f0" stroke-width="8" />
                    <circle 
                      class="predict__score-ring-progress" 
                      cx="60" cy="60" r="50" fill="none" 
                      :stroke="getConfidenceColor(predictionResult.confidenceScore)" 
                      stroke-width="8" 
                      stroke-linecap="round"
                      :stroke-dasharray="`${predictionResult.confidenceScore * 314} 314`"
                      transform="rotate(-90 60 60)"
                    />
                  </svg>
                  <div class="predict__score-value">
                    {{ Math.round(predictionResult.confidenceScore * 100) }}%
                  </div>
                </div>
                <div class="predict__score-badge" :style="{ background: getConfidenceBgColor(predictionResult.confidenceLevel), color: getConfidenceColor(predictionResult.confidenceScore) }">
                  {{ getConfidenceText(predictionResult.confidenceLevel) }}置信度
                </div>
              </div>
              
              <div class="predict__result-details">
                <div class="predict__result-detail-item">
                  <svg class="predict__detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                  <div class="predict__detail-content">
                    <span class="predict__detail-label">靶点名称</span>
                    <span class="predict__detail-value">{{ predictionResult.targetName }}</span>
                  </div>
                </div>
                <div class="predict__result-detail-item">
                  <svg class="predict__detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 20V10"></path>
                    <path d="M18 20V4"></path>
                    <path d="M6 20v-6"></path>
                  </svg>
                  <div class="predict__detail-content">
                    <span class="predict__detail-label">靶点ID</span>
                    <span class="predict__detail-value">{{ predictionResult.targetId }}</span>
                  </div>
                </div>
                <div class="predict__result-detail-item predict__result-detail-item--highlight">
                  <svg class="predict__detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="12" y1="2" x2="12" y2="22"></line>
                    <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                  </svg>
                  <div class="predict__detail-content">
                    <span class="predict__detail-label">结合亲和力</span>
                    <span class="predict__detail-value predict__detail-value--highlight">
                      {{ predictionResult.bindingAffinity.toFixed(2) }} <span class="predict__detail-unit">kcal/mol</span>
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="predict__result-section">
            <h4 class="predict__result-section-title">相互作用分析</h4>
            <div class="predict__interaction-list">
              <div 
                v-for="(interaction, index) in predictionResult.interactions" 
                :key="index"
                class="predict__interaction-item"
              >
                <div class="predict__interaction-type" :style="{ background: getInteractionBgColor(interaction.type), color: getInteractionColor(interaction.type) }">
                  <span class="predict__interaction-icon">{{ getInteractionIcon(interaction.type) }}</span>
                  {{ getInteractionTypeName(interaction.type) }}
                </div>
                <div class="predict__interaction-info">
                  <span class="predict__interaction-residue">{{ interaction.residueName }} {{ interaction.residueNumber }}</span>
                  <span class="predict__interaction-distance">距离: {{ interaction.distance }} Å</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="predict__result-section">
            <h4 class="predict__result-section-title">数据集信息</h4>
            <div class="predict__dataset-info">
              <div class="predict__dataset-item">
                <span class="predict__dataset-label">数据集名称</span>
                <span class="predict__dataset-value">{{ predictionResult.datasetInfo.name }}</span>
              </div>
              <div class="predict__dataset-item">
                <span class="predict__dataset-label">数据集大小</span>
                <span class="predict__dataset-value">{{ formatNumber(predictionResult.datasetInfo.size) }} 条记录</span>
              </div>
              <div class="predict__dataset-item">
                <span class="predict__dataset-label">数据源</span>
                <span class="predict__dataset-value">{{ predictionResult.datasetInfo.description }}</span>
              </div>
            </div>
          </div>
          
          <div class="predict__result-actions">
            <button class="predict__btn predict__btn--secondary">
              <svg class="predict__btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="7 10 12 15 17 10"></polyline>
                <line x1="12" y1="15" x2="12" y2="3"></line>
              </svg>
              保存结果
            </button>
            <button class="predict__btn predict__btn--primary">
              <svg class="predict__btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
                <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
                <line x1="12" y1="22.08" x2="12" y2="12"></line>
              </svg>
              3D可视化
            </button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { predictApi } from '@/api/predict'
import Sidebar from '@/components/Sidebar.vue'
import type { PredictionResult } from '@/types'

const selectedType = ref<'ppi' | 'dti' | 'ddi'>('dti')
const selectedInputType = ref<'pdb' | 'uniprot' | 'smiles' | 'csv'>('smiles')

const firstInputValue = ref('')
const secondInputValue = ref('')

const firstInputError = ref('')
const secondInputError = ref('')

const confidenceThreshold = ref(70)
const detailedOutput = ref(true)
const showAdvancedOptions = ref(false)
const isLoading = ref(false)
const predictionResult = ref<PredictionResult | null>(null)
const predictError = ref('')

const predictionTypes: Array<{ value: 'ppi' | 'dti' | 'ddi', label: string, icon: string, description: string }> = [
  { value: 'ppi', label: 'PPI预测', icon: '🔬', description: '蛋白质-蛋白质相互作用' },
  { value: 'dti', label: 'DTI预测', icon: '💊', description: '药物-靶点相互作用' },
  { value: 'ddi', label: 'DDI预测', icon: '⚗️', description: '药物-药物相互作用' }
]

const isValidInput = computed(() => {
  return firstInputValue.value.trim() !== '' && secondInputValue.value.trim() !== '' && 
         !firstInputError.value && !secondInputError.value
})

const SMILES_REGEX = /^[A-Za-z0-9@+\-\[\]\(\)\{\}.=#$%&\/\\<>~`'*:;]+$/
const PDB_REGEX = /^[0-9A-Za-z]{4}$/
const UNIPROT_REGEX = /^[A-Z0-9]{6,10}$/

const validateSMILES = (value: string): string => {
  if (!value) return ''
  if (value.length < 3) return 'SMILES表达式过短'
  if (!SMILES_REGEX.test(value)) return 'SMILES格式无效，请检查输入'
  return ''
}

const validatePDB = (value: string): string => {
  if (!value) return ''
  if (!PDB_REGEX.test(value)) return 'PDB ID应为4位字母数字组合'
  return ''
}

const validateUniProt = (value: string): string => {
  if (!value) return ''
  if (!UNIPROT_REGEX.test(value)) return 'UniProt ID格式无效'
  return ''
}

const validateFirstInput = () => {
  if (selectedType.value === 'dti') {
    firstInputError.value = validateSMILES(firstInputValue.value)
  } else if (selectedType.value === 'ddi') {
    firstInputError.value = validateSMILES(firstInputValue.value)
  } else if (selectedType.value === 'ppi') {
    if (selectedInputType.value === 'pdb') {
      firstInputError.value = validatePDB(firstInputValue.value)
    } else if (selectedInputType.value === 'uniprot') {
      firstInputError.value = validateUniProt(firstInputValue.value)
    } else {
      firstInputError.value = ''
    }
  }
}

const validateSecondInput = () => {
  if (selectedType.value === 'dti') {
    if (selectedInputType.value === 'pdb') {
      secondInputError.value = validatePDB(secondInputValue.value)
    } else if (selectedInputType.value === 'uniprot') {
      secondInputError.value = validateUniProt(secondInputValue.value)
    } else {
      secondInputError.value = ''
    }
  } else if (selectedType.value === 'ddi') {
    secondInputError.value = validateSMILES(secondInputValue.value)
  } else if (selectedType.value === 'ppi') {
    if (selectedInputType.value === 'pdb') {
      secondInputError.value = validatePDB(secondInputValue.value)
    } else if (selectedInputType.value === 'uniprot') {
      secondInputError.value = validateUniProt(secondInputValue.value)
    } else {
      secondInputError.value = ''
    }
  }
}

const clearFirstInput = () => {
  firstInputValue.value = ''
  firstInputError.value = ''
}

const clearSecondInput = () => {
  secondInputValue.value = ''
  secondInputError.value = ''
}

const getFormSubtitle = () => {
  const subtitles: Record<string, string> = {
    ppi: '分析两个蛋白质之间的相互作用关系',
    dti: '预测药物与靶点蛋白的结合能力',
    ddi: '分析两种药物之间的相互作用'
  }
  return subtitles[selectedType.value] || ''
}

const getFirstInputLabel = () => {
  const labels: Record<string, string> = {
    dti: '药物 (SMILES)',
    ddi: '药物 A (SMILES)',
    ppi: `蛋白质 A (${selectedInputType.value === 'pdb' ? 'PDB ID' : 'UniProt ID'})`
  }
  return labels[selectedType.value] || '输入A'
}

const getSecondInputLabel = () => {
  const labels: Record<string, string> = {
    dti: `靶点 (${selectedInputType.value === 'pdb' ? 'PDB ID' : 'UniProt ID'})`,
    ddi: '药物 B (SMILES)',
    ppi: `蛋白质 B (${selectedInputType.value === 'pdb' ? 'PDB ID' : 'UniProt ID'})`
  }
  return labels[selectedType.value] || '输入B'
}

const getFirstInputPlaceholder = () => {
  const placeholders: Record<string, string> = {
    dti: '输入药物SMILES表达式，如: CC(=O)OC1=CC=CC=C1C(=O)O',
    ddi: '输入药物A的SMILES表达式',
    ppi: selectedInputType.value === 'pdb' ? '输入PDB ID，如: 6M0J' : '输入UniProt ID，如: P0DTC2'
  }
  return placeholders[selectedType.value] || ''
}

const getSecondInputPlaceholder = () => {
  const placeholders: Record<string, string> = {
    dti: selectedInputType.value === 'pdb' ? '输入PDB ID，如: 6M0J' : '输入UniProt ID，如: P0DTC2',
    ddi: '输入药物B的SMILES表达式',
    ppi: selectedInputType.value === 'pdb' ? '输入PDB ID，如: 6LU7' : '输入UniProt ID，如: P05067'
  }
  return placeholders[selectedType.value] || ''
}

const getConfidenceColor = (score: number): string => {
  if (score >= 0.8) return '#10b981'
  if (score >= 0.6) return '#f59e0b'
  return '#ef4444'
}

const getConfidenceBgColor = (level: string): string => {
  const colors: Record<string, string> = {
    high: 'rgba(16, 185, 129, 0.1)',
    medium: 'rgba(245, 158, 11, 0.1)',
    low: 'rgba(239, 68, 68, 0.1)'
  }
  return colors[level] || 'rgba(148, 163, 184, 0.1)'
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
    hydrogen_bond: '#10b981',
    hydrophobic: '#f59e0b',
    ionic: '#ef4444',
    pi_pi: '#8b5cf6',
    metal: '#3b82f6'
  }
  return colors[type] || '#64748b'
}

const getInteractionBgColor = (type: string): string => {
  const colors: Record<string, string> = {
    hydrogen_bond: 'rgba(16, 185, 129, 0.1)',
    hydrophobic: 'rgba(245, 158, 11, 0.1)',
    ionic: 'rgba(239, 68, 68, 0.1)',
    pi_pi: 'rgba(139, 92, 246, 0.1)',
    metal: 'rgba(59, 130, 246, 0.1)'
  }
  return colors[type] || 'rgba(100, 116, 139, 0.1)'
}

const getInteractionIcon = (type: string): string => {
  const icons: Record<string, string> = {
    hydrogen_bond: 'H',
    hydrophobic: '◉',
    ionic: '⚡',
    pi_pi: 'π',
    metal: '⊕'
  }
  return icons[type] || '●'
}

const getInteractionTypeName = (type: string): string => {
  const types: Record<string, string> = {
    hydrogen_bond: '氢键',
    hydrophobic: '疏水',
    ionic: '离子',
    pi_pi: 'π-π堆积',
    metal: '金属配位'
  }
  return types[type] || type
}

const formatTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { 
    month: 'short', 
    day: 'numeric', 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

const handleDemoPredict = async () => {
  // 根据当前预测类型填充演示输入
  if (selectedType.value === 'dti') {
    firstInputValue.value = 'C(=O)(C(=O)O)NC(CCC(=O)O)C(=O)O'
    secondInputValue.value = 'P0DTC2'
  } else if (selectedType.value === 'ppi') {
    firstInputValue.value = 'MGLGLGQ'
    secondInputValue.value = 'MVHLTEK'
  } else {
    firstInputValue.value = 'CC(=O)OC1=CC=CC=C1C(=O)O'
    secondInputValue.value = 'C1CCCCC1'
  }
  firstInputError.value = ''
  secondInputError.value = ''
  
  await handlePredict()
}

const handlePredict = async () => {
  if (!isValidInput.value) return
  
  isLoading.value = true
  predictError.value = ''
  predictionResult.value = null
  
  try {
    let response
    if (selectedType.value === 'dti') {
      response = await predictApi.predictDTI({
        smiles: firstInputValue.value.trim(),
        targetId: secondInputValue.value.trim()
      })
    } else if (selectedType.value === 'ppi') {
      response = await predictApi.predictPPI({
        proteinA: firstInputValue.value.trim(),
        proteinB: secondInputValue.value.trim()
      })
    } else {
      response = await predictApi.predictDDI({
        drugASmiles: firstInputValue.value.trim(),
        drugBSmiles: secondInputValue.value.trim()
      })
    }
    predictionResult.value = response as unknown as PredictionResult
  } catch (error: unknown) {
    predictError.value = error instanceof Error ? error.message : '预测失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
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
  padding: $spacing-xl;
}

.predict__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-2xl;
  padding: $spacing-xl;
  background: linear-gradient(135deg, $primary-color 0%, $primary-light 100%);
  border-radius: $border-radius-xl;
  color: #ffffff;
}

.predict__header-left {
  flex: 1;
}

.predict__title {
  font-size: $font-size-2xl;
  font-weight: 700;
  margin-bottom: $spacing-xs;
}

.predict__subtitle {
  font-size: $font-size-sm;
  opacity: 0.8;
}

.predict__header-stats {
  display: flex;
  gap: $spacing-xl;
}

.predict__stat-item {
  text-align: center;
}

.predict__stat-value {
  display: block;
  font-size: $font-size-xl;
  font-weight: 700;
}

.predict__stat-label {
  font-size: $font-size-xs;
  opacity: 0.8;
}

.predict__type-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-lg;
  margin-bottom: $spacing-2xl;
}

.predict__type-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  background: $bg-primary;
  padding: $spacing-lg;
  border-radius: $border-radius-lg;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all $transition-normal;
  
  &:hover {
    border-color: $border-color;
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }
  
  &--active {
    border-color: $primary-color;
    background: rgba($primary-color, 0.03);
    box-shadow: $shadow-md;
  }
}

.predict__type-icon-wrapper {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-secondary;
  border-radius: $border-radius-md;
  flex-shrink: 0;
}

.predict__type-icon {
  font-size: 24px;
}

.predict__type-info {
  flex: 1;
}

.predict__type-name {
  display: block;
  font-size: $font-size-base;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 2px;
}

.predict__type-desc {
  display: block;
  font-size: $font-size-xs;
  color: $text-muted;
}

.predict__type-arrow {
  color: $text-muted;
}

.predict__check-icon {
  width: 16px;
  height: 16px;
  color: $success-color;
}

.predict__form {
  max-width: 800px;
  margin: 0 auto $spacing-2xl;
}

.predict__form-card {
  background: $bg-primary;
  border-radius: $border-radius-xl;
  padding: $spacing-2xl;
  border: 1px solid $border-light;
  box-shadow: $shadow-sm;
}

.predict__form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-2xl;
  padding-bottom: $spacing-xl;
  border-bottom: 1px solid $border-light;
}

.predict__form-header-left {
  flex: 1;
}

.predict__form-title {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.predict__form-subtitle {
  font-size: $font-size-sm;
  color: $text-muted;
}

.predict__demo-btn {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-md;
  background: rgba($info-color, 0.1);
  color: $info-color;
  border: none;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  cursor: pointer;
  transition: all $transition-fast;
  
  &:hover {
    background: rgba($info-color, 0.15);
  }
}

.predict__demo-icon {
  width: 14px;
  height: 14px;
}

.predict__input-wrapper {
  margin-bottom: $spacing-xl;
}

.predict__input-column {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: $spacing-md;
  align-items: start;
}

.predict__input-group {
  &--single {
    width: 100%;
  }
}

.predict__input-group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.predict__label {
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-primary;
}

.predict__input-clear {
  background: transparent;
  border: none;
  color: $text-muted;
  cursor: pointer;
  padding: 0;
  
  svg {
    width: 14px;
    height: 14px;
  }
  
  &:hover {
    color: $text-secondary;
  }
}

.predict__input-type-group {
  display: flex;
  gap: $spacing-xs;
}

.predict__input-type-btn {
  padding: $spacing-xs $spacing-sm;
  border: 1px solid $border-color;
  border-radius: $border-radius-sm;
  background: $bg-secondary;
  color: $text-secondary;
  font-size: $font-size-xs;
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
  border: 1.5px solid $border-color;
  border-radius: $border-radius-md;
  font-size: $font-size-base;
  background: $bg-secondary;
  transition: all $transition-fast;
  
  &:focus {
    outline: none;
    border-color: $primary-color;
    box-shadow: 0 0 0 3px rgba($primary-color, 0.05);
  }
  
  &--error {
    border-color: $error-color;
    
    &:focus {
      box-shadow: 0 0 0 3px rgba($error-color, 0.1);
    }
  }
}

.predict__input-error {
  display: block;
  font-size: $font-size-xs;
  color: $error-color;
  margin-top: $spacing-xs;
}

.predict__input-valid {
  display: block;
  font-size: $font-size-xs;
  color: $success-color;
  margin-top: $spacing-xs;
}

.predict__input-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-lg 0;
}

.predict__divider-text {
  font-size: $font-size-xs;
  font-weight: 600;
  color: $text-muted;
  padding: $spacing-xs $spacing-sm;
  background: $bg-secondary;
  border-radius: $border-radius-sm;
}

.predict__options {
  margin-bottom: $spacing-xl;
}

.predict__options-toggle {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  background: transparent;
  border: none;
  color: $primary-color;
  font-size: $font-size-sm;
  cursor: pointer;
  padding: $spacing-xs 0;
}

.predict__toggle-icon {
  width: 14px;
  height: 14px;
  transition: transform $transition-fast;
  
  &--rotated {
    transform: rotate(180deg);
  }
}

.predict__advanced-options {
  margin-top: $spacing-md;
  padding: $spacing-xl;
  background: $bg-secondary;
  border-radius: $border-radius-lg;
}

.predict__advanced-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-xl;
}

.predict__advanced-option {
  &:last-child {
    margin-bottom: 0;
  }
}

.predict__advanced-option-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.predict__range-input {
  width: 100%;
  height: 6px;
  border-radius: 3px;
  background: $bg-tertiary;
  appearance: none;
  cursor: pointer;
  
  &::-webkit-slider-thumb {
    appearance: none;
    width: 18px;
    height: 18px;
    border-radius: 50%;
    background: $primary-color;
    cursor: pointer;
    border: 3px solid #ffffff;
    box-shadow: $shadow-sm;
    transition: transform $transition-fast;
    
    &:hover {
      transform: scale(1.1);
    }
  }
}

.predict__range-labels {
  display: flex;
  justify-content: space-between;
  font-size: $font-size-xs;
  color: $text-muted;
  margin-top: $spacing-xs;
}

.predict__range-value {
  font-size: $font-size-sm;
  color: $text-primary;
  font-weight: 600;
  width: 50px;
  text-align: right;
}

.predict__checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  cursor: pointer;
}

.predict__checkbox {
  display: none;
}

.predict__checkbox-custom {
  width: 18px;
  height: 18px;
  border: 2px solid $border-color;
  border-radius: $border-radius-sm;
  position: relative;
  transition: all $transition-fast;
  
  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%) rotate(-45deg);
    width: 6px;
    height: 10px;
    border-left: 2px solid transparent;
    border-bottom: 2px solid transparent;
    transition: all $transition-fast;
  }
}

.predict__checkbox:checked + .predict__checkbox-custom {
  background: $primary-color;
  border-color: $primary-color;
  
  &::before {
    border-left-color: #ffffff;
    border-bottom-color: #ffffff;
  }
}

.predict__advanced-option-desc {
  font-size: $font-size-xs;
  color: $text-muted;
}

.predict__actions {
  display: flex;
  gap: $spacing-md;
}

.predict__error {
  margin-top: $spacing-md;
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-md;
  background: rgba(239, 68, 68, 0.1);
  color: $error-color;
  font-size: $font-size-sm;
  text-align: center;
}

.predict__btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
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
      transform: translateY(-1px);
      box-shadow: $shadow-md;
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

.predict__btn-icon {
  width: 16px;
  height: 16px;
  
  &--loading {
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.predict__loading-spinner {
  stroke-dasharray: 283;
  stroke-dashoffset: 70;
  animation: spin 1s linear infinite;
}

.predict__result {
  max-width: 900px;
  margin: 0 auto;
}

.predict__result-card {
  background: $bg-primary;
  border-radius: $border-radius-xl;
  padding: $spacing-2xl;
  border: 1px solid $border-light;
  box-shadow: $shadow-sm;
}

.predict__result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-xl;
}

.predict__result-header-right {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.predict__result-title {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
}

.predict__result-badge {
  font-size: $font-size-xs;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 100px;
  background: rgba($info-color, 0.1);
  color: $info-color;
}

.predict__result-time {
  font-size: $font-size-xs;
  color: $text-muted;
}

.predict__result-summary {
  margin-bottom: $spacing-xl;
}

.predict__result-main {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: $spacing-xl;
}

.predict__result-score-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-xl;
  background: linear-gradient(135deg, $bg-secondary 0%, $bg-tertiary 100%);
  border-radius: $border-radius-lg;
}

.predict__score-label {
  font-size: $font-size-sm;
  color: $text-muted;
  margin-bottom: $spacing-md;
}

.predict__score-ring {
  position: relative;
  width: 140px;
  height: 140px;
  margin-bottom: $spacing-md;
}

.predict__score-ring-svg {
  width: 100%;
  height: 100%;
}

.predict__score-ring-bg {
  stroke-width: 10;
}

.predict__score-ring-progress {
  stroke-width: 10;
  transition: stroke-dasharray 0.5s ease;
}

.predict__score-value {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 32px;
  font-weight: 700;
  color: $text-primary;
}

.predict__score-badge {
  font-size: $font-size-xs;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 100px;
}

.predict__result-details {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.predict__result-detail-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: $bg-secondary;
  border-radius: $border-radius-md;
  
  &--highlight {
    background: rgba($primary-color, 0.05);
    border-left: 3px solid $primary-color;
  }
}

.predict__detail-icon {
  width: 20px;
  height: 20px;
  color: $text-muted;
  flex-shrink: 0;
}

.predict__detail-content {
  flex: 1;
}

.predict__detail-label {
  display: block;
  font-size: $font-size-xs;
  color: $text-muted;
  margin-bottom: 2px;
}

.predict__detail-value {
  font-size: $font-size-base;
  font-weight: 600;
  color: $text-primary;
  
  &--highlight {
    font-size: $font-size-lg;
    color: $primary-color;
  }
}

.predict__detail-unit {
  font-size: $font-size-sm;
  font-weight: 400;
  color: $text-muted;
}

.predict__result-section {
  margin-bottom: $spacing-xl;
  padding-top: $spacing-xl;
  border-top: 1px solid $border-light;
  
  &:first-of-type {
    border-top: none;
    padding-top: 0;
  }
}

.predict__result-section-title {
  font-size: $font-size-base;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-lg;
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
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: $font-size-xs;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: $border-radius-sm;
}

.predict__interaction-icon {
  font-size: 10px;
}

.predict__interaction-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.predict__interaction-residue {
  font-size: $font-size-xs;
  color: $text-primary;
  font-weight: 500;
}

.predict__interaction-distance {
  font-size: $font-size-xs;
  color: $text-muted;
}

.predict__dataset-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;
}

.predict__dataset-item {
  padding: $spacing-md;
  background: $bg-secondary;
  border-radius: $border-radius-md;
}

.predict__dataset-label {
  display: block;
  font-size: $font-size-xs;
  color: $text-muted;
  margin-bottom: $spacing-xs;
}

.predict__dataset-value {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $text-primary;
}

.predict__result-actions {
  display: flex;
  gap: $spacing-md;
  padding-top: $spacing-xl;
  border-top: 1px solid $border-light;
}
</style>