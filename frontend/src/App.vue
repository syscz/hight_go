<template>
  <div class="app">
    <!-- 广场：沉浸式布局（无侧边栏） -->
    <template v-if="layout === 'square'">
      <header class="square-topbar">
        <router-link to="/public" class="square-logo">SEE</router-link>
        <ModeSwitch class="sw-normal" />
        <ModeSwitch compact class="sw-compact" />
        <div class="square-actions">
          <button class="ghost-btn" @click="toggleTheme" :title="isDark ? '浅色模式' : '深色模式'">
            {{ isDark ? '☀️' : '🌙' }}
          </button>
          <template v-if="userStore.isLoggedIn">
            <button class="ghost-btn enter-space" @click="router.push('/')">进入个人空间</button>
            <img
              v-if="userStore.user?.avatar"
              :src="resolveMediaUrl(userStore.user.avatar)"
              class="bar-avatar"
              alt="头像"
              @click="router.push('/')"
            />
            <span v-else class="bar-avatar bar-avatar--placeholder" @click="router.push('/')">
              {{ avatarInitial }}
            </span>
          </template>
          <router-link v-else to="/auth" class="auth-link">登录 / 注册</router-link>
        </div>
      </header>
      <main class="square-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </template>

    <!-- 登录/注册：极简布局 -->
    <template v-else-if="layout === 'bare'">
      <header class="bare-topbar">
        <router-link to="/" class="square-logo">SEE</router-link>
        <button class="ghost-btn" @click="toggleTheme" :title="isDark ? '浅色模式' : '深色模式'">
          {{ isDark ? '☀️' : '🌙' }}
        </button>
      </header>
      <main class="bare-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </template>

    <!-- 个人空间：侧边栏布局 -->
    <template v-else>
      <aside class="sidebar">
        <router-link to="/" class="sidebar-logo">SEE</router-link>
        <nav class="sidebar-nav">
          <router-link to="/" class="side-link">
            <span class="side-icon">🖼</span><span>照片</span>
          </router-link>
          <router-link to="/upload" class="side-link">
            <span class="side-icon">⬆️</span><span>上传</span>
          </router-link>
          <router-link to="/?tab=albums" class="side-link">
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
          <img
            v-if="userStore.user?.avatar"
            :src="resolveMediaUrl(userStore.user.avatar)"
            class="bar-avatar"
            alt="头像"
          />
          <span v-else class="bar-avatar bar-avatar--placeholder">{{ avatarInitial }}</span>
          <span v-if="userStore.isLoggedIn" class="user-name">{{ userStore.displayName }}</span>
          <button v-if="userStore.isLoggedIn" class="logout-btn" @click="passwordDialogVisible = true">改密码</button>
          <button v-if="userStore.isLoggedIn" class="logout-btn" @click="handleLogout">退出</button>
        </div>
      </aside>

      <div class="main-area">
        <div class="topbar">
          <ModeSwitch />
          <el-input
            v-model="globalKeyword"
            placeholder="搜索媒体..."
            clearable
            class="global-search"
            @keyup.enter="doGlobalSearch"
          >
            <template #prefix>🔍</template>
          </el-input>
        </div>
        <header class="mobile-header">
          <router-link to="/" class="mobile-logo">SEE</router-link>
          <div class="mobile-header-right">
            <ModeSwitch compact />
            <button class="theme-toggle" @click="toggleTheme">{{ isDark ? '☀️' : '🌙' }}</button>
          </div>
        </header>
        <main class="main">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </main>
      </div>

      <nav class="mobile-nav">
        <router-link to="/" class="mobile-link"><span>🖼</span><span>照片</span></router-link>
        <router-link to="/?tab=albums" class="mobile-link"><span>📁</span><span>相册</span></router-link>
        <router-link to="/upload" class="mobile-link mobile-upload"><span class="upload-plus">＋</span></router-link>
        <router-link to="/search" class="mobile-link"><span>🔍</span><span>搜索</span></router-link>
        <router-link to="/calendar" class="mobile-link"><span>📅</span><span>日历</span></router-link>
      </nav>
    </template>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="360px" align-center>
      <el-form label-width="80px">
        <el-form-item label="旧密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="pwdForm.confirm" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api/index'
import { resolveMediaUrl } from '@/utils/media'
import ModeSwitch from '@/components/ModeSwitch.vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 启动时刷新用户资料（头像/背景在其他端更新后，本端免重登即可见）
onMounted(async () => {
  if (!localStorage.getItem('token')) return
  try {
    const response = await api.get('/auth/me')
    userStore.applyUserVO(response.data)
  } catch {
    // token 失效由 axios 拦截器统一处理
  }
})

