import { request, baseURL } from '@/utils/request'
import type { PredictionResult, Task } from '@/types'

export interface DTIPredictRequest {
  smiles: string
  targetId: string
}

export interface PPIPredictRequest {
  proteinA: string
  proteinB: string
}

export interface DDIPredictRequest {
  drugASmiles: string
  drugBSmiles: string
}

/** 预测结果响应（与后端 PredictResultResponse 对齐） */
export interface PredictResultResponse {
  id?: string | number
  algoType?: string
  targetId?: string
  targetName?: string
  ligandSmiles?: string
  bindingAffinity?: number
  confidenceScore?: number
  confidenceLevel?: string
  interactions?: Array<{
    type?: string
    residueName?: string
    residueNumber?: string
    distance?: number
  }>
  createdAt?: string
  datasetInfo?: {
    name?: string
    size?: number
    description?: string
    source?: string
  }
}

/** 分页结果结构（后端 /api/results 返回 { total, list }） */
export interface PagedResult<T> {
  total: number
  list: T[]
}

/** 统一预测请求（对应后端 GeneralPredictRequest） */
export interface GeneralPredictRequest {
  /** 输入类型；后端会按 algoType 决定怎么解析 */
  inputType: 'smiles' | 'uniprot' | 'pdb' | 'csv'
  algoType: 'DTI' | 'PPI' | 'DDI'
  /** 后端支持 json / csv，实时预测固定用 json */
  outputType?: 'json' | 'csv'
  /** 逗号分隔的输入值，语义随 algoType 变化 */
  inputValue: string
  fileUrl?: string
}

/** DDI 可预测药物（白名单条目） */
export interface DdiDrug {
  drugId: string
  drugName?: string | null
}

export interface DdiDrugListResponse {
  total?: number
  drugs?: DdiDrug[]
}

/**
 * 统一预测入口。
 *
 * ⚠️ 后端已把 `/dti` `/ppi` `/ddi` 三个接口标注为 `@Deprecated`
 * （`PredictController`），前端此前仍在调用它们（问题总账 A-04）。
 * 这里统一改为 `/api/predict/general`，`inputValue` 均为逗号分隔：
 *
 * | 算法 | inputValue | 说明 |
 * |---|---|---|
 * | DTI | `SMILES,靶点` | 靶点可写 UniProt ID 或 PDB 引用，后端会自动嗅探并取序列 |
 * | PPI | `蛋白序列A,蛋白序列B` | 必须是序列本身，不是蛋白名 |
 * | DDI | `药物A,药物B` | 可传 SMILES（后端会转成 DrugBank ID）或直接传 DrugBank ID |
 */
function predictGeneral(data: GeneralPredictRequest): Promise<PredictResultResponse> {
  return request.post<PredictResultResponse>('/api/predict/general', {
    outputType: 'json',
    ...data
  })
}

export const predictApi = {
  predictGeneral,

  predictDTI(data: DTIPredictRequest): Promise<PredictResultResponse> {
    return predictGeneral({
      inputType: 'smiles',
      algoType: 'DTI',
      inputValue: `${data.smiles},${data.targetId}`
    })
  },

  predictPPI(data: PPIPredictRequest): Promise<PredictResultResponse> {
    return predictGeneral({
      inputType: 'smiles',
      algoType: 'PPI',
      inputValue: `${data.proteinA},${data.proteinB}`
    })
  },

  predictDDI(data: DDIPredictRequest): Promise<PredictResultResponse> {
    return predictGeneral({
      inputType: 'smiles',
      algoType: 'DDI',
      inputValue: `${data.drugASmiles},${data.drugBSmiles}`
    })
  },

  /**
   * DDI 可预测药物白名单（能力边界）。
   *
   * DDI-LLM 是转导式模型，只能预测训练图内的药物；据此可以渲染可选药物下拉，
   * 或对用户输入做前置校验，把"模型不支持"从事后报错变成事前可见。
   */
  getDdiDrugs(): Promise<DdiDrugListResponse> {
    return request.get<DdiDrugListResponse>('/api/predict/ddi/drugs')
  },

  /** 获取当前用户的预测历史列表（按创建时间倒序） */
  getPredictHistory(): Promise<PredictResultResponse[]> {
    return request.get<PredictResultResponse[]>('/api/predict/history')
  }
}

export const taskApi = {
  getTaskList(): Promise<Task[]> {
    return request.get<Task[]>('/api/tasks')
  },

  getTaskDetail(id: string | number): Promise<Task> {
    return request.get<Task>(`/api/tasks/${id}`)
  },

  cancelTask(id: string | number): Promise<void> {
    return request.delete<void>(`/api/tasks/${id}`)
  }
}

export const resultApi = {
  getResultList(page = 1, pageSize = 10): Promise<PagedResult<PredictionResult>> {
    return request.get<PagedResult<PredictionResult>>('/api/results', { params: { page, pageSize } })
  },

  getResultDetail(id: string | number): Promise<PredictionResult> {
    return request.get<PredictionResult>(`/api/results/${id}`)
  },

  deleteResult(id: string | number): Promise<void> {
    return request.delete<void>(`/api/results/${id}`)
  }
}

export interface BatchUploadResult {
  batchId: string
  totalCount: number
  status: string
}

export interface BatchStatus {
  batchId: string
  algoType?: string
  totalCount: number
  successCount: number
  failCount: number
  progress: number
  status: string
  resultUrl?: string
  createTime?: string
  updateTime?: string
}

export const batchApi = {
  /** 上传 CSV 批量预测 */
  upload(file: File, algoType: string): Promise<BatchUploadResult> {
    const form = new FormData()
    form.append('file', file)
    form.append('algoType', algoType)
    return request.post<BatchUploadResult>('/api/batch/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  /** 查询批量任务进度 */
  getStatus(batchId: string): Promise<BatchStatus> {
    return request.get<BatchStatus>(`/api/batch/status/${batchId}`)
  },

  /** 下载批量结果 CSV（带 token，绕过拦截器） */
  async download(batchId: string): Promise<void> {
    const token = localStorage.getItem('auth_token')
    const url = `${baseURL}/api/batch/download/${batchId}`
    const resp = await fetch(url, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    if (!resp.ok) {
      throw new Error('下载失败')
    }
    const blob = await resp.blob()
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `${batchId}_result.csv`
    link.click()
    URL.revokeObjectURL(link.href)
  }
}
