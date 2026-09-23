/**
 * 设计工作台 · 接口门面
 * ============================================================================
 * 这是页面与数据之间的**唯一入口**：
 *   - 页面/组件/store 只依赖本文件导出的 `designApi`，不接触 mock 也不接触 HTTP 细节
 *   - mock 与真实实现共用同一份 `DesignApi` 签名，可整体替换
 *   - 后端就绪后只需把 `VITE_DESIGN_MOCK` 置为 `false`，页面代码零改动
 *
 * 端点路径与 `docs/modules/design/` 各模块文档保持一致。
 */
import { request } from '@/utils/request'
import { mockDesignApi } from '@/api/design.mock'
import type {
  CandidateQuery,
  DesignCandidate,
  DesignCommand,
  DesignTree,
  EvaluationCapabilities,
  MetricDefinition,
  MoleculeMetrics,
  Page,
  WorkspaceState
} from '@/types/design'

/**
 * 数据来源开关。
 * 默认使用 mock（后端 `/api/design/**` 尚未实现），只有显式设为 'false' 才走真实接口，
 * 这样生产构建也能正常演示；后端就绪后再关闭即可。
 */
const USE_MOCK = import.meta.env.VITE_DESIGN_MOCK !== 'false'

export interface GenerateCandidatesPayload {
  projectId: number
  roundId: number
  count: number
}

export interface GenerateCandidatesResult {
  runId: number
  producedCount: number
  duplicateCount: number
}

export interface SaveEditedMoleculePayload {
  parentMolId: number
  roundId: number
  smiles: string
}

/** 前端依赖的全部设计域接口；mock 与真实实现必须同签名 */
export interface DesignApi {
  /** 左栏一次拉全的项目树 */
  fetchTree(projectId: number): Promise<DesignTree>
  /** 候选列表（支持筛选/排序/分页） */
  fetchCandidates(query: CandidateQuery): Promise<Page<DesignCandidate>>
  /** 单个候选的全部指标与约束明细 */
  fetchMoleculeMetrics(moleculeId: number): Promise<MoleculeMetrics>
  /** 指标定义（驱动右栏与筛选面板） */
  fetchMetricDefinitions(): Promise<MetricDefinition[]>
  /** 工作台视图状态（恢复上次停在哪） */
  fetchWorkspace(): Promise<WorkspaceState>
  saveWorkspace(state: Partial<WorkspaceState>): Promise<void>
  /** 命令注册表（写操作的唯一执行入口） */
  fetchCommands(): Promise<DesignCommand[]>
  /** 当前可用的计算能力（用于降级可见） */
  fetchCapabilities(): Promise<EvaluationCapabilities>
  /** 生成候选（真实实现为异步任务，返回 runId） */
  generateCandidates(payload: GenerateCandidatesPayload): Promise<GenerateCandidatesResult>
  /** 保存编辑产物为新候选（保留父子谱系） */
  saveEditedMolecule(payload: SaveEditedMoleculePayload): Promise<DesignCandidate>
}

/** 真实后端实现 */
const realDesignApi: DesignApi = {
  fetchTree: (projectId) => request.get<DesignTree>(`/api/design/projects/${projectId}/tree`),

  fetchCandidates: (query) =>
    request.get<Page<DesignCandidate>>('/api/design/candidates/ranked', { params: query }),

  fetchMoleculeMetrics: (moleculeId) =>
    request.get<MoleculeMetrics>(`/api/design/molecules/${moleculeId}/metrics`),

  fetchMetricDefinitions: () => request.get<MetricDefinition[]>('/api/design/metrics/meta'),

  fetchWorkspace: () => request.get<WorkspaceState>('/api/design/workspace'),

  saveWorkspace: (state) => request.put<void>('/api/design/workspace', state),

  fetchCommands: () => request.get<DesignCommand[]>('/api/design/commands'),

  fetchCapabilities: () => request.get<EvaluationCapabilities>('/api/design/capabilities'),

  generateCandidates: (payload) =>
    request.post<GenerateCandidatesResult>('/api/design/generations', payload),

  saveEditedMolecule: (payload) =>
    request.post<DesignCandidate>(`/api/design/molecules/${payload.parentMolId}/edit`, payload)
}

export const designApi: DesignApi = USE_MOCK ? mockDesignApi : realDesignApi

export const isDesignMockEnabled = USE_MOCK
