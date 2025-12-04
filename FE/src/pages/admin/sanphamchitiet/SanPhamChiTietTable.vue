<template>
  <DivCustom label="Danh sách sản phẩm chi tiết" customClasses="mt-5">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <div style="font-size: 13px; color: #5FB3B3; margin-left: 15px;">

      </div>

      <div style="display: flex; gap: 10px;">
        <a-tooltip title="Tải QR cho các sản phẩm đã chọn">
          <a-button style="background-color: #ff7f32;" type="primary" @click="handleDownloadSelectedQr"
            :disabled="selectedRowKeys.length === 0" class="d-flex justify-content-center align-items-center px-4">
            <DownloadOutlined /> Tải QR đã chọn ({{ selectedRowKeys.length }})
          </a-button>
        </a-tooltip>

        <a-tooltip title="Thêm sản phẩm chi tiết">
          <a-button style="background-color: #54bddb;" type="primary" @click="handleAddClick(idSP)"
            class="d-flex justify-content-center align-items-center px-4">
            <PlusCircleOutlined /> Thêm mới sản phẩm chi tiết
          </a-button>
        </a-tooltip>
      </div>
    </div>

    <div class="min-h-[360px]">
      <a-table :columns="columns" :data-source="products" :pagination="{
        current: paginationParams.page,
        pageSize: paginationParams.size,
        total: totalItems,
        showSizeChanger: true,
        pageSizeOptions: ['10', '20', '30', '40', '50']
      }" :scroll="{ y: 300 }" @change="handlePageChange" :row-selection="rowSelection" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status == 'ACTIVE' ? 'green' : 'red'">
              {{ record.status == 'ACTIVE' ? 'Đang kinh doanh' : 'Ngừng kinh doanh' }}
            </a-tag>
          </template>

          <div v-if="column.key === 'stt'">
            {{ products.indexOf(record) + 1 + (paginationParams.page - 1) * paginationParams.size }}
          </div>

          <template v-if="column.key === 'giaBan'">
            {{ formatCurrencyVND(parseFloat(record.giaBan)) }}
          </template>

          <template v-if="column.key === 'anh'">
            <div class="center-cell">
              <img :src="record.anh" class="anh"
                style="width: 50px; height: 50px; border-radius: 4px; object-fit: cover;" />
            </div>
          </template>

          <template v-if="column.key === 'soLuong'">
            <div class="center-cell" style="display: flex; align-items: center;">
              <span> {{ record.soLuong <= 0 ? 0 : record.soLuong }}</span>
            </div>
          </template>

          <template v-if="column.key === 'operation'">
            <div class="d-flex gap-1 justify-center">
              <a-popconfirm title="Bạn có chắc chắn muốn thay đổi trạng thái không?"
                @confirm="handleChangeStatusClick(record.id)" ok-text="Đồng ý" cancel-text="Huỷ">
                <a-tooltip title="Đổi trạng thái sản phẩm">
                  <a-button style="background-color: #9b6dc7;" type="primary"
                    class="p-2 d-flex justify-content-center align-items-center">
                    <RedoOutlined />
                  </a-button>
                </a-tooltip>
              </a-popconfirm>
              <a-tooltip title="Xem chi tiết sản phẩm">
                <a-button style="background-color: #54bddb;" type="primary" @click="handleClick(record.id)"
                  class="p-2 d-flex justify-content-center align-items-center">
                  <EditOutlined />
                </a-button>
              </a-tooltip>
            </div>
          </template>
        </template>
      </a-table>
    </div>
  </DivCustom>
</template>

