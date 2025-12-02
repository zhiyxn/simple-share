<template>
  <div ref="container" class="w-full h-screen relative bg-gradient-to-b from-gray-900 to-black overflow-hidden">
    <AiTechBackButton />
    <!-- 顶部科普面板 (Top Info Panel) -->
    <div class="absolute top-0 left-0 w-full p-4 md:p-6 z-20 pointer-events-none bg-gradient-to-b from-black/80 via-black/40 to-transparent">
      <div class="flex justify-between items-start">
        <h1 class="text-2xl md:text-4xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-green-400 to-emerald-600 filter drop-shadow-lg">
          光合作用实验室
        </h1>
        <!-- 进度指示器 -->
        <div class="flex gap-1 mt-2">
          <div v-for="(s, i) in steps" :key="i" 
            class="h-1.5 rounded-full transition-all duration-300"
            :class="i === currentStepIndex ? 'w-8 bg-green-400' : 'w-2 bg-white/20'"
          ></div>
        </div>
      </div>

      <div class="mt-4 max-w-2xl">
        <transition name="fade" mode="out-in">
          <div :key="currentStepIndex" class="bg-black/40 backdrop-blur-md p-4 md:p-6 rounded-xl border border-white/10 shadow-xl pointer-events-auto">
            <h2 class="text-lg md:text-2xl font-bold text-green-300 mb-2 flex items-center gap-2">
              <span class="bg-green-500/20 text-green-400 text-xs md:text-sm px-2 py-0.5 rounded uppercase tracking-wider">Step {{ currentStepIndex + 1 }}</span>
              {{ steps[currentStepIndex].title }}
            </h2>
            <p class="text-gray-100 text-sm md:text-lg leading-relaxed font-light tracking-wide">
              {{ steps[currentStepIndex].description }}
            </p>
            <!-- 关键知识点标签 -->
            <div class="mt-3 flex flex-wrap gap-2">
              <span v-for="tag in steps[currentStepIndex].tags" :key="tag" class="text-xs text-white/70 bg-white/10 px-2 py-1 rounded">
                # {{ tag }}
              </span>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <!-- 底部控制栏 (Bottom Controls) -->
    <div class="absolute bottom-0 left-0 w-full p-6 z-20 bg-gradient-to-t from-black/90 to-transparent flex justify-center items-end pointer-events-none">
      <div class="flex items-center gap-6 pointer-events-auto mb-4 md:mb-8">
        <button 
          @click="prevStep" 
          :disabled="currentStepIndex === 0"
          class="p-4 rounded-full bg-white/10 backdrop-blur-md border border-white/20 text-white hover:bg-white/20 disabled:opacity-30 transition-all active:scale-90"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 md:h-8 md:w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>

        <button 
          @click="nextStep"
          class="group relative px-8 py-4 md:px-12 md:py-5 bg-gradient-to-r from-green-600 to-emerald-600 rounded-full text-white font-bold shadow-[0_0_30px_rgba(16,185,129,0.4)] hover:shadow-[0_0_40px_rgba(16,185,129,0.6)] hover:scale-105 active:scale-95 transition-all duration-300"
        >
          <span class="relative z-10 flex items-center gap-3 text-lg md:text-xl">
            {{ currentStepIndex === steps.length - 1 ? '再次探索' : '下一步' }}
            <svg v-if="currentStepIndex !== steps.length - 1" xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 group-hover:translate-x-1 transition-transform" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
            </svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
          </span>
        </button>
      </div>
    </div>

    <!-- 3D Canvas Container -->
    <div ref="canvasContainer" class="w-full h-full touch-none z-0"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { CSS2DRenderer, CSS2DObject } from 'three/examples/jsm/renderers/CSS2DRenderer.js';
import gsap from 'gsap';
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue'

// --- State ---
const currentStepIndex = ref(0);
const canvasContainer = ref<HTMLElement | null>(null);

