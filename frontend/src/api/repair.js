import request from './request'

export function createRepair(data) {
  return request.post('/repair', data)
}

export function getMyRepairPage(params) {
  return request.get('/repair/page', { params })
}

export function getRepairDetail(id) {
  return request.get(`/repair/${id}`)
}

export function evaluateRepair(id, data) {
  return request.post(`/repair/${id}/comment`, data)
}

export function cancelRepair(id) {
  return request.put(`/repair/${id}/cancel`)
}

export function getAdminRepairPage(params) {
  return request.get('/admin/repair/page', { params })
}

export function getAdminRepairDetail(id) {
  return request.get(`/admin/repair/${id}`)
}

export function acceptRepair(id) {
  return request.put(`/admin/repair/${id}/accept`)
}

export function startRepair(id) {
  return request.put(`/admin/repair/${id}/start`)
}

export function completeRepair(id, data) {
  return request.put(`/admin/repair/${id}/complete`, data)
}
