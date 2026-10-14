<template>
  <div class="favorites-page">
    <h2>我的收藏</h2>
    <EmptyState
      v-if="mediaList.length === 0"
      icon="❤️"
      title="暂无收藏内容"
      hint="浏览时点击卡片右上角的 ♡，喜欢的作品就会出现在这里"
      cta-text="去逛逛"
      @cta="router.push('/')"
    />
    <div v-else class="media-grid">
      <MediaTile
        v-for="item in mediaList"
        :key="item.id"
        :item="item"
        :selectable="false"
        @open="goToDetail"
        @favorite="toggleFavorite"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api/index'
import MediaTile from '@/components/MediaTile.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const mediaList = ref([])

const fetchFavorites = async () => {
  try {
    const response = await api.get('/media', { params: { favorite: true, size: 100 } })
    const data = response.data
    mediaList.value = Array.isArray(data) ? data : (data.list || [])
  } catch (error) {
    ElMessage.error('获取收藏失败')
  }
}

const goToDetail = (item) => {
  router.push(`/media/${item.id}`)
}

const toggleFavorite = async (item) => {
  try {
    await api.post(`/media/${item.id}/favorite`)
    fetchFavorites()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style lang="scss" scoped>
.favorites-page {
  h2 {
    margin-bottom: 1.5rem;
    color: var(--text-primary);
  }
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 0.45rem;
}

@media (max-width: 768px) {
  .media-grid {
    grid-template-columns: repeat(auto-fill, minmax(112px, 1fr));
    gap: 0.3rem;
  }
}
</style>
