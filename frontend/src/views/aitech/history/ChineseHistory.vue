<template>
  <div class="history-container relative w-full h-screen overflow-hidden bg-black">
    <!-- 3D Canvas -->
    <div ref="canvasContainer" class="absolute inset-0 z-0"></div>
    
    <!-- Loading Overlay -->
    <div v-if="loading" class="absolute inset-0 z-50 flex flex-col items-center justify-center bg-black text-gold-500">
      <div class="text-4xl mb-4 font-serif">中华上下五千年</div>
      <div class="loading-bar w-64 h-1 bg-gray-800 rounded overflow-hidden">
        <div class="h-full bg-yellow-600 transition-all duration-300" :style="{ width: loadProgress + '%' }"></div>
      </div>
      <div class="mt-2 text-sm text-gray-400">正在构建历史长河...</div>
    </div>

    <!-- UI Overlay -->
    <div class="absolute top-0 left-0 w-full p-4 z-10 flex justify-between items-start pointer-events-none">
      <div class="pointer-events-auto">
        <h1 class="text-3xl md:text-5xl font-bold text-transparent bg-clip-text bg-gradient-to-b from-yellow-300 to-yellow-600 font-serif tracking-widest">
          历史长河
        </h1>
        <p class="text-gray-400 text-xs md:text-sm mt-1 max-w-[200px]">
          滑动或拖拽穿越时空
        </p>
      </div>
      
      <button @click="$router.back()" class="pointer-events-auto px-4 py-2 bg-white/10 backdrop-blur-md rounded-full text-white hover:bg-white/20 transition">
        返回
      </button>
    </div>

    <!-- Detail Panel (Side Panel) -->
    <Transition name="slide-right">
      <div v-if="selectedEra" class="absolute top-0 right-0 bottom-0 z-20 w-full md:w-[400px] bg-gray-900/95 border-l border-yellow-900/50 backdrop-blur-md shadow-2xl flex flex-col pointer-events-auto">
        <button @click="closeDetail" class="absolute top-4 right-4 text-gray-400 hover:text-white p-2 z-30">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
        
        <div class="p-6 md:p-8 overflow-y-auto flex-1">
          <div class="text-yellow-500 text-sm font-bold mb-2 tracking-widest">{{ selectedEra.period }}</div>
          <h2 class="text-3xl font-serif font-bold text-white mb-6 border-b border-yellow-800/30 pb-4">
            {{ selectedEra.name }}
          </h2>
          
          <div class="space-y-6">
            <div class="bg-black/40 rounded-lg p-4 border border-white/5">
               <div class="w-full h-32 bg-gradient-to-br from-gray-800 to-gray-900 flex items-center justify-center text-gray-600 text-xs mb-2">
                  历史影像
               </div>
               <p class="text-gray-400 text-sm italic leading-relaxed">
                 {{ selectedEra.description }}
               </p>
            </div>

            <div>
              <h3 class="text-yellow-600 font-bold text-sm mb-2 flex items-center gap-2">
                <span class="w-1 h-4 bg-yellow-600 rounded-full"></span> 主要成就
              </h3>
              <p class="text-gray-300 leading-relaxed text-sm pl-3 border-l border-gray-700">{{ selectedEra.achievements }}</p>
            </div>
            
            <div>
              <h3 class="text-yellow-600 font-bold text-sm mb-2 flex items-center gap-2">
                <span class="w-1 h-4 bg-yellow-600 rounded-full"></span> 著名人物
              </h3>
              <p class="text-gray-300 leading-relaxed text-sm pl-3 border-l border-gray-700">{{ selectedEra.figures }}</p>
            </div>
          </div>
        </div>
        
        <div class="p-4 border-t border-white/5 bg-black/20">
          <div class="text-xs text-gray-500 mb-2 text-center">
            <span class="animate-pulse">👆</span> 拖拽屏幕可 360° 观察节点
          </div>
          <div class="flex gap-3">
             <button @click="prevEra" class="flex-1 py-2 bg-white/5 hover:bg-white/10 text-white rounded transition text-sm border border-white/10">
              上一个
            </button>
            <button @click="nextEra" class="flex-1 py-2 bg-yellow-700 hover:bg-yellow-600 text-white rounded transition text-sm">
              下一个
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Navigation Controls (Visible when no detail selected) -->
    <Transition name="fade">
      <div v-if="!selectedEra" class="absolute bottom-20 left-0 w-full px-4 flex justify-between pointer-events-none z-10">
        <button @click="prevEra" class="pointer-events-auto w-12 h-12 rounded-full bg-white/10 backdrop-blur-md flex items-center justify-center text-white hover:bg-white/20 transition border border-white/20">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <button @click="nextEra" class="pointer-events-auto w-12 h-12 rounded-full bg-white/10 backdrop-blur-md flex items-center justify-center text-white hover:bg-white/20 transition border border-white/20">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>
    </Transition>

    <!-- Navigation Slider (Optional, for quick jumping) -->
    <div class="absolute bottom-8 left-1/2 -translate-x-1/2 w-4/5 max-w-lg z-10 pointer-events-auto">
      <input 
        type="range" 
        min="0" 
        max="1" 
        step="0.001" 
        v-model="scrollProgress" 
        class="w-full h-1 bg-gray-700 rounded-lg appearance-none cursor-pointer accent-yellow-500"
        @input="onSliderInput"
      >
      <div class="flex justify-between text-xs text-gray-500 mt-2 font-mono">
        <span>远古</span>
        <span>现代</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import * as THREE from 'three';
