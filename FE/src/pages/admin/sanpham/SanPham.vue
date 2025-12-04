<template>
  <div class="page-container">
    <div class="breadcrumb-section">
      <BreadcrumbDefault
        :pageTitle="'Quản lý sản phẩm'"
        :routes="[{ path: '/admin/san-pham', name: 'Quản lý sản phẩm' }]"
      />
    </div>
    <p class="section-title">
      <FilterOutlined /> Bộ lọc tìm kiếm
    </p>
    <ProductFilter
      v-model:searchQuery="state.searchQuery"
      v-model:searchStatus="state.searchStatus"
      v-model:selectedCategory="state.selectedCategory"
      v-model:selectedMaterial="state.selectedMaterial"
      v-model:selectedBrand="state.selectedBrand"
      v-model:selectedSoleType="state.selectedSoleType"
      @update:searchQuery="updateSearchQuery"
      @update:searchStatus="updateSearchStatus"
      @update:selectedCategory="updateSelectedCategory"
      @update:selectedMaterial="updateSelectedMaterial"
      @update:selectedBrand="updateSelectedBrand"
      @update:selectedSoleType="updateSelectedSoleType"
    />
    <p class="section-title">
      <UnorderedListOutlined /> Danh sách sản phẩm
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
import ProductFilter from './SanPhamFilter.vue'; // Corrected component name if needed
import ProductTable from './SanPhamTable.vue';
import ProductModal from './SanPhamModal.vue';
import { computed, onMounted, reactive, watch } from 'vue';
import { GetSanPhams, type SanPhamResponse, type ParamsGetSanPham } from '@/services/api/admin/sanpham.api';
import { debounce } from 'lodash';
// DivCustom is imported but not used in this template directly. It's used within ProductFilter.
// import DivCustom from '@/components/custom/Div/DivCustomAll.vue';
import { FilterOutlined, UnorderedListOutlined } from '@ant-design/icons-vue';

const state = reactive({
  searchQuery: '',
  searchStatus: null as number | null,
  // New filter states
  selectedCategory: null as string | null,
  selectedMaterial: null as string | null,
  selectedBrand: null as string | null,
  selectedSoleType: null as string | null,

  isModalOpen: false,
  isModalChangeStatus: false,
  selectedProductId: null as string | null,
  products: [] as SanPhamResponse[],
  paginationParams: { page: 1, size: 10 },
  totalItems: 0
});

const modalTitle = computed(() => {
  return state.selectedProductId ? 'Cập nhật sản phẩm' : 'Thêm sản phẩm';
});

// Update methods for search query and status (already present)
const updateSearchQuery = (newQuery: string) => {
  state.searchQuery = newQuery;
};

const updateSearchStatus = (newStatus: number | null) => {
  state.searchStatus = newStatus;
};

// New update methods for additional filters
const updateSelectedCategory = (newCategory: string | null) => {
  state.selectedCategory = newCategory;
};

const updateSelectedMaterial = (newMaterial: string | null) => {
  state.selectedMaterial = newMaterial;
};

const updateSelectedBrand = (newBrand: string | null) => {
  state.selectedBrand = newBrand;
};

const updateSelectedSoleType = (newSoleType: string | null) => {
  state.selectedSoleType = newSoleType;
};

const openAddModal = () => {
  state.selectedProductId = null;
  state.isModalOpen = true;
};

const openViewModal = (id: string) => {
  state.selectedProductId = id;
  state.isModalOpen = true;
};

const openChangeStatusModal = (id: string) => {
  state.selectedProductId = id;
  state.isModalChangeStatus = true;
};

const closeModal = () => {
  state.isModalOpen = false;
};

const closeModalChangeStatus = () => {
  fetchProducts();
  state.isModalChangeStatus = false;
};

const fetchProducts = async () => {
  try {
    const params: ParamsGetSanPham = {
      page: state.paginationParams.page,
      size: state.paginationParams.size,
      q: state.searchQuery,
      status: state.searchStatus,
      // Pass new filter parameters to the API
      danhMucId: state.selectedCategory, // Assuming your API expects 'danhMucId'
      chatLieuId: state.selectedMaterial, // Assuming your API expects 'chatLieuId'
      thuongHieuId: state.selectedBrand, // Assuming your API expects 'thuongHieuId'
      loaiDeId: state.selectedSoleType, // Assuming your API expects 'loaiDeId'
    };
    const response = await GetSanPhams(params);

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

// Watch all filter states to trigger a debounced fetch
watch(
  () => [
    state.searchQuery,
    state.searchStatus,
    state.selectedCategory,
    state.selectedMaterial,
    state.selectedBrand,
    state.selectedSoleType
  ],
  () => {
    state.paginationParams.page = 1; // Reset to first page on filter change
    debouncedFetchProducts();
  }
);

const handlePageChange = ({ page, pageSize }: { page: number; pageSize?: number }) => {
  state.paginationParams.page = page;
  if (pageSize) {
    state.paginationParams.size = pageSize;
  }
  fetchProducts(); // No debounce here, as it's a direct pagination change
};

const handleChangeStatus = async () => {
  fetchProducts(); // Re-fetch products after a status change in modal
};
</script>

<style scoped>
.page-container {
  padding: 20px;
  /* Overall padding for the page content */
}

.breadcrumb-section {
  margin-bottom: 25px;
  /* Space below the breadcrumb and above the first section */
  background-color: #fff;
  /* White background for the breadcrumb box */
  padding: 15px 20px;
  /* Padding inside the breadcrumb box */
  border-radius: 8px;
  /* Rounded corners */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  /* Subtle shadow */
}

.section-title {
  margin-top: 30px;
  /* Space above each main section title */
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  /* Space below the title */
  margin-left: 0px;
  /* Remove left margin if section-title is directly under padding */
  color: #333;
  /* Darker color for titles */
  display: flex;
  /* To align icon and text */
  align-items: center;
  /* Vertically center icon and text */
  gap: 8px;
  /* Space between icon and text */
}

/* Remove or adjust body styles if they are global.
    Scoped styles prevent them from affecting the entire app. */
body {
  font-family: 'Roboto', sans-serif;
}
</style>