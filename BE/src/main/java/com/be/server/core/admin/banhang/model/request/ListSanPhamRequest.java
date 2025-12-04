package com.be.server.core.admin.banhang.model.request;

import com.be.server.core.common.base.PageableRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListSanPhamRequest extends PageableRequest {
    private String q; // Từ khóa tìm kiếm
    private String status; // Trạng thái sản phẩm
    private String idMauSac; // ID màu sắc
    private String idKichThuoc; // ID kích thước
    private String idDanhMuc; // ID danh mục
    private String idChatLieu; // ID chất liệu
    private String idThuongHieu; // ID thương hiệu
    private String idLoaiDe; // ID loại đế
}