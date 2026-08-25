package com.ecommerce.user.service.impl;

import com.ecommerce.common.base.PageableObject;
import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.util.PageUtils;
import com.ecommerce.user.constant.EntityRole;
import com.ecommerce.user.constant.EntityStatus;
import com.ecommerce.user.constant.EntityVaiTro;
import com.ecommerce.user.entity.Staff;
import com.ecommerce.user.model.request.ADStaffSearchRequest;
import com.ecommerce.user.model.request.UserUpsertRequest;
import com.ecommerce.user.repository.StaffRepository;
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

    private final StaffRepository nhanVienRepository;

    private final FileStorageService fileStorageService;

    public EmployeeServiceImpl(StaffRepository nhanVienRepository, FileStorageService fileStorageService) {
        this.nhanVienRepository = nhanVienRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public ResponseObject<?> getAllStaff(ADStaffSearchRequest request) {
        Pageable pageable = PageUtils.createPageable(request, "created_date");
        Page<Staff> page;
        if ((request.getQ() == null || request.getQ().isEmpty()) && request.getStatus() == null) {
            page = nhanVienRepository.findAll(pageable);
        } else {
            if (request.getStatus() != null) {
                request.setEntityStatus(request.getStatus() == 1 ? EntityStatus.ACTIVE : EntityStatus.INACTIVE);
            }
            page = nhanVienRepository.getAllStaff(pageable, request.getQ(), request.getEntityStatus());
        }
        return new ResponseObject<>(PageableObject.of(page), HttpStatus.OK, "Lay danh sach nhan vien thanh cong");
    }

    @Override
    public ResponseObject<?> getStaffById(String id) {
        return nhanVienRepository.findById(id)
                .map(nhanVien -> new ResponseObject<>(nhanVien, HttpStatus.OK, "Nhan vien thanh cong"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay Mau sac"));
    }

    @Override
    public ResponseObject<?> modifyStaff(UserUpsertRequest request) {
        if (request.getIdentityNumber() != null && checkDuplicateField("identityNumber", request.getIdentityNumber(), request.getId())) {
            throw new IllegalArgumentException("Ma dinh danh (CCCD) da ton tai!");
        }
        if (request.getPhoneNumber() != null && checkDuplicateField("phoneNumber", request.getPhoneNumber(), request.getId())) {
            throw new IllegalArgumentException("So dien thoai da ton tai!");
        }
        if (request.getEmail() != null && checkDuplicateField("email", request.getEmail(), request.getId())) {
            throw new IllegalArgumentException("Email da ton tai!");
        }

        if (StringUtils.hasLength(request.getId())) {
            Optional<Staff> existing = nhanVienRepository.findById(request.getId());
            if (existing.isPresent()) {
                Staff nhanVien = existing.get();
                applyRequest(nhanVien, request);
                saveAvatarIfPresent(nhanVien, request);
                nhanVienRepository.save(nhanVien);
                return new ResponseObject<>(nhanVien, HttpStatus.OK, "Cap nhat nhan vien thanh cong");
            }
        }

        Staff nhanVien = new Staff();
        applyRequest(nhanVien, request);
        nhanVien.setCode(generateCode(request.getName()));
        nhanVien.setRoleType(EntityVaiTro.STAFF);
        nhanVien.setPassword(DEFAULT_PASSWORD_HASH);
        nhanVien.setRole(EntityRole.STAFF);
        nhanVien.setStatus(EntityStatus.ACTIVE);
        nhanVienRepository.save(nhanVien);
        saveAvatarIfPresent(nhanVien, request);
        nhanVienRepository.save(nhanVien);

        return new ResponseObject<>(nhanVien, HttpStatus.CREATED, "Tao nhan vien thanh cong");
    }

    @Override
    public ResponseObject<?> changeStaffStatus(String id) {
        Optional<Staff> optional = nhanVienRepository.findById(id);
        if (optional.isEmpty()) {
            return new ResponseObject<>(HttpStatus.NOT_FOUND, HttpStatus.OK, "Khong tim nhan vien ");
        }

        Staff nhanVien = optional.get();
        nhanVien.setStatus(nhanVien.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        nhanVienRepository.save(nhanVien);
        return new ResponseObject<>(HttpStatus.OK, HttpStatus.OK, "Doi trang thai thanh cong");
    }

    @Override
    public ResponseObject<?> changeStaffRole(String id) {
        Optional<Staff> optional = nhanVienRepository.findById(id);
        if (optional.isEmpty()) {
            return new ResponseObject<>(HttpStatus.NOT_FOUND, HttpStatus.OK, "Khong tim nhan vien ");
        }

        Staff nhanVien = optional.get();
        nhanVien.setRole(nhanVien.getRole() == EntityRole.ADMIN ? EntityRole.STAFF : EntityRole.ADMIN);
        nhanVienRepository.save(nhanVien);
        return new ResponseObject<>(HttpStatus.OK, HttpStatus.OK, "Doi vai tro nhan vien thanh cong");
    }

    @Override
    public boolean checkDuplicateField(String field, String value, String excludeId) {
        String normalizedExcludeId = excludeId == null ? "" : excludeId;
        return switch (field.toLowerCase()) {
            case "identityNumber" -> nhanVienRepository.existsByIdentityNumberAndIdNot(value, normalizedExcludeId);
            case "phoneNumber" -> nhanVienRepository.existsByPhoneNumberAndIdNot(value, normalizedExcludeId);
            case "email" -> nhanVienRepository.existsByEmailAndIdNot(value, normalizedExcludeId);
            default -> throw new IllegalArgumentException("Truong khong hop le: " + field);
        };
    }

    private void applyRequest(Staff nhanVien, UserUpsertRequest request) {
        if (StringUtils.hasLength(request.getCode())) {
            nhanVien.setCode(request.getCode());
        }
        nhanVien.setName(request.getName());
        nhanVien.setEmail(request.getEmail());
        nhanVien.setPhoneNumber(request.getPhoneNumber());
        nhanVien.setAddress(request.getAddress());
        nhanVien.setGender(request.getGioiTinh());
        nhanVien.setWard(request.getWard());
        nhanVien.setDistrict(request.getDistrict());
        nhanVien.setProvince(request.getProvince());
        nhanVien.setDateOfBirth(request.getDateOfBirth());
        nhanVien.setIdentityNumber(request.getIdentityNumber());
    }

    private void saveAvatarIfPresent(Staff nhanVien, UserUpsertRequest request) {
        try {
            String avatarPath = fileStorageService.uploadAvatar(request.getAvatar(), nhanVien.getId());
            if (avatarPath != null) {
                nhanVien.setAvatar(avatarPath);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Loi khi doc file imageUrl: " + ex.getMessage(), ex);
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
