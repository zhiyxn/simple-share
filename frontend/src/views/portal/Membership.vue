<template>
  <div class="membership-page">
    <!-- 动态背景 -->
    <div class="animated-background">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
      <div class="floating-particles">
        <div v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></div>
      </div>
    </div>

    <!-- 主要内容容器 -->
    <div class="main-container">
      <!-- 顶部导航指示器 -->
      <div class="progress-indicator">
        <div class="step" :class="{ active: currentStep >= 1 }">
          <div class="step-circle">1</div>
          <span>选择方案</span>
        </div>
        <div class="step-line"></div>
        <div class="step" :class="{ active: currentStep >= 2 }">
          <div class="step-circle">2</div>
          <span>联系开通</span>
        </div>
        <div class="step-line"></div>
        <div class="step" :class="{ active: currentStep >= 3 }">
          <div class="step-circle">3</div>
          <span>享受服务</span>
        </div>
      </div>

      <!-- 页面标题区域 -->
      <div class="hero-section">
        <div class="hero-content">
          <div class="hero-badge">
             <div class="badge-glow"></div>
             <el-icon class="badge-icon"><Trophy /></el-icon>
             <span>会员特权</span>
           </div>
          
          <h1 class="hero-title">
            <span class="title-line">解锁</span>
            <span class="title-highlight">专属会员</span>
            <span class="title-line">体验</span>
          </h1>
          
          <p class="hero-subtitle">
            加入我们的会员社区，享受无限制阅读、专属内容和优质服务
          </p>
        </div>
      </div>

      <!-- 会员方案选择和开通区域 -->
      <div class="plans-section">
        <div class="section-header">
          <h2 class="section-title">选择适合您的方案</h2>
          <p class="section-subtitle">灵活的订阅选项，满足不同需求</p>
        </div>

        <!-- 方案选择区域 -->
        <div class="plans-container">
          <!-- 月付方案 -->
          <div 
            class="plan-card monthly-plan" 
            :class="{ 
              'selected': selectedPlan === 'monthly',
              'hover': hoveredPlan === 'monthly' 
            }"
            @click="selectPlan('monthly')"
            @mouseenter="hoveredPlan = 'monthly'"
            @mouseleave="hoveredPlan = null"
          >
            <div class="plan-header">
              <div class="plan-type">月付方案</div>
              <div class="plan-price-container">
                <div class="plan-price">
                  <span class="currency">¥</span>
                  <span class="amount">19</span>
                  <span class="period">/月</span>
                </div>
                <div class="plan-description">灵活订阅，随时取消</div>
              </div>
            </div>

            <div class="plan-features">
              <div class="feature-item" v-for="feature in monthlyFeatures" :key="feature">
                <div class="feature-check">
                  <el-icon><Check /></el-icon>
                </div>
                <span class="feature-text">{{ feature }}</span>
              </div>
            </div>

            <div class="plan-action">
              <div class="select-indicator" v-if="selectedPlan === 'monthly'">
                <el-icon><Check /></el-icon>
                <span>已选择</span>
              </div>
            </div>
          </div>

          <!-- 年付方案 -->
          <div 
            class="plan-card yearly-plan" 
            :class="{ 
              'selected': selectedPlan === 'yearly',
              'hover': hoveredPlan === 'yearly' 
            }"
            @click="selectPlan('yearly')"
            @mouseenter="hoveredPlan = 'yearly'"
            @mouseleave="hoveredPlan = null"
          >
            <div class="popular-badge">
              <el-icon><Star /></el-icon>
              <span>推荐</span>
            </div>

            <div class="plan-header">
              <div class="plan-type">年付方案</div>
              <div class="plan-price-container">
                <div class="plan-price">
                  <span class="currency">¥</span>
                  <span class="amount">99</span>
                  <span class="period">/年</span>
                </div>
                <div class="savings-badge">
                  <span>节省 ¥129</span>
                </div>
                <div class="plan-description">超值优惠，一次付费全年享受</div>
              </div>
            </div>

            <div class="plan-features">
              <div class="feature-item" v-for="feature in yearlyFeatures" :key="feature">
                <div class="feature-check">
                  <el-icon><Check /></el-icon>
                </div>
                <span class="feature-text">{{ feature }}</span>
              </div>
            </div>

            <div class="plan-action">
              <div class="select-indicator" v-if="selectedPlan === 'yearly'">
                <el-icon><Check /></el-icon>
                <span>已选择</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 二维码开通区域 -->
        <div class="qr-activation-section" id="qr-section">
          <div class="qr-section-header">
            <h3 class="qr-title">
              <el-icon class="qr-title-icon"><ChatDotRound /></el-icon>
              联系站长开通会员
            </h3>
            <p class="qr-subtitle">{{ ownerRemarkText }}</p>
          </div>
          
          <div class="qr-display-container">
            <div class="qr-code-wrapper">
              <div class="qr-code">
                <img
                  v-if="ownerQrUrl"
                  :src="ownerQrUrl"
                  alt="联系站长二维码"
                  class="qr-image"
                  @error="handleImageError"
                  @load="handleImageLoad"
                />
                <div v-else class="qr-placeholder" :class="{ 'qr-placeholder--loading': ownerLoading }">
                  <span v-if="ownerLoading">二维码加载中...</span>
                  <span v-else>暂未配置二维码</span>
                </div>
              </div>

              <div class="qr-pulse"></div>
            </div>

            <div class="qr-info">
              <div class="qr-info-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="qr-info-content">
                <h4>会员开通指引</h4>
                <p>扫码添加站长，按照指引完成会员开通流程</p>
              </div>
            </div>
          </div>

          <div class="contact-steps">
            <h4 class="steps-title">开通步骤</h4>
            <div class="steps-grid">
              <div class="step-item">
                <div class="step-number">1</div>
                <span>微信扫码添加</span>
              </div>
              <div class="step-item">
                <div class="step-number">2</div>
                <span>提交开通信息</span>
              </div>
              <div class="step-item">
                <div class="step-number">3</div>
                <span>完成支付并开通</span>
              </div>
            </div>

            <div class="highlight-tip">
              <el-icon><Lock /></el-icon>
              <span>全程人工审核，信息安全可靠</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 会员特权展示 -->
      <div class="benefits-showcase">
        <div class="benefits-header">
          <h2 class="benefits-title">会员专享特权</h2>
          <p class="benefits-subtitle">解锁更多专属功能，提升您的使用体验</p>
        </div>

        <div class="benefits-grid">
          <div 
            class="benefit-card" 
            v-for="(benefit, index) in benefits" 
            :key="index"
            :style="{ animationDelay: `${index * 0.1}s` }"
          >
            <div class="benefit-icon">
              <el-icon>
                <component :is="benefit.icon" />
              </el-icon>
            </div>
            <h3 class="benefit-title">{{ benefit.title }}</h3>
            <p class="benefit-description">{{ benefit.description }}</p>
          </div>
        </div>
      </div>

      <!-- 底部CTA -->
      <div class="cta-section" v-if="!selectedPlan">
        <div class="cta-content">
          <h2 class="cta-title">准备好开始了吗？</h2>
          <p class="cta-subtitle">选择适合您的方案，立即开启会员之旅</p>
          <div class="cta-arrow">
            <el-icon><ArrowUp /></el-icon>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  Check,
  Star,
  Trophy,
  ChatDotRound,
  Reading,
  Download,
  Service,
  TrendCharts,
  ArrowUp,
  User,
  Lock
} from '@element-plus/icons-vue'
import { tenantApi } from '@/api/tenant'
import { ensureAbsoluteUrl } from '@/utils/url'

