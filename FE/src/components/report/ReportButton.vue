<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { createBuyerReport, createSellerReport, type ReportTargetType } from '@/services/api/report/report.api'
import { useAuthStore } from '@/stores/auth'

const props = withDefaults(defineProps<{ targetType: ReportTargetType; targetId: string; reporterType?: 'buyer' | 'seller'; label?: string }>(), { reporterType: 'buyer', label: 'Báo cáo vi phạm' })
const open = ref(false)
const authStore = useAuthStore()
const submitting = ref(false)
const form = reactive({ reasonCode: 'OTHER', description: '', evidenceText: '' })
const start = () => {
  if (!authStore.isAuthenticated) return message.warning('Vui lòng đăng nhập để gửi báo cáo')
  open.value = true
}
const submit = async () => {
  if (!form.description.trim()) return message.warning('Vui lòng mô tả dấu hiệu vi phạm')
  submitting.value = true
  try {
    const payload = { targetType: props.targetType, targetId: props.targetId, reasonCode: form.reasonCode, description: form.description, evidenceUrls: form.evidenceText.split('\n').map(v => v.trim()).filter(Boolean) }
    if (props.reporterType === 'seller') await createSellerReport(payload); else await createBuyerReport(payload)
    message.success('Đã gửi báo cáo đến bộ phận kiểm duyệt')
    open.value = false; form.description = ''; form.evidenceText = ''; form.reasonCode = 'OTHER'
  } catch (error: any) { message.error(error?.response?.data?.message || 'Không thể gửi báo cáo') }
  finally { submitting.value = false }
}
</script>

<template>
  <a-button type="link" danger size="small" @click.stop="start">{{ label }}</a-button>
  <a-modal v-model:open="open" title="Báo cáo vi phạm" :confirm-loading="submitting" ok-text="Gửi báo cáo" cancel-text="Hủy" @ok="submit">
    <a-form layout="vertical">
      <a-form-item label="Lý do" required><a-select v-model:value="form.reasonCode">
        <a-select-option value="FAKE_PRODUCT">Sản phẩm giả</a-select-option><a-select-option value="PROHIBITED_ITEM">Hàng hóa bị cấm</a-select-option>
        <a-select-option value="COPYRIGHT">Vi phạm bản quyền</a-select-option><a-select-option value="FAKE_REVIEW">Đánh giá giả</a-select-option>
        <a-select-option value="SCAM">Lừa đảo</a-select-option><a-select-option value="OFFENSIVE_CONTENT">Nội dung phản cảm</a-select-option><a-select-option value="OTHER">Khác</a-select-option>
      </a-select></a-form-item>
      <a-form-item label="Mô tả" required><a-textarea v-model:value="form.description" :rows="4" placeholder="Mô tả rõ dấu hiệu vi phạm" /></a-form-item>
      <a-form-item label="URL bằng chứng (mỗi dòng một URL)"><a-textarea v-model:value="form.evidenceText" :rows="3" /></a-form-item>
    </a-form>
  </a-modal>
</template>
