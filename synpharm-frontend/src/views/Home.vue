<template>
  <div class="home">
    <!-- ============================================================ Navigation -->
    <header class="home__nav" :class="{ 'home__nav--open': mobileMenuOpen }">
      <div class="home__nav-inner">
        <router-link to="/" class="home__logo" aria-label="SynPharm 首页">
          <svg class="home__logo-icon" viewBox="0 0 32 32" fill="none" aria-hidden="true">
            <path
              d="M16 4L26.4 10V22L16 28L5.6 22V10L16 4Z"
              stroke="currentColor"
              stroke-width="2"
              fill="rgba(12, 74, 110, 0.08)"
            />
            <circle cx="16" cy="16" r="3" fill="currentColor" />
            <circle cx="16" cy="9" r="2" fill="currentColor" />
            <circle cx="22.5" cy="12.5" r="2" fill="currentColor" />
            <circle cx="22.5" cy="19.5" r="2" fill="currentColor" />
            <circle cx="16" cy="23" r="2" fill="currentColor" />
            <circle cx="9.5" cy="19.5" r="2" fill="currentColor" />
            <circle cx="9.5" cy="12.5" r="2" fill="currentColor" />
          </svg>
          <span class="home__logo-text">SynPharm</span>
        </router-link>

        <nav class="home__nav-links" aria-label="主导航">
          <a href="#capabilities" class="home__nav-link">核心能力</a>
          <a href="#design" class="home__nav-link">分子设计</a>
          <a href="#architecture" class="home__nav-link">技术架构</a>
        </nav>

        <div class="home__nav-actions">
          <router-link to="/login" class="home__btn home__btn--outline home__btn--sm">登录</router-link>
          <button
            class="home__nav-toggle"
            aria-label="切换导航菜单"
            :aria-expanded="mobileMenuOpen"
            @click="toggleMobileMenu"
          >
            <span class="home__nav-toggle-bar" />
            <span class="home__nav-toggle-bar" />
            <span class="home__nav-toggle-bar" />
          </button>
        </div>
      </div>
    </header>

    <!-- ============================================================ Hero -->
    <section class="home__hero">
      <div class="home__container home__hero-grid">
        <div class="home__hero-content">
          <span class="home__hero-eyebrow">SYNPHARM · AI-DRIVEN DRUG DISCOVERY</span>
          <h1 class="home__hero-title">智能药物研发预测平台</h1>
          <p class="home__hero-subtitle">
            基于多维分子互作与 AI 预测，辅助药物发现、靶点分析与分子设计。
          </p>
          <div class="home__hero-actions">
            <router-link to="/predict" class="home__btn home__btn--primary">
              开始探索
            </router-link>
            <a href="#capabilities" class="home__btn home__btn--outline">
              了解平台
            </a>
          </div>

          <!-- 能力导航 rail -->
          <div class="home__hero-rail">
            <a href="#capabilities" class="home__rail-item">
              <span class="home__rail-abbr">PPI</span>
              <span class="home__rail-label">蛋白互作</span>
            </a>
            <a href="#capabilities" class="home__rail-item">
              <span class="home__rail-abbr">DTI</span>
              <span class="home__rail-label">药物-靶点</span>
            </a>
            <a href="#capabilities" class="home__rail-item">
              <span class="home__rail-abbr">DDI</span>
              <span class="home__rail-label">药物相互作用</span>
            </a>
            <a href="#design" class="home__rail-item home__rail-item--accent">
              <span class="home__rail-abbr">MD</span>
              <span class="home__rail-label">分子设计</span>
            </a>
          </div>
        </div>

        <div class="home__hero-visual">
          <HeroMolecule3D />
        </div>
      </div>
    </section>

    <!-- ============================================================ Core Capabilities -->
    <section id="capabilities" class="home__section home__section--surface">
      <div class="home__container">
        <div class="home__section-header">
          <span class="home__section-eyebrow">CORE COMPUTATION</span>
          <h2 class="home__section-title">四大核心计算模块</h2>
          <p class="home__section-desc">覆盖从分子互作预测到候选分子设计的完整计算链路</p>
        </div>

        <div class="home__cap-grid">
          <!-- PPI -->
          <article class="home__cap">
            <div class="home__cap-illustration">
              <svg viewBox="0 0 320 200" fill="none" aria-hidden="true">
                <path
                  d="M42 150C28 96 66 44 112 44c34 0 46 28 44 48 0 40-40 72-80 72-18 0-30-6-34-14Z"
                  fill="rgba(12, 74, 110, 0.06)"
                  stroke="#0C4A6E"
                  stroke-width="1.5"
                />
                <path
                  d="M278 150c14-54-24-106-70-106-34 0-46 28-44 48 0 40 40 72 80 72 18 0 30-6 34-14Z"
                  fill="rgba(8, 145, 178, 0.06)"
                  stroke="#0891B2"
                  stroke-width="1.5"
                />
                <line x1="160" y1="64" x2="160" y2="142" stroke="#0891B2" stroke-width="1.4" stroke-dasharray="4 4" />
                <circle cx="160" cy="103" r="3.5" fill="#10B981" />
                <circle cx="108" cy="72" r="3" fill="#0C4A6E" />
                <circle cx="104" cy="120" r="3" fill="#0C4A6E" />
                <circle cx="210" cy="78" r="3" fill="#0891B2" />
                <circle cx="216" cy="126" r="3" fill="#0891B2" />
              </svg>
            </div>
            <div class="home__cap-body">
              <span class="home__cap-index">01</span>
              <h3 class="home__cap-title">PPI</h3>
              <span class="home__cap-en">Protein–Protein Interaction</span>
              <p class="home__cap-desc">蛋白质–蛋白质相互作用预测，揭示潜在分子互作关系</p>
            </div>
          </article>

          <!-- DTI -->
          <article class="home__cap">
            <div class="home__cap-illustration">
              <svg viewBox="0 0 320 200" fill="none" aria-hidden="true">
                <path
                  d="M44 66C46 158 150 196 226 160c30-14 44-44 42-76"
                  stroke="#0C4A6E"
                  stroke-width="2"
                  stroke-linecap="round"
                />
                <path d="M76 96c10 46 74 74 138 58" stroke="#0C4A6E" stroke-width="1" stroke-dasharray="3 4" opacity="0.55" />
                <g transform="translate(158 116)">
                  <path d="M0-19 16-10l16 9-16 10-16-9Z" fill="rgba(16, 185, 129, 0.14)" stroke="#10B981" stroke-width="1.5" stroke-linejoin="round" />
                  <circle cx="0" cy="-19" r="3.5" fill="#10B981" />
                  <circle cx="16" cy="10" r="3.5" fill="#0891B2" />
                  <circle cx="-16" cy="10" r="3.5" fill="#0C4A6E" />
                </g>
                <line x1="104" y1="126" x2="140" y2="116" stroke="#0891B2" stroke-width="1.4" stroke-dasharray="4 3" />
              </svg>
            </div>
            <div class="home__cap-body">
              <span class="home__cap-index">02</span>
              <h3 class="home__cap-title">DTI</h3>
              <span class="home__cap-en">Drug–Target Interaction</span>
              <p class="home__cap-desc">药物–靶点相互作用预测，分析潜在结合关系</p>
            </div>
          </article>

          <!-- DDI -->
          <article class="home__cap">
            <div class="home__cap-illustration">
              <svg viewBox="0 0 320 200" fill="none" aria-hidden="true">
                <g transform="translate(104 100)">
                  <path d="M0-20 17-10l17 10-17 10-17-10Z" fill="rgba(12, 74, 110, 0.08)" stroke="#0C4A6E" stroke-width="1.5" stroke-linejoin="round" />
                  <circle cx="0" cy="-20" r="4" fill="#0C4A6E" />
                  <circle cx="17" cy="10" r="4" fill="#0891B2" />
                  <circle cx="-17" cy="10" r="4" fill="#0C4A6E" />
                </g>
                <g transform="translate(216 100)">
                  <path d="M0-20 17-10l17 10-17 10-17-10Z" fill="rgba(8, 145, 178, 0.08)" stroke="#0891B2" stroke-width="1.5" stroke-linejoin="round" />
                  <circle cx="0" cy="-20" r="4" fill="#0891B2" />
                  <circle cx="17" cy="10" r="4" fill="#0C4A6E" />
                  <circle cx="-17" cy="10" r="4" fill="#0891B2" />
                </g>
                <line x1="138" y1="100" x2="182" y2="100" stroke="#0891B2" stroke-width="1.4" stroke-dasharray="4 3" />
                <circle cx="160" cy="100" r="3.5" fill="#10B981" />
              </svg>
            </div>
            <div class="home__cap-body">
              <span class="home__cap-index">03</span>
              <h3 class="home__cap-title">DDI</h3>
              <span class="home__cap-en">Drug–Drug Interaction</span>
              <p class="home__cap-desc">药物–药物相互作用预测，评估潜在相互作用</p>
            </div>
          </article>

          <!-- Molecular Design -->
          <article class="home__cap home__cap--accent">
            <div class="home__cap-illustration">
              <svg viewBox="0 0 320 200" fill="none" aria-hidden="true">
                <g transform="translate(160 104)">
                  <path d="M0-22 19-11l19 11-19 11-19-11Z" fill="rgba(12, 74, 110, 0.08)" stroke="#0C4A6E" stroke-width="1.5" stroke-linejoin="round" />
                  <circle cx="0" cy="-22" r="4.5" fill="#0891B2" />
                  <circle cx="19" cy="11" r="4.5" fill="#0C4A6E" />
                  <circle cx="-19" cy="11" r="4.5" fill="#0891B2" />
                </g>
                <line x1="160" y1="104" x2="160" y2="44" stroke="#10B981" stroke-width="1.5" />
                <circle cx="160" cy="38" r="4.5" fill="#10B981" />
                <line x1="160" y1="104" x2="232" y2="62" stroke="#0891B2" stroke-width="1.2" opacity="0.75" />
                <circle cx="238" cy="58" r="3.5" fill="#0891B2" opacity="0.85" />
                <line x1="160" y1="104" x2="88" y2="62" stroke="#0891B2" stroke-width="1.2" opacity="0.75" />
                <circle cx="82" cy="58" r="3.5" fill="#0891B2" opacity="0.85" />
                <path d="M240 118h12M246 112v12" stroke="#10B981" stroke-width="1.6" stroke-linecap="round" />
              </svg>
            </div>
            <div class="home__cap-body">
              <span class="home__cap-index">04</span>
              <h3 class="home__cap-title">Molecular Design</h3>
              <span class="home__cap-en">AI-Driven Molecular Design</span>
              <p class="home__cap-desc">AI 辅助分子结构生成与候选分子优化设计</p>
            </div>
          </article>
        </div>
      </div>
    </section>

    <!-- ============================================================ Molecular Design -->
    <section id="design" class="home__section home__section--subdued">
      <div class="home__container home__design-grid">
        <div class="home__design-visual">
          <Molecule3D />
        </div>

        <div class="home__design-content">
          <span class="home__section-eyebrow">AI-DRIVEN DESIGN</span>
          <h2 class="home__section-title">从预测走向设计</h2>
          <p class="home__design-lead">
            基于分子互作预测结果，进行候选分子的结构生成与优化探索，辅助潜在活性分子发现与结构优化。
          </p>

          <div class="home__design-features">
            <div class="home__design-feature">
              <h3 class="home__design-feature-title">
                Structure Generation
                <span>分子结构生成</span>
              </h3>
              <p class="home__design-feature-desc">基于骨架与取代基组合的分子结构生成</p>
            </div>
            <div class="home__design-feature">
              <h3 class="home__design-feature-title">
                Property Prediction
                <span>性质预测</span>
              </h3>
              <p class="home__design-feature-desc">MW / LogP / TPSA / QED 等多维分子性质估算</p>
            </div>
            <div class="home__design-feature">
              <h3 class="home__design-feature-title">
                Candidate Screening
                <span>候选筛选</span>
              </h3>
              <p class="home__design-feature-desc">基于约束规则与加权评分的候选分子筛选排序</p>
            </div>
          </div>

          <div class="home__design-actions">
            <router-link to="/design" class="home__btn home__btn--primary">
              进入分子设计 <span aria-hidden="true">→</span>
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- ============================================================ Workflow -->
    <section id="workflow" class="home__section home__section--surface">
      <div class="home__container">
        <div class="home__section-header">
          <span class="home__section-eyebrow">RESEARCH WORKFLOW</span>
          <h2 class="home__section-title">从输入到预测，再到结构分析</h2>
          <p class="home__section-desc">覆盖药物研发预测的完整科研闭环</p>
        </div>

        <ol class="home__flow">
          <li class="home__flow-step">
            <span class="home__flow-index">01</span>
            <h3 class="home__flow-title">Input</h3>
            <p class="home__flow-desc">PDB / UniProt / SMILES / CSV</p>
          </li>
          <li class="home__flow-arrow" aria-hidden="true">→</li>
          <li class="home__flow-step">
            <span class="home__flow-index">02</span>
            <h3 class="home__flow-title">Prediction</h3>
            <p class="home__flow-desc">PPI / DTI / DDI 预测</p>
          </li>
          <li class="home__flow-arrow" aria-hidden="true">→</li>
          <li class="home__flow-step">
            <span class="home__flow-index">03</span>
            <h3 class="home__flow-title">Structure</h3>
            <p class="home__flow-desc">3D 分子结构可视化</p>
          </li>
          <li class="home__flow-arrow" aria-hidden="true">→</li>
          <li class="home__flow-step">
            <span class="home__flow-index">04</span>
            <h3 class="home__flow-title">Design</h3>
            <p class="home__flow-desc">AI 分子设计</p>
          </li>
          <li class="home__flow-arrow" aria-hidden="true">→</li>
          <li class="home__flow-step home__flow-step--accent">
            <span class="home__flow-index">05</span>
            <h3 class="home__flow-title">Analysis</h3>
            <p class="home__flow-desc">预测结果 / 候选筛选</p>
          </li>
        </ol>
      </div>
    </section>

    <!-- ============================================================ Architecture -->
    <section id="architecture" class="home__section home__section--subdued">
      <div class="home__container home__container--narrow">
        <div class="home__section-header">
          <span class="home__section-eyebrow">COMPUTATIONAL ARCHITECTURE</span>
          <h2 class="home__section-title">技术架构</h2>
          <p class="home__section-desc">模块化分层设计，灵活扩展</p>
        </div>

        <div class="home__arch">
          <div class="home__arch-layer">
            <div class="home__arch-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none">
                <rect x="2" y="3" width="20" height="14" rx="2" stroke="currentColor" stroke-width="1.5" />
                <path d="M8 21h8M12 17v4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
              </svg>
            </div>
            <div class="home__arch-info">
              <span class="home__arch-name">用户交互层</span>
              <span class="home__arch-en">User Interface Layer</span>
            </div>
            <div class="home__arch-tags">
              <span>Web 前端</span>
              <span>小程序</span>
              <span>API 接口</span>
            </div>
          </div>
          <div class="home__arch-connector" aria-hidden="true" />

          <div class="home__arch-layer">
            <div class="home__arch-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M4 21v-7M4 10V3M12 21v-9M12 8V3M20 21v-5M20 12V3M1 14h6M9 8h6M17 16h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
              </svg>
            </div>
            <div class="home__arch-info">
              <span class="home__arch-name">业务服务层</span>
              <span class="home__arch-en">Business Service Layer</span>
            </div>
            <div class="home__arch-tags">
              <span>任务管理</span>
              <span>结果分析</span>
              <span>数据处理</span>
            </div>
          </div>
          <div class="home__arch-connector" aria-hidden="true" />

          <div class="home__arch-layer">
            <div class="home__arch-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none">
                <rect x="7" y="7" width="10" height="10" rx="1.5" stroke="currentColor" stroke-width="1.5" />
                <path d="M9 1v3M15 1v3M9 20v3M15 20v3M1 9h3M1 15h3M20 9h3M20 15h3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
              </svg>
            </div>
            <div class="home__arch-info">
              <span class="home__arch-name">AI 预测层</span>
              <span class="home__arch-en">AI Prediction Layer</span>
            </div>
            <div class="home__arch-tags">
              <span>PPI 模型</span>
              <span>DTI 模型</span>
              <span>DDI 模型</span>
            </div>
          </div>
          <div class="home__arch-connector" aria-hidden="true" />

          <div class="home__arch-layer">
            <div class="home__arch-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none">
                <ellipse cx="12" cy="5" rx="8" ry="3" stroke="currentColor" stroke-width="1.5" />
                <path d="M4 5v14c0 1.66 3.58 3 8 3s8-1.34 8-3V5M4 12c0 1.66 3.58 3 8 3s8-1.34 8-3" stroke="currentColor" stroke-width="1.5" />
              </svg>
            </div>
            <div class="home__arch-info">
              <span class="home__arch-name">数据层</span>
              <span class="home__arch-en">Data Layer</span>
            </div>
            <div class="home__arch-tags">
              <span>分子数据</span>
              <span>预测结果</span>
              <span>用户数据</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ============================================================ Footer -->
    <footer class="home__footer">
      <div class="home__container home__footer-inner">
        <div class="home__footer-brand">
          <svg class="home__footer-logo-icon" viewBox="0 0 32 32" fill="none" aria-hidden="true">
            <path
              d="M16 4L26.4 10V22L16 28L5.6 22V10L16 4Z"
              stroke="currentColor"
              stroke-width="2"
              fill="rgba(12, 74, 110, 0.08)"
            />
            <circle cx="16" cy="16" r="3" fill="currentColor" />
            <circle cx="16" cy="9" r="2" fill="currentColor" />
            <circle cx="22.5" cy="12.5" r="2" fill="currentColor" />
            <circle cx="22.5" cy="19.5" r="2" fill="currentColor" />
            <circle cx="16" cy="23" r="2" fill="currentColor" />
            <circle cx="9.5" cy="19.5" r="2" fill="currentColor" />
            <circle cx="9.5" cy="12.5" r="2" fill="currentColor" />
          </svg>
          <span class="home__footer-logo-text">SynPharm</span>
        </div>
        <nav class="home__footer-links" aria-label="页脚导航">
          <a href="#capabilities" class="home__footer-link">核心能力</a>
          <a href="#design" class="home__footer-link">分子设计</a>
          <a href="#architecture" class="home__footer-link">技术架构</a>
        </nav>
        <span class="home__footer-copy">© 2024 SynPharm. 智能药物研发预测平台</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import HeroMolecule3D from '@/components/home/HeroMolecule3D.vue'
