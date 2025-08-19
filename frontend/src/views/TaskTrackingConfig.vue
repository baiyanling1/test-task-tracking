<template>
  <div class="task-tracking-config-container">
    <div class="page-header">
      <h1>任务跟踪配置</h1>
    </div>

    <div class="config-section">
      <el-card>
        <template #header>
          <span>白名单配置</span>
        </template>
        
        <div class="whitelist-content">
          <p class="description">
            白名单中的用户将不会被任务跟踪提醒检查，默认包含admin用户。
          </p>
          
          <el-form :model="whitelistForm" label-width="120px">
            <el-form-item label="白名单用户">
              <el-tag
                v-for="username in whitelistForm.usernames"
                :key="username"
                closable
                @close="removeUser(username)"
                style="margin-right: 8px; margin-bottom: 8px;"
              >
                {{ getUserDisplayName(username) }}
              </el-tag>
              
              <el-select
                v-model="selectedUser"
                placeholder="选择用户添加到白名单"
                filterable
                clearable
                style="width: 300px;"
                @change="addUserToWhitelist"
              >
                <el-option
                  v-for="user in availableUsers"
                  :key="user.username"
                  :label="`${user.realName} (${user.username}) - ${user.department}`"
                  :value="user.username"
                  :disabled="whitelistForm.usernames.includes(user.username)"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveWhitelist" :loading="saving">
                保存配置
              </el-button>
              <el-button @click="loadWhitelist">刷新</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>

    <div class="help-section">
      <el-card>
        <template #header>
          <span>配置说明</span>
        </template>
        
        <div class="help-content">
          <h4>白名单功能说明：</h4>
          <ul>
            <li><strong>白名单用户</strong>：在白名单中的用户不会被任务跟踪提醒检查</li>
            <li><strong>默认配置</strong>：admin用户默认在白名单中</li>
            <li><strong>检查逻辑</strong>：系统会检查所有活跃用户（除白名单外）的任务填写情况</li>
            <li><strong>提醒时间</strong>：每周一上午9:30检查上周五的任务填写情况</li>
          </ul>
          
          <h4>使用说明：</h4>
          <ul>
            <li>从下拉列表中选择用户添加到白名单</li>
            <li>点击用户名标签上的"×"按钮从白名单中移除</li>
            <li>点击"保存配置"按钮保存当前白名单设置</li>
            <li>配置会立即生效，无需重启应用</li>
          </ul>
          
          <h4>注意事项：</h4>
          <ul>
            <li>下拉列表显示所有活跃用户，已添加的用户会被禁用</li>
            <li>建议只将不需要任务跟踪提醒的用户添加到白名单</li>
            <li>白名单配置会持久化保存，重启应用后仍然有效</li>
            <li>配置保存在应用目录下的 task-tracking-config.properties 文件中</li>
          </ul>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getWhitelist, saveWhitelist as saveWhitelistApi, getAllActiveUsers } from '../api/taskTrackingConfig'

// 响应式数据
const whitelistForm = ref({
  usernames: []
})
const saving = ref(false)
const selectedUser = ref('')
const availableUsers = ref([])

// 加载白名单配置
const loadWhitelist = async () => {
  try {
    const response = await getWhitelist()
    whitelistForm.value.usernames = response.whitelist || []
  } catch (error) {
    ElMessage.error('加载白名单配置失败')
    console.error('Load whitelist error:', error)
  }
}

// 加载用户列表
const loadUsers = async () => {
  try {
    const response = await getAllActiveUsers()
    availableUsers.value = response.users || []
  } catch (error) {
    ElMessage.error('加载用户列表失败')
    console.error('Load users error:', error)
  }
}

// 保存白名单配置
const saveWhitelist = async () => {
  try {
    saving.value = true
    await saveWhitelistApi(whitelistForm.value.usernames)
    ElMessage.success('白名单配置保存成功')
  } catch (error) {
    ElMessage.error('保存白名单配置失败: ' + (error.response?.data || error.message))
  } finally {
    saving.value = false
  }
}

// 移除用户
const removeUser = (username) => {
  const index = whitelistForm.value.usernames.indexOf(username)
  if (index > -1) {
    whitelistForm.value.usernames.splice(index, 1)
  }
}

// 添加用户到白名单
const addUserToWhitelist = (username) => {
  if (username && !whitelistForm.value.usernames.includes(username)) {
    whitelistForm.value.usernames.push(username)
    selectedUser.value = '' // 清空选择
  } else if (whitelistForm.value.usernames.includes(username)) {
    ElMessage.warning('用户已在白名单中')
    selectedUser.value = '' // 清空选择
  }
}

// 获取用户显示名称
const getUserDisplayName = (username) => {
  const user = availableUsers.value.find(u => u.username === username)
  if (user) {
    return `${user.realName} (${user.username})`
  }
  return username
}

// 组件挂载时加载配置
onMounted(() => {
  loadWhitelist()
  loadUsers()
})
</script>

<style scoped>
.task-tracking-config-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
}

.config-section,
.help-section {
  margin-bottom: 20px;
}

.whitelist-content {
  line-height: 1.6;
}

.description {
  margin-bottom: 20px;
  color: #606266;
}

.input-new-tag {
  width: 100px;
  margin-left: 8px;
  vertical-align: bottom;
}

.button-new-tag {
  margin-left: 8px;
  height: 32px;
  line-height: 30px;
  padding-top: 0;
  padding-bottom: 0;
}

.help-content {
  line-height: 1.6;
}

.help-content h4 {
  margin: 16px 0 8px 0;
  color: #303133;
}

.help-content ul {
  margin: 8px 0;
  padding-left: 20px;
}

.help-content li {
  margin: 4px 0;
}
</style>