import { CSS2DRenderer, CSS2DObject } from 'three/examples/jsm/renderers/CSS2DRenderer.js';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import gsap from 'gsap';

// --- Types & Data ---
interface Era {
  id: string;
  name: string;
  period: string;
  description: string;
  achievements: string;
  figures: string;
  color: string;
}

const historyData: Era[] = [
  {
    id: 'myth',
    name: '三皇五帝',
    period: '约前3000年 - 前2070年',
    description: '中华文明的起源，神话与传说的时代。',
    achievements: '钻木取火、结绳记事、农耕起源、陶器制作。',
    figures: '黄帝、炎帝、尧、舜、禹',
    color: '#eab308' // yellow-500
  },
  {
    id: 'xia',
    name: '夏朝',
    period: '约前2070年 - 前1600年',
    description: '中国历史上第一个世袭制朝代。',
    achievements: '青铜器铸造初具规模，历法《夏小正》。',
    figures: '禹、启、桀',
    color: '#ca8a04'
  },
  {
    id: 'shang',
    name: '商朝',
    period: '约前1600年 - 前1046年',
    description: '甲骨文的出现标志着信史的开始。',
    achievements: '甲骨文、成熟的青铜文明（司母戊鼎）。',
    figures: '商汤、盘庚、武丁、纣王',
    color: '#a16207'
  },
  {
    id: 'zhou',
    name: '周朝',
    period: '前1046年 - 前256年',
    description: '分封制确立，礼乐文明，诸子百家争鸣。',
    achievements: '礼乐制度、宗法制、铁器牛耕出现、百家争鸣。',
    figures: '周武王、周公旦、孔子、老子、孟子、墨子',
    color: '#854d0e'
  },
  {
    id: 'qin',
    name: '秦朝',
    period: '前221年 - 前207年',
    description: '中国历史上第一个大一统的中央集权帝国。',
    achievements: '统一文字、度量衡、车轨，修筑长城、灵渠。',
    figures: '秦始皇、李斯、蒙恬',
    color: '#888888' // Lighter gray for visibility
  },
  {
    id: 'han',
    name: '汉朝',
    period: '前202年 - 220年',
    description: '强盛的帝国，丝绸之路开通，独尊儒术。',
    achievements: '造纸术、丝绸之路、地动仪、史记。',
    figures: '汉高祖、汉武帝、张骞、司马迁、蔡伦',
    color: '#dc2626' // Red for Han
  },
  {
    id: 'threekingdoms',
    name: '三国',
    period: '220年 - 280年',
    description: '魏蜀吴三足鼎立，英雄辈出的时代。',
    achievements: '建安风骨，军事谋略发展。',
    figures: '曹操、刘备、孙权、诸葛亮、关羽',
    color: '#b91c1c'
  },
  {
    id: 'jin',
    name: '两晋南北朝',
    period: '265年 - 589年',
    description: '政权更迭频繁，民族大融合，佛教盛行。',
    achievements: '圆周率（祖冲之）、兰亭序（王羲之）、石窟艺术。',
    figures: '司马炎、王羲之、祖冲之、孝文帝',
    color: '#991b1b'
  },
  {
    id: 'sui',
    name: '隋朝',
    period: '581年 - 618年',
    description: '结束分裂，开创科举，开凿大运河。',
    achievements: '科举制、大运河、赵州桥。',
    figures: '隋文帝、隋炀帝',
    color: '#7f1d1d'
  },
  {
    id: 'tang',
    name: '唐朝',
    period: '618年 - 907年',
    description: '中国封建社会的鼎盛时期，万邦来朝。',
    achievements: '唐诗、雕版印刷、曲辕犁、对外开放。',
    figures: '李世民、武则天、李白、杜甫、玄奘',
    color: '#f59e0b' // Goldish for Tang
  },
  {
    id: 'song',
    name: '宋朝',
    period: '960年 - 1279年',
    description: '经济文化极度繁荣，科技发展高峰。',
    achievements: '活字印刷、指南针、火药应用、交子（纸币）、宋词。',
    figures: '赵匡胤、苏轼、王安石、岳飞、毕昇',
    color: '#16a34a' // Greenish (often associated with Song arts/ceramics)
  },
  {
    id: 'yuan',
    name: '元朝',
    period: '1271年 - 1368年',
    description: '疆域辽阔，行省制度，东西方交流。',
    achievements: '行省制、元曲、青花瓷成熟。',
    figures: '忽必烈、关汉卿、马可波罗',
    color: '#1e3a8a' // Blue (Mongol sky)
  },
  {
    id: 'ming',
    name: '明朝',
    period: '1368年 - 1644年',
    description: '君主专制加强，郑和下西洋，资本主义萌芽。',
    achievements: '本草纲目、天工开物、永乐大典、郑和下西洋。',
    figures: '朱元璋、朱棣、郑和、王阳明、李时珍',
    color: '#be123c'
  },
  {
    id: 'qing',
    name: '清朝',
    period: '1644年 - 1912年',
    description: '最后一个封建王朝，奠定现代版图。',
    achievements: '康乾盛世、红楼梦、京剧形成。',
    figures: '康熙、乾隆、曹雪芹、林则徐',
    color: '#4c1d95'
  },
  {
    id: 'roc',
    name: '中华民国',
    period: '1912年 - 1949年',
    description: '结束帝制，走向共和，抗日战争。',
    achievements: '新文化运动、五四运动、抗战胜利。',
    figures: '孙中山、鲁迅、李大钊',
    color: '#2563eb'
  },
  {
    id: 'prc',
    name: '中华人民共和国',
    period: '1949年 - 至今',
    description: '中国人民站起来了，改革开放，伟大复兴。',
    achievements: '两弹一星、杂交水稻、改革开放、载人航天、高铁。',
    figures: '毛泽东、邓小平、钱学森、袁隆平',
    color: '#ef4444' // Red
  }
];

