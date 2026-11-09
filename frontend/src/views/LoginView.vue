<template>
  <div class="login">
    <!-- 背景装饰 -->
    <div class="blob blob-1" />
    <div class="blob blob-2" />
    <div class="grid-mask" />

    <div class="wrap">
      <!-- 左：品牌区 -->
      <section class="brand">
        <div class="logo-row">
          <span class="logo">威</span>
          <span class="brand-name">威客帮</span>
          <span class="brand-tag">Beta</span>
        </div>

        <h1>把技能<br /><em>交给对的人</em></h1>
        <p class="lead">悬赏任务 · 赏金托管 · 验收结算<br />一条链路，全流程可追溯</p>

        <ul class="features">
          <li>
            <span class="ico"><el-icon><Lock /></el-icon></span>
            <div>
              <b>赏金先托管</b>
              <span>发布即冻结，验收通过才打款给威客</span>
            </div>
          </li>
          <li>
            <span class="ico"><el-icon><Switch /></el-icon></span>
            <div>
              <b>身份自由切换</b>
              <span>一个账号既是雇主，也可以是威客</span>
            </div>
          </li>
          <li>
            <span class="ico"><el-icon><DataLine /></el-icon></span>
            <div>
              <b>状态与资金留痕</b>
              <span>每一步都能查，账目随时可对账</span>
            </div>
          </li>
        </ul>

        <div class="chips">
          <span>悬赏</span><span>投标</span><span>担保</span><span>结算</span><span>举报处理</span>
        </div>
      </section>

      <!-- 右：表单区 -->
      <section class="panel">
        <div class="card">
          <header class="card-head">
            <h2>{{ tab === 'login' ? '欢迎回来' : '创建账号' }}</h2>
            <p class="wk-muted">{{ tab === 'login' ? '登录后继续你的任务与订单' : '选择身份，开始发布或承接任务' }}</p>
          </header>

          <div class="seg">
            <button class="seg-btn" :class="{ on: tab === 'login' }" @click="tab = 'login'">登录</button>
            <button class="seg-btn" :class="{ on: tab === 'register' }" @click="tab = 'register'">注册</button>
          </div>

          <!-- 登录 -->
          <el-form
            v-if="tab === 'login'"
            ref="loginRef"
            :model="loginForm"
            :rules="loginRules"
            label-position="top"
            size="large"
            @submit.prevent="onLogin"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" :prefix-icon="User" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                show-password
                placeholder="请输入密码"
                :prefix-icon="Lock"
                @keyup.enter="onLogin"
              />
            </el-form-item>

            <el-button class="submit" :loading="loading" @click="onLogin">登 录</el-button>

            <div class="demo-box">
              <div class="demo-title">
                <el-icon><MagicStick /></el-icon>
                演示账号，点一下自动填入
              </div>
              <div class="demo-list">
                <button
                  v-for="account in demoAccounts"
                  :key="account.username"
                  type="button"
                  class="demo-item"
                  :class="{ used: loginForm.username === account.username }"
                  @click="fillDemo(account)"
                >
                  <span class="demo-role">{{ account.label }}</span>
                  <span class="demo-account">{{ account.username }} / {{ account.password }}</span>
                </button>
              </div>
            </div>
          </el-form>

          <!-- 注册 -->
          <el-form
            v-else
            ref="registerRef"
            :model="registerForm"
            :rules="registerRules"
            label-position="top"
            size="large"
            @submit.prevent="onRegister"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="registerForm.username" placeholder="4-20 位字母、数字或下划线" />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="registerForm.nickname" placeholder="展示给别人看的名字" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" show-password placeholder="6-32 位" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirm">
              <el-input v-model="registerForm.confirm" type="password" show-password placeholder="再输入一次" />
            </el-form-item>

            <el-form-item label="我是" prop="identityType">
              <div class="identity-group">
                <div
                  v-for="item in identities"
                  :key="item.value"
                  class="identity-card"
                  :class="{ active: registerForm.identityType === item.value }"
                  @click="registerForm.identityType = item.value"
                >
                  <el-icon :size="20"><component :is="item.icon" /></el-icon>
                  <div>
                    <div class="id-title">{{ item.label }}</div>
                    <div class="id-desc">{{ item.desc }}</div>
                  </div>
                </div>
              </div>
            </el-form-item>

            <el-button class="submit" :loading="loading" @click="onRegister">注 册</el-button>
          </el-form>

          <p class="agree">登录即表示同意《用户协议》与《隐私政策》</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, MagicStick } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const store = useUserStore()

