<template>
  <div v-if="products == null" class="breadcrumb-section">
    <BreadcrumbDefault :pageTitle="'Thêm sản phẩm chi tiết'" :routes="[
      { path: '/admin/san-pham/', name: 'Quản lý sản phẩm ' },
      { path: '/admin/san-pham-chi-tiet', name: 'Thêm sản phẩm chi tiết' }
    ]" />
  </div>

  <div v-if="products != null" class="breadcrumb-section">
    <BreadcrumbDefault :pageTitle="'Thêm sản phẩm chi tiết'" :routes="[
      { path: '/admin/san-pham', name: 'Quản lý sản phẩm ' },
      { path: '/admin/san-pham-chi-tiet?id=' + products.id, name: 'Quản lý sản phẩm chi tiết' },
      { path: '/admin/them-san-pham-chi-tiet', name: 'Thêm sản phẩm chi tiết' }
    ]" />
  </div>

  <div class="form-container">
    <h2>{{ products ? 'Cập nhật' : 'Thêm' }} sản phẩm chi tiết</h2>

    <a-form ref="productForm" name="productForm" autocomplete="off" class="product-form">

      <div class="form-row-single">
        <a-form-item name="ten" :label-col="{ span: 24 }">
          <template #label>
            <span style="color: red;">* </span>
            <span> Tên sản phẩm</span>
          </template>
          <a-auto-complete v-model:value="formData.ten" :options="filteredProductNames"
            @search="handleProductNameSearch" @select="handleProductNameSelect"
            placeholder="Nhập tên sản phẩm hoặc chọn từ gợi ý" class="full-width-input" :disabled="!!products" />
        </a-form-item>

        <a-form-item label="Mô tả" name="mota" :label-col="{ span: 24 }" v-if="products == null">
          <QuillEditor theme="snow" toolbar="full" v-model:content="formData.moTa" contentType="html"
            :options="editorOptions" style="height: 200px;" />
        </a-form-item>
        <a-form-item label="Mô tả" name="mota" :label-col="{ span: 24 }" v-if="products != null">
          <QuillEditor theme="snow" toolbar="full" v-model:content="formData.moTa" contentType="html"
            :options="editorOptions" style="height: 200px;" :readOnly="!!products" />
        </a-form-item>
      </div>

      <div class="form-row">
        <a-form-item name="idDanhMuc" :label-col="{ span: 24 }">
          <template #label>
            <span style="color: red;">* </span>
            <span>Danh mục</span>
          </template>
          <div class="input-with-button">
            <a-select v-model:value="formData.idDanhMuc" :options="danhMucOptions" placeholder="Chọn danh mục"
              :disabled="!!products" />
            <a-button class="input-button" @click="handleAddLoaiGiayClick" :disabled="!!products"
              style="background-color: #54bddb; color: white;">+</a-button>
          </div>
        </a-form-item>

        <a-form-item name="idLoaiDe" :label-col="{ span: 24 }">
          <template #label>
            <span style="color: red;">* </span>
            <span>Loại đế</span>
          </template>
          <div class="input-with-button">
            <a-select v-model:value="formData.idLoaiDe" :options="loaiDeOptions" placeholder="Chọn Loại đế"
              :disabled="!!products" />
            <a-button class="input-button" @click="handleAddLoaiDeClick" :disabled="!!products"
              style="background-color: #54bddb; color: white;">+</a-button>
          </div>
        </a-form-item>
      </div>

      <div class="form-row">
        <a-form-item name="idThuongHieu" :label-col="{ span: 24 }">
          <template #label>
            <span style="color: red;">* </span>
            <span>Thương hiệu</span>
          </template>
          <div class="input-with-button">
            <a-select v-model:value="formData.idThuongHieu" :options="thuongHieuOptions" placeholder="Chọn thương hiệu"
              :disabled="!!products" />
            <a-button class="input-button" @click="handleAddXuatXuClick" :disabled="!!products"
              style="background-color: #54bddb; color: white;">+</a-button>
          </div>
        </a-form-item>

        <a-form-item name="idChatLieu" style="margin-top: 18px;" :label-col="{ span: 24 }">
          <span style="color: red;">*</span>
          <span>Chất liệu</span>
          <div class="input-with-button">
            <a-select v-model:value="formData.idChatLieu" :options="chatLieuOptions" placeholder="Chọn chất liệu"
              :disabled="!!products" />
            <a-button class="input-button" @click="handleAddChatLieuClick" :disabled="!!products"
              style="background-color: #54bddb; color: white;">+</a-button>
          </div>
        </a-form-item>
      </div>

      <div class="form-container">
        <h2>Kích cỡ và màu sắc</h2>

        <div class="form-row1">
          <div class="selected-sizes">
            <span style="color: red; font-size: 20px;">* </span>
            <label class="size-label">Kích cỡ:</label>

            <div v-for="(size, index) in selectedSizes" :key="index" class="size-box">
              <div class="color-label" style="margin-right: 10px;">{{ size.label }}</div>
              <a-button class="remove-btn" @click="removeSize(index)" shape="circle" icon="-" size="small" />

            </div>
            <a-button @click="isSizeModalVisible = true" style="background-color: #54bddb; color: white;"
              class="add-btn">+
              Chọn kích cỡ</a-button>
          </div>

          <div class="selected-colors">
            <span style="color: red; font-size: 20px;">* </span>
            <label class="size-label">Màu sắc:</label>
            <div v-for="(color, index) in selectedColors" :key="index" class="color-box">
              <div class="color-swatch" :style="{ backgroundColor: color.color }"></div>
              <div class="color-label">{{ color.label }}</div>
              <a-button class="remove-btn" @click="removeColor(index)" shape="circle" icon="-" size="small" />
            </div>
            <a-button @click="isColorModalVisible = true" style="background-color: #54bddb; color: white;"
              class="add-btn">+
              Chọn màu sắc</a-button>
          </div>
        </div>
      </div>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <a-button @click="handleSubmit" style="width: 150px; background-color: #54bddb; color: white;">
          Lưu sản phẩm
        </a-button>
      </div>

      <div v-if="selectedColors.length > 0 && selectedSizes.length > 0">
        <h4>Sản phẩm chi tiết </h4>
        <div v-if="selectedRowKeys.length > 0" class="batch-action-container">
          <div class="common-inputs">
            <a-form-item label="Số lượng chung" :label-col="{ span: 24 }">
              <a-input v-model:value="batchCommonQuantity" type="number" placeholder="Nhập số lượng chung" />
            </a-form-item>

            <a-form-item label="Giá chung" :label-col="{ span: 24 }">
              <a-input :value="formatPrice(batchCommonPrice)" @input="e => handleBatchPriceInput(e)"
                placeholder="Nhập giá chung" />
            </a-form-item>

            <a-button type="primary" style="background-color: #54bddb; color: white;" @click="applyBatchValues"
              class="apply-batch-btn">Áp dụng</a-button>
          </div>
        </div>
        <a-tabs v-model:activeKey="activeColorTab">
          <a-tab-pane v-for="color in selectedColors" :key="color.value">
            <template #tab>
              <span class="tab-title-with-error">
                Sản phẩm màu {{ color.label }}
                <ExclamationCircleOutlined v-if="hasErrorInColorTab(color.value)" class="error-icon" />
              </span>
            </template>
            <a-table :dataSource="productVariantsByColor(color.value)" :columns="columns" bordered rowKey="key"
              :row-selection="rowSelection">
              <template #bodyCell="{ column, record, index }">
                <template v-if="column.key === 'operation'">
                  <a-button danger @click="removeProductVariant(record.key)"
                    style="background-color: red; color: white;">xóa</a-button>
                </template>
                <template v-else-if="column.key === 'giaBan'">
                  <a-input :value="formatPrice(record.giaBan)" @input="e => handlePriceInput(e, record)"
                    placeholder="Nhập giá" :class="{ 'input-error-border': !record.isPriceValid }" />
                </template>
                <template v-else-if="column.key === 'soLuong'">
                  <a-input v-model:value="record.soLuong" placeholder="Nhập số lượng" type="number"
                    @blur="handleUpdateQuantity(record.key, record.soLuong)"
                    :class="{ 'input-error-border': !record.isQuantityValid }" />
                </template>
                <template v-else-if="column.key === 'upload'">
                  <a-upload :before-upload="(file) => handleImageUpload(file, record)" accept="image/*"
                    list-type="picture-card" :show-upload-list="false"
                    :class="{ 'upload-error-border': !record.isImageValid }">
                    <div v-if="!record.imagePreviewUrl">
                      <plus-outlined />
                      <div style="margin-top: 8px">Thêm ảnh</div>
                    </div>
                    <a-image v-else :src="record.imagePreviewUrl" width="100px" height="100px" />
                  </a-upload>
                </template>
              </template>
            </a-table>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-form>
  </div>

  <a-modal v-model:open="isColorModalVisible" title="Chọn màu sắc" width="400px" @cancel="handleCloseColorModal">
    <a-button class="input-button-model" @click="handleAddColorClick">+ Thêm màu sắc</a-button>
    <div class="color-picker">
      <div v-for="(color, index) in ColorOptions" :key="index" class="color-option-item"
        @click="handleSelectColor(color)"
        :class="{ 'selected-option-border': selectedColors.some(s => s.value === color.value) }">
        <div class="color-swatch" :style="{ backgroundColor: color.color }"></div>
        <div class="color-label">{{ color.label }}</div>
      </div>
    </div>
    <template #footer>
      <a-button type="primary" style="background-color: #54bddb; color: white;"
        @click="handleConfirmColorSelection">Xong</a-button>
    </template>
  </a-modal>

  <a-modal v-model:open="isSizeModalVisible" title="Chọn kích cỡ" width="400px" @cancel="handleCloseSizeModal">
    <a-button class="input-button-model" @click="handleAddSizeClick">+ Thêm kích cỡ</a-button>
    <div class="color-picker">
      <a-button v-for="(size, index) in SizeOptions" :key="index" class="size-color-btn" @click="handleSelectSize(size)"
        :class="{ 'selected-option-border': selectedSizes.some(s => s.value === size.value) }">
        {{ size.label }}
      </a-button>
    </div>
    <template #footer>
      <a-button type="primary" style="background-color: #54bddb; color: white;"
        @click="handleConfirmSizeSelection">Xong</a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch, defineProps, defineEmits, onMounted, onBeforeUnmount, computed } from 'vue';
