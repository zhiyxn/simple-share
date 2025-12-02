<template>
  <div class="holo-earth-container">
    <AiTechBackButton />
    <div v-if="isLoading" id="loading">
      <div class="loading-spinner"></div>
      <div class="loading-text">正在加载全息地球数据...</div>
    </div>
    
    <div id="ui-layer">
      <div class="header">
        <h1>🌏 全息地球航线监控</h1>
        <div class="status-bar">
          <span :class="{ active: isGestureEnabled }">
            <span class="icon">🖐️</span> 手势控制: {{ isGestureEnabled ? '已开启' : '已关闭' }}
          </span>
          <span class="separator">|</span>
          <span>🖱️ 鼠标控制: 始终开启</span>
        </div>
      </div>

      <div class="controls-panel">
        <button 
          class="toggle-btn" 
          :class="{ 'btn-active': isGestureEnabled }"
          @click="toggleGesture"
        >
          {{ isGestureEnabled ? '🚫 关闭手势控制' : '🖐️ 开启手势控制' }}
        </button>
        
        <div class="gesture-guide" v-if="isGestureEnabled">
          <div class="guide-item">✊ 捏合拖拽: 旋转地球</div>
          <div class="guide-item">🤏 捏合缩放: 调整距离</div>
        </div>
      </div>

      <button class="back-btn" @click="$router.push('/aitech')">返回列表</button>
    </div>
    
    <video ref="videoInput" id="video-input" style="display:none"></video>
    <canvas ref="overlayCanvas" id="overlay-canvas" v-show="isGestureEnabled"></canvas>
    <div ref="canvasContainer" id="canvas-container"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass.js';
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer.js';
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass.js';
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue'

const canvasContainer = ref(null);
const videoInput = ref(null);
const overlayCanvas = ref(null);
const isLoading = ref(true);
const isGestureEnabled = ref(true);

// Three.js globals
let scene, camera, renderer, controls, composer;
let earthGroup, earthMesh, cloudsMesh, atmosphereMesh;
let animationId = null;

// MediaPipe globals
let hands = null;
let cameraUtils = null;
let overlayCtx = null;
let lastHandPos = null;
let lastHandDist = null; // For zoom
let pinchStartDist = 0;
let isPinching = false;

// Config
const EARTH_RADIUS = 10;

onMounted(async () => {
  try {
    initThree();
    addRoutes();
    
    // Pre-load MediaPipe scripts but don't start camera yet
    await loadMediaPipeScripts();
    await initMediaPipe(); 
    
    if (isGestureEnabled.value) {
       // Wait a bit for video element to be ready? usually cameraUtils handles it
       cameraUtils.start();
    }
    
    isLoading.value = false;
  } catch (e) {
    console.error(e);
    isLoading.value = false; // Show anyway
  }
});

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId);
  if (renderer) renderer.dispose();
  if (cameraUtils) cameraUtils.stop();
  if (hands) hands.close();
  window.removeEventListener('resize', onWindowResize);
});

watch(isGestureEnabled, (newVal) => {
  if (newVal) {
    // Start camera if not started
    if (cameraUtils && !videoInput.value.srcObject) {
      cameraUtils.start();
    }
    controls.enabled = false; // Disable mouse orbit when gesture is dominant? 
    // Actually user said "mouse ... rotation" too.
    // Usually we keep OrbitControls enabled for mouse, but gesture might conflict.
    // Let's keep OrbitControls enabled for mouse, but gesture will manually rotate camera/object.
    // If we manually rotate camera with gesture, we should probably disable OrbitControls auto-update or coordinate.
    controls.enabled = true; // Keep mouse working
  } else {
    // Stop camera to save resources? Or just hide?
    // Let's keep it running for fast toggle, just stop processing in loop.
    // Or stop it.
    // User requirement: "Close gesture control".
    controls.enabled = true;
  }
});

