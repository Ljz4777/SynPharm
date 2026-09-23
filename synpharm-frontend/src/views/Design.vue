<template>
  <div class="ide">
    <Sidebar />

    <div class="ide__body">
      <!-- ============ 左栏：项目 / 靶点 / 轮次 ============ -->
      <aside class="ide__left">
        <div class="ide__pane-title">项目</div>
        <div class="ide__project">
          <span class="ide__project-icon">🧬</span>
          <div class="ide__project-info">
            <span class="ide__project-name">{{ projectName }}</span>
            <span class="ide__project-meta">EGFR 选择性优化 · 草稿</span>
          </div>
        </div>

        <div class="ide__pane-title">设计流程</div>
        <nav class="ide__tree">
          <button
            v-for="node in treeNodes"
            :key="node.key"
            type="button"
            class="ide__tree-item"
            :class="{ 'ide__tree-item--active': activeNode === node.key }"
            @click="activeNode = node.key"
          >
            <span class="ide__tree-icon">{{ node.icon }}</span>
            <span class="ide__tree-label">{{ node.label }}</span>
            <span v-if="node.badge" class="ide__tree-badge">{{ node.badge }}</span>
          </button>
        </nav>

        <div class="ide__pane-title">轮次</div>
        <div class="ide__rounds">
          <div v-for="round in rounds" :key="round.id" class="ide__round">
            <span class="ide__round-dot" :class="`ide__round-dot--${round.state}`" />
            <span class="ide__round-name">{{ round.name }}</span>
            <span class="ide__round-count">{{ round.candidates }}</span>
          </div>
        </div>

        <p class="ide__hint">
          左栏数据待接入 <code>/api/design/projects/{id}/tree</code>
        </p>
      </aside>

      <!-- ============ 中栏：主工作面 ============ -->
      <main class="ide__main">
        <div class="ide__tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            class="ide__tab"
            :class="{ 'ide__tab--active': activeTab === tab.key }"
            @click="activeTab = tab.key"
          >
            <span class="ide__tab-icon">{{ tab.icon }}</span>
            <span>{{ tab.label }}</span>
          </button>

          <div class="ide__tabs-spacer" />
          <span class="ide__tabs-note">界面骨架 · 画布与数据待接入</span>
        </div>

        <div class="ide__stage">
          <div class="ide__placeholder">
            <span class="ide__placeholder-icon">{{ activeTabMeta.icon }}</span>
            <span class="ide__placeholder-title">{{ activeTabMeta.label }}</span>
            <span class="ide__placeholder-desc">{{ activeTabMeta.desc }}</span>
          </div>
        </div>

        <div class="ide__statusbar">
          <span class="ide__status-item">
            <span class="ide__status-dot ide__status-dot--idle" />
            界面骨架已就绪 · 计算与数据待接入
          </span>
          <span class="ide__status-spacer" />
          <span class="ide__status-item ide__status-item--muted">2D 编辑器选型待定</span>
        </div>
      </main>

      <!-- ============ 右栏：检查器 ============ -->
      <aside class="ide__right">
        <div class="ide__pane-title">设计约束</div>
        <div class="ide__check-list">
          <div v-for="rule in constraintRules" :key="rule.code" class="ide__check">
            <span class="ide__check-dot" :class="`ide__check-dot--${rule.level}`" />
            <span class="ide__check-label">{{ rule.label }}</span>
            <span class="ide__check-value">{{ rule.value }}</span>
          </div>
        </div>

        <div class="ide__pane-title">评估指标</div>
        <div class="ide__check-list">
          <div v-for="metric in metrics" :key="metric.code" class="ide__check">
            <span class="ide__check-label">{{ metric.label }}</span>
            <span class="ide__check-value">{{ metric.value }}</span>
          </div>
        </div>

        <p class="ide__hint">
          右栏数据待接入 <code>/v1/constraints/validate</code> 与 <code>/v1/evaluate/describe</code>
        </p>
      </aside>
    </div>

    <!-- ============ 底部：时间线 ============ -->
    <footer class="ide__timeline">
      <span class="ide__timeline-title">时间线</span>
      <div class="ide__timeline-track">
        <span
          v-for="(round, index) in rounds"
          :key="round.id"
          class="ide__timeline-node"
          :class="`ide__timeline-node--${round.state}`"
        >
          R{{ index + 1 }}
        </span>
      </div>
      <span class="ide__timeline-spacer" />
      <span class="ide__timeline-note">谱系树 / 操作日志 待实现</span>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import Sidebar from '@/components/Sidebar.vue'

