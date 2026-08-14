<template>
  <div class="app">
    <header class="header">
      <div class="container">
        <h1 class="logo">SEE</h1>
        <nav>
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/upload" class="nav-link">上传</router-link>
          <router-link to="/favorites" class="nav-link">收藏</router-link>
          <router-link to="/timeline" class="nav-link">时间线</router-link>
          <router-link to="/trash" class="nav-link">回收站</router-link>
          <router-link to="/search" class="nav-link">搜索</router-link>
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
        <p>SEE</p>
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
body {
  margin: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.3) 0%, rgba(0, 0, 0, 0.5) 100%),
              url('https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1920&q=80') center/cover no-repeat fixed;
  min-height: 100vh;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.3);

  .container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 2rem;
    height: 64px;
  }

  .logo {
    font-size: 1.4rem;
    font-weight: 700;
    color: white;
    text-shadow: 0 2px 4px rgba(0,0,0,0.3);
  }

  nav {
    display: flex;
    gap: 0.25rem;
    align-items: center;

    .nav-link {
      color: rgba(255, 255, 255, 0.85);
      text-decoration: none;
      padding: 0.5rem 1rem;
      border-radius: 8px;
      transition: all 0.2s ease;
      font-weight: 500;
      font-size: 0.95rem;
      position: relative;

      &:hover {
        color: white;
        background: rgba(255, 255, 255, 0.15);
      }

      &.router-link-active {
        color: white;
        background: rgba(129, 212, 250, 0.4);
        font-weight: 600;
        
        &::after {
          content: '';
          position: absolute;
          bottom: 4px;
          left: 50%;
          transform: translateX(-50%);
          width: 20px;
          height: 3px;
          background: #81d4fa;
          border-radius: 2px;
        }
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-left: 1rem;
      padding-left: 1rem;
      border-left: 1px solid rgba(255, 255, 255, 0.2);

      .username {
        font-size: 0.9rem;
        color: #81d4fa;
        font-weight: 600;
      }

      .logout-btn {
        background: rgba(255, 255, 255, 0.15);
        color: white;
        border: 1px solid rgba(255, 255, 255, 0.3);
        padding: 0.35rem 0.85rem;
        border-radius: 6px;
        font-size: 0.85rem;
        cursor: pointer;
        transition: all 0.2s ease;
        font-weight: 500;

        &:hover {
          background: #fee2e2;
          border-color: #fca5a5;
          color: #dc2626;
        }
      }
    }
  }
}

.main {
  flex: 1;
  padding: 1.5rem;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0;
  }
}

.footer {
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  padding: 1.25rem 0;
  margin-top: auto;
  border-top: 1px solid rgba(255, 255, 255, 0.1);

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 1rem;
    text-align: center;
    color: rgba(255, 255, 255, 0.8);
    font-size: 0.9rem;
  }
}

@media (max-width: 768px) {
  .header {
    .container {
      flex-direction: column;
      gap: 0.75rem;
      padding: 0 1rem;
      height: auto;
    }

    .logo {
      font-size: 1.2rem;
    }

    nav {
      flex-wrap: wrap;
      justify-content: center;
      gap: 0.25rem;

      .nav-link {
        padding: 0.4rem 0.6rem;
        font-size: 0.85rem;
      }

      .user-info {
        width: 100%;
        justify-content: center;
        margin-left: 0;
        padding-left: 0;
        border-left: none;
        margin-top: 0.5rem;
        padding-top: 0.5rem;
        border-top: 1px solid rgba(255, 255, 255, 0.1);
      }
    }
  }

  .main {
    padding: 0.75rem;
  }

  .footer {
    padding: 1rem 0;

    .container {
      font-size: 0.8rem;
    }
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