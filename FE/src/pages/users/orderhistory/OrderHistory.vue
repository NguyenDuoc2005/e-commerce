<template>
  <main class="orders-page">
    <header class="page-header">
      <div>
        <h1>Don mua</h1>
        <p>Theo doi tung kien hang theo shop.</p>
      </div>
      <a-input-search v-model:value="keyword" placeholder="Tim ma don, ten shop hoac san pham" allow-clear class="search" />
    </header>

    <a-segmented v-model:value="selectedStatus" :options="statusOptions" block />
    <a-spin :spinning="loading">
      <a-empty v-if="!loading && !filteredOrders.length" description="Chua co don hang phu hop" />
      <section v-else class="order-list">
        <article v-for="order in filteredOrders" :key="order.id" class="parent-order">
          <div class="parent-header">
            <div>
              <span class="muted">Don hang</span>
              <strong>{{ order.code }}</strong>
            </div>
            <div class="parent-summary">
              <span>{{ formatDate(order.created_date) }}</span>
              <strong>{{ formatMoney(order.total_after_discount) }}</strong>
            </div>
          </div>

          <div class="suborders">
            <section v-for="subOrder in visibleSubOrders(order)" :key="subOrder.id" class="suborder">
              <div class="shop-row">
                <router-link :to="`/shop/${subOrder.seller_slug}`">{{ subOrder.shop_name }}</router-link>
                <a-tag :color="statusColor(subOrder.order_status)">{{ statusLabel(subOrder.order_status) }}</a-tag>
              </div>

              <div v-for="item in subOrder.items" :key="item.id" class="item-row">
                <img :src="item.imageUrl || '/images/default-product.png'" :alt="item.productName" />
                <div class="item-main">
                  <strong>{{ item.productName }}</strong>
                  <span>{{ [item.color, item.size].filter(Boolean).join(' - ') || 'Phan loai tieu chuan' }}</span>
                  <span>So luong: {{ item.quantity }}</span>
                </div>
                <div class="item-price">{{ formatMoney(item.sale_price * item.quantity) }}</div>
                <a-button
                  v-if="subOrder.order_status === 4 && !reviewedItems.has(reviewKey(subOrder.id, item.product_variant_id))"
                  type="primary"
                  @click="openReview(subOrder, item)"
                >
                  Danh gia
                </a-button>
                <a-tag v-else-if="reviewedItems.has(reviewKey(subOrder.id, item.product_variant_id))" color="green">Da danh gia</a-tag>
              </div>

              <div class="suborder-total">
                <span>Thanh tien shop</span>
                <strong>{{ formatMoney(subOrder.total_after_discount) }}</strong>
              </div>
            </section>
          </div>
        </article>
      </section>
    </a-spin>

    <a-modal v-model:open="reviewOpen" title="Danh gia san pham va shop" :confirm-loading="submitting" @ok="submitReview">
      <a-form layout="vertical">
        <a-form-item label="San pham">
          <strong>{{ reviewTarget?.item.productName }}</strong>
        </a-form-item>
        <a-form-item label="Chat luong san pham" required>
          <a-rate v-model:value="reviewForm.productRating" />
        </a-form-item>
        <a-form-item label="Trai nghiem voi shop" required>
          <a-rate v-model:value="reviewForm.shopRating" />
        </a-form-item>
        <a-form-item label="Nhan xet">
          <a-textarea v-model:value="reviewForm.comment" :maxlength="2000" show-count :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getGroupedOrders, type GroupedOrder, type GroupedOrderItem, type GroupedSubOrder } from '@/services/api/buyer/orders/orders.api'
import { createReview, getMyReviews } from '@/services/api/seller/review.api'

const statuses = ['Cho xac nhan', 'Da xac nhan', 'Cho giao', 'Dang giao', 'Hoan thanh', 'Da huy']
const statusOptions = [{ label: 'Tat ca', value: -1 }, ...statuses.map((label, value) => ({ label, value }))]
const orders = ref<GroupedOrder[]>([])
const loading = ref(false)
const keyword = ref('')
const selectedStatus = ref(-1)
const reviewOpen = ref(false)
const submitting = ref(false)
const reviewedItems = ref(new Set<string>())
const reviewTarget = ref<{ subOrder: GroupedSubOrder; item: GroupedOrderItem } | null>(null)
const reviewForm = reactive({ productRating: 5, shopRating: 5, comment: '' })

const filteredOrders = computed(() => {
  const query = keyword.value.trim().toLowerCase()
  return orders.value.filter(order => {
    const matchingSubOrders = visibleSubOrders(order)
    if (!matchingSubOrders.length) return false
    if (!query) return true
    return order.code.toLowerCase().includes(query) || matchingSubOrders.some(subOrder =>
      subOrder.shop_name.toLowerCase().includes(query) || subOrder.items.some(item => item.productName?.toLowerCase().includes(query)))
  })
})

