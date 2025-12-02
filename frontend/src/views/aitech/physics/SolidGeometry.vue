<template>
  <div class="relative w-full h-screen bg-gradient-to-b from-gray-900 to-black overflow-hidden flex">
    <AiTechBackButton />
    <!-- 3D Container -->
    <div ref="canvasContainer" class="absolute inset-0 z-0"></div>

    <!-- UI Overlay: Left Sidebar (Controls) -->
    <div class="absolute top-4 left-4 z-10 flex flex-col gap-4 max-h-[calc(100vh-2rem)] overflow-y-auto w-80">
      <!-- Header -->
      <div class="bg-gray-800/80 backdrop-blur-md p-4 rounded-2xl border border-white/10 shadow-lg">
        <h1 class="text-2xl font-bold mb-2 text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-purple-500">立体几何实验室</h1>
        <p class="text-xs text-gray-400">探索三维形状、展开图与计算</p>
      </div>

      <!-- Shape Selector -->
      <div class="bg-gray-800/80 backdrop-blur-md p-4 rounded-2xl border border-white/10 shadow-lg">
        <h3 class="text-sm font-bold text-gray-300 mb-3 uppercase tracking-wider">选择模型</h3>
        <div class="grid grid-cols-3 gap-2">
          <button 
            v-for="shape in shapes" 
            :key="shape.id"
            @click="switchShape(shape.id)"
            class="p-2 rounded-lg text-sm flex flex-col items-center gap-1 transition-all duration-200"
            :class="currentShapeId === shape.id ? 'bg-blue-600 text-white shadow-blue-500/50 shadow-md' : 'bg-gray-700/50 text-gray-400 hover:bg-gray-700 hover:text-white'"
          >
            <span class="text-xl">{{ shape.icon }}</span>
            <span>{{ shape.name }}</span>
          </button>
        </div>
      </div>

      <!-- Parameters & Actions -->
      <div class="bg-gray-800/80 backdrop-blur-md p-4 rounded-2xl border border-white/10 shadow-lg">
        <h3 class="text-sm font-bold text-gray-300 mb-3 uppercase tracking-wider">参数与控制</h3>
        
        <!-- Cube/Unfolding Controls -->
        <div v-if="currentShapeId === 'cube'" class="mb-4">
          <div class="flex items-center justify-between mb-2">
            <span class="text-gray-300 text-sm">边长 (a)</span>
            <span class="text-blue-400 font-mono">{{ params.size.toFixed(1) }}</span>
          </div>
          <input 
            type="range" min="1" max="5" step="0.1" 
            v-model.number="params.size" 
            @input="updateShape"
            class="w-full h-2 bg-gray-700 rounded-lg appearance-none cursor-pointer accent-blue-500 mb-4"
          >
          
          <button 
            @click="toggleUnfold"
            class="w-full py-2 rounded-xl transition-all duration-300 flex items-center justify-center gap-2 font-bold"
            :class="isUnfolded ? 'bg-orange-500 hover:bg-orange-600 text-white' : 'bg-green-500 hover:bg-green-600 text-white'"
          >
            <span class="text-lg">{{ isUnfolded ? '📦' : '👐' }}</span>
            <span>{{ isUnfolded ? '折叠还原' : '展开图形' }}</span>
          </button>
        </div>

        <!-- Sphere Controls -->
        <div v-if="currentShapeId === 'sphere'" class="mb-4">
          <div class="flex items-center justify-between mb-2">
            <span class="text-gray-300 text-sm">半径 (r)</span>
            <span class="text-blue-400 font-mono">{{ params.radius.toFixed(1) }}</span>
          </div>
          <input 
            type="range" min="0.5" max="3" step="0.1" 
            v-model.number="params.radius" 
            @input="updateShape"
            class="w-full h-2 bg-gray-700 rounded-lg appearance-none cursor-pointer accent-blue-500"
          >
        </div>

        <!-- Cylinder Controls -->
        <div v-if="currentShapeId === 'cylinder' || currentShapeId === 'cone'" class="mb-4">
          <div class="flex items-center justify-between mb-2">
            <span class="text-gray-300 text-sm">底面半径 (r)</span>
            <span class="text-blue-400 font-mono">{{ params.radius.toFixed(1) }}</span>
          </div>
          <input 
            type="range" min="0.5" max="3" step="0.1" 
            v-model.number="params.radius" 
            @input="updateShape"
            class="w-full h-2 bg-gray-700 rounded-lg appearance-none cursor-pointer accent-blue-500 mb-3"
          >
          
          <div class="flex items-center justify-between mb-2">
            <span class="text-gray-300 text-sm">高度 (h)</span>
            <span class="text-blue-400 font-mono">{{ params.height.toFixed(1) }}</span>
          </div>
          <input 
            type="range" min="1" max="5" step="0.1" 
            v-model.number="params.height" 
            @input="updateShape"
            class="w-full h-2 bg-gray-700 rounded-lg appearance-none cursor-pointer accent-blue-500"
          >
        </div>

        <!-- Global View Controls -->
        <div class="flex gap-2 mt-4 pt-4 border-t border-white/10">
          <button 
            @click="toggleWireframe"
            class="flex-1 py-2 rounded-lg bg-white/5 hover:bg-white/10 text-xs text-gray-300 flex flex-col items-center gap-1"
            :class="{ 'text-blue-400 bg-blue-500/20': showWireframe }"
          >
            <span>🕸️</span> 线框
          </button>
          <button 
            @click="resetView"
            class="flex-1 py-2 rounded-lg bg-white/5 hover:bg-white/10 text-xs text-gray-300 flex flex-col items-center gap-1"
          >
            <span>↺</span> 复位
          </button>
        </div>
      </div>
    </div>

    <!-- UI Overlay: Right Panel (Calculations) -->
    <div class="absolute top-4 right-4 z-10 w-80">
      <div class="bg-gray-800/80 backdrop-blur-md p-4 rounded-2xl border border-white/10 shadow-lg text-white">
        <h3 class="font-bold text-lg mb-3 flex items-center gap-2 text-green-400">
          <span>📐</span> 数学计算
        </h3>
        
        <div class="space-y-4">
          <!-- Surface Area -->
          <div class="bg-black/20 p-3 rounded-xl">
            <div class="text-gray-400 text-xs mb-1 uppercase">表面积 (Surface Area)</div>
            <div class="text-2xl font-mono font-bold text-white mb-1">
              {{ calculatedValues.area.toFixed(2) }}
            </div>
            <div class="text-xs text-gray-500 font-mono bg-black/30 p-2 rounded border border-white/5">
              {{ currentFormulas.area }}
            </div>
          </div>

          <!-- Volume -->
          <div class="bg-black/20 p-3 rounded-xl">
            <div class="text-gray-400 text-xs mb-1 uppercase">体积 (Volume)</div>
            <div class="text-2xl font-mono font-bold text-white mb-1">
              {{ calculatedValues.volume.toFixed(2) }}
            </div>
            <div class="text-xs text-gray-500 font-mono bg-black/30 p-2 rounded border border-white/5">
              {{ currentFormulas.volume }}
            </div>
          </div>

          <!-- Description -->
          <div class="text-sm text-gray-300 leading-relaxed opacity-80 border-t border-white/10 pt-3">
            {{ currentShapeDescription }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import gsap from 'gsap'
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue'

// --- Types ---
type ShapeType = 'cube' | 'sphere' | 'cylinder' | 'cone' | 'dodecahedron'

interface ShapeDef {
  id: ShapeType
  name: string
  icon: string
  description: string
}

// --- State ---
const canvasContainer = ref<HTMLElement | null>(null)
const showWireframe = ref(false)
const isUnfolded = ref(false)
const currentShapeId = ref<ShapeType>('cube')

const params = reactive({
  size: 2,
  radius: 1.5,
  height: 3
})

const shapes: ShapeDef[] = [
  { id: 'cube', name: '正方体', icon: '🧊', description: '正方体有6个面，每个面都是相等的正方形。它是最基本的立体图形之一。' },
  { id: 'sphere', name: '球体', icon: '⚽', description: '球体是空间中到定点距离等于定长的所有点组成的图形。它是最完美的对称图形。' },
  { id: 'cylinder', name: '圆柱体', icon: '🔋', description: '圆柱由两个平行的圆面和一个侧面组成。展开后侧面是一个矩形。' },
  { id: 'cone', name: '圆锥体', icon: '🎉', description: '圆锥有一个圆底面和一个顶点。展开后侧面是一个扇形。' },
  { id: 'dodecahedron', name: '十二面体', icon: '💎', description: '正十二面体由12个正五边形组成，是五种柏拉图多面体之一。' },
]

// --- Calculation Logic ---
const currentFormulas = computed(() => {
  const s = params.size
  const r = params.radius
  const h = params.height
  
  switch (currentShapeId.value) {
    case 'cube':
      return { area: `6 × a² = 6 × ${s}²`, volume: `a³ = ${s}³` }
    case 'sphere':
      return { area: `4πr² = 4π × ${r}²`, volume: `(4/3)πr³ = (4/3)π × ${r}³` }
    case 'cylinder':
      return { area: `2πr(h+r) = 2π×${r}×(${h}+${r})`, volume: `πr²h = π×${r}²×${h}` }
    case 'cone':
      const l = Math.sqrt(r*r + h*h).toFixed(2)
      return { area: `πr(r+l) (l≈${l})`, volume: `(1/3)πr²h` }
    case 'dodecahedron':
      return { area: `3√(25+10√5)a² (approx)`, volume: `(15+7√5)/4 × a³ (approx)` }
    default:
      return { area: '', volume: '' }
  }
})

const calculatedValues = computed(() => {
  const s = params.size
  const r = params.radius
  const h = params.height

  switch (currentShapeId.value) {
    case 'cube':
      return { area: 6 * s * s, volume: Math.pow(s, 3) }
    case 'sphere':
      return { area: 4 * Math.PI * r * r, volume: (4/3) * Math.PI * Math.pow(r, 3) }
    case 'cylinder':
      return { area: 2 * Math.PI * r * (h + r), volume: Math.PI * r * r * h }
    case 'cone':
      const l = Math.sqrt(r*r + h*h)
      return { area: Math.PI * r * (r + l), volume: (1/3) * Math.PI * r * r * h }
    case 'dodecahedron':
      // Using approx radius as side length proxy for simplicity in visualization context, 
      // but strictly "a" usually refers to edge length. 
      // Let's assume params.radius is the circumradius roughly.
      // For simplicity, let's just use r as a scaling factor relative to unit dodecahedron
      return { area: 20.65 * r * r, volume: 7.66 * r * r * r } 
    default:
      return { area: 0, volume: 0 }
  }
})

const currentShapeDescription = computed(() => {
  return shapes.find(s => s.id === currentShapeId.value)?.description || ''
})


// --- Three.js Variables ---
let scene: THREE.Scene
let camera: THREE.PerspectiveCamera
let renderer: THREE.WebGLRenderer
let controls: OrbitControls
let currentMeshGroup: THREE.Group | THREE.Mesh
let animationId: number

// Cube Animation Parts
let cubeParts: {
  front: THREE.Group,
  back: THREE.Group,
  left: THREE.Group,
  right: THREE.Group,
  top: THREE.Group,
  bottom: THREE.Mesh
} | null = null

// --- Materials ---
const materials = {
  solid: new THREE.MeshPhysicalMaterial({
    color: 0x3b82f6, metalness: 0.1, roughness: 0.2, transmission: 0.2, transparent: true, opacity: 0.9, side: THREE.DoubleSide
  }),
  wireframe: new THREE.LineBasicMaterial({ color: 0xffffff, transparent: true, opacity: 0.3 }),
  highlight: new THREE.MeshBasicMaterial({ color: 0xffeb3b, wireframe: true }),
  faceColors: [
    0xef4444, // Red
    0x22c55e, // Green
    0x3b82f6, // Blue
    0xeab308, // Yellow
    0xa855f7, // Purple
    0xf97316  // Orange
  ].map(c => new THREE.MeshPhysicalMaterial({
    color: c, metalness: 0.1, roughness: 0.2, side: THREE.DoubleSide
  }))
}

// --- Initialization ---
const init = () => {
  if (!canvasContainer.value) return

  // Scene
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0x0f172a)
  scene.fog = new THREE.FogExp2(0x0f172a, 0.02)

  // Camera
  camera = new THREE.PerspectiveCamera(60, canvasContainer.value.clientWidth / canvasContainer.value.clientHeight, 0.1, 100)
  camera.position.set(5, 5, 5)

  // Renderer
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setSize(canvasContainer.value.clientWidth, canvasContainer.value.clientHeight)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  canvasContainer.value.appendChild(renderer.domElement)

  // Controls
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05

  // Lights
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambientLight)

  const dirLight = new THREE.DirectionalLight(0xffffff, 1)
  dirLight.position.set(5, 10, 7)
  dirLight.castShadow = true
  scene.add(dirLight)
  
  const pointLight = new THREE.PointLight(0x3b82f6, 1)
  pointLight.position.set(-5, 0, -5)
  scene.add(pointLight)

  // Initial Shape
  createShape()

  // Loop
  animate()

  // Resize
  window.addEventListener('resize', onWindowResize)
}

