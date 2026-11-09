/** 状态 → 文案 / 颜色（全站统一，别在页面里各写一套） */
const STATUS_META = {
  PENDING_AUDIT: { text: '待审核', type: 'warning' },
  REJECTED: { text: '审核驳回', type: 'danger' },
  RECRUITING: { text: '招募中', type: 'primary' },
  SELECTED: { text: '已选标', type: 'success' },
  IN_PROGRESS: { text: '进行中', type: 'success' },
  TO_ACCEPT: { text: '待验收', type: 'warning' },
  DONE: { text: '已完成', type: 'success' },
  WITHDRAWN: { text: '已撤回', type: 'info' },
  FLOWED: { text: '已流标', type: 'info' }
}

export const taskStatus = (status) => STATUS_META[status] || { text: status, type: 'info' }

export function formatTime(value, withTime = true) {
  if (!value) return '-'
  const s = String(value).replace('T', ' ')
  return withTime ? s.slice(0, 16) : s.slice(0, 10)
}

export function formatMoney(value) {
  const n = Number(value || 0)
  return n.toFixed(2)
}

/** 剩余时间：返回「剩 3 天 4 小时」这样的字符串 */
export function remainText(deadline) {
  if (!deadline) return '-'
  const end = new Date(String(deadline).replace(' ', 'T')).getTime()
  const diff = end - Date.now()
  if (diff <= 0) return '已截止'
  const days = Math.floor(diff / 86400000)
  const hours = Math.floor((diff % 86400000) / 3600000)
  if (days > 0) return `剩 ${days} 天 ${hours} 小时`
  const minutes = Math.floor((diff % 3600000) / 60000)
  return `剩 ${hours} 小时 ${minutes} 分`
}
