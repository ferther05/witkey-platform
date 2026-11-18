// api/admin.js：接口：后台看板、审核、举报、用户、分类、财务、文件、操作日志
import request from './request'

// 接口：GET /admin/category
export const adminGetCategories = () => request.get('/admin/category')

// 接口：POST /admin/category
export const adminCreateCategory = (data) => request.post('/admin/category', data)

// 接口：PUT /admin/category/${id}
export const adminUpdateCategory = (id, data) => request.put(`/admin/category/${id}`, data)

// 接口：DELETE /admin/category/${id}
export const adminDeleteCategory = (id) => request.delete(`/admin/category/${id}`)

// 接口：GET /admin/task/audit
export const adminPendingTasks = (params) => request.get('/admin/task/audit', { params })

// 接口：POST /admin/task/${id}/audit
export const adminAuditTask = (id, data) => request.post(`/admin/task/${id}/audit`, data)

// 接口：GET /admin/user
export const adminUsers = (params) => request.get('/admin/user', { params })

// 接口：PUT /admin/user/${id}/status
export const adminSetUserStatus = (id, status) => request.put(`/admin/user/${id}/status`, { status })

// 接口：GET /admin/report
export const adminReports = (params) => request.get('/admin/report', { params })

// 接口：POST /admin/report/${id}/handle
export const adminHandleReport = (id, data) => request.post(`/admin/report/${id}/handle`, data)

// 接口：GET /admin/stats/dashboard
export const adminDashboard = () => request.get('/admin/stats/dashboard')

// 接口：POST /report
export const submitReport = (data) => request.post('/report', data)

// 接口：GET /admin/log（操作日志）
export const adminLogs = (params) => request.get('/admin/log', { params })
// 接口：GET /admin/log/actions（动作字典）
export const adminLogActions = () => request.get('/admin/log/actions')

// 接口：GET /admin/user/{id}（用户详情 + 统计）
export const adminUserDetail = (id) => request.get(`/admin/user/${id}`)
// 接口：PUT /admin/user/{id}/auth（实名认证审核）
export const adminReviewAuth = (id, data) => request.put(`/admin/user/${id}/auth`, data)
// 接口：PUT /admin/user/{id}/password（重置密码）
export const adminResetUserPassword = (id, data) => request.put(`/admin/user/${id}/password`, data)
