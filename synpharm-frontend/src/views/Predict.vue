<template>
  <div class="pc">
    <Sidebar />
    <main class="pc__main">
      <!-- ==================== 页头 ==================== -->
      <header class="pc__header">
        <div class="pc__header-main">
          <h1 class="pc__title">预测中心</h1>
          <p class="pc__subtitle">药物-靶点 · 蛋白-蛋白 · 药物-药物 相互作用智能预测</p>
        </div>
      </header>

      <!-- ==================== 模式切换 ==================== -->
      <div class="pc__tabs">
        <button class="pc__tab" :class="{ 'pc__tab--active': mode === 'single' }" @click="mode = 'single'">单条预测</button>
        <button class="pc__tab" :class="{ 'pc__tab--active': mode === 'batch' }" @click="mode = 'batch'">批量预测</button>
        <button class="pc__tab" :class="{ 'pc__tab--active': mode === 'history' }" @click="mode = 'history'">预测历史</button>
      </div>

      <!-- ===== 单条预测 ===== -->
      <section v-show="mode === 'single'" class="pc__panel">
        <div class="pc__card">
          <!-- 预测类型选择 -->
          <div class="pc__types">
            <button
              v-for="type in predictionTypes"
              :key="type.value"
              class="pc__type"
              :class="{ 'pc__type--active': selectedType === type.value }"
              @click="selectedType = type.value"
            >
              <span class="pc__type-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                  <template v-if="type.value === 'dti'">
                    <path d="M12 3l6.5 3.75v7.5L12 18l-6.5-3.75v-7.5L12 3z" />
                    <circle cx="12" cy="10.5" r="2.5" />
                  </template>
                  <template v-else-if="type.value === 'ppi'">
                    <circle cx="9" cy="12" r="5.5" />
                    <circle cx="15" cy="12" r="5.5" />
                  </template>
                  <template v-else>
                    <rect x="3" y="9" width="8" height="6" rx="3" />
                    <rect x="13" y="9" width="8" height="6" rx="3" />
                  </template>
                </svg>
              </span>
              <span class="pc__type-text">
                <span class="pc__type-name">{{ type.label }}</span>
                <span class="pc__type-desc">{{ type.description }}</span>
              </span>
              <span class="pc__type-check" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
                  <path d="m5 13 4 4L19 7" />
                </svg>
              </span>
            </button>
          </div>

          <p class="pc__form-subtitle">{{ getFormSubtitle() }}</p>

          <div v-if="prefilledTargetName" class="pc__target-tip">
            已选择靶点：<b>{{ prefilledTargetName }}</b>
          </div>

          <div class="pc__form">
            <div class="pc__input-block">
              <div class="pc__input-head">
                <label class="pc__label">{{ getFirstInputLabel() }}</label>
                <button v-if="firstInputValue" class="pc__clear" @click="clearFirstInput" title="清空">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M18 6 6 18M6 6l12 12" /></svg>
                </button>
              </div>
              <input
                v-model="firstInputValue"
                type="text"
                class="pc__input"
                :class="{ 'pc__input--error': firstInputError }"
                :placeholder="getFirstInputPlaceholder()"
                @input="validateFirstInput"
              />
              <div class="pc__hint">
                <span v-if="firstInputError" class="pc__err">{{ firstInputError }}</span>
                <span v-else-if="firstInputValue" class="pc__ok">格式有效</span>
              </div>
            </div>

            <div class="pc__vs"><span>VS</span></div>

            <div class="pc__input-block">
              <div class="pc__input-head">
                <label class="pc__label">{{ getSecondInputLabel() }}</label>
                <button v-if="secondInputValue" class="pc__clear" @click="clearSecondInput" title="清空">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M18 6 6 18M6 6l12 12" /></svg>
                </button>
              </div>
              <input
                v-model="secondInputValue"
                type="text"
                class="pc__input"
                :class="{ 'pc__input--error': secondInputError }"
                :placeholder="getSecondInputPlaceholder()"
                @input="validateSecondInput"
              />
              <div class="pc__hint">
                <span v-if="secondInputError" class="pc__err">{{ secondInputError }}</span>
                <span v-else-if="secondInputValue" class="pc__ok">格式有效</span>
              </div>
            </div>
          </div>

          <div class="pc__advanced">
            <button class="pc__advanced-toggle" @click="showAdvancedOptions = !showAdvancedOptions">
              <span class="pc__chevron" :class="{ 'pc__chevron--open': showAdvancedOptions }">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m6 9 6 6 6-6" /></svg>
              </span>
              {{ showAdvancedOptions ? '收起' : '展开' }}高级选项
            </button>
            <div v-if="showAdvancedOptions" class="pc__advanced-body">
              <div class="pc__advanced-item">
                <div class="pc__advanced-head">
                  <label class="pc__label">置信度阈值</label>
                  <span class="pc__range-val">{{ confidenceThreshold }}%</span>
                </div>
                <input v-model="confidenceThreshold" type="range" min="0" max="100" class="pc__range" />
                <p class="pc__desc">结果低于该阈值时给出提示（前端判定，不改变引擎计算）</p>
              </div>
              <label class="pc__advanced-item pc__advanced-item--row">
                <span class="pc__label">输出详细结果</span>
                <span class="pc__checkbox">
                  <input v-model="detailedOutput" type="checkbox" />
                  <span class="pc__checkbox-box"></span>
                </span>
                <span class="pc__desc">关闭后隐藏“相互作用分析”区块，只保留打分</span>
              </label>
            </div>
          </div>

          <div class="pc__actions">
            <button class="pc__btn pc__btn--ghost" @click="handleDemoPredict">
              <svg class="pc__btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9" /><path d="m10 8 6 4-6 4V8z" /></svg>
              使用演示数据
            </button>
            <button class="pc__btn pc__btn--primary" :disabled="!isValidInput || isLoading" @click="handlePredict">
              <span v-if="isLoading" class="pc__spin"></span>
              {{ isLoading ? '预测中...' : '开始预测' }}
            </button>
          </div>
          <div v-if="predictError" class="pc__error">{{ predictError }}</div>
        </div>
      </section>

      <!-- ===== 批量预测 ===== -->
      <section v-show="mode === 'batch'" class="pc__panel">
        <div class="pc__card">
          <div class="pc__card-head">
            <h3 class="pc__card-title">CSV 批量预测</h3>
            <p class="pc__card-sub">上传 CSV 文件，系统异步处理，支持 DTI / PPI / DDI</p>
          </div>

          <div class="pc__batch-body">
            <select v-model="batchAlgoType" class="pc__select">
              <option value="DTI">DTI 预测</option>
              <option value="PPI">PPI 预测</option>
              <option value="DDI">DDI 预测</option>
            </select>

            <label class="pc__dropzone" :class="{ 'pc__dropzone--has': batchFile }">
              <input type="file" accept=".csv" class="pc__file" @change="handleBatchFileChange" />
              <template v-if="!batchFile">
                <svg class="pc__dropzone-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <path d="M14 2v6h6" />
                </svg>
                <span class="pc__dropzone-text">点击选择 CSV 文件</span>
                <span class="pc__dropzone-sub">每行一个输入（SMILES / 序列）</span>
              </template>
              <template v-else>
                <svg class="pc__dropzone-icon pc__dropzone-icon--ok" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <path d="M14 2v6h6" />
                  <path d="m9 15 2 2 4-4" />
                </svg>
                <span class="pc__dropzone-text">{{ batchFile.name }}</span>
                <span class="pc__dropzone-sub">点击可重新选择</span>
              </template>
            </label>

            <button
              class="pc__btn pc__btn--primary pc__btn--block"
              :disabled="!batchFile || batchUploading"
              @click="handleBatchUpload"
            >
              <span v-if="batchUploading" class="pc__spin"></span>
              {{ batchUploading ? '上传中...' : '开始批量预测' }}
            </button>
          </div>

          <div v-if="batchError" class="pc__error">{{ batchError }}</div>

          <div v-if="batchStatus" class="pc__batch-status">
            <div class="pc__batch-status-row">
              <span class="pc__batch-id">批次 {{ batchStatus.batchId?.substring(0, 8) }}</span>
              <span
                class="pc__batch-state"
                :class="'pc__batch-state--' + batchStatus.status.toLowerCase()"
              >{{ batchStatus.status }}</span>
              <span class="pc__batch-pct">{{ batchStatus.progress }}%</span>
            </div>
            <div class="pc__progress">
              <div class="pc__progress-fill" :style="{ width: (batchStatus.progress || 0) + '%' }"></div>
            </div>
            <button
              v-if="batchStatus.status === 'SUCCESS'"
              class="pc__btn pc__btn--secondary pc__btn--block"
              @click="handleBatchDownload"
            >
              <svg class="pc__btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" /><polyline points="7 10 12 15 17 10" /><line x1="12" y1="15" x2="12" y2="3" /></svg>
              下载结果
            </button>
          </div>
        </div>
      </section>

      <!-- ===== 预测历史 ===== -->
      <section v-show="mode === 'history'" class="pc__panel">
        <div class="pc__card">
          <div class="pc__card-head">
            <h3 class="pc__card-title">预测历史</h3>
            <p class="pc__card-sub">查看当前用户的近期预测记录</p>
          </div>

          <div v-if="historyLoading" class="pc__state">加载中...</div>
          <div v-else-if="historyError" class="pc__state pc__state--error">{{ historyError }}</div>
          <div v-else-if="historyList.length === 0" class="pc__state">暂无预测历史</div>

          <template v-else>
            <div v-for="(item, idx) in historyList" :key="idx" class="pc__history-item">
              <div class="pc__history-head">
                <span class="pc__history-type">{{ getAlgoTypeText(item.algoType) }}</span>
                <span class="pc__history-time">{{ item.createdAt ? formatTime(item.createdAt) : '-' }}</span>
              </div>

              <div class="pc__history-body">
                <div class="pc__history-field">
                  <span class="pc__history-label">靶点名称</span>
                  <span class="pc__history-value">{{ item.targetName || '-' }}</span>
                </div>
                <div v-if="item.ligandSmiles" class="pc__history-field">
                  <span class="pc__history-label">配体 SMILES</span>
                  <span class="pc__history-value pc__history-value--smiles">{{ item.ligandSmiles }}</span>
                </div>
                <div v-if="isDti(item.algoType)" class="pc__history-field">
                  <span class="pc__history-label">结合亲和力</span>
                  <span class="pc__history-value">
                    {{ item.bindingAffinity != null ? item.bindingAffinity.toFixed(2) + ' kcal/mol' : '-' }}
                  </span>
                </div>
                <div class="pc__history-field">
                  <span class="pc__history-label">置信度</span>
                  <span class="pc__history-value">
                    {{ Math.round((item.confidenceScore ?? 0) * 100) }}%
                    <span v-if="item.confidenceLevel" class="pc__history-confidence" :style="{ color: getConfidenceColor(item.confidenceScore ?? 0) }">
                      {{ getConfidenceText(item.confidenceLevel || '') }}
                    </span>
                  </span>
                </div>
              </div>

              <div class="pc__history-actions">
                <button class="pc__btn pc__btn--secondary" @click="goHistoryDetail(item)">查看详情</button>
                <button class="pc__btn pc__btn--primary" @click="goHistoryVisualization(item)">跳转3D</button>
              </div>
            </div>
          </template>
        </div>
      </section>

      <!-- ===== 预测结果（仅单条预测页签内） ===== -->
      <section v-if="mode === 'single' && predictionResult" class="pc__panel">
        <div class="pc__card">
          <div class="pc__result-head">
            <div>
              <h3 class="pc__card-title">预测结果</h3>
              <span class="pc__result-time">{{ formatTime(predictionResult.createdAt) }}</span>
            </div>
            <div class="pc__result-badges">
              <span class="pc__badge">{{ predictionResult.datasetInfo.name }}</span>
              <span
                v-if="belowConfidenceThreshold"
                class="pc__badge pc__badge--warn"
              >低于阈值 {{ confidenceThreshold }}%</span>
            </div>
          </div>

          <div class="pc__result-grid">
            <div class="pc__score">
              <div class="pc__score-ring">
                <svg class="pc__score-svg" viewBox="0 0 120 120">
                  <circle cx="60" cy="60" r="50" fill="none" stroke="#e8edf3" stroke-width="10" />
                  <circle
                    cx="60" cy="60" r="50" fill="none"
                    :stroke="getConfidenceColor(predictionResult.confidenceScore)"
                    stroke-width="10" stroke-linecap="round"
                    :stroke-dasharray="`${predictionResult.confidenceScore * 314} 314`"
                    transform="rotate(-90 60 60)"
                  />
                </svg>
                <div class="pc__score-num">{{ Math.round(predictionResult.confidenceScore * 100) }}%</div>
              </div>
              <span class="pc__score-label">结合概率</span>
              <span
                class="pc__score-level"
                :style="{ color: getConfidenceColor(predictionResult.confidenceScore) }"
              >{{ getConfidenceText(predictionResult.confidenceLevel) }}置信度</span>
            </div>

            <div class="pc__details">
              <div class="pc__detail">
                <span class="pc__detail-label">靶点名称</span>
                <span class="pc__detail-val">{{ predictionResult.targetName }}</span>
              </div>
              <div class="pc__detail">
                <span class="pc__detail-label">靶点 ID</span>
                <span class="pc__detail-val">{{ predictionResult.targetId }}</span>
              </div>
              <div
                v-if="selectedType === 'dti'"
                class="pc__detail pc__detail--accent"
              >
                <span class="pc__detail-label">结合亲和力</span>
                <span class="pc__detail-val">
                  {{ predictionResult.bindingAffinity != null
                    ? predictionResult.bindingAffinity.toFixed(2)
                    : '-' }}
                  <small v-if="predictionResult.bindingAffinity != null">kcal/mol</small>
                </span>
              </div>
            </div>
          </div>

          <div v-if="detailedOutput" class="pc__section">
            <h4 class="pc__section-title">相互作用分析</h4>
            <div class="pc__interactions">
              <div v-for="(it, i) in predictionResult.interactions" :key="i" class="pc__interaction">
                <span
                  class="pc__interaction-tag"
                  :style="{ background: getInteractionBgColor(it.type), color: getInteractionColor(it.type) }"
                >{{ getInteractionTypeName(it.type) }}</span>
                <span class="pc__interaction-meta">{{ it.residueName }} {{ it.residueNumber }} · {{ it.distance }} Å</span>
              </div>
            </div>
          </div>

          <div class="pc__section">
            <h4 class="pc__section-title">数据集信息</h4>
            <div class="pc__dataset">
              <div class="pc__dataset-item"><span>名称</span><b>{{ predictionResult.datasetInfo.name }}</b></div>
              <div class="pc__dataset-item"><span>大小</span><b>{{ formatNumber(predictionResult.datasetInfo.size) }} 条记录</b></div>
              <div class="pc__dataset-item"><span>数据源</span><b>{{ predictionResult.datasetInfo.description }}</b></div>
            </div>
          </div>

          <div class="pc__result-actions">
            <button
              class="pc__btn pc__btn--secondary"
              :disabled="savingResult || savedResult || !predictionResult.id"
              :title="predictionResult.id ? '' : '该结果没有 ID，无法收藏'"
              @click="handleSaveResult"
            >
              <svg class="pc__btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" /><polyline points="7 10 12 15 17 10" /><line x1="12" y1="15" x2="12" y2="3" /></svg>
              {{ savedResult ? '已保存' : (savingResult ? '保存中…' : '保存结果') }}
            </button>
            <button class="pc__btn pc__btn--primary" @click="goToVisualization">
              <svg class="pc__btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
                <polyline points="3.27 6.96 12 12.01 20.73 6.96" />
                <line x1="12" y1="22.08" x2="12" y2="12" />
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
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { predictApi, batchApi, type BatchStatus, type PredictResultResponse } from '@/api/predict'
import { favoriteApi } from '@/api/favorite'
import Sidebar from '@/components/Sidebar.vue'
import type { PredictionResult } from '@/types'

