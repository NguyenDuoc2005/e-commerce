<template>
  <div class="container mx-auto p-6 space-y-6">
    <div class="breadcrumb-section">
      <BreadcrumbDefault :pageTitle="'Chi tiết hóa đơn'" :routes="[
        { path: '/admin/hoa-don', name: 'Quản lý hóa đơn' },
        { path: '/admin/hoa-don-detai', name: 'Chi tiết hóa đơn' },
      ]" />
    </div>
    <!-- Timeline Trạng thái đơn hàng - Ngang -->
    <a-card title="TRẠNG THÁI ĐƠN HÀNG" bordered class="order-info-card">
      <template #extra>
        <!-- Button In PDF nằm phía ngoài cùng bên phải -->
        <a-button v-if="canShowPrintButton" type="primary" @click="handlePrintPDF" :loading="printLoading"
          style="background-color: #1890ff; border-color: #1890ff">
          <template #icon>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M6 9V2h12v7M6 18H4a2 2 0 01-2-2v-5a2 2 0 012-2h16a2 2 0 012 2v5a2 2 0 01-2 2h-2M6 14h12v8H6v-8z"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </template>
          In PDF
        </a-button>
      </template>

      <div class="order-timeline-horizontal">
        <div class="timeline-container-horizontal">
          <!-- Dynamic Timeline Steps -->
          <div v-for="(step, index) in timelineSteps" :key="step.key" class="timeline-step-horizontal"
            :class="getStepStatus(index)">
            <div class="step-content-horizontal">
              <div class="step-icon-horizontal">
                <div class="icon-circle-horizontal" :class="['icon-circle-horizontal', getIconClass(index)]">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path :d="step.icon" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                      stroke-linejoin="round" />
                  </svg>
                </div>
              </div>
              <div class="step-title-horizontal">{{ step.title }}</div>

              <div class="step-note-horizontal">
                {{ getTimelineData(index).note }}
              </div>

              <div class="step-time-horizontal">
                {{ getTimelineData(index).time }}
              </div>
            </div>
            <div v-if="
              index < timelineSteps.length - 1 && !isLastActiveStep(index)
            " class="step-line-horizontal" :class="getLineClass(index)"></div>
          </div>
        </div>
      </div>

      <!-- Buttons for status update -->
      <div class="mt-4 flex gap-2 justify-center">
        <a-button style="background-color: #58bddb; box-shadow: gray 1px 2px 2px" v-if="canConfirmOrder" type="primary"
          :disabled="isConfirmOrderDisabled" @click="handleConfirmOrderClick">{{ getConfirmButtonText() }}
        </a-button>
        <!-- <a-button
          style="background-color: #58bddb"
          v-if="canCompleteOrder"
          type="primary"
          :disabled="isCompleteOrderDisabled"
          @click="openStatusModal('HOAN_THANH')"
        >
          Hoàn thành đơn hàng
        </a-button> -->
        <a-button style="background-color: #58bddb; margin-left: 5px; box-shadow: gray 1px 2px 2px;"
          class="confirm-payment-btn" v-if="canCancelOrder1" type="primary" danger @click="openStatusModal('DA_HUY')">
          Hủy đơn hàng
        </a-button>
      </div>
    </a-card>

    <!-- Thông tin đơn hàng và khách hàng -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Thông tin đơn hàng -->
      <a-card title="THÔNG TIN ĐƠN HÀNG" bordered class="order-info-card">
        <div class="order-info-container">
          <!-- Cột trái -->
          <div class="order-info-column">
            <div class="order-info-row">
              <span class="label">Trạng thái:</span>
              <span class="value status">{{ displayStatus }}</span>
            </div>
            <div class="order-info-row">
              <span class="label">Mã đơn hàng:</span>
              <span class="value code">{{ hoaDon?.maHoaDon }}</span>
            </div>
            <div class="order-info-row">
              <span class="label">Phiếu giảm giá:</span>
              <span class="value code">{{ hoaDon?.tenPGG || 'Không có' }}</span>
            </div>
            <div class="order-info-row">
              <span class="label">Loại đơn hàng:</span>
              <span class="value status">
                {{
                  hoaDon?.loaiHoaDon === "OFFLINE"
                    ? "OFFLINE"
                    : hoaDon?.loaiHoaDon === "GIAO_HANG"
                      ? "Giao hàng"
                      : hoaDon?.loaiHoaDon === "ONLINE"
                        ? "ONLINE"
                        : "Chưa rõ"
                }}
              </span>
            </div>
            <div
              v-if="hoaDon?.phuongThucThanhToan == '0' && ((hoaDon?.thanhTien - hoaDon?.giaTriPGG) + hoaDon?.phiVanChuyen) - tongTienThanhToan > 0 && hoaDon.loaiHoaDon == 'ONLINE'"
              class="order-info-row">
              <span class="label">Dư nợ:</span>
              <span class="value price">{{
                formatCurrency(((hoaDon?.thanhTien - hoaDon?.giaTriPGG) + hoaDon?.phiVanChuyen) - tongTienThanhToan)
                }}</span>
            </div>
            <div
              v-if="hoaDon?.phuongThucThanhToan == '0' && ((hoaDon?.thanhTien - hoaDon?.giaTriPGG) + hoaDon?.phiVanChuyen) - tongTienThanhToan < 0 && hoaDon.loaiHoaDon == 'ONLINE'"
              class="order-info-row">
              <span class="label">Hoàn phí:</span>
              <span class="value price">{{
                formatCurrency(Math.abs(((hoaDon?.thanhTien - hoaDon?.giaTriPGG) + hoaDon?.phiVanChuyen) -
                  tongTienThanhToan))
              }}</span>
            </div>
          </div>
          <!-- Cột phải -->
          <div class="order-info-column">
            <div class="order-info-row">
              <span class="label">Phí vận chuyển:</span>
              <span class="value price">{{
                formatCurrency(hoaDon?.phiVanChuyen)
                }}</span>
            </div>
            <div class="order-info-row">
              <span class="label">Tổng tiền:</span>
              <span class="value price">{{
                // formatCurrency(subtotalAmount)
                formatCurrency(hoaDon?.thanhTien)
                }}</span>
            </div>
            <div class="order-info-row">
              <span class="label">Được giảm giá:</span>
              <span class="value price">{{
                formatCurrency(hoaDon?.giaTriPGG || 0)
                }}</span>
            </div>
            <div v-if="hoaDon?.phuongThucThanhToan == '0' && hoaDon?.loaiHoaDon == 'ONLINE'" class="order-info-row">
              <span class="label">Đã thanh toán:</span>
              <span class="value price">{{
                formatCurrency(tongTienThanhToan)
                }}</span>
            </div>
            <div
              v-if="hoaDon?.phuongThucThanhToan == '0' && ((hoaDon?.thanhTien - hoaDon?.giaTriPGG) + hoaDon?.phiVanChuyen) - tongTienThanhToan >= 0 && hoaDon?.loaiHoaDon == 'ONLINE'"
              class="order-info-row">
              <span class="label">Phải thanh toán:</span>
              <span class="value total">{{
                // formatCurrency(finalTotalAmount)
                formatCurrency(((hoaDon?.thanhTien - hoaDon?.giaTriPGG) + hoaDon?.phiVanChuyen) <= 0 ? 0 :
                  ((hoaDon?.thanhTien - hoaDon?.giaTriPGG) + hoaDon?.phiVanChuyen)) }}</span>
            </div>
            <div
              v-if="hoaDon?.phuongThucThanhToan == '0' && ((hoaDon?.thanhTien - hoaDon?.giaTriPGG) + hoaDon?.phiVanChuyen) - tongTienThanhToan < 0 && hoaDon?.loaiHoaDon == 'ONLINE'"
              class="order-info-row">
              <span class="label">Phải thanh toán:</span>
              <span class="value total">{{
                // formatCurrency(finalTotalAmount)
                formatCurrency(0)
                }}</span>
            </div>
            <div v-if="hoaDon?.phuongThucThanhToan == '1' && hoaDon?.loaiHoaDon == 'ONLINE'" class="order-info-row">
              <span class="label">Phải thanh toán:</span>
              <span class="value total">{{
                // formatCurrency(finalTotalAmount)
                formatCurrency(((hoaDon?.thanhTien - hoaDon?.giaTriPGG) + hoaDon?.phiVanChuyen) <= 0 ? 0 :
                  ((hoaDon?.thanhTien - hoaDon?.giaTriPGG) + hoaDon?.phiVanChuyen)) }}</span>
            </div>
            <div v-if="hoaDon?.loaiHoaDon == 'OFFLINE' || hoaDon?.loaiHoaDon == 'GIAO_HANG'" class="order-info-row">
              <span class="label">Phải thanh toán:</span>
              <span class="value total">{{
                // formatCurrency(finalTotalAmount)
                formatCurrency(((hoaDon?.thanhTien - hoaDon?.giaTriPGG) + hoaDon?.phiVanChuyen) <= 0 ? 0 :
                  ((hoaDon?.thanhTien - hoaDon?.giaTriPGG) + hoaDon?.phiVanChuyen)) }}</span>
            </div>
          </div>
        </div>
      </a-card>

      <!-- Thông tin khách hàng -->
      <a-card bordered class="order-info-card">
        <template #title>
          <div class="card-title">
            <span>THÔNG TIN KHÁCH HÀNG</span>
            <!-- <a-button
              v-if="canChangeCustomerInfo"
              type="primary"
              class="change-info-btn"
            >
              Thay đổi thông tin
            </a-button> -->
          </div>
        </template>

        <div class="order-info-container">
          <!-- Cột trái -->
          <div class="order-info-column">
            <div class="order-info-row">
              <span class="label">Tên khách hàng:</span>
              <span class="value code">{{ (hoaDon?.tenKhachHang == null ? hoaDon?.tenKhachHang2 : hoaDon?.tenKhachHang) || 'Khách lẻ'}}</span>
            </div>
            <div class="order-info-row">
              <span class="label">Email:</span>
              <span class="value email">{{ (hoaDon?.email == null ? hoaDon?.email2 : hoaDon?.email) || 'Không rõ' }}</span>
            </div>
          </div>
          <!-- Cột phải -->
          <div class="order-info-column">
            <div class="order-info-row">
              <span class="label">Số điện thoại:</span>
              <span class="value code">{{ (hoaDon?.sdtKH == null ? hoaDon?.sdtKH2 : hoaDon?.sdtKH) || 'Không rõ'}}</span>
            </div>
            <div class="order-info-row">
              <span class="label">Địa chỉ:</span>
              <span class="value address text-right max-w-xs">{{
               hoaDon?.loaiHoaDon == 'OFFLINE' ? hoaDon?.diaChi2 : ((hoaDon?.diaChi == null ? hoaDon?.diaChi2 : (hoaDon?.diaChi)) == ', undefined, undefined, undefined' ? 'Không rõ' : hoaDon?.diaChi) || 'Không rõ'}}</span>
            </div>
          </div>
        </div>
      </a-card>
    </div>

    <!-- Lịch sử thanh toán -->
    <a-card title="LỊCH SỬ THANH TOÁN" bordered class="order-info-card">
      <div class="flex justify-end mb-4">
        <a-button v-if="canConfirmPayment" style="background-color: #58bddb; box-shadow: gray 1px 2px 2px;"
          :key="hoaDon?.trangThaiHoaDon" type="primary"
          class="bg-yellow-500 hover:bg-yellow-600 border-yellow-500 confirm-payment-btn" @click="openPaymentModal">
          Xác nhận thanh toán
        </a-button>
      </div>
      <a-table :dataSource="lichSuThanhToan" :columns="paymentColumns" :pagination="false" class="custom-table"
        rowKey="id">
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'stt'">
            {{ index + 1 }}
          </template>
          <template v-if="column.key === 'nhanVienXacNhan'">
            {{ record.tenNhanVien }}
          </template>
          <template v-if="column.key === 'ghiChu'">
            {{ record.ghiChu }}
          </template>
          <template v-else-if="column.key === 'soTien'">
            <span class="font-semibold text-green-600">
              {{ formatCurrency(record.soTien) }}
            </span>
          </template>
          <template v-else-if="column.key === 'thoiGian'">
            <span class="text-sm">{{ formatDateTime(record.thoiGian) }}</span>
          </template>
          <template v-else-if="column.key === 'loaiGiaoDich'">
            <span class="text-sm">
              {{
                record.loaiGiaoDich === "0" ? "Tiền mặt" : "Chuyển khoản"
              }}
            </span>
          </template>
        </template>
      </a-table>
    </a-card>
    <a-card title="DANH SÁCH SẢN PHẨM" bordered class="order-info-card">
      <a-table :columns="columns" :data-source="state.chiTietList" :pagination="{
        current: state.paginationParams.page,
        pageSize: state.paginationParams.size,
        total: state.totalItems,
        showSizeChanger: true,
        pageSizeOptions: ['3', '5', '10', '20'],
      }" :scroll="{ y: 300 }" @change="handlePageChange" rowKey="maHoaDonChiTiet">
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'stt'">
            {{
              (state.paginationParams.page - 1) * state.paginationParams.size + index + 1
            }}
          </template>

          <template v-if="column.key === 'image'">
            <img :src="record.anhSanPham || 'placeholder-image.png'" alt="Ảnh sản phẩm"
              style="width: 60px; height: 60px; object-fit: cover" />
          </template>

          <template v-if="column.key === 'productInfo'">
            <div>
              <div>
                <strong>
                  {{ record.tenSanPham }} [{{ record.mauSac }} - {{ record.size }}]
                </strong>
              </div>
              <div>{{ record.thuongHieu }}</div>
              <div>Đơn giá: {{ formatCurrency(record.giaBan) }}</div>
            </div>
          </template>

          <template v-if="column.key === 'soLuong'">
            <a-input-number v-model:value="record.soLuong" :min="1" :max="999" size="small" :disabled="true" />
          </template>

          <template v-if="column.key === 'thanhTien'">
            {{ formatCurrency(record.soLuong * record.giaBan) }}
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
  <a-modal v-model:open="showStatusModal" title="Ghi chú trạng thái đơn hàng" :footer="null" :width="400"
    :maskClosable="false" @cancel="closeStatusModal">
    <div class="status-modal-content">
      <div class="status-selection">
        <p class="selection-label">*Nội dung ghi chú:</p>

        <a-radio-group v-model:value="selectedStatusTemplate" class="status-radio-group">
          <!-- <div class="radio-option">
            <a-radio value="confirmed">Đã xác nhận đơn hàng</a-radio>
          </div>
          Cinder
          <div class="radio-option">
            <a-radio value="sap_shipped"
              >Đơn hàng của bạn đã sẵn sàng để vận chuyển</a-radio
            >
          </div>
          <div class="radio-option">
            <a-radio value="shipped">Đã bàn giao cho đơn vị vận chuyển</a-radio>
          </div>

          <div class="radio-option">
            <a-radio value="payment_confirmed"
              >Đã xác nhận thông tin thanh toán đơn hàng</a-radio
            >
          </div>
          <div class="radio-option">
            <a-radio value="delivered"
              >Đơn hàng đã được giao thành công</a-radio
            >
          </div>
          <div class="radio-option">
            <a-radio value="cancelled">Đơn hàng đã bị hủy</a-radio>
          </div>
          <div class="radio-option">
            <a-radio value="other">Khác</a-radio>
          </div> -->
        </a-radio-group>
      </div>

      <div class="note-section">
        <a-textarea v-model:value="statusNote" :rows="4" placeholder="Đơn hàng đã được giao thành công"
          class="status-textarea" />
      </div>

      <div class="modal-actions">
        <a-button @click="closeStatusModal" class="cancel-btn"> Hủy </a-button>
        <a-popconfirm title="Bạn có chắc chắn muốn lưu thay đổi?" @confirm="confirmStatusChange">
          <a-button type="primary" :loading="statusUpdateLoading" class="confirm-btn">
            Xác nhận
          </a-button>
        </a-popconfirm>
      </div>
    </div>
  </a-modal>

  <a-modal v-model:open="showPaymentModal" title="Xác nhận thanh toán" :footer="null" :width="500" :maskClosable="false"
    @cancel="closePaymentModal">
    <div class="payment-modal-content">
      <!-- Tiền khách đưa -->
      <div class="payment-field">
        <label class="payment-label">
          <span class="required">*</span> Tiền khách đưa
        </label>
        <div class="payment-input-container">
          <a-input-number v-model:value="customerPayment" :min="0" :max="99999999"
            :formatter="(value) => `${value || 0}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
            :parser="(value) => value.replace(/\$\s?|(,*)/g, '')" placeholder="0" size="large" class="payment-input" />
          <span class="currency-label">VND</span>
        </div>
      </div>

      <!-- Ghi chú -->
      <div class="payment-field">
        <label class="payment-label">
          <span class="required">*</span> Ghi chú
        </label>
        <a-textarea v-model:value="paymentNote" :rows="4" placeholder="Nhập ghi chú..." class="payment-textarea" />
      </div>

      <!-- Phương thức thanh toán -->
      <!-- <div class="payment-methods">
        <div class="payment-method" :class="{ active: selectedPaymentMethod === 'cash' }"
          @click="selectedPaymentMethod = 'cash'">
          <div class="method-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path
                d="M12 2C13.1 2 14 2.9 14 4V6H18C19.1 6 20 6.9 20 8V18C20 19.1 19.1 20 18 20H6C4.9 20 4 19.1 4 18V8C4 6.9 4.9 6 6 6H10V4C10 2.9 10.9 2 12 2Z"
                fill="currentColor" />
            </svg>
          </div>
          <span>Tiền mặt</span>
        </div>

        <div class="payment-method" :class="{ active: selectedPaymentMethod === 'transfer' }"
          @click="selectedPaymentMethod = 'transfer'">
          <div class="method-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path
                d="M20 4H4C2.9 4 2 4.9 2 6V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V6C22 4.9 21.1 4 20 4ZM20 8L12 13L4 8V6L12 11L20 6V8Z"
                fill="currentColor" />
            </svg>
          </div>
          <span>Chuyển khoản</span>
        </div>
      </div> -->

      <!-- Thông tin thanh toán -->
      <div class="payment-summary">
        <div class="summary-row">
          <span>Số tiền cần thanh toán:</span>
          <span class="amount-required">{{
            formatCurrency(finalTotalAmount <= 0 ? 0 : finalTotalAmount) }}</span>
        </div>
        <div class="summary-row">
          <span>Tiền thừa trả khách:</span>
          <span class="amount-change" :class="{ negative: changeAmount < 0 }">
            {{ formatCurrency(changeAmount) }}
          </span>
        </div>
      </div>

      <!-- Buttons -->
      <div class="payment-actions">
        <a-button @click="closePaymentModal" class="cancel-btn"> Hủy </a-button>
        <a-popconfirm title="Bạn có chắc chắn muốn lưu thay đổi?" @confirm="confirmPayment">
          <a-button type="primary" :disabled="!canConfirmPayment" :loading="paymentLoading" class="confirm-payment-btn">
            Thanh toán
          </a-button>
        </a-popconfirm>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick, reactive, toRef } from "vue";
