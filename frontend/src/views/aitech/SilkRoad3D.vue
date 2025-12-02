<template>
  <div class="silk-road-container" ref="container">
    <AiTechBackButton />
    <!-- Loading Screen -->
    <transition name="fade">
      <div v-if="loading" class="loading-overlay">
        <div class="loading-content">
          <div class="compass-spinner"></div>
          <div class="loading-text">
            <h2>重返丝路</h2>
            <p>正在加载历史数据与地形纹理...</p>
            <div class="progress-bar">
              <div class="progress" :style="{ width: loadProgress + '%' }"></div>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- 3D Canvas -->
    <div ref="canvasContainer" class="canvas-container"></div>

    <!-- UI Overlay -->
    <div class="ui-layer" :class="{ 'ui-hidden': isExploring }">
      <!-- Progress Timeline -->
      <div class="timeline-bar">
        <div class="timeline-progress" :style="{ height: scrollProgress * 100 + '%' }"></div>
        <div class="timeline-points">
          <div v-for="(chapter, index) in chapters" :key="index" 
               class="point" 
               :class="{ active: currentChapterIndex >= index }"
               @click="scrollToChapter(index)">
            <span class="point-tooltip">{{ chapter.title }}</span>
          </div>
        </div>
      </div>

      <!-- Main Content Area -->
      <div class="content-wrapper">
        <transition name="slide-up" mode="out-in">
          <div :key="currentChapterIndex" class="chapter-card" v-if="currentChapter">
            <div class="chapter-header">
              <span class="chapter-number">NO.0{{ currentChapterIndex + 1 }}</span>
              <h1 class="chapter-title">{{ currentChapter.title }}</h1>
              <div class="chapter-meta">
                <span class="meta-item"><i class="el-icon-location"></i> {{ currentChapter.location }}</span>
                <span class="meta-item"><i class="el-icon-time"></i> {{ currentChapter.era }}</span>
              </div>
            </div>
            
            <div class="chapter-body">
              <p class="main-text">{{ currentChapter.description }}</p>
              
              <div class="historical-facts">
                <h3><span class="icon">📜</span> 历史百科</h3>
                <ul>
                  <li v-for="(fact, fIndex) in currentChapter.facts" :key="fIndex">
                    {{ fact }}
                  </li>
                </ul>
              </div>

              <div class="goods-section" v-if="currentChapter.goods">
                <h3><span class="icon">📦</span> 关键贸易品</h3>
                <div class="goods-grid">
                  <div v-for="(good, gIndex) in currentChapter.goods" :key="gIndex" 
                       class="good-item" @click="inspectGood(good)">
                    <div class="good-icon">{{ good.icon }}</div>
                    <span class="good-name">{{ good.name }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- Interaction Hint -->
      <div class="scroll-hint" v-if="scrollProgress < 0.05">
        <div class="mouse"></div>
        <span>滚动探索</span>
      </div>
    </div>

    <!-- Detail Modal -->
    <transition name="zoom">
      <div v-if="inspectedGood" class="detail-modal-overlay" @click="closeInspect">
        <div class="detail-modal" @click.stop>
          <div class="detail-header">
            <h2>{{ inspectedGood.name }}</h2>
            <button class="close-btn" @click="closeInspect">×</button>
          </div>
          <div class="detail-content">
            <div class="detail-icon-large">{{ inspectedGood.icon }}</div>
            <p>{{ inspectedGood.description }}</p>
            <div class="detail-context">
              <strong>历史价值：</strong> {{ inspectedGood.value }}
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import * as THREE from 'three'
import { createNoise2D } from 'simplex-noise'
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue'

// --- Types & Data ---
interface TradeGood {
  name: string
  icon: string
  description: string
  value: string
}

interface Chapter {
  title: string
  location: string
  era: string
  description: string
  facts: string[]
  goods?: TradeGood[]
  skyColorTop: number
  skyColorBottom: number
  fogColor: number
  lightColor: number
  ambientIntensity: number
}

const chapters: Chapter[] = [
  {
    title: "长安：丝路起点",
    location: "中国 · 西安 (Chang'an)",
    era: "公元前138年 (汉)",
    description: "“无数铃声遥过碛，应驮白练到安西。” 长安不仅是帝国的中心，更是世界贸易的东方心脏。驼队在此集结，背负着丝绸、瓷器与茶叶，准备踏上吉凶未卜的万里征途。",
    facts: [
      "张骞两次出使西域，凿空了这条东西方交流的通道。",
      "长安城拥有百万人口，是当时世界上最大的国际都市。",
      "西市是主要的国际贸易市场，汇聚了来自中亚、波斯的胡商。"
    ],
    goods: [
      { name: "丝绸", icon: "🧣", description: "中国的名片。在罗马，丝绸通过层层转手，价格等同于黄金，被视为贵族身份的象征。", value: "硬通货，可直接作为货币使用。" },
      { name: "瓷器", icon: "🏺", description: "精美的青瓷与白瓷，展现了东方高超的工艺技术，深受西方贵族喜爱。", value: "易碎但极其珍贵，通常通过海路或精心包装陆运。" },
      { name: "茶叶", icon: "🍵", description: "不仅是饮品，更是游牧民族补充维生素的重要来源。", value: "通过茶马古道与丝绸之路向西传播。" }
    ],
    skyColorTop: 0x4fa6eb, // Clear Day Blue
    skyColorBottom: 0xc9e9f6,
    fogColor: 0xc9e9f6,
    lightColor: 0xfffee0,
    ambientIntensity: 0.8
  },
  {
    title: "河西走廊：帝国锁钥",
    location: "甘肃 · 嘉峪关 (Hexi Corridor)",
    era: "汉唐时期",
    description: "“羌笛何须怨杨柳，春风不度玉门关。” 穿过黄土高原，便进入了狭长的河西走廊。这里是汉帝国伸向西域的手臂，长城在此延伸，烽燧相望。",
    facts: [
      "嘉峪关被称为“天下第一雄关”，是明长城的西端起点。",
      "河西四郡（武威、张掖、酒泉、敦煌）保障了丝路的畅通。",
      "这里是农耕文明与游牧文明的交锋与融合地带。"
    ],
    goods: [
      { name: "玉石", icon: "📿", description: "来自和田的温润美玉，经由走廊输入中原，是礼制与君子的象征。", value: "价值连城的文化载体。" },
      { name: "皮毛", icon: "🧥", description: "游牧民族的特产，用于御寒与制作皮革制品。", value: "重要的生活物资。" }
    ],
    skyColorTop: 0x87ceeb, // Lighter Blue
    skyColorBottom: 0xe0c090, // Dusty
    fogColor: 0xe0c090,
    lightColor: 0xffe4b5,
    ambientIntensity: 0.7
  },
  {
    title: "敦煌：飞天与佛国",
    location: "甘肃 · 敦煌 (Dunhuang)",
    era: "公元4世纪-14世纪",
    description: "河西走廊的尽头，也是西出阳关的最后一站。莫高窟的壁画记录了多元文明的融合，商旅在此祈求平安，补给物资，为穿越即将到来的死亡沙海做准备。",
    facts: [
      "敦煌扼守阳关与玉门关，是丝路咽喉。",
      "莫高窟藏经洞保存了数万件珍贵文物，包括多国语言的经卷。",
      "反弹琵琶的飞天形象成为了大唐盛世的文化符号。"
    ],
    goods: [
      { name: "佛经", icon: "📜", description: "精神食粮。随着商队的脚步，宗教与哲学思想在东西方之间剧烈碰撞与融合。", value: "无价之宝，承载文明与信仰。" },
      { name: "纸张", icon: "📄", description: "造纸术经由丝绸之路传向阿拉伯世界，彻底改变了人类知识传播的方式。", value: "技术输出，推动了全球文明进程。" }
    ],
    skyColorTop: 0x8a6e4b, // Golden hour
    skyColorBottom: 0xeecfa1,
    fogColor: 0xeecfa1,
    lightColor: 0xffd700,
    ambientIntensity: 0.6
  },
  {
    title: "火洲：高昌古国",
    location: "新疆 · 吐鲁番 (Turpan)",
    era: "高昌回鹘时期",
    description: "火焰山下，葡萄沟旁。这里有着独特的坎儿井灌溉系统，使得沙漠中的绿洲瓜果飘香。高昌故城曾是西域最大的国际都会之一，玄奘大师曾在此讲经。",
    facts: [
      "火焰山因《西游记》而闻名，地表温度可达80℃。",
      "坎儿井是伟大的地下水利工程，被称为“地下长城”。",
      "盛产葡萄干与葡萄酒，是丝路上甜蜜的补给站。"
    ],
    goods: [
      { name: "葡萄酒", icon: "🍷", description: "“葡萄美酒夜光杯”，西域的葡萄酒传入中原，成为了诗人们的最爱。", value: "高档消费品。" },
      { name: "棉花", icon: "☁️", description: "棉花种植技术经由丝路传入中国，改变了中国人的衣着习惯。", value: "重要的经济作物。" }
    ],
    skyColorTop: 0xff7f50, // Coral Red (Heat)
    skyColorBottom: 0xffd700,
    fogColor: 0xffdab9,
    lightColor: 0xff4500,
    ambientIntensity: 0.65
  },
  {
    title: "死亡之海：塔克拉玛干",
    location: "新疆 · 塔里木盆地",
    era: "艰险路段",
    description: "维吾尔语意为“进去出不来”。流沙移动，水源匮乏，温差极大。这是对商队意志的终极考验。只有最富有经验的向导和最耐劳的骆驼才能穿越这片绝境。",
    facts: [
      "商队通常沿着盆地边缘的绿洲（如喀什、和田）行进，也就是著名的南道与北道。",
      "这里埋藏着楼兰、尼雅等失落的古国。",
      "依靠“沙漠之舟”双峰骆驼，它们能负重200公斤，耐饥渴数日。"
    ],
    goods: [
      { name: "骆驼", icon: "🐫", description: "它们不是商品，但是最宝贵的资产。没有它们，丝路就不复存在。", value: "生命之舟。" }
    ],
    skyColorTop: 0xcd5c5c, // Sunset Red/Orange
    skyColorBottom: 0xff8c00,
    fogColor: 0xcd853f, // Sandstorm
    lightColor: 0xff4500,
    ambientIntensity: 0.4
  },
  {
    title: "万邦通衢：喀什",
    location: "新疆 · 喀什 (Kashgar)",
    era: "丝路枢纽",
    description: "跨越沙漠后的休憩之地，也是天山南路与北路的汇合点。这里的大巴扎汇聚了东西方的奇珍异宝，不同语言、不同肤色的人们在此交易。",
    facts: [
      "喀什是丝绸之路进入帕米尔高原前的最后一座重镇。",
      "拥有两千多年的历史，是东西方文化交汇的十字路口。",
      "香妃墓等建筑展现了独特的伊斯兰建筑风格。"
    ],
    goods: [
      { name: "地毯", icon: "🧶", description: "精美的波斯风格地毯，图案繁复，工艺精湛。", value: "耐用的奢侈品。" },
      { name: "干果", icon: "🌰", description: "巴旦木、核桃、无花果，耐储存的高热量食物，适合长途携带。", value: "重要的补给。" }
    ],
    skyColorTop: 0x191970, // Midnight Blue (Early morning/Late eve)
    skyColorBottom: 0x483d8b,
    fogColor: 0x2f4f4f,
    lightColor: 0x9370db,
    ambientIntensity: 0.5
  },
  {
    title: "中亚明珠：撒马尔罕",
    location: "乌兹别克斯坦 (Samarkand)",
    era: "粟特文明",
    description: "翻越葱岭（帕米尔高原），便进入了中亚腹地。撒马尔罕是粟特人的家园，被誉为“传说之城”。蓝色的清真寺圆顶在阳光下熠熠生辉。",
    facts: [
      "粟特人被称为“东方的腓尼基人”，垄断了丝路贸易的中间环节。",
      "这里引进了中国的造纸术，并将其改进后传播到西方。",
      "帖木儿帝国的首都，建筑艺术达到顶峰。"
    ],
    goods: [
      { name: "玻璃", icon: "🔮", description: "萨珊玻璃工艺精湛，晶莹剔透，深受东方喜爱。", value: "当时的奢侈品。" },
      { name: "金银器", icon: "🏆", description: "粟特金银器造型独特，融合了希腊、波斯与中国的风格。", value: "贵族收藏。" }
    ],
    skyColorTop: 0x00bfff, // Deep Sky Blue
    skyColorBottom: 0xffd700, // Gold (Architecture)
    fogColor: 0xe0ffff,
    lightColor: 0xffffff,
    ambientIntensity: 0.7
  },
  {
    title: "条条大路通罗马",
    location: "意大利 · 罗马 (Rome)",
    era: "罗马帝国时期",
    description: "丝路的终点。来自东方的丝绸在这里被穿在贵族的身上，成为身份的象征。罗马的黄金则源源不断地流向东方。文明的交流在此完成了闭环。",
    facts: [
      "罗马人对丝绸的狂热导致了大量的黄金外流，甚至引起了元老院的担忧。",
      "罗马的玻璃、橄榄油和葡萄酒也经由丝路回流向东方。",
      "大秦（中国古称罗马）在汉朝史书中被描述为富庶而文明的国度。"
    ],
    goods: [
      { name: "金币", icon: "💰", description: "东罗马金币、萨珊银币在丝路沿线大量出土。", value: "国际通用货币。" },
      { name: "香料", icon: "🌿", description: "虽然产自东南亚/印度，但经由罗马商路进入欧洲，价如黄金。", value: "调味与防腐。" }
    ],
    skyColorTop: 0x4169e1, // Royal Blue
    skyColorBottom: 0xffffff, // White marble
    fogColor: 0xf0f8ff,
    lightColor: 0xffffff,
    ambientIntensity: 0.9
  }
]

// --- Vue State ---
const container = ref<HTMLElement | null>(null)
const canvasContainer = ref<HTMLElement | null>(null)
const loading = ref(true)
const loadProgress = ref(0)
const scrollProgress = ref(0)
const targetScrollProgress = ref(0)
const currentChapterIndex = ref(0)
const currentChapter = computed(() => chapters[currentChapterIndex.value])
const isExploring = ref(false) // Could be used for a "Free Roam" mode later
const inspectedGood = ref<TradeGood | null>(null)

// --- Three.js Variables ---
let scene: THREE.Scene
let camera: THREE.PerspectiveCamera
let renderer: THREE.WebGLRenderer
let clock: THREE.Clock
let animationId: number

// Objects
let terrain: THREE.Mesh
let particleSystem: THREE.Points
let starsSystem: THREE.Points
let caravan: THREE.Group
let sunLight: THREE.DirectionalLight
let ambientLight: THREE.AmbientLight

// Noise for terrain
const noise2D = createNoise2D()

// --- Initialization ---
onMounted(async () => {
  // Fake Loading
  const interval = setInterval(() => {
    loadProgress.value += Math.random() * 15
    if (loadProgress.value >= 100) {
      loadProgress.value = 100
      clearInterval(interval)
      setTimeout(() => {
        loading.value = false
        initThree()
      }, 500)
    }
  }, 200)

  window.addEventListener('wheel', handleScroll)
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(animationId)
  window.removeEventListener('wheel', handleScroll)
  window.removeEventListener('resize', handleResize)
  if (renderer) renderer.dispose()
})

// --- Three.js Core ---
function initThree() {
  if (!canvasContainer.value) return

  // 1. Scene
  scene = new THREE.Scene()
  scene.fog = new THREE.FogExp2(chapters[0].fogColor, 0.02)
  scene.background = new THREE.Color(chapters[0].skyColorBottom)

  // 2. Camera
  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000)
  camera.position.set(0, 5, 10)

  // 3. Renderer
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false })
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  canvasContainer.value.appendChild(renderer.domElement)

  // 4. Lights
  ambientLight = new THREE.AmbientLight(0xffffff, chapters[0].ambientIntensity)
  scene.add(ambientLight)

  sunLight = new THREE.DirectionalLight(chapters[0].lightColor, 1.5)
  sunLight.position.set(50, 50, 50)
  sunLight.castShadow = true
  sunLight.shadow.mapSize.width = 2048
  sunLight.shadow.mapSize.height = 2048
  sunLight.shadow.bias = -0.0001
  scene.add(sunLight)

  // 5. Objects
  createInfiniteTerrain()
  createParticles() // Sand/Dust
  createStars()
  createCaravan() // Detailed Camels
  createRoadsideObjects() // Rocks, Ruins

  // 6. Start Loop
  clock = new THREE.Clock()
  animate()
}

