package com.ecommerce.promotion.service.impl;

import com.ecommerce.common.base.PageableObject;
import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.catalog.CatalogVariantSnapshot;
import com.ecommerce.promotion.client.CatalogClient;
import com.ecommerce.promotion.constant.Status;
import com.ecommerce.promotion.constant.StatusPromotion;
import com.ecommerce.promotion.entity.PromotionCampaign;
import com.ecommerce.promotion.entity.PromotionCampaignProduct;
import com.ecommerce.promotion.model.request.CreatePromotionRequest;
import com.ecommerce.promotion.model.request.FindPromotionRequest;
import com.ecommerce.promotion.model.request.IdProductDetail;
import com.ecommerce.promotion.model.request.UpdatePromotionRequest;
import com.ecommerce.promotion.model.response.PromotionByIdResponse;
import com.ecommerce.promotion.repository.PromotionDetailRepository;
import com.ecommerce.promotion.repository.PromotionRepository;
import com.ecommerce.promotion.service.PromotionService;
import org.springframework.data.domain.PageRequest;
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
        Pageable pageable = legacyCampaignPageable(request);
        request.setPlatformOnly(true);
        request.setSellerId(null);
        return new ResponseObject<>(PageableObject.of(promotionRepository.getAllPromotionCampaign(request, pageable)), HttpStatus.OK, "Lay danh sach campaign san thanh cong");
    }

    @Override
    public List<Map<String, Object>> getProduct() {
        return catalogClient.getProducts();
    }

    @Override
    public List<Map<String, Object>> getProductVariants(String productId) {
        return catalogClient.getProductVariants(productId).stream().map(PromotionServiceImpl::variantMap).toList();
    }

    @Override
    public List<Map<String, Object>> getProductVariantsByCampaign(String campaignId) {
        List<String> ids = detailRepository.findActiveProductDetailIdsByPromotion(campaignId);
        return ids.isEmpty() ? List.of() : catalogClient.getProductVariantsByIds(ids).stream()
                .map(PromotionServiceImpl::variantMap).toList();
    }

    @Override
    @Transactional
    public PromotionCampaign add(CreatePromotionRequest request) {
        request.setSellerId(null);
        return addScoped(null, request);
    }

    @Override
    public ResponseObject<?> getSellerAll(String sellerId, FindPromotionRequest request) {
        Pageable pageable = legacyCampaignPageable(request);
        request.setSellerId(sellerId);
        request.setPlatformOnly(false);
        return new ResponseObject<>(PageableObject.of(promotionRepository.getAllPromotionCampaign(request, pageable)), HttpStatus.OK, "Lay danh sach campaign shop thanh cong");
    }

    private Pageable legacyCampaignPageable(FindPromotionRequest request) {
        int page = Math.max(request.getPage() - 1, 0);
        int size = request.getSize() == 0 ? 10 : request.getSize();
        return PageRequest.of(page, size);
    }

    @Override
    @Transactional
    public PromotionCampaign addSeller(String sellerId, CreatePromotionRequest request) {
        request.setSellerId(sellerId);
        return addScoped(sellerId, request);
    }

    private PromotionCampaign addScoped(String sellerId, CreatePromotionRequest request) {
        if (promotionRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Ten kdistrict mai da ton tai");
        }
        if (request.getIdProductDetails() == null || request.getIdProductDetails().isEmpty()) {
            throw new IllegalArgumentException("Khong co san pham");
        }
        validateProductDetails(request.getIdProductDetails(), sellerId);
        validateDates(request.getStartDate(), request.getEndDate(), true);

        PromotionCampaign promotion = new PromotionCampaign();
        promotion.setCode("KM" + UUID.randomUUID().toString().replace("-", "").substring(0, 9));
        promotion.setName(request.getName());
        promotion.setDiscountValue(request.getValue());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setTrangThai(getStatusPromotion(request.getStartDate(), request.getEndDate()));
        promotion.setSellerId(sellerId);
        promotionRepository.save(promotion);

        createOrUpdateDetails(promotion, request.getIdProductDetails(), request.getValue(), Status.DANG_SU_DUNG);
        return promotion;
    }

    @Override
    @Transactional
    public PromotionCampaign update(UpdatePromotionRequest request) {
        request.setSellerId(null);
        return updateScoped(null, request);
    }

    @Override
    @Transactional
    public PromotionCampaign updateSeller(String sellerId, UpdatePromotionRequest request) {
        request.setSellerId(sellerId);
        return updateScoped(sellerId, request);
    }

    private PromotionCampaign updateScoped(String sellerId, UpdatePromotionRequest request) {
        PromotionCampaign promotion = promotionRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("Kdistrict mai khong ton tai"));
        if (!sameScope(promotion.getSellerId(), sellerId)) {
            throw new IllegalArgumentException("Khong co quyen cap nhat campaign nay");
        }
        validateProductDetails(request.getIdProductDetails(), sellerId);
        validateDates(request.getStartDate(), request.getEndDate(), false);

        promotion.setName(request.getName());
        promotion.setDiscountValue(request.getValue());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setTrangThai(getStatusPromotion(request.getStartDate(), request.getEndDate()));
        promotionRepository.save(promotion);

        for (PromotionCampaignProduct oldDetail : detailRepository.findAllByIdPromotion(promotion.getId())) {
            oldDetail.setTrangThai(Status.KHONG_SU_DUNG);
            detailRepository.save(oldDetail);
        }
        createOrUpdateDetails(promotion, request.getIdProductDetails(), request.getValue(), getStatus(promotion.getTrangThai()));
        return promotion;
    }

    @Override
    public PromotionCampaign updateStatus(String id) {
        return updateStatusScoped(null, id);
    }

    @Override
    public PromotionCampaign updateSellerStatus(String sellerId, String id) {
        return updateStatusScoped(sellerId, id);
    }

    private PromotionCampaign updateStatusScoped(String sellerId, String id) {
        PromotionCampaign promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kdistrict mai khong ton tai"));
        if (!sameScope(promotion.getSellerId(), sellerId)) {
            throw new IllegalArgumentException("Khong co quyen doi trang thai campaign nay");
        }
        promotion.setTrangThai(getStatusPromotion(promotion.getStartDate(), promotion.getEndDate()));
        promotionRepository.save(promotion);
        updateProductDetailsStatus(promotion.getId(), promotion.getTrangThai());
        return promotion;
    }

    @Override
    public PromotionByIdResponse getByIdPromotion(String id) {
        return promotionRepository.getByIdPromotion(id);
    }

    private void validateProductDetails(List<IdProductDetail> ids, String sellerId) {
        for (IdProductDetail item : ids) {
            CatalogVariantSnapshot productDetail = catalogClient.getProductVariant(item.getId());
            if (productDetail == null) {
                throw new IllegalArgumentException("Co san pham khong ton tai");
            }
            if (sellerId != null && !sellerId.equals(productDetail.sellerId())) {
                throw new IllegalArgumentException("San pham khong thuoc seller hien tai");
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

    private void createOrUpdateDetails(PromotionCampaign promotion, List<IdProductDetail> ids, Double value, Status status) {
        List<PromotionCampaignProduct> details = new ArrayList<>();
        for (IdProductDetail item : ids) {
            CatalogVariantSnapshot productDetail = catalogClient.getProductVariant(item.getId());
            PromotionCampaignProduct detail = Optional.ofNullable(detailRepository.getByProductDetailAndPromotion(item.getId(), promotion.getId()))
                    .orElseGet(PromotionCampaignProduct::new);
            double original = productDetail.salePrice().doubleValue();
            detail.setCode(detail.getCode() == null ? "DGCTSP-" + UUID.randomUUID() : detail.getCode());
            detail.setPromotionCampaign(promotion);
            detail.setProductVariantId(item.getId());
            detail.setTrangThai(status);
            detail.setPriceBeforeDiscount(original);
            detail.setPriceAfterDiscount(roundTo2Decimals(original - (original * value / 100)));
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
        for (PromotionCampaignProduct detail : detailRepository.findAllByIdPromotion(idPromotion)) {
            detail.setTrangThai(Status.KHONG_SU_DUNG);
            detailRepository.save(detail);
        }
        return true;
    }

    private double roundTo2Decimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static Map<String, Object> variantMap(CatalogVariantSnapshot snapshot) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", snapshot.id());
        row.put("productId", snapshot.productId());
        row.put("sellerId", snapshot.sellerId());
        row.put("sku", snapshot.sku());
        row.put("productName", snapshot.productName());
        row.put("variantLabel", snapshot.variantLabel());
        row.put("selections", snapshot.selections());
        row.put("salePrice", snapshot.salePrice());
        row.put("quantity", snapshot.quantity());
        row.put("imageUrl", snapshot.imageUrl());
        row.put("status", snapshot.status());
        return row;
    }

    private boolean sameScope(String currentSellerId, String requestedSellerId) {
        if ((currentSellerId == null || currentSellerId.isBlank()) && (requestedSellerId == null || requestedSellerId.isBlank())) {
            return true;
        }
        return currentSellerId != null && currentSellerId.equals(requestedSellerId);
    }
}
