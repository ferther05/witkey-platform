import request from './request'

export const adminGetCategories = () => request.get('/admin/category')

export const adminCreateCategory = (data) => request.post('/admin/category', data)

export const adminUpdateCategory = (id, data) => request.put(`/admin/category/${id}`, data)

export const adminDeleteCategory = (id) => request.delete(`/admin/category/${id}`)

export const adminPendingTasks = (params) => request.get('/admin/task/audit', { params })

export const adminAuditTask = (id, data) => request.post(`/admin/task/${id}/audit`, data)

export const adminUsers = (params) => request.get('/admin/user', { params })

export const adminSetUserStatus = (id, status) => request.put(`/admin/user/${id}/status`, { status })

export const adminReports = (params) => request.get('/admin/report', { params })

export const adminHandleReport = (id, data) => request.post(`/admin/report/${id}/handle`, data)

export const adminDashboard = () => request.get('/admin/stats/dashboard')

export const submitReport = (data) => request.post('/report', data)
