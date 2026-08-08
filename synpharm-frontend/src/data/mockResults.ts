import type { PredictionResult, Task, Target } from '@/types'

export const mockResults: PredictionResult[] = [
  {
    id: 'result_001',
    targetId: 'P01234',
    targetName: 'ACE2',
    ligandSmiles: 'C(=O)(C(=O)O)NC(CCC(=O)O)C(=O)O',
    bindingAffinity: -9.2,
    confidenceScore: 0.92,
    confidenceLevel: 'high',
    interactions: [
      { type: 'hydrogen_bond', residueName: 'ASP', residueNumber: 30, distance: 2.8 },
      { type: 'hydrogen_bond', residueName: 'GLN', residueNumber: 24, distance: 3.1 },
      { type: 'hydrophobic', residueName: 'PHE', residueNumber: 45, distance: 4.2 },
      { type: 'ionic', residueName: 'LYS', residueNumber: 19, distance: 3.5 }
    ],
    createdAt: new Date(Date.now() - 86400000).toISOString(),
    datasetInfo: {
      name: 'DrugBank精选数据集',
      size: 12500,
      description: '包含FDA批准药物及其靶点相互作用数据',
      source: 'internal'
    }
  },
  {
    id: 'result_002',
    targetId: 'Q9Y2W8',
    targetName: 'SARS-CoV-2 Spike',
    ligandSmiles: 'CC(=O)OC1=CC=CC=C1C(=O)O',
    bindingAffinity: -7.8,
    confidenceScore: 0.85,
    confidenceLevel: 'high',
    interactions: [
      { type: 'hydrogen_bond', residueName: 'SER', residueNumber: 494, distance: 2.9 },
      { type: 'hydrogen_bond', residueName: 'ASN', residueNumber: 501, distance: 3.2 },
      { type: 'pi_pi', residueName: 'PHE', residueNumber: 486, distance: 4.8 }
    ],
    createdAt: new Date(Date.now() - 172800000).toISOString(),
    datasetInfo: {
      name: 'COVID-19专项数据集',
      size: 8500,
      description: '新冠病毒相关靶点与药物相互作用数据',
      source: 'internal'
    }
  },
  {
    id: 'result_003',
    targetId: 'P36888',
    targetName: 'EGFR',
    ligandSmiles: 'CN1CCN(CC1)C2=CC(=CC=C2)C(=O)NC3=CC=CC=C3',
    bindingAffinity: -10.5,
    confidenceScore: 0.96,
    confidenceLevel: 'high',
    interactions: [
      { type: 'hydrogen_bond', residueName: 'MET', residueNumber: 793, distance: 2.7 },
      { type: 'hydrogen_bond', residueName: 'THR', residueNumber: 854, distance: 3.0 },
      { type: 'hydrophobic', residueName: 'LEU', residueNumber: 844, distance: 4.0 },
      { type: 'ionic', residueName: 'LYS', residueNumber: 745, distance: 3.8 }
    ],
    createdAt: new Date(Date.now() - 259200000).toISOString(),
    datasetInfo: {
      name: 'Cancer Target Dataset',
      size: 25000,
      description: '癌症相关靶点药物筛选数据集',
      source: 'internal'
    }
  },
  {
    id: 'result_004',
    targetId: 'P05067',
    targetName: 'Alpha-synuclein',
    ligandSmiles: 'C1=CC=C(C=C1)C(=O)NCC2=CN=CC=C2',
    bindingAffinity: -6.2,
    confidenceScore: 0.72,
    confidenceLevel: 'medium',
    interactions: [
      { type: 'hydrogen_bond', residueName: 'TYR', residueNumber: 39, distance: 3.3 },
      { type: 'hydrophobic', residueName: 'VAL', residueNumber: 70, distance: 4.5 }
    ],
    createdAt: new Date(Date.now() - 345600000).toISOString(),
    datasetInfo: {
      name: 'Neurodegenerative Dataset',
      size: 6800,
      description: '神经退行性疾病相关靶点数据',
      source: 'internal'
    }
  },
  {
    id: 'result_005',
    targetId: 'P08237',
    targetName: 'Hsp90',
    ligandSmiles: 'CC(C)(C)C1=CC=C(C=C1)C(=O)N2CCN(CC2)C3=CC=C(C=C3)O',
    bindingAffinity: -8.9,
    confidenceScore: 0.88,
    confidenceLevel: 'high',
    interactions: [
      { type: 'hydrogen_bond', residueName: 'ASP', residueNumber: 93, distance: 2.6 },
      { type: 'hydrogen_bond', residueName: 'GLY', residueNumber: 94, distance: 2.9 },
      { type: 'hydrophobic', residueName: 'LEU', residueNumber: 107, distance: 4.1 },
      { type: 'metal', residueName: 'ZN', residueNumber: 101, distance: 2.1 }
    ],
    createdAt: new Date(Date.now() - 432000000).toISOString(),
    datasetInfo: {
      name: 'Chaperone Dataset',
      size: 4200,
      description: '分子伴侣蛋白相关数据集',
      source: 'internal'
    }
  }
]

