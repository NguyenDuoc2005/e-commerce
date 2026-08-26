<template>
  <div class="container py-3">
    <div class="row align-items-center">
      <BreadCrumbUser :routes="breadcrumbRoutes" title="Giỏ hàng của bạn" />
    </div>
  </div>

  <div class="container">
    <div class="d-none d-md-flex border-bottom pb-2 fw-semibold text-muted">
      <div class="form-check me-3">
        <input class="form-check-input" type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" />
      </div>
      <div class="flex-grow-1">Sản phẩm</div>
      <div class="text-center" style="width: 130px">Đơn giá</div>
      <div class="text-center" style="width: 100px">Số lượng</div>
      <div class="text-center" style="width: 130px">Thành tiền</div>
      <div class="text-center" style="width: 80px">Thao tác</div>
    </div>

    <div v-for="group in cartShopGroups" :key="group.sellerId" class="cart-shop border-bottom">
      <div class="d-flex align-items-center gap-2 py-3 px-2 bg-light">
        <input class="form-check-input mt-0" type="checkbox" :checked="isShopSelected(group)" @change="toggleSelectShop(group)" />
        <RouterLink v-if="group.sellerSlug" :to="`/shop/${group.sellerSlug}`" class="fw-semibold text-dark text-decoration-none">
          {{ group.shopName }}
        </RouterLink>
        <span v-else class="fw-semibold">{{ group.shopName }}</span>
        <span class="text-muted small">({{ group.items.length }} sản phẩm)</span>
        <span class="ms-auto small text-muted">Tạm tính shop: {{ shopSubtotal(group).toLocaleString("vi-VN") }}đ</span>
      </div>

      <div v-for="item in group.items" :key="item.id" class="d-flex flex-column flex-md-row align-items-md-center py-3 gap-3">
        <div class="form-check me-md-3 align-self-start">
          <input class="form-check-input" type="checkbox" :value="item.id" v-model="selectedIds" />
        </div>

        <div class="d-flex flex-grow-1 gap-3">
          <img :src="item.imageUrl || '/images/logo.jpg'" alt="Ảnh giày" class="rounded" style="width: 80px; height: 80px; object-fit: cover" />
          <div>
            <div class="fw-semibold">{{ item.name }}</div>
            <div class="text-muted small">Phân loại: Màu {{ item.color }} / Size {{ item.size }}</div>
          </div>
        </div>

        <div class="text-md-center mt-2 mt-md-0" style="width: 130px">
          <div v-if="item.discountPrice < item.originalPrice">
            <div class="text-danger fw-bold">{{ item.discountPrice.toLocaleString("vi-VN") }}đ</div>
            <div class="text-muted text-decoration-line-through small">{{ item.originalPrice.toLocaleString("vi-VN") }}đ</div>
          </div>
          <div v-else>
            <div class="fw-bold">{{ item.originalPrice.toLocaleString("vi-VN") }}đ</div>
          </div>
        </div>

        <div class="text-center mt-2 mt-md-0" style="width: 100px">
          <div class="d-flex justify-content-center align-items-center gap-2">
            <button class="btn btn-sm btn-outline-secondary px-2" @click="decreaseQuantity(item)">-</button>
            <span>{{ item.quantity }}</span>
            <button class="btn btn-sm btn-outline-secondary px-2" @click="increaseQuantity(item)">+</button>
          </div>
        </div>

        <div class="text-center fw-bold text-danger mt-2 mt-md-0" style="width: 130px">
          {{ (getPrice(item) * item.quantity).toLocaleString("vi-VN") }}đ
        </div>

        <div class="text-center mt-2 mt-md-0" style="width: 80px">
          <button class="btn btn-link text-danger p-0" @click="removeItem(item)">Xoá</button>
        </div>
      </div>
    </div>

    <div class="mt-4" v-if="cartItems.length > 0">
      <div class="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-3">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" />
          <label class="form-check-label">Chọn tất cả ({{ cartItems.length }})</label>
        </div>
        <div class="text-end w-100 w-md-auto">
          <div class="mb-2">
            Tổng tiền hàng ({{ totalSelectedQuantity }} sản phẩm):
            <strong class="text-danger fs-5">{{ totalSelectedPrice.toLocaleString("vi-VN") }}đ</strong>
          </div>
          <button class="btn btn-danger px-4 fw-semibold" @click="checkout" :disabled="selectedIds.length === 0" v-if="selectedIds.length > 0">
            Mua hàng
          </button>
          <div class="text-muted fst-italic" v-else>Chưa có sản phẩm nào được chọn</div>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-5 text-muted">
      <i class="bi bi-cart-x display-4 text-danger mb-3"></i>
      <div class="fw-semibold fs-5">Giỏ hàng của bạn đang trống.</div>
      <p class="mt-2 mb-4">Hãy quay lại cửa hàng để chọn những đôi giày yêu thích.</p>
      <RouterLink to="/" class="btn btn-outline-primary px-4 fw-semibold">
        <i class="bi bi-arrow-left me-2"></i> Tiếp tục mua sắm
      </RouterLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import BreadCrumbUser from "@/components/ui/Breadcrumbs/BreadCrumbUser.vue";
