<template>
    <DivCustom label="Danh sách sản phẩm chi tiết" customClasses="mt-5">
      <h4>Chi tiết sản phẩm</h4>
      
      <!-- Nút "Hoàn Tất" ở trên -->
      <div class="finish-button-container">
        <a-button style="background-color: #54bddb;" type="primary" class="finish-button">
          Hoàn Tất
        </a-button>
      </div>
  
      <div class="min-h-[360px] table">
        <a-table :columns="columns" :pagination="{
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '30', '40', '50']
        }" :scroll="{ y: 240 }">
          <template #bodyCell="{ column, record }">
            <div v-if="column.key === 'stt'">
              {{ products.indexOf(record) + 1 }}
            </div>
  
            <template v-if="column.key === 'anh'">
              <div class="center-cell">
                <img :src="record.avatar" class="anh"
                  style="width: 50px; height: 50px; border-radius: 50%" />
              </div>
            </template>
  
            <template v-if="column.key === 'mau'">
              <div class="center-cell">
                <div class="color"
                  :style="{ width: '30px', height: '30px', backgroundColor: record.mau, border: '1px solid #000' }">
                </div>
              </div>
            </template>
  
            <!-- Ô input mới (ví dụ cho cột "Số lượng") -->
            <template v-if="column.key === 'soLuong'">
              <a-input v-model="record.soLuong" style="width: 100px;" />
            </template>
  
            <!-- Cột Upload ảnh -->
            <template v-if="column.key === 'upload'">
              <a-button type="dashed" @click="handleUpload(record.id)" style="width: 100px;">
                Upload
              </a-button>
            </template>
  
          </template>
        </a-table>
      </div>
    </DivCustom>
  </template>
  
  <script setup lang="ts">
  import { PlusCircleOutlined, RedoOutlined, EyeOutlined } from '@ant-design/icons-vue';
  import { defineEmits, defineProps } from 'vue';
  import type { TableColumnsType } from 'ant-design-vue';
  

  
  const columns: TableColumnsType = [
      { title: 'STT', key: 'stt', dataIndex: 'stt', width: 80, align: 'center' },
      { title: 'Tên sản phẩm', key: 'ten', dataIndex: 'ten', width: 150, align: 'center' },
      { title: 'Số lượng', key: 'soLuong', dataIndex: 'soLuong', width: 150, align: 'center' },
      { title: 'Giá bán', key: 'giaBan', dataIndex: 'giaBan', width: 150, align: 'center' },
      { title: 'Kích cỡ', key: 'kichThuoc', dataIndex: 'kichThuoc', width: 150, align: 'center' },
      { title: 'Màu sắc', key: 'mau', dataIndex: 'mau', width: 150, align: 'center' },
      { title: 'Ảnh', key: 'anh', dataIndex: 'anh', width: 150, align: 'center' },
      { title: 'Trạng thái', key: 'status', dataIndex: 'status', width: 150, align: 'center' },
      { title: 'Hành động', key: 'operation', width: 90, align: 'center' },
      { title: 'Upload ảnh', key: 'upload', width: 150, align: 'center' },
  ];
  
  const handleChangeStatusClick = async (id: string) => {
      // Logic để thay đổi trạng thái
  }
  
  const handleClick = (idSanPham: string) => {
      // Logic để xem chi tiết sản phẩm
  }
  
  const handleUpload = (id: string) => {
      // Logic để tải ảnh lên
  }
  </script>
  
  <style scoped lang="scss">
  .color {
      border-radius: 50%;
  }
  
  .center-cell {
      display: flex;
      justify-content: center;
      align-items: center;
  }
  
  .input-with-button {
      display: flex;
      align-items: center;
      gap: 10px;
  }
  
  h4 {
      text-align: center;
      margin-bottom: 20px;
      font-size: 24px;
      font-weight: bold;
  }
  
  /* Nút Hoàn Tất */
  .finish-button-container {
    text-align: right;
    margin: 20px;
  }
  
  .finish-button {
    background-color: #1890ff;
    color: white;
    padding: 8px 16px;
    border-radius: 5px;
    font-weight: bold;
    font-size: 14px;
    width: 120px;
  
    &:hover {
      background-color: #40a9ff;
    }
  }
  .table{
    margin-left: 10px;
  }
  </style>
  