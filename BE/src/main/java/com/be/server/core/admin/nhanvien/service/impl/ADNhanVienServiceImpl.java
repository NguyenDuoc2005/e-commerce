package com.be.server.core.admin.nhanvien.service.impl;

import com.be.server.core.admin.nhanvien.model.request.ADNhanVienRequest;
import com.be.server.core.admin.nhanvien.model.request.ADNhanVienSearchRequest;
import com.be.server.core.admin.nhanvien.repository.ADNhanVienRepository;
import com.be.server.core.admin.nhanvien.service.ADNhanVienService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.NhanVien;
import com.be.server.infrastructure.constant.EntityRole;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.infrastructure.constant.EntityVaiTro;
import com.be.server.service.EmailService;
import com.be.server.utils.CloudinaryUtils;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
public class ADNhanVienServiceImpl implements ADNhanVienService {

    private final ADNhanVienRepository adNhanVienRepository;
    private final CloudinaryUtils cloudinaryUtils;

    @Override
    public ResponseObject<?> getAllNhanVien(ADNhanVienSearchRequest request) {
        Pageable pageable = Helper.createPageable(request, "created_date");
        System.out.println(request.getQ());
        Page<NhanVien> page;
        if (request.getQ() == null || request.getQ().isEmpty()) {
            page = adNhanVienRepository.findAll(pageable);
        } else {
            page = adNhanVienRepository.findByMaContainingOrTenContaining(request.getQ(), request.getQ(), pageable);
        }

        return new ResponseObject<>(
                PageableObject.of(page),
                HttpStatus.OK,
                "Lấy danh sách nhân viên thành công"
        );
    }

    @Override
    public ResponseObject<?> getNhanVienById(String id) {
        return adNhanVienRepository.findById(id)
                .map(technology -> new ResponseObject<>(technology, HttpStatus.OK, "Nhân viên thành công"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy Màu sắc"));
    }

    public static String generateShortName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return "";
        }

        String[] parts = fullName.trim().toLowerCase().split("\\s+");
        StringBuilder shortName = new StringBuilder();

        // Lấy phần tên chính (cuối cùng)
        String lastName = parts[parts.length - 1];
        shortName.append(lastName);

        // Lấy ký tự đầu của các phần còn lại
        for (int i = 0; i < parts.length - 1; i++) {
            shortName.append(parts[i].charAt(0));
        }

        return shortName.toString();
    }


