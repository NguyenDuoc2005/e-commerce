<script setup lang="ts">
import { useSidebarStore } from "@/stores/sidebar";
import { ref } from "vue";
import SidebarItem from "./SidebarItem.vue";
import { ROUTES_CONSTANTS } from "@/constants/path";

const sidebarStore = useSidebarStore();

const menuGroups = ref([
  {
    menuItems: [
      {
        label: "Thống kê",
        icon: `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
        </svg>`,
        routeName: ROUTES_CONSTANTS.ADMIN.children.THONG_KE.name,
      },
      {
        label: "Bán hàng",
        icon: `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
        </svg>`,
        routeName: ROUTES_CONSTANTS.ADMIN.children.BAN_HANG.name,
      },
      {
        label: "Quản lý hóa đơn",
        icon: `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>`,
        routeName: ROUTES_CONSTANTS.ADMIN.children.HOA_DON.name,
      },
      {
        label: "Quản lý sản phẩm",
        icon: `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
        </svg>`,
        children: [
          {
            label: "Sản phẩm",
            routeName: ROUTES_CONSTANTS.ADMIN.children.SAN_PHAM.name,
          },
          {
            label: "Màu sắc",
            routeName: ROUTES_CONSTANTS.ADMIN.children.MAUSAC.name,
          },
          {
            label: "Chất liệu",
            routeName: ROUTES_CONSTANTS.ADMIN.children.CHAT_LIEU.name,
          },
          {
            label: "Loại đế",
            routeName: ROUTES_CONSTANTS.ADMIN.children.LOAI_DE.name,
          },
          {
            label: "Danh mục",
            routeName: ROUTES_CONSTANTS.ADMIN.children.LOAI_GIAY.name,
          },
          {
            label: "Kích cỡ",
            routeName: ROUTES_CONSTANTS.ADMIN.children.SIZE.name,
          },
          {
            label: "Thương hiệu",
            routeName: ROUTES_CONSTANTS.ADMIN.children.THUONG_HIEU.name,
          },
        ],
      },
      {
        label: "Quản lý khách hàng",
        icon: `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
        </svg>`,
        routeName: ROUTES_CONSTANTS.ADMIN.children.KHACH_HANG.name,
      },
      {
        label: "Quản lý nhân viên",
        icon: `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>`,
        routeName: ROUTES_CONSTANTS.ADMIN.children.NHAN_VIEN.name,
      },
      // {
      //   label: "Quản lý đợt giảm giá",
      //   icon: `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5">
      //     <path stroke-linecap="round" stroke-linejoin="round" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
      //   </svg>`,
      //   routeName: ROUTES_CONSTANTS.ADMIN.children.DOT_GIAM_GIA.name,
      // },
      {
        label: "Quản lý phiếu giảm giá",
        icon: `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
        </svg>`,
        routeName: ROUTES_CONSTANTS.ADMIN.children.VOUCHER.name,
      },
    ],
  },
]);

// Toggle sidebar function
const toggleSidebar = () => {
  sidebarStore.toggleSidebar();
};

// Check if sidebar is collapsed (thu gọn)
const isCollapsed = ref(false);

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
};
</script>

