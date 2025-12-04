package com.be.server.infrastructure.listener;

import com.be.server.entity.DotGiamGia;
import com.be.server.entity.HoaDon;
import com.be.server.utils.Helper;
import jakarta.persistence.PrePersist;

public class CreateDotGiamGIaEntityListener {

    @PrePersist
    private void onCreate(DotGiamGia dotGiamGia) {
        dotGiamGia.setMa(Helper.generateCodeDotGiamGia());

    }

}
