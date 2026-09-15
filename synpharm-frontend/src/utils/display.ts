/**
 * 展示层语义工具
 *
 * 目的：把"置信度/状态 → 文案与色调"的映射收敛到一处，
 *       避免各页面各写一套（当前 high/medium/low 在多个视图里重复实现，颜色也不一致）。
 */

export type Tone = 'success' | 'warning' | 'danger' | 'info' | 'neutral' | 'brand'

export interface TagMeta {
  label: string
  tone: Tone
}

const CONFIDENCE_MAP: Record<string, TagMeta> = {
  high: { label: '高置信', tone: 'success' },
  medium: { label: '中置信', tone: 'warning' },
  low: { label: '低置信', tone: 'danger' }
}

/**
 * 置信度语义：高=绿 / 中=橙 / 低=红，全站统一。
 * 允许只给分数，由分数推导档位（阈值与算法侧 _confidence_level 保持一致）。
 */
export function confidenceMeta(level?: string, score?: number): TagMeta {
  const key = (level ?? '').toLowerCase()
  if (CONFIDENCE_MAP[key]) return CONFIDENCE_MAP[key]

  if (typeof score === 'number' && !Number.isNaN(score)) {
    if (score >= 0.8) return CONFIDENCE_MAP.high
    if (score >= 0.6) return CONFIDENCE_MAP.medium
    return CONFIDENCE_MAP.low
  }

  return { label: '—', tone: 'neutral' }
}

const STATUS_MAP: Record<string, TagMeta> = {
  completed: { label: '已完成', tone: 'success' },
  success: { label: '成功', tone: 'success' },
  running: { label: '运行中', tone: 'brand' },
  processing: { label: '处理中', tone: 'brand' },
  pending: { label: '待处理', tone: 'warning' },
  failed: { label: '失败', tone: 'danger' },
  error: { label: '错误', tone: 'danger' },
  cancelled: { label: '已取消', tone: 'neutral' },
  canceled: { label: '已取消', tone: 'neutral' }
}

export function statusMeta(status?: string): TagMeta {
  const key = (status ?? '').toLowerCase()
  return STATUS_MAP[key] ?? { label: status || '—', tone: 'neutral' }
}

/** 比例转百分比文案；非法值统一返回占位符，避免页面出现 NaN */
export function formatPercent(ratio?: number | null, digits = 0): string {
  if (typeof ratio !== 'number' || Number.isNaN(ratio)) return '—'
  return `${(ratio * 100).toFixed(digits)}%`
}

/** 数值千分位；非法值返回占位符 */
export function formatNumber(value?: number | null): string {
  if (typeof value !== 'number' || Number.isNaN(value)) return '—'
  return value.toLocaleString('zh-CN')
}

/** 时间戳 → 局部化短格式（列表用，避免整屏都是 ISO 串） */
export function formatDateTime(input?: string | number | null): string {
  if (!input) return '—'
  const date = new Date(input)
  if (Number.isNaN(date.getTime())) return String(input)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
