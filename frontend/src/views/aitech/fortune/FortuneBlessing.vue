<template>
  <div class="fortune-container">
    <AiTechBackButton />
    <!-- SVG Background Layer -->
    <svg class="svg-bg" width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <pattern id="cloud-pattern" x="0" y="0" width="100" height="100" patternUnits="userSpaceOnUse">
          <path d="M20,50 Q35,30 50,50 T80,50" fill="none" stroke="#d4af37" stroke-width="1" opacity="0.3"/>
        </pattern>
        <linearGradient id="gold-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#FFD700" />
          <stop offset="50%" stop-color="#FFA500" />
          <stop offset="100%" stop-color="#FFD700" />
        </linearGradient>
      </defs>
      <rect width="100%" height="100%" fill="#8B0000" />
      <rect width="100%" height="100%" fill="url(#cloud-pattern)" />
      
      <!-- Decorative Corners -->
      <g class="corner top-left" transform="translate(20,20)">
        <path d="M0,0 L50,0 L50,10 L10,10 L10,50 L0,50 Z" fill="#FFD700"/>
      </g>
      <g class="corner top-right" transform="translate(calc(100% - 70),20)">
        <path d="M50,0 L0,0 L0,10 L40,10 L40,50 L50,50 Z" fill="#FFD700"/>
      </g>
      <g class="corner bottom-left" transform="translate(20,calc(100% - 70))">
        <path d="M0,50 L50,50 L50,40 L10,40 L10,0 L0,0 Z" fill="#FFD700"/>
      </g>
      <g class="corner bottom-right" transform="translate(calc(100% - 70),calc(100% - 70))">
        <path d="M50,50 L0,50 L0,40 L40,40 L40,0 L50,0 Z" fill="#FFD700"/>
      </g>
    </svg>

    <!-- Three.js Canvas -->
    <div ref="canvasContainer" class="canvas-container"></div>

    <!-- Couplet UI Overlay -->
    <div class="ui-overlay">
      
      <!-- Top Horizontal Scroll (Hengpi) -->
      <div class="hengpi-container">
        <div class="hengpi-paper" ref="hengpiRef">
          <span class="hengpi-text">新春纳福</span>
        </div>
      </div>

      <!-- Left Scroll (Couplet) -->
      <div class="couplet-container left">
        <div class="couplet-paper" ref="leftScrollRef">
          <div class="couplet-text">{{ leftBlessing }}</div>
        </div>
      </div>

      <!-- Right Scroll (Couplet) -->
      <div class="couplet-container right">
        <div class="couplet-paper" ref="rightScrollRef">
          <div class="couplet-text">{{ rightBlessing }}</div>
        </div>
      </div>

      <!-- Center Fu (Interactive) -->
      <div class="center-fu" @click="newBlessing">
        <div class="fu-diamond">
          <span class="fu-char">福</span>
        </div>
        <div class="click-hint">点击接福</div>
      </div>

      <!-- Controls -->
      <div class="controls">
        <button class="back-btn" @click="$router.push('/aitech')">返回列表</button>
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

// GSAP will be loaded dynamically
let gsap: any;

// Data
const blessingsPairs = [
  { left: "岁岁平安", right: "年年有余" },
  { left: "万事如意", right: "恭喜发财" },
  { left: "吉星高照", right: "财源广进" },
  { left: "五福临门", right: "大吉大利" },
  { left: "身体健康", right: "心想事成" },
  { left: "生意兴隆", right: "日进斗金" }
];

const leftBlessing = ref("岁岁平安");
const rightBlessing = ref("年年有余");

// Refs
const hengpiRef = ref<HTMLElement | null>(null);
const leftScrollRef = ref<HTMLElement | null>(null);
const rightScrollRef = ref<HTMLElement | null>(null);

// Three.js variables
const canvasContainer = ref<HTMLElement | null>(null);
let scene: THREE.Scene;
let camera: THREE.PerspectiveCamera;
let renderer: THREE.WebGLRenderer;
let composer: EffectComposer;
let controls: OrbitControls;
let animationId: number;

// Objects
const wealthParticles: THREE.Sprite[] = [];
let dragonGroup: THREE.Group;
let dragonCurve: THREE.CatmullRomCurve3;
const dragonSegments: THREE.Mesh[] = [];

