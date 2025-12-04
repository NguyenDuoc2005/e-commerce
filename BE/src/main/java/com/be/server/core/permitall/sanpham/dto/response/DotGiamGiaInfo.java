package com.be.server.core.permitall.sanpham.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DotGiamGiaInfo {
    private String tenDotGiamGia;
    private Double phanTramGiam;
    private Double giaTruoc;
    private Double giaSau;
    private Long ngayBatDau;
    private Long ngayKetThuc;
}
