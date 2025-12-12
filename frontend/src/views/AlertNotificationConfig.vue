<template>
  <div class="alert-notification-config-container">
    <div class="page-header">
      <h1>告警通知配置</h1>
      <p class="page-description">配置哪些类型的告警需要发送钉钉/飞书通知</p>
    </div>

    <el-card>
      <template #header>
        <span>通知平台配置</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="saveAllConfigs" :loading="saving">
          保存所有配置
        </el-button>
      </template>

      <el-table :data="configs" v-loading="loading" stripe>
        <el-table-column prop="alertName" label="告警类型" width="200">
          <template #default="{ row }">
            <span>{{ row.alertName }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="alertType" label="类型代码" width="180">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.alertType }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="dingtalkEnabled" label="钉钉通知" width="150">
          <template #default="{ row }">
            <el-switch
              v-model="row.dingtalkEnabled"
              @change="handleConfigChange(row)"
              active-text="启用"
              inactive-text="禁用"
            />
          </template>
        </el-table-column>

        <el-table-column prop="feishuEnabled" label="飞书通知" width="150">
          <template #default="{ row }">
            <el-switch
              v-model="row.feishuEnabled"
              @change="handleConfigChange(row)"
              active-text="启用"
              inactive-text="禁用"
            />
          </template>
        </el-table-column>

        <el-table-column prop="description" label="说明" min-width="300">
          <template #default="{ row }">
            <span class="description-text">{{ getDescription(row.alertType) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="help-card">
      <template #header>
        <span>配置说明</span>
      </template>
      
      <div class="help-content">
        <h4>告警类型说明：</h4>
        <ul>
          <li><strong>任务跟踪表填写提醒</strong>：每周一检查上周五任务跟踪表填写情况，提醒未填写的人员</li>
          <li><strong>任务分配通知</strong>：当任务被分配给用户时发送的通知</li>
          <li><strong>任务超时提醒</strong>：当任务超过预计完成时间时发送的提醒</li>
          <li><strong>任务完成通知</strong>：当任务完成时发送的通知</li>
          <li><strong>系统维护通知</strong>：系统维护相关的通知</li>
        </ul>
        
        <h4>配置说明：</h4>
        <ul>
          <li>只有启用的告警类型才会发送对应平台的通知</li>
          <li>可以同时启用钉钉和飞书通知，系统会向两个平台都发送消息</li>
          <li>系统内部通知不受此配置影响，仍会正常发送</li>
          <li>配置会立即生效，无需重启系统</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAlertNotificationConfigs, updateAlertNotificationConfigs } from '@/api/alertNotificationConfig'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const configs = ref([])

// 加载配置
const loadConfigs = async () => {
  try {
    loading.value = true
    const response = await getAlertNotificationConfigs()
    configs.value = response
  } catch (error) {
    ElMessage.error('加载告警通知配置失败')
    console.error('Load configs error:', error)
  } finally {
    loading.value = false
  }
}

// 处理配置变更
const handleConfigChange = (config) => {
  console.log('配置变更:', config.alertType, config.dingtalkEnabled)
}

// 保存所有配置
const saveAllConfigs = async () => {
  try {
    saving.value = true
    await updateAlertNotificationConfigs(configs.value)
    ElMessage.success('配置保存成功')
  } catch (error) {
    ElMessage.error('配置保存失败')
    console.error('Save configs error:', error)
  } finally {
    saving.value = false
  }
}

// 获取描述信息
const getDescription = (alertType) => {
  const descriptions = {
    'TASK_TRACKING_REMINDER': '每周一检查上周五任务跟踪表填写情况，提醒未填写的人员',
    'TASK_ASSIGNMENT': '当任务被分配给用户时发送的通知',
    'TASK_OVERDUE': '当任务超过预计完成时间时发送的提醒',
    'TASK_COMPLETION': '当任务完成时发送的通知',
    'SYSTEM_MAINTENANCE': '系统维护相关的通知'
  }
  return descriptions[alertType] || '未知告警类型'
}

// 组件挂载时加载配置
onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.alert-notification-config-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  padding: 20px;
  background: var(--theme-backgroundCard, #ffffff);
  border-radius: 8px;
  box-shadow: var(--theme-cardShadow, 0 2px 8px rgba(0,0,0,0.08));
}

.page-header h1 {
  margin: 0 0 8px 0;
  color: var(--theme-text, #303133);
  font-size: 24px;
  font-weight: 600;
}

.page-description {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.help-card {
  margin-top: 20px;
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

.description-text {
  color: #606266;
  font-size: 14px;
}
</style>
