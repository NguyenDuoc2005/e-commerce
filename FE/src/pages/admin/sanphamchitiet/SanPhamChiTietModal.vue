<template>
  <a-modal :open="open" :title="props.title" width="1300px" @cancel="closeModal">
    <template #footer>
      <a-popconfirm title="Bạn có chắc chắn muốn lưu thay đổi?" @confirm="handleSubmit" ok-text="Đồng ý"
        cancel-text="Huỷ">
        <a-button style="background-color: #54bddb;" type="primary">Xác nhận</a-button>
      </a-popconfirm>
      <a-button style="background-color: #54bddb; color: white;" @click="closeModal">Huỷ</a-button>
    </template>

    <a-form :model="product" ref="productForm" name="productForm" autocomplete="off">
      <div class="form-row">
        <a-row :gutter="12">
          <a-col :span="6">
            <a-form-item label="Tên sản phẩm" name="facilityId" :label-col="{ span: 24 }">
              <div class="input-with-button">
                <a-input placeholder="Nhập tên sản phẩm" v-model:value="product.ten" :disabled="true" />
              </div>
            </a-form-item>
          </a-col>

          <a-col :span="6">
            <a-form-item label="Danh mục" name="facilityId" :label-col="{ span: 24 }">
              <div class="input-with-button">
                <a-input placeholder="Chọn danh mục" v-model:value="product.tenDanhMuc" :disabled="true" />
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="Chất liệu" name="facilityId" :label-col="{ span: 24 }">
              <div class="input-with-button">
                <a-input placeholder="Nhập tên sản phẩm" v-model:value="product.tenChatLieu" :disabled="true" />
              </div>
            </a-form-item>
          </a-col>

          <a-col :span="6">
            <a-form-item label="Thương hiệu" name="facilityId" :label-col="{ span: 24 }">
              <div class="input-with-button">
                <a-input placeholder="Nhập tên sản phẩm" v-model:value="product.tenThuongHieu" :disabled="true" />
              </div>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="6">
            <a-form-item label="Loại đế" name="facilityId" :label-col="{ span: 24 }">
              <div class="input-with-button">
                <a-input placeholder="Nhập tên sản phẩm" v-model:value="product.tenLoaiDe" :disabled="true" />
              </div>
            </a-form-item>
          </a-col>

          <a-col :span="6">
            <a-form-item label="Màu sắc" name="facilityId" :label-col="{ span: 24 }">
              <div class="input-with-button">
                <a-select v-model:value="product.idMauSac" placeholder="Chọn Màu Sắc">
                  <a-select-option v-for="option in mauSacOptions" :key="option.value" :value="option.value">
                    <template #default>
                      <div :style="{ display: 'flex', alignItems: 'center' }">
                        <div :style="{
                          width: '20px',
                          height: '20px',
                          borderRadius: '50%',
                          backgroundColor: option.color,
                          marginRight: '10px'
                        }"></div>
                        <span>{{ option.label }}</span>
                      </div>
                    </template>
                  </a-select-option>
                </a-select>
              </div>
            </a-form-item>
          </a-col>

          <a-col :span="6">
            <a-form-item label="Kích cỡ" name="facilityId" :label-col="{ span: 24 }">
              <div class="input-with-button">
                <a-select v-model:value="product.idKichThuoc" :options="kichThuocOptions" placeholder="Chọn Kích cỡ" />
              </div>
            </a-form-item>
          </a-col>


          <a-col :span="6">
            <a-form-item label="Giá bán" name="facilityId" :label-col="{ span: 24 }">
              <div class="input-with-button">
                <a-input-number v-model:value="product.giaBan" :min="0" style="width: 100%;"
                  :formatter="value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                  :parser="value => value.replace(/\s?đ|(,*)/g, '')" />
              </div>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">



          <a-col :span="6">
            <a-form-item label="Số lượng" name="facilityId" :label-col="{ span: 24 }">
              <div class="input-with-button">
                <a-input-number placeholder="Nhập số lượng" :min="0"  v-model:value="product.soLuong" style="width: 100%;" />
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="6"></a-col>
        </a-row>

        <a-row gutter={24}>
          <a-col :span="24">
            <a-form-item label="Mô tả" name="moTa" :label-col="{ span: 24 }">
              <div class="input-with-button">
                <a-textarea placeholder="Nhập mô tả sản phẩm" v-model:value="product.moTa" :disabled="true" />
              </div>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row gutter={24}>
          <a-col :span="6" style="margin-right: 20px;"> <a-form-item label="Ảnh sản phẩm" name="productImage"
              :label-col="{ span: 24 }">
              <div class="product-image-upload-wrapper">
                <img v-if="product.anh || previewImage" :src="previewImage || product.anh" alt="Product Image"
                  class="product-image-preview" />
                <div v-else class="image-placeholder">
                  Không có ảnh
                </div>
                <input type="file" @change="handleImageUpload" accept="image/*" class="file-input"
                  style="display: none;" />
                <a-button @click="triggerFileInput" class="upload-button">Chọn ảnh</a-button>
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <div>
              <a-form-item label="Mã QR sản phẩm" :label-col="{ span: 24 }">
                <div style="margin-top: 10px; text-align: center;">
                  <canvas ref="qrCanvas"></canvas>
                </div>
                <button @click="downloadQr" class="download-button">
                  Download QR
                </button>
              </a-form-item>
            </div>
          </a-col>
          <a-col :span="12">
          </a-col>
        </a-row>

      </div>
    </a-form>
  </a-modal>



