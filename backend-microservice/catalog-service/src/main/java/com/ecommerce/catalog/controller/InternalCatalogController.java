package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.constant.EntityStatus;
import com.ecommerce.catalog.entity.KichCo;
import com.ecommerce.catalog.entity.MauSac;
import com.ecommerce.catalog.entity.SanPham;
import com.ecommerce.catalog.entity.SanPhamChiTiet;
import com.ecommerce.catalog.repository.KichCoRepository;
import com.ecommerce.catalog.repository.MauSacRepository;
import com.ecommerce.catalog.repository.SanPhamChiTietRepository;
import com.ecommerce.catalog.repository.SanPhamRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/catalog")
public class InternalCatalogController {

    private final SanPhamRepository sanPhamRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final MauSacRepository mauSacRepository;
    private final KichCoRepository kichCoRepository;

    public InternalCatalogController(
            SanPhamRepository sanPhamRepository,
            SanPhamChiTietRepository sanPhamChiTietRepository,
            MauSacRepository mauSacRepository,
            KichCoRepository kichCoRepository
    ) {
        this.sanPhamRepository = sanPhamRepository;
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
        this.mauSacRepository = mauSacRepository;
        this.kichCoRepository = kichCoRepository;
    }

    @GetMapping("/products")
    public List<Map<String, Object>> getProducts() {
        return sanPhamRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE)
                .stream()
                .map(this::productMap)
                .toList();
    }

    @GetMapping("/products/{productId}/details")
    public List<Map<String, Object>> getProductDetails(@PathVariable String productId) {
        return sanPhamChiTietRepository.findBySanPhamIdAndStatusOrderByCreatedDateDesc(productId, EntityStatus.ACTIVE)
                .stream()
                .map(this::productDetailMap)
                .toList();
    }

    @GetMapping("/product-details")
    public List<Map<String, Object>> getProductDetailsByIds(@RequestParam List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return sanPhamChiTietRepository.findByIdIn(ids).stream().map(this::productDetailMap).toList();
    }

    @GetMapping("/product-details/search")
    public List<Map<String, Object>> searchProductDetails(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String idMauSac,
            @RequestParam(required = false) String idKichThuoc,
            @RequestParam(required = false) String idDanhMuc,
            @RequestParam(required = false) String idChatLieu,
            @RequestParam(required = false) String idThuongHieu,
            @RequestParam(required = false) String idLoaiDe,
            @RequestParam(required = false) String idSP,
            @RequestParam(required = false) Double priceMin,
            @RequestParam(required = false) Double priceMax
    ) {
        String normalizedQ = q == null ? "" : q.replace("%", "").toLowerCase();
        double giaMax = sanPhamChiTietRepository.findAll().stream()
                .map(SanPhamChiTiet::getGiaBan)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0D);
        return sanPhamChiTietRepository.findAll().stream()
                .filter(detail -> detail.getSoLuong() != null && detail.getSoLuong() > 0)
                .filter(detail -> status == null || status.isBlank() || String.valueOf(detail.getStatus() == null ? null : detail.getStatus().ordinal()).equals(status))
                .filter(detail -> normalizedQ.isBlank()
                        || contains(detail.getMa(), normalizedQ)
                        || (detail.getSanPham() != null && contains(detail.getSanPham().getTen(), normalizedQ)))
                .filter(detail -> idMauSac == null || idMauSac.isBlank() || (detail.getMauSac() != null && idMauSac.equals(detail.getMauSac().getId())))
                .filter(detail -> idKichThuoc == null || idKichThuoc.isBlank() || (detail.getKichCo() != null && idKichThuoc.equals(detail.getKichCo().getId())))
                .filter(detail -> idDanhMuc == null || idDanhMuc.isBlank() || (detail.getSanPham() != null && detail.getSanPham().getDanhMuc() != null && idDanhMuc.equals(detail.getSanPham().getDanhMuc().getId())))
                .filter(detail -> idChatLieu == null || idChatLieu.isBlank() || (detail.getSanPham() != null && detail.getSanPham().getChatLieu() != null && idChatLieu.equals(detail.getSanPham().getChatLieu().getId())))
                .filter(detail -> idThuongHieu == null || idThuongHieu.isBlank() || (detail.getSanPham() != null && detail.getSanPham().getThuongHieu() != null && idThuongHieu.equals(detail.getSanPham().getThuongHieu().getId())))
                .filter(detail -> idLoaiDe == null || idLoaiDe.isBlank() || (detail.getSanPham() != null && detail.getSanPham().getLoaiDe() != null && idLoaiDe.equals(detail.getSanPham().getLoaiDe().getId())))
                .filter(detail -> idSP == null || idSP.isBlank() || (detail.getSanPham() != null && idSP.equals(detail.getSanPham().getId())))
                .filter(detail -> priceMin == null || (detail.getGiaBan() != null && detail.getGiaBan() >= priceMin))
                .filter(detail -> priceMax == null || (detail.getGiaBan() != null && detail.getGiaBan() <= priceMax))
                .map(this::productDetailMap)
                .peek(row -> row.put("giaMax", giaMax))
                .toList();
    }

    @GetMapping("/product-details/{id}")
    public Map<String, Object> getProductDetail(@PathVariable String id) {
        List<Map<String, Object>> rows = getProductDetailsByIds(List.of(id));
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    @PostMapping("/product-details/{id}/stock/adjust")
    public void adjustStock(@PathVariable String id, @RequestParam int delta) {
        sanPhamChiTietRepository.findById(id).ifPresent(detail -> {
            detail.setSoLuong((detail.getSoLuong() == null ? 0 : detail.getSoLuong()) + delta);
            sanPhamChiTietRepository.save(detail);
        });
    }

    @GetMapping("/colors")
    public List<Map<String, Object>> getColors() {
        return mauSacRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE).stream().map(this::colorMap).toList();
    }

    @GetMapping("/sizes")
    public List<Map<String, Object>> getSizes() {
        return kichCoRepository.findByStatusOrderByCreatedDateDesc(EntityStatus.ACTIVE).stream().map(this::sizeMap).toList();
    }

    private Map<String, Object> productMap(SanPham product) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", product.getId());
        row.put("ma", product.getMa());
        row.put("ten", product.getTen());
        row.put("moTa", product.getMoTa());
        row.put("status", product.getStatus() == null ? null : product.getStatus().ordinal());
        return row;
    }

    private Map<String, Object> productDetailMap(SanPhamChiTiet detail) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", detail.getId());
        row.put("ma", detail.getMa());
        row.put("giaBan", detail.getGiaBan());
        row.put("anh", detail.getAnh());
        row.put("soLuong", detail.getSoLuong());
        row.put("status", detail.getStatus() == null ? null : detail.getStatus().ordinal());
        row.put("sanPhamId", detail.getSanPham() == null ? null : detail.getSanPham().getId());
        row.put("tenSanPham", detail.getSanPham() == null ? null : detail.getSanPham().getTen());
        row.put("ten", detail.getSanPham() == null ? null : detail.getSanPham().getTen());
        row.put("tenThuongHieu", detail.getSanPham() == null || detail.getSanPham().getThuongHieu() == null ? null : detail.getSanPham().getThuongHieu().getTen());
        row.put("tenLoaiDe", detail.getSanPham() == null || detail.getSanPham().getLoaiDe() == null ? null : detail.getSanPham().getLoaiDe().getTen());
        row.put("tenChatLieu", detail.getSanPham() == null || detail.getSanPham().getChatLieu() == null ? null : detail.getSanPham().getChatLieu().getTen());
        row.put("tenDanhMuc", detail.getSanPham() == null || detail.getSanPham().getDanhMuc() == null ? null : detail.getSanPham().getDanhMuc().getTen());
        row.put("kichCoId", detail.getKichCo() == null ? null : detail.getKichCo().getId());
        row.put("tenKichCo", detail.getKichCo() == null ? null : detail.getKichCo().getTen());
        row.put("kichThuoc", detail.getKichCo() == null ? null : detail.getKichCo().getTen());
        row.put("mauSacId", detail.getMauSac() == null ? null : detail.getMauSac().getId());
        row.put("tenMauSac", detail.getMauSac() == null ? null : detail.getMauSac().getTen());
        row.put("tenMau", detail.getMauSac() == null ? null : detail.getMauSac().getTen());
        row.put("mau", detail.getMauSac() == null ? null : detail.getMauSac().getMau());
        return row;
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }

    private Map<String, Object> colorMap(MauSac color) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", color.getId());
        row.put("ma", color.getMa());
        row.put("ten", color.getTen());
        row.put("mau", color.getMau());
        row.put("status", color.getStatus() == null ? null : color.getStatus().ordinal());
        return row;
    }

    private Map<String, Object> sizeMap(KichCo size) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", size.getId());
        row.put("ma", size.getMa());
        row.put("ten", size.getTen());
        row.put("status", size.getStatus() == null ? null : size.getStatus().ordinal());
        return row;
    }
}
