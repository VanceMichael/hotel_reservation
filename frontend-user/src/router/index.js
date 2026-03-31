import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  { path: '/hotels', name: 'Hotels', component: () => import('../views/Hotels.vue') },
  { path: '/hotels/:id', name: 'HotelDetail', component: () => import('../views/HotelDetail.vue') },
  { path: '/booking/:roomId', name: 'Booking', component: () => import('../views/Booking.vue'), meta: { auth: true } },
  { path: '/bookings', name: 'MyBookings', component: () => import('../views/MyBookings.vue'), meta: { auth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.auth && !userStore.token) {
    next('/login')
  } else {
    if (userStore.token && !userStore.userInfo) {
      try {
        await userStore.getInfo()
      } catch {
        userStore.logout()
      }
    }
    next()
  }
})

export default router
