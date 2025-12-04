package com.be.server.core.admin.khachhang.service.impl;

import com.be.server.core.admin.khachhang.model.request.ADKhachHangRequest;
import com.be.server.core.admin.khachhang.model.request.ADKhachHangSearchRequest;
import com.be.server.core.admin.khachhang.repository.ADKhachHangRepository;
import com.be.server.core.admin.khachhang.service.ADKhachHangService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.KhachHang;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.repository.SizeRepository;
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
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
public class ADKhachHangServiceImpl implements ADKhachHangService {

    public final ADKhachHangRepository adKhachHangRepository;
    private final CloudinaryUtils cloudinaryUtils;

    @Override
    public ResponseObject<?> getAllKhachHang(ADKhachHangSearchRequest request) {
        Pageable pageable = Helper.createPageable(request, "created_date");
        Page<KhachHang> page;
        if ((request.getQ() == null || request.getQ().isEmpty()) && (request.getStatus() == null)) {
            page = adKhachHangRepository.findAll(pageable);
        } else {

            if (request.getStatus() != null) {
                if (request.getStatus() == 1) {
                    request.setEntityStatus(EntityStatus.ACTIVE);
                } else {
                    request.setEntityStatus(EntityStatus.INACTIVE);
                }
            }

            page = adKhachHangRepository.getAllKhachHang(pageable, request.getQ(), request.getEntityStatus());
        }

        return new ResponseObject<>(
                PageableObject.of(page),
                HttpStatus.OK,
                "Lấy danh sách khách hàng thành công"
        );
    }


    @Override
    public ResponseObject<?> getKhachHangById(String id) {
        return adKhachHangRepository.findById(id)
                .map(technology -> new ResponseObject<>(technology, HttpStatus.OK, "Lấy khách hàng thành công"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));
    }

    @Override
    public ResponseObject<?> modifyKhachHang(ADKhachHangRequest request) {
        if (request.getId() != null && StringUtils.hasLength(request.getId())) {
            KhachHang exsitingMemberOpt = adKhachHangRepository.findById(request.getId()).get();

            if (exsitingMemberOpt != null) {

                KhachHang khachHang = exsitingMemberOpt;

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

                if (request.getAvatar() != null) {
                    try {
                        byte[] imageData = request.getAvatar().getBytes();
                        CompletableFuture.runAsync(() -> {
                            String imgPath = cloudinaryUtils.uploadImage(imageData, request.getId());
                            khachHang.setAvatar(imgPath);
                            adKhachHangRepository.save(khachHang);
                        });
                    } catch (IOException e) {
                        return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Lỗi khi đọc file ảnh: " + e.getMessage());
                    }
                }

                adKhachHangRepository.save(khachHang);

                return new ResponseObject<>(khachHang, HttpStatus.OK, "Cập nhật khách hàng thành công");
            }
        }

        KhachHang khachHang = new KhachHang();

        khachHang.setMa(request.getCode());

        khachHang.setTen(request.getTen());

        khachHang.setSdt(request.getSdt());

        khachHang.setEmail(request.getEmail());

        khachHang.setDiaChi(request.getDiaChi());

        khachHang.setCccd(request.getCccd());

        khachHang.setXa(request.getXa());

        khachHang.setHuyen(request.getHuyen());

        khachHang.setTinh(request.getTinh());

        khachHang.setNgaySinh(request.getNgaySinh());

        khachHang.setMat_khau("$2y$10$ey6ASnw6etj4YQtRFKZTjOlzjynNjDYgKXzf9/LDibTIXjEOdOgwa");

        khachHang.setGioiTimh(request.getGioiTinh());

        if (request.getAvatar() != null) {
            try {
                byte[] imageData = request.getAvatar().getBytes();
                CompletableFuture.runAsync(() -> {
                    String imgPath = cloudinaryUtils.uploadImage(imageData, request.getId());
                    khachHang.setAvatar(imgPath);
                    adKhachHangRepository.save(khachHang);
                });
            } catch (IOException e) {
                return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Lỗi khi đọc file ảnh: " + e.getMessage());
            }
        }


        khachHang.setStatus(EntityStatus.ACTIVE);

        adKhachHangRepository.save(khachHang);

        return new ResponseObject<>(khachHang, HttpStatus.CREATED, "Tạo khách hàng thành công");
    }

    @Override
    public ResponseObject<?> updateKhachHang(ADKhachHangRequest request) {
        if (request.getId() != null && StringUtils.hasLength(request.getId())) {
            KhachHang exsitingMemberOpt = adKhachHangRepository.findById(request.getId()).get();

            if (exsitingMemberOpt != null) {

                KhachHang khachHang = exsitingMemberOpt;

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

                if (request.getAvatar() != null) {
                    try {
                        byte[] imageData = request.getAvatar().getBytes();
                        CompletableFuture.runAsync(() -> {
                            String imgPath = cloudinaryUtils.uploadImage(imageData, request.getId());
                            khachHang.setAvatar(imgPath);
                            adKhachHangRepository.save(khachHang);
                        });
                    } catch (IOException e) {
                        return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Lỗi khi đọc file ảnh: " + e.getMessage());
                    }
                }

                adKhachHangRepository.save(khachHang);

                return new ResponseObject<>(khachHang, HttpStatus.OK, "Cập nhật khách hàng thành công");
            }
        }
        return null;
    }

    @Override
    public ResponseObject<?> changeKhachHangStatus(String id) {
        Optional<KhachHang> nemberOptional = adKhachHangRepository.findById(id);

        nemberOptional.map(nember -> {
            nember.setStatus(nember.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
            return new ResponseObject(adKhachHangRepository.save(nember), HttpStatus.OK, "Thay đổi trạng thái thành công");
        });

        return nemberOptional
                .map(product -> ResponseObject.successForward(HttpStatus.OK, "Đổi trạng thái thành công"))
                .orElseGet(() -> ResponseObject.successForward(HttpStatus.NOT_FOUND, "Không tìm size"));
    }

}
