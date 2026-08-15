<template>
  <div class="public-gallery">
    <div class="gallery-head">
      <h1>公开广场</h1>
      <p>这里展示所有用户公开分享的媒体</p>
    </div>

    <div v-if="loading && mediaList.length === 0" class="media-grid skeleton-grid">
      <div v-for="n in 12" :key="n" class="media-item">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item variant="image" style="width: 100%; height: 100%" />
          </template>
        </el-skeleton>
      </div>
    </div>

    <div v-else-if="mediaList.length === 0" class="empty-state">
      <div class="empty-icon">🌍</div>
      <p>还没有公开的媒体</p>
      <p class="empty-hint">在你的媒体详情里打开"公开"，就会出现在这里</p>
    </div>

    <div v-else class="media-grid">
      <div
        v-for="(item, index) in mediaList"
        :key="item.id"
        class="media-item"
        @click="openLightbox(index)"
      >
        <img
          v-if="item.mediaType === 'image'"
          :src="getCoverUrl(item)"
          :alt="item.title"
          loading="lazy"
        />
        <video v-else :src="getCoverUrl(item)" />
        <div class="media-overlay">
          <span class="media-title">{{ item.title || item.filename }}</span>
        </div>
        <span class="author-tag">{{ authorName(item) }}</span>
      </div>
    </div>

    <div v-if="hasMore && !loading" ref="sentinel" class="scroll-sentinel">
      <span>加载中…</span>
    </div>

    <MediaLightbox
      v-model:visible="lightboxVisible"
      v-model:index="lightboxIndex"
      :items="mediaList"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import api from '@/api/index'
import MediaLightbox from '../components/MediaLightbox.vue'

const mediaList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(24)
const loading = ref(false)
const lightboxVisible = ref(false)
const lightboxIndex = ref(0)
const sentinel = ref(null)
let observer = null

const hasMore = computed(() => mediaList.value.length < total.value)

const getCoverUrl = (item) => {
  if (!item.coverImage) return ''
  if (item.coverImage.startsWith('http')) return item.coverImage
  return 'http://47.97.191.168' + (item.coverImage.startsWith('/') ? '' : '/') + item.coverImage
}

const authorName = (item) => {
  return item.userId ? `用户 ${item.userId}` : ''
}

const openLightbox = (index) => {
  lightboxIndex.value = index
  lightboxVisible.value = true
}

const fetchMedia = async (append = false) => {
  if (loading.value) return
  loading.value = true
  try {
    const response = await api.get('/media/public', {
      params: { page: currentPage.value, size: pageSize.value }
    })
    const data = response.data
    const list = data.list || []
    total.value = data.total || 0
    mediaList.value = append ? [...mediaList.value, ...list] : list
  } catch (error) {
    console.error('获取公开媒体失败', error)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  if (!hasMore.value || loading.value) return
  currentPage.value += 1
  fetchMedia(true)
}

onMounted(() => {
  fetchMedia()
  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting) loadMore()
  }, { rootMargin: '300px' })
  watch(hasMore, (more) => {
    if (more) {
      nextTick(() => {
        if (sentinel.value && observer) observer.observe(sentinel.value)
      })
    }
  })
})

onBeforeUnmount(() => {
  if (observer) observer.disconnect()
})
</script>

<style lang="scss" scoped>
.public-gallery {
  .gallery-head {
    margin-bottom: 1rem;
    padding: 0 0.25rem;

    h1 {
      margin: 0;
      font-size: 1.3rem;
      color: var(--text-primary);
    }

    p {
      margin: 0.2rem 0 0;
      font-size: 0.85rem;
      color: var(--text-muted);
    }
  }
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 0.45rem;

  @media (max-width: 600px) {
    grid-template-columns: repeat(auto-fill, minmax(112px, 1fr));
    gap: 0.3rem;
  }
}

.media-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  background: var(--glass);
  transition: all 0.2s;

  &:hover {
    transform: scale(1.02);
  }

  img,
  video {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: transform 0.35s ease;
  }

  &:hover img {
    transform: scale(1.05);
  }

  .media-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.75));
    padding: 1.2rem 0.5rem 0.4rem;
    opacity: 0;
    transition: opacity 0.2s;

    .media-title {
      color: #fff;
      font-size: 0.8rem;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      display: block;
    }
  }

  &:hover .media-overlay {
    opacity: 1;
  }

  .author-tag {
    position: absolute;
    top: 8px;
    left: 8px;
    background: rgba(0, 0, 0, 0.55);
    color: rgba(255, 255, 255, 0.9);
    font-size: 0.7rem;
    padding: 2px 8px;
    border-radius: 20px;
    backdrop-filter: blur(4px);
  }
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;

  .empty-icon {
    font-size: 4rem;
    margin-bottom: 1rem;
  }

  p {
    color: var(--text-secondary);
    font-size: 1.1rem;
    font-weight: 500;
    margin-bottom: 0.4rem;
  }

  .empty-hint {
    font-size: 0.85rem;
    color: var(--text-muted);
    font-weight: 400;
  }
}

.scroll-sentinel {
  display: flex;
  justify-content: center;
  padding: 1.5rem 0;
  color: var(--text-muted);
  font-size: 0.85rem;
}
</style>