import Molecule3D from '@/components/home/Molecule3D.vue'

const mobileMenuOpen = ref(false)

const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}
</script>

<style lang="scss" scoped>
.home {
  min-height: 100vh;
  background: $sp-home-bg;
  color: $sp-home-text;
  font-family: $sp-home-font-family;
}

.home__container {
  width: 100%;
  max-width: 1240px;
  margin: 0 auto;
  padding-left: 24px;
  padding-right: 24px;

  &--narrow {
    max-width: 860px;
  }
}

.home__section {
  padding: 96px 0;

  &--surface {
    background: $sp-home-surface;
    border-top: 1px solid $sp-home-border;
    border-bottom: 1px solid $sp-home-border;
  }

  &--subdued {
    background: $sp-home-bg;
  }
}

.home__section-header {
  text-align: center;
  margin-bottom: 56px;
}

.home__section-eyebrow {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 2px;
  color: $sp-home-secondary;
  text-transform: uppercase;
  margin-bottom: 14px;
}

.home__section-title {
  font-size: 34px;
  font-weight: 700;
  color: $sp-home-text;
  margin-bottom: 14px;
  letter-spacing: -0.4px;
}

.home__section-desc {
  font-size: 16px;
  color: $sp-home-text-muted;
  line-height: 1.6;
}

