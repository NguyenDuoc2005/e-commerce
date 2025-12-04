<template>
  <DivCustom label="Bộ lọc">
    <div class="d-flex flex-column gap-3">
      <div class="d-flex align-items-center gap-2">
        <div class="filter-item search-input-group">
          <label for="search-status" class="filter-label">Trạng thái:</label>
          <a-select
            id="search-status"
            class="select-input"
            v-model:value="localSearchStatus"
            allow-clear
            style="width: 450px"
            size="small"
            placeholder="Chọn trạng thái"
          >
            <a-select-option :value="1">Hoạt động</a-select-option>
            <a-select-option :value="0">Ngừng hoạt động</a-select-option>
          </a-select>
        </div>
        
        <div class="filter-item search-input-group" style="margin-left: 20px;">
          <label for="price-range-min" class="filter-label">Khoảng giá:</label>
          <div class="d-flex align-items-center gap-2">
            <a-input
              id="price-range-min"
              v-model:value="localPriceMinFormatted"
              @blur="handlePriceInput"
              placeholder="Giá tối thiểu"
              style="width: 220px;"
              size="small"
              type="text"
              @input="formatPriceInput($event, 'min')"
            />
            <span>-</span>
            <a-input
              id="price-range-max"
              v-model:value="localPriceMaxFormatted"
              @blur="handlePriceInput"
              placeholder="Giá tối đa"
              style="width: 220px;"
              size="small"
              type="text"
              @input="formatPriceInput($event, 'max')"
            />
          </div>
        </div>
      </div>

      <div class="d-flex flex-wrap gap-4" style="margin-left: 0px">
        <div class="filter-item search-input-group">
          <label for="search-color" class="filter-label">Màu:</label>
          <a-select
            id="search-color"
            v-model:value="localColor"
            @change="handleColorChange"
            allow-clear
            :options="ColorOptions"
            placeholder="Chọn màu sắc"
            style="width: 450px"
            size="small"
          />
        </div>

        <div class="filter-item search-input-group">
          <label for="search-size" class="filter-label">Kích cỡ:</label>
          <a-select
            id="search-size"
            v-model:value="localSize"
            @change="handleSizeChange"
            allow-clear
            :options="SizeOptions"
            placeholder="Chọn kích cỡ"
            style="width: 450px"
            size="small"
          />
        </div>
        <div class="filter-item reset-button-group">
          <a-tooltip title="Làm mới bộ lọc">
            <a-button
              style="background-color: dimgrey; margin-left: 900px; color: white;"
              @click="resetFilters"
              class="reset-button"
            >
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
import { ref, watch, defineProps, defineEmits, onMounted } from 'vue';
import DivCustom from '@/components/custom/Div/DivCustom.vue';
import { ReloadOutlined } from '@ant-design/icons-vue';
import { GetListColor, GetListSize } from '@/services/api/admin/sanphamchitiet.api';

const props = defineProps<{
  searchQuery: string;
  searchStatus: number | null;
  priceRange?: [number | null, number | null]; // Có thể là null
  searchColor?: string | null;
  searchSize?: string | null;
  giaMax?: string | number | null; // Không còn sử dụng trong component này
}>();

const emit = defineEmits([
  'update:searchQuery',
  'update:searchStatus',
  'update:priceRange',
  'update:searchColor',
  'update:searchSize',
]);

const localSearchQuery = ref(props.searchQuery);
const localSearchStatus = ref<number | null>(props.searchStatus);

// Chúng ta sẽ dùng các biến này để lưu giá trị SỐ nguyên
const localPriceMin = ref<number | null>(null);
const localPriceMax = ref<number | null>(null);

// Và các biến này để hiển thị ra input (đã định dạng)
const localPriceMinFormatted = ref('');
const localPriceMaxFormatted = ref('');


const ColorOptions = ref<{ label: string; value: string }[]>([]);
const SizeOptions = ref<{ label: string; value: string }[]>([]);
const localColor = ref<string | null>(props.searchColor ?? null);
const localSize = ref<string | null>(props.searchSize ?? null);

const handleStatusChange = (v: number | undefined) => {
  localSearchStatus.value = v ?? null;
};
const handleColorChange = (v: string | undefined) => {
  localColor.value = v ?? null;
};
const handleSizeChange = (v: string | undefined) => {
  localSize.value = v ?? null;
};

// Hàm định dạng số thành chuỗi tiền tệ và cập nhật biến localPriceMinFormatted/localPriceMaxFormatted
const formatPriceInput = (event: Event, type: 'min' | 'max') => {
  const input = event.target as HTMLInputElement;
  let value = input.value.replace(/\D/g, ''); // Xóa tất cả trừ số
  
  if (value === '') {
    if (type === 'min') {
      localPriceMinFormatted.value = '';
      localPriceMin.value = null;
    } else {
      localPriceMaxFormatted.value = '';
      localPriceMax.value = null;
    }
    return;
  }

  const numValue = Number(value);
  if (!isNaN(numValue)) {
    if (type === 'min') {
      localPriceMin.value = numValue;
      localPriceMinFormatted.value = numValue.toLocaleString('vi-VN');
    } else {
      localPriceMax.value = numValue;
      localPriceMaxFormatted.value = numValue.toLocaleString('vi-VN');
    }
  } else {
    // Nếu không phải số, reset về giá trị trước đó hoặc rỗng
    if (type === 'min') {
      localPriceMinFormatted.value = localPriceMin.value !== null ? localPriceMin.value.toLocaleString('vi-VN') : '';
    } else {
      localPriceMaxFormatted.value = localPriceMax.value !== null ? localPriceMax.value.toLocaleString('vi-VN') : '';
    }
  }
};


