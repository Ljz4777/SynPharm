import { request } from '@/utils/request'
import type { LoginCredentials, RegisterCredentials, User } from '@/types'

/** 后端用户信息 DTO（字段与 UserResponse 对应） */
export interface UserDTO {
  id: number | string
  email: string
  nickname: string
  avatarUrl?: string
  role?: string
  status?: number
  registerType?: string
  createdAt?: string
}

/** 后端登录/注册响应（字段与 LoginResponse 对应） */
export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  isNewUser: boolean
  user: UserDTO
}

export interface SendCaptchaResponse {
  success: boolean
  /** 是否开发模式（未配置发件邮箱，验证码直接返回给前端显示） */
  devMode?: boolean
  /** 开发模式下返回的验证码 */
  code?: string
}

export const authApi = {
  login(data: LoginCredentials): Promise<LoginResponse> {
    return request.post<LoginResponse>('/api/auth/login', data)
  },

  register(data: RegisterCredentials): Promise<LoginResponse> {
    return request.post<LoginResponse>('/api/auth/register', data)
  },

  logout(): Promise<void> {
    return request.post<void>('/api/auth/logout')
  },

  getProfile(): Promise<User> {
    return request.get<User>('/api/users/profile')
  },

  updateProfile(data: Partial<User>): Promise<User> {
    return request.put<User>('/api/users/profile', data)
  },

  changePassword(oldPassword: string, newPassword: string): Promise<void> {
    return request.put<void>('/api/users/password', null, {
      params: { oldPassword, newPassword }
    })
  },

  deleteAccount(password: string): Promise<void> {
    return request.delete<void>('/api/users/account', {
      params: { password }
    })
  },

  sendCaptcha(email: string, type: 'login' | 'register' | 'reset'): Promise<SendCaptchaResponse> {
    return request.post<SendCaptchaResponse>('/api/auth/captcha/send', { email, type })
  },

  resetPassword(email: string, captcha: string, newPassword: string): Promise<void> {
    return request.post<void>('/api/auth/password/reset', { email, captcha, newPassword })
  }
}