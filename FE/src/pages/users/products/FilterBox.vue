<template>
  <div class="filter-container">
    <div class="filter-header">
      <h5 class="filter-title">
        <i class="bi bi-funnel-fill me-2"></i>
        Bộ lọc sản phẩm
      </h5>
      <button v-if="!isFiltersEmpty" @click="resetFilters" class="btn-clear-all">
        <i class="bi bi-x-circle me-1"></i>
        Xóa tất cả
      </button>
    </div>

    <!-- Loading state -->
    <div v-if="isLoading" class="loading-state">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Đang tải...</span>
      </div>
      <p class="mt-2 mb-0">Đang tải bộ lọc...</p>
    </div>

    <!-- Filter Sections -->
    <div v-else class="filter-sections">
      <!-- Price Range Filter -->
      <div class="filter-section">
        <div class="filter-section-header" @click="toggleSection('gia')">
          <span class="filter-section-title">
            <i class="bi bi-currency-dollar me-2"></i>
            Khoảng giá
            <span v-if="filters.giaTu || filters.giaDen" class="filter-count">
              {{ formatPriceRange() }}
            </span>
          </span>
          <i class="bi toggle-icon" :class="sectionOpen.gia ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
        </div>
        <transition name="slide-fade">
          <div v-show="sectionOpen.gia" class="filter-section-content">
            <div class="price-range-container">
              <div class="price-input-group">
                <div class="price-input-wrapper">
                  <input 
                    type="text" 
                    class="form-control price-input" 
                    v-model="priceFromDisplay"
                    @input="handlePriceInput('giaTu', $event)"
                    @blur="formatPriceDisplay('from')"
                    placeholder="Từ (₫)"
                  />
                </div>
                <span class="price-separator">-</span>
                <div class="price-input-wrapper">
                  <input 
                    type="text" 
                    class="form-control price-input" 
                    v-model="priceToDisplay"
                    @input="handlePriceInput('giaDen', $event)"
                    @blur="formatPriceDisplay('to')"
                    placeholder="Đến (₫)"
                  />
                </div>
              </div>
              <div class="price-quick-select">
                <button 
                  v-for="range in priceRanges" 
                  :key="range.label"
                  @click="selectPriceRange(range)"
                  class="btn-price-range"
                  :class="{ 'active': isPriceRangeActive(range) }"
                >
                  {{ range.label }}
                </button>
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- Brand Filter -->
      <div class="filter-section">
        <div class="filter-section-header" @click="toggleSection('thuongHieu')">
          <span class="filter-section-title">
            <i class="bi bi-award me-2"></i>
            Thương hiệu
            <span v-if="filters.thuongHieu.length" class="filter-count">{{ filters.thuongHieu.length }}</span>
          </span>
          <i class="bi toggle-icon" :class="sectionOpen.thuongHieu ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
        </div>
        <transition name="slide-fade">
          <div v-show="sectionOpen.thuongHieu" class="filter-section-content">
            <div class="search-box mb-3" v-if="thuongHieuOptions.length > 5">
              <i class="bi bi-search search-icon"></i>
              <input 
                v-model="brandSearch" 
                type="text" 
                class="form-control search-input" 
                placeholder="Tìm thương hiệu..."
              />
            </div>
            <div class="checkbox-list">
              <div 
                v-for="brand in filteredBrands" 
                :key="brand.value" 
                class="checkbox-item"
              >
                <input 
                  :id="'brand-' + brand.value"
                  v-model="filters.thuongHieu" 
                  :value="brand.value" 
                  type="checkbox" 
                  class="form-check-input"
                  @change="applyFilters"
                />
                <label :for="'brand-' + brand.value" class="form-check-label">
                  {{ brand.label }}
                </label>
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- Material Filter -->
      <div class="filter-section">
        <div class="filter-section-header" @click="toggleSection('chatLieu')">
          <span class="filter-section-title">
            <i class="bi bi-palette me-2"></i>
            Chất liệu
            <span v-if="filters.chatLieu.length" class="filter-count">{{ filters.chatLieu.length }}</span>
          </span>
          <i class="bi toggle-icon" :class="sectionOpen.chatLieu ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
        </div>
        <transition name="slide-fade">
          <div v-show="sectionOpen.chatLieu" class="filter-section-content">
            <div class="checkbox-list">
              <div 
                v-for="material in chatLieuOptions" 
                :key="material.value" 
                class="checkbox-item"
              >
                <input 
                  :id="'material-' + material.value"
                  v-model="filters.chatLieu" 
                  :value="material.value" 
                  type="checkbox" 
                  class="form-check-input"
                  @change="applyFilters"
                />
                <label :for="'material-' + material.value" class="form-check-label">
                  {{ material.label }}
                </label>
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- Sole Type Filter -->
      <div class="filter-section">
        <div class="filter-section-header" @click="toggleSection('loaiDe')">
          <span class="filter-section-title">
            <i class="bi bi-layers me-2"></i>
            Loại đế
            <span v-if="filters.loaiDe.length" class="filter-count">{{ filters.loaiDe.length }}</span>
          </span>
          <i class="bi toggle-icon" :class="sectionOpen.loaiDe ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
        </div>
        <transition name="slide-fade">
          <div v-show="sectionOpen.loaiDe" class="filter-section-content">
            <div class="checkbox-list">
              <div 
                v-for="sole in loaiDeOptions" 
                :key="sole.value" 
                class="checkbox-item"
              >
                <input 
                  :id="'sole-' + sole.value"
                  v-model="filters.loaiDe" 
                  :value="sole.value" 
                  type="checkbox" 
                  class="form-check-input"
                  @change="applyFilters"
                />
                <label :for="'sole-' + sole.value" class="form-check-label">
                  {{ sole.label }}
                </label>
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- Category Filter -->
      <div class="filter-section">
        <div class="filter-section-header" @click="toggleSection('danhMuc')">
          <span class="filter-section-title">
            <i class="bi bi-grid me-2"></i>
            Danh mục
            <span v-if="filters.danhMuc.length" class="filter-count">{{ filters.danhMuc.length }}</span>
          </span>
          <i class="bi toggle-icon" :class="sectionOpen.danhMuc ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
        </div>
        <transition name="slide-fade">
          <div v-show="sectionOpen.danhMuc" class="filter-section-content">
            <div class="checkbox-list">
              <div 
                v-for="category in danhMucOptions" 
                :key="category.value" 
                class="checkbox-item"
              >
                <input 
                  :id="'category-' + category.value"
                  v-model="filters.danhMuc" 
                  :value="category.value" 
                  type="checkbox" 
                  class="form-check-input"
                  @change="applyFilters"
                />
                <label :for="'category-' + category.value" class="form-check-label">
                  {{ category.label }}
                </label>
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- Color Filter -->
      <div class="filter-section" v-if="ColorOptions.length">
        <div class="filter-section-header" @click="toggleSection('mauSac')">
          <span class="filter-section-title">
            <i class="bi bi-circle-fill me-2"></i>
            Màu sắc
            <span v-if="filters.mauSac.length" class="filter-count">{{ filters.mauSac.length }}</span>
          </span>
          <i class="bi toggle-icon" :class="sectionOpen.mauSac ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
        </div>
        <transition name="slide-fade">
          <div v-show="sectionOpen.mauSac" class="filter-section-content">
            <div class="color-grid">
              <div 
                v-for="color in ColorOptions" 
                :key="color.value" 
                class="color-item"
                :class="{ 'selected': filters.mauSac.includes(color.value) }"
                @click="toggleMauSac(color.value)"
              >
                <div 
                  class="color-preview" 
                  :style="{ backgroundColor: color.color }"
                  :title="color.label"
                ></div>
                <span class="color-name">{{ color.label }}</span>
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- Size Filter -->
      <div class="filter-section" v-if="SizeOptions.length">
        <div class="filter-section-header" @click="toggleSection('kichCo')">
          <span class="filter-section-title">
            <i class="bi bi-rulers me-2"></i>
            Kích cỡ
            <span v-if="filters.kichCo.length" class="filter-count">{{ filters.kichCo.length }}</span>
          </span>
          <i class="bi toggle-icon" :class="sectionOpen.kichCo ? 'bi-chevron-up' : 'bi-chevron-down'"></i>
        </div>
        <transition name="slide-fade">
          <div v-show="sectionOpen.kichCo" class="filter-section-content">
            <div class="size-grid">
              <div 
                v-for="size in SizeOptions" 
                :key="size.value" 
                class="size-item"
                :class="{ 'selected': filters.kichCo.includes(size.value) }"
                @click="toggleKichCo(size.value)"
              >
                {{ size.label }}
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { GetListChatLieu, GetListDanhMuc, GetListLoaiDe, GetListThuongHieu,  GetListColor, GetListSize } from '@/services/api/permitall/sanpham/pmsanpham.api'
import { reactive, ref, defineEmits, onMounted, computed, watch } from 'vue'

