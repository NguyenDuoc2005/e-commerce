<template>
  <div class="add-discount-page p-6">
    <div class="breadcrumb-section">
      <BreadcrumbDefault :pageTitle="'Thêm đợt giảm giá'" :routes="[
        { path: '/admin/dot-giam-gia', name: 'Quản lý đợt giảm giá' },
        { path: '/admin/add-dot-giam-gia', name: 'Thêm đợt giảm giá' },
      ]" />
    </div>

    <div class="grid grid-cols-1 xl:grid-cols-4 gap-6">
      <div class="xl:col-span-1 bg-white rounded-lg shadow-sm border p-6">
        <h2 class="text-lg font-semibold mb-4">Thông tin đợt giảm giá</h2>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
            <span class="required">*</span>Tên khuyến mãi 
            </label>
            <a-input v-model:value="formData.tenKhuyenMai" placeholder="Tên khuyến mãi" size="large" />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
            <span class="required">*</span>Giá trị giảm
            </label>
            <a-input v-model:value="formData.giaTriGiam" placeholder="Giá trị giảm" size="large" suffix="%"
              class="hover-input" />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
             <span class="required">*</span>Ngày bắt đầu
            </label>
            <a-date-picker v-model:value="formData.ngayBatDau" placeholder="Ngày bắt đầu" size="large" class="w-full"
              format="DD/MM/YYYY HH:mm:ss" :show-time="true" />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
             <span class="required">*</span>Ngày kết thúc
            </label>
            <a-date-picker v-model:value="formData.ngayKetThuc" placeholder="Ngày kết thúc" size="large" class="w-full"
              format="DD/MM/YYYY HH:mm:ss" :show-time="true" />
          </div>

          <!-- <a-popconfirm
            title="Bạn có chắc chắn muốn lưu thay đổi?"
            @confirm="handleSubmit"
          >
            <a-button
              type="primary"
              size="large"
              ok-text="Đồng ý"
              cancel-text="Hủy"
              style="background-color: #54bddb; border-color: #54bddb"
            >
              Thêm mới đợt giảm giá
            </a-button>
          </a-popconfirm> -->
        </div>
      </div>

      <div class="xl:col-span-3 bg-white rounded-lg shadow-sm border p-6">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-semibold">Sản phẩm</h2>
        </div>

        <div class="mb-4">
          <a-input v-model:value="searchTerm" placeholder="Mã hoặc tên sản phẩm" size="large" class="w-full"
            @pressEnter="handleSearch">
            <template #prefix>
              <SearchOutlined />
            </template>
          </a-input>
        </div>

        <div class="mb-6">
          <div class="bg-gray-200 text-black px-4 py-2 rounded-t-lg">
            <div class="grid grid-cols-12 gap-2 text-sm font-medium">
              <div class="col-span-1 text-center">
                <a-checkbox :indeterminate="indeterminate" :checked="checkAll" @change="onCheckAllChange" />
              </div>
              <div class="col-span-1 text-center">STT</div>
              <div class="col-span-4 text-center">Tên sản phẩm</div>
              <div class="col-span-2 text-center">Thương hiệu</div>
              <div class="col-span-4 text-center">Trạng thái</div>
            </div>
          </div>

          <div class="border border-gray-200 rounded-b-lg max-h-64 overflow-y-auto">
            <div v-if="loading" class="p-8 text-center text-gray-500">
              <a-spin />
              <div class="mt-2">Đang tải dữ liệu...</div>
            </div>

            <div v-else-if="filteredProducts.length === 0" class="p-8 text-center text-gray-500">
              Không tìm thấy sản phẩm nào
            </div>

            <div v-else v-for="(product, index) in filteredProducts" :key="product.id"
              class="grid grid-cols-12 gap-2 p-3 border-b border-gray-100 hover:bg-gray-50 text-sm">
              <div class="col-span-1 text-center">
                <a-checkbox :checked="selectedProductIds.includes(product.id)" :loading="loadingDetails.has(product.id)"
                  @change="
                    (e) => handleProductSelect(product.id, e.target.checked)
                  " />
              </div>
              <div class="col-span-1 text-center">{{ index + 1 }}</div>
              <div class="col-span-4 text-center">{{ product.ten }}</div>
              <div class="col-span-2 text-center">
                {{ product.thuongHieu }}
              </div>
              <div class="col-span-4 text-center">
                <a-tag :color="product.status == 'ACTIVE' ? 'green' : 'red'">
                  {{
                    product.status == "ACTIVE"
                      ? "Đang kinh doanh"
                      : "Ngừng kinh doanh"
                  }}
                </a-tag>
              </div>
            </div>
          </div>
        </div>

        <div class="pagination-container">
          <a-pagination :current="currentPage" :total="totalProducts" :page-size="pageSize" @change="handlePageChange"
            size="small" show-size-changer :page-size-options="['10', '20', '30', '50']" @show-size-change="
              (current, size) => {
                pageSize = size;
                currentPage = 1;
                fetchProducts();
              }
            " />
        </div>

        <div class="mt-6">
          <h3 class="text-md font-semibold mb-4">
            Chi tiết sản phẩm
            <span class="text-blue-500 font-normal">({{ paginatedProductDetails.length }} /
              {{ filteredProductDetails.length }} sản phẩm)</span>
          </h3>
          <div class="row">
            <div class="col-md-3 mb-3">
              <label class="form-label hover-input d-flex align-items-center">
                <span class="ms-2 hover-input">Tìm sản phẩm</span>
              </label>
              <a-input v-model:value="productNameSearch" placeholder="Nhập tên sản phẩm..." size="large"
                @input="handleDetailSearch"
                class="w-full shadow-sm hover:shadow-md transition-shadow duration-200 hover-input" />
            </div>

            <div class="col-md-3 mb-3">
              <label class="form-label">Kích cỡ</label>
              <a-select v-model:value="selectedSize" placeholder="Chọn kích cỡ" size="large"
                class="w-full shadow-sm hover:shadow-md transition-shadow duration-200" @change="handleDetailSearch"
                :allowClear="true">
                <a-select-option v-for="size in sizes" :key="size.id" :value="size.id">
                  {{ size.ten }}
                </a-select-option>
              </a-select>
            </div>

            <div class="col-md-3 mb-3">
              <label class="form-label">Màu sắc</label>
              <a-select v-model:value="selectedColor" placeholder="Chọn màu sắc" size="large"
                class="w-full shadow-sm hover:shadow-md transition-shadow duration-200" @change="handleDetailSearch"
                :allowClear="true">
                <a-select-option v-for="color in colors" :key="color.id" :value="color.id">
                  {{ color.ten }}
                </a-select-option>
              </a-select>
            </div>

            <div class="col-md-2 mb-3" style="margin-top: 32px">
              <a-button @click="clearDetailFilters" style="background-color: gray;" size="large" type="default" class="mr-2 text-white">
                Đặt lại bộ lọc
              </a-button>
            </div>
          </div>

          <div class="bg-gray-200 text-black px-4 py-2 rounded-t-lg">
            <div class="grid grid-cols-12 gap-2 text-sm font-medium">
              <div class="col-span-1 text-center">
                <a-checkbox :indeterminate="detailsIndeterminate" :checked="detailsCheckAll"
                  @change="onCheckAllDetailsChange" />
              </div>
              <div class="col-span-1 text-center">STT</div>
              <div class="col-span-2 text-center">Ảnh sản phẩm</div>
              <div class="col-span-4 text-center">Tên sản phẩm</div>
              <div class="col-span-2 text-center">Số lượng</div>
              <div class="col-span-2 text-center">Đơn giá</div>
            </div>
          </div>

          <div class="border border-gray-200 rounded-b-lg min-h-32">
            <div v-if="loadingAnyDetails" class="p-8 text-center text-gray-500">
              <a-spin />
              <div class="mt-2">Đang tải chi tiết sản phẩm...</div>
            </div>

            <div v-else-if="paginatedProductDetails.length === 0" class="p-8 text-center text-gray-500">
              {{
                selectedProductDetails.length === 0
                  ? "Chưa có sản phẩm nào được chọn"
                  : "Không tìm thấy sản phẩm nào phù hợp với bộ lọc"
              }}
            </div>

            <div v-else v-for="(detail, index) in paginatedProductDetails" :key="detail.id"
              class="grid grid-cols-12 gap-2 p-3 border-b border-gray-100 text-sm hover:bg-gray-50">
              <div class="col-span-1 text-center">
                <a-checkbox :checked="detail.selected" @change="
                  (e) =>
                    handleProductDetailSelect(detail.id, e.target.checked)
                " />
              </div>
              <div class="col-span-1 text-center">
                {{ getDetailGlobalIndex(index) }}
              </div>
              <div class="col-span-2 text-center">
                <div class="w-10 h-10 bg-gray-200 rounded mx-auto flex items-center justify-center">
                  <img v-if="detail.anh || detail.hinhAnh" :src="detail.anh || detail.hinhAnh"
                    :alt="getProductName(detail)" class="w-full h-full object-cover rounded" @error="onImageError" />
                  <span v-else class="text-xs text-gray-400">N/A</span>
                </div>
              </div>
              <div class="col-span-4 text-center">
                {{ getProductName(detail) }} [{{ getColorCode(detail) }} -
                {{ getSizeName(detail) }}]
              </div>
              <div class="col-span-2 text-center">{{ detail.soLuong }}</div>
              <div class="col-span-2 text-center text-red-500">
                {{ formatCurrencyVND(detail.giaBan) }}
              </div>
            </div>
          </div>
          <div v-if="filteredProductDetails.length > 0" class="mt-4">
            <a-pagination v-model:current="currentDetailPageIndex" v-model:page-size="detailPageSize"
              :total="filteredProductDetails.length" show-size-changer :page-size-options="['5', '10', '20', '50']"
              @change="handleDetailPageChange" @showSizeChange="handleDetailShowSizeChange" />
          </div>
        </div>
        <a-popconfirm title="Bạn có chắc chắn muốn lưu thay đổi?" @confirm="handleSubmit">
          <a-button type="primary" size="large" ok-text="Đồng ý" cancel-text="Hủy"
            style="background-color: #54bddb; border-color: #54bddb;margin-top: 2%;">
            Thêm mới đợt giảm giá
          </a-button>
        </a-popconfirm>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { SearchOutlined } from "@ant-design/icons-vue";
