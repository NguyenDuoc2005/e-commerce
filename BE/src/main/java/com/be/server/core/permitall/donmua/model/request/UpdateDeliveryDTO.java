package com.be.server.core.permitall.donmua.model.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Data
@Setter
public class UpdateDeliveryDTO {
    private String maHoaDon;
    private String tenKhachHang;
    private String sdtKhachHang;
    private String email;
    private String diaChi;
    private Double tongTienSauGiam;
    private Double phiVanChuyen;
}