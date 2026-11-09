<template>
  <div class="admin">
    <aside class="sidebar" :class="{ collapsed }">
      <div class="brand" @click="router.push('/home')">
        <span class="logo">威</span>
        <span v-if="!collapsed" class="name">威客帮 · 后台</span>
      </div>

      <el-menu :default-active="route.path" router class="menu" :collapse="collapsed"
               :collapse-transition="false">
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataLine /></el-icon>
          <template #title>数据看板</template>
        </el-menu-item>
        <el-menu-item index="/admin/tasks">
          <el-icon><DocumentChecked /></el-icon>
          <template #title>任务审核</template>
        </el-menu-item>
        <el-menu-item index="/admin/reports">
          <el-icon><Warning /></el-icon>
          <template #title>举报处理</template>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Grid /></el-icon>
          <template #title>分类维护</template>
        </el-menu-item>
        <el-menu-item index="/admin/finance">
          <el-icon><Money /></el-icon>
          <template #title>财务总览</template>
        </el-menu-item>
        <el-menu-item index="/admin/files">
          <el-icon><Folder /></el-icon>
          <template #title>文件管理</template>
        </el-menu-item>
      </el-menu>

      <div class="bottom">
        <el-button text @click="router.push('/home')">
          <el-icon><Back /></el-icon>
          <span v-if="!collapsed">返回前台</span>
        </el-button>
      </div>
    </aside>

    <div class="body">
      <header class="bar">
        <div class="bar-left">
          <el-button text circle class="collapse-btn" @click="toggleCollapse">
            <el-icon :size="16">
              <Expand v-if="collapsed" />
              <Fold v-else />
            </el-icon>
          </el-button>
          <div class="crumb">{{ route.meta.title || '后台' }}</div>
        </div>
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
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const KEY = 'witkey_admin_sidebar_collapsed'
const collapsed = ref(localStorage.getItem(KEY) === '1')

function toggleCollapse() {
  collapsed.value = !collapsed.value
  localStorage.setItem(KEY, collapsed.value ? '1' : '0')
}

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
  transition: width 0.2s ease;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar.collapsed .brand {
  padding: 0 17px;
}

.sidebar.collapsed .menu {
  padding: 8px 4px;
}

.sidebar.collapsed .bottom {
  padding: 12px 8px;
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

.bar-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.collapse-btn {
  color: var(--wk-text-3);
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
