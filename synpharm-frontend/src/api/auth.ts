import { request } from '@/utils/request'
import type { LoginCredentials, User } from '@/types'

export interface LoginResponse {
  token: string
  user: User
}

export interface SendCaptchaResponse {
  success: boolean
}

export const authApi = {
  login(data: LoginCredentials): Promise<LoginResponse> {
    return request.post<LoginResponse>('/api/auth/login', data)
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

  sendCaptcha(email: string, type: 'login' | 'reset'): Promise<SendCaptchaResponse> {
    return request.post<SendCaptchaResponse>('/api/auth/captcha/send', { email, type })
  },

  resetPassword(email: string, captcha: string, newPassword: string): Promise<void> {
    return request.post<void>('/api/auth/password/reset', { email, captcha, newPassword })
  },

  debugLogin(captcha: string): Promise<LoginResponse> {
    return request.post<LoginResponse>('/api/auth/debug/login', { captcha })
  }
}