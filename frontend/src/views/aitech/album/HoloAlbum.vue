<template>
  <div class="holo-album-container">
    <AiTechBackButton />
    <div v-if="isLoading" id="loading">
      系统初始化中...<br>加载全息相册引擎...<br>正在连接神经接口...
      <div v-if="loadingError" class="error">{{ loadingError }}</div>
    </div>
    
    <div id="ui-layer">
      <div id="status" ref="statusRef">手势/鼠标控制就绪</div>
      <div id="controls">
        操作说明:<br>
        - 🖐️ 手势: 挥手翻页 | 捏合点击放大 | 再次捏合关闭<br>
        - 🖱️ 鼠标: 拖拽旋转 | 点击放大<br>
        - 滚轮: 缩放视角<br>
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
const PHOTO_COUNT = 18;
const RADIUS = 16;
const CARD_WIDTH = 8;
const CARD_HEIGHT = 6;

// --- State ---
let scene, camera, renderer, composer;
let albumGroup;
let cards = [];
let animationId = null;
let cameraUtils = null;
let hands = null;
let overlayCtx = null;
let raycaster, mouse;

// Physics / Interaction
let currentRotationY = 0;
let targetRotationX = 0;
let currentRotationX = 0;
let velocityY = 0.002; 
let isInteracting = false;
let lastHandPos = null;
let pinchStartTime = 0;
let isPinching = false;
let wasPinching = false;

// Zoom State
let zoomState = {
  active: false,
  targetCard: null,
  startPos: new THREE.Vector3(),
  startRot: new THREE.Euler(),
  startParent: null
};

// Mouse State
let isMouseDown = false;
let lastMouseX = 0;
let mouseVelocity = 0;

// --- Initialization ---
onMounted(async () => {
  try {
    await loadMediaPipeScripts();
    initThree();
    initInteraction();
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
  
  if (renderer) {
    renderer.dispose();
    if (canvasContainer.value && renderer.domElement) {
      canvasContainer.value.removeChild(renderer.domElement);
    }
  }
  window.removeEventListener('resize', onWindowResize);
});

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
  await new Promise(resolve => setTimeout(resolve, 100));
}

function initThree() {
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0x050505); // Slightly lighter black to see bloom better
  scene.fog = new THREE.FogExp2(0x050505, 0.015);

  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000);
  camera.position.set(0, 0, 35);
  camera.lookAt(0, 0, 0);

  renderer = new THREE.WebGLRenderer({ antialias: true });
  renderer.setPixelRatio(window.devicePixelRatio);
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.toneMapping = THREE.ReinhardToneMapping;
  canvasContainer.value.appendChild(renderer.domElement);

  // Bloom - Optimized for clarity (Reduced overexposure)
  const renderScene = new RenderPass(scene, camera);
  const bloomPass = new UnrealBloomPass(new THREE.Vector2(window.innerWidth, window.innerHeight), 1.5, 0.4, 0.85);
  bloomPass.threshold = 0.85; // Very high threshold: only white/specular highlights glow
  bloomPass.strength = 0.3;   // Low strength to keep images sharp
  bloomPass.radius = 0.2;

  composer = new EffectComposer(renderer);
  composer.addPass(renderScene);
  composer.addPass(bloomPass);

  const ambientLight = new THREE.AmbientLight(0x404040);
  scene.add(ambientLight);
  
  const pointLight = new THREE.PointLight(0xffffff, 1, 100);
  pointLight.position.set(0, 20, 20);
  scene.add(pointLight);

  createAlbum();
  addParticles();
  
  // Interaction Utils
  raycaster = new THREE.Raycaster();
  mouse = new THREE.Vector2();

  window.addEventListener('resize', onWindowResize);
  overlayCanvas.value.width = window.innerWidth;
  overlayCanvas.value.height = window.innerHeight;
  overlayCtx = overlayCanvas.value.getContext('2d');
  
  animate();
}