const emit = defineEmits(['filter'])
const isLoading = ref(true)
const error = ref<string | null>(null)

const ColorOptions = ref<{ label: string; value: string; color: string; }[]>([])
const SizeOptions = ref<{ label: string; value: string }[]>([])
const thuongHieuOptions = ref<{ label: string; value: string }[]>([])
const loaiDeOptions = ref<{ label: string; value: string }[]>([])
const danhMucOptions = ref<{ label: string; value: string }[]>([])
const chatLieuOptions = ref<{ label: string; value: string }[]>([])

const filters = reactive({
  thuongHieu: [] as string[],
  mauSac: [] as string[],
  kichCo: [] as string[],
  chatLieu: [] as string[],
  loaiDe: [] as string[],
  danhMuc: [] as string[],
  xuatXu: [] as string[],
  giaTu: undefined as number | undefined,
  giaDen: undefined as number | undefined
})

const sectionOpen = reactive<Record<string, boolean>>({
  gia: true,
  thuongHieu: true,
  mauSac: false,
  kichCo: false,
  chatLieu: false,
  loaiDe: false,
  danhMuc: false,
  xuatXu: false
})

// Search and display states
const brandSearch = ref('')
const priceFromDisplay = ref('')
const priceToDisplay = ref('')

// Price ranges for quick selection
const priceRanges = [
  { label: 'Dưới 500K', from: 0, to: 500000 },
  { label: '500K - 1M', from: 500000, to: 1000000 },
  { label: '1M - 2M', from: 1000000, to: 2000000 },
  { label: '2M - 5M', from: 2000000, to: 5000000 },
  { label: 'Trên 5M', from: 5000000, to: undefined }
]

