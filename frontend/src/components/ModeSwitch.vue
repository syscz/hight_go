<template>
  <div class="mode-switch" :class="{ compact }">
    <button
      class="ms-btn"
      :class="{ active: isSquare }"
      @click="goSquare"
    >
      <el-icon class="ms-icon"><Compass /></el-icon><span class="ms-text">广场</span>
    </button>
    <button
      class="ms-btn"
      :class="{ active: !isSquare }"
      @click="goSpace"
    >
      <el-icon class="ms-icon"><User /></el-icon><span class="ms-text">个人空间</span>
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Compass, User } from '@element-plus/icons-vue'

defineProps({
  /** 紧凑模式：移动端只显示图标 */
  compact: { type: Boolean, default: false }
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isSquare = computed(() => route.name === 'PublicGallery')

const goSquare = () => {
  if (!isSquare.value) router.push('/public')
}

const goSpace = () => {
  if (isSquare.value) {
    router.push(userStore.isLoggedIn ? '/' : '/auth')
  }
}
</script>

<style lang="scss" scoped>
.mode-switch {
  display: inline-flex;
  gap: 4px;
  padding: 4px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);

  .ms-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    border: none;
    background: transparent;
    color: rgba(255, 255, 255, 0.68);
    font-size: 0.9rem;
    font-weight: 600;
    padding: 0.42rem 1rem;
    border-radius: 9px;
    cursor: pointer;
    transition: all 0.2s ease;

    .ms-icon {
      font-size: 0.95rem;
    }

    &:hover {
      color: #fff;
      background: rgba(255, 255, 255, 0.1);
    }

    &.active {
      background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
      color: #06222e;
      box-shadow: 0 2px 10px rgba(79, 195, 247, 0.35);
    }
  }

  &.compact {
    .ms-btn {
      padding: 0.42rem 0.6rem;

      .ms-text {
        display: none;
      }
    }
  }
}

html[data-theme='light'] .mode-switch {
  background: rgba(15, 30, 50, 0.06);
  border-color: rgba(15, 30, 50, 0.1);

  .ms-btn {
    color: rgba(15, 30, 50, 0.6);

    &:hover {
      color: #10233a;
      background: rgba(15, 30, 50, 0.08);
    }

    &.active {
      color: #06222e;
    }
  }
}
</style>