// --- World Generation ---

function createInfiniteTerrain() {
  // Create a long strip of terrain
  const width = 200
  const length = 1600 // Increased for 8 chapters
  const segmentsW = 100
  const segmentsL = 800

  const geometry = new THREE.PlaneGeometry(width, length, segmentsW, segmentsL)
  const pos = geometry.attributes.position

  for (let i = 0; i < pos.count; i++) {
    const x = pos.getX(i)
    const y = pos.getY(i) // This corresponds to world Z (before rotation)
    
    // Multi-octave noise for dunes/mountains
    let z = 0
    
    // Base Dunes
    z += noise2D(x * 0.02, y * 0.02) * 5
    // Small details
    z += noise2D(x * 0.1, y * 0.1) * 1
    
    // Path flattening (create a road in the middle)
    const distFromCenter = Math.abs(x)
    const roadWidth = 8
    if (distFromCenter < roadWidth) {
      z *= (distFromCenter / roadWidth) // Flatten near 0
    }

    // Mountains on the sides
    if (distFromCenter > 20) {
      z += Math.pow((distFromCenter - 20) * 0.1, 2)
    }

    pos.setZ(i, z)
  }

  geometry.computeVertexNormals()
  
  const material = new THREE.MeshStandardMaterial({
    color: 0xd2b48c,
    roughness: 0.9,
    metalness: 0.1,
    flatShading: true, // Low poly style but high detail geometry
  })

  terrain = new THREE.Mesh(geometry, material)
  terrain.rotation.x = -Math.PI / 2;
  terrain.position.z = -length / 2 + 50; // Start point
  terrain.receiveShadow = true;
  scene.add(terrain);
}

