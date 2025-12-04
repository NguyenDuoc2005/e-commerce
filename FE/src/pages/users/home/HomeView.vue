<template>
  <div class="d-flex w-100 px-3 px-md-4 pt-4 justify-content-center gap-3">
    <div class="d-flex w-100" style="max-width: 1200px;">
      <div class="w-100">
        <!-- Các thành phần chính -->
        <HomePage  />
        <!-- <ShopList /> -->
         <!-- <DiscountedProducts/> -->
        <NewProduct/>
        <PromotionList />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import DiscountedProducts from './DiscountedProducts.vue'
import HomePage from '@/pages/users/home/HomePage.vue'
import ShopList from '@/pages/users/home/ShopList.vue'
import NewProduct from '@/pages/users/home/NewProduct.vue'
import PromotionList from '@/pages/users/home/PromotionList.vue'
</script>
