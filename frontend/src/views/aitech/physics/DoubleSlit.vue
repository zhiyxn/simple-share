<template>
  <div class="double-slit-container">
    <AiTechBackButton />
    <!-- Header -->
    <div class="header">
      <div class="left-nav">
        <button class="back-btn" @click="$router.push('/aitech')">
          <el-icon><ArrowLeft /></el-icon> 返回教程
        </button>
        <h1>光的双缝干涉侦探社</h1>
      </div>
      <div class="header-controls">
        <div class="mode-switch">
          <button 
            :class="['mode-btn', { active: mode === 'wave' }]"
            @click="setMode('wave')"
          >
            <el-icon><Share /></el-icon> 波动性 (Wave)
          </button>
          <button 
            :class="['mode-btn', { active: mode === 'particle' }]"
            @click="setMode('particle')"
          >
            <el-icon><Grid /></el-icon> 粒子性 (Particle)
          </button>
        </div>
        <button class="action-btn" @click="resetParams">
          <el-icon><Refresh /></el-icon> 重置参数
        </button>
      </div>
    </div>

    <div class="content-layout">
      <!-- Left: Controls -->
      <div class="controls-panel">
        <div class="control-group card">
          <h3>实验参数</h3>
          
          <div class="slider-item">
            <div class="slider-header">
              <span>波长 (λ) - 颜色</span>
              <span class="val" :style="{ color: wavelengthColor }">{{ params.wavelength }} nm</span>
            </div>
            <div class="color-bar" :style="{ background: spectrumGradient }"></div>
            <el-slider 
              v-model="params.wavelength" 
              :min="380" 
              :max="750" 
              :step="1" 
              @input="onParamChange" 
            />
          </div>

          <div class="slider-item">
            <div class="slider-header">
              <span>双缝间距 (d)</span>
              <span class="val">{{ params.slitDistance.toFixed(2) }} mm</span>
            </div>
            <el-slider 
              v-model="params.slitDistance" 
              :min="0.05" 
              :max="0.5" 
              :step="0.01" 
              @input="onParamChange" 
            />
          </div>

          <div class="slider-item">
            <div class="slider-header">
              <span>屏缝距离 (L)</span>
              <span class="val">{{ params.screenDistance.toFixed(1) }} m</span>
            </div>
            <el-slider 
              v-model="params.screenDistance" 
              :min="1.0" 
              :max="5.0" 
              :step="0.1" 
              @input="onParamChange" 
            />
          </div>

          <div class="info-box" v-if="mode === 'particle'">
            <p>光子/电子数: {{ particleCount }}</p>
            <button class="clear-btn" @click="clearParticles">清除累积</button>
          </div>
        </div>

        <!-- Detective Panel (Hover Info) -->
        <transition name="fade">
          <div class="detective-panel card" v-if="hoverInfo">
            <h3>🔍 侦探分析报告</h3>
            <div class="analysis-content">
              <div class="analysis-row">
                <span class="label">屏幕位置 (y):</span>
                <span class="value">{{ hoverInfo.y.toFixed(2) }} mm</span>
              </div>
              <div class="analysis-row">
                <span class="label">光程差 (Δr):</span>
                <span class="value">{{ hoverInfo.pathDiff.toFixed(1) }} nm</span>
              </div>
              <div class="analysis-row">
                <span class="label">相位差:</span>
                <span class="value">{{ hoverInfo.phaseDiff.toFixed(2) }}π</span>
              </div>
              <div class="divider"></div>
              <div class="verdict" :class="hoverInfo.isConstructive ? 'bright' : 'dark'">
                <span class="icon">{{ hoverInfo.isConstructive ? '✨' : '🌑' }}</span>
                <div class="text">
                  <h4>{{ hoverInfo.isConstructive ? '亮纹 (加强)' : '暗纹 (抵消)' }}</h4>
                  <p>{{ hoverInfo.reason }}</p>
                </div>
              </div>
            </div>
          </div>
          <div class="detective-panel card empty" v-else>
            <h3>🕵️‍♂️ 侦探待命中...</h3>
            <p>将鼠标悬停在右侧干涉图样上，查看光子到达该点的“身世之谜”。</p>
          </div>
        </transition>
      </div>

      <!-- Right: Experiment Canvas -->
      <div class="experiment-stage card">
        <canvas 
          ref="canvas" 
          class="main-canvas"
          @mousemove="onCanvasHover"
          @mouseleave="onCanvasLeave"
        ></canvas>
        
        <!-- Labels -->
        <div class="stage-labels">
          <div class="label source">光源</div>
          <div class="label slits">双缝挡板</div>
          <div class="label screen">探测屏 (Hover Me)</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue'
