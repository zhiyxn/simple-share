<template>
  <div class="solar-container" ref="container">
    <AiTechBackButton />
    <div class="loading-mask" v-if="isLoading">
      <div class="loading-content">
        <div class="spinner"></div>
        <p>正在加载星际数据...</p>
      </div>
    </div>

    <div class="controls-panel">
      <div class="panel-header">
        <h3>🌌 星际探索系统</h3>
        <!-- The back-home-btn is redundant now, but we can keep it or remove it. 
             The request was to add a button to top right, which AiTechBackButton does.
             I will keep the original button if it's part of the specific UI flow, or remove it if it conflicts.
             The user said "all card sub-pages should have a back button in top right".
             The existing "back-home-btn" is inside "controls-panel". 
             I'll remove the old button to avoid duplication if it serves the same purpose, 
             or keep it if the user wants it. 
             Let's remove the old button to be cleaner as the new one is "universal".
        -->
      </div>
      <div class="control-group">
        <div class="info-display" v-if="focusedPlanet">
          <h4>{{ focusedPlanet.name }}</h4>
          <p>{{ focusedPlanet.desc }}</p>
          <button class="action-btn" @click="resetView">返回全景</button>
        </div>
        <div class="info-display" v-else>
          <p>点击行星查看详情</p>
          <p class="hint">支持鼠标拖拽旋转，滚轮缩放</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls';
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer';
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass';
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass';
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue';

const container = ref<HTMLElement | null>(null);
const isLoading = ref(true);
const focusedPlanet = ref<any>(null);

// Three.js variables
let scene: THREE.Scene;
let camera: THREE.PerspectiveCamera;
let renderer: THREE.WebGLRenderer;
let controls: OrbitControls;
let composer: EffectComposer;
let raycaster: THREE.Raycaster;
let mouse: THREE.Vector2;

// Animation frame
let animationId: number;

// Objects
const planets: any[] = [];
const orbits: THREE.Line[] = [];
let sun: THREE.Mesh;
let solarGroup: THREE.Group;

// Config
const planetData = [
  { name: '水星', color: 0xA5A5A5, size: 2.4, distance: 28, speed: 0.02, desc: '距离太阳最近的行星，表面温差极大。' },
  { name: '金星', color: 0xE3BB76, size: 6.0, distance: 44, speed: 0.015, desc: '夜空中最亮的星，拥有浓厚的大气层。' },
  { name: '地球', color: 0x22A6F2, size: 6.3, distance: 62, speed: 0.01, desc: '我们需要保护的蓝色家园。' },
  { name: '火星', color: 0xDD4522, size: 3.3, distance: 78, speed: 0.008, desc: '红色的沙漠星球，人类探索的下一站。' },
  { name: '木星', color: 0xD9A066, size: 14.0, distance: 110, speed: 0.004, desc: '太阳系最大的行星，巨大的气态巨行星。' },
  { name: '土星', color: 0xEAD6B8, size: 11.6, distance: 150, speed: 0.003, desc: '拥有美丽光环的行星。', hasRing: true },
  { name: '天王星', color: 0xD1F5F8, size: 8.5, distance: 190, speed: 0.002, desc: '冰巨星，躺在轨道上公转。' },
  { name: '海王星', color: 0x4973FF, size: 8.4, distance: 230, speed: 0.001, desc: '距离太阳最远的行星，风暴强烈。' }
];

// Camera animation target
let targetCameraPos: THREE.Vector3 | null = null;
let targetLookAt: THREE.Vector3 | null = null;

onMounted(async () => {
  initThree();
  createSolarSystem();
  createStars();
  animate();
  
  window.addEventListener('resize', onWindowResize);
  window.addEventListener('click', onMouseClick);
  window.addEventListener('mousemove', onMouseMove);
  
  // Simulate loading
  setTimeout(() => {
    isLoading.value = false;
  }, 1000);
});

onBeforeUnmount(() => {
  cancelAnimationFrame(animationId);
  window.removeEventListener('resize', onWindowResize);
  window.removeEventListener('click', onMouseClick);
  window.removeEventListener('mousemove', onMouseMove);
  
  if (renderer) {
    renderer.dispose();
  }
});

