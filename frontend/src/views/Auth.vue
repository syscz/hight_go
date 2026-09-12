<template>
  <div class="auth-container">
    <div class="auth-box">
      <div class="auth-tabs">
        <button
          :class="{ active: activeTab === 'login' }"
          @click="activeTab = 'login'"
        >
          登录
        </button>
        <button
          :class="{ active: activeTab === 'register' }"
          @click="activeTab = 'register'"
        >
          注册
        </button>
      </div>

      <form v-if="activeTab === 'login'" @submit.prevent="handleLogin" class="auth-form">
        <h2>欢迎回来</h2>
        <div class="form-group">
          <input
            v-model="loginForm.username"
            type="text"
            placeholder="用户名"
            required
          />
        </div>
        <div class="form-group">
          <input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            required
          />
        </div>
        <button type="submit" class="submit-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <form v-else @submit.prevent="handleRegister" class="auth-form">
        <h2>创建账户</h2>
        <div class="form-group">
          <input
            v-model="registerForm.username"
            type="text"
            placeholder="用户名"
            required
            minlength="3"
          />
        </div>
        <div class="form-group">
          <input
            v-model="registerForm.password"
            type="password"
            placeholder="密码"
            required
            minlength="6"
          />
        </div>
        <div class="form-group">
          <input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            required
          />
        </div>
        <button type="submit" class="submit-btn" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <p v-if="error" class="error-msg">{{ error }}</p>
      <p v-if="success" class="success-msg">{{ success }}</p>
      <p class="guest-link">
        <router-link to="/public">游客访问公开广场 →</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api/index'

const activeTab = ref('login')
const loading = ref(false)
const error = ref('')
const success = ref('')

const loginForm = ref({
  username: '',
  password: ''
})

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

const handleLogin = async () => {
  error.value = ''
  loading.value = true

  try {
    const response = await api.post('/auth/login', {
      username: loginForm.value.username,
      password: loginForm.value.password
    })

    if (response.data.token) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('user', JSON.stringify(response.data.user))
      ElMessage.success('登录成功')
      window.location.href = '/'
    }
  } catch (err) {
    error.value = err.response?.data?.error || '登录失败'
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  error.value = ''
  success.value = ''

  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    error.value = '两次密码不一致'
    return
  }

  loading.value = true

  try {
    const response = await api.post('/auth/register', {
      username: registerForm.value.username,
      password: registerForm.value.password
    })

    if (response.data.token) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('user', JSON.stringify(response.data.user))
      ElMessage.success('注册成功')
      window.location.href = '/'
    }
  } catch (err) {
    error.value = err.response?.data?.error || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(800px 400px at 70% 20%, rgba(79, 195, 247, 0.12), transparent 60%),
    radial-gradient(600px 300px at 20% 90%, rgba(103, 232, 249, 0.07), transparent 60%),
    var(--bg-base);
}

.auth-box {
  background: var(--glass);
  backdrop-filter: blur(20px);
  padding: 2.5rem;
  border-radius: 20px;
  border: 1px solid var(--glass-border);
  box-shadow: var(--shadow-card);
  width: 100%;
  max-width: 400px;
}

.auth-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;

  button {
    flex: 1;
    padding: 0.75rem;
    border: none;
    background: rgba(255, 255, 255, 0.07);
    color: var(--text-secondary);
    font-size: 1rem;
    font-weight: 600;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
      color: white;
    }

    &:hover:not(.active) {
      background: rgba(255, 255, 255, 0.13);
    }
  }
}

.auth-form {
  h2 {
    text-align: center;
    margin-bottom: 1.5rem;
    color: var(--text-primary);
  }
}

.form-group {
  margin-bottom: 1.25rem;

  input {
    width: 100%;
    padding: 0.875rem 1rem;
    border: 1px solid var(--glass-border);
    border-radius: 10px;
    font-size: 1rem;
    background: rgba(255, 255, 255, 0.06);
    color: var(--text-primary);
    transition: border-color 0.3s;

    &:focus {
      outline: none;
      border-color: #4fc3f7;
      box-shadow: 0 0 0 3px rgba(79, 195, 247, 0.2);
    }

    &::placeholder {
      color: var(--text-muted);
    }
  }
}

.submit-btn {
  width: 100%;
  padding: 1rem;
  background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 28px rgba(79, 195, 247, 0.35);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.error-msg {
  color: #e74c3c;
  text-align: center;
  margin-top: 1rem;
  font-size: 0.9rem;
}

.success-msg {
  color: #27ae60;
  text-align: center;
  margin-top: 1rem;
  font-size: 0.9rem;
}

.guest-link {
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.88rem;

  a {
    color: var(--text-secondary);
    text-decoration: none;
    transition: color 0.2s;

    &:hover {
      color: #4fc3f7;
    }
  }
}
</style>
