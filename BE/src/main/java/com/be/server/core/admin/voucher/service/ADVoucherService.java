package com.be.server.core.admin.voucher.service;

import com.be.server.core.admin.thuonghieu.model.request.ADThuongHieuRequest;
import com.be.server.core.admin.thuonghieu.model.request.ADThuongHieuSearchRequest;
import com.be.server.core.admin.voucher.model.request.ADVoucherRequest;
import com.be.server.core.admin.voucher.model.request.ADVoucherSearchRequest;
import com.be.server.core.common.base.ResponseObject;
import org.springframework.data.domain.Page;


import java.util.List;

public interface ADVoucherService {

    ResponseObject<?> getAllVoucher(ADVoucherSearchRequest id);

    ResponseObject<?> getVoucherById(String id);

    Page<String> getListKH(String id, String search, int page, int size);

    ResponseObject<?> modifyVoucher(ADVoucherRequest request);

    ResponseObject<?> changeSizeStatus(String id);

}
