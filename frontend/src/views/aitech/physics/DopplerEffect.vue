<template>
  <div class="doppler-container">
    <AiTechBackButton />
    <!-- Header -->
    <div class="header">
      <div class="left-nav">
        <button class="back-btn" @click="$router.push('/aitech')">
          <el-icon><ArrowLeft /></el-icon> 返回教程
        </button>
        <h1>多普勒效应：听得见看得见的声波</h1>
      </div>
      <div class="header-controls">
        <!-- Info or Help -->
      </div>
    </div>

    <div class="content-layout">
      <!-- Controls Panel -->
      <div class="controls-bar card">
        <div class="control-group">
          <div class="control-item">
            <span class="label">🚑 声源速度 (马赫数: {{ machNumber.toFixed(1) }})</span>
            <el-slider 
              v-model="machNumber" 
              :min="0" 
              :max="2.0" 
              :step="0.1" 
              :marks="{0: '静止', 1: '音速', 2: '2倍音速'}"
            />
          </div>
          
          <div class="control-item toggles">
            <div class="toggle-row">
              <span class="label">🔊 声音模拟</span>
              <el-switch 
                v-model="isAudioEnabled" 
                active-text="开启" 
                inactive-text="静音"
                @change="toggleAudio"
              />
            </div>
            <div class="toggle-row" v-if="isAudioEnabled">
              <span class="label">👂 听众位置</span>
              <el-radio-group v-model="activeListener">
                <el-radio-button label="A">观察者 A (左)</el-radio-button>
                <el-radio-button label="B">观察者 B (右)</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </div>

        <div class="status-panel">
          <div class="status-item">
            <span class="status-label">发射频率</span>
            <span class="status-val">{{ baseFreq }} Hz</span>
          </div>
          <div class="status-item">
            <span class="status-label">接收频率 ({{ activeListener }})</span>
            <span class="status-val highlight">{{ currentObservedFreq.toFixed(0) }} Hz</span>
          </div>
        </div>
      </div>

      <!-- Main Visualization Area -->
      <div class="viz-area">
        <!-- Simulation Canvas -->
        <div class="canvas-panel card">
          <div class="panel-label">场景模拟 (俯视图)</div>
          <canvas ref="simCanvas" class="sim-canvas"></canvas>
          
          <!-- Legend Overlay -->
          <div class="canvas-legend">
            <div class="legend-item"><span class="dot source"></span> 声源 (救护车)</div>
            <div class="legend-item"><span class="dot observer-a"></span> 观察者 A</div>
            <div class="legend-item"><span class="dot observer-b"></span> 观察者 B</div>
          </div>
        </div>

        <!-- Frequency Chart -->
        <div class="chart-panel card">
          <div class="panel-label">频率变化实时图表</div>
          <canvas ref="chartCanvas" class="chart-canvas"></canvas>
        </div>
      </div>
      
      <!-- Explanation -->
      <div class="explanation card">
        <h3>原理说明</h3>
        <p>
          <strong>多普勒效应</strong>是指波源和观察者有相对运动时，观察者接受到的波的频率与波源发出的频率不同的现象。
        </p>
        <ul>
          <li>当声源<strong>靠近</strong>观察者时，波前被“挤压”，波长变短，频率变高（音调变尖）。</li>
          <li>当声源<strong>远离</strong>观察者时，波前被“拉伸”，波长变长，频率变低（音调变沉）。</li>
          <li>当速度超过音速（马赫数 > 1）时，声波无法跑在声源前面，形成<strong>马赫锥</strong>（激波）。</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElIcon, ElSlider, ElSwitch, ElRadioGroup, ElRadioButton } from 'element-plus'
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue'

// --- Configuration ---
const SPEED_OF_SOUND = 3 // pixels per frame
const EMISSION_INTERVAL = 10 // frames between wave emissions
const BASE_FREQ = 440 // Hz

