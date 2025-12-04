package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.listener.CreateChiTietPhieuGiamGiaEntityListener;
import com.be.server.infrastructure.listener.CreateDotGiamGIaEntityListener;
import com.be.server.infrastructure.listener.CreateVoucherEntityListener;
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
@Table(name = "phieu_giam_gia_chi_tiet_khach_hang")

@DynamicUpdate
@EntityListeners(CreateChiTietPhieuGiamGiaEntityListener.class)
public class PhieuGiamGiaChiTiet extends PrimaryEntity implements Serializable {

    @Column(name = "ma_phieu_giam_gia_chi_tiet")
    private String ma;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang", referencedColumnName = "id")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "id_phieu_giam_gia", referencedColumnName = "id")
    private PhieuGiamGia phieuGiamGia;

}