import { toast } from 'vue3-toastify';
import type { TableColumnsType } from 'ant-design-vue';
import { router } from '@/routes/router';
// THÊM: Import ExclamationCircleOutlined
import { PlusOutlined, ExclamationCircleOutlined } from '@ant-design/icons-vue';
import BreadcrumbDefault from '@/components/ui/Breadcrumbs/BreadcrumbDefault.vue';
import { GetListChatLieu, GetListDanhMuc, GetListLoaiDe, GetListThuongHieu, GetListXuatXu } from '@/services/api/admin/sanpham.api';
// Import all necessary APIs
import {
  modifySanPham,
  GetListColor,
  GetListSize,
  GetListSP, // For AutoComplete
  type SanPhamResponse,
  type OptionItem,
  type ProductBaseInfo, // For AutoComplete
} from '@/services/api/admin/sanphamchitiet.api';
import { QuillEditor } from '@vueup/vue-quill';
import '@vueup/vue-quill/dist/vue-quill.snow.css';

// --- PROPS ---
const props = defineProps<{
  products: SanPhamResponse | null;
}>();
const editorOptions = {
  modules: {
    toolbar: [
      [{ header: [1, 2, 3, false] }],
      ['bold', 'italic', 'underline', 'strike'],
      [{ 'script': 'sub' }, { 'script': 'super' }],
      [{ 'color': [] }, { 'background': [] }],
      ['link', 'image'],
      [{ 'list': 'ordered' }, { 'list': 'bullet' }],
      [{ 'align': [] }],
      ['clean']
    ]
  },
};

