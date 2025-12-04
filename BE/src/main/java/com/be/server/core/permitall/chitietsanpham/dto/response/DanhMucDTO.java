package com.be.server.core.permitall.chitietsanpham.dto.response;

import com.be.server.entity.DanhMuc;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DanhMucDTO {
    private String id;
    private String tenDanhMuc;
    private String maDanhMuc;

    public DanhMucDTO(DanhMuc entity) {
        this.id = entity.getId();
        this.tenDanhMuc = entity.getTen();
        this.maDanhMuc = entity.getMa();
    }
}
