<template>
  <div class="seller-profile-page">
    <div class="page-head">
      <div><h2>Hồ sơ shop</h2><p>Thông tin nhận diện và đối soát của shop hiện tại.</p></div>
      <a-button :loading="loading" @click="load">Tải lại</a-button>
    </div>
    <a-spin :spinning="loading">
      <a-descriptions v-if="shop" bordered :column="2">
        <a-descriptions-item label="Tên shop">{{ shop.shopName }}</a-descriptions-item>
        <a-descriptions-item label="Trạng thái"><a-tag :color="statusColor">{{ shop.status }}</a-tag></a-descriptions-item>
        <a-descriptions-item label="Slug">{{ shop.sellerSlug }}</a-descriptions-item>
        <a-descriptions-item label="Điện thoại">{{ shop.contactPhone || '—' }}</a-descriptions-item>
        <a-descriptions-item label="Địa chỉ lấy hàng" :span="2">{{ shop.pickupAddress || '—' }}</a-descriptions-item>
        <a-descriptions-item label="Mô tả" :span="2">{{ shop.description || '—' }}</a-descriptions-item>
        <a-descriptions-item label="Ngân hàng">{{ shop.bankName || '—' }}</a-descriptions-item>
        <a-descriptions-item label="Tài khoản">{{ shop.bankAccountNo || '—' }} · {{ shop.bankAccountHolder || '—' }}</a-descriptions-item>
      </a-descriptions>
      <a-empty v-else-if="!loading" description="Không tìm thấy hồ sơ shop" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getSellerProfile, type SellerResponse } from '@/services/api/seller/seller.api'

const loading = ref(false)
const shop = ref<SellerResponse | null>(null)
const statusColor = computed(() => shop.value?.status === 'APPROVED' ? 'green' : shop.value?.status === 'SUSPENDED' ? 'red' : 'gold')
const load = async () => {
  loading.value = true
  try { shop.value = (await getSellerProfile()).data }
  catch (error: any) { message.error(error?.response?.data?.message ?? 'Không tải được hồ sơ shop') }
  finally { loading.value = false }
}
onMounted(load)
</script>

<style scoped>
.seller-profile-page { padding: 24px; }
.page-head { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 18px; }
.page-head h2 { margin: 0; }
.page-head p { color: #667085; margin: 4px 0 0; }
</style>
