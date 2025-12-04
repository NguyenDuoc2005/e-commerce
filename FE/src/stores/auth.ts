// src/stores/auth.js

import { ACCESS_TOKEN_STORAGE_KEY, REFRESH_TOKEN_STORAGE_KEY, USER_INFO_STORAGE_KEY } from '@/constants/storageKey'
import type { UserInformation } from '@/types/auth.type'
import { localStorageAction } from '@/utils/storage'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

type authenticationData = {
  user: UserInformation
  accessToken: string
  refreshToken: string
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserInformation | null>(localStorageAction.get(USER_INFO_STORAGE_KEY) || null)
  const accessToken = ref<string | null>(localStorageAction.get(ACCESS_TOKEN_STORAGE_KEY) || null)
  const refreshToken = ref<string | null>(localStorageAction.get(REFRESH_TOKEN_STORAGE_KEY) || null)

  const isAuthenticated = computed(() => accessToken.value !== null)
  
  const login = (tokenData: authenticationData) => {
    const userData = tokenData.user
    user.value = userData
    accessToken.value = tokenData.accessToken
    refreshToken.value = tokenData.refreshToken
    console.log('token: ' + accessToken.value)
    localStorageAction.set(USER_INFO_STORAGE_KEY, userData)
    localStorageAction.set(ACCESS_TOKEN_STORAGE_KEY, tokenData.accessToken)
    localStorageAction.set(REFRESH_TOKEN_STORAGE_KEY, tokenData.refreshToken)

    console.log('🤡Ra User Login :', userData)
  }

  const logout = () => {
    user.value = null
    accessToken.value = null
    refreshToken.value = null
    localStorageAction.remove(USER_INFO_STORAGE_KEY)
    localStorageAction.remove(ACCESS_TOKEN_STORAGE_KEY)
    localStorageAction.remove(REFRESH_TOKEN_STORAGE_KEY)
  }

  return { user, isAuthenticated, login, logout, accessToken, refreshToken }
})
