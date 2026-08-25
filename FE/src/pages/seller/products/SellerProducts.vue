<template>
  <section class="seller-products">
    <div class="page-head">
      <div>
        <h2>Sản phẩm của shop</h2>
        <p>Quản lý thông số mô tả và toàn bộ phân loại hàng trong một aggregate.</p>
      </div>
      <a-button type="primary" @click="openCreate">Tạo sản phẩm</a-button>
    </div>

    <a-card :bordered="false">
      <div class="toolbar">
        <a-input-search v-model:value="keyword" placeholder="Tên sản phẩm" allow-clear @search="loadProducts" />
      </div>
      <a-table :columns="columns" :data-source="products" :loading="loading" :pagination="pagination"
        row-key="id" @change="changePage">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'product'">
            <div class="product-cell">
              <img v-if="record.thumbnailUrl" :src="record.thumbnailUrl" alt="" />
              <div><strong>{{ record.name }}</strong><small>{{ record.category?.name }}</small></div>
            </div>
          </template>
          <template v-else-if="column.key === 'price'">{{ priceRange(record) }}</template>
          <template v-else-if="column.key === 'stock'">{{ record.totalQuantity }} / {{ record.activeVariantCount }} variant</template>
          <template v-else-if="column.key === 'attributes'">
            {{ record.attributePreview?.map((item: any) => item.name).join(' · ') || '-' }}
          </template>
          <template v-else-if="column.key === 'axes'">
            <a-tag v-for="axis in record.axisPreview" :key="axis.id">{{ axis.name }}</a-tag>
            <span v-if="!record.axisPreview?.length">Không phân loại</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="openEdit(record.id)">Sửa aggregate</a-button>
              <a-button size="small" @click="toggleStatus(record.id, record.status)">Đổi trạng thái</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="modalOpen" :title="editingId ? 'Sửa sản phẩm' : 'Tạo sản phẩm'" width="1180px"
      :confirm-loading="saving" ok-text="Xem trước" @ok="openPreview">
      <div class="aggregate-form">
        <a-card size="small" title="1. Thông tin chung">
          <div class="grid three">
            <a-form-item label="Danh mục lá" required>
              <a-select :value="form.categoryId" show-search :options="categoryOptions"
                option-filter-prop="label" @change="changeCategory" />
            </a-form-item>
            <a-form-item label="Tên sản phẩm" required><a-input v-model:value="form.name" /></a-form-item>
            <a-form-item label="Mã sản phẩm"><a-input v-model:value="form.code" placeholder="Tự sinh nếu để trống" /></a-form-item>
          </div>
          <a-form-item label="Mô tả"><a-textarea v-model:value="form.description" :rows="3" /></a-form-item>
          <div class="section-title"><strong>Ảnh gallery</strong><a-button size="small" @click="addImage">Thêm URL ảnh</a-button></div>
          <div v-for="(image, index) in form.productImages" :key="index" class="inline-row">
            <a-input v-model:value="image.url" placeholder="https://..." />
            <a-button danger @click="form.productImages.splice(index, 1)">Xóa</a-button>
          </div>
        </a-card>

        <a-card size="small">
          <template #title>
            <div class="section-title"><span>2. Thông số mô tả</span><a-button size="small" @click="addCustomAttribute">Thêm thông số tự do</a-button></div>
          </template>
          <a-alert type="info" show-icon message="Không giới hạn 50 thông số. Giá trị mới của SELECT được dùng ngay và chờ hậu kiểm." />
          <a-empty v-if="!attributes.length" description="Chọn danh mục để nhận gợi ý" />
          <div v-for="(attribute, index) in attributes" :key="attribute.rowKey" class="attribute-row">
            <div class="attribute-flags">
              <a-tag v-if="attribute.required" color="red">Bắt buộc</a-tag>
              <a-tag v-if="attribute.filterable" color="blue">Dùng để lọc</a-tag>
              <a-tag v-if="!attribute.definitionId" color="gold">Seller tự thêm · Chờ hậu kiểm</a-tag>
            </div>
            <div class="grid attribute-grid">
              <a-form-item :label="`Tên ${attribute.required ? '*' : ''}`">
                <a-input v-model:value="attribute.name" :disabled="Boolean(attribute.definitionId)" />
              </a-form-item>
              <a-form-item label="Kiểu">
                <a-select v-model:value="attribute.dataType" :disabled="Boolean(attribute.definitionId)" :options="dataTypeOptions" />
              </a-form-item>
              <a-form-item v-if="attribute.dataType === 'TEXT'" label="Giá trị">
                <a-input v-model:value="attribute.valueText" />
              </a-form-item>
              <a-form-item v-else-if="attribute.dataType === 'NUMBER'" label="Giá trị / đơn vị">
                <a-input-group compact><a-input-number v-model:value="attribute.valueNumber" style="width: 65%" /><a-input v-model:value="attribute.unit" style="width: 35%" /></a-input-group>
              </a-form-item>
              <a-form-item v-else label="Chọn hoặc nhập mới">
                <a-select v-model:value="attribute.selectedTokens" :mode="attribute.dataType === 'SELECT_MULTI' ? 'tags' : 'tags'"
                  :max-count="attribute.dataType === 'SELECT_ONE' ? 1 : undefined" :options="attribute.options.map(option => ({ value: option.id, label: option.value }))" />
              </a-form-item>
              <a-button v-if="!attribute.required" danger class="remove-button" @click="attributes.splice(index, 1)">Xóa</a-button>
            </div>
          </div>
        </a-card>

        <a-card size="small">
          <template #title>
            <div class="section-title"><span>3. Phân loại hàng (0–2 trục)</span><a-button size="small" :disabled="axes.length >= 2" @click="addAxis">Thêm trục</a-button></div>
          </template>
          <a-alert type="warning" show-icon message="Đổi tên/giá trị trục sẽ sinh lại ma trận. Kiểm tra SKU, giá và tồn trước khi lưu." />
          <div v-for="(axis, axisIndex) in axes" :key="axis.clientKey" class="axis-row">
            <a-auto-complete
              v-model:value="axis.name"
              :options="axisNameOptions"
              placeholder="Tên trục, ví dụ Màu sắc / Dung lượng"
              @search="searchAxisNames"
              @select="(_value: string, option: any) => selectAxisSuggestion(axis, option)"
              @change="() => changeAxisName(axis)"
            />
            <a-select v-model:value="axis.valueLabels" mode="tags" placeholder="Nhập các giá trị" @change="markMatrixDirty" />
            <a-button danger @click="removeAxis(axisIndex)">Xóa trục</a-button>
          </div>
          <div class="matrix-actions">
            <a-button type="primary" ghost @click="generateMatrix">{{ matrixDirty ? 'Sinh lại ma trận' : 'Làm mới ma trận' }}</a-button>
            <a-input-number v-model:value="bulkPrice" :min="0" placeholder="Giá chung" />
            <a-input-number v-model:value="bulkStock" :min="0" placeholder="Tồn chung" />
            <a-button @click="applyBulk">Áp dụng hàng loạt</a-button>
          </div>
          <a-table :columns="variantColumns" :data-source="variants" row-key="localKey" size="small" :pagination="false" :scroll="{ x: 900 }">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'enabled'"><a-checkbox v-model:checked="record.enabled" /></template>
              <template v-else-if="column.key === 'sku'"><a-input v-model:value="record.sku" /></template>
              <template v-else-if="column.key === 'price'"><a-input-number v-model:value="record.salePrice" :min="0" /></template>
              <template v-else-if="column.key === 'stock'"><a-input-number v-model:value="record.quantity" :min="0" /></template>
              <template v-else-if="column.key === 'image'"><a-input v-model:value="record.imageUrl" placeholder="URL ảnh variant" /></template>
            </template>
          </a-table>
        </a-card>
      </div>
    </a-modal>

    <a-modal v-model:open="previewOpen" title="4. Xem trước product aggregate" width="1050px"
      :confirm-loading="saving" ok-text="Xác nhận lưu" cancel-text="Quay lại chỉnh sửa" @ok="submit">
      <div v-if="previewPayload" class="preview-grid">
        <a-descriptions bordered :column="2" size="small">
          <a-descriptions-item label="Sản phẩm">{{ previewPayload.name }}</a-descriptions-item>
          <a-descriptions-item label="Danh mục">{{ selectedCategoryLabel }}</a-descriptions-item>
          <a-descriptions-item label="Thông số">{{ previewPayload.attributes.length }}</a-descriptions-item>
          <a-descriptions-item label="Trục / biến thể">{{ previewPayload.variantAxes.length }} / {{ previewPayload.variants.length }}</a-descriptions-item>
          <a-descriptions-item label="Mô tả" :span="2">{{ previewPayload.description || '—' }}</a-descriptions-item>
        </a-descriptions>
        <a-card size="small" title="Thông số sản phẩm">
          <a-space wrap><a-tag v-for="item in previewAttributeLabels" :key="item">{{ item }}</a-tag><span v-if="!previewAttributeLabels.length">Không có</span></a-space>
        </a-card>
        <a-card size="small" title="Trục phân loại">
          <div v-for="axis in previewPayload.variantAxes" :key="axis.clientKey"><strong>{{ axis.name }}:</strong> {{ axis.values.map(value => value.value).join(', ') }}</div>
          <span v-if="!previewPayload.variantAxes.length">Sản phẩm không phân loại</span>
        </a-card>
        <a-table :columns="previewVariantColumns" :data-source="previewPayload.variants" row-key="sku" size="small" :pagination="false" />
      </div>
    </a-modal>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  changeSellerProductStatus,
  createSellerProduct,
  getSellerCategoryAttributes,
  getSellerCategoryTree,
  getAxisNameSuggestions,
  getSellerProduct,
  getSellerProducts,
  updateSellerProduct,
  type AttributeDataType,
  type AttributeOption,
  type AxisInput,
  type AxisNameSuggestion,
  type CategoryNode,
  type EntityStatus,
  type ProductAggregatePayload,
  type ProductSummary,
  type VariantInput
} from '@/services/api/seller/product.api'

