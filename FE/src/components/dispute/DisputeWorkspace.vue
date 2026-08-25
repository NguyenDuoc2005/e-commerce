<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  adminDisputeDetail, adminDisputes, buyerDisputeDetail, buyerDisputes, closeDispute,
  respondSellerDispute, resolveDispute, sellerDisputeDetail, sellerDisputes,
  sendBuyerDisputeMessage, takeDisputeReview, type Dispute
} from '@/services/api/dispute/dispute.api'

const props = defineProps<{ role: 'buyer' | 'seller' | 'admin' }>()
const loading = ref(false)
const rows = ref<Dispute[]>([])
const selected = ref<Dispute | null>(null)
const status = ref<string>()
const sellerId = ref('')
const dateFrom = ref('')
const dateTo = ref('')
const reply = reactive({ message: '', attachmentText: '' })
const resolution = reactive({ decision: 'REFUND_BUYER', resolvedAmount: undefined as number | undefined, note: '' })

const title = computed(() => props.role === 'admin' ? 'Xử lý tranh chấp' : props.role === 'seller' ? 'Tranh chấp với người mua' : 'Khiếu nại của tôi')
const terminal = computed(() => selected.value ? ['RESOLVED_REFUND_BUYER', 'RESOLVED_REJECT_BUYER', 'RESOLVED_PARTIAL_REFUND', 'CLOSED'].includes(selected.value.status) : false)
const statuses = ['OPEN', 'SELLER_RESPONDED', 'UNDER_ADMIN_REVIEW', 'RESOLVED_REFUND_BUYER', 'RESOLVED_REJECT_BUYER', 'RESOLVED_PARTIAL_REFUND', 'CLOSED']
const labels: Record<string, string> = {
  OPEN: 'Mới mở', SELLER_RESPONDED: 'Seller đã phản hồi', UNDER_ADMIN_REVIEW: 'Admin đang xem xét',
  RESOLVED_REFUND_BUYER: 'Hoàn tiền người mua', RESOLVED_REJECT_BUYER: 'Từ chối yêu cầu',
  RESOLVED_PARTIAL_REFUND: 'Hoàn tiền một phần', CLOSED: 'Đã đóng'
}

const errorText = (error: any) => error?.response?.data?.message || 'Không thể thực hiện thao tác'
const attachments = (value: string) => value.split('\n').map(item => item.trim()).filter(Boolean)
const money = (value?: number) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)
const time = (value?: string) => value ? new Date(value).toLocaleString('vi-VN') : '—'

const load = async () => {
  loading.value = true
  try {
    rows.value = props.role === 'admin'
      ? await adminDisputes({ status: status.value, sellerId: sellerId.value || undefined, dateFrom: dateFrom.value || undefined, dateTo: dateTo.value || undefined })
      : props.role === 'seller' ? await sellerDisputes(status.value) : await buyerDisputes(status.value)
    if (selected.value) {
      const found = rows.value.find(item => item.id === selected.value?.id)
      if (!found) selected.value = null
    }
  } catch (error) { message.error(errorText(error)) }
  finally { loading.value = false }
}

const open = async (id: string) => {
  loading.value = true
  try {
    selected.value = props.role === 'admin' ? await adminDisputeDetail(id) : props.role === 'seller' ? await sellerDisputeDetail(id) : await buyerDisputeDetail(id)
  } catch (error) { message.error(errorText(error)) }
  finally { loading.value = false }
}

const sendReply = async () => {
  if (!selected.value || !reply.message.trim()) return message.warning('Vui lòng nhập nội dung phản hồi')
  try {
    selected.value = props.role === 'seller'
      ? await respondSellerDispute(selected.value.id, { message: reply.message, attachmentUrls: attachments(reply.attachmentText) })
      : await sendBuyerDisputeMessage(selected.value.id, { message: reply.message, attachmentUrls: attachments(reply.attachmentText) })
    reply.message = ''; reply.attachmentText = ''; await load(); message.success('Đã gửi phản hồi')
  } catch (error) { message.error(errorText(error)) }
}

