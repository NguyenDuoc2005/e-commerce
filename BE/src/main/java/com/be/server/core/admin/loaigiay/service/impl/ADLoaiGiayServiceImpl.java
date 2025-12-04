package com.be.server.core.admin.loaigiay.service.impl;

import com.be.server.core.admin.loaigiay.model.request.ADLoaiGiayRequest;
import com.be.server.core.admin.loaigiay.model.request.ADLoaiGiaySearchRequest;
import com.be.server.core.admin.loaigiay.repository.ADLoaiGiayRepository;
import com.be.server.core.admin.loaigiay.service.ADLoaiGiayService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.DanhMuc;
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
public class ADLoaiGiayServiceImpl implements ADLoaiGiayService {

    private final ADLoaiGiayRepository adLoaiGiayRepository;

    @Override
    public ResponseObject<?> getAll(ADLoaiGiaySearchRequest id) {
        Pageable pageable = Helper.createPageable(id, "created_date");

        Page<DanhMuc> page;

        page = adLoaiGiayRepository.findByMaContainingOrTenContaining(id.getQ(), pageable);


        return new ResponseObject<>(
                PageableObject.of(page),
                HttpStatus.OK,
                "Lấy danh sách danh mục thành công"
        );
    }

    @Override
    public ResponseObject<?> getById(String id) {
        return adLoaiGiayRepository.findById(id)
                .map(technology -> new ResponseObject<>(technology, HttpStatus.OK, "Lấy danh mục thành công"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));
    }

    @Override
    public ResponseObject<?> modify(ADLoaiGiayRequest request) {



        if(adLoaiGiayRepository.findByTen(request.getTen()).size() > 0) {
            return new ResponseObject<>(null, HttpStatus.OK, "Danh mục này đã tồn tại");
        }

        if (request.getId() != null && StringUtils.hasLength(request.getId())) {
            Optional<DanhMuc> exsitingMemberOpt = adLoaiGiayRepository.findById(request.getId());

            if (exsitingMemberOpt.isPresent()) {

                DanhMuc loaiGiay = exsitingMemberOpt.get();

                loaiGiay.setMa(request.getCode());

                loaiGiay.setTen(request.getTen());

                adLoaiGiayRepository.save(loaiGiay);

                return new ResponseObject<>(loaiGiay, HttpStatus.OK, "Cập nhật danh mục thành công");
            }
        }


        DanhMuc loaiGiay = new DanhMuc();

        loaiGiay.setMa(request.getCode());

        loaiGiay.setTen(request.getTen());

        loaiGiay.setStatus(EntityStatus.ACTIVE);

        adLoaiGiayRepository.save(loaiGiay);

        return new ResponseObject<>(loaiGiay, HttpStatus.CREATED, "Tạo danh mục thành công");
    }

    @Override
    public ResponseObject<?> changeStatus(String id) {
        Optional<DanhMuc> nemberOptional = adLoaiGiayRepository.findById(id);

        nemberOptional.map(nember -> {
            nember.setStatus(nember.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
            return new ResponseObject(adLoaiGiayRepository.save(nember), HttpStatus.OK, "Thay đổi trạng thái thành công");
        });

        return nemberOptional
                .map(product -> ResponseObject.successForward(HttpStatus.OK, "Đổi trạng thái thành công"))
                .orElseGet(() -> ResponseObject.successForward(HttpStatus.NOT_FOUND, "Không tìm danh mục"));
    }
}
