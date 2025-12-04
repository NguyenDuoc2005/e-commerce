<template>
  <div class="container mt-5">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h4 class="fw-bold">
        <CheckCircleFilled class="text-success me-2" />

        Thương hiệu
      </h4>
      <h6 class="text-primary">Xem chi tiết</h6>
    </div>

    <!-- Shop Cards -->
    <div class="row g-3">
      <div class="col-6 col-md-4 col-lg-2" v-for="(item, index) in shops" :key="index">
        <a :href="item.link" target="_blank" class="text-decoration-none text-dark">
          <div class="card h-100 shadow-sm">
            <div class="card-img-top bg-light d-flex align-items-center justify-content-center" style="height: 140px;">
              <img :src="item.logo" alt="logo" class="img-fluid" style="max-height: 80px; max-width: 80%;">
            </div>
            <div class="card-body text-center p-2">
              <p class="fw-semibold mb-1">{{ item.name }}</p>
              <small class="text-muted">
                <CheckCircleFilled style="color: #52c41a; margin-right: 0.5rem;" />
                Official store
              </small>
            </div>
          </div>
        </a>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { CheckCircleFilled } from '@ant-design/icons-vue'
import { ref } from 'vue'

interface Shop {
  logo: string
  name: string
  link: string
}

const shops = ref<Shop[]>([
  {
    logo: 'https://1000logos.net/wp-content/uploads/2017/03/Nike-Logo.png',
    name: 'Nike Official Store',
    link: 'https://shopee.vn/nike.officialstore',
  },
  {
    logo: 'https://1000logos.net/wp-content/uploads/2017/05/Adidas-logo.png',
    name: 'Adidas Official',
    link: 'https://shopee.vn/adidas.officialstore',
  },
  {
    logo: 'https://1000logos.net/wp-content/uploads/2021/04/Puma-logo.png',
    name: 'Puma',
    link: 'https://shopee.vn/puma.officialstore',
  },
  {
    logo: 'https://1000logos.net/wp-content/uploads/2020/09/Converse-logo.png',
    name: 'Converse',
    link: 'https://shopee.vn/converse.officialstore',
  },
  {
    logo: 'https://1000logos.net/wp-content/uploads/2017/03/Vans-logo.png',
    name: 'Vans Official',
    link: 'https://shopee.vn/vans.officialstore',
  },
  {
    logo: 'https://1000logos.net/wp-content/uploads/2017/03/New-Balance-Logo.png',
    name: 'New Balance',
    link: 'https://shopee.vn/newbalance.officialstore',
  },
])

</script>
