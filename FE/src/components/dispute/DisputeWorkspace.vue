<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  adminDisputeDetail, adminDisputes, buyerDisputeDetail, buyerDisputes, closeDispute,
  respondSellerDispute, resolveDispute, sellerDisputeDetail, sellerDisputes,
  sendAdminDisputeMessage, sendBuyerDisputeMessage, takeDisputeReview, type Dispute, type DisputeStatus
} from '@/services/api/dispute/dispute.api'

const props = defineProps<{ role: 'buyer' | 'seller' | 'admin' }>()
const loading = ref(false)
const detailLoading = ref(false)
const actionLoading = ref<string>()
const rows = ref<Dispute[]>([])
const selected = ref<Dispute | null>(null)
const status = ref<DisputeStatus>()
const search = ref('')
const sellerId = ref('')
const dateFrom = ref('')
const dateTo = ref('')
const currentPage = ref(1)
const pageSize = 8
const reply = reactive({ message: '', attachmentText: '' })
const resolution = reactive({
  decision: 'REFUND_BUYER' as 'REFUND_BUYER' | 'PARTIAL_REFUND' | 'REJECT_BUYER',
  resolvedAmount: undefined as number | undefined,
  note: ''
})

const statusOptions: Array<{ value: DisputeStatus; label: string; color: string }> = [
  { value: 'OPEN', label: 'Mới mở', color: 'orange' },
  { value: 'SELLER_RESPONDED', label: 'Seller đã phản hồi', color: 'blue' },
  { value: 'UNDER_ADMIN_REVIEW', label: 'Đang xem xét', color: 'purple' },
  { value: 'RESOLVED_REFUND_BUYER', label: 'Hoàn toàn bộ', color: 'green' },
  { value: 'RESOLVED_PARTIAL_REFUND', label: 'Hoàn một phần', color: 'cyan' },
  { value: 'RESOLVED_REJECT_BUYER', label: 'Từ chối yêu cầu', color: 'red' },
  { value: 'CLOSED', label: 'Đã đóng', color: 'default' }
]
const typeLabels: Record<string, string> = {
  ITEM_NOT_RECEIVED: 'Chưa nhận được hàng', ITEM_DAMAGED: 'Hàng hư hỏng', WRONG_ITEM: 'Giao sai sản phẩm',
  NOT_AS_DESCRIBED: 'Không đúng mô tả', REFUND_REQUEST: 'Yêu cầu hoàn tiền', OTHER: 'Vấn đề khác'
}
const senderLabels: Record<string, string> = { BUYER: 'Người mua', SELLER: 'Nhà bán', ADMIN: 'Quản trị viên' }
const title = computed(() => props.role === 'admin' ? 'Xử lý tranh chấp' : props.role === 'seller' ? 'Tranh chấp với người mua' : 'Khiếu nại của tôi')
const subtitle = computed(() => props.role === 'admin'
  ? 'Tiếp nhận hồ sơ, đối chiếu bằng chứng và đưa ra quyết định hoàn tiền minh bạch.'
  : 'Theo dõi toàn bộ trao đổi và kết quả xử lý theo từng đơn hàng.')
const terminal = computed(() => selected.value ? ['RESOLVED_REFUND_BUYER', 'RESOLVED_REJECT_BUYER', 'RESOLVED_PARTIAL_REFUND', 'CLOSED'].includes(selected.value.status) : false)
const orderTotal = computed(() => Number(selected.value?.order?.totalAfterDiscount || 0))
const maxPartialAmount = computed(() => Math.max(0, orderTotal.value - 1))
const filteredRows = computed(() => {
  const keyword = search.value.trim().toLocaleLowerCase('vi-VN')
  if (!keyword) return rows.value
  return rows.value.filter(item => [item.reason, item.id, item.orderId, item.orderSellerId, item.sellerId, typeLabel(item.disputeType), statusMeta(item.status).label]
    .some(value => String(value || '').toLocaleLowerCase('vi-VN').includes(keyword)))
})
const pagedRows = computed(() => filteredRows.value.slice((currentPage.value - 1) * pageSize, currentPage.value * pageSize))
const metrics = computed(() => ({
  total: rows.value.length,
  waiting: rows.value.filter(item => ['OPEN', 'SELLER_RESPONDED'].includes(item.status)).length,
  reviewing: rows.value.filter(item => item.status === 'UNDER_ADMIN_REVIEW').length,
  completed: rows.value.filter(item => ['RESOLVED_REFUND_BUYER', 'RESOLVED_REJECT_BUYER', 'RESOLVED_PARTIAL_REFUND', 'CLOSED'].includes(item.status)).length
}))

