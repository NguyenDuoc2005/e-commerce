<template>
  <div class="flash-page">
    <section class="hero">
      <div><span class="eyebrow">MARKETPLACE EVENT</span><h1>⚡ Flash Sale toàn sàn</h1><p>Sản phẩm và mức giá đã được Platform Admin kiểm duyệt trước khi hiển thị.</p></div>
      <div class="hero-clock"><small>Cập nhật trong</small><strong>{{ clock }}</strong></div>
    </section>

    <a-skeleton v-if="loading" active />
    <a-empty v-else-if="!campaigns.length" description="Chưa có sự kiện Flash sale sắp diễn ra" />
    <section v-for="campaign in campaigns" v-else :key="campaign.id" class="campaign-block">
      <header class="campaign-head">
        <div><div class="campaign-title"><h2>{{ campaign.name }}</h2><a-tag :color="campaign.status === 'DANG_KICH_HOAT' ? 'red' : 'blue'">{{ campaign.status === 'DANG_KICH_HOAT' ? 'Đang diễn ra' : 'Sắp diễn ra' }}</a-tag></div><p>{{ campaign.description }}</p></div>
        <div class="countdown"><small>{{ campaign.status === 'DANG_KICH_HOAT' ? 'Kết thúc sau' : 'Bắt đầu sau' }}</small><strong>{{ countdown(campaign.status === 'DANG_KICH_HOAT' ? campaign.endDate : campaign.startDate) }}</strong></div>
      </header>
      <a-empty v-if="!campaign.products?.length" description="Sản phẩm đang chờ duyệt" />
      <div v-else class="product-grid">
        <article v-for="product in campaign.products" :key="product.id" class="product-card" @click="openProduct(product.productId)">
          <div class="image-wrap"><img :src="product.imageUrl || placeholder" /><span class="discount">-{{ product.discountPercent }}%</span></div>
          <div class="product-body"><h3>{{ product.productName }}</h3><p>{{ product.variantLabel }}</p><div class="price-row"><strong>{{ money(product.flashPrice) }}</strong><s>{{ money(product.priceBeforeDiscount) }}</s></div><div class="stock">Còn {{ product.quantity || 0 }} sản phẩm</div></div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getPublicFlashSales, type FlashSaleCampaign } from '@/services/api/flash-sale/flash-sale.api'

const router = useRouter()
const campaigns = ref<FlashSaleCampaign[]>([])
const loading = ref(false)
const now = ref(Date.now())
let timer: ReturnType<typeof setInterval> | undefined
const placeholder = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="300"%3E%3Crect width="100%25" height="100%25" fill="%23f1f5f9"/%3E%3C/svg%3E'
const clock = computed(() => new Intl.DateTimeFormat('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' }).format(new Date(now.value)))
const load = async () => { loading.value = true; try { campaigns.value = (await getPublicFlashSales()).filter(row => row.status !== 'HET_HAN_KICH_HOAT') } catch (error: any) { message.error(error?.response?.data?.message || 'Không tải được Flash sale') } finally { loading.value = false } }
const countdown = (target: number) => { const remaining = Math.max(0, target - now.value); const days = Math.floor(remaining / 86400000); const hours = Math.floor(remaining / 3600000) % 24; const minutes = Math.floor(remaining / 60000) % 60; const seconds = Math.floor(remaining / 1000) % 60; return `${days ? `${days} ngày ` : ''}${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}` }
const money = (value?: number) => `${Number(value || 0).toLocaleString('vi-VN')} ₫`
const openProduct = (productId?: string) => { if (productId) void router.push({ name: 'san-pham-chi-tiet', params: { idsp: productId } }) }
onMounted(() => { void load(); timer = setInterval(() => { now.value = Date.now() }, 1000) })
onBeforeUnmount(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.flash-page { max-width: 1180px; margin: 0 auto; padding: 24px 16px 52px; }
.hero { display: flex; justify-content: space-between; align-items: center; gap: 20px; padding: 30px; border-radius: 18px; color: #fff; background: linear-gradient(120deg, #991b1b, #ef4444 55%, #f97316); box-shadow: 0 16px 34px rgb(153 27 27 / 22%); }.hero h1 { margin: 3px 0 6px; font-size: 34px; font-weight: 850; }.hero p { margin: 0; }.eyebrow { font-size: 11px; letter-spacing: .18em; }.hero-clock { min-width: 150px; padding: 14px; border-radius: 12px; background: rgb(255 255 255 / 16%); text-align: center; }.hero-clock small, .countdown small { display: block; }.hero-clock strong { font-size: 24px; }
.campaign-block { margin-top: 28px; }.campaign-head { display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-bottom: 15px; }.campaign-title { display: flex; align-items: center; gap: 10px; }.campaign-title h2 { margin: 0; font-weight: 800; }.campaign-head p { margin: 3px 0 0; color: #64748b; }.countdown { min-width: 180px; padding: 10px 15px; border-radius: 10px; background: #fff1f2; color: #9f1239; text-align: center; }.countdown strong { font-size: 18px; }
.product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(205px, 1fr)); gap: 16px; }.product-card { overflow: hidden; border: 1px solid #fee2e2; border-radius: 12px; background: #fff; cursor: pointer; transition: transform .2s, box-shadow .2s; }.product-card:hover { transform: translateY(-3px); box-shadow: 0 10px 24px rgb(15 23 42 / 10%); }.image-wrap { position: relative; aspect-ratio: 1/1; background: #f8fafc; }.image-wrap img { width: 100%; height: 100%; object-fit: cover; }.discount { position: absolute; right: 8px; top: 8px; padding: 5px 7px; border-radius: 6px; background: #dc2626; color: #fff; font-weight: 700; }.product-body { padding: 12px; }.product-body h3 { height: 42px; margin: 0; overflow: hidden; font-size: 15px; }.product-body p { margin: 3px 0 8px; color: #64748b; font-size: 12px; }.price-row { display: flex; gap: 8px; align-items: baseline; }.price-row strong { color: #dc2626; font-size: 18px; }.price-row s { color: #94a3b8; font-size: 12px; }.stock { margin-top: 8px; color: #64748b; font-size: 12px; }
@media (max-width: 700px) { .hero, .campaign-head { align-items: flex-start; flex-direction: column; }.hero-clock, .countdown { width: 100%; }.hero h1 { font-size: 27px; } }
</style>