// --- State ---
const machNumber = ref(0.5) // v_source / c
const isAudioEnabled = ref(false)
const activeListener = ref<'A' | 'B'>('B')
const baseFreq = ref(BASE_FREQ)
const currentObservedFreq = ref(BASE_FREQ)

const simCanvas = ref<HTMLCanvasElement | null>(null)
const chartCanvas = ref<HTMLCanvasElement | null>(null)

// --- Audio Context ---
let audioCtx: AudioContext | null = null
let oscillator: OscillatorNode | null = null
let gainNode: GainNode | null = null

// --- Simulation Data ---
interface Wave {
  x: number
  y: number
  emitTime: number
  id: number
}

interface Particle {
  x: number
  y: number
  vx: number
  vy: number
  life: number
  maxLife: number
  color: string
}

const waves: Wave[] = []
let frameCount = 0
let sourceX = 0
const sourceY = 200 // Vertical center roughly
const particles: Particle[] = [] // For Mach cone effect or exhaust

// Observers
const obsA = { x: 150, y: 250, label: 'A' }
const obsB = { x: 850, y: 250, label: 'B' } // Assumes canvas width ~1000

// Chart Data
const freqHistoryA: number[] = []
const freqHistoryB: number[] = []
const MAX_HISTORY = 300

// Animation Loop
let animationId: number

// --- Audio Logic ---
const initAudio = () => {
  if (!audioCtx) {
    audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)()
  }
}

const startSound = () => {
  if (!audioCtx) initAudio()
  if (!audioCtx) return

  if (audioCtx.state === 'suspended') {
    audioCtx.resume()
  }

  if (!oscillator) {
    oscillator = audioCtx.createOscillator()
    oscillator.type = 'sine'
    oscillator.frequency.value = BASE_FREQ
    
    gainNode = audioCtx.createGain()
    gainNode.gain.value = 0.1 // Low volume
    
    oscillator.connect(gainNode)
    gainNode.connect(audioCtx.destination)
    oscillator.start()
  }
}

const stopSound = () => {
  if (oscillator) {
    try {
      oscillator.stop()
      oscillator.disconnect()
    } catch (e) {}
    oscillator = null
  }
  if (gainNode) {
    gainNode.disconnect()
    gainNode = null
  }
}

const toggleAudio = (val: boolean) => {
  if (val) {
    startSound()
  } else {
    stopSound()
  }
}

const updateAudioFreq = (freq: number) => {
  if (oscillator && audioCtx) {
    // Smooth transition
    oscillator.frequency.setTargetAtTime(freq, audioCtx.currentTime, 0.1)
  }
}

// --- Physics & Drawing ---

const calculateObservedFreq = (obsX: number, obsY: number, srcX: number, srcY: number, srcVx: number) => {
  // Vector from source to observer
  const dx = obsX - srcX
  const dy = obsY - srcY
  const dist = Math.sqrt(dx*dx + dy*dy)
  
  if (dist === 0) return BASE_FREQ
  
  // Unit vector from source to observer
  const ux = dx / dist
  const uy = dy / dist
  
  // Velocity component towards observer
  // v_radial = v . u
  // Source velocity is (srcVx, 0)
  const vRadial = srcVx * ux
  
  // Doppler formula: f = f0 * (c / (c - v_radial))
  // Note: If source moves TOWARDS observer, vRadial is positive?
  // Standard formula: f = f0 * (c / (c - vs * cos(theta)))
  // where vs is speed, theta is angle between velocity and direction to observer.
  // If moving directly towards, theta=0, cos=1. f = f0 * c/(c-v). Frequency increases.
  // If moving away, theta=180, cos=-1. f = f0 * c/(c+v). Frequency decreases.
  
  // My vRadial calculation:
  // If source at 0, obs at 100 (Right), moving Right (vx > 0).
  // dx = 100, ux = 1. vRadial = vx.
  // Source is moving towards observer. Frequency should increase.
  // Formula: f = f0 * c / (c - vRadial)
  // If vRadial = c (Mach 1), denominator is 0 -> Sonic Boom (infinite freq).
  // We need to clamp to avoid infinity/negative freq logic issues in simulation.
  
  // However, physically, if v > c, the formula changes or applies to the shockwave.
  // For simple audio simulation, we usually clamp or handle the "boom".
  
  let factor = 1
  const relativeSpeed = vRadial / SPEED_OF_SOUND
  
  // Limit relative speed to just under 1 to avoid divide by zero for audio safety
  const safeSpeed = Math.min(0.95, Math.max(-0.95, relativeSpeed))
  
  // If Mach > 1, and we are in the cone, it's complicated.
  // Simplified model:
  if (machNumber.value >= 1 && relativeSpeed > 0.9) {
      // Approaching supersonic
      // Just give a high freq or handle as boom
      return BASE_FREQ * 4 // Arbitrary high pitch
  }
  
  factor = 1 / (1 - relativeSpeed)
  
  return BASE_FREQ * factor
}

