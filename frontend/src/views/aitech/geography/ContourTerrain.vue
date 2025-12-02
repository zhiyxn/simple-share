<template>
  <div class="contour-container">
    <AiTechBackButton />
    <!-- Header -->
    <div class="header">
      <div class="left-nav">
        <button class="back-btn" @click="$router.push('/aitech')">
          <el-icon><ArrowLeft /></el-icon> 返回教程
        </button>
        <h1>等高线与 3D 地貌映射</h1>
      </div>
      <div class="header-controls">
        <button class="action-btn" @click="regenerateMap">
          <el-icon><Refresh /></el-icon> 随机地形
        </button>
      </div>
    </div>

    <div class="content-layout">
      <!-- Controls Panel (Top or floating, let's put it on top for visibility) -->
      <div class="controls-bar card">
        <div class="control-item">
          <span class="label">🌊 海平面 ({{ seaLevel.toFixed(2) }})</span>
          <el-slider v-model="seaLevel" :min="0" :max="0.8" :step="0.01" />
        </div>
        <div class="control-item">
          <span class="label">🏔️ 地形复杂度</span>
          <el-slider v-model="complexity" :min="1" :max="5" :step="0.1" @change="regenerateMap" />
        </div>
        <div class="control-item info">
          <span class="highlight-info" v-if="hoverHeight !== null">
            当前选定高度: <span class="val">{{ hoverHeight.toFixed(2) }}</span>
          </span>
          <span class="highlight-info" v-else>
            👆 悬停在左侧线条上查看映射
          </span>
        </div>
      </div>

      <div class="viz-area">
        <!-- Left: 2D Contour Map -->
        <div class="viz-panel left card">
          <div class="panel-header">
            <h3>2D 等高线地形图</h3>
            <p>俯视视角 · 颜色代表海拔</p>
          </div>
          <div class="canvas-wrapper" ref="canvasWrapper">
            <canvas 
              ref="canvas2d"
              @mousemove="on2DMouseMove"
              @mouseleave="on2DMouseLeave"
            ></canvas>
            <!-- Legend -->
            <div class="legend">
              <div class="gradient-bar"></div>
              <div class="labels">
                <span>低 (Low)</span>
                <span>高 (High)</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Right: 3D Terrain -->
        <div class="viz-panel right card">
          <div class="panel-header">
            <h3>3D 真实地貌</h3>
            <p>立体视角 · 对应左侧地形</p>
          </div>
          <div class="three-container" ref="threeContainer"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import { ElSlider, ElIcon } from 'element-plus'
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls'

// --- State ---
const seaLevel = ref(0.2)
const complexity = ref(2.5)
const hoverHeight = ref<number | null>(null)

const canvas2d = ref<HTMLCanvasElement | null>(null)
const canvasWrapper = ref<HTMLDivElement | null>(null)
const threeContainer = ref<HTMLDivElement | null>(null)

// Data
const GRID_SIZE = 128
const heightMap: number[][] = [] // 0 to 1

// Three.js Objects
let scene: THREE.Scene
let camera: THREE.PerspectiveCamera
let renderer: THREE.WebGLRenderer
let controls: OrbitControls
let terrainMesh: THREE.Mesh
let waterMesh: THREE.Mesh
let planeHelper: THREE.Mesh

// --- Initialization ---

const initHeightMap = () => {
  heightMap.length = 0
  const offset = Math.random() * 1000
  
  for (let y = 0; y < GRID_SIZE; y++) {
    const row: number[] = []
    for (let x = 0; x < GRID_SIZE; x++) {
      // Simple superposition of sine waves for terrain
      // x, y normalized to 0-1
      const nx = x / GRID_SIZE
      const ny = y / GRID_SIZE
      
      const freq = complexity.value
      
      let h = 0
      h += Math.sin((nx + offset) * freq * 3.14) * Math.cos((ny + offset) * freq * 3.14) * 0.5
      h += Math.sin((nx + offset) * freq * 2 * 3.14 + 1) * Math.sin((ny + offset) * freq * 2 * 3.14 + 2) * 0.25
      h += Math.sin((nx + offset) * freq * 4 * 3.14) * 0.1
      
      // Normalize roughly to 0-1 range, centering around 0.5
      h = h + 0.5
      h = Math.max(0, Math.min(1, h))
      
      // Make edges lower to form an island
      const dx = nx - 0.5
      const dy = ny - 0.5
      const d = Math.sqrt(dx*dx + dy*dy)
      h = h * (1 - THREE.MathUtils.smoothstep(d, 0.3, 0.5))
      
      row.push(h)
    }
    heightMap.push(row)
  }
}

// --- 2D Drawing ---

const draw2D = () => {
  const cvs = canvas2d.value
  if (!cvs) return
  const ctx = cvs.getContext('2d')
  if (!ctx) return
  
  const w = cvs.width
  const h = cvs.height
  const cellW = w / GRID_SIZE
  const cellH = h / GRID_SIZE
  
  // 1. Draw Heightmap Background
  const imgData = ctx.createImageData(w, h)
  const data = imgData.data
  
  // Pre-calculate colors for performance? No, canvas is small enough
  // Actually, directly drawing rects is slow for 128x128. ImageData is better.
  // But we need scaling. Let's just use rects for simplicity if performance is ok, or scale manually.
  // Better: render to small offscreen canvas then scale up?
  // Let's stick to simple iteration.
  
  // We will sample heightMap to fill pixels
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      // Map pixel to grid
      const gx = Math.floor((x / w) * GRID_SIZE)
      const gy = Math.floor((y / h) * GRID_SIZE)
      const val = heightMap[gy][gx]
      
      const idx = (y * w + x) * 4
      
      // Color Map: Blue -> Green -> Red
      // < SeaLevel: Blue
      if (val < seaLevel.value) {
        // Water depth
        const depth = (seaLevel.value - val) / seaLevel.value // 0 to 1
        data[idx] = 0     // R
        data[idx+1] = 100 + (1-depth)*100 // G
        data[idx+2] = 200 + (1-depth)*55  // B
        data[idx+3] = 255
      } else {
        // Land: Green -> Yellow -> Red -> White
        // Normalize land height: 0 to 1 (relative to seaLevel -> 1)
        const landH = (val - seaLevel.value) / (1 - seaLevel.value)
        
        let r, g, b
        if (landH < 0.3) { // Green
           r = 34 + landH/0.3 * (200 - 34)
           g = 139 + landH/0.3 * (200 - 139)
           b = 34
        } else if (landH < 0.6) { // Yellow
           r = 200 + (landH-0.3)/0.3 * (255 - 200)
           g = 200 + (landH-0.3)/0.3 * (0 - 200) // to Orange
           b = 34
        } else if (landH < 0.9) { // Red/Brown
           r = 255
           g = 0 + (landH-0.6)/0.3 * (255 - 0) // To white
           b = 34 + (landH-0.6)/0.3 * (255 - 34)
        } else { // Snow
           r = 255; g = 255; b = 255;
        }
        
        data[idx] = r
        data[idx+1] = g
        data[idx+2] = b
        data[idx+3] = 255
      }
    }
  }
  ctx.putImageData(imgData, 0, 0)
  
  // 2. Draw Contour Lines
  const levels = 10
  const step = 1.0 / levels // 0.1, 0.2 ...
  
  ctx.lineWidth = 1
  
  // Marching squares is best, but simple threshold edge detection is easier to implement quickly
  // Horizontal and Vertical edge check
  
  // Helper to get line color
  const getLineColor = (h: number) => {
    if (hoverHeight.value !== null && Math.abs(h - hoverHeight.value) < 0.05) {
      return '#ffffff' // Highlight
    }
    return 'rgba(0,0,0,0.3)'
  }
  
  const getLineWidth = (h: number) => {
    if (hoverHeight.value !== null && Math.abs(h - hoverHeight.value) < 0.05) {
      return 3
    }
    return 1
  }
  
  // We draw paths for each level
  for (let l = 1; l < levels; l++) {
    const threshold = l * step
    if (threshold < seaLevel.value) continue // Don't draw underwater contours usually
    
    ctx.strokeStyle = getLineColor(threshold)
    ctx.lineWidth = getLineWidth(threshold)
    ctx.beginPath()
    
    // Simple contouring
    for (let y = 0; y < GRID_SIZE - 1; y++) {
      for (let x = 0; x < GRID_SIZE - 1; x++) {
        const h1 = heightMap[y][x]
        const h2 = heightMap[y][x+1]
        const h3 = heightMap[y+1][x]
        // const h4 = heightMap[y+1][x+1]
        
        // Check horizontal edge
        if ((h1 < threshold && h2 >= threshold) || (h1 >= threshold && h2 < threshold)) {
          // Linear interpolation for precise x
          const t = (threshold - h1) / (h2 - h1)
          const px = (x + t) * cellW
          const py = y * cellH
          // Draw a small segment? No, we need connected lines for smooth look. 
          // But simple segments are fine for this demo.
          ctx.moveTo(px, py)
          ctx.lineTo(px, py + cellH) // Crude
        }
        
        // Check vertical edge
        if ((h1 < threshold && h3 >= threshold) || (h1 >= threshold && h3 < threshold)) {
          const t = (threshold - h1) / (h3 - h1)
          const px = x * cellW
          const py = (y + t) * cellH
          ctx.moveTo(px, py)
          ctx.lineTo(px + cellW, py)
        }
      }
    }
    ctx.stroke()
  }
  
  // Draw Coastline (Sea Level)
  ctx.strokeStyle = '#0000ff'
  ctx.lineWidth = 2
  ctx.beginPath()
  const seaThreshold = seaLevel.value
   for (let y = 0; y < GRID_SIZE - 1; y++) {
      for (let x = 0; x < GRID_SIZE - 1; x++) {
        const h1 = heightMap[y][x]
        const h2 = heightMap[y][x+1]
        const h3 = heightMap[y+1][x]
        
        if ((h1 < seaThreshold && h2 >= seaThreshold) || (h1 >= seaThreshold && h2 < seaThreshold)) {
          const t = (seaThreshold - h1) / (h2 - h1)
          ctx.moveTo((x + t) * cellW, y * cellH)
          ctx.lineTo((x + t) * cellW, (y+1) * cellH)
        }
        if ((h1 < seaThreshold && h3 >= seaThreshold) || (h1 >= seaThreshold && h3 < seaThreshold)) {
          const t = (seaThreshold - h3) / (h3 - h1)
          ctx.moveTo(x * cellW, (y + t) * cellH)
          ctx.lineTo((x+1) * cellW, (y + t) * cellH)
        }
      }
   }
   ctx.stroke()
}

