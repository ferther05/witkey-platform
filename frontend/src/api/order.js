import request from './request'

export const getMyOrders = (role) => request.get('/order/my', { params: { role } })

export const getOrder = (id) => request.get(`/order/${id}`)

export const deliverOrder = (id, note, fileIds = []) =>
  request.post(`/order/${id}/deliver`, { note, fileIds })

export const acceptOrder = (id) => request.post(`/order/${id}/accept`)

export const rejectOrder = (id, reason) => request.post(`/order/${id}/reject`, { reason })
