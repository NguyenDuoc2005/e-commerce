package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.listener.CreateVoucherEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "phieu_giam_gia")
@DynamicUpdate
@EntityListeners(CreateVoucherEntityListener.class)
public class PhieuGiamGia extends PrimaryEntity implements Serializable {

    @Column(name = "ma_phieu_giam_gia")
    private String ma;

    @Column(name = "ten_phieu_giam_gia")
    private String ten;

    @Column(name = "phan_tram")
    private Double phanTramGiam;

    @Transient // Không lưu vào database
    private Double giaTriGiamThucTe;

    @Column(name = "so_luong_phieu")
    private Integer soLuongPhieu;

    @Column(name = "ngay_bat_dau")
    private Date ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private Date ngayKetThuc;

    @Column(name = "dieu_kien")
    private Double dieuKien;

    @Column(name = "gia_giam_toi_da")
    private Double giaGiam;

    @Column(name = "loai_giam")
    private Boolean loaiGiam;

    @Column(name = "kieu_giam")
    private Boolean kieuGiam;

}
