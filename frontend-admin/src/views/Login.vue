<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand-content">
        <div class="brand-icon">
          <el-icon :size="36"><Key /></el-icon>
        </div>
        <h1>Hotel<span>Admin</span></h1>
        <p class="brand-desc">酒店预订管理系统控制台</p>
        <div class="brand-features">
          <div class="feature-item" v-for="(f, i) in features" :key="i" :style="{ animationDelay: `${0.2 + i * 0.1}s` }">
            <div class="feature-dot"></div>
            <span>{{ f }}</span>
          </div>
        </div>
      </div>
    </div>
    <div class="login-right">
      <div class="login-form-wrapper">
        <h2>欢迎回来</h2>
        <p class="login-subtitle">请输入管理员账号登录系统</p>
        <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleLogin" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" prefix-icon="Avatar" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleLogin" class="login-btn">
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const features = ['实时数据仪表盘', '酒店与房间管理', '预订订单追踪', '用户权限管理']
const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await userStore.login(form)
    if (res.user.role !== 1) {
      ElMessage.error('无管理员权限')
      userStore.logout()
      return
    }
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
}

.login-left {
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
    width: 500px;
    height: 500px;
    border-radius: 50%;
    background: rgba(201, 169, 110, 0.06);
    top: -100px;
    right: -100px;
  }

  &::after {
    content: '';
    position: absolute;
    width: 300px;
    height: 300px;
    border-radius: 50%;
    background: rgba(201, 169, 110, 0.04);
    bottom: -50px;
    left: -50px;
  }
}

.brand-content {
  color: #fff;
  padding: 60px;
  position: relative;
  z-index: 1;
  animation: fadeInUp 0.6s ease-out;
}

.brand-icon {
  width: 64px;
  height: 64px;
  background: rgba(201, 169, 110, 0.15);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
  color: var(--accent);
}

.brand-content h1 {
  font-family: var(--font-body);
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 12px;
  letter-spacing: -0.02em;

  span {
    color: var(--accent);
    font-weight: 300;
  }
}

.brand-desc {
  color: var(--gray-400);
  font-size: 16px;
  margin-bottom: 48px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--gray-300);
  font-size: 15px;
  animation: fadeInUp 0.5s ease-out both;
}

.feature-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent);
  flex-shrink: 0;
}

.login-right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: #fff;
}

.login-form-wrapper {
  width: 100%;
  max-width: 360px;
  animation: fadeInUp 0.5s ease-out 0.2s both;

  h2 {
    font-family: var(--font-body);
    font-size: 28px;
    font-weight: 700;
    color: var(--gray-900);
    margin-bottom: 8px;
  }
}

.login-subtitle {
  color: var(--gray-500);
  margin-bottom: 36px;
  font-size: 15px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  border-radius: var(--radius-sm);
  letter-spacing: 0.02em;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
