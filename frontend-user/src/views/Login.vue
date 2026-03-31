<template>
  <div class="auth-page">
    <div class="auth-left">
      <div class="auth-brand">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none"><path d="M3 21V7l9-4 9 4v14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M9 21V13h6v8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
        <h1>Luxstay</h1>
        <p>探索全球精选酒店<br />开启您的非凡旅程</p>
      </div>
    </div>
    <div class="auth-right">
      <div class="auth-form-wrap fade-in-up">
        <h2>欢迎回来</h2>
        <p class="auth-subtitle">登录您的账户继续预订</p>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="form.username" type="text" class="input" placeholder="请输入用户名" required />
          </div>
          <div class="form-group">
            <label>密码</label>
            <input v-model="form.password" type="password" class="input" placeholder="请输入密码" required />
          </div>
          <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>
        <p class="auth-link">还没有账号？<router-link to="/register">立即注册</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const showToast = inject('showToast')
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const handleSubmit = async () => {
  loading.value = true
  try {
    await userStore.login(form)
    showToast('登录成功')
    router.push('/')
  } catch (e) {
    showToast(e.message || '登录失败', 'error')
  } finally { loading.value = false }
}
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
}

.auth-left {
  flex: 1;
  background: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    width: 400px;
    height: 400px;
    border-radius: 50%;
    background: rgba(201, 169, 110, 0.06);
    top: -80px;
    right: -80px;
  }
}

.auth-brand {
  color: #fff;
  text-align: center;
  position: relative;
  z-index: 1;

  svg { color: var(--accent); margin-bottom: 20px; }

  h1 {
    font-size: 36px;
    font-weight: 700;
    margin-bottom: 16px;
    letter-spacing: -0.03em;
  }

  p {
    color: var(--gray-400);
    font-size: 16px;
    line-height: 1.7;
  }
}

.auth-right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.auth-form-wrap {
  width: 100%;
  max-width: 360px;

  h2 {
    font-size: 28px;
    font-weight: 700;
    color: var(--gray-900);
    margin-bottom: 8px;
  }
}

.auth-subtitle {
  color: var(--gray-500);
  margin-bottom: 36px;
  font-size: 15px;
}

.form-group {
  margin-bottom: 20px;

  label {
    display: block;
    margin-bottom: 8px;
    font-weight: 500;
    font-size: 14px;
    color: var(--gray-700);
  }
}

.btn-block {
  width: 100%;
  height: 48px;
  font-size: 15px;
  margin-top: 8px;
}

.auth-link {
  text-align: center;
  margin-top: 28px;
  color: var(--gray-500);
  font-size: 14px;

  a { color: var(--accent); font-weight: 600; &:hover { color: var(--accent-hover); } }
}
</style>
