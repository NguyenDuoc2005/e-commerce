package com.be.server.core.admin.dotgiamgia.util;

import com.be.server.entity.DotGiamGia;
import com.be.server.entity.DotGiamGiaChiTietSanPham;
import com.be.server.entity.SanPhamChiTiet;
import com.be.server.infrastructure.constant.StatusPromotion;
import com.be.server.repository.DotGiamGiaChiTietSanPhamRepository;
import com.be.server.repository.DotGiamGiaRepository;
import com.be.server.repository.SanPhamChiTietRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DotGiamGiaScheduler {

    private final DotGiamGiaRepository dotGiamGiaRepository;
    private final DotGiamGiaChiTietSanPhamRepository dotGiamGiaChiTietSanPhamRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    @Scheduled(fixedDelay = 1000) // mỗi giây
    public void autoUpdatePromotionStatuses() {
        long now = System.currentTimeMillis();

        // 1. Cập nhật sang DANG_KICH_HOAT nếu đến thời gian bắt đầu
        List<DotGiamGia> toActivate = dotGiamGiaRepository
                .findAllByTrangThaiAndNgayBatDauLessThanEqual(StatusPromotion.CHUA_KICH_HOAT, now);

        for (DotGiamGia dgg : toActivate) {
            if (dgg.getNgayBatDau() <= now) {
                dgg.setTrangThai(StatusPromotion.DANG_KICH_HOAT);
                updateGiaSanPhamTheoDotGiamGia(dgg.getId(), StatusPromotion.DANG_KICH_HOAT);
            }
        }

        // 2. Cập nhật sang HET_HAN_KICH_HOAT nếu đã hết hạn
        List<DotGiamGia> toExpire = dotGiamGiaRepository
                .findAllByTrangThaiAndNgayKetThucLessThan(StatusPromotion.DANG_KICH_HOAT, now);

        for (DotGiamGia dgg : toExpire) {
            if (dgg.getNgayKetThuc() <= now) {
                dgg.setTrangThai(StatusPromotion.HET_HAN_KICH_HOAT);
                updateGiaSanPhamTheoDotGiamGia(dgg.getId(), StatusPromotion.HET_HAN_KICH_HOAT);
            }
        }

        dotGiamGiaRepository.saveAll(toActivate);
        dotGiamGiaRepository.saveAll(toExpire);

        // 3. Luôn cập nhật giá bán cho DANG_KICH_HOAT
//        List<DotGiamGia> activePromotions = dotGiamGiaRepository.findAllByTrangThai(StatusPromotion.DANG_KICH_HOAT);
//        for (DotGiamGia dgg : activePromotions) {
//            updateGiaSanPhamTheoDotGiamGia(dgg.getId(), StatusPromotion.DANG_KICH_HOAT);
//        }
//
//        // 4. Luôn cập nhật giá bán cho HET_HAN_KICH_HOAT
//        List<DotGiamGia> expiredPromotions = dotGiamGiaRepository.findAllByTrangThai(StatusPromotion.HET_HAN_KICH_HOAT);
//        for (DotGiamGia dgg : expiredPromotions) {
//            updateGiaSanPhamTheoDotGiamGia(dgg.getId(), StatusPromotion.HET_HAN_KICH_HOAT);
//        }

        if (!toActivate.isEmpty() || !toExpire.isEmpty()) {
            log.info("Đã cập nhật trạng thái {} đợt giảm giá", toActivate.size() + toExpire.size());
        }
    }


    private void updateGiaSanPhamTheoDotGiamGia(String idDotGiamGia, StatusPromotion trangThaiMoi) {
        List<DotGiamGiaChiTietSanPham> chiTietList = dotGiamGiaChiTietSanPhamRepository.findAllByDotGiamGiaId(idDotGiamGia);

        for (DotGiamGiaChiTietSanPham chiTiet : chiTietList) {
            SanPhamChiTiet spct = chiTiet.getSanPhamChiTiet();
            if (trangThaiMoi.equals(StatusPromotion.DANG_KICH_HOAT)) {
                spct.setGiaBan(chiTiet.getGiaSau());
            } else {
                spct.setGiaBan(chiTiet.getGiaTruoc());
            }
            sanPhamChiTietRepository.save(spct);
        }
    }
}