<template>
  <div class="profile">
    <h2 class="wk-page-title">个人中心</h2>
    <p class="wk-page-desc">资料、实名认证、身份切换与账号安全都集中在这里</p>

    <div class="layout">
      <!-- 左：名片 -->
      <aside class="wk-card card">
        <div class="avatar-wrap" @click="pickAvatar" title="点击更换头像">
          <el-avatar :size="72" :src="store.profile?.avatar || ''" class="avatar">
            {{ (store.profile?.nickname || 'U').slice(0, 1) }}
          </el-avatar>
          <div class="avatar-mask">
            <el-icon><Camera /></el-icon>
            <span>更换</span>
          </div>
          <input ref="avatarInput" type="file" accept="image/png,image/jpeg,image/webp" hidden @change="onAvatarChange" />
        </div>
        <h3>{{ store.profile?.nickname || store.profile?.username }}</h3>
        <p class="wk-muted">@{{ store.profile?.username }}</p>

        <div class="badges">
          <el-tag :type="store.identityLabel === '威客' ? 'success' : 'warning'" effect="light">
            {{ store.identityLabel }}
          </el-tag>
          <el-tag :type="authTag.type" effect="light">{{ authTag.text }}</el-tag>
        </div>

        <el-divider />

        <div class="meta">
          <span class="wk-muted">技能标签</span>
          <div class="skills">
            <el-tag v-for="s in skillList" :key="s" size="small" effect="plain" round>{{ s }}</el-tag>
            <span v-if="!skillList.length" class="wk-muted">还没有填写</span>
          </div>
        </div>

        <el-divider />

        <div class="switch">
          <div class="wk-muted switch-label">当前主身份</div>
          <el-radio-group :model-value="store.profile?.identityType" :disabled="switchLoading" @change="onSwitch">
            <el-radio-button value="EMPLOYER">雇主</el-radio-button>
            <el-radio-button value="WITKEY">威客</el-radio-button>
          </el-radio-group>
        </div>
      </aside>

      <!-- 右：设置 -->
      <section class="wk-card main-card">
        <el-tabs v-model="tab">
          <!-- 基本资料 -->
          <el-tab-pane label="基本资料" name="basic">
            <el-form :model="profileForm" label-width="90px" class="form">
              <el-form-item label="昵称">
                <el-input v-model="profileForm.nickname" maxlength="50" show-word-limit />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="profileForm.mobile" maxlength="11" placeholder="用于联系与找回密码" />
              </el-form-item>
              <el-form-item label="技能标签">
                <el-input v-model="profileForm.skills" placeholder="用逗号分隔，如：UI,插画,品牌" />
              </el-form-item>
              <el-form-item label="个人简介">
                <el-input
                  v-model="profileForm.intro"
                  type="textarea"
                  :rows="4"
                  maxlength="255"
                  show-word-limit
                  placeholder="介绍一下自己擅长什么"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="saving" @click="onSaveProfile">保存修改</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 实名认证 -->
          <el-tab-pane label="实名认证" name="identity">
            <el-alert
              v-if="store.profile?.authStatus === 'PENDING'"
              type="warning"
              :closable="false"
              show-icon
              title="认证申请已提交，等待管理员审核"
              class="mb"
            />
            <el-alert
              v-else-if="store.profile?.authStatus === 'VERIFIED'"
              type="success"
              :closable="false"
              show-icon
              title="已完成实名认证"
              class="mb"
            />
            <el-form :model="identityForm" label-width="90px" class="form">
              <el-form-item label="真实姓名">
                <el-input v-model="identityForm.realName" maxlength="50" :disabled="authLocked" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="identityForm.mobile" maxlength="11" :disabled="authLocked" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :disabled="authLocked" :loading="applying" @click="onApply">
                  {{ authLocked ? '已提交' : '提交认证' }}
                </el-button>
                <span class="wk-muted hint">认证通过后才能参与资金相关操作</span>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 账号安全 -->
          <el-tab-pane label="账号安全" name="security">
            <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="90px" class="form">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认新密码" prop="confirm">
                <el-input v-model="pwdForm.confirm" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="changing" @click="onChangePassword">修改密码</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/user'
import { uploadFile } from '@/api/file'

const store = useUserStore()
const tab = ref('basic')

const saving = ref(false)
const applying = ref(false)
const changing = ref(false)
const switchLoading = ref(false)
const avatarInput = ref()
const uploadingAvatar = ref(false)

