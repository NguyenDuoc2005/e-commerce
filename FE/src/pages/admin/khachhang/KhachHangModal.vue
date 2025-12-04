<template>
  <div class="page-container">
    <div class="breadcrumb-section">
      <BreadcrumbDefault :pageTitle="pathName" :routes="[
        { path: '/admin/khach-hang', name: 'Quản lý khách hàng' },
        { path: '/admin/them-khach-hang', name: pathName }
      ]" />
    </div>

    <DivCustom :label="label" customClasses="mt-5">
      <a-form :model="product" ref="productForm" name="productForm" autocomplete="off">

        <a-button style="color: white; background-color: #54bddb; margin-left: 900px; width: 150px;"
          @click="openQrModal">  Quét CCCD</a-button>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="Ảnh đại diện" name="avatar" :label-col="{ span: 24 }">
              <a-upload :before-upload="handleImageUpload" accept="image/*" :show-upload-list="false">
                <div v-if="!imageUrl" class="image-upload-container">
                  Chọn ảnh đại diện
                </div>
                <a-image v-else :src="imageUrl" class="image-upload-container"
                  style="width: 250px; margin-left: 80px; height: 250px; border-radius: 50%;" />
              </a-upload>
            </a-form-item>

            <a-form-item label="Tên khách hàng" name="ten" :label-col="{ span: 24 }" :rules="rules.name">
              <a-input v-if="product" v-model:value="product.ten" placeholder="Nhập tên khách hàng"
                style="border-radius: 4px;" />
            </a-form-item>
          </a-col>

          <a-col :span="16" style="margin-top: 25px;">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="Mã định danh (Số CMND/CCCD)" name="cccd" :label-col="{ span: 24 }"
                  style="margin-left: 15px;" :rules="rules.cccd">
                  <a-input v-if="product" v-model:value="product.cccd" placeholder="Mã định danh"
                    style="border-radius: 4px;" />
                </a-form-item>

                <a-form-item label="Ngày sinh" name="ngaySinh" :label-col="{ span: 24 }" style="margin-left: 15px;"
                  :rules="rules.ngaySinh">
                  <a-date-picker v-model:value="product.ngaySinh" style="width: 100%" />
                </a-form-item>

                <a-form-item label="Số điện thoại" name="sdt" :label-col="{ span: 24 }" style="margin-left: 15px;"
                  :rules="rules.sdt">
                  <a-input v-if="product" v-model:value="product.sdt" placeholder="Số điện thoại"
                    style="border-radius: 4px;" />
                </a-form-item>
              </a-col>

              <a-col :span="12">
                <a-form-item label="Giới tính" name="gioiTinh" :label-col="{ span: 24 }" style="margin-left: 15px;">
                  <a-radio-group v-model:value="product.gioiTinh">
                    <a-radio :value="true">Nam</a-radio>
                    <a-radio :value="false">Nữ</a-radio>
                  </a-radio-group>
                </a-form-item>

                <a-form-item label="Email" name="email" :label-col="{ span: 24 }" :rules="rules.email"
                  style="margin-left: 15px;">
                  <a-input v-if="product" v-model:value="product.email" placeholder="Email"
                    style="border-radius: 4px;" />
                </a-form-item>

                <a-form-item label="Địa chỉ" name="diaChi" :label-col="{ span: 24 }" :rules="rules.diaChi"
                  style="margin-left: 15px;">
                  <a-input v-if="product" v-model:value="product.diaChi" placeholder="Địa chỉ"
                    style="border-radius: 4px;" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="16">

              <a-col :span="8">
                <a-form-item style="width: 250px; margin-left: 15px;" label="Tỉnh/thành phố" name="tinhThanhPho"
                  :label-col="{ span: 24 }">
                  <a-select v-model:value="product.tinh" placeholder="Chọn tỉnh/thành phố"
                    @change="handleProvinceChange" :options="tinhThanhOptions" label-in-value
                    :key="tinhThanhOptions.length">
                    <template v-if="tinhThanhOptions.length === 0">
                      <a-select-option value="" disabled>Đang tải dữ liệu...</a-select-option>
                    </template>
                  </a-select>
                </a-form-item>
              </a-col>

              <a-col :span="8">
                <a-form-item style="width: 250px; margin-left: 15px;" label="Quận/huyện" name="quanHuyen"
                  :label-col="{ span: 24 }">
                  <a-select v-model:value="product.huyen" placeholder="Chọn quận/huyện" @change="handleDistrictChange"
                    :options="quanHuyenOptions" label-in-value :key="quanHuyenOptions.length">
                    <template v-if="quanHuyenOptions.length === 0">
                      <a-select-option value="" disabled>Đang tải dữ liệu...</a-select-option>
                    </template>
                  </a-select>
                </a-form-item>
              </a-col>

              <a-col :span="8">
                <a-form-item style="width: 250px; margin-left: 15px;" label="Xã/Phường/Thị trấn" name="xaPhuong"
                  :label-col="{ span: 24 }">
                  <a-select v-model:value="product.xa" placeholder="Chọn xã/phường/thị trấn"
                    @change="handleCommuneChange" :options="phuongXaOptions" label-in-value
                    :key="phuongXaOptions.length">
                    <template v-if="phuongXaOptions.length === 0">
                      <a-select-option value="" disabled>Đang tải dữ liệu...</a-select-option>
                    </template>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
          </a-col>
        </a-row>

        <a-form-item
          style="text-align: right; display: flex; justify-content: flex-end; margin-top: 30px; margin-right: 120px;">
          <a-button style="background-color: #54bddb; color: white; width: 150px; margin-right: 1000px;"
            @click="closeModal">Quay lại</a-button>
          <a-button style="background-color: #54bddb; color: white; margin-right: 30px; width: 150px;"
            html-type="submit" @click="handleSubmit">{{ label }}</a-button>
        </a-form-item>
      </a-form>
      <a-modal v-model:visible="isQrModalVisible" title="Quét QR" @cancel="closeQrModal">
        <div id="reader" style="width: 100%; height: 300px;"></div>
      </a-modal>
    </DivCustom>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, defineProps, defineEmits, onMounted, nextTick, computed } from 'vue';
