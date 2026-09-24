<template>
  <div class="pv">
    <!-- ==================== 工具条 ==================== -->
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
        @click="molstarRef?.resetView()"
      >
        重置视角
      </button>
      <button
        type="button"
        class="pv__action"
        :disabled="exporting"
        @click="handleExport"
      >
        {{ exporting ? '导出中…' : '导出图片' }}
      </button>

      <span class="pv__spacer" />
      <span class="pv__hint">{{ statusText }}</span>
    </div>

    <!-- ==================== 主体 ==================== -->
    <div class="pv__body">
      <!-- 3D 视图 -->
      <div class="pv__canvas">
        <MolstarViewer
          v-if="pocket"
          ref="molstarRef"
          :pdb-id="pocket.structureRef"
          :auto-rotate="autoRotate"
          :show-grid="showGrid"
          :pocket="pocketGeometry"
          :show-ligand="showLigand"
          @structure-loaded="onStructureLoaded"
        />
        <div v-else class="pv__empty">当前项目未检测到结合口袋</div>

        <!-- 口袋摘要浮层：不遮挡操作，仅提示当前看的口袋 -->
        <div v-if="pocket" class="pv__badge">
          <span class="pv__badge-row">
            <b>{{ pocket.structureRef }}</b> · Pocket {{ pocket.pocketNo }}
          </span>
          <span class="pv__badge-row pv__badge-row--dim">
            中心 {{ pocket.center.x.toFixed(1) }}, {{ pocket.center.y.toFixed(1) }},
            {{ pocket.center.z.toFixed(1) }} · 半径 {{ pocket.radius.toFixed(1) }} Å
          </span>
          <span class="pv__badge-row pv__badge-row--dim">
            成药性 {{ (pocket.druggability * 100).toFixed(0) }}% · 残基 {{ pocket.residues.length }} 个
          </span>
        </div>
      </div>

      <!-- ==================== 显示控制 + 口袋信息 ==================== -->
      <aside v-if="pocket" class="pv__side">
        <!-- 显示模式 -->
        <div class="pv__section">
          <div class="pv__section-title">显示模式</div>
          <div class="pv__modes">
            <button
              v-for="mode in VIEWER_DISPLAY_MODES"
              :key="mode.value"
              type="button"
              class="pv__mode"
              :class="{ 'pv__mode--active': displayMode === mode.value }"
              @click="changeDisplayMode(mode.value)"
            >
              {{ mode.label }}
            </button>
          </div>
        </div>

        <!-- 颜色方案 -->
        <div class="pv__section">
          <div class="pv__section-title">颜色方案</div>
          <div class="pv__colors">
            <button
              v-for="color in VIEWER_COLOR_SCHEMES"
              :key="color.value"
              type="button"
              class="pv__color"
              :class="{ 'pv__color--active': colorScheme === color.value }"
              :style="{ background: color.preview }"
              :title="color.label"
              @click="changeColorScheme(color.value)"
            />
          </div>
        </div>

        <!-- 设置 -->
        <div class="pv__section">
          <div class="pv__section-title">设置</div>
          <label v-for="item in SETTINGS" :key="item.key" class="pv__setting">
            <span class="pv__setting-label">{{ item.label }}</span>
            <input
              type="checkbox"
              class="pv__setting-checkbox"
              :checked="flagOf(item.key)"
              @change="toggleSetting(item.key, ($event.target as HTMLInputElement).checked)"
            />
          </label>
        </div>

        <!-- 口袋信息 -->
        <div class="pv__section">
          <div class="pv__section-title">口袋信息</div>
          <div class="pv__kv">
            <span>靶点</span>
            <b>{{ targetLabelOf(pocket.targetId) }}</b>
            <span>结构</span>
            <b class="pv__mono">{{ pocket.structureRef }}</b>
            <span>中心</span>
            <b class="pv__mono">
              {{ pocket.center.x.toFixed(1) }}, {{ pocket.center.y.toFixed(1) }},
              {{ pocket.center.z.toFixed(1) }}
            </b>
            <span>半径</span>
            <b class="pv__mono">{{ pocket.radius.toFixed(1) }} Å</b>
          </div>
        </div>

        <!-- 成药性 -->
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

        <!-- 口袋残基 -->
        <div class="pv__section">
          <div class="pv__section-title">口袋残基（{{ pocket.residues.length }}）</div>
          <div class="pv__residues">
            <span v-for="residue in pocket.residues" :key="residue" class="pv__residue">
              {{ residue }}
            </span>
          </div>
        </div>

        <p class="pv__note">
          结构来自 RCSB PDB；橙色线框球为检测到的结合口袋空腔（中心 / 半径）。
          配体只能显示晶体结构中<strong>已有</strong>的共结晶配体；候选分子的对接构象需要对接引擎（当前未部署）。
        </p>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 口袋视图（工作台「口袋」页签）。
 *
 * 3D 能力对齐项目既有的 3D 可视化页：显示模式（卡通/球体/棍状/表面）、
 * 颜色方案（链/元素/二级结构/单色）、网格、自动旋转、标签、重置视角、导出图片。
 *
 * 实现要点：
 *   - 3D 显示控制接口（显示模式 / 颜色方案 / Mol* 映射 / 组件句柄）来自共享模块
 *     `components/protein/viewerControls.ts`，与预测域的 3D 可视化页是**同一份**，
 *     不再两边各写一份
 *   - 复用既有 `MolstarViewer`，只以**可选 prop** 扩展出口袋腔体与共结晶配体，
 *     不传即不生效 —— 因此既有 3D 可视化页零影响
 *   - 用 defineAsyncComponent 按需加载，避免 molstar（约 2 MB）进入 design 分块
 *   - 结构切换后 Mol* 会重建场景，需要把当前 UI 状态重新同步一次
 */
