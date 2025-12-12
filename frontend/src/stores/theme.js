import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

// 主题配置 - 精心设计的3个主题
export const themes = {
  // 默认主题 - 经典蓝色
  default: {
    name: '默认蓝',
    icon: '🔵',
    type: 'light',
    colors: {
      primary: '#409eff',
      primaryLight: '#ecf5ff',
      primaryDark: '#337ecc',
      success: '#67c23a',
      warning: '#e6a23c',
      danger: '#f56c6c',
      info: '#909399',
      background: '#f0f2f5',
      backgroundSecondary: '#f8f9fa',
      backgroundCard: '#ffffff',
      text: '#2c3e50',
      textSecondary: '#606266',
      border: '#e4e7ed',
      headerBg: '#ffffff',
      sidebarBg: '#304156',
      sidebarText: '#bfcbd9',
      sidebarTextActive: '#ffffff',
      sidebarActiveBg: 'rgba(64, 158, 255, 0.2)',
      sidebarHoverBg: 'rgba(255, 255, 255, 0.08)',
      tableHeaderBg: '#f5f7fa',
      tableHeaderText: '#606266',
      cardShadow: '0 2px 12px 0 rgba(0, 0, 0, 0.1)',
      hoverShadow: '0 4px 16px 0 rgba(0, 0, 0, 0.15)',
      // 图表配色
      chartColors: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
    }
  },
  
  // 深邃蓝 - 现代专业主题
  deepblue: {
    name: '深邃蓝',
    icon: '💎',
    type: 'light',
    colors: {
      primary: '#1890ff',
      primaryLight: '#e6f7ff',
      primaryDark: '#096dd9',
      success: '#52c41a',
      warning: '#faad14',
      danger: '#f5222d',
      info: '#8c8c8c',
      background: '#f0f2f5',
      backgroundSecondary: '#fafafa',
      backgroundCard: '#ffffff',
      text: '#262626',
      textSecondary: '#8c8c8c',
      border: '#e8e8e8',
      headerBg: '#001529',
      sidebarBg: '#001529',
      sidebarText: 'rgba(255, 255, 255, 0.65)',
      sidebarTextActive: '#ffffff',
      sidebarActiveBg: '#1890ff',
      sidebarHoverBg: 'rgba(255, 255, 255, 0.08)',
      tableHeaderBg: '#fafafa',
      tableHeaderText: '#262626',
      cardShadow: '0 2px 8px rgba(0, 0, 0, 0.09)',
      hoverShadow: '0 4px 12px rgba(0, 0, 0, 0.12)',
      // 图表配色 - 现代蓝色系
      chartColors: ['#1890ff', '#13c2c2', '#52c41a', '#faad14', '#f5222d']
    }
  },
  
  // 清新绿意 - 舒适绿色主题
  green: {
    name: '清新绿',
    icon: '🌿',
    type: 'light',
    colors: {
      primary: '#52c41a',
      primaryLight: '#f6ffed',
      primaryDark: '#389e0d',
      success: '#52c41a',
      warning: '#faad14',
      danger: '#ff4d4f',
      info: '#8c8c8c',
      background: '#f5f5f5',
      backgroundSecondary: '#fafafa',
      backgroundCard: '#ffffff',
      text: '#2c3e50',
      textSecondary: '#606266',
      border: '#e8e8e8',
      headerBg: '#ffffff',
      sidebarBg: '#ffffff',
      sidebarText: '#606266',
      sidebarTextActive: '#52c41a',
      sidebarActiveBg: 'rgba(82, 196, 26, 0.1)',
      sidebarHoverBg: 'rgba(82, 196, 26, 0.05)',
      tableHeaderBg: '#f6ffed',
      tableHeaderText: '#237804',
      cardShadow: '0 2px 12px 0 rgba(82, 196, 26, 0.08)',
      hoverShadow: '0 4px 16px 0 rgba(82, 196, 26, 0.12)',
      // 图表配色 - 绿色系
      chartColors: ['#52c41a', '#73d13d', '#95de64', '#b7eb8f', '#d9f7be']
    }
  }
}

export const useThemeStore = defineStore('theme', () => {
  const currentTheme = ref(localStorage.getItem('app_theme') || 'default')
  
  // 应用主题到 DOM
  const applyTheme = (themeName) => {
    const theme = themes[themeName]
    if (!theme) return
    
    const root = document.documentElement
    
    // 应用 CSS 变量
    Object.entries(theme.colors).forEach(([key, value]) => {
      root.style.setProperty(`--theme-${key}`, value)
    })
    
    // 添加特殊类名
    root.classList.remove('theme-light', 'theme-dark', 'theme-green')
    root.classList.add(`theme-${theme.type}`)
    if (themeName === 'green') {
      root.classList.add('theme-green')
    }
    
    // 设置背景
    document.body.style.background = theme.colors.background
  }
  
  // 切换主题
  const setTheme = (themeName) => {
    if (!themes[themeName]) return
    
    currentTheme.value = themeName
    localStorage.setItem('app_theme', themeName)
    applyTheme(themeName)
  }
  
  // 获取当前主题配置
  const getThemeConfig = () => {
    return themes[currentTheme.value] || themes.default
  }
  
  // 监听主题变化
  watch(currentTheme, (newTheme) => {
    applyTheme(newTheme)
  })
  
  // 初始化时应用主题
  applyTheme(currentTheme.value)
  
  return {
    currentTheme,
    themes,
    setTheme,
    getThemeConfig
  }
})

