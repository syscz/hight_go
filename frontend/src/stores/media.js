import { defineStore } from 'pinia'
import axios from 'axios'

// 配置axios基础URL
axios.defaults.baseURL = '/api'

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
        const response = await axios.get(url)
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
        const response = await axios.get('/albums')
        this.albums = response.data
      } catch (error) {
        console.error('获取相册列表失败:', error)
      }
    },

    async createAlbum(album) {
      try {
        const response = await axios.post('/albums', album)
        this.albums.push(response.data)
        return response.data
      } catch (error) {
        console.error('创建相册失败:', error)
        throw error
      }
    },

    async deleteAlbum(id) {
      try {
        await axios.delete(`/albums/${id}`)
        this.albums = this.albums.filter(a => a.id !== id)
      } catch (error) {
        console.error('删除相册失败:', error)
        throw error
      }
    },
    
    // 获取分类列表
    async fetchCategories() {
      try {
        const response = await axios.get('/categories')
        this.categories = response.data
      } catch (error) {
        console.error('获取分类列表失败:', error)
      }
    },
    
    // 获取标签列表
    async fetchTags() {
      try {
        const response = await axios.get('/tags')
        this.tags = response.data
      } catch (error) {
        console.error('获取标签列表失败:', error)
      }
    },
    
    // 上传媒体
    async uploadMedia(formData) {
      this.loading = true
      this.error = null
      try {
        const response = await axios.post('/media', formData, {
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
    
    // 删除媒体
    async deleteMedia(id) {
      try {
        await axios.delete(`/media/${id}`)
        this.mediaList = this.mediaList.filter(item => item.id !== id)
      } catch (error) {
        console.error('删除媒体失败:', error)
        throw error
      }
    },
    
    // 搜索媒体
    async searchMedia(keyword) {
      this.loading = true
      this.error = null
      try {
        const response = await axios.get(`/media/search?keyword=${keyword}`)
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
