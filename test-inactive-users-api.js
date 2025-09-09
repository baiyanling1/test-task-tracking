// 测试未活跃用户API的脚本
const axios = require('axios');

// 配置
const API_BASE_URL = 'http://localhost:8080/api';
const TEST_TOKEN = 'your_jwt_token_here'; // 需要替换为实际的JWT token

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Authorization': `Bearer ${TEST_TOKEN}`,
    'Content-Type': 'application/json'
  }
});

// 测试函数
async function testInactiveUsersAPI() {
  console.log('🚀 开始测试未活跃用户API...\n');

  try {
    // 1. 测试获取上周未活跃用户
    console.log('📋 测试1: 获取上周未活跃用户');
    console.log('请求URL:', `${API_BASE_URL}/dashboard/inactive-users`);
    
    const lastWeekResponse = await api.get('/dashboard/inactive-users');
    console.log('✅ 成功获取上周未活跃用户数据:');
    console.log('- 总用户数:', lastWeekResponse.data.totalUsers);
    console.log('- 未活跃用户数:', lastWeekResponse.data.inactiveCount);
    console.log('- 时间范围:', lastWeekResponse.data.startDate, '至', lastWeekResponse.data.endDate);
    console.log('- 未活跃用户列表长度:', lastWeekResponse.data.inactiveUsers.length);
    console.log('');

    // 2. 测试自定义时间范围
    console.log('📋 测试2: 获取自定义时间范围内未活跃用户');
    const startDate = '2024-01-01';
    const endDate = '2024-01-07';
    console.log('请求URL:', `${API_BASE_URL}/dashboard/inactive-users/range?startDate=${startDate}&endDate=${endDate}`);
    
    const customRangeResponse = await api.get('/dashboard/inactive-users/range', {
      params: { startDate, endDate }
    });
    console.log('✅ 成功获取自定义时间范围未活跃用户数据:');
    console.log('- 总用户数:', customRangeResponse.data.totalUsers);
    console.log('- 未活跃用户数:', customRangeResponse.data.inactiveCount);
    console.log('- 时间范围:', customRangeResponse.data.startDate, '至', customRangeResponse.data.endDate);
    console.log('- 未活跃用户列表长度:', customRangeResponse.data.inactiveUsers.length);

    if (customRangeResponse.data.inactiveUsers.length > 0) {
      console.log('- 第一个未活跃用户示例:');
      const firstUser = customRangeResponse.data.inactiveUsers[0];
      console.log('  - 姓名:', firstUser.realName);
      console.log('  - 用户名:', firstUser.username);
      console.log('  - 部门:', firstUser.department);
      console.log('  - 分配任务数:', firstUser.assignedTaskCount);
    }

    console.log('\n🎉 所有API测试通过！');

  } catch (error) {
    console.error('❌ API测试失败:');
    if (error.response) {
      console.error('- HTTP状态码:', error.response.status);
      console.error('- 错误信息:', error.response.data);
      console.error('- 请求URL:', error.config.url);
    } else if (error.request) {
      console.error('- 网络错误: 无法连接到服务器');
      console.error('- 请确保后端服务器已启动并运行在', API_BASE_URL);
    } else {
      console.error('- 未知错误:', error.message);
    }
  }
}

// 检查服务器连接
async function checkServerConnection() {
  console.log('🔍 检查服务器连接...');
  try {
    const response = await axios.get(`${API_BASE_URL}/dashboard`, {
      timeout: 5000,
      headers: {
        'Authorization': `Bearer ${TEST_TOKEN}`
      }
    });
    console.log('✅ 服务器连接正常');
    return true;
  } catch (error) {
    console.error('❌ 服务器连接失败:');
    if (error.code === 'ECONNREFUSED') {
      console.error('- 后端服务器未启动，请启动Spring Boot应用');
    } else if (error.response?.status === 401) {
      console.error('- 认证失败，请检查JWT token');
    } else if (error.response?.status === 404) {
      console.error('- API路径不存在，请检查Controller映射');
    } else {
      console.error('- 错误详情:', error.message);
    }
    return false;
  }
}

// 主函数
async function main() {
  console.log('🧪 未活跃用户API测试工具\n');
  console.log('⚠️  请确保:');
  console.log('1. 后端Spring Boot应用已启动 (http://localhost:8080)');
  console.log('2. 已将有效的JWT token配置到TEST_TOKEN变量中');
  console.log('3. 当前用户具有ADMIN或MANAGER权限\n');

  const isConnected = await checkServerConnection();
  if (isConnected) {
    await testInactiveUsersAPI();
  }
}

// 如果直接运行此脚本
if (require.main === module) {
  main().catch(console.error);
}

module.exports = { testInactiveUsersAPI, checkServerConnection };
