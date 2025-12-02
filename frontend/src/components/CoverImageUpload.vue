<template>
  <div class="cover-image-upload">
    <el-form-item label="文章封面">
      <div class="upload-container">
        <el-upload
          ref="uploadRef"
          class="cover-uploader"
          :show-file-list="false"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :on-progress="handleUploadProgress"
          :http-request="customUpload"
          accept="image/*"
        >
          <div v-if="imageUrl" class="cover-preview">
            <img :src="previewUrl || imageUrl" alt="封面图片" />
            <div class="cover-actions">
              <el-button type="primary" size="small" @click.stop="replaceImage">
                <el-icon><Edit /></el-icon>
                替换
              </el-button>
              <el-button type="danger" size="small" @click.stop="removeImage">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
          <div v-else class="cover-placeholder">
            <el-icon class="cover-icon" :size="32"><Plus /></el-icon>
            <div class="cover-text">点击上传封面图片</div>
            <div class="cover-hint">建议尺寸：16:9，支持 jpg、png、gif 格式</div>
          </div>
        </el-upload>

        <div v-if="uploading" class="upload-progress">
          <el-progress :percentage="uploadProgress" :show-text="false" />
          <div class="progress-text">正在上传... {{ uploadProgress }}%</div>
        </div>
      </div>
    </el-form-item>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { uploadInfraFile, buildFileUrl } from '@/utils/file'
import type { UploadRequestOptions, UploadUserFile } from 'element-plus'

interface Props {
  modelValue?: string
}

interface Emits {
  (e: 'update:modelValue', value: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const uploadRef = ref()
const uploading = ref(false)
const uploadProgress = ref(0)

const imageUrl = computed({
  get: () => props.modelValue || '',
  set: (value: string) => emit('update:modelValue', value)
})

// 预览图片URL（支持动态域名拼接）
const previewUrl = ref('')

// 构建预览URL
const buildPreviewUrl = async () => {
  if (!props.modelValue) {
    previewUrl.value = ''
    return
  }

  console.log('CoverImageUpload - 构建预览URL，原始路径:', props.modelValue)

  // 如果已经是完整URL，直接使用
  if (props.modelValue.startsWith('http://') || props.modelValue.startsWith('https://')) {
    previewUrl.value = props.modelValue
    console.log('CoverImageUpload - 使用完整URL:', previewUrl.value)
    return
  }

  // 否则使用 buildFileUrl 动态拼接域名
  try {
    previewUrl.value = await buildFileUrl(props.modelValue)
    console.log('CoverImageUpload - 构建的预览URL:', previewUrl.value)
  } catch (error) {
    console.warn('构建封面预览URL失败:', error)
    previewUrl.value = props.modelValue
    console.log('CoverImageUpload - 使用原始路径作为预览URL:', previewUrl.value)
  }
}

// 上传前检查
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }

  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }

  return true
}

// 自定义上传方法
const customUpload = async (options: UploadRequestOptions) => {
  try {
    uploading.value = true
    uploadProgress.value = 0

    const result = await uploadInfraFile(options.file as File, {
      onProgress: (progress: number) => {
        uploadProgress.value = progress
      }
    })

    // 返回格式要符合 Element Plus 的要求
    options.onSuccess(result)
  } catch (error: any) {
    console.error('封面上传失败:', error)
    ElMessage.error(error.message || '封面上传失败')
    options.onError(error)
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

// 上传成功处理
const handleUploadSuccess = (response: any) => {
  console.log('封面上传成功:', response)

  // 使用返回的 path 字段（相对路径），不要使用 url 字段
  // path 是相对路径如：upload/2025/11/26/xxx.png
  // url 是域名+路径如：aipic.easyjx.cn/upload/2025/11/26/xxx.png
  if (response.path) {
    // 确保保存的是相对路径，用于存储到数据库
    const relativePath = response.path.startsWith('/') ? response.path : `/${response.path}`
    imageUrl.value = relativePath
    console.log('保存的相对路径:', relativePath)
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error('上传失败：未返回文件路径')
  }
}

// 上传失败处理
const handleUploadError = (error: any) => {
  console.error('封面上传错误:', error)
  ElMessage.error('封面上传失败，请重试')
}

// 上传进度处理
const handleUploadProgress = (event: any) => {
  uploadProgress.value = Math.round(event.percent || 0)
}

// 替换图片
const replaceImage = () => {
  uploadRef.value?.$el.querySelector('input')?.click()
}

// 删除图片
const removeImage = () => {
  imageUrl.value = ''
  ElMessage.success('封面已删除')
}

// 监听外部值变化，确保预览正确
watch(() => props.modelValue, async (newValue) => {
  console.log('CoverImageUpload - modelValue 变化:', newValue)
  await buildPreviewUrl()
}, { immediate: true })

// 暴露方法给父组件
defineExpose({
  buildPreviewUrl
})
</script>

<style scoped>
.cover-image-upload {
  width: 100%;
}

.upload-container {
  position: relative;
}

.cover-uploader {
  border: 2px dashed var(--el-border-color-lighter);
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 100%;
  max-width: 400px;
}

.cover-uploader:hover {
  border-color: var(--el-color-primary);
}

.cover-preview {
  position: relative;
  width: 100%;
  height: 225px;
  overflow: hidden;
  border-radius: 6px;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.cover-preview:hover .cover-actions {
  opacity: 1;
}

.cover-actions .el-button {
  padding: 8px 16px;
  font-size: 12px;
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 225px;
  color: var(--el-text-color-secondary);
}

.cover-icon {
  color: var(--el-text-color-placeholder);
  margin-bottom: 16px;
}

.cover-text {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin-bottom: 8px;
}

.cover-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

.upload-progress {
  margin-top: 12px;
  width: 100%;
  max-width: 400px;
}

.progress-text {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-align: center;
  margin-top: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cover-uploader {
    max-width: 100%;
  }

  .upload-progress {
    max-width: 100%;
  }
}
</style>