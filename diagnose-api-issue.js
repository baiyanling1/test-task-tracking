// API问题诊断脚本
const axios = require('axios');

// 可能的后端地址
const POSSIBLE_BACKENDS = [
  'http://localhost:8080',
  'http://127.0.0.1:8080',
  'http://10.18.40.45:8080'
];

// 测试API端点
const TEST_ENDPOINTS = [
  '/api/dashboard',
  '/api/dashboard/inactive-users',
  '/api/tasks/stats'
];

async function testConnection(baseUrl, endpoint) {
  try {
    console.log(`🔍 测试: ${baseUrl}${endpoint}`);
    const response = await axios.get(`${baseUrl}${endpoint}`, {
      timeout: 3000,
      validateStatus: function (status) {
        return status < 500; // 只要不是服务器错误就算成功
      }
    });
    
    console.log(`   ✅ 响应状态: ${response.status}`);
    if (response.status === 401) {
      console.log(`   ℹ️  需要认证，但服务器可达`);
      return 'auth_required';
    } else if (response.status === 404) {
      console.log(`   ❌ 端点不存在`);
      return 'not_found';
    } else if (response.status < 400) {
      console.log(`   ✅ 成功响应`);
      return 'success';
    }
    return 'other';
  } catch (error) {
    if (error.code === 'ECONNREFUSED') {
      console.log(`   ❌ 连接被拒绝 (服务器未启动)`);
      return 'connection_refused';
    } else if (error.code === 'ETIMEDOUT') {
      console.log(`   ❌ 连接超时`);
      return 'timeout';
    } else {
      console.log(`   ❌ 错误: ${error.message}`);
      return 'error';
    }
  }
}

async function diagnoseAPI() {
  console.log('🏥 API连接诊断工具\n');
  
  let workingBackend = null;
  
  // 测试每个可能的后端地址
  for (const backend of POSSIBLE_BACKENDS) {
    console.log(`\n📡 测试后端服务器: ${backend}`);
    console.log('='.repeat(50));
    
    let hasWorkingEndpoint = false;
    
    for (const endpoint of TEST_ENDPOINTS) {
      const result = await testConnection(backend, endpoint);
      if (result === 'success' || result === 'auth_required') {
        hasWorkingEndpoint = true;
      }
    }
    
    if (hasWorkingEndpoint) {
      workingBackend = backend;
      console.log(`\n✅ 找到工作的后端服务器: ${backend}`);
      break;
    }
  }
  
  if (!workingBackend) {
    console.log('\n❌ 未找到可用的后端服务器');
    console.log('\n🔧 建议的解决步骤:');
    console.log('1. 确保后端Spring Boot应用已启动');
    console.log('2. 检查后端应用是否运行在端口8080');
    console.log('3. 检查防火墙设置');
    console.log('4. 确认网络连接');
  } else {
    console.log('\n🎯 建议的前端配置:');
    console.log(`将 vite.config.js 中的 target 设置为: ${workingBackend}`);
    
    // 生成正确的vite配置
    console.log('\n📝 推荐的 vite.config.js 配置:');
    console.log(`
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: '${workingBackend}',
      changeOrigin: true,
      secure: false
    }
  }
}`);
  }
  
  // 检查具体的未活跃用户API
  if (workingBackend) {
    console.log('\n🎯 专门测试未活跃用户API:');
    console.log('='.repeat(50));
    
    const inactiveUsersEndpoints = [
      '/api/dashboard/inactive-users',
      '/api/dashboard/inactive-users/range?startDate=2024-01-01&endDate=2024-01-07'
    ];
    
    for (const endpoint of inactiveUsersEndpoints) {
      await testConnection(workingBackend, endpoint);
    }
  }
}

// 运行诊断
if (require.main === module) {
  diagnoseAPI().catch(console.error);
}

module.exports = { diagnoseAPI };