import { useRoute } from "vue-router";
import {
  getHoaDonChiTiets,
  updateOrderStatusInDatabase,
  GetLSTTHD,
  GetLSTT,
  thanhToan,
  inPDFOFFLINE,
  inPDFONLINE,
  type HoaDonResponse,
  type ParamsGetHoaDonCT,
} from "@/services/api/admin/hoadon.api";
import { toast } from 'vue3-toastify';
import type { TableColumnsType } from "ant-design-vue";
import BreadcrumbDefault from "@/components/ui/Breadcrumbs/BreadcrumbDefault.vue";
import "./HoaDon.css";
import { localStorageAction } from '@/utils/storage';
import { USER_INFO_STORAGE_KEY } from '@/constants/storageKey';

const route = useRoute();
const hoaDon = ref<any>(null);
const chiTietList = ref<any[]>([]);
const tongTien = ref(0);
const showStatusModal = ref(false);
const selectedStatusTemplate = ref("delivered");
const statusNote = ref("Đơn hàng đã được giao thành công");
const statusUpdateLoading = ref(false);
const pendingStatusChange = ref("");
const idNV = localStorageAction.get(USER_INFO_STORAGE_KEY)

// Payment modal state
const showPaymentModal = ref(false);
const customerPayment = ref(0);
const paymentNote = ref("");
const selectedPaymentMethod = ref("cash");
const paymentLoading = ref(false);
const lichSuThanhToan = ref<any[]>([]);