// Computed properties
const isFiltersEmpty = computed(() => {
  return !filters.thuongHieu.length &&
    !filters.mauSac.length &&
    !filters.kichCo.length &&
    !filters.chatLieu.length &&
    !filters.loaiDe.length &&
    !filters.danhMuc.length &&
    !filters.xuatXu.length &&
    filters.giaTu === undefined &&
    filters.giaDen === undefined
})

const filteredBrands = computed(() => {
  if (!brandSearch.value) return thuongHieuOptions.value;
  return thuongHieuOptions.value.filter(brand => 
    brand.label.toLowerCase().includes(brandSearch.value.toLowerCase())
  );
})

// API fetch functions
const fetchColor = async () => {
  try {
    const { data } = await GetListColor()
    ColorOptions.value = data.map((c: any) => ({ label: c.ten, value: c.id, color: c.mau }))
  } catch (err) {
    console.error('Error fetching colors:', err)
  }
}

const fetchThuongHieu = async () => {
  try {
    const { data } = await GetListThuongHieu()
    thuongHieuOptions.value = data.map((item: any) => ({
      label: item.ten,
      value: item.id
    }))
  } catch (err) {
    console.error('Error fetching brands:', err)
  }
}

const fetchChatLieu = async () => {
  try {
    const { data } = await GetListChatLieu()
    chatLieuOptions.value = data.map((item: any) => ({
      label: item.ten,
      value: item.id
    }))
  } catch (err) {
    console.error('Error fetching materials:', err)
  }
}

const fetchDanhMuc = async () => {
  try {
    const { data } = await GetListDanhMuc()
    danhMucOptions.value = data.map((item: any) => ({
      label: item.ten,
      value: item.id
    }))
  } catch (err) {
    console.error('Error fetching categories:', err)
  }
}

const fetchLoaiDe = async () => {
  try {
    const { data } = await GetListLoaiDe()
    loaiDeOptions.value = data.map((item: any) => ({
      label: item.ten,
      value: item.id
    }))
  } catch (err) {
    console.error('Error fetching sole types:', err)
  }
}

const fetchSize = async () => {
  try {
    const { data } = await GetListSize()
    SizeOptions.value = data.map((s: any) => ({ label: s.ten, value: s.id }))
  } catch (err) {
    console.error('Error fetching sizes:', err)
  }
}

// Methods
const toggleSection = (key: string) => {
  sectionOpen[key] = !sectionOpen[key]
}

const toggleMauSac = (maMau: string) => {
  const index = filters.mauSac.indexOf(maMau)
  index >= 0 ? filters.mauSac.splice(index, 1) : filters.mauSac.push(maMau)
  applyFilters()
}

const toggleKichCo = (size: string) => {
  const index = filters.kichCo.indexOf(size)
  index >= 0 ? filters.kichCo.splice(index, 1) : filters.kichCo.push(size)
  applyFilters()
}

