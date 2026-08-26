<template>
  <div class="page-container"> 
    <div class="breadcrumb-section">
      <BreadcrumbDefault :pageTitle="'Campaign sàn'" :routes="[
        { path: '/admin/campaigns', name: 'Campaign sàn' }
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
      <UnorderedListOutlined /> Danh sách Campaign sàn
    </p>
    <ProductTable 
      :products="state.products" 
      :paginationParams="state.paginationParams" 
      :totalItems="state.totalItems"
      @page-change="handlePageChange" 
      @change-status="handleChangeStatus" 
    />
  </div>
</template>

<script setup lang="ts">
import BreadcrumbDefault from '@/components/ui/Breadcrumbs/BreadcrumbDefault.vue';
import ProductFilter from './CampaignFilter.vue';
import ProductTable from './CampaignTable.vue';
import { onMounted, reactive, watch } from 'vue';
import { getAdminCampaigns, type AdminCampaignResponse, type AdminCampaignParams } from '@/services/api/admin/campaign.api';
import { debounce } from 'lodash';
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
  products: [] as AdminCampaignResponse[],
  paginationParams: { page: 1, size: 10 },
  totalItems: 0
})

const updateFilter = (key: keyof typeof state.filters, value: any) => {
  state.filters[key] = value
}

const fetchProducts = async () => {
  try {
    const params: AdminCampaignParams = {
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
      if (params[key as keyof AdminCampaignParams] === '') {
        delete params[key as keyof AdminCampaignParams]
      }
    })
    
    const response = await getAdminCampaigns(params)
    state.products = response.data?.data ?? []
    state.totalItems = response.data?.totalElements ?? 0
  } catch (error) {
    console.error('Failed to fetch products:', error)
  }
}

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
