<template>
  <div class="admin">
    <aside class="sidebar">
      <div class="brand" @click="router.push('/home')">
        <span class="logo">威</span>
        <span class="name">威客帮 · 后台</span>
      </div>

      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/admin/tasks">
          <el-icon><DocumentChecked /></el-icon>
          <span>任务审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/reports">
          <el-icon><Warning /></el-icon>
          <span>举报处理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Grid /></el-icon>
          <span>分类维护</span>
        </el-menu-item>
        <el-menu-item index="/admin/finance">
          <el-icon><Money /></el-icon>
          <span>财务总览</span>
        </el-menu-item>
        <el-menu-item index="/admin/files">
          <el-icon><Folder /></el-icon>
          <span>文件管理</span>
        </el-menu-item>
      </el-menu>

      <div class="bottom">
        <el-button text @click="router.push('/home')">
          <el-icon><Back /></el-icon> 返回前台
        </el-button>
      </div>
    </aside>

    <div class="body">
      <header class="bar">
        <div class="crumb">{{ route.meta.title || '后台' }}</div>
        <div class="user">
          <el-avatar :size="26">{{ (store.profile?.nickname || 'A').slice(0, 1) }}</el-avatar>
          <span>{{ store.profile?.nickname || store.profile?.username }}</span>
          <el-tag size="small" type="danger" effect="light">管理员</el-tag>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

onMounted(async () => {
  if (!store.profile) {
    await store.fetchProfile().catch(() => {})
  }
  if (store.profile && store.profile.role !== 'ADMIN') {
    ElMessage.error('没有后台访问权限')
    router.replace('/home')
  }
})
</script>

<style scoped>
.admin {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 210px;
  background: #fff;
  border-right: 1px solid var(--wk-border);
  display: flex;
  flex-direction: column;
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
  cursor: pointer;
  border-bottom: 1px solid var(--wk-border);
}

.logo {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: var(--wk-primary);
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 700;
}

.name {
  font-weight: 600;
  font-size: 14px;
}

.menu {
  border-right: none;
  padding: 8px;
  flex: 1;
}

.bottom {
  padding: 12px 18px;
  border-top: 1px solid var(--wk-border);
}

.body {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--wk-bg);
}

.bar {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid var(--wk-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.crumb {
  font-weight: 600;
}

.user {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}

.content {
  padding: 24px;
  flex: 1;
}
</style>
