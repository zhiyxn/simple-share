<template>
  <InlineBlockBox class="secure-resource-wrapper">
    <div class="secure-resource-block secure-resource-block--editor">
      <div class="secure-resource-block__header">
        <div class="secure-resource-block__heading">
          <span class="secure-resource-block__icon">🔐</span>
          <div class="secure-resource-block__title-group">
            <span class="secure-resource-block__title">{{ displayTitle }}</span>
            <span class="secure-resource-block__type">{{ displayType }}</span>
          </div>
        </div>
        <div v-if="editable" class="secure-resource-block__actions">
          <el-button
            size="small"
            text
            type="primary"
            @click.stop="openEditor"
          >
            编辑
          </el-button>
        </div>
      </div>

      <div class="secure-resource-block__body">
        <div class="secure-resource-block__field">
          <span class="secure-resource-block__label">访问链接</span>
          <div :class="['secure-resource-block__value', { 'secure-resource-block__value--empty': !hasUrl }]">
            <template v-if="hasUrl">
              <a
                :href="resourceUrl"
                target="_blank"
                rel="noopener noreferrer"
                class="secure-resource-block__link"
                @click.stop
              >
                {{ resourceUrl }}
              </a>
              <el-button
                class="secure-resource-block__copy"
                type="primary"
                size="small"
                text
                @click.stop="copyToClipboard(resourceUrl, '链接已复制')"
              >
                复制链接
              </el-button>
            </template>
            <span v-else class="secure-resource-block__placeholder">{{ placeholderText }}</span>
          </div>
        </div>

        <div class="secure-resource-block__field">
          <span class="secure-resource-block__label">提取码 / 密码</span>
          <div :class="['secure-resource-block__value', { 'secure-resource-block__value--empty': !hasSecret }]">
            <template v-if="hasSecret">
              <span class="secure-resource-block__secret">{{ resourceSecret }}</span>
              <el-button
                class="secure-resource-block__copy"
                type="primary"
                size="small"
                text
                @click.stop="copyToClipboard(resourceSecret, '密码已复制')"
              >
                复制密码
              </el-button>
            </template>
            <span v-else class="secure-resource-block__placeholder">{{ placeholderText }}</span>
          </div>
        </div>
      </div>

      <div v-if="displayNotice" class="secure-resource-block__notice">
        {{ displayNotice }}
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="编辑重要链接"
      width="420px"
      :append-to-body="true"
    >
      <el-form
        :model="form"
        label-width="90px"
      >
        <el-form-item label="名称">
          <el-input
            v-model="form.title"
            placeholder="例如：资料合集链接"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-input
            v-model="form.type"
            placeholder="例如：百度网盘 / 夸克"
          />
        </el-form-item>
        <el-form-item label="访问链接">
          <el-input
            v-model="form.url"
            placeholder="https://..."
          />
        </el-form-item>
        <el-form-item label="提取码">
          <el-input
            v-model="form.secret"
            placeholder="若无可留空"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="form.notice"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 4 }"
            placeholder="补充说明信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </InlineBlockBox>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElMessage } from 'element-plus'
import type { NodeViewProps } from '@tiptap/vue-3'
import InlineBlockBox from '../../components/InlineBlockBox.vue'
import {
  SECURE_RESOURCE_DEFAULTS,
  SECURE_RESOURCE_PLACEHOLDER_TEXT,
  encodeSecureResourceConfig,
  normalizeSecureResourceConfig,
} from '@/utils/secureResource'

const emit = defineEmits(['copy'])

const props = defineProps<NodeViewProps>()

const dialogVisible = ref(false)
const form = reactive({
  title: props.node.attrs.title || SECURE_RESOURCE_DEFAULTS.title,
  type: props.node.attrs.type || SECURE_RESOURCE_DEFAULTS.type,
  url: props.node.attrs.url || '',
  secret: props.node.attrs.secret || '',
  notice: props.node.attrs.notice || SECURE_RESOURCE_DEFAULTS.notice,
})

const editable = computed(() => props.editor?.isEditable ?? false)

const resourceUrl = computed(() => (props.node.attrs.url || '').trim())
const resourceSecret = computed(() => (props.node.attrs.secret || '').trim())
const resourceNotice = computed(() => (props.node.attrs.notice || '').trim())

const hasUrl = computed(() => resourceUrl.value.length > 0)
const hasSecret = computed(() => resourceSecret.value.length > 0)