</template>

<script setup lang="ts">
// (Không thay đổi so với câu trả lời trước)
import { ref, watch, defineProps, defineEmits, onMounted } from 'vue';
import { getSanPham, type SanPhamResponse, modifySanPham, GetListThuongHieu, updateSanPham } from '@/services/api/admin/sanpham.api';
import { toast } from 'vue3-toastify';
import { getDetailSanPham, GetListColor, GetListSize } from '@/services/api/admin/sanphamchitiet.api';
import QRCode from 'qrcode';

// Khai báo props và emits
const props = defineProps<{ open: boolean; productId: string | null; title: string; idSP: string }>();
const emit = defineEmits(['close', 'success']);

// Refs cho dữ liệu và options
const textToEncode = ref('Hello Vue!');
const thuongHieuOptions = ref<{ label: string; value: string }[]>([]);
const mauSacOptions = ref<{ label: string; value: string; color: string }[]>([]);
const kichThuocOptions = ref<{ label: string; value: string }[]>([]);
const qrCanvas = ref<HTMLCanvasElement | null>(null); // Ref cho phần tử canvas QR

// START: Thêm refs cho quản lý ảnh
const selectedFile = ref<File | null>(null); // Để lưu trữ file ảnh được chọn từ input
const previewImage = ref<string | null>(null); // Để lưu trữ URL xem trước của ảnh mới

interface SanPhamResponse {
  id: string;
  ten: string;
  code: string;
  moTa: string;
  idThuongHieu: string;
  anh?: string; // URL ảnh sản phẩm
  tenDanhMuc?: string;
  tenXuatXu?: string;
  tenLoaiDe?: string;
  tenChatLieu?: string;
  giaBan?: number;
  idMauSac?: string;
  idKichThuoc?: string;
  soLuong?: string;
  // Thêm các trường khác từ API response nếu có
}

const product = ref<SanPhamResponse>({
  id: '',
  ten: '',
  code: '',
  moTa: '',
  idThuongHieu: '',
  anh: '', // Khởi tạo thuộc tính ảnh
  giaBan: 0,
  soLuong: '',
});

// Ref cho form instance của Ant Design Vue
const productForm = ref();

// Watcher để tạo QR code khi product.id thay đổi
watch(
  () => product.value.id,
  (newId) => {
    if (qrCanvas.value && newId) { // Đảm bảo canvas và ID tồn tại
      QRCode.toCanvas(qrCanvas.value, newId, { width: 150, margin: 2 }, (err) => { // Điều chỉnh width và margin cho QR dễ nhìn hơn
        if (err) console.error(err);
      });
    }
  },
  { immediate: true } // Chạy ngay khi component mounted
);

