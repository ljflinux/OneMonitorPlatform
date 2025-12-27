import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '../utils/axios'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const fullName = computed(() => userInfo.value?.full_name || '')
  const email = computed(() => userInfo.value?.email || '')

  // 方法
  async function login(username, password) {
    try {
      const response = await axios.post('/auth/login', {
        username,
        password
      })
      
      // 保存token
      token.value = response.data.access_token
      localStorage.setItem('token', token.value)
      
      // 获取用户信息
      await fetchUserInfo()
      
      return response.data
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    }
  }

  async function fetchUserInfo() {
    try {
      const response = await axios.get('/users/me')
      userInfo.value = response.data
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      return response.data
    } catch (error) {
      console.error('Failed to fetch user info:', error)
      throw error
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    fullName,
    email,
    login,
    fetchUserInfo,
    logout
  }
})