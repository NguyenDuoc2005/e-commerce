<template>
  <DivCustom label="Danh sách quản trị viên" customClasses="mt-5">
    <div class="table-toolbar">
      <div></div>
      <a-tooltip title="Thêm nhân sự vận hành">
        <a-button
          style="background-color: #54bddb;"
          type="primary"
          @click="handleAddClick"
          class="d-flex justify-content-center align-items-center px-4"
        >
          <PlusCircleOutlined /> Thêm nhân sự vận hành
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

          <template v-else-if="column.key === 'avatar'">
            <div class="center-cell">
              <img :src="record.avatar" class="avatar" />
            </div>
          </template>

          <template v-else-if="column.key === 'stt'">
            {{ products.indexOf(record) + 1 }}
          </template>

          <template v-else-if="column.key === 'createdDate'">
            {{ formatDate(record.createdDate) }}
          </template>

          <template v-else-if="column.key === 'role'">
            <a-tag :color="record.role === 'ADMIN' ? 'blue' : 'cyan'">
              {{ roleLabel(record.role) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'operation'">
            <div class="d-flex gap-1 justify-content-center align-items-center w-100 h-100">
              <a-tooltip title="Chỉnh sửa quản trị viên">
                <a-button
                  style="background-color: #54bddb;"
                  type="primary"
                  @click="handleViewClick(record.id)"
                  class="p-2 d-flex justify-content-center align-items-center"
                >
                  <EditOutlined />
                </a-button>
              </a-tooltip>

              <a-tooltip title="Đổi trạng thái quản trị viên">
                <a-popconfirm
                  title="Bạn có chắc chắn muốn thay đổi trạng thái không?"
                  @confirm="handleChangeStatusClick(record.id)"
                  ok-text="Đồng ý"
                  cancel-text="Huỷ"
                >
                  <a-button
                    style="background-color: #9b6dc7;"
                    type="primary"
                    class="p-2 d-flex justify-content-center align-items-center"
                  >
                    <RedoOutlined />
                  </a-button>
                </a-popconfirm>
              </a-tooltip>
            </div>
          </template>
          <template v-else-if="column.dataIndex">
            {{ record[column.dataIndex] ?? '—' }}
          </template>
        </template>
      </a-table>
    </div>
  </DivCustom>
</template>

<script setup lang="ts">
import DivCustom from '@/components/custom/Div/DivCustomTable.vue'
import { modifyStatusMember } from '@/services/api/admin/nhanvien.api'
import { EditOutlined, PlusCircleOutlined, RedoOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import { defineEmits, defineProps } from 'vue'
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
  { title: 'Mã quản trị viên', key: 'ma', dataIndex: 'ma', width: 150, align: 'center' },
  { title: 'Tên quản trị viên', key: 'ten', dataIndex: 'ten', width: 170, align: 'center' },
  { title: 'Email', key: 'email', dataIndex: 'email', width: 170, align: 'center' },
  { title: 'Số điện thoại', key: 'sdt', dataIndex: 'sdt', width: 130, align: 'center' },
  { title: 'Vai trò platform', key: 'role', dataIndex: 'role', width: 160, align: 'center' },
  { title: 'Ngày tham gia', key: 'createdDate', dataIndex: 'createdDate', width: 130, align: 'center' },
  { title: 'Trạng thái', key: 'status', dataIndex: 'status', width: 150, align: 'center' },
  { title: 'Hành động', key: 'operation', width: 100, align: 'center' }
]

const formatDate = (timestamp: number) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const options: Intl.DateTimeFormatOptions = { year: 'numeric', month: '2-digit', day: '2-digit' }
  return date.toLocaleString('vi-VN', options)
}

const roleLabel = (role?: string) => role === 'ADMIN'
  ? 'Quản trị viên cấp cao'
  : 'Nhân sự vận hành'

const handlePageChange = (pagination: any) => {
  emit('page-change', { page: pagination.current, pageSize: pagination.pageSize })
}

const handleAddClick = () => {
  router.push({ name: 'them-nhan-vien-admin' })
}

const handleChangeStatusClick = async (id: string) => {
  try {
    const res = await modifyStatusMember(id)
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
    name: 'them-nhan-vien-admin',
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

:deep(.ant-btn),
:deep(.ant-btn-primary) {
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: scale(1);
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
  }
}
</style>
