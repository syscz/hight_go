<template>
  <div class="timeline-page">
    <h2>照片时间线</h2>
    <div v-if="Object.keys(timeline).length === 0" class="empty-state">
      <p>暂无照片</p>
    </div>
    <div v-else class="timeline">
      <div
        v-for="(items, date) in timeline"
        :key="date"
        class="timeline-item"
      >
        <div class="timeline-date">{{ formatDate(date) }}</div>
        <div class="timeline-media">
          <div
            v-for="item in items"
            :key="item.id"
            class="media-item"
            @click="goToDetail(item.id)"
          >
            <img v-if="item.mediaType === 'image'" :src="getCoverUrl(item)" :alt="item.title" />
            <video v-else :src="getCoverUrl(item)" />
            <div class="media-overlay">
              <span class="media-title">{{ item.title || item.filename }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/index'

const router = useRouter()
const timeline = ref({})

const fetchTimeline = async () => {
  try {
    const response = await api.get('/media/timeline')
    timeline.value = response.data
  } catch (error) {
    console.error('获取时间线失败', error)
  }
}

const getCoverUrl = (item) => {
  if (!item.coverImage) return ''
  if (item.coverImage.startsWith('http')) return item.coverImage
  return 'http://47.97.191.168' + (item.coverImage.startsWith('/') ? '' : '/') + item.coverImage
}

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

const goToDetail = (id) => {
  router.push(`/media/${id}`)
}

onMounted(() => {
  fetchTimeline()
})
</script>

<style lang="scss" scoped>
.timeline-page {
  h2 {
    margin-bottom: 1.5rem;
    color: #333;
  }
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #999;
}

.timeline {
  .timeline-item {
    margin-bottom: 2rem;
    .timeline-date {
      font-size: 1.2rem;
      font-weight: bold;
      color: #333;
      margin-bottom: 1rem;
      padding-left: 0.5rem;
      border-left: 4px solid #283c5c;
    }
    .timeline-media {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
      gap: 1rem;
      padding-left: 1rem;
    }
  }
}

.media-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background: #f5f5f5;
  img, video {
    width: 100%;
    height: 150px;
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
      font-size: 0.85rem;
    }
  }
}
</style>
