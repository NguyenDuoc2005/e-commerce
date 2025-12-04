package com.be.server.core.permitall.chitietsanpham.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamDetailResponse {
    private String tenSanPham;
    private String moTa;
    private ThuongHieuDTO thuongHieu;
    private XuatXuDTO xuatXu;
    private ChatLieuDTO chatLieu;
    private DanhMucDTO danhMuc;
    private LoaiDeDTO loaiDe;
    private List<ChiTietSanPhamResponse> chiTietSanPham;
}
