<template>
  <div class="home">
    <ProfileHeader :stats="profileStats" />

    <nav class="space-tabs">
      <button class="tab-btn" :class="{ active: tab === 'all' }" @click="tab = 'all'">
        <span class="tab-icon">▦</span>全部
      </button>
      <button class="tab-btn" :class="{ active: tab === 'albums' }" @click="tab = 'albums'">
        <span class="tab-icon">📁</span>相册
      </button>
    </nav>

    <!-- 全部：Instagram 网格 -->
    <template v-if="tab === 'all'">
      <div class="space-toolbar">
        <el-radio-group v-model="typeFilter" size="small" class="type-chips" @change="handleFilterChange">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="image">🖼 图片</el-radio-button>
          <el-radio-button label="video">🎬 视频</el-radio-button>
        </el-radio-group>
        <div class="toolbar-right">
          <el-select
            v-model="selectedTagId"
            placeholder="标签"
            clearable
            size="small"
            class="tag-filter"
            @change="handleFilterChange"
          >
            <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
          <el-select v-model="sortBy" size="small" class="sort-select" @change="handleFilterChange">
            <el-option label="最新" value="latest" />
            <el-option label="最旧" value="oldest" />
            <el-option label="名称" value="name" />
            <el-option label="大小" value="size" />
          </el-select>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索标题/描述..."
            clearable
            size="small"
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>🔍</template>
          </el-input>
        </div>
      </div>

      <div v-if="initialLoading" class="ig-grid">
        <div v-for="n in 9" :key="n" class="ig-skeleton">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="width: 100%; aspect-ratio: 1" />
            </template>
          </el-skeleton>
        </div>
      </div>

      <template v-else>
        <div v-if="mediaList.length" class="ig-grid">
          <IgMediaTile
            v-for="item in mediaList"
            :key="item.id"
            :item="item"
            :selected="selectedMedia.includes(item.id)"
            @open="openLightbox"
            @select="toggleSelect"
          />
        </div>

        <EmptyState
          v-if="mediaList.length === 0"
            icon="🔍"
            title="还没有内容"
            hint="上传一些照片或视频，你的空间会立刻热闹起来"
            cta-text="去上传"
            @cta="router.push('/upload')"
        />

        <div v-if="hasMore && !loadingMore" ref="sentinel" class="scroll-sentinel">
          <span>加载中…</span>
        </div>
      </template>
    </template>

    <!-- 相册 -->
    <template v-else>
      <div class="albums-head">
        <span class="albums-count">共 {{ albums.length }} 个相册</span>
        <el-button type="primary" round size="small" @click="showCreateAlbumDialog">
          ➕ 新建相册
        </el-button>
      </div>
      <div v-loading="loading" class="albums-grid">
        <div
          v-for="(album, index) in albums"
          :key="album.id"
          class="album-item"
          :class="[`album-item--color-${index % 5}`, { 'has-cover': album.coverImage }]"
          @click="viewAlbum(album.id)"
        >
          <div v-if="album.coverImage" class="album-cover">
            <img :src="resolveMediaUrl(album.coverImage)" :alt="album.name" loading="lazy" />
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
      <EmptyState
        v-if="!loading && albums.length === 0"
        icon="📁"
        title="还没有相册"
        hint="创建一个相册，把照片和视频整理起来"
        cta-text="新建相册"
        @cta="showCreateAlbumDialog"
      />
    </template>

    <transition name="fade">
      <div v-if="selectedMedia.length > 0" class="floating-actions">
        <span class="sel-count">已选择 {{ selectedMedia.length }} 项</span>
        <el-button size="small" @click="clearSelection">取消</el-button>
        <el-button size="small" type="primary" @click="showMoveDialog">移动到</el-button>
        <el-button size="small" @click="showEditDialog">编辑</el-button>
        <el-button size="small" type="success" @click="batchDownload">下载</el-button>
        <el-button size="small" type="danger" @click="batchDelete">删除</el-button>
      </div>
    </transition>

    <el-dialog v-model="createAlbumDialogVisible" title="创建新相册" width="360px" round align-center>
      <el-form @submit.prevent="handleCreateAlbum">
        <el-form-item>
          <el-input v-model="newAlbumName" placeholder="给相册起个名字~" autofocus size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="newAlbumDesc" type="textarea" placeholder="相册描述（选填）" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createAlbumDialogVisible = false">算了</el-button>
        <el-button type="primary" round @click="handleCreateAlbum">创建！</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="moveDialogVisible" title="移动到相册" width="360px">
      <el-select v-model="targetAlbumId" placeholder="选择目标相册" style="width: 100%">
        <el-option v-for="album in albums" :key="album.id" :label="album.name" :value="album.id" />
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

    <MediaLightbox
      v-model:visible="lightboxVisible"
      v-model:index="lightboxIndex"
      :items="mediaList"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMediaStore } from '../stores/media'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import api from '@/api/index'
