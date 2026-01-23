<template>
  <div v-if="visible" class="star-overlay" @click="close">
    <div class="star-container">
      <!-- 星星背景 -->
      <div class="stars-background">
        <div 
          v-for="(star, index) in stars" 
          :key="index"
          class="star"
          :style="star.style"
        ></div>
      </div>
      
      <!-- 文字提示 -->
      <div class="success-message">
        <div class="message-icon-wrapper">
          <span class="message-icon">🌟</span>
        </div>
        <h2 class="message-title">
          <span 
            v-for="(char, index) in titleChars" 
            :key="index" 
            class="char"
            :style="{ animationDelay: index * 0.08 + 's' }"
          >
            {{ char === ' ' ? '\u00A0' : char }}
          </span>
        </h2>
        <p class="message-subtitle">你好，{{ username }}！</p>
        <p class="message-text">愿今天充满阳光与希望 ☀️</p>
        <div class="confetti">
          <div 
            v-for="(confetti, index) in confettis" 
            :key="index"
            class="confetti-piece"
            :style="confetti.style"
          ></div>
        </div>
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

const titleChars = computed(() => {
  return '登录成功！'.split('')
})

const stars = ref([])
const confettis = ref([])
let starAnimationId = null
let confettiAnimationId = null

const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#98D8C8', '#F7DC6F']

const createStars = () => {
  stars.value = []
  for (let i = 0; i < 50; i++) {
    stars.value.push({
      style: {
        left: Math.random() * 100 + '%',
        top: Math.random() * 100 + '%',
        animationDelay: Math.random() * 2 + 's',
        animationDuration: (Math.random() * 2 + 1) + 's'
      }
    })
  }
}

const createConfetti = () => {
  confettis.value = []
  for (let i = 0; i < 30; i++) {
    const color = colors[Math.floor(Math.random() * colors.length)]
    confettis.value.push({
      style: {
        left: Math.random() * 100 + '%',
        backgroundColor: color,
        animationDelay: Math.random() * 0.5 + 's',
        animationDuration: (Math.random() * 2 + 2) + 's'
      }
    })
  }
}

const animateStars = () => {
  // 星星闪烁动画由CSS处理
}

const close = () => {
  if (starAnimationId) {
    cancelAnimationFrame(starAnimationId)
    starAnimationId = null
  }
  if (confettiAnimationId) {
    cancelAnimationFrame(confettiAnimationId)
    confettiAnimationId = null
  }
  emit('close')
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    createStars()
    createConfetti()
    animateStars()
  } else {
    stars.value = []
    confettis.value = []
  }
})

onUnmounted(() => {
  if (starAnimationId) {
    cancelAnimationFrame(starAnimationId)
  }
  if (confettiAnimationId) {
    cancelAnimationFrame(confettiAnimationId)
  }
})
</script>

<style scoped>
.star-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.5s ease-in;
  overflow: hidden;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.star-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stars-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.star {
  position: absolute;
  width: 3px;
  height: 3px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 0 6px white;
  animation: twinkle infinite;
}

@keyframes twinkle {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.5);
  }
}

.success-message {
  position: relative;
  z-index: 10;
  text-align: center;
  color: white;
  padding: 40px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.message-icon-wrapper {
  margin-bottom: 25px;
  animation: iconFloat 2s ease-in-out infinite;
}

@keyframes iconFloat {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-10px) rotate(10deg);
  }
}

.message-icon {
  font-size: 90px;
  display: inline-block;
  filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.5));
}

.message-title {
  font-size: 52px;
  font-weight: bold;
  margin: 0 0 20px 0;
  text-shadow: 0 0 20px rgba(255, 255, 255, 0.5), 2px 2px 4px rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  gap: 3px;
}

.char {
  display: inline-block;
  animation: charGlow 0.8s ease-out both;
}

@keyframes charGlow {
  0% {
    opacity: 0;
    transform: scale(0) rotate(180deg);
    text-shadow: 0 0 0 rgba(255, 255, 255, 0);
  }
  50% {
    transform: scale(1.2) rotate(0deg);
  }
  100% {
    opacity: 1;
    transform: scale(1) rotate(0deg);
    text-shadow: 0 0 20px rgba(255, 255, 255, 0.5);
  }
}

.message-subtitle {
  font-size: 26px;
  margin: 0 0 12px 0;
  color: #FFD700;
  text-shadow: 0 0 15px rgba(255, 215, 0, 0.6);
  animation: fadeInUp 1s ease-out 0.8s both;
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

.message-text {
  font-size: 18px;
  margin: 0;
  opacity: 0.9;
  animation: fadeInUp 1s ease-out 1s both;
}

.confetti {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
}

.confetti-piece {
  position: absolute;
  width: 10px;
  height: 10px;
  top: -10px;
  border-radius: 2px;
  animation: confettiFall linear;
}

@keyframes confettiFall {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translateY(100vh) rotate(720deg);
    opacity: 0;
  }
}
</style>