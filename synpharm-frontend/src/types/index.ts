export interface Target {
  id: string
  name: string
  uniprotId: string
  pdbId: string
  description: string
  status: 'supported' | 'beta' | 'planned'
  geneName?: string
  organism?: string
  pdbIds?: string[]
}

export interface InputData {
  type: 'pdb' | 'uniprot' | 'smiles' | 'csv'
  value: string
  fileName?: string
}

export interface PredictionResult {
  id: string | number
  targetId: string
  targetName: string
  ligandSmiles: string
  bindingAffinity: number
  confidenceScore: number
  confidenceLevel: 'high' | 'medium' | 'low'
  interactions: Interaction[]
  createdAt: string
  datasetInfo: DatasetInfo
}

export interface Interaction {
  type: 'hydrogen_bond' | 'hydrophobic' | 'ionic' | 'pi_pi' | 'metal'
  residueName: string
  residueNumber: number
  distance: number
}

export interface DatasetInfo {
  name: string
  size: number
  description: string
  source: 'internal'
}

export interface Task {
  id: string | number
  taskNo?: string
  name?: string
  type?: string
  /** 后端 predict_type（dti/ppi/ddi） */
  predictType?: string
  status: 'pending' | 'running' | 'completed' | 'failed' | 'cancelled'
  progress: number
  input?: InputData
  resultId?: string
  createdAt: string
  updatedAt?: string
}

export interface ValidationResult {
  inputType: string
  inputValue: string
  isValid: boolean
  message: string
  suggestions?: string[]
}

/** 表单字段验证结果（简单版，用于登录/注册等表单验证） */
export interface FieldValidationResult {
  valid: boolean
  message: string
}

export interface User {
  id: string
  email: string
  nickname: string
  avatar?: string
  createdAt: string
}

export interface LoginCredentials {
  loginType: 'qq_email' | 'guest' | 'password' | 'phone'
  email?: string
  captcha?: string
  password?: string
  phone?: string
}

export interface RegisterCredentials {
  email: string
  nickname: string
  password: string
  captcha: string
}

export interface AuthState {
  user: User | null
  isLoggedIn: boolean
  token: string | null
}

export interface RegisterResult {
  success: boolean
  message: string
  user?: User
}

export interface LoginResult {
  success: boolean
  message: string
  user?: User
  token?: string
}