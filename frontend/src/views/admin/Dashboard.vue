<template>
  <div class="admin-dashboard" v-loading="loading">
    <section class="hero-card glass-card">
      <div class="hero-main">
        <div>
          <p class="hero-subtitle">{{ heroSubtitle }}</p>
          <h1 class="hero-title">
            <el-icon><DataBoard /></el-icon>
            智能数据看板
          </h1>
          <p class="hero-desc">掌握文章生命周期与作者运营情况，快速定位需要维护的内容。</p>
        </div>
        <div class="hero-actions">
          <el-select v-model="filters.range" placeholder="统计范围" @change="handleRangeChange" class="hero-select">
            <el-option label="近7天" value="7d" />
            <el-option label="近30天" value="30d" />
            <el-option label="近90天" value="90d" />
          </el-select>
          <el-button :icon="Refresh" @click="loadDashboard" :loading="loading" color="#2dd4bf">刷新数据</el-button>
        </div>
      </div>
      <div class="hero-meta">
        <div class="meta-item">
          <span class="meta-label">统计区间</span>
          <span class="meta-value">{{ rangeText }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">最后更新</span>
          <span class="meta-value">{{ lastUpdatedText }}</span>
        </div>
      </div>
    </section>

    <section class="metrics-grid">
      <div v-for="card in metricCards" :key="card.key" class="metric-card glass-card">
        <div class="metric-header">
          <div class="metric-icon" :class="card.key">
            <el-icon><component :is="card.icon" /></el-icon>
          </div>
          <div class="metric-info">
            <span class="metric-label">{{ card.label }}</span>
            <span class="metric-desc">{{ card.desc }}</span>
          </div>
        </div>
        <div class="metric-value">{{ formatNumber(card.value) }}</div>
        <div class="metric-trend" :class="{ positive: card.trend >= 0 }">
          {{ card.trend >= 0 ? '+' : '' }}{{ card.trend.toFixed(1) }}%
        </div>
      </div>
    </section>

    <section class="charts-row">
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <div>
              <span class="card-title">内容发布 & 互动趋势</span>
              <p class="card-subtitle">按日统计发布量、访问量与互动数据</p>
            </div>
            <el-tag type="info" v-if="dashboard?.trend.wowChange !== undefined">
              环比 {{ dashboard?.trend.wowChange >= 0 ? '+' : '' }}{{ dashboard?.trend.wowChange.toFixed(1) }}%
            </el-tag>
          </div>
        </template>
        <div ref="trendChartRef" class="trend-chart"></div>
      </el-card>

      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">内容漏斗 & 智能提示</span>
            <el-tag type="success">智能分析</el-tag>
          </div>
        </template>
        <div class="funnel-section">
          <div class="funnel-stages">
            <div v-for="stage in dashboard?.funnel.stages" :key="stage.key" class="funnel-stage">
              <div class="funnel-stage-header">
                <span class="stage-label">{{ stage.label }}</span>
                <span class="stage-value">{{ formatNumber(stage.value) }}</span>
              </div>
              <el-progress :percentage="Math.min(stage.conversion * 100, 100)" :stroke-width="8" />
              <p class="stage-hint">{{ stage.hint }}</p>
            </div>
          </div>
          <div class="insight-panel">
            <h4>
              <el-icon><WarningFilled /></el-icon>
              智能洞察
            </h4>
            <div v-if="dashboard?.insights?.length" class="insight-list">
              <div v-for="insight in dashboard?.insights" :key="insight.title" class="insight-item" :class="insight.severity">
                <p class="insight-title">{{ insight.title }}</p>
                <p class="insight-desc">{{ insight.description }}</p>
              </div>
            </div>
            <el-empty v-else description="暂无提示" />
          </div>
        </div>
      </el-card>
    </section>

    <section class="segments-row">
      <el-card shadow="hover" class="segment-card">
        <template #header>
          <div class="card-header">
            <div>
              <span class="card-title">内容分布</span>
              <p class="card-subtitle">按分类、标签与访问级别查看内容占比</p>
            </div>
            <div class="segment-tabs">
              <el-button-group>
                <el-button v-for="tab in segmentTabs" :key="tab.value" size="small" :type="segmentTab === tab.value ? 'primary' : 'default'" @click="segmentTab = tab.value">
                  {{ tab.label }}
                </el-button>
              </el-button-group>
            </div>
          </div>
        </template>
        <div class="segment-list">
          <div v-for="item in currentSegments" :key="item.key" class="segment-item">
            <div class="segment-info">
              <p class="segment-label">{{ item.label }}</p>
              <p class="segment-desc">{{ formatNumber(item.articleCount) }} 篇 · {{ formatPercent(item.percentage * 100) }}</p>
            </div>
            <div class="segment-stats">
              <span>浏览 {{ formatNumber(item.viewCount) }}</span>
              <span>互动 {{ formatNumber(item.likeCount + item.commentCount) }}</span>
            </div>
          </div>
          <el-empty v-if="!currentSegments.length" description="暂无数据" />
        </div>
      </el-card>

      <el-card shadow="hover" class="segment-card">
        <template #header>
          <div class="card-header">
            <div>
              <span class="card-title">文章榜单</span>
              <p class="card-subtitle">快速查看最受欢迎与最新内容</p>
            </div>
            <el-button-group>
              <el-button v-for="tab in leaderboardTabs" :key="tab.value" size="small" :type="leaderboardTab === tab.value ? 'primary' : 'default'" @click="leaderboardTab = tab.value">
                {{ tab.label }}
              </el-button>
            </el-button-group>
          </div>
        </template>
        <div class="leaderboard">
          <div v-for="(article, index) in leaderboardItems" :key="article.articleId" class="leaderboard-item">
            <div class="leaderboard-rank">{{ index + 1 }}</div>
            <div class="leaderboard-body">
              <div class="leaderboard-title">
                <span>{{ article.title }}</span>
                <el-tag size="small" v-if="article.top" type="warning">置顶</el-tag>
                <el-tag size="small" v-if="article.recommend" type="success">推荐</el-tag>
              </div>
              <p class="leaderboard-meta">
                {{ article.authorName }} · {{ formatDateTime(article.publishTime) }}
              </p>
              <div class="leaderboard-stats">
                <span>浏览 {{ formatNumber(article.viewCount) }}</span>
                <span>互动 {{ formatNumber(article.likeCount + article.commentCount) }}</span>
              </div>
            </div>
          </div>
          <el-empty v-if="!leaderboardItems.length" description="暂无文章" />
        </div>
      </el-card>
    </section>

    <section class="moderation-row">
      <el-card shadow="hover" class="moderation-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">审核与待办</span>
            <el-tag v-if="dashboard?.moderation.pendingReviews" type="danger">{{ dashboard?.moderation.pendingReviews }} 待审核</el-tag>
          </div>
        </template>
        <div class="moderation-summary">
          <div class="summary-card">
            <p>待审核</p>
            <h3>{{ dashboard?.moderation.pendingReviews ?? 0 }}</h3>
            <span>待作者/管理员处理</span>
          </div>
          <div class="summary-card">
            <p>未通过</p>
            <h3>{{ dashboard?.moderation.rejectedReviews ?? 0 }}</h3>
            <span>需复审或调整</span>
          </div>
          <div class="summary-card">
            <p>最久草稿</p>
            <h3>{{ dashboard?.moderation.oldestDraftDays ?? 0 }} 天</h3>
            <span>建议尽快发布或归档</span>
          </div>
        </div>
        <div class="moderation-timeline">
          <div v-for="record in dashboard?.moderation.records" :key="record.articleId" class="timeline-item">
            <div class="timeline-dot" />
            <div class="timeline-content">
              <p class="timeline-title">{{ record.title }}</p>
              <p class="timeline-meta">
                {{ reviewStatusText(record.reviewStatus) }} · {{ statusText(record.status) }} · {{ record.reviewer }}
              </p>
              <span class="timeline-time">{{ formatDateTime(record.updatedAt) }}</span>
            </div>
          </div>
          <el-empty v-if="!dashboard?.moderation.records?.length" description="暂无审核动态" />
        </div>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import {
  DataBoard,
  Refresh,
  WarningFilled,
  TrendCharts,
  Histogram
} from '@element-plus/icons-vue'
import { useTenantStore } from '@/stores/tenant'
import { useUserStore } from '@/stores/user'
import { articleApi } from '@/api'
import type {
  DashboardResponse,
  DashboardFilters,
  DashboardSegmentItem,
  DashboardArticleTile
} from '@/types/dashboard'

const tenantStore = useTenantStore()
const userStore = useUserStore()

const loading = ref(false)
const dashboard = ref<DashboardResponse | null>(null)
const filters = ref<DashboardFilters>({ range: '30d' })

const trendChartRef = ref<HTMLDivElement | null>(null)
let trendChart: echarts.ECharts | null = null

const segmentTab = ref<'categories' | 'tags' | 'accessLevels'>('categories')
const leaderboardTab = ref<'topViews' | 'topEngagement' | 'latestPublished'>('topViews')

const segmentTabs = [
  { label: '分类', value: 'categories' as const },
  { label: '标签', value: 'tags' as const },
  { label: '访问级别', value: 'accessLevels' as const }
]

const leaderboardTabs = [
  { label: '浏览榜', value: 'topViews' as const },
  { label: '互动榜', value: 'topEngagement' as const },
  { label: '最新发布', value: 'latestPublished' as const }
]

const heroSubtitle = computed(() => {
  const tenantName = tenantStore.tenantInfo?.name || '内容空间'
  const username = userStore.currentUser?.nickname || userStore.currentUser?.username || '管理员'
  return `${tenantName} · ${username}`
})

const rangeText = computed(() => {
  if (!dashboard.value?.range) {
    return '---'
  }
  return `${dashboard.value.range.startDate} ~ ${dashboard.value.range.endDate}`
})

const lastUpdatedText = computed(() => {
  const time = dashboard.value?.overview.lastUpdated
  return time ? formatDateTime(time) : '---'
})

const metricCards = computed(() => {
  const overview = dashboard.value?.overview
  if (!overview) {
    return []
  }
  return [
    {
      key: 'articles',
      label: '文章总数',
      value: overview.totalArticles,
      desc: `本周新增 ${overview.weeklyNewArticles}`,
      trend: overview.viewGrowthRate,
      icon: TrendCharts
    },
    {
      key: 'views',
      label: '总浏览量',
      value: overview.totalViews,
      desc: '全量访问',
      trend: overview.viewGrowthRate,
      icon: Histogram
    },
    {
      key: 'engagement',
      label: '互动总量',
      value: overview.totalLikes + overview.totalComments + overview.totalCollects,
      desc: '点赞/评论/收藏',
      trend: overview.engagementGrowthRate,
      icon: DataBoard
    },
    {
      key: 'shares',
      label: '分享次数',
      value: overview.totalShares,
      desc: '传播范围',
      trend: overview.engagementGrowthRate / 2,
      icon: WarningFilled
    }
  ]
})

const currentSegments = computed<DashboardSegmentItem[]>(() => {
  if (!dashboard.value) return []
  return (dashboard.value.segments as any)?.[segmentTab.value] || []
})

const leaderboardItems = computed<DashboardArticleTile[]>(() => {
  if (!dashboard.value) return []
  return (dashboard.value.leaderboards as any)?.[leaderboardTab.value] || []
})

const handleRangeChange = () => {
  loadDashboard()
}

const loadDashboard = async () => {
  loading.value = true
  try {
    dashboard.value = await articleApi.getStats(filters.value)
    await nextTick()
    renderTrendChart()
  } catch (error) {
    console.error('加载仪表盘失败', error)
    ElMessage.error('加载仪表盘失败')
  } finally {
    loading.value = false
  }
}

const renderTrendChart = () => {
  if (!trendChartRef.value || !dashboard.value) {
    return
  }
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  const points = dashboard.value.trend.points || []
  trendChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: 24,
      right: 24,
      top: 40,
      bottom: 24
    },
    legend: {
      data: ['发布数', '访问量', '互动']
    },
    xAxis: {
      type: 'category',
      data: points.map((item) => item.date),
      boundaryGap: false
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '发布数',
        type: 'bar',
        data: points.map((item) => item.published),
        barWidth: 12,
        itemStyle: {
          color: '#2dd4bf'
        }
      },
      {
        name: '访问量',
        type: 'line',
        data: points.map((item) => item.views),
        smooth: true,
        areaStyle: {
          color: 'rgba(59,130,246,0.15)'
        },
        lineStyle: {
          color: '#3b82f6'
        }
      },
      {
        name: '互动',
        type: 'line',
        data: points.map((item) => item.likes + item.comments + item.shares),
        smooth: true,
        lineStyle: {
          color: '#f97316'
        }
      }
    ]
  })
}