import { computed, defineAsyncComponent, nextTick, ref } from 'vue'
import { useDesignStore } from '@/stores/design'
import type { ProjectTargetRole } from '@/types/design'
import {
  VIEWER_COLOR_SCHEMES,
  VIEWER_DISPLAY_MODES,
  useMolstarControls,
  type MolstarHandle
} from '@/components/protein/viewerControls'

const store = useDesignStore()

/** 只在此页签需要时才加载 molstar */
const MolstarViewer = defineAsyncComponent(() => import('@/components/protein/MolstarViewer.vue'))

const molstarRef = ref<MolstarHandle | null>(null)

/* ------------------------------ 显示选项 ------------------------------ */

/**
 * 显示模式 / 颜色方案 / Mol* 映射 / 组件句柄全部来自共享模块 `viewerControls.ts`，
 * 与预测域的 3D 可视化页用的是**同一份接口**（此前两边各写一份，改一处必漏另一处）。
 */
const {
  displayMode,
  colorScheme,
  changeDisplayMode,
  changeColorScheme,
  resyncAfterLoad
} = useMolstarControls(molstarRef)

type SettingKey = 'showGrid' | 'autoRotate' | 'showLabels' | 'showPocket' | 'showLigand'

const SETTINGS: Array<{ key: SettingKey; label: string }> = [
  { key: 'showPocket', label: '口袋腔体' },
  { key: 'showLigand', label: '共结晶配体' },
  { key: 'showGrid', label: '显示网格' },
  { key: 'autoRotate', label: '自动旋转' },
  { key: 'showLabels', label: '显示标签' }
]

const showGrid = ref(false)
const autoRotate = ref(false)
const showLabels = ref(false)
/* 口袋腔体与配体默认开启：进页签就能看到该口袋的空间范围与已知配体 */
const showPocket = ref(true)
const showLigand = ref(true)
const exporting = ref(false)

const flagOf = (key: SettingKey): boolean => {
  if (key === 'showGrid') return showGrid.value
  if (key === 'autoRotate') return autoRotate.value
  if (key === 'showLabels') return showLabels.value
  if (key === 'showPocket') return showPocket.value
  return showLigand.value
}

/**
 * 交给 MolstarViewer 的口袋几何；为 null 即不画腔体线框球。
 *
 * 用 props 而非命令式方法，是为了让「切换口袋 → 重建腔体」完全由响应式驱动，
 * 避免在多处手动调用重建逻辑。
 */