<script setup lang="ts">
import DivCustom from '@/components/custom/Div/DivCustomTable.vue'
import { PlusCircleOutlined, RedoOutlined, EyeOutlined, DownloadOutlined, EditOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import { defineEmits, defineProps, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { toast } from 'vue3-toastify'
import { modifyStatusSanPham } from '@/services/api/admin/sanphamchitiet.api'
import QRCode from 'qrcode' // Import thư viện qrcode

// Định nghĩa Props mà component này nhận từ component cha
defineProps<{
  paginationParams: { page: number; size: number }
  totalItems: number
  products: any[] // Mảng chứa dữ liệu sản phẩm, có thể định nghĩa kiểu cụ thể hơn nếu có
  idSP: string // ID của sản phẩm chính (sản phẩm cha)
}>()

const router = useRouter()
// Định nghĩa các sự kiện mà component này có thể phát ra (emit)
const emit = defineEmits(['page-change', 'add', 'view', 'changeStatus'])

// --- State cho Checkbox Selection ---
const selectedRowKeys = ref<string[]>([]); // Mảng lưu trữ id của các hàng được chọn

// Hàm được gọi khi có sự thay đổi trong lựa chọn checkbox
const onSelectChange = (keys: string[]) => {
  console.log('selectedRowKeys changed:', keys);
  selectedRowKeys.value = keys;
};

// Computed property để cấu hình tính năng chọn hàng của a-table
const rowSelection = computed(() => {
  return {
    selectedRowKeys: selectedRowKeys.value, // Bind với state selectedRowKeys
    onChange: onSelectChange, // Gán hàm xử lý sự kiện thay đổi
    // Tùy chọn: disabled checkbox cho một số hàng cụ thể nếu cần
    // getCheckboxProps: (record: any) => ({
    //   disabled: record.someCondition, // Ví dụ: record.status === 'INACTIVE'
    //   name: record.id,
    // }),
  };
});

// --- Hàm định dạng tiền tệ VND ---
const formatCurrencyVND = (amount: number) => {
  if (typeof amount !== 'number' || isNaN(amount)) { // Xử lý trường hợp không phải số
    return amount;
  }
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0, // Không hiển thị số thập phân
    maximumFractionDigits: 0, // Không hiển thị số thập phân
  }).format(amount);
};

const columns: TableColumnsType = [
  { title: 'STT', key: 'stt', dataIndex: 'stt', width: 60, align: 'center' },
  { title: 'Ảnh', key: 'anh', dataIndex: 'anh', width: 70, align: 'center' }, // Giảm width ảnh
  {
    title: 'Tên sản phẩm',
    key: 'ten',
    dataIndex: 'ten',
    align: 'left',
    width: 100,
    customRender: ({ text }: { text: string }) => text || 'N/A'
  },
  { title: 'Số lượng', key: 'soLuong', dataIndex: 'soLuong', width: 70, align: 'center' },
  { title: 'Giá bán', key: 'giaBan', dataIndex: 'giaBan', width: 90, align: 'right' },
  { title: 'Kích cỡ', key: 'kichThuoc', dataIndex: 'kichThuoc', width: 80, align: 'center' },
  { title: 'Màu sắc', key: 'tenMau', dataIndex: 'tenMau', width: 100, align: 'center' },

  { title: 'Trạng thái', key: 'status', dataIndex: 'status', width: 130, align: 'center' },
  {
    title: 'Hành động',
    key: 'operation',
    width: 90,
    align: 'center'
  }
];
// --- Hàm xử lý thay đổi trạng thái sản phẩm ---
const handleChangeStatusClick = async (id: string) => {
  console.log('Attempting to change status for ID:', id);
  try {
    const res = await modifyStatusSanPham(id); // Gọi API thay đổi trạng thái
    emit('changeStatus'); // Phát sự kiện lên component cha để nó tải lại dữ liệu bảng
    toast.success(res.message || 'Cập nhật trạng thái thành công!');
  } catch (error: any) {
    console.error('Lỗi khi thay đổi trạng thái sản phẩm:', error);
    if (error?.response?.data?.message) {
      toast.error(error?.response?.data?.message);
    } else {
      toast.error('Có lỗi xảy ra khi thay đổi trạng thái sản phẩm.');
    }
  }
};

// --- Hàm xử lý chuyển trang bảng ---
const handlePageChange = (pagination: any) => {
  emit('page-change', { page: pagination.current, pageSize: pagination.pageSize });
  selectedRowKeys.value = []; // Reset lựa chọn khi chuyển trang để tránh lỗi dữ liệu cũ
};

// --- Hàm xử lý click xem chi tiết sản phẩm ---
const handleClick = (idSanPham: string) => {
  console.log('Viewing details for ID:', idSanPham);
  emit('view', idSanPham);
};

// --- Hàm xử lý click thêm sản phẩm chi tiết ---
const handleAddClick = (idSP: string) => {
  if (idSP) {
    console.log('Navigating to add new product detail for SP ID:', idSP);
    router.push({
      name: 'them-san-pham-chi-tiet-admin', // Đảm bảo route name này tồn tại trong router của bạn
      query: { id: idSP } // Truyền id sản phẩm chính qua query param
    });
  } else {
    toast.error('Không tìm thấy ID sản phẩm chính để thêm chi tiết.');
  }
};

