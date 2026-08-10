package com.ecommerce.cart.entity;

import com.ecommerce.cart.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "gio_hang_chi_tiet")
public class CartDetail extends PrimaryEntity {
    @Column(name = "id_san_pham_chi_tiet")
    private String sanPhamChiTietId;

    @ManyToOne
    @JoinColumn(name = "id_gio_hang", referencedColumnName = "id")
    private Cart cart;

    @Column(name = "so_luong")
    private Integer quantity;

    @Column(name = "tien")
    private Double price;

    public String getSanPhamChiTietId() { return sanPhamChiTietId; }
    public void setSanPhamChiTietId(String sanPhamChiTietId) { this.sanPhamChiTietId = sanPhamChiTietId; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
