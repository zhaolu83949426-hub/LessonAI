<template>
  <div class="home-page">
    <div class="home-glow home-glow-left"></div>
    <div class="home-glow home-glow-right"></div>
    <div class="content-wrapper">
      <div class="header">
        <div class="header-copy">
          <p class="eyebrow">Lesson AI</p>
          <h1 class="page-title">你好，{{ displayName }}!</h1>
          <p class="page-subtitle">把最近的教案会话整理成更轻巧的课程看板。</p>
        </div>
        <div class="header-actions">
          <button class="icon-btn" type="button" @click="$router.push('/templates')">
            <van-icon name="apps-o" />
          </button>
          <button class="profile-btn" type="button" @click="$router.push('/session/new')">
            {{ profileLetter }}
          </button>
        </div>
      </div>

      <div class="chip-row">
        <button
          v-for="chip in FILTER_OPTIONS"
          :key="chip.value"
          class="chip-btn"
          :class="{ active: activeFilter === chip.value }"
          type="button"
          @click="activeFilter = chip.value"
        >
          {{ chip.label }}
        </button>
      </div>

      <div class="hero-card" @click="goToFeatured">
        <div class="hero-copy">
          <span class="hero-badge">{{ featuredBadge }}</span>
          <h2 class="hero-title">{{ featuredTitle }}</h2>
          <p class="hero-desc">{{ featuredDescription }}</p>
          <div class="hero-meta">
            <div class="meta-pill">
              <span class="meta-value">{{ list.length }}</span>
              <span class="meta-label">全部会话</span>
            </div>
            <div class="meta-pill meta-pill-green">
              <span class="meta-value">{{ generatedCount }}</span>
              <span class="meta-label">已生成</span>
            </div>
          </div>
          <button class="hero-cta" type="button" @click.stop="goToFeatured">
            开始新会话
          </button>
        </div>
        <div class="hero-visual">
          <img
            class="hero-illustration"
            :src="heroIllustration"
            alt="课程学习插图"
          />
        </div>
      </div>

      <div class="section-head">
        <div>
          <p class="section-kicker">Recent sessions</p>
          <h2 class="section-title">继续你的课程设计</h2>
        </div>
        <button class="new-link" type="button" @click="$router.push('/session/new')">
          新建
        </button>
      </div>

      <div v-if="loading && !list.length" class="empty-state">
        <div class="empty-copy">
          <h3>加载中</h3>
          <p>正在整理最近的课程会话。</p>
        </div>
      </div>

      <div v-else-if="!filteredList.length" class="empty-state">
        <img
          class="empty-illustration"
          :src="emptyIllustration"
          alt="学习插图"
        />
        <div class="empty-copy">
          <h3>还没有会话</h3>
          <p>从一个新的教学主题开始，把目标、重点和活动流程交给 AI 一起整理。</p>
        </div>
        <button class="empty-cta" type="button" @click="$router.push('/session/new')">
          开始创建
        </button>
      </div>

      <div v-else class="card-grid">
        <article
          v-for="(item, index) in filteredList"
          :key="item.id"
          class="session-card"
          :class="cardThemes[index % cardThemes.length]"
          @click="$router.push(`/session/${item.id}`)"
        >
          <div class="card-copy">
            <span class="card-tag">{{ item.currentResultId ? '已生成' : '草稿中' }}</span>
            <h3 class="card-title">{{ item.title || '未命名会话' }}</h3>
            <p class="card-desc">{{ getSessionDescription(item) }}</p>
            <div class="card-footer">
              <span class="card-time">{{ formatDate(item.updatedAt) }}</span>
              <van-icon name="arrow" />
            </div>
          </div>
          <img
            class="card-illustration"
            :src="getCardIllustration(index)"
            alt="课程卡片插图"
          />
        </article>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getSessions } from '@/api'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

type SessionItem = {
  id: number
  title?: string
  updatedAt?: string
  currentResultId?: number | null
}

const FILTER_OPTIONS = [
  { label: '全部', value: 'all' },
  { label: '已生成', value: 'done' },
  { label: '草稿', value: 'draft' },
  { label: '今日更新', value: 'today' }
] as const

const ILLUSTRATION_BASE = `${import.meta.env.BASE_URL}illustrations/`
const CARD_ILLUSTRATIONS = [
  `${ILLUSTRATION_BASE}card-sketching.svg`,
  `${ILLUSTRATION_BASE}card-education.svg`
]
const heroIllustration = `${ILLUSTRATION_BASE}hero-learning.svg`
const emptyIllustration = `${ILLUSTRATION_BASE}card-education.svg`

const userStore = useUserStore()
const router = useRouter()
const list = ref<SessionItem[]>([])
const loading = ref(false)
const activeFilter = ref<(typeof FILTER_OPTIONS)[number]['value']>('all')

const cardThemes = ['card-theme-lilac', 'card-theme-mint', 'card-theme-peach']

const displayName = computed(() => userStore.userInfo.nickname || userStore.userInfo.account || '老师')
const profileLetter = computed(() => displayName.value.substring(0, 1))

const filteredList = computed(() => {
  if (activeFilter.value === 'done') {
    return list.value.filter(item => item.currentResultId)
  }
  if (activeFilter.value === 'draft') {
    return list.value.filter(item => !item.currentResultId)
  }
  if (activeFilter.value === 'today') {
    const today = new Date().toDateString()
    return list.value.filter(item => new Date(item.updatedAt || '').toDateString() === today)
  }
  return list.value
})

const generatedCount = computed(() => list.value.filter(item => item.currentResultId).length)
const draftCount = computed(() => list.value.filter(item => !item.currentResultId).length)

const featuredBadge = '快速入口'
const featuredTitle = '从一个新的教学主题开始'
const featuredDescription = computed(
  () => `直接发起新会话，把主题、班级、目标和重点交给 AI 整理。当前还有 ${draftCount.value} 个草稿可继续完善。`
)

const onLoad = async () => {
  try {
    loading.value = true
    const res = await getSessions()
    list.value = (res || []) as SessionItem[]
  } catch (_err) {
    showToast('获取会话列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  onLoad()
})

const goToFeatured = () => {
  router.push('/session/new')
}

const getCardIllustration = (index: number) => CARD_ILLUSTRATIONS[index % CARD_ILLUSTRATIONS.length]

const getSessionDescription = (item: SessionItem) => {
  if (item.currentResultId) {
    return '教案结果已生成，可以继续润色目标、过程和材料准备。'
  }
  return '还在整理需求草稿，点进去继续完善课程主题与教学重点。'
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}-${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d
    .getMinutes()
    .toString()
    .padStart(2, '0')}`
}
</script>

<style scoped src="./index.css"></style>
<style scoped src="./index-cards.css"></style>
