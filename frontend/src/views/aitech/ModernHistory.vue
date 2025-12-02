<template>
  <div class="modern-history-container">
    <AiTechBackButton />
    <div ref="canvasContainer" class="canvas-container"></div>

    <!-- Loading -->
    <div v-if="loading" class="loading-overlay">
      <div class="loader-content">
        <div class="spinner"></div>
        <div class="loading-text">历史回响加载中...</div>
      </div>
    </div>

    <!-- Interactive Overlay -->
    <div class="content-overlay" :class="{ 'active': currentEvent }">
      <div class="timeline-progress">
        <div class="progress-bar" :style="{ height: scrollProgress + '%' }"></div>
      </div>

      <!-- Active Event Info Panel -->
      <transition name="slide-fade">
        <div v-if="currentEvent" class="event-panel">
          <div class="year-badge">{{ currentEvent.year }}</div>
          <h1 class="event-title">{{ currentEvent.title }}</h1>
          <h2 class="event-subtitle">{{ currentEvent.subtitle }}</h2>
          
          <div class="event-content">
            <p>{{ currentEvent.description }}</p>
            <div class="event-impact">
              <strong>历史影响：</strong>
              <span>{{ currentEvent.impact }}</span>
            </div>
          </div>

          <div class="scroll-hint" v-if="!isLastEvent">
            <span class="arrow">↓</span> 继续探索
          </div>
        </div>
      </transition>

      <!-- Intro / Outro Overlay -->
      <div v-if="!currentEvent && scrollProgress < 5" class="intro-text">
        <h1>觉醒之路</h1>
        <p>滚动滚轮，重走中国近代史的峥嵘岁月</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import * as THREE from 'three';
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer';
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass';
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass';
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue';

// --- Data ---
interface HistoryEvent {
  year: number;
  title: string;
  subtitle: string;
  description: string;
  impact: string;
  color: string;
}

const events: HistoryEvent[] = [
  {
    year: 1840,
    title: '鸦片战争',
    subtitle: '屈辱的开端',
    description: '英国发动侵略战争，清政府被迫签订中国近代史上第一个不平等条约《南京条约》。中国开始沦为半殖民地半封建社会，国家主权和领土完整遭到严重破坏。',
    impact: '中国近代史的开端，自然经济开始解体，民族危机日益加深。',
    color: '#8B0000' // Dark Red
  },
  {
    year: 1860,
    title: '火烧圆明园',
    subtitle: '文明的浩劫',
    description: '第二次鸦片战争期间，英法联军攻入北京，抢掠并焚毁了"万园之园"圆明园。这是人类文明史上的一次巨大浩劫，暴露了清政府的腐败无能。',
    impact: '加深了中国的半殖民地化程度，激起了人民的反抗。',
    color: '#FF4500' // Orange Red
  },
  {
    year: 1894,
    title: '甲午战争',
    subtitle: '沉痛的教训',
    description: '日本发动侵华战争，北洋水师全军覆没，清政府被迫签订《马关条约》。割让台湾等地，赔款白银二亿两，允许日本在华设厂。',
    impact: '大大加深了中国社会的半殖民地化程度，列强掀起了瓜分中国的狂潮。',
    color: '#2F4F4F' // Dark Slate Gray
  },
  {
    year: 1900,
    title: '八国联军侵华',
    subtitle: '国耻的极点',
    description: '为镇压义和团运动，八国联军攻陷北京。清政府被迫签订《辛丑条约》，赔款4.5亿两白银，划定使馆界。',
    impact: '中国完全陷入半殖民地半封建社会的深渊，清政府完全成为"洋人的朝廷"。',
    color: '#000000' // Black
  },
  {
    year: 1911,
    title: '辛亥革命',
    subtitle: '帝制的终结',
    description: '孙中山领导的资产阶级民主革命，推翻了清朝统治，结束了中国两千多年的封建帝制，建立了中华民国。',
    impact: '使民主共和观念深入人心，打开了中国进步潮流的闸门。',
    color: '#DAA520' // Goldenrod
  },
  {
    year: 1919,
    title: '五四运动',
    subtitle: '思想的觉醒',
    description: '巴黎和会外交失败引发的反帝反封建爱国运动。"外争主权，内除国贼"，工人阶级登上政治舞台。',
    impact: '中国新民主主义革命的开端，促进了马克思主义在中国的传播。',
    color: '#1E90FF' // Dodger Blue
  },
  {
    year: 1921,
    title: '中共一大',
    subtitle: '开天辟地',
    description: '中国共产党第一次全国代表大会在上海（后转移至嘉兴南湖红船）召开，宣告了中国共产党的诞生。',
    impact: '中国革命的面貌从此焕然一新，中国人民有了坚强的领导核心。',
    color: '#FF0000' // Red
  },
  {
    year: 1937,
    title: '全民族抗战',
    subtitle: '血肉筑长城',
    description: '七七事变爆发，日本发动全面侵华战争。国共两党第二次合作，建立抗日民族统一战线，全民族浴血奋战。',
    impact: '中华民族近代以来规模最大、时间最长、范围最广的反侵略战争。',
    color: '#A52A2A' // Brown
  },
  {
    year: 1945,
    title: '抗战胜利',
    subtitle: '民族的凯歌',
    description: '日本宣布无条件投降。中国人民取得了近代以来反抗外敌入侵的第一次完全胜利。',
    impact: '洗刷了近代以来的民族耻辱，确立了中国在世界四强中的地位。',
    color: '#FFD700' // Gold
  },
  {
    year: 1949,
    title: '开国大典',
    subtitle: '站起来了',
    description: '毛泽东主席在天安门城楼庄严宣告中华人民共和国成立。中国人民推翻了三座大山，当家作主。',
    impact: '开辟了中国历史的新纪元，中国从此走上了独立、民主、统一的道路。',
    color: '#FF0000' // Bright Red
  }
];

