<template>
  <div class="update-discount-page p-6">
    <div class="breadcrumb-section">
      <BreadcrumbDefault
        :pageTitle="'Sửa đợt giảm giá'"
        :routes="[
          { path: '/admin/dot-giam-gia', name: 'Quản lý đợt giảm giá' },
          { path: '/admin/update-dot-giam-gia', name: 'Sửa đợt giảm giá' },
        ]"
      />
    </div>
    <div class="grid grid-cols-1 xl:grid-cols-4 gap-6">
      <div class="xl:col-span-1 bg-white rounded-lg shadow-sm border p-6">
        <h2 class="text-lg font-semibold mb-4">Thông tin đợt giảm giá</h2>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2"> Mã đợt giảm giá </label>
            <a-input
              v-model:value="formData.code"
              placeholder="Mã đợt giảm giá"
              size="large"
              disabled
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2"> Tên khuyến mãi </label>
            <a-input
              v-model:value="formData.tenKhuyenMai"
              placeholder="Tên khuyến mãi"
              size="large"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2"> Giá trị giảm </label>
            <a-input
              v-model:value="formData.giaTriGiam"
              placeholder="Giá trị giảm"
              size="large"
              suffix="%"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2"> Ngày bắt đầu </label>
            <a-date-picker
              v-model:value="formData.ngayBatDau"
              placeholder="Ngày bắt đầu"
              size="large"
              class="w-full"
              format="DD/MM/YYYY HH:mm:ss"
              :show-time="true"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2"> Ngày kết thúc </label>
            <a-date-picker
              v-model:value="formData.ngayKetThuc"
              placeholder="Ngày kết thúc"
              size="large"
              class="w-full"
              format="DD/MM/YYYY HH:mm:ss"
              :show-time="true"
            />
          </div>

          <div class="pt-4 flex gap-3">
            <a-button size="large" style="background-color: #54bddb; border-color: #54bddb; color: white;" @click="handleCancel" class="flex-1"> Hủy </a-button>
          </div>
        </div>
      </div>

      <div class="xl:col-span-3 bg-white rounded-lg shadow-sm border p-6">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-lg font-semibold">Sản phẩm</h2>
        </div>

        <div class="mb-4">
          <a-input
            v-model:value="searchTerm"
            placeholder="Mã hoặc tên sản phẩm"
            size="large"
            class="w-full"
            @pressEnter="handleSearch"
          >
            <template #prefix>
              <SearchOutlined />
            </template>
          </a-input>
        </div>

        <div class="mb-6">
          <div class="bg-gray-200 text-black px-4 py-2 rounded-t-lg">
            <div class="grid grid-cols-12 gap-2 text-sm font-medium">
              <div class="col-span-1 text-center">
                <a-checkbox
                  :indeterminate="indeterminate"
                  :checked="checkAll"
                  @change="onCheckAllChange"
                />
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

            <div
              v-else
              v-for="(product, index) in filteredProducts"
              :key="product.id"
              class="grid grid-cols-12 gap-2 p-3 border-b border-gray-100 hover:bg-gray-50 text-sm"
            >
              <div class="col-span-1 text-center">
                <a-checkbox
                  :checked="selectedProductIds.includes(product.id)"
                  :loading="loadingDetails.has(product.id)"
                  @change="(e) => handleProductSelect(product.id, e.target.checked)"
                />
              </div>
              <div class="col-span-1 text-center">{{ index + 1 }}</div>
              <div class="col-span-4 text-center">{{ product.ten }}</div>
              <div class="col-span-2 text-center">
                {{ product.thuongHieu.ten }}
              </div>
              <div
                style="color: green"
                class="col-span-4 text-center"
                :class="
                  product.status === 'ACTIVE'
                    ? 'border-green-500 text-green-600'
                    : 'border-red-500 text-red-600'
                "
              >
                {{ product.status === "ACTIVE" ? "Đang kinh doanh" : "Ngừng kinh doanh" }}
              </div>
            </div>
          </div>
        </div>

        <div class="">
          <a-pagination
            :current="currentPage"
            :total="totalProducts"
            :page-size="pageSize"
            @change="handlePageChange"
            size="small"
            show-size-changer
            :page-size-options="['10', '20', '30', '50']"
            @show-size-change="
              (current, size) => {
                pageSize = size;
                currentPage = 1;
                fetchProducts();
              }
            "
          />
        </div>

        <div class="mt-6">
          <h3 class="text-md font-semibold mb-4">
            Chi tiết sản phẩm
            <span class="text-gray-500 font-normal"
              >({{ totalFilteredProductDetails }} sản phẩm)</span
            >
          </h3>

          <div class="row">
            <div class="col-md-3 mb-3">
              <label class="form-label d-flex align-items-center">
                <span class="ms-2">Tìm sản phẩm</span>
              </label>
              <a-input
                v-model:value="productNameSearch"
                placeholder="Nhập tên sản phẩm..."
                size="large"
                @input="handleDetailSearch"
                class="w-full shadow-sm hover:shadow-md transition-shadow duration-200"
              />
            </div>

            <div class="col-md-3 mb-3">
              <label class="form-label">Kích cỡ</label>
              <a-select
                v-model:value="selectedSize"
                placeholder="Chọn kích cỡ"
                size="large"
                class="w-full shadow-sm hover:shadow-md transition-shadow duration-200"
                @change="handleDetailSearch"
                :allowClear="true"
              >
                <a-select-option v-for="size in sizes" :key="size.id" :value="size.id">
                  {{ size.ten }}
                </a-select-option>
              </a-select>
            </div>

            <div class="col-md-3 mb-3">
              <label class="form-label">Màu sắc</label>
              <a-select
                v-model:value="selectedColor"
                placeholder="Chọn màu sắc"
                size="large"
                class="w-full shadow-sm hover:shadow-md transition-shadow duration-200"
                @change="handleDetailSearch"
                :allowClear="true"
              >
                <a-select-option v-for="color in colors" :key="color.id" :value="color.id">
                  {{ color.ten }}
                </a-select-option>
              </a-select>
            </div>

            <div class="col-md-2 mb-3" style="margin-top: 32px">
              <a-button @click="clearDetailFilters" size="large" type="default" class="mr-2">
                Reset
              </a-button>
            </div>
          </div>

          <div class="bg-gray-200 text-black px-4 py-2 rounded-t-lg">
            <div class="grid grid-cols-12 gap-2 text-sm font-medium">
              <div class="col-span-1 text-center">
                <a-checkbox
                  :indeterminate="detailsIndeterminate"
                  :checked="detailsCheckAll"
                  @change="onCheckAllDetailsChange"
                />
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

            <div
              v-else-if="selectedProductDetails.length === 0"
              class="p-8 text-center text-gray-500"
            >
              Chưa có sản phẩm nào được chọn
            </div>

            <div
              v-else
              v-for="(detail, index) in filteredProductDetails"
              :key="detail.id"
              class="grid grid-cols-12 gap-2 p-3 border-b border-gray-100 text-sm hover:bg-gray-50"
            >
              <div class="col-span-1 text-center">
                <a-checkbox
                  :checked="detail.selected"
                  @change="
                    (e) => {
                      handleProductDetailSelect(detail.id, e.target.checked);
                    }
                  "
                />
              </div>
              <div class="col-span-1 text-center">
                {{ (detailCurrentPage - 1) * detailPageSize + index + 1 }}
              </div>
              <div class="col-span-2 text-center">
                <div class="w-10 h-10 bg-gray-200 rounded mx-auto flex items-center justify-center">
                  <img
                    v-if="detail.anh || detail.hinhAnh"
                    :src="detail.anh || detail.hinhAnh"
                    :alt="getProductName(detail)"
                    class="w-full h-full object-cover rounded"
                    @error="onImageError"
                  />
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
          <div class="mt-4">
            <a-pagination
              :current="detailCurrentPage"
              :total="totalFilteredProductDetails"
              :page-size="detailPageSize"
              @change="handleDetailPaginationChange"
              size="small"
              show-size-changer
              :page-size-options="detailPageSizeOptions"
              @show-size-change="
                (current, size) => {
                  detailPageSize = size;
                  detailCurrentPage = 1;
                }
              "
            />
          </div>
        </div>
        <a-popconfirm
            title="Bạn có chắc chắn muốn lưu thay đổi?"
            @confirm="handleSubmit"
          >
            <a-button
              type="primary"
              size="large"
              class="flex-1"
              :loading="submitting"
              style="background-color: #54bddb; border-color: #54bddb; margin-top: 2%;"
            >
              Cập nhật đợt giảm giá
            </a-button>
            </a-popconfirm>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
