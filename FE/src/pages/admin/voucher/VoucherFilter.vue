```vue
<template>
  <DivCustom label="Bộ lọc">
    <div class="container-fluid">
      <div class="row">
        <div class="col-12 mb-3">
          <label class="form-label d-flex align-items-center">
            <span class="ms-2">Mã/Tên Phiếu:</span>
          </label>
          <a-input v-model:value="localSearchQuery" placeholder="Tìm kiếm" style="width: 100%;" />
        </div>
      </div>

      <div class="row">
        <div class="col-md-4 col-sm-6 mb-3 filter-item">
          <label for="status-select" class="filter-label">Trạng thái:</label>
          <a-select v-model:value="localStatusFilter" class="select-input" allow-clear style="width: 100%"
            placeholder="Chọn trạng thái">
            <a-select-option :value="1">Hoạt động</a-select-option>
            <a-select-option :value="0">Ngừng hoạt động</a-select-option>
          </a-select>
        </div>

        <div class="col-md-4 col-sm-6 mb-3 filter-item">
          <label for="kieu-giam-select" class="filter-label">Kiểu giảm giá:</label>
          <a-select v-model:value="localKieuGiamFilter" class="select-input" allow-clear style="width: 100%"
            placeholder="Chọn kiểu giảm">
            <a-select-option :value="0">Phần trăm</a-select-option>
            <a-select-option :value="1">Tiền</a-select-option>
          </a-select>
        </div>

        <div class="col-md-4 col-sm-12 mb-3 filter-item">
          <label for="kieu-giam-select" class="filter-label">Chọn khoảng thời gian</label>
          <a-range-picker v-model:value="localDateRange" format="DD/MM/YYYY" :allowClear="true" style="width: 100%;"
            placeholder="Chọn khoảng thời gian" />
        </div>
      </div>

      <div class="row">
        <div class="col-12 mt-3 d-flex justify-content-end">
          <a-tooltip title="Làm mới bộ lọc">
            <a-button style="background-color: dimgrey; color: white" @click="resetFilters"
              class="reset-button filter-control-button">
              Đặt lại bộ lọc
              <ReloadOutlined />
            </a-button>
          </a-tooltip>
        </div>
      </div>
    </div>
  </DivCustom>
</template>

<script setup lang="ts">
import { ref, watch, defineProps, defineEmits, computed } from 'vue';
import DivCustom from '@/components/custom/Div/DivCustom.vue';
import { ReloadOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import type { Dayjs } from 'dayjs';

// --- Props Definition ---
const props = defineProps<{
  searchQuery: string;
  startDate?: string | null;
  endDate?: string | null;
  kieuGiam?: number | null; // 0 for Percentage, 1 for Amount
  status?: number | null; // 0 for Active, 1 for Inactive
}>();

// --- Emits Definition ---
const emit = defineEmits([
  'update:searchQuery',
  'update:startDate',
  'update:endDate',
  'update:kieuGiam',
  'update:status',
]);

// --- Local Reactive Variables for Filters ---
const localSearchQuery = ref(props.searchQuery);
const localKieuGiamFilter = ref(props.kieuGiam);
const localStatusFilter = ref(props.status);

// --- Computed Property for Date Range Picker ---
const localDateRange = computed<[Dayjs, Dayjs] | null>({
  get: () => {
    return props.startDate && props.endDate
      ? [dayjs(props.startDate) as Dayjs, dayjs(props.endDate) as Dayjs]
      : null;
  },
  set: (val: [Dayjs, Dayjs] | null) => {
    emit('update:startDate', val && val.length === 2 ? val[0].format('YYYY-MM-DD') : null);
    emit('update:endDate', val && val.length === 2 ? val[1].format('YYYY-MM-DD') : null);
  },
});

// --- Watchers for Filter Changes ---
watch(localSearchQuery, (newValue) => {
  emit('update:searchQuery', newValue);
});

watch(localKieuGiamFilter, (newValue) => {
  emit('update:kieuGiam', newValue);
});

watch(localStatusFilter, (newValue) => {
  emit('update:status', newValue);
});

// --- Reset Filters Function ---
const resetFilters = () => {
  localSearchQuery.value = '';
  localKieuGiamFilter.value = null;
  localStatusFilter.value = null;
  localDateRange.value = null;

  emit('update:searchQuery', '');
  emit('update:kieuGiam', null);
  emit('update:status', null);
  emit('update:startDate', null);
  emit('update:endDate', null);
};
</script>

<style scoped lang="scss">
/* Basic Bootstrap-like grid system for responsiveness */
.container-fluid {
  width: 100%;
  padding-left: 15px;
  padding-right: 15px;
  margin-left: auto;
  margin-right: auto;
}

.row {
  display: flex;
  flex-wrap: wrap;
  margin-left: -15px;
  margin-right: -15px;
}

.col-12, .col-md-4, .col-sm-6 {
  position: relative;
  width: 100%;
  padding-left: 15px;
  padding-right: 15px;
}

.col-12 {
  flex: 0 0 100%;
  max-width: 100%;
}

@media (min-width: 576px) {
  .col-sm-6 {
    flex: 0 0 50%;
    max-width: 50%;
  }
}

@media (min-width: 768px) {
  .col-md-4 {
    flex: 0 0 33.333333%;
    max-width: 33.333333%;
  }
}

.mb-3 {
  margin-bottom: 1rem !important;
}

.mt-3 {
  margin-top: 1rem !important;
}

.filter-item {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.form-label {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 5px;
  color: #555;
  white-space: nowrap;
  display: flex;
  align-items: center;
}

.filter-label {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 5px;
  color: #555;
  white-space: nowrap;
}

/* Ensure Ant Design components fill the width of their column */
.ant-input,
.ant-picker,
.ant-select {
  width: 100%;
  height: 32px;
}

.reset-button {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 32px;
  padding: 0 15px;
  width: auto;
}

/* Utility classes */
.d-flex {
  display: flex;
}

.align-items-center {
  align-items: center;
}

.justify-content-end {
  justify-content: flex-end;
}

.gap-2 {
  gap: 0.5rem;
}

.ms-2 {
  margin-left: 0.5rem;
}

body {
  font-family: 'Roboto', sans-serif;
}

/* Style cho a-input khi hover, focus, và focused */
:deep(.ant-input:hover),
:deep(.ant-input:focus),
:deep(.ant-input-focused) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

/* Style cho a-input-number khi hover, focus, và focused */
:deep(.ant-input-number:hover),
:deep(.ant-input-number:focus),
:deep(.ant-input-number-focused) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

/* Style cho a-date-picker khi hover, focus, và focused */
:deep(.ant-picker:hover),
:deep(.ant-picker-focused),
:deep(.ant-picker-focused .ant-picker-input > input),
:deep(.ant-picker:focus-within) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

/* Style cho a-select khi hover, focus, và focused */
:deep(.ant-select:not(.ant-select-disabled):hover .ant-select-selector),
:deep(.ant-select-focused .ant-select-selector),
:deep(.ant-select-focused:not(.ant-select-disabled) .ant-select-selector) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

/* Đảm bảo mũi tên của select cũng có màu phù hợp */
:deep(.ant-select:not(.ant-select-disabled) .ant-select-arrow) {
  color: #58bddb !important;
}
</style>
