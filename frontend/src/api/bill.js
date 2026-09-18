import request from './request'

export function getMyBillPage(params) {
  return request.get('/bill/page', { params })
}

export function payBill(id) {
  return request.post(`/bill/${id}/pay`)
}

export function getAdminBillPage(params) {
  return request.get('/admin/bill/page', { params })
}

export function generateBills(data) {
  return request.post('/admin/bill/generate', data)
}

export function deleteBill(id) {
  return request.delete(`/admin/bill/${id}`)
}

export function getFeeStandards() {
  return request.get('/admin/bill/standard')
}

export function updateFeeStandards(data) {
  return request.put('/admin/bill/standard', data)
}

export function exportBills(params) {
  return request.get('/admin/bill/export', { params, responseType: 'blob' })
}