import { message } from "ant-design-vue";
import { SearchOutlined } from "@ant-design/icons-vue";
import {
  GetSanPham,
  getSanPhamChiTiets,
  DetailDotGiamGia,
  getColorsFromAPI,
  getSizesFromAPI,
  updateDotGiamGia,
  type DotGiamGiaResponse,
  type DotGiamGiaRequest,
} from "@/services/api/admin/dotgiamgia.api";
import type { Dayjs } from "dayjs";
import dayjs from "dayjs";
import BreadcrumbDefault from "@/components/ui/Breadcrumbs/BreadcrumbDefault.vue";

const router = useRouter();
const route = useRoute();
const dotGiamGiaId = route.params.id as string;

// Loading states
const loadingDetail = ref(false);
const submitting = ref(false);

// Form data
const formData = ref({
  code: "",
  tenKhuyenMai: "",
  giaTriGiam: "",
  trangThai: "CHUA_KICH_HOAT",
  ngayBatDau: null as Dayjs | null,
  ngayKetThuc: null as Dayjs | null,
});

// Product selection
const selectedProductIds = ref<string[]>([]); // Store selected product IDs
const selectedProductDetails = ref<any[]>([]);
const loadingDetails = ref<Set<string>>(new Set()); // Track loading state for each product
const searchTerm = ref("");
const currentPage = ref(10); // Page for available products table
const pageSize = ref(5); // Page size for available products table
const loading = ref(false);

