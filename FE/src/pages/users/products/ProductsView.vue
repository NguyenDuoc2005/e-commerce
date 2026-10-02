<template>
  <div class="container py-3">
    <BreadCrumbUser :routes="[{ name: 'Trang chủ', path: '/' }, { name: 'Sản phẩm', path: '/san-pham' }]" title="Danh sách sản phẩm" />
    <div class="row g-4">
      <div class="col-12 col-lg-3">
        <FilterBox :model-value="filters" :facets="facets" @update:model-value="applyFilters" />
      </div>
      <main class="col">
        <div class="d-flex gap-2 mb-3 search-row">
          <div class="search-box">
            <input
              v-model="q"
              class="form-control"
              placeholder="Tìm kiếm sản phẩm…"
              aria-label="Tìm kiếm sản phẩm"
              autocomplete="off"
              @focus="autocompleteOpen = true"
              @blur="closeAutocomplete"
              @keyup.enter="submitSearch"
              @keydown.escape="autocompleteOpen = false"
            />
            <div v-if="autocompleteOpen && suggestions.length" class="autocomplete-menu">
              <button
                v-for="suggestion in suggestions"
                :key="suggestion.id"
                type="button"
                class="autocomplete-item"
                @mousedown.prevent="selectSuggestion(suggestion)"
              >
                <span>{{ suggestion.name }}</span>
                <small>{{ suggestion.categoryName }}</small>
              </button>
            </div>
          </div>
          <button class="btn btn-outline-primary" type="button" @click="submitSearch">Tìm</button>
          <select v-model="sort" class="form-select sort-select" aria-label="Sắp xếp sản phẩm" @change="changeSort">
            <option value="relevance">Liên quan</option>
            <option value="newest">Mới nhất</option>
            <option value="price_asc">Giá tăng dần</option>
            <option value="price_desc">Giá giảm dần</option>
            <option value="rating_desc">Đánh giá cao</option>
          </select>
        </div>

        <div v-if="recoveryNotice" class="alert alert-info" role="status" aria-live="polite">
          {{ recoveryNotice }}
        </div>
        <div v-if="error" class="alert alert-danger d-flex justify-content-between align-items-center gap-3" role="alert">
          <span>{{ error }}</span>
          <button class="btn btn-sm btn-outline-danger flex-shrink-0" type="button" @click="retry">Thử lại</button>
        </div>
        <div v-else-if="loading" class="text-center py-5" aria-live="polite">Đang tìm sản phẩm…</div>
        <div v-else-if="!products.length" class="text-center py-5 text-muted">Không tìm thấy sản phẩm phù hợp.</div>
        <template v-else>
          <div class="small text-muted mb-2">{{ totalElements.toLocaleString('vi-VN') }} sản phẩm</div>
          <div class="row g-3">
            <div v-for="product in products" :key="product.id" class="col-12 col-sm-6 col-xl-4">
              <article class="card h-100 product-card" @click="open(product.id)">
                <img :src="product.thumbnailUrl || placeholder" class="card-img-top" alt="Ảnh sản phẩm" />
                <div class="card-body">
                  <h6 class="card-title" v-html="highlightedName(product)" />
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
                </div>
              </article>
            </div>
          </div>
        </template>

        <nav v-if="totalPages > 1 && !loading && !error" class="mt-4" aria-label="Phân trang sản phẩm">
          <button class="btn btn-sm btn-outline-primary me-1 mb-1" type="button" :disabled="page === 0" @click="go(page)">‹</button>
          <button
            v-for="number in visiblePages"
            :key="number"
            class="btn btn-sm me-1 mb-1"
            :class="number === page + 1 ? 'btn-primary' : 'btn-outline-primary'"
            type="button"
            @click="go(number)"
          >
            {{ number }}
          </button>
          <button class="btn btn-sm btn-outline-primary me-1 mb-1" type="button" :disabled="page + 1 >= totalPages" @click="go(page + 2)">›</button>
        </nav>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import axios from 'axios'
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter, type LocationQueryRaw } from 'vue-router'
import DOMPurify from 'dompurify'
import BreadCrumbUser from '@/components/ui/Breadcrumbs/BreadCrumbUser.vue'
import FilterBox from './FilterBox.vue'
import {
  searchCatalogProducts,
  autocompleteCatalogProducts,
  closeCatalogSearchPit,
  type ProductAutocompleteSuggestion,
  type ProductSearchFacets,
  type ProductSearchItem,
  type ProductSearchParams,
  type ProductSearchSort
} from '@/services/api/catalog/catalog.api'
import { getPublicShopsByIds, type SellerResponse } from '@/services/api/seller/seller.api'

