<template>
  <div class="history-container">
    <AiTechBackButton />
    <div ref="canvasContainer" class="canvas-container"></div>
    
    <!-- Loading Screen -->
    <div v-if="loading" class="loading-screen">
      <div class="loader-content">
        <div class="loader-title">历史铭记</div>
        <div class="loader-subtitle">日本侵华事件纪实 (中古至今)</div>
        <div class="loading-bar-container">
            <div class="loading-bar" :style="{ width: loadingProgress + '%' }"></div>
        </div>
        <div class="loader-text">{{ loadingProgress }}%</div>
      </div>
    </div>

    <!-- UI Overlay -->
    <div class="ui-overlay" :class="{ 'hidden': isImmersive }">
      <div class="header">
        <h1 class="title">铭记历史 · 勿忘国耻</h1>
        <p class="subtitle">日本侵华历史事件回顾 (中古 - 现代)</p>
      </div>
      
      <div class="controls">
         <div class="control-group">
             <button class="control-btn" @click="toggleAutoPlay">{{ autoPlay ? '⏸ 暂停巡览' : '▶ 自动巡览' }}</button>
             <div class="speed-control" v-if="autoPlay">
                 <span class="label">速度:</span>
                 <input type="range" min="0.5" max="3" step="0.5" v-model.number="playSpeed" />
                 <span class="value">{{ playSpeed }}x</span>
             </div>
         </div>
         
         <div class="scroll-hint">
            <span>🖱️ 滚动鼠标 / 拖拽探索</span>
         </div>
      </div>

      <!-- Navigation Menu -->
      <div class="nav-menu">
          <button class="nav-toggle" @click="showNav = !showNav">
              {{ showNav ? '收起列表' : '事件列表' }}
          </button>
          <transition name="slide-up">
              <div class="nav-list" v-if="showNav">
                  <div 
                    v-for="(event, idx) in historyEvents" 
                    :key="idx" 
                    class="nav-item"
                    :class="{ active: currentEventIndex === idx }"
                    @click="navigateToEvent(idx)"
                  >
                      <span class="nav-year">{{ event.year }}</span>
                      <span class="nav-title">{{ event.title }}</span>
                  </div>
              </div>
          </transition>
      </div>

      <!-- Timeline Indicator -->
      <div class="timeline-indicator">
        <div class="current-year">{{ currentYearLabel }}</div>
      </div>
    </div>

    <!-- Event Detail Modal -->
    <transition name="modal-fade">
      <div v-if="selectedEvent" class="detail-modal" @click.self="closeModal">
        <div class="modal-content glass-effect">
          <button class="close-btn" @click="closeModal">
            <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
          </button>
          <div class="modal-header">
            <div class="event-year">{{ selectedEvent.year }}</div>
            <h2>{{ selectedEvent.title }}</h2>
          </div>
          <div class="modal-body">
            <div class="event-image" v-if="selectedEvent.image">
                <!-- Placeholder for images, in real app would use actual URLs -->
                <div class="img-placeholder">{{ selectedEvent.title }} 影像资料</div>
            </div>
            <div class="event-description">
                <p v-for="(para, idx) in selectedEvent.description" :key="idx">{{ para }}</p>
            </div>
            <div class="event-impact" v-if="selectedEvent.impact">
                <h3>🔴 影响与后果</h3>
                <p>{{ selectedEvent.impact }}</p>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls';
import TWEEN from '@tweenjs/tween.js';
import AiTechBackButton from '@/components/aitech/AiTechBackButton.vue';

// --- Data Definitions ---
interface HistoryEvent {
    year: string;
    title: string;
    description: string[];
    impact?: string;
    image?: string;
    type: 'major' | 'minor';
    x?: number; // Position in 3D space
    y?: number;
    z?: number;
}

