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
  },

  /**
   * 收藏一条预测结果，返回收藏记录 ID。
   *
   * 对应后端 `POST /api/favorites`（`FavoriteController`）。
   * 此前前端只有查询、没有新增，导致"保存结果"按钮无从绑定（问题总账 G-01）。
   */
  addFavorite(resultId: number | string, note?: string): Promise<number | string> {
    return request.post<number | string>('/api/favorites', {
      resultId: Number(resultId),
      note
    })
  },

  /** 取消收藏 */
  removeFavorite(id: number | string): Promise<void> {
    return request.delete<void>(`/api/favorites/${id}`)
  }
}
