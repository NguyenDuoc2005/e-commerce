<template>
  <div class="breadcrumb-section">
    <BreadcrumbDefault :pageTitle="pathName" :routes="[
      { path: '/admin/voucher', name: 'Quản lý phiếu giảm giá' },
      { path: '/admin/them-phieu-giam-gia', name: pathName },
    ]" />
  </div>

  <a-row :gutter="24">
    <a-col :span="8">
      <div class="form-container">
        <a-form :model="product" ref="productForm" name="productForm" autocomplete="off">
          <a-form-item label="Tên phiếu giảm giá" name="ten" :label-col="{ span: 24 }" :rules="rules.name">
            <a-input v-if="product" v-model:value="product.ten" placeholder="Nhập tên phiếu giảm giá"
              style="border-radius: 4px" />
          </a-form-item>

          <a-form-item label="Điều kiện giảm giá" name="dieuKien" :label-col="{ span: 24 }" :rules="rules.dieuKien">
            <a-input-number v-if="product" v-model:value="product.dieuKien" placeholder="Nhập điều kiện giảm giá"
              style="border-radius: 4px; width: 100%"
              :formatter="value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
              :parser="value => value.replace(/\$\s?|(,*)/g, '')" />
          </a-form-item>

          <a-form-item label="Giá trị giảm tối đa" name="giaGiam" :label-col="{ span: 24 }" :rules="rules.giaGiam">
            <a-input-number v-if="product" v-model:value="product.giaGiam" placeholder="Nhập giá trị giảm tối đa"
              style="border-radius: 4px; width: 100%"
              :formatter="value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
              :parser="value => value.replace(/\$\s?|(,*)/g, '')" />
          </a-form-item>

          <a-form-item v-if="product && product.kieuGiam === true" label="Phần trăm giảm giá" name="phanTramGiam"
            :label-col="{ span: 24 }" :rules="rules.phanTramGiam">
            <a-input-number v-model:value="product.phanTramGiam" style="width: 100%; border-radius: 4px" min="0"
              max="100" placeholder="Nhập phần trăm giảm giá" />
          </a-form-item>

          <a-form-item label="Kiểu giảm giá" name="kieuGiam" :label-col="{ span: 24 }">
            <a-radio-group v-model:value="product.kieuGiam">
              <a-radio :value="true">Phần trăm</a-radio>
              <a-radio :value="false">Tiền</a-radio>
            </a-radio-group>
          </a-form-item>

          <a-form-item label="Số lượng" name="soLuongPhieu" :label-col="{ span: 24 }" :rules="rules.soLuongPhieu">
            <a-input-number v-if="product" v-model:value="product.soLuongPhieu" placeholder="Nhập Số lượng"
              style="border-radius: 4px; width: 100%" :disabled="product.loaiGiam === true" />
          </a-form-item>

          <a-form-item label="Ngày bắt đầu" name="ngayBatDau" :rules="rules.ngayBatDau" :label-col="{ span: 24 }">
            <a-date-picker v-model:value="product.ngayBatDau" format="YYYY-MM-DD" style="width: 100%" />
          </a-form-item>

          <a-form-item label="Ngày kết thúc" name="ngayKetThuc" :rules="rules.ngayKetThuc" :label-col="{ span: 24 }">
            <a-date-picker v-model:value="product.ngayKetThuc" format="YYYY-MM-DD" style="width: 100%" />
          </a-form-item>
          <a-form-item label="Loại giảm giá" name="loaiGiam" :label-col="{ span: 24 } ">
            <a-radio-group v-model:value="product.loaiGiam" >
              <a-radio :value="false">Công khai</a-radio>
              <a-radio :value="true">Cá nhân</a-radio>
            </a-radio-group>
          </a-form-item>
        </a-form>
      </div>
    </a-col>

    <a-col :span="16">
      <div class="table-container">
        <h6 class="fw-semibold mb-3">Danh sách khách hàng</h6>
        <div class="flex items-center gap-3 mb-4" style="margin-bottom: 10px;">
          <a-input v-model:value="state.searchQuery" style="width: 600px; margin-right: 10px;"
            placeholder="Nhập mã/tên/số điện thoại/địa chỉ để tìm kiếm..."
            class="w-full rounded-md border-gray-300 focus:border-blue-500" />
          <a-tooltip title="Làm mới bộ lọc">
            <a-button @click="resetFilters" class="flex items-center justify-center bg-gray-100 hover:bg-gray-200">
              <ReloadOutlined class="text-gray-600" />
            </a-button>
          </a-tooltip>
        </div>

        <a-table :columns="columns" :data-source="state.products" :pagination="{
          current: state.paginationParams.page,
          pageSize: state.paginationParams.size,
          total: state.totalItems,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '30', '40', '50'],
          onChange: (page, size) => {
            state.paginationParams.page = page;
            state.paginationParams.size = size;
            fetchProducts(); // Gọi lại để cập nhật bảng
            if (product.value.loaiGiam && idSanPham.value) {
              fetchListKHDetails(idSanPham.value); // Cập nhật danh sách khách hàng được chọn
            }
          },
        }" :scroll="{ y: 558 }" class="bg-white rounded-md">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'select'">
              <a-checkbox :checked="state.selectedRows.includes(record.id)"
                @change="onCheckboxChange(record.id, $event.target.checked)" :disabled="!product.loaiGiam" />
            </template>
            <template v-if="column.key === 'stt'">
              {{ (state.paginationParams.page - 1) * state.paginationParams.size + state.products.indexOf(record) + 1 }}
            </template>
          </template>
        </a-table>
      </div>
    </a-col>
  </a-row>

  <a-form-item style="
        text-align: right;
        display: flex;
        justify-content: flex-end;
        margin-top: 30px;
        margin-right: 50px;
      ">
    <a-button style="background-color: #58bddb; color: white; width: 150px; margin-right: 700px"
      @click="closeModal">Quay
      lại</a-button>
    <a-button style="background-color: #58bddb; color: white; margin-right: 30px; width: 180px" html-type="submit"
      @click="handleSubmit">{{ label }}</a-button>
  </a-form-item>
