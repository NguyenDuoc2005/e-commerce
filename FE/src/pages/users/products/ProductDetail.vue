<template>
  <div class="container py-3">
    <div class="row align-items-center">
      <BreadCrumbUser :routes="breadcrumbRoutes" title="Thông tin chi tiết" />
    </div>
  </div>

  <div class="container" v-if="product">
    <div class="row gx-5 align-items-start">
      <!-- Cột trái -->
      <div class="col-md-6 d-flex flex-column gap-4">
        <div
          class="bg-white border rounded shadow-sm p-3 d-flex justify-content-center align-items-center product-img-box position-relative"
        >
          <img
            :src="currentVariant?.hinhAnh || 'https://via.placeholder.com/400'"
            class="product-img-main"
            alt="Ảnh sản phẩm"
          />
          <!-- Ribbon giảm giá góc chéo -->
          <template v-if="currentVariant?.dotGiamGia">
            <div class="sale-ribbon-main shadow">
              <span class="sale-percent"
                >-{{ Math.round(currentVariant.dotGiamGia.phanTramGiam) }}%</span
              >
              <span class="sale-text">SALE</span>
            </div>
          </template>
        </div>

        <div class="border-top pt-4">
          <h5 class="fw-bold mb-3">Mô tả sản phẩm</h5>
          <p class="text-muted" style="white-space: pre-line">{{ displayedDescription }}</p>
          <button
            v-if="hasMoreDescription"
            class="btn btn-sm btn-outline-secondary"
            @click="showFullDescription = !showFullDescription"
          >
            {{ showFullDescription ? "Thu gọn" : "Xem thêm" }}
            <i :class="'ri-arrow-' + (showFullDescription ? 'up' : 'down') + '-s-line ms-1'"></i>
          </button>
        </div>
      </div>

      <!-- Cột phải -->
      <div class="col-md-6">
        <div class="bg-white border rounded shadow-sm p-3 d-flex flex-column gap-2">
          <h5 class="fw-bold mb-1">{{ product.tenSanPham }}</h5>
          <div class="text-muted small">
            Thương hiệu: <strong>{{ product.thuongHieu?.tenThuongHieu || "-" }}</strong>
          </div>

          <!-- Giá -->
          <div class="text-danger fw-bold fs-6 mb-2">
            <template v-if="currentVariant?.dotGiamGia">
              <del class="text-muted me-2">
                {{
                  currentVariant?.dotGiamGia?.giaTruoc?.toLocaleString("vi-VN") ||
                  currentVariant?.giaBan?.toLocaleString("vi-VN")
                }}
                ₫
              </del>
              {{
                (currentVariant?.dotGiamGia?.giaSau || currentVariant?.giaBan)?.toLocaleString(
                  "vi-VN"
                )
              }}
              ₫
              <span class="badge bg-success ms-2"
                >-{{ currentVariant?.dotGiamGia?.phanTramGiam }}%</span
              >
            </template>
            <template v-else> {{ currentVariant?.giaBan?.toLocaleString("vi-VN") }} ₫ </template>
          </div>

          <!-- Màu sắc -->
          <div>
            <label class="form-label small fw-semibold mb-1">Màu sắc</label>
            <div class="d-flex flex-wrap gap-2">
              <div
                v-for="color in uniqueColors"
                :key="color.id"
                class="d-flex flex-column align-items-center"
                style="cursor: pointer; width: 50px"
                @click="handleChooseColor(color)"
              >
                <div
                  class="shadow-sm"
                  :style="{
                    backgroundColor: color.maMau,
                    width: '30px',
                    height: '30px',
                    borderRadius: '50%',
                    border: colorSelected?.id === color.id ? '2px solid #0d6efd' : '1px solid #ccc',
                    transition: 'border 0.2s ease',
                  }"
                ></div>
                <small class="mt-1 text-muted text-center" style="font-size: 0.7rem">
                  {{ color.tenMauSac }}
                </small>
              </div>
            </div>
          </div>

          <!-- Kích thước -->
          <div>
            <label class="form-label small fw-semibold mb-1">Kích cỡ</label>
            <div class="d-flex flex-wrap gap-2">
              <span
                v-for="size in filteredSizes"
                :key="size.id"
                class="px-2 py-1 border rounded text-center"
                :class="sizeSelected?.id === size.id ? 'bg-dark text-white' : 'bg-light text-dark'"
                style="min-width: 40px; font-size: 0.85rem; cursor: pointer"
                @click="handleChooseSize(size)"
              >
                {{ size.tenKichCo }}
              </span>
            </div>
          </div>

          <!-- Số lượng -->
          <div class="d-flex justify-content-between align-items-center">
            <label class="form-label mb-0 small fw-semibold">Số lượng</label>
            <input
              type="number"
              class="form-control form-control-sm w-25 text-end"
              v-model="cart.quantity"
              :max="currentVariant?.soLuong"
              min="1"
            />
            <span class="text-muted small ms-2">(Tồn: {{ currentVariant?.soLuong || 0 }})</span>
          </div>

          <!-- Tạm tính -->
          <div class="d-flex justify-content-between align-items-center border-top pt-2 mt-2">
            <span class="fw-semibold small">Tạm tính:</span>
            <span class="fw-bold text-primary small">
              {{ ((displayedPrice || 0) * (cart.quantity || 1)).toLocaleString("vi-VN") }} ₫
            </span>
          </div>

          <div v-if="errValidate.cart" class="text-danger small">{{ errValidate.cart }}</div>

          <div class="d-grid gap-1 mt-2">
            <button class="btn btn-outline-primary btn-sm fw-semibold" @click="addToCart">
              <i class="ri-shopping-cart-line me-1"></i> Giỏ hàng
            </button>
            <button class="btn btn-primary btn-sm fw-semibold" @click="buyNow">Mua ngay</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import BreadCrumbUser from "@/components/ui/Breadcrumbs/BreadCrumbUser.vue";
