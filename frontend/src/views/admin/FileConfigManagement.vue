<template>
  <div class="file-config-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <el-icon><Setting /></el-icon>
          文件配置管理
        </h1>
        <p class="page-description">管理文件存储配置，支持本地存储、阿里云OSS、七牛云等</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增配置
        </el-button>
      </div>
    </div>

    <!-- 配置列表 -->
    <el-card class="table-card">
      <el-table v-loading="loading" :data="configList">
        <el-table-column label="配置名称" prop="name" />
        <el-table-column label="存储类型" align="center" width="120">
          <template #default="scope">
            <el-tag :type="getStorageTagType(scope.row.storage)">
              {{ getStorageLabel(scope.row.storage) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否主配置" align="center" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.master" type="success">主配置</el-tag>
            <el-tag v-else type="info">普通配置</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" :show-overflow-tooltip="true" />
        <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
        <el-table-column label="操作" align="center" width="280" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button
              link
              type="primary"
              icon="View"
              @click="handleView(scope.row)"
            >
              查看
            </el-button>
            <el-button
              link
              type="primary"
              icon="Edit"
              @click="handleUpdate(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="!scope.row.master"
              link
              type="success"
              icon="Check"
              @click="handleSetMaster(scope.row)"
            >
              设为主配置
            </el-button>
            <el-button
              link
              type="warning"
              icon="Connection"
              @click="handleTest(scope.row)"
            >
              测试
            </el-button>
            <el-button
              link
              type="danger"
              icon="Delete"
              @click="handleDelete(scope.row)"
              :disabled="scope.row.master"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="dialogTitle"
      width="800px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入配置名称" />
        </el-form-item>
        
        <el-form-item label="存储类型" prop="storage">
          <el-radio-group v-model="form.storage">
            <el-radio :label="1">本地存储</el-radio>
            <el-radio :label="2">云存储 (S3兼容)</el-radio>
          </el-radio-group>
          <div class="form-tip">云存储支持阿里云OSS、七牛云、MinIO等S3兼容的存储服务</div>
        </el-form-item>

        <el-form-item label="是否主配置">
          <el-switch v-model="form.master" />
          <span class="form-tip">主配置将作为默认的文件存储配置</span>
        </el-form-item>

        <el-form-item label="存储配置" prop="config">
          <el-input
            v-model="form.config"
            type="textarea"
            :rows="8"
            placeholder="请输入JSON格式的存储配置"
          />
          <div class="config-examples">
            <el-collapse>
              <el-collapse-item title="查看配置示例" name="examples">
                <div class="example-section">
                  <h4>本地存储配置示例：</h4>
                  <pre>{{ localConfigExample }}</pre>
                  
                  <h4>云存储(S3兼容)配置示例：</h4>
                  <pre>{{ s3ConfigExample }}</pre>
                  
                  <h4>阿里云OSS配置示例：</h4>
                  <pre>{{ aliyunConfigExample }}</pre>
                  
                  <h4>七牛云配置示例：</h4>
                  <pre>{{ qiniuConfigExample }}</pre>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showDialog = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting,
  Plus,
  View,
  Edit,
  Delete,
  Check,
  Connection
} from '@element-plus/icons-vue'
import { fileConfigApi } from '@/api/infra'

// 响应式数据
const loading = ref(false)
const showDialog = ref(false)
const submitting = ref(false)
const configList = ref<any[]>([])
const form = reactive({
  id: null,
  name: '',
  storage: 1,
  master: false,
  config: '',
  remark: ''
})

// 表单引用
const formRef = ref()

// 计算属性
const dialogTitle = computed(() => {
  return form.id ? '编辑文件配置' : '新增文件配置'
})

// 配置示例
const localConfigExample = `{
  "basePath": "D:/simpleshare/upload",
  "baseUrl": "/upload",
  "pathPrefix": ""
}`

const s3ConfigExample = `{
  "endpoint": "s3.amazonaws.com",
  "bucket": "your-bucket-name",
  "accessKey": "your-access-key",
  "accessSecret": "your-access-secret",
  "domain": "https://your-domain.com"
}`

const aliyunConfigExample = `{
  "endpoint": "oss-cn-hangzhou.aliyuncs.com",
  "bucket": "your-bucket-name",
  "accessKey": "your-access-key",
  "accessSecret": "your-access-secret",
  "domain": "https://your-domain.com"
}`

const qiniuConfigExample = `{
  "endpoint": "s3-cn-north-1.qiniucs.com",
  "bucket": "your-bucket-name",
  "accessKey": "your-access-key",
  "accessSecret": "your-access-secret",
  "domain": "https://your-domain.com"
}`

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入配置名称', trigger: 'blur' }
  ],
  storage: [
    { required: true, message: '请选择存储类型', trigger: 'change' }
  ],
  config: [
    { required: true, message: '请输入存储配置', trigger: 'blur' },
    { validator: validateJsonConfig, trigger: 'blur' }
  ]
}