// 科学步骤数据 (Updated Narrative)
const steps = [
  {
    title: "生命共生 (Symbiosis)",
    description: "在一个阳光明媚的午后，室内绿植正静静地进行着一项伟大的工作。它吸收着人类（您）呼吸产生的二氧化碳，并源源不断地释放出清新的氧气。这构成了地球生命最基础的互补循环。",
    tags: ["宏观生态", "人与自然", "气体交换"],
    camPos: { x: 25, y: 15, z: 25 },
    target: { x: 0, y: 8, z: 0 },
    scene: 'macro'
  },
  {
    title: "阳光沐浴 (Solar Power)",
    description: "窗外的阳光洒在巨大的龟背竹叶片上。对于植物而言，光就是食物。每一束光线都携带者能量，准备唤醒叶片中沉睡的分子机器。",
    tags: ["光照模拟", "能量输入", "光子流"],
    camPos: { x: -10, y: 18, z: 10 },
    target: { x: -5, y: 10, z: 0 },
    scene: 'macro'
  },
  {
    title: "微观穿越 (Dive In)",
    description: "让我们跟随光子的脚步，缩小千万倍，穿越叶片的表皮。您即将进入植物细胞内部，亲眼见证那个将光能转化为化学能的奇迹工厂。",
    tags: ["微观视角", "细胞壁", "穿越"],
    camPos: { x: -5, y: 12, z: 2 },
    target: { x: -5, y: 11, z: 0 },
    scene: 'transition'
  },
  {
    title: "叶绿体结构 (The Chloroplast)",
    description: "欢迎来到叶绿体！这是植物细胞的“动力核心”。绿色的硬币状结构是“类囊体”，它们堆叠在一起，如同一个个微型太阳能电池阵列。",
    tags: ["叶绿体", "类囊体", "基质"],
    camPos: { x: 0, y: 20, z: 45 },
    target: { x: 0, y: 0, z: 0 },
    scene: 'micro'
  },
  {
    title: "光反应：能量捕获 (Light Reaction)",
    description: "光反应开始！太阳光子撞击类囊体膜上的叶绿素。这种撞击瞬间激发了电子，产生了 ATP 和 NADPH——这是驱动后续反应的“生物电池”。",
    tags: ["光子撞击", "电子激发", "能量转化"],
    camPos: { x: -12, y: 8, z: 18 },
    target: { x: -4, y: 0, z: 0 },
    scene: 'micro'
  },
  {
    title: "光反应：水的光解 (Water Splitting)",
    description: "植物喝下的水在这里发挥作用。光能将水分子(H₂O)强行拆解，释放出氧气(O₂)。这就是为什么植物能让空气清新的原因！",
    tags: ["H₂O → O₂", "产生氧气", "生命之源"],
    camPos: { x: 0, y: 2, z: 25 },
    target: { x: 0, y: -5, z: 0 },
    scene: 'micro'
  },
  {
    title: "暗反应：固碳 (Carbon Fixation)",
    description: "现在进入基质中。这里不需要光。人类呼出的二氧化碳(CO₂)在这里被“捕捉”，并利用刚才产生的能量，开始合成有机物。",
    tags: ["卡尔文循环", "吸收CO₂", "酶催化"],
    camPos: { x: 15, y: 12, z: 15 },
    target: { x: 5, y: 0, z: 0 },
    scene: 'micro'
  },
  {
    title: "生命燃料：葡萄糖 (Glucose)",
    description: "最终的奇迹诞生了——葡萄糖(C₆H₁₂O₆)。这种富含能量的糖分将被输送到植物全身，甚至成为整个食物链的能量基石。",
    tags: ["葡萄糖", "能量储存", "循环终点"],
    camPos: { x: 0, y: 25, z: 35 },
    target: { x: 0, y: 0, z: 0 },
    scene: 'micro'
  }
];

// --- Three.js Variables ---
let scene: THREE.Scene;
let camera: THREE.PerspectiveCamera;
let renderer: THREE.WebGLRenderer;
let labelRenderer: CSS2DRenderer;
let controls: OrbitControls;
let clock: THREE.Clock;

// Scene Groups
let macroGroup: THREE.Group;
let microGroup: THREE.Group;

// Macro Objects
let macroPlant: THREE.Group;
let macroHuman: THREE.Group;
let macroSunLight: THREE.SpotLight;
let macroAmbient: THREE.AmbientLight;
let macroGasSystem: THREE.Group;

// Micro Objects
let chloroplast: THREE.Group;
let thylakoids: THREE.Group[] = [];
let microSunLight: THREE.DirectionalLight;

// Label Objects
const labels: Record<string, CSS2DObject> = {};

// Particles Systems (Micro)
let photonSystem: THREE.Group;
let waterSystem: THREE.Group;
let oxygenSystem: THREE.Group;
let co2System: THREE.Group;
let sugarSystem: THREE.Group;

// Animation Refs
let frameId: number;

// --- Initialization ---
onMounted(() => {
  initThree();
  createMacroScene();
  createMicroScene(); 
  animate();
  updateStep(0);
  window.addEventListener('resize', onWindowResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', onWindowResize);
  cancelAnimationFrame(frameId);
  if (renderer) renderer.dispose();
  if (labelRenderer?.domElement?.parentNode) {
    labelRenderer.domElement.parentNode.removeChild(labelRenderer.domElement);
  }
});

function initThree() {
  if (!canvasContainer.value) return;

  scene = new THREE.Scene();
  scene.fog = new THREE.FogExp2(0x111111, 0.02); // Atmospheric fog

  camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 1000);
  camera.position.set(30, 20, 30);

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.2;
  canvasContainer.value.appendChild(renderer.domElement);

  labelRenderer = new CSS2DRenderer();
  labelRenderer.setSize(window.innerWidth, window.innerHeight);
  labelRenderer.domElement.style.position = 'absolute';
  labelRenderer.domElement.style.top = '0px';
  labelRenderer.domElement.style.pointerEvents = 'none';
  canvasContainer.value.appendChild(labelRenderer.domElement);

  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;
  controls.maxDistance = 80;
  controls.minDistance = 2;

  clock = new THREE.Clock();

  // Groups
  macroGroup = new THREE.Group();
  microGroup = new THREE.Group();
  microGroup.visible = false;
  scene.add(macroGroup);
  scene.add(microGroup);
}

