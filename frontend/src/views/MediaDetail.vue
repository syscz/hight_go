<template>
  <div class="media-detail">
    <el-button @click="goBack" class="back-button">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>

    <div v-loading="loading" class="detail-content">
      <div class="media-preview">
        <img
          v-if="media && media.mediaType === 'image'"
          :src="getMediaUrl(media.filePath)"
          :alt="media.title"
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
        <div class="info-description">
          <h3>描述</h3>
          <p>{{ media.description || '无描述' }}</p>
        </div>
        <div class="info-actions">
          <el-button type="danger" @click="handleDelete">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Calendar, Document, View, Delete } from '@element-plus/icons-vue'
import api from '@/api/index'

const route = useRoute()
const router = useRouter()

const media = ref(null)
const loading = ref(true)
const showLightbox = ref(false)
const scale = ref(1)
const lightboxImg = ref(null)

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
    router.push('/')
  } catch (error) {
    console.error('删除媒体失败:', error)
    ElMessage.error('删除失败')
  }
}

const goBack = () => {
  router.push('/')
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

onMounted(() => {
  fetchMediaDetail()
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
    background: white;
    border-radius: 12px;
    padding: 2rem;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  .media-preview {
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f5f5;
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
    }
  }

  .media-info {
    h2 {
      margin: 0 0 1rem 0;
      color: #333;
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
        color: #666;
        font-size: 0.9rem;
      }
    }

    .info-description {
      margin-bottom: 1.5rem;

      h3 {
        font-size: 1rem;
        color: #333;
        margin: 0 0 0.5rem 0;
      }

      p {
        color: #666;
        line-height: 1.6;
      }
    }

    .info-actions {
      display: flex;
      gap: 1rem;
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