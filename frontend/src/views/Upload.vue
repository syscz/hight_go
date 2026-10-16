<template>
  <div class="upload" @dragover.prevent @drop.prevent>
    <h1 class="page-title">上传媒体</h1>

    <!-- 收集区：拖拽 / 点击 / 粘贴 -->
    <div
      class="drop-zone"
      :class="{ dragging, disabled: loading }"
      @dragover.prevent="dragging = true"
      @dragleave.prevent="dragging = false"
      @drop.prevent="handleDrop"
      @click="!loading && openPicker()"
    >
      <input ref="picker" type="file" accept="image/*,video/*" multiple hidden @change="handlePick" />
      <div class="drop-icon">⬆</div>
      <p class="drop-text">拖拽文件到这里，或 <em>点击选择文件</em></p>
      <p class="drop-hint">也可以直接 Ctrl+V 粘贴图片 · 支持批量 · 单个文件最大 2GB</p>
    </div>

    <!-- 已选文件（缩略图网格） -->
    <template v-if="fileList.length">
      <div class="file-grid">
        <div v-for="(f, i) in fileList" :key="f.uid" class="file-item">
          <img v-if="f.isImage" :src="f.preview" :alt="f.name" loading="lazy" />
          <video v-else :src="f.preview" preload="metadata" muted playsinline></video>
          <span class="file-badge">{{ f.isImage ? '图' : '视' }}</span>
          <button class="file-remove" :disabled="loading" title="移除" @click.stop="removeFile(i)">×</button>
          <span class="file-name" :title="f.name">{{ f.name }}</span>
        </div>
      </div>
      <p class="picked-count">
        已选 <b>{{ fileList.length }}</b> 个文件
        <button class="clear-all" :disabled="loading" @click="clearAll">清空</button>
      </p>
    </template>

    <el-form :model="form" label-width="80px" class="upload-form">
      <!-- 批量设置（多文件时折叠） -->
      <el-collapse v-if="fileList.length > 1" class="batch-settings">
        <el-collapse-item title="批量设置（可选）" name="1">
          <el-form-item label="标题">
            <el-input v-model="form.title" placeholder="留空则使用文件名作为标题" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="form.description" type="textarea" :rows="2" placeholder="统一描述（可选）" />
          </el-form-item>
          <el-form-item label="相册">
            <el-select v-model="form.albumId" placeholder="请选择相册" clearable>
              <el-option v-for="album in albums" :key="album.id" :label="album.name" :value="album.id" />
            </el-select>
          </el-form-item>
        </el-collapse-item>
      </el-collapse>

      <!-- 单文件时显示完整表单 -->
      <template v-if="fileList.length === 1">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="相册">
          <el-select v-model="form.albumId" placeholder="请选择相册" clearable>
            <el-option v-for="album in albums" :key="album.id" :label="album.name" :value="album.id" />
          </el-select>
        </el-form-item>
      </template>

      <!-- 上传进度 -->
      <div v-if="uploadProgress.length > 0" class="upload-progress">
        <div v-for="(item, index) in uploadProgress" :key="index" class="progress-item">
          <span class="file-name">{{ item.name }}</span>
          <el-progress :percentage="item.percentage" :status="item.status" :stroke-width="8" />
          <span class="status-text">{{ item.message }}</span>
        </div>
      </div>

      <el-form-item class="actions">
        <el-button
          type="primary"
          round
          size="large"
          @click="handleSubmit"
          :loading="loading"
          :disabled="fileList.length === 0"
        >
          {{ loading ? `上传中 (${completedCount}/${fileList.length})` : `开始上传${fileList.length ? ` (${fileList.length})` : ''}` }}
        </el-button>
        <el-button round size="large" @click="resetForm" :disabled="loading">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMediaStore } from '../stores/media'
import { ElMessage } from 'element-plus'

const router = useRouter()
const mediaStore = useMediaStore()

const form = ref({
  title: '',
  description: '',
  categoryId: '',
  albumId: ''
})

const fileList = ref([])
const loading = ref(false)
const uploadProgress = ref([])
const completedCount = ref(0)
const dragging = ref(false)
const picker = ref(null)
let uidSeed = 0

