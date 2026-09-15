<template>
  <span class="status-tag" :class="`status-tag--${meta.tone}`">
    <i class="status-tag__dot" aria-hidden="true" />
    {{ meta.label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { confidenceMeta, statusMeta } from '@/utils/display'

/**
 * 状态 / 置信度标签。
 *
 * 关键作用是把"语义 → 颜色"收敛到一处：此前 high/medium/low 与
 * completed/pending/failed 的配色在多个视图里各写了一遍，同一档在不同页面颜色不同。
 */
const props = withDefaults(
  defineProps<{
    kind?: 'confidence' | 'status'
    /** 后端返回的 level 或 status 字段 */
    value?: string
    /** 置信度模式下可只给分数，由分数推导档位 */
    score?: number
  }>(),
  { kind: 'status' }
)

const meta = computed(() =>
  props.kind === 'confidence' ? confidenceMeta(props.value, props.score) : statusMeta(props.value)
)
</script>

<style lang="scss" scoped>
$tones: (
  success: $success-color,
  warning: $warning-color,
  danger: $error-color,
  info: $info-color,
  brand: $accent-color,
  neutral: $text-muted
);

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 22px;
  padding: 0 10px;
  border-radius: $radius-pill;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
  line-height: 1;
  white-space: nowrap;

  &__dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: currentColor;
    flex-shrink: 0;
  }

  @each $name, $color in $tones {
    &--#{$name} {
      color: $color;
      background: rgba($color, 0.11);
    }
  }
}
</style>
