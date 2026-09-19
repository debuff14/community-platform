export const VISITOR_STATUS = {
  0: { text: '待到访', type: 'warning' },
  1: { text: '已入场', type: 'primary' },
  2: { text: '已离场', type: 'info' },
  3: { text: '已取消', type: 'info' }
}

export function visitorStatus(status) {
  return VISITOR_STATUS[status] || { text: '未知', type: 'info' }
}