type FilterState = {
  categoryId?: string
  sellerId?: string
  color?: string
  sizeValue?: string
  minPrice?: number
  maxPrice?: number
}

type SearchState = ProductSearchParams & { sort: ProductSearchSort }

const DEFAULT_PAGE_SIZE = 12
const SEARCH_DEBOUNCE_MS = 400
const VALID_SORTS = new Set<ProductSearchSort>(['relevance', 'newest', 'price_asc', 'price_desc', 'rating_desc'])

const router = useRouter()
const route = useRoute()
const products = ref<ProductSearchItem[]>([])
const shops = ref(new Map<string, SellerResponse>())
const loading = ref(false)
const error = ref('')
const recoveryNotice = ref('')
const q = ref('')
const sort = ref<ProductSearchSort>('newest')
const page = ref(0)
const totalElements = ref(0)
const totalPages = ref(0)
const facets = ref<ProductSearchFacets>({ categories: [], shops: [], priceRanges: [], colors: [], sizes: [] })
const filters = ref<FilterState>({})
const suggestions = ref<ProductAutocompleteSuggestion[]>([])
const autocompleteOpen = ref(false)
const placeholder = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="300"%3E%3Crect width="100%" height="100%" fill="%23f1f5f9"/%3E%3C/svg%3E'

let activeController: AbortController | undefined
let requestSequence = 0
let searchTimer: number | undefined
let autocompleteTimer: number | undefined
let autocompleteController: AbortController | undefined
const cursorByPage = new Map<number, string>()
let activePitCursor: string | undefined
let activeSearchSessionKey: string | undefined
let recoveringExpiredCursor = false

const visiblePages = computed(() => {
  const first = Math.max(1, page.value + 1 - 2)
  const last = Math.min(totalPages.value, page.value + 1 + 2)
  return Array.from({ length: Math.max(0, last - first + 1) }, (_, index) => first + index)
})

const firstQueryValue = (value: unknown): string | undefined => {
  const candidate = Array.isArray(value) ? value[0] : value
  return typeof candidate === 'string' && candidate.trim() ? candidate.trim() : undefined
}

const nonNegativeNumber = (value: unknown): number | undefined => {
  const raw = firstQueryValue(value)
  if (raw === undefined) return undefined
  const parsed = Number(raw)
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : undefined
}

const nonNegativeInteger = (value: unknown, fallback: number): number => {
  const parsed = nonNegativeNumber(value)
  return parsed !== undefined && Number.isInteger(parsed) ? parsed : fallback
}

const readRouteState = (): SearchState => {
  const keyword = firstQueryValue(route.query.q) ?? firstQueryValue(route.query.keyword)
  const requestedSort = firstQueryValue(route.query.sort) as ProductSearchSort | undefined
  return {
    q: keyword,
    categoryId: firstQueryValue(route.query.categoryId),
    sellerId: firstQueryValue(route.query.sellerId),
    color: firstQueryValue(route.query.color),
    sizeValue: firstQueryValue(route.query.sizeValue),
    minPrice: nonNegativeNumber(route.query.minPrice),
    maxPrice: nonNegativeNumber(route.query.maxPrice),
    page: nonNegativeInteger(route.query.page, 0),
    size: Math.min(nonNegativeInteger(route.query.size, DEFAULT_PAGE_SIZE) || DEFAULT_PAGE_SIZE, 100),
    cursor: firstQueryValue(route.query.cursor),
    sort: requestedSort && VALID_SORTS.has(requestedSort)
      ? requestedSort
      : keyword ? 'relevance' : 'newest'
  }
}

const routeQueryFor = (state: SearchState): LocationQueryRaw => {
  const query: LocationQueryRaw = {
    page: String(state.page),
    size: String(state.size)
  }
  if (state.q) query.q = state.q
  if (state.categoryId) query.categoryId = state.categoryId
  if (state.sellerId) query.sellerId = state.sellerId
  if (state.color) query.color = state.color
  if (state.sizeValue) query.sizeValue = state.sizeValue
  if (state.minPrice !== undefined) query.minPrice = String(state.minPrice)
  if (state.maxPrice !== undefined) query.maxPrice = String(state.maxPrice)
  if (state.cursor) query.cursor = state.cursor

  const defaultSort = state.q ? 'relevance' : 'newest'
  if (state.sort !== defaultSort) query.sort = state.sort
  return query
}

const sameState = (left: SearchState, right: SearchState) => JSON.stringify(left) === JSON.stringify(right)
const searchSessionKey = (state: SearchState) => JSON.stringify({
  q: state.q,
  categoryId: state.categoryId,
  sellerId: state.sellerId,
  color: state.color,
  sizeValue: state.sizeValue,
  minPrice: state.minPrice,
  maxPrice: state.maxPrice,
  size: state.size,
  sort: state.sort
})