import {
  GetSanPham,
  getSanPhamChiTiets,
  addDotGiamGia,
  getColorsFromAPI,
  getSizesFromAPI,
  type DotGiamGiaResponse,
  DotGiamGiaRequest,
  getSanPhamChiTietsByDot,
} from "@/services/api/admin/dotgiamgia.api";
import type { Dayjs } from "dayjs";
import BreadcrumbDefault from "@/components/ui/Breadcrumbs/BreadcrumbDefault.vue";
import { GetSanPhams } from "@/services/api/admin/sanpham.api";

const router = useRouter();

const formatCurrencyVND = (amount: number): string => {
  return amount.toLocaleString("vi-VN", { style: "currency", currency: "VND" });
};

// Form data
const formData = ref({
  tenKhuyenMai: "",
  giaTriGiam: "",
  ngayBatDau: null as Dayjs | null,
  ngayKetThuc: null as Dayjs | null,
});

// Product selection
const selectedProductIds = ref<string[]>([]);
const selectedProductDetails = ref<any[]>([]); // Store detailed product info
const loadingDetails = ref<Set<string>>(new Set()); // Track loading state for each product
const searchTerm = ref("");
const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(false);

// Detail search filters
const productNameSearch = ref("");
const selectedColor = ref(null);
const selectedSize = ref(null);
const sizes = ref<any[]>([]);
const colors = ref<any[]>([]);