const formatNumber = (value?: number) => {
  if (value === undefined || value === null) return '0'
  return new Intl.NumberFormat('zh-CN').format(value)
}

const formatPercent = (value?: number) => {
  if (value === undefined || value === null) return '0%'
  return `${value >= 0 ? '+' : ''}${value.toFixed(1)}%`
}

const formatDateTime = (value?: string | null) => {
  if (!value) return '---'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

const statusText = (status?: number | null) => {
  switch (status) {
    case 0:
      return '草稿'
    case 1:
      return '已发布'
    case 2:
      return '已下线'
    default:
      return '未知'
  }
}

const reviewStatusText = (status?: number | null) => {
  switch (status) {
    case 0:
      return '待审核'
    case 1:
      return '已通过'
    case 2:
      return '未通过'
    default:
      return '未审核'
  }
}

const handleResize = () => {
  trendChart?.resize()
}

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  trendChart = null
})
</script>

<style scoped>
.admin-dashboard {
  min-height: 100%;
  padding: 24px;
  background: radial-gradient(circle at top, #0f172a, #020617 60%);
  color: #fff;
}

.glass-card {
  background: rgba(15, 23, 42, 0.75);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 16px;
  padding: 24px;
  backdrop-filter: blur(16px);
  box-shadow: 0 20px 45px rgba(2, 6, 23, 0.45);
}

.hero-card {
  margin-bottom: 24px;
}

.hero-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
}

