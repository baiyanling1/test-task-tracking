// 测试后端API是否存在的脚本
const axios = require('axios');

const BACKEND_URL = 'http://10.18.40.45:8080';

async function testAPI() {
  console.log('🔍 测试后端API是否存在...\n');
  console.log(`后端地址: ${BACKEND_URL}`);
  
  // 测试各个API端点
  const endpoints = [
    '/api/dashboard',
    '/api/dashboard/inactive-users',
    '/api/dashboard/inactive-users/range?startDate=2024-01-01&endDate=2024-01-07',
    '/api/tasks/stats',
    '/api/users'
  ];

  for (const endpoint of endpoints) {
    console.log(`\n📋 测试: ${endpoint}`);
    try {
      const response = await axios.get(`${BACKEND_URL}${endpoint}`, {
        timeout: 5000,
        validateStatus: function (status) {
          return status < 500; // 接受所有非服务器错误的状态码
        }
      });
      
      if (response.status === 200) {
        console.log(`   ✅ 状态: ${response.status} - API正常工作`);
      } else if (response.status === 401) {
        console.log(`   🔐 状态: ${response.status} - API存在但需要认证`);
      } else if (response.status === 403) {
        console.log(`   🚫 状态: ${response.status} - API存在但权限不足`);
      } else if (response.status === 404) {
        console.log(`   ❌ 状态: ${response.status} - API不存在`);
      } else {
        console.log(`   ⚠️  状态: ${response.status} - 其他响应`);
      }
      
    } catch (error) {
      if (error.code === 'ECONNREFUSED') {
        console.log(`   ❌ 连接被拒绝 - 服务器未启动或地址错误`);
        break; // 如果连接都失败了，后续测试没意义
      } else if (error.code === 'ETIMEDOUT') {
        console.log(`   ❌ 连接超时`);
      } else if (error.response) {
        console.log(`   ❌ 状态: ${error.response.status} - ${error.response.statusText}`);
      } else {
        console.log(`   ❌ 错误: ${error.message}`);
      }
    }
  }
  
  console.log('\n📊 结论和建议:');
  console.log('='.repeat(50));
  
  // 如果没有任何API可达，说明服务器问题
  console.log('1. 如果所有API都返回404，可能原因：');
  console.log('   - 后端服务器没有部署最新代码');
  console.log('   - Controller注解配置错误');
  console.log('   - 包扫描路径不正确');
  
  console.log('\n2. 如果连接被拒绝：');
  console.log('   - 服务器未启动');
  console.log('   - 防火墙阻挡');
  console.log('   - IP地址错误');
  
  console.log('\n3. 解决建议：');
  console.log('   - 检查后端控制台日志');
  console.log('   - 重新编译并启动后端服务');
  console.log('   - 确认Controller类正确注册');
}

// 专门测试我们新增的API
async function testNewInactiveUsersAPI() {
  console.log('\n🎯 专门测试新增的未活跃用户API...\n');
  
  const newEndpoints = [
    '/api/dashboard/inactive-users',
    '/api/dashboard/inactive-users/range?startDate=2024-01-01&endDate=2024-01-07'
  ];
  
  for (const endpoint of newEndpoints) {
    console.log(`📋 测试新API: ${endpoint}`);
    try {
      const response = await axios.get(`${BACKEND_URL}${endpoint}`, {
        timeout: 5000,
        headers: {
          'Authorization': 'Bearer dummy-token' // 使用虚拟token测试认证响应
        },
        validateStatus: function (status) {
          return status < 500;
        }
      });
      
      if (response.status === 401) {
        console.log(`   ✅ API存在但需要认证 (这是正常的)`);
      } else if (response.status === 403) {
        console.log(`   ✅ API存在但权限不足 (这是正常的)`);
      } else if (response.status === 404) {
        console.log(`   ❌ API不存在 - 需要检查后端部署`);
      } else {
        console.log(`   ✅ API响应正常: ${response.status}`);
      }
      
    } catch (error) {
      if (error.response?.status === 404) {
        console.log(`   ❌ API不存在 - Controller可能未正确部署`);
      } else {
        console.log(`   ❌ 测试失败: ${error.message}`);
      }
    }
  }
}

async function main() {
  await testAPI();
  await testNewInactiveUsersAPI();
  
  console.log('\n💡 下一步操作建议:');
  console.log('1. 如果API不存在，请在后端服务器上：');
  console.log('   mvn clean compile');
  console.log('   mvn spring-boot:run');
  console.log('2. 检查后端启动日志中是否有错误');
  console.log('3. 确认DashboardController被正确扫描和注册');
}

if (require.main === module) {
  main().catch(console.error);
}
