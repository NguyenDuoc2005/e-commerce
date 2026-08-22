<template>
  <div v-if="loading" class="banner-skeleton" />
  <div v-else-if="banners.length" id="bannerCarousel" class="carousel slide w-100" data-bs-ride="carousel">
    <div class="carousel-inner overflow-hidden">
      <div v-for="(item, index) in banners" :key="item.id" class="carousel-item" :class="{ active: index === 0 }">
        <router-link v-if="item.targetUrl?.startsWith('/')" :to="item.targetUrl">
          <img :src="item.imageUrl" class="d-block w-100 banner-img" :alt="item.title" />
        </router-link>
        <a v-else :href="item.targetUrl || undefined" :target="item.targetUrl ? '_blank' : undefined" rel="noopener">
          <img :src="item.imageUrl" class="d-block w-100 banner-img" :alt="item.title" />
        </a>
      </div>
    </div>
    <div v-if="banners.length > 1" class="carousel-indicators">
      <button v-for="(item, index) in banners" :key="`dot-${item.id}`" type="button"
        data-bs-target="#bannerCarousel" :data-bs-slide-to="index" :class="{ active: index === 0 }"
        :aria-current="index === 0" :aria-label="`Slide ${index + 1}`" />
    </div>
    <template v-if="banners.length > 1">
      <button class="carousel-control-prev" type="button" data-bs-target="#bannerCarousel" data-bs-slide="prev" aria-label="Banner truoc">
        <span class="carousel-control-prev-icon" aria-hidden="true" />
      </button>
      <button class="carousel-control-next" type="button" data-bs-target="#bannerCarousel" data-bs-slide="next" aria-label="Banner tiep theo">
        <span class="carousel-control-next-icon" aria-hidden="true" />
      </button>
    </template>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { getPublicBanners, type PlatformBanner } from '@/services/api/admin/banner.api'

const banners = ref<PlatformBanner[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const response = await getPublicBanners()
    banners.value = response.data ?? []
    await nextTick()
    const carouselElement = document.getElementById('bannerCarousel')
    if (carouselElement && typeof (window as any).bootstrap !== 'undefined') {
      new (window as any).bootstrap.Carousel(carouselElement, { interval: 5000, ride: 'carousel' })
    }
  } catch {
    banners.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
#bannerCarousel,
.carousel-inner,
.banner-skeleton {
  width: 100%;
  min-height: 180px;
  aspect-ratio: 12 / 5;
  background: #f1f3f5;
}
.banner-img {
  width: 100%;
  aspect-ratio: 12 / 5;
  object-fit: cover;
  display: block;
}
.banner-skeleton { animation: pulse 1.4s ease-in-out infinite; }
@keyframes pulse { 50% { opacity: 0.55; } }
</style>
