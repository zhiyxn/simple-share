<template>
  <div class="oscillator-container">
    <AiTechBackButton />
    <!-- Header -->
    <div class="header">
      <div class="left-nav">
        <button class="back-btn" @click="$router.push('/aitech')">
          <el-icon><ArrowLeft /></el-icon> 返回教程
        </button>
        <h1>简谐振动“能量舞者”模拟器</h1>
      </div>
      <div class="header-controls">
        <button class="action-btn" @click="toggleEnergyView" :class="{ active: showEnergy }">
          <el-icon><TrendCharts /></el-icon> 能量视图
        </button>
        <button class="action-btn primary" @click="resetSimulation">
          <el-icon><Refresh /></el-icon> 重置
        </button>
      </div>
    </div>

    <div class="content-layout">
      <!-- Left: Simulation Canvas -->
      <div class="sim-panel">
        <canvas 
          ref="simCanvas" 
          class="sim-canvas"
          @mousedown="onMouseDown"
          @mousemove="onMouseMove"
          @mouseup="onMouseUp"
          @mouseleave="onMouseLeave"
        ></canvas>
        
        <!-- Energy Overlay (Floating on canvas or drawn inside, implementing as overlay for crisp text) -->
        <div v-if="showEnergy" class="energy-hud">
          <div class="energy-bars">
            <div class="energy-bar-group">
              <div class="bar-label">动能 (KE)</div>
              <div class="bar-track">
                <div class="bar-fill ke" :style="{ height: kePercent + '%' }"></div>
              </div>
              <div class="bar-value">{{ ke.toFixed(2) }} J</div>
            </div>
            <div class="energy-bar-group">
              <div class="bar-label">势能 (PE)</div>
              <div class="bar-track">
                <div class="bar-fill pe" :style="{ height: pePercent + '%' }"></div>
              </div>
              <div class="bar-value">{{ pe.toFixed(2) }} J</div>
            </div>
            <div class="energy-bar-group total">
              <div class="bar-label">总能 (E)</div>
              <div class="bar-track">
                <div class="bar-fill total" :style="{ height: (totalEnergy / maxTotalEnergy * 100) + '%' }"></div>
              </div>
              <div class="bar-value">{{ totalEnergy.toFixed(2) }} J</div>
            </div>
          </div>
          
          <div class="pie-chart-container">
            <canvas ref="pieCanvas" width="120" height="120"></canvas>
            <div class="pie-label">能量分布</div>
          </div>
        </div>

        <div class="visual-hint" v-if="isEquilibrium">
          <span class="speed-max-badge">速度最大点 (Max Velocity)</span>
        </div>
      </div>

      <!-- Right: Controls & Graphs -->
      <div class="dashboard-panel">
        <!-- Controls -->
        <div class="control-group card">
          <h3>参数调整</h3>
          
          <div class="slider-item">
            <div class="slider-header">
              <span>质量 (m)</span>
              <span class="val">{{ params.mass }} kg</span>
            </div>
            <el-slider v-model="params.mass" :min="0.1" :max="5" :step="0.1" @input="onParamChange" />
          </div>

          <div class="slider-item">
            <div class="slider-header">
              <span>劲度系数 (k)</span>
              <span class="val">{{ params.stiffness }} N/m</span>
            </div>
            <el-slider v-model="params.stiffness" :min="1" :max="50" :step="1" @input="onParamChange" />
          </div>

          <div class="slider-item">
            <div class="slider-header">
              <span>阻尼系数 (c)</span>
              <span class="val">{{ params.damping }} Ns/m</span>
            </div>
            <el-slider v-model="params.damping" :min="0" :max="2" :step="0.01" @input="onParamChange" />
          </div>
        </div>

        <!-- Graphs -->
        <div class="graphs-container card">
          <h3>实时曲线</h3>
          <div class="graph-item">
            <span class="graph-label x-label">位移 (x)</span>
            <canvas ref="graphX" height="60"></canvas>
          </div>
          <div class="graph-item">
            <span class="graph-label v-label">速度 (v)</span>
            <canvas ref="graphV" height="60"></canvas>
          </div>
          <div class="graph-item">
            <span class="graph-label a-label">加速度 (a)</span>
            <canvas ref="graphA" height="60"></canvas>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ArrowLeft, Refresh, TrendCharts } from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue'

