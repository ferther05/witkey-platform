// api/task.js：接口：任务列表、详情、发布、编辑、撤回、收藏、我的任务
import request from './request'

// 接口：GET /category
export const getCategories = () => request.get('/category')

// 接口：GET /task
export const getTaskHall = (params) => request.get('/task', { params })

// 接口：GET /task/${id}
export const getTaskDetail = (id) => request.get(`/task/${id}`)

// 接口：POST /task
export const publishTask = (data) => request.post('/task', data)

// 接口：PUT /task/${id}
export const updateTask = (id, data) => request.put(`/task/${id}`, data)

// 接口：POST /task/${id}/withdraw
export const withdrawTask = (id) => request.post(`/task/${id}/withdraw`)

// 接口：GET /task/my
export const getMyTasks = (params) => request.get('/task/my', { params })

// 接口：GET /task/favorite
export const getMyFavorites = (params) => request.get('/task/favorite', { params })

// 切换收藏
export const toggleFavorite = (id) => request.post(`/task/${id}/favorite`)
