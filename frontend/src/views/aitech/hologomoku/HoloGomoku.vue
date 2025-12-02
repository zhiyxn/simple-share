<template>
  <div class="hologomoku-container">
    <AiTechBackButton />
    <div v-if="isLoading" id="loading">
      系统初始化中...<br>启动全息投影...<br>正在连接神经接口...
      <div v-if="loadingError" class="error">{{ loadingError }}</div>
    </div>
    
    <div id="ui-layer">
      <div id="status" ref="statusRef">等待玩家就绪</div>
      <div id="controls">
        手势说明:<br>
        - 食指移动: 控制光标<br>
        - 拇指+食指捏合: 锁定/落子<br>
        - 保持捏合: 拖拽锁定<br>
      </div>
      <button class="back-btn" @click="$router.push('/aitech')">返回列表</button>
    </div>
    
    <video ref="videoInput" id="video-input" style="display:none"></video>
    <canvas ref="overlayCanvas" id="overlay-canvas"></canvas>
    <div ref="canvasContainer" id="canvas-container"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer.js';
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass.js';
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass.js';
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue'

const router = useRouter();
const canvasContainer = ref(null);
const videoInput = ref(null);
const overlayCanvas = ref(null);
const statusRef = ref(null);
const isLoading = ref(true);
const loadingError = ref('');

// --- Configuration ---
const BOARD_SIZE = 15;
const CELL_SIZE = 2;
const BOARD_WIDTH = (BOARD_SIZE - 1) * CELL_SIZE;
const COLOR_PLAYER = 0x00ffff; // Cyan
const COLOR_AI = 0xff0000;     // Red
const COLOR_GRID = 0x0044aa;
const COLOR_CURSOR = 0xffff00;

// --- State ---
let scene, camera, renderer, composer, controls;
let boardGrid = []; // 2D array for logic: 0=empty, 1=player, 2=AI
let boardGroup;
let cursorMesh;
let isPlayerTurn = true;
let gameActive = true;
let lastPinchState = false; // false = open, true = pinched
let pinchStartTime = 0;
let pieces = [];
let effects = [];
let animationId = null;
let cameraUtils = null;
let hands = null;
let overlayCtx = null;
let lastIndexTip = null; // For smoothing

// --- Initialization ---
onMounted(async () => {
  try {
    await loadMediaPipeScripts();
    initThree();
    initGame();
    await initMediaPipe();
  } catch (error) {
    console.error("Initialization failed:", error);
    loadingError.value = "初始化失败: " + error.message;
  }
});

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId);
  if (cameraUtils) cameraUtils.stop();
  if (hands) hands.close();
  
  // Cleanup Three.js
  if (renderer) {
    renderer.dispose();
    if (canvasContainer.value && renderer.domElement) {
      canvasContainer.value.removeChild(renderer.domElement);
    }
  }
  if (scene) {
    scene.traverse((object) => {
      if (object.geometry) object.geometry.dispose();
      if (object.material) {
        if (Array.isArray(object.material)) {
          object.material.forEach(m => m.dispose());
        } else {
          object.material.dispose();
        }
      }
    });
  }
  
  window.removeEventListener('resize', onWindowResize);
});