.hero-title {
  font-size: 32px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 4px 0;
}

.hero-subtitle {
  text-transform: uppercase;
  letter-spacing: 0.2em;
  color: #94a3b8;
  font-size: 12px;
}

.hero-desc {
  color: #cbd5f5;
  margin: 0;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hero-select {
  min-width: 160px;
}

.hero-meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
  margin-top: 24px;
}

.meta-item {
  padding: 16px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.15);
}

.meta-label {
  display: block;
  color: #94a3b8;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.meta-value {
  font-weight: 600;
  font-size: 18px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 18px;
  margin-bottom: 24px;
}

.metric-card {
  padding: 20px;
  border-radius: 14px;
  position: relative;
}

.metric-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(59, 130, 246, 0.2);
  color: #93c5fd;
}

.metric-info {
  display: flex;
  flex-direction: column;
}

.metric-label {
  font-weight: 600;
}

.metric-desc {
  font-size: 12px;
  color: #94a3b8;
}

.metric-value {
  font-size: 32px;
  font-weight: 700;
  margin-top: 12px;
}

.metric-trend {
  font-size: 14px;
  color: #f97316;
}

.metric-trend.positive {
  color: #34d399;
}

.charts-row,
.segments-row,
.moderation-row {
  display: grid;
  gap: 24px;
  margin-bottom: 24px;
}

