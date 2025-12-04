package com.be.server.repository;

import com.be.server.core.admin.banhang.model.request.ADThemSanPhamRequest;
import com.be.server.entity.Cart;
import com.be.server.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,String> {

    Optional<Cart> findByKhachHang(KhachHang khachHang);

    @Query(value = """
    select hdct.id from Cart hdct
    where hdct.khachHang.id = :#{#idKH}
""")
    String findByIdKH(@Param("idKH") String idKH);

    @Query(value = """
    select hdct.id from CartDetail hdct
    where hdct.cart.id = :#{#idHD} and hdct.sanPhamChiTiet.id = :#{#idSP}
""")
    String checkChungSp(@Param("idHD") String req, @Param("idSP") String idSP);
}