const handleUpdateQuantity = (key: string, newQuantity: string) => {
  const variant = productVariants.value.find(v => v.key === key);
  if (variant) {
    const numQuantity = Number(newQuantity);
    variant.soLuong = isNaN(numQuantity) ? null : numQuantity;
    // CẬP NHẬT: Kiểm tra tính hợp lệ khi người dùng nhập/rời khỏi input
    variant.isQuantityValid = variant.soLuong !== null && variant.soLuong > 0;
  }
};

// --- EMITS ---
const emit = defineEmits(['addCL', 'addXX', 'addLD', 'addLG', 'addColor', 'addSize']);

// --- LOCAL FORM DATA STATE (Unified for Add/Edit) ---
interface FormDataProduct {
  id?: string;
  idSanPhamGoc?: string;
  ten: string;
  moTa: string;
  idThuongHieu: string;
  idLoaiDe: string;
  idXuatXu: string;
  idDanhMuc: string;
  idChatLieu: string;
}

const formData = ref<FormDataProduct>({
  ten: '',
  moTa: '',
  idThuongHieu: '',
  idLoaiDe: '',
  idXuatXu: '',
  idDanhMuc: '',
  idChatLieu: '',
  idSanPhamGoc: undefined,
});

watch(() => props.products, (newProducts) => {
  if (newProducts) {
    formData.value = {
      id: newProducts.id,
      ten: newProducts.ten,
      moTa: newProducts.moTa,
      idThuongHieu: newProducts.idThuongHieu,
      idLoaiDe: newProducts.idLoaiDe,
      idXuatXu: newProducts.idXuatXu || '',
      idDanhMuc: newProducts.idDanhMuc,
      idChatLieu: newProducts.idChatLieu,
      idSanPhamGoc: newProducts.id,
    };
  } else {
    formData.value = {
      ten: '',
      moTa: '',
      idThuongHieu: '',
      idLoaiDe: '',
      idXuatXu: '',
      idDanhMuc: '',
      idChatLieu: '',
      idSanPhamGoc: undefined,
    };
  }
}, { immediate: true, deep: true });


// --- DROPDOWN OPTIONS ---
const thuongHieuOptions = ref<{ label: string; value: string }[]>([]);
const loaiDeOptions = ref<{ label: string; value: string }[]>([]);
const danhMucOptions = ref<{ label: string; value: string }[]>([]);
const chatLieuOptions = ref<{ label: string; value: string }[]>([]);
const SizeOptions = ref<{ label: string; value: string }[]>([]);
const ColorOptions = ref<{ label: string; value: string; color: string }[]>([]);


// --- AUTOCOMPLETE STATE ---
const allProductNames = ref<{ label: string; value: string }[]>([]);
const filteredProductNames = ref<{ label: string; value: string }[]>([]);

// --- AUTOCOMPLETE HANDLERS ---
const fetchAllProductNames = async () => {
  try {
    const response = await GetListSP();
    allProductNames.value = response.data.map((item: ProductBaseInfo) => ({
      label: item.ten,
      value: item.id,
    }));
    console.log("Fetched product names for autocomplete:", allProductNames.value);
  } catch (error) {
    console.error('Lỗi khi lấy danh sách tên sản phẩm cho AutoComplete:', error);
  }
};

