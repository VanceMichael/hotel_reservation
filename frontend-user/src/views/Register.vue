<template>
  <div class="auth-page">
    <div class="auth-left">
      <div class="auth-brand">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none"><path d="M3 21V7l9-4 9 4v14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M9 21V13h6v8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
        <h1>Luxstay</h1>
        <p>加入我们<br />开启精彩旅程</p>
      </div>
    </div>
    <div class="auth-right">
      <div class="auth-form-wrap fade-in-up">
        <h2>创建账户</h2>
        <p class="auth-subtitle">注册后即可预订全球精选酒店</p>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="form.username" type="text" class="input" placeholder="3-20个字符" required minlength="3" maxlength="20" />
          </div>
          <div class="form-group">
            <label>密码</label>
            <input v-model="form.password" type="password" class="input" placeholder="至少6个字符" required minlength="6" maxlength="20" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>手机号</label>
              <input 
                v-model="form.phone" 
                type="tel" 
                class="input" 
                :class="{ 'input-error': phoneError }"
                placeholder="11位手机号（选填）" 
                @blur="validatePhone"
                @input="clearPhoneError"
              />
              <span v-if="phoneError" class="error-msg">{{ phoneError }}</span>
            </div>
            <div class="form-group">
              <label>邮箱</label>
              <input v-model="form.email" type="email" class="input" placeholder="选填" />
            </div>
          </div>
          <button type="submit" class="btn btn-primary btn-block" :disabled="loading || !!phoneError">
            {{ loading ? '注册中...' : '注册' }}
          </button>
        </form>
        <p class="auth-link">已有账号？<router-link to="/login">立即登录</router-link></p>
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
const phoneError = ref('')
const form = reactive({ username: '', password: '', phone: '', email: '' })

const validatePhone = () => {
  if (form.phone && !/^1[3-9]\d{9}$/.test(form.phone)) {
    phoneError.value = '请输入正确的11位手机号'
    return false
  }
  phoneError.value = ''
  return true
}

const clearPhoneError = () => {
  if (phoneError.value) phoneError.value = ''
}

const handleSubmit = async () => {
  if (!validatePhone()) {
    showToast('请输入正确的手机号', 'error')
    return
  }
  loading.value = true
  try {
    await userStore.register(form)
    showToast('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    showToast(e.message || '注册失败', 'error')
  } finally { loading.value = false }
}
</script>

<style lang="scss" scoped>
.auth-page { min-height: 100vh; display: flex; }

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
    bottom: -80px;
    left: -80px;
  }
}

.auth-brand {
  color: #fff;
  text-align: center;

  svg { color: var(--accent); margin-bottom: 20px; }
  h1 { font-size: 36px; font-weight: 700; margin-bottom: 16px; letter-spacing: -0.03em; }
  p { color: var(--gray-400); font-size: 16px; line-height: 1.7; }
}

.auth-right {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.auth-form-wrap {
  width: 100%;
  max-width: 400px;

  h2 { font-size: 28px; font-weight: 700; color: var(--gray-900); margin-bottom: 8px; }
}

.auth-subtitle { color: var(--gray-500); margin-bottom: 36px; font-size: 15px; }

.form-group {
  margin-bottom: 20px;
  flex: 1;

  label { display: block; margin-bottom: 8px; font-weight: 500; font-size: 14px; color: var(--gray-700); }
}

.form-row { display: flex; gap: 16px; }

.btn-block { width: 100%; height: 48px; font-size: 15px; margin-top: 8px; }

.auth-link {
  text-align: center;
  margin-top: 28px;
  color: var(--gray-500);
  font-size: 14px;

  a { color: var(--accent); font-weight: 600; &:hover { color: var(--accent-hover); } }
}

.input-error {
  border-color: #ff4d4f !important;
  &:focus { box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.2) !important; }
}

.error-msg {
  display: block;
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}
</style>
