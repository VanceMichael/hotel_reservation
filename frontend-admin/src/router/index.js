import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'users', name: 'Users', component: () => import('../views/Users.vue'), meta: { title: '用户管理' } },
      { path: 'hotels', name: 'Hotels', component: () => import('../views/Hotels.vue'), meta: { title: '酒店管理' } },
      { path: 'rooms', name: 'Rooms', component: () => import('../views/Rooms.vue'), meta: { title: '房间管理' } },
      { path: 'bookings', name: 'Bookings', component: () => import('../views/Bookings.vue'), meta: { title: '预订管理' } },
      { path: 'logs', name: 'Logs', component: () => import('../views/Logs.vue'), meta: { title: '操作日志' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  if (to.path === '/login') {
    next()
  } else if (!userStore.token) {
    next('/login')
  } else {
    if (!userStore.userInfo) {
      try {
        await userStore.getInfo()
      } catch {
        userStore.logout()
        next('/login')
        return
      }
    }
    next()
  }
})

export default router
