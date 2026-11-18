// api/notice.js：接口：消息列表、未读数、标记已读
import request from './request'

// 接口：GET /notice
export const getNotices = (page = 1, size = 10) =>
  request.get('/notice', { params: { page, size } })

// 接口：GET /notice/unread-count
export const getUnreadCount = () => request.get('/notice/unread-count')

// 接口：PUT /notice/${id}/read
export const markRead = (id) => request.put(`/notice/${id}/read`)

// 接口：PUT /notice/read-all
export const markAllRead = () => request.put('/notice/read-all')
