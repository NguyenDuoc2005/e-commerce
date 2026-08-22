<template>
  <aside class="filter-panel">
    <div class="filter-header">
      <h5>Bộ lọc</h5>
      <button v-if="hasFilters" class="clear-button" type="button" @click="resetFilters">Xóa</button>
    </div>

    <div class="filter-section">
      <label for="category-filter">Danh mục</label>
      <select id="category-filter" v-model="categoryId" class="form-select form-select-sm" @change="changeCategory">
        <option value="">Tất cả danh mục</option>
        <option v-for="category in categories" :key="category.value" :value="category.value">
          {{ category.label }}
        </option>
      </select>
    </div>

    <div class="filter-section">
      <div class="section-title">Khoảng giá</div>
      <div class="range-grid">
        <input v-model.number="priceMin" class="form-control form-control-sm" type="number" min="0" placeholder="Từ" @change="emitFilters" />
        <input v-model.number="priceMax" class="form-control form-control-sm" type="number" min="0" placeholder="Đến" @change="emitFilters" />
      </div>
    </div>

    <div v-if="loading" class="filter-loading">
      <span class="spinner-border spinner-border-sm" aria-hidden="true"></span>
    </div>

    <div v-for="filter in dynamicFilters" v-else :key="filter.attributeId" class="filter-section">
      <div class="section-title">{{ filter.name }}</div>
      <div v-if="filter.dataType === 'NUMBER'" class="range-grid">
        <input
          v-model.number="selections[filter.attributeId].min"
          class="form-control form-control-sm"
          type="number"
          :placeholder="filter.min == null ? 'Từ' : `Từ ${filter.min}`"
          @change="emitFilters"
        />
        <input
          v-model.number="selections[filter.attributeId].max"
          class="form-control form-control-sm"
          type="number"
          :placeholder="filter.max == null ? 'Đến' : `Đến ${filter.max}`"
          @change="emitFilters"
        />
      </div>
      <div v-else class="option-list">
        <label v-for="option in filter.options" :key="option.id" class="option-row">
          <input
            v-model="selections[filter.attributeId].values"
            type="checkbox"
            :value="option.id"
            @change="emitFilters"
          />
          <span>{{ option.value }}</span>
        </label>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { GetCategoryFilters, GetListDanhMuc, type DynamicCategoryFilter } from '@/services/api/permitall/sanpham/pmsanpham.api'

type Selection = { values: string[]; min?: number; max?: number }

const emit = defineEmits<{ filter: [value: { categoryId?: string; attributeFilters: Record<string, Selection>; giaTu?: number; giaDen?: number }] }>()
const categories = ref<{ label: string; value: string }[]>([])
const categoryId = ref('')
const dynamicFilters = ref<DynamicCategoryFilter[]>([])
const selections = reactive<Record<string, Selection>>({})
const priceMin = ref<number>()
const priceMax = ref<number>()
const loading = ref(false)

const activeAttributeFilters = computed(() => Object.fromEntries(
  Object.entries(selections).filter(([, selection]) =>
    selection.values.length > 0 || selection.min !== undefined || selection.max !== undefined
  )
))

const hasFilters = computed(() =>
  !!categoryId.value
  || priceMin.value !== undefined
  || priceMax.value !== undefined
  || Object.keys(activeAttributeFilters.value).length > 0
)

const emitFilters = () => emit('filter', {
  categoryId: categoryId.value || undefined,
  attributeFilters: activeAttributeFilters.value,
  giaTu: priceMin.value,
  giaDen: priceMax.value
})

const loadDynamicFilters = async () => {
  dynamicFilters.value = []
  Object.keys(selections).forEach((key) => delete selections[key])
  if (!categoryId.value) return
  loading.value = true
  try {
    dynamicFilters.value = await GetCategoryFilters(categoryId.value)
    dynamicFilters.value.forEach((filter) => {
      selections[filter.attributeId] = { values: [] }
    })
  } finally {
    loading.value = false
  }
}

const changeCategory = async () => {
  await loadDynamicFilters()
  emitFilters()
}

const resetFilters = async () => {
  categoryId.value = ''
  priceMin.value = undefined
  priceMax.value = undefined
  await loadDynamicFilters()
  emitFilters()
}

onMounted(async () => {
  const response = await GetListDanhMuc()
  categories.value = (response.data || []).map((item: any) => ({ label: item.ten || item.name, value: item.id }))
})
</script>

<style scoped>
.filter-panel { width: 100%; border: 1px solid #e5e7eb; background: #fff; }
.filter-header { min-height: 48px; display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; border-bottom: 1px solid #e5e7eb; }
.filter-header h5 { margin: 0; font-size: 16px; font-weight: 700; }
.clear-button { border: 0; background: transparent; color: #dc2626; font-size: 13px; padding: 4px; }
.filter-section { padding: 14px; border-bottom: 1px solid #e5e7eb; }
.filter-section > label,
.section-title { display: block; margin-bottom: 8px; color: #111827; font-size: 13px; font-weight: 700; }
.range-grid { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: 8px; }
.option-list { display: grid; gap: 8px; max-height: 220px; overflow-y: auto; }
.option-row { display: flex; align-items: center; gap: 8px; min-height: 24px; font-size: 13px; cursor: pointer; }
.option-row input { width: 16px; height: 16px; }
.filter-loading { display: flex; justify-content: center; padding: 18px; }
@media (max-width: 991px) {
  .filter-panel { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .filter-header { grid-column: 1 / -1; }
}
@media (max-width: 575px) {
  .filter-panel { grid-template-columns: 1fr; }
}
</style>
