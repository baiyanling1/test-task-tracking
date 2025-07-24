<template>
  <div class="departments-container">
    <div class="page-header">
      <h1>部门管理</h1>
      <el-button v-if="canEditDepartments" type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新建部门
      </el-button>
    </div>

    <!-- 部门列表 -->
    <div class="departments-table">
      <el-table
        :data="departments"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="name" label="部门名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="isActive" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'">
              {{ row.isActive ? '活跃' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" v-if="canEditDepartments">
          <template #default="{ row }">
            <el-button size="small" @click="editDepartment(row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="row.isActive ? 'warning' : 'success'"
              @click="toggleDepartmentStatus(row)"
            >
              {{ row.isActive ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteDepartment(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 创建/编辑部门对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingDepartment ? '编辑部门' : '新建部门'"
      width="500px"
    >
      <el-form
        ref="departmentFormRef"
        :model="departmentForm"
        :rules="departmentRules"
        label-width="100px"
      >
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="departmentForm.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="departmentForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入部门描述"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number
            v-model="departmentForm.sortOrder"
            :min="0"
            :max="999"
            placeholder="请输入排序值"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="isActive">
          <el-radio-group v-model="departmentForm.isActive">
            <el-radio :label="true">活跃</el-radio>
            <el-radio :label="false">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="saveDepartment" :loading="saving">
            {{ editingDepartment ? '更新' : '创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getAllDepartments, createDepartment, updateDepartment, deleteDepartment as deleteDepartmentApi } from '@/api/departments'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const departments = ref([])

// 对话框状态
const showCreateDialog = ref(false)
const editingDepartment = ref(null)

// 表单数据
const departmentFormRef = ref()
const departmentForm = reactive({
  name: '',
  description: '',
  sortOrder: 0,
  isActive: true
})

// 表单验证规则
const departmentRules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 100, message: '部门名称长度必须在2-100个字符之间', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序值', trigger: 'blur' },
    { type: 'number', min: 0, max: 999, message: '排序值必须在0-999之间', trigger: 'blur' }
  ]
}

// 计算属性
const canEditDepartments = computed(() => {
  return authStore.user?.role === 'ADMIN' || authStore.user?.role === 'MANAGER'
})

// 方法
const loadDepartments = async () => {
  loading.value = true
  try {
    const response = await getAllDepartments()
    departments.value = response || []
  } catch (error) {
    ElMessage.error('加载部门列表失败')
    console.error('Load departments error:', error)
  } finally {
    loading.value = false
  }
}

const editDepartment = (department) => {
  editingDepartment.value = department
  Object.assign(departmentForm, {
    name: department.name,
    description: department.description || '',
    sortOrder: department.sortOrder || 0,
    isActive: department.isActive
  })
  showCreateDialog.value = true
}

const saveDepartment = async () => {
  if (!departmentFormRef.value) return
  
  try {
    await departmentFormRef.value.validate()
    saving.value = true

    const departmentData = {
      name: departmentForm.name,
      description: departmentForm.description,
      sortOrder: departmentForm.sortOrder,
      isActive: departmentForm.isActive
    }

    if (editingDepartment.value) {
      await updateDepartment(editingDepartment.value.id, departmentData)
      ElMessage.success('部门更新成功')
    } else {
      await createDepartment(departmentData)
      ElMessage.success('部门创建成功')
    }

    showCreateDialog.value = false
    resetForm()
    loadDepartments()
  } catch (error) {
    ElMessage.error(editingDepartment.value ? '更新部门失败' : '创建部门失败')
    console.error('Save department error:', error)
  } finally {
    saving.value = false
  }
}

const toggleDepartmentStatus = async (department) => {
  try {
    const newStatus = department.isActive
    const action = newStatus ? '禁用' : '启用'
    
    await ElMessageBox.confirm(
      `确定要${action}部门 "${department.name}" 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const departmentData = {
      name: department.name,
      description: department.description || '',
      sortOrder: department.sortOrder || 0,
      isActive: !newStatus
    }

    await updateDepartment(department.id, departmentData)
    ElMessage.success(`部门${action}成功`)
    loadDepartments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
      console.error('Toggle department status error:', error)
    }
  }
}

const deleteDepartment = async (department) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除部门 "${department.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteDepartmentApi(department.id)
    ElMessage.success('部门删除成功')
    loadDepartments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除部门失败')
      console.error('Delete department error:', error)
    }
  }
}

const resetForm = () => {
  editingDepartment.value = null
  Object.assign(departmentForm, {
    name: '',
    description: '',
    sortOrder: 0,
    isActive: true
  })
  if (departmentFormRef.value) {
    departmentFormRef.value.resetFields()
  }
}

const formatDateTime = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadDepartments()
})
</script>

<style scoped>
.departments-container {
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

.departments-table {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 