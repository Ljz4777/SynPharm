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

export interface PredictResultResponse {
  id: string
  targetId: string
  targetName: string
  bindingAffinity: number
  confidenceScore: number
  confidenceLevel: string
  interactions: Array<{
    residue: string
    type: string
    distance: number
  }>
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

  getTaskDetail(taskNo: string): Promise<Task> {
    return request.get<Task>(`/api/tasks/${taskNo}`)
  }
}

export const resultApi = {
  getResultList(): Promise<PredictionResult[]> {
    return request.get<PredictionResult[]>('/api/results')
  },

  getResultDetail(resultNo: string): Promise<PredictionResult> {
    return request.get<PredictionResult>(`/api/results/${resultNo}`)
  }
}
