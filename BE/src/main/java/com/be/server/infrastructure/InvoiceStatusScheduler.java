package com.be.server.infrastructure;

import com.be.server.core.admin.banhang.repository.ADTaoHoaDonRepository;
import com.be.server.entity.HoaDon;
import com.be.server.entity.LichSuTrangThaiHoaDon;
import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import com.be.server.repository.LichSuTrangThaiHoaDonRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class InvoiceStatusScheduler {

    private final ADTaoHoaDonRepository adTaoHoaDonRepository;
    private final LichSuTrangThaiHoaDonRepository lichSuTrangThaiHoaDonRepository;

    public InvoiceStatusScheduler(ADTaoHoaDonRepository adTaoHoaDonRepository,
                                  LichSuTrangThaiHoaDonRepository lichSuTrangThaiHoaDonRepository) {
        this.adTaoHoaDonRepository = adTaoHoaDonRepository;
        this.lichSuTrangThaiHoaDonRepository = lichSuTrangThaiHoaDonRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Chạy vào 00:00:00 mỗi ngày
    @Transactional
    public void updatePendingInvoicesToCancelled() {
        // Tìm tất cả hóa đơn có trạng thái CHO_XAC_NHAN
        List<HoaDon> pendingInvoices = adTaoHoaDonRepository.findByTrangThaiHoaDon((EntityTrangThaiHoaDon.CHO_XAC_NHAN));

        for (HoaDon hoaDon : pendingInvoices) {
            // Cập nhật trạng thái hóa đơn thành DA_HUY
            hoaDon.setTrangThaiHoaDon(EntityTrangThaiHoaDon.DA_HUY);
            adTaoHoaDonRepository.save(hoaDon);

            // Tạo bản ghi lịch sử trạng thái mới
            LichSuTrangThaiHoaDon lichSuTrangThaiHoaDon = new LichSuTrangThaiHoaDon();
            lichSuTrangThaiHoaDon.setHoaDon(hoaDon);
            lichSuTrangThaiHoaDon.setTrangThai(EntityTrangThaiHoaDon.DA_HUY);
            lichSuTrangThaiHoaDon.setThoiGian(LocalDateTime.now()); // Tùy chọn: Ghi thời gian
            lichSuTrangThaiHoaDonRepository.save(lichSuTrangThaiHoaDon);
        }
    }
}