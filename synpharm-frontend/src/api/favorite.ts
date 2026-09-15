import { request } from '@/utils/request'

/**
 * 收藏分页响应。
 *
 * 字段按实测的后端响应对齐：{ total, page, pageSize, list }。
 */
export interface FavoritePageResponse {
  total?: number
  page?: number
  pageSize?: number
  list?: unknown[]
}

export const favoriteApi = {
  /** 我的收藏（分页）。传 pageSize=1 即可只取 total 用于统计展示 */
  getFavoriteList(page = 1, pageSize = 10): Promise<FavoritePageResponse> {
    return request.get<FavoritePageResponse>('/api/favorites', { params: { page, pageSize } })
  }
}
