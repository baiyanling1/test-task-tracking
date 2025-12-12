<template>
  <el-dropdown @command="handleCommand" trigger="click" placement="bottom-end">
    <el-button :type="buttonType" :size="size" class="theme-switcher-btn">
      <span class="theme-icon">{{ currentThemeIcon }}</span>
      <span class="theme-name">{{ currentThemeName }}</span>
      <el-icon class="el-icon--right"><arrow-down /></el-icon>
    </el-button>
    <template #dropdown>
      <el-dropdown-menu class="theme-dropdown-menu">
        <div class="theme-dropdown-header">选择主题</div>
        <el-dropdown-item 
          v-for="(theme, key) in themes" 
          :key="key"
          :command="key"
          :class="{ 'is-active': currentTheme === key }"
          class="theme-dropdown-item"
        >
          <span class="theme-item-icon">{{ theme.icon }}</span>
          <span class="theme-item-name">{{ theme.name }}</span>
          <el-icon v-if="currentTheme === key" class="check-icon">
            <check />
          </el-icon>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { ElMessage } from 'element-plus'
import { ArrowDown, Check } from '@element-plus/icons-vue'

const props = defineProps({
  size: {
    type: String,
    default: 'default'
  },
  buttonType: {
    type: String,
    default: 'default'
  }
})

const themeStore = useThemeStore()
const currentTheme = computed(() => themeStore.currentTheme)
const themes = computed(() => themeStore.themes)

const currentThemeIcon = computed(() => {
  return themes.value[currentTheme.value]?.icon || '🎨'
})

const currentThemeName = computed(() => {
  return themes.value[currentTheme.value]?.name || '主题'
})

const handleCommand = (themeName) => {
  themeStore.setTheme(themeName)
  ElMessage.success({
    message: `已切换至 ${themes.value[themeName].name}`,
    duration: 2000,
    showClose: true
  })
}
</script>

<style scoped>
.theme-switcher-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.theme-switcher-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.theme-icon {
  font-size: 18px;
  line-height: 1;
}

.theme-name {
  font-size: 14px;
}

.theme-dropdown-header {
  padding: 12px 16px;
  font-size: 13px;
  color: #909399;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid #e4e7ed;
}

.theme-dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px !important;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.theme-dropdown-item:hover {
  background-color: #f5f7fa;
  padding-left: 24px !important;
}

.theme-dropdown-item.is-active {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 600;
}

.theme-item-icon {
  font-size: 20px;
  line-height: 1;
  flex-shrink: 0;
}

.theme-item-name {
  flex: 1;
  font-size: 14px;
}

.check-icon {
  color: #409eff;
  font-size: 16px;
  animation: checkBounce 0.3s ease;
}

@keyframes checkBounce {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

/* 深色模式适配 */
:deep(.el-dropdown-menu) {
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  padding: 0;
  overflow: hidden;
}

.theme-dark .theme-dropdown-header {
  background-color: var(--theme-backgroundSecondary);
  color: var(--theme-textSecondary);
  border-bottom-color: var(--theme-border);
}

.theme-dark .theme-dropdown-item:hover {
  background-color: var(--theme-backgroundSecondary);
}

.theme-dark .theme-dropdown-item.is-active {
  background-color: rgba(88, 166, 255, 0.2);
  color: #58a6ff;
}
</style>

