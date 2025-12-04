<template>
  <div class="login-page">
    <!-- Background -->
    <div class="bg-overlay"></div>
    
    <!-- Header -->
    <div class="header">
      <div class="container">
        <BreadCrumbUser :routes="breadcrumbRoutes" title="Đăng nhập tài khoản" />
      </div>
    </div>

    <!-- Main Content -->
    <div class="main-content">
      <div class="container">
        <div class="login-wrapper">
          <div class="login-box">
            
            <!-- Brand -->
            <div class="brand-section">
              <div class="brand-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2L13.09 8.26L22 9L13.09 9.74L12 16L10.91 9.74L2 9L10.91 8.26L12 2Z" fill="white"/>
                </svg>
              </div>
              <h1 class="brand-title">GLAMSOLE</h1>
              <p class="brand-desc">Chuyên giày thể thao cao cấp</p>
            </div>

            <!-- Login Form -->
            <div class="form-container">
              <div class="form-header">
                <h2>Đăng nhập</h2>
                <p>Chào mừng bạn quay trở lại!</p>
              </div>

              <form @submit.prevent="onLogin" class="login-form">
                
                <!-- Email Field -->
                <div class="field-group">
                  <label>Email đăng nhập</label>
                  <div class="input-container">
                    <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none">
                      <rect x="2" y="4" width="20" height="16" rx="2" stroke="currentColor" stroke-width="2"/>
                      <path d="m22 7-10 5L2 7" stroke="currentColor" stroke-width="2"/>
                    </svg>
                    <input 
                      v-model="form.email" 
                      type="email" 
                      placeholder="Nhập email của bạn"
                      :class="['field-input', { 'error': errors.email }]"
                    />
                  </div>
                  <div v-if="errors.email" class="error-text">{{ errors.email }}</div>
                </div>

                <!-- Password Field -->
                <div class="field-group">
                  <label>Mật khẩu</label>
                  <div class="input-container">
                    <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none">
                      <rect x="3" y="11" width="18" height="10" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
                      <circle cx="12" cy="16" r="1" fill="currentColor"/>
                      <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="2"/>
                    </svg>
                    <input 
                      v-model="form.password"
                      :type="showPassword ? 'text' : 'password'"
                      placeholder="Nhập mật khẩu"
                      :class="['field-input', { 'error': errors.password }]"
                      autocomplete="current-password"
                    />
                    <button type="button" class="toggle-btn" @click="togglePassword">
                      <svg v-if="showPassword" width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="2"/>
                        <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
                      </svg>
                      <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none">
                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" stroke="currentColor" stroke-width="2"/>
                        <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="2"/>
                      </svg>
                    </button>
                  </div>
                  <div v-if="errors.password" class="error-text">{{ errors.password }}</div>
                </div>

                <!-- Login Button -->
                <button type="submit" class="login-button" :disabled="loading">
                  <div v-if="loading" class="loading">
                    <div class="spinner"></div>
                    <span>Đang xử lý...</span>
                  </div>
                  <div v-else class="login-content">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                      <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" stroke="currentColor" stroke-width="2"/>
                      <polyline points="10,17 15,12 10,7" stroke="currentColor" stroke-width="2"/>
                      <line x1="15" y1="12" x2="3" y2="12" stroke="currentColor" stroke-width="2"/>
                    </svg>
                    <span>Đăng nhập</span>
                  </div>
                </button>


              </form>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import BreadCrumbUser from '@/components/ui/Breadcrumbs/BreadCrumbUser.vue'
import { loginAdmin } from '@/services/api/auth/authentication.api'
import { getUserInformation } from '@/utils/token.helper'
import { useAuthStore } from '@/stores/auth'
import { router } from '@/routes/router'
import { ROUTES_CONSTANTS } from '@/constants/path'

const form = reactive({
  email: '',
  password: ''
})

const errors = reactive({
  email: '',
  password: ''
})

const showPassword = ref(false)
const loading = ref(false)
const breadcrumbRoutes = [
  { name: 'Trang chủ', path: '/admin' },
  { name: 'Đăng nhập' }
]

const authStore = useAuthStore()

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