const closeStatusModal = () => {
  showStatusModal.value = false;
  pendingStatusChange.value = "";
  statusNote.value = "";
  selectedStatusTemplate.value = "delivered";
};

const currentStatus = ref(0);

enum EntityTrangThaiHoaDon {
  CHO_XAC_NHAN = 0,
  DA_XAC_NHAN = 1,
  CHO_GIAO = 2,
  DANG_GIAO = 3,
  HOAN_THANH = 4,
  DA_HUY = 5,
}

enum EntityLoaiHoaDon {
  OFFLINE = "OFFLINE",
  GIAO_HANG = "GIAO_HANG",
  ONLINE = "ONLINE",
}

const statusMapping: { [key: string]: number } = {
  CHO_XAC_NHAN: EntityTrangThaiHoaDon.CHO_XAC_NHAN,
  DA_XAC_NHAN: EntityTrangThaiHoaDon.DA_XAC_NHAN,
  CHO_GIAO: EntityTrangThaiHoaDon.CHO_GIAO,
  DANG_GIAO: EntityTrangThaiHoaDon.DANG_GIAO,
  HOAN_THANH: EntityTrangThaiHoaDon.HOAN_THANH,
  DA_HUY: EntityTrangThaiHoaDon.DA_HUY,
};

const statusTemplates = {
  confirmed: "Đơn hàng của bạn đã được xác nhận",
  sap_shipped: "Đơn hàng của bạn đã sẵn sàng để vận chuyển.",
  shipped: "Đơn hàng đã được bàn giao cho đơn vị vận chuyển.",
  delivered: "Đơn hàng đã được giao đến khách hàng thành công",
  cancelled: "Đơn hàng đã bị hủy.",
  other: "",
};

