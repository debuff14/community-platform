import request from './request'

export function registerVisitor(data) {
  return request.post('/visitor', data)
}

export function getMyVisitorPage(params) {
  return request.get('/visitor/page', { params })
}

export function cancelVisitor(id) {
  return request.put(`/visitor/${id}/cancel`)
}

export function getAdminVisitorPage(params) {
  return request.get('/admin/visitor/page', { params })
}

export function enterVisitor(id) {
  return request.put(`/admin/visitor/${id}/enter`)
}

export function leaveVisitor(id) {
  return request.put(`/admin/visitor/${id}/leave`)
}
