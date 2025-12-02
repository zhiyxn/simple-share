<template>
  <div class="file-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <el-icon><Folder /></el-icon>
          文件管理
        </h1>
        <p class="page-description">管理系统中的所有文件，包括上传、删除、查看等操作</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showUploadDialog = true">
          <el-icon><Upload /></el-icon>
          上传文件
        </el-button>
        <el-button @click="refreshList">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
              <div class="stat-label">总文件数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><DataBoard /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalSizeFormatted || '0 B' }}</div>
              <div class="stat-label">总存储大小</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Picture /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.typeStats?.image || 0 }}</div>
              <div class="stat-label">图片文件</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><VideoPlay /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.typeStats?.video || 0 }}</div>
              <div class="stat-label">视频文件</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="文件名" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入文件名"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="文件类型" prop="type">
          <el-select v-model="queryParams.type" placeholder="请选择文件类型" clearable style="width: 150px">
            <el-option label="图片" value="image" />
            <el-option label="文档" value="document" />
            <el-option label="视频" value="video" />
            <el-option label="音频" value="audio" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 文件列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="fileList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="文件预览" align="center" width="100">
          <template #default="scope">
            <div class="file-preview">
              <img
                v-if="scope.row.type === 'image'"
                :src="getPreviewUrlSync(scope.row.id, scope.row.path)"
                class="preview-image"
                @click="handlePreviewFile(scope.row)"
              />
              <el-icon v-else class="file-icon" @click="handlePreviewFile(scope.row)">
                <Document v-if="scope.row.type === 'document'" />
                <VideoPlay v-else-if="scope.row.type === 'video'" />
                <Headset v-else-if="scope.row.type === 'audio'" />
                <Files v-else />
              </el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="文件名" prop="name" :show-overflow-tooltip="true" />
        <el-table-column label="文件类型" align="center" width="100">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)">
              {{ getTypeLabel(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="文件大小" align="center" width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.size) }}
          </template>
        </el-table-column>
        <el-table-column label="上传时间" align="center" prop="createTime" width="180" />
        <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button
              link
              type="primary"
              icon="View"
              @click="handlePreviewFile(scope.row)"
            >
              预览
            </el-button>
            <el-button
              link
              type="primary"
              icon="Download"
              @click="downloadFile(scope.row)"
            >
              下载
            </el-button>
            <el-button
              link
              type="danger"
              icon="Delete"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 批量操作 -->
    <div class="batch-actions" v-if="selectedFiles.length > 0">
      <el-button type="danger" @click="handleBatchDelete">
        <el-icon><Delete /></el-icon>
        批量删除 ({{ selectedFiles.length }})
      </el-button>
    </div>

    <!-- 上传对话框 -->
    <el-dialog v-model="showUploadDialog" title="上传文件" width="600px">
      <el-upload
        ref="uploadRef"
        class="upload-demo"
        drag
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        multiple
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持jpg/png/gif/pdf/doc/docx等格式，单个文件不超过10MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showUploadDialog = false">取消</el-button>
          <el-button type="primary" @click="showUploadDialog = false">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 文件预览对话框 -->
    <el-dialog v-model="showPreviewDialog" title="文件预览" width="800px">
      <div class="file-preview-content">
        <div class="preview-info">
          <h3>{{ previewFile?.name }}</h3>
          <p>类型：{{ getTypeLabel(previewFile?.type) }}</p>
          <p>大小：{{ formatFileSize(previewFile?.size) }}</p>
          <p>上传时间：{{ previewFile?.createTime }}</p>
        </div>
        <div class="preview-content">
          <img
            v-if="previewFile?.type === 'image'"
            :src="getPreviewUrlSync(previewFile?.id, previewFile?.path)"
            class="preview-image-large"
          />
          <video
            v-else-if="previewFile?.type === 'video'"
            :src="getPreviewUrlSync(previewFile?.id, previewFile?.path)"
            controls
            class="preview-video"
          />
          <audio
            v-else-if="previewFile?.type === 'audio'"
            :src="getPreviewUrlSync(previewFile?.id, previewFile?.path)"
            controls
            class="preview-audio"
          />
          <div v-else class="preview-placeholder">
            <el-icon><Document /></el-icon>
            <p>此文件类型不支持预览</p>
            <el-button type="primary" @click="downloadFile(previewFile)">
              下载文件
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Folder,
  Upload,
  Refresh,
  Document,
  DataBoard,
  Picture,
  VideoPlay,
  Search,
  View,
  Download,
  Delete,
  Check,
  Files,
  Headset,
  UploadFilled
} from '@element-plus/icons-vue'
import { fileApi } from '@/api/infra'
import { getToken } from '@/utils/auth'
import { useTenantStore } from '@/stores/tenant'
import { useFileConfig } from '@/composables/useFileConfig'

// 响应式数据
const loading = ref(false)
const showUploadDialog = ref(false)
const showPreviewDialog = ref(false)
const selectedFiles = ref<any[]>([])
const fileList = ref<any[]>([])
const total = ref(0)
const statistics = ref({
  totalCount: 0,
  totalSize: 0,
  totalSizeFormatted: '0 B',
  typeStats: {
    image: 0,
    document: 0,
    video: 0,
    audio: 0,
    other: 0
  }
})
const previewFile = ref<any | null>(null)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  type: ''
})

// 上传配置
const tenantStore = useTenantStore()
const apiPrefix = import.meta.env.VITE_API_BASE_URL || '/api'
const uploadUrl = `${apiPrefix}/infra/file/upload`
const uploadHeaders = ref<Record<string, string>>({})

// 文件配置
const { buildPreviewUrl, init: initFileConfig } = useFileConfig()

