<template>  
  <a-modal :open="open" :title="props.title" @cancel="closeModal" width="400px">  
    <template #footer>  
      <a-popconfirm title="Bạn có chắc chắn muốn lưu thay đổi?" @confirm="handleSubmit" ok-text="Đồng ý"  
        cancel-text="Huỷ">  
        <a-button style="background-color: #54bddb;" type="primary">Xác nhận</a-button>  
      </a-popconfirm>  
      <a-button @click="closeModal">Huỷ</a-button>  
    </template>  

    <a-form :model="product" ref="productForm" name="productForm" autocomplete="off">  
      <a-form-item label="Tên màu sắc" name="ten" :label-col="{ span: 24 }" :rules="rules.name">  
        <a-input  
          v-if="product"  
          v-model:value="product.ten"  
          placeholder="Nhập tên màu sắc"  
          style="border-radius: 4px;"  
        />  
      </a-form-item>  

      <a-form-item label="Màu sắc" name="color" :label-col="{ span: 24 }">  
        <a-input  
          v-if="product"  
          v-model:value="product.mau"  
          @focus="openColorPicker"  
          style="width: 100%; text-align: center; border-radius: 4px;"  
          readonly  
        />  
        <input type="color" ref="colorPicker" style="display: none;" @input="updateColor" />  
      </a-form-item>  

      <div  
        :style="{ width: '100px', height: '100px', backgroundColor: product?.mau, border: '2px solid #000', borderRadius: '4px' }"  
      ></div>  
    </a-form>  
  </a-modal>  
</template>  

<script setup lang="ts">  
import { ref, watch, defineProps, defineEmits } from 'vue';  
import { getMember, type MauSacResponse, modifyMember } from '@/services/api/admin/mausac.api';  
import { toast } from 'vue3-toastify';  

const props = defineProps<{ open: boolean; productId: string | null; title: string }>();  
const emit = defineEmits(['close', 'success']);  

const product = ref<MauSacResponse>();  
const productForm = ref();  
const colorPicker = ref<HTMLInputElement | null>(null);  

const rules = {  
  name: [{ required: true, message: 'Tên không được để trống!', trigger: 'blur' }],  
};  

const fetchProductDetails = async (id: string) => {  
  try {      
    const response = await getMember(id);  
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
        product.value = { name: '', mau: '#000000' };   
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
    formData.append('ten', product?.value.ten?.trim());  
    formData.append('color', product?.value.mau); 
    const res = await modifyMember(formData);  
    closeModal();  
    emit('success');  
     if (res.message == 'Màu sắc này đã tồn tại') {
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

const openColorPicker = () => {  
  if (colorPicker.value) {  
    colorPicker.value.click();  
  }  
};  

const updateColor = (event: Event) => {  
  const target = event.target as HTMLInputElement;  
  if (target.value) {  
    product.value.mau = target.value;
  }  
};  
</script>  

<style scoped>  
</style>  