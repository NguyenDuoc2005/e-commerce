package com.be.server.core.permitall.chitietsanpham.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChiTietSanPhamResponse {
    private String id;
    private MauSacDTO mauSac;
    private KichCoDTO kichCo;
    private Double giaBan;
    private Integer soLuong;
    private String hinhAnh;
    private DotGiamGiaDTO dotGiamGia;
}
