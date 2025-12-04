package com.be.server.core.admin.mausac.service.impl;

import com.be.server.core.admin.mausac.model.request.ADMauSacRequest;
import com.be.server.core.admin.mausac.model.request.ADMauSacSearchRequest;
import com.be.server.core.admin.mausac.repository.ADMauSacRepository;
import com.be.server.core.admin.mausac.service.ADMauSacService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.MauSac;
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
public class ADMauSacServiceImpl implements ADMauSacService {

    public final ADMauSacRepository mauSacRepository ;

    @Override
    public ResponseObject<?> getAllMauSac(ADMauSacSearchRequest request) {
        Pageable pageable = Helper.createPageable(request, "created_date");

        Page<MauSac> page;

        page = mauSacRepository.findByMaContainingOrTenContaining(request.getQ(), pageable);


        return new ResponseObject<>(
                PageableObject.of(page),
                HttpStatus.OK,
                "Lấy danh sách màu sắc thành công"
        );
    }


    @Override
    public ResponseObject<?> getMauSacById(String id) {
        System.out.println("id" + id);
        return mauSacRepository.findById(id)
                .map(technology -> new ResponseObject<>(technology, HttpStatus.OK, "Màu sắc thành công"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy Màu sắc"));
    }

    @Override
    public ResponseObject<?> modifyMauSac(ADMauSacRequest request) {



        if (request.getId() != null && StringUtils.hasLength(request.getId())) {
            Optional<MauSac> exsitingMemberOpt = mauSacRepository.findById(request.getId());

            if (exsitingMemberOpt.isPresent()) {

                MauSac mauSac = exsitingMemberOpt.get();

                System.out.println("code" + request.getCode());

                mauSac.setMa(request.getCode());

                mauSac.setTen(request.getTen());

                mauSac.setMau(request.getColor());

                mauSacRepository.save(mauSac);

                return new ResponseObject<>(mauSac, HttpStatus.OK, "Cập nhật màu sắc thành công");
            }
        }

        if(mauSacRepository.findByTen(request.getTen()).size() > 0) {
            return new ResponseObject<>(null, HttpStatus.OK, "Màu sắc này đã tồn tại");
        }

        if(mauSacRepository.findByMau(request.getColor()).size() > 0) {
            return new ResponseObject<>(null, HttpStatus.OK, "Màu sắc này đã tồn tại");
        }

        MauSac mauSac = new MauSac();

        mauSac.setMa(request.getCode());

        mauSac.setTen(request.getTen());

        mauSac.setMau(request.getColor());

        mauSac.setStatus(EntityStatus.ACTIVE);

        mauSacRepository.save(mauSac);

        return new ResponseObject<>(mauSac, HttpStatus.CREATED, "Tạo màu sắc thành công");
    }

    @Override
    public ResponseObject<?> changeMauSacStatus(String id) {
        Optional<MauSac> nemberOptional = mauSacRepository.findById(id);

        nemberOptional.map(nember -> {
            nember.setStatus(nember.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
            return new ResponseObject(mauSacRepository.save(nember), HttpStatus.OK, "Thay đổi trạng thái thành công");
        });

        return nemberOptional
                .map(product -> ResponseObject.successForward(HttpStatus.OK, "Đổi trạng thái thành công"))
                .orElseGet(() -> ResponseObject.successForward(HttpStatus.NOT_FOUND, "Không tìm màu sắc"));
    }

}
