import { defineStore } from 'pinia'
import { ref } from 'vue'

type UserInfo = {
  id: number
  account: string
  nickname: string
  defaultTemplateId: number | null
  defaultStyle: string
}

const EMPTY_USER: UserInfo = {
  id: 0,
  account: '',
  nickname: '',
  defaultTemplateId: null,
  defaultStyle: ''
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo>({ ...EMPTY_USER })

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: any) => {
    userInfo.value = {
      id: info?.id || 0,
      account: info?.account || '',
      nickname: info?.nickname || '',
      defaultTemplateId: info?.defaultTemplateId || null,
      defaultStyle: info?.defaultStyle || ''
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = { ...EMPTY_USER }
    localStorage.removeItem('token')
  }

  return { token, userInfo, setToken, setUserInfo, logout }
}, {
  persist: true
})