// --- State ---
const canvasContainer = ref<HTMLElement | null>(null);
const loading = ref(true);
const loadProgress = ref(0);
const selectedEra = ref<Era | null>(null);
const scrollProgress = ref(0);
const lastVisitedEraId = ref<string | null>(null);

// --- Three.js Variables ---
let scene: THREE.Scene;
let camera: THREE.PerspectiveCamera;
let renderer: THREE.WebGLRenderer;
let labelRenderer: CSS2DRenderer;
let controls: OrbitControls;
let curve: THREE.CatmullRomCurve3;
let tubeGeometry: THREE.TubeGeometry;
let stars: THREE.Points;
let animationId: number;

// Interaction Mode
type Mode = 'path' | 'orbit';
let mode: Mode = 'path';

// Camera movement state
const cameraState = {
  progress: 0,
  targetProgress: 0,
  speed: 0.0005
};

// Interactive objects
const eraObjects: { mesh: THREE.Mesh; era: Era, t: number }[] = [];

// --- Setup Functions ---

const initThree = () => {
  if (!canvasContainer.value) return;

  // 1. Scene
  scene = new THREE.Scene();
  scene.fog = new THREE.FogExp2(0x000000, 0.002);

  // 2. Camera
  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000);
  
  // 3. Renderer
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  canvasContainer.value.appendChild(renderer.domElement);

  // 4. Label Renderer
  labelRenderer = new CSS2DRenderer();
  labelRenderer.setSize(window.innerWidth, window.innerHeight);
  labelRenderer.domElement.style.position = 'absolute';
  labelRenderer.domElement.style.top = '0px';
  labelRenderer.domElement.style.pointerEvents = 'none'; 
  canvasContainer.value.appendChild(labelRenderer.domElement);

  // 5. Controls (Orbit) - Initially Disabled
  controls = new OrbitControls(camera, renderer.domElement); // Attach to renderer (canvas) as labelRenderer is pointer-events: none
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;
  controls.enableZoom = true;
  controls.enabled = false; // Start in path mode
  controls.autoRotate = true;
  controls.autoRotateSpeed = 1.0;

  // 6. Lighting
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
  scene.add(ambientLight);
  
  const pointLight = new THREE.PointLight(0xffaa00, 1, 50);
  scene.add(pointLight);

  // 7. Create Path (The River of Time)
  createPath();
  
  // 8. Create Background (Stars)
  createStars();

  // 9. Event Listeners
  window.addEventListener('resize', onWindowResize);
  window.addEventListener('wheel', onWheel, { passive: false });
  window.addEventListener('touchstart', onTouchStart, { passive: false });
  window.addEventListener('touchmove', onTouchMove, { passive: false });
  window.addEventListener('click', onClick);

  // Start Animation
  animate();
  
  // Simulate loading
  let p = 0;
  const interval = setInterval(() => {
    p += 5;
    loadProgress.value = p;
    if (p >= 100) {
      clearInterval(interval);
      loading.value = false;
    }
  }, 50);
};