// Load MediaPipe Scripts Dynamically
function loadScript(src) {
  return new Promise((resolve, reject) => {
    if (document.querySelector(`script[src="${src}"]`)) {
      resolve();
      return;
    }
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
  
  for (const src of scripts) {
    await loadScript(src);
  }
  
  // Wait a bit to ensure globals are registered if needed
  await new Promise(resolve => setTimeout(resolve, 100));
}

function initThree() {
  // Scene
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0x050505);
  scene.fog = new THREE.FogExp2(0x050505, 0.015);

  // Camera
  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000);
  camera.position.set(0, 40, 30);
  camera.lookAt(0, 0, 0);

  // Renderer
  renderer = new THREE.WebGLRenderer({ antialias: true });
  renderer.setPixelRatio(window.devicePixelRatio);
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.toneMapping = THREE.ReinhardToneMapping;
  canvasContainer.value.appendChild(renderer.domElement);

  // Post-processing (Bloom)
  const renderScene = new RenderPass(scene, camera);
  const bloomPass = new UnrealBloomPass(new THREE.Vector2(window.innerWidth, window.innerHeight), 1.5, 0.4, 0.85);
  bloomPass.threshold = 0.1; // Increase threshold to reduce blur on dark areas
  bloomPass.strength = 1.2; // Reduce strength for clarity
  bloomPass.radius = 0.4;

  composer = new EffectComposer(renderer);
  composer.addPass(renderScene);
  composer.addPass(bloomPass);

  // Orbit Controls
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;
  controls.maxPolarAngle = Math.PI / 2; // Don't go below ground


  // Lights
  const ambientLight = new THREE.AmbientLight(0x404040);
  scene.add(ambientLight);
  
  const pointLight = new THREE.PointLight(0xffffff, 1, 100);
  pointLight.position.set(0, 50, 0);
  scene.add(pointLight);

  // Holographic Grid Board
  createBoard();

  // Cursor
  const cursorGeo = new THREE.RingGeometry(0.8, 1.0, 32);
  const cursorMat = new THREE.MeshBasicMaterial({ color: COLOR_CURSOR, side: THREE.DoubleSide, transparent: true, opacity: 0.8 });
  cursorMesh = new THREE.Mesh(cursorGeo, cursorMat);
  cursorMesh.rotation.x = -Math.PI / 2;
  cursorMesh.visible = false;
  scene.add(cursorMesh);
  
  // Background Stars
  addStars();

  // Resize handler
  window.addEventListener('resize', onWindowResize);
  
  // Overlay Canvas Size
  overlayCanvas.value.width = window.innerWidth;
  overlayCanvas.value.height = window.innerHeight;
  overlayCtx = overlayCanvas.value.getContext('2d');
  
  animate();
}

function createBoard() {
  boardGroup = new THREE.Group();
  
  // Main grid lines
  const gridHelper = new THREE.GridHelper(BOARD_WIDTH + 2, BOARD_SIZE, 0x0088ff, 0x002244); // Brighter grid
  gridHelper.position.y = -0.1;
  boardGroup.add(gridHelper);
  
  // Add a semi-transparent plane for better visibility
  const bgGeo = new THREE.PlaneGeometry(BOARD_WIDTH + 2, BOARD_WIDTH + 2);
  const bgMat = new THREE.MeshBasicMaterial({ color: 0x000510, transparent: true, opacity: 0.5, side: THREE.DoubleSide });
  const bgPlane = new THREE.Mesh(bgGeo, bgMat);
  bgPlane.rotation.x = -Math.PI / 2;
  bgPlane.position.y = -0.15;
  boardGroup.add(bgPlane);

  // Board Plane (invisible raycast target)
  const planeGeo = new THREE.PlaneGeometry(BOARD_WIDTH + 10, BOARD_WIDTH + 10);
  const planeMat = new THREE.MeshBasicMaterial({ visible: false });
  const plane = new THREE.Mesh(planeGeo, planeMat);
  plane.rotation.x = -Math.PI / 2;
  plane.name = "BoardPlane";
  boardGroup.add(plane);

  scene.add(boardGroup);
}

function addStars() {
  const geometry = new THREE.BufferGeometry();
  const vertices = [];
  for (let i = 0; i < 1000; i++) {
      vertices.push(THREE.MathUtils.randFloatSpread(200)); // x
      vertices.push(THREE.MathUtils.randFloatSpread(100) + 50); // y
      vertices.push(THREE.MathUtils.randFloatSpread(200)); // z
  }
  geometry.setAttribute('position', new THREE.Float32BufferAttribute(vertices, 3));
  const material = new THREE.PointsMaterial({ color: 0x888888, size: 0.5 });
  const stars = new THREE.Points(geometry, material);
  scene.add(stars);
}

