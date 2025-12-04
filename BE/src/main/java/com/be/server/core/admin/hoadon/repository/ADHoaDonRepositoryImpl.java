package com.be.server.core.admin.hoadon.repository;

import com.be.server.core.admin.hoadon.model.request.ADHoaDonSearchRequest;
import com.be.server.core.admin.hoadon.model.response.*;
import com.be.server.entity.HoaDon;
import com.be.server.infrastructure.constant.EntityLoaiHoaDon;
import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ADHoaDonRepositoryImpl implements ADHoaDonRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public HoaDonPageResponse getAllHoaDonResponse(ADHoaDonSearchRequest request, Pageable pageable) {
        String hql = """
        SELECT new com.be.server.core.admin.hoadon.model.response.ADHoaDonResponse(
                        hd.id,
                        hd.ma,
                        kh.ten,
                        kh.sdt,
                        nv.ma,
                        nv.ten,
                        hd.tongTienSauGiam,
                        hd.loaiHoaDon,
                        hd.createdDate,
                        hd.trangThaiHoaDon
                    )
                    FROM HoaDon hd
                    LEFT JOIN hd.khachHang kh
                    LEFT JOIN hd.nhanVien nv
                    WHERE (:q IS NULL OR :q = '' 
                        OR LOWER(kh.ten) LIKE LOWER(CONCAT('%', :q, '%'))
                        OR LOWER(hd.ma) LIKE LOWER(CONCAT('%', :q, '%'))
                        OR LOWER(hd.ten) LIKE LOWER(CONCAT('%', :q, '%'))
                        OR LOWER(kh.sdt) LIKE LOWER(CONCAT('%', :q, '%'))
                        OR LOWER(nv.ma) LIKE LOWER(CONCAT('%', :q, '%')))
                      AND (:trangThai IS NULL OR hd.trangThaiHoaDon = :trangThai)
                      AND (:startDate IS NULL OR CAST(hd.createdDate AS BIGINTEGER) >= :startDate)
                      AND (:endDate IS NULL OR CAST(hd.createdDate AS BIGINTEGER) <= :endDate)
                      AND hd.trangThaiHoaDon != :luuTam
                    ORDER BY hd.createdDate ASC
                """;

        String countByStatusHql = """
                    SELECT hd.trangThaiHoaDon, COUNT(hd)
                    FROM HoaDon hd
                    LEFT JOIN hd.khachHang kh
                    LEFT JOIN hd.nhanVien nv
                    WHERE (:q IS NULL OR :q = '' 
                        OR LOWER(kh.ten) LIKE LOWER(CONCAT('%', :q, '%'))
                        OR LOWER(kh.sdt) LIKE LOWER(CONCAT('%', :q, '%'))
                        OR LOWER(nv.ten) LIKE LOWER(CONCAT('%', :q, '%')))
                      AND (:startDate IS NULL OR CAST(hd.createdDate AS BIGINTEGER) >= :startDate)
                      AND (:endDate IS NULL OR CAST(hd.createdDate AS BIGINTEGER) <= :endDate)
                      AND hd.trangThaiHoaDon != :luuTam
                    GROUP BY hd.trangThaiHoaDon
                """;

        String totalCountHql = """
                    SELECT COUNT(hd)
                    FROM HoaDon hd
                    LEFT JOIN hd.khachHang kh
                    LEFT JOIN hd.nhanVien nv
                    WHERE (:q IS NULL OR :q = '' 
                        OR LOWER(kh.ten) LIKE LOWER(CONCAT('%', :q, '%'))
                        OR LOWER(kh.sdt) LIKE LOWER(CONCAT('%', :q, '%'))
                        OR LOWER(nv.ten) LIKE LOWER(CONCAT('%', :q, '%')))
                      AND (:trangThai IS NULL OR hd.trangThaiHoaDon = :trangThai)
                      AND (:startDate IS NULL OR CAST(hd.createdDate AS BIGINTEGER) >= :startDate)
                      AND (:endDate IS NULL OR CAST(hd.createdDate AS BIGINTEGER) <= :endDate)
                      AND hd.trangThaiHoaDon != :luuTam
                """;

        List<Object[]> countByStatusList = entityManager.createQuery(countByStatusHql)
                .setParameter("q", request.getQ() == null ? "" : request.getQ().trim())
                .setParameter("startDate", request.getStartDate())
                .setParameter("endDate", request.getEndDate())
                .setParameter("luuTam", EntityTrangThaiHoaDon.LUU_TAM)
                .getResultList();

        Map<EntityTrangThaiHoaDon, Long> countByStatusMap = new HashMap<>();
        for (Object[] row : countByStatusList) {
            EntityTrangThaiHoaDon status = (EntityTrangThaiHoaDon) row[0];
            Long count = (Long) row[1];

            // Bỏ qua nếu là trạng thái LƯU_TẠM
            if (status == EntityTrangThaiHoaDon.LUU_TAM) {
                continue;
            }

            countByStatusMap.put(status, count);
        }


        Long totalRecords = (Long) entityManager.createQuery(totalCountHql)
                .setParameter("q", request.getQ() == null ? "" : request.getQ().trim())
                .setParameter("trangThai", request.getStatus())
                .setParameter("startDate", request.getStartDate())
                .setParameter("endDate", request.getEndDate())
                .setParameter("luuTam", EntityTrangThaiHoaDon.LUU_TAM)
                .getSingleResult();

        List<ADHoaDonResponse> hoaDonResponses = entityManager.createQuery(hql, ADHoaDonResponse.class)
                .setParameter("q", request.getQ() == null ? "" : request.getQ().trim())
                .setParameter("trangThai", request.getStatus())
                .setParameter("startDate", request.getStartDate())
                .setParameter("endDate", request.getEndDate())
                .setParameter("luuTam", EntityTrangThaiHoaDon.LUU_TAM)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Page<ADHoaDonResponse> pageResult = new PageImpl<>(hoaDonResponses, pageable, totalRecords);
        return new HoaDonPageResponse(pageResult, countByStatusMap);
    }

    @Override
    public HoaDonPageResponse1 getAllHoaDonResponse1(ADHoaDonSearchRequest request) {
        String hql = """
        SELECT new com.be.server.core.admin.hoadon.model.response.ADHoaDonResponse1(
                        hd.id,
                        hd.ma,
                        spct.anh,
                        spct.sanPham.ten,
                        spct.sanPham.thuongHieu.ten,
                        spct.mauSac.ten,
                        spct.kichCo.ten,
                        hdct.soLuong,
                        hdct.gia,
                        hd.trangThaiHoaDon,
                        hd.tongTienSauGiam
                    )
        FROM HoaDon hd
        JOIN hd.hoaDonChiTiets hdct
        JOIN hdct.spct spct
        LEFT JOIN hd.khachHang kh
        WHERE (:q IS NULL OR :q = '' OR LOWER(kh.id) LIKE LOWER(CONCAT('%', :q, '%')))
        AND (:trangThai IS NULL OR hd.trangThaiHoaDon = :trangThai)
        AND hd.loaiHoaDon = :loaiHoaDon
        AND (hd.trangThaiHoaDon != 7 )
        ORDER BY hd.createdDate, hd.ma ASC
    """;

        String countByStatusHql = """
        SELECT hd.trangThaiHoaDon, COUNT(hd.id)
        FROM HoaDon hd
        LEFT JOIN hd.khachHang kh
        WHERE (:q IS NULL OR :q = '' OR LOWER(kh.id) LIKE LOWER(CONCAT('%', :q, '%')))
        AND (:trangThai IS NULL OR hd.trangThaiHoaDon = :trangThai)
        AND hd.loaiHoaDon = :loaiHoaDon
        AND (hd.trangThaiHoaDon != 7 )
        GROUP BY hd.trangThaiHoaDon
    """;

        String totalCountHql = """
        SELECT COUNT(hd.id)
        FROM HoaDon hd
        JOIN hd.hoaDonChiTiets hdct
        JOIN hdct.spct spct
        LEFT JOIN hd.khachHang kh
        WHERE (:q IS NULL OR :q = '' OR LOWER(kh.id) LIKE LOWER(CONCAT('%', :q, '%')))
        AND hd.loaiHoaDon = :loaiHoaDon
        AND (hd.trangThaiHoaDon != 7 )
        AND (:trangThai IS NULL OR hd.trangThaiHoaDon = :trangThai)
    """;

        List<ADHoaDonResponse1> hoaDonList = entityManager.createQuery(hql, ADHoaDonResponse1.class)
                .setParameter("q", request.getQ() == null ? "" : request.getQ().trim())
                .setParameter("trangThai", request.getStatus())
                .setParameter("loaiHoaDon", EntityLoaiHoaDon.ONLINE)
                .getResultList();

        Long totalRecords = (Long) entityManager.createQuery(totalCountHql)
                .setParameter("q", request.getQ() == null ? "" : request.getQ().trim())
                .setParameter("trangThai", request.getStatus())
                .setParameter("loaiHoaDon", EntityLoaiHoaDon.ONLINE)
                .getSingleResult();

        List<Object[]> countByStatusList = entityManager.createQuery(countByStatusHql)
                .setParameter("q", request.getQ() == null ? "" : request.getQ().trim())
                .setParameter("trangThai", null)
                .setParameter("loaiHoaDon", EntityLoaiHoaDon.ONLINE)
                .getResultList();

        Map<EntityTrangThaiHoaDon, Long> countByStatusMap = new HashMap<>();
        for (Object[] row : countByStatusList) {
            EntityTrangThaiHoaDon status = (EntityTrangThaiHoaDon) row[0];
            Long count = (Long) row[1];
            countByStatusMap.put(status, count);
        }

        return new HoaDonPageResponse1(hoaDonList, totalRecords, countByStatusMap);
    }

    @Override
    public HoaDonPageResponse1 getAllHoaDonResponse1(String code) {
        String hql = """
        SELECT new com.be.server.core.admin.hoadon.model.response.ADHoaDonResponse1(
                        hd.id,
                        hd.ma,
                        spct.anh,
                        spct.sanPham.ten,
                        spct.sanPham.thuongHieu.ten,
                        spct.mauSac.ten,
                        spct.kichCo.ten,
                        hdct.soLuong,
                        hdct.gia,
                        hd.trangThaiHoaDon,
                        hd.tongTienSauGiam
                    )
        FROM HoaDon hd
        JOIN hd.hoaDonChiTiets hdct
        JOIN hdct.spct spct
        WHERE hd.ma = :code
        AND (hd.trangThaiHoaDon != 7 )
        ORDER BY hd.createdDate, hd.ma ASC
    """;

        String countByStatusHql = """
        SELECT hd.trangThaiHoaDon, COUNT(hd.id)
        FROM HoaDon hd
        WHERE hd.ma = :code
        AND (hd.trangThaiHoaDon != 7 )
        GROUP BY hd.trangThaiHoaDon
    """;

        String totalCountHql = """
        SELECT COUNT(hd.id)
        FROM HoaDon hd
        
        WHERE hd.ma = :code
        AND (hd.trangThaiHoaDon != 7 )
    """;

        // Lấy danh sách hóa đơn
        List<ADHoaDonResponse1> hoaDonList = entityManager.createQuery(hql, ADHoaDonResponse1.class)
                .setParameter("code", code)
                .getResultList();

        // Tổng số bản ghi
        Long totalRecords = (Long) entityManager.createQuery(totalCountHql)
                .setParameter("code", code)
                .getSingleResult();

        // Đếm số lượng theo trạng thái
        List<Object[]> countByStatusList = entityManager.createQuery(countByStatusHql)
                .setParameter("code", code)
                .getResultList();

        Map<EntityTrangThaiHoaDon, Long> countByStatusMap = new HashMap<>();
        for (Object[] row : countByStatusList) {
            EntityTrangThaiHoaDon status = (EntityTrangThaiHoaDon) row[0];
            Long count = (Long) row[1];
            countByStatusMap.put(status, count);
        }

        return new HoaDonPageResponse1(hoaDonList, totalRecords, countByStatusMap);
    }

    @Override
    public List<ADHoaDonChiTietResponseDetail> getAllHoaDonChiTietResponse(String maHoaDon) {
        String hql = """
                SELECT new com.be.server.core.admin.hoadon.model.response.ADHoaDonChiTietResponseDetail(
                    hd.ten,
                    hdct.ma,
                    sp.ten,
                    hdct.spct.anh,
                    sp.thuongHieu.ten,
                    sp.xuatSu.ten,
                    spct.mauSac.ten,
                    spct.kichCo.ten,
                    hdct.soLuong,
                    hdct.tongTien,
                    (hdct.soLuong * hdct.tongTien),
                    kh.ten,
                    kh.sdt,
                    hd.loaiHoaDon,
                    hd.status,
                    hd.createdDate
                )
                FROM HoaDonChiTiet hdct
                LEFT JOIN hdct.hoaDon hd
                LEFT JOIN hd.khachHang kh
                LEFT JOIN hdct.spct spct
                LEFT JOIN spct.sanPham sp
                
                WHERE hdct.hoaDon.ma = :maHoaDon
                AND (hd.trangThaiHoaDon != 7 )
                """;

        return entityManager.createQuery(hql, ADHoaDonChiTietResponseDetail.class)
                .setParameter("maHoaDon", maHoaDon)
                .getResultList();
    }

    @Override
    @Transactional
    public HoaDon updateTrangThaiHoaDon(String maHoaDon, EntityTrangThaiHoaDon newTrangThai) {
        String hql = """
                    UPDATE HoaDon hd
                    SET hd.trangThaiHoaDon = :newTrangThai
                    WHERE hd.ma = :maHoaDon
                """;

        int updatedRows = entityManager.createQuery(hql)
                .setParameter("newTrangThai", newTrangThai)
                .setParameter("maHoaDon", maHoaDon)
                .executeUpdate();

        if (updatedRows == 0) {
            throw new EntityNotFoundException("Không tìm thấy hóa đơn với mã: " + maHoaDon);
        }

        // Retrieve the updated HoaDon object
        return entityManager.createQuery("SELECT hd FROM HoaDon hd WHERE hd.ma = :maHoaDon", HoaDon.class)
                .setParameter("maHoaDon", maHoaDon)
                .getSingleResult();
    }

}
