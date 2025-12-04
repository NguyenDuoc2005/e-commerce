package com.be.server.core.permitall.donmua.service.impl;

import com.be.server.core.admin.SanPhamChiTiet.repository.ADSanPhamChiTietRepository;
import com.be.server.core.admin.banhang.repository.ADTaoHoaDonChiTietRepository;
import com.be.server.core.admin.banhang.repository.ADTaoHoaDonRepository;
import com.be.server.core.admin.hoadon.model.request.ADHoaDonSearchRequest;
import com.be.server.core.admin.hoadon.model.response.HoaDonPageResponse1;
import com.be.server.core.admin.hoadon.model.response.LichSuThanhToanResponse;
import com.be.server.core.admin.hoadon.repository.ADHoaDonRepository;
import com.be.server.core.admin.hoadon.repository.ADLichSuThanhToanRepository;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.core.permitall.donmua.model.request.PMThemSanPhamRequest;
import com.be.server.core.permitall.donmua.model.request.UpdateDeliveryDTO;
import com.be.server.core.permitall.donmua.service.DonMuaService;
import com.be.server.entity.HoaDon;
import com.be.server.entity.HoaDonChiTiet;
import com.be.server.entity.SanPhamChiTiet;
import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import com.be.server.repository.HoaDonRepository;
import com.be.server.service.EmailService;
import com.be.server.utils.Helper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class DonMuaServiceImpl implements DonMuaService {


    public final ADTaoHoaDonRepository adTaoHoaDonRepository;
    public final ADHoaDonRepository adHoaDonRepository;
    public final ADTaoHoaDonChiTietRepository adTaoHoaDonChiTietRepository;
    private final HoaDonRepository hoaDonRepository;
    public final ADSanPhamChiTietRepository adSanPhamRepository;
    public final ADLichSuThanhToanRepository adLichSuThanhToanRepository;

    @Override
    public ResponseObject<?> getAllHoaDon(ADHoaDonSearchRequest request) {
        try {
            HoaDonPageResponse1 result = adHoaDonRepository.getAllHoaDonResponse1(request);

            return new ResponseObject<>(
                    result,
                    HttpStatus.OK,
                    "Lấy danh sách lịch sử đơn hàng thành công"
            );
        } catch (Exception e) {
            return new ResponseObject<>(
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage()
            );
        }
    }

    public ResponseObject<?> getAllHoaDon1(String code) {
        try {
            HoaDonPageResponse1 result = adHoaDonRepository.getAllHoaDonResponse1(code);

            return new ResponseObject<>(
                    result,
                    HttpStatus.OK,
                    "Lấy danh sách lịch sử đơn hàng thành công"
            );
        } catch (Exception e) {
            return new ResponseObject<>(
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage()
            );
        }
    }

    @Override
    public ResponseObject<?> createThemSanPham(PMThemSanPhamRequest request) {
        List<String> idHDCT = adTaoHoaDonChiTietRepository.checkGioHangV2(request);

        System.out.println(idHDCT.size());

        if (idHDCT == null || idHDCT.size() <= 0) {
            HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();

            SanPhamChiTiet sanPham = adSanPhamRepository.findById(request.getIdSP()).get();


            if (sanPham.getSoLuong() < Integer.valueOf(1)) {
                return new ResponseObject<>(null, HttpStatus.OK, "Số lượng sản phẩm thêm vào nhiều hơn số lượng trong kho");
            } else {


                HoaDon hoaDon = adTaoHoaDonRepository.findById(request.getIdHD()).get();


                hoaDonChiTiet.setHoaDon(hoaDon);

                hoaDonChiTiet.setSpct(sanPham);

                hoaDonChiTiet.setSoLuong(1);

                double gia = sanPham.getGiaBan();

                hoaDonChiTiet.setGia(gia);

                adTaoHoaDonChiTietRepository.save(hoaDonChiTiet);

                return new ResponseObject<>(null, HttpStatus.OK, "thêm sản phẩm thanh công");

            }
        } else {

            HoaDonChiTiet hoaDonChiTiet = adTaoHoaDonChiTietRepository.findById(idHDCT.get(0)).get();

            SanPhamChiTiet sanPham = adSanPhamRepository.findById(request.getIdSP()).get();


            double gia1 = hoaDonChiTiet.getGia();
            double gia2 = sanPham.getGiaBan();
            double epsilon = 0.0001;

            if (Math.abs(gia1 - gia2) > epsilon) {

                if (sanPham.getSoLuong() < Integer.valueOf(1)) {
                    return new ResponseObject<>(null, HttpStatus.OK, "Số lượng sản phẩm thêm vào nhiều hơn số lượng trong kho");
                } else {
                    HoaDonChiTiet hoaDonChiTiet1 = new HoaDonChiTiet();

                    HoaDon hoaDon = adTaoHoaDonRepository.findById(request.getIdHD()).get();

                    hoaDonChiTiet1.setHoaDon(hoaDon);

                    hoaDonChiTiet1.setSpct(sanPham);

                    hoaDonChiTiet1.setSoLuong(1);

                    double gia = sanPham.getGiaBan();

                    hoaDonChiTiet1.setGia(gia);

                    adTaoHoaDonChiTietRepository.save(hoaDonChiTiet1);

                    return new ResponseObject<>(null, HttpStatus.OK, "Sản phẩm này đang đc thay đổi giá từ " + (hoaDonChiTiet.getGia()) + "đ thành " + sanPham.getGiaBan());

                }
            } else {
                hoaDonChiTiet.setSoLuong(hoaDonChiTiet.getSoLuong() + 1);

                if (sanPham.getSoLuong() < hoaDonChiTiet.getSoLuong()) {
                    return new ResponseObject<>(null, HttpStatus.OK, "Số lượng sản phẩm thêm vào nhiều hơn số lượng trong kho");
                } else {

                    adTaoHoaDonChiTietRepository.save(hoaDonChiTiet);

                    return new ResponseObject<>(null, HttpStatus.OK, "thêm sản phẩm");

                }
            }
        }
    }

    @Override
    public ResponseObject<?> suaThongTin(UpdateDeliveryDTO request) {
        // Tìm hóa đơn
        HoaDon hoaDon = hoaDonRepository.findByMa(request.getMaHoaDon())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn với mã: " + request.getMaHoaDon()));

        // Chỉ cho phép cập nhật nếu trạng thái là "Chờ xác nhận" (0)
        if (!hoaDon.getTrangThaiHoaDon().equals(EntityTrangThaiHoaDon.CHO_XAC_NHAN)) {
            throw new IllegalStateException("Chỉ có thể cập nhật thông tin giao hàng khi đơn hàng đang chờ xác nhận");
        }

        // Cập nhật thông tin giao hàng
        hoaDon.setTenKH(request.getTenKhachHang());
        hoaDon.setSdt(request.getSdtKhachHang());
        hoaDon.setEmail(request.getEmail());
        hoaDon.setDiaChi(request.getDiaChi());
        hoaDon.setPhiVanChuyen(request.getPhiVanChuyen());

        if (hoaDon.getTongTienSauGiam() > request.getTongTienSauGiam()) {
            if (hoaDon.getDuNo() != null) {
                hoaDon.setDuNo(hoaDon.getDuNo() + (hoaDon.getTongTienSauGiam() - request.getTongTienSauGiam()));
                if (hoaDon.getHoanPhi() != null) {
                    if (hoaDon.getHoanPhi() > 0) {
                        if (hoaDon.getHoanPhi() - (request.getTongTienSauGiam() - hoaDon.getTongTienSauGiam()) <= 0) {
                            hoaDon.setDuNo(0D);
                        } else {
                            hoaDon.setDuNo(hoaDon.getDuNo() - (hoaDon.getTongTienSauGiam() - request.getTongTienSauGiam()));
                            hoaDon.setHoanPhi(0D);
                        }
                    }
                }
            } else {
                hoaDon.setHoanPhi(request.getTongTienSauGiam() - hoaDon.getTongTienSauGiam());
            }
        } else if (hoaDon.getTongTienSauGiam() < request.getTongTienSauGiam()) {
            if (hoaDon.getHoanPhi() != null) {
                hoaDon.setHoanPhi(hoaDon.getHoanPhi() + (request.getTongTienSauGiam() - hoaDon.getTongTienSauGiam()));
                if (hoaDon.getDuNo() != null) {
                    if (hoaDon.getDuNo() > 0) {
                        if (hoaDon.getDuNo() - (hoaDon.getTongTienSauGiam() - request.getTongTienSauGiam()) <= 0) {
                            hoaDon.setDuNo(0D);
                        } else {
                            hoaDon.setDuNo(hoaDon.getDuNo() - (hoaDon.getTongTienSauGiam() - request.getTongTienSauGiam()));
                            hoaDon.setDuNo(0D);
                        }
                    }
                }
            } else {
                hoaDon.setDuNo(request.getTongTienSauGiam() - hoaDon.getTongTienSauGiam());
            }
        }

        hoaDon.setTongTienSauGiam(request.getTongTienSauGiam());

        // Lưu hóa đơn
        hoaDonRepository.save(hoaDon);

        String email = request.getEmail();
        System.out.println(email);
        String trackingUrl = "http://localhost:6688/don-mua-detail/" + hoaDon.getMa() + "/" + hoaDon.getId();
        String subject = "Thay đổi thông tin đơn hàng";

        String content =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border:1px solid #e0e0e0; border-radius:8px; overflow:hidden;'>"
                        + "  <div style='background:#fff; padding:20px; text-align:center;'>"
                        + "    <h2 style='color:#333; margin:0;'>GLAMSOLE</h2>"
                        + "    <p style='color:#666; margin:8px 0 0;'>Đơn đặt hàng số: <b>" + hoaDon.getMa() + "</b></p>"
                        + "  </div>"
                        + "  <hr style='border:none; border-top:1px solid #eee;'/>"
                        + "  <div style='padding:20px;'>"
                        + "    <h3 style='color:#333; margin-top:0;'>Thay đổi thông tin</h3>"
                        + "    <p style='color:#555;'>Xin chào <b>" + hoaDon.getTenKH() + "</b>, đơn hàng của bạn đã được thay đổi thông tin.</p>"
                        + "    <p style='color:#555;'>Bạn có thể theo dõi trạng thái đơn hàng bằng cách nhấn vào nút bên dưới:</p>"
                        + "    <div style='text-align:center; margin:30px 0;'>"
                        + "      <a href='" + trackingUrl + "' style='background:#f5c542; color:#000; text-decoration:none; padding:12px 20px; border-radius:6px; font-weight:bold;'>Xem đơn đặt hàng</a>"
                        + "    </div>"
                        + "    <p style='color:#777; font-size:14px;'>-- GLAMSOLE team --</p>"
                        + "  </div>"
                        + "</div>";

        CompletableFuture.runAsync(() -> EmailService.sendEmail(email, subject, content));

        return new ResponseObject<>(
                hoaDon,
                HttpStatus.OK,
                "Lấy danh sách lịch sử đơn hàng thành công");
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
}
