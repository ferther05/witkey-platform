<template>
  <div class="home">
    <section class="hero wk-card">
      <div class="hero-left">
        <h2>你好，{{ store.profile?.nickname || store.profile?.username || '朋友' }} 👋</h2>
        <p class="wk-muted">
          当前身份
          <el-tag size="small" :type="store.identityLabel === '威客' ? 'success' : 'warning'">
            {{ store.identityLabel }}
          </el-tag>
          <span class="dot">·</span>
          实名状态
          <el-tag size="small" :type="authTag.type">{{ authTag.text }}</el-tag>
        </p>
        <div class="hero-actions">
          <el-button v-if="store.profile?.identityType === 'EMPLOYER'" type="primary" @click="router.push('/tasks/publish')">
            发布任务
          </el-button>
          <el-button v-else type="primary" @click="router.push('/tasks')">去任务大厅接单</el-button>
          <el-button @click="router.push('/wallet')">我的钱包</el-button>
        </div>
      </div>
      <div class="hero-right">
        <div class="stat">
          <div class="num">{{ store.profile?.authStatus === 'VERIFIED' ? '已认证' : '未认证' }}</div>
          <div class="label">实名认证</div>
        </div>
        <div class="stat">
          <div class="num">{{ store.profile?.role === 'ADMIN' ? '管理员' : '普通用户' }}</div>
          <div class="label">账号角色</div>
        </div>
        <div class="stat">
          <div class="num">{{ unread }}</div>
          <div class="label">未读消息</div>
        </div>
      </div>
    </section>

    <section class="grid">
      <div class="entry wk-card" @click="router.push('/profile')">
        <el-icon :size="28" color="#ff6b00"><User /></el-icon>
        <div>
          <h3>个人中心</h3>
          <p class="wk-muted">资料、实名认证、密码与身份切换</p>
        </div>
      </div>
      <div class="entry wk-card" @click="router.push('/notice')">
        <el-icon :size="28" color="#ff6b00"><Bell /></el-icon>
        <div>
          <h3>消息中心</h3>
          <p class="wk-muted">审核结果、中标、验收与结算通知</p>
        </div>
      </div>
      <div class="entry wk-card" @click="router.push('/tasks')">
        <el-icon :size="28" color="#ff6b00"><Document /></el-icon>
        <div>
          <h3>任务大厅</h3>
          <p class="wk-muted">浏览正在招募的任务，赏金已托管</p>
        </div>
      </div>
    </section>

    <section class="wk-card roadmap">
      <h3>开发进度</h3>
      <el-steps :active="2" finish-status="success" align-center>
        <el-step title="用户与权限" description="已完成" />
        <el-step title="任务管理" description="进行中" />
        <el-step title="投标与交付" description="待开始" />
        <el-step title="担保交易与资金" description="待开始" />
        <el-step title="后台与看板" description="待开始" />
      </el-steps>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUnreadCount } from '@/api/notice'

const router = useRouter()
const store = useUserStore()
const unread = ref(0)

const authTag = computed(() => {
  switch (store.profile?.authStatus) {
    case 'VERIFIED':
      return { text: '已认证', type: 'success' }
    case 'PENDING':
      return { text: '审核中', type: 'warning' }
    case 'REJECTED':
      return { text: '已驳回', type: 'danger' }
    default:
      return { text: '未认证', type: 'info' }
  }
})

onMounted(async () => {
  if (!store.profile) await store.fetchProfile().catch(() => {})
  const data = await getUnreadCount().catch(() => ({ count: 0 }))
  unread.value = data.count || 0
})
</script>

<style scoped>
.home {
  display: grid;
  gap: 20px;
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}

.hero h2 {
  margin: 0 0 8px;
  font-size: 22px;
}

.hero-actions {
  margin-top: 18px;
  display: flex;
  gap: 12px;
}

.dot {
  margin: 0 8px;
}

.hero-right {
  display: flex;
  gap: 32px;
  align-items: center;
}

.stat {
  text-align: center;
}

.stat .num {
  font-size: 20px;
  font-weight: 700;
  color: var(--wk-text-1);
}

.stat .label {
  font-size: 12px;
  color: var(--wk-text-3);
  margin-top: 4px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.entry {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.entry:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.09);
}

.entry.disabled {
  cursor: not-allowed;
  opacity: 0.75;
}

.entry h3 {
  margin: 0 0 6px;
  font-size: 15px;
}

.entry p {
  margin: 0;
  font-size: 12px;
}

.roadmap h3 {
  margin: 0 0 24px;
  font-size: 15px;
}
</style>
