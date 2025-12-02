<template>
  <div class="editor-sidebar">
    <!-- 大纲 -->
    <div class="sidebar-section">
      <div class="section-header">
        <h3>大纲</h3>
        <el-button 
          text 
          size="small" 
          @click="toggleOutline"
          :icon="outlineVisible ? 'ep-arrow-up' : 'ep-arrow-down'"
        />
      </div>
      <div v-show="outlineVisible" class="outline-content">
        <div v-if="outline.length === 0" class="empty-state">
          暂无标题
        </div>
        <div v-else class="outline-list">
          <div
            v-for="item in outline"
            :key="item.id"
            :class="['outline-item', `level-${item.level}`]"
            @click="scrollToHeading(item.id)"
          >
            {{ item.text }}
          </div>
        </div>
      </div>
    </div>

    <!-- 字符统计 -->
    <div class="sidebar-section">
      <div class="section-header">
        <h3>统计信息</h3>
        <el-button 
          text 
          size="small" 
          @click="toggleStats"
          :icon="statsVisible ? 'ep-arrow-up' : 'ep-arrow-down'"
        />
      </div>
      <div v-show="statsVisible" class="stats-content">
        <div class="stat-item">
          <span class="stat-label">字符数：</span>
          <span class="stat-value">{{ stats.characters }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">单词数：</span>
          <span class="stat-value">{{ stats.words }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">段落数：</span>
          <span class="stat-value">{{ stats.paragraphs }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">预计阅读：</span>
          <span class="stat-value">{{ readingTime }}分钟</span>
        </div>
      </div>
    </div>

    <!-- 文档信息 -->
    <div class="sidebar-section">
      <div class="section-header">
        <h3>文档信息</h3>
        <el-button 
          text 
          size="small" 
          @click="toggleInfo"
          :icon="infoVisible ? 'ep-arrow-up' : 'ep-arrow-down'"
        />
      </div>
      <div v-show="infoVisible" class="info-content">
        <div class="info-item">
          <span class="info-label">创建时间：</span>
          <span class="info-value">{{ formatDate(createdAt) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">修改时间：</span>
          <span class="info-value">{{ formatDate(updatedAt) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">状态：</span>
          <el-tag :type="statusType" size="small">{{ statusText }}</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface OutlineItem {
  id: string
  level: number
  text: string
}

interface Stats {
  characters: number
  words: number
  paragraphs: number
}

interface Props {
  outline: OutlineItem[]
  stats: Stats
  createdAt?: Date
  updatedAt?: Date
  status?: 'draft' | 'published' | 'archived'
}

const props = withDefaults(defineProps<Props>(), {
  outline: () => [],
  stats: () => ({ characters: 0, words: 0, paragraphs: 0 }),
  status: 'draft'
})

const emit = defineEmits<{
  scrollToHeading: [id: string]
}>()

// 展开状态
const outlineVisible = ref(true)
const statsVisible = ref(true)
const infoVisible = ref(true)

// 计算属性
const readingTime = computed(() => {
  // 按照每分钟200字计算
  return Math.ceil(props.stats.characters / 200)
})

const statusType = computed(() => {
  switch (props.status) {
    case 'published':
      return 'success'
    case 'archived':
      return 'info'
    default:
      return 'warning'
  }
})

const statusText = computed(() => {
  switch (props.status) {
    case 'published':
      return '已发布'
    case 'archived':
      return '已归档'
    default:
      return '草稿'
  }
})

// 方法
const toggleOutline = () => {
  outlineVisible.value = !outlineVisible.value
}

const toggleStats = () => {
  statsVisible.value = !statsVisible.value
}

const toggleInfo = () => {
  infoVisible.value = !infoVisible.value
}

const scrollToHeading = (id: string) => {
  emit('scrollToHeading', id)
}

const formatDate = (date?: Date) => {
  if (!date) return '未知'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}
</script>

<style scoped>
.editor-sidebar {
  width: 280px;
  height: 100%;
  background: var(--el-bg-color);
  border-left: 1px solid var(--el-border-color);
  overflow-y: auto;
}

.sidebar-section {
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--el-bg-color-page);
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.section-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.outline-content,
.stats-content,
.info-content {
  padding: 12px 16px;
}

.empty-state {
  color: var(--el-text-color-placeholder);
  font-size: 12px;
  text-align: center;
  padding: 20px 0;
}

.outline-list {
  max-height: 300px;
  overflow-y: auto;
}

.outline-item {
  padding: 4px 0;
  cursor: pointer;
  font-size: 13px;
  color: var(--el-text-color-regular);
  transition: color 0.2s;
  line-height: 1.4;
}

.outline-item:hover {
  color: var(--el-color-primary);
}

.outline-item.level-1 {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.outline-item.level-2 {
  padding-left: 12px;
}

.outline-item.level-3 {
  padding-left: 24px;
}

.outline-item.level-4 {
  padding-left: 36px;
}

.outline-item.level-5 {
  padding-left: 48px;
}

.outline-item.level-6 {
  padding-left: 60px;
}

.stat-item,
.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 13px;
}

.stat-label,
.info-label {
  color: var(--el-text-color-regular);
}

.stat-value,
.info-value {
  color: var(--el-text-color-primary);
  font-weight: 500;
}
</style>