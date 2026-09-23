/**
 * 设计工作台 · 状态容器
 * ============================================================================
 * 只依赖 `designApi`，因此 mock 与真实后端对本 store 完全透明。
 * 数据分四类：项目树（左栏）、候选列表（中栏）、选中分子指标（右栏）、
 * 工作台视图状态（页签/轮次/选中，可持久化到服务端以便"换设备接着干"）。
 */
import { defineStore } from 'pinia'
import { designApi } from '@/api/design'
import type {
  CandidateQuery,
  DesignCandidate,
  DesignCommand,
  DesignRound,
  DesignTree,
  EvaluationCapabilities,
  MetricDefinition,
  MoleculeMetrics,
  WorkbenchTab
} from '@/types/design'

/** 默认项目（后端就绪后由项目列表页选择，此处先固定为 mock 项目） */
const DEFAULT_PROJECT_ID = 1001

export const useDesignStore = defineStore('design', {
  state: () => ({
    projectId: DEFAULT_PROJECT_ID,

    /** 左栏：项目树（靶点、口袋、约束、轮次、生成任务） */
    tree: null as DesignTree | null,

    /** 中栏：候选列表与查询条件 */
    candidates: [] as DesignCandidate[],
    candidateTotal: 0,
    query: {
      projectId: DEFAULT_PROJECT_ID,
      roundId: undefined as number | undefined,
      keyword: '',
      sortBy: 'compositeScore',
      sortOrder: 'desc' as CandidateQuery['sortOrder'],
      page: 1,
      pageSize: 20
    } as CandidateQuery,

    /** 右栏：当前选中分子与其指标 */
    selectedMolId: null as number | null,
    metrics: null as MoleculeMetrics | null,

    /** 元数据 */
    metricDefinitions: [] as MetricDefinition[],
    commands: [] as DesignCommand[],
    capabilities: null as EvaluationCapabilities | null,

    /** 工作台视图 */
    activeTab: 'canvas' as WorkbenchTab,

    loading: { tree: false, candidates: false, metrics: false, initiating: false },
    error: ''
  }),

  getters: {
    project: (state) => state.tree?.project ?? null,
    targets: (state) => state.tree?.targets ?? [],
    pockets: (state) => state.tree?.pockets ?? [],
    rounds: (state): DesignRound[] => state.tree?.rounds ?? [],
    constraintSet: (state) => state.tree?.constraintSet ?? null,
    constraintRules: (state) => state.tree?.constraintSet?.rules.filter((r) => r.enabled) ?? [],
    runs: (state) => state.tree?.runs ?? [],

    currentRound(state): DesignRound | null {
      const rounds = state.tree?.rounds ?? []
      if (!rounds.length) return null
      return rounds.find((r) => r.id === state.query.roundId) ?? rounds[rounds.length - 1] ?? null
    },

    /** 选中候选对象（画布与谱系展示用） */
    selectedCandidate(state): DesignCandidate | null {
      if (state.selectedMolId === null) return null
      return state.candidates.find((c) => c.id === state.selectedMolId) ?? null
    },

    /** 约束违规（右栏红/黄/绿） */
    violations: (state) => state.metrics?.violations ?? [],

    hardViolationCount(): number {
      return this.violations.filter((v) => !v.passed && v.severity === 'HARD').length
    },
    softViolationCount(): number {
      return this.violations.filter((v) => !v.passed && v.severity === 'SOFT').length
    },

    /** 指标按定义顺序排列，并带上定义信息，供右栏渲染 */
    metricRows(state): Array<{
      definition: MetricDefinition
      value?: number
      text?: string
      passed?: boolean
      grade?: string
    }> {
      const valueMap = new Map((state.metrics?.values ?? []).map((v) => [v.metricCode, v]))
      return state.metricDefinitions.map((definition) => {
        const value = valueMap.get(definition.metricCode)
        return {
          definition,
          value: value?.valueNum,
          text: value?.valueText ?? (value?.success === false ? '引擎未启用' : undefined),
          passed: value?.passed,
          grade: value?.grade
        }
      })
    },

    /** 只有可用的命令才可执行（未部署的引擎对应命令会被置灰） */
    availableCommands: (state) => state.commands.filter((c) => c.available),
    unavailableCommands: (state) => state.commands.filter((c) => !c.available),

    /** 是否有任何计算能力降级（用于状态栏提示） */
    degraded: (state) =>
      state.capabilities !== null && Object.values(state.capabilities).some((v) => !v),

    totalPages: (state) => Math.max(1, Math.ceil(state.candidateTotal / state.query.pageSize)),
    /** 当前轮次候选总数（来自轮次统计，与分页无关） */
    currentRoundCandidateCount(): number {
      return this.currentRound?.candidateCount ?? 0
    }
  },

  actions: {
    async init(): Promise<void> {
      this.loading.initiating = true
      this.error = ''
      try {
        const workspace = await designApi.fetchWorkspace()
        this.activeTab = workspace.activeTab ?? 'canvas'
        this.selectedMolId = workspace.lastMolId ?? null

        const [tree, definitions, commands, capabilities] = await Promise.all([
          designApi.fetchTree(this.projectId),
          designApi.fetchMetricDefinitions(),
          designApi.fetchCommands(),
          designApi.fetchCapabilities()
        ])

        this.tree = tree
        this.metricDefinitions = definitions
        this.commands = commands
        this.capabilities = capabilities

        const targetRound =
          workspace.lastRoundId ?? tree.project.currentRoundId ?? tree.rounds[tree.rounds.length - 1]?.id
        this.query = { ...this.query, roundId: targetRound, page: 1 }

        await this.loadCandidates()
      } catch (err) {
        this.error = err instanceof Error ? err.message : '工作台初始化失败'
      } finally {
        this.loading.initiating = false
      }
    },

    async loadCandidates(): Promise<void> {
      this.loading.candidates = true
      this.error = ''
      try {
        const page = await designApi.fetchCandidates({ ...this.query, projectId: this.projectId })
        this.candidates = page.list
        this.candidateTotal = page.total

        // 选中项不在当前页时保持选中不变（例如翻页后仍展示原分子）
        const keepSelected = this.selectedMolId
        if (keepSelected !== null) {
          await this.loadMetrics(keepSelected)
        }
      } catch (err) {
        this.error = err instanceof Error ? err.message : '候选列表加载失败'
      } finally {
        this.loading.candidates = false
      }
    },

    async loadMetrics(moleculeId: number): Promise<void> {
      this.loading.metrics = true
      try {
        this.metrics = await designApi.fetchMoleculeMetrics(moleculeId)
      } catch (err) {
        this.metrics = null
        this.error = err instanceof Error ? err.message : '指标加载失败'
      } finally {
        this.loading.metrics = false
      }
    },

    /** 选中候选：同步加载指标并记录到工作台视图状态 */
    async selectCandidate(moleculeId: number): Promise<void> {
      this.selectedMolId = moleculeId
      await this.loadMetrics(moleculeId)
      void this.persistWorkspace()
    },

    async setRound(roundId: number): Promise<void> {
      this.query = { ...this.query, roundId, page: 1 }
      await this.loadCandidates()
      void this.persistWorkspace()
    },

    async setTab(tab: WorkbenchTab): Promise<void> {
      this.activeTab = tab
      void this.persistWorkspace()
    },

    async setQuery(patch: Partial<CandidateQuery>): Promise<void> {
      // 改变筛选/排序时回到第一页，避免落在越界页
      const resetPage = patch.page === undefined
      this.query = { ...this.query, ...patch, page: resetPage ? 1 : (patch.page as number) }
      await this.loadCandidates()
    },

    async generate(count: number): Promise<{ producedCount: number; duplicateCount: number }> {
      const roundId = this.query.roundId
      if (!roundId) return { producedCount: 0, duplicateCount: 0 }

      const result = await designApi.generateCandidates({ projectId: this.projectId, roundId, count })
      // 生成后刷新树（轮次计数）与列表
      this.tree = await designApi.fetchTree(this.projectId)
      await this.loadCandidates()
      return { producedCount: result.producedCount, duplicateCount: result.duplicateCount }
    },

    /** 保存编辑器产物：以选中候选为父，产生子候选（谱系 +1 层） */
    async saveEdited(smiles: string): Promise<DesignCandidate | null> {
      const parent = this.selectedCandidate
      const roundId = this.query.roundId
      if (!roundId) return null

      const created = await designApi.saveEditedMolecule({
        parentMolId: parent?.id ?? 0,
        roundId,
        smiles
      })
      this.tree = await designApi.fetchTree(this.projectId)
      await this.loadCandidates()
      await this.selectCandidate(created.id)
      return created
    },

    async persistWorkspace(): Promise<void> {
      try {
        await designApi.saveWorkspace({
          projectId: this.projectId,
          lastRoundId: this.query.roundId,
          lastMolId: this.selectedMolId ?? undefined,
          activeTab: this.activeTab
        })
      } catch {
        // 视图状态持久化失败不影响主流程
      }
    },

    reset(): void {
      this.$reset()
    }
  }
})
