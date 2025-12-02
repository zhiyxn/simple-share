<template>
  <div class="tag-cloud">
    <!-- 标题 -->
    <div class="cloud-header">
      <h3 class="cloud-title">
        <el-icon><PriceTag /></el-icon>
        热门标签
      </h3>
      
      <el-button
        v-if="showRefreshBtn"
        type="text"
        size="small"
        :loading="loading"
        @click="refreshTags"
      >
        <el-icon><Refresh /></el-icon>
      </el-button>
    </div>

    <!-- 标签云 -->
    <div class="tags-container" v-loading="loading">
      <div v-if="displayTags.length === 0" class="empty-state">
        <el-empty description="暂无标签" :image-size="60" />
      </div>
      
      <div v-else class="tags-wrapper">
        <transition-group name="tag" tag="div" class="tags-list">
          <div
            v-for="tag in displayTags"
            :key="tag.name"
            class="tag-item"
            :class="{
              'is-active': selectedTags.includes(tag.name),
              'is-hot': tag.isHot,
              'is-new': tag.isNew
            }"
            :style="getTagStyle(tag)"
            @click="toggleTag(tag.name)"
          >
            <!-- 热门标识 -->
            <el-icon v-if="tag.isHot" class="hot-icon">
              <Sunny />
            </el-icon>
            
            <!-- 新标签标识 -->
            <el-icon v-if="tag.isNew" class="new-icon">
              <Star />
            </el-icon>
            
            <!-- 标签名称 -->
            <span class="tag-name">{{ tag.name }}</span>
            
            <!-- 文章数量 -->
            <span class="tag-count">{{ tag.count }}</span>
          </div>
        </transition-group>
      </div>
    </div>

    <!-- 展开/收起 -->
    <div v-if="hasMoreTags" class="cloud-footer">
      <el-button
        type="text"
        size="small"
        @click="toggleExpanded"
      >
        {{ expanded ? '收起' : `展开更多 (${hiddenTagsCount})` }}
        <el-icon>
          <ArrowUp v-if="expanded" />
          <ArrowDown v-else />
        </el-icon>
      </el-button>
    </div>

    <!-- 已选标签 -->
    <div v-if="selectedTags.length > 0" class="selected-tags">
      <div class="selected-header">
        <span class="selected-title">已选标签：</span>
        <el-button
          type="text"
          size="small"
          @click="clearSelection"
        >
          清除全部
        </el-button>
      </div>
      
      <div class="selected-list">
        <el-tag
          v-for="tagName in selectedTags"
          :key="tagName"
          type="primary"
          closable
          @close="removeTag(tagName)"
        >
          {{ tagName }}
        </el-tag>
      </div>
    </div>

    <!-- 搜索标签 -->
    <div v-if="showSearch" class="tag-search">
      <el-input
        v-model="searchQuery"
        placeholder="搜索标签..."
        size="small"
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <!-- 搜索结果 -->
      <div v-if="searchResults.length > 0" class="search-results">
        <div
          v-for="tag in searchResults"
          :key="tag.name"
          class="search-result-item"
          @click="selectTag(tag.name)"
        >
          <span class="result-name">{{ tag.name }}</span>
          <span class="result-count">({{ tag.count }})</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import {
  PriceTag,
  Refresh,
  Sunny,
  Star,
  ArrowUp,
  ArrowDown,
  Search
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { debounce } from 'lodash-es'

interface Tag {
  name: string
  count: number
  color?: string
  isHot?: boolean
  isNew?: boolean
  weight?: number
}

interface Props {
  modelValue?: string[]
  maxDisplay?: number
  showRefreshBtn?: boolean
  showSearch?: boolean
  multiple?: boolean
  colorful?: boolean
}

interface Emits {
  'update:modelValue': [value: string[]]
  change: [tags: string[]]
  tagClick: [tag: string]
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [],
  maxDisplay: 20,
  showRefreshBtn: true,
  showSearch: false,
  multiple: true,
  colorful: true
})

const emit = defineEmits<Emits>()

