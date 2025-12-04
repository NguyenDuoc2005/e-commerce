<template>
  <div v-if="isOpen" class="dropdown-wrapper">
    <ul>
      <li
        v-for="(item, index) in items"
        :key="index"
        class="dropdown-item"
      >
        <router-link
          :to="{ name: item.routeName }"
          @click="handleItemClick(index)"
          class="dropdown-link"
          :class="{ active: isActive(item) }"
        >
          <span class="label">{{ item.label }}</span>
        </router-link>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from "vue";
import { useRoute } from "vue-router";

const props = defineProps({
  items: {
    type: Array,
    required: true,
  },
  isOpen: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["item-click"]);
const route = useRoute();

function handleItemClick(index) {
  emit("item-click", index);
}

const isActive = (item) => {
  return route.name === item.routeName;
};
</script>

<style scoped>
.dropdown-wrapper {
  background: #ffffff;
  border-radius: 8px;
  margin-top: 6px;
  overflow: hidden;
}

.dropdown-item {
  margin: 0;
}

.dropdown-link {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 8px 20px;
  font-size: 14px;
  color: #334155;
  text-decoration: none;
  border-radius: 6px;
  position: relative;
  transition: background-color 0.2s ease, color 0.2s ease;
}

/* Dot chỉ hiển thị khi hover hoặc active */
.dropdown-link::before {
  content: "";
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%) scale(0);
  width: 4px;
  height: 4px;
  background-color: #59bddb;
  border-radius: 50%;
  transition: transform 0.2s ease;
}

.dropdown-link:hover {
  background-color: #f1f5f9;
  color: #59bddb;
}

.dropdown-link:hover::before {
  transform: translateY(-50%) scale(1);
}

.dropdown-link.active {
  background-color: #59bddb;
  color: white;
  font-weight: 500;
}

.dropdown-link.active::before {
  transform: translateY(-50%) scale(1);
}

.label {
  margin-left: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
