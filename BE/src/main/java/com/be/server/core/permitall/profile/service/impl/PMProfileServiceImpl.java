package com.be.server.core.permitall.profile.service.impl;

import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.core.permitall.profile.model.PMKhachHangResponse;
import com.be.server.core.permitall.profile.repository.PMProfileRepository;
import com.be.server.core.permitall.profile.service.PMProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PMProfileServiceImpl implements PMProfileService {

    @Autowired
    private PMProfileRepository pmProfileRepository;

    @Override
    public ResponseObject<?> getLSKH(String id) {
        List<PMKhachHangResponse> page = pmProfileRepository.getLSKH(id);
        return new ResponseObject<>(
                page,
                HttpStatus.OK,
                "Lấy danh sách lịch sử hóa đơn thành công"
        );
    }
}