// --- State ---
const simCanvas = ref<HTMLCanvasElement | null>(null)
const pieCanvas = ref<HTMLCanvasElement | null>(null)
const graphX = ref<HTMLCanvasElement | null>(null)
const graphV = ref<HTMLCanvasElement | null>(null)
const graphA = ref<HTMLCanvasElement | null>(null)

const showEnergy = ref(false)
const animationId = ref(0)

const params = reactive({
  mass: 2.0,
  stiffness: 10.0,
  damping: 0.05
})

// Physics State
let state = {
  t: 0,
  x: 150, // Initial displacement (pixels) - mapped to meters roughly
  v: 0,
  a: 0
}

// History for graphs (circular buffer or simple array shift)
const maxHistory = 300
const history = {
  x: new Array(maxHistory).fill(0),
  v: new Array(maxHistory).fill(0),
  a: new Array(maxHistory).fill(0)
}

// Energy Metrics
const ke = ref(0)
const pe = ref(0)
const totalEnergy = ref(0)
const maxTotalEnergy = ref(0) // For scaling total energy bar
const isEquilibrium = ref(false)

const kePercent = computed(() => totalEnergy.value > 0 ? (ke.value / totalEnergy.value) * 100 : 0)
const pePercent = computed(() => totalEnergy.value > 0 ? (pe.value / totalEnergy.value) * 100 : 0)

// Constants
const PIXELS_PER_METER = 100 // Visual scale
const TIME_STEP = 0.016 // ~60fps

// --- Methods ---

const toggleEnergyView = () => {
  showEnergy.value = !showEnergy.value
}

const resetSimulation = () => {
  state.x = 150 // Reset to initial amplitude
  state.v = 0
  state.a = 0
  state.t = 0
  
  // Reset graphs
  history.x.fill(0)
  history.v.fill(0)
  history.a.fill(0)
  
  // Recalculate initial max energy for scaling
  const xMeters = state.x / PIXELS_PER_METER
  maxTotalEnergy.value = 0.5 * params.stiffness * xMeters * xMeters
}

const onParamChange = () => {
  // Optional: dynamic adjustments if needed
}

const isDragging = ref(false)
const dragStartX = ref(0)
const dragOffset = ref(0)

// Physics Loop
const updatePhysics = () => {
  if (isDragging.value) return

  // Convert pixels to meters for calculation
  const xMeters = state.x / PIXELS_PER_METER
  const vMeters = state.v / PIXELS_PER_METER
  
  // F = -kx - cv
  const fSpring = -params.stiffness * xMeters
  const fDamping = -params.damping * vMeters
  const force = fSpring + fDamping
  
  // a = F/m
  const aMeters = force / params.mass
  
  // Euler Integration
  state.v += aMeters * PIXELS_PER_METER * TIME_STEP
  state.x += state.v * TIME_STEP
  state.a = aMeters * PIXELS_PER_METER
  state.t += TIME_STEP

  // Energy Threshold to stop jitter
  if (Math.abs(state.v) < 0.1 && Math.abs(state.x) < 0.1 && Math.abs(state.a) < 0.1) {
     state.v = 0
     state.x = 0
     state.a = 0
  }

  // Energy Calculation
  const currentXMeters = state.x / PIXELS_PER_METER
  const currentVMeters = state.v / PIXELS_PER_METER
  
  ke.value = 0.5 * params.mass * currentVMeters * currentVMeters
  pe.value = 0.5 * params.stiffness * currentXMeters * currentXMeters
  totalEnergy.value = ke.value + pe.value
  
  // Update Max Energy if current exceeds it (e.g. if stiffness changed)
  if (totalEnergy.value > maxTotalEnergy.value) {
    maxTotalEnergy.value = totalEnergy.value
  }

  // Equilibrium Check (approximate zero crossing)
  isEquilibrium.value = Math.abs(state.x) < 5 && Math.abs(state.v) > 10
  
  // Update History
  history.x.push(state.x)
  history.x.shift()
  history.v.push(state.v) 
  history.v.shift()
  history.a.push(state.a) 
  history.a.shift()
}

