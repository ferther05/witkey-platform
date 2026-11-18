// api/home.js：接口：首页平台概览与我的待办数量
import request from './request'

/** 首页公开概览（游客可读） */
// 接口：GET /public/overview
export const getOverview = () => request.get('/public/overview')

/** 当前用户的待办数量 */
// 接口：GET /order/todo
export const getTodoCounts = () => request.get('/order/todo')
