<template>
  <div class="album-view">
    <header class="header">
      <div class="header-content">
        <h1>{{ albumName }}</h1>
        <p>🎨 {{ mediaList.length }} 个作品</p>
      </div>
      <div class="header-actions">
        <el-button round @click="goBack">← 返回</el-button>
        <template v-if="!sortMode">
          <el-button round :type="coverMode ? 'warning' : 'default'" @click="enterCoverMode" v-if="mediaList.length > 0">
            <el-icon style="margin-right: 4px"><Picture /></el-icon>{{ coverMode ? '退出封面选择' : '封面' }}
          </el-button>
          <el-button round @click="enterSortMode" v-if="mediaList.length > 1">
            <el-icon style="margin-right: 4px"><Sort /></el-icon>排序
          </el-button>
          <el-button round type="primary" @click="startSlideshow" v-if="mediaList.length > 0">
            <el-icon style="margin-right: 4px"><VideoPlay /></el-icon>播放
          </el-button>
        </template>
        <template v-else>
          <el-button round @click="cancelSort">取消</el-button>
          <el-button round type="success" @click="saveOrder">保存排序</el-button>
        </template>
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
          :src="videoSrc(ssMedia)"
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
      <div v-if="coverMode" class="cover-mode-bar">
        <span>🖼 点击一张图片，将它设为「{{ albumName }}」的封面（视频需转码完成后才可选择）</span>
        <el-button size="small" @click="coverMode = false">取消</el-button>
      </div>

      <div v-if="selectedIds.length > 0" class="batch-actions">
        <span class="selected-count">已选择 {{ selectedIds.length }} 项</span>
        <el-button type="primary" @click="handleBatchMove">移动到</el-button>
        <el-button type="danger" @click="handleBatchDelete">删除</el-button>
        <el-button type="primary" @click="showEditDialog">编辑</el-button>
        <el-button type="success" @click="batchDownload">下载</el-button>
        <el-button type="warning" :disabled="selectedIds.length !== 1" @click="setCoverFromSelection">
          设为封面
        </el-button>
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
          v-for="(media, index) in (sortMode ? sortItems : filteredMediaList)"
          :key="media.id"
          class="media-item"
          :class="{ selected: selectedIds.includes(media.id), 'cover-mode': coverMode }"
          @click="coverMode ? chooseCover(media) : (sortMode ? null : handleItemClick(media.id, index))"
          @click.right.prevent="toggleSelection(media.id)"
        >
          <span v-if="isCurrentCover(media)" class="current-cover-badge">当前封面</span>
          <div v-if="sortMode" class="sort-controls">
            <button @click.stop="moveUp(index)" :disabled="index === 0">↑</button>
            <button @click.stop="moveDown(index)" :disabled="index === (sortMode ? sortItems.length : 0) - 1">↓</button>
          </div>
          <div class="media-checkbox" @click.stop>
            <el-checkbox
              :model-value="selectedIds.includes(media.id)"
              @change="toggleSelection(media.id)"
            />
          </div>
          <div class="media-thumbnail">
            <img
              v-if="media.mediaType === 'image'"
              :src="getMediaUrl(media.filePath)"
              :alt="media.title"
              loading="lazy"
              v-img-fade
            />
            <div v-else class="video-thumbnail">
              <img v-if="media.coverImage" :src="getMediaUrl(media.coverImage)" :alt="media.title" loading="lazy" v-img-fade />
              <video v-else :src="videoSrc(media) + '#t=1'" preload="metadata" muted playsinline></video>
              <div class="play-overlay">
                <div class="play-btn">▶</div>
              </div>
              <span v-if="durationText(media)" class="video-duration">{{ durationText(media) }}</span>
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

      <div v-if="hasMore && !loadingMore" ref="sentinel" class="scroll-sentinel">加载中…</div>

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

      <MediaLightbox
        v-model:visible="lightboxVisible"
        v-model:index="lightboxIndex"
        :items="filteredMediaList"
      />

      <EmptyState
        v-if="!loading && filteredMediaList.length === 0"
        icon="📭"
        title="还没有作品"
        hint="上传一些内容到这个相册吧~"
        cta-text="去上传"
        @cta="router.push('/upload')"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMediaStore } from '../stores/media'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api/index'
