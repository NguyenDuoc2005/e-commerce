<template>
  <div class="purchase-history-container">
    <!-- Header với thông tin khách hàng -->
    <div class="customer-header">
      <div class="customer-info">
        <div class="customer-details">
          <h3 class="customer-name">Thông tin đơn hàng đã mua</h3>
        </div>
      </div>
    </div>

    <!-- Search Section -->
    <div class="search-section">
      <a-input-search
        v-model:value="searchQuery"
        placeholder="Tìm kiếm theo mã hóa đơn, tên sản phẩm..."
        style="width: 100%"
        @search="handleSearch"
        @keyup.enter="handleSearch"
      />
    </div>

    <!-- Tabs trạng thái -->
    <div class="status-tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        :class="['tab-button', { active: trangThaiTab === tab.key }]"
        @click="handleTabChange(tab.key)"
      >
        {{ tab.label }}
        <span v-if="tab.count > 0" class="count-badge">{{ tab.count }}</span>
      </button>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Đang tải dữ liệu...</p>
    </div>

    <!-- Danh sách đơn hàng -->
    <div v-else class="orders-list">
      <div 
        v-for="order in groupedOrders" 
        :key="order.maHoaDon"
        class="order-card"
      >
        <!-- Header đơn hàng -->
        <div class="order-header">
          <div class="order-info">
            <span class="order-code">Mã đơn hàng: {{ order.maHoaDon }}</span>
            <span :class="['order-status', getStatusClass(order.status)]">
              {{ getStatusText(order.status) }}
            </span>
          </div>
          <div class="order-meta">
            <span class="order-date">{{ formatDate(order.createdDate) }}</span>
            <span class="order-type">{{ getInvoiceTypeText(order.loaiHoaDon) }}</span>
          </div>
        </div>

        <!-- Danh sách sản phẩm trong đơn hàng -->
        <div class="products-list">
          <div 
            v-for="(product, index) in order.products" 
            :key="index"
            class="product-item"
          >
            <div class="product-image">
              <img 
                v-if="product.anh" 
                :src="product.anh" 
                :alt="product.tenSanPham"
              />
              <div v-else class="product-image-placeholder">
                <span>📦</span>
              </div>
            </div>
            <div class="product-details">
              <h4 class="product-name">{{ product.tenSanPham }}</h4>
              <p class="product-brand">{{ product.tenThuongHieu }}</p>
              <p class="product-variant">{{ product.kichCo }} - {{ product.mauSac }}</p>
              <p class="product-quantity">x{{ product.soLuong }}</p>
            </div>
            <div class="product-price">
              <span class="price">{{ formatCurrency(product.giaBan) }}</span>
            </div>
            <div class="product-actions">
            </div>
          </div>
        </div>

        <!-- Footer đơn hàng -->
        <div class="order-footer">
          <div class="order-total">
            <span class="total-label">Tổng cộng:</span>
            <span class="total-amount">{{ formatCurrency(order.totalAmount) }}</span>
          </div>
          <div class="order-actions">
            <button 
              v-if="order.status === 'CHO_XAC_NHAN'" 
              class="btn-cancel"
              @click="cancelOrder(order.maHoaDon)"
            >
              Hủy đơn
            </button>
            <button 
              class="btn-view-detail"
              @click="viewOrderDetail(order.maHoaDon, order.id)"
            >
              Xem đơn hàng
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="!loading && groupedOrders.length === 0" class="empty-state">
      <div class="empty-icon">📦</div>
      <h3>Chưa có đơn hàng nào</h3>
      <p>Hãy khám phá và đặt hàng những sản phẩm yêu thích!</p>
      <button class="btn-shopping" @click="goShopping">Mua sắm ngay</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { getDonMua, changeStatus, type DonMuaRequest } from "@/services/api/permitall/donmua/donmua.api";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { localStorageAction } from '@/utils/storage';
import { USER_INFO_STORAGE_KEY } from '@/constants/storageKey';

// Types
interface ProductItem {
  maHoaDon: string;
  tenSanPham: string;
  tenThuongHieu: string;
  mauSac: string;
  kichCo: string;
  soLuong: number;
  giaBan: number;
  status: string;
  id?: string;
  createdDate?: number;
  loaiHoaDon?: string;
  tenKhachHang?: string;
  sdtKhachHang?: string;
  maNhanVien?: string;
  tongTien?: number;
}

