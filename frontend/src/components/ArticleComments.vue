<template>
  <div class="comments-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="decoration-circle decoration-1"></div>
      <div class="decoration-circle decoration-2"></div>
      <div class="decoration-circle decoration-3"></div>
    </div>

    <div class="comments-content">
      <!-- 评论头部 -->
      <div class="comments-header">
        <div class="header-left">
          <h3 class="comments-title">
            <i class="el-icon-chat-dot-round"></i>
            评论讨论
            <span class="comments-count">({{ commentsCount }})</span>
          </h3>
          <p class="comments-subtitle">参与讨论，分享你的观点</p>
        </div>
        
        <!-- 排序选项 -->
        <div class="sort-options">
          <el-radio-group v-model="sortBy" size="small" @change="fetchComments">
            <el-radio-button label="latest">
              <i class="el-icon-time"></i>
              最新
            </el-radio-button>
            <el-radio-button label="oldest">
              <i class="el-icon-sort"></i>
              最早
            </el-radio-button>
            <el-radio-button label="hot">
              <i class="el-icon-star-on"></i>
              最热
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 评论输入框 -->
      <div v-if="userStore.isLoggedIn" class="comment-form">
        <div class="form-header">
          <h4>发表评论</h4>
          <span class="form-subtitle">说说你的想法...</span>
        </div>
        
        <div class="comment-input">
          <div class="user-info">
            <el-avatar 
              :src="userStore.user?.avatar" 
              :size="48"
              class="user-avatar"
            >
              {{ userStore.user?.username?.charAt(0) }}
            </el-avatar>
            <div class="user-details">
              <span class="username">{{ userStore.user?.username }}</span>
              <span class="user-level">活跃用户</span>
            </div>
          </div>
          
          <div class="input-wrapper">
            <el-input
              v-model="newComment"
              type="textarea"
              :rows="4"
              placeholder="写下你的评论，分享你的观点和想法..."
              maxlength="500"
              show-word-limit
              resize="none"
              class="comment-textarea"
            />
            
            <div class="comment-actions">
              <div class="action-tips">
                <el-tooltip content="支持Markdown语法" placement="top">
                  <i class="el-icon-info"></i>
                </el-tooltip>
                <span class="tips-text">支持Markdown语法</span>
              </div>
              
              <div class="action-buttons">
                <el-button 
                  size="small"
                  @click="newComment = ''"
                  :disabled="!newComment.trim()"
                >
                  清空
                </el-button>
                <el-button 
                  type="primary" 
                  size="small"
                  :loading="submitting"
                  :disabled="!newComment.trim()"
                  @click="submitComment"
                  class="submit-btn"
                >
                  <i class="el-icon-s-promotion"></i>
                  发表评论
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 未登录提示 -->
      <div v-else class="login-prompt">
        <div class="login-card">
          <div class="login-icon">
            <i class="el-icon-user"></i>
          </div>
          <div class="login-content">
            <h4>加入讨论</h4>
            <p>登录后即可发表评论，与作者和其他读者互动</p>
            <div class="login-actions">
              <el-button type="primary" @click="goToLogin" class="login-btn">
                <i class="el-icon-right"></i>
                立即登录
              </el-button>
              <el-button @click="goToRegister" class="register-btn">
                注册账号
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 评论列表 -->
      <div class="comments-section">
        <div v-loading="loading" class="comments-list" element-loading-text="加载评论中...">
          <div v-if="comments.length === 0 && !loading" class="empty-comments">
            <div class="empty-icon">
              <i class="el-icon-chat-dot-round"></i>
            </div>
            <h4>暂无评论</h4>
            <p>成为第一个评论的人，分享你的观点吧！</p>
          </div>
          
          <comment-item
            v-for="comment in comments"
            :key="comment.id"
            :comment="comment"
            :article-id="articleId"
            @reply="handleReply"
            @like="handleLike"
            @delete="handleDelete"
            class="comment-item-wrapper"
          />
        </div>

        <!-- 分页 -->
        <div v-if="total > pageSize" class="comments-pagination">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next, total"
            @current-change="handlePageChange"
            class="custom-pagination"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { articleApi } from '@/api'
import { useUserStore } from '@/stores/user'
import CommentItem from './CommentItem.vue'
import type { Comment } from '@/types'

interface Props {
  articleId: string
  commentsCount: number
}