function initInteraction() {
  const canvas = canvasContainer.value;
  
  canvas.addEventListener('pointerdown', (e) => {
    if (zoomState.active) {
      // Click to close zoom
      closeZoom();
      return;
    }
    isMouseDown = true;
    lastMouseX = e.clientX;
    mouseVelocity = 0;
    isInteracting = true;
  });
  
  canvas.addEventListener('pointermove', (e) => {
    // Update mouse pos for raycasting
    mouse.x = (e.clientX / window.innerWidth) * 2 - 1;
    mouse.y = -(e.clientY / window.innerHeight) * 2 + 1;
    
    if (isMouseDown && !zoomState.active) {
      const deltaX = e.clientX - lastMouseX;
      lastMouseX = e.clientX;
      // Dragging
      mouseVelocity = deltaX * 0.005;
      currentRotationY += mouseVelocity;
      
      // Tilt based on Y position
      targetRotationX = mouse.y * 0.3; 
    }
  });
  
  canvas.addEventListener('pointerup', (e) => {
    isMouseDown = false;
    isInteracting = false; // Let momentum take over
    
    // Reset tilt when released
    targetRotationX = 0;
    
    // Check for Click (minimal movement)
    if (Math.abs(mouseVelocity) < 0.002 && !zoomState.active) {
      checkClick();
    }
  });
  
  // Wheel to zoom camera slightly
  canvas.addEventListener('wheel', (e) => {
    if (zoomState.active) return;
    const delta = e.deltaY * 0.05;
    camera.position.z = Math.max(20, Math.min(60, camera.position.z + delta));
  });
}

function updateStatus(text, color = '#0ff') {
  if (statusRef.value) {
    statusRef.value.innerText = text;
    statusRef.value.style.color = color;
    statusRef.value.style.textShadow = `0 0 10px ${color}`;
  }
}

function checkClick() {
  if (zoomState.active) return;
  
  raycaster.setFromCamera(mouse, camera);
  // Use recursive to hit frames/children, then find parent card
  const intersects = raycaster.intersectObjects(cards, true);
  
  if (intersects.length > 0) {
    const selected = findCardParent(intersects[0].object);
    if (selected) {
      triggerZoom(selected);
    }
  }
}

function findCardParent(object) {
  let target = object;
  // Traverse up until we find the card (which has userData.basePos)
  while (target) {
    if (target.userData && target.userData.basePos) {
      return target;
    }
    if (target === scene || target === albumGroup) return null;
    target = target.parent;
  }
  return null;
}

function triggerZoom(card) {
  if (zoomState.active) return;
  if (!card || !card.userData || !card.userData.basePos) return;
  
  zoomState.active = true;
  zoomState.targetCard = card;
  zoomState.startPos.copy(card.position);
  zoomState.startRot.copy(card.rotation);
  zoomState.startParent = card.parent;
  
  // Detach from group and attach to scene to move independently
  // Use Three.js attach to preserve world transform
  scene.attach(card);
  
  // Target position: In front of camera
  // Camera is at (0, 0, 35) looking at (0,0,0)
  // Let's put card at (0, 0, 28)
  const targetZ = camera.position.z - 7;
  
  // Animate using GSAP-like logic in render loop or simple interpolation
  // For simplicity, we'll handle lerp in animate
  
  updateStatus("查看详情 (点击/捏合关闭)", "#ff0");
}

function closeZoom() {
  if (!zoomState.active || !zoomState.targetCard) return;
  
  // The card will be moved back in animate loop logic
  // But we need a flag to say "returning" or just set active false and let it lerp back?
  // To simplify, we will just reset active.
  // However, we need to re-attach to group eventually.
  // For now, let's keep it simple:
  // We will manually put it back to group when it's close enough?
  // Or just reattach immediately and animate local position?
  // Reattaching immediately changes coordinate system.
  
  // Better strategy:
  // Just toggle state. Logic in animate will handle position.
  // When "fully back", we re-parent.
  
  zoomState.active = false;
  updateStatus("手势/鼠标控制就绪", "#0f0");
}