function initThree() {
  if (!container.value) return;

  // Scene
  scene = new THREE.Scene();
  scene.fog = new THREE.FogExp2(0x000000, 0.0005);

  // Camera
  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 2000);
  camera.position.set(0, 150, 250);
  camera.lookAt(0, 0, 0);

  // Renderer
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.setPixelRatio(window.devicePixelRatio);
  container.value.appendChild(renderer.domElement);

  // Controls
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;
  controls.minDistance = 20;
  controls.maxDistance = 800;

  // Post-processing (Bloom)
  const renderScene = new RenderPass(scene, camera);
  const bloomPass = new UnrealBloomPass(new THREE.Vector2(window.innerWidth, window.innerHeight), 1.5, 0.4, 0.85);
  bloomPass.threshold = 0.2;
  bloomPass.strength = 1.2; // Glow strength
  bloomPass.radius = 0.5;

  composer = new EffectComposer(renderer);
  composer.addPass(renderScene);
  composer.addPass(bloomPass);

  // Raycaster
  raycaster = new THREE.Raycaster();
  mouse = new THREE.Vector2();

  // Lights
  const ambientLight = new THREE.AmbientLight(0x333333);
  scene.add(ambientLight);

  const sunLight = new THREE.PointLight(0xffffff, 2, 600);
  scene.add(sunLight);
}

function createSolarSystem() {
  solarGroup = new THREE.Group();
  scene.add(solarGroup);

  // Sun
  const sunGeo = new THREE.SphereGeometry(15, 64, 64);
  const sunMat = new THREE.MeshBasicMaterial({ 
    color: 0xFFDD00,
    transparent: true,
    opacity: 0.9
  });
  sun = new THREE.Mesh(sunGeo, sunMat);
  // Add sun glow layer
  const sunGlowGeo = new THREE.SphereGeometry(16, 64, 64);
  const sunGlowMat = new THREE.MeshBasicMaterial({
    color: 0xFFaa00,
    transparent: true,
    opacity: 0.3,
    side: THREE.BackSide
  });
  const sunGlow = new THREE.Mesh(sunGlowGeo, sunGlowMat);
  sun.add(sunGlow);
  solarGroup.add(sun);

  // Planets
  planetData.forEach(data => {
    const orbitGroup = new THREE.Group();
    
    // Randomize start angle
    const angle = Math.random() * Math.PI * 2;
    orbitGroup.rotation.y = angle;
    
    // Orbit Line
    const orbitCurve = new THREE.EllipseCurve(
      0, 0,
      data.distance, data.distance,
      0, 2 * Math.PI,
      false,
      0
    );
    const points = orbitCurve.getPoints(128);
    const orbitGeo = new THREE.BufferGeometry().setFromPoints(points);
    const orbitMat = new THREE.LineBasicMaterial({ color: 0x333333, transparent: true, opacity: 0.3 });
    const orbitLine = new THREE.Line(orbitGeo, orbitMat);
    orbitLine.rotation.x = Math.PI / 2;
    solarGroup.add(orbitLine);
    orbits.push(orbitLine);

    // Planet Mesh
    const geometry = new THREE.SphereGeometry(data.size, 32, 32);
    const material = new THREE.MeshStandardMaterial({
      color: data.color,
      roughness: 0.7,
      metalness: 0.3,
      emissive: data.color,
      emissiveIntensity: 0.2
    });
    const planet = new THREE.Mesh(geometry, material);
    
    // Position planet
    planet.position.x = data.distance;
    
    // Add Ring for Saturn
    if (data.hasRing) {
      const ringGeo = new THREE.RingGeometry(data.size * 1.4, data.size * 2.2, 64);
      const ringMat = new THREE.MeshBasicMaterial({
        color: 0xAFAFAF,
        side: THREE.DoubleSide,
        transparent: true,
        opacity: 0.6
      });
      const ring = new THREE.Mesh(ringGeo, ringMat);
      ring.rotation.x = Math.PI / 2.2;
      planet.add(ring);
    }

    // Store data for interaction
    planet.userData = { 
      isPlanet: true, 
      name: data.name, 
      desc: data.desc,
      speed: data.speed,
      distance: data.distance,
      angle: angle,
      originalScale: 1
    };

    // Add to group but we will animate position manually to keep hierarchy simple for rotation
    // Actually, simpler: OrbitGroup rotates? No, planets have different speeds.
    // So we add planet directly to solarGroup and update x/z in animate.
    solarGroup.add(planet);
    planets.push(planet);
  });
}

