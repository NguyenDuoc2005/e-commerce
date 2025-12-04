<template>
  <div class="page-container">
    <div class="breadcrumb-section">
      <BreadcrumbDefault
        :pageTitle="'Thống kê'"
        :routes="[{ path: '/admin/thong-ke', name: 'Thống kê' }]"
      />
    </div>

    <!-- Cards thống kê -->
    <div class="stats-cards">
      <div class="stats-card" style="background-color: #e6e6fa;">
        <div class="stats-content">
          <div class="stats-title">Doanh số tháng</div>
          <div class="stats-value">
            {{ formatCurrency(doanhThuData.doanhSoThangNay) }}
          </div>
          <div class="stats-subtitle">
            {{ doanhThuData.soHoaDonThangNay }} đơn hàng
          </div>
        </div>
        <div class="stats-icon">
          <RiseOutlined />
        </div>
      </div>

      <div class="stats-card" style="background-color: #f0fff0;">
        <div class="stats-content">
          <div class="stats-title">Doanh số hôm nay</div>
          <div class="stats-value">
            {{ formatCurrency(doanhThuData.doanhSoHomNay) }}
          </div>
          <div class="stats-subtitle">
            {{ doanhThuData.soHoaDonHomNay }} đơn hàng
          </div>
        </div>
        <div class="stats-icon">
          <DollarOutlined />
        </div>
      </div>

      <div class="stats-card" style="background-color: #d6f2fa;">
        <div class="stats-content">
          <div class="stats-title">Số lượng sản phẩm bán trong tháng</div>
          <div class="stats-value">{{ doanhThuData.hangBanDuocThangNay }}</div>
          <div class="stats-subtitle">sản phẩm</div>
        </div>
        <div class="stats-icon">
          <ShoppingOutlined />
        </div>
      </div>
    </div>

    <!-- Biểu đồ thống kê -->
    <div class="chart-section">
      <div class="chart-header">
        <h3 class="chart-title"><BarChartOutlined /> Biểu đồ thống kê</h3>
        <div class="date-filter">
          <a-date-picker
            v-model:value="dateRange[0]"
            placeholder="Ngày bắt đầu"
            format="DD/MM/YYYY"
            @change="handleDateChange"
          />
          <a-date-picker
            v-model:value="dateRange[1]"
            placeholder="Ngày kết thúc"
            format="DD/MM/YYYY"
            @change="handleDateChange"
          />
        </div>
      </div>

      <div class="chart-container">
        <canvas ref="chartCanvas"></canvas>
      </div>
    </div>

    <!-- Top 3 sản phẩm và trạng thái đơn hàng -->
    <div class="bottom-section">
      <!-- Top 3 sản phẩm bán chạy -->
      <div class="top-products-section">
        <h3 class="section-title">
          <ShoppingOutlined /> Top 3 sản phẩm bán chạy
        </h3>
        <div class="top-products-table">
          <div class="table-header">
            <span>STT</span>
            <span>Ảnh</span>
            <span>Tên sản phẩm</span>
            <span>Số lượng</span>
            <span>Doanh số</span>
          </div>
          <div class="table-body">
            <div
              v-for="(product, index) in top3Products"
              :key="product.id"
              class="table-row"
            >
              <span class="stt">{{ index + 1 }}</span>
              <img
                :src="product.anhSanPham"
                alt="Product image"
                class="product-image"
              />
              <span class="product-name">{{ product.tenSanPham }}</span>
              <span class="quantity">{{ product.soLuongBan }}</span>
              <span class="revenue">{{
                formatCurrency(product.doanhThu)
              }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Biểu đồ tròn trạng thái hóa đơn -->
      <div class="order-status-section">
        <h3 class="section-title"><PieChartOutlined /> Trạng thái đơn hàng</h3>
        <div class="pie-chart-container">
          <canvas ref="pieChartCanvas"></canvas>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from "vue";
import {
  Chart,
  CategoryScale,
  LinearScale,
  BarElement,
  BarController,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  PieController,
  type ChartConfiguration,
} from "chart.js";
import {
  getDoanhThu,
  getDonHangHoanThanh,
  getTop3SP,
  getTTHD,
  type DoanhThuResponse,
  type DonHangHoanThanhResponse,
} from "@/services/api/admin/thongke.api";
import BreadcrumbDefault from "@/components/ui/Breadcrumbs/BreadcrumbDefault.vue";
import {
  RiseOutlined,
  DollarOutlined,
  ShoppingOutlined,
  BarChartOutlined,
  PieChartOutlined,
} from "@ant-design/icons-vue";
import dayjs, { type Dayjs } from "dayjs";

// Register Chart.js components
Chart.register(
  CategoryScale,
  LinearScale,
  BarElement,
  BarController,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  PieController
);

// Reactive data
const doanhThuData = reactive<DoanhThuResponse>({
  doanhSoThangNay: 0,
  soHoaDonThangNay: 0,
  doanhSoHomNay: 0,
  soHoaDonHomNay: 0,
  hangBanDuocThangNay: 0,
});

const chartData = ref<DonHangHoanThanhResponse[]>([]);
const chartCanvas = ref<HTMLCanvasElement>();
const chartInstance = ref<Chart | null>(null);
const top3Products = ref<any[]>([]);
const pieChartCanvas = ref<HTMLCanvasElement>();
const pieChartInstance = ref<Chart | null>(null);
const orderStatusData = ref<any[]>([]);
const isLoading = ref(false);

// Date range picker (default to current month)
const dateRange = ref<[Dayjs | null, Dayjs | null]>([
  dayjs().startOf("month"),
  dayjs().endOf("month"),
]);

// Format currency
const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(amount);
};

