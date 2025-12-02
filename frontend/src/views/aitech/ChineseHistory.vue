<template>
  <div class="history-3d-container">
    <div ref="canvasContainer" class="canvas-container"></div>
    
    <!-- Loading Screen -->
    <div v-if="loading" class="loading-screen">
      <div class="loader-text">历史长河加载中...</div>
      <div class="loading-bar"><div class="bar-fill" :style="{ width: loadingProgress + '%' }"></div></div>
    </div>

    <!-- UI Overlay -->
    <div class="ui-overlay" :class="{ 'hidden': isImmersive }">
      <div class="header">
        <h1 class="title">中华上下五千年</h1>
        <p class="subtitle">滚动鼠标穿越时空 · 点击朝代查看详情</p>
      </div>
      
      <!-- Timeline Slider/Indicator -->
      <div class="timeline-control">
        <div class="year-display">{{ currentYearLabel }}</div>
        <div class="slider-track" @click="handleTimelineClick">
          <div class="slider-thumb" :style="{ left: timelineProgress + '%' }"></div>
        </div>
      </div>
    </div>

    <!-- Detail Modal -->
    <transition name="modal-fade">
      <div v-if="selectedEra" class="detail-modal" @click.self="closeModal">
        <div class="modal-content glass-effect">
          <button class="close-btn" @click="closeModal">×</button>
          <div class="modal-header" :style="{ borderColor: selectedEra.color }">
            <h2 :style="{ color: selectedEra.color }">{{ selectedEra.name }}</h2>
            <span class="period">{{ formatYear(selectedEra.start) }} ~ {{ formatYear(selectedEra.end) }}</span>
          </div>
          <div class="modal-body">
            <div class="info-row">
              <span class="label">持续时间:</span>
              <span class="value">{{ selectedEra.end - selectedEra.start }} 年</span>
            </div>
            <p class="description">{{ selectedEra.desc }}</p>
            
            <!-- Key Events (Mock Data for demo) -->
            <div class="events-list">
              <h3>✨ 历史大事件</h3>
              <ul>
                <li v-for="(event, idx) in getEraEvents(selectedEra.name)" :key="idx">
                  {{ event }}
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </transition>
    
    <!-- Navigation Hint -->
    <div class="nav-hint" v-if="!isImmersive && !selectedEra">
      <span class="mouse-icon">🖱️</span> 滚动鼠标 / 滑动屏幕
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls';
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass';
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer';
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass';

// --- Data Definitions ---
interface Era {
    name: string;
    start: number;
    end: number;
    desc: string;
    color: string;
}

