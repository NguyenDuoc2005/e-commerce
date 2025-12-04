package com.be.server.core.admin.loaide.service.impl;

import com.be.server.core.admin.loaide.model.request.ADLoaiDeRequest;
import com.be.server.core.admin.loaide.model.request.ADLoaiDeSearchRequest;
import com.be.server.core.admin.loaide.repository.ADLoaiDeRepository;
import com.be.server.core.admin.loaide.service.ADLoaiDeService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.LoaiDe;
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
public class ADLoaiDeServiceImpl implements ADLoaiDeService {

    private final ADLoaiDeRepository adLoaiDeRepository;

    @Override
    public ResponseObject<?> getAll(ADLoaiDeSearchRequest request) {
        Pageable pageable = Helper.createPageable(request, "created_date");
        Page<LoaiDe> page;
        if ((request.getQ() == null || request.getQ().isEmpty()) && (request.getStatus() == null)) {
            page = adLoaiDeRepository.findAll(pageable);
        } else {

            if (request.getStatus() != null) {
                if (request.getStatus() == 1) {
                    request.setEntityStatus(EntityStatus.ACTIVE);
                } else {
                    request.setEntityStatus(EntityStatus.INACTIVE);
                }
            }

            page = adLoaiDeRepository.getAllKhachHang(pageable,request.getQ(), request.getEntityStatus());

        }

        return new ResponseObject<>(
                PageableObject.of(page),
                HttpStatus.OK,
                "Lấy danh sách loại đế thành công"
        );
    }

    @Override
    public ResponseObject<?> getById(String id) {
        return adLoaiDeRepository.findById(id)
                .map(technology -> new ResponseObject<>(technology, HttpStatus.OK, "Lấy loại đế thành công"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy loại đế"));
    }

    @Override
    public ResponseObject<?> modify(ADLoaiDeRequest request) {


        if(adLoaiDeRepository.findByTen(
                request.getTen()).size() > 0) {
            return new ResponseObject<>(null, HttpStatus.OK, "Loại để này đã tồn tại");
        }

        if (request.getId() != null && StringUtils.hasLength(request.getId())) {
            Optional<LoaiDe> exsitingMemberOpt = adLoaiDeRepository.findById(request.getId());

            if (exsitingMemberOpt.isPresent()) {

                LoaiDe loaiGiay = exsitingMemberOpt.get();

                loaiGiay.setMa(request.getCode());

                loaiGiay.setTen(request.getTen());

                adLoaiDeRepository.save(loaiGiay);

                return new ResponseObject<>(loaiGiay, HttpStatus.OK, "Cập nhật loại đế thành công");
            }
        }


        LoaiDe loaiGiay = new LoaiDe();

        loaiGiay.setMa(request.getCode());

        loaiGiay.setTen(request.getTen());

        loaiGiay.setStatus(EntityStatus.ACTIVE);

        adLoaiDeRepository.save(loaiGiay);

        return new ResponseObject<>(loaiGiay, HttpStatus.CREATED, "Tạo loại đế thành công");
    }

    @Override
    public ResponseObject<?> changeStatus(String id) {
        Optional<LoaiDe> nemberOptional = adLoaiDeRepository.findById(id);

        nemberOptional.map(nember -> {
            nember.setStatus(nember.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
            return new ResponseObject(adLoaiDeRepository.save(nember), HttpStatus.OK, "Thay đổi trạng thái thành công");
        });

        return nemberOptional
                .map(product -> ResponseObject.successForward(HttpStatus.OK, "Đổi trạng thái thành công"))
                .orElseGet(() -> ResponseObject.successForward(HttpStatus.NOT_FOUND, "Không tìm loại giày"));
    }
}