function createLabel(text: string, className: string = 'label-default'): CSS2DObject {
  const div = document.createElement('div');
  div.className = `label-3d ${className}`;
  div.textContent = text;
  const label = new CSS2DObject(div);
  return label;
}

// --- Macro Scene Construction (Redesigned) ---
function createMacroScene() {
  // 1. Room Environment
  // Floor
  const floorGeo = new THREE.PlaneGeometry(60, 60);
  const floorMat = new THREE.MeshStandardMaterial({ 
    color: 0x3e2723, // Dark wood
    roughness: 0.6,
    metalness: 0.1
  });
  const floor = new THREE.Mesh(floorGeo, floorMat);
  floor.rotation.x = -Math.PI / 2;
  floor.receiveShadow = true;
  macroGroup.add(floor);

  // Back Wall (with Window cutout implied by light)
  const wallGeo = new THREE.BoxGeometry(60, 40, 1);
  const wallMat = new THREE.MeshStandardMaterial({ color: 0xf5f5f5, roughness: 0.9 });
  const wall = new THREE.Mesh(wallGeo, wallMat);
  wall.position.set(0, 20, -20);
  wall.receiveShadow = true;
  macroGroup.add(wall);

  // Rug
  const rugGeo = new THREE.CircleGeometry(12, 32);
  const rugMat = new THREE.MeshStandardMaterial({ color: 0xd7ccc8, roughness: 1.0 });
  const rug = new THREE.Mesh(rugGeo, rugMat);
  rug.rotation.x = -Math.PI / 2;
  rug.position.y = 0.05;
  rug.receiveShadow = true;
  macroGroup.add(rug);

  // 2. Large Potted Plant (Procedural Monstera-ish)
  macroPlant = new THREE.Group();
  macroPlant.position.set(-8, 0, -5);
  
  // Pot
  const potGeo = new THREE.CylinderGeometry(3, 2.5, 5, 32);
  const potMat = new THREE.MeshStandardMaterial({ color: 0x5d4037 });
  const pot = new THREE.Mesh(potGeo, potMat);
  pot.position.y = 2.5;
  pot.castShadow = true;
  pot.receiveShadow = true;
  macroPlant.add(pot);

  // Soil
  const soilGeo = new THREE.CircleGeometry(2.8, 32);
  const soilMat = new THREE.MeshStandardMaterial({ color: 0x212121 });
  const soil = new THREE.Mesh(soilGeo, soilMat);
  soil.rotation.x = -Math.PI / 2;
  soil.position.y = 4.8;
  macroPlant.add(soil);

  // Stems & Leaves
  const stemMat = new THREE.MeshStandardMaterial({ color: 0x2e7d32 });
  const leafMat = new THREE.MeshStandardMaterial({ color: 0x4caf50, side: THREE.DoubleSide, roughness: 0.4, metalness: 0.1 });
  
  // Create a few branches
  const createBranch = (angle: number, height: number, lean: number) => {
    const branchGroup = new THREE.Group();
    // Stem
    const stemGeo = new THREE.CylinderGeometry(0.1, 0.3, height, 8);
    stemGeo.translate(0, height/2, 0);
    const stem = new THREE.Mesh(stemGeo, stemMat);
    // Bend the stem
    stem.rotation.z = lean;
    stem.castShadow = true;
    branchGroup.add(stem);

    // Leaf (Large Heart Shape approximate)
    const leafShape = new THREE.Shape();
    leafShape.moveTo(0, 0);
    leafShape.bezierCurveTo(2, 2, 4, 0, 0, -5);
    leafShape.bezierCurveTo(-4, 0, -2, 2, 0, 0);
    const leafGeo = new THREE.ShapeGeometry(leafShape);
    leafGeo.scale(0.8, 0.8, 0.8);
    
    const leaf = new THREE.Mesh(leafGeo, leafMat);
    leaf.position.set(Math.sin(lean)*height, Math.cos(lean)*height, 0);
    leaf.rotation.x = -Math.PI / 3;
    leaf.rotation.z = Math.PI; // Tip down
    leaf.castShadow = true;
    leaf.receiveShadow = true;
    branchGroup.add(leaf);

    branchGroup.rotation.y = angle;
    return branchGroup;
  };

  // Add multiple branches
  for (let i = 0; i < 7; i++) {
    const angle = (i / 7) * Math.PI * 2 + (Math.random()*0.5);
    const height = 10 + Math.random() * 8;
    const lean = 0.2 + Math.random() * 0.4;
    const branch = createBranch(angle, height, lean);
    branch.position.y = 3; // Start from soil
    macroPlant.add(branch);
  }

  macroGroup.add(macroPlant);

  // Label: Plant
  const plantLabel = createLabel('植物 (Plant)', 'label-title');
  plantLabel.position.set(0, 18, 0);
  macroPlant.add(plantLabel);
  labels['macro_plant'] = plantLabel;

  // 3. Human (Low Poly Style)
  macroHuman = new THREE.Group();
  macroHuman.position.set(8, 0, 2);
  macroHuman.rotation.y = -Math.PI / 4; // Facing plant roughly

  // Chair
  const chairGroup = new THREE.Group();
  const seatGeo = new THREE.BoxGeometry(6, 1, 6);
  const chairMat = new THREE.MeshStandardMaterial({ color: 0xffab91 });
  const seat = new THREE.Mesh(seatGeo, chairMat);
  seat.position.y = 4;
  seat.castShadow = true;
  chairGroup.add(seat);
  
  const backGeo = new THREE.BoxGeometry(6, 8, 1);
  const back = new THREE.Mesh(backGeo, chairMat);
  back.position.set(0, 8, 2.5);
  back.rotation.x = -0.2;
  back.castShadow = true;
  chairGroup.add(back);

  // Legs
  const legGeo = new THREE.CylinderGeometry(0.3, 0.2, 4);
  const legMat = new THREE.MeshStandardMaterial({ color: 0x8d6e63 });
  [[-2.5, -2.5], [2.5, -2.5], [-2.5, 2.5], [2.5, 2.5]].forEach(pos => {
    const l = new THREE.Mesh(legGeo, legMat);
    l.position.set(pos[0], 2, pos[1]);
    chairGroup.add(l);
  });
  macroHuman.add(chairGroup);

  // Human Body
  const skinMat = new THREE.MeshStandardMaterial({ color: 0xffccbc });
  const shirtMat = new THREE.MeshStandardMaterial({ color: 0x90caf9 });
  const pantsMat = new THREE.MeshStandardMaterial({ color: 0x5c6bc0 });

  // Torso
  const torsoGeo = new THREE.BoxGeometry(3.5, 5, 2);
  const torso = new THREE.Mesh(torsoGeo, shirtMat);
  torso.position.y = 7;
  torso.position.z = 0.5;
  torso.castShadow = true;
  // Add breathing animation target
  torso.userData = { isTorso: true };
  macroHuman.add(torso);

  // Head
  const headGeo = new THREE.SphereGeometry(1.5, 16, 16);
  const head = new THREE.Mesh(headGeo, skinMat);
  head.position.set(0, 10.5, 0.5);
  head.castShadow = true;
  macroHuman.add(head);

  // Legs (Sitting)
  const upperLegGeo = new THREE.BoxGeometry(1.4, 4, 1.4);
  const lowerLegGeo = new THREE.BoxGeometry(1.2, 4, 1.2);
  
  // Right Leg
  const rUpper = new THREE.Mesh(upperLegGeo, pantsMat);
  rUpper.position.set(1, 4.5, 2.5);
  rUpper.rotation.x = -Math.PI / 2; // Forward
  macroHuman.add(rUpper);
  const rLower = new THREE.Mesh(lowerLegGeo, pantsMat);
  rLower.position.set(1, 2.5, 4.2); // Down
  macroHuman.add(rLower);

  // Left Leg
  const lUpper = new THREE.Mesh(upperLegGeo, pantsMat);
  lUpper.position.set(-1, 4.5, 2.5);
  lUpper.rotation.x = -Math.PI / 2;
  macroHuman.add(lUpper);
  const lLower = new THREE.Mesh(lowerLegGeo, pantsMat);
  lLower.position.set(-1, 2.5, 4.2);
  macroHuman.add(lLower);

  // Arms (Reading book pose)
  const armGeo = new THREE.BoxGeometry(1, 3.5, 1);
  // Right Arm
  const rArm = new THREE.Mesh(armGeo, shirtMat);
  rArm.position.set(2, 8, 0.5);
  rArm.rotation.z = -0.2;
  rArm.rotation.x = -0.5;
  macroHuman.add(rArm);
  // Left Arm
  const lArm = new THREE.Mesh(armGeo, shirtMat);
  lArm.position.set(-2, 8, 0.5);
  lArm.rotation.z = 0.2;
  lArm.rotation.x = -0.5;
  macroHuman.add(lArm);

  // Book
  const bookGeo = new THREE.BoxGeometry(3, 0.5, 2.5);
  const bookMat = new THREE.MeshStandardMaterial({ color: 0xffffff });
  const book = new THREE.Mesh(bookGeo, bookMat);
  book.position.set(0, 6.5, 3);
  book.rotation.x = 0.5;
  macroHuman.add(book);

  macroGroup.add(macroHuman);

  // Label: Human
  const humanLabel = createLabel('人类 (Human)', 'label-info');
  humanLabel.position.set(0, 13, 0);
  macroHuman.add(humanLabel);
  labels['macro_human'] = humanLabel;

  // 4. Lighting (Enhanced)
  // Ambient
  macroAmbient = new THREE.AmbientLight(0xffffff, 0.4);
  macroGroup.add(macroAmbient);

  // Sun SpotLight (Simulating Window)
  macroSunLight = new THREE.SpotLight(0xfff176, 4);
  macroSunLight.position.set(-30, 40, 20); // High left
  macroSunLight.angle = Math.PI / 6;
  macroSunLight.penumbra = 0.3;
  macroSunLight.decay = 1; // Physical decay
  macroSunLight.distance = 100;
  macroSunLight.castShadow = true;
  macroSunLight.shadow.mapSize.width = 2048;
  macroSunLight.shadow.mapSize.height = 2048;
  macroSunLight.shadow.bias = -0.0001;
  macroGroup.add(macroSunLight);

  // Fake God Rays (Cone)
  const rayGeo = new THREE.ConeGeometry(15, 60, 32, 1, true);
  rayGeo.translate(0, -30, 0);
  rayGeo.rotateX(-Math.PI/2);
  const rayMat = new THREE.MeshBasicMaterial({ 
    color: 0xfff59d, 
    transparent: true, 
    opacity: 0.05, 
    side: THREE.DoubleSide,
    depthWrite: false,
    blending: THREE.AdditiveBlending
  });
  const ray = new THREE.Mesh(rayGeo, rayMat);
  ray.position.copy(macroSunLight.position);
  ray.lookAt(-5, 0, -5); // Point at plant
  macroGroup.add(ray);

  // Sun Label
  const sunLabel = createLabel('太阳能 (Solar Energy)', 'label-energy');
  sunLabel.position.set(0, 0, -10); // Relative to ray?
  // Put it in world space near window
  const sunLabelObj = new THREE.Object3D();
  sunLabelObj.position.set(-20, 30, 0);
  sunLabelObj.add(sunLabel);
  macroGroup.add(sunLabelObj);
  labels['macro_sun'] = sunLabel;


  // 5. Gas Exchange System (Refined)
  macroGasSystem = new THREE.Group();
  macroGroup.add(macroGasSystem);
  
  const o2Geo = new THREE.SphereGeometry(0.2, 8, 8);
  const co2Geo = new THREE.SphereGeometry(0.2, 8, 8);
  
  // Glowing materials
  const o2Mat = new THREE.MeshBasicMaterial({ color: 0x00ffff }); // Cyan glow
  const co2Mat = new THREE.MeshBasicMaterial({ color: 0xaaaaaa }); // Grey glow

  // Create many particles
  for(let i=0; i<40; i++) {
    // O2: Plant -> Human
    const o2 = new THREE.Mesh(o2Geo, o2Mat);
    o2.userData = { type: 'o2', offset: Math.random() * 100, speed: 0.5 + Math.random()*0.5 };
    macroGasSystem.add(o2);

    // CO2: Human -> Plant
    const co2 = new THREE.Mesh(co2Geo, co2Mat);
    co2.userData = { type: 'co2', offset: Math.random() * 100, speed: 0.5 + Math.random()*0.5 };
    macroGasSystem.add(co2);
  }
}

