<template>
  <div class="pv">
    <!-- 工具条 -->
    <div class="pv__toolbar">
      <span class="pv__label">结合口袋</span>
      <select v-model.number="pocketId" class="pv__select">
        <option v-for="p in pockets" :key="p.id" :value="p.id">
          {{ targetLabelOf(p.targetId) }} · 口袋 {{ p.pocketNo }} · {{ p.structureRef }}
        </option>
      </select>

      <button
        type="button"
        class="pv__action"
        :disabled="!structureLoaded"
        @click="viewerRef?.resetView()"
      >
        重置视角
      </button>

      <span class="pv__spacer" />
      <span class="pv__hint">{{ statusText }}</span>
    </div>

    <!-- 主体 -->
    <div class="pv__body">
      <div class="pv__canvas">
        <component
          :is="MolstarViewer"
          v-if="pocket"
          :key="pocket.id"
          ref="viewerRef"
          :pdb-id="pocket.structureRef"
          @structure-loaded="onLoaded"
        />
        <div v-else class="pv__empty">当前项目未检测到结合口袋</div>
      </div>

      <!-- 口袋信息 -->
      <aside v-if="pocket" class="pv__info">
        <div class="pv__section">
          <div class="pv__section-title">基本信息</div>
          <div class="pv__kv">
            <span>靶点</span>
            <b>{{ targetLabelOf(pocket.targetId) }}</b>
            <span>结构</span>
            <b class="pv__mono">{{ pocket.structureRef }}</b>
            <span>口袋编号</span>
            <b>Pocket {{ pocket.pocketNo }}</b>
          </div>
        </div>

        <div class="pv__section">
          <div class="pv__section-title">几何参数</div>
          <div class="pv__kv">
            <span>中心 X</span>
            <b class="pv__mono">{{ pocket.center.x.toFixed(2) }} Å</b>
            <span>中心 Y</span>
            <b class="pv__mono">{{ pocket.center.y.toFixed(2) }} Å</b>
            <span>中心 Z</span>
            <b class="pv__mono">{{ pocket.center.z.toFixed(2) }} Å</b>
            <span>半径</span>
            <b class="pv__mono">{{ pocket.radius.toFixed(1) }} Å</b>
          </div>
        </div>

        <div class="pv__section">
          <div class="pv__section-title">成药性</div>
          <div class="pv__gauge">
            <div class="pv__gauge-track">
              <div
                class="pv__gauge-fill"
                :class="druggabilityTone"
                :style="{ width: `${pocket.druggability * 100}%` }"
              />
            </div>
            <span class="pv__gauge-value">{{ (pocket.druggability * 100).toFixed(0) }}%</span>
          </div>
        </div>

        <div class="pv__section">
          <div class="pv__section-title">口袋残基（{{ pocket.residues.length }}）</div>
          <div class="pv__residues">
            <span v-for="residue in pocket.residues" :key="residue" class="pv__residue">
              {{ residue }}
            </span>
          </div>
        </div>

        <p class="pv__note">
          结构与残基数据来自 RCSB PDB；候选分子的对接构象需要对接引擎（当前未部署），
          因此暂不显示配体姿态。
        </p>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 口袋视图（工作台「口袋」页签）。
 *
 * 刻意复用既有的 `MolstarViewer` 而不改动它：该组件已在 3D 可视化页稳定使用，
 * 修改它有回归风险。这里只做「口袋上下文 + 视图控制」的外层封装。
 * 为避免 molstar（约 2 MB）进入 design 分块，按需异步加载。
 *
 * 待后续增强（需要后端能力）：
 *   - 口袋球体/表面与残基的 3D 高亮（Mol* 选择表达式）
 *   - 候选分子在口袋中的对接构象（依赖对接引擎）
 */
import { computed, defineAsyncComponent, ref } from 'vue'
import { useDesignStore } from '@/stores/design'
import type { ProjectTargetRole } from '@/types/design'

const store = useDesignStore()

/** 只在此页签需要时才加载 molstar */
const MolstarViewer = defineAsyncComponent(() => import('@/components/protein/MolstarViewer.vue'))

/** MolstarViewer 对外暴露的方法中本页用到的子集 */
const viewerRef = ref<{ resetView: () => void } | null>(null)