const takeReview = async () => {
  if (!selected.value) return
  try { selected.value = await takeDisputeReview(selected.value.id); await load(); message.success('Đã tiếp nhận xử lý') }
  catch (error) { message.error(errorText(error)) }
}

const resolve = async () => {
  if (!selected.value || !resolution.note.trim()) return message.warning('Vui lòng nhập ghi chú xử lý')
  try {
    selected.value = await resolveDispute(selected.value.id, resolution)
    await load(); message.success('Đã lưu kết quả tranh chấp')
  } catch (error) { message.error(errorText(error)) }
}

const closeCase = async () => {
  if (!selected.value) return
  try { selected.value = await closeDispute(selected.value.id); await load(); message.success('Đã đóng tranh chấp') }
  catch (error) { message.error(errorText(error)) }
}

onMounted(load)
</script>

<template>
  <section class="workspace">
    <header><div><p class="eyebrow">MARKETPLACE CARE</p><h1>{{ title }}</h1><p>Theo dõi hội thoại, bằng chứng và kết quả xử lý theo từng đơn của shop.</p></div><a-button @click="load">Làm mới</a-button></header>
    <div class="filters">
      <a-select v-model:value="status" allow-clear placeholder="Tất cả trạng thái" style="min-width: 220px" @change="load">
        <a-select-option v-for="item in statuses" :key="item" :value="item">{{ labels[item] }}</a-select-option>
      </a-select>
      <template v-if="role === 'admin'">
        <a-input v-model:value="sellerId" placeholder="Seller ID" @pressEnter="load" />
        <a-input v-model:value="dateFrom" type="date" /><a-input v-model:value="dateTo" type="date" />
        <a-button type="primary" @click="load">Lọc</a-button>
      </template>
    </div>

    <div class="grid">
      <div class="case-list" :class="{ loading }">
        <button v-for="item in rows" :key="item.id" class="case" :class="{ active: selected?.id === item.id }" @click="open(item.id)">
          <span class="case-top"><b>{{ item.reason }}</b><a-tag color="blue">{{ labels[item.status] }}</a-tag></span>
          <small>{{ item.disputeType }} · {{ time(item.createdAt) }}</small>
          <span>Đơn shop: {{ item.orderSellerId }}</span>
        </button>
        <a-empty v-if="!loading && !rows.length" description="Chưa có tranh chấp" />
      </div>

      <article v-if="selected" class="detail">
        <div class="detail-head"><div><h2>{{ selected.reason }}</h2><p>{{ selected.description || 'Không có mô tả bổ sung' }}</p></div><a-tag color="cyan">{{ labels[selected.status] }}</a-tag></div>
        <div class="facts">
          <div><small>Mã đơn</small><b>{{ selected.order?.orderCode || selected.orderId }}</b></div>
          <div><small>Shop</small><b>{{ selected.order?.shopName || selected.sellerId }}</b></div>
          <div><small>Yêu cầu hoàn</small><b>{{ money(selected.requestedAmount) }}</b></div>
          <div><small>Đã giải quyết</small><b>{{ money(selected.resolvedAmount) }}</b></div>
        </div>
        <div v-if="selected.evidenceUrls?.length" class="links"><b>Bằng chứng</b><a v-for="url in selected.evidenceUrls" :key="url" :href="url" target="_blank">{{ url }}</a></div>
        <h3>Trao đổi</h3>
        <div class="messages">
          <div v-for="item in selected.messages" :key="item.id" class="bubble" :class="item.senderType.toLowerCase()">
            <b>{{ item.senderType }}</b><p>{{ item.message }}</p><small>{{ time(item.createdAt) }}</small>
            <a v-for="url in item.attachmentUrls" :key="url" :href="url" target="_blank">Tệp đính kèm</a>
          </div>
          <a-empty v-if="!selected.messages?.length" description="Chưa có phản hồi" />
        </div>
        <div v-if="role !== 'admin' && !terminal" class="composer">
          <a-textarea v-model:value="reply.message" :rows="3" placeholder="Nội dung phản hồi" />
          <a-textarea v-model:value="reply.attachmentText" :rows="2" placeholder="URL tệp đính kèm, mỗi dòng một URL" />
          <a-button type="primary" @click="sendReply">Gửi phản hồi</a-button>
        </div>
        <div v-if="role === 'admin'" class="admin-actions">
          <a-button v-if="selected.status === 'SELLER_RESPONDED'" type="primary" @click="takeReview">Tiếp nhận xem xét</a-button>
          <template v-if="selected.status === 'UNDER_ADMIN_REVIEW'">
            <a-select v-model:value="resolution.decision"><a-select-option value="REFUND_BUYER">Hoàn toàn bộ</a-select-option><a-select-option value="PARTIAL_REFUND">Hoàn một phần</a-select-option><a-select-option value="REJECT_BUYER">Từ chối</a-select-option></a-select>
            <a-input-number v-if="resolution.decision === 'PARTIAL_REFUND'" v-model:value="resolution.resolvedAmount" :min="1" placeholder="Số tiền" />
            <a-textarea v-model:value="resolution.note" :rows="3" placeholder="Căn cứ và ghi chú xử lý" />
            <a-button type="primary" danger @click="resolve">Chốt kết quả</a-button>
          </template>
          <a-button v-if="selected.status.startsWith('RESOLVED_')" @click="closeCase">Đóng hồ sơ</a-button>
        </div>
        <a-alert v-if="selected.resolutionNote" type="info" show-icon :message="'Kết luận: ' + selected.resolutionNote" />
      </article>
      <div v-else class="placeholder"><a-empty description="Chọn một tranh chấp để xem chi tiết" /></div>
    </div>
  </section>
