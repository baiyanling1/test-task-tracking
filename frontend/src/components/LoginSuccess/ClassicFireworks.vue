<template>
  <div v-if="visible" class="fireworks-overlay" @click="handleClick">
    <!-- 闪烁星星背景 -->
    <div class="stars-background">
      <div 
        v-for="(star, index) in stars" 
        :key="index"
        class="star"
        :style="star.style"
      ></div>
    </div>

    <div class="fireworks-container">
      <!-- 烟花画布 -->
      <canvas ref="canvasRef" class="fireworks-canvas"></canvas>
      
      <!-- 文字提示 -->
      <div class="success-message">
        <h1 class="main-title">登录成功！</h1>
        <p class="encouragement-text">看来任务有进度了，加油！</p>
        <p class="welcome-text">欢迎回来，{{ username }}！</p>
        <p class="hint-text">点击屏幕或等待5秒自动进入</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  username: {
    type: String,
    default: '朋友'
  }
})

const emit = defineEmits(['close'])

// 调试：监听props变化
watch(() => props.visible, (newVal) => {
  console.log('[Fireworks] visible changed to:', newVal)
}, { immediate: true })

const canvasRef = ref(null)
let animationId = null
let particles = []
let fireworks = []
let autoCloseTimer = null
const stars = ref([])

// 更丰富的颜色
const colors = [
  '#FF6B6B', '#FF8E53', '#FF6B9D', '#C44569', '#F8B500',
  '#00D2FF', '#4ECDC4', '#95E1D3', '#F38181', '#AA96DA',
  '#FFD93D', '#6BCB77', '#FF6B6B', '#4ECDC4', '#45B7D1',
  '#FFA07A', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E2'
]

// 创建闪烁星星
const createStars = () => {
  stars.value = []
  for (let i = 0; i < 100; i++) {
    stars.value.push({
      style: {
        left: Math.random() * 100 + '%',
        top: Math.random() * 100 + '%',
        animationDelay: Math.random() * 3 + 's',
        animationDuration: (Math.random() * 2 + 1.5) + 's',
        width: (Math.random() * 3 + 1) + 'px',
        height: (Math.random() * 3 + 1) + 'px'
      }
    })
  }
}

class Particle {
  constructor(x, y, color) {
    this.x = x
    this.y = y
    // 全屏散开，速度更快
    const angle = Math.random() * Math.PI * 2
    const speed = 10 + Math.random() * 15 // 进一步增加速度
    this.vx = Math.cos(angle) * speed
    this.vy = Math.sin(angle) * speed
    this.life = 1.0
    this.decay = Math.random() * 0.018 + 0.012 // 稍微减慢消失，让效果持续更久
    this.color = color || colors[Math.floor(Math.random() * colors.length)]
    this.size = Math.random() * 3 + 1.5 // 减小粒子大小
    this.gravity = 0.12 // 进一步减少重力，让粒子飞得更远
  }

  update() {
    this.x += this.vx
    this.y += this.vy
    this.vy += this.gravity
    this.life -= this.decay
    this.vx *= 0.98
    this.vy *= 0.98
  }

  draw(ctx) {
    if (this.life <= 0) return
    
    ctx.save()
    ctx.globalAlpha = this.life
    
    // 发光效果 - 稍微小一点
    ctx.shadowBlur = 15
    ctx.shadowColor = this.color
    
    // 渐变填充
    const gradient = ctx.createRadialGradient(this.x, this.y, 0, this.x, this.y, this.size * 2)
    gradient.addColorStop(0, this.color)
    gradient.addColorStop(0.5, this.color + 'CC')
    gradient.addColorStop(1, this.color + '00')
    
    ctx.fillStyle = gradient
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fill()
    ctx.restore()
  }
}

class Firework {
  constructor(startX, startY, targetX, targetY) {
    this.x = startX
    this.y = startY
    this.startX = startX
    this.startY = startY
    this.targetX = targetX
    this.targetY = targetY
    this.exploded = false
    this.color = colors[Math.floor(Math.random() * colors.length)]
    
    // 计算速度 - 加快速度
    const dx = targetX - startX
    const dy = targetY - startY
    const distance = Math.sqrt(dx * dx + dy * dy)
    const speed = 18 + Math.random() * 10 // 进一步加快速度
    this.vx = (dx / distance) * speed
    this.vy = (dy / distance) * speed
    
    // 轨迹点
    this.trail = []
  }

