package com.ecommerce.promotion.repository;

import com.ecommerce.promotion.entity.Voucher;
import com.ecommerce.promotion.constant.EntityStatus;
import com.ecommerce.promotion.model.request.VoucherSearchRequest;
import com.ecommerce.promotion.model.response.VoucherResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface VoucherRepository extends JpaRepository<Voucher, String> {
    Optional<Voucher> findByCode(String code);

    List<Voucher> findByStatusAndQuantityGreaterThan(EntityStatus status, Integer quantity);

    @Query(value = """
            SELECT
                pgg.id AS id,
                pgg.code AS code,
                pgg.name AS name,
                pgg.conditionAmount AS conditionAmount,
                pgg.maxDiscountAmount AS maxDiscountAmount,
                pgg.discountMethod AS discountMethod,
                pgg.discountType AS discountType,
                pgg.discountValue AS discountValue,
                pgg.quantity AS quantity,
                pgg.startDate AS startDate,
                pgg.endDate AS endDate,
                pgg.status AS status,
                pgg.sellerId AS sellerId
            FROM Voucher pgg
            WHERE (:#{#req.q == null || #req.q.isEmpty()} = TRUE OR LOWER(pgg.code) LIKE LOWER(CONCAT('%', :#{#req.q}, '%')) OR LOWER(pgg.name) LIKE LOWER(CONCAT('%', :#{#req.q}, '%')))
              AND (:#{#req.startDate == null} = TRUE OR pgg.startDate >= :#{#req.startDate})
              AND (:#{#req.endDate == null} = TRUE OR pgg.endDate <= :#{#req.endDate})
              AND (:#{#req.discountMethod == null} = TRUE OR pgg.discountMethod = :#{#req.kieu})
              AND (:#{#req.status == null} = TRUE OR pgg.status = :#{#req.entityStatus})
              AND (:#{#req.platformOnly != true} = TRUE OR pgg.sellerId IS NULL)
              AND (:#{#req.sellerId == null || #req.sellerId.isEmpty()} = TRUE OR pgg.sellerId = :#{#req.sellerId})
            ORDER BY pgg.createdDate DESC
            """, countQuery = """
            SELECT COUNT(pgg.id)
            FROM Voucher pgg
            WHERE (:#{#req.q == null || #req.q.isEmpty()} = TRUE OR LOWER(pgg.code) LIKE LOWER(CONCAT('%', :#{#req.q}, '%')) OR LOWER(pgg.name) LIKE LOWER(CONCAT('%', :#{#req.q}, '%')))
              AND (:#{#req.startDate == null} = TRUE OR pgg.startDate >= :#{#req.startDate})
              AND (:#{#req.endDate == null} = TRUE OR pgg.endDate <= :#{#req.endDate})
              AND (:#{#req.discountMethod == null} = TRUE OR pgg.discountMethod = :#{#req.kieu})
              AND (:#{#req.status == null} = TRUE OR pgg.status = :#{#req.entityStatus})
              AND (:#{#req.platformOnly != true} = TRUE OR pgg.sellerId IS NULL)
              AND (:#{#req.sellerId == null || #req.sellerId.isEmpty()} = TRUE OR pgg.sellerId = :#{#req.sellerId})
            """)
    Page<VoucherResponse> getAllVoucherFilter(Pageable pageable, @Param("req") VoucherSearchRequest req);

    @Query("""
            SELECT
                pgg.id AS id,
                pgg.code AS code,
                pgg.name AS name,
                pgg.conditionAmount AS conditionAmount,
                pgg.maxDiscountAmount AS maxDiscountAmount,
                pgg.discountMethod AS discountMethod,
                pgg.discountType AS discountType,
                pgg.discountValue AS discountValue,
                pgg.quantity AS quantity,
                pgg.startDate AS startDate,
                pgg.endDate AS endDate,
                pgg.status AS status,
                pgg.sellerId AS sellerId
            FROM Voucher pgg
            WHERE pgg.id LIKE CONCAT('%', :id, '%')
            """)
    Optional<VoucherResponse> getVoucherById(@Param("id") String id);

    @Query("""
            SELECT DISTINCT pggct.customerId
            FROM VoucherCustomer pggct
            WHERE pggct.phieuGiamGia.id = :id
              AND (:search IS NULL OR :search = '' OR pggct.customerId LIKE CONCAT('%', :search, '%'))
            """)
    Page<String> getDanhSachCustomer(@Param("id") String id, @Param("search") String search, Pageable pageable);

    @Query("select distinct p.id from Voucher p where p.name = :name")
    String checkThemPhieu(@Param("name") String name);
}
