<template>
  <div class="search-page">
    <header class="header">
      <div class="header-content">
        <h1>🔍 搜索</h1>
        <p>在媒体库中查找图片和视频</p>
      </div>
      <div class="header-actions">
        <el-button round @click="goBack">← 返回</el-button>
      </div>
    </header>

    <div class="search-container">
      <div class="search-box">
        <el-input
          v-model="searchQuery"
          placeholder="搜索文件名、标题、描述..."
          size="large"
          clearable
          @keyup.enter="searchFiles"
          class="search-input"
        >
          <template #prefix>🔍</template>
          <template #append>
            <el-button :icon="Search" @click="searchFiles" />
          </template>
        </el-input>
      </div>

      <!-- Google Photos 风格筛选 chips -->
      <div class="filter-chips">
        <button
          v-for="chip in chips"
          :key="chip.value"
          class="filter-chip"
          :class="{ active: filter === chip.value }"
          @click="filter = chip.value"
        >
          {{ chip.label }}
          <span v-if="chip.count" class="chip-count">{{ chip.count }}</span>
        </button>
      </div>

      <div class="file-results">
        <div v-if="loading" class="loading">
          <el-icon class="is-loading"><loading /></el-icon>
          搜索中...
        </div>

        <template v-else-if="filteredResults.length > 0">
          <div class="results-summary">找到 {{ filteredResults.length }} 个结果</div>
          <div class="results-list">
            <div
              v-for="item in filteredResults"
              :key="item.id"
              class="result-item"
              @click="viewMedia(item.id)"
            >
              <div class="result-thumb">
                <img v-if="thumbSrc(item)" :src="thumbSrc(item)" :alt="item.title" loading="lazy" v-img-fade />
                <div v-else class="video-icon">🎬</div>
                <span v-if="item.mediaType === 'video' && durationText(item)" class="thumb-duration">
                  {{ durationText(item) }}
                </span>
              </div>
              <div class="result-info">
                <div class="result-title" v-html="highlight(item.title || item.filename)"></div>
                <div class="result-meta">
                  {{ item.mediaType === 'image' ? '🖼️ 图片' : '🎬 视频' }} · {{ formatSize(item.fileSize) }}
                  <template v-if="item.createdAt"> · {{ formatDate(item.createdAt) }}</template>
                </div>
              </div>
              <button
                class="fav-inline"
                :class="{ active: item.isFavorite }"
                title="收藏"
                @click.stop="toggleFavorite(item)"
              >
                {{ item.isFavorite ? '♥' : '♡' }}
              </button>
            </div>
          </div>
        </template>

        <EmptyState
          v-else-if="searched"
          icon="🔍"
          title="未找到相关文件"
          :hint="`没有匹配“${searchQuery}”${filterLabel}的内容，换个关键词试试`"
        />
        <EmptyState
          v-else
          icon="⌨️"
          title="输入关键词开始搜索"
          hint="支持按文件名、标题、描述模糊匹配，结果会高亮显示"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import { tileImageSrc, formatDuration } from '@/utils/media'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const route = useRoute()
const searchQuery = ref('')
const loading = ref(false)
const searched = ref(false)
const fileResults = ref([])
const filter = ref('all')
let debounceTimer = null

const chips = computed(() => [
  { value: 'all', label: '全部', count: fileResults.value.length },
  { value: 'image', label: '🖼 图片', count: fileResults.value.filter(i => i.mediaType === 'image').length },
  { value: 'video', label: '🎬 视频', count: fileResults.value.filter(i => i.mediaType === 'video').length },
  { value: 'favorite', label: '❤️ 收藏', count: fileResults.value.filter(i => i.isFavorite).length }
])

const filterLabel = computed(() => {
  const c = chips.value.find(c => c.value === filter.value)
  return c && c.value !== 'all' ? `（${c.label.replace(/^\S+\s/, '')}）` : ''
})

const filteredResults = computed(() => {
  switch (filter.value) {
    case 'image': return fileResults.value.filter(i => i.mediaType === 'image')
    case 'video': return fileResults.value.filter(i => i.mediaType === 'video')
    case 'favorite': return fileResults.value.filter(i => i.isFavorite)
    default: return fileResults.value
  }
})

watch(searchQuery, () => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    if (searchQuery.value.trim()) {
      searchFiles()
    } else {
      fileResults.value = []
      searched.value = false
    }
  }, 400)
})

const searchFiles = async () => {
  if (!searchQuery.value.trim()) return
  loading.value = true
  searched.value = true
  try {
    const response = await api.get('/media/search', {
      params: { keyword: searchQuery.value.trim() }
    })
    fileResults.value = response.data || []
  } catch (error) {
    console.error('搜索失败:', error)
    fileResults.value = []
  } finally {
    loading.value = false
  }
}

const viewMedia = (id) => {
  router.push({ name: 'MediaDetail', params: { id } })
}