import { GetSanPhamChiTietById } from "@/services/api/permitall/sanphamchitiet/pmsanphamchitiet.api";
import { createCartDetail } from "@/services/api/permitall/cart/cart";
import { localStorageAction } from "@/utils/storage";
import { USER_INFO_STORAGE_KEY, CHECKOUT_STORAGE_KEY,CART_STORAGE_KEY  } from "@/constants/storageKey";
import { toast } from "vue3-toastify";

const router = useRouter();
const route = useRoute();
const idSanPham = route.params.idsp?.toString() || "";
const colorIdParam = route.query.colorId?.toString() || null;
const sizeIdParam = route.query.sizeId?.toString() || null;

const product = ref<any>(null);
const colorSelected = ref<any>(null);
const sizeSelected = ref<any>(null);
const cart = ref({ quantity: 1 });
const showFullDescription = ref(false);

const breadcrumbRoutes = [
  { name: "Trang chủ", path: "/" },
  { name: "Sản phẩm", path: "/san-pham" },
  { name: "Chi tiết sản phẩm", path: "/chi-tiet-san-pham" },
];

onMounted(async () => {
  await loadSanPhamChiTiet();
  pickVariantFromParamOrDefault();
});

/** Hàm gọi API */
async function loadSanPhamChiTiet() {
  if (!idSanPham) return;
  const res = await GetSanPhamChiTietById({ idSanPham });
  product.value = res.data;
}

/** Hàm chọn variant theo param nếu có, không thì lấy mặc định */
function pickVariantFromParamOrDefault() {
  const variants = product.value?.chiTietSanPham || [];
  let chosenVariant = null;

  if (colorIdParam && sizeIdParam) {
    chosenVariant = variants.find(
      (ct) => ct.mauSac.id === colorIdParam && ct.kichCo.id === sizeIdParam
    );
  } else if (colorIdParam) {
    chosenVariant = variants.find((ct) => ct.mauSac.id === colorIdParam);
  }

  if (chosenVariant) {
    colorSelected.value = chosenVariant.mauSac;
    sizeSelected.value = chosenVariant.kichCo;
  } else if (variants.length > 0) {
    setDefaultVariant();
  }
}

/** Hàm chọn variant mặc định */
function setDefaultVariant() {
  const variants = product.value?.chiTietSanPham || [];
  if (variants.length > 0) {
    colorSelected.value = variants[0].mauSac;
    sizeSelected.value = variants[0].kichCo;
  }
}

/** Lấy danh sách màu không trùng */
const uniqueColors = computed(() => {
  const seen = new Set();
  return (product.value?.chiTietSanPham || [])
    .map((ct) => ct.mauSac)
    .filter((color) => {
      if (seen.has(color.id)) return false;
      seen.add(color.id);
      return true;
    });
});

/** Lấy danh sách size phù hợp với màu đang chọn */
const filteredSizes = computed(() => {
  return (product.value?.chiTietSanPham || [])
    .filter((ct) => ct.mauSac.id === colorSelected.value?.id)
    .map((ct) => ct.kichCo);
});

/** Lấy variant hiện tại (đúng màu + size) */
const currentVariant = computed(() => {
  return (product.value?.chiTietSanPham || []).find(
    (ct) => ct.mauSac.id === colorSelected.value?.id && ct.kichCo.id === sizeSelected.value?.id
  );
});

/** Giá hiển thị */
const displayedPrice = computed(() => {
  if (currentVariant.value?.dotGiamGia?.giaSau) return currentVariant.value.dotGiamGia.giaSau;
  return currentVariant.value?.giaBan;
});

