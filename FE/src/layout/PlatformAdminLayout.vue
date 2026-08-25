<script setup lang="ts">
import AdminSidebar from '@/components/custom/Sidebar/AdminSidebar.vue'
import HeaderArea from '@/components/ui/Header/HeaderArea.vue'
</script>

<template>
  <div class="admin-shell d-flex vh-100 overflow-hidden">
    <AdminSidebar />
    <div class="d-flex flex-column flex-grow-1 overflow-auto">
      <HeaderArea />
      <main class="admin-content container-fluid">
        <router-view />
      </main>
    </div>
  </div>
</template>
