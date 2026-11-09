<template>
  <div class="audit">
    <h2 class="wk-page-title">任务审核</h2>
    <p class="wk-page-desc">待审核任务按提交时间排序，审核通过后立即在大厅展示</p>

    <div class="wk-card" v-loading="loading">
      <el-empty v-if="!loading && list.length === 0" description="没有待审核的任务" />

      <div v-for="task in list" :key="task.id" class="item">
        <div class="main">
          <div class="title-row">
            <span class="title">{{ task.title }}</span>
            <el-tag size="small" effect="plain">{{ task.categoryName }}</el-tag>
            <span class="budget">¥ {{ formatMoney(task.budget) }}</span>
          </div>
          <p class="desc">{{ task.description }}</p>
          <div class="meta">
            <span>发布者：{{ task.employerName }}</span>
            <span>提交时间：{{ formatTime(task.createdAt) }}</span>
            <span>截止：{{ formatTime(task.deadline) }}</span>
          </div>
        </div>
        <div class="actions">
          <el-button type="success" size="small" @click="onAudit(task, true)">通过</el-button>
          <el-button type="danger" size="small" plain @click="onAudit(task, false)">驳回</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, ref } from 'vue'
import { adminAuditTask, adminPendingTasks } from '@/api/admin'
import { formatMoney, formatTime } from '@/utils/format'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await adminPendingTasks({ page: page.value, size: size.value })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function onAudit(task, pass) {
  let remark = ''
  if (pass) {
    try {
      await ElMessageBox.confirm(`确认通过《${task.title}》的审核？`, '审核通过', {
        type: 'success',
        confirmButtonText: '通过',
        cancelButtonText: '取消'
      })
    } catch {
      return
    }
  } else {
    try {
      const res = await ElMessageBox.prompt('请填写驳回理由，雇主会在"我的任务"里看到', '驳回任务', {
        confirmButtonText: '确定驳回',
        cancelButtonText: '取消',
        inputPlaceholder: '例如：描述不完整、赏金明显低于市场价',
        inputValidator: (v) => (v && v.trim().length > 0) || '驳回必须填写理由'
      })
      remark = res.value
    } catch {
      return
    }
  }

  await adminAuditTask(task.id, { pass, remark })
  ElMessage.success(pass ? '已通过' : '已驳回')
  await load()
}

function onPage(p) {
  page.value = p
  load()
}

onMounted(load)
</script>

<style scoped>
.item {
  display: flex;
  gap: 20px;
  padding: 20px 4px;
  border-bottom: 1px solid var(--wk-border);
}

.item:last-of-type {
  border-bottom: none;
}

.main {
  flex: 1;
  min-width: 0;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-size: 15px;
  font-weight: 600;
}

.budget {
  color: var(--wk-primary);
  font-weight: 700;
  margin-left: auto;
}

.desc {
  margin: 10px 0;
  color: var(--wk-text-2);
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  display: flex;
  gap: 18px;
  font-size: 12px;
  color: var(--wk-text-3);
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

.pager {
  justify-content: center;
  margin-top: 20px;
}
</style>
