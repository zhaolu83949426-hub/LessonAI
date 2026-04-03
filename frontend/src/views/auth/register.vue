<template>
  <div class="auth-page">
    <div class="auth-glow auth-glow-left"></div>
    <div class="auth-glow auth-glow-right"></div>
    <div class="content-wrapper">
      <div class="brand-card">
        <div class="brand-copy">
          <p class="eyebrow">Lesson AI</p>
          <h1 class="title">创建账号</h1>
          <p class="subtitle">保持和首页一致的课程看板风格，从注册开始就进入同一套体验。</p>
        </div>
        <div class="brand-visual">
          <img
            class="brand-illustration"
            src="/illustrations/card-sketching.svg"
            alt="注册页插图"
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
            <p class="field-label">昵称</p>
            <van-field
              v-model="nickname"
              name="nickname"
              placeholder="请输入昵称"
              :rules="[{ required: true, message: '请填写昵称' }]"
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
              注册
            </van-button>
          </div>
        </van-form>
      </div>
      
      <div class="link-wrap">
        <span class="link-text" @click="$router.push('/login')">已有账号？返回登录</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api'
import { showSuccessToast } from 'vant'

const router = useRouter()

const account = ref('')
const nickname = ref('')
const password = ref('')
const loading = ref(false)

const onSubmit = async () => {
  try {
    loading.value = true
    await register({ account: account.value, nickname: nickname.value, password: password.value })
    showSuccessToast('注册成功，请登录')
    router.push('/login')
  } catch (err: any) {
    // handled in request interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped src="./auth.css"></style>
