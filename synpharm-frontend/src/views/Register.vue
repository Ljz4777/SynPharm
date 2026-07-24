<template>
  <div class="register">
    <div class="register__left">
      <div class="register__logo">
        <span class="register__logo-icon">🧬</span>
        <span class="register__logo-text">SynPharm</span>
      </div>
      <h2 class="register__title">创建您的账号</h2>
      <p class="register__desc">加入我们，开启智能药物研发之旅</p>
      <div class="register__features">
        <div class="register__feature">
          <span class="register__feature-icon">🔬</span>
          <span class="register__feature-text">免费使用基础功能</span>
        </div>
        <div class="register__feature">
          <span class="register__feature-icon">📊</span>
          <span class="register__feature-text">保存预测结果</span>
        </div>
        <div class="register__feature">
          <span class="register__feature-icon">🔄</span>
          <span class="register__feature-text">跨设备同步</span>
        </div>
      </div>
    </div>
    
    <div class="register__right">
      <div class="register__form-container">
        <router-link to="/" class="register__back-link">
          ← 返回首页
        </router-link>
        <h3 class="register__form-title">注册</h3>
        <form @submit.prevent="handleRegister" class="register__form">
          <div class="register__form-group">
            <label class="register__form-label">QQ邮箱</label>
            <input 
              v-model="form.email" 
              type="email"
              autocomplete="email"
              class="register__form-input"
              :class="{ 'register__form-input--error': errors.email }"
              placeholder="请输入QQ邮箱"
              @blur="validateEmail"
              @input="errors.email && validateEmail()"
            />
            <span v-if="errors.email" class="register__form-error">{{ errors.email }}</span>
          </div>
          
          <div class="register__form-group">
            <label class="register__form-label">昵称</label>
            <input 
              v-model="form.nickname" 
              type="text" 
              class="register__form-input"
              :class="{ 'register__form-input--error': errors.nickname }"
              placeholder="请输入昵称"
              @blur="validateNicknameField"
              @input="errors.nickname && validateNicknameField()"
            />
            <span v-if="errors.nickname" class="register__form-error">{{ errors.nickname }}</span>
          </div>
          
          <div class="register__form-group">
            <label class="register__form-label">密码</label>
            <input 
              v-model="form.password" 
              type="password"
              autocomplete="new-password"
              class="register__form-input"
              :class="{ 'register__form-input--error': errors.password }"
              placeholder="请输入密码（8位以上，含大小写字母和数字）"
              @blur="validatePasswordField"
              @input="errors.password && validatePasswordField()"
            />
            <span v-if="errors.password" class="register__form-error">{{ errors.password }}</span>
          </div>
          
          <div class="register__form-group">
            <label class="register__form-label">确认密码</label>
            <input 
              v-model="form.confirmPassword" 
              type="password" 
              class="register__form-input"
              :class="{ 'register__form-input--error': errors.confirmPassword }"
              placeholder="请再次输入密码"
              @blur="validateConfirmPassword"
              @input="errors.confirmPassword && validateConfirmPassword()"
            />
            <span v-if="errors.confirmPassword" class="register__form-error">{{ errors.confirmPassword }}</span>
          </div>
          
          <div class="register__form-group">
            <label class="register__form-label">验证码</label>
            <div class="register__captcha-row">
              <input 
                v-model="form.captcha" 
                type="text"
                maxlength="6"
                class="register__form-input register__form-input--captcha"
                :class="{ 'register__form-input--error': errors.captcha }"
                placeholder="请输入6位验证码"
                @blur="validateCaptcha"
                @input="errors.captcha && validateCaptcha()"
              />
              <button 
                type="button"
                class="register__captcha-btn"
                :disabled="isSendingCaptcha || countdown > 0 || !isEmailValid"
                @click="handleSendCaptcha"
              >
                <span v-if="isSendingCaptcha">发送中...</span>
                <span v-else-if="countdown > 0">{{ countdown }}s</span>
                <span v-else>发送验证码</span>
              </button>
            </div>
            <span v-if="errors.captcha" class="register__form-error">{{ errors.captcha }}</span>
            <span v-if="captchaMessage" class="register__form-hint">{{ captchaMessage }}</span>
          </div>
          
          <label class="register__form-checkbox">
            <input type="checkbox" v-model="agreeTerms" />
            <span>我已阅读并同意</span>
            <a href="#" class="register__form-link">服务条款</a>
            <span>和</span>
            <a href="#" class="register__form-link">隐私政策</a>
          </label>
          
          <button 
            type="submit" 
            class="register__form-btn"
            :disabled="isLoading || !agreeTerms || !isValid"
          >
            {{ isLoading ? '注册中...' : '注册' }}
          </button>
          
          <div class="register__divider">
            <span class="register__divider-text">或</span>
          </div>
          
          <button 
            type="button"
            class="register__form-btn register__form-btn--outline"
            @click="handleGuestLogin"
          >
            游客模式
          </button>
        </form>
        
        <p class="register__form-footer">
          已有账号? 
          <router-link to="/login" class="register__form-link">立即登录</router-link>
        </p>
        
        <div v-if="message" class="register__message" :class="messageType">
          {{ message }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { validateQqEmail, validatePassword, validateNickname } from '@/utils/validators'

const IS_MOCK = import.meta.env.VITE_ENABLE_MOCK === 'true'

const authStore = useAuthStore()
const router = useRouter()

const form = reactive({
  email: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  captcha: ''
})

const agreeTerms = ref(false)
const isLoading = ref(false)
const isSendingCaptcha = ref(false)
const countdown = ref(0)
const message = ref('')
const messageType = ref<'success' | 'error'>('success')
const captchaMessage = ref('')

const errors = reactive({
  email: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  captcha: ''
})

const isEmailValid = computed(() => {
  return validateQqEmail(form.email).valid && form.email.trim() !== ''
})

const isValid = computed(() => {
  const baseValid = isEmailValid.value && 
         !errors.nickname && form.nickname.trim() !== '' &&
         !errors.password && form.password.trim() !== '' &&
         !errors.confirmPassword && form.confirmPassword.trim() !== '' &&
         agreeTerms.value
  
  if (IS_MOCK) {
    return baseValid
  }
  
  return baseValid && !errors.captcha && form.captcha.trim() !== ''
})

const validateEmail = (): boolean => {
  const result = validateQqEmail(form.email)
  errors.email = result.valid ? '' : result.message
  return result.valid
}

const validateNicknameField = (): boolean => {
  const result = validateNickname(form.nickname)
  errors.nickname = result.valid ? '' : result.message
  return result.valid
}

const validatePasswordField = (): boolean => {
  const result = validatePassword(form.password)
  errors.password = result.valid ? '' : result.message
  return result.valid
}

const validateConfirmPassword = (): boolean => {
  if (!form.confirmPassword) {
    errors.confirmPassword = '请确认密码'
    return false
  }
  if (form.password !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    return false
  }
  errors.confirmPassword = ''
  return true
}

const validateCaptcha = (): boolean => {
  if (!form.captcha) {
    errors.captcha = '请输入验证码'
    return false
  }
  if (!/^\d{6}$/.test(form.captcha)) {
    errors.captcha = '验证码为6位数字'
    return false
  }
  errors.captcha = ''
  return true
}

const handleSendCaptcha = async () => {
  if (!isEmailValid.value) {
    errors.email = '请输入正确的QQ邮箱'
    return
  }
  
  isSendingCaptcha.value = true
  
  const result = await authStore.sendCaptcha(form.email, 'register')
  
  if (result.success) {
    captchaMessage.value = result.message
    countdown.value = 60
    
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } else {
    message.value = result.message
    messageType.value = 'error'
  }
  
  isSendingCaptcha.value = false
}

const handleRegister = async () => {
  validateEmail()
  validateNicknameField()
  validatePasswordField()
  validateConfirmPassword()
  if (!IS_MOCK) {
    validateCaptcha()
  }
  
  if (!isValid.value) return
  
  isLoading.value = true
  message.value = ''
  
  const result = await authStore.register({
    email: form.email,
    nickname: form.nickname,
    password: form.password,
    confirmPassword: form.confirmPassword,
    captcha: form.captcha
  })
  
  if (result.success) {
    message.value = result.message
    messageType.value = 'success'
    setTimeout(() => {
      router.push('/dashboard')
    }, 1500)
  } else {
    message.value = result.message
    messageType.value = 'error'
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
.register {
  min-height: 100vh;
  display: flex;
}

.register__left {
  width: 50%;
  background: linear-gradient(135deg, $info-color 0%, $primary-color 100%);
  padding: $spacing-2xl;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #ffffff;
}

.register__logo {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-xl;
}

.register__logo-icon {
  font-size: 40px;
}

.register__logo-text {
  font-size: 28px;
  font-weight: 700;
}

.register__title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: $spacing-md;
}

.register__desc {
  font-size: $font-size-lg;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: $spacing-xl;
}

.register__features {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-lg;
}

.register__feature {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: rgba(255, 255, 255, 0.1);
  border-radius: $border-radius-md;
}

.register__feature-icon {
  font-size: 24px;
}

.register__feature-text {
  font-size: $font-size-sm;
}

.register__right {
  width: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-xl;
  background: $bg-secondary;
}

.register__form-container {
  width: 100%;
  max-width: 450px;
  background: $bg-primary;
  padding: $spacing-xl;
  border-radius: $border-radius-xl;
  box-shadow: $shadow-xl;
}

.register__back-link {
  display: inline-block;
  font-size: $font-size-sm;
  color: $text-secondary;
  text-decoration: none;
  margin-bottom: $spacing-lg;
  transition: color $transition-fast;
  
  &:hover {
    color: $primary-color;
    text-decoration: underline;
  }
}

.register__form-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: $spacing-lg;
  text-align: center;
}

.register__form {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.register__form-group {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.register__form-label {
  font-size: $font-size-sm;
  font-weight: 500;
  color: $text-primary;
}

.register__form-input {
  padding: $spacing-md;
  border: 1px solid $border-color;
  border-radius: $border-radius-md;
  font-size: $font-size-base;
  transition: border-color $transition-fast;
  
  &:focus {
    outline: none;
    border-color: $primary-color;
  }

  &--error {
    border-color: $error-color;

    &:focus {
      border-color: $error-color;
    }
  }
}

.register__form-error {
  font-size: $font-size-xs;
  color: $error-color;
}

.register__form-hint {
  font-size: $font-size-xs;
  color: $success-color;
}

.register__captcha-row {
  display: flex;
  gap: $spacing-sm;
}

.register__form-input--captcha {
  flex: 1;
}

.register__captcha-btn {
  padding: $spacing-md $spacing-lg;
  background: $primary-color;
  color: #ffffff;
  border: none;
  border-radius: $border-radius-md;
  font-size: $font-size-sm;
  font-weight: 500;
  cursor: pointer;
  transition: background $transition-fast;
  
  &:hover:not(:disabled) {
    background: $primary-dark;
  }
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    background: $border-color;
  }
}

.register__form-checkbox {
  font-size: $font-size-sm;
  color: $text-secondary;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  flex-wrap: wrap;
}

.register__form-link {
  color: $primary-color;
  text-decoration: none;
  
  &:hover {
    text-decoration: underline;
  }
}

.register__form-btn {
  padding: $spacing-md;
  background: $primary-color;
  color: #ffffff;
  border: none;
  border-radius: $border-radius-md;
  font-size: $font-size-base;
  font-weight: 500;
  cursor: pointer;
  transition: background $transition-fast;
  
  &:hover:not(:disabled) {
    background: $primary-dark;
  }
  
  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
  
  &--outline {
    background: transparent;
    color: $text-primary;
    border: 1px solid $border-color;
    
    &:hover {
      background: $bg-secondary;
    }
  }
}

.register__divider {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin: $spacing-md 0;
}

.register__divider::before,
.register__divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: $border-color;
}

.register__divider-text {
  font-size: $font-size-xs;
  color: $text-muted;
}

.register__form-footer {
  text-align: center;
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-top: $spacing-md;
}

.register__message {
  margin-top: $spacing-md;
  padding: $spacing-md;
  border-radius: $border-radius-md;
  text-align: center;
  font-size: $font-size-sm;
  
  &.success {
    background: rgba($success-color, 0.1);
    color: $success-color;
  }
  
  &.error {
    background: rgba($error-color, 0.1);
    color: $error-color;
  }
}
</style>