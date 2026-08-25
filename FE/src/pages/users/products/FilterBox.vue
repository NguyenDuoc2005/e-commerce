<template>
  <aside class="filter-panel">
    <div class="filter-header">
      <h5>Bộ lọc</h5>
      <button v-if="hasFilters" class="clear-button" type="button" @click="reset">Xóa</button>
    </div>

    <div class="filter-section">
      <label>Danh mục</label>
      <select v-model="categoryId" class="form-select form-select-sm" @change="changeCategory">
        <option value="">Tất cả danh mục</option>
        <option v-for="category in categories" :key="category.id" :value="category.id" :disabled="category.disabled">
          {{ category.label }}
        </option>
      </select>
      <small class="filter-hint">Chọn danh mục cuối để tải đúng bộ lọc ngành hàng.</small>
    </div>

    <div class="filter-section">
      <div class="section-title">Khoảng giá</div>
      <div class="range-grid">
        <input v-model.number="priceMin" class="form-control form-control-sm" type="number" min="0" placeholder="Từ" @change="emitFilters" />
        <input v-model.number="priceMax" class="form-control form-control-sm" type="number" min="0" placeholder="Đến" @change="emitFilters" />
      </div>
    </div>

    <div v-if="loading" class="filter-loading">Đang tải bộ lọc động…</div>
    <div v-else-if="filterError" class="filter-error">{{ filterError }}</div>
    <template v-else>
      <div v-for="filter in dynamicFilters" :key="filter.definitionId" class="filter-section dynamic-filter">
        <div class="section-title">
          {{ filter.name }}
          <small v-if="filter.defaultUnit">({{ filter.defaultUnit }})</small>
        </div>
        <div v-if="filter.dataType === 'NUMBER'" class="range-grid">
          <input v-model.number="selection(filter.definitionId).min" class="form-control form-control-sm" type="number" placeholder="Từ" @change="emitFilters" />
          <input v-model.number="selection(filter.definitionId).max" class="form-control form-control-sm" type="number" placeholder="Đến" @change="emitFilters" />
        </div>
        <input
          v-else-if="filter.dataType === 'TEXT'"
          v-model="selection(filter.definitionId).text"
          class="form-control form-control-sm"
          :placeholder="`Nhập ${filter.name.toLocaleLowerCase('vi')}`"
          @change="emitFilters"
        />
        <label v-for="option in filter.options || []" v-else :key="option.id" class="option-row">
          <input
            v-model="selection(filter.definitionId).values"
            type="checkbox"
            :value="option.resolvedOptionId || option.id"
            @change="emitFilters"
          />
          <span>{{ option.value }}</span>
        </label>
        <small v-if="filter.dataType.startsWith('SELECT') && !filter.options?.length" class="filter-hint">Chưa có giá trị để lọc.</small>
      </div>
      <div v-if="categoryId && !dynamicFilters.length" class="filter-loading">Danh mục này chưa cấu hình thuộc tính dùng để lọc.</div>
    </template>
  </aside>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { getCategoryFilters, getCategoryTree } from '@/services/api/catalog/catalog.api'

type Selection = { values: string[]; text?: string; min?: number; max?: number }
type DynamicFilter = {
  definitionId: string
  name: string
  dataType: 'TEXT' | 'NUMBER' | 'SELECT_ONE' | 'SELECT_MULTI'
  defaultUnit?: string
  filterable: boolean
  options: Array<{ id: string; value: string; resolvedOptionId?: string }>
}
type CategoryOption = { id: string; label: string; disabled: boolean }
type FilterPayload = {
  categoryId?: string
  attributeFilters: Record<string, { values: string[]; min?: number; max?: number }>
  giaTu?: number
  giaDen?: number
}

const emit = defineEmits<{ filter: [value: FilterPayload] }>()
const categories = ref<CategoryOption[]>([])
const categoryId = ref('')
const dynamicFilters = ref<DynamicFilter[]>([])
const selections = reactive<Record<string, Selection>>({})
const priceMin = ref<number>()
const priceMax = ref<number>()
const loading = ref(false)
const filterError = ref('')

const active = computed(() => Object.fromEntries(Object.entries(selections).flatMap(([id, item]) => {
  const values = item.text?.trim() ? [item.text.trim()] : item.values
  if (!values.length && item.min === undefined && item.max === undefined) return []
  return [[id, { values, min: item.min, max: item.max }]]
})))
const hasFilters = computed(() => Boolean(categoryId.value || priceMin.value !== undefined || priceMax.value !== undefined || Object.keys(active.value).length))
const selection = (id: string) => selections[id] || (selections[id] = { values: [] })

const emitFilters = () => emit('filter', {
  categoryId: categoryId.value || undefined,
  attributeFilters: active.value,
  giaTu: priceMin.value,
  giaDen: priceMax.value
})

const clearDynamicSelections = () => Object.keys(selections).forEach(key => delete selections[key])

const changeCategory = async () => {
  dynamicFilters.value = []
  clearDynamicSelections()
  filterError.value = ''
  if (categoryId.value) {
    loading.value = true
    try {
      const suggestions = await getCategoryFilters(categoryId.value)
      dynamicFilters.value = suggestions.filter((item: DynamicFilter) => item.filterable)
    } catch (error: any) {
      filterError.value = error?.response?.data?.message || 'Không tải được bộ lọc của danh mục.'
    } finally {
      loading.value = false
    }
  }
  emitFilters()
}

const reset = async () => {
  categoryId.value = ''
  priceMin.value = undefined
  priceMax.value = undefined
  await changeCategory()
}

onMounted(async () => {
  const flatten = (nodes: any[], prefix = ''): CategoryOption[] => nodes.flatMap(node => {
    const children = node.children || node.childCategories || []
    const label = prefix ? `${prefix} / ${node.name}` : node.name
    return [{ id: node.id, label, disabled: children.length > 0 }, ...flatten(children, label)]
  })
  try {
    categories.value = flatten(await getCategoryTree())
  } catch {
    filterError.value = 'Không tải được cây danh mục.'
  }
})
</script>

<style scoped>
.filter-panel { width: 100%; border: 1px solid #e5e7eb; background: #fff; border-radius: 10px; overflow: hidden; }
.filter-header { display: flex; justify-content: space-between; padding: 12px 14px; border-bottom: 1px solid #e5e7eb; }
.filter-header h5 { margin: 0; }
.clear-button { border: 0; background: transparent; color: #dc2626; }
.filter-section { padding: 14px; border-bottom: 1px solid #e5e7eb; }
.filter-section label, .section-title { display: block; margin-bottom: 8px; font-weight: 700; font-size: 13px; }
.section-title small { color: #64748b; font-weight: 500; }
.range-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.option-row { display: flex !important; gap: 8px; margin: 7px 0; font-weight: 400 !important; }
.filter-loading, .filter-error { padding: 14px; color: #64748b; font-size: 13px; }
.filter-error { color: #b91c1c; }
.filter-hint { display: block; margin-top: 7px; color: #64748b; font-size: 11px; }
</style>