onMounted(async () => {
  // Load GSAP
  const gsapModule = await import('https://cdn.skypack.dev/gsap');
  gsap = gsapModule.gsap;

  initThree();
  createDragon();
  createDiverseWealthRain();
  animate();
  
  // Initial Animation
  animateScrolls();
  
  window.addEventListener('resize', onWindowResize);
});

onBeforeUnmount(() => {
  cancelAnimationFrame(animationId);
  window.removeEventListener('resize', onWindowResize);
  if (renderer) renderer.dispose();
});

function animateScrolls() {
  if (!gsap || !leftScrollRef.value || !rightScrollRef.value || !hengpiRef.value) return;

  // Reset
  gsap.set([leftScrollRef.value, rightScrollRef.value], { height: 0, opacity: 0 });
  gsap.set(hengpiRef.value, { width: 0, opacity: 0 });

  const tl = gsap.timeline();

  // 1. Hengpi unfolds horizontally
  tl.to(hengpiRef.value, {
    width: '300px',
    opacity: 1,
    duration: 1,
    ease: "power2.out"
  })
  // 2. Couplets unfold vertically
  .to([rightScrollRef.value, leftScrollRef.value], {
    height: '60vh',
    opacity: 1,
    duration: 1.5,
    ease: "power2.inOut",
    stagger: 0.2
  }, "-=0.5");
}

function newBlessing() {
  // Random pair
  const currentLeft = leftBlessing.value;
  let nextPair = blessingsPairs[Math.floor(Math.random() * blessingsPairs.length)];
  
  // Ensure change
  while(nextPair.left === currentLeft) {
    nextPair = blessingsPairs[Math.floor(Math.random() * blessingsPairs.length)];
  }

  // Animate out
  if (gsap) {
    const tl = gsap.timeline();
    tl.to([leftScrollRef.value, rightScrollRef.value, hengpiRef.value], {
      opacity: 0,
      duration: 0.3,
      onComplete: () => {
        // Update Text
        leftBlessing.value = nextPair.left;
        rightBlessing.value = nextPair.right;
        // Animate In
        animateScrolls();
      }
    });
  } else {
    leftBlessing.value = nextPair.left;
    rightBlessing.value = nextPair.right;
  }
  
  // Trigger explosion effect
  explodeWealth();
}

// --- SVG Data URIs ---
const svgYuanbao = `
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 60">
  <path d="M10,40 Q50,60 90,40 L85,25 Q50,45 15,25 Z" fill="#FFA500"/>
  <path d="M15,25 Q50,45 85,25 Q50,5 15,25" fill="#FFD700"/>
  <circle cx="50" cy="20" r="8" fill="#FFFFE0"/>
</svg>`;

const svgMoneyBag = `
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100">
  <path d="M20,40 C20,90 80,90 80,40 L70,20 L30,20 Z" fill="#CC0000"/>
  <path d="M30,20 L25,10 L75,10 L70,20" fill="#FFD700"/>
  <circle cx="50" cy="55" r="15" fill="#FFD700"/>
  <text x="50" y="62" font-size="20" font-weight="bold" fill="#CC0000" text-anchor="middle">¥</text>
</svg>`;

const svgCoin = `
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100">
  <circle cx="50" cy="50" r="45" fill="#FFD700" stroke="#DAA520" stroke-width="5"/>
  <rect x="35" y="35" width="30" height="30" fill="none" stroke="#DAA520" stroke-width="4"/>
  <text x="50" y="25" font-size="12" text-anchor="middle" fill="#DAA520" font-weight="bold">招财</text>
  <text x="50" y="85" font-size="12" text-anchor="middle" fill="#DAA520" font-weight="bold">进宝</text>
</svg>`;

function createTextureFromSVG(svgString: string): THREE.Texture {
  const img = new Image();
  img.src = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svgString);
  const texture = new THREE.Texture(img);
  img.onload = () => { texture.needsUpdate = true; };
  return texture;
}

