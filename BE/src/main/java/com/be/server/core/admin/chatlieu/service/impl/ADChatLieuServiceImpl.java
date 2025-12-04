package com.be.server.core.admin.chatlieu.service.impl;

import com.be.server.core.admin.chatlieu.model.request.ADChatLieuRequest;
import com.be.server.core.admin.chatlieu.model.request.ADChatLieuSearchRequest;
import com.be.server.core.admin.chatlieu.repository.ADChatLieuRepository;
import com.be.server.core.admin.chatlieu.service.ADChatLieuService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.ChatLieu;
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
public class ADChatLieuServiceImpl implements ADChatLieuService {

    private final ADChatLieuRepository adChatLieuRepository;

    @Override
    public ResponseObject<?> getAll(ADChatLieuSearchRequest id) {
        Pageable pageable = Helper.createPageable(id, "created_date");

        Page<ChatLieu> page;

        page = adChatLieuRepository.findByMaContainingOrTenContaining(id.getQ(), pageable);


        return new ResponseObject<>(
                PageableObject.of(page),
                HttpStatus.OK,
                "Lấy danh sách chất liệu thành công"
        );
    }

    @Override
    public ResponseObject<?> getById(String id) {
        return adChatLieuRepository.findById(id)
                .map(technology -> new ResponseObject<>(technology, HttpStatus.OK, "Lấy chất liệu thành công"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy chất liệu"));
    }

    @Override
    public ResponseObject<?> modify(ADChatLieuRequest request) {
        if (request.getId() != null && StringUtils.hasLength(request.getId())) {
            Optional<ChatLieu> exsitingMemberOpt = adChatLieuRepository.findById(request.getId());

            if (exsitingMemberOpt.isPresent()) {

                ChatLieu loaiGiay = exsitingMemberOpt.get();

                loaiGiay.setMa(request.getCode());

                loaiGiay.setTen(request.getTen());

                adChatLieuRepository.save(loaiGiay);

                return new ResponseObject<>(loaiGiay, HttpStatus.OK, "Cập nhật chất liệu thành công");
            }
        }


        if(adChatLieuRepository.findByTen(request.getTen()).size() > 0) {
            return new ResponseObject<>(null, HttpStatus.OK, "Chất liệu này đã tồn tại");
        }


        ChatLieu loaiGiay = new ChatLieu();

        loaiGiay.setMa(request.getCode());

        loaiGiay.setTen(request.getTen());

        loaiGiay.setStatus(EntityStatus.ACTIVE);

        adChatLieuRepository.save(loaiGiay);

        return new ResponseObject<>(loaiGiay, HttpStatus.CREATED, "Tạo chất liệu thành công");
    }

    @Override
    public ResponseObject<?> changeStatus(String id) {
        Optional<ChatLieu> nemberOptional = adChatLieuRepository.findById(id);

        nemberOptional.map(nember -> {
            nember.setStatus(nember.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
            return new ResponseObject(adChatLieuRepository.save(nember), HttpStatus.OK, "Thay đổi trạng thái thành công");
        });

        return nemberOptional
                .map(product -> ResponseObject.successForward(HttpStatus.OK, "Đổi trạng thái thành công"))
                .orElseGet(() -> ResponseObject.successForward(HttpStatus.NOT_FOUND, "Không tìm loại giày"));
    }
}
