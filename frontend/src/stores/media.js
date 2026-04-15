import { defineStore } from 'pinia'
import api from '@/api/index'

export const useMediaStore = defineStore('media', {
  state: () => ({
    mediaList: [],
    categories: [],
    tags: [],
    albums: [],
    loading: false,
    error: null
  }),

  getters: {
    images: (state) => state.mediaList.filter(item => item.mediaType === 'image'),
    videos: (state) => state.mediaList.filter(item => item.mediaType === 'video')
  },

  actions: {
    async fetchMediaList(type = null, categoryId = null, albumId = null) {
      this.loading = true
      this.error = null
      try {
        let url = '/media'
        const params = []
        if (type) params.push(`type=${type}`)
        if (categoryId) params.push(`categoryId=${categoryId}`)
        if (albumId) params.push(`albumId=${albumId}`)
        if (params.length > 0) url += '?' + params.join('&')
        const response = await api.get(url)
        this.mediaList = response.data
      } catch (error) {
        this.error = error.message
        console.error('获取媒体列表失败:', error)
      } finally {
        this.loading = false
      }
    },

    async fetchAlbums() {
      try {
        const response = await api.get('/albums')
        this.albums = response.data
      } catch (error) {
        console.error('获取相册列表失败:', error)
      }
    },

    async createAlbum(album) {
      try {
        const response = await api.post('/albums', album)
        this.albums.push(response.data)
        return response.data
      } catch (error) {
        console.error('创建相册失败:', error)
        throw error
      }
    },

    async deleteAlbum(id) {
      try {
        await api.delete(`/albums/${id}`)
        this.albums = this.albums.filter(a => a.id !== id)
      } catch (error) {
        console.error('删除相册失败:', error)
        throw error
      }
    },

    async fetchCategories() {
      try {
        const response = await api.get('/categories')
        this.categories = response.data
      } catch (error) {
        console.error('获取分类列表失败:', error)
      }
    },

    async fetchTags() {
      try {
        const response = await api.get('/tags')
        this.tags = response.data
      } catch (error) {
        console.error('获取标签列表失败:', error)
      }
    },

    async uploadMedia(formData) {
      this.loading = true
      this.error = null
      try {
        const response = await api.post('/media', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        this.mediaList.unshift(response.data)
        return response.data
      } catch (error) {
        this.error = error.message
        console.error('上传媒体失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    async deleteMedia(id) {
      try {
        await api.delete(`/media/${id}`)
        this.mediaList = this.mediaList.filter(item => item.id !== id)
      } catch (error) {
        console.error('删除媒体失败:', error)
        throw error
      }
    },

    async searchMedia(keyword) {
      this.loading = true
      this.error = null
      try {
        const response = await api.get(`/media/search?keyword=${keyword}`)
        this.mediaList = response.data
      } catch (error) {
        this.error = error.message
        console.error('搜索媒体失败:', error)
      } finally {
        this.loading = false
      }
    }
  }
})