/* ============================================================================
 * Buttons
 * ========================================================================== */
.home__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 46px;
  padding: 0 24px;
  border-radius: $sp-home-radius-md;
  font-size: 15px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s ease;
  cursor: pointer;
  border: none;
  white-space: nowrap;

  &--sm {
    height: 38px;
    padding: 0 18px;
    font-size: 14px;
  }

  &--primary {
    background: $sp-home-primary;
    color: #ffffff;

    &:hover {
      background: $sp-home-primary-dark;
    }
  }

  &--outline {
    background: transparent;
    color: $sp-home-primary;
    border: 1px solid $sp-home-border;

    &:hover {
      background: rgba($sp-home-primary, 0.04);
      border-color: $sp-home-secondary;
    }
  }
}

/* ============================================================================
 * Navigation
 * ========================================================================== */
.home__nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 68px;
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid $sp-home-border;
  z-index: $z-sticky;
  backdrop-filter: blur(10px);
}

.home__nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 24px;
}

.home__logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: $sp-home-primary;
}

.home__logo-icon {
  width: 32px;
  height: 32px;
  color: $sp-home-primary;
}

.home__logo-text {
  font-size: 18px;
  font-weight: 600;
  color: $sp-home-text;
  letter-spacing: -0.3px;
}

.home__nav-links {
  display: flex;
  align-items: center;
  gap: 40px;
}

