<template>
  <div class="editor-link-obtain">
    <el-dialog
      v-model="visible"
      title="添加链接或上传文件"
      width="520px"
      :before-close="handleClose"
    >
      <div class="dialog-body">
        <el-form @submit.prevent="handleSubmit" class="link-form">
          <el-form-item label="链接地址">
            <el-input
              v-model="linkUrl"
              placeholder="请输入链接地址"
              :disabled="uploading"
              @keyup.enter="handleSubmit"
            />
          </el-form-item>
          <el-form-item label="链接文本">
            <el-input
              v-model="linkText"
              placeholder="请输入链接文本"
              :disabled="uploading"
              @keyup.enter="handleSubmit"
            />
          </el-form-item>
        </el-form>

        <div v-if="showUpload" class="upload-section">
          <div class="upload-label">或上传文件</div>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            drag
            :disabled="uploading"
            class="upload-area"
          >
            <div class="upload-content">
              <el-icon class="upload-icon"><Upload /></el-icon>
              <div class="upload-text">
                <p>{{ uploading ? '正在上传…' : '将文件拖到此处，或点击上传' }}</p>
                <p class="upload-tip">支持图片、视频、音频等常用格式</p>
              </div>
            </div>
          </el-upload>

          <div v-if="uploading" class="upload-progress">
            <el-progress :percentage="uploadProgress" :status="uploadError ? 'exception' : undefined" />
            <div class="progress-actions">
              <el-button size="small" @click="handleAbort">取消上传</el-button>
              <el-button v-if="uploadError" size="small" type="primary" @click="handleRetry">
                重试
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleClose" :disabled="uploading">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :disabled="uploading">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElDialog, ElForm, ElFormItem, ElInput, ElButton, ElUpload, ElIcon, ElProgress } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import type { AttachmentAttr } from '../types'
import { uploadInfraFile } from '@/utils/file'

interface Props {
  visible?: boolean
  showUpload?: boolean
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'link-submit', data: AttachmentAttr): void
  (e: 'file-upload', file: File): void
  (e: 'upload-progress', progress: number): void
  (e: 'upload-error', error: Error): void
  (e: 'upload-success', attachment: AttachmentAttr): void
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  showUpload: false
})

const emit = defineEmits<Emits>()

const linkUrl = ref('')
const linkText = ref('')
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadError = ref(false)
const uploadRef = ref()
const currentFile = ref<File>()
const currentController = ref<AbortController | null>(null)
const currentFileSignature = ref('')

const createFileSignature = (file?: File | null) => {
  if (!file) {
    return ''
  }
  return [file.name, file.size, file.type, file.lastModified ?? 0].join('|')
}

const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const resetFields = () => {
  linkUrl.value = ''
  linkText.value = ''
  currentFile.value = undefined
  uploadProgress.value = 0
  uploadError.value = false
  uploadRef.value?.clearFiles()
}

const handleClose = () => {
  if (uploading.value) {
    handleAbort()
  }
  visible.value = false
  resetFields()
}

const handleSubmit = () => {
  if (!linkUrl.value) return

  emit('link-submit', {
    url: linkUrl.value,
    name: linkText.value || linkUrl.value
  })

  handleClose()
}

const handleFileChange = (file: any) => {
  const rawFile = file.raw as File
  if (!rawFile) return

  const signature = createFileSignature(rawFile)
  if (uploading.value && signature === currentFileSignature.value) {
    return
  }

  currentFileSignature.value = signature
  currentFile.value = rawFile
  emit('file-upload', rawFile)
  // ???????? startUpload ?????????????????????
  // startUpload(rawFile)
}

const startUpload = async (file: File) => {
  const signature = createFileSignature(file)
  if (uploading.value && signature === currentFileSignature.value) {
    return
  }

  uploading.value = true
  uploadProgress.value = 1
  uploadError.value = false

  const controller = new AbortController()
  currentController.value = controller

  try {
    const result = await uploadInfraFile(file, {
      signal: controller.signal,
      onProgress: (progress) => {
        uploadProgress.value = Math.max(progress, 1)
        emit('upload-progress', uploadProgress.value)
      }
    })

    uploadProgress.value = 100
    emit('upload-success', {
      url: result.url,
      name: result.name
    })

    uploading.value = false
    uploadError.value = false
    currentController.value = null
    uploadRef.value?.clearFiles()
    currentFileSignature.value = signature
  } catch (error) {
    if (controller.signal.aborted) {
      uploadProgress.value = 0
      uploadError.value = false
    } else {
      uploadError.value = true
      emit('upload-error', error as Error)
    }
    uploading.value = false
    currentController.value = null
  }
}

const startUploadFromParent = (file: File) => {
  if (!file) {
    return
  }
  const signature = createFileSignature(file)
  if (uploading.value && signature === currentFileSignature.value) {
    return
  }
  currentFileSignature.value = signature
  currentFile.value = file
  startUpload(file)
}

const handleAbort = () => {
  currentController.value?.abort()
  currentController.value = null
  uploading.value = false
  uploadProgress.value = 0
  uploadError.value = false
}

const handleRetry = () => {
  if (currentFile.value) {
    startUpload(currentFile.value)
  }
}

const abort = () => handleAbort()
const retry = () => handleRetry()
const reset = () => {
  handleAbort()
  resetFields()
  currentFileSignature.value = ''
}

defineExpose({
  abort,
  retry,
  reset,
  startUploadFromParent
})
</script>

<style scoped>
.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upload-section {
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 12px;
}

.upload-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.upload-area {
  margin: 12px 0;
}

.upload-content {
  padding: 32px;
  text-align: center;
}

.upload-icon {
  font-size: 42px;
  color: #409eff;
  margin-bottom: 12px;
}

.upload-text p {
  margin: 6px 0;
  color: #606266;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
}

.upload-progress {
  margin-top: 12px;
  text-align: center;
}

.progress-actions {
  margin-top: 10px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
