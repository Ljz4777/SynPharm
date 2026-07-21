import { request } from '@/utils/request'
import type { LoginCredentials, RegisterData, User } from '@/types'

export interface LoginResponse {
  token: string
  user: User
}

export const authApi = {
  login(data: LoginCredentials): Promise<LoginResponse> {
    return request.post<LoginResponse>('/api/auth/login', data)
  },

  register(data: RegisterData): Promise<LoginResponse> {
    return request.post<LoginResponse>('/api/auth/register', data)
  },

  guestLogin(): Promise<LoginResponse> {
    return request.post<LoginResponse>('/api/auth/guest')
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
  }
}
