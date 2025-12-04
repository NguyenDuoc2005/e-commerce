<template>
  <div class="page-container"> 
    <div class="breadcrumb-section">
      <BreadcrumbDefault :pageTitle="'Quản lý đợt giảm giá'" :routes="[
        { path: '/admin/dot-giam-gia', name: 'Quản lý đợt giảm giá' }
      ]" />
    </div>
        <p class="section-title">
      <FilterOutlined /> Bộ lọc tìm kiếm
    </p>
    <ProductFilter 
      :ma="state.filters.ma"
      :ten="state.filters.ten"
      :phanTramGiam="state.filters.phanTramGiam"
      :ngayBatDau="state.filters.ngayBatDau"
      :ngayKetThuc="state.filters.ngayKetThuc"
      :trangThai="state.filters.trangThai"
      @update:ma="updateFilter('ma', $event)"
      @update:ten="updateFilter('ten', $event)"
      @update:phanTramGiam="updateFilter('phanTramGiam', $event)"
      @update:ngayBatDau="updateFilter('ngayBatDau', $event)"
      @update:ngayKetThuc="updateFilter('ngayKetThuc', $event)"
      @update:trangThai="updateFilter('trangThai', $event)"
    />
   <p class="section-title">
      <UnorderedListOutlined /> Danh sách đợt giảm giá
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
    
    <!-- <ProductModal
      :open="state.isModalOpen" 
      :openChangeStatus="state.isModalChangeStatus"
      :productId="state.selectedProductId" 
      :title="modalTitle" 
      @closeChangeStatus="closeModalChangeStatus"
      @close="closeModal"
      @success="fetchProducts"
    /> -->
  </div>
</template>

<script setup lang="ts">
import BreadcrumbDefault from '@/components/ui/Breadcrumbs/BreadcrumbDefault.vue';
import ProductFilter from './DotGiamGiaFilter.vue';
import ProductTable from './DotGiamGiaTable.vue';
import ProductModal from './DotGiamGiaModal.vue';
import { computed, onMounted, reactive, watch } from 'vue';
import { GetDotGiamGia, type DotGiamGiaResponse, type ParamsGetDotGiamGia } from '@/services/api/admin/dotgiamgia.api';
import { debounce } from 'lodash';
import DivCustom from '@/components/custom/Div/DivCustomAll.vue'
import { FilterOutlined, UnorderedListOutlined } from '@ant-design/icons-vue';

const state = reactive({
  filters: {
    ma: '',
    ten: '',
    phanTramGiam: '',
    ngayBatDau: null as number | null,
    ngayKetThuc: null as number | null,
    trangThai: null as number | null
  },
  isModalOpen: false,
  isModalChangeStatus: false,
  selectedProductId: null as string | null,
  products: [] as DotGiamGiaResponse[],
  paginationParams: { page: 1, size: 10 },
  totalItems: 0
})

const modalTitle = computed(() => {
  return state.selectedProductId ? 'Cập nhật đợt giảm giá' : null
})

const updateFilter = (key: keyof typeof state.filters, value: any) => {
  state.filters[key] = value
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
    const params: ParamsGetDotGiamGia = {
      page: state.paginationParams.page,
      size: state.paginationParams.size,
      ma: state.filters.ma || undefined,
      ten: state.filters.ten || undefined,
      phanTramGiam: state.filters.phanTramGiam || undefined,
      ngayBatDau: state.filters.ngayBatDau || undefined,
      ngayKetThuc: state.filters.ngayKetThuc || undefined,
      trangThai: state.filters.trangThai
    }
    
    // Remove empty string values
    Object.keys(params).forEach(key => {
      if (params[key as keyof ParamsGetDotGiamGia] === '') {
        delete params[key as keyof ParamsGetDotGiamGia]
      }
    })
    
    const response = await GetDotGiamGia(params)
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
  () => state.filters,
  () => {
    state.paginationParams.page = 1
    debouncedFetchProducts()
  },
  { deep: true }
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