<template>
    <a-result
      status="403"
      title="403"
      sub-title="Xin lỗi, bạn không có đủ quyền truy cập vào tài nguyên."
    >
      <template #extra>
        <a-button type="primary" @click="$router.push('/')">Quay lại</a-button>
      </template>
    </a-result>
  </template>
  