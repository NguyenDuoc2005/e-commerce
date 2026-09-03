<template>
  <div class="checkout-page container py-4">
    <div class="mb-3">
      <BreadCrumbUser :routes="breadcrumbRoutes" title="Xác nhận & Đặt hàng" />
    </div>

    <div class="row g-4">
      <div class="col-lg-5">
        <div class="bg-white p-4 rounded shadow-sm">
          <h5 class="fw-semibold mb-4">Thông tin nhận hàng</h5>

          <a-form layout="vertical" :model="form" :rules="rules" ref="formRef">
            <div class="row g-3">
              <div class="col-sm-6">
                <a-form-item label="Họ và tên" name="hoTen">
                  <a-input v-model:value="form.hoTen" placeholder="Nguyễn Văn A" />
                </a-form-item>
              </div>
              <div class="col-sm-6">
                <a-form-item label="Số điện thoại" name="soDienThoai">
                  <a-input v-model:value="form.soDienThoai" placeholder="0123 456 789" />
                </a-form-item>
              </div>
              <div class="col-sm-6">
                <a-form-item label="Email" name="email">
                  <a-input v-model:value="form.email" placeholder="example@domain.com" />
                </a-form-item>
              </div>
              <div class="col-sm-6">
                <a-form-item label="Tỉnh / TP" name="tinh">
                  <a-select v-model:value="form.tinh" placeholder="Chọn tỉnh" :options="provinceOptions"
                    @change="handleProvinceChange" :loading="loadingProvinces" />
                </a-form-item>
              </div>
              <div class="col-sm-4">
                <a-form-item label="Quận / Huyện" name="huyen">
                  <a-select v-model:value="form.huyen" placeholder="Chọn huyện" :options="districtOptions"
                    @change="handleDistrictChange" :disabled="!form.tinh" :loading="loadingDistricts" />
                </a-form-item>
              </div>
              <div class="col-sm-4">
                <a-form-item label="Phường / Xã" name="phuong">
                  <a-select v-model:value="form.phuong" placeholder="Chọn phường" :options="wardOptions"
                    :disabled="!form.huyen" :loading="loadingWards" />
                </a-form-item>
              </div>
              <div class="col-sm-4">
                <img src="/images/ghn-logo.webp" alt="Giỏ hàng nhanh"
                  style="width: 100px; height: 90px; margin-left: 20px ; object-fit: contain;" class="me-1" />
              </div>

              <div class="col-12">
                <a-form-item label="Địa chỉ cụ thể" name="diaChi">
                  <a-input v-model:value="form.diaChi" placeholder="Số nhà, tên đường..." />
                </a-form-item>
              </div>

              <div v-if="!ghnAvailable" class="col-12">
                <a-alert type="warning" show-icon
                  message="Dịch vụ địa chỉ GHN đang tạm không khả dụng. Hãy nhập đầy đủ địa chỉ cụ thể; phí ship tạm tính là 30.000₫." />
              </div>

              <div class="col-12">
                <a-form-item label="Ghi chú">
                  <a-textarea v-model:value="form.ghiChu" placeholder="Ghi chú thêm (nếu có)"
                    :auto-size="{ minRows: 5, maxRows: 8 }" />
                </a-form-item>
              </div>
            </div>
          </a-form>
        </div>
      </div>

      <div class="col-lg-7">
        <div class="bg-white p-4 rounded shadow-sm mb-4">
          <h5 class="fw-semibold mb-4">Đơn hàng ({{ listSanPham.length }} sản phẩm)</h5>

          <ul class="list-unstyled mb-3">
            <li class="d-flex align-items-center mb-3" v-for="item in listSanPham" :key="item.id">
              <img :src="item.imageUrl" class="rounded me-3" style="width: 50px; height: 50px; object-fit: cover" />
              <div class="flex-grow-1">
                <div class="fw-medium">{{ item.name }}</div>
                <div class="small text-muted">
                  Phân loại: Màu {{ item.color }} / Size {{ item.size }} - SL: {{ item.quantity }}
                </div>
              </div>
              <div class="fw-semibold">
                {{
                  ((item.discountPrice < item.originalPrice ? item.discountPrice : item.originalPrice) * item.quantity)
                    .toLocaleString("vi-VN") }}₫ </div>
            </li>
          </ul>

          <div class="d-flex mb-3">
            <a-input v-model:value="form.maGiamGia" placeholder="Mã giảm giá" class="me-2" disabled />
            <a-button @click="handleShowVouchers">Chọn</a-button>
          </div>
          <div v-if="isBestVoucher && form.maGiamGia" class="text-success small mt-1">
            Đã áp dụng phiếu giảm giá tốt nhất!
          </div>

          <div class="border-top pt-3">
            <div class="d-flex justify-content-between mb-2">
              <span class="fw-semibold">Tạm tính:</span>
              <span class="fw-semibold">{{ tongTien.toLocaleString("vi-VN") }}₫</span>
            </div>

            <div class="d-flex justify-content-between mb-2">
              <span class="fw-semibold">Phí vận chuyển:</span>
              <span class="fw-semibold text-success">{{ phiShip.toLocaleString("vi-VN") }}₫</span>
            </div>

            <div class="d-flex justify-content-between mb-2">
              <span class="fw-semibold">Giảm giá:</span>
              <span class="fw-semibold text-success">{{ giamGia.toLocaleString("vi-VN") }}₫</span>
            </div>

            <div class="d-flex justify-content-between border-top pt-2 mt-2">
              <span class="fw-bold">Tổng cộng:</span>
              <span class="fw-bold text-danger">{{ tongCong.toLocaleString("vi-VN") }}₫</span>
            </div>
          </div>
        </div>

        <div class="bg-white p-4 rounded shadow-sm">
          <h5 class="fw-semibold mb-3">Hình thức thanh toán</h5>

          <a-radio-group v-model:value="form.thanhToan" class="d-flex flex-column gap-2">
            <a-radio value="TIEN_MAT">Thanh toán khi nhận hàng (COD)</a-radio>
            <a-radio value="VNPAY">Thanh toán VnPay</a-radio>
          </a-radio-group>

          <a-button type="primary" block class="mt-4" style="height: 52px; font-size: 1.15rem" @click="confirmCheckout"
            :loading="loadingCheckout">
            ĐẶT HÀNG
          </a-button>
        </div>
      </div>
    </div>
  </div>

  <a-modal v-model:open="showConfirmModal" title="Xác nhận đặt hàng" @ok="handleConfirmOk" @cancel="handleConfirmCancel"
    :confirm-loading="loadingCheckout" ok-text="Xác nhận" cancel-text="Hủy">
    <p>Bạn có chắc chắn muốn đặt đơn hàng này với tổng cộng là **{{ tongCong.toLocaleString("vi-VN") }}₫** không?</p>
    <p>Phương thức thanh toán: **{{ form.thanhToan === 'TIEN_MAT' ? 'Thanh toán khi nhận hàng (COD)' : 'Thanh toán VnPay'}}**</p>
    <p class="text-danger mt-3">Vui lòng kiểm tra lại thông tin nhận hàng và đơn hàng trước khi xác nhận.</p>
  </a-modal>

  <a-modal v-model:open="showVoucherModal" title="Chọn phiếu giảm giá" @ok="applySelectedVoucher" ok-text="Áp dụng"
    cancel-text="Hủy">
    <a-radio-group v-model:value="selectedVoucher" class="d-flex flex-column gap-2">
      <a-radio v-for="(voucher, index) in vouchers" :key="voucher.ma" :value="voucher.ma" class="voucher-option">
        <div class="voucher-card">
          <div class="voucher-header">
            <span class="voucher-amount">{{ voucher.ten }}</span>
            <span v-if="index === 0" class="voucher-badge">Tốt nhất</span>
          </div>

          <div class="voucher-condition">Đơn tối thiểu {{ voucher.dieuKien }}₫</div>

          <div class="voucher-details">
            <span class="voucher-method" v-if="!voucher.kieuGiam">
              Giá trị giảm: {{ voucher.giaGiam }} đ
            </span>
            <span class="voucher-method" v-else>
              Giá trị giảm: {{ voucher.phanTramGiam }} %
            </span>
          </div>

          <div class="voucher-footer">
            <span class="voucher-expiry">HSD: {{ formatDate(voucher.ngayKetThuc) }}</span>
          </div>
        </div>
      </a-radio>
    </a-radio-group>

    <p v-if="!vouchers.length" class="text-danger">Không có phiếu giảm giá áp dụng.</p>
  </a-modal>


