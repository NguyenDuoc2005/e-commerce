<template>
  <div class="page-container">
    <div class="breadcrumb-section">
      <BreadcrumbDefault :pageTitle="'Người dùng'" :routes="[
        { path: '/admin/khach-hang', name: 'Người dùng' }
      ]" />
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
      <UnorderedListOutlined /> Danh sách người dùng
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
  </div>
</template>

<script setup lang="ts">
import BreadcrumbDefault from '@/components/ui/Breadcrumbs/BreadcrumbDefault.vue'
import ProductFilter from './KhachHangFilter.vue'
import ProductTable from './KhachHangTable.vue'
import { onMounted, reactive, watch } from 'vue'
import {
  GetKhachHangs,
  getSellerStatusesByOwnerIds,
  type KhachHangResponse,
  type ParamsGetKhachHang
} from '@/services/api/admin/khachhang.api'
import { debounce } from 'lodash'
import { toast } from 'vue3-toastify'
import { FilterOutlined, UnorderedListOutlined } from '@ant-design/icons-vue'

const state = reactive({
  searchQuery: '',
  searchStatus: null as number | null,
  selectedProductId: null as string | null,
  products: [] as KhachHangResponse[],
  paginationParams: { page: 1, size: 10 },
  totalItems: 0
})

const updateSearchQuery = (newQuery: string) => {
  state.searchQuery = newQuery
}

const updateSearchStatus = (newStatus: number | null) => {
  state.searchStatus = newStatus
}

const openAddModal = () => {
  state.selectedProductId = null
}

const openViewModal = (id: string) => {
  state.selectedProductId = id
}

const fetchProducts = async () => {
  try {
    const params: ParamsGetKhachHang = {
      page: state.paginationParams.page,
      size: state.paginationParams.size,
      q: state.searchQuery,
      status: state.searchStatus
    }
    const response = await GetKhachHangs(params)
    const users = response.data?.data || []
    const sellerStatuses = await getSellerStatusesByOwnerIds(users.map((user) => user.id))

    state.products = users.map((user) => {
      const seller = sellerStatuses[user.id]
      return {
        ...user,
        sellerStatus: seller?.status || null,
        sellerShopName: seller?.shopName || null
      }
    })
    state.totalItems = response.data?.totalElements || 0
  } catch (error) {
    console.error('Failed to fetch users:', error)
  }
}

const debouncedFetchProducts = debounce(fetchProducts, 300)

onMounted(() => {
  fetchProducts()
  const storedToast = sessionStorage.getItem('appToastMessage')
  if (storedToast) {
    try {
      const { message, type } = JSON.parse(storedToast)
      if (message) {
        if (type === 'success') {
          toast.success(message)
        } else if (type === 'error') {
          toast.error(message)
        }
      }
    } catch (e) {
      console.error('Error parsing stored toast message:', e)
    } finally {
      sessionStorage.removeItem('appToastMessage')
    }
  }
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
  fetchProducts()
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.breadcrumb-section {
  margin-bottom: 25px;
  background-color: #fff;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.section-title {
  margin-top: 30px;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  margin-left: 0;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
