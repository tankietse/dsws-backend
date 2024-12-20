package com.webgis.dsws.util;

import com.webgis.dsws.exception.DataImportException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Lớp tiện ích để kiểm tra và xác thực dữ liệu nhập vào.
 * Cung cấp các phương thức để kiểm tra tính hợp lệ của các loại dữ liệu khác nhau.
 */
@Component
public class DataValidator {
    // Định dạng số điện thoại: 10-11 số
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,11}$");

    // Độ dài tối đa cho tên
    private static final int MAX_NAME_LENGTH = 100;

    /**
     * Kiểm tra tính hợp lệ của số điện thoại theo định dạng yêu cầu.
     *
     * @param phone Số điện thoại cần kiểm tra
     * @param rowNum Số thứ tự dòng trong tệp dữ liệu
     * @throws DataImportException khi số điện thoại không hợp lệ
     */
    public void validatePhoneNumber(String phone, int rowNum) throws DataImportException {
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new DataImportException("Số điện thoại không hợp lệ tại dòng " + rowNum);
        }
    }

    /**
     * Kiểm tra độ dài của tên theo giới hạn cho phép.
     *
     * @param name Tên cần kiểm tra
     * @param rowNum Số thứ tự dòng trong tệp dữ liệu
     * @throws DataImportException khi tên vượt quá độ dài cho phép
     */
    public void validateName(String name, int rowNum) throws DataImportException {
        if (name.length() > MAX_NAME_LENGTH) {
            throw new DataImportException("Tên quá dài tại dòng " + rowNum);
        }
    }

    /**
     * Chuyển đổi giá trị ô Excel sang String, có kiểm tra hợp lệ.
     *
     * @param cell      ô Excel
     * @param fieldName tên trường
     * @param rowNum    số dòng trong file Excel
     * @return giá trị của ô dưới dạng String
     * @throws DataImportException nếu giá trị không hợp lệ
     */
    public String getCellValueAsString(Cell cell, String fieldName, int rowNum) {
        if (cell == null) {
            throw new DataImportException(fieldName + " không được để trống tại dòng " + rowNum);
        }

        String value = switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> throw new DataImportException(
                    fieldName + " không đúng định dạng tại dòng " + rowNum);
        };

        if (value.isEmpty()) {
            throw new DataImportException(fieldName + " không được để trống tại dòng " + rowNum);
        }

        return value;
    }

    /**
     * Chuyển đổi giá trị ô Excel không bắt buộc sang String.
     *
     * @param cell ô Excel
     * @return giá trị của ô dưới dạng String hoặc null nếu ô trống
     */
    public String getOptionalCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> {
                String value = cell.getStringCellValue().trim();
                yield value.isEmpty() ? null : value;
            }
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    /**
     * Xác thực và lấy giá trị từ một trường bắt buộc.
     *
     * @param value     giá trị của trường
     * @param fieldName tên trường
     * @param rowNum    số dòng trong file CSV
     * @return giá trị đã xác thực
     * @throws DataImportException nếu giá trị không hợp lệ
     */
    public String validateAndGetValue(String value, String fieldName, int rowNum) throws DataImportException {
        if (value == null || value.trim().isEmpty()) {
            throw new DataImportException(fieldName + " không được để trống tại dòng " + rowNum);
        }
        return value.trim();
    }

    /**
     * Lấy giá trị từ một trường không bắt buộc.
     *
     * @param value giá trị của trường
     * @return giá trị đã xác thực hoặc null nếu trống
     */
    public String getOptionalValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}