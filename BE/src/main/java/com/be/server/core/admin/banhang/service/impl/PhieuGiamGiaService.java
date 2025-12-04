package com.be.server.core.admin.banhang.service.impl;

import com.be.server.core.admin.banhang.model.response.AvailableVouchersResponse;
import com.be.server.core.admin.banhang.repository.ADTaoHoaDonRepository;
import com.be.server.core.admin.voucher.model.response.ADPhieuGiamGiaResponse;
import com.be.server.core.admin.voucher.repository.ADVoucherRepository;
import com.be.server.entity.HoaDon;
import com.be.server.entity.PhieuGiamGia;
import com.be.server.repository.HoaDonRepository;
import com.be.server.repository.VoucherRepository;
import com.nimbusds.openid.connect.sdk.assurance.evidences.Voucher;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class PhieuGiamGiaService {

    @Autowired
    private ADVoucherRepository phieuGiamGiaRepository;

    @Autowired
    private ADTaoHoaDonRepository hoaDonService; // Giả sử bạn có service để lấy thông tin hóa đơn

    public AvailableVouchersResponse getAvailableVouchers(String idHD, String idKH, Double tongTien) {
        if (tongTien == null) {
            tongTien = 0.0; // Gán giá trị mặc định nếu tongTien là null
        }

        Date currentDate = new Date();
        List<PhieuGiamGia> vouchers = phieuGiamGiaRepository.findAvailableVouchers(idKH);

        AvailableVouchersResponse response = new AvailableVouchersResponse();
        final double finalTongTien = tongTien;
        List<AvailableVouchersResponse.PhieuGiamGiaDTO> availableVouchers = vouchers.stream()
                .filter(v -> v != null && v.getSoLuongPhieu() != null && v.getSoLuongPhieu() > 0
                        && v.getDieuKien() != null && v.getDieuKien() <= finalTongTien)
                .map(v -> convertToDTO(v))
                .filter(dto -> dto != null && dto.getGiaTriGiamThucTe() != null) // Loại bỏ DTO null hoặc giaTriGiamThucTe null
                .collect(Collectors.toList());

        response.setAvailableVouchers(availableVouchers);

        // Tìm phiếu tốt nhất hiện tại
        AvailableVouchersResponse.PhieuGiamGiaDTO bestVoucher = availableVouchers.stream()
                .max((v1, v2) -> Double.compare(
                        v1.getGiaTriGiamThucTe() != null ? v1.getGiaTriGiamThucTe() : 0.0,
                        v2.getGiaTriGiamThucTe() != null ? v2.getGiaTriGiamThucTe() : 0.0))
                .orElse(null);
        response.setBestVoucher(bestVoucher);

        // Tìm phiếu tốt hơn (nếu có)
        AvailableVouchersResponse.BetterVoucherInfo betterVoucher = null;
        for (PhieuGiamGia voucher : vouchers) {
            if (voucher != null) {
                Double giaTriGiam = calculateGiaTriGiamThucTe(voucher, tongTien);
                Double dieuKien = voucher.getDieuKien() != null ? voucher.getDieuKien() : Double.MAX_VALUE;
                Double bestVoucherValue = (bestVoucher != null && bestVoucher.getGiaTriGiamThucTe() != null)
                        ? bestVoucher.getGiaTriGiamThucTe() : 0.0;

                if (dieuKien > tongTien && giaTriGiam != null && giaTriGiam > bestVoucherValue) {
                    betterVoucher = new AvailableVouchersResponse.BetterVoucherInfo();
                    betterVoucher.setMa(voucher.getMa());
                    betterVoucher.setGiaTriGiamThucTe(giaTriGiam);
                    betterVoucher.setAmountNeeded(dieuKien - tongTien);
                    break; // Lấy phiếu tốt hơn đầu tiên
                }
            }
        }
        response.setBetterVoucher(betterVoucher);

        return response;
    }

    private Double calculateGiaTriGiamThucTe(PhieuGiamGia pg, Double tongTien) {
        if (pg == null || tongTien == null) {
            return 0.0;
        }

        try {
            if (pg.getKieuGiam() == null) {
                return 0.0; // Trả về 0 nếu kieuGiam là null
            }

            if (pg.getKieuGiam()) {
                if (pg.getPhanTramGiam() == null || pg.getDieuKien() == null) {
                    return 0.0; // Trả về 0 nếu thiếu dữ liệu
                }
                Double giaTriGiam = tongTien * (pg.getPhanTramGiam() / 100.0);
                if (pg.getGiaGiam() != null && giaTriGiam >= pg.getGiaGiam()) {
                    return pg.getGiaGiam();
                }
                return giaTriGiam;
            } else {
                if (pg.getPhanTramGiam() == null) {
                    return 0.0; // Trả về 0 nếu thiếu phanTramGiam
                }
                return tongTien - (tongTien - pg.getPhanTramGiam());
            }
        } catch (Exception e) {
            System.err.println("Lỗi tính giá trị giảm thực tế: " + e.getMessage());
            return 0.0; // Trả về 0 nếu có lỗi
        }
    }

    private AvailableVouchersResponse.PhieuGiamGiaDTO convertToDTO(PhieuGiamGia voucher) {
        AvailableVouchersResponse.PhieuGiamGiaDTO dto = new AvailableVouchersResponse.PhieuGiamGiaDTO();
        dto.setId(voucher.getId());
        dto.setMa(voucher.getMa());
        dto.setTen(voucher.getTen());
        dto.setPhanTramGiam(voucher.getPhanTramGiam());
        dto.setGiaTriGiamThucTe(voucher.getGiaTriGiamThucTe());
        dto.setSoLuongPhieu(voucher.getSoLuongPhieu());
        dto.setNgayBatDau(voucher.getNgayBatDau());
        dto.setNgayKetThuc(voucher.getNgayKetThuc());
        dto.setDieuKien(voucher.getDieuKien());
        dto.setGiaGiam(voucher.getGiaGiam());
        dto.setLoaiGiam(voucher.getLoaiGiam());
        dto.setKieuGiam(voucher.getKieuGiam());
        return dto;
    }
}