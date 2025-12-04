package com.be.server.core.admin.hoadon.service;

import com.be.server.core.admin.hoadon.model.response.ADHoaDonChiTietResponseDetail;
import com.be.server.core.admin.hoadon.repository.ADHoaDonChiTietRepository;
import com.be.server.core.admin.hoadon.repository.ADHoaDonRepository;
import com.be.server.entity.HoaDon;
import com.be.server.infrastructure.constant.EntityLoaiHoaDon;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PDFInvoiceService {

    private final ADHoaDonRepository hoaDonRepository;
    private final ADHoaDonChiTietRepository hoaDonChiTietRepository;

    public byte[] generateInvoicePDF(String maHoaDon) throws IOException {
        // Lấy thông tin hóa đơn
        Optional<HoaDon> hoaDonOpt = hoaDonRepository.findByMa(maHoaDon);
        if (hoaDonOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy hóa đơn với mã: " + maHoaDon);
        }

        HoaDon hoaDon = hoaDonOpt.get();

        // Chỉ in PDF cho hóa đơn OFFLINE
        if (hoaDon.getLoaiHoaDon() != EntityLoaiHoaDon.OFFLINE) {
            throw new RuntimeException("Chỉ có thể in PDF cho hóa đơn OFFLINE");
        }

        // Lấy chi tiết hóa đơn
        List<ADHoaDonChiTietResponseDetail> chiTietList = hoaDonChiTietRepository.getAllHoaDonChiTietResponse1(maHoaDon);

        if (chiTietList.isEmpty()) {
            throw new RuntimeException("Không tìm thấy chi tiết hóa đơn");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        pdfDocument.setDefaultPageSize(PageSize.A4);

        Document document = new Document(pdfDocument);
        document.setMargins(20, 20, 20, 20);

        // Tạo font hỗ trợ tiếng Việt
        PdfFont regularFont = PdfFontFactory.createFont(
                "C:/Windows/Fonts/arial.ttf",
                PdfEncodings.IDENTITY_H
        );

        PdfFont boldFont = PdfFontFactory.createFont(
                "C:/Windows/Fonts/arialbd.ttf",
                PdfEncodings.IDENTITY_H
        );


        document.setFont(regularFont);

        // Lấy thông tin đầu tiên từ chi tiết (vì tất cả có cùng thông tin hóa đơn)
        ADHoaDonChiTietResponseDetail firstDetail = chiTietList.get(0);

        // Header với thông tin cửa hàng
        createHeader(document, boldFont, regularFont, maHoaDon);

        // Thông tin hóa đơn
        createInvoiceInfo(document, firstDetail, regularFont, boldFont, maHoaDon);

        // Bảng sản phẩm
        createProductTable(document, chiTietList, regularFont, boldFont);

        // Tổng tiền
        createTotalSection(document, firstDetail, regularFont, boldFont);

        // Footer
        createFooter(document, regularFont, boldFont);

        document.close();
        return outputStream.toByteArray();
    }

    private void createHeader(Document document, PdfFont boldFont, PdfFont regularFont, String maHoaDon) throws IOException {
        // Tạo bảng header
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{2, 1}));
        headerTable.setWidth(UnitValue.createPercentValue(100));
        headerTable.setMarginBottom(20);

        // Cột trái - Thông tin cửa hàng
        Cell leftCell = new Cell();
        leftCell.add(new Paragraph("GLAMSOLE").setFont(boldFont).setFontSize(24))
                .add(new Paragraph("Email: glamsole@gmail.com").setFont(regularFont).setFontSize(10))
                .add(new Paragraph("Số điện thoại: 0123456789").setFont(regularFont).setFontSize(10))
                .add(new Paragraph("Địa chỉ: FPT POLYTECHNIC, Trịnh Văn Bô").setFont(regularFont).setFontSize(10))
                .setBorder(Border.NO_BORDER);

        // Cột phải - QR Code
        Cell rightCell = new Cell();

        try {
            // Tạo QR code
            BarcodeQRCode barcodeQRCode = new BarcodeQRCode(maHoaDon);

            // Tạo XObject và Image
            com.itextpdf.kernel.pdf.xobject.PdfFormXObject qrXObject = barcodeQRCode.createFormXObject(document.getPdfDocument());
            Image qrImage = new Image(qrXObject);
            qrImage.scaleToFit(80, 80);

            rightCell.add(qrImage)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER);
        } catch (Exception e) {
            // Fallback: Thêm text thay vì QR code nếu có lỗi
            rightCell.add(new Paragraph("QR: " + maHoaDon).setFont(regularFont).setFontSize(10))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER);
        }

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);

        // Tiêu đề hóa đơn
        Paragraph invoiceTitle = new Paragraph("HÓA ĐƠN BÁN HÀNG")
                .setFont(boldFont)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(invoiceTitle);
    }

    private void createInvoiceInfo(Document document, ADHoaDonChiTietResponseDetail detail,
                                   PdfFont regularFont, PdfFont boldFont, String maHoaDon) {

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();

        // Tạo bảng thông tin hóa đơn
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        infoTable.setWidth(UnitValue.createPercentValue(100));
        infoTable.setMarginBottom(20);

        // Cột trái
        Cell leftCell = new Cell();
        leftCell.add(new Paragraph("Khách hàng: " + (detail.getTenKhachHang() != null ? detail.getTenKhachHang() : "Khách lẻ")).setFont(regularFont))
                .add(new Paragraph("Số: " + (detail.getSdtKH() != null ? detail.getSdtKH() : "N/A")).setFont(regularFont))
                .setBorder(Border.NO_BORDER);

        // Cột phải
        Cell rightCell = new Cell();
        rightCell.add(new Paragraph("#" + maHoaDon).setFont(boldFont))
                .add(new Paragraph("Ngày: " + now.format(dateFormatter)).setFont(regularFont))
                .add(new Paragraph("Lúc: " + now.format(timeFormatter)).setFont(regularFont))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);

        infoTable.addCell(leftCell);
        infoTable.addCell(rightCell);
        document.add(infoTable);

        // Tiêu đề danh sách sản phẩm
        Paragraph productTitle = new Paragraph("DANH SÁCH SẢN PHẨM")
                .setFont(boldFont)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15);
        document.add(productTitle);
    }

    private void createProductTable(Document document, List<ADHoaDonChiTietResponseDetail> chiTietList,
                                    PdfFont regularFont, PdfFont boldFont) {

        // Tạo bảng sản phẩm
        Table productTable = new Table(UnitValue.createPercentArray(new float[]{0.5f, 2.5f, 1f, 1f, 0.8f, 1.5f, 1.5f}));
        productTable.setWidth(UnitValue.createPercentValue(100));
        productTable.setMarginBottom(20);

        // Header
        String[] headers = {"#", "Sản phẩm", "Màu", "Size", "SL", "Giá", "Thành tiền"};
        for (String header : headers) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(header).setFont(boldFont).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setPadding(5);
            productTable.addHeaderCell(headerCell);
        }

        // Dữ liệu sản phẩm
        DecimalFormat currencyFormat = new DecimalFormat("#,###");
        int index = 1;
        double totalAmount = 0;

        for (ADHoaDonChiTietResponseDetail detail : chiTietList) {
            productTable.addCell(new Cell().add(new Paragraph(String.valueOf(index++)).setFont(regularFont).setFontSize(9))
                    .setTextAlignment(TextAlignment.CENTER).setPadding(3));

            productTable.addCell(new Cell().add(new Paragraph(detail.getTenSanPham()).setFont(regularFont).setFontSize(9))
                    .setPadding(3));

            productTable.addCell(new Cell().add(new Paragraph(detail.getMauSac() != null ? detail.getMauSac() : "N/A").setFont(regularFont).setFontSize(9))
                    .setTextAlignment(TextAlignment.CENTER).setPadding(3));

            productTable.addCell(new Cell().add(new Paragraph(detail.getSize() != null ? detail.getSize() : "N/A").setFont(regularFont).setFontSize(9))
                    .setTextAlignment(TextAlignment.CENTER).setPadding(3));

            productTable.addCell(new Cell().add(new Paragraph(String.valueOf(detail.getSoLuong())).setFont(regularFont).setFontSize(9))
                    .setTextAlignment(TextAlignment.CENTER).setPadding(3));

            productTable.addCell(new Cell().add(new Paragraph(currencyFormat.format(detail.getGiaBan()) + " VND").setFont(regularFont).setFontSize(9))
                    .setTextAlignment(TextAlignment.RIGHT).setPadding(3));

            double thanhTien = detail.getThanhTienSP() != null ? detail.getThanhTienSP() : 0;
            totalAmount += thanhTien;

            productTable.addCell(new Cell().add(new Paragraph(currencyFormat.format(thanhTien) + " VND").setFont(regularFont).setFontSize(9))
                    .setTextAlignment(TextAlignment.RIGHT).setPadding(3));
        }

        document.add(productTable);
    }

    private void createTotalSection(Document document, ADHoaDonChiTietResponseDetail detail,
                                    PdfFont regularFont, PdfFont boldFont) {
        DecimalFormat currencyFormat = new DecimalFormat("#,###");

        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        totalTable.setWidth(UnitValue.createPercentValue(100));

        Cell leftCell = new Cell();
        leftCell.add(new Paragraph("Tổng tiền:").setFont(regularFont))
                .add(new Paragraph("Phí vận chuyển:").setFont(regularFont))
                .add(new Paragraph("Giảm giá:").setFont(regularFont))
                .add(new Paragraph("Thanh toán:").setFont(regularFont))
                .setBorder(Border.NO_BORDER);

        Cell rightCell = new Cell();
        rightCell.add(new Paragraph(currencyFormat.format(detail.getTongTien()) + " VND").setFont(regularFont).setTextAlignment(TextAlignment.RIGHT))
                .add(new Paragraph(currencyFormat.format(detail.getPhiVanChuyen() != null ? detail.getPhiVanChuyen() : 0) + " VND").setFont(regularFont).setTextAlignment(TextAlignment.RIGHT))
                .add(new Paragraph(currencyFormat.format(detail.getTongTienSauGiam() != null ? (detail.getThanhTien() - detail.getTongTienSauGiam()) : 0) + " VND").setFont(regularFont).setTextAlignment(TextAlignment.RIGHT))
                .add(new Paragraph(currencyFormat.format(detail.getTongTienSauGiam() != null ? (detail.getTongTien() - (detail.getThanhTien() - detail.getTongTienSauGiam())) : 0) + " VND").setFont(regularFont).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER);

        totalTable.addCell(leftCell);
        totalTable.addCell(rightCell);
        document.add(totalTable);
    }

    private void createFooter(Document document, PdfFont regularFont, PdfFont boldFont) {
        Paragraph footer = new Paragraph("Cảm ơn quý khách đã mua hàng!").setFont(regularFont).setTextAlignment(TextAlignment.CENTER);
        document.add(footer);
    }
}