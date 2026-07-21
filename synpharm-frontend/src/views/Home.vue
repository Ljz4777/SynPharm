<template>
  <div class="home">
    <header class="home__header">
      <div class="home__header-content">
        <div class="home__logo">
          <span class="home__logo-icon">🧬</span>
          <span class="home__logo-text">SynPharm</span>
        </div>
        <nav class="home__nav">
          <a href="#features" class="home__nav-link">核心功能</a>
          <a href="#architecture" class="home__nav-link">技术架构</a>
          <a href="#demo" class="home__nav-link">在线演示</a>
        </nav>
        <div class="home__header-actions">
          <router-link to="/login" class="home__btn home__btn--outline">登录</router-link>
          <router-link to="/register" class="home__btn home__btn--primary">注册</router-link>
        </div>
      </div>
    </header>
    
    <section class="home__hero">
      <div class="home__hero-content">
        <div class="home__hero-text">
          <h1 class="home__hero-title">智能药物研发预测平台</h1>
          <p class="home__hero-desc">基于多智能体协同技术，精准预测分子互作，加速药物发现进程</p>
          <div class="home__hero-actions">
            <router-link to="/predict" class="home__btn home__btn--primary home__btn--large">开始预测</router-link>
            <a href="#features" class="home__btn home__btn--outline home__btn--large">了解更多</a>
          </div>
          <div class="home__hero-stats">
            <div class="home__stat">
              <span class="home__stat-value">10万+</span>
              <span class="home__stat-label">预测任务</span>
            </div>
            <div class="home__stat">
              <span class="home__stat-value">95%</span>
              <span class="home__stat-label">准确率</span>
            </div>
            <div class="home__stat">
              <span class="home__stat-value">500+</span>
              <span class="home__stat-label">支持靶点</span>
            </div>
          </div>
        </div>
        <div class="home__hero-visual">
          <div class="home__molecule">
            <svg viewBox="0 0 300 300" class="home__molecule-svg">
              <circle cx="150" cy="50" r="15" fill="#1e293b"/>
              <circle cx="80" cy="120" r="12" fill="#334155"/>
              <circle cx="220" cy="120" r="12" fill="#334155"/>
              <circle cx="100" cy="200" r="10" fill="#475569"/>
              <circle cx="200" cy="200" r="10" fill="#475569"/>
              <line x1="150" y1="50" x2="80" y2="120" stroke="#1e293b" stroke-width="2" opacity="0.25"/>
              <line x1="150" y1="50" x2="220" y2="120" stroke="#1e293b" stroke-width="2" opacity="0.25"/>
              <line x1="80" y1="120" x2="100" y2="200" stroke="#1e293b" stroke-width="2" opacity="0.25"/>
              <line x1="220" y1="120" x2="200" y2="200" stroke="#1e293b" stroke-width="2" opacity="0.25"/>
              <line x1="100" y1="200" x2="200" y2="200" stroke="#1e293b" stroke-width="2" opacity="0.25"/>
              <circle cx="150" cy="150" r="8" fill="#3b82f6" opacity="0.7"/>
              <circle cx="150" cy="150" r="22" fill="#3b82f6" opacity="0.08"/>
            </svg>
          </div>
        </div>
      </div>
    </section>
    
    <section id="features" class="home__features">
      <div class="home__section-header">
        <h2 class="home__section-title">核心功能</h2>
        <p class="home__section-desc">多维度分子互作预测能力</p>
      </div>
      <div class="home__features-grid">
        <div class="home__feature-card">
          <div class="home__feature-icon">🔬</div>
          <h3 class="home__feature-title">PPI预测</h3>
          <p class="home__feature-desc">蛋白质-蛋白质相互作用预测，揭示细胞内分子机制</p>
        </div>
        <div class="home__feature-card">
          <div class="home__feature-icon">💊</div>
          <h3 class="home__feature-title">DTI预测</h3>
          <p class="home__feature-desc">药物-靶点相互作用预测，加速药物发现进程</p>
        </div>
        <div class="home__feature-card">
          <div class="home__feature-icon">⚗️</div>
          <h3 class="home__feature-title">DDI预测</h3>
          <p class="home__feature-desc">药物-药物相互作用预测，评估用药安全性</p>
        </div>
        <div class="home__feature-card">
          <div class="home__feature-icon">🎯</div>
          <h3 class="home__feature-title">多格式输入</h3>
          <p class="home__feature-desc">支持PDB ID、UniProt ID、SMILES、CSV等多种格式</p>
        </div>
        <div class="home__feature-card">
          <div class="home__feature-icon">3️⃣D</div>
          <h3 class="home__feature-title">3D可视化</h3>
          <p class="home__feature-desc">交互式分子结构可视化，直观展示结合模式</p>
        </div>
        <div class="home__feature-card">
          <div class="home__feature-icon">📊</div>
          <h3 class="home__feature-title">批量处理</h3>
          <p class="home__feature-desc">支持大规模化合物库筛选，提升研发效率</p>
        </div>
      </div>
    </section>
    
    <section id="architecture" class="home__architecture">
      <div class="home__section-header">
        <h2 class="home__section-title">技术架构</h2>
        <p class="home__section-desc">模块化设计，灵活扩展</p>
      </div>
      <div class="home__architecture-diagram">
        <div class="home__architecture-box">
          <span class="home__architecture-label">用户界面层</span>
          <span class="home__architecture-content">Web前端 · 小程序 · API接口</span>
        </div>
        <div class="home__architecture-box">
          <span class="home__architecture-label">业务逻辑层</span>
          <span class="home__architecture-content">任务管理 · 结果分析 · 数据处理</span>
        </div>
        <div class="home__architecture-box">
          <span class="home__architecture-label">AI预测层</span>
          <span class="home__architecture-content">PPI模型 · DTI模型 · DDI模型</span>
        </div>
        <div class="home__architecture-box">
          <span class="home__architecture-label">数据存储层</span>
          <span class="home__architecture-content">分子数据库 · 预测结果 · 用户数据</span>
        </div>
      </div>
    </section>
    
    <section id="demo" class="home__demo">
      <div class="home__section-header">
        <h2 class="home__section-title">快速预测演示</h2>
        <p class="home__section-desc">体验AI预测能力</p>
      </div>
      <div class="home__demo-card">
        <div class="home__demo-form">
          <div class="home__demo-input-group">
            <label class="home__demo-label">输入类型</label>
            <select v-model="inputType" class="home__demo-select">
              <option value="pdb">PDB ID</option>
              <option value="smiles">SMILES</option>
            </select>
          </div>
          <div class="home__demo-input-group">
            <label class="home__demo-label">输入值</label>
            <input 
              v-model="inputValue" 
              type="text" 
              class="home__demo-input"
              :placeholder="inputType === 'pdb' ? '如: 6M0J' : '如: C(=O)(C(=O)O)NC(CCC(=O)O)C(=O)O'"
            />
          </div>
          <button 
            @click="handleDemoPredict" 
            class="home__btn home__btn--primary home__btn--full"
            :disabled="isLoading"
          >
            {{ isLoading ? '预测中...' : '开始预测' }}
          </button>
        </div>
        <div v-if="demoResult" class="home__demo-result">
          <div class="home__demo-result-header">预测结果</div>
          <div class="home__demo-result-content">
            <div class="home__demo-result-item">
              <span class="home__demo-result-label">结合亲和力</span>
              <span class="home__demo-result-value">{{ demoResult.bindingAffinity.toFixed(2) }} kcal/mol</span>
            </div>
            <div class="home__demo-result-item">
              <span class="home__demo-result-label">置信度</span>
              <span class="home__demo-result-value">{{ Math.round(demoResult.confidenceScore * 100) }}%</span>
            </div>
            <div class="home__demo-result-item">
              <span class="home__demo-result-label">相互作用类型</span>
              <span class="home__demo-result-value">{{ demoResult.interactions.length }}种</span>
            </div>
          </div>
        </div>
      </div>
    </section>
    
    <footer class="home__footer">
      <div class="home__footer-content">
        <div class="home__logo">
          <span class="home__logo-icon">🧬</span>
          <span class="home__logo-text">智互药研</span>
        </div>
        <div class="home__footer-links">
          <a href="#features" class="home__footer-link">功能介绍</a>
          <a href="#architecture" class="home__footer-link">技术架构</a>
          <a href="#demo" class="home__footer-link">在线演示</a>
        </div>
        <span class="home__footer-copyright">© 2024 SynPharm</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { PredictionResult } from '@/types'

