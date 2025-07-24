import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getProfile } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(() => {
    const userStr = localStorage.getItem('user')
    return userStr ? JSON.parse(userStr) : null
  })

  const isAuthenticated = computed(() => !!token.value)

  const login = async (credentials) => {
    try {
      const response = await loginApi(credentials)
      token.value = response.token
      user.value = response.user
      
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(response.user))
      
      return { success: true }
    } catch (error) {
      // 抛出错误，让调用方处理
      throw error
    }
  }

  const logout = async () => {
    try {
      await logoutApi()
    } catch (error) {
      console.error('登出API调用失败:', error)
    } finally {
      token.value = ''
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }

  const loadProfile = async () => {
    if (!token.value) return
    
    try {
      const profile = await getProfile()
      user.value = profile
      localStorage.setItem('user', JSON.stringify(profile))
    } catch (error) {
      console.error('加载用户信息失败:', error)
      await logout()
    }
  }

  return {
    token,
    user,
    isAuthenticated,
    login,
    logout,
    loadProfile
  }
}) 