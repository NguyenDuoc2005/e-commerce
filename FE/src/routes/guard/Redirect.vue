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

    const roles = user.roles?.length ? user.roles : [user.role]
    if (roles.includes(ROLES.ADMIN)) {
      router.push({ name: ROUTES_CONSTANTS.ADMIN.children.THONG_KE.name })
    } else if (roles.includes(ROLES.SELLER)) {
      router.push({ name: ROUTES_CONSTANTS.SELLER.children.DASHBOARD.name })
    } else {
      router.push({ name: ROUTES_CONSTANTS.USERS.children.TRANGCHU.name })
    }
  } else {
    router.push({ name: ROUTES_CONSTANTS.USERS.name })
  }
})
</script>
