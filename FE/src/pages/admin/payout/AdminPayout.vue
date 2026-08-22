<template>
  <div class="payout-page">
    <header class="page-head">
      <div>
        <h1>Đối soát marketplace</h1>
        <p>Kiểm soát hoa hồng và xác nhận chi trả cho từng sub-order đã hoàn thành.</p>
      </div>
      <a-button :loading="loading" @click="loadData">Tải lại</a-button>
    </header>

    <div class="summary-grid">
      <div class="metric"><span>Chờ chi trả</span><strong>{{ pendingRows.length }}</strong></div>
      <div class="metric"><span>Giá trị chờ chi</span><strong>{{ currency(pendingAmount) }}</strong></div>
      <div class="metric"><span>Đã thanh toán</span><strong>{{ currency(paidAmount) }}</strong></div>
    </div>

    <a-tabs v-model:active-key="activeTab">
      <a-tab-pane key="receivables" tab="Khoản đối soát">
        <a-table row-key="id" :columns="receivableColumns" :data-source="receivables" :loading="loading" :pagination="{ pageSize: 10 }">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'gross'">{{ currency(record.grossAmount) }}</template>
            <template v-else-if="column.key === 'commission'">{{ currency(record.commissionAmount) }} ({{ record.commissionRate }}%)</template>
            <template v-else-if="column.key === 'net'">{{ currency(record.netAmount) }}</template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="record.status === 'PAID' ? 'green' : 'gold'">{{ record.status }}</a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-popconfirm v-if="record.status !== 'PAID'" title="Xác nhận khoản này đã được chi trả?" ok-text="Xác nhận" cancel-text="Hủy" @confirm="pay(record.id)">
                <a-button type="primary" size="small" :loading="payingId === record.id">Ghi nhận đã trả</a-button>
              </a-popconfirm>
              <span v-else class="text-muted">Hoàn tất</span>
            </template>
          </template>
        </a-table>
      </a-tab-pane>

      <a-tab-pane key="commission" tab="Cấu hình hoa hồng">
        <form class="commission-form" @submit.prevent="saveCommission">
          <a-input v-model:value="commissionForm.categoryId" placeholder="ID danh mục, bỏ trống để áp dụng mặc định" allow-clear />
          <a-input-number v-model:value="commissionForm.ratePercent" :min="0" :max="100" :precision="2" addon-after="%" />
          <a-button type="primary" html-type="submit" :loading="savingConfig">Lưu cấu hình</a-button>
        </form>
        <a-table row-key="id" :columns="commissionColumns" :data-source="configs" :loading="loading" :pagination="false">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'scope'">{{ record.categoryId || 'Mặc định toàn sàn' }}</template>
            <template v-else-if="column.key === 'rate'">{{ record.ratePercent }}%</template>
            <template v-else-if="column.key === 'active'"><a-tag :color="record.active ? 'green' : 'default'">{{ record.active ? 'Đang áp dụng' : 'Ngừng áp dụng' }}</a-tag></template>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  createCommissionConfig,
  getAdminReceivables,
  getCommissionConfigs,
  payAdminReceivable,
  type AdminReceivable,
  type CommissionConfig
} from '@/services/api/admin/payout.api'

const activeTab = ref('receivables')
const loading = ref(false)
const savingConfig = ref(false)
const payingId = ref('')
const receivables = ref<AdminReceivable[]>([])
const configs = ref<CommissionConfig[]>([])
const commissionForm = reactive<{ categoryId: string; ratePercent: number }>({ categoryId: '', ratePercent: 5 })

const receivableColumns = [
  { title: 'Sub-order', dataIndex: 'orderSellerId' },
  { title: 'Seller', dataIndex: 'sellerId' },
  { title: 'Doanh thu gộp', key: 'gross' },
  { title: 'Hoa hồng', key: 'commission' },
  { title: 'Thực nhận', key: 'net' },
  { title: 'Trạng thái', key: 'status' },
  { title: 'Thao tác', key: 'action', width: 150 }
]
const commissionColumns = [
  { title: 'Phạm vi', key: 'scope' },
  { title: 'Tỷ lệ', key: 'rate' },
  { title: 'Trạng thái', key: 'active' },
  { title: 'Ngày tạo', dataIndex: 'createdAt' }
]

const pendingRows = computed(() => receivables.value.filter(item => item.status !== 'PAID'))
const pendingAmount = computed(() => pendingRows.value.reduce((sum, item) => sum + (item.netAmount || 0), 0))
const paidAmount = computed(() => receivables.value.filter(item => item.status === 'PAID').reduce((sum, item) => sum + (item.netAmount || 0), 0))

const loadData = async () => {
  loading.value = true
  try {
    const [receivableRows, configRows] = await Promise.all([getAdminReceivables(), getCommissionConfigs()])
    receivables.value = receivableRows ?? []
    configs.value = configRows ?? []
  } catch (error: any) {
    message.error(error?.response?.data?.message ?? 'Không tải được dữ liệu đối soát')
  } finally {
    loading.value = false
  }
}

const pay = async (id: string) => {
  payingId.value = id
  try {
    await payAdminReceivable(id)
    message.success('Đã ghi nhận thanh toán')
    await loadData()
  } catch (error: any) {
    message.error(error?.response?.data?.message ?? 'Không thể ghi nhận thanh toán')
  } finally {
    payingId.value = ''
  }
}

const saveCommission = async () => {
  savingConfig.value = true
  try {
    await createCommissionConfig({
      categoryId: commissionForm.categoryId.trim() || undefined,
      ratePercent: commissionForm.ratePercent
    })
    message.success('Đã lưu cấu hình hoa hồng')
    commissionForm.categoryId = ''
    await loadData()
  } catch (error: any) {
    message.error(error?.response?.data?.message ?? 'Không thể lưu cấu hình hoa hồng')
  } finally {
    savingConfig.value = false
  }
}

const currency = (value?: number) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)

onMounted(loadData)
</script>

<style scoped>
.payout-page { padding: 24px; }
.page-head { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 18px; }
.page-head h1 { margin: 0; font-size: 24px; font-weight: 700; }
.page-head p { margin: 5px 0 0; color: #64748b; }
.summary-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-bottom: 20px; }
.metric { padding: 14px; border: 1px solid #dbe3ef; border-radius: 8px; background: #fff; }
.metric span { display: block; margin-bottom: 5px; color: #64748b; }
.metric strong { font-size: 20px; }
.commission-form { display: grid; grid-template-columns: minmax(280px, 1fr) 150px auto; gap: 10px; margin-bottom: 16px; }
@media (max-width: 768px) {
  .page-head { flex-direction: column; }
  .summary-grid { grid-template-columns: 1fr; }
  .commission-form { grid-template-columns: 1fr; }
}
</style>