watch(selectedStatusTemplate, (newValue) => {
  if (newValue !== "other") {
    statusNote.value = statusTemplates[newValue] || "";
  } else {
    statusNote.value = "";
  }
});

const openStatusModal = (status: string) => {
  pendingStatusChange.value = status;
  setDefaultTemplate(status);
  showStatusModal.value = true;
};

const setDefaultTemplate = (status: string) => {
  switch (status) {
    case "DA_XAC_NHAN":
      selectedStatusTemplate.value = "confirmed";
      break;
    case "CHO_GIAO":
      selectedStatusTemplate.value = "sap_shipped";
      break;
    case "DANG_GIAO":
      selectedStatusTemplate.value = "shipped";
      break;
    case "HOAN_THANH":
      selectedStatusTemplate.value = "delivered";
      break;
    case "DA_HUY":
      selectedStatusTemplate.value = "cancelled";
      break;
    default:
      selectedStatusTemplate.value = "other";
  }

  statusNote.value = statusTemplates[selectedStatusTemplate.value] || "";
};

const tongTienThanhToan = computed(() =>
  (lichSuThanhToan.value || []).reduce((sum, item) => sum + (item.soTien || 0), 0)
);

// 1. Thêm vào phần khai báo biến reactive (sau dòng paymentLoading)
const printLoading = ref(false);

// 2. Thêm computed property để kiểm tra hiển thị button
const canShowPrintButton = computed(() => {
  return currentStatus.value > EntityTrangThaiHoaDon.CHO_XAC_NHAN;
});

// 3. Thêm hàm xử lý in PDF
const handlePrintPDF = async () => {
  if (!hoaDon.value?.maHoaDon) {
    toast.error("Không tìm thấy mã hóa đơn");
    return;
  }

  printLoading.value = true;

  try {
    const maHoaDon = hoaDon.value.maHoaDon;
    let response;

    // Sử dụng API functions đã định nghĩa thay vì fetch trực tiếp
    if (hoaDon.value.loaiHoaDon === EntityLoaiHoaDon.OFFLINE) {
      const blob = await inPDFOFFLINE(maHoaDon);
      downloadPDF(blob, `HoaDon_Offline_${maHoaDon}.pdf`);
    } else if (
      hoaDon.value.loaiHoaDon === EntityLoaiHoaDon.GIAO_HANG ||
      hoaDon.value.loaiHoaDon === EntityLoaiHoaDon.ONLINE
    ) {
      const blob = await inPDFONLINE(maHoaDon);
      downloadPDF(blob, `HoaDon_Online_${maHoaDon}.pdf`);
    } else {
      throw new Error("Loại hóa đơn không hợp lệ");
    }

    toast.success("Tải PDF thành công");
  } catch (error) {
    console.error("Lỗi khi in PDF:", error);
    toast.error("Có lỗi xảy ra khi in PDF: " + (error as Error).message);
  } finally {
    printLoading.value = false;
  }
};

// 4. Hàm helper để download PDF
const downloadPDF = (blob: Blob, fileName: string) => {
  try {
    // Kiểm tra blob có hợp lệ không
    if (!blob || blob.size === 0) {
      throw new Error("File PDF rỗng hoặc không hợp lệ");
    }

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = fileName;
    link.style.display = "none";

    // Trigger download
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    // Cleanup sau một khoảng thời gian ngắn
    setTimeout(() => {
      window.URL.revokeObjectURL(url);
    }, 100);
  } catch (error) {
    console.error("Lỗi khi download PDF:", error);
    toast.error("Có lỗi xảy ra khi tải file PDF");
  }
};