// Interaction
const getCanvasCoords = (e: MouseEvent) => {
  const canvas = simCanvas.value
  if (!canvas) return { x: 0, y: 0 }
  const rect = canvas.getBoundingClientRect()
  return {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  }
}

const onMouseDown = (e: MouseEvent) => {
  const { x, y } = getCanvasCoords(e)
  const canvas = simCanvas.value
  if (!canvas) return
  
  const width = canvas.width
  const height = canvas.height
  const centerX = width / 2
  const centerY = height / 2
  const blockX = centerX + state.x
  
  // Check hit block (40x40 radius approx)
  if (x >= blockX - 40 && x <= blockX + 40 && y >= centerY - 40 && y <= centerY + 40) {
    isDragging.value = true
    dragOffset.value = x - blockX
    document.body.style.cursor = 'grabbing'
  }
}

const onMouseMove = (e: MouseEvent) => {
  const { x, y } = getCanvasCoords(e)
  const canvas = simCanvas.value
  if (!canvas) return
  
  const width = canvas.width
  const height = canvas.height
  const centerX = width / 2
  const centerY = height / 2
  const blockX = centerX + state.x
  
  // Hover effect
  if (!isDragging.value) {
    if (x >= blockX - 40 && x <= blockX + 40 && y >= centerY - 40 && y <= centerY + 40) {
      canvas.style.cursor = 'grab'
    } else {
      canvas.style.cursor = 'default'
    }
    return
  }

  // Dragging logic
  const newBlockX = x - dragOffset.value
  state.x = newBlockX - centerX
  
  // Reset dynamics while dragging
  state.v = 0
  state.a = 0
  
  // Update energy during drag
  const xMeters = state.x / PIXELS_PER_METER
  pe.value = 0.5 * params.stiffness * xMeters * xMeters
  ke.value = 0
  totalEnergy.value = pe.value
  if (totalEnergy.value > maxTotalEnergy.value) maxTotalEnergy.value = totalEnergy.value
  
  // Update history for smooth graph during drag (flat v/a, changing x)
  history.x.push(state.x)
  history.x.shift()
  history.v.push(0)
  history.v.shift()
  history.a.push(0)
  history.a.shift()
}

const onMouseUp = () => {
  if (isDragging.value) {
    isDragging.value = false
    document.body.style.cursor = 'default'
    // Release with 0 velocity (potential energy converts to kinetic)
  }
}

const onMouseLeave = () => {
  if (isDragging.value) {
    isDragging.value = false
    document.body.style.cursor = 'default'
  }
}

