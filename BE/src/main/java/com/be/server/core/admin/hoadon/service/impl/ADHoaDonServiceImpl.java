package com.be.server.core.admin.hoadon.service.impl;

import com.be.server.core.admin.SanPhamChiTiet.repository.ADSanPhamChiTietRepository;
import com.be.server.core.admin.banhang.repository.ADTaoHoaDonChiTietRepository;
import com.be.server.core.admin.hoadon.model.request.ADChangeStatusRequest;
import com.be.server.core.admin.hoadon.model.request.ADHoaDonDetailRequest;
import com.be.server.core.admin.hoadon.model.request.ADHoaDonSearchRequest;
import com.be.server.core.admin.hoadon.model.request.ThanhToanRequest;
import com.be.server.core.admin.hoadon.model.response.*;
import com.be.server.core.admin.hoadon.repository.ADHoaDonChiTietRepository;
import com.be.server.core.admin.hoadon.repository.ADHoaDonRepository;
import com.be.server.core.admin.hoadon.repository.ADLichSuThanhToanRepository;
import com.be.server.core.admin.hoadon.repository.LichSuTrangThaiRepository;
import com.be.server.core.admin.hoadon.service.ADHoaDonService;
import com.be.server.core.admin.voucher.model.response.ADPhieuGiamGiaResponse;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.HoaDon;
import com.be.server.entity.HoaDonChiTiet;
import com.be.server.entity.LichSuThanhToan;
import com.be.server.entity.LichSuTrangThaiHoaDon;
import com.be.server.entity.NhanVien;
import com.be.server.entity.PhieuGiamGia;
import com.be.server.entity.SanPhamChiTiet;
import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import com.be.server.repository.NhanVienRepository;
import com.be.server.repository.VoucherRepository;
import com.be.server.service.EmailService;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
public class ADHoaDonServiceImpl implements ADHoaDonService {

    public final ADHoaDonRepository adHoaDonRepository;

    public final ADSanPhamChiTietRepository adSanPhamChiTietRepository;

    public final ADHoaDonChiTietRepository adHoaDonChiTietRepository;

    public final ADTaoHoaDonChiTietRepository adTaoHoaDonChiTietRepository;

    public final VoucherRepository voucherRepository;

    public final LichSuTrangThaiRepository lichSuTrangThaiRepository;

    public final ADLichSuThanhToanRepository adLichSuThanhToanRepository;

    public final NhanVienRepository nhanVienRepository;

    @Override
    public ResponseObject<?> getAllHoaDon(ADHoaDonSearchRequest request) {
        try {
            Pageable pageable = Helper.createPageable(request, "created_date");
            HoaDonPageResponse result = adHoaDonRepository.getAllHoaDonResponse(request, pageable);

            return new ResponseObject<>(
                    result,
                    HttpStatus.OK,
                    "Lấy danh sách hóa đơn thành công"
            );
        } catch (Exception e) {
            return new ResponseObject<>(
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi lấy danh sách hóa đơn: " + e.getMessage()
            );
        }
    }

    @Override
    public ResponseObject<?> getAllHoaDonCT(ADHoaDonDetailRequest request) {
        try {
            Pageable pageable = Helper.createPageable(request, "created_date");
            Page<ADHoaDonChiTietResponseDetail> page = adHoaDonChiTietRepository.getAllHoaDonChiTietResponse(request.getMaHoaDon(), pageable);
            return new ResponseObject<>(
                    page,
                    HttpStatus.OK,
                    "Lấy danh sách chi tiết hóa đơn thành công"
            );
        } catch (Exception e) {
            return new ResponseObject<>(
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi lấy chi tiết hóa đơn: " + e.getMessage()
            );
        }
    }

    @Override
    public ResponseObject<?> getAllHoaDonCT1(ADHoaDonDetailRequest request) {
        try {
            List<ADHoaDonChiTietResponseDetail> page = adHoaDonChiTietRepository.getAllHoaDonChiTietResponse2(request.getMaHoaDon());
            return new ResponseObject<>(
                    page,
                    HttpStatus.OK,
                    "Lấy danh sách chi tiết hóa đơn thành công"
            );
        } catch (Exception e) {
            return new ResponseObject<>(
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi lấy chi tiết hóa đơn: " + e.getMessage()
            );
        }
    }

