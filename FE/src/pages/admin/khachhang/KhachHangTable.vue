<template>
  <DivCustom label="Danh sách người dùng" customClasses="mt-5">
    <div class="table-toolbar">
      <div></div>
      <a-tooltip title="Thêm người dùng hỗ trợ">
        <a-button
          style="background-color: #54bddb;"
          type="primary"
          class="add-customer-btn px-4 d-flex justify-content-center align-items-center"
          @click="handleAddClick"
        >
          <PlusCircleOutlined /> Thêm người dùng hỗ trợ
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
          <div v-if="column.key === 'stt'">
            {{ products.indexOf(record) + 1 }}
          </div>

          <template v-if="column.key === 'avatar'">
            <div class="center-cell">
              <img :src="record.avatar" class="avatar" />
            </div>
          </template>

          <template v-if="column.key === 'createdDate'">
            {{ formatDate(record.createdDate) }}
          </template>

          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'">
              {{ record.status === 'ACTIVE' ? 'Kích hoạt' : 'Ngừng kích hoạt' }}
            </a-tag>
          </template>

          <template v-if="column.key === 'sellerStatus'">
            <div class="seller-status-cell">
              <a-tag :color="sellerStatusColor(record.sellerStatus)">
                {{ sellerStatusText(record.sellerStatus) }}
              </a-tag>
              <span v-if="record.sellerShopName" class="shop-name">{{ record.sellerShopName }}</span>
            </div>
          </template>

          <template v-if="column.key === 'operation'">
            <div class="d-flex gap-1 justify-center">
              <a-tooltip title="Chỉnh sửa người dùng">
                <a-button
                  style="background-color: #54bddb;"
                  type="primary"
                  @click="handleViewClick(record.id)"
                  class="p-2 d-flex justify-content-center align-items-center add-customer-btn"
                >
                  <EditOutlined />
                </a-button>
              </a-tooltip>
              <a-tooltip title="Đổi trạng thái người dùng">
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
import { modifyStatusKhachHang } from '@/services/api/admin/khachhang.api'
import { useRouter } from 'vue-router'
import { toast } from 'vue3-toastify'

defineProps<{
  paginationParams: { page: number; size: number }
  totalItems: number
  products: any[]
}>()

const router = useRouter()
const emit = defineEmits(['page-change', 'add', 'view', 'changeStatus'])

const columns: TableColumnsType = [
  { title: 'STT', key: 'stt', dataIndex: 'stt', width: 80, align: 'center' },
  { title: 'Mã người dùng', key: 'ma', dataIndex: 'ma', width: 150, align: 'center' },
  { title: 'Tên người dùng', key: 'ten', dataIndex: 'ten', width: 180, align: 'center' },
  { title: 'Số điện thoại', key: 'sdt', dataIndex: 'sdt', width: 150, align: 'center' },
  { title: 'Trạng thái seller', key: 'sellerStatus', dataIndex: 'sellerStatus', width: 180, align: 'center' },
  { title: 'Ngày tham gia', key: 'createdDate', dataIndex: 'createdDate', width: 150, align: 'center' },
  { title: 'Trạng thái', key: 'status', dataIndex: 'status', width: 150, align: 'center' },
  { title: 'Hành động', key: 'operation', width: 100, align: 'center' }
]

const handlePageChange = (pagination: any) => {
  emit('page-change', { page: pagination.current, pageSize: pagination.pageSize })
}

const handleAddClick = () => {
  router.push({ name: 'them-khach-hang-admin' })
}

const formatDate = (timestamp: number) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const options: Intl.DateTimeFormatOptions = { year: 'numeric', month: '2-digit', day: '2-digit' }
  return date.toLocaleString('vi-VN', options)
}

const sellerStatusColor = (status?: string | null) => {
  switch (status) {
    case 'APPROVED':
      return 'green'
    case 'PENDING_APPROVAL':
      return 'orange'
    case 'REJECTED':
      return 'red'
    case 'SUSPENDED':
      return 'volcano'
    case 'CLOSED':
      return 'default'
    default:
      return 'blue'
  }
}

const sellerStatusText = (status?: string | null) => {
  switch (status) {
    case 'APPROVED':
      return 'Đã duyệt'
    case 'PENDING_APPROVAL':
      return 'Chờ duyệt'
    case 'REJECTED':
      return 'Từ chối'
    case 'SUSPENDED':
      return 'Tạm khóa'
    case 'CLOSED':
      return 'Đã đóng'
    case 'DRAFT':
      return 'Nháp'
    default:
      return 'Chưa đăng ký shop'
  }
}

const handleChangeStatusClick = async (id: string) => {
  try {
    const res = await modifyStatusKhachHang(id)
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
  router.push({
    name: 'them-khach-hang-admin',
    query: { id }
  })
}
</script>

<style scoped lang="scss">
.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.center-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
}

.seller-status-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.shop-name {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #667085;
  font-size: 12px;
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
