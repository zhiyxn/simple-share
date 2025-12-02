<template>
  <div class="slingshot-container">
    <AiTechBackButton />
    <!-- Header -->
    <div class="header">
      <div class="left-nav">
        <button class="back-btn" @click="$router.push('/aitech')">
          <el-icon><ArrowLeft /></el-icon> 返回教程
        </button>
        <h1>黑洞“引力弹弓”挑战</h1>
      </div>
      <div class="header-controls">
        <div class="score-board" v-if="lastScore !== null">
          <span class="label">上次评分:</span>
          <span class="value" :class="getScoreClass(lastScore)">{{ lastScore.toFixed(1) }}</span>
        </div>
        <button class="action-btn" @click="resetGame">
          <el-icon><Refresh /></el-icon> 重置任务
        </button>
      </div>
    </div>

    <div class="content-layout">
      <!-- Left: Controls & Dashboard -->
      <div class="controls-panel">
        
        <!-- Mission Control -->
        <div class="control-group card">
          <h3>🚀 航线设定 (Mission Control)</h3>
          
          <div class="slider-item">
            <div class="slider-header">
              <span>初始速度 (v₀)</span>
              <span class="val">{{ params.velocity.toFixed(1) }} km/s</span>
            </div>
            <el-slider 
              v-model="params.velocity" 
              :min="5" 
              :max="25" 
              :step="0.1" 
              :disabled="gameState === 'running'"
            />
          </div>

          <div class="slider-item">
            <div class="slider-header">
              <span>入射角度 (θ)</span>
              <span class="val">{{ params.angle }}°</span>
            </div>
            <el-slider 
              v-model="params.angle" 
              :min="-60" 
              :max="60" 
              :step="1" 
              :disabled="gameState === 'running'"
            />
          </div>

          <button 
            class="launch-btn" 
            :class="{ 'running': gameState === 'running' }"
            @click="toggleLaunch"
          >
            <el-icon v-if="gameState !== 'running'"><VideoPlay /></el-icon>
            <el-icon v-else><VideoPause /></el-icon>
            {{ gameState === 'running' ? '中止航程' : '发射飞船' }}
          </button>
        </div>

        <!-- Telemetry -->
        <div class="telemetry-group card">
          <h3>📊 实时遥测 (Telemetry)</h3>
          
          <div class="metric-row">
            <span class="label">距离黑洞:</span>
            <span class="value">{{ telemetry.distance.toFixed(0) }} km</span>
          </div>
          <div class="metric-row">
            <span class="label">当前速度:</span>
            <span class="value highlight">{{ telemetry.currentVelocity.toFixed(1) }} km/s</span>
          </div>
          
          <div class="chart-box">
            <h4>能量转换监控</h4>
            <div class="energy-bar-container">
              <div class="bar-label">动能 (Ek)</div>
              <div class="bar-track">
                <div class="bar-fill kinetic" :style="{ width: telemetry.ekPercent + '%' }"></div>
              </div>
            </div>
            <div class="energy-bar-container">
              <div class="bar-label">势能 (Ep)</div>
              <div class="bar-track">
                <div class="bar-fill potential" :style="{ width: telemetry.epPercent + '%' }"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Status Log -->
        <div class="log-panel card">
          <h3>📝 航行日志</h3>
          <div class="log-content" ref="logRef">
            <div v-for="(log, index) in logs" :key="index" class="log-entry" :class="log.type">
              <span class="time">[{{ log.time }}]</span> {{ log.msg }}
            </div>
          </div>
        </div>
      </div>

      <!-- Right: Space Canvas -->
      <div class="space-stage card" ref="stageRef">
        <canvas ref="canvas"></canvas>
        
        <!-- Overlay UI for Game Over / Success -->
        <div class="mission-overlay" v-if="missionResult">
          <div class="result-card" :class="missionResult.type">
            <h2>{{ missionResult.title }}</h2>
            <p>{{ missionResult.message }}</p>
            <div class="score-display" v-if="missionResult.type === 'success'">
              <div class="score-circle">
                <span class="score-val">{{ missionResult.score }}</span>
                <span class="score-label">效率评分</span>
              </div>
            </div>
            <button @click="missionResult = null">确认</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ArrowLeft, Refresh, VideoPlay, VideoPause } from '@element-plus/icons-vue'