import { ArrowLeft, Refresh, Share, Grid } from '@element-plus/icons-vue'
import { ElSlider, ElIcon } from 'element-plus'
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue'

// --- Types ---
interface HoverInfo {
  y: number
  pathDiff: number
  phaseDiff: number
  isConstructive: boolean
  reason: string
  intensity: number
}

// --- State ---
const canvas = ref<HTMLCanvasElement | null>(null)
const mode = ref<'wave' | 'particle'>('wave')
const animationId = ref(0)
const hoverInfo = ref<HoverInfo | null>(null)
const particleCount = ref(0)

const params = reactive({
  wavelength: 550, // nm (Green)
  slitDistance: 0.2, // mm
  screenDistance: 2.0 // m
})

// Particles buffer
const particles: { x: number, y: number, color: string }[] = []
const MAX_PARTICLES = 5000

// --- Constants & Utilities ---
const spectrumGradient = 'linear-gradient(to right, #8b00ff, #0000ff, #00ff00, #ffff00, #ff7f00, #ff0000)'

// Wavelength to RGB approximation
const nmToRGB = (wavelength: number) => {
  let R, G, B, alpha;
  if (wavelength >= 380 && wavelength < 440) {
    R = -1 * (wavelength - 440) / (440 - 380);
    G = 0;
    B = 1;
  } else if (wavelength >= 440 && wavelength < 490) {
    R = 0;
    G = (wavelength - 440) / (490 - 440);
    B = 1;
  } else if (wavelength >= 490 && wavelength < 510) {
    R = 0;
    G = 1;
    B = -1 * (wavelength - 510) / (510 - 490);
  } else if (wavelength >= 510 && wavelength < 580) {
    R = (wavelength - 510) / (580 - 510);
    G = 1;
    B = 0;
  } else if (wavelength >= 580 && wavelength < 645) {
    R = 1;
    G = -1 * (wavelength - 645) / (645 - 580);
    B = 0;
  } else if (wavelength >= 645 && wavelength <= 750) {
    R = 1;
    G = 0;
    B = 0;
  } else {
    R = 0; G = 0; B = 0;
  }

  // Intensity correction
  if (wavelength > 700) alpha = 0.3 + 0.7 * (750 - wavelength) / (750 - 700);
  else if (wavelength < 420) alpha = 0.3 + 0.7 * (wavelength - 380) / (420 - 380);
  else alpha = 1;

  return `rgba(${Math.round(R * 255 * alpha)}, ${Math.round(G * 255 * alpha)}, ${Math.round(B * 255 * alpha)}, 1)`
}

const wavelengthColor = computed(() => nmToRGB(params.wavelength))

// --- Logic ---

const setMode = (newMode: 'wave' | 'particle') => {
  mode.value = newMode
  if (newMode === 'particle') {
    // Keep existing particles if any, or maybe not clear them to show contrast?
    // Let's keep them to allow toggling back and forth to see density match pattern
  }
}

const resetParams = () => {
  params.wavelength = 550
  params.slitDistance = 0.2
  params.screenDistance = 2.0
  clearParticles()
}

const clearParticles = () => {
  particles.length = 0
  particleCount.value = 0
}

const onParamChange = () => {
  if (mode.value === 'particle') {
    clearParticles() // Params changed, old particle pattern is invalid
  }
}

// Physics Calculation
const calculateIntensity = (yMM: number) => {
  // y is position on screen in mm
  // theta ~ y / L
  // path diff delta = d * sin(theta) ~ d * y / L
  // phase diff phi = 2*pi * delta / lambda
  
  const d = params.slitDistance * 1e-3 // m
  const L = params.screenDistance // m
  const y = yMM * 1e-3 // m
  const lambda = params.wavelength * 1e-9 // m

  const pathDiff = d * y / L
  const phaseDiff = (2 * Math.PI * pathDiff) / lambda
  
  // Intensity I = I0 * cos^2(phi / 2)
  const I = Math.pow(Math.cos(phaseDiff / 2), 2)
  
  return { I, pathDiff, phaseDiff }
}