// --- 3D Scene ---

const initThree = () => {
  if (!threeContainer.value) return
  
  const w = threeContainer.value.clientWidth
  const h = threeContainer.value.clientHeight
  
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x0f172a)
  
  camera = new THREE.PerspectiveCamera(45, w / h, 0.1, 1000)
  camera.position.set(0, 80, 100)
  camera.lookAt(0, 0, 0)
  
  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setSize(w, h)
  threeContainer.value.appendChild(renderer.domElement)
  
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  
  // Lights
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambientLight)
  
  const dirLight = new THREE.DirectionalLight(0xffffff, 0.8)
  dirLight.position.set(50, 100, 50)
  scene.add(dirLight)
  
  // Terrain Geometry
  const geometry = new THREE.PlaneGeometry(100, 100, GRID_SIZE - 1, GRID_SIZE - 1)
  geometry.rotateX(-Math.PI / 2)
  
  // Material
  const material = new THREE.MeshStandardMaterial({ 
    color: 0x4ade80,
    roughness: 0.8,
    metalness: 0.1,
    side: THREE.DoubleSide,
    vertexColors: true
  })
  
  terrainMesh = new THREE.Mesh(geometry, material)
  scene.add(terrainMesh)
  
  // Water Plane
  const waterGeo = new THREE.PlaneGeometry(100, 100)
  waterGeo.rotateX(-Math.PI / 2)
  const waterMat = new THREE.MeshStandardMaterial({
    color: 0x3b82f6,
    transparent: true,
    opacity: 0.6,
    roughness: 0.1,
    metalness: 0.5
  })
  waterMesh = new THREE.Mesh(waterGeo, waterMat)
  scene.add(waterMesh)
  
  // Highlight Plane Helper
  const helperGeo = new THREE.PlaneGeometry(100, 100)
  helperGeo.rotateX(-Math.PI / 2)
  const helperMat = new THREE.MeshBasicMaterial({
    color: 0xffffff,
    transparent: true,
    opacity: 0.0, // Hidden by default
    side: THREE.DoubleSide
  })
  planeHelper = new THREE.Mesh(helperGeo, helperMat)
  scene.add(planeHelper)
  
  updateTerrainGeometry()
  animate()
}

