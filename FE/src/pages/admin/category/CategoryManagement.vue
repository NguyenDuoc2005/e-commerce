<template>
  <div class="category-page">
    <div class="page-head">
      <div>
        <h2>Quản lý danh mục</h2>
        <p>Thiết lập cây ngành hàng và thuộc tính mô tả được gợi ý cho Seller.</p>
      </div>
      <a-space>
        <a-button :loading="loading" @click="refreshAll">Tải lại</a-button>
        <a-button type="primary" @click="startCreate()">Thêm danh mục gốc</a-button>
      </a-space>
    </div>

    <div class="category-layout">
      <section class="tree-panel panel">
        <div class="panel-head">
          <div><h3>Cây danh mục</h3><span>{{ flatCategories.length }} danh mục đang hiển thị</span></div>
          <a-button size="small" :disabled="!selectedId" @click="startCreate(selectedId)">Thêm danh mục con</a-button>
        </div>
        <a-input-search v-model:value="treeQuery" allow-clear placeholder="Tìm trong cây danh mục" />
        <a-spin :spinning="loading">
          <a-tree
            v-if="filteredTree.length"
            v-model:selected-keys="selectedKeys"
            v-model:expanded-keys="expandedKeys"
            block-node
            :tree-data="filteredTree"
            :field-names="{ title: 'name', key: 'id', children: 'children' }"
            @select="selectCategory"
          >
            <template #title="node">
              <span class="tree-title"><strong>{{ node.name }}</strong><small>{{ node.code }}</small></span>
            </template>
          </a-tree>
          <a-empty v-else-if="!loading" description="Chưa có danh mục phù hợp" />
        </a-spin>
      </section>

      <section class="detail-panel panel">
        <a-tabs v-model:active-key="activeTab">
          <a-tab-pane key="details" :tab="mode === 'create' ? 'Thêm danh mục' : 'Thông tin danh mục'">
            <a-alert
              v-if="mode === 'edit' && selectedCategory"
              type="info"
              show-icon
              :message="`Đang chỉnh sửa: ${selectedCategory.path}`"
              class="context-alert"
            />
            <a-form layout="vertical" class="category-form">
              <div class="form-grid">
                <a-form-item label="Tên danh mục" required>
                  <a-input v-model:value="form.name" placeholder="Ví dụ: Điện thoại & Phụ kiện" />
                </a-form-item>
                <a-form-item label="Danh mục cha">
                  <a-select
                    v-model:value="form.parentId"
                    allow-clear
                    show-search
                    option-filter-prop="label"
                    placeholder="Không có — danh mục gốc"
                    :options="parentOptions"
                  />
                </a-form-item>
                <a-form-item label="Mã danh mục">
                  <a-input v-model:value="form.code" placeholder="Tự sinh nếu để trống" />
                </a-form-item>
                <a-form-item label="Slug URL">
                  <a-input v-model:value="form.slug" placeholder="Tự sinh từ tên nếu để trống" />
                </a-form-item>
                <a-form-item label="Thứ tự hiển thị">
                  <a-input-number v-model:value="form.displayOrder" :min="0" :precision="0" class="full-width" />
                </a-form-item>
                <a-form-item label="Trạng thái">
                  <a-select v-model:value="form.status" :options="statusOptions" />
                </a-form-item>
              </div>
              <a-space wrap>
                <a-button type="primary" :loading="saving" @click="saveCategory">
                  {{ mode === 'create' ? 'Tạo danh mục' : 'Lưu thay đổi' }}
                </a-button>
                <a-button v-if="mode === 'edit'" @click="startCreate(form.parentId)">Tạo danh mục mới</a-button>
                <a-popconfirm
                  v-if="mode === 'edit'"
                  title="Ẩn danh mục này khỏi cây và storefront?"
                  ok-text="Ẩn danh mục"
                  cancel-text="Hủy"
                  @confirm="hideSelectedCategory"
                >
                  <a-button danger :loading="saving">Ẩn danh mục</a-button>
                </a-popconfirm>
              </a-space>
            </a-form>
          </a-tab-pane>

          <a-tab-pane key="suggestions" tab="Thuộc tính gợi ý" :disabled="!selectedId || mode !== 'edit'">
            <div class="suggestion-head">
              <div>
                <h3>Thuộc tính cho {{ selectedCategory?.name }}</h3>
                <p>Kéo thuộc tính đã hậu kiểm sang danh sách bên phải, hoặc bấm “Thêm”.</p>
              </div>
              <a-button type="primary" :loading="suggestionSaving" @click="saveSuggestions">Lưu gợi ý</a-button>
            </div>

            <div class="suggestion-layout">
              <div class="attribute-source">
                <h4>Thuộc tính đã hậu kiểm</h4>
                <a-input-search v-model:value="attributeQuery" allow-clear placeholder="Tìm thuộc tính" />
                <div class="attribute-list">
                  <div
                    v-for="attribute in availableAttributes"
                    :key="attribute.id"
                    class="attribute-card"
                    draggable="true"
                    @dragstart="startAttributeDrag($event, attribute.id)"
                  >
                    <div><strong>{{ attribute.name }}</strong><span>{{ attribute.code }} · {{ typeLabel(attribute.dataType) }}</span></div>
                    <a-button size="small" @click="addSuggestion(attribute.id)">Thêm</a-button>
                  </div>
                  <a-empty v-if="!availableAttributes.length" description="Không còn thuộc tính phù hợp" />
                </div>
              </div>

              <div class="suggestion-target" @dragover.prevent @drop.prevent="dropAttribute">
                <h4>Đang gán ({{ suggestions.length }})</h4>
                <a-table
                  row-key="definitionId"
                  size="small"
                  :columns="suggestionColumns"
                  :data-source="suggestions"
                  :pagination="false"
                  :loading="suggestionLoading"
                  :scroll="{ x: 700 }"
                >
                  <template #bodyCell="{ column, record, index }">
                    <template v-if="column.key === 'attribute'">
                      <div class="attribute-name"><strong>{{ record.name }}</strong><span>{{ typeLabel(record.dataType) }}</span></div>
                    </template>
                    <template v-else-if="column.key === 'required'">
                      <a-switch v-model:checked="record.required" />
                    </template>
                    <template v-else-if="column.key === 'filterable'">
                      <a-switch v-model:checked="record.filterable" />
                    </template>
                    <template v-else-if="column.key === 'displayOrder'">
                      <a-input-number v-model:value="record.displayOrder" :min="0" :precision="0" size="small" />
                    </template>
                    <template v-else-if="column.key === 'actions'">
                      <a-space>
                        <a-button size="small" :disabled="index === 0" @click="moveSuggestion(index, -1)">↑</a-button>
                        <a-button size="small" :disabled="index === suggestions.length - 1" @click="moveSuggestion(index, 1)">↓</a-button>
                        <a-button size="small" danger @click="removeSuggestion(record.definitionId)">Xóa</a-button>
                      </a-space>
                    </template>
                  </template>
                </a-table>
                <div v-if="!suggestions.length && !suggestionLoading" class="drop-empty">Thả thuộc tính vào đây</div>
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  configureAdminCategorySuggestions,
  createAdminCategory,
  getAdminCategorySuggestions,
  getAdminCategoryTree,
  updateAdminCategory,
  updateAdminCategoryStatus,
  type AdminCategoryNode,
  type CategoryAttributeSuggestion,
  type CategoryStatus
} from '@/services/api/admin/category.api'
import { getAdminProductAttributes, type AdminProductAttribute, type AttributeDataType } from '@/services/api/admin/product-attribute.api'

