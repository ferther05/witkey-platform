<template>
  <div class="reports">
    <h2 class="wk-page-title">举报处理</h2>
    <p class="wk-page-desc">举报是纠纷的唯一出口：可下架任务、封禁用户，或对订单强制退款/打款</p>

    <div class="wk-card">
      <el-radio-group v-model="status" size="small" class="filter" @change="load">
        <el-radio-button value="PENDING">待处理</el-radio-button>
        <el-radio-button value="DONE">已处理</el-radio-button>
        <el-radio-button value="">全部</el-radio-button>
      </el-radio-group>

      <el-empty v-if="!loading && list.length === 0" description="没有举报" />

      <div v-for="r in list" :key="r.id" class="item">
        <div class="main">
          <div class="title-row">
            <el-tag size="small" :type="targetType(r.targetType)" effect="plain">{{ targetText(r.targetType) }}</el-tag>
            <span class="title">{{ r.targetTitle }}</span>
            <el-tag size="small" :type="r.status === 'PENDING' ? 'warning' : 'success'" effect="light">
              {{ r.status === 'PENDING' ? '待处理' : '已处理' }}
            </el-tag>
          </div>

          <p class="reason">{{ r.reason }}</p>

          <div class="meta">
            <span>举报人：{{ r.reporterName }}</span>
            <span>{{ formatTime(r.createdAt) }}</span>
            <span v-if="r.handleOrderAction">处理结果：{{ actionText(r.handleOrderAction) }}</span>
          </div>

          <div v-if="r.handleRemark" class="remark">处理说明：{{ r.handleRemark }}</div>

          <div v-if="r.files?.length" class="files">
            <a v-for="f in r.files" :key="f.id" :href="f.url" target="_blank" class="file">{{ f.name }}</a>
          </div>
        </div>

        <div class="actions" v-if="r.status === 'PENDING'">
          <el-dropdown trigger="click" @command="(cmd) => openHandle(r, cmd)">
            <el-button type="primary" size="small">处理</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="REJECT">驳回举报</el-dropdown-item>
                <el-dropdown-item command="TAKE_DOWN" v-if="r.targetType === 'TASK'">下架任务</el-dropdown-item>
                <el-dropdown-item command="BAN" v-if="r.targetType === 'USER'">封禁用户</el-dropdown-item>
                <el-dropdown-item command="REFUND" v-if="r.targetType === 'ORDER'" divided>强制退款给雇主</el-dropdown-item>
                <el-dropdown-item command="PAY" v-if="r.targetType === 'ORDER'">强制打款给威客</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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

    <el-dialog v-model="dialog" :title="'处理举报：' + actionText(action)" width="460px">
      <el-alert
        type="warning" :closable="false" show-icon class="mb"
        :title="actionHint"
      />
      <el-input v-model="remark" type="textarea" :rows="3" maxlength="500" placeholder="处理说明（选填）" />
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminHandleReport, adminReports } from '@/api/admin'
import { formatTime } from '@/utils/format'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const status = ref('PENDING')
const loading = ref(false)

const dialog = ref(false)
const submitting = ref(false)
const current = ref(null)
const action = ref('REJECT')
const remark = ref('')

const targetText = (t) => ({ TASK: '任务', USER: '用户', ORDER: '订单' }[t] || t)
const targetType = (t) => ({ TASK: 'primary', USER: 'danger', ORDER: 'warning' }[t] || 'info')
const actionText = (a) => ({
  REJECT: '驳回', TAKE_DOWN: '下架任务', BAN: '封禁用户', REFUND: '强制退款', PAY: '强制打款'
}[a] || a)

const actionHint = computed(() => ({
  REJECT: '举报不成立，仅关闭工单，不影响任务或订单。',
  TAKE_DOWN: '任务立即下架，托管赏金退回雇主余额。',
  BAN: '被举报用户将被封禁，无法再登录。',
  REFUND: '托管赏金退回雇主，订单置为已退款，任务关闭。',
  PAY: '按订单金额结算：扣佣金后打款给威客，订单完成。'
}[action.value]))

async function load() {
  loading.value = true
  try {
    const data = await adminReports({ status: status.value || undefined, page: page.value, size: size.value })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openHandle(report, cmd) {
  current.value = report
  action.value = cmd
  remark.value = ''
  dialog.value = true
}

async function onSubmit() {
  submitting.value = true
  try {
    await adminHandleReport(current.value.id, { action: action.value, remark: remark.value })
    ElMessage.success('已处理')
    dialog.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

function onPage(p) {
  page.value = p
  load()
}

onMounted(load)
</script>

<style scoped>
.filter {
  margin-bottom: 8px;
}

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

.reason {
  margin: 10px 0 8px;
  color: var(--wk-text-2);
  line-height: 1.7;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 12px;
  color: var(--wk-text-3);
}

.remark {
  margin-top: 8px;
  font-size: 12px;
  color: var(--wk-text-2);
  background: #fafbfc;
  padding: 8px 10px;
  border-radius: 8px;
}

.files {
  margin-top: 8px;
  display: flex;
  gap: 12px;
}

.file {
  color: var(--wk-primary);
  font-size: 12px;
}

.actions {
  display: flex;
  align-items: flex-start;
}

.mb {
  margin-bottom: 14px;
}

.pager {
  justify-content: center;
  margin-top: 16px;
}
</style>
