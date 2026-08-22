<template>
  <div class="attribute-admin-page">
    <div class="page-head">
      <h2>Quản lý thuộc tính</h2>
      <a-space wrap>
        <a-input-search v-model:value="query" allow-clear placeholder="Tìm thuộc tính" style="width: 240px" @search="fetchAttributes" />
        <a-select v-model:value="status" :options="statusOptions" allow-clear placeholder="Trạng thái" style="width: 170px" @change="fetchAttributes" />
        <a-button title="Tải lại" :loading="loading" @click="fetchAttributes"><template #icon><ReloadOutlined /></template></a-button>
      </a-space>
    </div>

    <a-table row-key="id" :columns="columns" :data-source="attributes" :loading="loading" :pagination="{ pageSize: 12 }" :scroll="{ x: 980 }">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'name'">
          <div class="strong">{{ record.name }}</div>
          <div class="muted">{{ record.code }}</div>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.normalizationStatus)">{{ statusLabel(record.normalizationStatus) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'scope'">
          {{ record.normalizationStatus === 'PENDING' ? `Shop ${record.creatorSellerId || '-'}` : 'Toàn sàn' }}
        </template>
        <template v-else-if="column.key === 'categories'">
          <a-space wrap><a-tag v-for="category in record.categories" :key="category.id">{{ category.name }}</a-tag></a-space>
        </template>
        <template v-else-if="column.key === 'actions'">
          <a-space>
            <a-button size="small" title="Chuẩn hóa" :disabled="record.normalizationStatus === 'MERGED' || record.normalizationStatus === 'HIDDEN'" @click="openStandardize(record)">
              <template #icon><CheckOutlined /></template>
            </a-button>
            <a-button size="small" title="Gộp" :disabled="record.normalizationStatus === 'MERGED' || record.normalizationStatus === 'HIDDEN'" @click="openMerge(record)">
              <template #icon><BranchesOutlined /></template>
            </a-button>
            <a-button size="small" danger title="Ẩn" :disabled="record.normalizationStatus === 'MERGED' || record.normalizationStatus === 'HIDDEN'" @click="openHide(record)">
              <template #icon><EyeInvisibleOutlined /></template>
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="standardizeOpen" title="Chuẩn hóa thuộc tính" ok-text="Lưu" :confirm-loading="saving" @ok="submitStandardize">
      <a-form layout="vertical">
        <a-form-item label="Tên chuẩn" required><a-input v-model:value="standardizeForm.name" /></a-form-item>
        <a-form-item label="Danh mục gợi ý"><a-select v-model:value="standardizeForm.categoryIds" mode="multiple" :options="categoryOptions" /></a-form-item>
        <a-form-item label="Cho phép lọc"><a-switch v-model:checked="standardizeForm.filterable" /></a-form-item>
        <a-form-item label="Lý do"><a-textarea v-model:value="standardizeForm.reason" :rows="3" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="mergeOpen" title="Gộp thuộc tính" ok-text="Gộp" :confirm-loading="saving" @ok="submitMerge">
      <a-form layout="vertical">
        <a-form-item label="Thuộc tính chuẩn" required><a-select v-model:value="mergeForm.targetAttributeId" show-search option-filter-prop="label" :options="mergeTargets" /></a-form-item>
        <a-form-item label="Lý do"><a-textarea v-model:value="mergeForm.reason" :rows="3" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="hideOpen" title="Ẩn thuộc tính" ok-text="Ẩn" ok-type="danger" :confirm-loading="saving" @ok="submitHide">
      <a-form layout="vertical"><a-form-item label="Lý do"><a-textarea v-model:value="hideReason" :rows="3" /></a-form-item></a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { BranchesOutlined, CheckOutlined, EyeInvisibleOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { GetListDanhMuc } from '@/services/api/permitall/sanpham/pmsanpham.api'
import {
  getAdminProductAttributes,
  hideProductAttribute,
  mergeProductAttribute,
  standardizeProductAttribute,
  type AdminProductAttribute,
  type AttributeStatus
} from '@/services/api/admin/product-attribute.api'

const attributes = ref<AdminProductAttribute[]>([])
const loading = ref(false)
const saving = ref(false)
const query = ref('')
const status = ref<AttributeStatus>()
const selected = ref<AdminProductAttribute>()
const standardizeOpen = ref(false)
const mergeOpen = ref(false)
const hideOpen = ref(false)
const hideReason = ref('')
const categoryOptions = ref<Array<{ value: string; label: string }>>([])
const standardizeForm = reactive({ name: '', categoryIds: [] as string[], filterable: false, reason: '' })
const mergeForm = reactive({ targetAttributeId: '', reason: '' })
const statusOptions = [
  { value: 'PENDING', label: 'Chờ hậu kiểm' },
  { value: 'STANDARDIZED', label: 'Đã chuẩn hóa' },
  { value: 'MERGED', label: 'Đã gộp' },
  { value: 'HIDDEN', label: 'Đã ẩn' }
]
const columns = [
  { title: 'Thuộc tính', key: 'name', width: 210 },
  { title: 'Kiểu', dataIndex: 'dataType', width: 130 },
  { title: 'Trạng thái', key: 'status', width: 130 },
  { title: 'Phạm vi', key: 'scope', width: 210 },
  { title: 'Sử dụng', dataIndex: 'usageCount', width: 90 },
  { title: 'Danh mục', key: 'categories', minWidth: 220 },
  { title: 'Thao tác', key: 'actions', fixed: 'right', width: 145 }
]

const mergeTargets = computed(() => attributes.value
  .filter((item) => item.id !== selected.value?.id && item.dataType === selected.value?.dataType && !['MERGED', 'HIDDEN'].includes(item.normalizationStatus))
  .map((item) => ({ value: item.id, label: `${item.name} (${item.normalizationStatus})` })))

const statusLabel = (value: AttributeStatus) => statusOptions.find((item) => item.value === value)?.label || value
const statusColor = (value: AttributeStatus) => ({ PENDING: 'gold', STANDARDIZED: 'green', MERGED: 'blue', HIDDEN: 'default' }[value])

const fetchAttributes = async () => {
  loading.value = true
  try { attributes.value = await getAdminProductAttributes({ q: query.value || undefined, status: status.value }) }
  catch (error: any) { message.error(error?.response?.data?.message || 'Không tải được thuộc tính') }
  finally { loading.value = false }
}

const openStandardize = (record: AdminProductAttribute) => {
  selected.value = record
  Object.assign(standardizeForm, { name: record.name, categoryIds: record.categories.map((item) => item.id), filterable: record.categories.some((item) => item.filterable), reason: '' })
  standardizeOpen.value = true
}
const openMerge = (record: AdminProductAttribute) => { selected.value = record; Object.assign(mergeForm, { targetAttributeId: '', reason: '' }); mergeOpen.value = true }
const openHide = (record: AdminProductAttribute) => { selected.value = record; hideReason.value = ''; hideOpen.value = true }

const submitStandardize = async () => {
  if (!selected.value || !standardizeForm.name.trim()) return void message.warning('Vui lòng nhập tên chuẩn')
  saving.value = true
  try { await standardizeProductAttribute(selected.value.id, standardizeForm); message.success('Đã chuẩn hóa thuộc tính'); standardizeOpen.value = false; await fetchAttributes() }
  catch (error: any) { message.error(error?.response?.data?.message || 'Không chuẩn hóa được thuộc tính') }
  finally { saving.value = false }
}
const submitMerge = async () => {
  if (!selected.value || !mergeForm.targetAttributeId) return void message.warning('Vui lòng chọn thuộc tính chuẩn')
  saving.value = true
  try { await mergeProductAttribute(selected.value.id, mergeForm.targetAttributeId, mergeForm.reason); message.success('Đã gộp thuộc tính'); mergeOpen.value = false; await fetchAttributes() }
  catch (error: any) { message.error(error?.response?.data?.message || 'Không gộp được thuộc tính') }
  finally { saving.value = false }
}
const submitHide = async () => {
  if (!selected.value) return
  saving.value = true
  try { await hideProductAttribute(selected.value.id, hideReason.value); message.success('Đã ẩn thuộc tính'); hideOpen.value = false; await fetchAttributes() }
  catch (error: any) { message.error(error?.response?.data?.message || 'Không ẩn được thuộc tính') }
  finally { saving.value = false }
}

onMounted(async () => {
  const categories = await GetListDanhMuc()
  categoryOptions.value = (categories.data || []).map((item: any) => ({ value: item.id, label: item.ten || item.name }))
  await fetchAttributes()
})
</script>

<style scoped>
.attribute-admin-page { padding: 20px; }
.page-head { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.page-head h2 { margin: 0; font-size: 22px; font-weight: 700; }
.strong { font-weight: 700; }
.muted { color: #64748b; font-size: 12px; margin-top: 2px; }
@media (max-width: 720px) { .page-head { align-items: flex-start; flex-direction: column; } }
</style>
