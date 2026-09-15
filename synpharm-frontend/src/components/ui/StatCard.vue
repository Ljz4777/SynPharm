<template>
  <div class="stat-card" :class="`stat-card--${tone}`">
    <span v-if="icon" class="stat-card__icon" aria-hidden="true">{{ icon }}</span>

    <div class="stat-card__text">
      <span class="stat-card__value">{{ value }}</span>
      <span class="stat-card__label">{{ label }}</span>
    </div>

    <span v-if="hint" class="stat-card__hint">{{ hint }}</span>
  </div>
</template>

<script setup lang="ts">
/**
 * 指标卡。
 *
 * 数值使用等宽数字（tabular-nums，由全局样式统一），避免多个指标卡并排时
 * 数字宽度不同导致视觉抖动。
 */
withDefaults(
  defineProps<{
    value: string | number
    label: string
    icon?: string
    hint?: string
    tone?: 'brand' | 'success' | 'warning' | 'danger' | 'info' | 'neutral'
  }>(),
  { tone: 'brand' }
)
</script>

<style lang="scss" scoped>
$tones: (
  brand: $accent-color,
  success: $success-color,
  warning: $warning-color,
  danger: $error-color,
  info: $info-color,
  neutral: $text-muted
);

.stat-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-lg;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  transition:
    box-shadow 0.2s $ease-out,
    transform 0.2s $ease-out;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-card-hover;
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    width: 42px;
    height: 42px;
    border-radius: $border-radius-md;
    font-size: 20px;
    line-height: 1;
  }

  &__text {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }

  &__value {
    font-size: $font-size-2xl;
    font-weight: $font-weight-semibold;
    line-height: 1.15;
    color: $color-text;
  }

  &__label {
    font-size: $font-size-sm;
    color: $color-text-faint;
  }

  &__hint {
    margin-left: auto;
    font-size: $font-size-xs;
    color: $color-text-faint;
    white-space: nowrap;
  }

  @each $name, $color in $tones {
    &--#{$name} .stat-card__icon {
      color: $color;
      background: rgba($color, 0.12);
    }
  }
}
</style>
