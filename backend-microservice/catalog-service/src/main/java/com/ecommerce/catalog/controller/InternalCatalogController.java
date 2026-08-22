package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.Size;
import com.ecommerce.catalog.entity.Color;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.entity.ProductVariant;
import com.ecommerce.catalog.repository.SizeRepository;
import com.ecommerce.catalog.repository.ColorRepository;
import com.ecommerce.catalog.repository.ProductVariantRepository;
import com.ecommerce.catalog.repository.ProductRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/catalog")
public class InternalCatalogController {

    private final ProductRepository sanPhamRepository;
    private final ProductVariantRepository sanPhamChiTietRepository;
    private final ColorRepository mauSacRepository;
    private final SizeRepository kichCoRepository;

    public InternalCatalogController(
            ProductRepository sanPhamRepository,
            ProductVariantRepository sanPhamChiTietRepository,
            ColorRepository mauSacRepository,
            SizeRepository kichCoRepository
    ) {
        this.sanPhamRepository = sanPhamRepository;
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
        this.mauSacRepository = mauSacRepository;
        this.kichCoRepository = kichCoRepository;
    }

    @GetMapping("/products")
    public List<Map<String, Object>> getProducts() {
        return sanPhamRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE)
                .stream()
                .map(this::productMap)
                .toList();
    }

    @GetMapping("/products/{productId}/details")
    public List<Map<String, Object>> getProductDetails(@PathVariable String productId) {
        return sanPhamChiTietRepository.findByProductIdAndStatusOrderByCreatedDateDesc(productId, EntityStatus.ACTIVE)
                .stream()
                .map(this::productDetailMap)
                .toList();
    }

    @GetMapping("/product-details")
    public List<Map<String, Object>> getProductDetailsByIds(@RequestParam List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return sanPhamChiTietRepository.findByIdIn(ids).stream().map(this::productDetailMap).toList();
    }

    @GetMapping("/product-details/search")
    public List<Map<String, Object>> searchProductDetails(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String idColor,
            @RequestParam(required = false) String idKichThuoc,
            @RequestParam(required = false) String idCategory,
            @RequestParam(required = false) String idMaterial,
            @RequestParam(required = false) String idBrand,
            @RequestParam(required = false) String idSoleType,
            @RequestParam(required = false) String idSP,
            @RequestParam(required = false) Double priceMin,
            @RequestParam(required = false) Double priceMax
    ) {
        String normalizedQ = q == null ? "" : q.replace("%", "").toLowerCase();
        double giaMax = sanPhamChiTietRepository.findAll().stream()
                .map(ProductVariant::getSalePrice)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0D);
        return sanPhamChiTietRepository.findAll().stream()
                .filter(detail -> detail.getQuantity() != null && detail.getQuantity() > 0)
                .filter(detail -> status == null || status.isBlank() || String.valueOf(detail.getStatus() == null ? null : detail.getStatus().ordinal()).equals(status))
                .filter(detail -> normalizedQ.isBlank()
                        || contains(detail.getCode(), normalizedQ)
                        || (detail.getProduct() != null && contains(detail.getProduct().getName(), normalizedQ)))
                .filter(detail -> idColor == null || idColor.isBlank() || (detail.getColor() != null && idColor.equals(detail.getColor().getId())))
                .filter(detail -> idKichThuoc == null || idKichThuoc.isBlank() || (detail.getSize() != null && idKichThuoc.equals(detail.getSize().getId())))
                .filter(detail -> idCategory == null || idCategory.isBlank() || (detail.getProduct() != null && detail.getProduct().getCategory() != null && idCategory.equals(detail.getProduct().getCategory().getId())))
                .filter(detail -> idMaterial == null || idMaterial.isBlank() || (detail.getProduct() != null && detail.getProduct().getMaterial() != null && idMaterial.equals(detail.getProduct().getMaterial().getId())))
                .filter(detail -> idBrand == null || idBrand.isBlank() || (detail.getProduct() != null && detail.getProduct().getBrand() != null && idBrand.equals(detail.getProduct().getBrand().getId())))
                .filter(detail -> idSoleType == null || idSoleType.isBlank() || (detail.getProduct() != null && detail.getProduct().getSoleType() != null && idSoleType.equals(detail.getProduct().getSoleType().getId())))
                .filter(detail -> idSP == null || idSP.isBlank() || (detail.getProduct() != null && idSP.equals(detail.getProduct().getId())))
                .filter(detail -> priceMin == null || (detail.getSalePrice() != null && detail.getSalePrice() >= priceMin))
                .filter(detail -> priceMax == null || (detail.getSalePrice() != null && detail.getSalePrice() <= priceMax))
                .map(this::productDetailMap)
                .peek(row -> row.put("giaMax", giaMax))
                .toList();
    }

    @GetMapping("/product-details/{id}")
    public Map<String, Object> getProductDetail(@PathVariable String id) {
        List<Map<String, Object>> rows = getProductDetailsByIds(List.of(id));
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    @PostMapping("/products/{id}/rating")
    public void updateProductRating(
            @PathVariable String id,
            @RequestParam double average,
            @RequestParam long count
    ) {
        sanPhamRepository.findById(id).ifPresent(product -> {
            product.setRatingAverage(Math.max(0D, Math.min(5D, average)));
            product.setRatingCount(Math.max(0L, count));
            sanPhamRepository.save(product);
        });
    }

    @PostMapping("/product-details/{id}/stock/adjust")
    public void adjustStock(@PathVariable String id, @RequestParam int delta) {
        sanPhamChiTietRepository.findById(id).ifPresent(detail -> {
            detail.setQuantity((detail.getQuantity() == null ? 0 : detail.getQuantity()) + delta);
            sanPhamChiTietRepository.save(detail);
        });
    }

    @GetMapping("/colors")
    public List<Map<String, Object>> getColors() {
        return mauSacRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE).stream().map(this::colorMap).toList();
    }

    @GetMapping("/sizes")
    public List<Map<String, Object>> getSizes() {
        return kichCoRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE).stream().map(this::sizeMap).toList();
    }

    private Map<String, Object> productMap(Product product) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", product.getId());
        row.put("code", product.getCode());
        row.put("name", product.getName());
        row.put("description", product.getDescription());
        row.put("status", product.getStatus() == null ? null : product.getStatus().ordinal());
        row.put("sellerId", product.getSellerId());
        row.put("ratingAverage", product.getRatingAverage());
        row.put("ratingCount", product.getRatingCount());
        return row;
    }

    private Map<String, Object> productDetailMap(ProductVariant detail) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", detail.getId());
        row.put("code", detail.getCode());
        row.put("salePrice", detail.getSalePrice());
        row.put("imageUrl", detail.getImageUrl());
        row.put("quantity", detail.getQuantity());
        row.put("status", detail.getStatus() == null ? null : detail.getStatus().ordinal());
        row.put("sellerId", detail.getSellerId());
        row.put("productId", detail.getProduct() == null ? null : detail.getProduct().getId());
        row.put("ratingAverage", detail.getProduct() == null ? null : detail.getProduct().getRatingAverage());
        row.put("ratingCount", detail.getProduct() == null ? 0L : detail.getProduct().getRatingCount());
        row.put("tenProduct", detail.getProduct() == null ? null : detail.getProduct().getName());
        row.put("name", detail.getProduct() == null ? null : detail.getProduct().getName());
        row.put("tenBrand", detail.getProduct() == null || detail.getProduct().getBrand() == null ? null : detail.getProduct().getBrand().getName());
        row.put("tenSoleType", detail.getProduct() == null || detail.getProduct().getSoleType() == null ? null : detail.getProduct().getSoleType().getName());
        row.put("tenMaterial", detail.getProduct() == null || detail.getProduct().getMaterial() == null ? null : detail.getProduct().getMaterial().getName());
        row.put("tenCategory", detail.getProduct() == null || detail.getProduct().getCategory() == null ? null : detail.getProduct().getCategory().getName());
        row.put("sizeId", detail.getSize() == null ? null : detail.getSize().getId());
        row.put("tenSize", detail.getSize() == null ? null : detail.getSize().getName());
        row.put("kichThuoc", detail.getSize() == null ? null : detail.getSize().getName());
        row.put("colorId", detail.getColor() == null ? null : detail.getColor().getId());
        row.put("tenColor", detail.getColor() == null ? null : detail.getColor().getName());
        row.put("tenMau", detail.getColor() == null ? null : detail.getColor().getName());
        row.put("mau", detail.getColor() == null ? null : detail.getColor().getMau());
        return row;
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }

    private Map<String, Object> colorMap(Color color) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", color.getId());
        row.put("code", color.getCode());
        row.put("name", color.getName());
        row.put("mau", color.getMau());
        row.put("status", color.getStatus() == null ? null : color.getStatus().ordinal());
        return row;
    }

    private Map<String, Object> sizeMap(Size size) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", size.getId());
        row.put("code", size.getCode());
        row.put("name", size.getName());
        row.put("status", size.getStatus() == null ? null : size.getStatus().ordinal());
        return row;
    }
}