const updateTerrainGeometry = () => {
  if (!terrainMesh) return
  
  const posAttr = terrainMesh.geometry.attributes.position
  const colors: number[] = []
  const colorObj = new THREE.Color()
  
  // Update Z (height) based on heightMap
  // PlaneGeometry vertices are row by row
  // heightMap is [y][x]
  
  for (let i = 0; i < posAttr.count; i++) {
    // Map index to grid coords
    // Plane geometry vertices are ordered: x goes 0..seg, then y increments
    // GRID_SIZE vertices per row
    const x = i % GRID_SIZE
    const y = Math.floor(i / GRID_SIZE)
    
    // The geometry is centered.
    // We map grid [0, GRID_SIZE-1] to the geometry
    
    if (heightMap[y] && heightMap[y][x] !== undefined) {
      const h = heightMap[y][x]
      // Set Y in 3D (since we rotated X by -90, Y is up)
      // Actually we rotated geometry, so local Z became world Y?
      // Plane starts in XY plane. Rotated -90 X -> XZ plane.
      // Local Z is displaced.
      
      // Let's verify. PlaneGeo created in XY. Vertices have x, y, z=0.
      // Rotated -90 around X. Now x=x, y=z, z=-y.
      // Usually simpler to just set Z of geometry before rotation, or Y after.
      // Let's assume we modify the attribute directly.
      // If rotated, the axes transform.
      // It's easier to NOT rotate geometry and just modify Z, then rotate mesh.
      // Or just modify Y if we consider it "up".
      // Standard PlaneGeo: Z is 0. We want height.
      // Let's set Z attribute.
      
      const heightScale = 30
      posAttr.setZ(i, h * heightScale)
      
      // Vertex Color
      // Simple gradient based on height
      if (h < seaLevel.value) {
         colorObj.setHSL(0.6, 0.5, 0.3) // Underwater dirt
      } else {
         // Green to Brown to White
         const landH = (h - seaLevel.value) / (1 - seaLevel.value)
         if (landH < 0.3) colorObj.setHSL(0.3, 0.8, 0.4) // Green
         else if (landH < 0.8) colorObj.setHSL(0.1, 0.6, 0.4) // Brown
         else colorObj.setHSL(0.0, 0.0, 1.0) // Snow
      }
      colors.push(colorObj.r, colorObj.g, colorObj.b)
    }
  }
  
  posAttr.needsUpdate = true
  terrainMesh.geometry.setAttribute('color', new THREE.Float32BufferAttribute(colors, 3))
  terrainMesh.geometry.computeVertexNormals()
}

