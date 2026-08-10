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
  background: transparent;
  border-radius: 8px;
  margin: 6px 0 8px 18px;
  padding: 4px 0 4px 14px;
  border-left: 1px solid #d9e8ee;
  overflow: visible;
}

.dropdown-wrapper ul {
  margin: 0;
  padding: 0;
  list-style: none;
}

.dropdown-item {
  margin: 0;
}

.dropdown-link {
  display: flex;
  align-items: center;
  width: 100%;
  min-height: 38px;
  padding: 0 12px;
  font-size: 13.5px;
  color: #52606f;
  text-decoration: none;
  border-radius: 8px;
  position: relative;
  transition: background-color 0.18s ease, color 0.18s ease, transform 0.18s ease;
}

/* Dot chỉ hiển thị khi hover hoặc active */
.dropdown-link::before {
  content: "";
  position: absolute;
  left: -17px;
  top: 50%;
  transform: translateY(-50%) scaleY(0);
  width: 3px;
  height: 20px;
  background-color: var(--admin-primary, #54BDDB);
  border-radius: 999px;
  transition: transform 0.18s ease;
}

.dropdown-link:hover {
  background-color: rgba(84, 189, 219, 0.1);
  color: #1f7f98;
  transform: translateX(1px);
}

.dropdown-link:hover::before {
  transform: translateY(-50%) scaleY(1);
}

.dropdown-link.active {
  background-color: rgba(84, 189, 219, 0.16);
  color: #145f74;
  font-weight: 700;
}

.dropdown-link.active::before {
  transform: translateY(-50%) scaleY(1);
}

.label {
  margin-left: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
