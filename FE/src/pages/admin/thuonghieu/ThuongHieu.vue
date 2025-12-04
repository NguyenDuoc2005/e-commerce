<template>
  <div class="page-container">
    <div class="breadcrumb-section">
      <BreadcrumbDefault
        :pageTitle="'Quản lý thương hiệu'"
        :routes="[
          { path: '/admin/thuong-hieu', name: 'Quản lý thương hiệu' }
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
      <UnorderedListOutlined /> Danh sách thương hiệu
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
import ProductFilter from './ThuongHieuFilter.vue';
import ProductTable from './ThuongHieuTable.vue';
import ProductModal from './ThuongHieuModal.vue';
import { computed, onMounted, reactive, watch } from 'vue';
import { GetThuongHieus, type ThuongHieuResponse, type ParamsGetThuongHieu } from '@/services/api/admin/thuonghieu.api';
import { debounce } from 'lodash';
import DivCustom from '@/components/custom/Div/DivCustomAll.vue'
import { FilterOutlined, UnorderedListOutlined } from '@ant-design/icons-vue';


const state = reactive({
  searchQuery: '',
  searchStatus: null as number | null,
  isModalOpen: false,
  isModalChangeStatus: false,
  selectedProductId: null as string | null,
  products: [] as ThuongHieuResponse[],
  paginationParams: { page: 1, size: 10 },
  totalItems: 0
})


const modalTitle = computed(() => {
  return state.selectedProductId ? 'Cập nhật thương hiệu' : 'Thêm thương hiệu'
})

const updateSearchQuery = (newQuery: string) => {
  state.searchQuery = newQuery
}

const updateSearchStatus = (newStatus: number | null) => {
  state.searchStatus = newStatus
}

const openAddModal = () => {
  state.selectedProductId = null
  state.isModalOpen = true
}

const openViewModal = (id: string) => {
  state.selectedProductId = id
  state.isModalOpen = true
}

const openChangeStatusModal = (id: string) => {
  state.selectedProductId = id
  state.isModalChangeStatus = true
}

const closeModal = () => {
  state.isModalOpen = false
}

const closeModalChangeStatus = () => {
  fetchProducts();
  state.isModalChangeStatus = false
}

const fetchProducts = async () => {
  try {
    const params: ParamsGetThuongHieu = {
      page: state.paginationParams.page,
      size: state.paginationParams.size,
      q: state.searchQuery,
      status: state.searchStatus
    }
    const response = await GetThuongHieus(params)
    // const pagedData = response.data.data 

    state.products = response.data?.data
    state.totalItems = response.data?.totalElements
  } catch (error) {
    console.error('Failed to fetch products:', error)
  }
}

console.log(state.products)

const debouncedFetchProducts = debounce(fetchProducts, 300)

onMounted(() => {
  fetchProducts()
})

watch(
  () => [state.searchQuery, state.searchStatus],
  () => {
    state.paginationParams.page = 1
    debouncedFetchProducts()
  }
)

const handlePageChange = ({ page, pageSize }: { page: number; pageSize?: number }) => {
  state.paginationParams.page = page
  if (pageSize) {
    state.paginationParams.size = pageSize
  }
  fetchProducts()
}


const handleChangeStatus = async () => {
  fetchProducts();
}
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