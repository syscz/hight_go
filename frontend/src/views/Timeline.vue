<template>
  <div class="timeline-page">
    <h2>照片时间线</h2>
    <EmptyState
      v-if="Object.keys(timeline).length === 0"
      icon="🕐"
      title="暂无照片"
      hint="上传内容后，按日期在这里浏览你的回忆"
      cta-text="去上传"
      @cta="router.push('/upload')"
    />
    <div v-else class="timeline">
      <div
        v-for="(items, date) in timeline"
        :key="date"
        class="timeline-item"
      >
        <div class="timeline-date">
          <span class="date-text">{{ formatDate(date) }}</span>
          <span class="date-count">{{ items.length }} 项</span>
        </div>
        <div class="timeline-media">
          <MediaTile
            v-for="item in items"
            :key="item.id"
            :item="item"
            :selectable="false"
            :favoritable="false"
            @open="goToDetail"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/index'
import MediaTile from '@/components/MediaTile.vue'
import EmptyState from '@/components/EmptyState.vue'

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

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

const goToDetail = (item) => {
  router.push(`/media/${item.id}`)
}

onMounted(() => {
  fetchTimeline()
})
</script>

<style lang="scss" scoped>
.timeline-page {
  h2 {
    margin-bottom: 1.5rem;
    color: var(--text-primary);
  }
}

.timeline {
  .timeline-item {
    margin-bottom: 1rem;

    .timeline-date {
      position: sticky;
      top: 55px;
      z-index: 5;
      display: flex;
      align-items: baseline;
      gap: 0.6rem;
      padding: 0.55rem 0.3rem;
      background: var(--bg-translucent);
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
      border-radius: 8px;

      .date-text {
        font-size: 1rem;
        font-weight: 700;
        color: var(--text-primary);
      }

      .date-count {
        font-size: 0.8rem;
        color: var(--text-muted);
      }
    }

    .timeline-media {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
      gap: 0.45rem;
      margin-top: 0.5rem;
    }
  }
}

@media (max-width: 768px) {
  .timeline {
    .timeline-item {
      .timeline-date {
        top: 54px;
      }

      .timeline-media {
        grid-template-columns: repeat(auto-fill, minmax(112px, 1fr));
        gap: 0.3rem;
      }
    }
  }
}
</style>
