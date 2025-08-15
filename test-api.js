// 测试新功能的API脚本
const axios = require('axios');

const BASE_URL = 'http://localhost:8080/api';

// 测试配置
const config = {
  headers: {
    'Authorization': 'Bearer YOUR_TOKEN_HERE', // 需要替换为实际的token
    'Content-Type': 'application/json'
  }
};

// 测试函数
async function testNewFeatures() {
  console.log('开始测试新功能...\n');

  try {
    // 1. 测试本月个人任务统计
    console.log('1. 测试本月个人任务统计...');
    const currentMonthStats = await axios.get(`${BASE_URL}/tasks/statistics/user-tasks-by-month?month=current`, config);
    console.log('本月个人任务统计:', JSON.stringify(currentMonthStats.data, null, 2));
    console.log('✓ 本月个人任务统计API正常\n');

    // 2. 测试上月个人任务统计
    console.log('2. 测试上月个人任务统计...');
    const lastMonthStats = await axios.get(`${BASE_URL}/tasks/statistics/user-tasks-by-month?month=last`, config);
    console.log('上月个人任务统计:', JSON.stringify(lastMonthStats.data, null, 2));
    console.log('✓ 上月个人任务统计API正常\n');

    // 3. 测试任务统计
    console.log('3. 测试任务统计...');
    const taskStats = await axios.get(`${BASE_URL}/tasks/stats`, config);
    console.log('任务统计:', JSON.stringify(taskStats.data, null, 2));
    console.log('✓ 任务统计API正常\n');

    console.log('所有测试通过！新功能正常工作。');

  } catch (error) {
    console.error('测试失败:', error.response?.data || error.message);
  }
}

// 运行测试
testNewFeatures();