const handleProductNameSearch = (searchText: string) => {
  formData.value.idSanPhamGoc = undefined;
  if (!searchText) {
    filteredProductNames.value = [];
  } else {
    filteredProductNames.value = allProductNames.value.filter(item =>
      item.label.toLowerCase().includes(searchText.toLowerCase())
    );
  }
};

const handleProductNameSelect = (value: string, option: { label: string; value: string }) => {
  formData.value.ten = option.label;
  formData.value.idSanPhamGoc = option.value;
  console.log('Selected product name (label):', option.label);
  console.log('Selected product ID (value):', option.value);
};


// --- MODAL STATE ---
const isColorModalVisible = ref(false);
const isSizeModalVisible = ref(false);

// --- SELECTED COLORS & SIZES ---
const selectedColors = ref<any[]>([]);
const selectedSizes = ref<any[]>([]);

// --- PRODUCT VARIANTS TABLE STATE ---
// CẬP NHẬT: Thêm các trường isQuantityValid, isPriceValid, isImageValid
interface ProductVariant {
  idColor: string;
  idSize: string;
  key: string;
  ten: string;
  soLuong: number | null;
  giaBan: number | null;
  imageFile: File | null;
  imagePreviewUrl: string | null;
  isQuantityValid?: boolean;
  isPriceValid?: boolean;
  isImageValid?: boolean;
}
const productVariants = ref<ProductVariant[]>([]);
const activeColorTab = ref<string>('');
const selectedRowKeys = ref<string[]>([]);
const batchCommonQuantity = ref<number | null>(null);
const batchCommonPrice = ref<number | null>(null);


// --- LIFECYCLE HOOKS ---
let interval: number | null = null;

onMounted(() => {
  fetchAllProductNames();
  fetchDanhMuc();
  fetchThuongHieu();
  fetchLoaiDe();
  fetchChatLieu();
  fetchColor();
  fetchSize();

  interval = setInterval(() => {
    fetchDanhMuc();
    fetchThuongHieu();
    fetchChatLieu();
    fetchLoaiDe();
    fetchColor();
    fetchSize();
  }, 5000);
});

onBeforeUnmount(() => {
  if (interval !== null) {
    clearInterval(interval);
  }
});


// --- FETCH DROPDOWN DATA FUNCTIONS (unchanged) ---
const fetchThuongHieu = async () => {
  try {
    const response = await GetListThuongHieu();
    thuongHieuOptions.value = response.data.map(item => ({ label: item.ten, value: item.id }));
  } catch (error) { console.error('Lỗi khi lấy danh sách thương hiệu:', error); }
};
const fetchChatLieu = async () => {
  try {
    const response = await GetListChatLieu();
    chatLieuOptions.value = response.data.map(item => ({ label: item.ten, value: item.id }));
  } catch (error) { console.error('Lỗi khi lấy danh sách chất liệu:', error); }
};
const fetchDanhMuc = async () => {
  try {
    const response = await GetListDanhMuc();
    danhMucOptions.value = response.data.map(item => ({ label: item.ten, value: item.id }));
  } catch (error) { console.error('Lỗi khi lấy danh sách danh mục:', error); }
};
const fetchLoaiDe = async () => {
  try {
    const response = await GetListLoaiDe();
    loaiDeOptions.value = response.data.map(item => ({ label: item.ten, value: item.id }));
  } catch (error) { console.error('Lỗi khi lấy danh sách loại đế:', error); }
};
const fetchColor = async () => {
  try {
    const response = await GetListColor();
    ColorOptions.value = response.data.map(item => ({ label: item.ten, value: item.id, color: item.mau || '#FFFFFF' }));
  } catch (error) { console.error('Lỗi khi lấy danh sách màu:', error); }
};
const fetchSize = async () => {
  try {
    const response = await GetListSize();
    SizeOptions.value = response.data.map(item => ({ label: item.ten, value: item.id }));
  } catch (error) { console.error('Lỗi khi lấy danh sách kích cỡ:', error); }
};


// --- MODAL & SELECTION HANDLERS (unchanged) ---
const handleConfirmColorSelection = () => {
  isColorModalVisible.value = false;
  generateProductVariants();
};
const handleConfirmSizeSelection = () => {
  isSizeModalVisible.value = false;
  generateProductVariants();
};
const handleSelectColor = (color: any) => {
  const index = selectedColors.value.findIndex(item => item.value === color.value);
  if (index > -1) {
    selectedColors.value.splice(index, 1);
  } else {
    selectedColors.value.push(color);
  }
};
const handleSelectSize = (size: any) => {
  const index = selectedSizes.value.findIndex(item => item.value === size.value);
  if (index > -1) {
    selectedSizes.value.splice(index, 1);
  } else {
    selectedSizes.value.push(size);
  }
};
const removeColor = (index: number) => {
  const removedColor = selectedColors.value[index];
  selectedColors.value.splice(index, 1);
  productVariants.value = productVariants.value.filter(v => v.idColor !== removedColor.value);
  generateProductVariants();
};
const removeSize = (index: number) => {
  const removedSize = selectedSizes.value[index];
  selectedSizes.value.splice(index, 1);
  productVariants.value = productVariants.value.filter(v => v.idSize !== removedSize.value);
  generateProductVariants();
};
const handleCloseColorModal = () => { isColorModalVisible.value = false; };
const handleCloseSizeModal = () => { isSizeModalVisible.value = false; };


