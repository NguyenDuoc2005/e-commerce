# 🚀 QUY TRÌNH DEPLOY JAVA (GRADLE) + MYSQL + DOCKER + NGINX

> Tài liệu hướng dẫn triển khai ứng dụng Java Spring Boot production-ready với Docker, MySQL và Nginx

---

## 📋 MỤC LỤC

- [I. Kiến Trúc Hệ Thống](#i-kiến-trúc-hệ-thống)
- [II. Cấu Trúc Thư Mục](#ii-cấu-trúc-thư-mục)
- [III. Chuẩn Bị Trên Máy Local](#iii-chuẩn-bị-trên-máy-local)
- [IV. Cài Đặt VPS](#iv-cài-đặt-vps)
- [V. File Cấu Hình](#v-file-cấu-hình)
- [VI. Deploy Backend](#vi-deploy-backend)
- [VII. Cập Nhật Version Mới](#vii-cập-nhật-version-mới)
- [VIII. Cấu Hình Nginx](#viii-cấu-hình-nginx)
- [IX. Troubleshooting](#ix-troubleshooting)

---

## I. KIẾN TRÚC HỆ THỐNG

```
┌─────────────────┐
│     Client      │
└────────┬────────┘
         │
    ┌────▼────┐
    │  Nginx  │ Port 80/443
    │ (Proxy) │
    └────┬────┘
         │
    ┌────▼──────────┐
    │ Docker Backend│ Port 8386
    │  (Spring Boot)│
    └────┬──────────┘
         │
    ┌────▼─────────┐
    │Docker MySQL  │ Port 3306
    │  (Database)  │
    └──────────────┘
```

**Flow request:**
```
Client → Nginx (80/443) → Docker Backend (8386) → Docker MySQL (3306)
```

---

## II. CẤU TRÚC THƯ MỤC

### Trên VPS
```
/opt/ecommerce/
├── app.jar                 # File JAR build từ local
├── Dockerfile              # Docker image definition
├── docker-compose.yml      # Container orchestration
└── .env                    # Environment variables (KHÔNG commit lên Git)
```

> ⚠️ **Lưu ý:** Không cần copy toàn bộ source code lên server, chỉ cần file JAR và các file Docker

---

## III. CHUẨN BỊ TRÊN MÁY LOCAL

### Bước 1: Build JAR file
```bash
# Clean và build project (bỏ qua test để nhanh hơn)
./gradlew clean build -x test
```

### Bước 2: Đổi tên file JAR
```bash
# Tìm file JAR trong thư mục build/libs và đổi tên
cp build/libs/*.jar app.jar
```

### Bước 3: Upload lên VPS
```bash
# Thay SERVER_IP bằng IP thực tế của VPS
scp app.jar root@SERVER_IP:/opt/ecommerce/app.jar
scp Dockerfile docker-compose.yml .env root@SERVER_IP:/opt/ecommerce/
```

> 💡 **Tip:** Tạo alias trong `~/.bashrc` để deploy nhanh hơn:
> ```bash
> alias deploy="./gradlew clean build -x test && cp build/libs/*.jar app.jar && scp app.jar root@SERVER_IP:/opt/ecommerce/"
> ```

---

## IV. CÀI ĐẶT VPS

> ⚠️ **Chỉ cần thực hiện 1 lần duy nhất khi setup VPS mới**

### Bước 1: Cài đặt Docker
```bash
curl -fsSL https://get.docker.com | sh
```

### Bước 2: Cài Docker Compose Plugin
```bash
apt update
apt install -y docker-compose-plugin
```

### Bước 3: Kiểm tra cài đặt
```bash
docker --version
# Output: Docker version 24.x.x, build xxxxx

docker compose version
# Output: Docker Compose version v2.x.x
```

### Bước 4: Tạo thư mục project
```bash
mkdir -p /opt/ecommerce
cd /opt/ecommerce
```

---

## V. FILE CẤU HÌNH

### 1️⃣ Dockerfile
```dockerfile
# Sử dụng OpenJDK 17 slim để giảm kích thước image
FROM openjdk:17-slim

# Metadata
LABEL maintainer="your-email@example.com"
LABEL version="1.0"

# Tạo thư mục làm việc
WORKDIR /app

# Copy file JAR vào container
COPY app.jar app.jar

# Expose port
EXPOSE 8386

# Health check (tùy chọn nhưng nên có)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8386/actuator/health || exit 1

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2️⃣ .env
```env
# MySQL Configuration
MYSQL_ROOT_PASSWORD=your_strong_password_here
MYSQL_DATABASE=ecommerce_db
MYSQL_USER=ecommerce_user
MYSQL_PASSWORD=ecommerce_pass

# Application Configuration
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8386

# Timezone
TZ=Asia/Ho_Chi_Minh
```

> 🔒 **Bảo mật:** Đừng commit file `.env` lên Git! Thêm vào `.gitignore`

### 3️⃣ docker-compose.yml
```yaml
version: '3.8'

services:
  # MySQL Database
  mysql:
    image: mysql:8.0
    container_name: ecommerce-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      TZ: ${TZ}
    volumes:
      - mysql_data:/var/lib/mysql
    ports:
      - "127.0.0.1:3306:3306"  # Chỉ cho phép truy cập từ localhost
    networks:
      - ecommerce-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Spring Boot Backend
  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: ecommerce-be
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE}
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
      TZ: ${TZ}
    ports:
      - "127.0.0.1:8386:8386"  # Bind vào localhost (bảo mật hơn)
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - ecommerce-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8386/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

networks:
  ecommerce-network:
    driver: bridge

volumes:
  mysql_data:
    driver: local
```

---

## VI. DEPLOY BACKEND

### Deploy lần đầu
```bash
cd /opt/ecommerce

# Build và khởi động containers
docker compose up -d --build
```

### Kiểm tra trạng thái
```bash
# Xem danh sách containers đang chạy
docker ps

# Xem logs backend
docker logs -f ecommerce-be

# Xem logs MySQL
docker logs -f ecommerce-mysql
```

### Test kết nối
```bash
# Test từ VPS
curl http://localhost:8386

# Hoặc test endpoint cụ thể
curl http://localhost:8386/api/health
```

---

## VII. CẬP NHẬT VERSION MỚI

> 🔄 **Quy trình này sẽ được thực hiện mỗi khi có code mới**

### Trên máy Local
```bash
# 1. Build JAR mới
./gradlew clean build -x test

# 2. Đổi tên
cp build/libs/*.jar app.jar

# 3. Upload lên VPS
scp app.jar root@SERVER_IP:/opt/ecommerce/app.jar
```

### Trên VPS
```bash
cd /opt/ecommerce

# Rebuild và restart chỉ backend container
docker compose up -d --build backend

# Hoặc rebuild toàn bộ (nếu có thay đổi docker-compose.yml)
docker compose up -d --build
```

### Xác nhận update thành công
```bash
# Xem logs để check version mới
docker logs -f ecommerce-be

# Test API
curl http://localhost:8386/api/version
```

---

## VIII. CẤU HÌNH NGINX

> 🎯 **Mục tiêu:** Cho phép truy cập qua domain/IP mà không cần port 8386

### Bước 1: Cài đặt Nginx
```bash
apt update
apt install -y nginx

# Enable và start Nginx
systemctl enable nginx
systemctl start nginx
systemctl status nginx
```

### Bước 2: Tạo cấu hình Nginx
```bash
# Tạo file cấu hình mới
nano /etc/nginx/sites-available/ecommerce
```

**Nội dung file cấu hình:**

```nginx
# /etc/nginx/sites-available/ecommerce

# Upstream backend
upstream backend {
    server 127.0.0.1:8386;
    keepalive 32;
}

server {
    listen 80;
    listen [::]:80;
    
    # Thay YOUR_DOMAIN bằng domain thực tế hoặc IP
    server_name YOUR_DOMAIN www.YOUR_DOMAIN;
    
    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # Client body size (cho upload file)
    client_max_body_size 50M;
    
    # Logging
    access_log /var/log/nginx/ecommerce-access.log;
    error_log /var/log/nginx/ecommerce-error.log;
    
    # Root location
    location / {
        proxy_pass http://backend;
        
        # Proxy headers
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # WebSocket support (nếu cần)
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
    
    # Health check endpoint
    location /health {
        access_log off;
        proxy_pass http://backend/actuator/health;
    }
    
    # Static files cache (nếu có)
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf|eot)$ {
        proxy_pass http://backend;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### Bước 3: Kích hoạt cấu hình
```bash
# Tạo symbolic link
ln -s /etc/nginx/sites-available/ecommerce /etc/nginx/sites-enabled/

# Xóa cấu hình default (tùy chọn)
rm /etc/nginx/sites-enabled/default

# Test cấu hình Nginx
nginx -t

# Reload Nginx
systemctl reload nginx
```

### Bước 4: Test truy cập
```bash
# Test từ VPS
curl http://localhost

# Test từ máy khác
curl http://YOUR_SERVER_IP
```

---

## 🔒 CẤU HÌNH HTTPS VỚI LET'S ENCRYPT (Tùy chọn)

### Cài đặt Certbot
```bash
apt install -y certbot python3-certbot-nginx
```

### Lấy SSL certificate
```bash
# Thay YOUR_DOMAIN bằng domain thực tế
certbot --nginx -d YOUR_DOMAIN -d www.YOUR_DOMAIN
```

### Auto-renewal
```bash
# Test renewal
certbot renew --dry-run

# Certbot sẽ tự động setup cronjob để renew
```

**File cấu hình Nginx sau khi có SSL:**
```nginx
# HTTP - Redirect to HTTPS
server {
    listen 80;
    listen [::]:80;
    server_name YOUR_DOMAIN www.YOUR_DOMAIN;
    return 301 https://$server_name$request_uri;
}

# HTTPS
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    
    server_name YOUR_DOMAIN www.YOUR_DOMAIN;
    
    # SSL Configuration
    ssl_certificate /etc/letsencrypt/live/YOUR_DOMAIN/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/YOUR_DOMAIN/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    
    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # Logging
    access_log /var/log/nginx/ecommerce-access.log;
    error_log /var/log/nginx/ecommerce-error.log;
    
    # Proxy to backend
    location / {
        proxy_pass http://127.0.0.1:8386;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

---

## IX. TROUBLESHOOTING

### 🔍 Container không start
```bash
# Xem logs chi tiết
docker compose logs backend
docker compose logs mysql

# Kiểm tra port đã được sử dụng chưa
netstat -tulpn | grep 8386
netstat -tulpn | grep 3306

# Restart container
docker compose restart backend
```

### 🔍 Không kết nối được database
```bash
# Vào container backend
docker exec -it ecommerce-be bash

# Test kết nối MySQL
apt update && apt install -y mysql-client
mysql -h mysql -u ecommerce_user -p

# Kiểm tra network
docker network ls
docker network inspect ecommerce_ecommerce-network
```

### 🔍 Nginx lỗi 502 Bad Gateway
```bash
# Kiểm tra backend có chạy không
curl http://127.0.0.1:8386

# Xem logs Nginx
tail -f /var/log/nginx/ecommerce-error.log

# Test cấu hình Nginx
nginx -t

# Restart Nginx
systemctl restart nginx
```

### 🔍 Xóa và setup lại từ đầu
```bash
# Dừng và xóa containers
cd /opt/ecommerce
docker compose down -v

# Xóa images
docker rmi ecommerce-backend

# Build lại
docker compose up -d --build
```

---

## 📝 CHECKLIST DEPLOY

- [ ] Build JAR thành công trên local
- [ ] Upload JAR và Docker files lên VPS
- [ ] Docker và Docker Compose đã cài đặt
- [ ] File `.env` đã cấu hình đầy đủ
- [ ] Containers đã chạy thành công (`docker ps`)
- [ ] Backend logs không có lỗi
- [ ] MySQL connection thành công
- [ ] Test API qua curl thành công
- [ ] Nginx đã cài và cấu hình
- [ ] Domain đã trỏ về VPS (nếu có)
- [ ] SSL certificate đã setup (nếu có)

---

## 📚 TÀI LIỆU THAM KHẢO

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Nginx Documentation](https://nginx.org/en/docs/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Let's Encrypt](https://letsencrypt.org/)

---

## 📧 HỖ TRỢ

Nếu gặp vấn đề, vui lòng:
1. Kiểm tra phần [Troubleshooting](#ix-troubleshooting)
2. Xem logs: `docker logs -f ecommerce-be`
3. Liên hệ team qua Slack/Email

---

**Version:** 1.0  
**Last Updated:** January 2026  
**Maintainer:** Your Team Name