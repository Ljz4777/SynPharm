import axios, { type AxiosInstance, type AxiosRequestConfig, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const envBase = (import.meta.env.VITE_API_BASE_URL as string || '').replace(/\/+$/, '')
// 生产（Docker/nginx 同源反代）走相对路径；本地开发默认直连后端（跨域由 CORS 放开）
export const baseURL = envBase || (import.meta.env.PROD ? '' : 'http://localhost:8080')

const service: AxiosInstance = axios.create({
  baseURL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 在请求 config 上暂存发送时使用的 token，用于响应拦截器中判断
 * "收到 401 时，这个 401 对应的 token 是否已经被替换（即用户重新登录过）"
 * 如果请求时的 token !== 当前 store 的 token，说明这是一个"过期请求"的响应，
 * 不应该触发 logout，否则会把用户刚登录成功的新状态一并清掉。
 */
const REQ_TOKEN_KEY = '__synpharm_req_token__'

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    const currentToken = authStore.token
    if (currentToken) {
      config.headers.Authorization = `Bearer ${currentToken}`
    }
    // 记录本请求发出时的 token（供响应拦截器比对）
    ;(config as unknown as Record<string, unknown>)[REQ_TOKEN_KEY] = currentToken ?? null
    return config
  },
  (error: unknown) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 200) {
      // 401 或业务 Token 错误码（2001/2002/2003）都视为未登录/失效
      if (res.code === 401 || res.code === 2001 || res.code === 2002 || res.code === 2003) {
        handleAuthFailure(response.config)
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      handleAuthFailure(error.config)
    }
    return Promise.reject(toFriendlyError(error))
  }
)

/**
 * 把 axios 的原始错误转成"带后端原因"的 Error。
 *
 * 此前直接把 axios 错误原样抛出，调用方只能拿到
 * `Request failed with status code 400`，**后端精心写的中文原因 100% 丢失**
 * （问题总账 D-04）。例如 DDI 传了图外药物时，后端返回的是
 * "药物不在 DDI-LLM 训练图内（图内共 1323 个药物）…"，
 * 用户却只看到一句英文的 status code。
 *
 * 现在优先使用响应体里的 message（后端 `Result` 结构为 { code, errorCode, message, data }），
 * 并顺带把 errorCode 与 HTTP 状态码挂到 Error 上，供调用方做更精细的判断。
 */
export interface ApiError extends Error {
  /** HTTP 状态码（网络层失败时为 undefined） */
  status?: number
  /** 后端字符串错误码，如 DRUG_NOT_SUPPORTED / SEQUENCE_TOO_SHORT */
  errorCode?: string
  /** 原始 axios 错误，排查用 */
  raw?: unknown
}

function toFriendlyError(error: unknown): ApiError {
  if (!axios.isAxiosError(error)) {
    return error instanceof Error ? error : new Error(String(error))
  }

  const status = error.response?.status
  const data = error.response?.data

  let payload: { message?: unknown; errorCode?: unknown } | undefined
  if (data && typeof data === 'object' && !(data instanceof Blob)) {
    payload = data as { message?: unknown; errorCode?: unknown }
  }

  const backendMessage = typeof payload?.message === 'string' ? payload.message : ''
  // 无响应体（后端没起来 / 超时 / CORS）时给一句能看懂的话，而不是英文的 status code
  const fallback = error.response
    ? `请求失败（HTTP ${status ?? '未知'}）`
    : '网络异常，请检查服务是否已启动'

  const friendly = new Error(backendMessage || fallback) as ApiError
  friendly.status = status
  friendly.errorCode = typeof payload?.errorCode === 'string' ? payload.errorCode : undefined
  friendly.raw = error
  return friendly
}

/**
 * 统一处理认证失败：只有当"请求时的 token"与"当前 store 的 token"一致时，
 * 才执行 logout + 跳登录页；否则说明这个 401 来自旧请求，忽略即可。
 * 这是修复"首次登录成功却被踢回登录页（第二次才正常）"的关键。
 */
function handleAuthFailure(config: InternalAxiosRequestConfig | AxiosRequestConfig | undefined) {
  const authStore = useAuthStore()
  const reqToken = (config as unknown as Record<string, unknown> | undefined)?.[REQ_TOKEN_KEY] as string | null | undefined
  const currentToken = authStore.token ?? null

  // 关键比对：只有请求时的 token 与当前 token 一致时，才认为是"当前登录态真的失效了"
  if (reqToken !== currentToken) {
    return
  }

  // 如果当前已经是登录页，则不再重复跳转（避免无限循环 / 打断登录流程）
  if (router.currentRoute.value.path === '/login') {
    return
  }

  authStore.logout()
  router.push('/login')
}

export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export const request = {
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config)
  },
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config)
  },
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config)
  },
  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config)
  }
}

export default service
