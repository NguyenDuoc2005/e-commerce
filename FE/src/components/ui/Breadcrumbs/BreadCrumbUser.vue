<script setup>
defineProps({
  routes: {
    type: Array,
    required: true,
  },
  title: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <div class="bg-light border-bottom py-2 px-3 rounded mb-3">
    <div class="container d-flex flex-column flex-md-row justify-content-between align-items-md-center">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb mb-0 d-flex align-items-center">
          <li
            v-for="(item, index) in routes"
            :key="index"
            class="breadcrumb-item d-flex align-items-center"
            :class="{ active: index === routes.length - 1 }"
            aria-current="page"
          >
            <template v-if="index !== routes.length - 1">
              <RouterLink :to="item.path" class="text-decoration-none text-primary small fw-semibold">
                {{ item.name }}
              </RouterLink>
            </template>
            <template v-else>
              <span class="text-muted small fw-semibold">{{ item.name }}</span>
            </template>
          </li>
        </ol>
      </nav>

      <!-- Title bên phải -->
      <h6
  v-if="title"
  class="fw-semibold text-muted mb-0 small text-center text-md-end d-none d-md-block"
>
  {{ title }}
</h6>

    </div>
  </div>
</template>

<style scoped>
.breadcrumb-item + .breadcrumb-item::before {
  content: "›";
  color: #6c757d;
  margin: 0 0.5rem;
  font-weight: bold;
}
</style>
