# Product Search Sync Pipeline

Pipeline nay dong bo product search theo Outbox Pattern + Debezium CDC:

```text
catalog-service transaction
  -> save product aggregate / product_variant
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
- `elasticsearch/products-index-mapping.json`: mapping Phase 1 cua `products_v2`, chi giu cho rollback.
- `elasticsearch/products-v3-index-mapping.json`: mapping Phase 3 voi analyzer tieng Viet khong phan biet dau, `search_as_you_type` va facet nested.
- `scripts/deploy-connectors.ps1`: tao ES index/alias va deploy 2 connector qua Kafka Connect REST API.
- `scripts/verify-products-index.ps1`: verify strict mapping va bon truy van seed bat buoc, gom ca chong cross-match variant.
- `scripts/reindex-products-v3.ps1`: rebuild tu MySQL/outbox, pause sink, verify va atomically swap alias sang `products_v3`.
- `scripts/verify-products-consistency.ps1`: doi chieu ID va core fields giua MySQL va Elasticsearch.
- `scripts/verify-pit-pagination.ps1`: chen document tam, thay doi live index giua 4 trang va verify PIT snapshot khong lap/thieu.
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

Neu local Elasticsearch con index concrete `products` tu flow cu, script se dung thay vi nuot loi alias. Sau khi xac nhan day chi la search index dan xuat va co the rebuild tu MySQL/outbox, migrate co kiem soat bang:

```powershell
powershell -ExecutionPolicy Bypass -File backend-microservice\search-pipeline\scripts\deploy-connectors.ps1 -MigrateLegacyProductsIndex
```

Script chi xoa index legacy sau khi `_reindex` khong co failure va count dich khong nho hon count nguon. Sau khi connectors `RUNNING`, Platform Admin goi `POST /api/v1/admin/product-attributes/reindex` de enqueue `ProductUpdated` cho toan bo product active qua outbox; catalog khong dual-write truc tiep sang Elasticsearch.

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

2. Tao/sua product aggregate qua Seller/Admin API. `catalog-service` ghi product/attribute/variant va `outbox` trong cung transaction.

3. Kiem tra outbox:

```powershell
docker compose -f backend-microservice\docker-compose.yml exec -T mysql mysql -uroot -p12345678 ecommerce_catalog -e "SELECT id, aggregate_type, aggregate_id, event_type, created_at FROM outbox ORDER BY created_at DESC LIMIT 5;"
```

4. Kiem tra Kafka topic:

```powershell
docker compose -f backend-microservice\docker-compose.yml exec -T kafka kafka-console-consumer --bootstrap-server kafka:29092 --topic outbox.event.Product --from-beginning --max-messages 5 --property print.key=true
```

5. Kiem tra Elasticsearch (alias `products` tro vao index version dang active):

```powershell
Invoke-RestMethod "http://localhost:9200/products/_search?q=Adidas"
```

## Delete behavior

Khi product khong con du dieu kien public search hoac bi inactive, `catalog-service` insert outbox event `ProductDeleted` voi `payload = null`.

Debezium Outbox Event Router lay `aggregate_id` lam Kafka key. Elasticsearch sink cau hinh:

- `key.converter=org.apache.kafka.connect.storage.StringConverter`
- `key.ignore=false`
- `write.method=INSERT`
- `behavior.on.null.values=DELETE`
- Debezium `route.tombstone.on.empty.payload=true`

Day la Confluent Elasticsearch Sink 15.1.3. Connector nay khong co config `delete.enabled`; config delete tombstone dung va duoc runtime ConfigDef cong nhan la `behavior.on.null.values=DELETE`. `key.converter=StringConverter` va `key.ignore=false` dam bao Kafka key `aggregate_id` tro thanh dung Elasticsearch document `_id`. Connector Debezium dang `skipped.operations=u,d`, nen viec cleanup xoa row `outbox` cu khong tao delete event gia.

Bug release-blocker da fix ngay 23/09/2026: config cu dung `write.method=UPSERT`. Voi topic moi, chuoi offset `0=ProductCreated`, `1=ProductUpdated`, `2=ProductDeleted` lam document co Elasticsearch internal version `2`, trong khi delete cua connector gui Kafka offset `2` lam external version. Elasticsearch tra version conflict; connector coi day la conflict co the bo qua nen consumer van lag `0`, khong co DLQ, nhung document khong bi xoa. `INSERT` la mode dung cho pipeline nay vi moi outbox payload la full document replacement, va giu versioning theo Kafka offset nhat quan giua index/update/delete. Khong doi lai `UPSERT` neu chua co integration test tombstone tren topic bat dau tu offset thap.

Quan trong: sink dang dung `write.method=INSERT` voi gia dinh moi outbox event luon chua **FULL document**. Neu sau nay outbox doi sang gui partial field, phai doi `write.method` tuong ung; khong duoc giu `INSERT` voi partial payload vi cac field khong co trong event se bi mat khoi document Elasticsearch.

## Payload contract

`catalog-service` chi ghi search document canonical vao outbox payload: thong tin product/category, `createdAt`, `attributes` nested va `variants` nested (moi variant giu `selections` cua chinh no). Khong con `brandId/brand`, color hay size top-level. Cac decimal (`ratingAverage`, `valueNumber`, `salePrice`) duoc serialize thanh chuoi thap phan on dinh; Elasticsearch mapping se coerce/index chung theo kieu so. Debezium cau hinh `table.expand.json.payload=true` de Kafka message value la JSON object dung cho Elasticsearch Sink, khong phai string JSON boc ngoai. Product legacy khong co `created_date` dung sentinel `createdAt=0`; product moi dung epoch millis that.

## Known limitation: Seller Status Sync

San pham cua seller bi suspend hien van co the xuat hien trong ket qua search cho den khi `seller-service` publish event `SellerStatusChanged` va `catalog-service` consume de dong bo. Day la rui ro da biet, duoc ghi nhan la epic rieng `Seller Status Sync`, chua co timeline cu the. Root cause: `seller-service` hien chi cap nhat status trong DB noi bo `ecommerce_seller` va ghi `seller_status_history`, khong co Kafka outbox/event nao de `catalog-service` consume.

## Storefront read path Phase 3

`GET /api/v1/permitall/products/search` query truc tiep alias `products`. API ho tro keyword co/khong dau, fuzzy co `prefix_length=2` va `max_expansions=25`, highlight, category/seller/price/color/size, single-select facets va cac sort `relevance`, `newest`, `price_asc`, `price_desc`, `rating_desc`.

```text
GET /api/v1/permitall/products/search?q=giay+chay&color=do&sizeValue=42&sort=relevance&page=0&size=20
```

Response co them `highlights`, `facets.categories/shops/priceRanges/colors/sizes` va `nextCursor`. Facet la **single-select**: moi facet duoc tinh tren keyword va cac filter khac, nhung bo filter cua chinh no. Shop facet tra `sellerId` vi catalog outbox khong so huu ten shop. FE khong goi N+1: `FilterBox` lay danh sach shop mot lan, con `ProductsView` gom unique sellerId cua items + shop facet va goi mot request batch `/api/v1/permitall/shops/by-ids`.

Autocomplete dung field rieng `search_as_you_type`, khong dung edge-prefix token trong full-text field:

```text
GET /api/v1/permitall/products/search/autocomplete?q=giay&size=8
```

Moi chuoi phan trang mo Point In Time (PIT) tren request dau. `nextCursor` chua `pitId`, sort values, total hits, trang tiep theo va thoi diem phat hanh; toan bo payload duoc HMAC-SHA256. Cursor bi rang buoc voi keyword/filter/sort/page/page-size, nen khong the sua hoac tai su dung cho query khac. Trang sau dung lai PIT voi `search_after` va tie-breaker `id`. `keep_alive=2m` duoc gia han moi trang. Trang cuoi dong PIT; PIT vua mo cung duoc dong neu search loi. FE goi `DELETE /api/v1/permitall/products/search/pit?cursor=...` khi doi search session/roi trang. PIT bi bo quen tu het han sau 2 phut. Cursor sai/khac query tra `400 SEARCH_CURSOR_INVALID`; cursor qua han tra `400 SEARCH_CURSOR_EXPIRED`.

Phai dat `CATALOG_SEARCH_CURSOR_SIGNING_KEY` thanh secret ngau nhien it nhat 32 byte o moi truong ngoai local. Default trong `application.yml` chi danh cho may phat trien.

Verify snapshot that khi live index thay doi giua bon trang:

```powershell
powershell -ExecutionPolicy Bypass -File backend-microservice\search-pipeline\scripts\verify-pit-pagination.ps1
```

API MySQL cu `GET /api/v1/permitall/products` van duoc giu nguyen. Product detail, cart va checkout van doc MySQL. Search API khong fallback sang MySQL khi Elasticsearch loi; no tra HTTP 503 de caller nhan biet read model dang khong san sang.

Exact total va aggregation chi chay o request dau cua chuoi PIT. Cursor da ky mang total; cac trang tiep theo dat `track_total_hits=false`, bo aggregation va FE giu facet snapshot cua trang dau. `_source` cung chi lay field storefront can. Category/shop bi gioi han 50 bucket; color/size 30 bucket; price dung bon range co dinh. Theo doi `catalog.search.elasticsearch.latency`, `catalog.search.errors` va `catalog.search.zero_results` qua Actuator/Prometheus; neu latency trang dau tang, uu tien cache theo normalized query/filter va giam bucket size.

## ELK hardening va ranh gioi production

- Kafka Connect image pin Debezium MySQL `3.2.6-3` va Elasticsearch Sink `15.1.3`; khong dung tag connector `latest` trong CDC.
- Elasticsearch Sink dung `behavior.on.malformed.documents=WARN`: record loi mapping duoc ghi WARN, day vao DLQ va offset tiep tuc. Can alert tren `dlq.products.elasticsearch`, connector/task FAILED va consumer lag.
- Filebeat dung `filestream` + container parser thay cho input `container` da deprecated, va luu registry trong named volume de tranh doc lai log sau recreate.
- Logstash bat persistent queue, DLQ va ECS v8. Log index dung ILM alias `ecommerce-logs`, rollover khi shard dat 25 GB hoac 1 ngay, xoa sau 30 ngay. Job `elasticsearch-setup` cai policy truoc khi Logstash start.
- Elasticsearch, Logstash, Kafka Connect deu co readiness/health dependency; catalog client co connect timeout 1 giay va socket timeout 5 giay.

`docker-compose.yml` la stack **local development**: single-node, `number_of_replicas=0`, HTTP va `xpack.security.enabled=false`. Khong deploy file nay nguyen trang ra production. Production toi thieu can TLS + authentication/least-privilege API key, 3 master-eligible node, replica >= 1, snapshot repository + restore drill, shard/capacity alert, DLQ/lag alert va rolling-upgrade plan. Version Elastic hien tai cua repo duoc giu dong bo o `8.15.3`; viec nang major/minor phai test chung Spring client, Logstash/Filebeat va Elasticsearch Sink tren staging truoc cutover.

## Cleanup outbox

Outbox khong can giu vinh vien; giu du lau de trace/debug:

```powershell
cd "C:\My Project\e-commerce"
powershell -ExecutionPolicy Bypass -File backend-microservice\search-pipeline\scripts\cleanup-outbox.ps1 -RetentionDays 7
```

Nen dua len scheduler cua moi truong deploy. Khong dung cleanup job lam co che dong bo.

## Reindex / mapping changes Phase 3

Khong sua analyzer/mapping truc tiep tren index dang phuc vu. `products_v3` la index moi; index cu duoc giu lai sau cutover de rollback.

```powershell
powershell -ExecutionPolicy Bypass -File backend-microservice\search-pipeline\scripts\reindex-products-v3.ps1 `
  -CatalogAdminUrl http://localhost:8083/api/v1/admin/product-attributes/reindex `
  -GatewayToken local-dev-gateway-token
```

Script goi API reindex de sinh lai full `ProductUpdated` tu MySQL, doi den khi alias hien tai khop MySQL, tao `products_v3`, pause sink, Elasticsearch reindex, verify target, atomically swap alias, roi resume sink. Event phat sinh trong luc pause nam trong Kafka va se vao index moi sau resume. Neu bat ky buoc verify nao fail, alias khong doi. Khong dung `-SkipMysqlRebuild` tru khi alias nguon vua duoc doi soat rieng.

Kiem tra thu cong sau cutover:

```powershell
powershell -ExecutionPolicy Bypass -File backend-microservice\search-pipeline\scripts\verify-products-index.ps1
powershell -ExecutionPolicy Bypass -File backend-microservice\search-pipeline\scripts\verify-products-consistency.ps1
```
