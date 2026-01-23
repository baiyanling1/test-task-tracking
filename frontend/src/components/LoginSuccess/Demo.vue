<template>
  <div class="demo-container">
    <h1 class="demo-title">登录成功提示效果演示</h1>
    <p class="demo-subtitle">点击下方按钮查看不同效果，选择您喜欢的方案</p>
    
    <div class="demo-buttons">
      <el-button 
        type="primary" 
        size="large" 
        @click="showEffect('classic')"
        class="demo-button"
      >
        🎆 方案一：经典烟花
      </el-button>
      
      <el-button 
        type="success" 
        size="large" 
        @click="showEffect('particle')"
        class="demo-button"
      >
        ✨ 方案二：粒子爆炸
      </el-button>
      
      <el-button 
        type="warning" 
        size="large" 
        @click="showEffect('star')"
        class="demo-button"
      >
        🌟 方案三：星星闪烁
      </el-button>
    </div>

    <div class="demo-descriptions">
      <div class="description-card">
        <h3>方案一：经典烟花</h3>
        <p>从中心向四周散开的彩色烟花效果，经典优雅，适合正式场合</p>
        <ul>
          <li>多色烟花同时绽放</li>
          <li>重力效果自然</li>
          <li>动画流畅</li>
        </ul>
      </div>
      
      <div class="description-card">
        <h3>方案二：粒子爆炸</h3>
        <p>粒子从中心爆炸散开，充满活力，适合年轻化团队</p>
        <ul>
          <li>连续多次爆炸效果</li>
          <li>粒子拖尾效果</li>
          <li>渐变背景</li>
        </ul>
      </div>
      
      <div class="description-card">
        <h3>方案三：星星闪烁</h3>
        <p>星空背景配合闪烁星星和彩纸，浪漫温馨，适合创意团队</p>
        <ul>
          <li>星空背景</li>
          <li>星星闪烁动画</li>
          <li>彩纸飘落效果</li>
        </ul>
      </div>
    </div>

    <!-- 效果组件 -->
    <ClassicFireworks 
      v-if="currentEffect === 'classic'"
      :visible="showClassic"
      username="演示用户"
      @close="showClassic = false"
    />
    
    <ParticleExplosion 
      v-if="currentEffect === 'particle'"
      :visible="showParticle"
      username="演示用户"
      @close="showParticle = false"
    />
    
    <StarSparkle 
      v-if="currentEffect === 'star'"
      :visible="showStar"
      username="演示用户"
      @close="showStar = false"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ClassicFireworks from './ClassicFireworks.vue'
import ParticleExplosion from './ParticleExplosion.vue'
import StarSparkle from './StarSparkle.vue'

const currentEffect = ref('')
const showClassic = ref(false)
const showParticle = ref(false)
const showStar = ref(false)

const showEffect = (type) => {
  currentEffect.value = type
  if (type === 'classic') {
    showClassic.value = true
  } else if (type === 'particle') {
    showParticle.value = true
  } else if (type === 'star') {
    showStar.value = true
  }
}
</script>

<style scoped>
.demo-container {
  min-height: 100vh;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.demo-title {
  color: white;
  font-size: 36px;
  margin-bottom: 10px;
  text-align: center;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.demo-subtitle {
  color: rgba(255, 255, 255, 0.9);
  font-size: 18px;
  margin-bottom: 40px;
  text-align: center;
}

.demo-buttons {
  display: flex;
  gap: 20px;
  margin-bottom: 60px;
  flex-wrap: wrap;
  justify-content: center;
}

.demo-button {
  min-width: 200px;
  height: 50px;
  font-size: 16px;
  font-weight: bold;
}

.demo-descriptions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  max-width: 1200px;
  width: 100%;
}

.description-card {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.description-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
}

.description-card h3 {
  color: #333;
  font-size: 24px;
  margin-bottom: 15px;
  border-bottom: 2px solid #667eea;
  padding-bottom: 10px;
}

.description-card p {
  color: #666;
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 15px;
}

.description-card ul {
  list-style: none;
  padding: 0;
}

.description-card ul li {
  color: #555;
  font-size: 14px;
  padding: 8px 0;
  padding-left: 25px;
  position: relative;
}

.description-card ul li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #4ECDC4;
  font-weight: bold;
  font-size: 18px;
}

@media (max-width: 768px) {
  .demo-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .demo-button {
    width: 100%;
    max-width: 300px;
  }
  
  .demo-descriptions {
    grid-template-columns: 1fr;
  }
}
</style>