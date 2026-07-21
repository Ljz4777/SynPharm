import type { ValidationResult, FieldValidationResult } from '@/types'

export const validatePdbId = (value: string): ValidationResult => {
  const trimmed = value.trim()
  
  if (!trimmed) {
    return {
      inputType: 'pdb',
      inputValue: value,
      isValid: false,
      message: '请输入PDB ID'
    }
  }
  
  const pdbPattern = /^[1-9][A-Za-z0-9]{3}$/
  if (!pdbPattern.test(trimmed)) {
    return {
      inputType: 'pdb',
      inputValue: value,
      isValid: false,
      message: 'PDB ID格式不正确',
      suggestions: ['格式应为4个字符，如1ABC']
    }
  }
  
  return {
    inputType: 'pdb',
    inputValue: value,
    isValid: true,
    message: '有效'
  }
}

export const validateUniProtId = (value: string): ValidationResult => {
  const trimmed = value.trim()
  
  if (!trimmed) {
    return {
      inputType: 'uniprot',
      inputValue: value,
      isValid: false,
      message: '请输入UniProt ID'
    }
  }
  
  const uniprotPattern = /^[A-Z][A-Z0-9]{5}$/
  if (!uniprotPattern.test(trimmed)) {
    return {
      inputType: 'uniprot',
      inputValue: value,
      isValid: false,
      message: 'UniProt ID格式不正确',
      suggestions: ['格式应为6个字符，以字母开头，如P01234']
    }
  }
  
  return {
    inputType: 'uniprot',
    inputValue: value,
    isValid: true,
    message: '有效'
  }
}

export const validateSmiles = (value: string): ValidationResult => {
  const trimmed = value.trim()
  
  if (!trimmed) {
    return {
      inputType: 'smiles',
      inputValue: value,
      isValid: false,
      message: '请输入SMILES字符串'
    }
  }
  
  const smilesPattern = /^[A-Za-z0-9@+\-\[\]()=#$.%&\/\\~`'"]+$/
  if (!smilesPattern.test(trimmed)) {
    return {
      inputType: 'smiles',
      inputValue: value,
      isValid: false,
      message: 'SMILES格式不正确',
      suggestions: ['请输入有效的SMILES分子表示']
    }
  }
  
  if (trimmed.length > 1000) {
    return {
      inputType: 'smiles',
      inputValue: value,
      isValid: false,
      message: 'SMILES字符串过长',
      suggestions: ['建议长度不超过1000字符']
    }
  }
  
  return {
    inputType: 'smiles',
    inputValue: value,
    isValid: true,
    message: '有效'
  }
}

export const validateCsvFile = (fileName: string): ValidationResult => {
  if (!fileName) {
    return {
      inputType: 'csv',
      inputValue: fileName,
      isValid: false,
      message: '请选择CSV文件'
    }
  }
  
  if (!fileName.toLowerCase().endsWith('.csv')) {
    return {
      inputType: 'csv',
      inputValue: fileName,
      isValid: false,
      message: '请选择CSV格式文件'
    }
  }
  
  return {
    inputType: 'csv',
    inputValue: fileName,
    isValid: true,
    message: '有效'
  }
}

// ==========================================
// 表单验证相关
// ==========================================

// QQ邮箱正则：必须以 @qq.com 结尾，@前面为5-20位字母数字
export const QQ_EMAIL_REGEX = /^[a-zA-Z0-9]{5,20}@qq\.com$/i

// 密码正则：至少8位，必须包含大小写字母和数字
export const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[A-Za-z\d]{8,}$/

// 验证码正则：6位数字
export const CAPTCHA_REGEX = /^\d{6}$/

// 昵称正则：2-20位中文、字母、数字、下划线
export const NICKNAME_REGEX = /^[\u4e00-\u9fa5a-zA-Z0-9_]{2,20}$/

export const validateQqEmail = (email: string): FieldValidationResult => {
  const trimmed = email.trim()

  if (!trimmed) {
    return { valid: false, message: '请输入QQ邮箱' }
  }

  if (!QQ_EMAIL_REGEX.test(trimmed)) {
    return { valid: false, message: '请输入正确的QQ邮箱（如12345678@qq.com）' }
  }

  return { valid: true, message: '' }
}

export const validatePassword = (password: string): FieldValidationResult => {
  if (!password) {
    return { valid: false, message: '请输入密码' }
  }

  if (password.length < 8) {
    return { valid: false, message: '密码长度不少于8位' }
  }

  if (password.length > 64) {
    return { valid: false, message: '密码长度不能超过64位' }
  }

  if (!PASSWORD_REGEX.test(password)) {
    return { valid: false, message: '密码必须包含大小写字母和数字' }
  }

  return { valid: true, message: '' }
}

export const validateCaptcha = (captcha: string): FieldValidationResult => {
  if (!captcha) {
    return { valid: false, message: '请输入验证码' }
  }

  if (!CAPTCHA_REGEX.test(captcha)) {
    return { valid: false, message: '验证码为6位数字' }
  }

  return { valid: true, message: '' }
}

export const validateNickname = (nickname: string): FieldValidationResult => {
  const trimmed = nickname.trim()

  if (!trimmed) {
    return { valid: false, message: '请输入昵称' }
  }

  if (!NICKNAME_REGEX.test(trimmed)) {
    return { valid: false, message: '昵称长度2-20位，支持中文、字母、数字、下划线' }
  }

  return { valid: true, message: '' }
}