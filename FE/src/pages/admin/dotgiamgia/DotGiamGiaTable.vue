<template>
  <DivCustom label="Danh sách đợt giảm giá" customClasses="mt-5">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <div style="font-size: 13px; margin-left: 15px;color: #58bddb;">

      </div>

      <div>
        <a-tooltip title="Thêm đợt giảm giá">
          <a-button style="background-color: #54bddb;" type="primary" @click="handleAddClick"
            class="d-flex justify-content-center align-items-center px-4">
            <PlusCircleOutlined /> Thêm mới đợt giảm giá
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
      }" :scroll="{ y: 240 }" @change="handlePageChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'trangThai'">
            <a-tag
              :color="record.trangThai === 'CHUA_KICH_HOAT' ? 'green' : (record.trangThai === 'DANG_KICH_HOAT' ? 'blue' : 'red')">
              {{
                record.trangThai === 'CHUA_KICH_HOAT' ? 'Chưa kích hoạt' :
                  (record.trangThai === 'DANG_KICH_HOAT' ? 'Đang kích hoạt' : 'Hết hạn kích hoạt')
              }}
            </a-tag>
          </template>

          <template v-if="column.key === 'stt'">
            {{ products.indexOf(record) + 1 }}
          </template>

          <template v-if="column.key === 'phanTramGiam'">
            {{ record.phanTramGiam }}%
          </template>

          <template v-if="column.key === 'ngayBatDau'">
            {{ formatDate(record.ngayBatDau) }}
          </template>

          <template v-if="column.key === 'ngayKetThuc'">
            {{ formatDate(record.ngayKetThuc) }}
          </template>

          <template v-if="column.key === 'operation'">
            <div class="d-flex gap-1 justify-content-center align-items-center w-100 h-100">
              <template v-if="record.trangThai !== 'HET_HAN_KICH_HOAT'">
                <a-tooltip title="Cập nhật đợt giảm giá">
                  <a-button type="primary" @click="handleViewClick(record.id)"
                    class="p-2 d-flex justify-content-center align-items-center"
                    style="background-color: #54bddb; border-color: #096dd9; color: white;">
                    <EditOutlined style="font-size: 18px;" />
                  </a-button>
                </a-tooltip>
              </template>
            </div>
          </template>

        </template>
      </a-table>
    </div>
  </DivCustom>
</template>

<script setup lang="ts">
import DivCustom from '@/components/custom/Div/DivCustomTable.vue'
import { EditOutlined, EyeOutlined, PlusCircleOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import { defineEmits, defineProps } from 'vue'
import { useRouter } from 'vue-router'

defineProps<{
  paginationParams: { page: number; size: number }
  totalItems: number
  products: any[]
}>()

const router = useRouter()
const emit = defineEmits(['page-change', 'add', 'view', 'changeStatus'])

const columns: TableColumnsType = [
  { title: 'STT', key: 'stt', dataIndex: 'stt', width: 70, align: 'center' },
  { title: 'Mã', key: 'ma', dataIndex: 'ma', width: 100, align: 'center' },
  { title: 'Tên đợt', key: 'ten', dataIndex: 'ten', width: 150, align: 'center' },
  { title: 'Giá trị giảm', key: 'phanTramGiam', dataIndex: 'phanTramGiam', width: 90, align: 'center' },
  { title: 'Ngày bắt đầu', key: 'ngayBatDau', dataIndex: 'ngayBatDau', width: 150, align: 'center' },
  { title: 'Ngày kết thúc', key: 'ngayKetThuc', dataIndex: 'ngayKetThuc', width: 150, align: 'center' },
  { title: 'Trạng thái', key: 'trangThai', dataIndex: 'trangThai', width: 130, align: 'center' },
  { title: 'Hành động', key: 'operation', width: 100, align: 'center' }
]

const handlePageChange = (pagination: any) => {
  emit('page-change', { page: pagination.current, pageSize: pagination.pageSize })
}

const handleAddClick = () => {
  router.push({ name: 'add-dot-giam-gia-admin' })
}

const handleViewClick = (id: string) => {
  router.push({ name: 'update-dot-giam-gia-admin', params: { id } })
}

const formatDate = (timestamp: number) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${day}/${month}/${year} ${hours}:${minutes}:${seconds}`
}
</script>

<style scoped lang="scss">
.color {
  border-radius: 50%;
}

.center-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>