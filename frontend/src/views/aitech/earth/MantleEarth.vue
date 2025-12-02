<template>
  <div class="mantle-earth-container">
    <AiTechBackButton />
    <div v-if="isLoading" id="loading">
      <div class="loading-spinner"></div>
      <div class="loading-text">正在初始化地质模型...</div>
    </div>

    <div id="ui-layer">
      <div class="header">
        <h1>🌍 地质动力学模拟</h1>
        <button class="back-btn" @click="$router.push('/aitech')">返回</button>
      </div>

      <div class="controls-panel">
        <div class="control-group">
          <label>地核揭秘（剥离地壳）</label>
          <input 
            type="range" 
            min="0" 
            max="1" 
            step="0.01" 
            v-model.number="revealFactor"
            @input="updateReveal"
          >
          <span class="value">{{ (revealFactor * 100).toFixed(0) }}%</span>
        </div>

        <div class="control-group">
          <label>时间回溯（百万年前）</label>
          <input 
            type="range" 
            min="0" 
            max="500" 
            step="1" 
            v-model.number="timeTravel"
            @input="updateTime"
          >
          <span class="value">{{ timeTravel }} 百万年前</span>
        </div>

        <div class="action-buttons">
          <button @click="toggleCollisionMode" :class="{ active: isCollisionMode }">
            {{ isCollisionMode ? '退出碰撞模拟' : '模拟板块碰撞' }}
          </button>
        </div>
        
        <div class="info-box" v-if="isCollisionMode">
          <p>观察汇聚边界如何挤压地壳向上隆起形成山脉（造山运动）。</p>
        </div>
        
        <div class="info-box" v-else>
          <p>使用“地核揭秘”查看地幔。<br>使用“时间回溯”观察大陆漂移。</p>
        </div>
      </div>
    </div>

    <div ref="canvasContainer" id="canvas-container"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue'

// Refs
const canvasContainer = ref(null);
const isLoading = ref(true);
const revealFactor = ref(0);
const timeTravel = ref(0);
const isCollisionMode = ref(false);

// Three.js Globals
let scene, camera, renderer, controls;
let earthGroup, mantleMesh, crustMesh, atmosphereMesh;
let collisionGroup, plate1, plate2;
let animationId = null;
let clock = new THREE.Clock();

// Shader Uniforms
const crustUniforms = {
  time: { value: 0 },
  reveal: { value: 0 },
  drift: { value: 0 },
  tex: { value: null },
  bump: { value: null }
};

const mantleUniforms = {
  time: { value: 0 },
  flowSpeed: { value: 1.0 }
};

// Shaders
const crustVertexShader = `
  varying vec2 vUv;
  varying vec3 vNormal;
  varying vec3 vPos;
  
  uniform float drift;
  
  // Simplex Noise for Drift Distortion
  vec3 mod289(vec3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
  vec2 mod289(vec2 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
  vec3 permute(vec3 x) { return mod289(((x*34.0)+1.0)*x); }
  float snoise(vec2 v) {
    const vec4 C = vec4(0.211324865405187, 0.366025403784439, -0.577350269189626, 0.024390243902439);
    vec2 i  = floor(v + dot(v, C.yy) );
    vec2 x0 = v - i + dot(i, C.xx);
    vec2 i1;
    i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    vec4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;
    i = mod289(i);
    vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 )) + i.x + vec3(0.0, i1.x, 1.0 ));
    vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
    m = m*m ;
    m = m*m ;
    vec3 x = 2.0 * fract(p * C.www) - 1.0;
    vec3 h = abs(x) - 0.5;
    vec3 ox = floor(x + 0.5);
    vec3 a0 = x - ox;
    m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );
    vec3 g;
    g.x  = a0.x  * x0.x  + h.x  * x0.y;
    g.yz = a0.yz * x12.xz + h.yz * x12.yw;
    return 130.0 * dot(m, g);
  }

  void main() {
    vUv = uv;
    vNormal = normalize(normalMatrix * normal);
    vPos = position;
    
    // Drift Simulation: Distort position based on "drift" (time)
    // We actually distort UVs usually, but vertex displacement looks more "tectonic"
    float noiseVal = snoise(position.xy * 0.5 + drift * 0.5);
    vec3 newPos = position + normal * (noiseVal * drift * 0.5); // Mild height change
    
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
  }
`;

