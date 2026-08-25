<template>
  <div class="page-container">
    <div class="breadcrumb-section">
      <BreadcrumbDefault :pageTitle="'Voucher sàn'" :routes="[
        { path: '/admin/voucher', name: 'Voucher sàn' }
      ]" />
    </div>

    <p class="section-title">
      <FilterOutlined /> Bộ lọc tìm kiếm
    </p>
    <VoucherFilter
      v-model:searchQuery="state.searchQuery"
      v-model:startDate="state.startDate"
      v-model:endDate="state.endDate"
      v-model:kieuGiam="state.kieuGiam"
      v-model:status="state.status"
      @resetFilters="handleResetFilters"
    />

    <p class="section-title">
      <UnorderedListOutlined /> Danh sách voucher sàn
    </p>
    <VoucherTable
      :products="state.vouchers"
      :paginationParams="state.paginationParams"
      :totalItems="state.totalItems"
      @add="openAddModal"
      @view="openViewModal"
      @page-change="handlePageChange"
      @change-status="handleChangeStatus"
    />

    <VoucherModal
      v-if="state.isModalOpen"
      :open="state.isModalOpen"
      :productId="state.selectedVoucherId"
      :title="modalTitle"
      @close="closeModal"
      @success="handleModalSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import BreadcrumbDefault from '@/components/ui/Breadcrumbs/BreadcrumbDefault.vue'
import VoucherFilter from './VoucherFilter.vue'
import VoucherTable from './VoucherTable.vue'
import VoucherModal from './VoucherModal.vue'
import { computed, onMounted, reactive, watch } from 'vue'
import { debounce } from 'lodash'
import { FilterOutlined, UnorderedListOutlined } from '@ant-design/icons-vue'
import { GetSizes, type SizeResponse, type ParamsGetSize } from '@/services/api/admin/voucher.api'

const state = reactive({
  searchQuery: '',
  startDate: null as string | null,
  endDate: null as string | null,
  kieuGiam: null as number | null,
  status: null as number | null,
  isModalOpen: false,
  selectedVoucherId: null as string | null,
  vouchers: [] as SizeResponse[],
  paginationParams: { page: 1, size: 10 },
  totalItems: 0,
})

const modalTitle = computed(() => {
  return state.selectedVoucherId ? 'Cập nhật voucher sàn' : 'Thêm voucher sàn'
})

const openAddModal = () => {
  state.selectedVoucherId = null
  state.isModalOpen = true
}

const openViewModal = (id: string) => {
  state.selectedVoucherId = id
  state.isModalOpen = true
}

const closeModal = () => {
  state.isModalOpen = false
}

const handleModalSuccess = () => {
  closeModal()
  fetchVouchers()
}

const fetchVouchers = async () => {
  try {
    const params: ParamsGetSize = {
      page: state.paginationParams.page,
      size: state.paginationParams.size,
      q: state.searchQuery,
      startDate: state.startDate,
      endDate: state.endDate,
      kieuGiam: state.kieuGiam,
      status: state.status,
    }
    const response = await GetSizes(params)
    state.vouchers = response.data?.data || []
    state.totalItems = response.data?.totalElements || 0
  } catch (error) {
    console.error('Failed to fetch platform vouchers:', error)
    state.vouchers = []
    state.totalItems = 0
  }
}

const debouncedFetchVouchers = debounce(() => {
  state.paginationParams.page = 1
  fetchVouchers()
}, 300)

onMounted(() => {
  fetchVouchers()
})

watch(
  [
    () => state.searchQuery,
    () => state.startDate,
    () => state.endDate,
    () => state.kieuGiam,
    () => state.status,
  ],
  () => {
    debouncedFetchVouchers()
  }
)

const handleResetFilters = () => {
  state.paginationParams.page = 1
  fetchVouchers()
}

const handlePageChange = ({ page, pageSize }: { page: number; pageSize?: number }) => {
  state.paginationParams.page = page
  if (pageSize) {
    state.paginationParams.size = pageSize
  }
  fetchVouchers()
}

const handleChangeStatus = async () => {
  fetchVouchers()
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.breadcrumb-section {
  margin-bottom: 15px;
  background-color: #fff;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-left: 0;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