const updatePhysics = () => {
  frameCount++
  
  // 1. Move Ambulance
  const canvasWidth = simCanvas.value?.width || 1000
  const sourceSpeed = machNumber.value * SPEED_OF_SOUND
  
  sourceX += sourceSpeed
  
  // Loop mechanism
  if (sourceX > canvasWidth + 100) {
    sourceX = -100
    // Clear waves when looping to avoid confusion?
    // Or let them fade. Let's keep them.
  }
  
  // 2. Emit Waves
  if (frameCount % EMISSION_INTERVAL === 0) {
    waves.push({
      x: sourceX,
      y: sourceY,
      emitTime: frameCount,
      id: frameCount
    })
  }
  
  // 3. Remove old waves
  // Remove if radius > diagonal of canvas
  if (waves.length > 0) {
    const firstWave = waves[0]
    const radius = (frameCount - firstWave.emitTime) * SPEED_OF_SOUND
    if (radius > canvasWidth * 1.5) {
      waves.shift()
    }
  }
  
  // 4. Calculate Frequencies
  const freqA = calculateObservedFreq(obsA.x, obsA.y, sourceX, sourceY, sourceSpeed)
  const freqB = calculateObservedFreq(obsB.x, obsB.y, sourceX, sourceY, sourceSpeed)
  
  // Update history
  freqHistoryA.push(freqA)
  if (freqHistoryA.length > MAX_HISTORY) freqHistoryA.shift()
  
  freqHistoryB.push(freqB)
  if (freqHistoryB.length > MAX_HISTORY) freqHistoryB.shift()
  
  // Update Audio
  const targetFreq = activeListener.value === 'A' ? freqA : freqB
  currentObservedFreq.value = targetFreq
  if (isAudioEnabled.value) {
    updateAudioFreq(targetFreq)
  }
}

