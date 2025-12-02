<template>
  <div class="neon-smash-container" ref="container">
    <AiTechBackButton />
    <div class="loading-mask" v-if="isLoading">
      <div class="loading-content">
        <div class="spinner"></div>
        <p>正在加载物理引擎...</p>
      </div>
    </div>

    <div class="ui-layer">
      <div class="header">
        <h3>💥 霓虹粉碎室</h3>
        <div class="stats">
          <span>物体数量: {{ objectCount }}</span>
          <button class="reset-btn" @click="resetWorld">重置世界</button>
          <button class="back-btn" @click="$router.push('/aitech')">退出</button>
        </div>
      </div>
      <div class="instructions">
        <p>🖱️ 左键点击任意位置发射高能球体</p>
        <p>🖐️ 尽情破坏！享受物理碰撞的解压快感</p>
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

// Dynamic import for Cannon to avoid build issues if not installed
// We'll use a compatible build from a CDN or local if available.
// Using skypack or similar for ESM support.
const CANNON_URL = 'https://cdn.skypack.dev/cannon-es';

const container = ref<HTMLElement | null>(null);
const isLoading = ref(true);
const objectCount = ref(0);

// Three.js globals
let scene: THREE.Scene;
let camera: THREE.PerspectiveCamera;
let renderer: THREE.WebGLRenderer;
let controls: OrbitControls;
let composer: EffectComposer;
let raycaster: THREE.Raycaster;
let mouse: THREE.Vector2;

// Physics globals
let world: any;
let CANNON: any;
let lastCallTime: number;
const timeStep = 1 / 60;

// Objects sync arrays
const meshes: THREE.Mesh[] = [];
const bodies: any[] = [];

let animationId: number;

onMounted(async () => {
  try {
    // Load Cannon-es dynamically
    const cannonModule = await import(/* @vite-ignore */ CANNON_URL);
    CANNON = cannonModule;
    
    initThree();
    initPhysics();
    createLevel();
    animate();
    
    window.addEventListener('resize', onWindowResize);
    window.addEventListener('mousedown', onMouseDown);
    window.addEventListener('mousemove', onMouseMove);
    
    isLoading.value = false;
  } catch (e) {
    console.error("Failed to load physics engine:", e);
    alert("物理引擎加载失败，请检查网络连接");
  }
});

onBeforeUnmount(() => {
  cancelAnimationFrame(animationId);
  window.removeEventListener('resize', onWindowResize);
  window.removeEventListener('mousedown', onMouseDown);
  window.removeEventListener('mousemove', onMouseMove);
  
  if (renderer) {
    renderer.dispose();
  }
  // Cleanup physics world if needed
});