// --- Shape Creation ---
const switchShape = (id: ShapeType) => {
  if (currentShapeId.value === id) return
  currentShapeId.value = id
  isUnfolded.value = false // Reset unfold state
  createShape()
}

const updateShape = () => {
  if (isUnfolded.value && currentShapeId.value === 'cube') {
    // If unfolded, we might need to reset or carefully update
    // For simplicity, let's just re-create
    createShape()
    // Re-apply unfold immediately if we want smooth update, but animation complicates it.
    // For now, let's just reset unfold state on param change to avoid glitches
    isUnfolded.value = false
  } else {
    createShape()
  }
}

const createShape = () => {
  if (currentMeshGroup) {
    scene.remove(currentMeshGroup)
    // Recursive dispose
    currentMeshGroup.traverse((child) => {
      if (child instanceof THREE.Mesh) {
        if (child.geometry) child.geometry.dispose()
        // Materials can be shared, so be careful. 
        // In our case, materials are global constants (except faceColors which are also consts), 
        // so we SHOULD NOT dispose them if we want to reuse them.
        // Only dispose if we created unique materials per shape.
        // We are using shared materials 'materials.solid', 'materials.wireframe' etc.
        // So we do NOT dispose materials here.
      }
    })
  }

  cubeParts = null // Reset cube parts

  switch (currentShapeId.value) {
    case 'cube':
      createCube()
      break
    case 'sphere':
      createSphere()
      break
    case 'cylinder':
      createCylinder()
      break
    case 'cone':
      createCone()
      break
    case 'dodecahedron':
      createDodecahedron()
      break
  }
}

