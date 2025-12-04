package com.be.server.repository;

import com.be.server.entity.ChatLieu;
import com.be.server.entity.DotGiamGia;
import com.be.server.infrastructure.constant.StatusPromotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia, String> {

    List<DotGiamGia> findAllByTrangThaiAndNgayBatDauLessThanEqual(StatusPromotion trangThai, Long thoiGianHienTai);
    List<DotGiamGia> findAllByTrangThaiAndNgayKetThucLessThan(StatusPromotion trangThai, Long thoiGianHienTai);
    List<DotGiamGia> findAllByTrangThai(StatusPromotion trangThai);

}
