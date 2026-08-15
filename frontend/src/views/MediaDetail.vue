<template>
  <div class="media-detail" tabindex="0" @keydown="handleKeydown">
    <el-button @click="goBack" class="back-button">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>

    <div v-loading="loading" class="detail-content">
      <div class="media-preview">
        <img
          v-if="media && media.mediaType === 'image'"
          :src="getMediaUrl(media.filePath)"
          :alt="media.title"
          loading="lazy"
          class="image-preview"
          @click="showLightbox = true"
          style="cursor: zoom-in;"
        />
        <video
          v-else-if="media && media.mediaType === 'video'"
          :src="getMediaUrl(media.filePath)"
          controls
          class="video-preview"
        >
          您的浏览器不支持视频播放
        </video>
      </div>

      <div v-if="media" class="media-info">
        <h2>{{ media.title || media.filename }}</h2>
        <div class="info-meta">
          <span class="meta-item">
            <el-icon><Calendar /></el-icon>
            {{ formatDate(media.createdAt) }}
          </span>
          <span class="meta-item">
            <el-icon><Document /></el-icon>
            {{ formatSize(media.fileSize) }}
          </span>
          <span class="meta-item">
            <el-icon><View /></el-icon>
            {{ media.viewCount || 0 }} 次查看
          </span>
        </div>
        <div class="info-actions">
          <el-button :type="media.isFavorite ? 'danger' : 'default'" @click="toggleFavorite">
            {{ media.isFavorite ? '♥ 取消收藏' : '♡ 收藏' }}
          </el-button>
          <div class="public-toggle">
            <span class="public-label">公开</span>
            <el-switch
              :model-value="!!media.isPublic"
              @change="handlePublicChange"
              inline-prompt
              active-text="开"
              inactive-text="关"
            />
          </div>
          <el-button type="danger" @click="handleDelete">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
          <el-button @click="copyLink">
            <el-icon><Link /></el-icon> 复制链接
          </el-button>
        </div>
        <div class="info-description">
          <h3>描述</h3>
          <p>{{ media.description || '无描述' }}</p>
        </div>

        <div class="info-tags">
          <h3>标签</h3>
          <div class="tags-display">
            <el-tag
              v-for="tag in media.tags || []"
              :key="tag.id"
              closable
              @close="removeTag(tag.id)"
            >
              {{ tag.name }}
            </el-tag>
            <span v-if="!media.tags || media.tags.length === 0" class="no-tags">暂无标签</span>
          </div>
          <div class="tag-edit">
            <el-select
              v-model="newTagIds"
              multiple
              collapse-tags
              placeholder="选择已有标签"
              size="small"
              class="tag-select"
            >
              <el-option
                v-for="tag in allTags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
              />
            </el-select>
            <el-button size="small" type="primary" @click="saveTags">保存</el-button>
            <el-input
              v-model="newTagName"
              placeholder="新标签名"
              size="small"
              class="tag-name-input"
              @keyup.enter="createTag"
            />
            <el-button size="small" @click="createTag">新建</el-button>
          </div>
        </div>

        <div class="comments-section">
          <h3>评论 ({{ comments.length }})</h3>
          <div class="comment-input">
            <el-input
              v-model="newComment"
              type="textarea"
              placeholder="写下你的评论..."
              :rows="2"
            />
            <el-button type="primary" size="small" @click="submitComment" :disabled="!newComment.trim()">
              发布
            </el-button>
          </div>
          <div class="comments-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <span class="comment-user">{{ comment.username }}</span>
                <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                <el-button
                  v-if="comment.userId === currentUserId"
                  type="danger"
                  size="small"
                  link
                  @click="deleteComment(comment.id)"
                >
                  删除
                </el-button>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
            </div>
            <div v-if="comments.length === 0" class="no-comments">
              暂无评论，来发表第一条评论吧！
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showLightbox"
      class="lightbox-overlay"
      @click="showLightbox = false"
      @wheel.prevent="handleWheel"
    >
      <div class="lightbox-content" @click.stop>
        <img
          ref="lightboxImg"
          :src="getMediaUrl(media?.filePath)"
          :alt="media?.title"
          class="lightbox-image"
          :style="{ transform: `scale(${scale})` }"
          @click="toggleZoom"
        />
        <button class="lightbox-close" @click="showLightbox = false">×</button>
        <div class="zoom-controls">
          <button @click="zoomIn">+</button>
          <span>{{ Math.round(scale * 100) }}%</span>
          <button @click="zoomOut">−</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Calendar, Document, View, Delete, Link } from '@element-plus/icons-vue'
import api from '@/api/index'

const route = useRoute()
const router = useRouter()

const media = ref(null)
const loading = ref(true)
const showLightbox = ref(false)
const scale = ref(1)
const lightboxImg = ref(null)
const comments = ref([])
const newComment = ref('')
const currentUserId = ref(null)
const allTags = ref([])
const newTagIds = ref([])
const newTagName = ref('')

