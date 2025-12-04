package com.be.server.core.permitall.sanpham.controller;

import com.be.server.core.admin.sanpham.service.ADSanPhamService;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.core.permitall.sanpham.dto.request.PMDanhSachSanPhamSearchRequest;
import com.be.server.core.permitall.sanpham.dto.request.PMSanPhamMoiSearchRequest;
import com.be.server.core.permitall.sanpham.service.PMSanPhamService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_PERMITALL_SANPHAM)
public class PMSanPhamController {

    private final ADSanPhamService sanPhamService;

    public final PMSanPhamService PMSanPhamService;

    @GetMapping("/get-all/san-pham-giam-gia")
    public ResponseEntity<?> getAllSanPhamGiamGia(PMSanPhamMoiSearchRequest request) {
        return Helper.createResponseEntity(PMSanPhamService.getAllSanPhamGiamGia(request));
    }

    @GetMapping("/get-all/danh-sach-san-pham")
    public ResponseEntity<?> getDanhSachSanPham(PMDanhSachSanPhamSearchRequest request) {
        return Helper.createResponseEntity(PMSanPhamService.getDanhSachSanPham(request));
    }

    @GetMapping("/get-all/san-pham-moi")
    public ResponseEntity<?> getAllSanPhamMoi(PMSanPhamMoiSearchRequest request) {
        return Helper.createResponseEntity(PMSanPhamService.getAllSanPhamMoi(request));
    }

    @GetMapping("/list-thuong-hieu")
    public ResponseEntity<?> getListThuongHieu() {

        return Helper.createResponseEntity(sanPhamService.getListThuongHieu());

    }

    @GetMapping("/list-xuat-xu")
    public ResponseEntity<?> getListXuatXu() {

        return Helper.createResponseEntity(sanPhamService.getXuatXu());

    }

    @GetMapping("/list-size")
    public ResponseEntity<?> getListSize() {

        return Helper.createResponseEntity(sanPhamService.getListSize());

    }

    @GetMapping("/list-mau")
    public ResponseEntity<?> getListMau() {

        return Helper.createResponseEntity(sanPhamService.getListMau());

    }

    @GetMapping("/list-loai-de")
    public ResponseEntity<?> getListLoaiDe() {

        return Helper.createResponseEntity(sanPhamService.getListLoaiDe());

    }

    @GetMapping("/list-danh-muc")
    public ResponseEntity<?> getListDanhMuc() {

        return Helper.createResponseEntity(sanPhamService.getListDanhMuc());

    }

    @GetMapping("/list-chat-lieu")
    public ResponseEntity<?> getListChatLieu() {

        return Helper.createResponseEntity(sanPhamService.getListChetLieu());

    }
}