// Format date for API
const formatDateForAPI = (date: Dayjs): string => {
  return date.format("DD/MM/YYYY");
};

// Helper function to format percentage
const formatPercent = (value: number): string => {
  const percent = Math.round(Number(value) * 100) / 100;
  return percent.toFixed(2);
};

// Fetch doanh thu data
const fetchDoanhThu = async () => {
  try {
    const response = await getDoanhThu();
    if (response && response.data) {
      Object.assign(doanhThuData, response.data);
    } else if (response) {
      Object.assign(doanhThuData, response);
    }
  } catch (error) {
    console.error("Error fetching doanh thu:", error);
  }
};

// Fetch chart data
const fetchChartData = async () => {
  if (!dateRange.value[0] || !dateRange.value[1]) {
    return;
  }

  isLoading.value = true;
  try {
    const [chartResponse, top3Response, statusResponse] = await Promise.all([
      getDonHangHoanThanh({
        ngayBatDau: formatDateForAPI(dateRange.value[0]),
        ngayKetThuc: formatDateForAPI(dateRange.value[1]),
      }),
      getTop3SP({
        ngayBatDau: formatDateForAPI(dateRange.value[0]),
        ngayKetThuc: formatDateForAPI(dateRange.value[1]),
      }),
      getTTHD({
        ngayBatDau: formatDateForAPI(dateRange.value[0]),
        ngayKetThuc: formatDateForAPI(dateRange.value[1]),
      }),
    ]);

    // Xử lý chartResponse
    if (Array.isArray(chartResponse)) {
      chartData.value = chartResponse;
    } else if (chartResponse?.data) {
      chartData.value = Array.isArray(chartResponse.data)
        ? chartResponse.data
        : [];
    } else {
      chartData.value = [];
    }

    // Xử lý top3Response
    if (Array.isArray(top3Response)) {
      top3Products.value = top3Response;
    } else if (top3Response?.data) {
      top3Products.value = Array.isArray(top3Response.data)
        ? top3Response.data
        : [];
    } else {
      top3Products.value = [];
    }

    // Xử lý statusResponse
    if (Array.isArray(statusResponse)) {
      orderStatusData.value = statusResponse;
    } else if (statusResponse?.data) {
      orderStatusData.value = Array.isArray(statusResponse.data)
        ? statusResponse.data
        : [];
    } else {
      orderStatusData.value = [];
    }

    await nextTick();
    updateChart();
    updatePieChart();
  } catch (error) {
    console.error("Error fetching data:", error);
    chartData.value = [];
    top3Products.value = [];
    orderStatusData.value = [];
    await nextTick();
    updateChart();
    updatePieChart();
  } finally {
    isLoading.value = false;
  }
};

