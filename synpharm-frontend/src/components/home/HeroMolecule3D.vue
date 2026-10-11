<template>
  <div class="hero-mol3d" aria-label="3D 分子结构可视化">
    <div class="hero-mol3d__viewport">
      <!-- 真实 Mol* 3D：干净浅色底，蛋白用青绿色（teal）单色 ribbon -->
      <MolstarViewer
        pdb-id="1MBO"
        background="#FFFFFF"
        height="100%"
        frameless
        initial-color="uniform"
        uniform-color="#0d9488"
        :auto-rotate="autoRotate"
        show-ligand
        @structure-loaded="loaded = true"
      />

      <Transition name="hero-mol3d-fade">
        <div v-show="loaded" class="hero-mol3d__hud">
          <span class="hero-mol3d__hud-label">STRUCTURAL VIEW</span>
          <span class="hero-mol3d__hud-sep" aria-hidden="true" />
          <span class="hero-mol3d__hud-mono">PDB · 1MBO</span>
        </div>
      </Transition>

      <Transition name="hero-mol3d-fade">
        <div v-show="loaded" class="hero-mol3d__legend">
          <div class="hero-mol3d__legend-item">
            <span
              class="hero-mol3d__legend-swatch hero-mol3d__legend-swatch--protein"
              aria-hidden="true"
            />
            <span class="hero-mol3d__legend-label">蛋白质结构</span>
          </div>
          <div class="hero-mol3d__legend-item">
            <span
              class="hero-mol3d__legend-swatch hero-mol3d__legend-swatch--ligand"
              aria-hidden="true"
            />
            <span class="hero-mol3d__legend-label">配体 · 结合位点</span>
          </div>
          <span class="hero-mol3d__legend-hint">拖拽旋转 · 滚轮缩放</span>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import MolstarViewer from '@/components/protein/MolstarViewer.vue'

const loaded = ref(false)

// 尊重 prefers-reduced-motion：减少动效的用户不做自动旋转（拖拽查看仍然可用）
const autoRotate = !window.matchMedia('(prefers-reduced-motion: reduce)').matches
</script>

<style lang="scss" scoped>
.hero-mol3d {
  width: 100%;
  max-width: 560px;
  margin: 0 auto;
}

.hero-mol3d__viewport {
  position: relative;
  height: 460px;
  border-radius: 20px;
  border: 1px solid $sp-home-3d-border;
  background: $sp-home-3d-bg;
  overflow: hidden;
  box-shadow: $sp-home-3d-shadow;
}

/* =========================================================
 * HUD（顶部）：轻量科研数据条
 * ========================================================= */
.hero-mol3d__hud {
  position: absolute;
  top: 14px;
  left: 16px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 11px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(12, 74, 110, 0.10);
  backdrop-filter: blur(4px);
  z-index: 5;
  pointer-events: none;
}

.hero-mol3d__hud-label {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 1.2px;
  color: $sp-home-primary;
}

.hero-mol3d__hud-sep {
  width: 1px;
  height: 10px;
  background: rgba(12, 74, 110, 0.18);
}

.hero-mol3d__hud-mono {
  font-size: 11px;
  color: $sp-home-text-soft;
  font-family: $sp-home-font-mono;
  letter-spacing: 0.4px;
}

/* =========================================================
 * Legend（底部）
 * ========================================================= */
.hero-mol3d__legend {
  position: absolute;
  left: 16px;
  right: 16px;
  bottom: 14px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px 18px;
  z-index: 5;
  pointer-events: none;
}

.hero-mol3d__legend-item {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.hero-mol3d__legend-swatch {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;

  &--protein {
    background: $sp-home-3d-protein;
  }

  &--ligand {
    background: $sp-home-3d-ligand;
  }
}

.hero-mol3d__legend-label {
  font-size: 12px;
  color: $sp-home-text-soft;
  white-space: nowrap;
}

.hero-mol3d__legend-hint {
  margin-left: auto;
  font-size: 11px;
  color: $sp-home-text-muted;
  letter-spacing: 0.3px;
  white-space: nowrap;
}

/* =========================================================
 * Transition
 * ========================================================= */
.hero-mol3d-fade-enter-active,
.hero-mol3d-fade-leave-active {
  transition: opacity 0.5s ease;
}

.hero-mol3d-fade-enter-from,
.hero-mol3d-fade-leave-to {
  opacity: 0;
}

/* =========================================================
 * Responsive
 * ========================================================= */
@media (max-width: $bp-md) {
  .hero-mol3d__viewport {
    height: 360px;
    border-radius: 16px;
  }

  .hero-mol3d__legend-hint {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .hero-mol3d-fade-enter-active,
  .hero-mol3d-fade-leave-active {
    transition: none;
  }
}
</style>
