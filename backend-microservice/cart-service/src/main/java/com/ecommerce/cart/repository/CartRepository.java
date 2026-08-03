package com.ecommerce.cart.repository;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByKhachHang(KhachHang khachHang);

    @Query("select cd.id from CartDetail cd where cd.cart.id = :idCart and cd.sanPhamChiTiet.id = :idSP")
    String checkChungSp(@Param("idCart") String idCart, @Param("idSP") String idSP);
}
