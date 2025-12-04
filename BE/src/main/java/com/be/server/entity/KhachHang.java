package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.listener.CreateKhachHangEntityListener;
import com.be.server.infrastructure.listener.CreateNhanVIenEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
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
@Table(name = "khach_hang")
@DynamicUpdate
@EntityListeners(CreateKhachHangEntityListener.class)
public class KhachHang extends PrimaryEntity implements Serializable {

    @Column(name = "ma_khach_hang")
    private String ma;

    @Column(name = "ten_khach_hang")
    private String ten;

    @Column(name = "so_dien_thoai")
    private String sdt;

    @Column(name = "tinh")
    private String tinh;

    @Column(name = "huyen")
    private String huyen;

    @Column(name = "xa")
    private String xa;

    @Column(name = "email")
    private String email;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "cccd")
    private String cccd;

    @Column(name = "ngay_sinh")
    private Date ngaySinh;

    @Column(name = "gioi_timh")
    private Boolean gioiTimh;

    @Column(name = "mat_khau")
    private String mat_khau;

}
