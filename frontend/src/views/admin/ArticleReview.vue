<template>
  <div class="article-review-page">
    <div class="page-header">
      <div>
        <h2>文章审核</h2>
        <p class="page-description">管理员需要审核文章后才能对外展示，可在此快速预览与审批。</p>
      </div>
      <div class="header-actions">
        <el-button :loading="loading" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <el-card class="filter-card">
      <div class="filters">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索标题 / 作者"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select v-model="filters.reviewStatus" class="status-select" @change="handleStatusChange">
          <el-option
            v-for="option in reviewStatusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
        <el-button @click="handleResetFilters">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="46" />

        <el-table-column label="标题 / 作者" min-width="280">
          <template #default="{ row }">
            <div class="title-cell">
              <span class="article-title">{{ row.title || '未命名文章' }}</span>
              <el-tag
                v-if="row.status === '0' || row.status === 0"
                size="small"
                effect="plain"
                type="info"
              >
                草稿
              </el-tag>
              <el-tag
                v-else-if="row.status === '2' || row.status === 2"
                size="small"
                effect="plain"
                type="warning"
              >
                已下线
              </el-tag>
            </div>
            <div class="article-meta">
              <span>作者：{{ row.authorName || row.author?.name || '未知作者' }}</span>
              <span>分类：{{ row.categoryName || '未分类' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="审核状态" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="getReviewStatusTag(row).type" effect="plain">
              {{ getReviewStatusTag(row).label }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="提交时间" prop="createdAt" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt || row.updatedAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" fixed="right" width="260">
          <template #default="{ row }">
            <el-button text type="primary" @click="handlePreview(row)">
              <el-icon><View /></el-icon>
              预览
            </el-button>
            <el-button
              text
              type="success"
              :disabled="!canOperate(row)"
              @click="handleApprove(row)"
            >
              <el-icon><CircleCheck /></el-icon>
              通过
            </el-button>
            <el-button
              text
              type="danger"
              :disabled="!canOperate(row)"
              @click="handleReject(row)"
            >
              <el-icon><Close /></el-icon>
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <div class="footer-left">
          <el-button
            text
            type="success"
            :disabled="!canBatchApprove"
            @click="handleBatchApprove"
          >
            <el-icon><CircleCheck /></el-icon>
            批量通过
          </el-button>
          <el-button
            text
            type="danger"
            :disabled="!canBatchApprove"
            @click="handleBatchReject"
          >
            <el-icon><Close /></el-icon>
            批量驳回
          </el-button>
        </div>
        <el-pagination
          class="pagination"
          background
          layout="total, prev, pager, next, sizes"
          :total="pagination.total"
          :page-size="pagination.pageSize"
          :current-page="pagination.pageNum"
          :page-sizes="[10, 20, 30, 50]"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, CircleCheck, Close } from '@element-plus/icons-vue'
import { articleApi } from '@/api/article'
import { ArticleReviewStatus } from '@/types/article'
import { usePermission } from '@/composables/usePermission'

interface ReviewArticle {
  id: string | number
  title?: string
  authorName?: string
  categoryName?: string
  status?: number | string
  reviewStatus?: number
  reviewStatusLabel?: string
  pendingReview?: boolean
  reviewApproved?: boolean
  reviewRejected?: boolean
  createdAt?: string
  updatedAt?: string
}

const router = useRouter()
const { hasPermission } = usePermission()

const loading = ref(false)
const tableData = ref<ReviewArticle[]>([])
const selection = ref<ReviewArticle[]>([])
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const filters = reactive({
  keyword: '',
  reviewStatus: ArticleReviewStatus.PENDING
})

const reviewStatusOptions = [
  { label: '待审核', value: ArticleReviewStatus.PENDING },
  { label: '已通过', value: ArticleReviewStatus.APPROVED },
  { label: '未通过', value: ArticleReviewStatus.REJECTED }
]

const canReview = computed(() => hasPermission('article:article:review'))

const canBatchApprove = computed(
  () =>
    canReview.value &&
    selection.value.length > 0 &&
    selection.value.every(item => item.pendingReview || item.reviewStatus === ArticleReviewStatus.PENDING)
)

const getReviewStatusTag = (article: ReviewArticle) => {
  const status = article.reviewStatus ?? ArticleReviewStatus.PENDING
  if (status === ArticleReviewStatus.APPROVED) {
    return { label: '已通过', type: 'success' as const }
  }
  if (status === ArticleReviewStatus.REJECTED) {
    return { label: '未通过', type: 'danger' as const }
  }
  return { label: '待审核', type: 'warning' as const }
}

const formatDate = (value?: string) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString()
}

const canOperate = (row: ReviewArticle) => {
  if (!canReview.value) {
    return false
  }
  return row.pendingReview || row.reviewStatus === ArticleReviewStatus.PENDING
}

const parseListResponse = (payload: any): { items: ReviewArticle[]; total: number } => {
  if (!payload) {
    return { items: [], total: 0 }
  }
  const data = payload.data ?? payload
  const items = data.items ?? data.rows ?? data.list ?? data.records ?? []
  const total = data.total ?? data.totalCount ?? data.count ?? items.length
  return { items, total }
}

const fetchReviews = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: filters.keyword.trim() || undefined,
      reviewStatus: filters.reviewStatus
    }
    const response = await articleApi.getReviewArticles(params)
    const { items, total } = parseListResponse(response)
    tableData.value = items
    pagination.total = total
  } catch (error) {
    console.error('加载审核列表失败:', error)
    tableData.value = []
    pagination.total = 0
    ElMessage.error('加载审核列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchReviews()
}

const handleStatusChange = () => {
  pagination.pageNum = 1
  fetchReviews()
}

const handleResetFilters = () => {
  filters.keyword = ''
  filters.reviewStatus = ArticleReviewStatus.PENDING
  handleSearch()
}

const handleRefresh = () => {
  fetchReviews()
}

const handleSelectionChange = (rows: ReviewArticle[]) => {
  selection.value = rows
}

const handlePageChange = (page: number) => {
  pagination.pageNum = page
  fetchReviews()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchReviews()
}

const handlePreview = (article: ReviewArticle) => {
  if (!article?.id) return
  const url = router.resolve({ name: 'ArticleReader', params: { id: article.id } }).href
  window.open(url, '_blank', 'noopener=yes')
}

const confirmAction = (message: string) => {
  return ElMessageBox.confirm(message, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
}

const approveArticle = async (articleId: string | number) => {
  await articleApi.approveArticleReview(articleId)
}

const rejectArticle = async (articleId: string | number) => {
  await articleApi.rejectArticleReview(articleId)
}

const handleApprove = async (row: ReviewArticle) => {
  if (!row?.id) return
  try {
    await confirmAction('确认通过该文章的审核吗？')
    await approveArticle(row.id)
    ElMessage.success('审核通过')
    fetchReviews()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核通过失败:', error)
      ElMessage.error('审核通过失败')
    }
  }
}

const handleReject = async (row: ReviewArticle) => {
  if (!row?.id) return
  try {
    await confirmAction('确认驳回该文章吗？文章将返回草稿状态。')
    await rejectArticle(row.id)
    ElMessage.success('已驳回文章')
    fetchReviews()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('驳回文章失败:', error)
      ElMessage.error('驳回文章失败')
    }
  }
}

