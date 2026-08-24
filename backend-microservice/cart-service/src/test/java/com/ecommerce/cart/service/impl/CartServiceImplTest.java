package com.ecommerce.cart.service.impl;

import com.ecommerce.cart.client.CatalogClient;
import com.ecommerce.cart.client.SellerClient;
import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartDetail;
import com.ecommerce.cart.model.request.CartDetailRequest;
import com.ecommerce.cart.repository.CartDetailRepository;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.common.catalog.CatalogVariantSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {
    @Mock private CartRepository cartRepository;
    @Mock private CartDetailRepository detailRepository;
    @Mock private CatalogClient catalogClient;
    @Mock private SellerClient sellerClient;

    @Test
    void ignoresClientPriceAndUsesCatalogPriceStockAndSellerSnapshot() {
        Cart cart = new Cart();
        cart.setId("cart-1");
        when(cartRepository.findByCustomerId("customer-1")).thenReturn(Optional.of(cart));
        when(cartRepository.checkChungSp("cart-1", "variant-1")).thenReturn(null);
        when(catalogClient.getProductVariant("variant-1")).thenReturn(new CatalogVariantSnapshot(
                "variant-1", "product-1", "seller-1", "SKU-1", "Product", "Màu: Đỏ",
                List.of(new CatalogVariantSnapshot.VariantSelection("axis-1", "Màu", "value-1", "Đỏ")),
                new BigDecimal("125000.50"), 10, "image", "ACTIVE"));
        when(sellerClient.publicProfile("seller-1")).thenReturn(Map.of(
                "shopName", "Shop One", "sellerSlug", "shop-one"));
        CartDetailRequest request = new CartDetailRequest();
        request.setIdCustomer("customer-1");
        request.setIdSPCT("variant-1");
        request.setQuantity("2");
        request.setPrice("1");

        new CartServiceImpl(cartRepository, detailRepository, catalogClient, sellerClient)
                .createCartDetail(request);

        ArgumentCaptor<CartDetail> saved = ArgumentCaptor.forClass(CartDetail.class);
        verify(detailRepository).save(saved.capture());
        assertEquals(250001.0D, saved.getValue().getPrice());
        assertEquals(2, saved.getValue().getQuantity());
        assertEquals("seller-1", saved.getValue().getSellerId());
        assertEquals("Shop One", saved.getValue().getShopName());
    }
}
