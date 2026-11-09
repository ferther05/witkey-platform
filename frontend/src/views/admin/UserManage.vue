<template>
  <div class="users">
    <h2 class="wk-page-title">用户管理</h2>
    <p class="wk-page-desc">封禁后该账号无法登录，也无法投标或交付</p>

    <div class="wk-card">
      <div class="toolbar">
        <el-input
          v-model="keyword" placeholder="搜索用户名或昵称" clearable class="search"
          @keyup.enter="search"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="search">搜索</el-button>
      </div>

      <el-table :data="list" v-loading="loading" empty-text="没有匹配的用户">
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="30" :src="row.avatar || ''">{{ (row.nickname || 'U').slice(0, 1) }}</el-avatar>
              <div>
                <div class="name">{{ row.nickname || row.username }}</div>
                <div class="wk-muted small">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="roleType(row.role)" effect="light">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="实名" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="authType(row.authStatus)" effect="plain">
              {{ authText(row.authStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="mobile" label="手机号" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '正常' : '已封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              v-if="row.role !== 'ADMIN'"
              size="small"
              :type="row.status === 1 ? 'danger' : 'success'"
              plain
              @click="toggle(row)"
            >
              {{ row.status === 1 ? '封禁' : '解封' }}
            </el-button>
            <span v-else class="wk-muted small">—</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > size"
        class="pager"
        layout="prev, pager, next, total"
        :current-page="page"
        :page-size="size"
        :total="total"
        @current-change="onPage"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminSetUserStatus, adminUsers } from '@/api/admin'
import { formatTime } from '@/utils/format'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const loading = ref(false)

const roleText = (r) => ({ EMPLOYER: '雇主', WITKEY: '威客', ADMIN: '管理员' }[r] || r)
const roleType = (r) => ({ EMPLOYER: 'warning', WITKEY: 'success', ADMIN: 'danger' }[r] || 'info')
const authText = (s) => ({ VERIFIED: '已认证', PENDING: '审核中', REJECTED: '已驳回' }[s] || '未认证')
const authType = (s) => ({ VERIFIED: 'success', PENDING: 'warning', REJECTED: 'danger' }[s] || 'info')

async function load() {
  loading.value = true
  try {
    const data = await adminUsers({ keyword: keyword.value || undefined, page: page.value, size: size.value })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

function onPage(p) {
  page.value = p
  load()
}

async function toggle(row) {
  const ban = row.status === 1
  try {
    await ElMessageBox.confirm(
      ban ? `封禁后「${row.nickname || row.username}」将无法登录和接单，确定吗？` : '解封后该账号可正常登录，确定吗？',
      ban ? '封禁用户' : '解封用户',
      { type: 'warning' }
    )
  } catch {
    return
  }
  await adminSetUserStatus(row.id, ban ? 0 : 1)
  ElMessage.success(ban ? '已封禁' : '已解封')
  await load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.search {
  width: 280px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.name {
  font-weight: 600;
  font-size: 14px;
}

.small {
  font-size: 12px;
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