// --- Micro Scene Construction (Existing Logic - Preserved) ---
function createMicroScene() {
  chloroplast = new THREE.Group();
  microGroup.add(chloroplast);

  // 1. Chloroplast Shell
  const shellGeo = new THREE.CapsuleGeometry(12, 24, 4, 16);
  shellGeo.rotateZ(Math.PI / 2);
  const shellMat = new THREE.MeshPhysicalMaterial({
    color: 0x5fa05f,
    roughness: 0.1,
    metalness: 0.1,
    transmission: 0.7,
    thickness: 1.5,
    side: THREE.DoubleSide,
    transparent: true,
    opacity: 0.3
  });
  const shell = new THREE.Mesh(shellGeo, shellMat);
  chloroplast.add(shell);

  // Label: Chloroplast
  const chloroLabel = createLabel('叶绿体 (Chloroplast)', 'label-title');
  chloroLabel.position.set(0, 14, 0);
  chloroplast.add(chloroLabel);
  labels['chloroplast'] = chloroLabel;

  // Label: Stroma
  const stromaLabel = createLabel('基质 (Stroma)', 'label-info');
  stromaLabel.position.set(8, 5, 0);
  chloroplast.add(stromaLabel);
  labels['stroma'] = stromaLabel;

  // 2. Thylakoids
  const coinGeo = new THREE.CylinderGeometry(2.2, 2.2, 0.4, 32);
  const coinMat = new THREE.MeshStandardMaterial({ color: 0x1b5e20, roughness: 0.3, metalness: 0.1 });
  
  const createStack = (x: number, z: number, height: number, label?: boolean) => {
    const stackGroup = new THREE.Group();
    for (let i = 0; i < height; i++) {
      const coin = new THREE.Mesh(coinGeo, coinMat);
      coin.position.y = i * 0.5 - (height * 0.5) / 2;
      coin.castShadow = true;
      coin.receiveShadow = true;
      stackGroup.add(coin);
    }
    stackGroup.position.set(x, 0, z);
    chloroplast.add(stackGroup);
    thylakoids.push(stackGroup);

    if (label) {
      const l = createLabel('类囊体 (Thylakoid)', 'label-sub');
      l.position.set(0, height * 0.5 + 1, 0);
      stackGroup.add(l);
      labels['thylakoid'] = l;
    }
  };

  createStack(-5, -3, 6, true);
  createStack(5, -3, 5);
  createStack(0, 4, 8);
  createStack(-6, 4, 4);
  createStack(6, 4, 6);

  // 3. Stroma Particles
  const stromaGeo = new THREE.BufferGeometry();
  const stromaCount = 150;
  const stromaPos = new Float32Array(stromaCount * 3);
  for(let i=0; i<stromaCount * 3; i++) {
    stromaPos[i] = (Math.random() - 0.5) * 20;
  }
  stromaGeo.setAttribute('position', new THREE.BufferAttribute(stromaPos, 3));
  const stromaMat = new THREE.PointsMaterial({ color: 0xa5d6a7, size: 0.15, transparent: true, opacity: 0.4 });
  const stromaParticles = new THREE.Points(stromaGeo, stromaMat);
  chloroplast.add(stromaParticles);

  // 4. Initialize Dynamic Particle Groups
  initParticles();

  // Micro Light
  microSunLight = new THREE.DirectionalLight(0xffaa00, 2.5);
  microSunLight.position.set(20, 50, 20);
  microSunLight.castShadow = true;
  microGroup.add(microSunLight);
  
  const rimLight = new THREE.SpotLight(0x44ffaa, 5);
  rimLight.position.set(-20, 0, -20);
  rimLight.lookAt(0, 0, 0);
  microGroup.add(rimLight);
}