// 响应式数据
const tags = ref<Tag[]>([])
const selectedTags = ref<string[]>([...props.modelValue])
const expanded = ref(false)
const loading = ref(false)
const searchQuery = ref('')
const searchResults = ref<Tag[]>([])

// 计算属性
const displayTags = computed(() => {
  const filteredTags = searchQuery.value 
    ? tags.value.filter(tag => 
        tag.name.toLowerCase().includes(searchQuery.value.toLowerCase())
      )
    : tags.value
    
  if (expanded.value) {
    return filteredTags
  }
  
  return filteredTags.slice(0, props.maxDisplay)
})

const hasMoreTags = computed(() => {
  return tags.value.length > props.maxDisplay
})

const hiddenTagsCount = computed(() => {
  return Math.max(0, tags.value.length - props.maxDisplay)
})

// 监听器
watch(() => props.modelValue, (newValue) => {
  selectedTags.value = [...newValue]
})

watch(selectedTags, (newValue) => {
  emit('update:modelValue', newValue)
  emit('change', newValue)
}, { deep: true })

// 方法
const toggleTag = (tagName: string) => {
  if (!props.multiple) {
    selectedTags.value = selectedTags.value.includes(tagName) ? [] : [tagName]
  } else {
    const index = selectedTags.value.indexOf(tagName)
    if (index > -1) {
      selectedTags.value.splice(index, 1)
    } else {
      selectedTags.value.push(tagName)
    }
  }
  
  emit('tagClick', tagName)
}

const selectTag = (tagName: string) => {
  if (!selectedTags.value.includes(tagName)) {
    toggleTag(tagName)
  }
  searchQuery.value = ''
  searchResults.value = []
}

const removeTag = (tagName: string) => {
  const index = selectedTags.value.indexOf(tagName)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  }
}

const clearSelection = () => {
  selectedTags.value = []
}

const toggleExpanded = () => {
  expanded.value = !expanded.value
}

const getTagStyle = (tag: Tag) => {
  const baseSize = 12
  const maxSize = 18
  const minSize = 10
  
  // 根据权重计算字体大小
  const weight = tag.weight || 1
  const fontSize = Math.max(minSize, Math.min(maxSize, baseSize + weight * 2))
  
  const style: any = {
    fontSize: `${fontSize}px`
  }
  
  // 如果启用彩色模式
  if (props.colorful && tag.color) {
    style.borderColor = tag.color
    style.color = tag.color
  }
  
  return style
}

const refreshTags = async () => {
  await loadTags()
  ElMessage.success('标签已刷新')
}

const handleSearch = debounce((query: string) => {
  if (!query.trim()) {
    searchResults.value = []
    return
  }
  
  searchResults.value = tags.value
    .filter(tag => 
      tag.name.toLowerCase().includes(query.toLowerCase()) &&
      !selectedTags.value.includes(tag.name)
    )
    .slice(0, 10)
}, 300)

