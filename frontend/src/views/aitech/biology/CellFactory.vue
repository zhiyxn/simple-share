<template>
  <div class="cell-sim-container">
    <AiTechBackButton />
    <!-- Header -->
    <div class="header">
      <div class="left-nav">
        <button class="back-btn" @click="$router.push('/aitech')">
          <el-icon><ArrowLeft /></el-icon> 返回教程
        </button>
        <h1>细胞工厂运输模拟器</h1>
      </div>
      <div class="header-controls">
        <button class="action-btn" @click="resetSim">
          <el-icon><Refresh /></el-icon> 重置工厂
        </button>
      </div>
    </div>

    <div class="content-layout">
      <!-- Left: Controls & Info -->
      <div class="info-panel">
        <div class="control-card card">
          <h3>🏭 生产控制台</h3>
          <p class="status-text">当前状态: <span :class="currentPhase">{{ phaseStatusText }}</span></p>
          
          <button 
            class="start-btn" 
            :disabled="phase !== 'idle'"
            @click="startProduction"
          >
            <el-icon><VideoPlay /></el-icon> 启动基因表达
          </button>

          <div class="steps-indicator">
            <div class="step" :class="{ active: phase === 'transcription' || phase === 'translation' }">
              <span class="icon">🧬</span>
              <span class="label">合成</span>
            </div>
            <div class="arrow">↓</div>
            <div class="step" :class="{ active: phase === 'folding' || (phase === 'drag_to_er' && proteinState === 'unfolded') }">
              <span class="icon">🥨</span>
              <span class="label">折叠 (内质网)</span>
            </div>
            <div class="arrow">↓</div>
            <div class="step" :class="{ active: phase === 'processing' || (phase === 'drag_to_golgi' && proteinState === 'folded') }">
              <span class="icon">🏷️</span>
              <span class="label">修饰 (高尔基体)</span>
            </div>
            <div class="arrow">↓</div>
            <div class="step" :class="{ active: phase === 'secretion' || (phase === 'drag_to_membrane' && proteinState === 'modified') }">
              <span class="icon">📦</span>
              <span class="label">分泌 (细胞膜)</span>
            </div>
          </div>
        </div>

        <transition name="fade">
          <div class="organelle-info card" v-if="activeOrganelle">
            <h3>{{ activeOrganelle.name }}</h3>
            <p>{{ activeOrganelle.description }}</p>
            <div class="fun-fact">
              <strong>💡 知识点:</strong> {{ activeOrganelle.fact }}
            </div>
          </div>
          <div class="organelle-info card empty" v-else>
            <h3>🦠 细胞微观世界</h3>
            <p>点击“启动”开始生产蛋白质。拖动蛋白质穿梭于各个细胞器之间，探索生命物质的运输之旅。</p>
          </div>
        </transition>
      </div>

      <!-- Right: Cell Canvas -->
      <div class="cell-stage card" ref="stageRef">
        <canvas 
          ref="canvas"
          @mousedown="onMouseDown"
          @mousemove="onMouseMove"
          @mouseup="onMouseUp"
          @mouseleave="onMouseUp"
        ></canvas>
        
        <!-- Interactive Hints -->
        <div class="hint-overlay" v-if="showHint">
          <div class="hint-box" :style="{ left: hintPos.x + 'px', top: hintPos.y + 'px' }">
            {{ hintText }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { ArrowLeft, Refresh, VideoPlay } from '@element-plus/icons-vue'
import { ElIcon, ElMessage } from 'element-plus'
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue'

// --- Types ---
type Phase = 'idle' | 'transcription' | 'translation' | 'drag_to_er' | 'folding' | 'drag_to_golgi' | 'processing' | 'drag_to_membrane' | 'secretion' | 'finished'
type ProteinState = 'none' | 'unfolded' | 'folded' | 'modified'

interface Organelle {
  id: string
  name: string
  description: string
  fact: string
  color: string
  highlightColor: string
  x: number
  y: number
  width?: number
  height?: number
  radius?: number
  draw: (ctx: CanvasRenderingContext2D, isHighlight: boolean) => void
  checkHit: (x: number, y: number) => boolean
}

// --- State ---
const canvas = ref<HTMLCanvasElement | null>(null)
const stageRef = ref<HTMLDivElement | null>(null)
const phase = ref<Phase>('idle')
const proteinState = ref<ProteinState>('none')
const activeOrganelle = ref<Organelle | null>(null)

// Animation & Physics
const protein = reactive({
  x: 0,
  y: 0,
  vx: 0,
  vy: 0,
  radius: 15,
  isDragging: false,
  particles: [] as {dx: number, dy: number, color: string}[] // For visualization
})

const mRNA = reactive({
  active: false,
  x: 0,
  y: 0,
  path: [] as {x: number, y: number}[],
  progress: 0
})

// Interactive State
const showHint = ref(false)
const hintText = ref('')
const hintPos = reactive({ x: 0, y: 0 })

let animationId = 0
let lastTime = 0

// --- Organelle Definitions ---
// Positions will be scaled in resize
const organelles: Organelle[] = []

const defineOrganelles = (w: number, h: number) => {
  organelles.length = 0
  
  // 1. Nucleus (Bottom Left)
  organelles.push({
    id: 'nucleus',
    name: '细胞核 (Nucleus)',
    description: '细胞的指挥中心，储存遗传物质DNA。',
    fact: 'mRNA在这里转录生成，携带制造蛋白质的蓝图。',
    color: '#e11d48',
    highlightColor: '#fb7185',
    x: w * 0.2,
    y: h * 0.8,
    radius: w * 0.15,
    draw(ctx, hl) {
      ctx.beginPath()
      ctx.arc(this.x, this.y, this.radius!, 0, Math.PI * 2)
      ctx.fillStyle = hl ? this.highlightColor : this.color
      ctx.fill()
      // Inner Nucleolus
      ctx.beginPath()
      ctx.arc(this.x - 10, this.y + 10, this.radius! * 0.4, 0, Math.PI * 2)
      ctx.fillStyle = '#881337'
      ctx.fill()
      // Pores
      ctx.fillStyle = '#0f172a' // Bg color to simulate hole
      for(let i=0; i<8; i++) {
        const ang = i * (Math.PI * 2 / 8)
        const px = this.x + Math.cos(ang) * (this.radius! - 5)
        const py = this.y + Math.sin(ang) * (this.radius! - 5)
        ctx.beginPath()
        ctx.arc(px, py, 4, 0, Math.PI * 2)
        ctx.fill()
      }
    },
    checkHit(x, y) {
      const dx = x - this.x
      const dy = y - this.y
      return Math.sqrt(dx*dx + dy*dy) < this.radius!
    }
  })

  // 2. Rough ER (Adjacent to Nucleus)
  organelles.push({
    id: 'er',
    name: '粗面内质网 (Rough ER)',
    description: '蛋白质合成与加工的工厂，表面附着核糖体。',
    fact: '新合成的多肽链在这里进行折叠，形成正确的三维结构。',
    color: '#c026d3',
    highlightColor: '#e879f9',
    x: w * 0.45,
    y: h * 0.6,
    width: w * 0.25,
    height: h * 0.3,
    draw(ctx, hl) {
      ctx.fillStyle = hl ? this.highlightColor : this.color
      // Draw folds using curves
      const sx = this.x - this.width!/2
      const sy = this.y - this.height!/2
      
      ctx.beginPath()
      // Simplified maze-like structure
      for(let i=0; i<5; i++) {
        const yOffset = i * (this.height!/5)
        ctx.roundRect(sx, sy + yOffset, this.width!, this.height!/6, 10)
      }
      ctx.fill()
      
      // Draw Ribosomes (dots)
      ctx.fillStyle = '#fce7f3'
      for(let i=0; i<20; i++) {
        const rx = sx + Math.random() * this.width!
        const ry = sy + Math.random() * this.height!
        ctx.beginPath()
        ctx.arc(rx, ry, 2, 0, Math.PI*2)
        ctx.fill()
      }
    },
    checkHit(x, y) {
      const sx = this.x - this.width!/2
      const sy = this.y - this.height!/2
      return x > sx && x < sx + this.width! && y > sy && y < sy + this.height!
    }
  })

  // 3. Golgi Apparatus (Middle Right)
  organelles.push({
    id: 'golgi',
    name: '高尔基体 (Golgi Apparatus)',
    description: '细胞的物流中心，负责蛋白质的修饰、分拣和包装。',
    fact: '这里给蛋白质打上“标签”（如糖基化），决定它们的最终去向。',
    color: '#ea580c',
    highlightColor: '#fb923c',
    x: w * 0.75,
    y: h * 0.4,
    width: w * 0.15,
    height: h * 0.25,
    draw(ctx, hl) {
      ctx.fillStyle = hl ? this.highlightColor : this.color
      const sx = this.x - this.width!/2
      const sy = this.y - this.height!/2
      
      // Stack of curved sacs
      for(let i=0; i<4; i++) {
        const yOffset = i * (this.height!/4)
        const widthMod = Math.sin(i) * 10
        
        ctx.beginPath()
        ctx.ellipse(this.x, sy + yOffset + 15, this.width!/2 + widthMod, 10, 0, 0, Math.PI * 2)
        ctx.fill()
      }
      
      // Vesicles nearby
      ctx.beginPath()
      ctx.arc(this.x + this.width!/2 + 10, this.y, 8, 0, Math.PI*2)
      ctx.arc(this.x - this.width!/2 - 10, this.y + 20, 6, 0, Math.PI*2)
      ctx.fill()
    },
    checkHit(x, y) {
      // Simple box approximation for hit test
      const sx = this.x - this.width! * 0.8
      const sy = this.y - this.height! * 0.6
      return x > sx && x < sx + this.width! * 1.6 && y > sy && y < sy + this.height! * 1.2
    }
  })

  // 4. Cell Membrane (Top Right Edge)
  organelles.push({
    id: 'membrane',
    name: '细胞膜 (Cell Membrane)',
    description: '细胞的边界，控制物质进出。',
    fact: '通过胞吐作用（Exocytosis），囊泡与细胞膜融合，将蛋白质释放到细胞外。',
    color: '#0ea5e9',
    highlightColor: '#7dd3fc',
    x: w,
    y: 0, // Conceptual corner
    draw(ctx, hl) {
      ctx.strokeStyle = hl ? this.highlightColor : this.color
      ctx.lineWidth = 12
      ctx.beginPath()
      // Curve from top-mid to right-mid
      ctx.moveTo(w * 0.6, 0)
      ctx.quadraticCurveTo(w * 0.9, h * 0.1, w, h * 0.4)
      ctx.stroke()
      
      // Bilayer detail
      ctx.strokeStyle = '#bae6fd'
      ctx.lineWidth = 4
      ctx.stroke()
    },
    checkHit(x, y) {
      // Check proximity to the curve
      // Approximate check: top right corner area
      return x > w * 0.8 && y < h * 0.3
    }
  })
}

// --- Computed ---
const phaseStatusText = computed(() => {
  switch(phase.value) {
    case 'idle': return '等待启动'
    case 'transcription': return '转录中 (Transcription)... mRNA 生成'
    case 'translation': return '翻译中 (Translation)... 蛋白质合成'
    case 'drag_to_er': return '待折叠：请拖拽蛋白质到内质网'
    case 'folding': return '内质网折叠中...'
    case 'drag_to_golgi': return '待加工：请拖拽到高尔基体'
    case 'processing': return '高尔基体修饰中...'
    case 'drag_to_membrane': return '待分泌：请拖拽到细胞膜'
    case 'secretion': return '胞吐分泌中...'
    case 'finished': return '生产完成！'
    default: return ''
  }
})

const currentPhase = computed(() => {
  if (phase.value.includes('drag')) return 'action-needed'
  return 'processing'
})

// --- Actions ---

const startProduction = () => {
  if (phase.value !== 'idle') return
  phase.value = 'transcription'
  
  // Setup mRNA animation path (Nucleus -> ER)
  const nucleus = organelles.find(o => o.id === 'nucleus')!
  const er = organelles.find(o => o.id === 'er')!
  
  mRNA.x = nucleus.x
  mRNA.y = nucleus.y
  mRNA.active = true
  mRNA.progress = 0
  
  // Simple path
  mRNA.path = [
    {x: nucleus.x, y: nucleus.y},
    {x: (nucleus.x + er.x)/2, y: (nucleus.y + er.y)/2 - 50}, // Arc up
    {x: er.x, y: er.y}
  ]
}

const resetSim = () => {
  phase.value = 'idle'
  proteinState.value = 'none'
  protein.particles = []
  protein.x = -1000
  mRNA.active = false
  activeOrganelle.value = null
  showHint.value = false
}

// --- Interaction Handlers ---

const getMousePos = (e: MouseEvent) => {
  if (!canvas.value) return { x: 0, y: 0 }
  const rect = canvas.value.getBoundingClientRect()
  return {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  }
}

const onMouseDown = (e: MouseEvent) => {
  const { x, y } = getMousePos(e)
  
  // Check if clicking protein
  if (proteinState.value !== 'none' && phase.value.startsWith('drag')) {
    const dx = x - protein.x
    const dy = y - protein.y
    if (Math.sqrt(dx*dx + dy*dy) < protein.radius + 10) {
      protein.isDragging = true
      activeOrganelle.value = null // Clear organelle info when dragging starts
    }
  }
  
  // Check if clicking organelle (for info only)
  if (!protein.isDragging) {
    const hit = organelles.find(o => o.checkHit(x, y))
    if (hit) activeOrganelle.value = hit
    else activeOrganelle.value = null
  }
}

const onMouseMove = (e: MouseEvent) => {
  const { x, y } = getMousePos(e)
  
  if (protein.isDragging) {
    protein.x = x
    protein.y = y
    
    // Check potential targets
    let targetId = ''
    if (phase.value === 'drag_to_er') targetId = 'er'
    else if (phase.value === 'drag_to_golgi') targetId = 'golgi'
    else if (phase.value === 'drag_to_membrane') targetId = 'membrane'
    
    const target = organelles.find(o => o.id === targetId)
    if (target && target.checkHit(x, y)) {
      activeOrganelle.value = target
      showHint.value = true
      hintText.value = `松开鼠标进行${targetId === 'membrane' ? '分泌' : '加工'}`
      hintPos.x = x + 20
      hintPos.y = y
    } else {
      showHint.value = false
    }
  } else {
    // Hover effects
    if (canvas.value) canvas.value.style.cursor = 'default'
    
    // Hover protein
    if (proteinState.value !== 'none' && phase.value.startsWith('drag')) {
      const dx = x - protein.x
      const dy = y - protein.y
      if (Math.sqrt(dx*dx + dy*dy) < protein.radius + 10) {
        if (canvas.value) canvas.value.style.cursor = 'grab'
      }
    }
  }
}

const onMouseUp = () => {
  if (protein.isDragging) {
    protein.isDragging = false
    showHint.value = false
    
    // Logic for dropping
    checkDrop()
  }
}

const checkDrop = () => {
  const er = organelles.find(o => o.id === 'er')!
  const golgi = organelles.find(o => o.id === 'golgi')!
  const membrane = organelles.find(o => o.id === 'membrane')!
  
  if (phase.value === 'drag_to_er' && er.checkHit(protein.x, protein.y)) {
    phase.value = 'folding'
    protein.x = er.x
    protein.y = er.y
    activeOrganelle.value = er
    setTimeout(() => {
      proteinState.value = 'folded'
      phase.value = 'drag_to_golgi'
      ElMessage.success('蛋白质折叠完成！结构稳定。')
    }, 2000)
  }
  else if (phase.value === 'drag_to_golgi' && golgi.checkHit(protein.x, protein.y)) {
    phase.value = 'processing'
    protein.x = golgi.x
    protein.y = golgi.y
    activeOrganelle.value = golgi
    setTimeout(() => {
      proteinState.value = 'modified'
      phase.value = 'drag_to_membrane'
      ElMessage.success('蛋白质修饰完成！已打包进囊泡。')
    }, 2000)
  }
  else if (phase.value === 'drag_to_membrane' && membrane.checkHit(protein.x, protein.y)) {
    phase.value = 'secretion'
    activeOrganelle.value = membrane
    setTimeout(() => {
      phase.value = 'finished'
      proteinState.value = 'none' // Disappear
      ElMessage.success('胞吐成功！蛋白质已运输到细胞外。')
    }, 2000)
  }
}

// --- Rendering Loop ---

const draw = () => {
  const cvs = canvas.value
  if (!cvs) return
  const ctx = cvs.getContext('2d')
  if (!ctx) return
  
  const w = cvs.width
  const h = cvs.height
  
  // Clear
  ctx.fillStyle = '#0f172a'
  ctx.fillRect(0, 0, w, h)
  
  // Draw Cell Cytoplasm (Background)
  ctx.fillStyle = '#1e293b'
  ctx.fillRect(0, 0, w, h)
  
  // Draw Organelles
  organelles.forEach(org => {
    const isHighlight = activeOrganelle.value?.id === org.id || 
      (phase.value === 'folding' && org.id === 'er') ||
      (phase.value === 'processing' && org.id === 'golgi') ||
      (phase.value === 'secretion' && org.id === 'membrane')
      
    org.draw(ctx, isHighlight)
  })
  
  // Draw mRNA
  if (mRNA.active) {
    // Update mRNA pos
    if (phase.value === 'transcription') {
      mRNA.progress += 0.01
      if (mRNA.progress >= 1) {
        mRNA.progress = 1
        phase.value = 'translation'
        setTimeout(() => {
           // Translation Animation done -> Spawn protein
           const er = organelles.find(o => o.id === 'er')!
           protein.x = er.x
           protein.y = er.y
           proteinState.value = 'unfolded'
           initProteinParticles()
           phase.value = 'drag_to_er' // Technically it starts at ribosome, let's say free ribosome then moves to ER or starts on Rough ER.
           // Simpler: Starts at Nucleus/Cytoplasm boundary, drag to ER
           protein.x = (organelles[0].x + organelles[1].x) / 2
           protein.y = (organelles[0].y + organelles[1].y) / 2
        }, 2000)
      }
    }
    
    // Draw mRNA strand
    const p1 = mRNA.path[0]
    const p2 = mRNA.path[1]
    const p3 = mRNA.path[2]
    
    // Bezier interpolation
    const t = mRNA.progress
    const cx = (1-t)*(1-t)*p1.x + 2*(1-t)*t*p2.x + t*t*p3.x
    const cy = (1-t)*(1-t)*p1.y + 2*(1-t)*t*p2.y + t*t*p3.y
    
    ctx.strokeStyle = '#fbbf24'
    ctx.lineWidth = 4
    ctx.setLineDash([5, 5])
    ctx.beginPath()
    ctx.moveTo(p1.x, p1.y)
    ctx.quadraticCurveTo(p2.x, p2.y, cx, cy) // Only draw up to current
    ctx.stroke()
    ctx.setLineDash([])
    
    // Ribosome reading it
    if (phase.value === 'transcription' || phase.value === 'translation') {
      ctx.fillStyle = '#fbbf24'
      ctx.font = '20px Arial'
      ctx.fillText('mRNA', cx + 10, cy)
    }
  }
  
  // Draw Protein
  if (proteinState.value !== 'none') {
    ctx.save()
    ctx.translate(protein.x, protein.y)
    
    // Visuals based on state
    if (proteinState.value === 'unfolded') {
      // String of beads, wiggly
      ctx.strokeStyle = '#4ade80'
      ctx.lineWidth = 5
      ctx.lineCap = 'round'
      ctx.beginPath()
      const time = Date.now() * 0.005
      for(let i=-15; i<=15; i+=5) {
        ctx.lineTo(i, Math.sin(i * 0.5 + time) * 5)
      }
      ctx.stroke()
    } else if (proteinState.value === 'folded') {
      // Compact blob
      ctx.fillStyle = '#22c55e'
      ctx.beginPath()
      ctx.arc(0, 0, 12, 0, Math.PI*2)
      ctx.fill()
      ctx.fillStyle = '#86efac'
      ctx.beginPath()
      ctx.arc(-3, -3, 4, 0, Math.PI*2)
      ctx.fill()
    } else if (proteinState.value === 'modified') {
      // Blob with tags (sugar chains)
      ctx.fillStyle = '#22c55e'
      ctx.beginPath()
      ctx.arc(0, 0, 12, 0, Math.PI*2)
      ctx.fill()
      // Vesicle circle around it
      ctx.strokeStyle = '#fb923c'
      ctx.lineWidth = 2
      ctx.beginPath()
      ctx.arc(0, 0, 18, 0, Math.PI*2)
      ctx.stroke()
      // Tags
      ctx.fillStyle = '#facc15'
      ctx.fillRect(8, -8, 6, 6)
      ctx.fillRect(-10, 5, 6, 6)
    }
    
    // Glow if dragging
    if (protein.isDragging) {
      ctx.shadowBlur = 15
      ctx.shadowColor = 'white'
      ctx.strokeStyle = 'white'
      ctx.lineWidth = 2
      ctx.stroke() // Outline
      ctx.shadowBlur = 0
    }
    
    ctx.restore()
  }
  
  // Animation logic for Processing phases
  if (phase.value === 'folding' || phase.value === 'processing') {
     // Spin or pulse protein
     // Just visual effect handled in draw
  }
  
  if (phase.value === 'secretion') {
    // Move protein out
    protein.x += 2
    protein.y -= 2
  }
}

const initProteinParticles = () => {
  // Just setup if using particle system, simplified to shapes above
}

const animate = () => {
  draw()
  animationId = requestAnimationFrame(animate)
}

const resizeCanvas = () => {
  if (canvas.value && stageRef.value) {
    const w = stageRef.value.clientWidth
    const h = stageRef.value.clientHeight
    canvas.value.width = w
    canvas.value.height = h
    defineOrganelles(w, h)
    // Reposition protein if active? Reset easier
    if (proteinState.value !== 'none' && !protein.isDragging) {
       // Try to keep relative pos? Or just reset
       // For simplicity, user might need to reset if resize happens during game
    }
  }
}

onMounted(() => {
  window.addEventListener('resize', resizeCanvas)
  setTimeout(() => {
    resizeCanvas()
    animate()
  }, 100)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCanvas)
  cancelAnimationFrame(animationId)
})

