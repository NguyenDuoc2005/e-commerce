<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { adminReportDetail, adminReports, resolveReport, reviewReport, type ReportItem } from '@/services/api/report/report.api'

const loading = ref(false)
const rows = ref<ReportItem[]>([])
const selected = ref<ReportItem | null>(null)
const filters = reactive({ status: undefined as string | undefined, targetType: undefined as string | undefined, dateFrom: '', dateTo: '' })
const resolution = reactive({ actionTaken: 'WARNING_SENT', note: '' })
const statusLabels: Record<string, string> = { PENDING: 'Chờ xử lý', REVIEWING: 'Đang xem xét', ACTION_TAKEN: 'Đã xử lý', DISMISSED: 'Bác bỏ' }
const targetLabels: Record<string, string> = { PRODUCT: 'Sản phẩm', SHOP: 'Shop', REVIEW: 'Đánh giá', USER: 'Người dùng' }
const allowedActions = computed(() => {
  const common = [{ value: 'WARNING_SENT', label: 'Gửi cảnh báo' }, { value: 'NO_ACTION', label: 'Không vi phạm' }]
  if (selected.value?.targetType === 'PRODUCT') return [{ value: 'PRODUCT_DELISTED', label: 'Ẩn sản phẩm' }, ...common]
  if (selected.value?.targetType === 'SHOP') return [{ value: 'SHOP_SUSPENDED', label: 'Khóa shop' }, ...common]
  if (selected.value?.targetType === 'REVIEW') return [{ value: 'REVIEW_HIDDEN', label: 'Ẩn đánh giá' }, ...common]
  return common
})
const time = (value?: string) => value ? new Date(value).toLocaleString('vi-VN') : '—'
const errorText = (error: any) => error?.response?.data?.message || 'Không thể thực hiện thao tác'
const targetTitle = (item: ReportItem) => item.target?.name || item.target?.shopName || item.target?.comment || item.target?.email || item.targetId

const load = async () => {
  loading.value = true
  try { rows.value = await adminReports({ status: filters.status, targetType: filters.targetType, dateFrom: filters.dateFrom || undefined, dateTo: filters.dateTo || undefined }) }
  catch (error) { message.error(errorText(error)) } finally { loading.value = false }
}
const open = async (id: string) => {
  loading.value = true
  try { selected.value = await adminReportDetail(id); resolution.actionTaken = allowedActions.value[0]?.value || 'WARNING_SENT'; resolution.note = '' }
  catch (error) { message.error(errorText(error)) } finally { loading.value = false }
}
const takeReview = async () => {
  if (!selected.value) return
  try { selected.value = await reviewReport(selected.value.id); await load(); message.success('Đã tiếp nhận báo cáo') }
  catch (error) { message.error(errorText(error)) }
}
const resolve = async () => {
  if (!selected.value || !resolution.note.trim()) return message.warning('Vui lòng nhập căn cứ xử lý')
  try { selected.value = await resolveReport(selected.value.id, resolution); await load(); message.success('Đã hoàn tất kiểm duyệt') }
  catch (error) { message.error(errorText(error)) }
}
onMounted(load)
</script>