// Price handling with debounce
let priceDebounceTimer: number | null = null
const handlePriceInput = (field: 'giaTu' | 'giaDen', event: Event) => {
  const input = event.target as HTMLInputElement
  let value = input.value.replace(/[^0-9]/g, '')
  
  if (value === '') {
    filters[field] = undefined
  } else {
    filters[field] = parseInt(value, 10)
  }

  // Debounce the filter application
  if (priceDebounceTimer) clearTimeout(priceDebounceTimer)
  priceDebounceTimer = setTimeout(() => {
    applyFilters()
  }, 800)
}

const formatPriceDisplay = (type: 'from' | 'to') => {
  if (type === 'from' && filters.giaTu) {
    priceFromDisplay.value = filters.giaTu.toLocaleString('vi-VN')
  }
  if (type === 'to' && filters.giaDen) {
    priceToDisplay.value = filters.giaDen.toLocaleString('vi-VN')
  }
}

const selectPriceRange = (range: any) => {
  filters.giaTu = range.from
  filters.giaDen = range.to
  priceFromDisplay.value = range.from ? range.from.toLocaleString('vi-VN') : ''
  priceToDisplay.value = range.to ? range.to.toLocaleString('vi-VN') : ''
  applyFilters()
}

const isPriceRangeActive = (range: any) => {
  return filters.giaTu === range.from && filters.giaDen === range.to
}

const formatPriceRange = () => {
  const from = filters.giaTu ? `${filters.giaTu.toLocaleString('vi-VN')}₫` : ''
  const to = filters.giaDen ? `${filters.giaDen.toLocaleString('vi-VN')}₫` : ''
  
  if (from && to) return `${from} - ${to}`
  if (from) return `Từ ${from}`
  if (to) return `Đến ${to}`
  return ''
}

const applyFilters = () => {
  emit('filter', { ...filters })
}

const resetFilters = () => {
  Object.assign(filters, {
    thuongHieu: [],
    mauSac: [],
    kichCo: [],
    chatLieu: [],
    loaiDe: [],
    danhMuc: [],
    xuatXu: [],
    giaTu: undefined,
    giaDen: undefined
  })
  priceFromDisplay.value = ''
  priceToDisplay.value = ''
  brandSearch.value = ''
  applyFilters()
}

// Lifecycle
onMounted(async () => {
  isLoading.value = true
  try {
    await Promise.all([
      fetchColor(),
      fetchThuongHieu(),
      fetchChatLieu(),
      fetchDanhMuc(),
      fetchLoaiDe(),
      fetchSize()
    ])
  } catch (err) {
    error.value = 'Không thể tải dữ liệu bộ lọc'
  } finally {
    isLoading.value = false
  }
})
</script>

<style scoped>
.filter-container {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.filter-header {
  display: flex;
  justify-content: between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #FF0000 0%, #FF0000 100%);
  color: white;
}

.filter-title {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  flex: 1;
}

.btn-clear-all {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 0.85rem;
  transition: all 0.3s ease;
  cursor: pointer;
}

.btn-clear-all:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

.loading-state {
  text-align: center;
  padding: 40px 20px;
  color: #6c757d;
}

.filter-sections {
  padding: 0;
}

.filter-section {
  border-bottom: 1px solid #f1f3f5;
}

.filter-section:last-child {
  border-bottom: none;
}

.filter-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
  background: #fff;
}

.filter-section-header:hover {
  background: #f8f9fa;
}

.filter-section-title {
  font-weight: 600;
  color: #2d3436;
  display: flex;
  align-items: center;
  font-size: 0.95rem;
}

.filter-count {
  background: #FF0000;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  margin-left: 8px;
  font-weight: 500;
}

.toggle-icon {
  color: #FF0000;
  font-size: 0.9rem;
  transition: transform 0.3s ease;
}

.bi-chevron-up {
  transform: rotate(180deg);
}

.filter-section-content {
  padding: 0 20px 20px;
  background: #fafbfc;
}

