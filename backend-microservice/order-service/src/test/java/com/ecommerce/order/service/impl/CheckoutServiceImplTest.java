package com.ecommerce.order.service.impl;

import com.ecommerce.common.catalog.CatalogVariantSnapshot;
import com.ecommerce.order.client.CartClient;
import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.client.PromotionClient;
import com.ecommerce.order.client.SellerClient;
import com.ecommerce.order.client.UserClient;
import com.ecommerce.order.model.request.CheckoutProductItem;
import com.ecommerce.order.model.request.CheckoutRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceImplTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private CatalogClient catalogClient;
    @Mock private PromotionClient promotionClient;
    @Mock private CartClient cartClient;
    @Mock private UserClient userClient;
    @Mock private SellerClient sellerClient;

    private CheckoutServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CheckoutServiceImpl(
                jdbcTemplate, catalogClient, promotionClient, cartClient, userClient, sellerClient);
    }

    @Test
    void rejectsCheckoutWithoutProductsBeforeWritingAnything() {
        CheckoutRequest request = validRequest();
        request.setProduct(List.of());

        assertThrows(IllegalArgumentException.class, () -> service.createOrder(request));

        verifyNoInteractions(jdbcTemplate, catalogClient, promotionClient, cartClient);
    }

    @Test
    void acceptsCanonicalVariantIdAndReturnsNullWhenStockIsInsufficient() {
        CheckoutRequest request = validRequest();
        when(catalogClient.getProductVariant("variant-1")).thenReturn(new CatalogVariantSnapshot(
                "variant-1", "product-1", "seller-1", "SKU-1", "Product", "Default",
                List.of(), BigDecimal.valueOf(100_000), 0, null, "ACTIVE"));

        assertNull(service.createOrder(request));

        verify(catalogClient).getProductVariant("variant-1");
        verifyNoInteractions(jdbcTemplate, promotionClient, cartClient);
    }

    private CheckoutRequest validRequest() {
        CheckoutProductItem item = new CheckoutProductItem();
        item.setId("variant-1");
        item.setQuantity(1);

        CheckoutRequest request = new CheckoutRequest();
        request.setHoTen("Nguyen Van A");
        request.setSoDienThoai("0900000000");
        request.setAddress("Ha Noi");
        request.setHinhThucThanhToan("TIEN_MAT");
        request.setTongTien(100_000D);
        request.setPhiShip(30_000D);
        request.setGiamGia(0D);
        request.setTongCong(130_000D);
        request.setCustomer("khach le");
        request.setProduct(List.of(item));
        return request;
    }
}