  update() {
    if (!this.exploded) {
      // 记录轨迹
      this.trail.push({ x: this.x, y: this.y })
      if (this.trail.length > 8) {
        this.trail.shift()
      }
      
      this.x += this.vx
      this.y += this.vy
      
      // 检查是否到达目标
      const dx = this.targetX - this.x
      const dy = this.targetY - this.y
      const distance = Math.sqrt(dx * dx + dy * dy)
      
      if (distance < 20) {
        this.exploded = true
        return true // 需要爆炸
      }
    }
    return false
  }

  draw(ctx) {
    if (!this.exploded) {
      // 绘制轨迹 - 稍微细一点
      ctx.save()
      ctx.strokeStyle = this.color
      ctx.lineWidth = 1.5
      ctx.shadowBlur = 8
      ctx.shadowColor = this.color
      
      if (this.trail.length > 1) {
        ctx.beginPath()
        ctx.moveTo(this.trail[0].x, this.trail[0].y)
        for (let i = 1; i < this.trail.length; i++) {
          ctx.lineTo(this.trail[i].x, this.trail[i].y)
        }
        ctx.stroke()
      }
      
      // 绘制烟花主体
      ctx.fillStyle = this.color
      ctx.beginPath()
      ctx.arc(this.x, this.y, 4, 0, Math.PI * 2)
      ctx.fill()
      ctx.restore()
    }
  }

  explode() {
    const particles = []
    // 减少粒子数量，避免覆盖文字
    const particleCount = 40 + Math.floor(Math.random() * 30)
    
    for (let i = 0; i < particleCount; i++) {
      particles.push(new Particle(this.x, this.y, this.color))
    }
    
    return particles
  }
}

const animate = () => {
  if (!canvasRef.value || !props.visible) {
    console.log('[Fireworks] Animation stopped: canvasRef=', !!canvasRef.value, 'visible=', props.visible)
    return
  }
  
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  
  // 清除画布（保留拖尾效果，但更淡以便看到更多烟花）
  ctx.fillStyle = 'rgba(0, 0, 0, 0.12)'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  
  // 在中心文字区域绘制半透明遮罩，让文字更清晰
  const centerX = canvas.width / 2
  const centerY = canvas.height / 2
  const gradient = ctx.createRadialGradient(centerX, centerY, 0, centerX, centerY, 250)
  gradient.addColorStop(0, 'rgba(0, 0, 0, 0.4)')
  gradient.addColorStop(0.5, 'rgba(0, 0, 0, 0.2)')
  gradient.addColorStop(1, 'rgba(0, 0, 0, 0)')
  ctx.fillStyle = gradient
  ctx.fillRect(centerX - 300, centerY - 200, 600, 400)
  
  // 更新和绘制烟花
  for (let i = fireworks.length - 1; i >= 0; i--) {
    const firework = fireworks[i]
    firework.draw(ctx)
    
    if (firework.update()) {
      // 烟花到达目标，爆炸
      const newParticles = firework.explode()
      particles.push(...newParticles)
      fireworks.splice(i, 1)
    }
  }
  
  // 更新和绘制粒子
  for (let i = particles.length - 1; i >= 0; i--) {
    const particle = particles[i]
    particle.update()
    particle.draw(ctx)
    
    if (particle.life <= 0) {
      particles.splice(i, 1)
    }
  }
  
  // 继续动画（只要visible为true就继续）
  if (props.visible) {
    animationId = requestAnimationFrame(animate)
  }
}

// 从中心向四周发射的烟花
const createCenterFirework = () => {
  if (!canvasRef.value || !props.visible) {
    return
  }
  
  const canvas = canvasRef.value
  const centerX = canvas.width / 2
  const centerY = canvas.height / 2
  
  // 从中心向四周随机方向发射
  const angle = Math.random() * Math.PI * 2
  const distance = 200 + Math.random() * 300 // 目标距离中心的距离
  const targetX = centerX + Math.cos(angle) * distance
  const targetY = centerY + Math.sin(angle) * distance
  
  const firework = new Firework(centerX, centerY, targetX, targetY)
  fireworks.push(firework)
}

