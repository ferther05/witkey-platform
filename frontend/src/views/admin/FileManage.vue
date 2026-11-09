<template>
  <div class="files-page">
    <h2 class="wk-page-title">文件管理</h2>
    <p class="wk-page-desc">所有上传文件的元信息；删除会同时清理磁盘上的文件</p>

    <div class="wk-card" v-loading="loading">
      <el-table :data="list" empty-text="还没有上传过文件">
        <el-table-column label="预览" width="90">
          <template #default="{ row }">
            <img v-if="row.image" :src="row.url" class="thumb" alt="" />
            <el-icon v-else :size="22" color="#c9cdd4"><Document /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column label="大小" width="110">
          <template #default="{ row }">{{ (row.size / 1024).toFixed(1) }} KB</template>
        </el-table-column>
        <el-table-column prop="bizType" label="用途" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ bizText(row.bizType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contentType" label="类型" width="140" show-overflow-tooltip />
        <el-table-column label="上传时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <a :href="row.url.replace('/preview', '/download')" target="_blank" class="link">下载</a>
            <el-button size="small" type="danger" plain @click="onDelete(row)">删除</el-button>
          </template>
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
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminDeleteFile, adminListFiles } from '@/api/file'
import { formatTime } from '@/utils/format'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

const BIZ = { AVATAR: '头像', TASK: '任务附件', DELIVER: '交付附件', REPORT: '举报证据' }
const bizText = (t) => BIZ[t] || t || '-'

async function load() {
  loading.value = true
  try {
    const data = await adminListFiles({ page: page.value, size: size.value })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`删除「${row.name}」后无法恢复，确定吗？`, '删除文件', { type: 'warning' })
  } catch {
    return
  }
  await adminDeleteFile(row.id)
  ElMessage.success('已删除')
  await load()
}

function onPage(p) {
  page.value = p
  load()
}

onMounted(load)
</script>

<style scoped>
.thumb {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 6px;
}

.link {
  color: var(--wk-primary);
  margin-right: 12px;
  font-size: 13px;
}

.pager {
  justify-content: center;
  margin-top: 16px;
}
</style>
