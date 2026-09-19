export const GOODS_STATUS = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '在售', type: 'success' },
  2: { text: '已下架', type: 'info' },
  3: { text: '已驳回', type: 'danger' }
}

export const GOODS_CATEGORIES = {
  0: '其他',
  1: '图书',
  2: '家电',
  3: '生活用品'
}

export function goodsStatus(status) {
  return GOODS_STATUS[status] || { text: '未知', type: 'info' }
}

export function goodsCategory(category) {
  return GOODS_CATEGORIES[category] || '其他'
}
