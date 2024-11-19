package com.webgis.dsws.util;

import com.webgis.dsws.exception.DataImportException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DataValidator {
    // Định dạng số điện thoại: 10-11 số
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,11}$");

    // Độ dài tối đa cho tên
    private static final int MAX_NAME_LENGTH = 100;

    // Kiểm tra số điện thoại hợp lệ
    // public void validatePhoneNumber(String phone, int rowNum) throws
    // DataImportException {
    // if (!PHONE_PATTERN.matcher(phone).matches()) {
    // throw new DataImportException("Số điện thoại không hợp lệ tại dòng " +
    // rowNum);
    // }
    // }

    // Kiểm tra tên không quá dài
    public void validateName(String name, int rowNum) throws DataImportException {
        if (name.length() > MAX_NAME_LENGTH) {
            throw new DataImportException("Tên quá dài tại dòng " + rowNum);
        }
    }

    // Chuyển đổi giá trị ô Excel sang String, có kiểm tra hợp lệ
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

    // Dành cho các ô Excel không bắt buộc (có thể để trống)
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
}