package com.ecommerce.promotion.service.impl;

import com.ecommerce.common.base.PageableObject;
import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.util.PageUtils;
import com.ecommerce.promotion.constant.EntityStatus;
import com.ecommerce.promotion.entity.PhieuGiamGia;
import com.ecommerce.promotion.entity.PhieuGiamGiaChiTiet;
import com.ecommerce.promotion.model.request.VoucherRequest;
import com.ecommerce.promotion.model.request.VoucherSearchRequest;
import com.ecommerce.promotion.repository.KhachHangRepository;
import com.ecommerce.promotion.repository.PhieuGiamGiaChiTietRepository;
import com.ecommerce.promotion.repository.VoucherRepository;
import com.ecommerce.promotion.service.VoucherService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final KhachHangRepository khachHangRepository;
    private final PhieuGiamGiaChiTietRepository chiTietRepository;

    public VoucherServiceImpl(
            VoucherRepository voucherRepository,
            KhachHangRepository khachHangRepository,
            PhieuGiamGiaChiTietRepository chiTietRepository
    ) {
        this.voucherRepository = voucherRepository;
        this.khachHangRepository = khachHangRepository;
        this.chiTietRepository = chiTietRepository;
    }

    @Override
    public ResponseObject<?> getAllVoucher(VoucherSearchRequest request) {
        Pageable pageable = PageUtils.createPageable(request, "createdDate");
        if (request.getKieuGiam() != null) {
            request.setKieu(request.getKieuGiam() == 0);
        }
        if (request.getStatus() != null) {
            request.setEntityStatus(request.getStatus() == 0 ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        }
        return new ResponseObject<>(
                PageableObject.of(voucherRepository.getAllPhieuGiamGiaFilter(pageable, request)),
                HttpStatus.OK,
                "Lay danh sach phieu giam gia thanh cong"
        );
    }

    @Override
    public ResponseObject<?> getVoucherById(String id) {
        return voucherRepository.getVoucherById(id)
                .map(voucher -> new ResponseObject<>(voucher, HttpStatus.OK, "Lay phieu giam gia thanh cong"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay phieu giam gia"));
    }

    @Override
    public Page<String> getListKH(String id, String search, int page, int size) {
        return voucherRepository.getDanhSachKhachHang(id, search, PageRequest.of(page, size));
    }

    @Override
    public ResponseObject<?> modifyVoucher(VoucherRequest request) {
        if (StringUtils.hasLength(request.getId())) {
            Optional<PhieuGiamGia> existing = voucherRepository.findById(request.getId());
            if (existing.isPresent()) {
                PhieuGiamGia voucher = existing.get();
                applyRequest(voucher, request);
                voucherRepository.save(voucher);
                return new ResponseObject<>(voucher, HttpStatus.OK, "Cap nhat phieu giam gia thanh cong");
            }
        }

        if (voucherRepository.checkThemPhieu(request.getTen()) != null) {
            return new ResponseObject<>(null, HttpStatus.OK, "phieu giam gia nay da ton tai");
        }

        PhieuGiamGia voucher = new PhieuGiamGia();
        applyRequest(voucher, request);
        LocalDate today = LocalDate.now();
        LocalDate start = request.getNgayBatDau().toLocalDate();
        LocalDate end = request.getNgayKetThuc().toLocalDate();
        voucher.setStatus(!today.isBefore(start) && !today.isAfter(end) ? EntityStatus.ACTIVE : EntityStatus.INACTIVE);
        voucherRepository.save(voucher);

        if (request.getKhachHangIds() != null) {
            for (String khachHangId : request.getKhachHangIds()) {
                khachHangRepository.findById(khachHangId).ifPresent(khachHang -> {
                    PhieuGiamGiaChiTiet detail = new PhieuGiamGiaChiTiet();
                    detail.setPhieuGiamGia(voucher);
                    detail.setKhachHang(khachHang);
                    chiTietRepository.save(detail);
                });
            }
        }

        return new ResponseObject<>(voucher, HttpStatus.CREATED, "Tao size thanh cong");
    }

    @Override
    public ResponseObject<?> changeVoucherStatus(String id) {
        Optional<PhieuGiamGia> optional = voucherRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseObject.successForward(HttpStatus.NOT_FOUND, "Khong tim voucher");
        }
        PhieuGiamGia voucher = optional.get();
        voucher.setStatus(voucher.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        voucherRepository.save(voucher);
        return ResponseObject.successForward(HttpStatus.OK, "Doi trang thai thanh cong");
    }

    private void applyRequest(PhieuGiamGia voucher, VoucherRequest request) {
        voucher.setTen(request.getTen());
        voucher.setDieuKien(request.getDieuKien());
        voucher.setGiaGiam(request.getGiaGiam());
        voucher.setLoaiGiam(request.getLoaiGiam());
        voucher.setNgayBatDau(request.getNgayBatDau());
        voucher.setKieuGiam(request.getKieuGiam());
        voucher.setNgayKetThuc(request.getNgayKetThuc());
        voucher.setSoLuongPhieu(request.getSoLuongPhieu());
        if (Boolean.TRUE.equals(request.getKieuGiam())) {
            voucher.setPhanTramGiam(request.getLoiPhanNay());
        } else {
            voucher.setPhanTramGiam(request.getGiaGiam());
        }
    }
}
