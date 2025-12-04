package com.be.server.core.permitall.chitietsanpham.dto.response;

import com.be.server.entity.XuatSu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class XuatXuDTO {
    private String id;
    private String tenXuatXu;
    private String maXuatXu;

    public XuatXuDTO(XuatSu entity) {
        this.id = entity.getId();
        this.tenXuatXu = entity.getTen();
        this.maXuatXu = entity.getMa();
    }
}
