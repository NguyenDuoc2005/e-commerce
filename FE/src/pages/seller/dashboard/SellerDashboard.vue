<template>
  <div class="dashboard-page">
    <div class="page-head"><div><h2>Tong quan shop</h2><p>Doanh thu, don hang va ton kho cua shop hien tai.</p></div><a-button :loading="loading" @click="load">Tai lai</a-button></div>
    <div class="metric-grid">
      <div v-for="metric in metrics" :key="metric.label" class="metric"><span>{{ metric.label }}</span><strong>{{ metric.money ? currency(metric.value) : metric.value }}</strong></div>
    </div>
    <div class="dashboard-grid">
      <section class="panel">
        <h3>Doanh thu 7 ngay gan nhat</h3>
        <div v-if="dashboard.revenueSeries.length" class="bars">
          <div v-for="item in dashboard.revenueSeries" :key="item.date" class="bar-row">
            <span>{{ item.date }}</span><div class="bar-track"><div class="bar-value" :style="{ width: `${barWidth(item.revenue)}%` }" /></div><strong>{{ currency(item.revenue) }}</strong>
          </div>
        </div>
        <a-empty v-else description="Chua co doanh thu hoan thanh" />
      </section>
      <section class="panel">
        <h3>San pham sap het hang</h3>
        <a-table row-key="id" :columns="stockColumns" :data-source="lowStock" :pagination="false" size="small" />
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getSellerDashboard, type SellerDashboardData } from '@/services/api/seller/order.api'
import { getSellerLowStock, type LowStockProduct } from '@/services/api/seller/dashboard.api'

const loading = ref(false)
const dashboard = reactive<SellerDashboardData>({ todayRevenue: 0, weekRevenue: 0, monthRevenue: 0, pendingOrders: 0, shippingOrders: 0, completedOrders: 0, revenueSeries: [] })
const lowStock = ref<LowStockProduct[]>([])
const stockColumns = [{ title: 'San pham', dataIndex: 'productName' }, { title: 'Ma', dataIndex: 'code' }, { title: 'Ton', dataIndex: 'quantity', width: 70 }]
const metrics = computed(() => [
  { label: 'Doanh thu hom nay', value: dashboard.todayRevenue, money: true },
  { label: 'Doanh thu tuan', value: dashboard.weekRevenue, money: true },
  { label: 'Doanh thu thang', value: dashboard.monthRevenue, money: true },
  { label: 'Don cho xu ly', value: dashboard.pendingOrders, money: false },
  { label: 'Don dang giao', value: dashboard.shippingOrders, money: false },
  { label: 'Don hoan thanh', value: dashboard.completedOrders, money: false }
])
const maxRevenue = computed(() => Math.max(1, ...dashboard.revenueSeries.map((item) => Number(item.revenue))))
const barWidth = (value: number) => Math.max(2, (Number(value) / maxRevenue.value) * 100)
const currency = (value: number) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)
const load = async () => {
  loading.value = true
  try {
    Object.assign(dashboard, await getSellerDashboard())
    lowStock.value = (await getSellerLowStock()).data ?? []
  } catch (error: any) {
    message.error(error?.response?.data?.message ?? 'Khong tai duoc dashboard seller')
  } finally { loading.value = false }
}
onMounted(load)
</script>

<style scoped>
.dashboard-page { padding: 24px; }
.page-head { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 18px; }
.page-head h2, .panel h3 { margin: 0; }
.page-head p { color: #667085; margin: 4px 0 0; }
.metric-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-bottom: 18px; }
.metric, .panel { border: 1px solid #dbe3ef; background: #fff; border-radius: 8px; padding: 18px; }
.metric { display: flex; min-height: 96px; flex-direction: column; justify-content: space-between; }
.metric span { color: #667085; }
.metric strong { font-size: 22px; color: #172033; }
.dashboard-grid { display: grid; grid-template-columns: 1.1fr 1fr; gap: 18px; }
.panel h3 { font-size: 17px; margin-bottom: 16px; }
.bars { display: grid; gap: 12px; }
.bar-row { display: grid; grid-template-columns: 90px minmax(100px, 1fr) 130px; align-items: center; gap: 10px; font-size: 13px; }
.bar-track { height: 10px; background: #edf2f7; border-radius: 4px; overflow: hidden; }
.bar-value { height: 100%; background: #54bddb; border-radius: 4px; }
@media (max-width: 900px) { .metric-grid { grid-template-columns: repeat(2, 1fr); } .dashboard-grid { grid-template-columns: 1fr; } }
@media (max-width: 560px) { .metric-grid { grid-template-columns: 1fr; } .bar-row { grid-template-columns: 78px 1fr; } .bar-row strong { grid-column: 2; } }
</style>
