<template>
  <el-dialog :model-value="modelValue" title="举报" width="460px" @update:model-value="$emit('update:modelValue', $event)">
    <el-alert
      type="info" :closable="false" show-icon class="mb"
      title="举报会由平台管理员处理，请如实填写；恶意举报可能被封禁。"
    />
    <el-input
      v-model="reason" type="textarea" :rows="4" maxlength="500" show-word-limit
      placeholder="说明举报理由，例如：虚假宣传、恶意拖延、作品抄袭……"
    />
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="danger" :loading="submitting" @click="onSubmit">提交举报</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { submitReport } from '@/api/admin'

const props = defineProps({
  modelValue: Boolean,
  targetType: { type: String, required: true },
  targetId: { type: [Number, String], required: true }
})
const emit = defineEmits(['update:modelValue'])

const reason = ref('')
const submitting = ref(false)

async function onSubmit() {
  if (!reason.value.trim()) return ElMessage.warning('请填写举报理由')
  submitting.value = true
  try {
    await submitReport({ targetType: props.targetType, targetId: Number(props.targetId), reason: reason.value })
    ElMessage.success('举报已提交，管理员会尽快处理')
    reason.value = ''
    emit('update:modelValue', false)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.mb {
  margin-bottom: 14px;
}
</style>