// Pagination for product details
const currentDetailPageIndex = ref(1);
const detailPageSize = ref(5);

const validateForm = () => {
  const errors = [];
  if (!formData.value.tenKhuyenMai) {
    errors.push("Tên khuyến mãi không được để trống.");
  } else if (!/^[a-zA-Z0-9\s]*$/.test(formData.value.tenKhuyenMai)) {
    errors.push("Tên khuyến mãi không được chứa ký tự đặc biệt.");
  }
  if (!formData.value.giaTriGiam) {
    errors.push("Giá trị giảm không được để trống.");
  } else if (
    parseFloat(formData.value.giaTriGiam) <= 0 ||
    parseFloat(formData.value.giaTriGiam) > 80
  ) {
    errors.push("Giá trị giảm phải lớn hơn 0 và không được vượt quá 80%.");
  }
  if (!formData.value.ngayBatDau) {
    errors.push("Ngày bắt đầu không được để trống.");
  }
  if (!formData.value.ngayKetThuc) {
    errors.push("Ngày kết thúc không được để trống.");
  }
  if (formData.value.ngayBatDau && formData.value.ngayKetThuc) {
    if (formData.value.ngayBatDau.isAfter(formData.value.ngayKetThuc)) {
      errors.push("Ngày bắt đầu phải trước ngày kết thúc.");
    }
  }
  return errors;
};

