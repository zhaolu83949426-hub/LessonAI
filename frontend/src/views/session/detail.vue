<template>
  <div class="session-detail-page">
    <div class="top-wash"></div>
    <div class="content">
      <div class="header">
        <div class="title-wrap">
          <van-icon class="back-icon" name="arrow-left" size="24" @click="$router.push('/')" />
          <h1 class="page-title">{{ session?.title || '会话详情' }}</h1>
        </div>
      </div>

      <div class="meta-card" v-if="session">
        <div class="meta-row">
          <span class="label">教案模板</span>
          <span class="value">{{ session.templateName || '未知' }}</span>
        </div>
        <div class="meta-row">
          <span class="label">生成模型</span>
          <span class="value">{{ session.llmConfigName || '未知' }}</span>
        </div>
      </div>

      <div class="chat-timeline" ref="timelineRef">
        <div 
          v-for="msg in messages" 
          :key="msg.id" 
          :class="['chat-bubble-wrap', normalizeRole(msg.role)]"
        >
          <div class="chat-message">
            <div class="chat-bubble">
              {{ msg.content }}
            </div>
            <div class="message-time">{{ formatMessageTime(msg.createdAt || msg.updatedAt) }}</div>
          </div>
        </div>
      </div>

      <div class="composer-wrap">
        <div class="view-plan-pill" v-if="session?.currentResultId" @click="goToResult">
          <span class="pill-icon">📄</span>查看结果
        </div>

        <div class="composer">
          <input 
            type="text" 
            v-model="inputMsg" 
            placeholder="继续追问..." 
            class="msg-input"
            @keyup.enter="handleSend"
          />
          <button class="send-btn" @click="handleSend" :disabled="!inputMsg.trim() || sending">发送</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSessionById, getSessionMessages, generateLesson } from '@/api'
import { showToast, showLoadingToast, closeToast } from 'vant'

const route = useRoute()
const router = useRouter()
const sessionId = Number(route.params.id)

const session = ref<any>(null)
const messages = ref<any[]>([])
const inputMsg = ref('')
const sending = ref(false)
const timelineRef = ref<HTMLElement | null>(null)

const loadData = async () => {
  try {
    session.value = await getSessionById(sessionId)
    messages.value = await getSessionMessages(sessionId)
    scrollToBottom()
  } catch (e: any) {
    showToast('加载失败')
  }
}

onMounted(() => {
  loadData()
})

const scrollToBottom = () => {
  nextTick(() => {
    if (timelineRef.value) {
      timelineRef.value.scrollTop = timelineRef.value.scrollHeight
    }
  })
}

const handleSend = async () => {
  if (!inputMsg.value.trim() || sending.value) return
  
  const msgContent = inputMsg.value
  messages.value.push({
    id: Date.now(),
    role: 'user',
    content: msgContent,
    createdAt: new Date().toISOString()
  })
  inputMsg.value = ''
  scrollToBottom()

  sending.value = true
  showLoadingToast({ message: '生成中...', forbidClick: true, duration: 0 })

  try {
    await generateLesson({
      sessionId: sessionId,
      topic: session.value?.title || msgContent.substring(0, 100),
      userMessage: msgContent
    })
    
    // 生成成功，重新拉取拉取会话和消息（主要是更新了 currentResultId）
    await loadData()
    closeToast()
  } catch (err: any) {
    closeToast()
    showToast('回复失败: ' + (err.message || 'Error'))
    // 移除本地发出的消息？ MVP 暂时不管
  } finally {
    sending.value = false
  }
}

const goToResult = () => {
  if (session.value?.currentResultId) {
    router.push(`/session/${sessionId}/record`)
  }
}

const formatMessageTime = (dateStr?: string) => {
  if (!dateStr) return ''

  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return ''

  const now = new Date()
  const isToday =
    date.getFullYear() === now.getFullYear() &&
    date.getMonth() === now.getMonth() &&
    date.getDate() === now.getDate()

  const time = `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  if (isToday) return time

  return `${date.getMonth() + 1}-${date.getDate()} ${time}`
}

const normalizeRole = (role: string) => role?.toLowerCase() || ''
</script>

<style scoped>
.session-detail-page {
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
  background-color: #E8F4EC;
  z-index: 0;
}
.content {
  position: relative;
  z-index: 1;
  padding: 94px 16px 120px;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 18px 16px 14px;
  background: rgba(247, 244, 238, 0.94);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(230, 223, 212, 0.9);
  margin-bottom: 24px;
  z-index: 20;
}
.title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #2F2A24;
  width: 100%;
  min-width: 0;
}
.back-icon {
  flex-shrink: 0;
}
.page-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
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

.chat-timeline {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 20px;
}
.chat-bubble-wrap {
  display: flex;
}
.chat-bubble-wrap.user {
  justify-content: flex-end;
}
.chat-bubble-wrap.assistant {
  justify-content: flex-start;
}
.chat-message {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 80%;
}
.chat-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
}
.message-time {
  font-size: 12px;
  color: #9E968B;
  line-height: 1;
}
.chat-bubble-wrap.user .chat-bubble {
  background: #2F7D4A;
  color: #FFFFFF;
  border-bottom-right-radius: 4px;
}
.chat-bubble-wrap.user .message-time {
  text-align: right;
}
.chat-bubble-wrap.assistant .chat-bubble {
  background: #FFFFFF;
  color: #2F2A24;
  border: 1px solid #E6DFD4;
  border-bottom-left-radius: 4px;
}

.composer-wrap {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background: linear-gradient(180deg, rgba(247, 244, 238, 0) 0%, #F7F4EE 30%);
}
.view-plan-pill {
  position: absolute;
  right: 16px;
  bottom: 84px;
  background: #2F7D4A;
  border-radius: 24px;
  padding: 10px 16px;
  color: #FFFFFF;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 8px 16px rgba(47, 125, 74, 0.3);
  cursor: pointer;
  z-index: 10;
}
.pill-icon {
  font-size: 16px;
}

.composer {
  background: #FFFDF8;
  border: 1px solid #E6DFD4;
  border-radius: 20px;
  padding: 8px;
  display: flex;
  gap: 10px;
  align-items: center;
}
.msg-input {
  flex: 1;
  background: #F6F8F7;
  border: none;
  border-radius: 16px;
  padding: 14px;
  font-size: 14px;
  outline: none;
}
.send-btn {
  background: #2F7D4A;
  color: #fff;
  border: none;
  border-radius: 16px;
  padding: 0 20px;
  height: 44px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
}
.send-btn:disabled {
  background: #A5CBAF;
  cursor: not-allowed;
}
</style>