function createAlbum() {
  albumGroup = new THREE.Group();
  
  const loader = new THREE.TextureLoader();
  
  for (let i = 0; i < PHOTO_COUNT; i++) {
    const imageUrl = `https://picsum.photos/id/${i + 15}/800/600`;
    
    const geometry = new THREE.PlaneGeometry(CARD_WIDTH, CARD_HEIGHT);
    const material = new THREE.MeshBasicMaterial({ 
      side: THREE.DoubleSide,
      color: 0xffffff, // Full brightness but bloom threshold will handle glow
    });
    
    loader.load(imageUrl, (texture) => {
      texture.colorSpace = THREE.SRGBColorSpace; // Fix color accuracy
      material.map = texture;
      material.needsUpdate = true;
    });

    const card = new THREE.Mesh(geometry, material);
    
    // Layout: 3-Row Cylinder (Staggered)
    const rows = 3;
    const cardsPerRow = PHOTO_COUNT / rows;
    const row = Math.floor(i / cardsPerRow);
    const col = i % cardsPerRow;
    
    // Row Y positions: Top, Middle, Bottom
    const rowY = (1 - row) * 8; // 8, 0, -8
    
    // Angle with stagger offset for odd rows
    const rowOffset = (row % 2 === 0) ? 0 : (Math.PI / cardsPerRow);
    const angle = (col / cardsPerRow) * Math.PI * 2 + rowOffset;
    
    card.position.x = Math.cos(angle) * RADIUS;
    card.position.z = Math.sin(angle) * RADIUS;
    card.position.y = rowY;
    
    card.lookAt(0, rowY, 0); // Look at center of its row height
    card.rotateY(Math.PI); // Face out
    
    // Slight tilt for top/bottom rows to look at viewer level (optional, but better for "Sphere" feel)
    // Let's keep them vertical for "Cylinder" feel, clean and modern.
    
    // Border Frame (Glows)
    const frameGeo = new THREE.PlaneGeometry(CARD_WIDTH + 0.2, CARD_HEIGHT + 0.2);
    const frameMat = new THREE.MeshBasicMaterial({ color: 0x00ffff, side: THREE.BackSide });
    const frame = new THREE.Mesh(frameGeo, frameMat);
    frame.position.z = -0.02;
    card.add(frame);

    albumGroup.add(card);
    cards.push(card);
    
    // Store original local transform for reference (radius based)
    card.userData = {
      basePos: card.position.clone(),
      baseRot: card.rotation.clone(),
      index: i
    };
  }

  scene.add(albumGroup);
}

function addParticles() {
  const geometry = new THREE.BufferGeometry();
  const vertices = [];
  for (let i = 0; i < 2000; i++) {
    vertices.push(THREE.MathUtils.randFloatSpread(100));
    vertices.push(THREE.MathUtils.randFloatSpread(50)); // Less vertical spread
    vertices.push(THREE.MathUtils.randFloatSpread(100));
  }
  geometry.setAttribute('position', new THREE.Float32BufferAttribute(vertices, 3));
  const material = new THREE.PointsMaterial({ color: 0x00aaaa, size: 0.2, transparent: true, opacity: 0.5 });
  const stars = new THREE.Points(geometry, material);
  scene.add(stars);
}

