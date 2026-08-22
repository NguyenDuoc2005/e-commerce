<template>
  <div class="seller-page">
    <div class="page-head">
      <div>
        <h2>Đơn hàng seller</h2>
        <p>Danh sách sub-order thuộc shop hiện tại.</p>
      </div>
      <a-space>
        <a-input v-model:value="filters.q" placeholder="Mã đơn hoặc người nhận" allow-clear style="width: 240px" />
        <a-select v-model:value="filters.status" style="width: 180px" allow-clear placeholder="Trạng thái">
          <a-select-option v-for="item in statuses" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
        </a-select>
        <a-button :loading="loading" @click="fetchOrders">Tải lại</a-button>
      </a-space>
    </div>

    <a-table :columns="columns" :data-source="orders" :loading="loading" row-key="id" :pagination="{ pageSize: 10 }">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'order'">
          <div class="strong">{{ record.order_code }}</div>
          <div class="muted">{{ record.receiver_name }} - {{ record.receiver_phone }}</div>
        </template>
        <template v-if="column.key === 'amount'">
          {{ currency(record.total_after_discount || record.total_amount) }}
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.order_status)">{{ statusText(record.order_status) }}</a-tag>
        </template>
        <template v-if="column.key === 'actions'">
          <a-space wrap>
            <a-button size="small" @click="openDetail(record.id)">Chi tiết</a-button>
            <a-button v-for="action in actionsFor(record.order_status)" :key="action.action" size="small" type="primary" @click="changeStatus(record.id, action.action)">
              {{ action.label }}
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="detailOpen" title="Chi tiết sub-order" width="760px" :footer="null">
      <a-descriptions v-if="selected" bordered size="small" :column="2">
        <a-descriptions-item label="Mã đơn">{{ selected.order_code }}</a-descriptions-item>
        <a-descriptions-item label="Trạng thái">{{ statusText(selected.order_status) }}</a-descriptions-item>
        <a-descriptions-item label="Người nhận">{{ selected.receiver_name }}</a-descriptions-item>
        <a-descriptions-item label="SĐT">{{ selected.receiver_phone }}</a-descriptions-item>
        <a-descriptions-item label="Địa chỉ" :span="2">{{ selected.shipping_address }}</a-descriptions-item>
      </a-descriptions>
      <a-table class="items-table" :columns="itemColumns" :data-source="selected?.items || []" row-key="id" :pagination="false" size="small" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { changeSellerOrderStatus, getSellerOrderDetail, getSellerOrders, type SellerOrder } from '@/services/api/seller/order.api'

const loading = ref(false)
const detailOpen = ref(false)
const orders = ref<SellerOrder[]>([])
const selected = ref<SellerOrder | null>(null)
const filters = reactive<{ q: string; status: number | null }>({ q: '', status: null })

const statuses = [
  { value: 0, label: 'Chờ xác nhận' },
  { value: 1, label: 'Đã xác nhận' },
  { value: 2, label: 'Chờ giao' },
  { value: 3, label: 'Đang giao' },
  { value: 4, label: 'Hoàn thành' },
  { value: 5, label: 'Đã hủy' }
]

const columns = [
  { title: 'Đơn', key: 'order' },
  { title: 'Shop', dataIndex: 'shop_name' },
  { title: 'Tổng tiền', key: 'amount' },
  { title: 'Trạng thái', key: 'status' },
  { title: 'Thao tác', key: 'actions', width: 280 }
]

const itemColumns = [
  { title: 'Sản phẩm chi tiết', dataIndex: 'product_variant_id' },
  { title: 'Số lượng', dataIndex: 'quantity' },
  { title: 'Giá bán', dataIndex: 'sale_price' }
]

const fetchOrders = async () => {
  loading.value = true
  try {
    orders.value = await getSellerOrders({ q: filters.q, status: filters.status })
  } catch {
    message.error('Không tải được danh sách đơn seller')
  } finally {
    loading.value = false
  }
}

const openDetail = async (id: string) => {
  selected.value = await getSellerOrderDetail(id)
  detailOpen.value = true
}

const changeStatus = async (id: string, action: 'confirm' | 'ready-to-ship' | 'shipping' | 'complete' | 'cancel') => {
  await changeSellerOrderStatus(id, action)
  message.success('Đã cập nhật trạng thái')
  await fetchOrders()
}

const actionsFor = (status: number) => {
  if (status === 0) return [{ action: 'confirm' as const, label: 'Xác nhận' }, { action: 'cancel' as const, label: 'Hủy' }]
  if (status === 1) return [{ action: 'ready-to-ship' as const, label: 'Chờ giao' }, { action: 'cancel' as const, label: 'Hủy' }]
  if (status === 2) return [{ action: 'shipping' as const, label: 'Đang giao' }]
  if (status === 3) return [{ action: 'complete' as const, label: 'Hoàn thành' }]
  return []
}

const statusText = (status: number) => statuses.find((item) => item.value === status)?.label || 'Không rõ'
const statusColor = (status: number) => ['orange', 'blue', 'cyan', 'purple', 'green', 'red'][status] || 'default'
const currency = (value: number) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)

onMounted(fetchOrders)
</script>

<style scoped>
.seller-page { padding: 24px; }
.page-head { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 16px; }
.page-head h2 { margin: 0; font-size: 22px; font-weight: 700; }
.page-head p, .muted { color: #64748b; margin: 4px 0 0; }
.strong { font-weight: 700; }
.items-table { margin-top: 16px; }
</style>
