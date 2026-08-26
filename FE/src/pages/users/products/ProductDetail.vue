<template>
  <div class="container py-3">
    <BreadCrumbUser :routes="[{ name: 'Trang chủ', path: '/' }, { name: 'Sản phẩm', path: '/san-pham' }]" title="Chi tiết sản phẩm" />
    <div v-if="loading" class="py-5 text-center">Đang tải…</div>
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>
    <template v-else-if="product">
      <div class="row g-4">
        <div class="col-md-6">
          <img :src="current?.imageUrl || selectedImage || product.productImages?.[0]?.url || placeholder" class="hero" alt="Ảnh sản phẩm" />
          <div class="d-flex gap-2 mt-3">
            <button v-for="image in product.productImages" :key="image.id" class="thumb-button" type="button" @click="selectedImage = image.url">
              <img :src="image.url" class="thumb" alt="Ảnh thu nhỏ" />
            </button>
          </div>
        </div>

        <div class="col-md-6">
          <h3>{{ product.name }}</h3>
          <div class="rating-line">★ {{ Number(product.ratingAverage || 0).toFixed(1) }} · {{ product.ratingCount || 0 }} đánh giá</div>
          <ReportButton target-type="PRODUCT" :target-id="product.id" />
          <div class="text-muted mb-3">{{ product.category?.name }}</div>
          <div class="price mb-3">{{ money(current?.salePrice ?? lowestPrice) }}</div>

          <section v-if="product.variantAxes.length" class="classification-block">
            <h5>Chọn phân loại</h5>
            <div v-for="axis in product.variantAxes" :key="axis.id" class="mb-3 axis-group">
              <label class="fw-bold">{{ axis.name }}</label>
              <div class="d-flex flex-wrap gap-2">
                <button
                  v-for="value in axis.values"
                  :key="value.id"
                  class="btn btn-sm"
                  :class="selected[axis.id] === value.id ? 'btn-dark' : 'btn-outline-secondary'"
                  :disabled="!available(axis.id, value.id)"
                  @click="selected[axis.id] = value.id"
                >
                  {{ value.value }}
                </button>
              </div>
            </div>
          </section>
          <div class="text-muted small mb-3">
            {{ current ? `SKU ${current.sku} · Còn ${current.quantity}` : 'Vui lòng chọn đầy đủ tổ hợp còn hàng' }}
          </div>
          <div class="d-flex align-items-center gap-2">
            <input v-model.number="quantity" class="form-control" type="number" min="1" :max="current?.quantity || 1" style="width: 100px" />
            <button class="btn btn-outline-primary" :disabled="!current" @click="addCart">Thêm vào giỏ</button>
            <button class="btn btn-primary" :disabled="!current" @click="buyNow">Mua ngay</button>
          </div>
        </div>
      </div>

      <section v-if="shop" class="shop-card mt-4">
        <img :src="shop.logoUrl || placeholder" alt="Logo shop" />
        <div class="shop-main">
          <strong>{{ shop.shopName }}</strong>
          <span>{{ shop.rating || 0 }} sao · {{ shop.ratingCount || 0 }} đánh giá shop</span>
          <span>{{ shop.followerCount || 0 }} người theo dõi · Đã bán {{ shop.soldCount || 0 }}</span>
        </div>
        <button class="btn btn-primary" type="button" @click="openChat">Chat với shop</button>
        <button class="btn btn-outline-primary" type="button" @click="openShop">Xem shop</button>
        <ReportButton target-type="SHOP" :target-id="shop.id" />
      </section>

      <div class="detail-grid mt-4">
        <section>
          <h5>Mô tả</h5>
          <p class="description">{{ product.description || 'Chưa có mô tả.' }}</p>
        </section>
        <section>
          <h5>Thông số sản phẩm</h5>
          <dl v-if="product.attributes?.length" class="attrs">
            <template v-for="attribute in product.attributes" :key="attribute.definitionId">
              <dt>{{ attribute.name }}</dt>
              <dd>{{ attributeValue(attribute) }}</dd>
            </template>
          </dl>
          <p v-else class="text-muted">Sản phẩm chưa có thông số mô tả.</p>
        </section>
      </div>

      <section class="review-block mt-4">
        <div class="review-head">
          <h5>Đánh giá sản phẩm</h5>
          <span>{{ reviews.length }} đánh giá</span>
        </div>
        <div v-if="!reviews.length" class="text-muted">Sản phẩm chưa có đánh giá.</div>
        <article v-for="review in reviews" v-else :key="review.id" class="review-item">
          <div><strong>Khách hàng</strong> · <span class="review-stars">{{ '★'.repeat(review.productRating) }}{{ '☆'.repeat(5 - review.productRating) }}</span></div>
          <small>{{ formatDate(review.createdAt) }}</small>
          <p>{{ review.comment || 'Khách hàng không để lại bình luận.' }}</p>
          <div v-if="review.imageUrls?.length" class="review-images">
            <img v-for="image in review.imageUrls" :key="image" :src="image" alt="Ảnh đánh giá" />
          </div>
          <div v-if="review.sellerReply" class="seller-reply"><strong>Phản hồi của shop:</strong> {{ review.sellerReply }}</div>
          <ReportButton target-type="REVIEW" :target-id="review.id" label="Báo cáo đánh giá" />
        </article>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BreadCrumbUser from '@/components/ui/Breadcrumbs/BreadCrumbUser.vue'
