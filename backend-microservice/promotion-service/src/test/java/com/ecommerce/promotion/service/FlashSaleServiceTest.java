package com.ecommerce.promotion.service;

import com.ecommerce.common.catalog.CatalogVariantSnapshot;
import com.ecommerce.promotion.client.CatalogClient;
import com.ecommerce.promotion.constant.CampaignType;
import com.ecommerce.promotion.constant.RegistrationStatus;
import com.ecommerce.promotion.constant.Status;
import com.ecommerce.promotion.constant.StatusPromotion;
import com.ecommerce.promotion.entity.PromotionCampaign;
import com.ecommerce.promotion.entity.PromotionCampaignProduct;
import com.ecommerce.promotion.model.request.FlashSaleCampaignRequest;
import com.ecommerce.promotion.model.request.FlashSaleRegistrationRequest;
import com.ecommerce.promotion.model.request.FlashSaleReviewRequest;
import com.ecommerce.promotion.repository.PromotionDetailRepository;
import com.ecommerce.promotion.repository.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlashSaleServiceTest {
    @Mock private PromotionRepository campaignRepository;
    @Mock private PromotionDetailRepository registrationRepository;
    @Mock private CatalogClient catalogClient;

    @Test
    void adminCreatesPlatformFlashSaleWithRegistrationWindow() {
        FlashSaleService service = service();
        long now = System.currentTimeMillis();
        FlashSaleCampaignRequest request = campaignRequest(now);
        service.createCampaign("staff-1", request);

        ArgumentCaptor<PromotionCampaign> saved = ArgumentCaptor.forClass(PromotionCampaign.class);
        verify(campaignRepository).save(saved.capture());
        assertEquals(CampaignType.FLASH_SALE, saved.getValue().getCampaignType());
        assertEquals("staff-1", saved.getValue().getCreatedByStaffId());
        assertEquals(StatusPromotion.CHUA_KICH_HOAT, saved.getValue().getTrangThai());
    }

    @Test
    void sellerRegistrationUsesCatalogOwnershipAndWaitsForApproval() {
        FlashSaleService service = service();
        PromotionCampaign campaign = openCampaign();
        when(campaignRepository.findById("campaign-1")).thenReturn(Optional.of(campaign));
        when(catalogClient.getProductVariant("variant-1")).thenReturn(variant("seller-1"));
        when(registrationRepository.findByPromotionCampaign_IdAndProductVariantId("campaign-1", "variant-1"))
                .thenReturn(null);

        FlashSaleRegistrationRequest request = new FlashSaleRegistrationRequest();
        request.setProductVariantId("variant-1");
        request.setFlashPrice(80000D);
        service.register("campaign-1", "seller-1", request);

        ArgumentCaptor<PromotionCampaignProduct> saved = ArgumentCaptor.forClass(PromotionCampaignProduct.class);
        verify(registrationRepository).save(saved.capture());
        assertEquals(RegistrationStatus.PENDING, saved.getValue().getRegistrationStatus());
        assertEquals(Status.CHUA_KICH_HOAT, saved.getValue().getTrangThai());
        assertEquals(100000D, saved.getValue().getPriceBeforeDiscount());
        assertEquals(80000D, saved.getValue().getPriceAfterDiscount());
    }

    @Test
    void sellerCannotRegisterAnotherShopsVariant() {
        FlashSaleService service = service();
        when(campaignRepository.findById("campaign-1")).thenReturn(Optional.of(openCampaign()));
        when(catalogClient.getProductVariant("variant-1")).thenReturn(variant("seller-2"));
        FlashSaleRegistrationRequest request = new FlashSaleRegistrationRequest();
        request.setProductVariantId("variant-1");
        request.setFlashPrice(80000D);

        assertThrows(SecurityException.class, () -> service.register("campaign-1", "seller-1", request));
    }

    @Test
    void adminApprovalActivatesOnlyTheReviewedRegistration() {
        FlashSaleService service = service();
        PromotionCampaign campaign = openCampaign();
        PromotionCampaignProduct registration = new PromotionCampaignProduct();
        registration.setId("registration-1");
        registration.setPromotionCampaign(campaign);
        registration.setSellerId("seller-1");
        registration.setProductVariantId("variant-1");
        registration.setPriceBeforeDiscount(100000D);
        registration.setPriceAfterDiscount(75000D);
        registration.setRegistrationStatus(RegistrationStatus.PENDING);
        registration.setTrangThai(Status.CHUA_KICH_HOAT);
        when(campaignRepository.findById("campaign-1")).thenReturn(Optional.of(campaign));
        when(registrationRepository.findById("registration-1")).thenReturn(Optional.of(registration));
        when(catalogClient.getProductVariant("variant-1")).thenReturn(variant("seller-1"));

        FlashSaleReviewRequest request = new FlashSaleReviewRequest();
        request.setDecision("APPROVE");
        service.review("campaign-1", "registration-1", "staff-1", request);

        assertEquals(RegistrationStatus.APPROVED, registration.getRegistrationStatus());
        assertEquals(Status.DANG_SU_DUNG, registration.getTrangThai());
        assertEquals("staff-1", registration.getReviewedByStaffId());
    }

    private FlashSaleService service() {
        return new FlashSaleService(campaignRepository, registrationRepository, catalogClient);
    }

    private FlashSaleCampaignRequest campaignRequest(long now) {
        FlashSaleCampaignRequest request = new FlashSaleCampaignRequest();
        request.setName("Flash Sale 9.9");
        request.setDescription("Campaign test");
        request.setRegistrationStartDate(now - 1000);
        request.setRegistrationEndDate(now + 60000);
        request.setStartDate(now + 120000);
        request.setEndDate(now + 240000);
        return request;
    }

    private PromotionCampaign openCampaign() {
        long now = System.currentTimeMillis();
        PromotionCampaign campaign = new PromotionCampaign();
        campaign.setId("campaign-1");
        campaign.setCampaignType(CampaignType.FLASH_SALE);
        campaign.setName("Flash Sale");
        campaign.setRegistrationStartDate(now - 60000);
        campaign.setRegistrationEndDate(now + 60000);
        campaign.setStartDate(now + 120000);
        campaign.setEndDate(now + 240000);
        campaign.setTrangThai(StatusPromotion.CHUA_KICH_HOAT);
        return campaign;
    }

    private CatalogVariantSnapshot variant(String sellerId) {
        return new CatalogVariantSnapshot("variant-1", "product-1", sellerId, "SKU-1", "Product",
                "Mau: Do", List.of(), new BigDecimal("100000"), 10, "image", "ACTIVE");
    }
}
