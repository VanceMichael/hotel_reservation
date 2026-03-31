<template>
  <el-container class="layout-container">
    <el-aside :width="collapsed ? '72px' : '240px'" class="aside">
      <div class="sidebar-header">
        <div class="logo-icon">
          <el-icon :size="20"><Key /></el-icon>
        </div>
        <transition name="fade">
          <span v-if="!collapsed" class="logo-text">Hotel<span class="logo-accent">Admin</span></span>
        </transition>
      </div>
      <el-menu :default-active="route.path" router :collapse="collapsed" class="sidebar-menu">
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>数据概览</template>
        </el-menu-item>
        <el-menu-item index="/hotels">
          <el-icon><School /></el-icon>
          <template #title>酒店管理</template>
        </el-menu-item>
        <el-menu-item index="/rooms">
          <el-icon><Key /></el-icon>
          <template #title>房间管理</template>
        </el-menu-item>
        <el-menu-item index="/bookings">
          <el-icon><Calendar /></el-icon>
          <template #title>预订管理</template>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><Avatar /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/logs">
          <el-icon><Notebook /></el-icon>
          <template #title>操作日志</template>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-button text circle @click="collapsed = !collapsed">
          <el-icon :size="18"><component :is="collapsed ? 'Expand' : 'Fold'" /></el-icon>
        </el-button>
      </div>
    </el-aside>
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <h2 class="page-title">{{ route.meta.title }}</h2>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-avatar-wrap">
              <div class="user-avatar">{{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() }}</div>
              <span class="user-name">{{ userStore.userInfo?.username }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  min-height: 100vh;
}

.aside {
  background: #fff;
  border-right: 1px solid var(--gray-100);
  display: flex;
  flex-direction: column;
  transition: width var(--transition);
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  border-bottom: 1px solid var(--gray-100);
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: var(--primary);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent);
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--gray-900);
  white-space: nowrap;
  letter-spacing: -0.02em;
}

.logo-accent { color: var(--accent); font-weight: 400; }

.sidebar-menu {
  flex: 1;
  padding: 12px 8px;
  border-right: none !important;
  background: transparent;

  :deep(.el-menu-item) {
    height: 44px;
    line-height: 44px;
    border-radius: var(--radius-sm);
    margin-bottom: 2px;
    color: var(--gray-600);
    font-weight: 500;
    font-size: 14px;
    transition: all var(--transition);

    &:hover {
      background: var(--gray-50);
      color: var(--gray-900);
    }

    &.is-active {
      background: var(--primary);
      color: #fff;

      .el-icon { color: var(--accent); }
    }

    .el-icon {
      font-size: 18px;
      margin-right: 8px;
    }
  }

  &.el-menu--collapse {
    :deep(.el-menu-item) {
      padding: 0 !important;
      justify-content: center;

      .el-icon { margin-right: 0; }
    }
  }
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid var(--gray-100);
  display: flex;
  justify-content: center;
}

.main-container {
  margin-left: 240px;
  transition: margin-left var(--transition);
}

.aside[style*="72px"] + .main-container {
  margin-left: 72px;
}

.header {
  height: 64px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--gray-100);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--gray-900);
}

.user-avatar-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  transition: background var(--transition);

  &:hover { background: var(--gray-50); }
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--primary);
  color: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.user-name {
  font-weight: 500;
  color: var(--gray-700);
  font-size: 14px;
}

.arrow-icon {
  color: var(--gray-400);
  font-size: 12px;
}

.main {
  padding: 28px 32px;
  background: var(--gray-50);
  min-height: calc(100vh - 64px);
}

// 页面切换动画
.page-enter-active { animation: fadeInUp 0.3s ease-out; }
.page-leave-active { animation: fadeInUp 0.2s ease-in reverse; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
