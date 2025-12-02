<template>
  <div class="page-header">
    <FrontEditorToolbar
      :status="post.status || 'draft'"
      :updated-at="post.updatedAt"
      :publishing="publishing"
      :is-published="post.status === 'published'"
      :post="post"
      :is-publish-mode="isPublishMode"
      :fetch-editor-data="fetchEditorData"
      :apply-editor-settings="applyEditorSettings"
      :ensure-draft-saved="ensureDraftSaved"
      :get-publish-payload="getPublishPayload"
      :get-article-id="getArticleId"
      :show-back-button="true"
      :show-status="false"
      :show-preview-button="true"
      :show-save-draft-button="true"
      :show-settings-button="false"
      :show-publish-button="true"
      :show-share-button="false"
      back-button-text="返回我的文章"
      @back="emit('back')"
      @preview="emit('preview')"
      @save-draft="emit('save-draft')"
      @settings="emit('settings')"
      @publish="emit('publish')"
      @save-settings="emit('save-settings', $event)"
      @publish-success="emit('publish-success', $event)"
      @dialog-close="emit('dialog-close')"
      @share="emit('share')"
    />
  </div>
</template>

<script setup lang="ts">
import FrontEditorToolbar from '@/components/editor/components/toolbar/FrontEditorToolbar.vue'
import type { EditorPost } from '../types'

const {
  post,
  publishing,
  isPublishMode,
  fetchEditorData,
  applyEditorSettings,
  ensureDraftSaved,
  getPublishPayload,
  getArticleId
} = defineProps<{
  post: EditorPost
  publishing: boolean
  isPublishMode: boolean
  fetchEditorData: () => any
  applyEditorSettings: (settings: Record<string, any>) => void
  ensureDraftSaved: () => Promise<void>
  getPublishPayload: () => any
  getArticleId: () => string | null
}>()

const emit = defineEmits<{
  (e: 'back'): void
  (e: 'preview'): void
  (e: 'save-draft'): void
  (e: 'settings'): void
  (e: 'publish'): void
  (e: 'save-settings', settings: Record<string, any>): void
  (e: 'publish-success', payload: { id: string }): void
  (e: 'dialog-close'): void
  (e: 'share'): void
}>()

</script>

<style scoped>
.page-header {
  width: 100%;
}
</style>
