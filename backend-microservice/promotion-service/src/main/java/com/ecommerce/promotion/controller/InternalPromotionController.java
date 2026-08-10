package com.ecommerce.promotion.controller;

import com.ecommerce.promotion.constant.EntityStatus;
import com.ecommerce.promotion.entity.DotGiamGiaChiTietSanPham;
import com.ecommerce.promotion.entity.PhieuGiamGia;
import com.ecommerce.promotion.repository.PhieuGiamGiaChiTietRepository;
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
    private final PhieuGiamGiaChiTietRepository chiTietRepository;
    private final PromotionDetailRepository promotionDetailRepository;

    public InternalPromotionController(VoucherRepository voucherRepository, PhieuGiamGiaChiTietRepository chiTietRepository, PromotionDetailRepository promotionDetailRepository) {
        this.voucherRepository = voucherRepository;
        this.chiTietRepository = chiTietRepository;
        this.promotionDetailRepository = promotionDetailRepository;
    }

    @GetMapping("/vouchers/by-code")
    public Map<String, Object> getVoucherByCode(@RequestParam String code) {
        return voucherRepository.findByMa(code).map(this::voucherMap).orElseGet(Map::of);
    }

    @GetMapping("/vouchers/assigned")
    public boolean isVoucherAssigned(@RequestParam String voucherId, @RequestParam String customerId) {
        return chiTietRepository.findCustomerIdsByVoucherId(voucherId).contains(customerId);
    }

    @PostMapping("/vouchers/decrement")
    public void decrementVoucher(@RequestParam String voucherId) {
        voucherRepository.findById(voucherId).ifPresent(voucher -> {
            voucher.setSoLuongPhieu((voucher.getSoLuongPhieu() == null ? 0 : voucher.getSoLuongPhieu()) - 1);
            voucherRepository.save(voucher);
        });
    }

    @GetMapping("/vouchers/applicable")
    public List<Map<String, Object>> getApplicableVouchers(@RequestParam String customerId) {
        return voucherRepository.findByStatusAndSoLuongPhieuGreaterThan(EntityStatus.ACTIVE, 0)
                .stream()
                .filter(voucher -> !Boolean.TRUE.equals(voucher.getLoaiGiam()) || chiTietRepository.findCustomerIdsByVoucherId(voucher.getId()).contains(customerId))
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

    private Map<String, Object> voucherMap(PhieuGiamGia voucher) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", voucher.getId());
        row.put("ma_phieu_giam_gia", voucher.getMa());
        row.put("ten_phieu_giam_gia", voucher.getTen());
        row.put("phan_tram", voucher.getPhanTramGiam());
        row.put("so_luong_phieu", voucher.getSoLuongPhieu());
        row.put("ngay_bat_dau", voucher.getNgayBatDau());
        row.put("ngay_ket_thuc", voucher.getNgayKetThuc());
        row.put("dieu_kien", voucher.getDieuKien());
        row.put("gia_giam_toi_da", voucher.getGiaGiam());
        row.put("loai_giam", voucher.getLoaiGiam());
        row.put("kieu_giam", voucher.getKieuGiam());
        row.put("status", voucher.getStatus() == null ? null : voucher.getStatus().ordinal());
        return row;
    }

    private Map<String, Object> discountMap(DotGiamGiaChiTietSanPham detail) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("productDetailId", detail.getSanPhamChiTietId());
        row.put("ten", detail.getDotGiamGia() == null ? null : detail.getDotGiamGia().getTen());
        row.put("phanTramGiam", detail.getDotGiamGia() == null ? null : detail.getDotGiamGia().getPhanTramGiam());
        row.put("giaTruoc", detail.getGiaTruoc());
        row.put("giaSau", detail.getGiaSau());
        row.put("ngayBatDau", detail.getDotGiamGia() == null ? null : detail.getDotGiamGia().getNgayBatDau());
        row.put("ngayKetThuc", detail.getDotGiamGia() == null ? null : detail.getDotGiamGia().getNgayKetThuc());
        return row;
    }
}
