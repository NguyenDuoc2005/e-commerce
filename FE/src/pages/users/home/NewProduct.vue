<template>
  <div class="container py-5 rounded">
    <!-- Tiêu đề -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap">
      <div class="d-flex align-items-center">
        <GiftFilled class="me-2 text-primary gift-big" />
        <h4 class="fw-bold mb-0">Khám Phá Sản Phẩm Mới</h4>
      </div>
    </div>

    <!-- Danh sách sản phẩm -->
    <div class="row g-3 g-md-4">
      <div
        v-for="item in allProducts"
        :key="item.id"
        class="col-12 col-sm-6 col-md-4 col-lg-3"
      >
        <div
          class="card h-100 shadow-sm border-0 product-card"
          @click="handleClick(item)"
        >
          <div class="product-img-wrapper position-relative">
            <img
              :src="getShowImage(item)"
              class="card-img-top product-img-main"
              alt="Ảnh sản phẩm"
              draggable="false"
            />
          </div>
          <div class="card-body py-2">
            <h6 class="card-title fw-semibold text-truncate mb-2" :title="item.name">
              {{ item.name }}
            </h6>
            <div class="mb-1">
              <span class="main-price">{{ priceLabel(item) }}</span>
            </div>
            <div class="brand-row mb-1 text-muted">
              <span class="brand-label">Danh mục:</span>
              <span class="fw-medium text-dark ms-1">{{ item.category?.name || 'Marketplace' }}</span>
            </div>
            <div class="small text-muted">{{ item.activeVariantCount }} phân loại · Còn {{ item.totalQuantity }} sản phẩm</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Nút điều hướng & loading -->
    <div class="d-flex justify-content-center gap-3 mt-4">
      <button
        class="btn rounded-pill px-3 py-1 fw-medium text-secondary border"
        style="background-color: #f8f9fa; border-color: #ced4da; font-size: 14px;"
        @click="showLess"
      v-if="currentPage > 0"
      >
        Xem ít hơn
      </button>
      <button
        class="btn rounded-pill px-3 py-1 fw-medium text-white"
        style="background-color: #5c6bc0; font-size: 14px;"
        @click="showMore"
        v-if="hasMore && !isLoading"
      >
        <PlusOutlined class="me-1" /> Xem thêm
      </button>
    </div>
    <div v-if="isLoading" class="text-center mt-4">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { PlusOutlined, GiftFilled } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { getCatalogProducts, type CatalogSummary } from '@/services/api/catalog/catalog.api'

const allProducts = ref<CatalogSummary[]>([])
const currentPage = ref(0)
const pageSize = 8
const isLoading = ref(false)
const hasMore = ref(true)
const totalElements = ref(0)

const placeholder = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="300"%3E%3Crect width="100%25" height="100%25" fill="%23f1f5f9"/%3E%3C/svg%3E'
const getShowImage = (item: CatalogSummary) => item.thumbnailUrl || placeholder
const money = (value?: number) => `${Number(value || 0).toLocaleString('vi-VN')}₫`
const priceLabel = (item: CatalogSummary) => item.maxPrice && item.minPrice !== item.maxPrice
  ? `${money(item.minPrice)} - ${money(item.maxPrice)}`
  : money(item.minPrice)

const fetchProducts = async (append = false) => {
  isLoading.value = true
  try {
    const response = await getCatalogProducts({ page: currentPage.value, size: pageSize })
    const rows = response.content || []
    const fetchedCount = rows.length

    if (append) {
      allProducts.value = [...allProducts.value, ...rows]
    } else {
      allProducts.value = rows
    }
    totalElements.value = response.totalElements || 0
    hasMore.value = allProducts.value.length < totalElements.value && fetchedCount === pageSize
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchProducts()
})

const showMore = async () => {
  if (!hasMore.value || isLoading.value) return
  currentPage.value++
  await fetchProducts(true)
}

const showLess = async () => {
  if (currentPage.value === 0) return
  currentPage.value = 0
  await fetchProducts(false)
}

const router = useRouter()
const handleClick = (product: CatalogSummary) => {
  router.push({
    name: 'san-pham-chi-tiet',
    params: { idsp: product.id }
  })
}
</script>

<style scoped>
.product-img-wrapper {
  width: 100%;
  aspect-ratio: 1/1;
  overflow: hidden;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.product-img-main {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transition: opacity 0.34s cubic-bezier(.4,0,.2,1), transform 0.35s cubic-bezier(.4,0,.2,1);
}
.product-card {
  transition: transform 0.18s, box-shadow 0.18s;
  cursor: pointer;
  background: #fff;
  border-radius: 15px;
}
.product-card:hover {
  transform: translateY(-4px) scale(1.035);
  box-shadow: 0 4px 18px rgba(44,124,255,0.12);
}
.main-price {
  font-size: 1.14rem;
  font-weight: 700;
  color: #174b9c;
  letter-spacing: 0.4px;
}
.origin-price {
  font-size: 0.93rem;
  color: #b4b4b4;
  text-decoration: line-through;
  margin-left: 5px;
  vertical-align: middle;
}
.badge.bg-danger {
  vertical-align: middle;
  background: linear-gradient(90deg, #ff4d4f 80%, #ffb14c 100%);
  font-weight: 600;
  letter-spacing: 0.2px;
  border-radius: 8px 8px 8px 8px;
}
.brand-row {
  font-size: 14px;
}
.brand-label {
  color: #a2a2a2;
}
.color-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1px solid #ccc;
  display: inline-block;
}
.size-box {
  display: inline-block;
  padding: 2px 7px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 12px;
  background-color: #f9f9f9;
  margin-right: 3px;
}
.gift-big {
  font-size: 1.7rem !important;
}

@media (max-width: 575px) {
  .container {
    padding-left: 2.5px !important;
    padding-right: 2.5px !important;
  }
  .product-img-wrapper {
    aspect-ratio: 1/1.02;
  }
  .main-price {
    font-size: 1.01rem;
  }
}
</style>