/** Mô tả sản phẩm rút gọn */
const displayedDescription = computed(() =>
  showFullDescription.value
    ? product.value?.moTa || ""
    : (product.value?.moTa || "").split("\n").slice(0, 2).join("\n")
);

const hasMoreDescription = computed(() => (product.value?.moTa || "").split("\n").length > 2);

const errValidate = computed(() => {
  if (!currentVariant.value) return { cart: "Vui lòng chọn đủ màu và kích cỡ" };
  if (cart.value.quantity > currentVariant.value.soLuong) return { cart: "Vượt quá tồn kho" };
  if (cart.value.quantity < 1) return { cart: "Số lượng không hợp lệ" };
  return { cart: "" };
});

function handleChooseColor(color: any) {
  colorSelected.value = color;
  // Nếu size hiện tại không tồn tại với màu này, chọn size đầu tiên có thể chọn
  const sizesWithColor = (product.value?.chiTietSanPham || [])
    .filter((ct) => ct.mauSac.id === color.id)
    .map((ct) => ct.kichCo);
  if (!sizesWithColor.some((sz) => sz.id === sizeSelected.value?.id)) {
    sizeSelected.value = sizesWithColor[0] || null;
  }
}

function handleChooseSize(size: any) {
  sizeSelected.value = size;
}

function getCartData() {
  return {
    idSanPham: product.value?.id,
    tenSanPham: product.value?.tenSanPham,
    moTa: product.value?.moTa,
    hinhAnh: currentVariant.value?.hinhAnh,
    thuongHieu: product.value?.thuongHieu,
    xuatXu: product.value?.xuatXu,
    chatLieu: product.value?.chatLieu,
    danhMuc: product.value?.danhMuc,
    loaiDe: product.value?.loaiDe,
    idChiTietSanPham: currentVariant.value?.id,
    mauSac: currentVariant.value?.mauSac,
    kichCo: currentVariant.value?.kichCo,
    giaBan: currentVariant.value?.giaBan,
    soLuongTrongKho: currentVariant.value?.soLuong,
    dotGiamGia: currentVariant.value?.dotGiamGia,
    soLuongMua: cart.value.quantity,
  };
}

const idUser = localStorageAction.get(USER_INFO_STORAGE_KEY);

async function addToCart() {
  if (errValidate.value.cart) {
    toast.error(errValidate.value.cart);
    return;
  }

  const cartItem = getCartData();

  // Kiểm tra dữ liệu cần thiết
  if (!cartItem.idChiTietSanPham || !cartItem.giaBan || !cartItem.soLuongMua) {
    toast.error("Không thể thêm sản phẩm vào giỏ hàng. Thiếu thông tin sản phẩm.");
    return;
  }

  // Kiểm tra trạng thái đăng nhập
  if (idUser?.userId) {
    // Người dùng đã đăng nhập -> Gọi API để thêm vào giỏ hàng
    try {
      const dataToSend = {
        idKhachHang: idUser.userId,
        idSPCT: cartItem.idChiTietSanPham,
        price: cartItem.giaBan.toString(),
        quantity: cartItem.soLuongMua.toString(),
      };

      console.log("Dữ liệu gửi đến API createCartDetail:", dataToSend);

      const res = await createCartDetail(dataToSend);

      if (res.message === "Số lượng sản phẩm trong giỏ hàng đã vượt quá số lượng sản phẩm") {
        toast.warning(res.message);
        return;
      }

      toast.success(res.message);
      // Gửi sự kiện cập nhật giỏ hàng
      dispatchCartUpdate();
    } catch (error: any) {

    }
  } else {
    // Người dùng chưa đăng nhập -> Lưu vào localStorage
    try {
      let tempCart = localStorageAction.get(CART_STORAGE_KEY) || [];

      if (!Array.isArray(tempCart)) {
        tempCart = [];
      }

      const existingItemIndex = tempCart.findIndex(
        (item: any) => item.idChiTietSanPham === cartItem.idChiTietSanPham
      );

      if (existingItemIndex !== -1) {
        tempCart[existingItemIndex].soLuongMua += cartItem.soLuongMua;
        if (tempCart[existingItemIndex].soLuongMua > cartItem.soLuongTrongKho) {
          toast.warning("Số lượng vượt quá tồn kho!");
          return;
        }
      } else {
        tempCart.push(cartItem);
      }

      // Lưu lại giỏ hàng tạm vào localStorage
      localStorageAction.set(CART_STORAGE_KEY, tempCart);
      toast.success("Đã thêm sản phẩm vào giỏ hàng!");
      // Gửi sự kiện cập nhật giỏ hàng
      dispatchCartUpdate();
    } catch (error) {
      console.error("Lỗi khi lưu giỏ hàng tạm:", error);
      toast.error("Có lỗi xảy ra khi thêm vào giỏ hàng tạm.");
    }
  }
}

