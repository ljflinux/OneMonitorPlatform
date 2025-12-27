import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import router from '../router'

// 创建Axios实例
const instance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
instance.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    // 添加token到请求头
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  response => {
    // 直接返回数据
    return response.data
  },
  error => {
    let message = '未知错误'
    
    if (error.response) {
      // 服务器返回错误
      switch (error.response.status) {
        case 401:
          message = '请先登录'
          // 跳转到登录页面
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          break
        case 403:
          message = '没有权限访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = error.response.data?.detail || error.response.data?.message || `请求失败 (${error.response.status})`
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      message = '网络错误，服务器未响应'
    } else {
      // 请求配置错误
      message = error.message
    }
    
    // 显示错误消息
    ElMessage.error(message)
    
    return Promise.reject(error)
  }
)

export default instance