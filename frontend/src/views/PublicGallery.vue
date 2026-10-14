<template>
  <div class="public-gallery" :class="{ reloading }">
    <div class="square-hero">
      <h1>公开广场</h1>
      <p>随机漫步在大家公开分享的瞬间里</p>
      <button class="shuffle-btn" :disabled="loading" @click="shuffle">
        <span class="dice" :class="{ rolling: loading }">🎲</span>
        {{ loading ? '洗牌中…' : '换一批' }}
      </button>
    </div>

    <div v-if="loading && mediaList.length === 0" class="float-masonry skeleton-grid">
      <div v-for="n in 12" :key="n" class="float-card">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item variant="image" style="width: 100%; height: 200px" />
          </template>
        </el-skeleton>
      </div>
    </div>

    <EmptyState
      v-else-if="mediaList.length === 0"
      icon="🌍"
      title="还没有公开的媒体"
      hint="在你的媒体详情里打开「公开」，就会出现在这里"
    />

    <div v-else class="float-masonry">
      <div
        v-for="item in mediaList"
        :key="item.id"
        class="float-card"
        :style="floatVars(item)"
        @click="openLightbox(item)"
      >
        <div class="float-card-inner">
          <img
            v-if="imgSrc(item)"
            :src="imgSrc(item)"
            :alt="item.title || item.filename"
            loading="lazy"
            v-img-fade
          />
          <video
            v-else
            class="card-video"
            :src="frameSrc(item)"
            preload="metadata"
            muted
            playsinline
          ></video>

          <template v-if="item.mediaType === 'video'">
            <div class="video-play-badge"><span>▶</span></div>
            <span v-if="formatDuration(item.duration)" class="video-duration">
              {{ formatDuration(item.duration) }}
            </span>
          </template>

          <div class="author-chip">
            <img
              v-if="item.authorAvatar"
              :src="resolveMediaUrl(item.authorAvatar)"
              class="chip-avatar"
              alt=""
            />
            <span v-else class="chip-avatar chip-avatar--placeholder">
              {{ authorInitial(item) }}
            </span>
            <span class="chip-name">{{ item.authorName || `用户 ${item.userId}` }}</span>
          </div>
        </div>
      </div>
    </div>

    <MediaLightbox
      v-model:visible="lightboxVisible"
      v-model:index="lightboxIndex"
      :items="mediaList"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/index'
import MediaLightbox from '../components/MediaLightbox.vue'
import EmptyState from '@/components/EmptyState.vue'
import { resolveMediaUrl, tileImageSrc, videoFrameSrc, formatDuration } from '@/utils/media'

const RANDOM_LIMIT = 100

const mediaList = ref([])
const loading = ref(false)
const reloading = ref(false)
const lightboxVisible = ref(false)
const lightboxIndex = ref(0)

const imgSrc = (item) => tileImageSrc(item)
const frameSrc = (item) => videoFrameSrc(item)
const authorInitial = (item) => (item.authorName || `用户 ${item.userId}`).slice(0, 1).toUpperCase()

/** 每个卡片一份随机漂浮参数（负 delay 让动画起点错开，避免整页齐刷刷晃动） */
const decorate = (list) =>
  list.map((it) => ({
    ...it,
    _f: {
      dur: `${(4.5 + Math.random() * 4).toFixed(2)}s`,
      delay: `${(-Math.random() * 8).toFixed(2)}s`,
      amp: `${(6 + Math.random() * 9).toFixed(1)}px`
    }
  }))

const floatVars = (item) => ({
  '--f-dur': item._f?.dur,
  '--f-delay': item._f?.delay,
  '--f-amp': item._f?.amp
})

const fetchRandom = async () => {
  loading.value = true
  try {
    const response = await api.get('/media/public/random', { params: { limit: RANDOM_LIMIT } })
    mediaList.value = decorate(response.data || [])
  } catch (error) {
    console.error('获取随机公开媒体失败', error)
  } finally {
    loading.value = false
  }
}

const shuffle = async () => {
  reloading.value = true
  await fetchRandom()
  window.scrollTo({ top: 0, behavior: 'smooth' })
  setTimeout(() => {
    reloading.value = false
  }, 300)
}

const openLightbox = (item) => {
  const idx = mediaList.value.findIndex((m) => m.id === item.id)
  if (idx >= 0) {
    lightboxIndex.value = idx
    lightboxVisible.value = true
  }
}

onMounted(fetchRandom)
</script>

