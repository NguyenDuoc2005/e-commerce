package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.ProductVariant;
import com.ecommerce.catalog.model.request.ProductDetailSearchRequest;
import com.ecommerce.catalog.model.response.ListOptionResponse;
import com.ecommerce.catalog.model.response.ProductDetailEditResponse;
import com.ecommerce.catalog.model.response.ProductDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    List<ProductVariant> findByProductIdAndStatusOrderByCreatedDateDesc(String productId, EntityStatus status);

    List<ProductVariant> findByProductIdAndSellerIdAndStatusOrderByCreatedDateDesc(String productId, String sellerId, EntityStatus status);

    List<ProductVariant> findByIdIn(List<String> ids);

    List<ProductVariant> findTop20BySellerIdAndQuantityLessThanEqualOrderByQuantityAsc(String sellerId, Integer quantity);

    @Query(value = """
            SELECT
                ROW_NUMBER() OVER (ORDER BY sp.id DESC) AS stt,
                spct.id AS id,
                sp.name AS name,
                spct.quantity AS quantity,
                th.name AS tenBrand,
                ld.name AS tenSoleType,
                cl.name AS tenMaterial,
                dm.name AS tenCategory,
                spct.salePrice AS salePrice,
                kc.name AS kichThuoc,
                ms.mau AS mau,
                ms.name AS tenMau,
                spct.imageUrl AS imageUrl,
                spct.sellerId AS sellerId,
                spct.status AS status,
                (SELECT MAX(spct2.salePrice) FROM ProductVariant spct2) AS giaMax
            FROM ProductVariant spct
                LEFT JOIN Product AS sp ON spct.product.id = sp.id
                LEFT JOIN Brand AS th ON th.id = sp.brand.id
                LEFT JOIN Size AS kc ON kc.id = spct.size.id
                LEFT JOIN SoleType AS ld ON ld.id = sp.soleType.id
                LEFT JOIN Category AS dm ON dm.id = sp.category.id
                LEFT JOIN Material AS cl ON cl.id = sp.material.id
                LEFT JOIN Color AS ms ON ms.id = spct.color.id
            WHERE (:#{#rep.idSP} IS NULL OR spct.product.id = :#{#rep.idSP})
                AND (:#{#rep.q} IS NULL OR sp.name LIKE CONCAT('%', :#{#rep.q}, '%') OR spct.code LIKE CONCAT('%', :#{#rep.q}, '%'))
                AND (:#{#rep.entityStatus} IS NULL OR spct.status = :#{#rep.entityStatus})
                AND (:#{#rep.priceMin} IS NULL OR spct.salePrice >= :#{#rep.priceMin})
                AND (:#{#rep.priceMax} IS NULL OR spct.salePrice <= :#{#rep.priceMax})
                AND (:#{#rep.idKT} IS NULL OR kc.id = :#{#rep.idKT})
                AND (:#{#rep.idMS} IS NULL OR ms.id = :#{#rep.idMS})
                AND (:#{#rep.sellerId} IS NULL OR spct.sellerId = :#{#rep.sellerId})
            ORDER BY spct.createdDate DESC
            """, countQuery = """
            SELECT COUNT(spct.id)
            FROM ProductVariant spct
                LEFT JOIN Product AS sp ON spct.product.id = sp.id
                LEFT JOIN Size AS kc ON kc.id = spct.size.id
                LEFT JOIN Color AS ms ON ms.id = spct.color.id
            WHERE (:#{#rep.idSP} IS NULL OR spct.product.id = :#{#rep.idSP})
                AND (:#{#rep.q} IS NULL OR sp.name LIKE CONCAT('%', :#{#rep.q}, '%') OR spct.code LIKE CONCAT('%', :#{#rep.q}, '%'))
                AND (:#{#rep.entityStatus} IS NULL OR spct.status = :#{#rep.entityStatus})
                AND (:#{#rep.priceMin} IS NULL OR spct.salePrice >= :#{#rep.priceMin})
                AND (:#{#rep.priceMax} IS NULL OR spct.salePrice <= :#{#rep.priceMax})
                AND (:#{#rep.idKT} IS NULL OR kc.id = :#{#rep.idKT})
                AND (:#{#rep.idMS} IS NULL OR ms.id = :#{#rep.idMS})
                AND (:#{#rep.sellerId} IS NULL OR spct.sellerId = :#{#rep.sellerId})
            """)
    Page<ProductDetailResponse> getAllProductVariantByFilter(Pageable pageable, @Param("rep") ProductDetailSearchRequest req);

    @Query("""
            SELECT
                ROW_NUMBER() OVER (ORDER BY spct.id DESC) AS stt,
                spct.id AS id,
                sp.name AS name,
                sp.description AS description,
                th.name AS tenBrand,
                th.id AS idBrand,
                xx.name AS tenXuatXu,
                xx.id AS idXuatXu,
                ld.name AS tenSoleType,
                ld.id AS idSoleType,
                dm.name AS tenCategory,
                dm.id AS idCategory,
                cl.name AS tenMaterial,
                cl.id AS idMaterial,
                ms.name AS tenColor,
                ms.id AS idColor,
                kc.name AS tenKichThuoc,
                kc.id AS idKichThuoc,
                spct.quantity AS quantity,
                spct.salePrice AS salePrice,
                spct.imageUrl AS imageUrl,
                spct.sellerId AS sellerId
            FROM ProductVariant spct
                LEFT JOIN Product AS sp ON sp.id = spct.product.id
                LEFT JOIN Brand AS th ON th.id = sp.brand.id
                LEFT JOIN Origin AS xx ON xx.id = sp.origin.id
                LEFT JOIN Color AS ms ON ms.id = spct.color.id
                LEFT JOIN Size AS kc ON kc.id = spct.size.id
                LEFT JOIN SoleType AS ld ON ld.id = sp.soleType.id
                LEFT JOIN Category AS dm ON dm.id = sp.category.id
                LEFT JOIN Material AS cl ON cl.id = sp.material.id
            WHERE spct.id LIKE CONCAT('%', :id, '%')
            """)
    java.util.Optional<ProductDetailEditResponse> getProductVariantID(@Param("id") String id);

    @Query("""
            SELECT
                ROW_NUMBER() OVER (ORDER BY sp.id DESC) AS stt,
                sp.id AS id,
                sp.name AS name,
                sp.description AS description,
                th.name AS tenBrand,
                th.id AS idBrand,
                xx.name AS tenXuatXu,
                xx.id AS idXuatXu,
                ld.name AS tenSoleType,
                ld.id AS idSoleType,
                dm.name AS tenCategory,
                dm.id AS idCategory,
                cl.name AS tenMaterial,
                cl.id AS idMaterial,
                sp.sellerId AS sellerId
            FROM Product sp
                LEFT JOIN Brand AS th ON th.id = sp.brand.id
                LEFT JOIN Origin AS xx ON xx.id = sp.origin.id
                LEFT JOIN SoleType AS ld ON ld.id = sp.soleType.id
                LEFT JOIN Category AS dm ON dm.id = sp.category.id
                LEFT JOIN Material AS cl ON cl.id = sp.material.id
            WHERE sp.id LIKE CONCAT('%', :id, '%')
            """)
    java.util.Optional<ProductDetailEditResponse> getProductID(@Param("id") String id);

    @Query("select th.name as name, th.id as id from Size th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListSize();

    @Query("select th.name as name, th.id as id from Color th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListColor();

    @Query("""
            select distinct spct.id
            from ProductVariant spct
            where ((:idMau is null and spct.color is null) or spct.color.id = :idMau)
              and ((:idSize is null and spct.size is null) or spct.size.id = :idSize)
              and spct.product.id = :idProduct
            """)
    String checkThemProduct(@Param("idMau") String idMau, @Param("idSize") String idSize, @Param("idProduct") String idProduct);

    @Query("select distinct sp.id from Product sp where sp.name = :tenSP")
    String checkThemSP(@Param("tenSP") String tenSP);

    @Query("""
            select spct.id
            from ProductVariant spct
            left join Product sp on spct.product.id = sp.id
            where sp.id = :id
            """)
    List<String> checkIdProductCT(@Param("id") String id);

    @Query("select sp.id as id, sp.name as name from Product sp where sp.status = 0")
    List<ListOptionResponse> getListThemSP();

    @Query("select sp.id as id, sp.name as name from Product sp where sp.status = 0 and sp.sellerId = :sellerId")
    List<ListOptionResponse> getListThemSPBySeller(@Param("sellerId") String sellerId);
}