const selectedPlan = ref<'monthly' | 'yearly' | null>(null)
const hoveredPlan = ref<'monthly' | 'yearly' | null>(null)
const currentStep = computed(() => (selectedPlan.value ? 2 : 1))

const monthlyFeatures = ref([
  '全站文章无限阅读',
  '专属会员标识',
  '优先客服支持',
  '每月新增内容抢先体验'
])

const yearlyFeatures = ref([
  '全站文章无限阅读',
  '专属会员标识',
  '优先客服支持',
  '每月新增内容抢先体验',
  '独家资源下载权限',
  '专属技术交流群',
  '一对一技术咨询'
])

const benefits = ref([
  { icon: 'Reading', title: '深度阅读', description: '全站优质内容随心阅读，会员专享内容实时更新' },
  { icon: 'Download', title: '资源下载', description: '精选资料一键获取，模板、源码、课件持续更新' },
  { icon: 'Service', title: '贴心支持', description: '专属客服优先响应，疑难问题即时跟进处理' },
  { icon: 'ChatDotRound', title: '交流社群', description: '加入会员专属圈子，与同好者深入交流与合作' },
  { icon: 'Star', title: '身份标识', description: '尊贵会员标识彰显身份，专享活动优先邀请' },
  { icon: 'TrendCharts', title: '前沿资讯', description: '新功能、新课程、新资源第一时间了解' }
])

