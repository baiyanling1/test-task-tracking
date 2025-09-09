// 修复vite配置的脚本
const fs = require('fs');
const path = require('path');

const viteConfigPath = path.join(__dirname, 'frontend', 'vite.config.js');

// 读取当前配置
console.log('📖 读取当前vite配置...');
let content;
try {
  content = fs.readFileSync(viteConfigPath, 'utf8');
  console.log('✅ 成功读取vite.config.js');
} catch (error) {
  console.error('❌ 读取vite.config.js失败:', error.message);
  process.exit(1);
}

console.log('\n📋 当前代理配置:');
console.log(content);

// 创建新的配置，使用localhost作为后端地址
const newConfig = `import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    server: {
      port: parseInt(env.VITE_DEV_PORT) || 3000,
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
          rewrite: (path) => path.replace(/^\/api/, '/api')
        }
      }
    },
    build: {
      outDir: 'dist',
      assetsDir: 'assets'
    }
  }
})`;

// 备份原文件
const backupPath = viteConfigPath + '.backup';
try {
  fs.writeFileSync(backupPath, content);
  console.log(`\n💾 已创建备份文件: ${backupPath}`);
} catch (error) {
  console.error('❌ 创建备份失败:', error.message);
}

// 写入新配置
try {
  fs.writeFileSync(viteConfigPath, newConfig);
  console.log('✅ 已更新vite.config.js');
} catch (error) {
  console.error('❌ 更新vite.config.js失败:', error.message);
  process.exit(1);
}

// 创建.env.development文件
const envDevPath = path.join(__dirname, 'frontend', '.env.development');
const envContent = `# 开发环境配置
# 后端服务地址
VITE_API_BASE_URL=http://localhost:8080

# 开发服务器端口
VITE_DEV_PORT=3000

# 是否启用调试模式
VITE_DEBUG=true
`;

try {
  fs.writeFileSync(envDevPath, envContent);
  console.log('✅ 已创建.env.development文件');
} catch (error) {
  console.error('❌ 创建.env.development失败:', error.message);
}

console.log('\n🎉 配置修复完成！');
console.log('\n📋 下一步操作:');
console.log('1. 确保后端Spring Boot应用运行在 http://localhost:8080');
console.log('2. 重启前端开发服务器 (npm run dev)');
console.log('3. 尝试访问仪表盘页面');
console.log('\n💡 如果仍有问题，请运行: node diagnose-api-issue.js');