const inputType = ref<'pdb' | 'smiles'>('pdb')
const inputValue = ref('')
const isLoading = ref(false)
const demoResult = ref<PredictionResult | null>(null)

const handleDemoPredict = async () => {
  if (!inputValue.value.trim()) return
  
  isLoading.value = true
  
  await new Promise(resolve => setTimeout(resolve, 1500))
  
  demoResult.value = {
    id: 'demo_result',
    targetId: inputType.value === 'pdb' ? inputValue.value : 'P01234',
    targetName: inputType.value === 'pdb' ? 'ACE2' : '靶点蛋白',
    ligandSmiles: inputType.value === 'smiles' ? inputValue.value : 'C(=O)(C(=O)O)NC(CCC(=O)O)C(=O)O',
    bindingAffinity: -8.5 + Math.random() * (-2),
    confidenceScore: 0.85 + Math.random() * 0.1,
    confidenceLevel: 'high',
    interactions: [
      { type: 'hydrogen_bond', residueName: 'ASP', residueNumber: 30, distance: 2.8 },
      { type: 'hydrogen_bond', residueName: 'GLN', residueNumber: 24, distance: 3.1 },
      { type: 'hydrophobic', residueName: 'PHE', residueNumber: 45, distance: 4.2 },
      { type: 'ionic', residueName: 'LYS', residueNumber: 19, distance: 3.5 }
    ],
    createdAt: new Date().toISOString(),
    datasetInfo: {
      name: '演示数据集',
      size: 10000,
      description: '演示数据',
      source: 'internal'
    }
  }
  
  isLoading.value = false
}
</script>

