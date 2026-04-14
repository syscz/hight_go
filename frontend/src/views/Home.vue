<template>
  <div class="home">
    <header class="header">
      <div class="header-content">
        <h1>我的相册</h1>
        <p>记录美好瞬间</p>
      </div>
      <el-button type="primary" round @click="showCreateAlbumDialog">
        <span class="btn-icon">➕</span> 新建
      </el-button>
    </header>

    <div class="container">
      <div v-loading="loading" class="albums-grid">
        <div
          v-for="(album, index) in albums"
          :key="album.id"
          class="album-item"
          :class="`album-item--color-${index % 5}`"
          @click="viewAlbum(album.id)"
        >
          <div class="album-icon">📁</div>
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

      <div v-if="!loading && albums.length === 0" class="empty-state">
        <div class="empty-icon">🎉</div>
        <p>还没有相册</p>
        <p class="empty-hint">点击上方按钮创建你的第一个相册吧！</p>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMediaStore } from '../stores/media'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const router = useRouter()
const mediaStore = useMediaStore()

const albums = computed(() => mediaStore.albums)
const loading = computed(() => mediaStore.loading)

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

onMounted(() => {
  fetchAlbums()
})
</script>

<style lang="scss" scoped>
.home {
  min-height: 100vh;
  background: url('https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1920&q=80') center/cover no-repeat;
  position: relative;
}

.header {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2rem 2.5rem;
  background: linear-gradient(135deg, rgba(40, 60, 80, 0.7) 0%, rgba(20, 40, 60, 0.5) 100%);
  backdrop-filter: blur(10px);

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
      letter-spacing: 2px;
    }
  }

  :deep(.el-button--primary) {
    background: linear-gradient(135deg, #81d4fa 0%, #4fc3f7 100%);
    border: none;
    font-weight: 500;
    color: #fff;
    box-shadow: 0 4px 15px rgba(79, 195, 247, 0.3);
    transition: all 0.3s;

    &:hover {
      opacity: 0.9;
      transform: translateY(-2px);
    }
  }

  .btn-icon {
    margin-right: 4px;
  }
}

.container {
  position: relative;
  z-index: 1;
  max-width: 900px;
  margin: 0 auto;
  padding: 1rem 2rem 3rem;
}

.albums-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 1.5rem;
}

.album-item {
  position: relative;
  padding: 1.75rem 1.25rem;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.5);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    border-color: #81d4fa;
    background: rgba(255, 255, 255, 0.95);
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
    color: #29b6f6;
    font-weight: 600;
    margin-bottom: 0.5rem;
  }

  .album-count {
    font-size: 0.85rem;
    color: #81d4fa;
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
    color: #fff;
    font-size: 1.25rem;
    font-weight: 500;
    margin-bottom: 0.5rem;
    text-shadow: 0 2px 8px rgba(0,0,0,0.3);
  }

  .empty-hint {
    font-size: 0.95rem;
    color: rgba(255,255,255,0.8);
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

  .albums-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 1rem;
  }

  .album-item {
    padding: 1.25rem 1rem;

    .album-icon {
      font-size: 2rem;
    }

    .album-name {
      font-size: 0.9rem;
    }

    .album-count {
      font-size: 0.8rem;
    }
  }
}

:deep(.el-dialog) {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);

  .el-dialog__header {
    background: linear-gradient(135deg, rgba(129, 212, 250, 0.2) 0%, rgba(255, 255, 255, 0.5) 100%);
    border-bottom: 1px solid rgba(129, 212, 250, 0.3);
    border-radius: 20px 20px 0 0;
    margin-right: 0;
    padding: 1.25rem 1.5rem;

    .el-dialog__title {
      color: #0288d1;
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
  background: rgba(255,255,255,0.8);
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
  background: rgba(255,255,255,0.8);
  box-shadow: 0 0 0 1px rgba(129, 212, 250, 0.3);

  &:hover {
    box-shadow: 0 0 0 1px rgba(129, 212, 250, 0.6);
  }

  &:focus {
    box-shadow: 0 0 0 2px #4fc3f7;
  }
}
</style>