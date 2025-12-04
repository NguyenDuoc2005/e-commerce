package com.be.server.entity;


import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.listener.CreateNhanVIenEntityListener;
import com.be.server.infrastructure.listener.CreateSanPhamEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "san_pham")
@DynamicUpdate
@EntityListeners(CreateSanPhamEntityListener.class)
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
    @JoinColumn(name = "id_xuat_su",referencedColumnName = "id")
    private XuatSu xuatSu;

    @ManyToOne
    @JoinColumn(name = "id_danh_muc",referencedColumnName = "id")
    private DanhMuc danhMuc;


    @ManyToOne
    @JoinColumn(name = "id_loai_de",referencedColumnName = "id")
    private LoaiDe loaiDe;

    @ManyToOne
    @JoinColumn(name = "id_chat_lieu",referencedColumnName = "id")
    private ChatLieu chatLieu;
}
