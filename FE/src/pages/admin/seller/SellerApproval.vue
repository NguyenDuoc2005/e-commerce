<template>
  <div class="seller-admin-page">
    <div class="page-head">
      <div>
        <h2>Duyệt Seller</h2>
        <p>Quản lý hồ sơ shop đang chờ duyệt, đã duyệt, bị từ chối hoặc tạm khóa.</p>
      </div>
      <a-space>
        <a-select v-model:value="statusFilter" style="width: 190px" @change="fetchSellers">
          <a-select-option value="">Tất cả trạng thái</a-select-option>
          <a-select-option value="PENDING_APPROVAL">Chờ duyệt</a-select-option>
          <a-select-option value="APPROVED">Đã duyệt</a-select-option>
          <a-select-option value="REJECTED">Bị từ chối</a-select-option>
          <a-select-option value="SUSPENDED">Tạm khóa</a-select-option>
        </a-select>
        <a-button :loading="loading" @click="fetchSellers">Tải lại</a-button>
      </a-space>
    </div>

    <a-table
      row-key="id"
      :columns="columns"
      :data-source="sellers"
      :loading="loading"
      :pagination="{ pageSize: 10 }"
      class="seller-table"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'shop'">
          <div class="shop-cell">
            <a-avatar :src="record.logoUrl">{{ record.shopName?.[0] }}</a-avatar>
            <div>
              <div class="shop-name">{{ record.shopName }}</div>
              <div class="shop-slug">/shop/{{ record.sellerSlug }}</div>
            </div>
          </div>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'identity'">
          <div>{{ record.identityType }} - {{ record.identityNumber }}</div>
          <small>{{ record.bankName }} / {{ record.bankAccountHolder }}</small>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button
              v-if="record.status === 'PENDING_APPROVAL' || record.status === 'REJECTED'"
              type="primary"
              size="small"
              @click="approve(record.id)"
            >
              Duyệt
            </a-button>
            <a-button v-if="record.status === 'PENDING_APPROVAL'" danger size="small" @click="openReject(record)">
              Từ chối
            </a-button>
            <a-button v-if="record.status === 'APPROVED'" size="small" danger @click="openSuspend(record)">
              Khóa
            </a-button>
            <a-button v-if="record.status === 'SUSPENDED'" size="small" @click="reopen(record.id)">
              Mở lại
            </a-button>
          </a-space>
        </template>
        <template v-else-if="column.dataIndex">{{ record[column.dataIndex] ?? '—' }}</template>
      </template>
    </a-table>

    <a-modal
      v-model:open="decisionModal.open"
      :title="decisionModal.type === 'reject' ? 'Từ chối seller' : 'Khóa seller'"
      ok-text="Xác nhận"
      cancel-text="Hủy"
      :confirm-loading="decisionModal.loading"
      @ok="submitDecision"
    >
      <a-form layout="vertical">
        <a-form-item label="Lý do" required>
          <a-textarea v-model:value="decisionModal.reason" :rows="4" placeholder="Nhập lý do để seller biết cần xử lý gì" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  approveSeller,
  getAdminSellers,
  rejectSeller,
  reopenSeller,
  suspendSeller,
  type SellerResponse,
  type SellerStatus
} from '@/services/api/seller/seller.api'

const loading = ref(false)
const sellers = ref<SellerResponse[]>([])
const statusFilter = ref<SellerStatus | ''>('PENDING_APPROVAL')

const decisionModal = reactive({
  open: false,
  loading: false,
  type: 'reject' as 'reject' | 'suspend',
  sellerId: '',
  reason: ''
})

const columns = [
  { title: 'Shop', key: 'shop' },
  { title: 'Liên hệ', dataIndex: 'contactPhone', key: 'contactPhone' },
  { title: 'Định danh / Ngân hàng', key: 'identity' },
  { title: 'Trạng thái', key: 'status' },
  { title: 'Ngày tạo', dataIndex: 'createdAt', key: 'createdAt' },
  { title: 'Thao tác', key: 'action', width: 260 }
]

const fetchSellers = async () => {
  loading.value = true
  try {
    const res = await getAdminSellers(statusFilter.value)
    sellers.value = res.data ?? []
  } catch (error: any) {
    message.error(error?.response?.data?.message ?? 'Không tải được danh sách seller')
  } finally {
    loading.value = false
  }
}

const approve = async (id: string) => {
  try {
    const res = await approveSeller(id)
    message.success(res.message ?? 'Đã duyệt seller')
    fetchSellers()
  } catch (error: any) {
    message.error(error?.response?.data?.message ?? 'Không duyệt được seller')
  }
}

const reopen = async (id: string) => {
  try {
    const res = await reopenSeller(id)
    message.success(res.message ?? 'Đã mở lại seller')
    fetchSellers()
  } catch (error: any) {
    message.error(error?.response?.data?.message ?? 'Không mở lại được seller')
  }
}

const openReject = (seller: SellerResponse) => {
  decisionModal.type = 'reject'
  decisionModal.sellerId = seller.id
  decisionModal.reason = ''
  decisionModal.open = true
}

const openSuspend = (seller: SellerResponse) => {
  decisionModal.type = 'suspend'
  decisionModal.sellerId = seller.id
  decisionModal.reason = ''
  decisionModal.open = true
}

const submitDecision = async () => {
  if (!decisionModal.reason.trim()) {
    message.warning('Vui lòng nhập lý do')
    return
  }
  decisionModal.loading = true
  try {
    const res = decisionModal.type === 'reject'
      ? await rejectSeller(decisionModal.sellerId, decisionModal.reason)
      : await suspendSeller(decisionModal.sellerId, decisionModal.reason)
    message.success(res.message ?? 'Đã cập nhật seller')
    decisionModal.open = false
    fetchSellers()
  } catch (error: any) {
    message.error(error?.response?.data?.message ?? 'Không cập nhật được seller')
  } finally {
    decisionModal.loading = false
  }
}

const statusLabel = (status: SellerStatus) => {
  const labels: Record<SellerStatus, string> = {
    DRAFT: 'Nháp',
    PENDING_APPROVAL: 'Chờ duyệt',
    APPROVED: 'Đã duyệt',
    REJECTED: 'Bị từ chối',
    SUSPENDED: 'Tạm khóa',
    CLOSED: 'Đã đóng'
  }
  return labels[status] ?? status
}

const statusColor = (status: SellerStatus) => {
  if (status === 'APPROVED') return 'green'
  if (status === 'REJECTED') return 'red'
  if (status === 'SUSPENDED') return 'volcano'
  return 'blue'
}

onMounted(fetchSellers)
</script>

<style scoped>
.seller-admin-page {
  padding: 20px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 18px;
  padding: 18px 20px;
  border: 1px solid var(--admin-border, #dbe3ef);
  border-radius: 8px;
  background: #ffffff;
}

.page-head h2 {
  margin: 0 0 4px;
  color: #172033;
  font-size: 24px;
  font-weight: 800;
}

.page-head p {
  margin: 0;
  color: #5d6978;
}

.seller-table {
  background: #ffffff;
  border-radius: 8px;
}

.shop-cell {
  display: flex;
  gap: 10px;
  align-items: center;
}

.shop-name {
  font-weight: 700;
  color: #172033;
}

.shop-slug,
small {
  color: #6b7280;
}

@media (max-width: 768px) {
  .page-head {
    flex-direction: column;
  }
}
</style>