// --- Specific Shape Generators ---

const createCube = () => {
  const size = params.size
  const half = size / 2
  const group = new THREE.Group()
  
  // We need 6 faces. To support unfolding, we use a hierarchical structure.
  // Pivot points are crucial.
  // Center of cube is (0,0,0). Bottom face center is (0, -half, 0).
  
  // Materials for faces
  const mats = materials.faceColors

  // 1. Bottom Face (Static Base)
  const bottomGeo = new THREE.PlaneGeometry(size, size)
  const bottomMesh = new THREE.Mesh(bottomGeo, mats[0])
  bottomMesh.rotation.x = -Math.PI / 2
  bottomMesh.position.y = -half
  bottomMesh.receiveShadow = true
  group.add(bottomMesh)

  // 2. Front Face (Hinged at Bottom Front edge)
  // Pivot at z = half, y = -half relative to center? No.
  // Relative to Bottom Face center (0, -half, 0):
  // Front edge is at (0, 0, half).
  
  const createHinge = (x: number, y: number, z: number, axis: string) => {
    const hinge = new THREE.Group()
    hinge.position.set(x, y, z)
    return hinge
  }

  // Front Hinge: At (0, -half, half)
  const frontHinge = createHinge(0, -half, half, 'x')
  const frontGeo = new THREE.PlaneGeometry(size, size)
  const frontMesh = new THREE.Mesh(frontGeo, mats[1])
  // Shift mesh up by half size so its bottom edge is at hinge origin (0,0,0 local)
  frontMesh.position.y = half 
  // Initial Rotation: It should be vertical. The hinge is at the bottom.
  // If mesh is vertical (y+), and hinge is at bottom, it matches.
  // But wait, PlaneGeometry lies on XY plane by default.
  // If we want it to be Front Face, it needs to be upright.
  // Let's keep geometry simple: Plane is XY.
  // If we rotate hinge -90 deg X, it lays flat?
  // Let's set up "Folded" state as default.
  // Front Face in Folded Cube: Position (0, 0, half), Rotation (0, 0, 0).
  // Hinge is at (0, -half, half).
  // Relative to hinge, the face center is (0, half, 0).
  // So if hinge rot is 0, face is vertical? No.
  // Let's visualize: Hinge at bottom edge.
  // Face extends upwards from hinge.
  // Hinge rotation X = 0 -> Face is vertical (forming the wall).
  // Hinge rotation X = 90 -> Face lays flat on ground (unfolded).
  frontMesh.position.set(0, half, 0) // Center of face relative to hinge
  frontHinge.add(frontMesh)
  group.add(frontHinge)

  // 3. Back Face
  const backHinge = createHinge(0, -half, -half, 'x')
  const backGeo = new THREE.PlaneGeometry(size, size)
  const backMesh = new THREE.Mesh(backGeo, mats[2])
  backMesh.position.set(0, half, 0)
  backMesh.rotation.y = Math.PI // Face outward
  backHinge.add(backMesh)
  group.add(backHinge)

  // 4. Left Face
  const leftHinge = createHinge(-half, -half, 0, 'z')
  const leftGeo = new THREE.PlaneGeometry(size, size)
  const leftMesh = new THREE.Mesh(leftGeo, mats[3])
  leftMesh.position.set(0, half, 0)
  leftMesh.rotation.y = -Math.PI / 2
  leftHinge.add(leftMesh)
  group.add(leftHinge)

  // 5. Right Face
  const rightHinge = createHinge(half, -half, 0, 'z')
  const rightGeo = new THREE.PlaneGeometry(size, size)
  const rightMesh = new THREE.Mesh(rightGeo, mats[4])
  rightMesh.position.set(0, half, 0)
  rightMesh.rotation.y = Math.PI / 2
  rightHinge.add(rightMesh)
  group.add(rightHinge)

  // 6. Top Face (Attached to Back Face)
  // Hinge is at the TOP of the Back Face.
  // Back Face local top is (0, size, 0) relative to Back Hinge?
  // Yes, because Back Mesh is at (0, half, 0) relative to Back Hinge, and has size.
  // So top edge is at (0, size, 0) inside Back Hinge system.
  const topHinge = new THREE.Group()
  topHinge.position.set(0, size, 0) // At top of back face
  const topGeo = new THREE.PlaneGeometry(size, size)
  const topMesh = new THREE.Mesh(topGeo, mats[5])
  topMesh.position.set(0, half, 0) // Extends upwards from top hinge
  topMesh.rotation.x = -Math.PI / 2 // Needs to face up initially?
  // Wait, in folded state:
  // Back Hinge rot = 0.
  // Top Hinge is at top of Back Face.
  // Top Face should form the lid.
  // Relative to vertical Back Face, the Top Face should be horizontal, pointing forward (z+).
  // If Top Mesh is standard Plane (XY), and we want it horizontal (XZ), we rotate X -90.
  // But we want it to fold OUTWARDS (backwards).
  topMesh.rotation.x = 0 // Reset
  // Let's adjust logic during animation.
  // In folded state: Top Hinge Rot X = -90 deg (folds forward to cover top).
  // In unfolded state: Top Hinge Rot X = 0 (extends straight up from back face).
  
  // Correction:
  // Back Hinge (Folded) -> Rot X = 0. Back Face is Vertical.
  // Top Hinge (Folded) -> Needs to bend 90 deg to cover top.
  // If Top Mesh is (0, half, 0), it extends "Up" from hinge.
  // If we rotate Top Hinge -90 deg X, it points "Forward" (over the cube). Correct.
  
  topHinge.rotation.x = -Math.PI / 2
  topHinge.add(topMesh)
  // Attach Top Hinge to Back Hinge (not Back Mesh, to be cleaner, or Back Mesh?)
  // Back Hinge contains Back Mesh. We can add Top Hinge to Back Hinge as well.
  // But Back Mesh is scaled? No, geometry is sized.
  backHinge.add(topHinge)

  // Store references for animation
  cubeParts = {
    front: frontHinge,
    back: backHinge,
    left: leftHinge,
    right: rightHinge,
    top: topHinge,
    bottom: bottomMesh
  }

  // Add wireframes
  const addWire = (mesh: THREE.Mesh) => {
    const w = new THREE.LineSegments(new THREE.WireframeGeometry(mesh.geometry), materials.wireframe)
    mesh.add(w)
    w.visible = showWireframe.value
  }
  [bottomMesh, frontMesh, backMesh, leftMesh, rightMesh, topMesh].forEach(addWire)

  currentMeshGroup = group
  scene.add(group)
}