import { USER_INFO_STORAGE_KEY, CART_STORAGE_KEY, CHECKOUT_STORAGE_KEY } from "@/constants/storageKey";
import { deleteCartDetail, getAllCart, createCartDetail, type requestCart } from "@/services/api/buyer/cart/cart";
import { localStorageAction } from "@/utils/storage";
import { ref, computed, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";

const breadcrumbRoutes = [
  { name: "Trang chủ", path: "/" },
  { name: "Sản phẩm", path: "/san-pham" },
  { name: "Giỏ hàng" },
];

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
  idChiTietSanPham?: string;
  soLuongTrongKho?: number;
  sellerId?: string;
  shopName?: string;
  sellerSlug?: string;
}

interface CartShopGroup {
  sellerId: string;
  shopName: string;
  sellerSlug?: string;
  items: CartItem[];
}

const cartItems = ref<CartItem[]>([]);
const selectedIds = ref<string[]>([]);
const idUser = localStorageAction.get(USER_INFO_STORAGE_KEY);
const requestCart = reactive({
  idKhachHang: idUser?.userId || "",
});

const dispatchCartUpdate = () => {
  window.dispatchEvent(new Event("cartUpdated"));
};

const extractCartRows = (payload: any) => {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.items)) return payload.items;
  if (Array.isArray(payload?.data)) return payload.data;
  return [];
};

const normalizeCartItem = (detail: any): CartItem => {
  const spct = detail.productVariant || detail.sanPhamChiTiet || {};
  const product = spct.product || spct.sanPham || {};
  const sellerId = detail.sellerId || spct.sellerId || "UNKNOWN_SELLER";
  const price = Number(spct.giaBan || spct.salePrice || detail.price || 0);
  return {
    id: detail.id,
    idSP: spct.id || detail.productVariantId || detail.sanPhamChiTietId,
    name: product.name || spct.name || spct.tenProduct || spct.tenSanPham || spct.ten || "Sản phẩm",
    originalPrice: price,
    discountPrice: Number(spct.dotGiamGia?.giaSau || spct.discountPrice || spct.salePrice || price),
    quantity: Number(detail.quantity || 1),
    imageUrl: spct.imageUrl || spct.anh || spct.hinhAnh || "",
    color: spct.colorName || spct.tenColor || spct.tenMau || spct.mauSac?.ten || spct.tenMauSac || spct.mau || "-",
    size: spct.sizeName || spct.tenSize || spct.kichThuoc || spct.kichCo?.ten || spct.tenKichCo || "-",
    idChiTietSanPham: spct.id || detail.productVariantId || detail.sanPhamChiTietId,
    soLuongTrongKho: Number(spct.quantity || spct.soLuong || 0),
    sellerId,
    shopName: detail.shopName || "Shop",
    sellerSlug: detail.sellerSlug,
  };
};

const getAllProductByCart = async () => {
  if (!idUser?.userId) return;
  const param: requestCart = { idUser: idUser.userId };
  try {
    const res = await getAllCart(param);
    cartItems.value = extractCartRows(res.data).map(normalizeCartItem);
  } catch (error) {
    console.error("Lỗi khi lấy giỏ hàng từ server:", error);
    toast.error("Không thể tải giỏ hàng từ server.");
  }
};

