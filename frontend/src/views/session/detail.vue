<template>
  <div class="session-detail-page">
    <div class="detail-glow detail-glow-left"></div>
    <div class="detail-glow detail-glow-right"></div>
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

      <div class="chat-panel">
        <div class="chat-timeline">
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
          <div ref="bottomAnchorRef" class="bottom-anchor"></div>
        </div>
      </div>

      <div class="composer-wrap">
        <div class="view-plan-pill" v-if="session?.currentResultId" @click="goToResult">
          <van-icon name="notes-o" size="16" />
          <span>查看结果</span>
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
const bottomAnchorRef = ref<HTMLElement | null>(null)

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
    requestAnimationFrame(() => {
      bottomAnchorRef.value?.scrollIntoView({
        behavior: 'smooth',
        block: 'end'
      })
    })
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

<style scoped src="./detail.css"></style>
