<template>
  <div class="my-bids">
    <h2 class="wk-page-title">我的投标</h2>
    <p class="wk-page-desc">投标、中标与落选记录都在这，中标后去订单里交付</p>

    <div class="wk-card" v-loading="loading">
      <el-empty v-if="!loading && list.length === 0" description="还没有投过标，去任务大厅看看" />

      <div v-for="bid in list" :key="bid.id" class="item">
        <div class="main">
          <div class="title-row">
            <span class="title" @click="router.push(`/tasks/${bid.taskId}`)">{{ bid.taskTitle }}</span>
            <el-tag size="small" :type="bidStatus(bid.status).type" effect="light">
              {{ bidStatus(bid.status).text }}
            </el-tag>
          </div>
          <p class="content">{{ bid.content }}</p>
          <div class="meta">
            <span>我的报价 <b class="quote">¥{{ formatMoney(bid.quote) }}</b></span>
            <span>任务赏金 ¥{{ formatMoney(bid.budget) }}</span>
            <span>投标于 {{ formatTime(bid.createdAt) }}</span>
          </div>
        </div>

        <div class="actions">
          <el-button v-if="bid.status === 'WIN' && bid.orderId" type="primary" size="small"
                     @click="router.push(`/orders/${bid.orderId}`)">
            去交付
          </el-button>
          <el-button v-if="bid.status === 'PENDING'" size="small" @click="router.push(`/tasks/${bid.taskId}`)">
            修改方案
          </el-button>
          <el-button size="small" plain @click="router.push(`/tasks/${bid.taskId}`)">查看任务</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMyBids } from '@/api/bid'
import { bidStatus } from '@/utils/status'
import { formatMoney, formatTime } from '@/utils/format'

const router = useRouter()
const list = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    list.value = await getMyBids()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.item {
  display: flex;
  gap: 20px;
  padding: 20px 4px;
  border-bottom: 1px solid var(--wk-border);
  align-items: flex-start;
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
  cursor: pointer;
}

.title:hover {
  color: var(--wk-primary);
}

.content {
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

.quote {
  color: var(--wk-primary);
}

.actions {
  display: flex;
  gap: 8px;
}
</style>
