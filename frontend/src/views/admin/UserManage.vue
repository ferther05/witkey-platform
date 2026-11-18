<!-- views/admin/UserManage.vue：页面：后台用户管理（搜索、详情、封禁/解禁、实名审核、重置密码） -->
<template>
  <div class="users">
    <h2 class="wk-page-title">用户管理</h2>
    <p class="wk-page-desc">
      可查看用户详情、审核实名认证、重置密码；封禁后该账号无法登录，也无法投标或交付
    </p>

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
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="roleType(row.role)" effect="light">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="实名认证" width="150">
          <template #default="{ row }">
            <el-tooltip :content="row.authRemark || '暂无审核意见'" placement="top">
              <el-tag size="small" :type="authType(row.authStatus)" effect="plain">
                {{ authText(row.authStatus) }}
              </el-tag>
            </el-tooltip>
            <el-button
              v-if="row.authStatus === 'PENDING'"
              size="small"
              type="primary"
              link
              class="ml6"
              @click="openAuth(row)"
            >
              去审核
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="mobile" label="手机号" width="130" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '正常' : '已封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button size="small" plain @click="openDetail(row)">详情</el-button>
            <el-button size="small" plain @click="openReset(row)">重置密码</el-button>
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

    <!-- 用户详情抽屉 -->
    <el-drawer v-model="detailVisible" title="用户详情" size="420px">
      <div v-if="detail" class="detail">
        <div class="d-head">
          <el-avatar :size="54" :src="detail.user.avatar || ''">
            {{ (detail.user.nickname || 'U').slice(0, 1) }}
          </el-avatar>
          <div>
            <div class="d-name">{{ detail.user.nickname || detail.user.username }}</div>
            <div class="wk-muted small">
              @{{ detail.user.username }} · {{ roleText(detail.user.role) }} · {{ identityText(detail.user.identityType) }}
            </div>
          </div>
        </div>

        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="手机号">{{ detail.user.mobile || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实名认证">
            {{ authText(detail.user.authStatus) }}
            <span v-if="detail.user.realName">（{{ detail.user.realName }}）</span>
          </el-descriptions-item>
          <el-descriptions-item label="审核意见">{{ detail.user.authRemark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatTime(detail.user.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">{{ detail.user.status === 1 ? '正常' : '已封禁' }}</el-descriptions-item>
        </el-descriptions>

        <div class="d-title">业务统计</div>
        <div class="stat-grid">
          <div class="stat"><b>{{ detail.employerTaskCount }}</b><span>发布任务</span></div>
          <div class="stat"><b>{{ detail.employerOrderCount }}</b><span>雇主订单</span></div>
          <div class="stat"><b>{{ detail.witkeyOrderCount }}</b><span>威客订单</span></div>
          <div class="stat"><b>{{ detail.bidCount }}</b><span>投标次数</span></div>
          <div class="stat"><b>{{ detail.doneAsEmployer }}</b><span>雇主完成</span></div>
          <div class="stat"><b>{{ detail.doneAsWitkey }}</b><span>威客完成</span></div>
        </div>

        <div class="d-title">钱包</div>
        <div class="stat-grid">
          <div class="stat"><b>¥ {{ formatMoney(detail.balance) }}</b><span>可用余额</span></div>
          <div class="stat"><b>¥ {{ formatMoney(detail.frozen) }}</b><span>托管冻结</span></div>
        </div>
      </div>
    </el-drawer>

    <!-- 实名审核弹窗 -->
    <el-dialog v-model="authVisible" title="实名认证审核" width="460px">
      <div v-if="authRow" class="auth-body">
        <p class="wk-muted small">
          申请人：<b>{{ authRow.nickname || authRow.username }}</b>
          · 真实姓名 {{ authRow.realName || '-' }}
          · 手机号 {{ authRow.mobile || '-' }}
        </p>
        <el-radio-group v-model="authPass" class="auth-radio">
          <el-radio-button :value="true">通过</el-radio-button>
          <el-radio-button :value="false">驳回</el-radio-button>
        </el-radio-group>
        <el-input
          v-model="authRemark"
          type="textarea"
          :rows="3"
          :placeholder="authPass ? '审核意见（可选）' : '请填写驳回原因（必填），用户可在个人中心看到'"
        />
      </div>
      <template #footer>
        <el-button @click="authVisible = false">取消</el-button>
        <el-button type="primary" :loading="authLoading" @click="submitAuth">提交</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetVisible" title="重置登录密码" width="420px">
      <p class="wk-muted small">为「{{ resetRow?.nickname || resetRow?.username }}」设置新的登录密码（6-32 位）</p>
      <el-input v-model="newPassword" placeholder="新密码" show-password maxlength="32" />
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetLoading" @click="submitReset">确定重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminResetUserPassword,
  adminReviewAuth,
  adminSetUserStatus,
  adminUserDetail,
  adminUsers
} from '@/api/admin'
import { formatMoney, formatTime } from '@/utils/format'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const loading = ref(false)

// 详情抽屉
const detailVisible = ref(false)
const detail = ref(null)

// 实名审核
const authVisible = ref(false)
const authRow = ref(null)
const authPass = ref(true)
const authRemark = ref('')
const authLoading = ref(false)

// 重置密码
const resetVisible = ref(false)
const resetRow = ref(null)
const newPassword = ref('')
const resetLoading = ref(false)

// 角色转文字
const roleText = (r) => ({ EMPLOYER: '雇主', WITKEY: '威客', ADMIN: '管理员' }[r] || r)
// 角色转标签颜色
const roleType = (r) => ({ EMPLOYER: 'warning', WITKEY: 'success', ADMIN: 'danger' }[r] || 'info')
// 身份转文字
const identityText = (t) => ({ EMPLOYER: '雇主', WITKEY: '威客' }[t] || t || '-')
// 实名认证状态转文字
const authText = (s) => ({ VERIFIED: '已认证', PENDING: '审核中', REJECTED: '已驳回' }[s] || '未认证')
// 实名认证状态转标签颜色
const authType = (s) => ({ VERIFIED: 'success', PENDING: 'warning', REJECTED: 'danger' }[s] || 'info')

// 加载列表
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

// 搜索（回到第一页）
function search() {
  page.value = 1
  load()
}

// 翻页
function onPage(p) {
  page.value = p
  load()
}

// 打开用户详情
async function openDetail(row) {
  detail.value = await adminUserDetail(row.id)
  detailVisible.value = true
}

// 打开发放审核弹窗
function openAuth(row) {
  authRow.value = row
  authPass.value = true
  authRemark.value = ''
  authVisible.value = true
}

// 提交实名审核
async function submitAuth() {
  if (!authPass.value && !authRemark.value.trim()) {
    return ElMessage.warning('驳回需要填写原因')
  }
  authLoading.value = true
  try {
    await adminReviewAuth(authRow.value.id, { pass: authPass.value, remark: authRemark.value.trim() })
    ElMessage.success(authPass.value ? '已通过' : '已驳回')
    authVisible.value = false
    await load()
  } finally {
    authLoading.value = false
  }
}

// 打开重置密码弹窗
function openReset(row) {
  resetRow.value = row
  newPassword.value = ''
  resetVisible.value = true
}

// 提交重置密码
async function submitReset() {
  if (!newPassword.value || newPassword.value.length < 6) {
    return ElMessage.warning('密码至少 6 位')
  }
  resetLoading.value = true
  try {
    await adminResetUserPassword(resetRow.value.id, { newPassword: newPassword.value })
    ElMessage.success('已重置，请通知用户使用新密码登录')
    resetVisible.value = false
  } finally {
    resetLoading.value = false
  }
}

// 封禁 / 解封
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

.ml6 {
  margin-left: 6px;
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}

.detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.d-head {
  display: flex;
  align-items: center;
  gap: 14px;
}

.d-name {
  font-size: 17px;
  font-weight: 700;
}

.d-title {
  font-weight: 600;
  font-size: 14px;
  padding-top: 4px;
  border-top: 1px solid var(--wk-border);
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.stat {
  background: #fafbfc;
  border-radius: 10px;
  padding: 12px;
  text-align: center;
}

.stat b {
  display: block;
  font-size: 17px;
  color: var(--wk-primary);
}

.stat span {
  font-size: 12px;
  color: var(--wk-text-3);
}

.auth-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.auth-radio {
  align-self: flex-start;
}
</style>