const fetchColors = async () => {
  colors.value = await getColorsFromAPI();
};

const fetchSizes = async () => {
  const result = await getSizesFromAPI();
  sizes.value = result;
};

// Watch for changes in search term and refetch products
watch(searchTerm, (newValue) => {
  console.log("giá trị mới search :", newValue);
  fetchProducts();
});

// Watch for changes in product detail filters and reset detail page index
watch(
  [productNameSearch, selectedColor, selectedSize],
  () => {
    currentDetailPageIndex.value = 1; // Reset to first page when filters change
  },
  { immediate: true }
);

// API data
const availableProducts = ref<any[]>([]);
const totalProducts = ref(0);

// Computed properties
const filteredProducts = computed(() => {
  console.log("danh sách product: ", availableProducts.value);
  return availableProducts.value.filter(
    (product) =>
      product.ma.toLowerCase().includes(searchTerm.value.toLowerCase()) ||
      product.ten.toLowerCase().includes(searchTerm.value.toLowerCase())
  );
});

// Computed property for filtering product details
const filteredProductDetails = computed(() => {
  console.log("Filtering with:", {
    productNameSearch: productNameSearch.value,
    selectedColor: selectedColor.value,
    selectedSize: selectedSize.value,
    totalDetails: selectedProductDetails.value.length,
  });

  return selectedProductDetails.value.filter((detail) => {
    // Product name search
    const productName = getProductName(detail).toLowerCase();
    const searchMatch =
      !productNameSearch.value ||
      productName.includes(productNameSearch.value.toLowerCase());

    // Size filter - more explicit null checking
    let sizeMatch = true;
    if (selectedSize.value !== null && selectedSize.value !== undefined) {
      sizeMatch = detail.kichCo?.id === selectedSize.value;
    }

    // Color filter - more explicit null checking
    let colorMatch = true;
    if (selectedColor.value !== null && selectedColor.value !== undefined) {
      colorMatch = detail.mauSac?.id === selectedColor.value;
    }
    return searchMatch && sizeMatch && colorMatch;
  });
});

// Computed property for paginating filtered product details
const paginatedProductDetails = computed(() => {
  const startIndex = (currentDetailPageIndex.value - 1) * detailPageSize.value;
  const endIndex = startIndex + detailPageSize.value;
  return filteredProductDetails.value.slice(startIndex, endIndex);
});

const checkAll = computed(() => {
  return (
    filteredProducts.value.length > 0 &&
    filteredProducts.value.every((product) =>
      selectedProductIds.value.includes(product.id)
    )
  );
});

const indeterminate = computed(() => {
  const selectedCount = filteredProducts.value.filter((product) =>
    selectedProductIds.value.includes(product.id)
  ).length;
  return selectedCount > 0 && selectedCount < filteredProducts.value.length;
});

