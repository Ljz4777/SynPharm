/**
 * 干实验药物设计平台 · 前端类型契约
 * ============================================================================
 * 本文件是「前端 ↔ 后端」的唯一契约来源，字段命名与 docs/modules/design/ 中的
 * DDL 及接口设计保持一致（后端表为 snake_case，此处按前端惯例转 camelCase）。
 *
 * 使用方：src/api/design.ts（实现选择器）、src/stores/design.ts、src/views/Design.vue
 * 约定：页面与组件只依赖这些类型，不直接接触 mock 或 HTTP 细节。
 */

/* ------------------------------------------------------------------ */
/* 通用                                                                */
/* ------------------------------------------------------------------ */

export interface Page<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export type SortOrder = 'asc' | 'desc'

/* ------------------------------------------------------------------ */
/* 01 项目管理                                                          */
/* ------------------------------------------------------------------ */

export type DesignProjectStatus =
  | 'DRAFT'
  | 'READY'
  | 'ACTIVE'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'ARCHIVED'

export interface DesignProject {
  id: number
  projectNo: string
  name: string
  /** 设计目标（自然语言） */
  goalText?: string
  status: DesignProjectStatus
  /** 当前轮次（工作台恢复用） */
  currentRoundId?: number
  activeConstraintSetId?: number
  createdAt: string
  updatedAt: string
}

export type RoundStatus =
  | 'PLANNED'
  | 'GENERATING'
  | 'EDITING'
  | 'EVALUATING'
  | 'REVIEWING'
  | 'CLOSED'

export interface DesignRound {
  id: number
  roundNo: number
  name: string
  status: RoundStatus
  candidateCount: number
  qualifiedCount: number
  /** 本轮指标快照（design_round.metrics_json） */
  metricSnapshot?: Record<string, number>
  startedAt?: string
  closedAt?: string
}

/* ------------------------------------------------------------------ */
/* 02 靶点与结合口袋                                                    */
/* ------------------------------------------------------------------ */

/** WILD_TYPE / MUTANT 同时绑定才能算选择性指数（见设计文档 06 §7） */
export type ProjectTargetRole = 'WILD_TYPE' | 'MUTANT' | 'ANTI_TARGET' | 'OFF_TARGET'

export interface ProjectTarget {
  /** project_target.id */
  id: number
  /** target.id */
  targetId: number
  name: string
  chineseName?: string
  uniprotId: string
  geneName?: string
  role: ProjectTargetRole
  isPrimary: boolean
  sequenceLength: number
  pdbIds: string[]
}

export interface BindingPocket {
  id: number
  targetId: number
  structureRef: string
  pocketNo: number
  /** 口袋中心坐标 */
  center: { x: number; y: number; z: number }
  radius: number
  /** 成药性打分 0~1 */
  druggability: number
  residues: string[]
}

/* ------------------------------------------------------------------ */
/* 03 设计约束                                                          */
/* ------------------------------------------------------------------ */

export type ConstraintSeverity = 'HARD' | 'SOFT'

export type ConstraintRuleType =
  | 'MOLECULAR_WEIGHT'
  | 'LOGP'
  | 'TPSA'
  | 'H_BOND_DONOR'
  | 'H_BOND_ACCEPTOR'
  | 'ROTATABLE_BOND'
  | 'SA_SCORE'
  | 'QED'
  | 'SUBSTRUCTURE_EXCLUDE'
  | 'SUBSTRUCTURE_REQUIRE'
  | 'SIMILARITY_TO_REFERENCE'

export interface ConstraintRule {
  id: number
  ruleCode: string
  ruleName: string
  ruleType: ConstraintRuleType
  severity: ConstraintSeverity
  /** 规则参数，如 { min: 200, max: 500 } 或 { smarts: '...' } */
  params: Record<string, unknown>
  /** SOFT 规则在综合得分中的扣分权重 */
  weight?: number
  enabled: boolean
}

export type ConstraintSetStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED'

export interface ConstraintSet {
  id: number
  name: string
  version: number
  status: ConstraintSetStatus
  rules: ConstraintRule[]
}

/** 单条规则的校验结果（用于右栏红/黄/绿） */
export interface ConstraintViolation {
  ruleCode: string
  ruleName: string
  severity: ConstraintSeverity
  passed: boolean
  /** 实际值（展示用） */
  actual: string
  /** 约束期望（展示用） */
  expected: string
}

/* ------------------------------------------------------------------ */
/* 04 分子生成器                                                        */
/* ------------------------------------------------------------------ */

export type GenerationRunStatus =
  | 'PENDING'
  | 'RUNNING'
  | 'SUCCESS'
  | 'PARTIAL'
  | 'FAILED'
  | 'CANCELLED'

export interface GenerationRun {
  id: number
  runNo: string
  projectId: number
  roundId: number
  /** 策略编号 S1~S7（S1~S6 为确定性枚举，无需新模型） */
  strategy: string
  strategyLabel: string
  params: Record<string, unknown>
  status: GenerationRunStatus
  progress: number
  producedCount: number
  duplicateCount: number
  errorMsg?: string
  createdAt: string
}

/* ------------------------------------------------------------------ */
/* 05 分子编辑器 / 候选                                                 */
/* ------------------------------------------------------------------ */

