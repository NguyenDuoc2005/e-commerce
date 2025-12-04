<template>
  <li class="sidebar-item">
    <router-link
      v-if="!item.children"
      :to="{ name: item.routeName }"
      class="nav-link"
      :class="{ active: isActive }"
      :title="isCollapsed ? item.label : ''"
      tabindex="0"
    >
      <span class="icon" v-html="item.icon"></span>
      <span v-if="!isCollapsed" class="label">{{ item.label }}</span>
    </router-link>

    <div v-else class="dropdown-container">
      <button
        @click="toggleDropdown"
        type="button"
        tabindex="0"
        @keydown.enter.prevent="toggleDropdown"
        @keydown.space.prevent="toggleDropdown"
        :aria-expanded="isOpen"
        :title="isCollapsed ? item.label : ''"
        class="nav-link nav-dropdown"
        :class="{ active: isActive, open: isOpen }"
      >
        <span class="icon" v-html="item.icon"></span>
        <span v-if="!isCollapsed" class="label">{{ item.label }}</span>
        <span v-if="!isCollapsed" class="dropdown-arrow" :class="{ rotate: isOpen }" aria-hidden="true">
          <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
            <polyline points="6 9 12 15 18 9"></polyline>
          </svg>
        </span>
      </button>
      
      <SidebarDropdown 
        v-if="!isCollapsed"
        :key="`dropdown-${item.label}`"
        :items="item.children" 
        :isOpen="isOpen" 
        @item-click="handleChildClick" 
      />
      
      <!-- Tooltip cho collapsed mode -->
      <div v-if="isCollapsed && isOpen" class="collapsed-tooltip">
        <div class="tooltip-content">
          <h4 class="tooltip-title">{{ item.label }}</h4>
          <ul class="tooltip-list">
            <li v-for="child in item.children" :key="child.routeName">
              <router-link
                :to="{ name: child.routeName }"
                class="tooltip-link"
                :class="{ active: $route.name === child.routeName }"
                @click="isOpen = false"
              >
                {{ child.label }}
              </router-link>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </li>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import SidebarDropdown from './SidebarDropdown.vue';

const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
  isCollapsed: {
    type: Boolean,
    default: false,
  },
});

const isOpen = ref(false);
const route = useRoute();

const isActive = computed(() => {
  if (props.item.routeName) {
    return route.name === props.item.routeName;
  }
  if (props.item.children && props.item.children.length) {
    return props.item.children.some(child => child.routeName === route.name);
  }
  return false;
});

// Auto open dropdown if has active child item (chỉ khi không collapsed)
watch(isActive, (active) => {
  if (active && props.item.children && props.item.children.length && !props.isCollapsed) {
    isOpen.value = true;
  }
});

// Đóng dropdown khi chuyển sang collapsed mode
watch(() => props.isCollapsed, (collapsed) => {
  if (collapsed) {
    isOpen.value = false;
  }
});

function toggleDropdown() {
  if (props.isCollapsed && props.item.children) {
    // Trong chế độ collapsed, click sẽ hiển thị tooltip
    isOpen.value = !isOpen.value;
  } else if (!props.isCollapsed) {
    // Chế độ bình thường
    isOpen.value = !isOpen.value;
  }
}

function handleChildClick(index) {
  console.log('Child clicked:', index);
}
</script>

<style scoped>
.sidebar-item {
  margin-bottom: 2px;
  position: relative;
}

.nav-link {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  color: #6b7280;
  text-decoration: none;
  font-weight: 500;
  font-size: 15px;
  border-radius: 8px;
  transition: all 0.2s ease-in-out;
  cursor: pointer;
  user-select: none;
  position: relative;
  background-color: transparent;
  border: none;
  width: 100%;
  box-sizing: border-box;
  text-align: left;
  justify-content: flex-start;
}

/* Collapsed mode adjustments */
:deep(.sidebar.collapsed) .nav-link {
  padding: 0.75rem 0.5rem;
  justify-content: center;
}

.sidebar-item > .nav-link:hover {
  background-color: #59bddb;
  color: white;
}

.nav-link.active {
  background-color: #59bddb;
  color: white;
  font-weight: 500;
}

.nav-link.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background-color: #59bddb;
  border-radius: 0 2px 2px 0;
}

.icon {
  width: 20px;
  height: 20px;
  margin-right: 0.75rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: inherit;
  transition: margin-right 0.3s ease;
}

/* Collapsed mode - không có margin right cho icon */
:deep(.sidebar.collapsed) .icon {
  margin-right: 0;
}

.icon :deep(svg) {
  width: 100%;
  height: 100%;
}

.label {
  flex-grow: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
  opacity: 1;
  transition: opacity 0.3s ease;
}

.nav-dropdown {
  justify-content: space-between;
  background: none;
  border: none;
  outline: none;
}

.nav-dropdown:focus {
  outline: 2px solid #59bddb;
  outline-offset: 2px;
}

.nav-dropdown.open {
  background-color: #f3f4f6;
  color: #374151;
}

.dropdown-arrow {
  transition: transform 0.3s ease-in-out;
  display: flex;
  align-items: center;
  color: inherit;
  margin-left: 0.5rem;
}

.dropdown-arrow.rotate {
  transform: rotate(180deg);
}

.dropdown-container {
  width: 100%;
  position: relative;
}

.sidebar-item {
  width: 100%;
}

.nav-link,
.nav-dropdown {
  width: 100% !important;
  max-width: 100%;
  min-width: 0;
}

/* Collapsed Tooltip Styles */
.collapsed-tooltip {
  position: absolute;
  left: 100%;
  top: 0;
  z-index: 1000;
  margin-left: 8px;
  opacity: 1;
  visibility: visible;
  transition: opacity 0.2s ease, visibility 0.2s ease;
}

.tooltip-content {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  padding: 12px 0;
  min-width: 180px;
  max-width: 220px;
}

.tooltip-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  padding: 0 12px;
  margin-bottom: 8px;
  border-bottom: 1px solid #f3f4f6;
  padding-bottom: 8px;
}

.tooltip-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.tooltip-list li {
  margin: 0;
}

.tooltip-link {
  display: block;
  padding: 8px 12px;
  color: #6b7280;
  text-decoration: none;
  font-size: 13px;
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
}

.tooltip-link:hover {
  background-color: #f8fafc;
  color: #59bddb;
  border-left-color: #59bddb;
}

.tooltip-link.active {
  background-color: #59bddb;
  color: white;
  border-left-color: #59bddb;
  font-weight: 500;
}

/* Arrow cho tooltip */
.tooltip-content::before {
  content: '';
  position: absolute;
  left: -6px;
  top: 20px;
  width: 0;
  height: 0;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-right: 6px solid white;
}

.tooltip-content::after {
  content: '';
  position: absolute;
  left: -7px;
  top: 20px;
  width: 0;
  height: 0;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-right: 6px solid #e5e7eb;
}
</style>