<template>
  <div class="finance">
    <h2 class="wk-page-title">财务总览</h2>
    <p class="wk-page-desc">平台视角的资金口径：成交、佣金、托管与充提</p>

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
import { getFinanceSummary } from '@/api/wallet'
import { formatMoney } from '@/utils/format'

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

onMounted(async () => {
  loading.value = true
  try {
    Object.assign(data, await getFinanceSummary())
  } finally {
    loading.value = false
  }
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
  font-size: 13px;
  color: var(--wk-text-3);
}

.num {
  font-size: 28px;
  font-weight: 700;
  margin: 8px 0 6px;
}

.num.primary {
  color: var(--wk-primary);
}

.small-num {
  font-size: 22px;
}

.small {
  font-size: 12px;
}

.note h3 {
  margin: 0 0 12px;
  font-size: 15px;
}

.note p {
  margin: 0;
  font-size: 12px;
  color: var(--wk-text-3);
  line-height: 1.9;
}

@media (max-width: 1100px) {
  .grid,
  .grid.second {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
