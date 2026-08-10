package com.ecommerce.catalog.service.impl;

import com.ecommerce.catalog.client.PromotionClient;
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
    private final PromotionClient promotionClient;

    public ProductServiceImpl(
            SanPhamRepository sanPhamRepository,
            SanPhamChiTietRepository sanPhamChiTietRepository,
            ThuongHieuRepository thuongHieuRepository,
            DanhMucRepository danhMucRepository,
            LoaiDeRepository loaiDeRepository,
            XuatSuRepository xuatSuRepository,
            ChatLieuRepository chatLieuRepository,
            PromotionClient promotionClient
    ) {
        this.sanPhamRepository = sanPhamRepository;
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
        this.thuongHieuRepository = thuongHieuRepository;
        this.danhMucRepository = danhMucRepository;
        this.loaiDeRepository = loaiDeRepository;
        this.xuatSuRepository = xuatSuRepository;
        this.chatLieuRepository = chatLieuRepository;
        this.promotionClient = promotionClient;
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
        List<Map<String, Object>> rows = publicProductRows(request);
        enrichPublicProducts(rows);
        return new ResponseObject<>(pageMap(rows, request), HttpStatus.OK, "Lay danh sach san pham moi thanh cong");
    }

    @Override
    public ResponseObject<?> getSanPhamGiamGia(ProductSearchRequest request) {
        List<Map<String, Object>> rows = publicProductRows(request);
        enrichPublicProducts(rows);
        rows = rows.stream().filter(row -> row.get("dotGiamGia") != null).toList();
        return new ResponseObject<>(pageMap(rows, request), HttpStatus.OK, "Lay danh sach san pham giam gia thanh cong");
    }

    @Override
    public ResponseObject<?> getThuongHieuTrangChu(ProductSearchRequest request) {
        List<Map<String, Object>> all = thuongHieuRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE).stream()
                .map(attribute -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", attribute.getId());
                    row.put("ma", attribute.getMa());
                    row.put("ten", attribute.getTen());
                    return row;
                })
                .toList();
        List<Map<String, Object>> rows = slice(all, offset(request), pageSize(request));
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

    private List<Map<String, Object>> publicProductRows(ProductSearchRequest request) {
        List<Map<String, Object>> all = sanPhamRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE).stream()
                .map(product -> {
                    List<SanPhamChiTiet> details = sanPhamChiTietRepository.findBySanPhamIdAndStatusOrderByCreatedDateDesc(product.getId(), EntityStatus.ACTIVE);
                    if (details.isEmpty()) {
                        return null;
                    }
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", product.getId());
                    row.put("ten", product.getTen());
                    row.put("anh", details.stream().map(SanPhamChiTiet::getAnh).filter(java.util.Objects::nonNull).findFirst().orElse(null));
                    row.put("tenThuongHieu", product.getThuongHieu() == null ? null : product.getThuongHieu().getTen());
                    row.put("tenDanhMuc", product.getDanhMuc() == null ? null : product.getDanhMuc().getTen());
                    row.put("tenChatLieu", product.getChatLieu() == null ? null : product.getChatLieu().getTen());
                    row.put("tenXuatXu", product.getXuatSu() == null ? null : product.getXuatSu().getTen());
                    row.put("moTa", product.getMoTa());
                    row.put("giaBan", details.stream().map(SanPhamChiTiet::getGiaBan).filter(java.util.Objects::nonNull).mapToDouble(Double::doubleValue).min().orElse(0D));
                    row.put("createdDate", product.getCreatedDate());
                    return row;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        return slice(all, offset(request), pageSize(request));
    }

    private void enrichPublicProducts(List<Map<String, Object>> rows) {
        long now = System.currentTimeMillis();
        for (Map<String, Object> row : rows) {
            String productId = String.valueOf(row.get("id"));
            List<SanPhamChiTiet> details = sanPhamChiTietRepository.findBySanPhamIdAndStatusOrderByCreatedDateDesc(productId, EntityStatus.ACTIVE);
            row.put("kichCo", details.stream()
                    .map(SanPhamChiTiet::getKichCo)
                    .filter(java.util.Objects::nonNull)
                    .map(com.ecommerce.catalog.entity.KichCo::getTen)
                    .distinct()
                    .toList());
            row.put("mauSac", details.stream()
                    .map(SanPhamChiTiet::getMauSac)
                    .filter(java.util.Objects::nonNull)
                    .map(com.ecommerce.catalog.entity.MauSac::getTen)
                    .distinct()
                    .toList());
            row.put("dsAnh", details.stream().map(SanPhamChiTiet::getAnh).filter(java.util.Objects::nonNull).toList());
            List<Map<String, Object>> discounts = promotionClient.getActiveDiscounts(details.stream().map(SanPhamChiTiet::getId).toList());
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

    private List<Map<String, Object>> slice(List<Map<String, Object>> rows, int offset, int size) {
        if (offset >= rows.size()) {
            return List.of();
        }
        return rows.subList(offset, Math.min(rows.size(), offset + size));
    }
}
