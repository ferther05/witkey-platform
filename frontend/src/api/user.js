import request from './request'

export const getMe = () => request.get('/user/me')

export const updateProfile = (data) => request.put('/user/profile', data)

export const changePassword = (data) => request.put('/user/password', data)

export const applyIdentity = (data) => request.post('/user/identity/apply', data)

export const switchIdentityType = (identityType) =>
  request.put('/user/identity-type', { identityType })