const getTempCart = () => {
  const tempCart = localStorageAction.get(CART_STORAGE_KEY) || [];
  if (!Array.isArray(tempCart)) return [];
  return tempCart.map((item: any, index: number) => ({
    id: `temp_${index}_${item.idChiTietSanPham}`,
    idSP: item.idChiTietSanPham,
    name: item.tenSanPham,
    originalPrice: Number(item.giaBan || 0),
    discountPrice: Number(item.dotGiamGia?.giaSau || item.giaBan || 0),
    quantity: Number(item.soLuongMua || 1),
    imageUrl: item.hinhAnh,
    color: item.mauSac?.tenMauSac || "-",
    size: item.kichCo?.tenKichCo || "-",
    idChiTietSanPham: item.idChiTietSanPham,
    soLuongTrongKho: Number(item.soLuongTrongKho || 0),
    sellerId: item.sellerId || "LOCAL_CART",
    shopName: item.shopName || "Sản phẩm chưa đăng nhập",
    sellerSlug: item.sellerSlug,
  }));
};

const syncTempCart = async () => {
  if (!idUser?.userId) return;
  const tempCart = localStorageAction.get(CART_STORAGE_KEY) || [];
  if (!Array.isArray(tempCart) || tempCart.length === 0) return;

  try {
    for (const item of tempCart) {
      const dataToSend = {
        idKhachHang: idUser.userId,
        idSPCT: item.idChiTietSanPham,
        price: String(item.giaBan || 0),
        quantity: String(item.soLuongMua || 1),
      };
      const res = await createCartDetail(dataToSend);
      if (res.message?.includes("vuot qua") || res.message?.includes("vượt quá")) {
        toast.warning(res.message);
      }
    }
    localStorageAction.remove(CART_STORAGE_KEY);
    await getAllProductByCart();
    dispatchCartUpdate();
  } catch (error) {
    console.error("Lỗi khi đồng bộ giỏ hàng:", error);
    toast.error("Có lỗi khi đồng bộ giỏ hàng.");
  }
};

onMounted(async () => {
  if (idUser?.userId) {
    requestCart.idKhachHang = idUser.userId;
    await syncTempCart();
    await getAllProductByCart();
  } else {
    cartItems.value = getTempCart();
  }
});

const isAllSelected = computed(() => cartItems.value.length > 0 && selectedIds.value.length === cartItems.value.length);

const toggleSelectAll = () => {
  selectedIds.value = isAllSelected.value ? [] : cartItems.value.map((item) => item.id);
};

const cartShopGroups = computed<CartShopGroup[]>(() => {
  const grouped = new Map<string, CartShopGroup>();
  for (const item of cartItems.value) {
    const sellerId = item.sellerId || "UNKNOWN_SELLER";
    if (!grouped.has(sellerId)) {
      grouped.set(sellerId, {
        sellerId,
        shopName: item.shopName || "Shop",
        sellerSlug: item.sellerSlug,
        items: [],
      });
    }
    grouped.get(sellerId)!.items.push(item);
  }
  return Array.from(grouped.values());
});

const isShopSelected = (group: CartShopGroup) => {
  return group.items.length > 0 && group.items.every((item) => selectedIds.value.includes(item.id));
};

const toggleSelectShop = (group: CartShopGroup) => {
  const ids = group.items.map((item) => item.id);
  if (isShopSelected(group)) {
    selectedIds.value = selectedIds.value.filter((id) => !ids.includes(id));
    return;
  }
  selectedIds.value = Array.from(new Set([...selectedIds.value, ...ids]));
};

const getPrice = (item: CartItem) => {
  return item.discountPrice < item.originalPrice ? item.discountPrice : item.originalPrice;
};

const shopSubtotal = (group: CartShopGroup) => {
  return group.items.reduce((sum, item) => sum + getPrice(item) * item.quantity, 0);
};

const increaseQuantity = async (item: CartItem) => {
  if (item.soLuongTrongKho && item.quantity >= item.soLuongTrongKho) {
    toast.warning("Số lượng vượt quá tồn kho.");
    return;
  }
  item.quantity++;
  if (!idUser?.userId) {
    updateTempCart(item);
  }
  dispatchCartUpdate();
};