function initThree() {
  if (!canvasContainer.value) return;

  scene = new THREE.Scene();
  scene.background = null; 

  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000);
  camera.position.set(0, 0, 40);

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.setPixelRatio(window.devicePixelRatio);
  canvasContainer.value.appendChild(renderer.domElement);

  // Lights
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.8);
  scene.add(ambientLight);

  const dirLight = new THREE.DirectionalLight(0xffd700, 2);
  dirLight.position.set(10, 20, 10);
  scene.add(dirLight);
  
  const pointLight = new THREE.PointLight(0xff0000, 2, 50);
  pointLight.position.set(0, 0, 20);
  scene.add(pointLight);

  // Post-processing
  const renderScene = new RenderPass(scene, camera);
  const bloomPass = new UnrealBloomPass(new THREE.Vector2(window.innerWidth, window.innerHeight), 1.5, 0.4, 0.85);
  bloomPass.threshold = 0.2;
  bloomPass.strength = 1.5;
  bloomPass.radius = 0.5;

  composer = new EffectComposer(renderer);
  composer.addPass(renderScene);
  composer.addPass(bloomPass);
  
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.enableZoom = false;
  controls.autoRotate = true;
  controls.autoRotateSpeed = 0.5;
}

function createDiverseWealthRain() {
  const texYuanbao = createTextureFromSVG(svgYuanbao);
  const texMoneyBag = createTextureFromSVG(svgMoneyBag);
  const texCoin = createTextureFromSVG(svgCoin);
  
  const textures = [texYuanbao, texMoneyBag, texCoin];
  const materials = textures.map(tex => new THREE.SpriteMaterial({ map: tex, color: 0xffffff }));

  for(let i=0; i<80; i++) {
    const mat = materials[Math.floor(Math.random() * materials.length)];
    const sprite = new THREE.Sprite(mat);
    
    // Randomize scale based on type
    const scale = 2 + Math.random() * 2;
    sprite.scale.set(scale, scale, 1);
    
    resetObj(sprite);
    scene.add(sprite);
    wealthParticles.push(sprite);
  }
}

function resetObj(obj: THREE.Object3D) {
  obj.position.x = (Math.random() - 0.5) * 60;
  obj.position.y = Math.random() * 50 + 20;
  obj.position.z = (Math.random() - 0.5) * 40;
  
  // Sprites always face camera, but we can rotate them in Z for fun
  if (obj instanceof THREE.Sprite) {
     obj.material.rotation = Math.random() * Math.PI * 2;
  }

  obj.userData = {
    speed: Math.random() * 0.2 + 0.1,
    rotSpeed: (Math.random() - 0.5) * 0.1
  };
}

function createDragon() {
  dragonGroup = new THREE.Group();
  scene.add(dragonGroup);
  
  const points = [];
  for (let i = 0; i < 15; i++) {
    points.push(new THREE.Vector3(
      Math.sin(i * 0.5) * 15,
      (i - 7) * 2,
      Math.cos(i * 0.5) * 10
    ));
  }
  dragonCurve = new THREE.CatmullRomCurve3(points);
  dragonCurve.closed = true;
  
  const bodyGeo = new THREE.SphereGeometry(1, 16, 16);
  const bodyMat = new THREE.MeshStandardMaterial({ 
    color: 0xff0000, 
    emissive: 0x550000,
    roughness: 0.4 
  });
  const goldMat = new THREE.MeshStandardMaterial({ color: 0xffd700, emissive: 0xaa4400 });

  for (let i = 0; i < 60; i++) {
    const isGold = i % 5 === 0;
    const mesh = new THREE.Mesh(bodyGeo, isGold ? goldMat : bodyMat);
    
    if (i === 0) mesh.scale.set(2.5, 2.5, 2.5); // Head
    else mesh.scale.setScalar(2 - (i / 60) * 1.5); // Taper tail
    
    dragonGroup.add(mesh);
    dragonSegments.push(mesh);
  }
}

function explodeWealth() {
  wealthParticles.forEach(p => {
    p.position.y = Math.random() * 30; // Reset to varying heights
    p.userData.speed = -0.8 - Math.random(); // Shoot up
  });
}

