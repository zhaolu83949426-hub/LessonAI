<template>
  <div class="auth-page">
    <div class="top-wash"></div>
    <div class="content-wrapper">
      <div class="brand-card">
        <h1 class="title">智能教案生成</h1>
        <p class="subtitle">AI赋能，让幼教更轻松</p>
      </div>

      <div class="login-card">
        <van-form @submit="onSubmit" class="form">
          <van-field
            v-model="account"
            name="account"
            placeholder="请输入账号"
            :rules="[{ required: true, message: '请填写账号' }]"
            class="input-field"
          />
          <van-field
            v-model="password"
            type="password"
            name="password"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请填写密码' }]"
            class="input-field"
          />
          <div class="submit-btn-wrap">
            <van-button round block type="primary" native-type="submit" :loading="loading" class="submit-btn">
              登录
            </van-button>
          </div>
        </van-form>
      </div>
      
      <div class="link-wrap">
        <span class="link-text" @click="$router.push('/register')">还没有账号？去注册</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login, getUserInfo } from '@/api'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const account = ref('')
const password = ref('')
const loading = ref(false)

const onSubmit = async () => {
  try {
    loading.value = true
    const res: any = await login({ account: account.value, password: password.value })
    if (res && res.token) {
      userStore.setToken(res.token)
      showToast('登录成功')
      const userInfo = await getUserInfo()
      userStore.setUserInfo(userInfo)
      router.push('/')
    } else {
      showToast('登录返回数据异常')
    }
  } catch (err: any) {
    // handled in request interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  background-color: #F7F4EE;
  display: flex;
  flex-direction: column;
}
.top-wash {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 212px;
  background-color: #E8F4EC;
  z-index: 0;
}
.content-wrapper {
  position: relative;
  z-index: 1;
  padding: 88px 16px 0;
}
.brand-card {
  background: #FFFDF8;
  border-radius: 24px;
  padding: 32px 20px;
  margin-bottom: 32px;
  border: 1px solid #DCEADB;
  text-align: center;
}
.title {
  font-size: 24px;
  font-weight: 700;
  color: #2F2A24;
  margin: 0 0 8px;
}
.subtitle {
  font-size: 14px;
  color: #6F675D;
  margin: 0;
}
.login-card {
  background: #FFFFFF;
  border-radius: 24px;
  padding: 32px 20px;
  border: 1px solid #E6DFD4;
}
.input-field {
  background: #F6F8F7;
  border-radius: 12px;
  margin-bottom: 20px;
  padding: 16px;
}
.submit-btn-wrap {
  margin-top: 32px;
}
.submit-btn {
  background-color: #2F7D4A;
  border: none;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}
.link-wrap {
  text-align: center;
  margin-top: 24px;
}
.link-text {
  color: #2F7D4A;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}
</style>