function pickAvatar() {
  if (uploadingAvatar.value) return
  avatarInput.value?.click()
}

async function onAvatarChange(e) {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (!file) return
  if (file.size > 5 * 1024 * 1024) return ElMessage.warning('头像不能超过 5MB')
  uploadingAvatar.value = true
  try {
    const data = await uploadFile(file, 'AVATAR')
    await store.saveProfile({ ...profileForm, avatar: data.url })
    ElMessage.success('头像已更新')
  } finally {
    uploadingAvatar.value = false
  }
}

const profileForm = reactive({ nickname: '', mobile: '', skills: '', intro: '' })
const identityForm = reactive({ realName: '', mobile: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const pwdRef = ref()

const authLocked = computed(() => ['PENDING', 'VERIFIED'].includes(store.profile?.authStatus))

const authTag = computed(() => {
  switch (store.profile?.authStatus) {
    case 'VERIFIED':
      return { text: '已认证', type: 'success' }
    case 'PENDING':
      return { text: '审核中', type: 'warning' }
    case 'REJECTED':
      return { text: '已驳回', type: 'danger' }
    default:
      return { text: '未认证', type: 'info' }
  }
})

const skillList = computed(() =>
  (store.profile?.skills || '')
    .split(/[,，]/)
    .map((s) => s.trim())
    .filter(Boolean)
)

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) =>
        value === pwdForm.newPassword ? callback() : callback(new Error('两次输入的密码不一致')),
      trigger: 'blur'
    }
  ]
}

function fillForms() {
  const p = store.profile
  if (!p) return
  profileForm.nickname = p.nickname || ''
  profileForm.mobile = p.mobile || ''
  profileForm.skills = p.skills || ''
  profileForm.intro = p.intro || ''
  identityForm.realName = p.realName || ''
  identityForm.mobile = p.mobile || ''
}

watch(() => store.profile, fillForms, { immediate: true })

async function onSaveProfile() {
  saving.value = true
  try {
    await store.saveProfile({ ...profileForm })
    ElMessage.success('资料已更新')
  } finally {
    saving.value = false
  }
}

async function onApply() {
  if (!identityForm.realName.trim()) return ElMessage.warning('请填写真实姓名')
  if (!/^1[3-9]\d{9}$/.test(identityForm.mobile)) return ElMessage.warning('请填写正确的手机号')
  applying.value = true
  try {
    await store.applyRealName({ ...identityForm })
    ElMessage.success('认证申请已提交，等待审核')
  } finally {
    applying.value = false
  }
}

async function onChangePassword() {
  await pwdRef.value?.validate().catch(() => Promise.reject())
  changing.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码已修改，请重新登录')
    store.logout()
    setTimeout(() => (location.href = '/login'), 800)
  } finally {
    changing.value = false
  }
}

async function onSwitch(value) {
  try {
    await ElMessageBox.confirm(
      `切换后将以「${value === 'WITKEY' ? '威客' : '雇主'}」身份使用平台，权限会同步变化，确定切换吗？`,
      '切换主身份',
      { type: 'warning', confirmButtonText: '确定切换', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  switchLoading.value = true
  try {
    await store.switchIdentity(value)
    ElMessage.success('身份已切换')
  } finally {
    switchLoading.value = false
  }
}

onMounted(async () => {
  if (!store.profile) await store.fetchProfile().catch(() => {})
})
</script>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  align-items: start;
}

.card {
  text-align: center;
  padding: 28px 24px;
}

.avatar {
  background: var(--wk-primary-light);
  color: var(--wk-primary);
  font-size: 26px;
  font-weight: 700;
}

.avatar-wrap {
  position: relative;
  display: inline-block;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  font-size: 11px;
  opacity: 0;
  transition: opacity 0.2s;
}

.avatar-wrap:hover .avatar-mask {
  opacity: 1;
}

.card h3 {
  margin: 14px 0 4px;
  font-size: 17px;
}

.badges {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-top: 12px;
}

.meta {
  text-align: left;
}

.skills {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.switch {
  text-align: left;
}

.switch-label {
  margin-bottom: 8px;
  font-size: 13px;
}

.main-card {
  min-height: 480px;
}

.form {
  max-width: 520px;
  margin-top: 8px;
}

.hint {
  margin-left: 12px;
  font-size: 12px;
}

.mb {
  margin-bottom: 16px;
}

@media (max-width: 1000px) {
  .layout {
    grid-template-columns: 1fr;
  }
}
</style>
