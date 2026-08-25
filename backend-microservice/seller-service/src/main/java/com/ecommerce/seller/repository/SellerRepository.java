package com.ecommerce.seller.repository;

import com.ecommerce.seller.entity.Seller;
import com.ecommerce.seller.entity.SellerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, String> {

    boolean existsByShopNameIgnoreCase(String shopName);

    boolean existsBySellerSlug(String sellerSlug);

    Optional<Seller> findFirstByOwnerCustomerIdOrderByCreatedAtDesc(String ownerCustomerId);

    Optional<Seller> findFirstByOwnerCustomerIdAndStatusOrderByCreatedAtDesc(String ownerCustomerId, SellerStatus status);

    List<Seller> findByOwnerCustomerIdIn(List<String> ownerCustomerIds);

    Optional<Seller> findBySellerSlugAndStatus(String sellerSlug, SellerStatus status);

    List<Seller> findByStatusOrderByCreatedAtDesc(SellerStatus status);

    List<Seller> findByIdInAndStatus(List<String> ids, SellerStatus status);

    List<Seller> findTop12ByStatusOrderByCreatedAtDesc(SellerStatus status);

    List<Seller> findAllByOrderByCreatedAtDesc();
}
