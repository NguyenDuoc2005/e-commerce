<template>
  <aside class="filter-panel">
    <div class="filter-header">
      <h5>Bộ lọc</h5>
      <button v-if="hasFilters" class="clear-button" type="button" @click="reset">Xóa</button>
    </div>

    <div class="filter-section">
      <label for="product-category-filter">Danh mục</label>
      <select id="product-category-filter" v-model="categoryId" class="form-select form-select-sm" @change="emitFilters">
        <option value="">Tất cả danh mục</option>
        <option v-for="category in categories" :key="category.id" :value="category.id" :disabled="category.disabled">
          {{ category.label }}{{ facetCount('categories', category.id) !== undefined ? ` (${facetCount('categories', category.id)})` : '' }}
        </option>
      </select>
      <small v-if="categoryError" class="filter-error">{{ categoryError }}</small>
    </div>

    <div class="filter-section">
      <label for="product-seller-filter">Gian hàng</label>
      <select id="product-seller-filter" v-model="sellerId" class="form-select form-select-sm" @change="emitFilters">
        <option value="">Tất cả gian hàng</option>
        <option v-if="sellerId && !sellers.some(seller => seller.id === sellerId)" :value="sellerId">
          Gian hàng đã chọn
        </option>
        <option v-for="seller in sellers" :key="seller.id" :value="seller.id">
          {{ seller.shopName }}{{ facetCount('shops', seller.id) !== undefined ? ` (${facetCount('shops', seller.id)})` : '' }}
        </option>
      </select>
      <small v-if="sellerError" class="filter-error">{{ sellerError }}</small>
    </div>

    <div class="filter-section">
      <div class="section-title">Khoảng giá</div>
      <div class="range-grid">
        <input v-model.number="minPrice" class="form-control form-control-sm" type="number" min="0" placeholder="Từ" @change="emitFilters" />
        <input v-model.number="maxPrice" class="form-control form-control-sm" type="number" min="0" placeholder="Đến" @change="emitFilters" />
      </div>
      <div v-if="facets.priceRanges.length" class="facet-pills">
        <button v-for="range in facets.priceRanges" :key="range.key" type="button" @click="applyPriceRange(range.from, range.to)">
          {{ priceRangeLabel(range.from, range.to) }} ({{ range.count }})
        </button>
      </div>
    </div>

    <div v-if="facets.colors.length" class="filter-section">
      <label for="product-color-filter">Màu sắc</label>
      <select id="product-color-filter" v-model="color" class="form-select form-select-sm" @change="emitFilters">
        <option value="">Tất cả màu</option>
        <option v-for="item in facets.colors" :key="item.value" :value="item.value">{{ item.value }} ({{ item.count }})</option>
      </select>
    </div>

    <div v-if="facets.sizes.length" class="filter-section">
      <label for="product-size-filter">Kích thước</label>
      <select id="product-size-filter" v-model="sizeValue" class="form-select form-select-sm" @change="emitFilters">
        <option value="">Tất cả kích thước</option>
        <option v-for="item in facets.sizes" :key="item.value" :value="item.value">{{ item.value }} ({{ item.count }})</option>
      </select>
    </div>

    <div v-if="loading" class="filter-loading">Đang tải bộ lọc…</div>
  </aside>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { getCategoryTree } from '@/services/api/catalog/catalog.api'
import { getPublicShops, type SellerResponse } from '@/services/api/seller/seller.api'
import type { ProductSearchFacets } from '@/services/api/catalog/catalog.api'

interface ProductFilterState {
  categoryId?: string
  sellerId?: string
  color?: string
  sizeValue?: string
  minPrice?: number
  maxPrice?: number
}

type CategoryOption = { id: string; label: string; disabled: boolean }

const props = defineProps<{
  modelValue: ProductFilterState
  facets: ProductSearchFacets
}>()

const emit = defineEmits<{
  'update:modelValue': [value: ProductFilterState]
}>()

