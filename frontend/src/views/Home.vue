<template>
  <div class="home">
    <header class="hero-header">
      <div class="hero-content">
        <p class="hero-eyebrow">MY MEDIA GALLERY</p>
        <h1 class="hero-title">SEE</h1>
        <p class="hero-subtitle">记录美好瞬间</p>
      </div>
      <el-button type="primary" round size="large" @click="showCreateAlbumDialog" class="hero-action">
        <span class="btn-icon">➕</span> 新建
      </el-button>
    </header>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-radio-group v-model="viewMode" size="large" class="view-mode-group">
          <el-radio-button label="albums">
            <span class="radio-label">📁</span> 相册
          </el-radio-button>
          <el-radio-button label="all">
            <span class="radio-label">📋</span> 全部
          </el-radio-button>
          <el-radio-button label="images">
            <span class="radio-label">🖼️</span> 图片
          </el-radio-button>
          <el-radio-button label="videos">
            <span class="radio-label">🎬</span> 视频
          </el-radio-button>
        </el-radio-group>
      </div>
      <div class="toolbar-right">
        <div class="search-box">
          <div class="view-layout-toggle">
            <el-radio-group v-model="viewLayout" size="large">
              <el-radio-button label="grid">▦</el-radio-button>
              <el-radio-button label="list">☰</el-radio-button>
            </el-radio-group>
          </div>
          <el-select
            v-model="selectedTagId"
            placeholder="标签筛选"
            clearable
            size="large"
            class="tag-filter"
            @change="handleTagChange"
          >
            <el-option
              v-for="tag in tags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索标题/描述..."
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <span class="search-icon">🔍</span>
            </template>
          </el-input>
          <el-button type="primary" class="search-btn" @click="handleSearch">
            搜索
          </el-button>
        </div>
      </div>
    </div>

    <div v-if="stats" class="stats-bar">
      <span>总计: {{ animatedStats?.totalMedia ?? stats.totalMedia }} 个</span>
      <span>📷 {{ stats.imageCount }}</span>
      <span>🎬 {{ stats.videoCount }}</span>
      <span>❤️ {{ stats.favoriteCount }} 收藏</span>
      <span>📦 {{ formatSize(stats.totalSize) }}</span>
    </div>

    <div class="container">
      <div v-if="viewMode === 'albums'">
        <div v-loading="loading" class="albums-grid">
          <div
            v-for="(album, index) in albums"
            :key="album.id"
            class="album-item"
            :class="[`album-item--color-${index % 5}`, { 'has-cover': album.coverImage }]"
            @click="viewAlbum(album.id)"
          >
            <div v-if="album.coverImage" class="album-cover">
              <img :src="getCoverUrl(album)" :alt="album.name" loading="lazy" />
            </div>
            <div v-else class="album-icon">📁</div>
            <div class="album-name">{{ album.name }}</div>
            <div class="album-count">🎨 {{ album.mediaCount || 0 }} 个作品</div>
            <el-button
              v-if="album.name !== '默认相册'"
              type="danger"
              :icon="Delete"
              circle
              size="small"
              class="delete-btn"
              @click.stop="handleDeleteAlbum(album.id)"
            />
          </div>
        </div>
      </div>

      <div v-else>
        <div class="batch-actions" v-if="selectedMedia.length > 0">
          <span>已选择 {{ selectedMedia.length }} 项</span>
          <el-button size="small" @click="clearSelection">取消选择</el-button>
          <el-button size="small" type="primary" @click="showMoveDialog">移动到</el-button>
          <el-button size="small" @click="showEditDialog">编辑</el-button>
          <el-button size="small" type="success" @click="batchDownload">下载</el-button>
          <el-button size="small" type="danger" @click="batchDelete">移入回收站</el-button>
        </div>
        <div v-if="loading" class="media-grid skeleton-grid">
          <div v-for="n in 8" :key="n" class="media-item skeleton-item">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="image" style="width: 100%; height: 180px" />
                <el-skeleton-item variant="text" style="width: 60%; margin-top: 8px" />
              </template>
            </el-skeleton>
          </div>
        </div>
        <div v-else v-loading="loading" class="media-grid" :class="{ 'list-mode': viewLayout === 'list' }">
          <div
            v-for="item in mediaList"
            :key="item.id"
            class="media-item"
            :class="{ selected: selectedMedia.includes(item.id) }"
            @click="goToDetail(item.id)"
            @click.meta="toggleSelect(item.id, $event)"
          >
            <input
              type="checkbox"
              :checked="selectedMedia.includes(item.id)"
              @click.stop
              @change="toggleSelect(item.id)"
              class="media-checkbox"
            />
            <img v-if="item.mediaType === 'image'" :src="getCoverUrl(item)" :alt="item.title" loading="lazy" />
            <video v-else :src="getCoverUrl(item)" />
            <div class="media-overlay">
              <span class="media-title">{{ item.title || item.filename }}</span>
            </div>
            <button
              class="favorite-btn"
              :class="{ active: item.isFavorite }"
              @click.stop="toggleFavorite(item)"
            >
              {{ item.isFavorite ? '♥' : '♡' }}
            </button>
          </div>
        </div>
        <div v-if="!loading && mediaList.length === 0" class="empty-state">
          <div class="empty-icon">🔍</div>
          <p>没有找到内容</p>
        </div>
        <div v-if="viewMode !== 'albums' && totalMedia > pageSize" class="pagination">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="totalMedia"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>

    <el-dialog v-model="createAlbumDialogVisible" title="创建新相册" width="360px" round align-center>
      <el-form @submit.prevent="handleCreateAlbum">
        <el-form-item>
          <el-input
            v-model="newAlbumName"
            placeholder="给相册起个名字~"
            autofocus
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="newAlbumDesc"
            type="textarea"
            placeholder="相册描述（选填）"
            rows="2"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createAlbumDialogVisible = false">算了</el-button>
        <el-button type="primary" round @click="handleCreateAlbum">创建！</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="moveDialogVisible" title="移动到相册" width="360px">
      <el-select v-model="targetAlbumId" placeholder="选择目标相册" style="width: 100%">
        <el-option
          v-for="album in albums"
          :key="album.id"
          :label="album.name"
          :value="album.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="moveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleMove">移动</el-button>
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
            <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMediaStore } from '../stores/media'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import api from '@/api/index'

