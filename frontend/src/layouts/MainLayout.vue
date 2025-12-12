<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '250px'" class="sidebar">
      <div class="logo">
        <h2 v-if="!isCollapsed">测试任务跟踪系统</h2>
        <h2 v-else>测试</h2>
      </div>
      
      <el-menu
        :default-active="$route.path"
        class="sidebar-menu"
        router
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
        
        <el-menu-item index="/scheduled-tasks" v-if="isAdmin">
          <el-icon><Clock /></el-icon>
          <span>定时任务管理</span>
        </el-menu-item>
        
        <el-menu-item index="/webhook-config" v-if="isAdmin">
          <el-icon><Message /></el-icon>
          <span>Webhook配置</span>
        </el-menu-item>
        
        <el-menu-item index="/task-tracking-config" v-if="isAdmin">
          <el-icon><UserFilled /></el-icon>
          <span>任务跟踪配置</span>
        </el-menu-item>
        
        <el-menu-item index="/alert-notification-config" v-if="isAdmin">
          <el-icon><Setting /></el-icon>
          <span>告警通知配置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-button
            type="text"
            @click="toggleCollapse"
            class="collapse-btn"
          >
            <el-icon>
              <Expand v-if="isCollapsed" />
              <Fold v-else />
            </el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.path">
              {{ item.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 主题切换器 -->
          <ThemeSwitcher size="small" style="margin-right: 16px;" />
          
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
import { DataBoard, List, User, Bell, ArrowDown, OfficeBuilding, Expand, Fold, Clock, UserFilled, Setting, Message } from '@element-plus/icons-vue'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const user = computed(() => authStore.user)
const unreadCount = ref(0)
const isCollapsed = ref(false)

// 切换菜单折叠状态
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

// 计算属性
const canManageDepartments = computed(() => {
  return authStore.user?.role === 'ADMIN' || authStore.user?.role === 'MANAGER'
})

const canManageUsers = computed(() => {
  return authStore.user?.role === 'ADMIN' || authStore.user?.role === 'MANAGER'
})

const isAdmin = computed(() => {
  return authStore.user?.role === 'ADMIN'
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

/* 侧边栏 */
.sidebar {
  background: var(--theme-sidebarBg, #304156) !important;
  transition: all 0.3s ease;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

/* 清新绿主题特殊处理 - 添加左侧绿色边框 */
.theme-light.theme-green .sidebar {
  border-right: 3px solid #52c41a;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  background: rgba(0, 0, 0, 0.05);
}

.logo h2 {
  color: var(--theme-sidebarTextActive, #fff);
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* 菜单样式 */
.sidebar-menu {
  border: none !important;
  background: transparent !important;
}

/* 菜单项默认样式 */
:deep(.sidebar-menu .el-menu-item) {
  color: var(--theme-sidebarText, #bfcbd9) !important;
  background: transparent !important;
  transition: all 0.3s ease;
  font-size: 14px;
  font-weight: 500;
  border-left: 3px solid transparent;
}

/* 菜单项悬停 */
:deep(.sidebar-menu .el-menu-item:hover) {
  background: var(--theme-sidebarHoverBg, rgba(255, 255, 255, 0.08)) !important;
  color: var(--theme-sidebarTextActive, #ffffff) !important;
}

/* 菜单项激活状态 - 重点优化 */
:deep(.sidebar-menu .el-menu-item.is-active) {
  background: var(--theme-sidebarActiveBg, rgba(64, 158, 255, 0.2)) !important;
  color: var(--theme-sidebarTextActive, #ffffff) !important;
  border-left: 3px solid var(--theme-primary, #409eff) !important;
  font-weight: 600;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.1);
}

/* 菜单图标 */
:deep(.sidebar-menu .el-menu-item .el-icon) {
  margin-right: 8px;
  font-size: 16px;
}

.alert-badge {
  margin-left: 8px;
}

/* 顶部导航 */
.header {
  background: var(--theme-headerBg, #fff) !important;
  border-bottom: 1px solid var(--theme-border, #e6e6e6);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: var(--theme-cardShadow, 0 2px 4px rgba(0,0,0,0.1));
  transition: all 0.3s ease;
  z-index: 10;
}

.header-left {
  flex: 1;
  display: flex;
  align-items: center;
}

.collapse-btn {
  margin-right: 16px;
  font-size: 18px;
  color: var(--theme-text, #333);
  transition: all 0.3s ease;
}

.collapse-btn:hover {
  color: var(--theme-primary, #409eff);
  transform: scale(1.1);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.user-info:hover {
  background-color: var(--theme-backgroundSecondary, #f5f5f5);
}

.username {
  margin: 0 8px;
  color: var(--theme-text, #333);
  font-weight: 500;
}

/* 内容区 */
.main-content {
  background: var(--theme-background, #f0f2f5) !important;
  padding: 20px;
  transition: all 0.3s ease;
  overflow-y: auto;
}

/* 面包屑 */
:deep(.el-breadcrumb) {
  font-size: 14px;
}

:deep(.el-breadcrumb__item) {
  color: var(--theme-textSecondary, #606266);
}

:deep(.el-breadcrumb__item.is-link) {
  color: var(--theme-text, #2c3e50);
  font-weight: 500;
}

/* 深色主题特殊处理 */
.theme-dark .header-right .el-avatar {
  border: 2px solid var(--theme-border, #303030);
}
</style> 