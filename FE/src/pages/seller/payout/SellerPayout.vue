<template>
  <div class="seller-page">
    <div class="page-head">
      <div>
        <h2>Ví & đối soát</h2>
        <p>Theo dõi doanh thu chờ đối soát của shop.</p>
      </div>
      <a-button :loading="loading" @click="fetchData">Tải lại</a-button>
    </div>

    <div class="summary-grid">
      <div class="metric"><span>Chờ đối soát</span><strong>{{ currency(wallet?.pendingAmount) }}</strong></div>
      <div class="metric"><span>Có thể rút</span><strong>{{ currency(wallet?.availableAmount) }}</strong></div>
      <div class="metric"><span>Đã chi trả</span><strong>{{ currency(wallet?.paidAmount) }}</strong></div>
    </div>

    <a-table :columns="columns" :data-source="receivables" :loading="loading" row-key="id" :pagination="{ pageSize: 10 }">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'gross'">{{ currency(record.grossAmount) }}</template>
        <template v-if="column.key === 'commission'">{{ currency(record.commissionAmount) }} ({{ record.commissionRate }}%)</template>
        <template v-if="column.key === 'net'">{{ currency(record.netAmount) }}</template>
        <template v-if="column.key === 'availableAt'">{{ dateTime(record.availableAt) }}</template>
        <template v-if="column.key === 'status'"><a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag></template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getSellerReceivables, getSellerWallet, type SellerReceivable, type SellerWallet } from '@/services/api/seller/payout.api'

const loading = ref(false)
const wallet = ref<SellerWallet | null>(null)
const receivables = ref<SellerReceivable[]>([])

const columns = [
  { title: 'Sub-order', dataIndex: 'orderSellerId' },
  { title: 'Doanh thu gộp', key: 'gross' },
  { title: 'Hoa hồng sàn', key: 'commission' },
  { title: 'Thực nhận', key: 'net' },
  { title: 'Ngày khả dụng', key: 'availableAt' },
  { title: 'Trạng thái', key: 'status' }
]

const fetchData = async () => {
  loading.value = true
  try {
    const [walletRes, receivableRes] = await Promise.all([getSellerWallet(), getSellerReceivables()])
    wallet.value = walletRes
    receivables.value = receivableRes
  } finally {
    loading.value = false
  }
}

const currency = (value?: number) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)
const dateTime = (value?: string) => value ? new Date(value).toLocaleString('vi-VN') : '—'
const statusColor = (status: string) => ({ PENDING: 'gold', AVAILABLE: 'blue', PAID: 'green' }[status] || 'default')

onMounted(fetchData)
</script>

<style scoped>
.seller-page { padding: 24px; }
.page-head { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 16px; }
.page-head h2 { margin: 0; font-size: 22px; font-weight: 700; }
.page-head p { color: #64748b; margin: 4px 0 0; }
.summary-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-bottom: 16px; }
.metric { border: 1px solid #dbe3ef; border-radius: 8px; padding: 14px; background: #fff; }
.metric span { display: block; color: #64748b; margin-bottom: 6px; }
.metric strong { font-size: 20px; }
@media (max-width: 768px) { .summary-grid { grid-template-columns: 1fr; } .page-head { flex-direction: column; } }
</style>