import { getKhachHang, type KhachHangResponse, modifyKhachHang } from '@/services/api/admin/khachhang.api';
import { toast } from 'vue3-toastify';
import dayjs from 'dayjs';
import DivCustom from '@/components/custom/Div/DivCustom.vue';

import { Html5Qrcode } from 'html5-qrcode';
import { useRoute, useRouter } from 'vue-router';
import BreadcrumbDefault from '@/components/ui/Breadcrumbs/BreadcrumbDefault.vue';
import { getGHNProvinces, getGHNDistricts, getGHNWards } from '@/services/api/ghn.api';
import { QrcodeOutlined } from '@ant-design/icons-vue';

// Token GHN (thay bằng token thực tế từ GHN)
const GHN_TOKEN = '72f634c6-58a2-11f0-8a1e-1e10d8df3c04';

const route = useRoute();
const router = useRouter();

const idSanPham = ref('');

const props = defineProps<{ open: boolean; productId: string | null; title: string }>();
const emit = defineEmits(['close', 'success']);
const pathName = ref('');
const productForm = ref();
const imageUrl = ref<string | null>(null);
const imageFile = ref<File | null>(null);
const imageError = ref(false);

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

const tinhThanh = ref<Province[]>([]);
const quanHuyen = ref<District[]>([]);
const phuongXa = ref<Ward[]>([]);

const label = ref('');
const isQrModalVisible = ref(false);
const qrData = ref('');

let html5QrCode: Html5Qrcode;

const product = ref<KhachHangResponse>({
  id: '',
  ten: '',
  sdt: '',
  diaChi: '',
  email: '',
  ma: '',
  cccd: '',
  gioiTinh: true,
  tinh: '',
  huyen: '',
  xa: '',
});

const tinhThanhOptions = computed(() => {
  return tinhThanh.value.map(item => ({ value: item.ProvinceID.toString(), label: item.ProvinceName }));
});

