<template>
  <div class="home-page">
    <div class="top-wash"></div>
    <div class="content-wrapper">
      <div class="header">
        <h1 class="page-title">会话列表</h1>
        <van-button 
          icon="plus" 
          type="primary" 
          size="small" 
          round 
          class="new-btn"
          @click="$router.push('/session/new')"
        >
          新建会话
        </van-button>
      </div>

      <div class="hint-card">
        包含所有历史生成的教案对话记录。
      </div>

      <div class="list-container">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="onLoad"
          >
            <div 
              class="session-item" 
              v-for="item in list" 
              :key="item.id"
              @click="$router.push(`/session/${item.id}`)"
            >
              <div class="item-header">
                <span class="session-title">{{ item.title || '新会话' }}</span>
                <span class="session-time">{{ formatDate(item.updatedAt) }}</span>
              </div>
              <div class="item-meta">
                <van-tag type="primary" plain size="medium" v-if="item.currentResultId">已生成结果</van-tag>
                <van-tag type="default" plain size="medium" v-else>草稿</van-tag>
              </div>
            </div>
          </van-list>
        </van-pull-refresh>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getSessions } from '@/api'
import { showToast } from 'vant'

const list = ref<any[]>([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

const onLoad = async () => {
  try {
    const res: any = await getSessions()
    if (refreshing.value) {
      list.value = []
      refreshing.value = false
    }
    // For MVP we assume it returns full array instead of pages
    list.value = res || []
    finished.value = true
  } catch (err: any) {
    finished.value = true
    showToast('获取会话列表失败')
  } finally {
    loading.value = false
  }
}

const onRefresh = () => {
  finished.value = false
  loading.value = true
  onLoad()
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}-${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}
</script>

<style scoped>
.home-page {
  position: relative;
  min-height: 100vh;
}
.top-wash {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 150px;
  background-color: #E8F4EC;
  z-index: 0;
}
.content-wrapper {
  position: relative;
  z-index: 1;
  padding: 62px 16px 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #2F2A24;
  margin: 0;
}
.new-btn {
  background-color: #2F7D4A;
  border: none;
}
.hint-card {
  background: #FFFDF8;
  border-radius: 18px;
  padding: 10px 14px;
  margin-bottom: 20px;
  border: 1px solid #E6DFD4;
  color: #6F675D;
  font-size: 13px;
  line-height: 1.5;
}
.list-container {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.session-item {
  background: #FFFFFF;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 14px;
  border: 1px solid #E6DFD4;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.session-title {
  font-size: 16px;
  font-weight: 600;
  color: #2F2A24;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.session-time {
  font-size: 12px;
  color: #9E968B;
  margin-left: 12px;
}
.item-meta {
  display: flex;
  gap: 8px;
}
</style>
