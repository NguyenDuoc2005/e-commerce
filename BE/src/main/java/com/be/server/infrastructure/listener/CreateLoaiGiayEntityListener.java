package com.be.server.infrastructure.listener;

import com.be.server.entity.DanhMuc;
import com.be.server.utils.Helper;
import jakarta.persistence.PrePersist;

public class CreateLoaiGiayEntityListener {

    @PrePersist
    private void onCreate(DanhMuc nhanVien) {
        nhanVien.setMa(Helper.generateCodeLoaiGiay());
    }

}
