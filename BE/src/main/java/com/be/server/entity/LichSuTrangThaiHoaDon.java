package com.be.server.entity;

import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "lich_su_trang_thai_hoa_don")
public class LichSuTrangThaiHoaDon implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hoa_don_id", referencedColumnName = "id")
    private HoaDon hoaDon;

    @Column(name = "trang_thai")
    private EntityTrangThaiHoaDon trangThai;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;

    @Column(name = "note")
    private String note;

}