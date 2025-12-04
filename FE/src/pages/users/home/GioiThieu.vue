<template>
  <div class="intro-container">
    <header class="intro-header">
      <h1>GIỚI THIỆU VỀ GLAMESOLE</h1>
    </header>
    <main class="intro-content">
      <section class="about-us">
        <h2>Về Chúng Tôi</h2>
        <p>
          Chào mừng bạn đến với <strong>GLAMESOLE</strong> - điểm đến lý tưởng cho những tín đồ yêu thích giày thể thao chính hãng, phong cách và chất lượng vượt trội! Chúng tôi tự hào là một trong những shop hàng đầu chuyên cung cấp các mẫu giày thể thao đa dạng, từ những thiết kế cổ điển đến các xu hướng thời trang hiện đại, phù hợp cho cả nam và nữ.
        </p>
        <p>
          Ra đời với niềm đam mê mang đến những đôi giày hoàn hảo, GLAMESOLE cam kết mang lại trải nghiệm mua sắm tuyệt vời với:
        </p>
        <ul>
          <li><strong>Sản phẩm chất lượng cao:</strong> Tất cả giày tại GLAMESOLE đều được nhập khẩu chính hãng, đảm bảo độ bền và phong cách.</li>
          <li><strong>Đa dạng mẫu mã:</strong> Từ giày chạy bộ, giày tập gym đến giày thời trang, đáp ứng mọi nhu cầu của bạn.</li>
          <li><strong>Dịch vụ tận tâm:</strong> Đội ngũ nhân viên chuyên nghiệp, sẵn sàng hỗ trợ 24/7.</li>
        </ul>
      </section>
      <section class="mission">
        <h2>Sứ Mệnh Của Chúng Tôi</h2>
        <p>
          Tại GLAMESOLE, chúng tôi không chỉ bán giày mà còn lan tỏa phong cách sống năng động và tự tin. Mỗi đôi giày là một câu chuyện, một hành trình mà chúng tôi muốn đồng hành cùng bạn trên mọi bước đường.
        </p>
      </section>
      <section class="contact">
        <h2>Liên Hệ Với Chúng Tôi</h2>
        <p>
          Địa chỉ: 123 Đường Giày Thể Thao, Quận 1, TP. Hồ Chí Minh<br>
          Hotline: 0366 994 511<br>
          Email: support@glamesole.com
        </p>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

const currentDate = ref(new Date().toLocaleString('vi-VN', { timeZone: 'Asia/Ho_Chi_Minh' }));

onMounted(() => {
  console.log('Trang giới thiệu đã được tải vào lúc:', currentDate.value);
});
</script>

<style scoped>
.intro-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.intro-header {
  text-align: center;
  padding: 20px 0;
  background-color: #EA2F38;
  color: white;
  border-radius: 5px;
}

.intro-header h1 {
  margin: 0;
  font-size: 2.5rem;
}

.intro-content {
  padding: 20px;
}

section {
  margin-bottom: 30px;
}

h2 {
  color: #EA2F38;
  font-size: 1.8rem;
  border-bottom: 2px solid #EA2F38;
  padding-bottom: 5px;
}

p, ul {
  font-size: 1.1rem;
  line-height: 1.6;
}

ul {
  list-style-type: disc;
  padding-left: 20px;
}

strong {
  color: #D32F2F;
}

.contact {
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 5px;
}
</style>