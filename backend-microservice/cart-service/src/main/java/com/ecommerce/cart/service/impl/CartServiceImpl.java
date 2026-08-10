package com.ecommerce.cart.service.impl;

import com.ecommerce.cart.client.CatalogClient;
import com.ecommerce.cart.constant.EntityStatus;
import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartDetail;
import com.ecommerce.cart.model.request.CartDetailRequest;
import com.ecommerce.cart.model.request.CartGetAllRequest;
import com.ecommerce.cart.repository.CartDetailRepository;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.common.base.ResponseObject;
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

    public CartServiceImpl(
            CartRepository cartRepository,
            CartDetailRepository cartDetailRepository,
            CatalogClient catalogClient
    ) {
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.catalogClient = catalogClient;
    }

    @Override
    public ResponseObject<?> getAllProductCart(CartGetAllRequest req) {
        List<Map<String, Object>> list = cartDetailRepository.getAllCart(findByCart(req.getIdUser()).getId())
                .stream()
                .map(this::toCartResponse)
                .toList();
        return new ResponseObject<>(list, HttpStatus.OK, "Lay du lieu thanh cong");
    }

    @Override
    public ResponseObject<?> createCartDetail(CartDetailRequest req) {
        Cart cart = cartRepository.findByKhachHangId(req.getIdKhachHang()).orElseGet(() -> createCart(req.getIdKhachHang()));
        Map<String, Object> sanPhamChiTiet = findBySPCT(req.getIdSPCT());
        int quantity = Integer.parseInt(req.getQuantity());

        if (intValue(sanPhamChiTiet.get("soLuong")) < quantity) {
            return new ResponseObject<>().success("So luong san pham khong du");
        }

        String existingCartDetailId = cartRepository.checkChungSp(cart.getId(), req.getIdSPCT());
        if (existingCartDetailId == null) {
            CartDetail cartDetail = new CartDetail();
            cartDetail.setPrice(Double.parseDouble(req.getPrice()));
            cartDetail.setCart(cart);
            cartDetail.setSanPhamChiTietId(req.getIdSPCT());
            cartDetail.setQuantity(quantity);
            cartDetail.setStatus(EntityStatus.ACTIVE);
            cartDetailRepository.save(cartDetail);
            return new ResponseObject<>().success("Them thanh cong");
        }

        CartDetail cartDetail = cartDetailRepository.findById(existingCartDetailId).orElseThrow();
        cartDetail.setPrice(cartDetail.getPrice() + Double.parseDouble(req.getPrice()));
        cartDetail.setQuantity(cartDetail.getQuantity() + quantity);
        if (cartDetail.getQuantity() > intValue(sanPhamChiTiet.get("soLuong"))) {
            return new ResponseObject<>().success("So luong san pham trong gio hang da vuot qua so luong san pham");
        }
        cartDetailRepository.save(cartDetail);
        return new ResponseObject<>().success("Them thanh cong");
    }

    @Override
    public ResponseObject<?> deleteCartDetail(String id) {
        cartDetailRepository.deleteById(id);
        return new ResponseObject<>().success("Xoa thanh cong");
    }

    private Cart createCart(String khachHangId) {
        Cart cart = new Cart();
        cart.setKhachHangId(khachHangId);
        cart.setStatus(EntityStatus.ACTIVE);
        return cartRepository.save(cart);
    }

    private Map<String, Object> findBySPCT(String id) {
        Map<String, Object> productDetail = catalogClient.getProductDetail(id);
        if (productDetail == null || productDetail.isEmpty()) {
            throw new EntityNotFoundException("Khong tim thay");
        }
        return productDetail;
    }

    private Cart findByCart(String id) {
        return cartRepository.findByKhachHangId(id).orElseThrow(() -> new EntityNotFoundException("Khong tim thay"));
    }

    private Map<String, Object> toCartResponse(CartDetail detail) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", detail.getId());
        row.put("quantity", detail.getQuantity());
        row.put("price", detail.getPrice());
        row.put("cartId", detail.getCart() == null ? null : detail.getCart().getId());
        row.put("sanPhamChiTietId", detail.getSanPhamChiTietId());
        row.put("sanPhamChiTiet", findBySPCT(detail.getSanPhamChiTietId()));
        row.put("status", detail.getStatus());
        return row;
    }

    private int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return value == null ? 0 : Integer.parseInt(String.valueOf(value));
    }
}
