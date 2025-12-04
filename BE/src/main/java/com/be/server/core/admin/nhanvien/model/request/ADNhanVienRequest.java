package com.be.server.core.admin.nhanvien.model.request;

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
public class ADNhanVienRequest {

    private String id;

    private String user;

    private String ten;

    private String code;

    private String sdt;

    private String cccd;

    private String diaChi;

    private String tinh;

    private String huyen;

    private String xa;

    private String email;

    private MultipartFile avatar;

    private Date ngaySinh;

    private Boolean gioiTinh;

    private Date ngayTuyenDung;

    private String matKhau;

}
