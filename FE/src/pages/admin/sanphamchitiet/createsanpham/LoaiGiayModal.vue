<template>
  <a-modal :open="open" title="Thêm danh mục" width="400px" @cancel="closeModal">
    <template #footer>
      <a-popconfirm title="Bạn có chắc chắn muốn lưu thay đổi?" @confirm="handleSubmit" ok-text="Đồng ý" cancel-text="Huỷ">
        <a-button style="background-color: #54bddb;" type="primary">Xác nhận</a-button>
      </a-popconfirm>
      <a-button style="background-color: #54bddb; color: white;" @click="closeModal">Huỷ</a-button>
    </template>

    <a-form :model="product" ref="productForm" name="productForm" autocomplete="off">
      <a-form-item label="Tên danh mục" name="ten" :label-col="{ span: 24 }" :rules="rules.ten">
        <a-input
          v-if="product"
          v-model:value="product.ten"
          placeholder="Nhập danh mục"
          style="border-radius: 4px;"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch, defineProps, defineEmits } from 'vue';
import { getSize, type SizeResponse, modifySize } from '@/services/api/admin/loaigiay.api';
import { toast } from 'vue3-toastify';

const props = defineProps<{ open: boolean; productId: string | null; title: string }>();
const emit = defineEmits(['close', 'success']);

const product = ref<SizeResponse>({ ten: '', mau: '#000000', ma: '', id: '' });
const productForm = ref();

const rules = {
  ten: [{ required: true, message: 'danh mục không được để trống!', trigger: 'blur' }],
};

const generateCode = () => {
  const randomNumber = Math.floor(1000 + Math.random() * 9000); // đảm bảo luôn 4 chữ số
  return 'SIZE_' + randomNumber;
};

const fetchProductDetails = async (id: string) => {
  try {
    const response = await getSize(id);
    product.value = response.data;
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
          ma: generateCode(),
          mau: '#000000',
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
    formData.append('id', product?.value?.id?.trim() || '');
    formData.append('code', product?.value?.ma?.trim() || '');
    formData.append('ten', product?.value?.ten?.trim());

    const res = await modifySize(formData);
    closeModal();
    emit('success');
     if (res.message == 'Danh mục này đã tồn tại') {
      toast.error(res.message);
    } else {
      toast.success(res.message);
    }
  } catch (error) {
    if (error?.response?.data?.message) {
      toast.error(error?.response?.data?.message);
    }
  }
};

const closeModal = () => emit('close');
</script>