const historyData: Era[] = [
  { name: '三皇五帝', start: -3000, end: -2070, desc: '中华文明的起源，神话传说时代。包括燧人氏、伏羲氏、神农氏等传说人物，标志着人类从原始社会向文明社会的过渡。', color: '#d48265' },
  { name: '夏朝', start: -2070, end: -1600, desc: '中国历史上第一个世袭制朝代，大禹治水后，其子启继位，开启了"家天下"的局面。二里头遗址被认为是夏朝都城遗存。', color: '#c23531' },
  { name: '商朝', start: -1600, end: -1046, desc: '青铜器文明鼎盛，甲骨文出现，是中国第一个有直接文字记载的王朝。商汤灭夏建立商朝，盘庚迁都于殷。', color: '#2f4554' },
  { name: '周朝', start: -1046, end: -256, desc: '实行分封制和宗法制，制礼作乐。后期分为春秋和战国两个时期，诸子百家争鸣，思想文化空前繁荣。', color: '#61a0a8' },
  { name: '秦朝', start: -221, end: -206, desc: '秦始皇嬴政统一六国，建立中国历史上第一个大一统的中央集权封建王朝。统一度量衡、文字、货币，修筑长城。', color: '#d48265' },
  { name: '汉朝', start: -202, end: 220, desc: '分为西汉和东汉。汉武帝时期国力强盛，开辟丝绸之路，"罢黜百家，独尊儒术"。汉朝是当时世界上最强大的帝国之一。', color: '#91c7ae' },
  { name: '三国', start: 220, end: 280, desc: '魏、蜀、吴三足鼎立。赤壁之战后天下三分，是历史上著名的乱世英雄时期。', color: '#749f83' },
  { name: '晋朝', start: 265, end: 420, desc: '分为西晋和东晋。西晋短暂统一，后发生八王之乱和五胡乱华，衣冠南渡建立东晋。玄学兴盛，书法艺术大发展（王羲之）。', color: '#ca8622' },
  { name: '南北朝', start: 420, end: 589, desc: '中国历史上的一段大分裂时期，政权更迭频繁，但同时也促进了民族大融合和佛教的传播。', color: '#bda29a' },
  { name: '隋朝', start: 581, end: 618, desc: '结束了魏晋南北朝三百多年的分裂局面，重新统一中国。开创科举制，开凿大运河，为唐朝盛世奠定基础。', color: '#6e7074' },
  { name: '唐朝', start: 618, end: 907, desc: '中国封建社会的繁荣顶峰，政治清明，经济繁荣，文化昌盛，万国来朝。唐诗是中国文学的高峰。', color: '#546570' },
  { name: '五代十国', start: 907, end: 960, desc: '唐末藩镇割据的延续，大分裂时期。北方主要经历了五个短暂的王朝，南方则有十个割据政权。', color: '#c4ccd3' },
  { name: '宋朝', start: 960, end: 1279, desc: '分为北宋和南宋。经济文化高度繁荣，科技发达（活字印刷、指南针、火药广泛应用）。理学兴起，词极其繁荣。', color: '#c23531' },
  { name: '元朝', start: 1271, end: 1368, desc: '由蒙古族建立，是中国历史上疆域最辽阔的朝代。确立行省制度，促进了东西方文化交流（马可·波罗）。', color: '#2f4554' },
  { name: '明朝', start: 1368, end: 1644, desc: '汉族复兴，郑和七下西洋宣扬国威。后期资本主义萌芽出现，小说（四大名著中的三部）繁荣。', color: '#61a0a8' },
  { name: '清朝', start: 1636, end: 1912, desc: '中国最后一个封建王朝，由满族建立。康乾盛世时期国力强盛，奠定了现代中国的版图基础。后期逐渐落后于世界潮流。', color: '#d48265' },
  { name: '中华民国', start: 1912, end: 1949, desc: '辛亥革命推翻帝制，建立亚洲第一个共和国。经历了军阀混战、抗日战争和解放战争。', color: '#91c7ae' },
  { name: '中华人民共和国', start: 1949, end: 2025, desc: '1949年10月1日成立。经历了社会主义建设探索、改革开放，实现了从站起来、富起来到强起来的伟大飞跃，致力于中华民族伟大复兴。', color: '#c23531' }
];

// Mock Events Data
const getEraEvents = (name: string) => {
  const events: Record<string, string[]> = {
    '三皇五帝': ['神农尝百草', '仓颉造字', '涿鹿之战'],
    '夏朝': ['大禹治水', '涂山之会', '少康中兴'],
    '商朝': ['盘庚迁殷', '武丁盛世', '牧野之战'],
    '周朝': ['武王伐纣', '平王东迁', '百家争鸣', '商鞅变法'],
    '秦朝': ['修筑长城', '统一度量衡', '焚书坑儒'],
    '汉朝': ['张骞通西域', '卫青霍去病北击匈奴', '蔡伦改进造纸术'],
    '三国': ['赤壁之战', '三顾茅庐', '桃园结义'],
    '唐朝': ['贞观之治', '安史之乱', '玄奘西行'],
    '宋朝': ['王安石变法', '靖康之耻', '毕昇发明活字印刷'],
    '明朝': ['郑和下西洋', '土木堡之变', '李时珍著本草纲目'],
    '清朝': ['康乾盛世', '鸦片战争', '辛亥革命']
  };
  return events[name] || ['该时期暂无详细大事件记录', '文化繁荣发展', '社会制度变革'];
};

// --- Vue State ---
const canvasContainer = ref<HTMLElement | null>(null);
const loading = ref(true);
const loadingProgress = ref(0);
const selectedEra = ref<Era | null>(null);
const isImmersive = ref(false);
const currentYear = ref(-3000);
const timelineProgress = ref(0);

// --- Three.js Globals ---
let scene: THREE.Scene;
let camera: THREE.PerspectiveCamera;
let renderer: THREE.WebGLRenderer;
let composer: EffectComposer;
let curve: THREE.CatmullRomCurve3;
let cameraProgress = 0;
let targetCameraProgress = 0;
let animationId: number;
const raycaster = new THREE.Raycaster();
const mouse = new THREE.Vector2();
const clickMouse = new THREE.Vector2(); // For detecting clicks vs drags
const eraObjects: { mesh: THREE.Mesh, data: Era, startP: number, endP: number }[] = [];