</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { message } from "ant-design-vue";
import { useRouter } from "vue-router";
import BreadCrumbUser from "@/components/ui/Breadcrumbs/BreadCrumbUser.vue";
import {
  getGHNProvinces,
  getGHNDistricts,
  getGHNWards,
  calculateFee,
  getAvailableServices,
  type Province,
  type District,
  type Ward,
  type ShippingFeeRequest,
  type GHNAvailableServiceRequest,
} from "@/services/api/ghn.api";
import { localStorageAction } from "@/utils/storage";
import { USER_INFO_STORAGE_KEY, CHECKOUT_STORAGE_KEY } from "@/constants/storageKey";
import { ThanhToan, getListPGG, getKhachHangDetail } from "@/services/api/permitall/thanhtoan/thanhtoan.api";

const checkoutIdempotencyKey = ref(crypto.randomUUID());

// Interface definitions
interface CartItem {
  id: string;
  idSP: string;
  name: string;
  originalPrice: number;
  discountPrice: number;
  quantity: number;
  imageUrl: string;
  color: string;
  size: string;
  weight?: number;
  height?: number;
  length?: number;
  width?: number;
  idChiTietSanPham?: string;
  soLuongTrongKho?: number;
  sellerId?: string;
  shopName?: string;
  sellerSlug?: string;
}

