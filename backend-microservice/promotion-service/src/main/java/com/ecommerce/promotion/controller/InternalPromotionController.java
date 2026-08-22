package com.ecommerce.promotion.controller;

import com.ecommerce.promotion.constant.EntityStatus;
import com.ecommerce.promotion.entity.PromotionCampaignProduct;
import com.ecommerce.promotion.entity.Voucher;
import com.ecommerce.promotion.repository.VoucherCustomerRepository;
import com.ecommerce.promotion.repository.PromotionDetailRepository;
import com.ecommerce.promotion.repository.VoucherRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/promotions")
public class InternalPromotionController {

    private final VoucherRepository voucherRepository;
    private final VoucherCustomerRepository chiTietRepository;
    private final PromotionDetailRepository promotionDetailRepository;

    public InternalPromotionController(VoucherRepository voucherRepository, VoucherCustomerRepository chiTietRepository, PromotionDetailRepository promotionDetailRepository) {
        this.voucherRepository = voucherRepository;
        this.chiTietRepository = chiTietRepository;
        this.promotionDetailRepository = promotionDetailRepository;
    }

    @GetMapping("/vouchers/by-code")
    public Map<String, Object> getVoucherByCode(@RequestParam String code) {
        return voucherRepository.findByCode(code).map(this::voucherMap).orElseGet(Map::of);
    }

    @GetMapping("/vouchers/assigned")
    public boolean isVoucherAssigned(@RequestParam String voucherId, @RequestParam String customerId) {
        return chiTietRepository.findCustomerIdsByVoucherId(voucherId).contains(customerId);
    }

    @PostMapping("/vouchers/decrement")
    public void decrementVoucher(@RequestParam String voucherId) {
        voucherRepository.findById(voucherId).ifPresent(voucher -> {
            voucher.setQuantity((voucher.getQuantity() == null ? 0 : voucher.getQuantity()) - 1);
            voucherRepository.save(voucher);
        });
    }

    @PostMapping("/vouchers/increment")
    public void incrementVoucher(@RequestParam String voucherId) {
        voucherRepository.findById(voucherId).ifPresent(voucher -> {
            voucher.setQuantity((voucher.getQuantity() == null ? 0 : voucher.getQuantity()) + 1);
            voucherRepository.save(voucher);
        });
    }

    @GetMapping("/vouchers/applicable")
    public List<Map<String, Object>> getApplicableVouchers(@RequestParam String customerId) {
        return voucherRepository.findByStatusAndQuantityGreaterThan(EntityStatus.ACTIVE, 0)
                .stream()
                .filter(voucher -> !Boolean.TRUE.equals(voucher.getDiscountType()) || chiTietRepository.findCustomerIdsByVoucherId(voucher.getId()).contains(customerId))
                .map(this::voucherMap)
                .toList();
    }

    @GetMapping("/discounts/active")
    public List<Map<String, Object>> getActiveDiscounts(@RequestParam List<String> productDetailIds) {
        if (productDetailIds == null || productDetailIds.isEmpty()) {
            return List.of();
        }
        return promotionDetailRepository.findActiveDiscounts(productDetailIds, System.currentTimeMillis())
                .stream()
                .map(this::discountMap)
                .toList();
    }

    private Map<String, Object> voucherMap(Voucher voucher) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", voucher.getId());
        row.put("code", voucher.getCode());
        row.put("name", voucher.getName());
        row.put("discount_value", voucher.getDiscountValue());
        row.put("quantity", voucher.getQuantity());
        row.put("start_date", voucher.getStartDate());
        row.put("end_date", voucher.getEndDate());
        row.put("condition_amount", voucher.getConditionAmount());
        row.put("max_discount_amount", voucher.getMaxDiscountAmount());
        row.put("discount_type", voucher.getDiscountType());
        row.put("discount_method", voucher.getDiscountMethod());
        row.put("status", voucher.getStatus() == null ? null : voucher.getStatus().ordinal());
        return row;
    }

    private Map<String, Object> discountMap(PromotionCampaignProduct detail) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("productDetailId", detail.getProductVariantId());
        row.put("name", detail.getPromotionCampaign() == null ? null : detail.getPromotionCampaign().getName());
        row.put("discountValue", detail.getPromotionCampaign() == null ? null : detail.getPromotionCampaign().getDiscountValue());
        row.put("priceBeforeDiscount", detail.getPriceBeforeDiscount());
        row.put("priceAfterDiscount", detail.getPriceAfterDiscount());
        row.put("startDate", detail.getPromotionCampaign() == null ? null : detail.getPromotionCampaign().getStartDate());
        row.put("endDate", detail.getPromotionCampaign() == null ? null : detail.getPromotionCampaign().getEndDate());
        return row;
    }
}
