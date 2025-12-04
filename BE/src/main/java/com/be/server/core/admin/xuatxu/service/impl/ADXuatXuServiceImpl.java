package com.be.server.core.admin.xuatxu.service.impl;

import com.be.server.core.admin.xuatxu.model.request.ADXuatXuRequest;
import com.be.server.core.admin.xuatxu.model.request.ADXuatXuSearchRequest;
import com.be.server.core.admin.xuatxu.repository.ADXuatXuRepository;
import com.be.server.core.admin.xuatxu.service.ADXuatXuService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.XuatSu;
import com.be.server.infrastructure.constant.EntityStatus;
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
public class ADXuatXuServiceImpl implements ADXuatXuService {

    public final ADXuatXuRepository adXuatXuRepository ;

    @Override
    public ResponseObject<?> getAllXuatXu(ADXuatXuSearchRequest request) {
        Pageable pageable = Helper.createPageable(request, "created_date");
        Page<XuatSu> page;
        if (request.getQ() == null || request.getQ().isEmpty()) {
            page = adXuatXuRepository.findAll(pageable);
        } else {
            page = adXuatXuRepository.findByMaContainingOrTenContaining(request.getQ(), request.getQ(), pageable);
        }

        return new ResponseObject<>(
                PageableObject.of(page),
                HttpStatus.OK,
                "Lấy danh sách thương hiệu thành công"
        );
    }


    @Override
    public ResponseObject<?> getXuatXuById(String id) {
        return adXuatXuRepository.findById(id)
                .map(technology -> new ResponseObject<>(technology, HttpStatus.OK, "Lấy size thành công"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy size"));
    }

    @Override
    public ResponseObject<?> modifyXuatXu(ADXuatXuRequest request) {
        if (request.getId() != null && StringUtils.hasLength(request.getId())) {
            Optional<XuatSu> exsitingMemberOpt = adXuatXuRepository.findById(request.getId());

            if (exsitingMemberOpt.isPresent()) {

                XuatSu xuatSu = exsitingMemberOpt.get();

                xuatSu.setMa(request.getCode());

                xuatSu.setTen(request.getTen());

                adXuatXuRepository.save(xuatSu);

                return new ResponseObject<>(xuatSu, HttpStatus.OK, "Cập nhật xuất xứ thành công");
            }
        }


        XuatSu xuatSu = new XuatSu();

        xuatSu.setMa(request.getCode());

        xuatSu.setTen(request.getTen());

        xuatSu.setStatus(EntityStatus.ACTIVE);

        adXuatXuRepository.save(xuatSu);

        return new ResponseObject<>(xuatSu, HttpStatus.CREATED, "Tạo xuất xứ thành công");
    }

    @Override
    public ResponseObject<?> changeXuatXuStatus(String id) {
        Optional<XuatSu> nemberOptional = adXuatXuRepository.findById(id);

        nemberOptional.map(nember -> {
            nember.setStatus(nember.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
            return new ResponseObject(adXuatXuRepository.save(nember), HttpStatus.OK, "Thay đổi trạng thái thành công");
        });

        return nemberOptional
                .map(product -> ResponseObject.successForward(HttpStatus.OK, "Đổi trạng thái thành công"))
                .orElseGet(() -> ResponseObject.successForward(HttpStatus.NOT_FOUND, "Không tìm size"));
    }

}
