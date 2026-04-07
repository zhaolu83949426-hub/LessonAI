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

        <div class="draft-bar" v-if="hasDraftCache">
          <span>已恢复上次草稿</span>
          <button class="draft-clear-btn" type="button" @click="clearDraft">清空草稿</button>
        </div>

        <!-- 说明 -->
        <div class="guide-card">
          {{ sendingHint }}
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
import { computed, ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getTemplates, getLlmOptions, getDefaultLlm, createSession, generateLesson } from '@/api'
import { showToast, showLoadingToast, closeToast } from 'vant'

const SESSION_DRAFT_KEY = 'lesson-ai-session-draft'
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
const generatingStage = ref('')

const currentResultId = ref<number | null>(null)
const sessionId = ref<number | null>(null)
const hasDraftCache = ref(false)

const sendingHint = computed(() => generatingStage.value || '发送后开始对话，生成完成后可查看教案。')

const saveDraft = () => {
  localStorage.setItem(SESSION_DRAFT_KEY, JSON.stringify({
    inputMsg: inputMsg.value,
    templateId: currentTemplate.value?.id || null,
    modelId: currentModel.value?.id || null
  }))
  hasDraftCache.value = Boolean(inputMsg.value.trim() || currentTemplate.value?.id || currentModel.value?.id)
}

const restoreDraft = () => {
  const raw = localStorage.getItem(SESSION_DRAFT_KEY)
  if (!raw) {
    return
  }
  try {
    const draft = JSON.parse(raw)
    inputMsg.value = draft.inputMsg || ''
    if (draft.templateId) {
      currentTemplate.value = templates.value.find(item => item.id === draft.templateId) || currentTemplate.value
    }
    if (draft.modelId) {
      currentModel.value = models.value.find(item => item.id === draft.modelId) || currentModel.value
    }
    hasDraftCache.value = Boolean(draft.inputMsg || draft.templateId || draft.modelId)
  } catch (_err) {
    localStorage.removeItem(SESSION_DRAFT_KEY)
  }
}

const clearDraft = () => {
  localStorage.removeItem(SESSION_DRAFT_KEY)
  inputMsg.value = ''
  hasDraftCache.value = false
}

onMounted(async () => {
  try {
    const defaultLlm: any = await getDefaultLlm()
    if (defaultLlm) currentModel.value = defaultLlm

    const[tpls, mdls] = await Promise.all([getTemplates(), getLlmOptions()])
    templates.value = tpls || []
    models.value = mdls || []

    templateOptions.value = templates.value.map(t => ({ text: t.name, value: t.id }))
    modelOptions.value = models.value.map(m => ({ text: m.name, value: m.id }))

    if (!currentTemplate.value && userStore.userInfo.defaultTemplateId) {
      currentTemplate.value = templates.value.find(t => t.id === userStore.userInfo.defaultTemplateId) || null
    }
    if (!currentTemplate.value && templates.value.length > 0) {
      currentTemplate.value = templates.value[0]
    }
    restoreDraft()
  } catch (e) {
    showToast('加载配置失败')
  }
})

watch([inputMsg, currentTemplate, currentModel], () => saveDraft(), { deep: true })

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
  generatingStage.value = '正在创建会话...'
  showLoadingToast({ message: generatingStage.value, forbidClick: true, duration: 0 })

  try {
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

    generatingStage.value = '正在生成教案内容...'
    showLoadingToast({ message: generatingStage.value, forbidClick: true, duration: 0 })
    const generateRet: any = await generateLesson({
      sessionId: sId,
      topic: inputMsg.value.substring(0, 100),
      userMessage: inputMsg.value
    })
    currentResultId.value = generateRet?.id ?? null
    localStorage.removeItem(SESSION_DRAFT_KEY)
    hasDraftCache.value = false
    closeToast()
    showToast('生成成功')
    router.replace(`/session/${sId}`)

  } catch (err: any) {
    closeToast()
    showToast('生成失败: ' + (err.message || 'Error'))
  } finally {
    generatingStage.value = ''
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
<style scoped>
.draft-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  background: #fff5df;
  color: #6f675d;
  border-radius: 16px;
  padding: 10px 14px;
}

.draft-clear-btn {
  border: none;
  background: transparent;
  color: #2f7d4a;
  font-weight: 600;
}
</style>