const quanHuyenOptions = computed(() => {
  return quanHuyen.value.map(item => ({ value: item.DistrictID.toString(), label: item.DistrictName }));
});

const phuongXaOptions = computed(() => {
  return phuongXa.value.map(item => ({ value: item.WardCode, label: item.WardName }));
});

// Hàm kiểm tra tên khách hàng
const validateName = (_: any, value: any) => {
  // Convert the value to a string if it's an array
  const name = Array.isArray(value) ? value.join(' ') : value;

  if (!name) {
    return Promise.reject('Tên không được để trống!');
  }

  // Check for minimum character length (e.g., at least 2 characters)
  const minLength = 2; // You can adjust this value
  if (name.length < minLength) {
    return Promise.reject(`Tên phải có ít nhất ${minLength} ký tự!`);
  }

  // Check that the name only contains letters and spaces, no numbers or special characters
  const nameRegex = /^[a-zA-ZÀ-ỹ\s]+$/;
  if (!nameRegex.test(name)) {
    return Promise.reject('Tên chỉ được chứa chữ cái và dấu cách, không chứa số hoặc ký tự đặc biệt!');
  }

  return Promise.resolve();
};

// Hàm kiểm tra ngày sinh
const validateNgaySinh = (_: any, value: any) => {
  if (!value) {
    return Promise.reject('Ngày sinh không được để trống!');
  }
  const today = dayjs();
  if (dayjs(value).isSame(today, 'day') || dayjs(value).isAfter(today)) {
    return Promise.reject('Ngày sinh không được là ngày hiện tại hoặc trong tương lai!');
  }
  return Promise.resolve();
};

// Cập nhật rules với các quy tắc mới
const rules = {
  name: [
    { required: true, validator: validateName, trigger: 'blur' }
  ],
  avatar: [
    { required: true, message: 'Ảnh đại diện không được để trống!', trigger: 'change' }
  ],
  cccd: [
    { required: true, message: 'Mã định danh không được để trống!', trigger: 'blur' },
    { pattern: /^[0-9]{9,12}$/, message: 'Mã định danh phải là số và có độ dài từ 9 đến 12 ký tự!', trigger: 'blur' },
  ],
  ngaySinh: [
    { required: true, validator: validateNgaySinh, trigger: 'change' }
  ],
  sdt: [
    { required: true, message: 'Số điện thoại không được để trống!', trigger: 'blur' },
    { pattern: /^[0-9]{10}$/, message: 'Số điện thoại phải là 10 chữ số!', trigger: 'blur' },
  ],
  gioiTinh: [
    { required: true, message: 'Giới tính không được để trống!', trigger: 'change' }
  ],
  email: [
    { required: true, message: 'Email không được để trống!', trigger: 'blur' },
    { type: 'email', message: 'Email không hợp lệ!', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9._%+-]+@gmail\.com$/, message: 'Email phải có đuôi @gmail.com!', trigger: 'blur' }
  ],
  diaChi: [
    { required: true, message: 'Địa chỉ không được để trống!', trigger: 'blur' }
  ],
  tinhThanhPho: [
    { required: true, message: 'Tỉnh/thành phố không được để trống!', trigger: 'change' }
  ],
  quanHuyen: [
    { required: true, message: 'Quận/huyện không được để trống!', trigger: 'change' }
  ],
  xaPhuong: [
    { required: true, message: 'Xã/phường/thị trấn không được để trống!', trigger: 'change' }
  ]
};

// Các hàm khác giữ nguyên
const handleProvinceChange = (value: { value: string; label: string }) => {
  product.value.tinh = value.value;
  product.value.huyen = '';
  product.value.xa = '';
  quanHuyen.value = [];
  phuongXa.value = [];
  if (value.value) {
    fetchGHNDistricts(parseInt(value.value));
  }
};

const handleDistrictChange = (value: { value: string; label: string }) => {
  product.value.huyen = value.value;
  product.value.xa = '';
  phuongXa.value = [];
  if (value.value && product.value.tinh) {
    fetchGHNWards(parseInt(value.value));
  }
};

