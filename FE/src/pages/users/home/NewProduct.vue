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
          @mouseenter="hoverProductId = item.id"
          @mouseleave="hoverProductId = null"
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
            <h6 class="card-title fw-semibold text-truncate mb-2" :title="item.tenSanPham">
              {{ item.tenSanPham }}
            </h6>
            <div class="mb-1">
              <span class="main-price">
                {{ (item.giaSauGiam ?? item.giaBan) }}₫
              </span>
              <span
                v-if="item.dotGiamGia"
                class="origin-price ms-2"
              >
                {{ item.dotGiamGia.giaTruoc }}₫
              </span>
              <span
                v-if="item.dotGiamGia"
                class="badge bg-danger ms-2"
                style="font-size:12px;"
              >
                -{{ item.dotGiamGia.phanTramGiam }}%
              </span>
            </div>
            <div class="brand-row mb-1 text-muted">
              <span class="brand-label">Thương hiệu:</span>
              <span class="fw-medium text-dark ms-1">{{ item.thuongHieu }}</span>
            </div>
            <div class="d-flex flex-wrap align-items-center small text-muted mb-1">
              <span class="me-1">Màu:</span>
              <span
                v-for="(color, i) in item.mauSac"
                :key="i"
                class="color-dot me-1"
                :style="{ backgroundColor: color.maMau }"
                :title="color.ten"
              ></span>
            </div>
            <div class="d-flex flex-wrap align-items-center small">
              <span class="me-1">Kích cỡ:</span>
              <span
                v-for="(size, i) in item.kichCo"
                :key="i"
                class="size-box me-1 mb-1"
              >{{ size.ten }}</span>
            </div>
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
        v-if="currentPage > 1"
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
import { GetSanPhamMoiTrangChu, type ParamsGetSanPhamMoi, type SanPhamMoiResponse } from '@/services/api/permitall/sanpham/pmsanpham.api'

const allProducts = ref<SanPhamMoiResponse[]>([])
const currentPage = ref(1)
const pageSize = 8
const isLoading = ref(false)
const hasMore = ref(true)
const totalElements = ref(0)
const hoverProductId = ref<string | null>(null)

const getShowImage = (item: SanPhamMoiResponse) => {
  if (
    hoverProductId.value === item.id &&
    item.dsAnh &&
    item.dsAnh.length > 1 &&
    item.dsAnh[1]
  ) {
    return item.dsAnh[1]
  }
  return (item.dsAnh && item.dsAnh.length > 0)
    ? item.dsAnh[0]
    : item.hinhAnhDaiDien
}

const fetchProducts = async (append = false) => {
  isLoading.value = true
  try {
    const params: ParamsGetSanPhamMoi = {
      page: currentPage.value,
      size: pageSize
    }
    const res = await GetSanPhamMoiTrangChu(params)
    const fetchedCount = res.data.data.length

    if (append) {
      allProducts.value = [...allProducts.value, ...res.data.data]
    } else {
      allProducts.value = res.data.data
    }
    totalElements.value = res.data.totalElements
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
  if (currentPage.value === 1) return
  currentPage.value = 1
  await fetchProducts(false)
}

const router = useRouter()
const handleClick = (product: SanPhamMoiResponse) => {
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