function initParticles() {
  // Photons
  photonSystem = new THREE.Group();
  const photonGeo = new THREE.SphereGeometry(0.3, 8, 8);
  const photonMat = new THREE.MeshBasicMaterial({ color: 0xffeb3b }); 
  for(let i=0; i<20; i++) {
    const p = new THREE.Mesh(photonGeo, photonMat);
    p.visible = false;
    photonSystem.add(p);
  }
  microGroup.add(photonSystem);
  const pLabel = createLabel('光子 (Light)', 'label-energy');
  pLabel.visible = false;
  photonSystem.add(pLabel);
  labels['photon'] = pLabel;

  // Water
  waterSystem = new THREE.Group();
  const waterGeo = new THREE.SphereGeometry(0.4, 16, 16);
  const waterMat = new THREE.MeshStandardMaterial({ color: 0x29b6f6, metalness: 0.3, roughness: 0.1 });
  for(let i=0; i<12; i++) {
    const w = new THREE.Mesh(waterGeo, waterMat);
    w.visible = false;
    waterSystem.add(w);
  }
  microGroup.add(waterSystem);
  const wLabel = createLabel('水 (H₂O)', 'label-matter');
  waterSystem.add(wLabel);
  labels['water'] = wLabel;

  // Oxygen
  oxygenSystem = new THREE.Group();
  const o2Geo = new THREE.SphereGeometry(0.4, 16, 16);
  const o2Mat = new THREE.MeshPhysicalMaterial({ color: 0xffffff, transmission: 0.9, opacity: 0.6, transparent: true });
  for(let i=0; i<12; i++) {
    const o = new THREE.Mesh(o2Geo, o2Mat);
    o.visible = false;
    oxygenSystem.add(o);
  }
  microGroup.add(oxygenSystem);
  const oLabel = createLabel('氧气 (O₂)', 'label-matter');
  oxygenSystem.add(oLabel);
  labels['oxygen'] = oLabel;

  // CO2
  co2System = new THREE.Group();
  const co2Geo = new THREE.SphereGeometry(0.35, 16, 16);
  const co2Mat = new THREE.MeshStandardMaterial({ color: 0x90a4ae });
  for(let i=0; i<15; i++) {
    const c = new THREE.Mesh(co2Geo, co2Mat);
    c.visible = false;
    co2System.add(c);
  }
  microGroup.add(co2System);
  const cLabel = createLabel('二氧化碳 (CO₂)', 'label-matter');
  co2System.add(cLabel);
  labels['co2'] = cLabel;

  // Glucose
  sugarSystem = new THREE.Group();
  const sugarGeo = new THREE.CylinderGeometry(0.6, 0.6, 0.3, 6);
  const sugarMat = new THREE.MeshStandardMaterial({ color: 0xffffff, emissive: 0xffffff, emissiveIntensity: 0.3 });
  for(let i=0; i<8; i++) {
    const s = new THREE.Mesh(sugarGeo, sugarMat);
    s.rotation.x = Math.PI / 2;
    s.visible = false;
    sugarSystem.add(s);
  }
  microGroup.add(sugarSystem);
  const sLabel = createLabel('葡萄糖 (Sugar)', 'label-product');
  sugarSystem.add(sLabel);
  labels['sugar'] = sLabel;
}