watch(search, () => { currentPage.value = 1 })
watch(() => resolution.decision, decision => { if (decision !== 'PARTIAL_REFUND') resolution.resolvedAmount = undefined })

const statusMeta = (value: string) => statusOptions.find(item => item.value === value) || { label: value, color: 'default' }
const typeLabel = (value?: string) => value ? typeLabels[value] || value : '—'
const errorText = (error: any, fallback = 'Không thể thực hiện thao tác') => error?.response?.data?.message || error?.response?.data?.error || error?.message || fallback
const attachments = (value: string) => [...new Set(value.split('\n').map(item => item.trim()).filter(Boolean))]
const money = (value?: number | null) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(Number(value || 0))
const time = (value?: string) => {
  if (!value) return '—'
  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? '—' : parsed.toLocaleString('vi-VN')
}
const resetForms = () => {
  reply.message = ''; reply.attachmentText = ''; resolution.decision = 'REFUND_BUYER'; resolution.resolvedAmount = undefined; resolution.note = ''
}

const load = async () => {
  if (dateFrom.value && dateTo.value && dateFrom.value > dateTo.value) return message.warning('Ngày bắt đầu không được sau ngày kết thúc')
  loading.value = true
  try {
    rows.value = props.role === 'admin'
      ? await adminDisputes({ status: status.value, sellerId: sellerId.value.trim() || undefined, dateFrom: dateFrom.value || undefined, dateTo: dateTo.value || undefined })
      : props.role === 'seller' ? await sellerDisputes(status.value) : await buyerDisputes(status.value)
    currentPage.value = 1
    if (selected.value && !rows.value.some(item => item.id === selected.value?.id)) selected.value = null
  } catch (error) { message.error(errorText(error, 'Không thể tải danh sách tranh chấp')) }
  finally { loading.value = false }
}

const openCase = async (id: string) => {
  detailLoading.value = true; resetForms()
  try {
    selected.value = props.role === 'admin' ? await adminDisputeDetail(id) : props.role === 'seller' ? await sellerDisputeDetail(id) : await buyerDisputeDetail(id)
  } catch (error) { message.error(errorText(error, 'Không thể tải chi tiết tranh chấp')) }
  finally { detailLoading.value = false }
}

const refresh = async () => {
  const selectedId = selected.value?.id
  await load()
  if (selectedId && rows.value.some(item => item.id === selectedId)) await openCase(selectedId)
}
const resetFilters = async () => {
  status.value = undefined; sellerId.value = ''; dateFrom.value = ''; dateTo.value = ''; search.value = ''; await load()
}

const sendReply = async () => {
  if (!selected.value || !reply.message.trim()) return message.warning('Vui lòng nhập nội dung trao đổi')
  actionLoading.value = 'message'
  try {
    const payload = { message: reply.message.trim(), attachmentUrls: attachments(reply.attachmentText) }
    selected.value = props.role === 'admin'
      ? await sendAdminDisputeMessage(selected.value.id, payload)
      : props.role === 'seller' ? await respondSellerDispute(selected.value.id, payload) : await sendBuyerDisputeMessage(selected.value.id, payload)
    reply.message = ''; reply.attachmentText = ''; await load(); message.success('Đã gửi trao đổi')
  } catch (error) { message.error(errorText(error, 'Không thể gửi trao đổi')) }
  finally { actionLoading.value = undefined }
}

const takeReview = async () => {
  if (!selected.value) return
  actionLoading.value = 'take-review'
  try { selected.value = await takeDisputeReview(selected.value.id); await load(); message.success('Đã tiếp nhận hồ sơ tranh chấp') }
  catch (error) { message.error(errorText(error, 'Không thể tiếp nhận hồ sơ')); throw error }
  finally { actionLoading.value = undefined }
}
const confirmTakeReview = () => Modal.confirm({
  title: 'Tiếp nhận hồ sơ này?', content: 'Hồ sơ sẽ chuyển sang trạng thái đang xem xét và được gán cho tài khoản admin hiện tại.',
  okText: 'Tiếp nhận', cancelText: 'Hủy', onOk: takeReview
})

