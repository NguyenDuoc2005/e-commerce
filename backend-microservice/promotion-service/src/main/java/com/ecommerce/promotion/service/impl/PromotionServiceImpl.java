package com.ecommerce.promotion.service.impl;

import com.ecommerce.common.base.PageableObject;
import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.util.PageUtils;
import com.ecommerce.promotion.client.CatalogClient;
import com.ecommerce.promotion.constant.Status;
import com.ecommerce.promotion.constant.StatusPromotion;
import com.ecommerce.promotion.entity.DotGiamGia;
import com.ecommerce.promotion.entity.DotGiamGiaChiTietSanPham;
import com.ecommerce.promotion.model.request.CreatePromotionRequest;
import com.ecommerce.promotion.model.request.FindPromotionRequest;
import com.ecommerce.promotion.model.request.IdProductDetail;
import com.ecommerce.promotion.model.request.UpdatePromotionRequest;
import com.ecommerce.promotion.model.response.PromotionByIdResponse;
import com.ecommerce.promotion.repository.PromotionDetailRepository;
import com.ecommerce.promotion.repository.PromotionRepository;
import com.ecommerce.promotion.service.PromotionService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionDetailRepository detailRepository;
    private final CatalogClient catalogClient;

    public PromotionServiceImpl(
            PromotionRepository promotionRepository,
            PromotionDetailRepository detailRepository,
            CatalogClient catalogClient
    ) {
        this.promotionRepository = promotionRepository;
        this.detailRepository = detailRepository;
        this.catalogClient = catalogClient;
    }

    @Override
    public ResponseObject<?> getAll(FindPromotionRequest request) {
        Pageable pageable = PageUtils.createPageable(request, "createdDate");
        return new ResponseObject<>(PageableObject.of(promotionRepository.getAllDotGiamGia(request, pageable)), HttpStatus.OK, "Lay danh sach dot giam gia thanh cong");
    }

    @Override
    public List<Map<String, Object>> getSanPham() {
        return catalogClient.getProducts();
    }

    @Override
    public List<Map<String, Object>> getSanPhamCT(String id) {
        return catalogClient.getProductDetails(id);
    }

    @Override
    public List<Map<String, Object>> getSanPhamByDot(String id) {
        List<String> ids = detailRepository.findActiveProductDetailIdsByPromotion(id);
        return ids.isEmpty() ? List.of() : catalogClient.getProductDetailsByIds(ids);
    }

    @Override
    public List<Map<String, Object>> getMauSac() {
        return catalogClient.getColors();
    }

    @Override
    public List<Map<String, Object>> getKichCo() {
        return catalogClient.getSizes();
    }

    @Override
    @Transactional
    public DotGiamGia add(CreatePromotionRequest request) {
        if (promotionRepository.findByTen(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Ten khuyen mai da ton tai");
        }
        if (request.getIdProductDetails() == null || request.getIdProductDetails().isEmpty()) {
            throw new IllegalArgumentException("Khong co san pham");
        }
        validateProductDetails(request.getIdProductDetails());
        validateDates(request.getStartDate(), request.getEndDate(), true);

        DotGiamGia promotion = new DotGiamGia();
        promotion.setMa("KM" + UUID.randomUUID().toString().replace("-", "").substring(0, 9));
        promotion.setTen(request.getName());
        promotion.setPhanTramGiam(request.getValue());
        promotion.setNgayBatDau(request.getStartDate());
        promotion.setNgayKetThuc(request.getEndDate());
        promotion.setTrangThai(getStatusPromotion(request.getStartDate(), request.getEndDate()));
        promotionRepository.save(promotion);

        createOrUpdateDetails(promotion, request.getIdProductDetails(), request.getValue(), Status.DANG_SU_DUNG);
        return promotion;
    }

    @Override
    @Transactional
    public DotGiamGia update(UpdatePromotionRequest request) {
        DotGiamGia promotion = promotionRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("Khuyen mai khong ton tai"));
        validateProductDetails(request.getIdProductDetails());
        validateDates(request.getStartDate(), request.getEndDate(), false);

        promotion.setTen(request.getName());
        promotion.setPhanTramGiam(request.getValue());
        promotion.setNgayBatDau(request.getStartDate());
        promotion.setNgayKetThuc(request.getEndDate());
        promotion.setTrangThai(getStatusPromotion(request.getStartDate(), request.getEndDate()));
        promotionRepository.save(promotion);

        for (DotGiamGiaChiTietSanPham oldDetail : detailRepository.findAllByIdPromotion(promotion.getId())) {
            oldDetail.setTrangThai(Status.KHONG_SU_DUNG);
            detailRepository.save(oldDetail);
        }
        createOrUpdateDetails(promotion, request.getIdProductDetails(), request.getValue(), getStatus(promotion.getTrangThai()));
        return promotion;
    }

    @Override
    public DotGiamGia updateStatus(String id) {
        DotGiamGia promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khuyen mai khong ton tai"));
        promotion.setTrangThai(getStatusPromotion(promotion.getNgayBatDau(), promotion.getNgayKetThuc()));
        promotionRepository.save(promotion);
        updateProductDetailsStatus(promotion.getId(), promotion.getTrangThai());
        return promotion;
    }

    @Override
    public PromotionByIdResponse getByIdPromotion(String id) {
        return promotionRepository.getByIdPromotion(id);
    }

    @Override
    public List<Map<String, Object>> getByIdProductDetail(String id) {
        Map<String, Object> productDetail = catalogClient.getProductDetail(id);
        return detailRepository.findAllByProductDetailId(id).stream()
                .map(detail -> {
                    Map<String, Object> row = new java.util.LinkedHashMap<>();
                    row.put("image", productDetail.get("anh"));
                    row.put("code", productDetail.get("ma"));
                    row.put("name", productDetail.get("tenSanPham"));
                    row.put("namePromotion", detail.getDotGiamGia().getTen());
                    row.put("valuePromotion", detail.getDotGiamGia().getPhanTramGiam());
                    row.put("statusPromotion", detail.getTrangThai() == null ? null : detail.getTrangThai().name());
                    return row;
                })
                .toList();
    }

    private void validateProductDetails(List<IdProductDetail> ids) {
        for (IdProductDetail item : ids) {
            if (catalogClient.getProductDetail(item.getId()).isEmpty()) {
                throw new IllegalArgumentException("Co san pham khong ton tai");
            }
        }
    }

    private void validateDates(long startDate, long endDate, boolean rejectPastStart) {
        LocalDate today = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate start = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate();
        if (rejectPastStart && start.isBefore(today)) {
            throw new IllegalArgumentException("Ngay bat dau khong duoc nam trong qua khu");
        }
        if (endDate < startDate) {
            throw new IllegalArgumentException("Ngay ket thuc phai lon hon ngay bat dau");
        }
    }

    private void createOrUpdateDetails(DotGiamGia promotion, List<IdProductDetail> ids, Double value, Status status) {
        List<DotGiamGiaChiTietSanPham> details = new ArrayList<>();
        for (IdProductDetail item : ids) {
            Map<String, Object> productDetail = catalogClient.getProductDetail(item.getId());
            DotGiamGiaChiTietSanPham detail = Optional.ofNullable(detailRepository.getByProductDetailAndPromotion(item.getId(), promotion.getId()))
                    .orElseGet(DotGiamGiaChiTietSanPham::new);
            double original = doubleValue(productDetail.get("giaBan"));
            detail.setMa(detail.getMa() == null ? "DGCTSP-" + UUID.randomUUID() : detail.getMa());
            detail.setDotGiamGia(promotion);
            detail.setSanPhamChiTietId(item.getId());
            detail.setTrangThai(status);
            detail.setGiaTruoc(original);
            detail.setGiaSau(roundTo2Decimals(original - (original * value / 100)));
            details.add(detail);
        }
        detailRepository.saveAll(details);
    }

    private StatusPromotion getStatusPromotion(long startDate, long endDate) {
        LocalDateTime current = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime start = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime end = Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (start.isAfter(current)) {
            return StatusPromotion.CHUA_KICH_HOAT;
        }
        if (end.isBefore(current)) {
            return StatusPromotion.HET_HAN_KICH_HOAT;
        }
        return StatusPromotion.DANG_KICH_HOAT;
    }

    private Status getStatus(StatusPromotion status) {
        return status == StatusPromotion.HET_HAN_KICH_HOAT ? Status.KHONG_SU_DUNG : Status.DANG_SU_DUNG;
    }

    private boolean updateProductDetailsStatus(String idPromotion, StatusPromotion status) {
        if (!status.equals(StatusPromotion.HET_HAN_KICH_HOAT)) {
            return false;
        }
        for (DotGiamGiaChiTietSanPham detail : detailRepository.findAllByIdPromotion(idPromotion)) {
            detail.setTrangThai(Status.KHONG_SU_DUNG);
            detailRepository.save(detail);
        }
        return true;
    }

    private double roundTo2Decimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double doubleValue(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return value == null ? 0D : Double.parseDouble(String.valueOf(value));
    }
}