interface GroupedOrder {
  maHoaDon: string;
  status: string;
  products: ProductItem[];
  totalAmount: number;
  createdDate?: number;
  loaiHoaDon?: string;
  tenKhachHang?: string;
  sdtKhachHang?: string;
  maNhanVien?: string;
  id?: string;
  anh?: string;
}

const idKH = localStorageAction.get(USER_INFO_STORAGE_KEY);

// Router
const router = useRouter();

// Reactive data
const loading = ref(false);
const invoices = ref<ProductItem[]>([]);
const countByStatus = ref<Record<string, number>>({});
const trangThaiTab = ref<string | null>(null);
const searchQuery = ref("");

// Computed properties
const tabs = computed(() => {
  const statusMap = countByStatus.value || {};
  const total = Object.values(statusMap).reduce((sum, val) => sum + val, 0);

  return [
    { key: null, label: "Tất cả", count: total },
    {
      key: "CHO_XAC_NHAN",
      label: "Chờ xác nhận",
      count: statusMap.CHO_XAC_NHAN || 0,
    },
    {
      key: "DA_XAC_NHAN", 
      label: "Đã xác nhận",
      count: statusMap.DA_XAC_NHAN || 0,
    },
    { key: "CHO_GIAO", label: "Chờ giao", count: statusMap.CHO_GIAO || 0 },
    { key: "DANG_GIAO", label: "Đang giao", count: statusMap.DANG_GIAO || 0 },
    {
      key: "HOAN_THANH",
      label: "Hoàn thành", 
      count: statusMap.HOAN_THANH || 0,
    },
    { key: "DA_HUY", label: "Đã hủy", count: statusMap.DA_HUY || 0 },
  ];
});

// Nhóm sản phẩm theo mã hóa đơn
const groupedOrders = computed(() => {
  const grouped = invoices.value.reduce((acc, product) => {
    if (!acc[product.maHoaDon]) {
      acc[product.maHoaDon] = {
        id:product.id,
        maHoaDon: product.maHoaDon,
        status: product.status,
        products: [],
        totalAmount: product.tongTien,
        createdDate: product.createdDate,
        loaiHoaDon: product.loaiHoaDon,
        tenKhachHang: product.tenKhachHang,
        sdtKhachHang: product.sdtKhachHang,
        maNhanVien: product.maNhanVien,
        id: product.id,
        anh: product.anh
      };
    }
    acc[product.maHoaDon].products.push(product);
    // acc[product.maHoaDon].totalAmount += (product.tongTien || product.giaBan * product.soLuong);
    return acc;
  }, {} as Record<string, GroupedOrder>);

  return Object.values(grouped);
});

// API Functions
const fetchInvoices = async () => {
  try {
    loading.value = true;
    
    const params: DonMuaRequest = {
      q: idKH.userId || '',
      status: trangThaiTab.value || undefined,
      search: searchQuery.value || undefined,
    };

    const response = await getDonMua(params);
    console.log('API Response:', response);
    
    if (response.status === 'OK' && response.data) {
      invoices.value = response.data.page || [];
      countByStatus.value = response.data.countByStatus || {};
      
      console.log('Data loaded:', {
        invoices: invoices.value.length,
        countByStatus: countByStatus.value
      });
    } else {
      message.error('Không thể tải danh sách hóa đơn');
      invoices.value = [];
      countByStatus.value = {};
    }
  } catch (error) {
    console.error('Error fetching invoices:', error);
    message.error('Có lỗi xảy ra khi tải danh sách hóa đơn');
    invoices.value = [];
    countByStatus.value = {};
  } finally {
    loading.value = false;
  }
};

// Event Handlers
const handleTabChange = (key: string | null) => {
  trangThaiTab.value = key;
  fetchInvoices();
};

const handleSearch = () => {
  fetchInvoices();
};

const cancelOrder = async (maHoaDon: string) => {
  message.info(`Hủy đơn hàng ${maHoaDon}`);

  try {
    const response = await changeStatus({
      maHoaDon: maHoaDon,
      status: 'DA_HUY',
      note: 'Khách hàng hủy đơn hàng'
    });

    if (response.success) {
      message.success('Hủy đơn hàng thành công');
      fetchInvoices();
    } else {
      message.error(response.message || 'Hủy đơn hàng thất bại');
    }
  } catch (error) {
    message.error('Có lỗi xảy ra khi hủy đơn hàng');
  }
};

