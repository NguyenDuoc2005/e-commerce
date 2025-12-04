<script setup>
import { ref, computed, defineProps } from 'vue'
import { useRouter } from 'vue-router'
import { useSidebarStore } from '@/stores/sidebar'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()

const { toggleSidebar } = useSidebarStore()
const sidebarStore = useSidebarStore()

const props = defineProps({
  isNoSidebarPage: Boolean
})

// Get user login data
const authStore = useAuthStore()
const userLogin = computed(() => authStore.user)

const headerStyle = computed(() => {
  return props.isNoSidebarPage
    ? { backgroundImage: "url('/images/quiz-room-background-1.png')" }
    : { backgroundColor: 'white' }
})

const showDropdown = ref(false) // to toggle dropdown visibility

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

const closeDropdown = () => {
  showDropdown.value = false
}

const logout = () => {
  authStore.logout() // Xóa dữ liệu đăng nhập
  closeDropdown()
  router.push('/admin/login') // Điều hướng đúng cách
}
</script>


<template>
  <header :class="['position-sticky top-0 z-3 w-100 shadow-sm', props.isNoSidebarPage ? 'bg-dark' : 'bg-white']"
    :style="headerStyle">
    <div class="d-flex align-items-center justify-content-between py-3 px-4 shadow-sm">
      <!-- Hamburger Toggle BTN -->
      <button class="btn btn-outline-secondary d-lg-none" @click="toggleSidebar">
        <span class="position-relative d-block" style="width: 24px; height: 24px;">
          <span class="position-absolute start-0 top-0 w-100 bg-dark" :class="{ 'd-none': sidebarStore.isSidebarOpen }"
            style="height: 3px; transition: 0.3s;"></span>
          <span class="position-absolute start-0 top-50 translate-middle-y w-100 bg-dark"
            :class="{ 'd-none': sidebarStore.isSidebarOpen }" style="height: 3px; transition: 0.3s;"></span>
          <span class="position-absolute start-0 bottom-0 w-100 bg-dark"
            :class="{ 'd-none': sidebarStore.isSidebarOpen }" style="height: 3px; transition: 0.3s;"></span>

          <!-- X icon -->
          <span class="position-absolute start-50 top-50 translate-middle d-none bg-dark"
            :class="{ 'd-block': sidebarStore.isSidebarOpen }"
            style="width: 20px; height: 3px; rotate: 45deg; transition: 0.3s;"></span>
          <span class="position-absolute start-50 top-50 translate-middle d-none bg-dark"
            :class="{ 'd-block': sidebarStore.isSidebarOpen }"
            style="width: 20px; height: 3px; rotate: -45deg; transition: 0.3s;"></span>
        </span>
      </button>

      <!-- Right-aligned profile or login button -->
      <div class="d-flex align-items-center gap-3 ml-auto">
        <!-- Conditionally show the user's profile or login button -->
        <div v-if="userLogin" class="d-flex align-items-center gap-2" @click="toggleDropdown" style="cursor: pointer;">
          <!-- Avatar and username -->
          <img :src="userLogin.pictureUrl || '/images/default-avatar.png'" alt="User Avatar" class="rounded-circle"
            style="width: 35px; height: 35px; object-fit: cover;" />
          <span class="text-dark fw-bold fs-6">{{ userLogin.fullName || 'Tài khoản' }}</span>
        </div>

        <div v-else>
          <!-- Login button -->
          <RouterLink to="/admin/login">
            <button class="btn btn-outline-secondary btn-sm">Đăng nhập</button>
          </RouterLink>

        </div>

        <!-- Dropdown for logged-in users -->
        <div v-show="showDropdown && userLogin" class="account-dropdown-menu shadow-sm">
          <a href="#" class="dropdown-entry border-top" @click.prevent="logout">
            <LoginOutlined class="me-2 icon-thin" />
            <span class="text">Đăng xuất</span>
          </a>
        </div>
      </div>
    </div>
  </header>
</template>


<style scoped>
header {
  z-index: 9999;
  font-family: Arial, sans-serif;
}

.shadow-sm {
  background-color: #f7f8fb;
}

.d-flex.align-items-center {
  align-items: center;
}

.ms-2 {
  margin-left: 0.5rem;
}

.text-dark {
  color: #333;
}

.fw-bold {
  font-weight: 600;
}

.fs-6 {
  font-size: 14px;
}

.btn-outline-secondary {
  border: 1px solid #ccc;
  color: #333;
  padding: 0.5rem 1rem;
  /* Thêm padding để thay đổi chiều cao */
  height: 45px;
  /* Điều chỉnh chiều cao cho các nút */
}

.btn-outline-secondary:hover {
  background-color: #f0f0f0;
}

.btn-sm {
  font-size: 0.8rem;
  padding: 0.2rem 0.6rem;
}

button {
  transition: background-color 0.3s ease;
}

/* Adjust Avatar size */
img.rounded-circle {
  width: 35px;
  height: 35px;
  object-fit: cover;
  margin-right: 10px;
  /* Điều chỉnh khoảng cách giữa avatar và tên */
}

/* Dropdown menu */
.account-dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background-color: white;
  border-radius: 8px;
  min-width: 150px;
  z-index: 1009;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.dropdown-entry {
  display: flex;
  align-items: center;
  padding: 8px 14px;
  color: #333;
  font-size: 0.85rem;
  text-decoration: none;
  transition: background-color 0.2s ease;
}

.dropdown-entry:hover {
  background-color: #f5f5f5;
}

.icon-thin {
  font-size: 1.1rem;
}

.cart-icon-wrapper:hover {
  opacity: 0.85;
}

/* Right align the profile or login button */
.ml-auto {
  margin-left: auto;
}

/* Tăng chiều cao cho thanh header */
header {
  height: 70px;
  /* Tăng chiều cao cho thanh header */
}
</style>
