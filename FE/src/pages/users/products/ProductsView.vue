<template>
  <div class="container-fluid d-flex flex-column align-items-center">
    <div class="container py-3">
      <div class="row align-items-center">
        <BreadCrumbUser :routes="breadcrumbRoutes" title="Danh sách sản phẩm" />
      </div>
    </div>

    <div class="container d-flex flex-column flex-lg-row gap-4">
      <div class="col-12 col-lg-3">
        <FilterBox @filter="handleFilter" />
      </div>
      <div class="col flex-fill">
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-center mb-3">
          <div class="d-flex align-items-center gap-2">
            <input
              v-model="keyword"
              class="form-control form-control-sm border-red"
              placeholder="Tìm kiếm sản phẩm..."
              @input="handleSearchInput"
            />
            <p class="fw-bold mb-0 text-red">
              <span v-if="filtersApplied" class="text-muted small">({{ filtersApplied }})</span>
            </p>
          </div>
          <div class="d-flex gap-3">
            <select class="form-select form-select-sm text-red border-red" v-model="sortBy">
              <option value="createdAt_desc">Hàng mới nhất</option>
              <option value="createdAt_asc">Hàng cũ nhất</option>
              <option value="giaBan_asc">Giá tăng dần</option>
              <option value="giaBan_desc">Giá giảm dần</option>
              <option value="ten_asc">Tên A-Z</option>
              <option value="ten_desc">Tên Z-A</option>
            </select>
          </div>
        </div>

        <div v-if="isLoading && allProducts.length === 0" class="text-center py-5">
          <div class="spinner-border text-red" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
          <p class="mt-2 text-red">Đang tải sản phẩm...</p>
        </div>
        <div v-if="error" class="alert alert-danger mt-3">
          {{ error }}
        </div>
        <div v-else-if="allProducts.length === 0 && !isLoading" class="text-center py-5">
          <p class="text-muted">Không tìm thấy sản phẩm nào.</p>
        </div>

        <div v-else class="row g-3 g-md-4">
          <div v-for="item in allProducts" :key="item.id" class="col-12 col-sm-6 col-md-4 col-lg-3">
            <div class="card h-100 shadow-sm border-0 product-card" @click="handleClick(item)"
                 @mouseenter="hoverProductId = item.id" @mouseleave="hoverProductId = null">
              <div class="product-img-wrapper position-relative">
                <img :src="getShowImage(item)" class="card-img-top product-img-main" alt="Ảnh sản phẩm"
                     draggable="false" />
              </div>
              <div class="card-body py-2">
                <h6 class="card-title fw-semibold text-black text-truncate mb-2" :title="item.tenSanPham">
                  {{ item.tenSanPham }}
                </h6>
                <div class="mb-1">
                  <span class="main-price">
                    {{ formatVND(item.giaSauGiam ?? item.giaBan) }}
                  </span>
                  <span v-if="item.dotGiamGia" class="origin-price ms-2">
                    {{ formatVND(item.giaBan) }}
                  </span>
                  <span v-if="item.dotGiamGia" class="badge bg-red ms-2" style="font-size:12px;">
                    -{{ item.dotGiamGia.phanTramGiam }}%
                  </span>
                </div>
                <div class="brand-row mb-1 text-muted">
                  <span class="brand-label">Thương hiệu:</span>
                  <span class="fw-medium text-dark ms-1">{{ item.thuongHieu }}</span>
                </div>
                <div class="d-flex flex-wrap align-items-center small text-muted mb-1">
                  <span class="me-1">Màu:</span>
                  <span v-for="(color, i) in item.mauSac" :key="i" class="color-dot me-1"
                        :style="{ backgroundColor: color.maMau }" :title="color.ten"></span>
                </div>
                <div class="d-flex flex-wrap align-items-center small">
                  <span class="me-1">Kích cỡ:</span>
                  <span v-for="(size, i) in item.kichCo" :key="i" class="size-box me-1 mb-1">{{ size.ten }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="totalElements > 0" class="d-flex justify-content-center mt-4 mb-5">
          <nav aria-label="Product page navigation">
            <ul class="pagination">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link text-red" href="#" @click.prevent="prevPage">Trước</a>
              </li>
              <li class="page-item" v-for="page in totalPages" :key="page"
                  :class="{ active: page === currentPage }">
                <a class="page-link text-black" href="#" @click.prevent="goToPage(page)">{{ page }}</a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <a class="page-link text-red" href="#" @click.prevent="nextPage">Sau</a>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref, computed, watch } from 'vue';
