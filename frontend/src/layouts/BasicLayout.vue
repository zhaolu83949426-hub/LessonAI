<template>
  <div class="basic-layout">
    <div class="layout-content">
      <router-view />
    </div>
    <div class="bottom-nav-wrap">
      <div class="bottom-nav">
        <div 
          class="nav-item" 
          :class="{ active: route.path === '/' }" 
          @click="router.push('/')"
        >
          <van-icon name="chat-o" size="24" />
          <span>会话</span>
        </div>
        <div 
          class="nav-item" 
          :class="{ active: route.path.startsWith('/templates') }" 
          @click="router.push('/templates')"
        >
          <van-icon name="apps-o" size="24" />
          <span>模板</span>
        </div>
        <div class="nav-item" @click="handleLogout">
          <van-icon name="setting-o" size="24" />
          <span>退出</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.basic-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #F7F4EE;
}
.layout-content {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 90px;
}
.bottom-nav-wrap {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background: transparent;
}
.bottom-nav {
  background: #FFFDF8;
  border-radius: 31px;
  padding: 8px;
  display: flex;
  height: 62px;
  box-sizing: border-box;
  align-items: center;
  justify-content: space-around;
  border: 1px solid #E6DFD4;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6F675D;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
}
.nav-item.active {
  background: #E8F4EC;
  color: #2F7D4A;
  font-weight: 600;
}
</style>
