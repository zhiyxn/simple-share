<template>
  <div class="user-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <el-icon><User /></el-icon>
          用户管理
        </h1>
        <p class="page-description">管理系统用户，包括用户信息、权限设置等</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          添加用户
        </el-button>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-toolbar">
      <div class="filter-left">
        <el-input
          v-model="searchQuery"
          placeholder="搜索用户名、邮箱或昵称"
          style="width: 300px"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select
          v-model="filterRole"
          placeholder="筛选角色"
          style="width: 150px"
          clearable
          @change="handleFilter"
        >
          <el-option 
            v-for="role in roles" 
            :key="role.id" 
            :label="role.name" 
            :value="role.key" 
          />
        </el-select>
        
        <el-select
          v-model="filterStatus"
          placeholder="筛选状态"
          style="width: 150px"
          clearable
          @change="handleFilter"
        >
          <el-option label="启用" value="active" />
          <el-option label="禁用" value="inactive" />
        </el-select>
      </div>
      
      <div class="filter-right">
        <el-button @click="resetFilters">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
        <el-button @click="exportUsers">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 批量操作工具栏 -->
    <div v-if="selectedUsers.length > 0" class="batch-toolbar">
      <div class="batch-info">
        已选择 {{ selectedUsers.length }} 个用户
      </div>
      <div class="batch-actions">
        <el-button @click="batchUpdateStatus('active')">
          <el-icon><Check /></el-icon>
          批量启用
        </el-button>
        <el-button @click="batchUpdateStatus('inactive')">
          <el-icon><Close /></el-icon>
          批量禁用
        </el-button>
        <el-button type="danger" @click="batchDeleteUsers">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 用户表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="filteredUsers"
        @selection-change="handleSelectionChange"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :size="40">
              {{ row.nickname?.charAt(0) || row.username?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        
        <el-table-column prop="username" label="用户名" min-width="120" />
        
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        
        <el-table-column prop="email" label="邮箱" min-width="180" />
        
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getRoleTagType(row.role)"
              size="small"
            >
              {{ getRoleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="用户类型" width="120">
          <template #default="{ row }">
            <el-tag
              :type="getUserTypeTagType(row.userType)"
              size="small"
            >
              {{ getUserTypeLabel(row.userType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="VIP过期时间" width="160">
          <template #default="{ row }">
            <span v-if="row.userType === UserType.VIP && row.vipExpireTime">
              <el-tag
                :type="getVipStatusTagType(row.vipExpireTime)"
                size="small"
              >
                {{ formatVipExpireTime(row.vipExpireTime) }}
              </el-tag>
            </span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="active"
              inactive-value="inactive"
              @change="updateUserStatus(row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="发布审核" width="120">
          <template #default="{ row }">
            <el-tag :type="row.articleReviewRequired ? 'warning' : 'success'" size="small">
              {{ row.articleReviewRequired ? '需审核' : '免审核' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="lastLoginAt" label="最后登录" width="160">
          <template #default="{ row }">
            {{ formatDate(row.lastLoginAt) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="editUser(row)"
            >
              编辑
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="resetPassword(row)"
            >
              重置密码
            </el-button>
            
            <!-- VIP管理下拉菜单 -->
            <el-dropdown @command="(command) => handleVipAction(command, row)">
              <el-button type="success" size="small">
                VIP管理
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item 
                    command="setVip" 
                    v-if="row.userType !== UserType.VIP"
                  >
                    设为VIP
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="extendVip" 
                    v-if="row.userType === UserType.VIP"
                  >
                    延长VIP
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="cancelVip" 
                    v-if="row.userType === UserType.VIP"
                  >
                    取消VIP
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            
            <el-button
              type="danger"
              size="small"
              @click="deleteUser(row)"
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

    <!-- 创建/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isldit ? '编辑用户' : '创建用户'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="userForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="userForm.username"
                :disabled="isldit"
                placeholder="请输入用户名"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input
                v-model="userForm.nickname"
                placeholder="请输入昵称"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="文章审核">
              <el-switch
                v-model="userForm.articleReviewRequired"
                active-text="需要审核"
                inactive-text="免审核"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="userForm.email"
                placeholder="请输入邮箱"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="userForm.phone"
                placeholder="请输入手机号"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select
                v-model="userForm.role"
                placeholder="请选择角色"
                style="width: 100%"
              >
                <el-option 
                  v-for="role in roles" 
                  :key="role.id" 
                  :label="role.name" 
                  :value="role.key" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
          <el-select
            v-model="userForm.status"
            placeholder="请选择状态"
                style="width: 100%"
              >
                <el-option label="启用" value="active" />
                <el-option label="禁用" value="inactive" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户类型" prop="userType">
                            <el-select
                v-model="userForm.userType"
                placeholder="请选择用户类型"
                style="width: 100%"
              >
                <el-option label="普通用户" :value="UserType.NORMAL" />
                <el-option label="VIP会员" :value="UserType.VIP" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="userForm.userType === UserType.VIP">
            <el-form-item label="VIP到期" prop="vipExpireTime">
              <el-date-picker
                v-model="userForm.vipExpireTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择VIP到期时间"
                style="width: 100%"
                :teleported="false"
                autocomplete="off"
                :input-style="{ autocomplete: 'off' }"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="!isldit" label="密码" prop="password">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input
            v-model="userForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser" :loading="saving">
          {{ isldit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- VIP管理对话框 -->
    <el-dialog
      v-model="vipDialogVisible"
      :title="vipForm.action === 'set' ? '设置VIP' : '延长VIP'"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="vipFormRef"
        :model="vipForm"
        label-width="80px"
        :rules="{
          days: [
            { required: true, message: '请输入天数', trigger: 'blur' },
            { type: 'number', min: 1, max: 3650, message: '天数必须在1-3650之间', trigger: 'blur' }
          ]
        }"
      >
        <el-form-item label="天数" prop="days">
          <el-input-number
            v-model="vipForm.days"
            :min="1"
            :max="3650"
            placeholder="请输入天数"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item>
          <el-text type="info" size="small">
            {{ vipForm.action === 'set' ? '设置用户为VIP会员' : '延长用户VIP有效期' }}
          </el-text>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="vipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleVipSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Plus,
  Search,
  Refresh,
  Download,
  Check,
  Close,
  Delete,
  ArrowDown
} from '@element-plus/icons-vue'
import { userApi } from '@/api/user'
import { roleApi } from '@/api/role'
import type { User as UserModel, VipManagementData } from '@/types/user'
import type { Role } from '@/types/role'
import { UserType, UserTypeUtils } from '@/types/user'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const users = ref<UserModel[]>([])
const selectedUsers = ref<UserModel[]>([])
const searchQuery = ref('')
const filterRole = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 角色列表
const roles = ref<Role[]>([])

// 对话框相关
const dialogVisible = ref(false)
const isldit = ref(false)
const formRef = ref()
const userForm = reactive({
  id: '',
  username: '',
  nickname: '',
  email: '',
  phone: '',
  role: 'user',
  status: 'active',
  userType: UserType.NORMAL,
  vipExpireTime: '',
  articleReviewRequired: true,
  password: '',
  avatar: '',
  remark: ''
})

const validateVipExpireTime = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (userForm.userType === UserType.VIP && !value) {
    callback(new Error('请选择VIP到期时间'))
  } else {
    callback()
  }
}

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  vipExpireTime: [
    { validator: validateVipExpireTime, trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 上传地址
const uploadUrl = ref('/upload/avatar')

watch(
  () => userForm.userType,
  (value) => {
    if (value !== UserType.VIP) {
      userForm.vipExpireTime = ''
    }
  }
)

// 计算属性
const filteredUsers = computed(() => {
  let result = users.value
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(user => 
      user.username.toLowerCase().includes(query) ||
      user.nickname?.toLowerCase().includes(query) ||
      user.email.toLowerCase().includes(query)
    )
  }
  
  if (filterRole.value) {
    result = result.filter(user => user.role === filterRole.value)
  }
  
  if (filterStatus.value) {
    result = result.filter(user => user.status === filterStatus.value)
  }
  
  return result
})

// 方法
const loadUsers = async () => {
  try {
    loading.value = true
    const response = await userApi.getUsers({
      page: currentPage.value,
      pageSize: pageSize.value,
      search: searchQuery.value,
      role: filterRole.value,
      status: filterStatus.value
    })
    const items = response.items || []
    users.value = items.map((item) => ({
      ...item,
      articleReviewRequired: item.articleReviewRequired !== false
    }))
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 加载角色列表
const loadRoles = async () => {
  try {
    const roleList = await roleApi.getAllRoles()
    roles.value = roleList || []
  } catch (error) {
    console.error('加载角色列表失败:', error)
    // 如果API失败，使用默认角色
    roles.value = [
      { id: 'admin', name: '管理员', key: 'admin', sort: 1, status: '0', createTime: '', updateTime: '' },
      { id: 'editor', name: '编辑者', key: 'editor', sort: 2, status: '0', createTime: '', updateTime: '' },
      { id: 'user', name: '用户', key: 'user', sort: 3, status: '0', createTime: '', updateTime: '' }
    ]
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadUsers()
}

const handleFilter = () => {
  currentPage.value = 1
  loadUsers()
}

const resetFilters = () => {
  searchQuery.value = ''
  filterRole.value = ''
  filterStatus.value = ''
  currentPage.value = 1
  loadUsers()
}

const handleSelectionChange = (selection: UserModel[]) => {
  selectedUsers.value = selection
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadUsers()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadUsers()
}

const showCreateDialog = () => {
  isldit.value = false
  resetForm()
  dialogVisible.value = true
}

const editUser = (user: UserModel) => {
  isldit.value = true
  Object.assign(userForm, {
    id: user.id ?? '',
    username: user.username ?? '',
    nickname: user.nickname ?? user.username ?? '',
    email: user.email ?? '',
    phone: (user as any).phone ?? '',
    role: user.role ?? 'user',
    status: user.status ?? 'active',
    userType: (user.userType as UserType) ?? UserType.NORMAL,
    vipExpireTime: user.vipExpireTime ?? '',
    articleReviewRequired: user.articleReviewRequired ?? true,
    password: '',
    avatar: user.avatar ?? '',
    remark: (user as any).remark ?? ''
  })
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(userForm, {
    id: '',
    username: '',
    nickname: '',
    email: '',
    phone: '',
    role: 'user',
    status: 'active',
    userType: UserType.NORMAL,
    vipExpireTime: '',
    articleReviewRequired: true,
    password: '',
    avatar: '',
    remark: ''
  })
}

const saveUser = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true
    
    if (isldit.value) {
      await userApi.updateUser(userForm.id, userForm)
    } else {
      await userApi.createUser(userForm)
    }
    
    dialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error(isldit.value ? '用户更新失败' : '用户创建失败')
  } finally {
    saving.value = false
  }
}

const updateUserStatus = async (user: UserModel) => {
  try {
    await userApi.updateUserStatus(user.id, user.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    user.status = user.status === 'active' ? 'inactive' : 'active'
  }
}

const deleteUser = async (user: UserModel) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${user.username}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await userApi.deleteUser(user.id)
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('用户删除失败')
    }
  }
}

const resetPassword = async (user: UserModel) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户"${user.username}" 的密码吗？`,
      '确认重置密码',
      {
        confirmButtonText: '重置',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await userApi.resetPassword(user.id)
    ElMessage.success(`密码重置成功，新密码：${response.data.password}`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('密码重置失败')
    }
  }
}

const batchUpdateStatus = async (status: string) => {
  try {
    await ElMessageBox.confirm(
      `确定要${status === 'active' ? '启用' : '禁用'}选中的${selectedUsers.value.length} 个用户吗？`,
      '确认批量操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const userIds = selectedUsers.value.map(user => user.id)
    await userApi.batchUpdateStatus(userIds, status)
    ElMessage.success('批量操作成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量操作失败')
    }
  }
}

const batchDeleteUsers = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的${selectedUsers.value.length} 个用户吗？此操作不可恢复！`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const userIds = selectedUsers.value.map(user => user.id)
    await userApi.batchDeleteUsers(userIds)
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const exportUsers = async () => {
  try {
    const response = await userApi.exportUsers({
      search: searchQuery.value,
      role: filterRole.value,
      status: filterStatus.value
    })
    
    // 创建下载链接
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `users_${new Date().toISOString().split('T')[0]}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handleAvatarSuccess = (response: any) => {
  userForm.avatar = response.data.url
}

const beforeAvatarUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isJPG) {
    ElMessage.error('头像只能是JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

const getRoleLabel = (role: string) => {
  const roleMap: Record<string, string> = {
    admin: '管理员',
    editor: '编辑',
    user: '用户'
  }
  return roleMap[role] || role
}

const getRoleTagType = (role: string) => {
  const typeMap: Record<string, string> = {
    admin: 'danger',
    editor: 'warning',
    user: 'info'
  }
  return typeMap[role] || 'info'
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

// VIP管理相关方法
const getUserTypeLabel = (userType: string) => {
  return UserTypeUtils.getTypeDesc(userType as UserType)
}

const getUserTypeTagType = (userType: string) => {
  const typeMap: Record<string, string> = {
    '1': 'danger',   // 管理员
    '0': 'info',     // 普通用户
    '3': 'success'   // VIP会员
  }
  return typeMap[userType] || 'info'
}

const formatVipExpireTime = (vipExpireTime: string) => {
  if (!vipExpireTime) return '-'
  const remainingDays = UserTypeUtils.getVipRemainingDays(vipExpireTime)
  if (remainingDays <= 0) {
    return '已过期'
  } else if (remainingDays <= 7) {
    return `${remainingDays}天后过期`
  } else {
    return new Date(vipExpireTime).toLocaleDateString('zh-CN')
  }
}

const getVipStatusTagType = (vipExpireTime: string) => {
  const remainingDays = UserTypeUtils.getVipRemainingDays(vipExpireTime)
  if (remainingDays <= 0) {
    return 'danger'  // 已过期
  } else if (remainingDays <= 7) {
    return 'warning' // 即将过期
  } else {
    return 'success' // 正常
  }
}

// VIP操作处理
const handleVipAction = async (command: string, user: UserModel) => {
  try {
    switch (command) {
      case 'setVip':
        await showVipDialog(user, 'set')
        break
      case 'extendVip':
        await showVipDialog(user, 'extend')
        break
      case 'cancelVip':
        await handleCancelVip(user)
        break
    }
  } catch (error) {
    console.error('VIP操作失败:', error)
  }
}

// VIP设置/延长对话框
const vipDialogVisible = ref(false)
const vipForm = reactive({
  userId: '',
  days: 30,
  action: 'set' as 'set' | 'extend'
})
const vipFormRef = ref()

const showVipDialog = (user: UserModel, action: 'set' | 'extend') => {
  vipForm.userId = user.id
  vipForm.action = action
  vipForm.days = 30
  vipDialogVisible.value = true
}

const handleVipSubmit = async () => {
  try {
    await vipFormRef.value?.validate()
    
    const data: VipManagementData = {
      userId: vipForm.userId,
      days: vipForm.days
    }
    
    if (vipForm.action === 'set') {
      await userApi.vip.setUserVip(data)
    } else {
      await userApi.vip.extendUserVip(data)
    }
    
    vipDialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleCancelVip = async (user: UserModel) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消用户${user.username} 的VIP权限吗？`,
      '确认取消VIP',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await userApi.vip.cancelUserVip(user.id)
    ElMessage.success('VIP权限取消成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消VIP失败')
    }
  }
}

// 生命周期
onMounted(() => {
  loadUsers()
  loadRoles()
})
</script>

<style scoped>
.user-management {
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
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
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

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
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

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

/* 响应式设置*/
@media (max-width: 768px) {
  .user-management {
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
