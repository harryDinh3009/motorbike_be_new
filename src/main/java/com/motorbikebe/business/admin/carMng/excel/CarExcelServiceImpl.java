package com.motorbikebe.business.admin.carMng.excel;

import com.motorbikebe.business.admin.carMng.service.CarModelService;
import com.motorbikebe.common.ApiStatus;
import com.motorbikebe.config.exception.RestApiException;
import com.motorbikebe.constant.classconstant.CarConstants;
import com.motorbikebe.constant.enumconstant.CarStatus;
import com.motorbikebe.dto.business.admin.carMng.CarSaveDTO;
import com.motorbikebe.entity.domain.BranchEntity;
import com.motorbikebe.entity.domain.CarEntity;
import com.motorbikebe.repository.business.admin.BranchRepository;
import com.motorbikebe.repository.business.admin.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarExcelServiceImpl implements CarExcelService {

    private final CarRepository carRepository;
    private final BranchRepository branchRepository;
    private final CarModelService carModelService;

    // Column headers
    private static final String[] HEADERS = {
            "Mẫu xe (*)", "Biển số xe (*)", "Loại xe (*)", "Chi nhánh",
            "Giá ngày (VNĐ)", "Giá giờ (VNĐ)", "Tình trạng xe", "Odometer (km)",
            "Trạng thái (*)", "Ghi chú", "Năm SX", "Xuất xứ",
            "Giá trị xe (VNĐ)", "Số khung", "Số máy", "Màu sắc",
            "Số đăng ký", "Tên trên đăng ký", "Nơi đăng ký",
            "Số HĐ bảo hiểm", "Ngày hết hạn BH"
    };

    private static final int[] REQUIRED_COLUMNS = {0, 1, 2, 8}; // Mẫu xe, Biển số, Loại xe, Trạng thái

    @Override
    public ByteArrayOutputStream downloadTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Danh sách xe");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }

            // Create 10 empty rows with data style
            for (int i = 1; i <= 10; i++) {
                Row row = sheet.createRow(i);
                for (int j = 0; j < HEADERS.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellStyle(dataStyle);
                }
            }

            // Add dropdowns for combobox columns
            addDropdownValidations(sheet, workbook);

            // Auto-size specific columns
            for (int i = 0; i < HEADERS.length; i++) {
                if (i == 0 || i == 1 || i == 2 || i == 3) { // Auto-size important columns
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(out);
            return out;

        } catch (IOException e) {
            log.error("Error creating Excel template", e);
            throw new RestApiException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public Integer importExcel(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RestApiException(ApiStatus.BAD_REQUEST);
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            List<CarSaveDTO> carsToSave = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            List<String> validCarModels = carModelService.getActiveModelNames();

            // Start from row 1 (skip header)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                try {
                    CarSaveDTO carDTO = parseRowToCarDTO(row, i + 1);
                    validateCarDTO(carDTO, i + 1, validCarModels);
                    carsToSave.add(carDTO);
                } catch (Exception e) {
                    errors.add("Dòng " + (i + 1) + ": " + e.getMessage());
                }
            }

            // Nếu có lỗi, throw exception và không lưu gì cả
            if (!errors.isEmpty()) {
                String errorMessage = "Có lỗi khi import:\n" + String.join("\n", errors);
                log.error("Import validation errors: {}", errorMessage);
                throw new RuntimeException(errorMessage);
            }

            // Validate duplicate license plates trong file
            List<String> licensePlates = carsToSave.stream()
                    .map(CarSaveDTO::getLicensePlate)
                    .collect(Collectors.toList());
            long uniqueCount = licensePlates.stream().distinct().count();
            if (uniqueCount < licensePlates.size()) {
                throw new RuntimeException("File có biển số xe trùng lặp");
            }

            // Check duplicate with database
            for (CarSaveDTO carDTO : carsToSave) {
                CarEntity existing = carRepository.findByLicensePlate(carDTO.getLicensePlate());
                if (existing != null) {
                    throw new RuntimeException("Biển số xe " + carDTO.getLicensePlate() + " đã tồn tại trong hệ thống");
                }
            }

            // Save all cars
            for (CarSaveDTO carDTO : carsToSave) {
                CarEntity carEntity = new CarEntity();
                mapDTOToEntity(carDTO, carEntity);
                carRepository.save(carEntity);
            }

            return carsToSave.size();

        } catch (RuntimeException e) {
            // Re-throw validation errors with message
            log.error("Import error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error importing Excel file", e);
            throw new RestApiException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ByteArrayOutputStream exportExcel(List<CarSaveDTO> cars) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Danh sách xe");

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            for (CarSaveDTO car : cars) {
                Row row = sheet.createRow(rowNum++);
                populateCarRow(row, car, dataStyle, dateStyle, numberStyle);
            }

            // Auto-size columns
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out;

        } catch (IOException e) {
            log.error("Error exporting Excel file", e);
            throw new RestApiException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============== Helper Methods ==============

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        return style;
    }

    private void addDropdownValidations(XSSFSheet sheet, XSSFWorkbook workbook) {
        // Tạo hidden sheet để chứa dropdown lists (vượt qua giới hạn 255 ký tự của Excel)
        XSSFSheet hiddenSheet = workbook.createSheet("Dropdowns");
        workbook.setSheetHidden(workbook.getSheetIndex(hiddenSheet), true);

        DataValidationHelper validationHelper = sheet.getDataValidationHelper();

        // Column A: Mẫu xe (dynamic)
        List<String> carModels = carModelService.getActiveModelNames();
        if (!carModels.isEmpty()) {
            createDropdownList(hiddenSheet, 0, carModels);
            addDropdownWithFormula(sheet, validationHelper, 1, 10, 0, 0,
                    "Dropdowns!$A$1:$A$" + carModels.size());
        }

        // Column B: Loại xe (CAR_TYPES)
        List<String> carTypes = CarConstants.CAR_TYPES;
        createDropdownList(hiddenSheet, 1, carTypes);
        addDropdownWithFormula(sheet, validationHelper, 1, 10, 2, 2,
                "Dropdowns!$B$1:$B$" + carTypes.size());

        // Column C: Chi nhánh (lấy từ DB)
        List<String> branchNames = branchRepository.findAll().stream()
                .map(BranchEntity::getName)
                .collect(Collectors.toList());
        if (!branchNames.isEmpty()) {
            createDropdownList(hiddenSheet, 2, branchNames);
            addDropdownWithFormula(sheet, validationHelper, 1, 10, 3, 3,
                    "Dropdowns!$C$1:$C$" + branchNames.size());
        }

        // Column D: Tình trạng xe (CAR_CONDITIONS)
        List<String> conditions = CarConstants.CAR_CONDITIONS;
        createDropdownList(hiddenSheet, 3, conditions);
        addDropdownWithFormula(sheet, validationHelper, 1, 10, 6, 6,
                "Dropdowns!$D$1:$D$" + conditions.size());

        // Column E: Trạng thái (CAR_STATUSES)
        List<String> statuses = Arrays.asList(
                CarStatus.AVAILABLE.getDescription(),
                CarStatus.NOT_AVAILABLE.getDescription(),
                CarStatus.LOST.getDescription()
        );
        createDropdownList(hiddenSheet, 4, statuses);
        addDropdownWithFormula(sheet, validationHelper, 1, 10, 8, 8,
                "Dropdowns!$E$1:$E$" + statuses.size());

        // Column F: Màu sắc (CAR_COLORS)
        List<String> colors = CarConstants.CAR_COLORS;
        createDropdownList(hiddenSheet, 5, colors);
        addDropdownWithFormula(sheet, validationHelper, 1, 10, 15, 15,
                "Dropdowns!$F$1:$F$" + colors.size());
    }

    /**
     * Tạo dropdown list trong hidden sheet
     */
    private void createDropdownList(XSSFSheet hiddenSheet, int columnIndex, List<String> values) {
        for (int i = 0; i < values.size(); i++) {
            Row row = hiddenSheet.getRow(i);
            if (row == null) {
                row = hiddenSheet.createRow(i);
            }
            Cell cell = row.createCell(columnIndex);
            cell.setCellValue(values.get(i));
        }
    }

    /**
     * Thêm dropdown validation sử dụng formula reference
     */
    private void addDropdownWithFormula(XSSFSheet sheet, DataValidationHelper validationHelper,
                                        int firstRow, int lastRow, int firstCol, int lastCol, String formula) {
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        DataValidationConstraint constraint = validationHelper.createFormulaListConstraint(formula);
        DataValidation validation = validationHelper.createValidation(constraint, addressList);
        validation.setShowErrorBox(true);
        validation.createErrorBox("Lỗi", "Vui lòng chọn giá trị từ danh sách");
        validation.setSuppressDropDownArrow(true);
        validation.setShowPromptBox(true);
        sheet.addValidationData(validation);
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (StringUtils.isNotBlank(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    private CarSaveDTO parseRowToCarDTO(Row row, int rowNum) {
        CarSaveDTO dto = new CarSaveDTO();

        dto.setModel(getCellValueAsString(row.getCell(0)));
        dto.setLicensePlate(getCellValueAsString(row.getCell(1)));
        dto.setCarType(getCellValueAsString(row.getCell(2)));

        // Branch - tìm ID từ tên
        String branchName = getCellValueAsString(row.getCell(3));
        if (StringUtils.isNotBlank(branchName)) {
            dto.setBranchId(branchRepository.findAll().stream()
                    .filter(b -> b.getName().equals(branchName))
                    .findFirst()
                    .map(b -> b.getId())
                    .orElse(null));
        }

        dto.setDailyPrice(getCellValueAsBigDecimal(row.getCell(4)));
        dto.setHourlyPrice(getCellValueAsBigDecimal(row.getCell(5)));
        dto.setCondition(getCellValueAsString(row.getCell(6)));
        dto.setCurrentOdometer(getCellValueAsInteger(row.getCell(7)));

        // Status - convert description to enum
        String statusDesc = getCellValueAsString(row.getCell(8));
        if (StringUtils.isNotBlank(statusDesc)) {
            for (CarStatus status : CarStatus.values()) {
                if (status.getDescription().equals(statusDesc)) {
                    dto.setStatus(status);
                    break;
                }
            }
        }

        dto.setNote(getCellValueAsString(row.getCell(9)));
        dto.setYearOfManufacture(getCellValueAsInteger(row.getCell(10)));
        dto.setOrigin(getCellValueAsString(row.getCell(11)));
        dto.setValue(getCellValueAsBigDecimal(row.getCell(12)));
        dto.setFrameNumber(getCellValueAsString(row.getCell(13)));
        dto.setEngineNumber(getCellValueAsString(row.getCell(14)));
        dto.setColor(getCellValueAsString(row.getCell(15)));
        dto.setRegistrationNumber(getCellValueAsString(row.getCell(16)));
        dto.setRegisteredOwnerName(getCellValueAsString(row.getCell(17)));
        dto.setRegistrationPlace(getCellValueAsString(row.getCell(18)));
        dto.setInsuranceContractNumber(getCellValueAsString(row.getCell(19)));
        dto.setInsuranceExpiryDate(getCellValueAsDate(row.getCell(20)));

        return dto;
    }

    private void validateCarDTO(CarSaveDTO dto, int rowNum, List<String> validCarModels) {
        List<String> errors = new ArrayList<>();

        // Required fields
        if (StringUtils.isBlank(dto.getModel())) {
            errors.add("Mẫu xe không được để trống");
        } else if (validCarModels == null || !validCarModels.contains(dto.getModel())) {
            errors.add("Mẫu xe không hợp lệ");
        }

        if (StringUtils.isBlank(dto.getLicensePlate())) {
            errors.add("Biển số xe không được để trống");
        }

        if (StringUtils.isBlank(dto.getCarType())) {
            errors.add("Loại xe không được để trống");
        } else if (!CarConstants.CAR_TYPES.contains(dto.getCarType())) {
            errors.add("Loại xe không hợp lệ");
        }

        if (dto.getStatus() == null) {
            errors.add("Trạng thái không được để trống");
        }

        // Validate condition if provided
        if (StringUtils.isNotBlank(dto.getCondition()) &&
                !CarConstants.CAR_CONDITIONS.contains(dto.getCondition())) {
            errors.add("Tình trạng xe không hợp lệ");
        }

        // Validate color if provided
        if (StringUtils.isNotBlank(dto.getColor()) &&
                !CarConstants.CAR_COLORS.contains(dto.getColor())) {
            errors.add("Màu sắc không hợp lệ");
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException(String.join(", ", errors));
        }
    }

    private void populateCarRow(Row row, CarSaveDTO car, CellStyle dataStyle, CellStyle dateStyle, CellStyle numberStyle) {
        int colNum = 0;

        createCell(row, colNum++, car.getModel(), dataStyle);
        createCell(row, colNum++, car.getLicensePlate(), dataStyle);
        createCell(row, colNum++, car.getCarType(), dataStyle);

        // Branch name
        String branchName = "";
        if (StringUtils.isNotBlank(car.getBranchId())) {
            branchName = branchRepository.findById(car.getBranchId())
                    .map(b -> b.getName())
                    .orElse("");
        }
        createCell(row, colNum++, branchName, dataStyle);

        createCellNumber(row, colNum++, car.getDailyPrice(), numberStyle);
        createCellNumber(row, colNum++, car.getHourlyPrice(), numberStyle);
        createCell(row, colNum++, car.getCondition(), dataStyle);
        createCellNumber(row, colNum++, car.getCurrentOdometer(), numberStyle);
        createCell(row, colNum++, car.getStatus() != null ? car.getStatus().getDescription() : "", dataStyle);
        createCell(row, colNum++, car.getNote(), dataStyle);
        createCellNumber(row, colNum++, car.getYearOfManufacture(), numberStyle);
        createCell(row, colNum++, car.getOrigin(), dataStyle);
        createCellNumber(row, colNum++, car.getValue(), numberStyle);
        createCell(row, colNum++, car.getFrameNumber(), dataStyle);
        createCell(row, colNum++, car.getEngineNumber(), dataStyle);
        createCell(row, colNum++, car.getColor(), dataStyle);
        createCell(row, colNum++, car.getRegistrationNumber(), dataStyle);
        createCell(row, colNum++, car.getRegisteredOwnerName(), dataStyle);
        createCell(row, colNum++, car.getRegistrationPlace(), dataStyle);
        createCell(row, colNum++, car.getInsuranceContractNumber(), dataStyle);
        createCellDate(row, colNum++, car.getInsuranceExpiryDate(), dateStyle);
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void createCellNumber(Row row, int column, Number value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        }
        cell.setCellStyle(style);
    }

    private void createCellDate(Row row, int column, Date value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value);
        }
        cell.setCellStyle(style);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim().replaceAll(",", "");
                if (StringUtils.isNotBlank(value)) {
                    return new BigDecimal(value);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim().replaceAll(",", "");
                if (StringUtils.isNotBlank(value)) {
                    return Integer.parseInt(value);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private Date getCellValueAsDate(Cell cell) {
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (StringUtils.isNotBlank(value)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    return sdf.parse(value);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private void mapDTOToEntity(CarSaveDTO dto, CarEntity entity) {
        entity.setModel(dto.getModel());
        entity.setLicensePlate(dto.getLicensePlate());
        entity.setCarType(dto.getCarType());
        entity.setBranchId(dto.getBranchId());
        entity.setDailyPrice(dto.getDailyPrice());
        entity.setHourlyPrice(dto.getHourlyPrice());
        entity.setCondition(dto.getCondition());
        entity.setCurrentOdometer(dto.getCurrentOdometer());
        entity.setStatus(dto.getStatus());
        entity.setNote(dto.getNote());
        entity.setYearOfManufacture(dto.getYearOfManufacture());
        entity.setOrigin(dto.getOrigin());
        entity.setValue(dto.getValue());
        entity.setFrameNumber(dto.getFrameNumber());
        entity.setEngineNumber(dto.getEngineNumber());
        entity.setColor(dto.getColor());
        entity.setRegistrationNumber(dto.getRegistrationNumber());
        entity.setRegisteredOwnerName(dto.getRegisteredOwnerName());
        entity.setRegistrationPlace(dto.getRegistrationPlace());
        entity.setInsuranceContractNumber(dto.getInsuranceContractNumber());
        entity.setInsuranceExpiryDate(dto.getInsuranceExpiryDate());
    }
}