interface AttributeRow {
  rowKey: string
  definitionId?: string
  name: string
  dataType: AttributeDataType
  defaultUnit?: string
  required: boolean
  filterable: boolean
  options: AttributeOption[]
  valueText?: string
  valueNumber?: number
  unit?: string
  selectedTokens: string[]
  displayOrder: number
}

interface AxisRow extends AxisInput { valueLabels: string[] }
interface VariantRow extends VariantInput { localKey: string; label: string; enabled: boolean }

const columns = [
  { title: 'Sản phẩm', key: 'product' },
  { title: 'Giá', key: 'price', width: 180 },
  { title: 'Tồn / variant', key: 'stock', width: 150 },
  { title: 'Thông số', key: 'attributes' },
  { title: 'Phân loại', key: 'axes', width: 220 },
  { title: '', key: 'action', width: 220 }
]
const variantColumns = [
  { title: 'Bán', key: 'enabled', width: 60 },
  { title: 'Tổ hợp', dataIndex: 'label', width: 200 },
  { title: 'SKU', key: 'sku', width: 180 },
  { title: 'Giá', key: 'price', width: 150 },
  { title: 'Tồn', key: 'stock', width: 120 },
  { title: 'Ảnh', key: 'image', width: 240 }
]
const previewVariantColumns = [
  { title: 'SKU', dataIndex: 'sku', key: 'sku' },
  { title: 'Giá', dataIndex: 'salePrice', key: 'salePrice' },
  { title: 'Tồn kho', dataIndex: 'quantity', key: 'quantity' },
  { title: 'Ảnh riêng', dataIndex: 'imageUrl', key: 'imageUrl' }
]
const dataTypeOptions = [
  { value: 'TEXT', label: 'Văn bản' }, { value: 'NUMBER', label: 'Số' },
  { value: 'SELECT_ONE', label: 'Chọn một' }, { value: 'SELECT_MULTI', label: 'Chọn nhiều' }
]

