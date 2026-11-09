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
          <!-- 与上方标签保持一致，避免"审核中"被显示成"未认证" -->
          <div class="num">{{ authTag.text }}</div>
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

    <!-- 我的待办 -->
    <section v-if="todos.toAccept || todos.toDeliver" class="wk-card todo-card">
      <div class="todo-head">
        <h3>待我处理</h3>
        <span class="wk-muted small">点卡片直接进入订单列表</span>
      </div>
      <div class="todo-grid">
        <div v-if="todos.toAccept" class="todo-item" @click="router.push('/orders')">
          <div class="todo-num">{{ todos.toAccept }}</div>
          <div>
            <div class="todo-title">个订单待你验收</div>
            <div class="wk-muted small">威客已交付，验收通过即结算</div>
          </div>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
        <div v-if="todos.toDeliver" class="todo-item" @click="router.push('/orders')">
          <div class="todo-num">{{ todos.toDeliver }}</div>
          <div>
            <div class="todo-title">个订单待你交付</div>
            <div class="wk-muted small">尽快提交成果，避免超时</div>
          </div>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </section>

    <!-- 平台概览 -->
    <section class="wk-card overview">
      <div class="ov-head">
        <h3>平台概览</h3>
        <span class="wk-muted small">只统计已完成订单，托管中的金额不计入成交</span>
      </div>
      <div class="ov-grid">
        <div class="ov">
          <div class="ov-num">{{ overview.recruitingCount }}</div>
          <div class="ov-label">任务招募中</div>
        </div>
        <div class="ov">
          <div class="ov-num">{{ overview.doneOrderCount }}</div>
          <div class="ov-label">已完成订单</div>
        </div>
        <div class="ov">
          <div class="ov-num primary">¥ {{ formatMoney(overview.gmv) }}</div>
          <div class="ov-label">累计成交额</div>
        </div>
        <div class="ov">
          <div class="ov-num">{{ overview.userCount }}</div>
          <div class="ov-label">平台用户</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUnreadCount } from '@/api/notice'
import { getOverview, getTodoCounts } from '@/api/home'
import { formatMoney } from '@/utils/format'

const router = useRouter()
const store = useUserStore()
const unread = ref(0)

const overview = reactive({
  recruitingCount: 0,
  doneOrderCount: 0,
  gmv: 0,
  userCount: 0
})
const todos = reactive({ toAccept: 0, toDeliver: 0 })

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

  Object.assign(overview, await getOverview().catch(() => overview))
  const todo = await getTodoCounts().catch(() => null)
  if (todo) Object.assign(todos, todo)
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

/* 待我处理 */
.todo-head,
.ov-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.todo-head h3,
.ov-head h3 {
  margin: 0;
  font-size: 15px;
}

.small {
  font-size: 12px;
}

.todo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 14px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  border-radius: 12px;
  background: var(--wk-primary-light);
  border: 1px solid #ffe0c7;
  cursor: pointer;
  transition: transform 0.18s, box-shadow 0.18s;
}

.todo-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 107, 0, 0.12);
}

.todo-num {
  font-size: 26px;
  font-weight: 700;
  color: var(--wk-primary);
  min-width: 34px;
  text-align: center;
}

.todo-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 2px;
}

.arrow {
  margin-left: auto;
  color: var(--wk-primary);
}

/* 平台概览 */
.ov-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.ov {
  background: #fafbfc;
  border-radius: 12px;
  padding: 18px 20px;
}

.ov-num {
  font-size: 24px;
  font-weight: 700;
  color: var(--wk-text-1);
}

.ov-num.primary {
  color: var(--wk-primary);
}

.ov-label {
  margin-top: 6px;
  font-size: 12px;
  color: var(--wk-text-3);
}

@media (max-width: 1000px) {
  .ov-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