// Drawing
const draw = () => {
  const cvs = canvas.value
  if (!cvs) return
  const ctx = cvs.getContext('2d')
  if (!ctx) return

  const width = cvs.width
  const height = cvs.height
  
  // Layout constants
  const screenX = width - 80
  const slitsX = 150
  const sourceX = 50
  const centerY = height / 2
  
  // Clear
  ctx.fillStyle = '#0f172a' // Dark background
  ctx.fillRect(0, 0, width, height)

  // 1. Draw Source
  const color = wavelengthColor.value
  ctx.fillStyle = color
  ctx.shadowBlur = 20
  ctx.shadowColor = color
  ctx.beginPath()
  ctx.arc(sourceX, centerY, 8, 0, Math.PI * 2)
  ctx.fill()
  ctx.shadowBlur = 0

  // 2. Draw Slits Barrier
  ctx.strokeStyle = '#475569'
  ctx.lineWidth = 4
  ctx.beginPath()
  // Top barrier
  ctx.moveTo(slitsX, 0)
  ctx.lineTo(slitsX, centerY - 20)
  // Middle barrier
  ctx.moveTo(slitsX, centerY - 10)
  ctx.lineTo(slitsX, centerY + 10)
  // Bottom barrier
  ctx.moveTo(slitsX, centerY + 20)
  ctx.lineTo(slitsX, height)
  ctx.stroke()

  // Visual Light Beams (Schematic)
  ctx.globalAlpha = 0.1
  ctx.fillStyle = color
  ctx.beginPath()
  ctx.moveTo(sourceX, centerY)
  ctx.lineTo(slitsX, centerY - 15)
  ctx.lineTo(slitsX, centerY + 15)
  ctx.fill()
  ctx.globalAlpha = 1.0

  // 3. Draw Screen Background
  ctx.fillStyle = '#1e293b'
  ctx.fillRect(screenX, 20, 40, height - 40)
  ctx.strokeStyle = '#334155'
  ctx.strokeRect(screenX, 20, 40, height - 40)

  // 4. Render Interference Pattern
  // We map canvas Y pixels to physical screen Y coordinates
  // Let's say the screen height represents 20cm (200mm)
  const physicalScreenHeightMM = 200
  const mmPerPixel = physicalScreenHeightMM / height

  if (mode.value === 'wave') {
    const imageData = ctx.createImageData(40, height - 40)
    const data = imageData.data
    
    // Extract RGB from color string for pixel manipulation
    // Simple hack: parse the computed RGB string or use HSL
    // Let's assume we know RGB from nmToRGB logic or recalculate
    // For performance, we'll just parse the wavelengthColor string once per frame if needed
    // or use a helper. Let's re-calculate RGB components.
    const tempDiv = document.createElement('div')
    tempDiv.style.color = color
    document.body.appendChild(tempDiv)
    const rgbStr = window.getComputedStyle(tempDiv).color
    document.body.removeChild(tempDiv)
    const rgbMatch = rgbStr.match(/\d+/g)
    const r = rgbMatch ? parseInt(rgbMatch[0]) : 255
    const g = rgbMatch ? parseInt(rgbMatch[1]) : 255
    const b = rgbMatch ? parseInt(rgbMatch[2]) : 255

    for (let py = 0; py < height - 40; py++) {
      const yCanvas = py + 20
      const yMM = (yCanvas - centerY) * mmPerPixel
      
      const { I } = calculateIntensity(yMM)
      
      // Fill strip
      for (let px = 0; px < 40; px++) {
        const idx = (py * 40 + px) * 4
        data[idx] = r
        data[idx + 1] = g
        data[idx + 2] = b
        data[idx + 3] = Math.floor(I * 255) // Alpha based on intensity
      }
    }
    ctx.putImageData(imageData, screenX, 20)
  } else {
    // Particle Mode
    // Add new particles
    if (particleCount.value < MAX_PARTICLES) {
      for (let i = 0; i < 5; i++) { // Add 5 per frame
        // Rejection sampling for position Y
        let valid = false
        let yCanvas = 0
        let attempts = 0
        while (!valid && attempts < 10) {
          yCanvas = Math.random() * (height - 40) + 20
          const yMM = (yCanvas - centerY) * mmPerPixel
          const { I } = calculateIntensity(yMM)
          if (Math.random() < I) {
            valid = true
          }
          attempts++
        }
        
        if (valid) {
          particles.push({
            x: screenX + Math.random() * 40,
            y: yCanvas,
            color: color
          })
          particleCount.value++
        }
      }
    }

    // Draw particles
    ctx.fillStyle = color
    particles.forEach(p => {
      ctx.beginPath()
      ctx.arc(p.x, p.y, 1.5, 0, Math.PI * 2) // Small dots
      ctx.fill()
    })
  }

  // 5. Draw connecting lines if hovering
  if (hoverInfo.value) {
    const yPixel = centerY + (hoverInfo.value.y / mmPerPixel)
    
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.3)'
    ctx.setLineDash([5, 5])
    ctx.lineWidth = 1
    
    // Path 1: Top Slit to Point
    ctx.beginPath()
    ctx.moveTo(slitsX, centerY - 15)
    ctx.lineTo(screenX, yPixel)
    ctx.stroke()
    
    // Path 2: Bottom Slit to Point
    ctx.beginPath()
    ctx.moveTo(slitsX, centerY + 15)
    ctx.lineTo(screenX, yPixel)
    ctx.stroke()
    
    ctx.setLineDash([])

    // Highlight point
    ctx.fillStyle = '#ffffff'
    ctx.beginPath()
    ctx.arc(screenX, yPixel, 3, 0, Math.PI * 2)
    ctx.fill()
  }
}

