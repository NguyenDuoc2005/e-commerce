<template>
  <div class="seller-register-page">
    <section class="seller-hero">
      <div>
        <p class="eyebrow">Kênh Người Bán</p>
        <h1>Đăng ký bán hàng</h1>
        <p class="hero-copy">
          Tạo hồ sơ shop, gửi duyệt và bắt đầu vận hành gian hàng sau khi Platform Admin phê duyệt.
        </p>
      </div>
      <a-button type="primary" size="large" @click="loadMyShop">Kiểm tra hồ sơ của tôi</a-button>
    </section>

    <a-alert
      v-if="myShop"
      class="mb-3"
      :type="statusAlertType(myShop.status)"
      show-icon
      :message="`Trạng thái shop: ${statusLabel(myShop.status)}`"
      :description="myShop.rejectionReason ? `Lý do từ chối: ${myShop.rejectionReason}` : undefined"
    />

    <a-form layout="vertical" class="seller-form" @submit.prevent="submit">
      <div class="form-grid">
        <a-form-item label="Tên shop" required>
          <a-input v-model:value="form.shopName" placeholder="Ví dụ: Glamsole Official" />
        </a-form-item>
        <a-form-item label="Slug URL">
          <a-input v-model:value="form.sellerSlug" placeholder="glamsole-official" />
        </a-form-item>
        <a-form-item label="Số điện thoại liên hệ" required>
          <a-input v-model:value="form.contactPhone" placeholder="0987654321" />
        </a-form-item>
        <a-form-item label="Ngành hàng chính">
          <a-input v-model:value="form.mainCategoryId" placeholder="ID danh mục nếu có" />
        </a-form-item>
        <a-form-item label="Logo URL">
          <a-input v-model:value="form.logoUrl" placeholder="https://..." />
        </a-form-item>
        <a-form-item label="Ảnh bìa URL">
          <a-input v-model:value="form.coverImageUrl" placeholder="https://..." />
        </a-form-item>
      </div>

      <a-form-item label="Mô tả shop" required>
        <a-textarea v-model:value="form.description" :rows="4" placeholder="Mô tả mặt hàng, chính sách và điểm mạnh của shop" />
      </a-form-item>
      <a-form-item label="Địa chỉ lấy hàng" required>
        <a-textarea v-model:value="form.pickupAddress" :rows="3" placeholder="Địa chỉ kho/lấy hàng" />
      </a-form-item>

      <div class="form-grid">
        <a-form-item label="Loại định danh" required>
          <a-select v-model:value="form.identityType">
            <a-select-option value="CCCD">CCCD/CMND</a-select-option>
            <a-select-option value="TAX_CODE">Mã số thuế</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Số định danh" required>
          <a-input v-model:value="form.identityNumber" placeholder="Số CCCD/CMND hoặc mã số thuế" />
        </a-form-item>
        <a-form-item label="Ngân hàng" required>
          <a-input v-model:value="form.bankName" placeholder="Tên ngân hàng" />
        </a-form-item>
        <a-form-item label="Số tài khoản" required>
          <a-input v-model:value="form.bankAccountNo" placeholder="Số tài khoản nhận đối soát" />
        </a-form-item>
        <a-form-item label="Chủ tài khoản" required>
          <a-input v-model:value="form.bankAccountHolder" placeholder="Tên chủ tài khoản" />
        </a-form-item>
      </div>

      <div class="actions">
        <a-button type="primary" size="large" :loading="loading" @click="submit">Gửi hồ sơ duyệt shop</a-button>
      </div>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getMyShop, registerShop, type SellerRegistrationRequest, type SellerResponse, type SellerStatus } from '@/services/api/seller/seller.api'

const loading = ref(false)
const myShop = ref<SellerResponse | null>(null)

const form = reactive<SellerRegistrationRequest>({
  shopName: '',
  sellerSlug: '',
  description: '',
  logoUrl: '',
  coverImageUrl: '',
  pickupAddress: '',
  contactPhone: '',
  identityType: 'CCCD',
  identityNumber: '',
  bankName: '',
  bankAccountNo: '',
  bankAccountHolder: '',
  mainCategoryId: ''
})

const requiredFields: Array<keyof SellerRegistrationRequest> = [
  'shopName',
  'description',
  'pickupAddress',
  'contactPhone',
  'identityType',
  'identityNumber',
  'bankName',
  'bankAccountNo',
  'bankAccountHolder'
]

const validate = () => {
  const missing = requiredFields.find((field) => !String(form[field] ?? '').trim())
  if (missing) {
    message.warning('Vui lòng nhập đầy đủ thông tin bắt buộc')
    return false
  }
  return true
}

const loadMyShop = async () => {
  try {
    const res = await getMyShop()
    myShop.value = res.data
  } catch (error: any) {
    if (error?.response?.status !== 404) {
      message.error(error?.response?.data?.message ?? 'Không tải được hồ sơ shop')
    }
  }
}

const submit = async () => {
  if (!validate()) return
  loading.value = true
  try {
    const res = await registerShop({ ...form })
    myShop.value = res.data
    message.success(res.message ?? 'Đã gửi hồ sơ shop')
  } catch (error: any) {
    message.error(error?.response?.data?.message ?? 'Không gửi được hồ sơ shop')
  } finally {
    loading.value = false
  }
}

const statusLabel = (status: SellerStatus) => {
  const labels: Record<SellerStatus, string> = {
    DRAFT: 'Nháp',
    PENDING_APPROVAL: 'Chờ duyệt',
    APPROVED: 'Đã duyệt',
    REJECTED: 'Bị từ chối',
    SUSPENDED: 'Tạm khóa',
    CLOSED: 'Đã đóng'
  }
  return labels[status] ?? status
}

const statusAlertType = (status: SellerStatus) => {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED' || status === 'SUSPENDED') return 'error'
  return 'info'
}

onMounted(loadMyShop)
</script>

<style scoped>
.seller-register-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 28px 16px 48px;
}

.seller-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: center;
  margin-bottom: 24px;
  padding: 28px;
  border: 1px solid #d9e5ea;
  border-radius: 8px;
  background: #f8fbfc;
}

.eyebrow {
  margin: 0 0 8px;
  font-weight: 700;
  color: #1677ff;
  text-transform: uppercase;
}

h1 {
  margin: 0 0 10px;
  font-size: 32px;
  font-weight: 800;
  color: #172033;
}

.hero-copy {
  max-width: 640px;
  margin: 0;
  color: #53606f;
}

.seller-form {
  padding: 24px;
  border: 1px solid #e3e8ef;
  border-radius: 8px;
  background: #ffffff;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 18px;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .seller-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