const crustFragmentShader = `
  uniform sampler2D tex;
  uniform float reveal;
  uniform float drift;
  varying vec2 vUv;
  varying vec3 vNormal;
  varying vec3 vPos;

  // Noise for peeling edge
  float rand(vec2 co){
      return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
  }

  void main() {
    // Drift UV distortion
    vec2 distortedUv = vUv;
    distortedUv.x += sin(vUv.y * 10.0 + drift * 5.0) * 0.02 * drift;
    
    vec4 color = texture2D(tex, distortedUv);
    
    // Lighting
    vec3 lightDir = normalize(vec3(1.0, 1.0, 1.0));
    float diff = max(dot(vNormal, lightDir), 0.0);
    vec3 ambient = vec3(0.1);
    vec3 finalColor = color.rgb * (diff + ambient);
    
    // Peeling Logic (Discard based on reveal factor + noise)
    // We want to peel from one side or randomly?
    // Let's peel from right to left based on X position
    float noise = rand(vUv * 10.0) * 0.1;
    float peelThreshold = reveal * 2.0 - 1.0; // Map 0..1 to -1..1 range approx
    
    // Use dot product with view vector or just object space position
    // Let's use a "spherical wipe"
    float wipe = vPos.x / 10.0; // Assuming radius 10
    
    if (wipe + noise > 1.0 - reveal * 2.5) {
       discard;
    }
    
    // Add a glowing edge to the peel
    if (wipe + noise > 1.0 - reveal * 2.5 - 0.05) {
       finalColor = vec3(1.0, 0.5, 0.0); // Magma edge
    }

    gl_FragColor = vec4(finalColor, 1.0);
  }
`;

const mantleFragmentShader = `
  uniform float time;
  varying vec2 vUv;
  varying vec3 vNormal;

  // Simplex Noise 
  vec3 mod289(vec3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
  vec4 mod289(vec4 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
  vec4 permute(vec4 x) { return mod289(((x*34.0)+1.0)*x); }
  vec4 taylorInvSqrt(vec4 r) { return 1.79284291400159 - 0.85373472095314 * r; }
  float snoise(vec3 v) { 
    const vec2  C = vec2(1.0/6.0, 1.0/3.0) ;
    const vec4  D = vec4(0.0, 0.5, 1.0, 2.0);
    vec3 i  = floor(v + dot(v, C.yyy) );
    vec3 x0 = v - i + dot(i, C.xxx) ;
    vec3 g = step(x0.yzx, x0.xyz);
    vec3 l = 1.0 - g;
    vec3 i1 = min( g.xyz, l.zxy );
    vec3 i2 = max( g.xyz, l.zxy );
    vec3 x1 = x0 - i1 + C.xxx;
    vec3 x2 = x0 - i2 + C.yyy; 
    vec3 x3 = x0 - D.yyy;
    i = mod289(i); 
    vec4 p = permute( permute( permute( 
              i.z + vec4(0.0, i1.z, i2.z, 1.0 ))
            + i.y + vec4(0.0, i1.y, i2.y, 1.0 )) 
            + i.x + vec4(0.0, i1.x, i2.x, 1.0 ));
    float n_ = 0.142857142857; 
    vec3  ns = n_ * D.wyz - D.xzx;
    vec4 j = p - 49.0 * floor(p * ns.z * ns.z); 
    vec4 x_ = floor(j * ns.z);
    vec4 y_ = floor(j - 7.0 * x_ ); 
    vec4 x = x_ *ns.x + ns.yyyy;
    vec4 y = y_ *ns.x + ns.yyyy;
    vec4 h = 1.0 - abs(x) - abs(y);
    vec4 b0 = vec4( x.xy, y.xy );
    vec4 b1 = vec4( x.zw, y.zw );
    vec4 s0 = floor(b0)*2.0 + 1.0;
    vec4 s1 = floor(b1)*2.0 + 1.0;
    vec4 sh = -step(h, vec4(0.0));
    vec4 a0 = b0.xzyw + s0.xzyw*sh.xxyy ;
    vec4 a1 = b1.xzyw + s1.xzyw*sh.zzww ;
    vec3 p0 = vec3(a0.xy,h.x);
    vec3 p1 = vec3(a0.zw,h.y);
    vec3 p2 = vec3(a1.xy,h.z);
    vec3 p3 = vec3(a1.zw,h.w);
    vec4 norm = taylorInvSqrt(vec4(dot(p0,p0), dot(p1,p1), dot(p2, p2), dot(p3,p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;
    vec4 m = max(0.6 - vec4(dot(x0,x0), dot(x1,x1), dot(x2,x2), dot(x3,x3)), 0.0);
    m = m * m;
    return 42.0 * dot( m*m, vec4( dot(p0,x0), dot(p1,x1), dot(p2,x2), dot(p3,x3) ) );
  }

  void main() {
    // Moving noise for convection
    float n = snoise(vNormal * 3.0 + time * 0.2);
    float n2 = snoise(vNormal * 6.0 - time * 0.1);
    
    float heat = n * 0.5 + n2 * 0.5;
    
    // Colors: Dark Rock to Bright Magma
    vec3 cold = vec3(0.2, 0.0, 0.0);
    vec3 hot = vec3(1.0, 0.6, 0.1);
    vec3 veryHot = vec3(1.0, 1.0, 0.8);
    
    vec3 color = mix(cold, hot, heat + 0.5);
    if (heat > 0.3) color = mix(color, veryHot, (heat - 0.3) * 2.0);
    
    gl_FragColor = vec4(color, 1.0);
  }
`;