const products = ref<ProductSummary[]>([])
const categoryTree = ref<CategoryNode[]>([])
const attributes = ref<AttributeRow[]>([])
const axes = ref<AxisRow[]>([])
const variants = ref<VariantRow[]>([])
const loading = ref(false)
const saving = ref(false)
const modalOpen = ref(false)
const previewOpen = ref(false)
const previewPayload = ref<ProductAggregatePayload>()
const editingId = ref<string>()
const keyword = ref('')
const matrixDirty = ref(false)
const bulkPrice = ref<number>()
const bulkStock = ref<number>()
const axisSuggestions = ref<AxisNameSuggestion[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })
const form = reactive<ProductAggregatePayload>({
  categoryId: '', name: '', code: '', description: '', productImages: [], attributes: [], variantAxes: [], variants: []
})

const pagination = computed(() => ({ current: page.current, pageSize: page.size, total: page.total, showSizeChanger: true }))
const categoryOptions = computed(() => {
  const result: Array<{ value: string; label: string; disabled: boolean }> = []
  const walk = (nodes: CategoryNode[], prefix = '') => nodes.forEach(node => {
    const label = prefix ? `${prefix} / ${node.name}` : node.name
    result.push({ value: node.id, label, disabled: Boolean(node.children?.length) })
    walk(node.children || [], label)
  })
  walk(categoryTree.value)
  return result
})
const selectedCategoryLabel = computed(() => categoryOptions.value.find(item => item.value === form.categoryId)?.label ?? '—')
const axisNameOptions = computed(() => axisSuggestions.value.map(item => ({ value: item.name, label: item.verified ? `${item.name} · Đã chuẩn hóa` : item.name, suggestionId: item.resolvedSuggestionId || item.id })))
const previewAttributeLabels = computed(() => (previewPayload.value?.attributes ?? []).map(item => {
  const value = item.dataType === 'TEXT' ? item.valueText
    : item.dataType === 'NUMBER' ? `${item.valueNumber ?? ''}${item.unit ? ` ${item.unit}` : ''}`
      : [...item.selectedOptionIds, ...item.selectedOptionValues].join(', ')
  const name = item.name || attributes.value.find(row => row.definitionId === item.definitionId)?.name || 'Thuộc tính'
  return `${name}: ${value}`
}))

