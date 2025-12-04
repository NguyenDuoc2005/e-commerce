<template>
  <DivCustom>
    <div class="filter-container">
      <div class="filter-item search-input-group">
        <label for="search-query" class="filter-label">Tìm kiếm nhân viên</label>
        <a-input id="search-query" v-model:value="localSearchQuery" placeholder="Nhập mã / tên để tìm kiếm..."
          class="search-input" />
      </div>
      <div class="filter-item search-input-group">
        <label for="search-status" class="filter-label">Trạng thái:</label>
        <a-select id="search-status" class="select-input" v-model:value="localSearchStatus" allow-clear style="width: 180px" size="small"
          placeholder="Chọn trạng thái">
          <a-select-option :value="1">Hoạt động</a-select-option>
          <a-select-option :value="0">Ngừng hoạt động</a-select-option>
        </a-select>
      </div>
      <div class="filter-item reset-button-group">
          <a-button style="background-color: dimgrey; color: white;" @click="resetFilters" class="reset-button">
            Đặt lại bộ lọc
            <ReloadOutlined />
          </a-button>
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
.filter-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: flex-end;
  padding: 15px;
  border-radius: 8px;
}

.filter-item {
  display: flex;    
  flex-direction: column;
  justify-content: flex-end;
}

.filter-label {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 5px;
  color: #555;
  white-space: nowrap;
}

.search-input {
  width: 700px;
  min-width: 200px;
  height: 32px !important;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  transition: border-color 0.3s, box-shadow 0.3s;

  &:hover {
    border-color: #58bddb;
  }

  &:focus-within {
    border-color: #58bddb;
    box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.2);
  }
}

.reset-button {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 32px;
  padding: 0 15px;
  margin-top: 25px;
}

.ant-input {
  height: 32px !important; /* Fixed height for input */
}

.select-input {
  height: 32px; /* Fixed height for select */
  
  :deep(.ant-select-selector) {
    height: 32px !important; /* Fixed height for selector */
    display: flex;
    align-items: center;
    padding: 0 11px; /* Padding to match input */
    border-radius: 6px; /* Rounded corners */
  }

  :deep(.ant-select-selection-item) {
    line-height: 30px !important; /* Center text in selector */
    padding: 0; /* Remove default padding */
  }

  :deep(.ant-select-selection-placeholder) {
    line-height: 30px !important; /* Center placeholder */
    color: #bfbfbf; /* Placeholder color */
  }

  :deep(.ant-select-arrow) {
    right: 11px; /* Position arrow */
  }

  :deep(.ant-select-clear) {
    right: 32px; /* Position clear button */
  }

  :deep(.ant-select-selector:hover) {
    border-color: #58bddb !important; /* Hover effect */
  }

  :deep(.ant-select-focused .ant-select-selector) {
  border-color: #58bddb !important; /* Focus effect */
  box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.2) !important; /* Focus shadow */
  }
  
}

.search-input-group {
  .ant-input,
  .ant-select {
    height: 32px; /* Ensure consistent height */
  }
}

:deep(.ant-select.ant-select-focused .ant-select-selector) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.2) !important;
}

body {
  font-family: 'Roboto', sans-serif;
}
</style>