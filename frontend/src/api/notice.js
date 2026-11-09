import request from './request'

export const getNotices = (page = 1, size = 10) =>
  request.get('/notice', { params: { page, size } })

export const getUnreadCount = () => request.get('/notice/unread-count')

export const markRead = (id) => request.put(`/notice/${id}/read`)

export const markAllRead = () => request.put('/notice/read-all')
