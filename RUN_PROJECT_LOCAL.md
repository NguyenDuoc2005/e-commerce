# Huong dan chay du an local

Tai lieu nay danh cho nguoi moi pull code ve va muon chay full du an tren may local.

Backend can chay bang source microservice trong `backend-microservice`. Thu muc `BE` chi la monolith tham chieu, khong dung de chay flow hien tai.

## 1. Yeu cau cai san

- Windows PowerShell.
- Docker Desktop.
- Java JDK 17.
- Node.js + npm.
- Port can trong:
  - `3307`: MySQL Docker.
  - `8761`: Eureka Discovery.
  - `8080`: API Gateway.
  - `6688`: Frontend Vite.
  - `9092`: Kafka.
  - `8090`: Kafka UI.
  - `9200`: Elasticsearch.
  - `5601`: Kibana.
  - `9090`: Prometheus.
  - `3000`: Grafana.

Tat ca lenh ben duoi chay tu PowerShell.

## 2. Clone va vao project

```powershell
cd "C:\My Project"
git clone <repo-url> e-commerce
cd "C:\My Project\e-commerce"
```

Neu repo da co san thi chi can:

```powershell
cd "C:\My Project\e-commerce"
git pull
```

## 3. Chay ha tang bang Docker

Lenh nay chay MySQL, Kafka, Kafka UI, Elasticsearch, Kibana, Kafka Connect, Filebeat, Logstash, Prometheus, Grafana va cac exporter.

```powershell
cd "C:\My Project\e-commerce"
docker compose -f backend-microservice\docker-compose.yml up -d mysql kafka kafka-ui elasticsearch kibana kafka-connect logstash filebeat prometheus grafana node-exporter cadvisor
```

Kiem tra container:

```powershell
docker compose -f backend-microservice\docker-compose.yml ps
```

Mot so man hinh phu tro:

- Kafka UI: `http://localhost:8090`
- Elasticsearch: `http://localhost:9200`
- Kibana: `http://localhost:5601`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

Tai khoan monitoring:

- Grafana: `http://localhost:3000`
  - Username mac dinh: `admin`
  - Password mac dinh: `admin123`
  - Co the doi bang bien moi truong `GRAFANA_ADMIN_USER` va `GRAFANA_ADMIN_PASSWORD` truoc khi chay Docker compose.
- Prometheus: `http://localhost:9090`
  - Khong co tai khoan dang nhap mac dinh trong cau hinh local hien tai.
  - Prometheus chi dung de xem metrics/query truc tiep; Grafana da duoc provision datasource Prometheus san.

## 4. Tao database va seed demo data

Dung MySQL Docker publish ra host port `3307`.

Neu chi muon tao database rong:

```powershell
cd "C:\My Project\e-commerce"
powershell -ExecutionPolicy Bypass -File backend-microservice\init-databases.ps1 -UseDocker
```

Neu muon reset sach va nap data demo:

```powershell
cd "C:\My Project\e-commerce"
powershell -ExecutionPolicy Bypass -File backend-microservice\reset-demo-databases.ps1 -UseDocker -Force
```

Tai khoan demo sau khi seed:

- Admin: `admin@ecommerce.local` / `Admin@123`
- Staff: `staff@ecommerce.local` / `Admin@123`
- Customer: `customer1@ecommerce.local` / `Admin@123`

## 5. Chay backend microservice

Build kiem tra truoc:

```powershell
cd "C:\My Project\e-commerce\backend-microservice"
.\gradlew.bat clean build --no-daemon
```

Chay tat ca service backend bang runner local:

```powershell
cd "C:\My Project\e-commerce"
powershell -ExecutionPolicy Bypass -File backend-microservice\run-all.ps1 -DbPort 3307
```

Runner se start cac service:

- `discovery-server`: `http://localhost:8761`
- `api-gateway`: `http://localhost:8080`
- `auth-service`
- `user-service`
- `catalog-service`
- `promotion-service`
- `cart-service`
- `order-service`

Log nam trong:

```powershell
C:\My Project\e-commerce\backend-microservice\logs
```

Kiem tra Eureka:

```powershell
Invoke-RestMethod http://localhost:8761/eureka/apps
```

