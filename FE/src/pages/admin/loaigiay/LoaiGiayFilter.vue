<template>
  <DivCustom>
    <div class="filter-container">
      <div class="filter-item search-input-group">
        <label for="search-query" class="filter-label">Tìm kiếm danh mục</label>
        <a-input id="search-query" v-model:value="localSearchQuery" placeholder="Nhập mã / tên để tìm kiếm..."
          class="search-input" />
      </div>
      <div class="filter-item search-input-group">
        <label for="search-query" class="filter-label">Trạng thái:</label>
        <a-select class="select-input" v-model:value="localSearchStatus" allow-clear style="width: 180px" size="small"
          placeholder="Chọn trạng thái">
          <a-select-option :value="1">Hoạt động</a-select-option>
          <a-select-option :value="0">Ngừng hoạt động</a-select-option>
        </a-select>
      </div>
      <div class="filter-item reset-button-group">
        <a-tooltip title="Làm mới bộ lọc">
          <a-button style="background-color: dimgrey; color: white;" @click="resetFilters" class="reset-button">
            Đặt lại bộ lọc
            <ReloadOutlined />
          </a-button>
        </a-tooltip>
      </div>
    </div>
  </DivCustom>
</template>

<script setup lang="ts">
import { ref, watch, defineProps, defineEmits } from 'vue'
import DivCustom from '@/components/custom/Div/DivCustom.vue'
import { ReloadOutlined } from '@ant-design/icons-vue'

const props = defineProps<{ searchQuery: string; searchStatus: number | null }>()
const emit = defineEmits(['update:searchQuery', 'update:searchStatus'])

const localSearchQuery = ref(props.searchQuery)
const localSearchStatus = ref(props.searchStatus)

const options = [
  { label: 'Hoạt động', value: 0 },
  { label: 'Ngừng hoạt động', value: 1 }
]

watch([localSearchQuery, localSearchStatus], ([newQuery, newStatus]) => {
  emit('update:searchQuery', newQuery)
  emit('update:searchStatus', newStatus)
})

const resetFilters = () => {
  localSearchQuery.value = ''
  localSearchStatus.value = null
  emit('update:searchQuery', '')
  emit('update:searchStatus', null)
}
</script>

<style scoped lang="scss">
/* Use scoped style for better component encapsulation */

.filter-container {
  display: flex;
  flex-wrap: wrap;
  /* Allow items to wrap to the next line on smaller screens */
  gap: 20px;
  /* Space between filter items */
  align-items: flex-end;
  /* Align items to the bottom of the container */
  padding: 15px;
  /* Add some padding around the filter section */

  border-radius: 8px;
  /* Slightly rounded corners */
}

.filter-item {
  display: flex;
  flex-direction: column;
  /* Stack label above input/button */
  justify-content: flex-end;
  /* Push content to the bottom if container has extra space */
}

.filter-label {
  font-size: 14px;
  font-weight: bold;
  /* This will now be effective */
  margin-bottom: 5px;
  color: #555;
  white-space: nowrap;
}

.search-input {
  width: 700px;
  /* Adjust width as needed for better responsiveness */
  min-width: 200px;
  /* Minimum width for search input */
}

.reset-button {
  display: flex;
  /* Ensure icon and text are side-by-side */
  align-items: center;
  /* Vertically center icon and text */
  gap: 5px;
  /* Space between text and icon */
  height: 32px;
  /* Standard Ant Design button height */
  padding: 0 15px;
  /* Adjust padding for better look */
  margin-top: 25px;
  /* Align button baseline with input text. Adjust as needed based on actual font sizes/line heights */
}

/* Optional: If you want to match the Ant Design input height precisely for the button */
.ant-input {
  height: 32px;
  /* Default Ant Design input height */
}

// Basic Ant Design button styles often handle 'd-flex', 'justify-content-center', 'align-items-center', 'px-4'
// These are likely utility classes from another framework (like Bootstrap or Tailwind).
// If they are not working, you'd need to define them, e.g.:
/*
.d-flex { display: flex; }
.align-items-center { align-items: center; }
.justify-content-center { justify-content: center; }
.px-4 { padding-left: 1rem; padding-right: 1rem; }
*/

/* Global body font is okay, but usually specified in a global stylesheet */
body {
  font-family: 'Roboto', sans-serif;
}

:deep(.ant-input:hover),
:deep(.ant-input:focus),
:deep(.ant-input-focused) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

:deep(.ant-input-number:hover),
:deep(.ant-input-number:focus),
:deep(.ant-input-number-focused) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

:deep(.ant-picker:hover),
:deep(.ant-picker-focused),
:deep(.ant-picker-focused .ant-picker-input > input),
:deep(.ant-picker:focus-within) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

body {
  font-family: 'Roboto', sans-serif;
}

:deep(.ant-select:not(.ant-select-disabled):hover .ant-select-selector),
:deep(.ant-select-focused:not(.ant-select-disabled) .ant-select-selector),
:deep(.ant-select-open .ant-select-selector) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

:deep(.ant-select-focused .ant-select-selector),
:deep(.ant-select-open .ant-select-selector) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

:deep(.ant-select-item-option-active) {
  background-color: #e6f7ff !important;
}

:deep(.ant-select-selector) {
  border-radius: 4px !important;
}

:deep(.ant-btn:hover),
:deep(.ant-btn:focus) {
  background-color: #4aa8c6 !important;
  border-color: #4aa8c6 !important;
  color: white !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

:deep(.ant-radio-wrapper:hover .ant-radio-inner) {
  border-color: #58bddb !important;
}

:deep(.ant-radio-wrapper:hover .ant-radio .ant-radio-inner::after) {
  background-color: #58bddb !important;
}
</style>

