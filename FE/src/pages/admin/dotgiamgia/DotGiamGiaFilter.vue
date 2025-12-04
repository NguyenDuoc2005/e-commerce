<template>
  <DivCustom label="Bộ lọc">
    <div class="row">
      <!-- Row 1 -->
      <div class="col-md-4 mb-3">
        <label class="form-label d-flex align-items-center">
          <span class="ms-2">Mã/Tên đợt:</span>
        </label>
        <a-input 
          v-model:value="localFilters.ma" 
          placeholder="Nhập mã/tên đợt"
          style="width: 100%;"
        />
      </div>
      
      <div class="col-md-4 mb-3">
        <label class="form-label">Từ ngày:</label>
        <a-date-picker 
          v-model:value="localFilters.ngayBatDau"
          placeholder="Chọn ngày bắt đầu"
          style="width: 100%;"
          format="DD/MM/YYYY"
          :inputReadOnly="true"
        />
      </div>

      <div class="col-md-4 mb-3">
        <label class="form-label">Đến ngày:</label>
        <a-date-picker 
          v-model:value="localFilters.ngayKetThuc"
          placeholder="Chọn ngày kết thúc"
          style="width: 100%;"
          format="DD/MM/YYYY"
          :inputReadOnly="true"
        />
      </div>
    </div>

    <!-- Row 2 -->
    <div class="row">
      <div class="col-md-4 mb-3">
        <label class="form-label">Giá trị giảm:</label>
        <a-input 
          v-model:value="localFilters.phanTramGiam" 
          placeholder="Nhập giá trị giảm"
          style="width: 100%;"
        />
      </div>
      
      <div class="col-md-4 mb-3">
        <label class="form-label">Trạng thái:</label>
        <a-select 
          v-model:value="localFilters.trangThai"
          placeholder="Tất cả"
          style="width: 100%;"
          :options="statusOptions"
          allowClear
        />
      </div>
      
      <!-- Action buttons -->
    <div class="d-flex justify-content-center gap-2 mt-3 col-md-4 mb-3 align-self-end">
      <a-tooltip title="Làm mới bộ lọc">
        <a-button @click="resetFilters" class="d-flex align-items-center"
        style="background-color: dimgrey; border-color: dimgrey; color: white;">
          Làm mới bộ lọc
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
import dayjs, { type Dayjs } from 'dayjs'

interface FilterProps {
  ma?: string
  ten?: string
  phanTramGiam?: string
  ngayBatDau?: number | null
  ngayKetThuc?: number | null
  trangThai?: number | null
}

const props = defineProps<FilterProps>()
const emit = defineEmits<{
  'update:ma': [value: string]
  'update:ten': [value: string] 
  'update:phanTramGiam': [value: string]
  'update:ngayBatDau': [value: number | null]
  'update:ngayKetThuc': [value: number | null]
  'update:trangThai': [value: number | null]
}>()

const localFilters = ref({
  ma: props.ma || '',
  ten: props.ten || '',
  phanTramGiam: props.phanTramGiam || '',
  ngayBatDau: props.ngayBatDau ? dayjs(props.ngayBatDau) : null as Dayjs | null,
  ngayKetThuc: props.ngayKetThuc ? dayjs(props.ngayKetThuc) : null as Dayjs | null,
  trangThai: props.trangThai
})

const statusOptions = [
  { label: 'Chưa kích hoạt', value: 'CHUA_KICH_HOAT' },
  { label: 'Đang kích hoạt', value: 'DANG_KICH_HOAT' },
  { label: 'Hết hạn kích hoạt', value: 'HET_HAN_KICH_HOAT' }
]

// Watch for changes and emit updates
watch(() => localFilters.value.ma, (newValue) => {
  emit('update:ma', newValue)
})

watch(() => localFilters.value.ten, (newValue) => {
  emit('update:ten', newValue)
})

watch(() => localFilters.value.phanTramGiam, (newValue) => {
  emit('update:phanTramGiam', newValue)
})

watch(() => localFilters.value.ngayBatDau, (newValue) => {
  const timestamp = newValue ? newValue.valueOf() : null
  emit('update:ngayBatDau', timestamp)
})

watch(() => localFilters.value.ngayKetThuc, (newValue) => {
  const timestamp = newValue ? newValue.valueOf() : null
  emit('update:ngayKetThuc', timestamp)
})

watch(() => localFilters.value.trangThai, (newValue) => {
  emit('update:trangThai', newValue)
})

const resetFilters = () => {
  localFilters.value = {
    ma: '',
    ten: '',
    phanTramGiam: '',
    ngayBatDau: null,
    ngayKetThuc: null,
    trangThai: null
  }
  
  // Emit all reset values
  emit('update:ma', '')
  emit('update:ten', '')
  emit('update:phanTramGiam', '')
  emit('update:ngayBatDau', null)
  emit('update:ngayKetThuc', null)
  emit('update:trangThai', null)
}
</script>

<style scoped>
.promotion-icon {
  font-size: 15px;
}

.form-label {
  font-weight: 500;
  margin-bottom: 0.5rem;
  font-size: 15px;
  font-weight: bold;
  color: #333;
}

.row {
  margin-left: 0;
  margin-right: 0;
}

.col-md-4 {
  padding-left: 8px;
  padding-right: 8px;
}

/* Đổi màu border khi hover cho a-input */
:deep(.ant-input:hover),
:deep(.ant-input:focus) {
  border-color: #58bddb !important;
}

/* Đổi màu border khi hover cho a-date-picker */
:deep(.ant-picker:hover),
:deep(.ant-picker-focused) {
  border-color: #58bddb !important;
}

/* Đổi màu border khi hover cho a-select */
:deep(.ant-select-selector:hover),
:deep(.ant-select-focused .ant-select-selector) {
  border-color: #58bddb !important;
}

</style>