<template>
  <div class="seller-flash-page">
    <div class="page-head">
      <div><h2>Flash sale sàn</h2><p>Đăng ký phân loại sản phẩm và giá ưu đãi để Platform Admin xét duyệt.</p></div>
      <a-button @click="load">Tải lại</a-button>
    </div>

    <a-tabs v-model:activeKey="tab">
      <a-tab-pane key="campaigns" tab="Sự kiện đang mở">
        <a-empty v-if="!loading && !campaigns.length" description="Chưa có Flash sale sàn sắp diễn ra" />
        <div class="campaign-grid">
          <a-card v-for="campaign in campaigns" :key="campaign.id" class="campaign-card">
            <template #title><span class="fire">⚡</span> {{ campaign.name }}</template>
            <template #extra><a-tag :color="campaign.registrationOpen ? 'green' : 'default'">{{ campaign.registrationOpen ? 'Đang nhận đăng ký' : 'Đã đóng đăng ký' }}</a-tag></template>
            <p>{{ campaign.description || 'Sự kiện Flash sale toàn sàn' }}</p>
            <dl><dt>Nhận đăng ký</dt><dd>{{ range(campaign.registrationStartDate, campaign.registrationEndDate) }}</dd><dt>Thời gian sale</dt><dd>{{ range(campaign.startDate, campaign.endDate) }}</dd><dt>Shop đã đăng ký</dt><dd>{{ campaign.sellerRegistrationCount || 0 }} sản phẩm</dd></dl>
            <a-button type="primary" block :disabled="!campaign.registrationOpen" @click="openRegister(campaign)">Đăng ký sản phẩm</a-button>
          </a-card>
        </div>
      </a-tab-pane>
      <a-tab-pane key="registrations" :tab="`Sản phẩm đã đăng ký (${registrations.length})`">
        <a-table :columns="columns" :data-source="registrations" :loading="loading" row-key="id" :pagination="{ pageSize: 10 }">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'product'"><div class="product-cell"><img :src="record.imageUrl || placeholder" /><div><strong>{{ record.productName }}</strong><div class="muted">{{ record.sku }} · {{ record.variantLabel }}</div><div class="muted">{{ record.campaignName }}</div></div></div></template>
            <template v-else-if="column.key === 'price'"><s>{{ money(record.priceBeforeDiscount) }}</s><br /><strong class="flash-price">{{ money(record.flashPrice) }}</strong> <a-tag color="red">-{{ record.discountPercent }}%</a-tag></template>
            <template v-else-if="column.key === 'status'"><a-tag :color="registrationColor(record.registrationStatus)">{{ registrationLabel(record.registrationStatus) }}</a-tag><div v-if="record.rejectionReason" class="reason">{{ record.rejectionReason }}</div></template>
            <template v-else-if="column.key === 'actions'"><a-button v-if="record.registrationStatus !== 'WITHDRAWN'" size="small" danger @click="withdraw(record)">Rút đăng ký</a-button></template>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>

    <a-modal v-model:open="registerOpen" :title="`Đăng ký — ${selectedCampaign?.name || ''}`" ok-text="Gửi duyệt" :confirm-loading="submitting" @ok="submit">
      <a-form layout="vertical">
        <a-form-item label="Phân loại sản phẩm" required>
          <a-select v-model:value="form.productVariantId" show-search option-filter-prop="label" placeholder="Chọn SKU đang bán" :options="variantOptions" @change="syncPrice" />
        </a-form-item>
        <div v-if="selectedVariant" class="variant-preview">
          <img :src="selectedVariant.imageUrl || placeholder" /><div><strong>{{ selectedVariant.productName }}</strong><div>{{ selectedVariant.variantLabel }} · {{ selectedVariant.sku }}</div><div>Tồn kho: {{ selectedVariant.quantity }} · Giá bán: {{ money(selectedVariant.salePrice) }}</div></div>
        </div>
        <a-form-item label="Giá Flash sale" required>
          <a-input-number v-model:value="form.flashPrice" :min="1" :max="Math.max(1, (selectedVariant?.salePrice || 1) - 1)" :step="1000" style="width: 100%" />
        </a-form-item>
        <a-alert v-if="selectedVariant && form.flashPrice" :type="form.flashPrice < selectedVariant.salePrice ? 'success' : 'warning'" show-icon :message="discountMessage" />
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  getSellerFlashRegistrations, getSellerFlashSales, getSellerFlashVariants, registerFlashSaleProduct, withdrawFlashSaleRegistration,
  type FlashSaleCampaign, type FlashSaleProduct, type RegistrationStatus, type SellerVariant
} from '@/services/api/flash-sale/flash-sale.api'

