package com.ecommerce.promotion.service;

import com.ecommerce.common.catalog.CatalogVariantSnapshot;
import com.ecommerce.promotion.client.CatalogClient;
import com.ecommerce.promotion.constant.CampaignType;
import com.ecommerce.promotion.constant.RegistrationStatus;
import com.ecommerce.promotion.constant.Status;
import com.ecommerce.promotion.constant.StatusPromotion;
import com.ecommerce.promotion.entity.PromotionCampaign;
import com.ecommerce.promotion.entity.PromotionCampaignProduct;
import com.ecommerce.promotion.model.request.FlashSaleCampaignRequest;
import com.ecommerce.promotion.model.request.FlashSaleRegistrationRequest;
import com.ecommerce.promotion.model.request.FlashSaleReviewRequest;
import com.ecommerce.promotion.repository.PromotionDetailRepository;
import com.ecommerce.promotion.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class FlashSaleService {
    private final PromotionRepository campaignRepository;
    private final PromotionDetailRepository registrationRepository;
    private final CatalogClient catalogClient;

    public FlashSaleService(PromotionRepository campaignRepository,
                            PromotionDetailRepository registrationRepository,
                            CatalogClient catalogClient) {
        this.campaignRepository = campaignRepository;
        this.registrationRepository = registrationRepository;
        this.catalogClient = catalogClient;
    }

    public List<Map<String, Object>> adminCampaigns() {
        return campaigns().stream().map(campaign -> campaignMap(campaign, null, true)).toList();
    }

    @Transactional
    public Map<String, Object> createCampaign(String staffId, FlashSaleCampaignRequest request) {
        validateCampaign(request);
        PromotionCampaign campaign = new PromotionCampaign();
        campaign.setCode("FS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT));
        campaign.setName(request.getName().trim());
        campaign.setDescription(trimToNull(request.getDescription()));
        campaign.setDiscountValue(0D);
        campaign.setSellerId(null);
        campaign.setCampaignType(CampaignType.FLASH_SALE);
        campaign.setRegistrationStartDate(request.getRegistrationStartDate());
        campaign.setRegistrationEndDate(request.getRegistrationEndDate());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setCreatedByStaffId(staffId);
        campaign.setTrangThai(status(request.getStartDate(), request.getEndDate()));
        campaignRepository.save(campaign);
        return campaignMap(campaign, null, true);
    }

    @Transactional
    public Map<String, Object> updateCampaign(String id, FlashSaleCampaignRequest request) {
        validateCampaign(request);
        PromotionCampaign campaign = requireCampaign(id);
        if (System.currentTimeMillis() >= campaign.getStartDate()) {
            throw new IllegalArgumentException("Khong the sua Flash sale da bat dau");
        }
        campaign.setName(request.getName().trim());
        campaign.setDescription(trimToNull(request.getDescription()));
        campaign.setRegistrationStartDate(request.getRegistrationStartDate());
        campaign.setRegistrationEndDate(request.getRegistrationEndDate());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setTrangThai(status(request.getStartDate(), request.getEndDate()));
        campaignRepository.save(campaign);
        return campaignMap(campaign, null, true);
    }

    public List<Map<String, Object>> adminRegistrations(String campaignId) {
        requireCampaign(campaignId);
        return registrations(campaignId).stream().map(this::registrationMap).toList();
    }

    @Transactional
    public Map<String, Object> review(String campaignId, String registrationId, String staffId,
                                      FlashSaleReviewRequest request) {
        requireCampaign(campaignId);
        PromotionCampaignProduct registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay dang ky"));
        if (!campaignId.equals(registration.getPromotionCampaign().getId())) {
            throw new IllegalArgumentException("Dang ky khong thuoc Flash sale nay");
        }
        if (registration.getRegistrationStatus() != RegistrationStatus.PENDING) {
            throw new IllegalArgumentException("Chi duyet dang ky dang cho xu ly");
        }
        String decision = request.getDecision().trim().toUpperCase(Locale.ROOT);
        if ("APPROVE".equals(decision)) {
            CatalogVariantSnapshot variant = catalogClient.getProductVariant(registration.getProductVariantId());
            if (variant == null || !registration.getSellerId().equals(variant.sellerId())) {
                throw new IllegalArgumentException("San pham khong con thuoc seller dang ky");
            }
            validateFlashPrice(registration.getPriceAfterDiscount(), variant.salePrice().doubleValue());
            registration.setRegistrationStatus(RegistrationStatus.APPROVED);
            registration.setTrangThai(Status.DANG_SU_DUNG);
            registration.setRejectionReason(null);
        } else if ("REJECT".equals(decision)) {
            if (request.getReason() == null || request.getReason().isBlank()) {
                throw new IllegalArgumentException("Can nhap ly do tu choi");
            }
            registration.setRegistrationStatus(RegistrationStatus.REJECTED);
            registration.setTrangThai(Status.KHONG_SU_DUNG);
            registration.setRejectionReason(request.getReason().trim());
        } else {
            throw new IllegalArgumentException("Quyet dinh phai la APPROVE hoac REJECT");
        }
        registration.setReviewedByStaffId(staffId);
        registration.setReviewedAt(System.currentTimeMillis());
        registrationRepository.save(registration);
        return registrationMap(registration);
    }

    public List<Map<String, Object>> sellerCampaigns(String sellerId) {
        long now = System.currentTimeMillis();
        return campaigns().stream()
                .filter(campaign -> campaign.getEndDate() >= now)
                .map(campaign -> campaignMap(campaign, sellerId, false))
                .toList();
    }

    public List<Map<String, Object>> sellerRegistrations(String sellerId) {
        return registrationRepository.findBySellerIdOrderByCreatedDateDesc(sellerId).stream()
                .filter(row -> row.getPromotionCampaign() != null
                        && row.getPromotionCampaign().getCampaignType() == CampaignType.FLASH_SALE)
                .map(this::registrationMap).toList();
    }

    public List<Map<String, Object>> sellerVariants(String sellerId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> product : catalogClient.getProducts()) {
            if (!sellerId.equals(stringValue(product.get("sellerId")))) continue;
            String productId = stringValue(product.get("id"));
            if (productId == null) continue;
            for (CatalogVariantSnapshot variant : catalogClient.getProductVariants(productId)) {
                if (!sellerId.equals(variant.sellerId()) || !"ACTIVE".equalsIgnoreCase(variant.status())) continue;
                result.add(variantMap(variant));
            }
        }
        return result;
    }

    @Transactional
    public Map<String, Object> register(String campaignId, String sellerId, FlashSaleRegistrationRequest request) {
        PromotionCampaign campaign = requireCampaign(campaignId);
        requireRegistrationWindow(campaign);
        CatalogVariantSnapshot variant = catalogClient.getProductVariant(request.getProductVariantId());
        if (variant == null || !sellerId.equals(variant.sellerId())) {
            throw new SecurityException("San pham khong thuoc shop hien tai");
        }
        if (!"ACTIVE".equalsIgnoreCase(variant.status()) || variant.quantity() == null || variant.quantity() <= 0) {
            throw new IllegalArgumentException("San pham khong dang ban hoac da het hang");
        }
        validateFlashPrice(request.getFlashPrice(), variant.salePrice().doubleValue());

        PromotionCampaignProduct registration = registrationRepository
                .findByPromotionCampaign_IdAndProductVariantId(campaignId, request.getProductVariantId());
        if (registration == null) {
            registration = new PromotionCampaignProduct();
            registration.setCode("FSREG-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT));
            registration.setPromotionCampaign(campaign);
            registration.setProductVariantId(request.getProductVariantId());
        } else if (registration.getRegistrationStatus() == RegistrationStatus.PENDING
                || registration.getRegistrationStatus() == RegistrationStatus.APPROVED) {
            throw new IllegalArgumentException("San pham da duoc dang ky vao Flash sale nay");
        }
        registration.setSellerId(sellerId);
        registration.setPriceBeforeDiscount(variant.salePrice().doubleValue());
        registration.setPriceAfterDiscount(request.getFlashPrice());
        registration.setRegistrationStatus(RegistrationStatus.PENDING);
        registration.setTrangThai(Status.CHUA_KICH_HOAT);
        registration.setRejectionReason(null);
        registration.setReviewedAt(null);
        registration.setReviewedByStaffId(null);
        registrationRepository.save(registration);
        return registrationMap(registration);
    }

    @Transactional
    public Map<String, Object> withdraw(String campaignId, String registrationId, String sellerId) {
        PromotionCampaignProduct registration = requireSellerRegistration(campaignId, registrationId, sellerId);
        if (registration.getRegistrationStatus() == RegistrationStatus.APPROVED
                && System.currentTimeMillis() >= registration.getPromotionCampaign().getStartDate()) {
            throw new IllegalArgumentException("Khong the rut san pham khi Flash sale da bat dau");
        }
        registration.setRegistrationStatus(RegistrationStatus.WITHDRAWN);
        registration.setTrangThai(Status.KHONG_SU_DUNG);
        registrationRepository.save(registration);
        return registrationMap(registration);
    }

    public List<Map<String, Object>> publicCampaigns() {
        long now = System.currentTimeMillis();
        return campaigns().stream()
                .filter(campaign -> campaign.getEndDate() >= now)
                .map(campaign -> campaignMap(campaign, null, true))
                .toList();
    }

    private List<PromotionCampaign> campaigns() {
        List<PromotionCampaign> campaigns = campaignRepository.findByCampaignTypeOrderByStartDateDesc(CampaignType.FLASH_SALE);
        campaigns.forEach(this::syncStatus);
        return campaigns;
    }

    private PromotionCampaign requireCampaign(String id) {
        PromotionCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay Flash sale"));
        if (campaign.getCampaignType() != CampaignType.FLASH_SALE) {
            throw new IllegalArgumentException("Campaign khong phai Flash sale toan san");
        }
        syncStatus(campaign);
        return campaign;
    }

    private void syncStatus(PromotionCampaign campaign) {
        StatusPromotion next = status(campaign.getStartDate(), campaign.getEndDate());
        if (campaign.getTrangThai() != next) {
            campaign.setTrangThai(next);
            campaignRepository.save(campaign);
        }
    }

    private PromotionCampaignProduct requireSellerRegistration(String campaignId, String registrationId, String sellerId) {
        PromotionCampaignProduct registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay dang ky"));
        if (registration.getPromotionCampaign() == null
                || !campaignId.equals(registration.getPromotionCampaign().getId())
                || !sellerId.equals(registration.getSellerId())) {
            throw new SecurityException("Dang ky khong thuoc shop hien tai");
        }
        return registration;
    }

    private void validateCampaign(FlashSaleCampaignRequest request) {
        if (request.getRegistrationStartDate() > request.getRegistrationEndDate()) {
            throw new IllegalArgumentException("Thoi gian mo dang ky khong hop le");
        }
        if (request.getRegistrationEndDate() > request.getStartDate()) {
            throw new IllegalArgumentException("Dang ky phai dong truoc khi Flash sale bat dau");
        }
        if (request.getStartDate() >= request.getEndDate()) {
            throw new IllegalArgumentException("Thoi gian Flash sale khong hop le");
        }
    }

    private void requireRegistrationWindow(PromotionCampaign campaign) {
        long now = System.currentTimeMillis();
        if (campaign.getRegistrationStartDate() == null || campaign.getRegistrationEndDate() == null
                || now < campaign.getRegistrationStartDate() || now > campaign.getRegistrationEndDate()) {
            throw new IllegalArgumentException("Flash sale khong trong thoi gian nhan dang ky");
        }
    }

    private void validateFlashPrice(Double flashPrice, double originalPrice) {
        if (flashPrice == null || flashPrice <= 0 || flashPrice >= originalPrice) {
            throw new IllegalArgumentException("Gia Flash sale phai lon hon 0 va nho hon gia dang ban");
        }
    }

    private StatusPromotion status(Long startDate, Long endDate) {
        long now = System.currentTimeMillis();
        if (now < startDate) return StatusPromotion.CHUA_KICH_HOAT;
        if (now > endDate) return StatusPromotion.HET_HAN_KICH_HOAT;
        return StatusPromotion.DANG_KICH_HOAT;
    }

    private List<PromotionCampaignProduct> registrations(String campaignId) {
        return registrationRepository.findByPromotionCampaign_IdOrderByCreatedDateDesc(campaignId);
    }

    private Map<String, Object> campaignMap(PromotionCampaign campaign, String sellerId, boolean includeProducts) {
        List<PromotionCampaignProduct> rows = registrations(campaign.getId());
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", campaign.getId());
        map.put("code", campaign.getCode());
        map.put("name", campaign.getName());
        map.put("description", campaign.getDescription());
        map.put("registrationStartDate", campaign.getRegistrationStartDate());
        map.put("registrationEndDate", campaign.getRegistrationEndDate());
        map.put("startDate", campaign.getStartDate());
        map.put("endDate", campaign.getEndDate());
        map.put("status", status(campaign.getStartDate(), campaign.getEndDate()).name());
        map.put("registrationOpen", isRegistrationOpen(campaign));
        map.put("pendingCount", rows.stream().filter(row -> row.getRegistrationStatus() == RegistrationStatus.PENDING).count());
        map.put("approvedCount", rows.stream().filter(row -> row.getRegistrationStatus() == RegistrationStatus.APPROVED).count());
        if (sellerId != null) {
            map.put("sellerRegistrationCount", rows.stream().filter(row -> sellerId.equals(row.getSellerId())
                    && row.getRegistrationStatus() != RegistrationStatus.WITHDRAWN).count());
        }
        if (includeProducts) {
            map.put("products", rows.stream()
                    .filter(row -> row.getRegistrationStatus() == RegistrationStatus.APPROVED
                            && row.getTrangThai() == Status.DANG_SU_DUNG)
                    .map(this::registrationMap).toList());
        }
        return map;
    }

    private boolean isRegistrationOpen(PromotionCampaign campaign) {
        long now = System.currentTimeMillis();
        return campaign.getRegistrationStartDate() != null && campaign.getRegistrationEndDate() != null
                && now >= campaign.getRegistrationStartDate() && now <= campaign.getRegistrationEndDate();
    }

    private Map<String, Object> registrationMap(PromotionCampaignProduct registration) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", registration.getId());
        map.put("campaignId", registration.getPromotionCampaign() == null ? null : registration.getPromotionCampaign().getId());
        map.put("campaignName", registration.getPromotionCampaign() == null ? null : registration.getPromotionCampaign().getName());
        map.put("sellerId", registration.getSellerId());
        map.put("productVariantId", registration.getProductVariantId());
        map.put("priceBeforeDiscount", registration.getPriceBeforeDiscount());
        map.put("flashPrice", registration.getPriceAfterDiscount());
        map.put("discountPercent", discountPercent(registration));
        map.put("registrationStatus", registration.getRegistrationStatus());
        map.put("rejectionReason", registration.getRejectionReason());
        map.put("reviewedByStaffId", registration.getReviewedByStaffId());
        map.put("reviewedAt", registration.getReviewedAt());
        map.put("createdDate", registration.getCreatedDate());
        try {
            CatalogVariantSnapshot variant = catalogClient.getProductVariant(registration.getProductVariantId());
            map.putAll(variantMap(variant));
        } catch (RuntimeException ignored) {
            map.put("productName", "San pham khong con ton tai");
        }
        return map;
    }

    private Map<String, Object> variantMap(CatalogVariantSnapshot variant) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("productId", variant.productId());
        map.put("productVariantId", variant.id());
        map.put("sellerId", variant.sellerId());
        map.put("sku", variant.sku());
        map.put("productName", variant.productName());
        map.put("variantLabel", variant.variantLabel());
        map.put("salePrice", variant.salePrice());
        map.put("quantity", variant.quantity());
        map.put("imageUrl", variant.imageUrl());
        return map;
    }

    private double discountPercent(PromotionCampaignProduct registration) {
        double before = registration.getPriceBeforeDiscount() == null ? 0D : registration.getPriceBeforeDiscount();
        double after = registration.getPriceAfterDiscount() == null ? before : registration.getPriceAfterDiscount();
        return before <= 0 ? 0D : Math.round((before - after) * 10000D / before) / 100D;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