const onLogin = async () => {
  loading.value = true
  errors.email = ''
  errors.password = ''

  // Validate
  if (!form.email.trim()) {
    errors.email = 'Vui lòng nhập email'
  }
  if (!form.password.trim()) {
    errors.password = 'Vui lòng nhập mật khẩu'
  }

  // Nếu có lỗi validate thì không gọi API và ngừng loading
  if (errors.email || errors.password) {
    loading.value = false
    return
  }

  try {
    const payload = {
      email: form.email,
      password: form.password
    }
    const res = await loginAdmin(payload)

    // Chỉ redirect khi đăng nhập thành công (có accessToken)
    if (res && res.data && res.data.accessToken) {
      const accessToken = res.data.accessToken;
      const refreshToken = res.data.refreshToken;
      const userInfo = getUserInformation(accessToken)

      authStore.login({
        user: userInfo,
        accessToken,
        refreshToken
      })

      router.push({ name: ROUTES_CONSTANTS.ADMIN.children.BAN_HANG.name })
    } 
  } catch (err: any) {
    const errorMessage = err?.response?.data?.message ?? 'Đã xảy ra lỗi. Vui lòng thử lại.'
    errors.password = errorMessage 
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* Base Styles */
.login-page {
  min-height: 100vh;
  position: relative;
  background: linear-gradient(135deg, #58bddb 0%, #4a9cc9 50%, #3a86b4 100%);
}

.bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url('https://images.unsplash.com/photo-1556906781-9a412961c28c?ixlib=rb-4.0.3&auto=format&fit=crop&w=2000&q=80');
  background-size: cover;
  background-position: center;
  opacity: 0.3;
}

.login-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(88, 189, 219, 0.9) 0%, rgba(74, 156, 201, 0.95) 100%);
  z-index: 1;
}

/* Header */
.header {
  position: relative;
  z-index: 10;
  padding: 1rem 0;
}

/* Main Content */
.main-content {
  position: relative;
  z-index: 2;
  padding: 2rem 0;
  min-height: calc(100vh - 100px);
  display: flex;
  align-items: center;
}

.login-wrapper {
  display: flex;
  justify-content: center;
}

.login-box {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  width: 100%;
  max-width: 400px;
}

/* Brand Section */
.brand-section {
  background: linear-gradient(135deg, #58bddb 0%, #4a9cc9 100%);
  padding: 2rem;
  text-align: center;
  color: white;
}

.brand-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
}

.brand-title {
  font-size: 1.75rem;
  font-weight: 800;
  margin: 0 0 0.5rem 0;
  letter-spacing: 1px;
}

.brand-desc {
  margin: 0;
  opacity: 0.9;
  font-size: 0.9rem;
}

/* Form Container */
.form-container {
  padding: 2rem;
}

.form-header {
  text-align: center;
  margin-bottom: 2rem;
}

.form-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.form-header p {
  color: #6b7280;
  margin: 0;
  font-size: 0.9rem;
}

/* Form Fields */
.field-group {
  margin-bottom: 1.5rem;
}

.field-group label {
  display: block;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.input-container {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
}

.field-input {
  width: 100%;
  height: 44px;
  padding: 0 44px 0 40px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: all 0.2s ease;
  background: white;
}

.field-input:focus {
  outline: none;
  border-color: #58bddb;
  box-shadow: 0 0 0 3px rgba(88, 189, 219, 0.1);
}

.field-input.error {
  border-color: #ef4444;
}

.toggle-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: color 0.2s;
}

.toggle-btn:hover {
  color: #58bddb;
}

.error-text {
  color: #ef4444;
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

/* Login Button */
.login-button {
  width: 100%;
  height: 44px;
  background: linear-gradient(135deg, #58bddb 0%, #4a9cc9 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #4a9cc9 0%, #3a86b4 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(88, 189, 219, 0.3);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.login-content,
.loading {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Forgot Password */
.forgot-section {
  text-align: center;
  margin-bottom: 1.5rem;
}

.forgot-link {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  color: #58bddb;
  text-decoration: none;
  font-size: 0.85rem;
  font-weight: 500;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: #4a9cc9;
}

/* Form Footer */
.form-footer {
  text-align: center;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
  font-size: 0.9rem;
  color: #6b7280;
}

.signup-link {
  color: #58bddb;
  text-decoration: none;
  font-weight: 600;
  margin-left: 0.25rem;
}

.signup-link:hover {
  text-decoration: underline;
}

/* Responsive */
@media (max-width: 576px) {
  .main-content {
    padding: 1rem 0;
  }
  
  .login-box {
    margin: 0 1rem;
  }
  
  .brand-section {
    padding: 1.5rem;
  }
  
  .form-container {
    padding: 1.5rem;
  }
  
  .brand-title {
    font-size: 1.5rem;
  }
}

@media (max-height: 700px) {
  .main-content {
    padding: 1rem 0;
    min-height: auto;
  }
  
  .brand-section {
    padding: 1.5rem;
  }
  
  .form-container {
    padding: 1.5rem;
  }
}

/* Animation */
.login-box {
  animation: slideIn 0.5s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>