// --- PRODUCT VARIANTS GENERATION & MANAGEMENT (updated for validation fields) ---
watch([selectedColors, selectedSizes], () => {
  generateProductVariants();
}, { deep: true });

const generateProductVariants = () => {
  const newVariants: ProductVariant[] = [];
  selectedColors.value.forEach(color => {
    selectedSizes.value.forEach(size => {
      const variantKey = `${color.value}-${size.value}`;
      const existingVariant = productVariants.value.find(v => v.key === variantKey);

      if (existingVariant) {
        newVariants.push(existingVariant);
      } else {
        newVariants.push({
          idColor: color.value,
          idSize: size.value,
          key: variantKey,
          ten: `${formData.value.ten} [${color.label}-${size.label}]`,
          soLuong: 1, // Đặt mặc định là null để bắt lỗi nếu chưa nhập
          giaBan: 10000,  // Đặt mặc định là null để bắt lỗi nếu chưa nhập
          imageFile: null,
          imagePreviewUrl: null,
          isQuantityValid: true, // THÊM: Mặc định là KHÔNG hợp lệ khi tạo mới
          isPriceValid: true,    // THÊM: Mặc định là KHÔNG hợp lệ khi tạo mới
          isImageValid: false,    // THÊM: Mặc định là KHÔNG hợp lệ khi tạo mới
        });
      }
    });
  });
  productVariants.value = newVariants;

  if (selectedColors.value.length > 0 && (!activeColorTab.value || !selectedColors.value.some(c => c.value === activeColorTab.value))) {
    activeColorTab.value = selectedColors.value[0].value;
  } else if (selectedColors.value.length === 0) {
    activeColorTab.value = '';
  }
};

const productVariantsByColor = computed(() => (colorId: string) => {
  return productVariants.value.filter(variant => variant.idColor === colorId);
});

// THÊM: Computed property để kiểm tra lỗi trong từng tab màu
// THÊM: Computed property để kiểm tra lỗi trong từng tab màu
const hasErrorInColorTab = computed(() => (colorId: string) => {
  const variantsInColor = productVariants.value.filter(variant => variant.idColor === colorId);
  return variantsInColor.some(variant =>
    !variant.isQuantityValid || !variant.isPriceValid || !variant.isImageValid
  );
});

const removeProductVariant = (keyToRemove: string) => {
  productVariants.value = productVariants.value.filter(variant => variant.key !== keyToRemove);
};


const handleImageUpload = (file: File, record: ProductVariant) => {
  const reader = new FileReader();
  reader.onload = (e) => {
    const newImagePreviewUrl = e.target?.result as string;
    const colorIdToUpdate = record.idColor;

    // Cập nhật tất cả các biến thể có cùng màu
    productVariants.value.forEach(variant => {
      if (variant.idColor === colorIdToUpdate) {
        variant.imagePreviewUrl = newImagePreviewUrl;
        variant.imageFile = file;
        variant.isImageValid = true; // CẬP NHẬT: Đánh dấu là hợp lệ khi ảnh được tải lên
      }
    });
  };
  reader.readAsDataURL(file);
  return false;
};


// --- PRICE & QUANTITY HANDLERS (updated for validation) ---
const formatPrice = (value: number | string | null): string => {
  if (value === null || value === undefined || value === '') { return ''; }
  const numValue = typeof value === 'string' ? parseFloat(value.replace(/[^0-9.]/g, '')) : value;
  if (isNaN(numValue)) { return ''; }
  return numValue.toLocaleString('vi-VN');
};

const handleBatchPriceInput = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const rawValue = target.value.replace(/[^0-9]/g, '');
  batchCommonPrice.value = rawValue === '' ? null : Number(rawValue);
  target.value = formatPrice(batchCommonPrice.value);
};

const handlePriceInput = (event: Event, record: ProductVariant) => {
  const target = event.target as HTMLInputElement;
  const rawValue = target.value.replace(/[^0-9]/g, '');
  const numPrice = Number(rawValue);
  record.giaBan = isNaN(numPrice) ? null : numPrice;
  target.value = formatPrice(record.giaBan);
  // CẬP NHẬT: Kiểm tra tính hợp lệ khi người dùng nhập/rời khỏi input
  record.isPriceValid = record.giaBan !== null && record.giaBan > 0;
};

