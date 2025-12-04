package com.be.server.core.admin.sanpham.model.request;

import com.be.server.core.common.base.PageableRequest;
import com.be.server.infrastructure.constant.EntityStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ADSanPhamSearchRequest extends PageableRequest {

   private String q; // Tên hoặc mã sản phẩm

   private String idSP; // ID sản phẩm, nếu cần tìm kiếm chính xác theo ID

   private String danhMucId; // ID của danh mục

   private String chatLieuId; // ID của chất liệu

   private String thuongHieuId; // ID của thương hiệu

   private String loaiDeId; // ID của loại đế

   private String status;

   private EntityStatus entityStatus;// Trạng thái sản phẩm (0: Ngừng hoạt động, 1: Hoạt động)
}