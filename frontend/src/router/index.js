import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/album/:id',
    name: 'Album',
    component: () => import('../views/Album.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/upload',
    name: 'Upload',
    component: () => import('../views/Upload.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/media/:id',
    name: 'MediaDetail',
    component: () => import('../views/MediaDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/auth',
    name: 'Auth',
    component: () => import('../views/Auth.vue'),
    meta: { layout: 'bare' }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('../views/Favorites.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/timeline',
    name: 'Timeline',
    component: () => import('../views/Timeline.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/trash',
    name: 'Trash',
    component: () => import('../views/Trash.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('../views/Search.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/calendar',
    name: 'Calendar',
    component: () => import('../views/Calendar.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/public',
    name: 'PublicGallery',
    component: () => import('../views/PublicGallery.vue'),
    meta: { layout: 'square' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    // 未登录:访问根路径去公开广场(游客可看),其他受保护页才跳登录
    if (to.path === '/') {
      next({ name: 'PublicGallery' })
    } else {
      next({ name: 'Auth' })
    }
  } else if (to.name === 'Auth' && token) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
