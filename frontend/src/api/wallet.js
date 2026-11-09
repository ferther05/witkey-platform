import request from './request'

export const getWallet = () => request.get('/wallet')

export const getFlows = (params) => request.get('/wallet/flows', { params })

export const recharge = (amount) => request.post('/wallet/recharge', { amount })

export const withdraw = (amount) => request.post('/wallet/withdraw', { amount })

export const reconcile = () => request.get('/wallet/reconcile')

export const getFinanceSummary = () => request.get('/admin/finance/summary')