// Updated computed properties for product details check all (based on paginated results)
const detailsCheckAll = computed(() => {
  return (
    paginatedProductDetails.value.length > 0 &&
    paginatedProductDetails.value.every((detail) => detail.selected)
  );
});

const detailsIndeterminate = computed(() => {
  const selectedCount = paginatedProductDetails.value.filter(
    (detail) => detail.selected
  ).length;
  return (
    selectedCount > 0 && selectedCount < paginatedProductDetails.value.length
  );
});

const loadingAnyDetails = computed(() => {
  return loadingDetails.value.size > 0;
});

// Helper methods for displaying product details
const getProductName = (detail: any): string => {
  return detail.sanPham?.ten || detail.ten || "N/A";
};

const getBrandName = (detail: any): string => {
  return detail.sanPham?.thuongHieu?.ten || detail.thuongHieu?.ten || "N/A";
};

const getSizeName = (detail: any): string => {
  return detail.kichCo?.ten || detail.size || "N/A";
};

const getColorCode = (detail: any): string => {
  return detail.mauSac?.ten || detail.color || "#cccccc";
};

const getStatusText = (detail: any): string => {
  const status = detail.status || detail.trangThai;
  return status === "ACTIVE" || status === "Đang kinh doanh"
    ? "Đang kinh doanh"
    : "Ngừng kinh doanh";
};

const getStatusClass = (detail: any): string => {
  const status = detail.status || detail.trangThai;
  return status === "ACTIVE" || status === "Đang kinh doanh"
    ? "bg-green-100 text-green-800"
    : "bg-red-100 text-red-800";
};

const onImageError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  target.style.display = "none";
};

// New methods for detail search functionality
const handleDetailSearch = () => {
  currentDetailPageIndex.value = 1; // Reset to first page when filters change
};

const clearDetailFilters = () => {
  productNameSearch.value = "";
  selectedColor.value = null;
  selectedSize.value = null;
  currentDetailPageIndex.value = 1; // Reset to first page
  nextTick(() => {
    console.log(
      "Filters cleared, filtered results:",
      filteredProductDetails.value.length
    );
  });
};

const handleSubmit = async () => {
  const validationErrors = validateForm();
  if (validationErrors.length > 0) {
    validationErrors.forEach((error) => {
      message.error(error);
    });
    return;
  }
  const requestData: DotGiamGiaRequest = {
    name: formData.value.tenKhuyenMai,
    value: parseFloat(formData.value.giaTriGiam),
    startDate: formData.value.ngayBatDau?.valueOf() || null,
    endDate: formData.value.ngayKetThuc?.valueOf() || null,
    idProductDetails: selectedProductDetails.value
      .filter((detail) => detail.selected) // Only include selected details
      .map((detail) => ({ id: detail.id })), // Map to IdProductDetail structure
  };
  console.log("Request Data:", requestData);

  try {
    const response = await addDotGiamGia(requestData);
    console.log("Response from API:", response);
    message.success({
      content: "Thêm đợt giảm giá thành công!",
      duration: 1.5,
      style: {
        marginTop: "20vh",
        fontSize: "16px",
        fontWeight: "600",
      },
    });
    setTimeout(() => {
      router.push("/admin/dot-giam-gia");
    }, 1600);
  } catch (error: any) {
    if (error.response && error.response.data) {
      message.error(error.response.data.message);
    } else {
      message.error("Lỗi khi thêm đợt giảm giá. Vui lòng thử lại.");
    }
  }
};

