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
import java.util.Comparator;
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
        List<Map<String, Object>> rows = filteredPublicProductRows(request);
        enrichPublicProducts(rows);
        return new ResponseObject<>(
                pageMap(rows, request),
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
        List<Map<String, Object>> rows = filteredPublicProductRows(request);
        enrichPublicProducts(rows);
        return new ResponseObject<>(pageMap(rows, request), HttpStatus.OK, "Lay danh sach san pham moi thanh cong");
    }

    @Override
    public ResponseObject<?> getSanPhamGiamGia(ProductSearchRequest request) {
        List<Map<String, Object>> rows = filteredPublicProductRows(request);
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

    private List<Map<String, Object>> filteredPublicProductRows(ProductSearchRequest request) {
        String q = request.getQ() == null ? "" : request.getQ().trim().toLowerCase();
        List<String> brandIds = splitIds(request.getThuongHieuIds());
        List<String> materialIds = splitIds(request.getChatLieuIds());
        List<String> soleIds = splitIds(request.getLoaiDeIds());
        List<String> categoryIds = splitIds(request.getDanhMucIds());

        List<Map<String, Object>> all = sanPhamRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE).stream()
                .filter(product -> q.isEmpty()
                        || safe(product.getTen()).toLowerCase().contains(q)
                        || safe(product.getMa()).toLowerCase().contains(q))
                .filter(product -> brandIds.isEmpty() || (product.getThuongHieu() != null && brandIds.contains(product.getThuongHieu().getId())))
                .filter(product -> materialIds.isEmpty() || (product.getChatLieu() != null && materialIds.contains(product.getChatLieu().getId())))
                .filter(product -> soleIds.isEmpty() || (product.getLoaiDe() != null && soleIds.contains(product.getLoaiDe().getId())))
                .filter(product -> categoryIds.isEmpty() || (product.getDanhMuc() != null && categoryIds.contains(product.getDanhMuc().getId())))
                .map(product -> {
                    List<SanPhamChiTiet> details = sanPhamChiTietRepository.findBySanPhamIdAndStatusOrderByCreatedDateDesc(product.getId(), EntityStatus.ACTIVE);
                    details = details.stream()
                            .filter(detail -> request.getGiaMin() == null || value(detail.getGiaBan()) >= request.getGiaMin())
                            .filter(detail -> request.getGiaMax() == null || value(detail.getGiaBan()) <= request.getGiaMax())
                            .toList();
                    if (details.isEmpty()) {
                        return null;
                    }
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", product.getId());
                    row.put("tenSanPham", product.getTen());
                    row.put("hinhAnhDaiDien", details.stream().map(SanPhamChiTiet::getAnh).filter(java.util.Objects::nonNull).findFirst().orElse(null));
                    row.put("thuongHieu", product.getThuongHieu() == null ? null : product.getThuongHieu().getTen());
                    row.put("danhMuc", product.getDanhMuc() == null ? null : product.getDanhMuc().getTen());
                    row.put("chatLieu", product.getChatLieu() == null ? null : product.getChatLieu().getTen());
                    row.put("xuatXu", product.getXuatSu() == null ? null : product.getXuatSu().getTen());
                    row.put("moTa", product.getMoTa());
                    row.put("giaBan", details.stream().map(SanPhamChiTiet::getGiaBan).filter(java.util.Objects::nonNull).mapToDouble(Double::doubleValue).min().orElse(0D));
                    row.put("ngayTao", product.getCreatedDate());
                    row.put("_details", details);
                    return row;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        return sortProducts(all, request.getSortBy());
    }

    private void enrichPublicProducts(List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            @SuppressWarnings("unchecked")
            List<SanPhamChiTiet> details = (List<SanPhamChiTiet>) row.get("_details");
            row.put("kichCo", details.stream()
                    .filter(detail -> detail.getKichCo() != null)
                    .collect(java.util.stream.Collectors.toMap(
                            detail -> detail.getKichCo().getId(),
                            detail -> {
                                Map<String, Object> size = new LinkedHashMap<>();
                                size.put("id", detail.getKichCo().getId());
                                size.put("ten", detail.getKichCo().getTen());
                                size.put("soLuong", value(detail.getSoLuong()));
                                return size;
                            },
                            (left, right) -> {
                                left.put("soLuong", ((Integer) left.get("soLuong")) + ((Integer) right.get("soLuong")));
                                return left;
                            },
                            LinkedHashMap::new
                    ))
                    .values()
                    .stream()
                    .toList());
            row.put("mauSac", details.stream()
                    .filter(detail -> detail.getMauSac() != null)
                    .collect(java.util.stream.Collectors.toMap(
                            detail -> detail.getMauSac().getId(),
                            detail -> {
                                Map<String, Object> color = new LinkedHashMap<>();
                                color.put("id", detail.getMauSac().getId());
                                color.put("ten", detail.getMauSac().getTen());
                                color.put("tenMauSac", detail.getMauSac().getTen());
                                color.put("maMau", detail.getMauSac().getMau());
                                return color;
                            },
                            (left, right) -> left,
                            LinkedHashMap::new
                    ))
                    .values()
                    .stream()
                    .toList());
            row.put("dsAnh", details.stream().map(SanPhamChiTiet::getAnh).filter(java.util.Objects::nonNull).distinct().toList());
            List<Map<String, Object>> discounts = safeActiveDiscounts(details.stream().map(SanPhamChiTiet::getId).toList());
            Map<String, Object> discount = discounts.isEmpty() ? null : publicDiscountMap(discounts.get(0));
            row.put("dotGiamGia", discount);
            row.put("giaSauGiam", discount == null ? null : discount.get("giaSau"));
            row.remove("_details");
        }
    }

    private List<Map<String, Object>> sortProducts(List<Map<String, Object>> rows, String sortBy) {
        Comparator<Map<String, Object>> comparator = Comparator.comparing(row -> String.valueOf(row.get("ngayTao")));
        if ("createdAt_asc".equals(sortBy)) {
            return rows.stream().sorted(comparator).toList();
        }
        if ("giaBan_asc".equals(sortBy)) {
            return rows.stream().sorted(Comparator.comparingDouble(row -> doubleValue(row.get("giaBan")))).toList();
        }
        if ("giaBan_desc".equals(sortBy)) {
            return rows.stream().sorted(Comparator.comparingDouble((Map<String, Object> row) -> doubleValue(row.get("giaBan"))).reversed()).toList();
        }
        if ("ten_asc".equals(sortBy)) {
            return rows.stream().sorted(Comparator.comparing(row -> safe(row.get("tenSanPham")))).toList();
        }
        if ("ten_desc".equals(sortBy)) {
            return rows.stream().sorted(Comparator.comparing((Map<String, Object> row) -> safe(row.get("tenSanPham"))).reversed()).toList();
        }
        return rows.stream().sorted(comparator.reversed()).toList();
    }

    private Map<String, Object> publicDiscountMap(Map<String, Object> discount) {
        Map<String, Object> row = new LinkedHashMap<>(discount);
        row.put("tenDotGiamGia", discount.get("ten"));
        return row;
    }

    private List<Map<String, Object>> safeActiveDiscounts(List<String> productDetailIds) {
        try {
            return promotionClient.getActiveDiscounts(productDetailIds);
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    private List<String> splitIds(String ids) {
        if (ids == null || ids.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private double value(Double value) {
        return value == null ? 0D : value;
    }

    private double doubleValue(Object value) {
        return value == null ? 0D : ((Number) value).doubleValue();
    }

    private Map<String, Object> pageMap(List<Map<String, Object>> rows, ProductSearchRequest request) {
        List<Map<String, Object>> pageRows = slice(rows, offset(request), pageSize(request));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", pageRows);
        result.put("totalPages", (long) Math.ceil((double) rows.size() / pageSize(request)));
        result.put("currentPage", Math.max(request.getPage() - 1, 0));
        result.put("totalElements", (long) rows.size());
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
