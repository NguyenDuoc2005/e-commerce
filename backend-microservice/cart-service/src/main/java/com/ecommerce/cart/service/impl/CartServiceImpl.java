package com.ecommerce.cart.service.impl;

import com.ecommerce.cart.constant.EntityStatus;
import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartDetail;
import com.ecommerce.cart.entity.KhachHang;
import com.ecommerce.cart.entity.SanPhamChiTiet;
import com.ecommerce.cart.model.request.CartDetailRequest;
import com.ecommerce.cart.model.request.CartGetAllRequest;
import com.ecommerce.cart.repository.CartDetailRepository;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.cart.repository.KhachHangRepository;
import com.ecommerce.cart.repository.SanPhamChiTietRepository;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.common.base.ResponseObject;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final KhachHangRepository khachHangRepository;
    private final CartDetailRepository cartDetailRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    public CartServiceImpl(
            CartRepository cartRepository,
            KhachHangRepository khachHangRepository,
            CartDetailRepository cartDetailRepository,
            SanPhamChiTietRepository sanPhamChiTietRepository
    ) {
        this.cartRepository = cartRepository;
        this.khachHangRepository = khachHangRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
    }

    @Override
    public ResponseObject<?> getAllProductCart(CartGetAllRequest req) {
        List<CartDetail> list = cartDetailRepository.getAllCart(findByCart(req.getIdUser()).getId());
        return new ResponseObject<>(list, HttpStatus.OK, "Lay du lieu thanh cong");
    }

    @Override
    public ResponseObject<?> createCartDetail(CartDetailRequest req) {
        KhachHang khachHang = khachHang(req.getIdKhachHang());
        Cart cart = cartRepository.findByKhachHang(khachHang).orElseGet(() -> createCart(khachHang));
        SanPhamChiTiet sanPhamChiTiet = findBySPCT(req.getIdSPCT());
        int quantity = Integer.parseInt(req.getQuantity());

        if (sanPhamChiTiet.getSoLuong() < quantity) {
            return new ResponseObject<>().success("So luong san pham khong du");
        }

        String existingCartDetailId = cartRepository.checkChungSp(cart.getId(), sanPhamChiTiet.getId());
        if (existingCartDetailId == null) {
            CartDetail cartDetail = new CartDetail();
            cartDetail.setPrice(Double.parseDouble(req.getPrice()));
            cartDetail.setCart(cart);
            cartDetail.setSanPhamChiTiet(sanPhamChiTiet);
            cartDetail.setQuantity(quantity);
            cartDetail.setStatus(EntityStatus.ACTIVE);
            cartDetailRepository.save(cartDetail);
            return new ResponseObject<>().success("Them thanh cong");
        }

        CartDetail cartDetail = cartDetailRepository.findById(existingCartDetailId).orElseThrow();
        cartDetail.setPrice(cartDetail.getPrice() + Double.parseDouble(req.getPrice()));
        cartDetail.setQuantity(cartDetail.getQuantity() + quantity);
        if (cartDetail.getQuantity() > sanPhamChiTiet.getSoLuong()) {
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

    private Cart createCart(KhachHang khachHang) {
        Cart cart = new Cart();
        cart.setKhachHang(khachHang);
        cart.setStatus(EntityStatus.ACTIVE);
        return cartRepository.save(cart);
    }

    private SanPhamChiTiet findBySPCT(String id) {
        return sanPhamChiTietRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Khong tim thay"));
    }

    private Cart findByCart(String id) {
        return cartRepository.findByKhachHang(khachHang(id)).orElseThrow(() -> new EntityNotFoundException("Khong tim thay"));
    }

    private KhachHang khachHang(String id) {
        return khachHangRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Khong tim thay khach hang"));
    }
}