let time = 0;
function animate() {
  animationId = requestAnimationFrame(animate);
  time += 0.01;

  // Animate Wealth
  wealthParticles.forEach(p => {
    p.position.y -= p.userData.speed;
    if (p instanceof THREE.Sprite) {
      p.material.rotation += p.userData.rotSpeed;
    }
    
    // Gravity
    if (p.userData.speed < 0.1) {
        p.userData.speed += 0.02;
    }

    if (p.position.y < -30) {
      resetObj(p);
    }
  });

  // Animate Dragon
  if (dragonCurve) {
    const loopTime = 12; 
    const t = (time % loopTime) / loopTime;
    
    dragonSegments.forEach((seg, i) => {
      const offset = i * 0.012;
      let posT = t - offset;
      if (posT < 0) posT += 1;
      
      const point = dragonCurve.getPointAt(posT);
      const tangent = dragonCurve.getTangentAt(posT);
      
      seg.position.copy(point);
      seg.lookAt(point.clone().add(tangent));
      
      // Wiggle
      seg.position.y += Math.sin(time * 3 + i * 0.3) * 0.8;
    });
    
    dragonGroup.rotation.y = Math.sin(time * 0.2) * 0.5;
  }

  controls.update();
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

.fortune-container {
  width: 100%;
  height: 100vh;
  position: relative;
  overflow: hidden;
  background-color: #8B0000;
}

.svg-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.canvas-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.ui-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 10;
  pointer-events: none;
}

/* Hengpi (Top Horizontal Scroll) */
.hengpi-container {
  position: absolute;
  top: 10%;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  justify-content: center;
  width: 100%;
}

.hengpi-paper {
  background: #cc0000;
  border: 3px solid #FFD700;
  box-shadow: 0 10px 20px rgba(0,0,0,0.3);
  padding: 10px 0;
  width: 0; /* GSAP animates this */
  overflow: hidden;
  white-space: nowrap;
  display: flex;
  justify-content: center;
  align-items: center;
}

.hengpi-text {
  font-family: "Ma Shan Zheng", "Kaiti", serif;
  font-size: 3rem;
  color: #FFD700;
  font-weight: bold;
  letter-spacing: 0.5rem;
}

/* Couplets (Vertical Scrolls) */
.couplet-container {
  position: absolute;
  top: 25%;
  width: 80px; /* Fixed width */
  height: 60vh; /* Target height */
  display: flex;
  flex-direction: column;
  align-items: center;
}

.couplet-container.left {
  left: 15%;
}

.couplet-container.right {
  right: 15%;
}

.couplet-paper {
  background: #cc0000;
  border: 3px solid #FFD700;
  box-shadow: 0 10px 20px rgba(0,0,0,0.3);
  width: 100%;
  height: 0; /* GSAP animates this */
  overflow: hidden;
  padding: 20px 0;
  display: flex;
  justify-content: center;
}

.couplet-text {
  writing-mode: vertical-rl;
  text-orientation: upright;
  font-family: "ZCOOL XiaoWei", "Kaiti", serif;
  font-size: 3.5rem;
  color: #FFD700;
  font-weight: bold;
  letter-spacing: 1rem;
  line-height: 1;
}

/* Center Interactive Element */
.center-fu {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: auto;
  cursor: pointer;
  text-align: center;
  transition: transform 0.3s;
}

.center-fu:hover {
  transform: translate(-50%, -50%) scale(1.1);
}

.fu-diamond {
  width: 120px;
  height: 120px;
  background: #cc0000;
  border: 4px solid #FFD700;
  transform: rotate(45deg);
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 0 30px rgba(255, 215, 0, 0.6);
}

.fu-char {
  transform: rotate(-45deg);
  font-size: 5rem;
  color: #FFD700;
  font-family: "Ma Shan Zheng", serif;
}

.click-hint {
  margin-top: 40px; /* Push down below rotated diamond */
  color: rgba(255, 255, 255, 0.8);
  font-size: 1rem;
  background: rgba(0,0,0,0.3);
  padding: 5px 10px;
  border-radius: 10px;
}

.controls {
  position: absolute;
  bottom: 30px;
  width: 100%;
  text-align: center;
  pointer-events: auto;
}

.back-btn {
  background: transparent;
  border: 1px solid rgba(255, 215, 0, 0.5);
  color: #FFD700;
  padding: 8px 20px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 1rem;
}

.back-btn:hover {
  background: rgba(255, 215, 0, 0.2);
  border-color: #FFD700;
}
</style>
