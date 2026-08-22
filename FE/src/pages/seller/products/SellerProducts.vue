<template>
  <div class="seller-page">
    <div class="page-head">
      <h2>Sản phẩm shop</h2>
      <a-space wrap>
        <a-input-search
          v-model:value="keyword"
          placeholder="Tìm tên hoặc mã"
          allow-clear
          style="width: 240px"
          @search="reloadActiveTab"
        />
        <a-button :loading="loading" title="Tải lại" @click="reloadActiveTab">
          <template #icon><ReloadOutlined /></template>
        </a-button>
        <a-button type="primary" @click="openCreate">
          <template #icon><PlusOutlined /></template>
          {{ activeTab === 'products' ? 'Thêm sản phẩm' : 'Thêm phân loại' }}
        </a-button>
      </a-space>
    </div>

    <a-tabs v-model:active-key="activeTab" @change="reloadActiveTab">
      <a-tab-pane key="products" tab="Sản phẩm">
        <a-table
          row-key="id"
          :columns="productColumns"
          :data-source="products"
          :loading="loading"
          :pagination="productPagination"
          @change="onProductPageChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'name'">
              <div class="strong">{{ record.name }}</div>
              <div class="muted">{{ record.code }}</div>
            </template>
            <template v-if="column.key === 'attributes'">
              {{ attributeSummary(record.attributes) }}
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="isActive(record.status) ? 'green' : 'default'">
                {{ isActive(record.status) ? 'Đang bán' : 'Đã ẩn' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'actions'">
              <a-space>
                <a-button size="small" title="Sửa" @click="editProduct(record.id)">
                  <template #icon><EditOutlined /></template>
                </a-button>
                <a-button size="small" title="Đổi trạng thái" @click="toggleProduct(record.id)">
                  <template #icon><PoweroffOutlined /></template>
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-tab-pane>

      <a-tab-pane key="variants" tab="Phân loại">
        <a-table
          row-key="id"
          :columns="variantColumns"
          :data-source="variants"
          :loading="loading"
          :pagination="variantPagination"
          @change="onVariantPageChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'variant'">
              <div class="variant-name">
                <img v-if="record.imageUrl" :src="record.imageUrl" :alt="record.name" />
                <div>
                  <div class="strong">{{ record.name }}</div>
                  <div class="muted">{{ record.tenMau || '-' }} / {{ record.kichThuoc || '-' }}</div>
                </div>
              </div>
            </template>
            <template v-if="column.key === 'price'">{{ currency(record.salePrice) }}</template>
            <template v-if="column.key === 'status'">
              <a-tag :color="isActive(record.status) ? 'green' : 'default'">
                {{ isActive(record.status) ? 'Đang bán' : 'Đã ẩn' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'actions'">
              <a-space>
                <a-button size="small" title="Sửa" @click="editVariant(record.id)">
                  <template #icon><EditOutlined /></template>
                </a-button>
                <a-button size="small" title="Đổi trạng thái" @click="toggleVariant(record.id)">
                  <template #icon><PoweroffOutlined /></template>
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>

    <a-modal v-model:open="productOpen" :title="productForm.id ? 'Sửa sản phẩm' : 'Thêm sản phẩm'" width="860px" ok-text="Lưu" :confirm-loading="saving" @ok="submitProduct">
      <a-form layout="vertical">
        <a-form-item label="Tên sản phẩm" required><a-input v-model:value="productForm.name" /></a-form-item>
        <a-form-item label="Mô tả"><a-textarea v-model:value="productForm.description" :rows="3" /></a-form-item>
        <a-form-item label="Danh mục" required>
          <a-select
            :value="productForm.idCategory"
            :options="categories"
            show-search
            option-filter-prop="label"
            @change="changeCategory"
          />
        </a-form-item>

        <div class="attribute-head">
          <div>
            <h3>Thuộc tính sản phẩm</h3>
            <span class="muted">{{ dynamicAttributes.length }}/50</span>
          </div>
          <a-button :disabled="!productForm.idCategory || dynamicAttributes.length >= 50" @click="addCustomAttribute">
            <template #icon><PlusOutlined /></template>
            Thêm thuộc tính
          </a-button>
        </div>

        <a-empty v-if="productForm.idCategory && !dynamicAttributes.length" :image="simpleImage" />
        <div v-for="(attribute, index) in dynamicAttributes" :key="attribute.rowKey" class="attribute-row">
          <div class="attribute-grid">
            <a-form-item label="Tên thuộc tính" required>
              <a-auto-complete
                v-if="!attribute.attributeId"
                v-model:value="attribute.name"
                :options="attributeNameOptions"
                @search="searchAttributeNames"
                @select="(value: string) => selectExistingAttribute(value, index)"
              />
              <a-input v-else :value="attribute.name" disabled />
            </a-form-item>
            <a-form-item label="Kiểu dữ liệu" required>
              <a-select
                v-model:value="attribute.dataType"
                :options="attributeTypeOptions"
                :disabled="!!attribute.attributeId"
                @change="resetAttributeValue(attribute)"
              />
            </a-form-item>
            <a-button class="remove-attribute" danger title="Xóa thuộc tính" @click="removeAttribute(index)">
              <template #icon><DeleteOutlined /></template>
            </a-button>
          </div>

          <a-form-item v-if="attribute.dataType === 'TEXT'" label="Giá trị" required>
            <a-input v-model:value="attribute.textValue" />
          </a-form-item>
          <div v-else-if="attribute.dataType === 'NUMBER'" class="number-grid">
            <a-form-item label="Giá trị" required><a-input-number v-model:value="attribute.numberValue" style="width: 100%" /></a-form-item>
            <a-form-item label="Đơn vị"><a-input v-model:value="attribute.unit" maxlength="50" /></a-form-item>
          </div>
          <template v-else>
            <a-form-item v-if="!attribute.attributeId" label="Danh sách lựa chọn" required>
              <a-select v-model:value="attribute.optionValues" mode="tags" :token-separators="[',']" />
            </a-form-item>
            <a-form-item label="Giá trị" required>
              <a-select
                v-if="attribute.attributeId"
                v-model:value="attribute.selectedOptionIds"
                :mode="attribute.dataType === 'MULTI_SELECT' ? 'multiple' : undefined"
                :options="attribute.options.map((option) => ({ value: option.id, label: option.value }))"
              />
              <a-select
                v-else
                v-model:value="attribute.selectedOptionValues"
                :mode="attribute.dataType === 'MULTI_SELECT' ? 'multiple' : undefined"
                :options="attribute.optionValues.map((value) => ({ value, label: value }))"
              />
            </a-form-item>
          </template>
        </div>
      </a-form>
    </a-modal>

    <a-modal v-model:open="variantOpen" :title="variantForm.id ? 'Sửa phân loại' : 'Thêm phân loại'" ok-text="Lưu" :confirm-loading="saving" @ok="submitVariant">
      <a-form layout="vertical">
        <a-form-item label="Sản phẩm" required>
          <a-select v-model:value="variantForm.idSP" :options="variantOptions.products" :disabled="!!variantForm.id" show-search option-filter-prop="label" />
        </a-form-item>
        <div class="form-grid">
          <a-form-item label="Màu sắc"><a-select v-model:value="variantForm.idMau" :options="variantOptions.colors" allow-clear /></a-form-item>
          <a-form-item label="Kích cỡ"><a-select v-model:value="variantForm.idSize" :options="variantOptions.sizes" allow-clear /></a-form-item>
          <a-form-item label="Số lượng" required><a-input-number v-model:value="variantForm.quantity" :min="0" style="width: 100%" /></a-form-item>
          <a-form-item label="Giá bán" required><a-input-number v-model:value="variantForm.salePrice" :min="0" :step="1000" style="width: 100%" /></a-form-item>
        </div>
        <a-form-item label="Ảnh sản phẩm">
          <input type="file" accept="image/*" @change="selectImage" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Empty, Modal, message } from 'ant-design-vue'
import { DeleteOutlined, EditOutlined, PlusOutlined, PoweroffOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import {
  changeSellerProductStatus,
  changeSellerVariantStatus,
  getSellerProduct,
  getSellerCategoryAttributes,
  getSellerProductOptions,
  getSellerProducts,
  getSellerVariant,
  getSellerVariantOptions,
  getSellerVariants,
  saveSellerProduct,
  saveSellerVariant,
  type CatalogOption,
  type AttributeDataType,
  type DynamicAttribute,
  type DynamicAttributeOption,
  type DynamicAttributePayload,
  type ProductPayload,
  type SellerProduct,
  type SellerProductVariant,
  type VariantPayload
} from '@/services/api/seller/product.api'

type SelectOption = { value: string; label: string }
type DynamicAttributeInput = {
  rowKey: string
  attributeId?: string
  name: string
  dataType: AttributeDataType
  options: DynamicAttributeOption[]
  textValue?: string
  numberValue?: number
  unit?: string
  optionValues: string[]
  selectedOptionIds: string | string[]
  selectedOptionValues: string | string[]
}

const activeTab = ref('products')
const keyword = ref('')
const loading = ref(false)
const saving = ref(false)
const productOpen = ref(false)
const variantOpen = ref(false)
const products = ref<SellerProduct[]>([])
const variants = ref<SellerProductVariant[]>([])
const categories = ref<SelectOption[]>([])
const dynamicAttributes = ref<DynamicAttributeInput[]>([])
const autocompleteAttributes = ref<DynamicAttribute[]>([])
const productPage = reactive({ current: 1, pageSize: 10, total: 0 })
const variantPage = reactive({ current: 1, pageSize: 10, total: 0 })

const productForm = reactive<ProductPayload>({ name: '', description: '' })
const variantForm = reactive<VariantPayload>({ idSP: '', idMau: '', idSize: '', quantity: 0, salePrice: 0 })
const variantOptions = reactive({ products: [] as SelectOption[], colors: [] as SelectOption[], sizes: [] as SelectOption[] })
const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE
const attributeTypeOptions = [
  { value: 'TEXT', label: 'Văn bản' },
  { value: 'NUMBER', label: 'Số' },
  { value: 'SINGLE_SELECT', label: 'Chọn một' },
  { value: 'MULTI_SELECT', label: 'Chọn nhiều' }
]
const attributeNameOptions = computed(() => autocompleteAttributes.value.map((item) => ({ value: item.name, label: item.name })))

const productColumns = [
  { title: 'Sản phẩm', key: 'name' },
  { title: 'Thuộc tính', key: 'attributes' },
  { title: 'Tồn kho', dataIndex: 'tongSP', width: 100 },
  { title: 'Trạng thái', key: 'status', width: 120 },
  { title: 'Thao tác', key: 'actions', width: 110 }
]
const variantColumns = [
  { title: 'Phân loại', key: 'variant' },
  { title: 'Giá bán', key: 'price', width: 150 },
  { title: 'Tồn kho', dataIndex: 'quantity', width: 100 },
  { title: 'Trạng thái', key: 'status', width: 120 },
  { title: 'Thao tác', key: 'actions', width: 110 }
]
const productPagination = computed(() => ({ current: productPage.current, pageSize: productPage.pageSize, total: productPage.total, showSizeChanger: true }))
const variantPagination = computed(() => ({ current: variantPage.current, pageSize: variantPage.pageSize, total: variantPage.total, showSizeChanger: true }))

const toOptions = (items: CatalogOption[]): SelectOption[] => items.map((item) => ({ value: item.id, label: item.name }))
const isActive = (status: unknown) => status === 'ACTIVE' || status === 0 || status === '0'
const currency = (value: number) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(value) || 0)
const asArray = (value: string | string[] | undefined) => Array.isArray(value) ? value : value ? [value] : []
const rowKey = () => `${Date.now()}-${Math.random().toString(36).slice(2)}`

const toAttributeInput = (item: DynamicAttribute): DynamicAttributeInput => ({
  rowKey: rowKey(),
  attributeId: item.attributeId,
  name: item.name,
  dataType: item.dataType,
  options: item.options || [],
  textValue: item.textValue,
  numberValue: item.numberValue,
  unit: item.unit,
  optionValues: (item.options || []).map((option) => option.value),
  selectedOptionIds: item.dataType === 'SINGLE_SELECT' ? item.selectedOptionIds?.[0] || '' : item.selectedOptionIds || [],
  selectedOptionValues: item.dataType === 'SINGLE_SELECT' ? '' : []
})

const hasAttributeValue = (attribute: DynamicAttributeInput) => {
  if (attribute.dataType === 'TEXT') return !!attribute.textValue?.trim()
  if (attribute.dataType === 'NUMBER') return attribute.numberValue !== undefined && attribute.numberValue !== null
  return attribute.attributeId
    ? asArray(attribute.selectedOptionIds).length > 0
    : asArray(attribute.selectedOptionValues).length > 0
}

const attributeSummary = (attributes?: DynamicAttribute[]) => {
  if (!attributes?.length) return '-'
  return attributes.slice(0, 3).map((item) => item.name).join(' · ') + (attributes.length > 3 ? ` +${attributes.length - 3}` : '')
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const response = await getSellerProducts({ page: productPage.current, size: productPage.pageSize, q: keyword.value || undefined })
    products.value = response.data?.data || []
    productPage.total = Number(response.data?.totalElements || 0)
  } catch {
    message.error('Không tải được sản phẩm shop')
  } finally {
    loading.value = false
  }
}

const fetchVariants = async () => {
  loading.value = true
  try {
    const response = await getSellerVariants({ page: variantPage.current, size: variantPage.pageSize, q: keyword.value || undefined })
    variants.value = response.data?.data || []
    variantPage.total = Number(response.data?.totalElements || 0)
  } catch {
    message.error('Không tải được phân loại sản phẩm')
  } finally {
    loading.value = false
  }
}

const reloadActiveTab = () => activeTab.value === 'products' ? fetchProducts() : fetchVariants()

const loadProductOptions = async () => {
  categories.value = toOptions(await getSellerProductOptions())
}

const loadCategoryAttributes = async (categoryId: string, current: DynamicAttribute[] = []) => {
  const suggested = await getSellerCategoryAttributes(categoryId)
  const currentById = new Map(current.map((item) => [item.attributeId, item]))
  const merged = suggested.map((item) => toAttributeInput({ ...item, ...(currentById.get(item.attributeId) || {}) }))
  const suggestedIds = new Set(suggested.map((item) => item.attributeId))
  current.filter((item) => !suggestedIds.has(item.attributeId)).forEach((item) => merged.push(toAttributeInput(item)))
  dynamicAttributes.value = merged.slice(0, 50)
}

const applyCategory = async (categoryId: string) => {
  productForm.idCategory = categoryId
  dynamicAttributes.value = []
  autocompleteAttributes.value = []
  await loadCategoryAttributes(categoryId)
}

const changeCategory = (categoryId: string) => {
  if (!productForm.idCategory || productForm.idCategory === categoryId || !dynamicAttributes.value.some(hasAttributeValue)) {
    void applyCategory(categoryId)
    return
  }
  Modal.confirm({
    title: 'Đổi danh mục sản phẩm?',
    content: 'Các giá trị thuộc tính hiện tại sẽ được xóa và thay bằng gợi ý của danh mục mới.',
    okText: 'Đổi danh mục',
    cancelText: 'Giữ lại',
    onOk: () => applyCategory(categoryId)
  })
}

const addCustomAttribute = () => {
  if (!productForm.idCategory) return void message.warning('Vui lòng chọn danh mục trước')
  if (dynamicAttributes.value.length >= 50) return void message.warning('Mỗi sản phẩm chỉ được tối đa 50 thuộc tính')
  dynamicAttributes.value.push({
    rowKey: rowKey(),
    name: '',
    dataType: 'TEXT',
    options: [],
    optionValues: [],
    selectedOptionIds: [],
    selectedOptionValues: []
  })
}

const removeAttribute = (index: number) => dynamicAttributes.value.splice(index, 1)

const resetAttributeValue = (attribute: DynamicAttributeInput) => {
  attribute.textValue = undefined
  attribute.numberValue = undefined
  attribute.unit = undefined
  attribute.optionValues = []
  attribute.selectedOptionIds = []
  attribute.selectedOptionValues = []
}

let autocompleteRequest = 0
const searchAttributeNames = async (query: string) => {
  if (!productForm.idCategory || !query.trim()) {
    autocompleteAttributes.value = []
    return
  }
  const requestId = ++autocompleteRequest
  const result = await getSellerCategoryAttributes(productForm.idCategory, query.trim())
  if (requestId === autocompleteRequest) autocompleteAttributes.value = result
}

const selectExistingAttribute = (name: string, index: number) => {
  const match = autocompleteAttributes.value.find((item) => item.name === name)
  if (!match) return
  if (dynamicAttributes.value.some((item, itemIndex) => itemIndex !== index && item.attributeId === match.attributeId)) {
    message.warning('Thuộc tính này đã có trong sản phẩm')
    return
  }
  dynamicAttributes.value.splice(index, 1, toAttributeInput(match))
}

const loadVariantOptions = async () => {
  const [productItems, colors, sizes] = await getSellerVariantOptions()
  Object.assign(variantOptions, { products: toOptions(productItems), colors: toOptions(colors), sizes: toOptions(sizes) })
}

const openCreate = async () => {
  if (activeTab.value === 'products') {
    Object.assign(productForm, { id: undefined, name: '', description: '', idCategory: undefined, attributes: [] })
    dynamicAttributes.value = []
    await loadProductOptions()
    productOpen.value = true
    return
  }
  Object.assign(variantForm, { id: undefined, idSP: '', idMau: '', idSize: '', quantity: 0, salePrice: 0, imageUrl: undefined })
  await loadVariantOptions()
  variantOpen.value = true
}

const editProduct = async (id: string) => {
  const [response] = await Promise.all([getSellerProduct(id), loadProductOptions()])
  const detail = response.data
  Object.assign(productForm, { id: detail.id, name: detail.name, description: detail.description, idCategory: detail.idCategory })
  if (detail.idCategory) await loadCategoryAttributes(detail.idCategory, detail.attributes || [])
  productOpen.value = true
}

const editVariant = async (id: string) => {
  const [response] = await Promise.all([getSellerVariant(id), loadVariantOptions()])
  const detail = response.data || {}
  Object.assign(variantForm, {
    id,
    idSP: '',
    idMau: detail.idColor,
    idSize: detail.idKichThuoc,
    quantity: Number(detail.quantity || 0),
    salePrice: Number(detail.salePrice || 0),
    imageUrl: undefined
  })
  variantOpen.value = true
}

const submitProduct = async () => {
  if (!productForm.name.trim()) return void message.warning('Vui lòng nhập tên sản phẩm')
  if (!productForm.idCategory) return void message.warning('Vui lòng chọn danh mục')
  if (dynamicAttributes.value.length > 50) return void message.warning('Mỗi sản phẩm chỉ được tối đa 50 thuộc tính')

  const usedAttributes = dynamicAttributes.value.filter(hasAttributeValue)
  const invalidCustom = usedAttributes.find((item) => !item.attributeId && !item.name.trim())
  if (invalidCustom) return void message.warning('Vui lòng nhập tên thuộc tính')
  const invalidDropdown = usedAttributes.find((item) =>
    !item.attributeId
    && (item.dataType === 'SINGLE_SELECT' || item.dataType === 'MULTI_SELECT')
    && item.optionValues.length === 0
  )
  if (invalidDropdown) return void message.warning('Vui lòng nhập danh sách lựa chọn cho thuộc tính dropdown')

  const attributes: DynamicAttributePayload[] = usedAttributes.map((item, index) => ({
    attributeId: item.attributeId,
    name: item.name.trim(),
    dataType: item.dataType,
    textValue: item.dataType === 'TEXT' ? item.textValue?.trim() : undefined,
    numberValue: item.dataType === 'NUMBER' ? item.numberValue : undefined,
    unit: item.dataType === 'NUMBER' ? item.unit?.trim() : undefined,
    optionValues: item.attributeId ? undefined : item.optionValues,
    selectedOptionIds: item.attributeId ? asArray(item.selectedOptionIds) : undefined,
    selectedOptionValues: item.attributeId ? undefined : asArray(item.selectedOptionValues),
    displayOrder: index
  }))
  saving.value = true
  try {
    await saveSellerProduct({ ...productForm, attributes })
    message.success('Đã lưu sản phẩm')
    productOpen.value = false
    await fetchProducts()
  } catch {
    message.error('Không lưu được sản phẩm')
  } finally {
    saving.value = false
  }
}

const submitVariant = async () => {
  if (!variantForm.id && !variantForm.idSP) return void message.warning('Vui lòng chọn sản phẩm')
  saving.value = true
  try {
    await saveSellerVariant(variantForm)
    message.success('Đã lưu phân loại')
    variantOpen.value = false
    await fetchVariants()
  } catch {
    message.error('Không lưu được phân loại')
  } finally {
    saving.value = false
  }
}

const toggleProduct = async (id: string) => {
  await changeSellerProductStatus(id)
  message.success('Đã đổi trạng thái sản phẩm')
  await fetchProducts()
}

const toggleVariant = async (id: string) => {
  await changeSellerVariantStatus(id)
  message.success('Đã đổi trạng thái phân loại')
  await fetchVariants()
}

const selectImage = (event: Event) => {
  variantForm.imageUrl = (event.target as HTMLInputElement).files?.[0]
}

const onProductPageChange = (pagination: any) => {
  productPage.current = pagination.current
  productPage.pageSize = pagination.pageSize
  fetchProducts()
}

const onVariantPageChange = (pagination: any) => {
  variantPage.current = pagination.current
  variantPage.pageSize = pagination.pageSize
  fetchVariants()
}

onMounted(fetchProducts)
</script>

<style scoped>
.seller-page { padding: 24px; }
.page-head { display: flex; justify-content: space-between; gap: 16px; align-items: center; margin-bottom: 8px; }
.page-head h2 { margin: 0; font-size: 22px; font-weight: 700; }
.strong { font-weight: 700; }
.muted { color: #64748b; font-size: 13px; margin-top: 2px; }
.variant-name { display: flex; align-items: center; gap: 10px; min-width: 220px; }
.variant-name img { width: 44px; height: 44px; object-fit: cover; border: 1px solid #e2e8f0; border-radius: 4px; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 12px; }
.attribute-head { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin: 8px 0 12px; }
.attribute-head > div { display: flex; align-items: baseline; gap: 10px; }
.attribute-head h3 { margin: 0; font-size: 16px; font-weight: 700; }
.attribute-row { border-top: 1px solid #e5e7eb; padding: 16px 0 4px; }
.attribute-grid { display: grid; grid-template-columns: minmax(0, 1.5fr) minmax(180px, 1fr) 40px; gap: 12px; align-items: end; }
.remove-attribute { width: 36px; height: 32px; margin-bottom: 24px; padding: 0; }
.number-grid { display: grid; grid-template-columns: minmax(0, 2fr) minmax(120px, 1fr); gap: 12px; }
@media (max-width: 768px) {
  .seller-page { padding: 16px; }
  .page-head { align-items: flex-start; flex-direction: column; }
  .form-grid { grid-template-columns: 1fr; }
  .attribute-grid { grid-template-columns: minmax(0, 1fr) 40px; }
  .attribute-grid :deep(.ant-form-item):nth-child(2) { grid-column: 1 / -1; grid-row: 2; }
  .remove-attribute { grid-column: 2; grid-row: 1; }
  .number-grid { grid-template-columns: 1fr; gap: 0; }
}
</style>