const viewOrderDetail = (maHoaDon?: string, id?: string) => {
  router.push({ name: "don-mua-detail", params: {maHoaDon, id}});
};

const goShopping = () => {
  router.push({ name: 'trang-chu' });
};

// Utility Functions
const getStatusClass = (status: string) => {
  const classMap: Record<string, string> = {
    'CHO_XAC_NHAN': 'pending',
    'DA_XAC_NHAN': 'confirmed', 
    'CHO_GIAO': 'preparing',
    'DANG_GIAO': 'shipping',
    'XAC_NHAN_THANH_TOAN': 'payment',
    'HOAN_THANH': 'completed',
    'DA_HUY': 'cancelled'
  };
  return classMap[status] || 'default';
};

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'CHO_XAC_NHAN': 'Chờ xác nhận',
    'DA_XAC_NHAN': 'Đã xác nhận',
    'CHO_GIAO': 'Chờ giao',
    'DANG_GIAO': 'Đang giao',
    'XAC_NHAN_THANH_TOAN': 'Xác nhận thanh toán',
    'HOAN_THANH': 'Hoàn thành',
    'DA_HUY': 'Đã hủy'
  };
  return textMap[status] || 'Không rõ';
};

const getInvoiceTypeText = (type?: string) => {
  const textMap: Record<string, string> = {
    'OFFLINE': 'OFFLINE',
    'GIAO_HANG': 'GIAO HÀNG', 
    'ONLINE': 'ONLINE'
  };
  return textMap[type || ''] || '';
};

const formatCurrency = (value: number) => {
  return value?.toLocaleString("vi-VN", { style: "currency", currency: "VND" }) || "0 ₫";
};

const formatDate = (timestamp?: number) => {
  if (!timestamp) return "";
  const date = new Date(timestamp);
  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0"); 
  const year = date.getFullYear();
  return `${day}/${month}/${year}`;
};

// Watch for userId changes
watch(() => idKH?.userId, (newUserId) => {
  if (newUserId) {
    fetchInvoices();
  }
});

// Lifecycle
onMounted(() => {
  if (idKH?.userId) {
    fetchInvoices();
  } else {
    console.warn('Không tìm thấy userId để load danh sách hóa đơn');
  }
});

// Expose functions
defineExpose({
  fetchInvoices,
  refresh: fetchInvoices
});
</script>

<style scoped lang="scss">
.purchase-history-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.customer-header {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .customer-info {
    display: flex;
    align-items: center;
    gap: 15px;

    .avatar {
      width: 50px;
      height: 50px;
      border-radius: 50%;
      overflow: hidden;
      
      .avatar-placeholder {
        width: 100%;
        height: 100%;
        background: linear-gradient(135deg, #58bddb, #3fa3b9);
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-weight: 600;
        font-size: 16px;
      }

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .customer-details {
      .customer-name {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: #333;
      }

      .order-count {
        margin: 4px 0 0 0;
        color: #666;
        font-size: 14px;
      }
    }
  }
}

.search-section {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.status-tabs {
  display: flex;
  background: white;
  border-radius: 12px;
  padding: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
  gap: 4px;

  .tab-button {
    flex: 1;
    min-width: 120px;
    padding: 12px 16px;
    border: none;
    background: transparent;
    border-radius: 8px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    color: #666;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    transition: all 0.3s ease;
    white-space: nowrap;

    &:hover {
      background-color: #e6faff;
      color: #58bddb;
    }

    &.active {
      background-color: #58bddb;
      color: white;

      .count-badge {
        background-color: rgba(255, 255, 255, 0.2);
        color: white;
      }
    }

    .count-badge {
      background-color: #ff4d4f;
      color: white;
      border-radius: 12px;
      padding: 2px 8px;
      font-size: 12px;
      min-width: 20px;
      height: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid #f3f3f3;
    border-top: 3px solid #58bddb;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 16px;
  }

  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }

  p {
    color: #666;
    margin: 0;
  }
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  }
}

.order-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;

  .order-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .order-code {
      font-weight: 600;
      color: #333;
      font-size: 16px;
    }

    .order-status {
      padding: 6px 12px;
      border-radius: 6px;
      font-size: 12px;
      font-weight: 500;

      &.pending {
        background-color: #fff7e6;
        color: #fa8c16;
      }

      &.confirmed {
        background-color: #fff7e6;
        color: #faad14;
      }

      &.preparing {
        background-color: #e6f7ff;
        color: #1890ff;
      }

      &.shipping {
        background-color: #e6fffb;
        color: #13c2c2;
      }

      &.payment {
        background-color: #f9f0ff;
        color: #722ed1;
      }

      &.completed {
        background-color: #f6ffed;
        color: #52c41a;
      }

      &.cancelled {
        background-color: #fff2f0;
        color: #ff4d4f;
      }
    }
  }

  .order-meta {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    color: #999;

    .order-type {
      color: #58bddb;
      font-weight: 500;
    }
  }
}