// Detail search filters
const productNameSearch = ref("");
const selectedColor = ref(null);
const selectedSize = ref(null);
const sizes = ref([]);
const colors = ref([]);

// --- BIẾN MỚI CHO PHÂN TRANG CHI TIẾT SẢN PHẨM ---
const detailCurrentPage = ref(1);
const detailPageSize = ref(5);
const detailPageSizeOptions = ["5", "10", "20", "50"]; 
// --------------------------------------------------

const fetchColors = async () => {
  colors.value = await getColorsFromAPI();
};

const fetchSizes = async () => {
  const result = await getSizesFromAPI();
  sizes.value = result;
};

// API data
const availableProducts = ref<any[]>([]);
const totalProducts = ref(0); // Total for available products table

// Computed properties for available products table
const filteredProducts = computed(() => {
  return availableProducts.value.filter(
    (product) =>
      product.ma.toLowerCase().includes(searchTerm.value.toLowerCase()) ||
      product.ten.toLowerCase().includes(searchTerm.value.toLowerCase())
  );
});

// New computed property for filtering and PAGINATING product details
const filteredProductDetails = computed(() => {
  const allDetails = selectedProductDetails.value;

  const filtered = allDetails.filter((detail) => {
    // Product name search
    const productName = getProductName(detail).toLowerCase();
    const searchMatch =
      productNameSearch.value.trim() === "" ||
      productName.includes(productNameSearch.value.trim().toLowerCase());

    // Size filter
    const sizeMatch = selectedSize.value === null || detail.kichCo?.id === selectedSize.value;

    // Color filter
    const colorMatch = selectedColor.value === null || detail.mauSac?.id === selectedColor.value;

    return searchMatch && sizeMatch && colorMatch;
  });

  // --- LOGIC PHÂN TRANG MỚI CHO CHI TIẾT SẢN PHẨM ---
  const startIndex = (detailCurrentPage.value - 1) * detailPageSize.value;
  const endIndex = startIndex + detailPageSize.value;
  return filtered.slice(startIndex, endIndex);
  // ----------------------------------------------------
});