const displayTitle = computed(
  () => props.node.attrs.title || SECURE_RESOURCE_DEFAULTS.title
)
const displayType = computed(
  () => props.node.attrs.type || SECURE_RESOURCE_DEFAULTS.type
)

const placeholderText = SECURE_RESOURCE_PLACEHOLDER_TEXT

const displayNotice = computed(() => {
  const notice = resourceNotice.value
  const isDefault = notice === SECURE_RESOURCE_DEFAULTS.notice

  if (notice && (!isDefault || (!hasUrl.value && !hasSecret.value))) {
    return notice
  }

  if (!notice && !hasUrl.value && !hasSecret.value) {
    return SECURE_RESOURCE_DEFAULTS.notice
  }

  return ''
})

function openEditor() {
  if (!editable.value) {
    return
  }
  form.title = props.node.attrs.title || form.title
  form.type = props.node.attrs.type || form.type
  form.url = props.node.attrs.url || form.url
  form.secret = props.node.attrs.secret || form.secret
  form.notice = props.node.attrs.notice || form.notice
  dialogVisible.value = true
}

function handleSubmit() {
  const normalized = normalizeSecureResourceConfig({
    title: form.title,
    type: form.type,
    url: form.url,
    secret: form.secret,
    notice: form.notice,
  })

  props.updateAttributes({
    title: normalized.title,
    type: normalized.type,
    url: normalized.url,
    secret: normalized.secret,
    notice: normalized.notice,
    config: encodeSecureResourceConfig(normalized),
  })
  dialogVisible.value = false
}

async function copyToClipboard(value: string, successMessage: string) {
  if (!value) {
    return
  }

  try {
    if (navigator?.clipboard?.writeText) {
      await navigator.clipboard.writeText(value)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = value
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      textarea.style.pointerEvents = 'none'
      document.body.appendChild(textarea)
      textarea.focus()
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    ElMessage.success(successMessage)
    emit('copy', value)
  } catch (error) {
    console.warn('Failed to copy', error)
    ElMessage.error('复制失败，请手动复制')
  }
}
</script>

<style scoped>
.secure-resource-wrapper {
  display: block;
}

.secure-resource-block {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 16px;
  border-radius: 14px;
  border: 1px solid rgba(59, 130, 246, 0.25);
  background: linear-gradient(135deg, rgba(248, 250, 255, 1), rgba(255, 255, 255, 0.9));
  padding: 18px 20px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.05);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.secure-resource-block--editor:hover {
  box-shadow: 0 12px 32px rgba(30, 64, 175, 0.12);
  transform: translateY(-1px);
}

.secure-resource-block__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.secure-resource-block__heading {
  display: flex;
  align-items: center;
  gap: 12px;
}

.secure-resource-block__icon {
  font-size: 22px;
  line-height: 1;
}

.secure-resource-block__title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.secure-resource-block__title {
  font-weight: 600;
  font-size: 15px;
  color: #1f2937;
}

.secure-resource-block__type {
  font-size: 12px;
  color: #64748b;
  letter-spacing: 0.2px;
}

.secure-resource-block__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.secure-resource-block__body {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 14px 16px;
  border: 1px dashed rgba(59, 130, 246, 0.35);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.92);
}

.secure-resource-block__field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.secure-resource-block__label {
  font-size: 12px;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.secure-resource-block__value {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  color: #1f2937;
  word-break: break-all;
}

.secure-resource-block__value--empty {
  color: #9ca3af;
}

.secure-resource-block__link {
  color: #2563eb;
  text-decoration: none;
  font-weight: 500;
}

.secure-resource-block__link:hover {
  text-decoration: underline;
}

.secure-resource-block__copy {
  padding: 0;
  font-weight: 500;
}

.secure-resource-block__secret {
  font-weight: 600;
  letter-spacing: 0.4px;
}

.secure-resource-block__placeholder {
  font-style: italic;
  color: #cbd5f5;
}

.secure-resource-block__notice {
  font-size: 12px;
  line-height: 1.5;
  color: #1d4ed8;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.18), rgba(30, 64, 175, 0.08));
  border-radius: 10px;
  padding: 10px 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 640px) {
  .secure-resource-block {
    padding: 16px;
  }

  .secure-resource-block__body {
    padding: 12px 14px;
  }
}
</style>
