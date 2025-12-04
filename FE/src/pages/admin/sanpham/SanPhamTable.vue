<template>
    <DivCustom label="Danh sách sản phẩm" customClasses="mt-5">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <div style="font-size: 13px; color: #5FB3B3; margin-left: 15px;">
            
            </div>

            <div>
                <a-tooltip title="Thêm sản phẩm">
                    <a-button style="background-color: #54bddb;" type="primary" @click="handleAddClick"
                        class="d-flex justify-content-center align-items-center px-4">
                        <PlusCircleOutlined /> Thêm mới sản phẩm
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
                            {{ record.status == 'ACTIVE' ? 'Đang kinh doanh' : 'Ngừng kinh doanh' }}
                        </a-tag>
                    </template>

                    <div v-if="column.key === 'stt'">
                        {{ products.indexOf(record) + 1 }}
                    </div>

                    <div v-if="column.key === 'tongSP'">
                        {{ record.tongSP === null ? 0 : record.tongSP }}
                    </div>

                    <template v-if="column.key === 'gioiTimh'">
                        <a-tag :color="record.gioiTimh ? 'red' : 'yellow'">
                            {{ record.gioiTimh ? 'Nam' : 'Nữ' }}
                        </a-tag>
                    </template>

                    <template v-if="column.key === 'ngaySinh'">
                        {{ record.ngaySinh ? record.ngaySinh.split('T')[0] : '' }}
                    </template>

                    <template v-if="column.key === 'operation'">
                        <div class="d-flex gap-1 justify-center">
                            <a-tooltip title="Chỉnh sửa sản phẩm">
                                <a-button style="background-color: #54bddb;" type="primary"
                                    @click="handleViewClick(record.id)"
                                    class="p-2 d-flex justify-content-center align-items-center">
                                    <EditOutlined />
                                </a-button>
                            </a-tooltip>
                            <a-popconfirm title="Bạn có chắc chắn muốn thay đổi trạng thái không?"
                                @confirm="handleChangeStatusClick(record.id)" ok-text="Đồng ý" cancel-text="Huỷ">

                                <a-tooltip title="Đổi trạng thái sản phẩm">
                                    <a-button style="background-color: #9b6dc7;" type="primary"

                                        class="p-2 d-flex justify-content-center align-items-center">
                                        <RedoOutlined />
                                    </a-button>
                                </a-tooltip>
                            </a-popconfirm>
                            <a-tooltip title="Chi tiết sản phẩm">
                                <a-button style="background-color: #dd9bb1;" type="primary"
                                    @click="handleClick(record.id)"
                                    class="p-2 d-flex justify-content-center align-items-center">
                                    <SearchOutlined />
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
//   import DivCustom from '@/components/custom/Div/DivCustom.vue'
import DivCustom from '@/components/custom/Div/DivCustomTable.vue'
import { EditOutlined, PlusCircleOutlined, RedoOutlined, SearchOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import { defineEmits, defineProps, h } from 'vue'
import { modifyStatusKhachHang } from '@/services/api/admin/khachhang.api'
import { useRouter } from 'vue-router'
import { toast } from 'vue3-toastify'
import { modifyStatusSanPham } from '@/services/api/admin/sanpham.api'
import { ROUTES_CONSTANTS } from '@/constants/path'

defineProps<{
    paginationParams: { page: number; size: number }
    totalItems: number
    products: any[]
}>()

const router = useRouter()

const emit = defineEmits(['page-change', 'add', 'view', 'changeStatus'])

const columns: TableColumnsType = [
    { title: 'STT', key: 'stt', dataIndex: 'stt', width: 60, align: 'center' },
    { title: 'Mã sản phẩm', key: 'ma', dataIndex: 'ma', width: 80, align: 'center' },
    { title: 'Tên sản phẩm', key: 'ten', dataIndex: 'ten', width: 80, align: 'center' },
    { title: 'Thương hiệu', key: 'tenThuongHieu', dataIndex: 'tenThuongHieu', width: 90, align: 'center' },
    { title: 'Loại đế', key: 'tenLoaiDe', dataIndex: 'tenLoaiDe', width: 100, align: 'center' },
    { title: 'Danh mục', key: 'tenDanhMuc', dataIndex: 'tenDanhMuc', width: 100, align: 'center' },
    { title: 'Chất liệu', key: 'tenChatLieu', dataIndex: 'tenChatLieu', width: 100, align: 'center' },
    { title: 'Số lượng', key: 'tongSP', dataIndex: 'tongSP', width: 60, align: 'center' },
    { title: 'Trạng thái', key: 'status', dataIndex: 'status', width: 100, align: 'center' },
    {
        title: 'Hành động',
        key: 'operation',
        width: 90,
        align: 'center'
    }
]

const handleChangeStatusClick = async (id: string) => {
    try {
        const res = await modifyStatusSanPham(id);
        emit('changeStatus');

        toast.success(res.message);

    } catch (error) {
        console.log(error);

        if (error?.response?.data?.message) {
            toast.error(error?.response?.data?.message);
        }
    }

}

const handlePageChange = (pagination: any) => {
    emit('page-change', { page: pagination.current, pageSize: pagination.pageSize })
} 

const handleClick = (idSanPham: string) => {
    if (idSanPham) {
        console.log('idSanPham', idSanPham);
        router.push({
            name: 'san-pham-chi-tiet-admin',
            query: { id: idSanPham }
        });
    }
};



const handleAddClick = () => {

    router.push({
        name: 'them-san-pham-chi-tiet-admin',
    });

};


const handleViewClick = (id: string) => {
    emit('view', id)
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