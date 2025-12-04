<template>
  <a-spin size="large" />
</template>

<script lang="ts" setup>
import { useAuthStore } from '@/stores/auth'
import { getExpireTime } from '@/utils/token.helper'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'

const router = useRouter()
 
const { accessToken } = useAuthStore()

onMounted(() => {
  if (!accessToken) {
    router.push({ name: "login"})
    return
  }

  const expiredTimeAccessToken = getExpireTime(accessToken)

  if (dayjs().isAfter(expiredTimeAccessToken * 1000)) {
    router.push({ name: "login" })
    return
  }
})
</script>
