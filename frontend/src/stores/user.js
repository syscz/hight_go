import { defineStore } from 'pinia'

function loadFromStorage() {
  try {
    return JSON.parse(localStorage.getItem('user') || 'null')
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    user: loadFromStorage()
  }),

  getters: {
    isLoggedIn: (state) => !!state.user,
    displayName: (state) => state.user?.username || ''
  },

  actions: {
    setUser(user) {
      this.user = user
      if (user) {
        localStorage.setItem('user', JSON.stringify(user))
      } else {
        localStorage.removeItem('user')
      }
    },

    /** 用后端返回的 UserVO 合并更新本地用户（头像/背景等） */
    applyUserVO(vo) {
      if (!vo) return
      if (this.user) {
        this.setUser({ ...this.user, ...vo })
      } else {
        this.setUser({ id: vo.id, username: vo.username, ...vo })
      }
    },

    clear() {
      this.setUser(null)
    }
  }
})
