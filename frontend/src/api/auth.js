// api/auth.js：接口：注册 / 登录
import request from './request'

// 接口：POST /auth/register
export const register = (data) => request.post('/auth/register', data)

// 接口：POST /auth/login
export const login = (data) => request.post('/auth/login', data)
