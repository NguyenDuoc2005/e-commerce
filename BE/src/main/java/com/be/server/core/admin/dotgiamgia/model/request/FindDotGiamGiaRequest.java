package com.be.server.core.admin.dotgiamgia.model.request;

import com.be.server.core.common.base.PageableRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FindDotGiamGiaRequest extends PageableRequest {
    private String id;
    private String ma;
    private String ten;
    private Double phanTramGiam;
    private Long ngayBatDau;
    private Long ngayKetThuc;
    private String trangThai;
}