// Methods
const fetchProductDetails = async (productId: string) => {
  try {
    loadingDetails.value.add(productId);
    console.log("Fetching details for product:", productId);

    const response = await getSanPhamChiTietsByDot(productId);
    console.log("Product details response:", response);

    if (response && Array.isArray(response) && response.length > 0) {
      const details = response.map((item) => ({
        id: item.id,
        status: item.status,
        ma: item.ma,
        giaBan: item.giaBan,
        anh: item.anh,
        soLuong: item.soLuong,
        sanPham: {
          id: item.sanPham.id,
          ma: item.sanPham.ma,
          ten: item.sanPham.ten,
          moTa: item.sanPham.moTa,
          thuongHieu: item.sanPham.thuongHieu,
          xuatSu: item.sanPham.xuatSu,
          danhMuc: item.sanPham.danhMuc,
          loaiDe: item.sanPham.loaiDe,
          chatLieu: item.sanPham.chatLieu,
        },
        kichCo: item.kichCo,
        mauSac: item.mauSac,
      }));

      details.forEach((detail) => {
        const existingIndex = selectedProductDetails.value.findIndex(
          (item) => item.id === detail.id
        );
        if (existingIndex === -1) {
          selectedProductDetails.value.push({
            ...detail,
            productId: productId, // Reference to parent product
            selected: false, // Individual selection state for details
          });
        }
      });
      currentDetailPageIndex.value = 1; // Reset to first page when new details are added
      console.log(
        "Updated selectedProductDetails:",
        selectedProductDetails.value
      );
    } else {
      console.warn("No valid data in response:", response);
    }
  } catch (error) {
    console.error("Error fetching product details:", error);
  } finally {
    loadingDetails.value.delete(productId);
  }
};

const fetchProducts = async () => {
  try {
    loading.value = true;
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      q: searchTerm.value,
    };

    console.log("Fetching products with params:", params);

    const response = await GetSanPhams(params);
    console.log("API Response:", response);

    if (response) {
      let products = [];
      products = response.data.data;
      availableProducts.value = products.map((product) => ({
        id: product.id,
        ma: product.ma,
        ten: product.ten,
        status: product.status,
        trangThai:
          product.status === "ACTIVE" ? "Đang kinh doanh" : "Ngừng kinh doanh",
        thuongHieu: product.tenThuongHieu,
        moTa: product.moTa,
      }));

      totalProducts.value =
        response.data.totalElements || availableProducts.value.length;

      console.log("Mapped products:", availableProducts.value);
    } else {
      console.warn("No response received");
      availableProducts.value = [];
      totalProducts.value = 0;
    }
  } catch (error) {
    console.error("Error fetching products:", error);
    availableProducts.value = [];
    totalProducts.value = 0;
  } finally {
    loading.value = false;
  }
};

const handleProductSelect = async (productId: string, checked: boolean) => {
  if (checked) {
    selectedProductIds.value.push(productId);
    await fetchProductDetails(productId);
  } else {
    const index = selectedProductIds.value.indexOf(productId);
    if (index > -1) {
      selectedProductIds.value.splice(index, 1);
    }

    // Remove product details when unselected
    selectedProductDetails.value = selectedProductDetails.value.filter(
      (detail) => detail.productId !== productId
    );
  }
  currentDetailPageIndex.value = 1; // Reset to first page
};

const onCheckAllChange = async (e: any) => {
  currentDetailPageIndex.value = 1; // Reset to first page when selecting/deselecting all products
  if (e.target.checked) {
    const promises = [];
    filteredProducts.value.forEach((product) => {
      if (!selectedProductIds.value.includes(product.id)) {
        selectedProductIds.value.push(product.id);
        promises.push(fetchProductDetails(product.id));
      }
    });
    await Promise.all(promises);
  } else {
    filteredProducts.value.forEach((product) => {
      const index = selectedProductIds.value.indexOf(product.id);
      if (index > -1) {
        selectedProductIds.value.splice(index, 1);
      }
      selectedProductDetails.value = selectedProductDetails.value.filter(
        (detail) => detail.productId !== product.id
      );
    });
  }
};

const handleProductDetailSelect = (detailId: string, checked: boolean) => {
  const detail = selectedProductDetails.value.find(
    (item) => item.id === detailId
  );
  if (detail) {
    detail.selected = checked;
  }
};

const onCheckAllDetailsChange = (e: any) => {
  const checked = e.target.checked;
  paginatedProductDetails.value.forEach((detail) => {
    detail.selected = checked;
  });
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchProducts();
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchProducts();
};

const handleDetailPageChange = (page: number, pageSize: number) => {
  currentDetailPageIndex.value = page;
  detailPageSize.value = pageSize;
};


