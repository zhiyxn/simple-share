<template>
  <div class="operation-log-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">操作日志</h2>
        <p class="page-desc">查看系统内所有操作请求及结果，支持多条件过滤。</p>
      </div>
      <div class="header-actions">
        <el-button :loading="loading" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="danger" @click="handleClean" :loading="cleaning">
          <el-icon><Delete /></el-icon>
          清空日志
        </el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <div class="filter-grid">
        <el-input v-model="filters.requestUri" placeholder="请求URI" clearable @keyup.enter="fetchLogs" />
        <el-input v-model="filters.operatorName" placeholder="操作人" clearable @keyup.enter="fetchLogs" />
        <el-select v-model="filters.requestMethod" placeholder="请求方式" clearable>
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" />
          <el-option label="DELETE" value="DELETE" />
          <el-option label="PATCH" value="PATCH" />
        </el-select>
        <el-select v-model="filters.businessType" placeholder="业务类型" clearable>
          <el-option label="新增" value="INSERT" />
          <el-option label="修改" value="UPDATE" />
          <el-option label="删除" value="DELETE" />
          <el-option label="其它" value="OTHER" />
        </el-select>
        <el-select v-model="filters.status" placeholder="执行状态" clearable>
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
        <el-date-picker
          v-model="filters.dateRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          range-separator="至"
          clearable
        />
        <div class="filter-actions">
          <el-button type="primary" @click="fetchLogs" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetFilters">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table :data="logs" v-loading="loading" border height="600px">
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column prop="requestMethod" label="方式" width="90">
          <template #default="{ row }">
            <el-tag :type="methodTagType(row.requestMethod)">{{ row.requestMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUri" label="请求URI" min-width="220">
          <template #default="{ row }">
            <span class="uri-text">{{ row.requestUri }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="140" />
        <el-table-column prop="clientIp" label="IP" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseStatus" label="响应码" width="100" />
        <el-table-column prop="duration" label="耗时" width="120">
          <template #default="{ row }">
            {{ formatDuration(row.duration) }}
          </template>
        </el-table-column>
        <el-table-column label="请求参数" min-width="200">
          <template #default="{ row }">
            <el-popover placement="top" width="400" trigger="hover">
              <pre class="log-json">{{ row.requestParams || '无' }}</pre>
              <template #reference>
                <span class="link-text">{{ row.requestParams ? '查看参数' : '无' }}</span>
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="请求体" min-width="200">
          <template #default="{ row }">
            <el-popover placement="top" width="400" trigger="hover">
              <pre class="log-json">{{ row.requestBody || '无' }}</pre>
              <template #reference>
                <span class="link-text">{{ row.requestBody ? '查看请求体' : '无' }}</span>
              </template>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" min-width="220">
          <template #default="{ row }">
            <span v-if="row.errorMessage" class="error-text">{{ row.errorMessage }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 50]"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Delete, Search, RefreshLeft } from '@element-plus/icons-vue'
import { getOperationLogs, cleanOperationLogs } from '@/api/operationLog'
import type { OperationLog, OperationLogQuery } from '@/types/operation-log'

const logs = ref<OperationLog[]>([])
const loading = ref(false)
const cleaning = ref(false)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const filters = reactive({
  requestUri: '',
  requestMethod: '',
  operatorName: '',
  businessType: '',
  status: undefined as number | undefined,
  dateRange: [] as string[]
})

const buildQuery = (): OperationLogQuery => {
  const query: OperationLogQuery = {
    page: pagination.page,
    pageSize: pagination.pageSize
  }

  if (filters.requestUri) {
    query.requestUri = filters.requestUri
  }
  if (filters.operatorName) {
    query.operatorName = filters.operatorName
  }
  if (filters.requestMethod) {
    query.requestMethod = filters.requestMethod
  }
  if (filters.businessType) {
    query.businessType = filters.businessType
  }
  if (filters.status !== undefined) {
    query.status = filters.status
  }
  if (filters.dateRange && filters.dateRange.length === 2) {
    query.startTime = filters.dateRange[0]
    query.endTime = filters.dateRange[1]
  }
  return query
}

const fetchLogs = async () => {
  try {
    loading.value = true
    const query = buildQuery()
    const data = await getOperationLogs(query)
    logs.value = data.items || []
    pagination.total = data.total || 0
    pagination.page = data.page || query.page || 1
    pagination.pageSize = data.limit || query.pageSize || 10
  } catch (error) {
    ElMessage.error('加载操作日志失败')
  } finally {
    loading.value = false
  }
}

const handleRefresh = () => {
  fetchLogs()
}

const resetFilters = () => {
  filters.requestUri = ''
  filters.requestMethod = ''
  filters.operatorName = ''
  filters.businessType = ''
  filters.status = undefined
  filters.dateRange = []
  pagination.page = 1
  fetchLogs()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  fetchLogs()
}

const handlePageSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.page = 1
  fetchLogs()
}

const handleClean = () => {
  ElMessageBox.confirm('确定要清空所有操作日志吗？该操作不可撤销。', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      cleaning.value = true
      await cleanOperationLogs()
      ElMessage.success('操作日志已清空')
      pagination.page = 1
      fetchLogs()
    } catch (error) {
      ElMessage.error('清空日志失败')
    } finally {
      cleaning.value = false
    }
  }).catch(() => {})
}

const methodTagType = (method: string) => {
  switch (method) {
    case 'POST':
      return 'success'
    case 'PUT':
    case 'PATCH':
      return 'warning'
    case 'DELETE':
      return 'danger'
    default:
      return 'info'
  }
}

const formatDuration = (duration?: number) => {
  if (!duration && duration !== 0) {
    return '-'
  }
  if (duration < 1000) {
    return `${duration} ms`
  }
  return `${(duration / 1000).toFixed(2)} s`
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.operation-log-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.page-desc {
  margin: 4px 0 0;
  color: #666;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-card {
  border-radius: 8px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.filter-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.link-text {
  color: #409eff;
  cursor: pointer;
}

.uri-text {
  word-break: break-all;
}

.log-json {
  max-height: 200px;
  overflow: auto;
  white-space: pre-wrap;
}

.error-text {
  color: #f56c6c;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}
</style>
