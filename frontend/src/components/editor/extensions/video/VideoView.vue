<template>
  <InlineBlockBox>
    <div
      class="relative block h-full max-w-full overflow-hidden rounded-md transition-all"
      :class="{
        'rounded ring-2 ring-blue-500': selected,
      }"
      :style="containerStyle"
    >
      <!-- 初始化状态 -->
      <div v-if="initialization" class="video-placeholder">
        <div class="placeholder-content">
          <el-icon class="placeholder-icon"><VideoPlay /></el-icon>
          <p>点击添加视频</p>
          <el-button type="primary" @click="handleAddVideo">
            添加视频
          </el-button>
        </div>
      </div>

      <!-- 视频显示 -->
      <div v-else-if="src" class="group relative">
        <video
          :src="src"
          :controls="controls"
          :autoplay="autoplay"
          :loop="loop"
          playsinline
          preload="metadata"
          class="m-0 rounded-md max-w-full"
          :style="{
            width: node.attrs.width,
            height: node.attrs.height,
          }"
        />
        
        <!-- 悬停工具栏 -->
        <div
          class="absolute left-0 top-0 hidden h-1/4 w-full cursor-pointer justify-end bg-gradient-to-b from-gray-300 to-transparent p-2 ease-in-out group-hover:flex"
        >
          <div class="flex space-x-2">
            <el-button size="small" @click="handleResetInit">
              <el-icon><Refresh /></el-icon>
            </el-button>
            <el-button size="small" @click="deleteNode">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 上传状态 -->
      <div v-else class="upload-status">
        <div class="upload-progress">
          <el-progress :percentage="uploadProgress" />
          <div class="progress-actions">
            <el-button size="small" @click="handleUploadAbort">取消</el-button>
            <el-button v-if="retryFlag" size="small" type="primary" @click="handleUploadRetry">
              重试
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 链接获取组件 -->
    <EditorLinkObtain
      ref="editorLinkObtain"
      v-model:visible="showLinkDialog"
      show-upload
      @link-submit="handleSetExternalLink"
      @file-upload="handleFileUpload"
      @upload-success="handleUploadSuccess"
      @upload-error="handleUploadError"
      @upload-progress="handleUploadProgress"
    />
  </InlineBlockBox>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElIcon, ElButton, ElProgress } from 'element-plus'
import { VideoPlay, Refresh, Delete } from '@element-plus/icons-vue'
import type { NodeViewProps } from '@tiptap/vue-3'
import EditorLinkObtain from '../../components/EditorLinkObtain.vue'
import InlineBlockBox from '../../components/InlineBlockBox.vue'
import type { AttachmentAttr } from '../../types'

const props = defineProps<NodeViewProps>()

const src = computed({
  get: () => props.node?.attrs.src,
  set: (src: string) => {
    props.updateAttributes({ src })
  },
})

const controls = computed(() => props.node.attrs.controls)
const autoplay = computed(() => props.node.attrs.autoplay)
const loop = computed(() => props.node.attrs.loop)

const uploadProgress = ref<number>(0)
const retryFlag = ref<boolean>(false)
const uploading = ref<boolean>(false)
const showLinkDialog = ref<boolean>(false)
const editorLinkObtain = ref()

const initialization = computed(() => {
  return !src.value
})

type AlignmentStyle = {
  marginLeft: string
  marginRight: string
  textAlign: string
}

const createAlignmentStyle = (alignment?: string): AlignmentStyle => {
  switch (alignment) {
    case 'center':
      return {
        marginLeft: 'auto',
        marginRight: 'auto',
        textAlign: 'center',
      }
    case 'right':
      return {
        marginLeft: 'auto',
        marginRight: '0',
        textAlign: 'right',
      }
    default:
      return {
        marginLeft: '0',
        marginRight: 'auto',
        textAlign: 'left',
      }
  }
}

const alignmentStyle = computed<AlignmentStyle>(() =>
  createAlignmentStyle(props.node?.attrs.textAlign)
)

const containerStyle = computed(() => {
  const style: Record<string, string> = {
    ...alignmentStyle.value,
  }
  const width = initialization.value ? '100%' : props.node?.attrs.width
  if (width) {
    style.width = width
  }
  return style
})

const handleAddVideo = () => {
  showLinkDialog.value = true
}

const handleSetExternalLink = (attachment: AttachmentAttr) => {
  props.updateAttributes({
    src: attachment.url,
  })
  showLinkDialog.value = false
}

const handleFileUpload = (file: File) => {
  if (!file) {
    return
  }

  uploading.value = true
  retryFlag.value = false
  uploadProgress.value = 0

  if (!showLinkDialog.value) {
    editorLinkObtain.value?.startUploadFromParent?.(file)
  }
}

const handleUploadSuccess = (attachment: AttachmentAttr) => {
  props.updateAttributes({
    src: attachment.url,
  })
  uploading.value = false
  showLinkDialog.value = false
}

const handleUploadError = () => {
  retryFlag.value = true
  uploading.value = false
}

const handleUploadProgress = (progress: number) => {
  uploadProgress.value = progress
}

const handleUploadRetry = () => {
  editorLinkObtain.value?.retry()
}

const handleUploadAbort = () => {
  editorLinkObtain.value?.abort()
  uploading.value = false
}

const resetUpload = () => {
  const { file } = props.node.attrs
  if (file) {
    props.updateAttributes({
      width: undefined,
      height: undefined,
      file: undefined,
    })
  }
}

const handleResetInit = () => {
  editorLinkObtain.value?.reset()
  props.updateAttributes({
    src: '',
    file: undefined,
  })
  resetUpload()
  showLinkDialog.value = true
}

onMounted(() => {
  // 如果有文件属性，开始上传
  const { file } = props.node.attrs
  if (file) {
    handleFileUpload(file)
  }
})
</script>

<style scoped>
.video-placeholder {
  @apply border-2 border-dashed border-gray-300 rounded-lg p-6 text-center;
  min-height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-content {
  @apply text-gray-500;
}

.placeholder-icon {
  @apply text-4xl mb-4;
}

.upload-status {
  @apply p-4;
}

.upload-progress {
  @apply space-y-2;
}

.progress-actions {
  @apply flex justify-center space-x-2;
}
</style>
