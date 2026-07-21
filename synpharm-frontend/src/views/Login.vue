<template>
  <div class="login">
    <div class="login__left">
      <div class="login__logo">
        <span class="login__logo-icon">🧬</span>
        <span class="login__logo-text">SynPharm</span>
      </div>
      
      <h2 class="login__title">智能药物研发平台</h2>
      <p class="login__desc">基于多智能体协同技术，精准预测分子互作</p>
      
      <div class="login__features">
        <div class="login__feature">
          <span class="login__feature-icon">🔬</span>
          <span class="login__feature-text">PPI/DTI/DDI预测</span>
        </div>
        <div class="login__feature">
          <span class="login__feature-icon">3️⃣D</span>
          <span class="login__feature-text">3D可视化</span>
        </div>
        <div class="login__feature">
          <span class="login__feature-icon">📊</span>
          <span class="login__feature-text">批量处理</span>
        </div>
      </div>
    </div>
    
    <div class="login__right">
      <div class="login__form-container">
        <router-link to="/" class="login__back-link">
          ← 返回首页
        </router-link>
        
        <h3 class="login__form-title">欢迎回来</h3>
        
        <form @submit.prevent="handleLogin" class="login__form">
          <div class="login__form-group">
            <label class="login__form-label">QQ邮箱</label>
            <div class="login__input-wrapper">
              <span class="login__input-icon">📧</span>
              <input 
                v-model="email"
                type="email"
                autocomplete="email"
                class="login__form-input"
                :class="{ 'login__form-input--error': errors.email }"
                placeholder="请输入QQ邮箱"
                @blur="validateEmail"
                @input="errors.email && validateEmail()"
              />
            </div>
            <span v-if="errors.email" class="login__form-error">{{ errors.email }}</span>
          </div>
          
          <div class="login__form-group">
            <label class="login__form-label">密码</label>
            <div class="login__input-wrapper">
              <span class="login__input-icon">🔒</span>
              <input 
                v-model="password"
                type="password"
                autocomplete="current-password"
                class="login__form-input"
                :class="{ 'login__form-input--error': errors.password }"
                placeholder="请输入密码"
                @blur="validatePasswordField"
                @input="errors.password && validatePasswordField()"
              />
            </div>
            <span v-if="errors.password" class="login__form-error">{{ errors.password }}</span>
          </div>
          
          <div class="login__form-group login__form-group--remember">
            <label class="login__form-checkbox">
              <input 
                v-model="rememberMe"
                type="checkbox" 
                class="login__checkbox"
              />
              <span class="login__checkbox-checkmark"></span>
              <span>记住我</span>
            </label>
            <a href="#" class="login__form-link">忘记密码?</a>
          </div>
          
          <button 
            type="submit" 
            class="login__form-btn"
            :disabled="isLoading"
          >
            <span v-if="isLoading" class="login__btn-spinner"></span>
            {{ isLoading ? '登录中...' : '登录' }}
          </button>
          
          <div class="login__divider">
            <span class="login__divider-text">或</span>
          </div>
          
          <button 
            type="button"
            class="login__form-btn login__form-btn--outline"
            @click="handleGuestLogin"
          >
            游客模式
          </button>
        </form>
        
        <div v-if="loginError" class="login__form-message login__form-message--error">
          {{ loginError }}
        </div>
        
        <p class="login__form-footer">
          还没有账户? 
          <router-link to="/register" class="login__form-link">立即注册</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { validateQqEmail, validatePassword } from '@/utils/validators'

const authStore = useAuthStore()
const router = useRouter()

const email = ref('')
const password = ref('')
const rememberMe = ref(false)
const isLoading = ref(false)
const loginError = ref('')

const errors = reactive({
  email: '',
  password: ''
})

const validateEmail = (): boolean => {
  const result = validateQqEmail(email.value)
  errors.email = result.valid ? '' : result.message
  return result.valid
}

const validatePasswordField = (): boolean => {
  const result = validatePassword(password.value)
  errors.password = result.valid ? '' : result.message
  return result.valid
}

const validateForm = (): boolean => {
  const emailValid = validateEmail()
  const passwordValid = validatePasswordField()
  return emailValid && passwordValid
}

const handleLogin = async () => {
  if (!validateForm()) return

  isLoading.value = true
  loginError.value = ''
  
  const result = await authStore.login({
    email: email.value.trim(),
    password: password.value
  })
  
  if (result.success) {
    router.push('/dashboard')
  } else {
    loginError.value = result.message || '登录失败，请稍后重试'
  }
  
  isLoading.value = false
}

const handleGuestLogin = async () => {
  const result = await authStore.guestLogin()
  if (result.success) {
    router.push('/dashboard')
  }
}
</script>

<style lang="scss" scoped>
.login {
  min-height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 50%, #e2e8f0 100%);
}