const confirmStatusChange = async () => {
  if (!pendingStatusChange.value) return;

  // Validate for offline order completion
  if (
    hoaDon.value?.loaiHoaDon === EntityLoaiHoaDon.OFFLINE &&
    pendingStatusChange.value === "HOAN_THANH" &&
    !hasPaymentHistory.value
  ) {
    toast.error("Vui lòng xác nhận thanh toán trước khi hoàn thành đơn hàng");
    return;
  }

  if (statusNote.value.length == 0) {
    toast.warning("Vui lòng nhập nội dung ghi chú");
    return;
  }

  if (statusNote.value.length > 255) {
    toast.warning("Nội dung vượt quá 255 ký tự");
    return;
  }

  statusUpdateLoading.value = true;

  try {
    const response = await updateOrderStatusInDatabase({
      maHoaDon: hoaDon.value.maHoaDon,
      status: pendingStatusChange.value,
      note: statusNote.value,
    });

    if (response.success || response.data) {
      // Cập nhật trạng thái ngay lập tức
      const newStatusValue = statusMapping[pendingStatusChange.value];

      // Cập nhật currentStatus trước
      currentStatus.value = newStatusValue;

      // Cập nhật hoaDon object với cách force reactivity
      hoaDon.value = {
        ...hoaDon.value,
        trangThaiHoaDon: pendingStatusChange.value,
      };

      toast.success("Cập nhật trạng thái thành công");

      // Refresh timeline data
      const idHoaDon = route.params.id as string;
      try {
        const statusResponse = await GetLSTTHD(idHoaDon);
        if (statusResponse && statusResponse.success && statusResponse.data) {
          timelineStatusData.value = [...statusResponse.data]; // Force reactivity
        }
      } catch (refreshError) {
        console.warn("Lỗi khi refresh dữ liệu timeline:", refreshError);
      }

      closeStatusModal();
    } else {
      toast.error("Cập nhật trạng thái thất bại");
    }
  } catch (error) {
    toast.error(
      "Có lỗi xảy ra khi cập nhật trạng thái: " + (error as Error).message
    );
  } finally {
    statusUpdateLoading.value = false;
  }
};

const hasPaymentHistory = computed(() => {
  return lichSuThanhToan.value.length > 0;
});

const canCancelOrder1 = computed(() => {
  return (
    currentStatus.value === EntityTrangThaiHoaDon.CHO_XAC_NHAN
    // currentStatus.value === EntityTrangThaiHoaDon.DA_XAC_NHAN
  );
});

// Computed properties for payment
const changeAmount = computed(() => {
  return (customerPayment.value || 0) - finalTotalAmount.value;
});

const canConfirmPaymentButton = computed(() => {
  return customerPayment.value > 0 && paymentNote.value.trim() !== "";
});

// Payment modal methods
const openPaymentModal = () => {
  // Set default values
  customerPayment.value = finalTotalAmount.value <= 0 ? 0 : finalTotalAmount.value;
  paymentNote.value = "Khách hàng thanh toán đơn hàng";
  selectedPaymentMethod.value = "cash";
  showPaymentModal.value = true;
};

const closePaymentModal = () => {
  showPaymentModal.value = false;
  customerPayment.value = 0;
  paymentNote.value = "";
  selectedPaymentMethod.value = "cash";
};

const confirmPayment = async () => {


  if (changeAmount.value < 0) {
    toast.warning("Số tiền khách đưa không đủ");
    return;
  }

  if (paymentNote.value.length > 255) {
    toast.warning("Ghi chú không được vượt quá 255 ký tự");
    return;
  }

  const soTienGoc = hoaDon.value?.thanhTien || 0

  paymentLoading.value = true;

  try {
    const paymentData = {
      hoaDonId: route.params.id as string,
      soTienKhachDua: customerPayment.value,
      soTienTraLai: Math.max(0, changeAmount.value),
      ghiChu: paymentNote.value,
      loaiGiaoDich:
        selectedPaymentMethod.value === "cash" ? "0" : "1",
      nhanVienId: idNV.userId,
      soTienGoc: soTienGoc,
      trangThai:
        hoaDon.value?.loaiHoaDon === EntityLoaiHoaDon.OFFLINE
          ? "CHO_XAC_NHAN"
          : "DANG_GIAO",
    };

    const response = await thanhToan(paymentData);

    if (response.success) {
      toast.success("Xác nhận thanh toán thành công");

      // Cập nhật trạng thái đơn hàng

      if (hoaDon.value?.loaiHoaDon !== EntityLoaiHoaDon.OFFLINE) {
        currentStatus.value = EntityTrangThaiHoaDon.DANG_GIAO;
        hoaDon.value.trangThaiHoaDon = "DANG_GIAO";
      }

      // Refresh cả timeline và lịch sử thanh toán
      await Promise.all([refreshTimelineData(), loadPaymentHistory()]);

      closePaymentModal();
    } else {
      toast.error("Xác nhận thanh toán thất bại");
    }
  } catch (error) {
    console.error("Lỗi xác nhận thanh toán:", error);
    toast.error("Có lỗi xảy ra khi xác nhận thanh toán");
  } finally {
    paymentLoading.value = false;
  }
};

// Thêm vào script setup
const loadPaymentHistory = async () => {
  try {
    const idHoaDon = route.params.id as string;
    const response = await GetLSTT(idHoaDon);

    if (response && response.success && response.data) {
      lichSuThanhToan.value = response.data;
    }
  } catch (error) {
    console.error("Lỗi khi tải lịch sử thanh toán:", error);
  }
};

const refreshTimelineData = async () => {
  try {
    const idHoaDon = route.params.id as string;
    const statusResponse = await GetLSTTHD(idHoaDon);
    if (statusResponse && statusResponse.success && statusResponse.data) {
      timelineStatusData.value = [...statusResponse.data];
    }
  } catch (error) {
    console.warn("Lỗi khi refresh dữ liệu timeline:", error);
  }
};

