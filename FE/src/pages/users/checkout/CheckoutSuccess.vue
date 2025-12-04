<template>
  <div class="d-flex justify-content-center align-items-center w-100 bg-light" style="margin-top: 100px;">
    <div class="position-relative border rounded shadow p-4 bg-white" style="max-width: 500px; width: 100%;">
      <!-- Icon Thành công -->
      <div class="position-absolute top-0 start-50 translate-middle" style="transform: translate(-50%, -50%);">
        <div class="bg-white shadow rounded-circle d-flex justify-content-center align-items-center" style="width: 80px; height: 80px;">
          <CheckOutlined class="text-success fs-1" />
        </div>
      </div>

      <!-- Nội dung chính -->
      <div class="text-center pt-5 mt-3">
        <h4 class="fw-bold text-success">Đặt hàng thành công!</h4>
        <p class="mt-3">
          Cảm ơn bạn đã đặt hàng
        </p>
        <p class="text-muted small">
          Chúng tôi sẽ gửi email xác nhận với thông tin chi tiết và đường dẫn để theo dõi đơn hàng của bạn.
        </p>
      </div>

      <hr class="my-4" />

      <!-- Footer -->
      <div class="text-center">
        <p class="mb-3">Nếu có bất kỳ câu hỏi nào, hãy liên hệ với chúng tôi.</p>
        <div class="d-flex justify-content-center gap-3">
          <RouterLink to="/" class="btn btn-outline-primary">Tiếp tục mua sắm</RouterLink>
          <!-- <button class="btn btn-primary" @click="toTracking">Theo dõi đơn hàng</button> -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { CheckOutlined } from '@ant-design/icons-vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const toTracking = () => {
  router.push({ name: 'thanh-toan-thanh-cong', params: { id: route.params.id } })
}
</script>