interface CheckoutShopGroup {
  sellerId: string;
  shopName: string;
  items: CartItem[];
}

interface Voucher {
  ma: string;
  ten: string;
  giaTriGiamThucTe: number;
  dieuKien: number;
  giaGiam: number;
  phanTramGiam: number;
  kieuGiam: boolean;
  ngayKetThuc?: string;
}

const showVoucherModal = ref(false);
const vouchers = ref<Voucher[]>([]);
const selectedVoucher = ref<string | null>(null);
const isBestVoucher = ref(true); // Biến để kiểm soát hiển thị thông báo "phiếu giảm giá tốt nhất"

function formatDate(dateStr: string) {
  const date = new Date(dateStr);
  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const year = date.getFullYear();
  return `${day}.${month}.${year}`;
}


const breadcrumbRoutes = [
  { name: "Trang chủ", path: "/" },
  { name: "Giỏ hàng", path: "/gio-hang" },
  { name: "Thanh toán", path: "/thanh-toan" },
];

const formRef = ref();
const form = ref({
  hoTen: "",
  soDienThoai: "",
  email: "",
  tinh: null as number | null,
  huyen: null as number | null,
  phuong: null as string | null,
  diaChi: "",
  ghiChu: "",
  thanhToan: "TIEN_MAT",
  maGiamGia: "",
});

// GHN configuration
const GHN_TOKEN = "72f634c6-58a2-11f0-8a1e-1e10d8df3c04";
const SHOP_ID = 5872469;
const FROM_DISTRICT_ID = 3440;
const FROM_WARD_CODE = "13010";
const idKH = localStorageAction.get(USER_INFO_STORAGE_KEY) || null;

// VNPAY Configuration
const VNPAY_CONFIG = {
  vnp_TmnCode: "YOUR_TMN_CODE",
  vnp_HashSecret: "YOUR_HASH_SECRET",
  vnp_Url: "https://sandbox.vnpayment.vn/paymentv2/vpcpay.aspx",
  vnp_ReturnUrl: "http://localhost:5173/thanh-toan-thanh-cong",
};

// Reactive state for GHN data
const provinces = ref<Province[]>([]);
const districts = ref<District[]>([]);
const wards = ref<Ward[]>([]);
const phiShip = ref(0);
const loadingProvinces = ref(false);
const loadingDistricts = ref(false);
const loadingWards = ref(false);
const selectedServiceId = ref<number | null>(null);
const ghnAvailable = ref(true);
const loadingCheckout = ref(false);
const showConfirmModal = ref(false);
const idUser = localStorageAction.get(USER_INFO_STORAGE_KEY);
// Computed options for select components
const provinceOptions = computed(() =>
  provinces.value.map((p) => ({
    value: p.ProvinceID,
    label: p.ProvinceName,
  }))
);

const districtOptions = computed(() =>
  districts.value.map((d) => ({
    value: d.DistrictID,
    label: d.DistrictName,
  }))
);

const wardOptions = computed(() =>
  wards.value.map((w) => ({
    value: w.WardCode,
    label: w.WardName,
  }))
);

// Cart items
const listSanPham = ref<CartItem[]>([]);
const giamGia = ref(0);

const numberValue = (value: unknown, fallback = 0) => {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
};

const selectionValue = (item: any, keyword: string) => {
  const selections = Array.isArray(item?.selections) ? item.selections : [];
  return selections.find((selection: any) =>
    String(selection?.axisName || "").toLocaleLowerCase("vi-VN").includes(keyword)
  )?.value;
};

