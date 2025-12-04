<template>
  <DivCustom>
    <BreadcrumbDefault pageTitle="Sản phẩm chi tiết" />
    <FormCreateSanPham :responseCL="state.ChatLieu" :responseLD="state.LoaiDe" :responseLG="state.LoaiGiay"
      :responseXX="state.XuatXu" :products="state.products" @addCL="openAddModalCL" @addXX="openAddModalXX"
      @addLD="openAddModalLD" @addLG="openAddModalLG" @addColor="openAddModalColor" @addSize="openAddModalSize" />
    <LoaiGiayModal :open="state.isModalLGOpen" :productId="state.selectedProductId" @close="closeModalLG"
      @success="fetchProducts" />
    <ThuongHieuModal :open="state.isModalXXOpen" :productId="state.selectedProductId" @close="closeModalCXX"
      @success="fetchProducts" />
    <ChatLieuModal :open="state.isModalCLOpen" :productId="state.selectedProductId" @close="closeModalCL"
      @success="fetchProducts" />
    <LoaiDeModal :open="state.isModalLDOpen" :productId="state.selectedProductId" @close="closeModalLD"
      @success="fetchProducts" />
    <SizeModal :open="state.isModalSizeOpen" :productId="state.selectedProductId" @close="closeModalSize"
      @success="fetchProducts" />
    <MauSacModal :open="state.isModalColorOpen" :productId="state.selectedProductId" @close="closeModalColor"
      @success="fetchProducts" />
    <!-- <TableCreateSanPham /> -->

  </DivCustom>
</template>
<script setup lang="ts">
import FormCreateSanPham from './FormCreateSanPham.vue';
import TableCreateSanPham from './TableCreateSanPham.vue';
import LoaiDeModal from './LoaiDeModal.vue';
import LoaiGiayModal from './LoaiGiayModal.vue';
import XuatXuModal from './XuatXuModal.vue';
import ChatLieuModal from './ChatLieuModal.vue';
import ThuongHieuModal from './ThuongHieuModal.vue';
import SizeModal from './SizeModal.vue';
import MauSacModal from './MauSacModal.vue';
import { computed, onBeforeUnmount, onMounted, reactive } from 'vue';
import { useRoute } from 'vue-router';
import { getSanPham, type ParamsGetSanPham, type SanPhamResponse, type ThuocTinhResponse } from '@/services/api/admin/sanphamchitiet.api';
import type { SizeResponse } from '@/services/api/admin/size.api';

const route = useRoute();
const idSanPham = route.query.id ? String(route.query.id) : 'default-id';

const state = reactive({
  idSP: idSanPham,
  products: [] as SanPhamResponse[],
  ChatLieu: [] as ThuocTinhResponse[],
  XuatXu: [] as ThuocTinhResponse[],
  LoaiDe: [] as ThuocTinhResponse[],
  LoaiGiay: [] as ThuocTinhResponse[],
  Size: [] as ThuocTinhResponse[],
  Color: [] as ThuocTinhResponse[],
  searchQuery: '',
  searchStatus: null as number | null,
  isModalXXOpen: false,
  isModalLGOpen: false,
  isModalLDOpen: false,
  isModalCLOpen: false,
  isModalColorOpen: false,
  isModalSizeOpen: false,
  selectedProductId: null as string | null,
  size: [] as SizeResponse[],
  paginationParams: { page: 1, size: 10 },
  totalItems: 0
})

const openAddModalXX = () => {
  state.selectedProductId = null
  state.isModalXXOpen = true
}

const openAddModalLD = () => {
  state.selectedProductId = null
  state.isModalLDOpen = true
}

const openAddModalLG = () => {
  state.selectedProductId = null
  state.isModalLGOpen = true
}

const openAddModalCL = () => {
  state.selectedProductId = null
  state.isModalCLOpen = true
}

const openAddModalSize = () => {
  state.selectedProductId = null
  state.isModalSizeOpen = true
}

const openAddModalColor = () => {
  state.selectedProductId = null
  state.isModalColorOpen = true
}

const closeModalLD = () => {
  state.isModalLDOpen = false
}

const closeModalLG = () => {
  state.isModalLGOpen = false
}


const closeModalCXX = () => {
  state.isModalXXOpen = false
}


const closeModalCL = () => {
  state.isModalCLOpen = false
}

const closeModalColor = () => {
  state.isModalColorOpen = false
}
const closeModalSize = () => {
  state.isModalSizeOpen = false
}



const fetchProducts = async () => {
  try {
    if (state.idSP != "default-id") {
      console.log('Fetching products for ID:', idSanPham);
      const response = await getSanPham(state.idSP)
      state.products = response.data;
    }else {
      state.products = null;
    }

  } catch (error) {
    console.error('Failed to fetch products:', error)
  }
}

onMounted(() => {
  fetchProducts()
})



</script>
<style scoped lang="scss"></style>