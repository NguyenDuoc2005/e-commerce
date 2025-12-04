<template>
  <DivCustom>
    <div class="filter-container">
      <div class="filter-item">
        <label for="search-query" class="filter-label">Tìm kiếm sản phẩm</label>
        <a-input
          id="search-query"
          v-model:value="localSearchQuery"
          placeholder="Nhập mã / tên để tìm kiếm..."
          class="filter-control"
        />
      </div>

      <div class="filter-item">
        <label for="status-select" class="filter-label">Trạng thái:</label>
        <a-select
          id="status-select"
          class="filter-control"
          v-model:value="localSearchStatus"
          allow-clear
          placeholder="Chọn trạng thái"
        >
          <a-select-option :value="1">Hoạt động</a-select-option>
          <a-select-option :value="0">Ngừng hoạt động</a-select-option>
        </a-select>
      </div>

      <div class="filter-item">
        <label for="category-select" class="filter-label">Danh mục:</label>
        <a-select
          id="category-select"
          class="filter-control"
          allow-clear
          :options="danhMucOptions"
          placeholder="Chọn danh mục"
          v-model:value="localSelectedCategory"
        />
      </div>

      <div class="filter-item">
        <label for="material-select" class="filter-label">Chất liệu:</label>
        <a-select
          id="material-select"
          class="filter-control"
          allow-clear
          :options="chatLieuOptions"
          placeholder="Chọn chất liệu"
          v-model:value="localSelectedMaterial"
        />
      </div>

      <div class="filter-item">
        <label for="brand-select" class="filter-label">Thương hiệu:</label>
        <a-select
          id="brand-select"
          class="filter-control"
          allow-clear
          :options="thuongHieuOptions"
          placeholder="Chọn thương hiệu"
          v-model:value="localSelectedBrand"
        />
      </div>

      <div class="filter-item">
        <label for="sole-type-select" class="filter-label">Loại đế:</label>
        <a-select
          id="sole-type-select"
          class="filter-control"
          allow-clear
          :options="loaiDeOptions"
          placeholder="Chọn loại đế"
          v-model:value="localSelectedSoleType"
        />
      </div>

      <div class="filter-item reset-button-group" style="margin-left: auto">
        <a-tooltip title="Làm mới bộ lọc">
          <a-button
            style="background-color: dimgrey; color: white"
            @click="resetFilters"
            class="reset-button filter-control-button"
          >
            Đặt lại bộ lọc
            <ReloadOutlined />
          </a-button>
        </a-tooltip>
      </div>
    </div>
  </DivCustom>
</template>

<script setup lang="ts">
import { ref, watch, defineProps, defineEmits, onMounted } from "vue";
import DivCustom from "@/components/custom/Div/DivCustom.vue";
import { ReloadOutlined } from "@ant-design/icons-vue";
import {
  GetListChatLieu,
  GetListDanhMuc,
  GetListLoaiDe,
  GetListThuongHieu,
} from "@/services/api/admin/sanpham.api";

// Define props for initial search values and emits for updates
const props = defineProps<{
  searchQuery: string;
  searchStatus: number | null;
  selectedCategory: string | null;
  selectedMaterial: string | null;
  selectedBrand: string | null;
  selectedSoleType: string | null;
}>();

const emit = defineEmits([
  "update:searchQuery",
  "update:searchStatus",
  "update:selectedCategory",
  "update:selectedMaterial",
  "update:selectedBrand",
  "update:selectedSoleType",
]);

// Local refs to hold the filter values, initialized from props
const localSearchQuery = ref(props.searchQuery);
const localSearchStatus = ref(props.searchStatus);
const localSelectedCategory = ref(props.selectedCategory);
const localSelectedMaterial = ref(props.selectedMaterial);
const localSelectedBrand = ref(props.selectedBrand);
const localSelectedSoleType = ref(props.selectedSoleType);

// Refs for dropdown options
const thuongHieuOptions = ref<{ label: string; value: string }[]>([]);
const loaiDeOptions = ref<{ label: string; value: string }[]>([]);
const danhMucOptions = ref<{ label: string; value: string }[]>([]);
const chatLieuOptions = ref<{ label: string; value: string }[]>([]);

