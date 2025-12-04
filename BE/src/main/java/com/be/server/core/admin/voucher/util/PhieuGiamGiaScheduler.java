package com.be.server.core.admin.voucher.util;

import com.be.server.entity.PhieuGiamGia;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.repository.VoucherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class PhieuGiamGiaScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PhieuGiamGiaScheduler.class);

    @Autowired
    private VoucherRepository voucherRepository;

    // Chạy lúc 0:00 hàng ngày
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void updateVoucherStatus() {
        logger.info("Bắt đầu cập nhật trạng thái phiếu giảm giá...");

        LocalDate currentDate = LocalDate.now();
        logger.info("Ngày hiện tại: {}", currentDate);

        // Tối ưu hóa: chỉ lấy những voucher có khả năng thay đổi trạng thái
        List<PhieuGiamGia> vouchers = voucherRepository.findVouchersNeedStatusUpdate();

        int updatedCount = 0;
        for (PhieuGiamGia voucher : vouchers) {
            try {
                EntityStatus currentStatus = voucher.getStatus();
                EntityStatus newStatus = determineStatus(voucher, currentDate);

                if (currentStatus != newStatus) {
                    voucher.setStatus(newStatus);
                    voucherRepository.save(voucher);
                    updatedCount++;

                    logger.info("Cập nhật trạng thái phiếu giảm giá '{}' (ID: {}) từ {} sang {}",
                            voucher.getTen(), voucher.getId(), currentStatus, newStatus);
                }
            } catch (Exception e) {
                logger.error("Lỗi khi cập nhật trạng thái phiếu giảm giá ID {}: {}",
                        voucher.getId(), e.getMessage(), e);
            }
        }

        logger.info("Hoàn tất cập nhật trạng thái phiếu giảm giá. Đã cập nhật {} phiếu.", updatedCount);
    }

    private EntityStatus determineStatus(PhieuGiamGia voucher, LocalDate currentDate) {
        Date ngayBatDauDate = voucher.getNgayBatDau();
        Date ngayKetThucDate = voucher.getNgayKetThuc();

        if (ngayBatDauDate == null || ngayKetThucDate == null) {
            logger.warn("Phiếu giảm giá ID {} có ngày bắt đầu hoặc kết thúc null", voucher.getId());
            return EntityStatus.INACTIVE;
        }

        LocalDate ngayBatDau = ngayBatDauDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate ngayKetThuc = ngayKetThucDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        logger.debug("Phiếu ID {}: Từ {} đến {}, Hiện tại: {}",
                voucher.getId(), ngayBatDau, ngayKetThuc, currentDate);

        // Chưa bắt đầu
        if (currentDate.isBefore(ngayBatDau)) {
            return EntityStatus.INACTIVE;
        }

        // Đã kết thúc
        if (currentDate.isAfter(ngayKetThuc)) {
            return EntityStatus.INACTIVE;
        }

        // Đang diễn ra (bao gồm cả ngày bắt đầu và kết thúc)
        return EntityStatus.ACTIVE;
    }

    // Method để test thủ công
    public void manualUpdateVoucherStatus() {
        logger.info("Chạy cập nhật trạng thái thủ công...");
        updateVoucherStatus();
    }
}