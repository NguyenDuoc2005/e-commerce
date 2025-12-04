package com.be.server.core.permitall.sanpham.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class PMSanPhamMoiResponse {
    private String id;
    private String tenSanPham;
    private String hinhAnhDaiDien;
    private String thuongHieu;
    private String danhMuc;
    private String chatLieu;
    private String xuatXu;
    private String moTa;
    private Double giaBan ;
    private Double giaSauGiam;
    private List<PMKichCoDTO> kichCo;
    private List<PMMauSacDTO> mauSac;
    private List<String> dsAnh;
    private DotGiamGiaInfo dotGiamGia;
    private Long ngayTao;

    public PMSanPhamMoiResponse(
            String id,
            String tenSanPham,
            String hinhAnhDaiDien,
            String thuongHieu,
            String danhMuc,
            String chatLieu,
            String xuatXu,
            String moTa,
            Double giaBan,
            Long ngayTao
    ) {
        this.id = id;
        this.tenSanPham = tenSanPham;
        this.hinhAnhDaiDien = hinhAnhDaiDien;
        this.thuongHieu = thuongHieu;
        this.danhMuc = danhMuc;
        this.chatLieu = chatLieu;
        this.xuatXu = xuatXu;
        this.moTa = moTa;
        this.giaBan = giaBan;
        this.ngayTao = ngayTao;
    }



}
