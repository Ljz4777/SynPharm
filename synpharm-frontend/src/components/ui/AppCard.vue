<template>
  <section
    class="app-card"
    :class="[
      `app-card--pad-${padding}`,
      { 'app-card--hoverable': hoverable, 'app-card--flat': flat }
    ]"
  >
    <header v-if="title || $slots.header || $slots.actions" class="app-card__header">
      <slot name="header">
        <h3 class="app-card__title">{{ title }}</h3>
      </slot>
      <div v-if="$slots.actions" class="app-card__actions">
        <slot name="actions" />
      </div>
    </header>

    <div class="app-card__body">
      <slot />
    </div>
  </section>
</template>

<script setup lang="ts">
/**
 * 通用卡片容器。
 *
 * 替代各页面自行定义的 xxx__card（当前 pc__card / tg__card / dash__card 等
 * 各自实现了一遍白底 + 圆角 + 阴影），统一到一处后视觉才一致。
 */
withDefaults(
  defineProps<{
    title?: string
    /** 鼠标悬停时轻微上浮并加深阴影，用于可点击的卡片 */
    hoverable?: boolean
    /** 去掉阴影只保留描边，用于嵌套在卡片内部的次级容器 */
    flat?: boolean
    padding?: 'none' | 'sm' | 'md' | 'lg'
  }>(),
  { hoverable: false, flat: false, padding: 'md' }
)
</script>

<style lang="scss" scoped>
.app-card {
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  transition:
    box-shadow 0.2s $ease-out,
    transform 0.2s $ease-out,
    border-color 0.2s $ease-out;

  &--flat {
    box-shadow: none;
  }

  &--hoverable {
    cursor: pointer;

    &:hover {
      transform: translateY(-2px);
      border-color: rgba(59, 130, 246, 0.35);
      box-shadow: $shadow-card-hover;
    }
  }

  &--pad-none > .app-card__body { padding: 0; }
  &--pad-sm > .app-card__body { padding: $spacing-sm; }
  &--pad-md > .app-card__body { padding: $spacing-lg; }
  &--pad-lg > .app-card__body { padding: $spacing-xl; }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-md;
    padding: $spacing-md $spacing-lg;
    border-bottom: 1px solid $color-border-soft;
  }

  &__title {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    flex-shrink: 0;
  }
}
</style>
