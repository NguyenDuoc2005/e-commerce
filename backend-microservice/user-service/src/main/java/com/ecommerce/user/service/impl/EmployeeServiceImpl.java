package com.ecommerce.user.service.impl;

import com.ecommerce.common.base.PageableObject;
import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.util.PageUtils;
import com.ecommerce.user.constant.EntityRole;
import com.ecommerce.user.constant.EntityStatus;
import com.ecommerce.user.constant.EntityVaiTro;
import com.ecommerce.user.entity.NhanVien;
import com.ecommerce.user.model.request.ADNhanVienSearchRequest;
import com.ecommerce.user.model.request.UserUpsertRequest;
import com.ecommerce.user.repository.NhanVienRepository;
import com.ecommerce.user.service.EmployeeService;
import com.ecommerce.user.service.FileStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final String DEFAULT_PASSWORD_HASH = "$2y$10$ey6ASnw6etj4YQtRFKZTjOlzjynNjDYgKXzf9/LDibTIXjEOdOgwa";

    private final NhanVienRepository nhanVienRepository;

    private final FileStorageService fileStorageService;

    public EmployeeServiceImpl(NhanVienRepository nhanVienRepository, FileStorageService fileStorageService) {
        this.nhanVienRepository = nhanVienRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public ResponseObject<?> getAllNhanVien(ADNhanVienSearchRequest request) {
        Pageable pageable = PageUtils.createPageable(request, "created_date");
        Page<NhanVien> page = request.getQ() == null || request.getQ().isEmpty()
                ? nhanVienRepository.findAll(pageable)
                : nhanVienRepository.findByMaContainingOrTenContaining(request.getQ(), request.getQ(), pageable);
        return new ResponseObject<>(PageableObject.of(page), HttpStatus.OK, "Lay danh sach nhan vien thanh cong");
    }

    @Override
    public ResponseObject<?> getNhanVienById(String id) {
        return nhanVienRepository.findById(id)
                .map(nhanVien -> new ResponseObject<>(nhanVien, HttpStatus.OK, "Nhan vien thanh cong"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay Mau sac"));
    }

    @Override
    public ResponseObject<?> modifyNhanVien(UserUpsertRequest request) {
        if (request.getCccd() != null && checkDuplicateField("cccd", request.getCccd(), request.getId())) {
            throw new IllegalArgumentException("Ma dinh danh (CCCD) da ton tai!");
        }
        if (request.getSdt() != null && checkDuplicateField("sdt", request.getSdt(), request.getId())) {
            throw new IllegalArgumentException("So dien thoai da ton tai!");
        }
        if (request.getEmail() != null && checkDuplicateField("email", request.getEmail(), request.getId())) {
            throw new IllegalArgumentException("Email da ton tai!");
        }

        if (StringUtils.hasLength(request.getId())) {
            Optional<NhanVien> existing = nhanVienRepository.findById(request.getId());
            if (existing.isPresent()) {
                NhanVien nhanVien = existing.get();
                applyRequest(nhanVien, request);
                nhanVien.setChucVu(EntityRole.STAFF);
                nhanVien.setStatus(EntityStatus.ACTIVE);
                saveAvatarIfPresent(nhanVien, request);
                nhanVienRepository.save(nhanVien);
                return new ResponseObject<>(nhanVien, HttpStatus.OK, "Cap nhat nhan vien thanh cong");
            }
        }

        NhanVien nhanVien = new NhanVien();
        applyRequest(nhanVien, request);
        nhanVien.setMa(generateCode(request.getTen()));
        nhanVien.setVaitro(EntityVaiTro.NHAN_VIEN);
        nhanVien.setMatKhau(DEFAULT_PASSWORD_HASH);
        nhanVien.setChucVu(EntityRole.STAFF);
        nhanVien.setStatus(EntityStatus.ACTIVE);
        nhanVienRepository.save(nhanVien);
        saveAvatarIfPresent(nhanVien, request);
        nhanVienRepository.save(nhanVien);

        return new ResponseObject<>(nhanVien, HttpStatus.CREATED, "Tao nhan vien thanh cong");
    }

    @Override
    public ResponseObject<?> changeNhanVienStatus(String id) {
        Optional<NhanVien> optional = nhanVienRepository.findById(id);
        if (optional.isEmpty()) {
            return new ResponseObject<>(HttpStatus.NOT_FOUND, HttpStatus.OK, "Khong tim nhan vien ");
        }

        NhanVien nhanVien = optional.get();
        nhanVien.setStatus(nhanVien.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        nhanVienRepository.save(nhanVien);
        return new ResponseObject<>(HttpStatus.OK, HttpStatus.OK, "Doi trang thai thanh cong");
    }

    @Override
    public ResponseObject<?> changeNhanVienRole(String id) {
        Optional<NhanVien> optional = nhanVienRepository.findById(id);
        if (optional.isEmpty()) {
            return new ResponseObject<>(HttpStatus.NOT_FOUND, HttpStatus.OK, "Khong tim nhan vien ");
        }

        NhanVien nhanVien = optional.get();
        nhanVien.setChucVu(nhanVien.getChucVu() == EntityRole.ADMIN ? EntityRole.STAFF : EntityRole.ADMIN);
        nhanVienRepository.save(nhanVien);
        return new ResponseObject<>(HttpStatus.OK, HttpStatus.OK, "Doi vai tro nhan vien thanh cong");
    }

    @Override
    public boolean checkDuplicateField(String field, String value, String excludeId) {
        String normalizedExcludeId = excludeId == null ? "" : excludeId;
        return switch (field.toLowerCase()) {
            case "cccd" -> nhanVienRepository.existsByCccdAndIdNot(value, normalizedExcludeId);
            case "sdt" -> nhanVienRepository.existsBySdtAndIdNot(value, normalizedExcludeId);
            case "email" -> nhanVienRepository.existsByEmailAndIdNot(value, normalizedExcludeId);
            default -> throw new IllegalArgumentException("Truong khong hop le: " + field);
        };
    }

    private void applyRequest(NhanVien nhanVien, UserUpsertRequest request) {
        if (StringUtils.hasLength(request.getCode())) {
            nhanVien.setMa(request.getCode());
        }
        nhanVien.setTen(request.getTen());
        nhanVien.setEmail(request.getEmail());
        nhanVien.setSdt(request.getSdt());
        nhanVien.setDiaChi(request.getDiaChi());
        nhanVien.setGioiTimh(request.getGioiTinh());
        nhanVien.setXa(request.getXa());
        nhanVien.setHuyen(request.getHuyen());
        nhanVien.setTinh(request.getTinh());
        nhanVien.setNgaySinh(request.getNgaySinh());
        nhanVien.setCccd(request.getCccd());
    }

    private void saveAvatarIfPresent(NhanVien nhanVien, UserUpsertRequest request) {
        try {
            String avatarPath = fileStorageService.uploadAvatar(request.getAvatar(), nhanVien.getId());
            if (avatarPath != null) {
                nhanVien.setAvatar(avatarPath);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Loi khi doc file anh: " + ex.getMessage(), ex);
        }
    }

    private String generateCode(String fullName) {
        Random random = new Random();
        int number = random.nextInt(10000);
        return String.format("%s%03d", generateShortName(fullName), number);
    }

    private String generateShortName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().toLowerCase().split("\\s+");
        StringBuilder shortName = new StringBuilder(parts[parts.length - 1]);
        for (int i = 0; i < parts.length - 1; i++) {
            shortName.append(parts[i].charAt(0));
        }
        return shortName.toString();
    }
}