function initGame() {
  boardGrid = Array(BOARD_SIZE).fill().map(() => Array(BOARD_SIZE).fill(0));
  pieces = [];
  isPlayerTurn = true;
  gameActive = true;
  updateStatus("玩家回合 (青色)", "#0ff");
}

function updateStatus(text, color) {
  if (statusRef.value) {
    statusRef.value.innerText = text;
    statusRef.value.style.color = color;
  }
}

async function initMediaPipe() {
  if (!window.Hands || !window.Camera) {
    throw new Error("MediaPipe scripts not loaded correctly.");
  }

  hands = new window.Hands({locateFile: (file) => {
      return `https://cdn.jsdelivr.net/npm/@mediapipe/hands/${file}`;
  }});
  
  hands.setOptions({
      maxNumHands: 2,
      modelComplexity: 1,
      minDetectionConfidence: 0.5,
      minTrackingConfidence: 0.5
  });
  
  hands.onResults(onHandsResults);
  
  cameraUtils = new window.Camera(videoInput.value, {
      onFrame: async () => {
          await hands.send({image: videoInput.value});
      },
      width: 1280,
      height: 720
  });
  
  await cameraUtils.start();
  isLoading.value = false;
}

function onHandsResults(results) {
  if (!overlayCtx) return;
  
  overlayCtx.clearRect(0, 0, overlayCanvas.value.width, overlayCanvas.value.height);
  
  if (results.multiHandLandmarks && results.multiHandLandmarks.length > 0) {
      const landmarks = results.multiHandLandmarks[0];
      
      drawHandSkeleton(landmarks);
      processInteraction(landmarks);
      
  } else {
      cursorMesh.visible = false;
  }
}

function drawHandSkeleton(landmarks) {
  overlayCtx.save();
  const width = overlayCanvas.value.width;
  const height = overlayCanvas.value.height;
  
  const connections = window.HAND_CONNECTIONS || window.Hands.HAND_CONNECTIONS;
  if (!connections) {
      console.error("HAND_CONNECTIONS not found!");
      return;
  }
  overlayCtx.strokeStyle = '#00ffff';
  overlayCtx.lineWidth = 2;
  overlayCtx.shadowColor = '#00ffff';
  overlayCtx.shadowBlur = 10;
  
  for (const conn of connections) {
      const p1 = landmarks[conn[0]];
      const p2 = landmarks[conn[1]];
      
      const x1 = p1.x * width;
      const y1 = p1.y * height;
      const x2 = p2.x * width;
      const y2 = p2.y * height;
      
      overlayCtx.beginPath();
      overlayCtx.moveTo(x1, y1);
      overlayCtx.lineTo(x2, y2);
      overlayCtx.stroke();
  }
  
  overlayCtx.fillStyle = '#ffffff';
  for (let lm of landmarks) {
       overlayCtx.beginPath();
       overlayCtx.arc(lm.x * width, lm.y * height, 3, 0, 2 * Math.PI);
       overlayCtx.fill();
  }
  
  overlayCtx.restore();
}

