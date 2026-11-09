import request from './request'

/** 首页公开概览（游客可读） */
export const getOverview = () => request.get('/public/overview')

/** 当前用户的待办数量 */
export const getTodoCounts = () => request.get('/order/todo')
