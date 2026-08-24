package com.ecommerce.cart.service.impl;

import com.ecommerce.cart.client.CatalogClient;
import com.ecommerce.cart.client.SellerClient;
import com.ecommerce.cart.constant.EntityStatus;
import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartDetail;
import com.ecommerce.cart.model.request.CartDetailRequest;
import com.ecommerce.cart.model.request.CartGetAllRequest;
import com.ecommerce.cart.repository.CartDetailRepository;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.catalog.CatalogVariantSnapshot;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final CatalogClient catalogClient;
    private final SellerClient sellerClient;

    public CartServiceImpl(
            CartRepository cartRepository,
            CartDetailRepository cartDetailRepository,
            CatalogClient catalogClient,
            SellerClient sellerClient
    ) {
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.catalogClient = catalogClient;
        this.sellerClient = sellerClient;
    }

    @Override
    public ResponseObject<?> getAllProductCart(CartGetAllRequest req) {
        Cart cart = cartRepository.findByCustomerId(req.getIdUser()).orElseGet(() -> createCart(req.getIdUser()));
        List<Map<String, Object>> list = cartDetailRepository.getAllCart(cart.getId())
                .stream()
                .map(this::toCartResponse)
                .toList();
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("items", list);
        response.put("shopGroups", groupByShop(list));
        return new ResponseObject<>(response, HttpStatus.OK, "Lay du lieu thanh cong");
    }

    @Override
    public ResponseObject<?> createCartDetail(CartDetailRequest req) {
        Cart cart = cartRepository.findByCustomerId(req.getIdCustomer()).orElseGet(() -> createCart(req.getIdCustomer()));
        CatalogVariantSnapshot productVariant = findBySPCT(req.getIdSPCT());
        int quantity = Integer.parseInt(req.getQuantity());

        if (productVariant.quantity() < quantity) {
            return new ResponseObject<>().success("So luong san pham khong du");
        }

        String existingCartDetailId = cartRepository.checkChungSp(cart.getId(), req.getIdSPCT());
        if (existingCartDetailId == null) {
            CartDetail cartDetail = new CartDetail();
            cartDetail.setPrice(productVariant.salePrice().doubleValue() * quantity);
            cartDetail.setCart(cart);
            cartDetail.setProductVariantId(req.getIdSPCT());
            cartDetail.setQuantity(quantity);
            applySellerSnapshot(cartDetail, productVariant);
            cartDetail.setStatus(EntityStatus.ACTIVE);
            cartDetailRepository.save(cartDetail);
            return new ResponseObject<>().success("Them thanh cong");
        }

        CartDetail cartDetail = cartDetailRepository.findById(existingCartDetailId).orElseThrow();
        cartDetail.setQuantity(cartDetail.getQuantity() + quantity);
        cartDetail.setPrice(productVariant.salePrice().doubleValue() * cartDetail.getQuantity());
        applySellerSnapshot(cartDetail, productVariant);
        if (cartDetail.getQuantity() > productVariant.quantity()) {
            return new ResponseObject<>().success("So luong san pham trong gio hang da vuot qua so luong san pham");
        }
        cartDetailRepository.save(cartDetail);
        return new ResponseObject<>().success("Them thanh cong");
    }

    @Override
    public ResponseObject<?> deleteCartDetail(String id, String customerId) {
        CartDetail detail = cartDetailRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Khong tim thay san pham trong gio"));
        if (detail.getCart() == null || !customerId.equals(detail.getCart().getCustomerId())) {
            return new ResponseObject<>(null, HttpStatus.FORBIDDEN, "Khong co quyen xoa san pham nay");
        }
        cartDetailRepository.delete(detail);
        return new ResponseObject<>().success("Xoa thanh cong");
    }

    private Cart createCart(String customerId) {
        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        cart.setStatus(EntityStatus.ACTIVE);
        return cartRepository.save(cart);
    }

    private CatalogVariantSnapshot findBySPCT(String id) {
        CatalogVariantSnapshot productDetail = catalogClient.getProductVariant(id);
        if (productDetail == null) {
            throw new EntityNotFoundException("Khong tim thay");
        }
        return productDetail;
    }

    private Map<String, Object> toCartResponse(CartDetail detail) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", detail.getId());
        row.put("quantity", detail.getQuantity());
        row.put("price", detail.getPrice());
        row.put("cartId", detail.getCart() == null ? null : detail.getCart().getId());
        row.put("productVariantId", detail.getProductVariantId());
        row.put("sellerId", detail.getSellerId());
        row.put("shopName", detail.getShopName());
        row.put("sellerSlug", detail.getSellerSlug());
        row.put("productVariant", findBySPCT(detail.getProductVariantId()));
        row.put("status", detail.getStatus());
        return row;
    }

    private void applySellerSnapshot(CartDetail cartDetail, CatalogVariantSnapshot productVariant) {
        String sellerId = productVariant.sellerId();
        cartDetail.setSellerId(sellerId);
        Map<String, Object> seller = sellerProfile(sellerId);
        cartDetail.setShopName(stringValue(seller.get("shopName")));
        cartDetail.setSellerSlug(stringValue(seller.get("sellerSlug")));
    }

    private List<Map<String, Object>> groupByShop(List<Map<String, Object>> items) {
        Map<String, List<Map<String, Object>>> grouped = items.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        item -> Optional.ofNullable(stringValue(item.get("sellerId"))).orElse("UNKNOWN_SELLER"),
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));
        return grouped.entrySet().stream()
                .map(entry -> {
                    List<Map<String, Object>> shopItems = entry.getValue();
                    Map<String, Object> first = shopItems.isEmpty() ? Map.of() : shopItems.get(0);
                    Map<String, Object> row = new java.util.LinkedHashMap<>();
                    row.put("sellerId", entry.getKey());
                    row.put("shopName", first.get("shopName"));
                    row.put("sellerSlug", first.get("sellerSlug"));
                    row.put("items", shopItems);
                    row.put("totalQuantity", shopItems.stream().mapToInt(item -> intValue(item.get("quantity"))).sum());
                    row.put("subtotal", shopItems.stream().mapToDouble(item -> doubleValue(item.get("price"))).sum());
                    return row;
                })
                .toList();
    }

    private Map<String, Object> sellerProfile(String sellerId) {
        if (sellerId == null || sellerId.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, Object> seller = sellerClient.publicProfile(sellerId);
            return seller == null ? Map.of() : seller;
        } catch (RuntimeException ignored) {
            return Map.of();
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return value == null ? 0 : Integer.parseInt(String.valueOf(value));
    }

    private double doubleValue(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return value == null ? 0D : Double.parseDouble(String.valueOf(value));
    }
}