// Update the existing canConfirmPayment computed to use the new name
// Computed property to control visibility of the button that opens payment modal
const canConfirmPayment = computed(() => {
  // Force reactivity bằng cách access tất cả reactive values
  const invoiceData = hoaDon.value;
  const paymentHistory = lichSuThanhToan.value;
  const statusValue = currentStatus.value;

  // Kiểm tra lịch sử thanh toán rỗng
  const isPaymentHistoryEmpty = !paymentHistory || paymentHistory.length === 0;

  // Lấy loại hóa đơn & trạng thái
  const invoiceType = invoiceData?.loaiHoaDon;
  const invoiceStatus = invoiceData?.trangThaiHoaDon;

  console.log('=== DEBUG canConfirmPayment ===');
  console.log('invoiceType:', invoiceType);
  console.log('invoiceStatus:', invoiceStatus);
  console.log('invoiceStatusAsNumber:', statusValue);
  console.log('isPaymentHistoryEmpty:', isPaymentHistoryEmpty);
  console.log('paymentHistory length:', paymentHistory?.length);

  // Nếu không có dữ liệu hóa đơn => ẩn nút
  if (!invoiceData || invoiceType === undefined || invoiceStatus === undefined) {
    console.log('Missing invoice data');
    return false;
  }

  // Nếu đã có lịch sử thanh toán => ẩn nút
  if (!isPaymentHistoryEmpty) {
    console.log('Payment history is not empty');
    return false;
  }

  // Logic hiển thị nút - sử dụng cả string và number comparison
  if (invoiceType === 'OFFLINE') {
    const result = invoiceStatus === "0" || statusValue === 0;
    console.log('OFFLINE check result:', result);
    return result;
  }

  if (invoiceType === 'GIAO_HANG' || invoiceType === 'ONLINE') {
    const result = invoiceStatus === "3" || statusValue === 3;
    console.log('GIAO_HANG/ONLINE check result:', result);
    return result;
  }

  console.log('No matching condition');
  return false;
});

// Giải pháp 1: Thêm watcher để force update UI
watch([hoaDon, lichSuThanhToan, currentStatus], () => {
  console.log('Payment button dependencies changed');
  // Force re-evaluation
  nextTick(() => {
    console.log('canConfirmPayment current value:', canConfirmPayment.value);
  });
}, { deep: true, immediate: true });

const canAddProduct = computed(() => {
  return (
    currentStatus.value === EntityTrangThaiHoaDon.CHO_XAC_NHAN ||
    currentStatus.value === EntityTrangThaiHoaDon.CHO_GIAO
  );
});

const isAddProductDisabled = computed(() => {
  return !canAddProduct.value;
});

const isConfirmOrderDisabled = computed(() => {
  if (
    hoaDon.value?.loaiHoaDon === EntityLoaiHoaDon.OFFLINE &&
    !hasPaymentHistory.value
  ) {
    return true;
  }
  return false;
});

const isCompleteOrderDisabled = computed(() => {
  if (
    hoaDon.value?.loaiHoaDon === EntityLoaiHoaDon.OFFLINE &&
    !hasPaymentHistory.value
  ) {
    return true;
  }
  return false;
});

const offlineTimelineSteps = [
  {
    key: "CHO_XAC_NHAN",
    title: "Chờ xác nhận",
    status: EntityTrangThaiHoaDon.CHO_XAC_NHAN,
    icon: "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z",
  },
  {
    key: "HOAN_THANH",
    title: "Hoàn thành",
    status: EntityTrangThaiHoaDon.HOAN_THANH,
    icon: "M9 12L11 14L15 10M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z",
  },
  {
    key: "DA_HUY",
    title: "Đã hủy",
    status: EntityTrangThaiHoaDon.DA_HUY,
    icon: "M18 6L6 18M6 6L18 18",
  },
];

const deliveryTimelineSteps = [
  {
    key: "CHO_XAC_NHAN",
    title: "Chờ xác nhận",
    status: EntityTrangThaiHoaDon.CHO_XAC_NHAN,
    icon: "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z",
  },
  {
    key: "DA_XAC_NHAN",
    title: "Đã xác nhận",
    status: EntityTrangThaiHoaDon.DA_XAC_NHAN,
    icon: "M9 12L11 14L15 10M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z",
  },
  {
    key: "CHO_GIAO",
    title: "Chờ giao",
    status: EntityTrangThaiHoaDon.CHO_GIAO,
    icon: "M1 3H3L3.4 5M7 13H17L21 5H5.4M7 13L5.4 5M7 13L4.7 15.3C4.3 15.7 4.6 16.5 5.1 16.5H17M17 13V17C17 18.1 17.9 19 19 19S21 18.1 21 17V13M17 13H21M9 19.5C9.8 19.5 10.5 20.2 10.5 21S9.8 22.5 9 22.5 7.5 21.8 7.5 21 8.2 19.5 9 19.5ZM20 19.5C20.8 19.5 21.5 20.2 21.5 21S20.8 22.5 20 22.5 18.5 21.8 18.5 21 19.2 19.5 20 19.5Z",
  },
  {
    key: "DANG_GIAO",
    title: "Đang giao",
    status: EntityTrangThaiHoaDon.DANG_GIAO,
    icon: "M13 2L3 14h9l-1 8 10-12h-9l1-8z",
  },
  {
    key: "HOAN_THANH",
    title: "Hoàn thành",
    status: EntityTrangThaiHoaDon.HOAN_THANH,
    icon: "M9 12L11 14L15 10M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z",
  },
  {
    key: "DA_HUY",
    title: "Đã hủy",
    status: EntityTrangThaiHoaDon.DA_HUY,
    icon: "M18 6L6 18M6 6L18 18",
  },
];

const timelineSteps = computed(() => {
  const orderType = hoaDon.value?.loaiHoaDon;
  if (orderType === EntityLoaiHoaDon.OFFLINE) {
    return offlineTimelineSteps;
  }
  return deliveryTimelineSteps;
});

const paymentColumns = [
  { title: "#", key: "stt", width: 60, align: "center" },
  { title: "Số tiền", key: "soTien", width: 120, align: "center" },
  { title: "Thời gian", key: "thoiGian", width: 150, align: "center" },
  { title: "Loại giao dịch", key: "loaiGiaoDich", width: 150, align: "center" },
  {
    title: "Nhân viên xác nhận",
    key: "nhanVienXacNhan",
    width: 150,
    align: "center",
  },
  { title: "Ghi chú", key: "ghiChu", width: 200, align: "left" },
];

const getCurrentStepIndex = computed(() => {
  const current = currentStatus.value;
  console.log("Computing step index for status:", current);

  if (current === EntityTrangThaiHoaDon.DA_HUY) {
    const cancelIndex = timelineSteps.value.findIndex(
      (step) => step.key === "DA_HUY"
    );
    console.log("Cancel step index:", cancelIndex);
    return cancelIndex;
  }

  const stepIndex = timelineSteps.value.findIndex(
    (step) => step.status === current
  );
  console.log("Step index for status", current, ":", stepIndex);
  return stepIndex;
});

const forceUpdateUI = () => {
  hoaDon.value = { ...hoaDon.value };
  timelineStatusData.value = [...timelineStatusData.value];
};

