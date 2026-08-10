package com.ecommerce.cart.repository;

import com.ecommerce.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByKhachHangId(String khachHangId);

    @Query("select cd.id from CartDetail cd where cd.cart.id = :idCart and cd.sanPhamChiTietId = :idSP")
    String checkChungSp(@Param("idCart") String idCart, @Param("idSP") String idSP);
}
