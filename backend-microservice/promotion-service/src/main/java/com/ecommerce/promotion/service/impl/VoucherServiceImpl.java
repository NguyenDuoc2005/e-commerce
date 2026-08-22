package com.ecommerce.promotion.service.impl;

import com.ecommerce.common.base.PageableObject;
import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.util.PageUtils;
import com.ecommerce.promotion.constant.EntityStatus;
import com.ecommerce.promotion.entity.Voucher;
import com.ecommerce.promotion.entity.VoucherCustomer;
import com.ecommerce.promotion.model.request.VoucherRequest;
import com.ecommerce.promotion.model.request.VoucherSearchRequest;
import com.ecommerce.promotion.repository.VoucherCustomerRepository;
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
    private final VoucherCustomerRepository chiTietRepository;

    public VoucherServiceImpl(
            VoucherRepository voucherRepository,
            VoucherCustomerRepository chiTietRepository
    ) {
        this.voucherRepository = voucherRepository;
        this.chiTietRepository = chiTietRepository;
    }

    @Override
    public ResponseObject<?> getAllVoucher(VoucherSearchRequest request) {
        Pageable pageable = PageUtils.createPageable(request, "createdDate");
        request.setPlatformOnly(true);
        request.setSellerId(null);
        if (request.getDiscountMethod() != null) {
            request.setKieu(request.getDiscountMethod() == 0);
        }
        if (request.getStatus() != null) {
            request.setEntityStatus(request.getStatus() == 0 ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        }
        return new ResponseObject<>(
                PageableObject.of(voucherRepository.getAllVoucherFilter(pageable, request)),
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
        return voucherRepository.getDanhSachCustomer(id, search, PageRequest.of(page, size));
    }

    @Override
    public ResponseObject<?> modifyVoucher(VoucherRequest request) {
        request.setSellerId(null);
        return modifyScopedVoucher(null, request);
    }

    @Override
    public ResponseObject<?> getSellerVouchers(String sellerId, VoucherSearchRequest request) {
        Pageable pageable = PageUtils.createPageable(request, "createdDate");
        request.setSellerId(sellerId);
        request.setPlatformOnly(false);
        if (request.getDiscountMethod() != null) {
            request.setKieu(request.getDiscountMethod() == 0);
        }
        if (request.getStatus() != null) {
            request.setEntityStatus(request.getStatus() == 0 ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        }
        return new ResponseObject<>(
                PageableObject.of(voucherRepository.getAllVoucherFilter(pageable, request)),
                HttpStatus.OK,
                "Lay danh sach voucher shop thanh cong"
        );
    }

    @Override
    public ResponseObject<?> modifySellerVoucher(String sellerId, VoucherRequest request) {
        request.setSellerId(sellerId);
        return modifyScopedVoucher(sellerId, request);
    }

    private ResponseObject<?> modifyScopedVoucher(String sellerId, VoucherRequest request) {
        if (StringUtils.hasLength(request.getId())) {
            Optional<Voucher> existing = voucherRepository.findById(request.getId());
            if (existing.isPresent()) {
                Voucher voucher = existing.get();
                if (!sameScope(voucher.getSellerId(), sellerId)) {
                    return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "Khong co quyen cap nhat voucher nay");
                }
                applyRequest(voucher, request);
                voucherRepository.save(voucher);
                return new ResponseObject<>(voucher, HttpStatus.OK, "Cap nhat phieu giam gia thanh cong");
            }
        }

        if (voucherRepository.checkThemPhieu(request.getName()) != null) {
            return new ResponseObject<>(null, HttpStatus.OK, "phieu giam gia nay da ton tai");
        }

        Voucher voucher = new Voucher();
        applyRequest(voucher, request);
        LocalDate today = LocalDate.now();
        LocalDate start = request.getStartDate().toLocalDate();
        LocalDate end = request.getEndDate().toLocalDate();
        voucher.setStatus(!today.isBefore(start) && !today.isAfter(end) ? EntityStatus.ACTIVE : EntityStatus.INACTIVE);
        voucherRepository.save(voucher);

        if (request.getCustomerIds() != null) {
            for (String customerId : request.getCustomerIds()) {
                VoucherCustomer detail = new VoucherCustomer();
                detail.setVoucher(voucher);
                detail.setCustomerId(customerId);
                chiTietRepository.save(detail);
            }
        }

        return new ResponseObject<>(voucher, HttpStatus.CREATED, "Tao size thanh cong");
    }

    @Override
    public ResponseObject<?> changeVoucherStatus(String id) {
        return changeScopedVoucherStatus(null, id);
    }

    @Override
    public ResponseObject<?> changeSellerVoucherStatus(String sellerId, String id) {
        return changeScopedVoucherStatus(sellerId, id);
    }

    private ResponseObject<?> changeScopedVoucherStatus(String sellerId, String id) {
        Optional<Voucher> optional = voucherRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseObject.successForward(HttpStatus.NOT_FOUND, "Khong tim voucher");
        }
        Voucher voucher = optional.get();
        if (!sameScope(voucher.getSellerId(), sellerId)) {
            return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "Khong co quyen doi trang thai voucher nay");
        }
        voucher.setStatus(voucher.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        voucherRepository.save(voucher);
        return ResponseObject.successForward(HttpStatus.OK, "Doi trang thai thanh cong");
    }

    private void applyRequest(Voucher voucher, VoucherRequest request) {
        voucher.setName(request.getName());
        voucher.setConditionAmount(request.getConditionAmount());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setStartDate(request.getStartDate());
        voucher.setDiscountMethod(request.getDiscountMethod());
        voucher.setEndDate(request.getEndDate());
        voucher.setQuantity(request.getQuantity());
        if (Boolean.TRUE.equals(request.getDiscountMethod())) {
            voucher.setDiscountValue(request.getLoiPhanNay());
        } else {
            voucher.setDiscountValue(request.getMaxDiscountAmount());
        }
        voucher.setSellerId(request.getSellerId());
    }

    private boolean sameScope(String currentSellerId, String requestedSellerId) {
        if (!StringUtils.hasLength(currentSellerId) && !StringUtils.hasLength(requestedSellerId)) {
            return true;
        }
        return StringUtils.hasLength(currentSellerId) && currentSellerId.equals(requestedSellerId);
    }
}
