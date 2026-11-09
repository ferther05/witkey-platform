<template>
  <div class="my-tasks">
    <div class="head">
      <div>
        <h2 class="wk-page-title">我的任务</h2>
        <p class="wk-page-desc">发布的任务与收藏的任务都在这里</p>
      </div>
      <el-button type="primary" @click="router.push('/tasks/publish')">
        <el-icon><Plus /></el-icon> 发布任务
      </el-button>
    </div>

    <div class="wk-card">
      <el-tabs v-model="tab" @tab-change="onTabChange">
        <el-tab-pane label="我发布的" name="mine">
          <el-radio-group v-model="status" size="small" class="status-filter" @change="load">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="PENDING_AUDIT">待审核</el-radio-button>
            <el-radio-button value="REJECTED">已驳回</el-radio-button>
            <el-radio-button value="RECRUITING">招募中</el-radio-button>
            <el-radio-button value="WITHDRAWN">已撤回</el-radio-button>
          </el-radio-group>
        </el-tab-pane>
        <el-tab-pane label="我收藏的" name="fav" />
      </el-tabs>

      <div v-loading="loading">
        <el-empty v-if="!loading && list.length === 0" description="这里还没有内容" />

        <div v-for="task in list" :key="task.id" class="row" @click="router.push(`/tasks/${task.id}`)">
          <div class="row-main">
            <div class="row-title">
              <span>{{ task.title }}</span>
              <el-tag size="small" :type="taskStatus(task.status).type" effect="light">
                {{ taskStatus(task.status).text }}
              </el-tag>
            </div>
            <div class="row-meta">
              <span>{{ task.categoryName }}</span>
              <span>赏金 ¥{{ formatMoney(task.budget) }}</span>
              <span>投标 {{ task.bidCount }} 人</span>
              <span>截止 {{ formatTime(task.deadline) }}</span>
            </div>
            <div v-if="task.auditRemark && task.status === 'REJECTED'" class="reject">
              驳回理由：{{ task.auditRemark }}
            </div>
          </div>

          <div class="row-actions" @click.stop>
            <el-button
              v-if="tab === 'mine' && ['PENDING_AUDIT', 'REJECTED', 'RECRUITING'].includes(task.status)"
              size="small"
              @click="router.push(`/tasks/${task.id}/edit`)"
            >
              编辑
            </el-button>
            <el-button
              v-if="tab === 'mine' && ['PENDING_AUDIT', 'REJECTED', 'RECRUITING'].includes(task.status)"
              size="small"
              type="danger"
              plain
              @click="onWithdraw(task)"
            >
              撤回
            </el-button>
            <el-button size="small" type="primary" plain @click="router.push(`/tasks/${task.id}`)">
              查看
            </el-button>
          </div>
        </div>
      </div>

      <el-pagination
        v-if="total > size"
        class="pager"
        layout="prev, pager, next"
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyFavorites, getMyTasks, withdrawTask } from '@/api/task'
import { formatMoney, formatTime, taskStatus } from '@/utils/format'

const router = useRouter()
const tab = ref('mine')
const status = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(8)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = tab.value === 'mine'
      ? await getMyTasks({ status: status.value || undefined, page: page.value, size: size.value })
      : await getMyFavorites({ page: page.value, size: size.value })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  page.value = 1
  load()
}

function onPage(p) {
  page.value = p
  load()
}

async function onWithdraw(task) {
  try {
    await ElMessageBox.confirm(
      `撤回《${task.title}》后将下架，托管的 ¥${formatMoney(task.budget)} 会退回余额。`,
      '撤回任务',
      { type: 'warning', confirmButtonText: '确定撤回', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  await withdrawTask(task.id)
  ElMessage.success('已撤回，赏金已退回余额')
  await load()
}

onMounted(load)
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.status-filter {
  margin-bottom: 8px;
}

.row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 12px;
  border-bottom: 1px solid var(--wk-border);
  cursor: pointer;
  transition: background 0.2s;
}

.row:hover {
  background: #fafbfc;
}

.row-main {
  flex: 1;
  min-width: 0;
}

.row-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
}

.row-meta {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 12px;
  color: var(--wk-text-3);
}

.reject {
  margin-top: 8px;
  font-size: 12px;
  color: var(--wk-danger);
}

.row-actions {
  display: flex;
  gap: 8px;
}

.pager {
  justify-content: center;
  margin-top: 20px;
}
</style>
