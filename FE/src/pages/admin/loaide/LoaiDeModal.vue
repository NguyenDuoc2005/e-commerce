<template>
  <a-modal :open="open" @cancel="closeModal" :title="props.title" width="400px">
    <template #footer>
      <a-popconfirm title="Bạn có chắc chắn muốn lưu thay đổi?" @confirm="handleSubmit" ok-text="Đồng ý" cancel-text="Huỷ">
        <a-button style="background-color: #54bddb;" type="primary">Xác nhận</a-button>
      </a-popconfirm>
      <a-button @click="closeModal">Huỷ</a-button>
    </template>

    <a-form :model="product" ref="productForm" name="productForm" autocomplete="off">
      <a-form-item label="Tên loại đế" name="ten" :label-col="{ span: 24 }" :rules="rules.ten">
        <a-input
          v-if="product"
          v-model:value="product.ten"
          placeholder="Nhập loại đế"
          style="border-radius: 4px;"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch, defineProps, defineEmits } from 'vue';
import { getSize, type SizeResponse, modifySize } from '@/services/api/admin/loaide.api';
import { toast } from 'vue3-toastify';

const props = defineProps<{ open: boolean; productId: string | null; title: string }>();
const emit = defineEmits(['close', 'success']);

const product = ref<SizeResponse>({ ten: '', mau: '#000000', ma: '', id: '' });
const productForm = ref();
const rules = {
  ten: [
    { required: true, message: 'Loại đế không được để trống!', trigger: 'blur' },
    { min: 2, message: 'Tên loại đế phải có ít nhất 2 ký tự!', trigger: 'blur' },
    {
      validator: async (_rule: any, value: string) => {
        if (value && /\d/.test(value)) {
          return Promise.reject('Tên loại đế không được chứa số!');
        }
        return Promise.resolve();
      },
      trigger: 'blur'
    }
  ],
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
    if (res.message == 'Loại đế này đã tồn tại') {
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

<style lang="css" scoped>
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