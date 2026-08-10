package com.ecommerce.user.service.impl;

import com.ecommerce.common.base.PageableObject;
import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.util.PageUtils;
import com.ecommerce.user.client.OrderClient;
import com.ecommerce.user.constant.EntityStatus;
import com.ecommerce.user.entity.KhachHang;
import com.ecommerce.user.model.request.ADKhachHangSearchRequest;
import com.ecommerce.user.model.request.UserUpsertRequest;
import com.ecommerce.user.repository.KhachHangRepository;
import com.ecommerce.user.service.CustomerService;
import com.ecommerce.user.service.FileStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final String DEFAULT_PASSWORD_HASH = "$2y$10$ey6ASnw6etj4YQtRFKZTjOlzjynNjDYgKXzf9/LDibTIXjEOdOgwa";

    private final KhachHangRepository khachHangRepository;

    private final FileStorageService fileStorageService;

    private final OrderClient orderClient;

    public CustomerServiceImpl(KhachHangRepository khachHangRepository, FileStorageService fileStorageService, OrderClient orderClient) {
        this.khachHangRepository = khachHangRepository;
        this.fileStorageService = fileStorageService;
        this.orderClient = orderClient;
    }

    @Override
    public ResponseObject<?> getAllKhachHang(ADKhachHangSearchRequest request) {
        Pageable pageable = PageUtils.createPageable(request, "created_date");
        Page<KhachHang> page;
        if ((request.getQ() == null || request.getQ().isEmpty()) && request.getStatus() == null) {
            page = khachHangRepository.findAll(pageable);
        } else {
            if (request.getStatus() != null) {
                request.setEntityStatus(request.getStatus() == 1 ? EntityStatus.ACTIVE : EntityStatus.INACTIVE);
            }
            page = khachHangRepository.getAllKhachHang(pageable, request.getQ(), request.getEntityStatus());
        }

        return new ResponseObject<>(PageableObject.of(page), HttpStatus.OK, "Lay danh sach khach hang thanh cong");
    }

    @Override
    public ResponseObject<?> getKhachHangById(String id) {
        return khachHangRepository.findById(id)
                .map(khachHang -> new ResponseObject<>(khachHang, HttpStatus.OK, "Lay khach hang thanh cong"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay khach hang"));
    }

    @Override
    public ResponseObject<?> modifyKhachHang(UserUpsertRequest request) {
        if (StringUtils.hasLength(request.getId())) {
            Optional<KhachHang> existing = khachHangRepository.findById(request.getId());
            if (existing.isPresent()) {
                KhachHang khachHang = existing.get();
                applyRequest(khachHang, request);
                saveAvatarIfPresent(khachHang, request);
                khachHangRepository.save(khachHang);
                return new ResponseObject<>(khachHang, HttpStatus.OK, "Cap nhat khach hang thanh cong");
            }
        }

        KhachHang khachHang = new KhachHang();
        applyRequest(khachHang, request);
        khachHang.setMatKhau(DEFAULT_PASSWORD_HASH);
        khachHang.setStatus(EntityStatus.ACTIVE);
        khachHangRepository.save(khachHang);
        saveAvatarIfPresent(khachHang, request);
        khachHangRepository.save(khachHang);

        return new ResponseObject<>(khachHang, HttpStatus.CREATED, "Tao khach hang thanh cong");
    }

    @Override
    public ResponseObject<?> updateKhachHang(UserUpsertRequest request) {
        if (StringUtils.hasLength(request.getId())) {
            Optional<KhachHang> existing = khachHangRepository.findById(request.getId());
            if (existing.isPresent()) {
                KhachHang khachHang = existing.get();
                applyRequest(khachHang, request);
                saveAvatarIfPresent(khachHang, request);
                khachHangRepository.save(khachHang);
                return new ResponseObject<>(khachHang, HttpStatus.OK, "Cap nhat khach hang thanh cong");
            }
        }
        return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay khach hang");
    }

    @Override
    public ResponseObject<?> changeKhachHangStatus(String id) {
        Optional<KhachHang> optional = khachHangRepository.findById(id);
        if (optional.isEmpty()) {
            return new ResponseObject<>(HttpStatus.NOT_FOUND, HttpStatus.OK, "Khong tim size");
        }

        KhachHang khachHang = optional.get();
        khachHang.setStatus(khachHang.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        khachHangRepository.save(khachHang);
        return new ResponseObject<>(HttpStatus.OK, HttpStatus.OK, "Doi trang thai thanh cong");
    }

    @Override
    public ResponseObject<?> getLSKH(String id) {
        return new ResponseObject<>(orderClient.getCustomerOrderHistory(id), HttpStatus.OK, "Lay danh sach lich su hoa don thanh cong");
    }

    private void applyRequest(KhachHang khachHang, UserUpsertRequest request) {
        khachHang.setMa(request.getCode());
        khachHang.setTen(request.getTen());
        khachHang.setEmail(request.getEmail());
        khachHang.setSdt(request.getSdt());
        khachHang.setDiaChi(request.getDiaChi());
        khachHang.setXa(request.getXa());
        khachHang.setHuyen(request.getHuyen());
        khachHang.setTinh(request.getTinh());
        khachHang.setCccd(request.getCccd());
        khachHang.setNgaySinh(request.getNgaySinh());
        khachHang.setGioiTimh(request.getGioiTinh());
    }

    private void saveAvatarIfPresent(KhachHang khachHang, UserUpsertRequest request) {
        try {
            String avatarPath = fileStorageService.uploadAvatar(request.getAvatar(), khachHang.getId());
            if (avatarPath != null) {
                khachHang.setAvatar(avatarPath);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Loi khi doc file anh: " + ex.getMessage(), ex);
        }
    }
}
