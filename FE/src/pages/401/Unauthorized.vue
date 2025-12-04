<template>
    <a-result status="401" title="401" sub-title="Xin lỗi, bạn chưa xác thực thông tin.">
      <template #extra>
        <a-button type="primary" @click="$router.push('/')">Quay lại</a-button>
      </template>
    </a-result>
  </template>
  