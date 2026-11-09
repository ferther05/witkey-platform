<template>
  <div v-loading="loading" class="detail">
    <el-page-header content="任务详情" @back="router.back()" class="back" />

    <div v-if="task" class="layout">
      <section class="main wk-card">
        <div class="top">
          <div class="tags">
            <el-tag size="small" effect="plain">{{ task.categoryName }}</el-tag>
            <el-tag size="small" :type="taskStatus(task.status).type" effect="light">
              {{ taskStatus(task.status).text }}
            </el-tag>
          </div>
          <h1>{{ task.title }}</h1>
          <div class="meta">
            <span><el-icon><Clock /></el-icon> 截止 {{ formatTime(task.deadline) }}（{{ remainText(task.deadline) }}）</span>
            <span><el-icon><View /></el-icon> {{ task.viewCount }} 次浏览</span>
            <span><el-icon><ChatDotRound /></el-icon> {{ task.bidCount }} 人投标</span>
          </div>
        </div>

        <el-alert
          v-if="task.status === 'REJECTED' && task.auditRemark"
          type="error" :closable="false" show-icon :title="'审核驳回：' + task.auditRemark" class="mb"
        />
        <el-alert
          v-else-if="task.status === 'PENDING_AUDIT'"
          type="warning" :closable="false" show-icon
          title="任务审核中，审核通过后会出现在任务大厅" class="mb"
        />

        <h3 class="section-title">任务描述</h3>
        <p class="desc">{{ task.description }}</p>

        <h3 class="section-title">
          投标情况
          <span class="wk-muted count">（{{ bids.length }} 份方案）</span>
        </h3>

        <!-- 雇主视角：投标列表 + 选标 -->
        <template v-if="isOwner">
          <el-empty v-if="bids.length === 0" description="还没有威客投标，可以把任务链接发给朋友看看" />
          <div v-for="bid in bids" :key="bid.id" class="bid-card" :class="{ win: bid.status === 'WIN' }">
            <div class="bid-head">
              <el-avatar :size="38">{{ bid.witkeyName.slice(0, 1) }}</el-avatar>
              <div class="who">
                <div class="name">
                  {{ bid.witkeyName }}
                  <el-tag v-if="bid.status === 'WIN'" size="small" type="success">中标</el-tag>
                  <el-tag v-else-if="bid.status === 'LOSE'" size="small" type="info">未中标</el-tag>
                  <el-tag v-else-if="bid.status === 'WITHDRAWN'" size="small" type="info">已撤标</el-tag>
                </div>
                <div class="wk-muted small">
                  已完成 {{ bid.doneCount }} 单<template v-if="bid.witkeySkills"> · {{ bid.witkeySkills }}</template>
                </div>
              </div>
              <div class="quote">¥ {{ formatMoney(bid.quote) }}</div>
            </div>
            <p class="bid-content">{{ bid.content }}</p>
            <div class="bid-foot">
              <span class="wk-muted small">投标于 {{ formatTime(bid.createdAt) }}</span>
              <el-button
                v-if="bid.status === 'PENDING' && task.status === 'RECRUITING'"
                type="primary" size="small" @click="onSelect(bid)"
              >
                选择 TA
              </el-button>
              <el-button v-if="bid.status === 'WIN' && bid.orderId" size="small" @click="router.push(`/orders/${bid.orderId}`)">
                查看订单
              </el-button>
            </div>
          </div>
        </template>

        <!-- 威客视角：我的投标 -->
        <template v-else-if="myBid">
          <div class="bid-card mine">
            <div class="bid-head">
              <el-avatar :size="38">{{ myBid.witkeyName.slice(0, 1) }}</el-avatar>
              <div class="who">
                <div class="name">
                  我的投标
                  <el-tag size="small" :type="bidStatus(myBid.status).type">{{ bidStatus(myBid.status).text }}</el-tag>
                </div>
                <div class="wk-muted small">投标于 {{ formatTime(myBid.createdAt) }}</div>
              </div>
              <div class="quote">¥ {{ formatMoney(myBid.quote) }}</div>
            </div>
            <p class="bid-content">{{ myBid.content }}</p>
            <div class="bid-foot">
              <div class="foot-actions">
                <el-button v-if="myBid.status === 'PENDING'" size="small" @click="openBidDialog(myBid)">修改方案</el-button>
                <el-button v-if="myBid.status === 'PENDING'" size="small" type="danger" plain @click="onWithdrawBid">撤回投标</el-button>
                <el-button v-if="myBid.status === 'WIN' && myBid.orderId" type="primary" size="small"
                           @click="router.push(`/orders/${myBid.orderId}`)">
                  去订单交付
                </el-button>
              </div>
            </div>
          </div>
        </template>

        <div v-else-if="!myBid" class="bid-placeholder">
          <el-icon :size="18"><ChatDotRound /></el-icon>
          <span>{{ task.status === 'RECRUITING' ? '还没有投标，点右侧「我要投标」提交你的方案' : '该任务当前不接受投标' }}</span>
        </div>
      </section>

      <aside class="side">
        <div class="wk-card budget-card">
          <div class="label">任务赏金（已托管）</div>
          <div class="amount">¥ {{ formatMoney(task.budget) }}</div>
          <div class="tip">赏金已从雇主余额冻结，验收通过后结算给威客</div>

          <div class="actions">
            <el-button v-if="isOwner" @click="router.push(`/tasks/${task.id}/edit`)">编辑任务</el-button>
            <el-button v-if="isOwner && canEdit" type="danger" plain @click="onWithdraw">撤回任务</el-button>

            <template v-if="!isOwner">
              <el-button
                v-if="!myBid && task.status === 'RECRUITING'"
                type="primary" @click="openBidDialog()"
              >
                我要投标
              </el-button>
              <el-button v-else-if="myBid" type="primary" plain @click="router.push('/my-bids')">
                查看我的投标
              </el-button>
            </template>

            <el-button @click="onFavorite" :type="task.favorite ? 'warning' : 'default'" plain>
              <el-icon><StarFilled v-if="task.favorite" /><Star v-else /></el-icon>
              {{ task.favorite ? '已收藏' : '收藏' }}
            </el-button>

            <el-button v-if="!isOwner" text class="report" @click="reportVisible = true">
              举报此任务
            </el-button>
          </div>
        </div>

        <div class="wk-card employer-card">
          <div class="label">发布者</div>
          <div class="emp">
            <el-avatar :size="40">{{ (task.employerName || '匿').slice(0, 1) }}</el-avatar>
            <div>
              <div class="emp-name">{{ task.employerName }}</div>
              <div class="wk-muted small">发布于 {{ formatTime(task.createdAt) }}</div>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- 投标弹窗 -->
    <el-dialog v-model="bidDialog" :title="editingBid ? '修改投标' : '提交投标'" width="520px">
      <el-form label-position="top">
        <el-form-item label="我的报价（元）">
          <el-input-number v-model="bidForm.quote" :min="1" :max="Number(task?.budget || 1)" :step="10" controls-position="right" />
          <span class="wk-muted hint">不高于任务赏金 ¥{{ formatMoney(task?.budget) }}</span>
        </el-form-item>
        <el-form-item label="方案说明">
          <el-input
            v-model="bidForm.content" type="textarea" :rows="6" maxlength="2000" show-word-limit
            placeholder="说说你的思路、过往案例、交付时间，雇主选的就是这段"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bidDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmitBid">
          {{ editingBid ? '保存修改' : '提交投标' }}
        </el-button>
      </template>
    </el-dialog>

    <ReportDialog v-if="task" v-model="reportVisible" target-type="TASK" :target-id="task.id" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken } from '@/api/request'
