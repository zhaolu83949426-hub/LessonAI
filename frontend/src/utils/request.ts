import axios from 'axios'
import { showToast } from 'vant'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 60000
})

const getResponseMessage = (payload: unknown): string | undefined => {
  if (!payload || typeof payload !== 'object') {
    return undefined
  }
  const { message, msg } = payload as { message?: string; msg?: string }
  return message || msg
}

const isSuccessResponse = (payload: unknown): boolean => {
  if (!payload || typeof payload !== 'object') {
    return false
  }
  const typedPayload = payload as { success?: boolean; code?: number }
  if (typeof typedPayload.success === 'boolean') {
    return typedPayload.success
  }
  return typedPayload.code === 0
}

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
    const payload = response.data
    if (isSuccessResponse(payload)) {
      return payload.data
    }
    const message = getResponseMessage(payload) || '请求失败'
    showToast(message)
    if (response.status === 401) {
      localStorage.removeItem('token')
      router.replace('/login')
    }
    return Promise.reject(new Error(message))
  },
  (error) => {
    const message = getResponseMessage(error.response?.data) || error.message || '请求失败'
    showToast(message)
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      router.replace('/login')
    }
    return Promise.reject(error)
  }
)

export default request
