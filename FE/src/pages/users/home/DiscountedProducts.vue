<template>
  <div class="container py-5 rounded">
    <!-- Tiêu đề -->
    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap">
      <div class="d-flex align-items-center">
        <FireFilled class="me-2 text-warning fire-big" />
        <h4 class="fw-bold mb-0 text-danger">Khuyến Mãi Sập Sàn – Đừng Bỏ Lỡ!</h4>
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
            <!-- Ribbon giảm giá -->
            <template v-if="item.dotGiamGia">
              <div class="sale-ribbon">
                <span>🔥 SALE</span>
                <b>{{ item.dotGiamGia.phanTramGiam }}%</b>
              </div>
              <div class="dot-name" v-if="item.dotGiamGia.tenDotGiamGia">
                {{ item.dotGiamGia.tenDotGiamGia }}
              </div>
            </template>
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
            <!-- Giá giảm nổi bật -->
            <div class="price-block">
              <span class="main-sale-price">
                {{ getPriceAfterSale(item).toLocaleString() }}₫
              </span>
              <span v-if="item.dotGiamGia" class="origin-price">
                {{ item.dotGiamGia.giaTruoc.toLocaleString() }}₫
              </span>
            </div>
            <div v-if="item.dotGiamGia" class="save-amount mb-2">
              <span class="text-save">💰 Tiết kiệm</span>
              <span class="fw-bold text-success ms-1">
                {{ getSaveAmount(item).toLocaleString() }}₫
              </span>
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
        style="background-color: #ff4400; font-size: 14px;"
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
import { FireFilled, PlusOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { GetSanPhamGiamGiaTrangChu, type ParamsGetSanPhamMoi, type SanPhamMoiResponse } from '@/services/api/permitall/sanpham/pmsanpham.api'

const allProducts = ref<SanPhamMoiResponse[]>([])
const currentPage = ref(1)
const pageSize = 4
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

// Tính giá sau giảm dựa vào phần trăm giảm giá
const getPriceAfterSale = (item: SanPhamMoiResponse) => {
  if (item.dotGiamGia && typeof item.dotGiamGia.phanTramGiam === 'number') {
    return Math.round(item.dotGiamGia.giaTruoc * (1 - item.dotGiamGia.phanTramGiam / 100))
  }
  return item.giaBan
}

// Tính số tiền tiết kiệm
const getSaveAmount = (item: SanPhamMoiResponse) => {
  if (item.dotGiamGia && typeof item.dotGiamGia.phanTramGiam === 'number') {
    return Math.round(item.dotGiamGia.giaTruoc * (item.dotGiamGia.phanTramGiam / 100))
  }
  return 0
}

const fetchProducts = async (append = false) => {
  isLoading.value = true
  try {
    const params: ParamsGetSanPhamMoi = {
      page: currentPage.value,
      size: pageSize
    }
    const res = await GetSanPhamGiamGiaTrangChu(params)
    console.log('Fetched products:', res.data.data)
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
  transition: opacity 0.35s cubic-bezier(.4,0,.2,1), transform 0.36s cubic-bezier(.4,0,.2,1);
}
.sale-ribbon {
  position: absolute;
  left: 0;
  top: 8px;
  background: linear-gradient(90deg, #ff6600 70%, #ffc371 100%);
  color: #fff;
  font-weight: bold;
  font-size: .97rem;
  padding: 3px 13px 3px 9px;
  border-radius: 0 14px 14px 0;
  z-index: 5;
  box-shadow: 0 2px 8px rgba(255,102,0,0.08);
  letter-spacing: 1px;
  min-width: 60px;
  text-shadow: 0 1px 3px rgba(0,0,0,0.12);
  display: flex;
  align-items: center;
  gap: 5px;
}
.dot-name {
  position: absolute;
  left: 0;
  bottom: 6px;
  background: rgba(255, 255, 255, 0.93);
  color: #ff2600;
  font-weight: 500;
  font-size: 0.89rem;
  padding: 2px 10px 2px 10px;
  border-radius: 0 7px 7px 0;
  max-width: 80%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  z-index: 4;
  box-shadow: 0 2px 8px rgba(255,102,0,0.04);
}

.price-block {
  display: flex;
  align-items: baseline;
  gap: 9px;
  margin-bottom: 1px;
}
.main-sale-price {
  font-size: 1.18rem;
  font-weight: 700;
  color: #ff2600;
  letter-spacing: 0.6px;
}
.origin-price {
  font-size: 0.97rem;
  color: #b4b4b4;
  text-decoration: line-through;
  margin-left: 4px;
  vertical-align: middle;
}
.save-amount {
  font-size: 0.97rem;
  color: #16b65a;
  font-weight: 600;
  margin-bottom: 0px;
  margin-top: 1px;
  letter-spacing: 0.2px;
}
.text-save {
  color: #13a813;
  font-weight: 500;
}
.product-card {
  transition: transform 0.18s, box-shadow 0.18s;
  cursor: pointer;
  background: #fff;
  border-radius: 16px;
}
.product-card:hover {
  transform: translateY(-4px) scale(1.035);
  box-shadow: 0 4px 18px rgba(255,102,0,0.13);
}
.fire-big {
  font-size: 1.7rem !important;
}

/* Responsive: giữ ribbon & text đẹp trên mobile */
@media (max-width: 575px) {
  .container {
    padding-left: 2.5px !important;
    padding-right: 2.5px !important;
  }
  .product-img-wrapper {
    aspect-ratio: 1/1.02;
  }
  .sale-ribbon {
    font-size: 0.85rem;
    padding: 2.5px 8px 2.5px 8px;
    min-width: 45px;
    border-radius: 0 12px 12px 0;
  }
  .main-sale-price {
    font-size: 1rem;
  }
}
</style>
