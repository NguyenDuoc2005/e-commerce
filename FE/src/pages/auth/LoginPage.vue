<template>
  <div class="container py-3">
    <div class="row align-items-center">
      <BreadCrumbUser :routes="breadcrumbRoutes" title="Đăng nhập tại đây" />
    </div>
  </div>
  <div class="container ">
    <div class="d-flex justify-content-center">
      <div class="card shadow p-4" style="max-width: 400px; width: 100%;">
        <h4 class="text-center mb-2 fw-semibold text-dark">ĐĂNG NHẬP</h4>
        <p class="text-center text-muted small">
          Nếu bạn chưa có tài khoản,
          <a href="/register" class="fw-medium text-decoration-none" style="color: #4fc3f7;">đăng ký tại đây</a>
        </p>
        <form @submit.prevent="onLogin">
          <!-- Email -->
          <div class="mb-3">
            <input v-model="form.email" type="email" class="form-control" placeholder="Email" autocomplete="username" />
            <small class="text-danger" v-if="errors.email">{{ errors.email }}</small>
          </div>
          <!-- Mật khẩu + icon -->
          <div class="mb-3">
            <div class="position-relative">
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                class="form-control"
                placeholder="Mật khẩu"
                autocomplete="current-password"
              />
              <span
                class="toggle-eye"
                @click="togglePassword"
                :title="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
              >
                <svg v-if="showPassword" xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M16 8s-3-5.5-8-5.5S0 8 0 8s3 5.5 8 5.5S16 8 16 8zM1.173 8a13.133 13.133 0 0 1 1.66-2.043C4.12 4.668 5.88 3.5 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13.133 13.133 0 0 1 14.828 8c-.058.087-.122.183-.195.288a13.133 13.133 0 0 1-1.66 2.043C11.88 11.332 10.12 12.5 8 12.5c-2.12 0-3.879-1.168-5.168-2.457A13.133 13.133 0 0 1 1.172 8z"/>
                  <path d="M8 5.5a2.5 2.5 0 1 1 0 5 2.5 2.5 0 0 1 0-5zm0 1a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3z"/>
                </svg>
                <svg v-else xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M13.359 11.238l2.122 2.122-.708.708-2.122-2.122C11.12 12.331 9.44 13.5 8 13.5c-2.12 0-3.879-1.168-5.168-2.457A13.133 13.133 0 0 1 1.172 8a13.133 13.133 0 0 1 1.66-2.043c.465-.488 1.008-.976 1.619-1.432L2.146 3.854l.708-.708 12 12-.708.708-2.122-2.122zm-1.033-1.033l-2.07-2.07a2 2 0 0 0-2.364-2.364l-2.07-2.07C4.12 4.669 5.88 3.5 8 3.5c2.12 0 3.88 1.169 5.168 2.457A13.133 13.133 0 0 1 14.828 8a13.133 13.133 0 0 1-1.66 2.043c-.368.386-.783.772-1.221 1.162z"/>
                </svg>
              </span>
            </div>
            <small class="text-danger" v-if="errors.password">{{ errors.password }}</small>
          </div>
          <!-- Nút Đăng nhập -->
          <div class="d-grid mb-2">
            <button type="submit" class="btn text-white d-flex justify-content-center align-items-center"
              :disabled="loading" style="background-color: #4fc3f7; height: 38px;">
              <span v-if="!loading">Đăng nhập</span>
              <div v-else class="spinner-border spinner-border-sm text-light" role="status"></div>
            </button>
          </div>
          <!-- Đăng nhập mạng xã hội -->
          <div class="text-center text-muted small mb-2">Hoặc đăng nhập bằng</div>
          <!-- <div class="d-grid gap-2 mb-3">
            <button type="button" class="btn d-flex align-items-center justify-content-center gap-2 border rounded py-2"
              style="background-color: #fff; color: #444;" @click="loginWithGoogle">
              <GoogleOutlined style="font-size: 18px; color: #DB4437;" />
              <span style="font-size: 0.9rem;">Đăng nhập với Google</span>
            </button>
            <button type="button" class="btn d-flex align-items-center justify-content-center gap-2 border rounded py-2"
              style="background-color: #fff; color: #000;" @click="loginWithGithub">
              <GithubOutlined style="font-size: 18px; color: #000;" />
              <span style="font-size: 0.9rem;">Đăng nhập với GitHub</span>
            </button>
          </div> -->
          <!-- Quên mật khẩu -->
          <div class="text-center">
            <a href="#" class="small text-muted text-decoration-none">Quên mật khẩu?</a>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { GoogleOutlined, GithubOutlined } from '@ant-design/icons-vue'
