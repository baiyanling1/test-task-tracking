import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: 'tasks',
        name: 'Tasks',
        component: () => import('@/views/Tasks.vue')
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/Users.vue')
      },
      {
        path: 'departments',
        name: 'Departments',
        component: () => import('@/views/Departments.vue')
      },
      {
        path: 'alerts',
        name: 'Alerts',
        component: () => import('@/views/Alerts.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 如果需要认证
  if (to.meta.requiresAuth) {
    // 检查是否有token
    if (!authStore.token) {
      next('/login')
      return
    }
    
    // 如果有token但没有用户信息，尝试加载用户信息
    if (!authStore.user && authStore.token) {
      try {
        await authStore.loadProfile()
      } catch (error) {
        console.error('路由守卫中加载用户信息失败:', error)
        await authStore.logout()
        next('/login')
        return
      }
    }
    
    // 如果加载用户信息失败，跳转到登录页
    if (!authStore.user) {
      await authStore.logout()
      next('/login')
      return
    }
  }
  
  // 如果已登录用户访问登录页，重定向到首页
  if (to.path === '/login' && authStore.isAuthenticated) {
    next('/')
    return
  }
  
  next()
})

export default router 