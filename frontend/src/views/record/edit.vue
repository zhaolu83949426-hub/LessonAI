<template>
  <div class="record-edit-page">
    <div class="top-wash"></div>
    <div class="content">
      <div class="header">
        <div class="title-wrap">
          <van-icon name="arrow-left" size="24" @click="$router.back()" />
          <h1 class="page-title">教案结果</h1>
        </div>
      </div>

      <div class="meta-card" v-if="record">
        <div class="meta-row">
          <span class="label">教案模板</span>
          <span class="value">{{ record.templateName || '未知' }}</span>
        </div>
        <div class="meta-row">
          <span class="label">生成模型</span>
          <span class="value">{{ record.modelName || '未知' }}</span>
        </div>
        <div class="meta-row">
          <span class="label">更新时间</span>
          <span class="value">{{ formatDate(record.updatedAt) }}</span>
        </div>
        <div class="meta-row">
          <span class="label">当前视图</span>
          <span class="value">{{ activeView === 'edited' ? '当前编辑稿' : '原始生成稿' }}</span>
        </div>
      </div>

      <div class="view-switch" v-if="record">
        <van-button size="small" round :type="activeView === 'edited' ? 'primary' : 'default'" @click="switchView('edited')">当前编辑稿</van-button>
        <van-button size="small" round :type="activeView === 'raw' ? 'primary' : 'default'" @click="switchView('raw')">原始生成稿</van-button>
        <van-button size="small" round plain type="primary" @click="backToSession">返回会话</van-button>
      </div>

      <div class="editor-wrap">
        <textarea
          v-model="editorContent"
          class="editor-input"
          :readonly="activeView === 'raw'"
          placeholder="暂无结果..."
        ></textarea>
      </div>

      <div class="bottom-actions">
        <van-button 
          block 
          round 
          type="primary" 
          @click="handleSave" 
          :disabled="activeView === 'raw'"
          :loading="saving"
          class="save-btn"
        >保存教案</van-button>
        <van-button 
          block 
          round 
          plain 
          type="primary" 
          @click="handleCopy" 
          class="copy-btn"
        >复制到剪贴板</van-button>
      </div>

      <div class="feedback-card" v-if="record">
        <div class="feedback-title">教案质量反馈</div>
        <van-rate v-model="feedbackForm.feedbackRating" />
        <van-radio-group v-model="feedbackForm.feedbackLevel" direction="horizontal" class="feedback-levels">
          <van-radio name="可直接使用">可直接使用</van-radio>
          <van-radio name="需少量修改">需少量修改</van-radio>
          <van-radio name="质量较差">质量较差</van-radio>
        </van-radio-group>
        <textarea v-model="feedbackForm.feedbackComment" class="feedback-input" placeholder="补充反馈说明（可选）"></textarea>
        <van-button block round plain type="primary" :loading="feedbackSaving" @click="handleSaveFeedback">保存反馈</van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getLessonRecordsBySession, updateLessonFeedback, updateLessonRecord } from '@/api'
import { showToast, showSuccessToast } from 'vant'

const route = useRoute()
const router = useRouter()
const sessionId = Number(route.params.id) 
const record = ref<any>(null)
const editedContent = ref('')
const saving = ref(false)
const activeView = ref<'edited' | 'raw'>('edited')
const feedbackSaving = ref(false)
const feedbackForm = ref({
  feedbackRating: 5,
  feedbackLevel: '可直接使用',
  feedbackComment: ''
})

const editorContent = computed({
  get: () => activeView.value === 'edited'
    ? editedContent.value
    : (record.value?.resultContent || ''),
  set: (value: string) => {
    if (activeView.value === 'edited') {
      editedContent.value = value
    }
  }
})

onMounted(async () => {
  try {
    const list: any = await getLessonRecordsBySession(sessionId)
    if (list && list.length > 0) {
      record.value = list[list.length - 1]
      editedContent.value = record.value.editedContent || record.value.resultContent
      feedbackForm.value.feedbackRating = record.value.feedbackRating || 5
      feedbackForm.value.feedbackLevel = record.value.feedbackLevel || '可直接使用'
      feedbackForm.value.feedbackComment = record.value.feedbackComment || ''
    }
  } catch (e) {
    showToast('加载详情失败')
  }
})

const handleSave = async () => {
  if (!record.value || activeView.value !== 'edited') return
  saving.value = true
  try {
    await updateLessonRecord(record.value.id, {
      editedContent: editedContent.value
    })
    record.value.editedContent = editedContent.value
    showSuccessToast('保存成功')
  } catch (e) {
    showToast('保存失败')
  } finally {
    saving.value = false
  }
}

const switchView = (view: 'edited' | 'raw') => {
  activeView.value = view
}

const handleSaveFeedback = async () => {
  if (!record.value) return
  feedbackSaving.value = true
  try {
    const updated = await updateLessonFeedback(record.value.id, feedbackForm.value)
    record.value = updated
    showSuccessToast('反馈已保存')
  } catch (_err) {
    showToast('反馈保存失败')
  } finally {
    feedbackSaving.value = false
  }
}

const backToSession = () => {
  router.push(`/session/${sessionId}`)
}

const handleCopy = () => {
  navigator.clipboard.writeText(editedContent.value).then(() => {
    showSuccessToast('已复制')
  }).catch(() => {
    showToast('复制失败')
  })
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}-${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}
</script>

<style scoped>
.record-edit-page {
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
  height: 156px;
  background-color: #FFF1DF;
  z-index: 0;
}
.content {
  position: relative;
  z-index: 1;
  padding: 62px 16px 20px;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #2F2A24;
}
.page-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
}
.meta-card {
  background: #FFFDF8;
  border: 1px solid #E6DFD4;
  border-radius: 22px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}
.meta-row {
  display: flex;
  justify-content: space-between;
}
.label {
  font-size: 14px;
  color: #6F675D;
}
.value {
  font-size: 14px;
  font-weight: 600;
  color: #2F2A24;
}

.editor-wrap {
  flex: 1;
  background: #FFFFFF;
  border: 1px solid #E6DFD4;
  border-radius: 24px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
}
.editor-input {
  flex: 1;
  width: 100%;
  border: none;
  resize: none;
  outline: none;
  font-size: 14px;
  line-height: 1.6;
  color: #2F2A24;
  background: transparent;
}

.bottom-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.save-btn {
  background-color: #2F7D4A;
  border-color: #2F7D4A;
  font-weight: 600;
}
.copy-btn {
  font-weight: 600;
}
.view-switch {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.feedback-card {
  margin-top: 16px;
  background: #fffdf8;
  border: 1px solid #e6dfd4;
  border-radius: 22px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.feedback-title {
  font-size: 16px;
  font-weight: 700;
  color: #2f2a24;
}

.feedback-levels {
  gap: 12px;
}

.feedback-input {
  min-height: 88px;
  border: 1px solid #e6dfd4;
  border-radius: 16px;
  padding: 12px;
  resize: none;
}
</style>
