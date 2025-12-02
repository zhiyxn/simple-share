<template>
  <div v-if="autoSaveState.status !== 'idle'" class="save-indicator">
    <div class="save-status">
      <span v-if="autoSaveState.status === 'saving'" class="status-text saving">
        <el-icon class="is-loading"><Loading /></el-icon>
        自动保存中...
      </span>
      <span v-else-if="autoSaveState.status === 'saved'" class="status-text saved">
        <el-icon><Check /></el-icon>
        已自动保存
        <span v-if="autoSaveState.lastSavedAt" class="save-time">
          {{ formatSaveTime(autoSaveState.lastSavedAt) }}
        </span>
      </span>
      <span v-else-if="autoSaveState.status === 'error'" class="status-text error">
        <el-icon><Close /></el-icon>
        自动保存失败
        <span v-if="autoSaveState.error" class="error-detail">
          {{ autoSaveState.error }}
        </span>
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Check, Close, Loading } from '@element-plus/icons-vue'
import type { AutoSaveState } from '@/composables/useAutoSave'

defineProps<{
  autoSaveState: AutoSaveState
  formatSaveTime: (date: Date) => string
}>()
</script>

<style scoped>
.save-indicator {
  @apply fixed top-4 right-4 z-50;
}

.save-status {
  @apply bg-white rounded-lg shadow-lg px-4 py-2 border;
  background: var(--el-bg-color);
  border-color: var(--el-border-color);
}

.status-text {
  @apply flex items-center gap-2 text-sm font-medium;
}

.status-text.saving {
  color: var(--el-color-primary);
}

.status-text.saved {
  color: var(--el-color-success);
}

.status-text.error {
  color: var(--el-color-danger);
}

.save-time {
  @apply text-xs opacity-75 ml-2;
}

.error-detail {
  @apply text-xs opacity-75 ml-2 block;
}
</style>