// --- State ---
const canvasContainer = ref<HTMLElement | null>(null);
const loading = ref(true);
const scrollProgress = ref(0); // 0 to 100
const currentEvent = ref<HistoryEvent | null>(null);
const isLastEvent = computed(() => currentEvent.value?.year === 1949);

// --- Three.js Vars ---
let scene: THREE.Scene;
let camera: THREE.PerspectiveCamera;
let renderer: THREE.WebGLRenderer;
let composer: EffectComposer;
let particles: THREE.Points;
let path: THREE.CatmullRomCurve3;
let tube: THREE.Mesh;
let eventMarkers: THREE.Mesh[] = [];

// Animation vars
let targetProgress = 0; // 0 to 1
let currentProgress = 0;
let animationId: number;

// --- Lifecycle ---
onMounted(() => {
  initThree();
  animate();
  window.addEventListener('resize', onResize);
  window.addEventListener('wheel', onWheel);
  
  // Fake loading
  setTimeout(() => {
    loading.value = false;
  }, 1500);
});

onBeforeUnmount(() => {
  cancelAnimationFrame(animationId);
  window.removeEventListener('resize', onResize);
  window.removeEventListener('wheel', onWheel);
  renderer?.dispose();
});

// --- Three.js Logic ---
function initThree() {
  if (!canvasContainer.value) return;

  // Scene
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0x050505);
  scene.fog = new THREE.FogExp2(0x050505, 0.002);

  // Camera
  camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
  camera.position.set(0, 0, 10);

  // Renderer
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  canvasContainer.value.appendChild(renderer.domElement);

  // Post Processing
  const renderScene = new RenderPass(scene, camera);
  const bloomPass = new UnrealBloomPass(new THREE.Vector2(window.innerWidth, window.innerHeight), 1.5, 0.4, 0.85);
  bloomPass.threshold = 0.2;
  bloomPass.strength = 1.5;
  bloomPass.radius = 0.5;
  
  composer = new EffectComposer(renderer);
  composer.addPass(renderScene);
  composer.addPass(bloomPass);

  // Lighting
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.1);
  scene.add(ambientLight);

  // Create Path (Helix/Spiral)
  createPath();
  
  // Create Particles (Atmosphere)
  createParticles();
}

