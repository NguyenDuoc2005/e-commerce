<template>
  <div class="page-container">
    <div class="breadcrumb-section">
      <BreadcrumbDefault
        :pageTitle="'Quản lý kích cỡ'"
        :routes="[
          { path: '/admin/sizes', name: 'Quản lý kích cỡ' }
        ]"
      />
    </div>

    <p class="section-title">
      <FilterOutlined /> Bộ lọc tìm kiếm
    </p>
    <ProductFilter
      :searchQuery="state.searchQuery"
      :searchStatus="state.searchStatus"
      @update:searchQuery="updateSearchQuery"
      @update:searchStatus="updateSearchStatus"
    />
  
    <p class="section-title">
      
    </p>
    <ProductTable
      :products="state.products"
      :paginationParams="state.paginationParams"
      :totalItems="state.totalItems"
      @add="openAddModal"
      @view="openViewModal"
      @page-change="handlePageChange"
      @change-status="handleChangeStatus"
    />

    <ProductModal
      :open="state.isModalOpen"
      :openChangeStatus="state.isModalChangeStatus"
      :productId="state.selectedProductId"
      :title="modalTitle"
      @closeChangeStatus="closeModalChangeStatus"
      @close="closeModal"
      @success="fetchProducts"
    />
  </div>
</template>

<script setup lang="ts">
import BreadcrumbDefault from '@/components/ui/Breadcrumbs/BreadcrumbDefault.vue';
import ProductFilter from './SizeFilter.vue'; // Renamed to SizeFilter
import ProductTable from './SizeTable.vue'; // Renamed to SizeTable
import ProductModal from './SizeModal.vue'; // Renamed to SizeModal
import { computed, onMounted, reactive, watch } from 'vue';
import { GetSizes, type SizeResponse, type ParamsGetSize } from '@/services/api/admin/size.api';
import { debounce } from 'lodash';
// import DivCustom from '@/components/custom/Div/DivCustomAll.vue'; // Not used directly in this template, removed
import { FilterOutlined, UnorderedListOutlined } from '@ant-design/icons-vue';

const state = reactive({
  searchQuery: '',
  searchStatus: null as number | null,
  isModalOpen: false,
  isModalChangeStatus: false,
  selectedProductId: null as string | null, // Renamed to selectedSizeId for clarity, if needed
  products: [] as SizeResponse[], // Renamed to sizes for clarity, if needed
  paginationParams: { page: 1, size: 10 },
  totalItems: 0
});

const modalTitle = computed(() => {
  return state.selectedProductId ? 'Cập nhật kích cỡ' : 'Thêm kích cỡ';
});

const updateSearchQuery = (newQuery: string) => {
  state.searchQuery = newQuery;
};

const updateSearchStatus = (newStatus: number | null) => {
  state.searchStatus = newStatus;
};

const openAddModal = () => {
  state.selectedProductId = null;
  state.isModalOpen = true;
};

const openViewModal = (id: string) => {
  state.selectedProductId = id;
  state.isModalOpen = true;
};

// This function might need adjustment based on how change status is handled
const openChangeStatusModal = (id: string) => {
  state.selectedProductId = id;
  state.isModalChangeStatus = true;
};

const closeModal = () => {
  state.isModalOpen = false;
};

const closeModalChangeStatus = () => {
  fetchProducts(); // Re-fetch products after status change
  state.isModalChangeStatus = false;
};

const fetchProducts = async () => {
  try {
    const params: ParamsGetSize = {
      page: state.paginationParams.page,
      size: state.paginationParams.size,
      q: state.searchQuery,
      status: state.searchStatus
    };
    const response = await GetSizes(params);
    state.products = response.data?.data;
    state.totalItems = response.data?.totalElements;
  } catch (error) {
    console.error('Failed to fetch products:', error);
  }
};

const debouncedFetchProducts = debounce(fetchProducts, 300);

onMounted(() => {
  fetchProducts();
});

watch(
  () => [state.searchQuery, state.searchStatus],
  () => {
    state.paginationParams.page = 1;
    debouncedFetchProducts();
  }
);

const handlePageChange = ({ page, pageSize }: { page: number; pageSize?: number }) => {
  state.paginationParams.page = page;
  if (pageSize) {
    state.paginationParams.size = pageSize;
  }
  fetchProducts();
};

const handleChangeStatus = async () => {
  fetchProducts(); // Re-fetch products after status change from table
};
</script>

<style scoped>
.page-container {
  padding: 20px; /* Overall padding for the page content */
}

.breadcrumb-section {
  margin-bottom: 25px; /* Space below the breadcrumb and above the first section */
  background-color: #fff; /* White background for the breadcrumb box */
  padding: 15px 20px; /* Padding inside the breadcrumb box */
  border-radius: 8px; /* Rounded corners */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09); /* Subtle shadow */
}

.section-title {
  margin-top: 30px; /* Space above each main section title */
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px; /* Space below the title */
  margin-left: 0px; /* Remove left margin if section-title is directly under padding */
  color: #333; /* Darker color for titles */
  display: flex; /* To align icon and text */
  align-items: center; /* Vertically center icon and text */
  gap: 8px; /* Space between icon and text */
}
 
/* Remove or adjust body styles if they are global.
   Scoped styles prevent them from affecting the entire app. */
body {
  font-family: 'Roboto', sans-serif;
}
</style> 