import { resolveMediaUrl } from '@/utils/media'
import MediaLightbox from '../components/MediaLightbox.vue'
import IgMediaTile from '@/components/IgMediaTile.vue'
import ProfileHeader from '@/components/ProfileHeader.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const route = useRoute()
const mediaStore = useMediaStore()

const albums = computed(() => mediaStore.albums)
const loading = computed(() => mediaStore.loading)
const tags = computed(() => mediaStore.tags)

/** 全部 / 相册 Tab */
const tab = ref('all')
/** 全部 Tab 内的类型筛选（空 = 不限） */
const typeFilter = ref('')

const searchKeyword = ref('')
const mediaList = ref([])
const initialLoading = ref(true)
const loadingMore = ref(false)
const currentPage = ref(1)
const pageSize = ref(24)
const totalMedia = ref(0)
const selectedTagId = ref(null)
const sortBy = ref('latest')
const selectedMedia = ref([])
const moveDialogVisible = ref(false)
const targetAlbumId = ref(null)
const editDialogVisible = ref(false)
const editForm = ref({ title: '', description: '', tagIds: [] })
const lightboxVisible = ref(false)
const lightboxIndex = ref(0)
const sentinel = ref(null)
let observer = null
let searchDebounce = null

const statsVO = ref({ totalMedia: 0, favoriteCount: 0 })

const profileStats = computed(() => ({
  posts: statsVO.value.totalMedia || 0,
  albums: albums.value.length,
  favorites: statsVO.value.favoriteCount || 0
}))

watch(searchKeyword, () => {
  clearTimeout(searchDebounce)
  searchDebounce = setTimeout(() => {
    if (tab.value === 'all') {
      currentPage.value = 1
      fetchMediaList()
    }
  }, 400)
})

const createAlbumDialogVisible = ref(false)
const newAlbumName = ref('')
const newAlbumDesc = ref('')

const fetchAlbums = async () => {
  await mediaStore.fetchAlbums()
}

const fetchStats = async () => {
  try {
    const response = await api.get('/media/stats')
    statsVO.value = response.data || {}
  } catch (error) {
    console.error('获取统计失败', error)
  }
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

const fetchMediaList = async (append = false) => {
  if (append) loadingMore.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (typeFilter.value) params.type = typeFilter.value
    if (selectedTagId.value) params.tagId = selectedTagId.value
    if (sortBy.value !== 'latest') {
      params.sort = sortBy.value === 'oldest' ? 'created_asc' : sortBy.value
    }
    const response = await api.get('/media', { params })
    const data = response.data
    const list = Array.isArray(data) ? data : (data.list || [])
    const total = Array.isArray(data) ? list.length : (data.total || 0)
    if (append) {
      mediaList.value = [...mediaList.value, ...list]
    } else {
      mediaList.value = list
    }
    totalMedia.value = total
  } catch (error) {
    ElMessage.error('获取媒体列表失败')
  } finally {
    initialLoading.value = false
    loadingMore.value = false
  }
}

const hasMore = computed(() => mediaList.value.length < totalMedia.value)

const loadMore = () => {
  if (loadingMore.value || !hasMore.value) return
  currentPage.value += 1
  fetchMediaList(true)
}

const handleSearch = () => {
  clearTimeout(searchDebounce)
  currentPage.value = 1
  if (tab.value !== 'all') tab.value = 'all'
  else fetchMediaList()
}

const handleFilterChange = () => {
  currentPage.value = 1
  fetchMediaList()
}

const openLightbox = (item) => {
  const idx = mediaList.value.findIndex(m => m.id === item.id)
  if (idx >= 0) {
    lightboxIndex.value = idx
    lightboxVisible.value = true
  }
}

watch(tab, (mode) => {
  currentPage.value = 1
  if (mode === 'all') {
    fetchMediaList()
  } else {
    fetchAlbums()
  }
})

const toggleSelect = (id) => {
  if (selectedMedia.value.includes(id)) {
    selectedMedia.value = selectedMedia.value.filter(i => i !== id)
  } else {
    selectedMedia.value.push(id)
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
    fetchStats()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(() => {
  const q = route.query
  if (q.tab === 'albums' || q.viewMode === 'albums') tab.value = 'albums'
  if (q.keyword) searchKeyword.value = q.keyword
  fetchAlbums()
  mediaStore.fetchTags()
  fetchStats()
  if (tab.value === 'all') {
    fetchMediaList()
  } else {
    initialLoading.value = false
  }
  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting) {
      loadMore()
    }
  }, { rootMargin: '300px' })
  watch(hasMore, (more) => {
    if (more) {
      nextTick(() => {
        if (sentinel.value && observer) observer.observe(sentinel.value)
      })
    }
  })
})