function processInteraction(landmarks) {
  if (!gameActive || !isPlayerTurn) return;

  let indexTip = landmarks[8];
  const thumbTip = landmarks[4];
  
  // Smoothing
  if (lastIndexTip) {
      indexTip.x = lastIndexTip.x * 0.5 + indexTip.x * 0.5;
      indexTip.y = lastIndexTip.y * 0.5 + indexTip.y * 0.5;
  }
  lastIndexTip = { x: indexTip.x, y: indexTip.y };
  
  const dx = indexTip.x - thumbTip.x;
  const dy = indexTip.y - thumbTip.y;
  const distance = Math.sqrt(dx*dx + dy*dy);
  
  const isMagnetic = distance < 0.08; 
  const isPinching = distance < 0.04; 
  
  const ndcX = (indexTip.x * 2) - 1;
  const ndcY = -(indexTip.y * 2) + 1; 
  
  const raycaster = new THREE.Raycaster();
  raycaster.setFromCamera(new THREE.Vector2(ndcX, ndcY), camera);
  
  const intersects = raycaster.intersectObjects(boardGroup.children);
  const planeIntersect = intersects.find(hit => hit.object.name === "BoardPlane");
  
  if (planeIntersect) {
      cursorMesh.visible = true;
      const hitPoint = planeIntersect.point;
      
      const halfWidth = BOARD_WIDTH / 2;
      const localX = hitPoint.x + halfWidth;
      const localZ = hitPoint.z + halfWidth;
      
      let gridX = Math.round(localX / CELL_SIZE);
      let gridY = Math.round(localZ / CELL_SIZE);
      
      gridX = Math.max(0, Math.min(BOARD_SIZE - 1, gridX));
      gridY = Math.max(0, Math.min(BOARD_SIZE - 1, gridY));
      
      const snappedX = (gridX * CELL_SIZE) - halfWidth;
      const snappedZ = (gridY * CELL_SIZE) - halfWidth;
      
      // Occupied check for cursor color
      const isOccupied = boardGrid[gridY][gridX] !== 0;

      if (isMagnetic) {
          cursorMesh.position.set(snappedX, 0.1, snappedZ);
          
          if (isOccupied) {
              cursorMesh.material.color.setHex(0xff0000); // Red if occupied
          } else {
              cursorMesh.material.color.setHex(0xff00ff); // Magenta when locked
          }
          
          // Logic: Pinch to lock, Hold for > 200ms to confirm? 
          // Or Pinch -> Release (Click)
          // Let's implement "Click" (Pinch then Release) with a short hold check
          
          if (isPinching) {
              if (!lastPinchState) {
                  // Pinch Start
                  pinchStartTime = Date.now();
              }
          } else {
              if (lastPinchState) {
                  // Pinch Release
                  const duration = Date.now() - pinchStartTime;
                  // Valid click if held for a short time (prevent noise) but not too long? 
                  // Or just any release. Let's say any release > 50ms
                  if (duration > 50) {
                       attemptMove(gridX, gridY);
                  }
              }
          }
      } else {
          cursorMesh.position.set(hitPoint.x, 0.1, hitPoint.z);
          cursorMesh.material.color.setHex(COLOR_CURSOR);
      }
  } else {
      cursorMesh.visible = false;
  }
  
  lastPinchState = isPinching;
}

function attemptMove(gx, gy) {
  // Double check occupancy to prevent overlap
  if (boardGrid[gy][gx] !== 0) {
      createErrorEffect(gx, gy);
      return;
  }
  
  placePiece(gx, gy, 1);
  
  if (checkWin(gx, gy, 1)) {
      gameOver(1);
      return;
  }
  
  isPlayerTurn = false;
  updateStatus("AI 思考中...", "#f00");
  
  setTimeout(aiMove, 1000);
}

function placePiece(gx, gy, type) {
  boardGrid[gy][gx] = type;
  
  const halfWidth = BOARD_WIDTH / 2;
  const worldX = (gx * CELL_SIZE) - halfWidth;
  const worldZ = (gy * CELL_SIZE) - halfWidth;
  
  const geometry = new THREE.SphereGeometry(0.8, 32, 32);
  const color = (type === 1) ? COLOR_PLAYER : COLOR_AI;
  const material = new THREE.MeshStandardMaterial({
      color: 0x000000,
      emissive: color,
      emissiveIntensity: 2.0,
      roughness: 0.1,
      metalness: 0.8
  });
  
  const mesh = new THREE.Mesh(geometry, material);
  mesh.position.set(worldX, 0.8, worldZ);
  scene.add(mesh);
  pieces.push(mesh);
  
  if (type === 1) {
      createShockwave(worldX, worldZ, color);
  } else {
      createOrbitalLaser(worldX, worldZ, color);
  }
}