.login__left {
  width: 50%;
  background: linear-gradient(135deg, $info-color 0%, $primary-color 100%);
  padding: $spacing-2xl;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #ffffff;
}

.login__logo {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-xl;
}

.login__logo-icon {
  font-size: 48px;
}

.login__logo-text {
  font-size: 32px;
  font-weight: 700;
}

.login__title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: $spacing-md;
}

.login__desc {
  font-size: $font-size-lg;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: $spacing-xl;
  max-width: 450px;
}

.login__features {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-lg;
}

.login__feature {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: $border-radius-lg;
  backdrop-filter: blur(10px);
  transition: all $transition-normal;
  
  &:hover {
    background: rgba(255, 255, 255, 0.25);
    transform: translateY(-2px);
  }
}

.login__feature-icon {
  font-size: 28px;
}

.login__feature-text {
  font-size: $font-size-sm;
}

.login__right {
  width: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-xl;
}

.login__form-container {
  width: 100%;
  max-width: 450px;
  background: #ffffff;
  padding: $spacing-2xl;
  border-radius: $border-radius-2xl;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.08),
    0 4px 24px rgba(0, 0, 0, 0.04);
}

.login__back-link {
  display: inline-block;
  font-size: $font-size-sm;
  color: $text-secondary;
  text-decoration: none;
  margin-bottom: $spacing-lg;
  transition: all $transition-fast;
  
  &:hover {
    color: $accent-color;
    text-decoration: underline;
  }
}

.login__form-title {
  font-size: 28px;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: $spacing-lg;
  text-align: center;
}

.login__form {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.login__form-group {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.login__form-group--remember {
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.login__form-label {
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-primary;
}

.login__input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.login__input-icon {
  position: absolute;
  left: $spacing-md;
  font-size: 18px;
  color: $text-muted;
}

.login__form-input {
  width: 100%;
  padding: $spacing-md $spacing-md $spacing-md calc(#{$spacing-md} + 40px);
  background: $bg-tertiary;
  border: 1px solid $border-color;
  border-radius: $border-radius-lg;
  font-size: $font-size-base;
  color: $text-primary;
  transition: all $transition-fast;
  
  &::placeholder {
    color: $text-muted;
  }
  
  &:focus {
    outline: none;
    border-color: $accent-color;
    background: #ffffff;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  }

  &--error {
    border-color: $error-color;

    &:focus {
      border-color: $error-color;
      box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
    }
  }
}

.login__form-error {
  font-size: $font-size-xs;
  color: $error-color;
  margin-top: $spacing-xs;
}

.login__form-message {
  margin-top: $spacing-md;
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-md;
  text-align: center;
  font-size: $font-size-sm;

  &--error {
    background: rgba(239, 68, 68, 0.1);
    color: $error-color;
  }
}

.login__form-checkbox {
  font-size: $font-size-sm;
  color: $text-secondary;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.login__checkbox {
  position: absolute;
  opacity: 0;
  cursor: pointer;
}

.login__checkbox-checkmark {
  width: 18px;
  height: 18px;
  border: 1.5px solid $border-color;
  border-radius: $border-radius-sm;
  position: relative;
  transition: all $transition-fast;
  
  &::after {
    content: '';
    position: absolute;
    left: 5px;
    top: 1px;
    width: 5px;
    height: 10px;
    border: solid $accent-color;
    border-width: 0 2px 2px 0;
    transform: rotate(45deg);
    opacity: 0;
    transition: opacity $transition-fast;
  }
}

.login__checkbox:checked + .login__checkbox-checkmark {
  background: $accent-color;
  border-color: $accent-color;
  
  &::after {
    opacity: 1;
  }
}

.login__form-link {
  color: $accent-color;
  text-decoration: none;
  font-size: $font-size-sm;
  
  &:hover {
    text-decoration: underline;
  }
}

.login__form-btn {
  padding: $spacing-md;
  background: linear-gradient(135deg, $accent-color 0%, #2563eb 100%);
  color: #ffffff;
  border: none;
  border-radius: $border-radius-lg;
  font-size: $font-size-base;
  font-weight: 500;
  cursor: pointer;
  transition: all $transition-fast;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba(59, 130, 246, 0.3);
  }
  
  &:disabled {
    background: $border-color;
    cursor: not-allowed;
  }
  
  &--outline {
    background: transparent;
    color: $text-primary;
    border: 1px solid $border-color;
    
    &:hover {
      background: $bg-tertiary;
      border-color: $accent-color;
      transform: none;
      box-shadow: none;
    }
  }
}

.login__btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.login__divider {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin: $spacing-lg 0;
}

.login__divider::before,
.login__divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: $border-color;
}

.login__divider-text {
  font-size: $font-size-xs;
  color: $text-muted;
}

.login__form-footer {
  text-align: center;
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-top: $spacing-lg;
}
</style>

