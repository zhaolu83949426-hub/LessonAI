import axios from 'axios'
import { showToast } from 'vant'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 60000
})

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const { code, msg, data } = response.data
    if (code === 0) {
      return data
    } else {
      showToast(msg || '请求失败')
      if (code === 401) {
        localStorage.removeItem('token')
        router.replace('/login')
      }
      return Promise.reject(new Error(msg || 'Error'))
    }
  },
  (error) => {
    showToast(error.message || '请求失败')
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      router.replace('/login')
    }
    return Promise.reject(error)
  }
)

export default request