function aiMove() {
  if (!gameActive) return;
  
  const move = findBestMove(2);
  if (move) {
      placePiece(move.x, move.y, 2);
      if (checkWin(move.x, move.y, 2)) {
          gameOver(2);
          return;
      }
  }
  
  isPlayerTurn = true;
  updateStatus("玩家回合 (青色)", "#0ff");
}

function findBestMove(playerType) {
  // Instant win
  for(let y=0; y<BOARD_SIZE; y++) {
      for(let x=0; x<BOARD_SIZE; x++) {
          if(boardGrid[y][x] === 0) {
              boardGrid[y][x] = playerType;
              if(checkWin(x, y, playerType)) {
                  boardGrid[y][x] = 0;
                  return {x, y};
              }
              boardGrid[y][x] = 0;
          }
      }
  }
  
  // Block opponent
  const opponent = (playerType === 1) ? 2 : 1;
  for(let y=0; y<BOARD_SIZE; y++) {
      for(let x=0; x<BOARD_SIZE; x++) {
          if(boardGrid[y][x] === 0) {
              boardGrid[y][x] = opponent;
              if(checkWin(x, y, opponent)) {
                  boardGrid[y][x] = 0;
                  return {x, y};
              }
              boardGrid[y][x] = 0;
          }
      }
  }
  
  // Heuristic
  let bestScore = -Infinity;
  let bestMoves = [];
  const center = Math.floor(BOARD_SIZE/2);
  
  for(let y=0; y<BOARD_SIZE; y++) {
      for(let x=0; x<BOARD_SIZE; x++) {
          if(boardGrid[y][x] === 0) {
              let score = 0;
              const dist = Math.abs(x - center) + Math.abs(y - center);
              score -= dist;
              score += Math.random() * 5;
              
              if(score > bestScore) {
                  bestScore = score;
                  bestMoves = [{x, y}];
              } else if (score === bestScore) {
                  bestMoves.push({x, y});
              }
          }
      }
  }
  
  if(bestMoves.length > 0) {
      return bestMoves[Math.floor(Math.random() * bestMoves.length)];
  }
  return null;
}

function checkWin(x, y, type) {
  const directions = [[1, 0], [0, 1], [1, 1], [1, -1]];
  for (const [dx, dy] of directions) {
      let count = 1;
      let i = 1;
      while(true) {
          const nx = x + dx * i;
          const ny = y + dy * i;
          if (nx < 0 || nx >= BOARD_SIZE || ny < 0 || ny >= BOARD_SIZE || boardGrid[ny][nx] !== type) break;
          count++;
          i++;
      }
      i = 1;
      while(true) {
          const nx = x - dx * i;
          const ny = y - dy * i;
          if (nx < 0 || nx >= BOARD_SIZE || ny < 0 || ny >= BOARD_SIZE || boardGrid[ny][nx] !== type) break;
          count++;
          i++;
      }
      if (count >= 5) return true;
  }
  return false;
}

function gameOver(winner) {
  gameActive = false;
  const msg = winner === 1 ? "玩家胜利! (Player Wins)" : "AI 胜利! (AI Wins)";
  const color = winner === 1 ? "#0ff" : "#f00";
  updateStatus(msg, color);
}

// --- Effects ---
function createShockwave(x, z, color) {
  const geometry = new THREE.RingGeometry(0.5, 0.8, 32);
  const material = new THREE.MeshBasicMaterial({ 
      color: color, 
      side: THREE.DoubleSide, 
      transparent: true, 
      opacity: 1.0 
  });
  const mesh = new THREE.Mesh(geometry, material);
  mesh.position.set(x, 0.2, z);
  mesh.rotation.x = -Math.PI / 2;
  scene.add(mesh);
  
  effects.push({
      mesh: mesh,
      type: 'shockwave',
      age: 0,
      update: function(dt) {
          this.age += dt;
          const scale = 1 + this.age * 5;
          this.mesh.scale.set(scale, scale, 1);
          this.mesh.material.opacity = Math.max(0, 1 - this.age * 1.5);
          return this.age < 0.7;
      }
  });
}