const historyEvents: HistoryEvent[] = [
    {
        year: '明朝 (14-16世纪)',
        title: '倭寇之乱',
        description: [
            '明朝中期，日本海盗（倭寇）勾结中国海商，频繁骚扰中国沿海地区。',
            '嘉靖年间，倭寇之患达到顶峰，烧杀抢掠，无恶不作。',
            '名将戚继光组建“戚家军”，发明“鸳鸯阵”，在台州九战九捷，基本平息了浙江倭患。'
        ],
        impact: '严重破坏了中国沿海经济和百姓生活，但也促使明朝加强海防。',
        type: 'minor'
    },
    {
        year: '1592-1598',
        title: '万历援朝战争 (壬辰倭乱)',
        description: [
            '丰臣秀吉统一日本后，发动对朝鲜的侵略战争，意图以朝鲜为跳板进攻明朝。',
            '明神宗应朝鲜请求，派兵援助。',
            '中朝联军在平壤、碧蹄馆等地与日军激战，最终日军撤退。'
        ],
        impact: '明朝耗费巨大国力，客观上加速了明朝的衰落，但也粉碎了日本的扩张野心。',
        type: 'major'
    },
    {
        year: '1874',
        title: '牡丹社事件 (侵台)',
        description: [
            '日本借口琉球船民在台湾被杀，出兵侵犯台湾。',
            '这是日本明治维新后首次对外用兵，暴露了其侵略野心。',
            '清政府最终赔款50万两白银，日军撤出。'
        ],
        impact: '清政府认识到海防重要性，开始筹办海军。日本吞并琉球。',
        type: 'minor'
    },
    {
        year: '1894-1895',
        title: '甲午中日战争',
        description: [
            '日本蓄意挑起战争，在丰岛海面偷袭清军运兵船。',
            '主要战役包括平壤战役、黄海海战、辽东战役、威海卫战役。',
            '北洋水师全军覆没。'
        ],
        impact: '清政府战败，签订丧权辱国的《马关条约》，割让台湾、澎湖，赔款2亿两白银。大大加深了中国半殖民地化程度，刺激了列强瓜分中国的野心。',
        type: 'major'
    },
    {
        year: '1900',
        title: '八国联军侵华',
        description: [
            '日本作为八国联军主力之一（出兵最多），攻陷北京。',
            '日军在北京烧杀抢掠，抢走大量户部银两。'
        ],
        impact: '签订《辛丑条约》，中国完全陷入半殖民地半封建社会深渊。',
        type: 'major'
    },
    {
        year: '1904-1905',
        title: '日俄战争',
        description: [
            '日本与沙俄为了争夺在中国东北的利益，在中国领土上进行战争。',
            '清政府竟宣布“局外中立”。',
            '战争给东北人民带来巨大灾难。'
        ],
        impact: '日本战胜，接管了俄国在南满的特权，势力进一步深入中国东北。',
        type: 'major'
    },
    {
        year: '1915',
        title: '二十一条',
        description: [
            '日本趁第一次世界大战欧美列强无暇东顾，向袁世凯政府提出灭亡中国的“二十一条”。',
            '内容包括承认日本接管德国在山东特权、延长旅顺大连租期等。'
        ],
        impact: '袁世凯部分接受，引发全国人民愤怒，5月9日被定为“国耻日”。',
        type: 'major'
    },
    {
        year: '1928',
        title: '济南惨案 (五三惨案)',
        description: [
            '国民革命军北伐途中，日军借口保护侨民出兵山东济南。',
            '日军屠杀中国军民6000余人，杀害外交官蔡公时。'
        ],
        impact: '阻碍了中国统一进程，激起中国人民强烈愤慨。',
        type: 'minor'
    },
    {
        year: '1928',
        title: '皇姑屯事件',
        description: [
            '关东军在皇姑屯炸死奉系军阀张作霖。',
            '意图制造混乱，趁机占领东北。'
        ],
        type: 'minor'
    },
    {
        year: '1931',
        title: '九一八事变',
        description: [
            '9月18日，日本关东军炸毁南满铁路柳条湖段，诬陷中国军队所为，炮轰北大营。',
            '由于国民政府的不抵抗政策，日军迅速占领东北三省。'
        ],
        impact: '中国局部抗战开始，东北三省沦陷，3000万同胞沦为亡国奴。',
        type: 'major'
    },
    {
        year: '1932',
        title: '一·二八事变',
        description: [
            '日军在上海挑衅，进攻闸北。',
            '十九路军在蔡廷锴、蒋光鼐指挥下奋起抵抗。'
        ],
        type: 'minor'
    },
    {
        year: '1932',
        title: '伪满洲国成立',
        description: [
            '日本扶持溥仪在长春成立傀儡政权“满洲国”。',
            '企图将东北从中国分裂出去，实行殖民统治。'
        ],
        type: 'minor'
    },
    {
        year: '1933',
        title: '长城抗战',
        description: [
            '日军进攻热河及长城各口。',
            '中国军队在喜峰口等地顽强抵抗（大刀队）。'
        ],
        type: 'minor'
    },
    {
        year: '1935',
        title: '华北事变',
        description: [
            '日本策划“华北五省自治”，企图把华北变成第二个“满洲国”。'
        ],
        impact: '中日民族矛盾上升为主要矛盾，引发“一二·九”运动。',
        type: 'minor'
    },
    {
        year: '1937.7.7',
        title: '七七事变 (卢沟桥事变)',
        description: [
            '日军在卢沟桥附近演习，借口士兵失踪，要求进入宛平城搜查，遭拒后进攻。',
            '第二十九军奋起抵抗。'
        ],
        impact: '标志着日本全面侵华战争开始，也是中国全民族抗战的开端。',
        type: 'major'
    },
    {
        year: '1937.8',
        title: '淞沪会战',
        description: [
            '日军进攻上海，妄图“三个月灭亡中国”。',
            '中国军队投入精锐主力，激战三个月。',
            '谢晋元“八百壮士”坚守四行仓库。'
        ],
        impact: '粉碎了日本“三个月灭亡中国”的狂妄计划。上海失守。',
        type: 'major'
    },
    {
        year: '1937.12',
        title: '南京大屠杀',
        description: [
            '12月13日，日军攻陷南京。',
            '日军在南京进行了长达6周的血腥屠杀。',
            '屠杀手无寸铁的平民和放下武器的士兵达30万人以上。',
            '强奸、抢劫、纵火，犯下滔天罪行。'
        ],
        impact: '人类历史上最黑暗的一页之一，铁证如山，不容篡改。',
        type: 'major'
    },
    {
        year: '1938',
        title: '台儿庄大捷',
        description: [
            '徐州会战中，李宗仁指挥中国军队在台儿庄重创日军。',
            '歼灭日军一万余人。'
        ],
        impact: '抗战以来中国军队取得的最大胜利，极大地鼓舞了军民士气。',
        type: 'major'
    },
    {
        year: '1938.6',
        title: '武汉会战',
        description: [
            '抗战以来规模最大、时间最长、歼敌最多的一次战役。',
            '历时4个多月，毙伤日军25万余人。'
        ],
        impact: '武汉失守，但日军国力消耗巨大，抗日战争进入相持阶段。',
        type: 'major'
    },
    {
        year: '1940',
        title: '百团大战',
        description: [
            '八路军在彭德怀指挥下，在华北发动的大规模进攻战役。',
            '破袭日军交通线，拔除据点。'
        ],
        impact: '打击了日军的囚笼政策，坚定了全国人民抗战胜利的信心。',
        type: 'major'
    },
    {
        year: '1941-1942',
        title: '扫荡与反扫荡',
        description: [
            '日军对敌后抗日根据地实行“三光政策”（烧光、杀光、抢光）。',
            '制造了“潘家峪惨案”等无数惨案。'
        ],
        type: 'minor'
    },
    {
        year: '1944',
        title: '豫湘桂战役',
        description: [
            '日军为打通大陆交通线发动的最大规模进攻。',
            '国民党军队大溃败，丢失大片国土。'
        ],
        type: 'minor'
    },
    {
        year: '1945.8.15',
        title: '日本无条件投降',
        description: [
            '在美国投下原子弹、苏联出兵东北、中国军民大反攻的打击下，日本天皇裕仁广播《终战诏书》。',
            '9月2日，日本签署投降书。'
        ],
        impact: '中国抗日战争取得完全胜利，台湾回归祖国。世界反法西斯战争胜利。',
        type: 'major'
    },
    {
        year: '2012',
        title: '钓鱼岛国有化闹剧',
        description: [
            '日本政府不顾中方强烈反对，宣布将钓鱼岛“国有化”。'
        ],
        impact: '严重侵犯中国领土主权，中日关系降至冰点。',
        type: 'minor'
    },
    {
        year: '近年',
        title: '参拜靖国神社与篡改教科书',
        description: [
            '日本右翼势力屡次参拜供奉甲级战犯的靖国神社。',
            '修改历史教科书，美化侵略战争，否认南京大屠杀等罪行。'
        ],
        impact: '严重伤害中国及亚洲受害国人民感情，警惕军国主义复活。',
        type: 'minor'
    }
];