// --- Animation Loop ---
function animate() {
  frameId = requestAnimationFrame(animate);
  const time = clock.getElapsedTime();
  
  controls.update();
  
  // Macro Animations
  if (macroGroup.visible) {
    // 1. Plant Sway
    if (macroPlant) {
      macroPlant.children.forEach((child, i) => {
        if (child.isGroup) { // Branch
           // Slight wind effect
           child.rotation.z += Math.sin(time * 1 + i) * 0.002; 
        }
      });
    }
    
    // 2. Human Breathing
    if (macroHuman) {
      const torso = macroHuman.children.find(c => c.userData.isTorso);
      if (torso) {
        const breath = Math.sin(time * 2);
        torso.scale.set(1 + breath * 0.05, 1, 1 + breath * 0.05);
      }
    }
    
    // 3. Gas Exchange Animation (Bezier-like paths)
    macroGasSystem.children.forEach((p: any) => {
      const t = (time * p.userData.speed + p.userData.offset) % 10;
      const progress = t / 10;
      
      // Plant approx pos: (-5, 10, -5)
      // Human approx pos: (8, 10, 2)
      const plantPos = new THREE.Vector3(-5, 10 + Math.sin(time)*2, -5);
      const humanPos = new THREE.Vector3(8, 10, 2);
      
      if (p.userData.type === 'o2') {
        // Plant -> Human
        // Parabolic arc
        const mid = new THREE.Vector3().addVectors(plantPos, humanPos).multiplyScalar(0.5);
        mid.y += 5; // Arch up
        
        // Quadratic Bezier
        const p1 = plantPos;
        const p2 = mid;
        const p3 = humanPos;
        
        p.position.x = (1-progress)*(1-progress)*p1.x + 2*(1-progress)*progress*p2.x + progress*progress*p3.x;
        p.position.y = (1-progress)*(1-progress)*p1.y + 2*(1-progress)*progress*p2.y + progress*progress*p3.y;
        p.position.z = (1-progress)*(1-progress)*p1.z + 2*(1-progress)*progress*p2.z + progress*progress*p3.z;
        
        // Fade out near target
        if (progress > 0.9) p.visible = false;
        else p.visible = true;

      } else {
        // Human -> Plant
        const mid = new THREE.Vector3().addVectors(humanPos, plantPos).multiplyScalar(0.5);
        mid.y += 8; // Higher arch for CO2?
        
        const p1 = humanPos;
        const p2 = mid;
        const p3 = plantPos;
        
        p.position.x = (1-progress)*(1-progress)*p1.x + 2*(1-progress)*progress*p2.x + progress*progress*p3.x;
        p.position.y = (1-progress)*(1-progress)*p1.y + 2*(1-progress)*progress*p2.y + progress*progress*p3.y;
        p.position.z = (1-progress)*(1-progress)*p1.z + 2*(1-progress)*progress*p2.z + progress*progress*p3.z;
        
        if (progress > 0.9) p.visible = false;
        else p.visible = true;
      }
    });
  }

  // Micro Animations
  if (microGroup.visible && chloroplast) {
    chloroplast.rotation.y = Math.sin(time * 0.05) * 0.05;
    chloroplast.position.y = Math.sin(time * 0.2) * 0.5;
  }

  renderer.render(scene, camera);
  labelRenderer.render(scene, camera);
}

