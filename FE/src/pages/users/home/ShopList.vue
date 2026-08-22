<template>
  <div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h4 class="fw-bold">
        <CheckCircleFilled class="text-success me-2" />
        Shop noi bat
      </h4>
    </div>

    <a-skeleton v-if="loading" active />
    <a-empty v-else-if="!shops.length" description="Chua co shop da duyet" />
    <div v-else class="row g-3">
      <div class="col-6 col-md-4 col-lg-2" v-for="item in shops" :key="item.id">
        <router-link :to="{ name: 'shop-detail', params: { sellerSlug: item.sellerSlug } }" class="text-decoration-none text-dark">
          <div class="card h-100 shadow-sm">
            <div class="card-img-top bg-light d-flex align-items-center justify-content-center" style="height: 140px;">
              <img v-if="item.logoUrl" :src="item.logoUrl" alt="logo" class="img-fluid" style="max-height: 80px; max-width: 80%;">
              <a-avatar v-else :size="64">{{ item.shopName?.[0] }}</a-avatar>
            </div>
            <div class="card-body text-center p-2">
              <p class="fw-semibold mb-1 text-truncate" :title="item.shopName">{{ item.shopName }}</p>
              <small class="text-muted">
                <CheckCircleFilled style="color: #52c41a; margin-right: 0.5rem;" />
                {{ item.followerCount ?? 0 }} theo doi
              </small>
            </div>
          </div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { CheckCircleFilled } from '@ant-design/icons-vue'
import { onMounted, ref } from 'vue'
import { getPublicShops, type SellerResponse } from '@/services/api/seller/seller.api'

const shops = ref<SellerResponse[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getPublicShops()
    shops.value = res.data ?? []
  } finally {
    loading.value = false
  }
})
</script>