const router = useRouter()
const route = useRoute()
const mediaStore = useMediaStore()

const albums = computed(() => mediaStore.albums)
const loading = computed(() => mediaStore.loading)

const viewMode = ref('albums')
const searchKeyword = ref('')
const mediaList = ref([])
const currentPage = ref(1)
const pageSize = ref(24)
const totalMedia = ref(0)
const selectedTagId = ref(null)
const viewLayout = ref('grid')
const stats = ref(null)
const animatedStats = ref(null)
const selectedMedia = ref([])
const moveDialogVisible = ref(false)
const targetAlbumId = ref(null)
const editDialogVisible = ref(false)
const editForm = ref({ title: '', description: '', tagIds: [] })
const tags = computed(() => mediaStore.tags)
let searchDebounce = null

watch(searchKeyword, () => {
  clearTimeout(searchDebounce)
  searchDebounce = setTimeout(() => {
    if (viewMode.value !== 'albums') {
      currentPage.value = 1
      fetchMediaList()
    }
  }, 400)
})

const animateNumber = (from, to, duration, onUpdate) => {
  const start = performance.now()
  const tick = (now) => {
    const progress = Math.min((now - start) / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    onUpdate(Math.round(from + (to - from) * eased))
    if (progress < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}

const createAlbumDialogVisible = ref(false)
const newAlbumName = ref('')
const newAlbumDesc = ref('')

const fetchAlbums = async () => {
  await mediaStore.fetchAlbums()
}

const viewAlbum = (albumId) => {
  router.push({ name: 'Album', params: { id: albumId } })
}

const showCreateAlbumDialog = () => {
  newAlbumName.value = ''
  newAlbumDesc.value = ''
  createAlbumDialogVisible.value = true
}

const handleCreateAlbum = async () => {
  if (!newAlbumName.value.trim()) {
    ElMessage.warning('请输入相册名称')
    return
  }
  try {
    await mediaStore.createAlbum({
      name: newAlbumName.value,
      description: newAlbumDesc.value
    })
    ElMessage.success('创建成功 ✨')
    createAlbumDialogVisible.value = false
    fetchAlbums()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const handleDeleteAlbum = async (albumId) => {
  try {
    await ElMessageBox.confirm('确定删除这个相册吗？', '提示', {
      confirmButtonText: '删掉',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await mediaStore.deleteAlbum(albumId)
    ElMessage.success('已删除 🗑️')
    fetchAlbums()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const fetchStats = async () => {
  try {
    const response = await api.get('/media/stats')
    stats.value = response.data
    if (stats.value.totalMedia != null) {
      animateNumber(0, stats.value.totalMedia, 900, (value) => {
        if (!animatedStats.value) animatedStats.value = {}
        animatedStats.value.totalMedia = value
      })
    }
  } catch (error) {
    console.error('获取统计失败', error)
  }
}

const fetchMediaList = async () => {
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (viewMode.value === 'images') params.type = 'image'
    if (viewMode.value === 'videos') params.type = 'video'
    if (selectedTagId.value) params.tagId = selectedTagId.value
    const response = await api.get('/media', { params })
    const data = response.data
    if (Array.isArray(data)) {
      mediaList.value = data
      totalMedia.value = data.length
    } else {
      mediaList.value = data.list || []
      totalMedia.value = data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取媒体列表失败')
  }
}

const handleSearch = () => {
  clearTimeout(searchDebounce)
  currentPage.value = 1
  viewMode.value = 'all'
}

const handleTagChange = () => {
  currentPage.value = 1
  fetchMediaList()
}

const handlePageChange = (p) => {
  currentPage.value = p
  fetchMediaList()
}

const getCoverUrl = (item) => {
  if (!item.coverImage) return ''
  if (item.coverImage.startsWith('http')) return item.coverImage
  return 'http://47.97.191.168' + (item.coverImage.startsWith('/') ? '' : '/') + item.coverImage
}

const goToDetail = (id) => {
  router.push({
    path: `/media/${id}`,
    query: {
      from: 'home',
      viewMode: viewMode.value,
      keyword: searchKeyword.value
    }
  })
}

watch(viewMode, (mode) => {
  currentPage.value = 1
  if (mode !== 'albums') {
    fetchMediaList()
  }
})

const toggleFavorite = async (item) => {
  try {
    await api.post(`/media/${item.id}/favorite`)
    fetchMediaList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const toggleSelect = (id, event) => {
  if (event && event.metaKey) {
    if (selectedMedia.value.includes(id)) {
      selectedMedia.value = selectedMedia.value.filter(i => i !== id)
    } else {
      selectedMedia.value.push(id)
    }
  } else {
    if (selectedMedia.value.includes(id)) {
      selectedMedia.value = selectedMedia.value.filter(i => i !== id)
    } else {
      selectedMedia.value.push(id)
    }
  }
}

const clearSelection = () => {
  selectedMedia.value = []
}

const showEditDialog = () => {
  editForm.value = { title: '', description: '', tagIds: [] }
  editDialogVisible.value = true
}

const handleBatchEdit = async () => {
  try {
    const payload = { mediaIds: selectedMedia.value }
    if (editForm.value.title) payload.title = editForm.value.title
    if (editForm.value.description) payload.description = editForm.value.description
    if (editForm.value.tagIds.length) payload.tagIds = editForm.value.tagIds
    await api.post('/media/batch/update', payload)
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    selectedMedia.value = []
    fetchMediaList()
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const batchDownload = async () => {
  try {
    const response = await api.post('/media/download', { mediaIds: selectedMedia.value }, { responseType: 'blob' })
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

const showMoveDialog = () => {
  targetAlbumId.value = null
  moveDialogVisible.value = true
}

const handleMove = async () => {
  if (!targetAlbumId.value) {
    ElMessage.warning('请选择目标相册')
    return
  }
  try {
    await api.post('/media/batch/move', {
      mediaIds: selectedMedia.value,
      albumId: targetAlbumId.value
    })
    ElMessage.success('移动成功')
    moveDialogVisible.value = false
    selectedMedia.value = []
    fetchMediaList()
  } catch (error) {
    ElMessage.error('移动失败')
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定将 ${selectedMedia.value.length} 项移入回收站吗？`, '提示', { type: 'warning' })
    await api.post('/media/batch/delete', { mediaIds: selectedMedia.value })
    ElMessage.success('已移入回收站')
    selectedMedia.value = []
    fetchMediaList()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

onMounted(() => {
  const q = route.query
  if (q.viewMode) viewMode.value = q.viewMode
  if (q.keyword) searchKeyword.value = q.keyword
  fetchAlbums()
  fetchStats()
  mediaStore.fetchTags()
  if (viewMode.value !== 'albums') {
    fetchMediaList()
  }
})
</script>

<style lang="scss" scoped>
.home {
  min-height: 100vh;
  background: transparent;
  position: relative;
}

.hero-header {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 5rem 2rem 3.5rem;
  background: radial-gradient(700px 320px at 50% 0%, rgba(79, 195, 247, 0.14), transparent 70%);

  .hero-eyebrow {
    font-size: 0.8rem;
    letter-spacing: 6px;
    color: rgba(255, 255, 255, 0.45);
    text-transform: uppercase;
    margin-bottom: 0.75rem;
  }

  .hero-title {
    margin: 0;
    font-size: 4.5rem;
    font-weight: 800;
    letter-spacing: 4px;
    line-height: 1.1;
    background: linear-gradient(135deg, #fff 0%, #b3e5fc 45%, #4fc3f7 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    text-shadow: 0 20px 60px rgba(79, 195, 247, 0.2);
  }

  .hero-subtitle {
    font-size: 1.1rem;
    color: rgba(255, 255, 255, 0.6);
    letter-spacing: 3px;
    margin-top: 0.75rem;
  }

  .hero-action {
    margin-top: 2rem;
    background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
    border: none;
    color: #06222e;
    font-weight: 600;
    box-shadow: 0 8px 28px rgba(79, 195, 247, 0.35);
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 12px 36px rgba(79, 195, 247, 0.45);
    }
  }
}

.container {
  position: relative;
  z-index: 1;
  max-width: 900px;
  margin: 0 auto;
  padding: 1rem 2rem 3rem;
  background: var(--glass);
  backdrop-filter: blur(15px);
  border-radius: 16px;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--glass-border);
}

.albums-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 1.5rem;
}

.album-item {
  position: relative;
  padding: 1.75rem 1.25rem;
  background: var(--glass);
  backdrop-filter: blur(12px);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;

  &.has-cover {
    padding: 0;
    overflow: hidden;
  }

  .album-cover {
    width: 100%;
    aspect-ratio: 4 / 3;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.4s ease;
    }
  }

  &:hover .album-cover img {
    transform: scale(1.08);
  }

  &.has-cover .album-name,
  &.has-cover .album-count {
    padding: 0.4rem 0.75rem;
  }

  &.has-cover .delete-btn {
    top: 0.5rem;
    right: 0.5rem;
  }

  &:hover {
    border-color: #81d4fa;
    background: var(--glass-strong);
    box-shadow: var(--shadow-glow);
  }

  .album-icon {
    font-size: 2.5rem;
    margin-bottom: 0.75rem;
    transition: transform 0.3s;
  }

  &:hover .album-icon {
    transform: scale(1.1) rotate(-5deg);
  }

  .album-name {
    font-size: 1rem;
    color: var(--text-primary);
    font-weight: 600;
    margin-bottom: 0.5rem;
  }

  .album-count {
    font-size: 0.85rem;
    color: var(--text-secondary);
  }

  .delete-btn {
    position: absolute;
    top: 0.5rem;
    right: 0.5rem;
    opacity: 0;
    transform: scale(0.8);
    transition: all 0.2s;
  }

  &:hover .delete-btn {
    opacity: 1;
    transform: scale(1);
  }

  &--color-0 { border-top: 4px solid #81d4fa; }
  &--color-1 { border-top: 4px solid #80cbc4; }
  &--color-2 { border-top: 4px solid #fff59d; }
  &--color-3 { border-top: 4px solid #a5d6a7; }
  &--color-4 { border-top: 4px solid #ce93d8; }
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;

  .empty-icon {
    font-size: 4rem;
    margin-bottom: 1rem;
  }

  p {
    color: var(--text-secondary);
    font-size: 1.25rem;
    font-weight: 500;
    margin-bottom: 0.5rem;
  }

  .empty-hint {
    font-size: 0.95rem;
    color: var(--text-muted);
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 1.5rem;
}

.toolbar {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2.5rem;
  background: var(--glass);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  margin: 1rem 2.5rem;
  border-radius: 16px;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--glass-border);

  .toolbar-left {
    .view-mode-group {
      :deep(.el-radio-button__inner) {
        background: transparent;
        border: 1px solid rgba(255, 255, 255, 0.3);
        color: rgba(255, 255, 255, 0.8);
        font-weight: 500;
        padding: 0.6rem 1.2rem;
        border-radius: 8px;
        margin-right: 0.5rem;
        transition: all 0.2s ease;
        box-shadow: none;

        .radio-label {
          margin-right: 0.3rem;
        }

        &:hover {
          color: white;
          border-color: rgba(255, 255, 255, 0.5);
          background: rgba(255, 255, 255, 0.1);
        }
      }

      :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
        background: rgba(129, 212, 250, 0.4);
        border-color: #81d4fa;
        color: white;
        box-shadow: 0 2px 8px rgba(129, 212, 250, 0.3);
      }

      :deep(.el-radio-button:first-child .el-radio-button__inner) {
        border-radius: 8px;
      }

      :deep(.el-radio-button:last-child .el-radio-button__inner) {
        border-radius: 8px;
      }
    }
  }

  .toolbar-right {
    .search-box {
      display: flex;
      gap: 0.5rem;
      align-items: center;

      .tag-filter {
        width: 140px;
      }

      .view-layout-toggle {
        :deep(.el-radio-button__inner) {
          background: rgba(255, 255, 255, 0.15);
          border: 1px solid rgba(255, 255, 255, 0.3);
          color: rgba(255, 255, 255, 0.85);
          padding: 0.6rem 0.9rem;
          font-size: 1rem;
        }

        :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
          background: rgba(129, 212, 250, 0.4);
          border-color: #81d4fa;
          color: white;
        }
      }

      .search-input {
        width: 240px;

        :deep(.el-input__wrapper) {
          border-radius: 10px;
          padding: 4px 12px;
          background: rgba(255, 255, 255, 0.1);
          box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.2);
          transition: all 0.2s ease;

          &:hover {
            box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.3);
            background: rgba(255, 255, 255, 0.15);
          }

          &.is-focus {
            box-shadow: 0 0 0 2px #81d4fa;
            background: rgba(255, 255, 255, 0.2);
          }
        }

        :deep(.el-input__inner) {
          color: white;
          &::placeholder {
            color: rgba(255, 255, 255, 0.5);
          }
        }

        .search-icon {
          font-size: 1rem;
        }
      }

      .search-btn {
        background: rgba(129, 212, 250, 0.4);
        border: 1px solid #81d4fa;
        color: white;
        border-radius: 10px;
        padding: 0.6rem 1.2rem;
        font-weight: 500;
        transition: all 0.2s ease;
        box-shadow: 0 2px 8px rgba(129, 212, 250, 0.25);

        &:hover {
          background: rgba(129, 212, 250, 0.5);
          transform: translateY(-1px);
        }

        &:active {
          transform: translateY(0);
        }
      }
    }
  }
}

.stats-bar {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 1.5rem;
  padding: 0.85rem 2rem;
  margin: 0 2.5rem 1rem;
  background: var(--glass);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  color: var(--text-secondary);
  font-size: 0.9rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  border: 1px solid var(--glass-border);

  span {
    display: flex;
    align-items: center;
    gap: 0.4rem;
  }
}

.media-grid {
  columns: 3;
  column-gap: 1rem;
  margin-top: 1rem;

  @media (max-width: 1024px) {
    columns: 2;
  }

  @media (max-width: 600px) {
    columns: 2;
    column-gap: 0.65rem;
  }

  &.list-mode {
    columns: 1;

    .media-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-bottom: 0.75rem;

      img,
      video {
        width: 120px;
        height: 90px;
        flex-shrink: 0;
        border-radius: 8px;
      }

      .media-checkbox {
        position: static;
        flex-shrink: 0;
      }

      .media-overlay {
        position: static;
        background: none;
        padding: 0;
        flex: 1;

        .media-title {
          color: #333;
          font-size: 0.9rem;
          white-space: normal;
        }
      }

      .favorite-btn {
        position: static;
        margin-left: auto;
        flex-shrink: 0;
      }
    }
  }
}

.media-item {
  position: relative;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  background: var(--glass);
  border: 1px solid var(--glass-border);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
  transition: all 0.3s;
  break-inside: avoid;
  margin-bottom: 1rem;

  &:hover {
    transform: translateY(-4px);
    border-color: rgba(79, 195, 247, 0.45);
    box-shadow: var(--shadow-glow);
  }

  &.selected {
    outline: 3px solid #81d4fa;
  }

  img, video {
    width: 100%;
    object-fit: cover;
  }

  img {
    display: block;
    height: auto;
  }

  video {
    display: block;
    height: 150px;
  }

  .media-checkbox {
    position: absolute;
    top: 8px;
    left: 8px;
    width: 20px;
    height: 20px;
    cursor: pointer;
    z-index: 2;
  }

  .media-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(transparent, rgba(0,0,0,0.7));
    padding: 1.5rem 0.5rem 0.5rem;
    .media-title {
      color: white;
      font-size: 0.85rem;
      display: block;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .favorite-btn {
    position: absolute;
    top: 8px;
    right: 8px;
    background: rgba(0,0,0,0.3);
    border: none;
    color: white;
    width: 28px;
    height: 28px;
    border-radius: 50%;
    cursor: pointer;
    font-size: 14px;
    &.active {
      color: #ff6b6b;
    }
  }
}

.batch-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
  padding: 0.75rem 1rem;
  background: var(--glass);
  border: 1px solid var(--glass-border);
  border-radius: 8px;
  margin-bottom: 1rem;
  span {
    color: var(--text-primary);
    font-weight: 500;
  }
}

.skeleton-grid {
  .media-item {
    padding: 0.65rem;
    margin-bottom: 1rem;
  }
}

@media (max-width: 768px) {
  .hero-header {
    padding: 3.5rem 1rem 2.5rem;

    .hero-title {
      font-size: 3rem;
    }

    .hero-subtitle {
      font-size: 0.95rem;
      letter-spacing: 2px;
    }

    .hero-action {
      margin-top: 1.5rem;
    }
  }

  .toolbar {
    flex-direction: column;
    gap: 1rem;
    margin: 0.75rem 1rem;
    padding: 0.75rem 1rem;

    .toolbar-left {
      .view-mode-group {
        display: flex;
        flex-wrap: wrap;
        gap: 0.5rem;
        justify-content: center;

        :deep(.el-radio-button__inner) {
          padding: 0.4rem 0.75rem;
          font-size: 0.8rem;
          margin-right: 0;
        }
      }
    }

    .toolbar-right {
      width: 100%;

      .search-box {
        flex-direction: column;
        width: 100%;

        .tag-filter {
          width: 100%;
        }

        .view-layout-toggle {
          :deep(.el-radio-button__inner) {
            padding: 0.4rem 0.75rem;
          }
        }

        .search-input {
          width: 100%;

          :deep(.el-input__wrapper) {
            width: 100%;
          }
        }

        .search-btn {
          width: 100%;
          margin-top: 0.5rem;
        }
      }
    }
  }

  .stats-bar {
    flex-wrap: wrap;
    gap: 0.75rem;
    margin: 0 1rem 0.75rem;
    padding: 0.75rem 1rem;
    font-size: 0.8rem;
    justify-content: center;
  }

  .container {
    padding: 0.75rem 1rem 2rem;
    margin: 0 1rem;
  }

  .albums-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 0.75rem;
  }

  .album-item {
    padding: 1rem 0.75rem;

    .album-icon {
      font-size: 1.75rem;
    }

    .album-name {
      font-size: 0.85rem;
    }

    .album-count {
      font-size: 0.75rem;
    }
  }

  .media-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 0.75rem;
  }

  .batch-actions {
    flex-wrap: wrap;
    gap: 0.5rem;
    padding: 0.5rem 0.75rem;
    justify-content: center;
  }
}

:deep(.el-dialog) {
  border-radius: 20px;
  background: rgba(18, 25, 35, 0.96);
  backdrop-filter: blur(10px);
  border: 1px solid var(--glass-border);
  box-shadow: var(--shadow-card);

  .el-dialog__header {
    background: linear-gradient(135deg, rgba(79, 195, 247, 0.12) 0%, rgba(255, 255, 255, 0.03) 100%);
    border-bottom: 1px solid rgba(129, 212, 250, 0.3);
    border-radius: 20px 20px 0 0;
    margin-right: 0;
    padding: 1.25rem 1.5rem;

    .el-dialog__title {
      color: var(--text-primary);
      font-weight: 700;
      font-size: 1.1rem;
    }
  }

  .el-dialog__body {
    padding: 1.5rem;
  }

  .el-dialog__footer {
    padding: 1rem 1.5rem;
  }

  .el-button--primary {
    background: linear-gradient(135deg, #81d4fa 0%, #4fc3f7 100%);
    border: none;
    font-weight: 600;

    &:hover {
      opacity: 0.9;
    }
  }
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.07);
  box-shadow: 0 0 0 1px rgba(129, 212, 250, 0.3);

  &:hover {
    box-shadow: 0 0 0 1px rgba(129, 212, 250, 0.6);
  }

  &.is-focus {
    box-shadow: 0 0 0 2px #4fc3f7;
  }
}

:deep(.el-textarea__inner) {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.07);
  color: var(--text-primary);
  box-shadow: 0 0 0 1px rgba(129, 212, 250, 0.3);

  &:hover {
    box-shadow: 0 0 0 1px rgba(129, 212, 250, 0.6);
  }

  &:focus {
    box-shadow: 0 0 0 2px #4fc3f7;
  }
}
</style>
