import request from './request'

// 获取所有活跃部门
export const getDepartments = () => {
  return request({
    url: '/departments',
    method: 'get'
  })
}

// 获取所有部门（包括非活跃）
export const getAllDepartments = () => {
  return request({
    url: '/departments/all',
    method: 'get'
  })
}

// 根据ID获取部门
export const getDepartmentById = (id) => {
  return request({
    url: `/departments/${id}`,
    method: 'get'
  })
}

// 创建部门
export const createDepartment = (data) => {
  return request({
    url: '/departments',
    method: 'post',
    data
  })
}

// 更新部门
export const updateDepartment = (id, data) => {
  return request({
    url: `/departments/${id}`,
    method: 'put',
    data
  })
}

// 删除部门
export const deleteDepartment = (id) => {
  return request({
    url: `/departments/${id}`,
    method: 'delete'
  })
} 