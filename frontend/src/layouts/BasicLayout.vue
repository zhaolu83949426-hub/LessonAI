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
        <div
          class="nav-item"
          :class="{ active: route.path.startsWith('/profile') }"
          @click="router.push('/profile')"
        >
          <van-icon name="setting-o" size="24" />
          <span>我的</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent;
}

.layout-content {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 132px;
  min-height: 0;
  -webkit-overflow-scrolling: touch;
}

.bottom-nav-wrap {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 0 16px calc(16px + env(safe-area-inset-bottom));
  background: transparent;
  pointer-events: none;
  z-index: 1000;
}

.bottom-nav {
  background: rgba(255, 255, 255, 0.74);
  backdrop-filter: blur(18px);
  border-radius: 28px;
  padding: 10px;
  display: flex;
  min-height: 72px;
  align-items: center;
  justify-content: space-around;
  border: 1px solid rgba(124, 118, 255, 0.12);
  box-shadow: 0 18px 36px rgba(111, 103, 248, 0.16);
  pointer-events: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 6px;
  min-width: 88px;
  color: #7b78a0;
  font-size: 12px;
  font-weight: 500;
  padding: 10px 14px;
  border-radius: 20px;
  cursor: pointer;
  transition: background-color 0.25s ease, color 0.25s ease,
    transform 0.25s ease;
}

.nav-item.active {
  background: linear-gradient(135deg, rgba(139, 131, 247, 0.16), rgba(142, 216, 180, 0.18));
  color: #49439d;
  font-weight: 600;
  transform: translateY(-2px);
}

.nav-item :deep(.van-icon) {
  font-size: 22px;
}
</style>