</template>

<script setup lang="ts">
import { ref, watch, reactive, onMounted } from "vue";
import { getListKH, getSize, modifySize, type SizeResponse } from "@/services/api/admin/voucher.api";
import { GetKhachHangs, type KhachHangResponse, type ParamsGetKhachHang } from "@/services/api/admin/khachhang.api";
import { toast } from "vue3-toastify";
import { useRouter, useRoute } from "vue-router";
import { ReloadOutlined } from "@ant-design/icons-vue";
import dayjs from "dayjs";
import type { TableColumnsType } from "ant-design-vue";
import BreadcrumbDefault from "@/components/ui/Breadcrumbs/BreadcrumbDefault.vue";

const props = defineProps<{
  open: boolean;
  productId: string | null;
  title: string;
}>();
const emit = defineEmits(["close", "success"]);
const idSanPham = ref("");
const route = useRoute();
const router = useRouter();
const product = ref<SizeResponse>({
  id: "",
  ten: "",
  sdt: "",
  diaChi: "",
  email: "",
  ma: "",
  gioiTinh: true,
  kieuGiam: false,
  loaiGiam: false,
  phanTramGiam: 0,
  soLuongPhieu: undefined,
  dieuKien: undefined,
  giaGiam: undefined,
  ngayBatDau: undefined,
  ngayKetThuc: undefined,
});
const pathName = ref("");
const productForm = ref();
const label = ref("");

const rules = {
  name: [
    { required: true, message: "Trường này không được để trống!", trigger: "blur" },
  ],
  soLuongPhieu: [
    { required: true, message: "Số lượng phiếu không được để trống", trigger: "blur" },
    { type: "number", min: 1, message: "Số lượng phiếu phải lớn hơn 0", trigger: "blur" },
  ],
  dieuKien: [
    { required: true, message: "Điều kiện giảm giá không được để trống", trigger: "blur" },
    { type: "number", min: 1, message: "Điều kiện phải lớn hơn 0", trigger: "blur" },
  ],
  giaGiam: [
    { required: true, message: "Giá trị giảm không được để trống", trigger: "blur" },
    { type: "number", min: 1, message: "Giá trị giảm phải lớn hơn 0", trigger: "blur" },
  ],
  phanTramGiam: [
    { required: true, message: "Phần trăm giảm giá không được để trống", trigger: "blur" },
    { type: "number", min: 1, message: "Phần trăm giảm giá không được âm", trigger: "blur" },
    {
      validator: (_: any, value: any) => {
        if (value > 100) {
          return Promise.reject("Phần trăm giảm giá không được vượt quá 100%");
        }
        return Promise.resolve();
      },
      trigger: "blur",
    },
  ],
  ngayBatDau: [
    { required: true, message: "Vui lòng chọn ngày bắt đầu", trigger: "change" },
    {
      validator: (_: any, value: any) => {
        if (value && dayjs(value).isBefore(dayjs(), "day")) {
          return Promise.reject("Ngày bắt đầu không được là ngày quá khứ");
        }
        const ngayKetThuc = product.value?.ngayKetThuc;
        if (ngayKetThuc && dayjs(value).isAfter(dayjs(ngayKetThuc))) {
          return Promise.reject("Ngày bắt đầu phải trước ngày kết thúc");
        }
        return Promise.resolve();
      },
      trigger: "change",
    },
  ],
  ngayKetThuc: [
    { required: true, message: "Vui lòng chọn ngày kết thúc", trigger: "change" },
    {
      validator: (_: any, value: any) => {
        if (value && dayjs(value).isBefore(dayjs(), "day")) {
          return Promise.reject("Ngày kết thúc không được là ngày quá khứ");
        }
        const ngayBatDau = product.value?.ngayBatDau;
        if (ngayBatDau && dayjs(value).isBefore(dayjs(ngayBatDau))) {
          return Promise.reject("Ngày kết thúc phải sau ngày bắt đầu");
        }
        return Promise.resolve();
      },
      trigger: "change",
    },
  ],
};