import { ElSlider, ElIcon, ElMessage } from 'element-plus'
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue'

// --- Types ---
type GameState = 'idle' | 'running' | 'finished'
interface Log {
  time: string
  msg: string
  type: 'info' | 'success' | 'error' | 'warning'
}
interface MissionResult {
  type: 'success' | 'fail' | 'lost'
  title: string
  message: string
  score?: number
}

// --- Constants ---
const G = 1.0 // Gravitational constant (scaled)
const M = 2000 // Black hole mass (scaled)
const DT = 0.1 // Time step
const TRAIL_LENGTH = 200
const TARGET_RADIUS = 30
const EVENT_HORIZON = 25

// --- State ---
const canvas = ref<HTMLCanvasElement | null>(null)
const stageRef = ref<HTMLDivElement | null>(null)
const logRef = ref<HTMLDivElement | null>(null)

const gameState = ref<GameState>('idle')
const logs = reactive<Log[]>([])
const lastScore = ref<number | null>(null)
const missionResult = ref<MissionResult | null>(null)

const params = reactive({
  velocity: 10,
  angle: 0 // degrees
})

const telemetry = reactive({
  distance: 0,
  currentVelocity: 0,
  ekPercent: 0,
  epPercent: 0
})

// Physics State
let ship = {
  x: 0,
  y: 0,
  vx: 0,
  vy: 0,
  trail: [] as {x: number, y: number}[]
}

let blackHole = { x: 0, y: 0 }
let target = { x: 0, y: 0 }
let animationId = 0

// --- Logic ---

const addLog = (msg: string, type: Log['type'] = 'info') => {
  const now = new Date()
  const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
  logs.push({ time: timeStr, msg, type })
  
  nextTick(() => {
    if (logRef.value) {
      logRef.value.scrollTop = logRef.value.scrollHeight
    }
  })
}

const initGame = () => {
  if (!canvas.value) return
  const width = canvas.value.width
  const height = canvas.value.height
  
  // Setup positions
  blackHole = { x: width / 2, y: height / 2 }
  target = { x: width - 100, y: height / 2 } // Target on the right
  
  // Ship starts on left
  ship.x = 50
  ship.y = height / 2
  ship.vx = 0
  ship.vy = 0
  ship.trail = []
  
  telemetry.distance = Math.hypot(ship.x - blackHole.x, ship.y - blackHole.y)
  telemetry.currentVelocity = params.velocity
  
  // Initial Energy calc for baseline
  const r = telemetry.distance
  const v = params.velocity
  const Ek = 0.5 * v * v
  const Ep = - (G * M) / r // Simplified potential
  // Normalize percentages for display (relative)
  telemetry.ekPercent = 50
  telemetry.epPercent = 50
  
  draw()
}

const resetGame = () => {
  gameState.value = 'idle'
  missionResult.value = null
  logs.splice(0, logs.length)
  addLog('系统重置完成。等待发射指令。', 'info')
  initGame()
}

const toggleLaunch = () => {
  if (gameState.value === 'running') {
    gameState.value = 'idle'
    addLog('任务手动中止。', 'warning')
    initGame()
  } else {
    launch()
  }
}

const launch = () => {
  gameState.value = 'running'
  missionResult.value = null
  
  // Initialize velocity based on angle
  const rad = params.angle * Math.PI / 180
  ship.vx = params.velocity * Math.cos(rad)
  ship.vy = params.velocity * Math.sin(rad)
  
  addLog(`飞船发射！初始速度: ${params.velocity} km/s, 角度: ${params.angle}°`, 'info')
  
  animate()
}