function createCaravan() {
  caravan = new THREE.Group()
  
  // Create 3 Detailed Camels and Merchants
  for (let i = 0; i < 3; i++) {
    const camel = buildDetailedCamel()
    camel.position.z = -i * 8
    camel.position.x = (Math.random() - 0.5) * 3
    
    // Randomize walk cycle phase
    camel.userData.phase = Math.random() * Math.PI * 2
    
    caravan.add(camel)

    // Add a merchant walking next to the camel
    const merchant = buildMerchant()
    merchant.position.z = -i * 8 + 2 // Slightly behind or beside
    merchant.position.x = camel.position.x + (camel.position.x > 0 ? -2 : 2) // Opposite side
    merchant.userData.phase = camel.userData.phase // Sync with camel roughly
    caravan.add(merchant)
  }
  
  scene.add(caravan)
}

function buildMerchant() {
  const group = new THREE.Group()
  
  // Skin colors: Asian, Central Asian/Middle Eastern, European/Roman
  const skinColors = [0xffe0bd, 0xc58c85, 0x8d5524]
  const skinColor = skinColors[Math.floor(Math.random() * skinColors.length)]
  const skinMat = new THREE.MeshStandardMaterial({ color: skinColor })
  
  // Clothing colors: Robes of different cultures
  const robeColors = [0xffffff, 0x8b4513, 0x2e8b57, 0x800080, 0xa52a2a]
  const robeColor = robeColors[Math.floor(Math.random() * robeColors.length)]
  const robeMat = new THREE.MeshStandardMaterial({ color: robeColor })

  // Body (Robe)
  const body = new THREE.Mesh(new THREE.CylinderGeometry(0.3, 0.5, 1.8, 8), robeMat)
  body.position.y = 0.9
  body.castShadow = true
  group.add(body)
  
  // Head
  const head = new THREE.Mesh(new THREE.SphereGeometry(0.25, 8, 8), skinMat)
  head.position.y = 2.0
  head.castShadow = true
  group.add(head)
  
  // Turban/Hat (Optional simple detail)
  const turban = new THREE.Mesh(new THREE.TorusGeometry(0.15, 0.1, 8, 10), new THREE.MeshStandardMaterial({ color: 0xffffff }))
  turban.position.set(0, 2.1, 0)
  turban.rotation.x = Math.PI / 2
  group.add(turban)

  // Arms
  const armGeo = new THREE.BoxGeometry(0.15, 0.8, 0.15)
  const leftArm = new THREE.Mesh(armGeo, robeMat)
  leftArm.position.set(-0.4, 1.4, 0)
  leftArm.userData.isArm = true
  group.add(leftArm)
  
  const rightArm = new THREE.Mesh(armGeo, robeMat)
  rightArm.position.set(0.4, 1.4, 0)
  rightArm.userData.isArm = true
  group.add(rightArm)

  return group
}

