<template>
  <div class="ai-tech-page">
    <!-- 头部横幅 -->
    <div class="page-header">
      <div class="container mx-auto px-4">
        <h1 class="text-3xl font-bold text-gray-900 mb-4">AI 互动教程</h1>
        <p class="text-gray-600 text-lg max-w-2xl">
          探索人工智能与前沿技术的互动展示，通过直观的可视化和模拟体验，深入理解科学原理。
        </p>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="container mx-auto px-4 py-8">
      <!-- 筛选/分类栏 (预留) -->
      <!-- <div class="filter-bar mb-8">...</div> -->

      <!-- 卡片网格 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        
        

        

        

        

        

        
        <!-- 中华上下5000年卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'chinese-history', cardTitle: '中华上下五千年', cardPath: '/aitech/chinese-history', cardCategory: '历史文化' })">
          <div class="card-cover history-bg">
            <div class="card-icon">
              <span class="text-4xl">📜</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-amber-100 text-amber-700">历史文化</span>
              <span class="tag bg-red-100 text-red-700">数据可视化</span>
            </div>
            <h3 class="card-title">中华上下五千年</h3>
            <p class="card-desc">
              穿越时空，纵览华夏文明五千年兴衰更替。交互式时间轴展示各朝代疆域、重要事件与文化成就。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★☆☆☆☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 中国近代史卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'modern-history', cardTitle: '中国近代史：觉醒之路', cardPath: '/aitech/modern-history', cardCategory: '历史文化' })">
          <div class="card-cover" style="background: linear-gradient(to bottom right, #2c0000, #8b0000);">
            <div class="card-icon">
              <span class="text-4xl">🔥</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-red-100 text-red-700">红色历史</span>
              <span class="tag bg-amber-100 text-amber-700">3D 互动</span>
            </div>
            <h3 class="card-title">中国近代史：觉醒之路</h3>
            <p class="card-desc">
              重走从1840年到1949年的峥嵘岁月。通过3D时空螺旋，沉浸式体验民族觉醒与复兴的伟大历程。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★☆☆☆☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 日本侵华史实卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'japan-invasion-history', cardTitle: '日本侵华史实', cardPath: '/aitech/japan-invasion-history', cardCategory: '历史文化' })">
          <div class="card-cover" style="background: linear-gradient(to bottom right, #000000, #330000);">
            <div class="card-icon">
              <span class="text-4xl">🕯️</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-gray-100 text-gray-700">历史铭记</span>
              <span class="tag bg-red-100 text-red-700">勿忘国耻</span>
            </div>
            <h3 class="card-title">日本侵华史实</h3>
            <p class="card-desc">
              3D 沉浸式回顾从古至今日本侵华重大事件。以史为鉴，面向未来。详细梳理倭寇、甲午、九一八等历史节点。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★☆☆☆☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 丝绸之路卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'silk-road', cardTitle: '丝绸之路：文明纽带', cardPath: '/aitech/silk-road', cardCategory: '历史文化' })">
          <div class="card-cover history-bg" style="background: linear-gradient(to bottom right, #c29a6d, #f0e68c);">
            <div class="card-icon">
              <span class="text-4xl">🐫</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-orange-100 text-orange-700">历史交互</span>
              <span class="tag bg-yellow-100 text-yellow-700">3D 场景</span>
            </div>
            <h3 class="card-title">丝绸之路：文明纽带</h3>
            <p class="card-desc">
              重走千年古道，穿越塔克拉玛干与帕米尔高原。沉浸式体验商队旅程，见证东西方文明的交融与碰撞。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★★☆☆☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 多普勒效应卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'doppler-effect', cardTitle: '多普勒效应：听得见的波', cardPath: '/aitech/doppler-effect', cardCategory: '物理科学' })">
          <div class="card-cover sound-bg">
            <div class="card-icon">
              <span class="text-4xl">🚑</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-red-100 text-red-700">波动物理</span>
              <span class="tag bg-yellow-100 text-yellow-700">声学</span>
            </div>
            <h3 class="card-title">多普勒效应：听得见的波</h3>
            <p class="card-desc">
              为什么救护车驶过时声音会变调？可视化声波的挤压与拉伸，亲耳聆听不同位置的频率变化，甚至突破音障！
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★★☆☆☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 光合作用卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'photosynthesis', cardTitle: '光合作用实验室', cardPath: '/aitech/photosynthesis', cardCategory: '生物科学' })">
          <div class="card-cover bio-bg">
            <div class="card-icon">
              <span class="text-4xl">🌿</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-green-100 text-green-700">生物</span>
              <span class="tag bg-emerald-100 text-emerald-700">微观世界</span>
            </div>
            <h3 class="card-title">光合作用实验室</h3>
            <p class="card-desc">
              进入叶绿体内部，探索植物如何将光能转化为生命的燃料。3D 可视化光反应与暗反应的全过程。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★★☆☆☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 太阳系模拟卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'solar-system', cardTitle: '太阳系探索', cardPath: '/aitech/solar-system', cardCategory: '天文探索' })">
          <div class="card-cover solar-bg">
            <div class="card-icon">
              <el-icon><img src="https://cdn-icons-png.flaticon.com/512/3212/3212567.png" alt="planet" class="w-full h-full object-contain invert brightness-0" /></el-icon>
              <!-- 备用图标，如果图片加载失败 -->
              <span class="text-4xl">🪐</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-blue-100 text-blue-700">3D 模拟</span>
              <span class="tag bg-purple-100 text-purple-700">天文</span>
            </div>
            <h3 class="card-title">太阳系探索</h3>
            <p class="card-desc">
              支持自由视角缩放、旋转，查看八大行星的详细数据与公转轨迹。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★★☆☆☆</span>
              </span>
              <span class="learn-count">1.2k 人已学</span>
            </div>
          </div>
        </div>

        <!-- NPU 模拟卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'npu-simulation', cardTitle: 'NPU 工作原理', cardPath: '/aitech/npu-simulation', cardCategory: '计算机科学' })">
          <div class="card-cover npu-bg">
            <div class="card-icon">
              <span class="text-4xl">⚡</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-green-100 text-green-700">芯片原理</span>
              <span class="tag bg-blue-100 text-blue-700">硬件</span>
            </div>
            <h3 class="card-title">NPU 工作原理</h3>
            <p class="card-desc">
              深入了解神经网络处理单元（NPU）的内部架构。模拟数据流、矩阵运算核心以及高带宽内存的协同工作过程。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★★★☆☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 简谐振动卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'harmonic-oscillator', cardTitle: '能量舞者：简谐振动', cardPath: '/aitech/harmonic-oscillator', cardCategory: '物理科学' })">
          <div class="card-cover physics-bg">
            <div class="card-icon">
              <span class="text-4xl">〰️</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-purple-100 text-purple-700">经典物理</span>
              <span class="tag bg-indigo-100 text-indigo-700">能量守恒</span>
            </div>
            <h3 class="card-title">能量舞者：简谐振动</h3>
            <p class="card-desc">
              探索弹簧振子的优雅运动。实时观察动能与势能的交替转换，通过调整质量与阻尼系数，创造独特的能量舞步。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★☆☆☆☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 双缝干涉卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'double-slit', cardTitle: '光的双缝干涉侦探社', cardPath: '/aitech/double-slit', cardCategory: '光学实验' })">
          <div class="card-cover optics-bg">
            <div class="card-icon">
              <span class="text-4xl">🌈</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-indigo-100 text-indigo-700">量子力学</span>
              <span class="tag bg-pink-100 text-pink-700">波动光学</span>
            </div>
            <h3 class="card-title">光的双缝干涉侦探社</h3>
            <p class="card-desc">
              揭开光的波粒二象性神秘面纱。自由切换波动/粒子模式，调整波长与双缝间距，像侦探一样分析干涉条纹的成因。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★★★★☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 引力弹弓卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'gravity-slingshot', cardTitle: '黑洞“引力弹弓”挑战', cardPath: '/aitech/gravity-slingshot', cardCategory: '天体物理' })">
          <div class="card-cover gravity-bg">
            <div class="card-icon">
              <span class="text-4xl">🕳️</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-gray-100 text-gray-700">天体物理</span>
              <span class="tag bg-orange-100 text-orange-700">广义相对论</span>
            </div>
            <h3 class="card-title">黑洞“引力弹弓”挑战</h3>
            <p class="card-desc">
              驾驶飞船穿越黑洞视界边缘。利用极强引力场进行加速机动，体验动能与势能的惊险转换，目标：抵达遥远星系！
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★★★★★</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 立体几何卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'solid-geometry', cardTitle: '立体几何：空间思维透视镜', cardPath: '/aitech/solid-geometry', cardCategory: '数学几何' })">
          <div class="card-cover geometry-bg">
            <div class="card-icon">
              <span class="text-4xl">🧊</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-blue-100 text-blue-700">立体几何</span>
              <span class="tag bg-indigo-100 text-indigo-700">空间思维</span>
            </div>
            <h3 class="card-title">立体几何：空间思维透视镜</h3>
            <p class="card-desc">
              生成复杂几何体（如十二面体）的3D渲染图。自由旋转观察形态，一键透视内部结构，将抽象的空间关系可视化。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★★☆☆☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 细胞工厂卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'cell-factory', cardTitle: '细胞工厂运输模拟器', cardPath: '/aitech/cell-factory', cardCategory: '生物科学' })">
          <div class="card-cover bio-bg">
            <div class="card-icon">
              <span class="text-4xl">🧬</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-green-100 text-green-700">细胞生物学</span>
              <span class="tag bg-rose-100 text-rose-700">微观世界</span>
            </div>
            <h3 class="card-title">细胞工厂运输模拟器</h3>
            <p class="card-desc">
              化身细胞调度员！见证蛋白质从基因转录到胞吐分泌的全过程。亲手操作内质网折叠与高尔基体分拣。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★★☆☆☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 等高线地形卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'contour-terrain', cardTitle: '等高线与 3D 地貌映射', cardPath: '/aitech/contour-terrain', cardCategory: '地理科学' })">
          <div class="card-cover geo-bg">
            <div class="card-icon">
              <span class="text-4xl">🏔️</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-emerald-100 text-emerald-700">地理科学</span>
              <span class="tag bg-cyan-100 text-cyan-700">空间思维</span>
            </div>
            <h3 class="card-title">等高线与 3D 地貌映射</h3>
            <p class="card-desc">
              直观理解 2D 等高线图与 3D 真实地貌的对应关系。悬停高亮显示切片，调整海平面与地形复杂度，培养空间想象力。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★★☆☆☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 全息手势五子棋卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'hologomoku', cardTitle: '全息手势五子棋', cardPath: '/aitech/hologomoku', cardCategory: '交互游戏' })">
          <div class="card-cover gomoku-bg">
            <div class="card-icon">
              <span class="text-4xl">🖐️</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-cyan-100 text-cyan-700">AI 视觉</span>
              <span class="tag bg-purple-100 text-purple-700">手势交互</span>
            </div>
            <h3 class="card-title">全息手势五子棋</h3>
            <p class="card-desc">
              无需接触屏幕，用手势在空气中下棋！结合 MediaPipe 手部追踪与 Three.js 全息渲染，体验赛博朋克风格的对弈。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★★★☆☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 全息3D相册卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'holo-album', cardTitle: '全息 3D 相册', cardPath: '/aitech/holo-album', cardCategory: '全息互动' })">
          <div class="card-cover album-bg">
            <div class="card-icon">
              <span class="text-4xl">🖼️</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-pink-100 text-pink-700">3D 视觉</span>
              <span class="tag bg-blue-100 text-blue-700">体感操控</span>
            </div>
            <h3 class="card-title">全息 3D 相册</h3>
            <p class="card-desc">
              像钢铁侠一样在空中挥手翻阅照片。3D 环绕式相册，支持手势旋转、缩放与交互，体验未来的媒体浏览方式。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★★☆☆☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 全息地球监控卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'holo-earth', cardTitle: '全息地球航线监控', cardPath: '/aitech/holo-earth', cardCategory: '地理科学' })">
          <div class="card-cover earth-bg">
            <div class="card-icon">
              <span class="text-4xl">🌏</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-blue-100 text-blue-700">全球监控</span>
              <span class="tag bg-cyan-100 text-cyan-700">手势交互</span>
            </div>
            <h3 class="card-title">全息地球航线监控</h3>
            <p class="card-desc">
              上帝视角俯瞰地球，实时追踪全球航线。支持手势旋转地球、捏合缩放，探索数据可视化与全息交互的完美结合。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★★★★☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 地质动力学模拟卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'mantle-earth', cardTitle: '地幔对流与大陆漂移', cardPath: '/aitech/mantle-earth', cardCategory: '地球科学' })">
          <div class="card-cover geo-bg" style="background: linear-gradient(to bottom right, #330000, #ff4500);">
            <div class="card-icon">
              <span class="text-4xl">🌋</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-red-100 text-red-700">地质学</span>
              <span class="tag bg-orange-100 text-orange-700">板块构造</span>
            </div>
            <h3 class="card-title">地幔对流与大陆漂移</h3>
            <p class="card-desc">
              剥开地球表层，直观看到地幔对流如何推动板块移动。滑动时间轴见证亿万年大陆漂移，模拟板块碰撞造山运动。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★★★★☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 星际探索系统卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'stellar-explorer', cardTitle: '星际探索系统', cardPath: '/aitech/solar-system', cardCategory: '航天探索' })">
          <div class="card-cover solar-bg">
            <div class="card-icon">
              <span class="text-4xl">🌌</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-indigo-100 text-indigo-700">天体物理</span>
              <span class="tag bg-violet-100 text-violet-700">3D 可视化</span>
            </div>
            <h3 class="card-title">星际探索系统</h3>
            <p class="card-desc">
              构建完整的3D太阳系，探索八大行星的奥秘。支持自由视角漫游、行星特写聚焦，感受宇宙的宏大与静谧。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★★★★☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>
        
        <!-- 霓虹粉碎室卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'neon-smash', cardTitle: '霓虹粉碎室', cardPath: '/aitech/neon-smash', cardCategory: '物理实验' })">
          <div class="card-cover physics-bg">
            <div class="card-icon">
              <span class="text-4xl">💥</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-fuchsia-100 text-fuchsia-700">物理引擎</span>
              <span class="tag bg-purple-100 text-purple-700">解压破坏</span>
            </div>
            <h3 class="card-title">霓虹粉碎室</h3>
            <p class="card-desc">
              高压生活需要发泄？来这里尽情破坏！发射高能球体，粉碎霓虹立方体塔，享受最真实的物理碰撞与爆炸快感。
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★☆☆☆☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 新春福气卡片 -->
        <div class="tech-card group" @click="handleCardNavigate({ cardKey: 'fortune-festival', cardTitle: '新春福气+发财', cardPath: '/aitech/fortune', cardCategory: '节日活动' })">
          <div class="card-cover fortune-bg">
            <div class="card-icon">
              <span class="text-4xl">🧧</span>
            </div>
            <div class="card-overlay">
              <span class="play-btn">
                <el-icon><VideoPlay /></el-icon> 立即体验
              </span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-tags">
              <span class="tag bg-red-100 text-red-700">中国风</span>
              <span class="tag bg-yellow-100 text-yellow-700">财运</span>
            </div>
            <h3 class="card-title">新春福气+发财</h3>
            <p class="card-desc">
              沉浸式体验中国龙与漫天财运雨。点击接收您的专属祝福，金币与元宝为您起舞，祝您财源广进！
            </p>
            <div class="card-footer">
              <span class="difficulty">
                难度: <span class="stars">★☆☆☆☆</span>
              </span>
              <span class="learn-count">New</span>
            </div>
          </div>
        </div>

        <!-- 更多卡片占位 (示例) -->

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { VideoPlay } from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'
import { useRouter } from 'vue-router'
import { aitechApi } from '@/api'
import type { AiTechCardClickPayload } from '@/types'

