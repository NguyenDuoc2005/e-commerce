package com.be.server.core.admin.voucher.model.response;


import java.sql.Date;
import java.util.List;

public interface ADPhieuGiamGiaResponse {

     String getId();

     String getMa();

     String getTen();

     Double getPhanTramGiam();

     Integer getSoLuongPhieu();

     String getNgayBatDau();

     String getNgayKetThuc();

     Double getDieuKien();

     Double getGiaGiam();

     Boolean getLoaiGiam();

     Boolean getKieuGiam();

     String getIdKH();

     String getStatus();
}
