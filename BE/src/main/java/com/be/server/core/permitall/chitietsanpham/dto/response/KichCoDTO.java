package com.be.server.core.permitall.chitietsanpham.dto.response;

import com.be.server.entity.KichCo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KichCoDTO {
    private String id;
    private String tenKichCo;
    private String maKichCo;

    public KichCoDTO(KichCo entity) {
        this.id = entity.getId();
        this.tenKichCo = entity.getTen();
        this.maKichCo = entity.getMa();
    }
}
