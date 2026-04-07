<template>
  <div class="profile-page">
    <div class="top-wash"></div>
    <div class="content-wrapper">
      <div class="header">
        <div>
          <p class="eyebrow">Lesson AI</p>
          <h1 class="page-title">我的设置</h1>
        </div>
        <div class="avatar">{{ avatarText }}</div>
      </div>

      <div class="card info-card">
        <div class="card-title">账号信息</div>
        <van-field v-model="profileForm.nickname" label="昵称" placeholder="请输入昵称" />
        <van-field :model-value="userStore.userInfo.account" label="账号" readonly />
        <van-field
          v-model="profileForm.defaultTemplateId"
          is-link
          readonly
          label="默认模板"
          :placeholder="selectedTemplateName"
          @click="showTemplatePicker = true"
        />
        <van-field v-model="profileForm.defaultStyle" label="风格偏好" placeholder="如：温暖、结构清晰" />
        <van-button block round type="primary" class="save-btn" :loading="savingProfile" @click="handleSaveProfile">
          保存资料
        </van-button>
      </div>

      <div class="card password-card">
        <div class="card-title">修改密码</div>
        <van-field v-model="passwordForm.oldPassword" type="password" label="原密码" placeholder="请输入原密码" />
        <van-field v-model="passwordForm.newPassword" type="password" label="新密码" placeholder="请输入新密码" />
        <van-button block round plain type="primary" class="save-btn" :loading="savingPassword" @click="handleSavePassword">
          更新密码
        </van-button>
      </div>

      <div class="card logout-card" @click="handleLogout">
        <van-icon name="revoke" size="20" />
        <span>退出登录</span>
      </div>
    </div>

    <van-popup v-model:show="showTemplatePicker" position="bottom">
      <van-picker
        title="默认模板"
        :columns="templateOptions"
        @confirm="onConfirmTemplate"
        @cancel="showTemplatePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getTemplates, updateUserPassword, updateUserProfile } from '@/api'
import { useUserStore } from '@/stores/user'
import { showSuccessToast, showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()
const templates = ref<any[]>([])
const templateOptions = ref<any[]>([])
const showTemplatePicker = ref(false)
const savingProfile = ref(false)
const savingPassword = ref(false)

const profileForm = ref({
  nickname: '',
  defaultTemplateId: '',
  defaultStyle: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: ''
})

const avatarText = computed(() => (userStore.userInfo.nickname || userStore.userInfo.account || '我').slice(0, 1))
const selectedTemplateName = computed(() => {
  const current = templates.value.find(item => item.id === Number(profileForm.value.defaultTemplateId))
  return current?.name || '请选择默认模板'
})

const fillProfile = () => {
  profileForm.value.nickname = userStore.userInfo.nickname || ''
  profileForm.value.defaultTemplateId = userStore.userInfo.defaultTemplateId ? String(userStore.userInfo.defaultTemplateId) : ''
  profileForm.value.defaultStyle = userStore.userInfo.defaultStyle || ''
}

const loadTemplates = async () => {
  const list = await getTemplates()
  templates.value = list || []
  templateOptions.value = [{ text: '不设置', value: '' }, ...templates.value.map((item: any) => ({ text: item.name, value: String(item.id) }))]
}

onMounted(async () => {
  fillProfile()
  try {
    await loadTemplates()
  } catch (_err) {
    showToast('加载模板失败')
  }
})

const onConfirmTemplate = ({ selectedOptions }: any) => {
  profileForm.value.defaultTemplateId = selectedOptions[0]?.value || ''
  showTemplatePicker.value = false
}

const handleSaveProfile = async () => {
  if (!profileForm.value.nickname.trim()) {
    showToast('昵称不能为空')
    return
  }
  savingProfile.value = true
  try {
    const user = await updateUserProfile({
      nickname: profileForm.value.nickname,
      defaultTemplateId: profileForm.value.defaultTemplateId ? Number(profileForm.value.defaultTemplateId) : null,
      defaultStyle: profileForm.value.defaultStyle
    })
    userStore.setUserInfo(user)
    showSuccessToast('资料已更新')
  } catch (_err) {
    showToast('保存资料失败')
  } finally {
    savingProfile.value = false
  }
}

const handleSavePassword = async () => {
  if (!passwordForm.value.oldPassword.trim() || !passwordForm.value.newPassword.trim()) {
    showToast('请填写完整密码')
    return
  }
  savingPassword.value = true
  try {
    await updateUserPassword(passwordForm.value)
    passwordForm.value.oldPassword = ''
    passwordForm.value.newPassword = ''
    showSuccessToast('密码已更新')
  } catch (_err) {
    showToast('修改密码失败')
  } finally {
    savingPassword.value = false
  }
}

const handleLogout = () => {
  userStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  position: relative;
  background: #f5f1e8;
}
.top-wash {
  position: absolute;
  inset: 0 0 auto;
  height: 220px;
  background: linear-gradient(180deg, #d9efe3 0%, rgba(217, 239, 227, 0) 100%);
}
.content-wrapper {
  position: relative;
  z-index: 1;
  padding: 28px 16px 24px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.eyebrow {
  margin: 0 0 8px;
  color: #7c7568;
  font-size: 12px;
}
.page-title {
  margin: 0;
  font-size: 30px;
  color: #2f2a24;
}
.avatar {
  width: 52px;
  height: 52px;
  border-radius: 20px;
  background: #2f7d4a;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
}
.card {
  background: rgba(255, 253, 248, 0.92);
  border-radius: 24px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid #e8dece;
}
.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #2f2a24;
  margin-bottom: 12px;
}
.save-btn {
  margin-top: 16px;
}
.logout-card {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #a84b35;
  font-weight: 600;
}
</style>
