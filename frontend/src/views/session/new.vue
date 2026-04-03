<template>
  <div class="session-new-page">
    <div class="top-wash"></div>
    <div class="content">
      <div class="header">
        <div class="title-wrap">
          <van-icon name="arrow-left" size="24" @click="$router.back()" />
          <h1 class="page-title">新建会话</h1>
        </div>
        <div class="user-badge" v-if="userStore.userInfo.nickname">
          {{ userStore.userInfo.nickname.substring(0, 1) }}
        </div>
      </div>

      <div class="main-area">
        <!-- 模板选择 -->
        <div class="selector-card" @click="showTemplatePicker = true">
          <div class="card-left">
            <div class="label">选择教案模板</div>
            <div class="value">{{ currentTemplate?.name || '请选择模板' }}</div>
          </div>
          <div class="card-right">切换</div>
        </div>

        <!-- 模型选择 -->
        <div class="selector-card" @click="showModelPicker = true">
          <div class="card-left">
            <div class="label">选择生成模型</div>
            <div class="value">{{ currentModel?.name || '请选择模型' }}</div>
          </div>
          <div class="card-right">切换</div>
        </div>

        <!-- 说明 -->
        <div class="guide-card">
          发送后开始对话，生成完成后可查看教案。
        </div>

        <div class="chat-empty">
          <div class="empty-block">
            <div class="empty-title">系统</div>
            <div class="empty-desc">已选择模板和模型，<br/>发送第一条消息即可开始生成。</div>
          </div>
          <div class="example-block">
            先写主题、班级、重点和目标。<br/>
            例如：生成中班春天观察活动教案。
          </div>
        </div>
      </div>

      <div class="composer-wrap">
        <!-- 悬浮的查看教案入口 (假设施加判断) -->
        <div class="view-plan-pill" v-if="currentResultId" @click="goToResult">
          <van-icon name="notes-o" size="16" />
          <span>查看教案</span>
        </div>

        <div class="composer">
          <input 
            type="text" 
            v-model="inputMsg" 
            placeholder="输入需求，点击发送开始对话..." 
            class="msg-input"
            @keyup.enter="handleSend"
          />
          <button class="send-btn" @click="handleSend" :disabled="!inputMsg.trim() || sending">发送</button>
        </div>
      </div>
    </div>

    <!-- 底部弹窗选择器 -->
    <van-popup v-model:show="showTemplatePicker" position="bottom">
      <van-picker
        title="选择模板"
        :columns="templateOptions"
        @confirm="onConfirmTemplate"
        @cancel="showTemplatePicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showModelPicker" position="bottom">
      <van-picker
        title="选择模型"
        :columns="modelOptions"
        @confirm="onConfirmModel"
        @cancel="showModelPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getTemplates, getLlmOptions, getDefaultLlm, createSession, generateLesson } from '@/api'
import { showToast, showLoadingToast, closeToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const templates = ref<any[]>([])
const models = ref<any[]>([])

const templateOptions = ref<any[]>([])
const modelOptions = ref<any[]>([])

const currentTemplate = ref<any>(null)
const currentModel = ref<any>(null)

const showTemplatePicker = ref(false)
const showModelPicker = ref(false)

const inputMsg = ref('')
const sending = ref(false)

// 为了展示右下角漂浮按钮，这里提供一个变量，实际第一次创建会话前是空
const currentResultId = ref<number | null>(null)
const sessionId = ref<number | null>(null)

onMounted(async () => {
  try {
    const defaultLlm: any = await getDefaultLlm()
    if (defaultLlm) currentModel.value = defaultLlm

    const[tpls, mdls] = await Promise.all([getTemplates(), getLlmOptions()])
    templates.value = tpls || []
    models.value = mdls || []

    templateOptions.value = templates.value.map(t => ({ text: t.name, value: t.id }))
    modelOptions.value = models.value.map(m => ({ text: m.name, value: m.id }))

    if (!currentTemplate.value && templates.value.length > 0) {
      currentTemplate.value = templates.value[0]
    }
  } catch (e) {
    showToast('加载配置失败')
  }
})

const onConfirmTemplate = ({ selectedOptions }: any) => {
  currentTemplate.value = templates.value.find(t => t.id === selectedOptions[0].value)
  showTemplatePicker.value = false
}

const onConfirmModel = ({ selectedOptions }: any) => {
  currentModel.value = models.value.find(m => m.id === selectedOptions[0].value)
  showModelPicker.value = false
}

const handleSend = async () => {
  if (!inputMsg.value.trim()) return
  if (!currentTemplate.value || !currentModel.value) {
    showToast('请先选择模板和大模型')
    return
  }

  sending.value = true
  showLoadingToast({ message: '生成中...', forbidClick: true, duration: 0 })

  try {
    // 1. 创建会话 (如果还没有)
    let sId = sessionId.value
    if (!sId) {
      const sessionRet: any = await createSession({
        title: inputMsg.value.substring(0, 15),
        templateId: currentTemplate.value.id,
        llmConfigId: currentModel.value.id
      })
      sId = Number(sessionRet)
      sessionId.value = sId
    }

    // 2. 发送生成请求
    const generateRet: any = await generateLesson({
      sessionId: sId,
      topic: inputMsg.value.substring(0, 100),
      userMessage: inputMsg.value
    })
    currentResultId.value = generateRet?.id ?? null
    
    // 生成成功，跳转会话详情页
    closeToast()
    showToast('生成成功')
    router.replace(`/session/${sId}`)

  } catch (err: any) {
    closeToast()
    showToast('生成失败: ' + (err.message || 'Error'))
  } finally {
    sending.value = false
  }
}

const goToResult = () => {
  if (sessionId.value) {
    router.push(`/session/${sessionId.value}/record`)
  }
}
</script>

<style scoped src="./new.css"></style>
