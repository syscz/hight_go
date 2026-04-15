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
    component: () => import('../views/Auth.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next({ name: 'Auth' })
  } else if (to.name === 'Auth' && token) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router