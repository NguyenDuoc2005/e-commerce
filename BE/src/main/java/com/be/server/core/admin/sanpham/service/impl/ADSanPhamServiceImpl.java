package com.be.server.core.admin.sanpham.service.impl;

import com.be.server.core.admin.SanPhamChiTiet.repository.ADSanPhamChiTietRepository;
import com.be.server.core.admin.sanpham.model.request.ADSanPhamRequest;
import com.be.server.core.admin.sanpham.model.request.ADSanPhamSearchRequest;
import com.be.server.core.admin.sanpham.model.response.ADSanPhamResponse;
import com.be.server.core.admin.sanpham.repository.ADSanPhamRepository;
import com.be.server.core.admin.sanpham.service.ADSanPhamService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.ChatLieu;
import com.be.server.entity.DanhMuc;
import com.be.server.entity.LoaiDe;
import com.be.server.entity.SanPham;
import com.be.server.entity.SanPhamChiTiet;
import com.be.server.entity.ThuongHieu;
import com.be.server.entity.XuatSu;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.repository.ChatLieuRepository;
import com.be.server.repository.DanhMucRepository;
import com.be.server.repository.LoaiDeRepository;
import com.be.server.repository.ThuongHieuRepository;
import com.be.server.repository.XuatSuRepository;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ADSanPhamServiceImpl implements ADSanPhamService {

    private final ADSanPhamRepository adSanPhamRepository;

    private final ADSanPhamChiTietRepository adSanPhamChiTietRepository;

    private final ThuongHieuRepository thuongHieuRepository;

    private final DanhMucRepository danhMucRepository;

    private final LoaiDeRepository loaiDeRepository;

    private final XuatSuRepository xuatSuRepository;

    private final ChatLieuRepository chatLieuRepository;

    @Override
    public ResponseObject<?> getAll(ADSanPhamSearchRequest id) {
        Pageable pageable = Helper.createPageable(id, "created_date");

        if (id.getStatus() != null && !id.getStatus().isEmpty()) {
            if (id.getStatus().equals("0")) {
                id.setEntityStatus(EntityStatus.INACTIVE);
            } else {
                id.setEntityStatus(EntityStatus.ACTIVE);
            }
        }
        Page<ADSanPhamResponse> page = adSanPhamRepository.getAllSanPhamByFilter(pageable, id);

        return new ResponseObject<>(
                PageableObject.of(page),
                HttpStatus.OK,
                "Lấy danh sách sản phẩm thành công"
        );
    }

    @Override
    public ResponseObject<?> getSanPhamById(String id) {
        return adSanPhamRepository.getAllSanPhamID(id)
                .map(technology -> new ResponseObject<>(technology, HttpStatus.OK, "sản phẩm thành công"))
                .orElseGet(() -> new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
    }

    @Override
    public ResponseObject<?> modifySanPham(ADSanPhamRequest request) {
        if (request.getId() != null && StringUtils.hasLength(request.getId())) {
            Optional<SanPham> exsitingMemberOpt = adSanPhamRepository.findById(request.getId());

            if (exsitingMemberOpt.isPresent()) {

                SanPham sanPham = exsitingMemberOpt.get();

                sanPham.setTen(request.getTen());

                sanPham.setMoTa(request.getMoTa());

                if (request.getIdThuongHieu() != null) {
                    Optional<ThuongHieu> thuongHieuOptional = thuongHieuRepository.findById(request.getIdThuongHieu());
                    if (thuongHieuOptional.isPresent()) {
                        ThuongHieu thuongHieu = thuongHieuOptional.get();
                        sanPham.setThuongHieu(thuongHieu);
                    } else {
                        // Xử lý trường hợp không tìm thấy ThuongHieu
                    }
                }

                if (request.getIdDanhMuc() != null) {
                    Optional<DanhMuc> danhMucOptional = danhMucRepository.findById(request.getIdDanhMuc());
                    if (danhMucOptional.isPresent()) {
                        DanhMuc danhMuc = danhMucOptional.get();
                        sanPham.setDanhMuc(danhMuc);
                    } else {
                        // Xử lý trường hợp không tìm thấy DanhMuc
                    }
                }

                if (request.getIdLoaiDe() != null) {
                    Optional<LoaiDe> loaiDeOptional = loaiDeRepository.findById(request.getIdLoaiDe());
                    if (loaiDeOptional.isPresent()) {
                        LoaiDe loaiDe = loaiDeOptional.get();
                        sanPham.setLoaiDe(loaiDe);
                    } else {
                        // Xử lý trường hợp không tìm thấy LoaiDe
                    }
                }

                if (request.getIdXuatXu() != null) {
                    Optional<XuatSu> xuatSuOptional = xuatSuRepository.findById(request.getIdXuatXu());
                    if (xuatSuOptional.isPresent()) {
                        XuatSu xuatSu = xuatSuOptional.get();
                        sanPham.setXuatSu(xuatSu);
                    } else {
                        // Xử lý trường hợp không tìm thấy XuatSu
                    }
                }

                if (request.getIdChatLieu() != null) {
                    Optional<ChatLieu> xuatSuOptional = chatLieuRepository.findById(request.getIdChatLieu());
                    if (xuatSuOptional.isPresent()) {
                        ChatLieu xuatSu = xuatSuOptional.get();
                        sanPham.setChatLieu(xuatSu);
                    } else {
                        // Xử lý trường hợp không tìm thấy XuatSu
                    }
                }

                adSanPhamRepository.save(sanPham);

                return new ResponseObject<>(sanPham, HttpStatus.OK, "Cập nhật size thành công");
            }
        }

        SanPham newSanPham = new SanPham();

        newSanPham.setTen(request.getTen());

        newSanPham.setMoTa(request.getMoTa());

        if (request.getIdThuongHieu() != null) {
            Optional<ThuongHieu> thuongHieuOptional = thuongHieuRepository.findById(request.getIdThuongHieu());
            if (thuongHieuOptional.isPresent()) {
                ThuongHieu thuongHieu = thuongHieuOptional.get();
                newSanPham.setThuongHieu(thuongHieu);
            } else {
                // Xử lý trường hợp không tìm thấy ThuongHieu
            }
        }

        if (request.getIdDanhMuc() != null) {
            Optional<DanhMuc> danhMucOptional = danhMucRepository.findById(request.getIdDanhMuc());
            if (danhMucOptional.isPresent()) {
                DanhMuc danhMuc = danhMucOptional.get();
                newSanPham.setDanhMuc(danhMuc);
            } else {
                // Xử lý trường hợp không tìm thấy DanhMuc
            }
        }

        if (request.getIdLoaiDe() != null) {
            Optional<LoaiDe> loaiDeOptional = loaiDeRepository.findById(request.getIdLoaiDe());
            if (loaiDeOptional.isPresent()) {
                LoaiDe loaiDe = loaiDeOptional.get();
                newSanPham.setLoaiDe(loaiDe);
            } else {
                // Xử lý trường hợp không tìm thấy LoaiDe
            }
        }

        if (request.getIdXuatXu() != null) {
            Optional<XuatSu> xuatSuOptional = xuatSuRepository.findById(request.getIdXuatXu());
            if (xuatSuOptional.isPresent()) {
                XuatSu xuatSu = xuatSuOptional.get();
                newSanPham.setXuatSu(xuatSu);
            } else {
                // Xử lý trường hợp không tìm thấy XuatSu
            }
        }

        if (request.getIdChatLieu() != null) {
            Optional<ChatLieu> xuatSuOptional = chatLieuRepository.findById(request.getIdChatLieu());
            if (xuatSuOptional.isPresent()) {
                ChatLieu xuatSu = xuatSuOptional.get();
                newSanPham.setChatLieu(xuatSu);
            } else {
                // Xử lý trường hợp không tìm thấy XuatSu
            }
        }


        newSanPham.setStatus(EntityStatus.ACTIVE);

        adSanPhamRepository.save(newSanPham);

        return new ResponseObject<>(newSanPham, HttpStatus.CREATED, "Tạo sản phẩm thành công");
    }

    @Override
    public ResponseObject<?> changeSanPhamStatus(String id) {
        Optional<SanPham> optional = adSanPhamRepository.findById(id);

        if (optional.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm");
        }

        SanPham sanPham = optional.get();

        // Đảo trạng thái
        EntityStatus newStatus = sanPham.getStatus() == EntityStatus.ACTIVE
                ? EntityStatus.INACTIVE
                : EntityStatus.ACTIVE;

        sanPham.setStatus(newStatus);
        adSanPhamRepository.save(sanPham);

        // Lấy danh sách ID chi tiết sản phẩm liên quan
        List<String> sanPhamChiTietIds = adSanPhamChiTietRepository.checkIdSanPhamCT(id);

        for (String spctId : sanPhamChiTietIds) {
            Optional<SanPhamChiTiet> spctOpt = adSanPhamChiTietRepository.findById(spctId);

            if (spctOpt.isPresent()) {
                SanPhamChiTiet spct = spctOpt.get();
                spct.setStatus(newStatus);
                adSanPhamChiTietRepository.save(spct);
            }
        }

        return new ResponseObject<>(null, HttpStatus.OK, "Thay đổi trạng thái thành công");
    }



    @Override
    public ResponseObject<?> getListThuongHieu() {

        return new ResponseObject<>(adSanPhamRepository.getListThuongHieu(), HttpStatus.OK, "Lấy thành công danh sách thương hiệu");
    }

    @Override
    public ResponseObject<?> getXuatXu() {
        return new ResponseObject<>(adSanPhamRepository.getListXuatXu(), HttpStatus.OK, "Lấy thành công danh sách thương hiệu");
    }

    @Override
    public ResponseObject<?> getListLoaiDe() {
        return new ResponseObject<>(adSanPhamRepository.getLoaiDe(), HttpStatus.OK, "Lấy thành công danh sách thương hiệu");
    }

    @Override
    public ResponseObject<?> getListDanhMuc() {
        return new ResponseObject<>(adSanPhamRepository.getListDanhMuc(), HttpStatus.OK, "Lấy thành công danh sách thương hiệu");
    }

    @Override
    public ResponseObject<?> getListSize() {
        return new ResponseObject<>(adSanPhamRepository.getListSize(), HttpStatus.OK, "Lấy thành công danh sách size");
    }

    @Override
    public ResponseObject<?> getListMau() {
        return new ResponseObject<>(adSanPhamRepository.getListMau(), HttpStatus.OK, "Lấy thành công danh sách màu");
    }

    @Override
    public ResponseObject<?> getListChetLieu() {
        return new ResponseObject<>(adSanPhamRepository.getListChatLieu(), HttpStatus.OK, "Lấy thành công danh sách thương hiệu");
    }

}
