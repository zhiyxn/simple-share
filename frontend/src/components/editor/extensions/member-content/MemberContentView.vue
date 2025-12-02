<template>
  <NodeViewWrapper
    class="member-content-block"
    :class="{ 'member-content-block--locked': !isVip }"
  >
    <div class="member-content-block__header">
      <el-icon class="member-content-block__header-icon">
        <StarFilled />
      </el-icon>
      <span class="member-content-block__title">会员内容</span>
      <span v-if="isVip" class="member-content-block__status">会员可见</span>
      <span v-else class="member-content-block__status member-content-block__status--locked">
        会员查看所有内容
      </span>
    </div>

    <div class="member-content-block__body">
      <NodeViewContent class="member-content-block__editor" />
      <div v-if="!isVip" class="member-content-block__note">
        <el-icon class="member-content-block__note-icon">
          <Lock />
        </el-icon>
        <div class="member-content-block__note-text">
          当前账号非会员，可正常编辑预览，但发布后仅会员读者可见。
        </div>
        <el-button
          type="primary"
          text
          class="member-content-block__note-action"
          @click="goMembership"
        >
          立即开通会员
        </el-button>
      </div>
    </div>
  </NodeViewWrapper>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NodeViewContent, NodeViewWrapper, type NodeViewProps } from '@tiptap/vue-3'
import { ElButton, ElIcon } from 'element-plus'
import { StarFilled, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { UserType } from '@/types/user'
import { useRouter } from 'vue-router'

defineProps<NodeViewProps>()

const userStore = useUserStore()
const router = useRouter()

const isVip = computed(() => {
  const info = userStore.userInfo
  if (!info || info.userType == null) {
    return false
  }

  const rawType = info.userType

  if (typeof rawType === 'number') {
    return rawType === 3
  }

  const normalized = rawType.toString().trim().toLowerCase()
  return normalized === UserType.VIP || normalized === '3' || normalized === 'vip'
})

const goMembership = () => {
  router.push('/membership')
}
</script>

<style scoped>
.member-content-block {
  @apply relative border border-gray-200 rounded-xl bg-white my-6 overflow-hidden shadow-sm;
}

.member-content-block--locked {
  @apply bg-gray-50 border-dashed border-gray-300;
}

.member-content-block__header {
  @apply flex items-center gap-2 px-4 py-3 border-b border-gray-100 bg-gradient-to-r from-yellow-50 via-white to-white;
}

.member-content-block__header-icon {
  @apply text-yellow-500;
}

.member-content-block__title {
  @apply font-medium text-gray-800;
}

.member-content-block__status {
  @apply text-xs px-2 py-0.5 rounded-full bg-green-100 text-green-700;
}

.member-content-block__status--locked {
  @apply bg-gray-200 text-gray-600;
}

.member-content-block__body {
  @apply relative;
}

.member-content-block__editor {
  @apply px-4 py-4 min-h-[120px] space-y-3;
}

.member-content-block__note {
  @apply mt-3 flex flex-wrap items-center gap-3 rounded-lg border border-dashed border-yellow-300 bg-yellow-50 px-3 py-2 text-sm text-yellow-700;
}

.member-content-block__note-icon {
  @apply text-yellow-500;
}

.member-content-block__note-text {
  @apply leading-relaxed;
}

.member-content-block__note-action {
  @apply text-sm text-yellow-600 hover:text-yellow-500 font-medium;
}
</style>
