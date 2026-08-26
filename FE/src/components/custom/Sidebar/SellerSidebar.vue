<script setup lang="ts">
import { ref } from 'vue'
import { useSidebarStore } from '@/stores/sidebar'
import { ROUTES_CONSTANTS } from '@/constants/path'
import SidebarItem from './SidebarItem.vue'

const sidebarStore = useSidebarStore()
const isCollapsed = ref(false)
const icon = (path: string) => `
  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5">
    <path stroke-linecap="round" stroke-linejoin="round" d="${path}" />
  </svg>
`

const menuItems = [
  { label: 'Tổng quan shop', icon: icon('M3 12h4v8H3zM10 8h4v12h-4zM17 4h4v16h-4z'), routeName: ROUTES_CONSTANTS.SELLER.children.DASHBOARD.name },
  { label: 'Sản phẩm', icon: icon('M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4'), routeName: ROUTES_CONSTANTS.SELLER.children.PRODUCTS.name },
  { label: 'Đơn hàng', icon: icon('M9 12h6m-6 4h6M5 5h14v14H5z'), routeName: ROUTES_CONSTANTS.SELLER.children.ORDERS.name },
  { label: 'Tin nhắn', icon: icon('M8 10h8m-8 4h5M4 5h16v13H8l-4 3V5z'), routeName: ROUTES_CONSTANTS.SELLER.children.CHAT.name },
  { label: 'Marketing shop', icon: icon('M15 5v14M5 7a2 2 0 012-2h10a2 2 0 012 2v3a2 2 0 010 4v3a2 2 0 01-2 2H7a2 2 0 01-2-2v-3a2 2 0 010-4V7z'), routeName: ROUTES_CONSTANTS.SELLER.children.VOUCHERS.name },
  { label: 'Đánh giá', icon: icon('M12 3l2.7 5.5 6.1.9-4.4 4.3 1 6.1-5.4-2.9-5.4 2.9 1-6.1-4.4-4.3 6.1-.9L12 3z'), routeName: ROUTES_CONSTANTS.SELLER.children.REVIEWS.name },
  { label: 'Ví & đối soát', icon: icon('M3 7h18v10H3zM16 12h2'), routeName: ROUTES_CONSTANTS.SELLER.children.PAYOUT.name },
  { label: 'Tranh chấp/Khiếu nại', icon: icon('M12 9v4m0 4h.01M5.1 19h13.8a2 2 0 001.73-3L13.73 4a2 2 0 00-3.46 0L3.37 16a2 2 0 001.73 3z'), routeName: ROUTES_CONSTANTS.SELLER.children.DISPUTES.name },
  { label: 'Hồ sơ shop', icon: icon('M4 6h16v12H4zM8 10h8m-8 4h5'), routeName: ROUTES_CONSTANTS.SELLER.children.PROFILE.name },
]
</script>

<template>
  <aside class="sidebar" :class="{ 'translate-x-0': !sidebarStore.isSidebarOpen, '-translate-x-full': sidebarStore.isSidebarOpen, collapsed: isCollapsed }">
    <div class="header">
      <div class="toggle-container">
        <button class="toggle-button" type="button" :title="isCollapsed ? 'Mở rộng sidebar' : 'Thu gọn sidebar'" aria-label="Toggle sidebar collapse" @click="isCollapsed = !isCollapsed">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="toggle-icon" :class="{ 'rotate-180': isCollapsed }">
            <path stroke-linecap="round" stroke-linejoin="round" d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
          </svg>
        </button>
        <div class="logo-container">
          <router-link to="/" class="logo-link"><img src="/images/logo.jpg" alt="Logo" class="logo" :class="{ collapsed: isCollapsed }" /></router-link>
        </div>
      </div>
    </div>
    <nav class="menu" aria-label="Menu Kênh Người Bán">
      <ul class="list-unstyled" role="menu">
        <SidebarItem v-for="(menuItem, index) in menuItems" :key="menuItem.routeName" :item="menuItem" :index="index" :is-collapsed="isCollapsed" class="nav-link" role="menuitem" />
      </ul>
    </nav>
  </aside>
</template>

<style scoped src="./sidebar-shell.css"></style>