function buildDetailedCamel() {
  const group = new THREE.Group()
  const mat = new THREE.MeshStandardMaterial({ color: 0xbf9000 }) // Darker camel color
  
  // Body
  const body = new THREE.Mesh(new THREE.BoxGeometry(1.5, 1.5, 3.5), mat)
  body.position.y = 3
  body.castShadow = true
  group.add(body)
  
  // Humps
  const hump1 = new THREE.Mesh(new THREE.ConeGeometry(0.8, 1.5, 8), mat)
  hump1.position.set(0, 4, 0.5)
  group.add(hump1)
  const hump2 = new THREE.Mesh(new THREE.ConeGeometry(0.8, 1.5, 8), mat)
  hump2.position.set(0, 4, -1.0)
  group.add(hump2)
  
  // Neck & Head
  const neck = new THREE.Mesh(new THREE.BoxGeometry(0.6, 2, 0.8), mat)
  neck.position.set(0, 4, 2.2)
  neck.rotation.x = -0.4
  group.add(neck)
  
  const head = new THREE.Mesh(new THREE.BoxGeometry(0.7, 0.8, 1.5), mat)
  head.position.set(0, 5, 2.8)
  group.add(head)
  
  // Goods (Crates/Rolls)
  const crate = new THREE.Mesh(new THREE.BoxGeometry(1, 1, 1), new THREE.MeshStandardMaterial({ color: 0x8b4513 }))
  crate.position.set(0.8, 3.5, 0)
  crate.castShadow = true
  group.add(crate)
  
  const silk = new THREE.Mesh(new THREE.CylinderGeometry(0.3, 0.3, 1.2), new THREE.MeshStandardMaterial({ color: 0xff0000 }))
  silk.rotation.z = Math.PI / 2
  silk.position.set(-0.8, 3.8, 0)
  group.add(silk)
  
  // Legs (Pivot points for animation)
  const legGeo = new THREE.BoxGeometry(0.4, 3, 0.4)
  const legPositions = [
    { x: -0.6, y: 1.5, z: 1.2 },
    { x: 0.6, y: 1.5, z: 1.2 },
    { x: -0.6, y: 1.5, z: -1.2 },
    { x: 0.6, y: 1.5, z: -1.2 }
  ]
  
  legPositions.forEach(pos => {
    const leg = new THREE.Mesh(legGeo, mat)
    leg.position.set(pos.x, pos.y, pos.z)
    leg.castShadow = true
    // Store for animation
    leg.userData.isLeg = true
    group.add(leg)
  })
  
  return group
}

