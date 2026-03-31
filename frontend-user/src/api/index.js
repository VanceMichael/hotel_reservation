import axios from 'axios'
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
    return Promise.reject(new Error(message))
  },
  error => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export const authApi = {
  login: data => api.post('/auth/login', data),
  register: data => api.post('/auth/register', data),
  getInfo: () => api.get('/auth/info')
}

export const hotelApi = {
  page: params => api.get('/hotels', { params }),
  getById: id => api.get(`/hotels/${id}`),
  getRooms: hotelId => api.get(`/hotels/${hotelId}/rooms`)
}

export const roomApi = {
  getById: id => api.get(`/rooms/${id}`)
}

export const bookingApi = {
  page: params => api.get('/bookings', { params }),
  create: data => api.post('/bookings', data),
  cancel: id => api.put(`/bookings/${id}/cancel`)
}
