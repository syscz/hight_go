<template>
  <div class="app">
    <header class="header">
      <div class="container">
        <h1 class="logo">个人媒体网站</h1>
        <nav>
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/upload" class="nav-link">上传</router-link>
          <span v-if="currentUser" class="user-info">
            <span class="username">{{ currentUser.username }}</span>
            <button @click="handleLogout" class="logout-btn">退出</button>
          </span>
        </nav>
      </div>
    </header>

    <main class="main">
      <div class="container">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>

    <footer class="footer">
      <div class="container">
        <p>个人媒体网站</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const currentUser = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : null
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  window.location.href = '/auth'
}
</script>

<style lang="scss">
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: linear-gradient(135deg, #283c5c 0%, #1a2a40 100%);
  color: white;
  padding: 1rem 0;

  .container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 1rem;
  }

  .logo {
    font-size: 1.5rem;
    font-weight: bold;
  }

  nav {
    display: flex;
    gap: 1rem;
    align-items: center;

    .nav-link {
      color: white;
      text-decoration: none;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      transition: background-color 0.3s;

      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-left: 1rem;
      padding-left: 1rem;
      border-left: 1px solid rgba(255, 255, 255, 0.3);

      .username {
        font-size: 0.9rem;
      }

      .logout-btn {
        background: rgba(255, 255, 255, 0.2);
        color: white;
        border: none;
        padding: 0.35rem 0.75rem;
        border-radius: 4px;
        font-size: 0.85rem;
        cursor: pointer;
        transition: background-color 0.3s;

        &:hover {
          background: rgba(255, 255, 255, 0.3);
        }
      }
    }
  }
}

.main {
  flex: 1;
  padding: 2rem 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 1rem;
  }
}

.footer {
  background-color: #f5f5f5;
  padding: 1rem 0;
  margin-top: auto;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 1rem;
    text-align: center;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>