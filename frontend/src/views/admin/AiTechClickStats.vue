<template>
  <div class="aitech-clicks-page">
    <div class="page-header">
      <div>
        <h2>AiTech 卡片点击统计</h2>
        <p>追踪 /aitech 页面中互动卡片的点击热度，掌握用户偏好与活跃度。</p>
      </div>
      <div class="page-actions">
        <el-input v-model="keyword" placeholder="搜索标题或分类" clearable class="search-input">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :loading="loading" @click="fetchStats">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="stat-highlights">
      <el-col :md="6" :sm="12" :xs="24">
        <el-card class="stat-card">
          <div class="stat-icon primary">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <p>总点击量</p>
            <h3>{{ totalClicks.toLocaleString() }}</h3>
            <small>覆盖 {{ cardCount }} 个卡片</small>
          </div>
        </el-card>
      </el-col>
      <el-col :md="6" :sm="12" :xs="24">
        <el-card class="stat-card">
          <div class="stat-icon success">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="stat-info">
            <p>热度冠军</p>
            <h3>{{ topCard?.cardTitle ?? '暂无数据' }}</h3>
            <small v-if="topCard">累计 {{ (topCard.clickCount || 0).toLocaleString() }} 次</small>
            <small v-else>等待用户探索</small>
          </div>
        </el-card>
      </el-col>
      <el-col :md="6" :sm="12" :xs="24">
        <el-card class="stat-card">
          <div class="stat-icon warning">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <p>最近点击</p>
            <h3>{{ latestUpdate?.cardTitle ?? '暂无数据' }}</h3>
            <small v-if="latestUpdate">{{ formatDate(latestUpdate.lastClickTime) }}</small>
            <small v-else>等待新的点击</small>
          </div>
        </el-card>
      </el-col>
      <el-col :md="6" :sm="12" :xs="24">
        <el-card class="stat-card">
          <div class="stat-icon info">
            <el-icon><Collection /></el-icon>
          </div>
          <div class="stat-info">
            <p>覆盖分类</p>
            <h3>{{ categoryCount }}</h3>
            <small>自动去重分类标签</small>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <div>
            <h3>卡片点击详情</h3>
            <p>按照点击次数从高到低排序，可实时筛选</p>
          </div>
          <div class="header-actions">
            <el-tag size="small" type="info">共 {{ filteredStats.length }} 条</el-tag>
            <el-button text type="primary" @click="resetFilter" :disabled="!keyword">
              清除筛选
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredStats" v-loading="loading" border stripe>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column label="卡片标题">
          <template #default="{ row }">
            <div class="card-title-cell">
              <strong>{{ row.cardTitle }}</strong>
              <span class="path-text">{{ row.cardPath }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="cardCategory" label="分类" width="160">
          <template #default="{ row }">
            <el-tag :type="resolveCategoryType(row.cardCategory)" disable-transitions>
              {{ row.cardCategory || '未设置' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="clickCount" label="累计点击" width="140" align="center">
          <template #default="{ row }">
            <span class="click-count">{{ (row.clickCount || 0).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastClickTime" label="最近点击" width="200">
          <template #default="{ row }">
            {{ formatDate(row.lastClickTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { TrendCharts, DataLine, Timer, Refresh, Search, Collection } from '@element-plus/icons-vue'
import { aitechApi } from '@/api'
import type { AiTechCardClickStats } from '@/types'

const loading = ref(false)
const stats = ref<AiTechCardClickStats[]>([])
const keyword = ref('')
const totalClicks = computed(() =>
  stats.value.reduce((sum, item) => sum + (item.clickCount ?? 0), 0)
)
const cardCount = computed(() => stats.value.length)
const categoryCount = computed(() => {
  const set = new Set(
    stats.value
      .map((item) => (item.cardCategory || '').trim())
      .filter((item) => item.length > 0)
  )
  return set.size
})

const topCard = computed(() => stats.value[0] ?? null)
const latestUpdate = computed(() => {
  return stats.value
    .filter((item) => item.lastClickTime)
    .sort((a, b) => new Date(b.lastClickTime || 0).getTime() - new Date(a.lastClickTime || 0).getTime())[0]
})

const filteredStats = computed(() => {
  if (!keyword.value.trim()) {
    return stats.value
  }
  const query = keyword.value.trim().toLowerCase()
  return stats.value.filter((item) => {
    const title = item.cardTitle?.toLowerCase() ?? ''
    const path = item.cardPath?.toLowerCase() ?? ''
    const category = item.cardCategory?.toLowerCase() ?? ''
    return title.includes(query) || path.includes(query) || category.includes(query)
  })
})

const fetchStats = async () => {
  try {
    loading.value = true
    const data = await aitechApi.getAiTechCardStats()
    const list = Array.isArray(data) ? data : []
    stats.value = [...list].sort((a, b) => (b.clickCount ?? 0) - (a.clickCount ?? 0))
  } catch (error) {
    console.error('加载 AiTech 卡片点击数据失败', error)
    ElMessage.error('加载 AiTech 数据失败')
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  keyword.value = ''
}

const formatDate = (value?: string) => {
  if (!value) {
    return '—'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '—'
  }
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(
    date.getDate()
  ).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(
    2,
    '0'
  )}`
}

const resolveCategoryType = (category?: string) => {
  if (!category) return 'info'
  if (category.includes('历史')) return 'warning'
  if (category.includes('物理') || category.includes('科学')) return 'success'
  if (category.includes('地理') || category.includes('地球')) return 'info'
  if (category.includes('互动') || category.includes('游戏')) return 'success'
  if (category.includes('节日')) return 'danger'
  return 'info'
}

onMounted(fetchStats)
</script>

<style scoped>
.aitech-clicks-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1f2937;
}

.page-header p {
  margin: 4px 0 0;
  color: #6b7280;
}

.page-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 240px;
}

.stat-highlights {
  margin-bottom: 8px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
}

.stat-icon.primary {
  background: linear-gradient(135deg, #2563eb, #3b82f6);
}

.stat-icon.success {
  background: linear-gradient(135deg, #059669, #34d399);
}

.stat-icon.warning {
  background: linear-gradient(135deg, #d97706, #f59e0b);
}

.stat-icon.info {
  background: linear-gradient(135deg, #0ea5e9, #38bdf8);
}

.stat-info h3 {
  margin: 4px 0;
  font-size: 18px;
  color: #111827;
}

.stat-info p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.stat-info small {
  color: #9ca3af;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.card-header p {
  margin: 4px 0 0;
  color: #9ca3af;
  font-size: 13px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-title-cell {
  display: flex;
  flex-direction: column;
}

.card-title-cell strong {
  color: #111827;
  font-weight: 600;
}

.path-text {
  font-size: 12px;
  color: #9ca3af;
}

.click-count {
  font-weight: 600;
  color: #2563eb;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-actions {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 100%;
  }
}
</style>
