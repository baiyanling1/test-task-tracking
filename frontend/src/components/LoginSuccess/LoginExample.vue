<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 现有登录表单代码 -->
      <div class="login-header">
        <h1>测试任务跟踪系统</h1>
        <p>请登录您的账户</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- ========== 登录成功提示组件 - 请选择其中一个 ========== -->
    
    <!-- 方案一：经典烟花 -->
    <ClassicFireworks 
      v-if="showSuccess"
      :visible="showSuccess"
      :username="loginForm.username || '用户'"
      @close="handleSuccessClose"
    />
    
    <!-- 方案二：粒子爆炸（注释掉方案一后取消注释） -->
    <!--
    <ParticleExplosion 
      v-if="showSuccess"
      :visible="showSuccess"
      :username="loginForm.username || '用户'"
      @close="handleSuccessClose"
    />
    -->
    
    <!-- 方案三：星星闪烁（注释掉方案一后取消注释） -->
    <!--
    <StarSparkle 
      v-if="showSuccess"
      :visible="showSuccess"
      :username="loginForm.username || '用户'"
      @close="handleSuccessClose"
    />
    -->
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

// ========== 导入登录成功提示组件 - 请选择其中一个 ==========
import ClassicFireworks from '@/components/LoginSuccess/ClassicFireworks.vue'
// import ParticleExplosion from '@/components/LoginSuccess/ParticleExplosion.vue'
// import StarSparkle from '@/components/LoginSuccess/StarSparkle.vue'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref()
const loading = ref(false)
const showSuccess = ref(false) // 控制成功提示显示

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    await authStore.login(loginForm)
    
    // 显示成功提示（移除原来的 ElMessage.success）
    showSuccess.value = true
    // 注意：不要立即跳转，等动画结束后再跳转
    
  } catch (error) {
    console.error('登录失败:', error)
    if (error.response?.status === 400) {
      ElMessage.error(error.response.data || '用户名或密码错误')
    } else if (error.response?.status === 403) {
      ElMessage.error('权限不足，请联系管理员')
    } else {
      ElMessage.error('登录失败，请检查网络连接')
    }
  } finally {
    loading.value = false
  }
}

// 成功提示关闭后跳转
const handleSuccessClose = () => {
  showSuccess.value = false
  router.push('/')
}
</script>

<style scoped>
/* 保持原有的登录页面样式 */
.login-container {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-box {
  width: 400px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  color: #333;
  margin: 0 0 10px 0;
  font-size: 24px;
}

.login-header p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
}
</style>