// --- Vue State ---
const canvasContainer = ref<HTMLElement | null>(null);
const loading = ref(true);
const loadingProgress = ref(0);
const selectedEvent = ref<HistoryEvent | null>(null);
const isImmersive = ref(false);
const autoPlay = ref(false);
const currentYearLabel = ref('开始');
const playSpeed = ref(1);
const showNav = ref(false);
const currentEventIndex = ref(-1);

// --- Three.js Globals ---
let scene: THREE.Scene;
let camera: THREE.PerspectiveCamera;
let renderer: THREE.WebGLRenderer;
let controls: OrbitControls;
let particles: THREE.Points;
let raycaster = new THREE.Raycaster();
let mouse = new THREE.Vector2();
let eventMeshes: THREE.Group[] = [];
let animationId: number;
let progress = 0;
let lastTime = Date.now();

// Path curve
let curve: THREE.CatmullRomCurve3;

// --- Initialization ---
onMounted(async () => {
  lastTime = Date.now();
  // Simulate loading
  let p = 0;
  const interval = setInterval(() => {
    p += 2;
    loadingProgress.value = p;
    if(p >= 100) {
      clearInterval(interval);
      loading.value = false;
      initThree();
      animate();
    }
  }, 30);

  window.addEventListener('resize', onWindowResize);
  window.addEventListener('click', onMouseClick);
  window.addEventListener('mousemove', onMouseMove);
});

