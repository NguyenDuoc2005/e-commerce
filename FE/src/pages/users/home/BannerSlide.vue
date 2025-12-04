```vue
<template>
  <div id="bannerCarousel" class="carousel slide w-100" data-bs-ride="carousel"
    style="max-width: 100vw; margin: 0 auto;">
    <div class="carousel-inner overflow-hidden" style="width: 100%;">
      <div v-for="(item, index) in banners" :key="item.id" class="carousel-item" :class="{ active: index === 0 }">
        <img :src="item.image" class="d-block w-100 banner-img" alt="Banner" />
      </div>
    </div>

    <!-- Dots -->
    <div class="carousel-indicators">
      <button v-for="(item, index) in banners" :key="'dot-' + index" type="button" data-bs-target="#bannerCarousel"
        :data-bs-slide-to="index" :class="{ active: index === 0 }" :aria-current="index === 0"
        :aria-label="'Slide ' + (index + 1)"></button>
    </div>

    <!-- Controls -->
    <button class="carousel-control-prev" type="button" data-bs-target="#bannerCarousel" data-bs-slide="prev">
      <span class="carousel-control-prev-icon" aria-hidden="true"></span>
      <span class="visually-hidden">Previous</span>
    </button>
    <button class="carousel-control-next" type="button" data-bs-target="#bannerCarousel" data-bs-slide="next">
      <span class="carousel-control-next-icon" aria-hidden="true"></span>
      <span class="visually-hidden">Next</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';

const banners = [
  {
    id: 1,
    image: 'https://bizweb.dktcdn.net/100/347/092/themes/708609/assets/slider_1.jpg?1753792346461',
  },
  {
    id: 2,
    image: 'https://mka.com.vn/wp-content/uploads/2019/09/a-1568x777.jpg',
  },
  {
    id: 3,
    image: 'http://introngoi.com/wp-content/uploads/2017/03/mau-in-poster-an-tuong-2.jpg',
  },
];

// Initialize Bootstrap carousel on component mount
onMounted(() => {
  const carouselElement = document.getElementById('bannerCarousel');
  if (carouselElement) {
    // Ensure Bootstrap's Carousel is available
    if (typeof window.bootstrap !== 'undefined') {
      new window.bootstrap.Carousel(carouselElement, {
        interval: 5000, // Auto-slide every 5 seconds
        ride: 'carousel',
      });
    }
  }
});
</script>

<style scoped>
.carousel-inner {
  width: 100%;
  height: 520px;  
  background: #fff; 
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: contain;    
  object-position: center;
  display: block;
  background: #fff;
}

@media (max-width: 991px) {
  .carousel-inner {
    height: 320px;
  }
  .banner-img {
    height: 100%;
  }
}

@media (max-width: 576px) {
  .carousel-inner {
    height: 180px;
  }
  .banner-img {
    height: 100%;
  }
}
</style>
```