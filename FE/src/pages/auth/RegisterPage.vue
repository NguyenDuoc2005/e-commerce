<template>
  <div class="container py-3">
    <div class="row align-items-center">
      <BreadCrumbUser :routes="breadcrumbRoutes" title="Đăng ký tại đây" />
    </div>
  </div>

  <div class="container">
    <div class="d-flex justify-content-center">
      <div class="card shadow p-4" style="max-width: 400px; width: 100%;">
        <h4 class="text-center mb-2 fw-semibold text-dark">ĐĂNG KÝ</h4>
        <p class="text-center text-muted small">
          Đã có tài khoản?
          <a href="/login" class="text-decoration-none fw-medium" style="color: #4fc3f7;">Đăng nhập tại đây</a>
        </p>

        <!-- Hiện lỗi tổng nếu có -->
        <div v-if="errors.server" class="alert alert-danger p-2 text-center mb-2" role="alert">
          {{ errors.server }}
        </div>

        <form @submit.prevent="onRegister">
          <!-- Họ tên -->
          <div class="mb-3">
            <input v-model="form.hoTen" type="text" class="form-control" placeholder="Họ tên" />
            <small class="text-danger" v-if="errors.hoTen">{{ errors.hoTen }}</small>
          </div>

          <!-- Email -->
          <div class="mb-3">
            <input v-model="form.email" type="email" class="form-control" placeholder="Email" />
            <small class="text-danger" v-if="errors.email">{{ errors.email }}</small>
          </div>

          <!-- Số điện thoại -->
          <div class="mb-3">
            <input v-model="form.soDienThoai" type="text" class="form-control" placeholder="Số điện thoại" />
            <small class="text-danger" v-if="errors.soDienThoai">{{ errors.soDienThoai }}</small>
          </div>

          <!-- Mật khẩu -->
          <div class="mb-3">
            <div class="position-relative">
              <input
                v-model="form.matKhau"
                :type="showPassword ? 'text' : 'password'"
                class="form-control"
                placeholder="Mật khẩu"
                autocomplete="new-password"
              />
              <span
                class="toggle-eye"
                @click="showPassword = !showPassword"
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
            <small class="text-danger" v-if="errors.matKhau">{{ errors.matKhau }}</small>
          </div>

          <!-- Xác nhận mật khẩu -->
          <div class="mb-3">
            <div class="position-relative">
              <input
                v-model="form.xacNhanMatKhau"
                :type="showConfirmPassword ? 'text' : 'password'"
                class="form-control"
                placeholder="Xác nhận mật khẩu"
                autocomplete="new-password"
              />
              <span
                class="toggle-eye"
                @click="showConfirmPassword = !showConfirmPassword"
                :title="showConfirmPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
              >
                <svg v-if="showConfirmPassword" xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M16 8s-3-5.5-8-5.5S0 8 0 8s3 5.5 8 5.5S16 8 16 8zM1.173 8a13.133 13.133 0 0 1 1.66-2.043C4.12 4.668 5.88 3.5 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13.133 13.133 0 0 1 14.828 8c-.058.087-.122.183-.195.288a13.133 13.133 0 0 1-1.66 2.043C11.88 11.332 10.12 12.5 8 12.5c-2.12 0-3.879-1.168-5.168-2.457A13.133 13.133 0 0 1 1.172 8z"/>
                  <path d="M8 5.5a2.5 2.5 0 1 1 0 5 2.5 2.5 0 0 1 0-5zm0 1a1.5 1.5 0 1 0 0 3 1.5 1.5 0 0 0 0-3z"/>
                </svg>
                <svg v-else xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M13.359 11.238l2.122 2.122-.708.708-2.122-2.122C11.12 12.331 9.44 13.5 8 13.5c-2.12 0-3.879-1.168-5.168-2.457A13.133 13.133 0 0 1 1.172 8a13.133 13.133 0 0 1 1.66-2.043c.465-.488 1.008-.976 1.619-1.432L2.146 3.854l.708-.708 12 12-.708.708-2.122-2.122zm-1.033-1.033l-2.07-2.07a2 2 0 0 0-2.364-2.364l-2.07-2.07C4.12 4.669 5.88 3.5 8 3.5c2.12 0 3.88 1.169 5.168 2.457A13.133 13.133 0 0 1 14.828 8a13.133 13.133 0 0 1-1.66 2.043c-.368.386-.783.772-1.221 1.162z"/>
                </svg>
              </span>
            </div>
            <small class="text-danger" v-if="errors.xacNhanMatKhau">{{ errors.xacNhanMatKhau }}</small>
          </div>

          <div class="d-grid mb-2">
            <button type="submit" class="btn btn-custom-green text-white fw-medium" :disabled="loading">
              <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
              Đăng ký
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import BreadCrumbUser from '@/components/ui/Breadcrumbs/BreadCrumbUser.vue'
import { Register } from '@/services/api/auth/authentication.api'
import { message } from 'ant-design-vue'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const breadcrumbRoutes = [
  { name: 'Trang chủ', path: '/' },
  { name: 'Đăng ký' }
]