type FlatCategory = AdminCategoryNode & { parentId: string | null; path: string; displayOrder: number; status: CategoryStatus }
type EditableSuggestion = CategoryAttributeSuggestion

const loading = ref(false)
const saving = ref(false)
const suggestionLoading = ref(false)
const suggestionSaving = ref(false)
const tree = ref<AdminCategoryNode[]>([])
const attributes = ref<AdminProductAttribute[]>([])
const suggestions = ref<EditableSuggestion[]>([])
const selectedId = ref<string>()
const selectedKeys = ref<string[]>([])
const expandedKeys = ref<string[]>([])
const activeTab = ref('details')
const mode = ref<'create' | 'edit'>('create')
const treeQuery = ref('')
const attributeQuery = ref('')
const form = reactive({ name: '', code: '', slug: '', parentId: undefined as string | undefined, displayOrder: 0, status: 'ACTIVE' as CategoryStatus })

const statusOptions = [
  { value: 'ACTIVE', label: 'Đang hiển thị' },
  { value: 'INACTIVE', label: 'Ẩn' }
]
const suggestionColumns = [
  { title: 'Thuộc tính', key: 'attribute', width: 210 },
  { title: 'Bắt buộc', key: 'required', width: 90 },
  { title: 'Cho lọc', key: 'filterable', width: 80 },
  { title: 'Thứ tự', key: 'displayOrder', width: 95 },
  { title: 'Thao tác', key: 'actions', width: 175 }
]