function toggleGesture() {
  isGestureEnabled.value = !isGestureEnabled.value;
  if (isGestureEnabled.value && cameraUtils) {
     cameraUtils.start();
  } else if (!isGestureEnabled.value && cameraUtils) {
     cameraUtils.stop();
     // Clear canvas
     if(overlayCtx) overlayCtx.clearRect(0, 0, overlayCanvas.value.width, overlayCanvas.value.height);
  }
}

// --- Three.js Setup ---
function initThree() {
  const container = canvasContainer.value;
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0x000510);
  scene.fog = new THREE.FogExp2(0x000510, 0.02);

  camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 1000);
  camera.position.set(0, 0, 40);

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.setPixelRatio(window.devicePixelRatio);
  // Tone mapping for better lighting
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.outputColorSpace = THREE.SRGBColorSpace;
  container.appendChild(renderer.domElement);

  // Controls
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;
  controls.minDistance = 12;
  controls.maxDistance = 100;
  controls.autoRotate = true;
  controls.autoRotateSpeed = 0.5;

  // Lighting
  const ambientLight = new THREE.AmbientLight(0xffffff, 1.2);
  scene.add(ambientLight);

  const sunLight = new THREE.DirectionalLight(0xffffff, 2.0);
  sunLight.position.set(50, 30, 50);
  scene.add(sunLight);
  
  const spotLight = new THREE.SpotLight(0x3388ff, 5);
  spotLight.position.set(-20, 20, 20);
  spotLight.lookAt(0,0,0);
  scene.add(spotLight);

  // Earth Group
  earthGroup = new THREE.Group();
  scene.add(earthGroup);

  const loader = new THREE.TextureLoader();

  // 1. Earth Sphere
  const earthGeo = new THREE.SphereGeometry(EARTH_RADIUS, 64, 64);
  const earthMat = new THREE.MeshPhongMaterial({
    specular: new THREE.Color(0x333333),
    shininess: 15
  });
  
  // Load textures with fallback
  loader.load(
    'https://raw.githubusercontent.com/mrdoob/three.js/master/examples/textures/planets/earth_atmos_2048.jpg',
    (tex) => { tex.colorSpace = THREE.SRGBColorSpace; earthMat.map = tex; earthMat.needsUpdate = true; },
    undefined,
    (err) => { console.warn('Texture error, using fallback color', err); earthMat.color.set(0x0044ff); earthMat.wireframe = true; }
  );
  
  loader.load(
    'https://raw.githubusercontent.com/mrdoob/three.js/master/examples/textures/planets/earth_specular_2048.jpg',
    (tex) => { earthMat.specularMap = tex; earthMat.needsUpdate = true; }
  );

  earthMesh = new THREE.Mesh(earthGeo, earthMat);
  earthGroup.add(earthMesh);

  // Add Wireframe for "Map/Tech" look
  const wireframeGeo = new THREE.WireframeGeometry(earthGeo);
  const wireframeMat = new THREE.LineBasicMaterial({ color: 0x44aaff, transparent: true, opacity: 0.15 });
  const wireframe = new THREE.LineSegments(wireframeGeo, wireframeMat);
  earthGroup.add(wireframe);

  // 2. Clouds (Hidden for clarity)
  const cloudGeo = new THREE.SphereGeometry(EARTH_RADIUS + 0.1, 64, 64);
  const cloudMat = new THREE.MeshPhongMaterial({
    transparent: true,
    opacity: 0.8,
    blending: THREE.AdditiveBlending,
    side: THREE.DoubleSide,
    visible: false // Hide clouds to see countries clearly
  });
  
  loader.load(
    'https://raw.githubusercontent.com/mrdoob/three.js/master/examples/textures/planets/earth_clouds_1024.png',
    (tex) => { cloudMat.map = tex; cloudMat.needsUpdate = true; },
    undefined,
    () => { cloudMat.visible = false; } // Hide clouds if load fails
  );
  
  cloudsMesh = new THREE.Mesh(cloudGeo, cloudMat);
  earthGroup.add(cloudsMesh);

  // 3. Atmosphere Glow (Shader)
  const atmosGeo = new THREE.SphereGeometry(EARTH_RADIUS + 2.5, 64, 64);
  const vertexShader = `
    varying vec3 vNormal;
    void main() {
      vNormal = normalize(normalMatrix * normal);
      gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    }
  `;
  const fragmentShader = `
    varying vec3 vNormal;
    void main() {
      float intensity = pow(0.6 - dot(vNormal, vec3(0, 0, 1.0)), 4.0);
      gl_FragColor = vec4(0.2, 0.6, 1.0, 1.0) * intensity;
    }
  `;
  const atmosMat = new THREE.ShaderMaterial({
    vertexShader,
    fragmentShader,
    blending: THREE.AdditiveBlending,
    side: THREE.BackSide,
    transparent: true
  });
  atmosphereMesh = new THREE.Mesh(atmosGeo, atmosMat);
  scene.add(atmosphereMesh); // Add to scene, not group, so it doesn't rotate with earth texture? 
  // Actually atmosphere glow usually is static relative to camera or just a rim effect.
  // BackSide approach works best if static.

  // Stars
  addStars();

  // Post-processing (Bloom)
  const renderScene = new RenderPass(scene, camera);
  const bloomPass = new UnrealBloomPass(new THREE.Vector2(window.innerWidth, window.innerHeight), 1.5, 0.4, 0.85);
  bloomPass.threshold = 0.2;
  bloomPass.strength = 0.8; // Strong glow for routes
  bloomPass.radius = 0.5;
  
  composer = new EffectComposer(renderer);
  composer.addPass(renderScene);
  composer.addPass(bloomPass);

  // Setup Overlay Canvas
  overlayCanvas.value.width = window.innerWidth;
  overlayCanvas.value.height = window.innerHeight;
  overlayCtx = overlayCanvas.value.getContext('2d');

  window.addEventListener('resize', onWindowResize);
  animate();
}