onBeforeUnmount(() => {
  if (observer) observer.disconnect()
})
</script>

<style lang="scss" scoped>
.home {
  max-width: 1010px;
  margin: 0 auto;
  position: relative;
}

/* ===== IG 式 Tab ===== */
.space-tabs {
  display: flex;
  justify-content: center;
  gap: 3.5rem;
  border-top: 1px solid var(--glass-border);
  margin-top: 1.4rem;

  .tab-btn {
    display: inline-flex;
    align-items: center;
    gap: 0.45rem;
    background: none;
    border: none;
    border-bottom: 2px solid transparent;
    color: var(--text-muted);
    font-size: 0.88rem;
    font-weight: 700;
    letter-spacing: 1px;
    padding: 0.9rem 0.25rem;
    cursor: pointer;
    transition: color 0.2s ease, border-color 0.2s ease;
    margin-bottom: -1px;
    border-top: none;
    border-left: none;
    border-right: none;

    .tab-icon {
      font-size: 1rem;
    }

    &:hover {
      color: var(--text-secondary);
    }

    &.active {
      color: var(--text-primary);
      border-bottom-color: var(--text-primary);
    }
  }
}

/* ===== 工具行（低调收进一行） ===== */
.space-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  flex-wrap: wrap;
  padding: 0.65rem 0;
  margin-bottom: 0.4rem;

  .type-chips {
    :deep(.el-radio-button__inner) {
      background: transparent;
      border: 1px solid var(--glass-border);
      color: var(--text-secondary);
      font-weight: 500;
      padding: 0.38rem 0.85rem;
      transition: all 0.2s ease;
      box-shadow: none;

      &:hover {
        color: var(--text-primary);
      }
    }

    :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
      background: rgba(79, 195, 247, 0.16);
      border-color: #4fc3f7;
      color: #4fc3f7;
    }
  }

  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    flex-wrap: wrap;

    .tag-filter {
      width: 110px;
    }

    .sort-select {
      width: 90px;
    }

    .search-input {
      width: 200px;
    }
  }
}

/* ===== IG 网格 ===== */
.ig-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px;
}

.ig-skeleton {
  :deep(.el-skeleton) {
    width: 100%;
  }
}

/* ===== 相册 ===== */
.albums-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 0.25rem 0.9rem;

  .albums-count {
    color: var(--text-muted);
    font-size: 0.88rem;
  }
}

.albums-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 1.5rem;
  min-height: 120px;
}

.album-item {
  position: relative;
  padding: 0;
  background: none;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.25s ease;

  &:hover {
    transform: translateY(-3px);
  }

  .album-cover {
    width: 100%;
    aspect-ratio: 4 / 3;
    border-radius: 10px;
    overflow: hidden;
    background: var(--glass);

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
    padding: 0.5rem 0.25rem 0;
  }

  &.has-cover .delete-btn {
    top: 0.4rem;
    right: 0.4rem;
    z-index: 5;
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
    margin: 0;
    padding: 0.5rem 0.25rem 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .album-count {
    font-size: 0.85rem;
    color: var(--text-secondary);
    padding: 0 0.25rem;
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

.scroll-sentinel {
  display: flex;
  justify-content: center;
  padding: 1.5rem 0;
  color: var(--text-muted);
  font-size: 0.85rem;
}

.floating-actions {
  position: fixed;
  bottom: 1.5rem;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.7rem 1rem;
  background: var(--bg-elevated);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 14px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(16px);

  .sel-count {
    color: var(--text-primary);
    font-size: 0.88rem;
    font-weight: 600;
    margin-right: 0.25rem;
  }
}

@media (max-width: 768px) {
  .floating-actions {
    bottom: calc(4.4rem + env(safe-area-inset-bottom));
    max-width: 94vw;
    flex-wrap: wrap;
    justify-content: center;
  }

  .space-tabs {
    gap: 2rem;

    .tab-btn {
      font-size: 0.82rem;
      gap: 0.35rem;
      padding: 0.75rem 0.15rem;
    }
  }

  .space-toolbar {
    .toolbar-right {
      .search-input {
        width: 100%;
      }
    }
  }

  .ig-grid {
    gap: 2px;
  }
}
</style>
