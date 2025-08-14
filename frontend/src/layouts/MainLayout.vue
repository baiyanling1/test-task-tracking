<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="sidebarWidth" class="sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
      <div class="logo">
        <h2 v-if="!isCollapsed">测试任务跟踪系统</h2>
        <h2 v-else>测试</h2>
      </div>
      
      <!-- 折叠按钮 -->
      <div class="collapse-btn" @click="toggleSidebar">
        <el-icon>
          <ArrowLeft v-if="!isCollapsed" />
          <ArrowRight v-else />
        </el-icon>
      </div>
      
      <el-menu
        :default-active="$route.path"
        class="sidebar-menu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        :collapse="isCollapsed"
      >
        <el-menu-item index="/">
          <el-icon><DataBoard /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        
        <el-menu-item index="/tasks">
          <el-icon><List /></el-icon>
          <span>任务管理</span>
        </el-menu-item>
        
        <el-menu-item index="/users" v-if="canManageUsers">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        
        <el-menu-item index="/departments" v-if="canManageDepartments">
          <el-icon><OfficeBuilding /></el-icon>
          <span>部门管理</span>
        </el-menu-item>
        
        <el-menu-item index="/alerts">
          <el-icon><Bell /></el-icon>
          <span>提醒管理</span>
          <el-badge v-if="unreadCount > 0" :value="unreadCount" class="alert-badge" />
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.path">
              {{ item.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="user?.avatar">
                {{ user?.realName?.charAt(0)?.toUpperCase() || user?.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span class="username">{{ user?.realName || user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getUnreadAlertCount } from '@/api/alerts'
import { ElMessageBox } from 'element-plus'
import { DataBoard, List, User, Bell, ArrowDown, OfficeBuilding, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const user = computed(() => authStore.user)
const unreadCount = ref(0)

// 侧边栏折叠状态
const isCollapsed = ref(false)

// 计算侧边栏宽度
const sidebarWidth = computed(() => {
  return isCollapsed.value ? '64px' : '250px'
})

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// 计算属性
const canManageDepartments = computed(() => {
  return authStore.user?.role === 'ADMIN' || authStore.user?.role === 'MANAGER'
})

const canManageUsers = computed(() => {
  return authStore.user?.role === 'ADMIN' || authStore.user?.role === 'MANAGER'
})

// 面包屑导航
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.map(item => ({
    name: item.meta.title,
    path: item.path
  }))
})

// 处理用户菜单命令
const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await authStore.logout()
      router.push('/login')
    } catch {
      // 用户取消
    }
  }
}

// 加载未读提醒数量
const loadUnreadCount = async () => {
  // 检查用户是否已登录
  if (!authStore.user || !authStore.token) {
    unreadCount.value = 0
    return
  }
  
  try {
    const response = await getUnreadAlertCount()
    const count = response.unreadCount || 0
    unreadCount.value = count
  } catch (error) {
    console.error('加载未读提醒数量失败:', error)
    // 如果是权限错误，将未读数量设为0
    if (error.response?.status === 403) {
      unreadCount.value = 0
    }
  }
}

onMounted(() => {
  loadUnreadCount()
  // 每分钟刷新一次未读数量
  setInterval(loadUnreadCount, 60000)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  color: #bfcbd9;
  transition: width 0.3s ease;
  position: relative;
}

.sidebar-collapsed {
  width: 64px !important;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #1f2d3d;
  transition: all 0.3s ease;
}

.logo h2 {
  color: #fff;
  margin: 0;
  font-size: 18px;
  transition: all 0.3s ease;
}

.collapse-btn {
  position: absolute;
  top: 70px;
  right: -12px;
  width: 24px;
  height: 24px;
  background-color: #409EFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
  font-size: 12px;
  z-index: 1000;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.collapse-btn:hover {
  background-color: #66b1ff;
  transform: scale(1.1);
}

.sidebar-menu {
  border: none;
}

.alert-badge {
  margin-left: 8px;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  margin: 0 8px;
  color: #333;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style> 