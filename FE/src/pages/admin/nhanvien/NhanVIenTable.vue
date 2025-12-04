<template>
    <DivCustom label="Danh sách nhân viên" customClasses="mt-5">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <div style="font-size: 13px; color: #5FB3B3; margin-left: 15px;">

            </div>

            <div>
                <a-tooltip title="Thêm nhân viên">
                    <a-button style="background-color: #54bddb;" type="primary" @click="handleAddClick"
                        class="d-flex justify-content-center align-items-center px-4">
                        <PlusCircleOutlined /> Thêm mới nhân viên
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
                    <template v-if="column.key === 'status'">
                        <a-tag :color="record.status == 'ACTIVE' ? 'green' : 'red'">
                            {{ record.status == 'ACTIVE' ? 'Đang làm việc' : 'Nghỉ việc' }}
                        </a-tag>
                    </template>

                    <template v-if="column.key === 'avatar'">
                        <div class="center-cell">
                            <img :src="record.avatar" class="avatar"
                                style="width: 50px; height: 50px; border-radius: 50%" />
                        </div>
                    </template>

                    <!-- <template v-if="column.key === 'vaitro'">
                        <a-tag :color="record.vaitro == 'NHAN_VIEN' ? 'green' : 'yellow'">
                            {{ record.chucVu == 'STAFF' ? 'Nhân viên' : 'Quản lý' }}
                        </a-tag>
                    </template> -->

                    <div v-if="column.key === 'stt'">
                        {{ products.indexOf(record) + 1 }}
                    </div>

                    <!-- Hiển thị ngày tháng cho createdDate -->
                    <template v-if="column.key === 'createdDate'">
                        {{ formatDate(record.createdDate) }}
                    </template>

                    <template v-if="column.key === 'operation'">
                        <div class="d-flex gap-1 justify-content-center align-items-center w-100 h-100">
                            <a-tooltip title="Chỉnh sửa nhân viên">
                                <a-button style="background-color: #54bddb;" type="primary"
                                    @click="handleViewClick(record.id)"
                                    class="p-2 d-flex justify-content-center align-items-center">
                                    <EditOutlined />
                                </a-button>
                            </a-tooltip>

                            <a-tooltip title="Đổi trạng thái nhân viên">
                                <a-popconfirm title="Bạn có chắc chắn muốn thay đổi trạng thái không?"
                                    @confirm="handleChangeStatusClick(record.id)" ok-text="Đồng ý" cancel-text="Huỷ">
                                    <a-button style="background-color: #9b6dc7;" type="primary"
                                        class="p-2 d-flex justify-content-center align-items-center">
                                        <RedoOutlined />
                                    </a-button>
                                </a-popconfirm>
                            </a-tooltip>

                            <!-- <a-tooltip title="Đổi vai trò">
                                <a-popconfirm title="Bạn có chắc chắn muốn thay đổi vai trò không?"
                                    @confirm="handleChangeRoleClick(record.id)" ok-text="Đồng ý" cancel-text="Huỷ">
                                    <a-button style="background-color: #dd9bb1;" type="primary"
                                        class="p-2 d-flex justify-content-center align-items-center">
                                        <UserSwitchOutlined />
                                    </a-button>
                                </a-popconfirm>
                            </a-tooltip> -->
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
import { changeStatusMember, modifyStatusMember } from '@/services/api/admin/nhanvien.api'
import { EditOutlined, PlusCircleOutlined, RedoOutlined, UserSwitchOutlined } from '@ant-design/icons-vue'
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
    { title: 'Mã nhân viên', key: 'ma', dataIndex: 'ma', width: 150, align: 'center' },
    { title: 'Tên nhân viên', key: 'ten', dataIndex: 'ten', width: 150, align: 'center' },
    { title: 'Email', key: 'email', dataIndex: 'email', width: 150, align: 'center' },
    { title: 'Số điện thoại', key: 'sdt', dataIndex: 'sdt', width: 120, align: 'center' },
    // { title: 'Vai trò', key: 'vaitro', dataIndex: 'vaiTro', width: 100, align: 'center' },
    { title: 'Ngày tham gia', key: 'createdDate', dataIndex: 'createdDate', width: 130, align: 'center' },
    { title: 'Trạng thái', key: 'status', dataIndex: 'status', width: 150, align: 'center' },
    {
        title: 'Hành động',
        key: 'operation',
        width: 80,
        align: 'center'
    }
]

const formatDate = (timestamp: number) => {
    const date = new Date(timestamp);
    const options: Intl.DateTimeFormatOptions = { year: 'numeric', month: '2-digit', day: '2-digit' }; // Chỉ lấy ngày tháng năm
    return date.toLocaleString('vi-VN', options);
}
const handlePageChange = (pagination: any) => {
    emit('page-change', { page: pagination.current, pageSize: pagination.pageSize })
}

const handleAddClick = () => {
    router.push({
        name: 'them-nhan-vien-admin',
    });
}

const handleChangeStatusClick = async (id: string) => {
    try {
        const res = await modifyStatusMember(id);
        emit('changeStatus');
        toast.success(res.message);
    } catch (error) {
        console.log(error);
        if (error?.response?.data?.message) {
            toast.error(error?.response?.data?.message);
        }
    }
}

const handleChangeRoleClick = async (id: string) => {
    try {
        const res = await changeStatusMember(id);
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
        name: 'them-nhan-vien-admin',
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

:deep(.ant-btn) {
    transition: transform 0.2s ease, box-shadow 0.2s ease;

    &:hover {
        transform: scale(1.00);
        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
    }
}

:deep(.ant-btn-primary) {
    transition: transform 0.2s ease, box-shadow 0.2s ease;

    &:hover {
        transform: scale(1.00);
        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
    }
}
</style>