function createStars() {
  const geometry = new THREE.BufferGeometry();
  const vertices = [];
  
  for (let i = 0; i < 3000; i++) {
    const x = THREE.MathUtils.randFloatSpread(1000);
    const y = THREE.MathUtils.randFloatSpread(1000);
    const z = THREE.MathUtils.randFloatSpread(1000);
    vertices.push(x, y, z);
  }
  
  geometry.setAttribute('position', new THREE.Float32BufferAttribute(vertices, 3));
  const material = new THREE.PointsMaterial({ color: 0xFFFFFF, size: 1.5, transparent: true, opacity: 0.8 });
  const stars = new THREE.Points(geometry, material);
  scene.add(stars);
}

function onWindowResize() {
  if (!container.value) return;
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
  composer.setSize(window.innerWidth, window.innerHeight);
}

function onMouseMove(event: MouseEvent) {
  mouse.x = (event.clientX / window.innerWidth) * 2 - 1;
  mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;
}

function onMouseClick(event: MouseEvent) {
  raycaster.setFromCamera(mouse, camera);
  const intersects = raycaster.intersectObjects(planets);

  if (intersects.length > 0) {
    const object = intersects[0].object;
    focusOnPlanet(object);
  } else {
    // Check if clicked on empty space (optional: reset view)
    // resetView();
  }
}

function focusOnPlanet(planet: any) {
  focusedPlanet.value = planet.userData;
  
  // Calculate target position: Offset from planet
  // We need to follow the planet in animate loop if focused
}

function resetView() {
  focusedPlanet.value = null;
  targetCameraPos = new THREE.Vector3(0, 150, 250);
  targetLookAt = new THREE.Vector3(0, 0, 0);
}

function animate() {
  animationId = requestAnimationFrame(animate);

  // Rotate Sun
  if (sun) sun.rotation.y += 0.002;

  // Rotate Planets and Orbit
  const time = Date.now() * 0.001;
  
  planets.forEach(planet => {
    const data = planet.userData;
    
    // Self rotation
    planet.rotation.y += 0.01;
    
    // Orbit revolution
    // Update angle
    data.angle += data.speed;
    
    planet.position.x = Math.cos(data.angle) * data.distance;
    planet.position.z = Math.sin(data.angle) * data.distance;
  });

  // Camera Logic
  if (focusedPlanet.value) {
    // Find the actual mesh object
    const targetPlanet = planets.find(p => p.userData.name === focusedPlanet.value.name);
    if (targetPlanet) {
      const offset = new THREE.Vector3(20, 10, 20); // Offset relative to planet
      const targetPos = targetPlanet.position.clone().add(offset);
      
      // Smooth camera follow
      camera.position.lerp(targetPos, 0.05);
      controls.target.lerp(targetPlanet.position, 0.05);
    }
  } else if (targetCameraPos && targetLookAt) {
      camera.position.lerp(targetCameraPos, 0.05);
      controls.target.lerp(targetLookAt, 0.05);
      
      // Clear if close enough
      if (camera.position.distanceTo(targetCameraPos) < 1) {
          targetCameraPos = null;
      }
  }

  controls.update();
  composer.render();
}
</script>

<style scoped>
.solar-container {
  width: 100%;
  height: 100vh;
  background-color: #000;
  overflow: hidden;
  position: relative;
}

.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #000;
  z-index: 2000;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #333;
  border-top: 4px solid #00eeff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.controls-panel {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 280px;
  background: rgba(0, 15, 30, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 200, 255, 0.2);
  border-radius: 12px;
  padding: 20px;
  color: #fff;
  z-index: 100;
}

.panel-header h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #00eeff;
  text-shadow: 0 0 10px rgba(0, 238, 255, 0.5);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.back-home-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s;
}

.back-home-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: #fff;
}

.info-display h4 {
  margin: 0 0 10px;
  font-size: 1.5rem;
  color: #fff;
}

.info-display p {
  font-size: 0.9rem;
  line-height: 1.5;
  color: #ccc;
  margin-bottom: 15px;
}

.hint {
  color: #666;
  font-size: 0.8rem;
}

.action-btn {
  background: linear-gradient(45deg, #0066ff, #00ccff);
  border: none;
  border-radius: 20px;
  padding: 8px 20px;
  color: #fff;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
}

.action-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 0 15px rgba(0, 200, 255, 0.5);
}
</style>
