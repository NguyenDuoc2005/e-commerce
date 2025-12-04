<template>
  <div>
    <NavBar />

    <div>
      <router-view />
    </div>

    <BottomFooter />
  </div>
</template>

<script setup lang="ts">
import NavBar from '@/components/custom/layouts/NavBar.vue'
import BottomFooter from '@/components/custom/layouts/BottomFooter.vue'
</script>