// Drawing
const drawSimulation = () => {
  const canvas = simCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  
  // Resize handling
  const width = canvas.parentElement?.clientWidth || 600
  const height = canvas.parentElement?.clientHeight || 400
  canvas.width = width
  canvas.height = height
  
  const centerX = width / 2
  const centerY = height / 2
  
  // Clear
  ctx.fillStyle = '#f8fafc'
  ctx.fillRect(0, 0, width, height)
  
  // Draw Wall (Left side)
  ctx.fillStyle = '#cbd5e1'
  ctx.fillRect(0, centerY - 50, 20, 100)
  
  // Draw Floor (Line)
  ctx.beginPath()
  ctx.moveTo(0, centerY + 50)
  ctx.lineTo(width, centerY + 50)
  ctx.strokeStyle = '#e2e8f0'
  ctx.lineWidth = 2
  ctx.stroke()

  // Draw Equilibrium Line
  ctx.beginPath()
  ctx.setLineDash([5, 5])
  ctx.moveTo(centerX, centerY - 100)
  ctx.lineTo(centerX, centerY + 100)
  ctx.strokeStyle = '#94a3b8'
  ctx.lineWidth = 1
  ctx.stroke()
  ctx.setLineDash([])
  
  // Draw Spring
  // From wall (20) to Block (centerX + state.x)
  const blockX = centerX + state.x
  const startX = 20
  const endX = blockX - 40 // Connect to left side of block
  
  ctx.beginPath()
  ctx.moveTo(startX, centerY)
  
  // Zigzag spring
  const segments = 20
  const segmentLen = (endX - startX) / segments
  for (let i = 0; i < segments; i++) {
    const x = startX + i * segmentLen
    const y = centerY + ((i % 2 === 0) ? -10 : 10)
    ctx.lineTo(x, y)
  }
  ctx.lineTo(endX, centerY)
  
  ctx.strokeStyle = '#475569'
  ctx.lineWidth = 3
  ctx.stroke()
  
  // Draw Block
  ctx.fillStyle = '#3b82f6'
  if (isDragging.value) {
    ctx.fillStyle = '#2563eb' // Darker blue when dragging
    ctx.shadowColor = '#3b82f6'
    ctx.shadowBlur = 10
  } else if (isEquilibrium.value) {
    ctx.fillStyle = '#f59e0b' // Highlight at max velocity
    // Glow effect
    ctx.shadowColor = '#f59e0b'
    ctx.shadowBlur = 20
  } else {
    ctx.shadowBlur = 0
  }
  
  ctx.fillRect(blockX - 40, centerY - 40, 80, 80)
  
  // Block border
  ctx.strokeStyle = '#1d4ed8'
  ctx.lineWidth = 2
  ctx.strokeRect(blockX - 40, centerY - 40, 80, 80)
  
  // Mass Label
  ctx.fillStyle = 'white'
  ctx.font = 'bold 16px Arial'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.shadowBlur = 0 // Reset shadow for text
  ctx.fillText('m', blockX, centerY)
}

const drawGraphs = () => {
  drawSingleGraph(graphX.value, history.x, '#3b82f6', 200)
  drawSingleGraph(graphV.value, history.v, '#10b981', 10) // Scale down velocity for visual fit
  drawSingleGraph(graphA.value, history.a, '#ef4444', 10) // Scale down acceleration
}

const drawSingleGraph = (canvas: HTMLCanvasElement | null, data: number[], color: string, rangeScale: number) => {
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  
  const width = canvas.parentElement?.clientWidth || 300
  canvas.width = width
  // canvas.height is fixed in template or set here
  const height = canvas.height
  const midY = height / 2
  
  ctx.clearRect(0, 0, width, height)
  
  // Draw center line
  ctx.beginPath()
  ctx.moveTo(0, midY)
  ctx.lineTo(width, midY)
  ctx.strokeStyle = '#e2e8f0'
  ctx.stroke()
  
  ctx.beginPath()
  ctx.lineWidth = 2
  ctx.strokeStyle = color
  
  const stepX = width / maxHistory
  
  // Auto-scaling could be better, but using fixed scale for now with clamp
  // Or normalized: find max in history?
  // Let's use a dynamic scale based on recent max to keep it visible
  let maxVal = 0
  for (let v of data) maxVal = Math.max(maxVal, Math.abs(v))
  if (maxVal < 5) maxVal = 5 // Minimum scale to prevent noise zoom-in
  
  const scaleY = (height / 2 - 5) / maxVal
  
  data.forEach((val, i) => {
    const x = i * stepX
    const y = midY - val * scaleY
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  })
  ctx.stroke()
}

const drawPie = () => {
  if (!pieCanvas.value || !showEnergy.value) return
  const ctx = pieCanvas.value.getContext('2d')
  if (!ctx) return
  
  const width = pieCanvas.value.width
  const height = pieCanvas.value.height
  const cx = width / 2
  const cy = height / 2
  const radius = Math.min(cx, cy) - 5
  
  ctx.clearRect(0, 0, width, height)
  
  const total = ke.value + pe.value
  if (total <= 0.0001) return
  
  const keAngle = (ke.value / total) * Math.PI * 2
  
  // KE Slice
  ctx.beginPath()
  ctx.moveTo(cx, cy)
  ctx.arc(cx, cy, radius, 0, keAngle)
  ctx.fillStyle = '#10b981' // Green for Kinetic
  ctx.fill()
  
  // PE Slice
  ctx.beginPath()
  ctx.moveTo(cx, cy)
  ctx.arc(cx, cy, radius, keAngle, Math.PI * 2)
  ctx.fillStyle = '#3b82f6' // Blue for Potential
  ctx.fill()
}

