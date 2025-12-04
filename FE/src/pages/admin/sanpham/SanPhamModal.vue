<template>
  <a-modal :open="open" :title="props.title" width="600px" @cancel="closeModal">
    <template #footer>
      <a-popconfirm title="Bạn có chắc chắn muốn lưu thay đổi?" @confirm="handleSubmit" ok-text="Đồng ý"
        cancel-text="Huỷ">
        <a-button type="primary" style="background-color: #54bddb; color: white;">Xác nhận</a-button>
      </a-popconfirm>
      <a-button @click="closeModal">Huỷ</a-button>
    </template>

    <a-form :model="product" ref="productForm" name="productForm" autocomplete="off">
      <a-form-item label="Tên Sản Phẩm" name="ten" :label-col="{ span: 24 }" :rules="rules.ten">
        <a-input v-model:value="product.ten" placeholder="Nhập tên sản phẩm" />
      </a-form-item>

      <a-form :model="product" ref="productForm" name="productForm" autocomplete="off">
        <a-form-item label="Thương hiệu" name="facilityId" :label-col="{ span: 24 }" >
          <a-select v-model:value="product.idThuongHieu" :options="thuongHieuOptions" placeholder="Chọn thuong hiệu" />
        </a-form-item>
      </a-form>

      <a-form :model="product" ref="productForm" name="productForm" autocomplete="off">
        <a-form-item label="Loại đế" name="facilityId" :label-col="{ span: 24 }">
          <a-select v-model:value="product.idLoaiDe" :options="loaiDeOptions" placeholder="Chọn Loại đế" />
        </a-form-item>
      </a-form>

      <a-form :model="product" ref="productForm" name="productForm" autocomplete="off">
        <a-form-item label="Danh mục" name="facilityId" :label-col="{ span: 24 }">
          <a-select v-model:value="product.idDanhMuc" :options="danhMucOptions" placeholder="Chọn danh mục" />
        </a-form-item>
      </a-form>

      <a-form :model="product" ref="productForm" name="productForm" autocomplete="off">
        <a-form-item label="Chất liệu" name="facilityId" :label-col="{ span: 24 }">
          <a-select v-model:value="product.idChatLieu" :options="chatLieuOptions" placeholder="Chọn chất liệu" />
        </a-form-item>
      </a-form>

      <a-form-item label="Mô tả" name="mota" :label-col="{ span: 24 }">
        <a-textarea v-model:value="product.moTa" :rows="6" placeholder="Nhập mô tả" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>


<script setup lang="ts">
import { ref, watch, defineProps, defineEmits, onMounted } from 'vue';
import { getSanPham, type SanPhamResponse, modifySanPham, GetListThuongHieu, GetListXuatXu, GetListLoaiDe, GetListDanhMuc, GetListChatLieu } from '@/services/api/admin/sanpham.api';
import { toast } from 'vue3-toastify';
import dayjs from 'dayjs';
import { Row } from 'ant-design-vue';


const props = defineProps<{ open: boolean; productId: string | null; title: string }>();
const emit = defineEmits(['close', 'success']);
const thuongHieuOptions = ref<{ label: string; value: string }[]>([])
const xuatXuOptions = ref<{ label: string; value: string }[]>([])
const loaiDeOptions = ref<{ label: string; value: string }[]>([])
const danhMucOptions = ref<{ label: string; value: string }[]>([])
const chatLieuOptions = ref<{ label: string; value: string }[]>([])

const product = ref<SanPhamResponse>({
  id: '',
  ten: '',
  code: '',
  moTa: '',
  idThuongHieu: '',
  idLoaiDe: '',
  idXuatXu: '',
  idDanhMuc: '',
  idChatLieu: '',
});

const productForm = ref();

