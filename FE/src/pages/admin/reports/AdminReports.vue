<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  adminReportDetail, adminReports, resolveReport, reviewReport,
  type ReportItem, type ReportTargetType
} from '@/services/api/report/report.api'

type ModerationAction = 'PRODUCT_DELISTED' | 'SHOP_SUSPENDED' | 'REVIEW_HIDDEN' | 'WARNING_SENT' | 'NO_ACTION'

const listLoading = ref(false)
const detailLoading = ref(false)
const actionLoading = ref(false)
const loadError = ref('')
const rows = ref<ReportItem[]>([])
const selected = ref<ReportItem | null>(null)
const filters = reactive({
  status: undefined as ReportItem['status'] | undefined,
  targetType: undefined as ReportTargetType | undefined,
  dateFrom: '', dateTo: ''
})
const resolution = reactive<{ actionTaken: ModerationAction; note: string }>({ actionTaken: 'WARNING_SENT', note: '' })
let detailRequest = 0

const statusLabels: Record<ReportItem['status'], string> = {
  PENDING: 'Chờ xử lý', REVIEWING: 'Đang xem xét', ACTION_TAKEN: 'Đã xử lý', DISMISSED: 'Không vi phạm'
}
const statusColors: Record<ReportItem['status'], string> = {
  PENDING: 'orange', REVIEWING: 'blue', ACTION_TAKEN: 'green', DISMISSED: 'default'
}
const targetLabels: Record<ReportTargetType, string> = {
  PRODUCT: 'Sản phẩm', SHOP: 'Shop', REVIEW: 'Đánh giá', USER: 'Người dùng'
}
const targetColors: Record<ReportTargetType, string> = {
  PRODUCT: 'purple', SHOP: 'cyan', REVIEW: 'geekblue', USER: 'magenta'
}
const reasonLabels: Record<string, string> = {
  FAKE_PRODUCT: 'Sản phẩm giả/không đúng mô tả', PROHIBITED_ITEM: 'Hàng hóa bị cấm',
  COPYRIGHT: 'Vi phạm bản quyền', FAKE_REVIEW: 'Đánh giá gian lận', SCAM: 'Lừa đảo',
  OFFENSIVE_CONTENT: 'Nội dung phản cảm', OTHER: 'Lý do khác'
}
const actionLabels: Record<string, string> = {
  PRODUCT_DELISTED: 'Ẩn sản phẩm', SHOP_SUSPENDED: 'Khóa shop', REVIEW_HIDDEN: 'Ẩn đánh giá',
  WARNING_SENT: 'Đã gửi cảnh báo', NO_ACTION: 'Không có vi phạm'
}
const targetFieldLabels: Record<string, string> = {
  id: 'Mã đối tượng', name: 'Tên', shopName: 'Tên shop', sellerSlug: 'Đường dẫn shop', sellerId: 'Mã shop',
  ownerCustomerId: 'Chủ shop', customerId: 'Người đánh giá', productId: 'Mã sản phẩm', email: 'Email',
  comment: 'Nội dung đánh giá', status: 'Trạng thái hiện tại', productRating: 'Điểm sản phẩm',
  shopRating: 'Điểm shop', createdAt: 'Ngày tạo', targetType: 'Loại đối tượng'
}

const actionsFor = (targetType?: ReportTargetType) => {
  const common = [
    { value: 'WARNING_SENT' as const, label: 'Gửi cảnh báo' },
    { value: 'NO_ACTION' as const, label: 'Kết luận không vi phạm' }
  ]
  if (targetType === 'PRODUCT') return [{ value: 'PRODUCT_DELISTED' as const, label: 'Ẩn sản phẩm' }, ...common]
  if (targetType === 'SHOP') return [{ value: 'SHOP_SUSPENDED' as const, label: 'Khóa shop' }, ...common]
  if (targetType === 'REVIEW') return [{ value: 'REVIEW_HIDDEN' as const, label: 'Ẩn đánh giá' }, ...common]
  return common
}
const allowedActions = computed(() => actionsFor(selected.value?.targetType))
const counts = computed(() => ({
  total: rows.value.length,
  pending: rows.value.filter(item => item.status === 'PENDING').length,
  reviewing: rows.value.filter(item => item.status === 'REVIEWING').length,
  completed: rows.value.filter(item => ['ACTION_TAKEN', 'DISMISSED'].includes(item.status)).length
}))
const targetUnavailable = computed(() => selected.value?.target?.unavailable === true)
const targetEntries = computed(() => Object.entries(selected.value?.target || {})
  .filter(([key, value]) => !['unavailable', 'lookupError'].includes(key) && value !== null && value !== undefined && value !== ''))
