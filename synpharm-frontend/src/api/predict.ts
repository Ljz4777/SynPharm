import { request } from '@/utils/request'
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

export const predictApi = {
  predictDTI(data: DTIPredictRequest): Promise<PredictResultResponse> {
    return request.post<PredictResultResponse>('/api/predict/dti', data)
  },

  predictPPI(data: PPIPredictRequest): Promise<PredictResultResponse> {
    return request.post<PredictResultResponse>('/api/predict/ppi', data)
  },

  predictDDI(data: DDIPredictRequest): Promise<PredictResultResponse> {
    return request.post<PredictResultResponse>('/api/predict/ddi', data)
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
