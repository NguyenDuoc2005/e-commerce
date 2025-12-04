package com.be.server.core.permitall.thanhtoan.repository;

import com.be.server.repository.CartDetailRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PMChiTietGioHangRepository extends CartDetailRepository {


    @Query("""
    SELECT cd.id FROM CartDetail cd
    where cd.sanPhamChiTiet.id = :idSP AND cd.cart.id = :idGH
""")
    String getIDGioHang(@Param("idSP") String idSP, @Param("idGH") String idGH);

}
