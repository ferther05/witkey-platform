import axios from 'axios'
import { ElMessage } from 'element-plus'

const TOKEN_KEY = 'witkey_token'

export const getToken = () => localStorage.getItem(TOKEN_KEY) || ''
export const setToken = (token) => {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

const service = axios.create({ baseURL: '/api', timeout: 10000 })

// 请求：自动带 token
service.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应：统一拆 { code, message, data }，业务失败直接提示并抛出
service.interceptors.response.use(
  (resp) => {
    const body = resp.data
    if (body && typeof body.code === 'number') {
      if (body.code === 0) return body.data
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || '网络异常，请稍后再试'
    if (status === 401) {
      setToken('')
      if (location.pathname !== '/login') {
        location.href = '/login'
      }
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