// Hàm gửi sự kiện tùy chỉnh
const dispatchCartUpdate = () => {
  window.dispatchEvent(new Event('cartUpdated'));
}

function buyNow() {
  if (errValidate.value.cart) {
    toast.error(errValidate.value.cart);
    return;
  }

  const buyItem = getCartData();

  // Kiểm tra dữ liệu trước khi lưu
  if (!buyItem.idChiTietSanPham || !buyItem.tenSanPham || !buyItem.soLuongMua) {
    toast.error("Dữ liệu sản phẩm không hợp lệ. Vui lòng thử lại!");
    return;
  }

  // Chuẩn bị dữ liệu để lưu vào CHECKOUT_STORAGE_KEY
  const checkoutItem = {
    id: buyItem.idChiTietSanPham, // Sử dụng idChiTietSanPham làm ID duy nhất
    name: buyItem.tenSanPham,
    originalPrice: buyItem.giaBan,
    discountPrice: buyItem.dotGiamGia?.giaSau || buyItem.giaBan,
    quantity: buyItem.soLuongMua,
    imageUrl: buyItem.hinhAnh,
    color: buyItem.mauSac?.tenMauSac || "Không xác định",
    size: buyItem.kichCo?.tenKichCo || "Không xác định",
    idSP: buyItem.idChiTietSanPham,
    soLuongTrongKho: buyItem.soLuongTrongKho,
  };

  try {
    // Lưu vào CHECKOUT_STORAGE_KEY
    localStorageAction.set(CHECKOUT_STORAGE_KEY, [checkoutItem]);
    console.log("Dữ liệu gửi đến trang thanh toán:", [checkoutItem]);

    // Chuyển hướng sau khi lưu thành công
    router.push("/thanh-toan");
  } catch (error) {
    console.error("Lỗi khi lưu dữ liệu thanh toán:", error);
    toast.error("Có lỗi xảy ra khi chuẩn bị thanh toán. Vui lòng thử lại!");
  }
}
</script>

<style scoped>
.product-img-box {
  min-height: 410px;
  height: 410px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  position: relative;
}
.product-img-main {
  width: 100%;
  max-width: 400px;
  height: 390px;
  max-height: 390px;
  object-fit: contain;
  background: #f7f7f7;
  border-radius: 18px;
  display: block;
  box-shadow: 0 3px 16px 0 rgba(60, 60, 60, 0.1);
  margin: 0 auto;
  transition: box-shadow 0.18s;
}

/* Ribbon sale góc trên trái */
.sale-ribbon-main {
  position: absolute;
  top: 16px;
  left: -44px;
  width: 152px;
  text-align: center;
  background: linear-gradient(92deg, #ff7402 70%, #ffc94e 100%);
  color: #fff;
  font-weight: bold;
  font-size: 1.05rem;
  padding: 6px 0;
  border-radius: 8px;
  box-shadow: 0 3px 12px rgba(255, 102, 0, 0.14), 0 1.5px 6px rgba(0, 0, 0, 0.06);
  letter-spacing: 1.1px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.09);
  transform: rotate(-24deg);
  z-index: 4;
  display: flex;
  flex-direction: column;
  align-items: center;
  pointer-events: none;
  opacity: 0.97;
  border: 1.5px solid #fff6e0;
  filter: drop-shadow(0 1.5px 3px #ffa40025);
}
.sale-percent {
  font-size: 1.18rem;
  font-weight: 900;
  color: #fff8dc;
  letter-spacing: 2px;
  line-height: 1.05;
  text-shadow: 0 1.5px 2px #ff7300b3, 0 1.5px 1.5px #ffb30070;
}
.sale-text {
  font-size: 1.01rem;
  color: #fff;
  font-weight: 700;
  letter-spacing: 2.3px;
  margin-top: -1.5px;
  text-shadow: 0 1px 2px #f4730a8c, 0 0.5px 2.5px #fff;
}

@media (max-width: 767px) {
  .product-img-box {
    min-height: 260px;
    height: 260px;
  }
  .product-img-main {
    height: 250px;
    max-height: 250px;
    border-radius: 12px;
  }
  .sale-ribbon-main {
    top: 8px;
    left: -34px;
    width: 112px;
    padding: 4px 0;
    font-size: 0.95rem;
    border-radius: 7px;
  }
  .sale-percent {
    font-size: 1.02rem;
  }
  .sale-text {
    font-size: 0.93rem;
  }
}
</style>