const currentMediaIndex = ref(0)
const allMediaList = ref([])

const handleKeydown = (e) => {
  if (e.key === 'ArrowLeft') {
    navigatePrev()
  } else if (e.key === 'ArrowRight') {
    navigateNext()
  } else if (e.key === 'Escape') {
    showLightbox.value = false
  }
}

const fetchAllMedia = async () => {
  try {
    const response = await api.get('/media')
    allMediaList.value = response.data
    const currentId = parseInt(route.params.id)
    currentMediaIndex.value = allMediaList.value.findIndex(m => m.id === currentId)
  } catch (error) {
    console.error('获取媒体列表失败', error)
  }
}

const navigatePrev = () => {
  if (currentMediaIndex.value > 0) {
    const prevMedia = allMediaList.value[currentMediaIndex.value - 1]
    router.push(`/media/${prevMedia.id}`)
  }
}

const navigateNext = () => {
  if (currentMediaIndex.value < allMediaList.value.length - 1) {
    const nextMedia = allMediaList.value[currentMediaIndex.value + 1]
    router.push(`/media/${nextMedia.id}`)
  }
}

const handleWheel = (e) => {
  if (e.deltaY < 0) {
    zoomIn()
  } else {
    zoomOut()
  }
}

const zoomIn = () => {
  scale.value = Math.min(scale.value + 0.25, 5)
}

const zoomOut = () => {
  scale.value = Math.max(scale.value - 0.25, 0.5)
}

const toggleZoom = () => {
  if (scale.value !== 1) {
    scale.value = 1
  } else {
    scale.value = 2
  }
}

const getMediaUrl = (filePath) => {
  if (!filePath) return ''
  if (filePath.startsWith('http')) return filePath
  return 'http://47.97.191.168' + (filePath.startsWith('/') ? '' : '/') + filePath
}