import FilterBox from './FilterBox.vue';
import BreadCrumbUser from '@/components/ui/Breadcrumbs/BreadCrumbUser.vue';
import { useRouter } from 'vue-router';
import { GetDanhSachSanPhamTrangSanPham, type ParamsGetSanPhamMoi, type SanPhamMoiResponse } from '@/services/api/permitall/sanpham/pmsanpham.api';

// Format price to VND
const formatVND = (price: number | undefined): string => {
  if (price === undefined || price === null) return '0 ₫';
  return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.') + ' ₫';
};

const keyword = ref<string>('');
const breadcrumbRoutes = [
  { name: 'Trang chủ', path: '/' },
  { name: 'Sản phẩm', path: '/san-pham' }
];

const allProducts = ref<SanPhamMoiResponse[]>([]);
const currentPage = ref(1);
const pageSize = 12;
const isLoading = ref(false);
const totalElements = ref(0);
const hoverProductId = ref<string | null>(null);
const sortBy = ref<string>('createdAt_desc');
const error = ref<string | null>(null);

const filters = ref({
  thuongHieu: [] as string[],
  mauSac: [] as string[],
  kichCo: [] as string[],
  chatLieu: [] as string[],
  loaiDe: [] as string[],
  danhMuc: [] as string[],
  giaTu: undefined,
  giaDen: undefined
});

const filtersApplied = computed(() => {
  const applied: string[] = [];
  if (filters.value.thuongHieu.length) applied.push(`Thương hiệu: ${filters.value.thuongHieu.length}`);
  if (filters.value.chatLieu.length) applied.push(`Chất liệu: ${filters.value.chatLieu.length}`);
  if (filters.value.loaiDe.length) applied.push(`Loại đế: ${filters.value.loaiDe.length}`);
  if (filters.value.danhMuc.length) applied.push(`Danh mục: ${filters.value.danhMuc.length}`);
  return applied.join(', ');
});

const totalPages = computed(() => {
  return Math.ceil(totalElements.value / pageSize);
});

const getShowImage = (item: SanPhamMoiResponse) => {
  if (
    hoverProductId.value === item.id &&
    item.dsAnh &&
    item.dsAnh.length > 1 &&
    item.dsAnh[1]
  ) {
    return item.dsAnh[1];
  }
  return (item.dsAnh && item.dsAnh.length > 0)
    ? item.dsAnh[0]
    : item.hinhAnhDaiDien;
};

const handleFilter = (newFilters: any) => {
  filters.value = { ...newFilters };
  currentPage.value = 1; // Reset về trang 1 khi áp dụng bộ lọc
  fetchProducts();
};

const handleSearchInput = () => {
  currentPage.value = 1; // Reset về trang 1 khi nhập tìm kiếm
  fetchProducts(); // Gọi fetchProducts ngay khi có input
};

