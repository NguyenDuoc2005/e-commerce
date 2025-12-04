package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.Status;
import com.be.server.infrastructure.listener.CreateDotGiamGIaEntityListener;
import com.be.server.infrastructure.listener.CreateDotGiamGiaChiTietEntityListener;
import jakarta.persistence.*;
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
@Table(name = "dot_giam_gia_chi_tiet_san_pham")
@DynamicUpdate
@EntityListeners(CreateDotGiamGiaChiTietEntityListener.class)
public class DotGiamGiaChiTietSanPham extends PrimaryEntity implements Serializable {

    @Column(name = "ma_dot_giam_gia_chi_tiet_san_pham")
    private String ma;

    @Column(name = "gia_truoc_khi_giam")
    private Double giaTruoc;

    @Column(name = "gia_sau_khi_giam")
    private Double giaSau;

    @Enumerated(EnumType.STRING)
    private Status trangThai;

    @ManyToOne
    @JoinColumn(name = "id_chi_tiet_san_pham", referencedColumnName = "id")
    private SanPhamChiTiet sanPhamChiTiet;

    @ManyToOne
    @JoinColumn(name = "id_dot_giam_gia", referencedColumnName = "id")
    private DotGiamGia dotGiamGia;
}
