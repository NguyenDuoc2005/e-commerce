<template>
  <div class="shop-page">
    <a-skeleton v-if="loading" active />
    <template v-else-if="shop">
      <section class="shop-cover" :style="coverStyle">
        <div class="shop-info">
          <a-avatar :size="72" :src="shop.logoUrl">{{ shop.shopName?.[0] }}</a-avatar>
          <div>
            <h1>{{ shop.shopName }}</h1>
            <p>{{ shop.description }}</p>
            <a-space>
              <a-tag color="green">Da duyet</a-tag>
              <span>{{ shop.followerCount ?? 0 }} luot theo doi</span>
              <span>{{ shop.rating ?? 0 }} sao</span>
              <a-button v-if="authStore.isAuthenticated" :loading="followLoading" @click="toggleFollow">
                {{ following ? 'Bo theo doi' : 'Theo doi shop' }}
              </a-button>
              <a-button type="primary" @click="openChat">Chat với shop</a-button>
              <ReportButton target-type="SHOP" :target-id="shop.id" />
            </a-space>
          </div>
        </div>
      </section>

      <section class="shop-products">
        <div class="section-title">
          <h2>San pham cua shop</h2>
          <span>{{ totalElements }} san pham</span>
        </div>

        <a-skeleton v-if="productsLoading" active />
        <a-empty v-else-if="!products.length" description="Shop chua co san pham dang ban" />
        <div v-else class="product-grid">
          <div v-for="item in products" :key="item.id" class="product-card" @click="goProduct(item.id)">
            <div class="product-image">
              <img :src="productImage(item)" alt="product" />
            </div>
            <div class="product-body">
            <h3 :title="item.name">{{ item.name }}</h3>
              <div class="price-row">
                <strong>{{ formatVND(item.minPrice) }}</strong>
                <span v-if="item.maxPrice && item.maxPrice !== item.minPrice">– {{ formatVND(item.maxPrice) }}</span>
              </div>
              <div class="meta-row">
                <span>{{ item.category?.name || 'Sản phẩm' }}</span>
                <span>{{ item.totalQuantity }} còn</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="totalPages > 1" class="pagination-row">
          <a-pagination v-model:current="currentPage" :total="totalElements" :page-size="pageSize" @change="loadProducts" />
        </div>
      </section>

      <section class="shop-reviews">
        <div class="section-title">
          <h2>Danh gia shop</h2>
          <span>{{ reviews.length }} danh gia</span>
        </div>
        <a-empty v-if="!reviews.length" description="Shop chua co danh gia" />
        <a-list v-else :data-source="reviews" item-layout="vertical">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-rate :value="item.shopRating" disabled />
              <p>{{ item.comment || 'Khach hang khong de lai binh luan.' }}</p>
              <a-alert v-if="item.sellerReply" type="info" :message="`Phan hoi cua shop: ${item.sellerReply}`" />
              <ReportButton target-type="REVIEW" :target-id="item.id" label="Báo cáo đánh giá" />
            </a-list-item>
          </template>
        </a-list>
      </section>
    </template>
    <a-empty v-else description="Khong tim thay shop hoac shop chua duoc duyet" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { followShop, getPublicShop, getShopFollowState, unfollowShop, type SellerResponse } from '@/services/api/seller/seller.api'
import { getPublicReviews, type Review } from '@/services/api/seller/review.api'
import { getCatalogProducts, type CatalogSummary } from '@/services/api/catalog/catalog.api'
import { useAuthStore } from '@/stores/auth'
import ReportButton from '@/components/report/ReportButton.vue'