export type MoleculeOriginType = 'GENERATED' | 'EDITED' | 'IMPORTED'

export type ReviewStatus = 'NEW' | 'QUALIFIED' | 'TOP' | 'REJECTED' | 'SELECTED'

export interface DesignCandidate {
  id: number
  projectId: number
  roundId: number
  /** 全平台统一去重键 */
  inchikey: string
  smiles: string
  displayName?: string
  originType: MoleculeOriginType
  /** 谱系：父候选（撤销/重做即在此链上游标移动） */
  parentMolId?: number
  generationRunId?: number
  /** 以下为冗余列，用于列表排序与筛选，避免每次关联查询 */
  mw: number
  logp: number
  tpsa: number
  saScore: number
  qed: number
  compositeScore: number
  reviewStatus: ReviewStatus
  createdAt: string

  /**
   * 以下三项为「引擎校验」增强字段（均为可选，缺失时界面按估算值渲染）。
   *
   * 演示数据用轻量估算给出 InChIKey/分子量（列表可秒开），编辑器子应用就绪后
   * 再由本地化学引擎（Indigo）回填真值。后端就绪后这三项由后端直接给出，
   * 前端渲染逻辑无需改动。
   */
  /** 引擎实算分子量（Indigo）。回填后与 mw 同步 */
  mwEngine?: number
  /** 引擎实算分子式 */
  formula?: string
  /** InChIKey 来源：ENGINE=化学引擎实算；ESTIMATE=演示估算（非去重依据） */
  inchikeySource?: 'ENGINE' | 'ESTIMATE'
}

/** 列表查询条件（对齐设计文档 06 的 /api/design/candidates/ranked） */
export interface CandidateQuery {
  projectId: number
  roundId?: number
  keyword?: string
  originType?: MoleculeOriginType
  reviewStatus?: ReviewStatus
  /** 排序字段：compositeScore / mw / logp / qed / saScore / createdAt */
  sortBy?: string
  sortOrder?: SortOrder
  page: number
  pageSize: number
}

/* ------------------------------------------------------------------ */
/* 06 评估器                                                            */
/* ------------------------------------------------------------------ */

export type MetricCategory =
  | 'CONSTRAINT'
  | 'PROPERTY'
  | 'DRUGLIKENESS'
  | 'SAFETY'
  | 'SYNTHESIS'
  | 'DIVERSITY'
  | 'ACTIVITY'
  | 'SELECTIVITY'
  | 'DOCKING'
  | 'ADMET'

export interface MetricDefinition {
  metricCode: string
  metricName: string
  category: MetricCategory
  valueType: 'NUMERIC' | 'BOOLEAN' | 'GRADE' | 'JSON'
  unit?: string
  better: 'HIGHER' | 'LOWER' | 'NONE'
  minValue?: number
  maxValue?: number
  weight: number
  enabled: boolean
}

export interface MetricValue {
  metricCode: string
  valueNum?: number
  valueText?: string
  passed?: boolean
  normalized?: number
  grade?: string
  /** 该指标本次是否算成功（引擎不可用时为 false，权重会被重分配） */
  success: boolean
}

export interface MoleculeMetrics {
  moleculeId: number
  inchikey: string
  compositeScore: number
  values: MetricValue[]
  violations: ConstraintViolation[]
}

/** 评估能力（对应 FastAPI /v1/evaluate/capabilities，用于降级可见） */
export interface EvaluationCapabilities {
  docking: boolean
  admet: boolean
  dti: boolean
  ddi: boolean
  ppi: boolean
}

/**
 * 由编辑器子应用（Indigo）算出的分子事实。
 *
 * 注意：这是**前端编辑器自带的能力**，并非最终架构 —— 按设计文档，这类计算属于 FastAPI。
 * 当前用途是让演示数据由化学引擎真算得出（真实 InChIKey 也是全平台去重键），
 * 后端就绪后这些值应改由后端提供，本类型可原样复用。
 */
export interface EngineAnalysis {
  smiles: string
  inchikey: string
  molecularWeight: number
  formula: string
}

/* ------------------------------------------------------------------ */
/* 07 工作台                                                            */
/* ------------------------------------------------------------------ */

export type WorkbenchTab = 'canvas' | 'candidates' | 'diff' | 'pocket'

export interface WorkspaceState {
  projectId?: number
  lastRoundId?: number
  lastMolId?: number
  activeTab?: WorkbenchTab
  openMolIds?: number[]
  panelLayout?: Record<string, unknown>
}

/** 命令注册表条目（07 §8；二阶段的 AgentTool 直接复用同一注册表） */
export interface DesignCommand {
  id: string
  label: string
  group: string
  /** 是否为写操作（写操作必须经 CommandRegistry 执行） */
  writes: boolean
  /** 当前是否可用（未安装的引擎对应命令不可用） */
  available: boolean
  /** 不可用原因 */
  reason?: string
}

/* ------------------------------------------------------------------ */
/* 工作台左栏：一次拉全的树                                             */
/* ------------------------------------------------------------------ */

export interface DesignTree {
  project: DesignProject
  targets: ProjectTarget[]
  pockets: BindingPocket[]
  constraintSet: ConstraintSet
  rounds: DesignRound[]
  /** 当前轮次已完成的生成任务 */
  runs: GenerationRun[]
}