const normalizeChildren = (children: AdminCategoryNode['children']) => Array.isArray(children) ? children : []
const flatten = (nodes: AdminCategoryNode[], parentId: string | null = null, parentPath = ''): FlatCategory[] =>
  nodes.flatMap((node, index) => {
    const path = parentPath ? `${parentPath} / ${node.name}` : node.name
    const current: FlatCategory = { ...node, children: normalizeChildren(node.children), parentId, path, displayOrder: node.displayOrder ?? index, status: node.status ?? 'ACTIVE' }
    return [current, ...flatten(normalizeChildren(node.children), node.id, path)]
  })
const flatCategories = computed(() => flatten(tree.value))
const selectedCategory = computed(() => flatCategories.value.find((item) => item.id === selectedId.value))
const allTreeKeys = computed(() => flatCategories.value.map((item) => item.id))

const filterTree = (nodes: AdminCategoryNode[], query: string): AdminCategoryNode[] => nodes.flatMap((node) => {
  const children = filterTree(normalizeChildren(node.children), query)
  if (node.name.toLocaleLowerCase('vi').includes(query) || node.code?.toLocaleLowerCase('vi').includes(query) || children.length) {
    return [{ ...node, children }]
  }
  return []
})
const filteredTree = computed(() => {
  const query = treeQuery.value.trim().toLocaleLowerCase('vi')
  return query ? filterTree(tree.value, query) : tree.value
})
const descendantIds = computed(() => {
  if (!selectedId.value) return new Set<string>()
  const found = new Set<string>()
  const visit = (id: string) => flatCategories.value.filter((item) => item.parentId === id).forEach((child) => { found.add(child.id); visit(child.id) })
  visit(selectedId.value)
  return found
})
const parentOptions = computed(() => flatCategories.value
  .filter((item) => mode.value === 'create' || (item.id !== selectedId.value && !descendantIds.value.has(item.id)))
  .map((item) => ({ value: item.id, label: item.path })))
const availableAttributes = computed(() => {
  const used = new Set(suggestions.value.map((item) => item.definitionId))
  const query = attributeQuery.value.trim().toLocaleLowerCase('vi')
  return attributes.value.filter((item) => !used.has(item.id) && (!query || item.name.toLocaleLowerCase('vi').includes(query) || item.code.toLocaleLowerCase('vi').includes(query)))
})

const errorMessage = (error: any, fallback: string) => error?.response?.data?.message ?? fallback
const typeLabel = (type: AttributeDataType | string) => ({ TEXT: 'Văn bản', NUMBER: 'Số', SELECT_ONE: 'Một lựa chọn', SELECT_MULTI: 'Nhiều lựa chọn' }[type] ?? type)
const resetForm = (parentId?: string) => Object.assign(form, { name: '', code: '', slug: '', parentId, displayOrder: 0, status: 'ACTIVE' })