// --- Helper Functions ---
const formatYear = (year: number) => year < 0 ? `公元前${Math.abs(year)}` : `${year}`;
const currentYearLabel = computed(() => formatYear(Math.round(currentYear.value)));

// --- Initialization ---
onMounted(async () => {
  initThree();
  // Simulate loading
  let p = 0;
  const interval = setInterval(() => {
    p += 5;
    loadingProgress.value = p;
    if(p >= 100) {
      clearInterval(interval);
      loading.value = false;
    }
  }, 50);

  window.addEventListener('resize', onWindowResize);
  window.addEventListener('wheel', onWheel);
  window.addEventListener('mousemove', onMouseMove);
  window.addEventListener('click', onClick);
});

onBeforeUnmount(() => {
  cancelAnimationFrame(animationId);
  window.removeEventListener('resize', onWindowResize);
  window.removeEventListener('wheel', onWheel);
  window.removeEventListener('mousemove', onMouseMove);
  window.removeEventListener('click', onClick);
  renderer?.dispose();
});

function initThree() {
  if (!canvasContainer.value) return;

  // 1. Scene
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0x1a1a1a);
  scene.fog = new THREE.FogExp2(0x1a1a1a, 0.008);

  // 2. Camera
  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000);
  
  // 3. Renderer
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  canvasContainer.value.appendChild(renderer.domElement);

  // 4. Post Processing (Bloom)
  const renderScene = new RenderPass(scene, camera);
  const bloomPass = new UnrealBloomPass(new THREE.Vector2(window.innerWidth, window.innerHeight), 1.5, 0.4, 0.85);
  bloomPass.threshold = 0.2;
  bloomPass.strength = 1.2; // Glow strength
  bloomPass.radius = 0.5;
  
  composer = new EffectComposer(renderer);
  composer.addPass(renderScene);
  composer.addPass(bloomPass);

  // 5. Lighting
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
  scene.add(ambientLight);
  
  const pointLight = new THREE.PointLight(0xffd700, 2, 50);
  pointLight.position.set(0, 10, 0);
  scene.add(pointLight);

  // 6. Build Path & Eras
  createHistoryPath();
  createStars();

  // Start Loop
  animate();
}

function createHistoryPath() {
  // Create a long winding curve
  const points = [];
  const totalEras = historyData.length;
  // We want the path to go generally "forward" (-Z) and wiggle
  for (let i = 0; i <= totalEras * 2; i++) {
    const z = -i * 15; // Distance between points
    const x = Math.sin(i * 0.5) * 15;
    const y = Math.cos(i * 0.3) * 8;
    points.push(new THREE.Vector3(x, y, z));
  }
  
  curve = new THREE.CatmullRomCurve3(points);
  
  // Draw the continuous line (guide)
  const tubeGeo = new THREE.TubeGeometry(curve, 200, 0.2, 8, false);
  const tubeMat = new THREE.MeshBasicMaterial({ color: 0x333333, transparent: true, opacity: 0.3 });
  const tube = new THREE.Mesh(tubeGeo, tubeMat);
  scene.add(tube);

  // Create Dynasty Segments
  const totalYears = historyData[historyData.length - 1].end - historyData[0].start;
  let accumulatedYears = 0;

  historyData.forEach((era, index) => {
    const duration = era.end - era.start;
    const startRatio = accumulatedYears / totalYears;
    accumulatedYears += duration;
    const endRatio = accumulatedYears / totalYears;

    // Create segment mesh
    const steps = 50; // Higher steps for smoother curve
    const segmentPoints = curve.getPoints(200).slice(
      Math.floor(startRatio * 200), 
      Math.floor(endRatio * 200) + 1
    );

    if (segmentPoints.length < 2) return;

    const segmentCurve = new THREE.CatmullRomCurve3(segmentPoints);
    const geometry = new THREE.TubeGeometry(segmentCurve, 20, 1.5, 8, false);
    const material = new THREE.MeshStandardMaterial({
      color: era.color,
      emissive: era.color,
      emissiveIntensity: 0.5,
      roughness: 0.3,
      metalness: 0.8
    });

    const mesh = new THREE.Mesh(geometry, material);
    mesh.userData = { isEra: true, data: era };
    scene.add(mesh);

    // Store for raycasting
    eraObjects.push({ mesh, data: era, startP: startRatio, endP: endRatio });

    // Create Text Label
    const textPosition = segmentCurve.getPoint(0);
    const textSprite = createTextSprite(era.name, era.color);
    textSprite.position.copy(textPosition);
    textSprite.position.y += 4; // Float above
    scene.add(textSprite);
  });
}