.home__nav-link {
  font-size: 15px;
  font-weight: 500;
  color: $sp-home-text-soft;
  text-decoration: none;
  transition: color 0.2s ease;

  &:hover {
    color: $sp-home-primary;
  }
}

.home__nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.home__nav-toggle {
  display: none;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 40px;
  height: 40px;
  gap: 5px;
  background: transparent;
  border: none;
  cursor: pointer;
}

.home__nav-toggle-bar {
  display: block;
  width: 22px;
  height: 2px;
  background: $sp-home-text;
  border-radius: 1px;
  transition: transform 0.2s ease, opacity 0.2s ease;
}

/* ============================================================================
 * Hero
 * ========================================================================== */
.home__hero {
  padding-top: 68px + 88px;
  padding-bottom: 96px;
  background: $sp-home-bg;
}

.home__hero-grid {
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  gap: 64px;
  align-items: center;
}

.home__hero-content {
  display: flex;
  flex-direction: column;
  gap: 26px;
}

.home__hero-eyebrow {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 2px;
  color: $sp-home-secondary;
  text-transform: uppercase;
}

.home__hero-title {
  font-size: 52px;
  font-weight: 700;
  line-height: 1.12;
  color: $sp-home-text;
  letter-spacing: -0.8px;
}

.home__hero-subtitle {
  font-size: 18px;
  line-height: 1.7;
  color: $sp-home-text-soft;
  max-width: 480px;
}

