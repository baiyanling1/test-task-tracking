<template>
  <div class="webhook-config-container">
    <div class="page-header">
      <h1>Webhook通知配置</h1>
      <p class="page-description">配置钉钉和飞书机器人通知</p>
    </div>

    <!-- 钉钉配置 -->
    <div class="config-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>
              <el-icon style="margin-right: 8px;"><Message /></el-icon>
              钉钉配置
            </span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="toggleEdit('dingtalk')">
              {{ isEditingDingTalk ? '取消编辑' : '编辑配置' }}
            </el-button>
          </div>
        </template>
        
        <div v-if="!isEditingDingTalk">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="启用状态">
              <el-tag :type="dingtalkConfig.enabled ? 'success' : 'info'">
                {{ dingtalkConfig.enabled ? '已启用' : '已禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Webhook地址">
              <div>
                {{ dingtalkConfig.webhookUrl || '未配置' }}
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="密钥">
              {{ dingtalkConfig.secret && dingtalkConfig.secret.trim() ? '******' : '未配置' }}
            </el-descriptions-item>
          </el-descriptions>
          
          <div style="margin-top: 16px;">
            <el-button type="primary" @click="testDingTalk" :loading="testingDingTalk" size="small">
              <el-icon><Message /></el-icon>
              发送测试消息
            </el-button>
          </div>
          
          <div v-if="dingtalkTestResult" class="test-result">
            <el-alert
              :title="dingtalkTestResult.success ? '测试成功' : '测试失败'"
              :type="dingtalkTestResult.success ? 'success' : 'error'"
              :description="dingtalkTestResult.message"
              show-icon
              closable
            />
          </div>
        </div>
        
        <div v-else>
          <el-form :model="dingtalkEditForm" label-width="120px">
            <el-form-item label="启用钉钉通知">
              <el-switch v-model="dingtalkEditForm.enabled" />
            </el-form-item>
            
            <el-form-item label="Webhook地址">
              <el-input 
                v-model="dingtalkEditForm.webhookUrl" 
                placeholder="请输入钉钉webhook地址"
                :disabled="!dingtalkEditForm.enabled"
              />
            </el-form-item>
            
            <el-form-item label="签名密钥">
              <el-input 
                v-model="dingtalkEditForm.secret" 
                placeholder="可选，如果使用签名方式请填写"
                :disabled="!dingtalkEditForm.enabled"
                show-password
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveDingTalkConfig" :loading="savingDingTalk">保存配置</el-button>
              <el-button @click="cancelEdit('dingtalk')">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>

    <!-- 飞书配置 -->
    <div class="config-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>
              <el-icon style="margin-right: 8px;"><ChatDotRound /></el-icon>
              飞书配置
            </span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="toggleEdit('feishu')">
              {{ isEditingFeiShu ? '取消编辑' : '编辑配置' }}
            </el-button>
          </div>
        </template>
        
        <div v-if="!isEditingFeiShu">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="启用状态">
              <el-tag :type="feishuConfig.enabled ? 'success' : 'info'">
                {{ feishuConfig.enabled ? '已启用' : '已禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Webhook地址">
              <div>
                {{ feishuConfig.webhookUrl || '未配置' }}
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="密钥">
              {{ feishuConfig.secret && feishuConfig.secret.trim() ? '******' : '未配置' }}
            </el-descriptions-item>
          </el-descriptions>
          
          <div style="margin-top: 16px;">
            <el-button type="primary" @click="testFeiShu" :loading="testingFeiShu" size="small">
              <el-icon><ChatDotRound /></el-icon>
              发送测试消息
            </el-button>
          </div>
          
          <div v-if="feishuTestResult" class="test-result">
            <el-alert
              :title="feishuTestResult.success ? '测试成功' : '测试失败'"
              :type="feishuTestResult.success ? 'success' : 'error'"
              :description="feishuTestResult.message"
              show-icon
              closable
            />
          </div>
        </div>
        
        <div v-else>
          <el-form :model="feishuEditForm" label-width="120px">
            <el-form-item label="启用飞书通知">
              <el-switch v-model="feishuEditForm.enabled" />
            </el-form-item>
            
            <el-form-item label="Webhook地址">
              <el-input 
                v-model="feishuEditForm.webhookUrl" 
                placeholder="请输入飞书webhook地址"
                :disabled="!feishuEditForm.enabled"
              />
            </el-form-item>
            
            <el-form-item label="签名密钥">
              <el-input 
                v-model="feishuEditForm.secret" 
                placeholder="可选，如果使用签名方式请填写"
                :disabled="!feishuEditForm.enabled"
                show-password
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveFeiShuConfig" :loading="savingFeiShu">保存配置</el-button>
              <el-button @click="cancelEdit('feishu')">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>

    <!-- 配置说明 -->
    <div class="help-section">
      <el-card>
        <template #header>
          <span>配置说明</span>
        </template>
        
        <el-tabs type="border-card">
          <el-tab-pane label="钉钉配置">
            <div class="help-content">
              <h4>配置步骤：</h4>
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
                <li><strong>Webhook地址</strong>：钉钉webhook地址（支持机器人webhook、工作流webhook等）</li>
                <li><strong>签名密钥</strong>：如果需要签名验证，请填写签名密钥（可选）</li>
              </ul>
              
              <h4>注意事项：</h4>
              <ul>
                <li>webhook地址和密钥是敏感信息，请妥善保管</li>
                <li>建议使用签名方式，安全性更高</li>
                <li>配置会保存到服务器，重启应用后仍然有效</li>
              </ul>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="飞书配置">
            <div class="help-content">
              <h4>配置步骤：</h4>
              <ol>
                <li>在飞书群中添加自定义机器人</li>
                <li>获取webhook地址</li>
                <li>配置安全设置（签名验证）</li>
                <li>点击"编辑配置"按钮，填写配置信息</li>
                <li>点击"保存配置"保存设置</li>
              </ol>
              
              <h4>配置说明：</h4>
              <ul>
                <li><strong>启用飞书通知</strong>：开启或关闭飞书通知功能</li>
                <li><strong>Webhook地址</strong>：飞书webhook地址（支持机器人webhook、工作流webhook等）</li>
                <li><strong>签名密钥</strong>：如果需要签名验证，请填写签名密钥（可选）</li>
              </ul>
              
              <h4>注意事项：</h4>
              <ul>
                <li>webhook地址和密钥是敏感信息，请妥善保管</li>
                <li>建议使用签名方式，安全性更高</li>
                <li>飞书通知支持富文本格式，显示效果更加美观</li>
                <li>配置会保存到服务器，重启应用后仍然有效</li>
              </ul>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="使用说明">
            <div class="help-content">
              <h4>通知平台选择：</h4>
              <ul>
                <li>可以同时启用钉钉和飞书通知</li>
                <li>在"告警通知配置"页面可以为每种告警类型选择发送到哪个平台</li>
                <li>系统会根据配置自动发送通知到相应平台</li>
              </ul>
              
              <h4>测试说明：</h4>
              <ul>
                <li>配置完成后，点击"发送测试消息"按钮测试配置</li>
                <li>如果配置正确，对应的群会收到测试消息</li>
                <li>如果失败，请检查webhook地址和密钥配置</li>
              </ul>
              
              <h4>配置优先级：</h4>
              <ul>
                <li>系统会优先使用数据库中保存的配置</li>
                <li>如果数据库中没有配置，会使用配置文件中的默认值</li>
                <li>配置修改后立即生效，无需重启系统</li>
              </ul>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Message, ChatDotRound } from '@element-plus/icons-vue'
import { 
  getDingTalkConfig, 
  testDingTalkNotification, 
  saveDingTalkConfig as saveDingTalkConfigApi,
  getFeiShuConfig,
  testFeiShuNotification,
  saveFeiShuConfig as saveFeiShuConfigApi
} from '../api/notifications'

// 钉钉配置相关
const dingtalkConfig = ref({
  enabled: false,
  webhookUrl: '',
  secret: '',
  isConfigured: false
})
const isEditingDingTalk = ref(false)
const savingDingTalk = ref(false)
const testingDingTalk = ref(false)
const dingtalkTestResult = ref(null)
const dingtalkEditForm = ref({
  enabled: false,
  webhookUrl: '',
  secret: ''
})

// 飞书配置相关
const feishuConfig = ref({
  enabled: false,
  webhookUrl: '',
  secret: '',
  isConfigured: false
})
const isEditingFeiShu = ref(false)
const savingFeiShu = ref(false)
const testingFeiShu = ref(false)
const feishuTestResult = ref(null)
const feishuEditForm = ref({
  enabled: false,
  webhookUrl: '',
  secret: ''
})

// 加载钉钉配置
const loadDingTalkConfig = async () => {
  try {
    const response = await getDingTalkConfig()
    dingtalkConfig.value = response
  } catch (error) {
    ElMessage.error('加载钉钉配置失败')
    console.error('Load dingtalk config error:', error)
  }
}

// 加载飞书配置
const loadFeiShuConfig = async () => {
  try {
    const response = await getFeiShuConfig()
    feishuConfig.value = response
  } catch (error) {
    ElMessage.error('加载飞书配置失败')
    console.error('Load feishu config error:', error)
  }
}

// 切换编辑模式
const toggleEdit = (platform) => {
  if (platform === 'dingtalk') {
    if (!isEditingDingTalk.value) {
      dingtalkEditForm.value = {
        enabled: dingtalkConfig.value.enabled,
        webhookUrl: dingtalkConfig.value.webhookUrl || '',
        secret: dingtalkConfig.value.secret || ''
      }
    }
    isEditingDingTalk.value = !isEditingDingTalk.value
    dingtalkTestResult.value = null
  } else if (platform === 'feishu') {
    if (!isEditingFeiShu.value) {
      feishuEditForm.value = {
        enabled: feishuConfig.value.enabled,
        webhookUrl: feishuConfig.value.webhookUrl || '',
        secret: feishuConfig.value.secret || ''
      }
    }
    isEditingFeiShu.value = !isEditingFeiShu.value
    feishuTestResult.value = null
  }
}

// 取消编辑
const cancelEdit = (platform) => {
  if (platform === 'dingtalk') {
    isEditingDingTalk.value = false
    dingtalkTestResult.value = null
  } else if (platform === 'feishu') {
    isEditingFeiShu.value = false
    feishuTestResult.value = null
  }
}

// 保存钉钉配置
const saveDingTalkConfig = async () => {
  try {
    savingDingTalk.value = true
    
    if (dingtalkEditForm.value.enabled && !dingtalkEditForm.value.webhookUrl.trim()) {
      ElMessage.error('请填写webhook地址')
      return
    }
    
    await saveDingTalkConfigApi(dingtalkEditForm.value)
    ElMessage.success('钉钉配置保存成功')
    
    await loadDingTalkConfig()
    isEditingDingTalk.value = false
    dingtalkTestResult.value = null
  } catch (error) {
    console.error('保存钉钉配置错误:', error)
    const errorMessage = error.response?.data || error.message || '未知错误'
    ElMessage.error(`保存配置失败: ${errorMessage}`)
  } finally {
    savingDingTalk.value = false
  }
}

// 保存飞书配置
const saveFeiShuConfig = async () => {
  try {
    savingFeiShu.value = true
    
    if (feishuEditForm.value.enabled && !feishuEditForm.value.webhookUrl.trim()) {
      ElMessage.error('请填写webhook地址')
      return
    }
    
    await saveFeiShuConfigApi(feishuEditForm.value)
    ElMessage.success('飞书配置保存成功')
    
    await loadFeiShuConfig()
    isEditingFeiShu.value = false
    feishuTestResult.value = null
  } catch (error) {
    console.error('保存飞书配置错误:', error)
    const errorMessage = error.response?.data || error.message || '未知错误'
    ElMessage.error(`保存配置失败: ${errorMessage}`)
  } finally {
    savingFeiShu.value = false
  }
}

// 测试钉钉
const testDingTalk = async () => {
  try {
    testingDingTalk.value = true
    const response = await testDingTalkNotification()
    dingtalkTestResult.value = {
      success: true,
      message: response
    }
    ElMessage.success('钉钉测试成功')
  } catch (error) {
    console.error('钉钉测试错误:', error)
    const errorMessage = error.response?.data || error.message || '未知错误'
    dingtalkTestResult.value = {
      success: false,
      message: `测试失败: ${errorMessage}`
    }
    ElMessage.error('钉钉测试失败，请检查配置')
  } finally {
    testingDingTalk.value = false
  }
}

// 测试飞书
const testFeiShu = async () => {
  try {
    testingFeiShu.value = true
    const response = await testFeiShuNotification()
    feishuTestResult.value = {
      success: true,
      message: response
    }
    ElMessage.success('飞书测试成功')
  } catch (error) {
    console.error('飞书测试错误:', error)
    const errorMessage = error.response?.data || error.message || '未知错误'
    feishuTestResult.value = {
      success: false,
      message: `测试失败: ${errorMessage}`
    }
    ElMessage.error('飞书测试失败，请检查配置')
  } finally {
    testingFeiShu.value = false
  }
}

// 组件挂载时加载配置
onMounted(() => {
  loadDingTalkConfig()
  loadFeiShuConfig()
})
</script>

<style scoped>
.webhook-config-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-description {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.config-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.test-result {
  margin-top: 16px;
}

.help-section {
  margin-top: 20px;
}

.help-content {
  line-height: 1.6;
  padding: 16px;
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

.help-content code {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 3px;
  padding: 2px 6px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: #e96900;
}
</style>