// Watcher để reset previewImage và selectedFile khi modal đóng hoặc sản phẩm thay đổi
watch(
  () => [props.productId, props.open],
  ([newId, isOpen]) => {
    if (!isOpen) { // Nếu modal đóng
      previewImage.value = null; // Reset previewImage
      selectedFile.value = null; // Reset selectedFile
    } else { // Nếu modal mở
      if (productForm.value) {
        productForm.value.resetFields();
      }
      if (newId) {
        fetchProductDetails(newId as string); // Fetch chi tiết sản phẩm nếu có ID
      } else {
        // Reset product và tạo mã mới nếu không có ID sản phẩm (thêm mới)
        product.value = {
          id: '',
          ten: '',
          code: generateCode(),
          moTa: '',
          idThuongHieu: '',
          anh: '',
          giaBan: 0,
          soLuong: '',
        };
        previewImage.value = null;
        selectedFile.value = null;
      }
    }
  },
  { immediate: true }
);

// Hàm tải xuống QR code
const downloadQr = () => {
  if (!qrCanvas.value) return;

  const link = document.createElement('a');
  link.href = qrCanvas.value.toDataURL('image/png');
  link.download = `qr-code-${product.value.id || 'sanpham'}.png`; // Tên file có ID sản phẩm
  link.click();
};

// Hàm lấy danh sách Thương hiệu
const fetchThuongHieu = async () => {
  try {
    const response = await GetListThuongHieu();
    thuongHieuOptions.value = response.data.map((thuongHieu: { ten: string; id: string; }) => ({
      label: thuongHieu.ten,
      value: thuongHieu.id,
    }));
  } catch (error) {
    console.error('Lỗi khi lấy danh sách thương hiệu:', error);
  }
};

// Hàm lấy danh sách Kích thước
const fetchKichThuoc = async () => {
  try {
    const response = await GetListSize();
    kichThuocOptions.value = response.data.map((size: { ten: string; id: string; }) => ({
      label: size.ten,
      value: size.id,
    }));
  } catch (error) {
    console.error('Lỗi khi lấy danh sách kích cỡ:', error);
  }
};

// Hàm lấy danh sách Màu sắc
const fetchMauSac = async () => {
  try {
    const response = await GetListColor();
    mauSacOptions.value = response.data.map((color: { ten: string; id: string; mau: string; }) => ({
      label: color.ten,
      value: color.id,
      color: color.mau // Giữ lại thuộc tính color nếu bạn dùng để hiển thị màu
    }));
  } catch (error) {
    console.error('Lỗi khi lấy danh sách màu sắc:', error);
  }
};

// Quy tắc validation (nếu cần thêm)
const rules = {
  ten: [{ required: true, message: 'Tên không được để trống!', trigger: 'blur' }],
};

// Hàm tạo mã sản phẩm (ví dụ)
const generateCode = () => {
  const randomNumber = Math.floor(1000 + Math.random() * 9000);
  return 'SP_' + randomNumber; // Ví dụ: SP_1234
};

// Hàm lấy chi tiết sản phẩm
const fetchProductDetails = async (id: string) => {
  try {
    const response = await getDetailSanPham(id);
    const data = response.data;
    product.value = { ...data };
    textToEncode.value = product.value.id; // Cập nhật textToEncode cho QR
    // Không cần cập nhật previewImage ở đây, vì product.value.anh đã có sẵn URL ảnh cũ
  } catch (error: any) {
    if (error?.response?.data?.message) {
      toast.error(error?.response?.data?.message);
    }
  }
};

// START: Hàm xử lý tải lên ảnh
const handleImageUpload = (event: Event) => {
  const input = event.target as HTMLInputElement;
  if (input.files && input.files[0]) {
    selectedFile.value = input.files[0];
    // Tạo URL để xem trước ảnh
    const reader = new FileReader();
    reader.onload = (e) => {
      previewImage.value = e.target?.result as string;
    };
    reader.readAsDataURL(selectedFile.value);
  } else {
    selectedFile.value = null;
    previewImage.value = null;
  }
};

// Hàm kích hoạt input file ẩn
const triggerFileInput = () => {
  const fileInput = document.querySelector('.file-input') as HTMLInputElement;
  fileInput?.click();
};
// END: Hàm xử lý tải lên ảnh

