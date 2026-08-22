package com.ecommerce.seller.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seller")
public class Seller {

    @Id
    @Column(length = 36, updatable = false)
    private String id;

    @Column(name = "owner_customer_id", nullable = false, length = 36)
    private String ownerCustomerId;

    @Column(name = "shop_name", nullable = false, unique = true)
    private String shopName;

    @Column(name = "seller_slug", nullable = false, unique = true)
    private String sellerSlug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Column(name = "cover_image_url", length = 1000)
    private String coverImageUrl;

    @Column(name = "pickup_address", nullable = false, length = 1000)
    private String pickupAddress;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Column(name = "identity_type", nullable = false, length = 30)
    private String identityType;

    @Column(name = "identity_number", nullable = false, length = 100)
    private String identityNumber;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "bank_account_no", nullable = false, length = 100)
    private String bankAccountNo;

    @Column(name = "bank_account_holder", nullable = false)
    private String bankAccountHolder;

    @Column(name = "main_category_id", length = 36)
    private String mainCategoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SellerStatus status;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "approved_by_staff_id", length = 36)
    private String approvedByStaffId;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = SellerStatus.PENDING_APPROVAL;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerCustomerId() { return ownerCustomerId; }
    public void setOwnerCustomerId(String ownerCustomerId) { this.ownerCustomerId = ownerCustomerId; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getSellerSlug() { return sellerSlug; }
    public void setSellerSlug(String sellerSlug) { this.sellerSlug = sellerSlug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getIdentityType() { return identityType; }
    public void setIdentityType(String identityType) { this.identityType = identityType; }
    public String getIdentityNumber() { return identityNumber; }
    public void setIdentityNumber(String identityNumber) { this.identityNumber = identityNumber; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankAccountNo() { return bankAccountNo; }
    public void setBankAccountNo(String bankAccountNo) { this.bankAccountNo = bankAccountNo; }
    public String getBankAccountHolder() { return bankAccountHolder; }
    public void setBankAccountHolder(String bankAccountHolder) { this.bankAccountHolder = bankAccountHolder; }
    public String getMainCategoryId() { return mainCategoryId; }
    public void setMainCategoryId(String mainCategoryId) { this.mainCategoryId = mainCategoryId; }
    public SellerStatus getStatus() { return status; }
    public void setStatus(SellerStatus status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getApprovedByStaffId() { return approvedByStaffId; }
    public void setApprovedByStaffId(String approvedByStaffId) { this.approvedByStaffId = approvedByStaffId; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
