import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

import App from './App.vue'
import router from './router'
import './style.css'
import './assets/theme.css'  // 导入全局主题样式

// 配置dayjs时区插件
dayjs.extend(utc)
dayjs.extend(timezone)
// 设置默认时区为中国标准时间
dayjs.tz.setDefault('Asia/Shanghai')

const pinia = createPinia()
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})

// 初始化主题
import { useThemeStore } from './stores/theme'
const themeStore = useThemeStore()

app.mount('#app') 