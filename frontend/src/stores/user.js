import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null') || {}
  }),
  getters: {
    isAdmin: (state) => state.userInfo.role === 1
  },
  actions: {
    setLogin(data) {
      this.token = data.token
      this.userInfo = {
        id: data.id,
        username: data.username,
        role: data.role,
        phone: data.phone,
        building: data.building,
        roomNo: data.roomNo
      }
      localStorage.setItem('token', this.token)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },
    setUserInfo(info) {
      this.userInfo = { ...this.userInfo, ...info }
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
