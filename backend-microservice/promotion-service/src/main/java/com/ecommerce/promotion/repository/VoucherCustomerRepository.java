package com.ecommerce.promotion.repository;

import com.ecommerce.promotion.entity.VoucherCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VoucherCustomerRepository extends JpaRepository<VoucherCustomer, String> {
    @Query("select distinct p.customerId from VoucherCustomer p where p.phieuGiamGia.id = :voucherId")
    List<String> findCustomerIdsByVoucherId(@Param("voucherId") String voucherId);
}
