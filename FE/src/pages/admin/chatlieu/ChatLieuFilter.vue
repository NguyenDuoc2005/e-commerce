<template>
  <DivCustom>
    <div class="filter-container">
      <div class="filter-item search-input-group">
        <label for="search-query" class="filter-label">Tìm kiếm chất liệu</label>
        <a-input id="search-query" v-model:value="localSearchQuery" placeholder="Nhập mã / tên để tìm kiếm..."
          class="search-input" />
      </div>
      <div class="filter-item search-input-group">
        <label for="search-query" class="filter-label">Trạng thái:</label>
        <a-select class="select-input" v-model:value="localSearchStatus" allow-clear style="width: 180px" size="small"
          placeholder="Chọn trạng thái">
          <a-select-option :value="1">Hoạt động</a-select-option>
          <a-select-option :value="0">Không hoạt động</a-select-option>
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
  font-weight: bold; /* This will now be effective */
  margin-bottom: 5px;
  color: #555;
  white-space: nowrap;
}

.search-input {
  width: 700px;
  /* Adjust width as needed for better responsiveness */
  min-width: 200px;
  /* Minimum width for search input */
  height: 32px !important;
  /* Đặt chiều cao cố định cho input */
  transition: border-color 0.3s ease;
  /* Smooth transition for hover effect */
  
  &:hover {
    border-color: #58bddb !important;
    /* Hover color */
  }
  
  &:focus {
    border-color: #58bddb !important;
    box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.2) !important;
    /* Focus effect with same color */
  }
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

/* Đồng nhất chiều cao cho tất cả input và select */
.ant-input {
  height: 32px !important;
  /* Chiều cao cố định cho input */
  transition: border-color 0.3s ease;
  
  &:hover {
    border-color: #58bddb !important;
  }
  
  &:focus {
    border-color: #58bddb !important;
    box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.2) !important;
  }
}

/* Điều chỉnh chiều cao cho a-select để đồng nhất với input */
.select-input {
  height: 32px;
  transition: border-color 0.3s ease;
  
  &:hover {
    :deep(.ant-select-selector) {
      border-color: #58bddb !important;
      /* Hover effect for select */
    }
  }
  
  &:focus-within {
    :deep(.ant-select-selector) {
      border-color: #58bddb !important;
      box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.2) !important;
      /* Focus effect for select */
    }
  }
  
  :deep(.ant-select-selector) {
    height: 32px !important;
    /* Chiều cao của selector bằng với input */
    display: flex;
    align-items: center;
    /* Căn giữa nội dung theo chiều dọc */
    padding: 0 11px;
    /* Padding giống input */
    border-radius: 6px;
    /* Bo góc giống input */
    transition: border-color 0.3s ease, box-shadow 0.3s ease;
    /* Smooth transition */
  }

  :deep(.ant-select-selection-item) {
    line-height: 30px !important;
    /* Căn giữa text trong selector */
    padding: 0;
    /* Bỏ padding mặc định */
  }

  :deep(.ant-select-selection-placeholder) {
    line-height: 30px !important;
    /* Căn giữa placeholder */
    color: #bfbfbf;
    /* Màu placeholder giống input */
  }

  :deep(.ant-select-arrow) {
    right: 11px;
    /* Vị trí mũi tên giống input */
  }

  :deep(.ant-select-clear) {
    right: 32px;
    /* Vị trí nút clear */
  }
  
  /* Hover effect specifically for select dropdown */
  :deep(.ant-select-selector:hover) {
    border-color: #58bddb !important;
  }
  
  /* Focus effect for select */
  :deep(.ant-select-focused .ant-select-selector) {
    border-color: #58bddb !important;
    box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.2) !important;
  }
}

/* Đảm bảo tất cả các thành phần filter có chiều cao đồng nhất */
.search-input-group {
  .ant-input,
  .ant-select {
    height: 32px;
  }
}

/* Global body font is okay, but usually specified in a global stylesheet */
body {
  font-family: 'Roboto', sans-serif;
}
</style>