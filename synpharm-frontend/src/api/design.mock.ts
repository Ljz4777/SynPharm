/**
 * 设计工作台 · Mock 数据层
 * ============================================================================
 * 目标：在后端接口就绪前，让工作台以「真实形态」跑起来 ——
 *   1) 数据由「脚手架 × 取代基」组合生成，而不是把字面量堆在页面里
 *      （这正对应设计文档 04 中生成器 S1「R 基枚举」策略；
 *        真实后端就绪后，这里只是被替换掉，界面与契约都不动）
 *   2) 指标值由分子式确定性派生 —— 同一分子每次得到同一组数值（可复现），
 *      但数值本身是算出来的，不是写死的
 *   3) 约束校验与综合得分使用设计文档 06 中的**真实公式**，不是假数据
 *
 * ⚠️ 明确边界：理化性质为**估算值**，仅用于驱动界面；真实值由 FastAPI/RDKit 提供。
 *    这部分是本 mock 唯一"假"的地方，其余（约束判定、得分、排序、分页、
 *    去重、谱系）都是真实逻辑。
 */
import type {
  BindingPocket,
  CandidateQuery,
  ConstraintRule,
  ConstraintSet,
  ConstraintViolation,
  DesignCandidate,
  DesignCommand,
  DesignProject,
  DesignRound,
  DesignTree,
  EngineAnalysis,
  EvaluationCapabilities,
  GenerationRun,
  MetricDefinition,
  MetricValue,
  MoleculeMetrics,
  Page,
  ProjectTarget,
  WorkspaceState
} from '@/types/design'

/* ================================================================== */
/* 0. 确定性随机：同一输入永远得到同一输出                              */
/* ================================================================== */

/** FNV-1a：把字符串折成 32 位种子 */
function hashSeed(text: string): number {
  let h = 0x811c9dc5
  for (let i = 0; i < text.length; i++) {
    h ^= text.charCodeAt(i)
    h = Math.imul(h, 0x01000193)
  }
  return h >>> 0
}

