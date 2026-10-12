import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

// Axios 实例：统一管理请求前缀、超时时间与响应拦截
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动携带 JWT 令牌
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理业务状态码与错误提示
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    if (res.code === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    ElMessage.error(res.msg || '操作失败')
    return Promise.reject(new Error(res.msg || '操作失败'))
  },
  (error) => {
    ElMessage.error(error.message || '网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