const normalizeCheckoutItem = (item: any, index: number): CartItem | null => {
  const variant = item?.productVariant || item?.sanPhamChiTiet || {};
  const variantId = item?.idSP || item?.idChiTietSanPham || item?.idSPCT
    || item?.productVariantId || item?.sanPhamChiTietId || variant?.id;
  if (!variantId) return null;

  const originalPrice = numberValue(
    item?.originalPrice ?? item?.giaBan ?? item?.price ?? variant?.salePrice
  );
  const discountPrice = numberValue(
    item?.discountPrice ?? item?.dotGiamGia?.giaSau ?? variant?.discountPrice ?? variant?.salePrice,
    originalPrice
  );
  const quantity = Math.max(1, Math.trunc(numberValue(item?.quantity ?? item?.soLuongMua, 1)));
  const product = variant?.product || variant?.sanPham || {};

  return {
    id: String(item?.id || `checkout_${index}_${variantId}`),
    idSP: String(variantId),
    name: item?.name || item?.tenSanPham || product?.name || variant?.productName || "Sản phẩm",
    originalPrice,
    discountPrice,
    quantity,
    imageUrl: item?.imageUrl || item?.hinhAnh || variant?.imageUrl || "",
    color: item?.color || item?.mauSac?.tenMauSac || selectionValue(item, "màu") || "-",
    size: item?.size || item?.kichCo?.tenKichCo || selectionValue(item, "kích") || item?.variantLabel || "-",
    idChiTietSanPham: String(variantId),
    soLuongTrongKho: numberValue(item?.soLuongTrongKho ?? variant?.quantity),
    sellerId: item?.sellerId || variant?.sellerId,
    shopName: item?.shopName,
    sellerSlug: item?.sellerSlug,
  };
};

const normalizeVoucher = (voucher: any): Voucher => ({
  ma: String(voucher?.ma ?? voucher?.code ?? ""),
  ten: String(voucher?.ten ?? voucher?.name ?? voucher?.code ?? "Phiếu giảm giá"),
  giaTriGiamThucTe: numberValue(voucher?.giaTriGiamThucTe ?? voucher?.actualDiscountValue),
  dieuKien: numberValue(voucher?.dieuKien ?? voucher?.condition_amount),
  giaGiam: numberValue(voucher?.giaGiam ?? voucher?.maxDiscountAmount ?? voucher?.max_discount_amount),
  phanTramGiam: numberValue(voucher?.phanTramGiam ?? voucher?.discountValue ?? voucher?.discount_value),
  kieuGiam: Boolean(voucher?.kieuGiam ?? voucher?.discountMethod ?? voucher?.discount_method),
  ngayKetThuc: voucher?.ngayKetThuc ?? voucher?.end_date,
});

// Price calculations
const getPrice = (item: CartItem) => {
  return item.discountPrice < item.originalPrice ? item.discountPrice : item.originalPrice;
};

const tongTien = computed(() =>
  listSanPham.value.reduce((sum, sp) => sum + getPrice(sp) * sp.quantity, 0)
);

const tongTienTruocGiam = computed(() => tongTien.value + phiShip.value);
const tongCong = computed(() =>
  Math.max(tongTien.value + phiShip.value - giamGia.value, 0)
);

const checkoutShopGroups = computed<CheckoutShopGroup[]>(() => {
  const grouped = new Map<string, CheckoutShopGroup>();
  for (const item of listSanPham.value) {
    const sellerId = item.sellerId || "UNKNOWN_SELLER";
    if (!grouped.has(sellerId)) {
      grouped.set(sellerId, {
        sellerId,
        shopName: item.shopName || "Shop",
        items: [],
      });
    }
    grouped.get(sellerId)!.items.push(item);
  }
  return Array.from(grouped.values());
});

const shopSubtotal = (group: CheckoutShopGroup) => {
  return group.items.reduce((sum, item) => sum + getPrice(item) * item.quantity, 0);
};

watch(tongTienTruocGiam, (newValue) => {
  console.log("tongTienTruocGiam thay đổi:", newValue);
  if (form.value.maGiamGia) {
    handleApplyDiscount(); // Gọi lại để cập nhật giamGia với TongTien mới
  }
});

