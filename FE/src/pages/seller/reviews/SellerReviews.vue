<template>
  <section class="review-page">
    <header>
      <div>
        <h1>Danh gia shop</h1>
        <p>Doc phan hoi cua khach va tra loi theo tung danh gia.</p>
      </div>
      <a-button :loading="loading" @click="loadReviews">Tai lai</a-button>
    </header>

    <a-table :columns="columns" :data-source="reviews" :loading="loading" row-key="id" :scroll="{ x: 900 }">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'rating'">
          <div>San pham: <a-rate :value="record.productRating" disabled /></div>
          <div>Shop: <a-rate :value="record.shopRating" disabled /></div>
        </template>
        <template v-else-if="column.key === 'comment'">
          <span>{{ record.comment || 'Khong co binh luan' }}</span>
        </template>
        <template v-else-if="column.key === 'reply'">
          <a-alert v-if="record.sellerReply" type="info" :message="record.sellerReply" />
          <span v-else class="muted">Chua phan hoi</span>
        </template>
        <template v-else-if="column.key === 'createdAt'">
          {{ formatDate(record.createdAt) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button type="primary" @click="openReply(record)">{{ record.sellerReply ? 'Sua phan hoi' : 'Phan hoi' }}</a-button>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="replyOpen" title="Phan hoi danh gia" :confirm-loading="submitting" @ok="submitReply">
      <a-textarea v-model:value="replyText" :rows="5" :maxlength="2000" show-count />
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getSellerReviews, replyReview, type Review } from '@/services/api/seller/review.api'

const columns = [
  { title: 'Danh gia', key: 'rating', width: 260 },
  { title: 'Nhan xet', key: 'comment' },
  { title: 'Phan hoi cua shop', key: 'reply' },
  { title: 'Ngay tao', key: 'createdAt', width: 130 },
  { title: 'Thao tac', key: 'action', width: 120, fixed: 'right' as const }
]
const reviews = ref<Review[]>([])
const loading = ref(false)
const submitting = ref(false)
const replyOpen = ref(false)
const selected = ref<Review | null>(null)
const replyText = ref('')

const loadReviews = async () => {
  loading.value = true
  try {
    reviews.value = (await getSellerReviews()).data ?? []
  } catch (error: any) {
    message.error(error?.response?.data?.message ?? 'Khong tai duoc danh gia')
  } finally {
    loading.value = false
  }
}

const openReply = (review: Review) => {
  selected.value = review
  replyText.value = review.sellerReply ?? ''
  replyOpen.value = true
}

const submitReply = async () => {
  if (!selected.value || !replyText.value.trim()) {
    message.warning('Phan hoi khong duoc de trong')
    return
  }
  submitting.value = true
  try {
    const response = await replyReview(selected.value.id, replyText.value.trim())
    const index = reviews.value.findIndex(item => item.id === selected.value?.id)
    if (index >= 0) reviews.value[index] = response.data
    replyOpen.value = false
    message.success('Da luu phan hoi')
  } catch (error: any) {
    message.error(error?.response?.data?.message ?? 'Khong luu duoc phan hoi')
  } finally {
    submitting.value = false
  }
}

const formatDate = (value: string) => new Intl.DateTimeFormat('vi-VN').format(new Date(value))
onMounted(loadReviews)
</script>

<style scoped>
.review-page { padding: 24px; }
header { display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-bottom: 20px; }
h1 { margin: 0; font-size: 24px; }
p { margin: 5px 0 0; color: #64748b; }
.muted { color: #94a3b8; }
@media (max-width: 640px) { .review-page { padding: 16px; } header { align-items: stretch; flex-direction: column; } }
</style>