async function initMediaPipe() {
  if (!window.Hands || !window.Camera) {
    throw new Error("MediaPipe scripts not loaded correctly.");
  }

  hands = new window.Hands({locateFile: (file) => {
      return `https://cdn.jsdelivr.net/npm/@mediapipe/hands/${file}`;
  }});
  
  hands.setOptions({
      maxNumHands: 1,
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
      processGestures(landmarks);
  } else {
      isInteracting = false;
      lastHandPos = null;
  }
}

function drawHandSkeleton(landmarks) {
  overlayCtx.save();
  const w = overlayCanvas.value.width;
  const h = overlayCanvas.value.height;
  
  // Draw connectors
  const connections = window.HAND_CONNECTIONS || window.Hands.HAND_CONNECTIONS;
  if (connections) {
      overlayCtx.strokeStyle = 'rgba(0, 255, 255, 0.5)';
      overlayCtx.lineWidth = 2;
      for (const conn of connections) {
          const p1 = landmarks[conn[0]];
          const p2 = landmarks[conn[1]];
          overlayCtx.beginPath();
          overlayCtx.moveTo(p1.x * w, p1.y * h);
          overlayCtx.lineTo(p2.x * w, p2.y * h);
          overlayCtx.stroke();
      }
  }
  
  // Draw Index Finger Tip (Cursor)
  const index = landmarks[8];
  overlayCtx.beginPath();
  overlayCtx.arc(index.x * w, index.y * h, 10, 0, 2 * Math.PI);
  overlayCtx.fillStyle = isPinching ? '#ffff00' : '#00ffff'; // Yellow if pinch, Cyan normal
  overlayCtx.fill();
  overlayCtx.strokeStyle = '#ffffff';
  overlayCtx.stroke();
  
  overlayCtx.restore();
}

function processGestures(landmarks) {
  const indexTip = landmarks[8];
  const thumbTip = landmarks[4];
  
  // Pinch Logic
  const pinchDist = Math.hypot(indexTip.x - thumbTip.x, indexTip.y - thumbTip.y);
  isPinching = pinchDist < 0.05;
  
  if (isPinching && !wasPinching) {
    // Pinch Start
    pinchStartTime = Date.now();
  } else if (!isPinching && wasPinching) {
    // Pinch End (Release)
    const duration = Date.now() - pinchStartTime;
    if (duration < 300) {
      // Short pinch = Click
      handleHandClick(indexTip);
    }
  }
  wasPinching = isPinching;

  // Movement Logic
  if (zoomState.active) return; // No rotation when zoomed
  
  if (lastHandPos) {
    const dx = indexTip.x - lastHandPos.x;
    
    if (isPinching) {
      // Drag with pinch
      mouseVelocity = -dx * 2.0; // Sensitivity
      currentRotationY += mouseVelocity;
      isInteracting = true;
      
      // Vertical Tilt
      const dy = indexTip.y - lastHandPos.y;
      targetRotationX += -dy * 2.0;
      targetRotationX = Math.max(-0.5, Math.min(0.5, targetRotationX));
    } else {
      // Hover / Swipe
      // Only affect if moving significantly
      if (Math.abs(dx) > 0.005) {
        const sensitivity = 2.5;
        const moveX = -dx * sensitivity;
        
        currentRotationY += moveX;
        mouseVelocity = moveX; // Store for momentum
        isInteracting = true;
      } else {
        isInteracting = false; // Let momentum take over
      }
      
      // Tilt based on hand height
      targetRotationX = (0.5 - indexTip.y) * 0.6;
    }
  }
  
  lastHandPos = { x: indexTip.x, y: indexTip.y };
}

function handleHandClick(screenPos) {
  if (zoomState.active) {
    closeZoom();
    return;
  }
  
  // Map normalized hand coords (0-1) to NDC (-1 to 1)
  mouse.x = (screenPos.x * 2) - 1;
  mouse.y = -(screenPos.y * 2) + 1; 
  
  raycaster.setFromCamera(mouse, camera);
  const intersects = raycaster.intersectObjects(cards, true);
  
  if (intersects.length > 0) {
    const selected = findCardParent(intersects[0].object);
    if (selected) {
      triggerZoom(selected);
    }
  }
}

function animate() {
  animationId = requestAnimationFrame(animate);
  
  // Physics & Rotation
  if (!zoomState.active) {
    if (!isInteracting && !isMouseDown) {
      mouseVelocity *= 0.95; // Damping
      if (Math.abs(mouseVelocity) < 0.0001) mouseVelocity = 0;
      currentRotationY += mouseVelocity;
      
      // Auto spin if idle
      if (mouseVelocity === 0) {
        currentRotationY += 0.0005;
      }
      
      // Spring back tilt
      targetRotationX = 0;
    }
    
    // Smooth tilt
    currentRotationX += (targetRotationX - currentRotationX) * 0.1;
    
    albumGroup.rotation.y = currentRotationY;
    albumGroup.rotation.x = currentRotationX;
  }

  // Zoom Animation
  if (zoomState.targetCard) {
    const card = zoomState.targetCard;
    
    if (zoomState.active) {
      // Target: In front of camera
      const targetPos = new THREE.Vector3(0, 0, camera.position.z - 8);
      // Target Rotation: Look at camera (approx)
      const targetRot = new THREE.Euler(0, 0, 0); // Face Z+? No, Camera looks at Z-.
      // Card default facing is... we rotated it PI to face out.
      // So facing camera means rotation (0,0,0)? 
      // Camera looks -Z. Card faces +Z (if rot=0). So they face each other.
      
      card.position.lerp(targetPos, 0.1);
      // Interpolate rotation manually (Quaternion slerp is better but Euler lerp works for small diffs)
      card.rotation.x += (0 - card.rotation.x) * 0.1;
      card.rotation.y += (0 - card.rotation.y) * 0.1;
      card.rotation.z += (0 - card.rotation.z) * 0.1;
      
      card.scale.lerp(new THREE.Vector3(1.5, 1.5, 1.5), 0.1);
    } else {
      // Returning to carousel
      // We need to know where it *should* be in world space if it were attached to group
      // But group is rotating!
      // This is tricky.
      // Easiest way: Reattach to group, set position/rot to stored base, let matrix update place it.
      // But that snaps. We want smooth.
      
      // Solution: Calculate target world pos
      const dummyObj = new THREE.Object3D();
      dummyObj.position.copy(card.userData.basePos);
      dummyObj.rotation.copy(card.userData.baseRot);
      // Transform by group matrix
      dummyObj.applyMatrix4(albumGroup.matrixWorld);
      
      card.position.lerp(dummyObj.position, 0.1);
      card.quaternion.slerp(dummyObj.quaternion, 0.1); // Use quaternion for world rotation match
      card.scale.lerp(new THREE.Vector3(1, 1, 1), 0.1);
      
      // If close enough, reattach
      if (card.position.distanceTo(dummyObj.position) < 0.1) {
         albumGroup.attach(card);
         card.position.copy(card.userData.basePos);
         card.rotation.copy(card.userData.baseRot);
         card.scale.set(1,1,1);
         zoomState.targetCard = null; // Done
      }
    }
  }

  if (composer) composer.render();
}

function onWindowResize() {
  if (!camera || !renderer) return;
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
  composer.setSize(window.innerWidth, window.innerHeight);
  if (overlayCanvas.value) {
    overlayCanvas.value.width = window.innerWidth;
    overlayCanvas.value.height = window.innerHeight;
  }
}
</script>

<style scoped>
.holo-album-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: #000;
  user-select: none;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 9999;
}

