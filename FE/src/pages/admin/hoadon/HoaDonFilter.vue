<template>
  <DivCustom>
    <div class="d-flex align-items-center gap-4">
      <!-- Label: Tìm kiếm -->
      <div class="d-flex flex-column">
        <span class="text-sm text-gray-600 mb-1">Tìm kiếm</span>
        <div class="search-input-container">
          <a-input
            v-model:value="localSearchQuery"
            placeholder="Nhập mã hóa đơn, tên khách hàng, sdt, mã nhân viên..."
            style="width: 500px"
          />
          <a-tooltip title="Quét mã QR hóa đơn">
            <a-button
              class="qr-scan-button"
              @click="toggleQRScanner"
              :type="isQRScannerOpen ? 'primary' : 'default'"
            >
              <QrcodeOutlined />
            </a-button>
          </a-tooltip>
        </div>
      </div>

      <!-- Label: Khoảng thời gian -->
      <div class="d-flex flex-column">
        <span class="text-sm text-gray-600 mb-1">Chọn khoảng thời gian</span>
        <a-range-picker
          v-model:value="localDateRange"
          format="DD/MM/YYYY"
          :allowClear="true"
          style="width: 400px"
          :placeholder="['Ngày bắt đầu', 'Ngày kết thúc']"
        />
      </div>

      <!-- Nút làm mới -->
      <div class="filter-item reset-button-group">
          <a-button
            style="background-color: dimgrey; color: white; box-shadow: gray 1px 2px 2px;"
            @click="resetFilters"
            class="reset-button"
          >
            Đặt lại bộ lọc
            <ReloadOutlined />
          </a-button>
      </div>
    </div>

    <!-- QR Scanner Modal -->
    <a-modal
      v-model:open="isQRScannerOpen"
      title="Quét mã QR hóa đơn"
      :footer="null"
      width="500px"
      @cancel="closeQRScanner"
      :maskClosable="false"
      :afterOpenChange="handleModalOpenChange"
    >
      <div class="qr-scanner-container">
        <div v-if="!isCameraReady" class="camera-loading">
          <a-spin size="large" />
          <p>Đang khởi động camera...</p>
          <a-button v-if="!isCameraReady && isQRScannerOpen" @click="retryCamera" type="primary">
            Thử lại
          </a-button>
        </div>
        
        <!-- Always render video element but conditionally show it -->
        <div v-show="isCameraReady" class="camera-container">
          <video
            ref="videoRef"
            class="qr-video"
            autoplay
            playsinline
            muted
            :style="{ display: isCameraReady ? 'block' : 'none' }"
          ></video>
          <div class="qr-overlay">
            <div class="qr-frame">
              <div class="scanning-line"></div>
            </div>
          </div>
          <div class="qr-instruction">
            <p>Đưa mã QR vào khung để quét tự động</p>
          </div>
        </div>
        
        <div class="qr-controls">
          <a-button @click="closeQRScanner" class="cancel-btn">Đóng</a-button>
          <a-button v-if="!isCameraReady" @click="initializeCamera" type="primary">
            Khởi động camera
          </a-button>
          <a-button v-else type="primary" @click="captureQR" :loading="isProcessing">
            <CameraOutlined /> Chụp thủ công
          </a-button>
        </div>
        
        <div v-if="qrError" class="qr-error">
          <a-alert
            :message="qrError"
            type="error"
            show-icon
            closable
            @close="qrError = ''"
          />
        </div>
      </div>
    </a-modal>
  </DivCustom>
</template>

<script setup lang="ts">
import {
  ref,
  watch,
  defineProps,
  defineEmits,
  onMounted,
  onUnmounted,
} from "vue";
import DivCustom from "@/components/custom/Div/DivCustom.vue";
import {
  ReloadOutlined,
  QrcodeOutlined,
  CameraOutlined,
} from "@ant-design/icons-vue";
import dayjs from "dayjs";
import { message } from "ant-design-vue";
import jsQR from "jsqr";

const props = defineProps<{
  searchQuery: string;
  searchStatus: number | null;
}>();

const emit = defineEmits([
  "update:searchQuery",
  "update:searchStatus",
  "update:startDate",
  "update:endDate",
  "qr-scanned",
]);

const localSearchQuery = ref(props.searchQuery);
const localSearchStatus = ref(props.searchStatus);
const localDateRange = ref<[dayjs.Dayjs, dayjs.Dayjs] | null>(null);

// QR Scanner state
const isQRScannerOpen = ref(false);
const isCameraReady = ref(false);
const isProcessing = ref(false);
const qrError = ref("");
const videoRef = ref<HTMLVideoElement | null>(null);
let mediaStream: MediaStream | null = null;
let animationId: number | null = null;

watch(localDateRange, (newRange) => {
  if (newRange && newRange.length === 2) {
    emit("update:startDate", newRange[0].startOf("day").valueOf());
    emit("update:endDate", newRange[1].endOf("day").valueOf());
  } else {
    emit("update:startDate", null);
    emit("update:endDate", null);
  }
});

watch(localSearchQuery, (newValue) => {
  emit("update:searchQuery", newValue);
});

const resetFilters = () => {
  localSearchQuery.value = "";
  localSearchStatus.value = null;
  localDateRange.value = null;
  emit("update:searchQuery", "");
  emit("update:searchStatus", null);
  emit("update:startDate", null);
  emit("update:endDate", null);
};

const toggleQRScanner = () => {
  if (isQRScannerOpen.value) closeQRScanner();
  else {
    isQRScannerOpen.value = true;
    isCameraReady.value = false;
    qrError.value = "";
  }
};

