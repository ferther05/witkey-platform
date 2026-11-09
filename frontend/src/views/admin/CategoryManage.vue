<template>
  <div class="category">
    <div class="head">
      <div>
        <h2 class="wk-page-title">分类维护</h2>
        <p class="wk-page-desc">分类决定任务归类，佣金比例会写入订单快照</p>
      </div>
      <el-button type="primary" @click="openCreate">新增分类</el-button>
    </div>

    <div class="wk-card">
      <el-table :data="list" v-loading="loading" empty-text="还没有分类">
        <el-table-column prop="name" label="分类名称" />
        <el-table-column label="佣金比例" width="160">
          <template #default="{ row }">{{ Number(row.commissionRate).toFixed(2) }}%</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" plain @click="toggle(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" plain @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑分类' : '新增分类'" width="420px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="分类名称">
          <el-input v-model="form.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="佣金比例">
          <el-input-number v-model="form.commissionRate" :min="0" :max="50" :step="1" />
          <span class="unit">%</span>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminCreateCategory,
  adminDeleteCategory,
  adminGetCategories,
  adminUpdateCategory
} from '@/api/admin'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialog = ref(false)
const editing = ref(null)

const form = reactive({ name: '', commissionRate: 10, sort: 0 })

async function load() {
  loading.value = true
  try {
    list.value = await adminGetCategories()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  form.name = ''
  form.commissionRate = 10
  form.sort = list.value.length + 1
  dialog.value = true
}

function openEdit(row) {
  editing.value = row
  form.name = row.name
  form.commissionRate = Number(row.commissionRate)
  form.sort = row.sort
  dialog.value = true
}

async function onSave() {
  if (!form.name.trim()) return ElMessage.warning('请填写分类名称')
  saving.value = true
  try {
    if (editing.value) {
      await adminUpdateCategory(editing.value.id, { ...form })
      ElMessage.success('已保存')
    } else {
      await adminCreateCategory({ ...form })
      ElMessage.success('已新增')
    }
    dialog.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function toggle(row) {
  await adminUpdateCategory(row.id, { status: row.status === 1 ? 0 : 1 })
  ElMessage.success('状态已更新')
  await load()
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '删除分类', { type: 'warning' })
  } catch {
    return
  }
  try {
    await adminDeleteCategory(row.id)
    ElMessage.success('已删除')
  } finally {
    await load()
  }
}

onMounted(load)
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.unit {
  margin-left: 8px;
  color: var(--wk-text-3);
}
</style>
