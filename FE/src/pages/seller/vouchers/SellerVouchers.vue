<template>
  <div class="seller-page">
    <div class="page-head">
      <div>
        <h2>Voucher shop</h2>
        <p>Voucher chỉ áp dụng cho shop hiện tại.</p>
      </div>
      <a-space>
        <a-input v-model:value="q" placeholder="Tìm mã hoặc tên" allow-clear style="width: 220px" />
        <a-button @click="fetchVouchers">Tải lại</a-button>
        <a-button type="primary" @click="openForm()">Tạo voucher</a-button>
      </a-space>
    </div>

    <a-table :columns="columns" :data-source="vouchers" :loading="loading" row-key="id" :pagination="{ pageSize: 10 }">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'value'">{{ record.discountValue || record.discount_value }}</template>
        <template v-if="column.key === 'status'"><a-tag>{{ record.status }}</a-tag></template>
        <template v-if="column.key === 'actions'">
          <a-space>
            <a-button size="small" @click="openForm(record)">Sửa</a-button>
            <a-button size="small" @click="toggleStatus(record.id)">Đổi trạng thái</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="formOpen" title="Voucher shop" ok-text="Lưu" @ok="submit">
      <a-form layout="vertical">
        <a-form-item label="Tên voucher"><a-input v-model:value="form.name" /></a-form-item>
        <a-form-item label="Số lượng"><a-input-number v-model:value="form.quantity" style="width: 100%" :min="1" /></a-form-item>
        <a-form-item label="Điều kiện đơn tối thiểu"><a-input-number v-model:value="form.conditionAmount" style="width: 100%" :min="0" /></a-form-item>
        <a-form-item label="Giá trị giảm"><a-input-number v-model:value="form.maxDiscountAmount" style="width: 100%" :min="0" /></a-form-item>
        <a-form-item label="Bắt đầu"><a-input v-model:value="form.startDate" placeholder="2026-08-17" /></a-form-item>
        <a-form-item label="Kết thúc"><a-input v-model:value="form.endDate" placeholder="2099-12-31" /></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { changeSellerVoucherStatus, getSellerVouchers, modifySellerVoucher, type SellerVoucher } from '@/services/api/seller/voucher.api'

const loading = ref(false)
const formOpen = ref(false)
const q = ref('')
const vouchers = ref<SellerVoucher[]>([])
const form = reactive<any>({
  id: undefined,
  name: '',
  quantity: 1,
  conditionAmount: 0,
  maxDiscountAmount: 0,
  discountType: false,
  discountMethod: false,
  startDate: '2026-08-17',
  endDate: '2099-12-31'
})

const columns = [
  { title: 'Mã', dataIndex: 'code' },
  { title: 'Tên', dataIndex: 'name' },
  { title: 'Giá trị', key: 'value' },
  { title: 'Số lượng', dataIndex: 'quantity' },
  { title: 'Trạng thái', key: 'status' },
  { title: 'Thao tác', key: 'actions' }
]

const fetchVouchers = async () => {
  loading.value = true
  try {
    const res = await getSellerVouchers({ q: q.value, page: 1, size: 50 })
    vouchers.value = res.data?.data || []
  } catch {
    message.error('Không tải được voucher shop')
  } finally {
    loading.value = false
  }
}

const openForm = (record?: any) => {
  Object.assign(form, {
    id: record?.id,
    name: record?.name || '',
    quantity: record?.quantity || 1,
    conditionAmount: record?.conditionAmount || record?.condition_amount || 0,
    maxDiscountAmount: record?.maxDiscountAmount || record?.max_discount_amount || 0,
    discountType: false,
    discountMethod: false,
    startDate: '2026-08-17',
    endDate: '2099-12-31'
  })
  formOpen.value = true
}

const submit = async () => {
  await modifySellerVoucher(form)
  message.success('Đã lưu voucher shop')
  formOpen.value = false
  await fetchVouchers()
}

const toggleStatus = async (id: string) => {
  await changeSellerVoucherStatus(id)
  message.success('Đã đổi trạng thái')
  await fetchVouchers()
}

onMounted(fetchVouchers)
</script>

<style scoped>
.seller-page { padding: 24px; }
.page-head { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 16px; }
.page-head h2 { margin: 0; font-size: 22px; font-weight: 700; }
.page-head p { color: #64748b; margin: 4px 0 0; }
</style>