function createParticles() {
  const geometry = new THREE.BufferGeometry()
  const count = 2000
  const positions = new Float32Array(count * 3)
  
  for (let i = 0; i < count; i++) {
    positions[i * 3] = (Math.random() - 0.5) * 100
    positions[i * 3 + 1] = Math.random() * 20
    positions[i * 3 + 2] = (Math.random() - 0.5) * 100
  }
  
  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  
  const material = new THREE.PointsMaterial({
    color: 0xffffff,
    size: 0.2,
    transparent: true,
    opacity: 0.6
  })
  
  particleSystem = new THREE.Points(geometry, material)
  scene.add(particleSystem)
}

function createStars() {
  const geometry = new THREE.BufferGeometry()
  const count = 3000
  const positions = new Float32Array(count * 3)
  
  for (let i = 0; i < count; i++) {
    const r = 400 + Math.random() * 200
    const theta = Math.random() * Math.PI * 2
    const phi = Math.acos(Math.random() * 2 - 1)
    
    positions[i * 3] = r * Math.sin(phi) * Math.cos(theta)
    positions[i * 3 + 1] = r * Math.sin(phi) * Math.sin(theta)
    positions[i * 3 + 2] = r * Math.cos(phi)
  }
  
  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  
  const material = new THREE.PointsMaterial({
    color: 0xffffff,
    size: 1.5,
    transparent: true,
    opacity: 0
  })
  
  starsSystem = new THREE.Points(geometry, material)
  scene.add(starsSystem)
}