const validationResolution = () => {
  if (!resolution.note.trim()) return 'Vui lòng nhập căn cứ và ghi chú xử lý'
  if (resolution.note.trim().length < 10) return 'Ghi chú xử lý cần ít nhất 10 ký tự'
  if (resolution.decision === 'PARTIAL_REFUND') {
    const amount = Number(resolution.resolvedAmount || 0)
    if (amount <= 0) return 'Số tiền hoàn phải lớn hơn 0'
    if (!orderTotal.value || amount >= orderTotal.value) return 'Số tiền hoàn một phần phải nhỏ hơn giá trị đơn hàng'
  }
  return ''
}
const doResolve = async () => {
  if (!selected.value) return
  actionLoading.value = 'resolve'
  try {
    selected.value = await resolveDispute(selected.value.id, {
      decision: resolution.decision,
      resolvedAmount: resolution.decision === 'PARTIAL_REFUND' ? resolution.resolvedAmount : undefined,
      note: resolution.note.trim()
    })
    await load(); message.success('Đã chốt kết quả tranh chấp')
  } catch (error) { message.error(errorText(error, 'Không thể chốt kết quả tranh chấp')); throw error }
  finally { actionLoading.value = undefined }
}
const confirmResolve = () => {
  if (!selected.value) return
  const validationError = validationResolution()
  if (validationError) return message.warning(validationError)
  const decisionText = resolution.decision === 'REFUND_BUYER' ? `Hoàn toàn bộ ${money(orderTotal.value)} cho người mua`
    : resolution.decision === 'PARTIAL_REFUND' ? `Hoàn ${money(resolution.resolvedAmount)} cho người mua` : 'Từ chối yêu cầu hoàn tiền của người mua'
  Modal.confirm({
    title: 'Xác nhận chốt kết quả?', content: `${decisionText}. Quyết định này sẽ tác động đến đối soát của nhà bán và không thể sửa trên màn hình.`,
    okText: 'Chốt kết quả', okType: 'danger', cancelText: 'Kiểm tra lại', onOk: doResolve
  })
}

const closeCase = async () => {
  if (!selected.value) return
  actionLoading.value = 'close'
  try { selected.value = await closeDispute(selected.value.id); await load(); message.success('Đã đóng hồ sơ tranh chấp') }
  catch (error) { message.error(errorText(error, 'Không thể đóng hồ sơ')); throw error }
  finally { actionLoading.value = undefined }
}
const confirmClose = () => Modal.confirm({
  title: 'Đóng hồ sơ tranh chấp?', content: 'Hồ sơ sẽ được lưu ở trạng thái đã đóng và không thể trao đổi thêm.',
  okText: 'Đóng hồ sơ', cancelText: 'Hủy', onOk: closeCase
})

onMounted(load)
</script>