    @Override
    public ResponseObject<?> changeStatus(ADChangeStatusRequest adChangeStatusRequest) {
        HoaDon hoaDon = adHoaDonRepository.updateTrangThaiHoaDon(adChangeStatusRequest.getMaHoaDon(), adChangeStatusRequest.getStatus());

        HoaDon hoaDon1 = new HoaDon();

        LichSuTrangThaiHoaDon statusHistory = new LichSuTrangThaiHoaDon();
        statusHistory.setHoaDon(hoaDon);
        statusHistory.setTrangThai(adChangeStatusRequest.getStatus());
        statusHistory.setThoiGian(LocalDateTime.now());
        statusHistory.setNote(adChangeStatusRequest.getNote());
        // Save the status history
        lichSuTrangThaiRepository.save(statusHistory);
        System.out.println(adChangeStatusRequest.getStatus());
        if(adChangeStatusRequest.getStatus() == EntityTrangThaiHoaDon.DA_HUY){

            List<String> idHDCT = adTaoHoaDonChiTietRepository.getHoaDonChiTiet(hoaDon.getId());
            System.out.println("0");
            System.out.println(idHDCT);
            if(idHDCT != null){

                System.out.println("1");
                for (String id : idHDCT) {
                    System.out.println("2");
                    HoaDonChiTiet hoaDonChiTiet = adTaoHoaDonChiTietRepository.findById(id).get();

                    SanPhamChiTiet sanPhamChiTiet = adSanPhamChiTietRepository.findById(hoaDonChiTiet.getSpct().getId()).get();

                    sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() + hoaDonChiTiet.getSoLuong());

                    adSanPhamChiTietRepository.save(sanPhamChiTiet);


                }

            }

            if(hoaDon.getId() != null){
               HoaDon hoaDon2 = adHoaDonRepository.findById(hoaDon.getId()).get();
                System.out.println("3");
               PhieuGiamGia phieuGiamGia = voucherRepository.findById(hoaDon2.getVoucher().getId()).get();

               phieuGiamGia.setSoLuongPhieu(phieuGiamGia.getSoLuongPhieu() + 1);

               voucherRepository.save(phieuGiamGia);
            }

            String email = hoaDon.getEmail();
            String trackingUrl = "http://localhost:6688/don-mua-detail/" + hoaDon.getMa() + "/" + hoaDon.getId();
            String subject = "Hủy đơn hàng";

            String content =
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border:1px solid #e0e0e0; border-radius:8px; overflow:hidden;'>"
                            + "  <div style='background:#fff; padding:20px; text-align:center;'>"
                            + "    <h2 style='color:#333; margin:0;'>GLAMSOLE</h2>"
                            + "    <p style='color:#666; margin:8px 0 0;'>Đơn đặt hàng số: <b>" + hoaDon.getMa() + "</b></p>"
                            + "  </div>"
                            + "  <hr style='border:none; border-top:1px solid #eee;'/>"
                            + "  <div style='padding:20px;'>"
                            + "    <h3 style='color:#333; margin-top:0;'>Đơn hàng đã hủy</h3>"
                            + "    <p style='color:#555;'>Xin chào <b>" + hoaDon.getTenKH() + "</b>, đơn hàng của bạn đã hủy.</p>"
                            + "    <p style='color:#555;'>Bạn có thể theo dõi trạng thái đơn hàng bằng cách nhấn vào nút bên dưới:</p>"
                            + "    <div style='text-align:center; margin:30px 0;'>"
                            + "      <a href='" + trackingUrl + "' style='background:#f5c542; color:#000; text-decoration:none; padding:12px 20px; border-radius:6px; font-weight:bold;'>Xem đơn đặt hàng</a>"
                            + "    </div>"
                            + "    <p style='color:#777; font-size:14px;'>-- GLAMSOLE team --</p>"
                            + "  </div>"
                            + "</div>";

            CompletableFuture.runAsync(() -> EmailService.sendEmail(email, subject, content));

        }else {

            String email = hoaDon.getEmail();
            String trackingUrl = "http://localhost:6688/don-mua-detail/" + hoaDon.getMa() + "/" + hoaDon.getId();
            String subject = "Cập nhật trạng thái";

            String content =
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border:1px solid #e0e0e0; border-radius:8px; overflow:hidden;'>"
                            + "  <div style='background:#fff; padding:20px; text-align:center;'>"
                            + "    <h2 style='color:#333; margin:0;'>GLAMSOLE</h2>"
                            + "    <p style='color:#666; margin:8px 0 0;'>Đơn đặt hàng số: <b>" + hoaDon.getMa() + "</b></p>"
                            + "  </div>"
                            + "  <hr style='border:none; border-top:1px solid #eee;'/>"
                            + "  <div style='padding:20px;'>"
                            + "    <h3 style='color:#333; margin-top:0;'>Cập nhật trạng thái đơn hàng</h3>"
                            + "    <p style='color:#555;'>Xin chào <b>" + hoaDon.getTenKH() + "</b> Đơn hàng của bạn đã được cập nhật trạng thái.</p>"
                            + "    <p style='color:#555;'>Bạn có thể theo dõi trạng thái đơn hàng bằng cách nhấn vào nút bên dưới:</p>"
                            + "    <div style='text-align:center; margin:30px 0;'>"
                            + "      <a href='" + trackingUrl + "' style='background:#f5c542; color:#000; text-decoration:none; padding:12px 20px; border-radius:6px; font-weight:bold;'>Xem đơn đặt hàng</a>"
                            + "    </div>"
                            + "    <p style='color:#777; font-size:14px;'>-- GLAMSOLE team --</p>"
                            + "  </div>"
                            + "</div>";

            CompletableFuture.runAsync(() -> EmailService.sendEmail(email, subject, content));

        }


