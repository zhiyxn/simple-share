<template>
  <div class="tenant-management">
    <!-- 操作工具栏 -->
    <div class="action-toolbar">
      <div class="toolbar-left">
        <h3 class="section-title">租户列表</h3>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          添加租户
        </el-button>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-toolbar">
      <div class="filter-left">
        <el-input
          v-model="searchQuery"
          placeholder="搜索租户名称、编号或域名"
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
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </div>
      
      <div class="filter-right">
        <el-button @click="resetFilters">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
        <el-button @click="exportTenants">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 批量操作工具栏 -->
    <div v-if="selectedTenants.length > 0" class="batch-toolbar">
      <div class="batch-info">
        已选择 {{ selectedTenants.length }} 个租户
      </div>
      <div class="batch-actions">
        <el-button @click="batchUpdateStatus('0')">
          <el-icon><Check /></el-icon>
          批量启用
        </el-button>
        <el-button @click="batchUpdateStatus('1')">
          <el-icon><Close /></el-icon>
          批量停用
        </el-button>
        <el-button type="danger" @click="batchDeleteTenants">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 租户表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="filteredTenants"
        @selection-change="handleSelectionChange"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="tenantCode" label="租户编号" min-width="120" />
        
        <el-table-column prop="tenantName" label="租户名称" min-width="150" />
        
        <el-table-column prop="domain" label="域名" min-width="180">
          <template #default="{ row }">
            <span v-if="row.domain">{{ row.domain }}</span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="contactUserName" label="联系人" width="120">
          <template #default="{ row }">
            <span v-if="row.contactUserName">{{ row.contactUserName }}</span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="contactPhone" label="联系电话" width="140">
          <template #default="{ row }">
            <span v-if="row.contactPhone">{{ row.contactPhone }}</span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="companyName" label="企业名称" min-width="150">
          <template #default="{ row }">
            <span v-if="row.companyName">{{ row.companyName }}</span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>

        <el-table-column label="过期时间" width="160">
          <template #default="{ row }">
            <span v-if="row.expireTime">
              <el-tag
                :type="getExpireStatusTagType(row.expireTime)"
                size="small"
              >
                {{ formatDate(row.expireTime) }}
              </el-tag>
            </span>
            <span v-else class="text-gray-400">永久</span>
          </template>
        </el-table-column>

        <el-table-column prop="accountCount" label="用户数量" width="100">
          <template #default="{ row }">
            <span v-if="row.accountCount === -1">不限制</span>
            <span v-else>{{ row.accountCount || 0 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="0"
              inactive-value="1"
              @change="value => updateTenantStatus(row, value)"
            />
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="editTenant(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="deleteTenant(row)"
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

    <!-- 创建/编辑租户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑租户' : '创建租户'"
      width="800px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="tenantForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="租户编号" prop="tenantCode">
              <el-input
                v-model="tenantForm.tenantCode"
                :disabled="isEdit"
                placeholder="请输入租户编号"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="租户名称" prop="tenantName">
              <el-input
                v-model="tenantForm.tenantName"
                placeholder="请输入租户名称"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactUserName">
              <el-input
                v-model="tenantForm.contactUserName"
                placeholder="请输入联系人"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input
                v-model="tenantForm.contactPhone"
                placeholder="请输入联系电话"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业名称" prop="companyName">
              <el-input
                v-model="tenantForm.companyName"
                placeholder="请输入企业名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="域名" prop="domain">
              <el-input
                v-model="tenantForm.domain"
                placeholder="请输入域名"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="统一社会信用代码" prop="licenseNumber">
              <el-input
                v-model="tenantForm.licenseNumber"
                placeholder="请输入统一社会信用代码"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户数量限制" prop="accountCount">
              <el-input-number
                v-model="tenantForm.accountCount"
                :min="-1"
                placeholder="用户数量限制（-1为不限制）"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="过期时间" prop="expireTime">
              <el-date-picker
                v-model="tenantForm.expireTime"
                type="datetime"
                placeholder="选择过期时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="tenantForm.status">
                <el-radio value="0">正常</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="地址" prop="address">
          <el-input
            v-model="tenantForm.address"
            placeholder="请输入地址"
          />
        </el-form-item>

        <el-form-item label="企业简介" prop="intro">
          <el-input
            v-model="tenantForm.intro"
            type="textarea"
            :rows="3"
            placeholder="请输入企业简介"
          />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="tenantForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OfficeBuilding, Plus, Search, Refresh, Download, Check, Close, Delete } from '@element-plus/icons-vue'
import { tenantApi } from '@/api/tenant'
import type { Tenant, TenantCreateData } from '@/types/tenant'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const searchQuery = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedTenants = ref<Tenant[]>([])
const tenants = ref<Tenant[]>([])

// 表单相关
const formRef = ref()
const tenantForm = reactive<TenantCreateData & { tenantId?: number }>({
  tenantCode: '',
  tenantName: '',
  contactUserName: '',
  contactPhone: '',
  companyName: '',
  licenseNumber: '',
  address: '',
  domain: '',
  intro: '',
  remark: '',
  packageId: null,
  expireTime: '',
  accountCount: -1,
  status: '0'
})

// 表单验证规则
const formRules = {
  tenantCode: [
    { required: true, message: '请输入租户编号', trigger: 'blur' },
    { min: 2, max: 30, message: '租户编号长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  tenantName: [
    { required: true, message: '请输入租户名称', trigger: 'blur' },
    { min: 2, max: 50, message: '租户名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  domain: [
    { pattern: /^[a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?)*$/, message: '请输入正确的域名格式', trigger: 'blur' }
  ]
}

// 计算属性
const filteredTenants = computed(() => {
  let result = tenants.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(tenant => 
      tenant.tenantName.toLowerCase().includes(query) ||
      tenant.tenantCode.toLowerCase().includes(query) ||
      (tenant.domain && tenant.domain.toLowerCase().includes(query))
    )
  }

  if (filterStatus.value) {
    result = result.filter(tenant => tenant.status === filterStatus.value)
  }

  return result
})

// 方法
const loadTenants = async () => {
  try {
    loading.value = true
    const params: Record<string, any> = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      orderByColumn: 'createTime',
      isAsc: 'desc'
    }

    if (filterStatus.value) {
      params.status = filterStatus.value
    }

    const pageData = await tenantApi.getTenants(params)

    tenants.value = pageData?.items ?? []
    total.value = pageData?.total ?? 0
    selectedTenants.value = []
  } catch (error) {
    console.error('获取租户列表失败:', error)
    ElMessage.error('获取租户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
  loadTenants()
}

const resetFilters = () => {
  searchQuery.value = ''
  filterStatus.value = ''
  currentPage.value = 1
  loadTenants()
}

const handleSelectionChange = (selection: Tenant[]) => {
  selectedTenants.value = selection
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadTenants()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadTenants()
}

const showCreateDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
  resetForm()
}

const editTenant = (tenant: Tenant) => {
  isEdit.value = true
  dialogVisible.value = true
  
  // 填充表单数据
  Object.assign(tenantForm, {
    tenantId: tenant.tenantId,
    tenantCode: tenant.tenantCode,
    tenantName: tenant.tenantName,
    contactUserName: tenant.contactUserName || '',
    contactPhone: tenant.contactPhone || '',
    companyName: tenant.companyName || '',
    licenseNumber: tenant.licenseNumber || '',
    address: tenant.address || '',
    domain: tenant.domain || '',
    intro: tenant.intro || '',
    remark: tenant.remark || '',
    packageId: tenant.packageId,
    expireTime: tenant.expireTime || '',
    accountCount: tenant.accountCount || -1,
    status: tenant.status
  })
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  
  Object.assign(tenantForm, {
    tenantId: undefined,
    tenantCode: '',
    tenantName: '',
    contactUserName: '',
    contactPhone: '',
    companyName: '',
    licenseNumber: '',
    address: '',
    domain: '',
    intro: '',
    remark: '',
    packageId: null,
    expireTime: '',
    accountCount: -1,
    status: '0'
  })
}

const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      const updateData = {
        tenantId: tenantForm.tenantId!,
        tenantCode: tenantForm.tenantCode,
        tenantName: tenantForm.tenantName,
        contactUserName: tenantForm.contactUserName,
        contactPhone: tenantForm.contactPhone,
        companyName: tenantForm.companyName,
        licenseNumber: tenantForm.licenseNumber,
        address: tenantForm.address,
        domain: tenantForm.domain,
        intro: tenantForm.intro,
        remark: tenantForm.remark,
        packageId: tenantForm.packageId,
        expireTime: tenantForm.expireTime,
        accountCount: tenantForm.accountCount,
        status: tenantForm.status
      }

      await tenantApi.updateTenant(updateData.tenantId, updateData)
      dialogVisible.value = false
      loadTenants()
    } else {
      const createData: TenantCreateData = {
        tenantCode: tenantForm.tenantCode,
        tenantName: tenantForm.tenantName,
        contactUserName: tenantForm.contactUserName,
        contactPhone: tenantForm.contactPhone,
        companyName: tenantForm.companyName,
        licenseNumber: tenantForm.licenseNumber,
        address: tenantForm.address,
        domain: tenantForm.domain,
        intro: tenantForm.intro,
        remark: tenantForm.remark,
        packageId: tenantForm.packageId,
        expireTime: tenantForm.expireTime,
        accountCount: tenantForm.accountCount,
        status: tenantForm.status
      }

      await tenantApi.createTenant(createData)
      dialogVisible.value = false
      loadTenants()
    }
  } catch (error) {
    console.error('提交表单失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const updateTenantStatus = async (tenant: Tenant, value: string | number) => {
  const newStatus = String(value)
  const previousStatus = newStatus === '0' ? '1' : '0'

  try {
    await tenantApi.updateTenantStatus(tenant.tenantId, newStatus)
    tenant.status = newStatus
  } catch (error) {
    console.error('更新状态失败:', error)
    tenant.status = previousStatus
    ElMessage.error('状态更新失败')
  }
}

const deleteTenant = async (tenant: Tenant) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除租户 "${tenant.tenantName}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await tenantApi.deleteTenant(tenant.tenantId)
    selectedTenants.value = selectedTenants.value.filter(item => item.tenantId !== tenant.tenantId)
    loadTenants()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除租户失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const batchUpdateStatus = async (status: string) => {
  if (selectedTenants.value.length === 0) {
    ElMessage.warning('请选择要操作的租户')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要${status === '0' ? '启用' : '停用'}选中的 ${selectedTenants.value.length} 个租户吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const tenantIds = selectedTenants.value.map(tenant => tenant.tenantId)
    await Promise.all(
      tenantIds.map(id => tenantApi.updateTenantStatus(id, status, {
        showSuccess: false,
        showLoading: false
      }))
    )

    ElMessage.success(status === '0' ? '批量启用成功' : '批量停用成功')
    selectedTenants.value = []
    loadTenants()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量更新状态失败:', error)
      ElMessage.error('批量操作失败')
    }
  }
}

const batchDeleteTenants = async () => {
  if (selectedTenants.value.length === 0) {
    ElMessage.warning('请选择要删除的租户')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedTenants.value.length} 个租户吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const tenantIds = selectedTenants.value.map(tenant => tenant.tenantId)
    await tenantApi.batchDeleteTenants(tenantIds)
    selectedTenants.value = []
    loadTenants()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const exportTenants = async () => {
  try {
    const params: Record<string, any> = {}
    if (filterStatus.value) {
      params.status = filterStatus.value
    }
    if (searchQuery.value) {
      params.tenantName = searchQuery.value
    }

    await tenantApi.exportTenants(params)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

const getExpireStatusTagType = (expireTime: string) => {
  if (!expireTime) return ''
  
  const now = new Date()
  const expire = new Date(expireTime)
  const diffDays = Math.ceil((expire.getTime() - now.getTime()) / (1000 * 60 * 60 * 24))
  
  if (diffDays < 0) return 'danger'  // 已过期
  if (diffDays <= 7) return 'warning'  // 7天内过期
  if (diffDays <= 30) return 'info'  // 30天内过期
  return 'success'  // 正常
}

// 生命周期
onMounted(() => {
  loadTenants()
})
</script>

<style scoped>
.tenant-management {
  padding: 0;
}

.action-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 0;
}

.toolbar-left {
  flex: 1;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.toolbar-right {
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
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.text-gray-400 {
  color: #9ca3af;
}
</style>