const closeActivePit = () => {
  const cursor = activePitCursor
  activePitCursor = undefined
  if (cursor) void closeCatalogSearchPit(cursor).catch(() => undefined)
}

const updateRoute = (patch: Partial<SearchState>, replace = false) => {
  const next = { ...readRouteState(), ...patch }
  if (sameState(next, readRouteState())) return false
  const location = { name: 'san-pham', query: routeQueryFor(next) }
  void (replace ? router.replace(location) : router.push(location))
  return true
}

const money = (value?: number) => value == null ? 'Liên hệ' : `${Number(value).toLocaleString('vi-VN')} ₫`
const shopFor = (sellerId?: string) => sellerId ? shops.value.get(sellerId) : undefined
const highlightedName = (product: ProductSearchItem) => DOMPurify.sanitize(
  product.highlights?.name?.[0] || product.name,
  { ALLOWED_TAGS: ['mark'], ALLOWED_ATTR: [] }
)

const load = async (state: SearchState) => {
  activeController?.abort()
  const controller = new AbortController()
  activeController = controller
  const sequence = ++requestSequence
  if (state.cursor) activePitCursor = state.cursor

  if (state.minPrice !== undefined && state.maxPrice !== undefined && state.minPrice > state.maxPrice) {
    products.value = []
    shops.value = new Map()
    totalElements.value = 0
    totalPages.value = 0
    error.value = 'Giá tối thiểu không được lớn hơn giá tối đa.'
    loading.value = false
    return
  }

  loading.value = true
  error.value = ''
  try {
    const response = await searchCatalogProducts(state, controller.signal)
    if (sequence !== requestSequence) return

    const sellerIds = [...new Set([
      ...response.items.map(item => item.sellerId),
      ...(response.facets?.shops || []).map(item => item.value)
    ].filter((id): id is string => Boolean(id)))]
    let nextShops = new Map<string, SellerResponse>()
    if (sellerIds.length) {
      try {
        const shopResponse = await getPublicShopsByIds(sellerIds)
        nextShops = new Map((shopResponse.data || []).map(shop => [shop.id, shop]))
      } catch {
        nextShops = new Map()
      }
    }
    if (sequence !== requestSequence) return

    products.value = response.items || []
    shops.value = nextShops
    page.value = response.page
    totalElements.value = response.totalElements || 0
    totalPages.value = response.totalPages || 0
    // Cursor pages deliberately skip expensive aggregations. Keep the facet
    // snapshot returned by the first page for the lifetime of this PIT.
    if (!state.cursor && response.facets) facets.value = response.facets
    if (response.nextCursor) {
      cursorByPage.set(state.page + 1, response.nextCursor)
      activePitCursor = response.nextCursor
    } else {
      activePitCursor = undefined
    }
    if (recoveringExpiredCursor && state.page === 0) {
      recoveringExpiredCursor = false
      recoveryNotice.value = 'Kết quả tìm kiếm đã cũ nên đã được tải lại từ trang đầu.'
    }
  } catch (requestError: any) {
    if (axios.isCancel(requestError) || requestError?.code === 'ERR_CANCELED' || sequence !== requestSequence) return
    if (requestError?.response?.status === 400 && requestError?.response?.data?.message === 'SEARCH_CURSOR_EXPIRED') {
      activePitCursor = undefined
      cursorByPage.clear()
      recoveringExpiredCursor = true
      recoveryNotice.value = 'Kết quả tìm kiếm đã cũ, đang tải lại từ trang đầu…'
      error.value = ''
      const fallbackState = { ...state, page: 0, cursor: undefined }
      if (!updateRoute({ page: 0, cursor: undefined }, true)) void load(fallbackState)
      return
    }
    products.value = []
    shops.value = new Map()
    totalElements.value = 0
    totalPages.value = 0
    facets.value = { categories: [], shops: [], priceRanges: [], colors: [], sizes: [] }
    error.value = requestError?.response?.status === 503
      ? 'Tìm kiếm tạm thời không khả dụng. Vui lòng thử lại sau.'
      : requestError?.response?.data?.message || 'Không thể tải kết quả tìm kiếm.'
  } finally {
    if (sequence === requestSequence) loading.value = false
  }
}

const submitSearch = () => {
  window.clearTimeout(searchTimer)
  const keyword = q.value.trim() || undefined
  const current = readRouteState()
  const nextSort = firstQueryValue(route.query.sort)
    ? current.sort
    : keyword ? 'relevance' : 'newest'
  autocompleteOpen.value = false
  recoveryNotice.value = ''
  if (keyword !== current.q) cursorByPage.clear()
  if (!updateRoute({ q: keyword, sort: nextSort, page: 0, cursor: undefined })) void load(current)
}

