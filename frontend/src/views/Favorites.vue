<template>
  <div class="favorites-page">
    <h2>我的收藏</h2>
    <div v-if="mediaList.length === 0" class="empty-state">
      <p>暂无收藏内容</p>
    </div>
    <div v-else class="media-grid">
      <div
        v-for="item in mediaList"
        :key="item.id"
        class="media-item"
        @click="goToDetail(item.id)"
      >
        <img v-if="item.mediaType === 'image'" :src="getCoverUrl(item)" :alt="item.title" loading="lazy" />
        <video v-else :src="getCoverUrl(item)" />
        <div class="media-overlay">
          <span class="media-title">{{ item.title || item.filename }}</span>
        </div>
        <button class="favorite-btn active" @click.stop="toggleFavorite(item)">♥</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api/index'

const router = useRouter()
const mediaList = ref([])

const fetchFavorites = async () => {
  try {
    const response = await api.get('/media', { params: { favorite: true } })
    mediaList.value = response.data
  } catch (error) {
    ElMessage.error('获取收藏失败')
  }
}

const getCoverUrl = (item) => {
  if (!item.coverImage) return ''
  if (item.coverImage.startsWith('http')) return item.coverImage
  return 'http://47.97.191.168' + (item.coverImage.startsWith('/') ? '' : '/') + item.coverImage
}

const goToDetail = (id) => {
  router.push(`/media/${id}`)
}

const toggleFavorite = async (item) => {
  try {
    await api.post(`/media/${item.id}/favorite`)
    fetchFavorites()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style lang="scss" scoped>
.favorites-page {
  h2 {
    margin-bottom: 1.5rem;
    color: var(--text-primary);
  }
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted);
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.media-item {
  position: relative;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  background: var(--glass);
  border: 1px solid var(--glass-border);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
  transition: all 0.3s;

  &:hover {
    transform: translateY(-4px);
    border-color: rgba(79, 195, 247, 0.45);
    box-shadow: var(--shadow-glow);
  }

  img, video {
    width: 100%;
    height: 180px;
    object-fit: cover;
  }

  .media-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(transparent, rgba(0,0,0,0.7));
    padding: 1rem 0.5rem 0.5rem;
    .media-title {
      color: white;
      font-size: 0.9rem;
    }
  }

  .favorite-btn {
    position: absolute;
    top: 10px;
    right: 10px;
    background: rgba(0,0,0,0.3);
    border: none;
    color: white;
    width: 30px;
    height: 30px;
    border-radius: 50%;
    cursor: pointer;
    &.active {
      color: #ff6b6b;
    }
  }
}
</style>