// --- COMPUTED MỚI CHO TỔNG SỐ LƯỢNG CHI TIẾT SẢN PHẨM ĐÃ LỌC ---
const totalFilteredProductDetails = computed(() => {
  const allDetails = selectedProductDetails.value;

  const filtered = allDetails.filter((detail) => {
    const productName = getProductName(detail).toLowerCase();
    const searchMatch =
      productNameSearch.value.trim() === "" ||
      productName.includes(productNameSearch.value.trim().toLowerCase());
    const sizeMatch = selectedSize.value === null || detail.kichCo?.id === selectedSize.value;
    const colorMatch = selectedColor.value === null || detail.mauSac?.id === selectedColor.value;
    return searchMatch && sizeMatch && colorMatch;
  });
  return filtered.length;
});
// ----------------------------------------------------------------

const handleDetailSearch = () => {
  // Khi các bộ lọc chi tiết thay đổi, reset về trang 1
  detailCurrentPage.value = 1;
  console.log("Detail search filters changed:", {
    productNameSearch: productNameSearch.value.trim(),
    selectedColor: selectedColor.value,
    selectedSize: selectedSize.value,
  });
};

const clearDetailFilters = () => {
  productNameSearch.value = "";
  selectedColor.value = null;
  selectedSize.value = null;
  detailCurrentPage.value = 1; // Reset trang khi xóa bộ lọc
};

// Store original data for comparison
const originalData = ref<DotGiamGiaResponse | null>(null);

const checkAll = computed(() => {
  return (
    filteredProducts.value.length > 0 &&
    filteredProducts.value.every((product) => selectedProductIds.value.includes(product.id))
  );
});

const indeterminate = computed(() => {
  const selectedCount = filteredProducts.value.filter((product) =>
    selectedProductIds.value.includes(product.id)
  ).length;
  return selectedCount > 0 && selectedCount < filteredProducts.value.length;
});

// Computed properties for product details check all
const detailsCheckAll = computed(() => {
  // Chỉ kiểm tra trên các chi tiết đã lọc và phân trang hiện tại
  return (
    filteredProductDetails.value.length > 0 &&
    filteredProductDetails.value.every((detail) => detail.selected)
  );
});

const detailsIndeterminate = computed(() => {
  // Chỉ kiểm tra trên các chi tiết đã lọc và phân trang hiện tại
  const selectedCount = filteredProductDetails.value.filter((detail) => detail.selected).length;
  return selectedCount > 0 && selectedCount < filteredProductDetails.value.length;
});

const loadingAnyDetails = computed(() => {
  return loadingDetails.value.size > 0;
});

const formatCurrencyVND = (amount: number): string => {
  return amount.toLocaleString("vi-VN", { style: "currency", currency: "VND" });
};

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

const onImageError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  target.style.display = "none";
};