#canvas-container {
  width: 100%;
  height: 100%;
  cursor: grab;
}

#canvas-container:active {
  cursor: grabbing;
}

#overlay-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 10;
}

#ui-layer {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 20;
  color: #0ff;
  pointer-events: none;
}

#status {
  font-size: 24px;
  font-weight: bold;
  text-shadow: 0 0 10px #0ff;
  margin-bottom: 15px;
}

#controls {
  background: rgba(0, 20, 40, 0.8);
  padding: 15px;
  border: 1px solid rgba(0, 255, 255, 0.3);
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.8;
  backdrop-filter: blur(5px);
}

.back-btn {
  margin-top: 20px;
  background: rgba(0, 0, 0, 0.5);
  border: 1px solid #0ff;
  color: #0ff;
  padding: 8px 20px;
  cursor: pointer;
  pointer-events: auto;
  transition: all 0.3s;
  font-weight: bold;
  border-radius: 4px;
}

.back-btn:hover {
  background: #0ff;
  color: #000;
  box-shadow: 0 0 20px #0ff;
}

#loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #0ff;
  text-align: center;
  font-size: 20px;
  line-height: 2;
  z-index: 100;
  text-shadow: 0 0 10px #0ff;
}

.error {
  color: #ff4444;
  margin-top: 10px;
  font-size: 16px;
}
</style>