const pocketGeometry = computed(() =>
  showPocket.value && pocket.value
    ? { center: pocket.value.center, radius: pocket.value.radius }
    : null
)

/* ------------------------------ 口袋数据 ------------------------------ */

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

/* ------------------------------ 交互 ------------------------------ */

/* 显示模式与颜色方案的切换由 useMolstarControls 提供，此处不再各写一份 */

function toggleSetting(key: SettingKey, value: boolean): void {
  if (key === 'showGrid') {
    showGrid.value = value
    nextTick(() => molstarRef.value?.setGridVisible(value))
    return
  }
  if (key === 'autoRotate') {
    autoRotate.value = value
    return
  }
  if (key === 'showLabels') {
    showLabels.value = value
    nextTick(() => molstarRef.value?.setLabelsVisible(value))
    return
  }
  // 口袋腔体与配体走 props，MolstarViewer 内部的 watch 会负责重建
  if (key === 'showPocket') {
    showPocket.value = value
    return
  }
  showLigand.value = value
}

async function handleExport(): Promise<void> {
  if (!molstarRef.value) return
  exporting.value = true
  try {
    await molstarRef.value.exportImage()
  } finally {
    exporting.value = false
  }
}

/**
 * 结构加载完成后，Mol* 会重建场景并回到默认显示，
 * 因此需要把当前 UI 状态重新同步一次（与既有 3D 可视化页的做法一致）。
 */
function onStructureLoaded(): void {
  structureLoaded.value = true

  // 模式与配色的重新同步在共享逻辑里；网格与标签是本页独有的补充项
  resyncAfterLoad(() => {
    if (showGrid.value) molstarRef.value?.setGridVisible(true)
    if (showLabels.value) molstarRef.value?.setLabelsVisible(true)
  })
}

// 默认选中主靶点的口袋；切换口袋时以 :key 重建组件，状态随之复位
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
  min-width: 250px;
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
  grid-template-columns: minmax(0, 1fr) 236px;
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

/* 口袋摘要浮层 */
.pv__badge {
  position: absolute;
  left: $spacing-md;
  bottom: $spacing-md;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 6px $spacing-sm;
  border-radius: $radius-control;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: $shadow-sm;
  pointer-events: none;
}

.pv__badge-row {
  font-size: 11px;
  color: $color-text;

  &--dim {
    color: $color-text-faint;
    font-variant-numeric: tabular-nums;
  }
}

/* ---------------- 侧栏 ---------------- */
.pv__side {
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

/* 显示模式 */
.pv__modes {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px;
}

.pv__mode {
  padding: 5px 0;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  font-size: $font-size-xs;
  color: $color-text-soft;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover:not(:disabled) {
    border-color: $color-brand;
    color: $color-brand-strong;
  }

  &--active {
    border-color: $color-brand;
    background: $color-brand-soft;
    color: $color-brand-strong;
    font-weight: $font-weight-medium;
  }

  &:disabled {
    opacity: 0.45;
    cursor: not-allowed;
  }
}

/* 颜色方案 */
.pv__colors {
  display: flex;
  gap: 6px;
}

.pv__color {
  width: 30px;
  height: 30px;
  border: 2px solid transparent;
  border-radius: $radius-control;
  cursor: pointer;
  transition: border-color $transition-fast;

  &:hover:not(:disabled) {
    border-color: $color-border;
  }

  &--active {
    border-color: $color-brand;
  }

  &:disabled {
    opacity: 0.45;
    cursor: not-allowed;
  }
}

/* 设置 */
.pv__setting {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 3px 0;
  cursor: pointer;
}

.pv__setting-label {
  font-size: $font-size-sm;
  color: $color-text-soft;
}

.pv__setting-checkbox {
  width: 14px;
  height: 14px;
  cursor: pointer;
  accent-color: $color-brand;
}

/* 口袋信息 */
.pv__kv {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 3px $spacing-sm;
  font-size: 12px;

  span {
    color: $color-text-faint;
  }

  b {
    font-weight: $font-weight-medium;
    color: $color-text-soft;
    text-align: right;
    word-break: break-all;
  }
}

.pv__mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 11px;
}

/* 成药性 */
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

/* 口袋残基 */
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
