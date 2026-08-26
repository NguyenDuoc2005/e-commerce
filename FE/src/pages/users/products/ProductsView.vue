<template>
  <div class="container py-3">
    <BreadCrumbUser :routes="[{ name: 'Trang chủ', path: '/' }, { name: 'Sản phẩm', path: '/san-pham' }]" title="Danh sách sản phẩm" />
    <div class="row g-4">
      <div class="col-12 col-lg-3"><FilterBox :initial-category-id="initialCategoryId" @filter="applyFilters" /></div>
      <main class="col">
        <div class="d-flex gap-2 mb-3 search-row">
          <input v-model="q" class="form-control" placeholder="Tìm kiếm sản phẩm…" @keyup.enter="reload" />
          <button class="btn btn-outline-primary" type="button" @click="reload">Tìm</button>
          <select v-model="sort" class="form-select sort-select" @change="reload">
            <option value="createdAt_desc">Mới nhất</option>
            <option value="createdAt_asc">Cũ nhất</option>
            <option value="price_asc">Giá tăng dần</option>
            <option value="price_desc">Giá giảm dần</option>
          </select>
        </div>
        <div v-if="error" class="alert alert-danger">{{ error }}</div>
        <div v-else-if="loading" class="text-center py-5">Đang tải…</div>
        <div v-else-if="!products.length" class="text-center py-5 text-muted">Không tìm thấy sản phẩm.</div>
        <div v-else class="row g-3">
          <div v-for="product in products" :key="product.id" class="col-12 col-sm-6 col-xl-4">
            <article class="card h-100 product-card" @click="open(product.id)">
              <img :src="product.thumbnailUrl || placeholder" class="card-img-top" alt="Ảnh sản phẩm" />
              <div class="card-body">
                <h6 class="card-title">{{ product.name }}</h6>
                <div class="text-danger fw-bold">
                  {{ money(product.minPrice) }}
                  <span v-if="product.maxPrice && product.maxPrice !== product.minPrice"> – {{ money(product.maxPrice) }}</span>
                </div>
                <div class="product-meta">
                  <span>★ {{ Number(product.ratingAverage || 0).toFixed(1) }} ({{ product.ratingCount || 0 }})</span>
                  <span>Đã bán {{ shopFor(product.sellerId)?.soldCount || 0 }}</span>
                </div>
                <button v-if="shopFor(product.sellerId)" class="shop-link" type="button" @click.stop="openShop(product.sellerId)">
                  {{ shopFor(product.sellerId)?.shopName }}
                </button>
                <div class="text-muted small">{{ product.category?.name || '' }} · {{ product.totalQuantity }} còn</div>
                <div v-for="attribute in (product.attributePreview || []).slice(0, 2)" :key="attribute.definitionId" class="small text-secondary">
                  {{ attribute.name }}: {{ attributeValue(attribute) }}
                </div>
              </div>
            </article>
          </div>
        </div>
        <nav v-if="totalPages > 1" class="mt-4">
          <button v-for="number in totalPages" :key="number" class="btn btn-sm me-1" :class="number === page + 1 ? 'btn-primary' : 'btn-outline-primary'" @click="go(number)">
            {{ number }}
          </button>
        </nav>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BreadCrumbUser from '@/components/ui/Breadcrumbs/BreadCrumbUser.vue'
import FilterBox from './FilterBox.vue'
import { getCatalogProducts, type CatalogAttribute, type CatalogSummary } from '@/services/api/catalog/catalog.api'
import { getPublicShopsByIds, type SellerResponse } from '@/services/api/seller/seller.api'

type FilterState = {
  categoryId?: string
  attributeFilters: Record<string, { values: string[]; min?: number; max?: number }>
  giaTu?: number
  giaDen?: number
}

const router = useRouter()
const route = useRoute()
const initialCategoryId = computed(() => typeof route.query.categoryId === 'string' ? route.query.categoryId : '')
const products = ref<CatalogSummary[]>([])
const shops = ref(new Map<string, SellerResponse>())
const loading = ref(false)
const error = ref('')
const q = ref('')
const sort = ref('createdAt_desc')
const page = ref(0)
const totalPages = ref(0)
const filters = ref<FilterState>({ attributeFilters: {} })
const placeholder = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="300"%3E%3Crect width="100%" height="100%" fill="%23f1f5f9"/%3E%3C/svg%3E'

const money = (value?: number) => value == null ? 'Liên hệ' : `${Number(value).toLocaleString('vi-VN')} ₫`
const attributeValue = (attribute: CatalogAttribute) => attribute.valueText
  ?? (attribute.valueNumber == null ? undefined : `${attribute.valueNumber}${attribute.unit ? ` ${attribute.unit}` : ''}`)
  ?? attribute.selectedOptions?.map(option => option.value).join(', ')
  ?? attribute.optionValues?.join(', ')
  ?? '-'
const shopFor = (sellerId?: string) => sellerId ? shops.value.get(sellerId) : undefined

const loadShops = async (items: CatalogSummary[]) => {
  const sellerIds = [...new Set(items.map(item => item.sellerId).filter((id): id is string => Boolean(id)))]
  if (!sellerIds.length) {
    shops.value = new Map()
    return
  }
  try {
    const response = await getPublicShopsByIds(sellerIds)
    shops.value = new Map((response.data || []).map(shop => [shop.id, shop]))
  } catch {
    shops.value = new Map()
  }
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await getCatalogProducts({
      page: page.value,
      size: 12,
      q: q.value || undefined,
      sort: sort.value,
      categoryId: filters.value.categoryId,
      attributeFilters: Object.keys(filters.value.attributeFilters).length ? JSON.stringify(filters.value.attributeFilters) : undefined,
      minPrice: filters.value.giaTu,
      maxPrice: filters.value.giaDen
    })
    products.value = response.content || []
    totalPages.value = response.totalPages || 0
    await loadShops(products.value)
  } catch (requestError: any) {
    error.value = requestError?.response?.data?.message || 'Không thể tải danh sách sản phẩm'
  } finally {
    loading.value = false
  }
}

const reload = () => { page.value = 0; void load() }
const applyFilters = (value: FilterState) => { filters.value = value; reload() }
const go = (number: number) => { page.value = number - 1; void load() }
const open = (id: string) => router.push({ name: 'san-pham-chi-tiet', params: { idsp: id } })
const openShop = (sellerId?: string) => {
  const shop = shopFor(sellerId)
  if (shop) void router.push({ name: 'shop-detail', params: { sellerSlug: shop.sellerSlug } })
}

onMounted(load)
</script>

<style scoped>
.product-card { cursor: pointer; transition: transform .2s, box-shadow .2s; border-radius: 10px; overflow: hidden; }
.product-card:hover { transform: translateY(-3px); box-shadow: 0 10px 24px rgba(15, 23, 42, .12); }
.product-card img { height: 220px; object-fit: cover; }
.card-title { min-height: 2.5em; }
.sort-select { max-width: 190px; }
.product-meta { display: flex; justify-content: space-between; gap: 8px; margin: 8px 0 4px; color: #64748b; font-size: 12px; }
.shop-link { border: 0; padding: 0; background: transparent; color: #2563eb; font-size: 13px; font-weight: 600; }
@media (max-width: 767px) { .search-row { flex-wrap: wrap; } .sort-select { max-width: none; } }
</style>