const drawSimulation = () => {
  const cvs = simCanvas.value
  if (!cvs) return
  const ctx = cvs.getContext('2d')
  if (!ctx) return
  
  const w = cvs.width
  const h = cvs.height
  
  // Clear
  ctx.fillStyle = '#0f172a'
  ctx.fillRect(0, 0, w, h)
  
  // Draw Road
  ctx.fillStyle = '#334155'
  ctx.fillRect(0, sourceY - 20, w, 40)
  ctx.strokeStyle = '#475569'
  ctx.setLineDash([20, 20])
  ctx.beginPath()
  ctx.moveTo(0, sourceY)
  ctx.lineTo(w, sourceY)
  ctx.stroke()
  ctx.setLineDash([])
  
  // Draw Waves
  ctx.lineWidth = 2
  waves.forEach(wave => {
    const radius = (frameCount - wave.emitTime) * SPEED_OF_SOUND
    const alpha = Math.max(0, 1 - radius / (w * 0.8))
    
    // Color based on compression?
    // Actually just white circles is standard for Doppler visualization
    ctx.strokeStyle = `rgba(255, 255, 255, ${alpha})`
    ctx.beginPath()
    ctx.arc(wave.x, wave.y, radius, 0, Math.PI * 2)
    ctx.stroke()
  })
  
  // Draw Observers
  // A
  ctx.fillStyle = activeListener.value === 'A' ? '#4ade80' : '#94a3b8'
  ctx.beginPath()
  ctx.arc(obsA.x, obsA.y, 10, 0, Math.PI * 2)
  ctx.fill()
  ctx.fillStyle = 'white'
  ctx.textAlign = 'center'
  ctx.font = '12px Arial'
  ctx.fillText('A', obsA.x, obsA.y + 25)
  
  // B
  ctx.fillStyle = activeListener.value === 'B' ? '#4ade80' : '#94a3b8'
  ctx.beginPath()
  ctx.arc(obsB.x, obsB.y, 10, 0, Math.PI * 2)
  ctx.fill()
  ctx.fillText('B', obsB.x, obsB.y + 25)
  
  // Draw Source (Ambulance)
  ctx.fillStyle = '#ef4444'
  ctx.beginPath()
  ctx.rect(sourceX - 15, sourceY - 10, 30, 20)
  ctx.fill()
  // Siren light
  ctx.fillStyle = frameCount % 10 < 5 ? '#ffffff' : '#ff0000'
  ctx.beginPath()
  ctx.arc(sourceX, sourceY - 5, 4, 0, Math.PI * 2)
  ctx.fill()
  
  // Mach Cone Line (Visual Aid if supersonic)
  if (machNumber.value > 1) {
    // Angle sin(mu) = c / v = 1 / M
    const mu = Math.asin(1 / machNumber.value)
    const coneLen = 500
    
    ctx.strokeStyle = 'rgba(255, 100, 100, 0.5)'
    ctx.setLineDash([5, 5])
    
    // Top line
    ctx.beginPath()
    ctx.moveTo(sourceX, sourceY)
    ctx.lineTo(sourceX - coneLen * Math.cos(mu), sourceY - coneLen * Math.sin(mu))
    ctx.stroke()
    
    // Bottom line
    ctx.beginPath()
    ctx.moveTo(sourceX, sourceY)
    ctx.lineTo(sourceX - coneLen * Math.cos(mu), sourceY + coneLen * Math.sin(mu))
    ctx.stroke()
    
    ctx.setLineDash([])
  }
}

const drawChart = () => {
  const cvs = chartCanvas.value
  if (!cvs) return
  const ctx = cvs.getContext('2d')
  if (!ctx) return
  
  const w = cvs.width
  const h = cvs.height
  
  // Clear
  ctx.fillStyle = '#1e293b'
  ctx.fillRect(0, 0, w, h)
  
  // Grid
  ctx.strokeStyle = '#334155'
  ctx.lineWidth = 1
  ctx.beginPath()
  ctx.moveTo(0, h/2)
  ctx.lineTo(w, h/2) // Base frequency line
  ctx.stroke()
  
  // Helper to map freq to Y
  // Center = Base Freq
  // Range = +/- 1 octave (Base/2 to Base*2) ?
  // Let's say h/2 is 440. 0 is 880, h is 0.
  const mapY = (f: number) => {
    const diff = f - BASE_FREQ
    // Scale: 100px = 440Hz difference
    return h/2 - (diff / BASE_FREQ) * (h * 0.4)
  }
  
  // Draw A
  ctx.strokeStyle = '#60a5fa' // Blue
  ctx.lineWidth = 2
  ctx.beginPath()
  freqHistoryA.forEach((f, i) => {
    const x = (i / MAX_HISTORY) * w
    const y = mapY(f)
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  })
  ctx.stroke()
  
  // Draw B
  ctx.strokeStyle = '#f472b6' // Pink
  ctx.lineWidth = 2
  ctx.beginPath()
  freqHistoryB.forEach((f, i) => {
    const x = (i / MAX_HISTORY) * w
    const y = mapY(f)
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  })
  ctx.stroke()
  
  // Labels
  ctx.fillStyle = '#94a3b8'
  ctx.font = '10px Arial'
  ctx.fillText('Observer A', 10, 20)
  ctx.fillStyle = '#60a5fa'
  ctx.fillRect(70, 12, 10, 10)
  
  ctx.fillStyle = '#94a3b8'
  ctx.fillText('Observer B', 10, 35)
  ctx.fillStyle = '#f472b6'
  ctx.fillRect(70, 27, 10, 10)
  
  ctx.fillStyle = '#94a3b8'
  ctx.fillText(`${BASE_FREQ}Hz`, w - 40, h/2 - 5)
}