const loadTree = async () => {
  tree.value = await getAdminCategoryTree()
  expandedKeys.value = allTreeKeys.value
}
const loadAttributes = async () => { attributes.value = await getAdminProductAttributes({ verified: true, status: 'ACTIVE' }) }
const refreshAll = async () => {
  loading.value = true
  try { await Promise.all([loadTree(), loadAttributes()]) }
  catch (error: any) { message.error(errorMessage(error, 'Không tải được dữ liệu quản lý danh mục')) }
  finally { loading.value = false }
}
const loadSuggestions = async (categoryId: string) => {
  suggestionLoading.value = true
  try {
    suggestions.value = (await getAdminCategorySuggestions(categoryId)).map((item, index) => ({ ...item, required: Boolean(item.required), filterable: Boolean(item.filterable), displayOrder: item.displayOrder ?? index }))
  } catch (error: any) { message.error(errorMessage(error, 'Không tải được thuộc tính gợi ý')); suggestions.value = [] }
  finally { suggestionLoading.value = false }
}
const selectCategory = async (keys: Array<string | number>) => {
  const id = String(keys[0] ?? '')
  if (!id) return
  selectedId.value = id
  selectedKeys.value = [id]
  const item = flatCategories.value.find((category) => category.id === id)
  if (!item) return
  mode.value = 'edit'
  Object.assign(form, { name: item.name, code: item.code ?? '', slug: item.slug ?? '', parentId: item.parentId ?? undefined, displayOrder: item.displayOrder, status: item.status })
  await loadSuggestions(id)
}
const startCreate = (parentId?: string) => {
  mode.value = 'create'
  selectedId.value = undefined
  selectedKeys.value = []
  activeTab.value = 'details'
  suggestions.value = []
  resetForm(parentId)
}
const saveCategory = async () => {
  if (!form.name.trim()) return message.warning('Vui lòng nhập tên danh mục')
  saving.value = true
  try {
    const body = { name: form.name.trim(), code: form.code.trim() || undefined, slug: form.slug.trim() || undefined, parentId: form.parentId ?? null, displayOrder: Number(form.displayOrder || 0) }
    const saved = mode.value === 'create' ? await createAdminCategory(body) : await updateAdminCategory(selectedId.value!, body)
    if (form.status === 'INACTIVE') await updateAdminCategoryStatus(saved.id, 'INACTIVE')
    message.success(mode.value === 'create' ? 'Đã tạo danh mục' : 'Đã cập nhật danh mục')
    await loadTree()
    if (form.status === 'ACTIVE') await selectCategory([saved.id])
    else startCreate()
  } catch (error: any) { message.error(errorMessage(error, 'Không lưu được danh mục')) }
  finally { saving.value = false }
}
const hideSelectedCategory = async () => {
  if (!selectedId.value) return
  saving.value = true
  try {
    await updateAdminCategoryStatus(selectedId.value, 'INACTIVE')
    message.success('Đã ẩn danh mục')
    await loadTree()
    startCreate()
  } catch (error: any) { message.error(errorMessage(error, 'Không ẩn được danh mục')) }
  finally { saving.value = false }
}

