package com.be.server.core.permitall.chitietsanpham.dto.response;

import com.be.server.entity.MauSac;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MauSacDTO {
    private String id;
    private String tenMauSac;
    private String maMau;

    public MauSacDTO(MauSac entity) {
        this.id = entity.getId();
        this.tenMauSac = entity.getTen();
        this.maMau = entity.getMau();
    }
}
