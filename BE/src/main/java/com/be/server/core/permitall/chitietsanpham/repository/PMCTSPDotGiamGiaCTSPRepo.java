package com.be.server.core.permitall.chitietsanpham.repository;

import com.be.server.entity.DotGiamGiaChiTietSanPham;
import com.be.server.entity.SanPhamChiTiet;
import com.be.server.repository.DotGiamGiaChiTietSanPhamRepository;

public interface PMCTSPDotGiamGiaCTSPRepo extends DotGiamGiaChiTietSanPhamRepository {

    DotGiamGiaChiTietSanPham findFirstBySanPhamChiTiet_Id(String sanPhamChiTietId);
}
