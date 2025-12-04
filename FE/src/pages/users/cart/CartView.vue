<template>
  <div class="container py-3">
    <div class="row align-items-center">
      <BreadCrumbUser :routes="breadcrumbRoutes" title="Giỏ hàng của bạn" />
    </div>
  </div>
  <div class="container">
    <div class="d-none d-md-flex border-bottom pb-2 fw-semibold text-muted">
      <div class="form-check me-3">
        <input
          class="form-check-input"
          type="checkbox"
          :checked="isAllSelected"
          @change="toggleSelectAll"
        />
      </div>
      <div class="flex-grow-1">Sản Phẩm</div>
      <div class="text-center" style="width: 130px">Đơn Giá</div>
      <div class="text-center" style="width: 100px">Số Lượng</div>
      <div class="text-center" style="width: 130px">Thành Tiền</div>
      <div class="text-center" style="width: 80px">Thao Tác</div>
    </div>

    <div
      v-for="item in cartItems"
      :key="item.id"
      class="d-flex flex-column flex-md-row align-items-md-center border-bottom py-3 gap-3"
    >
      <div class="form-check me-md-3 align-self-start">
        <input class="form-check-input" type="checkbox" :value="item.id" v-model="selectedIds" />
      </div>
      <div class="d-flex flex-grow-1 gap-3">
        <img
          :src="item.imageUrl"
          alt="Ảnh giày"
          class="rounded"
          style="width: 80px; height: 80px; object-fit: cover"
        />
        <div>
          <div class="fw-semibold">{{ item.name }}</div>
          <div class="text-muted small">Phân loại: Màu {{ item.color }} / Size {{ item.size }}</div>
        </div>
      </div> 

      <div class="text-md-center mt-2 mt-md-0" style="width: 130px">
        <div v-if="item.discountPrice < item.originalPrice">
          <div class="text-danger fw-bold">{{ item.discountPrice.toLocaleString("vi-VN") }}₫</div>
          <div class="text-muted text-decoration-line-through small">
            {{ item.originalPrice.toLocaleString("vi-VN") }}₫
          </div>
        </div>
        <div v-else>
          <div class="fw-bold">{{ item.originalPrice.toLocaleString("vi-VN") }}₫</div>
        </div>
      </div>

      <div class="text-center mt-2 mt-md-0" style="width: 100px">
        <div class="d-flex justify-content-center align-items-center gap-2">
          <button class="btn btn-sm btn-outline-secondary px-2" @click="decreaseQuantity(item)">
            -
          </button>
          <span>{{ item.quantity }}</span>
          <button class="btn btn-sm btn-outline-secondary px-2" @click="increaseQuantity(item)">
            +
          </button>
        </div>
      </div>

      <div class="text-center fw-bold text-danger mt-2 mt-md-0" style="width: 130px">
        {{ (getPrice(item) * item.quantity).toLocaleString("vi-VN") }}₫
      </div>

      <div class="text-center mt-2 mt-md-0" style="width: 80px">
        <button class="btn btn-link text-danger p-0" @click="removeItem(item)">Xoá</button>
      </div>
    </div>

    <div class="mt-4" v-if="cartItems.length > 0">
      <div
        class="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-3"
      >
        <div class="form-check">
          <input
            class="form-check-input"
            type="checkbox"
            :checked="isAllSelected"
            @change="toggleSelectAll"
          />
          <label class="form-check-label">Chọn Tất Cả ({{ cartItems.length }})</label>
        </div>
        <div class="text-end w-100 w-md-auto">
          <div class="mb-2">
            Tổng tiền hàng ({{ totalSelectedQuantity }} sản phẩm):
            <strong class="text-danger fs-5"
              >{{ totalSelectedPrice.toLocaleString("vi-VN") }}₫</strong
            >
          </div>
          <button
            class="btn btn-danger px-4 fw-semibold"
            @click="checkout"
            :disabled="selectedIds.length === 0"
            v-if="selectedIds.length > 0"
          >
            Mua Hàng
          </button>
          <div class="text-muted fst-italic" v-else>Chưa có sản phẩm nào được chọn</div>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-5 text-muted">
      <i class="bi bi-cart-x display-4 text-danger mb-3"></i>
      <div class="fw-semibold fs-5">Giỏ hàng của bạn đang trống.</div>
      <p class="mt-2 mb-4">Hãy quay lại cửa hàng để chọn những đôi giày yêu thích nhé!</p>
      <RouterLink to="/" class="btn btn-outline-primary px-4 fw-semibold">
        <i class="bi bi-arrow-left me-2"></i> Tiếp tục mua sắm
      </RouterLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import BreadCrumbUser from "@/components/ui/Breadcrumbs/BreadCrumbUser.vue";
