package com.ecommerce.seller.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SellerRegistrationRequest {

    @NotBlank
    @Size(max = 255)
    private String shopName;

    @Size(max = 255)
    private String sellerSlug;

    @NotBlank
    private String description;

    private String logoUrl;

    private String coverImageUrl;

    @NotBlank
    private String pickupAddress;

    @NotBlank
    private String contactPhone;

    @NotBlank
    private String identityType;

    @NotBlank
    private String identityNumber;

    @NotBlank
    private String bankName;

    @NotBlank
    private String bankAccountNo;

    @NotBlank
    private String bankAccountHolder;

    private String mainCategoryId;

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
}
