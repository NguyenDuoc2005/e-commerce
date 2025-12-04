package com.be.server.core.permitall.chitietsanpham.service.impl;

import com.be.server.core.common.base.ResponseObject;
import com.be.server.core.permitall.chitietsanpham.dto.request.PMSanPhamChiTietRequest;
import com.be.server.core.permitall.chitietsanpham.dto.response.*;
import com.be.server.core.permitall.chitietsanpham.repository.PMCTSPDotGiamGiaCTSPRepo;
import com.be.server.core.permitall.chitietsanpham.repository.PMCTSPKichCoRepo;
import com.be.server.core.permitall.chitietsanpham.repository.PMCTSPSanPhamChiTietRepo;
import com.be.server.core.permitall.chitietsanpham.repository.PMCTSPSanPhamRepo;
import com.be.server.core.permitall.chitietsanpham.service.PMSanPhamChiTietService;
import com.be.server.entity.DotGiamGiaChiTietSanPham;
import com.be.server.entity.SanPham;
import com.be.server.entity.SanPhamChiTiet;
import com.be.server.infrastructure.constant.StatusPromotion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
public class PMSanPhamChiTietServiceImpl implements PMSanPhamChiTietService {

    private final PMCTSPSanPhamChiTietRepo pmCTSPSanPhamChiTietRepo;

    private final PMCTSPDotGiamGiaCTSPRepo pmctspDotGiamGiaCTSPRepo;

    private final PMCTSPKichCoRepo pmctspKichCoRepo;

    private final PMCTSPSanPhamRepo pmCTSPSanPhamRepo;

    @Override
    public ResponseObject<?> getSanPhamChiTiet(PMSanPhamChiTietRequest request) {
        Optional<SanPham> optionalSanPham = pmCTSPSanPhamRepo.findById(request.getIdSanPham());
        if (optionalSanPham.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm");
        }

        SanPham sanPham = optionalSanPham.get();

        // Map thông tin sản phẩm
        SanPhamDetailResponse response = new SanPhamDetailResponse();
        response.setTenSanPham(sanPham.getTen());
        response.setMoTa(sanPham.getMoTa());

        if (sanPham.getThuongHieu() != null) {
            response.setThuongHieu(new ThuongHieuDTO(sanPham.getThuongHieu()));
        }
        if (sanPham.getXuatSu() != null) {
            response.setXuatXu(new XuatXuDTO(sanPham.getXuatSu()));
        }
        if (sanPham.getDanhMuc() != null) {
            response.setDanhMuc(new DanhMucDTO(sanPham.getDanhMuc()));
        }
        if (sanPham.getLoaiDe() != null) {
            response.setLoaiDe(new LoaiDeDTO(sanPham.getLoaiDe()));
        }
        if (sanPham.getChatLieu() != null) {
            response.setChatLieu(new ChatLieuDTO(sanPham.getChatLieu()));
        }

        List<SanPhamChiTiet> ctspList = pmCTSPSanPhamChiTietRepo.findBySanPham(sanPham.getId());
        List<ChiTietSanPhamResponse> chiTietList = new ArrayList<>();

        for (SanPhamChiTiet ct : ctspList) {
            ChiTietSanPhamResponse ctResponse = new ChiTietSanPhamResponse();
            ctResponse.setId(ct.getId());

            if (ct.getMauSac() != null) {
                ctResponse.setMauSac(new MauSacDTO(ct.getMauSac()));
            }
            if (ct.getKichCo() != null) {
                ctResponse.setKichCo(new KichCoDTO(ct.getKichCo()));
            }

            ctResponse.setGiaBan(ct.getGiaBan());
            ctResponse.setSoLuong(ct.getSoLuong());
            ctResponse.setHinhAnh(ct.getAnh());

            // Map đợt giảm giá nếu có
            DotGiamGiaChiTietSanPham giamGia = pmctspDotGiamGiaCTSPRepo.findFirstBySanPhamChiTiet_Id(ct.getId());
            if (giamGia != null && giamGia.getDotGiamGia() != null && giamGia.getDotGiamGia().getTrangThai().equals(StatusPromotion.DANG_KICH_HOAT)
            ) {
                DotGiamGiaDTO dto = new DotGiamGiaDTO();
                dto.setTenDotGiamGia(giamGia.getDotGiamGia().getTen());
                dto.setPhanTramGiam(giamGia.getDotGiamGia().getPhanTramGiam());
                dto.setGiaTruoc(giamGia.getGiaTruoc());
                dto.setGiaSau(giamGia.getGiaSau());
                dto.setNgayBatDau(giamGia.getDotGiamGia().getNgayBatDau());
                dto.setNgayKetThuc(giamGia.getDotGiamGia().getNgayKetThuc());
                ctResponse.setDotGiamGia(dto);
            }

            chiTietList.add(ctResponse);
        }

        response.setChiTietSanPham(chiTietList);
        return new ResponseObject<>(response, HttpStatus.OK, "Lấy thành công sản phẩm chi tiết");
    }

}