const selectedType = ref<'ppi' | 'dti' | 'ddi'>('dti')
const selectedInputType = ref<'pdb' | 'uniprot' | 'smiles' | 'csv'>('smiles')
const mode = ref<'single' | 'batch' | 'history'>('single')

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
const savingResult = ref(false)
const savedResult = ref(false)

/** 结果置信度是否低于“高级选项”里设定的阈值（纯前端判定，不改引擎计算） */
const belowConfidenceThreshold = computed(() => {
  const r = predictionResult.value
  return r != null && r.confidenceScore < confidenceThreshold.value / 100
})

/** 收藏当前结果（对应后端 POST /api/favorites） */
const handleSaveResult = async () => {
  const r = predictionResult.value
  if (!r || !r.id || savingResult.value || savedResult.value) return
  savingResult.value = true
  predictError.value = ''
  try {
    await favoriteApi.addFavorite(r.id, r.targetName ? `${r.targetName} 预测结果` : undefined)
    savedResult.value = true
  } catch (error: unknown) {
    predictError.value = error instanceof Error ? error.message : '保存失败，请稍后重试'
  } finally {
    savingResult.value = false
  }
}
const router = useRouter()
const route = useRoute()

// 从靶点库跳转进入时，预填靶点信息
const prefilledTargetName = ref('')

