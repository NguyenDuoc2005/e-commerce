# Product Search Sync Pipeline

Pipeline nay dong bo product search theo Outbox Pattern + Debezium CDC:

```text
catalog-service transaction
  -> save san_pham / san_pham_chi_tiet
  -> insert outbox
  -> MySQL binlog ROW
  -> Debezium MySQL connector + Outbox Event Router
  -> Kafka topic outbox.event.Product
  -> Elasticsearch Sink connector
  -> Elasticsearch alias/index products
```

## Files

- `sql/001-create-outbox.sql`: migration tao bang `outbox`.
- `sql/002-create-debezium-user-mysql.sql`: MySQL replication user cho Debezium.
- `connectors/debezium-outbox-products.json`: Debezium source connector, chi doc `ecommerce_catalog.outbox`.
- `connectors/elasticsearch-products-sink.json`: Kafka to Elasticsearch sink connector; dung Kafka key lam ES `_id`.
- `elasticsearch/products-index-mapping.json`: explicit mapping cho index `products_v1`.
- `scripts/deploy-connectors.ps1`: tao ES index/alias va deploy 2 connector qua Kafka Connect REST API.
- `scripts/cleanup-outbox.ps1`: xoa outbox record cu.

## Start local infrastructure

Tu repo root:

```powershell
cd "C:\My Project\e-commerce"
docker compose -f backend-microservice\docker-compose.yml up -d mysql kafka elasticsearch kibana kafka-connect
```

Kafka trong compose nay chay KRaft, khong co Zookeeper.

## Enable MySQL CDC

Compose MySQL da bat:

- `--server-id=1001`
- `--log-bin=mysql-bin`
- `--binlog-format=ROW`
- `--binlog-row-image=FULL`

Tao schema/table va replication user:

```powershell
cd "C:\My Project\e-commerce"
docker compose -f backend-microservice\docker-compose.yml exec -T mysql mysql -uroot -p12345678 ecommerce_catalog < backend-microservice\search-pipeline\sql\001-create-outbox.sql
docker compose -f backend-microservice\docker-compose.yml exec -T mysql mysql -uroot -p12345678 < backend-microservice\search-pipeline\sql\002-create-debezium-user-mysql.sql
```

Neu PowerShell local khong ho tro input redirect cho `docker compose exec`, dung pipe:

```powershell
Get-Content -Raw backend-microservice\search-pipeline\sql\001-create-outbox.sql | docker compose -f backend-microservice\docker-compose.yml exec -T mysql mysql -uroot -p12345678 ecommerce_catalog
Get-Content -Raw backend-microservice\search-pipeline\sql\002-create-debezium-user-mysql.sql | docker compose -f backend-microservice\docker-compose.yml exec -T mysql mysql -uroot -p12345678
```

## Deploy connectors

```powershell
cd "C:\My Project\e-commerce"
powershell -ExecutionPolicy Bypass -File backend-microservice\search-pipeline\scripts\deploy-connectors.ps1
```

Kiem tra status:

```powershell
Invoke-RestMethod http://localhost:8084/connectors/catalog-products-outbox-source/status
Invoke-RestMethod http://localhost:8084/connectors/products-elasticsearch-sink/status
```

## Test end-to-end

1. Chay backend local nhu binh thuong:

```powershell
cd "C:\My Project\e-commerce"
powershell -ExecutionPolicy Bypass -File backend-microservice\run-all.ps1 -DbPort 3307
```

2. Tao/sua product qua API admin hoac UI admin. `catalog-service` se ghi `san_pham`/`san_pham_chi_tiet` va `outbox` trong cung transaction.

3. Kiem tra outbox:

```powershell
docker compose -f backend-microservice\docker-compose.yml exec -T mysql mysql -uroot -p12345678 ecommerce_catalog -e "SELECT id, aggregate_type, aggregate_id, event_type, created_at FROM outbox ORDER BY created_at DESC LIMIT 5;"
```

4. Kiem tra Kafka topic:

```powershell
docker compose -f backend-microservice\docker-compose.yml exec -T kafka kafka-console-consumer --bootstrap-server kafka:29092 --topic outbox.event.Product --from-beginning --max-messages 5 --property print.key=true
```

5. Kiem tra Elasticsearch:

```powershell
Invoke-RestMethod "http://localhost:9200/products/_search?q=Adidas"
```

6. Kiem tra search API legacy ma FE dang dung:

```powershell
Invoke-RestMethod "http://localhost:8080/api/v1/permitall/san-pham/get-all/danh-sach-san-pham?page=1&size=12&q=adi"
```

## Delete behavior

Khi product khong con du dieu kien public search hoac bi inactive, `catalog-service` insert outbox event `ProductDeleted` voi `payload = null`.

Debezium Outbox Event Router lay `aggregate_id` lam Kafka key. Elasticsearch sink cau hinh:

- `key.ignore=false`
- `behavior.on.null.values=DELETE`
- Debezium `route.tombstone.on.empty.payload=true`

Nen tombstone/null value se xoa document co `_id = aggregate_id`. Connector Debezium dang `skipped.operations=u,d`, nen viec cleanup xoa row `outbox` cu khong tao delete event gia.

## Payload contract

`catalog-service` chi ghi cac field public vao outbox payload: `id`, `name`, `description`, `categoryId`, `category`, `price`, `brandId`, `brand`, `imageUrl`. Debezium cau hinh `table.expand.json.payload=true` de Kafka message value la JSON object dung cho Elasticsearch Sink, khong phai string JSON boc ngoai.

## Cleanup outbox

Outbox khong can giu vinh vien; giu du lau de trace/debug:

```powershell
cd "C:\My Project\e-commerce"
powershell -ExecutionPolicy Bypass -File backend-microservice\search-pipeline\scripts\cleanup-outbox.ps1 -RetentionDays 7
```

Nen dua len scheduler cua moi truong deploy. Khong dung cleanup job lam co che dong bo.

## Reindex / mapping changes

Khong sua mapping truc tiep tren index production. Tao index moi, vi du `products_v2`, reindex, sau do swap alias `products`.

```powershell
Invoke-RestMethod -Method Put -Uri "http://localhost:9200/products_v2" -ContentType "application/json" -Body (Get-Content -Raw backend-microservice\search-pipeline\elasticsearch\products-index-mapping.json)
Invoke-RestMethod -Method Post -Uri "http://localhost:9200/_reindex" -ContentType "application/json" -Body '{"source":{"index":"products_v1"},"dest":{"index":"products_v2"}}'
Invoke-RestMethod -Method Post -Uri "http://localhost:9200/_aliases" -ContentType "application/json" -Body '{"actions":[{"remove":{"index":"products_v1","alias":"products"}},{"add":{"index":"products_v2","alias":"products"}}]}'
```

Neu outbox da bi cleanup va can rebuild full index tu DB, can chay mot bootstrap co kiem soat de sinh lai ProductUpdated outbox event cho tung product public. Do khong phai co che sync chinh hang ngay.