onBeforeUnmount(() => {
  cancelAnimationFrame(animationId);
  window.removeEventListener('resize', onWindowResize);
  window.removeEventListener('click', onMouseClick);
  window.removeEventListener('mousemove', onMouseMove);
  renderer?.dispose();
  if (controls) controls.dispose();
});

function initThree() {
  if (!canvasContainer.value) return;

  // 1. Scene Setup
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0x050505); // Very dark background
  scene.fog = new THREE.FogExp2(0x050505, 0.002);

  // 2. Camera
  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 2000);
  camera.position.set(0, 50, 100);

  // 3. Renderer
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  canvasContainer.value.appendChild(renderer.domElement);

  // 4. Controls
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;
  controls.maxDistance = 500;
  controls.minDistance = 10;
  
  // 5. Lights
  const ambientLight = new THREE.AmbientLight(0x404040);
  scene.add(ambientLight);
  
  const dirLight = new THREE.DirectionalLight(0xffffff, 1);
  dirLight.position.set(100, 100, 50);
  scene.add(dirLight);
  
  const redLight = new THREE.PointLight(0xff0000, 2, 300);
  redLight.position.set(0, 50, 0);
  scene.add(redLight); // Blood red ominous light

  // 6. Create Timeline Path (A winding bloody river/path)
  createPath();

  // 7. Create Particles (Stars/Dust)
  createParticles();
  
  // 8. Place Events
  placeEvents();
}

