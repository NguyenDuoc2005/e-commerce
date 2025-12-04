package com.be.server.core.admin.hoadon.service;

import com.be.server.core.admin.hoadon.model.response.ADHoaDonChiTietResponseDetail;
import com.be.server.core.admin.hoadon.repository.ADHoaDonChiTietRepository;
import com.be.server.core.admin.hoadon.repository.ADHoaDonRepository;
import com.be.server.entity.HoaDon;
import com.be.server.infrastructure.constant.EntityLoaiHoaDon;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.font.PdfEncodings;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PDFDeliveryService {

    private final ADHoaDonRepository hoaDonRepository;
    private final ADHoaDonChiTietRepository hoaDonChiTietRepository;

    public byte[] generateDeliveryPDF(String maHoaDon) throws IOException {
        // Lấy thông tin hóa đơn
        Optional<HoaDon> hoaDonOpt = hoaDonRepository.findByMa(maHoaDon);
        if (hoaDonOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy hóa đơn với mã: " + maHoaDon);
        }

        HoaDon hoaDon = hoaDonOpt.get();

        // Chỉ in PDF cho hóa đơn ONLINE (giao hàng)
        if (hoaDon.getLoaiHoaDon() == EntityLoaiHoaDon.OFFLINE) {
            throw new RuntimeException("Chỉ có thể in phiếu giao hàng cho hóa đơn ONLINE");
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

        // Lấy thông tin đầu tiên từ chi tiết
        ADHoaDonChiTietResponseDetail firstDetail = chiTietList.get(0);

        // Header với thông tin công ty và QR code
        createDeliveryHeader(document, boldFont, regularFont, maHoaDon);

        // Thông tin người gửi và người nhận
        createDeliveryInfo(document, firstDetail, regularFont, boldFont);

        // Danh sách sản phẩm
        createProductList(document, chiTietList, regularFont, boldFont);

        // Thông tin thu tiền và chữ ký
        createPaymentAndSignature(document, firstDetail, regularFont, boldFont);

        document.close();
        return outputStream.toByteArray();
    }

    private void createDeliveryHeader(Document document, PdfFont boldFont, PdfFont regularFont, String maHoaDon) throws IOException {
        // Tạo bảng header
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{2, 1}));
        headerTable.setWidth(UnitValue.createPercentValue(100));
        headerTable.setMarginBottom(20);

        // Cột trái - Logo và thông tin công ty
        Cell leftCell = new Cell();
        leftCell.add(new Paragraph("GLAMSOLE").setFont(boldFont).setFontSize(28).setFontColor(ColorConstants.BLACK))
                .add(new Paragraph("GHN Express").setFont(boldFont).setFontSize(16).setFontColor(ColorConstants.ORANGE))
                .setBorder(Border.NO_BORDER);

        // Cột phải - QR Code
        Cell rightCell = new Cell();
        try {
            BarcodeQRCode barcodeQRCode = new BarcodeQRCode(maHoaDon);
            com.itextpdf.kernel.pdf.xobject.PdfFormXObject qrXObject = barcodeQRCode.createFormXObject(document.getPdfDocument());
            Image qrImage = new Image(qrXObject);
            qrImage.scaleToFit(80, 80);

            rightCell.add(qrImage)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT);
        } catch (Exception e) {
            rightCell.add(new Paragraph("QR: " + maHoaDon).setFont(regularFont).setFontSize(10))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT);
        }

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);
    }

    private void createDeliveryInfo(Document document, ADHoaDonChiTietResponseDetail detail,
                                    PdfFont regularFont, PdfFont boldFont) {

        // Thông tin người gửi
        Paragraph senderTitle = new Paragraph("Từ: FPT POLYTECHNIC, Trịnh Văn Bô")
                .setFont(regularFont)
                .setFontSize(12)
                .setMarginBottom(5);
        document.add(senderTitle);

        Paragraph senderPhone = new Paragraph("Sdt: 0833486527")
                .setFont(regularFont)
                .setFontSize(12)
                .setMarginBottom(15);
        document.add(senderPhone);

        // Đường kẻ phân cách
        document.add(new Paragraph("_".repeat(80))
                .setFont(regularFont)
                .setFontSize(12)
                .setMarginBottom(10));

        // Thông tin người nhận
        String tenKhachHang = detail.getTenKhachHang() != null ? detail.getTenKhachHang() : "Khách hàng";
        String sdtKH = detail.getSdtKH() != null ? detail.getSdtKH() : "N/A";
        String diaChiGiaoHang = detail.getDiaChi() != null ? detail.getDiaChi() : "N/A";

        Paragraph receiverInfo = new Paragraph("Đến: " + diaChiGiaoHang)
                .setFont(regularFont)
                .setFontSize(12)
                .setMarginBottom(5);
        document.add(receiverInfo);

        Paragraph receiverName = new Paragraph("Tên: " + tenKhachHang)
                .setFont(regularFont)
                .setFontSize(12)
                .setMarginBottom(5);
        document.add(receiverName);

        Paragraph receiverPhone = new Paragraph("Sdt: " + sdtKH)
                .setFont(regularFont)
                .setFontSize(12)
                .setMarginBottom(15);
        document.add(receiverPhone);

        // Đường kẻ phân cách
        document.add(new Paragraph("_".repeat(80))
                .setFont(regularFont)
                .setFontSize(12)
                .setMarginBottom(10));
    }

    private void createProductList(Document document, List<ADHoaDonChiTietResponseDetail> chiTietList,
                                   PdfFont regularFont, PdfFont boldFont) {

        // Tổng số lượng sản phẩm
        int totalQuantity = chiTietList.stream().mapToInt(ADHoaDonChiTietResponseDetail::getSoLuong).sum();

        Paragraph productHeader = new Paragraph("Nội dung hàng (Tổng SL sản phẩm: " + totalQuantity + "):")
                .setFont(regularFont)
                .setFontSize(12)
                .setMarginBottom(10);
        document.add(productHeader);

        // Danh sách sản phẩm
        for (ADHoaDonChiTietResponseDetail detail : chiTietList) {
            String productInfo = String.format("+ %s [%s]     SL: %d",
                    detail.getTenSanPham(),
                    detail.getMauSac() != null ? detail.getMauSac() : "N/A",
                    detail.getSoLuong());

            Paragraph productLine = new Paragraph(productInfo)
                    .setFont(regularFont)
                    .setFontSize(11)
                    .setMarginBottom(5);
            document.add(productLine);
        }

        // Đường kẻ phân cách
        document.add(new Paragraph("_".repeat(80))
                .setFont(regularFont)
                .setFontSize(12)
                .setMarginTop(10)
                .setMarginBottom(10));
    }

    private void createPaymentAndSignature(Document document, ADHoaDonChiTietResponseDetail detail,
                                           PdfFont regularFont, PdfFont boldFont) {

        DecimalFormat currencyFormat = new DecimalFormat("#,###");

        // Tạo bảng thu tiền và chữ ký
        Table paymentTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        paymentTable.setWidth(UnitValue.createPercentValue(100));
        paymentTable.setMarginBottom(20);

        // Cột trái - Thu tiền
        Cell leftCell = new Cell();
        Double tongTien = detail.getTongTienSauGiam() != null ? detail.getTongTienSauGiam() : 0.0;

        leftCell.add(new Paragraph("Tổng thu người nhận:").setFont(regularFont).setFontSize(12))
                .add(new Paragraph(currencyFormat.format(tongTien <=0 ? 0: tongTien)  + " VND").setFont(boldFont).setFontSize(14))
                .setBorder(Border.NO_BORDER);

        // Cột phải - Chữ ký
        Cell rightCell = new Cell();
        rightCell.add(new Paragraph("Chữ ký người nhận").setFont(regularFont).setFontSize(12))
                .add(new Paragraph("(Xác nhận hàng nguyên vẹn)").setFont(regularFont).setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);

        paymentTable.addCell(leftCell);
        paymentTable.addCell(rightCell);
        document.add(paymentTable);

        // Thêm khoảng trống cho chữ ký
        document.add(new Paragraph(" ").setMarginBottom(40));

        // Thông tin thời gian tạo
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        Paragraph timestamp = new Paragraph("Thời gian tạo: " + now.format(formatter))
                .setFont(regularFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(timestamp);
    }
}