const decreaseQuantity = async (item: CartItem) => {
  if (item.quantity > 1) {
    item.quantity--;
    if (!idUser?.userId) {
      updateTempCart(item);
    }
    dispatchCartUpdate();
  }
};

const updateTempCart = (updatedItem: CartItem) => {
  let tempCart = localStorageAction.get(CART_STORAGE_KEY) || [];
  if (!Array.isArray(tempCart)) tempCart = [];
  const index = tempCart.findIndex((item: any) => item.idChiTietSanPham === updatedItem.idChiTietSanPham);
  if (index !== -1) {
    tempCart[index].soLuongMua = updatedItem.quantity;
  } else {
    tempCart.push({
      idChiTietSanPham: updatedItem.idChiTietSanPham,
      idSanPham: updatedItem.idSP,
      tenSanPham: updatedItem.name,
      giaBan: updatedItem.originalPrice,
      dotGiamGia: updatedItem.discountPrice < updatedItem.originalPrice ? { giaSau: updatedItem.discountPrice } : null,
      soLuongMua: updatedItem.quantity,
      hinhAnh: updatedItem.imageUrl,
      mauSac: { tenMauSac: updatedItem.color },
      kichCo: { tenKichCo: updatedItem.size },
      soLuongTrongKho: updatedItem.soLuongTrongKho,
      sellerId: updatedItem.sellerId,
      shopName: updatedItem.shopName,
      sellerSlug: updatedItem.sellerSlug,
    });
  }
  localStorageAction.set(CART_STORAGE_KEY, tempCart);
};

const removeItem = async (item: CartItem) => {
  if (idUser?.userId && !item.id.startsWith("temp_")) {
    try {
      await deleteCartDetail(item.id);
      toast.success("Đã xoá sản phẩm khỏi giỏ hàng.");
    } catch (error) {
      console.error("Lỗi khi xoá sản phẩm:", error);
      toast.error("Không thể xoá sản phẩm.");
    }
  } else {
    let tempCart = localStorageAction.get(CART_STORAGE_KEY) || [];
    tempCart = Array.isArray(tempCart)
      ? tempCart.filter((tempItem: any) => tempItem.idChiTietSanPham !== item.idChiTietSanPham)
      : [];
    localStorageAction.set(CART_STORAGE_KEY, tempCart);
    toast.success("Đã xoá sản phẩm khỏi giỏ hàng tạm.");
  }
  cartItems.value = cartItems.value.filter((i) => i.id !== item.id);
  selectedIds.value = selectedIds.value.filter((id) => id !== item.id);
  dispatchCartUpdate();
};

const totalSelectedPrice = computed(() => {
  return cartItems.value
    .filter((item) => selectedIds.value.includes(item.id))
    .reduce((sum, item) => sum + getPrice(item) * item.quantity, 0);
});

const totalSelectedQuantity = computed(() => {
  return cartItems.value
    .filter((item) => selectedIds.value.includes(item.id))
    .reduce((sum, item) => sum + item.quantity, 0);
});

const router = useRouter();

const checkout = () => {
  if (selectedIds.value.length === 0) {
    toast.error("Vui lòng chọn ít nhất một sản phẩm để thanh toán.");
    return;
  }

  const selectedCartItems = cartItems.value.filter((item) => selectedIds.value.includes(item.id));
  localStorageAction.set(CHECKOUT_STORAGE_KEY, selectedCartItems);
  if (!idUser?.userId) {
    const tempCart = localStorageAction.get(CART_STORAGE_KEY) || [];
    const updatedTempCart = Array.isArray(tempCart)
      ? tempCart.filter((item: any) => !selectedCartItems.some((cartItem) => cartItem.idChiTietSanPham === item.idChiTietSanPham))
      : [];
    localStorageAction.set(CART_STORAGE_KEY, updatedTempCart);
    dispatchCartUpdate();
  }
  router.push("/thanh-toan");
};
</script>

<style scoped>
.text-decoration-line-through {
  text-decoration: line-through;
}

.cart-shop + .cart-shop {
  margin-top: 12px;
}
</style>