const columns: TableColumnsType = [
  {
    title: "Chọn",
    key: "select",
    dataIndex: "select",
    width: 100,
    align: "center",
  },
  { title: "STT", key: "stt", width: 150, align: "center" },
  { title: "Mã khách hàng", key: "ma", dataIndex: "ma", width: 150, align: "center" },
  { title: "Tên khách hàng", key: "ten", dataIndex: "ten", width: 150, align: "center" },
  { title: "Số điện thoại", key: "sdt", dataIndex: "sdt", width: 150, align: "center" },
  { title: "Email", key: "email", dataIndex: "email", width: 150, align: "center" },
];

const state = reactive({
  selectAll: false,
  selectedRows: [] as string[],
  searchQuery: "",
  searchStatus: null as number | null,
  isModalOpen: false,
  isModalChangeStatus: false,
  selectedProductId: null as string | null,
  products: [] as KhachHangResponse[],
  paginationParams: { page: 1, size: 10 },
  totalItems: 0,
});

const fetchProductDetails = async (id: string) => {
  try {
    const response = await getSize(id);
    const data = response.data;
    product.value = {
      ...data,
      ngayBatDau: data.ngayBatDau ? dayjs(data.ngayBatDau) : undefined,
      ngayKetThuc: data.ngayKetThuc ? dayjs(data.ngayKetThuc) : undefined,
      loaiGiam: data.loaiGiam === true || data.loaiGiam === "true",
      kieuGiam: data.kieuGiam === true || data.kieuGiam === "true",
      phanTramGiam: data.phanTramGiam ?? 0,
    };
  } catch (error) {
    if (error?.response?.data?.message) {
      toast.error(error.response.data.message);
    } else {
      console.error("Lỗi khi lấy chi tiết phiếu giảm giá:", error);
      toast.error("Đã xảy ra lỗi khi lấy chi tiết phiếu giảm giá.");
    }
  }
};

const fetchListKHDetails = async (id: string) => {
  try {
    const response = await getListKH(id, state.searchQuery, state.paginationParams.page, state.paginationParams.size);
    state.selectedRows = response.content || [];
    console.log("Selected customer IDs:", state.selectedRows);
  } catch (error) {
    if (error?.response?.data?.message) {
      toast.error(error.response.data.message);
    } else {
      console.error("Error fetching list of customer IDs:", error);
      toast.error("Đã xảy ra lỗi khi lấy danh sách khách hàng liên kết.");
    }
  }
};

const fetchProducts = async () => {
  try {
    const params: ParamsGetKhachHang = {
      page: state.paginationParams.page,
      size: state.paginationParams.size,
      q: state.searchQuery,
      status: state.searchStatus,
    };
    const response = await GetKhachHangs(params);
    state.products = response.data?.data || [];
    state.totalItems = response.data?.totalElements || 0;
  } catch (error) {
    console.error("Failed to fetch customers for table:", error);
    toast.error("Đã xảy ra lỗi khi lấy danh sách khách hàng.");
  }
};

const resetFilters = async () => {
  state.searchQuery = "";
  state.paginationParams.page = 1;
  state.paginationParams.size = 10;
  await fetchProducts();
  if (product.value.loaiGiam && idSanPham.value) {
    await fetchListKHDetails(idSanPham.value);
  }
};

const onCheckboxChange = (id: string, checked: boolean) => {
  if (checked) {
    if (!state.selectedRows.includes(id)) {
      state.selectedRows.push(id);
    }
  } else {
    state.selectedRows = state.selectedRows.filter((rowId) => rowId !== id);
  }
  if (product.value.loaiGiam) {
    product.value.soLuongPhieu = state.selectedRows.length;
  }
  console.log("Current selected rows:", state.selectedRows);
};

