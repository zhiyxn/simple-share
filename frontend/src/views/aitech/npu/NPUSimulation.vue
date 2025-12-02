<template>
  <div class="npu-sim-container" ref="container">
    <AiTechBackButton />
    <!-- UI Overlay -->
    <div class="ui-overlay">
      <div class="header">
        <button class="back-btn" @click="$router.push('/aitech')">
          <el-icon><ArrowLeft /></el-icon> 返回教程
        </button>
        <h1>NPU 神经处理单元模拟</h1>
      </div>
      
      <div class="controls-panel">
        <div class="control-group">
          <h3>工作模式</h3>
          <div class="mode-buttons">
            <button @click="setMode('idle')" :class="{ active: currentMode === 'idle' }">待机</button>
            <button @click="setMode('training')" :class="{ active: currentMode === 'training' }">训练模式 (Training)</button>
            <button @click="setMode('inference')" :class="{ active: currentMode === 'inference' }">推理模式 (Inference)</button>
          </div>
        </div>
        <div class="status-panel">
          <div class="status-item">
            <span class="label">核心利用率</span>
            <div class="progress-bar">
              <div class="progress" :style="{ width: utilization + '%', backgroundColor: getStatusColor(utilization) }"></div>
            </div>
            <span class="value">{{ utilization }}%</span>
          </div>
          <div class="status-item">
            <span class="label">显存带宽</span>
            <div class="progress-bar">
              <div class="progress" :style="{ width: (bandwidth / 20) + '%', backgroundColor: '#409eff' }"></div>
            </div>
            <span class="value">{{ bandwidth }} GB/s</span>
          </div>
        </div>
        <div class="hint-text">
          <p>💡 点击芯片组件查看详情</p>
          <p>🖱️ 拖动旋转，滚轮缩放</p>
        </div>
      </div>

      <!-- Info Panel -->
      <transition name="slide-fade">
        <div v-if="selectedPart" class="info-panel">
          <div class="panel-header">
            <h3>{{ selectedPart.name }}</h3>
            <button class="close-btn" @click="selectedPart = null">×</button>
          </div>
          <div class="panel-content">
            <p class="description">{{ selectedPart.description }}</p>
            <div class="features-list" v-if="selectedPart.features">
              <h4>关键特性:</h4>
              <ul>
                <li v-for="(feature, idx) in selectedPart.features" :key="idx">{{ feature }}</li>
              </ul>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, shallowRef, reactive, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue'

// --- Types ---
interface ComponentInfo {
  name: string
  description: string
  features?: string[]
}

interface PartMesh extends THREE.Mesh {
  userData: {
    info: ComponentInfo
    isInteractable: boolean
    originalColor?: number
    glowColor?: number
  }
}

// --- State ---
const container = ref<HTMLElement | null>(null)
const currentMode = ref<'idle' | 'training' | 'inference'>('idle')
const utilization = ref(0)
const bandwidth = ref(0)
const selectedPart = ref<ComponentInfo | null>(null)

// Three.js objects
const scene = shallowRef<THREE.Scene | null>(null)
const camera = shallowRef<THREE.PerspectiveCamera | null>(null)
const renderer = shallowRef<THREE.WebGLRenderer | null>(null)
const controls = shallowRef<OrbitControls | null>(null)
const raycaster = new THREE.Raycaster()
const mouse = new THREE.Vector2()
const interactableMeshes: PartMesh[] = []
let animationId: number

// Particles system for data flow
const particles: { mesh: THREE.Mesh, path: THREE.Vector3[], speed: number, progress: number }[] = []

// --- Configuration ---
const COMPONENT_INFO: Record<string, ComponentInfo> = {
  'HBM': {
    name: 'HBM (高带宽内存)',
    description: 'High Bandwidth Memory。堆叠式内存技术，提供极高的数据传输速度，是 AI 芯片存取大规模模型参数的关键。',
    features: ['3D 堆叠结构', '超高带宽', '低功耗']
  },
  'MatrixUnit': {
    name: '矩阵运算单元 (Matrix Unit)',
    description: 'NPU 的核心计算引擎，专门用于加速深度学习中的矩阵乘法运算 (GEMM)。',
    features: ['张量核心 (Tensor Cores)', '并行计算', '混合精度支持 (FP16/INT8)']
  },
  'ControlUnit': {
    name: '控制单元 (Control Unit)',
    description: '负责指令调度和数据流控制，确保各个计算核心协同工作。',
    features: ['指令解码', '任务调度', '流水线管理']
  },
  'Cache': {
    name: '片上缓存 (SRAM)',
    description: '高速片上存储，用于暂存频繁访问的数据，减少对 HBM 的访问延迟。',
    features: ['低延迟', '高吞吐', '数据复用']
  },
  'Interconnect': {
    name: '片上互联 (Interconnect)',
    description: '连接各个计算核心和存储单元的高速通道，类似于城市的快速路网。',
    features: ['高带宽', '低延迟', 'Mesh/Ring 拓扑']
  }
}

