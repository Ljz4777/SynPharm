import { defineStore } from 'pinia'
import type { User, LoginCredentials, RegisterData, LoginResult, RegisterResult } from '@/types'
import { authApi } from '@/api/auth'

export interface SendCaptchaResult {
  success: boolean
  message: string
}

const STORAGE_KEY = {
  USER: 'auth_user',
  TOKEN: 'auth_token',
  IS_GUEST: 'auth_is_guest'
}

const USE_MOCK = import.meta.env.VITE_ENABLE_MOCK === 'true'

const generateId = (): string => {
  return 'user_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

const MOCK_USERS: Array<{ email: string; password: string; user: User }> = [
  {
    email: 'demo@protein.com',
    password: 'demo123',
    user: {
      id: 'user_demo_001',
      email: 'demo@protein.com',
      nickname: '演示用户',
      createdAt: '2024-01-01T00:00:00Z'
    }
  }
]

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as User | null,
    token: null as string | null,
    isLoggedIn: false,
    isGuest: false
  }),

  getters: {
    currentUser: (state) => state.user,
    isAuthenticated: (state) => state.isLoggedIn,
    userNickname: (state) => state.user?.nickname || '未登录',
    isGuestUser: (state) => state.isGuest
  },

  actions: {
    init() {
      const storedUser = localStorage.getItem(STORAGE_KEY.USER)
      const storedToken = localStorage.getItem(STORAGE_KEY.TOKEN)
      const storedIsGuest = localStorage.getItem(STORAGE_KEY.IS_GUEST)
      
      if (storedUser && storedToken) {
        try {
          this.user = JSON.parse(storedUser)
          this.token = storedToken
          this.isLoggedIn = true
          this.isGuest = storedIsGuest === 'true' || false
        } catch {
          // localStorage 数据损坏，清除后重置状态
          localStorage.removeItem(STORAGE_KEY.USER)
          localStorage.removeItem(STORAGE_KEY.TOKEN)
          localStorage.removeItem(STORAGE_KEY.IS_GUEST)
        }
      }
    },

    async login(credentials: LoginCredentials): Promise<LoginResult> {
      if (USE_MOCK) {
        return this.mockLogin(credentials)
      }
      
      try {
        const response = await authApi.login(credentials)
        this.user = response.user
        this.token = response.token
        this.isLoggedIn = true
        this.isGuest = false

        localStorage.setItem(STORAGE_KEY.USER, JSON.stringify(response.user))
        localStorage.setItem(STORAGE_KEY.TOKEN, response.token)
        localStorage.setItem(STORAGE_KEY.IS_GUEST, 'false')

        return {
          success: true,
          message: '登录成功',
          user: response.user,
          token: response.token
        }
      } catch (error: unknown) {
        const message = error instanceof Error ? error.message : '登录失败'
        return {
          success: false,
          message
        }
      }
    },

    async mockLogin(credentials: LoginCredentials): Promise<LoginResult> {
      await new Promise(resolve => setTimeout(resolve, 800))

      const mockUser = MOCK_USERS.find(
        u => u.email === credentials.email && u.password === credentials.password
      )

      if (mockUser) {
        const token = 'mock_token_' + Date.now()
        this.user = mockUser.user
        this.token = token
        this.isLoggedIn = true
        this.isGuest = false

        localStorage.setItem(STORAGE_KEY.USER, JSON.stringify(mockUser.user))
        localStorage.setItem(STORAGE_KEY.TOKEN, token)
        localStorage.setItem(STORAGE_KEY.IS_GUEST, 'false')

        return {
          success: true,
          message: '登录成功',
          user: mockUser.user,
          token
        }
      }

      return {
        success: false,
        message: '邮箱或密码错误'
      }
    },

    async register(data: RegisterData & { captcha: string }): Promise<RegisterResult> {
      if (USE_MOCK) {
        return this.mockRegister(data)
      }
      
      try {
        const response = await authApi.register(data)
        this.user = response.user
        this.token = response.token
        this.isLoggedIn = true
        this.isGuest = false

        localStorage.setItem(STORAGE_KEY.USER, JSON.stringify(response.user))
        localStorage.setItem(STORAGE_KEY.TOKEN, response.token)
        localStorage.setItem(STORAGE_KEY.IS_GUEST, 'false')

        return {
          success: true,
          message: '注册成功',
          user: response.user
        }
      } catch (error: unknown) {
        const message = error instanceof Error ? error.message : '注册失败'
        return {
          success: false,
          message
        }
      }
    },

    async sendCaptcha(email: string, type: string): Promise<SendCaptchaResult> {
      if (USE_MOCK) {
        await new Promise(resolve => setTimeout(resolve, 500))
        return {
          success: true,
          message: '验证码已发送，有效期1分钟'
        }
      }
      
      try {
        await authApi.sendCaptcha(email, type)
        return {
          success: true,
          message: '验证码已发送，有效期1分钟'
        }
      } catch (error: unknown) {
        const message = error instanceof Error ? error.message : '发送失败'
        return {
          success: false,
          message
        }
      }
    },

    async mockRegister(data: RegisterData & { captcha?: string }): Promise<RegisterResult> {
      await new Promise(resolve => setTimeout(resolve, 800))

      const existingUser = MOCK_USERS.find(u => u.email === data.email)
      if (existingUser) {
        return {
          success: false,
          message: '该邮箱已被注册'
        }
      }

      if (data.password !== data.confirmPassword) {
        return {
          success: false,
          message: '两次输入的密码不一致'
        }
      }

      if (data.password.length < 8) {
        return {
          success: false,
          message: '密码长度不少于8位'
        }
      }

      if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(data.password)) {
        return {
          success: false,
          message: '密码必须包含大小写字母和数字'
        }
      }

      const newUser: User = {
        id: generateId(),
        email: data.email,
        nickname: data.nickname || data.email.split('@')[0],
        createdAt: new Date().toISOString()
      }

      MOCK_USERS.push({
        email: data.email,
        password: data.password,
        user: newUser
      })

      return {
        success: true,
        message: '注册成功',
        user: newUser
      }
    },

    async guestLogin(): Promise<LoginResult> {
      if (USE_MOCK) {
        const guestUser: User = {
          id: 'guest_user',
          email: 'guest@example.com',
          nickname: '游客',
          createdAt: new Date().toISOString()
        }
        this.setGuestUser(guestUser)
        return {
          success: true,
          message: '游客登录成功',
          user: guestUser,
          token: 'guest_token_' + Date.now()
        }
      }
      
      try {
        const response = await authApi.guestLogin()
        this.user = response.user
        this.token = response.token
        this.isLoggedIn = true
        this.isGuest = true

        localStorage.setItem(STORAGE_KEY.USER, JSON.stringify(response.user))
        localStorage.setItem(STORAGE_KEY.TOKEN, response.token)
        localStorage.setItem(STORAGE_KEY.IS_GUEST, 'true')

        return {
          success: true,
          message: '游客登录成功',
          user: response.user,
          token: response.token
        }
      } catch (error: unknown) {
        const message = error instanceof Error ? error.message : '游客登录失败'
        return {
          success: false,
          message
        }
      }
    },

    setGuestUser(user: User) {
      const token = 'guest_token_' + Date.now()
      this.user = user
      this.token = token
      this.isLoggedIn = true
      this.isGuest = true

      localStorage.setItem(STORAGE_KEY.USER, JSON.stringify(user))
      localStorage.setItem(STORAGE_KEY.TOKEN, token)
      localStorage.setItem(STORAGE_KEY.IS_GUEST, 'true')
    },

    async logout() {
      if (!USE_MOCK) {
        try {
          await authApi.logout()
        } catch (e) {
          console.error('Logout API error:', e)
        }
      }
      
      this.user = null
      this.token = null
      this.isLoggedIn = false
      this.isGuest = false

      localStorage.removeItem(STORAGE_KEY.USER)
      localStorage.removeItem(STORAGE_KEY.TOKEN)
      localStorage.removeItem(STORAGE_KEY.IS_GUEST)
    },

    updateNickname(nickname: string) {
      if (this.user) {
        this.user.nickname = nickname
        localStorage.setItem(STORAGE_KEY.USER, JSON.stringify(this.user))
      }
    }
  }
})