const closeQRScanner = () => {
  isQRScannerOpen.value = false;
  isCameraReady.value = false;
  isProcessing.value = false;
  qrError.value = "";
  if (animationId) {
    cancelAnimationFrame(animationId);
    animationId = null;
  }
  if (mediaStream) {
    mediaStream.getTracks().forEach((t) => t.stop());
    mediaStream = null;
  }
  if (videoRef.value) videoRef.value.srcObject = null;
};

const initializeCamera = async () => {
  try {
    isCameraReady.value = false;
    qrError.value = "";
    await new Promise((r) => setTimeout(r, 300));
    if (!videoRef.value) throw new Error("Không tìm thấy video element");

    mediaStream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: "environment" },
    });
    videoRef.value.srcObject = mediaStream;

    await videoRef.value.play();
    isCameraReady.value = true;
    startQRScanning();
  } catch (e) {
    console.error(e);
    qrError.value = "Không thể khởi động camera";
    if (mediaStream) {
      mediaStream.getTracks().forEach((t) => t.stop());
      mediaStream = null;
    }
  }
};

const detectQRCode = (imageData: ImageData): string | null => {
  const code = jsQR(imageData.data, imageData.width, imageData.height);
  return code ? code.data : null;
};

// ✅ chỉnh lại: emit luôn update:searchQuery
const startQRScanning = () => {
  const scan = () => {
    if (!videoRef.value || !isCameraReady.value || !isQRScannerOpen.value) return;
    try {
      const canvas = document.createElement("canvas");
      canvas.width = videoRef.value.videoWidth;
      canvas.height = videoRef.value.videoHeight;
      const ctx = canvas.getContext("2d");
      if (ctx) {
        ctx.drawImage(videoRef.value, 0, 0);
        const data = ctx.getImageData(0, 0, canvas.width, canvas.height);
        const result = detectQRCode(data);
        if (result) {
          localSearchQuery.value = result;
          emit("update:searchQuery", result); // <-- thêm
          emit("qr-scanned", result);
          message.success("Quét QR thành công!");
          closeQRScanner();
          return;
        }
      }
    } catch (e) {
      console.error("Scan error:", e);
    }
    animationId = requestAnimationFrame(scan);
  };
  scan();
};

// ✅ chỉnh lại: emit luôn update:searchQuery
const captureQR = async () => {
  if (!videoRef.value || !isCameraReady.value) {
    qrError.value = "Camera chưa sẵn sàng";
    return;
  }
  try {
    isProcessing.value = true;
    const canvas = document.createElement("canvas");
    canvas.width = videoRef.value.videoWidth;
    canvas.height = videoRef.value.videoHeight;
    const ctx = canvas.getContext("2d");
    if (ctx) {
      ctx.drawImage(videoRef.value, 0, 0);
      const data = ctx.getImageData(0, 0, canvas.width, canvas.height);
      const result = detectQRCode(data);
      if (result) {
        localSearchQuery.value = result;
        emit("update:searchQuery", result); // <-- thêm
        emit("qr-scanned", result);
        message.success("Quét QR thành công!");
        closeQRScanner();
      } else {
        qrError.value = "Không tìm thấy mã QR";
      }
    }
  } catch (e) {
    console.error(e);
    qrError.value = "Có lỗi khi quét QR";
  } finally {
    isProcessing.value = false;
  }
};

onMounted(() => {
  const today = dayjs();
  localDateRange.value = [today, today];
});
onUnmounted(() => closeQRScanner());
</script>


<style scoped lang="scss">
.filter-item {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.search-input-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.qr-scan-button {
  height: 32px;
  width: 32px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;

  &:hover {
    background-color: #58bddb !important;
    border-color: #58bddb !important;
    color: white !important;
  }
}

.reset-button {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 32px;
  padding: 0 15px;
  margin-top: 25px;
}

.qr-scanner-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.camera-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 40px;

  p {
    margin: 0;
    color: #666;
  }
}

.camera-container {
  position: relative;
  width: 100%;
  max-width: 400px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #58bddb;
}

.qr-video {
  width: 100%;
  height: 300px;
  object-fit: cover;
  display: block;
}

.qr-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.qr-frame {
  width: 250px;
  height: 250px;
  border: 3px solid #58bddb;
  border-radius: 12px;
  background: rgba(88, 189, 219, 0.1);
  position: relative;
  overflow: hidden;

  &::before,
  &::after {
    content: "";
    position: absolute;
    width: 40px;
    height: 40px;
    border: 4px solid #58bddb;
  }

  &::before {
    top: -4px;
    left: -4px;
    border-right: none;
    border-bottom: none;
    border-radius: 12px 0 0 0;
  }

  &::after {
    bottom: -4px;
    right: -4px;
    border-left: none;
    border-top: none;
    border-radius: 0 0 12px 0;
  }
}

.scanning-line {
  position: absolute;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, transparent, #58bddb, transparent);
  animation: scan 2s linear infinite;
}

@keyframes scan {
  0% {
    top: 0;
  }
  100% {
    top: calc(100% - 3px);
  }
}

.qr-instruction {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;

  p {
    margin: 0;
    text-align: center;
  }
}

.qr-controls {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 16px;
}

.cancel-btn {
  min-width: 80px;
}

.qr-error {
  width: 100%;
  margin-top: 16px;
}

/* Hover effects */
:deep(.ant-input):hover {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.1) !important;
}

:deep(.ant-input-focused) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.1) !important;
}

:deep(.ant-picker):hover {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.1) !important;
}

:deep(.ant-picker-focused) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(88, 189, 219, 0.1) !important;
}
</style>