/** mulberry32：小而快的确定性伪随机数发生器 */
function createRng(seed: number): () => number {
  let a = seed >>> 0
  return () => {
    a = (a + 0x6d2b79f5) >>> 0
    let t = Math.imul(a ^ (a >>> 15), 1 | a)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

const round = (value: number, digits = 2): number => {
  const factor = 10 ** digits
  return Math.round(value * factor) / factor
}

/** 模拟网络延迟；用确定性抖动，避免每次刷新手感不一致 */
const latency = (seed: number): Promise<void> =>
  new Promise((resolve) => {
    const rng = createRng(seed)
    window.setTimeout(resolve, 90 + Math.floor(rng() * 140))
  })

/* ================================================================== */
/* 1. 候选分子来源：脚手架 × 取代基（对应生成策略 S1）                  */
/* ================================================================== */

/**
 * 脚手架：只写「母核」本身，取代基在**尾部**拼接（SMILES 中 `A B` 表示 A–B 键，
 * 所以写在后面的片段才是连接点那一端）。
 *
 * 注意：早期版本把 `[*]` 放在开头再 replace，会让每个片段被反向接入
 * （`OC` 会拼成 `OCc1...`＝苄醇而不是甲氧基），故改为尾部拼接。
 */
const SCAFFOLDS = [
  'c1ccccc1', // 苯基（取代位与母核任一位等价）
  'c1ccc(F)cc1', // 3-氟苯基（F 与取代基呈间位）
  'c1ccncc1', // 3-吡啶基
  'c1ccc2ccccc2c1', // 1-萘基
  'C1CCCCC1', // 环己基
  'c1ccsc1', // 2-噻吩基
  'c1ccc(-c2ccccc2)cc1', // 联苯（分子量 ~154）
  'c1ccc(cc1)C(=O)c1ccccc1', // 二苯甲酮（~182）
  'c1ccc(-c2ccc(-c3ccccc3)cc2)cc1' // 三联苯（~230）
] as const

/** 取代基：均为「可尾部拼接」的单开价片段；H 为空串，等价于母核本身 */
const SUBSTITUENTS = [
  { code: 'H', label: '氢', smiles: '' },
  { code: 'Me', label: '甲基', smiles: 'C' },
  { code: 'Et', label: '乙基', smiles: 'CC' },
  { code: 'nPr', label: '正丙基', smiles: 'CCC' },
  { code: 'nBu', label: '正丁基', smiles: 'CCCC' },
  { code: 'F', label: '氟', smiles: 'F' },
  { code: 'Cl', label: '氯', smiles: 'Cl' },
  { code: 'Br', label: '溴', smiles: 'Br' },
  { code: 'I', label: '碘', smiles: 'I' },
  { code: 'OH', label: '羟基', smiles: 'O' },
  { code: 'OMe', label: '甲氧基', smiles: 'OC' },
  { code: 'OEt', label: '乙氧基', smiles: 'OCC' },
  { code: 'OCH2CH2OH', label: '羟乙氧基', smiles: 'OCCO' },
  { code: 'NH2', label: '氨基', smiles: 'N' },
  { code: 'NHMe', label: '甲氨基', smiles: 'NC' },
  { code: 'NMe2', label: '二甲氨基', smiles: 'N(C)C' },
  { code: 'SH', label: '巯基', smiles: 'S' },
  { code: 'CN', label: '氰基', smiles: 'C#N' },
  { code: 'CF3', label: '三氟甲基', smiles: 'C(F)(F)F' },
  { code: 'CH2OH', label: '羟甲基', smiles: 'CO' },
  { code: 'COOH', label: '羧基', smiles: 'C(=O)O' },
  { code: 'CONH2', label: '酰胺基', smiles: 'C(=O)N' },
  { code: 'SO2NH2', label: '磺酰胺基', smiles: 'S(=O)(=O)N' }
] as const

interface SeedMolecule {
  smiles: string
  scaffolIndex: number
  substituent: (typeof SUBSTITUENTS)[number]
}

/** 组合出全部候选结构（取代基优先，保证各脚手架在池中均匀分布） */
function buildMoleculePool(): SeedMolecule[] {
  const pool: SeedMolecule[] = []
  SUBSTITUENTS.forEach((substituent) => {
    SCAFFOLDS.forEach((scaffold, scaffolIndex) => {
      pool.push({
        // 尾部拼接：H 为空串，得到母核本身；其余片段末端原子即为连接原子
        smiles: `${scaffold}${substituent.smiles}`,
        scaffolIndex,
        substituent
      })
    })
  })
  return pool
}

/** 全平台统一去重键。真实系统用 RDKit 生成 InChIKey，此处用结构串的稳定散列代替 */
function deriveInchikey(smiles: string): string {
  const h1 = hashSeed(smiles).toString(36).toUpperCase()
  const h2 = hashSeed(`salt::${smiles}`).toString(36).toUpperCase()
  return `${h1.slice(0, 14)}-${h2.slice(0, 10)}-N`
}

/* ================================================================== */
/* 2. 理化性质估算（本 mock 唯一近似部分）                              */
/* ================================================================== */

const ATOM_MASS: Record<string, number> = {
  C: 12.011,
  N: 14.007,
  O: 15.999,
  S: 32.06,
  F: 18.998,
  Cl: 35.45,
  Br: 79.904,
  I: 126.904,
  P: 30.974
}

interface Descriptors {
  heavyAtoms: number
  mw: number
  logp: number
  tpsa: number
  hDonors: number
  hAcceptors: number
  rotatableBonds: number
  fractionCsp3: number
  ringCount: number
  halogenCount: number
  qed: number
  saScore: number
  painsHits: number
  brenkHits: number
}

/** 粗解析 SMILES 的元素组成（够用即可；真实实现为 RDKit） */
function countElements(smiles: string): Record<string, number> {
  const counts: Record<string, number> = {}
  const bump = (el: string) => {
    counts[el] = (counts[el] ?? 0) + 1
  }

  for (let i = 0; i < smiles.length; i++) {
    const ch = smiles[i] as string
    if (ch === '[') {
      // [nH] / [N+] 等：取括号内的元素符号
      const close = smiles.indexOf(']', i)
      const inner = smiles.slice(i + 1, close === -1 ? undefined : close)
      const el = inner.replace(/[^A-Za-z]/g, '')
      if (el) {
        const normalized = el[0]?.toUpperCase() + (el[1]?.toLowerCase() ?? '')
        bump(ATOM_MASS[normalized] ? normalized : (el[0]?.toUpperCase() ?? 'C'))
      }
      i = close === -1 ? smiles.length : close
      continue
    }

    if (ch >= 'A' && ch <= 'Z') {
      const two = smiles.slice(i, i + 2)
      if (ATOM_MASS[two]) {
        bump(two)
        i++
      } else if (ATOM_MASS[ch]) {
        bump(ch)
      }
      continue
    }

    // 芳香小写原子
    if (ch === 'c') bump('C')
    else if (ch === 'n') bump('N')
    else if (ch === 'o') bump('O')
    else if (ch === 's') bump('S')
  }

  return counts
}

/**
 * 由结构估算理化性质。
 * 估算规则刻意保持简单且单调（原子越多分子越大、杂原子越多越亲水），
 * 目的是让列表排序与约束判定看起来合理，不追求与 RDKit 数值接近。
 */
function estimateDescriptors(smiles: string): Descriptors {
  const counts = countElements(smiles)
  const rng = createRng(hashSeed(`desc::${smiles}`))

  const carbon = counts.C ?? 0
  const nitrogen = counts.N ?? 0
  const oxygen = counts.O ?? 0
  const sulfur = counts.S ?? 0
  const halogen = (counts.F ?? 0) + (counts.Cl ?? 0) + (counts.Br ?? 0) + (counts.I ?? 0)
  const hetero = nitrogen + oxygen + sulfur + halogen
  const heavyAtoms = carbon + hetero

  // 重原子质量 + 氢估算（每个重原子约 1 个氢，缺电子杂原子适当减少）
  const heavyMass = Object.entries(counts).reduce(
    (sum, [el, n]) => sum + (ATOM_MASS[el] ?? 12) * n,
    0
  )
  const hydrogenCount = Math.max(0, Math.round(heavyAtoms * 1.1 - nitrogen * 0.5))
  const mw = heavyMass + hydrogenCount * 1.008

  const ringCount = Math.max(1, Math.round((smiles.match(/[1-9]/g) ?? []).length / 2))

  const rotatableBonds = Math.max(0, Math.round(heavyAtoms / 4 - ringCount + rng() * 2))
  const hDonors = Math.max(0, Math.round(oxygen * 0.6 + nitrogen * 0.4 - halogen * 0.3))
  const hAcceptors = nitrogen + Math.round(oxygen * 0.9) + (sulfur > 0 ? 1 : 0)

  // 亲脂性：碳多则升，杂原子多则降
  const logp = round(
    -0.6 + carbon * 0.42 - oxygen * 0.85 - nitrogen * 0.9 - halogen * 0.2 + (rng() - 0.5) * 0.8,
    2
  )

  // 极性表面积：按杂原子贡献粗估
  const tpsa = round(
    oxygen * 16 + nitrogen * 14 + sulfur * 10 + (rng() - 0.5) * 6,
    1
  )

  const saturatedCarbons = (smiles.match(/[A-Z]?(?=[0-9(]|$)/g) ?? []).length
  const fractionCsp3 = round(
    Math.min(0.95, Math.max(0.05, saturatedCarbons / Math.max(1, carbon) * 0.75 + rng() * 0.25)),
    3
  )

  // 合成可及性：环与手性中心越多越难合成
  const saScore = round(
    Math.min(9.5, Math.max(1.2, 1.4 + ringCount * 0.75 + hetero * 0.22 + (rng() - 0.5) * 1.2)),
    2
  )

  // 类药性：向 Lipinski/Veber 舒适区聚拢则升高
  const lipinskiPenalty =
    Math.max(0, mw - 500) / 300 +
    Math.max(0, logp - 5) / 3 +
    Math.max(0, hDonors - 5) / 5 +
    Math.max(0, hAcceptors - 10) / 10
  const qed = round(Math.min(0.97, Math.max(0.12, 0.82 - lipinskiPenalty * 0.5 - saScore * 0.035)), 3)

  // 结构警报：用可识别的子结构片段近似（真实实现为 RDKit FilterCatalog）
  const painsPatterns = ['N(=O)=O', 'C(=O)Cl', 'N=[N+]=[N-]', 'C=C(C)C(=O)', 'SC#N']
  const brenkPatterns = ['C(F)(F)F', 'C#N', 'S(=O)(=O)N', 'I']
  const painsHits = painsPatterns.filter((p) => smiles.includes(p)).length
  const brenkHits = brenkPatterns.filter((p) => smiles.includes(p)).length

  return {
    heavyAtoms,
    mw: round(mw, 2),
    logp,
    tpsa,
    hDonors,
    hAcceptors,
    rotatableBonds,
    fractionCsp3,
    ringCount,
    halogenCount: halogen,
    qed,
    saScore,
    painsHits,
    brenkHits
  }
}

/* ================================================================== */
/* 3. 约束定义与校验（真实判定逻辑）                                    */
/* ================================================================== */

/** 内置约束集：覆盖设计文档 03 的规则类型，HARD/SOFT 分级 */
function buildConstraintSet(projectId: number): ConstraintSet {
  const rule = (
    id: number,
    ruleCode: string,
    ruleName: string,
    ruleType: ConstraintRule['ruleType'],
    severity: ConstraintRule['severity'],
    params: Record<string, unknown>,
    weight?: number
  ): ConstraintRule => ({
    id,
    ruleCode,
    ruleName,
    ruleType,
    severity,
    params,
    weight,
    enabled: true
  })

  return {
    id: projectId * 10 + 1,
    name: 'EGFR 选择性候选约束集',
    version: 2,
    status: 'ACTIVE',
    rules: [
      rule(1, 'MW_RANGE', '分子量', 'MOLECULAR_WEIGHT', 'HARD', { min: 200, max: 500 }),
      rule(2, 'LOGP_RANGE', '脂水分配系数 LogP', 'LOGP', 'HARD', { min: 1, max: 5 }),
      rule(3, 'TPSA_MAX', '极性表面积 TPSA', 'TPSA', 'HARD', { max: 140 }),
      rule(4, 'HBD_MAX', '氢键供体数', 'H_BOND_DONOR', 'HARD', { max: 5 }),
      rule(5, 'HBA_MAX', '氢键受体数', 'H_BOND_ACCEPTOR', 'HARD', { max: 10 }),
      rule(6, 'ROTB_MAX', '可旋转键数', 'ROTATABLE_BOND', 'HARD', { max: 10 }),
      rule(7, 'SA_MAX', '合成可及性 SA Score', 'SA_SCORE', 'SOFT', { max: 6 }, 0.3),
      rule(8, 'QED_MIN', '类药性 QED', 'QED', 'SOFT', { min: 0.5 }, 0.4),
      rule(9, 'PAINS_EXCLUDE', '排除 PAINS 结构警报', 'SUBSTRUCTURE_EXCLUDE', 'HARD', {
        forbidden: ['N(=O)=O', 'C(=O)Cl', 'N=[N+]=[N-]']
      }),
      rule(10, 'RING_REQUIRED', '必须含环系', 'SUBSTRUCTURE_REQUIRE', 'HARD', {
        required: ['1']
      }),
      rule(11, 'HALOGEN_LIMIT', '卤素取代数', 'SUBSTRUCTURE_EXCLUDE', 'SOFT', { maxHalogen: 2 }, 0.2)
    ]
  }
}

const inRange = (value: number, min?: number, max?: number): boolean =>
  (min === undefined || value >= min) && (max === undefined || value <= max)

/** 逐条判定：返回每条规则的通过情况（真实逻辑，非假数据） */
function evaluateRules(descriptors: Descriptors, smiles: string, rules: ConstraintRule[]): ConstraintViolation[] {
  const fmt = (n: number, unit = ''): string => `${round(n, 1)}${unit}`

  return rules
    .filter((r) => r.enabled)
    .map((r) => {
      const p = r.params
      let passed = true
      let actual = ''
      let expected = ''

      switch (r.ruleType) {
        case 'MOLECULAR_WEIGHT': {
          const min = p.min as number | undefined
          const max = p.max as number | undefined
          passed = inRange(descriptors.mw, min, max)
          actual = fmt(descriptors.mw, ' Da')
          expected = `${min ?? '-'} ~ ${max ?? '-'} Da`
          break
        }
        case 'LOGP': {
          const min = p.min as number | undefined
          const max = p.max as number | undefined
          passed = inRange(descriptors.logp, min, max)
          actual = fmt(descriptors.logp)
          expected = `${min ?? '-'} ~ ${max ?? '-'}`
          break
        }
        case 'TPSA': {
          const max = p.max as number | undefined
          passed = inRange(descriptors.tpsa, undefined, max)
          actual = fmt(descriptors.tpsa, ' Å²')
          expected = `≤ ${max ?? '-'} Å²`
          break
        }
        case 'H_BOND_DONOR': {
          const max = p.max as number | undefined
          passed = inRange(descriptors.hDonors, undefined, max)
          actual = `${descriptors.hDonors} 个`
          expected = `≤ ${max ?? '-'} 个`
          break
        }
        case 'H_BOND_ACCEPTOR': {
          const max = p.max as number | undefined
          passed = inRange(descriptors.hAcceptors, undefined, max)
          actual = `${descriptors.hAcceptors} 个`
          expected = `≤ ${max ?? '-'} 个`
          break
        }
        case 'ROTATABLE_BOND': {
          const max = p.max as number | undefined
          passed = inRange(descriptors.rotatableBonds, undefined, max)
          actual = `${descriptors.rotatableBonds} 个`
          expected = `≤ ${max ?? '-'} 个`
          break
        }
        case 'SA_SCORE': {
          const max = p.max as number | undefined
          passed = inRange(descriptors.saScore, undefined, max)
          actual = fmt(descriptors.saScore)
          expected = `≤ ${max ?? '-'}`
          break
        }
        case 'QED': {
          const min = p.min as number | undefined
          passed = inRange(descriptors.qed, min, undefined)
          actual = fmt(descriptors.qed)
          expected = `≥ ${min ?? '-'}`
          break
        }
        case 'SUBSTRUCTURE_EXCLUDE': {
          const forbidden = (p.forbidden as string[] | undefined) ?? []
          const maxHalogen = p.maxHalogen as number | undefined
          if (maxHalogen !== undefined) {
            const halogen =
              (smiles.match(/F|Cl|Br|I/g) ?? []).length
            passed = halogen <= maxHalogen
            actual = `${halogen} 个卤素`
            expected = `≤ ${maxHalogen} 个`
          } else {
            const hit = forbidden.filter((f) => smiles.includes(f))
            passed = hit.length === 0
            actual = hit.length ? `命中 ${hit.join('、')}` : '未命中'
            expected = '不得命中'
          }
          break
        }
        case 'SUBSTRUCTURE_REQUIRE': {
          const required = (p.required as string[] | undefined) ?? []
          passed = required.every((token) => smiles.includes(token))
          actual = passed ? '含环系' : '无环'
          expected = '必须含环系'
          break
        }
        case 'SIMILARITY_TO_REFERENCE': {
          // 该规则需要参考分子指纹，Mock 中默认放行
          passed = true
          actual = '未启用'
          expected = '—'
          break
        }
        default:
          actual = '—'
          expected = '—'
          break
      }

      return {
        ruleCode: r.ruleCode,
        ruleName: r.ruleName,
        severity: r.severity,
        passed,
        actual,
        expected
      }
    })
}

/* ================================================================== */
/* 4. 指标定义与综合得分（表驱动 + 真实加权公式）                       */
/* ================================================================== */

const METRIC_DEFINITIONS: MetricDefinition[] = [
  { metricCode: 'MOL_WT', metricName: '分子量', category: 'PROPERTY', valueType: 'NUMERIC', unit: 'Da', better: 'NONE', minValue: 0, maxValue: 1000, weight: 0, enabled: true },
  { metricCode: 'MOL_LOGP', metricName: '脂水分配系数', category: 'PROPERTY', valueType: 'NUMERIC', better: 'NONE', minValue: -3, maxValue: 8, weight: 0, enabled: true },
  { metricCode: 'TPSA', metricName: '极性表面积', category: 'PROPERTY', valueType: 'NUMERIC', unit: 'Å²', better: 'NONE', minValue: 0, maxValue: 250, weight: 0, enabled: true },
  { metricCode: 'NUM_H_DONORS', metricName: '氢键供体数', category: 'PROPERTY', valueType: 'NUMERIC', better: 'LOWER', minValue: 0, maxValue: 10, weight: 0, enabled: true },
  { metricCode: 'NUM_H_ACCEPTORS', metricName: '氢键受体数', category: 'PROPERTY', valueType: 'NUMERIC', better: 'LOWER', minValue: 0, maxValue: 15, weight: 0, enabled: true },
  { metricCode: 'NUM_ROTATABLE', metricName: '可旋转键数', category: 'PROPERTY', valueType: 'NUMERIC', better: 'LOWER', minValue: 0, maxValue: 15, weight: 0, enabled: true },
  { metricCode: 'FRACTION_CSP3', metricName: 'sp³ 碳占比', category: 'PROPERTY', valueType: 'NUMERIC', better: 'HIGHER', minValue: 0, maxValue: 1, weight: 0, enabled: true },
  { metricCode: 'QED', metricName: '类药性 QED', category: 'DRUGLIKENESS', valueType: 'NUMERIC', better: 'HIGHER', minValue: 0, maxValue: 1, weight: 0.15, enabled: true },
  { metricCode: 'LIPINSKI_PASS', metricName: 'Lipinski 通过数', category: 'DRUGLIKENESS', valueType: 'NUMERIC', unit: '条', better: 'HIGHER', minValue: 0, maxValue: 4, weight: 0.05, enabled: true },
  { metricCode: 'SA_SCORE', metricName: '合成可及性', category: 'SYNTHESIS', valueType: 'NUMERIC', better: 'LOWER', minValue: 1, maxValue: 10, weight: 0.1, enabled: true },
  { metricCode: 'PAINS_HITS', metricName: 'PAINS 命中数', category: 'SAFETY', valueType: 'NUMERIC', unit: '个', better: 'LOWER', minValue: 0, maxValue: 5, weight: 0.1, enabled: true },
  { metricCode: 'BRENK_HITS', metricName: 'BRENK 命中数', category: 'SAFETY', valueType: 'NUMERIC', unit: '个', better: 'LOWER', minValue: 0, maxValue: 5, weight: 0.05, enabled: true },
  { metricCode: 'MAX_SIMILARITY', metricName: '最大相似度', category: 'DIVERSITY', valueType: 'NUMERIC', better: 'LOWER', minValue: 0, maxValue: 1, weight: 0.05, enabled: true },
  { metricCode: 'DTI_CONFIDENCE', metricName: 'DTI 结合置信度', category: 'ACTIVITY', valueType: 'NUMERIC', better: 'HIGHER', minValue: 0, maxValue: 1, weight: 0.3, enabled: true },
  { metricCode: 'DDI_PROBABILITY', metricName: 'DDI 相互作用概率', category: 'ACTIVITY', valueType: 'NUMERIC', better: 'LOWER', minValue: 0, maxValue: 1, weight: 0.05, enabled: true },
  { metricCode: 'SELECTIVITY_INDEX', metricName: '选择性指数', category: 'SELECTIVITY', valueType: 'NUMERIC', better: 'HIGHER', minValue: 0, maxValue: 10, weight: 0.1, enabled: true },
  /**
   * 对接引擎（AutoDock Vina）未部署 → 该指标不注册、不参与打分。
   * 保留定义是为了让界面能展示「未启用」而不是静默消失（对应设计文档 06 的降级可见）。
   */
  { metricCode: 'VINA_AFFINITY', metricName: 'Vina 对接打分', category: 'DOCKING', valueType: 'NUMERIC', unit: 'kcal/mol', better: 'LOWER', minValue: -14, maxValue: 0, weight: 0.1, enabled: false }
]

const METRIC_BY_CODE = new Map(METRIC_DEFINITIONS.map((m) => [m.metricCode, m]))

/** 归一化到 0~1；better=LOWER 时取反 */
function normalize(metric: MetricDefinition, value: number): number {
  const min = metric.minValue ?? 0
  const max = metric.maxValue ?? 1
  const span = max - min || 1
  const raw = Math.min(1, Math.max(0, (value - min) / span))
  return metric.better === 'LOWER' ? 1 - raw : raw
}

function gradeOf(normalized: number): string {
  if (normalized >= 0.8) return 'EXCELLENT'
  if (normalized >= 0.6) return 'GOOD'
  if (normalized >= 0.4) return 'FAIR'
  return 'POOR'
}

/** 由结构派生全部指标值（数值确定性、公式真实） */
function deriveMetrics(
  smiles: string,
  descriptors: Descriptors,
  maxSimilarity: number,
  violations: ConstraintViolation[]
): MetricValue[] {
  const rng = createRng(hashSeed(`metrics::${smiles}`))

  // Lipinski 通过数（真实判定）
  const lipinski = [
    descriptors.mw <= 500,
    descriptors.logp <= 5,
    descriptors.hDonors <= 5,
    descriptors.hAcceptors <= 10
  ].filter(Boolean).length

  // DTI 置信度：与类药性正相关，叠加确定性抖动
  const dtiConfidence = round(
    Math.min(0.98, Math.max(0.05, descriptors.qed * 0.7 + 0.15 + (rng() - 0.5) * 0.2)),
    4
  )
  // DDI 风险：杂原子越多越容易发生相互作用
  const ddiProbability = round(
    Math.min(0.95, Math.max(0.01, descriptors.hAcceptors * 0.045 + (rng() - 0.5) * 0.1)),
    4
  )
  // 选择性指数：突变体/野生型亲和力比（此处由结构与确定性抖动派生）
  const selectivityIndex = round(
    Math.max(0.2, 1 + descriptors.ringCount * 0.65 + (descriptors.halogenCount ?? 1) * 0.4 + (rng() - 0.5) * 1.4),
    2
  )

  const raw: Array<[string, number]> = [
    ['MOL_WT', descriptors.mw],
    ['MOL_LOGP', descriptors.logp],
    ['TPSA', descriptors.tpsa],
    ['NUM_H_DONORS', descriptors.hDonors],
    ['NUM_H_ACCEPTORS', descriptors.hAcceptors],
    ['NUM_ROTATABLE', descriptors.rotatableBonds],
    ['FRACTION_CSP3', descriptors.fractionCsp3],
    ['QED', descriptors.qed],
    ['LIPINSKI_PASS', lipinski],
    ['SA_SCORE', descriptors.saScore],
    ['PAINS_HITS', descriptors.painsHits],
    ['BRENK_HITS', descriptors.brenkHits],
    ['MAX_SIMILARITY', maxSimilarity],
    ['DTI_CONFIDENCE', dtiConfidence],
    ['DDI_PROBABILITY', ddiProbability],
    ['SELECTIVITY_INDEX', selectivityIndex]
  ]

  const values: MetricValue[] = raw.map(([code, value]) => {
    const definition = METRIC_BY_CODE.get(code)
    if (!definition) {
      return { metricCode: code, valueNum: value, success: false }
    }
    const normalized = normalize(definition, value)
    return {
      metricCode: code,
      valueNum: value,
      normalized: round(normalized, 4),
      grade: gradeOf(normalized),
      success: true
    }
  })

  // 约束合规作为一项布尔指标参与展示
  const hardViolations = violations.filter((v) => !v.passed && v.severity === 'HARD')
  values.push({
    metricCode: 'CONSTRAINT_PASS',
    passed: hardViolations.length === 0,
    success: true,
    grade: hardViolations.length === 0 ? 'EXCELLENT' : 'POOR',
    valueText: hardViolations.length === 0 ? '全部通过' : `违反 ${hardViolations.length} 条硬规则`
  })

  // 引擎不可用的指标显式标记为未成功（供界面降级展示）
  METRIC_DEFINITIONS.filter((m) => !m.enabled).forEach((m) => {
    values.push({ metricCode: m.metricCode, success: false, valueText: '引擎未启用' })
  })

  return values
}

/** 综合得分：设计文档 06 §7 的公式（真实加权 + SOFT 扣分 + 警报扣分） */
function computeCompositeScore(
  values: MetricValue[],
  violations: ConstraintViolation[],
  rules: ConstraintRule[]
): number {
  // 仅统计「成功且启用」的指标，缺失指标的权重按比例分摊（等价于只除以已成功权重和）
  const usable = values.filter((v) => {
    const definition = METRIC_BY_CODE.get(v.metricCode)
    return definition && definition.enabled && definition.weight > 0 && v.success && v.normalized !== undefined
  })

  const totalWeight = usable.reduce((sum, v) => sum + (METRIC_BY_CODE.get(v.metricCode)?.weight ?? 0), 0)
  if (totalWeight === 0) return 0

  const weighted = usable.reduce(
    (sum, v) => sum + (METRIC_BY_CODE.get(v.metricCode)?.weight ?? 0) * (v.normalized ?? 0),
    0
  )

  const ruleByCode = new Map(rules.map((r) => [r.ruleCode, r]))
  const softPenalty = Math.min(
    20,
    violations
      .filter((v) => !v.passed && v.severity === 'SOFT')
      .reduce((sum, v) => sum + (ruleByCode.get(v.ruleCode)?.weight ?? 0) * 10, 0)
  )

  const painsHits = values.find((v) => v.metricCode === 'PAINS_HITS')?.valueNum ?? 0
  const brenkHits = values.find((v) => v.metricCode === 'BRENK_HITS')?.valueNum ?? 0
  const alertPenalty = Math.min(15, painsHits * 5 + brenkHits * 2)

  return round(Math.max(0, (weighted / totalWeight) * 100 - softPenalty - alertPenalty), 2)
}

/* ================================================================== */
/* 5. 项目 / 靶点 / 轮次 / 候选 组装                                    */
/* ================================================================== */

const PROJECT_ID = 1001

const PROJECT: DesignProject = {
  id: PROJECT_ID,
  projectNo: 'P2026091801',
  name: 'EGFR 选择性抑制剂设计',
  goalText: '针对 EGFR 突变体（T790M）设计高选择性小分子抑制剂，尽量避开野生型以获得更好安全窗。',
  status: 'ACTIVE',
  currentRoundId: 3002,
  activeConstraintSetId: 10011,
  createdAt: '2026-09-18T09:12:00',
  updatedAt: '2026-09-23T08:20:00'
}

const TARGETS: ProjectTarget[] = [
  {
    id: 2001,
    targetId: 501,
    name: 'Epidermal growth factor receptor',
    chineseName: '表皮生长因子受体',
    uniprotId: 'P00533',
    geneName: 'EGFR',
    role: 'WILD_TYPE',
    isPrimary: false,
    sequenceLength: 1210,
    pdbIds: ['1M17', '4HJO']
  },
  {
    id: 2002,
    targetId: 502,
    name: 'EGFR T790M mutant',
    chineseName: 'EGFR T790M 突变体',
    uniprotId: 'P00533',
    geneName: 'EGFR',
    role: 'MUTANT',
    isPrimary: true,
    sequenceLength: 1210,
    pdbIds: ['3W2S', '5HG8']
  }
]

const POCKETS: BindingPocket[] = [
  {
    id: 4001,
    targetId: 2002,
    structureRef: '3W2S',
    pocketNo: 1,
    center: { x: 12.84, y: -3.27, z: 18.06 },
    radius: 9.4,
    druggability: 0.91,
    residues: ['Leu718', 'Val726', 'Ala743', 'Lys745', 'Met790', 'Leu792', 'Thr854', 'Asp855']
  },
  {
    id: 4002,
    targetId: 2001,
    structureRef: '1M17',
    pocketNo: 1,
    center: { x: 21.13, y: 4.86, z: -9.42 },
    radius: 8.7,
    druggability: 0.88,
    residues: ['Leu694', 'Val702', 'Ala719', 'Lys721', 'Met742', 'Leu764', 'Thr766', 'Asp831']
  }
]

const ROUNDS: DesignRound[] = [
  {
    id: 3001,
    roundNo: 1,
    name: 'R1 骨架枚举',
    status: 'CLOSED',
    candidateCount: 0,
    qualifiedCount: 0,
    metricSnapshot: { compositeScore: 58.4, qed: 0.62, saScore: 3.9, selectivityIndex: 2.1 },
    startedAt: '2026-09-18T10:00:00',
    closedAt: '2026-09-19T17:30:00'
  },
  {
    id: 3002,
    roundNo: 2,
    name: 'R2 R 基优化',
    status: 'EDITING',
    candidateCount: 0,
    qualifiedCount: 0,
    startedAt: '2026-09-20T09:00:00'
  }
]

const RUNS: GenerationRun[] = [
  {
    id: 6001,
    runNo: 'G20260918-001',
    projectId: PROJECT_ID,
    roundId: 3001,
    strategy: 'S1',
    strategyLabel: 'R 基枚举',
    params: { scaffolds: SCAFFOLDS.length, substituents: SUBSTITUENTS.length },
    status: 'SUCCESS',
    progress: 100,
    producedCount: 132,
    duplicateCount: 0,
    createdAt: '2026-09-18T10:00:00'
  },
  {
    id: 6002,
    runNo: 'G20260920-002',
    projectId: PROJECT_ID,
    roundId: 3002,
    strategy: 'S2',
    strategyLabel: '相似物扩展',
    params: { referenceCount: 12, radius: 2 },
    status: 'RUNNING',
    progress: 68,
    producedCount: 0,
    duplicateCount: 0,
    createdAt: '2026-09-20T09:00:00'
  }
]

/* ---------------------- 候选生成（含真实去重与谱系） ----------------- */

const pool = buildMoleculePool()

/** 判重表：InChIKey → 候选。模拟 candidate_molecule 上的唯一键 */
const byInchikey = new Map<string, DesignCandidate>()
let candidateSeq = 7000

interface Derived {
  candidate: DesignCandidate
  descriptors: Descriptors
  /** 计算一次后缓存，供列表 / 详情 / 轮次统计复用，避免重复计算 */
  metrics?: MetricValue[]
  violations?: ConstraintViolation[]
}

const derivedCache = new Map<number, Derived>()

function createCandidate(roundId: number, seed: SeedMolecule, originType: DesignCandidate['originType'], parentMolId?: number): DesignCandidate | null {
  const inchikey = deriveInchikey(seed.smiles)
  // 真实系统由 DB 唯一键兜底，这里等价地在内存判重
  if (byInchikey.has(inchikey)) return null

  const descriptors = estimateDescriptors(seed.smiles)
  const candidate: DesignCandidate = {
    id: ++candidateSeq,
    projectId: PROJECT_ID,
    roundId,
    inchikey,
    smiles: seed.smiles,
    displayName: `${seed.scaffolIndex + 1}-${seed.substituent.code}`,
    originType,
    parentMolId,
    generationRunId: roundId === 3001 ? 6001 : 6002,
    mw: descriptors.mw,
    logp: descriptors.logp,
    tpsa: descriptors.tpsa,
    saScore: descriptors.saScore,
    qed: descriptors.qed,
    compositeScore: 0,
    reviewStatus: 'NEW',
    createdAt: roundId === 3001 ? '2026-09-18T10:05:00' : '2026-09-20T09:30:00'
  }

  byInchikey.set(inchikey, candidate)
  derivedCache.set(candidate.id, { candidate, descriptors })
  return candidate
}

function allCandidates(): DesignCandidate[] {
  return Array.from(byInchikey.values())
}

const constraintSet = buildConstraintSet(PROJECT_ID)

/** 补全得分与分级（依赖全批上下文计算最大相似度、约束判定） */
function finalizeScores(): void {
  const candidates = allCandidates()

  // 相似度：用脚手架+取代基的字符集做粗略重叠度（真实系统用摩根指纹 Tanimoto）
  const fingerprintOf = (smiles: string): Set<string> => {
    const set = new Set<string>()
    for (let i = 0; i < smiles.length - 1; i++) set.add(smiles.slice(i, i + 2))
    return set
  }
  const fingerprints = new Map(candidates.map((c) => [c.id, fingerprintOf(c.smiles)]))

  candidates.forEach((candidate) => {
    const own = fingerprints.get(candidate.id)
    let maxSimilarity = 0
    if (own) {
      candidates.forEach((other) => {
        if (other.id === candidate.id) return
        const target = fingerprints.get(other.id)
        if (!target) return
        let inter = 0
        own.forEach((token) => {
          if (target.has(token)) inter++
        })
        const union = own.size + target.size - inter
        const tanimoto = union === 0 ? 0 : inter / union
        if (tanimoto > maxSimilarity) maxSimilarity = tanimoto
      })
    }

    const derived = derivedCache.get(candidate.id)
    if (!derived) return

    const violations = evaluateRules(derived.descriptors, candidate.smiles, constraintSet.rules)
    const values = deriveMetrics(candidate.smiles, derived.descriptors, round(maxSimilarity, 3), violations)
    derived.violations = violations
    derived.metrics = values
    const hardFailed = violations.some((v) => !v.passed && v.severity === 'HARD')
    // 对齐设计文档 06：硬规则未全部通过则「得分不算」，置 0 使其自然沉底
    candidate.compositeScore = hardFailed ? 0 : computeCompositeScore(values, violations, constraintSet.rules)

    candidate.reviewStatus = hardFailed
      ? 'REJECTED'
      : candidate.compositeScore >= 75
        ? 'TOP'
        : candidate.compositeScore >= 55
          ? 'QUALIFIED'
          : 'NEW'
  })

  // 回填轮次统计（注意：回调参数不能命名为 round，会遮蔽上方的取整函数）
  ROUNDS.forEach((roundItem) => {
    const list = candidates.filter((c) => c.roundId === roundItem.id)
    roundItem.candidateCount = list.length
    roundItem.qualifiedCount = list.filter(
      (c) => c.reviewStatus === 'TOP' || c.reviewStatus === 'QUALIFIED'
    ).length

    const selectivityOf = (id: number): number =>
      derivedCache.get(id)?.metrics?.find((m) => m.metricCode === 'SELECTIVITY_INDEX')?.valueNum ?? 0

    // 轮次结论只统计通过硬约束的候选：被淘汰分子得分为 0，混入会掩盖真实水平
    const passed = list.filter((c) => c.reviewStatus !== 'REJECTED')
    const base = passed.length ? passed : list

    roundItem.metricSnapshot = list.length
      ? {
          passRate: round((passed.length / list.length) * 100, 1),
          compositeScore: round(base.reduce((s, c) => s + c.compositeScore, 0) / base.length, 2),
          qed: round(base.reduce((s, c) => s + c.qed, 0) / base.length, 3),
          saScore: round(base.reduce((s, c) => s + c.saScore, 0) / base.length, 2),
          selectivityIndex: round(
            base.reduce((s, c) => s + selectivityOf(c.id), 0) / base.length,
            2
          )
        }
      : undefined
  })
}

/**
 * 初始化：R1 = 骨架枚举，R2 = 在 R1 优质候选上做 R 基优化。
 *
 * R2 的做法对应真实工作流 —— 保持骨架不变、只替换 R 基，
 * 因此 R2 的通过率天然高于 R1，且形成真实的父→子谱系（撤销即回到父）。
 */
function seedData(): void {
  if (byInchikey.size > 0) return

  // R1：骨架枚举（池已按取代基优先排列，骨架覆盖均匀）
  const r1Seeds = pool.slice(0, 132)
  r1Seeds.forEach((seed) => createCandidate(3001, seed, 'GENERATED'))

  finalizeScores()

  const keyOf = (scaffolIndex: number, code: string): string => `${scaffolIndex}::${code}`
  const taken = new Set(r1Seeds.map((s) => keyOf(s.scaffolIndex, s.substituent.code)))

  // 取 R1 中通过硬约束的候选作为父代
  const parents = allCandidates()
    .filter((c) => c.reviewStatus !== 'REJECTED')
    .slice(0, 24)

  parents.forEach((parent) => {
    const parentSeed = pool.find((s) => s.smiles === parent.smiles)
    if (!parentSeed) return

    // 同一骨架上找一个尚未使用过的取代基 —— 只换 R 基，不动骨架
    const optimized = pool.find(
      (s) =>
        s.scaffolIndex === parentSeed.scaffolIndex &&
        !taken.has(keyOf(s.scaffolIndex, s.substituent.code))
    )
    if (!optimized) return

    taken.add(keyOf(optimized.scaffolIndex, optimized.substituent.code))
    createCandidate(3002, optimized, 'EDITED', parent.id)
  })

  finalizeScores()
}

/* ---------------------- 引擎真值回填（可选增强） ---------------------- */

/** 已回填的引擎结果：SMILES → 事实。用于判断是否需重算 */
const engineAnalysis = new Map<string, EngineAnalysis>()

/**
 * 把化学引擎（编辑器子应用里的 Indigo）算出的真值回填到演示数据。
 *
 * 为什么需要：mock 为了列表秒开，用轻量估算给出 InChIKey / 分子量，
 * 而 InChIKey 是全平台去重键、分子量是硬约束判据，二者都要求真值。
 *
 * 只覆盖引擎能给的三项（InChIKey / 分子量 / 分子式）：
 * logP、QED、TPSA、SA 等 Indigo 不提供，仍保持估算（后端就绪后由后端计算）。
 *
 * 分子量参与硬约束判定，因此回填后必须重算得分、分级与轮次统计。
 *
 * @returns 实际发生变更的候选数
 */
export function applyAnalysis(items: EngineAnalysis[]): number {
  if (!items.length) return 0
  seedData()

  let changed = 0

  items.forEach((item) => {
    if (!item?.smiles || !item.inchikey) return

    const previous = engineAnalysis.get(item.smiles)
    engineAnalysis.set(item.smiles, item)

    // 同一结构重复校验且结果一致时跳过，避免无意义的全量重算
    if (
      previous &&
      previous.inchikey === item.inchikey &&
      previous.molecularWeight === item.molecularWeight
    ) {
      return
    }

    const candidate = allCandidates().find((c) => c.smiles === item.smiles)
    if (!candidate) return

    // 引擎真值替换估算分子量：候选冗余列与判定用的描述符必须同步更新
    const mw = round(item.molecularWeight, 2)
    candidate.mw = mw
    candidate.mwEngine = mw
    candidate.formula = item.formula
    candidate.inchikeySource = 'ENGINE'

    const derived = derivedCache.get(candidate.id)
    if (derived) derived.descriptors.mw = mw

    // InChIKey 变了要同步维护判重表，否则后续判重会漏
    if (candidate.inchikey !== item.inchikey) {
      byInchikey.delete(candidate.inchikey)
      candidate.inchikey = item.inchikey
      byInchikey.set(item.inchikey, candidate)
    }

    changed++
  })

  if (changed > 0) finalizeScores()
  return changed
}

/* ================================================================== */
/* 6. 对外功能（与真实接口一一对应，便于整体替换）                       */
/* ================================================================== */

let workspace: WorkspaceState = {
  projectId: PROJECT_ID,
  lastRoundId: 3002,
  activeTab: 'canvas',
  openMolIds: [],
  panelLayout: { left: 232, right: 268 }
}

export const designCapabilities: EvaluationCapabilities = {
  docking: false, // 未部署 AutoDock Vina → 对应指标不参与打分
  admet: false,
  dti: true,
  ddi: true,
  ppi: true
}

export const designCommands: DesignCommand[] = [
  { id: 'target.resolve', label: '解析靶点', group: '靶点', writes: true, available: true },
  { id: 'pocket.detect', label: '检测结合口袋', group: '靶点', writes: true, available: true },
  { id: 'constraint.validate', label: '校验约束', group: '约束', writes: false, available: true },
  { id: 'molecule.generate', label: '生成候选分子', group: '生成', writes: true, available: true },
  { id: 'molecule.edit', label: '编辑分子', group: '编辑', writes: true, available: true },
  { id: 'molecule.evaluate', label: '评估候选', group: '评估', writes: true, available: true },
  { id: 'dti.predict', label: '预测结合活性', group: '评估', writes: true, available: true },
  {
    id: 'molecule.dock',
    label: '分子对接',
    group: '评估',
    writes: true,
    available: false,
    reason: '未部署对接引擎（AutoDock Vina）'
  },
  { id: 'project.context', label: '读取项目上下文', group: '项目', writes: false, available: true }
]

export const mockDesignApi = {
  async fetchTree(projectId: number): Promise<DesignTree> {
    await latency(projectId)
    seedData()
    return {
      project: { ...PROJECT, id: projectId },
      targets: TARGETS,
      pockets: POCKETS,
      constraintSet: JSON.parse(JSON.stringify(constraintSet)) as ConstraintSet,
      rounds: ROUNDS.map((r) => ({ ...r })),
      runs: RUNS.map((r) => ({ ...r }))
    }
  },

  async fetchCandidates(query: CandidateQuery): Promise<Page<DesignCandidate>> {
    await latency(query.page)
    seedData()

    let list = allCandidates().filter((c) => c.projectId === query.projectId)
    if (query.roundId) list = list.filter((c) => c.roundId === query.roundId)
    if (query.originType) list = list.filter((c) => c.originType === query.originType)
    if (query.reviewStatus) list = list.filter((c) => c.reviewStatus === query.reviewStatus)
    if (query.keyword) {
      const kw = query.keyword.trim().toLowerCase()
      list = list.filter(
        (c) =>
          c.smiles.toLowerCase().includes(kw) ||
          c.inchikey.toLowerCase().includes(kw) ||
          (c.displayName ?? '').toLowerCase().includes(kw)
      )
    }

    const sortBy = query.sortBy ?? 'compositeScore'
    const order = query.sortOrder === 'asc' ? 1 : -1
    list = [...list].sort((a, b) => {
      const av = (a as unknown as Record<string, number | string>)[sortBy]
      const bv = (b as unknown as Record<string, number | string>)[sortBy]
      if (typeof av === 'number' && typeof bv === 'number') return (av - bv) * order
      return String(av).localeCompare(String(bv)) * order
    })

    const total = list.length
    const start = (query.page - 1) * query.pageSize
    return {
      list: list.slice(start, start + query.pageSize),
      total,
      page: query.page,
      pageSize: query.pageSize
    }
  },

  async fetchMoleculeMetrics(moleculeId: number): Promise<MoleculeMetrics> {
    await latency(moleculeId)
    seedData()

    const candidate = allCandidates().find((c) => c.id === moleculeId)
    if (!candidate) throw new Error('候选分子不存在')

    const derived = derivedCache.get(moleculeId)
    const descriptors = derived?.descriptors ?? estimateDescriptors(candidate.smiles)
    const violations = evaluateRules(descriptors, candidate.smiles, constraintSet.rules)
    const values = deriveMetrics(candidate.smiles, descriptors, 0, violations)

    return {
      moleculeId,
      inchikey: candidate.inchikey,
      compositeScore: candidate.compositeScore,
      values,
      violations
    }
  },

  async fetchMetricDefinitions(): Promise<MetricDefinition[]> {
    await latency(11)
    return METRIC_DEFINITIONS.map((m) => ({ ...m }))
  },

  async fetchWorkspace(): Promise<WorkspaceState> {
    await latency(7)
    return { ...workspace }
  },

  async saveWorkspace(state: Partial<WorkspaceState>): Promise<void> {
    await latency(5)
    workspace = { ...workspace, ...state }
  },

  async fetchCommands(): Promise<DesignCommand[]> {
    await latency(3)
    return designCommands.map((c) => ({ ...c }))
  },

  async fetchCapabilities(): Promise<EvaluationCapabilities> {
    await latency(2)
    return { ...designCapabilities }
  },

  /**
   * 生成候选：追加到指定轮次。
   * 真实实现为「建 generation_run → MQ → FastAPI /v1/generate → 分批落库」，
   * 此处同步产出以保持契约一致（返回 runId 供前端展示进度）。
   */
  async generateCandidates(payload: {
    projectId: number
    roundId: number
    count: number
  }): Promise<{ runId: number; producedCount: number; duplicateCount: number }> {
    await latency(payload.count)
    seedData()

    const used = new Set(allCandidates().map((c) => c.inchikey))
    const remaining = pool.filter((seed) => !used.has(deriveInchikey(seed.smiles)))

    let produced = 0
    let duplicate = 0
    remaining.slice(0, payload.count).forEach((seed) => {
      if (createCandidate(payload.roundId, seed, 'GENERATED')) produced++
      else duplicate++
    })

    finalizeScores()
    return { runId: Date.now(), producedCount: produced, duplicateCount: duplicate }
  },

  /** 编辑保存：以选中候选为父，产生子候选并保留谱系（撤销即回到父） */
  async saveEditedMolecule(payload: {
    parentMolId: number
    roundId: number
    smiles: string
  }): Promise<DesignCandidate> {
    await latency(payload.parentMolId)

    const inchikey = deriveInchikey(payload.smiles)
    const existing = byInchikey.get(inchikey)
    if (existing) return { ...existing }

    const descriptors = estimateDescriptors(payload.smiles)
    const parent = allCandidates().find((c) => c.id === payload.parentMolId)

    const candidate: DesignCandidate = {
      id: ++candidateSeq,
      projectId: PROJECT_ID,
      roundId: payload.roundId,
      inchikey,
      smiles: payload.smiles,
      displayName: parent ? `${parent.displayName ?? ''}·改` : '手工编辑',
      originType: 'EDITED',
      parentMolId: payload.parentMolId,
      mw: descriptors.mw,
      logp: descriptors.logp,
      tpsa: descriptors.tpsa,
      saScore: descriptors.saScore,
      qed: descriptors.qed,
      compositeScore: 0,
      reviewStatus: 'NEW',
      createdAt: new Date().toISOString()
    }

    byInchikey.set(inchikey, candidate)
    derivedCache.set(candidate.id, { candidate, descriptors })
    finalizeScores()

    return { ...candidate }
  }
}

export type MockDesignApi = typeof mockDesignApi