const albums = computed(() => mediaStore.albums)

const openPicker = () => picker.value?.click()

const addFiles = (rawFiles) => {
  const media = [...rawFiles].filter(
    (f) => f.type.startsWith('image/') || f.type.startsWith('video/')
  )
  if (media.length === 0) {
    ElMessage.warning('仅支持图片和视频文件')
    return
  }
  const existing = new Set(fileList.value.map((f) => `${f.name}:${f.size}`))
  media.forEach((raw) => {
    const key = `${raw.name}:${raw.size}`
    if (existing.has(key) || loading.value) return
    existing.add(key)
    fileList.value.push({
      uid: `f-${++uidSeed}`,
      name: raw.name,
      size: raw.size,
      raw,
      isImage: raw.type.startsWith('image/'),
      preview: URL.createObjectURL(raw)
    })
  })
}

const handlePick = (e) => {
  addFiles(e.target.files)
  e.target.value = ''
}

const handleDrop = (e) => {
  dragging.value = false
  if (loading.value) return
  addFiles(e.dataTransfer?.files || [])
}

/** Ctrl+V 粘贴图片直接加入 */
const handlePaste = (e) => {
  if (loading.value) return
  const files = [...(e.clipboardData?.files || [])].filter((f) => f.type.startsWith('image/'))
  if (files.length) {
    addFiles(files)
    ElMessage.success(`已粘贴 ${files.length} 个文件`)
  }
}

const removeFile = (index) => {
  const [removed] = fileList.value.splice(index, 1)
  if (removed?.preview) URL.revokeObjectURL(removed.preview)
}

const clearAll = () => {
  fileList.value.forEach((f) => f.preview && URL.revokeObjectURL(f.preview))
  fileList.value = []
}

const handleSubmit = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择文件')
    return
  }

  loading.value = true
  completedCount.value = 0
  uploadProgress.value = fileList.value.map((f) => ({
    name: f.name,
    percentage: 0,
    status: '',
    message: '等待上传...'
  }))

  const maxParallel = 3
  const files = fileList.value.map((f) => f.raw)

  const uploadFile = async (file, index) => {
    try {
      uploadProgress.value[index].message = '上传中...'
      uploadProgress.value[index].percentage = 10

      const formData = new FormData()
      formData.append('file', file)

      const title = form.value.title || file.name.replace(/\.[^/.]+$/, '')
      formData.append('title', title)
      formData.append('description', form.value.description || '')
      if (form.value.albumId) {
        formData.append('albumId', form.value.albumId)
      }

      uploadProgress.value[index].message = '传输中...'

      await mediaStore.uploadMedia(formData, (p) => {
        uploadProgress.value[index].percentage = Math.max(p, 10)
      })

      uploadProgress.value[index].percentage = 100
      uploadProgress.value[index].status = 'success'
      uploadProgress.value[index].message = '完成'
      completedCount.value++
    } catch (error) {
      uploadProgress.value[index].status = 'exception'
      uploadProgress.value[index].message = error.response?.data?.error || '失败'
    }
  }

  for (let i = 0; i < files.length; i += maxParallel) {
    const batch = []
    for (let j = 0; j < maxParallel && i + j < files.length; j++) {
      batch.push(uploadFile(files[i + j], i + j))
    }
    await Promise.all(batch)
  }

  loading.value = false

  if (completedCount.value === files.length) {
    ElMessage.success(`全部上传成功 (${completedCount.value}/${files.length})`)
    resetForm()
    router.push('/')
  } else {
    ElMessage.warning(`部分上传成功 (${completedCount.value}/${files.length})`)
  }
}

const resetForm = () => {
  form.value = {
    title: '',
    description: '',
    categoryId: '',
    albumId: ''
  }
  clearAll()
  uploadProgress.value = []
}

onMounted(() => {
  mediaStore.fetchAlbums()
  window.addEventListener('paste', handlePaste)
})

onUnmounted(() => {
  window.removeEventListener('paste', handlePaste)
  fileList.value.forEach((f) => f.preview && URL.revokeObjectURL(f.preview))
})
</script>