function createPath() {
  // Create a winding path
  const points = [];
  const count = historyEvents.length;
  const spacing = 40;
  
  for (let i = 0; i < count; i++) {
      // Spiral or winding path
      const angle = i * 0.5;
      const x = Math.sin(angle) * 50;
      const z = -i * spacing;
      const y = Math.cos(angle * 2) * 10;
      points.push(new THREE.Vector3(x, y, z));
      
      // Store position in event data for camera targeting
      historyEvents[i].x = x;
      historyEvents[i].y = y;
      historyEvents[i].z = z;
  }
  
  curve = new THREE.CatmullRomCurve3(points);
  
  // Tube Geometry for the path
  const geometry = new THREE.TubeGeometry(curve, count * 10, 2, 8, false);
  const material = new THREE.MeshStandardMaterial({ 
      color: 0x880000, // Dark red
      emissive: 0x330000,
      roughness: 0.4,
      metalness: 0.6,
      wireframe: false
  });
  const tube = new THREE.Mesh(geometry, material);
  scene.add(tube);
  
  // Add a wireframe glow effect around the tube
  const wireGeo = new THREE.TubeGeometry(curve, count * 10, 2.2, 8, false);
  const wireMat = new THREE.MeshBasicMaterial({ color: 0xff0000, wireframe: true, transparent: true, opacity: 0.1 });
  const wireMesh = new THREE.Mesh(wireGeo, wireMat);
  scene.add(wireMesh);
}

function createParticles() {
  const geometry = new THREE.BufferGeometry();
  const vertices = [];
  
  for (let i = 0; i < 2000; i++) {
    vertices.push(
        THREE.MathUtils.randFloatSpread(600),
        THREE.MathUtils.randFloatSpread(400),
        THREE.MathUtils.randFloatSpread(historyEvents.length * 40 + 200) * -1 + 200
    );
  }
  
  geometry.setAttribute('position', new THREE.Float32BufferAttribute(vertices, 3));
  const material = new THREE.PointsMaterial({ color: 0x888888, size: 1.5, transparent: true, opacity: 0.6 });
  particles = new THREE.Points(geometry, material);
  scene.add(particles);
}

function placeEvents() {
    // Using Sprite for text to avoid external font loading
    
    historyEvents.forEach((event, index) => {
        const group = new THREE.Group();
        group.position.set(event.x!, event.y!, event.z!);
        
        // Event Marker (Sphere)
        const size = event.type === 'major' ? 4 : 2;
        const color = event.type === 'major' ? 0xff0000 : 0xffaaaa;
        
        const geometry = new THREE.SphereGeometry(size, 32, 32);
        const material = new THREE.MeshStandardMaterial({ 
            color: color, 
            emissive: 0x550000, 
            emissiveIntensity: 0.5,
            roughness: 0.2
        });
        const sphere = new THREE.Mesh(geometry, material);
        
        // Add a glow ring
        const ringGeo = new THREE.RingGeometry(size + 1, size + 1.5, 32);
        const ringMat = new THREE.MeshBasicMaterial({ color: color, side: THREE.DoubleSide, transparent: true, opacity: 0.5 });
        const ring = new THREE.Mesh(ringGeo, ringMat);
        ring.rotation.x = Math.PI / 2; // lay flat? Or face camera? Let's make it billboard later
        
        // Store event data in userData for raycasting
        sphere.userData = { isEvent: true, index: index, eventData: event };
        
        group.add(sphere);
        group.add(ring);
        
        // Label (Billboard sprite or simple text mesh if font loaded)
        // Using Sprite for text is easier without font loader
        const canvas = document.createElement('canvas');
        const context = canvas.getContext('2d');
        if (context) {
            canvas.width = 512;
            canvas.height = 256;
            context.fillStyle = 'rgba(0,0,0,0)';
            context.fillRect(0, 0, 512, 256);
            
            context.font = 'bold 48px "Microsoft YaHei", sans-serif';
            context.textAlign = 'center';
            context.textBaseline = 'middle';
            context.fillStyle = '#ffffff';
            context.fillText(event.year, 256, 100);
            
            context.font = '36px "Microsoft YaHei", sans-serif';
            context.fillStyle = event.type === 'major' ? '#ff3333' : '#cccccc';
            context.fillText(event.title, 256, 160);
            
            const texture = new THREE.CanvasTexture(canvas);
            const spriteMat = new THREE.SpriteMaterial({ map: texture, transparent: true });
            const sprite = new THREE.Sprite(spriteMat);
            sprite.scale.set(20, 10, 1);
            sprite.position.y = size + 5;
            group.add(sprite);
        }

        scene.add(group);
        eventMeshes.push(group);
    });
}