function createRoadsideObjects() {
  // Add some rocks and dead trees randomly along the path
  const rockGeo = new THREE.DodecahedronGeometry(1)
  const rockMat = new THREE.MeshStandardMaterial({ color: 0x666666, flatShading: true })
  
  const objectsGroup = new THREE.Group()
  
  for (let i = 0; i < 200; i++) {
    const mesh = new THREE.Mesh(rockGeo, rockMat)
    const x = (Math.random() > 0.5 ? 1 : -1) * (10 + Math.random() * 50)
    const z = -Math.random() * 1600
    
    mesh.position.set(x, 0, z)
    mesh.scale.setScalar(Math.random() * 5 + 1)
    mesh.rotation.set(Math.random()*Math.PI, Math.random()*Math.PI, Math.random()*Math.PI)
    mesh.castShadow = true
    mesh.receiveShadow = true
    objectsGroup.add(mesh)
  }
  
  scene.add(objectsGroup)
}

// --- Animation & Interaction Logic ---

function handleScroll(e: WheelEvent) {
  // Smooth scroll control
  const delta = e.deltaY * 0.0005
  targetScrollProgress.value = Math.min(Math.max(targetScrollProgress.value + delta, 0), 1)
}

function scrollToChapter(index: number) {
  // Map index to progress 0-1
  const step = 1 / (chapters.length - 1)
  targetScrollProgress.value = index * step
}

function updateEnvironment(progress: number) {
  // Determine which two chapters we are blending between
  const totalSegments = chapters.length - 1
  const scaledProgress = progress * totalSegments
  let idx = Math.floor(scaledProgress)
  idx = Math.min(idx, totalSegments - 1)
  const alpha = scaledProgress - idx // 0 to 1 interpolation factor
  
  const c1 = chapters[idx]
  const c2 = chapters[idx + 1]
  
  // Interpolate Colors
  const top = new THREE.Color(c1.skyColorTop).lerp(new THREE.Color(c2.skyColorTop), alpha)
  const bottom = new THREE.Color(c1.skyColorBottom).lerp(new THREE.Color(c2.skyColorBottom), alpha)
  const fog = new THREE.Color(c1.fogColor).lerp(new THREE.Color(c2.fogColor), alpha)
  const light = new THREE.Color(c1.lightColor).lerp(new THREE.Color(c2.lightColor), alpha)
  const ambientInt = c1.ambientIntensity * (1 - alpha) + c2.ambientIntensity * alpha
  
  // Apply
  scene.background = bottom
  scene.fog!.color = fog
  
  // Adjust fog density based on "Sandstorm" chapter (Chapter 4 is Taklamakan)
  let density = 0.02
  // Taklamakan is index 4. Increase density when entering (3->4) and leaving (4->5)
  if (idx === 4) {
     density = 0.04 // Dense fog in desert
  } else if (idx === 3 && alpha > 0.5) {
     density = 0.02 + (alpha - 0.5) * 0.04
  } else if (idx === 5 && alpha < 0.5) {
     density = 0.04 - alpha * 0.04
  }

  (scene.fog as THREE.FogExp2).density = density
  
  sunLight.color = light
  ambientLight.intensity = ambientInt
  
  // Stars visibility (Show at night - Kashgar is index 5)
  if (idx === 5) { // Kashgar Night
    (starsSystem.material as THREE.PointsMaterial).opacity = 1
  } else if (idx === 4 && alpha > 0.5) { // Entering Night
    (starsSystem.material as THREE.PointsMaterial).opacity = (alpha - 0.5) * 2
  } else if (idx === 6 && alpha < 0.5) { // Leaving Night
    (starsSystem.material as THREE.PointsMaterial).opacity = 1 - alpha * 2
  } else {
    (starsSystem.material as THREE.PointsMaterial).opacity = 0
  }

  // Update Current Chapter Text
  const sectionThreshold = 1 / chapters.length
  currentChapterIndex.value = Math.min(Math.floor(progress / sectionThreshold), chapters.length - 1)
}

