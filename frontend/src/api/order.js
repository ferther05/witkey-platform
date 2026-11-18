// api/order.js：接口：我的订单、详情、交付、验收、驳回
import request from './request'

// 接口：GET /order/my
export const getMyOrders = (role) => request.get('/order/my', { params: { role } })

// 接口：GET /order/${id}
export const getOrder = (id) => request.get(`/order/${id}`)

// 接口：POST /order/${id}/deliver
export const deliverOrder = (id, note, fileIds = []) =>
  request.post(`/order/${id}/deliver`, { note, fileIds })

// 接口：POST /order/${id}/accept
export const acceptOrder = (id) => request.post(`/order/${id}/accept`)

// 接口：POST /order/${id}/reject
export const rejectOrder = (id, reason) => request.post(`/order/${id}/reject`, { reason })
