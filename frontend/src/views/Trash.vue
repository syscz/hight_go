<template>
  <div class="trash-page">
    <div class="page-header">
      <h2>回收站</h2>
      <div class="header-actions" v-if="mediaList.length > 0">
        <el-button type="success" @click="restoreAll">全部恢复</el-button>
        <el-button type="danger" @click="permanentDeleteAll">清空回收站</el-button>
      </div>
    </div>
    <EmptyState
      v-if="mediaList.length === 0"
      icon="🗑"
      title="回收站为空"
      hint="删除的内容会在这里保留 30 天，之后自动清理"
    />
    <div v-else class="media-grid">
      <MediaTile
        v-for="item in mediaList"
        :key="item.id"
        :item="item"
        :selectable="false"
        :favoritable="false"
        grayscale
      >
        <template #actions>
          <button class="trash-btn" @click.stop="restoreItem(item)">恢复</button>
          <button class="trash-btn delete" @click.stop="permanentDelete(item)">删除</button>
        </template>
      </MediaTile>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api/index'
import MediaTile from '@/components/MediaTile.vue'
import EmptyState from '@/components/EmptyState.vue'

const mediaList = ref([])

const fetchTrash = async () => {
  try {
    const response = await api.get('/media', { params: { deleted: true, size: 200 } })
    const data = response.data
    mediaList.value = Array.isArray(data) ? data : (data.list || [])
  } catch (error) {
    ElMessage.error('获取回收站失败')
  }
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

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 0.45rem;
}

.trash-btn {
  flex: 1;
  padding: 0.3rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  background: rgba(255, 255, 255, 0.2);
  color: white;

  &:hover {
    background: rgba(255, 255, 255, 0.3);
  }

  &.delete {
    background: rgba(255, 0, 0, 0.5);

    &:hover {
      background: rgba(255, 0, 0, 0.7);
    }
  }
}

@media (max-width: 768px) {
  .media-grid {
    grid-template-columns: repeat(auto-fill, minmax(112px, 1fr));
    gap: 0.3rem;
  }
}
</style>
