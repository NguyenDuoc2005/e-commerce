package com.be.server.repository;

import com.be.server.entity.DotGiamGiaChiTietSanPham;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DotGiamGiaChiTietSanPhamRepository extends JpaRepository<DotGiamGiaChiTietSanPham, String> {

    List<DotGiamGiaChiTietSanPham> findAllByDotGiamGiaId(String dotGiamGiaId);

}