const fetchThuongHieu = async () => {
  try {
    const response = await GetListThuongHieu()
    thuongHieuOptions.value = response.data.map(thuongHieu => ({
      label: thuongHieu.ten,
      value: thuongHieu.id,
    }))
  } catch (error) {
    console.error('Lỗi khi lấy danh sách cơ sở:', error)
  }
}

const fetchXuatXu = async () => {
  try {
    const response = await GetListXuatXu()
    xuatXuOptions.value = response.data.map(thuongHieu => ({
      label: thuongHieu.ten,
      value: thuongHieu.id,
    }))
  } catch (error) {
    console.error('Lỗi khi lấy danh sách cơ sở:', error)
  }
}

const fetchChatLieu = async () => {
  try {
    const response = await GetListChatLieu()
    chatLieuOptions.value = response.data.map(thuongHieu => ({
      label: thuongHieu.ten,
      value: thuongHieu.id,
    }))
  } catch (error) {
    console.error('Lỗi khi lấy danh sách cơ sở:', error)
  }
}



const fetchLoaiDe = async () => {
  try {
    const response = await GetListLoaiDe()
    loaiDeOptions.value = response.data.map(thuongHieu => ({
      label: thuongHieu.ten,
      value: thuongHieu.id,
    }))
  } catch (error) {
    console.error('Lỗi khi lấy danh sách cơ sở:', error)
  }
}


const fetchDanhMuc = async () => {
  try {
    const response = await GetListDanhMuc()
    danhMucOptions.value = response.data.map(thuongHieu => ({
      label: thuongHieu.ten,
      value: thuongHieu.id,
    }))
  } catch (error) {
    console.error('Lỗi khi lấy danh sách cơ sở:', error)
  }
}


const rules = {
  ten: [{ required: true, message: 'Tên không được để trống!', trigger: 'blur' }],
};

const generateCode = () => {
  const randomNumber = Math.floor(1000 + Math.random() * 9000); // đảm bảo luôn 4 chữ số
  return 'KH_' + randomNumber;
};

const fetchProductDetails = async (id: string) => {
  try {
    const response = await getSanPham(id);
    const data = response.data;

    product.value = {
      idThuongHieu: data.idThuongHieu,
      ...data
    };
  } catch (error) {
    if (error?.response?.data?.message) {
      toast.error(error?.response?.data?.message);
    }
  }
};


watch(
  () => [props.productId, props.open],
  async ([newId, isOpen]) => {
    if (isOpen) {
      if (productForm.value) {
        productForm.value.resetFields();
      }
      if (newId) {
        await fetchProductDetails(newId);
      } else {
        // reset product and auto-generate code
        product.value = {
          id: '',
          ten: '',
          ma: generateCode()
        };
      }
    }
  },
  { immediate: true }
);

const handleSubmit = async () => {
  try {
    await productForm.value.validate();

    const formData = new FormData();
    formData.append('id', product.value.id?.trim() || '');
    formData.append('code', product.value.code?.trim() || generateCode());
    formData.append('ten', product.value.ten?.trim() || '');
    formData.append('moTa', product.value.moTa?.trim() || '');
    formData.append('idThuongHieu', product.value.idThuongHieu?.trim() || '');
    formData.append('idChatLieu', product.value.idChatLieu?.trim() || '');
    formData.append('idLoaiDe', product.value.idLoaiDe?.trim() || '');
    formData.append('idXuatXu', product.value.idXuatXu?.trim() || '');
    formData.append('idDanhMuc', product.value.idDanhMuc?.trim() || '');
    const res = await modifySanPham(formData);
    closeModal();
    emit('success');
    toast.success(res.message);
  } catch (error) {
    toast.error(error?.response?.data?.message || 'Lỗi khi lưu sản phẩm');
  }
};

onMounted(() => {
  fetchThuongHieu();
  fetchXuatXu();
  fetchLoaiDe();
  fetchDanhMuc();
  fetchChatLieu();
});

const closeModal = () => emit('close');
</script>
<style scoped lang="scss">
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