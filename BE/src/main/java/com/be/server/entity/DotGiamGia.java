package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.StatusPromotion;
import com.be.server.infrastructure.listener.CreateDotGiamGIaEntityListener;
import com.be.server.infrastructure.listener.CreateSanPhamChiTietEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "dot_giam_gia")
@DynamicUpdate
@Builder
@EntityListeners(CreateDotGiamGIaEntityListener.class)
public class DotGiamGia extends PrimaryEntity implements Serializable {

    @Column(name = "ma_dot_giam_gia")
    private String ma;

    @Column(name = "ten_dot_giam_gia")
    private String ten;

    @Column(name = "phan_tram")
    private Double phanTramGiam;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "ngay_bat_dau")
    private Long ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private Long ngayKetThuc;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_dot")
    private StatusPromotion trangThai;


}
