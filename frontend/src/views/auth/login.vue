<template>
  <div class="auth-page">
    <div class="auth-glow auth-glow-left"></div>
    <div class="auth-glow auth-glow-right"></div>
    <div class="content-wrapper">
      <div class="brand-card">
        <div class="brand-copy">
          <p class="eyebrow">Lesson AI</p>
          <h1 class="title">欢迎回来</h1>
          <p class="subtitle">继续用更轻巧的方式整理课程目标、活动流程和教案生成会话。</p>
        </div>
        <div class="brand-visual">
          <img
            class="brand-illustration"
            src="/illustrations/hero-learning.svg"
            alt="登录页插图"
          />
        </div>
      </div>

      <div class="login-card">
        <van-form @submit="onSubmit" class="form">
          <div class="field-wrap">
            <p class="field-label">账号</p>
            <van-field
              v-model="account"
              name="account"
              placeholder="请输入账号"
              :rules="[{ required: true, message: '请填写账号' }]"
              class="input-field"
            />
          </div>
          <div class="field-wrap">
            <p class="field-label">密码</p>
            <van-field
              v-model="password"
              type="password"
              name="password"
              placeholder="请输入密码"
              :rules="[{ required: true, message: '请填写密码' }]"
              class="input-field"
            />
          </div>
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

<style scoped src="./auth.css"></style>