.products-list {
  padding: 0;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  gap: 15px;

  &:last-child {
    border-bottom: none;
  }

  .product-image {
    width: 80px;
    height: 80px;
    border-radius: 8px;
    overflow: hidden;
    flex-shrink: 0;
    background-color: #f5f5f5;

    .product-image-placeholder {
      width: 100%;
      height: 100%;
      background-color: #f5f5f5;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      color: #ccc;
      border: 1px dashed #ddd;
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .product-details {
    flex: 1;
    min-width: 0;

    .product-name {
      margin: 0 0 4px 0;
      font-size: 16px;
      font-weight: 600;
      color: #333;
      line-height: 1.4;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .product-brand {
      margin: 0 0 4px 0;
      color: #666;
      font-size: 14px;
    }

    .product-variant {
      margin: 0 0 4px 0;
      color: #999;
      font-size: 13px;
    }

    .product-quantity {
      margin: 0;
      color: #999;
      font-size: 13px;
    }
  }

  .product-price {
    text-align: right;
    margin-right: 15px;

    .price {
      font-size: 16px;
      font-weight: 600;
      color: #ff4d4f;
    }
  }

  .product-actions {
    .action-btn {
      width: 36px;
      height: 36px;
      border: none;
      background-color: #f5f5f5;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.2s ease;

      &:hover {
        background-color: #ffecec;
        transform: scale(1.1);
      }

      .icon-heart {
        font-size: 18px;
        filter: grayscale(100%);
      }

      &:hover .icon-heart {
        filter: grayscale(0%);
      }
    }
  }
}

.order-footer {
  padding: 16px 20px;
  background-color: #fafafa;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .order-total {
    .total-label {
      color: #666;
      font-size: 14px;
      margin-right: 8px;
    }

    .total-amount {
      font-size: 18px;
      font-weight: 700;
      color: #ff4d4f;
    }
  }

  .order-actions {
    display: flex;
    gap: 12px;

    .btn-cancel {
      padding: 8px 16px;
      border: 1px solid #ff4d4f;
      background-color: white;
      color: #ff4d4f;
      border-radius: 6px;
      cursor: pointer;
      font-size: 14px;
      font-weight: 500;
      transition: all 0.2s ease;

      &:hover {
        background-color: #ff4d4f;
        color: white;
      }
    }

    .btn-view-detail {
      padding: 8px 16px;
      border: 1px solid #58bddb;
      background-color: #58bddb;
      color: white;
      border-radius: 6px;
      cursor: pointer;
      font-size: 14px;
      font-weight: 500;
      transition: all 0.2s ease;

      &:hover {
        background-color: #3fa3b9;
        border-color: #3fa3b9;
      }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }

  h3 {
    margin: 0 0 8px 0;
    color: #333;
    font-size: 18px;
  }

  p {
    margin: 0 0 24px 0;
    color: #666;
    font-size: 14px;
  }

  .btn-shopping {
    padding: 10px 24px;
    background-color: #58bddb;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    transition: background-color 0.2s ease;

    &:hover {
      background-color: #3fa3b9;
    }
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

// Responsive
@media (max-width: 768px) {
  .purchase-history-container {
    padding: 16px;
  }

  .status-tabs {
    .tab-button {
      min-width: 90px;
      padding: 10px 12px;
      font-size: 13px;
    }
  }

  .product-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;

    .product-image {
      width: 60px;
      height: 60px;
    }

    .product-price,
    .product-actions {
      margin-right: 0;
    }
  }

  .order-footer {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;

    .order-actions {
      justify-content: center;
    }
  }
}
</style>