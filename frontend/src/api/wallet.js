// api/wallet.js：接口：钱包、流水、充值、提现、对账
import request from './request'

// 接口：GET /wallet
export const getWallet = () => request.get('/wallet')

// 接口：GET /wallet/flows
export const getFlows = (params) => request.get('/wallet/flows', { params })

// 接口：POST /wallet/recharge
export const recharge = (amount) => request.post('/wallet/recharge', { amount })

// 接口：POST /wallet/withdraw
export const withdraw = (amount) => request.post('/wallet/withdraw', { amount })

// 接口：GET /wallet/reconcile
export const reconcile = () => request.get('/wallet/reconcile')

// 接口：GET /admin/finance/summary
export const getFinanceSummary = () => request.get('/admin/finance/summary')

// 接口：GET /admin/finance/flows（平台流水，管理员）
export const adminFlows = (params) => request.get('/admin/finance/flows', { params })

// 接口：GET /admin/finance/flows/export（导出 CSV，返回二进制）
export const adminExportFlows = (params) =>
  request.get('/admin/finance/flows/export', { params, responseType: 'blob' })
