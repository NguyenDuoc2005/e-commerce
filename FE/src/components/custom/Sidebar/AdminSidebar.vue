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

const menuGroups = ref([
  {
    menuItems: [
      {
        label: 'Thống kê',
        icon: icon('M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6'),
        routeName: ROUTES_CONSTANTS.ADMIN.children.THONG_KE.name,
      },
      {
        label: 'Người dùng',
        icon: icon('M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z'),
        routeName: ROUTES_CONSTANTS.ADMIN.children.KHACH_HANG.name,
      },
      {
        label: 'Duyệt Seller',
        icon: icon('M3 7h18M6 7V5a2 2 0 012-2h8a2 2 0 012 2v2m-1 4l-5 5-3-3m-4-2v8a2 2 0 002 2h10a2 2 0 002-2v-8'),
        routeName: ROUTES_CONSTANTS.ADMIN.children.SELLER_APPROVAL.name,
      },
      {
        label: 'Danh mục & Thuộc tính',
        icon: icon('M4 7h10M4 12h16M4 17h7M17 5v4m-2-2h4'),
        children: [
          { label: 'Danh mục', routeName: ROUTES_CONSTANTS.ADMIN.children.CATEGORIES.name },
          { label: 'Thuộc tính sản phẩm', routeName: ROUTES_CONSTANTS.ADMIN.children.PRODUCT_ATTRIBUTES.name },
        ],
      },
      {
        label: 'Sản phẩm & Nội dung',
        icon: icon('M9 12h6m-6 4h6M5 4h14a2 2 0 012 2v14H3V6a2 2 0 012-2z'),
        children: [
          { label: 'Kiểm duyệt nội dung', routeName: ROUTES_CONSTANTS.ADMIN.children.REPORTS.name },
        ],
      },
      {
        label: 'Banner trang chủ',
        icon: icon('M4 5h16v14H4zM4 15l4-4 3 3 2-2 7 7M15 9h.01'),
        routeName: ROUTES_CONSTANTS.ADMIN.children.BANNERS.name,
      },
      {
        label: 'Đối soát seller',
        icon: icon('M3 7h18v10H3zM7 12h4m6 0h.01'),
        routeName: ROUTES_CONSTANTS.ADMIN.children.PAYOUT.name,
      },
      {
        label: 'Đơn hàng',
        icon: icon('M3 6h18M5 6l1 14h12l1-14M9 10v6m6-6v6'),
        children: [
          { label: 'Xử lý tranh chấp', routeName: ROUTES_CONSTANTS.ADMIN.children.DISPUTES.name },
        ],
      },
      {
        label: 'Quản trị viên/Phân quyền',
        icon: icon('M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z'),
        routeName: ROUTES_CONSTANTS.ADMIN.children.NHAN_VIEN.name,
      },
      {
        label: 'Voucher sàn',
        icon: icon('M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z'),
        routeName: ROUTES_CONSTANTS.ADMIN.children.VOUCHER.name,
      },
    ],
  },
])

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

</script>

<template>
  <aside
    class="sidebar"
    :class="{
      'translate-x-0': !sidebarStore.isSidebarOpen,
      '-translate-x-full': sidebarStore.isSidebarOpen,
      collapsed: isCollapsed
    }"
  >
    <div class="header">
      <div class="toggle-container">
        <button
          @click="toggleCollapse"
          class="toggle-button"
          type="button"
          :title="isCollapsed ? 'Mở rộng sidebar' : 'Thu gọn sidebar'"
          aria-label="Toggle sidebar collapse"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke-width="2"
            stroke="currentColor"
            class="toggle-icon"
            :class="{ 'rotate-180': isCollapsed }"
          >
            <path stroke-linecap="round" stroke-linejoin="round" d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
          </svg>
        </button>
        <div class="logo-container">
          <router-link to="/" class="logo-link">
            <img
              src="/images/logo.jpg"
              alt="Logo"
              class="logo"
              :class="{ collapsed: isCollapsed }"
            />
          </router-link>
        </div>
      </div>
    </div>

    <nav class="menu" aria-label="Menu điều hướng chính">
      <template v-for="(menuGroup, groupIndex) in menuGroups" :key="groupIndex">
        <ul class="list-unstyled" role="menu">
          <SidebarItem
            v-for="(menuItem, index) in menuGroup.menuItems"
            :key="index"
            :item="menuItem"
            :index="index"
            :isCollapsed="isCollapsed"
            class="nav-link"
            role="menuitem"
          />
        </ul>
      </template>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  background-color: #ffffff;
  width: 292px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  transition: width 0.24s ease, transform 0.24s ease;
  color: var(--admin-text, #172033);
  border-right: 1px solid var(--admin-border, #dbe3ef);
  user-select: none;
  font-family: Inter, Arial, sans-serif;
  position: relative;
  z-index: 100;
}

.sidebar.collapsed {
  width: 78px;
}

.header {
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 14px 12px;
  border-bottom: 1px solid var(--admin-border, #dbe3ef);
  min-height: 80px;
  position: relative;
}

.toggle-container {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  order: 1;
}

.logo-link {
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo {
  width: 148px;
  max-width: 80%;
  height: auto;
  object-fit: contain;
  transition: opacity 0.3s ease;
  margin-bottom: 10px;
}

.logo.collapsed {
  width: 42px;
}

.toggle-button {
  background-color: var(--admin-primary-soft, rgba(84, 189, 219, 0.14));
  border: 1px solid transparent;
  border-radius: 8px;
  padding: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--admin-primary, #54BDDB);
  width: 32px;
  height: 32px;
  flex: 0 0 auto;
  order: 2;
}

.toggle-button:hover {
  background-color: var(--admin-primary, #54BDDB);
  color: white;
  border-color: var(--admin-primary, #54BDDB);
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
  padding: 12px;
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
  padding: 0;
  list-style: none;
}

.translate-x-0 {
  transform: translateX(0);
}

.-translate-x-full {
  transform: translateX(-100%);
}

.toggle-button:focus,
.logo-link:focus {
  outline: 2px solid var(--admin-primary, #54BDDB);
  outline-offset: 2px;
}

.logo-link:focus {
  outline-offset: 4px;
  border-radius: 4px;
}

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
</style>
