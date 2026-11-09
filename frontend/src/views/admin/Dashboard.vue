<template>
  <div class="dashboard" v-loading="loading">
    <h2 class="wk-page-title">数据看板</h2>
    <p class="wk-page-desc">平台运营概况：成交、佣金、托管与待办</p>

    <div class="stat-grid">
      <div class="wk-card stat">
        <div class="label">用户总数</div>
        <div class="num">{{ data.userCount }}</div>
      </div>
      <div class="wk-card stat">
        <div class="label">任务总数</div>
        <div class="num">{{ data.taskCount }}</div>
      </div>
      <div class="wk-card stat">
        <div class="label">订单总数</div>
        <div class="num">{{ data.orderCount }}</div>
        <div class="wk-muted small">已完成 {{ data.doneOrderCount }}</div>
      </div>
      <div class="wk-card stat">
        <div class="label">累计成交额</div>
        <div class="num primary">¥ {{ formatMoney(data.gmv) }}</div>
      </div>
      <div class="wk-card stat">
        <div class="label">平台佣金</div>
        <div class="num">¥ {{ formatMoney(data.commission) }}</div>
      </div>
      <div class="wk-card stat">
        <div class="label">托管中</div>
        <div class="num">¥ {{ formatMoney(data.escrow) }}</div>
      </div>
      <div class="wk-card stat todo" @click="router.push('/admin/tasks')">
        <div class="label">待审核任务</div>
        <div class="num warn">{{ data.pendingAuditTask }}</div>
      </div>
      <div class="wk-card stat todo" @click="router.push('/admin/reports')">
        <div class="label">待处理举报</div>
        <div class="num danger">{{ data.pendingReport }}</div>
      </div>
    </div>

    <div class="chart-grid">
      <div class="wk-card chart-card">
        <h3>近 7 天订单趋势</h3>
        <div ref="trendRef" class="chart"></div>
      </div>
      <div class="wk-card chart-card">
        <h3>任务分类分布</h3>
        <div ref="pieRef" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { adminDashboard } from '@/api/admin'
import { formatMoney } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const trendRef = ref()
const pieRef = ref()
const data = reactive({
  userCount: 0, taskCount: 0, orderCount: 0, doneOrderCount: 0,
  pendingAuditTask: 0, pendingReport: 0,
  gmv: 0, commission: 0, escrow: 0,
  trend: [], categoryStats: []
})

function renderTrend() {
  const chart = echarts.init(trendRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '成交额'], bottom: 0 },
    grid: { left: 40, right: 40, top: 20, bottom: 50 },
    xAxis: { type: 'category', data: data.trend.map((t) => t.date) },
    yAxis: [
      { type: 'value', name: '订单数', minInterval: 1 },
      { type: 'value', name: '成交额' }
    ],
    series: [
      {
        name: '订单数',
        type: 'bar',
        barWidth: 26,
        itemStyle: { color: '#ff6b00', borderRadius: [6, 6, 0, 0] },
        data: data.trend.map((t) => t.orders)
      },
      {
        name: '成交额',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        symbolSize: 7,
        itemStyle: { color: '#2fa84f' },
        areaStyle: { opacity: 0.08 },
        data: data.trend.map((t) => Number(t.amount))
      }
    ]
  })
}

function renderPie() {
  const chart = echarts.init(pieRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 个 ({d}%)' },
    legend: { bottom: 0 },
    color: ['#ff6b00', '#2fa84f', '#3b82f6', '#f5a623', '#9b59b6'],
    series: [
      {
        type: 'pie',
        radius: ['42%', '66%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b}\n{c} 个' },
        data: data.categoryStats.map((c) => ({ name: c.name, value: c.count }))
      }
    ]
  })
}

onMounted(async () => {
  loading.value = true
  try {
    Object.assign(data, await adminDashboard())
    await nextTick()
    renderTrend()
    renderPie()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat {
  padding: 18px 20px;
}

.label {
  font-size: 13px;
  color: var(--wk-text-3);
}

.num {
  font-size: 26px;
  font-weight: 700;
  margin-top: 8px;
}

.num.primary {
  color: var(--wk-primary);
}

.num.warn {
  color: var(--wk-warning);
}

.num.danger {
  color: var(--wk-danger);
}

.small {
  font-size: 12px;
  margin-top: 4px;
}

.todo {
  cursor: pointer;
  transition: transform 0.2s;
}

.todo:hover {
  transform: translateY(-2px);
}

.chart-grid {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 20px;
}

.chart-card h3 {
  margin: 0 0 8px;
  font-size: 15px;
}

.chart {
  height: 300px;
  width: 100%;
}

@media (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
