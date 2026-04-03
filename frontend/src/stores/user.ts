import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref({
    id: 0,
    account: '',
    nickname: ''
  })

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: any) => {
    userInfo.value = info
  }

  const logout = () => {
    token.value = ''
    userInfo.value = { id: 0, account: '', nickname: '' }
    localStorage.removeItem('token')
  }

  return { token, userInfo, setToken, setUserInfo, logout }
}, {
  persist: true
})