const updateWaterLevel = () => {
  if (waterMesh) {
    waterMesh.position.y = seaLevel.value * 30
  }
}

const updateHelperPlane = () => {
  if (planeHelper) {
    if (hoverHeight.value !== null) {
      planeHelper.position.y = hoverHeight.value * 30
      planeHelper.material.opacity = 0.3 + Math.sin(Date.now() * 0.01) * 0.1 // Pulse
    } else {
      planeHelper.material.opacity = 0
    }
  }
}

const animate = () => {
  requestAnimationFrame(animate)
  controls.update()
  updateHelperPlane() // For pulsing
  renderer.render(scene, camera)
}

// --- Interaction ---

const on2DMouseMove = (e: MouseEvent) => {
  const cvs = canvas2d.value
  if (!cvs) return
  const rect = cvs.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  
  const scaleX = cvs.width / rect.width
  const scaleY = cvs.height / rect.height
  
  const gx = Math.floor((x * scaleX / cvs.width) * GRID_SIZE)
  const gy = Math.floor((y * scaleY / cvs.height) * GRID_SIZE)
  
  if (gx >= 0 && gx < GRID_SIZE && gy >= 0 && gy < GRID_SIZE) {
    const h = heightMap[gy][gx]
    // Snap to nearest contour level?
    // Levels are 0.1, 0.2...
    // Find closest
    const step = 0.1
    const closest = Math.round(h / step) * step
    
    // Only highlight if close enough to a line?
    // Or just highlight the plane at current mouse height?
    // "When hovering contour line" -> implies we should snap to line.
    if (Math.abs(h - closest) < 0.05) {
      hoverHeight.value = closest
    } else {
      hoverHeight.value = null
    }
    draw2D() // Redraw to update highlight
  }
}