function createOrbitalLaser(x, z, color) {
  const h = 100;
  const geometry = new THREE.CylinderGeometry(0.2, 0.2, h, 8);
  const material = new THREE.MeshBasicMaterial({ 
      color: color, 
      transparent: true, 
      opacity: 0.8 
  });
  const mesh = new THREE.Mesh(geometry, material);
  mesh.position.set(x, h/2, z);
  scene.add(mesh);
  
  createShockwave(x, z, color);

  effects.push({
      mesh: mesh,
      type: 'laser',
      age: 0,
      update: function(dt) {
          this.age += dt;
          this.mesh.material.opacity = Math.max(0, 0.8 - this.age * 2);
          this.mesh.scale.set(Math.max(0, 1 - this.age), 1, Math.max(0, 1 - this.age));
          return this.age < 0.5;
      }
  });
}

function createErrorEffect(gx, gy) {
  console.log("Invalid Move");
}

const clock = new THREE.Clock();

function animate() {
  animationId = requestAnimationFrame(animate);
  
  const dt = clock.getDelta();
  
  for (let i = effects.length - 1; i >= 0; i--) {
      const active = effects[i].update(dt);
      if (!active) {
          scene.remove(effects[i].mesh);
          if (effects[i].mesh.geometry) effects[i].mesh.geometry.dispose();
          if (effects[i].mesh.material) effects[i].mesh.material.dispose();
          effects.splice(i, 1);
      }
  }

  if (composer) composer.render();
  if (controls) controls.update();
}

function onWindowResize() {
  if (!camera || !renderer) return;
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
  if (composer) composer.setSize(window.innerWidth, window.innerHeight);
  if (overlayCanvas.value) {
    overlayCanvas.value.width = window.innerWidth;
    overlayCanvas.value.height = window.innerHeight;
  }
}
</script>

<style scoped>
.hologomoku-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: radial-gradient(circle at center, #1a1a2e 0%, #000000 100%);
  user-select: none;
  font-family: 'Courier New', Courier, monospace;
  position: fixed; /* Take over the screen */
  top: 0;
  left: 0;
  z-index: 9999;
}

#canvas-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 1;
}

#overlay-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 2;
  pointer-events: none;
}

#ui-layer {
  position: absolute;
  top: 20px;
  left: 20px;
  color: #0ff;
  z-index: 5;
  text-shadow: 0 0 10px #0ff;
}

#status {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
}

#controls {
  font-size: 14px;
  opacity: 0.8;
}

#loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #0ff;
  font-size: 20px;
  z-index: 10;
  text-align: center;
  text-shadow: 0 0 15px #0ff;
}

.error {
  color: #f00;
  margin-top: 10px;
  font-size: 16px;
}

.back-btn {
  margin-top: 20px;
  padding: 8px 16px;
  background: rgba(0, 255, 255, 0.1);
  border: 1px solid #0ff;
  color: #0ff;
  cursor: pointer;
  pointer-events: auto;
  font-family: inherit;
  text-transform: uppercase;
}

.back-btn:hover {
  background: rgba(0, 255, 255, 0.3);
}

/* Scanlines */
.hologomoku-container::after {
  content: " ";
  display: block;
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.25) 50%), linear-gradient(90deg, rgba(255, 0, 0, 0.06), rgba(0, 255, 0, 0.02), rgba(0, 0, 255, 0.06));
  z-index: 100;
  background-size: 100% 2px, 3px 100%;
  pointer-events: none;
}
</style>
