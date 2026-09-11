import request from './request'

export function getNoticePage(params) {
  return request.get('/notice/page', { params })
}

export function getNoticeDetail(id) {
  return request.get(`/notice/${id}`)
}
