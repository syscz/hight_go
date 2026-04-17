<template>
  <div class="upload">
    <h2>上传媒体</h2>

    <el-form :model="form" label-width="80px">
      <!-- 批量文件上传 -->
      <el-form-item label="文件">
        <el-upload
          class="upload-demo"
          drag
          :auto-upload="false"
          :on-change="handleFileChange"
          :file-list="fileList"
          accept="image/*,video/*"
          multiple
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持批量上传图片和视频，单个文件大小不超过2G<br/>
              上传数量: <span class="file-count">{{ fileList.length }}</span> 个文件
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <!-- 批量设置 -->
      <el-collapse v-if="fileList.length > 1" class="batch-settings">
        <el-collapse-item title="批量设置" name="1">
          <!-- 标题 -->
          <el-form-item label="标题">
            <el-input v-model="form.title" placeholder="留空则使用文件名作为标题" />
          </el-form-item>

          <!-- 描述 -->
          <el-form-item label="描述">
            <el-input
              v-model="form.description"
              type="textarea"
              rows="2"
              placeholder="统一描述（可选）"
            />
          </el-form-item>

          <!-- 相册 -->
          <el-form-item label="相册">
            <el-select v-model="form.albumId" placeholder="请选择相册">
              <el-option
                v-for="album in albums"
                :key="album.id"
                :label="album.name"
                :value="album.id"
              />
            </el-select>
          </el-form-item>
        </el-collapse-item>
      </el-collapse>

      <!-- 单文件时显示完整表单 -->
      <template v-if="fileList.length === 1">
        <!-- 标题 -->
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>

        <!-- 描述 -->
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>

        <!-- 相册 -->
        <el-form-item label="相册">
          <el-select v-model="form.albumId" placeholder="请选择相册" clearable>
            <el-option
              v-for="album in albums"
              :key="album.id"
              :label="album.name"
              :value="album.id"
            />
          </el-select>
        </el-form-item>
      </template>

      <!-- 上传进度 -->
      <el-form-item v-if="uploadProgress.length > 0" label="进度">
        <div class="upload-progress">
          <div v-for="(item, index) in uploadProgress" :key="index" class="progress-item">
            <span class="file-name">{{ item.name }}</span>
            <el-progress
              :percentage="item.percentage"
              :status="item.status"
              :stroke-width="10"
            />
            <span class="status-text">{{ item.message }}</span>
          </div>
        </div>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item>
        <el-button
          type="primary"
          @click="handleSubmit"
          :loading="loading"
          :disabled="fileList.length === 0"
        >
          {{ loading ? `上传中 (${completedCount}/${fileList.length})` : `上传 ${fileList.length > 0 ? fileList.length : ''}` }}
        </el-button>
        <el-button @click="resetForm" :disabled="loading">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMediaStore } from '../stores/media'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

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

const albums = computed(() => mediaStore.albums)

const handleFileChange = (file, files) => {
  fileList.value = files
}

const handleSubmit = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择文件')
    return
  }

  loading.value = true
  completedCount.value = 0
  uploadProgress.value = fileList.value.map(f => ({
    name: f.name,
    percentage: 0,
    status: '',
    message: '等待上传...'
  }))

  const maxParallel = 3
  const files = fileList.value.map(f => f.raw)

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

      uploadProgress.value[index].percentage = 50
      uploadProgress.value[index].message = '传输中...'

      await mediaStore.uploadMedia(formData)

      uploadProgress.value[index].percentage = 100
      uploadProgress.value[index].status = 'success'
      uploadProgress.value[index].message = '完成'
      completedCount.value++
    } catch (error) {
      uploadProgress.value[index].status = 'exception'
      uploadProgress.value[index].message = '失败'
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
  fileList.value = []
  uploadProgress.value = []
}

onMounted(() => {
  mediaStore.fetchAlbums()
})
</script>

<style lang="scss" scoped>
.upload {
  max-width: 600px;
  margin: 0 auto;
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  h2 {
    margin-bottom: 2rem;
    text-align: center;
  }

  .el-upload {
    margin-bottom: 1rem;
  }

  .file-count {
    color: #409eff;
    font-weight: 600;
  }

  .batch-settings {
    margin-bottom: 1.5rem;
  }

  .upload-progress {
    width: 100%;

    .progress-item {
      margin-bottom: 1rem;
      padding: 0.75rem;
      background: #f5f7fa;
      border-radius: 8px;

      .file-name {
        display: block;
        font-size: 0.85rem;
        color: #606266;
        margin-bottom: 0.5rem;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .status-text {
        display: block;
        font-size: 0.75rem;
        color: #909399;
        margin-top: 0.25rem;
        text-align: right;
      }
    }
  }
}

@media (max-width: 768px) {
  .upload {
    padding: 1rem;
    margin: 0.5rem;
    border-radius: 12px;

    h2 {
      font-size: 1.25rem;
      margin-bottom: 1rem;
    }

    :deep(.el-form-item) {
      margin-bottom: 1rem;
    }

    :deep(.el-form-item__label) {
      width: 70px !important;
      font-size: 0.85rem;
    }

    :deep(.el-form-item__content) {
      margin-left: 70px !important;
      font-size: 0.85rem;
    }

    :deep(.el-upload-dragger) {
      padding: 1.5rem 1rem;
    }

    :deep(.el-button--primary) {
      width: 100%;
      margin-bottom: 0.5rem;
    }

    :deep(.el-button:not(.el-button--primary)) {
      width: 100%;
    }

    .upload-progress {
      .progress-item {
        padding: 0.5rem;
      }
    }
  }
}
</style>