const ownerQrCode = ref('')
const ownerRemark = ref('')
const ownerLoading = ref(false)

const ownerQrUrl = computed(() => ensureAbsoluteUrl(ownerQrCode.value))
const ownerRemarkText = computed(() => ownerRemark.value || '扫码二维码，立即开通会员服务')

const fetchOwnerInfo = async () => {
  try {
    ownerLoading.value = true
    const response = await tenantApi.getPublicOwner()
    const data = (response as any)?.data ?? response
    ownerQrCode.value = typeof data?.qrCode === 'string' ? data.qrCode.trim() : ''
    ownerRemark.value = typeof data?.remark === 'string' ? data.remark.trim() : ''
  } catch (error) {
    console.error('获取站长二维码失败:', error)
    ownerQrCode.value = ''
    ownerRemark.value = ''
  } finally {
    ownerLoading.value = false
  }
}

const selectPlan = (plan: 'monthly' | 'yearly') => {
  selectedPlan.value = plan
  setTimeout(() => {
    const qrSection = document.querySelector('#qr-section')
    if (qrSection) {
      qrSection.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }, 100)
}

const getSelectedPlanBenefits = () => {
  if (selectedPlan.value === 'monthly') {
    return ['无限阅读', '专属标识', '优先客服']
  }
  return ['无限阅读', '资源下载', '专属社群', '技术咨询']
}

const getParticleStyle = (index: number) => {
  const delay = Math.random() * 20
  const duration = 15 + Math.random() * 10
  const size = 2 + Math.random() * 4
  return {
    left: `${Math.random() * 100}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    width: `${size}px`,
    height: `${size}px`
  }
}

const handleImageError = () => {
  ownerQrCode.value = ''
}

const handleImageLoad = () => {
  /* no-op */
}

onMounted(() => {
  fetchOwnerInfo()
})
</script>



<style scoped>
/* 基础样式 */
.membership-page {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(135deg, #0f0f23 0%, #1a1a2e 50%, #16213e 100%);
  color: white;
  overflow-x: hidden;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 动态背景 */
.animated-background {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.3;
  animation: float 20s infinite ease-in-out;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #f093fb, #f5576c);
  top: 50%;
  right: 10%;
  animation-delay: -7s;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  bottom: 20%;
  left: 30%;
  animation-delay: -14s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  33% { transform: translateY(-30px) rotate(120deg); }
  66% { transform: translateY(20px) rotate(240deg); }
}

.floating-particles {
  position: absolute;
  width: 100%;
  height: 100%;
}

.particle {
  position: absolute;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  animation: particleFloat linear infinite;
}

@keyframes particleFloat {
  0% {
    transform: translateY(100vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100px) rotate(360deg);
    opacity: 0;
  }
}

/* 主容器 */
.main-container {
  position: relative;
  z-index: 2;
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

/* 进度指示器 */
.progress-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4rem;
  padding: 2rem 0;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  opacity: 0.5;
  transition: all 0.3s ease;
}

.step.active {
  opacity: 1;
}

.step-circle {
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  transition: all 0.3s ease;
}

.step.active .step-circle {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: #667eea;
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.5);
}

.step-line {
  width: 4rem;
  height: 2px;
  background: rgba(255, 255, 255, 0.2);
  margin: 0 1rem;
}

/* 英雄区域 */
.hero-section {
  text-align: center;
  padding: 4rem 0;
  margin-bottom: 4rem;
}

.hero-badge {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 0.75rem 1.5rem;
  border-radius: 2rem;
  margin-bottom: 2rem;
  overflow: hidden;
}

.badge-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { left: -100%; }
  100% { left: 100%; }
}

.badge-icon {
  color: #ffd700;
  font-size: 1.2rem;
}

.hero-title {
  font-size: 4rem;
  font-weight: 800;
  line-height: 1.1;
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.title-highlight {
  background: linear-gradient(135deg, #667eea, #764ba2, #f093fb);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: gradientShift 3s ease-in-out infinite;
}

@keyframes gradientShift {
  0%, 100% { filter: hue-rotate(0deg); }
  50% { filter: hue-rotate(30deg); }
}

.hero-subtitle {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
  margin-bottom: 3rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

/* 方案选择区域 */
.plans-section {
  margin-bottom: 4rem;
}

.section-header {
  text-align: center;
  margin-bottom: 3rem;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.section-subtitle {
  font-size: 1.125rem;
  color: rgba(255, 255, 255, 0.7);
}

.plans-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
  max-width: 800px;
  margin: 0 auto;
}

/* 二维码开通区域 */
.qr-activation-section {
  margin-top: 4rem;
  text-align: center;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 1.5rem;
  padding: 3rem 2rem;
  position: relative;
  overflow: hidden;
}

.qr-activation-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(240, 147, 251, 0.1));
  opacity: 0.5;
}

.qr-section-header {
  position: relative;
  z-index: 2;
  margin-bottom: 2.5rem;
}

.qr-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-size: 1.75rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.qr-title-icon {
  font-size: 1.5rem;
  color: #667eea;
}

.qr-subtitle {
  color: rgba(255, 255, 255, 0.8);
  font-size: 1rem;
  line-height: 1.5;
}

.qr-display-container {
  position: relative;
  z-index: 2;
}

.plan-card {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 1.5rem;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
}

.plan-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(240, 147, 251, 0.1));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.plan-card.hover::before,
.plan-card.selected::before {
  opacity: 1;
}

.plan-card.selected {
  border-color: #667eea;
  box-shadow: 0 0 30px rgba(102, 126, 234, 0.3);
}

.yearly-plan {
  position: relative;
}

.popular-badge {
  position: absolute;
  top: -0.5rem;
  right: 1rem;
  background: linear-gradient(135deg, #f093fb, #f5576c);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 1rem;
  font-size: 0.875rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.4);
}

.plan-header {
  margin-bottom: 2rem;
  position: relative;
  z-index: 1;
}

.plan-type {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: rgba(255, 255, 255, 0.9);
}

.plan-price-container {
  text-align: center;
}

.plan-price {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 0.5rem;
}

.currency {
  font-size: 1.5rem;
  color: rgba(255, 255, 255, 0.7);
}

.amount {
  font-size: 3.5rem;
  font-weight: 800;
  color: white;
  margin: 0 0.25rem;
}

.period {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.7);
}

.savings-badge {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.75rem;
  font-weight: 600;
  display: inline-block;
  margin-bottom: 0.5rem;
}

.plan-description {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
}

.plan-features {
  margin-bottom: 2rem;
  position: relative;
  z-index: 1;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.feature-check {
  width: 1.5rem;
  height: 1.5rem;
  background: linear-gradient(135deg, #10b981, #059669);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.75rem;
  flex-shrink: 0;
}

.feature-text {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.875rem;
}

.plan-action {
  position: relative;
  z-index: 1;
}

.select-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 0.75rem;
  border-radius: 0.75rem;
  font-weight: 600;
}

/* 开通方式区域 */
.activation-section {
  margin-bottom: 4rem;
  animation: slideInUp 0.6s ease-out;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.activation-container {
  max-width: 900px;
  margin: 0 auto;
}

.activation-header {
  text-align: center;
  margin-bottom: 3rem;
}

.activation-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.activation-subtitle {
  font-size: 1.125rem;
  color: rgba(255, 255, 255, 0.7);
}

.activation-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  align-items: start;
}

/* 选中方案展示 */
.selected-plan-display {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 1.5rem;
  padding: 2rem;
}

.plan-summary {
  margin-bottom: 1.5rem;
}

.plan-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.plan-name {
  font-size: 1.25rem;
  font-weight: 600;
}

.plan-price-display {
  font-size: 1.5rem;
  font-weight: 700;
  color: #667eea;
}

.plan-benefits {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.benefit-tag {
  background: rgba(102, 126, 234, 0.2);
  color: #667eea;
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.75rem;
  font-weight: 500;
}

/* 二维码区域 */
.qr-section {
  display: flex;
  justify-content: center;
}

.qr-container {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 1.5rem;
  padding: 2rem;
  text-align: center;
  max-width: 300px;
}

.qr-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

.qr-icon {
  color: #667eea;
  font-size: 1.25rem;
}

.qr-code-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 1.5rem;
}

.qr-code {
  position: relative;
  z-index: 2;
}

.qr-svg {
  display: block;
  border-radius: 12px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
}

.qr-image {
  display: block;
  width: 180px;
  height: 180px;
  border-radius: 12px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
  object-fit: cover;
  transition: all 0.3s ease;
}

.qr-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 180px;
  height: 180px;
  border-radius: 12px;
  border: 2px dashed rgba(255, 255, 255, 0.6);
  background: rgba(15, 23, 42, 0.35);
  color: rgba(255, 255, 255, 0.85);
  font-size: 0.95rem;
  text-align: center;
  padding: 1rem;
  transition: all 0.3s ease;
}

.qr-placeholder--loading {
  border-color: rgba(102, 126, 234, 0.7);
  color: rgba(102, 126, 234, 0.95);
}

.qr-image:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.4);
}

.qr-pulse {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 200px;
  border: 2px solid #667eea;
  border-radius: 12px;
  opacity: 0.6;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.6;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.05);
    opacity: 0.3;
  }
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.6;
  }
}

.contact-instructions {
  text-align: left;
}

.instruction-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
}

.instruction-number {
  width: 1.5rem;
  height: 1.5rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  flex-shrink: 0;
}

/* 联系步骤 */
.contact-steps {
  margin-top: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 300px;
  margin-left: auto;
  margin-right: auto;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.step-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(102, 126, 234, 0.3);
}

.step-number {
  width: 2rem;
  height: 2rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: 600;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.step-item span {
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9rem;
  line-height: 1.4;
}

/* 会员特权展示 */
.benefits-showcase {
  margin-bottom: 4rem;
}

.benefits-header {
  text-align: center;
  margin-bottom: 3rem;
}

.benefits-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.benefits-subtitle {
  font-size: 1.125rem;
  color: rgba(255, 255, 255, 0.7);
}

.benefits-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.benefit-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 1.5rem;
  padding: 2rem;
  text-align: center;
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease-out both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.benefit-card:hover {
  transform: translateY(-8px);
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
}

.benefit-icon {
  width: 4rem;
  height: 4rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
  font-size: 1.5rem;
  color: white;
}

.benefit-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: white;
}

.benefit-description {
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
  font-size: 0.875rem;
}

/* 底部CTA */
.cta-section {
  text-align: center;
  padding: 4rem 0;
}

.cta-content {
  max-width: 500px;
  margin: 0 auto;
}

.cta-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1rem;
}

.cta-subtitle {
  font-size: 1.125rem;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 2rem;
}

.cta-arrow {
  font-size: 2rem;
  color: #667eea;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-container {
    padding: 1rem;
  }

  .hero-title {
    font-size: 2.5rem;
  }

  .plans-container {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .qr-activation-section {
    margin-top: 3rem;
    padding: 2rem 1.5rem;
  }

  .qr-title {
    font-size: 1.5rem;
  }

  .contact-steps {
    max-width: 100%;
  }

  .benefits-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .progress-indicator {
    flex-direction: column;
    gap: 1rem;
  }

  .step-line {
    width: 2px;
    height: 2rem;
    margin: 0;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 2rem;
  }

  .plan-card {
    padding: 1.5rem;
  }

  .qr-activation-section {
    margin-top: 2rem;
    padding: 1.5rem 1rem;
  }

  .qr-title {
    font-size: 1.25rem;
  }

  .qr-svg,
  .qr-image,
  .qr-placeholder {
    width: 150px;
    height: 150px;
  }

  .qr-pulse {
    width: 170px;
    height: 170px;
  }

  .step-item {
    padding: 0.5rem 0.75rem;
    gap: 0.75rem;
  }

  .step-number {
    width: 1.75rem;
    height: 1.75rem;
    font-size: 0.75rem;
  }
}
</style>
