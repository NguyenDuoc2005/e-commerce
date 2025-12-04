package com.be.server.core.admin.sanpham.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ADSanPhamRequest {

    private String stt;

    private String id;

    private String ten;

    private String ma;

    private String moTa;

    private String  idThuongHieu;

    private String  idXuatXu;

    private String  idLoaiDe;

    private String  idDanhMuc;

    private String  idChatLieu;

    private String  idSP;

    private Integer tongTien;

}