<style lang="scss" scoped>
.home {
  min-height: 100vh;
  background: $bg-secondary;
}

.home__header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid $border-light;
  z-index: $z-sticky;
}

.home__header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: $spacing-md $spacing-xl;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.home__logo {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.home__logo-icon {
  font-size: 24px;
}

.home__logo-text {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $primary-color;
}

.home__nav {
  display: flex;
  gap: $spacing-xl;
}

.home__nav-link {
  color: $text-secondary;
  text-decoration: none;
  font-size: $font-size-sm;
  font-weight: 500;
  transition: color $transition-fast;
  
  &:hover {
    color: $primary-color;
  }
}

.home__header-actions {
  display: flex;
  gap: $spacing-sm;
}

.home__btn {
  padding: $spacing-sm $spacing-lg;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  font-weight: 500;
  text-decoration: none;
  transition: all $transition-fast;
  cursor: pointer;
  border: none;
  
  &--primary {
    background: $primary-color;
    color: #ffffff;
    
    &:hover {
      background: $primary-dark;
    }
    
    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }
  
  &--outline {
    background: transparent;
    color: $primary-color;
    border: 1px solid $border-color;
    
    &:hover {
      background: rgba($primary-color, 0.04);
    }
  }
  
  &--large {
    padding: $spacing-md $spacing-xl;
    font-size: $font-size-base;
  }
  
  &--full {
    width: 100%;
  }
}

.home__hero {
  padding: 100px $spacing-xl $spacing-3xl;
}

.home__hero-content {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: $spacing-3xl;
  align-items: center;
}

.home__hero-text {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.home__hero-title {
  font-size: 44px;
  font-weight: 700;
  color: $text-primary;
  line-height: 1.15;
  letter-spacing: -0.5px;
}

.home__hero-desc {
  font-size: $font-size-lg;
  color: $text-secondary;
  line-height: 1.6;
}

.home__hero-actions {
  display: flex;
  gap: $spacing-md;
}

.home__hero-stats {
  display: flex;
  gap: $spacing-2xl;
  padding-top: $spacing-md;
  border-top: 1px solid $border-light;
}

.home__stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.home__stat-value {
  font-size: 32px;
  font-weight: 700;
  color: $primary-color;
}

.home__stat-label {
  font-size: $font-size-xs;
  color: $text-muted;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.home__hero-visual {
  position: relative;
  height: 420px;
}

.home__molecule {
  position: absolute;
  width: 320px;
  height: 320px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.home__molecule-svg {
  width: 100%;
  height: 100%;
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

.home__features {
  padding: $spacing-3xl $spacing-xl;
  background: $bg-primary;
}

.home__section-header {
  text-align: center;
  margin-bottom: $spacing-2xl;
}

.home__section-title {
  font-size: 32px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.home__section-desc {
  font-size: $font-size-base;
  color: $text-muted;
}

.home__features-grid {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-lg;
}

.home__feature-card {
  background: $bg-secondary;
  padding: $spacing-xl;
  border-radius: $border-radius-lg;
  text-align: center;
  transition: all $transition-normal;
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: $shadow-md;
  }
}

.home__feature-icon {
  font-size: 40px;
  margin-bottom: $spacing-md;
}

.home__feature-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.home__feature-desc {
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: 1.5;
}

.home__architecture {
  padding: $spacing-3xl $spacing-xl;
  background: $bg-secondary;
}

.home__architecture-diagram {
  max-width: 500px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.home__architecture-box {
  background: $bg-primary;
  padding: $spacing-lg;
  border-radius: $border-radius-md;
  text-align: center;
  border: 1px solid $border-light;
  position: relative;
  
  & + &::before {
    content: '';
    position: absolute;
    top: -14px;
    left: 50%;
    transform: translateX(-50%);
    width: 0;
    height: 0;
    border-left: 8px solid transparent;
    border-right: 8px solid transparent;
    border-bottom: 8px solid $border-light;
  }
}

.home__architecture-label {
  display: block;
  font-size: $font-size-xs;
  color: $text-muted;
  margin-bottom: $spacing-xs;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.home__architecture-content {
  font-size: $font-size-sm;
  color: $text-primary;
}

.home__demo {
  padding: $spacing-3xl $spacing-xl;
  background: $bg-primary;
}

.home__demo-card {
  max-width: 600px;
  margin: 0 auto;
  background: $primary-color;
  padding: $spacing-xl;
  border-radius: $border-radius-xl;
}

.home__demo-form {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
}

.home__demo-input-group {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.home__demo-label {
  font-size: $font-size-xs;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.home__demo-select,
.home__demo-input {
  padding: $spacing-md;
  border: none;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  background: rgba(255, 255, 255, 0.95);
}

.home__demo-result {
  background: rgba(255, 255, 255, 0.1);
  border-radius: $border-radius-lg;
  padding: $spacing-lg;
}

.home__demo-result-header {
  font-size: $font-size-sm;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: $spacing-md;
}

.home__demo-result-content {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.home__demo-result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.home__demo-result-label {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.7);
}

.home__demo-result-value {
  font-size: $font-size-sm;
  font-weight: 600;
  color: #ffffff;
}

.home__footer {
  padding: $spacing-xl $spacing-xl;
  background: $primary-color;
}

.home__footer-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-lg;
}

.home__footer .home__logo-text {
  color: #ffffff;
}

.home__footer-links {
  display: flex;
  gap: $spacing-xl;
}

.home__footer-link {
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  font-size: $font-size-sm;
  
  &:hover {
    color: #ffffff;
  }
}

.home__footer-copyright {
  font-size: $font-size-xs;
  color: rgba(255, 255, 255, 0.4);
}
</style>