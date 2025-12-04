package com.be.server.core.admin.thuonghieu.service.impl;

import com.be.server.core.admin.thuonghieu.model.request.ADThuongHieuRequest;
import com.be.server.core.admin.thuonghieu.model.request.ADThuongHieuSearchRequest;
import com.be.server.core.admin.thuonghieu.repository.ADThuongHieuRepository;
import com.be.server.core.admin.thuonghieu.service.ADThuongHieuService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.ThuongHieu;
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
public class ADThuongHieuServiceImpl implements ADThuongHieuService {

    public final ADThuongHieuRepository adThuongHieuRepository;

    @Override
    public ResponseObject<?> getAllThuongHieu(ADThuongHieuSearchRequest request) {
        Pageable pageable = Helper.createPageable(request, "created_date");

        Page<ThuongHieu> page;

        page = adThuongHieuRepository.findByMaContainingOrTenContaining(request.getQ(), pageable);


        return new ResponseObject<>(
                PageableObject.of(page),
                HttpStatus.OK,
                "Lấy danh sách thương hiệu thành công"
        );
    }


    @Override
    public ResponseObject<?> getThuongHieuById(String id) {
        return adThuongHieuRepository.findById(id)
                .map(technology -> new ResponseObject<>(technology, HttpStatus.OK, "Lấy size thành công"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy size"));
    }

    @Override
    public ResponseObject<?> modifyThuongHieu(ADThuongHieuRequest request) {


        if (adThuongHieuRepository.findByTen(request.getTen()).size() > 0) {
            return new ResponseObject<>(null, HttpStatus.OK, "Thương hiệu này đã tồn tại");
        }

        if (request.getId() != null && StringUtils.hasLength(request.getId())) {
            Optional<ThuongHieu> exsitingMemberOpt = adThuongHieuRepository.findById(request.getId());

            if (exsitingMemberOpt.isPresent()) {

                ThuongHieu thuongHieu = exsitingMemberOpt.get();

                thuongHieu.setMa(request.getCode());

                thuongHieu.setTen(request.getTen());

                adThuongHieuRepository.save(thuongHieu);

                return new ResponseObject<>(thuongHieu, HttpStatus.OK, "Cập nhật thương hiệu thành công");
            }
        }


        ThuongHieu thuongHieu = new ThuongHieu();

        thuongHieu.setMa(request.getCode());

        thuongHieu.setTen(request.getTen());

        thuongHieu.setStatus(EntityStatus.ACTIVE);

        adThuongHieuRepository.save(thuongHieu);

        return new ResponseObject<>(thuongHieu, HttpStatus.CREATED, "Tạo thương hiệu thành công");
    }

    @Override
    public ResponseObject<?> changeSizeStatus(String id) {
        Optional<ThuongHieu> nemberOptional = adThuongHieuRepository.findById(id);

        nemberOptional.map(nember -> {
            nember.setStatus(nember.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
            return new ResponseObject(adThuongHieuRepository.save(nember), HttpStatus.OK, "Thay đổi trạng thái thành công");
        });

        return nemberOptional
                .map(product -> ResponseObject.successForward(HttpStatus.OK, "Đổi trạng thái thành công"))
                .orElseGet(() -> ResponseObject.successForward(HttpStatus.NOT_FOUND, "Không tìm size"));
    }

}