const applyTargetFromQuery = () => {
  const targetId = route.query.targetId
  const targetName = route.query.targetName
  if (targetId) {
    selectedType.value = 'dti'
    selectedInputType.value = 'uniprot'
    secondInputValue.value = String(targetId)
    secondInputError.value = ''
    prefilledTargetName.value = targetName ? String(targetName) : ''
  }
}

onMounted(applyTargetFromQuery)

/** 预测类型：图标在模板内用 SVG 区分，避免 emoji */
const predictionTypes: Array<{ value: 'ppi' | 'dti' | 'ddi', label: string, description: string }> = [
  { value: 'dti', label: 'DTI 预测', description: '药物-靶点相互作用' },
  { value: 'ppi', label: 'PPI 预测', description: '蛋白质-蛋白质相互作用' },
  { value: 'ddi', label: 'DDI 预测', description: '药物-药物相互作用' }
]

const isValidInput = computed(() => {
  return firstInputValue.value.trim() !== '' && secondInputValue.value.trim() !== '' &&
         !firstInputError.value && !secondInputError.value
})

const SMILES_REGEX = /^[A-Za-z0-9@+\-\[\]\(\)\{\}.=#$%&\/\\<>~`'*:;]+$/

const validateSMILES = (value: string): string => {
  if (!value) return ''
  if (value.length < 3) return 'SMILES表达式过短'
  if (!SMILES_REGEX.test(value)) return 'SMILES格式无效，请检查输入'
  return ''
}

/**
 * 靶点 / 蛋白输入的宽松校验：后端 PredictionInputResolver 会自动嗅探
 * 氨基酸序列 / UniProt ID / PDB 引用并转成序列，前端只需兜底格式，
 * 不要用 6-10 位 UniProt 正则把真实蛋白序列误判为非法。
 */
const validateProteinInput = (value: string): string => {
  if (!value) return ''
  const v = value.trim()
  if (/^[A-Za-z]+$/.test(v)) return ''            // 氨基酸序列
  if (/^[A-Za-z0-9]{4,10}$/.test(v)) return ''   // UniProt ID / PDB ID
  return '请输入氨基酸序列，或合法的 UniProt ID / PDB ID'
}

const validateFirstInput = () => {
  if (selectedType.value === 'dti') {
    firstInputError.value = validateSMILES(firstInputValue.value)
  } else if (selectedType.value === 'ddi') {
    firstInputError.value = validateSMILES(firstInputValue.value)
  } else if (selectedType.value === 'ppi') {
    firstInputError.value = validateProteinInput(firstInputValue.value)
  }
}

const validateSecondInput = () => {
  if (selectedType.value === 'dti') {
    secondInputError.value = validateProteinInput(secondInputValue.value)
  } else if (selectedType.value === 'ddi') {
    secondInputError.value = validateSMILES(secondInputValue.value)
  } else if (selectedType.value === 'ppi') {
    secondInputError.value = validateProteinInput(secondInputValue.value)
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
    ppi: '蛋白质 A（氨基酸序列）'
  }
  return labels[selectedType.value] || '输入A'
}

const getSecondInputLabel = () => {
  const labels: Record<string, string> = {
    dti: '靶点（UniProt ID / PDB）',
    ddi: '药物 B (SMILES)',
    ppi: '蛋白质 B（氨基酸序列）'
  }
  return labels[selectedType.value] || '输入B'
}

const getFirstInputPlaceholder = () => {
  const placeholders: Record<string, string> = {
    dti: '输入药物SMILES表达式，如: CC(=O)OC1=CC=CC=C1C(=O)O',
    ddi: '输入药物A的SMILES表达式',
    ppi: '输入蛋白质 A 的氨基酸序列（也支持 UniProt ID / PDB ID）'
  }
  return placeholders[selectedType.value] || ''
}

const getSecondInputPlaceholder = () => {
  const placeholders: Record<string, string> = {
    dti: '输入靶点 UniProt ID 或 PDB ID（如 P0DTC2 / 6M0J）',
    ddi: '输入药物B的SMILES表达式',
    ppi: '输入蛋白质 B 的氨基酸序列（也支持 UniProt ID / PDB ID）'
  }
  return placeholders[selectedType.value] || ''
}

const getConfidenceColor = (score: number): string => {
  if (score >= 0.8) return '#10b981'
  if (score >= 0.6) return '#f59e0b'
  return '#ef4444'
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
    firstInputValue.value = 'MKTAYIAKQRQISFVKSHFSRQL'
    secondInputValue.value = 'MSTAGKVIKCKAAVLW'
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

// ================= 批量预测 =================
const batchFile = ref<File | null>(null)
const batchAlgoType = ref('DTI')
const batchUploading = ref(false)
const batchError = ref('')
const batchStatus = ref<BatchStatus | null>(null)
let batchTimer: number | null = null

const handleBatchFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  batchFile.value = input.files?.[0] || null
  batchError.value = ''
}

const handleBatchUpload = async () => {
  if (!batchFile.value) return
  batchUploading.value = true
  batchError.value = ''
  batchStatus.value = null
  try {
    const result = await batchApi.upload(batchFile.value, batchAlgoType.value)
    startBatchPolling(result.batchId)
  } catch (error: unknown) {
    batchError.value = error instanceof Error ? error.message : '批量上传失败'
  } finally {
    batchUploading.value = false
  }
}

const startBatchPolling = (batchId: string) => {
  if (batchTimer) clearInterval(batchTimer)
  batchTimer = window.setInterval(async () => {
    try {
      const status = await batchApi.getStatus(batchId)
      batchStatus.value = status
      if (status.status === 'SUCCESS' || status.status === 'FAIL') {
        if (batchTimer) clearInterval(batchTimer)
        batchTimer = null
      }
    } catch (error: unknown) {
      // 此前是空的 catch，轮询一旦失败就静默停表：进度条永久停在原地，
      // 用户完全不知道发生了什么（问题总账 G-10）
      if (batchTimer) clearInterval(batchTimer)
      batchTimer = null
      batchStatus.value = null
      batchError.value = error instanceof Error
        ? `进度查询失败：${error.message}`
        : '进度查询失败，请稍后重试'
    }
  }, 2000)
}

const handleBatchDownload = async () => {
  if (!batchStatus.value) return
  try {
    await batchApi.download(batchStatus.value.batchId)
  } catch (error: unknown) {
    batchError.value = error instanceof Error ? error.message : '下载失败'
  }
}

onUnmounted(() => {
  if (batchTimer) clearInterval(batchTimer)
})

// ================= 3D 可视化导航 =================
const goToVisualization = () => {
  if (!predictionResult.value) return
  router.push({
    path: '/visualization',
    query: {
      id: String(predictionResult.value.id),
      targetName: predictionResult.value.targetName,
      targetId: predictionResult.value.targetId,
    },
  })
}

// ================= 预测历史 =================
const historyList = ref<PredictResultResponse[]>([])
const historyLoading = ref(false)
const historyError = ref('')

const loadHistory = async () => {
  historyLoading.value = true
  historyError.value = ''
  try {
    historyList.value = await predictApi.getPredictHistory()
  } catch (error: unknown) {
    historyError.value = error instanceof Error ? error.message : '加载预测历史失败'
  } finally {
    historyLoading.value = false
  }
}

watch(mode, (val) => {
  if (val === 'history') {
    loadHistory()
  }
})

const getAlgoTypeText = (algoType?: string): string => {
  const texts: Record<string, string> = {
    dti: '药物-靶点 (DTI)',
    ppi: '蛋白-蛋白 (PPI)',
    ddi: '药物-药物 (DDI)'
  }
  const key = (algoType || '').toLowerCase()
  return texts[key] || algoType || '-'
}

const isDti = (algoType?: string): boolean => {
  return (algoType || '').toLowerCase() === 'dti'
}

const goHistoryDetail = (item: PredictResultResponse) => {
  router.push('/result/' + String(item.id ?? ''))
}

const goHistoryVisualization = (item: PredictResultResponse) => {
  router.push({
    path: '/visualization',
    query: {
      id: String(item.id ?? ''),
      targetName: item.targetName ?? '',
      targetId: item.targetId ?? '',
    },
  })
}
</script>

<style lang="scss" scoped>
/* ===================== 预测中心（深青品牌） ===================== */
.pc {
  display: flex;
  min-height: 100vh;
  background: $color-canvas;
  padding-top: $header-height;
}

// 宽度与居中由 styles/base.scss 的全局规则统一提供（1200px 窄幅）
.pc__main {
  flex: 1;
  padding: $spacing-lg $spacing-xl $spacing-2xl;
}

/* ---------- 页头 ---------- */
.pc__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: $spacing-lg;
  padding: $spacing-md 0 $spacing-xl;
}

