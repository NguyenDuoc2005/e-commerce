<template>
  <div class="container mt-5">
    <div class="row g-3">
      <!-- Ảnh lớn bên trái -->
      <div class="col-12 col-lg-6 d-flex">
        <div class="w-100 rounded overflow-hidden" style="height: 420px;">
          <img
            :src="mainImage"
            alt="Main"
            class="w-100 h-100 object-fit-cover"
          />
        </div>
      </div>

      <!-- Ảnh nhỏ bên phải -->
      <div class="col-12 col-lg-6">
        <div class="row g-3">
          <div
            v-for="(img, idx) in smallImages"
            :key="idx"
            class="col-6"
          >
            <div class="w-100 rounded overflow-hidden" style="height: 200px;">
              <img
                :src="img"
                alt="Small"
                class="w-100 h-100 object-fit-cover"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const mainImage = ref(
  'https://prices.vn/photos/8/blog/review-giay-nike-nam.jpg'
)

const smallImages = ref<string[]>([
  'https://tse4.mm.bing.net/th/id/OIP.C0b3zlLfZ0GD-5txQXOkzQHaE8?pid=Api&P=0&h=180', // Giày sneaker trắng
  'https://file.hstatic.net/1000061481/file/giay_da_bong_dep_nhat__34__8770f24415924befa037e429ad3ef1f4.jpg', // Giày thể thao đen nền xám
  'https://tse1.mm.bing.net/th/id/OIP.eRwwT0XbgUyZL-3t9BbhOgHaFj?pid=Api&P=0&h=180', // Giày màu trắng cổ thấp
  'https://salt.tikicdn.com/cache/w1200/ts/product/32/2e/aa/716e8f83bcfef3e249922c0685fd1247.jpg', // Giày thể thao đường phố
])


</script>

<style scoped>
.object-fit-cover {
  object-fit: cover;
}
</style>
