<template>
  <div class="empty-state">
    <div class="empty-state__halo" aria-hidden="true">
      <span class="empty-state__icon">{{ icon }}</span>
    </div>
    <p class="empty-state__title">{{ title }}</p>
    <p v-if="description" class="empty-state__desc">{{ description }}</p>
    <div v-if="$slots.default" class="empty-state__action">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 空状态占位。
 *
 * 替代当前各页面直接写一行灰字（如"暂无数据"）的做法：
 * 空状态应说明"为什么空"以及"下一步能做什么"。
 */
withDefaults(
  defineProps<{
    title: string
    description?: string
    icon?: string
  }>(),
  { icon: '📭' }
)
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  padding: $spacing-2xl $spacing-lg;
  text-align: center;

  &__halo {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 64px;
    height: 64px;
    margin-bottom: $spacing-xs;
    border-radius: 50%;
    background: radial-gradient(circle at 50% 40%, rgba(59, 130, 246, 0.12), rgba(59, 130, 246, 0.03));
  }

  &__icon {
    font-size: 26px;
    line-height: 1;
    opacity: 0.85;
  }

  &__title {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-soft;
  }

  &__desc {
    max-width: 380px;
    font-size: $font-size-sm;
    line-height: 1.6;
    color: $color-text-faint;
  }

  &__action {
    margin-top: $spacing-sm;
  }
}
</style>
