package com.ecommerce.cart.repository;

import com.ecommerce.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByCustomerId(String customerId);

    @Query("select cd.id from CartDetail cd where cd.cart.id = :idCart and cd.productVariantId = :idSP")
    String checkChungSp(@Param("idCart") String idCart, @Param("idSP") String idSP);
}
