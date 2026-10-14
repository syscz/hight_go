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
        <button class="lb-btn like-btn" :class="{ liked: liked }" @click="toggleLike">
          {{ liked ? '♥' : '♡' }} {{ likeCount }}
        </button>
        <button class="lb-btn" @click="toggleComments">💬 评论 ({{ comments.length }})</button>
        <button class="lb-btn" @click="infoVisible = !infoVisible">ℹ️ 信息</button>
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
        :style="{ transform: `scale(${scale})` }"
        class="lb-image"
        @wheel.prevent="handleWheel"
        @click="toggleZoom"
      />
      <video
        v-else
        :src="videoPlaySrc(current)"
        controls
        autoplay
      ></video>
    </div>

    <button class="lb-nav lb-next" @click="next">›</button>

    <div class="lb-caption">
      {{ current.title || current.filename }}
      <span v-if="current.mediaType === 'image'" class="lb-zoom-controls">
        <button @click="zoomOut">−</button>
        <span>{{ Math.round(scale * 100) }}%</span>
        <button @click="zoomIn">+</button>
      </span>
    </div>

    <transition name="fade">
      <div v-if="commentsVisible" class="lb-comments-panel">
        <h4>评论</h4>
        <div class="lb-comments-list">
          <div v-for="c in comments" :key="c.id" class="lb-comment">
            <div class="lb-comment-head">
              <b>{{ c.username }}</b>
              <span>{{ formatDate(c.createdAt) }}</span>
            </div>
            <div class="lb-comment-body">{{ c.content }}</div>
          </div>
          <div v-if="comments.length === 0" class="lb-comment-empty">还没有评论，来抢沙发～</div>
        </div>
        <div v-if="isLoggedIn" class="lb-comment-input">
          <input
            v-model="commentInput"
            type="text"
            placeholder="写下你的评论..."
            maxlength="200"
            @keyup.enter="submitComment"
          />
          <button @click="submitComment" :disabled="!commentInput.trim()">发送</button>
        </div>
        <p v-else class="lb-comment-login"><router-link to="/auth">登录</router-link> 后即可评论</p>
      </div>
    </transition>

    <transition name="fade">
      <div v-if="infoVisible" class="lb-info-panel">
        <h4>媒体信息</h4>
        <div class="info-row"><span>标题</span><b>{{ current.title || '—' }}</b></div>
        <div class="info-row"><span>文件名</span><b>{{ current.filename || '—' }}</b></div>
        <div class="info-row"><span>类型</span><b>{{ current.mediaType === 'image' ? '图片' : '视频' }}</b></div>
        <div class="info-row"><span>大小</span><b>{{ formatSize(current.fileSize) }}</b></div>
        <div class="info-row" v-if="current.mediaType === 'video' && current.duration">
          <span>时长</span><b>{{ formatDuration(current.duration) }}</b>
        </div>
        <div class="info-row"><span>上传时间</span><b>{{ formatDate(current.createdAt) }}</b></div>
        <div class="info-row"><span>浏览次数</span><b>{{ current.viewCount || 0 }}</b></div>
        <div class="info-row" v-if="current.tags && current.tags.length">
          <span>标签</span>
          <b>{{ current.tags.map(t => t.name).join('、') }}</b>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, ref, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api/index'
import { resolveMediaUrl, videoPlaySrc, formatDuration } from '@/utils/media'

const props = defineProps({
  visible: { type: Boolean, default: false },
  items: { type: Array, default: () => [] },
  index: { type: Number, default: 0 }
})

const emit = defineEmits(['update:visible', 'update:index'])
const router = useRouter()
const infoVisible = ref(false)
const scale = ref(1)

// 点赞与评论
const liked = ref(false)
const likeCount = ref(0)
const commentsVisible = ref(false)
const comments = ref([])
const commentInput = ref('')
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const resetSocialState = () => {
  const item = current.value
  liked.value = !!item?.likedByMe
  likeCount.value = item?.likeCount || 0
  comments.value = []
  commentInput.value = ''
  commentsVisible.value = false
}

const toggleLike = async () => {
  if (!isLoggedIn.value) {
    ElMessage.info('登录后即可点赞')
    return
  }
  try {
    const response = await api.post(`/media/${current.value.id}/like`)
    liked.value = response.data.liked
    likeCount.value = response.data.likeCount
    // 同步回列表数据，关闭灯箱后网格计数一致
    if (current.value) {
      current.value.likeCount = likeCount.value
      current.value.likedByMe = liked.value
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.error || '操作失败')
  }
}

const toggleComments = async () => {
  commentsVisible.value = !commentsVisible.value
  infoVisible.value = false
  if (commentsVisible.value && comments.value.length === 0) {
    try {
      const response = await api.get(`/comments/media/${current.value.id}`)
      comments.value = response.data || []
    } catch (error) {
      console.error('获取评论失败', error)
    }
  }
}