.charts-row,
.segments-row {
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
}

.chart-card {
  border-radius: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.card-subtitle {
  color: #94a3b8;
  margin: 0;
}

.trend-chart {
  width: 100%;
  height: 360px;
}

.funnel-section {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

.funnel-stage {
  padding: 12px 0;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.funnel-stage:last-child {
  border-bottom: none;
}

.funnel-stage-header {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
  margin-bottom: 4px;
}

.stage-hint {
  color: #94a3b8;
  font-size: 12px;
  margin: 6px 0 0;
}

.insight-panel {
  background: rgba(15, 23, 42, 0.6);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(148, 163, 184, 0.15);
}

.insight-panel h4 {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 12px;
}

.insight-item {
  padding: 12px;
  border-radius: 10px;
  margin-bottom: 12px;
  background: rgba(59, 130, 246, 0.12);
}

.insight-item.warning {
  background: rgba(251, 191, 36, 0.15);
}

.insight-item.success {
  background: rgba(34, 197, 94, 0.15);
}

.insight-title {
  font-weight: 600;
  margin: 0 0 4px;
}

.insight-desc {
  color: #cbd5f5;
  margin: 0;
}

.segment-card {
  border-radius: 16px;
}

.segment-list {
  max-height: 360px;
  overflow-y: auto;
}

.segment-item {
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
  padding: 16px 0;
}

.segment-item:last-child {
  border-bottom: none;
}

.segment-label {
  margin: 0;
  font-weight: 600;
}

.segment-desc,
.segment-stats span {
  color: #94a3b8;
  font-size: 13px;
}

.leaderboard {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.leaderboard-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.leaderboard-rank {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: rgba(59, 130, 246, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.leaderboard-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.leaderboard-meta {
  color: #94a3b8;
  margin: 2px 0;
  font-size: 13px;
}

.leaderboard-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #cbd5f5;
}

.moderation-card {
  border-radius: 16px;
}

.moderation-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.summary-card {
  text-align: center;
  padding: 16px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.7);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.summary-card h3 {
  margin: 8px 0;
  font-size: 28px;
}

.summary-card span {
  font-size: 12px;
  color: #94a3b8;
}

.moderation-timeline {
  position: relative;
  padding-left: 20px;
  max-height: 360px;
  overflow-y: auto;
}

.moderation-timeline::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: rgba(148, 163, 184, 0.3);
}

.timeline-item {
  position: relative;
  margin-bottom: 20px;
}

.timeline-dot {
  position: absolute;
  left: -12px;
  top: 6px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.25);
}

.timeline-content {
  padding: 12px 16px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.65);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.timeline-title {
  margin: 0;
  font-weight: 600;
}

.timeline-meta {
  color: #94a3b8;
  margin: 4px 0;
  font-size: 13px;
}

.timeline-time {
  font-size: 12px;
  color: #cbd5f5;
}

@media (max-width: 1024px) {
  .hero-main {
    flex-direction: column;
    align-items: flex-start;
  }

  .funnel-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .admin-dashboard {
    padding: 16px;
  }

  .hero-actions {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