const router = useRouter()

const handleCardNavigate = (payload: AiTechCardClickPayload) => {
  router.push(payload.cardPath).catch(() => {})
  aitechApi.recordAiTechCardClick(payload).catch((error: unknown) => {
    if (import.meta.env.DEV) {
      console.debug('Failed to record AiTech card click', error)
    }
  })
}
</script>

<style scoped>
.ai-tech-page {
  min-height: 100vh;
  background-color: #f8fafc;
  padding-top: 2rem;
}

.page-header {
  margin-bottom: 2rem;
}

.tech-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.tech-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.card-cover {
  height: 180px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.solar-bg {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
}

.npu-bg {
  background: linear-gradient(135deg, #052e16 0%, #14532d 100%);
}

.physics-bg {
  background: linear-gradient(135deg, #2e1065 0%, #5b21b6 100%);
}

.optics-bg {
  background: linear-gradient(135deg, #000000 0%, #434343 100%);
}

.gravity-bg {
  background: linear-gradient(135deg, #000000 0%, #1e1e1e 100%);
  position: relative;
}
.gravity-bg::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at center, rgba(255, 165, 0, 0.2) 0%, transparent 70%);
}

.geometry-bg {
  background: linear-gradient(135deg, #172554 0%, #2563eb 100%);
}

.bio-bg {
  background: linear-gradient(135deg, #14532d 0%, #064e3b 100%);
}

.geo-bg {
  background: linear-gradient(135deg, #064e3b 0%, #065f46 100%);
}

.sound-bg {
  background: linear-gradient(135deg, #ef4444 0%, #b91c1c 100%);
}

.history-bg {
  background: linear-gradient(135deg, #8e3e3e 0%, #5c2626 100%);
}

.pray-bg {
  background: linear-gradient(135deg, #e6b980 0%, #eacda3 100%);
}

.gomoku-bg {
  background: linear-gradient(135deg, #000000 0%, #1a1a2e 100%);
}

.album-bg {
  background: linear-gradient(135deg, #2c3e50 0%, #4ca1af 100%);
}

.earth-bg {
  background: linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%);
}

.fortune-bg {
  background: linear-gradient(135deg, #8B0000 0%, #FFD700 100%);
}

.card-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  transition: transform 0.3s ease;
}

.tech-card:hover .card-icon {
  transform: scale(1.1);
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.tech-card:hover .card-overlay {
  opacity: 1;
}

.play-btn {
  color: white;
  font-weight: 600;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  border-radius: 9999px;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.card-body {
  padding: 1.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-tags {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.tag {
  font-size: 0.75rem;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-weight: 500;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 0.5rem;
}

.card-desc {
  font-size: 0.875rem;
  color: #6b7280;
  line-height: 1.5;
  margin-bottom: 1.5rem;
  flex: 1;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
  color: #9ca3af;
  border-top: 1px solid #f3f4f6;
  padding-top: 1rem;
}

.stars {
  color: #fbbf24;
  letter-spacing: 2px;
}
</style>