const createPath = () => {
  // Create a winding curve
  const points: THREE.Vector3[] = [];
  const count = historyData.length;
  const step = 10; 
  
  for (let i = 0; i < count; i++) {
    const x = (i - count / 2) * step;
    const y = Math.sin(i * 0.5) * 5;
    const z = Math.cos(i * 0.3) * 5;
    points.push(new THREE.Vector3(x, y, z));
  }
  
  // Extend the curve a bit at the ends
  const first = points[0];
  const last = points[points.length - 1];
  points.unshift(new THREE.Vector3(first.x - 10, first.y, first.z));
  points.push(new THREE.Vector3(last.x + 10, last.y, last.z));

  curve = new THREE.CatmullRomCurve3(points);
  
  // Visual Tube for the "River"
  tubeGeometry = new THREE.TubeGeometry(curve, 200, 1, 8, false);
  const tubeMaterial = new THREE.MeshBasicMaterial({ 
    color: 0x222222,
    wireframe: true,
    transparent: true,
    opacity: 0.3
  });
  const tube = new THREE.Mesh(tubeGeometry, tubeMaterial);
  scene.add(tube);

  // Place Nodes for each Era
  historyData.forEach((era, index) => {
    const t = 0.1 + (index / (historyData.length - 1)) * 0.8;
    const point = curve.getPoint(t);
    
    // Create a glowing orb for the era
    const geometry = new THREE.SphereGeometry(0.8, 32, 32);
    const material = new THREE.MeshStandardMaterial({
      color: era.color,
      emissive: era.color,
      emissiveIntensity: 0.5,
      roughness: 0.2,
      metalness: 0.8
    });
    const sphere = new THREE.Mesh(geometry, material);
    sphere.position.copy(point);
    sphere.userData = { isEra: true, eraData: era }; 
    scene.add(sphere);
    eraObjects.push({ mesh: sphere, era, t });

    // Add a 2D Label
    const div = document.createElement('div');
    div.className = 'era-label';
    div.style.color = era.color;
    div.style.textShadow = '0 0 5px black';
    div.style.fontFamily = 'serif';
    div.style.fontSize = '16px';
    div.style.fontWeight = 'bold';
    div.style.cursor = 'pointer';
    div.style.pointerEvents = 'auto'; 
    div.innerHTML = `
      <div class="text-center">
        <div class="text-lg">${era.name}</div>
        <div class="text-xs opacity-75 text-white">${era.period}</div>
      </div>
    `;
    
    div.addEventListener('click', (e) => {
        e.stopPropagation(); 
        selectEra(era);
    });

    const label = new CSS2DObject(div);
    label.position.set(0, 1.5, 0);
    sphere.add(label);
    
    // Add a decorative ring
    const ringGeo = new THREE.TorusGeometry(1.2, 0.05, 16, 100);
    const ringMat = new THREE.MeshBasicMaterial({ color: 0xffffff, transparent: true, opacity: 0.3 });
    const ring = new THREE.Mesh(ringGeo, ringMat);
    ring.rotation.x = Math.PI / 2;
    sphere.add(ring);
    
    gsap.to(ring.rotation, {
      z: Math.PI * 2,
      duration: 10 + Math.random() * 10,
      repeat: -1,
      ease: "none"
    });
  });
};