// Hàm load chi tiết đợt giảm giá và tự động chọn sản phẩm/chi tiết
const loadDotGiamGiaDetail = async () => {
  try {
    loadingDetail.value = true;
    const response = await DetailDotGiamGia(dotGiamGiaId);

    if (response && typeof response === "object" && !Array.isArray(response)) {
      const data = response;
      originalData.value = data;

      // Set form data
      formData.value = {
        code: data.code || "",
        tenKhuyenMai: data.name || "",
        giaTriGiam: data.value?.toString() || "",
        trangThai: data.status || "CHUA_KICH_HOAT",
        ngayBatDau: data.startDate ? dayjs(data.startDate) : null,
        ngayKetThuc: data.endDate ? dayjs(data.endDate) : null,
      };

      // Improved parsing function to handle various API response formats for IDs
      const parseIds = (value: any) => {
        if (!value) return [];
        if (typeof value === "string") {
          return value
            .split(",")
            .map((id) => id.trim())
            .filter(Boolean);
        } else if (Array.isArray(value)) {
          return value.map((id) => id.toString().trim()).filter(Boolean);
        } else if (typeof value === "number") {
          return [value.toString()];
        }
        return [value.toString()];
      };

      const selectedProductIdsArray = parseIds(data.product);
      const productDetailIdsArray = parseIds(data.productDetail);

      // Set selected products
      selectedProductIds.value = [...selectedProductIdsArray];

      // Clear old details
      selectedProductDetails.value = [];
      await nextTick();

      // Load details with auto-select based on initial promotion data
      if (selectedProductIdsArray.length > 0) {
        for (const productId of selectedProductIdsArray) {
          await loadProductDetailsWithAutoSelect(productId, productDetailIdsArray);
          await new Promise((resolve) => setTimeout(resolve, 100)); // Delay to avoid overwhelming API
        }
      }
    }
  } catch (error) {
    console.error("Error loading detail:", error);
    message.error("Lỗi khi tải thông tin đợt giảm giá");
  } finally {
    loadingDetail.value = false;
  }
};

const loadProductDetailsWithAutoSelect = async (productId: string, productDetailIds: string[]) => {
  try {
    loadingDetails.value.add(productId);
    const response = await getSanPhamChiTiets(productId);

    if (response && Array.isArray(response) && response.length > 0) {
      const details = response.map((item) => {
        const itemIdStr = item.id.toString().trim();
        const isAutoSelected = productDetailIds.includes(itemIdStr); // Check if this detail ID is in the initial list
        return {
          id: item.id,
          productId: productId,
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
          selected: isAutoSelected, // Set selected state based on initial data
        };
      });

      // Remove existing details for this product to prevent duplicates
      selectedProductDetails.value = selectedProductDetails.value.filter(
        (d) => d.productId !== productId
      );
      // Add new details
      selectedProductDetails.value.push(...details);
      selectedProductDetails.value = [...selectedProductDetails.value]; // Force reactivity
    }
  } catch (error) {
    console.error(`Error loading details for product ${productId}:`, error);
    message.error(`Lỗi khi tải chi tiết sản phẩm ${productId}`);
  } finally {
    loadingDetails.value.delete(productId);
  }
};

// Adjust fetchProductDetails (for user selection, no auto-select)
const fetchProductDetails = async (productId: string) => {
  try {
    loadingDetails.value.add(productId);
    const response = await getSanPhamChiTiets(productId);

    if (response && Array.isArray(response) && response.length > 0) {
      const details = response.map((item) => ({
        id: item.id,
        productId: productId,
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
        selected: false, // User action - explicitly NOT auto-selected
      }));

      // Remove existing details for this product before adding new ones
      selectedProductDetails.value = selectedProductDetails.value.filter(
        (d) => d.productId !== productId
      );
      selectedProductDetails.value.push(...details);
      selectedProductDetails.value = [...selectedProductDetails.value]; // Force reactivity
    }
  } catch (error) {
    console.error("Error fetching product details:", error);
    message.error(`Lỗi khi tải chi tiết sản phẩm ${productId}`);
  } finally {
    loadingDetails.value.delete(productId);
  }
};