        return new ResponseObject<>(
                hoaDon1,
                HttpStatus.OK,
                "Thay đổi thành công "
        );
    }

    @Override
    public ResponseObject<?> getLSTTHD(String id) {
        List<TrangThaiThoiGianResponse> lichSuTrangThaiHoaDons = lichSuTrangThaiRepository.findTrangThaiVaThoiGianByHoaDonId(id);
        return new ResponseObject<>(
                lichSuTrangThaiHoaDons,
                HttpStatus.OK,
                "Lấy danh sách lịch sử trạng thái hóa đơn thành công"
        );
    }

    @Override
    public ResponseObject<?> getLSTT(String id) {
        List<LichSuThanhToanResponse> lichSuThanhToans = adLichSuThanhToanRepository.getLichSuThanhToanByHoaDonId(id);
        return new ResponseObject<>(
                lichSuThanhToans,
                HttpStatus.OK,
                "Lấy danh sách lịch sử thanh toán hóa đơn thành công"
        );
    }

    @Override
    public ResponseObject<?>  thanhToanHoaDon(ThanhToanRequest request) {
        HoaDon hoaDon = adHoaDonRepository.findById(request.getHoaDonId())
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        NhanVien nhanVien = nhanVienRepository.findById(request.getNhanVienId())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        LichSuThanhToan lichSu = new LichSuThanhToan();
        lichSu.setHoaDon(hoaDon);
        Double soTien = request.getSoTienKhachDua() - request.getSoTienTraLai();
        lichSu.setSoTien(soTien);
        lichSu.setThoiGian(LocalDateTime.now());
        lichSu.setGhiChu(request.getGhiChu());
        lichSu.setLoaiGiaoDich(request.getLoaiGiaoDich());
        lichSu.setNhanVien(nhanVien);
        lichSu.setMaGiaoDich(UUID.randomUUID().toString());

        adLichSuThanhToanRepository.save(lichSu);

        hoaDon.setTongTienSauGiam(soTien);
        hoaDon.setTongTien(request.getSoTienGoc());
        hoaDon.setTrangThaiHoaDon(request.getTrangThai());

        adHoaDonRepository.save(hoaDon);

        HoaDon hoaDon1 = new HoaDon();
        return new ResponseObject<>(
                hoaDon1,
                HttpStatus.OK,
                "Thanh toán thành công"
        );
    }

}
