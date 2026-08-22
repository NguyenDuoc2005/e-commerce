package com.ecommerce.user.service.impl;

import com.ecommerce.common.base.PageableObject;
import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.util.PageUtils;
import com.ecommerce.user.client.OrderClient;
import com.ecommerce.user.constant.EntityStatus;
import com.ecommerce.user.entity.Customer;
import com.ecommerce.user.model.request.ADCustomerSearchRequest;
import com.ecommerce.user.model.request.UserUpsertRequest;
import com.ecommerce.user.repository.CustomerRepository;
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

    private final CustomerRepository khachHangRepository;

    private final FileStorageService fileStorageService;

    private final OrderClient orderClient;

    public CustomerServiceImpl(CustomerRepository khachHangRepository, FileStorageService fileStorageService, OrderClient orderClient) {
        this.khachHangRepository = khachHangRepository;
        this.fileStorageService = fileStorageService;
        this.orderClient = orderClient;
    }

    @Override
    public ResponseObject<?> getAllCustomer(ADCustomerSearchRequest request) {
        Pageable pageable = PageUtils.createPageable(request, "created_date");
        Page<Customer> page;
        if ((request.getQ() == null || request.getQ().isEmpty()) && request.getStatus() == null) {
            page = khachHangRepository.findAll(pageable);
        } else {
            if (request.getStatus() != null) {
                request.setEntityStatus(request.getStatus() == 1 ? EntityStatus.ACTIVE : EntityStatus.INACTIVE);
            }
            page = khachHangRepository.getAllCustomer(pageable, request.getQ(), request.getEntityStatus());
        }

        return new ResponseObject<>(PageableObject.of(page), HttpStatus.OK, "Lay danh sach khach hang thanh cong");
    }

    @Override
    public ResponseObject<?> getCustomerById(String id) {
        return khachHangRepository.findById(id)
                .map(khachHang -> new ResponseObject<>(khachHang, HttpStatus.OK, "Lay khach hang thanh cong"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay khach hang"));
    }

    @Override
    public ResponseObject<?> modifyCustomer(UserUpsertRequest request) {
        if (StringUtils.hasLength(request.getId())) {
            Optional<Customer> existing = khachHangRepository.findById(request.getId());
            if (existing.isPresent()) {
                Customer khachHang = existing.get();
                applyRequest(khachHang, request);
                saveAvatarIfPresent(khachHang, request);
                khachHangRepository.save(khachHang);
                return new ResponseObject<>(khachHang, HttpStatus.OK, "Cap nhat khach hang thanh cong");
            }
        }

        Customer khachHang = new Customer();
        applyRequest(khachHang, request);
        khachHang.setPassword(DEFAULT_PASSWORD_HASH);
        khachHang.setStatus(EntityStatus.ACTIVE);
        khachHangRepository.save(khachHang);
        saveAvatarIfPresent(khachHang, request);
        khachHangRepository.save(khachHang);

        return new ResponseObject<>(khachHang, HttpStatus.CREATED, "Tao khach hang thanh cong");
    }

    @Override
    public ResponseObject<?> updateCustomer(UserUpsertRequest request) {
        if (StringUtils.hasLength(request.getId())) {
            Optional<Customer> existing = khachHangRepository.findById(request.getId());
            if (existing.isPresent()) {
                Customer khachHang = existing.get();
                applyRequest(khachHang, request);
                saveAvatarIfPresent(khachHang, request);
                khachHangRepository.save(khachHang);
                return new ResponseObject<>(khachHang, HttpStatus.OK, "Cap nhat khach hang thanh cong");
            }
        }
        return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay khach hang");
    }

    @Override
    public ResponseObject<?> changeCustomerStatus(String id) {
        Optional<Customer> optional = khachHangRepository.findById(id);
        if (optional.isEmpty()) {
            return new ResponseObject<>(HttpStatus.NOT_FOUND, HttpStatus.OK, "Khong tim size");
        }

        Customer khachHang = optional.get();
        khachHang.setStatus(khachHang.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        khachHangRepository.save(khachHang);
        return new ResponseObject<>(HttpStatus.OK, HttpStatus.OK, "Doi trang thai thanh cong");
    }

    @Override
    public ResponseObject<?> getLSKH(String id) {
        return new ResponseObject<>(orderClient.getCustomerOrderHistory(id), HttpStatus.OK, "Lay danh sach lich su hoa don thanh cong");
    }

    private void applyRequest(Customer khachHang, UserUpsertRequest request) {
        khachHang.setCode(request.getCode());
        khachHang.setName(request.getName());
        khachHang.setEmail(request.getEmail());
        khachHang.setPhoneNumber(request.getPhoneNumber());
        khachHang.setAddress(request.getAddress());
        khachHang.setWard(request.getWard());
        khachHang.setDistrict(request.getDistrict());
        khachHang.setProvince(request.getProvince());
        khachHang.setIdentityNumber(request.getIdentityNumber());
        khachHang.setDateOfBirth(request.getDateOfBirth());
        khachHang.setGender(request.getGioiTinh());
    }

    private void saveAvatarIfPresent(Customer khachHang, UserUpsertRequest request) {
        try {
            String avatarPath = fileStorageService.uploadAvatar(request.getAvatar(), khachHang.getId());
            if (avatarPath != null) {
                khachHang.setAvatar(avatarPath);
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Loi khi doc file imageUrl: " + ex.getMessage(), ex);
        }
    }
}