const handleCommuneChange = (value: { value: string; label: string }) => {
  product.value.xa = value.value;
};

const handleQrData = (message: string) => {
  const parts = message.split('|');
  product.value.cccd = parts[0];
  product.value.ten = parts[2];
  product.value.ngaySinh = parts[3] ? dayjs(parts[3], 'DDMMYYYY') : undefined;
  product.value.gioiTinh = parts[4] === 'Nam';
  product.value.diaChi = parts[5];
};

const openQrModal = () => {
  isQrModalVisible.value = true;
  nextTick(() => {
    startQrScanning();
  });
};

const closeQrModal = () => {
  isQrModalVisible.value = false;
  stopQrScanning();
};

const startQrScanning = () => {
  const qrRegionId = "reader";
  const qrRegionElement = document.getElementById(qrRegionId);
  if (!qrRegionElement) {
    console.error("Không tìm thấy phần tử với id 'reader'");
    return;
  }

  html5QrCode = new Html5Qrcode(qrRegionId);

  Html5Qrcode.getCameras().then((cameras: { id: string, label: string }[]) => {
    if (cameras && cameras.length) {
      const cameraId = cameras[0].id;
      html5QrCode.start(
        cameraId,
        { fps: 10, qrbox: 250 },
        (qrCodeMessage: string) => {
          qrData.value = qrCodeMessage;
          console.log("QR Code message: ", qrData.value);
          handleQrData(qrCodeMessage);
          html5QrCode.stop();
          closeQrModal();
        },
        errorMessage => {
          console.warn('Lỗi đọc QR: ', errorMessage);
        }
      );
    } else {
      console.warn("Không tìm thấy camera nào!");
    }
  }).catch((error: any) => {
    console.error("Lỗi khi lấy camera: ", error);
  });
};

const stopQrScanning = () => {
  if (html5QrCode) {
    html5QrCode.stop().catch(err => console.error('Không thể dừng scanner:', err));
  }
};

const fetchGHNProvinces = async () => {
  try {
    const provinces = await getGHNProvinces(GHN_TOKEN);
    tinhThanh.value = provinces;
    console.log('Fetched provinces:', provinces);
  } catch (error) {
    console.error('Lỗi khi lấy danh sách tỉnh:', error);
    toast.error('Không lấy được danh sách tỉnh từ GHN.');
  }
};

const fetchGHNDistricts = async (provinceId: number) => {
  try {
    const districts = await getGHNDistricts(provinceId, GHN_TOKEN);
    quanHuyen.value = districts;
    console.log('Fetched districts:', districts);
  } catch (error) {
    console.error('Lỗi khi lấy danh sách quận/huyện:', error);
    toast.error('Không lấy được danh sách quận/huyện.');
  }
};

const fetchGHNWards = async (districtId: number) => {
  try {
    const wards = await getGHNWards(districtId, GHN_TOKEN);
    phuongXa.value = wards;
    console.log('Fetched wards:', wards);
  } catch (error) {
    console.error('Lỗi khi lấy danh sách phường/xã:', error);
    toast.error('Không lấy được danh sách phường/xã.');
  }
};

const fetchProductDetails = async (id: string) => {
  try {
    const response = await getKhachHang(id);
    const data = response.data;

    await fetchGHNProvinces();
    await nextTick();

    product.value = {
      ...data,
      ngaySinh: data.ngaySinh ? dayjs(data.ngaySinh) : undefined,
      gioiTinh: data.gioiTinh === true || data.gioiTinh === 'true',
      tinh: '',
      huyen: '',
      xa: '',
    };

    imageUrl.value = data.avatar || null;

    if (data.tinh && tinhThanh.value.length > 0) {
      const province = tinhThanh.value.find(p => p.ProvinceID.toString() === data.tinh);
      if (province) {
        product.value.tinh = province.ProvinceID.toString();
        await fetchGHNDistricts(province.ProvinceID);
        await nextTick();

        if (data.huyen && quanHuyen.value.length > 0) {
          const district = quanHuyen.value.find(d => d.DistrictID.toString() === data.huyen);
          if (district) {
            product.value.huyen = district.DistrictID.toString();
            await fetchGHNWards(district.DistrictID);
            await nextTick();

            if (data.xa && phuongXa.value.length > 0) {
              const ward = phuongXa.value.find(w => w.WardCode === data.xa);
              if (ward) {
                product.value.xa = ward.WardCode;
              }
            }
          }
        }
      }
    }
  } catch (error) {
    if (error?.response?.data?.message) {
      toast.error(error?.response?.data?.message);
    }
  }
};