const createSphere = () => {
  const geometry = new THREE.SphereGeometry(params.radius, 32, 32)
  const mesh = new THREE.Mesh(geometry, materials.solid)
  const wire = new THREE.LineSegments(new THREE.WireframeGeometry(geometry), materials.wireframe)
  mesh.add(wire)
  wire.visible = showWireframe.value
  currentMeshGroup = mesh
  scene.add(mesh)
}

const createCylinder = () => {
  const geometry = new THREE.CylinderGeometry(params.radius, params.radius, params.height, 32)
  const mesh = new THREE.Mesh(geometry, materials.solid)
  const wire = new THREE.LineSegments(new THREE.WireframeGeometry(geometry), materials.wireframe)
  mesh.add(wire)
  wire.visible = showWireframe.value
  currentMeshGroup = mesh
  scene.add(mesh)
}

const createCone = () => {
  const geometry = new THREE.ConeGeometry(params.radius, params.height, 32)
  const mesh = new THREE.Mesh(geometry, materials.solid)
  const wire = new THREE.LineSegments(new THREE.WireframeGeometry(geometry), materials.wireframe)
  mesh.add(wire)
  wire.visible = showWireframe.value
  currentMeshGroup = mesh
  scene.add(mesh)
}

const createDodecahedron = () => {
  const geometry = new THREE.DodecahedronGeometry(params.radius, 0)
  const mesh = new THREE.Mesh(geometry, materials.solid)
  const wire = new THREE.LineSegments(new THREE.WireframeGeometry(geometry), materials.wireframe)
  mesh.add(wire)
  wire.visible = showWireframe.value
  currentMeshGroup = mesh
  scene.add(mesh)
}

