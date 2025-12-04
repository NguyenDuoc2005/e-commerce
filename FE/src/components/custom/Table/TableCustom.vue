<script setup lang="ts">
import { ref, watch } from 'vue'
import { Pagination } from 'ant-design-vue'

const props = defineProps({
  data: {
    type: Array,
    required: true
  },
  columns: {
    type: Array,
    required: true
  },
  pageSize: {
    type: Number,
    default: 5
  },
  total: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['change'])

const currentPage = ref(1)
const currentPageSize = ref(props.pageSize)

const handlePageChange = (page: number, pageSize?: number) => {
  currentPage.value = page
  if (pageSize) {
    currentPageSize.value = pageSize
  }
  emit('change', page, pageSize)
}

watch(
  () => props.pageSize,
  (newPageSize) => {
    currentPageSize.value = newPageSize
  }
)
</script>

<template>
  <div
    class="rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark"
  >
    <!-- Table Header -->
    <div
      class=" grid grid-cols-12 border border-stroke py-4.5 px-4 dark:border-strokedark sm:grid-cols-12 md:px-6 2xl:px-7.5"
    >
      <div v-for="column in columns" :key="column.key" :class="column.class">
        <p class="font-bold text-sm flex justify-center items-center">{{ column.title }}</p>
      </div>
    </div>

    <!-- Table Rows -->
    <div
      v-for="item in data"
      :key="item.id"
      class="grid grid-cols-12 border-t border-stroke py-4.5 px-4 dark:border-strokedark sm:grid-cols-12 md:px-6 2xl:px-7.5"
    >
      <slot :item="item"></slot>
    </div>
  </div>

  <!-- Pagination and Page Size Selector -->
  <div class="py-4.5 px-4 md:px-6 2xl:px-7.5 flex justify-end">
    <Pagination
      :current="currentPage"
      :total="props.total"
      :pageSize="currentPageSize"
      @change="handlePageChange"
      showLessItems
    />
  </div>
</template>

<style scoped>
.break-words {
  word-wrap: break-word;
  word-break: break-word;
  white-space: normal;
  padding-right: 10px;
}
</style>