const applyBatchValues = () => {
  if (selectedRowKeys.value.length === 0) {
    toast.warning("Vui lòng chọn ít nhất một sản phẩm để áp dụng.");
    return;
  }
  if (batchCommonPrice.value === null && batchCommonQuantity.value === null) {
    toast.warning("Vui lòng nhập giá chung hoặc số lượng chung.");
    return;
  }

  productVariants.value.forEach(variant => {
    if (selectedRowKeys.value.includes(variant.key)) {
      if (batchCommonPrice.value !== null) {
        variant.giaBan = batchCommonPrice.value;
        variant.isPriceValid = true; // CẬP NHẬT: Đặt là hợp lệ nếu đã áp dụng giá
      }
      if (batchCommonQuantity.value !== null) {
        variant.soLuong = batchCommonQuantity.value;
        variant.isQuantityValid = true; // CẬP NHẬT: Đặt là hợp lệ nếu đã áp dụng số lượng
      }
    }
  });
  toast.success(`Đã áp dụng giá và số lượng cho ${selectedRowKeys.value.length} sản phẩm đã chọn.`);
  selectedRowKeys.value = [];
  batchCommonPrice.value = null;
  batchCommonQuantity.value = null;
};


// --- TABLE COLUMN DEFINITION (unchanged) ---
const columns: TableColumnsType = [
  { title: 'STT', key: 'stt', dataIndex: 'stt', width: 60, customRender: ({ index }) => index + 1 },
  { title: 'Tên sản phẩm', key: 'ten', dataIndex: 'ten' },
  { title: 'Kích cỡ', key: 'size', customRender: ({ record }) => selectedSizes.value.find(s => s.value === record.idSize)?.label },
  { title: 'Số lượng', key: 'soLuong', dataIndex: 'soLuong' },
  { title: 'Giá bán', key: 'giaBan', dataIndex: 'giaBan' },
  { title: 'Hành động', key: 'operation' },
  { title: 'Ảnh', key: 'upload' },
];

const onSelectChange = (keys: string[]) => {
  selectedRowKeys.value = keys;
};

const rowSelection = computed(() => {
  return {
    selectedRowKeys: selectedRowKeys.value,
    onChange: onSelectChange,
  };
});


// --- EMIT HANDLERS (for + buttons next to selects - unchanged) ---
const handleAddChatLieuClick = () => { emit('addCL'); };
const handleAddXuatXuClick = () => { emit('addXX'); };
const handleAddLoaiGiayClick = () => { emit('addLG'); };
const handleAddLoaiDeClick = () => { emit('addLD'); };
const handleAddSizeClick = () => { emit('addSize'); };
const handleAddColorClick = () => { emit('addColor'); };


// --- SUBMIT LOGIC (updated for validation) ---
const check = ref(0);
import DOMPurify from 'dompurify';

const cleanDescription = (html: string) => {
  const cleaned = DOMPurify.sanitize(html, {
    ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'u', 'ol', 'ul', 'li', 'a'],
    KEEP_CONTENT: true,
  });

  return cleaned
    // Xóa <p><br> hoặc <p><br/> 
    .replace(/<p>\s*<br\s*\/?>\s*<\/p>/gi, '') 
    // Xóa <p></p> rỗng
    .replace(/<p>\s*<\/p>/gi, '')              
    // Xóa <p> mở đơn lẻ
    .replace(/<p>/gi, '')                      
    // Xóa </p> đóng đơn lẻ
    .replace(/<\/p>/gi, '')                    
    .trim();
};