const loadTags = async () => {
  try {
    loading.value = true
    
    // 这里应该调用API获取标签数据
    // const response = await articleApi.getTags()
    // tags.value = response.data
    
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const mockTags: Tag[] = [
      { name: 'Vue.js', count: 156, weight: 5, isHot: true, color: '#4fc08d' },
      { name: 'React', count: 142, weight: 4, isHot: true, color: '#61dafb' },
      { name: 'JavaScript', count: 234, weight: 6, isHot: true, color: '#f7df1e' },
      { name: 'TypeScript', count: 98, weight: 3, color: '#3178c6' },
      { name: 'Node.js', count: 87, weight: 3, color: '#339933' },
      { name: 'Python', count: 76, weight: 2, color: '#3776ab' },
      { name: 'Java', count: 65, weight: 2, color: '#ed8b00' },
      { name: 'Go', count: 54, weight: 2, isNew: true, color: '#00add8' },
      { name: 'Rust', count: 43, weight: 1, isNew: true, color: '#000000' },
      { name: 'Docker', count: 89, weight: 3, color: '#2496ed' },
      { name: 'Kubernetes', count: 67, weight: 2, color: '#326ce5' },
      { name: 'AWS', count: 78, weight: 2, color: '#ff9900' },
      { name: '前端开发', count: 123, weight: 4, isHot: true, color: '#409eff' },
      { name: '后端开发', count: 98, weight: 3, color: '#67c23a' },
      { name: '全栈开发', count: 76, weight: 2, color: '#e6a23c' },
      { name: '移动开发', count: 54, weight: 2, color: '#f56c6c' },
      { name: '数据库', count: 87, weight: 3, color: '#909399' },
      { name: '算法', count: 65, weight: 2, color: '#606266' },
      { name: '设计模式', count: 43, weight: 1, color: '#303133' },
      { name: '性能优化', count: 56, weight: 2, isNew: true, color: '#e6a23c' },
      { name: '微服务', count: 34, weight: 1, color: '#909399' },
      { name: 'GraphQL', count: 28, weight: 1, isNew: true, color: '#e10098' },
      { name: 'WebAssembly', count: 21, weight: 1, isNew: true, color: '#654ff0' },
      { name: '机器学习', count: 45, weight: 1, color: '#ff6b6b' },
      { name: '区块链', count: 32, weight: 1, color: '#4ecdc4' }
    ]
    
    // 按文章数量排序
    tags.value = mockTags.sort((a, b) => b.count - a.count)
    
  } catch (error) {
    console.error('加载标签失败:', error)
    ElMessage.error('加载标签失败')
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadTags()
})
</script>

<style scoped>
.tag-cloud {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.cloud-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 12px;
  border-bottom: 1px solid #f0f0f0;
}

.cloud-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.tags-container {
  padding: 16px 20px;
  min-height: 120px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 120px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
  position: relative;
}

.tag-item:hover {
  background: #ecf5ff;
  border-color: #b3d8ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.tag-item.is-active {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}

.tag-item.is-hot {
  background: linear-gradient(45deg, #ff6b6b, #ffa500);
  border-color: #ff6b6b;
  color: #fff;
  animation: pulse 2s infinite;
}

.tag-item.is-new {
  background: linear-gradient(45deg, #4ecdc4, #44a08d);
  border-color: #4ecdc4;
  color: #fff;
}

.hot-icon,
.new-icon {
  font-size: 12px;
  animation: bounce 1s infinite;
}

.tag-name {
  font-weight: 500;
  white-space: nowrap;
}

.tag-count {
  font-size: 11px;
  opacity: 0.8;
  background: rgba(255, 255, 255, 0.2);
  padding: 1px 4px;
  border-radius: 8px;
  min-width: 16px;
  text-align: center;
}

.tag-item:not(.is-active):not(.is-hot):not(.is-new) .tag-count {
  background: #e4e7ed;
  color: #909399;
}

.cloud-footer {
  padding: 8px 20px 16px;
  text-align: center;
  border-top: 1px solid #f0f0f0;
}

.selected-tags {
  padding: 16px 20px;
  background: #f8f9fa;
  border-top: 1px solid #f0f0f0;
}

.selected-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.selected-title {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.selected-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-search {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  position: relative;
}

.search-results {
  position: absolute;
  top: 100%;
  left: 20px;
  right: 20px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 200px;
  overflow-y: auto;
}

.search-result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.search-result-item:hover {
  background: #f5f7fa;
}

.result-name {
  font-size: 13px;
  color: #303133;
}

.result-count {
  font-size: 12px;
  color: #909399;
}

/* 动画效果 */
.tag-enter-active,
.tag-leave-active {
  transition: all 0.3s ease;
}

.tag-enter-from {
  opacity: 0;
  transform: scale(0.8);
}

.tag-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-3px);
  }
  60% {
    transform: translateY(-2px);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cloud-header {
    padding: 12px 16px 8px;
  }
  
  .tags-container {
    padding: 12px 16px;
  }
  
  .tag-item {
    padding: 4px 8px;
    font-size: 12px;
  }
  
  .selected-tags,
  .tag-search {
    padding: 12px 16px;
  }
}

@media (max-width: 480px) {
  .tags-list {
    gap: 6px;
  }
  
  .tag-item {
    padding: 3px 6px;
    font-size: 11px;
  }
  
  .selected-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>