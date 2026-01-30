Ah được, để tôi tóm gọn lại quy trình chuẩn:
🚀 QUY TRÌNH DEPLOY CHUẨN THỰC TẾ
📦 CHUẨN BỊ 1 LẦN (Setup đầu tiên)
Local:
bash# 1. Đăng nhập Docker Hub
docker login
VPS:
bash# 1. Cài Docker
curl -fsSL https://get.docker.com | sh
apt install -y docker-compose-plugin

# 2. Tạo thư mục và upload file
mkdir -p /opt/ecommerce
cd /opt/ecommerce

# Upload: docker-compose.yml và .env
scp docker-compose.yml .env root@SERVER_IP:/opt/ecommerce/

🔄 QUY TRÌNH HẰNG NGÀY (Mỗi lần update code)
1️⃣ Local / CI: Build + Push
bash# Build image
docker build -t nguyenduoc/datn-be:20260130-01 . (dùng để chạy dockerfile)

# Tag latest
docker tag nguyenduoc/datn-be:20260130-01 nguyenduoc/datn-be:latest ( gán tag ->)

# Push lên Docker Hub
docker push nguyenduoc/datn-be:20260130-01
docker push nguyenduoc/datn-be:latest
2️⃣ VPS: Pull + Restart
bashcd /opt/ecommerce

# Pull image mới
docker compose pull

# Restart
docker compose up -d

🧠 TÓM TẮT CẢ QUY TRÌNH – 1 SƠ ĐỒ DUY NHẤT
Dockerfile + Code
↓
docker build        → tạo Docker IMAGE
↓
docker push         → đưa IMAGE lên Docker Hub (repo của bạn)
↓
VPS: docker compose pull → kéo IMAGE về
↓
docker compose up -d     → chạy app
