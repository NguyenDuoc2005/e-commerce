<template>
    <div class="login-container">
      <h2>Đăng nhập</h2>
      <form @submit.prevent="handleLogin">
        <input v-model="username" type="text" placeholder="Tên đăng nhập" required />
        <input v-model="password" type="password" placeholder="Mật khẩu" required />
        <button type="submit">Đăng nhập</button>
        <p v-if="error" class="error">{{ error }}</p>
      </form>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { login, type LoginForm } from '@/services/api/auth/authentication.api';
  import { ROUTES_CONSTANTS } from '@/constants/path';
  
  const username = ref('');
  const password = ref('');
  const error = ref('');

  const router = useRouter();
  
  const handleLogin = async () => {
    try {
        const params : LoginForm={
            taiKhoan: username.value,
            matKhau: password.value
        }
      const response = await login(params);
      const data = response.data; // từ Helper.createResponseEntity(...)
      localStorage.setItem('accessToken', data); // nếu có token
      localStorage.setItem('user', JSON.stringify(data));
  
      // Điều hướng sau khi đăng nhập
      await router.push({
      path: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.MAUSAC.path}`
    });

    } catch (err: any) {
      error.value = err.response?.data?.message || 'Đăng nhập thất bại';
    }
  };
  </script>
  
  <style scoped>
  .login-container {
    max-width: 400px;
    margin: 100px auto;
    padding: 24px;
    border: 1px solid #ddd;
    border-radius: 8px;
  }
  
  input {
    width: 100%;
    padding: 8px;
    margin: 8px 0;
  }
  
  button {
    width: 100%;
    padding: 10px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
  }
  
  .error {
    color: red;
    margin-top: 8px;
  }
  </style>
  