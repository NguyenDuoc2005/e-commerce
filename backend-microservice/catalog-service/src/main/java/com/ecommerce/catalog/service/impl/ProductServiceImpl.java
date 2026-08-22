package com.ecommerce.catalog.service.impl;

import com.ecommerce.catalog.client.PromotionClient;
import com.ecommerce.catalog.client.SellerClient;
import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.document.ProductDocument;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.entity.ProductVariant;
import com.ecommerce.catalog.model.request.ProductRequest;
import com.ecommerce.catalog.model.request.ProductSearchRequest;
import com.ecommerce.catalog.repository.MaterialRepository;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.SoleTypeRepository;
import com.ecommerce.catalog.repository.ProductVariantRepository;
import com.ecommerce.catalog.repository.ProductRepository;
import com.ecommerce.catalog.repository.BrandRepository;
import com.ecommerce.catalog.repository.OriginRepository;
import com.ecommerce.catalog.service.ProductOutboxService;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.catalog.service.DynamicAttributeService;
import com.ecommerce.common.base.PageableObject;
import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.util.PageUtils;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository sanPhamRepository;
    private final ProductVariantRepository sanPhamChiTietRepository;
    private final BrandRepository thuongHieuRepository;
    private final CategoryRepository danhMucRepository;
    private final SoleTypeRepository loaiDeRepository;
    private final OriginRepository xuatSuRepository;
    private final MaterialRepository chatLieuRepository;
    private final PromotionClient promotionClient;
    private final SellerClient sellerClient;
    private final ProductOutboxService productOutboxService;
    private final DynamicAttributeService dynamicAttributeService;
    private final ElasticsearchOperations elasticsearchOperations;

    public ProductServiceImpl(
            ProductRepository sanPhamRepository,
            ProductVariantRepository sanPhamChiTietRepository,
            BrandRepository thuongHieuRepository,
            CategoryRepository danhMucRepository,
            SoleTypeRepository loaiDeRepository,
            OriginRepository xuatSuRepository,
            MaterialRepository chatLieuRepository,
            PromotionClient promotionClient,
            SellerClient sellerClient,
            ProductOutboxService productOutboxService,
            DynamicAttributeService dynamicAttributeService,
            ElasticsearchOperations elasticsearchOperations
    ) {
        this.sanPhamRepository = sanPhamRepository;
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
        this.thuongHieuRepository = thuongHieuRepository;
        this.danhMucRepository = danhMucRepository;
        this.loaiDeRepository = loaiDeRepository;
        this.xuatSuRepository = xuatSuRepository;
        this.chatLieuRepository = chatLieuRepository;
        this.promotionClient = promotionClient;
        this.sellerClient = sellerClient;
        this.productOutboxService = productOutboxService;
        this.dynamicAttributeService = dynamicAttributeService;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public ResponseObject<?> getAdminAll(ProductSearchRequest request) {
        Pageable pageable = PageUtils.createPageable(request, "createdDate");
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            request.setEntityStatus("0".equals(request.getStatus()) ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        }
        return new ResponseObject<>(
                PageableObject.of(sanPhamRepository.getAllProductByFilter(pageable, request)),
                HttpStatus.OK,
                "Lay danh sach san pham thanh cong"
        );
    }

    @Override
    public ResponseObject<?> getSellerAll(ProductSearchRequest request, String sellerId) {
        if (!StringUtils.hasText(sellerId)) {
            return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "SellerId khong hop le");
        }
        request.setSellerId(sellerId);
        Pageable pageable = PageUtils.createPageable(request, "createdDate");
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            request.setEntityStatus("0".equals(request.getStatus()) ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        }
        Page<Map<String, Object>> page = sanPhamRepository.getAllProductByFilter(pageable, request)
                .map(product -> productResponse(product, sellerId));
        return new ResponseObject<>(PageableObject.of(page), HttpStatus.OK, "Lay danh sach san pham thanh cong");
    }

    @Override
    public ResponseObject<?> getAll(ProductSearchRequest request) {
        List<Map<String, Object>> rows = filteredPublicProductRows(request);
        enrichPublicProducts(rows);
        return new ResponseObject<>(
                pageMap(rows, request),
                HttpStatus.OK,
                "Lay danh sach san pham thanh cong"
        );
    }

    @Override
    public ResponseObject<?> getProductById(String id) {
        return sanPhamRepository.getAllProductID(id)
                .map(product -> new ResponseObject<>(productResponse(product, null), HttpStatus.OK, "san pham thanh cong"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham"));
    }

    @Override
    public ResponseObject<?> getSellerProductById(String id, String sellerId) {
        Optional<Product> product = sanPhamRepository.findById(id);
        if (product.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham");
        }
        if (!sellerOwns(product.get(), sellerId)) {
            return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "Khong co quyen thao tac san pham nay");
        }
        return sanPhamRepository.getAllProductID(id)
                .map(row -> new ResponseObject<>(productResponse(row, sellerId), HttpStatus.OK, "san pham thanh cong"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham"));
    }

    @Override
    @Transactional
    public ResponseObject<?> modifyProduct(ProductRequest request) {
        if (StringUtils.hasLength(request.getId())) {
            Optional<Product> existing = sanPhamRepository.findById(request.getId());
            if (existing.isPresent()) {
                Product product = existing.get();
                applyRequest(product, request);
                sanPhamRepository.save(product);
                dynamicAttributeService.replaceProductAttributes(product, request.getSellerId(), request.getAttributes());
                productOutboxService.publishChanged(product.getId(), ProductOutboxServiceImpl.UPDATED);
                return new ResponseObject<>(product, HttpStatus.OK, "Cap nhat size thanh cong");
            }
        }

        Product product = new Product();
        applyRequest(product, request);
        product.setStatus(EntityStatus.ACTIVE);
        sanPhamRepository.save(product);
        dynamicAttributeService.replaceProductAttributes(product, request.getSellerId(), request.getAttributes());
        productOutboxService.publishChanged(product.getId(), ProductOutboxServiceImpl.CREATED);
        return new ResponseObject<>(product, HttpStatus.CREATED, "Tao san pham thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> modifySellerProduct(ProductRequest request, String sellerId) {
        if (!StringUtils.hasText(sellerId)) {
            return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "SellerId khong hop le");
        }
        request.setSellerId(sellerId);
        if (StringUtils.hasLength(request.getId())) {
            Optional<Product> existing = sanPhamRepository.findById(request.getId());
            if (existing.isEmpty()) {
                return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham");
            }
            if (!sellerOwns(existing.get(), sellerId)) {
                return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "Khong co quyen cap nhat san pham nay");
            }
        }
        return modifyProduct(request);
    }

    @Override
    @Transactional
    public ResponseObject<?> changeProductStatus(String id) {
        Optional<Product> optional = sanPhamRepository.findById(id);
        if (optional.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham");
        }

        Product product = optional.get();
        EntityStatus newStatus = product.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE;
        product.setStatus(newStatus);
        sanPhamRepository.save(product);

        List<String> ids = sanPhamChiTietRepository.checkIdProductCT(id);
        for (String spctId : ids) {
            sanPhamChiTietRepository.findById(spctId).ifPresent(spct -> {
                spct.setStatus(newStatus);
                sanPhamChiTietRepository.save(spct);
            });
        }

        if (newStatus == EntityStatus.ACTIVE) {
            productOutboxService.publishChanged(product.getId(), ProductOutboxServiceImpl.UPDATED);
        } else {
            productOutboxService.publishDeleted(product.getId());
        }
        return new ResponseObject<>(null, HttpStatus.OK, "Thay doi trang thai thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> changeSellerProductStatus(String id, String sellerId) {
        Optional<Product> optional = sanPhamRepository.findById(id);
        if (optional.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham");
        }
        if (!sellerOwns(optional.get(), sellerId)) {
            return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "Khong co quyen thay doi san pham nay");
        }
        return changeProductStatus(id);
    }

    @Override
    public ResponseObject<?> getListBrand() {
        return new ResponseObject<>(sanPhamRepository.getListBrand(), HttpStatus.OK, "Lay thanh cong danh sach thuong hieu");
    }

    @Override
    public ResponseObject<?> getXuatXu() {
        return new ResponseObject<>(sanPhamRepository.getListXuatXu(), HttpStatus.OK, "Lay thanh cong danh sach thuong hieu");
    }

    @Override
    public ResponseObject<?> getListSoleType() {
        return new ResponseObject<>(sanPhamRepository.getSoleType(), HttpStatus.OK, "Lay thanh cong danh sach thuong hieu");
    }

    @Override
    public ResponseObject<?> getListCategory() {
        return new ResponseObject<>(sanPhamRepository.getListCategory(), HttpStatus.OK, "Lay thanh cong danh sach thuong hieu");
    }

    @Override
    public ResponseObject<?> getListSize() {
        return new ResponseObject<>(sanPhamRepository.getListSize(), HttpStatus.OK, "Lay thanh cong danh sach size");
    }

    @Override
    public ResponseObject<?> getListMau() {
        return new ResponseObject<>(sanPhamRepository.getListMau(), HttpStatus.OK, "Lay thanh cong danh sach mau");
    }

    @Override
    public ResponseObject<?> getListMaterial() {
        return new ResponseObject<>(sanPhamRepository.getListMaterial(), HttpStatus.OK, "Lay thanh cong danh sach thuong hieu");
    }

    @Override
    public ResponseObject<?> getProductMoi(ProductSearchRequest request) {
        List<Map<String, Object>> rows = filteredPublicProductRows(request);
        enrichPublicProducts(rows);
        return new ResponseObject<>(pageMap(rows, request), HttpStatus.OK, "Lay danh sach san pham moi thanh cong");
    }

    @Override
    public ResponseObject<?> getProductGiamGia(ProductSearchRequest request) {
        List<Map<String, Object>> rows = filteredPublicProductRows(request);
        enrichPublicProducts(rows);
        rows = rows.stream().filter(row -> row.get("promotionCampaign") != null).toList();
        return new ResponseObject<>(pageMap(rows, request), HttpStatus.OK, "Lay danh sach san pham giam gia thanh cong");
    }

    @Override
    public ResponseObject<?> getBrandTrangChu(ProductSearchRequest request) {
        List<Map<String, Object>> all = thuongHieuRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE).stream()
                .map(attribute -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", attribute.getId());
                    row.put("code", attribute.getCode());
                    row.put("name", attribute.getName());
                    return row;
                })
                .toList();
        List<Map<String, Object>> rows = slice(all, offset(request), pageSize(request));
        return new ResponseObject<>(pageMap(rows, request), HttpStatus.OK, "lay thuong hieu thanh cong");
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        if (StringUtils.hasText(request.getSellerId())) {
            product.setSellerId(request.getSellerId());
        }
        if (request.getIdBrand() != null) {
            thuongHieuRepository.findById(request.getIdBrand()).ifPresent(product::setBrand);
        }
        if (request.getIdCategory() != null) {
            danhMucRepository.findById(request.getIdCategory()).ifPresent(product::setCategory);
        }
        if (request.getIdSoleType() != null) {
            loaiDeRepository.findById(request.getIdSoleType()).ifPresent(product::setSoleType);
        }
        if (request.getIdXuatXu() != null) {
            xuatSuRepository.findById(request.getIdXuatXu()).ifPresent(product::setOrigin);
        }
        if (request.getIdMaterial() != null) {
            chatLieuRepository.findById(request.getIdMaterial()).ifPresent(product::setMaterial);
        }
    }

    private Map<String, Object> productResponse(com.ecommerce.catalog.model.response.ProductResponse product, String sellerId) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("stt", product.getStt());
        row.put("id", product.getId());
        row.put("code", product.getCode());
        row.put("name", product.getName());
        row.put("tenBrand", product.getTenBrand());
        row.put("idBrand", product.getIdBrand());
        row.put("tenXuatXu", product.getTenXuatXu());
        row.put("idXuatXu", product.getIdXuatXu());
        row.put("tenSoleType", product.getTenSoleType());
        row.put("idSoleType", product.getIdSoleType());
        row.put("tenCategory", product.getTenCategory());
        row.put("idCategory", product.getIdCategory());
        row.put("tenMaterial", product.getTenMaterial());
        row.put("idMaterial", product.getIdMaterial());
        row.put("description", product.getDescription());
        row.put("tongSP", product.getTongSP());
        row.put("sellerId", product.getSellerId());
        row.put("status", product.getStatus());
        row.put("attributes", dynamicAttributeService.productValues(product.getId(), sellerId));
        return row;
    }

    private List<Map<String, Object>> filteredPublicProductRows(ProductSearchRequest request) {
        String q = request.getQ() == null ? "" : request.getQ().trim().toLowerCase();
        List<String> elasticProductIds = searchProductIds(q);
        List<String> brandIds = splitIds(firstText(request.getBrandIds(), request.getThuongHieuIds()));
        List<String> materialIds = splitIds(firstText(request.getMaterialIds(), request.getChatLieuIds()));
        List<String> soleIds = splitIds(firstText(request.getSoleTypeIds(), request.getLoaiDeIds()));
        List<String> categoryIds = splitIds(firstText(request.getCategoryIds(), request.getDanhMucIds()));
        Map<String, Map<String, Object>> sellerCache = new LinkedHashMap<>();

        List<Map<String, Object>> all = sanPhamRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE).stream()
                .filter(product -> elasticProductIds == null
                        ? (q.isEmpty()
                        || safe(product.getName()).toLowerCase().contains(q)
                        || safe(product.getCode()).toLowerCase().contains(q))
                        : elasticProductIds.contains(product.getId()))
                .filter(product -> brandIds.isEmpty() || (product.getBrand() != null && brandIds.contains(product.getBrand().getId())))
                .filter(product -> materialIds.isEmpty() || (product.getMaterial() != null && materialIds.contains(product.getMaterial().getId())))
                .filter(product -> soleIds.isEmpty() || (product.getSoleType() != null && soleIds.contains(product.getSoleType().getId())))
                .filter(product -> categoryIds.isEmpty() || (product.getCategory() != null && categoryIds.contains(product.getCategory().getId())))
                .filter(product -> dynamicAttributeService.matchesProductFilters(product.getId(), request.getAttributeFilters()))
                .filter(product -> !StringUtils.hasText(request.getSellerId()) || request.getSellerId().equals(product.getSellerId()))
                .map(product -> {
                    Map<String, Object> seller = sellerProfile(product.getSellerId(), sellerCache);
                    if (StringUtils.hasText(request.getSellerSlug()) && !request.getSellerSlug().equals(safe(seller.get("sellerSlug")))) {
                        return null;
                    }
                if (request.getRatingMin() != null && value(product.getRatingAverage()) < request.getRatingMin()) {
                        return null;
                    }
                    List<ProductVariant> details = sanPhamChiTietRepository.findByProductIdAndStatusOrderByCreatedDateDesc(product.getId(), EntityStatus.ACTIVE);
                    details = details.stream()
                            .filter(detail -> request.getGiaMin() == null || value(detail.getSalePrice()) >= request.getGiaMin())
                            .filter(detail -> request.getGiaMax() == null || value(detail.getSalePrice()) <= request.getGiaMax())
                            .toList();
                    if (details.isEmpty()) {
                        return null;
                    }
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", product.getId());
                    row.put("sellerId", product.getSellerId());
                    row.put("sellerName", seller.get("shopName"));
                    row.put("shopName", seller.get("shopName"));
                    row.put("sellerSlug", seller.get("sellerSlug"));
                    row.put("sellerLogoUrl", seller.get("logoUrl"));
                row.put("sellerRating", seller.get("rating"));
                row.put("soldCount", seller.get("soldCount"));
                row.put("ratingAverage", product.getRatingAverage());
                row.put("ratingCount", product.getRatingCount());
                    row.put("tenProduct", product.getName());
                    row.put("tenSanPham", product.getName());
                    row.put("hinhAnhDaiDien", details.stream().map(ProductVariant::getImageUrl).filter(java.util.Objects::nonNull).findFirst().orElse(null));
                    row.put("brand", product.getBrand() == null ? null : product.getBrand().getName());
                    row.put("thuongHieu", product.getBrand() == null ? null : product.getBrand().getName());
                    row.put("category", product.getCategory() == null ? null : product.getCategory().getName());
                    row.put("danhMuc", product.getCategory() == null ? null : product.getCategory().getName());
                    row.put("material", product.getMaterial() == null ? null : product.getMaterial().getName());
                    row.put("chatLieu", product.getMaterial() == null ? null : product.getMaterial().getName());
                    row.put("xuatXu", product.getOrigin() == null ? null : product.getOrigin().getName());
                    row.put("moTa", product.getDescription());
                    row.put("description", product.getDescription());
                    row.put("attributes", dynamicAttributeService.productValues(product.getId(), null));
                    row.put("salePrice", details.stream().map(ProductVariant::getSalePrice).filter(java.util.Objects::nonNull).mapToDouble(Double::doubleValue).min().orElse(0D));
                    row.put("giaBan", row.get("salePrice"));
                    row.put("ngayTao", product.getCreatedDate());
                    row.put("_details", details);
                    return row;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        return sortProducts(all, request.getSortBy());
    }

    private List<String> searchProductIds(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        try {
            var indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
            if (!indexOps.exists()) {
                indexOps.createWithMapping();
                return null;
            }
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.queryString(queryString -> queryString
                            .query("*" + escapeQueryString(keyword) + "*")
                            .fields("name", "description", "brand", "category", "sellerName")
                            .analyzeWildcard(true)
                            .defaultOperator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.Or)))
                    .build();
            SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
            List<String> ids = hits.stream()
                    .map(SearchHit::getContent)
                    .map(ProductDocument::getId)
                    .filter(StringUtils::hasText)
                    .toList();
            return ids.isEmpty() ? null : ids;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String escapeQueryString(String keyword) {
        return keyword.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("&&", "\\&&")
                .replace("||", "\\||")
                .replace(">", "\\>")
                .replace("<", "\\<")
                .replace("!", "\\!")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("^", "\\^")
                .replace("~", "\\~")
                .replace("?", "\\?")
                .replace(":", "\\:")
                .replace("/", "\\/");
    }

    private void enrichPublicProducts(List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            @SuppressWarnings("unchecked")
            List<ProductVariant> details = (List<ProductVariant>) row.get("_details");
            row.put("size", details.stream()
                    .filter(detail -> detail.getSize() != null)
                    .collect(java.util.stream.Collectors.toMap(
                            detail -> detail.getSize().getId(),
                            detail -> {
                                Map<String, Object> size = new LinkedHashMap<>();
                                size.put("id", detail.getSize().getId());
                                size.put("name", detail.getSize().getName());
                                size.put("ten", detail.getSize().getName());
                                size.put("quantity", value(detail.getQuantity()));
                                size.put("soLuong", value(detail.getQuantity()));
                                return size;
                            },
                            (left, right) -> {
                                left.put("quantity", ((Integer) left.get("quantity")) + ((Integer) right.get("quantity")));
                                left.put("soLuong", ((Integer) left.get("soLuong")) + ((Integer) right.get("soLuong")));
                                return left;
                            },
                            LinkedHashMap::new
                    ))
                    .values()
                    .stream()
                    .toList());
            row.put("kichCo", row.get("size"));
            row.put("color", details.stream()
                    .filter(detail -> detail.getColor() != null)
                    .collect(java.util.stream.Collectors.toMap(
                            detail -> detail.getColor().getId(),
                            detail -> {
                                Map<String, Object> color = new LinkedHashMap<>();
                                color.put("id", detail.getColor().getId());
                                color.put("name", detail.getColor().getName());
                                color.put("ten", detail.getColor().getName());
                                color.put("tenColor", detail.getColor().getName());
                                color.put("tenMauSac", detail.getColor().getName());
                                color.put("maMau", detail.getColor().getMau());
                                return color;
                            },
                            (left, right) -> left,
                            LinkedHashMap::new
                    ))
                    .values()
                    .stream()
                    .toList());
            row.put("mauSac", row.get("color"));
            row.put("dsAnh", details.stream().map(ProductVariant::getImageUrl).filter(java.util.Objects::nonNull).distinct().toList());
            List<Map<String, Object>> discounts = safeActiveDiscounts(details.stream().map(ProductVariant::getId).toList());
            Map<String, Object> discount = discounts.isEmpty() ? null : publicDiscountMap(discounts.get(0));
            row.put("promotionCampaign", discount);
            row.put("dotGiamGia", discount);
            row.put("priceAfterDiscountGiam", discount == null ? null : discount.get("priceAfterDiscount"));
            row.put("giaSauGiam", row.get("priceAfterDiscountGiam"));
            row.remove("_details");
        }
    }

    private List<Map<String, Object>> sortProducts(List<Map<String, Object>> rows, String sortBy) {
        Comparator<Map<String, Object>> comparator = Comparator.comparing(row -> String.valueOf(row.get("ngayTao")));
        if ("createdAt_asc".equals(sortBy)) {
            return rows.stream().sorted(comparator).toList();
        }
        if ("giaBan_asc".equals(sortBy)) {
            return rows.stream().sorted(Comparator.comparingDouble(row -> doubleValue(row.get("salePrice")))).toList();
        }
        if ("giaBan_desc".equals(sortBy)) {
            return rows.stream().sorted(Comparator.comparingDouble((Map<String, Object> row) -> doubleValue(row.get("salePrice"))).reversed()).toList();
        }
        if ("sold_desc".equals(sortBy)) {
            return rows.stream().sorted(Comparator.comparingDouble((Map<String, Object> row) -> doubleValue(row.get("soldCount"))).reversed()).toList();
        }
        if ("rating_desc".equals(sortBy)) {
        return rows.stream().sorted(Comparator.comparingDouble((Map<String, Object> row) -> doubleValue(row.get("ratingAverage"))).reversed()).toList();
        }
        if ("name_asc".equals(sortBy) || "ten_asc".equals(sortBy)) {
            return rows.stream().sorted(Comparator.comparing(row -> safe(row.get("tenProduct")))).toList();
        }
        if ("name_desc".equals(sortBy) || "ten_desc".equals(sortBy)) {
            return rows.stream().sorted(Comparator.comparing((Map<String, Object> row) -> safe(row.get("tenProduct"))).reversed()).toList();
        }
        return rows.stream().sorted(comparator.reversed()).toList();
    }

    private String firstText(String primary, String fallback) {
        return StringUtils.hasText(primary) ? primary : fallback;
    }

    private Map<String, Object> sellerProfile(String sellerId, Map<String, Map<String, Object>> cache) {
        if (!StringUtils.hasText(sellerId)) {
            return Map.of();
        }
        return cache.computeIfAbsent(sellerId, id -> {
            try {
                return sellerClient.publicProfile(id);
            } catch (RuntimeException ignored) {
                return Map.of();
            }
        });
    }

    private Map<String, Object> publicDiscountMap(Map<String, Object> discount) {
        Map<String, Object> row = new LinkedHashMap<>(discount);
        row.put("tenPromotionCampaign", discount.get("name"));
        row.put("tenDotGiamGia", discount.get("name"));
        row.put("phanTramGiam", discount.get("discountValue"));
        row.put("giaTruoc", discount.get("priceBeforeDiscount"));
        row.put("giaSau", discount.get("priceAfterDiscount"));
        row.put("ngayBatDau", discount.get("startDate"));
        row.put("ngayKetThuc", discount.get("endDate"));
        return row;
    }

    private List<Map<String, Object>> safeActiveDiscounts(List<String> productDetailIds) {
        try {
            return promotionClient.getActiveDiscounts(productDetailIds);
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    private List<String> splitIds(String ids) {
        if (ids == null || ids.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private double value(Double value) {
        return value == null ? 0D : value;
    }

    private double doubleValue(Object value) {
        return value == null ? 0D : ((Number) value).doubleValue();
    }

    private boolean sellerOwns(Product product, String sellerId) {
        return StringUtils.hasText(sellerId) && sellerId.equals(product.getSellerId());
    }

    private Map<String, Object> pageMap(List<Map<String, Object>> rows, ProductSearchRequest request) {
        List<Map<String, Object>> pageRows = slice(rows, offset(request), pageSize(request));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", pageRows);
        result.put("totalPages", (long) Math.ceil((double) rows.size() / pageSize(request)));
        result.put("currentPage", Math.max(request.getPage() - 1, 0));
        result.put("totalElements", (long) rows.size());
        return result;
    }

    private int pageSize(ProductSearchRequest request) {
        return request.getSize() <= 0 ? 10 : request.getSize();
    }

    private int offset(ProductSearchRequest request) {
        return Math.max(request.getPage() - 1, 0) * pageSize(request);
    }

    private List<Map<String, Object>> slice(List<Map<String, Object>> rows, int offset, int size) {
        if (offset >= rows.size()) {
            return List.of();
        }
        return rows.subList(offset, Math.min(rows.size(), offset + size));
    }
}
