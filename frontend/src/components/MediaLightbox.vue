<template>
  <div
    v-if="visible && current"
    class="media-lightbox"
    tabindex="0"
    @click.self="close"
  >
    <div class="lb-top">
      <span class="lb-count">{{ index + 1 }} / {{ items.length }}</span>
      <div class="lb-actions">
        <button class="lb-btn" @click="download">⬇ 下载</button>
        <button class="lb-btn" @click="goDetail">详情</button>
        <button class="lb-close" @click="close">×</button>
      </div>
    </div>

    <button class="lb-nav lb-prev" @click="prev">‹</button>

    <div class="lb-stage">
      <img
        v-if="current.mediaType === 'image'"
        :src="fullUrl(current.filePath)"
        :alt="current.title || current.filename"
      />
      <video
        v-else
        :src="fullUrl(current.videoMp4Path || current.filePath)"
        controls
        autoplay
      ></video>
    </div>

    <button class="lb-nav lb-next" @click="next">›</button>

    <div class="lb-caption">
      {{ current.title || current.filename }}
    </div>
  </div>
</template>

<script setup>
import { computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  visible: { type: Boolean, default: false },
  items: { type: Array, default: () => [] },
  index: { type: Number, default: 0 }
})

const emit = defineEmits(['update:visible', 'update:index'])
const router = useRouter()

const current = computed(() => props.items[props.index] || null)

const fullUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return 'http://47.97.191.168' + (path.startsWith('/') ? '' : '/') + path
}

const close = () => emit('update:visible', false)
const prev = () => {
  if (props.items.length > 0) {
    emit('update:index', (props.index - 1 + props.items.length) % props.items.length)
  }
}
const next = () => {
  if (props.items.length > 0) {
    emit('update:index', (props.index + 1) % props.items.length)
  }
}

const download = () => {
  if (!current.value) return
  const url = fullUrl(current.value.filePath)
  const a = document.createElement('a')
  a.href = url
  a.download = current.value.originalFilename || current.value.filename || 'media'
  a.click()
}

const goDetail = () => {
  if (!current.value) return
  close()
  router.push(`/media/${current.value.id}`)
}

const onKeydown = (e) => {
  if (!props.visible) return
  if (e.key === 'Escape') close()
  else if (e.key === 'ArrowLeft') prev()
  else if (e.key === 'ArrowRight') next()
}

watch(
  () => props.visible,
  (v) => {
    if (v) document.addEventListener('keydown', onKeydown)
    else document.removeEventListener('keydown', onKeydown)
  }
)

onUnmounted(() => document.removeEventListener('keydown', onKeydown))
</script>

<style lang="scss" scoped>
.media-lightbox {
  position: fixed;
  inset: 0;
  background: rgba(2, 3, 6, 0.97);
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  outline: none;
}

.lb-top {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: linear-gradient(rgba(0, 0, 0, 0.5), transparent);
}

.lb-count {
  color: rgba(255, 255, 255, 0.75);
  font-size: 0.9rem;
}

.lb-actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.lb-btn {
  padding: 0.45rem 1rem;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.2s;

  &:hover {
    background: rgba(79, 195, 247, 0.35);
    border-color: #4fc3f7;
  }
}

.lb-close {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  font-size: 1.4rem;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.25);
  }
}

.lb-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.06);
  color: white;
  font-size: 2rem;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba(79, 195, 247, 0.35);
    border-color: #4fc3f7;
  }
}

.lb-prev {
  left: 20px;
}

.lb-next {
  right: 20px;
}

.lb-stage {
  max-width: 88vw;
  max-height: 84vh;
  display: flex;
  align-items: center;
  justify-content: center;

  img,
  video {
    max-width: 88vw;
    max-height: 84vh;
    object-fit: contain;
    border-radius: 6px;
  }
}

.lb-caption {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  color: rgba(255, 255, 255, 0.85);
  font-size: 0.95rem;
  max-width: 70vw;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