function onWindowResize() {
  if (!camera || !renderer) return;
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
}

function onMouseMove(event: MouseEvent) {
  mouse.x = (event.clientX / window.innerWidth) * 2 - 1;
  mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;
  
  // Raycast for hover effect
  if (camera && eventMeshes.length > 0) {
      raycaster.setFromCamera(mouse, camera);
      // Check intersections with event spheres
      // Optimization: collect just spheres
      const spheres = eventMeshes.map(g => g.children[0]); 
      const intersects = raycaster.intersectObjects(spheres);
      
      if (intersects.length > 0) {
          document.body.style.cursor = 'pointer';
          const object = intersects[0].object as THREE.Mesh;
          // Hover effect (scale up)
          // TWEEN could be used here
      } else {
          document.body.style.cursor = 'default';
      }
  }
}

function onMouseClick(event: MouseEvent) {
    if (isImmersive.value) return;
    
    raycaster.setFromCamera(mouse, camera);
    const spheres = eventMeshes.map(g => g.children[0]);
    const intersects = raycaster.intersectObjects(spheres);
    
    if (intersects.length > 0) {
        const object = intersects[0].object;
        const data = object.userData.eventData;
        selectEvent(data);
    }
}

function selectEvent(event: HistoryEvent) {
    // Pause auto-play when selecting an event
    autoPlay.value = false;
    selectedEvent.value = event;
    
    // Move camera to event
    if (event.x !== undefined && event.z !== undefined) {
        const targetPos = new THREE.Vector3(event.x + 10, event.y! + 10, event.z + 20);
        const lookAtPos = new THREE.Vector3(event.x, event.y, event.z);
        
        new TWEEN.Tween(camera.position)
            .to({ x: targetPos.x, y: targetPos.y, z: targetPos.z }, 1500)
            .easing(TWEEN.Easing.Cubic.Out)
            .start();
            
        new TWEEN.Tween(controls.target)
            .to({ x: lookAtPos.x, y: lookAtPos.y, z: lookAtPos.z }, 1500)
            .easing(TWEEN.Easing.Cubic.Out)
            .start();
    }
}

function navigateToEvent(index: number) {
    if (index >= 0 && index < historyEvents.length) {
        const event = historyEvents[index];
        selectEvent(event);
        currentEventIndex.value = index;
        // showNav.value = false; // Keep menu open or close it? User might want to browse. Let's keep it open but maybe highlight.
        // The requirement says "guide clicking position", so maybe navigating to it is enough.
        // Let's close it for better view.
        showNav.value = false;
    }
}

function closeModal() {
    selectedEvent.value = null;
}

function toggleAutoPlay() {
    autoPlay.value = !autoPlay.value;
}