// 全屏分布，原地绽放的烟花（避开中心文字区域）
const createDistributedFirework = () => {
  if (!canvasRef.value || !props.visible) {
    return
  }
  
  const canvas = canvasRef.value
  const centerX = canvas.width / 2
  const centerY = canvas.height / 2
  
  // 在全屏随机位置创建，但避开中心区域（文字区域）
  let x, y
  let attempts = 0
  do {
    x = Math.random() * canvas.width
    y = Math.random() * canvas.height
    // 避开中心区域（文字区域大约在中心300x400范围内）
    const distanceFromCenter = Math.sqrt(
      Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)
    )
    attempts++
    // 如果距离中心太近，重新生成位置
    if (distanceFromCenter < 200 || attempts > 10) {
      break
    }
  } while (distanceFromCenter < 200 && attempts < 10)
  
  // 直接创建粒子，不需要发射过程
  const color = colors[Math.floor(Math.random() * colors.length)]
  const particleCount = 35 + Math.floor(Math.random() * 25) // 减少粒子数量
  
  for (let i = 0; i < particleCount; i++) {
    particles.push(new Particle(x, y, color))
  }
}

let fireworkInterval = null

const initCanvas = () => {
  console.log('[Fireworks] initCanvas called, canvasRef:', canvasRef.value)
  if (!canvasRef.value) {
    console.error('[Fireworks] Canvas ref not available!')
    return
  }
  
  const canvas = canvasRef.value
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  
  console.log('[Fireworks] Canvas initialized:', canvas.width, 'x', canvas.height)
  
  // 立即创建6个从中心向四周发射的烟花
  for (let i = 0; i < 6; i++) {
    setTimeout(() => {
      createCenterFirework()
    }, i * 100)
  }
  
  // 立即在全屏创建少量原地绽放的烟花（避开中心区域）
  for (let i = 0; i < 8; i++) {
    setTimeout(() => {
      createDistributedFirework()
    }, i * 80)
  }
  
  // 启动动画
  console.log('[Fireworks] Starting animation...')
  animate()
  
  // 持续创建新烟花 - 减少频率
  fireworkInterval = setInterval(() => {
    if (!props.visible || !canvasRef.value) {
      if (fireworkInterval) {
        clearInterval(fireworkInterval)
        fireworkInterval = null
      }
      return
    }
    
    // 每300ms创建新烟花，概率降低
    if (Math.random() < 0.5) {
      // 50%概率从中心发射
      createCenterFirework()
    } else if (Math.random() < 0.3) {
      // 30%概率全屏分布原地绽放
      createDistributedFirework()
    }
  }, 300)
  
  // 5秒后自动关闭
  console.log('[Fireworks] Setting 5 second auto-close timer...')
  autoCloseTimer = setTimeout(() => {
    console.log('[Fireworks] Auto close triggered after 5 seconds')
    if (fireworkInterval) {
      clearInterval(fireworkInterval)
      fireworkInterval = null
    }
    close()
  }, 5000)
}

const handleClick = () => {
  if (autoCloseTimer) {
    clearTimeout(autoCloseTimer)
    autoCloseTimer = null
  }
  close()
}

const close = () => {
  console.log('[Fireworks] Closing fireworks...')
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
  if (autoCloseTimer) {
    clearTimeout(autoCloseTimer)
    autoCloseTimer = null
  }
  if (fireworkInterval) {
    clearInterval(fireworkInterval)
    fireworkInterval = null
  }
  particles = []
  fireworks = []
  console.log('[Fireworks] Emitting close event...')
  emit('close')
}

