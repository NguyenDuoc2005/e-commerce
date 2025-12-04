package com.be.server.infrastructure.listener;

import com.be.server.entity.MauSac;
import com.be.server.utils.Helper;
import jakarta.persistence.PrePersist;

public class CreateMauSacEntityListener {

    @PrePersist
    private void onCreate(MauSac mauSac) {
        mauSac.setMa(Helper.generateCodeMauSac());
    }



}
