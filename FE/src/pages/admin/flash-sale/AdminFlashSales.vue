<template>
  <div class="flash-admin-page">
    <div class="page-head">
      <div>
        <h2>Flash sale toàn sàn</h2>
        <p>Tạo khung sự kiện, nhận sản phẩm từ seller và duyệt giá trước khi lên sàn.</p>
      </div>
      <a-space><a-button @click="load">Tải lại</a-button><a-button type="primary" @click="openCampaign()">Tạo Flash sale</a-button></a-space>
    </div>

    <a-row :gutter="16" class="summary-row">
      <a-col :xs="24" :md="8"><a-card><a-statistic title="Sự kiện" :value="campaigns.length" /></a-card></a-col>
      <a-col :xs="24" :md="8"><a-card><a-statistic title="Đăng ký chờ duyệt" :value="pendingTotal" /></a-card></a-col>
      <a-col :xs="24" :md="8"><a-card><a-statistic title="Sản phẩm đã duyệt" :value="approvedTotal" /></a-card></a-col>
    </a-row>

    <a-table :columns="campaignColumns" :data-source="campaigns" :loading="loading" row-key="id" :pagination="{ pageSize: 8 }">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'name'">
          <strong>{{ record.name }}</strong><div class="muted">{{ record.code }}</div>
        </template>
        <template v-else-if="column.key === 'registration'">
          <div>{{ timeRange(record.registrationStartDate, record.registrationEndDate) }}</div>
          <a-tag :color="record.registrationOpen ? 'green' : 'default'">{{ record.registrationOpen ? 'Đang nhận đăng ký' : 'Đã đóng đăng ký' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'saleTime'">{{ timeRange(record.startDate, record.endDate) }}</template>
        <template v-else-if="column.key === 'status'"><a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag></template>
        <template v-else-if="column.key === 'registrations'">
          <span class="pending-number">{{ record.pendingCount }} chờ</span> · {{ record.approvedCount }} đã duyệt
        </template>
        <template v-else-if="column.key === 'actions'">
          <a-space><a-button size="small" @click="viewRegistrations(record)">Duyệt sản phẩm</a-button><a-button size="small" :disabled="Date.now() >= record.startDate" @click="openCampaign(record)">Sửa</a-button></a-space>
        </template>
      </template>
    </a-table>

    <section v-if="selectedCampaign" class="registration-section">
      <div class="section-head">
        <div><h3>Đăng ký — {{ selectedCampaign.name }}</h3><p>{{ registrations.length }} sản phẩm seller gửi lên</p></div>
        <a-select v-model:value="registrationFilter" style="width: 170px" :options="registrationFilterOptions" />
      </div>
      <a-table :columns="registrationColumns" :data-source="filteredRegistrations" :loading="registrationLoading" row-key="id" :pagination="{ pageSize: 10 }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'product'">
            <div class="product-cell"><img :src="record.imageUrl || placeholder" /><div><strong>{{ record.productName }}</strong><div class="muted">{{ record.sku }} · {{ record.variantLabel }}</div></div></div>
          </template>
          <template v-else-if="column.key === 'price'">
            <div><s>{{ money(record.priceBeforeDiscount) }}</s></div><strong class="flash-price">{{ money(record.flashPrice) }}</strong> <a-tag color="red">-{{ record.discountPercent }}%</a-tag>
          </template>
          <template v-else-if="column.key === 'status'"><a-tag :color="registrationColor(record.registrationStatus)">{{ registrationLabel(record.registrationStatus) }}</a-tag><div v-if="record.rejectionReason" class="reject-reason">{{ record.rejectionReason }}</div></template>
          <template v-else-if="column.key === 'actions'">
            <a-space v-if="record.registrationStatus === 'PENDING'">
              <a-button size="small" type="primary" @click="approve(record)">Duyệt</a-button>
              <a-button size="small" danger @click="openReject(record)">Từ chối</a-button>
            </a-space>
            <span v-else class="muted">Đã xử lý</span>
          </template>
          <template v-else-if="column.dataIndex">{{ record[column.dataIndex] ?? '—' }}</template>
        </template>
      </a-table>
    </section>

    <a-modal v-model:open="campaignOpen" :title="editingId ? 'Cập nhật Flash sale' : 'Tạo Flash sale'" ok-text="Lưu" :confirm-loading="saving" @ok="saveCampaign">
      <a-form layout="vertical">
        <a-form-item label="Tên sự kiện" required><a-input v-model:value="form.name" placeholder="Flash Sale 9.9" /></a-form-item>
        <a-form-item label="Mô tả"><a-textarea v-model:value="form.description" :rows="2" /></a-form-item>
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="Mở đăng ký" required><a-input v-model:value="form.registrationStart" type="datetime-local" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="Đóng đăng ký" required><a-input v-model:value="form.registrationEnd" type="datetime-local" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="Bắt đầu sale" required><a-input v-model:value="form.start" type="datetime-local" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="Kết thúc sale" required><a-input v-model:value="form.end" type="datetime-local" /></a-form-item></a-col>
        </a-row>
        <a-alert type="info" show-icon message="Thời gian đăng ký phải kết thúc trước khi Flash sale bắt đầu." />
      </a-form>
    </a-modal>

    <a-modal v-model:open="rejectOpen" title="Từ chối đăng ký" ok-text="Xác nhận" ok-type="danger" @ok="reject">
      <a-textarea v-model:value="rejectReason" :rows="3" placeholder="Nhập lý do để seller chỉnh sửa và đăng ký lại" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  createFlashSale, getAdminFlashSales, getFlashSaleRegistrations, reviewFlashSaleRegistration, updateFlashSale,
  type FlashSaleCampaign, type FlashSaleProduct, type FlashSaleStatus, type RegistrationStatus
} from '@/services/api/flash-sale/flash-sale.api'

const campaigns = ref<FlashSaleCampaign[]>([])
const registrations = ref<FlashSaleProduct[]>([])
const selectedCampaign = ref<FlashSaleCampaign>()
const loading = ref(false)
const registrationLoading = ref(false)
const saving = ref(false)
const campaignOpen = ref(false)
const rejectOpen = ref(false)
const editingId = ref('')
const rejecting = ref<FlashSaleProduct>()
const rejectReason = ref('')
const registrationFilter = ref('ALL')
const placeholder = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="100"%3E%3Crect width="100%25" height="100%25" fill="%23f1f5f9"/%3E%3C/svg%3E'
const form = reactive({ name: '', description: '', registrationStart: '', registrationEnd: '', start: '', end: '' })

const campaignColumns = [
  { title: 'Sự kiện', key: 'name', width: 190 }, { title: 'Nhận đăng ký', key: 'registration', width: 230 },
  { title: 'Thời gian sale', key: 'saleTime', width: 220 }, { title: 'Trạng thái', key: 'status', width: 130 },
  { title: 'Sản phẩm', key: 'registrations', width: 150 }, { title: 'Thao tác', key: 'actions', width: 190 }
]
const registrationColumns = [
  { title: 'Sản phẩm / phân loại', key: 'product' }, { title: 'Giá đăng ký', key: 'price', width: 230 },
  { title: 'Tồn kho', dataIndex: 'quantity', width: 90 }, { title: 'Trạng thái', key: 'status', width: 180 },
  { title: 'Thao tác', key: 'actions', width: 170 }
]
const registrationFilterOptions = [
  { label: 'Tất cả', value: 'ALL' }, { label: 'Chờ duyệt', value: 'PENDING' }, { label: 'Đã duyệt', value: 'APPROVED' },
  { label: 'Từ chối', value: 'REJECTED' }, { label: 'Đã rút', value: 'WITHDRAWN' }
]
const pendingTotal = computed(() => campaigns.value.reduce((sum, row) => sum + Number(row.pendingCount || 0), 0))
const approvedTotal = computed(() => campaigns.value.reduce((sum, row) => sum + Number(row.approvedCount || 0), 0))
const filteredRegistrations = computed(() => registrationFilter.value === 'ALL' ? registrations.value : registrations.value.filter(row => row.registrationStatus === registrationFilter.value))

const load = async () => {
  loading.value = true
  try { campaigns.value = await getAdminFlashSales() } catch (error: any) { message.error(error?.response?.data?.message || 'Không tải được Flash sale') } finally { loading.value = false }
}
const viewRegistrations = async (campaign: FlashSaleCampaign) => {
  selectedCampaign.value = campaign
  registrationLoading.value = true
  try { registrations.value = await getFlashSaleRegistrations(campaign.id) } catch (error: any) { message.error(error?.response?.data?.message || 'Không tải được đăng ký') } finally { registrationLoading.value = false }
}
const openCampaign = (campaign?: FlashSaleCampaign) => {
  editingId.value = campaign?.id || ''
  Object.assign(form, campaign ? {
    name: campaign.name, description: campaign.description || '', registrationStart: inputDate(campaign.registrationStartDate),
    registrationEnd: inputDate(campaign.registrationEndDate), start: inputDate(campaign.startDate), end: inputDate(campaign.endDate)
  } : { name: '', description: '', registrationStart: inputDate(Date.now()), registrationEnd: inputDate(Date.now() + 86400000), start: inputDate(Date.now() + 172800000), end: inputDate(Date.now() + 176400000) })
  campaignOpen.value = true
}
const saveCampaign = async () => {
  const payload = { name: form.name.trim(), description: form.description.trim(), registrationStartDate: Date.parse(form.registrationStart), registrationEndDate: Date.parse(form.registrationEnd), startDate: Date.parse(form.start), endDate: Date.parse(form.end) }
  if (!payload.name || Object.values(payload).some(value => typeof value === 'number' && Number.isNaN(value))) { message.warning('Vui lòng nhập đủ thông tin'); return }
  saving.value = true
  try {
    if (editingId.value) await updateFlashSale(editingId.value, payload); else await createFlashSale(payload)
    message.success('Đã lưu Flash sale'); campaignOpen.value = false; await load()
  } catch (error: any) { message.error(error?.response?.data?.message || 'Không lưu được Flash sale') } finally { saving.value = false }
}
const approve = async (row: FlashSaleProduct) => {
  if (!selectedCampaign.value) return
  try { await reviewFlashSaleRegistration(selectedCampaign.value.id, row.id, 'APPROVE'); message.success('Đã duyệt sản phẩm'); await Promise.all([viewRegistrations(selectedCampaign.value), load()]) } catch (error: any) { message.error(error?.response?.data?.message || 'Không duyệt được') }
}
const openReject = (row: FlashSaleProduct) => { rejecting.value = row; rejectReason.value = ''; rejectOpen.value = true }
const reject = async () => {
  if (!selectedCampaign.value || !rejecting.value || !rejectReason.value.trim()) { message.warning('Vui lòng nhập lý do'); return }
  try { await reviewFlashSaleRegistration(selectedCampaign.value.id, rejecting.value.id, 'REJECT', rejectReason.value); message.success('Đã từ chối đăng ký'); rejectOpen.value = false; await Promise.all([viewRegistrations(selectedCampaign.value), load()]) } catch (error: any) { message.error(error?.response?.data?.message || 'Không xử lý được') }
}
const money = (value?: number) => `${Number(value || 0).toLocaleString('vi-VN')} ₫`
const dateTime = (value: number) => new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value))
const timeRange = (start: number, end: number) => `${dateTime(start)} → ${dateTime(end)}`
const inputDate = (value: number) => { const date = new Date(value - new Date().getTimezoneOffset() * 60000); return date.toISOString().slice(0, 16) }
const statusLabel = (value: FlashSaleStatus) => ({ CHUA_KICH_HOAT: 'Sắp diễn ra', DANG_KICH_HOAT: 'Đang diễn ra', HET_HAN_KICH_HOAT: 'Đã kết thúc' }[value])
const statusColor = (value: FlashSaleStatus) => ({ CHUA_KICH_HOAT: 'blue', DANG_KICH_HOAT: 'red', HET_HAN_KICH_HOAT: 'default' }[value])
const registrationLabel = (value: RegistrationStatus) => ({ PENDING: 'Chờ duyệt', APPROVED: 'Đã duyệt', REJECTED: 'Từ chối', WITHDRAWN: 'Đã rút' }[value])
const registrationColor = (value: RegistrationStatus) => ({ PENDING: 'orange', APPROVED: 'green', REJECTED: 'red', WITHDRAWN: 'default' }[value])

onMounted(load)
</script>

<style scoped>
.flash-admin-page { padding: 24px; }
.page-head, .section-head { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 18px; }
.page-head h2, .section-head h3 { margin: 0; font-weight: 750; }
.page-head p, .section-head p, .muted { color: #64748b; margin: 4px 0 0; font-size: 13px; }
.summary-row { margin-bottom: 18px; }
.registration-section { margin-top: 26px; padding: 20px; border: 1px solid #e5e7eb; border-radius: 12px; background: #fff; }
.product-cell { display: flex; gap: 10px; align-items: center; }
.product-cell img { width: 54px; height: 54px; border-radius: 8px; object-fit: cover; background: #f1f5f9; }
.flash-price, .pending-number { color: #dc2626; }
.reject-reason { margin-top: 4px; color: #b91c1c; font-size: 12px; }
@media (max-width: 760px) { .flash-admin-page { padding: 14px; } .page-head, .section-head { flex-direction: column; } }
</style>