const handleDetailShowSizeChange = (current: number, size: number) => {
  currentDetailPageIndex.value = 1;
  detailPageSize.value = size;
};

const getDetailGlobalIndex = (index: number) => {
  return (currentDetailPageIndex.value - 1) * detailPageSize.value + index + 1;
};

onMounted(() => {
  fetchProducts();
  fetchColors();
  fetchSizes();
});
</script>

<style scoped>
/* Add any custom styles here if needed */
.hover-input:hover {
  border-color: #54bddb;
  /* Example hover effect */
}
</style>

<style scoped lang="scss">
* {
  font-family: "Roboto", sans-serif;
}

.form-label {
  margin-bottom: 0.5rem;
  color: #333;
}

.row {
  margin-left: 0;
  margin-right: 0;
}

.col-md-4 {
  padding-left: 8px;
  padding-right: 8px;
}

.text-red-500 {
  color: red;
}

.add-discount-page {
  background-color: #f5f5f5;
  min-height: 100vh;
}

.ant-input-affix-wrapper .ant-input-prefix {
  color: #bfbfbf;
}

.grid {
  display: grid;
}

.grid-cols-1 {
  grid-template-columns: repeat(1, minmax(0, 1fr));
}

.grid-cols-12 {
  grid-template-columns: repeat(12, minmax(0, 1fr));
}

.grid-cols-4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

