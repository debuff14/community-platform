import request from './request'

export function uploadImage(file) {
  const data = new FormData()
  data.append('file', file)
  return request.post('/upload/image', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
