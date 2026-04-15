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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.auth-box {
  background: white;
  padding: 2.5rem;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
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
    background: #f0f0f0;
    color: #666;
    font-size: 1rem;
    font-weight: 600;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }

    &:hover:not(.active) {
      background: #e0e0e0;
    }
  }
}

.auth-form {
  h2 {
    text-align: center;
    margin-bottom: 1.5rem;
    color: #333;
  }
}

.form-group {
  margin-bottom: 1.25rem;

  input {
    width: 100%;
    padding: 0.875rem 1rem;
    border: 2px solid #e0e0e0;
    border-radius: 10px;
    font-size: 1rem;
    transition: border-color 0.3s;

    &:focus {
      outline: none;
      border-color: #667eea;
    }

    &::placeholder {
      color: #aaa;
    }
  }
}

.submit-btn {
  width: 100%;
  padding: 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
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
</style>