<style lang="scss" scoped>
.upload {
  max-width: 720px;
  margin: 0 auto;

  .page-title {
    margin: 0 0 1.25rem;
    font-size: 1.3rem;
    color: var(--text-primary);
    text-align: center;
  }
}

/* 收集区 */
.drop-zone {
  border: 2px dashed var(--glass-border);
  border-radius: 16px;
  background: var(--glass);
  padding: 2.2rem 1rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;

  .drop-icon {
    font-size: 2rem;
    color: #4fc3f7;
    margin-bottom: 0.5rem;
  }

  .drop-text {
    margin: 0;
    color: var(--text-primary);
    font-size: 0.95rem;

    em {
      color: #4fc3f7;
      font-style: normal;
      font-weight: 600;
    }
  }

  .drop-hint {
    margin: 0.4rem 0 0;
    color: var(--text-muted);
    font-size: 0.8rem;
  }

  &:hover,
  &.dragging {
    border-color: #4fc3f7;
    background: rgba(79, 195, 247, 0.08);
  }

  &.dragging {
    transform: scale(1.01);
  }

  &.disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

/* 已选文件网格 */
.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(96px, 1fr));
  gap: 0.5rem;
  margin-top: 1rem;
}

.file-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 10px;
  overflow: hidden;
  background: var(--glass);
  border: 1px solid var(--glass-border);

  img,
  video {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  .file-badge {
    position: absolute;
    top: 4px;
    left: 4px;
    background: rgba(0, 0, 0, 0.55);
    color: #fff;
    font-size: 0.65rem;
    padding: 1px 6px;
    border-radius: 8px;
  }

  .file-remove {
    position: absolute;
    top: 4px;
    right: 4px;
    width: 20px;
    height: 20px;
    border: none;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
    font-size: 12px;
    line-height: 1;
    cursor: pointer;

    &:hover {
      background: #f56c6c;
    }
  }

  .file-name {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
    color: #fff;
    font-size: 0.68rem;
    padding: 12px 5px 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.picked-count {
  margin: 0.6rem 0 0;
  font-size: 0.85rem;
  color: var(--text-secondary);

  b {
    color: #4fc3f7;
  }

  .clear-all {
    margin-left: 0.75rem;
    background: none;
    border: none;
    color: var(--text-muted);
    font-size: 0.85rem;
    cursor: pointer;
    text-decoration: underline;

    &:hover {
      color: #f56c6c;
    }
  }
}

.upload-form {
  margin-top: 1.25rem;
}

.batch-settings {
  margin-bottom: 1rem;

  :deep(.el-collapse-item__header),
  :deep(.el-collapse-item__wrap) {
    background: transparent;
    border-color: var(--glass-border);
    color: var(--text-secondary);
  }
}

.upload-progress {
  width: 100%;
  margin-bottom: 1rem;

  .progress-item {
    margin-bottom: 0.75rem;
    padding: 0.75rem;
    background: var(--glass);
    border: 1px solid var(--glass-border);
    border-radius: 10px;

    .file-name {
      display: block;
      font-size: 0.85rem;
      color: var(--text-secondary);
      margin-bottom: 0.5rem;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .status-text {
      display: block;
      font-size: 0.75rem;
      color: var(--text-muted);
      margin-top: 0.25rem;
      text-align: right;
    }
  }
}

.actions {
  margin-top: 1.25rem;
}

@media (max-width: 768px) {
  .upload {
    .page-title {
      font-size: 1.15rem;
      margin-bottom: 0.9rem;
    }
  }

  .drop-zone {
    padding: 1.6rem 0.8rem;

    .drop-hint {
      font-size: 0.72rem;
    }
  }

  .file-grid {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  }

  :deep(.el-form-item__label) {
    width: 70px !important;
    font-size: 0.85rem;
  }

  :deep(.el-form-item__content) {
    margin-left: 70px !important;
  }

  .actions {
    :deep(.el-form-item__content) {
      margin-left: 0 !important;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;

      .el-button {
        width: 100%;
        margin-left: 0;
      }
    }
  }
}
</style>
