<template>
  <div class="app">
    <!-- 桌面侧边栏 -->
    <aside class="sidebar">
      <router-link to="/" class="sidebar-logo">SEE</router-link>
      <nav class="sidebar-nav">
        <router-link to="/" class="side-link">
          <span class="side-icon">🖼</span><span>照片</span>
        </router-link>
        <router-link to="/?viewMode=albums" class="side-link">
          <span class="side-icon">📁</span><span>相册</span>
        </router-link>
        <router-link to="/timeline" class="side-link">
          <span class="side-icon">🕐</span><span>时间线</span>
        </router-link>
        <router-link to="/calendar" class="side-link">
          <span class="side-icon">📅</span><span>日历</span>
        </router-link>
        <router-link to="/search" class="side-link">
          <span class="side-icon">🔍</span><span>搜索</span>
        </router-link>
        <router-link to="/trash" class="side-link">
          <span class="side-icon">🗑</span><span>回收站</span>
        </router-link>
      </nav>
      <div class="sidebar-bottom">
        <button class="theme-toggle" @click="toggleTheme" :title="isDark ? '浅色模式' : '深色模式'">
          {{ isDark ? '☀️' : '🌙' }}
        </button>
        <span v-if="currentUser" class="user-name">{{ currentUser.username }}</span>
        <button v-if="currentUser" class="logout-btn" @click="handleLogout">退出</button>
      </div>
    </aside>

    <!-- 主内容 -->
    <div class="main-area">
      <header class="mobile-header">
        <router-link to="/" class="mobile-logo">SEE</router-link>
        <button class="theme-toggle" @click="toggleTheme">{{ isDark ? '☀️' : '🌙' }}</button>
      </header>
      <main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>

    <!-- 移动端底部导航 -->
    <nav class="mobile-nav">
      <router-link to="/" class="mobile-link"><span>🖼</span><span>照片</span></router-link>
      <router-link to="/?viewMode=albums" class="mobile-link"><span>📁</span><span>相册</span></router-link>
      <router-link to="/search" class="mobile-link"><span>🔍</span><span>搜索</span></router-link>
      <router-link to="/calendar" class="mobile-link"><span>📅</span><span>日历</span></router-link>
      <router-link to="/timeline" class="mobile-link"><span>🕐</span><span>时间线</span></router-link>
    </nav>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const currentUser = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : null
})

const isDark = ref(localStorage.getItem('theme') !== 'light')
document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')

const toggleTheme = () => {
  isDark.value = !isDark.value
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  window.location.href = '/auth'
}
</script>

<style lang="scss">
.app {
  display: flex;
  min-height: 100vh;
}

/* 桌面侧边栏 */
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 210px;
  background: rgba(10, 10, 12, 0.92);
  border-right: 1px solid rgba(255, 255, 255, 0.07);
  display: flex;
  flex-direction: column;
  padding: 1.2rem 0.75rem;
  z-index: 1000;

  .sidebar-logo {
    font-size: 1.4rem;
    font-weight: 800;
    letter-spacing: 2px;
    padding: 0.5rem 0.9rem 1.2rem;
    background: linear-gradient(135deg, #81d4fa 0%, #e0f7ff 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    text-decoration: none;
  }

  .sidebar-nav {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 0.2rem;

    .side-link {
      display: flex;
      align-items: center;
      gap: 0.7rem;
      padding: 0.55rem 0.9rem;
      border-radius: 8px;
      color: rgba(255, 255, 255, 0.72);
      text-decoration: none;
      font-size: 0.92rem;
      font-weight: 500;
      transition: all 0.15s;

      .side-icon {
        font-size: 1.05rem;
      }

      &:hover {
        background: rgba(255, 255, 255, 0.08);
        color: #fff;
      }

      &.router-link-active {
        background: rgba(79, 195, 247, 0.16);
        color: #4fc3f7;
        font-weight: 600;
      }
    }
  }

  .sidebar-bottom {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 0.5rem 0;
    border-top: 1px solid rgba(255, 255, 255, 0.07);

    .theme-toggle,
    .logout-btn {
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.14);
      color: rgba(255, 255, 255, 0.85);
      border-radius: 8px;
      padding: 0.4rem 0.7rem;
      cursor: pointer;
      font-size: 0.85rem;
      transition: all 0.15s;

      &:hover {
        background: rgba(255, 255, 255, 0.16);
      }
    }

    .user-name {
      color: rgba(255, 255, 255, 0.7);
      font-size: 0.85rem;
      margin-left: auto;
    }
  }
}

.main-area {
  flex: 1;
  margin-left: 210px;
  min-width: 0;
}

.main {
  padding: 1.25rem 1.5rem;
  max-width: 1500px;
  margin: 0 auto;
}

.mobile-header {
  display: none;
}

.mobile-nav {
  display: none;
}

/* 浅色主题侧边栏 */
html[data-theme='light'] .sidebar {
  background: rgba(255, 255, 255, 0.94);
  border-right-color: rgba(15, 30, 50, 0.08);

  .side-link {
    color: rgba(15, 30, 50, 0.7);

    &:hover {
      background: rgba(15, 30, 50, 0.06);
      color: #10233a;
    }

    &.router-link-active {
      background: rgba(79, 195, 247, 0.16);
      color: #0d86c9;
    }
  }

  .sidebar-bottom {
    border-top-color: rgba(15, 30, 50, 0.08);

    .theme-toggle,
    .logout-btn {
      background: rgba(15, 30, 50, 0.06);
      border-color: rgba(15, 30, 50, 0.12);
      color: rgba(15, 30, 50, 0.8);
    }

    .user-name {
      color: rgba(15, 30, 50, 0.6);
    }
  }
}

/* 移动端 */
@media (max-width: 768px) {
  .sidebar {
    display: none;
  }

  .main-area {
    margin-left: 0;
  }

  .mobile-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.75rem 1rem;
    position: sticky;
    top: 0;
    z-index: 900;
    background: rgba(10, 10, 12, 0.9);
    backdrop-filter: blur(16px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.07);

    .mobile-logo {
      font-size: 1.2rem;
      font-weight: 800;
      letter-spacing: 2px;
      background: linear-gradient(135deg, #81d4fa 0%, #e0f7ff 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      text-decoration: none;
    }

    .theme-toggle {
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.14);
      color: white;
      border-radius: 8px;
      padding: 0.35rem 0.6rem;
      cursor: pointer;
    }
  }

  html[data-theme='light'] .mobile-header {
    background: rgba(255, 255, 255, 0.94);
    border-bottom-color: rgba(15, 30, 50, 0.08);
  }

  .mobile-nav {
    display: flex;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(10, 10, 12, 0.94);
    backdrop-filter: blur(16px);
    border-top: 1px solid rgba(255, 255, 255, 0.08);
    z-index: 900;
    padding-bottom: env(safe-area-inset-bottom);

    .mobile-link {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 2px;
      padding: 0.55rem 0 0.4rem;
      color: rgba(255, 255, 255, 0.6);
      text-decoration: none;
      font-size: 0.68rem;

      span:first-child {
        font-size: 1.1rem;
      }

      &.router-link-active {
        color: #4fc3f7;
      }
    }
  }

  html[data-theme='light'] .mobile-nav {
    background: rgba(255, 255, 255, 0.96);
    border-top-color: rgba(15, 30, 50, 0.08);

    .mobile-link {
      color: rgba(15, 30, 50, 0.55);

      &.router-link-active {
        color: #0d86c9;
      }
    }
  }

  .main {
    padding: 0.75rem 0.6rem calc(4.5rem + env(safe-area-inset-bottom));
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
