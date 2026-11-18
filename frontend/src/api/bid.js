// api/bid.js：接口：投标、改标、撤标、选标、投标列表、我的投标
import request from './request'

// 接口：POST /bid
export const submitBid = (data) => request.post('/bid', data)

// 接口：PUT /bid/${id}
export const updateBid = (id, data) => request.put(`/bid/${id}`, data)

// 接口：POST /bid/${id}/withdraw
export const withdrawBid = (id) => request.post(`/bid/${id}/withdraw`)

// 接口：GET /bid/my
export const getMyBids = () => request.get('/bid/my')

// 接口：GET /task/${taskId}/bids
export const getTaskBids = (taskId) => request.get(`/task/${taskId}/bids`)

// 接口：POST /bid/${id}/select
export const selectBid = (id) => request.post(`/bid/${id}/select`)