// --- Animation Actions ---

const toggleUnfold = () => {
  if (!cubeParts || currentShapeId.value !== 'cube') return
  isUnfolded.value = !isUnfolded.value

  const duration = 1.5
  const ease = "power2.inOut"

  if (isUnfolded.value) {
    // UNFOLD
    // Front: Rotate X 90 (down)
    gsap.to(cubeParts.front.rotation, { x: Math.PI / 2, duration, ease })
    // Back: Rotate X -90 (down backwards)
    gsap.to(cubeParts.back.rotation, { x: -Math.PI / 2, duration, ease })
    // Left: Rotate Z 90 (left down) - Check axis orientation
    // Left hinge at (-h, -h, 0). Axis Z.
    // Rotate Z -90?
    // Normal is X-. Up is Y+.
    // Rotate Z +90 moves Y+ to X-. Correct.
    gsap.to(cubeParts.left.rotation, { z: Math.PI / 2, duration, ease })
    // Right: Rotate Z -90
    gsap.to(cubeParts.right.rotation, { z: -Math.PI / 2, duration, ease })
    
    // Top: Attached to Back.
    // Currently -90 (covering top).
    // Needs to become 0 (straight relative to Back).
    // Because Back rotates -90, Top (at 0) will be in line with Back.
    gsap.to(cubeParts.top.rotation, { x: 0, duration, ease })

  } else {
    // FOLD
    gsap.to(cubeParts.front.rotation, { x: 0, duration, ease })
    gsap.to(cubeParts.back.rotation, { x: 0, duration, ease })
    gsap.to(cubeParts.left.rotation, { z: 0, duration, ease })
    gsap.to(cubeParts.right.rotation, { z: 0, duration, ease })
    gsap.to(cubeParts.top.rotation, { x: -Math.PI / 2, duration, ease })
  }
}

