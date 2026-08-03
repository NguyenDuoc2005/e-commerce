package com.ecommerce.promotion.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.promotion.model.request.VoucherRequest;
import com.ecommerce.promotion.model.request.VoucherSearchRequest;
import org.springframework.data.domain.Page;

public interface VoucherService {
    ResponseObject<?> getAllVoucher(VoucherSearchRequest request);
    ResponseObject<?> getVoucherById(String id);
    Page<String> getListKH(String id, String search, int page, int size);
    ResponseObject<?> modifyVoucher(VoucherRequest request);
    ResponseObject<?> changeVoucherStatus(String id);
}