// --- Three.js Setup ---
const initThree = () => {
  if (!container.value) return

  // Scene
  const s = new THREE.Scene()
  s.background = new THREE.Color(0x0a0a12)
  s.fog = new THREE.FogExp2(0x0a0a12, 0.002)
  scene.value = s

  // Camera
  const c = new THREE.PerspectiveCamera(60, container.value.clientWidth / container.value.clientHeight, 0.1, 1000)
  c.position.set(30, 40, 50)
  camera.value = c

  // Renderer
  const r = new THREE.WebGLRenderer({ antialias: true })
  r.setSize(container.value.clientWidth, container.value.clientHeight)
  r.setPixelRatio(window.devicePixelRatio)
  r.shadowMap.enabled = true
  container.value.appendChild(r.domElement)
  renderer.value = r

  // Controls
  const ctrl = new OrbitControls(c, r.domElement)
  ctrl.enableDamping = true
  ctrl.dampingFactor = 0.05
  ctrl.maxPolarAngle = Math.PI / 2 - 0.1 // Prevent going below ground
  controls.value = ctrl

  // Lights
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.4)
  s.add(ambientLight)

  const dirLight = new THREE.DirectionalLight(0x409eff, 2)
  dirLight.position.set(20, 50, 20)
  dirLight.castShadow = true
  s.add(dirLight)

  const pointLight = new THREE.PointLight(0xff00ff, 1, 100)
  pointLight.position.set(-20, 30, -20)
  s.add(pointLight)

  // Build NPU Model
  buildNPUModel(s)

  // Grid Helper
  const gridHelper = new THREE.GridHelper(200, 50, 0x333333, 0x111111)
  s.add(gridHelper)

  // Events
  r.domElement.addEventListener('mousemove', onMouseMove)
  r.domElement.addEventListener('click', onClick)
  window.addEventListener('resize', onWindowResize)

  // Start Loop
  animate()
}

const buildNPUModel = (s: THREE.Scene) => {
  // 1. PCB Board
  const pcbGeo = new THREE.BoxGeometry(60, 1, 60)
  const pcbMat = new THREE.MeshStandardMaterial({ color: 0x052205, roughness: 0.8 })
  const pcb = new THREE.Mesh(pcbGeo, pcbMat)
  pcb.position.y = -0.5
  s.add(pcb)

  // 2. Chip Substrate (Package)
  const subGeo = new THREE.BoxGeometry(30, 1, 30)
  const subMat = new THREE.MeshStandardMaterial({ color: 0x1a1a1a, roughness: 0.5 })
  const substrate = new THREE.Mesh(subGeo, subMat)
  substrate.position.y = 0.5
  s.add(substrate)

  // 3. NPU Die (The actual silicon)
  const dieGeo = new THREE.BoxGeometry(20, 0.5, 20)
  const dieMat = new THREE.MeshStandardMaterial({ color: 0x222222, metalness: 0.8, roughness: 0.2 })
  const die = new THREE.Mesh(dieGeo, dieMat)
  die.position.y = 1.25
  s.add(die)

  // 4. Cores (Matrix Units) - Grid on the die
  const gridSize = 4
  const coreSize = 4
  const gap = 0.5
  const startX = -((gridSize * coreSize) + ((gridSize - 1) * gap)) / 2 + coreSize / 2
  const startZ = startX

  for (let i = 0; i < gridSize; i++) {
    for (let j = 0; j < gridSize; j++) {
      const coreGeo = new THREE.BoxGeometry(coreSize, 0.6, coreSize)
      const coreMat = new THREE.MeshStandardMaterial({ 
        color: 0x333333, 
        emissive: 0x000000,
        metalness: 0.5
      })
      const core = new THREE.Mesh(coreGeo, coreMat) as PartMesh
      core.position.set(
        startX + i * (coreSize + gap),
        1.6,
        startZ + j * (coreSize + gap)
      )
      
      core.userData = {
        info: COMPONENT_INFO['MatrixUnit'],
        isInteractable: true,
        originalColor: 0x333333,
        glowColor: 0x00ff00
      }
      
      s.add(core)
      interactableMeshes.push(core)
    }
  }

  // 5. HBM Stacks (Memory)
  const hbmPositions = [
    { x: -20, z: -10 }, { x: -20, z: 10 },
    { x: 20, z: -10 }, { x: 20, z: 10 }
  ]

  hbmPositions.forEach(pos => {
    const hbmGeo = new THREE.BoxGeometry(6, 4, 8)
    const hbmMat = new THREE.MeshStandardMaterial({ color: 0x111111, metalness: 0.9, roughness: 0.1 })
    const hbm = new THREE.Mesh(hbmGeo, hbmMat) as PartMesh
    hbm.position.set(pos.x, 2.5, pos.z)
    
    hbm.userData = {
      info: COMPONENT_INFO['HBM'],
      isInteractable: true,
      originalColor: 0x111111,
      glowColor: 0x00aaff
    }
    
    s.add(hbm)
    interactableMeshes.push(hbm)
  })

  // 6. Central Controller (in the middle of die, maybe underneath or logical)
  // Let's add a visual block for Control Unit/Cache in the center if we had space, 
  // but our grid is full. Let's add "Interconnect" lines.
}