function addStars() {
  const r = 200;
  const starsGeometry = new THREE.BufferGeometry();
  const starsVertices = [];
  for (let i = 0; i < 3000; i++) {
    const x = THREE.MathUtils.randFloatSpread(r * 2);
    const y = THREE.MathUtils.randFloatSpread(r * 2);
    const z = THREE.MathUtils.randFloatSpread(r * 2);
    starsVertices.push(x, y, z);
  }
  starsGeometry.setAttribute('position', new THREE.Float32BufferAttribute(starsVertices, 3));
  const starsMaterial = new THREE.PointsMaterial({ color: 0xffffff, size: 0.5, transparent: true, opacity: 0.6 });
  const starField = new THREE.Points(starsGeometry, starsMaterial);
  scene.add(starField);
}

// --- Route Visualization ---

// Helper: Lat/Lon to Vector3
function latLonToVector3(lat, lon, radius) {
  const phi = (90 - lat) * (Math.PI / 180);
  const theta = (lon + 180) * (Math.PI / 180);
  const x = -(radius * Math.sin(phi) * Math.cos(theta));
  const z = (radius * Math.sin(phi) * Math.sin(theta));
  const y = (radius * Math.cos(phi));
  return new THREE.Vector3(x, y, z);
}

function addRoutes() {
  const cities = [
    { name: 'Beijing', lat: 39.9042, lon: 116.4074 },
    { name: 'New York', lat: 40.7128, lon: -74.0060 },
    { name: 'London', lat: 51.5074, lon: -0.1278 },
    { name: 'Tokyo', lat: 35.6762, lon: 139.6503 },
    { name: 'Sydney', lat: -33.8688, lon: 151.2093 },
    { name: 'Moscow', lat: 55.7558, lon: 37.6173 },
    { name: 'Rio', lat: -22.9068, lon: -43.1729 },
    { name: 'Cape Town', lat: -33.9249, lon: 18.4241 }
  ];

  // Create markers
  cities.forEach(city => {
    const pos = latLonToVector3(city.lat, city.lon, EARTH_RADIUS);
    const markerGeo = new THREE.SphereGeometry(0.15, 16, 16);
    const markerMat = new THREE.MeshBasicMaterial({ color: 0xff0000 });
    const marker = new THREE.Mesh(markerGeo, markerMat);
    marker.position.copy(pos);
    earthGroup.add(marker);
    
    // Label (Simple billboard or just skip for now, 3D text is heavy. Let's assume visual is enough)
  });

  // Create curves
  const routes = [
    [0, 1], [0, 2], [0, 3], [0, 4], // Beijing to others
    [1, 2], [1, 6], // NY to London, Rio
    [2, 5], [2, 7], // London to Moscow, Cape Town
    [3, 4] // Tokyo to Sydney
  ];

  routes.forEach(pair => {
    const start = cities[pair[0]];
    const end = cities[pair[1]];
    createCurve(start, end);
  });
}