<style lang="scss" scoped>
.public-gallery {
  max-width: 1560px;
  margin: 0 auto;
  padding: 1rem 1.5rem 3rem;
  transition: opacity 0.3s ease;

  &.reloading {
    opacity: 0.25;
  }

  @media (max-width: 600px) {
    padding: 0.75rem 0.6rem 2rem;
  }
}

.square-hero {
  text-align: center;
  padding: 1.5rem 0 1.8rem;

  h1 {
    margin: 0;
    font-size: 1.7rem;
    letter-spacing: 1px;
    background: linear-gradient(135deg, #81d4fa 0%, #e0f7ff 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  p {
    margin: 0.35rem 0 0;
    font-size: 0.9rem;
    color: var(--text-muted);
  }

  .shuffle-btn {
    margin-top: 1rem;
    display: inline-flex;
    align-items: center;
    gap: 0.45rem;
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.16);
    color: rgba(255, 255, 255, 0.9);
    font-size: 0.92rem;
    font-weight: 600;
    padding: 0.5rem 1.2rem;
    border-radius: 999px;
    cursor: pointer;
    backdrop-filter: blur(8px);
    transition: all 0.2s ease;

    .dice {
      display: inline-block;
    }

    .rolling {
      animation: dice-roll 0.6s linear infinite;
    }

    &:hover:not(:disabled) {
      background: rgba(79, 195, 247, 0.18);
      border-color: rgba(79, 195, 247, 0.5);
      transform: translateY(-1px);
    }

    &:disabled {
      opacity: 0.6;
      cursor: wait;
    }
  }
}

@keyframes dice-roll {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 瀑布流：CSS columns 保持原始宽高比 */
.float-masonry {
  columns: 240px;
  column-gap: 14px;

  @media (max-width: 600px) {
    columns: 140px;
    column-gap: 8px;
  }
}

.float-card {
  break-inside: avoid;
  margin-bottom: 14px;
  animation: float-bob var(--f-dur, 6s) ease-in-out var(--f-delay, 0s) infinite alternate;
  will-change: transform;

  @media (max-width: 600px) {
    margin-bottom: 8px;
  }

  &:hover {
    animation-play-state: paused;
    z-index: 2;

    .float-card-inner {
      transform: scale(1.03);
      box-shadow: 0 10px 34px rgba(0, 0, 0, 0.45);

      img,
      .card-video {
        filter: brightness(1.06);
      }

      .author-chip {
        background: rgba(0, 0, 0, 0.72);
      }
    }
  }
}

@keyframes float-bob {
  from {
    transform: translateY(calc(var(--f-amp, 8px) * -1));
  }
  to {
    transform: translateY(var(--f-amp, 8px));
  }
}

@media (prefers-reduced-motion: reduce) {
  .float-card {
    animation: none;
  }
}

.float-card-inner {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  background: var(--glass);
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;

  img,
  .card-video {
    width: 100%;
    height: auto;
    display: block;
    min-height: 90px;
  }
}

.video-play-badge {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;

  span {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.45);
    border: 1.5px solid rgba(255, 255, 255, 0.8);
    color: white;
    font-size: 15px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding-left: 3px;
    backdrop-filter: blur(4px);
  }
}

.video-duration {
  position: absolute;
  right: 8px;
  bottom: 8px;
  background: rgba(0, 0, 0, 0.65);
  color: white;
  font-size: 0.72rem;
  font-weight: 600;
  line-height: 1;
  padding: 4px 6px;
  border-radius: 4px;
  font-variant-numeric: tabular-nums;
  pointer-events: none;
}

.author-chip {
  position: absolute;
  left: 8px;
  bottom: 8px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: calc(100% - 16px);
  padding: 3px 9px 3px 4px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(6px);
  transition: background 0.2s ease;

  .chip-avatar {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;

    &--placeholder {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
      color: #06222e;
      font-size: 0.62rem;
      font-weight: 700;
    }
  }

  .chip-name {
    color: rgba(255, 255, 255, 0.92);
    font-size: 0.72rem;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.skeleton-grid {
  .float-card {
    animation: none;
  }
}

html[data-theme='light'] .square-hero .shuffle-btn {
  background: rgba(15, 30, 50, 0.06);
  border-color: rgba(15, 30, 50, 0.14);
  color: rgba(15, 30, 50, 0.85);

  &:hover:not(:disabled) {
    background: rgba(79, 195, 247, 0.15);
    border-color: rgba(13, 134, 201, 0.5);
  }
}
</style>