import { resolveMediaUrl, videoPlaySrc, formatDuration } from '@/utils/media'
import { Picture, Sort, VideoPlay } from '@element-plus/icons-vue'
import MediaLightbox from '../components/MediaLightbox.vue'
import EmptyState from '@/components/EmptyState.vue'

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
const lightboxVisible = ref(false)
const lightboxIndex = ref(0)
const sortMode = ref(false)
const sortItems = ref([])
const coverMode = ref(false)

// 相册内媒体的本地分页累积（不再一次性拉全库）
const mediaList = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const currentPage = ref(1)
const pageSize = ref(24)
const totalMedia = ref(0)
const sentinel = ref(null)
let observer = null

const currentAlbum = computed(() =>
  mediaStore.albums.find(a => a.id === parseInt(route.params.id)) || null
)

const isCurrentCover = (media) =>
  !!currentAlbum.value?.coverImage && media.coverImage === currentAlbum.value.coverImage

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
  // 相册名/描述从相册列表取（列表本身很小）
  await mediaStore.fetchAlbums()
  const album = mediaStore.albums.find(a => a.id === parseInt(albumId))
  albumName.value = album ? album.name : '相册'
}

/** 分页拉取相册内媒体（后端按 sort_order 保持手动排序） */
const fetchPage = async (append = false) => {
  if (append) loadingMore.value = true
  try {
    const response = await api.get(`/media`, {
      params: { albumId: route.params.id, page: currentPage.value, size: pageSize.value }
    })
    const data = response.data
    const list = Array.isArray(data) ? data : (data.list || [])
    const total = Array.isArray(data) ? list.length : (data.total || 0)
    mediaList.value = append ? [...mediaList.value, ...list] : list
    totalMedia.value = total
  } catch (error) {
    console.error('获取相册媒体失败', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const hasMore = computed(() => mediaList.value.length < totalMedia.value)

const loadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  currentPage.value += 1
  fetchPage(true)
}

const handleTypeChange = () => {
  // 类型筛选是前端过滤，不需要重新请求
}

const handleItemClick = (id, index) => {
  if (selectedIds.value.length > 0) {
    toggleSelection(id)
  } else {
    lightboxIndex.value = index
    lightboxVisible.value = true
  }
}

const enterCoverMode = () => {
  coverMode.value = true
  ElMessage.info('点击一张图片，将它设为相册封面')
}

const chooseCover = async (media) => {
  if (!media.coverImage) {
    ElMessage.warning('该媒体暂无可用封面（视频转码完成后才能选择），请选择图片')
    return
  }
  try {
    await ElMessageBox.confirm(
      `将「${media.title || media.filename}」设为相册「${albumName.value}」的封面？`,
      '设置封面',
      { confirmButtonText: '设为封面', cancelButtonText: '取消', type: 'info' }
    )
  } catch {
    return
  }
  try {
    await api.put(`/albums/${route.params.id}`, {
      name: albumName.value,
      description: currentAlbum.value?.description || '',
      coverImage: media.coverImage
    })
    await mediaStore.fetchAlbums()
    ElMessage.success('封面已更新 ✨')
    coverMode.value = false
  } catch (error) {
    ElMessage.error('封面设置失败')
  }
}

const setCoverFromSelection = async () => {
  if (selectedIds.value.length !== 1) return
  const media = mediaList.value.find(m => m.id === selectedIds.value[0])
  if (!media) return
  await chooseCover(media)
  clearSelection()
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

const enterSortMode = async () => {
  // 手动排序必须基于完整有序列表：还有未加载的分页时先补齐
  if (hasMore.value) {
    ElMessage.info('正在加载全部媒体以进行排序…')
    while (hasMore.value) {
      await loadMore()
    }
  }
  sortItems.value = [...filteredMediaList.value]
  sortMode.value = true
  selectedIds.value = []
}

const cancelSort = () => {
  sortMode.value = false
  sortItems.value = []
}

const moveUp = (index) => {
  if (index <= 0) return
  const arr = sortItems.value
  ;[arr[index - 1], arr[index]] = [arr[index], arr[index - 1]]
  sortItems.value = [...arr]
}

const moveDown = (index) => {
  if (index >= sortItems.value.length - 1) return
  const arr = sortItems.value
  ;[arr[index + 1], arr[index]] = [arr[index], arr[index + 1]]
  sortItems.value = [...arr]
}

const saveOrder = async () => {
  try {
    await api.put(`/albums/${route.params.id}/order`, {
      mediaIds: sortItems.value.map(m => m.id)
    })
    ElMessage.success('排序已保存')
    sortMode.value = false
    sortItems.value = []
    await fetchAlbumData()
  } catch (error) {
    ElMessage.error('保存失败')
  }
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

const getMediaUrl = (filePath) => resolveMediaUrl(filePath)

const videoSrc = (item) => videoPlaySrc(item)

const durationText = (item) => formatDuration(item.duration)

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
  fetchPage()
  api.get('/tags').then(r => { allTags.value = r.data }).catch(() => {})
  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting) loadMore()
  }, { rootMargin: '300px' })
  watch(hasMore, (more) => {
    if (more) nextTick(() => {
      if (sentinel.value && observer) observer.observe(sentinel.value)
    })
  })
})

