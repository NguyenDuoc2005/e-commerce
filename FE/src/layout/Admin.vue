<script setup lang="ts">
import AdminSidebar from '@/components/custom/Sidebar/AdminSidebar.vue'
import HeaderArea from '@/components/ui/Header/HeaderArea.vue';
</script>

<template>
  <!-- ===== Page Wrapper Start ===== -->
  <div class="admin-shell d-flex vh-100 overflow-hidden">
    <!-- ===== Sidebar Start ===== -->
    <AdminSidebar />
    <!-- ===== Sidebar End ===== -->

    <!-- ===== Content Area Start ===== -->
    <div class="d-flex flex-column flex-grow-1 overflow-auto">
      <!-- ===== Header Start ===== -->
      <HeaderArea />
      <!-- ===== Header End ===== -->

      <!-- ===== Main Content Start ===== -->
      <main class="admin-content container-fluid">
        <router-view></router-view>
      </main>
      <!-- ===== Main Content End ===== -->
    </div>
  </div>
  <!-- ===== Page Wrapper End ===== -->
</template>
