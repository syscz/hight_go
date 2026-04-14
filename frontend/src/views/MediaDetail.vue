<template>
  <div class="media-detail">
    <el-button @click="goBack" class="back-button">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>

    <div v-loading="loading" class="detail-content">
      <!-- 媒体预览 -->
      <div class="media-preview">
        <img
          v-if="media && media.mediaType === 'image'"
          :src="getMediaUrl(media.filePath)"
          :alt="media.title"
          class="image-preview"
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

      <!-- 媒体信息 -->
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Calendar, Document, View, Delete } from '@element-plus/icons-vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

// 响应式数据
const media = ref(null)
const loading = ref(true)

// 获取完整的媒体文件URL
const getMediaUrl = (filePath) => {
  if (!filePath) return ''
  if (filePath.startsWith('http')) return filePath
  return 'http://47.97.191.168' + (filePath.startsWith('/') ? '' : '/') + filePath
}

// 方法
const fetchMediaDetail = async () => {
  const id = route.params.id
  try {
    const response = await axios.get(`/media/${id}`)
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
    await axios.delete(`/media/${media.value.id}`)
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

// 生命周期
onMounted(() => {
  fetchMediaDetail()
})
</script>

<style lang="scss" scoped>
.media-detail {
  .back-button {
    margin-bottom: 2rem;
  }

  .detail-content {
    display: grid;
    grid-template-columns: 1fr 300px;
    gap: 2rem;
    align-items: start;

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }

    .media-preview {
      background: #f5f5f5;
      border-radius: 8px;
      padding: 2rem;
      text-align: center;

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
      background: white;
      padding: 2rem;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      h2 {
        margin: 0 0 1rem 0;
        font-size: 1.5rem;
      }

      .info-meta {
        display: flex;
        gap: 1rem;
        margin-bottom: 1.5rem;
        flex-wrap: wrap;

        .meta-item {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          color: #666;
          font-size: 0.9rem;
        }
      }

      .info-description {
        margin-bottom: 2rem;

        h3 {
          margin: 0 0 0.5rem 0;
          font-size: 1.1rem;
        }

        p {
          margin: 0;
          line-height: 1.6;
          color: #333;
        }
      }

      .info-actions {
        display: flex;
        gap: 1rem;
      }
    }
  }
}
</style>