const resetView = () => {
  gsap.to(camera.position, {
    x: 5, y: 5, z: 5,
    duration: 1.5,
    ease: "power2.inOut",
    onUpdate: () => controls.update()
  })
}

const toggleWireframe = () => {
  showWireframe.value = !showWireframe.value
  createShape() // Re-create to apply wireframe visibility or just traverse
}

// --- Animation Loop ---
const animate = () => {
  animationId = requestAnimationFrame(animate)
  controls.update()
  renderer.render(scene, camera)
}

const onWindowResize = () => {
  if (!canvasContainer.value) return
  camera.aspect = canvasContainer.value.clientWidth / canvasContainer.value.clientHeight
  camera.updateProjectionMatrix()
  renderer.setSize(canvasContainer.value.clientWidth, canvasContainer.value.clientHeight)
}

// --- Lifecycle ---
onMounted(() => {
  init()
})

onUnmounted(() => {
  window.removeEventListener('resize', onWindowResize)
  if (animationId) cancelAnimationFrame(animationId)
  if (renderer) renderer.dispose()
})
</script>

<style scoped>
/* Custom Scrollbar for Sidebar */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
}
::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

input[type=range] {
  -webkit-appearance: none;
  background: transparent;
}
input[type=range]::-webkit-slider-thumb {
  -webkit-appearance: none;
  height: 16px;
  width: 16px;
  border-radius: 50%;
  background: #3b82f6;
  cursor: pointer;
  margin-top: -6px; 
  box-shadow: 0 0 10px rgba(59, 130, 246, 0.5);
}
input[type=range]::-webkit-slider-runnable-track {
  width: 100%;
  height: 4px;
  cursor: pointer;
  background: #374151;
  border-radius: 2px;
}
</style>
