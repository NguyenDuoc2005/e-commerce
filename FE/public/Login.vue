<template>
    <div
      class="bg-gray-200 bg-cover h-screen"
      :style="{ backgroundImage: `url(${backgroundImage})` }"
    >
      <div class="flex justify-center items-center" :style="{ height: 'calc(100vh - 64px)' }">
        <div class="card border-0">
          <div>
            <div class="my-4 flex justify-center items-center">
              <img :src="logo" alt="Logo" class="mb-2 w-48" />
              <img :src="logoUDPM" alt="Logo" class="mb-2 w-48" />
            </div>
            <div class="flex justify-center items-center flex-col">
              <button
                @click="handleRedirectLogin"
                :style="buttonStyleLogin"
                type="button"
                class="mt-5 flex justify-center items-center p-3"
                :disabled="isLoginProcessing"
              >
                <v-icon name="fc-google" class="mr-2" />
                <span>Đăng nhập với Google</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { URL_OAUTH2_GOOGLE } from '@/constants/url'
  import { computed, onMounted, ref } from 'vue'
  import backgroundImage from '/images/bg-simple.jpg'
  import logoUDPM from '/images/logo-udpm-dark.png'
  import logo from '/images/Logo_FPT.png'
  import { cookieStorageAction } from '@/utils/storage'
  import {
    ACCOUNT_EXPIRED,
    ACCOUNT_EXPIRED_MESSAGE,
    ACCOUNT_NOT_EXIST,
    ACCOUNT_NOT_EXIST_MESSAGE,
    ACCOUNT_NOT_HAVE_PERMISSION,
    ACCOUNT_NOT_HAVE_PERMISSION_MESSAGE
  } from '@/constants/cookie.constant'
  import { toast } from 'vue3-toastify'
  import { useAuthStore } from '@/stores/auth'
  import { useRouter } from 'vue-router'
  import { getUserInformation } from '@/utils/token.helper'
  
  const isLoginProcessing = ref(false)
  
  //authen
  const authStore = useAuthStore()
  
  //router
  const router = useRouter()
  
  onMounted(() => {
    if (authStore.accessToken) {
      const user = getUserInformation(authStore.accessToken)
    
        router.push('/')
        return
      
    }
    const accountNotExistError = cookieStorageAction.get(ACCOUNT_NOT_EXIST)
    const accountNotHavePermission = cookieStorageAction.get(ACCOUNT_NOT_HAVE_PERMISSION)
    const accountExpried = cookieStorageAction.get(ACCOUNT_EXPIRED)
  
    if (accountNotExistError) {
      toast.error(ACCOUNT_NOT_EXIST_MESSAGE)
      cookieStorageAction.remove(ACCOUNT_NOT_EXIST)
    }
    if (accountNotHavePermission) {
      toast.error(ACCOUNT_NOT_HAVE_PERMISSION_MESSAGE)
      cookieStorageAction.remove(ACCOUNT_NOT_HAVE_PERMISSION)
    }
    if (accountExpried) {
      toast.error(ACCOUNT_EXPIRED_MESSAGE)
      cookieStorageAction.remove(ACCOUNT_EXPIRED)
    }
  })
  
  const handleRedirectLogin = () => {
    window.location.href = URL_OAUTH2_GOOGLE
  }
  
  const buttonStyleLogin = computed(() => ({
    backgroundColor: '#253741',
    color: '#fff',
    borderRadius: '5px',
    fontSize: '16px',
    boxShadow: '0 0 4px #253741'
  }))
  </script>
  
  <style scoped></style>
  