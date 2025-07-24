<template>
  <div class="profile-container">
    <div class="page-header">
      <h1>个人资料</h1>
    </div>

    <el-row :gutter="20">
      <!-- 编辑表单 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>编辑个人信息</span>
            </div>
          </template>

          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
          >
            <el-form-item label="头像">
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
              >
                <img v-if="profileForm.avatar" :src="profileForm.avatar" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>

            <el-form-item label="姓名" prop="fullName">
              <el-input v-model="profileForm.fullName" placeholder="请输入姓名" />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>

            <el-form-item label="部门" prop="department">
              <el-select v-model="profileForm.department" placeholder="请选择部门" style="width: 100%">
                <el-option
                  v-for="dept in departments"
                  :key="dept.id"
                  :label="dept.name"
                  :value="dept.name"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="职位" prop="position">
              <el-input v-model="profileForm.position" placeholder="请输入职位" />
            </el-form-item>

            <el-form-item label="个人简介" prop="bio">
              <el-input
                v-model="profileForm.bio"
                type="textarea"
                :rows="4"
                placeholder="请输入个人简介"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving">
                保存修改
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 修改密码卡片 -->
        <el-card style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>修改密码</span>
            </div>
          </template>

          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
          >
            <el-form-item label="当前密码" prop="currentPassword">
              <el-input
                v-model="passwordForm.currentPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button type="warning" @click="changePassword" :loading="changingPassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 登录历史卡片 -->
        <el-card style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>登录历史</span>
            </div>
          </template>

          <el-table :data="loginHistory" stripe style="width: 100%">
            <el-table-column prop="loginTime" label="登录时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.loginTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="ipAddress" label="IP地址" width="150" />
            <el-table-column prop="userAgent" label="设备信息" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
                  {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getProfile, changePassword as changePasswordApi } from '@/api/auth'

const authStore = useAuthStore()

// 响应式数据
const saving = ref(false)
const changingPassword = ref(false)

// 用户信息
const userInfo = ref({
  id: 1,
  username: 'admin',
  email: 'admin@example.com',
  fullName: '系统管理员',
  role: 'ADMIN',
  status: 'ACTIVE',
  avatar: '',
  phone: '13800138000',
  department: '技术部',
  position: '系统管理员',
  bio: '负责系统管理和维护工作',
  lastLoginTime: new Date(),
  createdTime: new Date('2024-01-01')
})

// 登录历史
const loginHistory = ref([
  {
    id: 1,
    loginTime: new Date(),
    ipAddress: '192.168.1.100',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    status: 'SUCCESS'
  },
  {
    id: 2,
    loginTime: new Date(Date.now() - 86400000),
    ipAddress: '192.168.1.100',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    status: 'SUCCESS'
  },
  {
    id: 3,
    loginTime: new Date(Date.now() - 172800000),
    ipAddress: '192.168.1.101',
    userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36',
    status: 'SUCCESS'
  }
])

// 表单数据
const profileFormRef = ref()
const profileForm = reactive({
  fullName: '',
  email: '',
  phone: '',
  department: '',
  position: '',
  bio: '',
  avatar: ''
})

// 密码表单
const passwordFormRef = ref()
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const profileRules = {
  fullName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 方法
const loadUserInfo = async () => {
  try {
    const userData = await getProfile()
    Object.assign(profileForm, {
      fullName: userData.realName,
      email: userData.email,
      phone: userData.phoneNumber,
      department: userData.department,
      position: userData.position,
      bio: userData.bio || '',
      avatar: userData.avatar || ''
    })
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const saveProfile = async () => {
  if (!profileFormRef.value) return
  
  try {
    await profileFormRef.value.validate()
    saving.value = true

    // 模拟保存
    Object.assign(userInfo.value, profileForm)
    ElMessage.success('个人信息更新成功')
  } catch (error) {
    ElMessage.error('更新失败')
    console.error('Save profile error:', error)
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true

    await changePasswordApi({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    
    ElMessage.success('密码修改成功')
    
    // 清空密码表单
    Object.assign(passwordForm, {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    passwordFormRef.value.resetFields()
  } catch (error) {
    ElMessage.error('密码修改失败')
    console.error('Change password error:', error)
  } finally {
    changingPassword.value = false
  }
}

const resetForm = () => {
  loadUserInfo()
  if (profileFormRef.value) {
    profileFormRef.value.resetFields()
  }
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

const handleAvatarUpload = (options) => {
  // 模拟文件上传
  const reader = new FileReader()
  reader.onload = (e) => {
    profileForm.avatar = e.target.result
    userInfo.value.avatar = e.target.result
    ElMessage.success('头像上传成功')
  }
  reader.readAsDataURL(options.file)
}

const getRoleType = (role) => {
  const types = {
    'ADMIN': 'danger',
    'TESTER': 'primary',
    'OBSERVER': 'info'
  }
  return types[role] || 'info'
}

const getRoleText = (role) => {
  const texts = {
    'ADMIN': '管理员',
    'TESTER': '测试员',
    'OBSERVER': '观察者'
  }
  return texts[role] || role
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
}

.profile-card {
  text-align: center;
}

.avatar-section {
  padding: 20px 0;
}

.avatar-section h3 {
  margin: 10px 0 5px 0;
  color: #303133;
}

.avatar-section p {
  margin: 0;
  color: #909399;
}

.info-list {
  text-align: left;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  color: #606266;
  font-weight: 500;
}

.info-item .value {
  color: #303133;
}

.card-header {
  font-weight: bold;
  color: #303133;
}

.avatar-uploader {
  text-align: center;
}

.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}
</style> 