const onCanvasHover = (e: MouseEvent) => {
  const cvs = canvas.value
  if (!cvs) return
  
  const rect = cvs.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  
  const width = cvs.width
  const height = cvs.height
  const screenX = width - 80
  const centerY = height / 2
  
  // Only active if near screen area
  if (x > screenX - 20) {
    const physicalScreenHeightMM = 200
    const mmPerPixel = physicalScreenHeightMM / height
    const yMM = (y - centerY) * mmPerPixel
    
    const { I, pathDiff, phaseDiff } = calculateIntensity(yMM)
    
    // Determine Constructive/Destructive
    // Constructive: pathDiff = k * lambda (phaseDiff = 2k*pi) -> I near 1
    // Destructive: pathDiff = (k+0.5) * lambda -> I near 0
    
    const lambda = params.wavelength // nm
    const pathDiffNM = pathDiff * 1e9 // nm
    const ratio = Math.abs(pathDiffNM / lambda)
    const fraction = ratio - Math.floor(ratio)
    
    // Simple heuristic for UI
    let isConstructive = fraction < 0.25 || fraction > 0.75
    let reason = ''
    
    if (fraction < 0.1 || fraction > 0.9) {
      const k = Math.round(ratio)
      reason = `光程差 Δr ≈ ${k}λ (波长整数倍)，波峰遇波峰，振动加强。`
      isConstructive = true
    } else if (fraction > 0.4 && fraction < 0.6) {
      const k = Math.floor(ratio)
      reason = `光程差 Δr ≈ ${k}.5λ (半波长奇数倍)，波峰遇波谷，振动抵消。`
      isConstructive = false
    } else {
      reason = `光程差 Δr = ${ratio.toFixed(2)}λ，介于加强与抵消之间。`
      isConstructive = I > 0.5
    }

    hoverInfo.value = {
      y: yMM,
      pathDiff: pathDiffNM,
      phaseDiff: phaseDiff / Math.PI, // in units of pi
      isConstructive,
      reason,
      intensity: I
    }
  } else {
    hoverInfo.value = null
  }
}

const onCanvasLeave = () => {
  hoverInfo.value = null
}

const animate = () => {
  draw()
  animationId.value = requestAnimationFrame(animate)
}

const resizeCanvas = () => {
  if (canvas.value && canvas.value.parentElement) {
    canvas.value.width = canvas.value.parentElement.clientWidth
    canvas.value.height = canvas.value.parentElement.clientHeight
  }
}

