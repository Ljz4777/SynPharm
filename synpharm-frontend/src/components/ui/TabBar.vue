<template>
  <div class="tab-bar" role="tablist">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      class="tab-bar__item"
      :class="{ 'tab-bar__item--active': tab.key === modelValue }"
      type="button"
      role="tab"
      :aria-selected="tab.key === modelValue"
      @click="$emit('update:modelValue', tab.key)"
    >
      <span v-if="tab.icon" class="tab-bar__icon" aria-hidden="true">{{ tab.icon }}</span>
      <span class="tab-bar__label">{{ tab.label }}</span>
      <span v-if="typeof tab.count === 'number'" class="tab-bar__count">{{ tab.count }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
/**
 * 二级栏目切换（标签栏）。
 *
 * 用于同一页面内并列的若干视图切换，配合 `v-model` 使用。
 */
interface TabItem {
  key: string
  label: string
  icon?: string
  /** 可选计数徽标，例如列表条数 */
  count?: number
}

defineProps<{
  tabs: TabItem[]
  modelValue: string
}>()

defineEmits<{
  'update:modelValue': [key: string]
}>()
</script>

<style lang="scss" scoped>
.tab-bar {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  border-bottom: 1px solid $color-border;
  margin-bottom: $spacing-lg;
}

.tab-bar__item {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: $spacing-sm $spacing-md $spacing-md;
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: $font-size-base;
  font-family: inherit;
  color: $color-text-soft;
  transition: color 0.18s $ease-out;

  // 选中态下划线
  &::after {
    content: '';
    position: absolute;
    left: $spacing-sm;
    right: $spacing-sm;
    bottom: -1px;
    height: 2px;
    border-radius: $radius-pill;
    background: transparent;
    transition: background 0.18s $ease-out;
  }

  &:hover {
    color: $color-text;
  }

  &--active {
    color: $color-brand;
    font-weight: $font-weight-medium;

    &::after {
      background: $color-brand;
    }
  }
}

.tab-bar__icon {
  font-size: 14px;
  line-height: 1;
}

.tab-bar__count {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: $radius-pill;
  background: $color-surface-sunken;
  color: $color-text-faint;
  font-size: $font-size-xs;
  line-height: 18px;
  text-align: center;
}

.tab-bar__item--active .tab-bar__count {
  background: $color-brand-soft;
  color: $color-brand;
}
</style>
