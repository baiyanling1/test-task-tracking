const axios = require('axios');

// 从环境变量或默认值获取后端地址
const backendUrl = process.env.VITE_API_BASE_URL || 'http://localhost:8080';

console.log('========================================');
console.log('测试任务跟踪系统 - 后端连接测试');
console.log('========================================');
console.log(`后端服务地址: ${backendUrl}`);
console.log('');

async function testConnection() {
  try {
    console.log('正在测试连接...');
    
    // 测试健康检查接口
    const healthResponse = await axios.get(`${backendUrl}/api/health`, {
      timeout: 5000
    });
    
    console.log('✅ 连接成功！');
    console.log(`状态码: ${healthResponse.status}`);
    console.log(`响应: ${JSON.stringify(healthResponse.data, null, 2)}`);
    
  } catch (error) {
    console.log('❌ 连接失败！');
    
    if (error.code === 'ECONNREFUSED') {
      console.log('错误: 连接被拒绝，请检查：');
      console.log('1. 后端服务是否已启动');
      console.log('2. 端口号是否正确');
      console.log('3. 防火墙设置');
    } else if (error.code === 'ENOTFOUND') {
      console.log('错误: 无法解析主机名，请检查：');
      console.log('1. 服务器地址是否正确');
      console.log('2. 网络连接是否正常');
    } else if (error.code === 'ETIMEDOUT') {
      console.log('错误: 连接超时，请检查：');
      console.log('1. 网络连接是否正常');
      console.log('2. 服务器是否响应');
    } else {
      console.log(`错误: ${error.message}`);
    }
    
    console.log('');
    console.log('请检查以下配置：');
    console.log(`1. 后端服务地址: ${backendUrl}`);
    console.log('2. 后端服务是否正常运行');
    console.log('3. 网络连接是否正常');
  }
}

// 运行测试
testConnection();