watch(
  () => [idSanPham, props.open],
  async ([id, isOpen]) => {
    if (isOpen) {
      if (productForm.value) {
        productForm.value.resetFields();
      }
      await fetchGHNProvinces();

      if (id) {
        await fetchProductDetails(id as string);
      } else {
        product.value = {
          id: '', ten: '', sdt: '', diaChi: '', email: '', ma: '', cccd: '', gioiTinh: true, tinh: '', huyen: '', xa: ''
        };
        imageUrl.value = null;
        imageFile.value = null;
        imageError.value = false;
        quanHuyen.value = [];
        phuongXa.value = [];
      }
    }
  },
  { immediate: true }
);

const handleImageUpload = (file: File) => {
  const reader = new FileReader();
  reader.onload = (e) => {
    imageUrl.value = e.target?.result as string;
    imageError.value = false;
  };
  reader.readAsDataURL(file);
  imageFile.value = file;
  return false;
};

const handleSubmit = async () => {
  try {
    await productForm.value.validate();

    const formData = new FormData();
    formData.append('id', product.value.id?.trim() || '');
    formData.append('code', product.value.ma?.trim() || '');
    formData.append('cccd', product.value.cccd?.trim() || '');
    formData.append('user', product.value.user?.trim() || '');
    formData.append('ten', product.value.ten?.trim() || '');
    formData.append('ngaySinh', product.value.ngaySinh ? dayjs(product.value.ngaySinh).format('YYYY-MM-DD') : '');
    formData.append('email', product.value.email?.trim() || '');
    formData.append('sdt', product.value.sdt?.trim() || '');
    formData.append('diaChi', product.value.diaChi?.trim() || '');
    formData.append('gioiTinh', product.value.gioiTinh?.toString() || 'true');
    formData.append('tinh', product.value.tinh || '');
    formData.append('huyen', product.value.huyen || '');
    formData.append('xa', product.value.xa || '');

    if (imageFile.value) {
      formData.append('avatar', imageFile.value);
    }

    const res = await modifyKhachHang(formData);

    if (res.message == 'số điện thoạt đã tồn tại') {
      toast.error(res.message)
      return
    }

    if (res.message == 'mã định danh đã tồn tại') {
      toast.error(res.message)
      return
    }


    nextTick(() => {
      sessionStorage.setItem('appToastMessage', JSON.stringify({
        message: res.message || 'Thao tác thành công!',
        type: 'success'
      }));
      router.push({ name: 'khach-hang-admin' });
    });
  } catch (error) {
    if (error?.response?.data?.message) {
      toast.error(error?.response?.data?.message);
    } else {
      toast.error('Có lỗi xảy ra khi gửi dữ liệu!');
    }
  }
};

onMounted(() => {
  idSanPham.value = route.query.id as string;
  if (idSanPham.value) {
    label.value = 'Sửa khách hàng';
    pathName.value = 'Sửa khách hàng';
  } else {
    pathName.value = 'Thêm khách hàng';
    label.value = 'Thêm khách hàng';
  }
  fetchGHNProvinces();
  if (idSanPham.value) {
    fetchProductDetails(idSanPham.value);
  }
});

const closeModal = () => {
  router.push({ name: 'khach-hang-admin' });
};
</script>

<style scoped>
/* Giữ nguyên phần style của bạn */
.input-item {
  margin-left: 10px;
  margin-bottom: 16px;
}

