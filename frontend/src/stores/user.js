import { defineStore } from 'pinia'

/**
 * 登录用户状态：保存令牌与用户信息，并持久化到 localStorage，刷新页面后仍保持登录。
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  getters: {
    roleCode: (state) => state.userInfo.roleCode || '',
    realName: (state) => state.userInfo.realName || '',
    isAdmin: (state) => state.userInfo.roleCode === 'ADMIN'
  },
  actions: {
    setLoginInfo(data) {
      this.token = data.token
      this.userInfo = data
      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(data))
    },
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