import { getCatalogProduct, type CatalogAttribute, type CatalogDetail, type CatalogVariant } from '@/services/api/catalog/catalog.api'
import { getPublicShopsByIds, type SellerResponse } from '@/services/api/seller/seller.api'
import { getPublicReviews, type Review } from '@/services/api/seller/review.api'
import { createCartDetail } from '@/services/api/permitall/cart/cart'
import { localStorageAction } from '@/utils/storage'
import { CART_STORAGE_KEY, CHECKOUT_STORAGE_KEY, USER_INFO_STORAGE_KEY } from '@/constants/storageKey'
import { toast } from 'vue3-toastify'
import ReportButton from '@/components/report/ReportButton.vue'

const route = useRoute()
const router = useRouter()
const product = ref<CatalogDetail>()
const shop = ref<SellerResponse>()
const reviews = ref<Review[]>([])
const loading = ref(true)
const error = ref('')
const selected = reactive<Record<string, string>>({})
const selectedImage = ref('')
const quantity = ref(1)
const placeholder = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="300"%3E%3Crect width="100%" height="100%" fill="%23f1f5f9"/%3E%3C/svg%3E'

const variants = computed(() => product.value?.variants || [])
const current = computed(() => {
  const axes = product.value?.variantAxes || []
  if (!axes.length) return variants.value.find(variant => variant.isDefault && variant.quantity > 0) || variants.value.find(variant => variant.quantity > 0)
  if (!axes.every(axis => selected[axis.id])) return undefined
  return variants.value.find(variant => variant.quantity > 0 && axes.every(axis =>
    variant.selections.some(selection => selection.axisId === axis.id && selection.valueId === selected[axis.id])))
})
const lowestPrice = computed(() => variants.value.length ? Math.min(...variants.value.map(variant => variant.salePrice)) : undefined)

const money = (value?: number) => value == null ? 'Liên hệ' : `${Number(value).toLocaleString('vi-VN')} ₫`
const attributeValue = (attribute: CatalogAttribute) => attribute.valueText
  ?? (attribute.valueNumber == null ? undefined : `${attribute.valueNumber}${attribute.unit ? ` ${attribute.unit}` : ''}`)
  ?? attribute.selectedOptions?.map(option => option.value).join(', ')
  ?? attribute.optionValues?.join(', ')
  ?? '-'
const available = (axisId: string, valueId: string) => variants.value.some(variant => variant.quantity > 0
  && variant.selections.some(selection => selection.axisId === axisId && selection.valueId === valueId)
  && (product.value?.variantAxes || []).filter(axis => axis.id !== axisId).every(axis => !selected[axis.id]
    || variant.selections.some(selection => selection.axisId === axis.id && selection.valueId === selected[axis.id])))
const variantLabel = (variant: CatalogVariant) => variant.selections.map(selection => `${selection.axisName}: ${selection.value}`).join(' · ')
const cartItem = () => ({
  idSPCT: current.value?.id,
  price: String(current.value?.salePrice || 0),
  quantity: String(quantity.value),
  variantLabel: variantLabel(current.value!),
  selections: current.value?.selections || [],
  productId: product.value?.id,
  name: product.value?.name,
  imageUrl: current.value?.imageUrl || selectedImage.value || product.value?.productImages?.[0]?.url,
  sellerId: product.value?.sellerId,
  shopName: shop.value?.shopName,
  sellerSlug: shop.value?.sellerSlug
})

const initializeSelection = () => {
  const preferred = variants.value.find(variant => variant.isDefault && variant.quantity > 0) || variants.value.find(variant => variant.quantity > 0)
  preferred?.selections.forEach(selection => { selected[selection.axisId] = selection.valueId })
}

