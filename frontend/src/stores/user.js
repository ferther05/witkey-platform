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
  const identityLabel = computed(() =>
    profile.value?.identityType === 'WITKEY' ? '威客' : '雇主'
  )

  async function doLogin(form) {
    const data = await authApi.login(form)
    token.value = data.token
    setToken(data.token)
    profile.value = data.user
    return data.user
  }

  async function doRegister(form) {
    return authApi.register(form)
  }

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

  async function saveProfile(form) {
    profile.value = await userApi.updateProfile(form)
    return profile.value
  }

  async function applyRealName(form) {
    profile.value = await userApi.applyIdentity(form)
    return profile.value
  }

  async function switchIdentity(identityType) {
    profile.value = await userApi.switchIdentityType(identityType)
    return profile.value
  }

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