export const mockTasks: Task[] = [
  {
    id: 'task_001',
    type: 'prediction',
    status: 'completed',
    progress: 100,
    input: {
      type: 'pdb',
      value: '6M0J'
    },
    resultId: 'result_001',
    createdAt: new Date(Date.now() - 86400000).toISOString(),
    updatedAt: new Date(Date.now() - 86000000).toISOString()
  },
  {
    id: 'task_002',
    type: 'prediction',
    status: 'completed',
    progress: 100,
    input: {
      type: 'smiles',
      value: 'C(C(=O)O)N'
    },
    resultId: 'result_002',
    createdAt: new Date(Date.now() - 172800000).toISOString(),
    updatedAt: new Date(Date.now() - 172400000).toISOString()
  },
  {
    id: 'task_003',
    type: 'batch_screening',
    status: 'completed',
    progress: 100,
    input: {
      type: 'csv',
      value: 'compounds.csv',
      fileName: 'compounds.csv'
    },
    createdAt: new Date(Date.now() - 259200000).toISOString(),
    updatedAt: new Date(Date.now() - 258000000).toISOString()
  },
  {
    id: 'task_004',
    type: 'validation',
    status: 'completed',
    progress: 100,
    input: {
      type: 'uniprot',
      value: 'P01234'
    },
    createdAt: new Date(Date.now() - 345600000).toISOString(),
    updatedAt: new Date(Date.now() - 345200000).toISOString()
  },
  {
    id: 'task_005',
    type: 'prediction',
    status: 'running',
    progress: 65,
    input: {
      type: 'smiles',
      value: 'CC(C)(C)NC(=O)CN'
    },
    createdAt: new Date(Date.now() - 3600000).toISOString(),
    updatedAt: new Date(Date.now() - 1800000).toISOString()
  }
]