// --- Animation & Simulation ---

const createDataParticle = (from: THREE.Vector3, to: THREE.Vector3, color: number) => {
  const geometry = new THREE.SphereGeometry(0.3, 8, 8)
  const material = new THREE.MeshBasicMaterial({ color: color })
  const mesh = new THREE.Mesh(geometry, material)
  
  // Calculate path points (simple arc)
  const midPoint = new THREE.Vector3().addVectors(from, to).multiplyScalar(0.5)
  midPoint.y += 5 // Arc height

  const path = [from, midPoint, to]
  
  scene.value?.add(mesh)
  particles.push({
    mesh,
    path,
    speed: 0.02 + Math.random() * 0.01,
    progress: 0
  })
}

const updateSimulation = () => {
  if (!scene.value) return

  // Simulate Data Flow based on mode
  if (currentMode.value !== 'idle') {
    // Spawn particles randomly
    if (Math.random() < (currentMode.value === 'training' ? 0.1 : 0.05)) {
      // Pick random HBM
      const hbms = interactableMeshes.filter(m => m.userData.info.name.includes('HBM'))
      const cores = interactableMeshes.filter(m => m.userData.info.name.includes('Matrix'))
      
      if (hbms.length > 0 && cores.length > 0) {
        const source = hbms[Math.floor(Math.random() * hbms.length)]
        const target = cores[Math.floor(Math.random() * cores.length)]
        
        // HBM -> Core (Read)
        createDataParticle(source.position.clone(), target.position.clone(), 0x00ffff)
      }
    }

    // Core glow effect
    interactableMeshes.forEach(mesh => {
      if (mesh.userData.info.name.includes('Matrix')) {
        const mat = mesh.material as THREE.MeshStandardMaterial
        if (Math.random() < (currentMode.value === 'training' ? 0.1 : 0.05)) {
           mat.emissive.setHex(currentMode.value === 'training' ? 0xff0000 : 0x00ff00)
        } else {
           mat.emissive.lerp(new THREE.Color(0x000000), 0.1)
        }
      }
    })

    // Update metrics
    if (currentMode.value === 'training') {
      utilization.value = Math.min(100, Math.max(utilization.value + (Math.random() * 10 - 4), 80))
      bandwidth.value = Math.min(2000, Math.max(bandwidth.value + (Math.random() * 100 - 40), 1200))
    } else {
      utilization.value = Math.min(80, Math.max(utilization.value + (Math.random() * 10 - 5), 40))
      bandwidth.value = Math.min(1000, Math.max(bandwidth.value + (Math.random() * 50 - 25), 400))
    }

  } else {
    // Idle mode cleanup
    utilization.value = Math.max(0, utilization.value - 5)
    bandwidth.value = Math.max(0, bandwidth.value - 50)
    
    interactableMeshes.forEach(mesh => {
      if (mesh.userData.info.name.includes('Matrix')) {
        (mesh.material as THREE.MeshStandardMaterial).emissive.setHex(0x000000)
      }
    })
  }

  // Update Particles
  for (let i = particles.length - 1; i >= 0; i--) {
    const p = particles[i]
    p.progress += p.speed
    
    if (p.progress >= 1) {
      scene.value.remove(p.mesh)
      particles.splice(i, 1)
      continue
    }

    // Quadratic Bezier Curve
    const t = p.progress
    const pos = new THREE.Vector3()
    const p0 = p.path[0]
    const p1 = p.path[1]
    const p2 = p.path[2]

    pos.x = (1 - t) * (1 - t) * p0.x + 2 * (1 - t) * t * p1.x + t * t * p2.x
    pos.y = (1 - t) * (1 - t) * p0.y + 2 * (1 - t) * t * p1.y + t * t * p2.y
    pos.z = (1 - t) * (1 - t) * p0.z + 2 * (1 - t) * t * p1.z + t * t * p2.z
    
    p.mesh.position.copy(pos)
  }
}

