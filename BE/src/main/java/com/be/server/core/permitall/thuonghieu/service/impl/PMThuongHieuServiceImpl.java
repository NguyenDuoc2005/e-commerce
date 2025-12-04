package com.be.server.core.permitall.thuonghieu.service.impl;

import com.be.server.core.common.base.ResponseObject;
import com.be.server.core.permitall.thuonghieu.dto.request.PMThuongHieuSearchRequest;
import com.be.server.core.permitall.thuonghieu.repository.PMThuongHieuRepository;
import com.be.server.core.permitall.thuonghieu.service.PMThuongHieuService;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class PMThuongHieuServiceImpl implements PMThuongHieuService {

    private final PMThuongHieuRepository pmThuongHieuRepository;

    @Override
    public ResponseObject<?> getAllThuongHieu(PMThuongHieuSearchRequest request) {
        Pageable pageable = Helper.createPageable(request, "created_date");
        return new ResponseObject<>(pmThuongHieuRepository.findAllThuongHieuInfo(pageable),
                HttpStatus.OK,
                "lay thuong hieu thanh cong");
    }

}
