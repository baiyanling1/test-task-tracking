<template>
  <div class="users-container">
    <div class="page-header">
      <h1>用户管理</h1>
      <el-button v-if="canEditUsers" type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新建用户
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchQuery"
            placeholder="搜索用户名或邮箱"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="roleFilter" placeholder="角色筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="项目经理" value="MANAGER" />
            <el-option label="测试人员" value="TESTER" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadUsers">刷新</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 用户列表 -->
    <div class="users-table">
      <el-table
        :data="filteredUsers"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="120">
          <template #default="{ row }">
            {{ row.department || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="position" label="职位" width="120">
          <template #default="{ row }">
            {{ row.position || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'">
              {{ row.isActive ? '活跃' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" v-if="canEditUsers">
          <template #default="{ row }">
            <el-button size="small" @click="editUser(row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="row.isActive ? 'warning' : 'success'"
              @click="toggleUserStatus(row)"
            >
              {{ row.isActive ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="totalUsers"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 创建/编辑用户对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingUser ? '编辑用户' : '新建用户'"
      width="500px"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="项目经理" value="MANAGER" />
            <el-option label="测试人员" value="TESTER" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-select v-model="userForm.department" placeholder="请选择部门" style="width: 100%">
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="userForm.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item v-if="!editingUser" label="密码" prop="password">
          <el-input 
            v-model="userForm.password" 
            type="password" 
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="状态" prop="isActive">
          <el-radio-group v-model="userForm.isActive">
            <el-radio :label="true">活跃</el-radio>
            <el-radio :label="false">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="saveUser" :loading="saving">
            {{ editingUser ? '更新' : '创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getUsers, createUser, updateUser, deleteUser as deleteUserApi, disableUser, enableUser } from '@/api/users'
import { getDepartments } from '@/api/departments'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const users = ref([])
const departments = ref([])
const searchQuery = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const totalUsers = ref(0)

// 对话框状态
const showCreateDialog = ref(false)
const editingUser = ref(null)

// 表单数据
const userFormRef = ref()
const userForm = reactive({
  username: '',
  email: '',
  realName: '',
  role: 'TESTER',
  password: '',
  isActive: true,
  department: '',
  position: ''
})

// 表单验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3-50个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ]
}

// 计算属性
const canEditUsers = computed(() => {
  return authStore.user?.role === 'ADMIN' || authStore.user?.role === 'MANAGER'
})

const filteredUsers = computed(() => {
  let filtered = users.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(user => 
      user.username.toLowerCase().includes(query) ||
      user.realName.toLowerCase().includes(query) ||
      user.email.toLowerCase().includes(query)
    )
  }

  if (roleFilter.value) {
    filtered = filtered.filter(user => user.role === roleFilter.value)
  }

  if (statusFilter.value) {
    filtered = filtered.filter(user => {
      if (statusFilter.value === 'ACTIVE') return user.isActive
      if (statusFilter.value === 'INACTIVE') return !user.isActive
      return true
    })
  }

  return filtered
})

// 方法
const loadUsers = async () => {
  loading.value = true
  try {
    const response = await getUsers({
      page: currentPage.value - 1,
      size: pageSize.value
    })
    users.value = response.content || []
    totalUsers.value = response.totalElements || 0
  } catch (error) {
    ElMessage.error('加载用户列表失败')
    console.error('Load users error:', error)
  } finally {
    loading.value = false
  }
}

const loadDepartments = async () => {
  try {
    const response = await getDepartments()
    departments.value = response || []
  } catch (error) {
    console.error('加载部门列表失败:', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

const editUser = (user) => {
  editingUser.value = user
  Object.assign(userForm, {
    username: user.username,
    email: user.email,
    realName: user.realName,
    role: user.role,
    password: '',
    isActive: user.isActive,
    department: user.department || '',
    position: user.position || ''
  })
  showCreateDialog.value = true
}

const saveUser = async () => {
  if (!userFormRef.value) return
  
  try {
    await userFormRef.value.validate()
    saving.value = true

    // 转换字段名以匹配后端API
    const userData = {
      username: userForm.username,
      email: userForm.email,
      realName: userForm.realName,
      role: userForm.role,
      password: userForm.password,
      isActive: userForm.isActive,
      department: userForm.department,
      position: userForm.position
    }

    if (editingUser.value) {
      await updateUser(editingUser.value.id, userData)
      ElMessage.success('用户更新成功')
    } else {
      await createUser(userData)
      ElMessage.success('用户创建成功')
    }

    showCreateDialog.value = false
    resetForm()
    loadUsers()
  } catch (error) {
    ElMessage.error(editingUser.value ? '更新用户失败' : '创建用户失败')
    console.error('Save user error:', error)
  } finally {
    saving.value = false
  }
}

const toggleUserStatus = async (user) => {
  try {
    const newStatus = user.isActive
    const action = newStatus ? '禁用' : '启用'
    
    await ElMessageBox.confirm(
      `确定要${action}用户 "${user.username}" 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    if (newStatus) {
      await disableUser(user.id)
    } else {
      await enableUser(user.id)
    }
    
    ElMessage.success(`用户${action}成功`)
    loadUsers() // Reload users after status change
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
      console.error('Toggle user status error:', error)
    }
  }
}

const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteUserApi(user.id)
    ElMessage.success('用户删除成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除用户失败')
      console.error('Delete user error:', error)
    }
  }
}

const resetForm = () => {
  editingUser.value = null
  Object.assign(userForm, {
    username: '',
    email: '',
    realName: '',
    role: 'TESTER',
    password: '',
    isActive: true,
    department: '',
    position: ''
  })
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
}

const getRoleType = (role) => {
  const types = {
    'ADMIN': 'danger',
    'MANAGER': 'warning',
    'TESTER': 'info'
  }
  return types[role] || 'info'
}

const getRoleText = (role) => {
  const texts = {
    'ADMIN': '管理员',
    'MANAGER': '项目经理',
    'TESTER': '测试人员'
  }
  return texts[role] || role
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadDepartments()
  loadUsers()
})
</script>

<style scoped>
.users-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
}

.search-section {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.users-table {
  margin-bottom: 20px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 