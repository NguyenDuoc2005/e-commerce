<template>
  <div class="page-container">
    <div class="breadcrumb-section">
      <BreadcrumbDefault :pageTitle="'Quản lý phiếu giảm giá'" :routes="[
        { path: '/admin/voucher', name: 'Quản lý phiếu giảm giá' }
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
      <UnorderedListOutlined /> Danh sách phiếu giảm giá
    </p>
    <VoucherTable
      :products="state.vouchers" :paginationParams="state.paginationParams"
      :totalItems="state.totalItems"
      @add="openAddModal"
      @view="openViewModal"
      @page-change="handlePageChange"
      @change-status="handleChangeStatus"
    />

    </div>
</template>

<script setup lang="ts">
import BreadcrumbDefault from '@/components/ui/Breadcrumbs/BreadcrumbDefault.vue';
import VoucherFilter from './VoucherFilter.vue';
import VoucherTable from './VoucherTable.vue';
// import VoucherModal from './VoucherModal.vue'; // Uncomment if you are using a modal
import { computed, onMounted, reactive, watch } from 'vue';
import { debounce } from 'lodash';
import { FilterOutlined, UnorderedListOutlined } from '@ant-design/icons-vue';

// --- IMPORTANT: Adjust these imports and types to match your actual API ---
// Assuming you have an API service for vouchers.
// Replace `GetSizes`, `SizeResponse`, `ParamsGetSize` with your actual voucher API.
import { GetSizes, type SizeResponse, type ParamsGetSize } from '@/services/api/admin/voucher.api';
// If your API returns a generic data structure, define it. Example:
// interface ApiResponse<T> {
//   data?: {
//     data: T[];
//     totalElements: number;
//     // ... other pagination info
//   };
//   // ... other response properties
// }

// Khai báo state với đầy đủ các bộ lọc và dữ liệu
const state = reactive({
  searchQuery: '',
  startDate: null as string | null,
  endDate: null as string | null,
  kieuGiam: null as number | null, // Use number | null for 0 (percentage), 1 (amount)
  status: null as number | null, // Use number | null for 0 (active), 1 (inactive)
  isModalOpen: false,
  isModalChangeStatus: false,
  selectedVoucherId: null as string | null,
  vouchers: [] as SizeResponse[], // Renamed from 'products' to 'vouchers' for clarity
  paginationParams: { page: 1, size: 10 },
  totalItems: 0,
});

const modalTitle = computed(() => {
  return state.selectedVoucherId ? 'Cập nhật phiếu giảm giá' : 'Thêm phiếu giảm giá';
});

const openAddModal = () => {
  state.selectedVoucherId = null;
  state.isModalOpen = true;
};

const openViewModal = (id: string) => {
  state.selectedVoucherId = id;
  state.isModalOpen = true;
};

const openChangeStatusModal = (id: string) => {
  state.selectedVoucherId = id;
  state.isModalChangeStatus = true;
};

const closeModal = () => {
  state.isModalOpen = false;
  fetchVouchers(); // Refresh list after modal closes
};

const closeModalChangeStatus = () => {
  state.isModalChangeStatus = false;
  fetchVouchers(); // Refresh list after status change modal closes
};

// Hàm fetch chính để lấy danh sách voucher
const fetchVouchers = async () => {
  try {
    const params: ParamsGetSize = { // Use your actual parameter type here
      page: state.paginationParams.page,
      size: state.paginationParams.size,
      q: state.searchQuery,
      startDate: state.startDate,
      endDate: state.endDate,
      kieuGiam: state.kieuGiam,
      status: state.status,
    };

    // Replace `GetVouchers` with your actual API call for fetching vouchers
    const response = await GetSizes(params);

    state.vouchers = response.data?.data || [];
    state.totalItems = response.data?.totalElements || 0;
  } catch (error) {
    console.error('Failed to fetch vouchers:', error);
    state.vouchers = [];
    state.totalItems = 0;
  }
};

// Debounced version of fetchVouchers to limit API calls when typing in search
const debouncedFetchVouchers = debounce(() => {
  state.paginationParams.page = 1; // Reset page on filter changes (except page change itself)
  fetchVouchers();
}, 300);

onMounted(() => {
  fetchVouchers(); // Initial data fetch when component mounts
});

// Watch all filter variables. When any of them change (due to v-model updates from VoucherFilter),
// trigger the debounced fetch.
watch(
  [
    () => state.searchQuery,
    () => state.startDate,
    () => state.endDate,
    () => state.kieuGiam,
    () => state.status,
  ],
  () => {
    debouncedFetchVouchers();
  }
);


const handleResetFilters = () => {

  state.paginationParams.page = 1; // Reset page to 1 after filters are reset
  fetchVouchers();
};


const handlePageChange = ({ page, pageSize }: { page: number; pageSize?: number }) => {
  state.paginationParams.page = page;
  if (pageSize) {
    state.paginationParams.size = pageSize;
  }
  fetchVouchers(); // Fetch data when page or page size changes
};

const handleChangeStatus = async () => {

  fetchVouchers();
};
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
  margin-left: 0px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

body {
  font-family: 'Roboto', sans-serif;
}
</style>