function createPath() {
  const points = [];
  const radius = 10;
  const height = 200;
  const turns = 3;
  
  for (let i = 0; i <= 100; i++) {
    const t = i / 100;
    const angle = t * Math.PI * 2 * turns;
    const x = Math.cos(angle) * radius;
    const z = Math.sin(angle) * radius;
    const y = t * height;
    points.push(new THREE.Vector3(x, y, z));
  }

  path = new THREE.CatmullRomCurve3(points);

  // Draw Tube (The "Road")
  const tubeGeo = new THREE.TubeGeometry(path, 200, 0.5, 8, false);
  const tubeMat = new THREE.MeshStandardMaterial({ 
    color: 0xff0000, 
    emissive: 0x550000,
    roughness: 0.4,
    metalness: 0.8,
    transparent: true,
    opacity: 0.6
  });
  tube = new THREE.Mesh(tubeGeo, tubeMat);
  scene.add(tube);

  // Add Event Markers along the path
  events.forEach((event, index) => {
    const t = (index + 1) / (events.length + 1); // Distribute evenly
    const pos = path.getPointAt(t);
    
    // Marker Mesh (Glowing Orb)
    const geo = new THREE.SphereGeometry(0.8, 32, 32);
    const mat = new THREE.MeshStandardMaterial({
      color: event.color,
      emissive: event.color,
      emissiveIntensity: 2,
      toneMapped: false
    });
    const mesh = new THREE.Mesh(geo, mat);
    mesh.position.copy(pos);
    mesh.userData = { event, t }; // Store data
    scene.add(mesh);
    eventMarkers.push(mesh);

    // Add a light at each marker
    const light = new THREE.PointLight(event.color, 1, 15);
    light.position.copy(pos);
    scene.add(light);
  });
}

function createParticles() {
  const geometry = new THREE.BufferGeometry();
  const count = 3000;
  const positions = new Float32Array(count * 3);
  const colors = new Float32Array(count * 3);

  for (let i = 0; i < count; i++) {
    positions[i * 3] = (Math.random() - 0.5) * 100;
    positions[i * 3 + 1] = (Math.random() - 0.1) * 250; // Tall vertical spread
    positions[i * 3 + 2] = (Math.random() - 0.5) * 100;

    // Red/Orange hues for revolution theme
    colors[i * 3] = 1.0;
    colors[i * 3 + 1] = Math.random() * 0.5;
    colors[i * 3 + 2] = 0.0;
  }

  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
  geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));

  const material = new THREE.PointsMaterial({
    size: 0.3,
    vertexColors: true,
    transparent: true,
    opacity: 0.6,
    blending: THREE.AdditiveBlending
  });

  particles = new THREE.Points(geometry, material);
  scene.add(particles);
}

// --- Interaction ---
function onWheel(e: WheelEvent) {
  // Scroll delta controls progress
  const delta = e.deltaY * 0.0005;
  targetProgress += delta;
  targetProgress = Math.max(0, Math.min(1, targetProgress));
}

function animate() {
  animationId = requestAnimationFrame(animate);
  
  // Smooth Camera
  currentProgress += (targetProgress - currentProgress) * 0.05;
  scrollProgress.value = Math.round(currentProgress * 100);

  if (path) {
    // Move camera along path
    const camPos = path.getPointAt(currentProgress);
    const lookAtPos = path.getPointAt(Math.min(1, currentProgress + 0.05));
    
    // Offset camera slightly to the side/back
    // Calculate tangent to know which way is "forward"
    const tangent = path.getTangentAt(currentProgress).normalize();
    const normal = new THREE.Vector3(0, 1, 0);
    const binormal = new THREE.Vector3().crossVectors(tangent, normal).normalize();
    
    // Position: On the path + offset
    camera.position.copy(camPos)
      .add(binormal.multiplyScalar(8)) // Move right
      .add(new THREE.Vector3(0, 2, 0)); // Move up
      
    camera.lookAt(lookAtPos);
    
    // Update Tube Material (pulse)
    const tubeMat = tube.material as THREE.MeshStandardMaterial;
    tubeMat.emissiveIntensity = 0.5 + Math.sin(Date.now() * 0.002) * 0.2;

    // Particle Animation
    if (particles) {
      particles.rotation.y += 0.001;
    }

    // Check for active event
    checkActiveEvent();
  }

  composer.render();
}