const tab = ref('campaigns')
const loading = ref(false)
const submitting = ref(false)
const registerOpen = ref(false)
const campaigns = ref<FlashSaleCampaign[]>([])
const registrations = ref<FlashSaleProduct[]>([])
const variants = ref<SellerVariant[]>([])
const selectedCampaign = ref<FlashSaleCampaign>()
const form = reactive({ productVariantId: '', flashPrice: 0 })
const placeholder = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="100"%3E%3Crect width="100%25" height="100%25" fill="%23f1f5f9"/%3E%3C/svg%3E'
const columns = [
  { title: 'Sản phẩm / sự kiện', key: 'product' }, { title: 'Giá Flash sale', key: 'price', width: 230 },
  { title: 'Trạng thái', key: 'status', width: 190 }, { title: 'Thao tác', key: 'actions', width: 120 }
]
const selectedVariant = computed(() => variants.value.find(row => row.productVariantId === form.productVariantId))
const variantOptions = computed(() => variants.value.map(row => ({ value: row.productVariantId, label: `${row.productName} · ${row.variantLabel} · ${row.sku} · ${money(row.salePrice)}` })))
const discountMessage = computed(() => {
  if (!selectedVariant.value || !form.flashPrice) return ''
  const percent = Math.round((selectedVariant.value.salePrice - form.flashPrice) * 10000 / selectedVariant.value.salePrice) / 100
  return form.flashPrice < selectedVariant.value.salePrice ? `Giảm ${percent}% so với giá đang bán` : 'Giá Flash sale phải thấp hơn giá đang bán'
})

const load = async () => {
  loading.value = true
  try {
    const [campaignRows, registrationRows, variantRows] = await Promise.all([getSellerFlashSales(), getSellerFlashRegistrations(), getSellerFlashVariants()])
    campaigns.value = campaignRows; registrations.value = registrationRows; variants.value = variantRows
  } catch (error: any) { message.error(error?.response?.data?.message || 'Không tải được Flash sale') } finally { loading.value = false }
}
const openRegister = (campaign: FlashSaleCampaign) => { selectedCampaign.value = campaign; form.productVariantId = ''; form.flashPrice = 0; registerOpen.value = true }
const syncPrice = () => { form.flashPrice = selectedVariant.value ? Math.floor(selectedVariant.value.salePrice * 0.9 / 1000) * 1000 : 0 }
const submit = async () => {
  if (!selectedCampaign.value || !selectedVariant.value || !form.flashPrice) { message.warning('Vui lòng chọn sản phẩm và nhập giá'); return }
  submitting.value = true
  try { await registerFlashSaleProduct(selectedCampaign.value.id, form.productVariantId, form.flashPrice); message.success('Đã gửi sản phẩm chờ Admin duyệt'); registerOpen.value = false; tab.value = 'registrations'; await load() } catch (error: any) { message.error(error?.response?.data?.message || 'Không đăng ký được') } finally { submitting.value = false }
}
const withdraw = (row: FlashSaleProduct) => Modal.confirm({ title: 'Rút sản phẩm khỏi Flash sale?', content: `${row.productName} — ${row.campaignName}`, okText: 'Rút đăng ký', okType: 'danger', cancelText: 'Hủy', async onOk() { try { await withdrawFlashSaleRegistration(row.campaignId, row.id); message.success('Đã rút đăng ký'); await load() } catch (error: any) { message.error(error?.response?.data?.message || 'Không rút được đăng ký') } } })
const money = (value?: number) => `${Number(value || 0).toLocaleString('vi-VN')} ₫`
const dateTime = (value: number) => new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value))
const range = (start: number, end: number) => `${dateTime(start)} → ${dateTime(end)}`
const registrationLabel = (value: RegistrationStatus) => ({ PENDING: 'Chờ Admin duyệt', APPROVED: 'Đã duyệt', REJECTED: 'Bị từ chối', WITHDRAWN: 'Đã rút' }[value])
const registrationColor = (value: RegistrationStatus) => ({ PENDING: 'orange', APPROVED: 'green', REJECTED: 'red', WITHDRAWN: 'default' }[value])

onMounted(load)
</script>

<style scoped>
.seller-flash-page { padding: 24px; }
.page-head { display: flex; justify-content: space-between; gap: 16px; margin-bottom: 12px; }
.page-head h2 { margin: 0; font-weight: 750; }.page-head p, .muted { color: #64748b; margin: 4px 0 0; font-size: 13px; }
.campaign-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(310px, 1fr)); gap: 16px; }
.campaign-card { border-top: 3px solid #ef4444; }.campaign-card dl { display: grid; grid-template-columns: 110px 1fr; gap: 7px; font-size: 13px; }.campaign-card dt { color: #64748b; }.campaign-card dd { margin: 0; }.fire, .flash-price { color: #dc2626; }
.product-cell, .variant-preview { display: flex; align-items: center; gap: 10px; }.product-cell img, .variant-preview img { width: 56px; height: 56px; object-fit: cover; border-radius: 8px; background: #f1f5f9; }.reason { margin-top: 4px; color: #b91c1c; font-size: 12px; }
.variant-preview { padding: 10px; margin-bottom: 14px; border-radius: 8px; background: #f8fafc; font-size: 13px; }
@media (max-width: 760px) { .seller-flash-page { padding: 14px; }.page-head { flex-direction: column; } }
</style>