.home__hero-actions {
  display: flex;
  gap: 16px;
}

.home__hero-rail {
  display: flex;
  align-items: stretch;
  margin-top: 12px;
  padding-top: 28px;
  border-top: 1px solid $sp-home-border;
  gap: 0;
}

.home__rail-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 0 20px;
  text-decoration: none;
  border-right: 1px solid $sp-home-border;

  &:first-child {
    padding-left: 0;
  }

  &:last-child {
    padding-right: 0;
    border-right: none;
  }

  &:hover .home__rail-abbr {
    color: $sp-home-primary;
  }
}

.home__rail-abbr {
  font-size: 20px;
  font-weight: 700;
  color: $sp-home-primary;
  letter-spacing: 0.5px;
  transition: color 0.2s ease;
}

.home__rail-label {
  font-size: 13px;
  color: $sp-home-text-soft;
  line-height: 1.4;
}

.home__rail-item--accent .home__rail-abbr {
  color: $sp-home-accent;
}

.home__hero-visual {
  display: flex;
  justify-content: center;
  align-items: center;
}

/* ============================================================================
 * Core Capabilities
 * ========================================================================== */
.home__cap-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 28px;
}

.home__cap {
  background: $sp-home-bg;
  border: 1px solid $sp-home-border;
  border-radius: $sp-home-radius-lg;
  overflow: hidden;
  box-shadow: $sp-home-shadow;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    border-color: rgba(8, 145, 178, 0.4);
    box-shadow: $sp-home-shadow-hover;
  }

  &--accent {
    border-color: rgba(16, 185, 129, 0.32);
  }
}