// --- Hàm tạo và tải QR Code cho một ID cụ thể ---
const generateAndDownloadQr = async (id: string) => {
  try {
    const canvas = document.createElement('canvas');
    // Dữ liệu trong QR là ID của sản phẩm
    await QRCode.toCanvas(canvas, id, { width: 300, margin: 4, color: { dark: '#000', light: '#fff' } });
    const link = document.createElement('a');
    link.href = canvas.toDataURL('image/png');
    link.download = `qr-sanpham-${id}.png`; // Tên file tải về
    document.body.appendChild(link); // Tạm thời thêm link vào body để kích hoạt click
    link.click(); // Kích hoạt sự kiện click để tải file
    document.body.removeChild(link); // Xóa link khỏi body sau khi tải
  } catch (error) {
    console.error(`Lỗi khi tạo hoặc tải QR cho ID ${id}:`, error);
    toast.error(`Không thể tạo QR cho sản phẩm có ID: ${id}`);
  }
};

// --- Hàm xử lý khi click nút "Tải QR đã chọn" ---
const handleDownloadSelectedQr = async () => {
  if (selectedRowKeys.value.length === 0) {
    toast.warn('Vui lòng chọn ít nhất một sản phẩm để tải QR.');
    return;
  }

  // Tải từng QR code một cách tuần tự
  for (const id of selectedRowKeys.value) {
    await generateAndDownloadQr(id);
  }
  toast.success(`Đã hoàn tất tải QR cho ${selectedRowKeys.value.length} sản phẩm.`);
  selectedRowKeys.value = []; // Xóa lựa chọn sau khi tải xong
};
</script>

<style scoped lang="scss">
.color-box {
  width: 25px;
  height: 25px;
  border-radius: 4px;
  border: 1px solid #ccc;
  margin-right: 8px;
  /* Khoảng cách giữa ô màu và tên màu */
}

.center-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.anh {
  object-fit: cover;
  /* Đảm bảo ảnh không bị méo */
}

/* Style cho nút Download QR */
.ant-btn-primary[style*="background-color: #ff7f32;"] {
  background-color: #ff7f32;
  border-color: #ff7f32;
}

.ant-btn-primary[style*="background-color: #ff7f32;"]:hover,
.ant-btn-primary[style*="background-color: #ff7f32;"]:focus {
  background-color: #e67e22;
  /* Màu đậm hơn khi hover/focus */
  border-color: #e67e22;
}

:deep(.ant-table-wrapper .ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #1890ff;
  border-color: #1890ff;
}

:deep(.ant-table-wrapper .ant-checkbox-input:focus + .ant-checkbox-inner),
:deep(.ant-table-wrapper .ant-checkbox-wrapper:hover .ant-checkbox-inner) {
  border-color: #1890ff;
}

:deep(.ant-table-wrapper .ant-checkbox-disabled .ant-checkbox-inner) {
  background-color: #f5f5f5;
  border-color: #d9d9d9;

}

:deep(.ant-checkbox-checked::after) {
  border-color: #fff !important;
}

:deep(.ant-tabs-tab.ant-tabs-tab-active) {
  background-color: transparent !important;
  background: transparent !important;
  border-bottom: none !important;

  .ant-tabs-tab-btn {
    color: #1890ff !important;
  }
}


:deep(.ant-tabs-ink-bar) {
  background: #1890ff !important;
  height: 2px !important;
  bottom: 0px !important;
}


:deep(.ant-tabs-tab:hover) {
  .ant-tabs-tab-btn {
    color: #1890ff !important;
  }
}


:deep(.ant-tabs-nav-wrap::after) {
  background-color: transparent !important;
}


/* CSS cho Checkbox trong Ant Design Table */
:deep(.ant-table-wrapper .ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #1890ff !important;
  border-color: #1890ff !important;
}

:deep(.ant-table-wrapper .ant-checkbox-input:focus + .ant-checkbox-inner),
:deep(.ant-table-wrapper .ant-checkbox-wrapper:hover .ant-checkbox-inner) {
  border-color: white !important;
}


:deep(.ant-table-wrapper .ant-table-tbody > tr.ant-table-row-selected > td) {
  background: transparent !important;
}
</style>