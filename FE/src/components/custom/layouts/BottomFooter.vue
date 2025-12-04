<template>
  <footer class="bg-light text-muted py-5 mt-5 border-top w-100">
    <div class="container">
      <div class="row text-center text-md-start">
        <!-- Logo & Giới thiệu -->
        <div class="col-md-3 mb-4">
          <img
            src="https://openwebdocs.org/img/openwebdocs-logo-light.png"
            alt="Logo"
            style="height: 50px; object-fit: contain"
            class="mb-3"
          />
          <p style="font-size: 0.9rem">
            Emmable cung cấp các giải pháp công nghệ hiện đại, mang lại giá trị
            bền vững cho khách hàng và cộng đồng.
          </p>
        </div>

        <!-- Liên kết nhanh -->
        <div class="col-md-3 mb-4">
          <h6 class="fw-bold mb-3">Liên kết nhanh</h6>
          <ul class="list-unstyled">
            <li>
              <a href="#" class="text-decoration-none text-muted"
                >Về chúng tôi</a
              >
            </li>
            <li>
              <a href="#" class="text-decoration-none text-muted">Hỗ trợ</a>
            </li>
            <li>
              <a href="#" class="text-decoration-none text-muted">Liên hệ</a>
            </li>
            <li>
              <a href="#" class="text-decoration-none text-muted"
                >Chính sách bảo mật</a
              >
            </li>
          </ul>
        </div>

        <!-- Thông tin liên hệ -->
        <div class="col-md-3 mb-4">
          <h6 class="fw-bold mb-3">Liên hệ</h6>
          <p class="mb-1">
            <i class="bi bi-geo-alt-fill"></i> Phố Trịnh Văn Bô, Từ Liêm, Hà Nội
          </p>
          <p class="mb-1">
            <i class="bi bi-telephone-fill"></i> +84 123 456 789
          </p>
          <p class="mb-1">
            <i class="bi bi-envelope-fill"></i> contact@emmable.com
          </p>
          <div class="d-flex gap-3 mt-2">
            <a href="#" class="text-muted"><i class="bi bi-facebook"></i></a>
            <a href="#" class="text-muted"><i class="bi bi-twitter"></i></a>
            <a href="#" class="text-muted"><i class="bi bi-instagram"></i></a>
            <a href="#" class="text-muted"><i class="bi bi-linkedin"></i></a>
          </div>
        </div>

        <!-- Google Map -->
        <div class="col-md-3 mb-4">
          <h6 class="fw-bold mb-3">Bản đồ</h6>
          <iframe
            src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3723.8680883965467!2d105.74435187503178!3d21.037963480613683!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x313455305afd834b%3A0x17268e09af37081e!2sT%C3%B2a%20nh%C3%A0%20FPT%20Polytechnic.!5e0!3m2!1svi!2s!4v1755178872694!5m2!1svi!2s"
            width="300"
            height="150"
            style="border: 0"
            allowfullscreen=""
            loading="lazy"
            referrerpolicy="no-referrer-when-downgrade"
          ></iframe>
        </div>
      </div>

      <!-- Copyright -->
      <div class="text-center pt-3 mt-3 border-top" style="font-size: 0.875rem">
        © {{ currentYear }} Emmable. All rights reserved.
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from "vue";

const currentYear = computed(() => new Date().getFullYear());
</script>

<style scoped>
footer a:hover {
  color: #495057;
}

.bi {
  font-size: 1.3rem;
}

@media (max-width: 576px) {
  .bi {
    font-size: 1.1rem;
  }
  .container {
    padding: 0 1rem;
  }
  a.text-decoration-none {
    font-size: 0.9rem;
  }
}
</style>
