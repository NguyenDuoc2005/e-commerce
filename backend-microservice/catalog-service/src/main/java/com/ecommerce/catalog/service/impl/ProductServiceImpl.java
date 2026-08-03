package com.ecommerce.catalog.service.impl;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.SanPham;
import com.ecommerce.catalog.entity.SanPhamChiTiet;
import com.ecommerce.catalog.model.request.ProductRequest;
import com.ecommerce.catalog.model.request.ProductSearchRequest;
import com.ecommerce.catalog.repository.ChatLieuRepository;
import com.ecommerce.catalog.repository.DanhMucRepository;
import com.ecommerce.catalog.repository.LoaiDeRepository;
import com.ecommerce.catalog.repository.SanPhamChiTietRepository;
import com.ecommerce.catalog.repository.SanPhamRepository;
import com.ecommerce.catalog.repository.ThuongHieuRepository;
import com.ecommerce.catalog.repository.XuatSuRepository;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.common.base.PageableObject;
import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.util.PageUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final SanPhamRepository sanPhamRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final DanhMucRepository danhMucRepository;
    private final LoaiDeRepository loaiDeRepository;
    private final XuatSuRepository xuatSuRepository;
    private final ChatLieuRepository chatLieuRepository;
    private final JdbcTemplate jdbcTemplate;

    public ProductServiceImpl(
            SanPhamRepository sanPhamRepository,
            SanPhamChiTietRepository sanPhamChiTietRepository,
            ThuongHieuRepository thuongHieuRepository,
            DanhMucRepository danhMucRepository,
            LoaiDeRepository loaiDeRepository,
            XuatSuRepository xuatSuRepository,
            ChatLieuRepository chatLieuRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.sanPhamRepository = sanPhamRepository;
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
        this.thuongHieuRepository = thuongHieuRepository;
        this.danhMucRepository = danhMucRepository;
        this.loaiDeRepository = loaiDeRepository;
        this.xuatSuRepository = xuatSuRepository;
        this.chatLieuRepository = chatLieuRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ResponseObject<?> getAll(ProductSearchRequest request) {
        Pageable pageable = PageUtils.createPageable(request, "createdDate");
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            request.setEntityStatus("0".equals(request.getStatus()) ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        }
        return new ResponseObject<>(
                PageableObject.of(sanPhamRepository.getAllSanPhamByFilter(pageable, request)),
                HttpStatus.OK,
                "Lay danh sach san pham thanh cong"
        );
    }

    @Override
    public ResponseObject<?> getSanPhamById(String id) {
        return sanPhamRepository.getAllSanPhamID(id)
                .map(product -> new ResponseObject<>(product, HttpStatus.OK, "san pham thanh cong"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham"));
    }

    @Override
    public ResponseObject<?> modifySanPham(ProductRequest request) {
        if (StringUtils.hasLength(request.getId())) {
            Optional<SanPham> existing = sanPhamRepository.findById(request.getId());
            if (existing.isPresent()) {
                SanPham sanPham = existing.get();
                applyRequest(sanPham, request);
                sanPhamRepository.save(sanPham);
                return new ResponseObject<>(sanPham, HttpStatus.OK, "Cap nhat size thanh cong");
            }
        }

        SanPham sanPham = new SanPham();
        applyRequest(sanPham, request);
        sanPham.setStatus(EntityStatus.ACTIVE);
        sanPhamRepository.save(sanPham);
        return new ResponseObject<>(sanPham, HttpStatus.CREATED, "Tao san pham thanh cong");
    }

    @Override
    public ResponseObject<?> changeSanPhamStatus(String id) {
        Optional<SanPham> optional = sanPhamRepository.findById(id);
        if (optional.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Khong tim thay san pham");
        }

        SanPham sanPham = optional.get();
        EntityStatus newStatus = sanPham.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE;
        sanPham.setStatus(newStatus);
        sanPhamRepository.save(sanPham);

        List<String> ids = sanPhamChiTietRepository.checkIdSanPhamCT(id);
        for (String spctId : ids) {
            sanPhamChiTietRepository.findById(spctId).ifPresent(spct -> {
                spct.setStatus(newStatus);
                sanPhamChiTietRepository.save(spct);
            });
        }

        return new ResponseObject<>(null, HttpStatus.OK, "Thay doi trang thai thanh cong");
    }

    @Override
    public ResponseObject<?> getListThuongHieu() {
        return new ResponseObject<>(sanPhamRepository.getListThuongHieu(), HttpStatus.OK, "Lay thanh cong danh sach thuong hieu");
    }

    @Override
    public ResponseObject<?> getXuatXu() {
        return new ResponseObject<>(sanPhamRepository.getListXuatXu(), HttpStatus.OK, "Lay thanh cong danh sach thuong hieu");
    }

    @Override
    public ResponseObject<?> getListLoaiDe() {
        return new ResponseObject<>(sanPhamRepository.getLoaiDe(), HttpStatus.OK, "Lay thanh cong danh sach thuong hieu");
    }

    @Override
    public ResponseObject<?> getListDanhMuc() {
        return new ResponseObject<>(sanPhamRepository.getListDanhMuc(), HttpStatus.OK, "Lay thanh cong danh sach thuong hieu");
    }

    @Override
    public ResponseObject<?> getListSize() {
        return new ResponseObject<>(sanPhamRepository.getListSize(), HttpStatus.OK, "Lay thanh cong danh sach size");
    }

    @Override
    public ResponseObject<?> getListMau() {
        return new ResponseObject<>(sanPhamRepository.getListMau(), HttpStatus.OK, "Lay thanh cong danh sach mau");
    }

    @Override
    public ResponseObject<?> getListChatLieu() {
        return new ResponseObject<>(sanPhamRepository.getListChatLieu(), HttpStatus.OK, "Lay thanh cong danh sach thuong hieu");
    }

    @Override
    public ResponseObject<?> getSanPhamMoi(ProductSearchRequest request) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(publicProductSelect() + publicProductGroupBy() + " ORDER BY sp.created_date DESC LIMIT ? OFFSET ?", pageSize(request), offset(request));
        enrichPublicProducts(rows);
        return new ResponseObject<>(pageMap(rows, request), HttpStatus.OK, "Lay danh sach san pham moi thanh cong");
    }

    @Override
    public ResponseObject<?> getSanPhamGiamGia(ProductSearchRequest request) {
        long now = System.currentTimeMillis();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(publicProductSelect() + """
                 AND EXISTS (
                    SELECT 1
                    FROM dot_giam_gia_chi_tiet_san_pham dggct
                    JOIN dot_giam_gia dgg ON dgg.id = dggct.id_dot_giam_gia
                    WHERE dggct.id_chi_tiet_san_pham = sct.id
                      AND dggct.trang_thai = 'DANG_SU_DUNG'
                      AND dgg.trang_thai_dot = 'DANG_KICH_HOAT'
                      AND dgg.ngay_bat_dau <= ?
                      AND dgg.ngay_ket_thuc >= ?
                 )
                """ + publicProductGroupBy() + """
                ORDER BY sp.created_date DESC
                LIMIT ? OFFSET ?
                """, now, now, pageSize(request), offset(request));
        enrichPublicProducts(rows);
        return new ResponseObject<>(pageMap(rows, request), HttpStatus.OK, "Lay danh sach san pham giam gia thanh cong");
    }

    @Override
    public ResponseObject<?> getThuongHieuTrangChu(ProductSearchRequest request) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT id AS id, ma_thuong_hieu AS ma, ten_thuong_hieu AS ten
                FROM thuong_hieu
                WHERE status = 0
                ORDER BY created_date DESC
                LIMIT ? OFFSET ?
                """, pageSize(request), offset(request));
        return new ResponseObject<>(pageMap(rows, request), HttpStatus.OK, "lay thuong hieu thanh cong");
    }

    private void applyRequest(SanPham sanPham, ProductRequest request) {
        sanPham.setTen(request.getTen());
        sanPham.setMoTa(request.getMoTa());
        if (request.getIdThuongHieu() != null) {
            thuongHieuRepository.findById(request.getIdThuongHieu()).ifPresent(sanPham::setThuongHieu);
        }
        if (request.getIdDanhMuc() != null) {
            danhMucRepository.findById(request.getIdDanhMuc()).ifPresent(sanPham::setDanhMuc);
        }
        if (request.getIdLoaiDe() != null) {
            loaiDeRepository.findById(request.getIdLoaiDe()).ifPresent(sanPham::setLoaiDe);
        }
        if (request.getIdXuatXu() != null) {
            xuatSuRepository.findById(request.getIdXuatXu()).ifPresent(sanPham::setXuatSu);
        }
        if (request.getIdChatLieu() != null) {
            chatLieuRepository.findById(request.getIdChatLieu()).ifPresent(sanPham::setChatLieu);
        }
    }

    private String publicProductSelect() {
        return """
                SELECT sp.id AS id,
                       sp.ten_san_pham AS ten,
                       MIN(sct.anh_san_pham) AS anh,
                       th.ten_thuong_hieu AS tenThuongHieu,
                       dm.ten_danh_muc AS tenDanhMuc,
                       cl.ten_chat_lieu AS tenChatLieu,
                       xs.ten_xuat_su AS tenXuatXu,
                       sp.mo_ta AS moTa,
                       MIN(sct.gia_ban) AS giaBan,
                       sp.created_date AS createdDate
                FROM san_pham sp
                JOIN san_pham_chi_tiet sct ON sct.id_san_pham = sp.id
                LEFT JOIN thuong_hieu th ON th.id = sp.id_thuong_hieu
                LEFT JOIN danh_muc dm ON dm.id = sp.id_danh_muc
                LEFT JOIN chat_lieu cl ON cl.id = sp.id_chat_lieu
                LEFT JOIN xuat_su xs ON xs.id = sp.id_xuat_su
                WHERE sct.status = 0 AND sp.status = 0
                """;
    }

    private String publicProductGroupBy() {
        return " GROUP BY sp.id, sp.ten_san_pham, th.ten_thuong_hieu, dm.ten_danh_muc, cl.ten_chat_lieu, xs.ten_xuat_su, sp.mo_ta, sp.created_date ";
    }

    private void enrichPublicProducts(List<Map<String, Object>> rows) {
        long now = System.currentTimeMillis();
        for (Map<String, Object> row : rows) {
            String productId = String.valueOf(row.get("id"));
            row.put("kichCo", jdbcTemplate.queryForList("""
                    SELECT DISTINCT kc.ten_kich_co
                    FROM san_pham_chi_tiet spct
                    JOIN kich_co kc ON kc.id = spct.id_kich_co
                    WHERE spct.id_san_pham = ?
                    """, String.class, productId));
            row.put("mauSac", jdbcTemplate.queryForList("""
                    SELECT DISTINCT ms.ten_mau_sac
                    FROM san_pham_chi_tiet spct
                    JOIN mau_sac ms ON ms.id = spct.id_mau_sac
                    WHERE spct.id_san_pham = ?
                    """, String.class, productId));
            row.put("dsAnh", jdbcTemplate.queryForList("SELECT anh_san_pham FROM san_pham_chi_tiet WHERE id_san_pham = ? AND anh_san_pham IS NOT NULL", String.class, productId));
            List<Map<String, Object>> discounts = jdbcTemplate.queryForList("""
                    SELECT dgg.ten_dot_giam_gia AS ten,
                           dgg.phan_tram AS phanTramGiam,
                           dggct.gia_truoc_khi_giam AS giaTruoc,
                           dggct.gia_sau_khi_giam AS giaSau,
                           dgg.ngay_bat_dau AS ngayBatDau,
                           dgg.ngay_ket_thuc AS ngayKetThuc
                    FROM san_pham_chi_tiet spct
                    JOIN dot_giam_gia_chi_tiet_san_pham dggct ON dggct.id_chi_tiet_san_pham = spct.id
                    JOIN dot_giam_gia dgg ON dgg.id = dggct.id_dot_giam_gia
                    WHERE spct.id_san_pham = ?
                      AND dggct.trang_thai = 'DANG_SU_DUNG'
                      AND dgg.trang_thai_dot = 'DANG_KICH_HOAT'
                      AND dgg.ngay_bat_dau <= ?
                      AND dgg.ngay_ket_thuc >= ?
                    ORDER BY dgg.phan_tram_giam DESC
                    LIMIT 1
                    """, productId, now, now);
            row.put("dotGiamGia", discounts.isEmpty() ? null : discounts.get(0));
        }
    }

    private Map<String, Object> pageMap(List<Map<String, Object>> rows, ProductSearchRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", rows);
        result.put("totalPages", 1);
        result.put("currentPage", Math.max(request.getPage() - 1, 0));
        result.put("totalElements", rows.size());
        return result;
    }

    private int pageSize(ProductSearchRequest request) {
        return request.getSize() <= 0 ? 10 : request.getSize();
    }

    private int offset(ProductSearchRequest request) {
        return Math.max(request.getPage() - 1, 0) * pageSize(request);
    }
}
