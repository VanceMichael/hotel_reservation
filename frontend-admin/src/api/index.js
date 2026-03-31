import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import router from '../router'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

api.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    if (code === 200) return data
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  error => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export const authApi = {
  login: data => api.post('/auth/login', data),
  getInfo: () => api.get('/auth/info')
}

export const userApi = {
  page: params => api.get('/admin/users', { params }),
  updateStatus: (id, status) => api.put(`/admin/users/${id}/status`, null, { params: { status } }),
  delete: id => api.delete(`/admin/users/${id}`)
}

export const hotelApi = {
  page: params => api.get('/hotels', { params }),
  getById: id => api.get(`/hotels/${id}`),
  create: data => api.post('/admin/hotels', data),
  update: (id, data) => api.put(`/admin/hotels/${id}`, data),
  delete: id => api.delete(`/admin/hotels/${id}`)
}

export const roomApi = {
  page: params => api.get('/rooms', { params }),
  getById: id => api.get(`/rooms/${id}`),
  create: data => api.post('/admin/rooms', data),
  update: (id, data) => api.put(`/admin/rooms/${id}`, data),
  delete: id => api.delete(`/admin/rooms/${id}`)
}

export const bookingApi = {
  page: params => api.get('/admin/bookings', { params }),
  updateStatus: (id, status) => api.put(`/admin/bookings/${id}/status`, null, { params: { status } })
}

export const logApi = {
  page: params => api.get('/admin/logs', { params })
}