const getStepStatus = (stepIndex: number) => {
  const currentStepIndex = getCurrentStepIndex.value;
  const currentStatusValue = currentStatus.value;

  if (currentStatusValue === EntityTrangThaiHoaDon.DA_HUY) {
    if (timelineSteps.value[stepIndex].key === "DA_HUY") return "current";
    return "pending";
  }

  if (stepIndex < currentStepIndex) return "completed";
  if (stepIndex === currentStepIndex) return "current";
  return "pending";
};

const getIconClass = (stepIndex: number) => {
  return getStepStatus(stepIndex);
};

const getLineClass = (stepIndex: number) => {
  const currentStepIndex = getCurrentStepIndex.value;

  if (currentStatus.value === EntityTrangThaiHoaDon.DA_HUY) {
    return "pending";
  }

  if (stepIndex < currentStepIndex) return "completed";
  return "pending";
};

const isLastActiveStep = (stepIndex: number) => {
  if (currentStatus.value === EntityTrangThaiHoaDon.DA_HUY) {
    return timelineSteps.value[stepIndex].key === "DA_HUY";
  }
  return stepIndex === timelineSteps.value.length - 1;
};

const getStatusText = (status: string | number) => {
  const statusTexts = [
    "Chờ xác nhận",
    "Đã xác nhận",
    "Chờ giao",
    "Đang giao",
    "Hoàn thành",
    "Đã hủy",
  ];
  const s = parseInt(status as string);
  return statusTexts[s] || "Không rõ";
};

const getTimelineData = (stepIndex: number) => {
  const step = timelineSteps.value[stepIndex];
  if (!step) return { title: "", note: "", time: "--:-- --/--/----" };

  const statusData = timelineStatusData.value.find((item) => {
    const itemStatus = parseInt(item.trangThai);
    return itemStatus === step.status;
  });

  return {
    title: step.title,
    note: statusData ? statusData.note : "",
    time: statusData ? formatDateTime(statusData.thoiGian) : "--:-- --/--/----",
  };
};

const timelineStatusData = ref<any[]>([]);

// Button visibility and actions
const canConfirmOrder = computed(() => {
  if (hoaDon.value?.loaiHoaDon === EntityLoaiHoaDon.OFFLINE) {
    return (
      currentStatus.value === EntityTrangThaiHoaDon.CHO_XAC_NHAN &&
      hasPaymentHistory.value
    );
  }

  // Nếu là trạng thái DANG_GIAO, phải check có lịch sử thanh toán
  if (currentStatus.value === EntityTrangThaiHoaDon.DANG_GIAO) {
    return hasPaymentHistory.value;
  }

  return (
    currentStatus.value === EntityTrangThaiHoaDon.CHO_XAC_NHAN ||
    currentStatus.value === EntityTrangThaiHoaDon.DA_XAC_NHAN ||
    currentStatus.value === EntityTrangThaiHoaDon.CHO_GIAO
  );
});

const getDisabledTooltip = computed(() => {
  if (
    (currentStatus.value === EntityTrangThaiHoaDon.DANG_GIAO ||
      (hoaDon.value?.loaiHoaDon === EntityLoaiHoaDon.OFFLINE &&
        currentStatus.value === EntityTrangThaiHoaDon.CHO_XAC_NHAN)) &&
    !hasPaymentHistory.value
  ) {
    return "Cần xác nhận thanh toán trước khi hoàn thành đơn hàng";
  }
  return "";
});

const handleConfirmOrderClick = () => {
  if (
    (currentStatus.value === EntityTrangThaiHoaDon.DANG_GIAO ||
      (hoaDon.value?.loaiHoaDon === EntityLoaiHoaDon.OFFLINE &&
        currentStatus.value === EntityTrangThaiHoaDon.CHO_XAC_NHAN)) &&
    !hasPaymentHistory.value
  ) {
    toast.warning(
      "Vui lòng xác nhận thanh toán trước khi hoàn thành đơn hàng"
    );
    return;
  }
  openStatusModal(getNextStatus());
};


const getNextStatus = () => {
  if (hoaDon.value?.loaiHoaDon === EntityLoaiHoaDon.OFFLINE) {
    return "HOAN_THANH";
  }

  switch (currentStatus.value) {
    case EntityTrangThaiHoaDon.CHO_XAC_NHAN:
      return "DA_XAC_NHAN";
    case EntityTrangThaiHoaDon.DA_XAC_NHAN:
      return "CHO_GIAO";
    case EntityTrangThaiHoaDon.CHO_GIAO:
      return "DANG_GIAO";
    case EntityTrangThaiHoaDon.DANG_GIAO:
      return "HOAN_THANH";
    default:
      return "HOAN_THANH";
  }
};

const getConfirmButtonText = () => {
  if (hoaDon.value?.loaiHoaDon === EntityLoaiHoaDon.OFFLINE) {
    return "Hoàn thành";
  }

  switch (currentStatus.value) {
    case EntityTrangThaiHoaDon.CHO_XAC_NHAN:
      return "Xác nhận đơn hàng";
    case EntityTrangThaiHoaDon.DA_XAC_NHAN:
      return "Chuẩn bị giao hàng";
    case EntityTrangThaiHoaDon.CHO_GIAO:
      return "Bắt đầu giao hàng";
    case EntityTrangThaiHoaDon.DANG_GIAO:
      return "Hoàn thành";
    default:
      return "Tiếp tục";
  }
};

const formatDateTime = (dateString: string) => {
  if (!dateString) return "--:-- --/--/----";
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) {
      console.error("Invalid date string:", dateString);
      return "--:-- --/--/----";
    }

    return date.toLocaleString("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  } catch (error) {
    console.error("Error formatting date:", dateString, error);
    return "--:-- --/--/----";
  }
};

const subtotalAmount = computed(() => {
  return hoaDon.value?.thanhTien || 0;
});

// Final amount to be paid (after discount and shipping)
const finalTotalAmount = computed(() => {
  const subtotal = hoaDon.value?.thanhTien || 0;
  const discount = hoaDon.value?.giaTriPGG || 0;
  const shipping = hoaDon.value?.phiVanChuyen || 0;

  // Final amount = (subtotal - discount) + shipping
  return (subtotal - discount) + shipping;
});

watch([subtotalAmount, finalTotalAmount], ([newSubtotal, newFinalTotal]) => {
  if (hoaDon.value) {
    hoaDon.value.tongTienSauGiam = newFinalTotal;
  }
});

const displayStatus = computed(() => {
  return getStatusText(currentStatus.value);
});