function initThree() {
  if (!container.value) return;

  scene = new THREE.Scene();
  scene.background = new THREE.Color(0x050505);
  scene.fog = new THREE.FogExp2(0x050505, 0.01);

  camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 1000);
  camera.position.set(0, 20, 40);
  camera.lookAt(0, 5, 0);

  renderer = new THREE.WebGLRenderer({ antialias: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.setPixelRatio(window.devicePixelRatio);
  renderer.shadowMap.enabled = true;
  container.value.appendChild(renderer.domElement);

  // Controls
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;

  // Post-processing (Bloom)
  const renderScene = new RenderPass(scene, camera);
  const bloomPass = new UnrealBloomPass(new THREE.Vector2(window.innerWidth, window.innerHeight), 1.5, 0.4, 0.85);
  bloomPass.threshold = 0.1;
  bloomPass.strength = 1.5;
  bloomPass.radius = 0.5;

  composer = new EffectComposer(renderer);
  composer.addPass(renderScene);
  composer.addPass(bloomPass);

  // Lights
  const ambientLight = new THREE.AmbientLight(0x404040);
  scene.add(ambientLight);

  const spotLight = new THREE.SpotLight(0xffffff, 500); // High intensity for PBR
  spotLight.position.set(20, 50, 20);
  spotLight.angle = Math.PI / 4;
  spotLight.penumbra = 0.5;
  spotLight.castShadow = true;
  spotLight.shadow.mapSize.width = 2048;
  spotLight.shadow.mapSize.height = 2048;
  scene.add(spotLight);
  
  // Colored lights for neon feel
  const pointLight1 = new THREE.PointLight(0xff00ff, 200, 50);
  pointLight1.position.set(-10, 10, 10);
  scene.add(pointLight1);
  
  const pointLight2 = new THREE.PointLight(0x00ffff, 200, 50);
  pointLight2.position.set(10, 10, -10);
  scene.add(pointLight2);

  raycaster = new THREE.Raycaster();
  mouse = new THREE.Vector2();
}

function initPhysics() {
  world = new CANNON.World();
  world.gravity.set(0, -9.82, 0);
  world.broadphase = new CANNON.NaiveBroadphase();
  world.solver.iterations = 10;

  // Ground material
  const groundMat = new CANNON.Material();
  const boxMat = new CANNON.Material();
  const boxGroundContact = new CANNON.ContactMaterial(groundMat, boxMat, {
    friction: 0.5,
    restitution: 0.3
  });
  world.addContactMaterial(boxGroundContact);

  // Ground Body
  const groundShape = new CANNON.Plane();
  const groundBody = new CANNON.Body({ mass: 0, material: groundMat });
  groundBody.addShape(groundShape);
  groundBody.quaternion.setFromAxisAngle(new CANNON.Vec3(1, 0, 0), -Math.PI / 2);
  world.addBody(groundBody);

  // Ground Mesh (Visual)
  const groundGeo = new THREE.PlaneGeometry(100, 100);
  const groundMeshMat = new THREE.MeshStandardMaterial({ 
    color: 0x111111, 
    roughness: 0.8, 
    metalness: 0.2 
  });
  const groundMesh = new THREE.Mesh(groundGeo, groundMeshMat);
  groundMesh.rotation.x = -Math.PI / 2;
  groundMesh.receiveShadow = true;
  scene.add(groundMesh);
  
  // Grid Helper
  const grid = new THREE.GridHelper(100, 50, 0x444444, 0x222222);
  scene.add(grid);
}

function createLevel() {
  // Clear existing
  // (Ideally we should remove bodies and meshes, but for now we just add)
  
  // Create a wall of cubes
  const boxSize = 1;
  const startX = -5;
  const startY = 0.5; // Half height
  const startZ = -10;
  
  const colors = [0xff0055, 0x00ff55, 0x5500ff, 0xffff00, 0x00ffff];

  for (let i = 0; i < 10; i++) { // Rows
    for (let j = 0; j < 10; j++) { // Cols
      const x = startX + j * boxSize + (Math.random() * 0.05); // Slight jitter
      const y = startY + i * boxSize;
      const z = startZ;
      
      const color = colors[Math.floor(Math.random() * colors.length)];
      createBox(x, y, z, boxSize, boxSize, boxSize, 1, color);
    }
  }
  
  // Create a pyramid
  const pyStartZ = 5;
  const levels = 6;
  for(let i=0; i<levels; i++) {
     const count = levels - i;
     for(let j=0; j<count; j++) {
         // Center the row
         const x = (j - count/2 + 0.5) * (boxSize + 0.1);
         const y = startY + i * boxSize;
         const z = pyStartZ;
         createBox(x, y, z, boxSize, boxSize, boxSize, 1, 0xffaa00);
     }
  }
}

function createBox(x: number, y: number, z: number, w: number, h: number, d: number, mass: number, color: number) {
  // Physics
  const shape = new CANNON.Box(new CANNON.Vec3(w/2, h/2, d/2));
  const body = new CANNON.Body({ mass: mass });
  body.addShape(shape);
  body.position.set(x, y, z);
  world.addBody(body);
  bodies.push(body);

  // Visual
  const geo = new THREE.BoxGeometry(w, h, d);
  const mat = new THREE.MeshStandardMaterial({
    color: color,
    roughness: 0.4,
    metalness: 0.1,
    emissive: color,
    emissiveIntensity: 0.5
  });
  const mesh = new THREE.Mesh(geo, mat);
  mesh.castShadow = true;
  mesh.receiveShadow = true;
  scene.add(mesh);
  meshes.push(mesh);
  
  objectCount.value = bodies.length;
}

function createBall(x: number, y: number, z: number, dir: THREE.Vector3) {
  const radius = 0.8;
  const mass = 50; // Heavy ball
  
  // Physics
  const shape = new CANNON.Sphere(radius);
  const body = new CANNON.Body({ mass: mass });
  body.addShape(shape);
  body.position.set(x, y, z);
  
  // Shoot velocity
  const shootSpeed = 50;
  body.velocity.set(dir.x * shootSpeed, dir.y * shootSpeed, dir.z * shootSpeed);
  
  world.addBody(body);
  bodies.push(body);

  // Visual
  const geo = new THREE.SphereGeometry(radius, 32, 32);
  const mat = new THREE.MeshStandardMaterial({
    color: 0xffffff,
    emissive: 0xffffff,
    emissiveIntensity: 10, // Very bright
    roughness: 0,
    metalness: 0
  });
  const mesh = new THREE.Mesh(geo, mat);
  mesh.castShadow = true;
  scene.add(mesh);
  meshes.push(mesh);
  
  objectCount.value = bodies.length;
}

function resetWorld() {
  // Remove dynamic bodies
  for (let i = bodies.length - 1; i >= 0; i--) {
    world.removeBody(bodies[i]);
    scene.remove(meshes[i]);
  }
  bodies.length = 0;
  meshes.length = 0;
  
  createLevel();
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

function onMouseDown(event: MouseEvent) {
  if (event.button !== 0) return; // Only left click
  
  // Calculate shoot direction from camera to mouse pos in world
  raycaster.setFromCamera(mouse, camera);
  
  // Shoot from camera position
  const shootDir = raycaster.ray.direction.clone();
  const origin = raycaster.ray.origin.clone();
  
  // Move origin slightly forward so we don't clip camera? 
  // Actually just shooting from camera pos is fine, but maybe start a bit in front
  origin.add(shootDir.multiplyScalar(2));
  
  createBall(origin.x, origin.y, origin.z, shootDir.normalize());
}

function animate() {
  animationId = requestAnimationFrame(animate);

  const now = performance.now() / 1000;
  if (!lastCallTime) {
    world.step(timeStep);
  } else {
    const dt = now - lastCallTime;
    world.step(timeStep, dt, 3);
  }
  lastCallTime = now;

  // Sync visual meshes with physics bodies
  for (let i = 0; i < meshes.length; i++) {
    const mesh = meshes[i];
    const body = bodies[i];
    
    mesh.position.copy(body.position as any);
    mesh.quaternion.copy(body.quaternion as any);
    
    // Optional: Remove objects falling too far
    if (body.position.y < -20) {
       // Removing from array during loop is tricky, let's just stop updating or reset
       body.velocity.set(0,0,0);
       body.angularVelocity.set(0,0,0);
       // Ideally remove
    }
  }

  controls.update();
  composer.render();
}
</script>

<style scoped>
.neon-smash-container {
  width: 100%;
  height: 100vh;
  background-color: #000;
  overflow: hidden;
  position: relative;
  user-select: none;
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
  border-top: 4px solid #ff00ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.ui-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
  pointer-events: none;
  z-index: 100;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  text-shadow: 0 0 10px #ff00ff;
}

.header h3 {
  margin: 0;
  color: #fff;
  font-size: 1.8rem;
  font-weight: 800;
  letter-spacing: 2px;
}

.stats {
  pointer-events: auto;
  display: flex;
  gap: 15px;
  align-items: center;
}

.stats span {
  color: #00ffff;
  font-family: monospace;
  font-size: 1.2rem;
}

.reset-btn, .back-btn {
  background: rgba(255, 0, 255, 0.2);
  border: 1px solid #ff00ff;
  color: #fff;
  padding: 8px 20px;
  cursor: pointer;
  transition: all 0.2s;
  text-transform: uppercase;
  font-weight: bold;
}

.reset-btn:hover, .back-btn:hover {
  background: #ff00ff;
  box-shadow: 0 0 15px #ff00ff;
}

.instructions {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  color: rgba(255, 255, 255, 0.7);
  background: rgba(0, 0, 0, 0.5);
  padding: 15px 30px;
  border-radius: 30px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.instructions p {
  margin: 5px 0;
  font-size: 1rem;
}
</style>
