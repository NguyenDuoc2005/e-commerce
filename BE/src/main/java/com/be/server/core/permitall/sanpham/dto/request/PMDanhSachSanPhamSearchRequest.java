package com.be.server.core.permitall.sanpham.dto.request;

import com.be.server.core.common.base.PageableRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PMDanhSachSanPhamSearchRequest extends PageableRequest {

    private String q;
    private Double giaMin;
    private Double giaMax;
    private String thuongHieuIds;
    private String chatLieuIds;
    private String loaiDeIds;
    private String danhMucIds;
    private String sortBy;

}