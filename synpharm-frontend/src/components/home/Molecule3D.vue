<template>
  <div ref="rootRef" class="mol3d" aria-label="3D 小分子结构预览">
    <div class="mol3d__viewport">
      <!-- 懒加载前的轻量占位 -->
      <div v-if="!visible" class="mol3d__placeholder">
        <span class="mol3d__placeholder-text">3D MOLECULE</span>
        <span class="mol3d__placeholder-sub">imatinib · STI</span>
      </div>

      <!-- 真实 Mol* 3D：小分子球棍模型，滚动接近时才挂载 -->
      <MolstarViewer
        v-else
        pdb-id="STI"
        format="sdf"
        background="#FFFFFF"
        height="100%"
        frameless
        initial-rep="ball-and-stick"
        initial-color="element-symbol"
        :auto-rotate="autoRotate"
        @structure-loaded="loaded = true"
      />

      <Transition name="mol3d-fade">
        <div v-show="visible && loaded" class="mol3d__hud">
          <span class="mol3d__hud-label">MOLECULAR DESIGN</span>
          <span class="mol3d__hud-sep" aria-hidden="true" />
          <span class="mol3d__hud-mono">Ligand · imatinib (STI)</span>
        </div>
      </Transition>

      <Transition name="mol3d-fade">
        <div v-show="visible && loaded" class="mol3d__legend">
          <span class="mol3d__legend-label">真实 3D 球棍结构 · 元素着色</span>
          <span class="mol3d__legend-hint">拖拽旋转 · 滚轮缩放</span>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import MolstarViewer from '@/components/protein/MolstarViewer.vue'

const rootRef = ref<HTMLElement | null>(null)
const visible = ref(false)
const loaded = ref(false)

let observer: IntersectionObserver | null = null

onMounted(() => {
  if (!rootRef.value) return

  // 不支持 IntersectionObserver 时直接挂载（兜底）
  if (!('IntersectionObserver' in window)) {
    visible.value = true
    return
  }

  observer = new IntersectionObserver(
    (entries) => {
      if (entries.some((e) => e.isIntersecting)) {
        visible.value = true
        observer?.disconnect()
        observer = null
      }
    },
    // 提前 240px 触发，滚动接近时就开始加载
    { rootMargin: '240px 0px', threshold: 0.01 }
  )

  observer.observe(rootRef.value)
})

onBeforeUnmount(() => {
  observer?.disconnect()
  observer = null
})

// 尊重 prefers-reduced-motion：减少动效的用户不自动旋转
const autoRotate = !window.matchMedia('(prefers-reduced-motion: reduce)').matches
</script>

<style lang="scss" scoped>
.mol3d {
  width: 100%;
  max-width: 560px;
  margin: 0 auto;
}

.mol3d__viewport {
  position: relative;
  height: 480px;
  border-radius: 20px;
  border: 1px solid $sp-home-3d-border;
  background: $sp-home-3d-bg;
  overflow: hidden;
  box-shadow: $sp-home-3d-shadow;
}

/* =========================================================
 * Placeholder（懒加载前）
 * ========================================================= */
.mol3d__placeholder {
  position: absolute;
  inset: 0;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.mol3d__placeholder-text {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 2px;
  color: $sp-home-primary;
}

.mol3d__placeholder-sub {
  font-size: 12px;
  color: $sp-home-text-muted;
  font-family: $sp-home-font-mono;
}

/* =========================================================
 * HUD（顶部）
 * ========================================================= */
.mol3d__hud {
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

.mol3d__hud-label {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 1.2px;
  color: $sp-home-primary;
}

.mol3d__hud-sep {
  width: 1px;
  height: 10px;
  background: rgba(12, 74, 110, 0.18);
}

.mol3d__hud-mono {
  font-size: 11px;
  color: $sp-home-text-soft;
  font-family: $sp-home-font-mono;
  letter-spacing: 0.4px;
}

/* =========================================================
 * Legend（底部）
 * ========================================================= */
.mol3d__legend {
  position: absolute;
  left: 16px;
  right: 16px;
  bottom: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  z-index: 5;
  pointer-events: none;
}

.mol3d__legend-label {
  font-size: 12px;
  color: $sp-home-text-soft;
}

.mol3d__legend-hint {
  font-size: 11px;
  color: $sp-home-text-muted;
  letter-spacing: 0.3px;
  white-space: nowrap;
}

/* =========================================================
 * Transition
 * ========================================================= */
.mol3d-fade-enter-active,
.mol3d-fade-leave-active {
  transition: opacity 0.5s ease;
}

.mol3d-fade-enter-from,
.mol3d-fade-leave-to {
  opacity: 0;
}

/* =========================================================
 * Responsive
 * ========================================================= */
@media (max-width: $bp-md) {
  .mol3d__viewport {
    height: 380px;
    border-radius: 16px;
  }

  .mol3d__legend-hint {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .mol3d-fade-enter-active,
  .mol3d-fade-leave-active {
    transition: none;
  }
}
</style>
