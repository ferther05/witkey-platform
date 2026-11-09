/** 投标 / 订单状态文案（与后端枚举一一对应） */
const BID_META = {
  PENDING: { text: '待选', type: 'warning' },
  WIN: { text: '中标', type: 'success' },
  LOSE: { text: '未中标', type: 'info' },
  WITHDRAWN: { text: '已撤回', type: 'info' }
}

const ORDER_META = {
  TO_DELIVER: { text: '待交付', type: 'warning' },
  TO_ACCEPT: { text: '待验收', type: 'primary' },
  DONE: { text: '已完成', type: 'success' },
  REFUNDED: { text: '已退款', type: 'info' }
}

export const bidStatus = (s) => BID_META[s] || { text: s, type: 'info' }
export const orderStatus = (s) => ORDER_META[s] || { text: s, type: 'info' }

const FLOW_META = {
  RECHARGE: { text: '充值', type: 'success' },
  FREEZE: { text: '冻结', type: 'warning' },
  UNFREEZE: { text: '解冻', type: 'info' },
  SETTLE_PAY: { text: '托管扣划', type: 'danger' },
  SETTLE_IN: { text: '结算到账', type: 'success' },
  COMMISSION: { text: '佣金', type: 'danger' },
  WITHDRAW: { text: '提现', type: 'info' }
}
export const flowStatus = (s) => FLOW_META[s] || { text: s, type: 'info' }