.image-upload-container {
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
  border-radius: 50%;
  width: 250px;
  height: 250px;
  border: 2px dashed #1890ff;
  cursor: pointer;
}

.image-upload-container {
  margin-left: 80px;
  width: 250px;
  height: 250px;
  border-radius: 10px;
  border: 2px dashed #1890ff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background-color: #f5f5f5;
  text-align: center;
  border-radius: 50%;
}

a-image .avatar-upload {
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.avatar-image {
  border-radius: 50%;
}

.upload-text {
  color: #66b3ff;
  margin-top: 8px;
  font-size: 14px;
}

.upload-text:hover {
  text-decoration: underline;
}

.a-upload {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  border: none !important;
  outline: none !important;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background-color: #f5f5f5;
}

.a-upload .ant-upload-trigger {
  width: 100%;
  height: 100%;
  border: none !important;
  display: flex;
  align-items: center;
  justify-content: center;
}

.a-upload .ant-upload-list-picture-card {
  display: none;
}

.ant-upload .ant-upload-trigger a-icon {
  font-size: 40px;
}

.page-container {
  padding: 20px;
}

.breadcrumb-section {
  margin-bottom: 25px;
  background-color: #fff;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.section-title {
  margin-top: 30px;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  margin-left: 0px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.ant-input:hover),
:deep(.ant-input:focus),
:deep(.ant-input-focused) {
  border-color: #58bddb !important;
  /* Màu xanh đậm hơn */
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
  /* Đổ bóng màu xanh đậm */
}

/* Style cho a-input-number khi hover, focus, và focused */
:deep(.ant-input-number:hover),
:deep(.ant-input-number:focus),
:deep(.ant-input-number-focused) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

/* Style cho a-date-picker khi hover, focus, và focused */
:deep(.ant-picker:hover),
:deep(.ant-picker-focused),
/* Khi DatePicker đã mở và focus vào */
:deep(.ant-picker-focused .ant-picker-input > input),
/* Đảm bảo input bên trong cũng bị ảnh hưởng */
:deep(.ant-picker:focus-within)

/* Dùng cho các component phức tạp có nhiều phần tử con */
  {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

body {
  font-family: 'Roboto', sans-serif;
}

:deep(.ant-select:not(.ant-select-disabled):hover .ant-select-selector),
:deep(.ant-select-focused:not(.ant-select-disabled) .ant-select-selector),
:deep(.ant-select-open .ant-select-selector) {
  border-color: #58bddb !important;
  /* Màu xanh đậm hơn */
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
  /* Đổ bóng màu xanh đậm */
}

/* Đảm bảo hiệu ứng khi focus hoặc mở dropdown */
:deep(.ant-select-focused .ant-select-selector),
:deep(.ant-select-open .ant-select-selector) {
  border-color: #58bddb !important;
  /* Màu xanh đậm hơn */
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
  /* Đổ bóng màu xanh đậm */
}

/* Style cho option khi hover trong dropdown */
:deep(.ant-select-item-option-active) {
  background-color: #e6f7ff !important;
  /* Nền xanh nhạt khi hover trên option */
}

/* Đảm bảo các combobox trong form không bị ảnh hưởng bởi style khác */
:deep(.ant-select-selector) {
  border-radius: 4px !important;
  /* Giữ đồng nhất với các input */
}

/* Style cho a-button khi hover */
:deep(.ant-btn:hover),
:deep(.ant-btn:focus) {
  background-color: #4aa8c6 !important;
  /* Màu xanh đậm hơn khi hover */
  border-color: #4aa8c6 !important;
  color: white !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
  /* Đổ bóng xanh */
}

/* Style cho a-radio khi hover */
:deep(.ant-radio-wrapper:hover .ant-radio-inner) {
  border-color: #58bddb !important;
  /* Viền xanh khi hover */
}

:deep(.ant-radio-wrapper:hover .ant-radio .ant-radio-inner::after) {
  background-color: #58bddb !important;
  /* Điểm chọn xanh khi hover */
}
</style>