<template>
  <div class="customer-profile">
    <div class="container">
      <div class="profile-header">
        <h3>Thông Tin Cá Nhân</h3>
      </div>

      <div class="profile-content">
        <div class="avatar-section">
          <div class="avatar-container">
            <img
              :src="product.avatar || 'https://via.placeholder.com/200x200/667eea/ffffff?text=Avatar'"
              alt="Avatar"
              class="avatar"
            />
            <div class="avatar-overlay">
              <button
                type="button"
                style="margin-left: 25px;"
                class="upload-btn"
                @click="$refs.avatarInput.click()"
              >
                <svg class="camera-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"></path>
                  <circle cx="12" cy="13" r="4"></circle>
                </svg>
                Đổi ảnh
              </button>
            </div>
          </div>
          <input
            type="file"
            accept="image/*"
            ref="avatarInput"
            @change="handleAvatarChange"
            style="display: none"
          />
        </div>

        <div class="info-section">
          <form @submit.prevent="handleSubmit" class="profile-form">
            <div class="form-row">
              <div class="form-group">
                <label for="name" class="form-label">
                  <svg class="label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                  Họ và Tên
                </label>
                <input
                  id="name"
                  type="text"
                  v-model="product.ten"
                  class="form-input"
                  placeholder="Nhập họ và tên của bạn"
                  required
                />
              </div>

              <div class="form-group">
                <label for="email" class="form-label">
                  <svg class="label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                    <polyline points="22,6 12,13 2,6"></polyline>
                  </svg>
                  Email
                </label>
                <input
                  id="email"
                  type="email"
                  v-model="product.email"
                  class="form-input"
                  placeholder="Nhập địa chỉ email của bạn"
                  required
                />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="phone" class="form-label">
                  <svg class="label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                  </svg>
                  Số Điện Thoại
                </label>
                <input
                  id="phone"
                  type="tel"
                  v-model="product.sdt"
                  class="form-input"
                  placeholder="Nhập số điện thoại của bạn"
                  required
                />
              </div>

              <div class="form-group">
                <label for="birthdate" class="form-label">
                  <svg class="label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                    <line x1="16" y1="2" x2="16" y2="6"></line>
                    <line x1="8" y1="2" x2="8" y2="6"></line>
                    <line x1="3" y1="10" x2="21" y2="10"></line>
                  </svg>
                  Ngày Sinh
                </label>
                <input
                  id="birthdate"
                  type="date"
                  v-model="product.ngaySinh"
                  class="form-input"
                  :max="new Date().toISOString().split('T')[0]"
                  required
                />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="province" class="form-label">
                  <svg class="label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                    <circle cx="12" cy="10" r="3"></circle>
                  </svg>
                  Tỉnh/Thành phố
                </label>
                <select
                  id="province"
                  v-model="product.tinh"
                  class="form-input"
                  @change="handleProvinceChange"
                  required
                >
                  <option value="">Chọn tỉnh/thành phố</option>
                  <option 
                    v-for="province in tinhThanh" 
                    :key="province.ProvinceID" 
                    :value="province.ProvinceID.toString()"
                  >
                    {{ province.ProvinceName }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label for="district" class="form-label">
                  <svg class="label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                    <circle cx="12" cy="10" r="3"></circle>
                  </svg>
                  Quận/Huyện
                </label>
                <select
                  id="district"
                  v-model="product.huyen"
                  class="form-input"
                  @change="handleDistrictChange"
                  :disabled="!product.tinh"
                  required
                >
                  <option value="">
                    {{ product.tinh ? 'Chọn quận/huyện' : 'Vui lòng chọn tỉnh/thành phố trước' }}
                  </option>
                  <option 
                    v-for="district in quanHuyen" 
                    :key="district.DistrictID" 
                    :value="district.DistrictID.toString()"
                  >
                    {{ district.DistrictName }}
                  </option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="ward" class="form-label">
                  <svg class="label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                    <circle cx="12" cy="10" r="3"></circle>
                  </svg>
                  Xã/Phường/Thị trấn
                </label>
                <select
                  id="ward"
                  v-model="product.xa"
                  class="form-input"
                  @change="handleCommuneChange"
                  :disabled="!product.huyen"
                  required
                >
                  <option value="">
                    {{ product.huyen ? 'Chọn xã/phường/thị trấn' : 'Vui lòng chọn quận/huyện trước' }}
                  </option>
                  <option 
                    v-for="ward in phuongXa" 
                    :key="ward.WardCode" 
                    :value="ward.WardCode"
                  >
                    {{ ward.WardName }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label for="gender" class="form-label">
                  <svg class="label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                  Giới Tính
                </label>
                <select
                  id="gender"
                  v-model="product.gioiTinh"
                  class="form-input"
                  required
                >
                  <option value="">Chọn giới tính</option>
                  <option :value="false">Nam</option>
                  <option :value="true">Nữ</option>
                </select>
              </div>
            </div>

            <div class="form-group full-width">
              <label for="address" class="form-label">
                <svg class="label-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                  <polyline points="9,22 9,12 15,12 15,22"></polyline>
                </svg>
                Địa Chỉ Chi Tiết
              </label>
              <textarea
                id="address"
                v-model="product.diaChi"
                class="form-textarea"
                placeholder="Nhập địa chỉ chi tiết (số nhà, tên đường...)"
                rows="3"
                required
              ></textarea>
            </div>

            <div class="form-actions">
              <button type="submit" class="submit-btn" :disabled="loading">
                <svg v-if="loading" class="loading-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <line x1="12" y1="2" x2="12" y2="6"></line>
                  <line x1="12" y1="18" x2="12" y2="22"></line>
                  <line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line>
                  <line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line>
                  <line x1="2" y1="12" x2="6" y2="12"></line>
                  <line x1="18" y1="12" x2="22" y2="12"></line>
                  <line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line>
                  <line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line>
                </svg>
                <svg v-else class="save-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                  <polyline points="17,21 17,13 7,13 7,21"></polyline>
                  <polyline points="7,3 7,8 15,8"></polyline>
                </svg>
                {{ loading ? 'Đang cập nhật...' : 'Cập Nhật Thông Tin' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import dayjs from "dayjs";
import {
  getKhachHang,
  modifyKhachHang,
  getKhachHangs,
  type KhachHangResponse,
} from "@/services/api/permitall/profile/profile.api";
import {
  getGHNProvinces,
  getGHNDistricts,
  getGHNWards,
} from "@/services/api/ghn.api";
import { localStorageAction } from "@/utils/storage";
import { USER_INFO_STORAGE_KEY } from "@/constants/storageKey";
import { useAuthStore } from "@/stores/auth";
import { ROUTES_CONSTANTS } from "@/constants/path";

const GHN_TOKEN = "72f634c6-58a2-11f0-8a1e-1e10d8df3c04";
const idKH = localStorageAction.get(USER_INFO_STORAGE_KEY);

const router = useRouter();
const authStore = useAuthStore();
const imageFile = ref<File | null>(null);
const avatarInput = ref();
const loading = ref(false);

// Product data model - keeping original structure
const product = ref<KhachHangResponse>({
  id: "",
  ten: "",
  sdt: "",
  diaChi: "",
  email: "",
  ma: "",
  cccd: "",
  gioiTinh: false,
  tinh: "",
  huyen: "",
  xa: "",
  ngaySinh: "",
  avatar: "",
});

// GHN location data
const tinhThanh = ref<Province[]>([]);
const quanHuyen = ref<District[]>([]);
const phuongXa = ref<Ward[]>([]);

interface Province {
  ProvinceID: number;
  ProvinceName: string;
}

interface District {
  DistrictID: number;
  DistrictName: string;
  ProvinceID: number;
}

interface Ward {
  WardCode: string;
  WardName: string;
  DistrictID: number;
}

const orders = ref<KhachHangResponse[]>([]);

// GHN API functions
const fetchGHNProvinces = async () => {
  try {
    const provinces = await getGHNProvinces(GHN_TOKEN);
    tinhThanh.value = provinces;
    console.log("Fetched provinces:", provinces);
  } catch (error) {
    console.error("Lỗi khi lấy danh sách tỉnh:", error);
  }
};

const fetchGHNDistricts = async (provinceId: number) => {
  try {
    const districts = await getGHNDistricts(provinceId, GHN_TOKEN);
    quanHuyen.value = districts;
    console.log("Fetched districts:", districts);
  } catch (error) {
    console.error("Lỗi khi lấy danh sách quận/huyện:", error);
    toast.error("Không lấy được danh sách quận/huyện.");
  }
};

const fetchGHNWards = async (districtId: number) => {
  try {
    const wards = await getGHNWards(districtId, GHN_TOKEN);
    phuongXa.value = wards;
    console.log("Fetched wards:", wards);
  } catch (error) {
    console.error("Lỗi khi lấy danh sách phường/xã:", error);
    toast.error("Không lấy được danh sách phường/xã.");
  }
};

// Location change handlers
const handleProvinceChange = (event: Event) => {
  const target = event.target as HTMLSelectElement;
  product.value.tinh = target.value;
  product.value.huyen = "";
  product.value.xa = "";
  quanHuyen.value = [];
  phuongXa.value = [];
  if (target.value) {
    fetchGHNDistricts(parseInt(target.value));
  }
};

const handleDistrictChange = (event: Event) => {
  const target = event.target as HTMLSelectElement;
  product.value.huyen = target.value;
  product.value.xa = "";
  phuongXa.value = [];
  if (target.value && product.value.tinh) {
    fetchGHNWards(parseInt(target.value));
  }
};

const handleCommuneChange = (event: Event) => {
  const target = event.target as HTMLSelectElement;
  product.value.xa = target.value;
};

// Fetch order history function
const fetchOrderHistory = async (orders: any, userId: string) => {
  try {
    const response = await getKhachHangs(userId);
    const { status, data, message, success } = response;

    if (status === "OK" && success) {
      orders.value = data.map((order: any) => ({
        id: order.id || "",
        ma: order.ma || "",
        ten: order.ten || "Đơn hàng online",
        sdt: order.sdt || "",
        tenKH: order.tenKH || "",
        phiVanChuyen: order.phiVanChuyen || 0,
        diaChi: order.diaChi || "",
        tongTienSauGiam: order.tongTienSauGiam || order.tongTien || 0,
        tongTien: order.tongTien || 0,
        ghiChu: order.ghiChu || "",
        phuongThucThanhToan: order.phuongThucThanhToan || "TIEN_MAT",
        loaiHoaDon: "ONLINE",
        trangThaiHoaDon: order.trangThaiHoaDon,
        ngayTao: order.ngayTao ? new Date(order.ngayTao).toISOString() : "",
      }));
    } else {
      toast.error(message || "Không thể lấy lịch sử đơn hàng!");
    }
  } catch (error: any) {
    console.error("Error fetching order history:", error);
    if (error.response?.status === 401) {
      toast.error("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại!");
      authStore.logout();
      router.push({ name: ROUTES_CONSTANTS.USERS.children.LOGIN.name });
    } else {
      const errorMessage = error.response?.data?.message || "Có lỗi xảy ra khi lấy lịch sử đơn hàng!";
      toast.error(errorMessage);
    }
  }
};

// Fetch product details with full original logic
const fetchProductDetails = async (id: string) => {
  try {
    const response = await getKhachHang(id);
    const data = response.data;

    console.log("API Response:", data);

    await fetchGHNProvinces();
    await nextTick();

    let formattedDate = "";
    if (data.ngaySinh) {
      const date = new Date(data.ngaySinh);
      formattedDate =
        date.getFullYear() +
        "-" +
        String(date.getMonth() + 1).padStart(2, "0") +
        "-" +
        String(date.getDate()).padStart(2, "0");
    }

    product.value = {
      ...product.value,
      id: data.id || "",
      ten: data.ten || "",
      sdt: data.sdt || "",
      diaChi: data.diaChi || "",
      email: data.email || "",
      ma: data.ma || "",
      cccd: data.cccd || "",
      gioiTinh: data.gioiTinh ?? false,
      ngaySinh: formattedDate,
      avatar: data.avatar || "",
    };

    if (data.tinh && tinhThanh.value.length > 0) {
      const province = tinhThanh.value.find(
        (p) => p.ProvinceID.toString() === data.tinh.toString()
      );
      if (province) {
        product.value.tinh = province.ProvinceID.toString();
        await fetchGHNDistricts(province.ProvinceID);
        await nextTick();

        if (data.huyen && quanHuyen.value.length > 0) {
          const district = quanHuyen.value.find(
            (d) => d.DistrictID.toString() === data.huyen.toString()
          );
          if (district) {
            product.value.huyen = district.DistrictID.toString();
            await fetchGHNWards(district.DistrictID);
            await nextTick();

            if (data.xa && phuongXa.value.length > 0) {
              const ward = phuongXa.value.find((w) => w.WardCode === data.xa);
              if (ward) {
                product.value.xa = ward.WardCode;
              }
            }
          }
        }
      }
    }
  } catch (error) {
    console.error("Error fetching product details:", error);
    if (error?.response?.data?.message) {
      toast.error(error.response.data.message);
    }
  }
};

// Form validation
const validateForm = (): boolean => {
  if (!product.value.ten?.trim()) {
    toast.error("Vui lòng nhập họ và tên!");
    return false;
  }

  if (!product.value.email?.trim()) {
    toast.error("Vui lòng nhập email!");
    return false;
  }

  if (!product.value.sdt?.trim()) {
    toast.error("Vui lòng nhập số điện thoại!");
    return false;
  }

  if (!product.value.diaChi?.trim()) {
    toast.error("Vui lòng nhập địa chỉ!");
    return false;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(product.value.email)) {
    toast.error("Email không đúng định dạng!");
    return false;
  }

  const phoneRegex = /^[0-9]{10}$/;
  if (!phoneRegex.test(product.value.sdt)) {
    toast.error("Số điện thoại phải là 10 chữ số!");
    return false;
  }

  if (product.value.gioiTinh === undefined || product.value.gioiTinh === null || product.value.gioiTinh === "") {
    toast.error("Vui lòng chọn giới tính!");
    return false;
  }

  return true;
};

// Handle avatar change with original logic
const handleAvatarChange = (event: Event) => {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (file) {
    // Validate file
    const validTypes = ["image/jpeg", "image/png", "image/gif"];
    if (!validTypes.includes(file.type)) {
      toast.error("File phải là định dạng JPEG, PNG hoặc GIF!");
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      toast.error("Kích thước file không được vượt quá 5MB!");
      return;
    }

    imageFile.value = file;
    const reader = new FileReader();
    reader.onload = (e) => {
      product.value.avatar = e.target?.result as string;
    };
    reader.readAsDataURL(file);
  }
};

// Handle form submission with original logic
const handleSubmit = async () => {
  try {
    if (!validateForm()) {
      return;
    }

    loading.value = true;

    const formData = new FormData();
    if (product.value.id) {
      formData.append("id", product.value.id.trim());
    }
    if (product.value.ma) {
      formData.append("code", product.value.ma.trim());
    }
    if (product.value.user) {
      formData.append("user", product.value.user.trim());
    }
    formData.append("ten", product.value.ten.trim());
    formData.append("email", product.value.email.trim());
    formData.append("sdt", product.value.sdt.trim());
    formData.append("diaChi", product.value.diaChi.trim());
    formData.append("gioiTinh", String(product.value.gioiTinh));
    if (product.value.cccd && product.value.cccd.trim()) {
      formData.append("cccd", product.value.cccd.trim());
    }
    if (product.value.ngaySinh) {
      const formattedDate = dayjs(product.value.ngaySinh).format("YYYY-MM-DD");
      formData.append("ngaySinh", formattedDate);
    }
    if (product.value.tinh) {
      formData.append("tinh", product.value.tinh);
    }
    if (product.value.huyen) {
      formData.append("huyen", product.value.huyen);
    }
    if (product.value.xa) {
      formData.append("xa", product.value.xa);
    }
    if (imageFile.value) {
      formData.append("avatar", imageFile.value);
    }

    console.log("Sending form data:");
    for (let [key, value] of formData.entries()) {
      console.log(key, value);
    }

    const res = await modifyKhachHang(formData);
    if (res.success && res.data?.avatar) {
      product.value.avatar = res.data.avatar;
    }
    toast.success(res.message || "Cập nhật thông tin thành công!");
    await nextTick();
    sessionStorage.setItem(
      "appToastMessage",
      JSON.stringify({
        message: res?.message || "Cập nhật thông tin thành công!",
        type: "success",
      })
    );

    if (product.value.id) {
      await fetchProductDetails(product.value.id);
    }
  } catch (error: any) {
    console.error("Error updating profile:", error);
    if (error?.response) {
      const errorMessage =
        error.response.data?.message || "Có lỗi xảy ra từ server!";
      toast.error(errorMessage);
    } else if (error?.request) {
      toast.error("Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng!");
    } else {
      toast.error("Có lỗi không xác định xảy ra!");
    }
  } finally {
    loading.value = false;
  }
};

// Initialize component
onMounted(() => {
  fetchGHNProvinces();
  if (idKH?.userId) {
    fetchProductDetails(idKH.userId);
    fetchOrderHistory(orders, idKH.userId);
  }
});
</script>

<style scoped>
.customer-profile {
  min-height: 100vh;
  padding: 20px;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 0 32px 64px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(20px);
  overflow: hidden;
}

.profile-header {
  color: black;
  padding: 40px;
  text-align: center;
  position: relative;
}

.profile-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.1'%3E%3Ccircle cx='7' cy='7' r='1'/%3E%3Ccircle cx='27' cy='7' r='1'/%3E%3Ccircle cx='47' cy='7' r='1'/%3E%3Ccircle cx='7' cy='27' r='1'/%3E%3Ccircle cx='27' cy='27' r='1'/%3E%3Ccircle cx='47' cy='27' r='1'/%3E%3Ccircle cx='7' cy='47' r='1'/%3E%3Ccircle cx='27' cy='47' r='1'/%3E%3Ccircle cx='47' cy='47' r='1'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.3;
}

.profile-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 8px 0;
  position: relative;
  z-index: 1;
}

.profile-header p {
  font-size: 1.1rem;
  opacity: 0.9;
  margin: 0;
  position: relative;
  z-index: 1;
}

.profile-content {
  padding: 40px;
}

.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 40px;
}

.avatar-container {
  position: relative;
  display: inline-block;
}

.avatar {
  width: 250px;
  height: 250px;
  border-radius: 50%;
  object-fit: cover;
  border: 6px solid #fff;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.avatar-container:hover .avatar {
  transform: scale(1.05);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.avatar-overlay {
  position: absolute;
  bottom: -5px;
  right: -15px;
}

.upload-btn {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  color: white;
  border: none;
  border-radius: 25px;
  margin-left: auto;
  padding: 8px 16px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(220, 38, 38, 0.4);
}

.upload-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(220, 38, 38, 0.6);
}

.camera-icon {
  width: 16px;
  height: 16px;
  stroke-width: 2;
}

.profile-form {
  max-width: 100%;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
  font-size: 0.95rem;
}

.label-icon {
  width: 18px;
  height: 18px;
  stroke-width: 2;
  color: #dc2626;
}

.form-input,
.form-textarea {
  padding: 14px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: #fff;
  font-family: inherit;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #dc2626;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
  transform: translateY(-1px);
}

.form-input:disabled {
  background-color: #f9fafb;
  color: #9ca3af;
  cursor: not-allowed;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.submit-btn {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 16px 32px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s ease;
  box-shadow: 0 8px 25px rgba(220, 38, 38, 0.3);
  min-width: 200px;
  justify-content: center;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 35px rgba(220, 38, 38, 0.4);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.save-icon,
.loading-icon {
  width: 20px;
  height: 20px;
  stroke-width: 2;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Responsive Design */
@media (max-width: 768px) {
  .customer-profile {
    padding: 10px;
  }
  
  .container {
    border-radius: 16px;
    margin: 10px;
  }
  
  .profile-header {
    padding: 30px 20px;
  }
  
  .profile-header h1 {
    font-size: 2rem;
  }
  
  .profile-content {
    padding: 30px 20px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
    gap: 16px;
    margin-bottom: 16px;
  }
  
  .avatar {
    width: 120px;
    height: 120px;
  }
  
  .form-input,
  .form-textarea {
    padding: 12px 14px;
  }
  
  .submit-btn {
    padding: 14px 24px;
    font-size: 1rem;
    min-width: 180px;
  }
}

@media (max-width: 480px) {
  .profile-header h1 {
    font-size: 1.75rem;
  }
  
  .avatar {
    width: 100px;
    height: 100px;
  }
  
  .upload-btn {
    padding: 6px 12px;
    font-size: 0.8rem;
  }
  
  .camera-icon {
    width: 14px;
    height: 14px;
  }
}

/* Custom Select Styling */
select.form-input {
  appearance: none;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e");
  background-position: right 12px center;
  background-repeat: no-repeat;
  background-size: 16px;
  padding-right: 40px;
}

/* Form Validation States */
.form-input:invalid {
  border-color: #ef4444;
}

.form-input:valid {
  border-color: #10b981;
}

/* Smooth Transitions */
* {
  transition: all 0.2s ease;
}

/* Focus States for Accessibility */
.form-input:focus-visible,
.form-textarea:focus-visible,
.submit-btn:focus-visible,
.upload-btn:focus-visible {
  outline: 2px solid #dc2626;
  outline-offset: 2px;
}

/* Loading State Animation */
.submit-btn:disabled .loading-icon {
  animation: spin 1s linear infinite;
}

/* Hover Effects */
.form-input:hover:not(:disabled):not(:focus) {
  border-color: #9ca3af;
}

.avatar-container:hover .upload-btn {
  transform: scale(1.05);
}
</style>