Kiem tra login admin qua gateway:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/auth/login-admin" -ContentType "application/json" -Body '{"email":"admin@ecommerce.local","password":"Admin@123"}'
```

Neu can chay them `notification-service`:

```powershell
cd "C:\My Project\e-commerce"
powershell -ExecutionPolicy Bypass -File backend-microservice\run-all.ps1 -DbPort 3307 -WithNotification
```

## 6. Chay frontend

File FE `.env` hien dang tro backend ve gateway:

```text
VITE_BASE_URL_SERVER=http://localhost:8080
VITE_BASE_URL_CLIENT=http://localhost:6688
VITE_BASE_URL_CLIENT_SOCKET=localhost:8080
```

Chay FE:

```powershell
cd "C:\My Project\e-commerce\FE"
npm install
npm run dev
```

Mo trinh duyet:

```text
http://localhost:6688
```

## 7. Search pipeline Elasticsearch

Phan search sync dung Outbox Pattern + Debezium + Kafka Connect + Elasticsearch.

Sau khi Docker infrastructure da chay, tao outbox table va replication user:

```powershell
cd "C:\My Project\e-commerce"
Get-Content -Raw backend-microservice\search-pipeline\sql\001-create-outbox.sql | docker compose -f backend-microservice\docker-compose.yml exec -T mysql mysql -uroot -p12345678 ecommerce_catalog
Get-Content -Raw backend-microservice\search-pipeline\sql\002-create-debezium-user-mysql.sql | docker compose -f backend-microservice\docker-compose.yml exec -T mysql mysql -uroot -p12345678
```

Deploy Kafka Connect connectors:

```powershell
cd "C:\My Project\e-commerce"
powershell -ExecutionPolicy Bypass -File backend-microservice\search-pipeline\scripts\deploy-connectors.ps1
```

Kiem tra connector:

```powershell
Invoke-RestMethod http://localhost:8084/connectors/catalog-products-outbox-source/status
Invoke-RestMethod http://localhost:8084/connectors/products-elasticsearch-sink/status
```

Kiem tra Elasticsearch:

```powershell
Invoke-RestMethod "http://localhost:9200/products/_search?q=Adidas"
```

## 8. Cach dung Docker compose build full backend

Neu muon build va chay backend microservice trong Docker thay vi `run-all.ps1`:

```powershell
cd "C:\My Project\e-commerce"
docker compose -f backend-microservice\docker-compose.yml up -d --build
```

Cach nay se build tat ca service trong compose. Khi dev local, cach de debug de hon la:

1. Docker chi chay ha tang o buoc 3.
2. Backend chay bang `backend-microservice\run-all.ps1`.
3. FE chay bang `npm run dev`.

## 9. Dung du an

Dung backend local runner:

```powershell
cd "C:\My Project\e-commerce"
powershell -ExecutionPolicy Bypass -File backend-microservice\stop-all.ps1
```

Dung Docker infrastructure:

```powershell
cd "C:\My Project\e-commerce"
docker compose -f backend-microservice\docker-compose.yml down
```

Neu muon xoa luon volume MySQL/Kafka/Elasticsearch/Prometheus/Grafana:

```powershell
cd "C:\My Project\e-commerce"
docker compose -f backend-microservice\docker-compose.yml down -v
```

Lenh `down -v` se xoa data trong Docker volume, chi dung khi muon reset sach moi thu.

## 10. Loi hay gap

### FE bao `ERR_CONNECTION_REFUSED`

Kiem tra frontend va gateway co dang chay dung port khong:

```powershell
Get-NetTCPConnection -LocalPort 6688,8080 -State Listen
```

### Gateway tra 503

Kiem tra Eureka co du service dang `UP`:

```powershell
Invoke-RestMethod http://localhost:8761/eureka/apps
```

Vi du route `/api/v1/admin/mau-sac` can `CATALOG-SERVICE` dang `UP`.

### Service backend fail khi start

Doc log theo service:

```powershell
Get-Content -Tail 120 backend-microservice\logs\catalog-service.out.log
Get-Content -Tail 120 backend-microservice\logs\api-gateway.out.log
```

Neu gap loi `common-lib-0.0.1-SNAPSHOT.jar` bi lock, stop backend roi build lai:

```powershell
cd "C:\My Project\e-commerce"
powershell -ExecutionPolicy Bypass -File backend-microservice\stop-all.ps1
cd "C:\My Project\e-commerce\backend-microservice"
.\gradlew.bat :common-lib:jar --no-daemon --max-workers=1
```

### Muon reset data demo lai tu dau

```powershell
cd "C:\My Project\e-commerce"
powershell -ExecutionPolicy Bypass -File backend-microservice\stop-all.ps1
powershell -ExecutionPolicy Bypass -File backend-microservice\reset-demo-databases.ps1 -UseDocker -Force
powershell -ExecutionPolicy Bypass -File backend-microservice\run-all.ps1 -DbPort 3307
```

## 11. Thu tu chay nhanh

Ban copy lan luot cac block nay neu moi pull code ve:

```powershell
cd "C:\My Project\e-commerce"
docker compose -f backend-microservice\docker-compose.yml up -d mysql kafka kafka-ui elasticsearch kibana kafka-connect logstash filebeat prometheus grafana node-exporter cadvisor
powershell -ExecutionPolicy Bypass -File backend-microservice\reset-demo-databases.ps1 -UseDocker -Force
```

```powershell
cd "C:\My Project\e-commerce\backend-microservice"
.\gradlew.bat clean build --no-daemon
```

```powershell
cd "C:\My Project\e-commerce"
powershell -ExecutionPolicy Bypass -File backend-microservice\run-all.ps1 -DbPort 3307
```

```powershell
cd "C:\My Project\e-commerce\FE"
npm install
npm run dev
```

Sau do mo:

- FE: `http://localhost:6688`
- Gateway: `http://localhost:8080`
- Eureka: `http://localhost:8761`
- Prometheus: `http://localhost:9090` khong can dang nhap
- Grafana: `http://localhost:3000` voi `admin` / `admin123`
- Kibana: `http://localhost:5601`
- Kafka UI: `http://localhost:8090`