// Create/Update bar chart
const updateChart = () => {
  if (!chartCanvas.value) {
    return;
  }

  if (chartInstance.value) {
    chartInstance.value.destroy();
    chartInstance.value = null;
  }

  const ctx = chartCanvas.value.getContext("2d");
  if (!ctx) {
    return;
  }

  if (!chartData.value || chartData.value.length === 0) {
    return;
  }

  const labels = chartData.value.map((item) => item.date);
  const data = chartData.value.map((item) => item.soLuongDonHang);

  const config: ChartConfiguration<"bar"> = {
    type: "bar",
    data: {
      labels,
      datasets: [
        {
          label: "Số lượng đơn hàng",
          data,
          backgroundColor: "rgba(54, 162, 235, 0.6)",
          borderColor: "rgba(54, 162, 235, 1)",
          borderWidth: 1,
          borderRadius: 4,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        title: {
          display: true,
          text: "Đơn hàng hoàn thành theo ngày",
          font: {
          size: 15,
        },
        },
        legend: {
          display: true,
          position: "top",
          font: {
          size: 15,
        },
        },
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            stepSize: 1,
          },
        },
        x: {
          ticks: {
            maxRotation: 45,
            minRotation: 45,
          },
        },
      },
    },
  };

  try {
    chartInstance.value = new Chart(ctx, config);
  } catch (error) {
    console.error("Error creating bar chart:", error);
  }
};

// Create/Update pie chart
const updatePieChart = () => {
  if (!pieChartCanvas.value) {
    return;
  }

  if (pieChartInstance.value) {
    pieChartInstance.value.destroy();
    pieChartInstance.value = null;
  }

  const ctx = pieChartCanvas.value.getContext("2d");
  if (!ctx) {
    return;
  }

  if (!orderStatusData.value || orderStatusData.value.length === 0) {
    return;
  }

  const labels = orderStatusData.value.map((item) => {
    const formattedPercent = formatPercent(item.tiLePhanTram);
    switch (item.trangThai) {
      case "CHO_XAC_NHAN":
        return `Chờ xác nhận (${formattedPercent}%)`;
      case "DA_XAC_NHAN":
        return `Đã xác nhận (${formattedPercent}%)`;
      case "CHO_GIAO":
        return `Chờ giao (${formattedPercent}%)`;
      case "DANG_GIAO":
        return `Đang giao (${formattedPercent}%)`;
      case "HOAN_THANH":
        return `Hoàn thành (${formattedPercent}%)`;
      case "DA_HUY":
        return `Đã hủy (${formattedPercent}%)`;
      default:
        return `${item.trangThai} (${formattedPercent}%)`;
    }
  });

  const data = orderStatusData.value.map((item) => item.tiLePhanTram);
  const backgroundColors = [
    "rgba(255, 99, 132, 0.6)",
    "rgba(54, 162, 235, 0.6)",
    "rgba(255, 206, 86, 0.6)",
    "rgba(75, 192, 192, 0.6)",
    "rgba(153, 102, 255, 0.6)",
    "rgba(255, 159, 64, 0.6)",
  ];

  const config: ChartConfiguration<"pie"> = {
    type: "pie",
    data: {
      labels,
      datasets: [
        {
          data,
          backgroundColor: backgroundColors.slice(0, data.length),
          borderWidth: 1,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        title: {
          display: true,
          text: "Tỷ lệ trạng thái đơn hàng",
        },
        legend: {
          display: true,
          position: "top",
        },
        tooltip: {
          callbacks: {
            label: (context) => {
              const formattedPercent = formatPercent(context.raw as number);
              const statusName = context.label.split(' (')[0];
              return `${statusName}: ${formattedPercent}%`;
            },
          },
        },
      },
    },
  };

  try {
    pieChartInstance.value = new Chart(ctx, config);
  } catch (error) {
    console.error("Error creating pie chart:", error);
  }
};

// Handle date change
const handleDateChange = async () => {
  if (dateRange.value[0] && dateRange.value[1]) {
    await fetchChartData();
  }
};

// Initialize
onMounted(async () => {
  await fetchDoanhThu();
  await fetchChartData();
});
</script>

<style scoped>
:root {
  --primary-color: #58bddb;
  --text-color: #58bddb;
  --card-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  --border-radius: 12px;
  --transition: all 0.3s ease;
  --ant-font-family: 'Roboto', sans-serif;
}

*,
*::before,
*::after {
  font-family: 'Roboto', sans-serif;
  box-sizing: border-box;
}

.page-container {
  padding: 24px;
  max-width: 1440px;
  margin: 0 auto;
  background-color: #f9fafb;
}

.breadcrumb-section {
  background: white;
  padding: 16px 24px;
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow);
  margin-bottom: 24px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stats-card {
  background: white;
  padding: 24px;
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: var(--transition);
}

.stats-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stats-content {
  flex: 1;
}

.stats-title {
  font-size: 16px;
  font-weight: 500;
  color: #58bddb;
  margin-bottom: 8px;
}

.stats-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.stats-subtitle {
  font-size: 14px;
  color: #58bddb;
}

.stats-icon {
  font-size: 48px;
  color: var(--primary-color);
  opacity: 0.8;
}

.chart-section {
  background: white;
  padding: 24px;
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow);
  margin-bottom: 32px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.chart-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-color);
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-filter {
  display: flex;
  gap: 16px;
}

