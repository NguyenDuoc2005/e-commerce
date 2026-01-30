# 🚀 HƯỚNG DẪN DEPLOY ỨNG DỤNG ECOMMERCE LÊN VPS

> **Tài liệu đầy đủ từ server mới cứng đến chạy production**

---

## 📑 MỤC LỤC

1. [Chuẩn bị VPS mới](#1-chuẩn-bị-vps-mới)
2. [Cài đặt môi trường](#2-cài-đặt-môi-trường)
3. [Tạo cấu trúc thư mục](#3-tạo-cấu-trúc-thư-mục)
4. [Tạo file cấu hình](#4-tạo-file-cấu-hình)
5. [Cấu hình Nginx](#5-cấu-hình-nginx)
6. [Cài đặt SSL](#6-cài-đặt-ssl)
7. [Chạy ứng dụng](#7-chạy-ứng-dụng)
8. [Quy trình update code](#8-quy-trình-update-code)
9. [Các lệnh hữu ích](#9-các-lệnh-hữu-ích)
10. [Troubleshooting](#10-troubleshooting)

---

## 1️⃣ CHUẨN BỊ VPS MỚI

### Yêu cầu
- VPS Ubuntu 20.04/22.04/24.04
- RAM tối thiểu: 2GB
- Disk: 20GB+
- Domain đã trỏ về IP VPS

### Đăng nhập VPS
```bash
ssh root@YOUR_SERVER_IP
```

### Update hệ thống
```bash
apt update && apt upgrade -y
```

---

## 2️⃣ CÀI ĐẶT MÔI TRƯỜNG

### 2.1. Cài Docker
```bash
curl -fsSL https://get.docker.com | sh

# Kiểm tra
docker --version
```

### 2.2. Cài Docker Compose Plugin
```bash
apt install -y docker-compose-plugin

# Kiểm tra
docker compose version
```

### 2.3. Cài Nginx
```bash
apt install -y nginx

# Start và enable
systemctl start nginx
systemctl enable nginx

# Kiểm tra status
systemctl status nginx
```

### 2.4. Cài Certbot (SSL miễn phí)
```bash
apt install -y certbot python3-certbot-nginx
```

---

## 3️⃣ TẠO CẤU TRÚC THỦ MỤC

```bash
# Tạo thư mục chính cho app
mkdir -p /opt/ecommerce
cd /opt/ecommerce

# Tạo thư mục cho volume data
mkdir -p data/postgres data/redis uploads logs
```

---

## 4️⃣ TẠO FILE CẤU HÌNH

### 4.1. Tạo `docker-compose.yml`

```bash
nano /opt/ecommerce/docker-compose.yml
```

**Nội dung file:**

```yaml
version: '3.8'

services:
  app:
    image: nguyenduoc/datn-be:latest
    container_name: ecommerce-app
    restart: unless-stopped
    ports:
      - "3000:3000"
    env_file:
      - .env
    volumes:
      - ./uploads:/app/uploads
      - ./logs:/app/logs
    depends_on:
      - postgres
      - redis
    networks:
      - ecommerce-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3000/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  postgres:
    image: postgres:15-alpine
    container_name: ecommerce-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - ./data/postgres:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    networks:
      - ecommerce-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER}"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: ecommerce-redis
    restart: unless-stopped
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - ./data/redis:/data
    ports:
      - "6379:6379"
    networks:
      - ecommerce-network
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

networks:
  ecommerce-network:
    driver: bridge
```

### 4.2. Tạo file `.env`

```bash
nano /opt/ecommerce/.env
```

**Nội dung file:**

```bash
# ======================
# DATABASE CONFIG
# ======================
DB_HOST=postgres
DB_PORT=5432
DB_NAME=ecommerce
DB_USER=admin
DB_PASSWORD=your_strong_password_here_123456

# ======================
# REDIS CONFIG
# ======================
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password_here_123456

# ======================
# APPLICATION CONFIG
# ======================
NODE_ENV=production
PORT=3000
APP_URL=https://yourdomain.com

# ======================
# JWT CONFIG
# ======================
JWT_SECRET=your_jwt_secret_super_secure_key_here
JWT_EXPIRES_IN=7d
JWT_REFRESH_SECRET=your_refresh_token_secret_here
JWT_REFRESH_EXPIRES_IN=30d

# ======================
# EMAIL CONFIG (nếu có)
# ======================
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASS=your-app-password

# ======================
# UPLOAD CONFIG
# ======================
MAX_FILE_SIZE=10485760
UPLOAD_PATH=/app/uploads

# ======================
# OTHER CONFIGS
# ======================
API_PREFIX=/api/v1
RATE_LIMIT_TTL=60
RATE_LIMIT_MAX=100
```

**⚠️ LƯU Ý:** Nhớ thay đổi tất cả password và secret bằng giá trị thật!

---

## 5️⃣ CẤU HÌNH NGINX

### 5.1. Tạo file config cho domain

```bash
nano /etc/nginx/sites-available/ecommerce
```

**Nội dung file:**

```nginx
# HTTP Server - Redirect to HTTPS
server {
    listen 80;
    listen [::]:80;
    server_name yourdomain.com www.yourdomain.com;

    # Certbot validation
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }

    # Redirect all HTTP to HTTPS
    location / {
        return 301 https://$server_name$request_uri;
    }
}

# HTTPS Server
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;

    # SSL Configuration (sẽ được Certbot tự động thêm)
    # ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    # ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    # Security Headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    # Giới hạn upload size
    client_max_body_size 50M;
    client_body_buffer_size 128k;

    # Timeout settings
    proxy_connect_timeout 600;
    proxy_send_timeout 600;
    proxy_read_timeout 600;
    send_timeout 600;

    # Proxy to Node.js app
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        
        # WebSocket support
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        
        # Headers
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $server_name;
        
        proxy_cache_bypass $http_upgrade;
        proxy_buffering off;
    }

    # Health check endpoint
    location /health {
        proxy_pass http://localhost:3000/health;
        access_log off;
        proxy_set_header Host $host;
    }

    # Static files caching (nếu có serve static files)
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf|eot)$ {
        proxy_pass http://localhost:3000;
        expires 1y;
        add_header Cache-Control "public, immutable";
        access_log off;
    }

    # Logs
    access_log /var/log/nginx/ecommerce-access.log;
    error_log /var/log/nginx/ecommerce-error.log;
}
```

### 5.2. Enable site và test config

```bash
# Tạo symlink
ln -s /etc/nginx/sites-available/ecommerce /etc/nginx/sites-enabled/

# Xóa default site (tùy chọn)
rm /etc/nginx/sites-enabled/default

# Test config
nginx -t

# Reload Nginx
systemctl reload nginx
```

---

## 6️⃣ CÀI ĐẶT SSL

### 6.1. Lấy SSL certificate từ Let's Encrypt

```bash
certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

**Trong quá trình cài đặt:**
- Nhập email của bạn
- Đồng ý Terms of Service: `Y`
- Share email with EFF (tùy chọn): `N` hoặc `Y`
- Chọn redirect HTTP to HTTPS: `2` (khuyến nghị)

### 6.2. Test auto-renewal

```bash
certbot renew --dry-run
```

### 6.3. Kiểm tra SSL

```bash
# Xem thông tin certificate
certbot certificates
```

---

## 7️⃣ CHẠY ỨNG DỤNG

### 7.1. Pull images từ Docker Hub

```bash
cd /opt/ecommerce

# Pull tất cả images
docker compose pull
```

### 7.2. Start ứng dụng

```bash
# Start tất cả services (chạy background)
docker compose up -d

# Hoặc xem logs realtime khi start
docker compose up
```

### 7.3. Kiểm tra containers

```bash
# Xem danh sách containers đang chạy
docker ps

# Xem logs
docker compose logs -f

# Xem logs của từng service
docker compose logs -f app
docker compose logs -f postgres
docker compose logs -f redis
```

### 7.4. Test ứng dụng

```bash
# Test local
curl http://localhost:3000/health

# Test qua domain
curl https://yourdomain.com/health

# Test với browser
# Mở: https://yourdomain.com
```

---

## 8️⃣ QUY TRÌNH UPDATE CODE

### 🔧 Trên Local Machine / CI/CD

```bash
# 1. Build image mới với tag cụ thể
docker build -t nguyenduoc/datn-be:20260130-01 .

# 2. Tag thêm latest
docker tag nguyenduoc/datn-be:20260130-01 nguyenduoc/datn-be:latest

# 3. Login Docker Hub (nếu chưa login)
docker login

# 4. Push lên Docker Hub
docker push nguyenduoc/datn-be:20260130-01
docker push nguyenduoc/datn-be:latest
```

### 🚀 Trên VPS

```bash
# 1. Vào thư mục app
cd /opt/ecommerce

# 2. Pull image mới nhất
docker compose pull

# 3. Restart app (zero-downtime nếu có health check)
docker compose up -d

# 4. Xem logs để kiểm tra
docker compose logs -f app

# 5. Kiểm tra app hoạt động
curl https://yourdomain.com/health
```

### 🔄 Script tự động update (tùy chọn)

Tạo file `/opt/ecommerce/update.sh`:

```bash
nano /opt/ecommerce/update.sh
```

```bash
#!/bin/bash

echo "🔄 Starting update process..."

cd /opt/ecommerce

echo "📥 Pulling latest images..."
docker compose pull

echo "🔄 Restarting services..."
docker compose up -d

echo "🧹 Cleaning old images..."
docker image prune -f

echo "✅ Update completed!"
echo "📊 Current status:"
docker ps

echo ""
echo "📝 Recent logs:"
docker compose logs --tail=50 app
```

Chmod và chạy:

```bash
chmod +x /opt/ecommerce/update.sh
./update.sh
```

---

## 9️⃣ CÁC LỆNH HỮU ÍCH

### 📊 Quản lý Containers

```bash
# Xem logs realtime
docker compose logs -f

# Xem logs của service cụ thể
docker compose logs -f app
docker compose logs -f postgres

# Xem logs 100 dòng cuối
docker compose logs --tail=100 app

# Restart service cụ thể
docker compose restart app

# Stop tất cả services
docker compose stop

# Start lại tất cả
docker compose start

# Stop và xóa containers (giữ volumes)
docker compose down

# Stop, xóa containers VÀ volumes (⚠️ MẤT DATA!)
docker compose down -v
```

### 🔍 Debug & Inspect

```bash
# Vào trong container
docker exec -it ecommerce-app sh

# Chạy lệnh trong container
docker exec ecommerce-app ls -la

# Xem resource usage
docker stats

# Xem thông tin chi tiết container
docker inspect ecommerce-app
```

### 💾 Backup & Restore Database

```bash
# Backup PostgreSQL
docker exec ecommerce-postgres pg_dump -U admin ecommerce > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore PostgreSQL
docker exec -i ecommerce-postgres psql -U admin ecommerce < backup_20260130_120000.sql

# Backup Redis
docker exec ecommerce-redis redis-cli --rdb /data/dump.rdb SAVE
cp data/redis/dump.rdb backup_redis_$(date +%Y%m%d_%H%M%S).rdb
```

### 🧹 Dọn dẹp

```bash
# Xóa images không dùng
docker image prune -a -f

# Xóa volumes không dùng
docker volume prune -f

# Xóa containers đã stop
docker container prune -f

# Xóa tất cả (images, containers, volumes không dùng)
docker system prune -a -f --volumes
```

### 🔒 Firewall (UFW)

```bash
# Cài UFW
apt install -y ufw

# Cho phép SSH (QUAN TRỌNG - làm trước khi enable!)
ufw allow 22/tcp

# Cho phép HTTP và HTTPS
ufw allow 80/tcp
ufw allow 443/tcp

# Enable firewall
ufw enable

# Kiểm tra status
ufw status

# Xem rules chi tiết
ufw status numbered

# Xóa rule (ví dụ rule số 3)
ufw delete 3
```

---

## 🔟 TROUBLESHOOTING

### ❌ Container không start được

```bash
# Xem logs chi tiết
docker compose logs app

# Kiểm tra config
docker compose config

# Restart lại
docker compose restart app
```

### ❌ Không connect được database

```bash
# Kiểm tra postgres có chạy không
docker ps | grep postgres

# Xem logs postgres
docker compose logs postgres

# Test connect từ app container
docker exec -it ecommerce-app sh
# Trong container:
nc -zv postgres 5432
```

### ❌ Nginx 502 Bad Gateway

```bash
# Kiểm tra app có chạy không
docker ps | grep ecommerce-app

# Kiểm tra port 3000
netstat -tlnp | grep 3000

# Test direct
curl http://localhost:3000/health

# Xem nginx error log
tail -f /var/log/nginx/ecommerce-error.log

# Restart nginx
systemctl restart nginx
```

### ❌ SSL certificate hết hạn

```bash
# Check certificate
certbot certificates

# Renew manually
certbot renew

# Renew và restart nginx
certbot renew --nginx
```

### ❌ Disk đầy

```bash
# Kiểm tra disk usage
df -h

# Xem thư mục lớn
du -sh /opt/ecommerce/*
du -sh /var/lib/docker/*

# Dọn dẹp Docker
docker system prune -a -f --volumes

# Dọn dẹp logs
truncate -s 0 /opt/ecommerce/logs/*.log
```

### ❌ High memory usage

```bash
# Xem resource usage
docker stats

# Limit memory trong docker-compose.yml
# Thêm vào service app:
#   deploy:
#     resources:
#       limits:
#         memory: 512M
```

---

## 📋 CHECKLIST HOÀN CHỈNH

### Setup lần đầu
- [ ] VPS đã update: `apt update && apt upgrade -y`
- [ ] Docker đã cài: `docker --version`
- [ ] Docker Compose đã cài: `docker compose version`
- [ ] Nginx đã cài và chạy: `systemctl status nginx`
- [ ] Certbot đã cài: `certbot --version`
- [ ] Thư mục `/opt/ecommerce` đã tạo
- [ ] File `docker-compose.yml` đã tạo và cấu hình
- [ ] File `.env` đã tạo và điền đầy đủ thông tin
- [ ] Nginx config đã tạo: `/etc/nginx/sites-available/ecommerce`
- [ ] Nginx config đã enable: symlink vào `sites-enabled`
- [ ] Nginx test OK: `nginx -t`
- [ ] SSL certificate đã cài: `certbot certificates`
- [ ] Domain đã trỏ đúng IP VPS
- [ ] Firewall đã cấu hình (UFW)
- [ ] Containers đã chạy: `docker ps`
- [ ] App response OK: `curl https://yourdomain.com/health`

### Khi update code
- [ ] Build image mới trên local
- [ ] Push image lên Docker Hub
- [ ] SSH vào VPS
- [ ] Pull image mới: `docker compose pull`
- [ ] Restart: `docker compose up -d`
- [ ] Kiểm tra logs: `docker compose logs -f app`
- [ ] Test app: `curl https://yourdomain.com/health`

---

## 🎯 KẾT LUẬN

Bạn đã hoàn thành setup VPS production-ready! 🎉

**Các bước tiếp theo:**
1. Setup monitoring (Prometheus + Grafana)
2. Setup auto-backup database
3. Setup CI/CD tự động deploy
4. Cấu hình CDN cho static files
5. Setup staging environment

**Tài liệu tham khảo:**
- Docker: https://docs.docker.com
- Nginx: https://nginx.org/en/docs/
- Let's Encrypt: https://letsencrypt.org/docs/
- PostgreSQL: https://www.postgresql.org/docs/

---

**📞 Liên hệ support:**
- GitHub Issues: [link repo]
- Email: your-email@example.com

**📅 Cập nhật lần cuối:** 30/01/2026

---

**Made with ❤️ by Your Team**