const animate = () => {
  animationId = requestAnimationFrame(animate)
  if (controls.value) controls.value.update()
  updateSimulation()
  if (renderer.value && scene.value && camera.value) {
    renderer.value.render(scene.value, camera.value)
  }
}

// --- Interaction ---
const hoverObject = ref<THREE.Object3D | null>(null)

const onMouseMove = (event: MouseEvent) => {
  if (!camera.value || !container.value) return
  
  const rect = container.value.getBoundingClientRect()
  mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

  raycaster.setFromCamera(mouse, camera.value)
  const intersects = raycaster.intersectObjects(interactableMeshes)

  if (intersects.length > 0) {
    const obj = intersects[0].object as PartMesh
    if (hoverObject.value !== obj) {
      // Reset previous
      if (hoverObject.value) {
        // Optional: restore previous hover state
      }
      hoverObject.value = obj
      document.body.style.cursor = 'pointer'
    }
  } else {
    hoverObject.value = null
    document.body.style.cursor = 'default'
  }
}

const onClick = (event: MouseEvent) => {
  if (hoverObject.value) {
    const mesh = hoverObject.value as PartMesh
    selectedPart.value = mesh.userData.info
    
    // Highlight effect
    const mat = mesh.material as THREE.MeshStandardMaterial
    const oldEmissive = mat.emissive.getHex()
    mat.emissive.setHex(0xffffff)
    setTimeout(() => {
      mat.emissive.setHex(oldEmissive)
    }, 200)
  }
}

const onWindowResize = () => {
  if (!camera.value || !renderer.value || !container.value) return
  camera.value.aspect = container.value.clientWidth / container.value.clientHeight
  camera.value.updateProjectionMatrix()
  renderer.value.setSize(container.value.clientWidth, container.value.clientHeight)
}

const setMode = (mode: 'idle' | 'training' | 'inference') => {
  currentMode.value = mode
}

const getStatusColor = (val: number) => {
  if (val < 50) return '#67c23a'
  if (val < 80) return '#e6a23c'
  return '#f56c6c'
}

onMounted(() => {
  initThree()
})

onUnmounted(() => {
  cancelAnimationFrame(animationId)
  window.removeEventListener('resize', onWindowResize)
  if (renderer.value) {
    renderer.value.dispose()
  }
})
</script>

<style scoped>
.npu-sim-container {
  width: 100%;
  height: 100vh;
  position: relative;
  overflow: hidden;
  background: #0a0a12;
  color: white;
}

.ui-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none; /* Let clicks pass through to canvas */
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.header {
  display: flex;
  align-items: center;
  gap: 20px;
  pointer-events: auto;
  margin-bottom: 20px;
}

.back-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.header h1 {
  font-size: 24px;
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(0,0,0,0.5);
}

.controls-panel {
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  padding: 20px;
  border-radius: 16px;
  width: 300px;
  pointer-events: auto;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.control-group h3 {
  margin-bottom: 10px;
  font-size: 14px;
  color: #94a3b8;
}

.mode-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.mode-buttons button {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #cbd5e1;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s;
}

.mode-buttons button:hover {
  background: rgba(255, 255, 255, 0.1);
}

.mode-buttons button.active {
  background: #409eff;
  color: white;
  border-color: #409eff;
  box-shadow: 0 0 15px rgba(64, 158, 255, 0.3);
}

.status-panel {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.status-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
}

.status-item .label {
  width: 70px;
  color: #94a3b8;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress {
  height: 100%;
  transition: width 0.3s ease, background-color 0.3s ease;
}

.status-item .value {
  width: 60px;
  text-align: right;
  font-family: monospace;
}

.hint-text {
  font-size: 12px;
  color: #64748b;
  text-align: center;
  margin-top: 10px;
}

.info-panel {
  position: absolute;
  right: 20px;
  top: 20px;
  width: 320px;
  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px;
  pointer-events: auto;
  border: 1px solid rgba(64, 158, 255, 0.3);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 10px;
}

.panel-header h3 {
  font-size: 18px;
  color: #409eff;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  color: #94a3b8;
  font-size: 24px;
  cursor: pointer;
  line-height: 1;
}

.close-btn:hover {
  color: white;
}

.description {
  color: #cbd5e1;
  line-height: 1.6;
  margin-bottom: 15px;
  font-size: 14px;
}

.features-list h4 {
  color: #94a3b8;
  font-size: 12px;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.features-list ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.features-list li {
  color: #e2e8f0;
  font-size: 13px;
  padding: 4px 0;
  padding-left: 15px;
  position: relative;
}

.features-list li::before {
  content: '•';
  color: #409eff;
  position: absolute;
  left: 0;
}

/* Transitions */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease-out;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(20px);
  opacity: 0;
}
</style>
