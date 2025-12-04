package com.be.server.core.admin.SanPhamChiTiet.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ADSPCTRequest {

    private String id;

    private Double giaBan;

    private String ten;

    private String ma;

    private String moTa;

    private MultipartFile anh;

    private Integer soLuong;

    private String  idThuongHieu;

    private String  idXuatXu;

    private String  idLoaiDe;

    private String  idDanhMuc;

    private String  idChatLieu;

    private String idMau;

    private String idSize;

    private String  idSP;

    private String  check;
}