interface Emits {
  commentAdded: []
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const comments = ref<Comment[]>([])
const newComment = ref('')
const sortBy = ref('latest')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 方法
const fetchComments = async () => {
  try {
    loading.value = true
    const response = await articleApi.comments.getList(props.articleId, {
      page: currentPage.value,
      pageSize: pageSize.value,
      sort: sortBy.value
    })
    
    comments.value = response.items
    total.value = response.total
  } catch (error) {
    console.error('获取评论失败:', error)
    ElMessage.error('获取评论失败')
  } finally {
    loading.value = false
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  try {
    submitting.value = true
    await articleApi.comments.create(props.articleId, {
      content: newComment.value.trim()
    })
    
    newComment.value = ''
    ElMessage.success('评论发表成功')
    
    // 重新获取评论列表
    currentPage.value = 1
    await fetchComments()
    
    // 通知父组件评论数量变化
    emit('commentAdded')
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('发表评论失败')
  } finally {
    submitting.value = false
  }
}

const handleReply = async (parentComment: Comment, content: string) => {
  try {
    await articleApi.comments.create(props.articleId, {
      content,
      parentId: parentComment.id
    })
    
    ElMessage.success('回复成功')
    
    // 重新获取评论列表
    await fetchComments()
    
    // 通知父组件评论数量变化
    emit('commentAdded')
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  }
}

const handleLike = async (comment: Comment) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    if (comment.isLiked) {
      await articleApi.comments.unlike(comment.id)
      comment.isLiked = false
      comment.likesCount--
    } else {
      await articleApi.comments.like(comment.id)
      comment.isLiked = true
      comment.likesCount++
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (comment: Comment) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await articleApi.comments.delete(comment.id)
    ElMessage.success('评论已删除')
    
    // 重新获取评论列表
    await fetchComments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchComments()
}

const goToLogin = () => {
  router.push('/auth/login')
}

const goToRegister = () => {
  router.push('/auth/register')
}

// 生命周期
onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comments-container {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 2px;
  margin: 32px 0;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.decoration-1 {
  width: 100px;
  height: 100px;
  top: -50px;
  right: -50px;
  animation-delay: 0s;
}

.decoration-2 {
  width: 60px;
  height: 60px;
  bottom: -30px;
  left: -30px;
  animation-delay: 2s;
}

.decoration-3 {
  width: 80px;
  height: 80px;
  top: 50%;
  right: 10%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

.comments-content {
  background: white;
  border-radius: 18px;
  padding: 32px;
  position: relative;
  z-index: 1;
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 2px solid #f0f2f5;
}

.header-left {
  flex: 1;
}

.comments-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 12px;
}

.comments-title i {
  color: #667eea;
  font-size: 28px;
}

.comments-count {
  color: #667eea;
  font-weight: 600;
}

.comments-subtitle {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.sort-options {
  flex-shrink: 0;
}

.sort-options :deep(.el-radio-button__inner) {
  border-radius: 8px;
  margin: 0 2px;
  border: 1px solid #e4e7ed;
  background: white;
  color: #606266;
  transition: all 0.3s ease;
}

.sort-options :deep(.el-radio-button__inner:hover) {
  border-color: #667eea;
  color: #667eea;
}

.sort-options :deep(.el-radio-button.is-active .el-radio-button__inner) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
}

.comment-form {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 32px;
  border: 1px solid #e8eaff;
}

.form-header {
  margin-bottom: 20px;
}

.form-header h4 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.form-subtitle {
  color: #666;
  font-size: 14px;
}

.comment-input {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  border: 3px solid white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 600;
  color: #1a1a1a;
  font-size: 16px;
}

.user-level {
  font-size: 12px;
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  padding: 2px 8px;
  border-radius: 12px;
  width: fit-content;
}

.input-wrapper {
  flex: 1;
}

.comment-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 2px solid #e8eaff;
  background: white;
  transition: all 0.3s ease;
  font-size: 14px;
  line-height: 1.6;
}

.comment-textarea :deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.action-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #999;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  padding: 8px 20px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.login-prompt {
  margin-bottom: 32px;
}

.login-card {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
  border-radius: 16px;
  padding: 32px;
  text-align: center;
  border: 2px dashed #d0d7ff;
}

.login-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.login-icon i {
  font-size: 28px;
  color: white;
}

.login-content h4 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.login-content p {
  margin: 0 0 24px 0;
  color: #666;
  font-size: 14px;
}

.login-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.login-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 600;
}

.register-btn {
  border: 2px solid #667eea;
  color: #667eea;
  border-radius: 8px;
  padding: 8px 24px;
  font-weight: 600;
}

.comments-section {
  margin-top: 24px;
}

.comments-list {
  min-height: 200px;
}

.empty-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  text-align: center;
}

.empty-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #f0f2f5 0%, #e8eaed 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.empty-icon i {
  font-size: 36px;
  color: #999;
}

.empty-comments h4 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #666;
}

.empty-comments p {
  margin: 0;
  color: #999;
  font-size: 14px;
}

.comment-item-wrapper {
  margin-bottom: 16px;
  animation: slideInUp 0.3s ease-out;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.comments-pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 2px solid #f0f2f5;
}

.custom-pagination :deep(.el-pager li) {
  border-radius: 8px;
  margin: 0 2px;
  transition: all 0.3s ease;
}

.custom-pagination :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .comments-content {
    padding: 20px;
  }
  
  .comments-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .comments-title {
    font-size: 20px;
  }
  
  .sort-options {
    width: 100%;
  }
  
  .sort-options :deep(.el-radio-group) {
    width: 100%;
    display: flex;
  }
  
  .sort-options :deep(.el-radio-button) {
    flex: 1;
  }
  
  .comment-form {
    padding: 16px;
  }
  
  .user-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .action-tips {
    display: none;
  }
  
  .action-buttons {
    width: 100%;
    justify-content: space-between;
  }
  
  .login-card {
    padding: 24px 16px;
  }
  
  .login-actions {
    flex-direction: column;
    align-items: stretch;
  }
}

@media (max-width: 480px) {
  .comments-content {
    padding: 16px;
  }
  
  .comments-title {
    font-size: 18px;
  }
  
  .comment-form {
    padding: 12px;
  }
  
  .login-card {
    padding: 20px 12px;
  }
}
</style>