const pockets = computed(() => store.pockets)
const pocketId = ref(0)
const structureLoaded = ref(false)

const pocket = computed(() => {
  const list = pockets.value
  if (!list.length) return null
  return list.find((p) => p.id === pocketId.value) ?? list[0] ?? null
})

const ROLE_LABEL: Record<ProjectTargetRole, string> = {
  WILD_TYPE: '野生型',
  MUTANT: '突变体',
  ANTI_TARGET: '反靶点',
  OFF_TARGET: '脱靶'
}

/** 口袋归属哪个靶点（靶点名称 + 角色，便于区分野生型/突变体） */
function targetLabelOf(targetId: number): string {
  const target = store.targets.find((t) => t.id === targetId)
  if (!target) return `靶点 ${targetId}`
  return `${target.chineseName || target.name}（${ROLE_LABEL[target.role]}）`
}

const statusText = computed(() => {
  if (!pocket.value) return '无口袋数据'
  return structureLoaded.value ? '结构已加载' : '正在加载结构…'
})

const druggabilityTone = computed(() => {
  const value = pocket.value?.druggability ?? 0
  if (value >= 0.8) return 'pv__gauge-fill--good'
  if (value >= 0.6) return 'pv__gauge-fill--ok'
  return 'pv__gauge-fill--poor'
})

function onLoaded(): void {
  structureLoaded.value = true
}

// 默认选中主靶点的口袋
const primaryTargetId = computed(() => store.targets.find((t) => t.isPrimary)?.id)
pocketId.value =
  pockets.value.find((p) => p.targetId === primaryTargetId.value)?.id ?? pockets.value[0]?.id ?? 0
</script>

<style lang="scss" scoped>
.pv {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: $color-surface;
}

/* ---------------- 工具条 ---------------- */
.pv__toolbar {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  border-bottom: 1px solid $color-border;
}

.pv__label {
  font-size: $font-size-xs;
  color: $color-text-faint;
}

.pv__select {
  min-width: 260px;
  padding: 4px $spacing-sm;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  font-size: $font-size-sm;
  color: $color-text;
}

.pv__action {
  padding: 5px $spacing-sm;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  font-size: $font-size-xs;
  color: $color-text-soft;
  cursor: pointer;

  &:hover:not(:disabled) {
    border-color: $color-brand;
    color: $color-brand-strong;
  }

  &:disabled {
    opacity: 0.45;
    cursor: not-allowed;
  }
}

.pv__spacer {
  flex: 1;
}

.pv__hint {
  font-size: 11px;
  color: $color-text-faint;
}

/* ---------------- 主体 ---------------- */
.pv__body {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
}

.pv__canvas {
  position: relative;
  min-width: 0;
  background: $color-canvas;
}

.pv__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: $font-size-sm;
  color: $color-text-faint;
}

/* ---------------- 信息栏 ---------------- */
.pv__info {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  padding: $spacing-md;
  border-left: 1px solid $color-border;
  overflow-y: auto;
}

.pv__section-title {
  margin-bottom: 6px;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: $color-text-faint;
}

.pv__kv {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 3px $spacing-sm;
  font-size: $font-size-sm;

  span {
    color: $color-text-faint;
  }

  b {
    font-weight: $font-weight-medium;
    color: $color-text-soft;
    text-align: right;
  }
}

.pv__mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 12px;
}

.pv__gauge {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.pv__gauge-track {
  flex: 1;
  height: 6px;
  border-radius: $radius-pill;
  background: $color-surface-sunken;
  overflow: hidden;
}

.pv__gauge-fill {
  height: 100%;
  border-radius: $radius-pill;

  &--good {
    background: $success-color;
  }

  &--ok {
    background: $accent-color;
  }

  &--poor {
    background: $warning-color;
  }
}

.pv__gauge-value {
  font-size: $font-size-xs;
  font-variant-numeric: tabular-nums;
  color: $color-text-soft;
}

.pv__residues {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.pv__residue {
  padding: 1px 6px;
  border-radius: 3px;
  background: $color-surface-alt;
  font-size: 11px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  color: $color-text-soft;
}

.pv__note {
  margin-top: auto;
  padding-top: $spacing-sm;
  font-size: 11px;
  line-height: 1.7;
  color: $color-text-faint;
}
</style>
