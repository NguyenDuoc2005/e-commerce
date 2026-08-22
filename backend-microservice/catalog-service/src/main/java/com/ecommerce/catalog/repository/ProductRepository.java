package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.model.request.ProductSearchRequest;
import com.ecommerce.catalog.model.response.ListOptionResponse;
import com.ecommerce.catalog.model.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByStatusOrderByCreatedDateDesc(EntityStatus status);

    @Query(value = """
            SELECT
                ROW_NUMBER() OVER (ORDER BY sp.id DESC) AS stt,
                sp.id AS id,
                sp.code AS code,
                sp.name AS name,
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
                sp.description AS description,
                SUM(spct.quantity) AS tongSP,
                sp.sellerId AS sellerId,
                sp.status AS status
            FROM Product sp
                LEFT JOIN ProductVariant AS spct ON spct.product.id = sp.id
                LEFT JOIN Brand AS th ON th.id = sp.brand.id
                LEFT JOIN Origin AS xx ON xx.id = sp.origin.id
                LEFT JOIN SoleType AS ld ON ld.id = sp.soleType.id
                LEFT JOIN Category AS dm ON dm.id = sp.category.id
                LEFT JOIN Material AS cl ON cl.id = sp.material.id
            WHERE (:#{#rep.q} IS NULL OR sp.name LIKE CONCAT('%', :#{#rep.q}, '%') OR sp.code LIKE CONCAT('%', :#{#rep.q}, '%'))
                AND (:#{#rep.categoryId} IS NULL OR sp.category.id = :#{#rep.categoryId})
                AND (:#{#rep.materialId} IS NULL OR sp.material.id = :#{#rep.materialId})
                AND (:#{#rep.brandId} IS NULL OR sp.brand.id = :#{#rep.brandId})
                AND (:#{#rep.soleTypeId} IS NULL OR sp.soleType.id = :#{#rep.soleTypeId})
                AND (:#{#rep.sellerId} IS NULL OR sp.sellerId = :#{#rep.sellerId})
                AND (:#{#rep.status} IS NULL OR sp.status = :#{#rep.entityStatus})
            GROUP BY sp.id, sp.code, sp.name, sp.description, sp.sellerId, sp.status, th.id, xx.id, ld.id, dm.id, cl.id
            ORDER BY sp.createdDate DESC
            """, countQuery = """
            SELECT COUNT(sp.id)
            FROM Product sp
                LEFT JOIN Brand AS th ON th.id = sp.brand.id
                LEFT JOIN Origin AS xx ON xx.id = sp.origin.id
                LEFT JOIN SoleType AS ld ON ld.id = sp.soleType.id
                LEFT JOIN Category AS dm ON dm.id = sp.category.id
                LEFT JOIN Material AS cl ON cl.id = sp.material.id
            WHERE (:#{#rep.q} IS NULL OR sp.name LIKE CONCAT('%', :#{#rep.q}, '%') OR sp.code LIKE CONCAT('%', :#{#rep.q}, '%'))
                AND (:#{#rep.categoryId} IS NULL OR sp.category.id = :#{#rep.categoryId})
                AND (:#{#rep.materialId} IS NULL OR sp.material.id = :#{#rep.materialId})
                AND (:#{#rep.brandId} IS NULL OR sp.brand.id = :#{#rep.brandId})
                AND (:#{#rep.soleTypeId} IS NULL OR sp.soleType.id = :#{#rep.soleTypeId})
                AND (:#{#rep.sellerId} IS NULL OR sp.sellerId = :#{#rep.sellerId})
                AND (:#{#rep.status} IS NULL OR sp.status = :#{#rep.entityStatus})
            """)
    Page<ProductResponse> getAllProductByFilter(Pageable pageable, @Param("rep") ProductSearchRequest req);

    @Query("""
            SELECT
                ROW_NUMBER() OVER (ORDER BY sp.id DESC) AS stt,
                sp.id AS id,
                sp.code AS code,
                sp.name AS name,
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
                sp.description AS description,
                SUM(spct.quantity) AS tongSP,
                sp.sellerId AS sellerId,
                sp.status AS status
            FROM Product sp
                LEFT JOIN ProductVariant AS spct ON sp.id = spct.product.id
                LEFT JOIN Brand AS th ON th.id = sp.brand.id
                LEFT JOIN Origin AS xx ON xx.id = sp.origin.id
                LEFT JOIN SoleType AS ld ON ld.id = sp.soleType.id
                LEFT JOIN Category AS dm ON dm.id = sp.category.id
                LEFT JOIN Material AS cl ON cl.id = sp.material.id
            WHERE sp.id LIKE CONCAT('%', :id, '%')
            GROUP BY sp.id, sp.code, sp.name, sp.description, sp.sellerId, sp.status, th.id, xx.id, ld.id, dm.id, cl.id
            """)
    Optional<ProductResponse> getAllProductID(@Param("id") String id);

    @Query("select th.name as name, th.id as id from Brand th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListBrand();

    @Query("select th.name as name, th.id as id from Origin th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListXuatXu();

    @Query("select th.name as name, th.id as id from SoleType th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getSoleType();

    @Query("select th.name as name, th.id as id from Category th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListCategory();

    @Query("select th.name as name, th.id as id from Size th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListSize();

    @Query("select th.name as name, th.id as id from Color th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListMau();

    @Query("select th.name as name, th.id as id from Material th where th.status = 0 order by th.createdDate DESC")
    List<ListOptionResponse> getListMaterial();
}
