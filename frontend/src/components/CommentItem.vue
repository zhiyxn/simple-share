<template>
  <div class="comment-item" :class="{ 'is-reply': isReply }">
    <div class="comment-card">
      <div class="comment-main">
        <!-- 用户头像 -->
        <div class="avatar-wrapper">
          <el-avatar 
            :src="comment.author.avatar" 
            :size="isReply ? 36 : 44"
            class="comment-avatar"
          >
            {{ comment.author.username.charAt(0) }}
          </el-avatar>
          <div class="avatar-status" v-if="comment.author.isOnline"></div>
        </div>

        <!-- 评论内容 -->
        <div class="comment-content">
          <!-- 用户信息和时间 -->
          <div class="comment-header">
            <div class="user-info">
              <span class="username">{{ comment.author.username }}</span>
              <div class="user-badges">
                <el-tag 
                  v-if="comment.author.isAuthor" 
                  type="warning" 
                  size="small"
                  class="author-badge"
                >
                  <i class="el-icon-edit"></i>
                  作者
                </el-tag>
                <el-tag 
                  v-if="comment.author.isVip" 
                  type="success" 
                  size="small"
                  class="vip-badge"
                >
                  <i class="el-icon-star-on"></i>
                  VIP
                </el-tag>
                <el-tag 
                  v-if="comment.author.level" 
                  size="small"
                  class="level-badge"
                >
                  Lv.{{ comment.author.level }}
                </el-tag>
              </div>
            </div>
            
            <div class="comment-meta">
              <span class="time">
                <i class="el-icon-time"></i>
                {{ formatTime(comment.createdAt) }}
              </span>
              <el-dropdown 
                v-if="canDelete"
                trigger="click"
                @command="handleCommand"
                class="more-dropdown"
              >
                <el-button type="text" size="small" class="more-btn">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="delete" class="delete-item">
                      <i class="el-icon-delete"></i>
                      删除评论
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <!-- 回复目标 -->
          <div v-if="comment.parent" class="reply-target">
            <div class="reply-indicator">
              <i class="el-icon-back"></i>
              <span>回复 <strong>@{{ comment.parent.author.username }}</strong></span>
            </div>
            <div class="parent-content">
              <i class="el-icon-chat-dot-round"></i>
              {{ comment.parent.content }}
            </div>
          </div>

          <!-- 评论正文 -->
          <div class="comment-text">
            <div class="text-content">{{ comment.content }}</div>
          </div>

          <!-- 评论操作 -->
          <div class="comment-actions">
            <div class="action-buttons">
              <el-button
                :type="comment.isLiked ? 'primary' : 'default'"
                :class="{ 'is-liked': comment.isLiked }"
                size="small"
                text
                @click="handleLike"
                class="like-btn"
              >
                <i :class="comment.isLiked ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
                <span class="action-text">
                  {{ comment.likesCount > 0 ? comment.likesCount : '点赞' }}
                </span>
              </el-button>
              
              <el-button
                size="small"
                text
                @click="toggleReply"
                class="reply-btn"
              >
                <i class="el-icon-chat-dot-round"></i>
                <span class="action-text">回复</span>
              </el-button>

              <el-button
                size="small"
                text
                @click="shareComment"
                class="share-btn"
              >
                <i class="el-icon-share"></i>
                <span class="action-text">分享</span>
              </el-button>
            </div>

            <div class="comment-stats">
              <span v-if="comment.repliesCount > 0" class="replies-count">
                <i class="el-icon-chat-dot-round"></i>
                {{ comment.repliesCount }} 回复
              </span>
            </div>
          </div>

          <!-- 回复输入框 -->
          <transition name="slide-down">
            <div v-if="showReplyInput" class="reply-input">
              <div class="reply-form">
                <div class="reply-header">
                  <i class="el-icon-edit-outline"></i>
                  <span>回复 @{{ comment.author.username }}</span>
                </div>
                
                <el-input
                  v-model="replyContent"
                  type="textarea"
                  :rows="3"
                  :placeholder="`回复 @${comment.author.username}，说说你的想法...`"
                  maxlength="300"
                  show-word-limit
                  resize="none"
                  class="reply-textarea"
                />
                
                <div class="reply-actions">
                  <div class="reply-tips">
                    <i class="el-icon-info"></i>
                    <span>支持@提及用户</span>
                  </div>
                  
                  <div class="reply-buttons">
                    <el-button size="small" @click="cancelReply">
                      取消
                    </el-button>
                    <el-button 
                      type="primary" 
                      size="small"
                      :loading="replying"
                      :disabled="!replyContent.trim()"
                      @click="submitReply"
                      class="submit-reply-btn"
                    >
                      <i class="el-icon-s-promotion"></i>
                      发布回复
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </div>

    <!-- 子评论 -->
    <transition-group name="comment-list" tag="div" v-if="comment.replies && comment.replies.length > 0" class="replies">
      <comment-item
        v-for="reply in comment.replies"
        :key="reply.id"
        :comment="reply"
        :article-id="articleId"
        :is-reply="true"
        @reply="handleReplyToReply"
        @like="handleReplyLike"
        @delete="handleReplyDelete"
        class="reply-item"
      />
    </transition-group>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  MoreFilled 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { Comment } from '@/types'

