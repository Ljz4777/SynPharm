<template>
  <header class="topbar">
    <div class="topbar__brand" @click="router.push('/dashboard')">
      <span class="topbar__logo">🧬</span>
      <span class="topbar__name">SynPharm</span>
    </div>

    <div class="topbar__nav">
      <el-select
        class="topbar__select"
        :model-value="currentPath"
        placeholder="选择栏目"
        @change="navigateTo"
      >
        <el-option
          v-for="item in navItems"
          :key="item.path"
          :value="item.path"
          :label="item.icon + ' ' + item.label"
        />
      </el-select>
    </div>

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

const navItems = [
  { path: '/dashboard', label: '仪表盘', icon: '📊' },
  { path: '/predict', label: '预测中心', icon: '🎯' },
  { path: '/results', label: '预测结果', icon: '📈' },
  { path: '/tasks', label: '任务管理', icon: '📋' },
  { path: '/targets', label: '靶点库', icon: '🧪' },
  { path: '/visualization', label: '3D可视化', icon: '🧫' },
  { path: '/profile', label: '个人中心', icon: '👤' }
]

const currentPath = computed(() => route.path)

const avatarText = computed(() => {
  if (!authStore.userNickname) return '👤'
  return authStore.userNickname.charAt(0).toUpperCase()
})

const navigateTo = (path: string) => {
  router.push(path)
}

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
}

.topbar__select {
  width: 240px;
  :deep(.el-select__wrapper) {
    background: rgba(255, 255, 255, 0.1);
    box-shadow: none;
    border-radius: $border-radius-md;
  }
  :deep(.el-select__placeholder),
  :deep(.el-select__selected-item) {
    color: #fff;
    font-size: $font-size-sm;
  }
  :deep(.el-select__caret) { color: rgba(255, 255, 255, 0.7); }
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