const tab = ref('login')
const loading = ref(false)
const loginRef = ref()
const registerRef = ref()

const identities = [
  { value: 'EMPLOYER', label: '我是雇主', desc: '发布任务，找人干活', icon: 'Suitcase' },
  { value: 'WITKEY', label: '我是威客', desc: '接任务，凭技能赚钱', icon: 'MagicStick' }
]

/** 演示账号：点一下自动填入，方便现场演示 */
const demoAccounts = [
  { label: '管理员', username: 'admin', password: '123456', desc: '审核 / 举报 / 看板' },
  { label: '雇主', username: 'alice', password: '654321', desc: '发任务 / 选标 / 验收' },
  { label: '威客', username: 'bobby', password: '123456', desc: '投标 / 交付 / 收款' }
]

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({
  username: '',
  nickname: '',
  password: '',
  confirm: '',
  identityType: 'EMPLOYER'
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '4-20 位字母、数字或下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ],
  identityType: [{ required: true, message: '请选择身份', trigger: 'change' }]
}

function fillDemo(account) {
  loginForm.username = account.username
  loginForm.password = account.password
  ElMessage.success(`已填入「${account.label}」账号，点击登录即可`)
}

async function onLogin() {
  await loginRef.value?.validate().catch(() => Promise.reject())
  loading.value = true
  try {
    const user = await store.doLogin(loginForm)
    ElMessage.success(`欢迎回来，${user.nickname || user.username}`)
    router.push(route.query.redirect || '/home')
  } finally {
    loading.value = false
  }
}

async function onRegister() {
  await registerRef.value?.validate().catch(() => Promise.reject())
  loading.value = true
  try {
    await store.doRegister({
      username: registerForm.username,
      nickname: registerForm.nickname,
      password: registerForm.password,
      identityType: registerForm.identityType
    })
    ElMessage.success('注册成功，请登录')
    loginForm.username = registerForm.username
    loginForm.password = ''
    tab.value = 'login'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: radial-gradient(1200px 600px at 8% 8%, #fff5ec 0%, transparent 60%),
              radial-gradient(900px 500px at 92% 92%, #eef7f1 0%, transparent 62%),
              #fbfbfc;
}

/* 柔光色块 */
.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.55;
  pointer-events: none;
}

.blob-1 {
  width: 620px;
  height: 620px;
  left: -180px;
  top: -160px;
  background: #ffd0ab;
}

.blob-2 {
  width: 520px;
  height: 520px;
  right: -140px;
  bottom: -180px;
  background: #cfe9d8;
}

.grid-mask {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(31, 35, 41, 0.06) 1px, transparent 1px);
  background-size: 22px 22px;
  mask-image: linear-gradient(to bottom right, rgba(0, 0, 0, 0.5), transparent 55%);
  pointer-events: none;
}

.wrap {
  position: relative;
  z-index: 1;
  max-width: 1180px;
  margin: 0 auto;
  padding: 0 48px;
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  align-items: center;
  gap: 64px;
}

/* ---------------- 品牌区 ---------------- */
.logo-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 44px;
}

.logo {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  background: linear-gradient(135deg, #ff8a3c, #ff6b00);
  color: #fff;
  font-weight: 700;
  display: grid;
  place-items: center;
  box-shadow: 0 8px 20px rgba(255, 107, 0, 0.28);
}

.brand-name {
  font-size: 19px;
  font-weight: 700;
  letter-spacing: 1px;
}

.brand-tag {
  font-size: 10px;
  color: var(--wk-primary);
  background: var(--wk-primary-light);
  border-radius: 4px;
  padding: 2px 6px;
  margin-left: 2px;
}

.brand h1 {
  font-size: 46px;
  line-height: 1.24;
  letter-spacing: 1px;
  margin: 0 0 18px;
  font-weight: 800;
  color: var(--wk-text-1);
}

.brand h1 em {
  font-style: normal;
  background: linear-gradient(100deg, #ff8a3c, #ff6b00 60%, #ff4d00);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.lead {
  font-size: 15.5px;
  line-height: 1.9;
  color: var(--wk-text-3);
  margin: 0 0 42px;
}

.features {
  list-style: none;
  padding: 0;
  margin: 0 0 36px;
  display: grid;
  gap: 18px;
}

.features li {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.ico {
  width: 38px;
  height: 38px;
  flex: none;
  border-radius: 11px;
  display: grid;
  place-items: center;
  background: #fff;
  color: var(--wk-primary);
  box-shadow: 0 6px 18px rgba(31, 35, 41, 0.07);
}

.features b {
  display: block;
  font-size: 14.5px;
  margin-bottom: 3px;
}

.features span {
  font-size: 12.5px;
  color: var(--wk-text-3);
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chips span {
  font-size: 12px;
  color: var(--wk-text-2);
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(31, 35, 41, 0.06);
  border-radius: 999px;
  padding: 5px 12px;
  backdrop-filter: blur(6px);
}

/* ---------------- 表单卡片 ---------------- */
.panel {
  display: flex;
  justify-content: center;
}

.card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(14px);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 22px;
  padding: 34px 34px 24px;
  box-shadow: 0 24px 70px rgba(31, 35, 41, 0.1), 0 2px 6px rgba(31, 35, 41, 0.04);
}

/* 表单紧凑一些，注册表单也能一屏放下 */
:deep(.el-form-item) {
  margin-bottom: 12px;
}

:deep(.el-form-item:last-child) {
  margin-bottom: 4px;
}

.card-head h2 {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 700;
}

.card-head p {
  margin: 0 0 24px;
  font-size: 13px;
}

.seg {
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: #f2f3f5;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 24px;
}

.seg-btn {
  border: 0;
  background: transparent;
  border-radius: 9px;
  height: 36px;
  font-size: 14px;
  color: var(--wk-text-2);
  cursor: pointer;
  transition: all 0.22s ease;
  font-family: inherit;
}

.seg-btn.on {
  background: #fff;
  color: var(--wk-primary);
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(31, 35, 41, 0.08);
}

:deep(.el-form-item__label) {
  font-size: 12.5px;
  color: var(--wk-text-2);
  line-height: 20px;
  padding-bottom: 4px !important;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 12px;
  box-shadow: 0 0 0 1px #e9eaee inset;
  transition: box-shadow 0.2s;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d7d9df inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1.5px var(--wk-primary) inset;
}

.submit {
  width: 100%;
  height: 46px;
  margin-top: 6px;
  border: 0;
  border-radius: 11px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 4px;
  color: #fff;
  background: linear-gradient(100deg, #ff8a3c, #ff6b00 70%);
  box-shadow: 0 10px 24px rgba(255, 107, 0, 0.28);
  transition: transform 0.18s, box-shadow 0.18s;
}

.submit:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 30px rgba(255, 107, 0, 0.32);
  background: linear-gradient(100deg, #ff8a3c, #ff6b00 70%);
  color: #fff;
}

.demo-box {
  margin-top: 18px;
  border: 1px dashed #e6e7ec;
  border-radius: 12px;
  padding: 12px 12px 10px;
  background: rgba(247, 248, 250, 0.6);
}

.demo-title {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  color: var(--wk-text-3);
  margin-bottom: 10px;
}

.demo-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.demo-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 3px;
  padding: 8px 9px;
  border: 1px solid #e9eaee;
  border-radius: 9px;
  background: #fff;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.18s;
  text-align: left;
}

.demo-item:hover {
  border-color: #ffcfa8;
  background: var(--wk-primary-light);
  transform: translateY(-1px);
}

.demo-item.used {
  border-color: var(--wk-primary);
  background: var(--wk-primary-light);
}

.demo-role {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--wk-text-1);
}

.demo-item.used .demo-role {
  color: var(--wk-primary);
}

.demo-account {
  font-size: 10.5px;
  color: var(--wk-text-3);
  letter-spacing: 0.2px;
  white-space: nowrap;
}

.identity-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  width: 100%;
}

.identity-card {
  border: 1px solid #e9eaee;
  border-radius: 11px;
  padding: 11px;
  display: flex;
  gap: 9px;
  align-items: flex-start;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--wk-text-2);
}

.identity-card:hover {
  border-color: #ffcfa8;
}

.identity-card.active {
  border-color: var(--wk-primary);
  background: var(--wk-primary-light);
  color: var(--wk-primary);
}

.id-title {
  font-weight: 600;
  font-size: 13.5px;
}

.id-desc {
  font-size: 11.5px;
  color: var(--wk-text-3);
  margin-top: 2px;
}

.agree {
  margin: 20px 0 0;
  text-align: center;
  font-size: 11px;
  color: #b0b4bd;
}

@media (max-width: 980px) {
  .wrap {
    grid-template-columns: 1fr;
    gap: 32px;
    padding: 40px 24px;
  }
  .brand {
    text-align: center;
  }
  .brand h1 {
    font-size: 34px;
  }
  .features,
  .chips {
    display: none;
  }
  .logo-row {
    justify-content: center;
    margin-bottom: 20px;
  }
}
</style>