function createCurve(startCity, endCity) {
  const v1 = latLonToVector3(startCity.lat, startCity.lon, EARTH_RADIUS);
  const v2 = latLonToVector3(endCity.lat, endCity.lon, EARTH_RADIUS);
  
  // Control points for Bezier (elevated)
  const dist = v1.distanceTo(v2);
  const mid = v1.clone().add(v2).multiplyScalar(0.5).normalize().multiplyScalar(EARTH_RADIUS + dist * 0.5);
  
  const curve = new THREE.QuadraticBezierCurve3(v1, mid, v2);
  const points = curve.getPoints(50);
  const geometry = new THREE.BufferGeometry().setFromPoints(points);
  
  const material = new THREE.LineBasicMaterial({ 
    color: 0x00ffff, 
    transparent: true, 
    opacity: 0.6,
    linewidth: 2 // Note: WebGL linewidth is often limited to 1
  });
  
  // TubeGeometry might be better for visibility
  const tubeGeo = new THREE.TubeGeometry(curve, 20, 0.05, 8, false);
  const tubeMat = new THREE.MeshBasicMaterial({ color: 0x00ffff });
  
  const mesh = new THREE.Mesh(tubeGeo, tubeMat);
  earthGroup.add(mesh);
}

// --- MediaPipe & Gesture ---

async function loadScript(src) {
  return new Promise((resolve, reject) => {
    if (document.querySelector(`script[src="${src}"]`)) { resolve(); return; }
    const script = document.createElement('script');
    script.src = src;
    script.crossOrigin = "anonymous";
    script.onload = resolve;
    script.onerror = reject;
    document.head.appendChild(script);
  });
}

async function loadMediaPipeScripts() {
  const scripts = [
    "https://cdn.jsdelivr.net/npm/@mediapipe/camera_utils/camera_utils.js",
    "https://cdn.jsdelivr.net/npm/@mediapipe/control_utils/control_utils.js",
    "https://cdn.jsdelivr.net/npm/@mediapipe/drawing_utils/drawing_utils.js",
    "https://cdn.jsdelivr.net/npm/@mediapipe/hands/hands.js"
  ];
  for (const src of scripts) await loadScript(src);
}

async function initMediaPipe() {
  if (!window.Hands) throw new Error("Hands lib not loaded");
  
  hands = new window.Hands({locateFile: (file) => `https://cdn.jsdelivr.net/npm/@mediapipe/hands/${file}`});
  hands.setOptions({
    maxNumHands: 2,
    modelComplexity: 1,
    minDetectionConfidence: 0.6,
    minTrackingConfidence: 0.6
  });
  hands.onResults(onHandsResults);
  
  cameraUtils = new window.Camera(videoInput.value, {
    onFrame: async () => {
      if(isGestureEnabled.value) await hands.send({image: videoInput.value});
    },
    width: 640,
    height: 480
  });
  // Do not start here. Wait for toggle.
}

