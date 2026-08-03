package com.ecommerce.catalog.entity;

import com.ecommerce.catalog.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Random;

@Entity
@Table(name = "san_pham")
@DynamicUpdate
public class SanPham extends PrimaryEntity implements Serializable {

    @Column(name = "ma_san_pham")
    private String ma;

    @Column(name = "ten_san_pham")
    private String ten;

    @Column(name = "mo_ta")
    private String moTa;

    @ManyToOne
    @JoinColumn(name = "id_thuong_hieu", referencedColumnName = "id")
    private ThuongHieu thuongHieu;

    @ManyToOne
    @JoinColumn(name = "id_xuat_su", referencedColumnName = "id")
    private XuatSu xuatSu;

    @ManyToOne
    @JoinColumn(name = "id_danh_muc", referencedColumnName = "id")
    private DanhMuc danhMuc;

    @ManyToOne
    @JoinColumn(name = "id_loai_de", referencedColumnName = "id")
    private LoaiDe loaiDe;

    @ManyToOne
    @JoinColumn(name = "id_chat_lieu", referencedColumnName = "id")
    private ChatLieu chatLieu;

    @PrePersist
    void generateCode() {
        if (ma == null || ma.isBlank()) {
            ma = String.format("SP%04d", new Random().nextInt(10000));
        }
    }

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public ThuongHieu getThuongHieu() { return thuongHieu; }
    public void setThuongHieu(ThuongHieu thuongHieu) { this.thuongHieu = thuongHieu; }
    public XuatSu getXuatSu() { return xuatSu; }
    public void setXuatSu(XuatSu xuatSu) { this.xuatSu = xuatSu; }
    public DanhMuc getDanhMuc() { return danhMuc; }
    public void setDanhMuc(DanhMuc danhMuc) { this.danhMuc = danhMuc; }
    public LoaiDe getLoaiDe() { return loaiDe; }
    public void setLoaiDe(LoaiDe loaiDe) { this.loaiDe = loaiDe; }
    public ChatLieu getChatLieu() { return chatLieu; }
    public void setChatLieu(ChatLieu chatLieu) { this.chatLieu = chatLieu; }
}