    @Override
    public ResponseObject<?> modifyMNhanVien(ADNhanVienRequest request) {
        if (request.getCccd() != null && checkDuplicateField("cccd", request.getCccd(), request.getId())) {
            throw new RuntimeException("Mã định danh (CCCD) đã tồn tại!");
        }
        if (request.getSdt() != null && checkDuplicateField("sdt", request.getSdt(), request.getId())) {
            throw new RuntimeException("Số điện thoại đã tồn tại!");
        }
        if (request.getEmail() != null && checkDuplicateField("email", request.getEmail(), request.getId())) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        if (request.getId() != null && StringUtils.hasLength(request.getId())) {
            Optional<NhanVien> exsitingMemberOpt = adNhanVienRepository.findById(request.getId());

            if (exsitingMemberOpt.isPresent()) {

                NhanVien nhanVien = exsitingMemberOpt.get();

                nhanVien.setMa(request.getCode());

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

                nhanVien.setChucVu(EntityRole.STAFF);

                nhanVien.setStatus(EntityStatus.ACTIVE);    

                if (request.getAvatar() != null) {
                    try {
                        byte[] imageData = request.getAvatar().getBytes();
                        CompletableFuture.runAsync(() -> {
                            String imgPath = cloudinaryUtils.uploadImage(imageData, request.getId());
                            nhanVien.setAvatar(imgPath);
                            adNhanVienRepository.save(nhanVien);
                        });
                    } catch (IOException e) {
                        return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Lỗi khi đọc file ảnh: " + e.getMessage());
                    }
                }

                adNhanVienRepository.save(nhanVien);

                return new ResponseObject<>(nhanVien, HttpStatus.OK, "Cập nhật nhân viên thành công");
            }
        }


        NhanVien nhanVien = new NhanVien();

        Random random = new Random();
        int number = random.nextInt(10000);
        String shortName = generateShortName(request.getTen());
        String code = String.format("%s%03d", shortName, number);

        nhanVien.setMa(code);

        nhanVien.setTen(request.getTen());

        nhanVien.setEmail(request.getEmail());

        nhanVien.setSdt(request.getSdt());

        nhanVien.setDiaChi(request.getDiaChi());

        nhanVien.setGioiTimh(request.getGioiTinh());

        nhanVien.setNgaySinh(request.getNgaySinh());

        nhanVien.setXa(request.getXa());

        nhanVien.setHuyen(request.getHuyen());

        nhanVien.setTinh(request.getTinh());

        nhanVien.setVaitro(EntityVaiTro.NHAN_VIEN);

        nhanVien.setCccd(request.getCccd());

        nhanVien.setMatKhau("$2y$10$ey6ASnw6etj4YQtRFKZTjOlzjynNjDYgKXzf9/LDibTIXjEOdOgwa");

        nhanVien.setChucVu(EntityRole.STAFF);

        nhanVien.setStatus(EntityStatus.ACTIVE);

        if (request.getAvatar() != null){
            try {
                byte[] imageData = request.getAvatar().getBytes();
                CompletableFuture.runAsync(() -> {
                    String imgPath = cloudinaryUtils.uploadImage(imageData, request.getId());
                    nhanVien.setAvatar(imgPath);
                    adNhanVienRepository.save(nhanVien);
                });
            } catch (IOException e) {
                return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Lỗi khi đọc file ảnh: " + e.getMessage());
            }

        }


        adNhanVienRepository.save(nhanVien);

        String email = nhanVien.getEmail();
        String subject = "Thông tin xác thực tài khoản";
        String content = "Chào " + nhanVien.getTen() + ",\n\n" +
                "Đây là thông tin đăng nhập của bạn\n" +
                "Tài khoản đăng nhập của bạn là: "+ nhanVien.getEmail() +"\n" +
                "Mật khẩu đăng nhập của bạn là: 123\n" +
                "Trân trọng cảm ơn";

        CompletableFuture.runAsync(() -> EmailService.sendEmail(email, subject, content));


        return new ResponseObject<>(nhanVien, HttpStatus.CREATED, "Tạo nhân viên thành công");
    }

    @Override
    public ResponseObject<?> changeNhanVienStatus(String id) {
        Optional<NhanVien> nemberOptional = adNhanVienRepository.findById(id);

        nemberOptional.map(nember -> {
            nember.setStatus(nember.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
            return new ResponseObject(adNhanVienRepository.save(nember), HttpStatus.OK, "Thay đổi trạng thái thành công");
        });

        return nemberOptional
                .map(product -> ResponseObject.successForward(HttpStatus.OK, "Đổi trạng thái thành công"))
                .orElseGet(() -> ResponseObject.successForward(HttpStatus.NOT_FOUND, "Không tìm nhân viên "));
    }

    @Override
    public ResponseObject<?> changeNhanVienRole(String id) {
        Optional<NhanVien> nemberOptional = adNhanVienRepository.findById(id);

        nemberOptional.map(nember -> {
            nember.setChucVu(nember.getChucVu() == EntityRole.ADMIN ? EntityRole.STAFF : EntityRole.ADMIN);
            return new ResponseObject(adNhanVienRepository.save(nember), HttpStatus.OK, "Thay đổi vai trò nhân viên thành công");
        });

        return nemberOptional
                .map(product -> ResponseObject.successForward(HttpStatus.OK, "Đổi vai trò nhân viên thành công"))
                .orElseGet(() -> ResponseObject.successForward(HttpStatus.NOT_FOUND, "Không tìm nhân viên "));
    }

    @Override
    public boolean checkDuplicateField(String field, String value, String excludeId) {
        return switch (field.toLowerCase()) {
            case "cccd" -> adNhanVienRepository.existsByCccdAndIdNot(value, excludeId);
            case "sdt" -> adNhanVienRepository.existsBySdtAndIdNot(value, excludeId);
            case "email" -> adNhanVienRepository.existsByEmailAndIdNot(value, excludeId);
            default -> throw new IllegalArgumentException("Trường không hợp lệ: " + field);
        };
    }
}
