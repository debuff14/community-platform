import request from './request'

export function register(data) {
  return request.post('/auth/register', data)
}

export function checkUsername(username) {
  return request.get('/auth/check-username', { params: { username } })
}

export function login(data) {
  return request.post('/auth/login', data)
}