// Hàm gửi dữ liệu form
const handleSubmit = async () => {
  console.log('Submitting form with data:', product.value);
  try {
    await productForm.value.validate();

    const formData = new FormData();
    formData.append('id', product.value.id?.trim() || '');
    formData.append('idSP', props.idSP || '');
    formData.append('soLuong', product.value.soLuong?.trim() || '');
    formData.append('giaBan', product.value.giaBan);
    formData.append('idSize', product.value.idKichThuoc?.trim() || '');
    formData.append('idMau', product.value.idMauSac?.trim() || '');

    // START: Thêm ảnh nếu có
    if (selectedFile.value) {
      formData.append('anh', selectedFile.value);
    }
    // END: Thêm ảnh nếu có

    const res = await updateSanPham(formData);
    if (res.message.includes('đã tồn tại sẽ không được thêm mới')) {
      toast.error(res.message);
      return;
    }
    if (res.message == 'Sản phẩm chi tiết với màu với kích cỡ này đã tồn tại') {
      toast.error(res.message);
      return;
    } else {
      closeModal();
      emit('success');
      toast.success(res.message);
    }
    // Gửi FormData lên API

  } catch (error: any) {
    console.error('Lỗi khi lưu sản phẩm:', response);
    console.error('Lỗi khi lưu sản phẩm:', error);
    toast.error(error?.response?.data?.message || 'Lỗi khi lưu sản phẩm');
  }
};

// Hook onMounted để fetch dữ liệu khi component được mount
onMounted(() => {
  fetchThuongHieu();
  fetchMauSac();
  fetchKichThuoc();
});

// Hàm đóng modal
const closeModal = () => emit('close');

// START: Dummy state cho phần khách hàng (nếu bạn dùng Vuex hoặc Pinia, sẽ khác)
// Đây là giả định bạn có một state.detailKhachHang ở component cha hoặc store
// Nếu phần khách hàng nằm ở component khác, bạn có thể bỏ qua phần này

// Để giả lập việc chuyển đổi giữa có và không có khách hàng, bạn có thể thêm logic ở đây
// Ví dụ: state.value.detailKhachHang = null;
const showKhachHangModal = ref(false); // Ref để điều khiển hiển thị modal khách hàng
// END: Dummy state cho phần khách hàng
</script>

<style scoped lang="scss">
/* START: Cải thiện giao diện tổng thể và Ant Design Table Overrides */
.main-layout {
  display: flex;
  gap: 20px;
  padding: 20px;
  background-color: #f0f2f5;
  width: 100%;
}

/* Card Styling */
.card {
  background-color: #fff;
  border-radius: 12px;
  /* Mềm mại hơn */
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  /* Bóng đổ mềm hơn */
  margin-bottom: 20px;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  /* Giữ kích thước ban đầu */
  color: #333;
}

.card-body {
  padding: 20px;
}

/* Top Header */
.top-header {
  background-color: #54bddb;
  height: 60px;
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
  border-radius: 8px;
  /* Bo góc header */
}

.search-and-create-section {
  display: flex;
  align-items: center;
  gap: 15px;
  width: 100%;
  justify-content: flex-end;
}

.search-box-top {
  display: flex;
  align-items: center;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  margin-left: 15px;
  padding: 5px 10px;
  height: 30px;
  width: 100%;
  background-color: #fff;
  flex-grow: 1;
  max-width: 1420px;
}

.search-input-top {
  width: 100%;
  border: none;
  outline: none;
  height: 30px;
  flex-grow: 1;
  padding: 5px;
  font-size: 14px;
}

.search-icon-top {
  background-color: white;
  background: none;
  border: none;
  cursor: pointer;
  color: #8c8c8c;
  padding-left: 5px;
}

.btn-create-new-invoice {
  color: black;
  background-color: white;
  padding: 10px 15px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  height: 30px;
  align-items: center;
  gap: 5px;
  margin-right: 30px;
  white-space: nowrap;
}

.left-column {
  flex: 1;

}

.right-column {
  flex: 2;

}

/* Tab info (chỉ có trong card "Hóa đơn chờ") */
.tab-info {
  display: flex;
  gap: 10px;
}

