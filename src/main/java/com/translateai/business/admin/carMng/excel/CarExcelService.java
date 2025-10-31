package com.translateai.business.admin.carMng.excel;

import com.translateai.dto.business.admin.carMng.CarSaveDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Service xử lý import/export Excel cho quản lý xe
 */
public interface CarExcelService {

    /**
     * Tạo file Excel mẫu với 10 dòng trống
     * Các trường combobox sẽ có dropdown validation
     *
     * @return ByteArrayOutputStream chứa file Excel
     */
    ByteArrayOutputStream downloadTemplate();

    /**
     * Import dữ liệu từ file Excel
     * Kiểm tra validation cho từng record
     * Nếu có 1 record lỗi thì throw exception, không lưu record nào
     *
     * @param file File Excel upload
     * @return Số lượng record đã import thành công
     */
    Integer importExcel(MultipartFile file);

    /**
     * Export danh sách xe ra file Excel
     * Chỉ export các record có trong danh sách (page hiện tại)
     *
     * @param cars Danh sách xe cần export
     * @return ByteArrayOutputStream chứa file Excel
     */
    ByteArrayOutputStream exportExcel(List<CarSaveDTO> cars);
}