const handleBatchApprove = async () => {
  if (selection.value.length === 0) {
    return
  }
  try {
    await confirmAction(`确认批量通过 ${selection.value.length} 篇文章吗？`)
    await Promise.all(selection.value.map(item => approveArticle(item.id)))
    ElMessage.success('批量审核通过')
    fetchReviews()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量审核失败:', error)
      ElMessage.error('批量审核失败')
    }
  }
}

const handleBatchReject = async () => {
  if (selection.value.length === 0) {
    return
  }
  try {
    await confirmAction(`确认批量驳回 ${selection.value.length} 篇文章吗？`)
    await Promise.all(selection.value.map(item => rejectArticle(item.id)))
    ElMessage.success('批量驳回成功')
    fetchReviews()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量驳回失败:', error)
      ElMessage.error('批量驳回失败')
    }
  }
}

onMounted(() => {
  fetchReviews()
})
</script>

<style scoped>
.article-review-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.page-description {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-card {
  padding-bottom: 4px;
}

.filters {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filters .el-input {
  width: 260px;
}

.status-select {
  width: 160px;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #111827;
}

.article-title {
  max-width: 220px;
  display: inline-block;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.article-meta {
  margin-top: 4px;
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #6b7280;
}

.table-footer {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination {
  margin-left: auto;
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
    align-items: stretch;
  }

  .filters .el-input,
  .status-select {
    width: 100%;
  }

  .article-meta {
    flex-direction: column;
    gap: 2px;
  }

  .table-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .pagination {
    width: 100%;
    justify-content: center;
  }
}
</style>