function animate() {
  animationId = requestAnimationFrame(animate)
  
  const delta = clock.getDelta()
  const time = clock.getElapsedTime()
  
  // 1. Scroll Physics
  const diff = targetScrollProgress.value - scrollProgress.value
  scrollProgress.value += diff * 0.05
  
  // 2. Move Camera & Caravan
  // We move the caravan forward visually by moving camera and terrain relative
  // Let's move the camera along Z
  const maxZ = -1400 // Increased length
  const currentZ = scrollProgress.value * maxZ
  
  camera.position.z = currentZ + 15
  camera.position.y = 6 + Math.sin(time * 0.5) * 0.5 // Slight hover
  
  // Look ahead
  camera.lookAt(0, 2, currentZ - 20)
  
  // Move Caravan matching camera
  if (caravan) {
    caravan.position.z = currentZ
    
    // Animate Camels Walking
    const isMoving = Math.abs(diff) > 0.0001
    caravan.children.forEach((camel) => {
      // Bobbing
      if (isMoving) {
        camel.position.y = Math.sin(time * 5 + camel.userData.phase) * 0.1
        
        // Legs & Arms
        camel.children.forEach(part => {
          if (part.userData.isLeg) {
             // Simple leg swing logic
             const offset = part.position.x > 0 ? 0 : Math.PI
             part.rotation.x = Math.sin(time * 8 + offset + camel.userData.phase) * 0.5
          }
          if (part.userData.isArm) {
             // Arm swing logic (opposite to legs usually, but here simple swing)
             const offset = part.position.x > 0 ? Math.PI : 0
             part.rotation.x = Math.sin(time * 8 + offset + camel.userData.phase) * 0.5
          }
        })
      } else {
        // Idle breathing
        camel.position.y = Math.sin(time) * 0.05
        camel.children.forEach(part => {
          if (part.userData.isLeg) part.rotation.x = 0 // Reset legs
          if (part.userData.isArm) part.rotation.x = 0 // Reset arms
        })
      }
    })
  }
  
  // 3. Particles (Sandstorm wind)
  if (particleSystem) {
    const positions = particleSystem.geometry.attributes.position.array as Float32Array
    for(let i=0; i<positions.length; i+=3) {
      positions[i] += delta * 20 // Wind X
      positions[i+2] += delta * 50 // Wind Z
      
      // Reset if out of bounds (relative to camera)
      if (positions[i] > 50) positions[i] = -50
      if (positions[i+2] > currentZ + 50) positions[i+2] = currentZ - 50
      if (positions[i+2] < currentZ - 50) positions[i+2] = currentZ + 50
    }
    particleSystem.geometry.attributes.position.needsUpdate = true
    
    // Move whole system with camera loosely
    particleSystem.position.z = currentZ
  }
  
  // 4. Update Environment Colors
  updateEnvironment(scrollProgress.value)
  
  renderer.render(scene, camera)
}

// --- UI Interaction ---
function handleResize() {
  if (!camera || !renderer) return
  camera.aspect = window.innerWidth / window.innerHeight
  camera.updateProjectionMatrix()
  renderer.setSize(window.innerWidth, window.innerHeight)
}

function inspectGood(good: TradeGood) {
  inspectedGood.value = good
}

function closeInspect() {
  inspectedGood.value = null
}

</script>

<style scoped>
/* Global Fonts & Reset */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;700&display=swap');

.silk-road-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  position: relative;
  background: #000;
  font-family: 'Noto Serif SC', serif;
}

