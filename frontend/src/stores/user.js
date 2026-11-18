// stores/user.js：登录态 store：token、用户信息、身份标签、登录/登出
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth'
import * as userApi from '@/api/user'
import { getToken, setToken } from '@/api/request'

/**
 * 登录态：token 存 localStorage，用户信息每次进站拉一次 /user/me，
 * 避免本地缓存与实际权限不一致。
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const profile = ref(null)
  const loading = ref(false)

  const isLogin = computed(() => !!token.value)
  const isAdmin = computed(() => profile.value?.role === 'ADMIN')
  // 管理员既不是雇主也不是威客，单独显示，避免后台账号顶着「雇主」标签
  const identityLabel = computed(() => {
    if (profile.value?.role === 'ADMIN') return '管理员'
    return profile.value?.identityType === 'WITKEY' ? '威客' : '雇主'
  })

  // 调用登录接口并保存 token
  async function doLogin(form) {
    const data = await authApi.login(form)
    token.value = data.token
    setToken(data.token)
    profile.value = data.user
    return data.user
  }

  // 调用注册接口
  async function doRegister(form) {
    return authApi.register(form)
  }

  // 拉取当前用户信息
  async function fetchProfile() {
    if (!token.value) return null
    loading.value = true
    try {
      profile.value = await userApi.getMe()
      return profile.value
    } finally {
      loading.value = false
    }
  }

  // 保存个人资料
  async function saveProfile(form) {
    profile.value = await userApi.updateProfile(form)
    return profile.value
  }

  // 提交实名认证
  async function applyRealName(form) {
    profile.value = await userApi.applyIdentity(form)
    return profile.value
  }

  // 切换雇主/威客身份
  async function switchIdentity(identityType) {
    profile.value = await userApi.switchIdentityType(identityType)
    return profile.value
  }

  // 退出登录并清理本地状态
  function logout() {
    token.value = ''
    profile.value = null
    setToken('')
  }

  return {
    token,
    profile,
    loading,
    isLogin,
    isAdmin,
    identityLabel,
    doLogin,
    doRegister,
    fetchProfile,
    saveProfile,
    applyRealName,
    switchIdentity,
    logout
  }
})