/* ------------------------------------------------------------------
 * 说明：本页为干实验设计工作台（IDE）的骨架。
 * 当前仅实现「外壳」：路由、导航入口、三栏布局、底部时间线与页签切换。
 * 中栏画布与各栏数据接入见后续步骤；页面内展示数据均为占位。
 * ------------------------------------------------------------------ */

const projectName = ref('未命名设计项目')

const activeNode = ref('target')
const activeTab = ref<'canvas' | 'candidates' | 'diff' | 'pocket'>('canvas')

const treeNodes = [
  { key: 'target', icon: '🎯', label: '靶点与口袋', badge: '' },
  { key: 'constraint', icon: '📐', label: '设计约束', badge: '' },
  { key: 'generate', icon: '⚗️', label: '分子生成', badge: '' },
  { key: 'edit', icon: '✏️', label: '分子编辑', badge: '' },
  { key: 'evaluate', icon: '📊', label: '评估结果', badge: '' }
]

const tabs = [
  { key: 'canvas' as const, icon: '✏️', label: '画布' },
  { key: 'candidates' as const, icon: '🧫', label: '候选' },
  { key: 'diff' as const, icon: '🔀', label: '对比' },
  { key: 'pocket' as const, icon: '🧊', label: '口袋' }
]

const activeTabMeta = computed(() => {
  const found = tabs.find((item) => item.key === activeTab.value)
  return {
    icon: found?.icon ?? '',
    label: found?.label ?? '',
    desc: `${found?.label ?? ''}视图将在后续步骤实现（见 docs/modules/design/07-工作台与交互设计.md）`
  }
})

const rounds = [
  { id: 1, name: 'R1 骨架枚举', candidates: 0, state: 'active' },
  { id: 2, name: 'R2 R 基优化', candidates: 0, state: 'planned' }
]

const constraintRules = [
  { code: 'MOL_WT', label: '分子量', value: '≤ 500', level: 'hard' },
  { code: 'MOL_LOGP', label: 'LogP', value: '1 ~ 5', level: 'hard' },
  { code: 'QED', label: '类药性', value: '≥ 0.5', level: 'soft' },
  { code: 'PAINS', label: 'PAINS 命中', value: '0', level: 'hard' }
]

const metrics = [
  { code: 'MOL_WT', label: '分子量', value: '—' },
  { code: 'MOL_LOGP', label: 'LogP', value: '—' },
  { code: 'QED', label: 'QED', value: '—' },
  { code: 'SA_SCORE', label: '合成可及性', value: '—' }
]
</script>

<style lang="scss" scoped>
/* 说明：本页顶栏（Sidebar）为 fixed 定位，外层需留出等高上边距 */
.ide {
  height: 100vh;
  padding-top: $header-height;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: $color-canvas;
}

.ide__body {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 232px minmax(0, 1fr) 268px;
}

/* ---------- 通用面板 ---------- */
.ide__left,
.ide__right {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-md;
  overflow-y: auto;
  background: $color-surface;
}

.ide__left {
  border-right: 1px solid $color-border;
}

.ide__right {
  border-left: 1px solid $color-border;
}

.ide__pane-title {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: $color-text-faint;
  margin-top: $spacing-xs;
}

