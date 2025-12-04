<script setup lang="ts">
import AdminSidebar from '@/components/custom/Sidebar/AdminSidebar.vue'
import HeaderArea from '@/components/ui/Header/HeaderArea.vue';
</script>

<template>
  <!-- ===== Page Wrapper Start ===== -->
  <div class="d-flex vh-100 overflow-hidden">
    <!-- ===== Sidebar Start ===== -->
    <AdminSidebar />
    <!-- ===== Sidebar End ===== -->

    <!-- ===== Content Area Start ===== -->
    <div class="d-flex flex-column flex-grow-1 overflow-auto">
      <!-- ===== Header Start ===== -->
      <HeaderArea />
      <!-- ===== Header End ===== -->

      <!-- ===== Main Content Start ===== -->
      <main class="container-fluid py-4 px-md-6 px-xxl-10 bg-light">
        <router-view></router-view>
      </main>
      <!-- ===== Main Content End ===== -->
    </div>
  </div>
  <!-- ===== Page Wrapper End ===== -->
</template>
