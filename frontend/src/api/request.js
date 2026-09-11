import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

service.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '操作失败')
      if (res.code === 401) {
        redirectToLogin()
      }
      return Promise.reject(new Error(res.msg || '操作失败'))
    }
    return res.data
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      redirectToLogin()
    } else {
      ElMessage.error(error.response?.data?.msg || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

function redirectToLogin() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  if (!window.location.pathname.startsWith('/login')) {
    window.location.href = '/login'
  }
}

export default service
