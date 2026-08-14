<template>
  <div class="trash-page">
    <div class="page-header">
      <h2>回收站</h2>
      <div class="header-actions" v-if="mediaList.length > 0">
        <el-button type="success" @click="restoreAll">全部恢复</el-button>
        <el-button type="danger" @click="permanentDeleteAll">清空回收站</el-button>
      </div>
    </div>
    <div v-if="mediaList.length === 0" class="empty-state">
      <p>回收站为空</p>
    </div>
    <div v-else class="media-grid">
      <div
        v-for="item in mediaList"
        :key="item.id"
        class="media-item"
      >
        <img v-if="item.mediaType === 'image'" :src="getCoverUrl(item)" :alt="item.title" loading="lazy" />
        <video v-else :src="getCoverUrl(item)" />
        <div class="media-overlay">
          <span class="media-title">{{ item.title || item.filename }}</span>
          <div class="media-actions">
            <button @click="restoreItem(item)">恢复</button>
            <button class="delete" @click="permanentDelete(item)">删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api/index'

const mediaList = ref([])

const fetchTrash = async () => {
  try {
    const response = await api.get('/media', { params: { deleted: true } })
    mediaList.value = response.data
  } catch (error) {
    ElMessage.error('获取回收站失败')
  }
}

const getCoverUrl = (item) => {
  if (!item.coverImage) return ''
  if (item.coverImage.startsWith('http')) return item.coverImage
  return 'http://47.97.191.168' + (item.coverImage.startsWith('/') ? '' : '/') + item.coverImage
}

const restoreItem = async (item) => {
  try {
    await api.post('/media/batch/restore', { mediaIds: [item.id] })
    ElMessage.success('恢复成功')
    fetchTrash()
  } catch (error) {
    ElMessage.error('恢复失败')
  }
}

const restoreAll = async () => {
  try {
    await ElMessageBox.confirm('确定要恢复所有内容吗？', '提示', { type: 'warning' })
    const ids = mediaList.value.map(m => m.id)
    await api.post('/media/batch/restore', { mediaIds: ids })
    ElMessage.success('恢复成功')
    fetchTrash()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('恢复失败')
  }
}

const permanentDelete = async (item) => {
  try {
    await ElMessageBox.confirm('此操作不可逆，确定要永久删除吗？', '警告', { type: 'warning' })
    await api.delete(`/media/${item.id}/permanent`)
    ElMessage.success('删除成功')
    fetchTrash()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const permanentDeleteAll = async () => {
  try {
    await ElMessageBox.confirm('此操作不可逆，确定要清空回收站吗？', '警告', { type: 'warning' })
    const ids = mediaList.value.map(m => m.id)
    for (const id of ids) {
      await api.delete(`/media/${id}/permanent`)
    }
    ElMessage.success('清空成功')
    fetchTrash()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('清空失败')
  }
}

onMounted(() => {
  fetchTrash()
})
</script>

<style lang="scss" scoped>
.trash-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
    h2 {
      margin: 0;
      color: var(--text-primary);
    }
    .header-actions {
      display: flex;
      gap: 1rem;
    }
  }
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted);
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.media-item {
  position: relative;
  border-radius: 14px;
  overflow: hidden;
  background: var(--glass);
  border: 1px solid var(--glass-border);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
  transition: all 0.3s;

  &:hover {
    transform: translateY(-4px);
    border-color: rgba(79, 195, 247, 0.45);
    box-shadow: var(--shadow-glow);
  }
  img, video {
    width: 100%;
    height: 180px;
    object-fit: cover;
    filter: grayscale(50%);
  }
  .media-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(transparent, rgba(0,0,0,0.8));
    padding: 1rem 0.5rem 0.5rem;
    .media-title {
      color: white;
      font-size: 0.9rem;
      display: block;
      margin-bottom: 0.5rem;
    }
    .media-actions {
      display: flex;
      gap: 0.5rem;
      button {
        flex: 1;
        padding: 0.3rem;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 0.8rem;
        background: rgba(255,255,255,0.2);
        color: white;
        &:hover {
          background: rgba(255,255,255,0.3);
        }
        &.delete {
          background: rgba(255,0,0,0.5);
          &:hover {
            background: rgba(255,0,0,0.7);
          }
        }
      }
    }
  }
}
</style>