const visibleSubOrders = (order: GroupedOrder) => selectedStatus.value < 0
  ? order.subOrders
  : order.subOrders.filter(subOrder => subOrder.order_status === selectedStatus.value)

const loadOrders = async () => {
  loading.value = true
  try {
    const [orderResponse, reviewResponse] = await Promise.all([getGroupedOrders(), getMyReviews()])
    orders.value = orderResponse.data ?? []
    reviewedItems.value = new Set((reviewResponse.data ?? []).map(review => reviewKey(review.orderSellerId, review.productDetailId)))
  } catch (error: any) {
    message.error(error?.response?.data?.message ?? 'Khong tai duoc lich su don hang')
  } finally {
    loading.value = false
  }
}

const openReview = (subOrder: GroupedSubOrder, item: GroupedOrderItem) => {
  reviewTarget.value = { subOrder, item }
  reviewForm.productRating = 5
  reviewForm.shopRating = 5
  reviewForm.comment = ''
  reviewOpen.value = true
}

const submitReview = async () => {
  if (!reviewTarget.value || !reviewForm.productRating || !reviewForm.shopRating) {
    message.warning('Vui long chon day du so sao')
    return
  }
  submitting.value = true
  try {
    await createReview({
      orderSellerId: reviewTarget.value.subOrder.id,
      productDetailId: reviewTarget.value.item.product_variant_id,
      productRating: reviewForm.productRating,
      shopRating: reviewForm.shopRating,
      comment: reviewForm.comment.trim() || undefined
    })
    reviewedItems.value.add(reviewKey(reviewTarget.value.subOrder.id, reviewTarget.value.item.product_variant_id))
    reviewOpen.value = false
    message.success('Danh gia da duoc ghi nhan')
  } catch (error: any) {
    message.error(error?.response?.data?.message ?? 'Khong gui duoc danh gia')
  } finally {
    submitting.value = false
  }
}

const reviewKey = (subOrderId: string, productDetailId: string) => `${subOrderId}:${productDetailId}`
const statusLabel = (status: number) => statuses[status] ?? 'Khong xac dinh'
const statusColor = (status: number) => status === 4 ? 'green' : status === 5 ? 'red' : status >= 2 ? 'blue' : 'orange'
const formatDate = (value: number) => value ? new Intl.DateTimeFormat('vi-VN').format(new Date(value)) : ''
const formatMoney = (value: number) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)

onMounted(loadOrders)
</script>

<style scoped>
.orders-page { max-width: 1120px; margin: 0 auto; padding: 28px 16px 56px; }
.page-header { display: flex; justify-content: space-between; align-items: end; gap: 24px; margin-bottom: 20px; }
.page-header h1 { margin: 0; font-size: 28px; }
.page-header p { margin: 6px 0 0; color: #64748b; }
.search { width: min(420px, 100%); }
.order-list { display: grid; gap: 20px; margin-top: 24px; }
.parent-order { border: 1px solid #dbe2ea; border-radius: 8px; background: #fff; overflow: hidden; }
.parent-header, .shop-row, .suborder-total { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.parent-header { padding: 16px 18px; background: #f8fafc; border-bottom: 1px solid #e2e8f0; }
.parent-header strong { display: block; }
.muted, .item-main span, .parent-summary span { color: #64748b; font-size: 13px; }
.parent-summary { display: flex; align-items: center; gap: 20px; }
.suborders { display: grid; gap: 0; }
.suborder { padding: 18px; border-bottom: 1px solid #e2e8f0; }
.suborder:last-child { border-bottom: 0; }
.shop-row { margin-bottom: 12px; }
.shop-row a { color: #0f766e; font-weight: 700; }
.item-row { display: grid; grid-template-columns: 72px minmax(0, 1fr) auto auto; align-items: center; gap: 14px; padding: 12px 0; border-top: 1px solid #eef2f6; }
.item-row img { width: 72px; height: 72px; object-fit: cover; border-radius: 6px; background: #f1f5f9; }
.item-main { min-width: 0; display: grid; gap: 3px; }
.item-main strong { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-price { font-weight: 700; white-space: nowrap; }
.suborder-total { padding-top: 12px; border-top: 1px solid #eef2f6; }
.suborder-total strong { color: #be123c; font-size: 17px; }
@media (max-width: 720px) {
  .page-header { align-items: stretch; flex-direction: column; }
  .search { width: 100%; }
  .parent-header, .parent-summary { align-items: flex-start; flex-direction: column; gap: 5px; }
  .item-row { grid-template-columns: 60px minmax(0, 1fr); }
  .item-row img { width: 60px; height: 60px; }
  .item-price, .item-row :deep(.ant-btn), .item-row :deep(.ant-tag) { grid-column: 2; justify-self: start; }
}
</style>
