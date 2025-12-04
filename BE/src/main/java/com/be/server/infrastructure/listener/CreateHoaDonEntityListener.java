package com.be.server.infrastructure.listener;

import com.be.server.entity.ChatLieu;
import com.be.server.entity.HoaDon;
import com.be.server.utils.Helper;
import jakarta.persistence.PrePersist;

public class CreateHoaDonEntityListener {

    @PrePersist
    private void onCreate(HoaDon HoaDon) {
        HoaDon.setMa(Helper.generateCodeHoaDon());
        HoaDon.setTen(Helper.generateCodeHoaDon());
    }

}