function onWindowResize() {
  if (!camera || !renderer || !labelRenderer) return;
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
  labelRenderer.setSize(window.innerWidth, window.innerHeight);
}

// --- Step Logic ---
function nextStep() {
  if (currentStepIndex.value < steps.length - 1) {
    currentStepIndex.value++;
  } else {
    currentStepIndex.value = 0;
  }
}

function prevStep() {
  if (currentStepIndex.value > 0) {
    currentStepIndex.value--;
  }
}

watch(currentStepIndex, updateStep);

function updateStep(index: number) {
  const step = steps[index];
  
  // Scene Visibility Switching
  if (step.scene === 'macro') {
    macroGroup.visible = true;
    microGroup.visible = false;
    labels['macro_plant'].visible = true;
    labels['macro_human'].visible = true;
    labels['macro_sun'].visible = true;
  } else if (step.scene === 'micro') {
    macroGroup.visible = false;
    microGroup.visible = true;
  } else if (step.scene === 'transition') {
    macroGroup.visible = true;
    microGroup.visible = false;
    gsap.to(macroGroup.position, { z: 30, opacity: 0, duration: 2 });
    setTimeout(() => {
        macroGroup.visible = false;
        microGroup.visible = true;
        macroGroup.position.z = 0; 
    }, 1500);
  }

  // Camera Move
  gsap.to(camera.position, {
    x: step.camPos.x,
    y: step.camPos.y,
    z: step.camPos.z,
    duration: 1.5,
    ease: "power2.inOut"
  });
  gsap.to(controls.target, {
    x: step.target.x,
    y: step.target.y,
    z: step.target.z,
    duration: 1.5,
    ease: "power2.inOut"
  });

  // Reset Micro Logic
  resetAll();
  
  switch(index) {
    case 3: // Overview
      labels['chloroplast'].visible = true;
      labels['thylakoid'].visible = true;
      labels['stroma'].visible = true;
      break;
    case 4: // Light
      animateLightReaction();
      break;
    case 5: // Water Split
      animateWaterSplit();
      break;
    case 6: // Calvin Cycle
      animateCalvinCycle();
      break;
    case 7: // Sugar
      animateSugar();
      break;
  }
}

function resetAll() {
  // Hide micro labels
  const microLabelKeys = ['chloroplast', 'thylakoid', 'stroma', 'photon', 'water', 'oxygen', 'co2', 'sugar'];
  microLabelKeys.forEach(k => {
    if (labels[k]) labels[k].visible = false;
  });
  
  // Stop GSAP tweens on particles
  [photonSystem, waterSystem, oxygenSystem, co2System, sugarSystem].forEach(sys => {
    if (!sys) return;
    gsap.killTweensOf(sys.children);
    sys.children.forEach((c: any) => {
      if (c.isMesh) {
        c.visible = false;
        c.scale.set(1,1,1);
      }
    });
  });
}

