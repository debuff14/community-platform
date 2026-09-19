export const REPAIR_STATUS = {
  0: { text: '已提交', type: 'info' },
  1: { text: '已接单', type: 'warning' },
  2: { text: '处理中', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已评价', type: 'success', effect: 'dark' },
  5: { text: '已取消', type: 'info' }
}

export const REPAIR_TYPES = {
  0: '其他',
  1: '水电气',
  2: '门窗',
  3: '电梯'
}

export function repairStatus(status) {
  return REPAIR_STATUS[status] || { text: '未知', type: 'info' }
}

export function repairTypeName(type) {
  return REPAIR_TYPES[type] || '其他'
}

export function repairImages(images) {
  if (!images) return []
  return String(images).split(',').filter(Boolean)
}