import { getTaskDetail, toggleFavorite, withdrawTask } from '@/api/task'
import { getTaskBids, selectBid, submitBid, updateBid, withdrawBid } from '@/api/bid'
import { useUserStore } from '@/stores/user'
import { bidStatus } from '@/utils/status'
import { formatMoney, formatTime, remainText, taskStatus } from '@/utils/format'
import ReportDialog from '@/components/ReportDialog.vue'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const task = ref(null)
const bids = ref([])
const loading = ref(false)
const submitting = ref(false)
const bidDialog = ref(false)
const editingBid = ref(null)
const reportVisible = ref(false)

const bidForm = reactive({ quote: 100, content: '' })

const isOwner = computed(() => task.value && store.profile && task.value.employerId === store.profile.id)
const canEdit = computed(() =>
  task.value && ['PENDING_AUDIT', 'REJECTED', 'RECRUITING'].includes(task.value.status)
)
const myBid = computed(() => {
  if (!store.profile) return null
  return bids.value.find((b) => b.witkeyId === store.profile.id) || null
})

async function load() {
  loading.value = true
  try {
    task.value = await getTaskDetail(route.params.id)
    if (isOwner.value) {
      bids.value = await getTaskBids(task.value.id).catch(() => [])
    } else if (store.profile) {
      // 威客：从"我的投标"里找这条任务，避免多开一个接口
      const { getMyBids } = await import('@/api/bid')
      const mine = await getMyBids().catch(() => [])
      bids.value = mine.filter((b) => b.taskId === task.value.id)
    }
  } catch {
    router.replace('/tasks')
  } finally {
    loading.value = false
  }
}

function openBidDialog(existing) {
  if (!getToken()) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  editingBid.value = existing || null
  bidForm.quote = existing ? Number(existing.quote) : Number(task.value.budget)
  bidForm.content = existing ? existing.content : ''
  bidDialog.value = true
}