<template>
  <aside class="sidebar" :class="{
    'translate-x-0': !sidebarStore.isSidebarOpen,
    '-translate-x-full': sidebarStore.isSidebarOpen,
    'collapsed': isCollapsed
  }">
    <!-- Header with Logo and Toggle Button -->
    <div class="header">
      <!-- Toggle Button ở trên -->
      <div class="toggle-container">
        <!-- <button @click="toggleCollapse" class="toggle-button" type="button"
          :title="isCollapsed ? 'Mở rộng sidebar' : 'Thu gọn sidebar'" aria-label="Toggle sidebar collapse">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"
            class="toggle-icon" :class="{ 'rotate-180': isCollapsed }">
            <path stroke-linecap="round" stroke-linejoin="round" d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
          </svg>
        </button> -->
        <div class="logo-container">
          <router-link to="/" class="logo-link">
            <img src="/images/logo.jpg" alt="Tên thương hiệu logo, biểu tượng màu sắc chủ đạo trắng đen tinh tế"
              class="logo" :class="{ 'collapsed': isCollapsed }" style="margin-bottom: 10px;" />
          </router-link>
        </div>
      </div>

      <!-- Logo Container -->

    </div>

    <!-- Navigation Menu -->
    <nav class="menu" aria-label="Menu điều hướng chính">
      <template v-for="(menuGroup, groupIndex) in menuGroups" :key="groupIndex">
        <div>
          <ul class="list-unstyled" role="menu">
            <SidebarItem v-for="(menuItem, index) in menuGroup.menuItems" :item="menuItem" :key="index" :index="index"
              :isCollapsed="isCollapsed" class="nav-link" role="menuitem" />
          </ul>
        </div>
      </template>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  background-color: #ffffff;
  /* width: 300px; */
  height: 100vh;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease-in-out, transform 0.3s ease-in-out;
  color: #374151;
  border-right: 1px solid #e5e7eb;
  user-select: none;
  font-family: Roboto, sans-serif;
  position: relative;
  z-index: 100;
}

/* Trạng thái thu gọn */
.sidebar.collapsed {
  width: 70px;
}

.header {
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1rem 0.5rem 0.5rem 0.5rem;
  border-bottom: 1px solid #e5e7eb;
  min-height: 80px;
  position: relative;
}

.toggle-container {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  padding-bottom: 0.5rem;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  margin-bottom: 10px;
}

.logo-link {
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo {
  width: 160px;
  max-width: 80%;
  height: auto;
  object-fit: contain;
  transition: opacity 0.3s ease;
}

/* Toggle Button Styles */
.toggle-button {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  width: 28px;
  height: 28px;
}

.toggle-button:hover {
  background-color: #59bddb;
  color: white;
  border-color: #59bddb;
}

.toggle-icon {
  width: 16px;
  height: 16px;
  transition: transform 0.3s ease;
}

.toggle-icon.rotate-180 {
  transform: rotate(180deg);
}

.menu {
  flex-grow: 1;
  overflow-y: auto;
  padding: 0.5rem 0;
  scrollbar-width: thin;
  scrollbar-color: #d1d5db transparent;
}

.menu::-webkit-scrollbar {
  width: 4px;
}

.menu::-webkit-scrollbar-track {
  background: transparent;
}

.menu::-webkit-scrollbar-thumb {
  background-color: #d1d5db;
  border-radius: 2px;
}

.menu::-webkit-scrollbar-thumb:hover {
  background-color: #9ca3af;
}

.list-unstyled {
  margin: 0;
  padding: 0 0.1rem;
  list-style: none;
}

/* Footer Styles */
.sidebar-footer {
  padding: 1rem;
  border-top: 1px solid #e5e7eb;
  background-color: #f8fafc;
}

.version-info {
  text-align: center;
}

.version-text {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
}

/* Sidebar States */
.translate-x-0 {
  transform: translateX(0);
}

.-translate-x-full {
  transform: translateX(-100%);
}

/* Overlay for mobile */
.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 50;
  transition: opacity 0.3s ease;
}

/* Responsive Design */
@media (max-width: 768px) {
  .sidebar {
    width: 260px;
  }

  .sidebar.collapsed {
    width: 60px;
  }

  .menu {
    padding: 0.25rem 0;
  }

  .list-unstyled {
    padding: 0 0.75rem;
  }

  .sidebar.collapsed .list-unstyled {
    padding: 0 0.25rem;
  }
}

/* Focus styles for accessibility */
.toggle-button:focus {
  outline: 2px solid #59bddb;
  outline-offset: 2px;
}

.logo-link:focus {
  outline: 2px solid #59bddb;
  outline-offset: 4px;
  border-radius: 4px;
}
</style>