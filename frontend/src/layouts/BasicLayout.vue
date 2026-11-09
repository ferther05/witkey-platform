<template>
  <div class="layout">
    <header class="hdr">
      <div class="wk-container hdr-inner">
        <div class="brand" @click="router.push('/home')">
          <span class="logo">威</span>
          <span class="brand-name">威客帮</span>
        </div>

        <nav class="nav">
          <router-link to="/home" class="nav-item" :class="{ active: route.path === '/home' }">
            首页
          </router-link>
          <router-link to="/tasks" class="nav-item" :class="{ active: route.path.startsWith('/tasks') }">
            任务大厅
          </router-link>
          <router-link to="/my-tasks" class="nav-item" :class="{ active: route.path === '/my-tasks' }">
            我的任务
          </router-link>
          <router-link to="/notice" class="nav-item" :class="{ active: route.path === '/notice' }">
            消息中心
          </router-link>
        </nav>

        <div class="right">
          <el-badge :value="unread" :hidden="unread === 0" :max="99" class="bell">
            <el-button text circle @click="router.push('/notice')">
              <el-icon :size="18"><Bell /></el-icon>
            </el-button>
          </el-badge>

          <el-dropdown trigger="click" @command="onCommand">
            <span class="user">
              <el-avatar :size="30" :src="store.profile?.avatar || ''">
                {{ firstChar }}
              </el-avatar>
              <span class="uname">{{ store.profile?.nickname || store.profile?.username || '...' }}</span>
              <el-tag size="small" :type="store.identityLabel === '威客' ? 'success' : 'warning'" effect="light">
                {{ store.identityLabel }}
              </el-tag>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="myTasks">我的任务</el-dropdown-item>
                <el-dropdown-item command="myBids">我的投标</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item command="wallet">我的钱包</el-dropdown-item>
                <el-dropdown-item command="notice">消息中心</el-dropdown-item>
                <el-dropdown-item v-if="store.profile?.role === 'ADMIN'" command="admin" divided>
                  后台管理
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="main">
      <div class="wk-container">
        <router-view />
      </div>
    </main>

    <footer class="ftr">威客帮 · 任务协作平台 · 毕业设计演示项目</footer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUnreadCount } from '@/api/notice'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const unread = ref(0)

const firstChar = computed(() => {
  const name = store.profile?.nickname || store.profile?.username || 'U'
  return name.slice(0, 1).toUpperCase()
})

async function refreshUnread() {
  try {
    const data = await getUnreadCount()
    unread.value = data.count || 0
  } catch {
    unread.value = 0
  }
}

function onCommand(cmd) {
  if (cmd === 'profile') router.push('/profile')
  if (cmd === 'myTasks') router.push('/my-tasks')
  if (cmd === 'myBids') router.push('/my-bids')
  if (cmd === 'orders') router.push('/orders')
  if (cmd === 'wallet') router.push('/wallet')
  if (cmd === 'notice') router.push('/notice')
  if (cmd === 'admin') router.push('/admin')
  if (cmd === 'logout') {
    store.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}

onMounted(async () => {
  if (!store.profile) {
    await store.fetchProfile().catch(() => {})
  }
  await refreshUnread()
})
</script>

<style scoped>
.layout {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.hdr {
  background: #fff;
  border-bottom: 1px solid var(--wk-border);
  position: sticky;
  top: 0;
  z-index: 10;
}

.hdr-inner {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 40px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo {
  width: 32px;
  height: 32px;
  border-radius: 9px;
  background: var(--wk-primary);
  color: #fff;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.brand-name {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
}

.nav {
  display: flex;
  gap: 8px;
  flex: 1;
}

.nav-item {
  padding: 6px 14px;
  border-radius: 8px;
  color: var(--wk-text-2);
  font-size: 14px;
  transition: all 0.2s;
}

.nav-item:hover {
  background: var(--wk-primary-light);
  color: var(--wk-primary);
}

.nav-item.active {
  color: var(--wk-primary);
  background: var(--wk-primary-light);
  font-weight: 600;
}

.nav-item.disabled {
  color: #c9cdd4;
  cursor: not-allowed;
}

.right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.uname {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--wk-text-1);
}

.main {
  flex: 1;
  padding: 28px 0 40px;
}

.ftr {
  text-align: center;
  color: var(--wk-text-3);
  font-size: 12px;
  padding: 20px 0 28px;
}
</style>
