<template>
  <div class="page-container">
    <div class="breadcrumb-section" v-if="state.products.length > 0">
      <BreadcrumbDefault :pageTitle="'Quản lý sản phẩm chi tiết: ' + state.products[0].ten" :routes="[
        { path: '/admin/san-pham', name: 'Quản lý sản phẩm ' },
        { path: '/admin/san-pham-chi-tiet?id=' + idSanPham, name: 'Quản lý sản phẩm chi tiết: ' + state.products[0].ten }
      ]" />
    </div>
    <div class="breadcrumb-section" v-else>
      <BreadcrumbDefault :pageTitle="'Quản lý sản phẩm chi tiết'" :routes="[
        { path: '/admin/san-pham', name: 'Quản lý sản phẩm ' },
        { path: '/admin/san-pham-chi-tiet?id=' + idSanPham, name: 'Quản lý sản phẩm chi tiết' }
      ]" />
    </div>
    <p class="section-title">
      <FilterOutlined /> Bộ lọc tìm kiếm
    </p>

    <ProductFilter :searchQuery="state.searchQuery" :searchStatus="state.searchStatus" :giaMax="state.products.giaMax" :priceRange="state.priceRange"
      :searchColor="state.searchColor" :searchSize="state.searchSize" @update:searchQuery="updateSearchQuery"
      @update:searchStatus="updateSearchStatus" @update:priceRange="updatePriceRange"
      @update:searchColor="updateSearchColor" @update:searchSize="updateSearchSize" />
    <p class="section-title" v-if="state.products.length > 0">
      <UnorderedListOutlined /> Danh sách sản phẩm chi tiết: {{ state.products[0].ten }}
    </p>
     <p class="section-title" v-else>
      <UnorderedListOutlined /> Danh sách sản phẩm chi tiết
    </p>
    <ProductTable :products="state.products" :paginationParams="state.paginationParams" :totalItems="state.totalItems"
      :idSP="idSanPham" @add="openAddModal" @view="openViewModal" @page-change="handlePageChange"
      @change-status="handleChangeStatus" />

    <ProductModal :open="state.isModalOpen" :openChangeStatus="state.isModalChangeStatus"
      :productId="state.selectedProductId" :idSP="idSanPham" :title="modalTitle"
      @closeChangeStatus="closeModalChangeStatus" @close="closeModal" @success="fetchProducts" />

  </div>
</template>

<script setup lang="ts">
import BreadcrumbDefault from '@/components/ui/Breadcrumbs/BreadcrumbDefault.vue';
import ProductFilter from './SanPhamChiTietFilter.vue';
import ProductTable from './SanPhamChiTietTable.vue';
import ProductModal from './SanPhamChiTietModal.vue';
import { computed, onMounted, reactive, watch } from 'vue';
import { GetSanPhams, type SanPhamResponse, type ParamsGetSanPham } from '@/services/api/admin/sanphamchitiet.api';
import { debounce } from 'lodash';
import DivCustom from '@/components/custom/Div/DivCustomAll.vue'
import { useRoute } from 'vue-router';
import { FilterOutlined, UnorderedListOutlined } from '@ant-design/icons-vue';
const route = useRoute();
const state = reactive({
  searchQuery: '',
  idSP: '',
  searchStatus: null as number | null,
  isModalOpen: false,
  priceRange: [0, 1000000000000] as [number, number],
  isModalChangeStatus: false,
  selectedProductId: null as string | null,
  products: [] as SanPhamResponse[],
  paginationParams: { page: 1, size: 10 },
  totalItems: 0,
  searchColor: null as string | null,    // ← thêm đây
  searchSize: null as string | null,
})

const idSanPham = route.query.id || 'default-id';
const modalTitle = computed(() => {
  return state.selectedProductId ? 'Cập nhật sản phẩm chi tiết  ' : 'Thêm khách hàng'
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

const updatePriceRange = (newRange: [number, number]) => {
  state.priceRange = newRange
}

const updateSearchColor = (newColor: string | null) => {
  state.searchColor = newColor
}
const updateSearchSize = (newSize: string | null) => {
  state.searchSize = newSize
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
  fetchProducts();
  state.isModalOpen = false
}

const closeModalChangeStatus = () => {
  fetchProducts();
  state.isModalChangeStatus = false
}

const fetchProducts = async () => {

  console.log(state.searchStatus)
  try {

    const params: ParamsGetSanPham = {
      page: state.paginationParams.page,
      size: state.paginationParams.size,
      q: state.searchQuery,
      idSP: idSanPham,
      idMS: state.searchColor,
      idKT: state.searchSize,
      status: state.searchStatus,
      priceMin: state.priceRange[0],
      priceMax: state.priceRange[1]
    }


    const response = await GetSanPhams(params)


    state.products = response.data?.data
    console.log('Products fetched:', state.products)
    console.log('Products fetched:', state.products.ten)
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
  () => [state.searchQuery, state.searchStatus, state.priceRange, state.searchColor, state.searchSize],
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