// Hàm xử lý khi blur khỏi input giá
const handlePriceInput = () => {
  let finalMin = localPriceMin.value;
  let finalMax = localPriceMax.value;

  // Logic hoán đổi nếu min > max và cả hai đều có giá trị
  if (finalMin !== null && finalMax !== null && finalMin > finalMax) {
    [finalMin, finalMax] = [finalMax, finalMin];
    // Cập nhật lại giá trị hiển thị đã được định dạng
    localPriceMinFormatted.value = finalMin.toLocaleString('vi-VN');
    localPriceMaxFormatted.value = finalMax.toLocaleString('vi-VN');
  }

  // Emit giá trị đã được chuẩn hóa (số hoặc null) về component cha
  emit('update:priceRange', [finalMin, finalMax]);
};

// Watch cho props.priceRange để cập nhật localPriceMin/Max khi cha thay đổi
watch(
  () => props.priceRange,
  (newPriceRange) => {
    const newPropMin = newPriceRange?.[0] ?? null;
    const newPropMax = newPriceRange?.[1] ?? null;

    // Chỉ cập nhật nếu giá trị số thực sự khác biệt
    if (localPriceMin.value !== newPropMin || localPriceMax.value !== newPropMax) {
      localPriceMin.value = newPropMin;
      localPriceMax.value = newPropMax;

      // Cập nhật các biến formatted
      localPriceMinFormatted.value = newPropMin !== null ? newPropMin.toLocaleString('vi-VN') : '';
      localPriceMaxFormatted.value = newPropMax !== null ? newPropMax.toLocaleString('vi-VN') : '';
    }
  },
  { immediate: true, deep: true }
);

// Các watcher khác giữ nguyên
watch(
  [localSearchQuery, localSearchStatus, localColor, localSize],
  ([q, st, col, sz]) => {
    emit('update:searchQuery', q);
    emit('update:searchStatus', st ?? null);
    emit('update:searchColor', col ?? null);
    emit('update:searchSize', sz ?? null);
  },
  { deep: true } 
);

const fetchColor = async () => {
  try {
    const { data } = await GetListColor();
    ColorOptions.value = data.map((c: any) => ({ label: c.ten, value: c.id }));
  } catch (e) {
    console.error('Error fetching colors:', e);
  }
};

const fetchSize = async () => {
  try {
    const { data } = await GetListSize();
    SizeOptions.value = data.map((s: any) => ({ label: s.ten, value: s.id }));
  } catch (e) {
    console.error('Error fetching sizes:', e);
  }
};

const resetFilters = () => {
  console.log('Resetting filters...');
  localSearchQuery.value = '';
  localSearchStatus.value = null;
  
  localPriceMin.value = null; // Đặt về null cho giá trị số
  localPriceMax.value = null; // Đặt về null cho giá trị số
  localPriceMinFormatted.value = ''; // Đặt về rỗng cho hiển thị
  localPriceMaxFormatted.value = ''; // Đặt về rỗng cho hiển thị

  localColor.value = null;
  localSize.value = null;

  emit('update:searchQuery', '');
  emit('update:searchStatus', null);
  emit('update:priceRange', [null, null]); // Emit [null, null] khi reset
  emit('update:searchColor', null);
  emit('update:searchSize', null);
  console.log('Filters reset. New priceRange (emitted):', [null, null]);
};

onMounted(async () => {
  await fetchColor();
  await fetchSize();
});
</script>

<style scoped lang="scss">
// CSS của bạn (không thay đổi)
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
  margin-bottom: 5px;
  color: #555;
  white-space: nowrap;
}

.search-input {
  width: 700px;
  min-width: 200px;
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
  height: 32px;
}

:deep(.ant-input:focus),
:deep(.ant-input-focused) {
  border-color: #54bddb !important;
  box-shadow: 0 0 0 2px rgba(95, 179, 179, 0.2) !important;
}

:deep(.ant-select:not(.ant-select-disabled):hover .ant-select-selector),
:deep(.ant-select-focused:not(.ant-select-disabled) .ant-select-selector) {
  border-color: #54bddb !important;
  box-shadow: 0 0 0 2px rgba(95, 179, 179, 0.2) !important;
}

:deep(.ant-textarea:focus),
:deep(.ant-textarea-focused) {
  border-color: #54bddb !important;
  box-shadow: 0 0 0 2px rgba(95, 179, 179, 0.2) !important;
}

:deep(.ant-radio-wrapper:hover .ant-radio .ant-radio-inner),
:deep(.ant-checkbox-wrapper:hover .ant-checkbox .ant-checkbox-inner),
:deep(.ant-radio-input:focus + .ant-radio-inner),
:deep(.ant-checkbox-input:focus + .ant-checkbox-inner) {
  border-color: #54bddb !important;
}

:deep(.ant-radio-checked .ant-radio-inner::after) {
  background-color: #54bddb !important;
}

:deep(.ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #54bddb !important;
  border-color: #54bddb !important;
}

:deep(.ant-btn:focus),
:deep(.ant-btn:active) {
  border-color: #54bddb !important;
  color: white !important;
}

body {
  font-family: 'Roboto', sans-serif;
}
</style>