<template>
  <div class="order-search-container">
    <h3 class="search-title">Tra cứu đơn hàng</h3>
    
    <div class="search-section">
      <a-input-search
        v-model:value="searchQuery"
        placeholder="Nhập mã hóa đơn..."
        style="width: 100%"
        @search="handleSearch"
        @keyup.enter="handleSearch"
      />
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Đang tải dữ liệu...</p>
    </div>

    <div v-else-if="groupedOrders.length > 0" class="orders-table">
      <table>
        <thead>
          <tr>
            <th>Mã đơn hàng</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th>Tổng cộng</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in groupedOrders" :key="order.maHoaDon">
            <td>{{ order.maHoaDon }}</td>
            <td>{{ getStatusText(order.status) }}</td>
            <td>{{ formatDate(order.createdDate) }}</td>
            <td>{{ formatCurrency(order.totalAmount) }}</td>
            <td>
              <button @click="viewOrderDetail(order.maHoaDon, order.id)">Xem chi tiết</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else-if="!loading && !groupedOrders.length" class="empty-state">
      <h3>Không tìm thấy đơn hàng nào</h3>
      <p>Vui lòng kiểm tra lại mã hóa đơn.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { getDonMuaByCode } from "@/services/api/permitall/donmua/donmua.api"; // Giả sử bạn có API này
import { message } from "ant-design-vue";
import { useRouter } from "vue-router";

// Reactive data
const loading = ref(false);
const searchQuery = ref("");
const orders = ref([]);

// Router
const router = useRouter();

// Computed property to group orders
const groupedOrders = computed(() => {
  const grouped = orders.value.reduce((acc, product) => {
    if (!acc[product.maHoaDon]) {
      acc[product.maHoaDon] = {
        id: product.id,
        maHoaDon: product.maHoaDon,
        status: product.status,
        createdDate: product.createdDate,
        totalAmount: product.tongTien,
        products: []
      };
    }
    acc[product.maHoaDon].products.push(product);
    return acc;
  }, {} as Record<string, any>);

  return Object.values(grouped);
});

// API Functions
const fetchOrders = async (code: string) => {
  try {
    loading.value = true;
    const response = await getDonMuaByCode(code);

    if (response.status === 'OK' && response.data) {
      orders.value = response.data.page || [];
    } else {
      message.error('Không tìm thấy đơn hàng');
      orders.value = [];
    }
  } catch (error) {
    console.error('Error fetching orders:', error);
    message.error('Có lỗi xảy ra khi tải danh sách đơn hàng');
    orders.value = [];
  } finally {
    loading.value = false;
  }
};

// Event Handlers
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    fetchOrders(searchQuery.value.trim());
  } else {
    message.warning('Vui lòng nhập mã hóa đơn');
  }
};

const viewOrderDetail = (maHoaDon?: string, id?: string) => {
  router.push({ name: "don-mua-detail", params: { maHoaDon, id } });
};

// Utility Functions
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'CHO_XAC_NHAN': 'Chờ xác nhận',
    'DA_XAC_NHAN': 'Đã xác nhận',
    'CHO_GIAO': 'Chờ giao',
    'DANG_GIAO': 'Đang giao',
    'HOAN_THANH': 'Hoàn thành',
    'DA_HUY': 'Đã hủy'
  };
  return textMap[status] || 'Không rõ';
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
</script>

<style scoped lang="scss">
.order-search-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f5f5f5;
  border-radius: 12px;

  .search-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 600;
    color: #333;
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
  }

  .orders-table {
    margin-top: 20px;
    table {
      width: 100%;
      border-collapse: collapse;

      th, td {
        padding: 12px;
        text-align: left;
        border-bottom: 1px solid #ddd;
      }

      th {
        background-color: #f0f0f0;
      }

      button {
        padding: 6px 12px;
        border: none;
        background-color: #58bddb;
        color: white;
        border-radius: 4px;
        cursor: pointer;

        &:hover {
          background-color: #3fa3b9;
        }
      }
    }
  }

  .empty-state {
    text-align: center;
    margin-top: 20px;
    h3 {
      color: #333;
    }
    p {
      color: #666;
    }
  }
}
</style>