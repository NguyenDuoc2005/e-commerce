package com.be.server.infrastructure.listener;

import com.be.server.entity.ChatLieu;
import com.be.server.utils.Helper;
import jakarta.persistence.PrePersist;

public class CreateChatLieuEntityListener {

    @PrePersist
    private void onCreate(ChatLieu nhanVien) {
        nhanVien.setMa(Helper.generateCodeLoaiGiay());
    }

}