type PublicSellerResponse = SellerResponse & {
  rating?: number
  soldCount?: number
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const productsLoading = ref(false)
const shop = ref<PublicSellerResponse | null>(null)
const products = ref<CatalogSummary[]>([])
const currentPage = ref(1)
const pageSize = 12
const totalElements = ref(0)
const following = ref(false)
const followLoading = ref(false)
const reviews = ref<Review[]>([])

const totalPages = computed(() => Math.ceil(totalElements.value / pageSize))
const coverStyle = computed(() => ({
  backgroundImage: shop.value?.coverImageUrl ? `linear-gradient(rgba(0,0,0,.36), rgba(0,0,0,.36)), url(${shop.value.coverImageUrl})` : undefined
}))

const formatVND = (price: number | undefined): string => {
  if (price === undefined || price === null) return '0 VND'
  return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.') + ' VND'
}

const productImage = (item: CatalogSummary) => item.thumbnailUrl || 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="300"%3E%3Crect width="100%" height="100%" fill="%23f1f5f9"/%3E%3C/svg%3E'

const loadProducts = async () => {
  if (!shop.value?.id) return
  productsLoading.value = true
  try {
    const res = await getCatalogProducts({
      page: currentPage.value - 1,
      size: pageSize,
      sellerId: shop.value.id,
      sort: 'createdAt_desc'
    })
    products.value = res.content ?? []
    totalElements.value = res.totalElements ?? 0
  } finally {
    productsLoading.value = false
  }
}

const loadShop = async () => {
  loading.value = true
  try {
    const res = await getPublicShop(String(route.params.sellerSlug))
    shop.value = res.data
    currentPage.value = 1
    await Promise.all([loadProducts(), loadReviews(), loadFollowState()])
  } catch (error: any) {
    if (error?.response?.status !== 404) {
      message.error(error?.response?.data?.message ?? 'Khong tai duoc shop')
    }
    shop.value = null
  } finally {
    loading.value = false
  }
}

const loadReviews = async () => {
  if (!shop.value?.id) return
  try {
    const response = await getPublicReviews({ sellerId: shop.value.id })
    reviews.value = response.data ?? []
  } catch {
    reviews.value = []
  }
}

const loadFollowState = async () => {
  if (!authStore.isAuthenticated || !shop.value?.id) return
  try {
    const response = await getShopFollowState(shop.value.id)
    following.value = response.data.following
    shop.value.followerCount = response.data.followerCount
  } catch {
    following.value = false
  }
}

const toggleFollow = async () => {
  if (!shop.value) return
  followLoading.value = true
  try {
    const response = following.value ? await unfollowShop(shop.value.id) : await followShop(shop.value.id)
    following.value = response.data.following
    shop.value.followerCount = response.data.followerCount
  } catch (error: any) {
    message.error(error?.response?.data?.message ?? 'Khong cap nhat duoc trang thai theo doi')
  } finally {
    followLoading.value = false
  }
}

const goProduct = (id: string) => {
  router.push({ name: 'san-pham-chi-tiet', params: { idsp: id } })
}

const openChat = () => {
  if (!shop.value) return
  const target = { name: 'buyer-chat', query: { sellerId: shop.value.id } }
  if (authStore.isAuthenticated) {
    void router.push(target)
    return
  }
  void router.push({ name: 'Login', query: { redirect: router.resolve(target).fullPath } })
}

onMounted(loadShop)
</script>

<style scoped>
.shop-page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.shop-cover {
  min-height: 260px;
  display: flex;
  align-items: flex-end;
  padding: 28px;
  border-radius: 8px;
  background: #172033;
  background-size: cover;
  background-position: center;
  color: #ffffff;
}

.shop-info {
  display: flex;
  gap: 18px;
  align-items: center;
}

.shop-info h1 {
  margin: 0 0 8px;
  font-size: 32px;
  font-weight: 800;
}

.shop-info p {
  margin: 0 0 12px;
  max-width: 680px;
}

.shop-products {
  margin-top: 24px;
}

.shop-reviews {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 16px;
}

.section-title h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
}

.section-title span {
  color: #6b7280;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
  gap: 16px;
}

.product-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  background: #ffffff;
  cursor: pointer;
  transition: transform .18s ease, box-shadow .18s ease;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 22px rgba(15, 23, 42, .12);
}

.product-image {
  aspect-ratio: 1;
  background: #f3f4f6;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-body {
  padding: 12px;
}

.product-body h3 {
  min-height: 40px;
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  line-height: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-row strong {
  color: #e11d48;
}

.price-row span {
  color: #9ca3af;
  font-size: 12px;
  text-decoration: line-through;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-top: 8px;
  color: #6b7280;
  font-size: 12px;
}

.pagination-row {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