const categories = ref<CategoryOption[]>([])
const sellers = ref<SellerResponse[]>([])
const categoryId = ref('')
const sellerId = ref('')
const color = ref('')
const sizeValue = ref('')
const minPrice = ref<number>()
const maxPrice = ref<number>()
const loading = ref(false)
const categoryError = ref('')
const sellerError = ref('')

const hasFilters = computed(() => Boolean(
  categoryId.value
  || sellerId.value
  || color.value
  || sizeValue.value
  || minPrice.value !== undefined
  || maxPrice.value !== undefined
))

const syncFromProps = (value: ProductFilterState) => {
  categoryId.value = value.categoryId || ''
  sellerId.value = value.sellerId || ''
  color.value = value.color || ''
  sizeValue.value = value.sizeValue || ''
  minPrice.value = value.minPrice
  maxPrice.value = value.maxPrice
}

const emitFilters = () => emit('update:modelValue', {
  categoryId: categoryId.value || undefined,
  sellerId: sellerId.value || undefined,
  color: color.value || undefined,
  sizeValue: sizeValue.value || undefined,
  minPrice: minPrice.value,
  maxPrice: maxPrice.value
})

const reset = () => {
  categoryId.value = ''
  sellerId.value = ''
  color.value = ''
  sizeValue.value = ''
  minPrice.value = undefined
  maxPrice.value = undefined
  emitFilters()
}

const facetCount = (facet: 'categories' | 'shops', value: string) =>
  props.facets[facet].find(item => item.value === value)?.count

const priceRangeLabel = (from?: number, to?: number) => {
  if (from == null) return `Dưới ${Number(to).toLocaleString('vi-VN')} ₫`
  if (to == null) return `Từ ${Number(from).toLocaleString('vi-VN')} ₫`
  return `${Number(from).toLocaleString('vi-VN')}–${Number(to).toLocaleString('vi-VN')} ₫`
}

const applyPriceRange = (from?: number, to?: number) => {
  minPrice.value = from
  maxPrice.value = to
  emitFilters()
}

watch(() => props.modelValue, syncFromProps, { immediate: true, deep: true })

onMounted(async () => {
  loading.value = true
  const flatten = (nodes: any[], prefix = ''): CategoryOption[] => nodes.flatMap(node => {
    const children = node.children || node.childCategories || []
    const label = prefix ? `${prefix} / ${node.name}` : node.name
    return [{ id: String(node.id), label, disabled: children.length > 0 }, ...flatten(children, label)]
  })

  const [categoryResult, sellerResult] = await Promise.allSettled([
    getCategoryTree(),
    getPublicShops()
  ])

  if (categoryResult.status === 'fulfilled') {
    categories.value = flatten(categoryResult.value)
  } else {
    categoryError.value = 'Không tải được cây danh mục.'
  }

  if (sellerResult.status === 'fulfilled') {
    sellers.value = sellerResult.value.data ?? []
  } else {
    sellerError.value = 'Không tải được danh sách gian hàng.'
  }
  loading.value = false
})
</script>

<style scoped>
.filter-panel { width: 100%; border: 1px solid #e5e7eb; background: #fff; border-radius: 10px; overflow: hidden; }
.filter-header { display: flex; justify-content: space-between; padding: 12px 14px; border-bottom: 1px solid #e5e7eb; }
.filter-header h5 { margin: 0; }
.clear-button { border: 0; background: transparent; color: #dc2626; }
.filter-section { padding: 14px; border-bottom: 1px solid #e5e7eb; }
.filter-section label, .section-title { display: block; margin-bottom: 8px; font-weight: 700; font-size: 13px; }
.range-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.facet-pills { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 9px; }
.facet-pills button { border: 1px solid #cbd5e1; border-radius: 999px; background: #fff; padding: 4px 8px; color: #475569; font-size: 11px; }
.filter-loading, .filter-error { display: block; padding: 8px 14px; color: #64748b; font-size: 12px; }
.filter-error { padding: 7px 0 0; color: #b91c1c; }
</style>