const debugTimelineData = () => {
  console.log("=== DEBUG TIMELINE DATA ===");
  console.log("Current status:", currentStatus.value);
  console.log("Timeline steps:", timelineSteps.value);
  console.log("Timeline status data from API:", timelineStatusData.value);

  timelineSteps.value.forEach((step, index) => {
    const data = getTimelineData(index);
    console.log(`Step ${index} (${step.title}):`, data);
  });
  console.log("=== END DEBUG ===");
};

watch(
  [timelineStatusData, currentStatus],
  () => {
    console.log("Timeline data or status changed");
    debugTimelineData();
  },
  { deep: true }
);

const formatCurrency = (value: number | undefined | null) => {
  if (value === undefined || value === null) return "0 ₫";
  return value.toLocaleString("vi-VN", { style: "currency", currency: "VND" });
};


const emit = defineEmits(["page-change"]);

const handlePageChange = (pagination: any) => {
  state.paginationParams.page = pagination.current;
  state.paginationParams.size = pagination.pageSize;
  fetchHoaDonChiTiet();
};

const state = reactive({
  chiTietList: [] as any[],
  paginationParams: {
    page: 1,
    size: 3,
  },
  totalItems: 0,
});

const fetchHoaDonChiTiet = async () => {
  const maHoaDon = route.params.maHoaDon as string;

  if (!maHoaDon) {
    toast.error("Không tìm thấy mã hóa đơn");
    return;
  }

  try {
    const res = await getHoaDonChiTiets({
      maHoaDon,
      page: state.paginationParams.page,
      size: state.paginationParams.size,
    });

    state.chiTietList = res.data?.content || [];
    state.totalItems = res.data?.totalElements || 0;
  } catch (error) {
    console.error("Lỗi khi tải chi tiết hóa đơn:", error);
    toast.error("Không thể tải dữ liệu sản phẩm");
  }
};

const columns: TableColumnsType = [
  {
    title: "STT",
    key: "stt",
    width: 50,
    align: "center",
  },
  {
    title: "Ảnh",
    key: "image",
    width: 70,
    align: "center",
  },
  {
    title: "Thông tin sản phẩm",
    key: "productInfo",
    width: 250,
  },
  {
    title: "Số lượng",
    key: "soLuong",
    width: 100,
    align: "center",
  },
  {
    title: "Thành tiền",
    key: "thanhTien",
    width: 120,
    align: "right",
  },
];

onMounted(async () => {
  const maHoaDon = route.params.maHoaDon as string;
  const idHoaDon = route.params.id as string;

  console.log("Route params:", { maHoaDon, idHoaDon });

  try {
    if (!idHoaDon) {
      console.error("idHoaDon is missing from route params");
      toast.error("Không tìm thấy ID hóa đơn");
      return;
    }

    const params: ParamsGetHoaDonCT = {
      maHoaDon: route.params.maHoaDon as string,
      page: state.paginationParams.page,
      size: state.paginationParams.size,
    };

    // Gọi API GetLSTTHD
    console.log("Calling GetLSTTHD with idHoaDon:", idHoaDon);
    const statusResponse = await GetLSTTHD(idHoaDon);
    console.log("GetLSTTHD response:", statusResponse);

    if (statusResponse && statusResponse.success && statusResponse.data) {
      timelineStatusData.value = statusResponse.data;
      console.log("Timeline status data loaded:", timelineStatusData.value);
    } else {
      console.warn("GetLSTTHD returned invalid data:", statusResponse);
      timelineStatusData.value = [];
    }

    // Gọi API lấy chi tiết hóa đơn
    if (!maHoaDon) {
      console.error("maHoaDon is missing from route params");
      toast.error("Không tìm thấy mã hóa đơn");
      return;
    }

    console.log("Calling getHoaDonChiTiets with maHoaDon:", maHoaDon);
    const res = await getHoaDonChiTiets(params);
    console.log("getHoaDonChiTiets response:", res);

    state.products = res.data?.content || [];
    state.totalItems = res.data?.totalElements || 0;

    if (res && res.data?.content && res.data?.content.length > 0) {
      const hoaDonData = res.data.content[0];

      hoaDon.value = {
        maHoaDon: hoaDonData.maHoaDon,
        tenHoaDon: hoaDonData.tenHoaDon,
        tenKhachHang: hoaDonData.tenKhachHang,
        sdtKH: hoaDonData.sdtKH,
        email: hoaDonData.email,
        diaChi: hoaDonData.diaChi,
        tenKhachHang2: hoaDonData.tenKhachHang2,
        sdtKH2: hoaDonData.sdtKH2,
        email2: hoaDonData.email2,
        diaChi2: hoaDonData.diaChi2,
        loaiHoaDon:
          hoaDonData.loaiHoaDon == "0"
            ? EntityLoaiHoaDon.OFFLINE
            : hoaDonData.loaiHoaDon == "1"
              ? EntityLoaiHoaDon.GIAO_HANG
              : EntityLoaiHoaDon.ONLINE,
        trangThaiHoaDon: hoaDonData.trangThaiHoaDon,
        phuongThucThanhToan: hoaDonData.phuongThucThanhToan,
        ngayTao: hoaDonData.ngayTao,
        phiVanChuyen: hoaDonData.phiVanChuyen,
        maPGG: hoaDonData.maVoucher,
        tenPGG: hoaDonData.tenVoucher,
        giaTriPGG: hoaDonData.giaTriVoucher,
        thanhTien: hoaDonData.thanhTien,
        tongTienSauGiam: hoaDonData.tongTienSauGiam,
      };

      // Đảm bảo currentStatus được set đúng và trigger reactivity
      const statusValue = parseInt(hoaDonData.trangThaiHoaDon);
      currentStatus.value = statusValue;

      console.log("Current status set to:", currentStatus.value);
      console.log("Display status:", getStatusText(currentStatus.value));

      chiTietList.value = res.data.content || [];
      tongTien.value = chiTietList.value.reduce(
        (sum: number, item: any) => sum + (item.thanhTien || 0),
        0
      );

      console.log("Data loaded successfully");

      // Force một lần update để đảm bảo UI sync
      await nextTick();
      forceUpdateUI();
    } else {
      console.error("getHoaDonChiTiets returned empty or invalid data:", res);
      toast.error("Không thể tải dữ liệu hóa đơn");
    }
  } catch (error) {
    console.error("Lỗi khi tải dữ liệu:", error);
    toast.error("Có lỗi xảy ra khi tải dữ liệu: " + (error as Error).message);
  }
  await loadPaymentHistory();
  fetchHoaDonChiTiet();
  console.log("Data loaded successfully");
});
</script>