const handleSubmit = async () => {

  const descriptionContent = formData.value.moTa;
  console.log("Nội dung mô tả từ editor trước khi làm sạch:", descriptionContent);

  // Làm sạch mô tả trước khi kiểm tra và gửi
  const cleanedDescription = cleanDescription(descriptionContent);
  try {
    // --- Frontend Validations ---
    if (!formData.value.ten || formData.value.ten.trim() === '') {
      toast.warning("Vui lòng nhập tên sản phẩm.");
      return;
    }
    // CẬP NHẬT: Kiểm tra nội dung rỗng của Quill
    if (!formData.value.idDanhMuc || formData.value.idDanhMuc.trim() === '') {
      toast.warning("Vui lòng chọn danh mục.");
      return;
    }
    if (!formData.value.idLoaiDe || formData.value.idLoaiDe.trim() === '') {
      toast.warning("Vui lòng chọn loại đế.");
      return;
    }
    if (!formData.value.idThuongHieu || formData.value.idThuongHieu.trim() === '') {
      toast.warning("Vui lòng chọn thương hiệu.");
      return;
    }
    if (!formData.value.idChatLieu || formData.value.idChatLieu.trim() === '') {
      toast.warning("Vui lòng chọn chất liệu.");
      return;
    }
    if (selectedColors.value.length === 0) {
      toast.warning("Vui lòng chọn ít nhất một màu sắc.");
      return;
    }
    if (selectedSizes.value.length === 0) {
      toast.warning("Vui lòng chọn ít nhất một kích cỡ.");
      return;
    }
    if (productVariants.value.length === 0) {
      toast.warning("Vui lòng chọn ít nhất một biến thể sản phẩm.");
      return;
    }

    // CẬP NHẬT: Kiểm tra và cập nhật trạng thái lỗi cho từng biến thể
    let hasValidationError = false;
    for (const item of productVariants.value) {
      const isPriceOk = item.giaBan !== null && !isNaN(Number(item.giaBan)) && Number(item.giaBan) > 0;
      item.isPriceValid = isPriceOk;
      if (!isPriceOk) {
        toast.warning(`Giá bán không hợp lệ cho biến thể ${item.ten}`);
        hasValidationError = true;
      }

      const isQuantityOk = item.soLuong !== null && !isNaN(Number(item.soLuong)) && Number(item.soLuong) > 0;
      item.isQuantityValid = isQuantityOk;
      if (!isQuantityOk) {
        toast.warning(`Số lượng không hợp lệ cho biến thể ${item.ten}`);
        hasValidationError = true;
      }

      const isImageOk = !!item.imageFile;
      item.isImageValid = isImageOk;
      if (!isImageOk) {
        toast.warning(`Vui lòng tải ảnh cho biến thể ${item.ten}`);
        hasValidationError = true;
      }
    }

    if (hasValidationError) {
      return; // Dừng nếu có bất kỳ lỗi nào
    }

    // --- Prepare and Send Data ---
    check.value = 0;
    for (const item of productVariants.value) {
      const formToSend = new FormData();

      if (formData.value.idSanPhamGoc) {
        formToSend.append('idSP', formData.value.idSanPhamGoc);
      }

      if (!formData.value.idSanPhamGoc) {
        formToSend.append('ten', formData.value.ten.trim());
        formToSend.append('moTa', cleanedDescription.trim());
        formToSend.append('idThuongHieu', formData.value.idThuongHieu.trim());
        formToSend.append('idChatLieu', formData.value.idChatLieu.trim());
        formToSend.append('idLoaiDe', formData.value.idLoaiDe.trim());
        formToSend.append('idXuatXu', formData.value.idXuatXu?.trim() || '');
        formToSend.append('idDanhMuc', formData.value.idDanhMuc.trim());
      }

      if (props.products) {
        formToSend.append('idSP', props.products.id);
      }

      // Variant-specific details
      formToSend.append('soLuong', item.soLuong!.toString());
      formToSend.append('giaBan', item.giaBan!.toString());
      formToSend.append('idMau', item.idColor);
      formToSend.append('idSize', item.idSize);
      formToSend.append('check', check.value.toString());

      if (item.imageFile) {
        formToSend.append('anh', item.imageFile);
      }

      const res = await modifySanPham(formToSend);
      check.value += 1;

      if (res.message == 'Sản phẩm này đã tồn tại') {
        toast.error("Tên sản phẩm bạn nhập đã tồn tại. Vui lòng chọn từ gợi ý hoặc nhập tên khác.");
        return;
      }
      
      if (res.message.includes('đã tồn tại sẽ không được thêm mới')) {
        toast.error(res.message);
        return;
      }

    }

    // --- Success Handling ---
    if (props.products) {
      toast.success("Cập nhật sản phẩm chi tiết thành công");
      router.push({
        name: 'san-pham-chi-tiet-admin',
        query: { id: props.products.id },
      });
    } else {
      toast.success("Thêm sản phẩm chi tiết thành công");
      router.push({ name: 'san-pham-admin' });
    }

  } catch (error: any) {
    console.error(error);
    toast.error(error?.response?.data?.message || "Đã xảy ra lỗi khi thêm/cập nhật sản phẩm.");
  }
};
</script>

<style scoped lang="scss">
.form-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-top: 20px;
}

h2 {
  color: #333;
  font-size: 24px;
  margin-bottom: 20px;
  text-align: center;
}

h4 {
  margin-top: 30px;
  margin-bottom: 15px;
  color: #333;
  font-size: 18px;
}

.product-form {
  .form-row-single {
    display: flex;
    flex-direction: column;
    gap: 15px;
    margin-bottom: 20px;

    .ant-form-item {
      margin-bottom: 0; // Remove default Ant Design margin
    }
  }

  .form-row {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;

    .ant-form-item {
      flex: 1;
      margin-bottom: 0; // Remove default Ant Design margin
    }
  }

  .input-with-button {
    display: flex;
    align-items: center;

    .ant-select,
    .ant-input {
      flex: 1;
    }

    .input-button {
      margin-left: 8px;
      // Adjust button height if needed to match select/input
      height: 32px;
      min-width: 32px; // Ensure it's a circle or square
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0; // Remove default padding for +/- buttons
    }
  }
}

.full-width-input {
  width: 100%; // Make AutoComplete take full width of its container
}

.selected-sizes,
.selected-colors {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap; // Allow items to wrap
  gap: 10px; // Space between items

  .size-label {
    font-weight: bold;
    margin-right: 10px;
  }

  .size-box,
  .color-box {
    display: flex;
    align-items: center;
    background-color: #f0f0f0;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    padding: 5px 10px;
    font-size: 14px;
    position: relative; // For remove button positioning

    .remove-btn {
      border-radius: 50%;
      margin-right: 8px;
      font-size: 12px;
      line-height: 1; // Ensure text alignment
      padding: 0;
      width: 20px;
      height: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5222d;
      color: white;
      border: none;
    }
  }

  .color-box {
    .color-swatch {
      width: 20px;
      height: 20px;
      border-radius: 50%;
      border: 1px solid #d9d9d9;
      margin-right: 8px;
    }

    .color-label {
      margin-right: 8px;
    }
  }

  .add-btn {
    // Add button styling
    height: 32px; // Match Ant Design input height
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 15px; // Standard Ant Design button padding
  }
}

