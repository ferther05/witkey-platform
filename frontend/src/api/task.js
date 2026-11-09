import request from './request'

export const getCategories = () => request.get('/category')

export const getTaskHall = (params) => request.get('/task', { params })

export const getTaskDetail = (id) => request.get(`/task/${id}`)

export const publishTask = (data) => request.post('/task', data)

export const updateTask = (id, data) => request.put(`/task/${id}`, data)

export const withdrawTask = (id) => request.post(`/task/${id}/withdraw`)

export const getMyTasks = (params) => request.get('/task/my', { params })

export const getMyFavorites = (params) => request.get('/task/favorite', { params })

export const toggleFavorite = (id) => request.post(`/task/${id}/favorite`)