const addCart = async () => {
  if (!current.value) return
  const user = localStorageAction.get(USER_INFO_STORAGE_KEY)
  try {
    if (user?.userId) {
      await createCartDetail({ idKhachHang: user.userId, idSPCT: current.value.id, price: String(current.value.salePrice), quantity: String(quantity.value) })
    } else {
      const cart = localStorageAction.get(CART_STORAGE_KEY) || []
      cart.push(cartItem())
      localStorageAction.set(CART_STORAGE_KEY, cart)
    }
    toast.success('Đã thêm vào giỏ')
    window.dispatchEvent(new Event('cartUpdated'))
  } catch {
    toast.error('Không thể thêm vào giỏ hàng')
  }
}
const buyNow = () => {
  if (!current.value) return
  localStorageAction.set(CHECKOUT_STORAGE_KEY, [cartItem()])
  void router.push('/thanh-toan')
}
const openShop = () => { if (shop.value) void router.push({ name: 'shop-detail', params: { sellerSlug: shop.value.sellerSlug } }) }
const openChat = () => {
  if (!shop.value) return
  const target = { name: 'buyer-chat', query: { sellerId: shop.value.id } }
  const user = localStorageAction.get(USER_INFO_STORAGE_KEY)
  if (user?.userId) {
    void router.push(target)
    return
  }
  void router.push({ name: 'Login', query: { redirect: router.resolve(target).fullPath } })
}
const formatDate = (value: string) => value ? new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium' }).format(new Date(value)) : ''

watch(current, variant => {
  if (!variant) quantity.value = 1
  else quantity.value = Math.max(1, Math.min(quantity.value, variant.quantity))
})

onMounted(async () => {
  try {
    product.value = await getCatalogProduct(String(route.params.idsp))
    selectedImage.value = product.value.productImages?.[0]?.url || ''
    initializeSelection()
    const requests: Promise<unknown>[] = [getPublicReviews({ productId: product.value.id }).then(response => { reviews.value = response.data || [] })]
    if (product.value.sellerId) {
      requests.push(getPublicShopsByIds([product.value.sellerId]).then(response => { shop.value = response.data?.[0] }))
    }
    await Promise.allSettled(requests)
  } catch (requestError: any) {
    error.value = requestError?.response?.data?.message || 'Không tải được sản phẩm'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.hero { width: 100%; height: 420px; object-fit: contain; background: #f8fafc; border-radius: 12px; }
.thumb-button { padding: 0; border: 2px solid transparent; border-radius: 8px; background: none; }
.thumb-button:hover { border-color: #2563eb; }
.thumb { width: 64px; height: 64px; object-fit: cover; border-radius: 6px; }
.price { font-size: 1.5rem; font-weight: 700; color: #dc2626; }
.rating-line, .review-stars { color: #d97706; }
.classification-block { padding: 16px; border: 1px solid #e5e7eb; border-radius: 10px; margin-bottom: 16px; }
.axis-group label { display: block; margin-bottom: 8px; }
.shop-card { display: flex; align-items: center; gap: 16px; padding: 18px; border: 1px solid #e5e7eb; border-radius: 12px; background: #fff; }
.shop-card > img { width: 64px; height: 64px; object-fit: cover; border-radius: 50%; }
.shop-main { display: grid; flex: 1; gap: 3px; }
.shop-main span { color: #64748b; font-size: 13px; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.detail-grid > section, .review-block { padding: 20px; border: 1px solid #e5e7eb; border-radius: 12px; }
.description { white-space: pre-line; }
.attrs { display: grid; grid-template-columns: minmax(140px, 1fr) 2fr; }
.attrs dt, .attrs dd { margin: 0; padding: 9px 0; border-bottom: 1px solid #e5e7eb; }
.attrs dt { font-weight: 600; color: #64748b; }
.review-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.review-head h5 { margin: 0; }
.review-item { padding: 14px 0; border-top: 1px solid #e5e7eb; }
.review-item small { color: #64748b; }
.review-item p { margin: 8px 0; }
.review-images { display: flex; gap: 8px; }
.review-images img { width: 72px; height: 72px; object-fit: cover; border-radius: 6px; }
.seller-reply { padding: 10px; border-radius: 8px; background: #eff6ff; color: #1e3a8a; }
@media (max-width: 767px) { .hero { height: 280px; } .detail-grid { grid-template-columns: 1fr; } .shop-card { align-items: flex-start; flex-wrap: wrap; } }
</style>