// Hàm mới để áp dụng phiếu giảm giá tốt nhất mà không mở modal
const applyBestVoucher = async () => {
  try {
    const req = {
      idKH: idKH?.userId || "khách lẻ",
      tongTien: tongTienTruocGiam.value,
      maPGG: "",
    };

    const found = await getListPGG(req);
    vouchers.value = Array.isArray(found.data) ? found.data.map(normalizeVoucher).filter(voucher => voucher.ma) : [];

    if (!vouchers.value.length) {
      message.warning("Không có phiếu giảm giá nào áp dụng được!");
      isBestVoucher.value = false;
      return;
    }

    // Áp dụng voucher tốt nhất (đầu tiên)
    selectedVoucher.value = vouchers.value[0].ma;
    form.value.maGiamGia = vouchers.value[0].ma;
    giamGia.value = Math.min(vouchers.value[0].giaTriGiamThucTe, tongTienTruocGiam.value);
    isBestVoucher.value = true;
    message.success(`✅ Tự động áp dụng phiếu giảm giá "${vouchers.value[0].ten}" giảm ${giamGia.value.toLocaleString("vi-VN")}₫`);
  } catch (error) {
    console.error("Lỗi khi áp dụng phiếu giảm giá tốt nhất:", error);
    message.error("Có lỗi khi áp dụng phiếu giảm giá!");
    isBestVoucher.value = false;
  }
};

const handleShowVouchers = async () => {
  try {
    const req = {
      idKH: idKH?.userId || "khách lẻ",
      tongTien: tongTienTruocGiam.value,
      maPGG: "",
    };

    const found = await getListPGG(req);
    vouchers.value = Array.isArray(found.data) ? found.data.map(normalizeVoucher).filter(voucher => voucher.ma) : [];

    if (!vouchers.value.length) {
      message.warning("Không có phiếu giảm giá nào áp dụng được!");
      isBestVoucher.value = false;
      return;
    }

    // Không tự động áp dụng, chỉ mở modal
    showVoucherModal.value = true;
  } catch (error) {
    console.error("Lỗi khi lấy danh sách phiếu giảm giá:", error);
    message.error("Có lỗi khi lấy danh sách phiếu giảm giá!");
    isBestVoucher.value = false;
  }
};

const applySelectedVoucher = () => {
  const voucher = vouchers.value.find((v) => v.ma === selectedVoucher.value);
  if (voucher) {
    form.value.maGiamGia = voucher.ma;
    giamGia.value = Math.min(voucher.giaTriGiamThucTe, tongTienTruocGiam.value);
    message.success(`✅ Áp dụng phiếu giảm giá "${voucher.ten}" giảm ${giamGia.value.toLocaleString("vi-VN")}₫`);

    // Nếu chọn voucher khác với voucher đầu tiên, ẩn thông báo
    if (voucher.ma !== vouchers.value[0]?.ma) {
      isBestVoucher.value = false;
    }
  }
  showVoucherModal.value = false;
};

const handleApplyDiscount = async () => {
  if (!form.value.maGiamGia) return;

  console.log("Áp dụng mã giảm giá:", tongTienTruocGiam.value);

  try {
    const req = {
      idKH: idKH?.userId || "khách lẻ",
      tongTien: tongTienTruocGiam.value,
      maPGG: form.value.maGiamGia,
    };

    const found = await getListPGG(req);
    const voucher = Array.isArray(found.data) ? normalizeVoucher(found.data[0]) : null;
    if (voucher) {
      giamGia.value = Math.min(voucher.giaTriGiamThucTe, tongTienTruocGiam.value);
      message.success(`✅ Cập nhật giảm giá: ${giamGia.value.toLocaleString("vi-VN")}₫`);
    }
  } catch (error) {
    console.error("Lỗi khi cập nhật phiếu giảm giá:", error);
    message.error("Có lỗi khi cập nhật phiếu giảm giá!");
  }
};

watch([tongTien, phiShip, giamGia], () => {
  console.log("tongTien:", tongTien.value);
  console.log("phiShip:", phiShip.value);
  console.log("giamGia:", giamGia.value);
  console.log("tongCong:", tongCong.value);
  console.log("tongCong:", tongTienTruocGiam.value);
});

// Validation rules
const requireGhnLocation = (messageText: string) => ({
  trigger: "change",
  validator: async (_rule: unknown, value: unknown) => {
    if (ghnAvailable.value && !value) throw new Error(messageText);
  },
});