.tab-item {
  background-color: #e6f7ff;
  color: #54bddb;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.tab-item.active {
  background-color: #54bddb;
  color: white;
}

/* Tabs cho hóa đơn */
.tabs-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tab-button {
  text-align: center;
  padding: 8px 15px;
  background: #f0f0f0;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  color: #595959;
  transition: all 0.3s ease;
}

.tab-button.active {
  background: #54bddb;
  color: white;
  box-shadow: 0 2px 0 rgba(45, 140, 240, 0.1);
}

.btn-close-tab {
  background: none;
  border: none;
  cursor: pointer;
  color: #8c8c8c;
  font-size: 18px;
  padding: 5px;
}

/* Nội dung hiển thị tab (giỏ hàng) */
.tab-content-display {
  padding: 20px;
}

.actions-add-product {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 15px;
}

.btn-add-product {
  background-color: #54bddb;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  height: 40px;
  width: 120px;
}

.empty-cart {
  text-align: center;
  color: #777;
  padding: 50px 0;
}

/* Ant Design Table Overrides */
:deep(.ant-table-wrapper) {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}

:deep(.ant-table-thead>tr>th) {
  background-color: #fafafa !important;
  font-weight: bold;
  text-align: center !important;
}

:deep(.ant-table-tbody>tr>td) {
  text-align: center;
}

.center-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.quantity-btn {
  padding: 4px 10px;
  background-color: #54bddb;
  color: white;
  border: none;
  font-size: 16px;
  cursor: pointer;
  border-radius: 6px;
  /* Mềm mại hơn */
  margin: 0 2px;
}

.quantity-input {
  width: 40px;
  text-align: center;
  font-size: 16px;
  border: 1px solid #d9d9d9;
  padding: 4px;
  border-radius: 8px;
  /* Mềm mại hơn */
  margin: 0 5px;
}

.highlight {
  color: red;
  font-weight: bold;
}

.btn-choose-product,
.btn-delete-product {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
}

/* Phần "Giỏ hàng" (dưới "Hóa đơn chờ") */
.search-box {
  display: flex;
  align-items: center;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  /* Mềm mại hơn */
  padding: 5px 10px;
  background-color: #fff;
  width: 250px;
}

.search-input {
  border: none;
  outline: none;
  flex-grow: 1;
  padding: 3px;
}

.search-icon {
  background: none;
  border: none;
  cursor: pointer;
  color: #8c8c8c;
  padding-left: 5px;
}

/* Phần "Khách hàng" (cột phải) */
.search-customer {
  display: flex;
  align-items: center;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  /* Mềm mại hơn */
  padding: 5px 10px;
  background-color: #fff;
  width: 100%;
  margin-bottom: 10px;
}

.search-input-customer {
  border: none;
  outline: none;
  flex-grow: 1;
  padding: 3px;
}

.search-icon-customer {
  background: none;
  border: none;
  cursor: pointer;
  color: #8c8c8c;
  padding-left: 5px;
}

.btn-add-customer {
  background-color: #54bddb;
  color: white;
  padding: 8px 15px;
  border: none;
  border-radius: 8px;
  /* Mềm mại hơn */
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  margin-bottom: 20px;
}

.customer-info-display h4 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.info-group {
  margin-bottom: 15px;
}

.info-group span {
  display: block;
  font-weight: bold;
  margin-bottom: 5px;
}

.input-full-width {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  /* Mềm mại hơn */
  box-sizing: border-box;
}

textarea.input-full-width {
  min-height: 60px;
  resize: vertical;
}

/* Phần "Thanh toán" (cột phải) */
.payment-section-content {
  padding-top: 20px;
}

.form-group-payment {
  margin-bottom: 15px;
}

.form-group-payment label {
  font-weight: bold;
  display: block;
  margin-bottom: 5px;
}

.select-discount {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  /* Mềm mại hơn */
  background-color: #fff;
}

.payment-summary {
  margin-top: 20px;
  border-top: 1px dashed #e0e0e0;
  padding-top: 15px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 15px;
}

.summary-item label {
  font-weight: normal;
  color: #595959;
}

.summary-item span {
  font-weight: bold;
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: #ff4d4f;
  border-top: 1px solid #f0f0f0;
  padding-top: 10px;
  margin-top: 15px;
}