// Animation Loop
function animate(time?: number) {
  animationId = requestAnimationFrame(animate);
  
  const now = Date.now();
  const delta = now - lastTime;
  lastTime = now;

  TWEEN.update(time);
  
  if (controls) controls.update();

  // Pulse effect for event rings and rotation
  const pulseScale = 1 + Math.sin(now * 0.003) * 0.2;
  eventMeshes.forEach(group => {
      const ring = group.children[1] as THREE.Mesh; // Ring is 2nd child
      if (ring) {
          ring.scale.set(pulseScale, pulseScale, 1);
          ring.rotation.z -= 0.01;
      }
  });
  
  // Auto-play logic: move camera along path
  if (autoPlay.value && curve) {
      // Base speed: 1 loop per 60 seconds
      const baseSpeed = 1 / 60000;
      // Adjust progress based on delta and playSpeed
      // If delta is too large (e.g. tab inactive), cap it to avoid jumps
      const validDelta = Math.min(delta, 100); 
      progress += validDelta * baseSpeed * playSpeed.value;
      
      const t = progress % 1;
      
      const position = curve.getPointAt(t);
      const lookAt = curve.getPointAt((t + 0.01) % 1);
      
      camera.position.copy(position).add(new THREE.Vector3(10, 10, 20));
      controls.target.copy(lookAt);
      
      // Update current year label and index based on t
      const index = Math.floor(t * historyEvents.length);
      if (historyEvents[index]) {
          currentYearLabel.value = historyEvents[index].year;
          // Only update index if we are not manually hovering? 
          // Actually updating it is good for the list to scroll/highlight
          currentEventIndex.value = index;
      }
  } else {
     // Subtle movement for particles
     if (particles) {
         particles.rotation.y += 0.0005;
     }
  }
  
  if (renderer && scene && camera) {
      renderer.render(scene, camera);
  }
}

</script>

<style scoped>
.history-container {
  width: 100%;
  height: 100vh;
  position: relative;
  overflow: hidden;
  background-color: #050505;
  font-family: 'SimHei', 'Microsoft YaHei', sans-serif;
}

.canvas-container {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

.loading-screen {
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
  color: #fff;
}

.loader-content {
    text-align: center;
    width: 80%;
    max-width: 500px;
}

.loader-title {
    font-size: 3rem;
    font-weight: bold;
    margin-bottom: 10px;
    letter-spacing: 5px;
    color: #d32f2f;
}

.loader-subtitle {
    font-size: 1.2rem;
    color: #888;
    margin-bottom: 30px;
}

.loading-bar-container {
    width: 100%;
    height: 4px;
    background: #333;
    border-radius: 2px;
    overflow: hidden;
    margin-bottom: 10px;
}

.loading-bar {
    height: 100%;
    background: #d32f2f;
    transition: width 0.1s ease;
}

.ui-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 10;
  pointer-events: none; /* Let clicks pass through to canvas */
}

.ui-overlay.hidden {
    opacity: 0;
}

.header {
  position: absolute;
  top: 30px;
  left: 50px;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.8);
  pointer-events: auto;
}