const form = reactive({
  hoTen: '',
  email: '',
  soDienThoai: '',
  matKhau: '',
  xacNhanMatKhau: ''
})

const errors = reactive({
  hoTen: '',
  email: '',
  soDienThoai: '',
  matKhau: '',
  xacNhanMatKhau: '',
  server: ''
})

const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

// Regex mới: miễn có chữ & số, cho phép ký tự đặc biệt
const validate = () => {
  let valid = true

  // Reset lỗi
  errors.hoTen = ''
  errors.email = ''
  errors.soDienThoai = ''
  errors.matKhau = ''
  errors.xacNhanMatKhau = ''
  errors.server = ''

  // Họ tên
  if (!form.hoTen.trim()) {
    errors.hoTen = 'Họ tên không được để trống'
    valid = false
  } else if (form.hoTen.length < 2 || form.hoTen.length > 50) {
    errors.hoTen = 'Họ tên phải từ 2 đến 50 ký tự'
    valid = false
  }

  // Email
  if (!form.email.trim()) {
    errors.email = 'Email không được để trống'
    valid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
    errors.email = 'Email không hợp lệ'
    valid = false
  }

  // Số điện thoại
  if (!form.soDienThoai.trim()) {
    errors.soDienThoai = 'Số điện thoại không được để trống'
    valid = false
  } else if (!/^(0[3|5|7|8|9])+([0-9]{8,9})$/.test(form.soDienThoai.trim())) {
    errors.soDienThoai = 'Số điện thoại không hợp lệ'
    valid = false
  }

  // Mật khẩu: Có chữ, có số, ≥ 6 ký tự, cho phép ký tự đặc biệt
  if (!form.matKhau) {
    errors.matKhau = 'Mật khẩu không được để trống'
    valid = false
  } else if (form.matKhau.length < 6 || form.matKhau.length > 100) {
    errors.matKhau = 'Mật khẩu phải tối thiểu 6 ký tự và không quá 100 ký tự'
    valid = false
  } else if (!/^(?=.*[A-Za-z])(?=.*\d).{6,}$/.test(form.matKhau)) {
    errors.matKhau = 'Mật khẩu phải chứa cả chữ và số'
    valid = false
  }

  // Xác nhận mật khẩu
  if (!form.xacNhanMatKhau) {
    errors.xacNhanMatKhau = 'Vui lòng xác nhận mật khẩu'
    valid = false
  } else if (form.xacNhanMatKhau !== form.matKhau) {
    errors.xacNhanMatKhau = 'Mật khẩu xác nhận không khớp'
    valid = false
  }

  return valid
}
const resetForm = () => {
  form.hoTen = ''
  form.email = ''
  form.soDienThoai = ''
  form.matKhau = ''
  form.xacNhanMatKhau = ''
}

const onRegister = async () => {
  if (!validate()) {
    return
  }
  loading.value = true
  errors.server = ''

  try {
    const res = await Register({
      userName: form.hoTen.trim(),
      email: form.email.trim(),
      phone: form.soDienThoai.trim(),
      password: form.matKhau
    })

    if (res?.success) {
      message.success('Đăng ký thành công! Vui lòng đăng nhập.')
      resetForm()
      setTimeout(() => {
        router.push('/login')
      }, 1200)
    }
  } catch (err: any) {
    errors.server = err?.response?.data?.message || 'Đã có lỗi xảy ra, vui lòng thử lại!'
  } finally {
    loading.value = false
  }
}

</script>

<style scoped>
.card {
  border-radius: 0.75rem;
}

.btn-custom-green {
  background-color: #4fc3f7;
  border: none;
  transition: background-color 0.3s ease-in-out;
}

.btn-custom-green:hover {
  background-color: #03a9f4;
}

/* Eye icon style fix lệch khi hiện lỗi */
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
