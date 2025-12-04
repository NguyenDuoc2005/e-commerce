package com.be.server.core.admin.banhang.service.impl;

import com.be.server.core.admin.SanPhamChiTiet.model.request.ADSPCTSearchRequest;
import com.be.server.core.admin.SanPhamChiTiet.model.response.ADSanPhamChiTietResponse;
import com.be.server.core.admin.SanPhamChiTiet.repository.ADSanPhamChiTietRepository;
import com.be.server.core.admin.banhang.model.request.ADCapNhatPhuongTHucThanhToanRequest;
import com.be.server.core.admin.banhang.model.request.ADHuyRequest;
import com.be.server.core.admin.banhang.model.request.ADNhanVienRequest;
import com.be.server.core.admin.banhang.model.request.ADThanhToanRequest;
import com.be.server.core.admin.banhang.model.request.ADThemKhachHangRequest;
import com.be.server.core.admin.banhang.model.request.ADThemSanPhamRequest;
import com.be.server.core.admin.banhang.model.request.ADXoaSanPhamRequest;
import com.be.server.core.admin.banhang.model.request.ChonPhieuGiamGiaRequest;
import com.be.server.core.admin.banhang.model.request.ListKhachHangRequest;
import com.be.server.core.admin.banhang.model.request.ListSanPhamRequest;
import com.be.server.core.admin.banhang.model.request.ThemMoiKhachHangRequest;
import com.be.server.core.admin.banhang.model.request.giaoHangRequest;
import com.be.server.core.admin.banhang.model.response.ADChonKhachHangRespones;
import com.be.server.core.admin.banhang.model.response.ADGioHangRespones;
import com.be.server.core.admin.banhang.model.response.ADPhuongThucThanhToanRespones;
import com.be.server.core.admin.banhang.model.response.ADThanhToanRespones;
import com.be.server.core.admin.banhang.model.response.ListHoaDon;
import com.be.server.core.admin.banhang.repository.ADSanPhamBanHangRepository;
import com.be.server.core.admin.banhang.repository.ADTaoHoaDonChiTietRepository;
import com.be.server.core.admin.banhang.repository.ADTaoHoaDonRepository;
import com.be.server.core.admin.banhang.service.ADBanHangService;
import com.be.server.core.admin.hoadon.repository.ADLichSuThanhToanRepository;
import com.be.server.core.admin.khachhang.repository.ADKhachHangRepository;
import com.be.server.core.admin.nhanvien.repository.ADNhanVienRepository;
import com.be.server.core.admin.voucher.model.response.ADPhieuGiamGiaResponse;
import com.be.server.core.admin.voucher.repository.ADVoucherRepository;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.HoaDon;
import com.be.server.entity.HoaDonChiTiet;
import com.be.server.entity.KhachHang;
import com.be.server.entity.LichSuThanhToan;
import com.be.server.entity.LichSuTrangThaiHoaDon;
import com.be.server.entity.NhanVien;
import com.be.server.entity.PhieuGiamGia;
import com.be.server.entity.SanPhamChiTiet;
import com.be.server.infrastructure.constant.EntityLoaiHoaDon;
import com.be.server.infrastructure.constant.EntityPhuongThucThanhToan;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import com.be.server.repository.LichSuTrangThaiHoaDonRepository;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ADBanHangServiceImpl implements ADBanHangService {

    public final ADTaoHoaDonRepository adTaoHoaDonRepository;
    public final ADSanPhamChiTietRepository adSanPhamRepository;
    public final ADTaoHoaDonChiTietRepository adTaoHoaDonChiTietRepository;
    public final ADKhachHangRepository adKhachHangRepository;
    public final ADSanPhamBanHangRepository adSanPhamBanHangRepository;
    public final ADNhanVienRepository adNhanVienRepository;
    private final LichSuTrangThaiHoaDonRepository lichSuTrangThaiHoaDonRepository;
    public final ADLichSuThanhToanRepository adLichSuThanhToanRepository;
    public final ADVoucherRepository adVoucherRepository;

    @Override
    public List<ListHoaDon> getHoaDon() {
        return adTaoHoaDonRepository.getAll();
    }

    @Override
    public ResponseObject<?> createHoaDon(ADNhanVienRequest adNhanVienRequest) {


        HoaDon hoaDon = new HoaDon();

        hoaDon.setTongTien(0d);

        hoaDon.setTongTienSauGiam(0.0);

        NhanVien nhanVien = adNhanVienRepository.findById(adNhanVienRequest.getIdNV()).get();

        hoaDon.setNhanVien(nhanVien);

        hoaDon.setTrangThaiHoaDon(EntityTrangThaiHoaDon.CHO_XAC_NHAN);

        hoaDon.setLoaiHoaDon(EntityLoaiHoaDon.OFFLINE);

        adTaoHoaDonRepository.save(hoaDon);

        LichSuTrangThaiHoaDon lichSuTrangThaiHoaDon = new LichSuTrangThaiHoaDon();

        lichSuTrangThaiHoaDon.setThoiGian(LocalDateTime.now());

        lichSuTrangThaiHoaDon.setNote("Đơn hàng đã được tạo và đang chờ xử lý.");

        lichSuTrangThaiHoaDon.setHoaDon(hoaDon);

        lichSuTrangThaiHoaDon.setThoiGian(LocalDateTime.now());

        lichSuTrangThaiHoaDon.setTrangThai(EntityTrangThaiHoaDon.CHO_XAC_NHAN);

        lichSuTrangThaiHoaDonRepository.save(lichSuTrangThaiHoaDon);
        return new ResponseObject<>(hoaDon, HttpStatus.CREATED, "Tạo hóa đơn thành công");
    }

    @Override
    public ResponseObject<?> createThemSanPham(ADThemSanPhamRequest request) {

        List<String> idHDCT = adTaoHoaDonChiTietRepository.checkGioHang(request);

        System.out.println(idHDCT.size());

        if (idHDCT == null || idHDCT.size() <= 0) {
            HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();

            SanPhamChiTiet sanPham = adSanPhamRepository.findById(request.getIdSP()).get();


            if (sanPham.getSoLuong() < Integer.valueOf(request.getSoLuong())) {
                return new ResponseObject<>(null, HttpStatus.OK, "Số lượng sản phẩm thêm vào nhiều hơn số lượng trong kho");
            } else {


                HoaDon hoaDon = adTaoHoaDonRepository.findById(request.getIdHD()).get();

                hoaDonChiTiet.setHoaDon(hoaDon);

                hoaDonChiTiet.setSpct(sanPham);

                hoaDonChiTiet.setSoLuong(Integer.parseInt(request.getSoLuong()));

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

                if (sanPham.getSoLuong() < Integer.valueOf(request.getSoLuong())) {
                    return new ResponseObject<>(null, HttpStatus.OK, "Số lượng sản phẩm thêm vào nhiều hơn số lượng trong kho");
                } else {
                    HoaDonChiTiet hoaDonChiTiet1 = new HoaDonChiTiet();

                    HoaDon hoaDon = adTaoHoaDonRepository.findById(request.getIdHD()).get();

                    hoaDonChiTiet1.setHoaDon(hoaDon);

                    hoaDonChiTiet1.setSpct(sanPham);

                    hoaDonChiTiet1.setSoLuong(Integer.parseInt(request.getSoLuong()));

                    double gia = sanPham.getGiaBan();

                    hoaDonChiTiet1.setGia(gia);

                    adTaoHoaDonChiTietRepository.save(hoaDonChiTiet1);

                    return new ResponseObject<>(null, HttpStatus.OK, "Sản phẩm này đang đc thay đổi giá từ " + (hoaDonChiTiet.getGia()) + "đ thành " + sanPham.getGiaBan());

                }
            } else {
                hoaDonChiTiet.setSoLuong(hoaDonChiTiet.getSoLuong() + Integer.parseInt(request.getSoLuong()));

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
    public List<ADGioHangRespones> getListGioHang(String id) {

        return adTaoHoaDonChiTietRepository.getAllGioHang(id);
    }

    @Override
    public void xoaHoaDonChiTiet(ADXoaSanPhamRequest adXoaSanPhamRequest) {


        String idHDCT = adTaoHoaDonChiTietRepository.getHoaDonChiTiet(adXoaSanPhamRequest);


        HoaDonChiTiet hoaDonChiTiet = adTaoHoaDonChiTietRepository.findById(idHDCT).get();

        adTaoHoaDonChiTietRepository.delete(hoaDonChiTiet);
    }


    @Override
    public ResponseObject<?> ThemSoLuong(ADXoaSanPhamRequest adXoaSanPhamRequest) {

        HoaDonChiTiet hoaDonChiTiet1 = adTaoHoaDonChiTietRepository.findById(adXoaSanPhamRequest.getIdHDCT()).get();

        SanPhamChiTiet sanPham1 = adSanPhamRepository.findById(adXoaSanPhamRequest.getIdSP()).get();

        double gia1 = hoaDonChiTiet1.getGia();
        double gia2 = sanPham1.getGiaBan();
        double epsilon = 0.0001;

        if (Math.abs(gia1 - gia2) > epsilon) {

//            HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
//
//            HoaDon hoaDon = adTaoHoaDonRepository.findById(adXoaSanPhamRequest.getIdHD()).get();
//
//            hoaDonChiTiet.setHoaDon(hoaDon);
//
//            hoaDonChiTiet.setSpct(sanPham1);
//
//            hoaDonChiTiet.setGia(sanPham1.getGiaBan());
//
//            hoaDonChiTiet.setSoLuong(1);
//
//            hoaDonChiTiet.setStatus(EntityStatus.ACTIVE);
//
//            adTaoHoaDonChiTietRepository.save(hoaDonChiTiet);

            return new ResponseObject<>(null, HttpStatus.OK, "Sản phẩm này đang đc thay đổi giá từ " + (hoaDonChiTiet1.getGia()) + "đ thành " + sanPham1.getGiaBan());
        }

        SanPhamChiTiet sanPham = adSanPhamRepository.findById(adXoaSanPhamRequest.getIdSP()).get();
//
//
//        String idHDCT = adTaoHoaDonChiTietRepository.getHoaDonChiTiet(adXoaSanPhamRequest);

        HoaDonChiTiet hoaDonChiTiet = adTaoHoaDonChiTietRepository.findById(adXoaSanPhamRequest.getIdHDCT()).get();

        hoaDonChiTiet.setSoLuong(hoaDonChiTiet.getSoLuong() + 1);

        if (sanPham.getSoLuong() < Integer.valueOf(hoaDonChiTiet.getSoLuong())) {

            return new ResponseObject<>(null, HttpStatus.OK, "Số lượng sản phẩm thêm vào nhiều hơn số lượng trong kho");
        }

        adTaoHoaDonChiTietRepository.save(hoaDonChiTiet);

        return new ResponseObject<>(null, HttpStatus.OK, "");
    }

    @Override
    public void xoaSoLuong(ADXoaSanPhamRequest adXoaSanPhamRequest) {
//
//        String idHDCT = adTaoHoaDonChiTietRepository.getHoaDonChiTiet(adXoaSanPhamRequest);

        HoaDonChiTiet hoaDonChiTiet = adTaoHoaDonChiTietRepository.findById(adXoaSanPhamRequest.getIdHDCT()).get();

        hoaDonChiTiet.setSoLuong(hoaDonChiTiet.getSoLuong() - 1);

        if (hoaDonChiTiet.getSoLuong() == 0) {
            adTaoHoaDonChiTietRepository.delete(hoaDonChiTiet);
        } else {
            adTaoHoaDonChiTietRepository.save(hoaDonChiTiet);
        }


    }

    @Override
    public ResponseObject<?> listKhachHang(ListKhachHangRequest listKhachHangRequest) {

        Pageable pageable = Helper.createPageable(listKhachHangRequest, "created_date");
        Page<ADChonKhachHangRespones> page = adTaoHoaDonChiTietRepository.getAllList(listKhachHangRequest, pageable);
        return new ResponseObject<>(PageableObject.of(page), HttpStatus.OK, "lấy danh sách khách hàng thanh công");
    }

    @Override
    public void themKhachHang(ADThemKhachHangRequest id) {

        KhachHang khachHang = adKhachHangRepository.findById(id.getIdKH()).get();

        HoaDon hoaDon = adTaoHoaDonRepository.findById(id.getIdHD()).get();

        hoaDon.setKhachHang(khachHang);

        adTaoHoaDonRepository.save(hoaDon);

    }

    @Override
    public ResponseObject<?> themMoiKhachHang(ThemMoiKhachHangRequest adXoaSanPhamRequest) {

        KhachHang khachHang = new KhachHang();

        khachHang.setTen(adXoaSanPhamRequest.getTen());

        khachHang.setSdt(adXoaSanPhamRequest.getSdt());

        khachHang.setStatus(EntityStatus.ACTIVE);

        adKhachHangRepository.save(khachHang);

        return new ResponseObject<>(khachHang, HttpStatus.OK, "thêm mới khách hàng thành công");
    }

    @Override
    public ADChonKhachHangRespones getKhachHang(String id) {

        return adTaoHoaDonChiTietRepository.getKhachHang(id);
    }

    @Override
    public ADThanhToanRespones getThanhToan(String id) {
        return adTaoHoaDonChiTietRepository.getThanhToan(id);
    }

    @Override
    public List<ADPhuongThucThanhToanRespones> getPhuongThucThanhToan(String id) {
        return adTaoHoaDonChiTietRepository.getPhuongThucThanhToan(id);
    }

    @Override
    public void capNhatPhuongThucThanhToan(ADCapNhatPhuongTHucThanhToanRequest id) {

        HoaDon hoaDon = adTaoHoaDonRepository.findById(id.getIdHD()).get();
        if (id.getPhuongThucThanhToan().equals("0")) {

            hoaDon.setPhuongThucThanhToan(EntityPhuongThucThanhToan.TIEN_MAT);
        } else if (id.getPhuongThucThanhToan().equals("1")) {

            hoaDon.setPhuongThucThanhToan(EntityPhuongThucThanhToan.CHUYEN_KHOAN);
        } else {

            hoaDon.setPhuongThucThanhToan(EntityPhuongThucThanhToan.TIEN_MAT_CHUYEN_KHOAN);
        }
        adTaoHoaDonRepository.save(hoaDon);

    }

    @Override
    public ResponseObject<?> getAll(ListSanPhamRequest id) {
        Pageable pageable = Helper.createPageable(id, "created_date");

//        if (id.getStatus() != null && !id.getStatus().isEmpty()) {
//            if (id.getStatus().equals("0")) {
//                id.setEntityStatus(EntityStatus.INACTIVE);
//            } else {
//                id.setEntityStatus(EntityStatus.ACTIVE);
//            }
//        }

        Page<ADSanPhamChiTietResponse> page = adSanPhamBanHangRepository.getAllSanPhamChiTietByFilter(pageable, id);

        return new ResponseObject<>(PageableObject.of(page), HttpStatus.OK, "Lấy danh sách sản phẩm chi tiết thành công");
    }

    @Override
    public ResponseObject<?> thanhToanThanhCong(ADThanhToanRequest id) {


        if (id.getIdPGG() != null) {

            PhieuGiamGia phieuGiamGia1 = adVoucherRepository.findById(id.getIdPGG()).get();

            if (phieuGiamGia1.getStatus() == EntityStatus.INACTIVE) {
                return new ResponseObject<>(null, HttpStatus.OK, "Phiếu giảm giá đã ngừng hoạt động");
            }

            HoaDon hoaDon = adTaoHoaDonRepository.findById(id.getIdHD()).get();

            ChonPhieuGiamGiaRequest chonPhieuGiamGiaRequest = new ChonPhieuGiamGiaRequest();

            chonPhieuGiamGiaRequest.setIdHD(id.getTienHang());

            if (hoaDon.getKhachHang() != null) {

                chonPhieuGiamGiaRequest.setIdKH(hoaDon.getKhachHang().getId());

            }

            if (id.getCheck() == 1) {
                List<PhieuGiamGia> list = danhSachPhieuGiamGia1(chonPhieuGiamGiaRequest);

                if (list.get(0).getGiaTriGiamThucTe() > phieuGiamGia1.getGiaTriGiamThucTe()) {

                    return new ResponseObject<>(null, HttpStatus.OK, "Đã có 1 phiếu giảm giá tốt hơn");

                }
            }


        }


        List<String> idHDCTS = adTaoHoaDonChiTietRepository.getHoaDonChiTiet(id.getIdHD());

        for (int i = 0; i < idHDCTS.size(); i++) {
            HoaDonChiTiet hoaDonChiTiet = adTaoHoaDonChiTietRepository.findById(idHDCTS.get(i)).get();

            String idSPCT = adTaoHoaDonChiTietRepository.getSanPhamChiTiet(idHDCTS.get(i));

            SanPhamChiTiet sanPhamChiTiet = adSanPhamBanHangRepository.findById(idSPCT).get();

            if (sanPhamChiTiet.getSoLuong() < hoaDonChiTiet.getSoLuong()) {

                return new ResponseObject<>(null, HttpStatus.OK, "Số lượng sản phẩm không đủ");

            }

            sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() - hoaDonChiTiet.getSoLuong());

            adSanPhamBanHangRepository.save(sanPhamChiTiet);

        }


        HoaDon hoaDon = adTaoHoaDonRepository.findById(id.getIdHD()).get();

        List<String> idSHDCT = adTaoHoaDonRepository.getHDCT(id.getIdHD());


        if (hoaDon.getLoaiHoaDon() == EntityLoaiHoaDon.GIAO_HANG) {
            hoaDon.setTrangThaiHoaDon(EntityTrangThaiHoaDon.DA_XAC_NHAN);

            hoaDon.setTongTien(id.getTienHang());

            hoaDon.setTen(id.getTen());

            hoaDon.setDiaChi(id.getDiaChi());

            hoaDon.setSdt(id.getSdt());

            if (id.getPhuongThucThanhToan().equals("0")) {
                hoaDon.setPhuongThucThanhToan(EntityPhuongThucThanhToan.TIEN_MAT);
            } else if (id.getPhuongThucThanhToan().equals("1")) {
                hoaDon.setPhuongThucThanhToan(EntityPhuongThucThanhToan.CHUYEN_KHOAN);
            } else {
                hoaDon.setPhuongThucThanhToan(EntityPhuongThucThanhToan.TIEN_MAT_CHUYEN_KHOAN);
            }

            hoaDon.setPhiVanChuyen(id.getTienShip());

            System.out.println("giảm giá"+id.getGiamGia());

            hoaDon.setGiamGia(id.getGiamGia());

            hoaDon.setTongTienSauGiam(id.getTongTien());

            if (id.getIdPGG() != null) {

                PhieuGiamGia phieuGiamGia = adVoucherRepository.findById(id.getIdPGG()).get();

                phieuGiamGia.setSoLuongPhieu(phieuGiamGia.getSoLuongPhieu() - 1);

                adVoucherRepository.save(phieuGiamGia);

                hoaDon.setVoucher(phieuGiamGia);

            }

            adTaoHoaDonRepository.save(hoaDon);


            String idLSTT = adTaoHoaDonChiTietRepository.getHoaDonLichSuTrangThai(id.getIdHD());

            System.out.println(idLSTT);

            LichSuTrangThaiHoaDon lichSuTrangThaiHoaDon = new LichSuTrangThaiHoaDon();

            lichSuTrangThaiHoaDon.setThoiGian(LocalDateTime.now());

            lichSuTrangThaiHoaDon.setNote("Đơn hàng đã được xác nhận và chờ giao cho đơn vị vận chuyển.");

            lichSuTrangThaiHoaDon.setHoaDon(hoaDon);

            lichSuTrangThaiHoaDon.setTrangThai(EntityTrangThaiHoaDon.DA_XAC_NHAN);

            lichSuTrangThaiHoaDonRepository.save(lichSuTrangThaiHoaDon);

            System.out.println("xong trạng thái");

            HoaDon hoaDon1 = adTaoHoaDonRepository.findById(id.getIdHD()).orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

            NhanVien nhanVien = adNhanVienRepository.findById(id.getIdNV()).get();

//            LichSuThanhToan lichSu = new LichSuThanhToan();
//            lichSu.setHoaDon(hoaDon1);
//            lichSu.setSoTien(id.getTongTien());
//            if(id.getPhuongThucThanhToan().equals("0")) {
//                lichSu.setLoaiGiaoDich("TIEN_MAT");
//            }else if(id.getPhuongThucThanhToan().equals("1")) {
//                lichSu.setLoaiGiaoDich("CHUYEN_KHOAN");
//            }else {
//                lichSu.setLoaiGiaoDich("TIEN_MAT_CHUYEN_KHOAN");
//            }
//            lichSu.setThoiGian(LocalDateTime.now());
//            lichSu.setNhanVien(nhanVien);
//            lichSu.setMaGiaoDich(UUID.randomUUID().toString());
//
//            adLichSuThanhToanRepository.save(lichSu);


            if (id.getPhuongThucThanhToan().equals("1")) {
                LichSuThanhToan lichSu = new LichSuThanhToan();
                lichSu.setHoaDon(hoaDon1);
                lichSu.setSoTien(id.getTongTien());
                lichSu.setLoaiGiaoDich(id.getPhuongThucThanhToan());
                lichSu.setThoiGian(LocalDateTime.now());
                lichSu.setNhanVien(nhanVien);
                lichSu.setMaGiaoDich(UUID.randomUUID().toString());

                adLichSuThanhToanRepository.save(lichSu);
            }



            List<String> idHDCT = adTaoHoaDonChiTietRepository.getHoaDonChiTiet(id.getIdHD());

            System.out.println("hdct" + idHDCT);

            for (int i = 0; i < idHDCT.size(); i++) {
                System.out.println("hdct" + idHDCT.get(i));
                HoaDonChiTiet hoaDonChiTiet = adTaoHoaDonChiTietRepository.findById(idHDCT.get(i)).get();

                String idSPCT = adTaoHoaDonChiTietRepository.getSanPhamChiTiet(idHDCT.get(i));

                System.out.println("spct" + idSPCT);

                SanPhamChiTiet sanPhamChiTiet = adSanPhamBanHangRepository.findById(idSPCT).get();

                sanPhamChiTiet.setSoLuong(sanPhamChiTiet.getSoLuong() - hoaDonChiTiet.getSoLuong());

                System.out.println(sanPhamChiTiet.getSoLuong());
                adSanPhamBanHangRepository.save(sanPhamChiTiet);
            }

            return new ResponseObject<>(null, HttpStatus.CREATED, "Xác nhận giao hàng thành công");

        }

        hoaDon.setTrangThaiHoaDon(EntityTrangThaiHoaDon.HOAN_THANH);

        hoaDon.setTongTien(id.getTienHang());
        System.out.println("giảm giá"+id.getGiamGia());
        hoaDon.setGiamGia(id.getGiamGia());

        hoaDon.setTen(id.getTen());

        hoaDon.setDiaChi(id.getDiaChi());

        System.out.println("phương thức"+id.getPhuongThucThanhToan());

        if (id.getPhuongThucThanhToan().equals("0")) {
            hoaDon.setPhuongThucThanhToan(EntityPhuongThucThanhToan.TIEN_MAT);
        } else if (id.getPhuongThucThanhToan().equals("1")) {
            hoaDon.setPhuongThucThanhToan(EntityPhuongThucThanhToan.CHUYEN_KHOAN);
        } else {
            hoaDon.setPhuongThucThanhToan(EntityPhuongThucThanhToan.TIEN_MAT_CHUYEN_KHOAN);
        }

        hoaDon.setSdt(id.getSdt());

        hoaDon.setPhiVanChuyen(id.getTienShip());

        hoaDon.setTongTienSauGiam(id.getTongTien());

        if (id.getIdPGG() != null) {

            PhieuGiamGia phieuGiamGia = adVoucherRepository.findById(id.getIdPGG()).get();

            phieuGiamGia.setSoLuongPhieu(phieuGiamGia.getSoLuongPhieu() - 1);

            adVoucherRepository.save(phieuGiamGia);

            hoaDon.setVoucher(phieuGiamGia);

        }


        adTaoHoaDonRepository.save(hoaDon);


        String idLSTT = adTaoHoaDonChiTietRepository.getHoaDonLichSuTrangThai(id.getIdHD());

        System.out.println(idLSTT);

        LichSuTrangThaiHoaDon lichSuTrangThaiHoaDon = new LichSuTrangThaiHoaDon();

        lichSuTrangThaiHoaDon.setThoiGian(LocalDateTime.now());

        lichSuTrangThaiHoaDon.setNote("Đơn hàng đã được khách hàng thanh toán thành công.");

        lichSuTrangThaiHoaDon.setHoaDon(hoaDon);

        lichSuTrangThaiHoaDon.setTrangThai(EntityTrangThaiHoaDon.HOAN_THANH);

        lichSuTrangThaiHoaDonRepository.save(lichSuTrangThaiHoaDon);

        System.out.println("xong trạng thái");

        HoaDon hoaDon1 = adTaoHoaDonRepository.findById(id.getIdHD()).orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        NhanVien nhanVien = adNhanVienRepository.findById(id.getIdNV()).get();

        LichSuThanhToan lichSu = new LichSuThanhToan();
        lichSu.setHoaDon(hoaDon1);
        lichSu.setSoTien(id.getTongTien());
        lichSu.setLoaiGiaoDich(id.getPhuongThucThanhToan());
        lichSu.setThoiGian(LocalDateTime.now());
        lichSu.setNhanVien(nhanVien);
        lichSu.setMaGiaoDich(UUID.randomUUID().toString());

        adLichSuThanhToanRepository.save(lichSu);

        System.out.println("phuương thức 1"+ hoaDon1.getPhuongThucThanhToan());
        System.out.println("phuương thức 2"+ hoaDon.getPhuongThucThanhToan());

        return new ResponseObject<>(null, HttpStatus.CREATED, "Thanh toán thành công");

    }

    @Override
    public ResponseObject<?> danhSachPhieuGiamGia(ChonPhieuGiamGiaRequest id) {

        Double tongTien = id.getIdHD();

        if (tongTien == null) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Tổng tiền không được để trống");
        }

        List<PhieuGiamGia> phieuGiamGias = adTaoHoaDonRepository.getPhieuGiamGia(id.getIdKH(), id.getIdHD());
        if (phieuGiamGias == null) {
            phieuGiamGias = new ArrayList<>();
        }
        if (phieuGiamGias.size() == 0) {
            return new ResponseObject<>(null, HttpStatus.CREATED, "Lây giá trị phiếu giảm giá thành công");
        }

        System.out.println(phieuGiamGias.size());

        if (phieuGiamGias.size() > 0) {
            phieuGiamGias.forEach(pg -> {
                if (pg.getPhanTramGiam() != null && pg.getGiaGiam() != null) {
                    if (pg.getKieuGiam() == true) {
                        pg.setGiaTriGiamThucTe(tongTien * (pg.getPhanTramGiam() / 100));
                        if (pg.getGiaTriGiamThucTe() >= pg.getGiaGiam()) {
                            pg.setGiaTriGiamThucTe(pg.getGiaGiam());
                        }
                    } else {

                        pg.setGiaTriGiamThucTe(tongTien - (tongTien - pg.getPhanTramGiam()));
                    }
                }
            });
        }

        if (phieuGiamGias.size() > 0) {
            phieuGiamGias = phieuGiamGias.stream().sorted(Comparator.comparing(PhieuGiamGia::getGiaTriGiamThucTe, Comparator.reverseOrder())).collect(Collectors.toList());
        } else {
            phieuGiamGias = new ArrayList<>();
        }

        return new ResponseObject<>(phieuGiamGias, HttpStatus.CREATED, "Lây giá trị phiếu giảm giá thành công");

    }

    @Override
    public List<PhieuGiamGia> danhSachPhieuGiamGia1(ChonPhieuGiamGiaRequest id) {
        Double tongTien = id.getIdHD();

        if (tongTien == null) {

        }

        List<PhieuGiamGia> phieuGiamGias = adTaoHoaDonRepository.getPhieuGiamGia(id.getIdKH(), id.getIdHD());
        if (phieuGiamGias == null) {
            phieuGiamGias = new ArrayList<>();
        }
        if (phieuGiamGias.size() == 0) {

        }

        System.out.println(phieuGiamGias.size());

        if (phieuGiamGias.size() > 0) {
            phieuGiamGias.forEach(pg -> {
                if (pg.getPhanTramGiam() != null && pg.getGiaGiam() != null) {
                    if (pg.getKieuGiam() == true) {
                        pg.setGiaTriGiamThucTe(tongTien * (pg.getPhanTramGiam() / 100));
                        if (pg.getGiaTriGiamThucTe() >= pg.getGiaGiam()) {
                            pg.setGiaTriGiamThucTe(pg.getGiaGiam());
                        }
                    } else {

                        pg.setGiaTriGiamThucTe(tongTien - (tongTien - pg.getPhanTramGiam()));
                    }
                }
            });
        }

        if (phieuGiamGias.size() > 0) {
            phieuGiamGias = phieuGiamGias.stream().sorted(Comparator.comparing(PhieuGiamGia::getGiaTriGiamThucTe, Comparator.reverseOrder())).collect(Collectors.toList());
        } else {
            phieuGiamGias = new ArrayList<>();
        }

        return phieuGiamGias;
    }

    @Override
    public ResponseObject<?> danhSachPhieuGiamGiaKoDuDieuKien(ChonPhieuGiamGiaRequest id) {

        Double tongTien = id.getIdHD();

        if (tongTien == null) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Tổng tiền không được để trống");
        }

        List<PhieuGiamGia> phieuGiamGias = adTaoHoaDonRepository.getPhieuGiamGia(id.getIdKH(), id.getIdHD());
        if (phieuGiamGias == null) {
            phieuGiamGias = new ArrayList<>();
        }
        if (phieuGiamGias.size() == 0) {
            return new ResponseObject<>(null, HttpStatus.CREATED, "Lây giá trị phiếu giảm giá thành công");
        }


        if (phieuGiamGias.size() > 0) {
            phieuGiamGias.forEach(pg -> {
                if (pg.getPhanTramGiam() != null && pg.getGiaGiam() != null) {
                    if (pg.getKieuGiam() == true) {
                        pg.setGiaTriGiamThucTe(tongTien * (pg.getPhanTramGiam() / 100));
                        if (pg.getGiaTriGiamThucTe() >= pg.getGiaGiam()) {
                            pg.setGiaTriGiamThucTe(pg.getGiaGiam());
                        }
                    } else {
                        pg.setGiaTriGiamThucTe(tongTien - (tongTien - pg.getPhanTramGiam()));
                    }
                }
            });
        }

        return new ResponseObject<>(phieuGiamGias, HttpStatus.CREATED, "Lây giá trị phiếu giảm giá thành công");

    }

    @Override
    public ResponseObject<?> giaoHang(String request) {
        System.out.println("idHD" + request);
        HoaDon hoaDon = adTaoHoaDonRepository.findById(request).get();

        hoaDon.setLoaiHoaDon(hoaDon.getLoaiHoaDon() == EntityLoaiHoaDon.OFFLINE ? EntityLoaiHoaDon.GIAO_HANG : EntityLoaiHoaDon.OFFLINE);

        adTaoHoaDonRepository.save(hoaDon);

        return new ResponseObject<>(null, HttpStatus.CREATED, "Lây giá trị phiếu giảm giá thành công");
    }

    @Override
    public ResponseObject<?> huy(ADHuyRequest request) {

        HoaDon hoaDon = adTaoHoaDonRepository.findById(request.getIdHD()).get();

        hoaDon.setTrangThaiHoaDon(EntityTrangThaiHoaDon.DA_HUY);


        adTaoHoaDonRepository.save(hoaDon);

        LichSuTrangThaiHoaDon lichSuTrangThaiHoaDon = new LichSuTrangThaiHoaDon();
        lichSuTrangThaiHoaDon.setHoaDon(hoaDon);
        lichSuTrangThaiHoaDon.setTrangThai(EntityTrangThaiHoaDon.DA_HUY);
        lichSuTrangThaiHoaDon.setThoiGian(LocalDateTime.now());

        lichSuTrangThaiHoaDonRepository.save(lichSuTrangThaiHoaDon);

        return new ResponseObject<>(null, HttpStatus.CREATED, "Hủy hóa đơn thành công");
    }


}