const handleSubmit = async () => {
  try {
    submitting.value = true;

    // Validate form
    if (!formData.value.tenKhuyenMai.trim()) {
      message.error("Vui lòng nhập tên khuyến mãi");
      return;
    }

    if (!formData.value.giaTriGiam.trim()) {
      message.error("Vui lòng nhập giá trị giảm");
      return;
    }

    const giaTriGiam = parseFloat(formData.value.giaTriGiam);
    if (isNaN(giaTriGiam) || giaTriGiam <= 0 || giaTriGiam > 80) {
      message.error("Giá trị giảm phải lớn hơn 0 và không được vượt quá 80%");
      return;
    }

    if (!formData.value.ngayBatDau) {
      message.error("Vui lòng chọn ngày bắt đầu");
      return;
    }

    if (!formData.value.ngayKetThuc) {
      message.error("Vui lòng chọn ngày kết thúc");
      return;
    }

    // Gather form data
    const requestData: DotGiamGiaRequest = {
      id: dotGiamGiaId,
      name: formData.value.tenKhuyenMai,
      value: parseFloat(formData.value.giaTriGiam),
      status: formData.value.trangThai,
      startDate: formData.value.ngayBatDau?.valueOf() || null,
      endDate: formData.value.ngayKetThuc?.valueOf() || null,
      idProductDetails: selectedProductDetails.value
        .filter((detail) => detail.selected) // Only send selected details
        .map((detail) => ({ id: detail.id })),
    };

    const response = await updateDotGiamGia(requestData);
    console.log("Update response:", response);

    message.success({
      content: "Cập nhật đợt giảm giá thành công!",
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
  } catch (error) {
    console.error("Update error:", error);
    if (error.response && error.response.data) {
      message.error(error.response.data.message);
    } else {
      message.error("Lỗi khi cập nhật đợt giảm giá. Vui lòng thử lại.");
    }
  } finally {
    submitting.value = false;
  }
};

const handleCancel = () => {
  router.push("/admin/dot-giam-gia");
};

// Product selection handling
const handleProductSelect = async (productId: string, checked: boolean) => {
  if (checked) {
    if (!selectedProductIds.value.includes(productId)) {
      selectedProductIds.value.push(productId);
    }
    await fetchProductDetails(productId); // Fetch details for newly selected product
  } else {
    const index = selectedProductIds.value.indexOf(productId);
    if (index > -1) {
      selectedProductIds.value.splice(index, 1);
    }
    // Remove details of unchecked product
    selectedProductDetails.value = selectedProductDetails.value.filter(
      (detail) => detail.productId !== productId
    );
    selectedProductDetails.value = [...selectedProductDetails.value]; // Force reactivity
  }
};

const onCheckAllChange = async (e: any) => {
  if (e.target.checked) {
    const newProductIds = filteredProducts.value
      .map((product) => product.id)
      .filter((id) => !selectedProductIds.value.includes(id));

    selectedProductIds.value.push(...newProductIds);

    for (const productId of newProductIds) {
      await fetchProductDetails(productId); // Load details, but don't auto-select them
    }
  } else {
    const filteredProductIds = filteredProducts.value.map((product) => product.id);
    // Remove product IDs that are currently in the filtered view
    selectedProductIds.value = selectedProductIds.value.filter(
      (id) => !filteredProductIds.includes(id)
    );
    // Unselect and remove corresponding details
    selectedProductDetails.value = selectedProductDetails.value.filter((detail) => {
      return !filteredProductIds.includes(detail.productId);
    });
    selectedProductDetails.value = [...selectedProductDetails.value]; // Force reactivity
  }
};

const handleProductDetailSelect = (detailId: string | number, checked: boolean) => {
  const detailIndex = selectedProductDetails.value.findIndex(
    (item) => item.id.toString().trim() === detailId.toString().trim()
  );

  if (detailIndex !== -1) {
    const newDetails = [...selectedProductDetails.value];
    newDetails[detailIndex] = {
      ...newDetails[detailIndex],
      selected: checked,
    };
    selectedProductDetails.value = newDetails;
  }
};

const onCheckAllDetailsChange = (e: any) => {
  const checked = e.target.checked;
  // Apply changes to currently displayed (filtered and paginated) details
  filteredProductDetails.value.forEach((detail) => {
    // Find the original detail object in the main array and update its 'selected' state
    const originalDetail = selectedProductDetails.value.find((d) => d.id === detail.id);
    if (originalDetail) {
      originalDetail.selected = checked;
    }
  });
  // Force reactivity update for the entire selectedProductDetails array to reflect changes
  selectedProductDetails.value = [...selectedProductDetails.value];
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchProducts();
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchProducts();
};

const fetchProducts = async () => {
  try {
    loading.value = true;
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      search: searchTerm.value,
    };

    const response = await GetSanPham(params);

    if (response) {
      let products = [];
      if (Array.isArray(response)) {
        products = response;
      } else if (response.data && Array.isArray(response.data)) {
        products = response.data;
      } else if (response.data && response.data.content && Array.isArray(response.data.content)) {
        products = response.data.content;
        totalProducts.value = response.data.totalElements || response.data.content.length;
      } else if (response.content && Array.isArray(response.content)) {
        products = response.content;
        totalProducts.value = response.totalElements || response.content.length;
      } else {
        products = [];
      }

      availableProducts.value = products.map((product) => ({
        id: product.id,
        ma: product.ma,
        ten: product.ten,
        status: product.status,
        trangThai: product.status === "ACTIVE" ? "Đang kinh doanh" : "Ngừng kinh doanh",
        thuongHieu: product.thuongHieu,
        xuatSu: product.xuatSu,
        danhMuc: product.danhMuc,
        loaiDe: product.loaiDe,
        chatLieu: product.chatLieu,
        moTa: product.moTa,
      }));

      if (!totalProducts.value) {
        totalProducts.value = availableProducts.value.length;
      }
    } else {
      availableProducts.value = [];
      totalProducts.value = 0;
    }
  } catch (error) {
    availableProducts.value = [];
    totalProducts.value = 0;
  } finally {
    loading.value = false;
  }
};

// --- HÀM MỚI CHO PHÂN TRANG CHI TIẾT SẢN PHẨM ---
const handleDetailPaginationChange = (page: number, newPageSize: number) => {
  detailCurrentPage.value = page;
  detailPageSize.value = newPageSize;
  // No need to re-fetch as data is already in selectedProductDetails.value
};
// --------------------------------------------------

onMounted(async () => {
  try {
    console.log("Starting page initialization...");

    // Load colors and sizes first
    await Promise.all([fetchColors(), fetchSizes()]);
    console.log("Colors and sizes loaded");

    // Load products list for the available products table
    await fetchProducts();
    console.log("Products list loaded");

    // Load discount detail (this will also auto-load and auto-select product details)
    await loadDotGiamGiaDetail();
    console.log("Discount details loaded and auto-selected");

    console.log("All data loaded successfully");
  } catch (error) {
    console.error("Error in onMounted:", error);
    message.error("Lỗi khi tải dữ liệu trang");
  }
});
</script>

<style scoped lang="scss">
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

.h-10 {
  height: 2.5rem;
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

.space-y-4 > * + * {
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
  font-size: 0.875rem;
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

.page-container {
  padding: 20px;
  /* Overall padding for the page content */
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

.section-title {
  margin-top: 30px;
  /* Space above each main section title */
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  /* Space below the title */
  margin-left: 0px;
  /* Remove left margin if section-title is directly under padding */
  color: #333;
  /* Darker color for titles */
  display: flex;
  /* To align icon and text */
  align-items: center;
  /* Vertically center icon and text */
  gap: 8px;
  /* Space between icon and text */
}

/* Remove or adjust body styles if they are global.
   Scoped styles prevent them from affecting the entire app. */
body {
  font-family: "Roboto", sans-serif;
}
</style>
