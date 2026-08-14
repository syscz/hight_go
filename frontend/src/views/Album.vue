<template>
  <div class="album-view">
    <header class="header">
      <div class="header-content">
        <h1>{{ albumName }}</h1>
        <p>🎨 {{ mediaList.length }} 个作品</p>
      </div>
      <div class="header-actions">
        <el-button round @click="goBack">← 返回</el-button>
        <el-button round type="primary" @click="startSlideshow" v-if="mediaList.length > 0">▶ 播放</el-button>
      </div>
    </header>

    <div v-if="slideshowVisible && ssMedia" class="slideshow-overlay" @click.self="closeSlideshow">
      <button class="ss-close" @click="closeSlideshow">×</button>
      <button class="ss-prev" @click="ssPrev">‹</button>
      <div class="ss-stage">
        <img
          v-if="ssMedia.mediaType === 'image'"
          :src="getMediaUrl(ssMedia.filePath)"
          :alt="ssMedia.title"
        />
        <video
          v-else
          :src="getMediaUrl(ssMedia.filePath)"
          controls
          autoplay
        ></video>
        <div class="ss-caption">
          {{ ssMedia.title || ssMedia.filename }} · {{ ssIndex + 1 }}/{{ filteredMediaList.length }}
        </div>
      </div>
      <button class="ss-next" @click="ssNext">›</button>
      <div class="ss-controls">
        <button @click="togglePlay">{{ playing ? '⏸ 暂停' : '▶ 播放' }}</button>
      </div>
    </div>

    <div class="container">
      <div v-if="selectedIds.length > 0" class="batch-actions">
        <span class="selected-count">已选择 {{ selectedIds.length }} 项</span>
        <el-button type="primary" @click="handleBatchMove">移动到</el-button>
        <el-button type="danger" @click="handleBatchDelete">删除</el-button>
        <el-button type="primary" @click="showEditDialog">编辑</el-button>
        <el-button type="success" @click="batchDownload">下载</el-button>
        <el-button @click="clearSelection">取消</el-button>
      </div>

      <div class="filters" v-else>
        <el-radio-group v-model="activeType" @change="handleTypeChange">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="image">图片</el-radio-button>
          <el-radio-button label="video">视频</el-radio-button>
        </el-radio-group>
      </div>

      <div v-loading="loading" class="media-grid">
        <div
          v-for="media in filteredMediaList"
          :key="media.id"
          class="media-item"
          :class="{ selected: selectedIds.includes(media.id) }"
          @click="handleItemClick(media.id)"
          @click.right.prevent="toggleSelection(media.id)"
        >
          <div class="media-checkbox" @click.stop="toggleSelection(media.id)">
            <el-checkbox v-model="selectedIds" :value="media.id" />
          </div>
          <div class="media-thumbnail">
            <img
              v-if="media.mediaType === 'image'"
              :src="getMediaUrl(media.filePath)"
              :alt="media.title"
              loading="lazy"
            />
            <div v-else class="video-thumbnail">
              <img :src="getMediaUrl(media.coverImage) || '/placeholder-video.png'" :alt="media.title" loading="lazy" />
              <div class="play-overlay">
                <div class="play-btn">▶</div>
              </div>
            </div>
            <div class="media-type-badge">
              {{ media.mediaType === 'image' ? '🖼️' : '🎬' }}
            </div>
          </div>
          <div class="media-info">
            <div class="media-title">{{ media.title || media.filename }}</div>
            <div class="media-meta">
              <span>{{ formatSize(media.fileSize) }}</span>
              <span>{{ formatDate(media.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <el-dialog v-model="moveDialogVisible" title="移动到相册" width="400px">
        <el-select v-model="targetAlbumId" placeholder="选择目标相册" style="width: 100%">
          <el-option
            v-for="album in otherAlbums"
            :key="album.id"
            :label="album.name"
            :value="album.id"
          />
        </el-select>
        <template #footer>
          <el-button @click="moveDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmMove" :disabled="!targetAlbumId">确认移动</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="editDialogVisible" title="批量编辑" width="400px">
        <el-form label-width="70px">
          <el-form-item label="标题">
            <el-input v-model="editForm.title" placeholder="留空则不修改" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editForm.description" type="textarea" :rows="2" placeholder="留空则不修改" />
          </el-form-item>
          <el-form-item label="标签">
            <el-select v-model="editForm.tagIds" multiple collapse-tags placeholder="选择标签" style="width: 100%">
              <el-option v-for="t in allTags" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleBatchEdit">保存</el-button>
        </template>
      </el-dialog>

      <div v-if="!loading && filteredMediaList.length === 0" class="empty-state">
        <div class="empty-icon">📭</div>
        <p>还没有作品</p>
        <p class="empty-hint">上传一些内容到这个相册吧~</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMediaStore } from '../stores/media'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api/index'

const route = useRoute()
const router = useRouter()
const mediaStore = useMediaStore()

const albumName = ref('')
const activeType = ref('all')
const selectedIds = ref([])
const moveDialogVisible = ref(false)
const targetAlbumId = ref(null)
const editDialogVisible = ref(false)
const editForm = ref({ title: '', description: '', tagIds: [] })
const allTags = ref([])
const slideshowVisible = ref(false)
const ssIndex = ref(0)
const playing = ref(false)
let ssTimer = null

const mediaList = computed(() => mediaStore.mediaList)
const loading = computed(() => mediaStore.loading)

const otherAlbums = computed(() => {
  return mediaStore.albums.filter(a => a.id !== parseInt(route.params.id))
})

const filteredMediaList = computed(() => {
  if (activeType.value === 'all') return mediaList.value
  return mediaList.value.filter(m => m.mediaType === activeType.value)
})

const ssMedia = computed(() => filteredMediaList.value[ssIndex.value] || null)

const fetchAlbumData = async () => {
  const albumId = route.params.id
  await mediaStore.fetchMediaList(null, null, albumId)
  const album = mediaStore.albums.find(a => a.id === parseInt(albumId))
  if (album) {
    albumName.value = album.name
  } else {
    albumName.value = '相册'
  }
}

const handleTypeChange = () => {
  fetchAlbumData()
}

const handleItemClick = (id) => {
  if (selectedIds.value.length > 0) {
    toggleSelection(id)
  } else {
    viewMedia(id)
  }
}

const toggleSelection = (id) => {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const clearSelection = () => {
  selectedIds.value = []
}

const showEditDialog = () => {
  editForm.value = { title: '', description: '', tagIds: [] }
  editDialogVisible.value = true
}

const handleBatchEdit = async () => {
  try {
    const payload = { mediaIds: selectedIds.value }
    if (editForm.value.title) payload.title = editForm.value.title
    if (editForm.value.description) payload.description = editForm.value.description
    if (editForm.value.tagIds.length) payload.tagIds = editForm.value.tagIds
    await api.post('/media/batch/update', payload)
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    selectedIds.value = []
    await fetchAlbumData()
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const batchDownload = async () => {
  try {
    const response = await api.post('/media/download', { mediaIds: selectedIds.value }, { responseType: 'blob' })
    const url = URL.createObjectURL(response.data)
    const a = document.createElement('a')
    a.href = url
    a.download = 'media.zip'
    a.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const startSlideshow = () => {
  ssIndex.value = 0
  slideshowVisible.value = true
  playing.value = true
  ssAuto()
}

const ssAuto = () => {
  clearInterval(ssTimer)
  if (playing.value && filteredMediaList.value.length > 1) {
    ssTimer = setInterval(() => {
      ssIndex.value = (ssIndex.value + 1) % filteredMediaList.value.length
    }, 3000)
  }
}

const ssNext = () => {
  ssIndex.value = (ssIndex.value + 1) % filteredMediaList.value.length
}

const ssPrev = () => {
  ssIndex.value = (ssIndex.value - 1 + filteredMediaList.value.length) % filteredMediaList.value.length
}

const togglePlay = () => {
  playing.value = !playing.value
  ssAuto()
}

const closeSlideshow = () => {
  clearInterval(ssTimer)
  slideshowVisible.value = false
}

const handleBatchMove = () => {
  targetAlbumId.value = null
  moveDialogVisible.value = true
}

const confirmMove = async () => {
  if (!targetAlbumId.value) return
  try {
    await mediaStore.batchMoveMedia(selectedIds.value, targetAlbumId.value)
    ElMessage.success('移动成功')
    moveDialogVisible.value = false
    selectedIds.value = []
    await fetchAlbumData()
  } catch (error) {
    ElMessage.error('移动失败')
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要将 ${selectedIds.value.length} 个文件移入回收站吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await mediaStore.batchDeleteMedia(selectedIds.value)
    ElMessage.success('已移入回收站')
    selectedIds.value = []
    await fetchAlbumData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const viewMedia = (id) => {
  router.push({
    name: 'MediaDetail',
    params: { id },
    query: { from: 'album', albumId: route.params.id }
  })
}

const goBack = () => {
  router.push({ name: 'Home' })
}

const getMediaUrl = (filePath) => {
  if (!filePath) return ''
  if (filePath.startsWith('http')) return filePath
  return 'http://47.97.191.168' + (filePath.startsWith('/') ? '' : '/') + filePath
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  mediaStore.fetchAlbums().then(() => {
    fetchAlbumData()
  })
  api.get('/tags').then(r => { allTags.value = r.data }).catch(() => {})
})

onUnmounted(() => {
  clearInterval(ssTimer)
})
</script>

<style lang="scss" scoped>
.album-view {
  min-height: 100vh;
  background: transparent;
  position: relative;
}

.header {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2rem 2.5rem;
  background: var(--glass);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--glass-border);

  .header-content {
    h1 {
      font-size: 2rem;
      font-weight: 700;
      background: linear-gradient(135deg, #fff 0%, #e3f2fd 50%, #bbdefb 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin-bottom: 0.25rem;
    }

    p {
      font-size: 0.9rem;
      color: rgba(255, 255, 255, 0.7);
    }
  }

  :deep(.el-button) {
    background: rgba(79, 195, 247, 0.12);
    border: 1px solid rgba(79, 195, 247, 0.5);
    color: #e0f7ff;
    font-weight: 600;
    backdrop-filter: blur(5px);

    &:hover {
      background: rgba(79, 195, 247, 0.25);
      border-color: #4fc3f7;
      color: #fff;
    }
  }
}

.container {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 1.5rem 2rem 3rem;
}

.filters {
  margin-bottom: 1.5rem;

  :deep(.el-radio-button__inner) {
    background: var(--glass);
    border: 1px solid var(--glass-border);
    color: var(--text-secondary);

    &:hover {
      color: #4fc3f7;
      border-color: rgba(79, 195, 247, 0.5);
    }
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background: linear-gradient(135deg, #81d4fa 0%, #4fc3f7 100%);
    border-color: #4fc3f7;
    color: white;
    box-shadow: 0 2px 8px rgba(79, 195, 247, 0.3);
  }
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 1.5rem;
}

.media-item {
  background: var(--glass);
  backdrop-filter: blur(10px);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;

  &:hover {
    border-color: #81d4fa;
    background: var(--glass-strong);
    transform: translateY(-4px);
    box-shadow: var(--shadow-glow);
  }

  &.selected {
    border-color: #29b6f6;
    background: rgba(41, 182, 246, 0.1);
    box-shadow: 0 4px 15px rgba(41, 182, 246, 0.3);
  }
}

.media-checkbox {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.2s;
}

.media-item:hover .media-checkbox,
.media-item.selected .media-checkbox {
  opacity: 1;
}

.media-thumbnail {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
  z-index: 1;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;
  }

  &:hover img {
    transform: scale(1.05);
  }
}

.video-thumbnail {
  position: relative;
  width: 100%;
  height: 100%;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .play-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;

    .play-btn {
      width: 50px;
      height: 50px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.9);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
      color: #29b6f6;
    }
  }
}

.media-type-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 1.2rem;
}

.media-info {
  padding: 1rem;

  .media-title {
    font-size: 0.95rem;
    color: var(--text-primary);
    font-weight: 600;
    margin-bottom: 0.5rem;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .media-meta {
    display: flex;
    justify-content: space-between;
    font-size: 0.8rem;
    color: var(--text-secondary);
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
    color: white;
    font-size: 1.1rem;
    margin-bottom: 0.5rem;
  }

  .empty-hint {
    font-size: 0.9rem;
    color: rgba(255, 255, 255, 0.7);
  }
}

.slideshow-overlay {
  position: fixed;
  inset: 0;
  background: rgba(4, 7, 12, 0.96);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ss-stage {
  max-width: 86vw;
  max-height: 82vh;
  display: flex;
  flex-direction: column;
  align-items: center;

  img,
  video {
    max-width: 86vw;
    max-height: 76vh;
    object-fit: contain;
    border-radius: 12px;
    box-shadow: 0 20px 80px rgba(0, 0, 0, 0.6);
  }
}

.ss-caption {
  margin-top: 0.75rem;
  color: rgba(255, 255, 255, 0.75);
  font-size: 0.95rem;
}

.ss-close {
  position: absolute;
  top: 18px;
  right: 24px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: rgba(255, 255, 255, 0.08);
  color: white;
  font-size: 1.6rem;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
  }
}

.ss-prev,
.ss-next {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: rgba(255, 255, 255, 0.08);
  color: white;
  font-size: 2rem;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba(79, 195, 247, 0.3);
  }
}

.ss-prev {
  left: 24px;
}

.ss-next {
  right: 24px;
}

.ss-controls {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);

  button {
    padding: 0.5rem 1.2rem;
    border-radius: 24px;
    border: 1px solid rgba(255, 255, 255, 0.3);
    background: rgba(255, 255, 255, 0.1);
    color: white;
    cursor: pointer;
    font-size: 0.9rem;

    &:hover {
      background: rgba(79, 195, 247, 0.3);
    }
  }
}

@media (max-width: 768px) {
  .header {
    padding: 1.25rem 1rem;
    flex-direction: column;
    gap: 1rem;

    .header-content {
      text-align: center;

      h1 {
        font-size: 1.5rem;
      }

      p {
        font-size: 0.8rem;
      }
    }
  }

  .container {
    padding: 1rem 1rem 2rem;
  }

  .media-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 1rem;
  }

  .media-item {
    border-radius: 12px;
  }

  .media-info {
    padding: 0.75rem;

    .media-title {
      font-size: 0.85rem;
    }
  }
}
</style>
