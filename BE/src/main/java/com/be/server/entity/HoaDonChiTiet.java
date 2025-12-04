package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.listener.CreateHoaDonChiTietEntityListener;
import com.be.server.infrastructure.listener.CreateHoaDonEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
@Table(name = "hoa_don_chi_tiet")
@DynamicUpdate
@EntityListeners(CreateHoaDonChiTietEntityListener.class)
public class HoaDonChiTiet extends PrimaryEntity implements Serializable {

    @Column(name = "ma_hoa_don_chi_tiet")
    private String ma;

    @Column(name = "ten_hoa_don_chi_tiet")
    private String ten;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "gia_ban")
    private Double gia;

    @Transient // Không lưu vào database
    private Double gia1SP;

    @ManyToOne
    @JoinColumn(name = "id_spct")
    private SanPhamChiTiet spct;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don")
    private HoaDon hoaDon;
}
