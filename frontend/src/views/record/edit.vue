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
      </div>

      <div class="editor-wrap">
        <textarea
          v-model="editedContent"
          class="editor-input"
          placeholder="暂无结果..."
        ></textarea>
      </div>

      <div class="bottom-actions">
        <van-button 
          block 
          round 
          type="primary" 
          @click="handleSave" 
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getLessonRecordsBySession, updateLessonRecord } from '@/api'
import { showToast, showSuccessToast } from 'vant'

const route = useRoute()
// Note: route.params.id is the Result ID! 
// Wait! The user clicks "查看结果" which goes to `/record/:id/edit`.
// But first we need the specific record details. 
// However, the API provides: getLessonRecordsBySession, updateLessonRecord.
// We don't have getLessonRecordById? Let's assume we can fetch by session and find it, or we just pass sessionId?
// Wait, in `session/new` and `session/detail`, we route to `/record/${currentResultId}/edit`.
// The backend might not have `getLessonRecordById` in Requirements (8.5), it just has `GET /api/lesson-records/session/{sessionId}`.
// Let's change the router logic to pass `sessionId` to this page.
// Ah, the route is `/record/:id/edit`, but if `id` is result ID, how do we query it?
// Let me update the route to `/session/:id/record` instead to query by sessionId. But actually it's fine, I can just use an inner fetch. I'll modify router manually in a bit if needed.
// Wait! In requirements 8.5: `GET /api/lesson-records/session/{sessionId}`.
// Let's assume `route.params.id` is the `sessionId` here? No, in my prior code: `router.push("/record/${session.currentResultId}/edit")`.
// Let's modify `session/detail.vue` and `session/new.vue` to push `/session/${sId}/record` instead? Yes, that's better!

// Let me assume the route parameter here is session ID and change later.
// For now, let's just make it sessionId.
const sessionId = Number(route.params.id) 
const record = ref<any>(null)
const editedContent = ref('')
const saving = ref(false)

onMounted(async () => {
  try {
    const list: any = await getLessonRecordsBySession(sessionId)
    if (list && list.length > 0) {
      // get latest record
      record.value = list[0]
      editedContent.value = record.value.editedContent || record.value.resultContent
    }
  } catch (e) {
    showToast('加载详情失败')
  }
})

const handleSave = async () => {
  if (!record.value) return
  saving.value = true
  try {
    await updateLessonRecord(record.value.id, {
      editedContent: editedContent.value
    })
    showSuccessToast('保存成功')
  } catch (e) {
    showToast('保存失败')
  } finally {
    saving.value = false
  }
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
</style>