const fetchProducts = async () => {
  isLoading.value = true;
  allProducts.value = [];
  error.value = null;

  try {
    const params: ParamsGetSanPhamMoi = {
      page: currentPage.value,
      size: pageSize,
      sortBy: sortBy.value,
      q: keyword.value !== 'Tất cả sản phẩm' ? keyword.value.trim() : undefined,
      thuongHieuIds: filters.value.thuongHieu.length > 0 ? filters.value.thuongHieu.join(',') : undefined,
      chatLieuIds: filters.value.chatLieu.length > 0 ? filters.value.chatLieu.join(',') : undefined,
      loaiDeIds: filters.value.loaiDe.length > 0 ? filters.value.loaiDe.join(',') : undefined,
      danhMucIds: filters.value.danhMuc.length > 0 ? filters.value.danhMuc.join(',') : undefined,
      giaMin: filters.value.giaTu || undefined,
      giaMax: filters.value.giaDen || undefined
    };

    console.log("Fetching products with params:", JSON.stringify(params, null, 2));
    const res = await GetDanhSachSanPhamTrangSanPham(params);
    console.log("API response:", res);

    if (res.data && res.data.data) {
      allProducts.value = res.data.data;
      totalElements.value = res.data.totalElements || 0;
    } else {
      throw new Error('Dữ liệu trả về từ API không hợp lệ');
    }

    const calculatedTotalPages = Math.ceil(totalElements.value / pageSize);
    if (!totalPages.value && calculatedTotalPages > 0) {
      totalPages.value = calculatedTotalPages;
    }

    if (currentPage.value > totalPages.value && totalPages.value > 0 && currentPage.value !== totalPages.value) {
      currentPage.value = totalPages.value;
      await fetchProducts();
    }
  } catch (err) {
    console.error("Lỗi khi tải sản phẩm:", err);
    error.value = err.message || 'Không thể tải danh sách sản phẩm. Vui lòng thử lại.';
  } finally {
    isLoading.value = false;
  }
};

// Theo dõi thay đổi của keyword, currentPage, và sortBy
watch([keyword, currentPage, sortBy], () => {
  fetchProducts();
});

onMounted(() => {
  fetchProducts();
});

const router = useRouter();

const handleClick = (product: SanPhamMoiResponse) => {
  router.push({
    name: 'san-pham-chi-tiet',
    params: { idsp: product.id }
  });
};

const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value && page !== currentPage.value) {
    currentPage.value = page;
  }
};

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
};
</script>

<style scoped>
.form-control-sm {
  max-width: 200px;
}

.border-red {
  border-color: #FF0000 !important;
}

.text-red {
  color: #FF0000 !important;
}

.text-black {
  color: #000000 !important;
}


.img-wrapper {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background-color: #f8f8f8;
  position: relative;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
}

.product-card {
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(255, 0, 0, 0.2);
}

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
  transition: opacity 0.34s cubic-bezier(.4, 0, .2, 1), transform 0.35s cubic-bezier(.4, 0, .2, 1);
}

.hover-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  padding: 0.75rem;
  font-size: 14px;
  transform: translateY(100%);
  transition: transform 0.3s ease-in-out;
  color: #333;
}

.product-img-wrapper:hover .hover-overlay {
  transform: translateY(0%);
}

.color-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 1px solid #ccc;
  display: inline-block;
}

.size-box {
  padding: 2px 6px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 12px;
  background-color: #f9f9f9;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
}

.pagination {
  padding: 0;
  margin: 0;
}

.page-item {
  margin: 0 5px;
}

.page-link {
  color: #FF0000;
  background-color: #fff;
  border: 1px solid #FF0000;
  border-radius: 4px;
  padding: 8px 12px;
  transition: all 0.3s ease;
}

.page-link:hover {
  background-color: #ffebee;
  color: #FF0000;
}

.page-item.active .page-link {
  background-color: #FF0000;
  color: #fff;
  border-color: #FF0000;
}

.page-item.disabled .page-link {
  color: #6c757d;
  background-color: #fff;
  border-color: #dee2e6;
  pointer-events: none;
}

.main-price {
  font-size: 1.14rem;
  font-weight: 700;
  color: #FF0000;
  letter-spacing: 0.4px;
}

.origin-price {
  font-size: 0.93rem;
  color: #b4b4b4;
  text-decoration: line-through;
  margin-left: 5px;
  vertical-align: middle;
}

.badge.bg-red {
  vertical-align: middle;
  background: linear-gradient(90deg, #FF0000 80%, #FF3333 100%);
  font-weight: 600;
  letter-spacing: 0.2px;
  border-radius: 8px;
}

.brand-row {
  font-size: 14px;
}

.brand-label {
  color: #a2a2a2;
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