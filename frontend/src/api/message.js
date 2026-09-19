import request from './request'

export function getMessagePage(params) {
  return request.get('/message/page', { params })
}

export function getUnreadCount() {
  return request.get('/message/unread-count')
}

export function markAllRead() {
  return request.put('/message/read-all')
}
