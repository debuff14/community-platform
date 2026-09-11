import request from './request'

export function getAdminNoticePage(params) {
  return request.get('/admin/notice/page', { params })
}

export function addNotice(data) {
  return request.post('/admin/notice', data)
}

export function updateNotice(id, data) {
  return request.put(`/admin/notice/${id}`, data)
}

export function deleteNotice(id) {
  return request.delete(`/admin/notice/${id}`)
}
