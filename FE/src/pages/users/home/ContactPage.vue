<template>
  <div class="d-flex w-100 px-3 px-md-4 pt-4 justify-content-center gap-3">
    <div class="d-flex w-100" style="max-width: 1200px">
      <div class="w-100">
        <!-- Contact Page Header -->
        <section
          class="contact-header text-center py-5 mb-4"
          style="background-color: #ffebee"
        >
          <h1 class="display-4 text-red fw-bold">Liên Hệ Với Chúng Tôi</h1>
          <p class="lead text-muted">
            Chúng tôi luôn sẵn sàng hỗ trợ bạn 24/7!
          </p>
        </section>

        <!-- Contact Content -->
        <section class="contact-content row g-4">
          <!-- Contact Information -->
          <div class="col-12 col-lg-4">
            <div
              class="contact-info-card p-4 h-100 bg-white shadow-sm rounded-3"
            >
              <h3 class="text-red mb-4">Thông Tin Liên Hệ</h3>
              <ul class="list-unstyled">
                <li class="mb-3 d-flex align-items-center">
                  <i class="bi bi-geo-alt-fill text-red me-2"></i>
                  <span
                    >Địa chỉ: 123 Đường ABC, Quận 1, TP. Hồ Chí Minh, Việt
                    Nam</span
                  >
                </li>
                <li class="mb-3 d-flex align-items-center">
                  <i class="bi bi-telephone-fill text-red me-2"></i>
                  <span>Hotline: +84 123 456 789</span>
                </li>
                <li class="mb-3 d-flex align-items-center">
                  <i class="bi bi-envelope-fill text-red me-2"></i>
                  <span>Email: support@example.com</span>
                </li>
                <li class="mb-3 d-flex align-items-center">
                  <i class="bi bi-clock-fill text-red me-2"></i>
                  <span>Giờ làm việc: 8:00 - 20:00, Thứ 2 - Chủ Nhật</span>
                </li>
              </ul>
              <div class="mt-4">
                <a
                  href="https://facebook.com"
                  target="_blank"
                  class="text-red me-3"
                >
                  <i class="bi bi-facebook fs-4"></i>
                </a>
                <a
                  href="https://instagram.com"
                  target="_blank"
                  class="text-red me-3"
                >
                  <i class="bi bi-instagram fs-4"></i>
                </a>
                <a href="https://twitter.com" target="_blank" class="text-red">
                  <i class="bi bi-twitter fs-4"></i>
                </a>
              </div>
            </div>
          </div>

          <!-- Contact Form -->
          <div class="col-12 col-lg-8">
            <div
              class="contact-form-card p-4 bg-white shadow-sm rounded-3 h-100"
            >
              <h3 class="text-red mb-4">Gửi Tin Nhắn Cho Chúng Tôi</h3>
              <form
                @submit.prevent="handleSubmit"
                class="needs-validation"
                novalidate
              >
                <div class="row g-3">
                  <div class="col-12 col-md-6">
                    <input
                      v-model="form.name"
                      type="text"
                      class="form-control border-red"
                      placeholder="Họ và tên *"
                      required
                    />
                    <div class="invalid-feedback">Vui lòng nhập họ và tên.</div>
                  </div>
                  <div class="col-12 col-md-6">
                    <input
                      v-model="form.email"
                      type="email"
                      class="form-control border-red"
                      placeholder="Email *"
                      required
                    />
                    <div class="invalid-feedback">
                      Vui lòng nhập email hợp lệ.
                    </div>
                  </div>
                  <div class="col-12">
                    <input
                      v-model="form.subject"
                      type="text"
                      class="form-control border-red"
                      placeholder="Tiêu đề *"
                      required
                    />
                    <div class="invalid-feedback">Vui lòng nhập tiêu đề.</div>
                  </div>
                  <div class="col-12">
                    <textarea
                      v-model="form.message"
                      class="form-control border-red"
                      rows="5"
                      placeholder="Nội dung *"
                      required
                    ></textarea>
                    <div class="invalid-feedback">Vui lòng nhập nội dung.</div>
                  </div>
                  <div class="col-12 text-end">
                    <button type="submit" class="btn btn-red px-4 py-2">
                      Gửi Tin Nhắn
                    </button>
                    <p v-if="submitSuccess" class="text-success mt-2">
                      Tin nhắn của bạn đã được gửi thành công!
                    </p>
                    <p v-if="submitError" class="text-danger mt-2">
                      Đã xảy ra lỗi. Vui lòng thử lại sau.
                    </p>
                  </div>
                </div>
              </form>
            </div>
          </div>
        </section>

        <!-- Map Section -->
        <section class="contact-map mt-5">
          <div class="map-card p-4 bg-white shadow-sm rounded-3">
            <h3 class="text-red mb-4">Vị Trí Của Chúng Tôi</h3>
            <div style="height: 300px" class="rounded-3 overflow-hidden">
              <!-- Placeholder for Google Maps or Map API -->
              <iframe
                src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3723.8680883965467!2d105.74435187503178!3d21.037963480613683!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x313455305afd834b%3A0x17268e09af37081e!2sT%C3%B2a%20nh%C3%A0%20FPT%20Polytechnic.!5e0!3m2!1svi!2s!4v1755178872694!5m2!1svi!2s"
                width="100%"
                height="100%"
                style="border: 0"
                allowfullscreen=""
                loading="lazy"
                referrerpolicy="no-referrer-when-downgrade"
              ></iframe>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";

const form = ref({
  name: "",
  email: "",
  subject: "",
  message: "",
});

const submitSuccess = ref(false);
const submitError = ref(false);

const handleSubmit = () => {
  // Validate form
  const formElement = document.querySelector(
    ".needs-validation"
  ) as HTMLFormElement;
  if (formElement.checkValidity()) {
    // Simulate API call or form submission
    console.log("Form submitted:", form.value);
    submitSuccess.value = true;
    submitError.value = false;
    form.value = { name: "", email: "", subject: "", message: "" }; // Reset form
    setTimeout(() => {
      submitSuccess.value = false;
    }, 3000);
  } else {
    formElement.classList.add("was-validated");
    submitError.value = true;
    submitSuccess.value = false;
  }
};
</script>

<style scoped>
.contact-header {
  border-bottom: 4px solid #ff0000;
}

.text-red {
  color: #ff0000 !important;
}

.border-red {
  border-color: #ff0000 !important;
}

.contact-info-card,
.contact-form-card,
.map-card {
  border-left: 4px solid #ff0000;
}

.btn-red {
  background-color: #ff0000;
  color: white;
  border: none;
  transition: background-color 0.3s ease;
}

.btn-red:hover {
  background-color: #cc0000;
}

.form-control:focus {
  border-color: #ff0000;
  box-shadow: 0 0 0 0.2rem rgba(255, 0, 0, 0.25);
}

.invalid-feedback {
  display: none;
}

.was-validated .form-control:invalid {
  border-color: #dc3545;
}

.was-validated .form-control:invalid ~ .invalid-feedback {
  display: block;
}

.was-validated .form-control:valid {
  border-color: #28a745;
}

@media (max-width: 768px) {
  .contact-content {
    flex-direction: column;
  }

  .contact-form-card,
  .contact-info-card {
    padding: 2rem;
  }
}
</style>