.home__cap-illustration {
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #ffffff 0%, #f1f5f9 100%);

  svg {
    width: 78%;
    height: 78%;
  }
}

.home__cap-body {
  padding: 24px 26px 26px;
  position: relative;
}

.home__cap-index {
  position: absolute;
  top: 22px;
  right: 26px;
  font-size: 13px;
  font-weight: 600;
  color: $sp-home-text-muted;
  font-family: $sp-home-font-mono;
}

.home__cap-title {
  font-size: 24px;
  font-weight: 700;
  color: $sp-home-text;
  margin-bottom: 4px;
  letter-spacing: -0.3px;
}

.home__cap--accent .home__cap-title {
  color: $sp-home-primary;
}

.home__cap-en {
  display: block;
  font-size: 12px;
  color: $sp-home-text-muted;
  margin-bottom: 12px;
}

.home__cap-desc {
  font-size: 14px;
  line-height: 1.65;
  color: $sp-home-text-soft;
}

/* ============================================================================
 * Molecular Design
 * ========================================================================== */
.home__design-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 64px;
  align-items: center;
}

.home__design-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.home__design-lead {
  font-size: 16px;
  line-height: 1.7;
  color: $sp-home-text-soft;
  margin-top: -6px;
}

.home__design-features {
  display: flex;
  flex-direction: column;
  gap: 0;
  margin: 6px 0 4px;
}

