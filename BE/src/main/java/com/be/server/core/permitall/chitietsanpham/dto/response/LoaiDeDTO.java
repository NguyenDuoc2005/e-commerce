package com.be.server.core.permitall.chitietsanpham.dto.response;

import com.be.server.entity.LoaiDe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoaiDeDTO {
    private String id;
    private String tenLoaiDe;
    private String maLoaiDe;

    public LoaiDeDTO(LoaiDe entity) {
        this.id = entity.getId();
        this.tenLoaiDe = entity.getTen();
        this.maLoaiDe = entity.getMa();
    }
}