function createTextSprite(text: string, color: string) {
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  if (!ctx) return new THREE.Object3D();

  const fontSize = 64;
  canvas.width = 512;
  canvas.height = 128;
  
  ctx.font = `bold ${fontSize}px "Ma Shan Zheng", "KaiTi", "STKaiti", serif`;
  ctx.fillStyle = color;
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.shadowColor = 'rgba(0,0,0,0.8)';
  ctx.shadowBlur = 4;
  ctx.shadowOffsetX = 2;
  ctx.shadowOffsetY = 2;
  
  ctx.fillText(text, canvas.width / 2, canvas.height / 2);

  const texture = new THREE.CanvasTexture(canvas);
  const material = new THREE.SpriteMaterial({ map: texture, transparent: true });
  const sprite = new THREE.Sprite(material);
  sprite.scale.set(10, 2.5, 1);
  return sprite;
}

function createStars() {
  const geometry = new THREE.BufferGeometry();
  const count = 2000;
  const positions = new Float32Array(count * 3);
  
  for(let i=0; i<count * 3; i++) {
    positions[i] = (Math.random() - 0.5) * 400; // Wide spread
  }
  
  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
  const material = new THREE.PointsMaterial({ size: 0.5, color: 0xffffff, transparent: true, opacity: 0.6 });
  const stars = new THREE.Points(geometry, material);
  scene.add(stars);
}

// --- Interaction Logic ---

function onWheel(e: WheelEvent) {
  if (selectedEra.value) return; // Disable scroll when modal open
  
  // Adjust speed
  const delta = e.deltaY * 0.0005;
  targetCameraProgress += delta;
  targetCameraProgress = Math.max(0, Math.min(1, targetCameraProgress));
}

function handleTimelineClick(e: MouseEvent) {
  const track = e.currentTarget as HTMLElement;
  const rect = track.getBoundingClientRect();
  const p = (e.clientX - rect.left) / rect.width;
  targetCameraProgress = Math.max(0, Math.min(1, p));
}

function onMouseMove(e: MouseEvent) {
  mouse.x = (e.clientX / window.innerWidth) * 2 - 1;
  mouse.y = -(e.clientY / window.innerHeight) * 2 + 1;
}

function onClick(e: MouseEvent) {
  if (loading.value) return;
  
  // Raycast
  raycaster.setFromCamera(mouse, camera);
  const intersects = raycaster.intersectObjects(scene.children);
  
  for (const intersect of intersects) {
    if (intersect.object.userData.isEra) {
      const era = intersect.object.userData.data;
      openModal(era);
      
      // Move camera to focus on this era
      // We can find the approximate progress of this era and scroll there
      const eraObj = eraObjects.find(obj => obj.data.name === era.name);
      if (eraObj) {
        targetCameraProgress = eraObj.startP;
      }
      break;
    }
  }
}

function openModal(era: Era) {
  selectedEra.value = era;
  isImmersive.value = true;
}

function closeModal() {
  selectedEra.value = null;
  isImmersive.value = false;
}

// --- Animation Loop ---
function animate() {
  animationId = requestAnimationFrame(animate);

  // Smooth Camera Movement
  cameraProgress += (targetCameraProgress - cameraProgress) * 0.05;
  
  if (curve) {
    // 1. Get position on curve
    const point = curve.getPointAt(cameraProgress);
    const lookAtPoint = curve.getPointAt(Math.min(1, cameraProgress + 0.05)); // Look ahead
    
    // 2. Offset camera slightly so we are not INSIDE the tube, but flying along it
    // We can use the tangent/normal to offset
    // Simple approach: Just offset Y and X relative to curve
    camera.position.copy(point).add(new THREE.Vector3(5, 3, 10)); // Fixed offset? No, should rotate with curve.
    
    // Better offset:
    camera.position.copy(point);
    camera.position.y += 3;
    camera.position.x += 5;
    
    camera.lookAt(lookAtPoint);
    
    // Update UI State
    timelineProgress.value = cameraProgress * 100;
    
    // Calculate Current Year based on progress
    const totalSpan = historyData[historyData.length-1].end - historyData[0].start;
    currentYear.value = historyData[0].start + (totalSpan * cameraProgress);
  }

  // Hover Effect
  raycaster.setFromCamera(mouse, camera);
  const intersects = raycaster.intersectObjects(scene.children);
  
  // Reset emissive
  eraObjects.forEach(obj => {
    const mat = obj.mesh.material as THREE.MeshStandardMaterial;
    if (mat.userData.originalEmissive) {
        // mat.emissive.setHex(mat.userData.originalEmissive);
    }
  });
  
  let hovered = false;
  for (const intersect of intersects) {
    if (intersect.object.userData.isEra) {
      hovered = true;
      const mat = intersect.object.material as THREE.MeshStandardMaterial;
      // mat.emissiveIntensity = 2; // Highlight
      document.body.style.cursor = 'pointer';
    }
  }
  if (!hovered) {
    document.body.style.cursor = 'default';
  }

  composer.render();
}