.ide__hint {
  margin-top: auto;
  padding-top: $spacing-md;
  font-size: 11px;
  line-height: 1.6;
  color: $color-text-faint;

  code {
    padding: 1px 4px;
    border-radius: 4px;
    background: $color-surface-sunken;
    font-size: 10px;
  }
}

/* ---------- 左栏 ---------- */
.ide__project {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm;
  border-radius: $radius-control;
  background: $color-surface-alt;
}

.ide__project-icon {
  font-size: 18px;
}

.ide__project-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.ide__project-name {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ide__project-meta {
  font-size: 11px;
  color: $color-text-faint;
}

.ide__tree {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ide__tree-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: 6px $spacing-sm;
  border: none;
  border-radius: $radius-control;
  background: transparent;
  font-size: $font-size-sm;
  color: $color-text-soft;
  text-align: left;
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

.ide__tree-icon {
  font-size: 13px;
}

.ide__tree-label {
  flex: 1;
  min-width: 0;
}

.ide__tree-badge {
  font-size: 11px;
  color: $color-text-faint;
}

.ide__rounds {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ide__round {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: 4px $spacing-sm;
  font-size: $font-size-sm;
  color: $color-text-soft;
}

.ide__round-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: $text-light;

  &--active {
    background: $color-brand;
  }

  &--planned {
    background: $color-border;
  }
}

.ide__round-name {
  flex: 1;
  min-width: 0;
}

.ide__round-count {
  font-size: 11px;
  color: $color-text-faint;
}

/* ---------- 中栏 ---------- */
.ide__main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
}

.ide__tabs {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: $spacing-sm $spacing-md;
  border-bottom: 1px solid $color-border;
  background: $color-surface;
}

.ide__tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px $spacing-sm;
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

.ide__tab-icon {
  font-size: 12px;
}

.ide__tabs-spacer {
  flex: 1;
}

.ide__tabs-note {
  font-size: 11px;
  color: $color-text-faint;
}

.ide__stage {
  position: relative;
  flex: 1;
  min-height: 0;
  background: $color-canvas;
}

.ide__placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
}

.ide__placeholder-icon {
  font-size: 32px;
  opacity: 0.5;
}

.ide__placeholder-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-medium;
  color: $color-text-soft;
}

.ide__placeholder-desc {
  max-width: 460px;
  font-size: $font-size-sm;
  color: $color-text-faint;
  text-align: center;
  line-height: 1.7;
}

.ide__statusbar {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: 5px $spacing-md;
  border-top: 1px solid $color-border;
  background: $color-surface;
  font-size: 11px;
  color: $color-text-soft;
}

.ide__status-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  &--muted {
    color: $color-text-faint;
  }
}

.ide__status-spacer {
  flex: 1;
}

.ide__status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;

  &--ok {
    background: $color-brand;
  }

  &--idle {
    background: $text-light;
  }
}

/* ---------- 右栏 ---------- */
.ide__check-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ide__check {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: 4px $spacing-sm;
  font-size: $font-size-sm;
  color: $color-text-soft;
}

.ide__check-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: $text-light;

  &--hard {
    background: $error-color;
  }

  &--soft {
    background: $warning-color;
  }
}

.ide__check-label {
  flex: 1;
  min-width: 0;
}

.ide__check-value {
  font-size: 11px;
  color: $color-text-faint;
}

/* ---------- 底部时间线 ---------- */
.ide__timeline {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  height: 40px;
  padding: 0 $spacing-md;
  border-top: 1px solid $color-border;
  background: $color-surface;
}

.ide__timeline-title {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: $color-text-faint;
}

.ide__timeline-track {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.ide__timeline-node {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  font-size: 11px;
  font-weight: $font-weight-medium;
  background: $color-surface-sunken;
  color: $color-text-faint;

  &--active {
    background: $color-brand;
    color: #fff;
  }
}

.ide__timeline-spacer {
  flex: 1;
}

.ide__timeline-note {
  font-size: 11px;
  color: $color-text-faint;
}
</style>