// --- Specific Animations ---
function animateLightReaction() {
  labels['photon'].visible = true;
  labels['photon'].position.set(5, 20, 5);
  const photons = photonSystem.children.filter((c: any) => c.isMesh);
  photons.forEach((p: any, i: number) => {
    p.position.set(5 + Math.random() * 10, 40 + Math.random() * 10, 5 + Math.random() * 10);
    p.visible = true;
    gsap.to(p.position, {
      x: (Math.random() - 0.5) * 5 - 5,
      y: 3 + Math.random() * 2,
      z: (Math.random() - 0.5) * 5,
      duration: 1.5,
      delay: i * 0.15,
      ease: "power2.in",
      repeat: -1,
      repeatDelay: 0.2
    });
  });
}

function animateWaterSplit() {
  labels['water'].visible = true;
  labels['water'].position.set(0, -10, 0);
  const water = waterSystem.children.filter((c: any) => c.isMesh);
  const oxygen = oxygenSystem.children.filter((c: any) => c.isMesh);
  water.forEach((w: any, i: number) => {
    w.position.set((Math.random()-0.5)*5, -20, (Math.random()-0.5)*5);
    w.visible = true;
    gsap.to(w.position, {
      y: 0,
      duration: 2,
      delay: i * 0.3,
      ease: "power1.out",
      onComplete: () => {
        w.visible = false; 
        const o = oxygen[i];
        if (o) {
          o.position.copy(w.position);
          o.visible = true;
          labels['oxygen'].visible = true;
          labels['oxygen'].position.copy(o.position).add(new THREE.Vector3(0,2,0));
          gsap.to(o.position, {
            y: 25,
            x: o.position.x + (Math.random()-0.5)*10,
            z: o.position.z + (Math.random()-0.5)*10,
            duration: 3,
            ease: "power1.in",
            onComplete: () => { o.visible = false; }
          });
        }
      }
    });
  });
}

function animateCalvinCycle() {
  labels['co2'].visible = true;
  labels['co2'].position.set(10, 10, 0);
  const co2 = co2System.children.filter((c: any) => c.isMesh);
  co2.forEach((c: any, i: number) => {
    c.position.set(20, 15, (Math.random()-0.5)*10); 
    c.visible = true;
    const tl = gsap.timeline({ repeat: -1, repeatDelay: 0.2 });
    tl.to(c.position, {
      x: Math.cos(i) * 4 + 5,
      z: Math.sin(i) * 4,
      y: 0,
      duration: 2.5,
      delay: i * 0.2,
      ease: "power2.out"
    })
    .to(c.scale, {
      x: 0, y: 0, z: 0,
      duration: 0.5,
      ease: "back.in(2)"
    });
  });
}

function animateSugar() {
  labels['sugar'].visible = true;
  labels['sugar'].position.set(0, -5, 0);
  const sugar = sugarSystem.children.filter((c: any) => c.isMesh);
  sugar.forEach((s: any, i: number) => {
    s.position.set(0,0,0);
    s.visible = false;
    s.scale.set(0,0,0);
    const startX = (Math.random()-0.5)*8 + 5;
    const startZ = (Math.random()-0.5)*8;
    const tl = gsap.timeline({ delay: i * 0.5, repeat: -1, repeatDelay: 2 });
    tl.to(s.position, { x: startX, y: 0, z: startZ, duration: 0 }) 
      .set(s, { visible: true })
      .to(s.scale, { x: 1, y: 1, z: 1, duration: 0.8, ease: "elastic.out(1, 0.5)" })
      .to(s.position, {
        y: -25, 
        duration: 2,
        ease: "power2.in"
      }, "+=0.5")
      .set(s, { visible: false });
  });
}
</script>

<style>
/* Global Label Styles */
.label-3d {
  font-family: 'Inter', system-ui, sans-serif;
  padding: 4px 12px;
  border-radius: 999px;
  font-weight: 600;
  font-size: 14px;
  pointer-events: none;
  text-shadow: 0 2px 4px rgba(0,0,0,0.5);
  white-space: nowrap;
  transition: opacity 0.3s;
}

.label-title {
  background: rgba(34, 197, 94, 0.2);
  border: 1px solid rgba(34, 197, 94, 0.5);
  color: #86efac;
  font-size: 16px;
  backdrop-filter: blur(4px);
}

.label-info {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #e2e8f0;
}

.label-sub {
  color: #a7f3d0;
  background: rgba(0, 0, 0, 0.4);
  font-size: 12px;
}

.label-energy {
  background: rgba(234, 179, 8, 0.2);
  border: 1px solid rgba(234, 179, 8, 0.6);
  color: #fde047;
  box-shadow: 0 0 15px rgba(234, 179, 8, 0.3);
}

.label-matter {
  background: rgba(56, 189, 248, 0.2);
  border: 1px solid rgba(56, 189, 248, 0.5);
  color: #7dd3fc;
}

.label-product {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid #fff;
  color: #fff;
  font-size: 16px;
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
