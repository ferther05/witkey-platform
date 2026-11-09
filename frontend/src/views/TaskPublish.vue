<template>
  <div class="publish">
    <el-page-header :content="isEdit ? '编辑任务' : '发布任务'" @back="router.back()" class="back" />

    <div class="layout">
      <section class="wk-card form-card">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="任务标题" prop="title">
            <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="一句话说清要做什么，如：品牌 LOGO 设计" />
          </el-form-item>

          <el-form-item label="任务分类" prop="categoryId">
            <el-radio-group v-model="form.categoryId">
              <el-radio-button v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="任务描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="8"
              maxlength="5000"
              show-word-limit
              placeholder="需求背景、交付要求、验收标准、参考案例……写清楚能少很多来回"
            />
          </el-form-item>

          <div class="row">
            <el-form-item label="赏金（元）" prop="budget" class="half">
              <el-input-number v-model="form.budget" :min="1" :max="100000" :step="50" controls-position="right" />
            </el-form-item>

            <el-form-item label="截止时间" prop="deadline" class="half">
              <el-date-picker
                v-model="form.deadline"
                type="datetime"
                placeholder="选择截止时间"
                :disabled-date="disablePast"
                value-format="YYYY-MM-DDTHH:mm:ss"
              />
            </el-form-item>
          </div>

          <div class="submit-row">
            <el-button type="primary" size="large" :loading="submitting" @click="onSubmit">
              {{ isEdit ? '保存并重新提交审核' : '发布任务' }}
            </el-button>
            <el-button size="large" @click="router.back()">取消</el-button>
          </div>
        </el-form>
      </section>

      <aside class="side">
        <div class="wk-card">
          <div class="label">我的余额</div>
          <div class="balance">¥ {{ formatMoney(wallet.balance) }}</div>
          <div class="wk-muted small">托管中 ¥ {{ formatMoney(wallet.frozen) }}</div>
          <el-divider />
          <div class="rule">
            <p>1. 发布后赏金会<b>冻结</b>，不是直接扣掉；</p>
            <p>2. 平台审核通过后任务才在大厅展示；</p>
            <p>3. 撤回或流标，赏金原路退回余额；</p>
            <p>4. 已有威客投标后不能修改，也不能直接撤回。</p>
          </div>
          <el-alert
            v-if="Number(form.budget) > Number(wallet.balance)"
            type="error"
            :closable="false"
            show-icon
            title="余额不足，请先充值"
            class="mt"
          />
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategories, getTaskDetail, publishTask, updateTask } from '@/api/task'
import { getWallet } from '@/api/wallet'
import { formatMoney } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const categories = ref([])
const wallet = reactive({ balance: 0, frozen: 0 })
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  title: '',
  categoryId: null,
  description: '',
  budget: 100,
  deadline: ''
})

const rules = {
  title: [
    { required: true, message: '请填写任务标题', trigger: 'blur' },
    { max: 100, message: '标题最长 100 个字符', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择任务分类', trigger: 'change' }],
  description: [{ required: true, message: '请填写任务描述', trigger: 'blur' }],
  budget: [{ required: true, message: '请填写赏金', trigger: 'change' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
}

function disablePast(date) {
  return date.getTime() < Date.now() - 86400000
}

async function onSubmit() {
  await formRef.value?.validate().catch(() => Promise.reject())
  const amount = Number(form.budget)
  if (!isEdit.value && amount > Number(wallet.balance)) {
    ElMessage.warning('余额不足，请先到钱包充值')
    return
  }
  try {
    await ElMessageBox.confirm(
      isEdit.value
        ? '修改后任务会重新进入审核，托管的赏金会按新金额调整。确定提交吗？'
        : `发布后将冻结 ¥${amount.toFixed(2)} 作为赏金，审核通过后在大厅展示。确定发布吗？`,
      isEdit.value ? '确认修改' : '确认发布',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '再想想' }
    )
  } catch {
    return
  }

  submitting.value = true
  try {
    const payload = {
      title: form.title,
      categoryId: form.categoryId,
      description: form.description,
      budget: amount,
      deadline: form.deadline
    }
    if (isEdit.value) {
      await updateTask(route.params.id, payload)
      ElMessage.success('已保存，等待重新审核')
    } else {
      await publishTask(payload)
      ElMessage.success('发布成功，等待平台审核')
    }
    router.push('/my-tasks')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const [cats, w] = await Promise.all([
    getCategories().catch(() => []),
    getWallet().catch(() => ({ balance: 0, frozen: 0 }))
  ])
  categories.value = cats
  wallet.balance = w.balance
  wallet.frozen = w.frozen
  if (!form.categoryId && cats.length) form.categoryId = cats[0].id

  if (isEdit.value) {
    const task = await getTaskDetail(route.params.id)
    form.title = task.title
    form.categoryId = task.categoryId
    form.description = task.description
    form.budget = Number(task.budget)
    form.deadline = String(task.deadline).slice(0, 19)
  }
})
</script>

<style scoped>
.back {
  margin-bottom: 18px;
}

.layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
  align-items: start;
}

.row {
  display: flex;
  gap: 24px;
}

.half {
  flex: 1;
}

.submit-row {
  margin-top: 12px;
  display: flex;
  gap: 12px;
}

.side {
  position: sticky;
  top: 88px;
}

.label {
  font-size: 13px;
  color: var(--wk-text-3);
}

.balance {
  font-size: 26px;
  font-weight: 700;
  margin: 6px 0 2px;
}

.small {
  font-size: 12px;
}

.rule p {
  font-size: 12px;
  color: var(--wk-text-3);
  line-height: 1.8;
  margin: 0;
}

.mt {
  margin-top: 12px;
}
</style>