const uid = () => `${Date.now()}-${Math.random().toString(16).slice(2)}`
const priceRange = (record: ProductSummary) => {
  if (record.minPrice == null) return '-'
  const format = (value: number) => new Intl.NumberFormat('vi-VN').format(value) + ' ₫'
  return record.minPrice === record.maxPrice ? format(record.minPrice) : `${format(record.minPrice)} – ${format(record.maxPrice || record.minPrice)}`
}

const loadProducts = async () => {
  loading.value = true
  try {
    const response = await getSellerProducts({ page: page.current - 1, size: page.size, q: keyword.value || undefined })
    products.value = response.content || []
    page.total = response.totalElements || 0
  } finally { loading.value = false }
}

const resetForm = () => {
  Object.assign(form, { categoryId: '', name: '', code: '', description: '', productImages: [] })
  attributes.value = []
  axes.value = []
  variants.value = []
  editingId.value = undefined
  matrixDirty.value = false
  previewPayload.value = undefined
  previewOpen.value = false
}

const openCreate = () => {
  resetForm()
  variants.value = [defaultVariant()]
  modalOpen.value = true
}

const loadSuggestions = async (categoryId: string) => {
  const suggestions = await getSellerCategoryAttributes(categoryId)
  attributes.value = suggestions.map(item => ({
    rowKey: uid(), definitionId: item.definitionId, name: item.name, dataType: item.dataType,
    defaultUnit: item.defaultUnit, required: item.required, filterable: item.filterable, options: item.options || [], unit: item.defaultUnit,
    selectedTokens: [], displayOrder: item.displayOrder
  }))
}

const applyCategory = async (categoryId: string) => {
  form.categoryId = categoryId
  await loadSuggestions(categoryId)
}

const changeCategory = (categoryId: string) => {
  if (!form.categoryId || (!attributes.value.some(hasAttributeValue) && !axes.value.length)) return void applyCategory(categoryId)
  Modal.confirm({
    title: 'Đổi danh mục sẽ xóa thông số hiện tại',
    content: 'Bạn cần nhập lại thông số phù hợp với danh mục mới.',
    onOk: () => applyCategory(categoryId)
  })
}

const addImage = () => form.productImages.push({ url: '', displayOrder: form.productImages.length, status: 'ACTIVE' })
const addCustomAttribute = () => attributes.value.push({
  rowKey: uid(), name: '', dataType: 'TEXT', required: false, filterable: false, options: [], selectedTokens: [], displayOrder: attributes.value.length + 1
})
const addAxis = () => {
  if (axes.value.length >= 2) return
  axes.value.push({ clientKey: `axis-${uid()}`, name: '', displayOrder: axes.value.length + 1, values: [], valueLabels: [] })
  markMatrixDirty()
}
const removeAxis = (index: number) => {
  axes.value.splice(index, 1)
  axes.value.forEach((axis, order) => { axis.displayOrder = order + 1 })
  markMatrixDirty()
}
const markMatrixDirty = () => { matrixDirty.value = true }
const searchAxisNames = async (query: string) => {
  try { axisSuggestions.value = await getAxisNameSuggestions(query) }
  catch { axisSuggestions.value = [] }
}
const selectAxisSuggestion = (axis: AxisRow, option: { suggestionId?: string; value: string }) => {
  axis.name = option.value
  axis.nameSuggestionId = option.suggestionId
  markMatrixDirty()
}
const changeAxisName = (axis: AxisRow) => {
  const match = axisSuggestions.value.find(item => item.name.toLocaleLowerCase('vi') === axis.name.trim().toLocaleLowerCase('vi'))
  axis.nameSuggestionId = match?.resolvedSuggestionId || match?.id
  markMatrixDirty()
}

