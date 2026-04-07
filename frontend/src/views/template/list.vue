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
            <div class="template-item" v-for="item in list" :key="item.id">
              <div class="title-row" @click="$router.push(`/templates/${item.id}/edit`)">
                <span class="title">{{ item.name }}</span>
                <van-icon name="arrow" color="#6F675D" />
              </div>
              <div class="meta-row">
                <span class="meta-chip" v-if="item.category">{{ item.category }}</span>
                <span class="meta-chip" v-if="item.tags">{{ item.tags }}</span>
              </div>
              <div class="desc-row">{{ item.content }}</div>
              <div class="action-row">
                <van-button size="small" round plain type="primary" @click.stop="handleDuplicate(item.id)">模板化复用</van-button>
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
import { duplicateTemplate, getTemplates } from '@/api'
import { showSuccessToast, showToast } from 'vant'

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

const handleDuplicate = async (id: number) => {
  try {
    await duplicateTemplate(id)
    showSuccessToast('已生成复用模板')
    onRefresh()
  } catch (_err) {
    showToast('模板复用失败')
  }
}
</script>

<style scoped>
.template-list-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.top-wash {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 220px;
  background:
    radial-gradient(circle at top left, rgba(144, 204, 255, 0.42), transparent 35%),
    linear-gradient(180deg, rgba(201, 197, 255, 0.92), rgba(237, 248, 255, 0));
  z-index: 0;
}

.content-wrapper {
  position: relative;
  z-index: 1;
  padding: 28px 16px 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #1f1b37;
  margin: 0;
}

.new-btn {
  background: linear-gradient(135deg, #8b83f7, #6f67f8);
  border: none;
  box-shadow: 0 16px 24px rgba(111, 103, 248, 0.18);
}

.hint-card {
  background: rgba(255, 255, 255, 0.72);
  border-radius: 24px;
  padding: 14px 16px;
  margin-bottom: 20px;
  border: 1px solid rgba(124, 118, 255, 0.1);
  color: #6f6a93;
  font-size: 13px;
  line-height: 1.6;
  box-shadow: 0 16px 28px rgba(111, 103, 248, 0.08);
}

.list-container {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.template-item {
  background: rgba(255, 255, 255, 0.82);
  border-radius: 26px;
  padding: 18px;
  margin-bottom: 14px;
  border: 1px solid rgba(124, 118, 255, 0.1);
  box-shadow: 0 20px 34px rgba(111, 103, 248, 0.08);
  backdrop-filter: blur(10px);
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #1f1b37;
}

.desc-row {
  font-size: 13px;
  color: #76719a;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.6;
}

.meta-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.meta-chip {
  font-size: 12px;
  color: #6f675d;
  background: #f4ecdf;
  border-radius: 999px;
  padding: 4px 10px;
}

.action-row {
  margin-top: 12px;
}
</style>
