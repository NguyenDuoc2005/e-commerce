package com.ecommerce.catalog.service;

import com.ecommerce.catalog.client.SellerClient;
import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.document.ProductDocument;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.entity.ProductVariant;
import com.ecommerce.catalog.repository.ProductVariantRepository;
import com.ecommerce.catalog.repository.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.LinkedHashMap;

@Component
public class ProductSearchIndexer {

    private final ProductRepository sanPhamRepository;
    private final ProductVariantRepository sanPhamChiTietRepository;
    private final SellerClient sellerClient;
    private final DynamicAttributeService dynamicAttributeService;

    public ProductSearchIndexer(
            ProductRepository sanPhamRepository,
            ProductVariantRepository sanPhamChiTietRepository,
            SellerClient sellerClient,
            DynamicAttributeService dynamicAttributeService
    ) {
        this.sanPhamRepository = sanPhamRepository;
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
        this.sellerClient = sellerClient;
        this.dynamicAttributeService = dynamicAttributeService;
    }

    public Optional<ProductDocument> buildDocument(String productId) {
        if (productId == null || productId.isBlank()) {
            return Optional.empty();
        }
        return sanPhamRepository.findById(productId).flatMap(this::buildDocument);
    }

    public Optional<ProductDocument> buildDocument(Product product) {
        if (product == null || product.getStatus() != EntityStatus.ACTIVE) {
            return Optional.empty();
        }

        List<ProductVariant> details = sanPhamChiTietRepository.findByProductIdAndStatusOrderByCreatedDateDesc(product.getId(), EntityStatus.ACTIVE);
        if (details.isEmpty()) {
            return Optional.empty();
        }

        ProductDocument document = new ProductDocument();
        document.setId(product.getId());
        document.setName(product.getName());
        document.setDescription(product.getDescription());
        document.setSellerId(product.getSellerId());
        Map<String, Object> seller = safeSeller(product.getSellerId());
        document.setSellerName(stringValue(seller.get("shopName")));
        document.setSellerSlug(stringValue(seller.get("sellerSlug")));
        document.setSellerRating(doubleValue(seller.get("rating")));
        document.setSoldCount(longValue(seller.get("soldCount")));
        document.setCategoryId(product.getCategory() == null ? null : product.getCategory().getId());
        document.setCategory(product.getCategory() == null ? null : product.getCategory().getName());
        document.setBrandId(product.getBrand() == null ? null : product.getBrand().getId());
        document.setBrand(product.getBrand() == null ? null : product.getBrand().getName());
        document.setPrice(details.stream()
                .map(ProductVariant::getSalePrice)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(0D));
        document.setImageUrl(details.stream()
                .map(ProductVariant::getImageUrl)
                .filter(java.util.Objects::nonNull)
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElse(null));
        document.setAttributes(dynamicAttributeService.productValues(product.getId(), product.getSellerId()).stream()
                .map(attribute -> {
                    ProductDocument.AttributeDocument item = new ProductDocument.AttributeDocument();
                    item.setAttributeId(attribute.attributeId());
                    item.setName(attribute.name());
                    item.setDataType(attribute.dataType().name());
                    List<String> values = new java.util.ArrayList<>();
                    if (attribute.textValue() != null) {
                        values.add(attribute.textValue());
                    }
                    Map<String, String> optionValues = new LinkedHashMap<>();
                    attribute.options().forEach(option -> optionValues.put(option.id(), option.value()));
                    attribute.selectedOptionIds().stream()
                            .map(optionValues::get)
                            .filter(java.util.Objects::nonNull)
                            .forEach(values::add);
                    item.setTextValues(values);
                    item.setKeywordValues(values.stream().map(DynamicAttributeService::normalize).toList());
                    item.setNumberValue(attribute.numberValue() == null ? null : attribute.numberValue().doubleValue());
                    item.setOptionIds(attribute.selectedOptionIds());
                    return item;
                })
                .toList());
        return Optional.of(document);
    }

    private Map<String, Object> safeSeller(String sellerId) {
        if (sellerId == null || sellerId.isBlank()) {
            return Map.of();
        }
        try {
            return sellerClient.publicProfile(sellerId);
        } catch (RuntimeException ignored) {
            return Map.of();
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Double doubleValue(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0D;
    }

    private Long longValue(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }
}
