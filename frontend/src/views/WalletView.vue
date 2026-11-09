<template>
  <div class="wallet">
    <h2 class="wk-page-title">我的钱包</h2>
    <p class="wk-page-desc">赏金托管、收支流水都记录在这里，演示环境充值与提现均为模拟</p>

    <div class="cards">
      <div class="wk-card stat">
        <div class="label">可用余额</div>
        <div class="num primary">¥ {{ formatMoney(info.balance) }}</div>
      </div>
      <div class="wk-card stat">
        <div class="label">托管中（冻结）</div>
        <div class="num">¥ {{ formatMoney(info.frozen) }}</div>
      </div>
      <div class="wk-card stat actions">
        <div class="btn-row">
          <el-button type="primary" @click="openRecharge">
            <el-icon><Wallet /></el-icon> 模拟充值
          </el-button>
          <el-button @click="openWithdraw">
            <el-icon><Money /></el-icon> 模拟提现
          </el-button>
        </div>
        <div class="wk-muted small">演示环境，不接真实支付</div>
      </div>
    </div>

    <div class="wk-card reconcile" v-if="recon">
      <div class="recon-head">
        <h3>账户对账</h3>
        <el-tag :type="recon.consistent ? 'success' : 'danger'" effect="light">
          {{ recon.consistent ? '平账' : '异常' }}
        </el-tag>
      </div>
      <div class="recon-grid">
        <div><span class="wk-muted">累计充值</span><b>¥ {{ formatMoney(recon.totalRecharge) }}</b></div>
        <div><span class="wk-muted">累计结算到账</span><b>¥ {{ formatMoney(recon.totalIncome) }}</b></div>
        <div><span class="wk-muted">累计提现</span><b>¥ {{ formatMoney(recon.totalWithdraw) }}</b></div>
        <div><span class="wk-muted">流水推算余额</span><b>¥ {{ formatMoney(recon.computedBalance) }}</b></div>
        <div><span class="wk-muted">流水推算冻结</span><b>¥ {{ formatMoney(recon.computedFrozen) }}</b></div>
        <div><span class="wk-muted">账户实际</span><b>¥ {{ formatMoney(info.balance) }} / {{ formatMoney(info.frozen) }}</b></div>
      </div>
      <p class="wk-muted small tip">
        对账逻辑：用所有流水反推余额与冻结，与账户字段比对；不一致说明有绕过流水改余额的漏洞。
      </p>
    </div>

    <div class="wk-card flows">
      <h3>收支流水</h3>
      <el-table :data="flows" v-loading="loading" empty-text="还没有流水记录">
        <el-table-column label="类型" width="130">
          <template #default="{ row }">
            <el-tag size="small" :type="flowMeta(row.type).type" effect="light">
              {{ flowMeta(row.type).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="140">
          <template #default="{ row }">
            <span :class="row.amount >= 0 ? 'income' : 'expense'">
              {{ row.amount >= 0 ? '+' : '' }}{{ formatMoney(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变动后余额" width="140">
          <template #default="{ row }">¥ {{ formatMoney(row.balanceAfter) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="说明" />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>

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

    <!-- 充值 -->
    <el-dialog v-model="rechargeDialog" title="模拟充值" width="380px">
      <div class="quick">
        <el-button v-for="a in [100, 500, 1000, 2000]" :key="a" size="small" @click="amount = a">
          ¥{{ a }}
        </el-button>
      </div>
      <el-input-number v-model="amount" :min="1" :max="100000" :step="100" class="amount-input" />
      <template #footer>
        <el-button @click="rechargeDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onRecharge">确认充值</el-button>
      </template>
    </el-dialog>

    <!-- 提现 -->
    <el-dialog v-model="withdrawDialog" title="模拟提现" width="380px">
      <el-alert
        type="info" :closable="false" show-icon class="mb"
        title="演示环境：提交后立即到账，不经过提现审核"
      />
      <el-input-number v-model="amount" :min="1" :max="50000" :step="100" class="amount-input" />
      <p class="wk-muted small">可提现余额 ¥{{ formatMoney(info.balance) }}</p>
      <template #footer>
        <el-button @click="withdrawDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onWithdraw">确认提现</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getFlows, getWallet, recharge, reconcile, withdraw } from '@/api/wallet'
import { formatMoney, formatTime } from '@/utils/format'
import { flowStatus } from '@/utils/status'

const info = reactive({ balance: 0, frozen: 0 })
const recon = ref(null)
const flows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

const rechargeDialog = ref(false)
const withdrawDialog = ref(false)
const amount = ref(500)
const submitting = ref(false)

const flowMeta = (type) => flowStatus(type)

async function loadWallet() {
  const data = await getWallet()
  info.balance = data.balance
  info.frozen = data.frozen
  recon.value = await reconcile().catch(() => null)
}

async function loadFlows() {
  loading.value = true
  try {
    const data = await getFlows({ page: page.value, size: size.value })
    flows.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openRecharge() {
  amount.value = 500
  rechargeDialog.value = true
}

function openWithdraw() {
  amount.value = 100
  withdrawDialog.value = true
}

async function onRecharge() {
  submitting.value = true
  try {
    await recharge(amount.value)
    ElMessage.success(`充值成功 ¥${formatMoney(amount.value)}`)
    rechargeDialog.value = false
    await loadWallet()
    page.value = 1
    await loadFlows()
  } finally {
    submitting.value = false
  }
}

async function onWithdraw() {
  if (Number(amount.value) > Number(info.balance)) {
    ElMessage.warning('可提现余额不足')
    return
  }
  submitting.value = true
  try {
    await withdraw(amount.value)
    ElMessage.success(`提现成功 ¥${formatMoney(amount.value)}，已到账`)
    withdrawDialog.value = false
    await loadWallet()
    page.value = 1
    await loadFlows()
  } finally {
    submitting.value = false
  }
}

function onPage(p) {
  page.value = p
  loadFlows()
}

onMounted(async () => {
  await loadWallet()
  await loadFlows()
})
</script>

<style scoped>
.cards {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.stat { padding: 24px; }

.label { font-size: 13px; color: var(--wk-text-3); }

.num {
  font-size: 30px;
  font-weight: 700;
  margin-top: 8px;
}

.num.primary { color: var(--wk-primary); }

.actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  gap: 8px;
}

.btn-row { display: flex; gap: 10px; }

.small { font-size: 12px; }

.reconcile { margin-bottom: 20px; }

.recon-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.recon-head h3 { margin: 0; font-size: 15px; }

.recon-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.recon-grid div {
  background: #fafbfc;
  border-radius: 10px;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
}

.tip { margin: 14px 0 0; }

.flows h3 { margin: 0 0 16px; font-size: 15px; }

.income { color: var(--wk-success); font-weight: 600; }
.expense { color: var(--wk-danger); font-weight: 600; }

.pager { justify-content: center; margin-top: 16px; }

.quick { display: flex; gap: 8px; margin-bottom: 16px; }

.amount-input { width: 100%; }

.mb { margin-bottom: 16px; }
</style>
