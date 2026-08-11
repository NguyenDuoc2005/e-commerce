package com.ecommerce.catalog.service;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.document.ProductDocument;
import com.ecommerce.catalog.entity.SanPham;
import com.ecommerce.catalog.entity.SanPhamChiTiet;
import com.ecommerce.catalog.repository.SanPhamChiTietRepository;
import com.ecommerce.catalog.repository.SanPhamRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductSearchIndexer {

    private final SanPhamRepository sanPhamRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    public ProductSearchIndexer(SanPhamRepository sanPhamRepository, SanPhamChiTietRepository sanPhamChiTietRepository) {
        this.sanPhamRepository = sanPhamRepository;
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
    }

    public Optional<ProductDocument> buildDocument(String productId) {
        if (productId == null || productId.isBlank()) {
            return Optional.empty();
        }
        return sanPhamRepository.findById(productId).flatMap(this::buildDocument);
    }

    public Optional<ProductDocument> buildDocument(SanPham product) {
        if (product == null || product.getStatus() != EntityStatus.ACTIVE) {
            return Optional.empty();
        }

        List<SanPhamChiTiet> details = sanPhamChiTietRepository.findBySanPhamIdAndStatusOrderByCreatedDateDesc(product.getId(), EntityStatus.ACTIVE);
        if (details.isEmpty()) {
            return Optional.empty();
        }

        ProductDocument document = new ProductDocument();
        document.setId(product.getId());
        document.setName(product.getTen());
        document.setDescription(product.getMoTa());
        document.setCategoryId(product.getDanhMuc() == null ? null : product.getDanhMuc().getId());
        document.setCategory(product.getDanhMuc() == null ? null : product.getDanhMuc().getTen());
        document.setBrandId(product.getThuongHieu() == null ? null : product.getThuongHieu().getId());
        document.setBrand(product.getThuongHieu() == null ? null : product.getThuongHieu().getTen());
        document.setPrice(details.stream()
                .map(SanPhamChiTiet::getGiaBan)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(0D));
        document.setImageUrl(details.stream()
                .map(SanPhamChiTiet::getAnh)
                .filter(java.util.Objects::nonNull)
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElse(null));
        return Optional.of(document);
    }
}