const handleSubmit = async () => {
  try {
    await productForm.value.validate();
    const formData = new FormData();
    formData.append("id", product.value.id?.trim() || "");
    formData.append("ma", product.value.ma?.trim() || "");
    formData.append("ten", product.value.ten?.trim() || "");
    formData.append("soLuongPhieu", product.value.soLuongPhieu?.toString() || "");
    formData.append("dieuKien", product.value.dieuKien?.toString() || "");
    formData.append("giaGiam", product.value.giaGiam?.toString() || "");
    formData.append("kieuGiam", product.value.kieuGiam?.toString() || "false");
    formData.append("loaiGiam", product.value.loaiGiam?.toString() || "false");
    formData.append(
      "ngayBatDau",
      product.value.ngayBatDau ? dayjs(product.value.ngayBatDau).format("YYYY-MM-DD") : ""
    );
    formData.append(
      "ngayKetThuc",
      product.value.ngayKetThuc ? dayjs(product.value.ngayKetThuc).format("YYYY-MM-DD") : ""
    );

    if (product.value?.kieuGiam === true) {
      // Kiểu giảm giá là phần trăm
      if (product?.value?.phanTramGiam == null || product.value.phanTramGiam <= 0 || product.value.phanTramGiam > 100) {
        toast.error("Phần trăm giảm giá phải từ 1 đến 100");
        return;
      }
      formData.append("LoiPhanNay", product.value.phanTramGiam.toString());
      formData.append("giaGiam", product.value.giaGiam.toString());
    } else {
      formData.append("giaGiam", product.value.giaGiam.toString());
      formData.append("LoiPhanNay", product.value.giaGiam.toString()); // Đặt phanTramGiam về 0 khi dùng tiền
    }


    if (product.value.loaiGiam === true) {
      if (state.selectedRows.length === 0) {
        toast.error("Vui lòng chọn ít nhất một khách hàng cho phiếu giảm giá cá nhân.");
        return;
      }
      state.selectedRows.forEach((id) => {
        formData.append("khachHangIds", id);
      });
    }

    console.log("Form data before submission:", Object.fromEntries(formData));
    const res = await modifySize(formData);
    if (res.message === "phiếu giảm giá này đã tồn tại") {
      toast.error(res.message);
      return;
    }
    toast.success(res.message);
    emit("success");
    closeModal();
  } catch (error: any) {
    if (error?.response?.data?.message) {
      toast.error(error.response.data.message);
    } else {
      console.error("Lỗi trong quá trình gửi form:", error);
      toast.error("Đã xảy ra lỗi không mong muốn.");
    }
  }
};

const closeModal = () => {
  router.push({ name: "voucher-admin" });
};

watch(
  () => [state.searchQuery, state.paginationParams.page, state.paginationParams.size],
  async () => {
    await fetchProducts();
    if (product.value.loaiGiam && idSanPham.value) {
      await fetchListKHDetails(idSanPham.value);
    }
  },
  { deep: true }
);

watch(
  () => [props.productId, props.open],
  async ([newId, isOpen]) => {
    if (isOpen) {
      if (productForm.value) {
        productForm.value.resetFields();
      }
      state.selectedRows = [];

      if (newId) {
        label.value = "Sửa phiếu giảm giá";
        await fetchProductDetails(newId);
        if (product.value.loaiGiam) {
          await fetchListKHDetails(newId);
        }
      } else {
        label.value = "Thêm phiếu giảm giá";
        product.value = {
          id: "",
          ten: "",
          sdt: "",
          diaChi: "",
          email: "",
          ma: "",
          gioiTinh: true,
          kieuGiam: false,
          loaiGiam: false,
          phanTramGiam: 0,
          soLuongPhieu: undefined,
          dieuKien: undefined,
          giaGiam: undefined,
          ngayBatDau: undefined,
          ngayKetThuc: undefined,
        };
        state.selectedRows = [];
      }
      await fetchProducts();
    }
  },
  { immediate: true }
);

watch(
  () => product.value.loaiGiam,
  (newLoaiGiam) => {
    if (newLoaiGiam) {
      product.value.soLuongPhieu = state.selectedRows.length;
    }
  }
);

onMounted(() => {
  idSanPham.value = route.query.id as string;
  if (idSanPham.value) {
    label.value = "Sửa phiếu giảm giá";
    fetchProductDetails(idSanPham.value);
    fetchListKHDetails(idSanPham.value);
    pathName.value = "Sửa phiếu giảm giá";
  } else {
    pathName.value = "Thêm phiếu giảm giá";
    label.value = "Thêm phiếu giảm giá";
  }
  fetchProducts();
});
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.breadcrumb-section {
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

body {
  font-family: "Roboto", sans-serif;
}

.form-container,
.table-container {
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background-color: #fff;
}

.table-container {
  margin-top: 0px;
}

.table-container h5 {
  margin-bottom: 16px;
  font-size: 1.25rem;
  color: #333;
}

.text-decoration-line-through {
  text-decoration: line-through;
}

:deep(.ant-input:hover),
:deep(.ant-input:focus),
:deep(.ant-input-focused) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

:deep(.ant-input-number:hover),
:deep(.ant-input-number:focus),
:deep(.ant-input-number-focused) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

:deep(.ant-picker:hover),
:deep(.ant-picker-focused),
:deep(.ant-picker-focused .ant-picker-input > input),
:deep(.ant-picker:focus-within) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}
</style>