.btn-confirm-payment {
  background-color: #54bddb;
  color: white;
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  width: 100%;
  margin-top: 20px;
  transition: background-color 0.3s;
}

.btn-confirm-payment:hover {
  background-color: #54bddb;
}

/* Modal Thanh toán */
.tabs-payment-method {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.tab-button-payment {
  flex: 1;
  background-color: #f0f0f0;
  color: #595959;
  padding: 10px 15px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-button-payment.active {
  background-color: #54bddb;
  color: white;
  border-color: #007bff;
}

.tab-button-payment:hover:not(.active) {
  background-color: #e6f7ff;
}

.payment-method-summary {
  margin-bottom: 15px;
  text-align: center;
}

.payment-details-table {
  margin-bottom: 20px;
}

.footer-payment-summary {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.footer-info div {
  margin-bottom: 5px;
  font-size: 15px;
}

.footer-info strong {
  font-weight: bold;
}

/* Các style ban đầu của bạn, giữ lại và có thể điều chỉnh nếu cần */
.btn-create {
  background-color: #54bddb;
  color: white;
  padding: 8px 14px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}

.form-group {
  /* Đảm bảo mỗi form-group là một khối riêng */
  display: block;
  margin-bottom: 15px;
}

.form-group label {
  font-weight: bold;
  margin-bottom: 6px;
}

.required {
  color: red;
}

.input-wrapper {
  position: relative;
}

.input-wrapper .icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #888;
}

.input-wrapper input {
  width: 100%;
  padding: 10px 10px 10px 35px;
  border-radius: 8px;
  /* Mềm mại hơn */
  border: 1px solid #ccc;
  background-color: #f2f2f2;
}

/* START: Chỉnh sửa các nút Khách hàng */
.button-group-customer {
  display: flex;
  /* Biến container thành flex container */
  gap: 10px;
  /* Khoảng cách giữa các nút */
  margin-top: 16px;
  /* Giữ khoảng cách trên button group */
  padding: 0 20px 20px;
  /* Padding ngang và dưới để căn chỉnh với body card */
}

.add-button {
  flex: 1;
  /* Mỗi nút sẽ chiếm một phần bằng nhau */
  width: auto;
  /* Ghi đè width: 100% để flex hoạt động */
  padding: 12px;
  background-color: #54bddb;
  color: white;
  border: none;
  border-radius: 8px;
  /* Bo góc mềm mại */
  font-weight: bold;
  margin-top: 0;
  /* Bỏ margin-top ở đây vì đã có trên button-group-customer */
  cursor: pointer;
  transition: background 0.3s;
  font-size: 15px;
  /* Điều chỉnh cỡ chữ cho vừa phải */
}

.add-button:hover {
  background-color: #43a8c6;
  /* Màu xanh đậm hơn khi hover */
}

/* END: Chỉnh sửa các nút Khách hàng */

.khach-hang-cart {
  color: black;
}

/* START: Style cho phần Ảnh sản phẩm */
.product-image-upload-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  border: 1px dashed #d9d9d9;
  padding: 15px;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.product-image-preview {
  width: 150px;
  height: 150px;
  object-fit: cover;
  /* Đảm bảo ảnh không bị méo */
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.image-placeholder {
  width: 150px;
  height: 150px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #fff;
  color: #999;
  font-size: 14px;
  text-align: center;
}

.file-input {
  display: none;
  /* Ẩn input file mặc định */
}

.upload-button {
  background-color: #54bddb;
  color: white;
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  font-size: 14px;
}

.upload-button:hover {
  background-color: #43a8c6;
}

/* END: Style cho phần Ảnh sản phẩm */

/* START: Style cho nút Download QR */
.download-button {
  background-color: #ff7f32;
  /* Màu cam */
  color: white;
  padding: 10px 20px;
  font-size: 16px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  width: 100%;
  /* Chiếm toàn bộ chiều rộng cột */
  text-align: center;
  margin-top: 10px;
}

.download-button:hover {
  background-color: #e67e22;
}

.download-button:active {
  background-color: #c45a10;
}

.download-button:focus {
  outline: none;
}

/* END: Style cho nút Download QR */
</style>