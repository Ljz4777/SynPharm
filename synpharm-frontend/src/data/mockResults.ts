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
    description: '血管紧张素转换酶2，新冠病毒受体',
    status: 'supported'
  },
  {
    id: '2',
    name: 'SARS-CoV-2 Spike',
    uniprotId: 'P0DTC2',
    pdbId: '6VYB',
    description: '新冠病毒刺突蛋白',
    status: 'supported'
  },
  {
    id: '3',
    name: 'EGFR',
    uniprotId: 'P00533',
    pdbId: '1M17',
    description: '表皮生长因子受体，癌症治疗靶点',
    status: 'supported'
  },
  {
    id: '4',
    name: 'HER2',
    uniprotId: 'P04626',
    pdbId: '1N8Z',
    description: '人表皮生长因子受体2',
    status: 'supported'
  },
  {
    id: '5',
    name: 'Alpha-synuclein',
    uniprotId: 'P37840',
    pdbId: '1XQ8',
    description: 'α-突触核蛋白，帕金森病相关',
    status: 'beta'
  },
  {
    id: '6',
    name: 'Hsp90',
    uniprotId: 'P07900',
    pdbId: '1YET',
    description: '热休克蛋白90，癌症靶点',
    status: 'supported'
  },
  {
    id: '7',
    name: 'CDK2',
    uniprotId: 'P24941',
    pdbId: '1AQ1',
    description: '细胞周期蛋白依赖性激酶2',
    status: 'supported'
  },
  {
    id: '8',
    name: 'PPAR-gamma',
    uniprotId: 'P37231',
    pdbId: '2PRG',
    description: '过氧化物酶体增殖物激活受体γ',
    status: 'beta'
  },
  {
    id: '9',
    name: 'GPCR Family A',
    uniprotId: '-',
    pdbId: '-',
    description: 'G蛋白偶联受体A家族',
    status: 'planned'
  },
  {
    id: '10',
    name: 'Ion Channels',
    uniprotId: '-',
    pdbId: '-',
    description: '离子通道蛋白',
    status: 'planned'
  }
]