function onWindowResize() {
  if (!canvasContainer.value) return;
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
  composer.setSize(window.innerWidth, window.innerHeight);
}

</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Ma+Shan+Zheng&family=ZCOOL+XiaoWei&display=swap');

.history-3d-container {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
  background: #000;
  color: #fff;
}

.canvas-container {
  width: 100%;
  height: 100%;
}

/* Loading Screen */
.loading-screen {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #1a1a1a;
  z-index: 100;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.loader-text {
  font-family: "Ma Shan Zheng", serif;
  font-size: 2rem;
  color: #d4af37;
  margin-bottom: 20px;
}

.loading-bar {
  width: 300px;
  height: 4px;
  background: #333;
  border-radius: 2px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #d4af37;
  transition: width 0.2s;
}

/* UI Overlay */
.ui-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none; /* Let clicks pass through to canvas */
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 40px;
  box-sizing: border-box;
  transition: opacity 0.5s;
}

.ui-overlay.hidden {
  opacity: 0;
}

.header {
  text-align: center;
  pointer-events: auto;
}

.title {
  font-family: "Ma Shan Zheng", serif;
  font-size: 4rem;
  color: #ffd700;
  text-shadow: 0 0 20px rgba(255, 215, 0, 0.5);
  margin: 0;
}

.subtitle {
  color: rgba(255, 255, 255, 0.7);
  font-size: 1.2rem;
  margin-top: 10px;
}

.timeline-control {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  pointer-events: auto;
}

.year-display {
  text-align: center;
  font-size: 3rem;
  font-family: "ZCOOL XiaoWei", serif;
  color: #fff;
  margin-bottom: 10px;
  font-feature-settings: "tnum";
}

.slider-track {
  width: 100%;
  height: 4px;
  background: rgba(255, 255, 255, 0.2);
  cursor: pointer;
  position: relative;
  border-radius: 2px;
}

.slider-thumb {
  width: 20px;
  height: 20px;
  background: #ffd700;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 10px #ffd700;
}

/* Navigation Hint */
.nav-hint {
  position: absolute;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  color: rgba(255, 255, 255, 0.5);
  font-size: 1rem;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 0.5; }
  50% { opacity: 1; }
  100% { opacity: 0.5; }
}

/* Modal */
.detail-modal {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(5px);
  z-index: 50;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  width: 600px;
  max-width: 90%;
  background: rgba(20, 20, 20, 0.9);
  border: 1px solid #333;
  border-radius: 16px;
  padding: 40px;
  position: relative;
  box-shadow: 0 20px 50px rgba(0,0,0,0.5);
  color: #fff;
}

.glass-effect {
  background: rgba(30, 30, 30, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
}

.close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  background: none;
  border: none;
  color: #fff;
  font-size: 2rem;
  cursor: pointer;
  opacity: 0.7;
  transition: opacity 0.3s;
}

.close-btn:hover {
  opacity: 1;
}

.modal-header {
  border-bottom: 2px solid #ffd700;
  padding-bottom: 15px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.modal-header h2 {
  font-family: "Ma Shan Zheng", serif;
  font-size: 3rem;
  margin: 0;
}

.period {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.6);
}

.modal-body .label {
  color: #aaa;
  margin-right: 10px;
}

.modal-body .description {
  font-size: 1.1rem;
  line-height: 1.8;
  margin: 20px 0;
  color: #ddd;
}

.events-list h3 {
  color: #ffd700;
  margin-bottom: 10px;
  font-size: 1.2rem;
}

.events-list ul {
  list-style-type: none;
  padding: 0;
}

.events-list li {
  padding: 8px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: #ccc;
}

/* Transitions */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>
