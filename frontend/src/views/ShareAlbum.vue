<template>
  <div class="share-album">
    <div class="share-head">
      <h1>📷 {{ album.name }}</h1>
      <p v-if="album.description">{{ album.description }}</p>
      <p class="share-count">共 {{ items.length }} 个作品</p>
    </div>

    <div v-if="loading" class="share-grid">
      <div v-for="n in 6" :key="n" class="share-tile skeleton">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item variant="image" style="width: 100%; aspect-ratio: 1" />
          </template>
        </el-skeleton>
      </div>
    </div>

    <EmptyState
      v-else-if="loadError"
      icon="🔗"
      title="分享不存在或已关闭"
      hint="链接可能已失效，请联系分享者重新生成"
    />

    <div v-else class="share-grid">
      <div
        v-for="(item, index) in items"
        :key="item.id"
        class="share-tile"
        @click="openLightbox(index)"
      >
        <img
          v-if="item.mediaType === 'image'"
          :src="resolveMediaUrl(item.coverImage || item.filePath)"
          :alt="item.title || item.filename"
          loading="lazy"
          v-img-fade
        />
        <video
          v-else-if="item.coverImage"
          :src="resolveMediaUrl(item.videoMp4Path || item.filePath) + '#t=1'"
          preload="metadata"
          muted
          playsinline
        ></video>
        <div v-else class="video-placeholder">🎬</div>
        <span v-if="item.mediaType === 'video'" class="play-mark">▶</span>
        <span v-if="item.duration" class="duration-mark">{{ formatDuration(item.duration) }}</span>
        <span v-if="item.likeCount" class="like-mark">♥ {{ item.likeCount }}</span>
      </div>
    </div>

    <MediaLightbox
      v-model:visible="lightboxVisible"
      v-model:index="lightboxIndex"
      :items="items"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api/index'
import { resolveMediaUrl, formatDuration } from '@/utils/media'
import MediaLightbox from '@/components/MediaLightbox.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const album = ref({ name: '', description: '' })
const items = ref([])
const loading = ref(true)
const loadError = ref(false)
const lightboxVisible = ref(false)
const lightboxIndex = ref(0)

const openLightbox = (index) => {
  lightboxIndex.value = index
  lightboxVisible.value = true
}

onMounted(async () => {
  try {
    const response = await api.get(`/share/album/${route.params.token}`)
    album.value = response.data
    items.value = response.data.items || []
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.share-album {
  max-width: 1010px;
  margin: 0 auto;
  padding: 1.5rem 1rem 3rem;
}

.share-head {
  text-align: center;
  padding: 1rem 0 1.5rem;

  h1 {
    margin: 0;
    font-size: 1.5rem;
    color: var(--text-primary);
  }

  p {
    margin: 0.4rem 0 0;
    color: var(--text-secondary);
    font-size: 0.92rem;
  }

  .share-count {
    color: var(--text-muted);
    font-size: 0.85rem;
  }
}

.share-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px;
}

.share-tile {
  position: relative;
  aspect-ratio: 1;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  background: var(--glass);

  img,
  video {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  &.skeleton {
    cursor: default;
  }

  .video-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 2rem;
  }

  .play-mark {
    position: absolute;
    top: 8px;
    right: 8px;
    color: white;
    font-size: 0.8rem;
    text-shadow: 0 1px 4px rgba(0, 0, 0, 0.7);
    pointer-events: none;
  }

  .duration-mark {
    position: absolute;
    bottom: 8px;
    right: 8px;
    background: rgba(0, 0, 0, 0.65);
    color: white;
    font-size: 0.72rem;
    padding: 2px 6px;
    border-radius: 4px;
    pointer-events: none;
  }

  .like-mark {
    position: absolute;
    bottom: 8px;
    left: 8px;
    background: rgba(0, 0, 0, 0.55);
    color: #ff8a8a;
    font-size: 0.72rem;
    font-weight: 600;
    padding: 2px 8px;
    border-radius: 10px;
    pointer-events: none;
  }
}

@media (max-width: 600px) {
  .share-grid {
    gap: 2px;
  }
}
</style>