.pc__header-main {
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

.pc__title {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  line-height: 1.3;
  color: $color-text;
}

.pc__subtitle {
  margin-top: 4px;
  font-size: $font-size-sm;
  color: $color-text-faint;
}

/* ---------- 模式切换 Tabs ---------- */
.pc__tabs {
  display: inline-flex;
  background: $color-surface-sunken;
  border-radius: $radius-control;
  padding: 4px;
  margin-bottom: $spacing-lg;
  gap: 4px;
}

.pc__tab {
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

/* ---------- 面板 / 卡片 ---------- */
.pc__panel {
  margin-bottom: $spacing-lg;
}

.pc__card {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-card;
  padding: $spacing-xl;
  box-shadow: $shadow-card;
}

.pc__card-head {
  margin-bottom: $spacing-lg;
}

.pc__card-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $color-text;
}

.pc__card-sub {
  margin-top: $spacing-xs;
  font-size: $font-size-sm;
  color: $color-text-faint;
}

/* ---------- 类型选择 ---------- */
.pc__types {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;
}

.pc__type {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  cursor: pointer;
  text-align: left;
  transition: border-color $transition-fast, background $transition-fast, box-shadow $transition-fast;
  color: $color-text-soft;

  &:hover {
    border-color: $app-accent;
  }

  &--active {
    border-color: $app-accent;
    background: $app-accent-softer;
    color: $app-brand;
    box-shadow: 0 0 0 1px $app-accent;

    .pc__type-name { color: $app-brand; }
    .pc__type-icon { color: $app-accent; }
  }
}

.pc__type-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: $radius-control;
  background: $color-surface-alt;
  color: $color-text-faint;

  svg { width: 22px; height: 22px; }
}