<template>
  <section class="workspace">
    <header class="hero">
      <div><p class="eyebrow">MARKETPLACE CARE</p><h1>{{ title }}</h1><p>{{ subtitle }}</p></div>
      <a-button :loading="loading || detailLoading" @click="refresh">Làm mới dữ liệu</a-button>
    </header>

    <div v-if="role === 'admin'" class="metrics">
      <div><span>Tổng hồ sơ</span><strong>{{ metrics.total }}</strong></div>
      <div class="waiting"><span>Chờ tiếp nhận</span><strong>{{ metrics.waiting }}</strong></div>
      <div class="reviewing"><span>Đang xem xét</span><strong>{{ metrics.reviewing }}</strong></div>
      <div class="completed"><span>Đã có kết quả</span><strong>{{ metrics.completed }}</strong></div>
    </div>

    <div class="filter-card">
      <a-input-search v-model:value="search" allow-clear placeholder="Tìm mã hồ sơ, mã đơn, tiêu đề..." class="search-input" />
      <a-select v-model:value="status" allow-clear placeholder="Tất cả trạng thái" @change="load">
        <a-select-option v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
      </a-select>
      <template v-if="role === 'admin'">
        <a-input v-model:value="sellerId" allow-clear placeholder="Mã nhà bán" @pressEnter="load" />
        <a-input v-model:value="dateFrom" type="date" title="Từ ngày" />
        <a-input v-model:value="dateTo" type="date" title="Đến ngày" />
        <a-button type="primary" :loading="loading" @click="load">Áp dụng</a-button>
        <a-button @click="resetFilters">Đặt lại</a-button>
      </template>
    </div>

    <div class="content-grid">
      <aside class="case-panel">
        <div class="panel-title"><div><h2>Danh sách hồ sơ</h2><span>{{ filteredRows.length }} kết quả</span></div></div>
        <a-spin :spinning="loading">
          <div class="case-list">
            <button v-for="item in pagedRows" :key="item.id" class="case" :class="{ active: selected?.id === item.id }" @click="openCase(item.id)">
              <span class="case-top"><b>{{ item.reason }}</b><a-tag :color="statusMeta(item.status).color">{{ statusMeta(item.status).label }}</a-tag></span>
              <span class="case-type">{{ typeLabel(item.disputeType) }}</span>
              <span class="case-code">Đơn shop: {{ item.orderSellerId }}</span>
              <small>{{ time(item.createdAt) }}</small>
            </button>
            <a-empty v-if="!loading && !pagedRows.length" description="Không có hồ sơ phù hợp" />
          </div>
        </a-spin>
        <a-pagination v-if="filteredRows.length > pageSize" v-model:current="currentPage" :page-size="pageSize" :total="filteredRows.length" :show-size-changer="false" size="small" />
      </aside>

      <main class="detail-panel">
        <a-spin :spinning="detailLoading">
          <article v-if="selected" class="detail">
            <div class="detail-head">
              <div>
                <div class="detail-code">HỒ SƠ #{{ selected.id }}</div><h2>{{ selected.reason }}</h2>
                <div class="detail-tags"><a-tag :color="statusMeta(selected.status).color">{{ statusMeta(selected.status).label }}</a-tag><a-tag>{{ typeLabel(selected.disputeType) }}</a-tag></div>
              </div>
              <span class="created-at">Tạo lúc {{ time(selected.createdAt) }}</span>
            </div>

            <div class="facts">
              <div><small>Mã đơn hàng</small><b>{{ selected.order?.orderCode || selected.orderId }}</b></div>
              <div><small>Nhà bán</small><b>{{ selected.order?.shopName || selected.sellerId }}</b></div>
              <div><small>Giá trị đơn shop</small><b>{{ money(selected.order?.totalAfterDiscount) }}</b></div>
              <div><small>Người mua yêu cầu</small><b>{{ money(selected.requestedAmount) }}</b></div>
              <div><small>Quyết định hoàn</small><b>{{ money(selected.resolvedAmount) }}</b></div>
              <div><small>Admin xử lý</small><b>{{ selected.resolvedByStaffId || 'Chưa tiếp nhận' }}</b></div>
            </div>

            <section class="description-card"><h3>Nội dung khiếu nại</h3><p>{{ selected.description || 'Người mua không cung cấp mô tả bổ sung.' }}</p></section>

            <section v-if="selected.items?.length" class="section-block">
              <h3>Sản phẩm liên quan</h3>
              <div class="item-table">
                <div v-for="item in selected.items" :key="String(item.id)" class="order-item">
                  <div><b>{{ item.productName || 'Sản phẩm' }}</b><small>Phân loại: {{ item.productVariantId || '—' }}</small></div>
                  <span>x{{ item.quantity }}</span><strong>{{ money(item.salePrice) }}</strong>
                </div>
              </div>
            </section>

            <section v-if="selected.evidenceUrls?.length" class="section-block">
              <h3>Bằng chứng từ người mua</h3>
              <div class="evidence-list"><a v-for="(url, index) in selected.evidenceUrls" :key="`${url}-${index}`" :href="url" target="_blank" rel="noreferrer">Bằng chứng {{ index + 1 }} <span>↗</span></a></div>
            </section>

            <section class="section-block conversation">
              <div class="section-title"><h3>Lịch sử trao đổi</h3><span>{{ selected.messages?.length || 0 }} tin nhắn</span></div>
              <div class="messages">
                <div v-for="item in selected.messages" :key="item.id" class="bubble" :class="item.senderType.toLowerCase()">
                  <div class="bubble-head"><b>{{ senderLabels[item.senderType] || item.senderType }}</b><small>{{ time(item.createdAt) }}</small></div>
                  <p>{{ item.message }}</p>
                  <a v-for="(url, index) in item.attachmentUrls" :key="`${url}-${index}`" :href="url" target="_blank" rel="noreferrer">Xem tệp đính kèm {{ index + 1 }}</a>
                </div>
                <a-empty v-if="!selected.messages?.length" description="Chưa có trao đổi" />
              </div>
            </section>

            <div v-if="role !== 'admin' && !terminal" class="composer">
              <h3>Gửi phản hồi</h3>
              <a-textarea v-model:value="reply.message" :rows="3" :maxlength="2000" show-count placeholder="Nhập nội dung phản hồi..." />
              <a-textarea v-model:value="reply.attachmentText" :rows="2" placeholder="URL tệp đính kèm, mỗi dòng một URL" />
              <div class="action-row"><a-button type="primary" :loading="actionLoading === 'message'" @click="sendReply">Gửi phản hồi</a-button></div>
            </div>

            <section v-if="role === 'admin'" class="admin-workflow">
              <div class="workflow-head"><div><span>BƯỚC XỬ LÝ</span><h3>Thao tác quản trị</h3></div></div>
              <div v-if="['OPEN', 'SELLER_RESPONDED'].includes(selected.status)" class="take-review-card">
                <div><b>Hồ sơ đang chờ tiếp nhận</b><p>Admin có thể tiếp nhận ngay để yêu cầu bổ sung bằng chứng hoặc đưa ra quyết định.</p></div>
                <a-button type="primary" :loading="actionLoading === 'take-review'" @click="confirmTakeReview">Tiếp nhận xử lý</a-button>
              </div>

              <template v-if="selected.status === 'UNDER_ADMIN_REVIEW'">
                <div class="composer admin-composer">
                  <h3>Trao đổi với các bên</h3>
                  <a-textarea v-model:value="reply.message" :rows="3" :maxlength="2000" show-count placeholder="Yêu cầu bổ sung bằng chứng hoặc thông báo tiến độ..." />
                  <a-textarea v-model:value="reply.attachmentText" :rows="2" placeholder="URL tệp đính kèm, mỗi dòng một URL" />
                  <div class="action-row"><a-button :loading="actionLoading === 'message'" @click="sendReply">Gửi trao đổi</a-button></div>
                </div>

                <div class="resolution-card">
                  <h3>Chốt kết quả tranh chấp</h3>
                  <a-alert type="warning" show-icon message="Hãy đối chiếu đủ bằng chứng trước khi chốt. Quyết định hoàn tiền sẽ tác động đến đối soát nhà bán." />
                  <label>Quyết định xử lý</label>
                  <a-radio-group v-model:value="resolution.decision" class="decision-group">
                    <a-radio-button value="REFUND_BUYER">Hoàn toàn bộ</a-radio-button><a-radio-button value="PARTIAL_REFUND">Hoàn một phần</a-radio-button><a-radio-button value="REJECT_BUYER">Từ chối yêu cầu</a-radio-button>
                  </a-radio-group>
                  <div v-if="resolution.decision === 'PARTIAL_REFUND'" class="amount-field">
                    <label>Số tiền hoàn</label>
                    <a-input-number v-model:value="resolution.resolvedAmount" :min="1" :max="maxPartialAmount || undefined" :step="1000" placeholder="Nhập số tiền" />
                    <small>Tối đa nhỏ hơn {{ money(orderTotal) }}</small>
                  </div>
                  <label>Căn cứ và ghi chú xử lý</label>
                  <a-textarea v-model:value="resolution.note" :rows="4" :maxlength="2000" show-count placeholder="Mô tả bằng chứng đã đối chiếu và lý do đưa ra quyết định..." />
                  <div class="resolution-summary">
                    <span>Giá trị đơn: <b>{{ money(orderTotal) }}</b></span>
                    <span>Số tiền hoàn dự kiến: <b>{{ money(resolution.decision === 'REFUND_BUYER' ? orderTotal : resolution.decision === 'PARTIAL_REFUND' ? resolution.resolvedAmount : 0) }}</b></span>
                  </div>
                  <div class="action-row"><a-button type="primary" danger :loading="actionLoading === 'resolve'" @click="confirmResolve">Chốt kết quả</a-button></div>
                </div>
              </template>

              <div v-if="selected.status.startsWith('RESOLVED_')" class="close-card">
                <div><b>Hồ sơ đã có kết quả</b><p>Kiểm tra kết luận bên dưới trước khi đóng và lưu trữ hồ sơ.</p></div>
                <a-button :loading="actionLoading === 'close'" @click="confirmClose">Đóng hồ sơ</a-button>
              </div>
              <a-alert v-if="selected.status === 'CLOSED'" type="success" show-icon message="Hồ sơ đã đóng. Không còn thao tác xử lý nào cần thực hiện." />
            </section>

            <a-alert v-if="selected.resolutionNote" class="resolution-alert" type="info" show-icon :message="`Kết luận xử lý · ${money(selected.resolvedAmount)}`" :description="selected.resolutionNote" />
          </article>
          <div v-else class="placeholder"><a-empty description="Chọn một hồ sơ để xem chi tiết và xử lý" /></div>
        </a-spin>
      </main>
    </div>
  </section>
