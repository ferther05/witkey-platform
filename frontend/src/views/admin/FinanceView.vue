<!-- views/admin/FinanceView.vue：页面：后台财务总览（成交、佣金、托管、平台流水与导出） -->
<template>
  <div class="finance">
    <h2 class="wk-page-title">财务总览</h2>
    <p class="wk-page-desc">平台视角的资金口径：成交、佣金、托管、充提与全站流水</p>

    <div class="grid" v-loading="loading">
      <div class="wk-card stat">
        <div class="label">累计成交额</div>
        <div class="num">¥ {{ formatMoney(data.totalGmv) }}</div>
        <div class="wk-muted small">已完成订单 {{ data.doneOrderCount }} 笔</div>
      </div>
      <div class="wk-card stat">
        <div class="label">平台累计佣金</div>
        <div class="num primary">¥ {{ formatMoney(data.totalCommission) }}</div>
        <div class="wk-muted small">按分类比例从赏金中扣除</div>
      </div>
      <div class="wk-card stat">
        <div class="label">当前托管中</div>
        <div class="num">¥ {{ formatMoney(data.escrowAmount) }}</div>
        <div class="wk-muted small">所有用户冻结金额之和</div>
      </div>
      <div class="wk-card stat">
        <div class="label">进行中订单</div>
        <div class="num">{{ data.activeOrderCount }}</div>
        <div class="wk-muted small">待交付 + 待验收</div>
      </div>
    </div>

    <div class="grid second">
      <div class="wk-card stat">
        <div class="label">累计充值</div>
        <div class="num small-num">¥ {{ formatMoney(data.totalRecharge) }}</div>
      </div>
      <div class="wk-card stat">
        <div class="label">累计提现</div>
        <div class="num small-num">¥ {{ formatMoney(data.totalWithdraw) }}</div>
      </div>
      <div class="wk-card stat">
        <div class="label">待审核任务</div>
        <div class="num small-num">{{ data.pendingAuditTaskCount }}</div>
      </div>
    </div>

    <!-- 平台资金流水 -->
    <div class="wk-card flows">
      <div class="flows-head">
        <h3>平台资金流水</h3>
        <el-button size="small" :loading="exporting" @click="onExport">导出 CSV</el-button>
      </div>

      <div class="filters">
        <el-select v-model="filter.type" placeholder="全部类型" clearable class="w150" @change="search">
          <el-option v-for="(text, code) in TYPES" :key="code" :label="text" :value="code" />
        </el-select>
        <el-input
          v-model="filter.keyword" placeholder="搜索说明 / 业务单号" clearable class="w220"
          @keyup.enter="search"
        />
        <el-date-picker
          v-model="filter.range" type="datetimerange"
          start-placeholder="开始时间" end-placeholder="结束时间"
          value-format="YYYY-MM-DDTHH:mm:ss" class="w340"
        />
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table :data="flows" v-loading="flowLoading" empty-text="当前条件下没有流水">
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="username" label="用户" width="160" show-overflow-tooltip />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="info" effect="plain">{{ row.typeText || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="140">
          <template #default="{ row }">
            <span :class="Number(row.amount) >= 0 ? 'income' : 'expense'">
              {{ Number(row.amount) >= 0 ? '+' : '' }}{{ formatMoney(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变动后余额" width="140">
          <template #default="{ row }">¥ {{ formatMoney(row.balanceAfter) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="说明" min-width="240" show-overflow-tooltip />
      </el-table>

      <el-pagination
        v-if="flowTotal > flowSize"
        class="pager"
        layout="total, prev, pager, next"
        :current-page="flowPage"
        :page-size="flowSize"
        :total="flowTotal"
        @current-change="onFlowPage"
      />
    </div>

    <div class="wk-card note">
      <h3>口径说明</h3>
      <p>1. 成交额与佣金只统计状态为「已完成」的订单，避免把托管中的钱算成收入；</p>
      <p>2. 佣金在订单生成时按分类比例快照，之后调整分类佣金不影响历史订单；</p>
      <p>3. 托管中金额 = 所有用户钱包冻结之和，订单完成或任务流标后自动归零；</p>
      <p>4. 每个用户可在「我的钱包 - 账户对账」里用流水反推余额自检。</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminExportFlows, adminFlows, getFinanceSummary } from '@/api/wallet'
import { formatMoney, formatTime } from '@/utils/format'

const data = reactive({
  totalGmv: 0,
  totalCommission: 0,
  escrowAmount: 0,
  totalRecharge: 0,
  totalWithdraw: 0,
  doneOrderCount: 0,
  activeOrderCount: 0,
  pendingAuditTaskCount: 0
})
const loading = ref(false)

// 流水类型字典（与后端一致）
const TYPES = {
  RECHARGE: '充值',
  WITHDRAW: '提现',
  FREEZE: '冻结',
  UNFREEZE: '解冻',
  SETTLE_PAY: '托管扣划',
  SETTLE_IN: '结算到账',
  REFUND: '退款',
  COMMISSION: '平台佣金'
}

const flows = ref([])
const flowTotal = ref(0)
const flowPage = ref(1)
const flowSize = ref(20)
const flowLoading = ref(false)
const exporting = ref(false)
const filter = reactive({ type: '', keyword: '', range: null })

// 组装查询参数
function buildParams() {
  const params = {}
  if (filter.type) params.type = filter.type
  if (filter.keyword) params.keyword = filter.keyword
  if (filter.range && filter.range.length === 2) {
    params.start = filter.range[0]
    params.end = filter.range[1]
  }
  return params
}

// 加载平台流水
async function loadFlows() {
  flowLoading.value = true
  try {
    const params = { ...buildParams(), page: flowPage.value, size: flowSize.value }
    const res = await adminFlows(params)
    flows.value = res.list || []
    flowTotal.value = res.total || 0
  } finally {
    flowLoading.value = false
  }
}

// 查询（回到第一页）
function search() {
  flowPage.value = 1
  loadFlows()
}

// 重置筛选
function reset() {
  filter.type = ''
  filter.keyword = ''
  filter.range = null
  search()
}

// 翻页
function onFlowPage(p) {
  flowPage.value = p
  loadFlows()
}

// 导出当前筛选结果为 CSV
async function onExport() {
  exporting.value = true
  try {
    const blob = await adminExportFlows(buildParams())
    const url = URL.createObjectURL(new Blob([blob], { type: 'text/csv;charset=utf-8' }))
    const a = document.createElement('a')
    a.href = url
    a.download = `平台资金流水_${new Date().toISOString().slice(0, 10)}.csv`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('已导出')
  } finally {
    exporting.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    Object.assign(data, await getFinanceSummary())
  } finally {
    loading.value = false
  }
  loadFlows()
})
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.grid.second {
  grid-template-columns: repeat(3, 1fr);
}

.stat {
  padding: 22px;
}

.label {
  color: var(--wk-text-3);
  font-size: 13px;
}

.num {
  font-size: 24px;
  font-weight: 700;
  margin: 8px 0 6px;
}

.num.primary {
  color: var(--wk-primary);
}

.num.small-num {
  font-size: 19px;
}

.small {
  font-size: 12px;
}

.flows {
  margin-bottom: 20px;
}

.flows-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.flows-head h3 {
  margin: 0;
  font-size: 15px;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}

.w150 {
  width: 150px;
}

.w220 {
  width: 220px;
}

.w340 {
  width: 340px;
}

.income {
  color: var(--wk-success);
  font-weight: 600;
}

.expense {
  color: var(--wk-danger);
  font-weight: 600;
}

.pager {
  justify-content: flex-end;
  margin-top: 14px;
}

.note h3 {
  font-size: 15px;
  margin: 0 0 10px;
}

.note p {
  margin: 6px 0;
  color: var(--wk-text-2);
  font-size: 13px;
}
</style>
