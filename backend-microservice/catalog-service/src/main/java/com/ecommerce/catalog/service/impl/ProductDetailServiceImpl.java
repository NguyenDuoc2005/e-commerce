package com.ecommerce.catalog.service.impl;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.SanPham;
import com.ecommerce.catalog.entity.SanPhamChiTiet;
import com.ecommerce.catalog.model.request.ProductDetailRequest;
import com.ecommerce.catalog.model.request.ProductDetailSearchRequest;
import com.ecommerce.catalog.model.request.ProductRequest;
import com.ecommerce.catalog.repository.KichCoRepository;
import com.ecommerce.catalog.repository.MauSacRepository;
import com.ecommerce.catalog.repository.SanPhamChiTietRepository;
import com.ecommerce.catalog.repository.SanPhamRepository;
import com.ecommerce.catalog.service.ProductDetailService;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.common.base.PageableObject;
import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.util.PageUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class ProductDetailServiceImpl implements ProductDetailService {

    private final SanPhamChiTietRepository repository;
    private final SanPhamRepository sanPhamRepository;
    private final KichCoRepository kichCoRepository;
    private final MauSacRepository mauSacRepository;
    private final ProductService productService;

    public ProductDetailServiceImpl(
            SanPhamChiTietRepository repository,
            SanPhamRepository sanPhamRepository,
            KichCoRepository kichCoRepository,
            MauSacRepository mauSacRepository,
            ProductService productService
    ) {
        this.repository = repository;
        this.sanPhamRepository = sanPhamRepository;
        this.kichCoRepository = kichCoRepository;
        this.mauSacRepository = mauSacRepository;
        this.productService = productService;
    }

    @Override
    public ResponseObject<?> getAll(ProductDetailSearchRequest request) {
        Pageable pageable = PageUtils.createPageable(request, "createdDate");
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            request.setEntityStatus("0".equals(request.getStatus()) ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        }
        return new ResponseObject<>(PageableObject.of(repository.getAllSanPhamChiTietByFilter(pageable, request)), HttpStatus.OK, "Lay danh sach san pham chi tiet thanh cong");
    }

    @Override
    public ResponseObject<?> changeSanPhamStatus(String id) {
        Optional<SanPhamChiTiet> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return ResponseObject.successForward(HttpStatus.NOT_FOUND, "Khong tim thay san pham");
        }
        SanPhamChiTiet detail = optional.get();
        detail.setStatus(detail.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        repository.save(detail);
        return ResponseObject.successForward(HttpStatus.OK, "Doi trang thai thanh cong");
    }

    @Override
    public ResponseObject<?> getSPCTById(String id) {
        return repository.getSanPhamID(id)
                .map(value -> new ResponseObject<>(value, HttpStatus.OK, "san pham thanh cong"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham"));
    }

    @Override
    public ResponseObject<?> getDetailSPCT(String id) {
        return repository.getSanPhamChiTietID(id)
                .map(value -> new ResponseObject<>(value, HttpStatus.OK, "san pham thanh cong"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham"));
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
    public ResponseObject<?> getListThemSanPham() {
        return new ResponseObject<>(repository.getListThemSP(), HttpStatus.OK, "lay danh sach san pham thanh cong");
    }

    @Override
    public ResponseObject<?> modifySanPham(ProductDetailRequest request) {
        String productId = firstProductId(request.getIdSP());
        if (productId != null) {
            String existingId = repository.checkThemSanPham(request.getIdMau(), request.getIdSize(), productId);
            if (existingId != null) {
                SanPhamChiTiet existing = repository.findById(existingId).orElse(null);
                String productName = existing == null || existing.getSanPham() == null ? "" : existing.getSanPham().getTen();
                return new ResponseObject<>(null, HttpStatus.OK, "San pham " + productName + " voi mau va kich thuoc nay da ton tai se khong duoc them moi");
            }
        }

        SanPhamChiTiet detail = new SanPhamChiTiet();
        applyDetail(detail, request);
        if (productId != null) {
            sanPhamRepository.findById(productId).ifPresent(detail::setSanPham);
        } else {
            ProductRequest productRequest = toProductRequest(request);
            ResponseObject<?> created = productService.modifySanPham(productRequest);
            if (created.getData() instanceof SanPham sanPham) {
                detail.setSanPham(sanPham);
            }
        }
        detail.setStatus(EntityStatus.ACTIVE);
        saveImageIfPresent(detail, request);
        repository.save(detail);
        return new ResponseObject<>(detail, HttpStatus.CREATED, "Tao san pham thanh cong");
    }

    @Override
    public ResponseObject<?> updateSanPham(ProductDetailRequest request) {
        SanPhamChiTiet detail = repository.findById(request.getId()).orElse(null);
        if (detail == null) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham");
        }
        String productId = detail.getSanPham() == null ? null : detail.getSanPham().getId();
        String existingId = productId == null ? null : repository.checkThemSanPham(request.getIdMau(), request.getIdSize(), productId);
        if (existingId != null && !existingId.equals(request.getId())) {
            return new ResponseObject<>(null, HttpStatus.OK, "San pham voi mau va kich thuoc nay da ton tai se khong duoc them moi");
        }
        applyDetail(detail, request);
        saveImageIfPresent(detail, request);
        repository.save(detail);
        return new ResponseObject<>(detail, HttpStatus.CREATED, "cap nhat san pham chi tiet thanh cong");
    }

    private void applyDetail(SanPhamChiTiet detail, ProductDetailRequest request) {
        detail.setGiaBan(request.getGiaBan());
        detail.setSoLuong(request.getSoLuong());
        kichCoRepository.findById(request.getIdSize()).ifPresent(detail::setKichCo);
        mauSacRepository.findById(request.getIdMau()).ifPresent(detail::setMauSac);
    }

    private void saveImageIfPresent(SanPhamChiTiet detail, ProductDetailRequest request) {
        if (request.getAnh() == null || request.getAnh().isEmpty()) {
            return;
        }
        try {
            request.getAnh().getBytes();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Loi khi doc file anh: " + ex.getMessage(), ex);
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
        productRequest.setTen(request.getTen());
        productRequest.setMoTa(request.getMoTa());
        productRequest.setIdThuongHieu(request.getIdThuongHieu());
        productRequest.setIdXuatXu(request.getIdXuatXu());
        productRequest.setIdLoaiDe(request.getIdLoaiDe());
        productRequest.setIdDanhMuc(request.getIdDanhMuc());
        productRequest.setIdChatLieu(request.getIdChatLieu());
        return productRequest;
    }
}
