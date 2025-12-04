package com.be.server.core.admin.khachhang.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ADKhachHangRequest {

   private String id;

   private String ten;

   private String code;

   private String cccd;

   private String tinh;

   private String huyen;

   private String xa;

   private Date ngaySinh;

   private Boolean gioiTinh;

   private MultipartFile avatar;

   private String sdt;

   private String diaChi;

   private String email;


}
