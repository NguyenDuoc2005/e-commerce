package com.be.server.core.permitall.chitietsanpham.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DotGiamGiaDTO {
    private String tenDotGiamGia;
    private Double phanTramGiam;
    private Double giaTruoc;
    private Double giaSau;
    private Long ngayBatDau;
    private Long ngayKetThuc;

}
