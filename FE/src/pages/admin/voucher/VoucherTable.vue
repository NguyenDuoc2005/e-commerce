<template>
  <DivCustom label="Danh sách voucher sàn" customClasses="mt-5">
    <div class="table-toolbar">
      <div></div>
      <a-tooltip title="Thêm voucher sàn">
        <a-button
          style="background-color: #54bddb;"
          type="primary"
          @click="handleAddClick"
          class="d-flex justify-content-center align-items-center px-4 add-customer-btn"
        >
          <PlusCircleOutlined /> Thêm voucher sàn
        </a-button>
      </a-tooltip>
    </div>

    <div class="min-h-[300px]">
      <a-table
        :columns="columns"
        :data-source="products"
        :pagination="{
          current: paginationParams.page,
          pageSize: paginationParams.size,
          total: totalItems,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '30', '40', '50']
        }"
        :scroll="{ y: 300 }"
        @change="handlePageChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'">
              {{ record.status === 'ACTIVE' ? 'Đang hoạt động' : 'Ngừng hoạt động' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'loaiGiam'">
            <a-tag :color="record.loaiGiam === false ? 'green' : 'blue'">
              {{ record.loaiGiam === false ? 'Công khai' : 'Cá nhân' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'phanTramGiam'">
            {{ record.kieuGiam === true ? record.phanTramGiam + '%' : formatCurrencyVND(record.phanTramGiam) }}
          </template>

          <div v-if="column.key === 'stt'">
            {{ products.indexOf(record) + 1 }}
          </div>
          <template v-if="column.key === 'operation'">
            <div class="d-flex justify-content-center align-items-center gap-1 w-100 h-100">
              <a-tooltip v-if="record.status === 'INACTIVE' && record.loaiGiam === false" title="Chỉnh sửa voucher sàn">
                <a-button
                  style="background-color: #54bddb;"
                  type="primary"
                  @click="handleViewClick(record.id)"
                  class="p-2 d-flex justify-content-center align-items-center add-customer-btn"
                >
                  <EditOutlined />
                </a-button>
              </a-tooltip>

              <a-tooltip title="Đổi trạng thái voucher sàn">
                <a-popconfirm
                  title="Bạn có chắc chắn muốn thay đổi trạng thái không?"
                  @confirm="handleChangeStatusClick(record.id)"
                  ok-text="Đồng ý"
                  cancel-text="Huỷ"
                >
                  <a-button
                    style="background-color: #9b6dc7;"
                    type="primary"
                    class="p-2 d-flex justify-content-center align-items-center add-customer-btn1"
                  >
                    <RedoOutlined />
                  </a-button>
                </a-popconfirm>
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
import { EditOutlined, PlusCircleOutlined, RedoOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import { defineEmits, defineProps } from 'vue'
import { modifyStatusSize } from '@/services/api/admin/voucher.api'
import { toast } from 'vue3-toastify'

defineProps<{
  paginationParams: { page: number; size: number }
  totalItems: number
  products: any[]
}>()

const emit = defineEmits(['page-change', 'add', 'view', 'changeStatus'])

const columns: TableColumnsType = [
  { title: 'STT', key: 'stt', dataIndex: 'stt', width: 80, align: 'center' },
  { title: 'Mã voucher', key: 'ma', dataIndex: 'ma', width: 100, align: 'center' },
  { title: 'Tên voucher', key: 'ten', dataIndex: 'ten', width: 140, align: 'center' },
  {
    title: 'Điều kiện giảm',
    key: 'dieuKien',
    dataIndex: 'dieuKien',
    width: 120,
    align: 'center',
    customRender: ({ text }) => formatCurrencyVND(text)
  },
  {
    title: 'Giá trị giảm',
    key: 'phanTramGiam',
    dataIndex: 'phanTramGiam',
    width: 120,
    align: 'center'
  },
  {
    title: 'Loại voucher',
    key: 'loaiGiam',
    dataIndex: 'loaiGiam',
    width: 120,
    align: 'center'
  },
  { title: 'Số lượng', key: 'soLuongPhieu', dataIndex: 'soLuongPhieu', width: 100, align: 'center' },
  {
    title: 'Ngày bắt đầu',
    key: 'ngayBatDau',
    dataIndex: 'ngayBatDau',
    width: 110,
    align: 'center',
    customRender: ({ text }) => formatDate(text)
  },
  {
    title: 'Ngày kết thúc',
    key: 'ngayKetThuc',
    dataIndex: 'ngayKetThuc',
    width: 110,
    align: 'center',
    customRender: ({ text }) => formatDate(text)
  },
  { title: 'Trạng thái', key: 'status', dataIndex: 'status', width: 150, align: 'center' },
  { title: 'Hành động', key: 'operation', width: 150, align: 'center' }
]

const handlePageChange = (pagination: any) => {
  emit('page-change', { page: pagination.current, pageSize: pagination.pageSize })
}

const handleAddClick = () => {
  emit('add')
}

const formatCurrencyVND = (amount: number) => {
  if (typeof amount !== 'number') return amount
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount)
}

const formatDate = (date: string | Date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const handleChangeStatusClick = async (id: string) => {
  try {
    const res = await modifyStatusSize(id)
    emit('changeStatus')
    toast.success(res.message)
  } catch (error: any) {
    console.log(error)
    if (error?.response?.data?.message) {
      toast.error(error.response.data.message)
    }
  }
}

const handleViewClick = (id: string) => {
  emit('view', id)
}
</script>

<style scoped lang="scss">
.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.add-customer-btn,
.add-customer-btn1 {
  transition: all 0.3s ease;
}

.add-customer-btn:hover {
  background-color: #3aa8c1 !important;
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.add-customer-btn1:hover {
  background-color: #bc33ce !important;
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}
</style>