.date-filter .ant-picker {
  border-radius: 8px;
  border: 1px solid #d1d5db;
  transition: var(--transition);
}

.date-filter .ant-picker:hover,
.date-filter .ant-picker-focused {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(88, 189, 219, 0.2);
}

.chart-container {
  height: 400px;
  width: 100%;
}

.bottom-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}

.top-products-section {
  background: white;
  padding: 24px;
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow);
}

.order-status-section {
  background: white;
  padding: 24px;
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-products-table {
  width: 100%;
  border-radius: var(--border-radius);
  overflow: hidden;
}

.table-header {
  display: grid;
  grid-template-columns: 60px 80px 1fr 100px 120px;
  padding: 16px;
  background-color: #f3f4f6;
  font-weight: 600;
  color: var(--text-color);
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.table-header span {
  padding: 0 8px;
  display: flex;
  align-items: center;
}

.table-body {
  display: grid;
  gap: 1px;
  background-color: #e5e7eb;
}

.table-row {
  display: grid;
  grid-template-columns: 60px 80px 1fr 100px 120px;
  padding: 16px;
  background-color: white;
  align-items: center;
  text-align: left;
  transition: var(--transition);
}

.table-row:hover {
  background-color: #f9fafb;
}

.table-row span {
  padding: 0 8px;
}

.stt {
  font-weight: 500;
  color: #4b5563;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.product-name {
  font-weight: 500;
  color: var(--text-color);
}

.quantity {
  font-weight: 500;
  color: #4b5563;
}

.revenue {
  font-weight: 600;
  color: #ef4444;
}

.pie-chart-container {
  height: 360px;
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
}

@media (max-width: 1024px) {
  .bottom-section {
    grid-template-columns: 1fr;
  }

  .pie-chart-container {
    max-width: 100%;
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }

  .chart-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .date-filter {
    flex-direction: column;
    width: 100%;
  }

  .chart-container {
    height: 300px;
  }

  .table-header {
    grid-template-columns: 50px 60px 1fr 80px 100px;
  }

  .table-row {
    grid-template-columns: 50px 60px 1fr 80px 100px;
  }

  .product-image {
    width: 48px;
    height: 48px;
  }

  .pie-chart-container {
    height: 300px;
  }
}

@media (max-width: 480px) {
  .page-container {
    padding: 16px;
  }

  .stats-card {
    padding: 16px;
  }

  .stats-value {
    font-size: 24px;
  }

  .chart-section,
  .top-products-section,
  .order-status-section {
    padding: 16px;
  }

  .table-header,
  .table-row {
    grid-template-columns: 40px 50px 1fr 70px 90px;
    font-size: 14px;
  }

  .product-image {
    width: 40px;
    height: 40px;
  }
}
</style>