<!-- views/admin/FileManage.vue：页面：后台文件管理（筛选、预览、下载、删除，含引用校验） -->
<template>
  <div class="files-page">
    <h2 class="wk-page-title">文件管理</h2>
    <p class="wk-page-desc">
      全站文件（头像 / 任务附件 / 交付附件 / 举报证据），管理员可预览与下载任意文件；被任务或订单引用的文件不能删除
    </p>

    <div class="wk-card" v-loading="loading">
      <!-- 筛选 -->
      <div class="filters">
        <el-select v-model="filter.bizType" placeholder="全部用途" clearable class="w150" @change="search">
          <el-option v-for="(text, code) in BIZ" :key="code" :label="text" :value="code" />
        </el-select>
        <el-input
          v-model="filter.keyword" placeholder="搜索文件名 / 存储路径" clearable class="w220"
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

      <el-table :data="list" empty-text="当前条件下没有文件">
        <el-table-column label="预览" width="80">
          <template #default="{ row }">
            <a :href="row.url" target="_blank" rel="noreferrer" :title="'预览 ' + row.name">
              <img v-if="row.image" :src="row.url" class="thumb" alt="" />
              <el-icon v-else :size="22" color="#c9cdd4"><Document /></el-icon>
            </a>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column label="大小" width="100">
          <template #default="{ row }">{{ (row.size / 1024).toFixed(1) }} KB</template>
        </el-table-column>
        <el-table-column label="用途" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ bizText(row.bizType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="uploaderName" label="上传人" width="130" show-overflow-tooltip />
        <el-table-column prop="contentType" label="类型" width="120" show-overflow-tooltip />
        <el-table-column label="存储位置" width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.storageInfo }}</template>
        </el-table-column>
        <el-table-column label="上传时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <a :href="row.url" target="_blank" rel="noreferrer" class="link">预览</a>
            <a :href="row.url.replace('/preview', '/download')" target="_blank" rel="noreferrer" class="link">下载</a>
            <el-button size="small" type="danger" plain @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > size"
        class="pager"
        layout="total, prev, pager, next"
        :current-page="page"
        :page-size="size"
        :total="total"
        @current-change="onPage"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminDeleteFile, adminListFiles } from '@/api/file'
import { formatTime } from '@/utils/format'

const BIZ = { AVATAR: '头像', TASK: '任务附件', DELIVER: '交付附件', REPORT: '举报证据' }

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const filter = reactive({ bizType: '', keyword: '', range: null })

// 文件用途转中文
const bizText = (t) => BIZ[t] || t || '-'

// 组装查询参数
function buildParams() {
  const params = {}
  if (filter.bizType) params.bizType = filter.bizType
  if (filter.keyword) params.keyword = filter.keyword
  if (filter.range && filter.range.length === 2) {
    params.start = filter.range[0]
    params.end = filter.range[1]
  }
  return params
}

// 加载文件列表
async function load() {
  loading.value = true
  try {
    const data = await adminListFiles({ ...buildParams(), page: page.value, size: size.value })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

// 查询（回到第一页）
function search() {
  page.value = 1
  load()
}

// 重置筛选
function reset() {
  filter.bizType = ''
  filter.keyword = ''
  filter.range = null
  search()
}

// 删除文件（被引用时后端会拦下并提示）
async function onDelete(row) {
  try {
    await ElMessageBox.confirm(
      `删除「${row.name}」后无法恢复，确定吗？\n（被任务/订单引用的文件会被系统拦下）`,
      '删除文件',
      { type: 'warning' }
    )
  } catch {
    return
  }
  await adminDeleteFile(row.id)
  ElMessage.success('已删除')
  await load()
}

// 翻页
function onPage(p) {
  page.value = p
  load()
}

onMounted(load)
</script>

<style scoped>
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
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

.thumb {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 6px;
}

.link {
  color: var(--wk-primary);
  margin-right: 10px;
  font-size: 13px;
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