@media (min-width: 1280px) {
  .xl\:grid-cols-4 {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

.xl\:col-span-1 {
  grid-column: span 1 / span 1;
}

.xl\:col-span-3 {
  grid-column: span 3 / span 3;
}

@media (min-width: 1280px) {
  .xl\:col-span-1 {
    grid-column: span 1 / span 1;
  }

  .xl\:col-span-3 {
    grid-column: span 3 / span 3;
  }
}

.col-span-1 {
  grid-column: span 1 / span 1;
}

.col-span-2 {
  grid-column: span 2 / span 2;
}

.col-span-4 {
  grid-column: span 4 / span 4;
}

.gap-2 {
  gap: 0.5rem;
}

.gap-6 {
  gap: 1.5rem;
}

.p-3 {
  padding: 0.75rem;
}

.p-6 {
  padding: 1.5rem;
}

.p-8 {
  padding: 2rem;
}

.px-2 {
  padding-left: 0.5rem;
  padding-right: 0.5rem;
}

.px-4 {
  padding-left: 1rem;
  padding-right: 1rem;
}

.py-1 {
  padding-top: 0.25rem;
  padding-bottom: 0.25rem;
}

.py-2 {
  padding-top: 0.5rem;
  padding-bottom: 0.5rem;
}

.pt-4 {
  padding-top: 1rem;
}

.mb-2 {
  margin-bottom: 0.5rem;
}

.mb-4 {
  margin-bottom: 1rem;
}

.mb-6 {
  margin-bottom: 1.5rem;
}

.mt-2 {
  margin-top: 0.5rem;
}

.mt-6 {
  margin-top: 1.5rem;
}

.space-y-4>*+* {
  margin-top: 1rem;
}

.w-full {
  width: 100%;
}

.w-4 {
  width: 1rem;
}

.w-8 {
  width: 2rem;
}

.w-10 {
  width: 2.5rem;
}

.h-4 {
  height: 1rem;
}

.h-8 {
  height: 2rem;
}

.h-10 {
  height: 2.5rem;
}

.max-h-32 {
  max-height: 8rem;
}

.max-h-64 {
  max-height: 16rem;
}

.min-h-32 {
  min-height: 8rem;
}

.overflow-y-auto {
  overflow-y: auto;
}

.text-xs {
  font-size: 0.75rem;
  line-height: 1rem;
}

.text-sm {
  font-size: 15px;
  line-height: 1.25rem;
}

.text-md {
  font-size: 1rem;
  line-height: 1.5rem;
}

.text-lg {
  font-size: 1.125rem;
  line-height: 1.75rem;
}

.text-2xl {
  font-size: 1.5rem;
  line-height: 2rem;
}

.font-medium {
  font-weight: 500;
}

.font-semibold {
  font-weight: 600;
}

.font-bold {
  font-weight: 700;
}

.font-normal {
  font-weight: 400;
}

.text-center {
  text-align: center;
}

.text-white {
  color: rgb(255 255 255);
}

.text-gray-400 {
  color: rgb(156 163 175);
}

.text-gray-500 {
  color: rgb(107 114 128);
}

.text-blue-500 {
  color: #58bddb;
}

.text-white {
  color: white;
  background-color: #58bddb;
}

.text-gray-700 {
  color: rgb(55 65 81);
}

.text-gray-800 {
  color: rgb(31 41 55);
}

.text-green-800 {
  color: rgb(22 101 52);
}

.text-red-800 {
  color: rgb(153 27 27);
}

.bg-red-100 {
  background-color: rgb(254 226 226);
}

.bg-white {
  background-color: rgb(255 255 255);
}

.bg-gray-50 {
  background-color: rgb(249 250 251);
}

.bg-gray-200 {
  background-color: rgb(229 231 235);
}

.bg-blue-500 {
  background-color: rgb(59 130 246);
}

.bg-orange-500 {
  background-color: rgb(249 115 22);
}

.bg-green-100 {
  background-color: rgb(220 252 231);
}

.border {
  border-width: 1px;
}

.border-b {
  border-bottom-width: 1px;
}

.border-gray-100 {
  border-color: rgb(243 244 246);
}

.border-gray-200 {
  border-color: rgb(229 231 235);
}

.border-gray-300 {
  border-color: rgb(209 213 219);
}

.rounded {
  border-radius: 0.25rem;
}

.rounded-lg {
  border-radius: 0.5rem;
}

.rounded-t-lg {
  border-top-left-radius: 0.5rem;
  border-top-right-radius: 0.5rem;
}

.rounded-b-lg {
  border-bottom-left-radius: 0.5rem;
  border-bottom-right-radius: 0.5rem;
}

.rounded-full {
  border-radius: 9999px;
}

.shadow-sm {
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
}

.hover\:bg-gray-50:hover {
  background-color: rgb(249 250 251);
}

.flex {
  display: flex;
}

.inline-flex {
  display: inline-flex;
}

.items-center {
  align-items: center;
}

.justify-center {
  justify-content: center;
}

.justify-between {
  justify-content: space-between;
}

.block {
  display: block;
}

.mx-auto {
  margin-left: auto;
  margin-right: auto;
}

.object-cover {
  object-fit: cover;
}

.breadcrumb-section {
  margin-bottom: 25px;
  /* Space below the breadcrumb and above the first section */
  background-color: #fff;
  /* White background for the breadcrumb box */
  padding: 15px 20px;
  /* Padding inside the breadcrumb box */
  border-radius: 8px;
  /* Rounded corners */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  /* Subtle shadow */
}

.hover-input:hover {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.2) !important;
}


.required {
  color: red;
}

/* Hover effect cho Ant Design Input */
:deep(.ant-input:hover) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.2) !important;
}

/* Hover effect cho Ant Design Select */
:deep(.ant-select:hover .ant-select-selector) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.2) !important;
}

/* Hover effect cho Ant Design DatePicker */
:deep(.ant-picker:hover) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.2) !important;
}

/* Focus effect để đồng bộ với hover */
:deep(.ant-input:focus),
:deep(.ant-select-focused .ant-select-selector),
:deep(.ant-picker-focused) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.2) !important;
}

/* Existing styles remain unchanged */
.flex {
  display: flex;
}

.justify-end {
  justify-content: flex-end;
}

/* Ensure the pagination container takes full width and aligns content to the right */

.pagination-container {
  display: block;
  text-align: right;
  width: 100%;
  padding-right: 1rem; /* Adjust as needed */
  margin-top: 1rem; /* Add spacing above the pagination */
}

/* Optional: Style the pagination component itself */
:deep(.ant-pagination) {
  margin-top: 1rem;
  /* Add some spacing above the pagination */
}
</style>