// 文件预览URL缓存
const previewUrlCache = ref<Map<string, string>>(new Map())

// 获取文件预览URL
const getFilePreviewUrl = async (fileId: string | number, filePath?: string) => {
  const cacheKey = `${fileId}_${filePath || ''}`
  
  if (previewUrlCache.value.has(cacheKey)) {
    return previewUrlCache.value.get(cacheKey)!
  }
  
  const url = await buildPreviewUrl(fileId, filePath)
  previewUrlCache.value.set(cacheKey, url)
  return url
}

// 同步获取预览URL（用于模板）
const getPreviewUrlSync = (fileId: string | number, filePath?: string) => {
  const cacheKey = `${fileId}_${filePath || ''}`
  
  if (previewUrlCache.value.has(cacheKey)) {
    return previewUrlCache.value.get(cacheKey)!
  }
  
  // 异步获取并缓存
  getFilePreviewUrl(fileId, filePath).then(() => {
    // 触发重新渲染
  })
  
  // 返回默认API URL
  const apiPrefix = import.meta.env.VITE_API_BASE_URL || '/api'
  return `${apiPrefix}/infra/file/preview/${fileId}`
}

const refreshUploadHeaders = () => {
  const headers: Record<string, string> = {}
  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  const tenantId = tenantStore.tenantId
  if (tenantId) {
    headers['X-Tenant-ID'] = String(tenantId)
  }
  uploadHeaders.value = headers
}

watch(() => tenantStore.tenantId, () => {
  refreshUploadHeaders()
})

watch(showUploadDialog, (visible) => {
  if (visible) {
    refreshUploadHeaders()
  }
})

refreshUploadHeaders()

// 方法
const getList = async () => {
  loading.value = true
  try {
    const response = await fileApi.getFileList({ ...queryParams })
    const items = Array.isArray(response?.items)
      ? response.items
      : Array.isArray(response?.rows)
        ? response.rows
        : []

    fileList.value = items
    total.value = (response?.total ?? response?.totalCount ?? items.length) || 0
    
    // 预加载图片预览URL
    items.forEach(item => {
      if (item.type === 'image') {
        getFilePreviewUrl(item.id, item.path)
      }
    })
  } catch (error) {
    ElMessage.error('获取文件列表失败')
  } finally {
    loading.value = false
  }
}

const getStatistics = async () => {
  try {
    const response = await fileApi.getFileStatistics()
    const payload = response ?? {}
    statistics.value = {
      totalCount: payload.totalCount ?? payload.total ?? 0,
      totalSize: payload.totalSize ?? 0,
      totalSizeFormatted: payload.totalSizeFormatted ?? formatFileSize(payload.totalSize ?? 0),
      typeStats: {
        image: payload.typeStats?.image ?? 0,
        document: payload.typeStats?.document ?? 0,
        video: payload.typeStats?.video ?? 0,
        audio: payload.typeStats?.audio ?? 0,
        other: payload.typeStats?.other ?? 0
      }
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.name = ''
  queryParams.type = ''
  handleQuery()
}

const refreshList = () => {
  getList()
  getStatistics()
}

const handleSelectionChange = (selection: any[]) => {
  selectedFiles.value = selection
}

const handleUploadSuccess = (response: any) => {
  ElMessage.success('文件上传成功')
  refreshList()
}

const handleUploadError = (error: any) => {
  ElMessage.error('文件上传失败')
}

const beforeUpload = (file: File) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
  }
  return isLt10M
}

const handlePreviewFile = (file: any) => {
  previewFile.value = file
  showPreviewDialog.value = true
}

const downloadFile = (file: any) => {
  // 使用后端API下载文件
  window.location.href = `${apiPrefix}/infra/file/download/${file.id}`
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该文件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await fileApi.deleteFile([row.id])
    ElMessage.success('删除成功')
    refreshList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedFiles.value.length} 个文件吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const ids = selectedFiles.value.map((file: any) => file.id)
    await fileApi.deleteFile(ids)
    ElMessage.success('批量删除成功')
    refreshList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const getTypeLabel = (type: string) => {
  const typeMap: Record<string, string> = {
    image: '图片',
    document: '文档',
    video: '视频',
    audio: '音频',
    other: '其他'
  }
  return typeMap[type] || '未知'
}

const getTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    image: 'success',
    document: 'info',
    video: 'warning',
    audio: 'primary',
    other: 'info'
  }
  return typeMap[type] || 'info'
}

const formatFileSize = (size: number) => {
  if (!size) return '0 B'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(2) + ' MB'
  return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

// 分页处理
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  queryParams.pageNum = 1
  getList()
}

const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val
  getList()
}

// 生命周期
onMounted(async () => {
  await initFileConfig()
  getList()
  getStatistics()
})
</script>

<style scoped>
.file-management {
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

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #409eff;
  background: #ecf5ff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.search-card,
.table-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.file-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.preview-image {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
}

.file-icon {
  font-size: 32px;
  color: #909399;
}

.batch-actions {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 20px 0;
}

.file-preview-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preview-info {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.preview-info h3 {
  margin: 0 0 12px 0;
  color: #303133;
}

.preview-info p {
  margin: 4px 0;
  color: #606266;
  font-size: 14px;
}

.preview-content {
  text-align: center;
}

.preview-image-large {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
}

.preview-video {
  width: 100%;
  max-height: 400px;
}

.preview-audio {
  width: 100%;
}

.preview-placeholder {
  padding: 40px;
  color: #909399;
}

.preview-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .file-management {
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
  
  .stats-cards .el-col {
    margin-bottom: 12px;
  }
}
</style>
