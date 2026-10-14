<template>
  <div class="profile-header">
    <div
      class="ph-banner"
      :class="{ 'has-bg': !!bgUrl, uploading: uploadingBg }"
      :style="bgUrl ? { backgroundImage: `url(${bgUrl})` } : {}"
    >
      <label class="ph-edit ph-banner-edit" title="更换头像背景">
        <span>📷 更换背景</span>
        <input type="file" accept="image/*" hidden @change="(e) => upload(e, 'bg')" />
      </label>
    </div>

    <div class="ph-row">
      <div class="ph-avatar-wrap">
        <img v-if="avatarUrl" :src="avatarUrl" class="ph-avatar" alt="头像" />
        <span v-else class="ph-avatar ph-avatar--placeholder">{{ initial }}</span>
        <label class="ph-edit ph-avatar-edit" title="更换头像">
          <span>📷<i>更换头像</i></span>
          <input type="file" accept="image/*" hidden @change="(e) => upload(e, 'avatar')" />
        </label>
      </div>

      <div class="ph-meta">
        <h2 class="ph-username">{{ userStore.displayName }}</h2>
        <div class="ph-stats">
          <span class="ph-stat"><b>{{ stats.posts }}</b> 帖子</span>
          <span class="ph-stat"><b>{{ stats.albums }}</b> 相册</span>
          <span class="ph-stat"><b>{{ stats.favorites }}</b> 收藏</span>
        </div>
      </div>

      <el-button class="ph-upload" round type="primary" @click="router.push('/upload')">
        ⬆ 上传
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api/index'
import { resolveMediaUrl } from '@/utils/media'
import { useUserStore } from '@/stores/user'

defineProps({
  stats: {
    type: Object,
    default: () => ({ posts: 0, albums: 0, favorites: 0 })
  }
})

const router = useRouter()
const userStore = useUserStore()

const avatarUrl = computed(() =>
  userStore.user?.avatar ? resolveMediaUrl(userStore.user.avatar) : ''
)
const bgUrl = computed(() =>
  userStore.user?.avatarBg ? resolveMediaUrl(userStore.user.avatarBg) : ''
)
const initial = computed(() => (userStore.displayName || '?').slice(0, 1).toUpperCase())

const uploadingBg = ref(false)

const upload = async (event, target) => {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (target === 'bg') uploadingBg.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const url = target === 'bg' ? '/user/avatar-bg' : '/user/avatar'
    const response = await api.post(url, formData)
    userStore.applyUserVO(response.data)
    ElMessage.success(target === 'bg' ? '背景已更新 ✨' : '头像已更新 ✨')
  } catch (error) {
    ElMessage.error(error.response?.data?.error || '上传失败')
  } finally {
    uploadingBg.value = false
  }
}
</script>

<style lang="scss" scoped>
.profile-header {
  margin-bottom: 1.25rem;
}

.ph-banner {
  position: relative;
  width: 100%;
  aspect-ratio: 3.2 / 1;
  max-height: 260px;
  border-radius: 0 0 18px 18px;
  overflow: hidden;
  background:
    radial-gradient(ellipse at 20% 20%, rgba(79, 195, 247, 0.35), transparent 55%),
    radial-gradient(ellipse at 80% 60%, rgba(103, 232, 249, 0.22), transparent 55%),
    linear-gradient(135deg, #16324a 0%, #0a0e14 100%);
  background-size: cover;
  background-position: center;
  transition: opacity 0.2s;

  &.uploading {
    opacity: 0.6;
    pointer-events: none;
  }

  &.has-bg::after {
    content: '';
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.12);
  }
}

.ph-edit {
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.ph-banner-edit {
  position: absolute;
  right: 14px;
  bottom: 12px;
  z-index: 2;
  background: rgba(0, 0, 0, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 999px;
  padding: 0.4rem 0.9rem;
  color: white;
  font-size: 0.8rem;
  font-weight: 600;
  backdrop-filter: blur(8px);

  &:hover {
    background: rgba(0, 0, 0, 0.75);
  }
}

.ph-banner:hover .ph-banner-edit,
.ph-avatar-wrap:hover .ph-avatar-edit {
  opacity: 1;
}

.ph-row {
  display: flex;
  align-items: flex-end;
  gap: 1.25rem;
  padding: 0 1.5rem;
  margin-top: -44px;
}

.ph-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.ph-avatar {
  display: block;
  width: 116px;
  height: 116px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid var(--bg-base);
  background: var(--glass);
  position: relative;
  z-index: 1;

  &--placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
    color: #06222e;
    font-size: 2.6rem;
    font-weight: 800;
  }
}

.ph-avatar-edit {
  position: absolute;
  inset: 0;
  z-index: 2;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;

  span {
    color: white;
    font-size: 1.1rem;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;

    i {
      font-style: normal;
      font-size: 0.68rem;
      font-weight: 600;
    }
  }

  &:hover {
    background: rgba(0, 0, 0, 0.7);
  }
}

.ph-meta {
  flex: 1;
  min-width: 0;
  padding-bottom: 6px;

  .ph-username {
    margin: 0 0 0.35rem;
    font-size: 1.45rem;
    color: var(--text-primary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .ph-stats {
    display: flex;
    gap: 1.4rem;

    .ph-stat {
      color: var(--text-secondary);
      font-size: 0.9rem;

      b {
        color: var(--text-primary);
        font-size: 1.02rem;
        margin-right: 4px;
      }
    }
  }
}

.ph-upload {
  margin-bottom: 8px;
  flex-shrink: 0;
}

@media (max-width: 600px) {
  .ph-banner {
    aspect-ratio: 2.4 / 1;
    border-radius: 0 0 12px 12px;
  }

  .ph-row {
    padding: 0 0.75rem;
    margin-top: -34px;
    gap: 0.85rem;
  }

  .ph-avatar {
    width: 84px;
    height: 84px;
    border-width: 3px;

    &--placeholder {
      font-size: 1.9rem;
    }
  }

  .ph-meta {
    .ph-username {
      font-size: 1.15rem;
    }

    .ph-stats {
      gap: 0.8rem;

      .ph-stat {
        font-size: 0.78rem;

        b {
          font-size: 0.88rem;
        }
      }
    }
  }

  .ph-upload {
    display: none;
  }

  /* 移动端无 hover：常显更换按钮 */
  .ph-edit {
    opacity: 1;
  }

  .ph-avatar-edit span i {
    display: none;
  }
}
</style>
