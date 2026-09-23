<template>
  <header class="topbar">
    <div class="topbar__brand" @click="router.push('/dashboard')">
      <span class="topbar__logo">🧬</span>
      <span class="topbar__name">SynPharm</span>
    </div>

    <!-- 一级导航：横向平铺，当前栏目高亮（不再用下拉框，避免栏目被藏起来） -->
    <nav class="topbar__nav" aria-label="主导航">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="topbar__link"
        :class="{ 'topbar__link--active': isActive(item) }"
        :title="item.label"
      >
        <span class="topbar__link-icon">{{ item.icon }}</span>
        <span class="topbar__link-label">{{ item.label }}</span>
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
  icon: string
  /** 用于高亮的路径前缀，可多个（详情页需点亮其所属一级栏目） */
  match: string[]
}

const navItems: NavItem[] = [
  { path: '/dashboard', label: '仪表盘', icon: '📊', match: ['/dashboard'] },
  { path: '/predict', label: '预测中心', icon: '🎯', match: ['/predict'] },
  { path: '/results', label: '预测结果', icon: '📈', match: ['/results', '/result'] },
  { path: '/tasks', label: '任务管理', icon: '📋', match: ['/tasks'] },
  { path: '/targets', label: '靶点库', icon: '🧪', match: ['/targets'] },
  { path: '/visualization', label: '3D可视化', icon: '🧫', match: ['/visualization'] },
  { path: '/design', label: '设计工作台', icon: '🧬', match: ['/design'] },
  { path: '/profile', label: '个人中心', icon: '👤', match: ['/profile'] }
]

/**
 * 判断某项是否为当前栏目。
 * 注意：结果列表是 /results，而结果详情是 /result/:id（单数），需分别匹配才会正确点亮。
 */
const isActive = (item: NavItem): boolean =>
  item.match.some((prefix) => route.path === prefix || route.path.startsWith(prefix + '/'))

const avatarText = computed(() => {
  if (!authStore.userNickname) return '👤'
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
  background: $bg-sidebar;
  color: #fff;
  display: flex;
  align-items: center;
  padding: 0 $spacing-lg;
  gap: $spacing-xl;
  z-index: $z-sticky;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.18);
}

.topbar__brand {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  cursor: pointer;
}

.topbar__logo { font-size: 20px; }
.topbar__name { font-size: $font-size-lg; font-weight: 600; }

.topbar__nav {
  flex: 1;
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: none;
  &::-webkit-scrollbar { display: none; }
}

.topbar__link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 $spacing-md;
  border-radius: $border-radius-md;
  color: rgba(255, 255, 255, 0.66);
  font-size: $font-size-sm;
  text-decoration: none;
  white-space: nowrap;
  transition: $transition-fast;

  &:hover {
    color: #fff;
    background: rgba(255, 255, 255, 0.08);
  }

  // 当前栏目：蓝色底 + 内描边，比单纯变色更容易扫视
  &--active {
    color: #fff;
    font-weight: 500;
    background: rgba(59, 130, 246, 0.22);
    box-shadow: inset 0 0 0 1px rgba(96, 165, 250, 0.35);
  }
}

.topbar__link-icon { font-size: 14px; line-height: 1; }

// 窄屏放不下 7 项时只留图标，名称通过 title 悬浮查看
@media (max-width: 1200px) {
  .topbar__link { padding: 0 $spacing-sm; }
  .topbar__link-label { display: none; }
}

.topbar__user {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.topbar__avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(59, 130, 246, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-size-sm;
  font-weight: 600;
}

.topbar__user-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.topbar__name-text { font-size: $font-size-sm; font-weight: 500; }
.topbar__role { font-size: $font-size-xs; color: rgba(255, 255, 255, 0.45); }

.topbar__logout {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.6);
  font-size: $font-size-xs;
  cursor: pointer;
  padding: $spacing-xs $spacing-sm;
  border-radius: $border-radius-sm;
  transition: $transition-fast;
  &:hover {
    background: rgba(239, 68, 68, 0.12);
    color: $error-color;
  }
}
</style>