<template>
  <div id="app" :class="{ 'full-page': isFullPage }">
    <header v-if="!isFullPage" class="header" :class="{ scrolled: isScrolled }">
      <div class="container header-content">
        <router-link to="/" class="logo">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M3 21V7l9-4 9 4v14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M9 21V13h6v8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>Luxstay</span>
        </router-link>
        <nav class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/hotels" class="nav-link">探索酒店</router-link>
          <template v-if="userStore.token">
            <router-link to="/bookings" class="nav-link">我的预订</router-link>
            <div class="user-menu">
              <div class="user-avatar">{{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() }}</div>
              <button class="btn btn-ghost" @click="handleLogout">退出</button>
            </div>
          </template>
          <template v-else>
            <router-link to="/login" class="btn btn-ghost">登录</router-link>
            <router-link to="/register" class="btn btn-primary">注册</router-link>
          </template>
        </nav>
      </div>
    </header>
    <main class="main-content"><router-view /></main>
    <footer v-if="!isFullPage" class="footer">
      <div class="container footer-content">
        <div class="footer-brand">
          <div class="footer-logo">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M3 21V7l9-4 9 4v14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M9 21V13h6v8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            Luxstay
          </div>
          <p>发现世界各地的精选酒店，享受非凡旅程</p>
        </div>
        <div class="footer-bottom">
          <span>© 2024 Luxstay. All rights reserved.</span>
        </div>
      </div>
    </footer>
    <Toast :visible="toast.show" :message="toast.message" :type="toast.type" />
    <Confirm 
      :visible="confirm.show" 
      :title="confirm.title" 
      :message="confirm.message" 
      @confirm="handleConfirmOk" 
      @cancel="handleConfirmCancel" 
    />
  </div>
</template>

<script setup>
import { provide, reactive, ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from './stores/user'
import Toast from './components/Toast.vue'
import Confirm from './components/Confirm.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isScrolled = ref(false)

// 登录和注册页面为全屏页面，不显示 header 和 footer
const isFullPage = computed(() => {
  return route.path === '/login' || route.path === '/register'
})

const toast = reactive({ show: false, message: '', type: 'success' })
let toastTimer = null

const showToast = (message, type = 'success') => {
  if (toastTimer) clearTimeout(toastTimer)
  toast.message = message
  toast.type = type
  toast.show = true
  toastTimer = setTimeout(() => { toast.show = false }, 3000)
}
provide('showToast', showToast)

const confirm = reactive({ show: false, title: '', message: '', resolve: null })

const showConfirm = (message, title = '确认操作') => {
  return new Promise((resolve) => {
    confirm.message = message
    confirm.title = title
    confirm.resolve = resolve
    confirm.show = true
  })
}

const handleConfirmOk = () => {
  confirm.show = false
  if (confirm.resolve) confirm.resolve(true)
}

const handleConfirmCancel = () => {
  confirm.show = false
  if (confirm.resolve) confirm.resolve(false)
}

provide('showConfirm', showConfirm)

const handleLogout = () => {
  userStore.logout()
  showToast('已退出登录')
  router.push('/')
}

const onScroll = () => { isScrolled.value = window.scrollY > 20 }
onMounted(() => window.addEventListener('scroll', onScroll))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<style lang="scss" scoped>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;

  &.full-page {
    .main-content {
      flex: 1;
      display: flex;
      flex-direction: column;
    }
  }
}

.main-content {
  flex: 1;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(16px);
  transition: all var(--transition);
  border-bottom: 1px solid transparent;

  &.scrolled {
    border-bottom-color: var(--gray-100);
    box-shadow: 0 1px 12px rgba(0, 0, 0, 0.04);
  }
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: var(--primary);
  letter-spacing: -0.03em;

  svg { color: var(--accent); }
}

.nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-link {
  padding: 8px 16px;
  color: var(--gray-600);
  font-weight: 500;
  font-size: 14px;
  border-radius: var(--radius-sm);
  transition: all var(--transition);

  &:hover { color: var(--primary); background: var(--gray-50); }
  &.router-link-exact-active { color: var(--primary); font-weight: 600; }
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 8px;
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
  font-size: 13px;
}

.footer {
  background: var(--primary);
  color: var(--gray-400);
  padding: 48px 0 24px;
  margin-top: auto;
}

.footer-brand {
  margin-bottom: 32px;

  p { margin-top: 12px; font-size: 14px; max-width: 320px; line-height: 1.7; }
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #fff;

  svg { color: var(--accent); }
}

.footer-bottom {
  padding-top: 24px;
  border-top: 1px solid var(--gray-800);
  font-size: 13px;
}
</style>