function onHandsResults(results) {
  if (!isGestureEnabled.value) return;
  
  overlayCtx.clearRect(0, 0, overlayCanvas.value.width, overlayCanvas.value.height);
  
  if (results.multiHandLandmarks && results.multiHandLandmarks.length > 0) {
    const landmarks = results.multiHandLandmarks; // Array of hands
    
    // Draw all hands
    landmarks.forEach(hand => drawHand(hand));
    
    processGestures(landmarks);
    
    // If gesture active, pause auto rotation
    controls.autoRotate = false;
  } else {
    lastHandPos = null;
    lastHandDist = null;
    // Resume auto rotation if idle?
    controls.autoRotate = true;
  }
}

function drawHand(landmarks) {
  const ctx = overlayCtx;
  const w = overlayCanvas.value.width;
  const h = overlayCanvas.value.height;
  
  ctx.save();
  ctx.strokeStyle = '#00ffff';
  ctx.lineWidth = 2;
  
  const connections = window.HAND_CONNECTIONS || [
    [0,1],[1,2],[2,3],[3,4],
    [0,5],[5,6],[6,7],[7,8],
    [5,9],[9,10],[10,11],[11,12],
    [9,13],[13,14],[14,15],[15,16],
    [13,17],[17,18],[18,19],[19,20],
    [0,17]
  ];

  for (const conn of connections) {
    const p1 = landmarks[conn[0]];
    const p2 = landmarks[conn[1]];
    ctx.beginPath();
    ctx.moveTo(p1.x * w, p1.y * h);
    ctx.lineTo(p2.x * w, p2.y * h);
    ctx.stroke();
  }
  ctx.restore();
}

function zoomCamera(factor) {
  if (!camera || !controls) return;
  const offset = camera.position.clone().sub(controls.target);
  let dist = offset.length();
  dist *= factor;
  dist = Math.max(controls.minDistance, Math.min(controls.maxDistance, dist));
  offset.setLength(dist);
  camera.position.copy(controls.target).add(offset);
  controls.update();
}

function processGestures(allHands) {
  // Two hands -> Zoom
  if (allHands.length === 2) {
    const h1 = allHands[0][9]; // Middle finger MCP (centerish)
    const h2 = allHands[1][9];
    
    const dist = Math.hypot(h1.x - h2.x, h1.y - h2.y);
    
    if (lastHandDist) {
      const delta = dist - lastHandDist;
      // If distance increasing -> Zoom In (dollyIn)
      // If distance decreasing -> Zoom Out (dollyOut)
      
      const zoomSensitivity = 2.0;
      
      if (Math.abs(delta) > 0.01) { // Threshold
        if (delta > 0) {
             // Spread -> Zoom In
             zoomCamera(1 / (1 + delta * zoomSensitivity));
        } else {
             // Pinch -> Zoom Out
             zoomCamera(1 + Math.abs(delta) * zoomSensitivity);
        }
      }
    }
    
    lastHandDist = dist;
    lastHandPos = null; // Reset rotation state
    return;
  }
  
  // One hand -> Rotate (Pinch) or Old Zoom (Vertical)
  lastHandDist = null;
  const landmarks = allHands[0];
  const index = landmarks[8];
  const thumb = landmarks[4];
  
  // Pinch detection (Index tip to Thumb tip)
  const dist = Math.hypot(index.x - thumb.x, index.y - thumb.y);
  const isCurrentlyPinching = dist < 0.05;
  
  if (isCurrentlyPinching) {
    if (!isPinching) {
      isPinching = true;
    }
    
    // Rotate Logic
    if (lastHandPos) {
      const dx = index.x - lastHandPos.x;
      const dy = index.y - lastHandPos.y;
      
      // Sensitivity
      const rotateSpeed = 3.0;
      
      // Rotate the Earth object directly
      earthGroup.rotation.y += dx * rotateSpeed;
      earthGroup.rotation.x += dy * rotateSpeed;
    }
  } else {
    isPinching = false;
    
    // Keep vertical zoom as fallback if one hand?
    // Or just disable to avoid accidental zooms.
    // Let's keep it but make it less sensitive or require a specific pose?
    // Actually, let's remove the vertical zoom if two-hand zoom is the primary way,
    // to avoid confusion. Or keep it for single-hand users.
    // User asked for "Add gesture zoom".
    // I'll keep the vertical zoom but maybe visualize it better?
    // Or maybe just rely on Two Hand Zoom as the "New Feature".
    // Let's comment out the vertical zoom to avoid conflict/confusion, 
    // assuming the user will use two hands for zoom which is more natural.
    // Wait, what if they only have one hand free?
    // Let's keep vertical zoom but only at edges?
    // Previous logic: y < 0.2 (Top) or y > 0.8 (Bottom). This is fine.
    
    const zoomSpeed = 0.5;
    if (index.y < 0.15) { // Top 15%
       zoomCamera(1 / 1.02);
    } else if (index.y > 0.85) { // Bottom 15%
       zoomCamera(1.02);
    }
  }
  
  lastHandPos = { x: index.x, y: index.y };
}


