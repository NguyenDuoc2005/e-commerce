<script setup lang="ts">
import themeConfig from './theme/themeConfig'
</script>

<template>
  <a-config-provider :theme="themeConfig">
    <router-view />
  </a-config-provider>
</template>
