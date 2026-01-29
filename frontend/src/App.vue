<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

onMounted(async () => {
  // 应用启动时检查token并加载用户信息
  if (authStore.token) {
    try {
      await authStore.loadProfile()
    } catch (error) {
      console.error('启动时加载用户信息失败:', error)
      // 如果加载失败，清除token并跳转到登录页
      // 注意：不在这里显示错误提示，因为响应拦截器会处理401错误
      // 如果已经在登录页，则不需要跳转
      if (router.currentRoute.value?.path !== '/login') {
        await authStore.logout()
        router.push('/login')
      }
    }
  }
})
</script>

<style>
#app {
  height: 100vh;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}
</style> 