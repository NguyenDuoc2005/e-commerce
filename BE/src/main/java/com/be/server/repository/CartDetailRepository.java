package com.be.server.repository;

import com.be.server.entity.CartDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartDetailRepository extends JpaRepository<CartDetail,String> {

    @Query(value = """

                        select
            cd.*  from
            	gio_hang_chi_tiet cd
            join gio_hang c on
            	cd.id_gio_hang = c.id
            join san_pham_chi_tiet spct on
            	cd.id_san_pham_chi_tiet = spct.id
                 where c.id = :idCart and cd.so_luong > 0
            """,nativeQuery = true)
    List<CartDetail> getAllCart(@Param("idCart") String idCart);

    @Query(value = """

            select cd.id  from CartDetail cd
            join Cart c on cd.cart.id = c.id
            where cd.cart.id = :idCart and cd.sanPhamChiTiet.id = :idSanPhamChiTiet
            """)
    String getCart(@Param("idCart") String idCart, @Param("idSanPhamChiTiet") String idSanPhamChiTiet);


}
