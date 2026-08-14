<template>
  <div class="search-page">
    <header class="header">
      <div class="header-content">
        <h1>🔍 搜索</h1>
        <p>网络搜索 / 文件搜索</p>
      </div>
      <div class="header-actions">
        <el-button round @click="goBack">← 返回</el-button>
      </div>
    </header>

    <div class="search-container">
      <div class="search-tabs">
        <el-radio-group v-model="searchType" size="large">
          <el-radio-button label="web">🌐 网络搜索</el-radio-button>
          <el-radio-button label="file">📁 文件搜索</el-radio-button>
        </el-radio-group>
      </div>

      <div class="search-box">
        <el-input
          v-model="searchQuery"
          :placeholder="searchType === 'web' ? '输入关键词搜索...' : '搜索文件名、标题...'"
          size="large"
          @keyup.enter="doSearch"
          class="search-input"
        >
          <template #append>
            <el-button :icon="Search" @click="doSearch" />
          </template>
        </el-input>
      </div>

      <div v-if="searchType === 'web'" class="search-hint">
        <p>💡 使用 Yandex 搜索引擎</p>
      </div>

      <div v-if="searchType === 'file'" class="file-results">
        <div v-if="loading" class="loading">
          <el-icon class="is-loading"><loading /></el-icon>
          搜索中...
        </div>
        <div v-else-if="fileResults.length > 0" class="results-list">
          <div
            v-for="item in fileResults"
            :key="item.id"
            class="result-item"
            @click="viewMedia(item.id)"
          >
            <div class="result-thumb">
              <img v-if="item.mediaType === 'image'" :src="getMediaUrl(item.filePath)" :alt="item.title" loading="lazy" />
              <div v-else class="video-icon">🎬</div>
            </div>
            <div class="result-info">
              <div class="result-title" v-html="highlight(item.title || item.filename)"></div>
              <div class="result-meta">{{ item.mediaType === 'image' ? '🖼️ 图片' : '🎬 视频' }} · {{ formatSize(item.fileSize) }}</div>
            </div>
          </div>
        </div>
        <div v-else-if="searched" class="no-results">
          <p>未找到相关文件</p>
        </div>
        <div v-else class="search-tip">
          <p>输入关键词搜索您的媒体文件</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Loading } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const searchQuery = ref('')
const searchType = ref('web')
const loading = ref(false)
const searched = ref(false)
const fileResults = ref([])
let debounceTimer = null

watch(searchQuery, () => {
  if (searchType.value !== 'file') return
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    if (searchQuery.value.trim()) {
      searchFiles()
    }
  }, 400)
})

const doSearch = () => {
  if (!searchQuery.value.trim()) return

  if (searchType.value === 'web') {
    const encodedQuery = encodeURIComponent(searchQuery.value.trim())
    window.open(`https://yandex.com/search/?text=${encodedQuery}`, '_blank')
  } else {
    searchFiles()
  }
}

const searchFiles = async () => {
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

const goBack = () => {
  router.push({ name: 'Home' })
}
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
  margin: 2rem auto;
  padding: 0 1rem;
}

.search-tabs {
  margin-bottom: 1.5rem;
  text-align: center;

  :deep(.el-radio-button__inner) {
    padding: 0.8rem 2rem;
    font-size: 1rem;
  }
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
      background: #409eff;
      color: white;
      border: none;

      .el-icon {
        font-size: 1.2rem;
      }
    }
  }
}

.search-hint {
  p {
    font-size: 1rem;
    color: var(--text-secondary);
    text-align: center;
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

.no-results, .search-tip {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted);

  p {
    font-size: 1rem;
  }
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
  }

  .result-thumb {
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

  .search-tabs {
    :deep(.el-radio-button__inner) {
      padding: 0.6rem 1rem;
      font-size: 0.9rem;
    }
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
  }
}
</style>