import { USER_INFO_STORAGE_KEY, CART_STORAGE_KEY, CHECKOUT_STORAGE_KEY } from "@/constants/storageKey";
import { deleteCartDetail, getAllCart, createCartDetail, type requestCart } from "@/services/api/permitall/cart/cart";
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
  idChiTietSanPham?: string; // Thêm để tương thích với giỏ hàng tạm
  soLuongTrongKho?: number; // Thêm để kiểm tra tồn kho
}

const cartItems = ref<CartItem[]>([]);
const selectedIds = ref<string[]>([]);
const idUser = localStorageAction.get(USER_INFO_STORAGE_KEY);
const requestCart = reactive({
  idKhachHang: idUser?.userId || "",
});

// Lấy giỏ hàng từ server
const getAllProductByCart = async () => {
  if (!idUser?.userId) return;
  const param: requestCart = { idUser: idUser.userId };
  try {
    const res = await getAllCart(param);
    cartItems.value = res.data.map((detail) => ({
      id: detail.id,
      idSP: detail.sanPhamChiTiet.id,
      name: detail.sanPhamChiTiet.sanPham.ten,
      originalPrice: detail.sanPhamChiTiet.giaBan,
      discountPrice: detail.sanPhamChiTiet.dotGiamGia?.giaSau || detail.sanPhamChiTiet.giaBan,
      quantity: detail.quantity,
      imageUrl: detail.sanPhamChiTiet.anh,
      color: detail.sanPhamChiTiet.mauSac.ten,
      size: detail.sanPhamChiTiet.kichCo.ten,
      idChiTietSanPham: detail.sanPhamChiTiet.id,
      soLuongTrongKho: detail.sanPhamChiTiet.soLuong,
    }));
  } catch (error) {
    console.error("Lỗi khi lấy giỏ hàng từ server:", error);
    toast.error("Không thể tải giỏ hàng từ server.");
  }
};

// Lấy giỏ hàng từ localStorage
const getTempCart = () => {
  const tempCart = localStorageAction.get(CART_STORAGE_KEY) || [];
  console.log("Giỏ hàng tạm từ localStorage:", tempCart);
  if (!Array.isArray(tempCart)) return [];
  return tempCart.map((item: any, index: number) => ({
    id: `temp_${index}_${item.idChiTietSanPham}`, // Tạo ID tạm để tránh trùng
    idSP: item.idChiTietSanPham,
    name: item.tenSanPham,
    originalPrice: item.giaBan,
    discountPrice: item.dotGiamGia?.giaSau || item.giaBan,
    quantity: item.soLuongMua,
    imageUrl: item.hinhAnh,
    color: item.mauSac.tenMauSac,
    size: item.kichCo.tenKichCo,
    idChiTietSanPham: item.idChiTietSanPham,
    soLuongTrongKho: item.soLuongTrongKho,
  }));
};

// Đồng bộ giỏ hàng tạm với server
const syncTempCart = async () => {
  if (!idUser?.userId) return;
  const tempCart = localStorageAction.get(CART_STORAGE_KEY) || [];
  if (!Array.isArray(tempCart) || tempCart.length === 0) return;

  try {
    for (const item of tempCart) {
      const dataToSend = {
        idKhachHang: idUser.userId,
        idSPCT: item.idChiTietSanPham,
        price: item.giaBan.toString(),
        quantity: item.soLuongMua.toString(),
      };
      const res = await createCartDetail(dataToSend);
      if (res.message.includes("vượt quá số lượng sản phẩm")) {
        toast.warning(res.message);
      }
    }
    localStorageAction.remove(CART_STORAGE_KEY); // Xóa giỏ hàng tạm sau khi đồng bộ
    await getAllProductByCart(); // Cập nhật lại giỏ hàng từ server
    // toast.success("Đã đồng bộ giỏ hàng!");
  } catch (error) {
    console.error("Lỗi khi đồng bộ giỏ hàng:", error);
    toast.error("Có lỗi khi đồng bộ giỏ hàng.");
  }
};

