<template>
    <DivCustom label="Danh sách khách hàng" customClasses="mt-5">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <div style="font-size: 13px; color: #5FB3B3; margin-left: 15px;">
             
            </div>

            <div>
                <a-tooltip title="Thêm khách hàng">
                    <a-button style="background-color: #54bddb;" type="primary"
                        class="add-customer-btn px-4 d-flex justify-content-center align-items-center"
                        @click="handleAddClick">
                        <PlusCircleOutlined /> Thêm mới khách hàng
                    </a-button>
                </a-tooltip>
            </div>
        </div>
        <div class="min-h-[300px] ">
            <a-table :columns="columns" :data-source="products" :pagination="{
                current: paginationParams.page,
                pageSize: paginationParams.size,
                total: totalItems,
                showSizeChanger: true,
                pageSizeOptions: ['10', '20', '30', '40', '50']
            }" :scroll="{ y: 300 }" @change="handlePageChange">
                <template #bodyCell="{ column, record }">

                    <div v-if="column.key === 'stt'">
                        {{ products.indexOf(record) + 1 }}
                    </div>

                    <template v-if="column.key === 'avatar'">
                        <div class="center-cell">
                            <img :src="record.avatar" class="avatar"
                                style="width: 50px; height: 50px; border-radius: 50%" />
                        </div>
                    </template>

                    <template v-if="column.key === 'createdDate'">
                        {{ formatDate(record.createdDate) }}
                    </template>

                    <template v-if="column.key === 'status'">
                        <a-tag :color="record.status == 'ACTIVE' ? 'green' : 'red'">
                            {{ record.status == 'ACTIVE' ? 'Kích hoạt' : 'Ngừng kích hoạt' }}
                        </a-tag>
                    </template>

                    <template v-if="column.key === 'operation'">
                        <div class="d-flex gap-1 justify-center">
                            <a-tooltip title="Chỉnh sửa khách hàng">
                                <a-button style="background-color: #54bddb;" type="primary"
                                    @click="handleViewClick(record.id)"
                                    class="p-2 d-flex justify-content-center align-items-center add-customer-btn ">
                                    <EditOutlined />
                                </a-button>
                            </a-tooltip>
                            <a-tooltip title="Đổi trạng thái khách hàng">
                                <a-popconfirm title="Bạn có chắc chắn muốn thay đổi trạng thái không?"
                                    @confirm="handleChangeStatusClick(record.id)" ok-text="Đồng ý" cancel-text="Huỷ">
                                    <a-button style="background-color: #9b6dc7;" type="primary"
                                        class="p-2 d-flex justify-content-center align-items-center add-customer-btn1 ">
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
//   import DivCustom from '@/components/custom/Div/DivCustom.vue'
import DivCustom from '@/components/custom/Div/DivCustomTable.vue'
import { EditOutlined, PlusCircleOutlined, RedoOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import { defineEmits, defineProps, h } from 'vue'
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
    { title: 'STT', key: 'stt', dataIndex: 'stt', width: 150, align: 'center' },
    { title: 'Mã khách hàng', key: 'ma', dataIndex: 'ma', width: 150, align: 'center' },
    { title: 'Tên khách hàng', key: 'ten', dataIndex: 'ten', width: 150, align: 'center' },
    { title: 'Số điện thoại', key: 'sdt', dataIndex: 'sdt', width: 150, align: 'center' },
    { title: 'Ngày tham gia', key: 'createdDate', dataIndex: 'createdDate', width: 150, align: 'center' },
    { title: 'Trạng thái', key: 'status', dataIndex: 'status', width: 150, align: 'center' },
    {
        title: 'Hành động',
        key: 'operation',
        width: 80,
        align: 'center'
    }
]

const handlePageChange = (pagination: any) => {
    emit('page-change', { page: pagination.current, pageSize: pagination.pageSize })
}

const handleAddClick = () => {
    router.push({
        name: 'them-khach-hang-admin',
    });

}

const formatDate = (timestamp: number) => {
    const date = new Date(timestamp);
    const options: Intl.DateTimeFormatOptions = { year: 'numeric', month: '2-digit', day: '2-digit' }; // Chỉ lấy ngày tháng năm
    return date.toLocaleString('vi-VN', options);
}

const handleChangeStatusClick = async (id: string) => {
    try {
        const res = await modifyStatusKhachHang(id);
        emit('changeStatus');

        toast.success(res.message);

    } catch (error) {
        console.log(error);

        if (error?.response?.data?.message) {
            toast.error(error?.response?.data?.message);
        }
    }

}

const handleViewClick = (id: string) => {
    router.push({
        name: 'them-khach-hang-admin',
        query: { id: id }
    });
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

.add-customer-btn {
    transition: all 0.3s ease;
}

.add-customer-btn:hover {
    background-color: #3aa8c1 !important;
    transform: scale(1.05);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.add-customer-btn1 {
    transition: all 0.3s ease;
}

.add-customer-btn1:hover {
    background-color: #bc33ce !important;
    transform: scale(1.05);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}
</style>