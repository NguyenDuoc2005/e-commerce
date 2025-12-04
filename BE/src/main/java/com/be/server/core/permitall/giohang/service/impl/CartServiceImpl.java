package com.be.server.core.permitall.giohang.service.impl;

import com.be.server.core.common.base.ResponseObject;
import com.be.server.core.permitall.giohang.model.request.CartDetailRequest;
import com.be.server.core.permitall.giohang.model.request.CartGetAllRequest;
import com.be.server.core.permitall.giohang.service.CartService;
import com.be.server.entity.Cart;
import com.be.server.entity.CartDetail;
import com.be.server.entity.KhachHang;
import com.be.server.entity.SanPhamChiTiet;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.repository.CartDetailRepository;
import com.be.server.repository.CartRepository;
import com.be.server.repository.KhachHangRepository;
import com.be.server.repository.SanPhamChiTietRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final KhachHangRepository khachHangRepository;

    private final CartDetailRepository cartDetailRepository;

    private final SanPhamChiTietRepository sanPhamChiTietRepository;



    private Cart createCart(String idUser) {

        KhachHang khachHang = khachHangRepository.findById(idUser).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng"));

        Optional<KhachHang>khachHang1  =  khachHangRepository.findById(idUser);

        Optional<Cart> cart = cartRepository.findByKhachHang(khachHang1.get());
        if (cart.isEmpty()) {
            Cart cart1 = Cart.builder().khachHang(khachHang).build();

            cart1.setStatus(EntityStatus.ACTIVE);
            cartRepository.save(cart1);

            return cart1;
        }

        return cart.get();
    }

    @Override
    public ResponseObject<?> getAllProductCart(CartGetAllRequest req) {
        log.info("rqeust getall cartDetail : ===> {}", req.toString());
        List<CartDetail> list = cartDetailRepository.getAllCart(findByCart(req.getIdUser()).getId());
        return new ResponseObject<>(list, HttpStatus.OK, "Lấy dữ liệu thành công");
    }

    @Override
    public ResponseObject<?> createCartDetail(CartDetailRequest req) {

        log.info("request create cartDetail : ===> {}", req.toString());
        Cart newCart;
        Optional<KhachHang>khachHang1  =  khachHangRepository.findById(req.getIdKhachHang());

        Optional<Cart> cart = cartRepository.findByKhachHang(khachHang1.get());
        if (cart.isEmpty()) {
            newCart = createCart(req.getIdKhachHang());
        } else {
            newCart = cart.get();
        }
        System.out.println( "1");

        SanPhamChiTiet sanPhamChiTiet = findBySPCT(req.getIdSPCT());
        System.out.println( "2");
        if (sanPhamChiTiet.getSoLuong() < Integer.valueOf(req.getQuantity())) {
            return new ResponseObject<>().success("Số lượng sản phẩm không đủ");
        }
        System.out.println( "3");
        System.out.println(cartRepository.checkChungSp(newCart.getId(), sanPhamChiTiet.getId()));
        if(cartRepository.checkChungSp(newCart.getId(), sanPhamChiTiet.getId()) == null){
            CartDetail cartItems = new CartDetail();
            cartItems.setPrice(Double.parseDouble(req.getPrice()));
            cartItems.setCart(cart.get());
            cartItems.setSanPhamChiTiet(sanPhamChiTiet);
            cartItems.setQuantity(Integer.parseInt(req.getQuantity()));
            cartItems.setStatus(EntityStatus.ACTIVE);
            log.info("Dữ liệu cartdetail: ===>{} ", cartItems.toString());
            try {
                cartDetailRepository.save(cartItems);
            } catch (Exception e) {
                log.info("Đã chạy vào đấy");
                log.error(e.toString());
            }
            System.out.println( "4");
            return new ResponseObject<>().success("Thêm thành công");
        }else {

            String idCD = cartDetailRepository.getCart(newCart.getId(),sanPhamChiTiet.getId());
            System.out.println("idCD"+idCD);
            CartDetail cartItems = cartDetailRepository.findById(idCD).get();
            cartItems.setPrice(cartItems.getPrice() + Integer.parseInt(req.getPrice()));
            cartItems.setQuantity(cartItems.getQuantity() + Integer.parseInt(req.getQuantity()));

            if(cartItems.getQuantity() > sanPhamChiTiet.getSoLuong() ) {
                return new ResponseObject<>().success("Số lượng sản phẩm trong giỏ hàng đã vượt quá số lượng sản phẩm"  );
            }


            log.info("Dữ liệu cartdetail: ===>{} ", cartItems.toString());
            try {
                cartDetailRepository.save(cartItems);
            } catch (Exception e) {
                log.info("Đã chạy vào đấy");
                log.error(e.toString());
            }
            System.out.println( "4");
            return new ResponseObject<>().success("Thêm thành công");

        }


    }

    @Override
    public ResponseObject<?> deleteCartDetail(String id) {

        cartDetailRepository.deleteById(id);
        return new ResponseObject<>().success("Xóa thành công");
    }

    private SanPhamChiTiet findBySPCT(String id) {

        return sanPhamChiTietRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy"));
    }

    private Cart findByCart(String id) {



        return cartRepository.findByKhachHang(khachHang(id)).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy"));
    }

    private KhachHang khachHang(String id) {
        return khachHangRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng"));
    }


}
