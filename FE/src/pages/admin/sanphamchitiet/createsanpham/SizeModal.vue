<template>
  <a-modal :open="open" title="Thêm kích cỡ" width="400px" @cancel="closeModal">
    <template #footer>
      <a-popconfirm title="Bạn có chắc chắn muốn lưu thay đổi?" @confirm="handleSubmit" ok-text="Đồng ý" cancel-text="Huỷ">
        <a-button style="background-color: #54bddb;" type="primary">Xác nhận</a-button>
      </a-popconfirm>
      <a-button style="background-color: #54bddb; color: white;" @click="closeModal">Huỷ</a-button>
    </template>

    <a-form :model="product" ref="productForm" name="productForm" autocomplete="off">
      <a-form-item label="Kích cỡ" name="ten" :label-col="{ span: 24 }" :rules="rules.ten">
        <a-input
          v-if="product"
          v-model:value="product.ten"
          placeholder="Nhập kích cỡ"
          style="border-radius: 4px;"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch, defineProps, defineEmits } from 'vue';
import { getSize, type SizeResponse, modifySize } from '@/services/api/admin/size.api';
import { toast } from 'vue3-toastify';

const props = defineProps<{ open: boolean; productId: string | null}>();
const emit = defineEmits(['close', 'success']);

const product = ref<SizeResponse>({ ten: '', mau: '#000000', ma: '', id: '' });
const productForm = ref();

// --- START: MODIFIED RULES ---
const rules = {
  ten: [
    { required: true, message: 'Kích cỡ không được để trống!', trigger: 'blur' },
    {
      validator: async (_rule: any, value: string) => {
        if (!value) {
          return Promise.resolve(); // Handled by required rule
        }

        const numValue = Number(value);

        if (isNaN(numValue)) {
          return Promise.reject('Kích cỡ phải là một số!');
        }

        if (numValue < 26 || numValue > 50) {
          return Promise.reject('Kích cỡ phải từ 26 đến 50!');
        }

        // Check if it's an integer (optional, but common for sizes)
        if (!Number.isInteger(numValue)) {
          return Promise.reject('Kích cỡ phải là số nguyên!');
        }

        return Promise.resolve();
      },
      trigger: 'change', // Trigger on change for immediate feedback, or blur
    },
  ],
};
// --- END: MODIFIED RULES ---

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
    await productForm.value.validate(); // This will now run the custom validation

    const formData = new FormData();
    formData.append('id', product?.value?.id?.trim() || '');
    formData.append('code', product?.value?.ma?.trim() || '');
    formData.append('ten', product?.value?.ten?.trim()); // 'ten' will contain the validated size

    const res = await modifySize(formData);
    closeModal();
    emit('success');
    if (res.message === 'Kích cỡ này đã tồn tại') { // Use strict equality (===)
      toast.error(res.message);
    } else {
      toast.success(res.message);
    }
  } catch (error) {
    // Ant Design form validation errors are typically caught here
    if (error && typeof error === 'object' && 'errorFields' in error) {
      // Validation failed, no need to show a separate toast if Ant Design shows error messages
      console.error('Form validation failed:', error);
    } else if (error?.response?.data?.message) {
      toast.error(error?.response?.data?.message);
    } else {
      toast.error('Có lỗi xảy ra khi lưu kích cỡ.');
    }
  }
};

const closeModal = () => emit('close');
</script>

<style scoped>
/* Your existing styles */
</style>