<template>
    <DivCustom label="Danh sách danh mục" customClasses="mt-5">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <div style="font-size: 13px; color: #5FB3B3; margin-left: 15px;">
               
            </div>

            <div>
                <a-tooltip title="Thêm danh mục">
                    <a-button style="background-color: #54bddb;" type="primary" @click="handleAddClick"
                        class="d-flex justify-content-center align-items-center px-4">
                        <PlusCircleOutlined /> Thêm mới danh mục
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
                            {{ record.status == 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động' }}
                        </a-tag>
                    </template>
                    <div v-if="column.key === 'stt'">
                        {{ products.indexOf(record) + 1 }}
                    </div>
                    <template v-if="column.key === 'operation'">
                        <div class="d-flex gap-1 justify-center">
                            <a-tooltip title="Chỉnh sửa danh mục">
                                <a-button style="background-color: #54bddb;" type="primary"
                                    @click="handleViewClick(record.id)"
                                    class="p-2 d-flex justify-content-center align-items-center">
                                    <EditOutlined />
                                </a-button>
                            </a-tooltip>
                            <a-tooltip title="Chỉnh sửa danh mục">
                                <a-popconfirm title="Bạn có chắc chắn muốn thay đổi trạng thái không?"
                                    @confirm="handleChangeStatusClick(record.id)" ok-text="Đồng ý" cancel-text="Huỷ">
                                    <a-button style="background-color: #9b6dc7;" type="primary"
                                        class="p-2 d-flex justify-content-center align-items-center">
                                        <RedoOutlined />
                                    </a-button>
                                </a-popconfirm>
                            </a-tooltip>
                        </div>
                    </template>
                    <!-- <template v-if="column.key === 'operation'">
                        <div class="flex gap-1 justify-center">

                        </div>
                    </template> -->
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
import { modifyStatusSize } from '@/services/api/admin/loaigiay.api'
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
    { title: 'STT', key: 'stt', dataIndex: 'stt', width: 50, align: 'center' },
    { title: 'Mã danh mục ', key: 'ma', dataIndex: 'ma', width: 50, align: 'center' },
    { title: 'Tên danh mục', key: 'ten', dataIndex: 'ten', width: 150, align: 'center' },
    { title: 'Trạng thái', key: 'status', dataIndex: 'status', width: 150, align: 'center' },
    {
        title: 'Hành động',
        key: 'operation',
        width: 50,
        align: 'center'
    }
]

const handlePageChange = (pagination: any) => {
    emit('page-change', { page: pagination.current, pageSize: pagination.pageSize })
}

const handleAddClick = () => {
    emit('add')
}



const handleChangeStatusClick = async (id: string) => {
    try {
        const res = await modifyStatusSize(id);
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
<style scoped lang="scss">
body {
    font-family: 'Roboto', sans-serif;
}

.color {
    border-radius: 50%;
}

.center-cell {
    display: flex;
    justify-content: center;
    align-items: center;
}

// Custom class for centering buttons in the table if `justify-center` from Tailwind isn't available
.center-flex-buttons {
    display: flex;
    justify-content: center;
    align-items: center;
    /* Ensures vertical alignment as well */
}
</style>