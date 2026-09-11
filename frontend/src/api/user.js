import request from './request'

export function getProfile() {
  return request.get('/user/profile')
}

export function getStats() {
  return request.get('/user/stats')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function changePassword(data) {
  return request.put('/user/password', data)
}