const rules = {
  hoTen: [{ required: true, message: "Vui lòng nhập họ tên", trigger: "blur" }],
  soDienThoai: [{ required: true, message: "Vui lòng nhập số điện thoại", trigger: "blur" }],
  email: [
    { required: true, message: "Vui lòng nhập email", trigger: "blur" },
    { type: "email", message: "Email không hợp lệ", trigger: "blur" }
  ],
  tinh: [requireGhnLocation("Chọn tỉnh")],
  huyen: [requireGhnLocation("Chọn huyện")],
  phuong: [requireGhnLocation("Chọn phường")],
  diaChi: [{ required: true, message: "Nhập địa chỉ cụ thể", trigger: "blur" }],
};

onMounted(async () => {
  try {
    loadingProvinces.value = true;
    provinces.value = await getGHNProvinces(GHN_TOKEN);
    ghnAvailable.value = provinces.value.length > 0;
  } catch (error) {
    ghnAvailable.value = false;
    phiShip.value = 30000;
    message.warning("Không thể tải địa chỉ GHN. Hệ thống sẽ dùng địa chỉ nhập tay và phí ship tạm tính 30.000₫.");
  } finally {
    loadingProvinces.value = false;
  }

  const storedItems = localStorageAction.get(CHECKOUT_STORAGE_KEY);
  if (Array.isArray(storedItems)) {
    listSanPham.value = storedItems
      .map(normalizeCheckoutItem)
      .filter((item): item is CartItem => item !== null);
  }
  if (listSanPham.value.length > 0) {
    console.log("Dữ liệu sản phẩm từ CHECKOUT_STORAGE_KEY:", listSanPham.value);
  } else {
    console.warn("Không tìm thấy dữ liệu sản phẩm trong CHECKOUT_STORAGE_KEY.");
    message.warning("Không có sản phẩm để thanh toán. Vui lòng quay lại giỏ hàng!");
    await router.push("/gio-hang");
    return;
  }

  // Tự động áp dụng phiếu giảm giá tốt nhất mà không mở modal
  await applyBestVoucher();
});


const fetchProductDetails = async (id: string) => {
  try {
    const response = await getKhachHangDetail(id);
    const data = response.data;

    // Điền thông tin khách hàng vào form
    form.value.hoTen = data.ten || data.name || "";
    form.value.soDienThoai = data.sdt || data.phoneNumber || "";
    form.value.email = data.email || "";
    form.value.diaChi = data.diaChi || data.address || "";
    form.value.tinh = null; // Sẽ được cập nhật sau khi lấy danh sách tỉnh
    form.value.huyen = null; // Sẽ được cập nhật sau khi lấy danh sách huyện
    form.value.phuong = null; // Sẽ được cập nhật sau khi lấy danh sách phường
    console.log("Dữ liệu khách hàng:", data.tinh, data.huyen, data.xa);
    // Nếu có dữ liệu địa chỉ, tìm tỉnh, huyện, phường tương ứng từ API GHN
    if (data.tinh && data.huyen && data.xa) {
      // Tải danh sách tỉnh
      loadingProvinces.value = true;
      provinces.value = await getGHNProvinces(GHN_TOKEN);
      loadingProvinces.value = false;

      // Tìm tỉnh phù hợp
      const selectedProvince = provinces.value.find(
        (p) => p.ProvinceID == data.tinh
      );
      console.log("Selected Province:", data.tinh);
      console.log("Selected Province:", provinces.value);
      console.log("Selected Province:", selectedProvince);
      if (selectedProvince) {
        form.value.tinh = selectedProvince.ProvinceID;

        // Tải danh sách huyện
        loadingDistricts.value = true;
        districts.value = await getGHNDistricts(selectedProvince.ProvinceID, GHN_TOKEN);
        loadingDistricts.value = false;

        // Tìm huyện phù hợp
        const selectedDistrict = districts.value.find(
          (d) => d.DistrictID == data.huyen
        );
        if (selectedDistrict) {
          form.value.huyen = selectedDistrict.DistrictID;

          // Tải danh sách phường
          loadingWards.value = true;
          wards.value = await getGHNWards(selectedDistrict.DistrictID, GHN_TOKEN);
          loadingWards.value = false;

          // Tìm phường phù hợp
          const selectedWard = wards.value.find((w) => w.WardCode == data.xa);
          if (selectedWard) {
            form.value.phuong = selectedWard.WardCode;
          }
        }
      }
    }

    // Tính phí vận chuyển sau khi điền địa chỉ
    if (form.value.tinh && form.value.huyen && form.value.phuong) {
      await calculateShippingFee();
    }
  } catch (error) {
    console.error("Lỗi khi lấy thông tin khách hàng:", error);
  }
};

