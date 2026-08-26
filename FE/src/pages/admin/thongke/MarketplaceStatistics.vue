<template>
  <div class="statistics-page">
    <div class="page-head"><div><h2>Thong ke toan san</h2><p>GMV, seller va san pham tren toan marketplace.</p></div><a-button :loading="loading" @click="load">Tai lai</a-button></div>
    <div class="metric-grid">
      <div class="metric"><span>GMV hoan thanh</span><strong>{{ currency(data.gmv) }}</strong></div>
      <div class="metric"><span>Tong sub-order</span><strong>{{ data.totalSubOrders }}</strong></div>
      <div class="metric"><span>Sub-order hoan thanh</span><strong>{{ data.completedSubOrders }}</strong></div>
      <div class="metric"><span>Seller co don</span><strong>{{ data.activeSellers }}</strong></div>
    </div>
    <div class="table-grid">
      <section class="panel"><h3>Top seller</h3><a-table row-key="sellerId" :columns="sellerColumns" :data-source="data.topSellers" :pagination="false" size="small"><template #bodyCell="{ column, record }"><template v-if="column.key === 'revenue'">{{ currency(record.revenue) }}</template><template v-else-if="column.dataIndex">{{ record[column.dataIndex] ?? '—' }}</template></template></a-table></section>
      <section class="panel"><h3>Top san pham</h3><a-table row-key="productVariantId" :columns="productColumns" :data-source="data.topProducts" :pagination="false" size="small"><template #bodyCell="{ column, record }"><template v-if="column.key === 'revenue'">{{ currency(record.revenue) }}</template><template v-else-if="column.dataIndex">{{ record[column.dataIndex] ?? '—' }}</template></template></a-table></section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getMarketplaceDashboard, type MarketplaceDashboard } from '@/services/api/admin/marketplace-statistics.api'
const loading = ref(false)
const data = reactive<MarketplaceDashboard>({ gmv: 0, totalSubOrders: 0, completedSubOrders: 0, activeSellers: 0, topSellers: [], topProducts: [] })
const sellerColumns = [{ title: 'Shop', dataIndex: 'shopName' }, { title: 'Don', dataIndex: 'orderCount', width: 70 }, { title: 'Doanh thu', key: 'revenue' }]
const productColumns = [{ title: 'San pham', dataIndex: 'productName' }, { title: 'Da ban', dataIndex: 'soldCount', width: 80 }, { title: 'Doanh thu', key: 'revenue' }]
const currency = (value: number) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0)
const load = async () => { loading.value = true; try { Object.assign(data, await getMarketplaceDashboard()) } catch (error: any) { message.error(error?.response?.data?.message ?? 'Khong tai duoc thong ke') } finally { loading.value = false } }
onMounted(load)
</script>

<style scoped>
.statistics-page { padding: 24px; }
.page-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; margin-bottom: 18px; }
.page-head h2, .panel h3 { margin: 0; }
.page-head p { margin: 4px 0 0; color: #667085; }
.metric-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin-bottom: 18px; }
.metric, .panel { border: 1px solid #dbe3ef; background: #fff; border-radius: 8px; padding: 18px; }
.metric { display: flex; flex-direction: column; gap: 12px; }
.metric span { color: #667085; }.metric strong { font-size: 22px; }
.table-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; }.panel h3 { font-size: 17px; margin-bottom: 14px; }
@media (max-width: 950px) { .metric-grid { grid-template-columns: repeat(2, 1fr); }.table-grid { grid-template-columns: 1fr; } }
@media (max-width: 560px) { .metric-grid { grid-template-columns: 1fr; } }
</style>