// 方法
function validateJsonConfig(rule: any, value: string, callback: Function) {
  if (!value) {
    callback(new Error('请输入存储配置'))
    return
  }
  
  try {
    JSON.parse(value)
    callback()
  } catch (error) {
    callback(new Error('配置格式不正确，请输入有效的JSON格式'))
  }
}

const getList = async () => {
  loading.value = true
  try {
    const response = await fileConfigApi.getConfigList({})
    const items = Array.isArray(response?.items)
      ? response.items
      : Array.isArray(response?.rows)
        ? response.rows
        : []
    configList.value = items
  } catch (error) {
    ElMessage.error('获取配置列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  resetForm()
  showDialog.value = true
}

const handleView = async (row: any) => {
  try {
    const response = await fileConfigApi.getConfigInfo(row.id)
    Object.assign(form, response ?? {})
    showDialog.value = true
  } catch (error) {
    ElMessage.error('获取配置详情失败')
  }
}

const handleUpdate = async (row: any) => {
  try {
    const response = await fileConfigApi.getConfigInfo(row.id)
    Object.assign(form, response ?? {})
    showDialog.value = true
  } catch (error) {
    ElMessage.error('获取配置详情失败')
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该配置吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await fileConfigApi.deleteConfig([row.id])
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSetMaster = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认将此配置设为主配置吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await fileConfigApi.setMasterConfig(row.id)
    ElMessage.success('设置成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('设置失败')
    }
  }
}

const handleTest = async (row: any) => {
  try {
    await fileConfigApi.testConfig(row.id)
    ElMessage.success('配置测试成功')
  } catch (error) {
    ElMessage.error('配置测试失败')
  }
}

const submitForm = async () => {
  try {
    await formRef.value?.validate()
    
    submitting.value = true
    
    if (form.id) {
      await fileConfigApi.updateConfig(form)
      ElMessage.success('修改成功')
    } else {
      await fileConfigApi.addConfig(form)
      ElMessage.success('新增成功')
    }
    
    showDialog.value = false
    getList()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.storage = 1
  form.master = false
  form.config = ''
  form.remark = ''
  formRef.value?.resetFields()
}

const getStorageLabel = (storage: number) => {
  const storageMap: Record<number, string> = {
    1: '本地存储',
    2: '云存储'
  }
  return storageMap[storage] || '未知'
}

const getStorageTagType = (storage: number) => {
  const typeMap: Record<number, string> = {
    1: 'info',
    2: 'success'
  }
  return typeMap[storage] || 'info'
}

// 生命周期
onMounted(() => {
  getList()
})
</script>

<style scoped>
.file-config-management {
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

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}

.config-examples {
  margin-top: 12px;
}

.example-section h4 {
  margin: 16px 0 8px 0;
  color: #303133;
  font-size: 14px;
}

.example-section pre {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  margin: 8px 0;
  overflow-x: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .file-config-management {
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
}
</style>