.pc__type-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.pc__type-name {
  font-weight: $font-weight-semibold;
  font-size: $font-size-base;
  color: $color-text;
}

.pc__type-desc {
  font-size: $font-size-xs;
  color: $color-text-faint;
}

.pc__type-check {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: $app-accent;
  color: #fff;
  opacity: 0;
  transform: scale(0.6);
  transition: opacity $transition-fast, transform $transition-fast;

  svg { width: 13px; height: 13px; }
}

.pc__type--active .pc__type-check {
  opacity: 1;
  transform: scale(1);
}

.pc__form-subtitle {
  margin: $spacing-lg 0 $spacing-md;
  font-size: $font-size-sm;
  color: $color-text-faint;
}

.pc__target-tip {
  margin: $spacing-md 0;
  padding: $spacing-sm $spacing-md;
  background: $app-accent-softer;
  border: 1px solid rgba(8, 145, 178, 0.22);
  border-radius: $radius-control;
  font-size: $font-size-sm;
  color: $app-accent-strong;
  b { font-weight: $font-weight-semibold; }
}

/* ---------- 输入区 ---------- */
.pc__form {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: $spacing-md;
  align-items: start;
}

.pc__input-block {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.pc__input-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pc__label {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text-soft;
}

.pc__clear {
  border: none;
  background: transparent;
  color: $color-text-faint;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: $border-radius-sm;
  display: inline-flex;
  align-items: center;

  svg { width: 12px; height: 12px; }

  &:hover { color: $error-color; background: rgba(239, 68, 68, 0.08); }
}

.pc__input {
  width: 100%;
  padding: $spacing-md;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  font-size: $font-size-base;
  color: $color-text;
  background: $color-surface;
  transition: $transition-fast;
  font-family: inherit;

  &::placeholder { color: $text-light; }

  &:focus {
    outline: none;
    border-color: $app-accent;
    box-shadow: 0 0 0 3px rgba(8, 145, 178, 0.12);
  }

  &--error {
    border-color: $error-color;
    &:focus { box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.12); }
  }
}