</template>

<style scoped>
.workspace{padding:24px;color:#172033}.workspace header{display:flex;justify-content:space-between;gap:24px;align-items:start;margin-bottom:20px}.workspace h1{font-size:28px;margin:2px 0}.workspace header p{margin:0;color:#667085}.eyebrow{font-size:12px!important;letter-spacing:.12em;color:#54bddb!important;font-weight:800}.filters{display:flex;gap:10px;margin-bottom:16px}.filters .ant-input{max-width:210px}.grid{display:grid;grid-template-columns:minmax(300px,38%) 1fr;gap:16px;min-height:560px}.case-list,.detail,.placeholder{background:#fff;border:1px solid #e1e7ef;border-radius:14px;padding:16px}.case-list{display:flex;flex-direction:column;gap:10px}.case{background:#f8fafc;border:1px solid #e4e9f0;border-radius:10px;padding:14px;text-align:left;display:grid;gap:7px;color:#344054}.case:hover,.case.active{border-color:#54bddb;background:#f0fbfe}.case-top{display:flex;justify-content:space-between;gap:8px}.detail-head{display:flex;justify-content:space-between;gap:14px}.detail h2{margin:0 0 6px}.facts{display:grid;grid-template-columns:repeat(4,1fr);gap:10px;margin:18px 0}.facts div{background:#f8fafc;padding:12px;border-radius:9px;display:grid;gap:5px}.facts small{color:#667085}.links{display:flex;flex-direction:column;gap:4px;margin-bottom:16px}.messages{display:flex;flex-direction:column;gap:9px;max-height:250px;overflow:auto}.bubble{padding:10px 12px;border-radius:10px;background:#f3f5f8}.bubble.seller{border-left:3px solid #f59e0b}.bubble.buyer{border-left:3px solid #54bddb}.bubble p{margin:4px 0}.bubble small{color:#98a2b3}.bubble a{display:block}.composer,.admin-actions{display:grid;gap:9px;margin-top:16px;padding-top:16px;border-top:1px solid #e5e7eb}.placeholder{display:grid;place-items:center}@media(max-width:900px){.grid{grid-template-columns:1fr}.facts{grid-template-columns:1fr 1fr}.filters{flex-wrap:wrap}}
</style>
