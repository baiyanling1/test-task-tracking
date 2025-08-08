// 简单的API测试脚本
const axios = require('axios');

const baseURL = 'http://localhost:8080';

async function testAPI() {
  try {
    // 测试登录
    const loginResponse = await axios.post(`${baseURL}/api/auth/login`, {
      username: 'admin',
      password: 'admin123'
    });
    
    const token = loginResponse.data.token;
    console.log('登录成功，获取到token');
    
    // 测试获取任务列表
    const tasksResponse = await axios.get(`${baseURL}/api/tasks`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    console.log('获取任务列表成功:', tasksResponse.data.content.length, '个任务');
    
    // 测试获取任务进度
    if (tasksResponse.data.content.length > 0) {
      const taskId = tasksResponse.data.content[0].id;
      const progressResponse = await axios.get(`${baseURL}/api/tasks/${taskId}/progress`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      console.log('获取任务进度成功:', progressResponse.data);
    }
    
  } catch (error) {
    console.error('API测试失败:', error.response?.data || error.message);
  }
}

testAPI();
