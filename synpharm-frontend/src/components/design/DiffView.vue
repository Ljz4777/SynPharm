<template>
  <div class="dv">
    <div class="dv__tabs">
      <button
        v-for="item in SUB_TABS"
        :key="item.key"
        type="button"
        class="dv__tab"
        :class="{ 'dv__tab--active': active === item.key }"
        @click="active = item.key"
      >
        {{ item.icon }} {{ item.label }}
      </button>
      <span class="dv__spacer" />
      <span class="dv__note">{{ activeNote }}</span>
    </div>

    <div class="dv__body">
      <!--
        使用 v-if 而非 v-show：轮次对比内含 ECharts 图表，
        需要在容器有尺寸时初始化，v-show 会让它在 0 尺寸下建图。
      -->
      <MoleculeCompare
        v-if="active === 'molecule'"
        :render-structure="renderStructure"
        :initial-a="initialA"
      />
      <RoundCompare v-else />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 对比（Diff）页签。
 *
 * 两个层次：
 *   分子对比 —— 两个候选并排看结构式与逐项指标差值（回答"这次编辑改了什么"）
 *   轮次对比 —— 指标趋势与候选流向（回答"这一轮比上一轮好在哪里"）
 */
import { computed, ref } from 'vue'
import MoleculeCompare from '@/components/design/MoleculeCompare.vue'
import RoundCompare from '@/components/design/RoundCompare.vue'

const props = defineProps<{
  /** 由工作台注入：调用编辑器子应用渲染结构式 */
  renderStructure: (smiles: string) => Promise<string>
  /** 当前选中的候选，作为分子对比的默认 A */
  initialA?: number | null
}>()

type SubTab = 'molecule' | 'round'

const SUB_TABS: Array<{ key: SubTab; icon: string; label: string }> = [
  { key: 'molecule', icon: '🧬', label: '分子对比' },
  { key: 'round', icon: '📈', label: '轮次对比' }
]

const active = ref<SubTab>('molecule')

/** 未选中候选时传 undefined（而非 0），交由 MoleculeCompare 回退到候选列表首项 */
const initialA = computed(() => props.initialA || undefined)

const activeNote = computed(() => {
  if (active.value === 'molecule') {
    return '默认对比「当前选中候选」与其父代；结构差异高亮待后端 MCS 能力就绪'
  }
  return '趋势来自各轮指标快照；流向依据候选谱系（parentMolId）统计'
})
</script>

<style lang="scss" scoped>
.dv {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.dv__tabs {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: $spacing-sm $spacing-md;
  border-bottom: 1px solid $color-border;
  background: $color-surface;
}

.dv__tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px $spacing-sm;
  border: none;
  border-radius: $radius-control;
  background: transparent;
  font-size: $font-size-sm;
  color: $color-text-soft;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover {
    background: $color-surface-alt;
  }

  &--active {
    background: $color-brand-soft;
    color: $color-brand-strong;
    font-weight: $font-weight-medium;
  }
}

.dv__spacer {
  flex: 1;
}

.dv__note {
  font-size: 11px;
  color: $color-text-faint;
  text-align: right;
}

.dv__body {
  flex: 1;
  min-height: 0;
}
</style>