.home__design-feature {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 20px;
  padding: 18px 0;
  border-top: 1px solid $sp-home-border;

  &:last-child {
    border-bottom: 1px solid $sp-home-border;
  }
}

.home__design-feature-title {
  font-size: 15px;
  font-weight: 600;
  color: $sp-home-primary;

  span {
    display: block;
    font-size: 13px;
    font-weight: 500;
    color: $sp-home-text-soft;
    margin-top: 3px;
  }
}

.home__design-feature-desc {
  font-size: 14px;
  line-height: 1.6;
  color: $sp-home-text-muted;
  align-self: center;
}

.home__design-actions {
  margin-top: 8px;
}

/* ============================================================================
 * Workflow
 * ========================================================================== */
.home__flow {
  list-style: none;
  display: flex;
  align-items: stretch;
  justify-content: center;
  gap: 16px;
}

.home__flow-step {
  flex: 1;
  max-width: 190px;
  background: $sp-home-bg;
  border: 1px solid $sp-home-border;
  border-radius: $sp-home-radius-md;
  padding: 22px 20px;
  transition: border-color 0.2s ease;

  &:hover {
    border-color: rgba(8, 145, 178, 0.4);
  }

  &--accent {
    border-color: rgba(16, 185, 129, 0.36);
  }
}

.home__flow-index {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: $sp-home-secondary;
  font-family: $sp-home-font-mono;
  margin-bottom: 8px;
}

.home__flow-title {
  font-size: 17px;
  font-weight: 600;
  color: $sp-home-text;
  margin-bottom: 6px;
}

.home__flow-desc {
  font-size: 13px;
  line-height: 1.55;
  color: $sp-home-text-muted;
}

.home__flow-arrow {
  display: flex;
  align-items: center;
  color: $sp-home-secondary;
  font-size: 20px;
  flex-shrink: 0;
}

/* ============================================================================
 * Architecture
 * ========================================================================== */
.home__arch {
  display: flex;
  flex-direction: column;
}

.home__arch-layer {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px 24px;
  background: $sp-home-surface;
  border: 1px solid $sp-home-border;
  border-radius: $sp-home-radius-md;
  box-shadow: $sp-home-shadow;
}

.home__arch-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  color: $sp-home-primary;

  svg {
    width: 100%;
    height: 100%;
  }
}

.home__arch-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.home__arch-name {
  font-size: 16px;
  font-weight: 600;
  color: $sp-home-text;
}

.home__arch-en {
  font-size: 12px;
  color: $sp-home-text-muted;
}

.home__arch-tags {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;

  span {
    font-size: 13px;
    color: $sp-home-text-soft;
    background: $sp-home-bg;
    padding: 5px 12px;
    border-radius: $sp-home-radius-sm;
    border: 1px solid $sp-home-border;
    white-space: nowrap;
  }
}

.home__arch-connector {
  position: relative;
  height: 22px;
  width: 1px;
  margin: 0 auto;
  background: $sp-home-border;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 0;
    height: 0;
    border-left: 4px solid transparent;
    border-right: 4px solid transparent;
    border-top: 5px solid $sp-home-border;
  }
}

/* ============================================================================
 * Footer
 * ========================================================================== */
.home__footer {
  padding: 40px 0;
  background: $sp-home-bg;
  border-top: 1px solid $sp-home-border;
}

.home__footer-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.home__footer-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.home__footer-logo-icon {
  width: 28px;
  height: 28px;
  color: $sp-home-primary;
}