.pc__hint {
  min-height: 18px;
  font-size: $font-size-xs;
}

.pc__err { color: $error-color; }
.pc__ok { color: $app-success; }

.pc__vs {
  display: flex;
  align-items: center;
  align-self: center;
  margin-top: 26px;

  span {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: $color-surface-sunken;
    color: $color-text-faint;
    font-size: $font-size-sm;
    font-weight: $font-weight-bold;
  }
}

/* ---------- 高级选项 ---------- */
.pc__advanced {
  margin-top: $spacing-lg;
  border-top: 1px dashed $color-border;
  padding-top: $spacing-md;
}

.pc__advanced-toggle {
  border: none;
  background: transparent;
  color: $color-text-soft;
  font-size: $font-size-sm;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-xs 0;
  &:hover { color: $app-brand; }
}

.pc__chevron {
  display: inline-flex;
  transition: transform $transition-fast;

  svg { width: 14px; height: 14px; }

  &--open { transform: rotate(180deg); }
}

.pc__advanced-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-lg;
  margin-top: $spacing-md;
  padding: $spacing-md;
  background: $color-surface-sunken;
  border-radius: $radius-control;
}

.pc__advanced-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;

  &--row {
    flex-direction: row;
    align-items: center;
    gap: $spacing-sm;
    cursor: pointer;
  }
}

