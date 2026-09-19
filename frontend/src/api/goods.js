import request from './request'

export function publishGoods(data) {
  return request.post('/goods', data)
}

export function getMyGoodsPage(params) {
  return request.get('/goods/my/page', { params })
}

export function updateGoods(id, data) {
  return request.put(`/goods/${id}`, data)
}

export function offShelfGoods(id) {
  return request.put(`/goods/${id}/off-shelf`)
}

export function getMarketPage(params) {
  return request.get('/goods/market/page', { params })
}

export function getMarketDetail(id) {
  return request.get(`/goods/market/${id}`)
}

export function getSellerContact(id) {
  return request.get(`/goods/${id}/contact`)
}

export function getAdminGoodsPage(params) {
  return request.get('/admin/goods/page', { params })
}

export function approveGoods(id) {
  return request.put(`/admin/goods/${id}/approve`)
}

export function rejectGoods(id, data) {
  return request.put(`/admin/goods/${id}/reject`, data)
}

export function forceOffShelfGoods(id) {
  return request.put(`/admin/goods/${id}/off-shelf`)
}