const defaultVariant = (): VariantRow => ({
  localKey: 'DEFAULT', label: 'Mặc định', sku: '', salePrice: 0, quantity: 0,
  defaultVariant: true, status: 'ACTIVE', selectionValueKeys: [], enabled: true
})

const generateMatrix = () => {
  if (!axes.value.length) {
    variants.value = [variants.value.find(item => item.defaultVariant) || defaultVariant()]
    matrixDirty.value = false
    return
  }
  if (axes.value.some(axis => !axis.name.trim() || !axis.valueLabels.length)) return void message.warning('Mỗi trục cần tên và ít nhất một giá trị')
  const prepared = axes.value.map((axis, axisIndex) => axis.valueLabels.map((value, valueIndex) => ({
    key: axis.values.find(item => item.value === value)?.clientKey || `axis-${axisIndex}-value-${valueIndex}-${uid()}`,
    value
  })))
  const combinations = prepared.reduce<Array<Array<{ key: string; value: string }>>>((result, values) =>
    result.flatMap(combination => values.map(value => [...combination, value])), [[]])
  const previous = new Map(variants.value.map(item => [item.label, item]))
  variants.value = combinations.map(combination => {
    const label = combination.map(item => item.value).join(' / ')
    return previous.get(label) || {
      localKey: combination.map(item => item.key).join('|'), label, sku: suggestSku(combination.map(item => item.value)), salePrice: bulkPrice.value || 0,
      quantity: bulkStock.value || 0, defaultVariant: false, status: 'ACTIVE',
      selectionValueKeys: combination.map(item => item.key), enabled: true
    }
  })
  axes.value.forEach((axis, axisIndex) => {
    axis.values = prepared[axisIndex].map((item, valueIndex) => ({ clientKey: item.key, value: item.value, displayOrder: valueIndex + 1 }))
  })
  matrixDirty.value = false
}
const suggestSku = (values: string[]) => {
  const base = (form.code || form.name || 'SKU').normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D').replace(/[^a-zA-Z0-9]+/g, '-').replace(/^-|-$/g, '').toUpperCase()
  const suffix = values.map(value => value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D').replace(/[^a-zA-Z0-9]+/g, '-').replace(/^-|-$/g, '').toUpperCase()).join('-')
  return [base || 'SKU', suffix].filter(Boolean).join('-')
}

const applyBulk = () => variants.value.forEach(variant => {
  if (bulkPrice.value != null) variant.salePrice = bulkPrice.value
  if (bulkStock.value != null) variant.quantity = bulkStock.value
})

const openEdit = async (id: string) => {
  resetForm()
  const detail = await getSellerProduct(id)
  editingId.value = id
  Object.assign(form, {
    categoryId: detail.category.id, code: detail.code, name: detail.name, description: detail.description,
    productImages: detail.productImages.map(image => ({ url: image.url, displayOrder: image.displayOrder, status: image.status }))
  })
  const suggestions = await getSellerCategoryAttributes(detail.category.id)
  const suggestionMap = new Map(suggestions.map(item => [item.definitionId, item]))
  attributes.value = detail.attributes.map((item, index) => {
    const suggestion = suggestionMap.get(item.definitionId || '')
    return {
      rowKey: uid(), definitionId: item.definitionId, name: suggestion?.name || item.name || '', dataType: item.dataType,
      defaultUnit: suggestion?.defaultUnit, required: suggestion?.required || false, filterable: suggestion?.filterable || false, options: suggestion?.options || [],
      valueText: item.valueText, valueNumber: item.valueNumber, unit: item.unit || suggestion?.defaultUnit,
      selectedTokens: item.selectedOptions?.map(option => option.resolvedOptionId || option.id) || [], displayOrder: item.displayOrder || index + 1
    }
  })
  suggestions.filter(item => !attributes.value.some(row => row.definitionId === item.definitionId)).forEach(item =>
    attributes.value.push({ rowKey: uid(), definitionId: item.definitionId, name: item.name, dataType: item.dataType,
      defaultUnit: item.defaultUnit, required: item.required, filterable: item.filterable, options: item.options, unit: item.defaultUnit, selectedTokens: [], displayOrder: item.displayOrder }))
  axes.value = detail.variantAxes.map((axis, axisIndex) => ({
    id: axis.id, clientKey: axis.id || `axis-${axisIndex}`, name: axis.name, nameSuggestionId: axis.nameSuggestionId, displayOrder: axis.displayOrder,
    values: axis.values.map((value, valueIndex) => ({ id: value.id, clientKey: value.id || `value-${axisIndex}-${valueIndex}`, value: value.value, displayOrder: value.displayOrder })),
    valueLabels: axis.values.map(value => value.value)
  }))
  variants.value = detail.variants.map(variant => ({
    id: variant.id, localKey: variant.id, label: variant.selections.length ? variant.selections.map(item => item.value).join(' / ') : 'Mặc định',
    sku: variant.sku, salePrice: variant.salePrice, quantity: variant.quantity, imageUrl: variant.imageUrl,
    defaultVariant: variant.isDefault, status: variant.status, selectionValueKeys: variant.selections.map(item => item.valueId), enabled: true
  }))
  modalOpen.value = true
}

const hasAttributeValue = (row: AttributeRow) => row.dataType === 'TEXT' ? Boolean(row.valueText?.trim())
  : row.dataType === 'NUMBER' ? row.valueNumber != null : row.selectedTokens.length > 0

const buildPayload = (): ProductAggregatePayload => {
  if (matrixDirty.value) generateMatrix()
  const optionIds = (row: AttributeRow) => new Set(row.options.map(option => option.id))
  return {
    categoryId: form.categoryId, code: form.code || undefined, name: form.name.trim(), description: form.description,
    productImages: form.productImages.filter(image => image.url.trim()).map((image, index) => ({ ...image, url: image.url.trim(), displayOrder: index })),
    attributes: attributes.value.filter(hasAttributeValue).map((row, index) => ({
      definitionId: row.definitionId, name: row.definitionId ? undefined : row.name.trim(), dataType: row.dataType,
      valueText: row.dataType === 'TEXT' ? row.valueText?.trim() : undefined,
      valueNumber: row.dataType === 'NUMBER' ? row.valueNumber : undefined,
      unit: row.dataType === 'NUMBER' ? row.unit?.trim() || undefined : undefined,
      selectedOptionIds: row.selectedTokens.filter(token => optionIds(row).has(token)),
      selectedOptionValues: row.selectedTokens.filter(token => !optionIds(row).has(token)), displayOrder: index + 1
    })),
    variantAxes: axes.value.map((axis, axisIndex) => ({
      id: axis.id, clientKey: axis.clientKey, name: axis.name.trim(), nameSuggestionId: axis.nameSuggestionId, displayOrder: axisIndex + 1,
      values: axis.values.map((value, valueIndex) => ({ ...value, value: value.value.trim(), displayOrder: valueIndex + 1 }))
    })),
    variants: variants.value.filter(item => item.enabled).map(item => ({
      id: item.id, sku: item.sku.trim(), salePrice: item.salePrice, quantity: item.quantity, imageUrl: item.imageUrl?.trim() || undefined,
      defaultVariant: item.defaultVariant, status: item.status, selectionValueKeys: item.selectionValueKeys
    }))
  }
}

const normalizeValue = (value: string) => value.trim().toLocaleLowerCase('vi')
const validateAggregate = () => {
  if (!form.categoryId || !form.name.trim()) return void message.warning('Danh mục và tên sản phẩm là bắt buộc')
  const missingRequired = attributes.value.find(row => row.required && !hasAttributeValue(row))
  if (missingRequired) return void message.warning(`Thiếu thông số bắt buộc: ${missingRequired.name}`)
  const invalidCustom = attributes.value.find(row => !row.definitionId && hasAttributeValue(row) && !row.name.trim())
  if (invalidCustom) return void message.warning('Thuộc tính Seller tự thêm phải có tên')
  if (axes.value.length > 2) return void message.warning('Sản phẩm chỉ được có tối đa 2 trục biến thể')
  const axisNames = axes.value.map(axis => normalizeValue(axis.name))
  if (axisNames.some(name => !name) || new Set(axisNames).size !== axisNames.length) return void message.warning('Tên trục biến thể không được trống hoặc trùng nhau')
  const invalidAxis = axes.value.find(axis => {
    const values = axis.valueLabels.map(normalizeValue)
    return !values.length || values.some(value => !value) || new Set(values).size !== values.length
  })
  if (invalidAxis) return void message.warning(`Trục ${invalidAxis.name || 'chưa đặt tên'} cần giá trị không trống và không trùng`)
  if (matrixDirty.value) generateMatrix()
  if (matrixDirty.value) return false
  const enabled = variants.value.filter(item => item.enabled)
  if (!enabled.length || enabled.some(item => !item.sku.trim())) return void message.warning('Cần ít nhất một variant và mọi SKU phải có giá trị')
  const normalizedSkus = enabled.map(item => normalizeValue(item.sku))
  if (new Set(normalizedSkus).size !== normalizedSkus.length) return void message.warning('SKU phải duy nhất trong phạm vi sản phẩm')
  if (enabled.some(item => item.salePrice == null || item.salePrice < 0 || item.quantity == null || item.quantity < 0)) return void message.warning('Giá và tồn kho của variant phải lớn hơn hoặc bằng 0')
  return true
}

const openPreview = () => {
  if (!validateAggregate()) return
  previewPayload.value = buildPayload()
  previewOpen.value = true
}

const submit = async () => {
  if (!validateAggregate()) return
  saving.value = true
  try {
    const payload = previewPayload.value ?? buildPayload()
    if (editingId.value) await updateSellerProduct(editingId.value, payload)
    else await createSellerProduct(payload)
    message.success('Đã lưu product aggregate')
    previewOpen.value = false
    modalOpen.value = false
    await loadProducts()
  } catch (error: any) {
    message.error(error?.response?.data?.message ?? 'Không lưu được product aggregate')
  } finally { saving.value = false }
}

const toggleStatus = async (id: string, current?: EntityStatus) => {
  await changeSellerProductStatus(id, current === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE')
  await loadProducts()
}

const changePage = (next: { current: number; pageSize: number }) => {
  page.current = next.current
  page.size = next.pageSize
  void loadProducts()
}

onMounted(async () => {
  const [tree] = await Promise.all([getSellerCategoryTree(), searchAxisNames('')])
  categoryTree.value = tree
  await loadProducts()
})
</script>

<style scoped>
.seller-products { padding: 24px; background: #f6f8fb; min-height: 100%; }
.page-head, .section-title, .toolbar, .matrix-actions { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.page-head { margin-bottom: 18px; }
.page-head h2 { margin: 0; font-size: 24px; }
.page-head p { margin: 4px 0 0; color: #64748b; }
.toolbar { justify-content: flex-end; margin-bottom: 16px; }
.toolbar :deep(.ant-input-search) { max-width: 360px; }
.product-cell { display: flex; gap: 12px; align-items: center; }
.product-cell img { width: 48px; height: 48px; border-radius: 8px; object-fit: cover; }
.product-cell strong, .product-cell small { display: block; }
.product-cell small { color: #64748b; margin-top: 4px; }
.aggregate-form { display: grid; gap: 16px; max-height: 72vh; overflow-y: auto; padding-right: 6px; }
.grid { display: grid; gap: 12px; }
.grid.three { grid-template-columns: repeat(3, minmax(0, 1fr)); }
.attribute-grid { grid-template-columns: 1.2fr 160px minmax(280px, 2fr) 56px; align-items: end; }
.attribute-row, .axis-row { padding: 12px; border: 1px solid #e2e8f0; border-radius: 8px; margin-top: 12px; }
.attribute-flags { display: flex; gap: 6px; margin-bottom: 8px; }
.axis-row { display: grid; grid-template-columns: 220px 1fr 70px; gap: 12px; }
.inline-row { display: grid; grid-template-columns: 1fr 70px; gap: 10px; margin-top: 10px; }
.matrix-actions { justify-content: flex-start; flex-wrap: wrap; margin: 14px 0; }
.remove-button { margin-bottom: 24px; }
.preview-grid { display: grid; gap: 14px; max-height: 70vh; overflow-y: auto; }
@media (max-width: 900px) {
  .seller-products { padding: 12px; }
  .grid.three, .attribute-grid, .axis-row { grid-template-columns: 1fr; }
  .page-head { align-items: flex-start; }
}
</style>
