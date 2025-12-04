package com.be.server.core.permitall.sanpham.service.impl;

import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.core.permitall.sanpham.dto.request.PMDanhSachSanPhamSearchRequest;
import com.be.server.core.permitall.sanpham.dto.request.PMSanPhamMoiSearchRequest;
import com.be.server.core.permitall.sanpham.dto.response.DotGiamGiaInfo;
import com.be.server.core.permitall.sanpham.dto.response.PMSanPhamMoiResponse;
import com.be.server.core.permitall.sanpham.repository.*;
import com.be.server.core.permitall.sanpham.service.PMSanPhamService;
import com.be.server.entity.DotGiamGia;
import com.be.server.entity.DotGiamGiaChiTietSanPham;
import com.be.server.entity.SanPhamChiTiet;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class PMSanPhamServiceImpl implements PMSanPhamService {

    private final PMSanPhamRepository pmsanphamRepository;

    private final PMSanPhamChiTietRepository pmsanphamChiTietRepository;

    private final PMDotGiamGiaChiTietSanPhamRepository pmdotGiamGiaChiTietSanPhamRepository;

    private final PMKichCoRepository pmkichcoRepository;

    private final PMMauSacRepository pmMauSacRepository;

    private final PMDotGiamGiaRepository pmDotGiamGiaRepository;

    @Override
    public ResponseObject<?> getAllSanPhamMoi(PMSanPhamMoiSearchRequest request) {
        Pageable pageable = Helper.createPageable(request, "created_date");
        Page<PMSanPhamMoiResponse> page = pmsanphamRepository.getSanPhamMoiResponsePage(pageable);

        Long currentTimestamp = System.currentTimeMillis();

        for (PMSanPhamMoiResponse dto : page.getContent()) {
            dto.setKichCo(pmkichcoRepository.getDistinctBySanPhamId(dto.getId()));
            dto.setMauSac(pmMauSacRepository.getBySanPhamId(dto.getId()));

            List<String> dsAnh = pmsanphamChiTietRepository.getAllAnhBySanPhamId(dto.getId());
            dto.setDsAnh(dsAnh);

            List<SanPhamChiTiet> chiTietList = pmsanphamChiTietRepository.findBySanPham(dto.getId());
            DotGiamGiaInfo maxGiamGiaInfo = null;

            for (SanPhamChiTiet chiTiet : chiTietList) {
                Optional<DotGiamGiaChiTietSanPham> giamGia =
                        pmdotGiamGiaChiTietSanPhamRepository.findHieuLucBySanPhamChiTietId(
                                chiTiet.getId(), currentTimestamp);

                if (giamGia.isPresent()) {
                    DotGiamGiaChiTietSanPham chiTietGiamGia = giamGia.get();
                    DotGiamGia dotGiamGia = chiTietGiamGia.getDotGiamGia();

                    DotGiamGiaInfo tmp = new DotGiamGiaInfo(
                            dotGiamGia.getTen(),
                            dotGiamGia.getPhanTramGiam(),
                            chiTietGiamGia.getGiaTruoc(),
                            chiTietGiamGia.getGiaSau(),
                            dotGiamGia.getNgayBatDau(),
                            dotGiamGia.getNgayKetThuc()
                    );
                    if (maxGiamGiaInfo == null || tmp.getPhanTramGiam() > maxGiamGiaInfo.getPhanTramGiam()) {
                        maxGiamGiaInfo = tmp;
                    }
                }
            }
            dto.setDotGiamGia(maxGiamGiaInfo);
        }

        return new ResponseObject<>(
                PageableObject.of(page),
                HttpStatus.OK,
                "Lấy danh sách sản phẩm mới thành công"
        );
    }

    private List<String> convertStringToList(String idString) {
        if (idString == null || idString.trim().isEmpty()) {
            return null; // Trả về null để khớp với điều kiện IS NULL trong HQL
        }
        return Arrays.stream(idString.split(","))
                .map(String::toString)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseObject<?> getDanhSachSanPham(PMDanhSachSanPhamSearchRequest request) {
//        Pageable pageable = Helper.createPageable(request, "created_date");
        // In your service method
        String sortBy = request.getSortBy();
        Sort sort = switch (sortBy) {
            case "createdAt_asc"  -> Sort.by("createdDate").ascending();
            case "createdAt_desc" -> Sort.by("createdDate").descending();
            case "ten_asc"        -> Sort.by("ten").ascending();
            case "ten_desc"       -> Sort.by("ten").descending();
            case "giaBan_asc", "giaBan_desc" -> Sort.unsorted(); // No sorting in Sort object for giaBan
            default               -> Sort.by("createdDate").descending();
        };

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);

        List<String> thuongHieuIdsList = convertStringToList(request.getThuongHieuIds());
        List<String> chatLieuIdsList = convertStringToList(request.getChatLieuIds());
        List<String> loaiDeIdsList = convertStringToList(request.getLoaiDeIds());
        List<String> danhMucIdsList = convertStringToList(request.getDanhMucIds());


        Page<PMSanPhamMoiResponse> page = pmsanphamRepository.getDanhSachSanPhamResponsePage(
                pageable,
                request,
                sortBy,
                thuongHieuIdsList,
                chatLieuIdsList,
                loaiDeIdsList,
                danhMucIdsList
        );


        Long currentTimestamp = System.currentTimeMillis();

        for (PMSanPhamMoiResponse dto : page.getContent()) {
            dto.setKichCo(pmkichcoRepository.getDistinctBySanPhamId(dto.getId()));
            dto.setMauSac(pmMauSacRepository.getBySanPhamId(dto.getId()));

            List<String> dsAnh = pmsanphamChiTietRepository.getAllAnhBySanPhamId(dto.getId());
            dto.setDsAnh(dsAnh);

            List<SanPhamChiTiet> chiTietList = pmsanphamChiTietRepository.findBySanPham(dto.getId());
            DotGiamGiaInfo maxGiamGiaInfo = null;

            for (SanPhamChiTiet chiTiet : chiTietList) {
                Optional<DotGiamGiaChiTietSanPham> giamGia =
                        pmdotGiamGiaChiTietSanPhamRepository.findHieuLucBySanPhamChiTietId(
                                chiTiet.getId(), currentTimestamp);

                if (giamGia.isPresent()) {
                    DotGiamGiaChiTietSanPham chiTietGiamGia = giamGia.get();
                    DotGiamGia dotGiamGia = chiTietGiamGia.getDotGiamGia();

                    DotGiamGiaInfo tmp = new DotGiamGiaInfo(
                            dotGiamGia.getTen(),
                            dotGiamGia.getPhanTramGiam(),
                            chiTietGiamGia.getGiaTruoc(),
                            chiTietGiamGia.getGiaSau(),
                            dotGiamGia.getNgayBatDau(),
                            dotGiamGia.getNgayKetThuc()
                    );
                    if (maxGiamGiaInfo == null || tmp.getPhanTramGiam() > maxGiamGiaInfo.getPhanTramGiam()) {
                        maxGiamGiaInfo = tmp;
                    }
                }
            }
            dto.setDotGiamGia(maxGiamGiaInfo);
        }

        return new ResponseObject<>(
                PageableObject.of(page),
                HttpStatus.OK,
                "Lấy danh sách sản phẩm mới thành công"
        );
    }

    @Override
    public ResponseObject<?> getAllSanPhamGiamGia(PMSanPhamMoiSearchRequest request) {
        Pageable pageable = Helper.createPageable(request, "created_date");
        Long currentTimestamp = System.currentTimeMillis();

        Page<PMSanPhamMoiResponse> page = pmsanphamRepository.getSanPhamGiamGiaResponsePage(currentTimestamp, pageable);

        for (PMSanPhamMoiResponse dto : page.getContent()) {
            dto.setKichCo(pmkichcoRepository.getDistinctBySanPhamId(dto.getId()));
            dto.setMauSac(pmMauSacRepository.getBySanPhamId(dto.getId()));
            dto.setDsAnh(pmsanphamChiTietRepository.getAllAnhBySanPhamId(dto.getId()));

            List<SanPhamChiTiet> chiTietList = pmsanphamChiTietRepository.findBySanPham(dto.getId());
            DotGiamGiaInfo maxGiamGiaInfo = null;

            for (SanPhamChiTiet chiTiet : chiTietList) {
                Optional<DotGiamGiaChiTietSanPham> giamGia =
                        pmdotGiamGiaChiTietSanPhamRepository.findHieuLucBySanPhamChiTietId(
                                chiTiet.getId(), currentTimestamp);

                if (giamGia.isPresent()) {
                    DotGiamGiaChiTietSanPham chiTietGiamGia = giamGia.get();
                    DotGiamGia dotGiamGia = chiTietGiamGia.getDotGiamGia();

                    DotGiamGiaInfo tmp = new DotGiamGiaInfo(
                            dotGiamGia.getTen(),
                            dotGiamGia.getPhanTramGiam(),
                            chiTietGiamGia.getGiaTruoc(),
                            chiTietGiamGia.getGiaSau(),
                            dotGiamGia.getNgayBatDau(),
                            dotGiamGia.getNgayKetThuc()
                    );
                    if (maxGiamGiaInfo == null || tmp.getPhanTramGiam() > maxGiamGiaInfo.getPhanTramGiam()) {
                        maxGiamGiaInfo = tmp;
                    }
                }
            }
            dto.setDotGiamGia(maxGiamGiaInfo);
        }

        return new ResponseObject<>(
                PageableObject.of(page),
                HttpStatus.OK,
                "Lấy danh sách sản phẩm giảm giá thành công"
        );
    }

}
