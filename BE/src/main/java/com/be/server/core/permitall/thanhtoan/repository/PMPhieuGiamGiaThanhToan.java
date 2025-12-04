package com.be.server.core.permitall.thanhtoan.repository;

import com.be.server.entity.SanPhamChiTiet;
import com.be.server.repository.VoucherRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PMPhieuGiamGiaThanhToan extends VoucherRepository {

    @Query("""
    SELECT ct.id FROM PhieuGiamGia ct
    where ct.ma = :maPGG
""")
    String getPGG(@Param("maPGG") String maPGG);


    @Query("""
    SELECT gpp.id FROM PhieuGiamGiaChiTiet gpp
    where gpp.phieuGiamGia.id = :maPGG AND gpp.khachHang.id = :khachHangID
""")
    String checkKhachHang(@Param("maPGG") String maPGG, @Param("khachHangID") String khachHangID);

}
