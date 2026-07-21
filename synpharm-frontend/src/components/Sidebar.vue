<template>
  <aside class="sidebar">
    <div class="sidebar__brand">
      <div class="sidebar__logo">
        <span class="sidebar__logo-icon">🧬</span>
        <span class="sidebar__logo-text">SynPharm</span>
      </div>
    </div>
    
    <nav class="sidebar__nav">
      <router-link 
        v-for="item in navItems" 
        :key="item.path"
        :to="item.path"
        class="sidebar__nav-item"
        :class="{ 'sidebar__nav-item--active': $route.path === item.path }"
      >
        <span class="sidebar__nav-icon">{{ item.icon }}</span>
        <span class="sidebar__nav-text">{{ item.label }}</span>
        <span v-if="item.badge" class="sidebar__nav-badge">{{ item.badge }}</span>
      </router-link>
    </nav>
    
    <div class="sidebar__user">
      <div class="sidebar__user-avatar">
        <span class="sidebar__user-avatar-text">{{ avatarText }}</span>
      </div>
      <div class="sidebar__user-info">
        <span class="sidebar__user-name">{{ authStore.userNickname }}</span>
        <span class="sidebar__user-role">{{ authStore.isGuest ? '游客' : '用户' }}</span>
      </div>
      <button class="sidebar__logout-btn" @click="handleLogout">
        <span>退出</span>
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const navItems = [
  { path: '/dashboard', label: '仪表盘', icon: '📊' },
  { path: '/predict', label: '预测中心', icon: '🎯' },
  { path: '/results', label: '预测结果', icon: '📈' },
  { path: '/tasks', label: '任务管理', icon: '📋', badge: '2' },
  { path: '/targets', label: '靶点库', icon: '🧪' },
  { path: '/visualization', label: '3D可视化', icon: '3D' },
  { path: '/profile', label: '个人中心', icon: '👤' }
]

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
.sidebar {
  width: $sidebar-width;
  background: $bg-sidebar;
  color: #ffffff;
  display: flex;
  flex-direction: column;
  height: 100%;
  position: fixed;
  left: 0;
  top: 0;
  z-index: $z-sticky;
}

.sidebar__brand {
  padding: $spacing-lg;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.sidebar__logo {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.sidebar__logo-icon {
  font-size: 22px;
}

.sidebar__logo-text {
  font-size: $font-size-lg;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.sidebar__nav {
  flex: 1;
  padding: $spacing-md;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar__nav-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-md;
  color: rgba(255, 255, 255, 0.65);
  text-decoration: none;
  transition: all $transition-fast;
  
  &:hover {
    background: $bg-sidebar-hover;
    color: #ffffff;
  }
  
  &--active {
    background: rgba(59, 130, 246, 0.15);
    color: $accent-light;
  }
}

.sidebar__nav-icon {
  font-size: $font-size-base;
}

.sidebar__nav-text {
  flex: 1;
  font-size: $font-size-sm;
  font-weight: 400;
}

.sidebar__nav-badge {
  background: $error-color;
  color: #ffffff;
  font-size: $font-size-xs;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 100px;
}

.sidebar__user {
  padding: $spacing-md;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(0, 0, 0, 0.1);
}

.sidebar__user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(59, 130, 246, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.sidebar__user-avatar-text {
  font-size: $font-size-base;
  font-weight: 500;
}

.sidebar__user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sidebar__user-name {
  font-size: $font-size-sm;
  font-weight: 500;
}

.sidebar__user-role {
  font-size: $font-size-xs;
  color: rgba(255, 255, 255, 0.45);
}

.sidebar__logout-btn {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.45);
  font-size: $font-size-xs;
  cursor: pointer;
  padding: $spacing-xs $spacing-sm;
  border-radius: $border-radius-sm;
  transition: all $transition-fast;
  
  &:hover {
    background: rgba(239, 68, 68, 0.1);
    color: $error-color;
  }
}
</style>