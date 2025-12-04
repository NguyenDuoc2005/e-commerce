package com.be.server.core.admin.banhang.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class AvailableVouchersResponse {

    private List<PhieuGiamGiaDTO> availableVouchers; // Danh sách phiếu giảm giá có thể áp dụng
    private PhieuGiamGiaDTO bestVoucher; // Phiếu tốt nhất hiện tại
    private BetterVoucherInfo betterVoucher; // Thông tin phiếu tốt hơn (nếu có)

    @Getter
    @Setter
    public static class PhieuGiamGiaDTO {
        private String id;
        private String ma;
        private String ten;
        private Double phanTramGiam;
        private Double giaTriGiamThucTe;
        private Integer soLuongPhieu;
        private Date ngayBatDau;
        private Date ngayKetThuc;
        private Double dieuKien;
        private Double giaGiam;
        private Boolean loaiGiam;
        private Boolean kieuGiam;
    }

    @Getter
    @Setter
    public static class BetterVoucherInfo {
        private String ma;
        private Double giaTriGiamThucTe;
        private Double amountNeeded; // Số tiền cần mua thêm
    }

}