// Load giỏ hàng dựa trên trạng thái đăng nhập
onMounted(async () => {
  if (idUser?.userId) {
    requestCart.idKhachHang = idUser.userId;
    await syncTempCart(); // Đồng bộ giỏ hàng tạm nếu có
    await getAllProductByCart();
  } else {
    cartItems.value = getTempCart();
  }
});

const isAllSelected = computed(() => selectedIds.value.length === cartItems.value.length);

const toggleSelectAll = () => {
  selectedIds.value = isAllSelected.value ? [] : cartItems.value.map((item) => item.id);
};

const getPrice = (item: CartItem) => {
  return item.discountPrice < item.originalPrice ? item.discountPrice : item.originalPrice;
};

const increaseQuantity = async (item: CartItem) => {
  if (item.soLuongTrongKho && item.quantity >= item.soLuongTrongKho) {
    toast.warning("Số lượng vượt quá tồn kho!");
    return;
  }
  item.quantity++;
  if (!idUser?.userId) {
    updateTempCart(item);
  } else {
    // Cập nhật server nếu cần (yêu cầu API cập nhật số lượng)
  }
};

const decreaseQuantity = async (item: CartItem) => {
  if (item.quantity > 1) {
    item.quantity--;
    if (!idUser?.userId) {
      updateTempCart(item);
    } else {
      // Cập nhật server nếu cần
    }
  }
};

const updateTempCart = (updatedItem: CartItem) => {
  let tempCart = localStorageAction.get(CART_STORAGE_KEY) || [];
  if (!Array.isArray(tempCart)) tempCart = [];
  const index = tempCart.findIndex(
    (item: any) => item.idChiTietSanPham === updatedItem.idChiTietSanPham
  );
  if (index !== -1) {
    tempCart[index].soLuongMua = updatedItem.quantity;
    localStorageAction.set(CART_STORAGE_KEY, tempCart);
  } else {
    // Thêm sản phẩm mới nếu không tìm thấy
    tempCart.push({
      idChiTietSanPham: updatedItem.idChiTietSanPham,
      idSanPham: updatedItem.idSP,
      tenSanPham: updatedItem.name,
      giaBan: updatedItem.originalPrice,
      dotGiamGia: updatedItem.discountPrice < updatedItem.originalPrice
        ? { giaSau: updatedItem.discountPrice }
        : null,
      soLuongMua: updatedItem.quantity,
      hinhAnh: updatedItem.imageUrl,
      mauSac: { tenMauSac: updatedItem.color },
      kichCo: { tenKichCo: updatedItem.size },
      soLuongTrongKho: updatedItem.soLuongTrongKho,
    });
    localStorageAction.set(CART_STORAGE_KEY, tempCart);
  }
};

const removeItem = async (item: CartItem) => {
  if (idUser?.userId && !item.id.startsWith("temp_")) {
    try {
      await deleteCartDetail(item.id);
      toast.success("Đã xóa sản phẩm khỏi giỏ hàng!");
    } catch (error) {
      console.error("Lỗi khi xóa sản phẩm:", error);
      toast.error("Không thể xóa sản phẩm.");
    }
  } else {
    let tempCart = localStorageAction.get(CART_STORAGE_KEY) || [];
    tempCart = tempCart.filter(
      (tempItem: any) => tempItem.idChiTietSanPham !== item.idChiTietSanPham
    );
    localStorageAction.set(CART_STORAGE_KEY, tempCart); // Cập nhật lại localStorage
    toast.success("Đã xóa sản phẩm khỏi giỏ hàng tạm!");
  }
  cartItems.value = cartItems.value.filter((i) => i.id !== item.id);
  selectedIds.value = selectedIds.value.filter((id) => id !== item.id);
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
  localStorageAction.set(CHECKOUT_STORAGE_KEY, selectedCartItems); // Lưu vào CHECKOUT_STORAGE_KEY
  if (!idUser?.userId) {
    // Nếu không đăng nhập, xóa CART_STORAGE_KEY sau khi chuyển sang thanh toán
    const tempCart = localStorageAction.get(CART_STORAGE_KEY) || [];
    const updatedTempCart = tempCart.filter((item: any) =>
      !selectedCartItems.some(cartItem => cartItem.idChiTietSanPham === item.idChiTietSanPham)
    );
    localStorageAction.set(CART_STORAGE_KEY, updatedTempCart);
  }
  console.log("Dữ liệu gửi đến trang thanh toán:", selectedCartItems); // Debug
  router.push("/thanh-toan");
};
</script>

<style scoped>
.text-decoration-line-through {
  text-decoration: line-through;
}
</style>