const createStars = () => {
  const geometry = new THREE.BufferGeometry();
  const vertices = [];
  
  for (let i = 0; i < 2000; i++) {
    vertices.push(THREE.MathUtils.randFloatSpread(200)); 
    vertices.push(THREE.MathUtils.randFloatSpread(200)); 
    vertices.push(THREE.MathUtils.randFloatSpread(200)); 
  }
  
  geometry.setAttribute('position', new THREE.Float32BufferAttribute(vertices, 3));
  
  const material = new THREE.PointsMaterial({ color: 0xffffff, size: 0.5, transparent: true, opacity: 0.8 });
  stars = new THREE.Points(geometry, material);
  scene.add(stars);
};

// --- Interaction ---
let touchStartY = 0;

const onWheel = (e: WheelEvent) => {
  if (mode === 'orbit') return; // Don't scroll path in orbit mode
  
  e.preventDefault(); 
  const delta = e.deltaY;
  const sensitivity = 0.0005;
  cameraState.targetProgress += delta * sensitivity;
  cameraState.targetProgress = Math.max(0, Math.min(1, cameraState.targetProgress));
};

const onTouchStart = (e: TouchEvent) => {
  if (mode === 'orbit') return;
  touchStartY = e.touches[0].clientY;
};

const onTouchMove = (e: TouchEvent) => {
  if (mode === 'orbit') return;
  const touchY = e.touches[0].clientY;
  const delta = touchStartY - touchY;
  touchStartY = touchY;
  
  const sensitivity = 0.002;
  cameraState.targetProgress += delta * sensitivity;
  cameraState.targetProgress = Math.max(0, Math.min(1, cameraState.targetProgress));
};

const onSliderInput = (e: Event) => {
  if (mode === 'orbit') {
      // Exit orbit mode if slider dragged?
      // For now, let's just allow it to update path but orbit might fight
      closeDetail();
  }
  const val = parseFloat((e.target as HTMLInputElement).value);
  cameraState.targetProgress = val;
};

const onClick = (e: MouseEvent) => {
    if (mode === 'orbit') return;

    // Raycasting for 3D objects
    const mouse = new THREE.Vector2();
    mouse.x = (e.clientX / window.innerWidth) * 2 - 1;
    mouse.y = -(e.clientY / window.innerHeight) * 2 + 1;

    const raycaster = new THREE.Raycaster();
    raycaster.setFromCamera(mouse, camera);

    const intersects = raycaster.intersectObjects(scene.children, true);

    for (const intersect of intersects) {
        let obj = intersect.object;
        while (obj) {
             if (obj.userData && obj.userData.isEra) {
                selectEra(obj.userData.eraData);
                return;
            }
            obj = obj.parent as THREE.Object3D;
        }
    }
};

const selectEra = (era: Era) => {
  // Prevent re-selecting same era if already there
  if (selectedEra.value?.id === era.id && mode === 'orbit') return;

  selectedEra.value = era;
  lastVisitedEraId.value = era.id;
  mode = 'orbit';

  const index = historyData.findIndex(e => e.id === era.id);
  const t = 0.1 + (index / (historyData.length - 1)) * 0.8;
  
  // Sync path progress to this node
  cameraState.targetProgress = t;
  cameraState.progress = t;

  // Setup Orbit Controls
  const sphereObj = eraObjects.find(obj => obj.era.id === era.id);
  if (sphereObj) {
      const spherePos = sphereObj.mesh.position.clone();
      
      // Calculate a nice viewing position: slightly offset
      // We can use the path tangent to find a "side" view
      const tangent = curve.getTangentAt(t);
      const offset = new THREE.Vector3(-tangent.z, 0, tangent.x).normalize().multiplyScalar(3);
      const viewPos = spherePos.clone().add(offset).add(new THREE.Vector3(0, 1, 0));

      // Tween Camera to View Position
      gsap.to(camera.position, {
          x: viewPos.x,
          y: viewPos.y,
          z: viewPos.z,
          duration: 1.5,
          ease: "power2.out",
          onUpdate: () => {
              camera.lookAt(spherePos);
          },
          onComplete: () => {
              controls.target.copy(spherePos);
              controls.enabled = true;
          }
      });
  }
};

const closeDetail = () => {
  selectedEra.value = null;
  mode = 'path';
  controls.enabled = false;
  
  // Nudge forward slightly so we don't immediately re-trigger
  // cameraState.targetProgress += 0.01;
};

