package com.be.server.core.admin.hoadon.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ADHoaDonRequest {

   private String id;

   private String ten;

   private String code;

   private String sdt;

   private String diaChi;

   private Date ngaySinh;

   private Boolean gioiTinh;

}