.color-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 10px 0;

  .color-option-item,
  .size-color-btn {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: #54bddb;
    }

    .color-swatch {
      width: 24px;
      height: 24px;
      border-radius: 50%;
      border: 1px solid #eee;
      margin-right: 8px;
    }
  }

  .selected-option-border {
    border-color: #54bddb;
    box-shadow: 0 0 0 2px rgba(84, 189, 219, 0.2);
  }

  .size-color-btn {
    // Specific styling for size buttons in modal
    min-width: 60px; // Ensure button has some width
    justify-content: center;
  }
}

.input-button-model {
  margin-bottom: 15px;
  background-color: #54bddb;
  color: white;
}

.batch-action-container {
  background-color: #f9f9f9;
  border: 1px solid #e8e8e8;
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 4px;

  .common-inputs {
    display: flex;
    gap: 15px;
    align-items: flex-end; // Align inputs and button at the bottom

    .ant-form-item {
      flex: 1;
      margin-bottom: 0;
    }

    .apply-batch-btn {
      height: 32px; // Match input heights
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 15px;
    }
  }
}

.page-container {
  padding: 20px;
}

.breadcrumb-section {
  margin-bottom: 25px;
  background-color: #fff;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.section-title {
  margin-top: 30px;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  margin-left: 0px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

body {
  font-family: 'Roboto', sans-serif;
}

.input-error-border.ant-input {
  border-color: red !important;
  box-shadow: 0 0 0 2px rgba(255, 0, 0, 0.2) !important;
}

/* Style cho viền lỗi của upload */
.upload-error-border .ant-upload.ant-upload-select-picture-card {
  border-color: red !important;
  box-shadow: 0 0 0 2px rgba(255, 0, 0, 0.2) !important;
}

/* Style cho icon lỗi trên tab */
.error-icon {
  color: red;
  margin-left: 5px;
  font-size: 16px;
  /* Điều chỉnh kích thước nếu cần */
}

.tab-title-with-error {
  display: flex;
  align-items: center;
}

:deep(.ant-input:hover),
:deep(.ant-input:focus),
:deep(.ant-input-focused) {
  border-color: #58bddb !important;
  /* Màu xanh đậm hơn */
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
  /* Đổ bóng màu xanh đậm */
}

/* Style cho a-input-number khi hover, focus, và focused */
:deep(.ant-input-number:hover),
:deep(.ant-input-number:focus),
:deep(.ant-input-number-focused) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

/* Bỏ background cho row đã chọn */
:deep(.ant-table-wrapper .ant-table-tbody > tr.ant-table-row-selected > td) {
  background: transparent !important;
}


/* Style cho a-date-picker khi hover, focus, và focused */
:deep(.ant-picker:hover),
:deep(.ant-picker-focused),
/* Khi DatePicker đã mở và focus vào */
:deep(.ant-picker-focused .ant-picker-input > input),
/* Đảm bảo input bên trong cũng bị ảnh hưởng */
:deep(.ant-picker:focus-within)

/* Dùng cho các component phức tạp có nhiều phần tử con */
  {
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
  /* Màu xanh đậm hơn */
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
  /* Đổ bóng màu xanh đậm */
}

/* Đảm bảo hiệu ứng khi focus hoặc mở dropdown */
:deep(.ant-select-focused .ant-select-selector),
:deep(.ant-select-open .ant-select-selector) {
  border-color: #58bddb !important;
  /* Màu xanh đậm hơn */
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
  /* Đổ bóng màu xanh đậm */
}

/* Style cho option khi hover trong dropdown */
:deep(.ant-select-item-option-active) {
  background-color: #e6f7ff !important;
  /* Nền xanh nhạt khi hover trên option */
}

/* Đảm bảo các combobox trong form không bị ảnh hưởng bởi style khác */
:deep(.ant-select-selector) {
  border-radius: 4px !important;
  /* Giữ đồng nhất với các input */
}

/* Style cho a-button khi hover */
:deep(.ant-btn:hover),
:deep(.ant-btn:focus) {
  background-color: #4aa8c6 !important;
  /* Màu xanh đậm hơn khi hover */
  border-color: #4aa8c6 !important;
  color: white !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
  /* Đổ bóng xanh */
}

/* Style cho a-radio khi hover */
:deep(.ant-radio-wrapper:hover .ant-radio-inner) {
  border-color: #58bddb !important;
  /* Viền xanh khi hover */
}

:deep(.ant-radio-wrapper:hover .ant-radio .ant-radio-inner::after) {
  background-color: #58bddb !important;
  /* Điểm chọn xanh khi hover */
}
</style>