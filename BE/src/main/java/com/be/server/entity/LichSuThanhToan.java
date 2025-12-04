package com.be.server.entity;

import com.be.server.infrastructure.constant.EntityLoaiHoaDon;
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
@Table(name = "lich_su_thanh_toan")
public class LichSuThanhToan implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "so_tien")
    private Double soTien;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;

    @Column(name = "ma_giao_dich")
    private String maGiaoDich;

    @Column(name = "loai_giao_dich")
    private String loaiGiaoDich;

    @ManyToOne
    @JoinColumn(name = "nhan_vien_id")
    private NhanVien nhanVien;

    @ManyToOne
    @JoinColumn(name = "hoa_don_id", referencedColumnName = "id")
    private HoaDon hoaDon;

    @Column(name = "ghi_chu")
    private String ghiChu;

}