const checkCollision = () => {
  const dx = ship.x - blackHole.x
  const dy = ship.y - blackHole.y
  const r = Math.sqrt(dx * dx + dy * dy)
  
  // 1. Check Black Hole (Event Horizon)
  if (r < EVENT_HORIZON) {
    gameState.value = 'finished'
    addLog('警报：飞船已进入视界！通讯中断...', 'error')
    missionResult.value = {
      type: 'fail',
      title: '任务失败',
      message: '飞船被黑洞吞噬。引力过大，无法逃逸。'
    }
    return true
  }
  
  // 2. Check Target
  const tdx = ship.x - target.x
  const tdy = ship.y - target.y
  const tr = Math.sqrt(tdx * tdx + tdy * tdy)
  
  if (tr < TARGET_RADIUS) {
    gameState.value = 'finished'
    const finalV = Math.sqrt(ship.vx * ship.vx + ship.vy * ship.vy)
    
    // Calculate Efficiency Score
    // Base score 80. Bonus for speed (Gravity Assist). Penalty for time?
    // Let's define efficiency by how much speed we gained at periapsis relative to initial
    // Or simply: Did we hit it fast?
    const score = Math.min(100, Math.max(60, 60 + (finalV - params.velocity) * 5 + (1000/r))) 
    
    lastScore.value = score
    addLog(`任务成功！抵达目标星球。最终速度: ${finalV.toFixed(1)}`, 'success')
    missionResult.value = {
      type: 'success',
      title: '任务成功',
      message: '完美的引力弹弓机动！你利用黑洞引力成功抵达了目的地。',
      score: Math.round(score)
    }
    return true
  }
  
  // 3. Check Out of Bounds (Lost in space)
  if (!canvas.value) return false
  const w = canvas.value.width
  const h = canvas.value.height
  if (ship.x < -100 || ship.x > w + 100 || ship.y < -100 || ship.y > h + 100) {
    gameState.value = 'finished'
    addLog('飞船脱离传感器范围。迷失在深空。', 'warning')
    missionResult.value = {
      type: 'lost',
      title: '迷失深空',
      message: '飞船未能被黑洞引力捕获或偏转角度不足，已飞向宇宙深处。'
    }
    return true
  }
  
  return false
}

const updatePhysics = () => {
  if (gameState.value !== 'running') return

  const dx = blackHole.x - ship.x
  const dy = blackHole.y - ship.y
  const r2 = dx * dx + dy * dy
  const r = Math.sqrt(r2)
  
  // F = G * M * m / r^2
  // a = F / m = G * M / r^2
  const force = (G * M) / r2
  const ax = force * (dx / r)
  const ay = force * (dy / r)
  
  // Euler Integration (Simple)
  ship.vx += ax * DT
  ship.vy += ay * DT
  ship.x += ship.vx * DT
  ship.y += ship.vy * DT
  
  // Update Trail
  if (animationId % 3 === 0) { // Limit trail points
    ship.trail.push({ x: ship.x, y: ship.y })
    if (ship.trail.length > TRAIL_LENGTH) {
      ship.trail.shift()
    }
  }
  
  // Telemetry
  telemetry.distance = r
  const v = Math.sqrt(ship.vx * ship.vx + ship.vy * ship.vy)
  telemetry.currentVelocity = v
  
  // Energy Visualization (Normalized for visual effect)
  // Ek = 0.5 * v^2
  // Ep = -GM/r (We shift this to be positive for the bar chart, or show conversion)
  // Let's show relative changes.
  // Max possible V approx 30?
  const maxEk = 0.5 * 40 * 40
  const currentEk = 0.5 * v * v
  
  telemetry.ekPercent = Math.min(100, (currentEk / maxEk) * 100 * 3) // Scale up
  telemetry.epPercent = Math.min(100, (r / 600) * 100) // Further is more potential
}