const fetchMediaDetail = async () => {
  const id = route.params.id
  try {
    const response = await api.get(`/media/${id}`)
    media.value = response.data
  } catch (error) {
    console.error('获取媒体详情失败:', error)
    ElMessage.error('获取媒体详情失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async () => {
  if (!media.value) return

  try {
    await api.delete(`/media/${media.value.id}`)
    ElMessage.success('删除成功')
    goBack()
  } catch (error) {
    console.error('删除媒体失败:', error)
    ElMessage.error('删除失败')
  }
}

const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(window.location.href)
    ElMessage.success('链接已复制')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const goBack = () => {
  if (route.query.from === 'album') {
    router.push({ name: 'Album', params: { id: route.query.albumId } })
    return
  }
  if (route.query.from === 'home') {
    router.push({
      path: '/',
      query: {
        viewMode: route.query.viewMode || 'all',
        keyword: route.query.keyword || ''
      }
    })
    return
  }
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

const formatSize = (size) => {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / (1024 * 1024)).toFixed(1) + ' MB'
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString()
}

const toggleFavorite = async () => {
  try {
    await api.post(`/media/${media.value.id}/favorite`)
    fetchMediaDetail()
    fetchAllMedia()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handlePublicChange = async (value) => {
  try {
    await api.put(`/media/${media.value.id}/public`, { isPublic: value })
    media.value.isPublic = value
    ElMessage.success(value ? '已设为公开' : '已设为私密')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const fetchComments = async () => {
  try {
    const response = await api.get(`/comments/media/${route.params.id}`)
    comments.value = response.data
  } catch (error) {
    console.error('获取评论失败', error)
  }
}

const fetchTags = async () => {
  try {
    const response = await api.get('/tags')
    allTags.value = response.data
  } catch (error) {
    console.error('获取标签失败', error)
  }
}

const saveTags = async () => {
  try {
    await api.put(`/media/${media.value.id}/tags`, { tagIds: newTagIds.value })
    ElMessage.success('标签已更新')
    newTagIds.value = []
    fetchMediaDetail()
  } catch (error) {
    ElMessage.error('保存标签失败')
  }
}

const removeTag = async (tagId) => {
  newTagIds.value = (media.value.tags || []).filter(t => t.id !== tagId).map(t => t.id)
  saveTags()
}

const createTag = async () => {
  const name = newTagName.value.trim()
  if (!name) return
  try {
    const response = await api.post('/tags', { name })
    newTagName.value = ''
    fetchTags()
    if (media.value) {
      newTagIds.value = [...new Set([...newTagIds.value, response.data.id])]
    }
    ElMessage.success('标签已创建')
  } catch (error) {
    ElMessage.error('创建标签失败')
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  try {
    await api.post('/comments', {
      mediaId: route.params.id,
      content: newComment.value
    })
    newComment.value = ''
    fetchComments()
    ElMessage.success('评论发布成功')
  } catch (error) {
    ElMessage.error('发布评论失败')
  }
}

const deleteComment = async (commentId) => {
  try {
    await api.delete(`/comments/${commentId}`)
    fetchComments()
    ElMessage.success('删除成功')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchMediaDetail()
  fetchComments()
  fetchAllMedia()
  fetchTags()
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    currentUserId.value = user.id
  }
  document.querySelector('.media-detail').focus()
})
</script>

<style lang="scss" scoped>
.media-detail {
  .back-button {
    margin-bottom: 1rem;
  }

  .detail-content {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 2rem;
    background: var(--glass);
    border-radius: 12px;
    padding: 2rem;
    border: 1px solid var(--glass-border);
    box-shadow: var(--shadow-card);

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  .media-preview {
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.35);
    border-radius: 8px;
    overflow: hidden;
    min-height: 400px;

    .image-preview {
      max-width: 100%;
      max-height: 600px;
      object-fit: contain;
    }

    .video-preview {
      max-width: 100%;
      max-height: 600px;
      border-radius: 8px;
      background: #000;
    }
  }

  .media-info {
    h2 {
      margin: 0 0 1rem 0;
      color: var(--text-primary);
    }

    .info-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 1rem;
      margin-bottom: 1.5rem;
      padding-bottom: 1rem;
      border-bottom: 1px solid #eee;

      .meta-item {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        color: var(--text-secondary);
        font-size: 0.9rem;
      }
    }

    .info-description {
      margin-bottom: 1.5rem;

      h3 {
        font-size: 1rem;
        color: var(--text-primary);
        margin: 0 0 0.5rem 0;
      }

      p {
        color: var(--text-secondary);
        line-height: 1.6;
      }
    }

    .info-tags {
      margin-bottom: 1.5rem;

      h3 {
        font-size: 1rem;
        color: var(--text-primary);
        margin: 0 0 0.5rem 0;
      }

      .tags-display {
        display: flex;
        flex-wrap: wrap;
        gap: 0.5rem;
        margin-bottom: 0.75rem;

        .no-tags {
          color: #999;
          font-size: 0.9rem;
        }
      }

      .tag-edit {
        display: flex;
        flex-wrap: wrap;
        gap: 0.5rem;
        align-items: center;

        .tag-select {
          width: 180px;
        }

        .tag-name-input {
          width: 130px;
        }
      }
    }

    .info-actions {
      display: flex;
      gap: 1rem;
      margin-bottom: 1.5rem;
      align-items: center;
      flex-wrap: wrap;

      .public-toggle {
        display: flex;
        align-items: center;
        gap: 0.5rem;

        .public-label {
          font-size: 0.9rem;
          color: var(--text-secondary);
        }
      }
    }
  }

  .comments-section {
    margin-top: 1.5rem;
    padding-top: 1.5rem;
    border-top: 1px solid #eee;

    h3 {
      font-size: 1rem;
      color: #333;
      margin: 0 0 1rem 0;
    }

    .comment-input {
      display: flex;
      gap: 0.5rem;
      margin-bottom: 1rem;

      .el-textarea {
        flex: 1;
      }
    }

    .comments-list {
      max-height: 300px;
      overflow-y: auto;
    }

      .comment-item {
        padding: 0.75rem 0;
        border-bottom: 1px solid rgba(255, 255, 255, 0.08);

      .comment-header {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        margin-bottom: 0.5rem;

        .comment-user {
          font-weight: 600;
          color: var(--text-primary);
        }

        .comment-time {
          color: var(--text-muted);
          font-size: 0.85rem;
        }
      }

      .comment-content {
        color: var(--text-secondary);
        line-height: 1.5;
      }
    }

    .no-comments {
      text-align: center;
      color: var(--text-muted);
      padding: 1rem;
    }
  }
}

.lightbox-dialog {
  :deep(.el-dialog) {
    background: rgba(0, 0, 0, 0.9) !important;
    box-shadow: none !important;
    max-width: 100vw !important;
    max-height: 100vh !important;
    width: 100vw !important;
    height: 100vh !important;
    margin: 0 !important;
    padding: 0 !important;
    display: flex !important;
    justify-content: center !important;
    align-items: center !important;
  }

  :deep(.el-dialog__header) {
    display: none !important;
  }

  :deep(.el-dialog__body) {
    padding: 0 !important;
    margin: 0 !important;
    display: flex !important;
    justify-content: center !important;
    align-items: center !important;
    width: 100% !important;
    height: 100% !important;
  }

  :deep(.el-overlay) {
    display: flex !important;
    justify-content: center !important;
    align-items: center !important;
  }
}

.lightbox-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.lightbox-content {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.lightbox-image {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
  object-fit: contain;
  display: block;
  cursor: zoom-in;
  transition: transform 0.2s ease;
}

.lightbox-close {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 24px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: background 0.3s;
}

.lightbox-close:hover {
  background: rgba(255, 255, 255, 0.4);
}

.zoom-controls {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 15px;
  background: rgba(0, 0, 0, 0.6);
  padding: 10px 20px;
  border-radius: 30px;
  color: white;
  font-size: 14px;
}

.zoom-controls button {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: background 0.3s;
}

.zoom-controls button:hover {
  background: rgba(255, 255, 255, 0.4);
}
</style>
