<template>
  <div class="analytics-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <el-icon><TrendCharts /></el-icon>
          统计分析
        </h2>
        <p class="page-desc">查看系统运营数据和趋势分析</p>
      </div>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
          class="date-picker"
        />
        <el-button type="primary" @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 概览统计卡片 -->
    <div class="overview-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon articles">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ overviewStats.totalArticles || 0 }}</div>
                <div class="stat-label">总文章数</div>
                <div class="stat-change" :class="{ positive: overviewStats.todayArticles > 0 }">
                  今日新增 {{ overviewStats.todayArticles || 0 }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon users">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ overviewStats.totalUsers || 0 }}</div>
                <div class="stat-label">总用户数</div>
                <div class="stat-change" :class="{ positive: overviewStats.todayUsers > 0 }">
                  今日新增 {{ overviewStats.todayUsers || 0 }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon views">
                <el-icon><View /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ formatNumber(overviewStats.totalViews) || 0 }}</div>
                <div class="stat-label">总访问量</div>
                <div class="stat-change" :class="{ positive: overviewStats.todayViews > 0 }">
                  今日访问 {{ formatNumber(overviewStats.todayViews) || 0 }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon comments">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ overviewStats.totalComments || 0 }}</div>
                <div class="stat-label">总评论数</div>
                <div class="stat-change" :class="{ positive: overviewStats.todayComments > 0 }">
                  今日新增 {{ overviewStats.todayComments || 0 }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-section">
      <!-- 访问趋势图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">访问趋势</span>
              <el-select v-model="viewPeriod" size="small" @change="loadViewStats">
                <el-option label="最近7天" value="week" />
                <el-option label="最近30天" value="month" />
                <el-option label="最近90天" value="quarter" />
              </el-select>
            </div>
          </template>
          <div class="chart-container" ref="viewTrendChart" v-loading="chartsLoading"></div>
        </el-card>
      </el-col>

      <!-- 用户增长图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">用户增长</span>
              <el-select v-model="userPeriod" size="small" @change="loadUserStats">
                <el-option label="最近7天" value="week" />
                <el-option label="最近30天" value="month" />
                <el-option label="最近90天" value="quarter" />
              </el-select>
            </div>
          </template>
          <div class="chart-container" ref="userGrowthChart" v-loading="chartsLoading"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-section">
      <!-- 分类统计饼图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span class="card-title">文章分类分布</span>
          </template>
          <div class="chart-container" ref="categoryChart" v-loading="chartsLoading"></div>
        </el-card>
      </el-col>

      <!-- 热门文章列表 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span class="card-title">热门文章 TOP 10</span>
          </template>
          <div class="popular-articles" v-loading="chartsLoading">
            <div 
              v-for="(article, index) in popularArticles" 
              :key="article.id"
              class="article-item"
            >
              <div class="article-rank">{{ index + 1 }}</div>
              <div class="article-info">
                <div class="article-title">{{ article.title }}</div>
                <div class="article-meta">
                  <span>{{ article.author }}</span>
                  <span>{{ formatDate(article.publishTime) }}</span>
                </div>
              </div>
              <div class="article-stats">
                <div class="stat-item">
                  <el-icon><View /></el-icon>
                  {{ formatNumber(article.views) }}
                </div>
                <div class="stat-item">
                  <el-icon><ChatDotRound /></el-icon>
                  {{ article.comments }}
                </div>
              </div>
            </div>
            <div v-if="!popularArticles.length" class="empty-state">
              <el-empty description="暂无数据" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统信息 -->
    <el-card class="system-info-card">
      <template #header>
        <span class="card-title">系统信息</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="info-section">
            <h4>服务器信息</h4>
            <div class="info-item">
              <span class="info-label">操作系统:</span>
              <span class="info-value">{{ systemStats.serverInfo?.os || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Java版本:</span>
              <span class="info-value">{{ systemStats.serverInfo?.javaVersion || '-' }}</span>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-section">
            <h4>内存使用</h4>
            <div class="info-item">
              <span class="info-label">总内存:</span>
              <span class="info-value">{{ systemStats.serverInfo?.memory?.total || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">已使用:</span>
              <span class="info-value">{{ systemStats.serverInfo?.memory?.used || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">可用内存:</span>
              <span class="info-value">{{ systemStats.serverInfo?.memory?.free || '-' }}</span>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-section">
            <h4>数据库信息</h4>
            <div class="info-item">
              <span class="info-label">数据库类型:</span>
              <span class="info-value">{{ systemStats.databaseInfo?.type || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">版本:</span>
              <span class="info-value">{{ systemStats.databaseInfo?.version || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">数据库大小:</span>
              <span class="info-value">{{ systemStats.databaseInfo?.size || '-' }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  TrendCharts,
  Refresh,
  Document,
  User,
  View,
  ChatDotRound
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getOverviewStats,
  getViewStats,
  getUserStats,
  getCategoryStats,
  getSystemStats,
  getArticleStats
} from '@/api/analytics'
import type {
  OverviewStats,
  ViewStats,
  UserStats,
  CategoryStats,
  SystemStats,
  PopularArticle
} from '@/types/analytics'

// 响应式数据
const loading = ref(false)
const chartsLoading = ref(false)
const dateRange = ref<[string, string]>(['', ''])
const viewPeriod = ref('month')
const userPeriod = ref('month')

// 统计数据
const overviewStats = reactive<OverviewStats>({
  totalArticles: 0,
  totalUsers: 0,
  totalViews: 0,
  totalComments: 0,
  todayArticles: 0,
  todayUsers: 0,
  todayViews: 0,
  todayComments: 0
})

const systemStats = reactive<SystemStats>({
  serverInfo: {
    os: '',
    javaVersion: '',
    memory: { total: '', used: '', free: '' },
    disk: { total: '', used: '', free: '' }
  },
  databaseInfo: {
    type: '',
    version: '',
    size: ''
  }
})

const popularArticles = ref<PopularArticle[]>([])

// 图表引用
const viewTrendChart = ref<HTMLElement>()
const userGrowthChart = ref<HTMLElement>()
const categoryChart = ref<HTMLElement>()

// 图表实例
let viewChart: echarts.ECharts | null = null
let userChart: echarts.ECharts | null = null
let categoryChartInstance: echarts.ECharts | null = null

// 方法
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num.toString()
}

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const handleDateChange = () => {
  refreshData()
}

const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadOverviewStats(),
      loadViewStats(),
      loadUserStats(),
      loadCategoryStats(),
      loadSystemStats(),
      loadPopularArticles()
    ])
  } finally {
    loading.value = false
  }
}

const loadOverviewStats = async () => {
  try {
    const params = dateRange.value[0] && dateRange.value[1] ? {
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    } : undefined

    const data = await getOverviewStats(params)
    Object.assign(overviewStats, data)
  } catch (error) {
    console.error('加载概览统计失败:', error)
    // 使用模拟数据
    Object.assign(overviewStats, {
      totalArticles: 1250,
      totalUsers: 3680,
      totalViews: 125000,
      totalComments: 8900,
      todayArticles: 12,
      todayUsers: 45,
      todayViews: 2300,
      todayComments: 67
    })
  }
}

const loadViewStats = async () => {
  chartsLoading.value = true
  try {
    const params = { period: viewPeriod.value }
    const data = await getViewStats(params)
    
    await nextTick()
    if (viewTrendChart.value) {
      initViewTrendChart(data.viewTrends || [])
    }
  } catch (error) {
    console.error('加载访问统计失败:', error)
    // 使用模拟数据
    const mockData = generateMockViewData()
    await nextTick()
    if (viewTrendChart.value) {
      initViewTrendChart(mockData)
    }
  } finally {
    chartsLoading.value = false
  }
}

const loadUserStats = async () => {
  try {
    const params = { period: userPeriod.value }
    const data = await getUserStats(params)
    
    await nextTick()
    if (userGrowthChart.value) {
      initUserGrowthChart(data.userGrowthData || [])
    }
  } catch (error) {
    console.error('加载用户统计失败:', error)
    // 使用模拟数据
    const mockData = generateMockUserData()
    await nextTick()
    if (userGrowthChart.value) {
      initUserGrowthChart(mockData)
    }
  }
}

const loadCategoryStats = async () => {
  try {
    const data = await getCategoryStats()
    
    await nextTick()
    if (categoryChart.value) {
      initCategoryChart(data || [])
    }
  } catch (error) {
    console.error('加载分类统计失败:', error)
    // 使用模拟数据
    const mockData = [
      { categoryName: '技术分享', articleCount: 45, totalViews: 12000, percentage: 35 },
      { categoryName: '生活随笔', articleCount: 32, totalViews: 8500, percentage: 25 },
      { categoryName: '学习笔记', articleCount: 28, totalViews: 7200, percentage: 22 },
      { categoryName: '项目经验', articleCount: 15, totalViews: 4800, percentage: 12 },
      { categoryName: '其他', articleCount: 8, totalViews: 2100, percentage: 6 }
    ]
    await nextTick()
    if (categoryChart.value) {
      initCategoryChart(mockData)
    }
  }
}

const loadSystemStats = async () => {
  try {
    const data = await getSystemStats()
    Object.assign(systemStats, data)
  } catch (error) {
    console.error('加载系统统计失败:', error)
    // 使用模拟数据
    Object.assign(systemStats, {
      serverInfo: {
        os: 'Linux Ubuntu 20.04',
        javaVersion: 'OpenJDK 11.0.16',
        memory: { total: '8.0 GB', used: '4.2 GB', free: '3.8 GB' },
        disk: { total: '100 GB', used: '45 GB', free: '55 GB' }
      },
      databaseInfo: {
        type: 'MySQL',
        version: '8.0.30',
        size: '2.3 GB'
      }
    })
  }
}

const loadPopularArticles = async () => {
  try {
    const data = await getArticleStats()
    popularArticles.value = data.popularArticles || []
  } catch (error) {
    console.error('加载热门文章失败:', error)
    // 使用模拟数据
    popularArticles.value = [
      { id: 1, title: 'Vue 3 组合式API最佳实践', views: 15600, comments: 89, publishTime: '2024-01-15', author: '张三' },
      { id: 2, title: 'Spring Boot 微服务架构设计', views: 12300, comments: 67, publishTime: '2024-01-12', author: '李四' },
      { id: 3, title: 'React Hooks 深入理解', views: 11800, comments: 54, publishTime: '2024-01-10', author: '王五' },
      { id: 4, title: 'Docker 容器化部署实战', views: 9800, comments: 43, publishTime: '2024-01-08', author: '赵六' },
      { id: 5, title: 'TypeScript 进阶技巧', views: 8900, comments: 38, publishTime: '2024-01-05', author: '钱七' }
    ]
  }
}

// 图表初始化方法
const initViewTrendChart = (data: any[]) => {
  if (!viewTrendChart.value) return
  
  if (viewChart) {
    viewChart.dispose()
  }
  
  viewChart = echarts.init(viewTrendChart.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['访问量', '独立访客']
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.date)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '访问量',
        type: 'line',
        data: data.map(item => item.views),
        smooth: true,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '独立访客',
        type: 'line',
        data: data.map(item => item.uniqueViews),
        smooth: true,
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
  
  viewChart.setOption(option)
}

const initUserGrowthChart = (data: any[]) => {
  if (!userGrowthChart.value) return
  
  if (userChart) {
    userChart.dispose()
  }
  
  userChart = echarts.init(userGrowthChart.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['新增用户', '累计用户']
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.date)
    },
    yAxis: [
      {
        type: 'value',
        name: '新增用户',
        position: 'left'
      },
      {
        type: 'value',
        name: '累计用户',
        position: 'right'
      }
    ],
    series: [
      {
        name: '新增用户',
        type: 'bar',
        data: data.map(item => item.newUsers),
        itemStyle: { color: '#E6A23C' }
      },
      {
        name: '累计用户',
        type: 'line',
        yAxisIndex: 1,
        data: data.map(item => item.totalUsers),
        smooth: true,
        itemStyle: { color: '#F56C6C' }
      }
    ]
  }
  
  userChart.setOption(option)
}

const initCategoryChart = (data: CategoryStats[]) => {
  if (!categoryChart.value) return
  
  if (categoryChartInstance) {
    categoryChartInstance.dispose()
  }
  
  categoryChartInstance = echarts.init(categoryChart.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '文章分类',
        type: 'pie',
        radius: '50%',
        data: data.map(item => ({
          value: item.articleCount,
          name: item.categoryName
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  categoryChartInstance.setOption(option)
}

// 模拟数据生成
const generateMockViewData = () => {
  const data = []
  const days = viewPeriod.value === 'week' ? 7 : viewPeriod.value === 'month' ? 30 : 90
  
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    data.push({
      date: date.toISOString().split('T')[0],
      views: Math.floor(Math.random() * 1000) + 500,
      uniqueViews: Math.floor(Math.random() * 600) + 300
    })
  }
  
  return data
}

const generateMockUserData = () => {
  const data = []
  const days = userPeriod.value === 'week' ? 7 : userPeriod.value === 'month' ? 30 : 90
  let totalUsers = 3000
  
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    const newUsers = Math.floor(Math.random() * 50) + 10
    totalUsers += newUsers
    
    data.push({
      date: date.toISOString().split('T')[0],
      newUsers,
      totalUsers
    })
  }
  
  return data
}

// 生命周期
onMounted(() => {
  refreshData()
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    viewChart?.resize()
    userChart?.resize()
    categoryChartInstance?.resize()
  })
})
</script>

<style scoped>
.analytics-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-desc {
  margin: 4px 0 0;
  color: #909399;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.date-picker {
  width: 240px;
}

.overview-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-icon.articles {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.users {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.views {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.comments {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin: 4px 0;
}

.stat-change {
  font-size: 12px;
  color: #909399;
}

.stat-change.positive {
  color: #67c23a;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.popular-articles {
  max-height: 300px;
  overflow-y: auto;
}

.article-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  gap: 12px;
}

.article-item:last-child {
  border-bottom: none;
}

.article-rank {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.article-info {
  flex: 1;
  min-width: 0;
}

.article-title {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 12px;
}

.article-stats {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.system-info-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.info-section h4 {
  margin: 0 0 16px;
  font-size: 16px;
  color: #303133;
  font-weight: 600;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-label {
  color: #909399;
}

.info-value {
  color: #303133;
  font-weight: 500;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
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
  }

  .overview-cards .el-col {
    margin-bottom: 12px;
  }

  .charts-section .el-col {
    margin-bottom: 20px;
  }
}
</style>