const animate = () => {
  updatePhysics()
  drawSimulation()
  drawChart()
  animationId = requestAnimationFrame(animate)
}

const handleResize = () => {
  if (simCanvas.value && simCanvas.value.parentElement) {
    simCanvas.value.width = simCanvas.value.parentElement.clientWidth
    simCanvas.value.height = 400
    
    // Update observer positions based on new width
    const w = simCanvas.value.width
    obsA.x = w * 0.15
    obsB.x = w * 0.85
  }
  if (chartCanvas.value && chartCanvas.value.parentElement) {
    chartCanvas.value.width = chartCanvas.value.parentElement.clientWidth
    chartCanvas.value.height = 150
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  // Initial sizing
  setTimeout(handleResize, 100)
  
  animate()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  cancelAnimationFrame(animationId)
  stopSound()
  if (audioCtx) {
    audioCtx.close()
  }
})
</script>

<style scoped>
.doppler-container {
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

.content-layout {
  flex: 1;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.card {
  background: #1e293b;
  border-radius: 12px;
  border: 1px solid #334155;
  padding: 1.5rem;
}

.controls-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 2rem;
}

.control-group {
  flex: 2;
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
}

.control-item {
  flex: 1;
  min-width: 250px;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.toggles {
  flex: 0 0 auto;
  gap: 1rem;
}

.toggle-row {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.status-panel {
  flex: 1;
  display: flex;
  gap: 2rem;
  justify-content: flex-end;
}

.status-item {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.status-label {
  font-size: 0.8rem;
  color: #94a3b8;
}

.status-val {
  font-size: 1.5rem;
  font-family: monospace;
  font-weight: bold;
  color: #e2e8f0;
}

.status-val.highlight {
  color: #4ade80;
}

.viz-area {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.canvas-panel {
  position: relative;
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-label {
  position: absolute;
  top: 1rem;
  left: 1rem;
  background: rgba(0,0,0,0.5);
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.8rem;
  color: #cbd5e1;
  pointer-events: none;
}

.sim-canvas {
  width: 100%;
  height: 400px;
  display: block;
  background: #0f172a;
}

.canvas-legend {
  position: absolute;
  bottom: 1rem;
  right: 1rem;
  display: flex;
  gap: 1rem;
  background: rgba(0,0,0,0.7);
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.8rem;
  color: #cbd5e1;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.dot.source { background: #ef4444; }
.dot.observer-a { background: #60a5fa; }
.dot.observer-b { background: #f472b6; }

.chart-panel {
  padding: 0;
  height: 150px;
  position: relative;
  overflow: hidden;
}

.chart-canvas {
  width: 100%;
  height: 100%;
  display: block;
}

.explanation {
  color: #cbd5e1;
}

.explanation h3 {
  color: #f8fafc;
  margin-top: 0;
}

.explanation ul {
  margin-bottom: 0;
  padding-left: 1.5rem;
}

.explanation li {
  margin-bottom: 0.5rem;
}

@media (max-width: 768px) {
  .controls-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .status-panel {
    justify-content: space-between;
  }
}
</style>
