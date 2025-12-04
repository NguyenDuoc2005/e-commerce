<script setup lang="ts">
import { defineProps } from 'vue';
import { HomeOutlined } from '@ant-design/icons-vue'; // Import the Home icon for the first breadcrumb item


const props = defineProps<{
  pageTitle: string; 
  routes: Array<{ path: string; name: string }>; 
}>();
</script>

<template>
  <div class="breadcrumb-container">
    <h2 class="page-header-title">
      {{ props.pageTitle }}
    </h2>

    <a-breadcrumb>
      <a-breadcrumb-item v-for="(route, index) in props.routes" :key="index">
        <template v-if="index === 0">
        </template>
        <router-link v-if="index < props.routes.length - 1" :to="route.path">
          {{ route.name }}
        </router-link>
        <span v-else>
          {{ route.name }}
        </span>
      </a-breadcrumb-item>
    </a-breadcrumb>
  </div>
</template>

<style scoped lang="scss">
.breadcrumb-container {
  // Styles for the container of the title and breadcrumb
  padding: 10px 0; // Adjust padding as needed
}

.page-header-title {
  font-size: 1.5rem; 
  font-weight: bold; 
  color: #333; 
  margin-bottom: 10px; 
  font-family: 'Roboto', sans-serif;
}

:deep(.ant-breadcrumb) {
  font-size: 0.9rem;
  color: #666; 
  font-family: 'Roboto', sans-serif;

  .ant-breadcrumb-link {
    color: #007bff; // Link color
    &:hover {
      color: #0056b3; // Darker on hover
    }
  }

  .ant-breadcrumb-separator {
    color: #999; // Separator color
  }

  .anticon-home {
    margin-right: 5px; // Space between home icon and text
  }
}
</style>  