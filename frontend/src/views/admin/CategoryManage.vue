<template>
  <div class="p-6">
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">分类管理</h1>
        <p class="text-gray-600 mt-1">管理文章分类和标签</p>
      </div>
      
      <el-button 
        type="primary" 
        @click="showCreateDialog"
        class="px-6"
      >
        <el-icon class="mr-2"><Plus /></el-icon>
        新建分类
      </el-button>
    </div>
    
    <!-- 分类列表 -->
    <div class="modern-card">
      <el-table 
        v-loading="loading"
        :data="categories" 
        stripe
        class="w-full"
      >
        <!-- 图标 -->
        <el-table-column label="图标" width="80">
          <template #default="{ row }">
            <div class="w-8 h-8 rounded bg-gray-100 flex items-center justify-center">
              <el-icon v-if="row.icon" :class="row.icon" class="text-lg" />
              <el-icon v-else class="text-gray-400"><Folder /></el-icon>
            </div>
          </template>
        </el-table-column>
        
        <!-- 名称 -->
        <el-table-column label="分类名称" prop="name" min-width="150" />
        
        <!-- 描述 -->
        <el-table-column label="描述" prop="description" min-width="200">
          <template #default="{ row }">
            <span class="text-gray-600">{{ row.description || '暂无描述' }}</span>
          </template>
        </el-table-column>
        
        <!-- 文章数量 -->
        <el-table-column label="文章数量" width="100">
          <template #default="{ row }">
            <el-tag type="info" size="small">
              {{ row.articleCount || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 排序 -->
        <el-table-column label="排序" width="100">
          <template #default="{ row }">
            <span class="text-gray-600">{{ row.sort || 0 }}</span>
          </template>
        </el-table-column>
        
        <!-- 状态 -->
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="toggleStatus(row)"
              :loading="row.updating"
            />
          </template>
        </el-table-column>
        
        <!-- 创建时间 -->
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            <div class="text-sm text-gray-600">
              {{ formatDate(row.createdAt) }}
            </div>
          </template>
        </el-table-column>
        
        <!-- 操作 -->
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-button 
                size="small" 
                type="primary" 
                link
                @click="showEditDialog(row)"
              >
                编辑
              </el-button>
              
              <el-button 
                size="small" 
                type="danger" 
                link
                @click="deleteCategory(row)"
                :disabled="row.articleCount > 0"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空状态 -->
      <div v-if="!loading && categories.length === 0" class="text-center py-12">
        <el-empty description="暂无分类">
          <el-button type="primary" @click="showCreateDialog">
            创建第一个分类
          </el-button>
        </el-empty>
      </div>
    </div>
    
    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新建分类'"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入分类名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="分类描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea"
            placeholder="请输入分类描述（可选）"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="图标" prop="icon">
          <el-select 
            v-model="form.icon" 
            placeholder="选择图标（可选）"
            clearable
            class="w-full"
          >
            <el-option 
              v-for="icon in iconOptions" 
              :key="icon.value" 
              :label="icon.label" 
              :value="icon.value"
            >
              <div class="flex items-center gap-2">
                <el-icon :class="icon.value" />
                <span>{{ icon.label }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="排序" prop="sort">
          <el-input-number 
            v-model="form.sort" 
            :min="0" 
            :max="999"
            placeholder="排序值，数字越小越靠前"
            class="w-full"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="enabled">
          <el-switch
            v-model="form.enabled"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">
            取消
          </el-button>
          <el-button 
            type="primary" 
            @click="submitForm"
            :loading="submitting"
          >
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Folder } from '@element-plus/icons-vue'
import { categoryApi, type Category, type CreateCategoryRequest } from '@/api/category'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const categories = ref<Category[]>([])
const formRef = ref<FormInstance>()

// 表单数据
const form = reactive<CreateCategoryRequest>({
  name: '',
  description: '',
  icon: '',
  sort: 0,
  enabled: true
})

// 当前编辑的分类ID
const editingId = ref('')

// 图标选项
const iconOptions = [
  { label: '技术', value: 'ep-cpu' },
  { label: '生活', value: 'ep-coffee-cup' },
  { label: '学习', value: 'ep-reading' },
  { label: '工作', value: 'ep-briefcase' },
  { label: '旅行', value: 'ep-map-location' },
  { label: '美食', value: 'ep-food' },
  { label: '运动', value: 'ep-bicycle' },
  { label: '音乐', value: 'ep-headphones' },
  { label: '电影', value: 'ep-video-camera' },
  { label: '游戏', value: 'ep-game-icons' },
  { label: '摄影', value: 'ep-camera' },
  { label: '设计', value: 'ep-brush' },
  { label: '其他', value: 'ep-more' }
]

// 表单验证规则
const rules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '分类名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述长度不能超过 200 个字符', trigger: 'blur' }
  ],
  sort: [
    { type: 'number', min: 0, max: 999, message: '排序值必须在 0 到 999 之间', trigger: 'blur' }
  ]
}

// 方法
const fetchCategories = async () => {
  try {
    loading.value = true
    const response = await categoryApi.getCategories()
    const data = response.items || []
    categories.value = data.map(item => ({ ...item, updating: false }))
  } catch (error) {
    console.error('Failed to fetch categories:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  editingId.value = ''
  resetForm()
  dialogVisible.value = true
}

const showEditDialog = (category: Category) => {
  isEdit.value = true
  editingId.value = category.id
  
  Object.assign(form, {
    name: category.name,
    description: category.description || '',
    icon: category.icon || '',
    sort: category.sort || 0,
    enabled: category.enabled
  })
  
  dialogVisible.value = true
}

const resetForm = () => {
  Object.assign(form, {
    name: '',
    description: '',
    icon: '',
    sort: 0,
    enabled: true
  })
  
  formRef.value?.clearValidate()
}

const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await categoryApi.update({ ...form, id: editingId.value })
      ElMessage.success('分类更新成功')
    } else {
      await categoryApi.create(form)
      ElMessage.success('分类创建成功')
    }
    
    dialogVisible.value = false
    fetchCategories()
  } catch (error) {
    if (error !== false) { // 不是表单验证错误
      console.error('Submit failed:', error)
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    }
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (category: Category) => {
  try {
    category.updating = true
    await categoryApi.update({
      id: category.id,
      name: category.name,
      description: category.description,
      icon: category.icon,
      sort: category.sort,
      enabled: category.enabled
    })
    
    ElMessage.success(`分类已${category.enabled ? '启用' : '禁用'}`)
  } catch (error) {
    console.error('Toggle status failed:', error)
    category.enabled = !category.enabled // 回滚状态
    ElMessage.error('状态更新失败')
  } finally {
    category.updating = false
  }
}

const deleteCategory = async (category: Category) => {
  if (category.articleCount > 0) {
    ElMessage.warning('该分类下还有文章，无法删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除分类「${category.name}」吗？此操作不可恢复。`,
      '删除确认',
      {
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    await categoryApi.delete(category.id)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete failed:', error)
      ElMessage.error('删除失败')
    }
  }
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
/* 自定义样式 */
</style>