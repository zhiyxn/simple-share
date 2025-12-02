<template>
  <div class="role-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <el-icon><Key /></el-icon>
          角色权限管理
        </h1>
        <p class="page-description">管理系统角色及菜单/按钮级别的权限分配</p>
      </div>
      <div class="header-right">
        <el-button
          v-if="hasPermission('system:role:add')"
          type="primary"
          @click="showCreateDialog"
        >
          <el-icon><Plus /></el-icon>
          添加角色
        </el-button>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-toolbar">
      <div class="filter-left">
        <el-input
          v-model="searchQuery"
          placeholder="搜索角色名称或标识"
          style="width: 300px"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select
          v-model="filterStatus"
          placeholder="筛选状态"
          style="width: 150px"
          clearable
          @change="handleFilter"
        >
          <el-option label="启用" value="0" />
          <el-option label="禁用" value="1" />
        </el-select>
      </div>

      <div class="filter-right">
        <el-button @click="resetFilters">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </div>

    <!-- 批量操作工具栏 -->
    <div v-if="selectedRoles.length > 0" class="batch-toolbar">
      <div class="batch-info">
        已选择 {{ selectedRoles.length }} 个角色
      </div>
      <div class="batch-actions">
          <el-button
            v-if="hasPermission('system:role:remove')"
            type="danger"
            :disabled="!canBatchDeleteRole || selectedRoles.some(isSuperAdmin)"
            @click="batchDeleteRoles"
          >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 角色表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="filteredRoles"
        @selection-change="handleSelectionChange"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />

        <el-table-column prop="name" label="角色名称" min-width="120" />

        <el-table-column prop="key" label="角色标识" min-width="120">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.key }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="sort" label="排序" width="80" />

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="0"
              inactive-value="1"
              :disabled="isSuperAdmin(row) || !canEditRole"
              @change="value => updateRoleStatus(row, value)"
            />
          </template>
        </el-table-column>

        <el-table-column prop="remark" label="备注" min-width="150">
          <template #default="{ row }">
            <span class="text-gray-600">{{ row.remark || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
              <el-button
                v-if="hasPermission('system:role:edit')"
                type="primary"
                size="small"
                :disabled="!canEditRole || isSuperAdmin(row)"
                @click="editRole(row)"
              >
              编辑
            </el-button>
              <el-button
                v-if="hasPermission('system:role:edit')"
                type="warning"
                size="small"
                :disabled="!canAssignPermissions || isSuperAdmin(row)"
                @click="assignPermissions(row)"
              >
              分配权限
            </el-button>
              <el-button
                v-if="hasPermission('system:role:remove')"
                type="danger"
                size="small"
                :disabled="!canDeleteRole || row.key === 'admin'"
                @click="deleteRole(row)"
              >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 创建/编辑角色对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '创建角色'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="roleForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色名称" prop="name">
              <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色标识" prop="key">
              <el-input
                v-model="roleForm.key"
                placeholder="建议使用英文标识，需唯一"
                :disabled="isEdit && roleForm.key === 'admin'"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number
                v-model="roleForm.sort"
                :min="0"
                :max="999"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select
                v-model="roleForm.status"
                placeholder="请选择状态"
                style="width: 100%"
              >
                <el-option label="启用" value="0" />
                <el-option label="禁用" value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input
            v-model="roleForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole" :loading="saving">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 权限分配对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="分配权限"
      width="800px"
    >
      <div class="permission-assignment">
        <div class="permission-header">
          <h3>为角色 “{{ currentRole?.name }}” 分配菜单/按钮权限</h3>
          <p class="text-gray-600">选择该角色可以访问的功能模块</p>
        </div>

        <div class="permission-tree">
          <el-tree
            ref="permissionTreeRef"
            :data="permissionTree"
            :props="treeProps"
            show-checkbox
            node-key="id"
            :default-checked-keys="checkedPermissions"
          />
        </div>
      </div>

      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermissions" :loading="savingPermissions">
          保存权限
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Key,
  Plus,
  Search,
  Refresh,
  Delete
} from '@element-plus/icons-vue'
import { roleApi } from '@/api/role'
import { menuApi } from '@/api/menu'
import type { Role } from '@/types/role'
import { usePermission } from '@/composables/usePermission'

// 权限计算
const { hasPermission } = usePermission()
const canCreateRole = computed(() => hasPermission('system:role:add'))
const canEditRole = computed(() => hasPermission('system:role:edit'))
const canDeleteRole = computed(() => hasPermission('system:role:remove'))
const canAssignPermissions = computed(() => hasPermission('system:role:edit'))
const canBatchDeleteRole = computed(() => hasPermission('system:role:remove'))

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const savingPermissions = ref(false)
const roles = ref<Role[]>([])
const selectedRoles = ref<Role[]>([])
const searchQuery = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 对话框相关
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const isEdit = ref(false)
const currentRole = ref<Role | null>(null)
const formRef = ref()
const permissionTreeRef = ref()

const roleForm = reactive({
  id: '',
  name: '',
  key: '',
  sort: 0,
  status: '0',
  remark: ''
})

// 权限树相关
const permissionTree = ref<any[]>([])
const checkedPermissions = ref<number[]>([])
const treeProps = {
  children: 'children',
  label: 'label'
}

const formRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度需在 2-20 个字符之间', trigger: 'blur' }
  ],
  key: [
    { required: true, message: '请输入角色标识', trigger: 'blur' },
    { min: 2, max: 20, message: '角色标识长度需在 2-20 个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '角色标识须以字母开头，仅包含字母/数字/下划线', trigger: 'blur' }
  ],
  sort: [{ required: true, message: '请输入排序值', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const filteredRoles = computed(() => {
  let result = roles.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(role =>
      role.name.toLowerCase().includes(query) ||
      role.key.toLowerCase().includes(query)
    )
  }

  if (filterStatus.value) {
    result = result.filter(role => role.status === filterStatus.value)
  }

  return result
})

const isSuperAdmin = (role: Role) => role.id === '1' || role.key === 'admin'

const loadRoles = async () => {
  try {
    loading.value = true
    const response = await roleApi.getRoles({
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchQuery.value.trim() || undefined
    })
    roles.value = response.items || []
    total.value = response.total || 0
  } catch (error) {
    console.error(error)
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadRoles()
}

const handleFilter = () => {
  currentPage.value = 1
  loadRoles()
}

const resetFilters = () => {
  searchQuery.value = ''
  filterStatus.value = ''
  currentPage.value = 1
  loadRoles()
}

const handleSelectionChange = (selection: Role[]) => {
  selectedRoles.value = selection
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadRoles()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadRoles()
}

const showCreateDialog = () => {
  if (!canCreateRole.value) {
    ElMessage.warning('暂无新增角色权限')
    return
  }
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const editRole = (role: Role) => {
  if (!canEditRole.value) {
    ElMessage.warning('暂无编辑角色权限')
    return
  }
  isEdit.value = true
  Object.assign(roleForm, role)
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(roleForm, {
    id: '',
    name: '',
    key: '',
    sort: 0,
    status: '0',
    remark: ''
  })
}

const saveRole = async () => {
  if (!formRef.value) {
    return
  }
  try {
    await formRef.value.validate()
    saving.value = true

    const payload = {
      name: roleForm.name,
      key: roleForm.key,
      sort: roleForm.sort,
      status: roleForm.status,
      remark: roleForm.remark || ''
    }

    if (isEdit.value) {
      if (!canEditRole.value) {
        ElMessage.warning('暂无编辑角色权限')
        return
      }
      await roleApi.updateRole(roleForm.id, payload)
      ElMessage.success('角色更新成功')
    } else {
      if (!canCreateRole.value) {
        ElMessage.warning('暂无新增角色权限')
        return
      }
      await roleApi.createRole(payload)
      ElMessage.success('角色创建成功')
    }

    dialogVisible.value = false
    await loadRoles()
  } catch (error) {
    console.error(error)
    ElMessage.error(isEdit.value ? '角色更新失败' : '角色创建失败')
  } finally {
    saving.value = false
  }
}

const updateRoleStatus = async (role: Role, newStatus: string | number | boolean) => {
  if (!canEditRole.value) {
    ElMessage.warning('暂无修改角色状态的权限')
    return
  }

  const statusValue = String(newStatus)
  const previousStatus = role.status

  if (isSuperAdmin(role)) {
    role.status = previousStatus
    ElMessage.warning('超级管理员角色状态不可修改')
    return
  }

  role.status = statusValue
  try {
    await roleApi.updateRole(role.id, { status: statusValue })
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error(error)
    role.status = previousStatus
    ElMessage.error('状态更新失败')
  }
}

const deleteRole = async (role: Role) => {
  if (!canDeleteRole.value) {
    ElMessage.warning('暂无删除角色权限')
    return
  }
  if (isSuperAdmin(role)) {
    ElMessage.warning('超级管理员角色不能删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除角色 “${role.name}” 吗？`,
      '提示',
      { type: 'warning' }
    )
    await roleApi.deleteRole(role.id)
    ElMessage.success('角色删除成功')
    await loadRoles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('角色删除失败')
    }
  }
}

const batchDeleteRoles = async () => {
  if (!selectedRoles.value.length) {
    return
  }
  if (!canBatchDeleteRole.value) {
    ElMessage.warning('暂无删除角色权限')
    return
  }
  if (selectedRoles.value.some(isSuperAdmin)) {
    ElMessage.warning('选中项包含超级管理员，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRoles.value.length} 个角色吗？`,
      '提示',
      { type: 'warning' }
    )
    const ids = selectedRoles.value.map(role => role.id)
    await roleApi.batchDeleteRoles(ids)
    ElMessage.success('批量删除成功')
    selectedRoles.value = []
    await loadRoles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('批量删除失败')
    }
  }
}

const assignPermissions = async (role: Role) => {
  if (!canAssignPermissions.value) {
    ElMessage.warning('暂无分配权限的权限')
    return
  }

  currentRole.value = role
  permissionDialogVisible.value = true
  permissionTree.value = []
  checkedPermissions.value = []

  try {
    const response = await menuApi.roleTreeSelect(role.id)
    permissionTree.value = response?.menus || []
    const checked = response?.checkedKeys || []
    checkedPermissions.value = checked.map((item: any) => Number(item))
    await nextTick()
    permissionTreeRef.value?.setCheckedKeys(checkedPermissions.value, false)
  } catch (error) {
    console.error(error)
    ElMessage.error('加载权限数据失败')
  }
}

const savePermissions = async () => {
  if (!currentRole.value) {
    return
  }
  if (!canAssignPermissions.value) {
    ElMessage.warning('暂无保存角色权限的权限')
    return
  }

  savingPermissions.value = true
  try {
    const checked = (permissionTreeRef.value?.getCheckedKeys(false) || []) as Array<string | number>
    const halfChecked = (permissionTreeRef.value?.getHalfCheckedKeys?.() || []) as Array<string | number>
    const menuIds = Array.from(new Set([...checked, ...halfChecked])).map(id => Number(id))
    await roleApi.saveRolePermissions(currentRole.value.id, menuIds)
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch (error) {
    console.error(error)
    ElMessage.error('权限分配失败')
  } finally {
    savingPermissions.value = false
  }
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
.role-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #303133;
}

.page-description {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.filter-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-right {
  display: flex;
  gap: 8px;
}

.batch-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 6px;
}

.batch-info {
  color: #1890ff;
  font-weight: 500;
}

.batch-actions {
  display: flex;
  gap: 8px;
}

.table-container {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.permission-assignment {
  padding: 20px 0;
}

.permission-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.permission-header h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
}

.permission-tree {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .role-management {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .header-right {
    width: 100%;
    justify-content: flex-start;
  }

  .filter-toolbar {
    flex-direction: column;
    gap: 12px;
  }

  .filter-left,
  .filter-right {
    width: 100%;
    flex-wrap: wrap;
  }

  .batch-toolbar {
    flex-direction: column;
    gap: 12px;
  }

  .batch-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}
</style>
