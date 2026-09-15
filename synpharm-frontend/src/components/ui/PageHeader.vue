<template>
  <header class="page-header">
    <div class="page-header__main">
      <h1 class="page-header__title">
        <span v-if="icon" class="page-header__icon" aria-hidden="true">{{ icon }}</span>
        <span>{{ title }}</span>
      </h1>
      <p v-if="subtitle" class="page-header__subtitle">{{ subtitle }}</p>
    </div>

    <div v-if="$slots.actions" class="page-header__actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<script setup lang="ts">
/**
 * 页面标题区。
 *
 * 统一各页面自行拼装的 header 结构（标题 + 副标题 + 右侧操作区），
 * 左侧用品牌色竖条做视觉锚点，避免每页样式各写一套。
 */
defineProps<{
  title: string
  subtitle?: string
  /** 可选 emoji 图标 */
  icon?: string
}>()
</script>

<style lang="scss" scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: $spacing-lg;
  padding: $spacing-md 0 $spacing-xl;

  &__main {
    position: relative;
    padding-left: $spacing-md;
    min-width: 0;
  }

  // 品牌色竖条：给页头一个稳定的视觉锚点
  &__main::before {
    content: '';
    position: absolute;
    left: 0;
    top: 5px;
    bottom: 5px;
    width: 3px;
    border-radius: $radius-pill;
    background: linear-gradient(180deg, $accent-color, $accent-light);
  }

  &__title {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    line-height: 1.3;
    color: $color-text;
  }

  &__icon {
    font-size: 20px;
    line-height: 1;
  }

  &__subtitle {
    margin-top: 4px;
    font-size: $font-size-sm;
    line-height: 1.5;
    color: $color-text-faint;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    flex-shrink: 0;
  }
}
</style>
