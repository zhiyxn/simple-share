<template>
  <div class="admin-articles">
    <!-- 椤甸潰澶撮儴 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <AdminEditorToolbar
            v-if="showEditor"
            :article-id="editingArticleId"
            :status="editingArticle?.status || '0'"
            :updated-at="editingArticle?.updatedAt"
            :publishing="saving"
            :is-published="isPublishedStatus(editingArticle?.status)"
            :show-back-button="true"
            :show-status="true"
            :show-preview-button="canPreviewArticle"
            :show-save-draft-button="canSaveDraft"
            :show-publish-button="canPublishArticle"
            back-button-text="返回列表"
            :categories="categories"
            :fetch-editor-data="getEditorSnapshot"
            :apply-editor-settings="applyEditorSettings"
            :ensure-draft-saved="ensureDraftSaved"
            :get-publish-payload="getPublishPayload"
            :get-article-id="getArticleId"
            @back="handleBackToList"
            @preview="handlePreview"
            @save-draft="handleSaveDraft"
            @publish="handleToolbarPublish"
            @publish-success="handlePublishSuccess"
          />
        </div>
        
        <div class="header-right" v-if="!showEditor">
          <el-button
            v-if="hasPermission('article:article:add')"
            type="primary"
            @click="handleCreateArticle"
          >
            <el-icon><Plus /></el-icon>
            新建文章
          </el-button>
        </div>
      </div>
    </div>

    <!-- 文章列表视图 -->
    <div v-if="!showEditor" class="articles-list-view">
      <!-- 筛选和搜索 -->
      <div class="filter-section">
        <div class="filter-content">
          <div class="filter-left">
            <el-input
              v-model="searchQuery"
              placeholder="搜索文章标题、内容..."
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
              placeholder="状态筛选"
              style="width: 120px"
              clearable
              @change="handleFilter"
            >
              <el-option label="全部" value="" />
              <el-option label="已发布" value="1" />
              <el-option label="草稿" value="0" />
            </el-select>
            
            <el-select
              v-model="filterCategory"
              placeholder="分类筛选"
              style="width: 120px"
              clearable
              @change="handleFilter"
            >
              <el-option label="全部分类" value="" />
              <el-option
                v-for="category in categories"
                :key="getCategoryValue(category)"
                :label="formatCategoryLabel(category)"
                :value="getCategoryValue(category)"
                :title="formatCategoryLabel(category)"
              />
            </el-select>
            
            <el-select
              v-model="filterRecommend"
              placeholder="推荐筛选"
              style="width: 120px"
              clearable
              @change="handleFilter"
            >
              <el-option label="全部" value="" />
              <el-option label="推荐" value="1" />
              <el-option label="非推荐" value="0" />
            </el-select>
            
            <el-select
              v-model="filterTop"
              placeholder="置顶筛选"
              style="width: 120px"
              clearable
              @change="handleFilter"
            >
              <el-option label="全部" value="" />
              <el-option label="置顶" value="1" />
              <el-option label="非置顶" value="0" />
            </el-select>
            
            <el-select
              v-model="filterAccessLevel"
              placeholder="访问级别"
              style="width: 120px"
              clearable
              @change="handleFilter"
            >
              <el-option label="全部" value="" />
              <el-option label="公开" value="0" />
              <el-option label="会员" value="1" />
              <el-option label="仅自己" value="2" />
              <el-option label="高级会员" value="3" />
              <el-option label="VIP" value="4" />
            </el-select>
          </div>
          
          <div class="filter-right">
            <el-dropdown v-if="showBatchActions" @command="handleBatchAction">
              <el-button>
                批量操作
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-if="hasPermission('article:article:publish')"
                    command="publish"
                  >
                    批量发布
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="hasPermission('article:article:edit')"
                    command="draft"
                  >
                    批量转为草稿
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="hasPermission('article:article:remove')"
                    command="delete"
                    divided
                  >
                    批量删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <!-- 文章表格 -->
      <div class="articles-table">
        <el-table
          v-loading="loading"
          :data="articles"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          
          <el-table-column label="标题" min-width="200">
            <template #default="{ row }">
              <div class="article-title-cell">
                <h3 class="article-title" @click="handleViewArticle(row)">
                  {{ row.title || '无标题' }}
                </h3>
                <div class="article-meta">
                  <el-tag
                    :type="getStatusType(row.status)"
                    size="small"
                  >
                    {{ getStatusText(row.status) }}
                  </el-tag>
                  <el-tag
                    v-if="row.isRecommend === 1 || row.isRecommend === '1'"
                    type="warning"
                    size="small"
                  >
                    推荐
                  </el-tag>
                  <el-tag
                    v-if="row.isTop === 1 || row.isTop === '1'"
                    type="success"
                    size="small"
                  >
                    置顶
                  </el-tag>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="分类" width="120">
            <template #default="{ row }">
              <span>{{ getCategoryName(row.categoryId) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="作者" width="100">
            <template #default="{ row }">
              <span>{{ row.authorName || '未知' }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="发布时间" width="180">
            <template #default="{ row }">
              <span>{{ formatDate(row.publishTime || row.createTime) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="浏览量" width="100">
            <template #default="{ row }">
              <span>{{ row.viewCount || 0 }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="推荐" width="80" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.recommend"
                @change="(value) => handleToggleRecommend(row, value)"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="置顶" width="80" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.top"
                @change="(value) => handleToggleTop(row, value)"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="允许复制" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.allowCopy"
                @change="(value) => handleToggleAllowCopy(row, value)"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="访问级别" width="100">
            <template #default="{ row }">
              <el-tag
                :type="getAccessLevelType(row.accessLevel)"
                size="small"
              >
                {{ getAccessLevelText(row.accessLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                  <el-button
                    v-if="hasPermission('article:article:edit')"
                    type="primary"
                    size="small"
                    @click="handleEditArticle(row)"
                  >
                  编辑
                </el-button>
                  <el-button
                    v-if="hasPermission('article:article:query')"
                    size="small"
                    @click="handleViewArticle(row)"
                  >
                  查看
                </el-button>
                <el-dropdown
                  v-if="showMoreActions"
                  @command="(command) => handleActionCommand(command, row)"
                  trigger="click"
                >
                  <el-button size="small">
                    更多
                    <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item 
                          v-if="isDraftStatus(row.status) && hasPermission('article:article:publish')"
                          command="publish"
                          icon="Upload"
                        >
                        发布
                      </el-dropdown-item>
                        <el-dropdown-item 
                          v-if="isPublishedStatus(row.status) && hasPermission('article:article:offline')"
                          command="unpublish"
                          icon="Download"
                        >
                        下线
                      </el-dropdown-item>
                        <el-dropdown-item 
                          v-if="hasPermission('article:article:remove')"
                          command="delete"
                          icon="Delete"
                          divided
                      >
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-wrapper">
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
      </div>
    </div>

    <!-- 文章编辑器视图 - 使用PostEditor组件 -->
    <div v-if="showEditor" class="article-editor-view">
      <PostEditor
        ref="postEditorRef"
        :embedded="true"
        :hide-toolbar="true"
        :id="editingArticleId"
        :initial="editingArticle"
        @close="handleBackToList"
        @saved="handleArticleSaved"
        @published="handleArticlePublished"
      />
    </div>

    <el-dialog
      v-model="previewVisible"
      width="80%"
      class="article-preview-dialog"
      destroy-on-close
      :title="previewArticle?.title || '文章预览'"
      @closed="handlePreviewClosed"
    >
      <div v-if="previewLoading" class="preview-loading">
        <el-skeleton :rows="6" animated />
      </div>
      <div v-else-if="previewArticle" class="article-preview-body">
        <h2 class="article-preview-title">
          {{ previewArticle.title || '无标题' }}
        </h2>
        <div class="article-preview-meta">
          <span>分类：{{ getPreviewCategoryName(previewArticle) }}</span>
          <span>状态：{{ getStatusText(String(previewArticle.status || '')) }}</span>
          <span v-if="previewArticle.updatedAt || previewArticle.createdAt">
            最近更新：{{ formatDate(previewArticle.updatedAt || previewArticle.createdAt) }}
          </span>
        </div>
        <div class="article-preview-summary" v-if="previewArticle.summary">
          {{ previewArticle.summary }}
        </div>
        <div
          class="article-preview-tags"
          v-if="Array.isArray(previewArticle.tags) && previewArticle.tags.length"
        >
          <el-tag
            v-for="tag in previewArticle.tags"
            :key="tag"
            size="small"
            class="article-preview-tag"
          >
            {{ tag }}
          </el-tag>
        </div>
        <div
          v-if="previewArticle.content"
          class="article-preview-content"
          v-html="previewArticle.content"
        />
        <div v-else class="article-preview-empty">
          <el-empty description="暂无内容" />
        </div>
      </div>
      <div v-else class="preview-empty">
        <el-empty description="暂无预览内容" />
      </div>

      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToEditFromPreview" v-if="previewArticle?.id">
          编辑
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  ArrowDown,
} from '@element-plus/icons-vue'
import { articleApi } from '@/api/article'
import { usePublicCategoryStore } from '@/stores/publicCategory'
import PostEditor from '@/views/editor/PostEditor.vue'
import AdminEditorToolbar from '@/components/editor/components/toolbar/AdminEditorToolbar.vue'
import type { Article, Category } from '@/types'
import { usePermission } from '@/composables/usePermission'

type PostEditorInstance = InstanceType<typeof PostEditor>

type PreviewArticle = Partial<Article> & {
  categoryName?: string | null
  categoryId?: string | number | null
  tags?: string[] | null
}

// 路由
const route = useRoute()
const router = useRouter()
const categoryStore = usePublicCategoryStore()
const { hasPermission } = usePermission()

const canPreviewArticle = computed(() => hasPermission('article:article:query'))
const canSaveDraft = computed(() => hasPermission(['article:article:add', 'article:article:edit']))
const canPublishArticle = computed(() => hasPermission('article:article:publish'))
const canPublishDraft = computed(() => hasPermission('article:article:publish'))
const canUnpublishArticle = computed(() => hasPermission('article:article:offline'))
const canDeleteArticle = computed(() => hasPermission('article:article:remove'))
const showMoreActions = computed(
  () => canPublishDraft.value || canUnpublishArticle.value || canDeleteArticle.value
)
const showBatchActions = computed(
  () => canPublishDraft.value || canSaveDraft.value || canDeleteArticle.value
)

// 响应式数据
const postEditorRef = ref<PostEditorInstance | null>(null)
const loading = ref(false)
const saving = ref(false)
const showEditor = ref(false)
const editingArticle = ref<Article | null>(null)
const editingArticleId = ref<string | null>(null)
const previewVisible = ref(false)
const previewLoading = ref(false)
const previewArticle = ref<PreviewArticle | null>(null)

// 文章列表数据
const articles = ref<Article[]>([])
const selectedArticles = ref<Article[]>([])
const categories = ref<Category[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

// 筛选条件
const searchQuery = ref('')
const filterStatus = ref('')
const filterCategory = ref('')
const filterRecommend = ref('')
const filterTop = ref('')
const filterAccessLevel = ref('')

// 计算属性
const getCategoryName = (categoryId: number | string) => {
  if (categoryId === undefined || categoryId === null || categoryId === '') {
    return '未分类'
  }
  const targetId = categoryId.toString()
  const category = categories.value.find(
    c => c.id === targetId || c.categoryId?.toString() === targetId
  )
  return category?.name || '未分类'
}

const formatCategoryLabel = (category: Category) => {
  if (!category) {
    return ''
  }
  const name = category.name || category.categoryName || ''
  const remark = (category as Partial<{ remark?: string }>).remark
  const description = category.description || remark || ''
  return description ? `${name}（${description}）` : name
}

const getPreviewCategoryName = (article?: PreviewArticle | null) => {
  if (!article) {
    return '未分类'
  }
  if (article.categoryName) {
    return article.categoryName
  }
  if (article.category?.name) {
    return article.category.name
  }
  if (article.categoryId) {
    return getCategoryName(article.categoryId)
  }
  return '未分类'
}

const getCategoryValue = (category: Category) => {
  if (!category) {
    return ''
  }
  if (category.id) {
    return category.id.toString()
  }
  const fallback = category.categoryId
  return fallback !== undefined && fallback !== null ? fallback.toString() : ''
}

const getStatusType = (status: string) => {
  const normalized = (status ?? '').toString().toLowerCase()
  const typeMap: Record<string, string> = {
    '1': 'success',  // 已发布
    '0': 'info',     // 草稿
  }
  if (normalized === 'published') {
    return typeMap['1']
  }
  if (normalized === 'draft') {
    return typeMap['0']
  }
  if (normalized === 'private') {
    return typeMap['1']
  }
  if (normalized === 'archived' || normalized === 'offline') {
    return 'warning'
  }

  return typeMap[status] || typeMap[normalized] || 'info'
}

const getStatusText = (status: string) => {
  const normalized = (status ?? '').toString().toLowerCase()
  const textMap: Record<string, string> = {
    '1': '已发布',
    'published': '已发布',
    '0': '草稿',
    'draft': '草稿',
    'archived': '已下线',
    'offline': '已下线'
  }
  return textMap[normalized] || textMap[status] || '未知'
}

const isPublishedStatus = (status?: string | null) => {
  const normalized = (status ?? '').toString().toLowerCase()
  return normalized === '1' || normalized === 'published'
}

const isDraftStatus = (status?: string | null) => {
  const normalized = (status ?? '').toString().toLowerCase()
  return normalized === '0' || normalized === 'draft'
}

const formatDate = (date: string | Date) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const sanitizeArticleContent = (html: string) => {
  if (!html || typeof DOMParser === 'undefined') {
    return html
  }

  try {
    const parser = new DOMParser()
    const doc = parser.parseFromString(html, 'text/html')

    doc.querySelectorAll('img').forEach((img) => {
      const src = img.getAttribute('src') ?? ''
      const isPlaceholder = /placeholder/i.test(src)

      if (!src || src.length > 1024 * 64 || isPlaceholder) {
        img.removeAttribute('src')
        img.setAttribute('alt', img.getAttribute('alt') || 'unavailable image')
        img.setAttribute(
          'data-placeholder',
          'image removed to prevent remote placeholder fetching'
        )
      } else {
        img.setAttribute('loading', 'lazy')
        img.setAttribute('referrerpolicy', 'no-referrer')
        img.style.maxWidth = '100%'
        img.style.height = 'auto'
      }
    })

    return doc.body.innerHTML
  } catch (error) {
    console.warn('Failed to sanitize article content', error)
    return html
  }
}

const getEditorSnapshot = () => {
  const snapshot = postEditorRef.value?.getPostSnapshot?.() ?? null
  if (!snapshot) {
    return null
  }

  if (snapshot.content) {
    snapshot.content = sanitizeArticleContent(snapshot.content)
  }

  return snapshot
}

const applyEditorSettings = (settings: Record<string, unknown>) => {
  postEditorRef.value?.applyToolbarSettings?.(settings)
}

const ensureDraftSaved = async () => {
  if (!postEditorRef.value?.saveDraftForPublish) return undefined
  saving.value = true
  try {
    return await postEditorRef.value.saveDraftForPublish()
  } finally {
    saving.value = false
  }
}

const getPublishPayload = () => postEditorRef.value?.getPublishPayload?.() ?? null

// 访问级别相关函数
const getAccessLevelText = (level: number | string) => {
  const levelNum = typeof level === 'string' ? parseInt(level) : level
  const levelMap: Record<number, string> = {
    0: '公开',
    1: '会员',
    2: '仅自己',
    3: '高级会员',
    4: 'VIP',
    5: '管理员'
  }
  return levelMap[levelNum] || '未知'
}

const getAccessLevelType = (level: number | string) => {
  const levelNum = typeof level === 'string' ? parseInt(level) : level
  const typeMap: Record<number, string> = {
    0: 'success',    // 公开 - 绿色
    1: 'primary',    // 会员 - 蓝色
    2: 'warning',    // 仅自己 - 橙色
    3: 'info',       // 高级会员 - 灰色
    4: 'danger',     // VIP - 红色
    5: 'danger'      // 管理员 - 红色
  }
  return typeMap[levelNum] || 'info'
}

const getArticleId = () => postEditorRef.value?.getCurrentArticleId?.() ?? null

const handleToolbarPublish = () => {
  console.log('准备发布文章（工具栏）')
}

// 鏂规硶瀹氫箟
const loadArticles = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchQuery.value || undefined,
      status: filterStatus.value || undefined,
      categoryId: filterCategory.value || undefined,
      accessLevel: filterAccessLevel.value || undefined,
      isRecommend: filterRecommend.value || undefined,
      isTop: filterTop.value || undefined
    }
    
    const response = await articleApi.getAdminArticles(params)
    console.log('API响应:', response)
    
    if (response && (response as any).data) {
      // 处理不同的响应格式：items 或 rows
      articles.value = (response as any).data.items || (response as any).data.rows || []
      total.value = (response as any).data.total || 0
    } else if (response && (response as any).items) {
      // 直接返回的格式
      articles.value = (response as any).items || []
      total.value = (response as any).total || 0
    } else if (response && (response as any).rows) {
      // 另一种直接返回的格式
      articles.value = (response as any).rows || []
      total.value = (response as any).total || 0
    } else {
      articles.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载文章列表失败:', error)
    ElMessage.error('加载文章列表失败')
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const list = await categoryStore.fetchCategories()
    if (Array.isArray(list)) {
      categories.value = [...list].sort((a, b) => {
        const aSort = a.sort ?? a.orderNum ?? 0
        const bSort = b.sort ?? b.orderNum ?? 0
        return aSort - bSort
      })
    } else {
      categories.value = []
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadArticleFromRoute = async () => {
  const routePath = route.path
  
  if (routePath === '/admin/articles/create') {
    // 新建文章
    showEditor.value = true
    editingArticle.value = null
    editingArticleId.value = null
    previewVisible.value = false
  } else if (routePath.includes('/admin/articles/') && routePath.includes('/edit')) {
    // 编辑文章
    const articleIdParam = route.params.id
    const articleId = Array.isArray(articleIdParam) ? articleIdParam[0] : articleIdParam
    if (articleId) {
      try {
        const detail = await articleApi.getArticleForEdit(articleId as string)
        editingArticle.value = detail
        editingArticleId.value = detail?.id ?? String(articleId)
        showEditor.value = true
      } catch (error) {
        console.error('加载文章失败:', error)
        ElMessage.error('加载文章失败')
        showEditor.value = false
        editingArticle.value = null
        editingArticleId.value = null
        router.push('/admin/articles')
      }
    }
    previewVisible.value = false
  } else {
    // 文章列表
    showEditor.value = false
    editingArticle.value = null
    editingArticleId.value = null
    previewVisible.value = false
  }
}

// 监听路由变化
watch(() => route.path, () => {
  loadArticleFromRoute()
}, { immediate: true })

const handleCreateArticle = () => {
  if (!hasPermission('article:article:add')) {
    ElMessage.warning('暂无文章创建权限')
    return
  }
  editingArticle.value = null
  editingArticleId.value = null
  showEditor.value = true
  router.push('/admin/articles/create')
}

const handleEditArticle = async (article: Article) => {
  if (!hasPermission(['article:article:edit', 'article:article:query'])) {
    ElMessage.warning('暂无文章编辑权限')
    return
  }
  const targetId = article?.id
  if (!targetId) {
    ElMessage.warning('未找到文章ID')
    return
  }
  try {
    loading.value = true
    const detail = await articleApi.getArticleForEdit(targetId)
    editingArticle.value = detail
    editingArticleId.value = detail?.id ?? targetId
    showEditor.value = true
    previewVisible.value = false
    router.push(`/admin/articles/${targetId}/edit`)
  } catch (error) {
    console.error('加载文章详情失败:', error)
    ElMessage.error('加载文章详情失败')
  } finally {
    loading.value = false
  }
}

const handleViewArticle = async (article: Article) => {
  const targetId = article?.id
  if (!targetId) {
    ElMessage.warning('未找到文章ID')
    return
  }
  previewVisible.value = true
  previewLoading.value = true
  previewArticle.value = null
  try {
    const detail = await articleApi.getArticleForEdit(targetId)
    previewArticle.value = detail
  } catch (error) {
    console.error('加载文章预览失败:', error)
    previewVisible.value = false
    ElMessage.error('加载文章预览失败')
  } finally {
    previewLoading.value = false
  }
}

const handlePreviewClosed = () => {
  previewArticle.value = null
  previewLoading.value = false
}

const goToEditFromPreview = () => {
  if (!previewArticle.value?.id) {
    return
  }
  const targetId = String(previewArticle.value.id)
  previewVisible.value = false
  router.push(`/admin/articles/${targetId}/edit`)
}

const handleBackToList = () => {
  showEditor.value = false
  editingArticle.value = null
  editingArticleId.value = null
  router.push('/admin/articles')
  loadArticles()
}

const handleArticleSaved = () => {
  handleBackToList()
}

const handleArticlePublished = () => {
  handleBackToList()
}

const handlePublishArticle = async (article: Article) => {
  try {
    await ElMessageBox.confirm('确定要发布这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await articleApi.publishArticle(article.id)
    loadArticles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
      ElMessage.error('发布失败')
    }
  }
}

const handleUnpublishArticle = async (article: Article) => {
  try {
    await ElMessageBox.confirm('确定要下线这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await articleApi.unpublishArticle(article.id)
    loadArticles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下线失败:', error)
      ElMessage.error('下线失败')
    }
  }
}

const handleDeleteArticle = async (article: Article) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？删除后无法恢复！', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    await articleApi.deleteArticle(article.id)
    loadArticles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadArticles()
}

const handleFilter = () => {
  currentPage.value = 1
  loadArticles()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadArticles()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadArticles()
}

const handleSelectionChange = (selection: Article[]) => {
  selectedArticles.value = selection
}

const handleBatchAction = async (command: string) => {
  if (selectedArticles.value.length === 0) {
    ElMessage.warning('请先选择要操作的文章')
    return
  }

  if (command === 'publish' && !hasPermission('article:article:publish')) {
    ElMessage.warning('暂无文章发布权限')
    return
  }
  if (command === 'draft' && !hasPermission(['article:article:add', 'article:article:edit'])) {
    ElMessage.warning('暂无文章编辑权限')
    return
  }
  if (command === 'delete' && !hasPermission('article:article:remove')) {
    ElMessage.warning('暂无文章删除权限')
    return
  }
  
  try {
    let confirmText = ''
    let successText = ''
    
    switch (command) {
      case 'publish':
        confirmText = `确定要批量发布 ${selectedArticles.value.length} 篇文章吗？`
        successText = '批量发布成功'
        break
      case 'draft':
        confirmText = `确定要将 ${selectedArticles.value.length} 篇文章转为草稿吗？`
        successText = '批量转为草稿成功'
        break
      case 'delete':
        confirmText = `确定要删除 ${selectedArticles.value.length} 篇文章吗？删除后无法恢复！`
        successText = '批量删除成功'
        break
    }
    
    await ElMessageBox.confirm(confirmText, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: command === 'delete' ? 'error' : 'warning'
    })
    
    const articleIds = selectedArticles.value.map(article => article.id)
    
    switch (command) {
      case 'publish':
        await articleApi.batchPublishArticles(articleIds)
        break
      case 'draft':
        await articleApi.batchDraftArticles(articleIds)
        break
      case 'delete':
        await articleApi.batchDeleteArticles(articleIds)
        break
    }
    
    loadArticles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量操作失败:', error)
      ElMessage.error('批量操作失败')
    }
  }
}

// 处理操作下拉菜单命令
const handleActionCommand = async (command: string, row: Article) => {
  const permissionMap: Record<string, string | string[]> = {
    publish: 'article:article:publish',
    unpublish: 'article:article:offline',
    copy: ['article:article:add', 'article:article:edit'],
    recommend: 'article:article:recommend',
    unrecommend: 'article:article:recommend',
    top: 'article:article:top',
    untop: 'article:article:top',
    'access-level': ['article:article:edit', 'article:article:query'],
    delete: 'article:article:remove'
  }

  const required = permissionMap[command]
  if (required && !hasPermission(required)) {
    ElMessage.warning('暂无执行该操作的权限')
    return
  }

  try {
    switch (command) {
      case 'publish':
        await handlePublishArticle(row)
        break
      case 'unpublish':
        await handleUnpublishArticle(row)
        break
      case 'copy':
        await handleCopyArticle(row)
        break
      case 'recommend':
        await handleToggleRecommend(row, true)
        break
      case 'unrecommend':
        await handleToggleRecommend(row, false)
        break
      case 'top':
        await handleToggleTop(row, true)
        break
      case 'untop':
        await handleToggleTop(row, false)
        break
      case 'access-level':
        await handleChangeAccessLevel(row)
        break
      case 'delete':
        await handleDeleteArticle(row)
        break
      default:
        console.warn('未知的操作命令:', command)
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 复制文章
const handleCopyArticle = async (article: Article) => {
  try {
    await ElMessageBox.confirm('确定要复制这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    // 调用复制文章的API
    await articleApi.copyArticle(String(article.id))
    ElMessage.success('文章复制成功')
    loadArticles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('复制文章失败:', error)
      ElMessage.error('复制文章失败')
    }
  }
}

// 切换推荐状态
const handleToggleRecommend = async (article: Article, isRecommend: boolean) => {
  try {
    const action = isRecommend ? '推荐' : '取消推荐'
    
    // 调用切换推荐状态的API
    await articleApi.toggleRecommend(String(article.id), isRecommend)
    ElMessage.success(`${action}成功`)
    // 不需要重新加载，因为数据已经通过v-model更新了
  } catch (error) {
    console.error('切换推荐状态失败:', error)
    ElMessage.error('操作失败')
    // 如果API调用失败，恢复原来的状态
    article.recommend = !isRecommend
  }
}

// 切换置顶状态
const handleToggleTop = async (article: Article, isTop: boolean) => {
  try {
    const action = isTop ? '置顶' : '取消置顶'
    
    // 调用切换置顶状态的API
    await articleApi.toggleTop(String(article.id), isTop)
    ElMessage.success(`${action}成功`)
    // 不需要重新加载，因为数据已经通过v-model更新了
  } catch (error) {
    console.error('切换置顶状态失败:', error)
    ElMessage.error('操作失败')
    // 如果API调用失败，恢复原来的状态
    article.top = !isTop
  }
}

// 修改访问级别
const handleChangeAccessLevel = async (article: Article) => {
  try {
    const { value: accessLevel } = await ElMessageBox.prompt('请输入新的访问级别', '修改访问级别', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^[0-9]+$/,
      inputErrorMessage: '访问级别必须是数字'
    })
    
    // 这里需要调用修改访问级别的API
    // await articleApi.changeAccessLevel(article.id, parseInt(accessLevel))
    ElMessage.success('访问级别修改成功')
    loadArticles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('修改访问级别失败:', error)
      ElMessage.error('修改访问级别失败')
    }
  }
}

// 切换允许复制状态
const handleToggleAllowCopy = async (article: Article, allowCopy: boolean) => {
  try {
    const action = allowCopy ? '允许复制' : '禁止复制'
    
    // 调用更新允许复制状态的API
    await articleApi.updateAllowCopy(String(article.id), allowCopy)
    ElMessage.success(`${action}设置成功`)
    // 不需要重新加载，因为数据已经通过v-model更新了
  } catch (error) {
    console.error('切换允许复制状态失败:', error)
    ElMessage.error('操作失败')
    // 如果API调用失败，恢复原来的状态
    article.allowCopy = !allowCopy
  }
}

// EditorToolbar 浜嬩欢澶勭悊鏂规硶
const handlePreview = () => {
  // 预览功能 - 可以打开新窗口或模态框显示预览
  console.log('预览文章')
}

const handleSaveDraft = async () => {
  // 保存草稿功能 - 可以调用 PostEditor 的保存方法
  console.log('保存草稿')
}

const handlePublishSuccess = async ({ id }: { id: string }) => {
  if (editingArticle.value && id === (editingArticleId.value ?? editingArticle.value.id)) {
    editingArticle.value = {
      ...editingArticle.value,
      status: '1' as any
    } as Article
  }

  await loadArticles()
  // 发布成功后返回文章列表
  handleBackToList()
}

// 组件挂载时加载数据
onMounted(() => {
  loadCategories()
  if (!showEditor.value) {
    loadArticles()
  }
})
</script>

<style scoped>
.admin-articles {
  display: flex;
  flex-direction: column;
  margin: -24px;
  min-height: calc(100vh - 64px);
}

.page-header {
  background: white;
  border-bottom: 1px solid #e4e7ed;
  padding: 20px 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-description {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.articles-list-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.filter-section {
  background: white;
  border-bottom: 1px solid #e4e7ed;
  padding: 16px 24px;
}

.filter-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.articles-table {
  flex: 1;
  background: white;
  padding: 24px;
  overflow: auto;
}

.article-title-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.article-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 0;
}

.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.article-title {
  cursor: pointer;
  transition: color 0.2s;
}

.article-title:hover {
  color: #409eff;
}

.article-meta {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e4e7ed;
}

.article-editor-view {
  flex: 1;
  background: white;
  overflow: auto;
}

.article-preview-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.preview-loading,
.preview-empty,
.article-preview-empty {
  padding: 32px 0;
}

.article-preview-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-preview-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.article-preview-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 14px;
  color: #909399;
}

.article-preview-summary {
  font-size: 15px;
  color: #606266;
  line-height: 1.6;
}

.article-preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.article-preview-tag {
  margin: 0 !important;
}

.article-preview-content {
  line-height: 1.8;
  color: #303133;
}

.article-preview-content :deep(img) {
  max-width: 100%;
  height: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .filter-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .filter-left {
    flex-wrap: wrap;
  }
  
  .articles-table {
    padding: 16px;
  }
}
</style>