watch(() => props.visible, (newVal) => {
  console.log('Watch triggered, visible:', newVal)
  if (newVal) {
    console.log('Fireworks visible, initializing...')
    createStars()
    // 等待DOM更新后初始化canvas
    nextTick(() => {
      console.log('nextTick executed, canvasRef:', canvasRef.value)
      // 确保canvas元素已经渲染
      if (canvasRef.value) {
        console.log('Canvas ref available, initializing...')
        setTimeout(() => {
          initCanvas()
        }, 100)
      } else {
        console.warn('Canvas ref not available in nextTick, retrying...')
        // 如果还没准备好，再等一会儿
        setTimeout(() => {
          if (canvasRef.value) {
            console.log('Canvas ref available on retry, initializing...')
            initCanvas()
          } else {
            console.error('Canvas ref still not available after retry')
          }
        }, 300)
      }
    })
  } else {
    console.log('Fireworks hidden, closing...')
    close()
  }
}, { immediate: true })

onMounted(() => {
  console.log('[Fireworks] Component mounted, visible:', props.visible)
  if (props.visible) {
    console.log('[Fireworks] Already visible on mount, initializing...')
    createStars()
    nextTick(() => {
      if (canvasRef.value) {
        setTimeout(() => {
          initCanvas()
        }, 100)
      }
    })
  }
})

onUnmounted(() => {
  console.log('[Fireworks] Component unmounting...')
  close()
})
</script>

<style scoped>
.fireworks-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, #0a0a0a 0%, #1a1a2e 50%, #16213e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.5s ease-in;
  overflow: hidden;
  cursor: pointer;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.stars-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.star {
  position: absolute;
  background: white;
  border-radius: 50%;
  box-shadow: 0 0 10px white, 0 0 20px white;
  animation: twinkle infinite;
}

@keyframes twinkle {
  0%, 100% {
    opacity: 0.2;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.5);
  }
}

.fireworks-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
}

.fireworks-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.success-message {
  position: relative;
  z-index: 100;
  text-align: center;
  animation: slideUp 0.8s ease-out;
  pointer-events: none;
  background: rgba(0, 0, 0, 0.3);
  padding: 40px 60px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.main-title {
  font-size: 72px;
  font-weight: 900;
  margin: 0 0 30px 0;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 25%, #FFD93D 50%, #6BCB77 75%, #4ECDC4 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: titleGlow 2s ease-in-out infinite, titleScale 0.8s ease-out 0.3s both;
  letter-spacing: 4px;
  filter: drop-shadow(0 0 20px rgba(255, 107, 107, 0.8));
}

@keyframes titleGlow {
  0%, 100% {
    filter: drop-shadow(0 0 20px rgba(255, 107, 107, 0.8)) drop-shadow(0 0 40px rgba(255, 142, 83, 0.6));
  }
  50% {
    filter: drop-shadow(0 0 30px rgba(255, 107, 107, 1)) drop-shadow(0 0 60px rgba(255, 142, 83, 0.8));
  }
}

@keyframes titleScale {
  from {
    opacity: 0;
    transform: scale(0.5) rotate(-5deg);
  }
  50% {
    transform: scale(1.1) rotate(2deg);
  }
  to {
    opacity: 1;
    transform: scale(1) rotate(0deg);
  }
}

.encouragement-text {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 20px 0;
  color: #FFD700;
  text-shadow: 
    0 0 10px rgba(255, 215, 0, 0.8),
    0 0 20px rgba(255, 215, 0, 0.6),
    0 0 30px rgba(255, 215, 0, 0.4),
    2px 2px 4px rgba(0, 0, 0, 0.5);
  animation: encouragementPulse 1.5s ease-in-out infinite, fadeInUp 0.8s ease-out 0.5s both;
  letter-spacing: 2px;
}

@keyframes encouragementPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.9;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.welcome-text {
  font-size: 24px;
  margin: 0 0 15px 0;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 
    0 0 10px rgba(255, 255, 255, 0.5),
    1px 1px 2px rgba(0, 0, 0, 0.3);
  animation: fadeInUp 0.8s ease-out 0.7s both;
}

.hint-text {
  font-size: 16px;
  margin: 0;
  color: rgba(255, 255, 255, 0.6);
  animation: fadeInUp 0.8s ease-out 0.9s both;
}

@media (max-width: 768px) {
  .main-title {
    font-size: 48px;
  }
  
  .encouragement-text {
    font-size: 28px;
  }
  
  .welcome-text {
    font-size: 20px;
  }
  
  .hint-text {
    font-size: 14px;
  }
}
</style>