onUnmounted(() => {
  clearInterval(ssTimer)
  if (observer) observer.disconnect()
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

.cover-mode-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.7rem 1rem;
  margin-bottom: 0.75rem;
  border-radius: 12px;
  background: rgba(255, 193, 7, 0.12);
  border: 1px solid rgba(255, 193, 7, 0.4);
  color: var(--text-primary);
  font-size: 0.9rem;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 0.45rem;
}

.media-item {
  aspect-ratio: 1;
  background: var(--glass);
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;

  &.cover-mode {
    cursor: crosshair;

    &:hover {
      outline: 2px solid #ffc107;
      outline-offset: -2px;
    }
  }

  .current-cover-badge {
    position: absolute;
    top: 8px;
    left: 8px;
    z-index: 3;
    background: linear-gradient(135deg, #ffb74d 0%, #ffa726 100%);
    color: #3e2723;
    font-size: 0.7rem;
    font-weight: 700;
    padding: 2px 8px;
    border-radius: 10px;
    pointer-events: none;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  }
  position: relative;

  &:hover {
    transform: scale(1.02);
  }

  &.selected {
    outline: 2px solid #4fc3f7;
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

.sort-controls {
  position: absolute;
  right: 8px;
  bottom: 8px;
  z-index: 12;
  display: flex;
  flex-direction: column;
  gap: 4px;

  button {
    width: 28px;
    height: 28px;
    border: none;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.55);
    color: white;
    font-size: 0.9rem;
    cursor: pointer;
    backdrop-filter: blur(4px);
    transition: all 0.15s;

    &:hover:not(:disabled) {
      background: rgba(79, 195, 247, 0.7);
    }

    &:disabled {
      opacity: 0.35;
      cursor: not-allowed;
    }
  }
}

.media-item:hover .media-checkbox,
.media-item.selected .media-checkbox {
  opacity: 1;
}

@media (max-width: 768px) {
  .media-checkbox {
    opacity: 1;
  }
}

.media-thumbnail {
  position: relative;
  height: 100%;
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

  img,
  video {
    width: 100%;
    height: 100%;
    object-fit: cover;
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
    z-index: 2;
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

/* 手机竖屏：3 列紧凑网格（隐藏文字信息，接近 IG 观感） */
@media (max-width: 600px) {
  .media-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 2px;
  }

  .media-item {
    border-radius: 4px;

    .media-info {
      display: none;
    }

    .media-checkbox {
      transform: scale(0.85);
      transform-origin: top left;
    }
  }

  .cover-mode-bar {
    font-size: 0.8rem;
    padding: 0.55rem 0.7rem;
  }
}
</style>