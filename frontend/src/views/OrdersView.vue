<template>
  <div class="orders">
    <h2 class="wk-page-title">我的订单</h2>
    <p class="wk-page-desc">担保订单：赏金托管在平台，验收通过后才会结算给威客</p>

    <div class="wk-card">
      <el-tabs v-model="role" @tab-change="load">
        <el-tab-pane label="我发布的任务（雇主）" name="employer" />
        <el-tab-pane label="我接的任务（威客）" name="witkey" />
      </el-tabs>

      <div v-loading="loading">
        <el-empty v-if="!loading && list.length === 0" description="还没有订单" />

        <div v-for="order in list" :key="order.id" class="row" @click="router.push(`/orders/${order.id}`)">
          <div class="row-main">
            <div class="row-title">
              <span class="no">{{ order.orderNo }}</span>
              <el-tag size="small" :type="orderStatus(order.status).type" effect="light">
                {{ orderStatus(order.status).text }}
              </el-tag>
            </div>
            <div class="task-title">{{ order.taskTitle }}</div>
            <div class="row-meta">
              <span>金额 ¥{{ formatMoney(order.amount) }}</span>
              <span>佣金 ¥{{ formatMoney(order.commissionAmount) }}</span>
              <span>{{ role === 'employer' ? '威客' : '雇主' }}：{{ role === 'employer' ? order.witkeyName : order.employerName }}</span>
              <span>{{ formatTime(order.createdAt) }}</span>
            </div>
          </div>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMyOrders } from '@/api/order'
import { orderStatus } from '@/utils/status'
import { formatMoney, formatTime } from '@/utils/format'

const router = useRouter()
const role = ref('employer')
const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    list.value = await getMyOrders(role.value)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 8px;
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
}

.no {
  font-family: Consolas, monospace;
  font-size: 12px;
  color: var(--wk-text-3);
}

.task-title {
  margin-top: 6px;
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

.arrow {
  color: var(--wk-text-3);
}
</style>
