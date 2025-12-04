package com.be.server.core.permitall.chitietsanpham.dto.response;

import com.be.server.entity.ThuongHieu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThuongHieuDTO {
    private String id;
    private String tenThuongHieu;
    private String maThuongHieu;

    public ThuongHieuDTO(ThuongHieu entity) {
        this.id = entity.getId(); // nếu ID là String
        this.tenThuongHieu = entity.getTen(); // hoặc getTenThuongHieu() nếu em đặt tên vậy
        this.maThuongHieu = entity.getMa();   // tương tự
    }
}