const applyFilters = (value: FilterState) => {
  recoveryNotice.value = ''
  cursorByPage.clear()
  updateRoute({ ...value, page: 0, cursor: undefined })
}

const changeSort = () => {
  recoveryNotice.value = ''
  cursorByPage.clear()
  updateRoute({ sort: sort.value, page: 0, cursor: undefined })
}
const go = (number: number) => {
  recoveryNotice.value = ''
  const targetPage = number - 1
  updateRoute({ page: targetPage, cursor: targetPage > 0 ? cursorByPage.get(targetPage) : undefined })
}
const retry = () => void load(readRouteState())
const open = (id: string) => router.push({ name: 'san-pham-chi-tiet', params: { idsp: id } })
const openShop = (sellerId?: string) => {
  const shop = shopFor(sellerId)
  if (shop) void router.push({ name: 'shop-detail', params: { sellerSlug: shop.sellerSlug } })
}

const selectSuggestion = (suggestion: ProductAutocompleteSuggestion) => {
  q.value = suggestion.name
  suggestions.value = []
  autocompleteOpen.value = false
  submitSearch()
}

const closeAutocomplete = () => window.setTimeout(() => { autocompleteOpen.value = false }, 120)

const loadAutocomplete = (value: string) => {
  window.clearTimeout(autocompleteTimer)
  autocompleteController?.abort()
  const keyword = value.trim()
  if (keyword.length < 2) {
    suggestions.value = []
    return
  }
  autocompleteTimer = window.setTimeout(async () => {
    const controller = new AbortController()
    autocompleteController = controller
    try {
      const response = await autocompleteCatalogProducts(keyword, controller.signal)
      suggestions.value = response.suggestions || []
      autocompleteOpen.value = true
    } catch (requestError: any) {
      if (!axios.isCancel(requestError) && requestError?.code !== 'ERR_CANCELED') suggestions.value = []
    }
  }, 250)
}

watch(q, (value) => {
  loadAutocomplete(value)
  const keyword = value.trim() || undefined
  window.clearTimeout(searchTimer)
  if (keyword === readRouteState().q) return
  searchTimer = window.setTimeout(() => submitSearch(), SEARCH_DEBOUNCE_MS)
})

watch(() => route.fullPath, () => {
  window.clearTimeout(searchTimer)
  const state = readRouteState()
  const nextSessionKey = searchSessionKey(state)
  if (activeSearchSessionKey && activeSearchSessionKey !== nextSessionKey) closeActivePit()
  activeSearchSessionKey = nextSessionKey
  q.value = state.q || ''
  sort.value = state.sort
  page.value = state.page
  filters.value = {
    categoryId: state.categoryId,
    sellerId: state.sellerId,
    color: state.color,
    sizeValue: state.sizeValue,
    minPrice: state.minPrice,
    maxPrice: state.maxPrice
  }
  void load(state)
}, { immediate: true })

onBeforeUnmount(() => {
  window.clearTimeout(searchTimer)
  window.clearTimeout(autocompleteTimer)
  activeController?.abort()
  autocompleteController?.abort()
  closeActivePit()
})
</script>

<style scoped>
.product-card { cursor: pointer; transition: transform .2s, box-shadow .2s; border-radius: 10px; overflow: hidden; }
.product-card:hover { transform: translateY(-3px); box-shadow: 0 10px 24px rgba(15, 23, 42, .12); }
.product-card img { height: 220px; object-fit: cover; }
.card-title { min-height: 2.5em; }
.sort-select { max-width: 190px; }
.search-box { position: relative; flex: 1 1 auto; min-width: 220px; }
.autocomplete-menu { position: absolute; z-index: 30; top: calc(100% + 4px); left: 0; right: 0; background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; box-shadow: 0 10px 24px rgba(15, 23, 42, .14); overflow: hidden; }
.autocomplete-item { width: 100%; display: flex; justify-content: space-between; gap: 12px; border: 0; border-bottom: 1px solid #f1f5f9; background: #fff; padding: 10px 12px; text-align: left; }
.autocomplete-item:hover { background: #f8fafc; }
.autocomplete-item small { color: #64748b; }
:deep(mark) { padding: 0; background: #fef08a; }
.product-meta { display: flex; justify-content: space-between; gap: 8px; margin: 8px 0 4px; color: #64748b; font-size: 12px; }
.shop-link { border: 0; padding: 0; background: transparent; color: #2563eb; font-size: 13px; font-weight: 600; }
@media (max-width: 767px) { .search-row { flex-wrap: wrap; } .sort-select { max-width: none; } }
</style>