const addSuggestion = (definitionId: string) => {
  if (suggestions.value.some((item) => item.definitionId === definitionId)) return
  const attribute = attributes.value.find((item) => item.id === definitionId)
  if (!attribute) return
  suggestions.value.push({ definitionId, name: attribute.name, dataType: attribute.dataType, defaultUnit: attribute.defaultUnit, verified: attribute.verified, required: false, filterable: false, displayOrder: suggestions.value.length })
}
const startAttributeDrag = (event: DragEvent, id: string) => { event.dataTransfer?.setData('text/plain', id); if (event.dataTransfer) event.dataTransfer.effectAllowed = 'copy' }
const dropAttribute = (event: DragEvent) => addSuggestion(event.dataTransfer?.getData('text/plain') ?? '')
const removeSuggestion = (id: string) => { suggestions.value = suggestions.value.filter((item) => item.definitionId !== id); normalizeSuggestionOrder() }
const moveSuggestion = (index: number, delta: number) => {
  const target = index + delta
  if (target < 0 || target >= suggestions.value.length) return
  const next = [...suggestions.value]
  ;[next[index], next[target]] = [next[target], next[index]]
  suggestions.value = next
  normalizeSuggestionOrder()
}
const normalizeSuggestionOrder = () => suggestions.value.forEach((item, index) => { item.displayOrder = index })
const saveSuggestions = async () => {
  if (!selectedId.value) return
  suggestionSaving.value = true
  try {
    await configureAdminCategorySuggestions(selectedId.value, suggestions.value.map((item, index) => ({ definitionId: item.definitionId, required: item.required, filterable: item.filterable, displayOrder: item.displayOrder ?? index })))
    message.success('Đã lưu thuộc tính gợi ý')
    await loadSuggestions(selectedId.value)
  } catch (error: any) { message.error(errorMessage(error, 'Không lưu được thuộc tính gợi ý')) }
  finally { suggestionSaving.value = false }
}

onMounted(refreshAll)
</script>

<style scoped>
.category-page { padding: 24px; }
.page-head, .panel-head, .suggestion-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; }
.page-head { margin-bottom: 18px; }
.page-head h2, .panel-head h3, .suggestion-head h3, .attribute-source h4, .suggestion-target h4 { margin: 0; }
.page-head p, .panel-head span, .suggestion-head p { margin: 4px 0 0; color: #667085; }
.category-layout { display: grid; grid-template-columns: minmax(280px, .8fr) minmax(620px, 2fr); gap: 18px; align-items: start; }
.panel { background: #fff; border: 1px solid #dbe3ef; border-radius: 10px; padding: 18px; min-height: 600px; }
.tree-panel :deep(.ant-input-search) { margin: 16px 0; }
.tree-title { display: flex; flex-direction: column; line-height: 1.25; padding: 3px 0; }
.tree-title small { color: #98a2b3; font-size: 11px; }
.context-alert { margin-bottom: 18px; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 16px; }
.full-width { width: 100%; }
.suggestion-head { margin-bottom: 16px; }
.suggestion-layout { display: grid; grid-template-columns: minmax(230px, .7fr) minmax(480px, 1.5fr); gap: 16px; }
.attribute-source, .suggestion-target { border: 1px solid #e4e7ec; border-radius: 8px; padding: 14px; min-height: 430px; }
.attribute-source :deep(.ant-input-search) { margin: 12px 0; }
.attribute-list { display: grid; gap: 8px; max-height: 520px; overflow-y: auto; }
.attribute-card { border: 1px solid #dbe3ef; border-radius: 8px; padding: 10px; display: flex; align-items: center; justify-content: space-between; gap: 8px; cursor: grab; background: #fff; }
.attribute-card:hover { border-color: #54bddb; background: rgba(84, 189, 219, .05); }
.attribute-card div, .attribute-name { display: flex; min-width: 0; flex-direction: column; }
.attribute-card span, .attribute-name span { color: #667085; font-size: 12px; }
.drop-empty { margin-top: 16px; min-height: 120px; display: grid; place-items: center; border: 1px dashed #b9c4d2; border-radius: 8px; color: #98a2b3; }
@media (max-width: 1100px) { .category-layout, .suggestion-layout { grid-template-columns: 1fr; } .panel { min-height: auto; } }
@media (max-width: 640px) { .category-page { padding: 16px; } .page-head, .suggestion-head { flex-direction: column; } .form-grid { grid-template-columns: 1fr; } }
</style>
