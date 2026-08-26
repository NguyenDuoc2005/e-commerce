<template>
  <div class="attribute-admin-page">
    <div class="page-head">
      <div>
        <h2>Quản lý thuộc tính sản phẩm</h2>
        <p>Hậu kiểm thuộc tính mô tả và gợi ý tên trục biến thể cho catalog marketplace.</p>
      </div>
      <a-space wrap>
        <a-button :loading="loading || axisLoading" @click="refreshAll">
          <ReloadOutlined />
        </a-button>
        <a-button :loading="reindexing" @click="reindexSearch">
          Đồng bộ search
        </a-button>
      </a-space>
    </div>

    <div class="summary-grid">
      <div class="summary-item">
        <span>Tổng thuộc tính</span>
        <strong>{{ attributeStats.total }}</strong>
      </div>
      <div class="summary-item">
        <span>Chờ hậu kiểm</span>
        <strong>{{ attributeStats.pending }}</strong>
      </div>
      <div class="summary-item">
        <span>Đã chuẩn hóa</span>
        <strong>{{ attributeStats.verified }}</strong>
      </div>
      <div class="summary-item">
        <span>Gợi ý trục chuẩn</span>
        <strong>{{ axisSuggestions.length }}</strong>
      </div>
    </div>

    <a-tabs v-model:active-key="tab" class="admin-tabs" @change="handleTabChange">
      <a-tab-pane key="definitions" tab="Thuộc tính">
        <div class="toolbar">
          <a-input-search
            v-model:value="filters.q"
            allow-clear
            placeholder="Tìm theo tên thuộc tính"
            @search="loadAttributes"
          />
          <a-select
            v-model:value="filters.status"
            allow-clear
            placeholder="Trạng thái"
            :options="statusOptions"
            @change="loadAttributes"
          />
          <a-select
            v-model:value="filters.verified"
            allow-clear
            placeholder="Hậu kiểm"
            :options="verifiedOptions"
            @change="loadAttributes"
          />
          <a-select
            v-model:value="filters.categoryId"
            allow-clear
            show-search
            option-filter-prop="label"
            placeholder="Danh mục"
            :options="categoryOptions"
            @change="loadAttributes"
          />
        </div>

        <a-table
          row-key="id"
          :columns="attributeColumns"
          :data-source="attributes"
          :loading="loading"
          :pagination="{ pageSize: 10, showSizeChanger: true }"
          :scroll="{ x: 1120 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'name'">
              <div class="primary-cell">
                <strong>{{ record.name }}</strong>
                <span>{{ record.code }}</span>
              </div>
            </template>
            <template v-else-if="column.key === 'dataType'">
              <a-tag :color="typeColor(record.dataType)">{{ typeLabel(record.dataType) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'default'">
                {{ record.status === 'ACTIVE' ? 'Đang dùng' : 'Đã ẩn' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'verified'">
              <a-tag :color="record.verified ? 'blue' : 'gold'">
                {{ record.verified ? 'Đã duyệt' : 'Chờ duyệt' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'categoryIds'">
              <a-space wrap>
                <a-tag v-for="categoryId in record.categoryIds" :key="categoryId">
                  {{ categoryLabel(categoryId) }}
                </a-tag>
                <span v-if="!record.categoryIds?.length" class="muted">Chưa gắn</span>
              </a-space>
            </template>
            <template v-else-if="column.key === 'source'">
              <a-tag :color="record.creatorSellerId ? 'orange' : 'blue'">
                {{ record.creatorSellerId ? 'Seller' : 'Hệ thống/Admin' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'createdDate'">
              {{ formatDate(record.createdDate) }}
            </template>
            <template v-else-if="column.key === 'actions'">
              <a-space wrap>
                <a-button size="small" :disabled="!isSelectType(record.dataType)" @click="openOptions(record)">Option</a-button>
                <a-button size="small" @click="openStandardize(record)">Chuẩn hóa</a-button>
                <a-button size="small" :disabled="record.verified" @click="verifyAttribute(record)">Duyệt</a-button>
                <a-button size="small" @click="openMerge(record)">Gộp</a-button>
                <a-popconfirm
                  title="Ẩn thuộc tính này?"
                  ok-text="Ẩn"
                  cancel-text="Hủy"
                  :disabled="record.status === 'INACTIVE'"
                  @confirm="hideAttribute(record)"
                >
                  <a-button size="small" danger :disabled="record.status === 'INACTIVE'">Ẩn</a-button>
                </a-popconfirm>
              </a-space>
            </template>
            <template v-else-if="column.dataIndex">{{ record[column.dataIndex] ?? '—' }}</template>
          </template>
        </a-table>
      </a-tab-pane>

      <a-tab-pane key="options" tab="Option">
        <div class="toolbar option-toolbar">
          <a-select
            v-model:value="optionDefinitionId"
            show-search
            option-filter-prop="label"
            placeholder="Chọn thuộc tính SELECT_ONE/SELECT_MULTI"
            :options="selectAttributeOptions"
            @change="selectOptionDefinition"
          />
          <a-button :disabled="!optionDefinitionId" :loading="optionsLoading" @click="loadOptions">Tải lại</a-button>
        </div>
        <a-alert type="info" show-icon message="Option chỉ áp dụng cho thuộc tính kiểu một hoặc nhiều lựa chọn." class="context-alert" />
        <a-table row-key="id" :columns="optionColumns" :data-source="options" :loading="optionsLoading" :pagination="false">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'"><a-tag :color="record.status === 'ACTIVE' ? 'green' : 'default'">{{ record.status === 'ACTIVE' ? 'Đang dùng' : 'Đã ẩn' }}</a-tag></template>
            <template v-else-if="column.key === 'verified'"><a-tag :color="record.verified ? 'blue' : 'gold'">{{ record.verified ? 'Đã duyệt' : 'Chờ duyệt' }}</a-tag></template>
            <template v-else-if="column.key === 'actions'">
              <a-space>
                <a-button size="small" :disabled="record.verified" @click="verifyOption(record.id)">Duyệt</a-button>
                <a-button size="small" :disabled="options.length < 2" @click="openOptionMerge(record)">Gộp</a-button>
              </a-space>
            </template>
            <template v-else-if="column.dataIndex">{{ record[column.dataIndex] ?? '—' }}</template>
          </template>
        </a-table>
      </a-tab-pane>

      <a-tab-pane key="category-suggestions" tab="Danh mục gợi ý">
        <div class="toolbar suggestion-toolbar">
          <a-select v-model:value="suggestionCategoryId" show-search option-filter-prop="label" placeholder="Chọn danh mục" :options="categoryOptions" @change="loadCategorySuggestions" />
          <a-select v-model:value="suggestionDefinitionId" allow-clear show-search option-filter-prop="label" placeholder="Thêm thuộc tính đã duyệt" :options="availableSuggestionDefinitions" />
          <a-button :disabled="!suggestionDefinitionId" @click="addCategorySuggestion">Thêm</a-button>
          <a-button type="primary" :disabled="!suggestionCategoryId" :loading="suggestionSaving" @click="saveCategorySuggestions">Lưu gán</a-button>
        </div>
        <a-table row-key="definitionId" :columns="categorySuggestionColumns" :data-source="categorySuggestions" :loading="suggestionLoading" :pagination="false">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'attribute'"><div class="primary-cell"><strong>{{ record.name }}</strong><span>{{ typeLabel(record.dataType) }}</span></div></template>
            <template v-else-if="column.key === 'filterable'"><a-switch v-model:checked="record.filterable" /></template>
            <template v-else-if="column.key === 'required'"><a-switch v-model:checked="record.required" /></template>
            <template v-else-if="column.key === 'displayOrder'"><a-input-number v-model:value="record.displayOrder" :min="0" :precision="0" size="small" /></template>
            <template v-else-if="column.key === 'actions'"><a-button size="small" danger @click="removeCategorySuggestion(record.definitionId)">Xóa</a-button></template>
          </template>
        </a-table>
      </a-tab-pane>

      <a-tab-pane key="history" tab="Gộp & Lịch sử">
        <div class="section-split"><div><h3>Lịch sử hậu kiểm</h3><p>Nhật ký verify, chuẩn hóa, gộp và ẩn thuộc tính.</p></div><a-button :loading="historyLoading" @click="loadHistory">Tải lại</a-button></div>
        <a-table row-key="id" :columns="historyColumns" :data-source="audits" :loading="historyLoading" :pagination="{ pageSize: 10 }" :scroll="{ x: 1000 }">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'action'"><a-tag :color="actionColor(record.action)">{{ actionLabel(record.action) }}</a-tag></template>
            <template v-else-if="column.key === 'definitions'"><div class="primary-cell"><strong>{{ record.sourceDefinitionName }}</strong><span v-if="record.targetDefinitionName">→ {{ record.targetDefinitionName }}</span></div></template>
            <template v-else-if="column.key === 'actor'">{{ record.actorUserId || 'Hệ thống' }}</template>
            <template v-else-if="column.key === 'createdDate'">{{ formatDate(record.createdDate) }}</template>
            <template v-else-if="column.dataIndex">{{ record[column.dataIndex] ?? '—' }}</template>
          </template>
        </a-table>
      </a-tab-pane>

      <a-tab-pane key="axes" tab="Trục biến thể">
        <div class="toolbar axis-toolbar">
          <a-input-search
            v-model:value="axisQuery"
            allow-clear
            placeholder="Tìm tên trục biến thể"
            @search="loadAxes"
          />
          <a-button type="primary" @click="openAxisCreate()">Thêm trục gợi ý</a-button>
        </div>
        <a-table
          row-key="normalizedName"
          :columns="axisColumns"
          :data-source="axes"
          :loading="axisLoading"
          :pagination="{ pageSize: 10 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'displayName'">
              <div class="primary-cell">
                <strong>{{ record.displayName }}</strong>
                <span>{{ record.normalizedName }}</span>
              </div>
            </template>
            <template v-else-if="column.key === 'topValues'">
              <a-space wrap>
                <a-tag v-for="value in record.topValues" :key="value">{{ value }}</a-tag>
                <span v-if="!record.topValues?.length" class="muted">Chưa có giá trị</span>
              </a-space>
            </template>
            <template v-else-if="column.key === 'actions'">
              <a-button size="small" @click="openAxisCreate(record.displayName)">Tạo gợi ý chuẩn</a-button>
            </template>
            <template v-else-if="column.dataIndex">{{ record[column.dataIndex] ?? '—' }}</template>
          </template>
        </a-table>

        <div class="section-split">
          <div>
            <h3>Gợi ý trục chuẩn</h3>
            <p>Danh sách tên trục được chuẩn hóa để Seller chọn khi tạo biến thể.</p>
          </div>
        </div>
        <a-table
          row-key="id"
          :columns="axisSuggestionColumns"
          :data-source="axisSuggestions"
          :loading="axisLoading"
          :pagination="{ pageSize: 8 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'name'">
              <div class="primary-cell">
                <strong>{{ record.name }}</strong>
              </div>
            </template>
            <template v-else-if="column.key === 'verified'">
              <a-tag :color="record.verified ? 'blue' : 'gold'">
                {{ record.verified ? 'Đã duyệt' : 'Chờ duyệt' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'default'">
                {{ record.status === 'ACTIVE' ? 'Đang dùng' : 'Đã ẩn' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'actions'">
              <a-space>
                <a-button size="small" :disabled="record.verified" @click="verifyAxis(record.id)">Duyệt</a-button>
                <a-button size="small" :disabled="axisSuggestions.length < 2" @click="openAxisMerge(record)">Gộp</a-button>
                <a-popconfirm title="Ẩn gợi ý trục này?" ok-text="Ẩn" cancel-text="Hủy" @confirm="hideAxis(record.id)">
                  <a-button size="small" danger>Ẩn</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>

    <a-modal
      v-model:open="standardizeOpen"
      title="Chuẩn hóa thuộc tính"
      ok-text="Lưu"
      cancel-text="Hủy"
      :confirm-loading="saving"
      @ok="submitStandardize"
    >
      <a-form layout="vertical">
        <a-form-item label="Tên chuẩn" required>
          <a-input v-model:value="standardizeForm.name" placeholder="Ví dụ: Chất liệu" />
        </a-form-item>
        <a-form-item label="Đơn vị mặc định">
          <a-input
            v-model:value="standardizeForm.defaultUnit"
            :disabled="selectedAttribute?.dataType !== 'NUMBER'"
            placeholder="Chỉ dùng cho kiểu số"
          />
        </a-form-item>
        <a-form-item label="Danh mục gợi ý">
          <a-select
            v-model:value="standardizeForm.categoryIds"
            mode="multiple"
            show-search
            option-filter-prop="label"
            :options="categoryOptions"
            placeholder="Chọn danh mục"
          />
        </a-form-item>
        <a-form-item label="Lý do">
          <a-textarea v-model:value="standardizeForm.reason" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="optionMergeOpen" title="Gộp option" ok-text="Gộp" cancel-text="Hủy" :confirm-loading="saving" @ok="submitOptionMerge">
      <a-form layout="vertical">
        <a-form-item label="Option nguồn"><a-input :value="selectedOption?.value" disabled /></a-form-item>
        <a-form-item label="Gộp vào" required><a-select v-model:value="optionMergeTargetId" :options="optionMergeTargets" placeholder="Chọn option đích" /></a-form-item>
        <a-form-item label="Lý do"><a-textarea v-model:value="optionMergeReason" :rows="3" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="axisMergeOpen" title="Gộp gợi ý trục biến thể" ok-text="Gộp" cancel-text="Hủy" :confirm-loading="axisSaving" @ok="submitAxisMerge">
      <a-form layout="vertical">
        <a-form-item label="Gợi ý nguồn"><a-input :value="selectedAxisSuggestion?.name" disabled /></a-form-item>
        <a-form-item label="Gộp vào" required><a-select v-model:value="axisMergeTargetId" :options="axisMergeTargets" placeholder="Chọn gợi ý đích" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="mergeOpen"
      title="Gộp thuộc tính"
      ok-text="Gộp"
      cancel-text="Hủy"
      :confirm-loading="saving"
      @ok="submitMerge"
    >
      <a-alert
        type="warning"
        show-icon
        message="Thuộc tính nguồn sẽ bị ẩn và trỏ về thuộc tính đích cùng kiểu dữ liệu."
        class="modal-alert"
      />
      <a-form layout="vertical">
        <a-form-item label="Thuộc tính nguồn">
          <a-input :value="selectedAttribute?.name" disabled />
        </a-form-item>
        <a-form-item label="Gộp vào" required>
          <a-select
            v-model:value="mergeForm.targetId"
            show-search
            option-filter-prop="label"
            :options="mergeTargetOptions"
            placeholder="Chọn thuộc tính đích"
          />
        </a-form-item>
        <a-form-item label="Lý do">
          <a-textarea v-model:value="mergeForm.reason" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="axisCreateOpen"
      title="Thêm gợi ý trục biến thể"
      ok-text="Lưu"
      cancel-text="Hủy"
      :confirm-loading="axisSaving"
      @ok="submitAxisCreate"
    >
      <a-form layout="vertical">
        <a-form-item label="Tên trục" required>
          <a-input
            v-model:value="axisForm.name"
            autofocus
            placeholder="Ví dụ: Màu sắc, Kích cỡ, Dung tích"
            @press-enter="submitAxisCreate"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer
      v-model:open="optionsOpen"
      width="520"
      title="Giá trị lựa chọn"
      :destroy-on-close="true"
    >
      <template #extra>
        <a-button :loading="optionsLoading" @click="loadOptions">Tải lại</a-button>
      </template>
      <a-table
        row-key="id"
        size="small"
        :columns="optionColumns"
        :data-source="options"
        :loading="optionsLoading"
        :pagination="false"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'default'">
              {{ record.status === 'ACTIVE' ? 'Đang dùng' : 'Đã ẩn' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'verified'">
            <a-tag :color="record.verified ? 'blue' : 'gold'">
              {{ record.verified ? 'Đã duyệt' : 'Chờ duyệt' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-button size="small" :disabled="record.verified" @click="verifyOption(record.id)">Duyệt</a-button>
          </template>
          <template v-else-if="column.dataIndex">{{ record[column.dataIndex] ?? '—' }}</template>
        </template>
      </a-table>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { getCategoryTree } from '@/services/api/catalog/catalog.api'
import {
  getAdminProductAttributes,
  getProductAttributeModerationAudits,
  getProductAttributeOptions,
  hideProductAttribute,
  mergeProductAttribute,
  mergeProductAttributeOption,
  reindexProductAttributes,
  standardizeProductAttribute,
  verifyProductAttributeOption,
  verifyProductAttribute,
  type AdminProductAttribute,
  type AdminProductAttributeOption,
  type ProductAttributeModerationAudit,
  type AttributeDataType,
  type AttributeStatus
} from '@/services/api/admin/product-attribute.api'
import {
  createAxisSuggestion,
  getAxisInsights,
  getAxisSuggestions,
  hideAxisSuggestion,
  mergeAxisSuggestion,
  verifyAxisSuggestion,
  type AxisInsight,
  type AxisSuggestion
} from '@/services/api/admin/variant-axis.api'
import {
  configureAdminCategorySuggestions,
  getAdminCategorySuggestions,
  type CategoryAttributeSuggestion
} from '@/services/api/admin/category.api'

const tab = ref('definitions')
const loading = ref(false)
const saving = ref(false)
const reindexing = ref(false)
const attributes = ref<AdminProductAttribute[]>([])
const selectedAttribute = ref<AdminProductAttribute>()
const categoryOptions = ref<Array<{ value: string; label: string }>>([])
const filters = reactive<{ q: string; status?: AttributeStatus; verified?: boolean; categoryId?: string }>({
  q: '',
  status: undefined,
  verified: undefined,
  categoryId: undefined
})

const standardizeOpen = ref(false)
const standardizeForm = reactive({ name: '', defaultUnit: '', categoryIds: [] as string[], reason: '' })
const mergeOpen = ref(false)
const mergeForm = reactive({ targetId: undefined as string | undefined, reason: '' })
const optionsOpen = ref(false)
const optionsLoading = ref(false)
const options = ref<AdminProductAttributeOption[]>([])
const optionDefinitionId = ref<string>()
const selectedOption = ref<AdminProductAttributeOption>()
const optionMergeOpen = ref(false)
const optionMergeTargetId = ref<string>()
const optionMergeReason = ref('')

const suggestionCategoryId = ref<string>()
const suggestionDefinitionId = ref<string>()
const categorySuggestions = ref<CategoryAttributeSuggestion[]>([])
const suggestionLoading = ref(false)
const suggestionSaving = ref(false)
const audits = ref<ProductAttributeModerationAudit[]>([])
const historyLoading = ref(false)

const axisQuery = ref('')
const axisLoading = ref(false)
const axisSaving = ref(false)
const axes = ref<AxisInsight[]>([])
const axisSuggestions = ref<AxisSuggestion[]>([])
const axisCreateOpen = ref(false)
const axisForm = reactive({ name: '' })
const selectedAxisSuggestion = ref<AxisSuggestion>()
const axisMergeOpen = ref(false)
const axisMergeTargetId = ref<string>()

const statusOptions = [
  { value: 'ACTIVE', label: 'Đang dùng' },
  { value: 'INACTIVE', label: 'Đã ẩn' }
]
const verifiedOptions = [
  { value: true, label: 'Đã hậu kiểm' },
  { value: false, label: 'Chờ hậu kiểm' }
]
const attributeColumns = [
  { title: 'Thuộc tính', key: 'name', width: 240 },
  { title: 'Kiểu', key: 'dataType', width: 150 },
  { title: 'Trạng thái', key: 'status', width: 130 },
  { title: 'Hậu kiểm', key: 'verified', width: 130 },
  { title: 'Danh mục gợi ý', key: 'categoryIds', width: 260 },
  { title: 'Nguồn tạo', key: 'source', width: 150 },
  { title: 'Sản phẩm', dataIndex: 'productCount', width: 100 },
  { title: 'Ngày tạo', key: 'createdDate', width: 170 },
  { title: 'Thao tác', key: 'actions', fixed: 'right', width: 330 }
]
const axisColumns = [
  { title: 'Tên trục', key: 'displayName', width: 260 },
  { title: 'Số lần dùng', dataIndex: 'usageCount', width: 130 },
  { title: 'Giá trị thường gặp', key: 'topValues' },
  { title: 'Thao tác', key: 'actions', width: 180 }
]
const axisSuggestionColumns = [
  { title: 'Tên gợi ý', key: 'name' },
  { title: 'Trạng thái', key: 'status', width: 130 },
  { title: 'Hậu kiểm', key: 'verified', width: 130 },
  { title: 'Thao tác', key: 'actions', width: 160 }
]
const optionColumns = [
  { title: 'Giá trị', dataIndex: 'value', key: 'value' },
  { title: 'Trạng thái', key: 'status', width: 120 },
  { title: 'Hậu kiểm', key: 'verified', width: 120 },
  { title: 'Thao tác', key: 'actions', width: 170 }
]
const categorySuggestionColumns = [
  { title: 'Thuộc tính', key: 'attribute' },
  { title: 'Cho lọc', key: 'filterable', width: 100 },
  { title: 'Bắt buộc', key: 'required', width: 100 },
  { title: 'Thứ tự', key: 'displayOrder', width: 110 },
  { title: 'Thao tác', key: 'actions', width: 90 }
]
const historyColumns = [
  { title: 'Hành động', key: 'action', width: 130 },
  { title: 'Thuộc tính', key: 'definitions', width: 260 },
  { title: 'Người thực hiện', key: 'actor', width: 220 },
  { title: 'Lý do', dataIndex: 'reason' },
  { title: 'Sản phẩm ảnh hưởng', dataIndex: 'affectedProductCount', width: 150 },
  { title: 'Thời gian', key: 'createdDate', width: 180 }
]

const attributeStats = computed(() => ({
  total: attributes.value.length,
  pending: attributes.value.filter((item) => !item.verified).length,
  verified: attributes.value.filter((item) => item.verified).length
}))
const mergeTargetOptions = computed(() => attributes.value
  .filter((item) => selectedAttribute.value && item.id !== selectedAttribute.value.id && item.status === 'ACTIVE' && item.dataType === selectedAttribute.value.dataType)
  .map((item) => ({ value: item.id, label: `${item.name} (${typeLabel(item.dataType)})` })))
const selectAttributeOptions = computed(() => attributes.value
  .filter((item) => isSelectType(item.dataType) && item.status === 'ACTIVE')
  .map((item) => ({ value: item.id, label: `${item.name} (${typeLabel(item.dataType)})` })))
const optionMergeTargets = computed(() => options.value
  .filter((item) => item.id !== selectedOption.value?.id && item.status === 'ACTIVE')
  .map((item) => ({ value: item.id, label: item.value })))
const availableSuggestionDefinitions = computed(() => {
  const assigned = new Set(categorySuggestions.value.map((item) => item.definitionId))
  return attributes.value.filter((item) => item.verified && item.status === 'ACTIVE' && !assigned.has(item.id))
    .map((item) => ({ value: item.id, label: `${item.name} (${typeLabel(item.dataType)})` }))
})
const axisMergeTargets = computed(() => axisSuggestions.value
  .filter((item) => item.id !== selectedAxisSuggestion.value?.id && item.status === 'ACTIVE')
  .map((item) => ({ value: item.id, label: item.name })))

const typeLabel = (type: AttributeDataType) => ({
  TEXT: 'Văn bản',
  NUMBER: 'Số',
  SELECT_ONE: 'Một lựa chọn',
  SELECT_MULTI: 'Nhiều lựa chọn'
}[type] ?? type)
const typeColor = (type: AttributeDataType) => ({
  TEXT: 'default',
  NUMBER: 'purple',
  SELECT_ONE: 'cyan',
  SELECT_MULTI: 'geekblue'
}[type] ?? 'default')
const isSelectType = (type: AttributeDataType) => type === 'SELECT_ONE' || type === 'SELECT_MULTI'
const formatDate = (value?: number) => value ? new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value)) : '—'
const actionLabel = (action: string) => ({ VERIFY: 'Duyệt', STANDARDIZE: 'Chuẩn hóa', MERGE: 'Gộp', HIDE: 'Ẩn' }[action] ?? action)
const actionColor = (action: string) => ({ VERIFY: 'blue', STANDARDIZE: 'cyan', MERGE: 'purple', HIDE: 'red' }[action] ?? 'default')
const categoryLabel = (id: string) => categoryOptions.value.find((item) => item.value === id)?.label ?? 'Danh mục không còn hiển thị'
const errorMessage = (error: any, fallback: string) => error?.response?.data?.message ?? fallback
const categoryName = (item: any) => item?.name ?? item?.ten ?? item?.label ?? item?.title ?? item?.code ?? item?.id
const flattenCategories = (nodes: any[], parents: string[] = []): Array<{ value: string; label: string }> => {
  const safeNodes = Array.isArray(nodes) ? nodes : []
  return safeNodes.flatMap((node) => {
    const name = categoryName(node)
    const path = [...parents, name].filter(Boolean)
    const current = node?.id ? [{ value: node.id, label: path.join(' / ') }] : []
    return [...current, ...flattenCategories(Array.isArray(node?.children) ? node.children : [], path)]
  })
}

const loadAttributes = async () => {
  loading.value = true
  try {
    attributes.value = await getAdminProductAttributes({
      q: filters.q || undefined,
      status: filters.status,
      verified: filters.verified,
      categoryId: filters.categoryId
    })
  } catch (error: any) {
    message.error(errorMessage(error, 'Không tải được danh sách thuộc tính'))
  } finally {
    loading.value = false
  }
}
const loadAxes = async () => {
  axisLoading.value = true
  try {
    const q = axisQuery.value || undefined
    const [insights, suggestions] = await Promise.all([
      getAxisInsights(q),
      getAxisSuggestions(q)
    ])
    axes.value = insights
    axisSuggestions.value = suggestions
  } catch (error: any) {
    message.error(errorMessage(error, 'Không tải được trục biến thể'))
  } finally {
    axisLoading.value = false
  }
}
const refreshAll = async () => {
  await Promise.all([loadAttributes(), loadAxes()])
}
const loadCategories = async () => {
  const tree = await getCategoryTree()
  categoryOptions.value = flattenCategories(tree)
}
const ensureCategoryOptions = (categoryIds: string[]) => {
  const existingIds = new Set(categoryOptions.value.map((item) => item.value))
  const missingOptions = Array.from(new Set(categoryIds))
    .filter((id) => id && !existingIds.has(id))
    .map((id) => ({ value: id, label: categoryLabel(id) }))
  if (missingOptions.length) {
    categoryOptions.value = [...categoryOptions.value, ...missingOptions]
  }
}
const verifyAttribute = async (record: AdminProductAttribute) => {
  try {
    await verifyProductAttribute(record.id)
    message.success('Đã duyệt thuộc tính')
    await loadAttributes()
  } catch (error: any) {
    message.error(errorMessage(error, 'Không duyệt được thuộc tính'))
  }
}
const hideAttribute = async (record: AdminProductAttribute) => {
  try {
    await hideProductAttribute(record.id)
    message.success('Đã ẩn thuộc tính')
    await loadAttributes()
  } catch (error: any) {
    message.error(errorMessage(error, 'Không ẩn được thuộc tính'))
  }
}
const openStandardize = (record: AdminProductAttribute) => {
  selectedAttribute.value = record
  ensureCategoryOptions(record.categoryIds ?? [])
  Object.assign(standardizeForm, {
    name: record.name,
    defaultUnit: record.defaultUnit ?? '',
    categoryIds: Array.from(new Set(record.categoryIds ?? [])),
    reason: ''
  })
  standardizeOpen.value = true
}
const submitStandardize = async () => {
  if (!selectedAttribute.value || !standardizeForm.name.trim()) {
    message.warning('Vui lòng nhập tên chuẩn')
    return
  }
  saving.value = true
  try {
    await standardizeProductAttribute(selectedAttribute.value.id, {
      name: standardizeForm.name.trim(),
      defaultUnit: standardizeForm.defaultUnit?.trim() || undefined,
      categoryIds: Array.from(new Set(standardizeForm.categoryIds)),
      reason: standardizeForm.reason?.trim() || undefined
    })
    standardizeOpen.value = false
    message.success('Đã chuẩn hóa thuộc tính')
    await loadAttributes()
  } catch (error: any) {
    message.error(errorMessage(error, 'Không chuẩn hóa được thuộc tính'))
  } finally {
    saving.value = false
  }
}
const openMerge = (record: AdminProductAttribute) => {
  selectedAttribute.value = record
  Object.assign(mergeForm, { targetId: undefined, reason: '' })
  mergeOpen.value = true
}
const submitMerge = async () => {
  if (!selectedAttribute.value || !mergeForm.targetId) {
    message.warning('Vui lòng chọn thuộc tính đích')
    return
  }
  saving.value = true
  try {
    await mergeProductAttribute(selectedAttribute.value.id, mergeForm.targetId, mergeForm.reason?.trim() || undefined)
    mergeOpen.value = false
    message.success('Đã gộp thuộc tính')
    await loadAttributes()
  } catch (error: any) {
    message.error(errorMessage(error, 'Không gộp được thuộc tính'))
  } finally {
    saving.value = false
  }
}
const openOptions = async (record: AdminProductAttribute) => {
  selectedAttribute.value = record
  optionDefinitionId.value = record.id
  tab.value = 'options'
  await loadOptions()
}
const selectOptionDefinition = async (id: string) => {
  selectedAttribute.value = attributes.value.find((item) => item.id === id)
  await loadOptions()
}
const loadOptions = async () => {
  const definitionId = optionDefinitionId.value ?? selectedAttribute.value?.id
  if (!definitionId) return
  optionsLoading.value = true
  try {
    options.value = await getProductAttributeOptions(definitionId)
  } catch (error: any) {
    message.error(errorMessage(error, 'Không tải được giá trị lựa chọn'))
  } finally {
    optionsLoading.value = false
  }
}
const openOptionMerge = (record: AdminProductAttributeOption) => {
  selectedOption.value = record
  optionMergeTargetId.value = undefined
  optionMergeReason.value = ''
  optionMergeOpen.value = true
}
const submitOptionMerge = async () => {
  if (!selectedOption.value || !optionMergeTargetId.value) return message.warning('Vui lòng chọn option đích')
  saving.value = true
  try {
    await mergeProductAttributeOption(selectedOption.value.id, optionMergeTargetId.value, optionMergeReason.value.trim() || undefined)
    optionMergeOpen.value = false
    message.success('Đã gộp option')
    await loadOptions()
  } catch (error: any) { message.error(errorMessage(error, 'Không gộp được option')) }
  finally { saving.value = false }
}
const verifyOption = async (optionId: string) => {
  try {
    await verifyProductAttributeOption(optionId)
    message.success('Đã duyệt giá trị')
    await loadOptions()
  } catch (error: any) {
    message.error(errorMessage(error, 'Không duyệt được giá trị'))
  }
}
const loadCategorySuggestions = async () => {
  if (!suggestionCategoryId.value) { categorySuggestions.value = []; return }
  suggestionLoading.value = true
  try { categorySuggestions.value = await getAdminCategorySuggestions(suggestionCategoryId.value) }
  catch (error: any) { message.error(errorMessage(error, 'Không tải được gợi ý theo danh mục')); categorySuggestions.value = [] }
  finally { suggestionLoading.value = false }
}
const addCategorySuggestion = () => {
  const attribute = attributes.value.find((item) => item.id === suggestionDefinitionId.value)
  if (!attribute) return
  categorySuggestions.value.push({ definitionId: attribute.id, name: attribute.name, dataType: attribute.dataType, defaultUnit: attribute.defaultUnit, verified: attribute.verified, required: false, filterable: false, displayOrder: categorySuggestions.value.length })
  suggestionDefinitionId.value = undefined
}
const removeCategorySuggestion = (definitionId: string) => {
  categorySuggestions.value = categorySuggestions.value.filter((item) => item.definitionId !== definitionId)
  categorySuggestions.value.forEach((item, index) => { item.displayOrder = index })
}
const saveCategorySuggestions = async () => {
  if (!suggestionCategoryId.value) return
  suggestionSaving.value = true
  try {
    await configureAdminCategorySuggestions(suggestionCategoryId.value, categorySuggestions.value.map((item, index) => ({ definitionId: item.definitionId, required: item.required, filterable: item.filterable, displayOrder: item.displayOrder ?? index })))
    message.success('Đã lưu danh mục gợi ý')
    await Promise.all([loadCategorySuggestions(), loadAttributes()])
  } catch (error: any) { message.error(errorMessage(error, 'Không lưu được danh mục gợi ý')) }
  finally { suggestionSaving.value = false }
}
const loadHistory = async () => {
  historyLoading.value = true
  try { audits.value = await getProductAttributeModerationAudits() }
  catch (error: any) { message.error(errorMessage(error, 'Không tải được lịch sử hậu kiểm')) }
  finally { historyLoading.value = false }
}
const handleTabChange = async (key: string | number) => {
  if (key === 'history') await loadHistory()
  if (key === 'category-suggestions' && suggestionCategoryId.value) await loadCategorySuggestions()
  if (key === 'options' && optionDefinitionId.value) await loadOptions()
}
const openAxisCreate = (name = '') => {
  axisForm.name = name
  axisCreateOpen.value = true
}
const submitAxisCreate = async () => {
  if (!axisForm.name.trim()) {
    message.warning('Vui lòng nhập tên trục')
    return
  }
  axisSaving.value = true
  try {
    await createAxisSuggestion(axisForm.name.trim())
    axisCreateOpen.value = false
    message.success('Đã thêm gợi ý trục biến thể')
    await loadAxes()
  } catch (error: any) {
    message.error(errorMessage(error, 'Không thêm được gợi ý trục'))
  } finally {
    axisSaving.value = false
  }
}
const verifyAxis = async (id: string) => {
  try {
    await verifyAxisSuggestion(id)
    message.success('Đã duyệt gợi ý trục')
    await loadAxes()
  } catch (error: any) {
    message.error(errorMessage(error, 'Không duyệt được gợi ý trục'))
  }
}
const openAxisMerge = (record: AxisSuggestion) => {
  selectedAxisSuggestion.value = record
  axisMergeTargetId.value = undefined
  axisMergeOpen.value = true
}
const submitAxisMerge = async () => {
  if (!selectedAxisSuggestion.value || !axisMergeTargetId.value) return message.warning('Vui lòng chọn gợi ý trục đích')
  axisSaving.value = true
  try {
    await mergeAxisSuggestion(selectedAxisSuggestion.value.id, axisMergeTargetId.value)
    axisMergeOpen.value = false
    message.success('Đã gộp gợi ý trục')
    await loadAxes()
  } catch (error: any) { message.error(errorMessage(error, 'Không gộp được gợi ý trục')) }
  finally { axisSaving.value = false }
}
const hideAxis = async (id: string) => {
  try {
    await hideAxisSuggestion(id)
    message.success('Đã ẩn gợi ý trục')
    await loadAxes()
  } catch (error: any) {
    message.error(errorMessage(error, 'Không ẩn được gợi ý trục'))
  }
}
const reindexSearch = async () => {
  reindexing.value = true
  try {
    await reindexProductAttributes()
    message.success('Đã gửi yêu cầu đồng bộ search')
  } catch (error: any) {
    message.error(errorMessage(error, 'Không gửi được yêu cầu đồng bộ search'))
  } finally {
    reindexing.value = false
  }
}

onMounted(async () => {
  try {
    await loadCategories()
  } catch {
    categoryOptions.value = []
  }
  await refreshAll()
})
</script>

<style scoped>
.attribute-admin-page {
  padding: 20px;
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.page-head h2 {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 700;
  color: #101828;
}

.page-head p {
  margin: 0;
  color: #667085;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.summary-item {
  border: 1px solid #e4e7ec;
  border-radius: 8px;
  padding: 14px 16px;
  background: #ffffff;
}

.summary-item span {
  display: block;
  color: #667085;
  font-size: 13px;
  margin-bottom: 6px;
}

.summary-item strong {
  color: #101828;
  font-size: 24px;
  line-height: 1;
}

.admin-tabs {
  background: #ffffff;
  border: 1px solid #e4e7ec;
  border-radius: 8px;
  padding: 0 16px 16px;
}

.toolbar {
  display: grid;
  grid-template-columns: minmax(260px, 1.6fr) minmax(160px, 0.8fr) minmax(160px, 0.8fr) minmax(220px, 1fr);
  gap: 12px;
  margin: 8px 0 16px;
}

.option-toolbar {
  grid-template-columns: minmax(320px, 1fr) auto;
}

.suggestion-toolbar {
  grid-template-columns: minmax(220px, 1fr) minmax(260px, 1.2fr) auto auto;
}

.context-alert {
  margin-bottom: 16px;
}

.axis-toolbar {
  grid-template-columns: minmax(260px, 1fr) auto;
}

.primary-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.primary-cell span,
.muted {
  color: #667085;
  font-size: 12px;
}

.modal-alert {
  margin-bottom: 14px;
}

.section-split {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin: 24px 0 12px;
  padding-top: 18px;
  border-top: 1px solid #e4e7ec;
}

.section-split h3 {
  margin: 0 0 4px;
  color: #101828;
  font-size: 16px;
  font-weight: 700;
}

.section-split p {
  margin: 0;
  color: #667085;
}

@media (max-width: 900px) {
  .page-head {
    flex-direction: column;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .toolbar,
  .axis-toolbar {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 520px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