const draw = () => {
  const cvs = canvas.value
  if (!cvs) return
  const ctx = cvs.getContext('2d')
  if (!ctx) return

  const width = cvs.width
  const height = cvs.height
  
  // Clear
  ctx.fillStyle = '#000000'
  ctx.fillRect(0, 0, width, height)
  
  // Draw Background Stars
  ctx.fillStyle = '#ffffff'
  for(let i=0; i<50; i++) {
    const x = (Math.sin(i * 132) * 4321) % width
    const y = (Math.cos(i * 453) * 4321) % height
    ctx.globalAlpha = Math.random() * 0.5 + 0.2
    ctx.fillRect(Math.abs(x), Math.abs(y), 1, 1)
  }
  ctx.globalAlpha = 1.0

  // Draw Gravity Well Grid (Visual Effect)
  ctx.strokeStyle = '#1e293b'
  ctx.lineWidth = 1
  ctx.beginPath()
  const gridSize = 40
  for(let x = 0; x < width; x += gridSize) {
    for(let y = 0; y < height; y += gridSize) {
      // Distort grid based on gravity? Too expensive for now, just static grid
      // Or simple distortion:
      const dx = x - blackHole.x
      const dy = y - blackHole.y
      const d = Math.sqrt(dx*dx + dy*dy)
      const pull = Math.max(0, (200 - d) / 5)
      
      const gx = x - (dx/d) * pull
      const gy = y - (dy/d) * pull
      
      // Just draw dots
      ctx.fillStyle = '#334155'
      ctx.fillRect(gx, gy, 1, 1)
    }
  }

  // Draw Target Planet
  ctx.beginPath()
  ctx.arc(target.x, target.y, TARGET_RADIUS, 0, Math.PI * 2)
  const grad = ctx.createRadialGradient(target.x - 5, target.y - 5, 2, target.x, target.y, TARGET_RADIUS)
  grad.addColorStop(0, '#4ade80')
  grad.addColorStop(1, '#15803d')
  ctx.fillStyle = grad
  ctx.fill()
  ctx.shadowBlur = 15
  ctx.shadowColor = '#22c55e'
  ctx.stroke()
  ctx.shadowBlur = 0
  
  // Label Target
  ctx.fillStyle = '#86efac'
  ctx.font = '12px Arial'
  ctx.fillText('目标星球', target.x - 24, target.y + 45)

  // Draw Black Hole
  // Accretion Disk
  ctx.beginPath()
  ctx.arc(blackHole.x, blackHole.y, EVENT_HORIZON * 2.5, 0, Math.PI * 2)
  const diskGrad = ctx.createRadialGradient(blackHole.x, blackHole.y, EVENT_HORIZON, blackHole.x, blackHole.y, EVENT_HORIZON * 2.5)
  diskGrad.addColorStop(0, 'rgba(251, 146, 60, 0.8)') // Orange
  diskGrad.addColorStop(0.4, 'rgba(234, 88, 12, 0.4)')
  diskGrad.addColorStop(1, 'rgba(0,0,0,0)')
  ctx.fillStyle = diskGrad
  ctx.fill()

  // Event Horizon
  ctx.beginPath()
  ctx.arc(blackHole.x, blackHole.y, EVENT_HORIZON, 0, Math.PI * 2)
  ctx.fillStyle = '#000'
  ctx.fill()
  ctx.strokeStyle = '#fb923c'
  ctx.lineWidth = 2
  ctx.stroke()

  // Draw Ship Trail
  if (ship.trail.length > 1) {
    ctx.beginPath()
    ctx.moveTo(ship.trail[0].x, ship.trail[0].y)
    for (let i = 1; i < ship.trail.length; i++) {
      ctx.lineTo(ship.trail[i].x, ship.trail[i].y)
    }
    ctx.strokeStyle = '#38bdf8'
    ctx.lineWidth = 2
    ctx.stroke()
  }

  // Draw Ship
  ctx.save()
  ctx.translate(ship.x, ship.y)
  const angle = Math.atan2(ship.vy, ship.vx)
  ctx.rotate(angle)
  
  // Ship Shape
  ctx.beginPath()
  ctx.moveTo(10, 0)
  ctx.lineTo(-6, 5)
  ctx.lineTo(-6, -5)
  ctx.closePath()
  ctx.fillStyle = '#fff'
  ctx.fill()
  
  // Engine flame
  if (gameState.value === 'running') {
    ctx.beginPath()
    ctx.moveTo(-6, 0)
    ctx.lineTo(-12, 2)
    ctx.lineTo(-12, -2)
    ctx.fillStyle = '#0ea5e9'
    ctx.fill()
  }
  
  ctx.restore()
}

const animate = () => {
  if (gameState.value === 'running') {
    updatePhysics()
    if (checkCollision()) {
      draw() // Final draw
      return
    }
  }
  draw()
  if (gameState.value === 'running') {
    animationId = requestAnimationFrame(animate)
  }
}

const resizeCanvas = () => {
  if (canvas.value && stageRef.value) {
    canvas.value.width = stageRef.value.clientWidth
    canvas.value.height = stageRef.value.clientHeight
    if (gameState.value === 'idle') {
      initGame()
    }
  }
}

onMounted(() => {
  addLog('系统初始化...', 'info')
  window.addEventListener('resize', resizeCanvas)
  // Delay slightly to ensure container size is ready
  setTimeout(() => {
    resizeCanvas()
    initGame()
    addLog('等待任务开始。请设定航线参数。', 'info')
  }, 100)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCanvas)
  cancelAnimationFrame(animationId)
})