.canvas-container {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

/* Loading Overlay */
.loading-overlay {
  position: fixed;
  inset: 0;
  background: #1a1a1a;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d4af37; /* Gold */
}

.loading-content {
  text-align: center;
}

.loading-text h2 {
  font-size: 3rem;
  letter-spacing: 0.5rem;
  margin-bottom: 1rem;
}

.progress-bar {
  width: 300px;
  height: 2px;
  background: #333;
  margin: 2rem auto;
  position: relative;
}

.progress {
  height: 100%;
  background: #d4af37;
  transition: width 0.2s;
  box-shadow: 0 0 10px #d4af37;
}

.compass-spinner {
  width: 60px;
  height: 60px;
  border: 4px solid #d4af37;
  border-radius: 50%;
  border-top-color: transparent;
  margin: 0 auto 2rem;
  animation: spin 1s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

/* UI Layer */
.ui-layer {
  position: absolute;
  inset: 0;
  z-index: 10;
  pointer-events: none;
  display: flex;
  transition: opacity 0.5s;
}

.ui-hidden {
  opacity: 0;
}

/* Timeline Sidebar */
.timeline-bar {
  position: absolute;
  left: 3rem;
  top: 20%;
  bottom: 20%;
  width: 4px;
  background: rgba(255,255,255,0.2);
  pointer-events: auto;
}

.timeline-progress {
  width: 100%;
  background: #d4af37;
  box-shadow: 0 0 10px rgba(212, 175, 55, 0.5);
  transition: height 0.1s;
}

.timeline-points {
  position: absolute;
  top: 0;
  left: -6px;
  width: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.point {
  width: 16px;
  height: 16px;
  background: #333;
  border: 2px solid #666;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
}

.point.active {
  background: #d4af37;
  border-color: #fff;
  box-shadow: 0 0 15px #d4af37;
}

.point-tooltip {
  position: absolute;
  left: 25px;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0,0,0,0.8);
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  opacity: 0;
  transition: opacity 0.3s;
  white-space: nowrap;
}

.point:hover .point-tooltip {
  opacity: 1;
}

/* Content Card */
.content-wrapper {
  position: absolute;
  right: 5%;
  top: 50%;
  transform: translateY(-50%);
  width: 450px;
  pointer-events: auto;
}

.chapter-card {
  background: rgba(20, 20, 25, 0.85);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(212, 175, 55, 0.3);
  padding: 2.5rem;
  color: #e0e0e0;
  box-shadow: 0 20px 50px rgba(0,0,0,0.5);
  border-radius: 4px;
}

.chapter-header {
  border-bottom: 1px solid rgba(212, 175, 55, 0.2);
  padding-bottom: 1.5rem;
  margin-bottom: 1.5rem;
}

.chapter-number {
  font-family: 'Arial', sans-serif;
  font-size: 0.9rem;
  color: #d4af37;
  letter-spacing: 2px;
  display: block;
  margin-bottom: 0.5rem;
}

.chapter-title {
  font-size: 2.2rem;
  margin: 0;
  color: #fff;
  text-shadow: 0 2px 10px rgba(0,0,0,0.5);
}

.chapter-meta {
  margin-top: 1rem;
  display: flex;
  gap: 1.5rem;
  font-size: 0.9rem;
  color: #999;
}

.main-text {
  line-height: 1.8;
  font-size: 1.05rem;
  margin-bottom: 2rem;
  text-align: justify;
}

.historical-facts h3, .goods-section h3 {
  font-size: 1.1rem;
  color: #d4af37;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.historical-facts ul {
  list-style: none;
  padding: 0;
  font-size: 0.9rem;
  color: #bbb;
}

.historical-facts li {
  margin-bottom: 0.8rem;
  padding-left: 1rem;
  border-left: 2px solid #444;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.good-item {
  background: rgba(255,255,255,0.05);
  padding: 1rem;
  text-align: center;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.3s;
}

.good-item:hover {
  background: rgba(212, 175, 55, 0.1);
}

.good-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.good-name {
  font-size: 0.9rem;
  color: #ccc;
}

/* Scroll Hint */
.scroll-hint {
  position: absolute;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  color: rgba(255,255,255,0.7);
  animation: bounce 2s infinite;
}

.mouse {
  width: 26px;
  height: 42px;
  border: 2px solid rgba(255,255,255,0.7);
  border-radius: 13px;
  margin: 0 auto 0.5rem;
  position: relative;
}

.mouse::after {
  content: '';
  position: absolute;
  top: 8px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  background: #fff;
  border-radius: 50%;
  animation: scroll 1.5s infinite;
}

@keyframes scroll {
  0% { top: 8px; opacity: 1; }
  100% { top: 20px; opacity: 0; }
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% { transform: translate(-50%, 0); }
  40% { transform: translate(-50%, -10px); }
  60% { transform: translate(-50%, -5px); }
}

/* Detail Modal */
.detail-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.8);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(5px);
}

.detail-modal {
  background: #1a1a1a;
  width: 90%;
  max-width: 500px;
  padding: 2rem;
  border: 1px solid #d4af37;
  color: #fff;
  position: relative;
  box-shadow: 0 0 50px rgba(212, 175, 55, 0.2);
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #333;
  padding-bottom: 1rem;
}

.detail-header h2 {
  font-size: 1.8rem;
  color: #d4af37;
}

.close-btn {
  background: none;
  border: none;
  color: #999;
  font-size: 2rem;
  cursor: pointer;
  line-height: 1;
}

.close-btn:hover {
  color: #fff;
}

.detail-icon-large {
  font-size: 5rem;
  text-align: center;
  margin: 1rem 0 2rem;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.detail-context {
  margin-top: 2rem;
  padding: 1rem;
  background: rgba(212, 175, 55, 0.05);
  border-left: 3px solid #d4af37;
  font-size: 0.9rem;
}

/* Transitions */
.fade-enter-active, .fade-leave-active { transition: opacity 0.5s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.slide-up-enter-active, .slide-up-leave-active { transition: all 0.5s ease; }
.slide-up-enter-from { opacity: 0; transform: translateY(30px); }
.slide-up-leave-to { opacity: 0; transform: translateY(-30px); }

.zoom-enter-active, .zoom-leave-active { transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.zoom-enter-from, .zoom-leave-to { opacity: 0; transform: scale(0.8); }
</style>