export const mockTargets: Target[] = [
  {
    id: '1',
    name: 'ACE2',
    uniprotId: 'Q9BYF1',
    pdbId: '6M0J',
    description: '血管紧张素转化酶 2，SARS-CoV-2 进入宿主细胞的功能受体，参与血压调节。',
    status: 'supported',
    geneName: 'ACE2',
    organism: 'human',
    chineseName: '血管紧张素转化酶 2',
    targetType: '锌金属蛋白酶（跨膜）',
    family: '血管紧张素转化酶家族（ACE 家族）',
    pathway: '肾素-血管紧张素系统（RAS）；病毒入侵（ACE2 受体）',
    diseaseArea: '心血管 · 感染',
    relatedDiseases: '高血压；SARS-CoV-2 感染',
    knownDrugs: 'RAS 系统调节药（部分相关）；新冠中和抗体/进入抑制剂研究'
  },
  {
    id: '2',
    name: 'SARS-CoV-2 Spike',
    uniprotId: 'P0DTC2',
    pdbId: '6VYB',
    description: 'SARS-CoV-2 表面刺突糖蛋白，介导病毒与 ACE2 结合及膜融合，疫苗与中和抗体主要靶标。',
    status: 'supported',
    geneName: 'S',
    organism: 'human',
    chineseName: '新冠病毒刺突糖蛋白',
    targetType: '病毒 I 类融合蛋白（表面糖蛋白）',
    family: '冠状病毒刺突蛋白家族',
    pathway: '受体识别与膜融合（ACE2 → TMPRSS2）',
    diseaseArea: '感染',
    relatedDiseases: 'SARS-CoV-2 感染（COVID-19）',
    knownDrugs: 'mRNA 疫苗、重组蛋白疫苗、中和抗体'
  },
  {
    id: '3',
    name: 'EGFR',
    uniprotId: 'P00533',
    pdbId: '1M17',
    description: '表皮生长因子受体，激活下游增殖信号；突变/扩增与多种实体瘤相关。',
    status: 'supported',
    geneName: 'EGFR',
    organism: 'human',
    chineseName: '表皮生长因子受体',
    targetType: '受体酪氨酸激酶（RTK）',
    family: 'ErbB / HER 受体家族',
    pathway: 'RAS/MAPK；PI3K/AKT 信号通路',
    diseaseArea: '肿瘤',
    relatedDiseases: '非小细胞肺癌、结直肠癌、头颈部肿瘤',
    knownDrugs: '吉非替尼、厄洛替尼、奥希替尼；西妥昔单抗'
  },
  {
    id: '4',
    name: 'HER2',
    uniprotId: 'P04626',
    pdbId: '1N8Z',
    description: 'ERBB2 受体酪氨酸激酶，乳腺癌/胃癌重要驱动基因，过表达提示预后不良。',
    status: 'supported',
    geneName: 'ERBB2',
    organism: 'human',
    chineseName: '人表皮生长因子受体 2',
    targetType: '受体酪氨酸激酶（RTK）',
    family: 'ErbB / HER 受体家族',
    pathway: 'PI3K/AKT；RAS/MAPK 信号通路',
    diseaseArea: '肿瘤',
    relatedDiseases: 'HER2 阳性乳腺癌、胃癌',
    knownDrugs: '曲妥珠单抗、帕妥珠单抗、T-DXd（ADC）'
  },
  {
    id: '5',
    name: 'Alpha-synuclein',
    uniprotId: 'P37840',
    pdbId: '1XQ8',
    description: 'α-突触核蛋白，帕金森病 Lewy 小体主要成分，错误折叠聚集产生神经毒性。',
    status: 'beta',
    geneName: 'SNCA',
    organism: 'human',
    chineseName: 'α-突触核蛋白',
    targetType: '内在无序蛋白（聚集相关）',
    family: '突触核蛋白家族（Synuclein）',
    pathway: '突触囊泡运输；蛋白错误折叠/聚集',
    diseaseArea: '神经退行性疾病',
    relatedDiseases: '帕金森病、路易体痴呆',
    knownDrugs: '聚集抑制剂/免疫疗法（研究阶段）'
  },
  {
    id: '6',
    name: 'Hsp90',
    uniprotId: 'P07900',
    pdbId: '1YET',
    description: 'ATP 依赖分子伴侣，维持多种致癌客户蛋白构象稳定，抑制可同时降解多种癌蛋白。',
    status: 'supported',
    geneName: 'HSP90AA1',
    organism: 'human',
    chineseName: '热休克蛋白 90',
    targetType: '分子伴侣（ATP 酶）',
    family: 'HSP90 热休克蛋白家族',
    pathway: '蛋白折叠稳态；客户蛋白成熟（RTK、激酶等）',
    diseaseArea: '肿瘤',
    relatedDiseases: '多癌种（肺癌、乳腺癌、血液肿瘤等）',
    knownDrugs: '格尔德霉素类/雷普霉素类（研究阶段）'
  },
  {
    id: '7',
    name: 'CDK2',
    uniprotId: 'P24941',
    pdbId: '1AQ1',
    description: '细胞周期素依赖性激酶 2，驱动 G1/S 期转换，抑制可阻滞肿瘤细胞增殖。',
    status: 'supported',
    geneName: 'CDK2',
    organism: 'human',
    chineseName: '细胞周期素依赖性激酶 2',
    targetType: '丝/苏氨酸激酶（细胞周期激酶）',
    family: 'CDK 细胞周期素依赖性激酶家族',
    pathway: '细胞周期（G1/S 期转换）',
    diseaseArea: '肿瘤',
    relatedDiseases: '多癌种（乳腺癌、卵巢癌等）',
    knownDrugs: 'CDK 抑制剂（帕博西尼等以 CDK4/6 为主；CDK2 选择性抑制剂在研）'
  },
  {
    id: '8',
    name: 'PPAR-gamma',
    uniprotId: 'P37231',
    pdbId: '2PRG',
    description: '核受体转录因子，调控脂肪生成与糖代谢，为噻唑烷二酮类降糖药靶点。',
    status: 'beta',
    geneName: 'PPARG',
    organism: 'human',
    chineseName: '过氧化物酶体增殖物激活受体 γ',
    targetType: '核受体（转录因子）',
    family: 'PPAR 核受体亚家族',
    pathway: '脂质代谢；胰岛素敏感性调节',
    diseaseArea: '代谢性疾病',
    relatedDiseases: '2 型糖尿病、肥胖、非酒精性脂肪肝',
    knownDrugs: '罗格列酮、吡格列酮（噻唑烷二酮类）'
  },
  {
    id: '9',
    name: 'GPCR Family A',
    uniprotId: '-',
    pdbId: '-',
    description: 'G 蛋白偶联受体 A 类（视紫红质样）超家族，最大类药物靶点家族。',
    status: 'planned',
    geneName: '-',
    organism: 'human',
    chineseName: 'G 蛋白偶联受体 A 类',
    targetType: '七次跨膜 G 蛋白偶联受体（GPCR）',
    family: 'GPCR A 类（视紫红质样）超家族',
    pathway: 'GPCR 信号级联（G 蛋白 → 第二信使）',
    diseaseArea: '广谱（心血管 · 神经 · 内分泌）',
    relatedDiseases: '高血压、哮喘、精神疾病、内分泌疾病等',
    knownDrugs: 'β 受体阻滞剂、抗组胺药、5-HT 受体调节剂等'
  },
  {
    id: '10',
    name: 'Ion Channels',
    uniprotId: '-',
    pdbId: '-',
    description: '跨膜离子通道超家族，调控跨膜离子流，参与神经传导与心肌节律。',
    status: 'planned',
    geneName: '-',
    organism: 'human',
    chineseName: '离子通道',
    targetType: '离子通道（电压/配体门控）',
    family: '离子通道超家族（Na⁺/K⁺/Ca²⁺/Cl⁻ 通道）',
    pathway: '动作电位与兴奋性调控',
    diseaseArea: '神经 · 心血管',
    relatedDiseases: '癫痫、心律失常、慢性疼痛',
    knownDrugs: '钠通道阻滞剂、钙通道阻滞剂、部分抗癫痫药'
  }
]