/* Price Range Styles */
.price-range-container {
  background: white;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.price-input-group {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.price-input-wrapper {
  flex: 1;
}

.price-input {
  border: 2px solid #e9ecef;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.price-input:focus {
  border-color: #FF0000;
  box-shadow: 0 0 0 3px rgba(255, 0, 0, 0.1);
}

.price-separator {
  color: #FF0000;
  font-weight: 600;
  font-size: 1.1rem;
}

.price-quick-select {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.btn-price-range {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  color: #495057;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-price-range:hover {
  background: #e9ecef;
  transform: translateY(-1px);
}

.btn-price-range.active {
  background: #FF0000;
  color: white;
  border-color: #FF0000;
}

/* Search Box Styles */
.search-box {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #FF0000;
  z-index: 2;
}

.search-input {
  padding-left: 40px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.search-input:focus {
  border-color: #FF0000;
  box-shadow: 0 0 0 3px rgba(255, 0, 0, 0.1);
}

/* Checkbox List Styles */
.checkbox-list {
  max-height: 200px;
  overflow-y: auto;
  background: white;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.checkbox-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid #f8f9fa;
  transition: background 0.2s ease;
}

.checkbox-item:last-child {
  border-bottom: none;
}

.checkbox-item:hover {
  background: #f8f9fa;
}

.form-check-input {
  margin-right: 12px;
  cursor: pointer;
}

.form-check-label {
  cursor: pointer;
  margin: 0;
  font-size: 0.9rem;
  color: #495057;
}

/* Color Grid Styles */
.color-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 12px;
  background: white;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.color-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.color-item:hover {
  background: #f8f9fa;
  transform: translateY(-2px);
}

.color-item.selected {
  background: #ffebee;
  border: 2px solid #FF0000;
}

.color-preview {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid #dee2e6;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.color-name {
  font-size: 0.75rem;
  color: #495057;
  text-align: center;
  font-weight: 500;
}

/* Size Grid Styles */
.size-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(50px, 1fr));
  gap: 8px;
  background: white;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.size-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  border: 2px solid #dee2e6;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
  color: #495057;
}

.size-item:hover {
  border-color: #FF0000;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.size-item.selected {
  background: #FF0000;
  color: white;
  border-color: #FF0000;
}

/* Transitions */
.slide-fade-enter-active, .slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-fade-enter-from, .slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Scrollbar Styles */
.checkbox-list::-webkit-scrollbar {
  width: 6px;
}

.checkbox-list::-webkit-scrollbar-track {
  background: #f1f3f4;
  border-radius: 3px;
}

.checkbox-list::-webkit-scrollbar-thumb {
  background: #FF0000;
  border-radius: 3px;
}

.checkbox-list::-webkit-scrollbar-thumb:hover {
  background: #cc0000;
}

/* Responsive Design */
@media (max-width: 768px) {
  .filter-header {
    padding: 16px;
  }
  
  .filter-title {
    font-size: 1rem;
  }
  
  .filter-section-content {
    padding: 0 16px 16px;
  }
  
  .price-range-container {
    padding: 12px;
  }
  
  .price-input-group {
    flex-direction: column;
    gap: 8px;
  }
  
  .price-separator {
    align-self: center;
    transform: rotate(90deg);
  }
  
  .color-grid {
    grid-template-columns: repeat(auto-fill, minmax(70px, 1fr));
    gap: 8px;
    padding: 12px;
  }
  
  .size-grid {
    grid-template-columns: repeat(auto-fill, minmax(40px, 1fr));
    gap: 6px;
    padding: 12px;
  }
  
  .btn-price-range {
    font-size: 0.75rem;
    padding: 4px 8px;
  }
}

/* Animation for filter count badge */
@keyframes bounceIn {
  0% {
    opacity: 0;
    transform: scale(0.3);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.filter-count {
  animation: bounceIn 0.5s ease-out;
}

/* Enhanced hover effects */
.filter-section-header:hover .toggle-icon {
  transform: scale(1.1);
}

.filter-section-header:hover .filter-section-title i {
  color: #FF0000;
}

/* Loading spinner enhancement */
.spinner-border {
  width: 2rem;
  height: 2rem;
}

/* Custom checkbox styling */
.form-check-input:checked {
  background-color: #FF0000;
  border-color: #FF0000;
}

.form-check-input:focus {
  box-shadow: 0 0 0 3px rgba(255, 0, 0, 0.25);
}

/* Price input number styling */
.price-input::-webkit-outer-spin-button,
.price-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.price-input[type=number] {
  -moz-appearance: textfield;
}

/* Enhanced button styling */
.btn-clear-all:active {
  transform: translateY(0);
}

/* Smooth section toggle */
.toggle-icon {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Focus styles for accessibility */
.filter-section-header:focus-visible {
  outline: 2px solid #FF0000;
  outline-offset: -2px;
}

.color-item:focus-visible,
.size-item:focus-visible {
  outline: 2px solid #FF0000;
  outline-offset: 2px;
}
</style>