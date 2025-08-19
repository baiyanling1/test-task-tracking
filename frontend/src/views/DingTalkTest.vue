<template>
  <div class="dingtalk-test-container">
    <div class="page-header">
      <h1>钉钉配置</h1>
    </div>

    <div class="config-section">
      <el-card>
        <template #header>
          <span>钉钉配置</span>
          <el-button style="float: right; padding: 3px 0" type="text" @click="toggleEdit">
            {{ isEditing ? '取消编辑' : '编辑配置' }}
          </el-button>
        </template>
        
        <div v-if="!isEditing">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="启用状态">
              <el-tag :type="config.enabled ? 'success' : 'danger'">
                {{ config.enabled ? '已启用' : '已禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Webhook地址">
              <div>
                {{ config.webhookUrl || '未配置' }}
                <el-tag v-if="config.webhookUrl && !config.webhookUrl.includes('98b6b18dfda2b07323b01b7050fce8a34ffce394815428c85eef1cff4ee41726')" type="warning" size="small" style="margin-left: 8px;">
                  请确认webhook地址正确
                </el-tag>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="密钥">
              {{ config.secret && config.secret.trim() ? '******' : '未配置' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div v-else>
          <el-form :model="editForm" label-width="120px">
            <el-form-item label="启用钉钉通知">
              <el-switch v-model="editForm.enabled" />
            </el-form-item>
            
            <el-form-item label="Webhook地址" :rules="[{ required: editForm.enabled, message: '启用时必须填写webhook地址', trigger: 'blur' }]">
              <el-input 
                v-model="editForm.webhookUrl" 
                placeholder="https://oapi.dingtalk.com/robot/send?access_token=98b6b18dfda2b07323b01b7050fce8a34ffce394815428c85eef1cff4ee41726"
                :disabled="!editForm.enabled"
              />
            </el-form-item>
            
            <el-form-item label="签名密钥">
              <el-input 
                v-model="editForm.secret" 
                placeholder="可选，如果使用签名方式请填写"
                :disabled="!editForm.enabled"
                show-password
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
              <el-button @click="cancelEdit">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>

    <div class="test-section">
      <el-card>
        <template #header>
          <span>测试操作</span>
        </template>
        
        <el-button type="primary" @click="testDingTalk" :loading="testing">
          <el-icon><Message /></el-icon>
          发送测试消息
        </el-button>
        
        <div v-if="testResult" class="test-result">
          <el-alert
            :title="testResult.success ? '测试成功' : '测试失败'"
            :type="testResult.success ? 'success' : 'error'"
            :description="testResult.message"
            show-icon
          />
        </div>
      </el-card>
    </div>

    <div class="help-section">
      <el-card>
        <template #header>
          <span>配置说明</span>
        </template>
        
        <div class="help-content">
          <h4>钉钉配置步骤：</h4>
          <ol>
            <li>在钉钉群中添加自定义机器人</li>
            <li>获取webhook地址</li>
            <li>配置安全设置（关键词或签名）</li>
            <li>点击"编辑配置"按钮，填写配置信息</li>
            <li>点击"保存配置"保存设置</li>
          </ol>
          
          <h4>配置说明：</h4>
          <ul>
            <li><strong>启用钉钉通知</strong>：开启或关闭钉钉通知功能</li>
            <li><strong>Webhook地址</strong>：钉钉机器人的webhook地址，格式为：<code>https://oapi.dingtalk.com/robot/send?access_token=your_token</code></li>
            <li><strong>签名密钥</strong>：如果使用签名方式，请填写签名密钥（可选）</li>
          </ul>
          
          <h4>测试说明：</h4>
          <ul>
            <li>配置完成后，点击"发送测试消息"按钮测试钉钉配置</li>
            <li>如果配置正确，钉钉群会收到测试消息</li>
            <li>如果失败，请检查webhook地址和密钥配置</li>
          </ul>
          
          <h4>注意事项：</h4>
          <ul>
            <li>配置会保存到服务器，重启应用后仍然有效</li>
            <li>webhook地址和密钥是敏感信息，请妥善保管</li>
            <li>建议使用签名方式，安全性更高</li>
          </ul>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Message } from '@element-plus/icons-vue'
import { getDingTalkConfig, testDingTalkNotification, saveDingTalkConfig } from '../api/notifications'

// 响应式数据
const config = ref({
  enabled: false,
  webhookUrl: '',
  secret: '',
  isConfigured: false
})
const testing = ref(false)
const testResult = ref(null)
const isEditing = ref(false)
const saving = ref(false)
const editForm = ref({
  enabled: false,
  webhookUrl: '',
  secret: ''
})

// 加载配置
const loadConfig = async () => {
  try {
    const response = await getDingTalkConfig()
    config.value = response
  } catch (error) {
    ElMessage.error('加载钉钉配置失败')
    console.error('Load config error:', error)
  }
}

// 测试钉钉
const testDingTalk = async () => {
  try {
    testing.value = true
    const response = await testDingTalkNotification()
    testResult.value = {
      success: true,
      message: response
    }
    ElMessage.success('钉钉测试成功')
  } catch (error) {
    console.error('钉钉测试错误:', error)
    const errorMessage = error.response?.data || error.message || '未知错误'
    testResult.value = {
      success: false,
      message: `测试失败: ${errorMessage}`
    }
    ElMessage.error('钉钉测试失败，请检查配置')
  } finally {
    testing.value = false
  }
}

// 切换编辑模式
const toggleEdit = () => {
  if (!isEditing.value) {
    // 进入编辑模式，复制当前配置到编辑表单
    editForm.value = {
      enabled: config.value.enabled,
      webhookUrl: config.value.webhookUrl || '',
      secret: config.value.secret || ''
    }
  }
  isEditing.value = !isEditing.value
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  testResult.value = null
}

// 保存配置
const saveConfig = async () => {
  try {
    saving.value = true
    
    // 验证webhook地址格式
    if (editForm.value.enabled && !editForm.value.webhookUrl.includes('oapi.dingtalk.com/robot/send')) {
      ElMessage.error('webhook地址格式不正确，请使用钉钉机器人webhook地址')
      return
    }
    
    await saveDingTalkConfig(editForm.value)
    ElMessage.success('配置保存成功')
    
    // 重新加载配置
    await loadConfig()
    
    // 退出编辑模式
    isEditing.value = false
    testResult.value = null
  } catch (error) {
    console.error('保存配置错误:', error)
    const errorMessage = error.response?.data || error.message || '未知错误'
    ElMessage.error(`保存配置失败: ${errorMessage}`)
  } finally {
    saving.value = false
  }
}

// 组件挂载时加载配置
onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.dingtalk-test-container {
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
.test-section,
.help-section {
  margin-bottom: 20px;
}

.test-result {
  margin-top: 20px;
}

.help-content {
  line-height: 1.6;
}

.help-content h4 {
  margin: 16px 0 8px 0;
  color: #303133;
}

.help-content ol,
.help-content ul {
  margin: 8px 0;
  padding-left: 20px;
}

.help-content li {
  margin: 4px 0;
}

.el-code-block {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  margin: 12px 0;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.4;
  overflow-x: auto;
}
</style>