onMounted(() => {
  initThree();
  isLoading.value = false;
});

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId);
  if (renderer) renderer.dispose();
  window.removeEventListener('resize', onWindowResize);
});

function initThree() {
  const container = canvasContainer.value;
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0x000000);

  camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 1000);
  camera.position.set(0, 0, 35);

  renderer = new THREE.WebGLRenderer({ antialias: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  container.appendChild(renderer.domElement);

  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;

  // Lights
  const sunLight = new THREE.DirectionalLight(0xffffff, 1.5);
  sunLight.position.set(50, 30, 50);
  scene.add(sunLight);
  scene.add(new THREE.AmbientLight(0x404040));

  // Earth Group
  earthGroup = new THREE.Group();
  scene.add(earthGroup);

  const texLoader = new THREE.TextureLoader();

  // 1. Mantle (Inner Sphere)
  const mantleGeo = new THREE.SphereGeometry(9.8, 64, 64);
  const mantleMat = new THREE.ShaderMaterial({
    uniforms: mantleUniforms,
    vertexShader: `
      varying vec3 vNormal;
      void main() {
        vNormal = normal;
        gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
      }
    `,
    fragmentShader: mantleFragmentShader
  });
  mantleMesh = new THREE.Mesh(mantleGeo, mantleMat);
  earthGroup.add(mantleMesh);

  // 2. Crust (Outer Sphere)
  const crustGeo = new THREE.SphereGeometry(10, 64, 64);
  
  texLoader.load('https://raw.githubusercontent.com/mrdoob/three.js/master/examples/textures/planets/earth_atmos_2048.jpg', (tex) => {
    crustUniforms.tex.value = tex;
  });

  const crustMat = new THREE.ShaderMaterial({
    uniforms: crustUniforms,
    vertexShader: crustVertexShader,
    fragmentShader: crustFragmentShader,
    transparent: true,
    side: THREE.DoubleSide
  });
  crustMesh = new THREE.Mesh(crustGeo, crustMat);
  earthGroup.add(crustMesh);

  // 3. Atmosphere
  const atmosGeo = new THREE.SphereGeometry(11.0, 64, 64);
  const atmosMat = new THREE.MeshPhongMaterial({
    color: 0x00aaff,
    transparent: true,
    opacity: 0.1,
    side: THREE.BackSide,
    blending: THREE.AdditiveBlending
  });
  atmosphereMesh = new THREE.Mesh(atmosGeo, atmosMat);
  earthGroup.add(atmosphereMesh);

  // Stars
  addStars();

  // Collision Setup (Hidden initially)
  setupCollisionDemo();

  window.addEventListener('resize', onWindowResize);
  animate();
}

function addStars() {
  const r = 200;
  const starsGeo = new THREE.BufferGeometry();
  const starsVerts = [];
  for(let i=0; i<2000; i++) {
    starsVerts.push(
      THREE.MathUtils.randFloatSpread(r*2),
      THREE.MathUtils.randFloatSpread(r*2),
      THREE.MathUtils.randFloatSpread(r*2)
    );
  }
  starsGeo.setAttribute('position', new THREE.Float32BufferAttribute(starsVerts, 3));
  const stars = new THREE.Points(starsGeo, new THREE.PointsMaterial({color:0xffffff, size:0.5}));
  scene.add(stars);
}

function setupCollisionDemo() {
  collisionGroup = new THREE.Group();
  collisionGroup.visible = false;
  scene.add(collisionGroup);

  // Create two plates
  const plateGeo = new THREE.PlaneGeometry(10, 10, 30, 30);
  
  // Plate 1 (Oceanic - Darker, thinner)
  const mat1 = new THREE.MeshPhongMaterial({ color: 0x004488, side: THREE.DoubleSide, wireframe: true });
  plate1 = new THREE.Mesh(plateGeo, mat1);
  plate1.position.set(-5, 0, 0);
  plate1.rotation.x = -Math.PI / 2;
  collisionGroup.add(plate1);

  // Plate 2 (Continental - Lighter, thicker)
  const mat2 = new THREE.MeshPhongMaterial({ color: 0x886644, side: THREE.DoubleSide, wireframe: true });
  plate2 = new THREE.Mesh(plateGeo, mat2);
  plate2.position.set(5, 0, 0);
  plate2.rotation.x = -Math.PI / 2;
  collisionGroup.add(plate2);
}

function toggleCollisionMode() {
  isCollisionMode.value = !isCollisionMode.value;
  
  if (isCollisionMode.value) {
    // Switch to collision view
    earthGroup.visible = false;
    collisionGroup.visible = true;
    
    // Reset plates
    plate1.position.x = -6;
    plate2.position.x = 6;
    
    // Move camera close
    controls.minDistance = 5;
    camera.position.set(0, 10, 15);
    controls.target.set(0,0,0);
  } else {
    // Switch back to Earth view
    earthGroup.visible = true;
    collisionGroup.visible = false;
    camera.position.set(0, 0, 35);
    controls.target.set(0,0,0);
  }
}

function updateReveal() {
  crustUniforms.reveal.value = revealFactor.value;
}

function updateTime() {
  // Map 0-500 slider to 0-1 drift factor
  crustUniforms.drift.value = timeTravel.value / 500.0;
}

function animate() {
  animationId = requestAnimationFrame(animate);
  const delta = clock.getDelta();
  const elapsedTime = clock.getElapsedTime();

  // Update Shader Uniforms
  mantleUniforms.time.value = elapsedTime;
  crustUniforms.time.value = elapsedTime;

  // Rotate Earth if not in collision mode
  if (!isCollisionMode.value) {
    earthGroup.rotation.y += 0.001;
  } else {
    // Animate Collision
    if (plate1.position.x < -0.1) {
      plate1.position.x += 0.02;
      plate2.position.x -= 0.02;
      
      // Deform vertices near center (x=0)
      // Simple visual hack: Access geometry and modify vertices? 
      // Too expensive to do every frame for CPU.
      // Use Vertex Shader in real app. Here we just move them together.
    }
  }

  controls.update();
  renderer.render(scene, camera);
}

function onWindowResize() {
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
}
</script>

<style scoped>
.mantle-earth-container {
  width: 100%;
  height: 100vh;
  background: #000;
  overflow: hidden;
  position: relative;
}

#ui-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  padding: 20px;
  pointer-events: none;
  color: white;
  z-index: 10;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0;
  font-size: 2rem;
  text-shadow: 0 0 10px #00aaff;
}

.back-btn {
  pointer-events: auto;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.controls-panel {
  pointer-events: auto;
  background: rgba(0, 0, 0, 0.7);
  padding: 20px;
  border-radius: 12px;
  width: 300px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.control-group {
  margin-bottom: 20px;
}

.control-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 0.9rem;
  color: #aaa;
}

.control-group input {
  width: 100%;
  margin-bottom: 5px;
}

.control-group .value {
  font-size: 0.8rem;
  color: #00aaff;
}

.action-buttons button {
  width: 100%;
  padding: 10px;
  background: linear-gradient(45deg, #004488, #0088cc);
  border: none;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  transition: transform 0.2s;
}

.action-buttons button:hover {
  transform: scale(1.02);
}

.action-buttons button.active {
  background: linear-gradient(45deg, #882200, #cc4400);
}

.info-box {
  margin-top: 15px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 6px;
  font-size: 0.85rem;
  color: #ccc;
  line-height: 1.4;
}

#loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #00aaff;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(0, 170, 255, 0.3);
  border-top-color: #00aaff;
  border-radius: 50%;
  animation: spin 1s infinite linear;
  margin: 0 auto 10px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