// Watchers to emit updates to parent component when local values change
watch(
  [
    localSearchQuery,
    localSearchStatus,
    localSelectedCategory,
    localSelectedMaterial,
    localSelectedBrand,
    localSelectedSoleType,
  ],
  ([newQuery, newStatus, newCategory, newMaterial, newBrand, newSoleType]) => {
    emit("update:searchQuery", newQuery);
    emit("update:searchStatus", newStatus);
    emit("update:selectedCategory", newCategory);
    emit("update:selectedMaterial", newMaterial);
    emit("update:selectedBrand", newBrand);
    emit("update:selectedSoleType", newSoleType);
  }
);

// --- API Calls to fetch dropdown data ---

const fetchThuongHieu = async () => {
  try {
    const response = await GetListThuongHieu();
    thuongHieuOptions.value = response.data.map((item: any) => ({
      label: item.ten,
      value: item.id,
    }));
  } catch (error) {
    console.error("Lỗi khi lấy danh sách thương hiệu:", error);
  }
};

const fetchChatLieu = async () => {
  try {
    const response = await GetListChatLieu();
    chatLieuOptions.value = response.data.map((item: any) => ({
      label: item.ten,
      value: item.id,
    }));
  } catch (error) {
    console.error("Lỗi khi lấy danh sách chất liệu:", error);
  }
};

const fetchDanhMuc = async () => {
  try {
    const response = await GetListDanhMuc();
    danhMucOptions.value = response.data.map((item: any) => ({
      label: item.ten,
      value: item.id,
    }));
  } catch (error) {
    console.error("Lỗi khi lấy danh sách danh mục:", error);
  }
};

const fetchLoaiDe = async () => {
  try {
    const response = await GetListLoaiDe();
    loaiDeOptions.value = response.data.map((item: any) => ({
      label: item.ten,
      value: item.id,
    }));
  } catch (error) {
    console.error("Lỗi khi lấy danh sách loại đế:", error);
  }
};

// Function to reset all filters
const resetFilters = () => {
  localSearchQuery.value = "";
  localSearchStatus.value = null;
  localSelectedCategory.value = null;
  localSelectedMaterial.value = null;
  localSelectedBrand.value = null;
  localSelectedSoleType.value = null;

  // Also emit updates for immediate effect in parent
  emit("update:searchQuery", "");
  emit("update:searchStatus", null);
  emit("update:selectedCategory", null);
  emit("update:selectedMaterial", null);
  emit("update:selectedBrand", null);
  emit("update:selectedSoleType", null);
};

// Fetch data when the component is mounted
onMounted(() => {
  fetchDanhMuc();
  fetchThuongHieu();
  fetchLoaiDe();
  fetchChatLieu();
});
</script>

<style scoped lang="scss">
.filter-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: flex-end; /* This is key to aligning the bottom of inputs/buttons */
  padding: 15px;
  border-radius: 8px;
}

.filter-item {
  display: flex;
  flex-direction: column; /* Stacks label above the control */
  justify-content: flex-end; /* Ensures content aligns to the bottom within its flex container */
}

.filter-label {
  font-size: 15px;
  font-weight: bold;
  margin-bottom: 5px; /* Space between label and input/select */
  color: #555;
  white-space: nowrap;
}

/* Common styling for all filter controls (input and select) */
.filter-control {
  width: 350px; /* Consistent width for all inputs and selects */
  height: 32px; /* Standard Ant Design default height for inputs/selects without 'small' size */
}

/* Specific style for the reset button */
.reset-button {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 32px; /* Ensure button height matches other controls */
  padding: 0 15px;
}

/* If you need to override Ant Design's default small size,
   make sure you're not explicitly setting 'size="small"' in template
   or apply override like this: */
:deep(.ant-select-selector),
:deep(.ant-input) {
  height: 32px !important;
  line-height: 32px !important; // Ensure text is vertically centered
}
:deep(.ant-select-selection-item) {
  line-height: 30px !important; // Adjust for content inside select
}

/* Global body font (if this style is actually in this component, it should be moved to a global stylesheet) */
body {
  font-family: "Roboto", sans-serif;
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
</style>