const nextEra = () => {
  let currentIndex = -1;
  if (selectedEra.value) {
      currentIndex = historyData.findIndex(e => e.id === selectedEra.value?.id);
  } else {
       // Find closest
       let minDiff = Infinity;
       historyData.forEach((_, index) => {
        const t = 0.1 + (index / (historyData.length - 1)) * 0.8;
        const diff = Math.abs(t - cameraState.progress);
        if (diff < minDiff) {
          minDiff = diff;
          currentIndex = index;
        }
      });
  }

  if (currentIndex < historyData.length - 1) {
    closeDetail(); // Reset mode
    const nextEraData = historyData[currentIndex + 1];
    // We don't jump directly, we let the path animation handle it, 
    // but we can set target to slightly BEFORE the node to let auto-trigger handle it?
    // Or just call selectEra directly for smooth transition?
    // Let's call selectEra directly for "Next" button behavior
    selectEra(nextEraData);
  }
};

const prevEra = () => {
  let currentIndex = -1;
  if (selectedEra.value) {
      currentIndex = historyData.findIndex(e => e.id === selectedEra.value?.id);
  } else {
       let minDiff = Infinity;
       historyData.forEach((_, index) => {
        const t = 0.1 + (index / (historyData.length - 1)) * 0.8;
        const diff = Math.abs(t - cameraState.progress);
        if (diff < minDiff) {
          minDiff = diff;
          currentIndex = index;
        }
      });
  }

  if (currentIndex > 0) {
    closeDetail();
    const prevEraData = historyData[currentIndex - 1];
    selectEra(prevEraData);
  }
};

const checkAutoTrigger = () => {
    if (mode === 'orbit') return;

    // Check if we are close to any node
    for (const obj of eraObjects) {
        const diff = Math.abs(cameraState.progress - obj.t);
        // Trigger radius: very small
        if (diff < 0.005) {
            if (lastVisitedEraId.value !== obj.era.id) {
                selectEra(obj.era);
            }
            return; 
        }
    }
    
    // If we are far enough from the last visited node, reset it so we can visit it again if we turn back
    if (lastVisitedEraId.value) {
         const lastObj = eraObjects.find(o => o.era.id === lastVisitedEraId.value);
         if (lastObj && Math.abs(cameraState.progress - lastObj.t) > 0.02) {
             lastVisitedEraId.value = null;
         }
    }
};

const onWindowResize = () => {
  if (!camera || !renderer) return;
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
  labelRenderer.setSize(window.innerWidth, window.innerHeight);
};

const animate = () => {
  animationId = requestAnimationFrame(animate);
  
  if (mode === 'orbit') {
      controls.update();
      // Keep stars rotating slowly
      if (stars) stars.rotation.z += 0.0002;
  } else {
      // Path Mode Logic
      
      // Smooth damping for scroll
      const oldProgress = cameraState.progress;
      cameraState.progress += (cameraState.targetProgress - cameraState.progress) * 0.05;
      
      // Check for auto-popup
      // Only check if we are moving
      if (Math.abs(cameraState.progress - oldProgress) > 0.00001) {
          checkAutoTrigger();
      }

      // Sync UI slider
      scrollProgress.value = cameraState.progress;

      if (curve) {
        const t = Math.max(0.001, Math.min(0.999, cameraState.progress));
        const position = curve.getPointAt(t);
        const lookAtPosition = curve.getPointAt(Math.min(1, t + 0.05)); 
        
        // Simple offset camera logic
        // To make it smoother, we can use Frenet frames but simple lookAt works for this "on rails" feel
        camera.position.copy(position).add(new THREE.Vector3(0, 1, 4)); 
        camera.lookAt(lookAtPosition);
      }
      
      if (stars) {
          stars.rotation.z += 0.0005;
      }
  }

  renderer.render(scene, camera);
  labelRenderer.render(scene, camera);
};

// --- Lifecycle ---
onMounted(() => {
  initThree();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', onWindowResize);
  window.removeEventListener('wheel', onWheel);
  window.removeEventListener('touchstart', onTouchStart);
  window.removeEventListener('touchmove', onTouchMove);
  window.removeEventListener('click', onClick);
  cancelAnimationFrame(animationId);
  if (renderer) {
    renderer.dispose();
  }
  if (controls) {
      controls.dispose();
  }
});

</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.5s cubic-bezier(0.25, 1, 0.5, 1);
}

.slide-right-enter-from,
.slide-right-leave-to {
  transform: translateX(100%);
}

/* Custom scrollbar for detail panel */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
}
::-webkit-scrollbar-thumb {
  background: rgba(234, 179, 8, 0.5);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: rgba(234, 179, 8, 0.8);
}
</style>
