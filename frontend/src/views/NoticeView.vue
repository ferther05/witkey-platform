<template>
  <div class="notice">
    <div class="head">
      <div>
        <h2 class="wk-page-title">消息中心</h2>
        <p class="wk-page-desc">
          系统消息与交易消息都会在这里，共 {{ total }} 条，未读 {{ unread }} 条
        </p>
      </div>
      <el-button :disabled="unread === 0" @click="onReadAll">全部标为已读</el-button>
    </div>

    <div class="wk-card list-card" v-loading="loading">
      <el-empty v-if="!loading && list.length === 0" description="还没有消息，先去发个任务试试" />

      <div
        v-for="item in list"
        :key="item.id"
        class="item"
        :class="{ unread: !item.read }"
        @click="onRead(item)"
      >
        <div class="dot" />
        <div class="body">
          <div class="row">
            <span class="title">{{ item.title }}</span>
            <el-tag size="small" effect="plain" :type="item.type === 'TRADE' ? 'warning' : 'info'">
              {{ item.type === 'TRADE' ? '交易' : '系统' }}
            </el-tag>
          </div>
          <p class="content">{{ item.content }}</p>
          <div class="time">{{ formatTime(item.createdAt) }}</div>
        </div>
      </div>

      <el-pagination
        v-if="total > size"
        class="pager"
        layout="prev, pager, next"
        :current-page="page"
        :page-size="size"
        :total="total"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getNotices, markAllRead, markRead } from '@/api/notice'

const list = ref([])
const total = ref(0)
const unread = ref(0)
const page = ref(1)
const size = ref(8)
const loading = ref(false)

function formatTime(value) {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 16)
}

function recount() {
  unread.value = list.value.filter((i) => !i.read).length
}

async function load() {
  loading.value = true
  try {
    const data = await getNotices(page.value, size.value)
    // 后端 PageResult 解包后是 { list, total, page, size }
    list.value = data.list || []
    total.value = data.total || 0
    recount()
  } finally {
    loading.value = false
  }
}

async function onRead(item) {
  if (item.read) return
  await markRead(item.id)
  item.read = true
  recount()
}

async function onReadAll() {
  await markAllRead()
  ElMessage.success('已全部标为已读')
  await load()
}

function onPageChange(p) {
  page.value = p
  load()
}

onMounted(load)
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.list-card {
  padding: 8px 24px;
}

.item {
  display: flex;
  gap: 14px;
  padding: 18px 0;
  border-bottom: 1px solid var(--wk-border);
  cursor: pointer;
  transition: background 0.2s;
}

.item:last-of-type {
  border-bottom: none;
}

.item:hover {
  background: #fafbfc;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: transparent;
  margin-top: 8px;
  flex: none;
}

.item.unread .dot {
  background: var(--wk-primary);
}

.item.unread .title {
  font-weight: 600;
}

.body {
  flex: 1;
}

.row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title {
  font-size: 15px;
}

.content {
  margin: 6px 0 8px;
  color: var(--wk-text-2);
  line-height: 1.6;
}

.time {
  font-size: 12px;
  color: var(--wk-text-3);
}

.pager {
  justify-content: center;
  padding: 20px 0;
}
</style>