const toggleFavorite = async (item) => {
  try {
    await api.post(`/media/${item.id}/favorite`)
    item.isFavorite = !item.isFavorite
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const thumbSrc = (item) => tileImageSrc(item)

const durationText = (item) => formatDuration(item.duration)

const formatDate = (s) => String(s).slice(0, 10)

const highlight = (text) => {
  if (!text || !searchQuery.value.trim()) return text
  const kw = searchQuery.value.trim()
  const escaped = String(text).replace(/[&<>"']/g, (c) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }[c]))
  const safe = kw.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return escaped.replace(new RegExp(`(${safe})`, 'gi'), '<mark>$1</mark>')
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const goBack = () => {
  router.push({ name: 'Home' })
}

onMounted(() => {
  if (route.query.q) {
    searchQuery.value = route.query.q
    searchFiles()
  }
})
</script>

<style lang="scss" scoped>
.search-page {
  min-height: 100vh;
  background: transparent;
}

.header {
  background: var(--glass);
  backdrop-filter: blur(10px);
  padding: 1.5rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid var(--glass-border);
  box-shadow: var(--shadow-card);

  .header-content {
    h1 {
      margin: 0;
      font-size: 1.5rem;
      color: var(--text-primary);
    }

    p {
      margin: 0.25rem 0 0;
      font-size: 0.9rem;
      color: var(--text-secondary);
    }
  }
}

.search-container {
  max-width: 900px;
  margin: 1.5rem auto;
  padding: 0 1rem;
}

.search-box {
  margin-bottom: 1rem;

  .search-input {
    :deep(.el-input__wrapper) {
      padding: 0.5rem 1rem;
      border-radius: 25px;
      box-shadow: 0 2px 15px rgba(0, 0, 0, 0.1);
    }

    :deep(.el-input__inner) {
      font-size: 1.1rem;
    }

    :deep(.el-input-group__append) {
      border-radius: 0 25px 25px 0;
      background: linear-gradient(135deg, #4fc3f7 0%, #29b6f6 100%);
      color: white;
      border: none;

      .el-icon {
        font-size: 1.2rem;
      }
    }
  }
}

.filter-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;

  .filter-chip {
    display: inline-flex;
    align-items: center;
    gap: 0.35rem;
    padding: 0.45rem 1rem;
    border-radius: 20px;
    border: 1px solid var(--glass-border);
    background: var(--glass);
    color: var(--text-secondary);
    font-size: 0.88rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: var(--glass-strong);
      color: var(--text-primary);
    }

    &.active {
      background: rgba(79, 195, 247, 0.22);
      border-color: #4fc3f7;
      color: var(--accent);
      font-weight: 600;
    }

    .chip-count {
      background: rgba(255, 255, 255, 0.14);
      border-radius: 10px;
      padding: 0 6px;
      font-size: 0.75rem;
      line-height: 1.5;
    }
  }
}

.file-results {
  background: var(--glass);
  border-radius: 16px;
  padding: 1rem;
  border: 1px solid var(--glass-border);
  box-shadow: var(--shadow-card);
  min-height: 300px;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: var(--text-secondary);
}

.results-summary {
  color: var(--text-muted);
  font-size: 0.85rem;
  padding: 0.25rem 0.5rem 0.75rem;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: var(--glass-strong);

    .fav-inline {
      opacity: 1;
    }
  }

  .result-thumb {
    position: relative;
    width: 60px;
    height: 60px;
    border-radius: 8px;
    overflow: hidden;
    background: rgba(255, 255, 255, 0.08);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .video-icon {
      font-size: 1.5rem;
    }

    .thumb-duration {
      position: absolute;
      right: 3px;
      bottom: 3px;
      background: rgba(0, 0, 0, 0.65);
      color: white;
      font-size: 0.68rem;
      font-weight: 600;
      padding: 2px 4px;
      border-radius: 3px;
      font-variant-numeric: tabular-nums;
    }
  }

  .result-info {
    flex: 1;
    min-width: 0;

    .result-title {
      font-size: 1rem;
      color: var(--text-primary);
      font-weight: 500;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;

      :deep(mark) {
        background: #ffe58f;
        color: #333;
        border-radius: 2px;
        padding: 0 1px;
      }
    }

    .result-meta {
      font-size: 0.85rem;
      color: var(--text-muted);
      margin-top: 0.25rem;
    }
  }

  .fav-inline {
    flex-shrink: 0;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    border: none;
    background: transparent;
    color: var(--text-muted);
    font-size: 16px;
    cursor: pointer;
    opacity: 0;
    transition: all 0.2s;

    &.active {
      color: #ff6b6b;
      opacity: 1;
    }
  }
}

@media (max-width: 768px) {
  .header {
    padding: 1rem;
    flex-direction: column;
    gap: 0.75rem;

    .header-content {
      text-align: center;

      h1 {
        font-size: 1.25rem;
      }

      p {
        font-size: 0.8rem;
      }
    }

    .el-button {
      padding: 0.5rem 1rem;
    }
  }

  .search-container {
    margin: 1rem auto;
  }

  .search-box {
    :deep(.el-input__wrapper) {
      border-radius: 20px;
    }

    :deep(.el-input__inner) {
      font-size: 1rem;
    }
  }

  .result-item {
    padding: 0.5rem;

    .result-thumb {
      width: 50px;
      height: 50px;
    }

    .fav-inline {
      opacity: 1;
    }
  }
}
</style>