const loop = () => {
  updatePhysics()
  drawSimulation()
  drawGraphs()
  if (showEnergy.value) {
    drawPie()
  }
  animationId.value = requestAnimationFrame(loop)
}

onMounted(() => {
  resetSimulation()
  loop()
})

onUnmounted(() => {
  cancelAnimationFrame(animationId.value)
})
</script>

<style scoped>
.oscillator-container {
  min-height: 100vh;
  background: #f1f5f9;
  display: flex;
  flex-direction: column;
}

.header {
  background: white;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  z-index: 10;
}

.left-nav {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header h1 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.back-btn {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background: #f1f5f9;
}

.header-controls {
  display: flex;
  gap: 1rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  background: white;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #f8fafc;
}

.action-btn.active {
  background: #e0f2fe;
  border-color: #3b82f6;
  color: #0284c7;
}

.action-btn.primary {
  background: #3b82f6;
  color: white;
  border: none;
}

.action-btn.primary:hover {
  background: #2563eb;
}

.content-layout {
  flex: 1;
  display: flex;
  padding: 1.5rem;
  gap: 1.5rem;
  height: calc(100vh - 80px);
  overflow: hidden;
}

.sim-panel {
  flex: 1;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sim-canvas {
  width: 100%;
  height: 100%;
}

.dashboard-panel {
  width: 360px;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  overflow-y: auto;
}

.card {
  background: white;
  padding: 1.25rem;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.card h3 {
  margin-top: 0;
  margin-bottom: 1rem;
  font-size: 1rem;
  color: #475569;
  font-weight: 600;
}

.slider-item {
  margin-bottom: 1.5rem;
}

.slider-header {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
  color: #64748b;
  margin-bottom: 0.25rem;
}

.slider-header .val {
  color: #0f172a;
  font-weight: 500;
  font-family: monospace;
}

.graphs-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.graph-item {
  background: #f8fafc;
  border-radius: 8px;
  padding: 0.5rem;
  position: relative;
}

.graph-label {
  position: absolute;
  top: 4px;
  left: 8px;
  font-size: 0.75rem;
  font-weight: 600;
}

.x-label { color: #3b82f6; }
.v-label { color: #10b981; }
.a-label { color: #ef4444; }

.graph-item canvas {
  width: 100%;
  display: block;
}

/* Energy Overlay Styles */
.energy-hud {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  padding: 1rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  display: flex;
  gap: 2rem;
  align-items: center;
  border: 1px solid #e2e8f0;
}

.energy-bars {
  display: flex;
  gap: 1rem;
  height: 140px;
  align-items: flex-end;
}

.energy-bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  height: 100%;
}

.bar-track {
  flex: 1;
  width: 24px;
  background: #e2e8f0;
  border-radius: 12px;
  position: relative;
  overflow: hidden;
}

.bar-fill {
  position: absolute;
  bottom: 0;
  width: 100%;
  transition: height 0.1s linear;
  border-radius: 12px;
}

.bar-fill.ke { background: #10b981; }
.bar-fill.pe { background: #3b82f6; }
.bar-fill.total { background: #8b5cf6; }

.bar-label {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 600;
}

.bar-value {
  font-size: 0.75rem;
  font-family: monospace;
  color: #334155;
}

.pie-chart-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.pie-label {
  font-size: 0.75rem;
  color: #64748b;
}

.visual-hint {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.speed-max-badge {
  background: #f59e0b;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: bold;
  animation: fadeOut 0.5s ease-out forwards;
}

@keyframes fadeOut {
  0% { opacity: 1; transform: translateY(0); }
  100% { opacity: 0; transform: translateY(-20px); }
}

/* Responsive */
@media (max-width: 1024px) {
  .content-layout {
    flex-direction: column;
    height: auto;
    overflow: visible;
  }
  
  .dashboard-panel {
    width: 100%;
  }
  
  .sim-panel {
    height: 400px;
  }
}
</style>