function animate() {
  animationId = requestAnimationFrame(animate);
  
  // Rotate clouds slightly
  if (cloudsMesh) {
    cloudsMesh.rotation.y += 0.0002;
  }
  
  controls.update();
  composer.render();
}

function onWindowResize() {
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
  composer.setSize(window.innerWidth, window.innerHeight);
  if(overlayCanvas.value) {
    overlayCanvas.value.width = window.innerWidth;
    overlayCanvas.value.height = window.innerHeight;
  }
}
</script>

<style scoped>
.holo-earth-container {
  width: 100vw;
  height: 100vh;
  background: #000;
  overflow: hidden;
  position: relative;
  color: white;
  font-family: 'Segoe UI', sans-serif;
}

#loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 100;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 3px solid rgba(0, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #00ffff;
  animation: spin 1s ease-in-out infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

#ui-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
  z-index: 1000;
  pointer-events: none; /* Let clicks pass through to canvas */
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
}

.header h1 {
  margin: 0;
  font-size: 24px;
  color: #00ffff;
}

.status-bar {
  font-size: 14px;
  color: #aaa;
  background: rgba(0,0,0,0.5);
  padding: 5px 15px;
  border-radius: 20px;
  border: 1px solid #333;
}

.status-bar .active {
  color: #00ff00;
  text-shadow: 0 0 5px #00ff00;
}

.controls-panel {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  pointer-events: auto;
}

.toggle-btn {
  background: rgba(0, 20, 40, 0.8);
  border: 2px solid #00ffff;
  color: #00ffff;
  padding: 12px 30px;
  font-size: 18px;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 0 15px rgba(0, 255, 255, 0.2);
}

.toggle-btn:hover {
  background: rgba(0, 255, 255, 0.2);
  box-shadow: 0 0 25px rgba(0, 255, 255, 0.5);
}

.toggle-btn.btn-active {
  background: rgba(255, 50, 50, 0.2);
  border-color: #ff5555;
  color: #ff5555;
  box-shadow: 0 0 15px rgba(255, 50, 50, 0.2);
}

.gesture-guide {
  margin-top: 15px;
  background: rgba(0,0,0,0.6);
  padding: 10px;
  border-radius: 10px;
  font-size: 14px;
  color: #ddd;
}

.back-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  background: transparent;
  border: 1px solid #444;
  color: #888;
  padding: 5px 15px;
  cursor: pointer;
  pointer-events: auto;
}

.back-btn:hover {
  border-color: #fff;
  color: #fff;
}

#overlay-canvas {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 10;
  pointer-events: none;
}
</style>