onMounted(async () => {
  if (idKH?.userId != null) {
    console.log("Fetching product details for user ID:", idKH.userId);
    fetchProductDetails(idKH.userId);
  }
});

// Handle province change
const handleProvinceChange = async (provinceId: number) => {
  form.value.huyen = null;
  form.value.phuong = null;
  districts.value = [];
  wards.value = [];
  phiShip.value = 30000;

  try {
    loadingDistricts.value = true;
    districts.value = await getGHNDistricts(provinceId, GHN_TOKEN);
  } catch (error) {
    message.error("Không thể tải danh sách quận/huyện!");
  } finally {
    loadingDistricts.value = false;
  }
};

// Handle district change
const handleDistrictChange = async (districtId: number) => {
  form.value.phuong = null;
  wards.value = [];
  phiShip.value = 30000;

  try {
    loadingWards.value = true;
    wards.value = await getGHNWards(districtId, GHN_TOKEN);
  } catch (error) {
    message.error("Không thể tải danh sách phường/xã!");
  } finally {
    loadingWards.value = false;
  }
};

// Calculate shipping fee
const calculateShippingFee = async () => {
  if (!form.value.tinh || !form.value.huyen || !form.value.phuong) return;

  try {
    const serviceRequest: GHNAvailableServiceRequest = {
      shop_id: SHOP_ID,
      from_district: FROM_DISTRICT_ID,
      to_district: form.value.huyen!,
    };

    const serviceResponse = await getAvailableServices(GHN_TOKEN, serviceRequest);
    if (serviceResponse.data && serviceResponse.data.length > 0) {
      selectedServiceId.value = serviceResponse.data[0].service_id;
    } else {
      message.error("Không tìm thấy dịch vụ vận chuyển phù hợp!");
      return;
    }

    const totalWeight = listSanPham.value.reduce((sum, item) => sum + (item.weight || 1000), 0);
    const totalHeight = listSanPham.value.reduce((sum, item) => sum + (item.height || 20), 0);
    const totalLength = listSanPham.value.reduce((sum, item) => sum + (item.length || 30), 0);
    const totalWidth = listSanPham.value.reduce((sum, item) => sum + (item.width || 20), 0);

    const feeRequest: ShippingFeeRequest = {
      myRequest: {
        FromDistrictID: FROM_DISTRICT_ID,
        FromWardCode: FROM_WARD_CODE,
        ServiceID: selectedServiceId.value!,
        ToDistrictID: form.value.huyen!,
        ToWardCode: form.value.phuong!,
        Height: totalHeight,
        Length: totalLength,
        Weight: totalWeight,
        Width: totalWidth,
        InsuranceValue: tongTien.value,
        Coupon: null,
        PickShift: null,
      },
    };

    const feeResponse = await calculateFee(feeRequest, GHN_TOKEN, SHOP_ID);
    phiShip.value = feeResponse.data.total;
    message.success(`Phí vận chuyển: ${phiShip.value.toLocaleString("vi-VN")}₫`);
  } catch (error) {
    message.warning("Không thể tính phí GHN, đang dùng mức tạm tính 30.000₫.");
    phiShip.value = 30000;
  }
};

// Watch ward changes
watch(() => form.value.phuong, calculateShippingFee);

const router = useRouter();

const confirmCheckout = async () => {
  try {
    await formRef.value.validate();
    showConfirmModal.value = true;
  } catch (err) {
    message.error("Vui lòng điền đầy đủ và chính xác thông tin nhận hàng!");
    console.error("Validation failed:", err);
  }
};

const handleConfirmOk = async () => {
  showConfirmModal.value = false;
  await performCheckout();
};

const handleConfirmCancel = () => {
  showConfirmModal.value = false;
};

watch(() => form.value.thanhToan, (newValue) => {
  console.log('form.thanhToan updated:', newValue);
});

