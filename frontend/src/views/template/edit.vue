<template>
  <div class="template-edit-page">
    <div class="top-wash"></div>
    <div class="content">
      <div class="header">
        <div class="title-wrap">
          <van-icon name="arrow-left" size="24" @click="$router.push('/templates')" />
          <h1 class="page-title">{{ isNew ? '新建模板' : '编辑模板' }}</h1>
        </div>
        <div class="delete-icon" v-if="!isNew" @click="handleDelete">
          <van-icon name="delete-o" size="20" color="#EE0A24" />
        </div>
      </div>

      <div class="meta-card">
        <van-field
          v-model="formData.name"
          placeholder="请输入模板名称"
          class="name-input"
          :border="false"
        />
        <van-field v-model="formData.category" placeholder="请输入模板分类" :border="false" />
        <van-field v-model="formData.tags" placeholder="请输入模板标签，多个用逗号分隔" :border="false" />
      </div>

      <div class="editor-wrap">
        <textarea
          v-model="formData.content"
          class="editor-input"
          placeholder="请输入模板提示词内容，例如输出结构、语气要求等..."
        ></textarea>
      </div>

      <div class="hint-card">
        模板文本将作为系统设定，约束 AI 生成的格式与语气。
      </div>

      <div class="bottom-actions">
        <van-button 
          block 
          round 
          type="primary" 
          @click="handleSave" 
          :loading="saving"
          class="save-btn"
        >保存模板</van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTemplateById, createTemplate, updateTemplate, deleteTemplate } from '@/api'
import { showToast, showSuccessToast, showConfirmDialog } from 'vant'

const route = useRoute()
const router = useRouter()
const idParam = route.params.id as string

const isNew = computed(() => idParam === 'new')

const formData = ref({
  name: '',
  content: '',
  category: '',
  tags: ''
})
const saving = ref(false)

onMounted(async () => {
  if (!isNew.value) {
    try {
      const res: any = await getTemplateById(idParam)
      formData.value.name = res.name
      formData.value.content = res.content
      formData.value.category = res.category || ''
      formData.value.tags = res.tags || ''
    } catch (e) {
      showToast('获取细节失败')
    }
  }
})

const handleSave = async () => {
  if (!formData.value.name.trim() || !formData.value.content.trim()) {
    showToast('名称和内容不能为空')
    return
  }
  saving.value = true
  try {
    if (isNew.value) {
      await createTemplate(formData.value)
    } else {
      await updateTemplate(idParam, formData.value)
    }
    showSuccessToast('保存成功')
    router.back()
  } catch (e) {
    showToast('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = () => {
  showConfirmDialog({
    title: '确认删除',
    message: '删除后无法恢复，确定删除该模板吗？'
  }).then(async () => {
    try {
      await deleteTemplate(idParam)
      showSuccessToast('删除成功')
      router.push('/templates')
    } catch (e) {
      showToast('删除失败')
    }
  }).catch(() => {
    // cancel
  })
}
</script>

<style scoped>
.template-edit-page {
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
  height: 154px;
  background-color: #E8F4EC;
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
.delete-icon {
  padding: 8px;
  cursor: pointer;
}
.meta-card {
  background: #FFFDF8;
  border: 1px solid #E6DFD4;
  border-radius: 20px;
  margin-bottom: 16px;
  overflow: hidden;
}
.name-input {
  background: transparent;
  font-size: 16px;
  font-weight: 600;
}

.editor-wrap {
  flex: 1;
  background: #FFFFFF;
  border: 1px solid #E6DFD4;
  border-radius: 24px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
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

.hint-card {
  background: #FFF1DF;
  border-radius: 16px;
  padding: 10px 14px;
  margin-bottom: 20px;
  color: #6F675D;
  font-size: 13px;
  line-height: 1.5;
}

.bottom-actions {
  display: flex;
}
.save-btn {
  background-color: #2F7D4A;
  border-color: #2F7D4A;
  font-weight: 600;
  height: 54px;
}
</style>
