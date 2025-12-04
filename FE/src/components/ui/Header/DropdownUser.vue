<script setup lang="ts">
import { ROUTES_CONSTANTS } from '@/constants/path'
import { ROLES } from '@/constants/roles'
import { UserSwitchOutlined } from '@ant-design/icons-vue'
import { onClickOutside } from '@vueuse/core'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'


const props = defineProps({
  isNoSidebarPage: Boolean
})

const target = ref(null)
const dropdownOpen = ref(false)

onClickOutside(target, () => {
  dropdownOpen.value = false
})

const router = useRouter()

// const userInfor = reactive<UserInformation>({
//   email: '',
//   fullName: '',
//   pictureUrl: '',
//   rolesCodes: '',
//   rolesNames: '',
//   typeUser: '',
//   userCode: '',
//   userId: ''
// })

onMounted(() => {

  // userInfor.email = userAuthStore?.user?.email
  // userInfor.pictureUrl = userAuthStore?.user?.pictureUrl
  // userInfor.fullName = userAuthStore?.user?.fullName
  // userInfor.rolesCodes = userAuthStore?.user?.rolesCodes as string
})

const handleLogout = () => {
  // if (userAuthStore?.user?.typeUser === ROLES.ADMIN) {
  //   router.push({ name: ROUTES_CONSTANTS.AUTHENTICATION.children.LOGIN.name })
  // } else if (userAuthStore?.user?.typeUser === ROLES.CUSTOMER) {
  //   router.push({ name: ROUTES_CONSTANTS.AUTHENTICATION.children.LOGIN.name })
  // }
  // userAuthStore.logout()

  localStorage.removeItem("accessToken");
  sessionStorage.clear();
  
  router.push({ path: ROUTES_CONSTANTS.LOGIN.path });
}

// const showRoleUser = computed(() => {
//   if (!userInfor || !userInfor.rolesCodes) {
//     return 'Không có thông tin vai trò'
//   }
//   switch (userInfor.rolesCodes) {
//     case ROLES.ADMIN:
//       return 'ADMIN'
//     case ROLES.CUSTOMER:
//       return 'Người dùng'
//     default:
//       return userInfor.rolesCodes
//   }
// })
</script>

<template>
  <div class="dropdown" ref="target">
    <a class="d-flex align-items-center text-decoration-none dropdown-toggle" href="#" role="button"
      data-bs-toggle="dropdown" aria-expanded="false" @click.prevent="dropdownOpen = !dropdownOpen">
      <div class="me-2 text-end d-none d-lg-block">
        <div class="fw-bold" :class="isNoSidebarPage ? 'text-white' : 'text-dark'">
          <!-- {{ userInfor.fullName }} -->
        </div>
        <div class="small" :class="isNoSidebarPage ? 'text-white' : 'text-muted'">
          <!-- {{ userInfor.email }} -->
        </div>
      </div>
      <!-- <img class="rounded-circle" width="40" height="40" :src="userInfor.pictureUrl" :alt="userInfor.fullName" /> -->
    </a>
    <ul v-show="dropdownOpen" class="dropdown-menu dropdown-menu-end shadow">
      <li class="dropdown-item d-flex align-items-center">
        <!-- <UserSwitchOutlined class="me-2" /> Vai trò: {{ showRoleUser }} -->
      </li>
      <li>
        <button class="dropdown-item d-flex align-items-center" @click="handleLogout">
          <svg class="me-2" width="22" height="22" viewBox="0 0 22 22" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M15.5375 0.618744H11.6531C10.7594 0.618744 10.0031 1.37499 10.0031 2.26874V4.64062C10.0031 5.05312 10.3469 5.39687 10.7594 5.39687C11.1719 5.39687 11.55 5.05312 11.55 4.64062V2.23437C11.55 2.16562 11.5844 2.13124 11.6531 2.13124H15.5375C16.3625 2.13124 17.0156 2.78437 17.0156 3.60937V18.3562C17.0156 19.1812 16.3625 19.8344 15.5375 19.8344H11.6531C11.5844 19.8344 11.55 19.8 11.55 19.7312V17.3594C11.55 16.9469 11.2062 16.6031 10.7594 16.6031C10.3125 16.6031 10.0031 16.9469 10.0031 17.3594V19.7312C10.0031 20.625 10.7594 21.3812 11.6531 21.3812H15.5375C17.2219 21.3812 18.5625 20.0062 18.5625 18.3562V3.64374C18.5625 1.95937 17.1875 0.618744 15.5375 0.618744Z" />
          </svg>
          Đăng xuất
        </button>
      </li>
    </ul>
  </div>
</template>