const submitComment = async () => {
  if (!commentInput.value.trim() || !current.value) return
  try {
    await api.post('/comments', {
      mediaId: current.value.id,
      content: commentInput.value.trim()
    })
    commentInput.value = ''
    const response = await api.get(`/comments/media/${current.value.id}`)
    comments.value = response.data || []
    ElMessage.success('评论发布成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.error || '发布失败')
  }
}

const current = computed(() => props.items[props.index] || null)

const zoomIn = () => {
  scale.value = Math.min(scale.value + 0.25, 5)
}

const zoomOut = () => {
  scale.value = Math.max(scale.value - 0.25, 0.5)
}

const toggleZoom = () => {
  scale.value = scale.value !== 1 ? 1 : 2
}

const handleWheel = (e) => {
  if (e.deltaY < 0) zoomIn()
  else zoomOut()
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const formatDate = (dateStr) => {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleString()
}

const fullUrl = (path) => resolveMediaUrl(path)

/** 预加载相邻图片，切换时不白屏等待 */
const preloadAdjacent = () => {
  const n = props.items.length
  if (n < 2) return
  ;[(props.index + 1) % n, (props.index - 1 + n) % n].forEach((i) => {
    const item = props.items[i]
    if (item && item.mediaType === 'image' && item.filePath) {
      const img = new Image()
      img.src = resolveMediaUrl(item.filePath)
    }
  })
}

watch(() => props.index, () => {
  scale.value = 1
  preloadAdjacent()
  resetSocialState()
})

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
    if (v) infoVisible.value = false
    if (v) scale.value = 1
    if (v) resetSocialState()
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

  .lb-image {
    transition: transform 0.2s ease;
    cursor: zoom-in;
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
  display: flex;
  align-items: center;
  gap: 0.75rem;

  .lb-zoom-controls {
    display: inline-flex;
    align-items: center;
    gap: 0.4rem;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 20px;
    padding: 0.2rem 0.6rem;

    button {
      width: 24px;
      height: 24px;
      border: none;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.15);
      color: white;
      font-size: 1rem;
      cursor: pointer;

      &:hover {
        background: rgba(79, 195, 247, 0.4);
      }
    }

    span {
      font-size: 0.75rem;
      color: rgba(255, 255, 255, 0.85);
      min-width: 40px;
      text-align: center;
    }
  }
}

.like-btn.liked {
  color: #ff6b6b;
  font-weight: 700;
}

.lb-comments-panel {
  position: absolute;
  right: 16px;
  bottom: 70px;
  width: 320px;
  max-height: 55vh;
  overflow-y: auto;
  background: var(--bg-elevated, #1b2230);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 12px;
  padding: 14px;
  z-index: 5;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.5);

  h4 {
    margin: 0 0 8px;
    color: var(--text-primary, #fff);
    font-size: 0.95rem;
  }

  .lb-comments-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-bottom: 10px;
  }

  .lb-comment {
    .lb-comment-head {
      display: flex;
      justify-content: space-between;
      font-size: 0.78rem;
      color: var(--text-muted, #9aa4b5);
      margin-bottom: 2px;

      b {
        color: var(--text-secondary, #cfd8e3);
      }
    }

    .lb-comment-body {
      color: var(--text-secondary, #cfd8e3);
      font-size: 0.85rem;
      line-height: 1.5;
      word-break: break-word;
    }
  }

  .lb-comment-empty {
    color: var(--text-muted, #9aa4b5);
    font-size: 0.85rem;
    text-align: center;
    padding: 10px 0;
  }

  .lb-comment-input {
    display: flex;
    gap: 6px;

    input {
      flex: 1;
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.16);
      border-radius: 8px;
      color: var(--text-primary, #fff);
      padding: 7px 10px;
      font-size: 0.85rem;
      outline: none;

      &:focus {
        border-color: #4fc3f7;
      }
    }

    button {
      background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
      border: none;
      color: #06222e;
      font-weight: 600;
      border-radius: 8px;
      padding: 0 14px;
      cursor: pointer;

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
  }

  .lb-comment-login {
    text-align: center;
    font-size: 0.82rem;
    color: var(--text-muted, #9aa4b5);

    a {
      color: #4fc3f7;
    }
  }
}

.lb-info-panel {
  position: absolute;
  top: 4.5rem;
  right: 1.25rem;
  width: 260px;
  background: rgba(18, 24, 32, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 12px;
  padding: 1rem;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(16px);

  h4 {
    margin: 0 0 0.75rem;
    color: rgba(255, 255, 255, 0.9);
    font-size: 0.95rem;
  }

  .info-row {
    display: flex;
    justify-content: space-between;
    gap: 0.75rem;
    padding: 0.35rem 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.07);

    span {
      color: rgba(255, 255, 255, 0.55);
      font-size: 0.8rem;
      flex-shrink: 0;
    }

    b {
      color: rgba(255, 255, 255, 0.9);
      font-size: 0.8rem;
      font-weight: 500;
      text-align: right;
      word-break: break-all;
    }
  }
}
</style>
