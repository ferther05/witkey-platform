<template>
  <div v-loading="loading" class="order">
    <el-page-header content="订单详情" @back="router.back()" class="back" />

    <template v-if="order">
      <div class="wk-card head-card">
        <div class="head-top">
          <div>
            <div class="no">订单号 {{ order.orderNo }}</div>
            <h2>{{ order.taskTitle }}</h2>
          </div>
          <el-tag :type="orderStatus(order.status).type" effect="light" size="large">
            {{ orderStatus(order.status).text }}
          </el-tag>
        </div>

        <el-steps :active="stepActive" finish-status="success" align-center class="steps">
          <el-step title="选标完成" :description="formatTime(order.createdAt, false)" />
          <el-step title="威客交付" :description="order.deliverTime ? formatTime(order.deliverTime, false) : '进行中'" />
          <el-step title="雇主验收" :description="order.acceptTime ? formatTime(order.acceptTime, false) : '待验收'" />
          <el-step title="平台结算" :description="order.settleTime ? formatTime(order.settleTime, false) : '待结算'" />
        </el-steps>
      </div>

      <div class="layout">
        <section class="main">
          <div class="wk-card">
            <h3>订单信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="任务">{{ order.taskTitle }}</el-descriptions-item>
              <el-descriptions-item label="订单金额">¥ {{ formatMoney(order.amount) }}</el-descriptions-item>
              <el-descriptions-item label="雇主">{{ order.employerName }}</el-descriptions-item>
              <el-descriptions-item label="威客">{{ order.witkeyName }}</el-descriptions-item>
              <el-descriptions-item label="佣金比例">{{ Number(order.commissionRate).toFixed(2) }}%</el-descriptions-item>
              <el-descriptions-item label="平台佣金">¥ {{ formatMoney(order.commissionAmount) }}</el-descriptions-item>
              <el-descriptions-item label="成交时间">{{ formatTime(order.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="交付版本">{{ order.deliverCount }} 版</el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="wk-card deliver-card">
            <h3>交付记录</h3>
            <el-empty v-if="!order.deliverNote" description="威客还没有提交交付物" :image-size="80" />
            <template v-else>
              <div class="note">
                <div class="note-head">
                  <span class="badge">第 {{ order.deliverCount }} 版</span>
                  <span class="wk-muted small">{{ formatTime(order.deliverTime) }}</span>
                </div>
                <p class="note-body">{{ order.deliverNote }}</p>

                <div v-if="order.deliverFiles?.length" class="files">
                  <div v-for="f in order.deliverFiles" :key="f.id" class="file-item">
                    <img v-if="f.image" :src="f.url" class="thumb" alt="" />
                    <el-icon v-else :size="18"><Document /></el-icon>
                    <span class="file-name">{{ f.name }}</span>
                    <span class="wk-muted small">{{ (f.size / 1024).toFixed(1) }} KB</span>
                    <a :href="f.url.replace('/preview', '/download')" class="wk-link" target="_blank">下载</a>
                  </div>
                </div>
              </div>
              <el-alert
                v-if="order.rejectReason && order.status === 'TO_DELIVER'"
                type="warning" :closable="false" show-icon
                :title="'上一次被驳回：' + order.rejectReason" class="mt"
              />
            </template>
            <p class="wk-muted small mt">
              附件由平台统一存储：交付物仅订单双方可见，下载走鉴权接口，不暴露真实路径。
            </p>
          </div>

          <div class="wk-card">
            <h3>任务要求</h3>
            <p class="desc">{{ order.taskDescription }}</p>
          </div>
        </section>

        <aside class="side">
          <div class="wk-card">
            <div class="label">订单金额</div>
            <div class="amount">¥ {{ formatMoney(order.amount) }}</div>
            <div class="wk-muted small">
              威客实收 ¥{{ formatMoney(net) }}（扣佣金 ¥{{ formatMoney(order.commissionAmount) }}）
            </div>

            <div class="actions">
              <!-- 威客：待交付 -->
              <template v-if="isWitkey && order.status === 'TO_DELIVER'">
                <el-button type="primary" @click="deliverDialog = true">提交交付物</el-button>
              </template>

              <!-- 威客：等待验收 -->
              <template v-else-if="isWitkey && order.status === 'TO_ACCEPT'">
                <el-alert type="info" :closable="false" show-icon title="已交付，等待雇主验收" />
              </template>

              <!-- 雇主：待验收 -->
              <template v-else-if="isEmployer && order.status === 'TO_ACCEPT'">
                <el-button type="success" @click="onAccept">验收通过并结算</el-button>
                <el-button type="danger" plain @click="onReject">驳回，要求修改</el-button>
              </template>

              <!-- 雇主：待交付 -->
              <template v-else-if="isEmployer && order.status === 'TO_DELIVER'">
                <el-alert type="warning" :closable="false" show-icon title="等待威客交付" />
              </template>

              <!-- 已完成 -->
              <template v-else-if="order.status === 'DONE'">
                <el-alert type="success" :closable="false" show-icon title="订单已完成并结算" />
                <div class="settle">
                  <div><span class="wk-muted">托管扣划</span><b>-¥{{ formatMoney(order.amount) }}</b></div>
                  <div><span class="wk-muted">平台佣金</span><b>-¥{{ formatMoney(order.commissionAmount) }}</b></div>
                  <div><span class="wk-muted">威客到账</span><b class="income">+¥{{ formatMoney(net) }}</b></div>
                </div>
              </template>
            </div>

            <el-button text class="report" @click="reportVisible = true">举报此订单</el-button>
          </div>

          <div class="wk-card tip-card">
            <h3>担保交易说明</h3>
            <p>1. 选标时赏金已从雇主余额冻结，威客不会拿不到钱；</p>
            <p>2. 威客交付后由雇主验收，不满意可驳回要求修改；</p>
            <p>3. 验收通过即结算：扣平台佣金后打入威客钱包；</p>
            <p>4. 有争议可走举报，由管理员介入处理。</p>
          </div>
        </aside>
      </div>
    </template>

    <el-dialog v-model="deliverDialog" title="提交交付物" width="560px">
      <el-input
        v-model="deliverNote" type="textarea" :rows="5" maxlength="1000" show-word-limit
        placeholder="说明交付了什么：文件清单、设计说明、使用方式、交付链接等"
      />

      <div class="upload-row">
        <el-upload :auto-upload="false" :show-file-list="false" multiple :on-change="onPickFile">
          <el-button :loading="uploading">
            <el-icon><Upload /></el-icon> 添加附件
          </el-button>
        </el-upload>
        <span class="wk-muted small">支持图片/PDF/压缩包/文档，单个不超过 20MB</span>
      </div>

      <div v-if="deliverFiles.length" class="picked">
        <div v-for="(f, i) in deliverFiles" :key="f.id" class="picked-item">
          <span>{{ f.name }}</span>
          <el-button text type="danger" size="small" @click="deliverFiles.splice(i, 1)">移除</el-button>
        </div>
      </div>

      <template #footer>
        <el-button @click="deliverDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onDeliver">确认交付</el-button>
      </template>
    </el-dialog>
    <ReportDialog v-if="order" v-model="reportVisible" target-type="ORDER" :target-id="order.id" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { acceptOrder, deliverOrder, getOrder, rejectOrder } from '@/api/order'
import { uploadFile } from '@/api/file'
import { useUserStore } from '@/stores/user'
import { orderStatus } from '@/utils/status'
import ReportDialog from '@/components/ReportDialog.vue'
import { formatMoney, formatTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const order = ref(null)
const loading = ref(false)
const submitting = ref(false)
const deliverDialog = ref(false)
const deliverNote = ref('')
const deliverFiles = ref([])
const uploading = ref(false)
const reportVisible = ref(false)

async function onPickFile(uploadFileItem) {
  const raw = uploadFileItem.raw
  if (!raw) return
  if (raw.size > 20 * 1024 * 1024) return ElMessage.warning('单个文件不能超过 20MB')
  uploading.value = true
  try {
    const data = await uploadFile(raw, 'DELIVER', order.value.id)
    deliverFiles.value.push(data)
    ElMessage.success('附件已上传')
  } finally {
    uploading.value = false
  }
}

const isEmployer = computed(() => order.value && store.profile && order.value.employerId === store.profile.id)
const isWitkey = computed(() => order.value && store.profile && order.value.witkeyId === store.profile.id)
const net = computed(() =>
  order.value ? Number(order.value.amount) - Number(order.value.commissionAmount) : 0
)

const stepActive = computed(() => {
  switch (order.value?.status) {
    case 'TO_DELIVER': return 1
    case 'TO_ACCEPT': return 2
    case 'DONE': return 4
    default: return 1
  }
})

async function load() {
  loading.value = true
  try {
    order.value = await getOrder(route.params.id)
  } catch {
    router.replace('/orders')
  } finally {
    loading.value = false
  }
}

async function onDeliver() {
  if (!deliverNote.value.trim()) return ElMessage.warning('请填写交付说明')
  submitting.value = true
  try {
    await deliverOrder(order.value.id, deliverNote.value, deliverFiles.value.map((f) => f.id))
    ElMessage.success('已提交交付，等待雇主验收')
    deliverDialog.value = false
    deliverNote.value = ''
    deliverFiles.value = []
    await load()
  } finally {
    submitting.value = false
  }
}

async function onAccept() {
  try {
    await ElMessageBox.confirm(
      `验收通过后将立即结算：平台扣佣金 ¥${formatMoney(order.value.commissionAmount)}，威客到账 ¥${formatMoney(net.value)}。确定验收吗？`,
      '验收通过', { type: 'success', confirmButtonText: '确认验收', cancelButtonText: '再看看' }
    )
  } catch {
    return
  }
  await acceptOrder(order.value.id)
  ElMessage.success('验收通过，已完成结算')
  await load()
}

async function onReject() {
  let reason = ''
  try {
    const res = await ElMessageBox.prompt('说明哪里不符合要求，威客会看到并修改后重新交付', '驳回验收', {
      confirmButtonText: '确定驳回',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：第 2 张海报字体不符合品牌规范',
      inputValidator: (v) => (v && v.trim().length > 0) || '请填写驳回原因'
    })
    reason = res.value
  } catch {
    return
  }
  await rejectOrder(order.value.id, reason)
  ElMessage.success('已驳回，等待威客重新交付')
  await load()
}

onMounted(async () => {
  if (!store.profile) await store.fetchProfile().catch(() => {})
  await load()
})
</script>

<style scoped>
.back { margin-bottom: 18px; }

.head-card { margin-bottom: 20px; }

.head-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.no {
  font-size: 12px;
  color: var(--wk-text-3);
  font-family: Consolas, monospace;
}

.head-top h2 {
  margin: 6px 0 0;
  font-size: 19px;
}

.steps { margin-top: 26px; }

.layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
  align-items: start;
}

.main { display: grid; gap: 20px; }

.main h3, .side h3 {
  font-size: 15px;
  margin: 0 0 16px;
}

.desc {
  white-space: pre-wrap;
  line-height: 1.9;
  color: var(--wk-text-2);
  margin: 0;
}

.note {
  background: #fafbfc;
  border-radius: 10px;
  padding: 14px;
}

.note-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.badge {
  background: var(--wk-primary-light);
  color: var(--wk-primary);
  border-radius: 6px;
  padding: 2px 8px;
  font-size: 12px;
}

.note-body {
  margin: 10px 0 0;
  line-height: 1.8;
  white-space: pre-wrap;
}

.small { font-size: 12px; }
.mt { margin-top: 12px; }

.side { display: grid; gap: 16px; position: sticky; top: 88px; }
.label { font-size: 13px; color: var(--wk-text-3); }
.amount { font-size: 30px; font-weight: 700; color: var(--wk-primary); margin: 6px 0; }

.actions { display: grid; gap: 10px; margin-top: 18px; }
.actions .el-button { width: 100%; margin: 0; }

.settle {
  display: grid;
  gap: 8px;
  margin-top: 12px;
  padding: 12px;
  background: #fafbfc;
  border-radius: 10px;
  font-size: 13px;
}

.settle div { display: flex; justify-content: space-between; }
.income { color: var(--wk-success); }

.files {
  margin-top: 12px;
  display: grid;
  gap: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  background: #fff;
  border: 1px solid var(--wk-border);
  border-radius: 8px;
  font-size: 13px;
}

.thumb {
  width: 32px;
  height: 32px;
  object-fit: cover;
  border-radius: 6px;
}

.file-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.wk-link {
  color: var(--wk-primary);
  font-size: 12px;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 14px;
}

.picked {
  margin-top: 10px;
  display: grid;
  gap: 6px;
}

.picked-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fafbfc;
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 13px;
}

.tip-card p {
  font-size: 12px;
  color: var(--wk-text-3);
  line-height: 1.9;
  margin: 0;
}

@media (max-width: 1000px) {
  .layout { grid-template-columns: 1fr; }
  .side { position: static; }
}
</style>