const getScoreClass = (score: number) => {
  if (score >= 90) return 'text-green-500'
  if (score >= 70) return 'text-yellow-500'
  return 'text-red-500'
}

</script>

<style scoped>
.slingshot-container {
  min-height: 100vh;
  background: #020617;
  display: flex;
  flex-direction: column;
  color: white;
  font-family: 'Inter', sans-serif;
}

.header {
  background: #0f172a;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #1e293b;
}

.left-nav {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header h1 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #f8fafc;
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
  gap: 1.5rem;
  align-items: center;
}

.score-board {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  background: #1e293b;
  padding: 0.25rem 0.75rem;
  border-radius: 6px;
  border: 1px solid #334155;
}

.score-board .label {
  color: #94a3b8;
  font-size: 0.85rem;
}

.score-board .value {
  font-weight: bold;
  font-family: monospace;
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
  transition: all 0.2s;
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
  width: 320px;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  overflow-y: auto;
}

.card {
  background: #0f172a;
  border-radius: 12px;
  border: 1px solid #1e293b;
  padding: 1.25rem;
}

.card h3 {
  color: #38bdf8;
  margin-top: 0;
  margin-bottom: 1rem;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.slider-item {
  margin-bottom: 1.5rem;
}

.slider-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
  color: #94a3b8;
}

.slider-header .val {
  color: #f1f5f9;
  font-family: monospace;
}

.launch-btn {
  width: 100%;
  background: #2563eb;
  color: white;
  border: none;
  padding: 0.75rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  transition: background 0.3s;
}

.launch-btn:hover {
  background: #1d4ed8;
}

.launch-btn.running {
  background: #ef4444;
}

.launch-btn.running:hover {
  background: #dc2626;
}

.metric-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
  font-size: 0.9rem;
}

.metric-row .label {
  color: #94a3b8;
}

.metric-row .value {
  color: #f1f5f9;
  font-family: monospace;
}

.metric-row .value.highlight {
  color: #fcd34d;
}

.chart-box h4 {
  font-size: 0.8rem;
  color: #64748b;
  margin-bottom: 0.5rem;
  text-transform: uppercase;
}

.energy-bar-container {
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.bar-label {
  width: 60px;
  font-size: 0.75rem;
  color: #94a3b8;
}

.bar-track {
  flex: 1;
  height: 6px;
  background: #1e293b;
  border-radius: 3px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.1s;
}

.bar-fill.kinetic { background: #38bdf8; }
.bar-fill.potential { background: #f472b6; }

.log-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 200px;
}

.log-content {
  flex: 1;
  overflow-y: auto;
  font-family: monospace;
  font-size: 0.8rem;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.log-entry {
  padding: 2px 0;
  border-bottom: 1px solid #1e293b;
}

.log-entry .time { color: #64748b; }
.log-entry.info { color: #cbd5e1; }
.log-entry.success { color: #4ade80; }
.log-entry.error { color: #f87171; }
.log-entry.warning { color: #fbbf24; }

.space-stage {
  flex: 1;
  position: relative;
  padding: 0;
  background: black;
  overflow: hidden;
}

canvas {
  width: 100%;
  height: 100%;
}

.mission-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.result-card {
  background: #1e293b;
  padding: 2rem;
  border-radius: 16px;
  text-align: center;
  max-width: 400px;
  border: 1px solid #334155;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.result-card h2 {
  margin-top: 0;
  font-size: 1.5rem;
}

.result-card.success h2 { color: #4ade80; }
.result-card.fail h2 { color: #f87171; }
.result-card.lost h2 { color: #fbbf24; }

.result-card p {
  color: #cbd5e1;
  margin-bottom: 1.5rem;
}

.score-display {
  display: flex;
  justify-content: center;
  margin-bottom: 1.5rem;
}

.score-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 4px solid #4ade80;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.score-val {
  font-size: 2rem;
  font-weight: bold;
  color: white;
}

.score-label {
  font-size: 0.75rem;
  color: #4ade80;
}

.result-card button {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 0.75rem 2rem;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
}

.result-card button:hover {
  background: #2563eb;
}

/* Scrollbar styles */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  background: #0f172a;
}
::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 3px;
}
</style>