.title {
  font-size: 3rem;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(to right, #ff3333, #ff9999);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  font-size: 1.2rem;
  color: #aaa;
  margin-top: 5px;
}

.controls {
    position: absolute;
    bottom: 50px;
    left: 50px;
    pointer-events: auto;
    display: flex;
    gap: 20px;
    align-items: center;
}

.control-btn {
    background: rgba(255, 51, 51, 0.2);
    border: 1px solid #ff3333;
    color: #fff;
    padding: 10px 20px;
    border-radius: 5px;
    cursor: pointer;
    transition: all 0.3s;
}

.control-btn:hover {
    background: rgba(255, 51, 51, 0.5);
}

.speed-control {
    display: flex;
    align-items: center;
    gap: 10px;
    background: rgba(0, 0, 0, 0.5);
    padding: 5px 15px;
    border-radius: 20px;
    border: 1px solid #444;
}

.speed-control .label {
    color: #aaa;
    font-size: 0.9rem;
}

.speed-control input[type=range] {
    width: 100px;
    cursor: pointer;
}

.speed-control .value {
    color: #fff;
    min-width: 30px;
    text-align: right;
}

.nav-menu {
    position: absolute;
    top: 120px;
    right: 50px;
    width: 250px;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    pointer-events: auto;
}

.nav-toggle {
    background: rgba(211, 47, 47, 0.2);
    border: 1px solid #d32f2f;
    color: #fff;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s;
    margin-bottom: 10px;
}

.nav-toggle:hover {
    background: rgba(211, 47, 47, 0.6);
}

.nav-list {
    width: 100%;
    max-height: 60vh;
    overflow-y: auto;
    background: rgba(10, 10, 10, 0.8);
    border: 1px solid #333;
    border-radius: 4px;
    backdrop-filter: blur(5px);
}

.nav-item {
    padding: 10px 15px;
    border-bottom: 1px solid #222;
    cursor: pointer;
    transition: background 0.2s;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.nav-item:last-child {
    border-bottom: none;
}

.nav-item:hover {
    background: rgba(255, 255, 255, 0.1);
}

.nav-item.active {
    background: rgba(211, 47, 47, 0.3);
    border-left: 3px solid #d32f2f;
}

.nav-year {
    font-size: 0.85rem;
    color: #aaa;
    font-family: 'Impact', sans-serif;
}

.nav-title {
    font-size: 0.9rem;
    color: #fff;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 160px;
}

/* Custom Scrollbar for Nav */
.nav-list::-webkit-scrollbar {
    width: 6px;
}

.nav-list::-webkit-scrollbar-track {
    background: #111;
}

.nav-list::-webkit-scrollbar-thumb {
    background: #444;
    border-radius: 3px;
}

.slide-up-enter-active,
.slide-up-leave-active {
    transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
    opacity: 0;
    transform: translateY(-20px);
}

.scroll-hint {
    color: #888;
    font-size: 0.9rem;
}

.timeline-indicator {
    position: absolute;
    bottom: 50px;
    right: 50px;
    color: #fff;
}

.current-year {
    font-size: 4rem;
    font-weight: bold;
    opacity: 0.5;
    font-family: 'Impact', sans-serif;
}

.detail-modal {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 50;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(0,0,0,0.6);
  backdrop-filter: blur(5px);
}

.modal-content {
  width: 80%;
  max-width: 800px;
  max-height: 80vh;
  background: rgba(20, 20, 20, 0.9);
  border: 1px solid #333;
  border-left: 5px solid #d32f2f;
  border-radius: 8px;
  padding: 40px;
  color: #fff;
  position: relative;
  overflow-y: auto;
  box-shadow: 0 10px 30px rgba(0,0,0,0.5);
  pointer-events: auto;
}

.close-btn {
    position: absolute;
    top: 20px;
    right: 20px;
    background: none;
    border: none;
    color: #888;
    cursor: pointer;
    padding: 5px;
}

.close-btn:hover {
    color: #fff;
}

.modal-header {
    margin-bottom: 30px;
    border-bottom: 1px solid #333;
    padding-bottom: 20px;
}

.event-year {
    font-size: 1.5rem;
    color: #d32f2f;
    font-weight: bold;
}

.modal-header h2 {
    font-size: 2.5rem;
    margin: 10px 0 0;
}

.modal-body {
    line-height: 1.8;
    font-size: 1.1rem;
    color: #ddd;
}

.event-image {
    margin-bottom: 20px;
    width: 100%;
    height: 200px;
    background: #222;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #555;
    border-radius: 4px;
}

.event-impact {
    margin-top: 30px;
    padding: 20px;
    background: rgba(211, 47, 47, 0.1);
    border-radius: 4px;
}

.event-impact h3 {
    color: #d32f2f;
    margin-top: 0;
}

/* Animations */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>