</template>

<style scoped>
.workspace{min-height:100%;padding:28px;background:#f5f7fb;color:#172033}.hero{display:flex;justify-content:space-between;gap:24px;align-items:flex-start;margin-bottom:22px}.hero h1{margin:3px 0 6px;font-size:30px;line-height:1.2;color:#101828}.hero p{margin:0;color:#667085}.eyebrow{font-size:12px!important;letter-spacing:.14em;color:#0891b2!important;font-weight:800}.metrics{display:grid;grid-template-columns:repeat(4,1fr);gap:14px;margin-bottom:16px}.metrics>div{position:relative;overflow:hidden;display:flex;justify-content:space-between;align-items:center;padding:17px 18px;background:#fff;border:1px solid #e4e9f0;border-radius:14px;box-shadow:0 4px 14px rgba(15,23,42,.035)}.metrics>div::before{content:"";position:absolute;inset:0 auto 0 0;width:4px;background:#94a3b8}.metrics .waiting::before{background:#f59e0b}.metrics .reviewing::before{background:#8b5cf6}.metrics .completed::before{background:#10b981}.metrics span{font-size:13px;color:#667085}.metrics strong{font-size:25px;color:#101828}.filter-card{display:grid;grid-template-columns:minmax(260px,1.4fr) minmax(190px,.8fr) repeat(3,minmax(145px,.65fr)) auto auto;gap:10px;padding:14px;margin-bottom:16px;background:#fff;border:1px solid #e1e7ef;border-radius:14px}.content-grid{display:grid;grid-template-columns:minmax(310px,36%) minmax(0,1fr);gap:16px;align-items:start}.case-panel,.detail-panel{background:#fff;border:1px solid #e1e7ef;border-radius:16px;box-shadow:0 5px 18px rgba(15,23,42,.035)}.case-panel{position:sticky;top:16px;padding:16px}.panel-title{margin-bottom:13px}.panel-title h2{margin:0 0 2px;font-size:17px}.panel-title span,.section-title span{font-size:12px;color:#98a2b3}.case-list{display:flex;flex-direction:column;gap:9px;min-height:220px}.case{width:100%;display:grid;gap:6px;padding:13px 14px;text-align:left;color:#344054;background:#f8fafc;border:1px solid #e4e9f0;border-radius:11px;cursor:pointer;transition:.18s ease}.case:hover,.case.active{border-color:#22a7c7;background:#effbfe;box-shadow:0 3px 10px rgba(8,145,178,.08)}.case.active{box-shadow:inset 3px 0 #0891b2}.case-top{display:flex;justify-content:space-between;gap:8px;align-items:flex-start}.case-top b{overflow:hidden;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:2}.case-type{font-size:13px;color:#475467}.case-code{overflow:hidden;font-size:12px;color:#667085;text-overflow:ellipsis;white-space:nowrap}.case small{color:#98a2b3}.case-panel :deep(.ant-pagination){display:flex;justify-content:center;margin-top:15px}.detail-panel{min-height:600px;overflow:hidden}.detail{padding:22px}.detail-head{display:flex;justify-content:space-between;gap:18px;padding-bottom:18px;border-bottom:1px solid #edf0f4}.detail-head h2{margin:5px 0 10px;font-size:22px}.detail-code{max-width:520px;overflow:hidden;font-size:11px;letter-spacing:.08em;color:#98a2b3;text-overflow:ellipsis;white-space:nowrap}.detail-tags{display:flex;gap:6px;flex-wrap:wrap}.created-at{flex:0 0 auto;font-size:12px;color:#98a2b3}.facts{display:grid;grid-template-columns:repeat(3,1fr);gap:10px;margin:18px 0}.facts>div{min-width:0;display:grid;gap:5px;padding:13px;background:#f8fafc;border:1px solid #eef1f5;border-radius:10px}.facts small{color:#667085}.facts b{overflow:hidden;color:#27364b;text-overflow:ellipsis;white-space:nowrap}.description-card{padding:15px 16px;background:#f8fafc;border-left:3px solid #22a7c7;border-radius:9px}.description-card h3,.section-block h3,.composer h3,.resolution-card h3{margin:0 0 8px;font-size:16px}.description-card p{margin:0;color:#475467;white-space:pre-wrap}.section-block{margin-top:21px}.section-title{display:flex;justify-content:space-between;align-items:center}.item-table{border:1px solid #e7ebf0;border-radius:10px;overflow:hidden}.order-item{display:grid;grid-template-columns:1fr auto minmax(110px,auto);gap:18px;align-items:center;padding:12px 14px;border-bottom:1px solid #edf0f4}.order-item:last-child{border-bottom:0}.order-item>div{display:grid;gap:3px}.order-item small{color:#98a2b3}.evidence-list{display:flex;gap:8px;flex-wrap:wrap}.evidence-list a{padding:8px 11px;color:#087b98;background:#effbfe;border:1px solid #c7eff7;border-radius:8px}.messages{display:flex;flex-direction:column;gap:10px;max-height:360px;padding:12px;overflow:auto;background:#f8fafc;border-radius:11px}.bubble{max-width:88%;padding:11px 13px;background:#fff;border:1px solid #e6eaf0;border-radius:10px}.bubble.seller{border-left:3px solid #f59e0b}.bubble.buyer{border-left:3px solid #22a7c7}.bubble.admin{align-self:flex-end;border-right:3px solid #8b5cf6;background:#faf8ff}.bubble-head{display:flex;justify-content:space-between;gap:22px}.bubble-head small{color:#98a2b3}.bubble p{margin:5px 0;color:#344054;white-space:pre-wrap}.bubble>a{display:block;font-size:12px}.composer,.admin-workflow{display:grid;gap:10px;margin-top:20px;padding-top:18px;border-top:1px solid #e5e7eb}.action-row{display:flex;justify-content:flex-end}.workflow-head span{font-size:11px;font-weight:800;letter-spacing:.1em;color:#8b5cf6}.workflow-head h3{margin:3px 0 0;font-size:18px}.take-review-card,.close-card{display:flex;justify-content:space-between;gap:20px;align-items:center;padding:15px;background:#f8fafc;border:1px solid #e6eaf0;border-radius:10px}.take-review-card p,.close-card p{margin:3px 0 0;color:#667085}.admin-composer{margin-top:2px;padding:15px;background:#faf8ff;border:1px solid #ede9fe;border-radius:11px}.resolution-card{display:grid;gap:10px;padding:16px;margin-top:4px;background:#fffaf5;border:1px solid #fed7aa;border-radius:11px}.resolution-card label,.amount-field label{font-size:13px;font-weight:650;color:#344054}.decision-group{display:flex}.decision-group :deep(.ant-radio-button-wrapper){flex:1;text-align:center}.amount-field{display:grid;grid-template-columns:1fr;gap:6px}.amount-field :deep(.ant-input-number){width:100%}.amount-field small{color:#98a2b3}.resolution-summary{display:flex;justify-content:space-between;gap:15px;padding:11px 12px;background:#fff;border-radius:8px;color:#667085}.resolution-alert{margin-top:18px}.placeholder{min-height:598px;display:grid;place-items:center}@media(max-width:1200px){.filter-card{grid-template-columns:repeat(3,1fr)}.search-input{grid-column:span 2}.metrics{grid-template-columns:repeat(2,1fr)}.facts{grid-template-columns:repeat(2,1fr)}}@media(max-width:900px){.workspace{padding:18px}.content-grid{grid-template-columns:1fr}.case-panel{position:static}.filter-card{grid-template-columns:1fr 1fr}.search-input{grid-column:span 2}.facts{grid-template-columns:1fr 1fr}.detail-panel{min-height:420px}.placeholder{min-height:400px}}@media(max-width:600px){.hero,.detail-head,.take-review-card,.close-card{flex-direction:column}.metrics,.filter-card,.facts{grid-template-columns:1fr}.search-input{grid-column:auto}.detail{padding:16px}.created-at{align-self:flex-start}.order-item{grid-template-columns:1fr auto}.order-item strong{grid-column:1/-1}.decision-group,.resolution-summary{display:grid}.bubble{max-width:100%}}
</style>