.home__footer-logo-text {
  font-size: 16px;
  font-weight: 600;
  color: $sp-home-text;
}

.home__footer-links {
  display: flex;
  gap: 32px;
}

.home__footer-link {
  font-size: 14px;
  color: $sp-home-text-soft;
  text-decoration: none;
  transition: color 0.2s ease;

  &:hover {
    color: $sp-home-primary;
  }
}

.home__footer-copy {
  font-size: 13px;
  color: $sp-home-text-muted;
}

/* ============================================================================
 * Responsive
 * ========================================================================== */
@media (max-width: $bp-lg) {
  .home__hero-grid {
    gap: 40px;
  }

  .home__hero-title {
    font-size: 42px;
  }

  .home__cap-grid {
    gap: 20px;
  }

  .home__design-grid {
    gap: 40px;
  }

  .home__flow {
    gap: 10px;
  }

  .home__flow-arrow {
    font-size: 16px;
  }

  .home__footer-inner {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
}

@media (max-width: $bp-md) {
  .home__nav-links {
    position: absolute;
    top: 68px;
    left: 0;
    right: 0;
    flex-direction: column;
    gap: 0;
    background: rgba(255, 255, 255, 0.98);
    border-bottom: 1px solid $sp-home-border;
    padding: 12px 24px;
    opacity: 0;
    visibility: hidden;
    transform: translateY(-8px);
    transition: opacity 0.2s ease, transform 0.2s ease, visibility 0.2s ease;
  }

  .home__nav--open .home__nav-links {
    opacity: 1;
    visibility: visible;
    transform: translateY(0);
  }

  .home__nav-link {
    display: block;
    padding: 12px 0;
    border-bottom: 1px solid $sp-home-border;

    &:last-child {
      border-bottom: none;
    }
  }

  .home__nav-actions .home__btn--sm {
    display: none;
  }

  .home__nav-toggle {
    display: flex;
  }

  .home__nav--open .home__nav-toggle-bar:nth-child(1) {
    transform: translateY(7px) rotate(45deg);
  }

  .home__nav--open .home__nav-toggle-bar:nth-child(2) {
    opacity: 0;
  }

  .home__nav--open .home__nav-toggle-bar:nth-child(3) {
    transform: translateY(-7px) rotate(-45deg);
  }

  .home__hero {
    padding-top: 68px + 64px;
    padding-bottom: 64px;
  }

  .home__hero-grid {
    grid-template-columns: 1fr;
    gap: 48px;
  }

  .home__hero-content {
    order: 1;
  }

  .home__hero-visual {
    order: 2;
  }

  .home__hero-title {
    font-size: 34px;
  }

  .home__hero-subtitle {
    font-size: 16px;
  }

  .home__hero-actions {
    flex-direction: column;
    gap: 12px;

    .home__btn {
      width: 100%;
    }
  }

  .home__hero-rail {
    flex-wrap: wrap;
    gap: 14px 0;
  }

  .home__rail-item {
    flex: 0 0 50%;
    padding: 6px 12px;
    border-right: none;

    &:first-child {
      padding-left: 12px;
    }

    &:last-child {
      padding-right: 12px;
    }
  }

  .home__cap-grid {
    grid-template-columns: 1fr;
  }

  .home__design-grid {
    grid-template-columns: 1fr;
    gap: 48px;
  }

  .home__design-visual {
    order: 2;
  }

  .home__design-content {
    order: 1;
  }

  .home__design-feature {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .home__flow {
    flex-direction: column;
    align-items: stretch;
  }

  .home__flow-step {
    max-width: none;
  }

  .home__flow-arrow {
    justify-content: center;
    transform: rotate(90deg);
  }

  .home__arch-layer {
    flex-wrap: wrap;
  }

  .home__arch-tags {
    margin-left: 0;
    width: 100%;
    justify-content: flex-start;
  }

  .home__section {
    padding: 64px 0;
  }

  .home__section-title {
    font-size: 26px;
  }

  .home__footer-links {
    gap: 20px;
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (prefers-reduced-motion: reduce) {
  .home__btn,
  .home__cap,
  .home__flow-step,
  .home__nav-link,
  .home__footer-link {
    transition: none;
  }
}
</style>
