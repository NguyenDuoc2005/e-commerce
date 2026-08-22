package com.ecommerce.catalog.service.impl;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.entity.ProductVariant;
import com.ecommerce.catalog.client.PromotionClient;
import com.ecommerce.catalog.client.SellerClient;
import com.ecommerce.catalog.model.request.ProductDetailRequest;
import com.ecommerce.catalog.model.request.ProductDetailSearchRequest;
import com.ecommerce.catalog.model.request.ProductRequest;
import com.ecommerce.catalog.repository.SizeRepository;
import com.ecommerce.catalog.repository.ColorRepository;
import com.ecommerce.catalog.repository.ProductVariantRepository;
import com.ecommerce.catalog.repository.ProductRepository;
import com.ecommerce.catalog.service.ProductDetailService;
import com.ecommerce.catalog.service.ProductOutboxService;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.catalog.service.DynamicAttributeService;
import com.ecommerce.common.base.PageableObject;
import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.util.PageUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductDetailServiceImpl implements ProductDetailService {

    private final ProductVariantRepository repository;
    private final ProductRepository sanPhamRepository;
    private final SizeRepository kichCoRepository;
    private final ColorRepository mauSacRepository;
    private final ProductService productService;
    private final PromotionClient promotionClient;
    private final SellerClient sellerClient;
    private final ProductOutboxService productOutboxService;
    private final DynamicAttributeService dynamicAttributeService;

    public ProductDetailServiceImpl(
            ProductVariantRepository repository,
            ProductRepository sanPhamRepository,
            SizeRepository kichCoRepository,
            ColorRepository mauSacRepository,
            ProductService productService,
            PromotionClient promotionClient,
            SellerClient sellerClient,
            ProductOutboxService productOutboxService,
            DynamicAttributeService dynamicAttributeService
    ) {
        this.repository = repository;
        this.sanPhamRepository = sanPhamRepository;
        this.kichCoRepository = kichCoRepository;
        this.mauSacRepository = mauSacRepository;
        this.productService = productService;
        this.promotionClient = promotionClient;
        this.sellerClient = sellerClient;
        this.productOutboxService = productOutboxService;
        this.dynamicAttributeService = dynamicAttributeService;
    }

    @Override
    public ResponseObject<?> getAll(ProductDetailSearchRequest request) {
        Pageable pageable = PageUtils.createPageable(request, "createdDate");
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            request.setEntityStatus("0".equals(request.getStatus()) ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        }
        return new ResponseObject<>(PageableObject.of(repository.getAllProductVariantByFilter(pageable, request)), HttpStatus.OK, "Lay danh sach san pham chi tiet thanh cong");
    }

    @Override
    public ResponseObject<?> getSellerAll(ProductDetailSearchRequest request, String sellerId) {
        if (!StringUtils.hasText(sellerId)) {
            return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "SellerId khong hop le");
        }
        request.setSellerId(sellerId);
        return getAll(request);
    }

    @Override
    public ResponseObject<?> getPublicDetail(String productId) {
        if (productId == null || productId.isBlank()) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Id san pham khong duoc de trong");
        }
        Optional<Product> optionalProduct = sanPhamRepository.findById(productId);
        if (optionalProduct.isEmpty() || optionalProduct.get().getStatus() != EntityStatus.ACTIVE) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham");
        }

        Product product = optionalProduct.get();
        List<ProductVariant> details = repository.findByProductIdAndStatusOrderByCreatedDateDesc(productId, EntityStatus.ACTIVE);
        Map<String, Map<String, Object>> discounts = safeActiveDiscounts(details.stream().map(ProductVariant::getId).toList())
                .stream()
                .collect(Collectors.toMap(
                        discount -> String.valueOf(discount.get("productDetailId")),
                        this::publicDiscountMap,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", product.getId());
        response.put("tenProduct", product.getName());
        response.put("tenSanPham", product.getName());
        response.put("description", product.getDescription());
        response.put("moTa", product.getDescription());
        Map<String, Object> brand = attributeMap(
                product.getBrand(),
                attr -> attr.getId(),
                attr -> attr.getName(),
                "tenBrand",
                "maBrand"
        );
        aliasAttribute(brand, "tenThuongHieu", "maThuongHieu");
        response.put("brand", brand);
        response.put("thuongHieu", brand);
        response.put("xuatXu", attributeMap(
                product.getOrigin(),
                attr -> attr.getId(),
                attr -> attr.getName(),
                "tenNuoc",
                "maNuoc"
        ));
        Map<String, Object> material = attributeMap(
                product.getMaterial(),
                attr -> attr.getId(),
                attr -> attr.getName(),
                "tenMaterial",
                "maMaterial"
        );
        aliasAttribute(material, "tenChatLieu", "maChatLieu");
        response.put("material", material);
        response.put("chatLieu", material);
        Map<String, Object> category = attributeMap(
                product.getCategory(),
                attr -> attr.getId(),
                attr -> attr.getName(),
                "tenCategory",
                "maCategory"
        );
        aliasAttribute(category, "tenDanhMuc", "maDanhMuc");
        response.put("category", category);
        response.put("danhMuc", category);
        Map<String, Object> soleType = attributeMap(
                product.getSoleType(),
                attr -> attr.getId(),
                attr -> attr.getName(),
                "tenSoleType",
                "maSoleType"
        );
        aliasAttribute(soleType, "tenLoaiDe", "maLoaiDe");
        response.put("soleType", soleType);
        response.put("loaiDe", soleType);
        List<Map<String, Object>> publicDetails = details.stream()
                .map(detail -> publicDetailMap(detail, discounts.get(detail.getId())))
                .toList();
        response.put("chiTietProduct", publicDetails);
        response.put("chiTietSanPham", publicDetails);
        response.put("sellerId", product.getSellerId());
        response.put("ratingAverage", product.getRatingAverage());
        response.put("ratingCount", product.getRatingCount());
        response.put("attributes", dynamicAttributeService.productValues(product.getId(), product.getSellerId()));
        response.putAll(safeSellerProfile(product.getSellerId()));

        return new ResponseObject<>(response, HttpStatus.OK, "Lay chi tiet san pham thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> changeProductStatus(String id) {
        Optional<ProductVariant> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return ResponseObject.successForward(HttpStatus.NOT_FOUND, "Khong tim thay san pham");
        }
        ProductVariant detail = optional.get();
        detail.setStatus(detail.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        repository.save(detail);
        if (detail.getProduct() != null) {
            productOutboxService.publishChanged(detail.getProduct().getId(), ProductOutboxServiceImpl.UPDATED);
        }
        return ResponseObject.successForward(HttpStatus.OK, "Doi trang thai thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> changeSellerProductStatus(String id, String sellerId) {
        Optional<ProductVariant> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return ResponseObject.successForward(HttpStatus.NOT_FOUND, "Khong tim thay san pham");
        }
        if (!sellerOwns(optional.get(), sellerId)) {
            return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "Khong co quyen thay doi san pham nay");
        }
        return changeProductStatus(id);
    }

    @Override
    public ResponseObject<?> getSPCTById(String id) {
        return repository.getProductID(id)
                .map(value -> new ResponseObject<>(value, HttpStatus.OK, "san pham thanh cong"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham"));
    }

    @Override
    public ResponseObject<?> getSellerSPCTById(String id, String sellerId) {
        Optional<Product> product = sanPhamRepository.findById(id);
        if (product.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham");
        }
        if (!sellerOwns(product.get(), sellerId)) {
            return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "Khong co quyen xem san pham nay");
        }
        return getSPCTById(id);
    }

    @Override
    public ResponseObject<?> getDetailSPCT(String id) {
        return repository.getProductVariantID(id)
                .map(value -> new ResponseObject<>(value, HttpStatus.OK, "san pham thanh cong"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham"));
    }

    @Override
    public ResponseObject<?> getSellerDetailSPCT(String id, String sellerId) {
        Optional<ProductVariant> detail = repository.findById(id);
        if (detail.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham");
        }
        if (!sellerOwns(detail.get(), sellerId)) {
            return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "Khong co quyen xem san pham nay");
        }
        return getDetailSPCT(id);
    }

    @Override
    public ResponseObject<?> getListSize() {
        return new ResponseObject<>(repository.getListSize(), HttpStatus.OK, "Lay thanh cong danh sach thuong hieu");
    }

    @Override
    public ResponseObject<?> getListColor() {
        return new ResponseObject<>(repository.getListColor(), HttpStatus.OK, "Lay thanh cong danh sach thuong hieu");
    }

    @Override
    public ResponseObject<?> getListThemProduct() {
        return new ResponseObject<>(repository.getListThemSP(), HttpStatus.OK, "lay danh sach san pham thanh cong");
    }

    @Override
    public ResponseObject<?> getSellerListProduct(String sellerId) {
        if (!StringUtils.hasText(sellerId)) {
            return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "SellerId khong hop le");
        }
        return new ResponseObject<>(repository.getListThemSPBySeller(sellerId), HttpStatus.OK, "lay danh sach san pham thanh cong");
    }

    @Override
    public ResponseObject<?> getSellerLowStock(String sellerId, Integer threshold) {
        int safeThreshold = threshold == null ? 5 : Math.max(0, Math.min(threshold, 100));
        List<Map<String, Object>> rows = repository
                .findTop20BySellerIdAndQuantityLessThanEqualOrderByQuantityAsc(sellerId, safeThreshold)
                .stream()
                .map(variant -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", variant.getId());
                    row.put("code", variant.getCode());
                    row.put("productName", variant.getProduct() == null ? null : variant.getProduct().getName());
                    row.put("quantity", variant.getQuantity());
                    row.put("imageUrl", variant.getImageUrl());
                    return row;
                })
                .toList();
        return new ResponseObject<>(rows, HttpStatus.OK, "Lay danh sach ton kho thap thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> modifyProduct(ProductDetailRequest request) {
        String productId = firstProductId(request.getIdSP());
        if (productId != null) {
            String existingId = repository.checkThemProduct(request.getIdMau(), request.getIdSize(), productId);
            if (existingId != null) {
                ProductVariant existing = repository.findById(existingId).orElse(null);
                String productName = existing == null || existing.getProduct() == null ? "" : existing.getProduct().getName();
                return new ResponseObject<>(null, HttpStatus.OK, "San pham " + productName + " voi mau va kich thuoc nay da ton tai se khong duoc them moi");
            }
        }

        ProductVariant detail = new ProductVariant();
        applyDetail(detail, request);
        if (productId != null) {
            sanPhamRepository.findById(productId).ifPresent(detail::setProduct);
        } else {
            ProductRequest productRequest = toProductRequest(request);
            ResponseObject<?> created = productService.modifyProduct(productRequest);
            if (created.getData() instanceof Product product) {
                detail.setProduct(product);
            }
        }
        if (detail.getProduct() != null && !StringUtils.hasText(detail.getSellerId())) {
            detail.setSellerId(detail.getProduct().getSellerId());
        }
        detail.setStatus(EntityStatus.ACTIVE);
        saveImageIfPresent(detail, request);
        repository.save(detail);
        if (detail.getProduct() != null) {
            productOutboxService.publishChanged(detail.getProduct().getId(), ProductOutboxServiceImpl.UPDATED);
        }
        return new ResponseObject<>(detail, HttpStatus.CREATED, "Tao san pham thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> modifySellerProduct(ProductDetailRequest request, String sellerId) {
        if (!StringUtils.hasText(sellerId)) {
            return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "SellerId khong hop le");
        }
        request.setSellerId(sellerId);
        String productId = firstProductId(request.getIdSP());
        if (productId != null) {
            Optional<Product> product = sanPhamRepository.findById(productId);
            if (product.isEmpty()) {
                return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham");
            }
            if (!sellerOwns(product.get(), sellerId)) {
                return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "Khong co quyen them bien the cho san pham nay");
            }
        }
        return modifyProduct(request);
    }

    @Override
    @Transactional
    public ResponseObject<?> updateProduct(ProductDetailRequest request) {
        ProductVariant detail = repository.findById(request.getId()).orElse(null);
        if (detail == null) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham");
        }
        String productId = detail.getProduct() == null ? null : detail.getProduct().getId();
        String existingId = productId == null ? null : repository.checkThemProduct(request.getIdMau(), request.getIdSize(), productId);
        if (existingId != null && !existingId.equals(request.getId())) {
            return new ResponseObject<>(null, HttpStatus.OK, "San pham voi mau va kich thuoc nay da ton tai se khong duoc them moi");
        }
        applyDetail(detail, request);
        saveImageIfPresent(detail, request);
        repository.save(detail);
        if (detail.getProduct() != null) {
            productOutboxService.publishChanged(detail.getProduct().getId(), ProductOutboxServiceImpl.UPDATED);
        }
        return new ResponseObject<>(detail, HttpStatus.CREATED, "cap nhat san pham chi tiet thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> updateSellerProduct(ProductDetailRequest request, String sellerId) {
        ProductVariant detail = repository.findById(request.getId()).orElse(null);
        if (detail == null) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham");
        }
        if (!sellerOwns(detail, sellerId)) {
            return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "Khong co quyen cap nhat san pham nay");
        }
        request.setSellerId(sellerId);
        return updateProduct(request);
    }

    private void applyDetail(ProductVariant detail, ProductDetailRequest request) {
        detail.setSalePrice(request.getSalePrice());
        detail.setQuantity(request.getQuantity());
        if (StringUtils.hasText(request.getSellerId())) {
            detail.setSellerId(request.getSellerId());
        } else if (detail.getProduct() != null) {
            detail.setSellerId(detail.getProduct().getSellerId());
        }
        detail.setSize(null);
        detail.setColor(null);
        if (StringUtils.hasText(request.getIdSize())) {
            kichCoRepository.findById(request.getIdSize()).ifPresent(detail::setSize);
        }
        if (StringUtils.hasText(request.getIdMau())) {
            mauSacRepository.findById(request.getIdMau()).ifPresent(detail::setColor);
        }
    }

    private void saveImageIfPresent(ProductVariant detail, ProductDetailRequest request) {
        if (request.getImageUrl() == null || request.getImageUrl().isEmpty()) {
            return;
        }
        try {
            request.getImageUrl().getBytes();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Loi khi doc file imageUrl: " + ex.getMessage(), ex);
        }
    }

    private Map<String, Object> publicDetailMap(ProductVariant detail, Map<String, Object> discount) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", detail.getId());
        row.put("sellerId", detail.getSellerId());
        Map<String, Object> color = colorMap(detail);
        Map<String, Object> size = sizeMap(detail);
        row.put("color", color);
        row.put("mauSac", color);
        row.put("size", size);
        row.put("kichCo", size);
        row.put("salePrice", detail.getSalePrice());
        row.put("giaBan", detail.getSalePrice());
        int quantity = detail.getQuantity() == null ? 0 : detail.getQuantity();
        row.put("quantity", quantity);
        row.put("soLuong", quantity);
        row.put("hinhAnh", detail.getImageUrl());
        row.put("promotionCampaign", discount);
        row.put("dotGiamGia", discount);
        return row;
    }

    private Map<String, Object> colorMap(ProductVariant detail) {
        if (detail.getColor() == null) {
            return null;
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", detail.getColor().getId());
        row.put("tenColor", detail.getColor().getName());
        row.put("tenMauSac", detail.getColor().getName());
        row.put("maMau", detail.getColor().getMau());
        return row;
    }

    private Map<String, Object> sizeMap(ProductVariant detail) {
        if (detail.getSize() == null) {
            return null;
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", detail.getSize().getId());
        row.put("tenSize", detail.getSize().getName());
        row.put("tenKichCo", detail.getSize().getName());
        row.put("maSize", detail.getSize().getCode());
        return row;
    }

    private <T> Map<String, Object> attributeMap(T attribute, Function<T, String> idGetter, Function<T, String> nameGetter, String nameKey, String codeKey) {
        if (attribute == null) {
            return null;
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", idGetter.apply(attribute));
        row.put(nameKey, nameGetter.apply(attribute));
        row.put(codeKey, null);
        return row;
    }

    private void aliasAttribute(Map<String, Object> attribute, String nameKey, String codeKey) {
        if (attribute == null) {
            return;
        }
        Object name = attribute.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("ten"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        attribute.put(nameKey, name);
        attribute.put(codeKey, null);
    }

    private Map<String, Object> safeSellerProfile(String sellerId) {
        if (!StringUtils.hasText(sellerId)) {
            return Map.of();
        }
        try {
            return sellerClient.publicProfile(sellerId);
        } catch (RuntimeException ignored) {
            return Map.of();
        }
    }

    private Map<String, Object> publicDiscountMap(Map<String, Object> discount) {
        Map<String, Object> row = new LinkedHashMap<>(discount);
        row.put("tenPromotionCampaign", discount.get("name"));
        return row;
    }

    private List<Map<String, Object>> safeActiveDiscounts(List<String> productDetailIds) {
        try {
            return promotionClient.getActiveDiscounts(productDetailIds);
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    private String firstProductId(String idSP) {
        if (idSP == null || idSP.isBlank()) {
            return null;
        }
        return idSP.split(",")[0];
    }

    private ProductRequest toProductRequest(ProductDetailRequest request) {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName(request.getName());
        productRequest.setDescription(request.getDescription());
        productRequest.setIdBrand(request.getIdBrand());
        productRequest.setIdXuatXu(request.getIdXuatXu());
        productRequest.setIdSoleType(request.getIdSoleType());
        productRequest.setIdCategory(request.getIdCategory());
        productRequest.setIdMaterial(request.getIdMaterial());
        productRequest.setSellerId(request.getSellerId());
        return productRequest;
    }

    private boolean sellerOwns(Product product, String sellerId) {
        return StringUtils.hasText(sellerId) && sellerId.equals(product.getSellerId());
    }

    private boolean sellerOwns(ProductVariant detail, String sellerId) {
        if (!StringUtils.hasText(sellerId)) {
            return false;
        }
        if (sellerId.equals(detail.getSellerId())) {
            return true;
        }
        return detail.getProduct() != null && sellerId.equals(detail.getProduct().getSellerId());
    }
}
