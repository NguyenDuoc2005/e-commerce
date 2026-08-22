package com.ecommerce.cart.repository;

import com.ecommerce.cart.entity.CartDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartDetailRepository extends JpaRepository<CartDetail, String> {
    @Query("select cd from CartDetail cd where cd.cart.id = :idCart and cd.quantity > 0")
    List<CartDetail> getAllCart(@Param("idCart") String idCart);

    @Query("select cd.id from CartDetail cd join Cart c on cd.cart.id = c.id where cd.cart.id = :idCart and cd.productVariantId = :idProductVariant")
    String getCart(@Param("idCart") String idCart, @Param("idProductVariant") String idProductVariant);

    @Modifying
    @Query("delete from CartDetail cd where cd.cart.id = :cartId and cd.productVariantId = :productDetailId")
    void deleteByCartIdAndProductDetailId(@Param("cartId") String cartId, @Param("productDetailId") String productDetailId);
}