interface Props {
  comment: Comment
  articleId: string
  isReply?: boolean
}

interface Emits {
  reply: [comment: Comment, content: string]
  like: [comment: Comment]
  delete: [comment: Comment]
}

const props = withDefaults(defineProps<Props>(), {
  isReply: false
})

const emit = defineEmits<Emits>()

const userStore = useUserStore()

// 响应式数据
const showReplyInput = ref(false)
const replyContent = ref('')
const replying = ref(false)

// 计算属性
const canDelete = computed(() => {
  if (!userStore.isLoggedIn) return false
  
  // 评论作者可以删除自己的评论
  if (userStore.user?.id === props.comment.author.id) return true
  
  // 文章作者可以删除任何评论
  if (userStore.user?.id === props.comment.articleAuthorId) return true
  
  // 管理员可以删除任何评论
  if (userStore.user?.role === 'admin') return true
  
  return false
})

// 方法
const toggleReply = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  showReplyInput.value = !showReplyInput.value
  if (!showReplyInput.value) {
    replyContent.value = ''
  }
}

const cancelReply = () => {
  showReplyInput.value = false
  replyContent.value = ''
}

const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  try {
    replying.value = true
    emit('reply', props.comment, replyContent.value.trim())
    
    // 重置状态
    showReplyInput.value = false
    replyContent.value = ''
  } finally {
    replying.value = false
  }
}

const handleLike = () => {
  emit('like', props.comment)
}

const shareComment = () => {
  // 分享评论功能
  if (navigator.share) {
    navigator.share({
      title: '分享评论',
      text: props.comment.content,
      url: window.location.href + '#comment-' + props.comment.id
    })
  } else {
    // 复制链接到剪贴板
    const url = window.location.href + '#comment-' + props.comment.id
    navigator.clipboard.writeText(url).then(() => {
      ElMessage.success('评论链接已复制到剪贴板')
    })
  }
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'delete':
      emit('delete', props.comment)
      break
  }
}

const handleReplyToReply = (comment: Comment, content: string) => {
  emit('reply', comment, content)
}

const handleReplyLike = (comment: Comment) => {
  emit('like', comment)
}

const handleReplyDelete = (comment: Comment) => {
  emit('delete', comment)
}

const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const week = 7 * day
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else {
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}
</script>

<style scoped>
.comment-item {
  margin-bottom: 20px;
  animation: fadeInUp 0.3s ease-out;
}

.comment-item.is-reply {
  margin-bottom: 16px;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.comment-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #f0f2f5;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.comment-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.comment-card:hover {
  border-color: #e8eaff;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.1);
}

