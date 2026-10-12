<template>
  <header class="topbar">
    <div class="topbar__brand" @click="router.push('/dashboard')">
      <svg class="topbar__logo" viewBox="0 0 32 32" fill="none" aria-hidden="true">
        <path
          d="M16 4L26.4 10V22L16 28L5.6 22V10L16 4Z"
          stroke="currentColor"
          stroke-width="2"
          fill="rgba(8, 145, 178, 0.14)"
        />
        <circle cx="16" cy="16" r="3" fill="currentColor" />
        <circle cx="16" cy="9" r="1.6" fill="currentColor" />
        <circle cx="22.5" cy="12.5" r="1.6" fill="currentColor" />
        <circle cx="22.5" cy="19.5" r="1.6" fill="currentColor" />
        <circle cx="16" cy="23" r="1.6" fill="currentColor" />
        <circle cx="9.5" cy="19.5" r="1.6" fill="currentColor" />
        <circle cx="9.5" cy="12.5" r="1.6" fill="currentColor" />
      </svg>
      <span class="topbar__name">SynPharm</span>
    </div>

    <!-- 一级导航：横向平铺文字链接，当前栏目用青色下划线点亮（不再用卡片/emoji，降低噪声） -->
    <nav class="topbar__nav" aria-label="主导航">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="topbar__link"
        :class="{ 'topbar__link--active': isActive(item) }"
        :title="item.label"
      >
        {{ item.label }}
      </router-link>
    </nav>

    <div class="topbar__user">
      <span class="topbar__avatar">{{ avatarText }}</span>
      <div class="topbar__user-info">
        <span class="topbar__name-text">{{ authStore.userNickname }}</span>
        <span class="topbar__role">{{ authStore.isGuest ? '游客' : '用户' }}</span>
      </div>
      <button class="topbar__logout" @click="handleLogout">退出</button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

interface NavItem {
  path: string
  label: string
  /** 用于高亮的路径前缀，可多个（详情页需点亮其所属一级栏目） */
  match: string[]
}

/**
 * 主导航项，数组顺序即展示顺序。
 *
 * ⚠️ `/visualization`（3D 可视化）**刻意不在这里** —— 它的能力已并入设计工作台的口袋页签。
 * 该页面本身仍然存在：预测结果页 / 任务详情等会带着预测上下文（亲和力、置信度、相互作用）
 * 深链进来，那部分属于预测域数据，工作台的口袋页签替代不了，所以只从导航移除、不删页面。
 */
const navItems: NavItem[] = [
  { path: '/design', label: '设计工作台', match: ['/design'] },
  { path: '/dashboard', label: '仪表盘', match: ['/dashboard'] },
  { path: '/predict', label: '预测中心', match: ['/predict'] },
  { path: '/results', label: '预测结果', match: ['/results', '/result'] },
  { path: '/tasks', label: '任务管理', match: ['/tasks'] },
  { path: '/targets', label: '靶点库', match: ['/targets'] },
  { path: '/profile', label: '个人中心', match: ['/profile'] }
]

/**
 * 判断某项是否为当前栏目。
 * 注意：结果列表是 /results，而结果详情是 /result/:id（单数），需分别匹配才会正确点亮。
 */
const isActive = (item: NavItem): boolean =>
  item.match.some((prefix) => route.path === prefix || route.path.startsWith(prefix + '/'))

const avatarText = computed(() => {
  if (!authStore.userNickname) return 'S'
  return authStore.userNickname.charAt(0).toUpperCase()
})

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.topbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: $header-height;
  background: $app-shell-bg;
  color: #fff;
  display: flex;
  align-items: center;
  padding: 0 $spacing-lg;
  gap: $spacing-xl;
  z-index: $z-sticky;
}

.topbar__brand {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  cursor: pointer;
  flex-shrink: 0;
}

.topbar__logo {
  width: 26px;
  height: 26px;
  color: $app-accent;
}

.topbar__name {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $app-shell-text;
  letter-spacing: -0.2px;
}

.topbar__nav {
  flex: 1;
  display: flex;
  align-items: stretch;
  align-self: stretch;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

.topbar__link {
  position: relative;
  display: inline-flex;
  align-items: center;
  padding: 0 $spacing-md;
  color: $app-shell-muted;
  font-size: $font-size-sm;
  text-decoration: none;
  white-space: nowrap;
  transition: color $transition-fast;

  &:hover {
    color: $app-shell-text;
  }

  // 当前栏目：白色字 + 底部青色指示条，比单纯变色更易扫视
  &::after {
    content: '';
    position: absolute;
    left: $spacing-md;
    right: $spacing-md;
    bottom: 0;
    height: 2px;
    border-radius: $radius-pill;
    background: transparent;
    transition: background $transition-fast;
  }

  &--active {
    color: #fff;
    font-weight: $font-weight-medium;

    &::after {
      background: $app-accent;
    }
  }
}

// 窄屏放不下 7 项时压缩间距，靠横向滚动兜底
@media (max-width: 1200px) {
  .topbar__link {
    padding: 0 $spacing-sm;

    &::after {
      left: $spacing-sm;
      right: $spacing-sm;
    }
  }
}

.topbar__user {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  flex-shrink: 0;
}

.topbar__avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(8, 145, 178, 0.28);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-sm;
  font-weight: 600;
  color: $app-shell-text;
}

.topbar__user-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.topbar__name-text {
  font-size: $font-size-sm;
  font-weight: 500;
  color: $app-shell-text;
}

.topbar__role {
  font-size: $font-size-xs;
  color: $app-shell-muted;
}

.topbar__logout {
  background: transparent;
  border: none;
  color: $app-shell-muted;
  font-size: $font-size-xs;
  cursor: pointer;
  padding: $spacing-xs $spacing-sm;
  border-radius: $border-radius-sm;
  transition: all $transition-fast;

  &:hover {
    background: rgba(239, 68, 68, 0.12);
    color: $error-color;
  }
}
</style>