const safeEvidenceUrls = computed(() => (selected.value?.evidenceUrls || []).filter(url => /^https?:\/\//i.test(url)))
const actionImpact = computed(() => {
  if (resolution.actionTaken === 'PRODUCT_DELISTED') return 'Sản phẩm sẽ bị ẩn khỏi gian hàng và kết quả tìm kiếm.'
  if (resolution.actionTaken === 'SHOP_SUSPENDED') return 'Shop sẽ bị khóa và không thể tiếp tục hoạt động.'
  if (resolution.actionTaken === 'REVIEW_HIDDEN') return 'Đánh giá sẽ bị ẩn và điểm tổng hợp được tính lại.'
  if (resolution.actionTaken === 'WARNING_SENT') return 'Hệ thống sẽ gửi email cảnh báo đến người vi phạm.'
  return 'Báo cáo sẽ được đóng với kết luận không có vi phạm.'
})

const errorText = (error: any) => error?.response?.data?.message || error?.message || 'Không thể thực hiện thao tác'
const reasonLabel = (value: string) => reasonLabels[value] || value
const actionLabel = (value?: string) => value ? actionLabels[value] || value : 'Chưa có'
const time = (value?: string) => {
  if (!value) return '—'
  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? '—' : parsed.toLocaleString('vi-VN')
}
const targetTitle = (item: ReportItem) => item.target?.name || item.target?.shopName || item.target?.comment || item.target?.email || item.targetId
const displayValue = (key: string, value: unknown) => {
  if (key.toLowerCase().endsWith('at') && typeof value === 'string') return time(value)
  if (typeof value === 'boolean') return value ? 'Có' : 'Không'
  if (Array.isArray(value)) return value.join(', ')
  if (typeof value === 'object' && value !== null) return JSON.stringify(value)
  return String(value)
}

const load = async () => {
  if (filters.dateFrom && filters.dateTo && filters.dateFrom > filters.dateTo) {
    message.warning('Ngày bắt đầu không được sau ngày kết thúc')
    return
  }
  listLoading.value = true
  loadError.value = ''
  try {
    const result = await adminReports({ status: filters.status, targetType: filters.targetType, dateFrom: filters.dateFrom || undefined, dateTo: filters.dateTo || undefined })
    rows.value = Array.isArray(result) ? result : []
    if (selected.value && !rows.value.some(item => item.id === selected.value?.id)) selected.value = null
  } catch (error) {
    rows.value = []
    selected.value = null
    loadError.value = errorText(error)
  } finally { listLoading.value = false }
}
const resetFilters = () => {
  filters.status = undefined; filters.targetType = undefined; filters.dateFrom = ''; filters.dateTo = ''; load()
}
const open = async (id: string) => {
  const requestNumber = ++detailRequest
  detailLoading.value = true
  try {
    const detail = await adminReportDetail(id)
    if (requestNumber !== detailRequest) return
    selected.value = detail
    resolution.actionTaken = actionsFor(detail.targetType)[0]?.value || 'WARNING_SENT'
    resolution.note = ''
  } catch (error) {
    if (requestNumber === detailRequest) message.error(errorText(error))
  } finally {
    if (requestNumber === detailRequest) detailLoading.value = false
  }
}
const refreshAfterAction = async (updated: ReportItem) => {
  selected.value = updated
  await load()
  if (rows.value.some(item => item.id === updated.id)) selected.value = updated
}
const takeReview = async () => {
  if (!selected.value || actionLoading.value) return
  actionLoading.value = true
  try { await refreshAfterAction(await reviewReport(selected.value.id)); message.success('Đã tiếp nhận báo cáo') }
  catch (error) { message.error(errorText(error)) }
  finally { actionLoading.value = false }
}
const resolve = async () => {
  if (!selected.value || actionLoading.value) return
  const note = resolution.note.trim()
  if (!note) return message.warning('Vui lòng nhập căn cứ xử lý')
  actionLoading.value = true
  try {
    await refreshAfterAction(await resolveReport(selected.value.id, { actionTaken: resolution.actionTaken, note }))
    message.success('Đã hoàn tất kiểm duyệt')
  } catch (error) { message.error(errorText(error)) }
  finally { actionLoading.value = false }
}
onMounted(load)
</script>

<template>
  <section class="reports-page">
    <header class="page-header">
      <div><p class="eyebrow">TRUST &amp; SAFETY</p><h1>Kiểm duyệt nội dung</h1><p>Tiếp nhận, xác minh và xử lý báo cáo vi phạm trên toàn sàn.</p></div>
      <a-button :loading="listLoading" @click="load">Làm mới dữ liệu</a-button>
    </header>
    <div class="stats">
      <div><small>Tổng hồ sơ</small><b>{{ counts.total }}</b></div>
      <div><small>Chờ xử lý</small><b class="orange">{{ counts.pending }}</b></div>
      <div><small>Đang xem xét</small><b class="blue">{{ counts.reviewing }}</b></div>
      <div><small>Đã kết luận</small><b class="green">{{ counts.completed }}</b></div>
    </div>
    <div class="filters">
      <a-select v-model:value="filters.status" allow-clear placeholder="Tất cả trạng thái" @change="load"><a-select-option v-for="(label,key) in statusLabels" :key="key" :value="key">{{ label }}</a-select-option></a-select>
      <a-select v-model:value="filters.targetType" allow-clear placeholder="Tất cả đối tượng" @change="load"><a-select-option v-for="(label,key) in targetLabels" :key="key" :value="key">{{ label }}</a-select-option></a-select>
      <a-input v-model:value="filters.dateFrom" type="date" aria-label="Từ ngày" />
      <a-input v-model:value="filters.dateTo" type="date" aria-label="Đến ngày" />
      <a-button type="primary" :loading="listLoading" @click="load">Lọc</a-button>
      <a-button @click="resetFilters">Xóa lọc</a-button>
    </div>
    <a-alert v-if="loadError" class="load-error" type="error" show-icon :message="loadError">
      <template #action><a-button size="small" danger @click="load">Thử lại</a-button></template>
    </a-alert>
    <div class="grid">
      <div class="list">
        <a-spin :spinning="listLoading">
          <div class="case-list">
            <button v-for="item in rows" :key="item.id" type="button" class="case" :class="{ active: selected?.id === item.id }" @click="open(item.id)">
              <span class="case-top"><a-tag :color="targetColors[item.targetType]">{{ targetLabels[item.targetType] }}</a-tag><a-tag :color="statusColors[item.status]">{{ statusLabels[item.status] }}</a-tag></span>
              <b>{{ reasonLabel(item.reasonCode) }}</b>
              <small>{{ item.reporterType === 'BUYER' ? 'Người mua' : 'Người bán' }} · {{ time(item.createdAt) }}</small>
              <code>{{ item.targetId }}</code>
            </button>
            <a-empty v-if="!listLoading && !rows.length && !loadError" description="Không có báo cáo phù hợp" />
          </div>
        </a-spin>
      </div>
      <article v-if="selected" class="detail">
        <a-spin :spinning="detailLoading || actionLoading">
          <div class="detail-head">
            <div><a-tag :color="targetColors[selected.targetType]">{{ targetLabels[selected.targetType] }}</a-tag><h2>{{ targetTitle(selected) }}</h2><code>{{ selected.targetId }}</code></div>
            <a-tag :color="statusColors[selected.status]">{{ statusLabels[selected.status] }}</a-tag>
          </div>
          <div class="facts">
            <div><small>Người báo cáo</small><b>{{ selected.reporterType === 'BUYER' ? 'Người mua' : 'Người bán' }}</b><span>{{ selected.reporterId }}</span></div>
            <div><small>Lý do</small><b>{{ reasonLabel(selected.reasonCode) }}</b></div>
            <div><small>Ngày gửi</small><b>{{ time(selected.createdAt) }}</b></div>
            <div><small>Người xử lý</small><b>{{ selected.reviewedByStaffId || 'Chưa tiếp nhận' }}</b><span v-if="selected.reviewedAt">{{ time(selected.reviewedAt) }}</span></div>
          </div>
          <section class="content-section"><h3>Mô tả báo cáo</h3><p class="description">{{ selected.description || 'Người báo cáo không nhập mô tả.' }}</p></section>
          <section v-if="selected.evidenceUrls?.length" class="content-section">
            <h3>Bằng chứng</h3>
            <div v-if="safeEvidenceUrls.length" class="evidence"><a v-for="(url,index) in safeEvidenceUrls" :key="url" :href="url" target="_blank" rel="noopener noreferrer">Mở bằng chứng {{ index + 1 }}</a></div>
            <a-alert v-else type="warning" show-icon message="Các liên kết bằng chứng không hợp lệ hoặc không an toàn." />
          </section>
          <section class="content-section">
            <h3>Dữ liệu đối tượng</h3>
            <a-alert v-if="targetUnavailable" type="warning" show-icon message="Không tải được dữ liệu hiện tại của đối tượng" description="Đối tượng có thể đã bị xóa hoặc dịch vụ liên quan đang tạm gián đoạn. Hồ sơ báo cáo vẫn được giữ nguyên." />
            <a-descriptions v-else bordered size="small" :column="1"><a-descriptions-item v-for="([key,value]) in targetEntries" :key="key" :label="targetFieldLabels[key] || key">{{ displayValue(key, value) }}</a-descriptions-item></a-descriptions>
          </section>
          <section class="actions">
            <template v-if="selected.status === 'PENDING'">
              <h3>Tiếp nhận hồ sơ</h3><p>Đánh dấu hồ sơ đang được xem xét trước khi đưa ra kết luận.</p>
              <a-button type="primary" :loading="actionLoading" @click="takeReview">Tiếp nhận xem xét</a-button>
            </template>
            <template v-else-if="selected.status === 'REVIEWING'">
              <h3>Kết luận kiểm duyệt</h3>
              <a-select v-model:value="resolution.actionTaken"><a-select-option v-for="item in allowedActions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option></a-select>
              <a-alert type="info" show-icon :message="actionImpact" />
              <a-textarea v-model:value="resolution.note" :rows="4" :maxlength="2000" show-count placeholder="Nhập căn cứ xác minh, ghi chú xử lý hoặc nội dung cảnh báo" />
              <a-popconfirm title="Xác nhận chốt kết quả?" description="Hành động đã chọn sẽ được thực thi và hồ sơ được đóng." ok-text="Xác nhận" cancel-text="Kiểm tra lại" @confirm="resolve">
                <a-button type="primary" :danger="resolution.actionTaken !== 'NO_ACTION'" :loading="actionLoading" :disabled="!resolution.note.trim()">Xác nhận xử lý</a-button>
              </a-popconfirm>
            </template>
            <a-alert v-else type="success" show-icon :message="`Kết luận: ${actionLabel(selected.actionTaken)}`" :description="selected.resolutionNote || 'Không có ghi chú xử lý.'" />
          </section>
        </a-spin>
      </article>
      <div v-else class="empty"><a-empty description="Chọn một báo cáo để xem và xử lý" /></div>
    </div>
  </section>
</template>

<style scoped>
.reports-page{padding:24px;color:#172033;background:#f6f8fb;min-height:100%}.page-header{display:flex;justify-content:space-between;gap:20px;align-items:start;margin-bottom:18px}.page-header h1{font-size:28px;margin:2px 0}.page-header p{margin:0;color:#667085}.eyebrow{color:#2699bb!important;font-size:12px!important;font-weight:800;letter-spacing:.12em}.stats{display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-bottom:16px}.stats div{display:flex;align-items:center;justify-content:space-between;padding:14px 16px;background:#fff;border:1px solid #e1e7ef;border-radius:12px}.stats small{color:#667085}.stats b{font-size:22px}.stats .orange{color:#d97706}.stats .blue{color:#1677ff}.stats .green{color:#16a34a}.filters{display:flex;flex-wrap:wrap;gap:10px;margin-bottom:16px;padding:14px;background:#fff;border:1px solid #e1e7ef;border-radius:12px}.filters .ant-select,.filters .ant-input{min-width:170px}.load-error{margin-bottom:16px}.grid{display:grid;grid-template-columns:minmax(330px,38%) 1fr;gap:16px;min-height:590px}.list,.detail,.empty{background:#fff;border:1px solid #e1e7ef;border-radius:14px;padding:16px;min-width:0}.case-list{display:flex;flex-direction:column;gap:10px;min-height:500px}.case{display:grid;gap:8px;width:100%;padding:14px;border:1px solid #e4e9f0;border-radius:10px;background:#f8fafc;text-align:left;color:#344054;cursor:pointer;transition:.15s ease}.case-top,.detail-head{display:flex;justify-content:space-between;gap:10px;align-items:start}.case.active,.case:hover{border-color:#54bddb;background:#f0fbfe;box-shadow:0 2px 8px rgba(35,130,160,.08)}.case:focus-visible{outline:2px solid #1677ff;outline-offset:2px}.case small,.case code,.detail code,.facts span{color:#667085;font-size:12px}.detail-head h2{margin:10px 0 5px;font-size:23px;overflow-wrap:anywhere}.facts{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:10px;margin:20px 0}.facts div{display:grid;align-content:start;gap:5px;padding:12px;background:#f8fafc;border-radius:9px;overflow-wrap:anywhere}.facts small{color:#667085}.content-section{margin:18px 0}.content-section h3,.actions h3{margin:0 0 10px;font-size:16px}.description{margin:0;padding:12px;background:#f8fafc;border-radius:9px;white-space:pre-wrap;overflow-wrap:anywhere}.evidence,.actions{display:grid;gap:8px}.actions{margin-top:20px;padding-top:18px;border-top:1px solid #e5e7eb}.actions>p{margin:0;color:#667085}.actions>.ant-btn,.actions>.ant-select{width:100%}.empty{display:grid;place-items:center}@media(max-width:1000px){.grid{grid-template-columns:1fr}.stats{grid-template-columns:1fr 1fr}}@media(max-width:600px){.reports-page{padding:14px}.page-header{flex-direction:column}.stats{grid-template-columns:1fr}.filters>*{width:100%!important}.facts{grid-template-columns:1fr}}
</style>