<template>
  <section class="reports-page">
    <header><div><p class="eyebrow">TRUST & SAFETY</p><h1>Kiểm duyệt nội dung</h1><p>Xử lý báo cáo sản phẩm, shop, đánh giá và người dùng trên toàn sàn.</p></div><a-button @click="load">Làm mới</a-button></header>
    <div class="filters">
      <a-select v-model:value="filters.status" allow-clear placeholder="Trạng thái"><a-select-option v-for="(label,key) in statusLabels" :key="key" :value="key">{{ label }}</a-select-option></a-select>
      <a-select v-model:value="filters.targetType" allow-clear placeholder="Đối tượng"><a-select-option v-for="(label,key) in targetLabels" :key="key" :value="key">{{ label }}</a-select-option></a-select>
      <a-input v-model:value="filters.dateFrom" type="date" /><a-input v-model:value="filters.dateTo" type="date" /><a-button type="primary" @click="load">Lọc</a-button>
    </div>
    <div class="grid">
      <div class="list">
        <button v-for="item in rows" :key="item.id" class="case" :class="{ active: selected?.id === item.id }" @click="open(item.id)">
          <span><b>{{ targetLabels[item.targetType] }} · {{ item.reasonCode }}</b><a-tag color="blue">{{ statusLabels[item.status] }}</a-tag></span>
          <small>{{ item.reporterType }} · {{ time(item.createdAt) }}</small><em>{{ item.targetId }}</em>
        </button>
        <a-empty v-if="!loading && !rows.length" description="Không có báo cáo" />
      </div>
      <article v-if="selected" class="detail">
        <div class="detail-head"><div><small>{{ targetLabels[selected.targetType] }}</small><h2>{{ targetTitle(selected) }}</h2></div><a-tag color="cyan">{{ statusLabels[selected.status] }}</a-tag></div>
        <div class="facts"><div><small>Người báo cáo</small><b>{{ selected.reporterType }} · {{ selected.reporterId }}</b></div><div><small>Lý do</small><b>{{ selected.reasonCode }}</b></div><div><small>Thời gian</small><b>{{ time(selected.createdAt) }}</b></div></div>
        <h3>Mô tả</h3><p class="description">{{ selected.description || 'Không có mô tả' }}</p>
        <div v-if="selected.evidenceUrls?.length" class="evidence"><b>Bằng chứng</b><a v-for="url in selected.evidenceUrls" :key="url" :href="url" target="_blank">{{ url }}</a></div>
        <a-descriptions title="Dữ liệu đối tượng" bordered size="small" :column="1">
          <a-descriptions-item v-for="(value,key) in selected.target" :key="key" :label="String(key)">{{ typeof value === 'object' ? JSON.stringify(value) : value }}</a-descriptions-item>
        </a-descriptions>
        <div class="actions">
          <a-button v-if="selected.status === 'PENDING'" type="primary" @click="takeReview">Tiếp nhận xem xét</a-button>
          <template v-if="selected.status === 'REVIEWING'">
            <a-select v-model:value="resolution.actionTaken"><a-select-option v-for="item in allowedActions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option></a-select>
            <a-textarea v-model:value="resolution.note" :rows="4" placeholder="Căn cứ, ghi chú và nội dung cảnh báo nếu có" />
            <a-button type="primary" danger @click="resolve">Xác nhận xử lý</a-button>
          </template>
          <a-alert v-if="selected.resolutionNote" type="info" show-icon :message="`${selected.actionTaken}: ${selected.resolutionNote}`" />
        </div>
      </article>
      <div v-else class="empty"><a-empty description="Chọn báo cáo để xem chi tiết" /></div>
    </div>
  </section>
</template>

<style scoped>
.reports-page{padding:24px;color:#172033}.reports-page header{display:flex;justify-content:space-between;align-items:start;margin-bottom:20px}.reports-page h1{font-size:28px;margin:2px 0}.reports-page header p{margin:0;color:#667085}.eyebrow{color:#54bddb!important;font-size:12px!important;font-weight:800;letter-spacing:.12em}.filters{display:flex;gap:10px;margin-bottom:16px}.filters>*{min-width:160px}.grid{display:grid;grid-template-columns:minmax(320px,38%) 1fr;gap:16px;min-height:570px}.list,.detail,.empty{background:#fff;border:1px solid #e1e7ef;border-radius:14px;padding:16px}.list{display:flex;flex-direction:column;gap:10px}.case{display:grid;gap:7px;padding:14px;border:1px solid #e4e9f0;border-radius:10px;background:#f8fafc;text-align:left}.case>span{display:flex;justify-content:space-between;gap:8px}.case.active,.case:hover{border-color:#54bddb;background:#f0fbfe}.case small,.case em{color:#667085;font-style:normal}.detail-head{display:flex;justify-content:space-between}.detail-head h2{margin:4px 0 16px}.facts{display:grid;grid-template-columns:repeat(3,1fr);gap:10px}.facts div{display:grid;gap:5px;padding:12px;background:#f8fafc;border-radius:9px}.facts small{color:#667085}.description{white-space:pre-wrap}.evidence,.actions{display:grid;gap:8px;margin:16px 0}.actions{padding-top:16px;border-top:1px solid #e5e7eb}.empty{display:grid;place-items:center}@media(max-width:900px){.grid{grid-template-columns:1fr}.filters{flex-wrap:wrap}.facts{grid-template-columns:1fr}}
</style>
