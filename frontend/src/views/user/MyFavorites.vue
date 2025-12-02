<template>
  <div class="my-favorites">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Star /></el-icon>
          我的收藏
        </h1>
        <p class="page-description">管理您收藏的文章</p>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-toolbar">
      <div class="filter-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索收藏的文章..."
          class="search-input"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div class="filter-right">
        <el-select
          v-model="sortBy"
          placeholder="排序方式"
          class="sort-select"
          @change="handleSort"
        >
          <el-option label="收藏时间" value="created_at" />
          <el-option label="文章标题" value="title" />
          <el-option label="作者" value="author" />
        </el-select>
        <el-button
          type="danger"
          :disabled="selectedFavorites.length === 0"
          @click="handleBatchRemove"
        >
          <el-icon><Delete /></el-icon>
          批量移除
        </el-button>
      </div>
    </div>

    <!-- 收藏列表 -->
    <div class="favorites-list" v-loading="loading">
      <div v-if="favorites.length === 0" class="empty-state">
        <el-empty description="暂无收藏的文章">
          <el-button type="primary" @click="$router.push('/articles')">
            去浏览文章
          </el-button>
        </el-empty>
      </div>
      <div v-else class="favorites-grid">
        <div
          v-for="favorite in favorites"
          :key="favorite.id"
          class="favorite-card"
          :class="{ selected: selectedFavorites.includes(favorite.id) }"
        >
          <el-checkbox
            v-model="selectedFavorites"
            :value="favorite.id"
            class="card-checkbox"
          />
          <div class="card-content" @click="viewArticle(favorite.article)">
            <div class="article-cover" v-if="favorite.article.cover">
              <img :src="favorite.article.cover" :alt="favorite.article.title" />
            </div>
            <div class="article-info">
              <h3 class="article-title">{{ favorite.article.title }}</h3>
              <p class="article-summary">{{ favorite.article.summary }}</p>
              <div class="article-meta">
                <span class="author">
                  <el-icon><User /></el-icon>
                  {{ favorite.article.author }}
                </span>
                <span class="favorite-time">
                  <el-icon><Clock /></el-icon>
                  收藏于 {{ formatDate(favorite.created_at) }}
                </span>
              </div>
            </div>
          </div>
          <div class="card-actions">
            <el-button
              type="text"
              size="small"
              @click="viewArticle(favorite.article)"
            >
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button
              type="text"
              size="small"
              class="danger-text"
              @click="removeFavorite(favorite.id)"
            >
              <el-icon><Delete /></el-icon>
              移除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Star,
  Search,
  Delete,
  User,
  Clock,
  View
} from '@element-plus/icons-vue'
import { favoriteApi } from '@/api/favorite'
import { formatDate } from '@/utils/date'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const favorites = ref([])
const selectedFavorites = ref([])
const searchKeyword = ref('')
const sortBy = ref('created_at')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 计算属性
const filteredFavorites = computed(() => {
  let result = [...favorites.value]
  
  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(favorite => 
      favorite.article.title.toLowerCase().includes(keyword) ||
      favorite.article.author.toLowerCase().includes(keyword)
    )
  }
  
  // 排序
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'title':
        return a.article.title.localeCompare(b.article.title)
      case 'author':
        return a.article.author.localeCompare(b.article.author)
      case 'created_at':
      default:
        return new Date(b.created_at) - new Date(a.created_at)
    }
  })
  
  return result
})

// 方法
const loadFavorites = async () => {
  try {
    loading.value = true
    const response = await favoriteApi.getFavorites({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value,
      sort: sortBy.value
    })
    favorites.value = response.items ?? []
    total.value = response.total ?? 0
  } catch (error) {
    console.error('加载收藏列表失败:', error)
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadFavorites()
}

const handleSort = () => {
  currentPage.value = 1
  loadFavorites()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadFavorites()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadFavorites()
}

const viewArticle = (article) => {
  router.push(`/articles/${article.id}`)
}

const removeFavorite = async (favoriteId) => {
  try {
    await ElMessageBox.confirm(
      '确定要移除这篇收藏吗？',
      '确认移除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await favoriteApi.removeFavorite(favoriteId)
    ElMessage.success('移除收藏成功')
    loadFavorites()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除收藏失败:', error)
      ElMessage.error('移除收藏失败')
    }
  }
}

const handleBatchRemove = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要移除选中的 ${selectedFavorites.value.length} 篇收藏吗？`,
      '批量移除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await favoriteApi.batchRemoveFavorites(selectedFavorites.value)
    ElMessage.success('批量移除成功')
    selectedFavorites.value = []
    loadFavorites()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量移除失败:', error)
      ElMessage.error('批量移除失败')
    }
  }
}

// 生命周期
onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.my-favorites {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.header-content {
  text-align: center;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.page-description {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.filter-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-left {
  flex: 1;
  max-width: 400px;
}

.search-input {
  width: 100%;
}

.filter-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.sort-select {
  width: 120px;
}

.favorites-list {
  min-height: 400px;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.favorite-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.3s ease;
  position: relative;
}

.favorite-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.favorite-card.selected {
  border: 2px solid #409eff;
}

.card-checkbox {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 2;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  padding: 4px;
}

.card-content {
  cursor: pointer;
  padding: 16px;
}

.article-cover {
  width: 100%;
  height: 160px;
  margin-bottom: 12px;
  border-radius: 8px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-info {
  padding-top: 8px;
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #303133;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-summary {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-actions {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.danger-text {
  color: #f56c6c !important;
}

.pagination-wrapper {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .my-favorites {
    padding: 16px;
  }
  
  .filter-toolbar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .filter-right {
    justify-content: space-between;
  }
  
  .favorites-grid {
    grid-template-columns: 1fr;
  }
  
  .page-title {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .filter-right {
    flex-direction: column;
    gap: 8px;
  }
  
  .sort-select {
    width: 100%;
  }
  
  .article-meta {
    font-size: 11px;
  }
}
</style>