onMounted(() => {
  window.addEventListener('resize', resizeCanvas)
  resizeCanvas()
  animate()
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCanvas)
  cancelAnimationFrame(animationId.value)
})
</script>

<style scoped>
.double-slit-container {
  min-height: 100vh;
  background: #0f172a;
  display: flex;
  flex-direction: column;
  color: white;
}

.header {
  background: #1e293b;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #334155;
}

.left-nav {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header h1 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #f1f5f9;
  margin: 0;
}

.back-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.header-controls {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.mode-switch {
  display: flex;
  background: #0f172a;
  border-radius: 8px;
  padding: 4px;
  gap: 4px;
}

.mode-btn {
  background: transparent;
  border: none;
  color: #94a3b8;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.mode-btn:hover {
  color: white;
}

.mode-btn.active {
  background: #3b82f6;
  color: white;
  font-weight: 500;
}

.action-btn {
  background: transparent;
  border: 1px solid #475569;
  color: #cbd5e1;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.action-btn:hover {
  border-color: #94a3b8;
  color: white;
}

.content-layout {
  flex: 1;
  display: flex;
  padding: 1.5rem;
  gap: 1.5rem;
  height: calc(100vh - 80px);
  overflow: hidden;
}

.controls-panel {
  width: 340px;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.card {
  background: #1e293b;
  border-radius: 12px;
  border: 1px solid #334155;
  padding: 1.25rem;
}

.control-group h3 {
  margin-top: 0;
  margin-bottom: 1.5rem;
  color: #e2e8f0;
  font-size: 1rem;
}

.slider-item {
  margin-bottom: 1.5rem;
}

.slider-header {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
  color: #94a3b8;
  margin-bottom: 0.5rem;
}

.slider-header .val {
  font-family: monospace;
  font-weight: bold;
  color: #f1f5f9;
}

.color-bar {
  height: 4px;
  width: 100%;
  border-radius: 2px;
  margin-bottom: 8px;
  opacity: 0.8;
}

.info-box {
  background: #0f172a;
  padding: 1rem;
  border-radius: 8px;
  margin-top: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-box p {
  margin: 0;
  font-size: 0.9rem;
  color: #cbd5e1;
}

.clear-btn {
  background: #ef4444;
  border: none;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  cursor: pointer;
}

/* Detective Panel */
.detective-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.detective-panel h3 {
  color: #3b82f6;
  margin-top: 0;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.analysis-content {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.analysis-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
}

.analysis-row .label {
  color: #94a3b8;
}

.analysis-row .value {
  color: #f1f5f9;
  font-family: monospace;
}

.divider {
  height: 1px;
  background: #334155;
  margin: 0.5rem 0;
}

.verdict {
  background: rgba(15, 23, 42, 0.5);
  padding: 1rem;
  border-radius: 8px;
  display: flex;
  gap: 1rem;
  align-items: flex-start;
  border: 1px solid transparent;
}

.verdict.bright {
  border-color: rgba(255, 255, 0, 0.3);
  background: rgba(255, 255, 0, 0.1);
}

.verdict.dark {
  border-color: rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.3);
}

.verdict .icon {
  font-size: 1.5rem;
}

.verdict h4 {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
  color: #f1f5f9;
}

.verdict p {
  margin: 0;
  font-size: 0.85rem;
  color: #cbd5e1;
  line-height: 1.4;
}

.detective-panel.empty {
  justify-content: center;
  align-items: center;
  text-align: center;
  color: #64748b;
}

.detective-panel.empty h3 {
  color: #64748b;
  justify-content: center;
}

/* Experiment Stage */
.experiment-stage {
  flex: 1;
  position: relative;
  padding: 0;
  overflow: hidden;
}

.main-canvas {
  width: 100%;
  height: 100%;
  cursor: crosshair;
}

.stage-labels {
  position: absolute;
  bottom: 1rem;
  left: 0;
  width: 100%;
  pointer-events: none;
}

.label {
  position: absolute;
  bottom: 0;
  color: #64748b;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  transform: translateX(-50%);
}

.label.source { left: 50px; }
.label.slits { left: 150px; }
.label.screen { right: 60px; transform: translateX(0); }

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