import BreadCrumbUser from '@/components/ui/Breadcrumbs/BreadCrumbUser.vue'
import { URL_OAUTH2_GITHUB_USERS, URL_OAUTH2_GOOGLE_USERS } from '@/constants/url'
import { cookieStorageAction } from '@/utils/storage'
import {
  ACCOUNT_EXIST, ACCOUNT_EXIST_MESSAGE,
  ACCOUNT_NOT_EXIST, ACCOUNT_NOT_EXIST_MESSAGE,
  Registered_Awaiting_Confirmation, Registered_Awaiting_Confirmation_MESSAGE,
  Unverified_Account, Unverified_Account_MESSAGE
} from '@/constants/cookie.constant'
import { toast } from 'vue3-toastify'
import { loginUser } from '@/services/api/auth/authentication.api'
import { getUserInformation } from '@/utils/token.helper'
import { router } from '@/routes/router'
import { ROUTES_CONSTANTS } from '@/constants/path'
import { useAuthStore } from '@/stores/auth'

const loading = ref(false)

const form = reactive({
  email: '',
  password: ''
})

const errors = reactive({
  email: '',
  password: ''
})

const showPassword = ref(false)

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

const authStore = useAuthStore()

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

  if (!errors.email && !errors.password) {
    try {
      const payload = {
        email: form.email,
        password: form.password
      }
      const res = await loginUser(payload)
      const accessToken = res.data.accessToken;
      const refreshToken = res.data.refreshToken;
      const userInfo = getUserInformation(accessToken)
      authStore.login({
        user: userInfo,
        accessToken,
        refreshToken
      })
      router.push({ name: ROUTES_CONSTANTS.USERS.children.TRANGCHU.name }).then(() => {
        window.location.reload();
      })
    } catch (err: any) {
      const errorMessage = err?.response?.data?.message ?? 'Đã xảy ra lỗi. Vui lòng thử lại.'
      errors.password = errorMessage
    }
    finally {
      loading.value = false
    }
  } else {
    loading.value = false
  }
}

const breadcrumbRoutes = [
  { name: 'Trang chủ', path: '/' },
  { name: 'Đăng nhập' }
]

const loginWithGoogle = () => {
  window.location.href = URL_OAUTH2_GOOGLE_USERS();
}
const loginWithGithub = () => {
  window.location.href = URL_OAUTH2_GITHUB_USERS();
}

onMounted(() => {
  const accountNotExistError = cookieStorageAction.get(ACCOUNT_NOT_EXIST)
  const accountExistError = cookieStorageAction.get(ACCOUNT_EXIST)
  const unverified_Account = cookieStorageAction.get(Unverified_Account)
  const registered_Awaiting_Confirmation = cookieStorageAction.get(Registered_Awaiting_Confirmation)
  if (accountNotExistError) {
    toast.error(ACCOUNT_NOT_EXIST_MESSAGE)
    cookieStorageAction.remove(ACCOUNT_NOT_EXIST)
  }
  if (accountExistError) {
    toast.error(ACCOUNT_EXIST_MESSAGE)
    cookieStorageAction.remove(ACCOUNT_EXIST)
  }
  if (unverified_Account) {
    toast.warn(Unverified_Account_MESSAGE)
    cookieStorageAction.remove(Unverified_Account)
  }
  if (registered_Awaiting_Confirmation) {
    toast.info(Registered_Awaiting_Confirmation_MESSAGE)
    cookieStorageAction.remove(Registered_Awaiting_Confirmation)
  }
})
</script>

<style scoped>
.card {
  border-radius: 0.75rem;
}
/* Eye icon đẹp, không lệch khi hiện lỗi */
.toggle-eye {
  position: absolute;
  top: 0;
  bottom: 0;
  right: 12px;
  margin: auto 0;
  height: 22px;
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #757575;
  z-index: 2;
  user-select: none;
}
.toggle-eye:hover {
  color: #03a9f4;
}
.position-relative {
  position: relative;
}
</style>