const layout = computed(() => route.meta.layout || 'app')

const avatarInitial = computed(() => (userStore.displayName || '?').slice(0, 1).toUpperCase())

const isDark = ref(localStorage.getItem('theme') !== 'light')
document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')

const toggleTheme = () => {
  isDark.value = !isDark.value
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
}

const globalKeyword = ref('')
const passwordDialogVisible = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirm: '' })

const doGlobalSearch = () => {
  if (globalKeyword.value.trim()) {
    router.push({ path: '/search', query: { q: globalKeyword.value.trim() } })
  }
}

const handleChangePassword = async () => {
  if (pwdForm.value.newPassword !== pwdForm.value.confirm) {
    ElMessage.error('两次新密码不一致')
    return
  }
  try {
    await api.put('/auth/password', {
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
    pwdForm.value = { oldPassword: '', newPassword: '', confirm: '' }
  } catch (error) {
    ElMessage.error(error.response?.data?.error || '修改失败')
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  userStore.clear()
  window.location.href = '/auth'
}
</script>

<style lang="scss">
.app {
  display: block;
  min-height: 100vh;
}

/* ===== 通用小头像 / 幽灵按钮（广场顶栏 & 侧边栏共用） ===== */
.bar-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  border: 1.5px solid rgba(255, 255, 255, 0.35);
  flex-shrink: 0;
  cursor: pointer;

  &--placeholder {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
    color: #06222e;
    font-size: 0.8rem;
    font-weight: 700;
  }
}

.ghost-btn {
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

/* ===== 广场布局 ===== */
.square-topbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.65rem 1.5rem;
  background: rgba(10, 14, 20, 0.78);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);

  .square-logo {
    font-size: 1.4rem;
    font-weight: 800;
    letter-spacing: 2px;
    background: linear-gradient(135deg, #81d4fa 0%, #e0f7ff 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    text-decoration: none;
  }

  .sw-compact {
    display: none;
  }

  .square-actions {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 0.6rem;

    .enter-space {
      @media (max-width: 600px) {
        display: none;
      }
    }

    .auth-link {
      background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
      color: #06222e;
      font-weight: 700;
      font-size: 0.88rem;
      text-decoration: none;
      padding: 0.45rem 1rem;
      border-radius: 10px;
      transition: all 0.2s;

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 14px rgba(79, 195, 247, 0.4);
      }
    }
  }
}

html[data-theme='light'] .square-topbar,
html[data-theme='light'] .bare-topbar {
  background: rgba(255, 255, 255, 0.85);
  border-bottom-color: rgba(15, 30, 50, 0.08);

  .ghost-btn {
    background: rgba(15, 30, 50, 0.06);
    border-color: rgba(15, 30, 50, 0.12);
    color: rgba(15, 30, 50, 0.8);
  }
}

.square-main {
  min-height: calc(100vh - 61px);
}

/* ===== 登录页布局 ===== */
.bare-topbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.65rem 1.5rem;
  background: rgba(10, 14, 20, 0.78);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);

  .square-logo {
    font-size: 1.4rem;
    font-weight: 800;
    letter-spacing: 2px;
    background: linear-gradient(135deg, #81d4fa 0%, #e0f7ff 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    text-decoration: none;
  }
}

.bare-main {
  min-height: calc(100vh - 61px);
}

/* ===== 个人空间布局（原侧边栏布局） ===== */
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
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.main-area {
  flex: 1;
  margin-left: 210px;
  min-width: 0;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 950;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  padding: 0.7rem 1.5rem;
  background: rgba(10, 10, 12, 0.75);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);

  .global-search {
    width: 260px;
  }
}

html[data-theme='light'] .topbar {
  background: rgba(255, 255, 255, 0.8);
  border-bottom-color: rgba(15, 30, 50, 0.08);
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

  .bar-avatar {
    border-color: rgba(15, 30, 50, 0.2);
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

    .mobile-header-right {
      display: flex;
      align-items: center;
      gap: 0.5rem;
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

    .mobile-upload {
      .upload-plus {
        width: 44px;
        height: 44px;
        border-radius: 50%;
        background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
        color: #06222e;
        font-size: 1.5rem;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-top: -1.1rem;
        box-shadow: 0 6px 20px rgba(79, 195, 247, 0.4);
      }

      &.router-link-active .upload-plus {
        transform: scale(1.05);
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

  .topbar {
    display: none;
  }

  /* 广场顶栏移动端压缩 */
  .square-topbar {
    padding: 0.55rem 0.8rem;
    gap: 0.5rem;

    .sw-normal {
      display: none;
    }

    .sw-compact {
      display: inline-flex;
    }
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
