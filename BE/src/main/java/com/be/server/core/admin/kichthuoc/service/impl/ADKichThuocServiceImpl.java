package com.be.server.core.admin.kichthuoc.service.impl;

import com.be.server.core.admin.kichthuoc.model.request.ADKichThuocRequest;
import com.be.server.core.admin.kichthuoc.model.request.ADKichThuocSearchRequest;
import com.be.server.core.admin.kichthuoc.repository.ADKichThuocRepository;
import com.be.server.core.admin.kichthuoc.service.ADKichThuocService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.KichCo;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.repository.SizeRepository;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ADKichThuocServiceImpl implements ADKichThuocService {

    public final ADKichThuocRepository adSizeRepository;
    private final SizeRepository sizeRepository;

    @Override
    public ResponseObject<?> getAllSize(ADKichThuocSearchRequest request) {
        Pageable pageable = Helper.createPageable(request, "created_date");
        Page<KichCo> page;

        page = adSizeRepository.findByMaContainingOrTenContaining(request.getQ(), pageable);


        return new ResponseObject<>(
                PageableObject.of(page),
                HttpStatus.OK,
                "Lấy danh sách size thành công"
        );
    }


    @Override
    public ResponseObject<?> getSizeById(String id) {
        return adSizeRepository.findById(id)
                .map(technology -> new ResponseObject<>(technology, HttpStatus.OK, "Lấy size thành công"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy size"));
    }

    @Override
    public ResponseObject<?> modifySize(ADKichThuocRequest request) {
        if (request.getId() != null && StringUtils.hasLength(request.getId())) {
            Optional<KichCo> exsitingMemberOpt = sizeRepository.findById(request.getId());

            if (exsitingMemberOpt.isPresent()) {

                KichCo size = exsitingMemberOpt.get();

                size.setMa(request.getCode());

                size.setTen(request.getTen());

                sizeRepository.save(size);

                return new ResponseObject<>(size, HttpStatus.OK, "Cập nhật kích cớ thành công");
            }
        }


        if (sizeRepository.findByTenContaining(request.getTen()).size() > 0) {
            return new ResponseObject<>(null, HttpStatus.OK, "Kích cớ này đã tồn tại");
        }


        KichCo size = new KichCo();

        size.setMa(request.getCode());

        size.setTen(request.getTen());

        size.setStatus(EntityStatus.ACTIVE);

        sizeRepository.save(size);

        return new ResponseObject<>(size, HttpStatus.CREATED, "Tạo size thành công");
    }

    @Override
    public ResponseObject<?> changeSizeStatus(String id) {
        Optional<KichCo> nemberOptional = sizeRepository.findById(id);

        nemberOptional.map(nember -> {
            nember.setStatus(nember.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
            return new ResponseObject(sizeRepository.save(nember), HttpStatus.OK, "Thay đổi trạng thái thành công");
        });

        return nemberOptional
                .map(product -> ResponseObject.successForward(HttpStatus.OK, "Đổi trạng thái thành công"))
                .orElseGet(() -> ResponseObject.successForward(HttpStatus.NOT_FOUND, "Không tìm size"));
    }

}