.pc__advanced-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pc__range {
  width: 100%;
  accent-color: $app-accent;
}

.pc__range-val {
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: $app-accent;
}

.pc__checkbox {
  display: inline-flex;

  input { display: none; }

  .pc__checkbox-box {
    width: 18px;
    height: 18px;
    border: 1px solid $color-border;
    border-radius: 4px;
    position: relative;
    transition: $transition-fast;
  }

  input:checked + .pc__checkbox-box {
    background: $app-accent;
    border-color: $app-accent;

    &::after {
      content: '';
      position: absolute;
      left: 5px;
      top: 2px;
      width: 5px;
      height: 9px;
      border: solid #fff;
      border-width: 0 2px 2px 0;
      transform: rotate(45deg);
    }
  }
}

.pc__desc {
  font-size: $font-size-xs;
  color: $color-text-faint;
  line-height: 1.5;
}

/* ---------- 操作按钮 ---------- */
.pc__actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-md;
  margin-top: $spacing-lg;
}

.pc__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-lg;
  border-radius: $radius-control;
  border: none;
  font-size: $font-size-base;
  cursor: pointer;
  transition: $transition-fast;

  &--primary {
    background: $app-accent;
    color: #fff;
    &:hover:not(:disabled) { background: $app-accent-strong; }
  }
  &--secondary {
    background: $color-surface-sunken;
    color: $color-text-soft;
    border: 1px solid $color-border;
    &:hover:not(:disabled) { background: $color-border-soft; color: $color-text; }
  }
  &--ghost {
    background: transparent;
    color: $app-accent;
    &:hover { background: $app-accent-softer; }
  }
  &--block { width: 100%; }

  &:disabled { opacity: 0.5; cursor: not-allowed; }
}

.pc__btn-icon {
  width: 16px;
  height: 16px;
}

.pc__spin {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: pc-spin 0.6s linear infinite;
}

@keyframes pc-spin {
  to { transform: rotate(360deg); }
}

.pc__error {
  margin-top: $spacing-md;
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-control;
  background: rgba(239, 68, 68, 0.08);
  color: $error-color;
  font-size: $font-size-sm;
  text-align: center;
}

/* ---------- 批量预测 ---------- */
.pc__batch-body {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.pc__select {
  padding: $spacing-sm $spacing-md;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  font-size: $font-size-base;
  background: $color-surface;
  color: $color-text;
  align-self: flex-start;
  min-width: 160px;
  &:focus { outline: none; border-color: $app-accent; }
}

.pc__dropzone {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  padding: $spacing-xl;
  border: 2px dashed $color-border;
  border-radius: $radius-control;
  background: $color-surface-alt;
  cursor: pointer;
  text-align: center;
  transition: border-color $transition-normal, background $transition-normal;

  &:hover { border-color: $app-accent; background: $app-accent-softer; }

  &--has {
    border-style: solid;
    border-color: $app-success;
    background: $app-success-soft;
  }
}

.pc__file {
  display: none;
}

.pc__dropzone-icon {
  width: 32px;
  height: 32px;
  color: $color-text-faint;

  &--ok { color: $app-success; }
}

.pc__dropzone-text {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $color-text;
}

.pc__dropzone-sub {
  font-size: $font-size-xs;
  color: $color-text-faint;
}

.pc__batch-status {
  margin-top: $spacing-lg;
  padding: $spacing-md;
  background: $color-surface-sunken;
  border-radius: $radius-control;
}

.pc__batch-status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: $font-size-sm;
  margin-bottom: $spacing-sm;
}

.pc__batch-id {
  color: $color-text-soft;
  font-family: monospace;
}

.pc__batch-pct {
  font-weight: $font-weight-bold;
  color: $color-text;
}

