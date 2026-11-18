<!-- views/admin/OperationLog.vue：页面：操作日志（所有人操作留痕，管理员查看） -->
<template>
  <div class="log-page">
    <h2 class="wk-page-title">操作日志</h2>
    <p class="wk-page-desc">
      记录全站关键操作：注册登录、任务发布/审核/撤回、投标/选标、交付/验收、资金、举报处理、用户封禁等
    </p>

    <div class="wk-card">
      <!-- 筛选区 -->
      <div class="filters">
        <el-select v-model="filters.action" placeholder="动作类型" clearable size="default" class="w180">
          <el-option v-for="(text, code) in actions" :key="code" :label="text" :value="code" />
        </el-select>
        <el-input
          v-model="filters.keyword"
          placeholder="搜索用户名 / 说明 / 动作"
          clearable
          class="w220"
          @keyup.enter="search"
        />
        <el-date-picker
          v-model="filters.range"
          type="datetimerange"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          class="w360"
        />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table :data="list" v-loading="loading" empty-text="暂无操作记录">
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作人" width="180">
          <template #default="{ row }">
            <span>{{ row.username || '系统' }}</span>
            <el-tag v-if="row.role === 'ADMIN'" size="small" type="danger" effect="light" class="ml6">管理员</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="动作" width="130">
          <template #default="{ row }">
            <el-tag size="small" :type="actionType(row.action)" effect="light">{{ row.actionText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="对象" width="130">
          <template #default="{ row }">
            <span v-if="row.targetType">
              {{ targetText(row.targetType) }}<template v-if="row.targetId"> #{{ row.targetId }}</template>
            </span>
            <span v-else class="wk-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="说明" min-width="320" show-overflow-tooltip />
      </el-table>

      <el-pagination
        v-if="total > 0"
        class="pager"
        layout="total, prev, pager, next, jumper"
        :current-page="page"
        :page-size="size"
        :total="total"
        @current-change="onPage"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { adminLogActions, adminLogs } from '@/api/admin'
import { formatTime } from '@/utils/format'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)
const actions = ref({})

const filters = reactive({ action: '', keyword: '', range: null })

// 动作 -> 标签颜色
function actionType(action) {
  if (action.startsWith('TASK_AUDIT') || action.startsWith('USER_') || action.startsWith('REPORT_HANDLE')) return 'danger'
  if (action.startsWith('ORDER_') || action.startsWith('BID_')) return 'warning'
  if (action.startsWith('WALLET_')) return 'success'
  if (action.startsWith('AUTH_')) return 'info'
  return ''
}

// 对象类型 -> 中文
const TARGETS = { USER: '用户', TASK: '任务', BID: '投标', ORDER: '订单', FILE: '文件', REPORT: '举报', CATEGORY: '分类', WALLET: '钱包', AUTH: '账号' }
// 举报对象类型转文字
function targetText(t) {
  return TARGETS[t] || t
}

// 加载数据
async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filters.action) params.action = filters.action
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.range && filters.range.length === 2) {
      params.start = filters.range[0]
      params.end = filters.range[1]
    }
    const data = await adminLogs(params)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

// 查询（回到第一页）
function search() {
  page.value = 1
  load()
}

// 重置筛选
function reset() {
  filters.action = ''
  filters.keyword = ''
  filters.range = null
  search()
}

// 翻页
function onPage(p) {
  page.value = p
  load()
}

onMounted(async () => {
  actions.value = await adminLogActions().catch(() => ({}))
  load()
})
</script>

<style scoped>
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.w180 {
  width: 180px;
}

.w220 {
  width: 220px;
}

.w360 {
  width: 360px;
}

.ml6 {
  margin-left: 6px;
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
