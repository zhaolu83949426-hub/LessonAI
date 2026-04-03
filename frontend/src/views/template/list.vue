<template>
  <div class="template-list-page">
    <div class="top-wash"></div>
    <div class="content-wrapper">
      <div class="header">
        <h1 class="page-title">模板列表</h1>
        <van-button 
          icon="plus" 
          type="primary" 
          size="small" 
          round 
          class="new-btn"
          @click="$router.push('/templates/new/edit')"
        >
          新建模板
        </van-button>
      </div>

      <div class="hint-card">
        自定义模板可以帮助每次教案生成更符合你的个人风格。
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
              class="template-item" 
              v-for="item in list" 
              :key="item.id"
              @click="$router.push(`/templates/${item.id}/edit`)"
            >
              <div class="title-row">
                <span class="title">{{ item.name }}</span>
                <van-icon name="arrow" color="#6F675D" />
              </div>
              <div class="desc-row">{{ item.content }}</div>
            </div>
          </van-list>
        </van-pull-refresh>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getTemplates } from '@/api'
import { showToast } from 'vant'

const list = ref<any[]>([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

const onLoad = async () => {
  try {
    const res: any = await getTemplates()
    if (refreshing.value) {
      list.value = []
      refreshing.value = false
    }
    list.value = res || []
    finished.value = true
  } catch (err: any) {
    finished.value = true
    showToast('获取模板列表失败')
  } finally {
    loading.value = false
  }
}

const onRefresh = () => {
  finished.value = false
  loading.value = true
  onLoad()
}
</script>

<style scoped>
.template-list-page {
  position: relative;
  min-height: 100vh;
}
.top-wash {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 144px;
  background-color: #FFF1DF;
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
.template-item {
  background: #FFFFFF;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 14px;
  border: 1px solid #E6DFD4;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.title {
  font-size: 16px;
  font-weight: 600;
  color: #2F2A24;
}
.desc-row {
  font-size: 13px;
  color: #6F675D;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