.comment-card:hover::before {
  transform: scaleX(1);
}

.is-reply .comment-card {
  background: linear-gradient(135deg, #f8f9ff 0%, #f5f7ff 100%);
  border: 1px solid #e8eaff;
  padding: 16px;
}

.comment-main {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.comment-avatar {
  border: 3px solid white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
}

.comment-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.3);
}

.avatar-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: #67c23a;
  border: 2px solid white;
  border-radius: 50%;
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.username {
  font-weight: 700;
  color: #1a1a1a;
  font-size: 16px;
  transition: color 0.3s ease;
}

.username:hover {
  color: #667eea;
}

.user-badges {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.author-badge {
  background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
  border: none;
  color: white;
  font-weight: 600;
}

.vip-badge {
  background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);
  border: none;
  color: white;
  font-weight: 600;
}

.level-badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  font-weight: 600;
  font-size: 11px;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time {
  font-size: 13px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.more-btn {
  padding: 6px;
  color: #999;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.more-btn:hover {
  background: #f0f2f5;
  color: #667eea;
}

.reply-target {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 12px;
  border-left: 4px solid #667eea;
}

.reply-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #667eea;
  font-weight: 600;
  margin-bottom: 6px;
}

.parent-content {
  color: #666;
  font-style: italic;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  line-height: 1.4;
}

.comment-text {
  margin-bottom: 16px;
}

.text-content {
  color: #1a1a1a;
  line-height: 1.7;
  font-size: 15px;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-buttons {
  display: flex;
  gap: 20px;
}

.action-buttons .el-button {
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.like-btn.is-liked {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.like-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  transform: translateY(-1px);
}

.reply-btn:hover {
  background: rgba(52, 152, 219, 0.1);
  color: #3498db;
  transform: translateY(-1px);
}

.share-btn:hover {
  background: rgba(46, 204, 113, 0.1);
  color: #2ecc71;
  transform: translateY(-1px);
}

.action-text {
  margin-left: 4px;
}

.comment-stats {
  display: flex;
  align-items: center;
  gap: 12px;
}

.replies-count {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.reply-input {
  margin-top: 16px;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #e8eaff;
}

.reply-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667eea;
  font-weight: 600;
  font-size: 14px;
}

.reply-textarea :deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 2px solid #e8eaff;
  background: white;
  transition: all 0.3s ease;
}

.reply-textarea :deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.reply-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reply-tips {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 12px;
}

.reply-buttons {
  display: flex;
  gap: 8px;
}

.submit-reply-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 6px;
  font-weight: 600;
}

.replies {
  margin-left: 60px;
  margin-top: 20px;
  padding-left: 20px;
  border-left: 3px solid #f0f2f5;
  position: relative;
}

.replies::before {
  content: '';
  position: absolute;
  left: -3px;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.replies:hover::before {
  transform: scaleY(1);
}

.reply-item {
  position: relative;
}

.delete-item {
  color: #f56c6c;
}

/* 过渡动画 */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.comment-list-enter-active,
.comment-list-leave-active {
  transition: all 0.3s ease;
}

.comment-list-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.comment-list-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .comment-card {
    padding: 16px;
  }
  
  .comment-main {
    gap: 12px;
  }
  
  .comment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .user-info {
    width: 100%;
  }
  
  .comment-meta {
    align-self: flex-end;
  }
  
  .action-buttons {
    gap: 16px;
  }
  
  .action-text {
    display: none;
  }
  
  .replies {
    margin-left: 40px;
    padding-left: 16px;
  }
  
  .reply-actions {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .reply-tips {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .comment-card {
    padding: 12px;
  }
  
  .comment-main {
    gap: 8px;
  }
  
  .user-badges {
    gap: 4px;
  }
  
  .action-buttons {
    gap: 12px;
  }
  
  .replies {
    margin-left: 28px;
    padding-left: 12px;
  }
  
  .comment-stats {
    display: none;
  }
}
</style>