<template>
  <div v-if="visible" class="explosion-overlay" @click="close">
    <div class="explosion-container">
      <!-- 粒子画布 -->
      <canvas ref="canvasRef" class="explosion-canvas"></canvas>
      
      <!-- 文字提示 -->
      <div class="success-message">
        <div class="message-icon-wrapper">
          <span class="message-icon">✨</span>
        </div>
        <h2 class="message-title">
          <span class="char" v-for="(char, index) in titleChars" :key="index" :style="{ animationDelay: index * 0.1 + 's' }">
            {{ char === ' ' ? '\u00A0' : char }}
          </span>
        </h2>
        <p class="message-subtitle">欢迎，{{ username }}！</p>
        <p class="message-text">开启美好的一天 🌈</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'

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

const canvasRef = ref(null)
let animationId = null
let particles = []
let explosionCount = 0
const maxExplosions = 3

const titleChars = computed(() => {
  return '登录成功！'.split('')
})

const colors = [
  '#FF6B9D', '#C44569', '#F8B500', '#00D2FF',
  '#4ECDC4', '#95E1D3', '#F38181', '#AA96DA'
]

class Particle {
  constructor(x, y, color) {
    this.x = x
    this.y = y
    this.startX = x
    this.startY = y
    this.color = color
    this.angle = Math.random() * Math.PI * 2
    this.speed = Math.random() * 5 + 3
    this.life = 1.0
    this.decay = Math.random() * 0.02 + 0.01
    this.size = Math.random() * 4 + 2
    this.distance = 0
    this.maxDistance = Math.random() * 100 + 50
  }

  update() {
    this.distance += this.speed
    this.x = this.startX + Math.cos(this.angle) * this.distance
    this.y = this.startY + Math.sin(this.angle) * this.distance
    this.life -= this.decay
    
    // 添加重力效果
    this.y += 0.3
  }

  draw(ctx) {
    if (this.life <= 0) return
    
    ctx.save()
    ctx.globalAlpha = this.life
    ctx.fillStyle = this.color
    ctx.shadowBlur = 10
    ctx.shadowColor = this.color
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fill()
    ctx.restore()
  }
}

const createExplosion = () => {
  if (!canvasRef.value || explosionCount >= maxExplosions) return
  
  const canvas = canvasRef.value
  const centerX = canvas.width / 2
  const centerY = canvas.height / 2
  
  // 创建爆炸粒子
  const particleCount = 50
  for (let i = 0; i < particleCount; i++) {
    const color = colors[Math.floor(Math.random() * colors.length)]
    particles.push(new Particle(centerX, centerY, color))
  }
  
  explosionCount++
  
  // 延迟创建下一个爆炸
  if (explosionCount < maxExplosions) {
    setTimeout(() => {
      createExplosion()
    }, 400)
  }
}

const animate = () => {
  if (!canvasRef.value) return
  
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  
  // 清除画布，使用半透明黑色实现拖尾效果
  ctx.fillStyle = 'rgba(0, 0, 0, 0.15)'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  
  // 更新和绘制粒子
  for (let i = particles.length - 1; i >= 0; i--) {
    const particle = particles[i]
    particle.update()
    particle.draw(ctx)
    
    if (particle.life <= 0 || particle.distance > particle.maxDistance) {
      particles.splice(i, 1)
    }
  }
  
  // 如果还有粒子，继续动画
  if (particles.length > 0) {
    animationId = requestAnimationFrame(animate)
  } else if (explosionCount >= maxExplosions) {
    // 所有爆炸完成，延迟关闭
    setTimeout(() => {
      close()
    }, 1500)
  }
}

const initCanvas = () => {
  if (!canvasRef.value) return
  
  const canvas = canvasRef.value
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  
  // 重置状态
  particles = []
  explosionCount = 0
  
  // 创建第一个爆炸
  createExplosion()
  animate()
}

const close = () => {
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
  particles = []
  explosionCount = 0
  emit('close')
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    setTimeout(() => {
      initCanvas()
    }, 100)
  } else {
    if (animationId) {
      cancelAnimationFrame(animationId)
      animationId = null
    }
    particles = []
    explosionCount = 0
  }
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
})
</script>

<style scoped>
.explosion-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.explosion-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.explosion-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.success-message {
  position: relative;
  z-index: 10;
  text-align: center;
  color: white;
}

.message-icon-wrapper {
  margin-bottom: 30px;
  animation: rotate 1s ease-in-out;
}

@keyframes rotate {
  0% {
    transform: rotate(0deg) scale(0);
  }
  50% {
    transform: rotate(180deg) scale(1.2);
  }
  100% {
    transform: rotate(360deg) scale(1);
  }
}

.message-icon {
  font-size: 100px;
  display: inline-block;
  animation: sparkle 2s ease-in-out infinite;
}

@keyframes sparkle {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}

.message-title {
  font-size: 56px;
  font-weight: bold;
  margin: 0 0 20px 0;
  text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  gap: 5px;
}

.char {
  display: inline-block;
  animation: charBounce 0.6s ease-out both;
}

@keyframes charBounce {
  0% {
    opacity: 0;
    transform: translateY(-20px) scale(0.5);
  }
  50% {
    transform: translateY(5px) scale(1.1);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.message-subtitle {
  font-size: 28px;
  margin: 0 0 15px 0;
  color: #FFD700;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
  animation: slideIn 0.8s ease-out 0.6s both;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.message-text {
  font-size: 20px;
  margin: 0;
  opacity: 0.95;
  animation: slideIn 0.8s ease-out 0.8s both;
}
</style>