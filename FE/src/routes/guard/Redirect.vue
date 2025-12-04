<script lang="ts" setup>
import { ROUTES_CONSTANTS } from '@/constants/path'
import { ROLES } from '@/constants/roles'
import { useAuthStore } from '@/stores/auth'
import { getUserInformation } from '@/utils/token.helper'
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const { state } = route.query

onMounted(() => {
  if (state) {
    const decodedState = atob(state as string)
    const { accessToken, refreshToken } = JSON.parse(decodedState)
    const user = getUserInformation(accessToken)

    authStore.login({
      user,
      accessToken,
      refreshToken
    })

    // Điều hướng theo role duy nhất
    if (user.role === ROLES.ADMIN) {
      router.push({ name: ROUTES_CONSTANTS.ADMIN.children.BAN_HANG.name })
    } else {
      router.push({ name: ROUTES_CONSTANTS.USERS.children.TRANGCHU.name })
    }
  } else {
    router.push({ name: ROUTES_CONSTANTS.USERS.name })
  }
})
</script>