.pc__batch-state {
  padding: 2px 10px;
  border-radius: $radius-pill;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;

  &--pending { background: rgba(148, 163, 184, 0.15); color: $color-text-faint; }
  &--processing { background: rgba(8, 145, 178, 0.12); color: $app-accent-strong; }
  &--success { background: rgba(16, 185, 129, 0.12); color: $success-color; }
  &--fail { background: rgba(239, 68, 68, 0.12); color: $error-color; }
}

.pc__progress {
  width: 100%;
  height: 8px;
  background: $color-border;
  border-radius: $radius-pill;
  overflow: hidden;
  margin-bottom: $spacing-md;
}

.pc__progress-fill {
  height: 100%;
  background: linear-gradient(90deg, $app-accent, $app-brand);
  border-radius: $radius-pill;
  transition: width 0.3s ease;
}

/* ---------- 结果 ---------- */
.pc__result-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-lg;
}

.pc__result-time {
  display: block;
  margin-top: $spacing-xs;
  font-size: $font-size-xs;
  color: $color-text-faint;
}

.pc__badge {
  padding: 4px 12px;
  border-radius: $radius-pill;
  background: $app-accent-soft;
  color: $app-accent-strong;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
}

.pc__result-badges {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-xs;
  align-items: center;
}

.pc__badge--warn {
  background: rgba(239, 68, 68, 0.12);
  color: $error-color;
}

.pc__result-grid {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: $spacing-xl;
  padding: $spacing-lg;
  background: $color-surface-alt;
  border-radius: $radius-control;
}

.pc__score {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
}

.pc__score-ring {
  position: relative;
  width: 120px;
  height: 120px;
}

.pc__score-svg {
  width: 100%;
  height: 100%;
}

.pc__score-num {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $color-text;
}

.pc__score-label {
  font-size: $font-size-sm;
  color: $color-text-faint;
}

.pc__score-level {
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
}

.pc__details {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: $spacing-md;
}

.pc__detail {
  display: flex;
  align-items: baseline;
  gap: $spacing-md;

  &--accent .pc__detail-val { color: $app-accent; font-size: $font-size-xl; }
}

.pc__detail-label {
  width: 90px;
  flex-shrink: 0;
  font-size: $font-size-sm;
  color: $color-text-faint;
}

.pc__detail-val {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $color-text;

  small { font-size: $font-size-xs; font-weight: $font-weight-normal; color: $color-text-faint; }
}

.pc__section {
  margin-top: $spacing-lg;
}

.pc__section-title {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $color-text;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-sm;
  border-bottom: 1px solid $color-border-soft;
}

.pc__interactions {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
}

.pc__interaction {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
}

.pc__interaction-tag {
  padding: 2px 10px;
  border-radius: $radius-pill;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
}

.pc__interaction-meta {
  font-size: $font-size-sm;
  color: $color-text-soft;
}

.pc__dataset {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-md;
}

.pc__dataset-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  padding: $spacing-md;
  background: $color-surface-sunken;
  border-radius: $radius-control;

  span { font-size: $font-size-xs; color: $color-text-faint; }
  b { font-size: $font-size-sm; color: $color-text; }
}

.pc__result-actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-md;
  margin-top: $spacing-lg;
  padding-top: $spacing-lg;
  border-top: 1px solid $color-border-soft;
}

/* ---------- 预测历史 ---------- */
.pc__state {
  padding: $spacing-2xl;
  text-align: center;
  color: $color-text-faint;
  font-size: $font-size-base;

  &--error { color: $error-color; }
}

.pc__history-item {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  padding: $spacing-lg;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  margin-bottom: $spacing-md;

  &:last-child { margin-bottom: 0; }
}

.pc__history-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pc__history-type {
  padding: 3px 10px;
  border-radius: $radius-pill;
  background: $app-accent-soft;
  color: $app-accent-strong;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
}

.pc__history-time {
  font-size: $font-size-xs;
  color: $color-text-faint;
}

.pc__history-body {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.pc__history-field {
  display: flex;
  gap: $spacing-md;
  align-items: baseline;
}

.pc__history-label {
  flex-shrink: 0;
  width: 90px;
  font-size: $font-size-sm;
  color: $color-text-faint;
}

.pc__history-value {
  font-size: $font-size-sm;
  color: $color-text;

  &--smiles {
    font-family: monospace;
    word-break: break-all;
  }
}

.pc__history-confidence {
  margin-left: $spacing-sm;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
}

.pc__history-actions {
  display: flex;
  gap: $spacing-sm;
  padding-top: $spacing-md;
  border-top: 1px solid $color-border-soft;

  .pc__btn {
    padding: $spacing-xs $spacing-md;
    font-size: $font-size-sm;
  }
}

/* ---------- 响应式 ---------- */
@media (max-width: 900px) {
  .pc__types {
    grid-template-columns: 1fr;
  }

  .pc__form {
    grid-template-columns: 1fr;
  }

  .pc__vs {
    margin-top: 0;
    align-self: center;

    span { width: 32px; height: 32px; }
  }

  .pc__advanced-body {
    grid-template-columns: 1fr;
  }

  .pc__result-grid {
    grid-template-columns: 1fr;
  }

  .pc__dataset {
    grid-template-columns: 1fr;
  }
}
</style>