</script>

<style scoped>
.cell-sim-container {
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

.info-panel {
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

.control-card h3 {
  color: #4ade80;
  margin-top: 0;
  margin-bottom: 1rem;
}

.status-text {
  color: #94a3b8;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}

.status-text span {
  color: #f1f5f9;
  font-weight: bold;
}
.status-text span.action-needed { color: #fbbf24; }

.start-btn {
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
  margin-bottom: 1.5rem;
  transition: all 0.2s;
}

.start-btn:hover:not(:disabled) {
  background: #1d4ed8;
}

.start-btn:disabled {
  background: #475569;
  cursor: not-allowed;
  opacity: 0.5;
}

.steps-indicator {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.step {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem;
  border-radius: 6px;
  background: #0f172a;
  opacity: 0.5;
  transition: all 0.3s;
}

.step.active {
  opacity: 1;
  background: #334155;
  border-left: 3px solid #4ade80;
}

.step .icon { font-size: 1.2rem; }
.step .label { font-size: 0.9rem; color: #e2e8f0; }
.arrow { text-align: center; color: #64748b; font-size: 0.8rem; }

.organelle-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
}

.organelle-info h3 {
  color: #38bdf8;
  margin-top: 0;
}

.organelle-info p {
  color: #cbd5e1;
  line-height: 1.6;
  font-size: 0.95rem;
}

.fun-fact {
  margin-top: auto;
  background: rgba(255, 255, 255, 0.05);
  padding: 0.75rem;
  border-radius: 8px;
  font-size: 0.9rem;
  color: #fcd34d;
}

.organelle-info.empty {
  justify-content: center;
  align-items: center;
  text-align: center;
}

.organelle-info.empty h3 { color: #64748b; }

.cell-stage {
  flex: 1;
  position: relative;
  padding: 0;
  overflow: hidden;
}

canvas {
  width: 100%;
  height: 100%;
  cursor: default;
}

.hint-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.hint-box {
  position: absolute;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  pointer-events: none;
  white-space: nowrap;
  transform: translateY(-100%);
  margin-top: -10px;
}

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