const performCheckout = async () => {
  try {
    loadingCheckout.value = true;

    if (!listSanPham.value.length || listSanPham.value.some(item => !item.idSP || item.quantity <= 0)) {
      message.error("Dữ liệu sản phẩm thanh toán không hợp lệ. Vui lòng chọn lại sản phẩm từ giỏ hàng!");
      await router.push("/gio-hang");
      return;
    }

    const selectedProvince = provinces.value.find((p) => p.ProvinceID === form.value.tinh);
    const selectedDistrict = districts.value.find((d) => d.DistrictID === form.value.huyen);
    const selectedWard = wards.value.find((w) => w.WardCode === form.value.phuong);

    const ListSP = listSanPham.value.map(item => ({
      id: String(item.idSP),
      quantity: item.quantity,
    }));

    const shippingAddress = [
      form.value.diaChi,
      selectedWard?.WardName,
      selectedDistrict?.DistrictName,
      selectedProvince?.ProvinceName,
    ].filter(Boolean).join(", ");

    const orderData = {
      hoTen: form.value.hoTen,
      soDienThoai: form.value.soDienThoai,
      email: form.value.email,
      address: shippingAddress,
      diaChi: shippingAddress,
      ghiChu: form.value.ghiChu,
      maGiamGia: form.value.maGiamGia,
      hinhThucThanhToan: form.value.thanhToan,
      tongTien: tongTien.value,
      phiShip: phiShip.value,
      giamGia: giamGia.value,
      tongCong: tongCong.value,
      product: ListSP,
      sanPham: ListSP,
      Customer: idKH?.userId || "khách lẻ",
      KhachHang: idKH?.userId || "khách lẻ",
      vnp_TmnCode: VNPAY_CONFIG.vnp_TmnCode,
      vnp_ReturnUrl: VNPAY_CONFIG.vnp_ReturnUrl,
    };

    console.log("Dữ liệu gửi đi:", JSON.stringify(orderData, null, 2));

    // Reuse the same key for retries of this checkout attempt so a timeout
    // cannot create a second COD order. A new page/checkout gets a new key.
    const idempotencyKey = form.value.thanhToan === "TIEN_MAT"
      ? checkoutIdempotencyKey.value
      : undefined;
    const response = await ThanhToan(orderData, idempotencyKey);

    console.log("Phản hồi từ API ThanhToan:", response);

    if (!response || Object.keys(response).length === 0) {
      message.error("sản phẩm đã hết hàng, vui lòng kiểm tra lại giỏ hàng!");
      router.push({
        name: 'trang-chu'
      });
      return;
    }
    
    if (form.value.thanhToan === "VNPAY") {
        if (response && response.paymentUrl) {

          window.location.href = response.paymentUrl;
          localStorageAction.remove(CHECKOUT_STORAGE_KEY);
        } else {
          message.error("❌ Không thể tạo liên kết thanh toán VNPAY. Vui lòng thử lại!");
        }

    } else {
      message.success("✅ Đặt hàng thành công!");
      localStorageAction.remove(CHECKOUT_STORAGE_KEY);
      router.push({ name: "thanh-toan-thanh-cong" });
    }
  } catch (err: any) {
    message.error(err?.response?.data?.message || "❌ Có lỗi xảy ra trong quá trình đặt hàng. Vui lòng thử lại!");
    console.error("Lỗi khi xử lý thanh toán:", err);
  } finally {
    loadingCheckout.value = false;
  }
};
</script>

<style scoped>
.text-decoration-line-through {
  text-decoration: line-through;
}

.text-success {
  color: #28a745;
  font-style: italic;
}

.voucher-option {
  width: 100%;
}

.voucher-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  width: 200px;
  /* luôn full ngang */
  min-height: 120px;
  /* chiều cao bằng nhau */
  padding: 12px 14px;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  background: #fff;
  position: relative;
  transition: all 0.2s ease;
  box-sizing: border-box;
  /* để padding không phá layout */
}

.voucher-card::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  width: 6px;
  height: 100%;
  background: #ff5722;
  border-radius: 6px 0 0 6px;
}

.voucher-card:hover {
  border-color: #ff5722;
  box-shadow: 0 2px 6px rgba(255, 87, 34, 0.2);
}

.voucher-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  /* tránh tràn */
}

.voucher-amount {
  font-size: 16px;
  font-weight: 700;
  color: #d0021b;
}

.voucher-badge {
  background: #ff5722;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
}

.voucher-condition {
  font-size: 13px;
  color: #555;
  margin: 4px 0;
}

.voucher-details {
  margin: 4px 0;
}

.voucher-method {
  padding: 2px 6px;
  border: 1px solid #ff5722;
  color: #ff5722;
  border-radius: 3px;
  font-size: 12px;
  display: inline-block;
}

.voucher-footer {
  font-size: 12px;
  color: #555;
}

.voucher-expiry {
  font-weight: 500;
}
</style>
