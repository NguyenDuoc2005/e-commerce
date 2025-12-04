package com.be.server.core.admin.voucher.service.impl;

import com.be.server.core.admin.SanPhamChiTiet.model.response.ADSanPhamChiTietResponse;
import com.be.server.core.admin.khachhang.repository.ADKhachHangRepository;
import com.be.server.core.admin.thuonghieu.model.request.ADThuongHieuRequest;
import com.be.server.core.admin.thuonghieu.model.request.ADThuongHieuSearchRequest;
import com.be.server.core.admin.voucher.model.request.ADVoucherRequest;
import com.be.server.core.admin.voucher.model.request.ADVoucherSearchRequest;
import com.be.server.core.admin.voucher.model.response.ADPhieuGiamGiaResponse;
import com.be.server.core.admin.voucher.repository.ADVoucherRepository;
import com.be.server.core.admin.voucher.service.ADVoucherService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.KhachHang;
import com.be.server.entity.PhieuGiamGia;
import com.be.server.entity.PhieuGiamGiaChiTiet;
import com.be.server.entity.ThuongHieu;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.repository.PhieuGiamGiaChiTietRepository;
import com.be.server.service.EmailService;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ADVoucherServiceImpl implements ADVoucherService {

    private final ADVoucherRepository advoucherRepository;
    private final ADKhachHangRepository adKhachHangRepository;
    private final PhieuGiamGiaChiTietRepository phieuGiamGiaChiTietRepository;

    @Override
    public ResponseObject<?> getAllVoucher(ADVoucherSearchRequest request) {
        Pageable pageable = Helper.createPageable(request, "created_date");
        ;

        if (request.getKieuGiam() != null) {
            if (request.getKieuGiam() == 0) {
                request.setKieu(true);
            } else {
                request.setKieu(false);
            }
        }

        if (request.getStatus() != null) {
            if (request.getStatus() == 0) {
                request.setEntityStatus(EntityStatus.INACTIVE);
            } else {
                request.setEntityStatus(EntityStatus.ACTIVE);
            }
        }


        Page<ADPhieuGiamGiaResponse> page = advoucherRepository.getAllPhieuGiamGiaFilter(pageable, request);

        return new ResponseObject<>(
                PageableObject.of(page),
                HttpStatus.OK,
                "Lấy danh sách phiếu giảm giá thành công"
        );

    }

    @Override
    public ResponseObject<?> getVoucherById(String id) {


        return advoucherRepository.getNhanVienID(id).map(technology -> new ResponseObject<>(technology, HttpStatus.OK, "Lấy phiếu giảm giá thành công")).orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy phiếu giảm giá"));
    }

    @Override
    public Page<String> getListKH(String id, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return advoucherRepository.getDanhSachKhachHang(id, search, pageable);
    }

    @Override
    public ResponseObject<?> modifyVoucher(ADVoucherRequest request) {
        if (request.getId() != null && StringUtils.hasLength(request.getId())) {
            Optional<PhieuGiamGia> exsitingMemberOpt = advoucherRepository.findById(request.getId());

            if (exsitingMemberOpt.isPresent()) {

                PhieuGiamGia voucher = exsitingMemberOpt.get();

                voucher.setTen(request.getTen());

                voucher.setDieuKien(request.getDieuKien());

                voucher.setGiaGiam(request.getGiaGiam());

                voucher.setLoaiGiam(request.getLoaiGiam());

                voucher.setNgayBatDau(request.getNgayBatDau());

                voucher.setKieuGiam(request.getKieuGiam());

                voucher.setNgayKetThuc(request.getNgayKetThuc());

                voucher.setSoLuongPhieu(request.getSoLuongPhieu());

                if (request.getKieuGiam() == true) {
                    voucher.setPhanTramGiam(request.getLoiPhanNay());
                } else {
                    voucher.setPhanTramGiam(request.getGiaGiam());
                }


                advoucherRepository.save(voucher);

                return new ResponseObject<>(voucher, HttpStatus.OK, "Cập nhật phiếu giảm giá thành công");
            }
        }

        if (advoucherRepository.checkThemPhieu(request.getTen()) != null) {
            return new ResponseObject<>(null, HttpStatus.OK, "phiếu giảm giá này đã tồn tại");
        }

        PhieuGiamGia voucher = new PhieuGiamGia();

        voucher.setTen(request.getTen());

        voucher.setDieuKien(request.getDieuKien());

        voucher.setGiaGiam(request.getGiaGiam());

        voucher.setLoaiGiam(request.getLoaiGiam());

        voucher.setNgayBatDau(request.getNgayBatDau());

        voucher.setNgayKetThuc(request.getNgayKetThuc());

        voucher.setSoLuongPhieu(request.getSoLuongPhieu());

        voucher.setKieuGiam(request.getKieuGiam());

        if (request.getKieuGiam() == true) {
            voucher.setPhanTramGiam(request.getLoiPhanNay());
        } else {
            voucher.setPhanTramGiam(request.getGiaGiam());
        }

        LocalDate today = LocalDate.now();
        LocalDate start = request.getNgayBatDau().toLocalDate();
        LocalDate end = request.getNgayKetThuc().toLocalDate();

        if (!today.isBefore(start) && !today.isAfter(end)) {
            voucher.setStatus(EntityStatus.ACTIVE);
        } else {
            voucher.setStatus(EntityStatus.INACTIVE);
        }


        advoucherRepository.save(voucher);



        if (request.getKhachHangIds() != null) {
            for (int i = 0; i < request.getKhachHangIds().size(); i++) {

                KhachHang khachHang = adKhachHangRepository.findById(request.getKhachHangIds().get(i)).get();

                PhieuGiamGiaChiTiet phieuGiamGiaChiTiet = new PhieuGiamGiaChiTiet();

                phieuGiamGiaChiTiet.setPhieuGiamGia(voucher);

                phieuGiamGiaChiTiet.setKhachHang(khachHang);

                phieuGiamGiaChiTietRepository.save(phieuGiamGiaChiTiet);

                String email = khachHang.getEmail();
                String subject = "Khuyến mại của cửa hàng";
                String content =
                        "<!DOCTYPE html>" +
                                "<html>" +
                                "<head>" +
                                "  <meta charset='UTF-8'>" +
                                "  <style>" +
                                "    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                                "    .container { padding: 20px; background-color: #f9f9f9; border-radius: 8px; }" +
                                "    .header { font-size: 18px; font-weight: bold; color: #2c3e50; }" +
                                "    .voucher { font-size: 20px; font-weight: bold; color: #e74c3c; }" +
                                "    .footer { margin-top: 20px; font-size: 14px; color: #555; }" +
                                "  </style>" +
                                "</head>" +
                                "<body>" +
                                "  <div class='container'>" +
                                "    <p class='header'>🎉 Xin chào " + khachHang.getTen() + ",</p>" +
                                "    <p>Cửa hàng chúng tôi rất vui mừng thông báo rằng bạn đã nhận được một <b>phiếu giảm giá đặc biệt</b> dành riêng cho bạn!</p>" +
                                "    <p>Mã phiếu giảm giá của bạn là:</p>" +
                                "    <p class='voucher'>" + voucher.getMa() + "</p>" +
                                "    <p>Hãy nhập mã này để nhận ưu đãi khi mua sắm tại cửa hàng.</p>" +
                                "    <div class='footer'>" +
                                "      <p>Cảm ơn bạn đã luôn tin tưởng và đồng hành cùng chúng tôi. 💖</p>" +
                                "      <p>Trân trọng,<br/>Đội ngũ Cửa hàng</p>" +
                                "    </div>" +
                                "  </div>" +
                                "</body>" +
                                "</html>";

                CompletableFuture.runAsync(() -> EmailService.sendEmail(email, subject, content));

            }
        }
        return new ResponseObject<>(voucher, HttpStatus.CREATED, "Tạo size thành công");
    }

    @Override
    public ResponseObject<?> changeSizeStatus(String id) {
        Optional<PhieuGiamGia> nemberOptional = advoucherRepository.findById(id);

        nemberOptional.map(voucher -> {
            voucher.setStatus(voucher.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
            return new ResponseObject(advoucherRepository.save(voucher), HttpStatus.OK, "Thay đổi trạng thái thành công");
        });

        return nemberOptional.map(product -> ResponseObject.successForward(HttpStatus.OK, "Đổi trạng thái thành công")).orElseGet(() -> ResponseObject.successForward(HttpStatus.NOT_FOUND, "Không tìm voucher"));
    }
}