function checkActiveEvent() {
  let found = false;
  const threshold = 0.05; // How close to the marker to trigger

  for (const marker of eventMarkers) {
    const t = marker.userData.t;
    if (Math.abs(currentProgress - t) < threshold) {
      if (currentEvent.value !== marker.userData.event) {
        currentEvent.value = marker.userData.event;
      }
      found = true;
      
      // Scale up the active marker
      marker.scale.lerp(new THREE.Vector3(2, 2, 2), 0.1);
    } else {
      marker.scale.lerp(new THREE.Vector3(1, 1, 1), 0.1);
    }
  }

  if (!found) {
    currentEvent.value = null;
  }
}

function onResize() {
  if (!canvasContainer.value) return;
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
  composer.setSize(window.innerWidth, window.innerHeight);
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;700;900&display=swap');

.modern-history-container {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
  background: #000;
  color: #fff;
  font-family: 'Noto Serif SC', serif;
}

.canvas-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

/* Loading */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #000;
  z-index: 100;
  display: flex;
  justify-content: center;
  align-items: center;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 3px solid #333;
  border-top-color: #d43f3f;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin { to { transform: rotate(360deg); } }

.loading-text {
  color: #d43f3f;
  font-size: 1.2rem;
  letter-spacing: 2px;
}

/* Content Overlay */
.content-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 10;
  pointer-events: none; /* Let clicks pass through */
  padding: 40px;
  box-sizing: border-box;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-end; /* Panel on right */
}

.timeline-progress {
  position: absolute;
  left: 40px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 60%;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.progress-bar {
  width: 100%;
  background: #d43f3f;
  transition: height 0.1s linear;
}

/* Intro Text */
.intro-text {
  position: absolute;
  bottom: 10%;
  left: 10%;
  text-shadow: 0 0 20px rgba(0,0,0,0.8);
  animation: fadeIn 2s ease-out;
}

.intro-text h1 {
  font-size: 5rem;
  color: #d43f3f;
  margin: 0;
  letter-spacing: 10px;
  font-weight: 900;
}

.intro-text p {
  font-size: 1.5rem;
  color: #ccc;
  margin-top: 10px;
}

/* Event Panel */
.event-panel {
  width: 450px;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(10px);
  padding: 40px;
  border-left: 4px solid #d43f3f;
  color: #fff;
  pointer-events: auto;
  margin-right: 5%;
  box-shadow: -10px 0 30px rgba(0, 0, 0, 0.5);
}

.year-badge {
  font-size: 4rem;
  font-weight: 900;
  color: rgba(255, 255, 255, 0.1);
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: -1;
}

.event-title {
  font-size: 2.5rem;
  color: #d43f3f;
  margin: 0 0 10px 0;
  text-shadow: 0 0 10px rgba(212, 63, 63, 0.3);
}

.event-subtitle {
  font-size: 1.2rem;
  color: #aaa;
  margin-bottom: 30px;
  font-weight: 400;
  border-bottom: 1px solid #333;
  padding-bottom: 10px;
  display: inline-block;
}

.event-content p {
  line-height: 1.8;
  font-size: 1.1rem;
  color: #ddd;
  margin-bottom: 20px;
  text-align: justify;
}

.event-impact {
  background: rgba(212, 63, 63, 0.1);
  padding: 15px;
  border-radius: 4px;
  border: 1px solid rgba(212, 63, 63, 0.3);
}

.event-impact strong {
  color: #d43f3f;
  display: block;
  margin-bottom: 5px;
}

.scroll-hint {
  margin-top: 30px;
  font-size: 0.9rem;
  color: #666;
  animation: bounce 2s infinite;
  display: flex;
  align-items: center;
  gap: 10px;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {transform: translateY(0);}
  40% {transform: translateY(-5px);}
  60% {transform: translateY(-3px);}
}

/* Transitions */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.5s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(50px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-50px);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
