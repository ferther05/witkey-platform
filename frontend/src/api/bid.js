import request from './request'

export const submitBid = (data) => request.post('/bid', data)

export const updateBid = (id, data) => request.put(`/bid/${id}`, data)

export const withdrawBid = (id) => request.post(`/bid/${id}/withdraw`)

export const getMyBids = () => request.get('/bid/my')

export const getTaskBids = (taskId) => request.get(`/task/${taskId}/bids`)

export const selectBid = (id) => request.post(`/bid/${id}/select`)
