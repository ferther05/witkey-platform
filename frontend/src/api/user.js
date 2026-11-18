// api/user.js：接口：个人资料、改密、实名认证、身份切换
import request from './request'

// 接口：GET /user/me
export const getMe = () => request.get('/user/me')

// 接口：PUT /user/profile
export const updateProfile = (data) => request.put('/user/profile', data)

// 修改密码
export const changePassword = (data) => request.put('/user/password', data)

// 接口：POST /user/identity/apply
export const applyIdentity = (data) => request.post('/user/identity/apply', data)

// 接口：PUT /user/identity-type
export const switchIdentityType = (identityType) =>
  request.put('/user/identity-type', { identityType })