async function onSubmitBid() {
  if (!bidForm.content.trim()) return ElMessage.warning('请填写方案说明')
  submitting.value = true
  try {
    if (editingBid.value) {
      await updateBid(editingBid.value.id, { taskId: task.value.id, ...bidForm })
      ElMessage.success('方案已更新')
    } else {
      await submitBid({ taskId: task.value.id, ...bidForm })
      ElMessage.success('投标成功，等待雇主选标')
    }
    bidDialog.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

async function onWithdrawBid() {
  try {
    await ElMessageBox.confirm('撤回后该投标不再展示给雇主，确定撤回吗？', '撤回投标', { type: 'warning' })
  } catch {
    return
  }
  await withdrawBid(myBid.value.id)
  ElMessage.success('已撤回投标')
  await load()
}

async function onSelect(bid) {
  try {
    await ElMessageBox.confirm(
      `选择「${bid.witkeyName}」中标？其余投标将自动落选，并生成担保订单。`,
      '确认选标',
      { type: 'warning', confirmButtonText: '确定选标', cancelButtonText: '再看看' }
    )
  } catch {
    return
  }
  const data = await selectBid(bid.id)
  ElMessage.success('选标成功，已生成担保订单')
  router.push(`/orders/${data.orderId}`)
}

async function onFavorite() {
  if (!getToken()) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  const data = await toggleFavorite(task.value.id)
  task.value.favorite = data.favorite
  ElMessage.success(data.favorite ? '已收藏' : '已取消收藏')
}

async function onWithdraw() {
  try {
    await ElMessageBox.confirm(
      '撤回后任务会下架，托管的赏金将退回你的余额，确定撤回吗？',
      '撤回任务', { type: 'warning', confirmButtonText: '确定撤回', cancelButtonText: '再想想' }
    )
  } catch {
    return
  }
  await withdrawTask(task.value.id)
  ElMessage.success('任务已撤回，赏金已退回余额')
  await load()
}

onMounted(async () => {
  if (!store.profile) await store.fetchProfile().catch(() => {})
  await load()
})
</script>

<style scoped>
.back { margin-bottom: 18px; }

.layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
  align-items: start;
}

.top h1 { font-size: 22px; margin: 12px 0 10px; line-height: 1.4; }
.tags { display: flex; gap: 8px; }

.meta {
  display: flex; flex-wrap: wrap; gap: 16px;
  color: var(--wk-text-3); font-size: 13px;
}
.meta span { display: inline-flex; align-items: center; gap: 5px; }

.mb { margin: 20px 0 0; }
.section-title { font-size: 15px; margin: 28px 0 10px; }
.count { font-size: 12px; font-weight: 400; }

.desc { white-space: pre-wrap; line-height: 1.9; color: var(--wk-text-2); margin: 0; }

.bid-card {
  border: 1px solid var(--wk-border);
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 12px;
}
.bid-card.win { border-color: var(--wk-success); background: #f6fdf8; }
.bid-card.mine { background: var(--wk-primary-light); border-color: #ffd8ba; }

.bid-head { display: flex; align-items: center; gap: 12px; }
.who { flex: 1; min-width: 0; }
.name { display: flex; align-items: center; gap: 8px; font-weight: 600; }
.small { font-size: 12px; }
.quote { color: var(--wk-primary); font-size: 18px; font-weight: 700; }

.bid-content {
  margin: 12px 0;
  line-height: 1.8;
  color: var(--wk-text-2);
  white-space: pre-wrap;
}

.bid-foot { display: flex; align-items: center; justify-content: space-between; }
.foot-actions { display: flex; gap: 8px; margin-left: auto; }

.bid-placeholder {
  display: flex; align-items: center; gap: 8px; padding: 18px;
  border: 1px dashed var(--wk-border); border-radius: 10px;
  color: var(--wk-text-3); font-size: 13px;
}

.side { display: grid; gap: 16px; position: sticky; top: 88px; }
.budget-card .label, .employer-card .label { font-size: 13px; color: var(--wk-text-3); }
.amount { font-size: 30px; font-weight: 700; color: var(--wk-primary); margin: 6px 0; }
.tip { font-size: 12px; color: var(--wk-text-3); line-height: 1.6; }
.actions { display: grid; gap: 10px; margin-top: 18px; }
.actions .el-button { width: 100%; margin: 0; }

.emp { display: flex; align-items: center; gap: 12px; margin-top: 14px; }
.emp-name { font-weight: 600; }

.hint { margin-left: 12px; font-size: 12px; }

@media (max-width: 1000px) {
  .layout { grid-template-columns: 1fr; }
  .side { position: static; }
}
</style>
