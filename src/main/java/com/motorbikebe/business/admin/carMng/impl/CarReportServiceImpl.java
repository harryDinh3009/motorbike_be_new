package com.motorbikebe.business.admin.carMng.impl;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.motorbikebe.business.admin.carMng.service.CarReportService;
import com.motorbikebe.common.ApiStatus;
import com.motorbikebe.config.exception.RestApiException;
import com.motorbikebe.dto.business.admin.carMng.AvailableCarReportRequestDTO;
import com.motorbikebe.dto.business.admin.carMng.CarDTO;
import com.motorbikebe.dto.business.admin.carMng.CarSearchDTO;
import com.motorbikebe.entity.domain.BranchEntity;
import com.motorbikebe.repository.business.admin.BranchRepository;
import com.motorbikebe.repository.business.admin.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarReportServiceImpl implements CarReportService {

    private static final String STORE_NAME = "CỬA HÀNG CHO THUÊ XE MÁY MOTOGO";
    private static final String STORE_ADDRESS = "Địa chỉ: Tổ 1, Thôn Cầu Mè, Phương Thiện, Hà Giang, Việt Nam";
    private static final String STORE_PHONE = "SDT: 0859963204";

    private final CarRepository carRepository;
    private final BranchRepository branchRepository;

    @Override
    public byte[] exportAvailableCarsReport(AvailableCarReportRequestDTO request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new RestApiException(ApiStatus.BAD_REQUEST);
        }

        CarSearchDTO searchDTO = new CarSearchDTO();
        searchDTO.setStartDate(request.getStartDate());
        searchDTO.setEndDate(request.getEndDate());
        searchDTO.setBranchId(request.getBranchId());
        searchDTO.setModelName(request.getModelName());
        searchDTO.setCarType(request.getCarType());

        List<CarDTO> availableCars = carRepository.findAvailableCarsForReport(searchDTO);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());
            document.setMargins(20, 30, 40, 30);

            PdfFont font;
            PdfFont fontBold;
            try (InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/times.ttf");
                 InputStream fontBoldStream = getClass().getClassLoader().getResourceAsStream("fonts/timesbd.ttf")) {
                if (fontStream == null || fontBoldStream == null) {
                    throw new FileNotFoundException("Font files not found in resources/fonts/");
                }
                font = PdfFontFactory.createFont(fontStream.readAllBytes(), com.itextpdf.io.font.PdfEncodings.IDENTITY_H);
                fontBold = PdfFontFactory.createFont(fontBoldStream.readAllBytes(), com.itextpdf.io.font.PdfEncodings.IDENTITY_H);
            }

            document.add(new Paragraph("MOTOGO")
                    .setFont(fontBold)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(0));
            document.add(new Paragraph(STORE_NAME).setFont(fontBold).setFontSize(12).setMarginTop(0).setMarginBottom(0));
            document.add(new Paragraph(STORE_ADDRESS).setFont(font).setFontSize(11).setMarginTop(0).setMarginBottom(0));
            document.add(new Paragraph(STORE_PHONE).setFont(font).setFontSize(11).setMarginTop(0).setMarginBottom(10));

            document.add(new Paragraph("BÁO CÁO XE KHẢ DỤNG CHO THUÊ")
                    .setFont(fontBold)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5));

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String dateRange = String.format("Từ ngày %s tới ngày %s",
                    dateFormat.format(request.getStartDate()),
                    dateFormat.format(request.getEndDate()));
            document.add(new Paragraph(dateRange)
                    .setFont(font)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5));

            String branchLabel = resolveBranchName(request.getBranchId());
            document.add(new Paragraph("Chi nhánh: " + branchLabel)
                    .setFont(font)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15));

            float[] columnWidths = {40f, 120f, 110f, 150f, 100f, 110f, 90f, 90f};
            Table table = new Table(columnWidths);
            table.setWidth(UnitValue.createPercentValue(100));

            addHeaderCell(table, "STT", font, fontBold);
            addHeaderCell(table, "Mẫu xe", font, fontBold);
            addHeaderCell(table, "Biển số", font, fontBold);
            addHeaderCell(table, "Chi nhánh", font, fontBold);
            addHeaderCell(table, "Loại xe", font, fontBold);
            addHeaderCell(table, "Tình trạng", font, fontBold);
            addHeaderCell(table, "Giá ngày", font, fontBold);
            addHeaderCell(table, "Giá giờ", font, fontBold);

            NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

            if (availableCars.isEmpty()) {
                Cell emptyCell = new Cell(1, 8)
                        .add(new Paragraph("Không có dữ liệu").setFont(font))
                        .setTextAlignment(TextAlignment.CENTER);
                table.addCell(emptyCell);
            } else {
                int index = 1;
                for (CarDTO car : availableCars) {
                    table.addCell(createBodyCell(String.valueOf(index++), font));
                    table.addCell(createBodyCell(defaultString(car.getModel()), font));
                    table.addCell(createBodyCell(defaultString(car.getLicensePlate()), font));
                    table.addCell(createBodyCell(defaultString(car.getBranchName()), font));
                    table.addCell(createBodyCell(defaultString(car.getCarType()), font));
                    table.addCell(createBodyCell(defaultString(car.getCondition()), font));
                    table.addCell(createBodyCell(formatCurrency(car.getDailyPrice(), currencyFormat), font));
                    table.addCell(createBodyCell(formatCurrency(car.getHourlyPrice(), currencyFormat), font));
                }
            }

            document.add(table);

            document.add(new Paragraph("\n\n"));
            Table signTable = new Table(new float[]{1, 1});
            signTable.setWidth(UnitValue.createPercentValue(100));
            signTable.addCell(createSignCell("Bên cho thuê\n(ký ghi rõ họ tên)", fontBold));
            signTable.addCell(createSignCell("Người lập báo cáo\n(ký ghi rõ họ tên)", fontBold));
            document.add(signTable);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error exporting available car report", e);
            throw new RestApiException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String resolveBranchName(String branchId) {
        if (StringUtils.isBlank(branchId)) {
            return "Tất cả chi nhánh";
        }
        Optional<BranchEntity> branchOpt = branchRepository.findById(branchId);
        return branchOpt.map(BranchEntity::getName).orElse("Không xác định");
    }

    private void addHeaderCell(Table table, String text, PdfFont font, PdfFont fontBold) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFont(fontBold).setFontSize(11))
                .setTextAlignment(TextAlignment.CENTER);
        table.addCell(cell);
    }

    private Cell createBodyCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(10))
                .setTextAlignment(TextAlignment.LEFT);
    }

    private Cell createSignCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(11).setTextAlignment(TextAlignment.CENTER))
                .setBorder(null);
    }

    private String defaultString(String value) {
        return value != null ? value : "";
    }

    private String formatCurrency(BigDecimal value, NumberFormat format) {
        if (value == null) {
            return "";
        }
        return format.format(value);
    }
}

