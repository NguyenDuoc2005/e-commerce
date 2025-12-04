<script setup lang="ts">
const props = defineProps({
  customClasses: String,
  customClassesLabel: String,
  label: String
});
</script>

<template>
  <div class="rounded border-0 bg-secondary-subtle" :class="props.customClasses">
    <div class="bg-white rounded p-3 shadow-sm">

      <div>
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bg-secondary-subtle {
  background-color: white !important;
}

.rounded {
  border-radius: 8px;
}

.bg-white {
  background-color: white;
  border: 1px solid #e0e0e0;
  font-family: 'Roboto', sans-serif;
}
</style>