const on2DMouseLeave = () => {
  hoverHeight.value = null
  draw2D()
}

const regenerateMap = () => {
  initHeightMap()
  draw2D()
  updateTerrainGeometry()
}

watch(seaLevel, () => {
  draw2D()
  updateWaterLevel()
})

onMounted(() => {
  initHeightMap()
  
  // Resize Canvas
  if (canvasWrapper.value && canvas2d.value) {
    canvas2d.value.width = canvasWrapper.value.clientWidth
    canvas2d.value.height = canvasWrapper.value.clientHeight
    draw2D()
  }
  
  initThree()
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  // Cleanup Three
  renderer?.dispose()
})

const handleResize = () => {
  if (canvasWrapper.value && canvas2d.value) {
    canvas2d.value.width = canvasWrapper.value.clientWidth
    canvas2d.value.height = canvasWrapper.value.clientHeight
    draw2D()
  }
  if (threeContainer.value && renderer && camera) {
    const w = threeContainer.value.clientWidth
    const h = threeContainer.value.clientHeight
    camera.aspect = w / h
    camera.updateProjectionMatrix()
    renderer.setSize(w, h)
  }
}

</script>

<style scoped>
.contour-container {
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
  flex-direction: column;
  padding: 1.5rem;
  gap: 1.5rem;
  height: calc(100vh - 80px);
}

.controls-bar {
  display: flex;
  align-items: center;
  gap: 2rem;
  padding: 1rem 2rem;
  background: #1e293b;
  border-radius: 12px;
}

.control-item {
  flex: 1;
  max-width: 300px;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.control-item.info {
  flex: 2;
  align-items: flex-end;
  justify-content: center;
  color: #94a3b8;
  font-size: 0.9rem;
}

.highlight-info .val {
  color: #4ade80;
  font-weight: bold;
  font-family: monospace;
  font-size: 1.1rem;
}

.label {
  font-size: 0.85rem;
  color: #cbd5e1;
}

.viz-area {
  flex: 1;
  display: flex;
  gap: 1.5rem;
  min-height: 0;
}

.viz-panel {
  flex: 1;
  background: #1e293b;
  border-radius: 12px;
  border: 1px solid #334155;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  padding: 1rem;
  background: rgba(0,0,0,0.2);
  border-bottom: 1px solid #334155;
}

.panel-header h3 {
  margin: 0;
  font-size: 1rem;
  color: #f1f5f9;
}

.panel-header p {
  margin: 0.25rem 0 0 0;
  font-size: 0.8rem;
  color: #94a3b8;
}

.canvas-wrapper {
  flex: 1;
  position: relative;
}

canvas {
  width: 100%;
  height: 100%;
  display: block;
}

.legend {
  position: absolute;
  bottom: 1rem;
  right: 1rem;
  background: rgba(0,0,0,0.6);
  padding: 0.5rem;
  border-radius: 6px;
  width: 150px;
}

.gradient-bar {
  height: 10px;
  width: 100%;
  background: linear-gradient(to right, #228b22, #ffd700, #ff4500, #ffffff);
  border-radius: 5px;
  margin-bottom: 4px;
}

.labels {
  display: flex;
  justify-content: space-between;
  font-size: 0.7rem;
  color: #cbd5e1;
}

.three-container {
  flex: 1;
}

</style>
